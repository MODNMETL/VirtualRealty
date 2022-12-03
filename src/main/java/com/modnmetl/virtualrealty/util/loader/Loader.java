package com.modnmetl.virtualrealty.util.loader;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.modnmetl.virtualrealty.VirtualRealty;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Random;
import java.util.jar.JarFile;

public class Loader {

    public Loader(String licenseKey, String licenseEmail, String pluginVersion, ClassLoader classLoader, boolean debug) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        runLoader(licenseKey, licenseEmail, pluginVersion, classLoader, debug);
    }

    private void runLoader(String licenseKey, String licenseEmail, String pluginVersion, ClassLoader classLoader, boolean debug) throws IOException {
        VirtualRealty.debug("Injecting premium..");
        URL url;
        URLConnection httpConn;
        if (debug) {
            String premiumPath = VirtualRealty.getInstance().getDataFolder().getAbsolutePath() + File.separator + "data" + File.separator + "virtualrealty-premium-" + VirtualRealty.getInstance().getDescription().getVersion() + ".jar";
            File originFile = new File(premiumPath);
            InputStream targetStream = new FileInputStream(originFile);
            File loaderFile = File.createTempFile(String.valueOf(Arrays.asList(new Random().nextInt(9), new Random().nextInt(9), new Random().nextInt(9))), ".tmp");
            VirtualRealty.setLoaderFile(loaderFile);
            FileUtils.deleteQuietly(loaderFile);
            Files.copy(targetStream, Paths.get(loaderFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
            targetStream.close();
            VirtualRealty.getInstance().jarFiles.add(new JarFile(loaderFile));
            URL jarUrl = loaderFile.toURI().toURL();
            VirtualRealty.getInstance().setClassLoader(new CustomClassLoader(
                    new URL[]{jarUrl}, classLoader)
            );
        } else {
            url = new URL("https://api.modnmetl.com/auth/key");
            httpConn = url.openConnection();
            ((HttpURLConnection)httpConn).setRequestMethod("POST");
            httpConn.setDoOutput(true);
            httpConn.setRequestProperty("Content-Type", "application/json");

            Gson gson = new Gson();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", "62110bdbf58570001812deb8");
            jsonObject.addProperty("key", licenseKey);
            jsonObject.addProperty("email", licenseEmail);
            jsonObject.addProperty("version", pluginVersion);
            String data = gson.toJson(jsonObject);

            byte[] out = data.getBytes(StandardCharsets.UTF_8);
            OutputStream stream = httpConn.getOutputStream();
            stream.write(out);
            int responseCode = ((HttpURLConnection) httpConn).getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                VirtualRealty.debug("Authentication error | " + ((HttpURLConnection) httpConn).getResponseCode() + " " + ((HttpURLConnection) httpConn).getResponseMessage());
                return;
            }

            File loaderFile;
            try (InputStream in = httpConn.getInputStream()) {
                loaderFile = File.createTempFile(String.valueOf(Arrays.asList(new Random().nextInt(9), new Random().nextInt(9), new Random().nextInt(9))), ".tmp");
                VirtualRealty.setLoaderFile(loaderFile);
                FileUtils.deleteQuietly(loaderFile);
                Files.copy(in, Paths.get(loaderFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
                VirtualRealty.getInstance().jarFiles.add(new JarFile(loaderFile));
            }
            ((HttpURLConnection) httpConn).disconnect();
            URL jarUrl = loaderFile.toURI().toURL();
            VirtualRealty.getInstance().setClassLoader(new CustomClassLoader(
                    new URL[]{jarUrl}, classLoader)
            );
        }
        try {
            Class<?> clazz = Class.forName("com.modnmetl.virtualrealty.premiumloader.PremiumLoader", true, VirtualRealty.getLoader());
            VirtualRealty.setPremium(clazz.newInstance());
        } catch (Exception ignored) {
            VirtualRealty.debug("Premium injection failed");
            return;
        }
        VirtualRealty.debug("Premium injected");
    }

}
