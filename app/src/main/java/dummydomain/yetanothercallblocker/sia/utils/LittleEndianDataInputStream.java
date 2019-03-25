package dummydomain.yetanothercallblocker.sia.utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class LittleEndianDataInputStream {

    private InputStream in;

    private byte[] buffer;

    public LittleEndianDataInputStream(InputStream in) {
        this(in, 200);
    }

    public LittleEndianDataInputStream(InputStream in, int bufferSize) {
        this.in = in;
        buffer = new byte[bufferSize];
    }

    public byte readByte() throws IOException {
        int ch = in.read();
        if (ch < 0) throw new EOFException();
        return (byte)(ch);
    }

    public int readInt() throws IOException {
        if (in.read(buffer, 0, 4) == 4) {
            return (((buffer[0] & 0xff) | ((buffer[1] & 0xff) << 8))
                    | ((buffer[2] & 0xff) << 16)) | ((buffer[3] & 0xff) << 24);
        }
        throw new EOFException();
    }

    public long readLong() throws IOException {
        if (in.read(buffer, 0, 8) == 8) {
            return (((((((((((((((long) (buffer[7] & 0xff)) << 8)
                    | ((long) (buffer[6] & 0xff))) << 8)
                    | ((long) (buffer[5] & 0xff))) << 8)
                    | ((long) (buffer[4] & 0xff))) << 8)
                    | ((long) (buffer[3] & 0xff))) << 8)
                    | ((long) (buffer[2] & 0xff))) << 8)
                    | ((long) (buffer[1] & 0xff))) << 8)
                    | ((long) (buffer[0] & 0xff));
        }
        throw new EOFException();
    }

    public String readUtf8StringChars(int length) throws IOException {
        if (buffer.length < length) buffer = new byte[length];

        if (in.read(buffer, 0, length) == length) {
            return new String(buffer, 0, length, Charset.forName("UTF-8"));
        }
        throw new EOFException();
    }

}
