package com.modnmetl.virtualrealty.registry;

import com.modnmetl.virtualrealty.managers.PlotManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.objects.Plot;
import org.bukkit.entity.Player;

public class VirtualPlaceholders extends PlaceholderExpansion {

    private final VirtualRealty plugin;

    /**
     * Since we register the expansion inside our own plugin, we
     * can simply use this method here to get an instance of our
     * plugin.
     *
     * @param plugin
     *        The instance of our plugin.
     */
    public VirtualPlaceholders(VirtualRealty plugin){
        this.plugin = plugin;
    }

    /**
     * Because this is an internal class,
     * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
     * PlaceholderAPI is reloaded
     *
     * @return true to persist through reloads
     */
    @Override
    public boolean persist(){
        return true;
    }

    /**
     * Because this is a internal class, this check is not needed
     * and we can simply return {@code true}
     *
     * @return Always true since it's an internal class.
     */
    @Override
    public boolean canRegister(){
        return true;
    }

    /**
     * The name of the person who created this expansion should go here.
     * <br>For convienience do we return the author from the plugin.yml
     *
     * @return The name of the author as a String.
     */
    @Override
    public String getAuthor(){
        return plugin.getDescription().getAuthors().toString();
    }

    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest
     * method to obtain a value if a placeholder starts with our
     * identifier.
     * <br>The identifier has to be lowercase and can't contain _ or %
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public String getIdentifier(){
        return "virtualrealty";
    }

    /**
     * This is the version of the expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     *
     * For convienience do we return the version from the plugin.yml
     *
     * @return The version as a String.
     */
    @Override
    public String getVersion(){
        return plugin.getDescription().getVersion();
    }

    /**
     * This is the method called when a placeholder with our identifier
     * is found and needs a value.
     * <br>We specify the value identifier in this method.
     * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param  player
     *         A {@link org.bukkit.entity.Player Player}.
     * @param  identifier
     *         A String containing the identifier/value.
     *
     * @return possibly-null String of the requested identifier.
     */
    @Override
    public String onPlaceholderRequest(Player player, String identifier){
        if(player == null) return "";

        Plot plot = PlotManager.getBorderedPlot(player.getLocation());
        if (identifier.equals("plot_id")) {
            if (plot == null) return "";
            return String.valueOf(plot.getID());
        }
        if (identifier.equals("plot_owner")) {
            if (plot == null) return "";
            if (plot.getOwnedBy() == null) return "";
            return String.valueOf(plot.getPlotOwner().getName());
        }
        if (identifier.equals("is_plot_owner")) {
            if (plot == null) return "";
            if (plot.getOwnedBy() == null) return "false";
            if (plot.getOwnedBy().equals(player.getUniqueId())) return "true";
            return "false";
        }
        return null;
    }

}
