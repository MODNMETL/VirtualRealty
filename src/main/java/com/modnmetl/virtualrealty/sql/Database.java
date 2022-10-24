package com.modnmetl.virtualrealty.sql;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.configs.PluginConfiguration;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.SneakyThrows;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    @Getter
    private static Database instance;

    @Getter
    private final PluginConfiguration.DataModel dataModel;

    @Getter
    private final DataSource dataSource;

    public Database(File file) {
        dataModel = VirtualRealty.getPluginConfiguration().dataModel;
        SQLiteDataSource sqLiteDataSource = new SQLiteDataSource();
        sqLiteDataSource.setUrl("jdbc:sqlite:" + file.getAbsolutePath());
        dataSource = sqLiteDataSource;
        instance = this;
        createTables();
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
        instance = this;
        createTables();
    }

    @SneakyThrows
    public Connection getConnection() {
        return dataSource.getConnection();
    }


    private void createTables() {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `" + VirtualRealty.getPluginConfiguration().mysql.plotsTableName + "` (`ID` INT(12) NOT NULL, `ownedBy` VARCHAR(36) NOT NULL, `nonMemberPermissions` TEXT NOT NULL, `assignedBy` VARCHAR(36) NOT NULL, `ownedUntilDate` DATETIME NOT NULL, `floorMaterial` VARCHAR(32) NOT NULL, `borderMaterial` VARCHAR(32) NOT NULL, `plotSize` VARCHAR(32) NOT NULL, `length` INT(24) NOT NULL, `width` INT(24) NOT NULL, `height` INT(24) NOT NULL, `createdLocation` TEXT(500) NOT NULL, `created` DATETIME, `modified` DATETIME, `selectedGameMode` TEXT, PRIMARY KEY(`ID`))")) {
            ps.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `" + VirtualRealty.getPluginConfiguration().mysql.plotMembersTableName + "` (`uuid` VARCHAR(36) NOT NULL, `plot` INT(11) NOT NULL, `selectedGameMode` TEXT NOT NULL, `permissions` TEXT NOT NULL, `managementPermissions` TEXT NOT NULL)")) {
             ps.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void connectToDatabase(File databaseFile) throws SQLException {
        if (VirtualRealty.getPluginConfiguration().dataModel == PluginConfiguration.DataModel.SQLITE) new Database(databaseFile);
        if (VirtualRealty.getPluginConfiguration().dataModel == PluginConfiguration.DataModel.MYSQL)
            new Database(
                    VirtualRealty.getPluginConfiguration().mysql.hostname,
                    VirtualRealty.getPluginConfiguration().mysql.port,
                    VirtualRealty.getPluginConfiguration().mysql.user,
                    VirtualRealty.getPluginConfiguration().mysql.password,
                    VirtualRealty.getPluginConfiguration().mysql.database
            );
        VirtualRealty.debug("Connected to database");
    }

}

