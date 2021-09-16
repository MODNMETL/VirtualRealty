//package me.plytki.virtualrealty.managers;
//
//import me.plytki.virtualrealty.objects.Plot;
//import org.bukkit.Bukkit;
//import org.bukkit.Location;
//import org.bukkit.event.Listener;
//import org.bukkit.plugin.Plugin;
//import org.dynmap.DynmapAPI;
//import org.dynmap.markers.Marker;
//import org.dynmap.markers.MarkerAPI;
//import org.dynmap.markers.MarkerIcon;
//import org.dynmap.markers.MarkerSet;
//
//import java.util.logging.Level;
//
//public class DynmapManager implements Listener {
//
//    private DynmapAPI dynmapAPI;
//    private MarkerAPI markerAPI;
//    private MarkerSet ms;
//    private MarkerIcon mi;
//    private static String MarkerSetName = "SignShopMarkers";
//    private static String MarkerSetLabel = "SignShop Marker Set";
//    private static String Filename = "signshopsign.png";
//    private static String MarkerName = "signshop_icon_555";
//    private static String MarkerLabel = "SignShop";
//
//    public DynmapManager() {
//        this.dynmapAPI = null;
//        this.markerAPI = null;
//        this.ms = null;
//        this.mi = null;
//        this.init();
//    }
//
//    private boolean safelyCheckInit() {
//        try {
//            return this.dynmapAPI != null && this.dynmapAPI.markerAPIInitialized();
//        }
//        catch (NullPointerException ex) {
//            return false;
//        }
//    }
//
//    private void init() {
//        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("dynmap");
//        if (plugin == null) {
//            return;
//        }
//        this.dynmapAPI = (DynmapAPI)plugin;
//        if (!SignShopConfig.getEnableDynmapSupport()) {
//            if (this.safelyCheckInit()) {
//                MarkerSet temp = this.dynmapAPI.getMarkerAPI().getMarkerSet("SignShopMarkers");
//                if (temp != null) {
//                    temp.deleteMarkerSet();
//                }
//            }
//            return;
//        }
//        if (!this.safelyCheckInit()) {
//            SignShop.log("MarkerAPI for Dynmap has not been initialised, please check dynmap's configuration.", Level.WARNING);
//            return;
//        }
//        this.markerAPI = this.dynmapAPI.getMarkerAPI();
//        this.ms = this.markerAPI.getMarkerSet("SignShopMarkers");
//        if (this.ms == null) {
//            this.ms = this.markerAPI.createMarkerSet("SignShopMarkers", "SignShop Marker Set", null, false);
//        }
//        if (this.ms == null) {
//            SignShop.log("Could not create MarkerSet for Dynmap.", Level.WARNING);
//            return;
//        }
//        try {
//            if (this.markerAPI.getMarkerIcon("signshop_icon_555") == null) {
//                InputStream in = this.getClass().getResourceAsStream("/signshopsign.png");
//                if (in.available() > 0) {
//                    this.mi = this.markerAPI.createMarkerIcon("signshop_icon_555", "SignShop", in);
//                }
//            }
//            else {
//                this.mi = this.markerAPI.getMarkerIcon("signshop_icon_555");
//            }
//        }
//        catch (IOException ex) {}
//        if (this.mi == null) {
//            this.mi = this.markerAPI.getMarkerIcon("sign");
//        }
//        for (Plot plot : PlotManager.plots) {
//            this.manageMarkerForSeller(plot, false);
//        }
//    }
//
//    private void manageMarkerForSeller(Plot plot, boolean remove) {
//        this.manageMarkerForSeller(seller.getSignLocation(), seller.getOwner().getName(), seller.getWorld(), remove);
//    }
//
//    private void manageMarkerForSeller(Location loc, String owner, String world, boolean remove) {
//        if (this.ms == null) {
//            return;
//        }
//        String id = "SignShop_" + signshopUtil.convertLocationToString(loc).replace(".", "");
//        String label = owner + "'s SignShop";
//        Marker m = this.ms.findMarker(id);
//        if (remove) {
//            if (m != null) {
//                m.deleteMarker();
//            }
//            return;
//        }
//        if (m == null) {
//            this.ms.createMarker(id, label, world, loc.getX(), loc.getY(), loc.getZ(), this.mi, false);
//        }
//        else {
//            m.setLocation(world, loc.getX(), loc.getY(), loc.getZ());
//            m.setLabel(label);
//            m.setMarkerIcon(this.mi);
//        }
//    }
//
//}
