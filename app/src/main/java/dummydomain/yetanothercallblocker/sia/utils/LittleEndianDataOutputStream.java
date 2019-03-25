package dummydomain.yetanothercallblocker.sia.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class LittleEndianDataOutputStream {

    private OutputStream out;

    private byte[] buffer;

    public LittleEndianDataOutputStream(OutputStream out) {
        this(out, 8);
    }

    public LittleEndianDataOutputStream(OutputStream out, int bufferSize) {
        this.out = out;
        buffer = new byte[bufferSize];
    }

    public void writeByte(byte b) throws IOException {
        out.write(b);
    }

    public void writeInt(int i) throws IOException {
        buffer[0] = (byte) (i & 0xff);
        buffer[1] = (byte) ((i >>> 8) & 0xff);
        buffer[2] = (byte) ((i >>> 16) & 0xff);
        buffer[3] = (byte) ((i >>> 24) & 0xff);
        out.write(buffer, 0, 4);
    }

    public void writeLong(long l) throws IOException {
        buffer[0] = (byte) (l & 0xff);
        buffer[1] = (byte) ((l >>> 8) & 0xff);
        buffer[2] = (byte) ((l >>> 16) & 0xff);
        buffer[3] = (byte) ((l >>> 24) & 0xff);
        buffer[4] = (byte) ((l >>> 32) & 0xff);
        buffer[5] = (byte) ((l >>> 40) & 0xff);
        buffer[6] = (byte) ((l >>> 48) & 0xff);
        buffer[7] = (byte) ((l >>> 56) & 0xff);
        out.write(buffer, 0, 8);
    }

    public void writeUtf8StringChars(String str) throws IOException {
        byte[] bytes = str.getBytes(Charset.forName("UTF-8"));
        out.write(bytes, 0, bytes.length);
    }

}
