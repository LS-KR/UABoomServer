package io.github.elihuso.data;

import io.github.elihuso.logic.MathEx;
import io.github.elihuso.logic.Streaming;

import java.io.IOException;
import java.io.InputStream;

public class Boom {
    private final byte[] boomZstdByte;
    private final byte[] boomGzipByte;

    public Boom() throws IOException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream is = loader.getResourceAsStream("zeroboom.zstd");
        boomZstdByte = Streaming.ByteInputStream(is);
        is = loader.getResourceAsStream("zeroboom.gz");
        boomGzipByte = Streaming.ByteInputStream(is);
    }

    public byte[] getBoomZstdByte() {
        return boomZstdByte;
    }

    public long getZstdLength() {
        return MathEx.getByteLength(boomZstdByte);
    }

    public byte[] getBoomGzipByte() {
        return boomGzipByte;
    }

    public long getGzipLength() {
        return MathEx.getByteLength(boomGzipByte);
    }
}
