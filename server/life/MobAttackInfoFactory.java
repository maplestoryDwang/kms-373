package server.life;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.StringUtil;

public class MobAttackInfoFactory {
   private static final MobAttackInfoFactory instance = new MobAttackInfoFactory();
   public static Map<Integer, List<MobAttackInfo>> mobAttacks;
   public static List<String> strings;

   public MobAttackInfoFactory() {
      this.initialize();
   }

   private void initialize() {
      mobAttacks = new HashMap();
      strings = new ArrayList();
      String WZpath = System.getProperty("wz");
      MapleDataProvider stringData = MapleDataProviderFactory.getDataProvider(new File(WZpath + "/String.wz"));
      MapleData mobStringData = stringData.getData("Mob.img");
      Iterator var4 = mobStringData.iterator();

      while(var4.hasNext()) {
         MapleData ms = (MapleData)var4.next();

         try {
            strings.add(ms.getName());
         } catch (Exception var15) {
         }
      }

      MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File(WZpath + "/Mob.wz"));
      Iterator var18 = strings.iterator();

      while(var18.hasNext()) {
         String string = (String)var18.next();

         try {
            if (mobStringData.getChildByPath(string) != null) {
               List<MobAttackInfo> attacks = new ArrayList();
               MapleData mobData = data.getData(StringUtil.getLeftPaddedStr(string + ".img", '0', 11));
               if (mobData != null) {
                  MapleData infoData = mobData.getChildByPath("info/link");
                  if (infoData != null) {
                     String linkedmob = MapleDataTool.getString("info/link", mobData);
                     mobData = data.getData(StringUtil.getLeftPaddedStr(linkedmob + ".img", '0', 11));
                  }

                  for(int j = 0; j < 20; ++j) {
                     MapleData attackData = mobData.getChildByPath("info/attack/" + j);
                     if (attackData != null) {
                        MobAttackInfo ret = new MobAttackInfo(Integer.parseInt(string), j);
                        ret.setDiseaseSkill(MapleDataTool.getInt("disease", attackData, 0));
                        ret.setDiseaseLevel(MapleDataTool.getInt("level", attackData, 0));
                        ret.setMpCon(MapleDataTool.getInt("conMP", attackData, 0));
                        ret.setFixDamR(MapleDataTool.getInt("fixDamR", attackData, 0));
                        MapleData skillData = attackData.getChildByPath("callSkillWithData");
                        if (skillData != null) {
                           MobAttackInfo.MobSkillData skill = new MobAttackInfo.MobSkillData(MapleDataTool.getInt("skill", skillData, 0), MapleDataTool.getInt("level", skillData, 0), MapleDataTool.getInt("delay", skillData, 0));
                           ret.setSkill(skill);
                        }

                        attacks.add(ret);
                     }
                  }
               }

               mobAttacks.put(Integer.parseInt(string), attacks);
            }
         } catch (RuntimeException var16) {
         }
      }

   }

   public static MobAttackInfoFactory getInstance() {
      return instance;
   }

   public static MobAttackInfo getMobAttackInfo(MapleMonster mob, int type) {
      List<MobAttackInfo> attacks = (List)mobAttacks.get(mob.getId());
      Iterator var3 = attacks.iterator();

      MobAttackInfo attack;
      do {
         if (!var3.hasNext()) {
            return null;
         }

         attack = (MobAttackInfo)var3.next();
      } while(attack.getAttackId() != type);

      return attack;
   }
}
