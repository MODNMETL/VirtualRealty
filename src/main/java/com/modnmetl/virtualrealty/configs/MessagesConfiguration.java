package com.modnmetl.virtualrealty.configs;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Header;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;

import java.util.Arrays;
import java.util.List;

@Header("-------------------------------------------------------------- #")
@Header("                                                               #")
@Header("                          Messages                             #")
@Header("                                                               #")
@Header("-------------------------------------------------------------- #")
@Names(strategy = NameStrategy.IDENTITY)
public class MessagesConfiguration extends OkaeriConfig {

    //Core
    public String reloadComplete = "§aReload complete!";
    public String cmdOnlyPlayers = "§cCommand only for players!";
    public String noAccess = "§cYou don't have access to this feature!";

    //Markers
    public String available = "§cAvailable";
    public String assignedByConsole = "§eConsole";
    public String assignedByShopPurchase = "§eShop Purchase";
    public String notAssigned = "§cNot assigned";
    public String creationPlotComponent1 = "§aPlot ";
    public String creationPlotComponent2 = "§8#§7%plot_id%";
    public String creationPlotComponent3 = " §acreated! §8(§7%creation_time% ms§8)";

    //Checks
    public String insufficientPermissions = "§cInsufficient permissions! §8(§7%permission%§8)";
    public String useNaturalNumbersOnly = "§cUse only natural numbers!";
    public String incorrectGamemode = "§cIncorrect gamemode value!";
    public String sizeNotRecognised = "§cSize not recognized!";
    public String LHWHardLimit = "§cL, W and H hard-limit is 500!";
    public String specifyUsername = "§cSpecify username!";
    public String playerNotFoundWithUsername = "§cCouldn't find player with specified username!";
    public String invalidDateProvided = "§cInvalid date format provided!";
    public String specifyMaterialName = "§cSpecify material name!";
    public String specifyExpiryDate = "§cSpecify expiry date!";
    public String minPlotID = "§cMinimum plot ID is %min_id%!";
    public String maxPlotID = "§cMaximum plot ID is %max_id%!";

    //Permissions
    public String cantDoAnyDMG = "§cYou can't do any damage here!";
    public String cantInteract = "§cYou can't interact here!";
    public String cantBuildHere = "§cYou can't build here!";
    public String cantRideOnPlot = "§cYou can't ride on someones plot!";

    //Plot
    public String noPlotFound = "§cCouldn't get plot with specified ID!";
    public String notYourPlot = "§cIt's not your plot!";
    public String ownershipExpired = "§cYour ownership has expired!";
    public String teleportedToPlot = "§aYou have been teleported to the plot!";
    public String gamemodeFeatureDisabled = "§cGamemode feature is disabled!";
    public String gamemodeDisabled = "§cThis gamemode is disabled!";
    public String cantSwitchGamemode = "§cYou can't switch gamemode here!";
    public String noPlots = "§cThere are no plots!";
    public String noPlayerPlotsFound = "§cYou don't own any plot!";
    public String cantCreateOnExisting = "§cYou can't create new plot over an existing plot!";
    public String cantGetFloorMaterial = "§cCouldn't get floor material with specified name!";
    public String cantGetBorderMaterial = "§cCouldn't get border material with specified name!";
    public String cantGetMaterial = "§cCouldn't get material with specified name!";
    public String notCollidingCreating = "§aNot colliding. Creating plot..";
    public String clickToShowDetailedInfo = "§a§oClick to show detailed information about the plot! §8(§7ID: §f%plot_id%§8)";
    public String removedPlot = "§aPlot successfully removed!";
    public String assignedTo = "§aPlot has been assigned to §f%assigned_to%!";
    public String newFloorMaterialSet = "§aNew floor material has been set!";
    public String newBorderMaterialSet = "§aNew border material has been set!";
    public String ownedUntilUpdated = "§aOwned until date has been updated!";
    public String assignedToBy = "§aPlot has been assigned to §f%assigned_to% §aby §f%assigned_by%!";
    public String unassigned = "§aPlot has been unassigned!";
    public String cantAddYourself = "§cYou can't add yourself to the plot!";
    public String cantKickYourself = "§cYou can't kick yourself from the plot!";
    public String alreadyInMembers = "§cThis player is already one of the plot members!";
    public String standingOnPlot = "§cYou are standing on a plot!";
    public String notStandingOnPlot = "§cYou aren't standing on any plot!";
    public String playerKick = "§aPlayer §7%player% §ahas been kicked out of your plot!";
    public String playerAdd = "§aPlayer §7%player% §ahas been added to your plot!";
    public String gamemodeSwitched = "§aYour gamemode has changed!";
    public String gamemodeAlreadySelected = "§cThis gamemode is already selected!";
    public String noPlotMembers = "§cThis plot has no members.";

    //Entrance
    public String enteredAvailablePlot = "§2You have entered an available plot!";
    public String enteredOwnedPlot = "§7You have entered §2%owner%'s §7plot!";
    public String leftAvailablePlot = "§cYou have left an available plot!";
    public String leftOwnedPlot = "§7You have left §2%owner%'s §7plot!";
    public String enteredProtectedArea = "§6You have entered a protected area!";
    public String leftProtectedArea = "§6You have left a protected area!";

    //Draft
    public String notHoldingPlotClaim = "§cYou currently don't hold any plot claim item in your hand.";
    public String cantPlaceDraftItems = "§cYou can't place draft items.";
    public String noDraftClaimEnabled = "§cYou don't have plot draft claim enabled.";
    public String draftModeDisabled = "§aDraft mode successfully disabled!";
    public String draftModeCancelled = "§cDraft cancelled. Collision with another plot.";
    public String draftModeCancelledCollision = "§cDraft cancelled. Collision with another plot.";
    public String draftModeDisabledDueToDeath = "§cDraft mode has been disabled due to death.";
    public List<String> draftModeEnabled = Arrays.asList(
            " ",
            " §8§l«§8§m                    §8[§aDraft Mode§8]§m                    §8§l»",
            " ",
            " §8§l» §7Type §a/plot stake §7to place plot.",
            " §8§l» §7If you want to leave draft mode type §a/plot draft",
            " "
    );

    //Other
    public String visualBoundaryDisplayed = "§aThe visual boundary was displayed.";
    public String visualBoundaryActive = "§cThe visual boundary is already active for this region.";

}
