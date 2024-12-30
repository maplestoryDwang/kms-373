package server;

import client.InnerSkillValueHolder;
import client.SkillFactory;
import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import tools.Triple;

public class InnerAbillity {
   private static InnerAbillity instance = null;
   private static List<Triple<Byte, Integer, int[]>> rarelists = new ArrayList();
   private static List<Triple<Byte, Integer, int[]>> epiclists = new ArrayList();
   private static List<Triple<Byte, Integer, int[]>> uniquelists = new ArrayList();
   private static List<Triple<Byte, Integer, int[]>> legendlists = new ArrayList();

   public static InnerAbillity getInstance() {
      if (instance == null) {
         instance = new InnerAbillity();
      }

      return instance;
   }

   public final void load() {
      try {
         Connection con = DatabaseConnection.getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT * FROM `abilityinfotable`");

         try {
            ResultSet rs = ps.executeQuery();

            try {
               while(true) {
                  if (!rs.next()) {
                     rs.close();
                     break;
                  }

                  String[] sp = rs.getString("skillpoint").split(",");
                  int[] sps = new int[sp.length];

                  for(int i = 0; i < sp.length; ++i) {
                     sps[i] = Integer.parseInt(sp[i]);
                  }

                  if (rs.getInt("rare") == 0) {
                     rarelists.add(new Triple((byte)rs.getInt("rare"), rs.getInt("skillid"), sps));
                  } else if (rs.getInt("rare") == 1) {
                     epiclists.add(new Triple((byte)rs.getInt("rare"), rs.getInt("skillid"), sps));
                  } else if (rs.getInt("rare") == 2) {
                     uniquelists.add(new Triple((byte)rs.getInt("rare"), rs.getInt("skillid"), sps));
                  } else if (rs.getInt("rare") == 3) {
                     legendlists.add(new Triple((byte)rs.getInt("rare"), rs.getInt("skillid"), sps));
                  }
               }
            } catch (Throwable var9) {
               if (rs != null) {
                  try {
                     rs.close();
                  } catch (Throwable var8) {
                     var9.addSuppressed(var8);
                  }
               }

               throw var9;
            }

            if (rs != null) {
               rs.close();
            }

            ps.close();
         } catch (Throwable var10) {
            if (ps != null) {
               try {
                  ps.close();
               } catch (Throwable var7) {
                  var10.addSuppressed(var7);
               }
            }

            throw var10;
         }

         if (ps != null) {
            ps.close();
         }

         con.close();
         System.out.println("[알림] " + rarelists.size() + "개의 레어 어빌리티를 캐싱 하였습니다.");
         System.out.println("[알림] " + epiclists.size() + "개의 에픽 어빌리티를 캐싱 하였습니다.");
         System.out.println("[알림] " + uniquelists.size() + "개의 유니크 어빌리티를 캐싱 하였습니다.");
         System.out.println("[알림] " + legendlists.size() + "개의 레전드리 어빌리티를 캐싱 하였습니다.");
      } catch (SQLException var11) {
         var11.printStackTrace();
      }

   }

   public InnerSkillValueHolder renewSkill(int rank, boolean circulator) {
      int randskillnum = Randomizer.nextInt(rank == 0 ? rarelists.size() : (rank == 1 ? epiclists.size() : (rank == 2 ? uniquelists.size() : legendlists.size())));
      Triple<Byte, Integer, int[]> randomSkillTriple = rank == 0 ? (Triple)rarelists.get(randskillnum) : (rank == 1 ? (Triple)epiclists.get(randskillnum) : (rank == 2 ? (Triple)uniquelists.get(randskillnum) : (Triple)legendlists.get(randskillnum)));
      int randomSkill = (Integer)randomSkillTriple.getMid();
      byte skillLevel;
      if (circulator) {
         skillLevel = (byte)((int[])randomSkillTriple.getRight())[((int[])randomSkillTriple.getRight()).length - 1];
      } else {
         skillLevel = (byte)((int[])randomSkillTriple.getRight())[Randomizer.nextInt(((int[])randomSkillTriple.getRight()).length)];
      }

      return new InnerSkillValueHolder(randomSkill, skillLevel, (byte)SkillFactory.getSkill(randomSkill).getMaxLevel(), (Byte)randomSkillTriple.getLeft());
   }

   public InnerSkillValueHolder renewLevel(int rank, int skill) {
      List<Triple<Byte, Integer, int[]>> list = rank == 0 ? rarelists : (rank == 1 ? epiclists : (rank == 2 ? uniquelists : legendlists));
      Iterator var4 = list.iterator();

      Triple data;
      do {
         if (!var4.hasNext()) {
            return null;
         }

         data = (Triple)var4.next();
      } while((Integer)data.getMid() != skill);

      return new InnerSkillValueHolder(skill, (byte)((int[])data.getRight())[Randomizer.nextInt(((int[])data.getRight()).length)], (byte)SkillFactory.getSkill(skill).getMaxLevel(), (Byte)data.getLeft());
   }
}
