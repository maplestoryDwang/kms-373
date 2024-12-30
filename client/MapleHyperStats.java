package client;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import tools.Pair;

public class MapleHyperStats implements Serializable {
   private int position;
   private int skillid;
   private int skilllevel;
   private Map<Integer, Pair<Integer, Integer>> hyperstats;

   public MapleHyperStats(int pos, int skill, int level) {
      this.position = pos;
      this.skillid = skill;
      this.skilllevel = level;
   }

   public MapleHyperStats MapleHyperStats(int pos, int skill, int level) {
      this.position = pos;
      this.skillid = skill;
      this.skilllevel = level;
      return this;
   }

   public void saveToDB(Connection connection) throws SQLException {
      try {
         try {
            PreparedStatement hps = connection.prepareStatement("UPDATE hyperstats SET chrid = ?, pos = ?, skillid = ?, skilllevel = ? WHERE chrid = ?");
            hps.setInt(1, 1);
            hps.setInt(2, this.position);
            hps.setInt(3, this.skillid);
            hps.setInt(4, this.skilllevel);
            hps.executeUpdate();
            hps.close();
            connection.close();
         } catch (Exception var6) {
         }

      } finally {
         ;
      }
   }

   public static ArrayList<MapleHyperStats> loadFromDB(Connection connection, int characterId) {
      ArrayList<MapleHyperStats> hyperStats = new ArrayList();
      return hyperStats;
   }

   public int getPosition() {
      return this.position;
   }

   public void setPosition(int position) {
      this.position = position;
   }

   public int getSkillid() {
      return this.skillid;
   }

   public void setSkillid(int skill) {
      this.skillid = skill;
   }

   public int getSkillLevel() {
      return this.skilllevel;
   }

   public void setSkillLevel(int level) {
      this.skilllevel = level;
   }
}
