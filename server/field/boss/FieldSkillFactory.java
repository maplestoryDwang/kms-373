package server.field.boss;

import java.awt.Point;
import java.awt.Rectangle;
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
import tools.Pair;

public class FieldSkillFactory {
   private final Map<Pair<Integer, Integer>, FieldSkill> fieldSkillCache = new HashMap();
   private static final FieldSkillFactory instance = new FieldSkillFactory();

   public FieldSkillFactory() {
      this.load();
   }

   public static FieldSkillFactory getInstance() {
      return instance;
   }

   public static FieldSkill getFieldSkill(int skillId, int level) {
      return (FieldSkill)instance.fieldSkillCache.get(new Pair(skillId, level));
   }

   public void load() {
      MapleDataProvider skill2 = MapleDataProviderFactory.getDataProvider(new File("Wz/Skill.wz"));
      MapleData skillz = skill2.getData("FieldSkill.img");
      System.out.println("Adding into wz_fieldskilldata.....");
      Iterator var3 = skillz.getChildren().iterator();

      while(var3.hasNext()) {
         MapleData skill3 = (MapleData)var3.next();
         Iterator var5 = skill3.getChildByPath("level").getChildren().iterator();

         while(var5.hasNext()) {
            MapleData lvlz = (MapleData)var5.next();
            int skillId = Integer.parseInt(skill3.getName());
            int skillLevel = Integer.parseInt(lvlz.getName());
            FieldSkill skill = new FieldSkill(skillId, skillLevel);
            MapleData summonData = lvlz.getChildByPath("summonedSequenceInfo");
            List<FieldSkill.SummonedSequenceInfo> infoList = new ArrayList();
            if (summonData != null) {
               Iterator var12 = summonData.iterator();

               while(var12.hasNext()) {
                  MapleData sumData = (MapleData)var12.next();

                  for(int i = 0; i > -1 && sumData.getChildByPath(String.valueOf(i)) != null; ++i) {
                     int attackerId = MapleDataTool.getInt("id", sumData.getChildByPath(String.valueOf(i)), 0);
                     Point sumPosition = MapleDataTool.getPoint("pt", sumData.getChildByPath(String.valueOf(i)), new Point(0, 0));
                     infoList.add(new FieldSkill.SummonedSequenceInfo(attackerId, sumPosition));
                  }
               }
            }

            skill.setSummonedSequenceInfoList(infoList);
            MapleData attackData = lvlz.getChildByPath("attackInfo");
            List<FieldSkill.FieldFootHold> footHolds = new ArrayList();
            if (attackData != null) {
               Iterator var29 = attackData.iterator();

               while(var29.hasNext()) {
                  MapleData atkData = (MapleData)var29.next();

                  for(int i = 0; i > -1 && atkData.getChildByPath(String.valueOf(i)) != null; ++i) {
                     int duration = MapleDataTool.getInt("duration", atkData.getChildByPath(String.valueOf(i)), 0);
                     int interval = MapleDataTool.getInt("interval", atkData.getChildByPath(String.valueOf(i)), 0);
                     int angleMin = MapleDataTool.getInt("angleMin", atkData.getChildByPath(String.valueOf(i)), 0);
                     int angleMax = MapleDataTool.getInt("angleMax", atkData.getChildByPath(String.valueOf(i)), 0);
                     int attackDelay = MapleDataTool.getInt("attackDelay", atkData.getChildByPath(String.valueOf(i)), 0);
                     int z = MapleDataTool.getInt("z", atkData.getChildByPath(String.valueOf(i)), 0);
                     int set = MapleDataTool.getInt("set", atkData.getChildByPath(String.valueOf(i)), 0);
                     Point lt = null;
                     Point rb = null;
                     Point pos = null;
                     if (lvlz.getChildByPath("lt") != null) {
                        lt = (Point)lvlz.getChildByPath("lt").getData();
                     }

                     if (lvlz.getChildByPath("rb") != null) {
                        rb = (Point)lvlz.getChildByPath("rb").getData();
                     }

                     if (lvlz.getChildByPath("pos") != null) {
                        pos = (Point)lvlz.getChildByPath("pos").getData();
                     }

                     if (lt != null && rb != null && pos != null) {
                        footHolds.add(new FieldSkill.FieldFootHold(duration, interval, angleMin, angleMax, attackDelay, z, (short)set, (short)0, new Rectangle(lt.x, rb.y, rb.x - lt.x, rb.y - lt.y), pos, false));
                     }
                  }
               }
            }

            skill.setFootHolds(footHolds);
            this.fieldSkillCache.put(new Pair(skillId, skillLevel), skill);
         }
      }

   }
}
