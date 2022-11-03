package com.modnmetl.virtualrealty.configs.migrations;

import eu.okaeri.configs.migrate.builtin.NamedMigration;

import static eu.okaeri.configs.migrate.ConfigMigrationDsl.delete;

public class C0001_Remove_Old_Plot_Sub_Commands extends NamedMigration {

    public C0001_Remove_Old_Plot_Sub_Commands() {
        super(
                "Removing unused subcommands for plot command",
                delete("plot-aliases.stake"),
                delete("plot-aliases.draft")
                );
    }
}