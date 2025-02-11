package server.life;

import database.DatabaseConnection;
import java.awt.Point;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tools.Pair;

public class MobSkillFactory {
   private final Map<Pair<Integer, Integer>, MobSkill> mobSkillCache = new HashMap();
   private static final MobSkillFactory instance = new MobSkillFactory();

   public MobSkillFactory() {
      this.initialize();
   }

   public static MobSkillFactory getInstance() {
      return instance;
   }

   public static MobSkill getMobSkill(int skillId, int level) {
      return (MobSkill)instance.mobSkillCache.get(new Pair(skillId, level));
   }

   private void initialize() {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM wz_mobskilldata");
         rs = ps.executeQuery();

         while(rs.next()) {
            this.mobSkillCache.put(new Pair(rs.getInt("skillid"), rs.getInt("level")), this.get(rs));
         }

         rs.close();
         ps.close();
      } catch (Exception var13) {
         var13.printStackTrace();
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
         } catch (SQLException var12) {
            var12.printStackTrace();
         }

      }

   }

   private MobSkill get(ResultSet rs) throws SQLException {
      List<Integer> toSummon = new ArrayList();
      String[] summs = rs.getString("summons").split(", ");
      if (summs.length <= 0 && rs.getString("summons").length() > 0) {
         toSummon.add(Integer.parseInt(rs.getString("summons")));
      }

      String[] var4 = summs;
      int var5 = summs.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         String s = var4[var6];
         if (s.length() > 0) {
            toSummon.add(Integer.parseInt(s));
         }
      }

      Point lt = null;
      Point rb = null;
      if (rs.getInt("ltx") != 0 || rs.getInt("lty") != 0 || rs.getInt("rbx") != 0 || rs.getInt("rby") != 0) {
         lt = new Point(rs.getInt("ltx"), rs.getInt("lty"));
         rb = new Point(rs.getInt("rbx"), rs.getInt("rby"));
      }

      MobSkill ret = new MobSkill(rs.getInt("skillid"), rs.getInt("level"));
      ret.addSummons(toSummon);
      ret.setInterval((long)Math.max(5000, rs.getInt("interval") * 1000));
      ret.setDuration((long)(rs.getInt("time") * 1000));
      ret.setHp(rs.getInt("hp") == 0 ? 100 : rs.getInt("hp"));
      ret.setMpCon(rs.getInt("mpcon"));
      ret.setSpawnEffect(rs.getInt("spawneffect"));
      ret.setX(rs.getInt("x"));
      ret.setY(rs.getInt("y"));
      ret.setProp((float)rs.getInt("prop") / 100.0F);
      ret.setLimit((short)rs.getInt("limit"));
      ret.setOnce(rs.getByte("once") > 0);
      ret.setMobGroup(rs.getByte("ismobgroup") > 0);
      ret.setLtRb(lt, rb);
      ret.setOtherSkillID(rs.getInt("otherSkillID"));
      ret.setOtherSkillLev(rs.getInt("otherSkillLev"));
      ret.setSkillAfter(rs.getInt("skillAfter"));
      ret.setForce(rs.getInt("forced"));
      return ret;
   }
}
