package com.modnmetl.virtualrealty.listeners.protection;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.enums.permissions.RegionPermission;
import com.modnmetl.virtualrealty.listeners.VirtualListener;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.utils.WorldUtil;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Switch;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.permissions.Permission;
import org.bukkit.util.Vector;

public class WorldProtectionListener extends VirtualListener {

    public static final Permission WORLD_BUILD = new Permission("virtualrealty.build.world");

    public WorldProtectionListener(VirtualRealty plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockInteract(PlayerInteractEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getPlayer();
        if (e.getClickedBlock() == null) return;
        if (e.getClickedBlock().getType() == Material.CHEST) return;
        if (!(e.getAction() == Action.PHYSICAL || e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if (player.isSneaking() && e.isBlockInHand()) return;
        if (!(PlotProtectionListener.INTERACTABLE.contains(e.getClickedBlock().getType()) || PlotProtectionListener.SWITCHABLE.contains(e.getClickedBlock().getType())))
            return;
        if (!VirtualRealty.legacyVersion) {
            if (e.getHand() == EquipmentSlot.OFF_HAND) return;
            if (!e.getClickedBlock().getType().isInteractable()) return;
        }
        Plot plot = PlotManager.getPlot(e.getClickedBlock().getLocation());
        if (plot != null) return;
        if (hasPermission(player, WORLD_BUILD)) return;
        try {
            if ((!VirtualRealty.legacyVersion && e.getClickedBlock().getBlockData() instanceof Switch) || PlotProtectionListener.SWITCHABLE.contains(e.getClickedBlock().getType())) {
                Class.forName("com.modnmetl.virtualrealty.premiumloader.PremiumLoader", false, VirtualRealty.getLoader());
                if (!WorldUtil.hasPermission(RegionPermission.SWITCH)) {
                    e.setCancelled(true);
                    player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
                }
                return;
            }
            if (PlotProtectionListener.INTERACTABLE.contains(e.getClickedBlock().getType())) {
                Class.forName("com.modnmetl.virtualrealty.premiumloader.PremiumLoader", false, VirtualRealty.getLoader());
                if (!WorldUtil.hasPermission(RegionPermission.ITEM_USE)) {
                    e.setCancelled(true);
                    player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
                }
            }
        } catch (Exception ignored) {}
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChestClick(PlayerInteractEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getPlayer();
        if (e.getClickedBlock() == null) return;
        if (e.getClickedBlock().getType() != Material.CHEST) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (player.isSneaking() && e.isBlockInHand()) return;
        Plot plot = PlotManager.getPlot(e.getClickedBlock().getLocation());
        if (plot != null) return;
        if (hasPermission(player, WORLD_BUILD)) return;
        if (!WorldUtil.hasPermission(RegionPermission.CHEST_ACCESS)) {
            e.setCancelled(true);
            player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getPlayer();
        Plot plot = PlotManager.getPlot(e.getBlockPlaced().getLocation());
        if (plot != null) return;
        if (hasPermission(player, WORLD_BUILD)) return;
        if (!WorldUtil.hasPermission(RegionPermission.PLACE)) {
            e.setCancelled(true);
            player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantBuildHere);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getPlayer();
        Plot plot = PlotManager.getPlot(e.getBlock().getLocation());
        if (plot != null) return;
        if (hasPermission(player, WORLD_BUILD)) return;
        if (!WorldUtil.hasPermission(RegionPermission.BREAK)) {
            e.setCancelled(true);
            player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantBuildHere);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onIgniteEvent(BlockIgniteEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getPlayer();
        if (player == null) return;
        if (e.getIgnitingBlock() == null) return;
        Plot plot = PlotManager.getBorderedPlot(e.getIgnitingBlock().getLocation());
        if (plot != null) return;
        if (hasPermission(player, WORLD_BUILD)) return;
        if (!WorldUtil.hasPermission(RegionPermission.ITEM_USE)) {
            e.setCancelled(true);
            player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onArmorStandChange(PlayerArmorStandManipulateEvent e) {
        if (e.isCancelled()) return;
        Plot plot = PlotManager.getPlot(e.getPlayer().getLocation());
        Player player = e.getPlayer();
        if (plot != null) return;
        if (hasPermission(player, WORLD_BUILD)) return;
        if (!WorldUtil.hasPermission(RegionPermission.ARMOR_STAND)) {
            e.setCancelled(true);
            player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onItemFrameDamage(HangingBreakByEntityEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getRemover() instanceof Player)) return;
        Player player = (Player) e.getRemover();
        Plot plot = PlotManager.getPlot(player.getLocation());
        if (plot != null) return;
        if (hasPermission(player, WORLD_BUILD)) return;
        if (!WorldUtil.hasPermission(RegionPermission.ENTITY_DAMAGE)) {
            e.setCancelled(true);
            player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantDoAnyDMG);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onItemFrameRotate(PlayerInteractEntityEvent e) {
        if (e.isCancelled()) return;
        if (!e.getRightClicked().getType().equals(EntityType.ITEM_FRAME)) return;
        Player player = e.getPlayer();
        Plot plot = PlotManager.getPlot(player.getLocation());
        if (plot != null) return;
        if (hasPermission(player, WORLD_BUILD)) return;
        if (!WorldUtil.hasPermission(RegionPermission.ITEM_USE)) {
            e.setCancelled(true);
            player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
        }
    }


    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getDamager() instanceof Player)) return;
        Player player = (Player) e.getDamager();
        Plot plot = PlotManager.getPlot(e.getEntity().getLocation());
        if (plot == null) return;
        if (hasPermission(player, WORLD_BUILD)) return;
        if (!WorldUtil.hasPermission(RegionPermission.ENTITY_DAMAGE)) {
            e.setCancelled(true);
            player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantDoAnyDMG);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityProjectileDamage(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        if (!((e.getDamager() instanceof Projectile) && ((Projectile) e.getDamager()).getShooter() instanceof Player))
            return;
        Player player = ((Player) ((Projectile) e.getDamager()).getShooter());
        Plot plot = PlotManager.getBorderedPlot(e.getDamager().getLocation());
        if (plot == null) return;
        if (hasPermission(player, WORLD_BUILD)) return;
        if (!WorldUtil.hasPermission(RegionPermission.ENTITY_DAMAGE)) {
            e.setCancelled(true);
            player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantDoAnyDMG);
        }
    }

    public boolean hasPermission(CommandSender sender, Permission permission) {
        return sender.hasPermission(permission);
    }

}
