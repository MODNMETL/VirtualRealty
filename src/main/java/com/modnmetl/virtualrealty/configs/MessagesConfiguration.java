package com.modnmetl.virtualrealty.configs;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Header;
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
    public String insufficientPermissionsShort = "§cInsufficient permissions!";
    public String insufficientPermissions = "§cInsufficient permissions! §8(§7%permission%§8)";
    public String useNaturalNumbersOnly = "§cUse only natural numbers!";
    public String incorrectGamemode = "§cIncorrect gamemode value!";
    public String incorrectValue = "§cIncorrect value!";
    public String sizeNotRecognised = "§cSize not recognized!";
    public String hardLimit = "§cL, W and H hard-limit is 500!";
    public String graterThenZero = "§cL, W and H values must be greater than 0!";
    public String specifyUsername = "§cSpecify username!";
    public String playerNotFoundWithUsername = "§cCouldn't find player with specified username!";
    public String invalidDateProvided = "§cInvalid date format provided!";
    public String specifyMaterialName = "§cSpecify material name!";
    public String specifyExpiryDate = "§cSpecify expiry date!";
    public String minPlotID = "§cMinimum plot ID is %min_id%!";
    public String maxPlotID = "§cMaximum plot ID is %max_id%!";
    public String noRegionFileFound = "No saved plot schema found, remove and restore not possible - deleting plot record only";
    public String disabledPlotCreation = "Plot creation is not allowed in this world!";

    //Permissions
    public String cantDoAnyDMG = "§cYou can't do any damage here!";
    public String cantInteract = "§cYou can't interact here!";
    public String cantBuildHere = "§cYou can't build here!";

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
    public String cantKickOwner = "§cYou can't kick the owner!";
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

    //Claim Mode
    public String notHoldingPlotClaim = "§cYou currently don't hold any plot claim item in your hand.";
    public String cantPlaceClaimItems = "§cYou can't place claim items.";
    public String claimModeDisabled = "§aClaim mode successfully disabled!";
    public String claimModeCancelledBedrock = "§cClaim cancelled. Colliding with bedrock.";
    public String claimModeCancelledCollision = "§cClaim cancelled. Colliding with another plot.";
    public String claimModeDisabledDueToDeath = "§cClaim mode has been disabled due to death.";
    public List<String> claimEnabled = Arrays.asList(
            " ",
            " §8§l«§8§m                    §8[§aClaim Mode§8]§m                    §8§l»",
            " ",
            " §8§l» §aLeft-Click §7to %feature%.",
            " §8§l» §7If you want to leave claim mode §cRight-Click.",
            " "
    );

    public String stakeCancelled = "§cStake cancelled.";
    public String createFeature = "place plot";
    public String claimFeature = "claim this existing plot for yourself";
    public String extendFeature = "extend the lease on this existing plot";
    public String plotClaimed = "§aYou have successfully claimed the plot!";
    public String leaseExtended = "§7Plot §8#§7%plot_id% lease extended to %date%.";
    public List<String> stakeConfirmation = Arrays.asList(
            "§7You are about to stake your claim to the plot shown, once done you cannot undo.",
            "§7Type §aYES §7to proceed."
    );
    public List<String> extendConfirmation = Arrays.asList(
            "§7You are about extend the lease duration of the plot shown, once done you cannot undo.",
            "§7Type §aYES §7to proceed."
    );
    public List<String> claimConfirmation = Arrays.asList(
            "§7You are about to claim the plot shown, once done you cannot undo.",
            "§7Type §aYES §7to proceed."
    );
    public String removalCancelled = "§cRemoval cancelled.";
    public List<String> removeConfirmation = Arrays.asList(
            "§7You are about to remove §a%plot_id%§7, once done you cannot undo.",
            "§7Type §aYES §7to proceed."
    );

    //Other
    public String confirmationExpired = "§cConfirmation not received, command cancelled.";
    public String visualBoundaryDisplayed = "§aThe visual boundary was displayed.";
    public String visualBoundaryActive = "§cThe visual boundary is already active for this region.";

}
