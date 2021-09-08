package me.plytki.virtualrealty.sql;

import me.plytki.virtualrealty.VirtualRealty;
import me.plytki.virtualrealty.configs.PluginConfiguration;

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
            if (VirtualRealty.getPluginConfiguration().dataModel == PluginConfiguration.DataModel.H2) {
                Class.forName("me.plytki.virtualrealty.utils.h2.Driver");
                connection = DriverManager.getConnection("jdbc:h2:" + VirtualRealty.getInstance().getDataFolder().getAbsolutePath() + "\\data\\data");
//                Class.forName("org.sqlite.JDBC");
//                File dataDir = new File(VirtualRealty.getInstance().getDataFolder().getAbsolutePath() + "\\data");
//                if (!dataDir.exists()) {
//                    dataDir.mkdirs();
//                }
//                connection = DriverManager.getConnection("jdbc:sqlite:" + VirtualRealty.getInstance().getDataFolder().getAbsolutePath() + "\\data\\data.db");
            } else {
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + VirtualRealty.getPluginConfiguration().mysql.hostname + ":" + VirtualRealty.getPluginConfiguration().mysql.port + "/" + VirtualRealty.getPluginConfiguration().mysql.database + "?useSSL=" + VirtualRealty.getPluginConfiguration().mysql.useSSL + "&autoReconnect=true", VirtualRealty.getPluginConfiguration().mysql.user, VirtualRealty.getPluginConfiguration().mysql.password);
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
            if (VirtualRealty.getPluginConfiguration().dataModel.equals(PluginConfiguration.DataModel.H2)) {
                statement.execute("SHUTDOWN");
            }
            statement.close();
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void createTables() {
        try {
            SQL.getStatement().execute("CREATE TABLE IF NOT EXISTS `" + VirtualRealty.getPluginConfiguration().mysql.plotsTableName + "` (`ID` INT(12) NOT NULL, `ownedBy` VARCHAR(36) NOT NULL, `assignedBy` VARCHAR(36) NOT NULL, `ownedUntilDate` DATETIME NOT NULL, `floorMaterial` VARCHAR(32) NOT NULL, `plotSize` VARCHAR(32) NOT NULL, `length` INT(24) NOT NULL, `width` INT(24) NOT NULL, `height` INT(24) NOT NULL, `createdLocation` TEXT(500) NOT NULL, PRIMARY KEY(`ID`))");
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static Statement getStatement() {
        try {
            if (System.currentTimeMillis() - lastQuery > MYSQL_TIMEOUT_MS) {
                connection.close();
                statement.close();
                connect();
                createStatement();
            } else if(connection.isClosed()) {
                connect();
                if(statement.isClosed()) {
                    createStatement();
                }
            }
            lastQuery = System.currentTimeMillis();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statement;
    }

}
