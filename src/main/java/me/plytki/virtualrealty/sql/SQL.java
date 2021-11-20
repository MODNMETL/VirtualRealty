package me.plytki.virtualrealty.sql;

import me.plytki.virtualrealty.VirtualRealty;
import me.plytki.virtualrealty.configs.PluginConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQL {

    private static Connection connection;
    private static Statement statement;
    public static final long MYSQL_TIMEOUT_MS = 28800000;
    private static long lastQuery = System.currentTimeMillis();

    public static void connect() {
        try {
            switch (VirtualRealty.getPluginConfiguration().dataModel) {
                case H2: {
                    Class.forName("me.plytki.virtualrealty.utils.h2.Driver");
                    connection = DriverManager.getConnection("jdbc:h2:" + VirtualRealty.getInstance().getDataFolder().getAbsolutePath() + "\\data\\data");
                    break;
                }
                case SQLITE: {
                    Class.forName("org.sqlite.JDBC");
                    File dataDir = new File(VirtualRealty.getInstance().getDataFolder().getAbsolutePath() + "\\data");
                    if (!dataDir.exists()) {
                        dataDir.mkdirs();
                    }
                    connection = DriverManager.getConnection("jdbc:sqlite:" + VirtualRealty.getInstance().getDataFolder().getAbsolutePath() + "\\data\\data.db");
                    break;
                }
                case MYSQL: {
                    Class.forName("com.mysql.jdbc.Driver");
                    connection = DriverManager.getConnection("jdbc:mysql://" + VirtualRealty.getPluginConfiguration().mysql.hostname + ":" + VirtualRealty.getPluginConfiguration().mysql.port + "/" + VirtualRealty.getPluginConfiguration().mysql.database + "?useSSL=" + VirtualRealty.getPluginConfiguration().mysql.useSSL + "&autoReconnect=true", VirtualRealty.getPluginConfiguration().mysql.user, VirtualRealty.getPluginConfiguration().mysql.password);
                    break;
                }
            }
            createStatement();
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public static void createStatement() {
        try {
            statement = connection.createStatement();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void closeConnection() {
        try {
            if (statement != null && !statement.isClosed()) {
                statement.close();
            }
            if (connection != null)
                connection.close();
            VirtualRealty.debug("Database connection closed");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void createTables() {
        try {
            statement.execute("CREATE TABLE IF NOT EXISTS `" + VirtualRealty.getPluginConfiguration().mysql.plotsTableName + "` (`ID` INT(12) NOT NULL, `ownedBy` VARCHAR(36) NOT NULL, `members` TEXT, `assignedBy` VARCHAR(36) NOT NULL, `ownedUntilDate` DATETIME NOT NULL, `floorMaterial` VARCHAR(32) NOT NULL, `borderMaterial` VARCHAR(32) NOT NULL, `plotSize` VARCHAR(32) NOT NULL, `length` INT(24) NOT NULL, `width` INT(24) NOT NULL, `height` INT(24) NOT NULL, `createdLocation` TEXT(500) NOT NULL, `created` DATETIME, `modified` DATETIME, PRIMARY KEY(`ID`))");
            updateTables();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    public static void updateTables() {
        try {
            statement.execute("ALTER TABLE `" + VirtualRealty.getPluginConfiguration().mysql.plotsTableName + "` ADD `borderMaterial` VARCHAR(32) AFTER `floorMaterial`;");
        } catch (SQLException ignored) {

        }
        try {
            statement.execute("ALTER TABLE `" + VirtualRealty.getPluginConfiguration().mysql.plotsTableName + "` ADD `members` TEXT AFTER `ownedBy`;");
        } catch (SQLException ignored) {

        }
        try {
            statement.execute("ALTER TABLE `" + VirtualRealty.getPluginConfiguration().mysql.plotsTableName + "` ADD `created` DATETIME AFTER `createdLocation`;");
        } catch (SQLException ignored) {

        }
        try {
            statement.execute("ALTER TABLE `" + VirtualRealty.getPluginConfiguration().mysql.plotsTableName + "` ADD `modified` DATETIME AFTER `created`;");
        } catch (SQLException ignored) {

        }
    }

    public static Statement getStatement() {
        try {
            if (System.currentTimeMillis() - lastQuery > MYSQL_TIMEOUT_MS) {
                connection.close();
                statement.close();
                connect();
            } else if(connection.isClosed()) {
                connect();
            }
            lastQuery = System.currentTimeMillis();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statement;
    }


    public static void setConnection(Connection connection) {
        SQL.connection = connection;
    }

    public static void setStatement(Statement statement) {
        SQL.statement = statement;
    }

}
