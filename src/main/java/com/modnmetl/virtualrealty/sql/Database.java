package com.modnmetl.virtualrealty.sql;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.configs.PluginConfiguration;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class Database {

    private static Database instance;

    @Getter
    private final PluginConfiguration.DataModel dataModel;

    @Getter
    private final DataSource dataSource;
    @Getter
    private final Connection connection;
    @Getter
    private final Statement statement;

    public Database(File file) throws SQLException {
        dataModel = VirtualRealty.getPluginConfiguration().dataModel;
        SQLiteDataSource sqLiteDataSource = new SQLiteDataSource();
        sqLiteDataSource.setUrl("jdbc:sqlite:" + file.getAbsolutePath());
        dataSource = sqLiteDataSource;
        connection = dataSource.getConnection();
        statement = connection.createStatement();
        createTables();
        updateTables();
    }

    public Database(String hostname, int port, String username, String password, String database) throws SQLException {
        dataModel = VirtualRealty.getPluginConfiguration().dataModel;
        dataSource = new HikariDataSource();
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        hikariDataSource.setJdbcUrl("jdbc:mysql://" + hostname + ":" + port + "/" + database);
        hikariDataSource.setUsername(username);
        if (!password.isEmpty()) {
            hikariDataSource.setPassword(password);
        }
        dataSource.setLogWriter(new PrintWriter(System.out));
        connection = dataSource.getConnection();
        statement = connection.createStatement();
        createTables();
        updateTables();
    }

    private void createTables() {
        try {
            statement.execute("CREATE TABLE IF NOT EXISTS `" + VirtualRealty.getPluginConfiguration().mysql.plotsTableName + "` (`ID` INT(12) NOT NULL, `ownedBy` VARCHAR(36) NOT NULL, `nonMemberPermissions` TEXT NOT NULL, `assignedBy` VARCHAR(36) NOT NULL, `ownedUntilDate` DATETIME NOT NULL, `floorMaterial` VARCHAR(32) NOT NULL, `borderMaterial` VARCHAR(32) NOT NULL, `plotSize` VARCHAR(32) NOT NULL, `length` INT(24) NOT NULL, `width` INT(24) NOT NULL, `height` INT(24) NOT NULL, `createdLocation` TEXT(500) NOT NULL, `created` DATETIME, `modified` DATETIME, `selectedGameMode` TEXT, PRIMARY KEY(`ID`))");
            statement.execute("CREATE TABLE IF NOT EXISTS `" + VirtualRealty.getPluginConfiguration().mysql.plotMembersTableName + "` (`uuid` VARCHAR(36) NOT NULL, `plot` INT(11) NOT NULL, `selectedGameMode` TEXT NOT NULL, `permissions` TEXT NOT NULL, `managementPermissions` TEXT NOT NULL)");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void updateTables() {
        if (dataModel == PluginConfiguration.DataModel.MYSQL) {
            try {
                statement.execute("ALTER TABLE IF EXISTS `" + VirtualRealty.getPluginConfiguration().mysql.plotsTableName + "` ADD `borderMaterial` VARCHAR(32) AFTER `floorMaterial`;");
            } catch (SQLException ignored) {}
            try {
                statement.execute("ALTER TABLE `" + VirtualRealty.getPluginConfiguration().mysql.plotsTableName + "` DROP `members`;");
            } catch (SQLException ignored) {}
            try {
                statement.execute("ALTER TABLE `" + VirtualRealty.getPluginConfiguration().mysql.plotsTableName + "` ADD `created` DATETIME AFTER `createdLocation`;");
            } catch (SQLException ignored) {}
            try {
                statement.execute("ALTER TABLE `" + VirtualRealty.getPluginConfiguration().mysql.plotsTableName + "` ADD `modified` DATETIME AFTER `created`;");
            } catch (SQLException ignored) {}
            try {
                statement.execute("ALTER TABLE `" + VirtualRealty.getPluginConfiguration().mysql.plotsTableName + "` ADD `selectedGameMode` TEXT;");
            } catch (SQLException ignored) {}
            try {
                statement.execute("ALTER TABLE `" + VirtualRealty.getPluginConfiguration().mysql.plotsTableName + "` ADD `nonMemberPermissions` TEXT NOT NULL AFTER `ownedBy`;");
            } catch (SQLException ignored) {}
        }
        if (dataModel == PluginConfiguration.DataModel.SQLITE) {
            try {
                statement.execute("SELECT `nonMemberPermissions` FROM `" + VirtualRealty.getPluginConfiguration().mysql.plotsTableName + "`");
            } catch (SQLException ex) {
                try {
                    statement.execute("ALTER TABLE `" + VirtualRealty.getPluginConfiguration().mysql.plotsTableName + "` RENAME TO `_" + VirtualRealty.getPluginConfiguration().mysql.plotsTableName + "_old`;");
                    createTables();
                    statement.execute("INSERT INTO `" + VirtualRealty.getPluginConfiguration().mysql.plotsTableName + "` (`ID`, `ownedBy`, `nonMemberPermissions`, `assignedBy`, `ownedUntilDate`, `floorMaterial`, `borderMaterial`, `plotSize`, `length`, `width`, `height`, `createdLocation`, `created`, `modified`, `selectedGameMode`)" +
                            "  SELECT `ID`, `ownedBy`, '" + "" +  "', `assignedBy`, `ownedUntilDate`, `floorMaterial`, `borderMaterial`, `plotSize`, `length`, `width`, `height`, `createdLocation`, `created`, `modified`, '" + "" + "'" +
                            "  FROM _" + VirtualRealty.getPluginConfiguration().mysql.plotsTableName + "_old;");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Database getInstance() {
        return instance;
    }

    public static void setInstance(Database database) {
        instance = database;
    }

}

