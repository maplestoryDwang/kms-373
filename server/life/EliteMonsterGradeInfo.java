package server.life;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;

public class EliteMonsterGradeInfo {
   private static Map<Integer, EliteMonsterGradeInfo> FirstGradeInfo = new LinkedHashMap();
   private static Map<Integer, EliteMonsterGradeInfo> SecondGradeInfo = new LinkedHashMap();
   private static Map<Integer, EliteMonsterGradeInfo> ThirdGradeInfo = new LinkedHashMap();
   private static Map<Integer, EliteMonsterGradeInfo> MistAffectedInfo = new LinkedHashMap();
   private int skillid;
   private int skilllv;
   private int affectedOtherSkillID;
   private int affectedOtherSkillLev;

   public EliteMonsterGradeInfo(int skillid, int skilllv) {
      this.skillid = skillid;
      this.skilllv = skilllv;
   }

   public static Map<Integer, EliteMonsterGradeInfo> getFirstGradeInfo() {
      return FirstGradeInfo;
   }

   public static void setFirstGradeInfo(Map<Integer, EliteMonsterGradeInfo> FirstGradeInfo) {
      EliteMonsterGradeInfo.FirstGradeInfo = FirstGradeInfo;
   }

   public static Map<Integer, EliteMonsterGradeInfo> getSecondGradeInfo() {
      return SecondGradeInfo;
   }

   public static void setSecondGradeInfo(Map<Integer, EliteMonsterGradeInfo> SecondGradeInfo) {
      EliteMonsterGradeInfo.SecondGradeInfo = SecondGradeInfo;
   }

   public static Map<Integer, EliteMonsterGradeInfo> getThirdGradeInfo() {
      return ThirdGradeInfo;
   }

   public static void setThirdGradeInfo(Map<Integer, EliteMonsterGradeInfo> ThirdGradeInfo) {
      EliteMonsterGradeInfo.ThirdGradeInfo = ThirdGradeInfo;
   }

   public int getSkillid() {
      return this.skillid;
   }

   public void setSkillid(int skillid) {
      this.skillid = skillid;
   }

   public int getSkilllv() {
      return this.skilllv;
   }

   public void setSkilllv(int skilllv) {
      this.skilllv = skilllv;
   }

   public static void loadFromWZData() {
      FirstGradeInfo.clear();
      SecondGradeInfo.clear();
      ThirdGradeInfo.clear();
      MapleData effdata = MapleDataProviderFactory.getDataProvider(MapleDataProviderFactory.fileInWZPath("Skill.wz")).getData("EliteMobSkill.img");
      Iterator var1 = effdata.getChildren().iterator();

      while(var1.hasNext()) {
         MapleData dataType = (MapleData)var1.next();
         int type = Integer.parseInt(dataType.getName());
         Iterator var4 = dataType.getChildren().iterator();

         while(var4.hasNext()) {
            MapleData info = (MapleData)var4.next();
            EliteMonsterGradeInfo eg = null;
            int skillid = MapleDataTool.getInt("skill", info, 0);
            int skilllv = MapleDataTool.getInt("level", info, 0);
            eg = new EliteMonsterGradeInfo(skillid, skilllv);
            if (eg != null) {
               if (type == 0) {
                  FirstGradeInfo.put(Integer.parseInt(info.getName()), eg);
               } else if (type == 1) {
                  SecondGradeInfo.put(Integer.parseInt(info.getName()), eg);
               } else if (type == 2) {
                  ThirdGradeInfo.put(Integer.parseInt(info.getName()), eg);
               }
            }
         }
      }

   }
}
