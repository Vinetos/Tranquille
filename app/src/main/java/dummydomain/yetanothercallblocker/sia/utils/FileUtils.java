package dummydomain.yetanothercallblocker.sia.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class FileUtils {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    public static File createDirectory(String path) {
        return createDirectory(new File(path));
    }

    public static File createDirectory(String base, String dir) {
        return createDirectory(new File(base, dir));
    }

    public static File createDirectory(File base, String dir) {
        return createDirectory(new File(base, dir));
    }

    public static File createDirectory(File d) {
        if (!d.exists()) {
            LOG.debug("createDirectory() creating: {}, result: {}", d.getAbsolutePath(), d.mkdir());
        }
        if (!d.isDirectory()) {
            LOG.error("createDirectory() is not a directory: {}", d.getAbsolutePath());
        }
        return d;
    }

    public static void delete(File base, String name) {
        delete(new File(base, name));
    }

    public static void delete(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] list = file.listFiles();
                if (list != null) {
                    for (File f : list) {
                        delete(f);
                    }
                }
            } else {
                file.delete();
            }
        }
    }

}
