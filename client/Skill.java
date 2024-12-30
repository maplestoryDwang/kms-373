package client;

import constants.GameConstants;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import provider.MapleData;
import provider.MapleDataTool;
import server.Randomizer;
import server.SecondaryStatEffect;
import server.life.Element;
import tools.Pair;
import tools.StringUtil;

public class Skill {
   private String name = "";
   private String psdDamR = "";
   private String desc = "";
   private final List<SecondaryStatEffect> effects = new ArrayList();
   private List<SecondaryStatEffect> pvpEffects = null;
   private List<Integer> animation = null;
   private List<Integer> psdSkills = new ArrayList();
   private final List<Pair<String, Integer>> requiredSkill = new ArrayList();
   private Element element;
   private int id;
   private int animationTime;
   private int type;
   private int masterLevel;
   private int maxLevel;
   private int delay;
   private int trueMax;
   private int eventTamingMob;
   private int skillType;
   private int psd;
   private int weaponIdx;
   private int finalAttackId;
   private int vehicleID;
   private int categoryIndex;
   private boolean invisible;
   private boolean chargeskill;
   private boolean timeLimited;
   private boolean combatOrders;
   private boolean pvpDisabled;
   private boolean magic;
   private boolean casterMove;
   private boolean pushTarget;
   private boolean pullTarget;
   private boolean hyper;
   private boolean chainAttack;
   private boolean finalAttack;
   private boolean notCooltimeReset;
   private boolean vSkill;
   private boolean notIncBuffDuration;
   private boolean encode4Byte;
   private boolean ignoreCounter;
   private List<Integer> skillList;
   private List<Integer> skillList2;
   private List<SecondAtom2> secondAtoms;
   private List<RandomSkillEntry> randomSkills;

