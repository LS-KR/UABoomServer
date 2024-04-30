package io.github.elihuso.data;

import io.github.elihuso.logic.MathEx;
import io.github.elihuso.logic.Streaming;

import java.io.IOException;
import java.io.InputStream;

public class Boom {
    private final byte[] boomByte;

    public Boom() throws IOException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream is = loader.getResourceAsStream("zeroboom.zstd");
        boomByte = Streaming.ByteInputStream(is);
    }

    public byte[] getBoomByte() {
        return boomByte;
    }

    public long getLength() {
        return MathEx.getByteLength(boomByte);
    }
}
