package com.modnmetl.virtualrealty.commands.vrplot.subcommand;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.commands.SubCommand;
import com.modnmetl.virtualrealty.exceptions.FailedCommandException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedList;

public class DebugSubCommand extends SubCommand {

    private static long LAST_REQUEST = 0;

    public DebugSubCommand(CommandSender sender, Command command, String label, String[] args) throws FailedCommandException {
        super(sender, command, label, args, new LinkedList<>());
    }

    @Override
    public void exec(CommandSender sender, Command command, String label, String[] args) throws Exception {
        assertPermission();
        VirtualRealty instance = VirtualRealty.getInstance();
        if (System.currentTimeMillis() - LAST_REQUEST < 1500) {
            sender.sendMessage(VirtualRealty.PREFIX + "§cPlease wait " + Math.abs(System.currentTimeMillis() - LAST_REQUEST - 1500) + "ms. §7(Rate limiting)");
            return;
        }
        sender.sendMessage(" ");
        sender.sendMessage(" §8§l«§8§m                    §8[§aDebug§8]§m                    §8§l»");
        sender.sendMessage(" §8┏ §7Operating System: §a" + System.getProperty("os.name") + " §7(" + System.getProperty("os.version") + "-" + System.getProperty("os.arch") + ")");
        sender.sendMessage(" §8┣ §7Java Version: §a" + System.getProperty("java.version"));
        sender.sendMessage(" §8┣ §7Engine: §a" + instance.getServer().getName() + " §7(" + instance.getServer().getBukkitVersion() + ")");
        sender.sendMessage(" §8┣ §7Core count: §a" + Runtime.getRuntime().availableProcessors());
        sender.sendMessage(" §8┣ §7RAM: §a" + format(Runtime.getRuntime().totalMemory()) + "§7/§a" + format(Runtime.getRuntime().maxMemory()) + " §7MB");
        try {
            // API Rate Limit is 45 requests per minute
            URL url = new URL("http://ip-api.com/json/?fields=org,isp,country,countryCode");
            URLConnection urlConnection = url.openConnection();
            JsonObject jsonObject = new JsonParser().parse(new InputStreamReader(urlConnection.getInputStream())).getAsJsonObject();
            LAST_REQUEST = System.currentTimeMillis();
            sender.sendMessage(" §8┣ §7Location: §a" + jsonObject.get("country").getAsString() + " §7(" + jsonObject.get("countryCode").getAsString() + ")");
            sender.sendMessage(" §8┣ §7ISP: §a" + jsonObject.get("isp").getAsString());
            sender.sendMessage(" §8┣ §7Host: §a" + jsonObject.get("org").getAsString());
        } catch (IOException ignored) {
            sender.sendMessage(" §8┣ §7Couldn't connect to the IP-API");
        }
        sender.sendMessage(" §8┣ §7Default GM: §a" + instance.getServer().getDefaultGameMode().name().charAt(0) + instance.getServer().getDefaultGameMode().name().substring(1).toLowerCase());
        Plugin[] plugins = instance.getServer().getPluginManager().getPlugins();
        sender.sendMessage(" §8┣ §7Version: §a" + VirtualRealty.currentServerVersion.name().substring(0, 1).toUpperCase() + VirtualRealty.currentServerVersion.name().substring(1).toLowerCase());
        sender.sendMessage(" §8┣ §7Plugins (§a" + plugins.length + "§7):");
        int limit = sender instanceof Player ? 2 : 3;
        int lastRow = (plugins.length / limit + (plugins.length % limit == 0 ? 0 : 1));
        int pluginIndex = 0;
        for (int row = 0; row < lastRow; row++) {
            StringBuilder sb = new StringBuilder(" §8" + (row == lastRow - 1 ? "┗" : "┣") + "   §a");
            for (int index = 0; index < Arrays.stream(plugins).skip((long) row * limit).limit(limit).count(); index++) {
                Plugin plugin = (Plugin) Arrays.stream(plugins).skip((long) row * limit).limit(index + 1).toArray()[index];
                if (plugin != null) {
                    sb.append("§a").append(plugin.getName()).append(" §7(").append(plugin.getDescription().getVersion()).append(")");
                    try {
                        Object o = Arrays.stream(plugins).skip((long) row * limit).limit(index + 2).toArray()[index + 1];
                        if (pluginIndex % limit != (limit == 2 ? 1 : 2))
                            sb.append(" §8| ");
                    } catch (Exception ignored) {
                    }
                } else break;
                pluginIndex++;
            }
            sender.sendMessage(sb.toString());
        }
    }

    private String format(double memory) {
        DecimalFormat df = new DecimalFormat("##.#");
        memory = memory / (1024 * 1024);
        return df.format(memory);
    }

}
