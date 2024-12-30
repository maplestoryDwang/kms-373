package client;

import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MapleWeaponType;
import constants.GameConstants;
import handling.channel.handler.UnionHandler;
import java.awt.Point;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.SecondaryStatEffect;
import server.StructSetItem;
import server.life.Element;
import server.life.MapleMonster;
import tools.Pair;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class PlayerStats implements Serializable {
   private static final long serialVersionUID = -679541993413738569L;
   private final Map<Integer, Integer> setHandling = new ConcurrentHashMap();
   private final Map<Integer, Integer> skillsIncrement = new HashMap();
   private final Map<Integer, Integer> damageIncrease = new HashMap();
   private final Map<Integer, Integer> IgIncrease = new HashMap();
   private final Map<Integer, Integer> BossDamIncrease = new HashMap();
   private Map<String, Integer> ApplyStatFinalDamage = new HashMap();
   private List<Integer> ignoreMobpdpR = new ArrayList();
   private List<Integer> FinalDamage = new ArrayList();
   private EnumMap<Element, Integer> elemBoosts = new EnumMap(Element.class);
   private transient WeakReference<MapleCharacter> chr;
   private transient Map<Integer, Integer> demonForce = new HashMap();
   private List<Equip> durabilityHandling = new ArrayList();
   private List<Equip> equipLevelHandling = new ArrayList();
   private List<Triple<Integer, String, Integer>> psdSkills = new ArrayList();
   private transient float shouldHealHP;
   private transient float shouldHealMP;
   public short str;
   public short dex;
   public short luk;
   public short int_;
   public long hp = 50L;
   public long maxhp = 50L;
   public long mp = 5L;
   public long maxmp = 5L;
   public transient short critical_rate;
   public transient short critical_damage;
   private transient byte passive_mastery;
   private transient int localstr;
   private transient int localdex;
   private transient int localluk;
   private transient int localint_;
   private transient int ms_maxhp;
   private transient int ms_maxmp;
   private transient int Nlocalstr;
   private transient int Nlocaldex;
   private transient int Nlocalint;
   private transient int Nlocalluk;
   private transient int Nlocalhp;
   private transient int Nlocalmp;
   private transient long localmaxhp;
   private transient long localmaxmp;
   private transient long shp;
   private transient int magic;
   private transient int watk;
   private transient int hands;
   private transient int accuracy;
   private transient int attackSpeed;
   public transient boolean equippedWelcomeBackRing;
   public transient boolean hasClone;
   public transient boolean hasPartyBonus;
   public transient boolean Berserk;
   public transient double expBuff;
   public transient double expBuffZero;
   public transient double expBuffUnion;
   public transient double dropBuff;
   public transient double mesoBuff;
   public transient double cashBuff;
   public transient double MesoGuard;
   public transient double MesoGuardMeso;
   public transient double expMod;
   public transient double pickupRange;
   public double dam_r;
   public double bossdam_r;
   public transient int recoverHP;
   public transient int recoverMP;
   public transient int mpconReduce;
   public transient int mpconPercent;
   public transient int incMesoProp;
   public transient int coolTimeR;
   public transient int suddenDeathR;
   public transient int expLossReduceR;
   public transient int DAMreflect;
   public transient int DAMreflect_rate;
   public transient int ignoreDAMr;
   public transient int ignoreDAMr_rate;
   public transient int ignoreDAM;
   public transient int ignoreDAM_rate;
   public transient int mpRestore;
   public transient int hpRecover;
   public transient int hpRecoverProp;
   public transient int hpRecoverPercent;
   public transient int mpRecover;
   public transient int mpRecoverProp;
   public transient int RecoveryUP;
   public transient int BuffUP;
   public transient int RecoveryUP_Skill;
   public transient int BuffUP_Skill;
   public transient int incAllskill;
   public transient int combatOrders;
   public transient int ignoreTargetDEF;
   public transient int defRange;
   public transient int BuffUP_Summon;
   public transient int dodgeChance;
   public transient int speed;
   public transient int jump;
   public transient int harvestingTool;
   public transient int evaR;
   public transient int equipmentBonusExp;
   public transient int dropMod;
   public transient int cashMod;
   public transient int levelBonus;
   public transient int ASR;
   public transient int TER;
   public transient int pickRate;
   public transient int decreaseDebuff;
   public transient int equippedFairy;
   public transient int equippedSummon;
   public transient int percent_hp;
   public transient int before_maxhp;
   public transient int percent_mp;
   public transient int before_percent_mp;
   public transient int before_maxmp;
   public transient int multi_lateral_hp;
   public transient int multi_lateral_mp;
   public transient int percent_str;
   public transient int percent_dex;
   public transient int percent_int;
   public transient int percent_luk;
   public transient int percent_acc;
   public transient int percent_atk;
   public transient int percent_matk;
   public transient int percent_wdef;
   public transient int percent_mdef;
   public transient int pvpDamage;
   public transient int hpRecoverTime = 0;
   public transient int mpRecoverTime = 0;
   public transient int dot;
   public transient int dotTime;
   public transient int questBonus;
   public transient int pvpRank;
   public transient int pvpExp;
   public transient int wdef;
   public transient int mdef;
   public transient int trueMastery;
   public transient int damX;
   public transient int DAMreduceR;
   public transient int randCooldown;
   public transient int stance;
   public transient int ppd;
   public transient int damAbsorbShieldR;
   public transient int arc;
   public transient long fixHp;
   private transient float localmaxbasedamage;
   private transient float localmaxbasepvpdamage;
   private transient float localmaxbasepvpdamageL;
   public transient int def;
   public transient int element_ice;
   public transient int element_fire;
   public transient int element_light;
   public transient int element_psn;
   private double sword;
   private double blunt;
   private double axe;
   private double spear;
   private double polearm;
   private double claw;
   private double dagger;
   private double staffwand;
   private double CROSSBOW;
   private double bow;
   private int skill = 0;
   private int BossDamage;
   private int NomarbdR;
   private int starforce;
   private double DamagePercent;
   private double WatkPercent;
   private double MatkPercent;
   private double Mastery;
   private Skill skil;
   private ReentrantLock lock = new ReentrantLock();
   public transient int reduceCooltime;
   private static final int[] allJobs = new int[]{0, 10000, 10000000, 20000000, 20010000, 20020000, 30000000, 30010000};
   public static final int[] pvpSkills = new int[]{1000007, 2000007, 3000006, 4000010, 5000006, 5010004, 11000006, 12000006, 13000005, 14000006, 15000005, 21000005, 22000002, 23000004, 31000005, 32000012, 33000004, 35000005};

   public final void init(MapleCharacter chra) {
      this.recalcLocalStats(chra);
   }

   public final short getStr() {
      return this.str;
   }

   public final short getDex() {
      return this.dex;
   }

   public final short getLuk() {
      return this.luk;
   }

   public final short getInt() {
      return this.int_;
   }

   public final void setStr(short str, MapleCharacter chra) {
      this.str = str;
      this.recalcLocalStats(chra);
   }

   public final void setDex(short dex, MapleCharacter chra) {
      this.dex = dex;
      this.recalcLocalStats(chra);
   }

   public final void setLuk(short luk, MapleCharacter chra) {
      this.luk = luk;
      this.recalcLocalStats(chra);
   }

   public final void setInt(short int_, MapleCharacter chra) {
      this.int_ = int_;
      this.recalcLocalStats(chra);
   }

   public final boolean setHp(long newhp, MapleCharacter chra) {
      return this.setHp(newhp, false, chra, false);
   }

   public final boolean setHp(long newhp, MapleCharacter chra, boolean igskill) {
      return this.setHp(newhp, false, chra, igskill);
   }

   public final boolean setHp(long newhp, boolean silent, MapleCharacter chra, boolean igskill) {
      long oldHp = this.hp;
      long thp = newhp;
      if (newhp < 0L) {
         thp = 0L;
      }

      if (thp > this.hp && this.hp > 0L && chra.getBuffedEffect(SecondaryStat.DebuffIncHp) != null) {
         return false;
      } else {
         if (thp > this.localmaxhp) {
            thp = this.localmaxhp;
         }

         if (chra.getBattleGroundChr() != null) {
            this.hp = thp;
            if (thp > (long)chra.getBattleGroundChr().getMaxHp()) {
               this.hp = (long)chra.getBattleGroundChr().getMaxHp();
            }

            return true;
         } else {
            this.hp = thp;
            if (chra != null) {
               if (!silent) {
                  chra.updatePartyMemberHP();
               }

               if (!igskill && GameConstants.isDemonAvenger(chra.getJob()) && chra.getBuffedValue(400011112) && this.hp <= 0L) {
                  long duration = chra.getBuffLimit(400011112);
                  this.hp = 1L;
                  chra.setSkillCustomInfo(400011112, (long)((int)(chra.getSkillCustomValue0(400011112) + newhp * -1L)), 0L);
                  if (chra.getSkillCustomValue0(400011112) >= this.localmaxhp * 2L) {
                     chra.setSkillCustomInfo(400011112, (long)((int)(this.localmaxhp * 2L)), 0L);
                  }

                  HashMap<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
                  statups.put(SecondaryStat.Revenant, new Pair(1, (int)duration));
                  chra.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, chra.getBuffedEffect(SecondaryStat.Revenant), chra));
                  chra.updateSingleStat(MapleStat.HP, this.hp);
                  return true;
               }

               if (oldHp > this.hp && !chra.isAlive()) {
                  if (!igskill) {
                     SecondaryStatEffect effect;
                     if (!chra.skillisCooling(1320016) && !chra.skillisCooling(1320019) && chra.getBuffedValue(1321020) && chra.getSkillLevel(1320016) > 0) {
                        effect = SkillFactory.getSkill(1320019).getEffect(chra.getSkillLevel(1320016));
                        effect.applyTo(chra, true);
                        chra.addCooldown(1320019, System.currentTimeMillis(), (long)effect.getCooldown(chra));
                        chra.getClient().getSession().writeAndFlush(CField.skillCooldown(1320019, effect.getCooldown(chra)));
                        chra.getStat().heal(chra);
                     } else if (chra.getBuffedEffect(SecondaryStat.HeavensDoor) != null) {
                        if (chra.getBuffedEffect(SecondaryStat.Lotus) != null) {
                           chra.Lotus = true;
                        }

                        chra.cancelEffect(chra.getBuffedEffect(SecondaryStat.HeavensDoor));
                        this.hp = this.localmaxhp;
                     } else if (chra.getBuffedEffect(SecondaryStat.FlareTrick) != null) {
                        this.hp = chra.getStat().getCurrentMaxHp() / 2L;
                        chra.cancelEffect(chra.getBuffedEffect(SecondaryStat.FlareTrick));
                        SkillFactory.getSkill(12111023).getEffect(chra.getSkillLevel(12111023)).applyTo(chra, false);
                        chra.updateSingleStat(MapleStat.HP, this.hp);
                     } else if (chra.getBuffedEffect(SecondaryStat.ReviveOnce) != null) {
                        if (chra.getBuffedEffect(SecondaryStat.ReviveOnce).getSourceId() == 24111002) {
                           chra.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chra, chra.getBuffSource(SecondaryStat.ReviveOnce), chra.getBuffSource(SecondaryStat.ReviveOnce), 1, 0, 0, (byte)0, true, (Point)null, (String)null, (Item)null));
                           chra.getMap().broadcastMessage(chra, CField.EffectPacket.showEffect(chra, chra.getBuffSource(SecondaryStat.ReviveOnce), chra.getBuffSource(SecondaryStat.ReviveOnce), 1, 0, 0, (byte)0, false, (Point)null, (String)null, (Item)null), false);
                        } else if (chra.getJob() == 1412) {
                           chra.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chra, 3000, 14111030, 62, 0, 0, (byte)(chra.isFacingLeft() ? 1 : 0), true, chra.getTruePosition(), (String)null, (Item)null));
                           chra.getMap().broadcastMessage(chra, CField.EffectPacket.showEffect(chra, 3000, 14111030, 62, 0, 0, (byte)(chra.isFacingLeft() ? 1 : 0), false, chra.getTruePosition(), (String)null, (Item)null), false);
                        }

                        this.hp = chra.getStat().getCurrentMaxHp() / 2L;
                        chra.updateSingleStat(MapleStat.HP, this.hp);
                        effect = chra.getBuffedEffect(SecondaryStat.ReviveOnce);
                        chra.cancelEffect(chra.getBuffedEffect(SecondaryStat.ReviveOnce));
                        chra.addCooldown(effect.getSourceId(), System.currentTimeMillis(), (long)effect.getCooldown(chra));
                        chra.getClient().getSession().writeAndFlush(CField.skillCooldown(effect.getSourceId(), effect.getCooldown(chra)));
                        effect.applyTo(chra, false);
                     } else if (chra.getBuffedEffect(SecondaryStat.PreReviveOnce) != null) {
                        if (chra.getBuffedEffect(SecondaryStat.PreReviveOnce).makeChanceResult()) {
                           this.hp = this.localmaxhp;
                        } else {
                           chra.updateSingleStat(MapleStat.HP, this.hp);
                           chra.playerDead();
                        }
                     } else {
                        chra.updateSingleStat(MapleStat.HP, this.hp);
                        chra.playerDead();
                     }
                  } else {
                     chra.updateSingleStat(MapleStat.HP, this.hp);
                     chra.playerDead();
                  }
               }
            }

            if (GameConstants.isDemonAvenger(chra.getJob())) {
               EnumMap<SecondaryStat, Pair<Integer, Integer>> statups = new EnumMap(SecondaryStat.class);
               statups.put(SecondaryStat.LifeTidal, new Pair(3, 0));
               chra.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, (SecondaryStatEffect)null, chra));
            }

            return this.hp != oldHp;
         }
      }
   }

   public final boolean setMp(long newmp, MapleCharacter chra) {
      long oldMp = this.mp;
      long tmp = newmp;
      if (newmp < 0L) {
         tmp = 0L;
      }

      if (tmp > this.localmaxmp) {
         tmp = this.localmaxmp;
      }

      if (chra.getBattleGroundChr() != null) {
         this.mp = tmp;
         if (tmp > (long)chra.getBattleGroundChr().getMaxMp()) {
            this.mp = (long)chra.getBattleGroundChr().getMaxMp();
         }

         return true;
      } else {
         this.mp = tmp;
         return this.mp != oldMp;
      }
   }

   public final void setInfo(long maxhp, long maxmp, long hp, long mp) {
      this.maxhp = maxhp;
      this.maxmp = maxmp;
      this.hp = hp;
      this.mp = mp;
   }

   public final void setMaxHp(long hp, MapleCharacter chra) {
      this.maxhp = hp;
      this.recalcLocalStats(chra);
   }

   public final void setMaxMp(long mp, MapleCharacter chra) {
      this.maxmp = mp;
      this.recalcLocalStats(chra);
   }

   public final long getHp() {
      return this.hp;
   }

   public final long getMaxHp() {
      return this.maxhp;
   }

   public final long getMp() {
      return this.mp;
   }

   public final long getMaxMp() {
      return this.maxmp;
   }

   public final int getTotalDex() {
      return this.localdex;
   }

   public final int getTotalInt() {
      return this.localint_;
   }

   public final int getTotalStr() {
      return this.localstr;
   }

   public final int getTotalLuk() {
      return this.localluk;
   }

   public final int getTotalMagic() {
      return this.magic;
   }

   public final int getSpeed() {
      return this.speed;
   }

   public final int getJump() {
      return this.jump;
   }

   public final int getTotalWatk() {
      return this.watk;
   }

   public final long getCurrentMaxHp() {
      return this.localmaxhp;
   }

   public final long getCurrentMaxMp(MapleCharacter chr) {
      return GameConstants.isDemonSlayer(chr.getJob()) ? (long)GameConstants.getMPByJob(chr) : this.localmaxmp;
   }

   public final int getHands() {
      return this.hands;
   }

   public final float getCurrentMaxBaseDamage() {
      return this.localmaxbasedamage;
   }

   public final float getCurrentMaxBasePVPDamage() {
      return this.localmaxbasepvpdamage;
   }

   public final float getCurrentMaxBasePVPDamageL() {
      return this.localmaxbasepvpdamageL;
   }

   private void resetLocalStats(int job) {
      this.accuracy = 0;
      this.wdef = 0;
      this.mdef = 0;
      this.damX = 0;
      this.localstr = this.getStr();
      this.localdex = this.getDex();
      this.localint_ = this.getInt();
      this.localluk = this.getLuk();
      this.speed = 100;
      this.jump = 100;
      this.pickupRange = 0.0D;
      this.decreaseDebuff = 0;
      this.ASR = 0;
      this.TER = 0;
      this.dot = 0;
      this.questBonus = 1;
      this.dotTime = 0;
      this.trueMastery = 0;
      this.percent_wdef = 0;
      this.percent_mdef = 0;
      this.percent_hp = 0;
      this.percent_mp = 0;
      this.before_percent_mp = 0;
      this.multi_lateral_hp = 0;
      this.multi_lateral_mp = 0;
      this.percent_str = 0;
      this.percent_dex = 0;
      this.percent_int = 0;
      this.percent_luk = 0;
      this.percent_acc = 0;
      this.percent_atk = 0;
      this.percent_matk = 0;
      this.critical_rate = 5;
      this.critical_damage = 0;
      this.magic = 0;
      this.watk = 0;
      this.evaR = 0;
      this.pvpDamage = 0;
      this.MesoGuard = 50.0D;
      this.MesoGuardMeso = 0.0D;
      this.dam_r = 100.0D;
      this.bossdam_r = 100.0D;
      this.fixHp = 0L;
      this.expBuff = 0.0D;
      this.expBuffZero = 0.0D;
      this.expBuffUnion = 0.0D;
      this.cashBuff = 100.0D;
      this.dropBuff = 100.0D;
      this.mesoBuff = 100.0D;
      this.reduceCooltime = 0;
      this.randCooldown = 0;
      this.recoverHP = 0;
      this.recoverMP = 0;
      this.mpconReduce = 0;
      this.mpconPercent = 100;
      this.incMesoProp = 0;
      this.coolTimeR = 0;
      this.suddenDeathR = 0;
      this.expLossReduceR = 0;
      this.DAMreflect = 0;
      this.DAMreflect_rate = 0;
      this.ignoreDAMr = 0;
      this.ignoreDAMr_rate = 0;
      this.ignoreDAM = 0;
      this.ignoreDAM_rate = 0;
      this.ignoreTargetDEF = 0;
      this.hpRecover = 0;
      this.hpRecoverProp = 0;
      this.hpRecoverPercent = 0;
      this.mpRecover = 0;
      this.mpRecoverProp = 0;
      this.pickRate = 0;
      this.equippedWelcomeBackRing = false;
      this.equippedFairy = 0;
      this.equippedSummon = 0;
      this.hasClone = false;
      this.Berserk = false;
      this.equipmentBonusExp = 0;
      this.RecoveryUP = 0;
      this.BuffUP = 0;
      this.RecoveryUP_Skill = 0;
      this.BuffUP_Skill = 0;
      this.BuffUP_Summon = 0;
      this.dropMod = 1;
      this.expMod = 1.0D;
      this.cashMod = 1;
      this.levelBonus = 0;
      this.incAllskill = 0;
      this.combatOrders = 0;
      this.defRange = this.isRangedJob(job) ? 200 : 0;
      this.durabilityHandling.clear();
      this.equipLevelHandling.clear();
      this.skillsIncrement.clear();
      this.damageIncrease.clear();
      this.IgIncrease.clear();
      this.BossDamIncrease.clear();
      this.setHandling.clear();
      this.harvestingTool = 0;
      this.element_fire = 100;
      this.element_ice = 100;
      this.element_light = 100;
      this.element_psn = 100;
      this.def = 100;
      this.before_maxhp = (int)this.maxhp;
      this.before_maxmp = (int)this.maxmp;
      this.stance = 0;
      this.ppd = 0;
      this.damAbsorbShieldR = 0;
      this.DamagePercent = 0.0D;
      this.WatkPercent = 0.0D;
      this.MatkPercent = 0.0D;
      this.BossDamage = 0;
      this.NomarbdR = 0;
      this.Mastery = 0.0D;
      this.arc = 0;
      this.starforce = 0;
      this.Nlocalstr = 0;
      this.Nlocaldex = 0;
      this.Nlocalint = 0;
      this.Nlocalluk = 0;
      this.Nlocalhp = 0;
      this.Nlocalmp = 0;
      this.attackSpeed = 0;
      this.shp = 0L;
      this.FinalDamage.clear();
      this.ignoreMobpdpR.clear();
      this.ApplyStatFinalDamage.clear();
   }

   public void recalcLocalStats(MapleCharacter chra) {
      this.recalcLocalStats(false, chra);
   }

   public void recalcLocalStats(boolean first_login, MapleCharacter chra) {
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      long oldmaxhp = this.localmaxhp;
      long localmaxhp_ = this.getMaxHp();
      long localmaxmp_ = this.getMaxMp();
      this.resetLocalStats(chra.getJob());
      localmaxhp_ += (long)Math.floor((double)((float)((long)this.ms_maxhp * localmaxhp_) / 100.0F));
      localmaxmp_ += (long)Math.floor((double)((float)((long)this.ms_maxmp * localmaxmp_) / 100.0F));
      MapleTrait.MapleTraitType[] var10 = MapleTrait.MapleTraitType.values();
      int starforce = var10.length;

      int plushp;
      for(plushp = 0; plushp < starforce; ++plushp) {
         MapleTrait.MapleTraitType t = var10[plushp];
         chra.getTrait(t).clearLocalExp();
      }

      int jokerItemId;
      if (chra.getKeyValue(19019, "id") >= 1L) {
         jokerItemId = (int)chra.getKeyValue(19019, "id");
         if (ii.getItemInformation(jokerItemId) != null) {
            starforce = ii.getItemInformation(jokerItemId).nickSkill;
            if (SkillFactory.getSkill(starforce) != null) {
               SecondaryStatEffect eff = SkillFactory.getSkill(starforce).getEffect(1);
               this.arc += eff.getArcX();
               this.BossDamage += eff.getBossDamage();
               this.localstr += eff.getStrX();
               this.localdex += eff.getDexX();
               this.localint_ += eff.getIntX();
               this.localluk += eff.getLukX();
               this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
               this.watk += eff.getAttackX();
               this.magic += eff.getMagicX();
               this.NomarbdR += eff.getNbdR();
               this.dropBuff += (double)eff.getDropR();
               this.critical_rate += (short)eff.getCr();
               this.critical_damage += eff.getCriticalDamage();
               localmaxhp_ += (long)eff.getMaxHpX();
               localmaxmp_ += (long)eff.getMaxMpX();
            }
         }
      }

      jokerItemId = 0;
      starforce = 0;
      plushp = 0;
      int itemid = false;
      Map<Skill, SkillEntry> sData = new HashMap();
      Iterator var19;
      int value;
      synchronized(chra.getInventory(MapleInventoryType.EQUIPPED)) {
         Iterator itera = chra.getInventory(MapleInventoryType.EQUIPPED).newList().iterator();

         while(true) {
            if (!itera.hasNext()) {
               break;
            }

            Equip equip = (Equip)itera.next();
            if (equip.getItemId() / 1000 == 1672) {
               Item android = chra.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-27);
               if (android == null) {
                  continue;
               }
            }

            if (GameConstants.isZero(chra.getJob())) {
               if (chra.getGender() == 0) {
                  if (GameConstants.isBetaWeapon(equip.getItemId())) {
                     continue;
                  }
               } else if (chra.getGender() == 1 && GameConstants.isAlphaWeapon(equip.getItemId())) {
                  continue;
               }
            }

            List<Integer> potentials = new ArrayList();
            if (equip.getPotential1() > 0) {
               potentials.add(equip.getPotential1());
            }

            if (equip.getPotential2() > 0) {
               potentials.add(equip.getPotential2());
            }

            if (equip.getPotential3() > 0) {
               potentials.add(equip.getPotential3());
            }

            if (equip.getPotential4() > 0) {
               potentials.add(equip.getPotential4());
            }

            if (equip.getPotential5() > 0) {
               potentials.add(equip.getPotential5());
            }

            if (equip.getPotential6() > 0) {
               potentials.add(equip.getPotential6());
            }

            var19 = potentials.iterator();

            while(var19.hasNext()) {
               Integer potential = (Integer)var19.next();
               int lv = ii.getReqLevel(equip.getItemId()) / 10 - 1;
               if (lv < 0) {
                  lv = 0;
               }

               if (potential != 0 && ii.getPotentialInfo(potential) != null && ii.getPotentialInfo(potential).get(lv) != null) {
                  localmaxhp_ += (long)(((StructPotentialItem)ii.getPotentialInfo(potential).get(lv)).incMHP / (GameConstants.isDemonAvenger(chra.getJob()) ? 2 : 1));
                  localmaxmp_ += (long)((StructPotentialItem)ii.getPotentialInfo(potential).get(lv)).incMMP;
                  this.percent_hp += ((StructPotentialItem)ii.getPotentialInfo(potential).get(lv)).incMHPr;
                  this.percent_mp += ((StructPotentialItem)ii.getPotentialInfo(potential).get(lv)).incMMPr;
                  this.mesoBuff += (double)((StructPotentialItem)ii.getPotentialInfo(potential).get(lv)).incMesoProp;
                  this.dropBuff += (double)((StructPotentialItem)ii.getPotentialInfo(potential).get(lv)).incRewardProp;
                  this.percent_str += ((StructPotentialItem)ii.getPotentialInfo(potential).get(lv)).incSTRr;
                  this.percent_dex += ((StructPotentialItem)ii.getPotentialInfo(potential).get(lv)).incDEXr;
                  this.percent_int += ((StructPotentialItem)ii.getPotentialInfo(potential).get(lv)).incINTr;
                  this.percent_luk += ((StructPotentialItem)ii.getPotentialInfo(potential).get(lv)).incLUKr;
                  if ((potential < 201 || potential > 212) && potential != 391 && potential != 10081 && potential != 12081 && potential != 12082 && potential != 12801) {
                     this.localstr += ((StructPotentialItem)ii.getPotentialInfo(potential).get(lv)).incSTR;
                  } else {
                     this.localstr += ((StructPotentialItem)ii.getPotentialInfo(potential).get(lv)).incSTR;
                     this.localdex += ((StructPotentialItem)ii.getPotentialInfo(potential).get(lv)).incSTR;
                     this.localint_ += ((StructPotentialItem)ii.getPotentialInfo(potential).get(lv)).incSTR;
                     this.localluk += ((StructPotentialItem)ii.getPotentialInfo(potential).get(lv)).incSTR;
                  }

                  this.reduceCooltime += ((StructPotentialItem)ii.getPotentialInfo(potential).get(lv)).reduceCooltime;
                  this.localdex += ((StructPotentialItem)ii.getPotentialInfo(potential).get(lv)).incDEX;
                  this.localint_ += ((StructPotentialItem)ii.getPotentialInfo(potential).get(lv)).incINT;
                  this.localstr += ((StructPotentialItem)ii.getPotentialInfo(potential).get(lv)).incSTR;
                  this.localluk += ((StructPotentialItem)ii.getPotentialInfo(potential).get(lv)).incLUK;
                  this.WatkPercent += (double)((StructPotentialItem)ii.getPotentialInfo(potential).get(lv)).incPADr;
                  this.MatkPercent += (double)((StructPotentialItem)ii.getPotentialInfo(potential).get(lv)).incMADr;
                  this.dam_r *= ((double)((StructPotentialItem)ii.getPotentialInfo(potential).get(lv)).incDAMr + 100.0D) / 100.0D;
               }
            }

            chra.getTrait(MapleTrait.MapleTraitType.craft).addLocalExp(equip.getHands());
            this.accuracy += equip.getAcc();
            if (GameConstants.isDemonAvenger(chra.getJob()) && equip.getEnhance() > 0) {
               starforce += equip.getEnhance();
               if (GameConstants.isLongcoat(equip.getItemId())) {
                  starforce += equip.getEnhance();
               }

               if (starforce > 0 && starforce <= 425) {
                  plushp = starforce > 391 ? 168 : (starforce > 380 && starforce <= 390 ? 166 : (starforce > 370 && starforce <= 380 ? 164 : (starforce > 360 && starforce <= 370 ? 162 : (starforce > 350 && starforce <= 360 ? 160 : (starforce > 340 && starforce <= 350 ? 158 : (starforce > 330 && starforce <= 340 ? 156 : (starforce > 320 && starforce <= 330 ? 154 : (starforce > 310 && starforce <= 320 ? 152 : (starforce > 290 && starforce <= 310 ? 150 : (starforce > 270 && starforce <= 290 ? 148 : (starforce > 250 && starforce <= 270 ? 146 : (starforce > 225 && starforce <= 250 ? 144 : (starforce > 200 && starforce <= 225 ? 142 : (starforce > 175 && starforce <= 200 ? 140 : (starforce > 150 && starforce <= 175 ? 138 : (starforce > 125 && starforce <= 150 ? 135 : (starforce > 90 && starforce <= 125 ? 120 : (starforce > 60 && starforce <= 90 ? 100 : (starforce > 35 && starforce <= 60 ? 80 : (starforce > 15 && starforce <= 35 ? 60 : 35))))))))))))))))))));
                  plushp *= starforce;
               }
            }

            this.DamagePercent += (double)equip.getTotalDamage();
            this.BossDamage += equip.getBossDamage();
            this.ignoreMobpdpR.add(Integer.valueOf(equip.getIgnorePDR()));
            if (!GameConstants.isArcaneSymbol(equip.getItemId()) && !GameConstants.isAuthenticSymbol(equip.getItemId())) {
               localmaxhp_ += (long)(!GameConstants.isDemonAvenger(chra.getJob()) && (!GameConstants.isZero(chra.getJob()) || !GameConstants.isWeapon(equip.getItemId())) ? equip.getHp() : equip.getHp() / 2);
               localmaxmp_ += (long)equip.getMp();
               this.localstr += equip.getStr();
               this.localdex += equip.getDex();
               this.localint_ += equip.getInt();
               this.localluk += equip.getLuk();
            } else {
               this.arc += equip.getArc();
               this.Nlocalhp += equip.getHp() * 10;
               this.Nlocalmp += equip.getMp();
               this.Nlocalstr += equip.getStr();
               this.Nlocaldex += equip.getDex();
               this.Nlocalint += equip.getInt();
               this.Nlocalluk += equip.getLuk();
            }

            this.starforce += GameConstants.isLongcoat(equip.getItemId()) ? equip.getEnhance() * 2 : equip.getEnhance();
            this.watk += equip.getWatk();
            this.magic += equip.getMatk();
            this.wdef += equip.getWdef();
            this.mdef += equip.getMdef();
            this.speed += equip.getSpeed();
            this.jump += equip.getJump();
            if (equip.getAllStat() > 0) {
               this.percent_str += equip.getAllStat();
               this.percent_dex += equip.getAllStat();
               this.percent_int += equip.getAllStat();
               this.percent_luk += equip.getAllStat();
            }

            this.pvpDamage += equip.getPVPDamage();
            Integer set = ii.getSetItemID(equip.getItemId());
            if (set != null && set > 0) {
               value = 1;
               if (this.setHandling.containsKey(set)) {
                  value += (Integer)this.setHandling.get(set);
               }

               this.setHandling.put(set, value);
            }

            if (ii.isJokerToSetItem(equip.getItemId()) && jokerItemId > equip.getItemId()) {
               jokerItemId = equip.getItemId();
            }

            Pair<Long, Long> add = this.handleEquipAdditions(ii, chra, first_login, sData, equip.getItemId());
            localmaxhp_ += (Long)add.left;
            localmaxmp_ += (Long)add.right;
            if (ii.getEquipStats(equip.getItemId()) != null && ii.getEquipStats(equip.getItemId()).get("MHPr") != null) {
               this.percent_hp += (Integer)ii.getEquipStats(equip.getItemId()).get("MHPr");
            }

            if (ii.getEquipStats(equip.getItemId()) != null && ii.getEquipStats(equip.getItemId()).get("MMPr") != null) {
               this.percent_mp += (Integer)ii.getEquipStats(equip.getItemId()).get("MMPr");
            }

            if (equip.getDurability() > 0) {
               this.durabilityHandling.add(equip);
            }

            if (GameConstants.getMaxLevel(equip.getItemId()) > 0 && equip.getEquipLevel() <= GameConstants.getMaxLevel(equip.getItemId())) {
               this.equipLevelHandling.add(equip);
            }
         }
      }

      Iterator var15 = this.setHandling.entrySet().iterator();

      while(true) {
         Entry entry;
         StructSetItem set2;
         do {
            if (!var15.hasNext()) {
               if (GameConstants.isDemonAvenger(chra.getJob()) && plushp > 0) {
                  localmaxhp_ += (long)plushp;
               }

               this.handleProfessionTool(chra);
               if (first_login && chra.getLevel() >= 30) {
                  if (chra.isGM()) {
                     for(int i = 0; i < allJobs.length; ++i) {
                        sData.put(SkillFactory.getSkill(1085 + allJobs[i]), new SkillEntry(1, (byte)0, -1L));
                        sData.put(SkillFactory.getSkill(1087 + allJobs[i]), new SkillEntry(1, (byte)0, -1L));
                     }
                  } else {
                     sData.put(SkillFactory.getSkill(getSkillByJob(1085, chra.getJob())), new SkillEntry(1, (byte)0, -1L));
                     sData.put(SkillFactory.getSkill(getSkillByJob(1087, chra.getJob())), new SkillEntry(1, (byte)0, -1L));
                  }
               }

               Pair<Long, Long> buffstats = this.handleBuffStats(chra);
               localmaxhp_ += (Long)buffstats.left;
               localmaxmp_ += (Long)buffstats.right;
               Pair<Long, Long> inner = this.handleInnerSkills(chra);
               localmaxhp_ += (Long)inner.left;
               localmaxmp_ += (Long)inner.right;
               Pair<Long, Long> hpmp = this.handlePassiveSkills(chra);
               localmaxhp_ += (Long)hpmp.left;
               localmaxmp_ += (Long)hpmp.right;
               Pair<Long, Long> unions = this.handleUnionSkills(chra);
               localmaxhp_ += (Long)unions.left;
               localmaxmp_ += (Long)unions.right;
               this.localstr += (int)Math.floor((double)((float)(this.localstr * this.percent_str) / 100.0F));
               this.localdex += (int)Math.floor((double)((float)(this.localdex * this.percent_dex) / 100.0F));
               this.localint_ += (int)Math.floor((double)((float)(this.localint_ * this.percent_int) / 100.0F));
               this.localluk += (int)Math.floor((double)((float)(this.localluk * this.percent_luk) / 100.0F));
               this.localstr += this.Nlocalstr;
               this.localdex += this.Nlocaldex;
               this.localint_ += this.Nlocalint;
               this.localluk += this.Nlocalluk;
               if (this.localint_ > this.localdex) {
                  this.accuracy += (int)((double)this.localint_ + Math.floor((double)this.localluk * 1.2D));
               } else {
                  this.accuracy += (int)((double)this.localluk + Math.floor((double)this.localdex * 1.2D));
               }

               this.watk += (int)Math.floor((double)((float)(this.watk * this.percent_atk) / 100.0F));
               this.magic += (int)Math.floor((double)((float)(this.magic * this.percent_matk) / 100.0F));
               this.wdef += (int)Math.floor((double)this.localstr * 1.2D + (double)(this.localdex + this.localluk) * 0.5D + (double)this.localint_ * 0.4D);
               this.mdef += (int)Math.floor((double)this.localstr * 0.4D + (double)(this.localdex + this.localluk) * 0.5D + (double)this.localint_ * 1.2D);
               this.wdef += (int)Math.min(30000.0D, Math.floor((double)((float)(this.wdef * this.percent_wdef) / 100.0F)));
               this.mdef += (int)Math.min(30000.0D, Math.floor((double)((float)(this.wdef * this.percent_mdef) / 100.0F)));
               this.critical_rate = (short)Math.min(100, this.critical_rate);
               localmaxhp_ += (long)Math.floor((double)((float)((long)this.multi_lateral_hp * localmaxhp_) / 100.0F));
               localmaxhp_ += (long)(chra.getTrait(MapleTrait.MapleTraitType.will).getLevel() / 5 * 100);
               localmaxhp_ += (long)Math.floor((double)((float)((long)this.percent_hp * localmaxhp_) / 100.0F));
               localmaxhp_ += (long)this.Nlocalhp;
               if (this.multi_lateral_hp >= 30) {
                  this.localmaxhp = (long)((double)localmaxhp_ * 0.977202581369248D);
               }

               localmaxhp_ += this.fixHp;
               localmaxmp_ += (long)Math.floor((double)((float)((long)this.multi_lateral_mp * localmaxmp_) / 100.0F));
               localmaxmp_ += (long)Math.floor((double)((float)((long)this.percent_mp * localmaxmp_) / 100.0F));
               localmaxmp_ += (long)Math.floor((double)((float)(this.before_percent_mp * this.before_maxmp) / 100.0F));
               localmaxmp_ += (long)(chra.getTrait(MapleTrait.MapleTraitType.sense).getLevel() / 5 * 100);
               localmaxmp_ += (long)this.Nlocalmp;
               this.shp = localmaxhp_;
               this.localmaxhp = Math.min(500000L, Math.abs(Math.max(-500000L, localmaxhp_)));
               this.localmaxhp = Math.max(1L, this.localmaxhp);
               this.localmaxmp = Math.min(500000L, Math.abs(Math.max(-500000L, localmaxmp_)));
               if (chra.getBuffedEffect(SecondaryStat.LimitMP) != null) {
                  this.localmaxmp = (long)chra.getBuffedValue(SecondaryStat.LimitMP);
               }

               if (chra.getBuffedEffect(SecondaryStat.DropItemRate) != null) {
                  this.dropBuff += (double)chra.getBuffedValue(SecondaryStat.DropItemRate);
               }

               if (this.hp > this.localmaxhp) {
                  chra.addHP(-(this.hp - this.localmaxhp));
               }

               if (this.mp > this.localmaxmp) {
                  chra.addMP(-(this.mp - this.localmaxmp));
               }

               this.hands = this.localdex + this.localint_ + this.localluk;
               this.calculateFame(chra);
               this.pvpDamage += chra.getTrait(MapleTrait.MapleTraitType.charisma).getLevel() / 10;
               this.ASR += chra.getTrait(MapleTrait.MapleTraitType.will).getLevel() / 5;
               this.accuracy += (int)Math.floor((double)((float)(this.accuracy * this.percent_acc) / 100.0F));
               this.accuracy += chra.getTrait(MapleTrait.MapleTraitType.insight).getLevel() * 15 / 10;
               chra.changeSkillLevel_Skip(sData, false);
               if (GameConstants.isDemonSlayer(chra.getJob())) {
                  this.localmaxmp = (long)GameConstants.getMPByJob(chra);
                  int force = 30;
                  switch(chra.getJob()) {
                  case 3110:
                     force = 50;
                     break;
                  case 3111:
                     force = 100;
                     break;
                  case 3112:
                     force = 120;
                  }

                  this.maxmp = (long)force;
               } else if (GameConstants.isZero(chra.getJob())) {
                  this.localmaxmp = 100L;
               }

               if (GameConstants.isDemonAvenger(chra.getJob())) {
                  EnumMap<SecondaryStat, Pair<Integer, Integer>> statups = new EnumMap(SecondaryStat.class);
                  statups.put(SecondaryStat.LifeTidal, new Pair(3, 0));
                  chra.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, (SecondaryStatEffect)null, chra));
               }

               if (first_login) {
                  chra.silentEnforceMaxHpMp();
                  this.relocHeal(chra);
               } else {
                  chra.enforceMaxHpMp();
               }

               if (oldmaxhp != 0L && oldmaxhp != this.localmaxhp) {
                  chra.updatePartyMemberHP();
               }

               return;
            }

            entry = (Entry)var15.next();
            set2 = ii.getSetItem((Integer)entry.getKey());
         } while(set2 == null);

         Map<Integer, StructSetItem.SetItem> itemz = set2.getItems();
         if (set2.jokerPossible && jokerItemId > 0 && (Integer)entry.getValue() < set2.completeCount) {
            var19 = set2.itemIDs.iterator();

            while(var19.hasNext()) {
               value = (Integer)var19.next();
               if (GameConstants.isWeapon(value) && GameConstants.isWeapon(jokerItemId)) {
                  entry.setValue((Integer)entry.getValue() + 1);
                  break;
               }

               if (!GameConstants.isWeapon(value) && !GameConstants.isWeapon(jokerItemId) && value / 10000 == jokerItemId / 10000 && chra.getInventory(MapleInventoryType.EQUIPPED).findById(value) == null) {
                  entry.setValue((Integer)entry.getValue() + 1);
                  break;
               }
            }
         }

         boolean zeroweaponlucky = false;
         if ((long)set2.setItemID == chra.getKeyValue(46523, "luckyscroll") && set2.zeroWeaponJokerPossible) {
            zeroweaponlucky = true;
         }

         Iterator var41 = itemz.entrySet().iterator();

         while(var41.hasNext()) {
            Entry<Integer, StructSetItem.SetItem> ent = (Entry)var41.next();
            if ((Integer)ent.getKey() <= (Integer)entry.getValue() + (zeroweaponlucky ? 1 : 0)) {
               StructSetItem.SetItem se = (StructSetItem.SetItem)ent.getValue();
               this.localstr += se.incSTR + se.incAllStat;
               this.localdex += se.incDEX + se.incAllStat;
               this.localint_ += se.incINT + se.incAllStat;
               this.localluk += se.incLUK + se.incAllStat;
               this.watk += se.incPAD;
               this.magic += se.incMAD;
               this.speed += se.incSpeed;
               this.accuracy += se.incACC;
               localmaxhp_ += (long)(GameConstants.isDemonAvenger(chra.getJob()) ? se.incMHP / 2 : se.incMHP);
               localmaxmp_ += (long)se.incMMP;
               this.percent_hp += se.incMHPr;
               this.percent_mp += se.incMMPr;
               this.wdef += se.incPDD;
               this.mdef += se.incMDD;
            }
         }
      }
   }

   private Pair<Long, Long> handleUnionSkills(MapleCharacter chra) {
      long localmaxhp_ = 0L;
      long localmaxmp_ = 0L;
      ArrayList setPoints = new ArrayList();

      try {
         Iterator var7 = chra.getUnions().getUnions().iterator();

         label458:
         while(true) {
            MapleUnion union;
            byte level;
            byte type;
            int unionLevel;
            do {
               do {
                  do {
                     do {
                        do {
                           if (!var7.hasNext()) {
                              return new Pair(localmaxhp_, localmaxmp_);
                           }

                           union = (MapleUnion)var7.next();
                        } while(union.getPosition() < 0);

                        level = 0;
                        if (!GameConstants.isZero(union.getJob())) {
                           if (union.getLevel() >= 250) {
                              level = 5;
                           } else if (union.getLevel() >= 200) {
                              level = 4;
                           } else if (union.getLevel() >= 140) {
                              level = 3;
                           } else if (union.getLevel() >= 100) {
                              level = 2;
                           } else if (union.getLevel() >= 60) {
                              level = 1;
                           }
                        } else if (union.getLevel() >= 250) {
                           level = 5;
                        } else if (union.getLevel() >= 200) {
                           level = 4;
                        } else if (union.getLevel() >= 180) {
                           level = 3;
                        } else if (union.getLevel() >= 160) {
                           level = 2;
                        } else if (union.getLevel() >= 130) {
                           level = 1;
                        }

                        type = 0;
                        if (GameConstants.isXenon(union.getJob())) {
                           type = 36;
                        } else if (GameConstants.isWarrior(union.getJob())) {
                           type = 1;
                        } else if (GameConstants.isMagician(union.getJob())) {
                           type = 2;
                        } else if (GameConstants.isArcher(union.getJob())) {
                           type = 3;
                        } else if (GameConstants.isThief(union.getJob())) {
                           type = 4;
                        } else if (GameConstants.isPirate(union.getJob())) {
                           type = 5;
                        }

                        unionLevel = (int)chra.getKeyValue(18771, "rank");
                     } while(level <= 0);
                  } while(type <= 0);

                  int jobSkill;
                  SecondaryStatEffect jobSkillEffect;
                  if (UnionHandler.cardSkills.containsKey(union.getJob() / 10) && (jobSkillEffect = SkillFactory.getSkill(jobSkill = (Integer)UnionHandler.cardSkills.get(union.getJob() / 10)).getEffect(level)) != null) {
                     switch(jobSkill) {
                     case 71000013:
                        this.percent_hp += jobSkillEffect.getPercentHP();
                        break;
                     case 71000021:
                        this.percent_mp += jobSkillEffect.getPercentMP();
                        break;
                     case 71000052:
                        this.BuffUP_Summon += jobSkillEffect.getSummonTimeR();
                        break;
                     case 71000111:
                     case 71000511:
                        this.fixHp += (long)jobSkillEffect.getHpFX();
                        break;
                     case 71000351:
                        this.BuffUP_Skill += jobSkillEffect.getBufftimeR();
                        break;
                     case 71000711:
                        this.expBuffZero += (double)jobSkillEffect.getEXPRate();
                     }
                  }
               } while(!UnionHandler.characterSizes.containsKey(Integer.valueOf(type)));
            } while(!((Map)UnionHandler.characterSizes.get(Integer.valueOf(type))).containsKey(level - 1));

            HashMap<Integer, Integer> skills = new HashMap();
            List<Point> characterData = (List)((Map)UnionHandler.characterSizes.get(Integer.valueOf(type))).get(level - 1);
            Point main = null;
            int indexof = 0;
            int Jobtype = GameConstants.isWarrior(union.getJob()) ? 1 : (GameConstants.isMagician(union.getJob()) ? 2 : (GameConstants.isArcher(union.getJob()) ? 3 : (GameConstants.isThief(union.getJob()) ? 4 : 5)));
            Iterator var19 = characterData.iterator();

            while(true) {
               Point poszz;
               Point newWon;
               do {
                  int angle;
                  if (!var19.hasNext()) {
                     var19 = skills.entrySet().iterator();

                     while(var19.hasNext()) {
                        Entry entry = (Entry)var19.next();
                        SecondaryStatEffect fieldEffect = SkillFactory.getSkill((Integer)entry.getKey()).getEffect((Integer)entry.getValue());
                        if (fieldEffect != null) {
                           angle = fieldEffect.getSourceId() % 10;
                           if (chra.getKeyValue(18791, angle.makeConcatWithConstants<invokedynamic>(angle)) != -1L && chra.getKeyValue(18791, angle.makeConcatWithConstants<invokedynamic>(angle)) != (long)angle) {
                              int skillid = (int)(71004000L + chra.getKeyValue(18791, angle.makeConcatWithConstants<invokedynamic>(angle)));
                              fieldEffect = SkillFactory.getSkill(skillid).getEffect((Integer)entry.getValue());
                           }

                           switch(fieldEffect.getSourceId()) {
                           case 71004006:
                              localmaxhp_ += (long)fieldEffect.getMhpX();
                              break;
                           case 71004007:
                              localmaxmp_ += (long)fieldEffect.getMaxMpX();
                           case 71004008:
                           case 71004009:
                           default:
                              break;
                           case 71004010:
                              this.expBuffUnion += fieldEffect.getExpRPerM();
                              break;
                           case 71004011:
                              this.critical_rate = (short)(this.critical_rate + fieldEffect.getCr());
                              break;
                           case 71004012:
                              this.BossDamage += fieldEffect.getBdR();
                              break;
                           case 71004013:
                              this.stance += fieldEffect.getStanceProp();
                              break;
                           case 71004014:
                              this.BuffUP_Skill += fieldEffect.getBufftimeR();
                              break;
                           case 71004015:
                              this.ignoreMobpdpR.add(Integer.valueOf(fieldEffect.getIgnoreMob()));
                           }
                        }
                     }
                     continue label458;
                  }

                  Point point = (Point)var19.next();
                  Point pos = (Point)UnionHandler.boardPos.get(union.getPosition());
                  angle = union.getUnk2();
                  poszz = new Point(pos.x + point.x, pos.y + point.y);
                  int totalangle = angle;
                  if (angle >= 1000) {
                     int anglccalc = angle / 1000;
                     angle -= anglccalc * 1000;
                  }

                  if (indexof != 0) {
                     totalangle = angle - angle;
                     switch(Jobtype) {
                     case 1:
                        if (angle == 90) {
                           if (indexof == 1) {
                              poszz.x = main.x;
                              poszz.y = main.y + 1;
                           } else if (indexof == 2) {
                              poszz.x = main.x + 1;
                              poszz.y = main.y;
                           } else if (indexof == 3) {
                              poszz.x = main.x + 1;
                              poszz.y = main.y + 1;
                           } else if (indexof == 4) {
                              poszz.x = main.x;
                              poszz.y = main.y + 2;
                           }
                        } else if (angle == 180) {
                           if (indexof == 1) {
                              poszz.x = main.x - 1;
                              poszz.y = main.y;
                           } else if (indexof == 2) {
                              poszz.x = main.x;
                              poszz.y = main.y + 1;
                           } else if (indexof == 3) {
                              poszz.x = main.x - 1;
                              poszz.y = main.y + 1;
                           } else if (indexof == 4) {
                              poszz.x = main.x - 2;
                              poszz.y = main.y;
                           }
                        } else if (angle == 270) {
                           if (indexof == 1) {
                              poszz.x = main.x;
                              poszz.y = main.y - 1;
                           } else if (indexof == 2) {
                              poszz.x = main.x - 1;
                              poszz.y = main.y;
                           } else if (indexof == 3) {
                              poszz.x = main.x - 1;
                              poszz.y = main.y - 1;
                           } else if (indexof == 4) {
                              poszz.x = main.x;
                              poszz.y = main.y - 2;
                           }
                        }
                        break;
                     case 2:
                        if (angle == 90) {
                           if (indexof == 1) {
                              poszz.x = main.x;
                              poszz.y = main.y + 1;
                           } else if (indexof == 2) {
                              poszz.x = main.x;
                              poszz.y = main.y - 1;
                           } else if (indexof == 3) {
                              poszz.x = main.x + 1;
                              poszz.y = main.y;
                           } else if (indexof == 4) {
                              poszz.x = main.x - 1;
                              poszz.y = main.y;
                           }
                        } else if (angle == 180) {
                           if (indexof == 1) {
                              poszz.x = main.x - 1;
                              poszz.y = main.y;
                           } else if (indexof == 2) {
                              poszz.x = main.x + 1;
                              poszz.y = main.y;
                           } else if (indexof == 3) {
                              poszz.x = main.x;
                              poszz.y = main.y + 1;
                           } else if (indexof == 4) {
                              poszz.x = main.x;
                              poszz.y = main.y - 1;
                           }
                        } else if (angle == 270) {
                           if (indexof == 1) {
                              poszz.x = main.x;
                              poszz.y = main.y - 1;
                           } else if (indexof == 2) {
                              poszz.x = main.x;
                              poszz.y = main.y + 1;
                           } else if (indexof == 3) {
                              poszz.x = main.x - 1;
                              poszz.y = main.y;
                           } else if (indexof == 4) {
                              poszz.x = main.x + 1;
                              poszz.y = main.y;
                           }
                        }
                        break;
                     case 3:
                        if (angle == 90) {
                           if (indexof == 1) {
                              poszz.x = main.x;
                              poszz.y = main.y + 1;
                           } else if (indexof == 2) {
                              poszz.x = main.x;
                              poszz.y = main.y - 1;
                           } else if (indexof == 3) {
                              poszz.x = main.x;
                              poszz.y = main.y + 2;
                           } else if (indexof == 4) {
                              poszz.x = main.x;
                              poszz.y = main.y - 2;
                           }
                        } else if (angle == 180) {
                           if (indexof == 1) {
                              poszz.x = main.x - 1;
                              poszz.y = main.y;
                           } else if (indexof == 2) {
                              poszz.x = main.x + 1;
                              poszz.y = main.y;
                           } else if (indexof == 3) {
                              poszz.x = main.x - 2;
                              poszz.y = main.y;
                           } else if (indexof == 4) {
                              poszz.x = main.x + 2;
                              poszz.y = main.y;
                           }
                        } else if (angle == 270) {
                           if (indexof == 1) {
                              poszz.x = main.x;
                              poszz.y = main.y - 1;
                           } else if (indexof == 2) {
                              poszz.x = main.x;
                              poszz.y = main.y + 1;
                           } else if (indexof == 3) {
                              poszz.x = main.x;
                              poszz.y = main.y - 2;
                           } else if (indexof == 4) {
                              poszz.x = main.x;
                              poszz.y = main.y + 2;
                           }
                        }
                        break;
                     case 4:
                        if (angle == 90) {
                           if (indexof == 1) {
                              poszz.x = main.x;
                              poszz.y = main.y + 1;
                           } else if (indexof == 2) {
                              poszz.x = main.x;
                              poszz.y = main.y - 1;
                           } else if (indexof == 3) {
                              poszz.x = main.x + 1;
                              poszz.y = main.y + 1;
                           } else if (indexof == 4) {
                              poszz.x = main.x - 1;
                              poszz.y = main.y + 1;
                           }
                        } else if (angle == 180) {
                           if (indexof == 1) {
                              poszz.x = main.x - 1;
                              poszz.y = main.y;
                           } else if (indexof == 2) {
                              poszz.x = main.x + 1;
                              poszz.y = main.y;
                           } else if (indexof == 3) {
                              poszz.x = main.x - 1;
                              poszz.y = main.y + 1;
                           } else if (indexof == 4) {
                              poszz.x = main.x - 1;
                              poszz.y = main.y - 1;
                           }
                        } else if (angle == 270) {
                           if (indexof == 1) {
                              poszz.x = main.x;
                              poszz.y = main.y - 1;
                           } else if (indexof == 2) {
                              poszz.x = main.x;
                              poszz.y = main.y + 1;
                           } else if (indexof == 3) {
                              poszz.x = main.x + 1;
                              poszz.y = main.y - 1;
                           } else if (indexof == 4) {
                              poszz.x = main.x - 1;
                              poszz.y = main.y - 1;
                           }
                        }
                        break;
                     case 5:
                        if (angle == 90) {
                           if (indexof == 1) {
                              poszz.x = main.x;
                              poszz.y = main.y + 1;
                           } else if (indexof == 2) {
                              poszz.x = main.x + 1;
                              poszz.y = main.y;
                           } else if (indexof == 3) {
                              poszz.x = main.x - 1;
                              poszz.y = main.y + 1;
                           } else if (indexof == 4) {
                              poszz.x = main.x - 2;
                              poszz.y = main.y + 1;
                           }
                        } else if (angle == 180) {
                           if (indexof == 1) {
                              poszz.x = main.x - 1;
                              poszz.y = main.y;
                           } else if (indexof == 2) {
                              poszz.x = main.x;
                              poszz.y = main.y + 1;
                           } else if (indexof == 3) {
                              poszz.x = main.x - 1;
                              poszz.y = main.y - 1;
                           } else if (indexof == 4) {
                              poszz.x = main.x - 1;
                              poszz.y = main.y - 2;
                           }
                        } else if (angle == 270) {
                           if (indexof == 1) {
                              poszz.x = main.x;
                              poszz.y = main.y - 1;
                           } else if (indexof == 2) {
                              poszz.x = main.x - 1;
                              poszz.y = main.y;
                           } else if (indexof == 3) {
                              poszz.x = main.x + 1;
                              poszz.y = main.y - 1;
                           } else if (indexof == 4) {
                              poszz.x = main.x + 2;
                              poszz.y = main.y - 1;
                           }
                        }
                     }
                  } else if (indexof == 0) {
                     main = poszz;
                  }

                  newWon = poszz;
                  int calc;
                  if (totalangle == 1000) {
                     calc = main.x - poszz.x;
                     if (main.x != poszz.x) {
                        newWon = new Point(main.x + calc, poszz.y);
                     }
                  } else if (totalangle == 2000) {
                     calc = main.y - poszz.y;
                     if (main.y != poszz.y) {
                        newWon = new Point(poszz.x, main.y + calc);
                     }
                  } else if (totalangle == 3000) {
                     calc = main.y - poszz.y;
                     if (main.y != poszz.y) {
                        newWon = new Point(poszz.x, main.y + calc);
                     }

                     int calcx = main.x - newWon.x;
                     if (main.x != newWon.x) {
                        newWon = new Point(main.x + calcx, newWon.y);
                     }
                  }

                  poszz = newWon;
               } while(setPoints.contains(newWon));

               setPoints.add(newWon);
               Iterator var36 = UnionHandler.boardPos.iterator();

               while(var36.hasNext()) {
                  Point realPos = (Point)var36.next();
                  if (realPos.x == poszz.x && realPos.y == poszz.y) {
                     int index = (Integer)UnionHandler.groupIndex.get(UnionHandler.boardPos.indexOf(realPos));
                     int reqLevel = (Integer)UnionHandler.openLevels.get(UnionHandler.boardPos.indexOf(realPos));
                     if (unionLevel >= reqLevel) {
                        int skillID = (Integer)UnionHandler.skills.get(index);
                        if (skills.containsKey(skillID)) {
                           skills.put(skillID, (Integer)skills.get(skillID) + 1);
                        } else {
                           skills.put(skillID, 1);
                        }
                     }
                  }
               }

               ++indexof;
            }
         }
      } catch (Exception var31) {
         var31.printStackTrace();
         return new Pair(localmaxhp_, localmaxmp_);
      }
   }

   public List<Triple<Integer, String, Integer>> getPsdSkills() {
      return this.psdSkills;
   }

   private Pair<Long, Long> handlePassiveSkills(MapleCharacter chra) {
      SecondaryStatEffect eff = null;
      this.DAMreduceR = 0;
      long localmaxhp_ = 0L;
      long localmaxmp_ = 0L;
      this.psdSkills.clear();
      int bof;
      Skill bx;
      if (chra.getGuild() != null) {
         bx = SkillFactory.getSkill(91000034);
         bof = chra.getGuild().getSkillLevel(91000034);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localstr += eff.getStrX();
            this.localdex += eff.getDexX();
            this.localint_ += eff.getIntX();
            this.localluk += eff.getLukX();
            localmaxhp_ += (long)bx.getEffect(bof).getMaxHpX();
         }

         bx = SkillFactory.getSkill(91000005);
         bof = chra.getGuild().getSkillLevel(91000005);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
         }
      }

      int skillid;
      if (chra.getSkillLevel(80000400) > 0) {
         skillid = 80000400;
         bx = SkillFactory.getSkill(skillid);
         bof = chra.getTotalSkillLevel(skillid);
         if (bof > 0) {
            this.Nlocalstr += bx.getEffect(bof).getStrFX();
         }
      }

      if (chra.getSkillLevel(80000401) > 0) {
         skillid = 80000401;
         bx = SkillFactory.getSkill(skillid);
         bof = chra.getTotalSkillLevel(skillid);
         if (bof > 0) {
            this.Nlocaldex += bx.getEffect(bof).getDexFX();
         }
      }

      if (chra.getSkillLevel(80000402) > 0) {
         skillid = 80000402;
         bx = SkillFactory.getSkill(skillid);
         bof = chra.getTotalSkillLevel(skillid);
         if (bof > 0) {
            this.Nlocalint += bx.getEffect(bof).getIntFX();
         }
      }

      if (chra.getSkillLevel(80000403) > 0) {
         skillid = 80000403;
         bx = SkillFactory.getSkill(skillid);
         bof = chra.getTotalSkillLevel(skillid);
         if (bof > 0) {
            this.Nlocalluk += bx.getEffect(bof).getLukFX();
         }
      }

      if (chra.getSkillLevel(80000404) > 0) {
         skillid = 80000404;
         bx = SkillFactory.getSkill(skillid);
         bof = chra.getTotalSkillLevel(skillid);
         if (bof > 0) {
            this.percent_hp += bx.getEffect(bof).getPercentHP();
         }
      }

      if (chra.getSkillLevel(80000405) > 0) {
         skillid = 80000405;
         bx = SkillFactory.getSkill(skillid);
         bof = chra.getTotalSkillLevel(skillid);
         if (bof > 0) {
            this.percent_mp += bx.getEffect(bof).getPercentMP();
         }
      }

      if (chra.getSkillLevel(80000407) > 0) {
         skillid = 80000407;
         bx = SkillFactory.getSkill(skillid);
         bof = chra.getTotalSkillLevel(skillid);
         if (bof > 0) {
            this.speed += bx.getEffect(bof).getPsdSpeed();
         }
      }

      if (chra.getSkillLevel(80000408) > 0) {
         skillid = 80000408;
         bx = SkillFactory.getSkill(skillid);
         bof = chra.getTotalSkillLevel(skillid);
         if (bof > 0) {
            this.jump += bx.getEffect(bof).getPsdJump();
         }
      }

      int i;
      int up;
      if (chra.getSkillLevel(80000409) > 0) {
         skillid = 80000409;
         bx = SkillFactory.getSkill(skillid);
         bof = chra.getTotalSkillLevel(skillid);
         up = 0;

         for(i = 0; i < bof; ++i) {
            if (i >= 5) {
               up += 2;
            } else {
               ++up;
            }
         }

         if (bof > 0) {
            this.critical_rate = (short)(this.critical_rate + up);
         }
      }

      if (chra.getSkillLevel(80000410) > 0) {
         skillid = 80000410;
         bx = SkillFactory.getSkill(skillid);
         bof = chra.getTotalSkillLevel(skillid);
         if (bof > 0) {
            this.critical_damage += bx.getEffect(bof).getCriticalDamage();
         }
      }

      if (chra.getSkillLevel(80000412) > 0) {
         skillid = 80000412;
         bx = SkillFactory.getSkill(skillid);
         bof = chra.getTotalSkillLevel(skillid);
         if (bof > 0) {
            this.ignoreMobpdpR.add(Integer.valueOf(bx.getEffect(bof).getIgnoreMob()));
         }
      }

      if (chra.getSkillLevel(80000413) > 0) {
         skillid = 80000413;
         bx = SkillFactory.getSkill(skillid);
         bof = chra.getTotalSkillLevel(skillid);
         if (bof > 0) {
            this.dam_r += (double)bx.getEffect(bof).getDAMRate();
         }
      }

      if (chra.getSkillLevel(80000414) > 0) {
         skillid = 80000414;
         bx = SkillFactory.getSkill(skillid);
         bof = chra.getTotalSkillLevel(skillid);
         up = 0;

         for(i = 0; i < bof; ++i) {
            if (i >= 5) {
               up += 4;
            } else {
               up += 3;
            }
         }

         if (bof > 0) {
            this.BossDamage += up;
         }
      }

      if (chra.getSkillLevel(80000415) > 0) {
         skillid = 80000415;
         bx = SkillFactory.getSkill(skillid);
         bof = chra.getTotalSkillLevel(skillid);
         if (bof > 0) {
            this.TER += bx.getEffect(bof).getTERRate();
         }
      }

      if (chra.getSkillLevel(80000416) > 0) {
         skillid = 80000416;
         bx = SkillFactory.getSkill(skillid);
         bof = chra.getTotalSkillLevel(skillid);
         up = 0;

         for(i = 0; i < bof; ++i) {
            if (i >= 5) {
               up += 2;
            } else {
               ++up;
            }
         }

         if (bof > 0) {
            this.ASR += up;
         }
      }

      if (chra.getSkillLevel(80000417) > 0) {
         skillid = 80000417;
         bx = SkillFactory.getSkill(skillid);
         bof = chra.getTotalSkillLevel(skillid);
         if (bof > 0) {
            this.stance += bx.getEffect(bof).getStanceProp();
         }
      }

      if (chra.getSkillLevel(80000419) > 0) {
         skillid = 80000419;
         bx = SkillFactory.getSkill(skillid);
         bof = chra.getTotalSkillLevel(skillid);
         if (bof > 0) {
            this.watk += bx.getEffect(bof).getAttackX();
            this.magic += bx.getEffect(bof).getMagicX();
         }
      }

      if (chra.getSkillLevel(80000421) > 0) {
         skillid = 80000421;
         bx = SkillFactory.getSkill(skillid);
         bof = chra.getTotalSkillLevel(skillid);
         up = 0;

         for(i = 0; i < bof; ++i) {
            if (i >= 10) {
               up += 10;
            } else {
               up += 5;
            }
         }

         if (bof > 0) {
            this.arc += up;
         }
      }

      int[] up2 = new int[]{400001000, 400001001, 400001002, 400001003, 400001004, 400001006, 400001007, 400001021, 400011066, 400021068, 400021095, 400031005, 400041032, 400051000, 400051072};
      i = up2.length;

      int n;
      for(n = 0; n < i; ++n) {
         Integer skill = up2[n];
         bx = SkillFactory.getSkill(skill);
         bof = chra.getTotalSkillLevel(skill);
         if (bof > 0) {
            switch(skill) {
            case 400001004:
               this.ASR += bx.getEffect(bof).getASRRate();
               break;
            case 400001021:
               this.localint_ += bx.getEffect(bof).getIntX();
               break;
            case 400011066:
               localmaxhp_ += (long)bx.getEffect(bof).getMaxHpX();
               this.localstr += bx.getEffect(bof).getIntX();
               break;
            case 400021068:
            case 400021095:
               this.magic += bx.getEffect(bof).getMagicX();
               break;
            case 400031005:
            case 400041032:
            case 400051000:
               this.watk += bx.getEffect(bof).getAttackX();
               break;
            case 400051072:
               this.localdex += bx.getEffect(bof).getDexX();
               break;
            default:
               this.localstr += bx.getEffect(bof).getStrX();
               this.localdex += bx.getEffect(bof).getStrX();
               this.localint_ += bx.getEffect(bof).getStrX();
               this.localluk += bx.getEffect(bof).getStrX();
            }
         }
      }

      int[] i2 = new int[]{80000654, 80000655, 80000656, 80000657, 80000658, 80000659, 80000660, 80000661};
      n = i2.length;

      for(int skill = 0; skill < n; ++skill) {
         Integer skill2 = i2[skill];
         bx = SkillFactory.getSkill(skill2);
         bof = chra.getTotalSkillLevel(skill2);
         if (bof > 0) {
            switch(skill2) {
            default:
               if (skill2 >= 80000654 && skill2 <= 80000661) {
                  this.NomarbdR += bx.getEffect(bof).getNbdR();
                  this.arc += bx.getEffect(bof).getArcX();
                  this.starforce += bx.getEffect(bof).getStarX();
                  this.critical_rate = (short)(this.critical_rate + bx.getEffect(bof).getCr());
                  this.watk += bx.getEffect(bof).getAttackX();
                  this.magic += bx.getEffect(bof).getAttackX();
                  this.localstr += bx.getEffect(bof).getStrX();
                  this.localdex += bx.getEffect(bof).getStrX();
                  this.localint_ += bx.getEffect(bof).getStrX();
                  this.localluk += bx.getEffect(bof).getStrX();
                  localmaxhp_ += (long)bx.getEffect(bof).getMhpX();
                  localmaxmp_ += (long)bx.getEffect(bof).getMhpX();
                  this.ignoreMobpdpR.add(Integer.valueOf(bx.getEffect(bof).getIgnoreMob()));
                  this.BossDamage += bx.getEffect(bof).getBdR();
               }
            }
         }
      }

      int[] linkskills = new int[]{80002766, 80002774, 80000055, 80000002, 80000005, 80000050, 80000001, 80000047, 80000110, 80000188, 80002857, 80000609, 80000006};
      int mylinkskillid = GameConstants.getMyLinkSkill(chra.getJob());
      int skill2 = ((int[])linkskills).length;

      int size;
      Integer i95;
      int a;
      for(size = 0; size < skill2; ++size) {
         i95 = linkskills[size];
         bof = 0;
         if (i95 != 80002774) {
            if (i95 != 80002766) {
               if (i95 == 80000055 && mylinkskillid >= 10000255 && mylinkskillid <= 10000259) {
                  bx = SkillFactory.getSkill(mylinkskillid);
                  bof = chra.getTotalSkillLevel(mylinkskillid);
                  if (bof > 0) {
                     eff = bx.getEffect(bof);
                     this.watk += eff.getAttackX();
                     this.magic += eff.getAttackX();
                     this.ASR += eff.getASRRate();
                     this.TER += eff.getTERRate();
                  }

                  bof = 0 + chra.getTotalSkillLevel(mylinkskillid);
               }
            } else if (mylinkskillid == 258 || mylinkskillid == 259 || mylinkskillid == 260) {
               bof += chra.getTotalSkillLevel(mylinkskillid);
            }
         } else if (mylinkskillid == 110 || mylinkskillid == 264 || mylinkskillid == 265) {
            bof += chra.getTotalSkillLevel(mylinkskillid);
         }

         bx = SkillFactory.getSkill(i95);
         if ((bof += chra.getTotalSkillLevel(i95)) > 0) {
            eff = bx.getEffect(bof);
            switch(i95) {
            case 80000001:
               this.BossDamage += eff.getBdR();
               break;
            case 80000002:
               this.critical_rate = (short)(this.critical_rate + eff.getCr());
               break;
            case 80000005:
               this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
               break;
            case 80000006:
               this.percent_hp += eff.getPercentHP();
               this.percent_mp += eff.getPercentMP();
               break;
            case 80000047:
               this.percent_str += eff.getStrR();
               this.percent_dex += eff.getStrR();
               this.percent_int += eff.getStrR();
               this.percent_luk += eff.getStrR();
               break;
            case 80000050:
               this.DamagePercent += (double)eff.getDAMRate();
               break;
            case 80000055:
               this.watk += eff.getAttackX();
               this.magic += eff.getAttackX();
               this.ASR += eff.getASRRate();
               this.TER += eff.getTERRate();
               break;
            case 80000110:
               this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
               break;
            case 80000188:
               this.critical_damage += eff.getCriticalDamage();
               break;
            case 80000609:
               this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
               break;
            case 80002766:
               a = eff.getY() <= 0 ? eff.getCr() : eff.getY();
               this.critical_rate = (short)(this.critical_rate + a);
               break;
            case 80002774:
               this.localstr += eff.getStrX();
               this.localdex += eff.getDexX();
               this.localint_ += eff.getIntX();
               this.localluk += eff.getLukX();
               localmaxhp_ += (long)eff.getMaxHpX();
               localmaxmp_ += (long)eff.getMaxMpX();
               break;
            case 80002857:
               this.BossDamage += eff.getBdR();
               if (chra.getParty() != null) {
                  a = eff.getX() * chra.getParty().getMembers().size();
                  if (a >= eff.getY()) {
                     a = eff.getY();
                  }

                  this.DamagePercent += (double)a;
               } else {
                  this.DamagePercent += (double)eff.getX();
               }
            }
         }
      }

      bx = SkillFactory.getSkill(10000250);
      bof = chra.getTotalSkillLevel(10000250);
      if (bof > 0) {
         eff = bx.getEffect(bof);
         this.percent_atk += eff.getPadR();
         this.percent_matk += eff.getPadR();
      }

      Iterator var30 = chra.getSkills().keySet().iterator();

      while(var30.hasNext()) {
         Skill sk = (Skill)var30.next();
         if (sk.getName().equals("연합의 의지")) {
            this.localstr += sk.getEffect(chra.getTotalSkillLevel(sk)).getStrX();
            this.localdex += sk.getEffect(chra.getTotalSkillLevel(sk)).getDexX();
            this.localluk += sk.getEffect(chra.getTotalSkillLevel(sk)).getLukX();
            this.localint_ += sk.getEffect(chra.getTotalSkillLevel(sk)).getIntX();
            this.watk += sk.getEffect(chra.getTotalSkillLevel(sk)).getAttackX();
            this.magic += sk.getEffect(chra.getTotalSkillLevel(sk)).getMagicX();
         }
      }

      int n4;
      int j;
      byte mas;
      byte mas;
      int size;
      Item weapon;
      switch(chra.getJob()) {
      case 100:
      case 110:
      case 111:
      case 112:
      case 120:
      case 121:
      case 122:
      case 130:
      case 131:
      case 132:
         bx = SkillFactory.getSkill(1000009);
         bof = chra.getTotalSkillLevel(1000009);
         if (bof > 0) {
            localmaxhp_ += (long)(bx.getEffect(bof).getLv2mhp() * chra.getLevel());
            this.speed += bx.getEffect(bof).getPsdSpeed();
            this.jump += bx.getEffect(bof).getPsdJump();
            this.stance += bx.getEffect(bof).getStanceProp();
         }

         bx = SkillFactory.getSkill(1000003);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.percent_hp += bx.getEffect(bof).getPercentHP();
            this.ppd += bx.getEffect(bof).getPdd();
            this.damAbsorbShieldR += bx.getEffect(bof).getDamAbsorbShieldR();
         }

         bx = SkillFactory.getSkill(1100000);
         bof = chra.getTotalSkillLevel(1100000);
         Item weapon3;
         byte by;
         if (bof > 0) {
            by = chra.getSkillLevel(1120003) <= 0 ? (mas = 0) : SkillFactory.getSkill(1120003).getEffect(chra.getSkillLevel(1120003)).getMastery();
            this.Mastery = by <= 0 ? (double)bx.getEffect(bof).getMastery() : (double)by;
            --this.attackSpeed;
            this.InsertFinalDamage(bx.getEffect(bof).getPdR());
            weapon3 = chra.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
            if (weapon3 != null && weapon3.getItemId() / 1000 == 1412) {
               this.DamagePercent += 5.0D;
            }
         }

         bx = SkillFactory.getSkill(1100009);
         bof = chra.getTotalSkillLevel(1100009);
         if (bof > 0) {
            this.localstr += bx.getEffect(bof).getStrX();
            this.localdex += bx.getEffect(bof).getDexX();
         }

         bx = SkillFactory.getSkill(1110009);
         bof = chra.getTotalSkillLevel(1110009);
         if (bof > 0) {
            this.ApplyStatFinalDamage.put("MS_Stun", bx.getEffect(bof).getX());
            this.ApplyStatFinalDamage.put("MS_Blind", bx.getEffect(bof).getX());
            this.ApplyStatFinalDamage.put("MS_Speed", bx.getEffect(bof).getX());
            this.ApplyStatFinalDamage.put("MS_Freeze", bx.getEffect(bof).getX());
            this.critical_rate = (short)(this.critical_rate + bx.getEffect(bof).getCr());
         }

         bx = SkillFactory.getSkill(1110011);
         bof = chra.getTotalSkillLevel(1110011);
         if (bof > 0) {
            this.ASR += bx.getEffect(bof).getASRRate();
            this.TER += bx.getEffect(bof).getTERRate();
         }

         bx = SkillFactory.getSkill(1120012);
         bof = chra.getTotalSkillLevel(1120012);
         if (bof > 0) {
            this.ignoreMobpdpR.add(Integer.valueOf(bx.getEffect(bof).getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(1120013);
         bof = chra.getTotalSkillLevel(1120013);
         if (bof > 0) {
            this.watk += bx.getEffect(bof).getAttackX();
            if (chra.getTotalSkillLevel(1120047) > 0) {
               this.watk += SkillFactory.getSkill(1120047).getEffect(1).getAttackX();
            }
         }

         bx = SkillFactory.getSkill(1120014);
         bof = chra.getTotalSkillLevel(1120014);
         if (bof > 0) {
            this.stance += bx.getEffect(bof).getStanceProp();
         }

         bx = SkillFactory.getSkill(1120049);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.InsertDamageIncrease(1121008, bx.getEffect(bof).getDAMRate());
         }

         bx = SkillFactory.getSkill(1120046);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.InsertDamageIncrease(1120013, bx.getEffect(bof).getDAMRate());
         }

         bx = SkillFactory.getSkill(1200000);
         bof = chra.getTotalSkillLevel(1200000);
         if (bof > 0) {
            by = chra.getSkillLevel(1220018) <= 0 ? (mas = 0) : SkillFactory.getSkill(1220018).getEffect(chra.getSkillLevel(1220018)).getMastery();
            this.Mastery += by <= 0 ? (double)bx.getEffect(bof).getMastery() : (double)by;
         }

         bx = SkillFactory.getSkill(1200009);
         bof = chra.getTotalSkillLevel(1200009);
         if (bof > 0) {
            this.localstr += bx.getEffect(bof).getStrX();
            this.localdex += bx.getEffect(bof).getDexX();
         }

         bx = SkillFactory.getSkill(1210001);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0 && chra.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10) != null) {
            --this.attackSpeed;
            this.ASR += bx.getEffect(bof).getASRRate();
            this.percent_wdef += bx.getEffect(bof).getX();
            this.percent_mdef += bx.getEffect(bof).getX();
            this.watk += bx.getEffect(bof).getY();
         }

         if ((bof = chra.getTotalSkillLevel(bx = SkillFactory.getSkill(1211011))) > 0) {
            this.InsertDamageIncrease(1201011, bx.getEffect(bof).getDamPlus());
            this.InsertDamageIncrease(1201012, bx.getEffect(bof).getDamPlus());
         }

         bx = SkillFactory.getSkill(1211011);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.InsertDamageIncrease(1201011, bx.getEffect(bof).getDamPlus());
            this.InsertDamageIncrease(1201012, bx.getEffect(bof).getDamPlus());
            this.InsertDamageIncrease(1211008, bx.getEffect(bof).getDamPlus());
         }

         bx = SkillFactory.getSkill(1220017);
         bof = chra.getTotalSkillLevel(1220017);
         if (bof > 0) {
            this.stance += bx.getEffect(bof).getStanceProp();
         }

         bx = SkillFactory.getSkill(1220018);
         bof = chra.getTotalSkillLevel(1220018);
         if (bof > 0) {
            this.wdef += bx.getEffect(bof).getPddX();
            this.critical_rate = (short)(this.critical_rate + bx.getEffect(bof).getCr());
            this.critical_damage += bx.getEffect(bof).getCriticalDamage();
            this.ignoreMobpdpR.add(Integer.valueOf(bx.getEffect(bof).getIgnoreMob()));
            this.InsertFinalDamage(bx.getEffect(bof).getPdR());
            weapon = chra.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
            if (weapon != null) {
               if (weapon.getItemId() / 1000 != 1302) {
                  if (weapon.getItemId() / 1000 != 1402) {
                     if (weapon.getItemId() / 1000 != 1322) {
                        if (weapon.getItemId() / 1000 == 1422) {
                           this.critical_damage = (short)(this.critical_damage + 5);
                           this.ignoreMobpdpR.add(10);
                        }
                     } else {
                        this.critical_damage = (short)(this.critical_damage + 5);
                        this.ignoreMobpdpR.add(10);
                        this.Mastery += 3.0D;
                     }
                  } else {
                     this.critical_damage = (short)(this.critical_damage + 5);
                  }
               } else {
                  this.critical_damage = (short)(this.critical_damage + 6);
                  this.Mastery += 3.0D;
               }
            }
         }

         bx = SkillFactory.getSkill(1220046);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.InsertDamageIncrease(1221009, bx.getEffect(bof).getDAMRate());
         }

         bx = SkillFactory.getSkill(1221011);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.InsertDamageIncrease(1221011, bx.getEffect(bof).getDAMRate());
         }

         bx = SkillFactory.getSkill(1300000);
         bof = chra.getTotalSkillLevel(1300000);
         if (bof > 0) {
            by = chra.getSkillLevel(1320018) <= 0 ? (mas = 0) : SkillFactory.getSkill(1320018).getEffect(chra.getSkillLevel(1320018)).getMastery();
            this.Mastery += by <= 0 ? (double)bx.getEffect(bof).getMastery() : (double)by;
            weapon3 = chra.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
            if (weapon3 != null && weapon3.getItemId() / 1000 == 1432) {
               this.DamagePercent += 5.0D;
            }
         }

         bx = SkillFactory.getSkill(1300009);
         bof = chra.getTotalSkillLevel(1300009);
         if (bof > 0) {
            this.localstr += bx.getEffect(bof).getStrX();
            this.localdex += bx.getEffect(bof).getDexX();
         }

         bx = SkillFactory.getSkill(1311015);
         bof = chra.getTotalSkillLevel(1311015);
         if (bof > 0) {
            this.InsertFinalDamage(bx.getEffect(bof).getPdR());
         }

         bx = SkillFactory.getSkill(1310009);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.critical_rate = (short)(this.critical_rate + bx.getEffect(bof).getCr());
            this.critical_damage += bx.getEffect(bof).getCriticalDamage();
         }

         bx = SkillFactory.getSkill(1310010);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.ASR += bx.getEffect(bof).getASRRate();
            this.TER += bx.getEffect(bof).getTERRate();
         }

         bx = SkillFactory.getSkill(1320017);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.stance += bx.getEffect(bof).getStanceProp();
         }

         bx = SkillFactory.getSkill(1321015);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.ignoreMobpdpR.add(Integer.valueOf(bx.getEffect(bof).getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(1320016);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            if (chra.getStat().getHPPercent() >= eff.getX()) {
               this.critical_rate = (short)(this.critical_rate + eff.getCr());
               this.critical_damage += bx.getEffect(bof).getCriticalDamage();
               short up4 = chra.getBuffedValue(SecondaryStat.Reincarnation) != null && chra.getTotalSkillLevel(1320046) > 0 ? SkillFactory.getSkill(1320046).getEffect(1).getDamage() : 0;
               this.InsertFinalDamage(bx.getEffect(bof).getDamage() + up4);
               this.speed += bx.getEffect(bof).getPsdSpeed();
               bx = SkillFactory.getSkill(1320048);
               bof = chra.getTotalSkillLevel(bx);
               if (bof > 0) {
                  this.critical_rate = (short)(this.critical_rate + bx.getEffect(bof).getCr());
               }
            }
         }

         bx = SkillFactory.getSkill(1320018);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.watk += bx.getEffect(bof).getAttackX();
            this.critical_damage += bx.getEffect(bof).getCriticalDamage();
         }

         bx = SkillFactory.getSkill(1320011);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.InsertDamageIncrease(1301014, bx.getEffect(bof).getDamPlus());
         }

         bx = SkillFactory.getSkill(1320049);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.InsertDamageIncrease(1321013, bx.getEffect(bof).getDAMRate());
         }

         bx = SkillFactory.getSkill(1320050);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.InsertIgIncrease(1321013, bx.getEffect(bof).getIgnoreMob());
         }

         bx = SkillFactory.getSkill(1320051);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.InsertBossDamIncrease(1321013, bx.getEffect(bof).getBdR());
         }
         break;
      case 200:
      case 210:
      case 211:
      case 212:
      case 220:
      case 221:
      case 222:
      case 230:
      case 231:
      case 232:
         bx = SkillFactory.getSkill(2000006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            localmaxmp_ += (long)(bx.getEffect(bof).getLv2mmp() * chra.getLevel());
            this.before_percent_mp += bx.getEffect(bof).getPercentMP();
            --this.attackSpeed;
            weapon = chra.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
            if (weapon != null && weapon.getItemId() / 1000 == 1372) {
               this.critical_rate = (short)(this.critical_rate + 5);
            }
         }

         bx = SkillFactory.getSkill(2100006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            if (chra.getTotalSkillLevel(2121005) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(2121005).getEffect(chra.getSkillLevel(2121005)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)bx.getEffect(bof).getMastery() : (double)mas;
            this.magic += bx.getEffect(bof).getX();
         }

         if ((bof = chra.getTotalSkillLevel(bx = SkillFactory.getSkill(2100007))) > 0) {
            this.localint_ += bx.getEffect(bof).getIntX();
         }

         bx = SkillFactory.getSkill(2111011);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.ASR += bx.getEffect(bof).getASRRate();
         }

         bx = SkillFactory.getSkill(2111008);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.InsertFinalDamage(bx.getEffect(bof).getMdR());
         }

         bx = SkillFactory.getSkill(2110001);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.DamagePercent += (double)bx.getEffect(bof).getDAMRate();
         }

         bx = SkillFactory.getSkill(2110009);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.critical_rate = (short)(this.critical_rate + bx.getEffect(bof).getCr());
            this.critical_damage += bx.getEffect(bof).getCriticalDamage();
         }

         bx = SkillFactory.getSkill(2110000);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.ApplyStatFinalDamage.put("MS_Burn", bx.getEffect(bof).getZ());
            this.ApplyStatFinalDamage.put("MS_Stun", bx.getEffect(bof).getZ());
            this.ApplyStatFinalDamage.put("MS_Speed", bx.getEffect(bof).getZ());
            this.ApplyStatFinalDamage.put("MS_Blind", bx.getEffect(bof).getZ());
         }

         bx = SkillFactory.getSkill(2120010);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.ignoreMobpdpR.add(Integer.valueOf(bx.getEffect(bof).getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(2120012);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.magic += bx.getEffect(bof).getMagicX();
            this.BuffUP_Skill += bx.getEffect(bof).getBufftimeR();
         }

         bx = SkillFactory.getSkill(2120046);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.InsertDamageIncrease(2120010, bx.getEffect(bof).getDAMRate());
         }

         bx = SkillFactory.getSkill(2120049);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.InsertDamageIncrease(2121003, bx.getEffect(bof).getDAMRate());
         }

         bx = SkillFactory.getSkill(2120050);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.InsertIgIncrease(2121003, bx.getEffect(bof).getIgnoreMob());
         }

         bx = SkillFactory.getSkill(2200006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            if (chra.getTotalSkillLevel(2221005) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(2221005).getEffect(chra.getSkillLevel(2221005)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)bx.getEffect(bof).getMastery() : (double)mas;
            this.magic += bx.getEffect(bof).getX();
         }

         if ((bof = chra.getTotalSkillLevel(bx = SkillFactory.getSkill(2200007))) > 0) {
            this.localint_ += bx.getEffect(bof).getIntX();
         }

         bx = SkillFactory.getSkill(2211012);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.ASR += bx.getEffect(bof).getASRRate();
         }

         bx = SkillFactory.getSkill(2210009);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.critical_rate = (short)(this.critical_rate + bx.getEffect(bof).getCr());
            this.critical_damage += bx.getEffect(bof).getCriticalDamage();
         }

         bx = SkillFactory.getSkill(2210000);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.ApplyStatFinalDamage.put("MS_Burn", bx.getEffect(bof).getZ());
            this.ApplyStatFinalDamage.put("MS_Stun", bx.getEffect(bof).getZ());
            this.ApplyStatFinalDamage.put("MS_Speed", bx.getEffect(bof).getZ());
            this.ApplyStatFinalDamage.put("MS_Blind", bx.getEffect(bof).getZ());
         }

         bx = SkillFactory.getSkill(2210001);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.DamagePercent += (double)bx.getEffect(bof).getDAMRate();
         }

         bx = SkillFactory.getSkill(2211008);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.InsertFinalDamage(bx.getEffect(bof).getMdR());
         }

         bx = SkillFactory.getSkill(2220010);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.ignoreMobpdpR.add(Integer.valueOf(bx.getEffect(bof).getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(2220013);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.magic += bx.getEffect(bof).getMagicX();
            this.BuffUP_Skill += bx.getEffect(bof).getBufftimeR();
         }

         bx = SkillFactory.getSkill(2220043);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.InsertDamageIncrease(2211007, bx.getEffect(bof).getDAMRate());
         }

         bx = SkillFactory.getSkill(2220046);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.InsertDamageIncrease(2221006, bx.getEffect(bof).getDAMRate());
         }

         bx = SkillFactory.getSkill(2220049);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.InsertDamageIncrease(2221012, bx.getEffect(bof).getDAMRate());
         }

         bx = SkillFactory.getSkill(2300006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            if (chra.getTotalSkillLevel(2310008) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(2310008).getEffect(chra.getSkillLevel(2310008)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)bx.getEffect(bof).getMastery() : (double)mas;
            this.magic += bx.getEffect(bof).getX();
         }

         if ((bof = chra.getTotalSkillLevel(bx = SkillFactory.getSkill(2300007))) > 0) {
            this.localint_ += bx.getEffect(bof).getIntX();
         }

         bx = SkillFactory.getSkill(2311012);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.ASR += bx.getEffect(bof).getASRRate();
         }

         bx = SkillFactory.getSkill(2310010);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.critical_rate = (short)(this.critical_rate + bx.getEffect(bof).getCr());
            this.critical_damage += bx.getEffect(bof).getCriticalDamage();
         }

         bx = SkillFactory.getSkill(2310008);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.critical_rate = (short)(this.critical_rate + bx.getEffect(bof).getCr());
         }

         bx = SkillFactory.getSkill(2320011);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.ignoreMobpdpR.add(Integer.valueOf(bx.getEffect(bof).getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(2320012);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.magic += bx.getEffect(bof).getMagicX();
            this.BuffUP_Skill += bx.getEffect(bof).getBufftimeR();
         }

         bx = SkillFactory.getSkill(2321054);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0 && !chra.getBuffedValue(2321054)) {
            this.DamagePercent += (double)bx.getEffect(bof).getZ();
         }

         if (chra.getTotalSkillLevel(SkillFactory.getSkill(2320047)) > 0) {
            this.ASR += 10;
            this.TER += 10;
         }
         break;
      case 300:
      case 310:
      case 311:
      case 312:
      case 320:
      case 321:
      case 322:
         bx = SkillFactory.getSkill(3000001);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.critical_rate += bx.getEffect(bof).getProp();
         }

         bx = SkillFactory.getSkill(3100000);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            --this.attackSpeed;
            if (chra.getTotalSkillLevel(3120005) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(3120005).getEffect(chra.getSkillLevel(3120005)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)bx.getEffect(bof).getMastery() : (double)mas;
         }

         bx = SkillFactory.getSkill(3100006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.localstr += bx.getEffect(bof).getStrX();
            this.localdex += bx.getEffect(bof).getDexX();
         }

         bx = SkillFactory.getSkill(3100002);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.attackSpeed -= 2;
            this.localdex += bx.getEffect(bof).getDexX();
         }

         bx = SkillFactory.getSkill(3111010);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            this.percent_hp += bx.getEffect(bof).getPercentHP();
         }

         bx = SkillFactory.getSkill(3110012);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            this.ASR += bx.getEffect(bof).getASRRate();
            this.TER += bx.getEffect(bof).getTERRate();
         }

         bx = SkillFactory.getSkill(3110014);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            this.ignoreMobpdpR.add(Integer.valueOf(bx.getEffect(bof).getIgnoreMob()));
            this.percent_atk += bx.getEffect(bof).getPadR();
         }

         bx = SkillFactory.getSkill(3121014);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            this.InsertDamageIncrease(3111013, bx.getEffect(bof).getDamPlus());
            this.InsertDamageIncrease(95001000, bx.getEffect(bof).getDamPlus());
         }

         bx = SkillFactory.getSkill(3120005);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            this.watk += bx.getEffect(bof).getX();
            this.critical_damage += bx.getEffect(bof).getCriticalDamage();
         }

         bx = SkillFactory.getSkill(3120007);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            this.localdex += bx.getEffect(bof).getDexX();
         }

         bx = SkillFactory.getSkill(3120008);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            this.watk += bx.getEffect(bof).getAttackX();
         }

         bx = SkillFactory.getSkill(3121020);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            this.InsertDamageIncrease(3121015, bx.getEffect(bof).getDamPlus());
         }

         bx = SkillFactory.getSkill(3120046);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            this.InsertDamageIncrease(3121015, bx.getEffect(bof).getDAMRate());
         }

         bx = SkillFactory.getSkill(3120049);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            this.InsertDamageIncrease(3111013, bx.getEffect(bof).getDAMRate());
            this.InsertDamageIncrease(3121020, bx.getEffect(bof).getDAMRate());
            this.InsertDamageIncrease(95001000, bx.getEffect(bof).getDAMRate());
         }

         bx = SkillFactory.getSkill(3200000);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            if (chra.getTotalSkillLevel(3220004) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(3220004).getEffect(chra.getSkillLevel(3220004)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)bx.getEffect(bof).getMastery() : (double)mas;
            this.InsertFinalDamage(bx.getEffect(bof).getPdR());
         }

         if ((bof = chra.getTotalSkillLevel(bx = SkillFactory.getSkill(3200006))) > 0) {
            this.localstr += bx.getEffect(bof).getStrX();
            this.localdex += bx.getEffect(bof).getDexX();
         }

         bx = SkillFactory.getSkill(3211010);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            this.percent_hp += bx.getEffect(bof).getPercentHP();
         }

         bx = SkillFactory.getSkill(3211011);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            this.ASR += bx.getEffect(bof).getASRRate();
            this.TER += bx.getEffect(bof).getTERRate();
         }

         bx = SkillFactory.getSkill(3210015);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            this.ignoreMobpdpR.add(Integer.valueOf(bx.getEffect(bof).getIgnoreMob()));
            this.DamagePercent += (double)bx.getEffect(bof).getDAMRate();
         }

         bx = SkillFactory.getSkill(3220004);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            this.watk += bx.getEffect(bof).getX();
            this.critical_damage += bx.getEffect(bof).getCriticalDamage();
         }

         bx = SkillFactory.getSkill(3220006);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            this.localdex += bx.getEffect(bof).getDexX();
         }

         bx = SkillFactory.getSkill(3220015);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            this.InsertFinalDamage(bx.getEffect(bof).getPdR());
         }

         bx = SkillFactory.getSkill(3220049);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.InsertDamageIncrease(3221007, bx.getEffect(bof).getDAMRate());
         }

         bx = SkillFactory.getSkill(3220050);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.InsertDamageIncrease(3221007, bx.getEffect(bof).getBdR());
         }
         break;
      case 301:
      case 330:
      case 331:
      case 332:
         bx = SkillFactory.getSkill(3010002);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.critical_rate += bx.getEffect(bof).getProp();
         }

         bx = SkillFactory.getSkill(3300006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            --this.attackSpeed;
            if (chra.getTotalSkillLevel(3320010) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(3320010).getEffect(chra.getSkillLevel(3320010)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)bx.getEffect(bof).getMastery() : (double)mas;
            this.watk += bx.getEffect(bof).getAttackX();
         }

         bx = SkillFactory.getSkill(3300007);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.localstr += bx.getEffect(bof).getStrX();
            this.localdex += bx.getEffect(bof).getDexX();
         }

         bx = SkillFactory.getSkill(3310006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.percent_hp += bx.getEffect(bof).getPercentHP();
         }

         bx = SkillFactory.getSkill(3311012);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.ASR += bx.getEffect(bof).getASRRate();
            this.TER += bx.getEffect(bof).getTERRate();
         }

         bx = SkillFactory.getSkill(3310008);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.critical_rate = (short)(this.critical_rate + bx.getEffect(bof).getCr());
            this.DamagePercent += (double)bx.getEffect(bof).getDAMRate();
            this.ignoreMobpdpR.add(Integer.valueOf(bx.getEffect(bof).getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(3320008);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.percent_atk += bx.getEffect(bof).getPadR();
         }

         bx = SkillFactory.getSkill(3320010);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.watk += bx.getEffect(bof).getX();
            this.critical_damage += bx.getEffect(bof).getCriticalDamage();
         }

         bx = SkillFactory.getSkill(3320011);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.localdex += bx.getEffect(bof).getDexX();
         }
         break;
      case 400:
      case 410:
      case 411:
      case 412:
      case 420:
      case 421:
      case 422:
         bx = SkillFactory.getSkill(4000000);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.localluk += bx.getEffect(bof).getLukX();
         }

         if (chra.getJob() >= 410 && chra.getJob() <= 412) {
            bx = SkillFactory.getSkill(4100000);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               mas = 0;
               if (chra.getTotalSkillLevel(4120012) <= 0) {
                  mas = 0;
               } else {
                  SkillFactory.getSkill(4120012).getEffect(chra.getSkillLevel(4120012)).getMastery();
               }

               this.Mastery += mas <= 0 ? (double)bx.getEffect(bof).getMastery() : (double)mas;
            }

            if ((bof = chra.getTotalSkillLevel(bx = SkillFactory.getSkill(4100001))) > 0) {
               this.critical_rate += bx.getEffect(bof).getProp();
               this.critical_damage += bx.getEffect(bof).getCriticalDamage();
            }

            bx = SkillFactory.getSkill(4100007);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.localluk += eff.getLukX();
               this.localdex += eff.getDexX();
            }

            bx = SkillFactory.getSkill(4110008);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.percent_hp += eff.getPercentHP();
               this.ASR += eff.getASRRate();
               this.TER += eff.getTERRate();
            }

            bx = SkillFactory.getSkill(4110012);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.InsertFinalDamage(eff.getPdR());
            }

            bx = SkillFactory.getSkill(4110014);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.RecoveryUP += eff.getX() - 100;
               this.critical_damage += eff.getCriticalDamage();
            }

            bx = SkillFactory.getSkill(4121015);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.BossDamage += eff.getBdR();
            }

            bx = SkillFactory.getSkill(4120014);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.watk += eff.getAttackX();
               this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
            }

            bx = SkillFactory.getSkill(4120012);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.watk += eff.getX();
               this.critical_damage += eff.getCriticalDamage();
            }

            bx = SkillFactory.getSkill(4120043);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.InsertDamageIncrease(4121017, eff.getDAMRate());
            }

            bx = SkillFactory.getSkill(4120049);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.InsertDamageIncrease(4121013, eff.getDAMRate());
            }

            bx = SkillFactory.getSkill(4120050);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.InsertBossDamIncrease(4121013, eff.getBdR());
            }
         } else if (chra.getJob() >= 420 && chra.getJob() <= 422) {
            bx = SkillFactory.getSkill(4200000);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               mas = 0;
               if (chra.getTotalSkillLevel(4220012) <= 0) {
                  mas = 0;
               } else {
                  SkillFactory.getSkill(4220012).getEffect(chra.getSkillLevel(4220012)).getMastery();
               }

               this.Mastery += mas <= 0 ? (double)bx.getEffect(bof).getMastery() : (double)mas;
            }

            if ((bof = chra.getTotalSkillLevel(bx = SkillFactory.getSkill(4200007))) > 0) {
               eff = bx.getEffect(bof);
               this.localluk += eff.getLukX();
               this.localdex += eff.getDexX();
            }

            bx = SkillFactory.getSkill(4200009);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               this.watk += bx.getEffect(bof).getAttackX();
            }

            bx = SkillFactory.getSkill(4200010);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0 && chra.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10) != null) {
               this.watk += bx.getEffect(bof).getY();
            }

            if ((bof = chra.getTotalSkillLevel(bx = SkillFactory.getSkill(4210012))) > 0) {
               eff = bx.getEffect(bof);
               this.watk += eff.getAttackX();
               this.mesoBuff *= ((double)eff.getMesoRate() + 100.0D) / 100.0D;
               this.pickRate += eff.getU();
               this.InsertDamageIncrease(4211006, eff.getX());
            }

            bx = SkillFactory.getSkill(4210013);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.ASR += eff.getASRRate();
               this.TER += eff.getTERRate();
               this.percent_hp += eff.getPercentHP();
            }

            bx = SkillFactory.getSkill(4221007);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.InsertFinalDamage(eff.getPdR());
            }

            bx = SkillFactory.getSkill(4221018);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.localluk += eff.getLukX();
               this.critical_damage += eff.getCriticalDamage();
            }

            bx = SkillFactory.getSkill(4221013);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
            }

            bx = SkillFactory.getSkill(4220012);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.watk += eff.getX();
               this.critical_damage += eff.getCriticalDamage();
            }

            bx = SkillFactory.getSkill(4220043);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.InsertDamageIncrease(4210014, eff.getDAMRate());
            }

            bx = SkillFactory.getSkill(4220043);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.InsertIgIncrease(4210014, eff.getIgnoreMob());
            }

            bx = SkillFactory.getSkill(4220046);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.InsertDamageIncrease(4221007, eff.getDAMRate());
            }

            bx = SkillFactory.getSkill(4220049);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.InsertDamageIncrease(4221014, eff.getDAMRate());
               this.InsertDamageIncrease(4221016, eff.getDAMRate());
            }

            bx = SkillFactory.getSkill(4220050);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.InsertBossDamIncrease(4221014, eff.getBdR());
               this.InsertBossDamIncrease(4221016, eff.getBdR());
            }

            bx = SkillFactory.getSkill(4220051);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.InsertIgIncrease(4221014, eff.getIgnoreMob());
               this.InsertIgIncrease(4221016, eff.getIgnoreMob());
            }
         }
         break;
      case 430:
      case 431:
      case 432:
      case 433:
      case 434:
         bx = SkillFactory.getSkill(4300000);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(4340013) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(4340013).getEffect(chra.getSkillLevel(4340013)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
         }

         bx = SkillFactory.getSkill(4310005);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(4310006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localluk += eff.getLukX();
            this.localdex += eff.getDexX();
         }

         bx = SkillFactory.getSkill(4331000);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertDamageIncrease(4301004, eff.getDAMRate());
            this.InsertDamageIncrease(4321006, eff.getDAMRate());
         }

         bx = SkillFactory.getSkill(4330008);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
            this.ASR += eff.getASRRate();
            this.TER += eff.getTERRate();
         }

         bx = SkillFactory.getSkill(4340007);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
            this.stance += eff.getStanceProp();
         }

         bx = SkillFactory.getSkill(4340010);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(4340013);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getX();
            this.InsertFinalDamage(eff.getPdR());
         }

         bx = SkillFactory.getSkill(4340043);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertDamageIncrease(4331000, eff.getDAMRate());
         }

         bx = SkillFactory.getSkill(4340046);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertDamageIncrease(4341009, eff.getDAMRate());
         }

         bx = SkillFactory.getSkill(4340047);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertIgIncrease(4341009, eff.getIgnoreMob());
         }

         bx = SkillFactory.getSkill(4340055);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertDamageIncrease(4341004, eff.getDAMRate());
         }

         bx = SkillFactory.getSkill(4340056);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertIgIncrease(4341004, eff.getIgnoreMob());
         }
         break;
      case 500:
      case 510:
      case 511:
      case 512:
      case 520:
      case 521:
      case 522:
         bx = SkillFactory.getSkill(5000007);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.critical_damage += eff.getCriticalDamage();
         }

         if (!GameConstants.isViper(chra.getJob())) {
            if (GameConstants.isCaptain(chra.getJob())) {
               bx = SkillFactory.getSkill(5200000);
               bof = chra.getTotalSkillLevel(bx);
               if (bof > 0) {
                  mas = 0;
                  eff = bx.getEffect(bof);
                  if (chra.getTotalSkillLevel(5220020) <= 0) {
                     mas = 0;
                  } else {
                     SkillFactory.getSkill(5220020).getEffect(chra.getSkillLevel(5220020)).getMastery();
                  }

                  this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
               }

               bx = SkillFactory.getSkill(5200009);
               bof = chra.getTotalSkillLevel(bx);
               if (bof > 0) {
                  eff = bx.getEffect(bof);
                  this.localstr += eff.getStrX();
                  this.localdex += eff.getDexX();
               }

               bx = SkillFactory.getSkill(5210009);
               bof = chra.getTotalSkillLevel(bx);
               if (bof > 0) {
                  eff = bx.getEffect(bof);
                  this.watk += eff.getAttackX();
               }

               bx = SkillFactory.getSkill(5210012);
               bof = chra.getTotalSkillLevel(bx);
               if (bof > 0) {
                  eff = bx.getEffect(bof);
                  localmaxhp_ += (long)eff.getMaxHpX();
                  localmaxmp_ += (long)eff.getMaxMpX();
                  this.percent_wdef += eff.getWDEFRate();
                  this.percent_mdef += eff.getMDEFRate();
               }

               bx = SkillFactory.getSkill(5210013);
               bof = chra.getTotalSkillLevel(bx);
               if (bof > 0) {
                  eff = bx.getEffect(bof);
                  this.InsertFinalDamage(eff.getPdR());
                  this.critical_rate = (short)(this.critical_rate + eff.getCr());
                  this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
               }

               bx = SkillFactory.getSkill(5221015);
               bof = chra.getTotalSkillLevel(bx);
               if (bof > 0) {
                  eff = bx.getEffect(bof);
                  this.critical_damage += eff.getCriticalDamage();
               }

               bx = SkillFactory.getSkill(5221017);
               bof = chra.getTotalSkillLevel(bx);
               if (bof > 0) {
                  eff = bx.getEffect(bof);
                  this.InsertDamageIncrease(5221013, eff.getDamPlus());
               }

               bx = SkillFactory.getSkill(5220019);
               bof = chra.getTotalSkillLevel(bx);
               if (bof > 0) {
                  eff = bx.getEffect(bof);
                  localmaxhp_ += (long)eff.getMhpX();
                  this.critical_damage += eff.getCriticalDamage();
               }

               bx = SkillFactory.getSkill(5220020);
               bof = chra.getTotalSkillLevel(bx);
               if (bof > 0) {
                  eff = bx.getEffect(bof);
                  this.watk += eff.getAttackX();
               }

               bx = SkillFactory.getSkill(5220046);
               bof = chra.getTotalSkillLevel(bx);
               if (bof > 0) {
                  eff = bx.getEffect(bof);
                  this.InsertDamageIncrease(5221016, eff.getDAMRate());
               }

               bx = SkillFactory.getSkill(5220048);
               bof = chra.getTotalSkillLevel(bx);
               if (bof > 0) {
                  eff = bx.getEffect(bof);
                  this.InsertDamageIncrease(5221016, eff.getBdR());
               }

               bx = SkillFactory.getSkill(5220049);
               bof = chra.getTotalSkillLevel(bx);
               if (bof > 0) {
                  eff = bx.getEffect(bof);
                  this.InsertDamageIncrease(5221004, eff.getDAMRate());
               }

               bx = SkillFactory.getSkill(5220051);
               bof = chra.getTotalSkillLevel(bx);
               if (bof > 0) {
                  eff = bx.getEffect(bof);
                  this.InsertDamageIncrease(5221004, eff.getBdR());
               }

               bx = SkillFactory.getSkill(5221054);
               bof = chra.getTotalSkillLevel(bx);
               if (bof > 0) {
                  eff = bx.getEffect(bof);
                  this.ASR += eff.getASRRate();
                  this.TER += eff.getTERRate();
                  this.critical_rate = (short)(this.critical_rate + eff.getCr());
               }
            }
         } else {
            bx = SkillFactory.getSkill(5100011);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.watk += eff.getAttackX();
            }

            bx = SkillFactory.getSkill(5100001);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               mas = 0;
               eff = bx.getEffect(bof);
               if (chra.getTotalSkillLevel(5121015) <= 0) {
                  mas = 0;
               } else {
                  SkillFactory.getSkill(5121015).getEffect(chra.getSkillLevel(5121015)).getMastery();
               }

               this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
            }

            bx = SkillFactory.getSkill(5100009);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.percent_hp += eff.getPercentHP();
               this.stance += eff.getStanceProp();
            }

            bx = SkillFactory.getSkill(5100010);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.localstr += eff.getStrX();
               this.localdex += eff.getDexX();
            }

            bx = SkillFactory.getSkill(5111009);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.InsertDamageIncrease(5101004, eff.getDAMRate());
            }

            bx = SkillFactory.getSkill(5110011);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.critical_rate = (short)(this.critical_rate + eff.getCr());
               this.critical_damage += eff.getCriticalDamage();
            }

            bx = SkillFactory.getSkill(5121016);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               if (chra.getBuffedEffect(SecondaryStat.EnergyCharged) != null) {
                  this.InsertDamageIncrease(5121017, eff.getDAMRate());
               }
            }

            bx = SkillFactory.getSkill(5121015);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.ASR += eff.getASRRate();
               this.TER += eff.getTERRate();
            }

            bx = SkillFactory.getSkill(5120011);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.InsertDamageIncrease(5101004, eff.getDAMRate());
               this.InsertDamageIncrease(5111009, eff.getDAMRate());
            }

            bx = SkillFactory.getSkill(5120046);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.InsertDamageIncrease(5121007, eff.getDAMRate());
               this.InsertDamageIncrease(5121020, eff.getDAMRate());
            }

            bx = SkillFactory.getSkill(5120047);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.InsertBossDamIncrease(5121007, eff.getBdR());
               this.InsertBossDamIncrease(5121020, eff.getBdR());
            }

            bx = SkillFactory.getSkill(5120049);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.InsertDamageIncrease(5121016, eff.getDAMRate());
               this.InsertDamageIncrease(5121017, eff.getDAMRate());
            }

            bx = SkillFactory.getSkill(5121054);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.BossDamage += eff.getBdR();
            }
         }
         break;
      case 501:
      case 530:
      case 531:
      case 532:
         bx = SkillFactory.getSkill(5010003);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.watk += bx.getEffect(bof).getAttackX();
         }

         bx = SkillFactory.getSkill(5300004);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(5300005);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(5320009) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(5320009).getEffect(chra.getSkillLevel(5320009)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
         }

         bx = SkillFactory.getSkill(5300008);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localstr += eff.getStrX();
            this.localdex += eff.getDexX();
         }

         bx = SkillFactory.getSkill(5311002);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
         }

         bx = SkillFactory.getSkill(5311004);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getPdR());
         }

         bx = SkillFactory.getSkill(5310006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.watk += bx.getEffect(bof).getAttackX();
            --this.attackSpeed;
         }

         bx = SkillFactory.getSkill(5310007);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
            this.ASR += eff.getASRRate();
            this.percent_wdef += eff.getWDEFRate();
         }

         bx = SkillFactory.getSkill(5321010);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.BossDamage += eff.getBdR();
         }

         bx = SkillFactory.getSkill(5320009);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getPdR());
            --this.attackSpeed;
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(5320046);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertDamageIncrease(5321000, eff.getDAMRate());
         }

         bx = SkillFactory.getSkill(5320049);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertDamageIncrease(5321012, eff.getDAMRate());
         }
         break;
      case 1100:
      case 1110:
      case 1111:
      case 1112:
         bx = SkillFactory.getSkill(11000021);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(11000023);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
         }

         bx = SkillFactory.getSkill(11001022);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(11100000);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.percent_hp += bx.getEffect(bof).getPercentHP();
         }

         bx = SkillFactory.getSkill(11100023);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(11100025);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(11120007) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(11120007).getEffect(chra.getSkillLevel(11120007)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
         }

         bx = SkillFactory.getSkill(11100026);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localstr += eff.getStrX();
            this.localdex += eff.getDexX();
         }

         bx = SkillFactory.getSkill(11110024);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            localmaxhp_ += (long)bx.getEffect(bof).getMaxHpX();
         }

         bx = SkillFactory.getSkill(11110025);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ASR += eff.getASRRate();
            this.TER += eff.getTERRate();
         }

         bx = SkillFactory.getSkill(11110026);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
            this.localstr += eff.getStrX();
         }

         bx = SkillFactory.getSkill(11120006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.localstr += eff.getStrX();
            this.localdex += eff.getStrX();
            this.localint_ += eff.getStrX();
            this.localluk += eff.getStrX();
            this.stance += eff.getStanceProp();
         }

         bx = SkillFactory.getSkill(11120007);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getX();
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(11120008);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
            this.BossDamage += eff.getBdR();
         }

         bx = SkillFactory.getSkill(11120046);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertDamageIncrease(11121103, eff.getDAMRate());
            this.InsertDamageIncrease(11121203, eff.getDAMRate());
         }

         bx = SkillFactory.getSkill(11120049);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertDamageIncrease(11121101, eff.getDAMRate());
            this.InsertDamageIncrease(11121102, eff.getDAMRate());
            this.InsertDamageIncrease(11121201, eff.getDAMRate());
            this.InsertDamageIncrease(11121202, eff.getDAMRate());
         }

         bx = SkillFactory.getSkill(11120050);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertIgIncrease(11121101, eff.getIgnoreMob());
            this.InsertIgIncrease(11121102, eff.getIgnoreMob());
            this.InsertIgIncrease(11121201, eff.getIgnoreMob());
            this.InsertIgIncrease(11121202, eff.getIgnoreMob());
         }

         bx = SkillFactory.getSkill(11120051);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertIgIncrease(11121101, eff.getBdR());
            this.InsertIgIncrease(11121102, eff.getBdR());
            this.InsertIgIncrease(11121201, eff.getBdR());
            this.InsertIgIncrease(11121202, eff.getBdR());
         }
         break;
      case 1200:
      case 1210:
      case 1211:
      case 1212:
         bx = SkillFactory.getSkill(12000025);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.before_percent_mp += bx.getEffect(bof).getPercentMP();
            localmaxmp_ += (long)(bx.getEffect(bof).getLv2mmp() * chra.getLevel());
            weapon = chra.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
            if (weapon != null && weapon.getItemId() / 1000 == 1372) {
               this.critical_rate = (short)(this.critical_rate + 5);
            }
         }

         bx = SkillFactory.getSkill(12100027);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(12120009) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(12120009).getEffect(chra.getSkillLevel(12120009)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
            this.magic += eff.getX();
         }

         bx = SkillFactory.getSkill(12110025);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getZ());
         }

         bx = SkillFactory.getSkill(12110026);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(12110027);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localint_ += eff.getIntX();
         }

         bx = SkillFactory.getSkill(12120009);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.magic += eff.getX();
            this.InsertFinalDamage(eff.getMdR());
         }

         bx = SkillFactory.getSkill(12120008);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ASR += eff.getASRRate();
            this.TER += eff.getTERRate();
         }

         bx = SkillFactory.getSkill(12120044);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertIgIncrease(12120010, eff.getIgnoreMob());
         }
         break;
      case 1300:
      case 1310:
      case 1311:
      case 1312:
         bx = SkillFactory.getSkill(13000023);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(13100025);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(13120006) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(13120006).getEffect(chra.getSkillLevel(13120006)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
         }

         bx = SkillFactory.getSkill(13100026);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localstr += eff.getStrX();
            this.localdex += eff.getDexX();
         }

         bx = SkillFactory.getSkill(13110025);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ASR += eff.getASRRate();
            this.TER += eff.getTERRate();
         }

         bx = SkillFactory.getSkill(13121002);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertDamageIncrease(13111020, eff.getDamPlus());
         }

         bx = SkillFactory.getSkill(13100023);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.attackSpeed -= 2;
            this.localdex += eff.getDexX();
         }

         bx = SkillFactory.getSkill(13111023);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
            localmaxhp_ += (long)eff.getMhpX();
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            --this.attackSpeed;
         }

         bx = SkillFactory.getSkill(13120008);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
            this.DamagePercent += (double)eff.getDAMRate();
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.ASR += eff.getASRRate();
            this.TER += eff.getTERRate();
         }

         bx = SkillFactory.getSkill(13120004);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_atk += eff.getPadR();
            this.percent_dex += eff.getDexR();
            this.percent_hp += eff.getPercentHP();
         }

         bx = SkillFactory.getSkill(13120006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getX();
            this.InsertFinalDamage(eff.getPdR());
            this.critical_damage += eff.getCriticalDamage();
            this.BossDamage += eff.getBdR();
         }

         bx = SkillFactory.getSkill(13120043);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertDamageIncrease(13120003, eff.getDAMRate());
            this.InsertDamageIncrease(13120010, eff.getDAMRate());
         }

         bx = SkillFactory.getSkill(13120046);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertDamageIncrease(13121002, eff.getDAMRate());
         }

         bx = SkillFactory.getSkill(13120049);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertDamageIncrease(13121001, eff.getDAMRate());
         }

         bx = SkillFactory.getSkill(13120050);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertIgIncrease(13121001, eff.getIgnoreMob());
         }

         bx = SkillFactory.getSkill(13120051);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertBossDamIncrease(13121001, eff.getBdR());
         }
         break;
      case 1400:
      case 1410:
      case 1411:
      case 1412:
         bx = SkillFactory.getSkill(14001021);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getPdR());
         }

         bx = SkillFactory.getSkill(14100023);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(14120005) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(14120005).getEffect(chra.getSkillLevel(14120005)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
            this.DamagePercent += (double)eff.getDAMRate();
         }

         bx = SkillFactory.getSkill(14100024);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.critical_rate += eff.getProp();
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(14100025);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localluk += eff.getLukX();
         }

         bx = SkillFactory.getSkill(14110026);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ASR += eff.getASRRate();
            this.TER += eff.getTERRate();
         }

         bx = SkillFactory.getSkill(14110027);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.RecoveryUP += eff.getX() - 100;
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(14120005);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getX();
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(14120006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(14120043);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertDamageIncrease(14121001, eff.getDAMRate());
            this.InsertDamageIncrease(14121002, eff.getDAMRate());
         }

         bx = SkillFactory.getSkill(14120044);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertBossDamIncrease(14121001, eff.getBdR());
            this.InsertBossDamIncrease(14121002, eff.getBdR());
         }

         bx = SkillFactory.getSkill(14120048);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertBossDamIncrease(14121003, eff.getDAMRate());
         }
         break;
      case 1500:
      case 1510:
      case 1511:
      case 1512:
         bx = SkillFactory.getSkill(15100023);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(15120006) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(15120006).getEffect(chra.getSkillLevel(15120006)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
         }

         bx = SkillFactory.getSkill(15100024);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localstr += eff.getStrX();
         }

         bx = SkillFactory.getSkill(15100025);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(15110023);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ASR += eff.getASRRate();
            this.TER += eff.getTERRate();
         }

         bx = SkillFactory.getSkill(15110024);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.DamagePercent += (double)eff.getDAMRate();
         }

         bx = SkillFactory.getSkill(15110026);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(15120006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getX();
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(15120007);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
         }

         bx = SkillFactory.getSkill(15120008);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(15120043);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertDamageIncrease(15111022, eff.getDAMRate());
            this.InsertDamageIncrease(15120003, eff.getDAMRate());
         }

         bx = SkillFactory.getSkill(15120046);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertDamageIncrease(15121002, eff.getDAMRate());
         }

         bx = SkillFactory.getSkill(15120049);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertDamageIncrease(15121001, eff.getDAMRate());
         }

         bx = SkillFactory.getSkill(15120050);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertIgIncrease(15121001, eff.getIgnoreMob());
         }

         bx = SkillFactory.getSkill(15120051);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertBossDamIncrease(15121001, eff.getBdR());
         }

         bx = SkillFactory.getSkill(15121054);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getPdR());
         }
         break;
      case 2002:
      case 2300:
      case 2310:
      case 2311:
      case 2312:
         bx = SkillFactory.getSkill(20020112);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            --this.attackSpeed;
            this.NomarbdR += eff.getNbdR();
         }

         bx = SkillFactory.getSkill(23000001);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.DamagePercent += (double)eff.getDAMRate();
         }

         bx = SkillFactory.getSkill(23000003);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
         }

         bx = SkillFactory.getSkill(23100003);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.DamagePercent += (double)eff.getDAMRate();
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
         }

         bx = SkillFactory.getSkill(23100005);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(23120009) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(23120009).getEffect(chra.getSkillLevel(23120009)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
         }

         bx = SkillFactory.getSkill(23100008);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localstr += eff.getStrX();
            this.localdex += eff.getDexX();
         }

         bx = SkillFactory.getSkill(23110006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDamPlus());
            }
         }

         bx = SkillFactory.getSkill(23110004);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getPdR());
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(23121002);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDamPlus());
            }
         }

         bx = SkillFactory.getSkill(23121003);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDamPlus());
            }
         }

         bx = SkillFactory.getSkill(23121011);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDamPlus());
            }
         }

         bx = SkillFactory.getSkill(23120009);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getX();
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(23120010);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
            this.InsertFinalDamage(eff.getPdR());
            this.BossDamage += eff.getBdR();
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(23120012);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(23120043);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(23120044);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertIgIncrease(i95, eff.getIgnoreMob());
            }
         }

         bx = SkillFactory.getSkill(23120045);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertBossDamIncrease(i95, eff.getBdR());
            }
         }

         bx = SkillFactory.getSkill(23120049);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(23120051);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }
         break;
      case 2003:
      case 2400:
      case 2410:
      case 2411:
      case 2412:
         bx = SkillFactory.getSkill(20030204);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
         }

         bx = SkillFactory.getSkill(20030206);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localdex += eff.getDexX();
         }

         bx = SkillFactory.getSkill(24100004);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(24120006) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(24120006).getEffect(chra.getSkillLevel(24120006)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
         }

         bx = SkillFactory.getSkill(24100006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localluk += eff.getLukX();
         }

         bx = SkillFactory.getSkill(24111006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(24111002);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localluk += eff.getLukX();
         }

         bx = SkillFactory.getSkill(24110003);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
            this.percent_mp += eff.getPercentMP();
            this.ASR += eff.getASRRate();
            this.TER += eff.getTERRate();
         }

         bx = SkillFactory.getSkill(24110005);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(24110007);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.InsertFinalDamage(eff.getPdR());
         }

         bx = SkillFactory.getSkill(24121003);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(24121004);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.DamagePercent += (double)eff.getDAMRate();
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(24120006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
            this.critical_damage += eff.getCriticalDamage();
            this.InsertFinalDamage(eff.getPdR());
         }

         bx = SkillFactory.getSkill(24120043);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(24120046);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(24120048);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertIgIncrease(i95, eff.getIgnoreMob());
            }
         }
         break;
      case 2100:
      case 2110:
      case 2111:
      case 2112:
         bx = SkillFactory.getSkill(20000194);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_atk += eff.getPadR();
         }

         bx = SkillFactory.getSkill(21000000);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.stance += eff.getStanceProp();
            if (chra.getCombo() >= 50) {
               size = chra.getCombo() / 50;
               if (size >= eff.getX()) {
                  size = eff.getX();
               }

               a = chra.getSkillLevel(21110000) <= 0 ? (mas = 0) : SkillFactory.getSkill(21110000).getEffect(chra.getSkillLevel(21110000)).getZ();
               n4 = chra.getSkillLevel(21110000) <= 0 ? 0 : SkillFactory.getSkill(21110000).getEffect(chra.getSkillLevel(21110000)).getY();
               this.watk += size * (eff.getY() + a);
               this.critical_rate = (short)(this.critical_rate + size * n4);
            }
         }

         bx = SkillFactory.getSkill(21101005);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
         }

         bx = SkillFactory.getSkill(21100000);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(21120001) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(21120001).getEffect(chra.getSkillLevel(21120001)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
            --this.attackSpeed;
         }

         bx = SkillFactory.getSkill(21100008);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localstr += eff.getStrX();
            this.localdex += eff.getDexX();
         }

         bx = SkillFactory.getSkill(21110000);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.stance += eff.getStanceProp();
            this.watk += eff.getAttackX();
            this.ASR += eff.getASRRate();
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(21101006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.DamagePercent += (double)eff.getDAMRate();
         }

         bx = SkillFactory.getSkill(21110029);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(21110010);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
            this.DamagePercent += (double)eff.getDAMRate();
         }

         bx = SkillFactory.getSkill(21120001);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getX();
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(21120004);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
         }

         bx = SkillFactory.getSkill(21120011);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDamPlus());
            }
         }

         bx = SkillFactory.getSkill(21120012);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(21120059);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDamPlus());
            }
         }

         bx = SkillFactory.getSkill(21120061);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(21120063);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(21120065);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(21120067);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertIgIncrease(i95, eff.getIgnoreMob());
            }
         }
         break;
      case 2200:
      case 2210:
      case 2211:
      case 2212:
      case 2213:
      case 2214:
      case 2215:
      case 2216:
      case 2217:
      case 2218:
         bx = SkillFactory.getSkill(20010194);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.magic += eff.getMagicX();
            this.localstr += eff.getStrX();
            this.localdex += eff.getStrX();
            this.localluk += eff.getStrX();
            this.localint_ += eff.getStrX();
         }

         bx = SkillFactory.getSkill(22000014);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.before_percent_mp += eff.getPercentMP();
            localmaxmp_ += (long)(eff.getLv2mmp() * chra.getLevel());
            this.magic += eff.getMagicX();
         }

         bx = SkillFactory.getSkill(22110015);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localint_ += eff.getIntX();
         }

         bx = SkillFactory.getSkill(22110016);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.stance += eff.getStanceProp();
         }

         bx = SkillFactory.getSkill(22110018);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(22170071) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(22170071).getEffect(chra.getSkillLevel(22170071)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
            this.magic += eff.getX();
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            Item weapon = chra.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
            if (weapon != null && weapon.getItemId() / 1000 == 1372) {
               this.critical_rate = (short)(this.critical_rate + 5);
            }
         }

         bx = SkillFactory.getSkill(22141016);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getMdR());
         }

         bx = SkillFactory.getSkill(22140018);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.critical_rate += eff.getProp();
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(22140019);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ASR += eff.getASRRate();
            this.TER += eff.getTERRate();
         }

         bx = SkillFactory.getSkill(22140020);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.mpconPercent += eff.getX() - 100;
            this.InsertFinalDamage(eff.getZ());
         }

         bx = SkillFactory.getSkill(22140021);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(22170071);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.magic += eff.getX();
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(22170074);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            if (this.getMPPercent() >= eff.getX() && this.getMPPercent() <= eff.getY()) {
               this.percent_matk += eff.getDamage();
            }
         }

         bx = SkillFactory.getSkill(22170075);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.BossDamage += eff.getBdR();
         }

         bx = SkillFactory.getSkill(22170089);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(400021095);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.magic += eff.getMagicX();
         }
         break;
      case 2500:
      case 2510:
      case 2511:
      case 2512:
         bx = SkillFactory.getSkill(20050074);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.BuffUP_Summon += eff.getSummonTimeR();
         }

         bx = SkillFactory.getSkill(20050285);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.stance += eff.getStanceProp();
         }

         bx = SkillFactory.getSkill(25000105);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
            this.percent_mp += eff.getPercentMP();
         }

         bx = SkillFactory.getSkill(25100106);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(25120113) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(25120113).getEffect(chra.getSkillLevel(25120113)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
         }

         bx = SkillFactory.getSkill(25100107);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            bx.getEffect(bof);
            --this.attackSpeed;
         }

         bx = SkillFactory.getSkill(25100108);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localstr += eff.getStrX();
         }

         bx = SkillFactory.getSkill(25110108);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ASR += eff.getASRRate();
            this.TER += eff.getTERRate();
         }

         bx = SkillFactory.getSkill(25110107);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.DamagePercent += (double)eff.getDAMRate();
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(25120112);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
            this.InsertFinalDamage(eff.getPdR());
            this.BossDamage += eff.getBdR();
         }

         bx = SkillFactory.getSkill(25120113);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.critical_damage += eff.getCriticalDamage();
            this.InsertFinalDamage(eff.getPdR());
         }

         bx = SkillFactory.getSkill(25120214);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
         }

         bx = SkillFactory.getSkill(25120146);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(25120147);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertBossDamIncrease(i95, eff.getBdR());
            }
         }

         bx = SkillFactory.getSkill(25120149);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(25120152);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }
         break;
      case 2700:
      case 2710:
      case 2711:
      case 2712:
         bx = SkillFactory.getSkill(20040221);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localint_ += eff.getIntX();
         }

         bx = SkillFactory.getSkill(20040218);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(27000004);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_mp += eff.getPercentMP();
         }

         bx = SkillFactory.getSkill(27100005);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(27120007) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(27120007).getEffect(chra.getSkillLevel(27120007)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
            this.magic += eff.getX();
            this.DamagePercent += (double)eff.getDAMRate();
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            --this.attackSpeed;
         }

         bx = SkillFactory.getSkill(27100006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localint_ += eff.getIntX();
         }

         bx = SkillFactory.getSkill(27111004);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ASR += eff.getASRRate();
            this.TER += eff.getTERRate();
         }

         bx = SkillFactory.getSkill(27121201);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getMdR());
         }

         bx = SkillFactory.getSkill(27121006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getMdR());
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(27120007);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.magic += eff.getX();
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(27120008);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(27120043);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(27120046);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(27120049);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }
         break;
      case 3001:
      case 3100:
      case 3110:
      case 3111:
      case 3112:
         this.mpRecoverProp = 100;
         bx = SkillFactory.getSkill(30010112);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.BossDamage += eff.getBdR();
         }

         bx = SkillFactory.getSkill(31000003);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
         }

         bx = SkillFactory.getSkill(31100004);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(31120008) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(31120008).getEffect(chra.getSkillLevel(31120008)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
         }

         bx = SkillFactory.getSkill(31100005);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localstr += eff.getStrX();
            this.localdex += eff.getDexX();
         }

         bx = SkillFactory.getSkill(31100006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
         }

         bx = SkillFactory.getSkill(31110004);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ASR += eff.getASRRate();
            this.TER += eff.getTERRate();
         }

         bx = SkillFactory.getSkill(31110007);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getPdR());
         }

         bx = SkillFactory.getSkill(31121006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(31121005);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
         }

         bx = SkillFactory.getSkill(31120008);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(31110009);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            ++this.mpRecover;
            this.mpRecoverProp += eff.getProp();
         }

         bx = SkillFactory.getSkill(30010112);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.BossDamage += eff.getBossDamage();
            this.mpRecover += eff.getX();
            this.mpRecoverProp += eff.getBossDamage();
         }

         bx = SkillFactory.getSkill(31100007);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDamPlus());
            }
         }

         bx = SkillFactory.getSkill(31120011);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(31120044);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getPdR());
            }
         }

         bx = SkillFactory.getSkill(31120047);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(31120049);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }
         break;
      case 3101:
      case 3120:
      case 3121:
      case 3122:
         bx = SkillFactory.getSkill(30010241);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.DamagePercent += (double)eff.getDAMRate();
         }

         bx = SkillFactory.getSkill(30010185);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.stance += eff.getX();
         }

         bx = SkillFactory.getSkill(30010185);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.stance += eff.getX();
         }

         bx = SkillFactory.getSkill(31010003);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
         }

         bx = SkillFactory.getSkill(31200003);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(31200005);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(31220006) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(31220006).getEffect(chra.getSkillLevel(31220006)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
         }

         bx = SkillFactory.getSkill(31200006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            localmaxhp_ += (long)eff.getMhpX();
         }

         bx = SkillFactory.getSkill(31220005);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
            this.InsertDamageIncrease(31221014, 20);
            this.InsertDamageIncrease(31221001, 20);
         }

         bx = SkillFactory.getSkill(31221014);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDamPlus());
            }
         }

         bx = SkillFactory.getSkill(31221002);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDamPlus());
            }
         }

         bx = SkillFactory.getSkill(31221008);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
         }

         bx = SkillFactory.getSkill(31220004);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            --this.attackSpeed;
            this.DamagePercent += (double)eff.getDAMRate();
         }

         bx = SkillFactory.getSkill(31220006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(31220043);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(31220049);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }
         break;
      case 3200:
      case 3210:
      case 3211:
      case 3212:
         bx = SkillFactory.getSkill(32001016);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            bx.getEffect(bof);
            this.attackSpeed -= 2;
         }

         bx = SkillFactory.getSkill(32000015);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.magic += eff.getMagicX();
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
         }

         bx = SkillFactory.getSkill(32100006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(32120016) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(32120016).getEffect(chra.getSkillLevel(32120016)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
            this.magic += eff.getX();
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
         }

         bx = SkillFactory.getSkill(32100007);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localint_ += eff.getIntX();
         }

         bx = SkillFactory.getSkill(32100008);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
         }

         bx = SkillFactory.getSkill(32111015);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(32111012);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ASR += eff.getASRRate();
            this.TER += eff.getTERRate();
         }

         bx = SkillFactory.getSkill(32110001);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getMdR());
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(32110018);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.stance += eff.getStanceProp();
         }

         bx = SkillFactory.getSkill(32110019);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            bx.getEffect(bof);
            --this.attackSpeed;
         }

         bx = SkillFactory.getSkill(32121004);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(32121017);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_matk += eff.getMadR();
         }

         bx = SkillFactory.getSkill(32121010);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
            this.percent_mp += eff.getPercentMP();
         }

         bx = SkillFactory.getSkill(32120016);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.magic += eff.getX();
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(32120020);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_matk += eff.getMadR();
            this.DamagePercent += (double)eff.getDAMRate();
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(32120058);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(32120059);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }
         break;
      case 3300:
      case 3310:
      case 3311:
      case 3312:
         bx = SkillFactory.getSkill(33000005);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(33000034);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
         }

         String info = chra.getInfoQuest(23008);
         if (!info.contains("6=1") && !info.contains("7=1")) {
            if (info.contains("5=1")) {
               this.critical_rate = (short)(this.critical_rate + 5);
            }
         } else {
            this.BuffUP_Skill += 10;
         }

         bx = SkillFactory.getSkill(33100012);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.attackSpeed -= 2;
            this.localdex += eff.getDexX();
         }

         bx = SkillFactory.getSkill(33101005);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_mp += eff.getPercentMP();
         }

         bx = SkillFactory.getSkill(33100000);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(33120000) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(33120000).getEffect(chra.getSkillLevel(33120000)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
            this.DamagePercent += (double)eff.getDAMRate();
         }

         bx = SkillFactory.getSkill(33100010);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localstr += eff.getStrX();
            this.localdex += eff.getDexX();
         }

         bx = SkillFactory.getSkill(33100014);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
            this.stance += eff.getStanceProp();
         }

         bx = SkillFactory.getSkill(33111007);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
         }

         bx = SkillFactory.getSkill(33110008);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localdex += eff.getDexX();
         }

         size = 0;
         String[] var36 = chra.getInfoQuest(23008).split(";");
         n4 = var36.length;

         for(j = 0; j < n4; ++j) {
            String str = var36[j];
            if (str.contains("=1")) {
               ++size;
               if (size >= 6) {
                  size = 6;
                  break;
               }
            }
         }

         if ((bof = chra.getTotalSkillLevel(bx = SkillFactory.getSkill(33110014))) > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();

            for(a = 0; a < size; ++a) {
               this.critical_rate = (short)(this.critical_rate + eff.getY());
               this.critical_damage = (short)(this.critical_damage + eff.getZ());
            }
         }

         bx = SkillFactory.getSkill(33120013);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getPdR());
            this.localstr += eff.getStrX();
            this.localdex += eff.getStrX();
            this.localint_ += eff.getStrX();
            this.localluk += eff.getStrX();
         }

         bx = SkillFactory.getSkill(33120015);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ASR += eff.getASRRate();
            this.TER += eff.getTERRate();
         }

         bx = SkillFactory.getSkill(33120000);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getX();
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(33120010);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(33120011);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(33120044);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
         }

         bx = SkillFactory.getSkill(33120046);
         bof = chra.getTotalSkillLevel(bx);
         Integer i49;
         Iterator var37;
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var37 = bx.getPsdSkills().iterator();

            while(var37.hasNext()) {
               i49 = (Integer)var37.next();
               this.InsertDamageIncrease(i49, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(33120047);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var37 = bx.getPsdSkills().iterator();

            while(var37.hasNext()) {
               i49 = (Integer)var37.next();
               this.InsertDamageIncrease(i49, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(33120049);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var37 = bx.getPsdSkills().iterator();

            while(var37.hasNext()) {
               i49 = (Integer)var37.next();
               this.InsertDamageIncrease(i49, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(33120050);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var37 = bx.getPsdSkills().iterator();

            while(var37.hasNext()) {
               i49 = (Integer)var37.next();
               this.InsertIgIncrease(i49, eff.getIgnoreMob());
            }
         }

         bx = SkillFactory.getSkill(33120051);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var37 = bx.getPsdSkills().iterator();

            while(var37.hasNext()) {
               i49 = (Integer)var37.next();
               this.InsertBossDamIncrease(i49, eff.getBdR());
            }
         }
         break;
      case 3500:
      case 3510:
      case 3511:
      case 3512:
         bx = SkillFactory.getSkill(30000227);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.DamagePercent += (double)eff.getIndieDamR();
         }

         bx = SkillFactory.getSkill(35001002);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            localmaxhp_ += (long)eff.getMaxHpX();
            localmaxmp_ += (long)eff.getMaxHpX();
         }

         bx = SkillFactory.getSkill(35100000);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(35120000) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(35120000).getEffect(chra.getSkillLevel(35120000)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
            this.watk += eff.getAttackX();
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(35100011);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localstr += eff.getStrX();
            this.localdex += eff.getDexX();
         }

         bx = SkillFactory.getSkill(35111008);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ASR += eff.getASRRate();
            this.TER += eff.getTERRate();
         }

         bx = SkillFactory.getSkill(35110016);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getPdR());
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(35110017);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDamage());
            }
         }

         bx = SkillFactory.getSkill(35110018);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
            this.percent_mp += eff.getPercentMP();
         }

         bx = SkillFactory.getSkill(35120001);
         bof = chra.getTotalSkillLevel(bx);
         size = 0;
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.BuffUP_Summon += eff.getY();
            int[] a = new int[]{35121003, 35101012, 35121009, 35120002, 35111008, 35111002, 400051068, 400051009};
            n4 = a.length;

            for(j = 0; j < n4; ++j) {
               Integer s = a[j];
               if (chra.getBuffedValue(s)) {
                  ++size;
               }
            }

            if (chra.getMechDoors().size() >= 2) {
               ++size;
            }

            this.DamagePercent += (double)(size * eff.getW());
         }

         bx = SkillFactory.getSkill(35120018);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
            this.percent_mp += eff.getPercentMP();
         }

         bx = SkillFactory.getSkill(35120049);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            Iterator var43 = bx.getPsdSkills().iterator();

            while(var43.hasNext()) {
               Integer i51 = (Integer)var43.next();
               this.InsertDamageIncrease(i51, eff.getDAMRate());
            }
         }
         break;
      case 3600:
      case 3610:
      case 3611:
      case 3612:
         bx = SkillFactory.getSkill(30020232);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_str += eff.getStrR() * chra.getXenonSurplus();
            this.percent_dex += eff.getStrR() * chra.getXenonSurplus();
            this.percent_int += eff.getStrR() * chra.getXenonSurplus();
            this.percent_luk += eff.getStrR() * chra.getXenonSurplus();
         }

         bx = SkillFactory.getSkill(30020233);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_str += eff.getStrR();
            this.percent_dex += eff.getStrR();
            this.percent_int += eff.getStrR();
            this.percent_luk += eff.getStrR();
         }

         bx = SkillFactory.getSkill(36101003);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            localmaxhp_ += (long)eff.getMhpX();
            localmaxmp_ += (long)eff.getMhpX();
         }

         bx = SkillFactory.getSkill(36100002);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.critical_rate += (short)eff.getCr();
         }

         bx = SkillFactory.getSkill(36100005);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localstr += eff.getStrX();
            this.localdex += eff.getStrX();
            this.localint_ += eff.getStrX();
            this.localluk += eff.getStrX();
         }

         bx = SkillFactory.getSkill(36100006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            int mas = chra.getTotalSkillLevel(36120006) > 0 ? SkillFactory.getSkill(36120006).getEffect(chra.getSkillLevel(36120006)).getMastery() : 0;
            this.Mastery += mas > 0 ? (double)mas : (double)eff.getMastery();
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(36111003);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localstr += eff.getStrX();
            this.localdex += eff.getStrX();
            this.localint_ += eff.getStrX();
            this.localluk += eff.getStrX();
         }

         bx = SkillFactory.getSkill(36100010);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDamPlus());
            }
         }

         bx = SkillFactory.getSkill(36110012);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDamPlus());
            }
         }

         bx = SkillFactory.getSkill(36120005);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(36120006);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(36120004);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.stance += eff.getStanceProp();
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(30020234);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            if (this.str >= eff.getX()) {
               this.stance += eff.getY();
            }

            if (this.dex >= eff.getX()) {
               this.ASR += eff.getY();
            }

            if (this.str >= eff.getX() && this.dex >= eff.getX() && this.luk >= eff.getX()) {
               this.DamagePercent += (double)eff.getW();
               this.multi_lateral_hp += eff.getS();
               this.multi_lateral_mp += eff.getS();
            }
         }

         bx = SkillFactory.getSkill(36000004);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            if (this.str >= eff.getX()) {
               this.stance += eff.getY();
            }

            if (this.dex >= eff.getX()) {
               this.ASR += eff.getY();
            }

            if (this.str >= eff.getX() && this.dex >= eff.getX() && this.luk >= eff.getX()) {
               this.DamagePercent += (double)eff.getW();
               this.multi_lateral_hp += eff.getS();
               this.multi_lateral_mp += eff.getS();
            }
         }

         bx = SkillFactory.getSkill(36100007);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            if (this.str >= eff.getX()) {
               this.stance += eff.getY();
            }

            if (this.dex >= eff.getX()) {
               this.ASR += eff.getY();
            }

            if (this.str >= eff.getX() && this.dex >= eff.getX() && this.luk >= eff.getX()) {
               this.DamagePercent += (double)eff.getW();
               this.multi_lateral_hp += eff.getS();
               this.multi_lateral_mp += eff.getS();
            }
         }

         bx = SkillFactory.getSkill(36110007);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            if (this.str >= eff.getX()) {
               this.stance += eff.getY();
            }

            if (this.dex >= eff.getX()) {
               this.ASR += eff.getY();
            }

            if (this.str >= eff.getX() && this.dex >= eff.getX() && this.luk >= eff.getX()) {
               this.DamagePercent += (double)eff.getW();
               this.multi_lateral_hp += eff.getS();
               this.multi_lateral_mp += eff.getS();
            }

            if (chra.getLevel() >= 200) {
               bx = SkillFactory.getSkill(36120010);
               eff = bx.getEffect(1);
               if (this.str >= eff.getX() && this.dex >= eff.getX() && this.luk >= eff.getX()) {
                  this.multi_lateral_hp += eff.getS();
                  this.multi_lateral_mp += eff.getS();
               }
            }
         }

         bx = SkillFactory.getSkill(36120044);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(36120046);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(36120047);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertIgIncrease(i95, eff.getIgnoreMob());
            }
         }

         bx = SkillFactory.getSkill(36120050);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }
         break;
      case 3700:
      case 3710:
      case 3711:
      case 3712:
         bx = SkillFactory.getSkill(37000006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
         }

         bx = SkillFactory.getSkill(37101001);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDamPlus());
            }
         }

         bx = SkillFactory.getSkill(37100004);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(37120010) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(37120010).getEffect(chra.getSkillLevel(37120010)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(37100005);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localstr += eff.getStrX();
            this.localdex += eff.getDexX();
         }

         bx = SkillFactory.getSkill(37100006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.DamagePercent += (double)eff.getDAMRate();
         }

         bx = SkillFactory.getSkill(37111003);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(37110008);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ASR += eff.getASRRate();
            this.TER += eff.getTERRate();
         }

         bx = SkillFactory.getSkill(37110009);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_atk += eff.getPadR();
         }

         bx = SkillFactory.getSkill(37121000);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDamPlus());
            }
         }

         bx = SkillFactory.getSkill(37120002);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDamPlus());
            }
         }

         bx = SkillFactory.getSkill(37121003);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDamPlus());
            }
         }

         bx = SkillFactory.getSkill(37121005);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDamPlus());
            }
         }

         bx = SkillFactory.getSkill(37120009);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDamPlus());
            }
         }

         bx = SkillFactory.getSkill(37120010);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.critical_damage += eff.getCriticalDamage();
            this.BossDamage += eff.getBdR();
         }

         bx = SkillFactory.getSkill(37120011);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(37120012);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
         }
         break;
      case 5000:
      case 5100:
      case 5110:
      case 5111:
      case 5112:
         bx = SkillFactory.getSkill(50000250);
         bof = chra.getTotalSkillLevel(50000250);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_atk += eff.getPadR();
            this.percent_matk += eff.getPadR();
         }

         bx = SkillFactory.getSkill(50000074);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getX();
            this.percent_mp += eff.getX();
         }

         bx = SkillFactory.getSkill(51000000);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.percent_hp += bx.getEffect(bof).getPercentHP();
         }

         bx = SkillFactory.getSkill(51100001);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(51120001) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(51120001).getEffect(chra.getSkillLevel(51120001)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
            this.InsertFinalDamage(eff.getPdR());
         }

         bx = SkillFactory.getSkill(51101004);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(51100000);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localstr += eff.getStrX();
            this.localdex += eff.getDexX();
         }

         bx = SkillFactory.getSkill(51110001);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localstr += eff.getStrX();
            this.InsertFinalDamage(eff.getPdR());
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            --this.attackSpeed;
         }

         bx = SkillFactory.getSkill(51110002);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ApplyStatFinalDamage.put("MS_Stun", eff.getX());
            this.ApplyStatFinalDamage.put("MS_Speed", eff.getX());
            this.ApplyStatFinalDamage.put("MS_Blind", eff.getX());
            this.ApplyStatFinalDamage.put("MS_Freeze", eff.getX());
            this.ApplyStatFinalDamage.put("MS_DeadlyCharge", eff.getX());
            --this.attackSpeed;
         }

         bx = SkillFactory.getSkill(51120001);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(51120002);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(51120000);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(51120004);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.stance += eff.getStanceProp();
         }

         bx = SkillFactory.getSkill(51110003);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.DamagePercent += (double)eff.getDAMRate();
         }

         bx = SkillFactory.getSkill(51121054);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
         }

         bx = SkillFactory.getSkill(51120056);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(51120049);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }
         break;
      case 6000:
      case 6100:
      case 6110:
      case 6111:
      case 6112:
         if (chra.getKaiserCombo() < 100) {
            if (chra.getKaiserCombo() >= 200) {
               --this.attackSpeed;
               this.stance += 40;
            }
         } else {
            --this.attackSpeed;
            this.stance += 20;
         }

         bx = SkillFactory.getSkill(60000222);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
            size = chra.getKaiserCombo() < 300 ? (chra.getKaiserCombo() < 200 ? (chra.getKaiserCombo() < 100 ? 0 : 1) : 2) : 3;
            this.DamagePercent += (double)(eff.getY() * size);
         }

         bx = SkillFactory.getSkill(61000003);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.stance += eff.getProp();
         }

         bx = SkillFactory.getSkill(61000003);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(61100006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(61120012) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(61120012).getEffect(chra.getSkillLevel(61120012)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
         }

         bx = SkillFactory.getSkill(61100007);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localstr += eff.getStrX();
            this.percent_hp += eff.getPercentHP();
         }

         bx = SkillFactory.getSkill(61110015);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(61110004);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_atk += eff.getPadR();
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.InsertFinalDamage(eff.getPdR());
         }

         bx = SkillFactory.getSkill(61110007);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localstr += eff.getStrX();
            this.percent_hp += eff.getPercentHP();
         }

         bx = SkillFactory.getSkill(61120007);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(61120011);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(61120012);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(61120043);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(61120046);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(61120048);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(61120049);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }
         break;
      case 6005:
      case 6500:
      case 6510:
      case 6511:
      case 6512:
         bx = SkillFactory.getSkill(65000005);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            localmaxhp_ += (long)eff.getMhpX();
         }

         bx = SkillFactory.getSkill(65001002);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            localmaxhp_ += (long)eff.getMhpX();
         }

         bx = SkillFactory.getSkill(65100003);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(65120005) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(65120005).getEffect(chra.getSkillLevel(65120005)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(65100004);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localdex += eff.getDexX();
         }

         bx = SkillFactory.getSkill(65100005);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ASR += eff.getASRRate();
            this.TER += eff.getTERRate();
         }

         bx = SkillFactory.getSkill(65110003);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(65110005);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            localmaxhp_ += (long)eff.getMhpX();
         }

         bx = SkillFactory.getSkill(65110006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localdex += eff.getDexX();
            this.DamagePercent += (double)eff.getDAMRate();
         }

         bx = SkillFactory.getSkill(65121101);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getPdR());
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(65120005);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(65120043);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(65120045);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertIgIncrease(i95, eff.getIgnoreMob());
            }
         }

         bx = SkillFactory.getSkill(65120046);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(65120049);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(65120050);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertIgIncrease(i95, eff.getIgnoreMob());
            }
         }
         break;
      case 6300:
      case 6310:
      case 6311:
      case 6312:
         bx = SkillFactory.getSkill(63000007);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
            this.percent_hp += eff.getPercentHP();
            this.stance += eff.getStanceProp();
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
         }

         bx = SkillFactory.getSkill(63100002);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertDamageIncrease(63001001, eff.getX());
            this.InsertDamageIncrease(63001001, eff.getY());
         }

         bx = SkillFactory.getSkill(63100011);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(63120013) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(63120013).getEffect(chra.getSkillLevel(63120013)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(63100012);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localdex += eff.getDexX();
         }

         bx = SkillFactory.getSkill(63110001);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertDamageIncrease(63001001, eff.getX());
            this.InsertDamageIncrease(63001001, eff.getY());
         }

         bx = SkillFactory.getSkill(63110014);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getPdR());
            this.watk += eff.getAttackX();
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
            this.ASR += eff.getASRRate();
            this.TER += eff.getTERRate();
         }

         bx = SkillFactory.getSkill(63110015);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_mp += eff.getPercentMP();
            this.InsertDamageIncrease(63101004, eff.getX());
            this.InsertDamageIncrease(63101104, eff.getY());
            this.InsertDamageIncrease(63101006, eff.getW());
         }

         bx = SkillFactory.getSkill(63120001);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(63120012);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getPdR());
            this.critical_damage += eff.getCriticalDamage();
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(63120013);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getPdR());
            this.watk += eff.getAttackX();
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(63120014);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.DamagePercent += (double)eff.getDAMRate();
            this.BossDamage += eff.getBdR();
            this.percent_hp += eff.getPercentHP();
         }

         bx = SkillFactory.getSkill(63120031);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertIgIncrease(i95, eff.getIgnoreMob());
            }
         }

         bx = SkillFactory.getSkill(63120032);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(63120033);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertBossDamIncrease(i95, eff.getBdR());
            }
         }

         bx = SkillFactory.getSkill(63120034);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertIgIncrease(i95, eff.getIgnoreMob());
            }
         }

         bx = SkillFactory.getSkill(63120035);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(63120037);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }
         break;
      case 6400:
      case 6410:
      case 6411:
      case 6412:
         bx = SkillFactory.getSkill(64000005);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localluk += eff.getLukX();
         }

         bx = SkillFactory.getSkill(64100004);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDamPlus());
            }
         }

         bx = SkillFactory.getSkill(64100005);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(64120008) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(64120008).getEffect(chra.getSkillLevel(64120008)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
         }

         bx = SkillFactory.getSkill(64100006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localluk += eff.getLukX();
            this.localdex += eff.getDexX();
         }

         bx = SkillFactory.getSkill(64100007);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
            this.critical_damage += eff.getCriticalDamage();
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
         }

         bx = SkillFactory.getSkill(64110005);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(64110006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
            this.ASR += eff.getASRRate();
            this.TER += eff.getTERRate();
         }

         bx = SkillFactory.getSkill(64110014);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(64120008);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(64120009);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(64120046);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(64120049);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(64120043);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(64120043);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertBossDamIncrease(i95, eff.getBdR());
            }
         }
         break;
      case 10000:
      case 10100:
      case 10110:
      case 10111:
      case 10112:
         bx = SkillFactory.getSkill(100000271);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(100000279);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getPdR());
            this.localstr += eff.getStrX();
            this.percent_hp += eff.getPercentHP();
         }

         if (chra.getGender() != 0) {
            if (chra.getGender() == 1) {
               bx = SkillFactory.getSkill(101000103);
               bof = chra.getTotalSkillLevel(bx);
               if (bof > 0) {
                  eff = bx.getEffect(bof);
                  this.Mastery += (double)eff.getMastery();
                  this.critical_rate = (short)(this.critical_rate + eff.getCr());
                  this.BossDamage += eff.getBdR();
                  this.watk += eff.getAttackX();
                  this.InsertFinalDamage(eff.getPdR());
                  this.attackSpeed -= 3;
               }

               bx = SkillFactory.getSkill(101100102);
               bof = chra.getTotalSkillLevel(bx);
               if (bof > 0) {
                  eff = bx.getEffect(bof);
                  this.ASR += eff.getASRRate();
                  this.TER += eff.getTERRate();
                  this.stance += eff.getStanceProp();
               }
            }
         } else {
            bx = SkillFactory.getSkill(101000203);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.Mastery += (double)eff.getMastery();
               this.watk += eff.getAttackX();
               this.InsertFinalDamage(eff.getPdR());
               this.attackSpeed -= 3;
               this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
            }

            bx = SkillFactory.getSkill(101100203);
            bof = chra.getSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.percent_hp += eff.getPercentHP();
               this.ASR += eff.getASRRate();
               this.TER += eff.getTERRate();
               this.critical_rate = (short)(this.critical_rate + eff.getCr());
            }

            bx = SkillFactory.getSkill(101120207);
            bof = chra.getSkillLevel(bx);
            if (bof > 0) {
               eff = bx.getEffect(bof);
               this.critical_damage += eff.getCriticalDamage();
            }
         }
         break;
      case 13000:
      case 13100:
         bx = SkillFactory.getSkill(131000016);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.stance += eff.getStanceProp();
            this.percent_hp += eff.getPercentHP();
            this.percent_mp += eff.getPercentMP();
            this.ASR += eff.getASRRate();
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
            this.dropBuff += (double)eff.getDropR();
            this.mesoBuff += (double)eff.getMesoRate();
            this.expBuff += (double)eff.getEXPRate();
         }

         bx = SkillFactory.getSkill(131000014);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            --this.attackSpeed;
            size = chra.getLevel() / eff.getX();
            if (size > 0) {
               this.attackSpeed -= size;
            }

            this.critical_rate = (short)(this.critical_rate + eff.getY());
            size = chra.getLevel() / eff.getZ();
            if (size > 0) {
               this.critical_rate = (short)(this.critical_rate + size);
            }

            size = chra.getLevel() * eff.getW();
            if (size > 0) {
               this.DamagePercent += (double)size;
            }

            size = chra.getLevel() * eff.getS();
            if (size > 0) {
               this.watk += size * eff.getV();
            }

            this.Mastery += (double)eff.getU();
         }
         break;
      case 13001:
      case 13500:
         bx = SkillFactory.getSkill(135000021);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.stance += eff.getStanceProp();
            this.percent_hp += eff.getPercentHP();
            this.percent_mp += eff.getPercentMP();
            this.ASR += eff.getASRRate();
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
            this.dropBuff += (double)eff.getDropR();
            this.mesoBuff += (double)eff.getMesoRate();
            this.expBuff += (double)eff.getEXPRate();
         }

         bx = SkillFactory.getSkill(135000022);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            --this.attackSpeed;
            size = chra.getLevel() / eff.getX();
            if (size > 0) {
               this.attackSpeed -= size;
            }

            this.critical_rate = (short)(this.critical_rate + eff.getY());
            size = chra.getLevel() / eff.getZ();
            if (size > 0) {
               this.critical_rate = (short)(this.critical_rate + size);
            }

            size = chra.getLevel() * eff.getW();
            if (size > 0) {
               this.DamagePercent += (double)size;
            }

            size = chra.getLevel() * eff.getS();
            if (size > 0) {
               this.watk += size * eff.getV();
            }

            this.Mastery += (double)eff.getU();
         }

         bx = SkillFactory.getSkill(135001008);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getPdR());
         }

         bx = SkillFactory.getSkill(135001011);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getPdR());
         }

         bx = SkillFactory.getSkill(135001016);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getPdR());
         }

         bx = SkillFactory.getSkill(135001019);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getPdR());
         }
         break;
      case 14200:
      case 14210:
      case 14211:
      case 14212:
         bx = SkillFactory.getSkill(140000291);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
         }

         bx = SkillFactory.getSkill(140000292);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(142001000);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.magic += eff.getMagicX();
         }

         bx = SkillFactory.getSkill(142000005);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.magic += eff.getMagicX();
            this.percent_hp += eff.getPercentHP();
         }

         bx = SkillFactory.getSkill(142100000);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.magic += eff.getMagicX();
         }

         bx = SkillFactory.getSkill(142100005);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.DamagePercent += (double)eff.getDAMRate();
            --this.attackSpeed;
         }

         bx = SkillFactory.getSkill(142100007);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
            this.magic += eff.getMagicX();
         }

         bx = SkillFactory.getSkill(142101003);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getS2());
            }
         }

         bx = SkillFactory.getSkill(142100006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(142120013) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(142120013).getEffect(chra.getSkillLevel(142120013)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.localint_ += eff.getIntX();
         }

         bx = SkillFactory.getSkill(142110000);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.magic += eff.getMagicX();
         }

         bx = SkillFactory.getSkill(142111007);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertDamageIncrease(142101003, eff.getS2());
            this.InsertDamageIncrease(142001002, eff.getU2());
         }

         bx = SkillFactory.getSkill(142110008);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_matk += eff.getMadR();
         }

         bx = SkillFactory.getSkill(142110012);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(142110013);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ASR += eff.getASRRate();
            this.TER += eff.getTERRate();
         }

         bx = SkillFactory.getSkill(142111007);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertDamageIncrease(142101003, eff.getS2());
            this.InsertDamageIncrease(142001002, eff.getU2());
            this.InsertDamageIncrease(142111007, eff.getW2());
         }

         bx = SkillFactory.getSkill(142121008);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.BossDamage += eff.getBossDamage();
         }

         bx = SkillFactory.getSkill(142121007);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ASR += eff.getASRRate();
         }

         bx = SkillFactory.getSkill(142120006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.magic += eff.getMagicX();
            this.DamagePercent += (double)eff.getDAMRate();
         }

         bx = SkillFactory.getSkill(142120009);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.BuffUP_Skill += bx.getEffect(bof).getBufftimeR();
         }

         bx = SkillFactory.getSkill(142120010);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getX());
         }

         bx = SkillFactory.getSkill(142120012);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(142120013);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(142120034);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(142120037);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(142120039);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }
         break;
      case 15000:
      case 15200:
      case 15210:
      case 15211:
      case 15212:
         bx = SkillFactory.getSkill(152000006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(152120015) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(152120015).getEffect(chra.getSkillLevel(152120015)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
         }

         bx = SkillFactory.getSkill(152000006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(152120015) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(152120015).getEffect(chra.getSkillLevel(152120015)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
         }

         bx = SkillFactory.getSkill(152000007);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
            this.percent_mp += eff.getPercentMP();
            this.DamagePercent += (double)eff.getDAMRate();
         }

         bx = SkillFactory.getSkill(152111007);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ASR += eff.getASRRate();
            this.TER += eff.getTERRate();
         }

         bx = SkillFactory.getSkill(152110009);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertBossDamIncrease(i95, eff.getDamPlus());
            }
         }

         bx = SkillFactory.getSkill(152110011);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
            this.percent_mp += eff.getPercentMP();
            this.DamagePercent += (double)eff.getDAMRate();
         }

         bx = SkillFactory.getSkill(152110012);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localint_ += eff.getIntX();
            this.percent_matk += eff.getMadR();
         }

         bx = SkillFactory.getSkill(152110013);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.magic += eff.getMagicX();
            this.critical_damage += eff.getCriticalDamage();
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
         }

         bx = SkillFactory.getSkill(152120015);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            --this.attackSpeed;
            this.BossDamage += eff.getBdR();
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
            this.InsertFinalDamage(eff.getMdR());
         }

         bx = SkillFactory.getSkill(152120031);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(152120033);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertBossDamIncrease(i95, eff.getBdR());
            }
         }

         bx = SkillFactory.getSkill(152120034);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(152120034);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }
         break;
      case 15001:
      case 15500:
      case 15510:
      case 15511:
      case 15512:
         bx = SkillFactory.getSkill(155000006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getX();
            this.stance += eff.getStanceProp();
         }

         bx = SkillFactory.getSkill(155100007);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(155120010) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(155120010).getEffect(chra.getSkillLevel(155120010)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(155100010);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
            this.percent_mp += eff.getPercentMP();
         }

         bx = SkillFactory.getSkill(155100011);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localstr += eff.getStrX();
         }

         bx = SkillFactory.getSkill(155110010);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getPdR());
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.ASR += eff.getASRRate();
            this.TER += eff.getTERRate();
         }

         bx = SkillFactory.getSkill(155120010);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(155120013);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(155120014);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getPdR());
         }

         bx = SkillFactory.getSkill(155120031);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(155120032);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertIgIncrease(i95, eff.getIgnoreMob());
            }
         }

         bx = SkillFactory.getSkill(155120033);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertBossDamIncrease(i95, eff.getBdR());
            }
         }

         bx = SkillFactory.getSkill(155120037);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }
         break;
      case 15002:
      case 15100:
      case 15110:
      case 15111:
      case 15112:
         bx = SkillFactory.getSkill(150020241);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.BossDamage += eff.getBdR();
            if (chra.getParty() != null) {
               size = eff.getX() * chra.getParty().getMembers().size();
               if (size >= eff.getY()) {
                  size = eff.getY();
               }

               this.DamagePercent += (double)size;
            } else {
               this.DamagePercent += (double)eff.getX();
            }
         }

         bx = SkillFactory.getSkill(151000005);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
            this.stance += eff.getStanceProp();
            localmaxhp_ += (long)eff.getMaxHpX();
         }

         bx = SkillFactory.getSkill(151100014);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(151120007) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(151120007).getEffect(chra.getSkillLevel(151120007)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(151100016);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localstr += eff.getStrX();
         }

         bx = SkillFactory.getSkill(151110006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
            this.InsertFinalDamage(eff.getPdR());
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.InsertDamageIncrease(151001001, eff.getX());
            this.InsertDamageIncrease(151101009, eff.getW());
            this.InsertDamageIncrease(151101001, eff.getY());
            this.InsertDamageIncrease(151101003, eff.getZ());
            this.InsertDamageIncrease(151101004, eff.getZ());
            this.InsertDamageIncrease(151101010, eff.getZ());
         }

         bx = SkillFactory.getSkill(151110007);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
            this.stance += eff.getStanceProp();
            this.ASR += eff.getASRRate();
            this.TER += eff.getTERRate();
         }

         bx = SkillFactory.getSkill(151120007);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(151120010);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
            this.BossDamage += eff.getBdR();
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.percent_hp += eff.getPercentHP();
         }

         bx = SkillFactory.getSkill(151120009);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getPdR());
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(151120008);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertDamageIncrease(151001001, eff.getX());
            this.InsertDamageIncrease(151101009, eff.getU());
            this.InsertDamageIncrease(151101001, eff.getY());
            this.InsertDamageIncrease(151101003, eff.getZ());
            this.InsertDamageIncrease(151101004, eff.getZ());
            this.InsertDamageIncrease(151101010, eff.getZ());
            this.InsertDamageIncrease(151111003, eff.getS());
            this.InsertDamageIncrease(151111002, eff.getW());
            this.InsertDamageIncrease(151111001, eff.getV());
         }

         bx = SkillFactory.getSkill(151120031);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(151120032);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertIgIncrease(i95, eff.getIgnoreMob());
            }
         }

         bx = SkillFactory.getSkill(151120033);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertBossDamIncrease(i95, eff.getBdR());
            }
         }
         break;
      case 15400:
      case 15410:
      case 15411:
      case 15412:
         bx = SkillFactory.getSkill(154000004);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getX();
            this.stance += eff.getStanceProp();
            localmaxhp_ += (long)eff.getMaxHpX();
            localmaxmp_ += (long)eff.getMaxMpX();
         }

         bx = SkillFactory.getSkill(154110009);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            this.stance += eff.getStanceProp();
            this.percent_hp += 15;
            this.percent_mp += 15;
         }
         break;
      case 16000:
      case 16400:
      case 16410:
      case 16411:
      case 16412:
         bx = SkillFactory.getSkill(160000000);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.BuffUP_Summon += eff.getSummonTimeR();
         }

         bx = SkillFactory.getSkill(160000001);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(164001001);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.BossDamage += eff.getBdR();
         }

         bx = SkillFactory.getSkill(164100010);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(164120010) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(164120010).getEffect(chra.getSkillLevel(164120010)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
            this.watk += eff.getAttackX();
         }

         bx = SkillFactory.getSkill(164100011);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.critical_damage += eff.getCriticalDamage();
         }

         bx = SkillFactory.getSkill(164100013);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localluk += eff.getLukX();
         }

         bx = SkillFactory.getSkill(164110011);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getAttackX();
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.critical_damage += eff.getCriticalDamage();
            this.BossDamage += eff.getBdR();
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(164110012);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.percent_hp += eff.getPercentHP();
            this.stance += eff.getStanceProp();
         }

         bx = SkillFactory.getSkill(164110013);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.ASR += eff.getASRRate();
            this.TER += eff.getTERRate();
         }

         bx = SkillFactory.getSkill(164120010);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getWatk();
            this.InsertFinalDamage(eff.getPdR());
         }

         bx = SkillFactory.getSkill(164120011);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.DamagePercent += (double)eff.getDAMRate();
         }

         bx = SkillFactory.getSkill(164120012);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.watk += eff.getWatk();
            this.InsertFinalDamage(eff.getPdR());
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.critical_damage += eff.getCriticalDamage();
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMob()));
         }

         bx = SkillFactory.getSkill(164120031);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(164120032);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertIgIncrease(i95, eff.getIgnoreMob());
            }
         }

         bx = SkillFactory.getSkill(164120033);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertBossDamIncrease(i95, eff.getBdR());
            }
         }

         bx = SkillFactory.getSkill(164120034);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(164120035);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertIgIncrease(i95, eff.getIgnoreMob());
            }
         }

         bx = SkillFactory.getSkill(164120037);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertDamageIncrease(i95, eff.getDAMRate());
            }
         }

         bx = SkillFactory.getSkill(164120038);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            var30 = bx.getPsdSkills().iterator();

            while(var30.hasNext()) {
               i95 = (Integer)var30.next();
               this.InsertBossDamIncrease(i95, eff.getBdR());
            }
         }
         break;
      case 16001:
      case 16200:
      case 16210:
      case 16211:
      case 16212:
         bx = SkillFactory.getSkill(160010000);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.BuffUP_Summon += eff.getSummonTimeR();
         }

         bx = SkillFactory.getSkill(162000006);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.speed += eff.getPsdSpeed();
            this.jump += eff.getPsdJump();
            this.percent_hp += eff.getPercentHP();
         }

         bx = SkillFactory.getSkill(162100014);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            mas = 0;
            eff = bx.getEffect(bof);
            if (chra.getTotalSkillLevel(162120025) <= 0) {
               mas = 0;
            } else {
               SkillFactory.getSkill(162120025).getEffect(chra.getSkillLevel(162120025)).getMastery();
            }

            this.Mastery += mas <= 0 ? (double)eff.getMastery() : (double)mas;
            this.magic += eff.getMagicX();
         }

         bx = SkillFactory.getSkill(162100015);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localint_ += eff.getIntX();
         }

         bx = SkillFactory.getSkill(162110008);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.DamagePercent += (double)eff.getDAMRate();
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.percent_mp += eff.getPercentMP();
         }

         bx = SkillFactory.getSkill(162120027);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.critical_rate = (short)(this.critical_rate + eff.getCr());
            this.critical_damage += eff.getCriticalDamage();
            this.InsertFinalDamage(eff.getMdR());
            this.ignoreMobpdpR.add(Integer.valueOf(eff.getIgnoreMobDamR()));
         }

         bx = SkillFactory.getSkill(162120028);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.magic += eff.getMagicX();
            this.ASR += eff.getASRRate();
            this.TER += eff.getTERRate();
         }

         bx = SkillFactory.getSkill(162120025);
         bof = chra.getTotalSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.InsertFinalDamage(eff.getMdR());
            this.magic += eff.getMagicX();
         }
      }

      if (GameConstants.isAdventurer(chra.getJob())) {
         bx = SkillFactory.getSkill(74);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            this.levelBonus += bx.getEffect(bof).getX();
         }

         bx = SkillFactory.getSkill(80);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            this.levelBonus += bx.getEffect(bof).getX();
         }

         bx = SkillFactory.getSkill(10074);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            this.levelBonus += bx.getEffect(bof).getX();
         }

         bx = SkillFactory.getSkill(10080);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            this.levelBonus += bx.getEffect(bof).getX();
         }

         bx = SkillFactory.getSkill(10110);
         bof = chra.getSkillLevel(bx);
         if (bof > 0) {
            eff = bx.getEffect(bof);
            this.localstr += eff.getStrX();
            this.localdex += eff.getDexX();
            this.localint_ += eff.getIntX();
            this.localluk += eff.getLukX();
            this.percent_hp = (int)((double)this.percent_hp + eff.getHpR());
            this.percent_mp = (int)((double)this.percent_mp + eff.getMpR());
         }
      }

      size = GameConstants.getEmpress_ForJob(chra.getJob());
      size = GameConstants.getBOF_ForJob(chra.getJob());
      int skillid3 = false;
      a = chra.getSkillLevel(size) <= chra.getSkillLevel(size) ? size : size;
      bx = SkillFactory.getSkill(a);
      bof = chra.getSkillLevel(bx);
      if (bof > 0) {
         eff = bx.getEffect(bof);
         this.watk += eff.getX();
         this.magic += eff.getY();
      }

      if (GameConstants.isResist(chra.getJob()) && (bof = chra.getTotalSkillLevel(bx = SkillFactory.getSkill(30000002))) > 0) {
         this.RecoveryUP += bx.getEffect(bof).getX() - 100;
      }

      return new Pair(localmaxhp_, localmaxmp_);
   }

   private Pair<Long, Long> handleBuffStats(MapleCharacter chra) {
      long localmaxhp_ = 0L;
      long localmaxmp_ = 0L;
      SecondaryStatEffect eff = chra.getBuffedEffect(SecondaryStat.RideVehicle);
      if (eff != null && eff.getSourceId() == 33001001) {
         this.stance += eff.getY();
      }

      Integer buff;
      if ((buff = chra.getBuffedValue(SecondaryStat.DiceRoll)) != null) {
         this.percent_wdef += GameConstants.getDiceStat(buff, 2);
         this.percent_mdef += GameConstants.getDiceStat(buff, 2);
         this.percent_hp += GameConstants.getDiceStat(buff, 3);
         this.percent_mp += GameConstants.getDiceStat(buff, 3);
         this.DamagePercent *= ((double)GameConstants.getDiceStat(buff, 5) + 100.0D) / 100.0D;
         this.BossDamage = (int)((double)this.BossDamage * (((double)GameConstants.getDiceStat(buff, 5) + 100.0D) / 100.0D));
      }

      int up;
      int skillid;
      if (GameConstants.isFPMage(chra.getJob()) && chra.getPoisonStack() > 0) {
         skillid = chra.getSkillLevel(2120014) > 0 ? 2120014 : 2100009;
         up = SkillFactory.getSkill(skillid).getEffect(1).getX();
         this.InsertFinalDamage(up * chra.getPoisonStack());
      }

      if (chra.getBuffedEffect(SecondaryStat.DarkSight) != null) {
         skillid = chra.getSkillLevel(4210015) > 0 ? 4210015 : 4330001;
         up = chra.getSkillLevel(skillid) > 0 ? SkillFactory.getSkill(skillid).getEffect(chra.getSkillLevel(skillid)).getY() : 0;
         if (chra.getBuffedEffect(400001023) != null) {
            up += chra.getBuffedEffect(400001023).getY();
         }

         if (up > 0) {
            this.InsertFinalDamage(up);
         }
      } else {
         SecondaryStatEffect effect;
         if (chra.getSkillLevel(5110014) > 0) {
            skillid = chra.getSkillLevel(5120018) > 0 ? 5120018 : 5110014;
            effect = SkillFactory.getSkill(skillid).getEffect(chra.getSkillLevel(skillid));
            this.watk += chra.getBuffedEffect(SecondaryStat.EnergyCharged) != null ? effect.getWatk() : effect.getWatk() / 2;
         } else if (chra.getSkillLevel(5220019) > 0) {
            effect = SkillFactory.getSkill(5220019).getEffect(chra.getSkillLevel(5220019));
            if (chra.getBuffedEffect(SecondaryStat.SpiritLink) != null) {
               this.watk += effect.getW();
               this.critical_rate = (short)(this.critical_rate + effect.getS());
               this.critical_damage = (short)(this.critical_damage + effect.getZ());
            }
         }
      }

      Iterator var23 = chra.getEffects().iterator();

      while(true) {
         while(var23.hasNext()) {
            Pair<SecondaryStat, SecondaryStatValueHolder> buffs = (Pair)var23.next();
            String buffname;
            String var13 = buffname = ((Class)buffs.getLeft()).makeConcatWithConstants<invokedynamic>(buffs.getLeft());
            byte var14 = -1;
            switch(var13.hashCode()) {
            case -2102508891:
               if (var13.equals("EnrageCr")) {
                  var14 = 41;
               }
               break;
            case -2060283315:
               if (var13.equals("BlessingArmorIncPad")) {
                  var14 = 26;
               }
               break;
            case -2018342806:
               if (var13.equals("IgnisRore")) {
                  var14 = 78;
               }
               break;
            case -1993711665:
               if (var13.equals("MesoUp")) {
                  var14 = 9;
               }
               break;
            case -1857213939:
               if (var13.equals("CrossOverChain")) {
                  var14 = 60;
               }
               break;
            case -1856031238:
               if (var13.equals("IndieAllStat")) {
                  var14 = 53;
               }
               break;
            case -1702393448:
               if (var13.equals("IndieAllStatR")) {
                  var14 = 57;
               }
               break;
            case -1624216224:
               if (var13.equals("AranSmashSwing")) {
                  var14 = 77;
               }
               break;
            case -1593413081:
               if (var13.equals("LuckOfUnion")) {
                  var14 = 5;
               }
               break;
            case -1497198815:
               if (var13.equals("CriticalIncrease")) {
                  var14 = 39;
               }
               break;
            case -1279701903:
               if (var13.equals("BullsEye")) {
                  var14 = 42;
               }
               break;
            case -1071002004:
               if (var13.equals("WeaponVariety")) {
                  var14 = 59;
               }
               break;
            case -990697295:
               if (var13.equals("QuickDraw")) {
                  var14 = 62;
               }
               break;
            case -956829441:
               if (var13.equals("BlessOfDarkness")) {
                  var14 = 79;
               }
               break;
            case -878916107:
               if (var13.equals("IndieBooster")) {
                  var14 = 21;
               }
               break;
            case -686922006:
               if (var13.equals("IndieCr")) {
                  var14 = 38;
               }
               break;
            case -686921853:
               if (var13.equals("IndieHp")) {
                  var14 = 15;
               }
               break;
            case -686921698:
               if (var13.equals("IndieMp")) {
                  var14 = 14;
               }
               break;
            case -616648436:
               if (var13.equals("FinalCut")) {
                  var14 = 66;
               }
               break;
            case -557605280:
               if (var13.equals("HowlingParty")) {
                  var14 = 20;
               }
               break;
            case -497125663:
               if (var13.equals("HolyUnity")) {
                  var14 = 1;
               }
               break;
            case -451829734:
               if (var13.equals("ReshuffleSwitch")) {
                  var14 = 74;
               }
               break;
            case -367967089:
               if (var13.equals("DropRate")) {
                  var14 = 4;
               }
               break;
            case -356028942:
               if (var13.equals("BeastFormDamage")) {
                  var14 = 19;
               }
               break;
            case -326882917:
               if (var13.equals("ElementalCharge")) {
                  var14 = 2;
               }
               break;
            case -105288021:
               if (var13.equals("WealthOfUnion")) {
                  var14 = 7;
               }
               break;
            case -15608658:
               if (var13.equals("ComboCounter")) {
                  var14 = 3;
               }
               break;
            case 66144:
               if (var13.equals("Asr")) {
                  var14 = 44;
               }
               break;
            case 67575:
               if (var13.equals("DEX")) {
                  var14 = 50;
               }
               break;
            case 72655:
               if (var13.equals("INT")) {
                  var14 = 51;
               }
               break;
            case 75746:
               if (var13.equals("LUK")) {
                  var14 = 52;
               }
               break;
            case 80080:
               if (var13.equals("Pdd")) {
                  var14 = 56;
               }
               break;
            case 82449:
               if (var13.equals("STR")) {
                  var14 = 49;
               }
               break;
            case 83969:
               if (var13.equals("Ter")) {
                  var14 = 46;
               }
               break;
            case 2122466:
               if (var13.equals("DamR")) {
                  var14 = 31;
               }
               break;
            case 8209110:
               if (var13.equals("JaguarSummoned")) {
                  var14 = 80;
               }
               break;
            case 74118476:
               if (var13.equals("MaxHP")) {
                  var14 = 47;
               }
               break;
            case 74118631:
               if (var13.equals("MaxMP")) {
                  var14 = 48;
               }
               break;
            case 102788534:
               if (var13.equals("SoulArrow")) {
                  var14 = 27;
               }
               break;
            case 141700351:
               if (var13.equals("ArcaneAim")) {
                  var14 = 71;
               }
               break;
            case 180251989:
               if (var13.equals("IndieBDR")) {
                  var14 = 68;
               }
               break;
            case 180259119:
               if (var13.equals("IndieHpR")) {
                  var14 = 35;
               }
               break;
            case 180263477:
               if (var13.equals("IndieMad")) {
                  var14 = 30;
               }
               break;
            case 180263924:
               if (var13.equals("IndieMpR")) {
                  var14 = 36;
               }
               break;
            case 180266360:
               if (var13.equals("IndiePad")) {
                  var14 = 24;
               }
               break;
            case 237817416:
               if (var13.equals("Infinity")) {
                  var14 = 72;
               }
               break;
            case 331565974:
               if (var13.equals("EnhancedMaxHp")) {
                  var14 = 17;
               }
               break;
            case 331566129:
               if (var13.equals("EnhancedMaxMp")) {
                  var14 = 16;
               }
               break;
            case 425840881:
               if (var13.equals("MesoGuard")) {
                  var14 = 12;
               }
               break;
            case 431659852:
               if (var13.equals("SharpEyes")) {
                  var14 = 76;
               }
               break;
            case 449617237:
               if (var13.equals("FlipTheCoin")) {
                  var14 = 40;
               }
               break;
            case 451741146:
               if (var13.equals("EnhancedMad")) {
                  var14 = 55;
               }
               break;
            case 451744029:
               if (var13.equals("EnhancedPad")) {
                  var14 = 25;
               }
               break;
            case 451744122:
               if (var13.equals("EnhancedPdd")) {
                  var14 = 54;
               }
               break;
            case 723179760:
               if (var13.equals("ExpBuffRate")) {
                  var14 = 11;
               }
               break;
            case 756936490:
               if (var13.equals("ExtremeArchery")) {
                  var14 = 28;
               }
               break;
            case 792521913:
               if (var13.equals("MesoUpByItem")) {
                  var14 = 8;
               }
               break;
            case 816987609:
               if (var13.equals("AdvancedBless")) {
                  var14 = 75;
               }
               break;
            case 840882621:
               if (var13.equals("BasicStatUp")) {
                  var14 = 58;
               }
               break;
            case 896278439:
               if (var13.equals("EnrageCrDamMin")) {
                  var14 = 0;
               }
               break;
            case 973642521:
               if (var13.equals("CombatOrders")) {
                  var14 = 37;
               }
               break;
            case 1074867018:
               if (var13.equals("RWCombination")) {
                  var14 = 67;
               }
               break;
            case 1162855128:
               if (var13.equals("ItemUpByItem")) {
                  var14 = 6;
               }
               break;
            case 1204233954:
               if (var13.equals("IgnoreTargetDEF")) {
                  var14 = 70;
               }
               break;
            case 1248310762:
               if (var13.equals("PartyBooster")) {
                  var14 = 22;
               }
               break;
            case 1292860813:
               if (var13.equals("IndieAsrR")) {
                  var14 = 43;
               }
               break;
            case 1292932733:
               if (var13.equals("IndieDamR")) {
                  var14 = 32;
               }
               break;
            case 1293200573:
               if (var13.equals("IndieMadR")) {
                  var14 = 29;
               }
               break;
            case 1293289946:
               if (var13.equals("IndiePadR")) {
                  var14 = 18;
               }
               break;
            case 1293301478:
               if (var13.equals("IndiePmdR")) {
                  var14 = 64;
               }
               break;
            case 1293413388:
               if (var13.equals("IndieTerR")) {
                  var14 = 45;
               }
               break;
            case 1379245331:
               if (var13.equals("MaxLevelBuff")) {
                  var14 = 13;
               }
               break;
            case 1381339935:
               if (var13.equals("DemonFrenzy")) {
                  var14 = 63;
               }
               break;
            case 1545334319:
               if (var13.equals("BlessingAnsanble")) {
                  var14 = 61;
               }
               break;
            case 1709464619:
               if (var13.equals("IndieStance")) {
                  var14 = 73;
               }
               break;
            case 1729588080:
               if (var13.equals("Booster")) {
                  var14 = 23;
               }
               break;
            case 1755599365:
               if (var13.equals("MHPCutR")) {
                  var14 = 33;
               }
               break;
            case 1826161346:
               if (var13.equals("DropItemRate")) {
                  var14 = 10;
               }
               break;
            case 1898745120:
               if (var13.equals("MMPCutR")) {
                  var14 = 34;
               }
               break;
            case 2072555113:
               if (var13.equals("IndieIgnoreMobPdpR")) {
                  var14 = 69;
               }
               break;
            case 2080491414:
               if (var13.equals("Enrage")) {
                  var14 = 65;
               }
            }

            int upwatk;
            switch(var14) {
            case 0:
               this.critical_damage = (short)(this.critical_damage + ((SecondaryStatValueHolder)buffs.getRight()).value);
               break;
            case 1:
               this.InsertFinalDamage(chra.getBuffedEffect(SecondaryStat.HolyUnity).getU());
               break;
            case 2:
               upwatk = chra.getBuffedEffect(SecondaryStat.ElementalCharge).getY();
               SecondaryStatEffect advcharge = SkillFactory.getSkill(1220010).getEffect(chra.getSkillLevel(1220010));
               if (chra.getSkillLevel(1220010) > 0) {
                  upwatk = advcharge.getY();
                  this.DamagePercent += (double)(chra.getElementalCharge() * advcharge.getX());
               }

               this.watk += chra.getElementalCharge() * upwatk;
               this.ASR += chra.getElementalCharge() * chra.getBuffedEffect(SecondaryStat.ElementalCharge).getU();
               break;
            case 3:
               this.watk += (((SecondaryStatValueHolder)buffs.getRight()).value - 1) * chra.getBuffedEffect(SecondaryStat.ComboCounter).getY();
               if (chra.getSkillLevel(1120003) <= 0 && chra.getSkillLevel(1110013) <= 0) {
                  break;
               }

               int up2 = false;
               if (chra.getSkillLevel(1120043) <= 0 && chra.getSkillLevel(1120003) <= 0) {
                  if (chra.getSkillLevel(1110013) > 0) {
                     if (chra.getSkillLevel(1110013) > 0) {
                        SkillFactory.getSkill(1110013).getEffect(chra.getSkillLevel(1110013)).getDAMRate();
                     } else {
                        boolean var28 = false;
                     }
                  }
               } else if (chra.getSkillLevel(1120043) > 0) {
                  int var10000 = SkillFactory.getSkill(1120003).getEffect(chra.getSkillLevel(1120003)).getV() + SkillFactory.getSkill(1120043).getEffect(chra.getSkillLevel(1120043)).getDAMRate();
               } else {
                  SkillFactory.getSkill(1120003).getEffect(chra.getSkillLevel(1120003)).getV();
               }

               if (chra.getSkillLevel(1120045) > 0) {
                  this.BossDamage += (((SecondaryStatValueHolder)buffs.getRight()).value - 1) * SkillFactory.getSkill(1120045).getEffect(1).getW();
               }
               break;
            case 4:
            case 5:
            case 6:
               this.dropBuff += (double)((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 7:
            case 8:
            case 9:
               if (buffname.equals("MesoUp")) {
                  if (((SecondaryStatValueHolder)buffs.getRight()).effect.getSourceId() != 8001535) {
                     this.mesoBuff += (double)((SecondaryStatValueHolder)buffs.getRight()).value;
                  }
               } else {
                  this.mesoBuff += (double)((SecondaryStatValueHolder)buffs.getRight()).value;
               }
               break;
            case 10:
               this.mesoBuff += (double)((SecondaryStatValueHolder)buffs.getRight()).value;
               this.dropBuff += (double)((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 11:
               this.expBuff += (double)((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 12:
               this.MesoGuardMeso += (double)((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 13:
               this.percent_atk += ((SecondaryStatValueHolder)buffs.getRight()).value;
               this.percent_matk += ((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 14:
               localmaxmp_ += (long)((SecondaryStatValueHolder)buffs.getRight()).value;
               this.before_maxmp += ((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 15:
               localmaxhp_ += (long)((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 16:
               localmaxmp_ += (long)((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 17:
               localmaxhp_ += (long)((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 18:
            case 19:
               this.WatkPercent += (double)((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 20:
               this.WatkPercent += (double)((SecondaryStatValueHolder)buffs.getRight()).value;
               this.MatkPercent += (double)((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 21:
            case 22:
            case 23:
               this.attackSpeed += ((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 24:
            case 25:
            case 26:
               this.watk += ((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 27:
               if (!GameConstants.isWildHunter(chra.getJob())) {
                  this.watk += ((SecondaryStatValueHolder)buffs.getRight()).value;
               }
               break;
            case 28:
               if (GameConstants.isBowMaster(chra.getJob())) {
                  this.watk += chra.getBuffedEffect((SecondaryStat)buffs.getLeft()).getAttackX();
               } else {
                  this.critical_damage = (short)(this.critical_damage + ((SecondaryStatValueHolder)buffs.getRight()).value);
               }
               break;
            case 29:
            case 30:
               this.magic += ((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 31:
            case 32:
               this.DamagePercent += (double)((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 33:
               this.percent_hp -= ((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 34:
               this.percent_mp -= ((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 35:
               this.percent_hp += ((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 36:
               this.percent_mp += ((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 37:
               this.combatOrders += ((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 38:
            case 39:
               this.critical_rate = (short)(this.critical_rate + ((SecondaryStatValueHolder)buffs.getRight()).value);
               break;
            case 40:
               this.critical_rate = (short)(this.critical_rate + chra.getBuffedEffect(SecondaryStat.FlipTheCoin).getX());
               break;
            case 41:
               this.critical_rate = (short)(this.critical_rate + chra.getBuffedEffect(SecondaryStat.EnrageCr).getZ());
               break;
            case 42:
               this.critical_rate = (short)(this.critical_rate + chra.getBuffedEffect(SecondaryStat.BullsEye).getZ());
               this.critical_damage = (short)(this.critical_damage + chra.getBuffedEffect(SecondaryStat.BullsEye).getY());
               break;
            case 43:
            case 44:
               this.ASR += ((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 45:
            case 46:
               this.TER += ((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 47:
               this.percent_hp += ((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 48:
               this.percent_mp += ((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 49:
               this.localstr += ((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 50:
               this.localdex += ((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 51:
               this.localint_ += ((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 52:
               this.localluk += ((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 53:
               this.localstr += ((SecondaryStatValueHolder)buffs.getRight()).value;
               this.localdex += ((SecondaryStatValueHolder)buffs.getRight()).value;
               this.localint_ += ((SecondaryStatValueHolder)buffs.getRight()).value;
               this.localluk += ((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 54:
               this.def += ((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 55:
               this.magic += ((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 56:
               this.def += ((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 57:
            case 58:
               double d = (double)((SecondaryStatValueHolder)buffs.getRight()).value / 100.0D;
               this.localstr = (int)((double)this.localstr + d * (double)this.str);
               this.localdex = (int)((double)this.localdex + d * (double)this.dex);
               this.localluk = (int)((double)this.localluk + d * (double)this.luk);
               this.localint_ = (int)((double)this.localint_ + d * (double)this.int_);
               break;
            case 59:
               upwatk = chra.getSkillLevel(64120006) > 0 ? 64120006 : (chra.getSkillLevel(64110005) > 0 ? 64110005 : 64100004);
               this.InsertFinalDamage(SkillFactory.getSkill(upwatk).getEffect(chra.getSkillLevel(upwatk)).getX() * chra.weaponChanges1.size());
               break;
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
               this.InsertFinalDamage(((SecondaryStatValueHolder)buffs.getRight()).value);
               break;
            case 66:
               this.InsertFinalDamage(chra.getBuffedEffect((SecondaryStat)buffs.getLeft()).getW());
               break;
            case 67:
               this.InsertFinalDamage(((SecondaryStatValueHolder)buffs.getRight()).value * chra.getBuffedEffect((SecondaryStat)buffs.getLeft()).getY());
               break;
            case 68:
               this.BossDamage += ((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 69:
            case 70:
               this.ignoreMobpdpR.add(((SecondaryStatValueHolder)buffs.getRight()).value);
               break;
            case 71:
               this.DamagePercent += (double)(chra.getBuffedEffect((SecondaryStat)buffs.getLeft()).getX() * chra.getArcaneAim());
               break;
            case 72:
               this.InsertFinalDamage(chra.getBuffedEffect((SecondaryStat)buffs.getLeft()).getQ() + (chra.getInfinity() - 1) * chra.getBuffedEffect((SecondaryStat)buffs.getLeft()).getDamage());
               this.stance += chra.getBuffedEffect((SecondaryStat)buffs.getLeft()).getProp();
               break;
            case 73:
               this.stance += ((SecondaryStatValueHolder)buffs.getRight()).value;
               break;
            case 74:
               if (!chra.getBuffedValue(60001217)) {
                  break;
               }

               int watk = chra.getBuffedEffect((SecondaryStat)buffs.getLeft()).getAttackX();
               int cr = chra.getBuffedEffect((SecondaryStat)buffs.getLeft()).getCr();
               int bdr = chra.getBuffedEffect((SecondaryStat)buffs.getLeft()).getBdR();
               int[] arrn = new int[]{61100008, 61110010, 61120013};
               int n = arrn.length;

               for(int i = 0; i < n; ++i) {
                  Integer skill = arrn[i];
                  if (chra.getSkillLevel(skill) > 0) {
                     watk += SkillFactory.getSkill(skill).getEffect(chra.getSkillLevel(skill)).getAttackX();
                     cr += SkillFactory.getSkill(skill).getEffect(chra.getSkillLevel(skill)).getCr();
                     bdr += SkillFactory.getSkill(skill).getEffect(chra.getSkillLevel(skill)).getBdR();
                     if (skill == 61120013 && (chra.getBuffedValue(61111008) || chra.getBuffedValue(61120008) || chra.getBuffedValue(61121053))) {
                        bdr += SkillFactory.getSkill(skill).getEffect(chra.getSkillLevel(skill)).getX();
                     }
                  }
               }

               this.watk += watk;
               this.critical_rate = (short)(this.critical_rate + cr);
               this.BossDamage += bdr;
               break;
            case 75:
               MapleCharacter owner = chra.getMap().getCharacter(chra.getBuffedOwner(2321005));
               if (owner != null && owner.getSkillLevel(2320050) > 0) {
                  this.BossDamage += SkillFactory.getSkill(2320050).getEffect(1).getBdR();
               }
               break;
            case 76:
               this.critical_rate = (short)(this.critical_rate + chra.getBuffedEffect((SecondaryStat)buffs.getLeft()).getX());
               this.critical_damage = (short)(this.critical_damage + chra.getBuffedEffect((SecondaryStat)buffs.getLeft()).getY());
               break;
            case 77:
               this.InsertFinalDamage(15);
               break;
            case 78:
               this.InsertFinalDamage((int)(chra.getSkillCustomValue0(23110005) * (long)chra.getBuffedEffect((SecondaryStat)buffs.getLeft()).getX()));
               break;
            case 79:
               upwatk = chra.getBlessofDarkness() == 1 ? chra.getBuffedEffect((SecondaryStat)buffs.getLeft()).getU() : (chra.getBlessofDarkness() == 2 ? chra.getBuffedEffect((SecondaryStat)buffs.getLeft()).getV() : (chra.getBlessofDarkness() == 3 ? chra.getBuffedEffect((SecondaryStat)buffs.getLeft()).getY() : 0));
               this.magic += upwatk;
               break;
            case 80:
               this.critical_damage += chra.getBuffedEffect((SecondaryStat)buffs.getLeft()).getCriticalDamage();
               this.ASR += chra.getBuffedEffect((SecondaryStat)buffs.getLeft()).getASRRate();
            }
         }

         return new Pair(localmaxhp_, localmaxmp_);
      }
   }

   public boolean checkEquipLevels(MapleCharacter chr, long gain) {
      boolean changed = false;
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      ArrayList<Equip> all = new ArrayList(this.equipLevelHandling);

      Equip eq;
      for(Iterator var7 = all.iterator(); var7.hasNext(); chr.forceReAddItem(eq.copy(), MapleInventoryType.EQUIPPED)) {
         eq = (Equip)var7.next();
         int lvlz = eq.getEquipLevel();
         eq.setItemEXP((int)((long)eq.getItemEXP() + gain));
         if (eq.getEquipLevel() > lvlz) {
            for(int i = eq.getEquipLevel() - lvlz; i > 0; --i) {
               Map<Integer, Map<String, Integer>> inc = ii.getEquipIncrements(eq.getItemId());
               if (inc != null && inc.containsKey(lvlz + i)) {
                  eq = ii.levelUpEquip(eq, (Map)inc.get(lvlz + i));
               }

               if (ii.getEquipSkills(eq.getItemId()) != null) {
                  Iterator var12 = ii.getEquipSkills(eq.getItemId()).iterator();

                  while(var12.hasNext()) {
                     int zzz = (Integer)var12.next();
                     Skill skil = SkillFactory.getSkill(zzz);
                     if (skil != null && skil.canBeLearnedBy(chr)) {
                        eq.setIncSkill(skil.getId());
                        chr.dropMessage(5, "Your skill has gained a levelup: " + skil.getName() + " +1");
                     }
                  }
               }
            }

            changed = true;
         }
      }

      if (changed) {
         chr.equipChanged();
         chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showNormalEffect(chr, 21, true));
         chr.getMap().broadcastMessage(chr, CField.EffectPacket.showNormalEffect(chr, 21, false), false);
      }

      return changed;
   }

   public boolean checkEquipDurabilitys(MapleCharacter chr, int gain) {
      return this.checkEquipDurabilitys(chr, gain, false);
   }

   public boolean checkEquipDurabilitys(MapleCharacter chr, int gain, boolean aboveZero) {
      if (chr.inPVP()) {
         return true;
      } else {
         ArrayList<Equip> all = new ArrayList(this.durabilityHandling);
         Iterator var5 = all.iterator();

         Equip eqq;
         while(var5.hasNext()) {
            eqq = (Equip)var5.next();
            if (eqq != null && eqq.getPosition() >= 0 == aboveZero) {
               eqq.setDurability(eqq.getDurability() + gain);
               if (eqq.getDurability() < 0) {
                  eqq.setDurability(0);
               }
            }
         }

         var5 = all.iterator();

         while(true) {
            while(var5.hasNext()) {
               eqq = (Equip)var5.next();
               if (eqq != null && eqq.getDurability() == 0 && eqq.getPosition() < 0) {
                  if (chr.getInventory(MapleInventoryType.EQUIP).isFull()) {
                     chr.getClient().getSession().writeAndFlush(CWvsContext.InventoryPacket.getInventoryFull());
                     chr.getClient().getSession().writeAndFlush(CWvsContext.InventoryPacket.getShowInventoryFull());
                     return false;
                  }

                  this.durabilityHandling.remove(eqq);
                  short pos = chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot();
                  MapleInventoryManipulator.unequip(chr.getClient(), eqq.getPosition(), pos, MapleInventoryType.EQUIP);
               } else if (eqq != null) {
                  chr.forceReAddItem(eqq.copy(), MapleInventoryType.EQUIPPED);
               }
            }

            return true;
         }
      }
   }

   public final void handleProfessionTool(MapleCharacter chra) {
      if (chra.getProfessionLevel(92000000) > 0 || chra.getProfessionLevel(92010000) > 0) {
         synchronized(chra.getInventory(MapleInventoryType.EQUIP)) {
            Iterator itera = chra.getInventory(MapleInventoryType.EQUIP).newList().iterator();

            Equip equip;
            do {
               if (!itera.hasNext()) {
                  return;
               }

               equip = (Equip)itera.next();
            } while((equip.getDurability() == 0 || equip.getItemId() / 10000 != 150 || chra.getProfessionLevel(92000000) <= 0) && (equip.getItemId() / 10000 != 151 || chra.getProfessionLevel(92010000) <= 0));

            if (equip.getDurability() > 0) {
               this.durabilityHandling.add(equip);
            }

            this.harvestingTool = equip.getPosition();
         }
      }

   }

   private void CalcPassive_Mastery(MapleCharacter player) {
      if (player.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11) == null) {
         this.passive_mastery = 0;
      } else {
         MapleWeaponType weaponType = GameConstants.getWeaponType(player.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11).getItemId());
         boolean acc = true;
         int skil;
         switch(weaponType) {
         case BOW:
            skil = GameConstants.isKOC(player.getJob()) ? 13100000 : 3100000;
            break;
         case CLAW:
            skil = 4100000;
            break;
         case CANE:
            skil = player.getTotalSkillLevel(24120006) > 0 ? 24120006 : 24100004;
            break;
         case HANDCANNON:
            skil = 5300005;
            break;
         case KATARA:
         case DAGGER:
            skil = player.getJob() >= 430 && player.getJob() <= 434 ? 4300000 : 4200000;
            break;
         case CROSSBOW:
            skil = GameConstants.isResist(player.getJob()) ? 33100000 : 3200000;
            break;
         case AXE1H:
         case BLUNT1H:
            skil = GameConstants.isResist(player.getJob()) ? 31100004 : (GameConstants.isKOC(player.getJob()) ? 11100000 : (player.getJob() > 112 ? 1200000 : 1100000));
            break;
         case AXE2H:
         case SWORD1H:
         case SWORD2H:
         case BLUNT2H:
            skil = GameConstants.isKOC(player.getJob()) ? 11100000 : (player.getJob() > 112 ? 1200000 : 1100000);
            break;
         case POLE_ARM:
            skil = GameConstants.isAran(player.getJob()) ? 21100000 : 1300000;
            break;
         case SPEAR:
            skil = 1300000;
            break;
         case KNUCKLE:
            skil = GameConstants.isKOC(player.getJob()) ? 15100001 : 5100001;
            break;
         case GUN:
            skil = GameConstants.isResist(player.getJob()) ? 35100000 : 5200000;
            break;
         case DUAL_BOW:
            skil = 23100005;
            break;
         case WAND:
         case STAFF:
            acc = false;
            skil = GameConstants.isResist(player.getJob()) ? 32100006 : (player.getJob() <= 212 ? 2100006 : (player.getJob() <= 222 ? 2200006 : (player.getJob() <= 232 ? 2300006 : (player.getJob() <= 2000 ? 12100007 : 22120002))));
            break;
         default:
            this.passive_mastery = 0;
            return;
         }

         if (player.getSkillLevel(skil) <= 0) {
            this.passive_mastery = 0;
         } else {
            SecondaryStatEffect eff = SkillFactory.getSkill(skil).getEffect(player.getTotalSkillLevel(skil));
            if (acc) {
               this.accuracy += eff.getX();
               if (skil == 35100000) {
                  this.watk += eff.getX();
               }
            } else {
               this.magic += eff.getX();
            }

            this.passive_mastery = eff.getMastery();
            this.trueMastery += eff.getMastery() + weaponType.getBaseMastery();
         }
      }
   }

   private void calculateFame(MapleCharacter player) {
      player.getTrait(MapleTrait.MapleTraitType.charm).addLocalExp(player.getFame());
      MapleTrait.MapleTraitType[] var2 = MapleTrait.MapleTraitType.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         MapleTrait.MapleTraitType t = var2[var4];
         player.getTrait(t).recalcLevel();
      }

   }

   public final byte passive_mastery() {
      return this.passive_mastery;
   }

   public int calculateMinBaseDamage(MapleCharacter player) {
      int minbasedamage = 0;
      int atk = player.getStat().getTotalWatk();
      if (atk == 0) {
         minbasedamage = 1;
      } else {
         Item weapon_item = player.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
         if (weapon_item != null) {
            MapleWeaponType weapon = GameConstants.getWeaponType(weapon_item.getItemId());
            if (player.getJob() == 110) {
               this.skil = SkillFactory.getSkill(1100000);
               this.skill = player.getSkillLevel(this.skil);
               this.sword = this.skill > 0 ? (double)((this.skil.getEffect(player.getSkillLevel(this.skil)).getMastery() * 5 + 10) / 100) : 0.1D;
            } else {
               this.skil = SkillFactory.getSkill(1200000);
               this.skill = player.getSkillLevel(this.skil);
               this.sword = this.skill > 0 ? (double)((this.skil.getEffect(player.getSkillLevel(this.skil)).getMastery() * 5 + 10) / 100) : 0.1D;
            }

            this.skil = SkillFactory.getSkill(1100001);
            this.skill = player.getSkillLevel(this.skil);
            this.axe = this.skill > 0 ? (double)((this.skil.getEffect(player.getSkillLevel(this.skil)).getMastery() * 5 + 10) / 100) : 0.1D;
            this.skil = SkillFactory.getSkill(1200001);
            this.skill = player.getSkillLevel(this.skil);
            this.blunt = this.skill > 0 ? (double)((this.skil.getEffect(player.getSkillLevel(this.skil)).getMastery() * 5 + 10) / 100) : 0.1D;
            this.skil = SkillFactory.getSkill(1300000);
            this.skill = player.getSkillLevel(this.skil);
            this.spear = this.skill > 0 ? (double)((this.skil.getEffect(player.getSkillLevel(this.skil)).getMastery() * 5 + 10) / 100) : 0.1D;
            this.skil = SkillFactory.getSkill(1300001);
            this.skill = player.getSkillLevel(this.skil);
            this.polearm = this.skill > 0 ? (double)((this.skil.getEffect(player.getSkillLevel(this.skil)).getMastery() * 5 + 10) / 100) : 0.1D;
            this.skil = SkillFactory.getSkill(3200000);
            this.skill = player.getSkillLevel(this.skil);
            this.CROSSBOW = this.skill > 0 ? (double)((this.skil.getEffect(player.getSkillLevel(this.skil)).getMastery() * 5 + 10) / 100) : 0.1D;
            this.skil = SkillFactory.getSkill(3100000);
            this.skill = player.getSkillLevel(this.skil);
            this.bow = this.skill > 0 ? (double)((this.skil.getEffect(player.getSkillLevel(this.skil)).getMastery() * 5 + 10) / 100) : 0.1D;
            if (weapon == MapleWeaponType.CROSSBOW) {
               minbasedamage = (int)((double)this.localdex * 0.9D * 3.6D * this.CROSSBOW + (double)this.localstr) / 100 * (atk + 15);
            }

            if (weapon == MapleWeaponType.BOW) {
               minbasedamage = (int)((double)this.localdex * 0.9D * 3.4D * this.bow + (double)this.localstr) / 100 * (atk + 15);
            }

            if (player.getJob() == 400 && weapon == MapleWeaponType.DAGGER) {
               minbasedamage = (int)((double)this.localluk * 0.9D * 3.6D * this.dagger + (double)this.localstr + (double)this.localdex) / 100 * atk;
            }

            if (player.getJob() != 400 && weapon == MapleWeaponType.DAGGER) {
               minbasedamage = (int)((double)this.localstr * 0.9D * 4.0D * this.dagger + (double)this.localdex) / 100 * atk;
            }

            if (player.getJob() == 400 && weapon == MapleWeaponType.CLAW) {
               minbasedamage = (int)((double)this.localluk * 0.9D * 3.6D * this.claw + (double)this.localstr + (double)this.localdex) / 100 * (atk + 15);
            }

            if (weapon == MapleWeaponType.SPEAR) {
               minbasedamage = (int)((double)this.localstr * 0.9D * 3.0D * this.spear + (double)this.localdex) / 100 * atk;
            }

            if (weapon == MapleWeaponType.POLE_ARM) {
               minbasedamage = (int)((double)this.localstr * 0.9D * 3.0D * this.polearm + (double)this.localdex) / 100 * atk;
            }

            if (weapon == MapleWeaponType.SWORD1H) {
               minbasedamage = (int)((double)this.localstr * 0.9D * 4.0D * this.sword + (double)this.localdex) / 100 * atk;
            }

            if (weapon == MapleWeaponType.SWORD2H) {
               minbasedamage = (int)((double)this.localstr * 0.9D * 4.6D * this.sword + (double)this.localdex) / 100 * atk;
            }

            if (weapon == MapleWeaponType.AXE1H) {
               minbasedamage = (int)((double)this.localstr * 0.9D * 3.2D * this.axe + (double)this.localdex) / 100 * atk;
            }

            if (weapon == MapleWeaponType.BLUNT1H) {
               minbasedamage = (int)((double)this.localstr * 0.9D * 3.2D * this.blunt + (double)this.localdex) / 100 * atk;
            }

            if (weapon == MapleWeaponType.AXE2H) {
               minbasedamage = (int)((double)this.localstr * 0.9D * 3.4D * this.axe + (double)this.localdex) / 100 * atk;
            }

            if (weapon == MapleWeaponType.BLUNT2H) {
               minbasedamage = (int)((double)this.localstr * 0.9D * 3.4D * this.blunt + (double)this.localdex) / 100 * atk;
            }

            if (weapon == MapleWeaponType.STAFF || weapon == MapleWeaponType.WAND) {
               minbasedamage = (int)((double)this.localstr * 0.9D * 3.0D * this.staffwand + (double)this.localdex) / 100 * atk;
            }
         }
      }

      return minbasedamage;
   }

   public final float calculateMaxBaseDamage(int watk) {
      MapleCharacter chra = (MapleCharacter)this.chr.get();
      if (chra == null) {
         return 0.0F;
      } else {
         float maxbasedamage;
         if (watk == 0) {
            maxbasedamage = 1.0F;
         } else {
            Item weapon_item = chra.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
            short job = chra.getJob();
            MapleWeaponType weapon = weapon_item == null ? MapleWeaponType.NOT_A_WEAPON : GameConstants.getWeaponType(weapon_item.getItemId());
            int secondarystat;
            int mainstat;
            switch(weapon) {
            case BOW:
            case CROSSBOW:
               mainstat = this.localdex;
               secondarystat = this.localstr;
               break;
            case CLAW:
            case KATARA:
            case DAGGER:
               if ((job < 400 || job > 434) && (job < 1400 || job > 1412)) {
                  mainstat = this.localstr;
                  secondarystat = this.localdex;
               } else {
                  mainstat = this.localluk;
                  secondarystat = this.localdex + this.localstr;
               }
               break;
            case CANE:
            case HANDCANNON:
            case AXE1H:
            case BLUNT1H:
            case AXE2H:
            case SWORD1H:
            case SWORD2H:
            case BLUNT2H:
            case POLE_ARM:
            case SPEAR:
            case DUAL_BOW:
            case WAND:
            case STAFF:
            default:
               mainstat = this.localstr;
               secondarystat = this.localdex;
               break;
            case KNUCKLE:
               mainstat = this.localstr;
               secondarystat = this.localdex;
               break;
            case GUN:
               mainstat = this.localdex;
               secondarystat = this.localstr;
               break;
            case NOT_A_WEAPON:
               if ((job < 500 || job > 522) && (job < 1500 || job > 1512) && (job < 3500 || job > 3512)) {
                  mainstat = 0;
                  secondarystat = 0;
               } else {
                  mainstat = this.localstr;
                  secondarystat = this.localdex;
               }
            }

            maxbasedamage = (weapon.getMaxDamageMultiplier() * (float)mainstat + (float)secondarystat) * (float)watk / 100.0F;
         }

         return maxbasedamage;
      }
   }

   public final void calculateMaxBaseDamage(int watk, int pvpDamage, MapleCharacter chra) {
   }

   public final float getHealHP() {
      return this.shouldHealHP;
   }

   public final float getHealMP() {
      return this.shouldHealMP;
   }

   public final void relocHeal(MapleCharacter chra) {
      short playerjob = chra.getJob();
      this.shouldHealHP = (float)(10 + this.recoverHP);
      this.shouldHealMP = GameConstants.isDemonSlayer(chra.getJob()) ? 0.0F : (float)(3 + this.mpRestore + this.recoverMP + this.localint_ / 10);
      this.mpRecoverTime = 0;
      this.hpRecoverTime = 0;
      Skill effect;
      int lvl4;
      if (playerjob != 111 && playerjob != 112) {
         if (playerjob != 1111 && playerjob != 1112) {
            if (GameConstants.isMercedes(playerjob)) {
               effect = SkillFactory.getSkill(20020109);
               lvl4 = chra.getSkillLevel(effect);
               if (lvl4 > 0) {
                  this.shouldHealHP += (float)((long)effect.getEffect(lvl4).getX() * this.localmaxhp / 100L);
                  this.hpRecoverTime = 4000;
                  this.shouldHealMP += (float)((long)effect.getEffect(lvl4).getX() * this.localmaxmp / 100L);
                  this.mpRecoverTime = 4000;
               }
            } else {
               int lvl;
               if ((playerjob == 3111 || playerjob == 3112) && (lvl = chra.getSkillLevel(effect = SkillFactory.getSkill(31110009))) > 0) {
                  this.shouldHealMP += (float)effect.getEffect(lvl).getY();
                  this.mpRecoverTime = 4000;
               }
            }
         } else {
            effect = SkillFactory.getSkill(11110000);
            lvl4 = chra.getSkillLevel(effect);
            if (lvl4 > 0) {
               this.shouldHealMP += (float)effect.getEffect(lvl4).getMp();
               this.mpRecoverTime = 4000;
            }
         }
      } else {
         effect = SkillFactory.getSkill(1110000);
         lvl4 = chra.getSkillLevel(effect);
         if (lvl4 > 0) {
            SecondaryStatEffect eff = effect.getEffect(lvl4);
            if (eff.getHp() > 0) {
               this.shouldHealHP += (float)eff.getHp();
               this.hpRecoverTime = 4000;
            }

            this.shouldHealMP += (float)eff.getMp();
            this.mpRecoverTime = 4000;
         }
      }

      if (chra.getChair() != 0) {
         this.shouldHealHP += 99.0F;
         this.shouldHealMP += 99.0F;
      } else {
         float recvRate;
         if (chra.getMap() != null && (recvRate = chra.getMap().getRecoveryRate()) > 0.0F) {
            this.shouldHealHP *= recvRate;
            this.shouldHealMP *= recvRate;
         }
      }

   }

   public final void connectData(MaplePacketLittleEndianWriter mplew) {
      mplew.writeShort(this.str);
      mplew.writeShort(this.dex);
      mplew.writeShort(this.int_);
      mplew.writeShort(this.luk);
      mplew.writeInt(this.hp);
      mplew.writeInt(this.maxhp);
      mplew.writeInt(this.mp);
      mplew.writeInt(this.maxmp);
   }

   public static int getSkillByJob(int skillID, int job) {
      if (GameConstants.isKOC(job)) {
         return skillID + 10000000;
      } else if (GameConstants.isAran(job)) {
         return skillID + 20000000;
      } else if (GameConstants.isEvan(job)) {
         return skillID + 20010000;
      } else if (GameConstants.isMercedes(job)) {
         return skillID + 20020000;
      } else if (GameConstants.isPhantom(job)) {
         return skillID + 20030000;
      } else if (GameConstants.isLuminous(job)) {
         return skillID + 20040000;
      } else if (GameConstants.isEunWol(job)) {
         return skillID + 20050000;
      } else if (GameConstants.isResist(job)) {
         return skillID + 30000000;
      } else if (GameConstants.isDemonSlayer(job)) {
         return skillID + 30010000;
      } else if (GameConstants.isDemonAvenger(job)) {
         return skillID + 31010000;
      } else if (GameConstants.isXenon(job)) {
         return skillID + 30020000;
      } else if (GameConstants.isMichael(job)) {
         return skillID + 50000000;
      } else if (GameConstants.isKaiser(job)) {
         return skillID + 60000000;
      } else if (GameConstants.isAngelicBuster(job)) {
         return skillID + 60010000;
      } else if (GameConstants.isKadena(job)) {
         return skillID + 60020000;
      } else if (GameConstants.isCain(job)) {
         return skillID + 60030000;
      } else if (GameConstants.isZero(job)) {
         return skillID + 100000000;
      } else if (GameConstants.isKinesis(job)) {
         return skillID + 140000000;
      } else if (GameConstants.isIllium(job)) {
         return skillID + 150000000;
      } else if (GameConstants.isArk(job)) {
         return skillID + 150010000;
      } else {
         return GameConstants.isAdel(job) ? skillID + 150020000 : skillID;
      }
   }

   public final int getSkillIncrement(int skillID) {
      return this.skillsIncrement.containsKey(skillID) ? (Integer)this.skillsIncrement.get(skillID) : 0;
   }

   public final int getElementBoost(Element key) {
      return this.elemBoosts.containsKey(key) ? (Integer)this.elemBoosts.get(key) : 0;
   }

   public final int getDamageIncrease(int key) {
      return this.damageIncrease.containsKey(key) ? (Integer)this.damageIncrease.get(key) + this.damX : this.damX;
   }

   public final int getAccuracy() {
      return this.accuracy;
   }

   public void heal_noUpdate(MapleCharacter chra) {
      this.setHp(this.getCurrentMaxHp(), chra);
      this.setMp(this.getCurrentMaxMp(chra), chra);
   }

   public void heal(MapleCharacter chra) {
      this.heal_noUpdate(chra);
      if (chra.getBattleGroundChr() != null) {
         chra.updateSingleStat(MapleStat.HP, (long)chra.getBattleGroundChr().getMaxHp());
         chra.updateSingleStat(MapleStat.MP, (long)chra.getBattleGroundChr().getMaxMp());
      } else {
         chra.updateSingleStat(MapleStat.HP, this.getCurrentMaxHp());
         chra.updateSingleStat(MapleStat.MP, this.getCurrentMaxMp(chra));
      }

   }

   public Pair<Long, Long> handleEquipAdditions(MapleItemInformationProvider ii, MapleCharacter chra, boolean first_login, Map<Skill, SkillEntry> sData, int itemId) {
      List<Triple<String, String, String>> additions = ii.getEquipAdditions(itemId);
      ii.getEquipIncrements(itemId);
      int skillid = 0;
      int skilllevel = 0;
      long localmaxhp_a = 0L;
      long localmaxmp_a = 0L;
      if (additions != null) {
         Iterator var14 = additions.iterator();

         label347:
         while(true) {
            Triple add;
            int right;
            int day;
            String s6;
            byte var35;
            do {
               boolean canJob;
               boolean canLevelx;
               String job;
               String level;
               do {
                  label331:
                  do {
                     while(true) {
                        do {
                           if (!var14.hasNext()) {
                              break label347;
                           }

                           add = (Triple)var14.next();
                        } while(((String)add.getMid()).contains("con"));

                        right = Integer.parseInt((String)add.getRight());
                        String s = (String)add.getLeft();
                        byte var19 = -1;
                        switch(s.hashCode()) {
                        case -1897135862:
                           if (s.equals("statinc")) {
                              var19 = 6;
                           }
                           break;
                        case -1068860032:
                           if (s.equals("mobdie")) {
                              var19 = 3;
                           }
                           break;
                        case 3029869:
                           if (s.equals("boss")) {
                              var19 = 2;
                           }
                           break;
                        case 109496913:
                           if (s.equals("skill")) {
                              var19 = 4;
                           }
                           break;
                        case 376267774:
                           if (s.equals("mobcategory")) {
                              var19 = 0;
                           }
                           break;
                        case 708288539:
                           if (s.equals("hpmpchange")) {
                              var19 = 5;
                           }
                           break;
                        case 1952151455:
                           if (s.equals("critical")) {
                              var19 = 1;
                           }
                        }

                        String[] split2;
                        String[] var25;
                        int var26;
                        int var27;
                        String x2;
                        String s5;
                        String s4;
                        byte var33;
                        switch(var19) {
                        case 0:
                           if (((String)add.getMid()).equals("damage")) {
                              this.dam_r *= ((double)right + 100.0D) / 100.0D;
                              this.bossdam_r += ((double)right + 100.0D) / 100.0D;
                           }
                           break;
                        case 1:
                           canJob = false;
                           canLevelx = false;
                           job = ii.getEquipAddReqs(itemId, (String)add.getLeft(), "job");
                           if (job != null) {
                              if (job.contains(",")) {
                                 split2 = job.split(",");
                                 var25 = split2;
                                 var26 = split2.length;

                                 for(var27 = 0; var27 < var26; ++var27) {
                                    x2 = var25[var27];
                                    if (chra.getJob() == Integer.parseInt(x2)) {
                                       canJob = true;
                                    }
                                 }
                              } else if (chra.getJob() == Integer.parseInt(job)) {
                                 canJob = true;
                              }
                           }

                           level = ii.getEquipAddReqs(itemId, (String)add.getLeft(), "level");
                           if (level != null && chra.getLevel() >= Integer.parseInt(level)) {
                              canLevelx = true;
                           }

                           if ((job != null && canJob || job == null) && (level != null && canLevelx || level == null)) {
                              s6 = (String)add.getMid();
                              var35 = -1;
                              switch(s6.hashCode()) {
                              case -1339126929:
                                 if (s6.equals("damage")) {
                                    var35 = 1;
                                 }
                                 break;
                              case 3449699:
                                 if (s6.equals("prop")) {
                                    var35 = 0;
                                 }
                              }

                              switch(var35) {
                              case 0:
                                 this.critical_rate += (short)right;
                                 continue;
                              case 1:
                                 this.critical_damage += (short)right;
                              }
                           }
                           break;
                        case 2:
                           s5 = ii.getEquipAddReqs(itemId, (String)add.getLeft(), "craft");
                           if (((String)add.getMid()).equals("damage") && (s5 == null || s5 != null && chra.getTrait(MapleTrait.MapleTraitType.craft).getLocalTotalExp() >= Integer.parseInt(s5))) {
                              this.bossdam_r *= ((double)right + 100.0D) / 100.0D;
                           }
                           break;
                        case 3:
                           s5 = ii.getEquipAddReqs(itemId, (String)add.getLeft(), "craft");
                           if (s5 != null && (s5 == null || chra.getTrait(MapleTrait.MapleTraitType.craft).getLocalTotalExp() < Integer.parseInt(s5))) {
                              break;
                           }

                           s4 = (String)add.getMid();
                           var33 = -1;
                           switch(s4.hashCode()) {
                           case 30924922:
                              if (s4.equals("mpIncOnMobDie")) {
                                 var33 = 1;
                              }
                              break;
                           case 1497939957:
                              if (s4.equals("hpIncOnMobDie")) {
                                 var33 = 0;
                              }
                           }

                           switch(var33) {
                           case 0:
                              this.hpRecover += right;
                              this.hpRecoverProp += 5;
                              continue;
                           case 1:
                              this.mpRecover += right;
                              this.mpRecoverProp += 5;
                           default:
                              continue;
                           }
                        case 4:
                           if (!first_login) {
                              break;
                           }

                           s5 = ii.getEquipAddReqs(itemId, (String)add.getLeft(), "craft");
                           if (s5 != null && (s5 == null || chra.getTrait(MapleTrait.MapleTraitType.craft).getLocalTotalExp() < Integer.parseInt(s5))) {
                              break;
                           }

                           s4 = (String)add.getMid();
                           var33 = -1;
                           switch(s4.hashCode()) {
                           case 3355:
                              if (s4.equals("id")) {
                                 var33 = 0;
                              }
                              break;
                           case 102865796:
                              if (s4.equals("level")) {
                                 var33 = 1;
                              }
                           }

                           switch(var33) {
                           case 0:
                              skillid = right;
                              continue;
                           case 1:
                              skilllevel = right;
                           default:
                              continue;
                           }
                        case 5:
                           s5 = (String)add.getMid();
                           byte var31 = -1;
                           switch(s5.hashCode()) {
                           case -2021566229:
                              if (s5.equals("mpChangerPerTime")) {
                                 var31 = 1;
                              }
                              break;
                           case 531104656:
                              if (s5.equals("hpChangerPerTime")) {
                                 var31 = 0;
                              }
                           }

                           switch(var31) {
                           case 0:
                              this.recoverHP += right;
                              continue;
                           case 1:
                              this.recoverMP += right;
                           default:
                              continue;
                           }
                        case 6:
                           canJob = false;
                           canLevelx = false;
                           job = ii.getEquipAddReqs(itemId, (String)add.getLeft(), "job");
                           if (job != null) {
                              if (job.contains(",")) {
                                 split2 = job.split(",");
                                 var25 = split2;
                                 var26 = split2.length;

                                 for(var27 = 0; var27 < var26; ++var27) {
                                    x2 = var25[var27];
                                    if (chra.getJob() == Integer.parseInt(x2)) {
                                       canJob = true;
                                    }
                                 }
                              } else if (chra.getJob() == Integer.parseInt(job)) {
                                 canJob = true;
                              }
                           }

                           level = ii.getEquipAddReqs(itemId, (String)add.getLeft(), "level");
                           if (level != null && chra.getLevel() >= Integer.parseInt(level)) {
                              canLevelx = true;
                           }
                           continue label331;
                        }
                     }
                  } while(!canJob && job != null);
               } while(!canLevelx && level != null);

               if (itemId != 1142367) {
                  break;
               }

               day = Calendar.getInstance().get(7);
            } while(day != 1 && day != 7);

            s6 = (String)add.getMid();
            var35 = -1;
            switch(s6.hashCode()) {
            case -1184305213:
               if (s6.equals("incACC")) {
                  var35 = 13;
               }
               break;
            case -1184302247:
               if (s6.equals("incDEX")) {
                  var35 = 3;
               }
               break;
            case -1184300782:
               if (s6.equals("incEVA")) {
                  var35 = 14;
               }
               break;
            case -1184297167:
               if (s6.equals("incINT")) {
                  var35 = 4;
               }
               break;
            case -1184294076:
               if (s6.equals("incLUK")) {
                  var35 = 5;
               }
               break;
            case -1184293742:
               if (s6.equals("incMAD")) {
                  var35 = 1;
               }
               break;
            case -1184293649:
               if (s6.equals("incMDD")) {
                  var35 = 12;
               }
               break;
            case -1184293513:
               if (s6.equals("incMHP")) {
                  var35 = 7;
               }
               break;
            case -1184293358:
               if (s6.equals("incMMP")) {
                  var35 = 9;
               }
               break;
            case -1184290859:
               if (s6.equals("incPAD")) {
                  var35 = 0;
               }
               break;
            case -1184290766:
               if (s6.equals("incPDD")) {
                  var35 = 11;
               }
               break;
            case -1184287373:
               if (s6.equals("incSTR")) {
                  var35 = 2;
               }
               break;
            case 67023625:
               if (s6.equals("incSpeed")) {
                  var35 = 15;
               }
               break;
            case 1941561644:
               if (s6.equals("incJump")) {
                  var35 = 6;
               }
               break;
            case 1941606875:
               if (s6.equals("incMHPr")) {
                  var35 = 8;
               }
               break;
            case 1941611680:
               if (s6.equals("incMMPr")) {
                  var35 = 10;
               }
            }

            switch(var35) {
            case 0:
               this.watk += right;
               break;
            case 1:
               this.magic += right;
               break;
            case 2:
               this.localstr += right;
               break;
            case 3:
               this.localdex += right;
               break;
            case 4:
               this.localint_ += right;
               break;
            case 5:
               this.localluk += right;
               break;
            case 6:
               this.jump += right;
               break;
            case 7:
               localmaxhp_a += (long)right;
               break;
            case 8:
               this.percent_hp += right;
               break;
            case 9:
               localmaxmp_a += (long)right;
               break;
            case 10:
               this.percent_mp += right;
               break;
            case 11:
               this.wdef += right;
               break;
            case 12:
               this.mdef += right;
               break;
            case 13:
               this.accuracy += right;
            case 14:
            default:
               break;
            case 15:
               this.speed += right;
            }
         }
      }

      if (skillid != 0 && skilllevel != 0) {
         sData.put(SkillFactory.getSkill(skillid), new SkillEntry((byte)skilllevel, (byte)0, -1L));
      }

      return new Pair(localmaxhp_a, localmaxmp_a);
   }

   public void recalcPVPRank(MapleCharacter chra) {
      this.pvpRank = 10;
      this.pvpExp = chra.getTotalBattleExp();

      for(int i = 0; i < 10; ++i) {
         if (this.pvpExp > GameConstants.getPVPExpNeededForLevel(i + 1)) {
            --this.pvpRank;
            this.pvpExp -= GameConstants.getPVPExpNeededForLevel(i + 1);
         }
      }

   }

   public int getHPPercent() {
      return (int)Math.ceil((double)this.hp * 100.0D / (double)this.localmaxhp);
   }

   public int getMPPercent() {
      return (int)Math.ceil((double)this.mp * 100.0D / (double)this.localmaxmp);
   }

   public int getForce(int room) {
      return this.demonForce.containsKey(room) ? (Integer)this.demonForce.get(room) : 0;
   }

   public final boolean isRangedJob(int job) {
      return GameConstants.isArcher(job) || GameConstants.isCannon(job) || GameConstants.isNightLord(job) || GameConstants.isCaptain(job) || GameConstants.isNightWalker(job) || GameConstants.isMechanic(job) || GameConstants.isAngelicBuster(job);
   }

   public final int d(int variable) {
      return (int)Math.floor((double)variable);
   }

   public int getRandomage(MapleCharacter player) {
      int maxdamage = (int)this.localmaxbasedamage;
      int mindamage = this.calculateMinBaseDamage(player);
      return Randomizer.rand(mindamage, maxdamage);
   }

   public static long getSerialVersionUID() {
      return -679541993413738569L;
   }

   public Map<Integer, Integer> getSetHandling() {
      return this.setHandling;
   }

   public Map<Integer, Integer> getSkillsIncrement() {
      return this.skillsIncrement;
   }

   public Map<Integer, Integer> getDamageIncrease() {
      return this.damageIncrease;
   }

   public Map<String, Integer> getApplyStatFinalDamage() {
      return this.ApplyStatFinalDamage;
   }

   public EnumMap<Element, Integer> getElemBoosts() {
      return this.elemBoosts;
   }

   public WeakReference<MapleCharacter> getChr() {
      return this.chr;
   }

   public Map<Integer, Integer> getDemonForce() {
      return this.demonForce;
   }

   public List<Equip> getDurabilityHandling() {
      return this.durabilityHandling;
   }

   public List<Equip> getEquipLevelHandling() {
      return this.equipLevelHandling;
   }

   public float getShouldHealHP() {
      return this.shouldHealHP;
   }

   public float getShouldHealMP() {
      return this.shouldHealMP;
   }

   public short getInt_() {
      return this.int_;
   }

   public long getMaxhp() {
      return this.maxhp;
   }

   public long getMaxmp() {
      return this.maxmp;
   }

   public short getCritical_rate() {
      return this.critical_rate;
   }

   public short getCritical_damage() {
      return this.critical_damage;
   }

   public byte getPassive_mastery() {
      return this.passive_mastery;
   }

   public int getLocalstr() {
      return this.localstr;
   }

   public int getLocaldex() {
      return this.localdex;
   }

   public int getLocalluk() {
      return this.localluk;
   }

   public int getLocalint_() {
      return this.localint_;
   }

   public int getMs_maxhp() {
      return this.ms_maxhp;
   }

   public int getMs_maxmp() {
      return this.ms_maxmp;
   }

   public long getLocalmaxhp() {
      return this.localmaxhp;
   }

   public long getLocalmaxmp() {
      return this.localmaxmp;
   }

   public int getMagic() {
      return this.magic;
   }

   public int getWatk() {
      return this.watk;
   }

   public double getFinalDamage() {
      double finaldamage = 0.0D;
      double t = 1.0D;
      Iterator var5 = this.FinalDamage.iterator();

      while(var5.hasNext()) {
         Integer i = (Integer)var5.next();
         if (i > 0) {
            t *= (double)(100 + i) / 100.0D;
         }
      }

      finaldamage = t * 100.0D - 100.0D;
      finaldamage = (double)Math.round(finaldamage);
      return finaldamage;
   }

   public int getBossDamage() {
      return this.BossDamage;
   }

   public double getIgnoreMobpdpR() {
      double pdr = 0.0D;
      double t = 1.0D;
      Iterator var5 = this.ignoreMobpdpR.iterator();

      while(var5.hasNext()) {
         Integer i = (Integer)var5.next();
         if (i > 0) {
            t *= (double)(100 - i) / 100.0D;
         }
      }

      pdr = 1.0D - t;
      pdr *= 100.0D;
      pdr = (double)Math.round(pdr);
      return pdr;
   }

   public void setIgnoreMobpdpR(List<Integer> ignoreMobpdpR) {
      this.ignoreMobpdpR = ignoreMobpdpR;
   }

   public boolean isEquippedWelcomeBackRing() {
      return this.equippedWelcomeBackRing;
   }

   public boolean isHasClone() {
      return this.hasClone;
   }

   public boolean isHasPartyBonus() {
      return this.hasPartyBonus;
   }

   public boolean isBerserk() {
      return this.Berserk;
   }

   public double getExpBuff() {
      return this.expBuff;
   }

   public double getExpBuffZero() {
      return this.expBuffZero;
   }

   public double getExpBuffUnion() {
      return this.expBuffUnion;
   }

   public double getDropBuff() {
      return this.dropBuff;
   }

   public double getMesoBuff() {
      return this.mesoBuff;
   }

   public double getCashBuff() {
      return this.cashBuff;
   }

   public double getMesoGuard() {
      return this.MesoGuard;
   }

   public double getMesoGuardMeso() {
      return this.MesoGuardMeso;
   }

   public double getExpMod() {
      return this.expMod;
   }

   public double getPickupRange() {
      return this.pickupRange;
   }

   public double getDam_r() {
      return this.dam_r;
   }

   public double getBossdam_r() {
      return this.bossdam_r;
   }

   public int getRecoverHP() {
      return this.recoverHP;
   }

   public int getRecoverMP() {
      return this.recoverMP;
   }

   public int getMpconReduce() {
      return this.mpconReduce;
   }

   public int getMpconPercent() {
      return this.mpconPercent;
   }

   public int getIncMesoProp() {
      return this.incMesoProp;
   }

   public int getCoolTimeR() {
      return this.coolTimeR;
   }

   public int getSuddenDeathR() {
      return this.suddenDeathR;
   }

   public int getExpLossReduceR() {
      return this.expLossReduceR;
   }

   public int getDAMreflect() {
      return this.DAMreflect;
   }

   public int getDAMreflect_rate() {
      return this.DAMreflect_rate;
   }

   public int getIgnoreDAMr() {
      return this.ignoreDAMr;
   }

   public int getIgnoreDAMr_rate() {
      return this.ignoreDAMr_rate;
   }

   public int getIgnoreDAM() {
      return this.ignoreDAM;
   }

   public int getIgnoreDAM_rate() {
      return this.ignoreDAM_rate;
   }

   public int getMpRestore() {
      return this.mpRestore;
   }

   public int getHpRecover() {
      return this.hpRecover;
   }

   public int getHpRecoverProp() {
      return this.hpRecoverProp;
   }

   public int getHpRecoverPercent() {
      return this.hpRecoverPercent;
   }

   public int getMpRecover() {
      return this.mpRecover;
   }

   public int getMpRecoverProp() {
      return this.mpRecoverProp;
   }

   public int getRecoveryUP() {
      return this.RecoveryUP;
   }

   public int getBuffUP() {
      return this.BuffUP;
   }

   public int getRecoveryUP_Skill() {
      return this.RecoveryUP_Skill;
   }

   public int getBuffUP_Skill() {
      return this.BuffUP_Skill;
   }

   public int getIncAllskill() {
      return this.incAllskill;
   }

   public int getCombatOrders() {
      return this.combatOrders;
   }

   public int getIgnoreTargetDEF() {
      return this.ignoreTargetDEF;
   }

   public int getDefRange() {
      return this.defRange;
   }

   public int getBuffUP_Summon() {
      return this.BuffUP_Summon;
   }

   public int getDodgeChance() {
      return this.dodgeChance;
   }

   public int getHarvestingTool() {
      return this.harvestingTool;
   }

   public int getEvaR() {
      return this.evaR;
   }

   public int getEquipmentBonusExp() {
      return this.equipmentBonusExp;
   }

   public int getDropMod() {
      return this.dropMod;
   }

   public int getCashMod() {
      return this.cashMod;
   }

   public int getLevelBonus() {
      return this.levelBonus;
   }

   public int getASR() {
      return this.ASR;
   }

   public int getTER() {
      return this.TER;
   }

   public int getPickRate() {
      return this.pickRate;
   }

   public int getDecreaseDebuff() {
      return this.decreaseDebuff;
   }

   public int getEquippedFairy() {
      return this.equippedFairy;
   }

   public int getEquippedSummon() {
      return this.equippedSummon;
   }

   public int getPercent_hp() {
      return this.percent_hp;
   }

   public int getBefore_maxhp() {
      return this.before_maxhp;
   }

   public int getPercent_mp() {
      return this.percent_mp;
   }

   public int getBefore_percent_mp() {
      return this.before_percent_mp;
   }

   public int getBefore_maxmp() {
      return this.before_maxmp;
   }

   public int getMulti_lateral_hp() {
      return this.multi_lateral_hp;
   }

   public int getMulti_lateral_mp() {
      return this.multi_lateral_mp;
   }

   public int getPercent_str() {
      return this.percent_str;
   }

   public int getPercent_dex() {
      return this.percent_dex;
   }

   public int getPercent_int() {
      return this.percent_int;
   }

   public int getPercent_luk() {
      return this.percent_luk;
   }

   public int getPercent_acc() {
      return this.percent_acc;
   }

   public int getPercent_atk() {
      return this.percent_atk;
   }

   public int getPercent_matk() {
      return this.percent_matk;
   }

   public int getPercent_wdef() {
      return this.percent_wdef;
   }

   public int getPercent_mdef() {
      return this.percent_mdef;
   }

   public int getPvpDamage() {
      return this.pvpDamage;
   }

   public int getHpRecoverTime() {
      return this.hpRecoverTime;
   }

   public int getMpRecoverTime() {
      return this.mpRecoverTime;
   }

   public int getDot() {
      return this.dot;
   }

   public int getDotTime() {
      return this.dotTime;
   }

   public int getQuestBonus() {
      return this.questBonus;
   }

   public int getPvpRank() {
      return this.pvpRank;
   }

   public int getPvpExp() {
      return this.pvpExp;
   }

   public int getWdef() {
      return this.wdef;
   }

   public int getMdef() {
      return this.mdef;
   }

   public int getTrueMastery() {
      return this.trueMastery;
   }

   public int getDamX() {
      return this.damX;
   }

   public int getDAMreduceR() {
      return this.DAMreduceR;
   }

   public int getRandCooldown() {
      return this.randCooldown;
   }

   public int getStance() {
      return this.stance;
   }

   public int getPpd() {
      return this.ppd;
   }

   public int getDamAbsorbShieldR() {
      return this.damAbsorbShieldR;
   }

   public double getMastery() {
      return this.Mastery;
   }

   public long getFixHp() {
      return this.fixHp;
   }

   public float getLocalmaxbasedamage() {
      return this.localmaxbasedamage;
   }

   public float getLocalmaxbasepvpdamage() {
      return this.localmaxbasepvpdamage;
   }

   public float getLocalmaxbasepvpdamageL() {
      return this.localmaxbasepvpdamageL;
   }

   public int getDef() {
      return this.def;
   }

   public int getElement_ice() {
      return this.element_ice;
   }

   public int getElement_fire() {
      return this.element_fire;
   }

   public int getElement_light() {
      return this.element_light;
   }

   public int getElement_psn() {
      return this.element_psn;
   }

   public int getReduceCooltime() {
      return this.reduceCooltime;
   }

   public double getSword() {
      return this.sword;
   }

   public double getBlunt() {
      return this.blunt;
   }

   public double getAxe() {
      return this.axe;
   }

   public double getSpear() {
      return this.spear;
   }

   public double getPolearm() {
      return this.polearm;
   }

   public double getClaw() {
      return this.claw;
   }

   public double getDagger() {
      return this.dagger;
   }

   public double getStaffwand() {
      return this.staffwand;
   }

   public double getCROSSBOW() {
      return this.CROSSBOW;
   }

   public double getBow() {
      return this.bow;
   }

   public int getSkill() {
      return this.skill;
   }

   public Skill getSkil() {
      return this.skil;
   }

   public ReentrantLock getLock() {
      return this.lock;
   }

   public static int[] getAllJobs() {
      return allJobs;
   }

   public static int[] getPvpSkills() {
      return pvpSkills;
   }

   public int getArc() {
      return this.arc;
   }

   public void setArc(int arc) {
      this.arc = arc;
   }

   public double getDamagePercent() {
      return this.DamagePercent;
   }

   public void InsertDamageIncrease(int key, int value) {
      if (this.damageIncrease.containsKey(key)) {
         Iterator var3 = this.damageIncrease.entrySet().iterator();

         while(var3.hasNext()) {
            Entry<Integer, Integer> skill = (Entry)var3.next();
            if ((Integer)skill.getKey() == key) {
               skill.setValue((Integer)skill.getValue() + value);
               break;
            }
         }
      } else {
         this.damageIncrease.put(key, value);
      }

   }

   public void InsertIgIncrease(int key, int value) {
      if (this.IgIncrease.containsKey(key)) {
         Iterator var3 = this.IgIncrease.entrySet().iterator();

         while(var3.hasNext()) {
            Entry<Integer, Integer> skill = (Entry)var3.next();
            if ((Integer)skill.getKey() == key) {
               skill.setValue((Integer)skill.getValue() + value);
               break;
            }
         }
      } else {
         this.IgIncrease.put(key, value);
      }

   }

   public void InsertBossDamIncrease(int key, int value) {
      if (this.BossDamIncrease.containsKey(key)) {
         Iterator var3 = this.BossDamIncrease.entrySet().iterator();

         while(var3.hasNext()) {
            Entry<Integer, Integer> skill = (Entry)var3.next();
            if ((Integer)skill.getKey() == key) {
               skill.setValue((Integer)skill.getValue() + value);
               break;
            }
         }
      } else {
         this.BossDamIncrease.put(key, value);
      }

   }

   public int BeforeStatWatk(MapleCharacter chr) {
      short job = chr.getJob();
      boolean statwatk = false;
      double mas = this.Mastery;
      mas = GameConstants.isMagician(chr.getJob()) ? (mas += 25.0D) : (!GameConstants.isArcher(job) && !GameConstants.isNightLord(job) && !GameConstants.isCaptain(job) && !GameConstants.isCannon(job) && !GameConstants.isNightWalker(job) && !GameConstants.isMechanic(job) && !GameConstants.isAngelicBuster(job) ? (mas += 20.0D) : (mas += 15.0D));
      if (GameConstants.isAngelicBuster(job)) {
         mas += 10.0D;
      }

      if (GameConstants.isKinesis(job) && (mas += (double)(chr.getCombo() / 30)) > 99.0D) {
         mas = 99.0D;
      }

      return (int)Math.round(mas / 100.0D * (double)this.AfterStatWatk(chr));
   }

   public int AfterStatWatk(MapleCharacter chr) {
      int statwatk = false;
      int mainstat = GameConstants.isWarrior(chr.getJob()) ? this.getTotalStr() : (GameConstants.isMagician(chr.getJob()) ? this.getTotalInt() : (GameConstants.isArcher(chr.getJob()) ? this.getTotalDex() : (GameConstants.isThief(chr.getJob()) ? this.getTotalLuk() : 0)));
      int secondstat = GameConstants.isWarrior(chr.getJob()) ? this.getTotalDex() : (GameConstants.isMagician(chr.getJob()) ? this.getTotalLuk() : (GameConstants.isArcher(chr.getJob()) ? this.getTotalStr() : (GameConstants.isThief(chr.getJob()) ? this.getTotalDex() : 0)));
      Item weapon = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
      if (weapon == null) {
         return 0;
      } else {
         int atk = GameConstants.isMagician(chr.getJob()) ? this.magic : this.watk;
         double atkpercent = GameConstants.isMagician(chr.getJob()) ? this.MatkPercent : this.WatkPercent;
         int statwatk;
         if (GameConstants.isXenon(chr.getJob())) {
            statwatk = (int)((double)((this.getTotalStr() + this.getTotalDex() + this.getTotalLuk()) * 4) * 0.01D * (double)atk * GameConstants.getWeponInt(weapon.getItemId()) * GameConstants.getJobInt(chr.getJob()) * ((100.0D + atkpercent) / 100.0D) * ((100.0D + this.DamagePercent) / 100.0D) * ((100.0D + this.getFinalDamage()) / 100.0D));
         } else {
            if (GameConstants.isPirate(chr.getJob())) {
               if (!GameConstants.isYeti(chr.getJob()) && !GameConstants.isViper(chr.getJob()) && !GameConstants.isCannon(chr.getJob()) && !GameConstants.isStriker(chr.getJob()) && !GameConstants.isEunWol(chr.getJob()) && !GameConstants.isArc(chr.getJob())) {
                  mainstat = this.getTotalDex();
                  secondstat = this.getTotalStr();
               } else {
                  mainstat = this.getTotalStr();
                  secondstat = this.getTotalDex();
               }
            } else if (GameConstants.isDemonAvenger(chr.getJob())) {
               mainstat = (int)((double)(this.shp / 14L) + (double)(this.shp - this.maxhp) / 17.5D);
               secondstat = this.getTotalStr();
            }

            statwatk = (int)Math.round((double)(mainstat * 4 + secondstat) * 0.01D * (double)atk * GameConstants.getWeponInt(weapon.getItemId()) * GameConstants.getJobInt(chr.getJob()) * ((100.0D + atkpercent) / 100.0D) * ((100.0D + this.DamagePercent) / 100.0D) * ((100.0D + this.getFinalDamage()) / 100.0D));
         }

         return statwatk;
      }
   }

   public int getNomarbdR() {
      return this.NomarbdR;
   }

   public int getStarforce() {
      return this.starforce;
   }

   public void InsertFinalDamage(int dam) {
      this.FinalDamage.add(dam);
   }

   public Pair<Long, Long> handleInnerSkills(MapleCharacter chra) {
      long localmaxhp_ = 0L;
      long localmaxmp_ = 0L;
      Iterator var6 = chra.getInnerSkills().iterator();

      while(var6.hasNext()) {
         InnerSkillValueHolder ISVH = (InnerSkillValueHolder)var6.next();
         byte x = ISVH.getSkillLevel();
         switch(ISVH.getSkillId()) {
         case 70000000:
            this.localstr += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getStrFX();
            break;
         case 70000001:
            this.localdex += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getDexFX();
            break;
         case 70000002:
            this.localint_ += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getIntX();
            break;
         case 70000003:
            this.localluk += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getLukFX();
            break;
         case 70000004:
            this.jump += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getPsdJump();
            break;
         case 70000005:
            this.speed += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getPsdSpeed();
            break;
         case 70000006:
         case 70000007:
            this.wdef += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getPddX();
            this.wdef += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getPddX();
            break;
         case 70000008:
            localmaxhp_ += (long)SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getMhpX();
            break;
         case 70000009:
            localmaxmp_ += (long)SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getMaxMpX();
            break;
         case 70000010:
            this.jump += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getPsdJump();
            break;
         case 70000011:
            this.speed += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getPsdSpeed();
            break;
         case 70000012:
            this.watk += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getAttackX();
            break;
         case 70000013:
            this.magic += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getMagicX();
            break;
         case 70000014:
            this.critical_rate = (short)(this.critical_rate + SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getCr());
            break;
         case 70000015:
         case 70000044:
            this.localstr += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getStrFX();
            this.localdex += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getStrFX();
            this.localint_ += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getStrFX();
            this.localluk += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getStrFX();
            break;
         case 70000016:
            --this.attackSpeed;
            break;
         case 70000017:
            this.localstr += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getStrFX();
            break;
         case 70000018:
            this.localdex += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getStrFX();
            break;
         case 70000019:
            this.localint_ += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getStrFX();
            break;
         case 70000020:
            this.localluk += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getStrFX();
         case 70000021:
         case 70000022:
         case 70000023:
         case 70000024:
         case 70000025:
         case 70000026:
         case 70000034:
         case 70000037:
         case 70000038:
         case 70000039:
         case 70000042:
         case 70000046:
         case 70000047:
         default:
            break;
         case 70000027:
            this.percent_hp += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getPercentHP();
            break;
         case 70000028:
            this.percent_mp += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getPercentMP();
            break;
         case 70000029:
         case 70000030:
            this.wdef += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getPddR();
            this.mdef += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getPddR();
            break;
         case 70000031:
            this.percent_hp += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getPercentHP();
            break;
         case 70000032:
            this.percent_mp += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getPercentMP();
            break;
         case 70000033:
            this.mesoBuff += (double)SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getMesoRate();
            break;
         case 70000035:
            this.BossDamage += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getBossDamage();
            break;
         case 70000036:
            this.NomarbdR += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getNbdR();
            break;
         case 70000040:
            this.watk += this.accuracy * (SecondaryStatEffect.parseEval("x * 2 + u (x / 2)", x) / 100);
            this.magic += this.accuracy * (SecondaryStatEffect.parseEval("x * 2 + u (x / 2)", x) / 100);
            break;
         case 70000041:
            this.watk += this.wdef * (SecondaryStatEffect.parseEval("x * 2 + u (x / 2)", x) / 100);
            this.magic += this.wdef * (SecondaryStatEffect.parseEval("x * 2 + u (x / 2)", x) / 100);
            break;
         case 70000043:
            this.critical_rate = (short)(this.critical_rate + SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getCr());
            break;
         case 70000045:
            this.randCooldown += x;
            break;
         case 70000048:
            this.BuffUP_Skill += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getBufftimeR();
            break;
         case 70000049:
            this.dropBuff += (double)SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getDropR();
            break;
         case 70000050:
            this.mesoBuff += (double)SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getMesoRate();
            break;
         case 70000051:
            this.localstr += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getStrFX();
            this.localdex += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getDexFX();
            break;
         case 70000052:
            this.localstr += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getStrFX();
            this.localint_ += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getIntFX();
            break;
         case 70000053:
            this.localstr += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getStrFX();
            this.localluk += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getLukFX();
            break;
         case 70000054:
            this.localdex += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getDexFX();
            this.localint_ += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getIntFX();
            break;
         case 70000055:
            this.localdex += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getDexFX();
            this.localluk += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getLukFX();
            break;
         case 70000056:
            this.localint_ += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getIntFX();
            this.localluk += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getLukFX();
            break;
         case 70000057:
            this.localdex += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getDexFX();
            this.localstr += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getStrFX();
            break;
         case 70000058:
            this.localint_ += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getIntFX();
            this.localstr += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getStrFX();
            break;
         case 70000059:
            this.localluk += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getLukFX();
            this.localstr += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getStrFX();
            break;
         case 70000060:
            this.localint_ += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getIntFX();
            this.localdex += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getDexFX();
            break;
         case 70000061:
            this.localluk += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getLukFX();
            this.localdex += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getDexFX();
            break;
         case 70000062:
            this.localluk += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getLukFX();
            this.localint_ += SkillFactory.getSkill(ISVH.getSkillId()).getEffect(x).getIntFX();
         }
      }

      return new Pair(localmaxhp_, localmaxmp_);
   }

   public int getAttackSpeed() {
      return this.attackSpeed;
   }

   public long test(MapleCharacter chr, MapleMonster mob, int skillid) {
      long dam = 0L;
      SecondaryStatEffect effect = SkillFactory.getSkill(skillid).getEffect(chr.getSkillLevel(skillid));
      if (effect != null) {
         int mainstat = GameConstants.isWarrior(chr.getJob()) ? this.getTotalStr() : (GameConstants.isMagician(chr.getJob()) ? this.getTotalInt() : (GameConstants.isArcher(chr.getJob()) ? this.getTotalDex() : (GameConstants.isThief(chr.getJob()) ? this.getTotalLuk() : 0)));
         int secondstat = GameConstants.isWarrior(chr.getJob()) ? this.getTotalDex() : (GameConstants.isMagician(chr.getJob()) ? this.getTotalLuk() : (GameConstants.isArcher(chr.getJob()) ? this.getTotalStr() : (GameConstants.isThief(chr.getJob()) ? this.getTotalDex() : 0)));
         Item weapon = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
         if (weapon == null) {
            return 0L;
         }

         if (GameConstants.isPirate(chr.getJob())) {
            if (!GameConstants.isViper(chr.getJob()) && !GameConstants.isCannon(chr.getJob()) && !GameConstants.isStriker(chr.getJob()) && !GameConstants.isEunWol(chr.getJob()) && !GameConstants.isArc(chr.getJob())) {
               mainstat = this.getTotalDex();
               secondstat = this.getTotalStr();
            } else {
               mainstat = this.getTotalStr();
               secondstat = this.getTotalDex();
            }
         }

         int atk = GameConstants.isMagician(chr.getJob()) ? this.magic : this.watk;
         double atkpercent = GameConstants.isMagician(chr.getJob()) ? this.MatkPercent : this.WatkPercent;
         double criint = (double)(this.critical_rate / 100 * ((35 + this.critical_damage) / 100) + 1);
         double igint = 100.0D - ((double)mob.getStats().getPDRate() - (double)mob.getStats().getPDRate() * (this.getIgnoreMobpdpR() / 100.0D));
         double own = (double)((mainstat * 4 + secondstat) * atk) * GameConstants.getWeponInt(weapon.getItemId()) * GameConstants.getJobInt(weapon.getItemId()) / 100.0D;
         double two = (double)(effect.getDamage() / 100) * criint * ((100.0D + atkpercent) / 100.0D) * ((100.0D + this.DamagePercent + (double)this.BossDamage) / 100.0D);
         double third = 1.0D * ((this.Mastery + 100.0D) / 200.0D) * ((this.getFinalDamage() + 100.0D) / 100.0D);
         dam = (long)(own * two * third);
         if (igint <= 0.0D) {
            dam = 1L;
         }
      }

      return dam;
   }
}
