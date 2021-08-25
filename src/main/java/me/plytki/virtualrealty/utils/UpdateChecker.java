package me.plytki.virtualrealty.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.plytki.virtualrealty.VirtualRealty;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {
    
    private static final String VERSION_URL = "https://api.spiget.org/v2/resources/95600/versions/latest";
    private static final String DESCRIPTION_URL = "https://api.spiget.org/v2/resources/95600/updates/latest";

    public static String[] getUpdate() {
        try {
            JsonObject latestVersionObj = getURLResults(VERSION_URL);
            String newVersion = latestVersionObj.get("name").getAsString();
            JsonObject updateDesc = getURLResults(DESCRIPTION_URL);
            String updateName = updateDesc.get("title").getAsString();
            return new String[] { newVersion, updateName };
        } catch (IOException ex) {
            VirtualRealty.getInstance().getLogger().severe("Failed to get plugin update information, is Spiget down?");
            return null;
        }
    }

    private static JsonObject getURLResults(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.addRequestProperty("User-Agent", "AdvancedTeleportPA");
        return new JsonParser().parse(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
    }
    
}

