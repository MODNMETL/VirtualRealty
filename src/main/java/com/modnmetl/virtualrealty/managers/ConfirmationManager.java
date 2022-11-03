package com.modnmetl.virtualrealty.managers;

import com.modnmetl.virtualrealty.enums.ConfirmationType;
import com.modnmetl.virtualrealty.objects.data.Confirmation;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConfirmationManager {

    @Getter
    private static final List<Confirmation> confirmations = new ArrayList<>();

    public static void addConfirmation(Confirmation confirmation) {
        confirmations.add(confirmation);
    }

    public static boolean doesConfirmationExist(ConfirmationType confirmationType, UUID player) {
        return confirmations.stream().anyMatch(confirmation -> confirmation.getConfirmationType() == confirmationType && confirmation.getSender().getUniqueId().equals(player));
    }

    public static void removeConfirmations(int plotID, ConfirmationType confirmationType) {
        confirmations.removeIf(confirmation -> confirmation.getPlotID() == plotID && confirmation.getConfirmationType() == confirmationType);
    }

    public static void removeStakeConfirmations(ConfirmationType confirmationType, UUID sender) {
        confirmations.removeIf(confirmation -> confirmation.getConfirmationType() == confirmationType && confirmation.getSender().getUniqueId() == sender);
    }

    public static boolean isConfirmationAvailable(UUID confirmationUUID) {
        return confirmations.stream().anyMatch(s -> s.getConfirmationUUID() == confirmationUUID);
    }

}
