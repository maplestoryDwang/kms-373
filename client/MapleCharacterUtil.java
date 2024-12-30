package client;

import constants.GameConstants;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.world.World;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;
import tools.Triple;
import tools.packet.CSPacket;

public class MapleCharacterUtil {
   private static final Pattern namePattern = Pattern.compile("[a-zA-Z0-9]{4,12}");
   private static final Pattern name2Pattern = Pattern.compile("[가-힣a-zA-Z0-9\\w\\s]{4,12}");
   private static final Pattern petPattern = Pattern.compile("[a-zA-Z0-9]{4,12}");

   public static final boolean canCreateChar(String name, boolean gm) {
      return name.getBytes().length >= 4 && name.getBytes().length <= 13 && getIdByName(name) == -1;
   }

   public static final boolean canCreateChar(String name) {
      return name.getBytes().length >= 2 && name.getBytes().length <= 13;
   }

   public static final boolean isEligibleCharName(String name, boolean gm) {
      if (name.length() > 12) {
         return false;
      } else if (gm) {
         return true;
      } else if (name.length() >= 3 && namePattern.matcher(name).matches()) {
         String[] var2 = GameConstants.RESERVED;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            String z = var2[var4];
            if (name.indexOf(z) != -1) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static final boolean isEligibleCharNameTwo(String name, boolean gm) {
      if (name.length() > 12) {
         return false;
      } else if (gm) {
         return true;
      } else {
         String[] var2 = GameConstants.RESERVED;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            String z = var2[var4];
            if (name.indexOf(z) != -1) {
               return false;
            }
         }

         return true;
      }
   }

   public static final boolean canChangePetName(String name) {
      if (name.getBytes(Charset.forName("MS949")).length > 12) {
         return false;
      } else if (name.getBytes(Charset.forName("MS949")).length < 3) {
         return false;
      } else if (petPattern.matcher(name).matches()) {
         String[] var1 = GameConstants.RESERVED;
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            String z = var1[var3];
            if (name.indexOf(z) != -1) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static final String makeMapleReadable(String in) {
      String wui = in.replace('I', 'i');
      wui = wui.replace('l', 'L');
      wui = wui.replace("rn", "Rn");
      wui = wui.replace("vv", "Vv");
      wui = wui.replace("VV", "Vv");
      return wui;
   }

   public static final int getIdByName(String name) {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT id FROM characters WHERE name = ?");
         ps.setString(1, name);
         rs = ps.executeQuery();
         if (!rs.next()) {
            rs.close();
            ps.close();
            byte var18 = -1;
            return var18;
         }

         int id = rs.getInt("id");
         rs.close();
         ps.close();
         int var5 = id;
         return var5;
      } catch (SQLException var16) {
         System.err.println("error 'getIdByName' " + var16);
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
         } catch (SQLException var15) {
            var15.printStackTrace();
         }

      }

      return -1;
   }

   public static final int getAccByName(String name) {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      byte var4;
      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT accountid FROM characters WHERE name LIKE ?");
         ps.setString(1, name);
         rs = ps.executeQuery();
         if (rs.next()) {
            int id = rs.getInt("accountid");
            rs.close();
            ps.close();
            int var5 = id;
            return var5;
         }

         rs.close();
         ps.close();
         var4 = -1;
      } catch (SQLException var16) {
         System.err.println("error 'getIdByName' " + var16);
         return -1;
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
         } catch (SQLException var15) {
            var15.printStackTrace();
         }

      }

