package client.inventory;

import client.MapleCharacter;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import database.DatabaseConnection;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.MapleInventoryManipulator;

public class MapleRing implements Serializable {
   private static final long serialVersionUID = 9179541993413738579L;
   private long ringId;
   private long ringId2;
   private int partnerId;
   private int itemId;
   private String partnerName;
   private boolean equipped = false;

   private MapleRing(long id, long id2, int partnerId, int itemid, String partnerName) {
      this.ringId = id;
      this.ringId2 = id2;
      this.partnerId = partnerId;
      this.itemId = itemid;
      this.partnerName = partnerName;
   }

   public static MapleRing loadFromDb(long ringId) {
      return loadFromDb(ringId, false);
   }

   public static MapleRing loadFromDb(long ringId, boolean equipped) {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      MapleRing var7;
      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM rings WHERE ringId = ?");
         ps.setLong(1, ringId);
         rs = ps.executeQuery();
         MapleRing ret = null;
         if (rs.next()) {
            ret = new MapleRing(ringId, rs.getLong("partnerRingId"), rs.getInt("partnerChrId"), rs.getInt("itemid"), rs.getString("partnerName"));
            ret.setEquipped(equipped);
         }

         rs.close();
         ps.close();
         con.close();
         var7 = ret;
         return var7;
      } catch (SQLException var17) {
         var17.printStackTrace();
         var7 = null;
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var16) {
            var16.printStackTrace();
         }

      }

      return var7;
   }

   public static void addToDB(int itemid, MapleCharacter chr, String player, int id, long[] ringId) throws SQLException {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("INSERT INTO rings (ringId, itemid, partnerChrId, partnerName, partnerRingId) VALUES (?, ?, ?, ?, ?)");
         ps.setLong(1, ringId[0]);
         ps.setInt(2, itemid);
         ps.setInt(3, chr.getId());
         ps.setString(4, chr.getName());
         ps.setLong(5, ringId[1]);
         ps.executeUpdate();
         ps.close();
         ps = con.prepareStatement("INSERT INTO rings (ringId, itemid, partnerChrId, partnerName, partnerRingId) VALUES (?, ?, ?, ?, ?)");
         ps.setLong(1, ringId[1]);
         ps.setInt(2, itemid);
         ps.setInt(3, id);
         ps.setString(4, player);
         ps.setLong(5, ringId[0]);
         ps.executeUpdate();
         ps.close();
         con.close();
      } catch (Exception var20) {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var19) {
            var19.printStackTrace();
         }
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var18) {
            var18.printStackTrace();
         }

      }

   }

   public static int makeRing(int itemid, MapleCharacter partner, long id, long id2) throws Exception {
      addToDB(itemid, partner, id, id2);
      return 1;
   }

   public static void addToDB(int itemid, MapleCharacter chr, long id, long id2) throws SQLException {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("INSERT INTO rings (ringId, itemid, partnerChrId, partnerName, partnerRingId) VALUES (?, ?, ?, ?, ?)");
         ps.setLong(1, id);
         ps.setInt(2, itemid);
         ps.setInt(3, chr.getId());
         ps.setString(4, chr.getName());
         ps.setLong(5, id2);
         ps.executeUpdate();
         ps.close();
         con.close();
      } catch (Exception var21) {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var20) {
            var20.printStackTrace();
         }
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var19) {
            var19.printStackTrace();
         }

      }

   }

   public static int createRing(int itemid, MapleCharacter partner1, String partner2, String msg, int id2, int sn) {
      try {
         if (partner1 == null) {
            return -2;
         } else {
            return id2 <= 0 ? -1 : makeRing(itemid, partner1, partner2, id2, msg, sn);
         }
      } catch (Exception var7) {
         var7.printStackTrace();
         return 0;
      }
   }

   public static long[] makeRing(int itemid, MapleCharacter partner1, MapleCharacter partner2) {
      long[] ringID = new long[]{MapleInventoryIdentifier.getInstance(), MapleInventoryIdentifier.getInstance()};

      try {
         addToDB(itemid, partner1, partner2.getName(), partner2.getId(), ringID);
      } catch (MySQLIntegrityConstraintViolationException var5) {
         return ringID;
      } catch (SQLException var6) {
         Logger.getLogger(MapleRing.class.getName()).log(Level.SEVERE, (String)null, var6);
      }

      return ringID;
   }

   public static int makeRing(int itemid, MapleCharacter partner1, String partner2, int id2, String msg, int sn) throws Exception {
      long[] ringID = new long[]{MapleInventoryIdentifier.getInstance(), MapleInventoryIdentifier.getInstance()};

      try {
         addToDB(itemid, partner1, partner2, id2, ringID);
      } catch (MySQLIntegrityConstraintViolationException var8) {
         return 0;
      }

      MapleInventoryManipulator.addRing(partner1, itemid, ringID[1], sn, partner2);
      partner1.getCashInventory().gift(id2, partner1.getName(), msg, sn, ringID[0]);
      return 1;
   }

   public long getRingId() {
      return this.ringId;
   }

   public long getPartnerRingId() {
      return this.ringId2;
   }

   public int getPartnerChrId() {
      return this.partnerId;
   }

   public int getItemId() {
      return this.itemId;
   }

   public boolean isEquipped() {
      return this.equipped;
   }

   public void setEquipped(boolean equipped) {
      this.equipped = equipped;
   }

   public String getPartnerName() {
      return this.partnerName;
   }

   public void setPartnerName(String partnerName) {
      this.partnerName = partnerName;
   }

   public boolean equals(Object o) {
      if (o instanceof MapleRing) {
         return ((MapleRing)o).getRingId() == this.getRingId();
      } else {
         return false;
      }
   }

   public int hashCode() {
      long hash = 5L;
      hash = 53L * hash + this.ringId;
      return (int)hash;
   }

   public static void removeRingFromDb(MapleCharacter player) {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM rings WHERE partnerChrId = ?");
         ps.setInt(1, player.getId());
         rs = ps.executeQuery();
         if (rs.next()) {
            int otherId = rs.getInt("partnerRingId");
            int otherotherId = rs.getInt("ringId");
            rs.close();
            ps.close();
            ps = con.prepareStatement("DELETE FROM rings WHERE ringId = ? OR ringId = ?");
            ps.setInt(1, otherotherId);
            ps.setInt(2, otherId);
            ps.executeUpdate();
            ps.close();
            con.close();
            return;
         }

         ps.close();
         rs.close();
      } catch (SQLException var15) {
         var15.printStackTrace();
         return;
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var14) {
            var14.printStackTrace();
         }

      }

   }

   public static class RingComparator implements Comparator<MapleRing>, Serializable {
      public int compare(MapleRing o1, MapleRing o2) {
         if (o1.ringId < o2.ringId) {
            return -1;
         } else {
            return o1.ringId == o2.ringId ? 0 : 1;
         }
      }
   }
}
