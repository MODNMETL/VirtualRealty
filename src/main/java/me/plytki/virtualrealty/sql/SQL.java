package me.plytki.virtualrealty.sql;

import me.plytki.virtualrealty.VirtualRealty;

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

    private static String pathname = "mysql-settings.";

    private static String host = VirtualRealty.getInstance().getConfig().getString(pathname + "host");
    private static int port = VirtualRealty.getInstance().getConfig().getInt(pathname + "port");
    private static String db_name = VirtualRealty.getInstance().getConfig().getString(pathname + "database-name");
    private static String db_username = VirtualRealty.getInstance().getConfig().getString(pathname + "database-username");
    private static String db_password = VirtualRealty.getInstance().getConfig().getString(pathname + "database-password");
    private static boolean useSSL = VirtualRealty.getInstance().getConfig().getBoolean(pathname + "useSSL");
    private static boolean autoReconnect = VirtualRealty.getInstance().getConfig().getBoolean(pathname + "autoReconnect");

    public static void connect() {
        try {
            if (VirtualRealty.getInstance().getConfig().getString("data-storage").equalsIgnoreCase("h2")) {
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
                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + db_name + "?useSSL=" + useSSL + "&autoReconnect=" + autoReconnect, db_username, db_password);
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
            statement.close();
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void createTables() {
        try {
            SQL.getStatement().execute("CREATE TABLE IF NOT EXISTS `vr_plots` (`ID` INT(12) NOT NULL, `ownedBy` VARCHAR(36) NOT NULL, `assignedBy` VARCHAR(36) NOT NULL, `ownedUntilDate` DATETIME NOT NULL, `floorMaterial` VARCHAR(32) NOT NULL, `plotSize` VARCHAR(32) NOT NULL, `length` INT(24) NOT NULL, `width` INT(24) NOT NULL, `height` INT(24) NOT NULL, `createdLocation` TEXT(500) NOT NULL, PRIMARY KEY(`ID`))");
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
