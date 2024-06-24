package com.modnmetl.virtualrealty.util.data;

import com.modnmetl.virtualrealty.VirtualRealty;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public class SkullUtil {

    public static ItemStack getSkull(String texture, UUID uuid) {
        final String textureValue = getBase64Value(texture);
        ItemStack item;
        if (VirtualRealty.legacyVersion) {
            item = new ItemStack(Material.valueOf("SKULL_ITEM"));
            item.setDurability((short) 3);
        } else {
            item = new ItemStack(Material.PLAYER_HEAD);
        }
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
            NBT.modifyComponents(item, nbt -> {
                ReadWriteNBT profileNbt = nbt.getOrCreateCompound("minecraft:profile");
                profileNbt.setUUID("id", uuid);
                ReadWriteNBT propertiesNbt = profileNbt.getCompoundList("properties").addCompound();
                propertiesNbt.setString("name", "textures");
                propertiesNbt.setString("value", textureValue);
            });
            System.out.println(">=1.20.5");
        } else {
            NBT.modify(item, nbt -> {
                final ReadWriteNBT skullOwnerCompound = nbt.getOrCreateCompound("SkullOwner");
                skullOwnerCompound.setUUID("Id", uuid);
                skullOwnerCompound.getOrCreateCompound("Properties")
                        .getCompoundList("textures")
                        .addCompound()
                        .setString("Value", textureValue);
            });
            System.out.println("<1.20.5");
        }
//        System.out.println(item);
        System.out.println(NBT.itemStackToNBT(item));
        return item;
    }

    public static ItemStack getSkull(String url) {
        return getSkull(url, UUID.randomUUID());
    }

    private static String getBase64Value(String texture) {
        String original = "{\"textures\":{\"SKIN\":{\"url\":\"https://textures.minecraft.net/texture/" + texture + "\"}}}";
        return Base64.getEncoder().encodeToString(original.getBytes(StandardCharsets.UTF_8));
    }

}