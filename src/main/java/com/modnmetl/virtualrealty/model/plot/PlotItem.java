package com.modnmetl.virtualrealty.model.plot;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.model.other.VItem;
import com.modnmetl.virtualrealty.util.data.ItemBuilder;
import com.modnmetl.virtualrealty.util.data.SkullUtil;
import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class PlotItem {

    public static final String NBT_PREFIX = "vrplot_";

    private final VItem itemType;
    private final PlotSize plotSize;
    private final int length;
    private final int height;
    private final int width;
    private final Map.Entry<String, Byte> floorData;
    private final Map.Entry<String, Byte> borderData;
    private final boolean natural;
    private int additionalDays;
    private UUID uuid;

    public ItemStack getItemStack() {
        ItemBuilder itemBuilder = (uuid == null)
                ? new ItemBuilder(SkullUtil.getSkull("16bb9fb97ba87cb727cd0ff477f769370bea19ccbfafb581629cd5639f2fec2b"))
                : new ItemBuilder(SkullUtil.getSkull("16bb9fb97ba87cb727cd0ff477f769370bea19ccbfafb581629cd5639f2fec2b", uuid));
        switch (itemType) {
            case CLAIM: {
                itemBuilder
                        .setName("§a" + plotSize.name().toCharArray()[0] + plotSize.name().substring(1).toLowerCase() + " Plot Claim");
                break;
            }
            case DRAFT: {
                itemBuilder
                        .setName("§a" + plotSize.name().toCharArray()[0] + plotSize.name().substring(1).toLowerCase() + " Plot Draft Claim")
                        .addEnchant(Enchantment.ARROW_INFINITE, 10);
                break;
            }
        }
        Material floor;
        Material border;
        if (VirtualRealty.legacyVersion) {
            floor = Material.valueOf(floorData.getKey());
            border = Material.valueOf(borderData.getKey());
        } else {
            floor = Bukkit.createBlockData(floorData.getKey()).getMaterial();
            border = Bukkit.createBlockData(borderData.getKey()).getMaterial();
        }
        itemBuilder
                .addLoreLine(" §8┏ §fSize: §7" + plotSize.name())
                .addLoreLine(" §8┣ §fNatural: §7" + (natural ? "Yes" : "No"))
                .addLoreLine(" §8┣ §fLength: §7" + length)
                .addLoreLine(" §8┣ §fHeight: §7" + height)
                .addLoreLine(" §8┣ §fWidth: §7" + width)
                .addLoreLine(" §8┣ §fFloor: §7" + (floor == Material.AIR ? "NONE" : floor.name()))
                .addLoreLine(" §8┣ §fBorder: §7" + (border == Material.AIR ? "NONE" : border.name()))
                .addLoreLine(" §8┗ §fLease days: §7" + (additionalDays == 0 ? "No Expiry" : additionalDays));
        ItemStack itemStack = itemBuilder.toItemStack();
        NBTItem nbtItem = new NBTItem(itemStack);

        nbtItem.setString(NBT_PREFIX + "item", itemType.name());
        nbtItem.setString(NBT_PREFIX + "size", plotSize.name());
        nbtItem.setInteger(NBT_PREFIX + "length", length);
        nbtItem.setInteger(NBT_PREFIX + "height", height);
        nbtItem.setInteger(NBT_PREFIX + "width", width);
        nbtItem.setString(NBT_PREFIX + "floor_material", floorData.getKey());
        nbtItem.setByte(NBT_PREFIX + "floor_data", floorData.getValue());
        nbtItem.setString(NBT_PREFIX + "border_material", borderData.getKey());
        nbtItem.setByte(NBT_PREFIX + "border_data", borderData.getValue());
        nbtItem.setBoolean(NBT_PREFIX + "natural", natural);
        nbtItem.setInteger(NBT_PREFIX + "additional_days", additionalDays);
        nbtItem.setString(NBT_PREFIX + "stack_uuid", uuid == null ? UUID.randomUUID().toString() : uuid.toString());
        nbtItem.applyNBT(itemStack);
        return itemStack;
    }

    public Map.Entry<String, Byte> getLegacyFloorData() {
        return floorData;
    }

    public BlockData getFloorData() {
        return Bukkit.createBlockData(floorData.getKey());
    }

    public Map.Entry<String, Byte> getLegacyBorderData() {
        return borderData;
    }

    public BlockData getBorderData() {
        return Bukkit.createBlockData(borderData.getKey());
    }

    public static PlotItem fromItemStack(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack);
        Map.Entry<String, Byte> floorData = new AbstractMap.SimpleEntry<>(nbtItem.getString("vrplot_floor_material"), nbtItem.getByte("vrplot_floor_data"));
        Map.Entry<String, Byte> borderData = new AbstractMap.SimpleEntry<>(nbtItem.getString("vrplot_border_material"), nbtItem.getByte("vrplot_border_data"));
        PlotSize plotSize = PlotSize.valueOf(nbtItem.getString("vrplot_size"));
        return new PlotItem(
                VItem.valueOf(nbtItem.getString(NBT_PREFIX + "item")),
                plotSize,
                nbtItem.getInteger(NBT_PREFIX + "length"),
                nbtItem.getInteger(NBT_PREFIX + "height"),
                nbtItem.getInteger(NBT_PREFIX + "width"),
                floorData,
                borderData,
                nbtItem.getBoolean(NBT_PREFIX + "natural"),
                nbtItem.getInteger(NBT_PREFIX + "additional_days"),
                UUID.fromString(nbtItem.getString(NBT_PREFIX + "stack_uuid"))
        );
    }

    public static UUID getPlotItemUuid(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack);
        String string = nbtItem.getString(NBT_PREFIX + "stack_uuid");
        if (string == null) return UUID.randomUUID();
        return UUID.fromString(string);
    }

    public static PlotItem fromItemStack(ItemStack itemStack, VItem itemType) {
        PlotItem plotItem = fromItemStack(itemStack);
        return new PlotItem(itemType, plotItem.getPlotSize(), plotItem.getLength(), plotItem.getHeight(), plotItem.getWidth(), plotItem.floorData, plotItem.borderData, plotItem.isNatural(), plotItem.getAdditionalDays(), plotItem.getUuid());
    }

    public int getLength() {
        return ((plotSize == PlotSize.AREA || plotSize == PlotSize.CUSTOM) ? length : plotSize.getLength());
    }

    public int getHeight() {
        return ((plotSize == PlotSize.AREA || plotSize == PlotSize.CUSTOM) ? height : plotSize.getHeight());
    }

    public int getWidth() {
        return ((plotSize == PlotSize.AREA || plotSize == PlotSize.CUSTOM) ? width : plotSize.getWidth());
    }

}
