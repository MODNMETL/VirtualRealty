package com.modnmetl.virtualrealty.listeners.protection;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.enums.materials.InteractMaterial;
import com.modnmetl.virtualrealty.enums.permissions.RegionPermission;
import com.modnmetl.virtualrealty.enums.materials.SwitchMaterial;
import com.modnmetl.virtualrealty.listeners.VirtualListener;
import com.modnmetl.virtualrealty.managers.PlotManager;
import com.modnmetl.virtualrealty.objects.Plot;
import com.modnmetl.virtualrealty.objects.PlotMember;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Switch;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.permissions.Permission;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;

public class PlotProtectionListener extends VirtualListener {
    
    public static final Permission PLOT_BUILD = new Permission("virtualrealty.build.plot");

    public static final LinkedList<Material> INTERACTABLE = new LinkedList<>();
    public static final LinkedList<Material> SWITCHABLE = new LinkedList<>();

    static {
        for (InteractMaterial value : InteractMaterial.values()) {
            Material material = Material.getMaterial(value.toString());
            if (Objects.nonNull(material)) {
                INTERACTABLE.add(material);
            }
        }
        for (SwitchMaterial value : SwitchMaterial.values()) {
            Material material = Material.getMaterial(value.toString());
            if (Objects.nonNull(material)) {
                SWITCHABLE.add(material);
            }
        }
    }

