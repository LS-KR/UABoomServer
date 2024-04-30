package io.github.elihuso.data;

import io.github.elihuso.logic.MathEx;
import io.github.elihuso.logic.Streaming;
import io.github.elihuso.module.Logger;
import io.github.elihuso.style.LoggerLevel;

import java.io.IOException;
import java.io.InputStream;

public class Boom {
    private final byte[] boomZstdByte;
    private final byte[] boomGzipByte;

    public Boom(ClassLoader loader) throws IOException {
        Logger.Log(LoggerLevel.NOTIFICATION, "Loader: " + loader.getName());
        try {
            InputStream is = loader.getResourceAsStream("zeroboom.zst");
            boomZstdByte = Streaming.ByteInputStream(is);
            Logger.Log(LoggerLevel.NOTIFICATION, "Loaded Zstd");
            is = loader.getResourceAsStream("zeroboom.gz");
            boomGzipByte = Streaming.ByteInputStream(is);
            Logger.Log(LoggerLevel.NOTIFICATION, "Loaded Gzip");
        }
        catch (IOException ex) {
            Logger.Log(LoggerLevel.NEGATIVE, ex.getLocalizedMessage());
            throw ex;
        }
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
