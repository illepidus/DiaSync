package ru.krotarnya.diasync.common.util;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressionUtils {
    private static final int BUFFER_SIZE = 1024;

    public static byte[] compress(byte[] input) {
        Deflater compressor = new Deflater(Deflater.BEST_COMPRESSION);
        compressor.setInput(input);
        compressor.finish();

        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];

        while (!compressor.finished()) {
            int bytes = compressor.deflate(buffer);
            if (bytes > 0) bao.write(buffer, 0, bytes);
        }
        compressor.end();
        return bao.toByteArray();
    }

    public static byte[] decompress(byte[] input) throws DataFormatException {
        Inflater decompressor = new Inflater();
        decompressor.setInput(input);

        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];

        while (!decompressor.finished()) {
            int bytes = decompressor.inflate(buffer);
            if (bytes > 0) bao.write(buffer, 0, bytes);
        }

        decompressor.end();
        return bao.toByteArray();
    }
}