    public PlotProtectionListener(VirtualRealty plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockInteract(PlayerInteractEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getPlayer();
        if (e.getClickedBlock() == null) return;
        if (e.getClickedBlock().getType() == Material.CHEST) return;
        if (!(e.getAction() == Action.PHYSICAL || e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if (player.isSneaking() && e.isBlockInHand()) return;
        if (!(INTERACTABLE.contains(e.getClickedBlock().getType()) || SWITCHABLE.contains(e.getClickedBlock().getType()))) return;
        if (!VirtualRealty.legacyVersion) {
            if (e.getHand() == EquipmentSlot.OFF_HAND) return;
            if (!e.getClickedBlock().getType().isInteractable()) return;
        }
        Plot plot = PlotManager.getPlot(e.getClickedBlock().getLocation());
        if (plot == null) return;
        if (hasPermission(player, PLOT_BUILD)) return;
        if (plot.hasMembershipAccess(player.getUniqueId())) {
            PlotMember plotMember = plot.getMember(player.getUniqueId());
            if (plot.isOwnershipExpired()) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownershipExpired);
                return;
            }
            if (plotMember == null) return;
            if ((!VirtualRealty.legacyVersion && e.getClickedBlock().getBlockData() instanceof Switch) || SWITCHABLE.contains(e.getClickedBlock().getType())) {
                if (!plotMember.hasPermission(RegionPermission.SWITCH)) {
                    e.setCancelled(true);
                    player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
                }
                return;
            }
            if (INTERACTABLE.contains(e.getClickedBlock().getType())) {
                if (!plotMember.hasPermission(RegionPermission.ITEM_USE)) {
                    e.setCancelled(true);
                    player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
                }
            }
        } else {
            if ((!VirtualRealty.legacyVersion && e.getClickedBlock().getBlockData() instanceof Switch) || SWITCHABLE.contains(e.getClickedBlock().getType())) {
                if (!plot.hasPermission(RegionPermission.SWITCH)) {
                    e.setCancelled(true);
                    player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
                }
                return;
            }
            if (INTERACTABLE.contains(e.getClickedBlock().getType())) {
                if (!plot.hasPermission(RegionPermission.ITEM_USE)) {
                    e.setCancelled(true);
                    player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChestEvent(PlayerInteractEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getPlayer();
        if (e.getClickedBlock() == null) return;
        if (e.getClickedBlock().getType() != Material.CHEST) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (player.isSneaking() && e.isBlockInHand()) return;
        Plot plot = PlotManager.getPlot(e.getClickedBlock().getLocation());
        if (plot == null) return;
        if (hasPermission(player, PLOT_BUILD)) return;
        if (plot.hasMembershipAccess(player.getUniqueId())) {
            PlotMember plotMember = plot.getMember(player.getUniqueId());
            if (plot.isOwnershipExpired()) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownershipExpired);
                return;
            }
            if (plotMember == null) return;
            if (!plotMember.hasPermission(RegionPermission.CHEST_ACCESS)) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
            }
        } else {
            if (!plot.hasPermission(RegionPermission.CHEST_ACCESS)) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onLiquidPlace(PlayerBucketEmptyEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getPlayer();
        Plot plot = PlotManager.getBorderedPlot(e.getBlockClicked().getLocation());
        if (plot == null) return;
        if (hasPermission(player, PLOT_BUILD)) return;
        if (plot.hasMembershipAccess(player.getUniqueId())) {
            PlotMember plotMember = plot.getMember(player.getUniqueId());
            if (plot.isOwnershipExpired()) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownershipExpired);
                return;
            }
            if (plotMember == null) return;
            if (!plotMember.hasPermission(RegionPermission.ITEM_USE)) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
            }
        } else {
            if (!plot.hasPermission(RegionPermission.ITEM_USE)) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onVehicleDestroy(VehicleDestroyEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getAttacker() instanceof Player)) return;
        Player player = (Player) e.getAttacker();
        Plot plot = PlotManager.getBorderedPlot(e.getVehicle().getLocation());
        if (plot == null) return;
        if (hasPermission(player, PLOT_BUILD)) return;
        if (plot.hasMembershipAccess(player.getUniqueId())) {
            PlotMember plotMember = plot.getMember(player.getUniqueId());
            if (plot.isOwnershipExpired()) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownershipExpired);
                return;
            }
            if (plotMember == null) return;
            if (!plotMember.hasPermission(RegionPermission.ENTITY_DAMAGE)) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantDoAnyDMG);
            }
        } else {
            if (!plot.hasPermission(RegionPermission.ENTITY_DAMAGE)) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantDoAnyDMG);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onLiquidTake(PlayerBucketFillEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getPlayer();
        Plot plot = PlotManager.getBorderedPlot(e.getBlockClicked().getLocation());
        if (plot == null) return;
        if (hasPermission(player, PLOT_BUILD)) return;
        if (plot.hasMembershipAccess(player.getUniqueId())) {
            PlotMember plotMember = plot.getMember(player.getUniqueId());
            if (plot.isOwnershipExpired()) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownershipExpired);
                return;
            }
            if (plotMember == null) return;
            if (!plotMember.hasPermission(RegionPermission.ITEM_USE)) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
            }
        } else {
            if (!plot.hasPermission(RegionPermission.ITEM_USE)) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Plot plot = PlotManager.getBorderedPlot(e.getBlockPlaced().getLocation());
        if (plot == null) return;
        if (hasPermission(player, PLOT_BUILD)) return;
        if (plot.hasMembershipAccess(player.getUniqueId())) {
            PlotMember plotMember = plot.getMember(player.getUniqueId());
            if (plot.isOwnershipExpired()) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownershipExpired);
                return;
            }
            if (plotMember == null) return;
            if (!plotMember.hasPermission(RegionPermission.PLACE)) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantBuildHere);
            }
        } else {
            if (!plot.hasPermission(RegionPermission.PLACE)) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantBuildHere);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getPlayer();
        Plot plot = PlotManager.getBorderedPlot(e.getBlock().getLocation());
        if (plot == null) return;
        if (hasPermission(player, PLOT_BUILD)) return;
        if (plot.hasMembershipAccess(player.getUniqueId())) {
            PlotMember plotMember = plot.getMember(player.getUniqueId());
            if (plot.isOwnershipExpired()) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownershipExpired);
                return;
            }
            if (plotMember == null) return;
            if (!plotMember.hasPermission(RegionPermission.BREAK)) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantBuildHere);
            }
        } else {
            if (!plot.hasPermission(RegionPermission.BREAK)) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantBuildHere);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        if (e.isCancelled()) return;
        Block piston = e.getBlock();
        Plot fromPlot = PlotManager.getPlot(piston.getLocation());
        e.getBlocks().forEach(block -> {
            Location toLocation = block.getLocation();
            Plot toPlot = PlotManager.getPlot(toLocation);
            Plot toBorderedPlot = PlotManager.getBorderedPlot(toLocation);
            if (fromPlot != null) {
                if (toPlot == null) {
                    e.setCancelled(true);
                }
            } else {
                if (toBorderedPlot != null) {
                    e.setCancelled(true);
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockMove(BlockPistonExtendEvent e) {
        if (e.isCancelled()) return;
        for (Block block : e.getBlocks()) {
            Location blockLocation = block.getLocation();
            Plot plot = PlotManager.getPlot(blockLocation);
            Plot borderedPlot = PlotManager.getBorderedPlot(blockLocation);
            if (borderedPlot != null) {
                if (plot == null) {
                    e.setCancelled(true);
                } else if (plot.getID() != borderedPlot.getID()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onIgniteEvent(BlockIgniteEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getPlayer();
        if (player == null) return;
        if (e.getIgnitingBlock() == null) return;
        Plot plot = PlotManager.getBorderedPlot(e.getIgnitingBlock().getLocation());
        if (plot == null) return;
        if (hasPermission(player, PLOT_BUILD)) return;
        if (plot.hasMembershipAccess(player.getUniqueId())) {
            PlotMember plotMember = plot.getMember(player.getUniqueId());
            if (plot.isOwnershipExpired()) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownershipExpired);
                return;
            }
            if (plotMember == null) return;
            if (!plotMember.hasPermission(RegionPermission.ITEM_USE)) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
            }
        } else {
            if (!plot.hasPermission(RegionPermission.ITEM_USE)) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
            }
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e){
        if (e.isCancelled()) return;
        if (e.getCause() != BlockIgniteEvent.IgniteCause.SPREAD) return;
        Plot fromPlot = PlotManager.getPlot(e.getBlock().getLocation());
        Plot toPlot = PlotManager.getPlot(e.getIgnitingBlock().getLocation());
        if (toPlot != null && fromPlot == null) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onTreeGrow(StructureGrowEvent e) {
        if (e.isCancelled()) return;
        for (BlockState block : new ArrayList<>(e.getBlocks())) {
            Location saplingLocation = e.getLocation();
            Location blockLocation = block.getLocation();
            Plot fromPlot = PlotManager.getPlot(saplingLocation);
            Plot toPlot = PlotManager.getPlot(blockLocation);
            if (fromPlot != null) {
                if (toPlot != null) {
                    if (fromPlot.getID() != toPlot.getID()) {
                        e.getBlocks().remove(block);
                    }
                } else {
                    e.getBlocks().remove(block);
                }
            } else if (toPlot != null) {
                e.getBlocks().remove(block);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDragonEggMove(BlockFromToEvent e) {
        if (e.isCancelled()) return;
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

    @EventHandler(priority = EventPriority.LOW)
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (e.isCancelled()) return;
        if (e.getEntity().getShooter() == null) return;
        if (!(e.getEntity().getShooter() instanceof Player)) return;
        Player shooter = ((Player) e.getEntity().getShooter());
        Plot plot = PlotManager.getPlot(e.getEntity().getLocation());
        if (plot == null) return;
        if (hasPermission(shooter, PLOT_BUILD)) return;
        if (plot.getOwnedBy() != null && plot.getOwnedBy().equals(((Player) e.getEntity().getShooter()).getUniqueId()))
            return;
        if (plot.hasMembershipAccess(shooter.getUniqueId())) return;
        e.getEntity().remove();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onProjectileHit(ProjectileHitEvent e) {
        if (e.getEntity().getShooter() == null) return;
        if (!(e.getEntity().getShooter() instanceof Player)) return;
        Player shooter = ((Player) e.getEntity().getShooter());
        Plot plot = PlotManager.getPlot(e.getEntity().getLocation());
        if (plot == null) return;
        if (hasPermission(shooter, PLOT_BUILD)) return;
        if (plot.getOwnedBy() != null && plot.getOwnedBy().equals(((Player) e.getEntity().getShooter()).getUniqueId()))
            return;
        if (plot.hasMembershipAccess(shooter.getUniqueId())) return;
        e.getEntity().remove();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onArmorStandChange(PlayerArmorStandManipulateEvent e) {
        if (e.isCancelled()) return;
        Plot plot = PlotManager.getPlot(e.getPlayer().getLocation());
        Player player = e.getPlayer();
        if (plot == null) return;
        if (hasPermission(player, PLOT_BUILD)) return;
        if (plot.hasMembershipAccess(player.getUniqueId())) {
            PlotMember plotMember = plot.getMember(player.getUniqueId());
            if (plot.isOwnershipExpired()) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownershipExpired);
                return;
            }
            if (plotMember == null) return;
            if (!plotMember.hasPermission(RegionPermission.ARMOR_STAND)) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
            }
        } else {
            if (!plot.hasPermission(RegionPermission.ARMOR_STAND)) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onItemFrameDamage(HangingBreakByEntityEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getRemover() instanceof Player)) return;
        Player player = (Player) e.getRemover();
        Plot plot = PlotManager.getPlot(player.getLocation());
        if (plot == null) return;
        if (hasPermission(player, PLOT_BUILD)) return;
        if (plot.hasMembershipAccess(player.getUniqueId())) {
            PlotMember plotMember = plot.getMember(player.getUniqueId());
            if (plot.isOwnershipExpired()) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownershipExpired);
                return;
            }
            if (plotMember == null) return;
            if (!plotMember.hasPermission(RegionPermission.ENTITY_DAMAGE)) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantDoAnyDMG);
            }
        } else {
            if (!plot.hasPermission(RegionPermission.ENTITY_DAMAGE)) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantDoAnyDMG);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onItemFrameRotate(PlayerInteractEntityEvent e) {
        if (e.isCancelled()) return;
        Player player = e.getPlayer();
        Plot plot = PlotManager.getPlot(player.getLocation());
        if (plot == null) return;
        if (hasPermission(player, PLOT_BUILD)) return;
        if (plot.hasMembershipAccess(player.getUniqueId())) {
            PlotMember plotMember = plot.getMember(player.getUniqueId());
            if (plot.isOwnershipExpired()) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownershipExpired);
                return;
            }
            if (plotMember == null) return;
            if (!plotMember.hasPermission(RegionPermission.ITEM_USE)) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
            }
        } else {
            if (!plot.hasPermission(RegionPermission.ITEM_USE)) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantInteract);
            }
        }
    }


    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getDamager() instanceof Player)) return;
        Player player = (Player) e.getDamager();
        Plot plot = PlotManager.getPlot(e.getEntity().getLocation());
        if (plot == null) return;
        if (hasPermission(player, PLOT_BUILD)) return;
        if (plot.hasMembershipAccess(player.getUniqueId())) {
            PlotMember plotMember = plot.getMember(player.getUniqueId());
            if (plot.isOwnershipExpired()) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownershipExpired);
                return;
            }
            if (plotMember == null) return;
            if (!plotMember.hasPermission(RegionPermission.ENTITY_DAMAGE)) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantDoAnyDMG);
            }
        } else {
            if (!plot.hasPermission(RegionPermission.ENTITY_DAMAGE)) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantDoAnyDMG);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityExplode(EntityExplodeEvent e) {
        for (Block block : new ArrayList<>(e.blockList())) {
            Plot plot = PlotManager.getBorderedPlot(block.getLocation());
            if (plot != null)
                e.blockList().remove(block);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockExplode(BlockExplodeEvent e) {
        for (Block block : new ArrayList<>(e.blockList())) {
            Plot plot = PlotManager.getBorderedPlot(block.getLocation());
            if (plot != null)
                e.blockList().remove(block);
        }
    }

    @EventHandler
    public void onFireSpread(BlockSpreadEvent e) {
        Plot plot = PlotManager.getBorderedPlot(e.getNewState().getLocation());
        if (plot != null)
            e.setCancelled(true);
    }

    @EventHandler
    public void onFireBurn(BlockBurnEvent e) {
        if (!VirtualRealty.legacyVersion) {
            Plot fromPlot = PlotManager.getPlot(e.getIgnitingBlock().getLocation());
            Plot toPlot = PlotManager.getPlot(e.getBlock().getLocation());
            if (toPlot == null) return;
            if (fromPlot != null) {
                if (toPlot.getID() != fromPlot.getID()) {
                    e.setCancelled(true);
                }
            } else {
                e.setCancelled(true);
            }
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
        if (hasPermission(player, PLOT_BUILD)) return;
        if (plot.hasMembershipAccess(player.getUniqueId())) {
            PlotMember plotMember = plot.getMember(player.getUniqueId());
            if (plot.isOwnershipExpired()) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().ownershipExpired);
                return;
            }
            if (plotMember == null) return;
            if (!plotMember.hasPermission(RegionPermission.ENTITY_DAMAGE)) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantDoAnyDMG);
            }
        } else {
            if (!plot.hasPermission(RegionPermission.ENTITY_DAMAGE)) {
                e.setCancelled(true);
                player.sendMessage(VirtualRealty.PREFIX + VirtualRealty.getMessages().cantDoAnyDMG);
            }
        }
    }

    public boolean hasPermission(CommandSender sender, Permission permission) {
        return sender.hasPermission(permission);
    }

}