      return var4;
   }

   public static final int Change_SecondPassword(int accid, String password, String newpassword) {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      byte var30;
      try {
         byte var7;
         try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT * from accounts where id = ?");
            ps.setInt(1, accid);
            rs = ps.executeQuery();
            if (!rs.next()) {
               rs.close();
               ps.close();
               var7 = -1;
               return var7;
            }

            String secondPassword = rs.getString("2ndpassword");
            String salt2 = rs.getString("salt2");
            byte var9;
            if (secondPassword != null && salt2 != null) {
               secondPassword = LoginCrypto.rand_r(secondPassword);
            } else if (secondPassword == null && salt2 == null) {
               rs.close();
               ps.close();
               var9 = 0;
               return var9;
            }

            if (!check_ifPasswordEquals(secondPassword, password, salt2)) {
               rs.close();
               ps.close();
               var9 = 1;
               return var9;
            }

            rs.close();
            ps.close();

            String SHA1hashedsecond;
            try {
               SHA1hashedsecond = LoginCryptoLegacy.encodeSHA1(newpassword);
            } catch (Exception var26) {
               byte var10 = -2;
               return var10;
            }

            ps = con.prepareStatement("UPDATE accounts set 2ndpassword = ?, salt2 = ? where id = ?");
            ps.setString(1, SHA1hashedsecond);
            ps.setString(2, (String)null);
            ps.setInt(3, accid);
            if (!ps.execute()) {
               ps.close();
               var9 = 2;
               return var9;
            }

            ps.close();
            var30 = -2;
         } catch (SQLException var27) {
            System.err.println("error 'getIdByName' " + var27);
            var7 = -2;
            return var7;
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
               rs.close();
            }
         } catch (SQLException var25) {
            var25.printStackTrace();
         }

      }

      return var30;
   }

   private static final boolean check_ifPasswordEquals(String passhash, String pwd, String salt) {
      if (LoginCryptoLegacy.isLegacyPassword(passhash) && LoginCryptoLegacy.checkPassword(pwd, passhash)) {
         return true;
      } else if (salt == null && LoginCrypto.checkSha1Hash(passhash, pwd)) {
         return true;
      } else {
         return LoginCrypto.checkSaltedSha512Hash(passhash, pwd, salt);
      }
   }

   public static Triple<Integer, Integer, Integer> getInfoByName(String name, int world) {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Triple id;
      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM characters WHERE name = ? AND world = ?");
         ps.setString(1, name);
         ps.setInt(2, world);
         rs = ps.executeQuery();
         if (rs.next()) {
            id = new Triple(rs.getInt("id"), rs.getInt("accountid"), rs.getInt("gender"));
            rs.close();
            ps.close();
            con.close();
            Triple var6 = id;
            return var6;
         }

         rs.close();
         ps.close();
         id = null;
      } catch (Exception var17) {
         var17.printStackTrace();
         return null;
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

      return id;
   }

   public static void setNXCodeUsed(String name, String code) throws SQLException {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         ps = ((Connection)con).prepareStatement("UPDATE nxcode SET `user` = ?, `valid` = 0 WHERE code = ?");
         ps.setString(1, name);
         ps.setString(2, code);
         ps.execute();
         ps.close();
         ((Connection)con).close();
      } catch (Exception var17) {
         try {
            if (con != null) {
               ((Connection)con).close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var16) {
            var16.printStackTrace();
         }
      } finally {
         try {
            if (con != null) {
               ((Connection)con).close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var15) {
            var15.printStackTrace();
         }

      }

   }

   public static void sendNote(String to, String name, String msg, int fame, int type, int senderid) {
      Connection con = null;
      PreparedStatement ps = null;
      Object var8 = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("INSERT INTO notes (`to`, `from`, `message`, `timestamp`, `gift`, `show`, `type`, `senderid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
         ps.setString(1, to);
         ps.setString(2, name);
         ps.setString(3, msg);
         ps.setLong(4, System.currentTimeMillis());
         ps.setInt(5, fame);
         ps.setInt(6, 1);
         ps.setInt(7, type);
         ps.setInt(8, senderid);
         ps.executeUpdate();
         ps.close();
         con.close();
      } catch (SQLException var18) {
         System.err.println("Unable to send note" + var18);
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var17) {
         }

      }

      if (World.Find.findChannel(to) >= 0) {
         MapleCharacter chr = ChannelServer.getInstance(World.Find.findChannel(to)).getPlayerStorage().getCharacterByName(to);
         if (chr != null) {
            chr.getClient().send(CSPacket.NoteHandler(16, 0));
         }
      }

   }

   public static Triple<Boolean, Integer, Integer> getNXCodeInfo(String code) throws SQLException {
      Triple<Boolean, Integer, Integer> ret = null;
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT `valid`, `type`, `item` FROM nxcode WHERE code LIKE ?");
         ps.setString(1, code);
         rs = ps.executeQuery();
         if (rs.next()) {
            ret = new Triple(rs.getInt("valid") > 0, rs.getInt("type"), rs.getInt("item"));
         }

         rs.close();
         ps.close();
         con.close();
      } catch (Exception var17) {
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
         } catch (SQLException var15) {
            var15.printStackTrace();
         }

      }

      return ret;
   }
}
