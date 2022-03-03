package com.modnmetl.virtualrealty.enums.permissions;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.utils.data.ItemBuilder;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

@Getter
public enum ManagementPermission {

    ADD_MEMBER(0, "Add Members", new ItemBuilder(Material.DIAMOND_AXE).addItemFlag(ItemFlag.HIDE_ATTRIBUTES)),
    KICK_MEMBER(1, "Kick Members", new ItemBuilder(Material.STONE_AXE).addItemFlag(ItemFlag.HIDE_ATTRIBUTES)),
    PLOT_PERMISSIONS(2, "Plot Management", new ItemBuilder(VirtualRealty.legacyVersion ? Material.getMaterial("BOOK_AND_QUILL") : Material.WRITABLE_BOOK));

    private final int slot;
    private final String name;
    private final ItemBuilder item;

    ManagementPermission(int slot, String name, ItemBuilder item) {
        this.slot = slot;
        this.name = name;
        this.item = item;
    }

    public static ManagementPermission getPermission(int i) {
        for (ManagementPermission value : values()) {
            if (value.getSlot() == i) return value;
        }
        return null;
    }

    public String getConfigName() {
        return name().replaceAll("_", " ");
    }

}
