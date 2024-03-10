package fr.vinetos.tranquille.data;

import android.text.TextUtils;

import androidx.core.util.ObjectsCompat;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import fr.vinetos.tranquille.data.db.BlacklistDao;
import fr.vinetos.tranquille.data.db.BlacklistItem;

import static fr.vinetos.tranquille.data.BlacklistUtils.cleanPattern;
import static fr.vinetos.tranquille.data.BlacklistUtils.isValidPattern;
import static fr.vinetos.tranquille.data.BlacklistUtils.patternFromHumanReadable;
import static fr.vinetos.tranquille.data.BlacklistUtils.patternToHumanReadable;

public class BlacklistImporterExporter {

    private static final Logger LOG = LoggerFactory.getLogger(BlacklistImporterExporter.class);

    private static final String HEADER_ID = "ID";
    private static final String HEADER_NAME = "name";
    private static final String HEADER_PATTERN = "pattern";

    private static final int INDEX_ID = 0;
    private static final int INDEX_NAME = 1;
    private static final int INDEX_PATTERN = 2;
    private static final int INDEX_CREATION_DATE = 3;
    private static final int INDEX_NUMBER_OF_CALLS = 4;
    private static final int INDEX_LAST_CALL_DATE = 5;

    public boolean writeBackup(Iterable<BlacklistItem> blacklistItems, Appendable out) {
        try (CSVPrinter printer = CSVFormat.DEFAULT.print(out)) {
            printer.printRecord(HEADER_ID, HEADER_NAME, HEADER_PATTERN,
                    "creationTimestamp", "numberOfCalls", "lastCallTimestamp");

            for (BlacklistItem item : blacklistItems) {
                printer.printRecord(item.getId(), item.getName(),
                        patternToHumanReadable(item.getPattern()),
                        item.getCreationDate().getTime(), item.getNumberOfCalls(),
                        item.getLastCallDate() != null ? item.getLastCallDate().getTime() : "");
            }
        } catch (IOException e) {
            LOG.warn("write()", e);
            return false;
        }

        return true;
    }

    public boolean importBlacklist(BlacklistDao blacklistDao, BlacklistService blacklistService,
                                   FileDescriptor fileDescriptor) {
        List<BlacklistItem> items = null;

        try (FileInputStream inputStream = new FileInputStream(fileDescriptor)) {
            items = read(inputStream);
        } catch (IOException e) {
            LOG.warn("importBlacklist()", e);
        }

        if (items == null) return false;

        for (BlacklistItem item : items) {
            BlacklistItem existingItem = null;

            if (item.getId() != null) {
                existingItem = blacklistDao.findById(item.getId());

                if (existingItem != null && !ObjectsCompat
                        .equals(item.getPattern(), existingItem.getPattern())) {
                    item.setId(null);
                    existingItem = null;
                }
            }

            if (existingItem == null) {
                existingItem = blacklistDao.findByPattern(item.getPattern());
            }

            if (existingItem != null) {
                boolean changed = false;
                if (TextUtils.isEmpty(existingItem.getName())
                        && !TextUtils.isEmpty(item.getName())) {
                    existingItem.setName(item.getName());
                    changed = true;
                }
                if (existingItem.getNumberOfCalls() < item.getNumberOfCalls()) {
                    existingItem.setNumberOfCalls(item.getNumberOfCalls());
                    changed = true;
                }
                if (item.getLastCallDate() != null && (existingItem.getLastCallDate() == null
                        || existingItem.getLastCallDate().before(item.getLastCallDate()))) {
                    existingItem.setLastCallDate(item.getLastCallDate());
                    changed = true;
                }

                if (changed) {
                    blacklistService.save(existingItem);
                }
            } else {
                blacklistService.insert(item);
            }
        }

        return true;
    }

    public List<BlacklistItem> read(InputStream inputStream) throws IOException {
        List<BlacklistItem> items = null;

        try (BufferedInputStream bis = new BufferedInputStream(inputStream)) {
            // BufferedReaders used in `isYacbBackup` and `isNoPhoneSpamBackup` use 8192 char buffers,
            // the max size of a UTF-8 char is 4 bytes.
            bis.mark(8192 * 4);

            if (isYacbBackup(new InputStreamReader(bis))) {
                bis.reset();
                LOG.info("importBlacklist() importing as YACB backup");
                items = readYacbBackup(new InputStreamReader(bis));
            } else {
                LOG.debug("importBlacklist() not a YACB backup");
                bis.reset();
                if (isNoPhoneSpamBackup(new InputStreamReader(bis))) {
                    bis.reset();
                    LOG.info("importBlacklist() trying to import as NoPhoneSpam backup");
                    items = readNoPhoneSpamBackup(new InputStreamReader(bis));
                } else {
                    LOG.debug("importBlacklist() not a NoPhoneSpam backup");
                }
            }
        }

        return items;
    }

