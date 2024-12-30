package server.life;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;

public class AffectedOtherSkillInfo {
   private static Map<Integer, AffectedOtherSkillInfo> MistAffectedInfo = new LinkedHashMap();
   private int num;
   private int affectedOtherSkillID;
   private int affectedOtherSkillLev;

   public AffectedOtherSkillInfo(int num, int affectedOtherSkillID, int affectedOtherSkillLev) {
      this.num = num;
      this.affectedOtherSkillID = affectedOtherSkillID;
      this.affectedOtherSkillLev = affectedOtherSkillLev;
   }

   public static Map<Integer, AffectedOtherSkillInfo> getMistAffectedInfo() {
      return MistAffectedInfo;
   }

   public static void setMistAffectedInfo(Map<Integer, AffectedOtherSkillInfo> MistAffectedInfo) {
      AffectedOtherSkillInfo.MistAffectedInfo = MistAffectedInfo;
   }

   public int getNum() {
      return this.num;
   }

   public void setNum(int num) {
      this.num = num;
   }

   public int getAffectedOtherSkillID() {
      return this.affectedOtherSkillID;
   }

   public void setAffectedOtherSkillID(int affectedOtherSkillID) {
      this.affectedOtherSkillID = affectedOtherSkillID;
   }

   public int getAffectedOtherSkillLev() {
      return this.affectedOtherSkillLev;
   }

   public void setAffectedOtherSkillLev(int affectedOtherSkillLev) {
      this.affectedOtherSkillLev = affectedOtherSkillLev;
   }

   public static void loadFromWZData() {
      MistAffectedInfo.clear();
      MapleData effdata = MapleDataProviderFactory.getDataProvider(MapleDataProviderFactory.fileInWZPath("Skill.wz/MobSkill")).getData("211.img");
      Iterator var1 = effdata.getChildren().iterator();

      while(var1.hasNext()) {
         MapleData dataType = (MapleData)var1.next();
         Iterator var3 = dataType.getChildren().iterator();

         label55:
         while(var3.hasNext()) {
            MapleData info = (MapleData)var3.next();
            int number = Integer.parseInt(info.getName());
            Iterator var6 = info.getChildren().iterator();

            label53:
            while(true) {
               MapleData infos;
               do {
                  if (!var6.hasNext()) {
                     continue label55;
                  }

                  infos = (MapleData)var6.next();
               } while(!infos.getName().equals("affectedOtherSkill"));

               Iterator var8 = infos.getChildren().iterator();

               while(true) {
                  while(true) {
                     if (!var8.hasNext()) {
                        continue label53;
                     }

                     MapleData infos1 = (MapleData)var8.next();
                     int number2;
                     if (!infos1.getName().equals("0") && !infos1.getName().equals("1") && !infos1.getName().equals("2")) {
                        number2 = MapleDataTool.getInt("affectedOtherSkillID", infos, 0);
                        int affectedOtherSkillLev = MapleDataTool.getInt("affectedOtherSkillLev", infos, 0);
                        AffectedOtherSkillInfo AOS = new AffectedOtherSkillInfo(-1, number2, affectedOtherSkillLev);
                        MistAffectedInfo.put(number, AOS);
                     } else {
                        number2 = Integer.parseInt(infos1.getName());
                        Iterator var11 = infos1.getChildren().iterator();

                        while(var11.hasNext()) {
                           MapleData infos2 = (MapleData)var11.next();
                           int i = MapleDataTool.getInt("affectedOtherSkillID", infos1, 0);
                           int j = MapleDataTool.getInt("affectedOtherSkillLev", infos1, 0);
                           AffectedOtherSkillInfo affectedOtherSkillInfo = new AffectedOtherSkillInfo(number2, i, j);
                           MistAffectedInfo.put(number, affectedOtherSkillInfo);
                        }
                     }
                  }
               }
            }
         }
      }

   }
}
