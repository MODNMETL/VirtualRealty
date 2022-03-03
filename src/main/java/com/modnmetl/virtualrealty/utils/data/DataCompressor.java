package com.modnmetl.virtualrealty.utils.data;

import java.io.*;
import java.util.zip.*;

public class DataCompressor {

    public void compressData(byte[] data, File file) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(file)) {
            Deflater d = new Deflater();
            d.setLevel(4);
            DeflaterOutputStream dout = new DeflaterOutputStream(outputStream, d);
            dout.write(data);
            dout.close();
        }
    }

    public byte[] decompressData(File file) throws IOException {
        try (InputStream inputStream = new FileInputStream(file)) {
            InflaterInputStream newInput = new InflaterInputStream(inputStream);
            ByteArrayOutputStream bout = new ByteArrayOutputStream(512*2*2*2*2);
            int b;
            while ((b = newInput.read()) != -1) {
                bout.write(b);
            }
            newInput.close();
            bout.close();
            return bout.toByteArray();
        }
    }

}
