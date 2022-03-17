package com.modnmetl.virtualrealty.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.utils.loader.CustomClassLoader;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
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
        HttpURLConnection httpConn;
        if (debug) {
            url = new URL("http://localhost/virtualrealty/premium" + "?license=" + licenseKey + "&email=" + licenseEmail + "&version=" + pluginVersion);
            httpConn = (HttpURLConnection) url.openConnection();
        } else {
            url = new URL("https://api.modnmetl.com/auth/key");
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("POST");
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
        }

        int responseCode = httpConn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            VirtualRealty.debug("Authentication error | " + httpConn.getResponseCode() + " " + httpConn.getResponseMessage());
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
        httpConn.disconnect();
        URL jarUrl = loaderFile.toURI().toURL();
        VirtualRealty.getInstance().setClassLoader(new CustomClassLoader(
                new URL[]{ jarUrl }, classLoader)
        );
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
