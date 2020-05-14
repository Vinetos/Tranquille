package dummydomain.yetanothercallblocker.sia;

import java.io.IOException;
import java.io.InputStream;

public interface Storage {

    String getDataDirPath();

    String getCacheDirPath();

    InputStream openFile(String fileName, boolean internal) throws IOException;

}
