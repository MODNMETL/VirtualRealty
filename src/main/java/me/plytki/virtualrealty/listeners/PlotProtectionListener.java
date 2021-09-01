package me.plytki.virtualrealty.listeners;

import me.plytki.virtualrealty.VirtualRealty;
import me.plytki.virtualrealty.managers.PlotManager;
import me.plytki.virtualrealty.objects.Plot;
import me.plytki.virtualrealty.utils.PlotUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.util.Vector;

import java.time.LocalDateTime;

public class PlotProtectionListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (e.getClickedBlock() != null && (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.LEFT_CLICK_BLOCK))) {
            Plot plot = PlotManager.getPlot(e.getClickedBlock().getLocation());
            if (plot != null) {
                if (plot.getOwnedBy() != null) {
                    if (!plot.getOwnedBy().equals(player.getUniqueId())) {
                        e.setCancelled(true);
                        player.sendMessage(VirtualRealty.PREFIX + "§cYou can't interact here!");
                    } else {
                        if (plot.getOwnedUntilDate().isBefore(LocalDateTime.now())) {
                            e.setCancelled(true);
                            player.sendMessage(VirtualRealty.PREFIX + "§cYour ownership has expired!");
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlaceOutside(BlockPlaceEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Player player = e.getPlayer();
        Plot plot = PlotManager.getBorderedPlot(player.getLocation());
        if (plot != null) {
            if (plot.getOwnedBy() != null) {
                if (plot.getOwnedBy().equals(player.getUniqueId())) {
                    Plot locationPlot = PlotManager.getBorderedPlot(player.getLocation());
                    if (locationPlot != null && !PlotManager.isLocationInPlot(e.getBlockPlaced().getLocation(), plot)) {
                        e.setCancelled(true);
                        player.sendMessage(VirtualRealty.PREFIX + "§cYou can't build there!");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreakOutside(BlockBreakEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Player player = e.getPlayer();
        Plot plot = PlotManager.getBorderedPlot(player.getLocation());
        if (plot != null) {
            if (plot.getOwnedBy() != null) {
                if (plot.getOwnedBy().equals(player.getUniqueId())) {
                    Plot locationPlot = PlotManager.getBorderedPlot(player.getLocation());
                    if (locationPlot != null && !PlotManager.isLocationInPlot(e.getBlock().getLocation(), plot)) {
                        e.setCancelled(true);
                        player.sendMessage(VirtualRealty.PREFIX + "§cYou can't build there!");
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Plot plot = PlotManager.getPlot(e.getBlockPlaced().getLocation());
        if (plot != null) {
            if (plot.getOwnedBy() != null) {
                if (!plot.getOwnedBy().equals(player.getUniqueId())) {
                    e.setCancelled(true);
                    player.sendMessage(VirtualRealty.PREFIX + "§cYou can't build here!");
                } else {
                    if (plot.getOwnedUntilDate().isBefore(LocalDateTime.now())) {
                        e.setCancelled(true);
                        player.sendMessage(VirtualRealty.PREFIX + "§cYour ownership has expired!");
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Plot plot = PlotManager.getPlot(e.getBlock().getLocation());
        if (plot != null) {
            if (plot.getOwnedBy() != null) {
                if (!plot.getOwnedBy().equals(player.getUniqueId())) {
                    e.setCancelled(true);
                    player.sendMessage(VirtualRealty.PREFIX + "§cYou can't build here!");
                } else {
                    if (plot.getOwnedUntilDate().isBefore(LocalDateTime.now())) {
                        e.setCancelled(true);
                        player.sendMessage(VirtualRealty.PREFIX + "§cYour ownership has expired!");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockMove(BlockPistonExtendEvent e) {
        for (Block block : e.getBlocks()) {
            Location fromLocation = block.getLocation();
            Location toLocation = block.getLocation();
            toLocation.add(e.getDirection().getDirection());
            Plot fromPlot = PlotManager.getPlot(fromLocation);
            Plot toPlot = PlotManager.getPlot(toLocation);
            if (toPlot != null) {
                if (fromPlot != null && fromPlot.getID() != toPlot.getID()) {
                    e.setCancelled(true);
                }
                if (fromPlot == null) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        if (PlotManager.getPlot(e.getLocation()) != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlotEnter(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Plot plot = PlotManager.getPlot(e.getTo());
        if (plot != null && e.getPlayer().isInsideVehicle() && plot.getOwnedBy() != null && !plot.getOwnedBy().equals(e.getPlayer().getUniqueId())) {
            e.getPlayer().getVehicle().eject();
            player.sendMessage(VirtualRealty.PREFIX + "§cYou can't ride on someones plot!");
        }
    }

    @EventHandler
    public void onTreeGrow(StructureGrowEvent e) {
        for (BlockState block : e.getBlocks()) {
            Location fromLocation = e.getLocation();
            Location toLocation = block.getLocation();
            Plot fromPlot = PlotManager.getPlot(fromLocation);
            Plot toPlot = PlotManager.getPlot(toLocation);
            if (toPlot != null) {
                if (fromPlot != null && fromPlot.getID() != toPlot.getID()) {
                    block.setType(Material.AIR);
                }
                if (fromPlot == null) {
                    block.setType(Material.AIR);
                }
            }
        }
    }

    @EventHandler
    public void onDragonEggMove(BlockFromToEvent e) {
        Material block = e.getBlock().getType();
        if (block == Material.DRAGON_EGG || block == Material.LAVA || block == Material.WATER) {
            Location fromLocation = e.getBlock().getLocation();
            Location toLocation = e.getToBlock().getLocation();
            Plot fromPlot = PlotManager.getPlot(fromLocation);
            Plot toPlot = PlotManager.getPlot(toLocation);
            if (toPlot != null) {
                if (fromPlot != null && fromPlot.getID() != toPlot.getID()) {
                    e.setCancelled(true);
                }
                if (fromPlot == null) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        Plot plot = PlotManager.getPlot(e.getEntity().getLocation());
        if (plot != null) {
            if (!(e.getEntity().getShooter() instanceof Player && plot.getOwnedBy() != null && plot.getOwnedBy().equals(((Player)e.getEntity().getShooter()).getUniqueId()))) {
                e.getEntity().remove();
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        Plot plot = PlotManager.getPlot(e.getEntity().getLocation());
        if (plot != null) {
            if (!(e.getEntity().getShooter() instanceof Player && plot.getOwnedBy() != null && plot.getOwnedBy().equals(((Player)e.getEntity().getShooter()).getUniqueId()))) {
                e.getEntity().remove();
            }
        }
    }

    @EventHandler
    public void onFireSpread(BlockSpreadEvent e) {
        Location fromLocation = e.getSource().getLocation();
        Location toLocation = e.getBlock().getLocation();
        Plot fromPlot = PlotManager.getPlot(fromLocation);
        Plot toPlot = PlotManager.getPlot(toLocation);
        if (toPlot != null) {
            if (fromPlot != null && fromPlot.getID() != toPlot.getID()) {
                e.setCancelled(true);
            }
            if (fromPlot == null) {
                e.setCancelled(true);
            }
        }
    }

}
