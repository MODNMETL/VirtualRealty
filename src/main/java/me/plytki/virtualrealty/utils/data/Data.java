package me.plytki.virtualrealty.utils.data;

import net.minecraft.server.v1_13_R2.EntityChicken;
import net.minecraft.server.v1_13_R2.EntityPlayer;

import java.io.*;
import java.util.zip.*;

public class Data {

    public void compressData(byte[] data, File file) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(file)) {
            Deflater d = new Deflater();
            DeflaterOutputStream dout = new DeflaterOutputStream(outputStream, d);
            dout.write(data);
            dout.close();
        }
    }

    public byte[] decompressData(File file) throws IOException {
        try (InputStream inputStream = new FileInputStream(file)) {
            InflaterInputStream newInput = new InflaterInputStream(inputStream);
            ByteArrayOutputStream bout = new ByteArrayOutputStream(512);
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
