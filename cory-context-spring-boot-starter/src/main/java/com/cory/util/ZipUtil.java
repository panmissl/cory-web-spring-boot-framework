package com.cory.util;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    public static void unzip(File zipFile, File destDir) throws IOException {
        try (ZipFile zip = new ZipFile(zipFile)) {
            for (Enumeration entries = zip.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                InputStream in = zip.getInputStream(entry);
                String curEntryName = entry.getName();
                if (curEntryName.contains("__MACOSX")) {
                    continue;
                }
                File outFile = new File(destDir, curEntryName);
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    OutputStream out = new FileOutputStream(outFile);
                    byte[] buf1 = new byte[1024];
                    int len;
                    while ((len = in.read(buf1)) > 0) {
                        out.write(buf1, 0, len);
                    }
                    in.close();
                    out.close();
                }
            }
        }
    }

    public static byte[] zipDir(File originalDir) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        File[] files = originalDir.listFiles();
        try {
            for (File file : files) {
                zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
                zipOutputStream.write(IOUtils.readFully(new FileInputStream(file), (int) file.length()));
                zipOutputStream.closeEntry();
            }
            zipOutputStream.close();
            byteArrayOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] zipFile(File originalFile) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        try {
            zipOutputStream.putNextEntry(new ZipEntry(originalFile.getName()));
            zipOutputStream.write(IOUtils.readFully(new FileInputStream(originalFile), (int) originalFile.length()));
            zipOutputStream.closeEntry();

            zipOutputStream.close();
            byteArrayOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
