package com.modnmetl.virtualrealty.model.permission;

public enum Permission {

    WORLD_BUILD("virtualrealty.build.world"),
    PLOT_BUILD("virtualrealty.build.plot"),
    BORDER_BUILD("virtualrealty.build.border");

    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

    @Override
    public String toString() {
        return permission;
    }

}
