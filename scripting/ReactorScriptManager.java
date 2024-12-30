package scripting;

import client.MapleClient;
import database.DatabaseConnection;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import server.maps.MapleReactor;
import server.maps.ReactorDropEntry;
import tools.FileoutputUtil;

public class ReactorScriptManager extends AbstractScriptManager {
   private static final ReactorScriptManager instance = new ReactorScriptManager();
   private final Map<Integer, List<ReactorDropEntry>> drops = new HashMap();

   public static final ReactorScriptManager getInstance() {
      return instance;
   }

   public final void act(MapleClient c, MapleReactor reactor) {
      try {
         Invocable iv = this.getInvocable("reactor/" + reactor.getReactorId() + ".js", c);
         if (iv == null) {
            return;
         }

         ScriptEngine scriptengine = (ScriptEngine)iv;
         ReactorActionManager rm = new ReactorActionManager(c, reactor);
         scriptengine.put("rm", rm);
         iv.invokeFunction("act");
      } catch (Exception var6) {
         PrintStream var10000 = System.err;
         int var10001 = reactor.getReactorId();
         var10000.println("Error executing reactor script. ReactorID: " + var10001 + ", ReactorName: " + reactor.getName() + ":" + var6);
         var10001 = reactor.getReactorId();
         FileoutputUtil.log("Log_Script_Except.rtf", "Error executing reactor script. ReactorID: " + var10001 + ", ReactorName: " + reactor.getName() + ":" + var6);
      }

   }

   public final List<ReactorDropEntry> getDrops(int rid) {
      List<ReactorDropEntry> ret = (List)this.drops.get(rid);
      if (ret != null) {
         return ret;
      } else {
         List<ReactorDropEntry> ret = new LinkedList();
         Connection con = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         label128: {
            LinkedList var7;
            try {
               con = DatabaseConnection.getConnection();
               ps = con.prepareStatement("SELECT * FROM reactordrops WHERE reactorid = ?");
               ps.setInt(1, rid);
               rs = ps.executeQuery();

               while(rs.next()) {
                  ret.add(new ReactorDropEntry(rs.getInt("itemid"), rs.getInt("minimum_quantity"), rs.getInt("maximum_quantity"), rs.getInt("chance"), rs.getInt("questid")));
               }

               rs.close();
               ps.close();
               con.close();
               break label128;
            } catch (SQLException var17) {
               System.err.println("Could not retrieve drops for reactor " + rid + var17);
               var7 = ret;
            } finally {
               try {
                  if (ps != null) {
                     ps.close();
                  }

                  if (rs != null) {
                     rs.close();
                  }

                  if (con != null) {
                     con.close();
                  }
               } catch (Exception var16) {
               }

            }

            return var7;
         }

         this.drops.put(rid, ret);
         return ret;
      }
   }

   public final void clearDrops() {
      this.drops.clear();
   }
}
