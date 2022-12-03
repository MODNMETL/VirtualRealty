package com.modnmetl.virtualrealty.model.other;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.manager.ConfirmationManager;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

@Getter
public abstract class Confirmation implements Executable {

    private final UUID confirmationUUID;
    private final ConfirmationType confirmationType;
    private final Player sender;
    private Integer plotID;
    private final String proceedText;

    public Confirmation(ConfirmationType confirmationType, Player sender, String proceedText) {
        this.confirmationUUID = UUID.randomUUID();
        this.confirmationType = confirmationType;
        this.sender = sender;
        this.proceedText = proceedText;
        this.runExpiry();
    }

    public Confirmation(ConfirmationType confirmationType, Player sender, Integer plotID, String proceedText) {
        this.confirmationUUID = UUID.randomUUID();
        this.confirmationType = confirmationType;
        this.sender = sender;
        this.plotID = plotID;
        this.proceedText = proceedText;
        this.runExpiry();
    }

    private void runExpiry() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (ConfirmationManager.isConfirmationAvailable(confirmationUUID)) {
                    ChatMessage.of(VirtualRealty.getMessages().confirmationExpired).sendWithPrefix(sender);
                    expiry();
                }
            }
        }.runTaskLater(VirtualRealty.getInstance(), 20 * 60);
    }

}
