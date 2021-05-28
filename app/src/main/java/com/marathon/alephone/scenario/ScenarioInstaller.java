package com.marathon.alephone.scenario;

import android.content.Context;
import android.net.Uri;

import com.marathon.alephone.IInstallListener;

import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ScenarioInstaller {
    private final Uri source;
    private final Context context;

    protected class ScenarioException extends Exception {
        public ScenarioException(String message) {
            super(message);
        }
    }

    public ScenarioInstaller(Uri source, Context context) {
        this.source = source;
        this.context = context;
    }

    public void install(
        IInstallListener listener,
        boolean deleteAfterInstall
    ) {
        String md5 = null;

        try {
            md5 = getMD5();
        } catch (IOException e) {
            listener.onDataInstallError(e.getLocalizedMessage());
            return;
        }

        File filesDir = new File(context.getFilesDir(), md5);
        installData(listener, filesDir, md5, null);

        if (deleteAfterInstall) {
            this.context.getContentResolver().delete(this.source, null, null);
        }
    }

    public void installData(
        IInstallListener listener,
        File filesDir,
        String md5,
        List<String> excludeFiles
    ) {
        long size = 0;

        excludeFiles = excludeFiles != null ? excludeFiles : new ArrayList<String>();

        try {
            int totalCount = validateScenario();
            InputStream is = context.getContentResolver().openInputStream(this.source);

            if (is == null) {
                listener.onDataInstallError("Can't open selected file");
                return;
            }

            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));

            ZipEntry ze;
            byte[] buffer = new byte[1024 * 8];
            int count;
            int processedCount = 0;

            listener.onDataInstallStarted(filesDir, totalCount, md5);

            while ((ze = zis.getNextEntry()) != null) {
                String filename = ze.getName();
                boolean skip = false;

                for (String s : excludeFiles) {
                    if (s.equals(filename)) {
                        skip = true;
                        break;
                    }
                }

                if (!skip) {
                    if (ze.isDirectory()) {
                        File fmd = new File(filesDir, filename);

                        if (!fmd.exists() && !fmd.mkdirs()) {
                            listener.onDataInstallError(
                                    String.format("Can't create directory: %s", fmd.getAbsolutePath())
                            );

                            return;
                        }

                        listener.onDataInstallProgress(++processedCount, totalCount);
                        continue;
                    }

                    File fo = new File(filesDir, filename);
                    FileOutputStream fouts = new FileOutputStream(fo);

                    while ((count = zis.read(buffer)) != -1) {
                        fouts.write(buffer, 0, count);
                        size += count;
                    }

                    fouts.close();
                }

                zis.closeEntry();
                listener.onDataInstallProgress(++processedCount, totalCount);
            }

            zis.close();
            listener.onDataInstallDone(filesDir, size, md5);
        } catch (Exception e) {
            listener.onDataInstallError(e.getLocalizedMessage());
        }
    }

    private String getMD5() throws IOException {
        MessageDigest digest;

        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        InputStream is = getInputStream();

        byte[] buffer = new byte[8192];
        int read;

        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }

            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            output = String.format("%32s", output).replace(' ', '0');

            return output;
        } finally {
            is.close();
        }
    }

    private int validateScenario() throws IOException, ScenarioException {
        InputStream is = getInputStream();

        ZipInputStream zis = new ZipInputStream(is);
        int count = 0;
        boolean valid = false;
        ZipEntry entry = null;

        while ((entry = zis.getNextEntry()) != null) {
            File f = new File(entry.getName());
            String name = FilenameUtils.getName(f.getParent());

            if (name != null && (name.equals("MML") || name.equals("Scripts"))) {
                valid = true;
            }

            count++;
            zis.closeEntry();
        }

        zis.close();

        if (!valid) {
            throw new ScenarioException("Invalid scenario file");
        }

        return count;
    }

    private InputStream getInputStream() throws IOException {
        InputStream is = context.getContentResolver().openInputStream(this.source);

        if (is == null) {
            throw new IOException("Can't open selected file");
        }

        return is;
    }
}
