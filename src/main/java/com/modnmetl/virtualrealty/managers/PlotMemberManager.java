package com.modnmetl.virtualrealty.managers;

import com.modnmetl.virtualrealty.VirtualRealty;
import com.modnmetl.virtualrealty.objects.PlotMember;
import com.modnmetl.virtualrealty.sql.Database;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PlotMemberManager {

    public static void loadMembers() {
        try {
            ResultSet rs = Database.getInstance().getStatement().executeQuery("SELECT * FROM `" + VirtualRealty.getPluginConfiguration().mysql.plotMembersTableName + "`");
            while (rs.next()) {
                new PlotMember(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