   public Skill(int id) {
      this.element = Element.NEUTRAL;
      this.animationTime = 0;
      this.type = 0;
      this.masterLevel = 0;
      this.maxLevel = 0;
      this.delay = 0;
      this.trueMax = 0;
      this.eventTamingMob = 0;
      this.skillType = 0;
      this.psd = 0;
      this.weaponIdx = 0;
      this.finalAttackId = 0;
      this.vehicleID = 0;
      this.categoryIndex = -1;
      this.invisible = false;
      this.chargeskill = false;
      this.timeLimited = false;
      this.combatOrders = false;
      this.pvpDisabled = false;
      this.magic = false;
      this.casterMove = false;
      this.pushTarget = false;
      this.pullTarget = false;
      this.hyper = false;
      this.chainAttack = false;
      this.finalAttack = false;
      this.notCooltimeReset = false;
      this.vSkill = false;
      this.notIncBuffDuration = false;
      this.encode4Byte = false;
      this.ignoreCounter = false;
      this.skillList = new ArrayList();
      this.skillList2 = new ArrayList();
      this.secondAtoms = new ArrayList();
      this.randomSkills = new ArrayList();
      this.id = id;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setDesc(String desc) {
      this.desc = desc;
   }

   public String getDesc() {
      return this.desc;
   }

   public int getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public static final Skill loadFromData(int id, MapleData data, MapleData delayData) {
      Skill ret = new Skill(id);
      boolean isBuff = false;
      int skillType = MapleDataTool.getInt("skillType", data, -1);
      String elem = MapleDataTool.getString("elemAttr", data, (String)null);
      if (elem != null) {
         ret.element = Element.getFromChar(elem.charAt(0));
      }

      ret.skillType = skillType;
      ret.invisible = MapleDataTool.getInt("invisible", data, 0) > 0;
      ret.timeLimited = MapleDataTool.getInt("timeLimited", data, 0) > 0;
      ret.combatOrders = MapleDataTool.getInt("combatOrders", data, 0) > 0;
      ret.masterLevel = MapleDataTool.getInt("masterLevel", data, 0);
      ret.vehicleID = MapleDataTool.getInt("vehicleID", data, 0);
      ret.hyper = data.getChildByPath("hyper") != null;
      ret.vSkill = data.getChildByPath("vSkill") != null;
      ret.categoryIndex = MapleDataTool.getInt("categoryIndex", data, 0);
      MapleData additional_process = data.getChildByPath("additional_process");
      MapleData inf;
      int pskill;
      if (additional_process != null) {
         Iterator var8 = additional_process.getChildren().iterator();

         while(var8.hasNext()) {
            inf = (MapleData)var8.next();
            pskill = MapleDataTool.getInt(inf.getName(), additional_process, 0);
            if (pskill == 11) {
               ret.encode4Byte = MapleDataTool.getInt("processtype", data, 0) != 0;
               break;
            }
         }
      }

      ret.psd = MapleDataTool.getInt("psd", data, 0);
      MapleData psdskill = data.getChildByPath("psdSkill");
      MapleData info2;
      if (psdskill != null) {
         Iterator var30 = data.getChildByPath("psdSkill").getChildren().iterator();

         while(var30.hasNext()) {
            info2 = (MapleData)var30.next();
            ret.psdSkills.add(Integer.parseInt(info2.getName()));
         }

         var30 = ret.psdSkills.iterator();

         while(var30.hasNext()) {
            pskill = (Integer)var30.next();
            Skill skil = SkillFactory.getSkill(pskill);
            if (skil != null) {
               skil.getPsdSkills().add(id);
            }
         }
      }

      if (id == 22140000 || id == 22141002) {
         ret.masterLevel = 5;
      }

      ret.notCooltimeReset = data.getChildByPath("notCooltimeReset") != null;
      ret.notIncBuffDuration = data.getChildByPath("notIncBuffDuration") != null;
      ret.eventTamingMob = MapleDataTool.getInt("eventTamingMob", data, 0);
      inf = data.getChildByPath("info");
      if (inf != null) {
         ret.type = MapleDataTool.getInt("type", inf, 0);
         ret.pvpDisabled = MapleDataTool.getInt("pvp", inf, 1) <= 0;
         ret.magic = MapleDataTool.getInt("magicDamage", inf, 0) > 0;
         ret.casterMove = MapleDataTool.getInt("casterMove", inf, 0) > 0;
         ret.pushTarget = MapleDataTool.getInt("pushTarget", inf, 0) > 0;
         ret.pullTarget = MapleDataTool.getInt("pullTarget", inf, 0) > 0;
         ret.chainAttack = MapleDataTool.getInt("chainAttack", inf, 0) > 0;
         ret.finalAttack = MapleDataTool.getInt("finalAttack", inf, 0) > 0;
      }

      info2 = data.getChildByPath("info2");
      if (inf != null) {
         ret.ignoreCounter = MapleDataTool.getInt("ignoreCounter", info2, 0) > 0;
      }

      MapleData SecondAtom = data.getChildByPath("SecondAtom");
      MapleData listinfo2;
      Iterator var13;
      MapleData f_Data;
      if (SecondAtom != null) {
         listinfo2 = SecondAtom.getChildByPath("atom");
         if (listinfo2 != null) {
            var13 = listinfo2.getChildren().iterator();

            while(var13.hasNext()) {
               f_Data = (MapleData)var13.next();
               ret.secondAtoms.add(parseSecondAtom(f_Data, id));
            }
         } else {
            ret.secondAtoms.add(parseSecondAtom(SecondAtom, id));
         }
      }

      listinfo2 = data.getChildByPath("skillList");
      if (listinfo2 != null) {
         var13 = listinfo2.getChildren().iterator();

         while(var13.hasNext()) {
            f_Data = (MapleData)var13.next();
            ret.skillList.add(MapleDataTool.getInt(f_Data.getName(), listinfo2, -1));
         }
      }

      MapleData listinfo3 = data.getChildByPath("skillList2");
      MapleData effect;
      if (listinfo3 != null) {
         Iterator var34 = listinfo3.getChildren().iterator();

         while(var34.hasNext()) {
            effect = (MapleData)var34.next();
            ret.skillList2.add(MapleDataTool.getInt(effect.getName(), listinfo3, -1));
         }
      }

      ret.weaponIdx = MapleDataTool.getInt("weapon", data, 0);
      f_Data = data.getChildByPath("finalAttack");
      MapleData action_;
      int i;
      if (f_Data != null) {
         Iterator var35 = f_Data.getChildren().iterator();

         while(var35.hasNext()) {
            action_ = (MapleData)var35.next();
            i = Integer.parseInt(action_.getName());
            if (i > 0) {
               ret.finalAttackId = i;
               break;
            }
         }
      }

      effect = data.getChildByPath("effect");
      MapleData reqDataRoot;
      MapleData rs;
      int skillid;
      MapleData level2;
      if (skillType == 1) {
         isBuff = false;
      } else if (skillType == 2) {
         isBuff = true;
      } else if (skillType == 3) {
         (ret.animation = new ArrayList()).add(0);
         isBuff = effect != null;
      } else {
         action_ = data.getChildByPath("action");
         level2 = data.getChildByPath("hit");
         reqDataRoot = data.getChildByPath("ball");
         boolean action = false;
         if (action_ == null && data.getChildByPath("prepare/action") != null) {
            action_ = data.getChildByPath("prepare/action");
            action = true;
         }

         isBuff = effect != null && level2 == null && reqDataRoot == null;
         if (action_ != null) {
            String d3 = null;
            if (action) {
               d3 = MapleDataTool.getString((MapleData)action_, (String)null);
            } else {
               d3 = MapleDataTool.getString("0", action_, (String)null);
            }

            if (d3 != null) {
               isBuff |= d3.equals("alert2");
               rs = delayData.getChildByPath(d3);
               Iterator var22;
               MapleData ddc;
               if (rs != null) {
                  for(var22 = rs.iterator(); var22.hasNext(); ret.delay += skillid) {
                     ddc = (MapleData)var22.next();
                     skillid = Math.abs(MapleDataTool.getInt("delay", ddc, 0));
                     if (skillid < 0) {
                        skillid *= -1;
                     }
                  }
               }

               if (SkillFactory.getDelay(d3) != null) {
                  (ret.animation = new ArrayList()).add(SkillFactory.getDelay(d3));
                  if (!action) {
                     var22 = action_.iterator();

                     while(var22.hasNext()) {
                        ddc = (MapleData)var22.next();
                        if (!MapleDataTool.getString(ddc, d3).equals(d3)) {
                           String c = MapleDataTool.getString(ddc);
                           if (SkillFactory.getDelay(c) != null) {
                              ret.animation.add(SkillFactory.getDelay(c));
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      if (StringUtil.getLeftPaddedStr(String.valueOf(id / 10000), '0', 3).equals("8000")) {
         isBuff = true;
      }

      ret.chargeskill = data.getChildByPath("keydown") != null;
      action_ = data.getChildByPath("common");
      if (action_ != null) {
         ret.maxLevel = MapleDataTool.getInt("maxLevel", action_, 1);
         ret.psdDamR = MapleDataTool.getString("damR", action_, "");
         ret.trueMax = ret.maxLevel + (ret.combatOrders ? 2 : 0);

         for(i = 1; i <= ret.trueMax; ++i) {
            ret.getEffects().add(SecondaryStatEffect.loadSkillEffectFromData(action_, id, isBuff, i, "x"));
         }
      } else {
         Iterator var37 = data.getChildByPath("level").iterator();

         while(var37.hasNext()) {
            reqDataRoot = (MapleData)var37.next();
            ret.getEffects().add(SecondaryStatEffect.loadSkillEffectFromData(reqDataRoot, id, isBuff, Byte.parseByte(reqDataRoot.getName()), (String)null));
         }

         ret.maxLevel = ret.getEffects().size();
         ret.trueMax = ret.getEffects().size();
      }

      level2 = data.getChildByPath("PVPcommon");
      if (level2 != null) {
         ret.pvpEffects = new ArrayList();

         for(int j = 1; j <= ret.trueMax; ++j) {
            ret.pvpEffects.add(SecondaryStatEffect.loadSkillEffectFromData(level2, id, isBuff, j, "x"));
         }
      }

      reqDataRoot = data.getChildByPath("req");
      Iterator var39;
      MapleData effectEntry;
      if (reqDataRoot != null) {
         var39 = reqDataRoot.getChildren().iterator();

         while(var39.hasNext()) {
            effectEntry = (MapleData)var39.next();
            ret.requiredSkill.add(new Pair(effectEntry.getName(), MapleDataTool.getInt(effectEntry, 1)));
         }
      }

      ret.animationTime = 0;
      if (effect != null) {
         for(var39 = effect.iterator(); var39.hasNext(); ret.animationTime += MapleDataTool.getIntConvert("delay", effectEntry, 0)) {
            effectEntry = (MapleData)var39.next();
         }
      }

      MapleData randomSkill = data.getChildByPath("randomSkill");
      if (randomSkill != null) {
         Iterator var42 = randomSkill.getChildren().iterator();

         while(true) {
            MapleData info3;
            do {
               if (!var42.hasNext()) {
                  return ret;
               }

               rs = (MapleData)var42.next();
               info3 = randomSkill.getChildByPath(rs.getName());
            } while(info3 == null);

            List<Pair<Integer, Integer>> skilllist = new ArrayList();
            skillid = MapleDataTool.getInt("skillID", info3, -1);
            int prob = MapleDataTool.getInt("prob", info3, -1);
            MapleData listinfo4 = info3.getChildByPath("skillList");
            if (listinfo4 != null) {
               Iterator var27 = listinfo4.getChildren().iterator();

               while(var27.hasNext()) {
                  MapleData list3 = (MapleData)var27.next();
                  skilllist.add(new Pair(Integer.parseInt(list3.getName()), MapleDataTool.getInt(list3.getName(), listinfo4, -1)));
               }
            }

            ret.randomSkills.add(new RandomSkillEntry(skillid, prob, skilllist));
         }
      } else {
         return ret;
      }
   }

   public SecondaryStatEffect getEffect(int level) {
      if (this.getEffects().size() < level) {
         return this.getEffects().size() > 0 ? (SecondaryStatEffect)this.getEffects().get(this.getEffects().size() - 1) : null;
      } else {
         return level <= 0 ? (SecondaryStatEffect)this.getEffects().get(0) : (SecondaryStatEffect)this.getEffects().get(level - 1);
      }
   }

   public SecondaryStatEffect getPVPEffect(int level) {
      if (this.pvpEffects == null) {
         return this.getEffect(level);
      } else if (this.pvpEffects.size() < level) {
         return this.pvpEffects.size() > 0 ? (SecondaryStatEffect)this.pvpEffects.get(this.pvpEffects.size() - 1) : null;
      } else {
         return level <= 0 ? (SecondaryStatEffect)this.pvpEffects.get(0) : (SecondaryStatEffect)this.pvpEffects.get(level - 1);
      }
   }

   public int getSkillType() {
      return this.skillType;
   }

   public List<Integer> getAllAnimation() {
      return this.animation;
   }

   public int getAnimation() {
      return this.animation == null ? -1 : (Integer)this.animation.get(Randomizer.nextInt(this.animation.size()));
   }

   public boolean isPVPDisabled() {
      return this.pvpDisabled;
   }

   public boolean isChargeSkill() {
      return this.chargeskill;
   }

   public boolean isInvisible() {
      return this.invisible;
   }

   public boolean isEncode4Byte() {
      return this.encode4Byte;
   }

   public boolean hasRequiredSkill() {
      return this.requiredSkill.size() > 0;
   }

   public List<Pair<String, Integer>> getRequiredSkills() {
      return this.requiredSkill;
   }

   public int getMaxLevel() {
      return this.maxLevel;
   }

   public int getTrueMax() {
      return this.trueMax;
   }

   public boolean combatOrders() {
      return this.combatOrders;
   }

   public boolean canBeLearnedBy(MapleCharacter chr) {
      short jid;
      short job = (short)(jid = chr.getJob());
      int skillForJob = this.id / 10000;
      if (skillForJob == 2001) {
         return GameConstants.isEvan(job);
      } else if (chr.getSubcategory() == 1) {
         return GameConstants.isDualBlade(job);
      } else if (chr.getSubcategory() == 2) {
         return GameConstants.isCannon(job);
      } else if (skillForJob == 0) {
         return GameConstants.isAdventurer(job);
      } else if (skillForJob == 1000) {
         return GameConstants.isKOC(job);
      } else if (skillForJob == 2000) {
         return GameConstants.isAran(job);
      } else if (skillForJob == 3000) {
         return GameConstants.isResist(job);
      } else if (skillForJob == 3001) {
         return GameConstants.isDemonSlayer(job);
      } else if (skillForJob == 2002) {
         return GameConstants.isMercedes(job);
      } else {
         return jid / 100 == skillForJob / 100 && jid / 1000 == skillForJob / 1000 && (!GameConstants.isCannon(skillForJob) || GameConstants.isCannon(job)) && (!GameConstants.isDemonSlayer(skillForJob) || GameConstants.isDemonSlayer(job)) && (!GameConstants.isAdventurer(skillForJob) || GameConstants.isAdventurer(job)) && (!GameConstants.isKOC(skillForJob) || GameConstants.isKOC(job)) && (!GameConstants.isAran(skillForJob) || GameConstants.isAran(job)) && (!GameConstants.isEvan(skillForJob) || GameConstants.isEvan(job)) && (!GameConstants.isMercedes(skillForJob) || GameConstants.isMercedes(job)) && (!GameConstants.isResist(skillForJob) || GameConstants.isResist(job)) && (jid / 10 % 10 != 0 || skillForJob / 10 % 10 <= jid / 10 % 10) && (skillForJob / 10 % 10 == 0 || skillForJob / 10 % 10 == jid / 10 % 10) && skillForJob % 10 <= jid % 10;
      }
   }

   public boolean isTimeLimited() {
      return this.timeLimited;
   }

   public boolean sub_4FD900(int a1) {
      boolean v1;
      if (a1 <= 5320007) {
         if (a1 == 5320007) {
            return true;
         }

         if (a1 > 4210012) {
            if (a1 > 5220012) {
               if (a1 == 5220014) {
                  return true;
               }

               v1 = a1 == 5221022;
            } else {
               if (a1 == 5220012) {
                  return true;
               }

               if (a1 > 4340012) {
                  return a1 >= 5120011 && a1 <= 5120012;
               }

               if (a1 == 4340012) {
                  return true;
               }

               v1 = a1 == 4340010;
            }
         } else {
            if (a1 == 4210012) {
               return true;
            }

            if (a1 > 2221009) {
               if (a1 == 2321010 || a1 == 3210015) {
                  return true;
               }

               v1 = a1 == 4110012;
            } else {
               if (a1 == 2221009 || a1 == 1120012 || a1 == 1320011) {
                  return true;
               }

               v1 = a1 == 2121009;
            }
         }
      }

      if (a1 > 23120011) {
         if (a1 > 35120014) {
            if (a1 == 51120000) {
               return true;
            }

            v1 = a1 == 80001913;
         } else {
            if (a1 == 35120014 || a1 == 23120013 || a1 == 23121008) {
               return true;
            }

            v1 = a1 == 33120010;
         }

         return v1;
      } else if (a1 == 23120011) {
         return true;
      } else if (a1 <= 21120014) {
         if (a1 != 21120014 && a1 != 5321004 && a1 - 5321003 + 1 != 2) {
            v1 = a1 - 5321003 + 1 - 2 == 15799005;
            return v1;
         } else {
            return true;
         }
      } else if (a1 > 21121008) {
         v1 = a1 == 22171069;
         return v1;
      } else {
         return a1 == 21121008 || a1 >= 21120020 && a1 <= 21120021;
      }
   }

   public boolean sub_4FDA20(int a1) {
      boolean result = false;
      if (a1 - 92000000 >= 1000000 || a1 % 10000 != 0) {
         int v1 = 10000 * (a1 / 10000);
         if (v1 - 92000000 < 1000000 && v1 % 10000 == 0) {
            result = true;
         }
      }

      return result;
   }

   public boolean sub_4FD870(int a1) {
      int v1 = a1 / 10000;
      if (a1 / 10000 == 8000) {
         v1 = a1 / 100;
      }

      return v1 - 800000 <= 99;
   }

   public boolean sub_48AEF0(int a1) {
      int v1 = a1 / 10000;
      if (a1 / 10000 == 8000) {
         v1 = a1 / 100;
      }

      boolean result = v1 - '鱀' > 5 && this.sub_48A360(v1);
      return result;
   }

   public boolean sub_48A360(int a1) {
      boolean v2;
      if (a1 > 6001) {
         if (a1 == 13000) {
            return true;
         }

         v2 = a1 == 14000;
      } else {
         if (a1 >= 6000) {
            return true;
         }

         if (a1 <= 3002) {
            if (a1 >= 3001 || a1 >= 2001 && a1 <= 2005) {
               return true;
            }

            if (a1 - '鱀' <= 5) {
               return false;
            }

            if (a1 % 1000 == 0) {
               return true;
            }
         }

         v2 = a1 == 5000;
      }

      return v2 || a1 - 800000 < 100;
   }

   public boolean sub_4FD8B0(int a1) {
      boolean result;
      if (a1 >= 0) {
         int v1 = a1 / 10000;
         if (a1 / 10000 == 8000) {
            v1 = a1 / 100;
         }

         result = v1 == 9500;
      } else {
         result = false;
      }

      return result;
   }

   public int sub_48A160(int a1) {
      int result = a1 / 10000;
      if (a1 / 10000 == 8000) {
         result = a1 / 100;
      }

      return result;
   }

   public int sub_489A10(int a1) {
      int result = false;
      int result;
      if (!this.sub_48A360(a1) && a1 % 100 != 0 && a1 != 501 && a1 != 3101) {
         if (a1 - 2200 >= 100 && a1 != 2001) {
            if (a1 / 10 == 43) {
               result = 0;
               if ((a1 - 430) / 2 <= 2) {
                  result = (a1 - 430) / 2 + 2;
               }
            } else {
               result = 0;
               if (a1 % 10 <= 2) {
                  result = a1 % 10 + 2;
               }
            }
         } else {
            switch(a1) {
            case 2200:
            case 2210:
               result = 1;
               break;
            case 2201:
            case 2202:
            case 2203:
            case 2204:
            case 2205:
            case 2206:
            case 2207:
            case 2208:
            case 2209:
            default:
               result = 0;
               break;
            case 2211:
            case 2212:
            case 2213:
               result = 2;
               break;
            case 2214:
            case 2215:
            case 2216:
               result = 3;
               break;
            case 2217:
            case 2218:
               result = 4;
            }
         }
      } else {
         result = 1;
      }

      return result;
   }

   public boolean sub_4FD7F0(int a1) {
      boolean v1;
      if (a1 > 101100101) {
         if (a1 > 101110203) {
            if (a1 == 101120104) {
               return true;
            }

            v1 = a1 - 101120104 == 100;
         } else {
            if (a1 == 101110203 || a1 == 101100201 || a1 == 101110102) {
               return true;
            }

            v1 = a1 - 101110102 == 98;
         }
      } else {
         if (a1 == 101100101) {
            return true;
         }

         if (a1 > 4331002) {
            if (a1 == 4340007 || a1 == 4341004) {
               return true;
            }

            v1 = a1 == 101000101;
         } else {
            if (a1 == 4331002 || a1 == 4311003 || a1 == 4321006) {
               return true;
            }

            v1 = a1 == 4330009;
         }
      }

      return v1;
   }

   public boolean isFourthJob() {
      int a1 = this.id;
      boolean result;
      if (!this.sub_4FD900(a1) && (a1 - 92000000 >= 1000000 || a1 % 10000 != 0) && !this.sub_4FDA20(a1) && !this.sub_4FD870(a1) && !this.sub_48AEF0(a1) && !this.sub_4FD8B0(a1)) {
         int v2 = this.sub_48A160(a1);
         int v3 = this.sub_489A10(v2);
         result = v2 - '鱀' > 5 && (this.sub_4FD7F0(a1) || v3 == 4 && !GameConstants.isZero(v2));
      } else {
         result = false;
      }

      return result;
   }

   public Element getElement() {
      return this.element;
   }

   public int getAnimationTime() {
      return this.animationTime;
   }

   public int getMasterLevel() {
      return this.masterLevel;
   }

   public int getDelay() {
      return this.delay;
   }

   public int getTamingMob() {
      return this.eventTamingMob;
   }

   public boolean isBeginnerSkill() {
      int jobId = this.id / 10000;
      return GameConstants.isBeginnerJob(jobId);
   }

   public boolean isMagic() {
      return this.magic;
   }

   public boolean isMovement() {
      return this.casterMove;
   }

   public boolean isPush() {
      return this.pushTarget;
   }

   public boolean isPull() {
      return this.pullTarget;
   }

   public int getPsd() {
      return this.psd;
   }

   public String getPsdDamR() {
      return this.psdDamR;
   }

   public boolean isHyper() {
      return this.hyper;
   }

   public boolean isVMatrix() {
      return this.vSkill;
   }

   public boolean isNotCooltimeReset() {
      return this.notCooltimeReset;
   }

   public boolean isNotIncBuffDuration() {
      return this.notIncBuffDuration;
   }

   public boolean isSpecialSkill() {
      int jobId = this.id / 10000;
      return jobId == 900 || jobId == 800 || jobId == 9000 || jobId == 9200 || jobId == 9201 || jobId == 9202 || jobId == 9203 || jobId == 9204;
   }

   public List<SecondaryStatEffect> getEffects() {
      return this.effects;
   }

   public int getType() {
      return this.type;
   }

   public void setType(int type) {
      this.type = type;
   }

   public List<Integer> getPsdSkills() {
      return this.psdSkills;
   }

   public void setPsdSkills(List<Integer> psdSkills) {
      this.psdSkills = psdSkills;
   }

   public boolean isChainAttack() {
      return this.chainAttack;
   }

   public void setChainAttack(boolean chainAttack) {
      this.chainAttack = chainAttack;
   }

   public boolean isFinalAttack() {
      return this.finalAttack;
   }

   public List<Integer> getSkillList() {
      return this.skillList;
   }

   public List<Integer> getSkillList2() {
      return this.skillList2;
   }

   public boolean isIgnoreCounter() {
      return this.ignoreCounter;
   }

   public static SecondAtom2 parseSecondAtom(MapleData d, int id) {
      List<Point> aExtraPos = new ArrayList();
      List<Integer> aCustom = new ArrayList();
      int dataIndex = MapleDataTool.getInt("dataIndex", d, 0);
      int createDelay = MapleDataTool.getInt("createDelay", d, 0);
      int enableDelay = MapleDataTool.getInt("enableDelay", d, 0);
      int expire = MapleDataTool.getInt("expire", d, 0);
      int attackableCount = MapleDataTool.getInt("attackableCount", d, 1);
      Point pos = MapleDataTool.getPoint("pos", d, new Point(0, 0));
      int rotate = MapleDataTool.getInt("rotate", d, 0);
      int localOnly = MapleDataTool.getInt("localOnly", d, 0);
      MapleData extraPos = d.getChildByPath("extraPos");
      if (extraPos != null) {
         Iterator var13 = extraPos.getChildren().iterator();

         while(var13.hasNext()) {
            MapleData ep = (MapleData)var13.next();
            aExtraPos.add(MapleDataTool.getPoint(ep));
         }
      }

      MapleData custom = d.getChildByPath("custom");
      if (custom != null) {
         Iterator var17 = custom.getChildren().iterator();

         while(var17.hasNext()) {
            MapleData c = (MapleData)var17.next();
            aCustom.add(MapleDataTool.getInt(c));
         }
      }

      return new SecondAtom2(dataIndex, 0, createDelay, enableDelay, expire, 0, attackableCount, pos, rotate, aExtraPos, aCustom, localOnly, id);
   }

   public List<SecondAtom2> getSecondAtoms() {
      return this.secondAtoms;
   }

   public void setSecondAtoms(List<SecondAtom2> secondAtoms) {
      this.secondAtoms = secondAtoms;
   }

   public int getFinalAttackIdx() {
      return this.finalAttackId;
   }

   public int getWeaponIdx() {
      return this.weaponIdx;
   }

   public List<RandomSkillEntry> getRSE() {
      return this.randomSkills;
   }

   public int getVehicleID() {
      return this.vehicleID;
   }

   public int getCategoryIndex() {
      return this.categoryIndex;
   }

   public void setCategoryIndex(int categoryIndex) {
      this.categoryIndex = categoryIndex;
   }
}
