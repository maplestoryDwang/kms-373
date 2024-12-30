package server;

import client.MapleCharacter;
import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import provider.MapleData;
import provider.MapleDataProviderFactory;
import tools.Pair;
import tools.Triple;

public class ChatEmoticon {
   private static Map<Integer, List<Integer>> emoticons = new ConcurrentHashMap();

   public static void LoadEmoticon() {
      MapleData effdata = MapleDataProviderFactory.getDataProvider(MapleDataProviderFactory.fileInWZPath("UI.wz")).getData("ChatEmoticon.img");
      Iterator var1 = effdata.iterator();

      while(var1.hasNext()) {
         MapleData data = (MapleData)var1.next();
         int type = Integer.parseInt(data.getName());
         List<Integer> e_list = new ArrayList();
         Iterator var5 = data.iterator();

         while(var5.hasNext()) {
            MapleData dat = (MapleData)var5.next();
            if (isNumeric(dat.getName())) {
               e_list.add(Integer.parseInt(dat.getName()));
            }
         }

         getEmoticons().put(type, e_list);
      }

   }

   public static void LoadChatEmoticonTabs(MapleCharacter chr) {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM emoticon WHERE charid = ?");
         ps.setInt(1, chr.getId());
         rs = ps.executeQuery();

         while(rs.next()) {
            int emoticonId = rs.getInt("emoticonid");
            long time = rs.getLong("time");
            String bookmarks = rs.getString("bookmarks");
            MapleChatEmoticon em = new MapleChatEmoticon(chr.getId(), emoticonId, time, bookmarks);
            chr.getEmoticonTabs().add(em);
         }

         ps.close();
         rs.close();
      } catch (Exception var25) {
         var25.printStackTrace();
      } finally {
         if (ps != null) {
            try {
               ps.close();
            } catch (SQLException var24) {
            }
         }

         if (rs != null) {
            try {
               rs.close();
            } catch (SQLException var23) {
            }
         }

         if (con != null) {
            try {
               con.close();
            } catch (SQLException var22) {
               var22.printStackTrace();
            }
         }

      }

   }

   public static void LoadSavedChatEmoticon(MapleCharacter chr) {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM emoticon_saved WHERE charid = ?");
         ps.setInt(1, chr.getId());
         rs = ps.executeQuery();

         while(rs.next()) {
            int emoticonId = rs.getInt("emoticonid");
            String chat = rs.getString("chat");
            MapleSavedEmoticon em = new MapleSavedEmoticon(chr.getId(), emoticonId, chat);
            chr.getSavedEmoticon().add(em);
         }

         ps.close();
         rs.close();
      } catch (Exception var23) {
         var23.printStackTrace();
      } finally {
         if (ps != null) {
            try {
               ps.close();
            } catch (SQLException var22) {
            }
         }

         if (rs != null) {
            try {
               rs.close();
            } catch (SQLException var21) {
            }
         }

         if (con != null) {
            try {
               con.close();
            } catch (SQLException var20) {
               var20.printStackTrace();
            }
         }

      }

   }

   public static void LoadChatEmoticons(MapleCharacter chr, List<MapleChatEmoticon> ems) {
      Iterator var2 = ems.iterator();

      while(var2.hasNext()) {
         MapleChatEmoticon em = (MapleChatEmoticon)var2.next();
         List<Integer> e_list = (List)getEmoticons().get(em.getEmoticonid());
         Iterator iterator = e_list.iterator();

         while(iterator.hasNext()) {
            int e = (Integer)iterator.next();
            short slot = 0;
            Iterator var8 = em.getBookmarks().iterator();

            while(var8.hasNext()) {
               Pair<Integer, Short> a = (Pair)var8.next();
               if ((Integer)a.left == e && (Short)a.right > 0) {
                  slot = (Short)a.right;
               }
            }

            Triple<Long, Integer, Short> p = new Triple(em.getTime(), e, slot);
            chr.getEmoticons().add(p);
         }
      }

   }

   public static boolean isNumeric(String input) {
      try {
         Double.parseDouble(input);
         return true;
      } catch (NumberFormatException var2) {
         return false;
      }
   }

   public static Map<Integer, List<Integer>> getEmoticons() {
      return emoticons;
   }
}
