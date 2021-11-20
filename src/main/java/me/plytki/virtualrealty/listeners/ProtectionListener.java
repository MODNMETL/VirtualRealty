package me.plytki.virtualrealty.listeners;

import me.plytki.virtualrealty.VirtualRealty;
import me.plytki.virtualrealty.enums.Flag;
import me.plytki.virtualrealty.listeners.VirtualListener;
import me.plytki.virtualrealty.managers.PlotManager;
import me.plytki.virtualrealty.objects.Plot;
import me.plytki.virtualrealty.utils.ProtectionUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.time.LocalDateTime;

public class ProtectionListener extends VirtualListener {

    public ProtectionListener(VirtualRealty plugin) {
        super(plugin);
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (e.getClickedBlock() != null && (e.getAction() == Action.RIGHT_CLICK_BLOCK || (e.getAction() == Action.LEFT_CLICK_BLOCK && e.getClickedBlock().getType().isInteractable()))) {
            Plot plot = PlotManager.getPlot(e.getClickedBlock().getLocation());
            if (plot != null) {
                if (plot.hasPermissionToPlot(player)) {
                    if (plot.isOwnershipExpired()) {
                        e.setCancelled(true);
                        player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownershipExpired);
                    }
                } else {
                    e.setCancelled(true);
                    player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
                }
            } else {
                if (!ProtectionUtil.hasPermissionToWorld(player, Flag.World.BLOCK_PLACE)) {
                    if (!Flag.World.BLOCK_PLACE.isAllowed()) {
                        e.setCancelled(true);
                        player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Plot plot = PlotManager.getPlot(e.getBlockPlaced().getLocation());
        if (plot != null) {
            if (plot.hasPermissionToPlot(player)) {
                if (plot.getOwnedUntilDate().isBefore(LocalDateTime.now())) {
                    e.setCancelled(true);
                    player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownershipExpired);
                }
            } else {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantBuildHere);
            }
        } else {
            if (!ProtectionUtil.hasPermissionToWorld(player, Flag.World.BLOCK_PLACE)) {
                if (!Flag.World.BLOCK_PLACE.isAllowed()) {
                    e.setCancelled(true);
                    player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Plot plot = PlotManager.getPlot(e.getBlock().getLocation());
        if (plot != null) {
            if (plot.hasPermissionToPlot(player)) {
                if (plot.isOwnershipExpired()) {
                    e.setCancelled(true);
                    player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownershipExpired);
                }
            } else {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantBuildHere);
            }
        } else {
            if (!ProtectionUtil.hasPermissionToWorld(player, Flag.World.BLOCK_BREAK)) {
                if (!Flag.World.BLOCK_BREAK.isAllowed()) {
                    e.setCancelled(true);
                    player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
                }
            }
        }
    }

    @EventHandler
    public void onBlockMove(BlockPistonExtendEvent e) {
        if (e.getBlocks().isEmpty()) {
            Location fromLocation = e.getBlock().getLocation();
            Location toLocation = e.getBlock().getLocation();
            toLocation.add(e.getDirection().getDirection());
            Plot fromPlot = PlotManager.getBorderedPlot(fromLocation);
            Plot toPlot = PlotManager.getBorderedPlot(toLocation);
            if (toPlot != null) {
                if (fromPlot == null) {
                    e.setCancelled(true);
                } else if (fromPlot.getID() != toPlot.getID()) {
                    e.setCancelled(true);
                }
            }
        } else {
            for (Block block : e.getBlocks()) {
                Location fromLocation = block.getLocation();
                Location toLocation = block.getLocation();
                toLocation.add(e.getDirection().getDirection());
                Plot fromPlot = PlotManager.getBorderedPlot(fromLocation);
                Plot toPlot = PlotManager.getBorderedPlot(toLocation);
                if (toPlot != null) {
                    if (fromPlot == null) {
                        e.setCancelled(true);
                    } else if (fromPlot.getID() != toPlot.getID()) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }
    @EventHandler
    public void onIgniteEvent(BlockIgniteEvent e) {
        Player player = e.getPlayer();
        if (e.getIgnitingBlock() != null) {
            Plot plot = PlotManager.getBorderedPlot(e.getIgnitingBlock().getLocation());
            if (plot != null) {
                if (plot.isOwnershipExpired()) {
                    e.setCancelled(true);
                    return;
                }
                e.setCancelled(!plot.hasPermissionToPlot(player));
            } else {
                if (!ProtectionUtil.hasPermissionToWorld(player, Flag.World.IGNITE)) {
                    if (!Flag.World.IGNITE.isAllowed()) {
                        e.setCancelled(true);
                        player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityExplode(ExplosionPrimeEvent e) {
        Plot plot = PlotManager.getBorderedPlot(e.getEntity().getLocation());
        if (plot != null) {
            e.setCancelled(true);
        } else {
            if (!Flag.World.EXPLOSION_PRIME.isAllowed()) {
                e.setCancelled(true);
            }
        }
    }

//    @EventHandler
//    public void onPlotEnter(PlayerMoveEvent e) {
//        Player player = e.getPlayer();
//        Plot plot = PlotManager.getPlot(e.getTo());
//        if (plot != null && e.getPlayer().isInsideVehicle()) {
//            if (!plot.hasPermissionToPlot(player)) {
//                e.getPlayer().getVehicle().eject();
//                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantRideOnPlot);
//            }
//        }
//    }

    @EventHandler
    public void onTreeGrow(StructureGrowEvent e) {
        for (BlockState block : e.getBlocks()) {
            Location fromLocation = e.getLocation();
            Location toLocation = block.getLocation();
            Plot fromPlot = PlotManager.getPlot(fromLocation);
            Plot toPlot = PlotManager.getPlot(toLocation);
            if (toPlot != null) {
                if (fromPlot == null) {
                    e.setCancelled(true);
                } else if (fromPlot.getID() != toPlot.getID()) {
                    e.setCancelled(true);
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
                if (fromPlot == null) {
                    e.setCancelled(true);
                } else if (fromPlot.getID() != toPlot.getID()) {
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
                Player player = (Player) e.getEntity().getShooter();
                if (plot.hasPermissionToPlot(player)) {
                    if (plot.isOwnershipExpired()) {
                        e.getEntity().remove();
                    }
                } else {
                    e.getEntity().remove();
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        Plot plot = PlotManager.getPlot(e.getEntity().getLocation());
        if (plot != null) {
            if (!(e.getEntity().getShooter() instanceof Player && plot.getOwnedBy() != null && plot.getOwnedBy().equals(((Player)e.getEntity().getShooter()).getUniqueId()))) {
                Player player = (Player) e.getEntity().getShooter();
                if (plot.hasPermissionToPlot(player)) {
                    if (plot.isOwnershipExpired()) {
                        e.getEntity().remove();
                    }
                } else {
                    e.getEntity().remove();
                }
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
            if (fromPlot == null) {
                e.setCancelled(true);
            } else if (fromPlot.getID() != toPlot.getID()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onArmorStandChange(PlayerArmorStandManipulateEvent e) {
        Plot plot = PlotManager.getPlot(e.getPlayer().getLocation());
        Player player = e.getPlayer();
        if (plot != null) {
            if (plot.hasPermissionToPlot(player)) {
                if (plot.isOwnershipExpired()) {
                    e.setCancelled(true);
                    player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownershipExpired);
                }
            } else {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
            }
        } else {
            if (!ProtectionUtil.hasPermissionToWorld(player, Flag.World.ARMOR_STAND_MANIPULATION)) {
                if (!Flag.World.ARMOR_STAND_MANIPULATION.isAllowed()) {
                    e.setCancelled(true);
                    player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
                }
            }
        }
    }

    @EventHandler
    public void onItemFrameDamage(HangingBreakByEntityEvent e) {
        if (e.getRemover() instanceof Player) {
            Player player = (Player) e.getRemover();
            Plot plot = PlotManager.getPlot(player.getLocation());
            if (plot != null) {
                if (plot.hasPermissionToPlot(player)) {
                    if (plot.isOwnershipExpired()) {
                        e.setCancelled(true);
                        player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownershipExpired);
                    }
                } else {
                    e.setCancelled(true);
                    player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
                }
            } else {
                if (!ProtectionUtil.hasPermissionToWorld(player, Flag.World.ITEM_FRAME_DESTROY)) {
                    if (!Flag.World.ITEM_FRAME_ROTATE.isAllowed()) {
                        e.setCancelled(true);
                        player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onItemFrameRotate(PlayerInteractEntityEvent e) {
        if (e.getRightClicked().getType().equals(EntityType.ITEM_FRAME)) {
            Player player = e.getPlayer();
            Plot plot = PlotManager.getPlot(player.getLocation());
            if (plot != null) {
                if (plot.hasPermissionToPlot(player)) {
                    if (plot.isOwnershipExpired()) {
                        e.setCancelled(true);
                        player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownershipExpired);
                    }
                } else {
                    e.setCancelled(true);
                    player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
                }
            } else {
                if (!ProtectionUtil.hasPermissionToWorld(player, Flag.World.ITEM_FRAME_ROTATE)) {
                    if (!Flag.World.ITEM_FRAME_ROTATE.isAllowed()) {
                        e.setCancelled(true);
                        player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
                    }
                }
            }
        }
    }


    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player player = (Player) e.getDamager();
            Plot plot = PlotManager.getPlot(e.getEntity().getLocation());
            if (plot != null) {
                if (plot.hasPermissionToPlot(player)) {
                    if (plot.isOwnershipExpired()) {
                        e.setCancelled(true);
                        player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownershipExpired);
                    }
                } else {
                    e.setCancelled(true);
                    player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantDoAnyDMG);
                }
            } else {
                if (!ProtectionUtil.hasPermissionToWorld(player, Flag.World.ENTITY_DAMAGE)) {
                    if (!Flag.World.ENTITY_DAMAGE.isAllowed()) {
                        e.setCancelled(true);
                        player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityProjectileDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Projectile) {
            if (((Projectile) e.getDamager()).getShooter() instanceof Player) {
                Player player = ((Player) ((Projectile) e.getDamager()).getShooter());
                Plot plot = PlotManager.getBorderedPlot(e.getDamager().getLocation());
                if (plot != null) {
                    if (!plot.hasPermissionToPlot(player)) {
                        e.getDamager().remove();
                        e.setCancelled(true);
                    }
                } else {
                    if (!ProtectionUtil.hasPermissionToWorld(player, Flag.World.ENTITY_DAMAGE)) {
                        if (!Flag.World.ENTITY_DAMAGE.isAllowed()) {
                            e.setCancelled(true);
                            player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
                        }
                    }
                }
            }
        }
    }

}