    public boolean isYacbBackup(Reader in) {
        try {
            CSVParser parser = CSVFormat.DEFAULT.parse(in); // do NOT close

            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator.hasNext()) {
                CSVRecord record = iterator.next();

                if (record.size() < INDEX_PATTERN + 1) return false;

                boolean foundHeader = checkYacbHeader(record);
                LOG.debug("isYacbBackup() found header={}", foundHeader);
                if (foundHeader) return true;

                // check that the types match
                try {
                    if (!TextUtils.isEmpty(get(record, INDEX_ID))) {
                        Long.parseLong(get(record, INDEX_ID));
                    }

                    if (!isValidPattern(cleanPattern(patternFromHumanReadable(
                            get(record, INDEX_PATTERN))))) {
                        return false;
                    }

                    if (!TextUtils.isEmpty(get(record, INDEX_CREATION_DATE))) {
                        Long.parseLong(get(record, INDEX_CREATION_DATE));
                    }

                    if (!TextUtils.isEmpty(get(record, INDEX_NUMBER_OF_CALLS))) {
                        Integer.parseInt(get(record, INDEX_NUMBER_OF_CALLS));
                    }

                    if (!TextUtils.isEmpty(get(record, INDEX_LAST_CALL_DATE))) {
                        Long.parseLong(get(record, INDEX_LAST_CALL_DATE));
                    }
                } catch (Exception e) {
                    LOG.debug("isYacbBackup() error parsing item", e);
                    return false;
                }

                return true;
            }
        } catch (IOException e) {
            LOG.debug("isYacbBackup()", e);
            return false;
        }

        LOG.debug("isYacbBackup() empty file?");
        return true;
    }

    private boolean checkYacbHeader(CSVRecord record) {
        boolean foundHeader = false;
        try {
            foundHeader = HEADER_ID.equals(record.get(INDEX_ID))
                    && HEADER_NAME.equals(record.get(INDEX_NAME))
                    && HEADER_PATTERN.equals(record.get(INDEX_PATTERN));
        } catch (Exception e) {
            LOG.warn("checkYacbHeader() error checking header", e);
        }
        return foundHeader;
    }

    public List<BlacklistItem> readYacbBackup(Reader in) {
        try (CSVParser parser = CSVFormat.DEFAULT.parse(in)) {
            List<BlacklistItem> blacklistItems = new ArrayList<>();

            boolean first = true;

            for (CSVRecord record : parser) {
                if (first) {
                    first = false;

                    boolean foundHeader = checkYacbHeader(record);
                    LOG.debug("readYacbBackup() found header={}", foundHeader);

                    if (foundHeader) {
                        continue;
                    }
                }

                BlacklistItem item = new BlacklistItem();

                boolean enough = false;

                try {
                    if (!TextUtils.isEmpty(get(record, INDEX_ID))) {
                        item.setId(Long.valueOf(get(record, INDEX_ID)));
                    }

                    item.setName(record.get(INDEX_NAME));

                    item.setPattern(cleanPattern(patternFromHumanReadable(
                            record.get(INDEX_PATTERN))));

                    enough = true;

                    if (!TextUtils.isEmpty(get(record, INDEX_CREATION_DATE))) {
                        item.setCreationDate(new Date(Long.parseLong(
                                get(record, INDEX_CREATION_DATE))));
                    }

                    if (!TextUtils.isEmpty(get(record, INDEX_NUMBER_OF_CALLS))) {
                        item.setNumberOfCalls(Integer.parseInt(
                                get(record, INDEX_NUMBER_OF_CALLS)));
                    }

                    if (!TextUtils.isEmpty(get(record, INDEX_LAST_CALL_DATE))) {
                        item.setLastCallDate(new Date(Long.parseLong(
                                get(record, INDEX_LAST_CALL_DATE))));
                    }
                } catch (Exception e) {
                    LOG.warn("readYacbBackup() error parsing item", e);
                }

                LOG.trace("readYacbBackup() enough={}", enough);
                if (enough) {
                    blacklistItems.add(sanitize(item));
                }
            }

            return blacklistItems;
        } catch (IOException e) {
            LOG.warn("readYacbBackup()", e);
            return null;
        }
    }

    private static String get(CSVRecord record, int index) {
        return record.size() > index ? record.get(index) : null;
    }

    public boolean isNoPhoneSpamBackup(Reader in) {
        try {
            BufferedReader br = new BufferedReader(in); // do NOT close

            String delimiter = ": ";

            String line = br.readLine();
            if (line != null) {
                return line.contains(delimiter);
            }
        } catch (IOException e) {
            LOG.warn("isNoPhoneSpamBackup()", e);
            return false;
        }

        return true;
    }

    public List<BlacklistItem> readNoPhoneSpamBackup(Reader in) {
        try (BufferedReader br = new BufferedReader(in)) {
            List<BlacklistItem> blacklistItems = new ArrayList<>();

            String delimiter = ": ";

            String line;
            while ((line = br.readLine()) != null) {
                int delimiterIndex = line.indexOf(delimiter);
                if (delimiterIndex == -1) {
                    LOG.warn("readNoPhoneSpamBackup() incorrect format: no delimiter");
                    continue;
                }

                String pattern = line.substring(0, delimiterIndex).trim();
                String name = line.substring(delimiterIndex + delimiter.length());

                BlacklistItem item = new BlacklistItem(name,
                        cleanPattern(patternFromHumanReadable(pattern)));
                blacklistItems.add(sanitize(item));
            }

            return blacklistItems;
        } catch (IOException e) {
            LOG.warn("readNoPhoneSpamBackup()", e);
            return null;
        }
    }

    private BlacklistItem sanitize(BlacklistItem item) {
        if (item.getId() != null && item.getId() < 1) {
            item.setId(null);
        }
        item.setInvalid(!isValidPattern(item.getPattern()));
        if (item.getCreationDate() == null) {
            item.setCreationDate(new Date());
        }
        if (item.getNumberOfCalls() < 0) {
            item.setNumberOfCalls(0);
        }

        return item;
    }

}
