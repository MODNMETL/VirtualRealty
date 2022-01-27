package com.modnmetl.virtualrealty.listeners;

import com.modnmetl.virtualrealty.VirtualRealty;
import org.bukkit.event.Listener;

public class VirtualListener implements Listener {

    private final VirtualRealty plugin;

    public VirtualListener(VirtualRealty plugin) {
        this.plugin = plugin;
    }

    public void registerEvents() {
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    protected VirtualRealty getPlugin() {
        return this.plugin;
    }

}
