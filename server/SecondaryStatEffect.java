package server;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleCoolDownValueHolder;
import client.MapleSkillManager;
import client.MapleStat;
import client.MapleTrait;
import client.PlayerStats;
import client.RangeAttack;
import client.SecondaryStat;
import client.SecondaryStatValueHolder;
import client.Skill;
import client.SkillFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.GameConstants;
import constants.ServerConstants;
import handling.channel.ChannelServer;
import handling.world.party.MaplePartyCharacter;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import provider.MapleData;
import provider.MapleDataTool;
import provider.MapleDataType;
import server.field.skill.MapleMagicSword;
import server.life.MapleMonster;
import server.maps.ForceAtom;
import server.maps.MapleAtom;
import server.maps.MapleDoor;
import server.maps.MapleExtractor;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleMist;
import server.maps.MapleSummon;
import server.maps.MechDoor;
import server.maps.SummonMovementType;
import tools.CaltechEval;
import tools.FileoutputUtil;
import tools.Pair;
import tools.Triple;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.SLFCGPacket;

public class SecondaryStatEffect implements Serializable {
   private static final long serialVersionUID = 9179541993413738569L;
   private byte mastery;
   private byte mobCount;
   private byte attackCount;
   private byte bulletCount;
   private byte reqGuildLevel;
   private byte period;
   private byte expR;
   private byte iceGageCon;
   private byte recipeUseCount;
   private byte recipeValidDay;
   private byte reqSkillLevel;
   private byte slotCount;
   private byte effectedOnAlly;
   private byte effectedOnEnemy;
   private byte type;
   private byte preventslip;
   private byte immortal;
   private byte bs;
   private byte powerCon;
   private short hp;
   private short hpFX;
   private short hcHp;
   private short mp;
   private short mhpR;
   private short mmpR;
   private short pad;
   private short padR;
   private short mad;
   private short madR;
   private short pdd;
   private short mdef;
   private short acc;
   private short avoid;
   private short hands;
   private short speed;
   private short jump;
   private short psdSpeed;
   private short psdJump;
   private short mdf;
   private short mpRCon;
   private short mpCon;
   private short hpCon;
   private short forceCon;
   private short comboConAran;
   private short bdR;
   private short damage;
   private short prop;
   private short subprop;
   private short emhp;
   private short emmp;
   private short epad;
   private short emad;
   private short epdd;
   private short emdd;
   private short ignoreMobpdpR;
   private short ignoreMobDamR;
   private short dot;
   private short dotTime;
   private short dotInterval;
   private short dotSuperpos;
   private short criticaldamage;
   private short pddX;
   private short mddX;
   private short pddR;
   private short mddR;
   private short asrR;
   private short terR;
   private short er;
   private short padX;
   private short madX;
   private short mesoR;
   private short thaw;
   private short selfDestruction;
   private short PVPdamage;
   private short indiePad;
   private short indiePadR;
   private short indieMad;
   private short indieDamReduceR;
   private short indieMadR;
   private short indiePMd;
   private short fatigueChange;
   private short onActive;
   private short str;
   private short dex;
   private short int_;
   private short luk;
   private short strX;
   private short dexX;
   private short intX;
   private short lukX;
   private short strFX;
   private short dexFX;
   private short intFX;
   private short lukFX;
   private short lifeId;
   private short imhp;
   private short immp;
   private short inflation;
   private short useLevel;
   private short mpConReduce;
   private short soulmpCon;
   private short indieDEX;
   private short indieCr;
   private short indieMhp;
   private short indieMmp;
   private short indieStance;
   private short indieAllStat;
   private short indieSpeed;
   private short indieBooster;
   private short indieJump;
   private short indieAcc;
   private short indieEva;
   private short indieEvaR;
   private short indiePdd;
   private short indieMdd;
   private short incPVPdamage;
   private short indieMhpR;
   private short indieMmpR;
   private short indieAsrR;
   private short indieTerR;
   private short indieDamR;
   private short indieBDR;
   private short indieCD;
   private short indieIgnoreMobpdpR;
   private short indiePddR;
   private short IndieExp;
   private short indieStatRBasic;
   private short indieCooltimeReduce;
   private short mobSkill;
   private short mobSkillLevel;
   private short indiePmdR;
   private short morph;
   private short lv2mhp;
   private short lv2mmp;
   private short bufftimeR;
   private short summonTimeR;
   private short killRecoveryR;
   private short dotHealHPPerSecondR;
   private short targetPlus;
   private short targetPlus_5th;
   private short pdR;
   private short arcX;
   private short nbdR;
   private short starX;
   private short mdR;
   private short strR;
   private short dexR;
   private short intR;
   private short lukR;
   private short dropR;
   private short lv2pad;
   private short lv2mad;
   private double hpR;
   private double hpRCon;
   private double mpR;
   private double expRPerM;
   private double t;
   private Map<MapleTrait.MapleTraitType, Integer> traits = new HashMap();
   private int duration;
   private int subTime;
   private int ppcon;
   private int ppReq;
   private int ppRecovery;
   private int sourceid;
   private int recipe;
   private int moveTo;
   private int stanceProp;
   private int u;
   private int u2;
   private int v;
   private int v2;
   private int w;
   private int w2;
   private int x;
   private int y;
   private int z;
   private int s;
   private int s2;
   private int q;
   private int q2;
   private int cr;
   private int itemCon;
   private int itemConNo;
   private int bulletConsume;
   private int moneyCon;
   private int damR;
   private int speedMax;
   private int accX;
   private int mhpX;
   private int mmpX;
   private int cooltime;
   private int cooltimeMS;
   private int coolTimeR;
   private int morphId = 0;
   private int expinc;
   private int exp;
   private int monsterRidingId;
   private int consumeOnPickup;
   private int range;
   private int price;
   private int extendPrice;
   private int charColor;
   private int interval;
   private int rewardMeso;
   private int totalprob;
   private int cosmetic;
   private int kp;
   private int damAbsorbShieldR;
   private int damPlus;
   private int nocoolProps;
   private boolean skill;
   private Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
   private ArrayList<Pair<Integer, Integer>> availableMap;
   private Point lt;
   private Point rb;
   private Point lt2;
   private Point rb2;
   private Point lt3;
   private Point rb3;
   private boolean energyChargeCooling = false;
   private boolean energyChargeActived = false;
   private int expBuff;
   private int itemup;
   private int mesoup;
   private int cashup;
   private int berserk;
   private int illusion;
   private int berserk2;
   private int cp;
   private int nuffSkill;
   private int eqskill1;
   private int eqskill2;
   private int eqskill3;
   private long starttime;
   private byte level;
   private List<Integer> petsCanConsume;
   private List<Integer> randomPickup;
   private List<Triple<Integer, Integer, Integer>> rewardItem;

   public static final SecondaryStatEffect loadSkillEffectFromData(MapleData source, int skillid, boolean overtime, int level, String variables) {
      return loadFromData(source, skillid, true, overtime, level, variables);
   }

   public static final SecondaryStatEffect loadItemEffectFromData(MapleData source, int itemid) {
      return loadFromData(source, itemid, false, false, 1, (String)null);
   }

   private static final Point parsePoint(String path, MapleData source, Point def, String variables, int level) {
      if (variables == null) {
         return MapleDataTool.getPoint(path, source, def);
      } else {
         MapleData dd = source.getChildByPath(path);
         if (dd == null) {
            return def;
         } else if (dd.getType() != MapleDataType.STRING) {
            return MapleDataTool.getPoint(path, source, def);
         } else {
            System.out.println("DATA : " + MapleDataTool.getString(dd));
            return null;
         }
      }
   }

   public static int parseEval(String data, int level) {
      String variables = "x";
      String dddd = data.replace(variables, String.valueOf(level));
      if (dddd.substring(0, 1).equals("-")) {
         dddd = !dddd.substring(1, 2).equals("u") && !dddd.substring(1, 2).equals("d") ? "n" + dddd.substring(1, dddd.length()) : "n(" + dddd.substring(1, dddd.length()) + ")";
      } else if (dddd.substring(0, 1).equals("=")) {
         dddd = dddd.substring(1, dddd.length());
      }

      return (int)(new CaltechEval(dddd.replace("\\r\\n", ""))).evaluate();
   }

   private static final int parseEval(String path, MapleData source, int def, String variables, int level) {
      if (variables == null) {
         return MapleDataTool.getIntConvert(path, source, def);
      } else {
         MapleData dd = source.getChildByPath(path);
         if (dd == null) {
            return def;
         } else if (dd.getType() != MapleDataType.STRING) {
            return MapleDataTool.getIntConvert(path, source, def);
         } else {
            String ddd = MapleDataTool.getString(dd).replace("y", "x").replace("X", "x");
            String dddd = ddd.replace(variables, String.valueOf(level));
            if (dddd.length() >= 3 && dddd.substring(0, 3).equals("log")) {
               dddd = dddd.replaceAll("\\(", "").replaceAll("\\)", "");
               double base = baseLog(Double.parseDouble(dddd.substring(5, level >= 10 ? 7 : 6)), Double.parseDouble(dddd.substring(3, 5)));
               String var10000 = String.valueOf(base);
               dddd = var10000 + dddd.substring(level >= 10 ? 7 : 6);
            } else if (dddd.substring(0, 1).equals("-")) {
               dddd = !dddd.substring(1, 2).equals("u") && !dddd.substring(1, 2).equals("d") ? "n" + dddd.substring(1, dddd.length()) : "n(" + dddd.substring(1, dddd.length()) + ")";
            } else if (dddd.substring(0, 1).equals("=")) {
               dddd = dddd.substring(1, dddd.length());
            }

            if (dddd.equals("2*u") || dddd.equals("n2*u")) {
               dddd = "2*0";
            }

            return (int)(new CaltechEval(dddd.replace("\\r\\n", ""))).evaluate();
         }
      }
   }

   private static double baseLog(double x, double base) {
      return Math.log10(x) / Math.log10(base);
   }

   private static SecondaryStatEffect loadFromData(MapleData source, int sourceid, boolean skill, boolean overTime, int level, String variables) {
      SecondaryStatEffect ret = new SecondaryStatEffect();

      try {
         ret.sourceid = sourceid;
         ret.skill = skill;
         ret.level = (byte)level;
         if (source == null) {
            return ret;
         }

         ret.duration = parseEval("time", source, -1, variables, level);
         ret.subTime = parseEval("subTime", source, -1, variables, level);
         ret.hp = (short)parseEval("hp", source, 0, variables, level);
         ret.hpFX = (short)parseEval("hpFX", source, 0, variables, level);
         ret.hcHp = (short)parseEval("hchp", source, 0, variables, level);
         ret.hpR = (double)parseEval("hpR", source, 0, variables, level) / 100.0D;
         ret.hpRCon = (double)parseEval("hpRCon", source, 0, variables, level) / 100.0D;
         ret.mp = (short)parseEval("mp", source, 0, variables, level);
         ret.mpR = (double)parseEval("mpR", source, 0, variables, level) / 100.0D;
         ret.ppRecovery = (short)parseEval("ppRecovery", source, 0, variables, level);
         ret.mhpR = (short)parseEval("mhpR", source, 0, variables, level);
         ret.mmpR = (short)parseEval("mmpR", source, 0, variables, level);
         ret.pddR = (short)parseEval("pddR", source, 0, variables, level);
         ret.mddR = (short)parseEval("mddR", source, 0, variables, level);
         ret.pdR = (short)parseEval("pdR", source, 0, variables, level);
         ret.strR = (short)parseEval("strR", source, 0, variables, level);
         ret.dexR = (short)parseEval("dexR", source, 0, variables, level);
         ret.intR = (short)parseEval("intR", source, 0, variables, level);
         ret.lukR = (short)parseEval("lukR", source, 0, variables, level);
         ret.arcX = (short)parseEval("arcX", source, 0, variables, level);
         ret.nbdR = (short)parseEval("nbdR", source, 0, variables, level);
         ret.starX = (short)parseEval("starX", source, 0, variables, level);
         ret.ignoreMobpdpR = (short)parseEval("ignoreMobpdpR", source, 0, variables, level);
         ret.ignoreMobDamR = (short)parseEval("ignoreMobDamR", source, 0, variables, level);
         ret.asrR = (short)parseEval("asrR", source, 0, variables, level);
         ret.terR = (short)parseEval("terR", source, 0, variables, level);
         ret.setBdR((short)parseEval("bdR", source, 0, variables, level));
         ret.damR = parseEval("damR", source, 0, variables, level);
         ret.mesoR = (short)parseEval("mesoR", source, 0, variables, level);
         ret.thaw = (short)parseEval("thaw", source, 0, variables, level);
         ret.padX = (short)parseEval("padX", source, 0, variables, level);
         ret.pddX = (short)parseEval("pddX", source, 0, variables, level);
         ret.mddX = (short)parseEval("mddX", source, 0, variables, level);
         ret.madX = (short)parseEval("madX", source, 0, variables, level);
         ret.dot = (short)parseEval("dot", source, 0, variables, level);
         ret.dotTime = (short)parseEval("dotTime", source, 0, variables, level);
         ret.dotInterval = (short)parseEval("dotInterval", source, 1, variables, level);
         ret.setDotSuperpos((short)parseEval("dotSuperpos", source, 0, variables, level));
         ret.criticaldamage = (short)parseEval("criticaldamage", source, 0, variables, level);
         ret.mpConReduce = (short)parseEval("mpConReduce", source, 0, variables, level);
         ret.soulmpCon = (short)parseEval("soulmpCon", source, 0, variables, level);
         ret.setForceCon((short)parseEval("forceCon", source, 0, variables, level));
         ret.mpCon = (short)parseEval("mpCon", source, 0, variables, level);
         ret.mpRCon = (short)parseEval("mpRCon", source, 0, variables, level);
         ret.hpCon = (short)parseEval("hpCon", source, 0, variables, level);
         ret.comboConAran = (short)parseEval("comboConAran", source, 0, variables, level);
         ret.prop = (short)parseEval("prop", source, 100, variables, level);
         ret.subprop = (short)parseEval("subProp", source, 100, variables, level);
         ret.mdR = (short)parseEval("mdR", source, 100, variables, level);
         ret.cooltime = Math.max(0, parseEval("cooltime", source, 0, variables, level));
         ret.cooltimeMS = Math.max(0, parseEval("cooltimeMS", source, 0, variables, level));
         ret.coolTimeR = Math.max(0, parseEval("coolTimeR", source, 0, variables, level));
         ret.interval = parseEval("interval", source, 0, variables, level);
         ret.expinc = parseEval("expinc", source, 0, variables, level);
         ret.exp = parseEval("exp", source, 0, variables, level);
         ret.range = parseEval("range", source, 0, variables, level);
         ret.morphId = parseEval("morph", source, 0, variables, level);
         ret.cp = parseEval("cp", source, 0, variables, level);
         ret.cosmetic = parseEval("cosmetic", source, 0, variables, level);
         ret.er = (short)parseEval("er", source, 0, variables, level);
         ret.ppcon = parseEval("ppCon", source, 0, variables, level);
         ret.ppReq = parseEval("ppReq", source, 0, variables, level);
         ret.ppRecovery = (short)parseEval("ppRecovery", source, 0, variables, level);
         ret.slotCount = (byte)parseEval("slotCount", source, 0, variables, level);
         ret.preventslip = (byte)parseEval("preventslip", source, 0, variables, level);
         ret.useLevel = (short)parseEval("useLevel", source, 0, variables, level);
         ret.nuffSkill = parseEval("nuffSkill", source, 0, variables, level);
         ret.mobCount = (byte)parseEval("mobCount", source, 1, variables, level);
         ret.immortal = (byte)parseEval("immortal", source, 0, variables, level);
         ret.iceGageCon = (byte)parseEval("iceGageCon", source, 0, variables, level);
         ret.expR = (byte)parseEval("expR", source, 0, variables, level);
         ret.expRPerM = (double)parseEval("expRPerM", source, 0, variables, level) / 100.0D;
         ret.dropR = (short)parseEval("expR", source, 0, variables, level);
         ret.reqGuildLevel = (byte)parseEval("reqGuildLevel", source, 0, variables, level);
         ret.period = (byte)parseEval("period", source, 0, variables, level);
         ret.type = (byte)parseEval("type", source, 0, variables, level);
         ret.bs = (byte)parseEval("bs", source, 0, variables, level);
         ret.mdf = (short)((byte)parseEval("MDF", source, 0, variables, level));
         ret.attackCount = (byte)parseEval("attackCount", source, 1, variables, level);
         ret.bulletCount = (byte)parseEval("bulletCount", source, 1, variables, level);
         ret.speedMax = parseEval("speedMax", source, 0, variables, level);
         ret.accX = parseEval("accX", source, 0, variables, level);
         ret.setMhpX(parseEval("mhpX", source, 0, variables, level));
         ret.mmpX = parseEval("mmpX", source, 0, variables, level);
         int priceUnit = parseEval("priceUnit", source, 0, variables, level);
         ret.indieDamReduceR = (short)parseEval("indieDamReduceR", source, 0, variables, level);
         ret.lv2mhp = (short)parseEval("lv2mhp", source, 0, variables, level);
         ret.lv2mmp = (short)parseEval("lv2mmp", source, 0, variables, level);
         ret.lv2pad = (short)parseEval("lv2pad", source, 0, variables, level);
         ret.lv2mad = (short)parseEval("lv2mad", source, 0, variables, level);
         ret.lt = parsePoint("lt", source, new Point(0, 0), variables, level);
         ret.rb = parsePoint("rb", source, new Point(0, 0), variables, level);
         ret.lt2 = parsePoint("lt2", source, new Point(0, 0), variables, level);
         ret.rb2 = parsePoint("rb2", source, new Point(0, 0), variables, level);
         ret.lt3 = parsePoint("lt3", source, new Point(0, 0), variables, level);
         ret.rb3 = parsePoint("rb3", source, new Point(0, 0), variables, level);
         ret.setBufftimeR((short)parseEval("bufftimeR", source, 0, variables, level));
         ret.summonTimeR = (short)parseEval("summonTimeR", source, 0, variables, level);
         ret.stanceProp = parseEval("stanceProp", source, 0, variables, level);
         ret.damAbsorbShieldR = parseEval("damAbsorbShieldR", source, 0, variables, level);
         ret.damPlus = parseEval("damPlus", source, 0, variables, level);
         ret.nocoolProps = parseEval("nocoolProps", source, 0, variables, level);
         ret.setKillRecoveryR((short)parseEval("killRecoveryR", source, 0, variables, level));
         ret.dotHealHPPerSecondR = (short)parseEval("dotHealHPPerSecondR", source, 0, variables, level);
         ret.targetPlus = (short)parseEval("targetPlus", source, 0, variables, level);
         ret.targetPlus_5th = (short)parseEval("targetPlus_5th", source, 0, variables, level);
         if (priceUnit > 0) {
            ret.price = parseEval("price", source, 0, variables, level) * priceUnit;
            ret.extendPrice = parseEval("extendPrice", source, 0, variables, level) * priceUnit;
         } else {
            ret.price = 0;
            ret.extendPrice = 0;
         }

         if (ret.skill || ret.duration <= -1) {
            ret.duration *= 1000;
            ret.subTime *= 1000;
         }

         ret.cooltime *= 1000;
         ret.dotTime = (short)(ret.dotTime * 1000);
         ret.dotInterval = (short)(ret.dotInterval * 1000);
         ret.mastery = (byte)parseEval("mastery", source, 0, variables, level);
         ret.pad = (short)parseEval("pad", source, 0, variables, level);
         ret.padR = (short)parseEval("padR", source, 0, variables, level);
         ret.setPdd((short)parseEval("pdd", source, 0, variables, level));
         ret.mad = (short)parseEval("mad", source, 0, variables, level);
         ret.madR = (short)parseEval("madR", source, 0, variables, level);
         ret.mdef = (short)parseEval("mdd", source, 0, variables, level);
         ret.emhp = (short)parseEval("emhp", source, 0, variables, level);
         ret.emmp = (short)parseEval("emmp", source, 0, variables, level);
         ret.epad = (short)parseEval("epad", source, 0, variables, level);
         ret.emad = (short)parseEval("emad", source, 0, variables, level);
         ret.epdd = (short)parseEval("epdd", source, 0, variables, level);
         ret.emdd = (short)parseEval("emdd", source, 0, variables, level);
         ret.acc = (short)parseEval("acc", source, 0, variables, level);
         ret.avoid = (short)parseEval("eva", source, 0, variables, level);
         ret.speed = (short)parseEval("speed", source, 0, variables, level);
         ret.jump = (short)parseEval("jump", source, 0, variables, level);
         ret.psdSpeed = (short)parseEval("psdSpeed", source, 0, variables, level);
         ret.psdJump = (short)parseEval("psdJump", source, 0, variables, level);
         ret.indieDEX = (short)parseEval("indieDEX", source, 0, variables, level);
         ret.indieCr = (short)parseEval("indieCr", source, 0, variables, level);
         ret.indiePad = (short)parseEval("indiePad", source, 0, variables, level);
         ret.indiePadR = (short)parseEval("indiePadR", source, 0, variables, level);
         ret.indieMad = (short)parseEval("indieMad", source, 0, variables, level);
         ret.indieMadR = (short)parseEval("indieMadR", source, 0, variables, level);
         ret.indiePMd = (short)parseEval("indiePMd", source, 0, variables, level);
         ret.indieMhp = (short)parseEval("indieMhp", source, 0, variables, level);
         ret.indieMmp = (short)parseEval("indieMmp", source, 0, variables, level);
         ret.indieBooster = (short)parseEval("indieBooster", source, 0, variables, level);
         ret.indieSpeed = (short)parseEval("indieSpeed", source, 0, variables, level);
         ret.indieJump = (short)parseEval("indieJump", source, 0, variables, level);
         ret.indieAcc = (short)parseEval("indieAcc", source, 0, variables, level);
         ret.indieEva = (short)parseEval("indieEva", source, 0, variables, level);
         ret.indieEvaR = (short)parseEval("indieEvaR", source, 0, variables, level);
         ret.indiePdd = (short)parseEval("indiePdd", source, 0, variables, level);
         ret.indieMdd = (short)parseEval("indieMdd", source, 0, variables, level);
         ret.indieDamR = (short)parseEval("indieDamR", source, 0, variables, level);
         ret.indieBDR = (short)parseEval("indieBDR", source, 0, variables, level);
         ret.indieCD = (short)parseEval("indieCD", source, 0, variables, level);
         ret.indieIgnoreMobpdpR = (short)parseEval("indieIgnoreMobpdpR", source, 0, variables, level);
         ret.indiePddR = (short)parseEval("indiePddR", source, 0, variables, level);
         ret.IndieExp = (short)parseEval("IndieExp", source, 0, variables, level);
         ret.indieStatRBasic = (short)parseEval("indieStatRBasic", source, 0, variables, level);
         ret.indieCooltimeReduce = (short)parseEval("indieCooltimeReduce", source, 0, variables, level);
         ret.indieAllStat = (short)parseEval("indieAllStat", source, 0, variables, level);
         ret.indieStance = (short)parseEval("indieStance", source, 0, variables, level);
         ret.setIndieMhpR((short)parseEval("indieMhpR", source, 0, variables, level));
         ret.indieMmpR = (short)parseEval("indieMmpR", source, 0, variables, level);
         ret.indieAsrR = (short)parseEval("indieAsrR", source, 0, variables, level);
         ret.indieTerR = (short)parseEval("indieTerR", source, 0, variables, level);
         ret.onActive = (short)parseEval("onActive", source, 0, variables, level);
         ret.str = (short)parseEval("str", source, 0, variables, level);
         ret.dex = (short)parseEval("dex", source, 0, variables, level);
         ret.int_ = (short)parseEval("int", source, 0, variables, level);
         ret.luk = (short)parseEval("luk", source, 0, variables, level);
         ret.strX = (short)parseEval("strX", source, 0, variables, level);
         ret.dexX = (short)parseEval("dexX", source, 0, variables, level);
         ret.intX = (short)parseEval("intX", source, 0, variables, level);
         ret.lukX = (short)parseEval("lukX", source, 0, variables, level);
         ret.strFX = (short)parseEval("strFX", source, 0, variables, level);
         ret.dexFX = (short)parseEval("dexFX", source, 0, variables, level);
         ret.intFX = (short)parseEval("intFX", source, 0, variables, level);
         ret.lukFX = (short)parseEval("lukFX", source, 0, variables, level);
         ret.expBuff = parseEval("expBuff", source, 0, variables, level);
         ret.cashup = parseEval("cashBuff", source, 0, variables, level);
         ret.itemup = parseEval("itemupbyitem", source, 0, variables, level);
         ret.mesoup = parseEval("mesoupbyitem", source, 0, variables, level);
         ret.berserk = parseEval("berserk", source, 0, variables, level);
         ret.berserk2 = parseEval("berserk2", source, 0, variables, level);
         ret.lifeId = (short)parseEval("lifeId", source, 0, variables, level);
         ret.inflation = (short)parseEval("inflation", source, 0, variables, level);
         ret.imhp = (short)parseEval("imhp", source, 0, variables, level);
         ret.immp = (short)parseEval("immp", source, 0, variables, level);
         ret.illusion = parseEval("illusion", source, 0, variables, level);
         ret.consumeOnPickup = parseEval("consumeOnPickup", source, 0, variables, level);
         ret.setIndiePmdR((short)parseEval("indiePMdR", source, 0, variables, level));
         ret.morph = (short)parseEval("morph", source, 0, variables, level);
         ret.kp = parseEval("kp", source, 0, variables, level);
         if (ret.consumeOnPickup == 1 && parseEval("party", source, 0, variables, level) > 0) {
            ret.consumeOnPickup = 2;
         }

         ret.charColor = 0;
         String cColor = MapleDataTool.getString("charColor", source, (String)null);
         if (cColor != null) {
            ret.charColor |= Integer.parseInt("0x" + cColor.substring(0, 2));
            ret.charColor |= Integer.parseInt("0x" + cColor.substring(2, 4) + "00");
            ret.charColor |= Integer.parseInt("0x" + cColor.substring(4, 6) + "0000");
            ret.charColor |= Integer.parseInt("0x" + cColor.substring(6, 8) + "000000");
         }

         ret.traits = new EnumMap(MapleTrait.MapleTraitType.class);
         MapleTrait.MapleTraitType[] var12 = MapleTrait.MapleTraitType.values();
         int var13 = var12.length;

         for(int var14 = 0; var14 < var13; ++var14) {
            MapleTrait.MapleTraitType t = var12[var14];
            int expz = parseEval(t.name() + "EXP", source, 0, variables, level);
            if (expz != 0) {
               ret.traits.put(t, expz);
            }
         }

         ret.recipe = parseEval("recipe", source, 0, variables, level);
         ret.recipeUseCount = (byte)parseEval("recipeUseCount", source, 0, variables, level);
         ret.recipeValidDay = (byte)parseEval("recipeValidDay", source, 0, variables, level);
         ret.reqSkillLevel = (byte)parseEval("reqSkillLevel", source, 0, variables, level);
         ret.powerCon = (byte)parseEval("powerCon", source, 0, variables, level);
         ret.effectedOnAlly = (byte)parseEval("effectedOnAlly", source, 0, variables, level);
         ret.effectedOnEnemy = (byte)parseEval("effectedOnEnemy", source, 0, variables, level);
         ret.petsCanConsume = new ArrayList();

         int dd;
         for(int i = 0; (dd = parseEval(String.valueOf(i), source, 0, variables, level)) > 0; ++i) {
            ret.petsCanConsume.add(dd);
         }

         MapleData mdd = source.getChildByPath("0");
         if (mdd != null && mdd.getChildren().size() > 0) {
            ret.mobSkill = (short)parseEval("mobSkill", mdd, 0, variables, level);
            ret.mobSkillLevel = (short)parseEval("level", mdd, 0, variables, level);
         } else {
            ret.mobSkill = 0;
            ret.mobSkillLevel = 0;
         }

         MapleData pd = source.getChildByPath("randomPickup");
         Iterator var24;
         MapleData lta;
         if (pd != null) {
            ret.randomPickup = new ArrayList();
            var24 = pd.iterator();

            while(var24.hasNext()) {
               lta = (MapleData)var24.next();
               ret.randomPickup.add(MapleDataTool.getInt(lta));
            }
         }

         MapleData ltd;
         if ((ltd = source.getChildByPath("lt")) != null) {
            ret.setLt((Point)ltd.getData());
            ret.rb = (Point)source.getChildByPath("rb").getData();
         }

         MapleData ltc;
         if ((ltc = source.getChildByPath("con")) != null) {
            ret.availableMap = new ArrayList();
            var24 = ltc.iterator();

            while(var24.hasNext()) {
               lta = (MapleData)var24.next();
               ret.availableMap.add(new Pair(MapleDataTool.getInt("sMap", lta, 0), MapleDataTool.getInt("eMap", lta, 999999999)));
            }
         }

         ret.fatigueChange = 0;
         int totalprob = 0;
         lta = source.getChildByPath("reward");
         if (lta != null) {
            ret.rewardMeso = parseEval("meso", lta, 0, variables, level);
            MapleData ltz = lta.getChildByPath("case");
            if (ltz != null) {
               ret.rewardItem = new ArrayList();

               MapleData lty;
               for(Iterator var18 = ltz.iterator(); var18.hasNext(); totalprob += MapleDataTool.getInt("prob", lty, 0)) {
                  lty = (MapleData)var18.next();
                  ret.rewardItem.add(new Triple(MapleDataTool.getInt("id", lty, 0), MapleDataTool.getInt("count", lty, 0), MapleDataTool.getInt("prop", lty, 0)));
               }
            }
         } else {
            ret.rewardMeso = 0;
         }

         ret.totalprob = totalprob;
         ret.cr = parseEval("cr", source, 0, variables, level);
         ret.t = (double)parseEval("t", source, 0, variables, level);
         ret.u = parseEval("u", source, 0, variables, level);
         ret.setU2(parseEval("u2", source, 0, variables, level));
         ret.v = parseEval("v", source, 0, variables, level);
         ret.v2 = parseEval("v2", source, 0, variables, level);
         ret.w = parseEval("w", source, 0, variables, level);
         ret.setW2(parseEval("w2", source, 0, variables, level));
         ret.x = parseEval("x", source, 0, variables, level);
         ret.y = parseEval("y", source, 0, variables, level);
         ret.z = parseEval("z", source, 0, variables, level);
         ret.s = parseEval("s", source, 0, variables, level);
         ret.setS2(parseEval("s2", source, 0, variables, level));
         ret.q = parseEval("q", source, 0, variables, level);
         ret.q2 = parseEval("q2", source, 0, variables, level);
         ret.damage = (short)parseEval("damage", source, 0, variables, level);
         ret.PVPdamage = (short)parseEval("PVPdamage", source, 0, variables, level);
         ret.incPVPdamage = (short)parseEval("incPVPDamage", source, 0, variables, level);
         ret.selfDestruction = (short)parseEval("selfDestruction", source, 0, variables, level);
         ret.bulletConsume = parseEval("bulletConsume", source, 0, variables, level);
         ret.moneyCon = parseEval("moneyCon", source, 0, variables, level);
         ret.itemCon = parseEval("itemCon", source, 0, variables, level);
         ret.itemConNo = parseEval("itemConNo", source, 0, variables, level);
         ret.moveTo = parseEval("moveTo", source, -1, variables, level);
         if (ret.skill) {
            switch(sourceid) {
            case 1005:
            case 10001005:
            case 10001215:
            case 20001005:
            case 20011005:
            case 20021005:
            case 20031005:
            case 20041005:
            case 20051005:
            case 30001005:
            case 30011005:
            case 30021005:
            case 50001005:
            case 50001215:
            case 60001005:
            case 60011005:
            case 60021005:
            case 60031005:
            case 100001005:
            case 140001005:
            case 150001005:
            case 150011005:
            case 150021005:
            case 160001005:
               ret.statups.put(SecondaryStat.MaxLevelBuff, new Pair(ret.x, ret.duration));
               break;
            case 1101004:
            case 1201004:
            case 1301004:
            case 2101008:
            case 2201010:
            case 2301008:
            case 3101002:
            case 3201002:
            case 3301010:
            case 4101003:
            case 4201002:
            case 4301002:
            case 4311009:
            case 5101006:
            case 5201003:
            case 5301002:
            case 11101024:
            case 12101004:
            case 13101023:
            case 14101022:
            case 15101002:
            case 15101022:
            case 22111020:
            case 23101002:
            case 24101005:
            case 27101004:
            case 31001001:
            case 31201002:
            case 32101005:
            case 33101012:
            case 35101006:
            case 36101004:
            case 37101003:
            case 51101003:
            case 63101010:
            case 64101003:
            case 151101005:
            case 152101007:
            case 155101005:
            case 162101013:
            case 164101005:
               ret.statups.put(SecondaryStat.Booster, new Pair(ret.x, ret.duration));
               break;
            case 1101006:
               ret.statups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(ret.indiePad), ret.duration));
               ret.statups.put(SecondaryStat.PowerGaurd, new Pair(ret.x, ret.duration));
               break;
            case 1101013:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.ComboCounter, new Pair(1, ret.duration));
               break;
            case 1121000:
            case 1221000:
            case 1321000:
            case 2121000:
            case 2221000:
            case 2321000:
            case 3121000:
            case 3221000:
            case 3321023:
            case 4121000:
            case 4221000:
            case 4341000:
            case 5121000:
            case 5221000:
            case 5321005:
            case 11121000:
            case 12121000:
            case 13121000:
            case 14121000:
            case 15121000:
            case 21121000:
            case 22171068:
            case 23121005:
            case 24121008:
            case 25121108:
            case 27121009:
            case 31121004:
            case 31221008:
            case 32121007:
            case 33121007:
            case 35121007:
            case 36121008:
            case 37121006:
            case 51121005:
            case 61121014:
            case 63121009:
            case 64121004:
            case 65121009:
            case 100001268:
            case 142121016:
            case 151121005:
            case 152121009:
            case 154121005:
            case 155121008:
            case 162121023:
            case 164121009:
               ret.statups.put(SecondaryStat.BasicStatUp, new Pair(ret.x, ret.duration));
               break;
            case 1121010:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.Enrage, new Pair(ret.x, ret.duration));
               ret.statups.put(SecondaryStat.EnrageCrDamMin, new Pair(ret.y, ret.duration));
               break;
            case 1121053:
            case 1221053:
            case 1321053:
            case 2121053:
            case 2221053:
            case 2321053:
            case 3121053:
            case 3221053:
            case 3321041:
            case 4121053:
            case 4221053:
            case 4341053:
            case 5121053:
            case 5221053:
            case 5321053:
            case 11121053:
            case 12121053:
            case 13121053:
            case 14121053:
            case 15121053:
            case 21121053:
            case 22171082:
            case 23121053:
            case 24121053:
            case 25121132:
            case 27121053:
            case 31121053:
            case 31221053:
            case 32121053:
            case 33121053:
            case 35121053:
            case 37121053:
            case 51121053:
            case 151121042:
            case 152121042:
            case 155121042:
               ret.statups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(ret.indieDamR), ret.duration));
               break;
            case 1121054:
               ret.statups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(ret.indiePad), ret.duration));
               ret.statups.put(SecondaryStat.IndieCr, new Pair(Integer.valueOf(ret.indieCr), ret.duration));
               ret.statups.put(SecondaryStat.Stance, new Pair(100, ret.duration));
               ret.statups.put(SecondaryStat.Asr, new Pair(ret.x, ret.duration));
               ret.statups.put(SecondaryStat.Ter, new Pair(ret.y, ret.duration));
               break;
            case 1200014:
            case 1220010:
               ret.statups.put(SecondaryStat.ElementalCharge, new Pair(1, ret.duration));
               break;
            case 1210016:
               ret.statups.put(SecondaryStat.BlessingArmor, new Pair(ret.x, ret.duration));
               ret.statups.put(SecondaryStat.BlessingArmorIncPad, new Pair(Integer.valueOf(ret.epad), ret.duration));
               break;
            case 1211010:
               ret.statups.put(SecondaryStat.Listonation, new Pair(ret.x, ret.duration));
               break;
            case 1211011:
               ret.statups.put(SecondaryStat.CombatOrders, new Pair(ret.x, ret.duration));
               break;
            case 1221015:
               ret.statups.put(SecondaryStat.IndiePmdR, new Pair(Integer.valueOf(ret.indiePmdR), ret.duration));
               break;
            case 1221016:
               ret.statups.put(SecondaryStat.NotDamaged, new Pair(ret.duration, ret.duration));
               break;
            case 1221054:
               ret.statups.put(SecondaryStat.IndieNotDamaged, new Pair(1, ret.duration));
               break;
            case 1301006:
               ret.statups.put(SecondaryStat.Pdd, new Pair(Integer.valueOf(ret.pdd), ret.duration));
               break;
            case 1301007:
               ret.statups.put(SecondaryStat.MaxHP, new Pair(ret.x, ret.duration));
               ret.statups.put(SecondaryStat.MaxMP, new Pair(ret.x, ret.duration));
               break;
            case 1311015:
               ret.statups.put(SecondaryStat.CrossOverChain, new Pair(ret.x, ret.duration));
               break;
            case 1320019:
               ret.statups.put(SecondaryStat.Reincarnation, new Pair(1, ret.duration));
               ret.statups.put(SecondaryStat.IndieNotDamaged, new Pair(1, ret.duration));
               break;
            case 1321015:
               ret.hpR = (double)ret.y / 100.0D;
               ret.statups.put(SecondaryStat.IgnoreTargetDEF, new Pair(ret.x, ret.duration));
               ret.statups.put(SecondaryStat.IndieBDR, new Pair(Integer.valueOf(ret.indieBDR), ret.duration));
               break;
            case 1321054:
               ret.statups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(ret.indiePad), ret.duration));
               ret.statups.put(SecondaryStat.AuraRecovery, new Pair(ret.x, ret.duration));
               break;
            case 2001002:
            case 12001001:
            case 22001012:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.MagicGaurd, new Pair(ret.x, ret.duration));
               break;
            case 2101001:
               ret.statups.put(SecondaryStat.IndieMad, new Pair(Integer.valueOf(ret.indieMad), ret.duration));
               break;
            case 2101010:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.WizardIgnite, new Pair(1, ret.duration));
               break;
            case 2111007:
            case 2211007:
            case 2311007:
            case 22161005:
            case 32111010:
               ret.mpCon = (short)ret.y;
               ret.duration = 0;
               ret.statups.put(SecondaryStat.TeleportMastery, new Pair(1, ret.duration));
               break;
            case 2111008:
            case 2211008:
            case 12101005:
            case 22141016:
               ret.statups.put(SecondaryStat.ElementalReset, new Pair(ret.x, ret.duration));
               break;
            case 2111016:
            case 2211017:
            case 2221045:
            case 2311016:
            case 27111008:
            case 32111021:
            case 152111014:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.TeleportMasteryRange, new Pair(ret.x, ret.duration));
               break;
            case 2121054:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.FireAura, new Pair(1, ret.duration));
               break;
            case 2201001:
               ret.statups.put(SecondaryStat.IndieMad, new Pair(Integer.valueOf(ret.indieMad), ret.duration));
               break;
            case 2221011:
               ret.duration = 1000;
               ret.statups.put(SecondaryStat.NotDamaged, new Pair(1, ret.duration));
               break;
            case 2311015:
               ret.statups.put(SecondaryStat.Triumph, new Pair(1, ret.duration));
               break;
            case 2321054:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.VengeanceOfAngel, new Pair(1, ret.duration));
               break;
            case 3101004:
               ret.statups.put(SecondaryStat.SoulArrow, new Pair(Integer.valueOf(ret.epad), ret.duration));
               ret.statups.put(SecondaryStat.EnhancedPad, new Pair(1, ret.duration));
               ret.statups.put(SecondaryStat.Concentration, new Pair(1, ret.duration));
               break;
            case 3121007:
               ret.statups.put(SecondaryStat.IndieDex, new Pair(Integer.valueOf(ret.indieDEX), ret.duration));
               ret.statups.put(SecondaryStat.Eva, new Pair(ret.x, ret.duration));
               break;
            case 3121016:
               ret.statups.put(SecondaryStat.AdvancedQuiver, new Pair(Integer.valueOf(ret.level), ret.duration));
               break;
            case 3121054:
               ret.statups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(ret.indiePad), ret.duration));
               ret.statups.put(SecondaryStat.Preparation, new Pair(1, ret.duration));
               break;
            case 3201004:
               ret.statups.put(SecondaryStat.SoulArrow, new Pair(Integer.valueOf(ret.epad), ret.duration));
               ret.statups.put(SecondaryStat.Concentration, new Pair(1, ret.duration));
               ret.statups.put(SecondaryStat.EnhancedPad, new Pair(1, ret.duration));
               break;
            case 3211011:
               ret.statups.put(SecondaryStat.Asr, new Pair(Integer.valueOf(ret.asrR), ret.duration));
               break;
            case 3221006:
               ret.statups.put(SecondaryStat.IndieDex, new Pair(Integer.valueOf(ret.indieDEX), ret.duration));
               ret.statups.put(SecondaryStat.Eva, new Pair(ret.x, ret.duration));
               break;
            case 3221054:
               ret.statups.put(SecondaryStat.IgnoreTargetDEF, new Pair(0, ret.duration));
               ret.statups.put(SecondaryStat.BullsEye, new Pair((ret.x << 8) + ret.y, ret.duration));
               ret.statups.put(SecondaryStat.IndieIgnoreMobPdpR, new Pair(Integer.valueOf(ret.indieIgnoreMobpdpR), ret.duration));
               ret.statups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(ret.indieDamR), ret.duration));
               break;
            case 3310006:
               ret.statups.put(SecondaryStat.IndiePmdR, new Pair(Integer.valueOf(ret.indiePmdR), ret.duration));
               break;
            case 3311012:
               ret.statups.put(SecondaryStat.IndieAsrR, new Pair(ret.s, ret.duration));
               break;
            case 3321036:
            case 3321038:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.IndieDamageReduce, new Pair(Integer.valueOf(ret.indieDamReduceR), ret.duration));
               break;
            case 3321040:
               ret.duration = ret.u * 1000;
               ret.statups.put(SecondaryStat.KeyDownMoving, new Pair(100, ret.duration));
               break;
            case 4001005:
            case 14001022:
               ret.statups.put(SecondaryStat.Speed, new Pair(Integer.valueOf(ret.speed), ret.duration));
               ret.statups.put(SecondaryStat.Jump, new Pair(Integer.valueOf(ret.jump), ret.duration));
               break;
            case 4101011:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.MarkofNightLord, new Pair(1, ret.duration));
               break;
            case 4111002:
            case 4211008:
            case 4331002:
               ret.statups.put(SecondaryStat.ShadowPartner, new Pair(ret.x, ret.duration));
               break;
            case 4121054:
               ret.statups.put(SecondaryStat.BleedingToxin, new Pair(1, ret.duration));
               ret.statups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(ret.indiePad), ret.duration));
               break;
            case 4201017:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.Steal, new Pair(1, ret.duration));
               break;
            case 4211003:
            case 4221018:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.PickPocket, new Pair(1, ret.duration));
               break;
            case 4221020:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.Murderous, new Pair(1, ret.duration));
               break;
            case 4341052:
               ret.statups.put(SecondaryStat.Asura, new Pair(100, ret.duration));
               break;
            case 4341054:
               ret.statups.put(SecondaryStat.WindBreakerFinal, new Pair(1, ret.duration));
               ret.statups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(ret.indieDamR), ret.duration));
               break;
            case 5001005:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.DashSpeed, new Pair(ret.x, ret.duration));
               ret.statups.put(SecondaryStat.DashJump, new Pair(ret.y, ret.duration));
               break;
            case 5100015:
            case 5110014:
            case 5120018:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.EnergyCharged, new Pair(1, ret.duration));
               break;
            case 5120011:
            case 5220012:
               ret.statups.put(SecondaryStat.DamageReduce, new Pair(ret.y, ret.duration));
               ret.statups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(ret.indieDamR), ret.duration));
               break;
            case 5121009:
               ret.statups.put(SecondaryStat.PartyBooster, new Pair(-2, ret.duration));
               break;
            case 5121015:
               ret.statups.put(SecondaryStat.IndiePadR, new Pair(Integer.valueOf(ret.indiePadR), ret.duration));
               break;
            case 5221015:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.GuidedBullet, new Pair(1, 0));
               break;
            case 5221018:
               ret.statups.put(SecondaryStat.IndiePadR, new Pair(Integer.valueOf(ret.indiePadR), ret.duration));
               ret.statups.put(SecondaryStat.IndieStance, new Pair(Integer.valueOf(ret.indieStance), ret.duration));
               ret.statups.put(SecondaryStat.Eva, new Pair(ret.x, ret.duration));
               ret.statups.put(SecondaryStat.IndieAsrR, new Pair(Integer.valueOf(ret.indieAsrR), ret.duration));
               ret.statups.put(SecondaryStat.IndieTerR, new Pair(Integer.valueOf(ret.indieTerR), ret.duration));
               break;
            case 5301003:
            case 5320008:
               ret.statups.put(SecondaryStat.IndieHp, new Pair(Integer.valueOf(ret.indieMhp), ret.duration));
               ret.statups.put(SecondaryStat.IndieMp, new Pair(Integer.valueOf(ret.indieMmp), ret.duration));
               ret.statups.put(SecondaryStat.IndieJump, new Pair(Integer.valueOf(ret.indieJump), ret.duration));
               ret.statups.put(SecondaryStat.IndieSpeed, new Pair(Integer.valueOf(ret.indieSpeed), ret.duration));
               ret.statups.put(SecondaryStat.IndieAllStat, new Pair(Integer.valueOf(ret.indieAllStat), ret.duration));
               break;
            case 5321010:
               ret.statups.put(SecondaryStat.Stance, new Pair(ret.x, ret.duration));
               break;
            case 5321054:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.IndieBooster, new Pair(-1, ret.duration));
               ret.statups.put(SecondaryStat.IndiePmdR, new Pair(-ret.y, ret.duration));
               ret.statups.put(SecondaryStat.Buckshot, new Pair(ret.x, ret.duration));
               break;
            case 9001004:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.IndieEva, new Pair(1, ret.duration));
               break;
            case 11121054:
               ret.statups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(ret.indieDamR), ret.duration));
               ret.statups.put(SecondaryStat.IndieIgnoreMobPdpR, new Pair(Integer.valueOf(ret.indieIgnoreMobpdpR), ret.duration));
               ret.statups.put(SecondaryStat.CosmicForge, new Pair(ret.x, ret.duration));
               break;
            case 12101022:
               ret.mpR = (double)ret.x / 100.0D;
               break;
            case 12101023:
               ret.statups.put(SecondaryStat.IndieMad, new Pair(Integer.valueOf(ret.indieMad), ret.duration));
               ret.statups.put(SecondaryStat.IndieBooster, new Pair(-ret.x, ret.duration));
               break;
            case 12101024:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.OrbitalExplosion, new Pair(1, ret.duration));
               break;
            case 12121003:
               ret.statups.put(SecondaryStat.DamageReduce, new Pair(ret.x, ret.duration));
               break;
            case 12121043:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.AddRange, new Pair(100, ret.duration));
               break;
            case 13001022:
               ret.statups.put(SecondaryStat.CygnusElementSkill, new Pair(1, 0));
               ret.statups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(ret.indieDamR), 0));
               break;
            case 13100022:
            case 13100027:
            case 13101022:
            case 13110022:
            case 13110027:
            case 13120003:
            case 13120010:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.TryflingWarm, new Pair(1, ret.duration));
               break;
            case 13101024:
               ret.statups.put(SecondaryStat.SoulArrow, new Pair(1, ret.duration));
               ret.statups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(ret.indiePad), ret.duration));
               ret.statups.put(SecondaryStat.CriticalIncrease, new Pair(ret.x, ret.duration));
               break;
            case 13110026:
               ret.statups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(ret.indiePad), ret.duration));
               break;
            case 13121004:
               ret.statups.put(SecondaryStat.IndieEvaR, new Pair(Integer.valueOf(ret.prop), ret.duration));
               break;
            case 13121005:
            case 33121004:
            case 400001002:
               ret.statups.put(SecondaryStat.SharpEyes, new Pair((ret.x << 8) + ret.y, ret.duration));
               break;
            case 14001021:
               ret.statups.put(SecondaryStat.ElementDarkness, new Pair(1, 0));
               break;
            case 14001023:
               ret.statups.put(SecondaryStat.DarkSight, new Pair(ret.x, ret.duration));
               break;
            case 14001027:
            case 14121016:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.ShadowBatt, new Pair(1, ret.duration));
               break;
            case 14111024:
               ret.statups.put(SecondaryStat.ShadowServant, new Pair(1, ret.duration));
               break;
            case 14121054:
               ret.statups.put(SecondaryStat.ShadowIllusion, new Pair(1, ret.duration));
               break;
            case 15121005:
               ret.statups.put(SecondaryStat.PartyBooster, new Pair(-2, ret.duration));
               break;
            case 15121054:
               ret.statups.put(SecondaryStat.StrikerHyperElectric, new Pair(ret.x, ret.duration));
               break;
            case 20040216:
            case 20040217:
            case 20040219:
            case 20040220:
               ret.duration = 0;
               break;
            case 20050286:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.PreReviveOnce, new Pair(1, ret.duration));
               break;
            case 21001003:
               ret.statups.put(SecondaryStat.Booster, new Pair(-ret.y, ret.duration));
               break;
            case 21001008:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.BodyPressure, new Pair(Integer.valueOf(ret.damage), ret.duration));
               break;
            case 21101005:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.AranDrain, new Pair(ret.x, ret.duration));
               break;
            case 21101006:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.SnowCharge, new Pair(ret.w, ret.duration));
               break;
            case 21111012:
               ret.statups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(ret.indiePad), ret.duration));
               ret.statups.put(SecondaryStat.IndieMad, new Pair(Integer.valueOf(ret.indieMad), ret.duration));
               break;
            case 21120022:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.BeyondNextAttackProb, new Pair(1, ret.duration));
               break;
            case 21120026:
               ret.duration = 10000;
               ret.statups.put(SecondaryStat.NotDamaged, new Pair(1, ret.duration));
               break;
            case 21121016:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.BeyondNextAttackProb, new Pair(2, ret.duration));
               break;
            case 22140013:
               ret.statups.put(SecondaryStat.IndieBooster, new Pair(-1, ret.duration));
               break;
            case 22171073:
               ret.statups.put(SecondaryStat.EnhancedMad, new Pair(Integer.valueOf(ret.emad), ret.duration));
               ret.statups.put(SecondaryStat.EnhancedPdd, new Pair(Integer.valueOf(ret.epdd), ret.duration));
               break;
            case 23111008:
               ret.statups.put(SecondaryStat.ElementalKnight, new Pair(1, ret.duration));
               break;
            case 23121004:
               ret.statups.put(SecondaryStat.IndiePadR, new Pair(Integer.valueOf(ret.indiePadR), ret.duration));
               ret.statups.put(SecondaryStat.EnhancedMaxHp, new Pair(Integer.valueOf(ret.emhp), ret.duration));
               break;
            case 23121054:
               ret.statups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(ret.indiePad), ret.duration));
               ret.statups.put(SecondaryStat.Stance, new Pair(ret.x, ret.duration));
               break;
            case 25101009:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.HiddenPossession, new Pair(1, ret.duration));
               break;
            case 25121030:
               ret.statups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 0));
               break;
            case 25121131:
            case 25121133:
               ret.statups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(ret.indieDamR), ret.duration));
               ret.statups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(ret.indiePad), ret.duration));
               ret.statups.put(SecondaryStat.IndieBDR, new Pair(Integer.valueOf(ret.indieBDR), ret.duration));
               ret.statups.put(SecondaryStat.IndieIgnoreMobPdpR, new Pair(Integer.valueOf(ret.indieIgnoreMobpdpR), ret.duration));
               ret.statups.put(SecondaryStat.IndieBooster, new Pair(-1, ret.duration));
               break;
            case 27101202:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.KeyDownAreaMoving, new Pair(16, ret.duration));
               break;
            case 27111005:
               ret.statups.put(SecondaryStat.IndiePdd, new Pair(Integer.valueOf(ret.indiePdd), ret.duration));
               break;
            case 27111006:
               ret.statups.put(SecondaryStat.EnhancedMad, new Pair(Integer.valueOf(ret.emad), ret.duration));
               break;
            case 27111009:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.EquilibriumLiberation, new Pair(1, ret.duration));
            case 15121004:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.ShadowPartner, new Pair(ret.x, ret.duration));
               break;
            case 27121006:
               ret.statups.put(SecondaryStat.ElementalReset, new Pair(ret.y, ret.duration));
               break;
            case 31101003:
               ret.statups.put(SecondaryStat.PowerGaurd, new Pair(ret.y, ret.duration));
               break;
            case 31111003:
               ret.hpR = (double)ret.x;
               break;
            case 31120045:
               ret.statups.put(SecondaryStat.NextAttackEnhance, new Pair(ret.x, ret.duration));
               break;
            case 31121002:
               ret.statups.put(SecondaryStat.DrainHp, new Pair(3, ret.duration));
               break;
            case 31121007:
               ret.statups.put(SecondaryStat.InfinityForce, new Pair(1, ret.duration));
               break;
            case 31121054:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.ShadowPartner, new Pair(ret.x, ret.duration));
               break;
            case 31211004:
               ret.statups.put(SecondaryStat.IndieHpR, new Pair(Integer.valueOf(ret.getIndieMhpR()), ret.duration));
               ret.statups.put(SecondaryStat.DiabloicRecovery, new Pair(ret.x, ret.duration));
               break;
            case 31221054:
               ret.statups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(ret.indieDamR), ret.duration));
               break;
            case 32001014:
            case 32100010:
            case 32110017:
            case 32120019:
               ret.duration = 0;
               break;
            case 32120044:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.TeleportMasteryRange, new Pair(ret.x, ret.duration));
               break;
            case 32121010:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.Enrage, new Pair(ret.x, ret.duration));
               ret.statups.put(SecondaryStat.EnrageCr, new Pair(ret.z, ret.duration));
               ret.statups.put(SecondaryStat.EnrageCrDamMin, new Pair(ret.y, ret.duration));
               break;
            case 32121056:
               ret.statups.put(SecondaryStat.AttackCountX, new Pair(Integer.valueOf(ret.attackCount), ret.duration));
               break;
            case 33001007:
            case 33001008:
            case 33001009:
            case 33001010:
            case 33001011:
            case 33001012:
            case 33001013:
            case 33001014:
            case 33001015:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.JaguarSummoned, new Pair((ret.criticaldamage << 8) + ret.asrR, ret.duration));
               break;
            case 33101003:
               ret.statups.put(SecondaryStat.SoulArrow, new Pair(1, ret.duration));
               ret.statups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(ret.indiePad), ret.duration));
               break;
            case 33101005:
               ret.statups.put(SecondaryStat.HowlingParty, new Pair(ret.x, ret.duration));
               break;
            case 33111011:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.DrawBack, new Pair(0, ret.duration));
               break;
            case 33121054:
               ret.statups.put(SecondaryStat.FinalAttackProp, new Pair(ret.x, ret.duration));
               ret.statups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(ret.indieDamR), ret.duration));
               break;
            case 35001002:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.EnhancedPad, new Pair(Integer.valueOf(ret.epad), ret.duration));
               ret.statups.put(SecondaryStat.EnhancedPdd, new Pair(Integer.valueOf(ret.epdd), ret.duration));
               ret.statups.put(SecondaryStat.IndieSpeed, new Pair(30, ret.duration));
               ret.statups.put(SecondaryStat.IndieBooster, new Pair(-1, ret.duration));
               ret.statups.put(SecondaryStat.Mechanic, new Pair(30, ret.duration));
               break;
            case 35101007:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.PerfectArmor, new Pair(ret.x, ret.duration));
               break;
            case 35111003:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.EnhancedPad, new Pair(Integer.valueOf(ret.epad), ret.duration));
               ret.statups.put(SecondaryStat.EnhancedPdd, new Pair(Integer.valueOf(ret.epdd), ret.duration));
               ret.statups.put(SecondaryStat.CriticalIncrease, new Pair(ret.cr, ret.duration));
               ret.statups.put(SecondaryStat.Mechanic, new Pair(30, ret.duration));
               ret.statups.put(SecondaryStat.RideVehicle, new Pair(1932016, ret.duration));
               break;
            case 35121055:
               ret.statups.put(SecondaryStat.BombTime, new Pair(ret.x, ret.duration));
               break;
            case 36001002:
               ret.statups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(ret.indiePad), ret.duration));
               break;
            case 36001005:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.PinPointRocket, new Pair(1, ret.duration));
               break;
            case 36101003:
               ret.statups.put(SecondaryStat.IndieHpR, new Pair(Integer.valueOf(ret.getIndieMhpR()), ret.duration));
               ret.statups.put(SecondaryStat.IndieMpR, new Pair(Integer.valueOf(ret.getIndieMhpR()), ret.duration));
               break;
            case 36111004:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.AegisSystem, new Pair(1, ret.duration));
               break;
            case 36111006:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.ShadowPartner, new Pair(ret.x, ret.duration));
               break;
            case 36121003:
               ret.statups.put(SecondaryStat.IndieBDR, new Pair(Integer.valueOf(ret.indieBDR), ret.duration));
               ret.statups.put(SecondaryStat.IndiePmdR, new Pair(Integer.valueOf(ret.indiePmdR), ret.duration));
               break;
            case 36121007:
               ret.statups.put(SecondaryStat.OnCapsule, new Pair(20, ret.duration));
               break;
            case 37121054:
               ret.statups.put(SecondaryStat.RWMaximizeCannon, new Pair(ret.x, ret.duration));
               break;
            case 51101004:
               ret.statups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(ret.indiePad), ret.duration));
               break;
            case 51120003:
               ret.statups.put(SecondaryStat.DamageDecreaseWithHP, new Pair(ret.y, ret.duration));
               break;
            case 51121006:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.Enrage, new Pair(ret.x, ret.duration));
               ret.statups.put(SecondaryStat.EnrageCrDamMin, new Pair(ret.y, ret.duration));
               break;
            case 51121054:
               ret.statups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(ret.indieDamR), ret.duration));
               ret.statups.put(SecondaryStat.IndieHpR, new Pair(Integer.valueOf(ret.getIndieMhpR()), ret.duration));
               break;
            case 51121059:
               ret.statups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(ret.indieDamR), ret.duration));
               break;
            case 60001216:
            case 60001217:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.ReshuffleSwitch, new Pair(0, ret.duration));
               break;
            case 60011219:
               ret.statups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(ret.indieDamR), ret.duration));
               break;
            case 61101002:
            case 61110211:
            case 61120007:
            case 61121217:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.StopForceAtominfo, new Pair(ret.cooltime / 1000, ret.duration));
               break;
            case 61101004:
               ret.statups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(ret.indiePad), ret.duration));
               ret.statups.put(SecondaryStat.Booster, new Pair(-2, ret.duration));
               break;
            case 61111003:
               ret.statups.put(SecondaryStat.Asr, new Pair(Integer.valueOf(ret.asrR), ret.duration));
               ret.statups.put(SecondaryStat.Ter, new Pair(Integer.valueOf(ret.terR), ret.duration));
               break;
            case 61111008:
            case 61120008:
            case 61121053:
               ret.statups.put(SecondaryStat.IndiePmdR, new Pair(Integer.valueOf(ret.indiePmdR), ret.duration));
               ret.statups.put(SecondaryStat.CriticalIncrease, new Pair(ret.cr, ret.duration));
               ret.statups.put(SecondaryStat.Stance, new Pair(100, ret.duration));
               ret.statups.put(SecondaryStat.Speed, new Pair(Integer.valueOf(ret.speed), ret.duration));
               ret.statups.put(SecondaryStat.Jump, new Pair(Integer.valueOf(ret.jump), ret.duration));
               break;
            case 64001007:
            case 64001008:
            case 64001009:
            case 64001010:
            case 64001011:
            case 64001012:
               ret.duration = 2000;
               ret.statups.put(SecondaryStat.DarkSight, new Pair(10, ret.duration));
               break;
            case 64121053:
               ret.statups.put(SecondaryStat.BonusAttack, new Pair(ret.x, ret.duration));
               break;
            case 64121054:
               ret.statups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(ret.indieDamR), ret.duration));
               ret.statups.put(SecondaryStat.IndieCr, new Pair(Integer.valueOf(ret.indieCr), ret.duration));
               break;
            case 65001002:
               ret.statups.put(SecondaryStat.IndieBooster, new Pair(-2, ret.duration));
               break;
            case 65121003:
               ret.duration = 8000;
               ret.statups.put(SecondaryStat.SoulResonance, new Pair(ret.y, ret.duration));
               break;
            case 65121004:
               ret.statups.put(SecondaryStat.SoulGazeCriDamR, new Pair(ret.x, ret.duration));
               break;
            case 65121011:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.SoulSeekerExpert, new Pair(ret.x, ret.duration));
               break;
            case 65121053:
               ret.statups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(ret.indiePad), ret.duration));
               ret.statups.put(SecondaryStat.CriticalIncrease, new Pair(ret.x, ret.duration));
               ret.statups.put(SecondaryStat.Asr, new Pair(Integer.valueOf(ret.asrR), ret.duration));
               ret.statups.put(SecondaryStat.Ter, new Pair(Integer.valueOf(ret.terR), ret.duration));
               break;
            case 65121054:
               ret.statups.put(SecondaryStat.IndieIgnoreMobPdpR, new Pair(Integer.valueOf(ret.indieIgnoreMobpdpR), ret.duration));
               ret.statups.put(SecondaryStat.IndieBDR, new Pair(Integer.valueOf(ret.indieBDR), ret.duration));
               ret.statups.put(SecondaryStat.SoulExalt, new Pair(ret.x, ret.duration));
               break;
            case 80000169:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.PreReviveOnce, new Pair(1, ret.duration));
               break;
            case 80001140:
               ret.statups.put(SecondaryStat.IndieStance, new Pair(100, ret.duration));
               break;
            case 80001155:
               ret.statups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(ret.indieDamR), ret.duration));
               break;
            case 80001242:
               ret.duration = Integer.MAX_VALUE;
               ret.statups.put(SecondaryStat.NewFlying, new Pair(1, ret.duration));
               break;
            case 80001427:
               ret.statups.put(SecondaryStat.IndieJump, new Pair(130, ret.duration));
               ret.statups.put(SecondaryStat.IndieSpeed, new Pair(150, ret.duration));
               ret.statups.put(SecondaryStat.IndieBooster, new Pair(-3, ret.duration));
               break;
            case 80001432:
               ret.statups.put(SecondaryStat.IndieDamR, new Pair(100, ret.duration));
               break;
            case 80001455:
               ret.statups.put(SecondaryStat.IndiePadR, new Pair(Integer.valueOf(ret.indiePadR), ret.duration));
               ret.statups.put(SecondaryStat.IndieMadR, new Pair(Integer.valueOf(ret.indieMadR), ret.duration));
               break;
            case 80001456:
               ret.statups.put(SecondaryStat.SetBaseDamageByBuff, new Pair(2000000, ret.duration));
               break;
            case 80001457:
               ret.statups.put(SecondaryStat.LimitMP, new Pair(500, ret.duration));
               ret.statups.put(SecondaryStat.IndieBDR, new Pair(Integer.valueOf(ret.indieBDR), ret.duration));
               break;
            case 80001458:
               ret.statups.put(SecondaryStat.MHPCutR, new Pair(ret.x, ret.duration));
               ret.statups.put(SecondaryStat.IndieBDR, new Pair(Integer.valueOf(ret.indieBDR), ret.duration));
               break;
            case 80001459:
               ret.statups.put(SecondaryStat.MMPCutR, new Pair(ret.x, ret.duration));
               ret.statups.put(SecondaryStat.IndieIgnoreMobPdpR, new Pair(Integer.valueOf(ret.indieIgnoreMobpdpR), ret.duration));
               break;
            case 80001460:
               ret.statups.put(SecondaryStat.IndieHpR, new Pair(Integer.valueOf(ret.indieMhpR), ret.duration));
               break;
            case 80001461:
               ret.statups.put(SecondaryStat.IndieCD, new Pair(ret.x, ret.duration));
               break;
            case 80001757:
               ret.statups.put(SecondaryStat.IndieJump, new Pair(100, ret.duration));
               ret.statups.put(SecondaryStat.IndieSpeed, new Pair(100, ret.duration));
               ret.statups.put(SecondaryStat.IndieNotDamaged, new Pair(1, ret.duration));
               ret.statups.put(SecondaryStat.Inflation, new Pair(500, ret.duration));
               break;
            case 80002280:
               ret.duration = 180000;
               ret.statups.put(SecondaryStat.IndieExp, new Pair(100, ret.duration));
               break;
            case 80002281:
               ret.statups.put(SecondaryStat.RuneOfGreed, new Pair(100, ret.duration));
               break;
            case 80002282:
               ret.duration = 900000;
               ret.statups.put(SecondaryStat.CooldownRune, new Pair(1, ret.duration));
               break;
            case 80002404:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.DebuffIncHp, new Pair(50, ret.duration));
               break;
            case 80002544:
               ret.statups.put(SecondaryStat.IndiePmdR, new Pair(Integer.valueOf(ret.indiePmdR), ret.duration));
               break;
            case 80002888:
               ret.statups.put(SecondaryStat.RuneOfPure, new Pair(100, ret.duration));
               break;
            case 80002890:
               ret.statups.put(SecondaryStat.RuneOfTransition, new Pair(1, ret.duration));
               break;
            case 80003016:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.EventSpecialSkill, new Pair(1, ret.duration));
               break;
            case 91001022:
               ret.statups.put(SecondaryStat.IndieBDR, new Pair(Integer.valueOf(ret.indieBDR), ret.duration));
               break;
            case 91001023:
               ret.statups.put(SecondaryStat.IndieIgnoreMobPdpR, new Pair(Integer.valueOf(ret.indieIgnoreMobpdpR), ret.duration));
               break;
            case 91001024:
               ret.statups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(ret.indieDamR), ret.duration));
               break;
            case 91001025:
               ret.statups.put(SecondaryStat.IndieCD, new Pair(Integer.valueOf(ret.indieCD), ret.duration));
               break;
            case 131001000:
            case 131001001:
            case 131001002:
            case 131001003:
               ret.statups.put(SecondaryStat.PinkbeanAttackBuff, new Pair(ret.x, ret.duration));
               break;
            case 131001015:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.PinkbeanMinibeenMove, new Pair(1, ret.duration));
               break;
            case 131001021:
               ret.statups.put(SecondaryStat.KeyDownMoving, new Pair(ret.x, ret.duration));
               break;
            case 131001106:
               ret.statups.put(SecondaryStat.IndieExp, new Pair(50, ret.duration));
               break;
            case 131001206:
               ret.statups.put(SecondaryStat.IndieExp, new Pair(50, ret.duration));
               ret.statups.put(SecondaryStat.DotHealHPPerSecond, new Pair(Integer.valueOf(ret.dotHealHPPerSecondR), ret.duration));
               break;
            case 131001306:
               ret.statups.put(SecondaryStat.IndieExp, new Pair(50, ret.duration));
               ret.statups.put(SecondaryStat.IndiePadR, new Pair(Integer.valueOf(ret.indiePadR), ret.duration));
               break;
            case 131001406:
               ret.statups.put(SecondaryStat.IndieExp, new Pair(50, ret.duration));
               ret.statups.put(SecondaryStat.IndieAsrR, new Pair(Integer.valueOf(ret.indieAsrR), ret.duration));
               break;
            case 131001506:
               ret.statups.put(SecondaryStat.IndieExp, new Pair(50, ret.duration));
               ret.statups.put(SecondaryStat.IndiePadR, new Pair(Integer.valueOf(ret.indiePadR), ret.duration));
               ret.statups.put(SecondaryStat.IndieAsrR, new Pair(Integer.valueOf(ret.indieAsrR), ret.duration));
               break;
            case 142001003:
               ret.statups.put(SecondaryStat.IndieBooster, new Pair(Integer.valueOf(ret.indieBooster), ret.duration));
               break;
            case 142001007:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.KinesisPsychicEnergeShield, new Pair(1, ret.duration));
               break;
            case 142111010:
               ret.statups.put(SecondaryStat.NewFlying, new Pair(1, 900));
               break;
            case 142121032:
               ret.statups.put(SecondaryStat.KinesisPsychicOver, new Pair(50, ret.duration));
               break;
            case 151101006:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.Creation, new Pair(1, ret.duration));
               break;
            case 151101013:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.Wonder, new Pair(1, ret.duration));
               break;
            case 151111005:
               ret.statups.put(SecondaryStat.Novility, new Pair(Integer.valueOf(ret.level), ret.duration));
               break;
            case 151121001:
               ret.duration = 30000;
               ret.statups.put(SecondaryStat.Grave, new Pair(1, ret.duration));
               break;
            case 154100003:
            case 154101009:
            case 154110003:
            case 154111004:
            case 154120003:
            case 154121003:
            case 154121009:
            case 154121011:
               ret.duration = 2000;
               ret.statups.put(SecondaryStat.DarkSight, new Pair(10, ret.duration));
               break;
            case 154101005:
               ret.statups.put(SecondaryStat.IndieBooster, new Pair(-2, ret.duration));
               break;
            case 154121042:
               ret.statups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(ret.indieDamR), ret.duration));
               break;
            case 154121043:
               ret.statups.put(SecondaryStat.Oblivion, new Pair(ret.v, ret.duration));
               break;
            case 155101008:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.ComingDeath, new Pair(1, ret.duration));
               break;
            case 400001003:
               ret.statups.put(SecondaryStat.MaxHP, new Pair(ret.x, ret.duration));
               ret.statups.put(SecondaryStat.MaxMP, new Pair(ret.y, ret.duration));
               break;
            case 400001004:
               ret.statups.put(SecondaryStat.CombatOrders, new Pair(1, ret.duration));
               break;
            case 400001005:
               ret.statups.put(SecondaryStat.IndiePad, new Pair(ret.x, ret.duration));
               ret.statups.put(SecondaryStat.IndieMad, new Pair(ret.y, ret.duration));
               ret.statups.put(SecondaryStat.IndieHp, new Pair(Integer.valueOf(ret.indieMhp), ret.duration));
               ret.statups.put(SecondaryStat.IndieMp, new Pair(Integer.valueOf(ret.indieMmp), ret.duration));
               break;
            case 400001006:
               ret.statups.put(SecondaryStat.PartyBooster, new Pair(-1, ret.duration));
               break;
            case 400001010:
               ret.statups.put(SecondaryStat.HiddenPossession, new Pair(ret.x, ret.duration));
               break;
            case 400001020:
               ret.statups.put(SecondaryStat.HolySymbol, new Pair(ret.x, ret.duration));
               ret.statups.put(SecondaryStat.DropRate, new Pair(ret.v, ret.duration));
               break;
            case 400001023:
               ret.statups.put(SecondaryStat.DarkSight, new Pair(ret.x, ret.duration));
               break;
            case 400001025:
               ret.statups.put(SecondaryStat.IndieReduceCooltime, new Pair(Integer.valueOf(ret.indieCooltimeReduce), ret.duration));
               ret.statups.put(SecondaryStat.FreudsProtection, new Pair(sourceid - 400001024, ret.duration));
               break;
            case 400001026:
               ret.statups.put(SecondaryStat.IndieReduceCooltime, new Pair(Integer.valueOf(ret.indieCooltimeReduce), ret.duration));
               ret.statups.put(SecondaryStat.IndieStance, new Pair(Integer.valueOf(ret.indieStance), ret.duration));
               ret.statups.put(SecondaryStat.FreudsProtection, new Pair(sourceid - 400001024, ret.duration));
               break;
            case 400001027:
               ret.statups.put(SecondaryStat.IndieReduceCooltime, new Pair(Integer.valueOf(ret.indieCooltimeReduce), ret.duration));
               ret.statups.put(SecondaryStat.IndieStance, new Pair(Integer.valueOf(ret.indieStance), ret.duration));
               ret.statups.put(SecondaryStat.IndieAllStat, new Pair(Integer.valueOf(ret.indieAllStat), ret.duration));
               ret.statups.put(SecondaryStat.FreudsProtection, new Pair(sourceid - 400001024, ret.duration));
               break;
            case 400001028:
               ret.statups.put(SecondaryStat.IndieReduceCooltime, new Pair(Integer.valueOf(ret.indieCooltimeReduce), ret.duration));
               ret.statups.put(SecondaryStat.IndieStance, new Pair(Integer.valueOf(ret.indieStance), ret.duration));
               ret.statups.put(SecondaryStat.IndieAllStat, new Pair(Integer.valueOf(ret.indieAllStat), ret.duration));
               ret.statups.put(SecondaryStat.IndieMad, new Pair(Integer.valueOf(ret.indieMad), ret.duration));
               ret.statups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(ret.indiePad), ret.duration));
               ret.statups.put(SecondaryStat.FreudsProtection, new Pair(sourceid - 400001024, ret.duration));
               break;
            case 400001029:
               ret.statups.put(SecondaryStat.IndieReduceCooltime, new Pair(Integer.valueOf(ret.indieCooltimeReduce), ret.duration));
               ret.statups.put(SecondaryStat.IndieStance, new Pair(Integer.valueOf(ret.indieStance), ret.duration));
               ret.statups.put(SecondaryStat.IndieAllStat, new Pair(Integer.valueOf(ret.indieAllStat), ret.duration));
               ret.statups.put(SecondaryStat.IndieBDR, new Pair(Integer.valueOf(ret.indieBDR), ret.duration));
               ret.statups.put(SecondaryStat.IndieMad, new Pair(Integer.valueOf(ret.indieMad), ret.duration));
               ret.statups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(ret.indiePad), ret.duration));
               ret.statups.put(SecondaryStat.FreudsProtection, new Pair(sourceid - 400001024, ret.duration));
               break;
            case 400001030:
               ret.statups.put(SecondaryStat.IndieReduceCooltime, new Pair(Integer.valueOf(ret.indieCooltimeReduce), ret.duration));
               ret.statups.put(SecondaryStat.IndieStance, new Pair(Integer.valueOf(ret.indieStance), ret.duration));
               ret.statups.put(SecondaryStat.IndieAllStat, new Pair(Integer.valueOf(ret.indieAllStat), ret.duration));
               ret.statups.put(SecondaryStat.IndieBDR, new Pair(Integer.valueOf(ret.indieBDR), ret.duration));
               ret.statups.put(SecondaryStat.IndieMad, new Pair(Integer.valueOf(ret.indieMad), ret.duration));
               ret.statups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(ret.indiePad), ret.duration));
               ret.statups.put(SecondaryStat.IndieNotDamaged, new Pair(1, ret.duration));
               ret.statups.put(SecondaryStat.FreudsProtection, new Pair(sourceid - 400001024, ret.duration));
               break;
            case 400001037:
               ret.statups.put(SecondaryStat.MagicCircuitFullDrive, new Pair(ret.y, ret.duration));
               break;
            case 400001043:
               ret.statups.put(SecondaryStat.IndieDamR, new Pair(ret.q, ret.duration));
               ret.statups.put(SecondaryStat.Bless5th, new Pair(1, ret.duration));
               break;
            case 400001044:
               ret.statups.put(SecondaryStat.IndieDamR, new Pair(ret.q, ret.duration));
               ret.statups.put(SecondaryStat.IndieDamageReduce, new Pair(ret.z, ret.duration));
               ret.statups.put(SecondaryStat.Bless5th, new Pair(1, ret.duration));
               break;
            case 400007000:
            case 400007001:
            case 400007002:
            case 400007009:
            case 400007010:
               ret.statups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(ret.indieDamR), ret.duration));
               break;
            case 400007003:
               ret.statups.put(SecondaryStat.IndieCr, new Pair(Integer.valueOf(ret.indieCr), ret.duration));
               break;
            case 400007004:
               ret.statups.put(SecondaryStat.IndieCD, new Pair(Integer.valueOf(ret.indieCD), ret.duration));
               break;
            case 400007005:
               ret.statups.put(SecondaryStat.IndieIgnoreMobPdpR, new Pair(Integer.valueOf(ret.indieIgnoreMobpdpR), ret.duration));
               break;
            case 400007006:
               ret.statups.put(SecondaryStat.IndieNotDamaged, new Pair(1, ret.duration));
               break;
            case 400007007:
            case 400007011:
               ret.statups.put(SecondaryStat.IndieExp, new Pair(Integer.valueOf(ret.IndieExp), ret.duration));
               break;
            case 400007008:
               ret.statups.put(SecondaryStat.IndieBDR, new Pair(Integer.valueOf(ret.indieBDR), ret.duration));
               break;
            case 400011000:
               ret.statups.put(SecondaryStat.IndiePmdR, new Pair(Integer.valueOf(ret.indiePmdR), ret.duration));
               ret.statups.put(SecondaryStat.IndieIgnoreMobPdpR, new Pair(Integer.valueOf(ret.indieIgnoreMobpdpR), ret.duration));
               ret.statups.put(SecondaryStat.AuraWeapon, new Pair(ret.z, ret.duration));
               break;
            case 400011015:
               ret.statups.put(SecondaryStat.IndieReduceCooltime, new Pair(ret.q, ret.duration));
               ret.statups.put(SecondaryStat.IndieBooster, new Pair(-2, ret.duration));
               ret.statups.put(SecondaryStat.IndiePmdR, new Pair(Integer.valueOf(ret.indiePmdR), ret.duration));
               break;
            case 400011017:
               ret.statups.put(SecondaryStat.BonusAttack, new Pair(Integer.valueOf(ret.level), ret.duration));
               break;
            case 400011039:
               ret.duration = 5000;
               ret.statups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 5000));
               ret.statups.put(SecondaryStat.IndieJointAttack, new Pair(1, 5000));
               break;
            case 400011055:
               ret.statups.put(SecondaryStat.Ellision, new Pair(Integer.valueOf(ret.level), ret.duration));
               break;
            case 400011066:
               ret.statups.put(SecondaryStat.IndieSuperStance, new Pair(1, ret.duration));
               ret.statups.put(SecondaryStat.IndieAsrR, new Pair(Integer.valueOf(ret.asrR), ret.duration));
               ret.statups.put(SecondaryStat.BodyOfSteal, new Pair(1, ret.duration));
               break;
            case 400011072:
               ret.duration = 10000;
               ret.statups.put(SecondaryStat.GrandCrossSize, new Pair(1, ret.duration));
               ret.statups.put(SecondaryStat.Speed, new Pair(-60, ret.duration));
               break;
            case 400011073:
               ret.statups.put(SecondaryStat.ComboInstict, new Pair(Integer.valueOf(ret.level), ret.duration));
               break;
            case 400011109:
               ret.statups.put(SecondaryStat.IndieDamR, new Pair(ret.y, ret.duration));
               ret.statups.put(SecondaryStat.Restore, new Pair(1, ret.duration));
               break;
            case 400021000:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.OverloadMana, new Pair(ret.z, ret.duration));
               break;
            case 400021003:
               ret.statups.put(SecondaryStat.Pray, new Pair(Integer.valueOf(ret.level), ret.duration));
               break;
            case 400021060:
               ret.statups.put(SecondaryStat.Etherealform, new Pair(1, ret.duration));
               ret.statups.put(SecondaryStat.IndieShotDamage, new Pair(1, ret.duration));
               break;
            case 400031000:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.GuidedArrow, new Pair(1, ret.duration));
               break;
            case 400031002:
               ret.statups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(ret.indieDamR), ret.duration));
               break;
            case 400031015:
               ret.statups.put(SecondaryStat.SplitArrow, new Pair(Integer.valueOf(ret.level), ret.duration));
               break;
            case 400031017:
               ret.statups.put(SecondaryStat.IndieStance, new Pair(Integer.valueOf(ret.indieStance), ret.duration));
               ret.statups.put(SecondaryStat.IndiePadR, new Pair(Integer.valueOf(ret.indiePadR), ret.duration));
               ret.statups.put(SecondaryStat.IndieDamageReduce, new Pair(Integer.valueOf(ret.indieDamReduceR), ret.duration));
               break;
            case 400031020:
               ret.statups.put(SecondaryStat.BonusAttack, new Pair(Integer.valueOf(ret.level), ret.duration));
               break;
            case 400031021:
               ret.statups.put(SecondaryStat.BonusAttack, new Pair(2, ret.duration));
               break;
            case 400031023:
               ret.statups.put(SecondaryStat.CriticalReinForce, new Pair(ret.x, ret.duration));
               break;
            case 400041001:
               ret.statups.put(SecondaryStat.SpreadThrow, new Pair(Integer.valueOf(ret.level), ret.duration));
               break;
            case 400041029:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.Overload, new Pair(ret.x, ret.duration));
               break;
            case 400041035:
               ret.statups.put(SecondaryStat.ChainArtsFury, new Pair(Integer.valueOf(ret.level), ret.duration));
               break;
            case 400041052:
               ret.statups.put(SecondaryStat.SageWrathOfGods, new Pair(1, ret.duration));
               ret.statups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(ret.indieDamR), ret.duration));
               break;
            case 400041084:
               ret.duration = 5000;
               ret.statups.put(SecondaryStat.IndieNotDamaged, new Pair(1, ret.duration));
               ret.statups.put(SecondaryStat.VoidBurst, new Pair(1, ret.duration));
               break;
            case 400051006:
               ret.statups.put(SecondaryStat.BulletParty, new Pair(Integer.valueOf(ret.level), ret.duration));
               break;
            case 400051007:
               ret.statups.put(SecondaryStat.IndiePmdR, new Pair(Integer.valueOf(ret.indiePmdR), ret.duration));
               ret.statups.put(SecondaryStat.Striker1st, new Pair(Integer.valueOf(ret.level), ret.duration));
               break;
            case 400051009:
               ret.statups.put(SecondaryStat.MultipleOption, new Pair(ret.q2, ret.duration));
               break;
            case 400051015:
               ret.duration = 0;
               ret.statups.put(SecondaryStat.SerpentScrew, new Pair(ret.damage / 10, ret.duration));
               break;
            case 400051018:
               ret.statups.put(SecondaryStat.Spotlight, new Pair(Integer.valueOf(ret.level), ret.duration));
               break;
            case 400051033:
               ret.statups.put(SecondaryStat.OverDrive, new Pair(ret.x, ret.duration));
               break;
            case 400051036:
               ret.statups.put(SecondaryStat.InfinitySpell, new Pair(ret.x, ret.duration));
            }

            if (GameConstants.isBeginnerJob(sourceid / 10000)) {
               switch(sourceid % 10000) {
               case 1001:
                  if (sourceid / 10000 != 3001 && sourceid / 10000 != 3000) {
                     ret.statups.put(SecondaryStat.Recovery, new Pair(ret.x, ret.duration));
                     break;
                  }

                  ret.statups.put(SecondaryStat.Infiltrate, new Pair(ret.x, ret.duration));
                  break;
               case 1002:
                  ret.statups.put(SecondaryStat.Speed, new Pair(ret.x, ret.duration));
                  break;
               case 8000:
                  ret.statups.put(SecondaryStat.Speed, new Pair(Integer.valueOf(ret.speed), ret.duration));
                  ret.statups.put(SecondaryStat.Jump, new Pair(Integer.valueOf(ret.jump), ret.duration));
                  break;
               case 8002:
                  ret.statups.put(SecondaryStat.SharpEyes, new Pair(2568, ret.duration));
                  break;
               case 8003:
                  ret.statups.put(SecondaryStat.MaxHP, new Pair(40, ret.duration));
                  ret.statups.put(SecondaryStat.MaxMP, new Pair(40, ret.duration));
                  break;
               case 8004:
                  ret.statups.put(SecondaryStat.CombatOrders, new Pair(1, ret.duration));
                  break;
               case 8005:
                  ret.statups.put(SecondaryStat.IndiePad, new Pair(20, ret.duration));
                  ret.statups.put(SecondaryStat.IndieMad, new Pair(20, ret.duration));
                  ret.statups.put(SecondaryStat.IndieHp, new Pair(475, ret.duration));
                  ret.statups.put(SecondaryStat.IndieMp, new Pair(475, ret.duration));
                  break;
               case 8006:
                  ret.statups.put(SecondaryStat.PartyBooster, new Pair(-1, ret.duration));
               }
            } else if (sourceid < 400000000) {
               switch(sourceid % 10000) {
               case 1085:
               case 1087:
               case 1090:
               case 1179:
                  ret.duration = 0;
               }
            }
         } else {
            switch(sourceid) {
            case 2002093:
               ret.hpR = 100.0D;
               ret.mpR = 100.0D;
               ret.statups.put(SecondaryStat.IndiePad, new Pair(30, ret.duration));
               break;
            case 2003516:
            case 2003517:
            case 2003518:
            case 2003519:
            case 2003520:
            case 2003552:
            case 2003553:
            case 2003561:
            case 2003566:
            case 2003568:
            case 2003570:
            case 2003571:
            case 2003572:
            case 2003576:
            case 2003591:
               ret.statups.put(SecondaryStat.Inflation, new Pair(Integer.valueOf(ret.inflation), ret.duration));
               break;
            case 2003550:
               ret.statups.put(SecondaryStat.IndieExp, new Pair(10, ret.duration));
               break;
            case 2003551:
               ret.statups.put(SecondaryStat.DropItemRate, new Pair(20, ret.duration));
               break;
            case 2003575:
               ret.statups.put(SecondaryStat.DropItemRate, new Pair(20, ret.duration));
               break;
            case 2003596:
               ret.statups.put(SecondaryStat.IndieBDR, new Pair(20, ret.duration));
               break;
            case 2003597:
               ret.statups.put(SecondaryStat.IndieDamR, new Pair(10, ret.duration));
               break;
            case 2003598:
               ret.statups.put(SecondaryStat.IndieIgnoreMobPdpR, new Pair(20, ret.duration));
               break;
            case 2003599:
               ret.statups.put(SecondaryStat.IndieAllStatR, new Pair(10, ret.duration));
               break;
            case 2004019:
               ret.statups.put(SecondaryStat.IndieStr, new Pair(30, ret.duration));
               break;
            case 2004039:
               ret.statups.put(SecondaryStat.IndieDex, new Pair(30, ret.duration));
               break;
            case 2004059:
               ret.statups.put(SecondaryStat.IndieInt, new Pair(30, ret.duration));
               break;
            case 2004079:
               ret.statups.put(SecondaryStat.IndieLuk, new Pair(30, ret.duration));
               break;
            case 2022125:
               ret.statups.put(SecondaryStat.Pdd, new Pair(1, ret.duration));
               break;
            case 2022126:
               ret.statups.put(SecondaryStat.Pdd, new Pair(1, ret.duration));
               break;
            case 2022127:
               ret.statups.put(SecondaryStat.Acc, new Pair(1, ret.duration));
               break;
            case 2022128:
               ret.statups.put(SecondaryStat.Eva, new Pair(1, ret.duration));
               break;
            case 2022129:
               ret.statups.put(SecondaryStat.IndiePadR, new Pair(1, ret.duration));
               break;
            case 2022746:
            case 2022764:
               ret.statups.clear();
               ret.duration = 0;
               ret.statups.put(SecondaryStat.IndiePadR, new Pair(5, ret.duration));
               ret.statups.put(SecondaryStat.IndieMadR, new Pair(5, ret.duration));
               ret.statups.put(SecondaryStat.RepeatEffect, new Pair(1, ret.duration));
               break;
            case 2022747:
               ret.statups.clear();
               ret.duration = 0;
               ret.statups.put(SecondaryStat.IndiePadR, new Pair(10, ret.duration));
               ret.statups.put(SecondaryStat.IndieMadR, new Pair(10, ret.duration));
               ret.statups.put(SecondaryStat.RepeatEffect, new Pair(1, ret.duration));
               break;
            case 2022823:
               ret.statups.clear();
               ret.duration = 0;
               ret.statups.put(SecondaryStat.IndiePadR, new Pair(12, ret.duration));
               ret.statups.put(SecondaryStat.IndieMadR, new Pair(12, ret.duration));
               ret.statups.put(SecondaryStat.RepeatEffect, new Pair(1, ret.duration));
               break;
            case 2023072:
               ret.statups.put(SecondaryStat.ItemUpByItem, new Pair(100, ret.duration));
               break;
            case 2023300:
               ret.statups.put(SecondaryStat.IndiePad, new Pair(20, 1800000));
               ret.statups.put(SecondaryStat.IndieMad, new Pair(20, 1800000));
               break;
            case 2023520:
               ret.statups.put(SecondaryStat.IndiePad, new Pair(30, ret.duration));
               ret.statups.put(SecondaryStat.IndieMad, new Pair(30, ret.duration));
               break;
            case 2023553:
               ret.statups.put(SecondaryStat.IndiePad, new Pair(30, ret.duration));
               break;
            case 2023554:
               ret.statups.put(SecondaryStat.IndieBooster, new Pair(-1, ret.duration));
               break;
            case 2023555:
               ret.statups.put(SecondaryStat.IndieMad, new Pair(30, ret.duration));
               break;
            case 2023556:
               ret.statups.put(SecondaryStat.IndieHp, new Pair(2000, ret.duration));
               ret.statups.put(SecondaryStat.IndieMp, new Pair(2000, ret.duration));
               ret.statups.put(SecondaryStat.IndieExp, new Pair(10, ret.duration));
               break;
            case 2023558:
               ret.statups.put(SecondaryStat.IndieExp, new Pair(50, ret.duration));
               break;
            case 2023658:
            case 2023659:
            case 2023660:
               ret.statups.put(SecondaryStat.IndiePad, new Pair(30, ret.duration));
               ret.statups.put(SecondaryStat.IndieMad, new Pair(30, ret.duration));
               break;
            case 2023661:
            case 2023662:
            case 2023663:
               ret.statups.put(SecondaryStat.ItemUpByItem, new Pair(50, ret.duration));
               break;
            case 2023664:
            case 2023665:
            case 2023666:
               ret.statups.put(SecondaryStat.WealthOfUnion, new Pair(50, ret.duration));
               break;
            case 2023912:
               ret.statups.put(SecondaryStat.IndiePad, new Pair(30, 1800000));
               ret.statups.put(SecondaryStat.IndieMad, new Pair(30, 1800000));
               break;
            case 2024011:
            case 2024017:
               ret.statups.put(SecondaryStat.IndieExp, new Pair(15, 1800000));
               break;
            case 2024012:
            case 2024018:
               ret.statups.put(SecondaryStat.IndieAllStat, new Pair(15, 1800000));
               ret.statups.put(SecondaryStat.IndieHp, new Pair(1500, 1800000));
               ret.statups.put(SecondaryStat.IndieMp, new Pair(1500, 1800000));
               ret.statups.put(SecondaryStat.IndiePad, new Pair(15, 1800000));
               ret.statups.put(SecondaryStat.IndieMad, new Pair(15, 1800000));
               ret.statups.put(SecondaryStat.IndieIgnoreMobPdpR, new Pair(15, 1800000));
               ret.statups.put(SecondaryStat.IndieBDR, new Pair(15, 1800000));
               break;
            case 2450054:
               ret.statups.put(SecondaryStat.IndieExp, new Pair(300, ret.duration));
               break;
            case 2450064:
               ret.statups.put(SecondaryStat.ExpBuffRate, new Pair(100, ret.duration));
               break;
            case 2450124:
               ret.statups.put(SecondaryStat.IndieExp, new Pair(150, ret.duration));
               break;
            case 2450134:
               ret.statups.put(SecondaryStat.ExpBuffRate, new Pair(200, ret.duration));
               break;
            case 2450147:
            case 2450148:
            case 2450149:
               ret.statups.put(SecondaryStat.ExpBuffRate, new Pair(100, ret.duration));
               break;
            case 2450163:
               ret.statups.put(SecondaryStat.ExpBuffRate, new Pair(300, ret.duration));
            }
         }

         if (ret.isMorph()) {
            ret.statups.put(SecondaryStat.Morph, new Pair(ret.morphId, ret.duration));
         }
      } catch (Exception var20) {
         var20.printStackTrace();
      }

      return ret;
   }

   public final boolean applyTo(MapleCharacter chr) {
      return this.applyTo(chr, chr, true, chr.getTruePosition(), this.duration, (byte)(chr.isFacingLeft() ? 1 : 0), false);
   }

   public final boolean applyToBuff(MapleCharacter chr) {
      return this.applyTo(chr, chr, true, chr.getTruePosition(), this.duration, (byte)(chr.isFacingLeft() ? 1 : 0), true);
   }

   public final boolean applyToBuff(MapleCharacter chr, int duration) {
      return this.applyTo(chr, chr, true, chr.getTruePosition(), duration, (byte)(chr.isFacingLeft() ? 1 : 0), false);
   }

   public final boolean applyTo(MapleCharacter applyfrom, MapleCharacter applyto) {
      return this.applyTo(applyfrom, applyto, true, applyto.getTruePosition(), this.duration, (byte)(applyfrom.isFacingLeft() ? 1 : 0), false);
   }

   public final boolean applyTo(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary) {
      return this.applyTo(applyfrom, applyto, primary, applyto.getTruePosition(), this.duration, (byte)(applyfrom.isFacingLeft() ? 1 : 0), false);
   }

   public final boolean applyTo(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary, int duration) {
      return this.applyTo(applyfrom, applyto, primary, applyto.getTruePosition(), duration, (byte)(applyfrom.isFacingLeft() ? 1 : 0), false);
   }

   public final boolean applyTo(MapleCharacter chr, boolean primary) {
      return this.applyTo(chr, chr, primary, chr.getTruePosition(), this.duration, (byte)(chr.isFacingLeft() ? 1 : 0), false);
   }

   public final boolean applyTo(MapleCharacter chr, boolean primary, boolean showEffect) {
      return this.applyTo(chr, chr, primary, chr.getTruePosition(), this.duration, (byte)(chr.isFacingLeft() ? 1 : 0), showEffect);
   }

   public final boolean applyTo(MapleCharacter chr, int duration) {
      return this.applyTo(chr, chr, true, chr.getTruePosition(), duration, (byte)(chr.isFacingLeft() ? 1 : 0), false);
   }

   public final boolean applyTo(MapleCharacter chr, boolean primary, int duration) {
      return this.applyTo(chr, chr, primary, chr.getTruePosition(), duration, (byte)(chr.isFacingLeft() ? 1 : 0), false);
   }

   public final boolean applyTo(MapleCharacter chr, boolean primary, int duration, boolean showEffect) {
      return this.applyTo(chr, chr, primary, chr.getTruePosition(), duration, (byte)(chr.isFacingLeft() ? 1 : 0), showEffect);
   }

   public final boolean applyTo(MapleCharacter chr, Point pos, boolean primary, int duration) {
      return this.applyTo(chr, chr, primary, pos, duration, (byte)(chr.isFacingLeft() ? 1 : 0), false);
   }

   public final boolean applyTo(MapleCharacter chr, Point pos, boolean primary) {
      return this.applyTo(chr, chr, primary, pos, this.duration, (byte)(chr.isFacingLeft() ? 1 : 0), false);
   }

   public final boolean applyTo(MapleCharacter chr, Point pos, boolean primary, boolean showEffect) {
      return this.applyTo(chr, chr, primary, pos, this.duration, (byte)(chr.isFacingLeft() ? 1 : 0), showEffect);
   }

   public final boolean applyTo(MapleCharacter chr, Point pos) {
      return this.applyTo(chr, chr, true, pos, this.duration, (byte)(chr.isFacingLeft() ? 1 : 0), false);
   }

   public final boolean applyTo(MapleCharacter chr, boolean primary, Point pos) {
      return this.applyTo(chr, chr, primary, pos, this.duration, (byte)(chr.isFacingLeft() ? 1 : 0), false);
   }

   public final boolean applyTo(MapleCharacter chr, Point pos, int duration) {
      return this.applyTo(chr, chr, true, pos, duration, (byte)(chr.isFacingLeft() ? 1 : 0), false);
   }

   public final boolean applyTo(MapleCharacter chr, Point pos, byte rltype) {
      return this.applyTo(chr, chr, true, pos, this.duration, rltype, false);
   }

   public final boolean applyTo(MapleCharacter chr, Point pos, byte rltype, boolean showEffect) {
      return this.applyTo(chr, chr, true, pos, this.duration, rltype, showEffect);
   }

   public final boolean applyTo(MapleCharacter chr, Point pos, boolean primary, byte rltype) {
      return this.applyTo(chr, chr, primary, pos, this.duration, rltype, false);
   }

   public final boolean applyTo(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary, Point pos, int localDuration, byte rltype, boolean showEffect) {
      if (applyfrom.getMapId() == ServerConstants.warpMap && this.skill && !applyfrom.isGM() && (this.getSummonMovementType() != null || this.isMist())) {
         applyfrom.getClient().getSession().writeAndFlush(CWvsContext.enableActions(applyto));
         return false;
      } else if (this.isHeal() && (applyfrom.getMapId() == 749040100 || applyto.getMapId() == 749040100)) {
         applyfrom.getClient().getSession().writeAndFlush(CWvsContext.enableActions(applyto));
         return false;
      } else if (this.sourceid == 4341006 && applyfrom.getBuffedValue(SecondaryStat.ShadowPartner) == null) {
         applyfrom.getClient().getSession().writeAndFlush(CWvsContext.enableActions(applyto));
         return false;
      } else if (this.sourceid != 33101008 || applyfrom.getBuffedValue(SecondaryStat.IndieSummon) == null && applyfrom.canSummon()) {
         if (this.sourceid == 33101004 && applyfrom.getMap().isTown()) {
            applyfrom.dropMessage(5, "You may not use this skill in towns.");
            applyfrom.getClient().getSession().writeAndFlush(CWvsContext.enableActions(applyto));
            return false;
         } else {
            EnumMap<MapleStat, Long> hpmpupdate = new EnumMap(MapleStat.class);
            long hpchange = (long)this.calcHPChange(applyfrom, primary);
            long mpchange = (long)this.calcMPChange(applyfrom, this.sourceid == 36101001 ? true : primary);
            int powerchange = this.calcPowerChange(applyfrom, primary);
            PlayerStats stat = applyto.getStat();
            boolean noCon = false;
            if (this.sourceid != 400011010 && this.sourceid != 400021006) {
               if (this.sourceid == 400011055) {
                  noCon = true;
               } else if (this.sourceid == 36101001) {
                  primary = true;
               }
            } else if (applyto.getBuffedValue(this.sourceid) && !primary) {
               noCon = true;
            }

            if (applyto.getSkillLevel(4110012) > 0 && localDuration < 0 && applyto.getSkillCustomValue0(4110012) > 0L) {
               applyto.removeSkillCustomInfo(4110012);
               mpchange = 1L;
            }

            if (primary && this.itemConNo != 0 && !applyto.inPVP()) {
               if (!applyto.haveItem(this.itemCon, this.itemConNo, false, true)) {
                  applyto.getClient().getSession().writeAndFlush(CWvsContext.enableActions(applyto));
                  return false;
               }

               MapleInventoryManipulator.removeById(applyto.getClient(), GameConstants.getInventoryType(this.itemCon), this.itemCon, this.itemConNo, false, true);
            }

            if (this.isResurrection() && applyto.getId() != applyfrom.getId() && primary) {
               hpchange = stat.getCurrentMaxHp();
               applyto.setStance(0);
               stat.setHp(stat.getHp() + hpchange, applyto);
               hpmpupdate.put(MapleStat.HP, stat.getHp());
               SkillFactory.getSkill(2321006).getEffect(applyfrom.getSkillLevel(2321006)).applyTo(applyfrom, applyto, false);
               SkillFactory.getSkill(2321006).getEffect(applyfrom.getSkillLevel(2321006)).applyTo(applyfrom, false);
            }

            if ((!this.isDispel() || !this.makeChanceResult()) && this.sourceid != 2001556) {
               if (this.isHeroWill() || this.sourceid == 80001478 && this.makeChanceResult()) {
                  applyto.dispelDebuffs();
                  if (this.isHeroWill()) {
                     applyto.cancelAllDebuffs();
                  }
               } else if (this.isMPRecovery()) {
                  long toDecreaseHP = stat.getMaxHp() / 100L * 10L;
                  if (stat.getHp() > toDecreaseHP) {
                     hpchange += -toDecreaseHP;
                     mpchange += toDecreaseHP / 100L * (long)this.getY();
                  } else {
                     hpchange = stat.getHp() == 1L ? 0L : stat.getHp() - 1L;
                  }
               }
            } else {
               applyto.dispelDebuffs(applyfrom, this.sourceid);
            }

            if (applyfrom.getId() != applyto.getId()) {
               mpchange = 0L;
               if (!this.isHeal() && !this.isResurrection()) {
                  hpchange = 0L;
               }
            }

            if (GameConstants.isZero(applyto.getJob()) && this.sourceid == 1221054) {
               noCon = true;
            }

            if (hpchange != 0L && applyto.isAlive() && !noCon) {
               if (hpchange < 0L && -hpchange > stat.getHp() && !applyto.hasDisease(SecondaryStat.Undead)) {
                  applyto.getClient().getSession().writeAndFlush(CWvsContext.enableActions(applyto));
                  return false;
               }

               if (this.skill) {
                  if (stat.getHp() + hpchange > 0L) {
                     stat.setHp(stat.getHp() + hpchange, applyto);
                  }
               } else if (stat.getHp() + hpchange <= 0L) {
                  stat.setHp(1L, applyto);
               } else {
                  stat.setHp(stat.getHp() + hpchange, applyto);
               }

               hpmpupdate.put(MapleStat.HP, stat.getHp());
            }

            if (mpchange != 0L && applyto.isAlive() && !noCon && applyto.getBattleGroundChr() == null) {
               if (mpchange < 0L && -mpchange > stat.getMp()) {
                  applyto.getClient().getSession().writeAndFlush(CWvsContext.enableActions(applyto));
                  return false;
               }

               if (mpchange < 0L && GameConstants.isDemonSlayer(applyto.getJob()) || !GameConstants.isDemonSlayer(applyto.getJob())) {
                  stat.setMp(stat.getMp() + mpchange, applyto);
               }

               hpmpupdate.put(MapleStat.MP, stat.getMp());
            }

            if (applyto.getMapId() == 993192600 && this.skill) {
               return false;
            } else {
               if (primary || applyfrom.getId() != applyto.getId()) {
                  applyto.getClient().getSession().writeAndFlush(CWvsContext.updatePlayerStats(hpmpupdate, !this.skill, false, applyto, false));
               }

               if (MapleSkillManager.isUnstableMemorizeSkills(this.sourceid)) {
                  applyto.getClient().getSession().writeAndFlush(CField.skillCooldown(400001021, localDuration));
               } else if (this.sourceid == 2221012) {
                  applyto.getClient().getSession().writeAndFlush(CField.skillCooldown(400001021, 5000));
               }

               if (!applyto.isAlive() && this.sourceid != 1320019) {
                  return false;
               } else {
                  if (powerchange != 0) {
                     if (applyto.getXenonSurplus() - powerchange < 0) {
                        return false;
                     }

                     applyto.gainXenonSurplus((short)(-powerchange), SkillFactory.getSkill(this.getSourceId()));
                  }

                  if (this.expinc != 0) {
                     applyto.gainExp((long)this.expinc, true, true, false);
                     applyto.getClient().getSession().writeAndFlush(CField.EffectPacket.showNormalEffect(applyto, 24, true));
                  } else if (this.isReturnScroll()) {
                     this.applyReturnScroll(applyto);
                  } else if (this.useLevel > 0 && !this.skill) {
                     applyto.setExtractor(new MapleExtractor(applyto, this.sourceid, this.useLevel * 50, 1440));
                     applyto.getMap().spawnExtractor(applyto.getExtractor());
                  } else if (this.cosmetic > 0) {
                     if (this.cosmetic >= 30000) {
                        applyto.setHair(this.cosmetic);
                        applyto.updateSingleStat(MapleStat.HAIR, (long)this.cosmetic);
                     } else if (this.cosmetic >= 20000) {
                        applyto.setFace(this.cosmetic);
                        applyto.updateSingleStat(MapleStat.FACE, (long)this.cosmetic);
                     } else if (this.cosmetic < 100) {
                        applyto.setSkinColor((byte)this.cosmetic);
                        applyto.updateSingleStat(MapleStat.SKIN, (long)this.cosmetic);
                     }

                     applyto.equipChanged();
                  } else if (this.recipe > 0) {
                     if (applyto.getSkillLevel(this.recipe) > 0 || applyto.getProfessionLevel(this.recipe / 10000 * 10000) < this.reqSkillLevel) {
                        return false;
                     }

                     applyto.changeSingleSkillLevel(SkillFactory.getCraft(this.recipe), Integer.MAX_VALUE, this.recipeUseCount, this.recipeValidDay > 0 ? System.currentTimeMillis() + (long)this.recipeValidDay * 24L * 60L * 60L * 1000L : -1L);
                  }

                  Iterator var38 = this.traits.entrySet().iterator();

                  while(var38.hasNext()) {
                     Entry<MapleTrait.MapleTraitType, Integer> t = (Entry)var38.next();
                     applyto.getTrait((MapleTrait.MapleTraitType)t.getKey()).addExp((Integer)t.getValue(), applyto);
                  }

                  if (this.sourceid == 2121003 && !applyto.getPosionNovas().isEmpty()) {
                     applyto.getClient().getSession().writeAndFlush(CField.poisonNova(applyto, applyto.getPosionNovas()));
                     applyto.setPosionNovas(new ArrayList());
                  } else if ((this.sourceid == 12120013 || this.sourceid == 12120014) && primary) {
                     if (applyfrom.getBuffedValue(12120013)) {
                        applyfrom.cancelEffect(applyfrom.getBuffedEffect(12120013));
                     }

                     if (applyfrom.getBuffedValue(12120014)) {
                        applyfrom.cancelEffect(applyfrom.getBuffedEffect(12120014));
                     }
                  }

                  if ((this.sourceid == 12120013 || this.sourceid == 12120014) && primary) {
                     if (applyfrom.getBuffedValue(12120013)) {
                        applyto.cancelEffectFromBuffStat(SecondaryStat.IndieSummon, 12120013);
                     }

                     if (applyfrom.getBuffedValue(12120014)) {
                        applyto.cancelEffectFromBuffStat(SecondaryStat.IndieSummon, 12120014);
                     }
                  }

                  SummonMovementType summonMovementType;
                  Iterator var19;
                  boolean aftercancel;
                  if ((summonMovementType = this.getSummonMovementType()) == null || (this.sourceid != 400021071 && this.sourceid != 35111008 && this.sourceid != 35120002 && this.sourceid != 400021092 || !primary) && (this.sourceid == 400021071 || this.sourceid == 35111008 || this.sourceid == 35120002 || this.sourceid == 400021092)) {
                     if (this.isMechDoor()) {
                        int newId = 0;
                        aftercancel = false;
                        MechDoor door;
                        if (applyto.getMechDoors().size() >= 2) {
                           door = (MechDoor)applyto.getMechDoors().remove(0);
                           newId = door.getId();
                           applyto.getMap().broadcastMessage(CField.removeMechDoor(door, true));
                           applyto.getMap().removeMapObject(door);
                        } else {
                           var19 = applyto.getMechDoors().iterator();

                           while(var19.hasNext()) {
                              MechDoor d = (MechDoor)var19.next();
                              if (d.getId() == newId) {
                                 aftercancel = true;
                                 newId = 1;
                                 break;
                              }
                           }
                        }

                        door = new MechDoor(applyto, new Point(pos == null ? applyto.getTruePosition() : pos), newId, localDuration);
                        applyto.getMap().spawnMechDoor(door);
                        applyto.addMechDoor(door);
                        if (!aftercancel) {
                           return true;
                        }
                     }
                  } else {
                     boolean cancels;
                     if (this.sourceid == 400011001) {
                        MapleSummon summon = applyto.getSummon(400011001);
                        if (summon != null) {
                           summon.setSkill(400011002);
                           summon.setMovementType(SummonMovementType.STATIONARY);
                           applyto.getMap().broadcastMessage(CField.SummonPacket.removeSummon(summon, true));
                           applyto.getMap().broadcastMessage(CField.SummonPacket.spawnSummon(summon, true, summon.getDuration()));
                           applyto.getClient().getSession().writeAndFlush(CWvsContext.enableActions(applyto, true, false));
                           return true;
                        }

                        summon = applyto.getSummon(400011002);
                        if (summon != null) {
                           summon.setSkill(400011001);
                           summon.setMovementType(SummonMovementType.FOLLOW);
                           applyto.getMap().broadcastMessage(CField.SummonPacket.removeSummon(summon, true));
                           applyto.getMap().broadcastMessage(CField.SummonPacket.spawnSummon(summon, true, summon.getDuration()));
                           applyto.getClient().getSession().writeAndFlush(CWvsContext.enableActions(applyto, true, false));
                           return true;
                        }
                     } else if (this.sourceid != 35111002 && this.sourceid != 400021047 && this.sourceid != 400011065 && this.sourceid != 14000027 && this.sourceid != 14100027 && this.sourceid != 14110029 && this.sourceid != 14120008 && this.sourceid != 151100002 && primary) {
                        if (applyto.getBuffedValue(this.sourceid)) {
                           cancels = true;
                           if (applyto.getBuffedEffect(SecondaryStat.SoulMP) != null && applyto.getBuffedEffect(SecondaryStat.SoulMP).getSourceId() == this.sourceid) {
                              cancels = false;
                           }

                           if (cancels) {
                              applyto.cancelEffect(this);
                           }
                        }

                        ArrayList<MapleSummon> toRemove = new ArrayList();
                        int delcount = 1;
                        int counting = 0;
                        switch(this.sourceid) {
                        case 14121003:
                           delcount = 2;
                           break;
                        case 162101012:
                        case 400021071:
                           delcount = 4;
                        }

                        MapleSummon summon;
                        Iterator var46;
                        if (!applyto.getSummons().isEmpty()) {
                           var46 = applyto.getSummons().iterator();

                           while(var46.hasNext()) {
                              summon = (MapleSummon)var46.next();
                              if (summon.getSkill() == this.sourceid) {
                                 ++counting;
                                 if (delcount <= counting) {
                                    toRemove.add(summon);
                                 }
                              }
                           }
                        }

                        var46 = toRemove.iterator();

                        while(var46.hasNext()) {
                           summon = (MapleSummon)var46.next();
                           summon.removeSummon(applyto.getMap(), false);
                        }

                        if (this.sourceid == 35111008) {
                           applyto.cancelEffectFromBuffStat(SecondaryStat.IndieSummon, 35111008);
                           applyto.cancelEffectFromBuffStat(SecondaryStat.IndieSummon, 35120002);
                        }
                     }

                     cancels = true;
                     aftercancel = false;
                     if (this.sourceid == 400051011) {
                        cancels = showEffect;
                     } else if (this.sourceid == 400031051) {
                        cancels = !primary;
                     } else if (this.sourceid != 400021071 && this.sourceid != 36121014) {
                        if (this.sourceid == 400011055) {
                           if (applyfrom.getBuffedValue(this.sourceid)) {
                              cancels = false;
                           }
                        } else if (GameConstants.isAfterRemoveSummonSkill(this.sourceid)) {
                           aftercancel = true;
                           if (applyfrom.getBuffedValue(this.sourceid)) {
                              cancels = false;
                           }
                        }
                     } else {
                        cancels = primary;
                     }

                     if (cancels) {
                        int summId = this.sourceid;
                        applyto.dropMessageGM(-8, "summon sourceId : " + summId);
                        Skill elite;
                        if (this.sourceid == 3111002) {
                           if (applyfrom.getTotalSkillLevel(elite = SkillFactory.getSkill(3120012)) > 0) {
                              return elite.getEffect(applyfrom.getTotalSkillLevel(elite)).applyTo(applyfrom, applyto, primary, pos, localDuration, rltype, showEffect);
                           }
                        } else if (this.sourceid == 3211002 && applyfrom.getTotalSkillLevel(elite = SkillFactory.getSkill(3220012)) > 0) {
                           return elite.getEffect(applyfrom.getTotalSkillLevel(elite)).applyTo(applyfrom, applyto, primary, pos, localDuration, rltype, showEffect);
                        }

                        if (this.sourceid != 400011012 && this.sourceid != 400011013 && this.sourceid != 400011014) {
                           if ((this.sourceid == 36121002 || this.sourceid == 36121013 || this.sourceid == 36121014 || this.sourceid == 400041044 || this.sourceid == 400041047) && applyto.getSkillLevel(36120051) > 0) {
                              localDuration += SkillFactory.getSkill(36120051).getEffect(applyto.getSkillLevel(36120051)).getDuration();
                           }
                        } else {
                           pos = applyfrom.getTruePosition();
                        }

                        if (summId == 400001039) {
                           summId = 400001040;
                           localDuration = SkillFactory.getSkill(summId).getEffect(this.level).getDuration();
                        } else if (summId == 400001059) {
                           summId = 400001060;
                           localDuration = SkillFactory.getSkill(summId).getEffect(this.level).getDuration();
                        } else if (summId == 152101000 || summId == 1301013) {
                           localDuration = Integer.MAX_VALUE;
                        }

                        if (this.sourceid == 5321004) {
                           pos = new Point(pos.x + (applyto.isFacingLeft() ? -45 : 45), pos.y);
                        }

                        MapleSummon tosummon = new MapleSummon(applyfrom, summId, new Point(pos == null ? applyfrom.getTruePosition() : pos), summonMovementType, rltype, aftercancel ? Integer.MAX_VALUE : localDuration);
                        switch(this.sourceid) {
                        case 5201012:
                        case 5201013:
                        case 5201014:
                        case 5210015:
                        case 5210016:
                        case 5210017:
                        case 5210018:
                           tosummon.setDebuffshell(1);
                           break;
                        case 151111001:
                           if (applyto.getSkillLevel(151120035) > 0) {
                              localDuration += SkillFactory.getSkill(151120035).getEffect(1).duration;
                           }
                           break;
                        case 400051046:
                           applyfrom.setSkillCustomInfo(this.sourceid, 0L, 3000L);
                        }

                        if (localDuration != 0 && localDuration < 2100000000 && this.sourceid < 400000000 && this.sourceid != 35111002) {
                           localDuration = this.alchemistModifyVal(applyfrom, localDuration, false);
                        }

                        if (applyfrom.isGM()) {
                           applyfrom.dropMessageGM(-8, "spawn summon : " + summId + " / duration : " + (aftercancel ? Integer.MAX_VALUE : localDuration) + " / pos : " + tosummon.getTruePosition());
                        }

                        applyfrom.getMap().spawnSummon(tosummon, aftercancel ? Integer.MAX_VALUE : localDuration);
                        applyfrom.addSummon(tosummon);
                        tosummon.addHP(this.x);
                        if (this.sourceid == 131003023 || this.sourceid == 131004023 || this.sourceid == 131005023 || this.sourceid == 131006023) {
                           tosummon.setLastAttackTime(System.currentTimeMillis());
                        }

                        ArrayList count;
                        MapleSummon mob;
                        if (this.sourceid == 400041028) {
                           applyto.getClient().getSession().writeAndFlush(CField.ShadowServentRefresh(applyto, tosummon, 3));
                        } else if (this.sourceid == 5220025 || this.sourceid == 5220024 || this.sourceid == 5220023 || this.sourceid == 5221022) {
                           count = new ArrayList();
                           int size = 0;
                           Iterator var25 = applyfrom.getMap().getAllSummonsThreadsafe().iterator();

                           label893:
                           while(true) {
                              do {
                                 do {
                                    do {
                                       if (!var25.hasNext()) {
                                          if (size < 2) {
                                             break label893;
                                          }

                                          int A_duration = (Integer)((Pair)count.get(0)).getRight();
                                          if (A_duration > (Integer)((Pair)count.get(1)).getRight()) {
                                             while(true) {
                                                if (!applyfrom.getBuffedValue((Integer)((Pair)count.get(1)).getLeft())) {
                                                   break label893;
                                                }

                                                applyfrom.cancelEffect(applyfrom.getBuffedEffect((Integer)((Pair)count.get(1)).getLeft()));
                                             }
                                          }

                                          while(true) {
                                             if (!applyfrom.getBuffedValue((Integer)((Pair)count.get(0)).getLeft())) {
                                                break label893;
                                             }

                                             applyfrom.cancelEffect(applyfrom.getBuffedEffect((Integer)((Pair)count.get(0)).getLeft()));
                                          }
                                       }

                                       mob = (MapleSummon)var25.next();
                                    } while(mob == null);
                                 } while(mob.getOwner().getId() != applyfrom.getId());
                              } while((mob.getSkill() < 5220023 || mob.getSkill() > 5220025) && mob.getSkill() != 5221022);

                              ++size;
                              count.add(new Pair(mob.getSkill(), (int)applyfrom.getBuffLimit(mob.getSkill())));
                           }
                        }

                        if (this.sourceid == 400011077) {
                           SkillFactory.getSkill(400011078).getEffect(this.level).applyTo(applyto, false, localDuration);
                        } else if (this.sourceid == 131001022) {
                           SkillFactory.getSkill(131002022).getEffect(this.level).applyTo(applyto, false, localDuration);
                           SkillFactory.getSkill(131003022).getEffect(this.level).applyTo(applyto, false, localDuration);
                           SkillFactory.getSkill(131004022).getEffect(this.level).applyTo(applyto, false, localDuration);
                           SkillFactory.getSkill(131005022).getEffect(this.level).applyTo(applyto, false, localDuration);
                           SkillFactory.getSkill(131006022).getEffect(this.level).applyTo(applyto, false, localDuration);
                        } else if (this.sourceid == 400031007) {
                           SkillFactory.getSkill(400031008).getEffect(this.level).applyTo(applyto, false, localDuration);
                        } else if (this.sourceid == 400031008) {
                           SkillFactory.getSkill(400031009).getEffect(this.level).applyTo(applyto, false, localDuration);
                        } else {
                           MapleSummon summon;
                           MapleSummon tosummon1;
                           if (this.sourceid == 5210015 && primary) {
                              if (applyfrom.getBuffedValue(5210015)) {
                                 applyfrom.cancelEffect(applyfrom.getBuffedEffect(5210015));
                              }

                              if (applyto.getSkillLevel(5210015) > 0) {
                                 SkillFactory.getSkill(5220019).getEffect(applyto.getSkillLevel(5220019)).applyTo(applyto, false, false);
                                 summon = applyto.getSummon(5211019);
                                 if (summon != null) {
                                    summon.removeSummon(applyto.getMap(), false);
                                 }

                                 tosummon1 = new MapleSummon(applyto, 5211019, applyto.getTruePosition(), SummonMovementType.FLAME_SUMMON, (byte)0, 120000);
                                 applyto.getMap().spawnSummon(tosummon1, 120000);
                                 applyto.addSummon(tosummon1);
                              }
                           } else {
                              MapleSummon tosummon2;
                              MapleSummon s;
                              if (this.sourceid == 400051038) {
                                 summon = applyto.getSummon(400051052);
                                 if (summon != null) {
                                    summon.removeSummon(applyto.getMap(), false);
                                 }

                                 if ((summon = applyto.getSummon(400051053)) != null) {
                                    summon.removeSummon(applyto.getMap(), false);
                                 }

                                 if ((summon = applyto.getSummon(400051038)) != null) {
                                    summon.removeSummon(applyto.getMap(), false);
                                 }

                                 try {
                                    tosummon1 = new MapleSummon(applyfrom, 400051038, new Point(applyfrom.getTruePosition().x, applyfrom.getTruePosition().y), summonMovementType, rltype, localDuration);
                                    applyfrom.getMap().spawnSummon(tosummon1, localDuration);
                                    applyfrom.addSummon(tosummon1);
                                    tosummon1.addHP(this.x);
                                    tosummon2 = new MapleSummon(applyfrom, 400051052, new Point(applyfrom.getTruePosition().x + 100, applyfrom.getTruePosition().y), summonMovementType, rltype, localDuration);
                                    applyfrom.getMap().spawnSummon(tosummon2, localDuration);
                                    applyfrom.addSummon(tosummon2);
                                    tosummon2.addHP(this.x);
                                    s = new MapleSummon(applyfrom, 400051053, new Point(applyfrom.getTruePosition().x + 200, applyfrom.getTruePosition().y), summonMovementType, rltype, localDuration);
                                    applyfrom.getMap().spawnSummon(s, localDuration);
                                    applyfrom.addSummon(s);
                                    s.addHP(this.x);
                                 } catch (Exception var30) {
                                    var30.printStackTrace();
                                 }
                              } else if (this.sourceid == 5321004) {
                                 if (applyfrom.getSkillLevel(5320044) > 0) {
                                    localDuration += SkillFactory.getSkill(5320044).getEffect(1).getDuration();
                                 }

                                 if ((summon = applyto.getSummon(5320011)) != null) {
                                    summon.removeSummon(applyto.getMap(), false);
                                 }

                                 try {
                                    Point pos1 = new Point(pos.x + (applyto.isFacingLeft() ? 90 : -90), pos.y);
                                    tosummon2 = new MapleSummon(applyfrom, 5320011, pos1, summonMovementType, rltype, localDuration);
                                    applyfrom.getMap().spawnSummon(tosummon2, localDuration);
                                    applyfrom.addSummon(tosummon2);
                                    tosummon2.addHP(this.x);
                                    if (applyfrom.getSkillLevel(5320045) > 0) {
                                       pos1 = new Point(pos.x + (applyto.isFacingLeft() ? 180 : -180), pos.y);
                                       s = new MapleSummon(applyfrom, 5320011, pos1, summonMovementType, rltype, localDuration);
                                       applyfrom.getMap().spawnSummon(s, localDuration);
                                       applyfrom.addSummon(s);
                                       s.addHP(this.x);
                                    }
                                 } catch (Exception var29) {
                                    var29.printStackTrace();
                                 }
                              } else if (this.sourceid == 4341006) {
                                 applyfrom.cancelEffectFromBuffStat(SecondaryStat.ShadowPartner);
                              } else if (this.sourceid == 35111002) {
                                 count = new ArrayList();
                                 Iterator var62 = applyfrom.getMap().getAllSummonsThreadsafe().iterator();

                                 while(var62.hasNext()) {
                                    s = (MapleSummon)var62.next();
                                    if (s.getSkill() == this.sourceid && s.getOwner().getId() == applyfrom.getId()) {
                                       count.add(s.getObjectId());
                                    }
                                 }

                                 if (count.size() == 3) {
                                    applyfrom.getClient().getSession().writeAndFlush(CField.skillCooldown(this.sourceid, this.getCooldown(applyfrom)));
                                    applyfrom.addCooldown(this.sourceid, System.currentTimeMillis(), (long)this.getCooldown(applyfrom));
                                    applyfrom.getMap().broadcastMessage(CField.teslaTriangle(applyfrom.getId(), (Integer)count.get(0), (Integer)count.get(1), (Integer)count.get(2)));
                                 }
                              } else if (this.sourceid == 35121003) {
                                 applyfrom.getClient().getSession().writeAndFlush(CWvsContext.enableActions(applyfrom));
                              } else if (this.sourceid == 400051017) {
                                 MapleAtom atom = new MapleAtom(false, applyfrom.getId(), 30, true, this.sourceid, 0, 0);
                                 List<MapleMapObject> objs = applyfrom.getMap().getMapObjectsInRange(applyto.getTruePosition(), 500000.0D, Arrays.asList(MapleMapObjectType.MONSTER));
                                 ArrayList<Integer> monsters = new ArrayList();
                                 mob = null;
                                 Iterator var27 = objs.iterator();

                                 while(var27.hasNext()) {
                                    MapleMapObject obj = (MapleMapObject)var27.next();
                                    atom.addForceAtom(new ForceAtom(1, Randomizer.rand(52, 72), Randomizer.rand(5, 6), Randomizer.rand(33, 72), 1440, applyfrom.getTruePosition()));
                                    monsters.add(obj.getObjectId());
                                 }

                                 for(int i = atom.getForceAtoms().size(); i < this.bulletCount; ++i) {
                                    MapleMonster mob = (MapleMonster)objs.get(Randomizer.rand(0, objs.size() - 1));
                                    atom.addForceAtom(new ForceAtom(1, Randomizer.rand(52, 72), Randomizer.rand(5, 6), Randomizer.rand(33, 72), 1440, applyfrom.getTruePosition()));
                                    monsters.add(mob.getObjectId());
                                 }

                                 atom.setDwTargets(monsters);
                                 ForceAtom forceAtom = new ForceAtom(1, 49, 5, Randomizer.rand(45, 90), 1440);
                                 atom.addForceAtom(forceAtom);
                                 applyfrom.getMap().spawnMapleAtom(atom);
                              }
                           }
                        }
                     }
                  }

                  if (primary && this.availableMap != null) {
                     var38 = this.availableMap.iterator();

                     while(var38.hasNext()) {
                        Pair<Integer, Integer> e = (Pair)var38.next();
                        if (applyto.getMapId() < (Integer)e.left || applyto.getMapId() > (Integer)e.right) {
                           applyto.getClient().getSession().writeAndFlush(CWvsContext.enableActions(applyto));
                           return true;
                        }
                     }
                  }

                  this.applyBuffEffect(applyfrom, applyto, primary, localDuration, pos, showEffect);
                  if (applyfrom.getId() == applyto.getId() && applyfrom.getParty() != null) {
                     Rectangle bounds = this.calculateBoundingBox(applyfrom.getTruePosition(), applyfrom.isFacingLeft());
                     List<MapleMapObject> affecteds = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.PLAYER));
                     var19 = affecteds.iterator();

                     while(var19.hasNext()) {
                        MapleMapObject affectedmo = (MapleMapObject)var19.next();
                        MapleCharacter affected = (MapleCharacter)affectedmo;
                        if (affected.getParty() != null && applyfrom.getId() != affected.getId() && this.isPartyBuff(applyfrom, affected) && this.calculateBoundingBox(applyfrom.getTruePosition(), applyfrom.isFacingLeft()).contains(applyto.getTruePosition()) && applyfrom.getParty().getId() == affected.getParty().getId()) {
                           this.applyTo(applyto, affected, primary, pos, localDuration, (byte)0, this.sourceid == 2311003);
                           affected.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(affected, 0, this.sourceid, 4, 0, 0, (byte)(affected.getTruePosition().x > pos.x ? 1 : 0), true, pos, (String)null, (Item)null));
                           affected.getMap().broadcastMessage(affected, CField.EffectPacket.showEffect(affected, 0, this.sourceid, 4, 0, 0, (byte)(affected.getTruePosition().x > pos.x ? 1 : 0), false, pos, (String)null, (Item)null), false);
                        }
                     }
                  }

                  if (!this.isMagicDoor()) {
                     if (this.isMist()) {
                        if (this.sourceid == 33111013 || this.sourceid == 21121057) {
                           pos = null;
                        }

                        if (this.sourceid == 400011098 || this.sourceid == 400011100) {
                           localDuration = this.cooltime;
                        }

                        if (this.sourceid == 4121015) {
                           applyfrom.getMap().removeMist(4121015);
                        }

                        if (this.sourceid == 61121116) {
                           localDuration = 750;
                        }

                        if (this.sourceid == 101120104) {
                           localDuration = 9000;
                        }

                        if (this.sourceid == 400031012) {
                           localDuration = 10000;
                        }

                        if (this.sourceid == 400011135) {
                           localDuration = 1800;
                        }

                        if (this.sourceid == 61121105 && applyto.getSkillLevel(61120047) > 0) {
                           localDuration += SkillFactory.getSkill(61120047).getEffect(1).getDuration();
                        }

                        if (this.sourceid == 32121006 && applyto.getSkillLevel(32120064) > 0) {
                           localDuration += SkillFactory.getSkill(32120064).getEffect(1).getDuration();
                        }

                        boolean spawn = false;
                        if ((this.sourceid == 400031039 || this.sourceid == 400031040) && primary || (this.sourceid == 400040008 || this.sourceid == 400041008) && !primary || this.sourceid == 2111003 || (this.sourceid == 12121005 || this.sourceid == 400001017 || this.sourceid == 100001261 || this.sourceid == 151121041 || this.sourceid == 4221006) && primary || this.sourceid != 400031039 && this.sourceid != 400031040 && this.sourceid != 12121005 && this.sourceid != 400001017 && this.sourceid != 100001261 && this.sourceid != 151121041 && this.sourceid != 400040008 && this.sourceid != 400041008 && this.sourceid != 4221006) {
                           spawn = true;
                        }

                        if (spawn) {
                           if (this.sourceid == 400051025 || this.sourceid == 400051026) {
                              pos = new Point(pos.x, applyto.getMap().getFootholds().findBelow(pos).getY1() + 18);
                           }

                           Rectangle bounds = this.calculateBoundingBox(pos != null ? pos : applyfrom.getTruePosition(), applyfrom.isFacingLeft());
                           MapleMist mist = new MapleMist(bounds, applyfrom, this, this.sourceid == 12121005 ? this.alchemistModifyVal(applyfrom, localDuration, false) : localDuration, rltype);
                           if (this.sourceid == 101120104) {
                              mist.setEndTime(9000);
                           }

                           if (this.sourceid == 151121041) {
                              mist.setDuration(1050);
                              mist.setEndTime(1050);
                           }

                           mist.setPosition(pos == null ? applyto.getTruePosition() : pos);
                           if (this.sourceid == 400051025 || this.sourceid == 400051026 || this.sourceid == 400031012 || this.sourceid == 22170093 || this.sourceid == 400041041) {
                              mist.setDelay(0);
                           }

                           applyfrom.getMap().spawnMist(mist, false);
                           if (applyfrom.isGM()) {
                              applyfrom.dropMessage(6, "spawn Mist : " + localDuration);
                           }

                           if (this.sourceid == 400051025) {
                              applyfrom.getMap().broadcastMessage(CField.ICBM(true, this.sourceid, this.calculateBoundingBox(pos, applyfrom.isFacingLeft())));
                           }
                        }
                     }
                  } else {
                     MapleDoor door = new MapleDoor(applyto, new Point(pos == null ? applyto.getTruePosition() : pos), this.sourceid);
                     if (door.getTownPortal() != null) {
                        applyto.getMap().spawnDoor(door);
                        applyto.addDoor(door);
                        MapleDoor townDoor = new MapleDoor(door);
                        applyto.addDoor(townDoor);
                        door.getTown().spawnDoor(townDoor);
                        if (applyto.getParty() != null) {
                           applyto.silentPartyUpdate();
                        }
                     } else {
                        applyto.dropMessage(5, "You may not spawn a door because all doors in the town are taken.");
                     }
                  }

                  Iterator var32;
                  if (this.isTimeLeap() && System.currentTimeMillis() - applyto.lastTimeleapTime >= (long)this.duration) {
                     var32 = applyto.getCooldowns().iterator();

                     while(var32.hasNext()) {
                        MapleCoolDownValueHolder i = (MapleCoolDownValueHolder)var32.next();
                        if (i.skillId != 5121010 && !SkillFactory.getSkill(i.skillId).isHyper() && i.skillId / 10000 <= applyto.getJob()) {
                           applyto.lastTimeleapTime = System.currentTimeMillis();
                           applyto.removeCooldown(i.skillId);
                           applyto.getClient().getSession().writeAndFlush(CField.skillCooldown(i.skillId, 0));
                        }
                     }
                  }

                  if (this.rewardMeso != 0) {
                     applyto.gainMeso((long)this.rewardMeso, false);
                  }

                  if (this.rewardItem != null && this.totalprob > 0) {
                     var32 = this.rewardItem.iterator();

                     while(var32.hasNext()) {
                        Triple<Integer, Integer, Integer> reward = (Triple)var32.next();
                        if (MapleInventoryManipulator.checkSpace(applyto.getClient(), (Integer)reward.left, (Integer)reward.mid, "") && (Integer)reward.right > 0 && Randomizer.nextInt(this.totalprob) < (Integer)reward.right) {
                           int var10001;
                           if (GameConstants.getInventoryType((Integer)reward.left) == MapleInventoryType.EQUIP) {
                              Item item = MapleItemInformationProvider.getInstance().getEquipById((Integer)reward.left);
                              var10001 = this.sourceid;
                              item.setGMLog("Reward item (effect): " + var10001 + " on " + FileoutputUtil.CurrentReadable_Date());
                              MapleInventoryManipulator.addbyItem(applyto.getClient(), item);
                           } else {
                              MapleClient var10000 = applyto.getClient();
                              var10001 = (Integer)reward.left;
                              short var10002 = ((Integer)reward.mid).shortValue();
                              int var10003 = this.sourceid;
                              MapleInventoryManipulator.addById(var10000, var10001, var10002, "Reward item (effect): " + var10003 + " on " + FileoutputUtil.CurrentReadable_Date());
                           }
                        }
                     }
                  }

                  return true;
               }
            }
         }
      } else {
         applyfrom.getClient().getSession().writeAndFlush(CWvsContext.enableActions(applyto));
         return false;
      }
   }

   public final boolean applyReturnScroll(MapleCharacter applyto) {
      if (this.moveTo == -1 || applyto.getMap().getReturnMapId() == applyto.getMapId() && this.sourceid != 2031010 && this.sourceid != 2030021) {
         return false;
      } else {
         MapleMap target;
         if (this.moveTo == 999999999) {
            target = applyto.getMap().getReturnMap();
         } else {
            target = ChannelServer.getInstance(applyto.getClient().getChannel()).getMapFactory().getMap(this.moveTo);
            if (target.getId() / 10000000 != 60 && applyto.getMapId() / 10000000 != 61 && target.getId() / 10000000 != 21 && applyto.getMapId() / 10000000 != 20 && target.getId() / 10000000 != applyto.getMapId() / 10000000) {
               return false;
            }
         }

         applyto.changeMap(target, target.getPortal(0));
         return true;
      }
   }

   public final Rectangle calculateBoundingBox(int skillid, int level, Point posFrom, boolean facingLeft) {
      return calculateBoundingBox(posFrom, facingLeft, SkillFactory.getSkill(skillid).getEffect(level).lt, SkillFactory.getSkill(skillid).getEffect(level).rb, this.range);
   }

   public final Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft) {
      return calculateBoundingBox(posFrom, facingLeft, this.lt, this.rb, this.range);
   }

   public final Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft, int addedRange) {
      return calculateBoundingBox(posFrom, facingLeft, this.lt, this.rb, this.range + addedRange);
   }

   public static Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft, Point lt, Point rb2, int range) {
      if (lt != null && rb2 != null) {
         Point myrb;
         Point mylt;
         if (facingLeft) {
            mylt = new Point(lt.x + posFrom.x - range, lt.y + posFrom.y);
            myrb = new Point(rb2.x + posFrom.x, rb2.y + posFrom.y);
         } else {
            myrb = new Point(lt.x * -1 + posFrom.x + range, rb2.y + posFrom.y);
            mylt = new Point(rb2.x * -1 + posFrom.x, lt.y + posFrom.y);
         }

         return new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
      } else {
         return new Rectangle((facingLeft ? -200 - range : 0) + posFrom.x, -100 - range + posFrom.y, 200 + range, 100 + range);
      }
   }

   public final double getMaxDistanceSq() {
      int maxX = Math.max(Math.abs(this.getLt() == null ? 0 : this.getLt().x), Math.abs(this.rb == null ? 0 : this.rb.x));
      int maxY = Math.max(Math.abs(this.getLt() == null ? 0 : this.getLt().y), Math.abs(this.rb == null ? 0 : this.rb.y));
      return (double)(maxX * maxX + maxY * maxY);
   }

   public final void setDuration(int d) {
      this.duration = d;
   }

   public final void silentApplyBuff(MapleCharacter chr, long starttime, Map<SecondaryStat, Pair<Integer, Integer>> statup, int cid) {
      HashMap<SecondaryStat, Pair<Integer, Integer>> cancelStats = new HashMap();
      Iterator var7 = statup.entrySet().iterator();

      while(var7.hasNext()) {
         Entry<SecondaryStat, Pair<Integer, Integer>> statupz = (Entry)var7.next();
         long remainDuration = (long)(Integer)((Pair)statupz.getValue()).right - (System.currentTimeMillis() - starttime);
         MapleSummon tosummon;
         SummonMovementType summonMovementType;
         if (remainDuration > 0L) {
            chr.registerEffect(this, starttime, statupz, true, cid);
            summonMovementType = this.getSummonMovementType();
            if (summonMovementType != null && !(tosummon = new MapleSummon(chr, this, chr.getTruePosition(), summonMovementType)).isPuppet()) {
               chr.getMap().spawnSummon(tosummon, (int)remainDuration);
               chr.addSummon(tosummon);
               tosummon.addHP(this.x);
            }
         } else if ((Integer)((Pair)statupz.getValue()).right == 0) {
            chr.registerEffect(this, starttime, statupz, true, cid);
            summonMovementType = this.getSummonMovementType();
            if (summonMovementType != null && !(tosummon = new MapleSummon(chr, this, chr.getTruePosition(), summonMovementType)).isPuppet()) {
               chr.getMap().spawnSummon(tosummon, (Integer)((Pair)statupz.getValue()).right);
               chr.addSummon(tosummon);
               tosummon.addHP(this.x);
            }
         } else {
            cancelStats.put((SecondaryStat)statupz.getKey(), new Pair(0, 0));
         }
      }

      if (!cancelStats.isEmpty()) {
         chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.cancelBuff(cancelStats, chr));
      }

   }

   public final void applyKaiserCombo(MapleCharacter applyto, short combo) {
      EnumMap<SecondaryStat, Pair<Integer, Integer>> stat = new EnumMap(SecondaryStat.class);
      stat.put(SecondaryStat.SmashStack, new Pair(Integer.valueOf(combo), 0));
      applyto.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(stat, (SecondaryStatEffect)null, applyto));
   }

   private final void applyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary, int newDuration, Point pos, boolean showEffect) {
      int localDuration = newDuration;
      if (pos == null) {
         pos = applyto.getTruePosition() != null ? applyto.getTruePosition() : applyto.getPosition();
      }

      HashMap<SecondaryStat, Pair<Integer, Integer>> localstatups = new HashMap();
      ArrayList<Pair<Integer, Integer>> indieList1 = new ArrayList();
      ArrayList<Pair<Integer, Integer>> indieList2 = new ArrayList();
      boolean aftercancel = false;
      boolean bufftimeR = true;
      boolean isPetBuff = false;
      boolean cancel = true;
      MaplePet[] var16 = applyto.getPets();
      int up = var16.length;

      int i;
      for(i = 0; i < up; ++i) {
         MaplePet pet = var16[i];
         if (pet != null && pet.getBuffSkillId() == this.sourceid && pet.getBuffSkillId2() == this.sourceid) {
            isPetBuff = true;
         }
      }

      if (applyto.getKeyValue(9999, "skillid") > 0L || applyto.getKeyValue(9999, "skillid2") > 0L || applyto.getKeyValue(9999, "skillid3") > 0L || applyto.getKeyValue(9999, "skillid4") > 0L || applyto.getKeyValue(9999, "skillid5") > 0L || applyto.getKeyValue(9999, "skillid6") > 0L) {
         isPetBuff = false;
      }

      Iterator var29;
      if (GameConstants.isPhantom(applyto.getJob())) {
         var29 = applyto.getStolenSkills().iterator();

         label3960:
         while(var29.hasNext()) {
            Pair pair = (Pair)var29.next();
            if ((Integer)pair.left == this.sourceid && (Boolean)pair.right) {
               switch((Integer)pair.left) {
               case 2121054:
               case 2221054:
               case 2321054:
               case 3121054:
               case 4121054:
               case 5121054:
               case 5221054:
                  localDuration = 30000;
               default:
                  break label3960;
               }
            }
         }
      }

      Iterator var38;
      int FreudsProtection;
      Rectangle bounds;
      List list;
      boolean exit;
      long starttime;
      MapleMapObject affectedmo;
      MapleCharacter affected;
      int size3;
      int n;
      int[] array2;
      ArrayList skills;
      int value;
      MapleCoolDownValueHolder i;
      ArrayList remove;
      short s;
      SecondaryStat[] stats;
      SecondaryStatEffect eff;
      byte pad2;
      short pdd;
      boolean type;
      SecondaryStatEffect ef;
      Equip eq;
      short asr;
      label3948:
      switch(this.sourceid) {
      case 261:
         localstatups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(this.indieDamR), localDuration));
         break;
      case 410115:
      case 63001002:
         localstatups.clear();
         localstatups.put(SecondaryStat.DarkSight, new Pair(Integer.valueOf(this.level), 420));
         break;
      case 1111003:
         localstatups.clear();
         if (!applyto.getBuffedValue(1111003)) {
            applyto.setSkillCustomInfo(1111003, 1L, 0L);
         } else {
            applyto.setSkillCustomInfo(1111003, applyto.getSkillCustomValue0(1111003) + 1L, 0L);
            if (applyto.getSkillCustomValue0(1111003) > 8L) {
               applyto.setSkillCustomInfo(1111003, 8L, 0L);
            }
         }

         localstatups.put(SecondaryStat.ComboCostInc, new Pair((int)applyto.getSkillCustomValue0(1111003), 20000));
         bufftimeR = false;
         break;
      case 1121010:
         applyfrom.handleOrbconsume(1121010);
         break;
      case 1200014:
      case 1220010:
         if (applyfrom.getElementalCharge() > 0 && applyfrom.getElementalCharge() <= this.getZ()) {
            localstatups.clear();
            value = this.u;
            if (applyfrom.getSkillLevel(1220010) > 0) {
               value = SkillFactory.getSkill(1220010).getEffect(applyfrom.getSkillLevel(1220010)).getX();
            }

            localstatups.put(SecondaryStat.ElementalCharge, new Pair(value * applyfrom.getElementalCharge(), localDuration));
         }
         break;
      case 1210016:
         localstatups.clear();
         localstatups.put(SecondaryStat.BlessingArmor, new Pair((int)applyfrom.getSkillCustomValue0(this.sourceid), localDuration));
         localstatups.put(SecondaryStat.BlessingArmorIncPad, new Pair(Integer.valueOf(this.epad), localDuration));
         break;
      case 1211010:
         bufftimeR = false;
         if (applyfrom.getListonation() < 5) {
            applyfrom.setListonation(applyfrom.getListonation() + 1);
         }

         localstatups.clear();
         localstatups.put(SecondaryStat.Listonation, new Pair(applyfrom.getListonation() * this.y, localDuration));
         break;
      case 1211014:
         applyto.setSkillCustomInfo(this.sourceid, (long)applyfrom.getId(), 0L);
         if (primary) {
            localstatups.put(SecondaryStat.KnightsAura, new Pair(this.y, 0));
            localstatups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(this.indiePad), 0));
            localstatups.put(SecondaryStat.IndiePddR, new Pair(this.z, 0));
         } else {
            localstatups.put(SecondaryStat.KnightsAura, new Pair(this.y, 0));
            localstatups.put(SecondaryStat.IndiePddR, new Pair(-this.z, 0));
         }
         break;
      case 1221016:
         bufftimeR = false;
         break;
      case 1310016:
         value = 0;
         up = this.epad;
         s = this.epdd;
         FreudsProtection = this.indieCr;
         if (applyfrom.getSkillLevel(1320044) > 0) {
            up += SkillFactory.getSkill(1320044).getEffect(1).getX();
         }

         if (applyfrom != applyto) {
            up /= 2;
            value = s / 2;
            FreudsProtection /= 2;
         }

         localstatups.put(SecondaryStat.EnhancedPad, new Pair(up, 0));
         localstatups.put(SecondaryStat.EnhancedPdd, new Pair(value, 0));
         localstatups.put(SecondaryStat.IndieCr, new Pair(FreudsProtection, 0));
         break;
      case 1311015:
         localDuration = 0;
         localstatups.clear();
         if (GameConstants.isDarkKnight(applyto.getJob()) && applyto.hasDonationSkill(1311015)) {
            localstatups.put(SecondaryStat.CrossOverChain, new Pair(this.x * 2, localDuration));
         } else {
            localstatups.put(SecondaryStat.CrossOverChain, new Pair(this.x, localDuration));
         }
         break;
      case 1320016:
         localDuration = 261000;
         localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
         break;
      case 1320019:
         value = this.z;
         if (applyfrom.getSkillLevel(1320047) > 0) {
            value -= this.z * SkillFactory.getSkill(1320047).getEffect(1).getX() / 100;
         }

         applyfrom.setReinCarnation(value);
         applyfrom.removeCooldown(1321013);
         break;
      case 1321020:
         localDuration = 2000;
         switch(applyto.리인카네이션) {
         case 1:
            localDuration = 2000;
            break;
         case 2:
            localDuration = 8000;
            break;
         case 3:
            localDuration = 40000;
         }

         localstatups.put(SecondaryStat.ReincarnationAccept, new Pair(applyto.리인카네이션, localDuration));
         break;
      case 2111011:
      case 2311012:
         aftercancel = true;
         localstatups.put(SecondaryStat.AntiMagicShell, new Pair(1, 0));
         applyfrom.setAntiMagicShell((byte)1);
         break;
      case 2111013:
         localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
         break;
      case 2120010:
      case 2220010:
      case 2320011:
         bufftimeR = false;
         localstatups.clear();
         localstatups.put(SecondaryStat.ArcaneAim, new Pair(applyto.getArcaneAim(), 5000));
         break;
      case 2121004:
      case 2221004:
      case 2321004:
         localstatups.clear();
         localstatups.put(SecondaryStat.Infinity, new Pair(1, localDuration));
         applyfrom.setInfinity((byte)0);
         break;
      case 2201009:
         if (applyfrom.getBuffedValue(2201009)) {
            return;
         }

         localstatups.put(SecondaryStat.ChillingStep, new Pair(1, 0));
         bufftimeR = false;
         aftercancel = true;
         break;
      case 2211012:
         if (!primary) {
            localstatups.put(SecondaryStat.AntiMagicShell, new Pair(1, 0));
            applyto.setAntiMagicShell((byte)100);
         } else {
            localstatups.put(SecondaryStat.AntiMagicShell, new Pair(0, 0));
            applyto.setAntiMagicShell((byte)1);
         }
         break;
      case 2211015:
         localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
         break;
      case 2221054:
         localDuration = 0;
         applyto.setSkillCustomInfo(this.sourceid, (long)applyfrom.getId(), localDuration <= 0 ? 0L : (long)localDuration);
         localstatups.put(SecondaryStat.IceAura, new Pair(1, localDuration <= 0 ? 0 : localDuration));
         localstatups.put(SecondaryStat.IndieAsrR, new Pair(this.z, localDuration <= 0 ? 0 : localDuration));
         localstatups.put(SecondaryStat.IndieTerR, new Pair(this.w, localDuration <= 0 ? 0 : localDuration));
         break;
      case 2300009:
         localstatups.put(SecondaryStat.BlessingAnsanble, new Pair((int)applyto.getSkillCustomValue0(2320013), 0));
         break;
      case 2301004:
         if (applyto.getBuffedValue(2321005)) {
            applyto.cancelEffectFromBuffStat(SecondaryStat.AdvancedBless, 2321005);
            applyto.cancelEffectFromBuffStat(SecondaryStat.IndiePad, 2321005);
            applyto.cancelEffectFromBuffStat(SecondaryStat.IndieMad, 2321005);
            applyto.cancelEffectFromBuffStat(SecondaryStat.IndieHp, 2321005);
            applyto.cancelEffectFromBuffStat(SecondaryStat.IndieMp, 2321005);
         }

         localstatups.put(SecondaryStat.Bless, new Pair(Integer.valueOf(this.level), localDuration));
         localstatups.put(SecondaryStat.IndiePad, new Pair(this.x, localDuration));
         localstatups.put(SecondaryStat.IndieMad, new Pair(this.y, localDuration));
         localstatups.put(SecondaryStat.IndiePdd, new Pair(this.z, localDuration));
         break;
      case 2310013:
         bufftimeR = false;
         localstatups.put(SecondaryStat.CooltimeHolyMagicShell, new Pair(1, SkillFactory.getSkill(2311009).getEffect(this.level).getY() * 1000));
         break;
      case 2311003:
         if (primary) {
            applyto.setSkillCustomInfo(this.sourceid, (long)applyfrom.getId(), 0L);
            applyto.setSkillCustomInfo(2311004, 0L, 0L);
         }

         if (!showEffect) {
            showEffect = true;
            bufftimeR = false;
         }

         asr = SkillFactory.getSkill(2320047).getEffect(1).asrR;
         if (applyfrom.getId() == applyto.getId() && applyfrom.getSkillLevel(2320046) > 0) {
            localstatups.put(SecondaryStat.HolySymbol, new Pair(this.x + SkillFactory.getSkill(2320046).getEffect(1).getY(), localDuration));
         } else {
            localstatups.put(SecondaryStat.HolySymbol, new Pair(!primary ? this.x / 2 : this.x, localDuration));
         }

         if (applyfrom.getSkillLevel(2320047) > 0) {
            localstatups.put(SecondaryStat.IndieAsrR, new Pair(!primary ? asr / 2 : asr, localDuration));
            localstatups.put(SecondaryStat.IndieTerR, new Pair(!primary ? asr / 2 : asr, localDuration));
         }

         if (applyfrom.getSkillLevel(2320048) > 0) {
            applyto.setSkillCustomInfo(2320048, (long)SkillFactory.getSkill(2320048).getEffect(1).v, 0L);
            localstatups.put(SecondaryStat.DropRate, new Pair(SkillFactory.getSkill(2320048).getEffect(1).v, localDuration));
         }
         break;
      case 2311009:
         if (applyto.getBuffedEffect(SecondaryStat.CooltimeHolyMagicShell) == null) {
            byte data = (byte)this.x;
            if (applyfrom.getSkillLevel(2320043) > 0) {
               data = (byte)(data + SkillFactory.getSkill(2320043).getEffect(1).getX());
            }

            applyto.setHolyMagicShell(data);
            this.hpR = (double)this.z / 100.0D;
            SkillFactory.getSkill(2310013).getEffect(this.level).applyTo(applyfrom, applyto);
            if (applyfrom.getSkillLevel(2320044) > 0) {
               localDuration += SkillFactory.getSkill(2320044).getEffect(applyfrom.getSkillLevel(2320044)).getDuration();
            }

            localstatups.put(SecondaryStat.HolyMagicShell, new Pair(Integer.valueOf(applyto.getHolyMagicShell()), localDuration));
         }
         break;
      case 2311014:
         localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
         break;
      case 2321001:
         localstatups.clear();
         if (primary) {
            return;
         }

         if (localDuration != this.duration) {
            bufftimeR = false;
         }
         break;
      case 2321005:
         if (applyto.getBuffedValue(2301004)) {
            applyto.cancelEffectFromBuffStat(SecondaryStat.Bless, 2301004);
            applyto.cancelEffectFromBuffStat(SecondaryStat.IndiePad, 2301004);
            applyto.cancelEffectFromBuffStat(SecondaryStat.IndieMad, 2301004);
            applyto.cancelEffectFromBuffStat(SecondaryStat.IndiePdd, 2301004);
         }

         if (applyto.getBuffedValue(400001005)) {
            applyto.cancelEffectFromBuffStat(SecondaryStat.IndieMp, 400001005);
            applyto.cancelEffectFromBuffStat(SecondaryStat.IndiePad, 400001005);
            applyto.cancelEffectFromBuffStat(SecondaryStat.IndieMad, 400001005);
            applyto.cancelEffectFromBuffStat(SecondaryStat.IndieHp, 400001005);
         }

         localstatups.put(SecondaryStat.AdvancedBless, new Pair(Integer.valueOf(this.level), localDuration));
         if (applyfrom.getSkillLevel(2320051) <= 0) {
            localstatups.put(SecondaryStat.IndieMp, new Pair(Integer.valueOf(this.indieMmp), localDuration));
            localstatups.put(SecondaryStat.IndieHp, new Pair(Integer.valueOf(this.indieMhp), localDuration));
         } else {
            localstatups.put(SecondaryStat.IndieMp, new Pair(this.indieMmp + SkillFactory.getSkill(2320051).getEffect(1).indieMmp, localDuration));
            localstatups.put(SecondaryStat.IndieHp, new Pair(this.indieMhp + SkillFactory.getSkill(2320051).getEffect(1).indieMhp, localDuration));
         }

         if (applyfrom.getSkillLevel(2320049) <= 0) {
            localstatups.put(SecondaryStat.IndieMad, new Pair(this.y, localDuration));
            localstatups.put(SecondaryStat.IndiePad, new Pair(this.x, localDuration));
         } else {
            localstatups.put(SecondaryStat.IndieMad, new Pair(this.y + SkillFactory.getSkill(2320049).getEffect(1).getX(), localDuration));
            localstatups.put(SecondaryStat.IndiePad, new Pair(this.x + SkillFactory.getSkill(2320049).getEffect(1).getX(), localDuration));
         }
         break;
      case 2321006:
         localstatups.clear();
         if (primary) {
            aftercancel = true;
            localstatups.put(SecondaryStat.NotDamaged, new Pair(1, localDuration));
         } else {
            bufftimeR = false;
            aftercancel = true;
            value = applyfrom.getStat().getTotalInt() / this.y * this.w;
            localstatups.put(SecondaryStat.IndieDamR, new Pair(this.x + value, this.subTime));
         }
         break;
      case 2321016:
         localDuration = this.w;
         value = applyto.getStat().getTotalInt() / this.s;
         localstatups.put(SecondaryStat.HolyBlood, new Pair(1, localDuration));
         localstatups.put(SecondaryStat.IndieShotDamage, new Pair(this.v, localDuration));
         localstatups.put(SecondaryStat.IndiePmdR, new Pair(Math.min(this.s2, this.u + this.u * value), localDuration));
         break;
      case 2321052:
         bufftimeR = false;
         if (applyto.getBuffedEffect(SecondaryStat.CooldownHeavensDoor) == null && !applyto.getBuffedValue(2321052)) {
            localstatups.put(SecondaryStat.HeavensDoor, new Pair(1, 0));
         }
         break;
      case 2321054:
         var29 = applyto.getMap().getAllSummonsThreadsafe().iterator();

         while(true) {
            MapleSummon mapleSummon;
            do {
               do {
                  if (!var29.hasNext()) {
                     if (applyto.getBuffedEffect(400021032) == null) {
                        if (applyto.getBuffedEffect(400021033) != null) {
                           starttime = applyto.getBuffLimit(400021033);

                           while(applyto.getBuffedValue(400021033)) {
                              applyto.cancelEffect(applyto.getBuffedEffect(400021033));
                           }

                           SkillFactory.getSkill(400021032).getEffect(applyto.getSkillLevel(400021032)).applyTo(applyto, false, (int)starttime);
                        }
                     } else {
                        starttime = applyto.getBuffLimit(400021032);

                        while(applyto.getBuffedValue(400021032)) {
                           applyto.cancelEffect(applyto.getBuffedEffect(400021032));
                        }

                        SkillFactory.getSkill(400021033).getEffect(applyto.getSkillLevel(400021032)).applyTo(applyto, false, (int)starttime);
                     }
                     break label3948;
                  }

                  mapleSummon = (MapleSummon)var29.next();
               } while(mapleSummon.getOwner().getId() != applyto.getId());
            } while(mapleSummon.getSkill() != 400021032 && mapleSummon.getSkill() != 400021033);

            mapleSummon.removeSummon(applyto.getMap(), false);
            applyto.removeSummon(mapleSummon);
         }
      case 2321055:
         bufftimeR = false;
         if (applyto.getBuffedEffect(SecondaryStat.CooldownHeavensDoor) != null) {
            return;
         }

         localstatups.put(SecondaryStat.CooldownHeavensDoor, new Pair(1, 600000));
         break;
      case 3101008:
         localstatups.put(SecondaryStat.KeyDownMoving, new Pair(10, 0));
         break;
      case 3101009:
         aftercancel = true;
         if (primary) {
            applyto.setQuiverType((byte)1);
            applyto.getRestArrow()[0] = 1;
            applyto.getRestArrow()[1] = 2;
         }

         switch(applyto.getQuiverType()) {
         case 1:
            localstatups.put(SecondaryStat.QuiverCatridge, new Pair(applyto.getRestArrow()[0], 0));
            break label3948;
         case 2:
            localstatups.put(SecondaryStat.QuiverCatridge, new Pair(applyto.getRestArrow()[1], 0));
         default:
            break label3948;
         }
      case 3110001:
         bufftimeR = false;
         aftercancel = true;
         if (applyto.getBuffedEffect(SecondaryStat.IndieDamR, this.sourceid) == null) {
            if (applyto.getMortalBlow() != this.x) {
               applyto.setMortalBlow((byte)(applyto.getMortalBlow() + 1));
               localstatups.put(SecondaryStat.MortalBlow, new Pair(Integer.valueOf(applyto.getMortalBlow()), 0));
            } else {
               applyto.setMortalBlow((byte)0);
               applyto.cancelEffect(this);
               localstatups.put(SecondaryStat.IndieDamR, new Pair(this.y, localDuration));
            }
         }
         break;
      case 3110012:
         bufftimeR = false;
         localstatups.put(SecondaryStat.BowMasterConcentration, new Pair(applyto.getConcentration() * 5, localDuration));
         break;
      case 3111015:
         bufftimeR = false;
         aftercancel = true;
         localstatups.put(SecondaryStat.FlashMirage, new Pair(applyto.플레시미라주스택, 0));
         break;
      case 3111017:
      case 3211019:
         localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
         break;
      case 3121002:
      case 3221002:
      case 3321022:
         localstatups.clear();
         if (applyfrom.getSkillLevel(3120043) > 0) {
            localDuration += SkillFactory.getSkill(3120043).getEffect(1).getDuration();
         }

         if (applyfrom.getSkillLevel(3220043) > 0) {
            localDuration += SkillFactory.getSkill(3220043).getEffect(1).getDuration();
         }

         if (applyfrom.getSkillLevel(3320025) > 0) {
            localDuration += SkillFactory.getSkill(3320025).getEffect(1).getDuration();
         }

         if (applyfrom.getSkillLevel(3120044) > 0) {
            localstatups.put(SecondaryStat.IndieIgnoreMobPdpR, new Pair(Integer.valueOf(SkillFactory.getSkill(3120044).getEffect(1).ignoreMobpdpR), localDuration));
         }

         if (applyfrom.getSkillLevel(3220044) > 0) {
            localstatups.put(SecondaryStat.IndieIgnoreMobPdpR, new Pair(Integer.valueOf(SkillFactory.getSkill(3220044).getEffect(1).ignoreMobpdpR), localDuration));
         }

         if (applyfrom.getSkillLevel(3320026) > 0) {
            localstatups.put(SecondaryStat.IndieIgnoreMobPdpR, new Pair(Integer.valueOf(SkillFactory.getSkill(3320026).getEffect(1).ignoreMobpdpR), localDuration));
         }

         localstatups.put(SecondaryStat.SharpEyes, new Pair(((applyfrom.getSkillLevel(3120045) <= 0 ? (applyfrom.getSkillLevel(3220045) <= 0 ? (applyfrom.getSkillLevel(3320027) <= 0 ? 0 : 5) : 5) : 5) + this.x << 8) + this.y, localDuration));
         break;
      case 3210001:
         localstatups.clear();
         if (!applyto.getBuffedValue(this.sourceid)) {
            if (Randomizer.isSuccess(this.x)) {
               localstatups.put(SecondaryStat.IndieDamR, new Pair(this.y, 0));
               applyto.addHP(applyto.getStat().getCurrentMaxHp() / 100L * (long)this.z);
               applyto.addMP(applyto.getStat().getCurrentMaxHp() / 100L * (long)this.z);
            }
         } else {
            applyto.cancelEffect(this);
         }
         break;
      case 3210013:
         bufftimeR = false;
         localstatups.put(SecondaryStat.PowerTransferGauge, new Pair(applyto.getBarrier(), localDuration));
         break;
      case 3310006:
         if (applyto.getBuffedValue(this.sourceid)) {
            localDuration = (int)((long)localDuration + applyto.getBuffLimit(this.sourceid));
         }

         localstatups.clear();
         localstatups.put(SecondaryStat.IndiePmdR, new Pair(Integer.valueOf(this.indiePmdR), localDuration));
         applyto.addHP(applyto.getStat().getCurrentMaxHp() / 100L * (long)this.y);
         applyto.addMP(applyto.getStat().getCurrentMaxMp(applyto) / 100L * (long)this.y);
         applyto.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(applyto, 0, 3310006, 1, 0, 0, (byte)(!applyto.isFacingLeft() ? 0 : 1), true, applyto.getTruePosition(), (String)null, (Item)null));
         applyto.getMap().broadcastMessage(applyto, CField.EffectPacket.showEffect(applyto, 0, 3310006, 1, 0, 0, (byte)(!applyto.isFacingLeft() ? 0 : 1), false, applyto.getTruePosition(), (String)null, (Item)null), false);
         break;
      case 3320008:
         showEffect = false;
         localstatups.put(SecondaryStat.BonusAttack, new Pair((int)applyto.getSkillCustomValue0(3320008), localDuration));
         break;
      case 3321034:
         localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
         if (GameConstants.isPathFinder(applyto.getJob())) {
            applyto.렐릭게이지 = 1000;
            applyto.에인션트가이던스 = 0;
            SkillFactory.getSkill(3310006).getEffect(applyto.getSkillLevel(3310006)).applyTo(applyto);
            MapleCharacter.렐릭게이지(applyfrom.getClient(), 0);
         }
         break;
      case 4001003:
         localstatups.put(SecondaryStat.DarkSight, new Pair(this.x, localDuration));
         break;
      case 4111009:
      case 5201008:
      case 14110031:
      case 14111025:
         localDuration = this.duration;
         if (newDuration / 10000 == 207 && this.sourceid != 5201008) {
            localstatups.put(SecondaryStat.NoBulletConsume, new Pair(newDuration - 2070000 + 1, localDuration));
         } else if (newDuration / 10000 == 233 && this.sourceid == 5201008) {
            localstatups.put(SecondaryStat.NoBulletConsume, new Pair(newDuration - 2330000 + 1, localDuration));
         }
         break;
      case 4121017:
         if (!primary) {
            localDuration = 5000;
            localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
         }
         break;
      case 4211016:
         applyto.addCooldown(4001003, System.currentTimeMillis(), 3000L);
         applyto.getClient().getSession().writeAndFlush(CField.skillCooldown(4001003, 3000));
         applyto.addCooldown(4211016, System.currentTimeMillis(), 3000L);
         applyto.getClient().getSession().writeAndFlush(CField.skillCooldown(4211016, 3000));
         if (applyto.getSkillLevel(4001003) > 0) {
            SkillFactory.getSkill(4001003).getEffect(applyto.getSkillLevel(4001003)).applyTo(applyto, false);
         }
         break;
      case 4221006:
         localstatups.clear();
         if (!primary) {
            localstatups.put(SecondaryStat.DamageDecreaseWithHP, new Pair(this.y, 1800));
         }
         break;
      case 4221016:
         if (applyto.getSkillLevel(400041025) > 0 && applyto.shadowerDebuffOid != 0) {
            aftercancel = true;
            localstatups.put(SecondaryStat.ShadowerDebuff, new Pair(applyto.shadowerDebuff, 10000));
         }
         break;
      case 4221054:
         bufftimeR = false;
         aftercancel = true;
         localDuration = 0;
         localstatups.put(SecondaryStat.IndieDamR, new Pair(this.indieDamR * applyto.getFlip(), localDuration));
         localstatups.put(SecondaryStat.IndieCr, new Pair(this.x * applyto.getFlip(), localDuration));
         localstatups.put(SecondaryStat.FlipTheCoin, new Pair(Integer.valueOf(applyto.getFlip()) + 1, localDuration));
         break;
      case 4301003:
         localstatups.put(SecondaryStat.IndieSpeed, new Pair(Integer.valueOf(this.indieSpeed), localDuration));
         localstatups.put(SecondaryStat.IndieJump, new Pair(Integer.valueOf(this.indieJump), localDuration));
         break;
      case 4330009:
         bufftimeR = false;
         localstatups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(this.indiePad), localDuration));
         break;
      case 4331006:
         bufftimeR = false;
         localstatups.put(SecondaryStat.NotDamaged, new Pair(1, 1500));
         break;
      case 4341002:
         localstatups.clear();
         if (applyto.hasDonationSkill(4341002) && (GameConstants.isDualBlade(applyto.getJob()) || GameConstants.isPhantom(applyto.getJob()))) {
            if (!primary) {
               bufftimeR = false;
               localDuration = 3000;
               localstatups.put(SecondaryStat.IndiePmdR, new Pair(40, 0));
               localstatups.put(SecondaryStat.NotDamaged, new Pair(1, 3000));
               localstatups.put(SecondaryStat.IgnorePCounter, new Pair(1, 3000));
            } else {
               bufftimeR = true;
               localstatups.put(SecondaryStat.FinalCut, new Pair(this.y, localDuration));
            }
         } else if (!primary) {
            bufftimeR = false;
            localDuration = 3000;
            localstatups.put(SecondaryStat.IgnorePCounter, new Pair(1, 3000));
            localstatups.put(SecondaryStat.NotDamaged, new Pair(1, 3000));
         } else {
            bufftimeR = true;
            localstatups.put(SecondaryStat.FinalCut, new Pair(this.y, localDuration));
         }
         break;
      case 5101017:
         localstatups.put(SecondaryStat.SeaSerpent, new Pair(1, 0));
         break;
      case 5110020:
         localDuration = 16000;
         localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
         break;
      case 5111007:
      case 5120012:
      case 5211007:
      case 5220014:
      case 5311005:
      case 5320007:
      case 35111013:
      case 35120014:
         boolean extra = false;
         if (applyfrom.getSkillLevel(5120044) > 0 || applyfrom.getSkillLevel(5220044) > 0) {
            extra = true;
         }

         exit = applyto.getBuffedEffect(SecondaryStat.SelectDice) != null;
         FreudsProtection = 0;
         if (exit) {
            FreudsProtection = applyto.getBuffedValue(SecondaryStat.SelectDice);
         }

         byte dice = (byte)Randomizer.rand(1, !extra ? 6 : 7);
         byte doubledice = 1;
         if (this.isDoubleDice() && this.makeChanceResult()) {
            doubledice = (byte)Randomizer.rand(2, !extra ? 6 : 7);
         }

         if (applyto.isOneMoreChance()) {
            applyto.setOneMoreChance(false);
            dice = (byte)Randomizer.rand(4, !extra ? 6 : 7);
            if (doubledice > 1) {
               doubledice = (byte)Randomizer.rand(4, !extra ? 6 : 7);
            }
         }

         applyto.setDice(FreudsProtection * 100 + doubledice * 10 + dice);
         if (applyfrom.getSkillLevel(5120043) > 0 && applyto.getDice() == 11) {
            applyfrom.setOneMoreChance(true);
            if (SkillFactory.getSkill(5120043).getEffect(1).makeChanceResult()) {
               applyfrom.removeCooldown(this.sourceid);
               applyfrom.getClient().getSession().writeAndFlush(CField.skillCooldown(this.sourceid, 0));
            }
         }

         if (applyfrom.getSkillLevel(5220043) > 0 && applyto.getDice() == 11) {
            applyfrom.setOneMoreChance(true);
            if (SkillFactory.getSkill(5220043).getEffect(1).makeChanceResult()) {
               applyfrom.removeCooldown(this.sourceid);
               applyfrom.getClient().getSession().writeAndFlush(CField.skillCooldown(this.sourceid, 0));
            }
         }

         localstatups.put(SecondaryStat.DiceRoll, new Pair(FreudsProtection * 100 + doubledice * 10 + dice, localDuration));
         n = !exit ? this.sourceid : applyto.getBuffSource(SecondaryStat.SelectDice);
         if (exit) {
            applyto.getMap().broadcastMessage(applyto, CField.EffectPacket.showDiceEffect(applyto, 0, n, -1, 1, false), false);
            applyto.getClient().getSession().writeAndFlush(CField.EffectPacket.showDiceEffect(applyto, 0, n, -1, 1, true));
         }

         if (this.isDoubleDice() && doubledice > 0) {
            applyto.getMap().broadcastMessage(applyto, CField.EffectPacket.showDiceEffect(applyto, 0, n, dice, -1, false), false);
            applyto.getClient().getSession().writeAndFlush(CField.EffectPacket.showDiceEffect(applyto, 0, n, dice, -1, true));
            applyto.getMap().broadcastMessage(applyto, CField.EffectPacket.showDiceEffect(applyto, 1, n, doubledice, -1, false), false);
            applyto.getClient().getSession().writeAndFlush(CField.EffectPacket.showDiceEffect(applyto, 1, n, doubledice, -1, true));
            if (dice == 1 && doubledice == 1) {
               applyto.dropMessage(5, "더블 럭키 다이스 스킬이 [" + dice + "], [" + doubledice + "]번이 나와 아무런 효과를 받지 못했습니다.");
               if (FreudsProtection == 1) {
                  return;
               }
            } else if (dice != 1) {
               if (doubledice != 1) {
                  applyto.dropMessage(5, "더블 럭키 다이스 스킬이 [" + dice + "], [" + doubledice + "]번 효과를 발동 시켰습니다.");
               } else {
                  applyto.dropMessage(5, "더블 럭키 다이스 스킬이 [" + dice + "]번 효과를 발동 시켰습니다.");
               }
            } else {
               applyto.dropMessage(5, "더블 럭키 다이스 스킬이 [" + doubledice + "]번 효과를 발동 시켰습니다.");
            }
         } else {
            applyto.getMap().broadcastMessage(applyto, CField.EffectPacket.showDiceEffect(applyto, 0, n, dice, -1, false), false);
            applyto.getClient().getSession().writeAndFlush(CField.EffectPacket.showDiceEffect(applyto, 0, n, dice, -1, true));
            if (dice != 1) {
               applyto.dropMessage(5, "럭키 다이스 스킬이 [" + dice + "]번 효과를 발동 시켰습니다.");
            } else {
               applyto.dropMessage(5, "럭키 다이스 스킬이 [" + dice + "]번이 나와 아무런 효과를 받지 못했습니다.");
               if (FreudsProtection == 1) {
                  return;
               }
            }
         }

         int[] arrn = new int[]{dice, doubledice};
         int i = 0;
         int[] var25 = arrn;
         int var26 = arrn.length;

         for(int var27 = 0; var27 < var26; ++var27) {
            int repeat2 = var25[var27];
            if (repeat2 != 1) {
               if (repeat2 <= 1) {
               }
            } else {
               applyto.dropMessage(5, "더블 럭키 다이스 스킬이 [" + repeat2 + "]번이 나와 아무런 효과를 받지 못했습니다.");
               ++i;
               if (i < 2) {
                  applyto.changeCooldown(this.sourceid, -this.getDuration() / 2);
               } else {
                  applyto.removeCooldown(this.sourceid);
               }
            }
         }

         if (applyto.getBuffedEffect(SecondaryStat.SelectDice) != null) {
            applyto.getMap().broadcastMessage(applyto, CField.EffectPacket.showDiceEffect(applyto, 1, n, FreudsProtection, 0, false), false);
            applyto.getClient().getSession().writeAndFlush(CField.EffectPacket.showDiceEffect(applyto, 1, n, FreudsProtection, 0, true));
         }

         applyto.getMap().broadcastMessage(applyto, CField.EffectPacket.showDiceEffect(applyto, 1, n, -1, 2, false), false);
         applyto.getClient().getSession().writeAndFlush(CField.EffectPacket.showDiceEffect(applyto, 1, n, -1, 2, true));
         break;
      case 5111017:
         bufftimeR = false;
         aftercancel = true;
         localDuration = 0;
         localstatups.put(SecondaryStat.SerpentStone, new Pair(applyto.서펜트스톤, localDuration));
         break;
      case 5120028:
         localstatups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(this.indieDamR), 10000));
         break;
      case 5121052:
         localDuration = 91000;
         localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
         break;
      case 5121054:
         localstatups.put(SecondaryStat.Stimulate, new Pair(1, this.duration));
         localstatups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(this.indieDamR), this.duration));
         break;
      case 5121055:
         bufftimeR = false;
         if (applyto.getUnityofPower() < 4) {
            applyto.setUnityofPower((byte)(applyto.getUnityofPower() + 1));
         }

         localstatups.put(SecondaryStat.UnityOfPower, new Pair(Integer.valueOf(applyto.getUnityofPower()), localDuration));
         break;
      case 5201012:
         localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
         break;
      case 5210015:
         localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
         break;
      case 5220019:
         localDuration = 120000;
         localstatups.put(SecondaryStat.SpiritLink, new Pair(5211019, localDuration));
         break;
      case 5220055:
         aftercancel = false;
         bufftimeR = false;
         localstatups.put(SecondaryStat.QuickDraw, new Pair(1, 0));
         break;
      case 5221022:
         localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
         break;
      case 5221029:
         localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
         break;
      case 5221054:
         localstatups.clear();
         localstatups.put(SecondaryStat.IgnoreMobDamR, new Pair(this.w, localDuration));
         applyto.addHP(applyto.getStat().getCurrentMaxHp() / 100L * (long)this.z);
         break;
      case 5310008:
         bufftimeR = false;
         localstatups.put(SecondaryStat.KeyDownTimeIgnore, new Pair(1, 15000));
         break;
      case 5311002:
         localstatups.put(SecondaryStat.HitCriDamR, new Pair(this.x, this.subTime));
         break;
      case 5311004:
         value = Randomizer.nextInt(4) + 1;
         applyto.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(applyto, 0, this.sourceid, 6, value, -1, (byte)(!applyto.isFacingLeft() ? 0 : 1), true, pos, (String)null, (Item)null));
         applyto.getMap().broadcastMessage(applyto, CField.EffectPacket.showEffect(applyto, 0, this.sourceid, 6, value, -1, (byte)(!applyto.isFacingLeft() ? 0 : 1), false, pos, (String)null, (Item)null), false);
         localstatups.put(SecondaryStat.Roulette, new Pair(value, localDuration));
         if (value == 2) {
            localstatups.put(SecondaryStat.IndieCD, new Pair(this.s, localDuration));
         }

         applyto.setSkillCustomInfo(this.sourceid, (long)value, 0L);
         break;
      case 5321054:
         localDuration = 0;
         localstatups.clear();
         if (GameConstants.isCannon(applyto.getJob()) && applyto.hasDonationSkill(5321054)) {
            localstatups.put(SecondaryStat.IndiePmdR, new Pair(-45, localDuration));
            localstatups.put(SecondaryStat.IndieBooster, new Pair(-1, localDuration));
            localstatups.put(SecondaryStat.Buckshot, new Pair(this.x, localDuration));
         } else if (!applyto.hasDonationSkill(5321054)) {
            localstatups.put(SecondaryStat.IndiePmdR, new Pair(-45, localDuration));
            localstatups.put(SecondaryStat.IndieBooster, new Pair(-1, localDuration));
            localstatups.put(SecondaryStat.Buckshot, new Pair(this.x, localDuration));
         } else {
            localstatups.put(SecondaryStat.IndiePmdR, new Pair(45, localDuration));
         }
         break;
      case 11001022:
         localstatups.put(SecondaryStat.ElementSoul, new Pair(Integer.valueOf(this.level), 0));
         break;
      case 11001024:
         asr = this.indieCr;
         if (applyto.getSkillLevel(11101031) > 0) {
            SecondaryStatEffect ef = SkillFactory.getSkill(11101031).getEffect(applyto.getSkillLevel(11101031));
            asr = 25;
         }

         localstatups.put(SecondaryStat.PoseType, new Pair(1, 0));
         localstatups.put(SecondaryStat.Buckshot, new Pair(1, 0));
         localstatups.put(SecondaryStat.IndieCr, new Pair(Integer.valueOf(asr), 0));
         if (applyto.getBuffedValue(11001025)) {
            applyto.cancelEffect(applyto.getBuffedEffect(11001025));
         }
         break;
      case 11001025:
         asr = this.indieBooster;
         pdd = this.indiePmdR;
         if (applyto.getSkillLevel(11101031) > 0) {
            ef = SkillFactory.getSkill(11101031).getEffect(applyto.getSkillLevel(11101031));
            pdd = 15;
         }

         localstatups.put(SecondaryStat.PoseType, new Pair(2, 0));
         localstatups.put(SecondaryStat.IndieBooster, new Pair(Integer.valueOf(asr), 0));
         localstatups.put(SecondaryStat.IndiePmdR, new Pair(Integer.valueOf(pdd), 0));
         if (applyto.getBuffedValue(11001024)) {
            applyto.cancelEffect(applyto.getBuffedEffect(11001024));
         }
         break;
      case 11001027:
         value = this.indiePad;
         up = localDuration;
         if (applyto.getCosmicCount() > 0) {
            ef = SkillFactory.getSkill(11001022).getEffect(applyto.getSkillLevel(11001022));
            value = ef.getU2();
            up = ef.getU();
         }

         localstatups.put(SecondaryStat.IndiePad, new Pair(value, up));
         localstatups.put(SecondaryStat.IndiePmdR, new Pair(Integer.valueOf(this.indiePmdR), localDuration));
         break;
      case 11001030:
         localstatups.put(SecondaryStat.CosmicOrb, new Pair(applyfrom.getCosmicCount(), localDuration));
         break;
      case 11101031:
         localstatups.clear();
         localstatups.put(SecondaryStat.GlimmeringTime, new Pair(1, 0));
         break;
      case 11111029:
         localstatups.clear();
         localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
         break;
      case 11121011:
         aftercancel = true;
         localstatups.clear();
         localstatups.put(SecondaryStat.PoseType, new Pair(2, 0));
         localstatups.put(SecondaryStat.Buckshot, new Pair(SkillFactory.getSkill(11001024).getEffect(applyto.getSkillLevel(11001024)).x, 0));
         break;
      case 11121012:
         aftercancel = true;
         localstatups.clear();
         localstatups.put(SecondaryStat.PoseType, new Pair(1, 0));
         localstatups.put(SecondaryStat.Buckshot, new Pair(SkillFactory.getSkill(11001024).getEffect(applyto.getSkillLevel(11001024)).x, 0));
         break;
      case 11121014:
         showEffect = false;
         break;
      case 11121157:
         bufftimeR = false;
         localstatups.put(SecondaryStat.NotDamaged, new Pair(1, 2000));
         break;
      case 12000022:
      case 12100026:
      case 12110024:
      case 12120007:
         localstatups.put(SecondaryStat.IndieMad, new Pair(this.x, localDuration));
         break;
      case 12111023:
         localstatups.clear();
         if (!primary) {
            bufftimeR = false;
            localstatups.put(SecondaryStat.NotDamaged, new Pair(1, this.x * 1000));
            localstatups.put(SecondaryStat.IgnorePCounter, new Pair(1, this.x * 1000));
         } else {
            localstatups.put(SecondaryStat.FlareTrick, new Pair(this.y, 2790000));
         }
         break;
      case 12120013:
      case 12120014:
         localstatups.clear();
         localstatups.put(SecondaryStat.IgnoreTargetDEF, new Pair(1, localDuration));
         break;
      case 12121005:
         localstatups.clear();
         if (!primary) {
            localstatups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(this.indieDamR), localDuration));
            localstatups.put(SecondaryStat.IndieBooster, new Pair(Integer.valueOf(this.indieBooster), localDuration));
         }
         break;
      case 12121052:
         localstatups.put(SecondaryStat.NotDamaged, new Pair(1, 3000));
         break;
      case 12121054:
         localstatups.put(SecondaryStat.PhoenixDrive, new Pair(1, localDuration / 2));
         applyto.addCooldown(12121054, System.currentTimeMillis(), (long)this.cooltime);
         applyto.getClient().getSession().writeAndFlush(CField.skillCooldown(12121054, this.cooltime));
      case 1301013:
         bufftimeR = false;
         localstatups.put(SecondaryStat.Beholder, new Pair(1, 0));
         if (applyfrom.getSkillLevel(1310013) <= 0) {
            applyfrom.setBeholderSkill1(1301013);
            break;
         } else {
            applyfrom.setBeholderSkill1(1310013);
         }
      case 13121017:
         if (!applyto.getBuffedValue(13121017)) {
            localstatups.put(SecondaryStat.StormBringer, new Pair(this.x, localDuration));
         }
         break;
      case 13001022:
         localstatups.put(SecondaryStat.CygnusElementSkill, new Pair(1, 0));
         localstatups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(this.indieDamR), 0));
         break;
      case 14001021:
         localstatups.put(SecondaryStat.ElementDarkness, new Pair(1, 0));
         break;
      case 14001031:
         localstatups.put(SecondaryStat.DarkSight, new Pair(Integer.valueOf(this.level), 2260));
         break;
      case 14110032:
         localstatups.put(SecondaryStat.ShadowMomentum, new Pair(applyfrom.getMomentumCount(), localDuration));
         break;
      case 14111030:
         localstatups.clear();
         if (!primary) {
            aftercancel = true;
            bufftimeR = false;
            localstatups.put(SecondaryStat.NotDamaged, new Pair(1, 7000));
            localstatups.put(SecondaryStat.IgnorePCounter, new Pair(1, 7000));
         } else {
            localstatups.put(SecondaryStat.ReviveOnce, new Pair(this.x, 0));
         }
         break;
      case 14120009:
         if (applyto.siphonVitality != this.x) {
            ++applyto.siphonVitality;
            localstatups.put(SecondaryStat.SiphonVitality, new Pair(applyto.siphonVitality, this.subTime));
         } else {
            localstatups.put(SecondaryStat.SiphonVitality, new Pair(applyto.siphonVitality, this.subTime));
            applyto.siphonVitality = 0;
            SkillFactory.getSkill(14120011).getEffect(this.level).applyTo(applyto);
         }
         break;
      case 14120011:
         if (applyto.getBuffedValue(SecondaryStat.Protective) == null) {
            applyto.setSkillCustomInfo(14120011, (long)SkillFactory.getSkill(14120009).getEffect(applyfrom.getSkillLevel(14120009)).y, 0L);
            if (applyto.getSkillLevel(14120049) > 0) {
               applyto.setSkillCustomInfo(14120011, applyto.getSkillCustomValue0(14120011) + (long)SkillFactory.getSkill(14120049).getEffect(1).getX(), 0L);
            }
         }

         localstatups.put(SecondaryStat.Protective, new Pair((int)applyto.getSkillCustomValue0(14120011), localDuration));
         if (applyto.getSkillLevel(14120050) > 0) {
            localstatups.put(SecondaryStat.IndiePad, new Pair(SkillFactory.getSkill(14120050).getEffect(1).getX(), localDuration));
         }

         if (applyto.getSkillLevel(14120051) > 0) {
            localstatups.put(SecondaryStat.IndieAsrR, new Pair(SkillFactory.getSkill(14120051).getEffect(1).getX(), localDuration));
         }
         break;
      case 14121004:
         localstatups.put(SecondaryStat.IndieStance, new Pair(100, this.x * 1000));
         break;
      case 14121052:
         bufftimeR = false;
         localstatups.clear();
         if (!primary) {
            localDuration = 30000;
            aftercancel = true;
            localstatups.put(SecondaryStat.IndiePmdR, new Pair(20, localDuration));
            localstatups.put(SecondaryStat.IndieCr, new Pair(100, localDuration));
            localstatups.put(SecondaryStat.IndieStance, new Pair(100, localDuration));
            localstatups.put(SecondaryStat.Dominion, new Pair(700, localDuration));
         } else {
            aftercancel = true;
            localstatups.put(SecondaryStat.NotDamaged, new Pair(1, 3500));
            localstatups.put(SecondaryStat.IgnorePCounter, new Pair(1, 3500));
            this.applyTo(applyto, false);
         }
         break;
      case 15001022:
         bufftimeR = false;
         localstatups.clear();
         if (!primary) {
            localstatups.put(SecondaryStat.IgnoreTargetDEF, new Pair((applyto.getSkillLevel(15121054) <= 0 ? this.x : 9) * applyto.lightning, this.y * 1000));
            localstatups.put(SecondaryStat.IndiePmdR, new Pair((applyto.getSkillLevel(15121054) <= 0 ? 0 : 5) * applyto.lightning, this.y * 1000));
         } else {
            localstatups.put(SecondaryStat.CygnusElementSkill, new Pair(1, 0));
         }
         break;
      case 15111022:
         if (applyto.lightning < 0) {
            applyto.lightning = 0;
         }

         localstatups.put(SecondaryStat.IndieDamR, new Pair(applyto.lightning * this.y, localDuration));
         break;
      case 15120003:
         if (applyto.lightning < 0) {
            applyto.lightning = 0;
         }

         localstatups.put(SecondaryStat.IndieDamR, new Pair(applyto.lightning * this.y, localDuration));
         break;
      case 15121052:
         localDuration = 2000;
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, localDuration));
         break;
      case 20031205:
         bufftimeR = false;
         applyto.getClient().getSession().writeAndFlush(CWvsContext.enableActions(applyto, true, false));
         localstatups.put(SecondaryStat.Invisible, new Pair(this.z * 100, localDuration));
         break;
      case 20031209:
      case 20031210:
         showEffect = false;
         bufftimeR = false;
         value = Randomizer.rand(1, this.sourceid != 20031210 ? 2 : 4);
         applyto.cancelEffect(this);
         if (value == 4) {
            ++value;
         }

         switch(value) {
         case 1:
            localstatups.put(SecondaryStat.Judgement, new Pair(value, localDuration));
            localstatups.put(SecondaryStat.IndieCr, new Pair(this.v, localDuration));
            break;
         case 2:
            localstatups.put(SecondaryStat.Judgement, new Pair(value, localDuration));
            localstatups.put(SecondaryStat.DropRIncrease, new Pair(this.w, localDuration));
            break;
         case 3:
            localstatups.put(SecondaryStat.Judgement, new Pair(value, localDuration));
            localstatups.put(SecondaryStat.IndieAsrR, new Pair(this.x, localDuration));
            localstatups.put(SecondaryStat.IndieTerR, new Pair(this.y, localDuration));
         case 4:
         default:
            break;
         case 5:
            localstatups.put(SecondaryStat.Judgement, new Pair(value, localDuration));
            localstatups.put(SecondaryStat.DrainHp, new Pair(this.z, localDuration));
         }

         applyto.setCardStack((byte)0);
         applyto.getClient().getSession().writeAndFlush(CField.updateCardStack(false, applyto.getCardStack()));
         applyto.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(applyto, 0, this.sourceid, 6, value - 1, -1, (byte)0, true, (Point)null, (String)null, (Item)null));
         MapleAtom atom = new MapleAtom(false, applyto.getId(), 1, true, applyto.getTotalSkillLevel(SkillFactory.getSkill(24120002)) <= 0 ? 24100003 : 24120002, applyto.getTruePosition().x, applyto.getTruePosition().y);

         for(i = 0; i < (this.sourceid != 24100003 ? 10 : 5); ++i) {
            atom.addForceAtom(new ForceAtom(2, Randomizer.rand(15, 29), Randomizer.rand(7, 11), Randomizer.rand(0, 9), 0));
         }

         applyto.getMap().spawnMapleAtom(atom);
         break;
      case 20040216:
      case 20040217:
         localstatups.put(SecondaryStat.Larkness, new Pair(applyto.getLuminusMorphUse(), localDuration));
         break;
      case 20040219:
      case 20040220:
         if (!primary) {
            bufftimeR = false;
         }

         if (!applyto.getLuminusMorph()) {
            applyto.setLuminusMorphUse(9999);
         } else {
            applyto.setLuminusMorphUse(1);
         }

         var29 = applyto.getCooldowns().iterator();

         while(true) {
            do {
               if (!var29.hasNext()) {
                  localstatups.clear();
                  if (!showEffect) {
                     localDuration = 10000 + SkillFactory.getSkill(27120008).getEffect(applyto.getSkillLevel(27120008)).duration;
                  }

                  localstatups.put(SecondaryStat.Larkness, new Pair(applyto.getLuminusMorphUse(), localDuration));
                  applyto.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.LuminusMorph(applyto.getLuminusMorphUse(), applyto.getLuminusMorph()));
                  break label3948;
               }

               i = (MapleCoolDownValueHolder)var29.next();
            } while(i.skillId != 27111303 && i.skillId != 27121303);

            applyto.removeCooldown(i.skillId);
            applyto.getClient().getSession().writeAndFlush(CField.skillCooldown(i.skillId, 0));
         }
      case 21000000:
         aftercancel = true;
         localstatups.put(SecondaryStat.AranCombo, new Pair(Integer.valueOf(applyto.getCombo()), 0));
         break;
      case 21100015:
      case 21120021:
         bufftimeR = false;
         break;
      case 21110016:
         applyto.setCombo((short)500);
         SkillFactory.getSkill(21000000).getEffect(applyfrom.getSkillLevel(21000000)).applyTo(applyfrom, false);
         localDuration = 15000;
         if (applyfrom.getSkillLevel(21120064) > 0) {
            localDuration += SkillFactory.getSkill(21120064).getEffect(1).getDuration();
         }

         localstatups.clear();
         localstatups.put(SecondaryStat.AdrenalinBoost, new Pair(150, localDuration));
         localstatups.put(SecondaryStat.AranBoostEndHunt, new Pair(1, localDuration));
         if (!primary) {
            applyto.setSkillCustomInfo(21110016, 0L, 0L);
         } else {
            applyto.setSkillCustomInfo(21110016, 1L, 0L);
         }
         break;
      case 21111030:
         localstatups.clear();
         if (applyto.getBuffedEffect(SecondaryStat.AdrenalinBoostActive) != null) {
            applyto.cancelEffect(this, (List)null, true);
            SkillFactory.getSkill(21110016).getEffect(applyto.getSkillLevel(21110016)).applyTo(applyto);
         } else {
            localstatups.put(SecondaryStat.AdrenalinBoostActive, new Pair(1, this.y * 1000));
         }
         break;
      case 21121017:
         applyto.cancelEffectFromBuffStat(SecondaryStat.BeyondNextAttackProb);
         break;
      case 21121058:
         bufftimeR = false;
         applyto.setCombo((short)500);
         localDuration = 15000;
         localstatups.clear();
         localstatups.put(SecondaryStat.AdrenalinBoost, new Pair(150, localDuration));
         localstatups.put(SecondaryStat.AranBoostEndHunt, new Pair(1, localDuration));
         if (!primary) {
            applyto.setSkillCustomInfo(21110016, 0L, 0L);
         } else {
            applyto.setSkillCustomInfo(21110016, 1L, 0L);
         }
         break;
      case 22110016:
         localstatups.clear();
         localstatups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(this.indieDamR), localDuration));
         break;
      case 22171080:
         bufftimeR = false;
         localstatups.put(SecondaryStat.NotDamaged, new Pair(1, 10000));
         localstatups.put(SecondaryStat.NewFlying, new Pair(1, 10000));
         localstatups.put(SecondaryStat.RideVehicleExpire, new Pair(1939007, 10000));
         break;
      case 23110004:
         bufftimeR = false;
         if (!primary) {
            if (!primary && applyto.getBuffedValue(400031017)) {
               localDuration = 0;
            } else if (!primary && !applyto.getBuffedValue(400031017)) {
               starttime = applyto.getBuffLimit(23110004);
               localDuration = (int)starttime + 100;
            }
         } else {
            localDuration = 15000;
         }

         if (localDuration > 15000) {
            localDuration = 15000;
         }

         localstatups.clear();
         localstatups.put(SecondaryStat.IgnisRore, new Pair((int)applyto.getSkillCustomValue0(23110005), localDuration));
         break;
      case 23111005:
         if (applyfrom.getSkillLevel(23120046) <= 0) {
            localstatups.put(SecondaryStat.DamAbsorbShield, new Pair(this.x, localDuration));
         } else {
            localstatups.put(SecondaryStat.DamAbsorbShield, new Pair(this.x + SkillFactory.getSkill(23120046).getEffect(1).getX(), localDuration));
         }

         if (applyfrom.getSkillLevel(23120047) <= 0) {
            localstatups.put(SecondaryStat.Asr, new Pair(Integer.valueOf(this.asrR), localDuration));
         } else {
            localstatups.put(SecondaryStat.Asr, new Pair(this.asrR + SkillFactory.getSkill(23120047).getEffect(1).getX(), localDuration));
         }

         if (applyfrom.getSkillLevel(23120048) <= 0) {
            localstatups.put(SecondaryStat.Ter, new Pair(Integer.valueOf(this.terR), localDuration));
         } else {
            localstatups.put(SecondaryStat.Ter, new Pair(this.terR + SkillFactory.getSkill(23120048).getEffect(1).getX(), localDuration));
         }
         break;
      case 24111002:
         localstatups.clear();
         if (!primary) {
            bufftimeR = false;
            applyto.getStat().heal(applyto);
            localstatups.put(SecondaryStat.IgnorePCounter, new Pair(1, 4000));
            localstatups.put(SecondaryStat.NotDamaged, new Pair(1, 4000));
         } else {
            aftercancel = true;
            localstatups.put(SecondaryStat.ReviveOnce, new Pair(this.x, 0));
         }
         break;
      case 24111003:
         if (applyfrom.getSkillLevel(24120049) <= 0) {
            localstatups.put(SecondaryStat.IndieAsrR, new Pair(this.x, localDuration));
            localstatups.put(SecondaryStat.IndieTerR, new Pair(this.y, localDuration));
         } else {
            localstatups.put(SecondaryStat.IndieAsrR, new Pair(this.x + 5, localDuration));
            localstatups.put(SecondaryStat.IndieTerR, new Pair(this.y + SkillFactory.getSkill(24120049).getEffect(1).getX(), localDuration));
         }

         if (applyfrom.getSkillLevel(24120050) <= 0) {
            localstatups.put(SecondaryStat.IndieHpR, new Pair(Integer.valueOf(this.indieMhpR), localDuration));
         } else {
            localstatups.put(SecondaryStat.IndieHpR, new Pair(this.indieMhpR + SkillFactory.getSkill(24120050).getEffect(1).getX(), localDuration));
         }

         if (applyfrom.getSkillLevel(24120051) <= 0) {
            localstatups.put(SecondaryStat.IndieMpR, new Pair(Integer.valueOf(this.indieMmpR), localDuration));
         } else {
            localstatups.put(SecondaryStat.IndieMpR, new Pair(this.indieMmpR + SkillFactory.getSkill(24120051).getEffect(1).getX(), localDuration));
         }
         break;
      case 25111209:
         bufftimeR = false;
         localstatups.clear();
         if (!primary) {
            applyto.cancelEffect(this);
            localstatups.put(SecondaryStat.IgnorePCounter, new Pair(1, 1000));
            localstatups.put(SecondaryStat.NotDamaged, new Pair(1, 1000));
         } else {
            showEffect = false;
            aftercancel = true;
            localstatups.put(SecondaryStat.ReviveOnce, new Pair(100, 0));
         }
         break;
      case 25121209:
         applyto.setSpiritGuard(3);
         localstatups.put(SecondaryStat.SpiritGuard, new Pair(3, localDuration));
         break;
      case 27111004:
         localstatups.put(SecondaryStat.AntiMagicShell, new Pair(3, 0));
         aftercancel = true;
         applyto.setAntiMagicShell((byte)3);
         break;
      case 27120005:
         if (applyto.stackbuff == 0) {
            ++applyto.stackbuff;
         }

         localstatups.put(SecondaryStat.StackBuff, new Pair(applyto.stackbuff * this.damR, localDuration));
         break;
      case 27121054:
         bufftimeR = false;
         if (!applyto.getBuffedValue(20040219) && !applyto.getBuffedValue(20040220)) {
            if (!applyto.getBuffedValue(20040216)) {
               SkillFactory.getSkill(20040219).getEffect(1).applyTo(applyto, false);
               applyto.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.LuminusMorph(applyto.getLuminusMorphUse(), applyto.getLuminusMorph()));
            } else {
               SkillFactory.getSkill(20040220).getEffect(1).applyTo(applyto, false);
               applyto.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.LuminusMorph(applyto.getLuminusMorphUse(), applyto.getLuminusMorph()));
            }
         } else {
            applyto.dropMessage(5, "이퀄리브리엄 상태에서는 사용하실 수 없습니다.");
            applyto.getClient().getSession().writeAndFlush(CWvsContext.enableActions(applyto));
            applyto.removeCooldown(27121054);
         }

         return;
      case 30000227:
         localstatups.clear();
         localstatups.put(SecondaryStat.HiddenPieceOn, new Pair((int)(applyto.getKeyValue(19752, "hiddenpiece") > 0L ? applyto.getKeyValue(19752, "hiddenpiece") : 0L), 0));
         break;
      case 30001080:
         showEffect = false;
         localstatups.clear();
         localstatups.put(SecondaryStat.IndieFloating, new Pair(1, localDuration));
         localstatups.put(SecondaryStat.NewFlying, new Pair(1, localDuration));
         break;
      case 30010230:
         aftercancel = true;
         localstatups.put(SecondaryStat.OverloadCount, new Pair(applyto.getExceed(), 0));
         break;
      case 30021237:
         bufftimeR = false;
         localstatups.put(SecondaryStat.NewFlying, new Pair(1, this.x * 1000));
         break;
      case 31011001:
         type = false;
         up = applyto.getSkillLevel(31220044) <= 0 ? 20 : 18;
         i = (int)Math.floor((double)applyto.getExceed() / (double)up * 100.0D);
         double d = applyto.getSkillLevel(31210006) <= 0 ? 0.625D : 1.25D;
         value = (int)Math.floor((double)applyto.getExceed() * d);
         if (up == applyto.getExceed()) {
            i = 100;
            value = applyto.getSkillLevel(31210006) <= 0 ? 15 : 25;
         }

         applyto.setExceed((short)0);
         applyto.cancelEffectFromBuffStat(SecondaryStat.OverloadCount);
         HashMap<SecondaryStat, Pair<Integer, Integer>> cancelList = new HashMap();
         cancelList.put(SecondaryStat.ExceedOverload, new Pair(1, 0));
         applyto.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.cancelBuff(cancelList, applyto));
         long healhp = applyto.getStat().getCurrentMaxHp() / 100L * (long)i;
         if (applyto.getBuffedEffect(SecondaryStat.DemonFrenzy) != null) {
            healhp = healhp / 100L * (long)this.y;
         }

         applyto.setSkillCustomInfo(30010232, 0L, 0L);
         applyto.addHP(healhp, applyto.getBuffedEffect(SecondaryStat.DemonFrenzy) != null, false);
         localstatups.put(SecondaryStat.IndiePmdR, new Pair(value, localDuration));
         break;
      case 31111003:
         bufftimeR = false;
         localstatups.put(SecondaryStat.NotDamaged, new Pair(1, 2000));
         localstatups.put(SecondaryStat.IgnorePCounter, new Pair(1, 2000));
         break;
      case 31120046:
         localstatups.put(SecondaryStat.IgnorePCounter, new Pair(1, localDuration * this.y / 100));
         localstatups.put(SecondaryStat.IgnorePImmune, new Pair(1, localDuration * this.y / 100));
         break;
      case 31121005:
         localstatups.put(SecondaryStat.IndieHpR, new Pair(Integer.valueOf(this.getIndieMhpR()), localDuration));
         localstatups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(this.indieDamR), localDuration));
         localstatups.put(SecondaryStat.DevilishPower, new Pair(this.damage / 10, localDuration));
         localstatups.put(SecondaryStat.NotDamaged, new Pair(1, 3000));
         localstatups.put(SecondaryStat.IgnoreAllImmune, new Pair(1, 3000));
         if (applyfrom.getSkillLevel(31120046) > 0) {
            SkillFactory.getSkill(31120046).getEffect(1).applyTo(applyfrom, localDuration);
         }
         break;
      case 31211003:
         if (applyfrom.getSkillLevel(31220046) <= 0) {
            localstatups.put(SecondaryStat.DamAbsorbShield, new Pair(this.x, localDuration));
         } else {
            localstatups.put(SecondaryStat.DamAbsorbShield, new Pair(this.x + SkillFactory.getSkill(31220046).getEffect(1).getX(), localDuration));
         }

         if (applyfrom.getSkillLevel(31220047) <= 0) {
            localstatups.put(SecondaryStat.Asr, new Pair(this.y, localDuration));
         } else {
            localstatups.put(SecondaryStat.Asr, new Pair(this.y + SkillFactory.getSkill(31220047).getEffect(1).getX(), localDuration));
         }

         if (applyfrom.getSkillLevel(31220048) <= 0) {
            localstatups.put(SecondaryStat.Ter, new Pair(this.z, localDuration));
         } else {
            localstatups.put(SecondaryStat.Ter, new Pair(this.z + SkillFactory.getSkill(31220048).getEffect(1).getX(), localDuration));
         }
         break;
      case 32001014:
      case 32100010:
      case 32110017:
      case 32120019:
         aftercancel = true;
         localstatups.put(SecondaryStat.BMageDeath, new Pair(Integer.valueOf(applyto.getDeath()), localDuration));
         break;
      case 32001016:
         localDuration = 0;
         applyto.setSkillCustomInfo(this.sourceid, (long)applyfrom.getId(), 0L);
         localstatups.put(SecondaryStat.YellowAura, new Pair(Integer.valueOf(this.level), 0));
         localstatups.put(SecondaryStat.IndieBooster, new Pair(-1, localDuration));
         localstatups.put(SecondaryStat.IndieSpeed, new Pair(Integer.valueOf(this.indieSpeed), localDuration));
         if (primary) {
            if (applyto.getBuffedValue(32101009) && applyto.getBuffedOwner(32101009) == applyto.getId()) {
               applyto.cancelEffectFromBuffStat(SecondaryStat.DrainAura, 32101009);
            }

            if (applyto.getBuffedValue(32111012) && applyto.getBuffedOwner(32111012) == applyto.getId()) {
               applyto.cancelEffectFromBuffStat(SecondaryStat.BlueAura, 32111012);
            }

            if (applyto.getBuffedValue(32121017) && applyto.getBuffedOwner(32121017) == applyto.getId()) {
               applyto.cancelEffectFromBuffStat(SecondaryStat.DarkAura, 32121017);
            }

            if (applyto.getBuffedValue(32121018) && applyto.getBuffedOwner(32121018) == applyto.getId()) {
               applyto.cancelEffectFromBuffStat(SecondaryStat.DebuffAura, 32121018);
            }

            if (applyto.getBuffedValue(400021006) && applyto.getBuffedOwner(400021006) == applyto.getId()) {
               applyto.cancelEffectFromBuffStat(SecondaryStat.UnionAura, 400021006);
            }
         }
         break;
      case 32101009:
         applyto.setSkillCustomInfo(this.sourceid, (long)applyfrom.getId(), 0L);
         localstatups.put(SecondaryStat.DrainAura, new Pair(Integer.valueOf(this.level), 0));
         if (primary) {
            if (applyto.getBuffedValue(32001016) && applyto.getBuffedOwner(32001016) == applyto.getId()) {
               applyto.cancelEffectFromBuffStat(SecondaryStat.YellowAura, 32001016);
            }

            if (applyto.getBuffedValue(32111012) && applyto.getBuffedOwner(32111012) == applyto.getId()) {
               applyto.cancelEffectFromBuffStat(SecondaryStat.BlueAura, 32111012);
            }

            if (applyto.getBuffedValue(32121017) && applyto.getBuffedOwner(32121017) == applyto.getId()) {
               applyto.cancelEffectFromBuffStat(SecondaryStat.DarkAura, 32121017);
            }

            if (applyto.getBuffedValue(32121018) && applyto.getBuffedOwner(32121018) == applyto.getId()) {
               applyto.cancelEffectFromBuffStat(SecondaryStat.DebuffAura, 32121018);
            }

            if (applyto.getBuffedValue(400021006) && applyto.getBuffedOwner(400021006) == applyto.getId()) {
               applyto.cancelEffectFromBuffStat(SecondaryStat.UnionAura, 400021006);
            }
         }
         break;
      case 32111012:
         applyto.setSkillCustomInfo(this.sourceid, (long)applyfrom.getId(), 0L);
         localDuration = 0;
         localstatups.put(SecondaryStat.BlueAura, new Pair(Integer.valueOf(this.level), 0));
         localstatups.put(SecondaryStat.IndieAsrR, new Pair(Integer.valueOf(this.indieAsrR), localDuration));
         if (primary) {
            if (applyto.getBuffedValue(32001016) && applyto.getBuffedOwner(32001016) == applyto.getId()) {
               applyto.cancelEffectFromBuffStat(SecondaryStat.YellowAura, 32001016);
            }

            if (applyto.getBuffedValue(32101009) && applyto.getBuffedOwner(32101009) == applyto.getId()) {
               applyto.cancelEffectFromBuffStat(SecondaryStat.DrainAura, 32101009);
            }

            if (applyto.getBuffedValue(32121017) && applyto.getBuffedOwner(32121017) == applyto.getId()) {
               applyto.cancelEffectFromBuffStat(SecondaryStat.DarkAura, 32121017);
            }

            if (applyto.getBuffedValue(32121018) && applyto.getBuffedOwner(32121018) == applyto.getId()) {
               applyto.cancelEffectFromBuffStat(SecondaryStat.DebuffAura, 32121018);
            }

            if (applyto.getBuffedValue(400021006) && applyto.getBuffedOwner(400021006) == applyto.getId()) {
               applyto.cancelEffectFromBuffStat(SecondaryStat.UnionAura, 400021006);
            }
         }
         break;
      case 32111016:
         localDuration = 0;
         localstatups.put(SecondaryStat.DarkLighting, new Pair(1, localDuration));
         break;
      case 32120045:
         if (applyfrom.getBuffedValue(SecondaryStat.TeleportMastery) != null) {
            applyfrom.cancelEffectFromBuffStat(SecondaryStat.TeleportMastery);
         }

         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, this.w * 1000));
         break;
      case 32121017:
         applyto.setSkillCustomInfo(this.sourceid, (long)applyfrom.getId(), 0L);
         localDuration = 0;
         localstatups.put(SecondaryStat.DarkAura, new Pair(Integer.valueOf(this.level), 0));
         localstatups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(this.indieDamR), localDuration));
         if (applyfrom.getId() == applyto.getId() && applyto.getSkillLevel(32120060) > 0) {
            localstatups.put(SecondaryStat.IndieBDR, new Pair(Integer.valueOf(SkillFactory.getSkill(32120060).getEffect(1).indieBDR), localDuration));
         }

         if (applyto.addBuffCheck == 1) {
            applyto.setSkillCustomInfo(this.sourceid, (long)applyfrom.getId(), 0L);
            localDuration = 0;
            localstatups.put(SecondaryStat.DarkAura, new Pair(Integer.valueOf(this.level), 0));
            localstatups.put(SecondaryStat.IndieDamR, new Pair(20, localDuration));
            localstatups.put(SecondaryStat.IndieBDR, new Pair(20, localDuration));
         }

         if (primary) {
            if (applyto.getBuffedValue(32001016) && applyto.getBuffedOwner(32001016) == applyto.getId()) {
               applyto.cancelEffectFromBuffStat(SecondaryStat.YellowAura, 32001016);
            }

            if (applyto.getBuffedValue(32101009) && applyto.getBuffedOwner(32101009) == applyto.getId()) {
               applyto.cancelEffectFromBuffStat(SecondaryStat.DrainAura, 32101009);
            }

            if (applyto.getBuffedValue(32111012) && applyto.getBuffedOwner(32111012) == applyto.getId()) {
               applyto.cancelEffectFromBuffStat(SecondaryStat.BlueAura, 32111012);
            }

            if (applyto.getBuffedValue(32121018) && applyto.getBuffedOwner(32121018) == applyto.getId()) {
               applyto.cancelEffectFromBuffStat(SecondaryStat.DebuffAura, 32121018);
            }

            if (applyto.getBuffedValue(400021006) && applyto.getBuffedOwner(400021006) == applyto.getId()) {
               applyto.cancelEffectFromBuffStat(SecondaryStat.UnionAura, 400021006);
            }
         }
         break;
      case 32121018:
         localstatups.put(SecondaryStat.DebuffAura, new Pair(Integer.valueOf(this.level), localDuration));
         if (primary) {
            if (applyto.getBuffedValue(32001016) && applyto.getBuffedOwner(32001016) == applyto.getId()) {
               applyto.cancelEffectFromBuffStat(SecondaryStat.YellowAura, 32001016);
            }

            if (applyto.getBuffedValue(32101009) && applyto.getBuffedOwner(32101009) == applyto.getId()) {
               applyto.cancelEffectFromBuffStat(SecondaryStat.DrainAura, 32101009);
            }

            if (applyto.getBuffedValue(32121017) && applyto.getBuffedOwner(32121017) == applyto.getId()) {
               applyto.cancelEffectFromBuffStat(SecondaryStat.DarkAura, 32121017);
            }

            if (applyto.getBuffedValue(32111012) && applyto.getBuffedOwner(32111012) == applyto.getId()) {
               applyto.cancelEffectFromBuffStat(SecondaryStat.BlueAura, 32111012);
            }

            if (applyto.getBuffedValue(400021006) && applyto.getBuffedOwner(400021006) == applyto.getId()) {
               applyto.cancelEffectFromBuffStat(SecondaryStat.UnionAura, 400021006);
            }
         }
         break;
      case 33001001:
         if (!primary) {
            localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(this.x, 10000));
         }

         applyto.cancelEffectFromBuffStat(SecondaryStat.JaguarSummoned);
         break;
      case 33110014:
         aftercancel = true;
         value = 0;
         String[] var79 = applyto.getInfoQuest(23008).split(";");
         i = var79.length;

         for(FreudsProtection = 0; FreudsProtection < i; ++FreudsProtection) {
            String str = var79[FreudsProtection];
            if (str.contains("=1")) {
               ++value;
            }
         }

         localstatups.put(SecondaryStat.JaguarCount, new Pair(value * (this.y << 8) + this.z * value, 0));
         break;
      case 33111007:
         localstatups.put(SecondaryStat.NotDamaged, new Pair(1, 2000));
         localstatups.put(SecondaryStat.Speed, new Pair(this.x, localDuration));
         localstatups.put(SecondaryStat.BeastFormDamage, new Pair(1, localDuration));
         if (applyfrom.getSkillLevel(33120044) > 0) {
            localstatups.put(SecondaryStat.IndieHpR, new Pair(Integer.valueOf(SkillFactory.getSkill(33120044).getEffect(1).mhpR), localDuration));
         }

         if (applyfrom.getSkillLevel(33120045) <= 0) {
            localstatups.put(SecondaryStat.IndieBooster, new Pair(-this.w, localDuration));
         } else {
            localstatups.put(SecondaryStat.IndieBooster, new Pair(-this.w - SkillFactory.getSkill(33120045).getEffect(1).getW(), localDuration));
         }
         break;
      case 35001002:
         localstatups.clear();
         asr = this.epad;
         pdd = this.epdd;
         if (applyfrom.getSkillLevel(35120000) > 0) {
            asr = SkillFactory.getSkill(35120000).getEffect(applyto.getSkillLevel(35120000)).epad;
            pdd = SkillFactory.getSkill(35120000).getEffect(applyto.getSkillLevel(35120000)).epdd;
         }

         localstatups.put(SecondaryStat.EnhancedPad, new Pair(Integer.valueOf(asr), localDuration));
         localstatups.put(SecondaryStat.EnhancedPdd, new Pair(Integer.valueOf(pdd), localDuration));
         localstatups.put(SecondaryStat.IndieSpeed, new Pair(30, localDuration));
         localstatups.put(SecondaryStat.IndieBooster, new Pair(-1, localDuration));
         localstatups.put(SecondaryStat.Mechanic, new Pair(30, localDuration));
         if (applyto.getBuffedValue(35111003)) {
            applyto.cancelEffect(SkillFactory.getSkill(35111003).getEffect(1), (List)null, true);
         }
         break;
      case 35111002:
         bufftimeR = false;
         if (applyto.getCooldownLimit(35111002) > 0L) {
            localDuration = 70000;
            localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
         }
         break;
      case 35111003:
         localstatups.clear();
         asr = this.epad;
         pdd = this.epdd;
         if (applyfrom.getSkillLevel(35120000) > 0) {
            asr = SkillFactory.getSkill(35120000).getEffect(applyto.getSkillLevel(35120000)).epad;
            pdd = SkillFactory.getSkill(35120000).getEffect(applyto.getSkillLevel(35120000)).epdd;
         }

         localstatups.put(SecondaryStat.EnhancedPad, new Pair(Integer.valueOf(asr), localDuration));
         localstatups.put(SecondaryStat.EnhancedPdd, new Pair(Integer.valueOf(pdd), localDuration));
         localstatups.put(SecondaryStat.IndieSpeed, new Pair(30, localDuration));
         localstatups.put(SecondaryStat.CriticalIncrease, new Pair(this.cr, localDuration));
         localstatups.put(SecondaryStat.IndieBooster, new Pair(-1, localDuration));
         localstatups.put(SecondaryStat.Mechanic, new Pair(30, localDuration));
         if (applyto.getBuffedValue(35001002)) {
            applyto.cancelEffect(SkillFactory.getSkill(35001002).getEffect(1), (List)null, true);
         }
         break;
      case 35120002:
         localstatups.clear();
         if (!primary) {
            localstatups.put(SecondaryStat.IndiePmdR, new Pair(this.z, localDuration));
         } else {
            localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
         }
         break;
      case 35121003:
         localDuration = 3000;
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, localDuration));
         break;
      case 36111003:
         applyto.stackbuff = primary ? this.x : --applyto.stackbuff;
         localstatups.put(SecondaryStat.DamAbsorbShield, new Pair(this.z, !primary ? (int)applyto.getBuffLimit(this.sourceid) : this.duration));
         localstatups.put(SecondaryStat.StackBuff, new Pair(this.x, !primary ? (int)applyto.getBuffLimit(this.sourceid) : this.duration));
         break;
      case 36121014:
         if (!primary) {
            aftercancel = false;
            localstatups.clear();
            localstatups.put(SecondaryStat.IndieHpR, new Pair(Integer.valueOf(this.indieMhpR), 3000));
         }
         break;
      case 36121052:
         localstatups.clear();
         bufftimeR = false;
         localstatups.put(SecondaryStat.IgnorePCounter, new Pair(1, 2000));
         localstatups.put(SecondaryStat.NotDamaged, new Pair(1, 2000));
         localstatups.put(SecondaryStat.IndieDamR, new Pair(this.w, this.y * 1000));
         break;
      case 36121054:
         bufftimeR = false;
         if (applyto.getXenonSurplus() < 20) {
            applyto.setXenonSurplus((short)20, SkillFactory.getSkill(this.sourceid));
            applyto.updateXenonSurplus((short)20, SkillFactory.getSkill(this.sourceid));
         }

         localstatups.put(SecondaryStat.AmaranthGenerator, new Pair(1, localDuration));
         break;
      case 37000006:
         aftercancel = true;
         localstatups.clear();
         localstatups.put(SecondaryStat.RwBarrier, new Pair((int)applyto.getSkillCustomValue0(37000006), 0));
         localDuration = 0;
         break;
      case 37000011:
      case 37000012:
      case 37000013:
      case 37001002:
         localstatups.clear();
         if (applyto.getBuffedValue(SecondaryStat.RWMaximizeCannon) != null) {
            localDuration = 1000;
         }

         localstatups.put(SecondaryStat.RWOverHeat, new Pair(1, localDuration));
         bufftimeR = false;
         break;
      case 37100002:
      case 37110004:
         bufftimeR = false;
         localstatups.put(SecondaryStat.RWMovingEvar, new Pair(this.x, 1500));
         break;
      case 37110009:
         if (applyto.getBuffedValue(SecondaryStat.RWCombination) == null) {
            applyto.combinationBuff = 0;
         }

         if (applyto.combinationBuff < this.x) {
            ++applyto.combinationBuff;
         }

         localstatups.put(SecondaryStat.RWCombination, new Pair(applyto.combinationBuff, localDuration));
         if (applyto.combinationBuff >= this.z) {
            localstatups.put(SecondaryStat.IndieBooster, new Pair(-1, localDuration));
         }
         break;
      case 37120012:
         if (applyto.getBuffedValue(SecondaryStat.RWCombination) == null && primary) {
            applyto.combinationBuff = 0;
         }

         if (applyto.combinationBuff < this.x) {
            ++applyto.combinationBuff;
         }

         if (applyto.combinationBuff == 15) {
            applyto.combinationBuff = 10;
            localDuration = 0;
         }

         localstatups.put(SecondaryStat.IndieCr, new Pair(this.q * applyto.combinationBuff, localDuration));
         localstatups.put(SecondaryStat.RWCombination, new Pair(applyto.combinationBuff, localDuration));
         if (applyto.combinationBuff >= this.z) {
            localstatups.put(SecondaryStat.IndieBooster, new Pair(-1, localDuration));
         }
         break;
      case 37120059:
         localstatups.put(SecondaryStat.RwMagnumBlow, new Pair(4, localDuration));
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 2000));
         break;
      case 37121004:
         localstatups.clear();
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 0));
         break;
      case 37121005:
         localstatups.put(SecondaryStat.RWBarrierHeal, new Pair(1, localDuration));
         break;
      case 37121052:
         aftercancel = true;
         localstatups.clear();
         localstatups.put(SecondaryStat.RwMagnumBlow, new Pair((int)applyto.getSkillCustomValue0(37121052), 0));
         bufftimeR = false;
         break;
      case 37121055:
         localstatups.put(SecondaryStat.RwMagnumBlow, new Pair(1, localDuration));
         break;
      case 37121056:
         localstatups.put(SecondaryStat.RwMagnumBlow, new Pair(2, localDuration));
         break;
      case 37121057:
         localstatups.put(SecondaryStat.RwMagnumBlow, new Pair(3, localDuration));
         break;
      case 37121058:
         localstatups.put(SecondaryStat.RwMagnumBlow, new Pair(4, localDuration));
         break;
      case 50001214:
         localstatups.clear();
         if (applyto.getBuffedValue(50001214)) {
            applyto.addSkillCustomInfo(50001214, -1L);
         } else {
            applyto.setSkillCustomInfo(50001214, (long)this.y, 0L);
         }

         if (applyto.getSkillCustomValue0(50001214) > 0L) {
            localstatups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(this.indieDamR), localDuration));
            localstatups.put(SecondaryStat.MichaelProtectofLight, new Pair((int)applyto.getSkillCustomValue0(50001214), localDuration));
         } else {
            applyto.removeSkillCustomInfo(50001214);
            applyto.cancelEffect(this);
         }
         break;
      case 51001005:
         pad2 = 0;
         if (applyto.getRoyalStack() <= 0 || applyto.getRoyalStack() > 5) {
            applyto.setRoyalStack((byte)1);
         }

         switch(applyto.getRoyalStack()) {
         case 1:
            pad2 = 10;
            break;
         case 2:
            pad2 = 15;
            break;
         case 3:
            pad2 = 20;
            break;
         case 4:
            pad2 = 30;
            break;
         case 5:
            pad2 = 45;
         }

         bufftimeR = false;
         if (primary) {
            localDuration = this.x * 1000;
         }

         localstatups.clear();
         localstatups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(pad2), localDuration));
         localstatups.put(SecondaryStat.RoyalGuardState, new Pair(Integer.valueOf(applyto.getRoyalStack()), localDuration));
         if (primary) {
            SkillFactory.getSkill(51001006).getEffect(1).applyTo(applyto, false);
         }
         break;
      case 51001006:
         bufftimeR = false;
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 5000));
         break;
      case 51001007:
      case 51001008:
      case 51001009:
      case 51001010:
         bufftimeR = false;
         aftercancel = true;
         value = applyto.getRoyalStack() == 5 ? 150 : (applyto.getRoyalStack() == 4 ? 1230 : (applyto.getRoyalStack() == 3 ? 1280 : (applyto.getRoyalStack() == 2 ? 1330 : 1420)));
         if (applyto.getBuffedValue(400011083)) {
            value += 500;
         }

         if (applyto.getBuffedValue(51121054)) {
            value += 500;
         }

         localstatups.put(SecondaryStat.RoyalGuardPrepare, new Pair(1, value));
         break;
      case 51001011:
      case 51001012:
      case 51001013:
         bufftimeR = false;
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 4000));
         break;
      case 51110009:
         value = 0;
         switch(applyto.getRoyalStack()) {
         case 4:
            value = this.getW2();
            break;
         case 5:
            value = this.v;
         }

         bufftimeR = false;
         localstatups.put(SecondaryStat.IndiePad, new Pair(value, localDuration));
         localstatups.put(SecondaryStat.RoyalGuardState, new Pair(Integer.valueOf(applyto.getRoyalStack()), localDuration));
         break;
      case 51111004:
         value = this.y;
         up = this.z;
         if (!primary) {
            value = (int)((double)value * 0.2D);
            up = (int)((double)up * 0.2D);
         }

         i = this.x;
         if (applyfrom.getSkillLevel(51120044) > 0 && primary) {
            i += SkillFactory.getSkill(51120044).getEffect(1).getX();
         }

         if (!primary) {
            i = (int)((double)i * 0.3D);
         }

         if (applyfrom.getSkillLevel(51120043) > 0) {
            localDuration += SkillFactory.getSkill(51120043).getEffect(1).getDuration();
         }

         if (applyfrom.getSkillLevel(51120045) > 0) {
            FreudsProtection = SkillFactory.getSkill(51120045).getEffect(1).getY();
            up += FreudsProtection;
         }

         localstatups.put(SecondaryStat.Ter, new Pair(up, localDuration));
         localstatups.put(SecondaryStat.Asr, new Pair(value, localDuration));
         localstatups.put(SecondaryStat.IncDefenseR, new Pair(i, localDuration));
         break;
      case 51111008:
         localDuration = 0;
         pad2 = 0;
         localstatups.clear();
         if (applyfrom.getId() != applyto.getId()) {
            aftercancel = true;
            MapleCharacter leader = null;
            var38 = applyto.getMap().getAllChracater().iterator();

            while(var38.hasNext()) {
               MapleCharacter mapleCharacter = (MapleCharacter)var38.next();
               if (mapleCharacter.getId() == applyfrom.getId()) {
                  leader = mapleCharacter;
                  break;
               }
            }

            if (leader != null) {
               if (leader.getBuffedValue(51111004)) {
                  localstatups.put(SecondaryStat.Asr, new Pair(20, 0));
                  localstatups.put(SecondaryStat.Ter, new Pair(20, 0));
                  localstatups.put(SecondaryStat.IncDefenseR, new Pair(30, 0));
               }

               if (leader.getBuffedValue(51001005)) {
                  switch(leader.getRoyalStack()) {
                  case 1:
                     pad2 = 5;
                     break;
                  case 2:
                     pad2 = 8;
                     break;
                  case 3:
                     pad2 = 10;
                     break;
                  case 4:
                     pad2 = 15;
                     break;
                  case 5:
                     pad2 = 23;
                  }

                  localstatups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(pad2), 0));
               }
            }
         } else {
            up = 1;
            if (applyto.getParty() != null) {
               var38 = applyto.getParty().getMembers().iterator();

               label3888:
               while(true) {
                  MaplePartyCharacter maplePartyCharacter;
                  do {
                     do {
                        if (!var38.hasNext()) {
                           applyfrom.setSkillCustomInfo(51111009, (long)up, 0L);
                           break label3888;
                        }

                        maplePartyCharacter = (MaplePartyCharacter)var38.next();
                     } while(!maplePartyCharacter.isOnline());
                  } while(!(affected = applyfrom.getClient().getChannelServer().getPlayerStorage().getCharacterByName(maplePartyCharacter.getName())).getBuffedValue(51111008) && (applyfrom.getTruePosition().x + this.getLt().x >= affected.getTruePosition().x || applyfrom.getTruePosition().x - this.getLt().x <= affected.getTruePosition().x || applyfrom.getTruePosition().y + this.getLt().y >= affected.getTruePosition().y || applyfrom.getTruePosition().y - this.getLt().y <= affected.getTruePosition().y));

                  ++up;
               }
            }

            localstatups.put(SecondaryStat.IndieDamR, new Pair(this.indieDamR * (applyto.getParty() != null ? up : 1), 0));
         }

         localstatups.put(SecondaryStat.MichaelSoulLink, new Pair(1, 0));
         break;
      case 51121054:
         localstatups.clear();
         localstatups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(this.indieDamR), localDuration));
         localstatups.put(SecondaryStat.DamAbsorbShield, new Pair(this.x, localDuration));
         break;
      case 60001216:
      case 60001217:
         localstatups.clear();
         localstatups.put(SecondaryStat.ReshuffleSwitch, new Pair(0, 0));

         while(true) {
            if (applyfrom.getBuffedEffect(SecondaryStat.ReshuffleSwitch) == null) {
               break label3948;
            }

            applyfrom.cancelEffect(applyfrom.getBuffedEffect(SecondaryStat.ReshuffleSwitch));
         }
      case 60030241:
      case 80003015:
         localstatups.clear();
         localstatups.put(SecondaryStat.IndieDamR, new Pair(this.y, localDuration));
         break;
      case 61111008:
      case 61120008:
      case 61121053:
         if (this.sourceid == 61121053) {
            SkillFactory.getSkill(61111008).getEffect(1).applyKaiserCombo(applyto, (short)1000);
            bufftimeR = false;
         }

         if (applyto.getBuffedValue(SecondaryStat.StopForceAtominfo) != null) {
            array2 = new int[]{61101002, 61110211, 61120007, 61121217};
            int[] var65 = array2;
            FreudsProtection = array2.length;

            for(size3 = 0; size3 < FreudsProtection; ++size3) {
               Integer skill = var65[size3];
               if (applyto.getBuffedValue(skill)) {
                  applyto.cancelEffect(applyto.getBuffedEffect(skill));
               }
            }

            if (this.sourceid != 61120008 && this.sourceid != 61121053) {
               SkillFactory.getSkill(61110211).getEffect(applyto.getSkillLevel(61101002)).applyTo(applyto);
            } else {
               SkillFactory.getSkill(61121217).getEffect(applyto.getSkillLevel(61120007)).applyTo(applyto);
            }
         }
         break;
      case 61121009:
         localstatups.put(SecondaryStat.RoburstArmor, new Pair(this.x, localDuration));
         break;
      case 61121052:
         localstatups.put(SecondaryStat.IgnorePCounter, new Pair(1, 2000));
         localstatups.put(SecondaryStat.NotDamaged, new Pair(1, 2000));
         break;
      case 61121054:
         bufftimeR = false;
         var29 = applyto.getCooldowns().iterator();

         while(var29.hasNext()) {
            i = (MapleCoolDownValueHolder)var29.next();
            if (SkillFactory.getSkill(i.skillId) != null && i.skillId != 61121054 && !SkillFactory.getSkill(i.skillId).isHyper() && GameConstants.isKaiser(i.skillId / 10000)) {
               applyto.removeCooldown(i.skillId);
               applyto.getClient().getSession().writeAndFlush(CField.skillCooldown(i.skillId, 0));
            }
         }

         localstatups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(this.indiePad), localDuration));
         localstatups.put(SecondaryStat.IndieBooster, new Pair(-1, localDuration));
         localstatups.put(SecondaryStat.IgnorePCounter, new Pair(1, localDuration));
         localstatups.put(SecondaryStat.IgnorePImmune, new Pair(1, localDuration));
         break;
      case 63101001:
         localstatups.clear();
         localstatups.put(SecondaryStat.Possession, new Pair(1, localDuration));
         break;
      case 63101005:
         localstatups.clear();
         localstatups.put(SecondaryStat.DragonPang, new Pair(1, 0));
         break;
      case 63111009:
         localstatups.clear();
         localstatups.put(SecondaryStat.RemainIncense, new Pair(1, 0));
         break;
      case 63111013:
         localstatups.clear();
         localstatups.put(SecondaryStat.DeathBlessing, new Pair(1, localDuration));
         break;
      case 63121008:
         localstatups.clear();
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 3660));
         localstatups.put(SecondaryStat.KeyDownMoving, new Pair(100, 3660));
         break;
      case 63121044:
         applyto.setSkillCustomInfo(this.sourceid, (long)applyfrom.getId(), 0L);
         localstatups.clear();
         localstatups.put(SecondaryStat.IncarnationAura, new Pair(1, localDuration));
         if (primary) {
            localstatups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(this.indieDamR), localDuration));
            localstatups.put(SecondaryStat.IndieStance, new Pair(Integer.valueOf(this.indieStance), localDuration));
            localstatups.put(SecondaryStat.IndiePadR, new Pair(Integer.valueOf(this.indiePadR), localDuration));
         }
         break;
      case 64001001:
         localstatups.put(SecondaryStat.BeyondNextAttackProb, new Pair(10, localDuration));
         break;
      case 64001007:
      case 64001008:
      case 64001009:
      case 64001010:
      case 64001011:
      case 64001012:
         localstatups.clear();
         localstatups.put(SecondaryStat.DarkSight, new Pair(Integer.valueOf(this.level), localDuration));
         break;
      case 64100004:
      case 64110005:
      case 64120006:
         bufftimeR = false;
         localstatups.put(SecondaryStat.WeaponVariety, new Pair(applyto.weaponChanges1.size(), localDuration));
         break;
      case 64121001:
         if (primary) {
            localDuration = 14000;
         }

         localstatups.put(SecondaryStat.NotDamaged, new Pair(1, localDuration));
         break;
      case 65101002:
         if (!primary) {
            bufftimeR = false;
         } else {
            applyto.setBarrier(1000);
         }

         localstatups.put(SecondaryStat.PowerTransferGauge, new Pair(applyto.getBarrier(), localDuration));
         break;
      case 65111004:
         localstatups.put(SecondaryStat.Stance, new Pair(Integer.valueOf(this.prop), localDuration));
         break;
      case 65120006:
         bufftimeR = false;
         localstatups.put(SecondaryStat.AffinitySlug, new Pair(this.y, 5000));
         break;
      case 65121012:
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 2000));
         break;
      case 65121101:
         localstatups.clear();
         value = (int)(applyto.getSkillCustomValue(65121101) != null ? applyto.getSkillCustomValue0(65121101) : 0L);
         if (value < 3) {
            applyto.setSkillCustomInfo(65121101, (long)(value + 1), 0L);
            if (applyto.getSkillCustomValue0(65121101) > 3L) {
               applyto.setSkillCustomInfo(65121101, 3L, 0L);
               value = (int)applyto.getSkillCustomValue0(65121101);
            }

            localstatups.put(SecondaryStat.Trinity, new Pair(this.x * (value + 1), 7000));
            localstatups.put(SecondaryStat.DamR, new Pair(this.x * (value + 1), 7000));
            localstatups.put(SecondaryStat.IgnoreMobPdpR, new Pair(this.x * (value + 1), 7000));
            localDuration = 7000;
         }
         break;
      case 80000268:
      case 150000017:
         bufftimeR = false;
         localstatups.put(SecondaryStat.IndieDamR, new Pair(this.y * applyto.FlowofFight, localDuration));
         localstatups.put(SecondaryStat.FlowOfFight, new Pair(applyto.FlowofFight, localDuration));
         break;
      case 80000329:
         bufftimeR = false;
         value = 0;
         if (applyto.getSkillLevel(30000074) <= 0) {
            if (applyto.getSkillLevel(30000075) <= 0) {
               if (applyto.getSkillLevel(30000076) <= 0) {
                  if (applyto.getSkillLevel(30000077) > 0) {
                     value += applyto.getSkillLevel(30000077);
                  }
               } else {
                  value += applyto.getSkillLevel(30000076);
               }
            } else {
               value += applyto.getSkillLevel(30000075);
            }
         } else {
            value += applyto.getSkillLevel(30000074);
         }

         if (applyto.getSkillLevel(80000329) > 0) {
            value += applyto.getSkillLevel(80000329);
         }

         if (value > 8) {
            value = 8;
         }

         if (value > 0) {
            localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, value * 1000));
         }
         break;
      case 80000514:
      case 150010241:
         bufftimeR = false;
         if (!applyto.getBuffedValue(this.sourceid)) {
            applyto.LinkofArk = 0;
         }

         if (applyto.LinkofArk < 5) {
            ++applyto.LinkofArk;
         }

         localstatups.put(SecondaryStat.IndieDamR, new Pair(this.y * applyto.LinkofArk, localDuration));
         localstatups.put(SecondaryStat.LinkOfArk, new Pair(applyto.LinkofArk, localDuration));
         break;
      case 80001428:
         bufftimeR = false;
         localstatups.put(SecondaryStat.IndieAsrR, new Pair(20, localDuration));
         localstatups.put(SecondaryStat.IndieTerR, new Pair(20, localDuration));
         localstatups.put(SecondaryStat.IndieStance, new Pair(100, localDuration));
         localstatups.put(SecondaryStat.DotHealHPPerSecond, new Pair(10, localDuration));
         localstatups.put(SecondaryStat.DotHealMPPerSecond, new Pair(10, localDuration));
         break;
      case 80001462:
         localstatups.put(SecondaryStat.IndieIgnoreMobPdpR, new Pair(applyto.getStat().critical_rate * this.x / 100, localDuration));
         break;
      case 80001463:
         bufftimeR = false;
         value = 0;
         if (!GameConstants.isXenon(applyto.getJob())) {
            if (!GameConstants.isDemonAvenger(applyto.getJob())) {
               if (!GameConstants.isWarrior(applyto.getJob())) {
                  if (!GameConstants.isMagician(applyto.getJob())) {
                     if (!GameConstants.isArcher(applyto.getJob()) && !GameConstants.isCaptain(applyto.getJob()) && !GameConstants.isMechanic(applyto.getJob()) && !GameConstants.isAngelicBuster(applyto.getJob())) {
                        if (!GameConstants.isThief(applyto.getJob())) {
                           if (GameConstants.isPirate(applyto.getJob())) {
                              value = applyto.getStat().getStr();
                           }
                        } else {
                           value = applyto.getStat().getLuk();
                        }
                     } else {
                        value = applyto.getStat().getDex();
                     }
                  } else {
                     value = applyto.getStat().getInt();
                  }
               } else {
                  value = applyto.getStat().getStr();
               }
            } else {
               value = (int)applyto.getStat().getCurrentMaxHp();
            }
         } else {
            value = applyto.getStat().getStr() + applyto.getStat().getDex() + applyto.getStat().getLuk();
         }

         localstatups.put(SecondaryStat.IndieCD, new Pair(value * this.x / 100, localDuration));
         break;
      case 80001464:
         bufftimeR = false;
         value = 0;
         if (!GameConstants.isXenon(applyto.getJob())) {
            if (!GameConstants.isDemonAvenger(applyto.getJob())) {
               if (!GameConstants.isWarrior(applyto.getJob())) {
                  if (!GameConstants.isMagician(applyto.getJob())) {
                     if (!GameConstants.isArcher(applyto.getJob()) && !GameConstants.isCaptain(applyto.getJob()) && !GameConstants.isMechanic(applyto.getJob()) && !GameConstants.isAngelicBuster(applyto.getJob())) {
                        if (!GameConstants.isThief(applyto.getJob())) {
                           if (GameConstants.isPirate(applyto.getJob())) {
                              value = applyto.getStat().getStr();
                           }
                        } else {
                           value = applyto.getStat().getLuk();
                        }
                     } else {
                        value = applyto.getStat().getDex();
                     }
                  } else {
                     value = applyto.getStat().getInt();
                  }
               } else {
                  value = applyto.getStat().getStr();
               }
            } else {
               value = (int)applyto.getStat().getCurrentMaxHp();
            }
         } else {
            value = applyto.getStat().getStr() + applyto.getStat().getDex() + applyto.getStat().getLuk();
         }

         localstatups.put(SecondaryStat.Stance, new Pair(value * this.x / 100, localDuration));
         break;
      case 80001465:
         bufftimeR = false;
         value = this.x * (applyto.getStat().getStr() + applyto.getStat().getDex() + applyto.getStat().getInt() + applyto.getStat().getLuk()) / 100;
         if (!GameConstants.isXenon(applyto.getJob())) {
            if (!GameConstants.isDemonAvenger(applyto.getJob())) {
               if (!GameConstants.isWarrior(applyto.getJob())) {
                  if (!GameConstants.isMagician(applyto.getJob())) {
                     if (!GameConstants.isArcher(applyto.getJob()) && !GameConstants.isCaptain(applyto.getJob()) && !GameConstants.isMechanic(applyto.getJob()) && !GameConstants.isAngelicBuster(applyto.getJob())) {
                        if (!GameConstants.isThief(applyto.getJob())) {
                           if (GameConstants.isPirate(applyto.getJob())) {
                              localstatups.put(SecondaryStat.IndieStr, new Pair(value, localDuration));
                           }
                        } else {
                           localstatups.put(SecondaryStat.IndieLuk, new Pair(value, localDuration));
                        }
                     } else {
                        localstatups.put(SecondaryStat.IndieDex, new Pair(value, localDuration));
                     }
                  } else {
                     localstatups.put(SecondaryStat.IndieInt, new Pair(value, localDuration));
                  }
               } else {
                  localstatups.put(SecondaryStat.IndieStr, new Pair(value, localDuration));
               }
            } else {
               localstatups.put(SecondaryStat.IndieHp, new Pair(value, localDuration));
            }
         } else {
            localstatups.put(SecondaryStat.IndieStr, new Pair(value / 3, localDuration));
            localstatups.put(SecondaryStat.IndieDex, new Pair(value / 3, localDuration));
            localstatups.put(SecondaryStat.IndieLuk, new Pair(value / 3, localDuration));
         }
         break;
      case 80001466:
         localstatups.put(SecondaryStat.IndieStr, new Pair((applyto.getLevel() + 1) * this.x / 100, localDuration));
         break;
      case 80001467:
         localstatups.put(SecondaryStat.IndieDex, new Pair((applyto.getLevel() + 1) * this.x / 100, localDuration));
         break;
      case 80001468:
         localstatups.put(SecondaryStat.IndieInt, new Pair((applyto.getLevel() + 1) * this.x / 100, localDuration));
         break;
      case 80001469:
         localstatups.put(SecondaryStat.IndieLuk, new Pair((applyto.getLevel() + 1) * this.x / 100, localDuration));
         break;
      case 80001470:
         eq = (Equip)applyto.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
         if (eq != null) {
            eq = (Equip)MapleItemInformationProvider.getInstance().getEquipById(eq.getItemId());
            localstatups.put(SecondaryStat.IndieStr, new Pair(eq.getWatk() * this.x / 100, localDuration));
         } else {
            System.out.println("무기 없이 버프를 어떻게 써 ㅡㅡ " + applyto.getName());
         }
         break;
      case 80001471:
         eq = (Equip)applyto.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
         if (eq != null) {
            eq = (Equip)MapleItemInformationProvider.getInstance().getEquipById(eq.getItemId());
            localstatups.put(SecondaryStat.IndieDex, new Pair(eq.getWatk() * this.x / 100, localDuration));
         } else {
            System.out.println("무기 없이 버프를 어떻게 써 ㅡㅡ " + applyto.getName());
         }
         break;
      case 80001472:
         eq = (Equip)applyto.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
         if (eq != null) {
            eq = (Equip)MapleItemInformationProvider.getInstance().getEquipById(eq.getItemId());
            localstatups.put(SecondaryStat.IndieInt, new Pair(eq.getMatk() * this.x / 100, localDuration));
         } else {
            System.out.println("무기 없이 버프를 어떻게 써 ㅡㅡ " + applyto.getName());
         }
         break;
      case 80001473:
         eq = (Equip)applyto.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
         if (eq != null) {
            eq = (Equip)MapleItemInformationProvider.getInstance().getEquipById(eq.getItemId());
            localstatups.put(SecondaryStat.IndieLuk, new Pair(eq.getWatk() * this.x / 100, localDuration));
         } else {
            System.out.println("무기 없이 버프를 어떻게 써 ㅡㅡ " + applyto.getName());
         }
         break;
      case 80001474:
         localstatups.put(SecondaryStat.IndieBooster, new Pair(-2, localDuration));
         break;
      case 80001475:
         localstatups.put(SecondaryStat.IgnoreAllCounter, new Pair(1, localDuration));
         localstatups.put(SecondaryStat.IgnoreAllImmune, new Pair(1, localDuration));
         break;
      case 80001476:
         localstatups.put(SecondaryStat.IndieIgnoreMobPdpR, new Pair(Integer.valueOf(this.indieIgnoreMobpdpR), localDuration));
         localstatups.put(SecondaryStat.IndiePddR, new Pair(Integer.valueOf(this.indiePddR), localDuration));
         break;
      case 80001477:
         localstatups.put(SecondaryStat.ReflectDamR, new Pair(this.x, localDuration));
         break;
      case 80001479:
         localstatups.put(SecondaryStat.IndiePadR, new Pair(Integer.valueOf(this.indiePadR), localDuration));
         localstatups.put(SecondaryStat.IndieMadR, new Pair(Integer.valueOf(this.indieMadR), localDuration));
         break;
      case 80001535:
         localstatups.put(SecondaryStat.MesoUp, new Pair(20, 0));
         break;
      case 80001537:
         localstatups.put(SecondaryStat.IndieAllStatR, new Pair(20, 0));
         break;
      case 80001538:
         localstatups.clear();
         list = applyfrom.getsuccessorEffect();
         stats = new SecondaryStat[]{SecondaryStat.IndieStr, SecondaryStat.IndieDex, SecondaryStat.IndieInt, SecondaryStat.IndieLuk, SecondaryStat.IndiePmdR};
         FreudsProtection = 0;

         while(true) {
            if (FreudsProtection >= 5) {
               break label3948;
            }

            size3 = (Integer)list.get(FreudsProtection);
            applyfrom.setEffect2(FreudsProtection, (Integer)list.get(FreudsProtection));
            if (size3 > 0) {
               localstatups.put(stats[FreudsProtection], new Pair(size3, 0));
            }

            ++FreudsProtection;
         }
      case 80001539:
         localstatups.put(SecondaryStat.IndieBDR, new Pair(40, 0));
         break;
      case 80001543:
         aftercancel = true;
         localDuration = 0;
         localstatups.put(SecondaryStat.IndieAllStat, new Pair(500, 0));
         localstatups.put(SecondaryStat.IndiePadR, new Pair(300, 0));
         localstatups.put(SecondaryStat.IndieMadR, new Pair(300, 0));
         localstatups.put(SecondaryStat.IndieExp, new Pair(20, 0));
         localstatups.put(SecondaryStat.IndieBDR, new Pair(20, 0));
         localstatups.put(SecondaryStat.IndieIgnoreMobPdpR, new Pair(20, 0));
         localstatups.put(SecondaryStat.IndieCD, new Pair(10, 0));
         break;
      case 80001544:
      case 80001545:
      case 80001546:
         localstatups.put(SecondaryStat.IndieSummon, new Pair(1, 0));
         break;
      case 80001649:
         localstatups.clear();
         localDuration = 1000;
         localstatups.put(SecondaryStat.Stun, new Pair(1, localDuration));
         break;
      case 80001651:
         localstatups.clear();
         localDuration = 15000;
         localstatups.put(SecondaryStat.Pad, new Pair(Integer.valueOf(this.pad), localDuration));
         localstatups.put(SecondaryStat.Speed, new Pair(Integer.valueOf(this.speed), localDuration));
         localstatups.put(SecondaryStat.Jump, new Pair(Integer.valueOf(this.jump), localDuration));
         localstatups.put(SecondaryStat.Recovery, new Pair(100, localDuration));
         break;
      case 80001654:
         localstatups.clear();
         localDuration = 6000;
         localstatups.put(SecondaryStat.DefUp, new Pair(100, localDuration));
         break;
      case 80001655:
         localstatups.clear();
         localDuration = 6000;
         localstatups.put(SecondaryStat.BattlePvP_Mike_Shield, new Pair(500, localDuration));
         break;
      case 80001658:
         localstatups.clear();
         localstatups.put(SecondaryStat.Weakness, new Pair(1, localDuration));
         localstatups.put(SecondaryStat.Speed, new Pair(-100, localDuration));
         break;
      case 80001668:
         localstatups.clear();
         localDuration = 2000;
         localstatups.put(SecondaryStat.Speed, new Pair(20, localDuration));
         localstatups.put(SecondaryStat.NoDebuff, new Pair(1, localDuration));
         break;
      case 80001675:
         value = applyfrom.getBattleGroundChr().getLevel() < 11 ? (applyfrom.getBattleGroundChr().getLevel() < 9 ? (applyfrom.getBattleGroundChr().getLevel() < 7 ? (applyfrom.getBattleGroundChr().getLevel() < 5 ? 20 : 30) : 40) : 50) : 60;
         localstatups.clear();
         localDuration = 10000;
         localstatups.put(SecondaryStat.Poison, new Pair(value, localDuration));
         break;
      case 80001676:
      case 80003003:
      case 80003005:
      case 80003012:
         localstatups.clear();
         localstatups.put(SecondaryStat.Stun, new Pair(1, localDuration));
         break;
      case 80001732:
         localstatups.clear();
         localstatups.put(SecondaryStat.BattlePvP_Helena_Mark, new Pair(1, localDuration));
         break;
      case 80001733:
         localstatups.clear();
         localDuration = 4000;
         localstatups.put(SecondaryStat.BattlePvP_Helena_WindSpirit, new Pair(30, localDuration));
         localstatups.put(SecondaryStat.Speed, new Pair(15, localDuration));
         break;
      case 80001735:
         localstatups.clear();
         localstatups.put(SecondaryStat.Slow, new Pair(30, localDuration));
         break;
      case 80001740:
         localstatups.clear();
         localstatups.put(SecondaryStat.BattlePvP_LangE_Protection, new Pair((int)applyto.getSkillCustomValue0(80001740), localDuration));
         break;
      case 80001762:
         bufftimeR = false;
         localstatups.put(SecondaryStat.RandAreaAttack, new Pair(1, 30000));
         break;
      case 80001809:
         if (applyto.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-29) != null && (value = applyto.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-29).getItemId()) >= 1182001 && value <= 1182005) {
            if (value >= 1182001) {
               localstatups.put(SecondaryStat.IndieAllStat, new Pair(30, 0));
            }

            if (value >= 1182002) {
               localstatups.put(SecondaryStat.IndiePad, new Pair(30, 0));
               localstatups.put(SecondaryStat.IndieMad, new Pair(30, 0));
            }

            if (value >= 1182003) {
               localstatups.put(SecondaryStat.IndieBDR, new Pair(15, 0));
            }

            if (value >= 1182004) {
               localstatups.put(SecondaryStat.IndieCD, new Pair(8, 0));
            }

            if (value >= 1182005) {
               localstatups.put(SecondaryStat.IndieSummon, new Pair(1, 0));
            }
         }
         break;
      case 80001878:
         localstatups.clear();
         localstatups.put(SecondaryStat.FixCooltime, new Pair(5, 60000));
         break;
      case 80001965:
         localstatups.put(SecondaryStat.DashSpeed, new Pair(300, localDuration));
         localstatups.put(SecondaryStat.IndieForceSpeed, new Pair(300, localDuration));
         localstatups.put(SecondaryStat.DashJump, new Pair(3, localDuration));
         localstatups.put(SecondaryStat.IndieForceJump, new Pair(3, localDuration));
         break;
      case 80002255:
         localstatups.put(SecondaryStat.StopPortion, new Pair(1, 6000));
         break;
      case 80002280:
         bufftimeR = false;
         if (applyfrom.getSkillLevel(20010294) > 0 || applyfrom.getSkillLevel(80000369) > 0) {
            value = applyfrom.getSkillLevel(20010294) > 0 ? applyfrom.getSkillLevel(20010294) : applyfrom.getSkillLevel(80000369);
            up = value == 1 ? 30 : 50;
            localDuration += localDuration / 100 * up;
         }
         break;
      case 80002282:
         bufftimeR = false;
         break;
      case 80002338:
         localstatups.clear();
         if (!applyto.getBuffedValue(this.sourceid) && applyto.getSkillCustomValue0(this.sourceid) > 0L) {
            applyto.removeSkillCustomInfo(this.sourceid);
         }

         applyto.setSkillCustomInfo(this.sourceid, applyto.getSkillCustomValue0(this.sourceid) + 1L, 0L);
         if (applyto.getSkillCustomValue0(this.sourceid) < 3L) {
            localDuration = 6000;
            localstatups.put(SecondaryStat.BattlePvP_Rude_Stack, new Pair((int)applyto.getSkillCustomValue0(this.sourceid), localDuration));
         } else {
            applyto.removeSkillCustomInfo(this.sourceid);
            localstatups.put(SecondaryStat.Stun, new Pair(1, 1000));
         }
         break;
      case 80002340:
         localstatups.clear();
         localstatups.put(SecondaryStat.Slow, new Pair(50, localDuration));
         break;
      case 80002341:
         localstatups.clear();
         localDuration = 7000;
         localstatups.put(SecondaryStat.Speed, new Pair(20, localDuration));
         localstatups.put(SecondaryStat.Jump, new Pair(10, localDuration));
         localstatups.put(SecondaryStat.NoDebuff, new Pair(1, localDuration));
         break;
      case 80002342:
         localstatups.clear();
         localstatups.put(SecondaryStat.NotDamaged, new Pair(1, localDuration));
         break;
      case 80002344:
         localstatups.clear();
         localstatups.put(SecondaryStat.Slow, new Pair(80, localDuration));
         localstatups.put(SecondaryStat.ReverseInput, new Pair(1, localDuration));
         break;
      case 80002393:
         localstatups.clear();
         if (!applyto.getBuffedValue(80002393)) {
            localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, localDuration));
            localstatups.put(SecondaryStat.NotDamaged, new Pair(1, localDuration));
            break;
         }
      case 80002093:
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, localDuration));
         localstatups.put(SecondaryStat.NotDamaged, new Pair(1, localDuration));
         break;
      case 80002416:
         pad2 = 20;
         localstatups.put(SecondaryStat.IndieMad, new Pair(Integer.valueOf(pad2), 0));
         localstatups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(pad2), 0));
         localstatups.put(SecondaryStat.IndieCD, new Pair(Integer.valueOf(pad2), 0));
         localstatups.put(SecondaryStat.IndieBDR, new Pair(Integer.valueOf(pad2), 0));
         localstatups.put(SecondaryStat.IndiePmdR, new Pair(Integer.valueOf(pad2), 0));
         localstatups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(pad2), 0));
         break;
      case 80002419:
         localstatups.clear();
         list = applyfrom.getBonusEffect();
         stats = new SecondaryStat[]{SecondaryStat.IndieDamR, SecondaryStat.IndieExp, SecondaryStat.DropRate, SecondaryStat.MesoUp, SecondaryStat.IndieCD, SecondaryStat.IndieBDR, SecondaryStat.IndieAllStatR, SecondaryStat.IndiePmdR};
         FreudsProtection = 0;

         while(true) {
            if (FreudsProtection >= 8) {
               break label3948;
            }

            size3 = (Integer)list.get(FreudsProtection);
            applyfrom.setEffect(FreudsProtection, (Integer)list.get(FreudsProtection));
            if (size3 > 0) {
               localstatups.put(stats[FreudsProtection], new Pair(size3, 0));
            }

            ++FreudsProtection;
         }
      case 80002421:
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 3000));
         break;
      case 80002543:
         localDuration = 0;
         localstatups.put(SecondaryStat.DebuffIncHp, new Pair(50, localDuration));
         break;
      case 80002625:
         localstatups.clear();
         type = false;
         value = applyto.getSkillCustomValue0(80002625) != 1L ? 1 : 2;
         applyto.setSkillCustomInfo(80002625, (long)value, 0L);
         localstatups.put(SecondaryStat.BlackMageDebuff, new Pair((int)applyto.getSkillCustomValue0(80002625), 2100000));
         break;
      case 80002632:
         localstatups.clear();
         localDuration = 0;
         localstatups.put(SecondaryStat.YalBuff, new Pair(15, localDuration));
         break;
      case 80002633:
         localstatups.clear();
         if (applyto.getBuffedValue(80002633)) {
            applyto.cancelEffectFromBuffStat(SecondaryStat.IndieNotDamaged, 80002633);
            applyto.cancelEffectFromBuffStat(SecondaryStat.NotDamaged, 80002633);
            applyto.cancelEffectFromBuffStat(SecondaryStat.IonBuff, 80002633);
         } else {
            localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, localDuration));
            localstatups.put(SecondaryStat.NotDamaged, new Pair(1, localDuration));
            localstatups.put(SecondaryStat.IonBuff, new Pair(1, localDuration));
         }
         break;
      case 80002644:
         localstatups.clear();
         break;
      case 80002670:
         localstatups.clear();
         localDuration = 600000;
         localstatups.put(SecondaryStat.BattlePvP_Wonky_ChargeA, new Pair(1, localDuration));
         localstatups.put(SecondaryStat.Speed, new Pair(-30, localDuration));
         break;
      case 80002671:
         localstatups.clear();
         localstatups.put(SecondaryStat.Flying, new Pair(1, localDuration - 700));
         localstatups.put(SecondaryStat.Speed, new Pair(-60, localDuration - 700));
         localDuration = 4000;
         break;
      case 80002672:
         localstatups.clear();
         localstatups.put(SecondaryStat.BattlePvP_Wonky_Awesome, new Pair(10, localDuration));
         localDuration = 10000;
         break;
      case 80002673:
         localstatups.clear();
         localstatups.put(SecondaryStat.ReverseInput, new Pair(1, localDuration));
         break;
      case 80002674:
         localstatups.clear();
         localDuration = 600000;
         localstatups.put(SecondaryStat.NoDebuff, new Pair(1, localDuration));
         localstatups.put(SecondaryStat.BattlePvP_Wonky_ChargeA, new Pair(1, localDuration));
         localstatups.put(SecondaryStat.Speed, new Pair(20, localDuration));
         break;
      case 80002751:
         localstatups.put(SecondaryStat.WillPoison, new Pair(1, this.duration));
         break;
      case 80002758:
         value = 1;

         while(true) {
            if (value > 3) {
               break label3948;
            }

            Timer.BuffTimer.getInstance().schedule(() -> {
               applyto.addHP((long)this.y);
            }, (long)(1000 * value));
            ++value;
         }
      case 80002762:
         if (applyto.empiricalStack < this.x) {
            ++applyto.empiricalStack;
         }

         localstatups.put(SecondaryStat.EmpiricalKnowledge, new Pair(applyto.empiricalStack, localDuration));
         break;
      case 80002770:
         if (!applyto.skillisCooling(80002770)) {
            applyto.addCooldown(80002770, System.currentTimeMillis(), 20000L);
            applyto.getClient().getSession().writeAndFlush(CField.skillCooldown(80002770, 20000));
            SkillFactory.getSkill(261).getEffect(this.level).applyTo(applyto);
         }
         break;
      case 80002902:
         bufftimeR = false;
         localstatups.clear();
         localstatups.put(SecondaryStat.DuskDarkness, new Pair(1, 0));
         break;
      case 80003004:
         localstatups.clear();
         localDuration = 10000;
         localstatups.put(SecondaryStat.Slow, new Pair(applyto.getBattleGroundChr().getSpeed() - 50, localDuration));
         break;
      case 80003018:
         aftercancel = true;
         localstatups.clear();
         localstatups.put(SecondaryStat.PriorPryperation, new Pair((int)applyto.getSkillCustomValue0(this.sourceid), 0));
         break;
      case 80003023:
         localstatups.put(SecondaryStat.EventSpecialSkill, new Pair(1, localDuration));
         break;
      case 80003025:
         localstatups.put(SecondaryStat.EventSpecialSkill, new Pair(1, 0));
         break;
      case 80003046:
         if (applyfrom.getKeyValue(100794, "today") >= (long)(Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 3000 : 6000)) {
            return;
         }

         if (applyfrom.getBuffedValue(this.sourceid)) {
            return;
         }

         applyfrom.getClient().send(SLFCGPacket.FollowNpctoSkill(false, 9062524, 0));
         applyfrom.getClient().send(SLFCGPacket.FollowNpctoSkill(true, 9062524, 80003051));
         if (applyfrom.getQuestStatus(100801) == 2) {
            applyfrom.getClient().send(SLFCGPacket.FollowNpctoSkill(false, 9062525, 0));
            applyfrom.getClient().send(SLFCGPacket.FollowNpctoSkill(true, 9062525, 80003052));
         }

         if (applyfrom.getQuestStatus(100801) == 3) {
            applyfrom.getClient().send(SLFCGPacket.FollowNpctoSkill(false, 9062526, 0));
            applyfrom.getClient().send(SLFCGPacket.FollowNpctoSkill(true, 9062526, 80003053));
         }

         localstatups.clear();
         localstatups.put(SecondaryStat.EventSpecialSkill, new Pair(1, 0));
         break;
      case 80003058:
      case 160010001:
         localstatups.put(SecondaryStat.IndieNDR, new Pair(this.w, localDuration));
         break;
      case 80003059:
         eff = SkillFactory.getSkill(162111000).getEffect(applyfrom.getSkillLevel(162111000));
         localstatups.put(SecondaryStat.NewFlying, new Pair(1, (int)eff.getT() * 1000));
         break;
      case 80003064:
         if (applyto.getKeyValue(100857, "feverCnt") >= 0L) {
            if (applyto.getKeyValue(100857, "feverCnt") < 10L) {
               localstatups.put(SecondaryStat.EventSpecialSkill, new Pair(1, localDuration));
            } else {
               applyto.dropMessage(5, "오늘은 이미 <리액션 팡팡>을 10번 완료하였습니다.");
            }
         } else {
            applyto.setKeyValue(100857, "feverCnt", "0");
         }
         break;
      case 80003070:
         aftercancel = true;
         localstatups.put(SecondaryStat.NatureFriend, new Pair((int)applyto.getSkillCustomValue0(this.sourceid), 0));
         break;
      case 100000276:
         bufftimeR = false;
         aftercancel = true;
         localstatups.clear();
         localDuration = !applyto.getBuffedValue(400001045) ? 20000 : 0;
         localstatups.put(SecondaryStat.TimeFastABuff, new Pair(Integer.valueOf(applyto.RapidTimeDetect), localDuration));
         break;
      case 100000277:
         bufftimeR = false;
         aftercancel = true;
         localstatups.clear();
         localDuration = !applyto.getBuffedValue(400001045) ? 20000 : 0;
         localstatups.put(SecondaryStat.TimeFastBBuff, new Pair(Integer.valueOf(applyto.RapidTimeStrength), localDuration));
         if (applyto.RapidTimeStrength == 10) {
            localstatups.put(SecondaryStat.IndieBooster, new Pair(-1, localDuration));
         }
         break;
      case 100001261:
         bufftimeR = false;
         localstatups.put(SecondaryStat.IndieBooster, new Pair(-1, 5000));
         break;
      case 100001263:
         if (primary) {
            localDuration = 0;
            localstatups.put(SecondaryStat.ZeroAuraStr, new Pair(1, 0));
         }

         localstatups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(this.indiePad), localDuration));
         localstatups.put(SecondaryStat.IndieMad, new Pair(Integer.valueOf(this.indieMad), localDuration));
         localstatups.put(SecondaryStat.IndiePdd, new Pair(Integer.valueOf(this.indiePdd), localDuration));
         localstatups.put(SecondaryStat.IndieTerR, new Pair(Integer.valueOf(this.indieTerR), localDuration));
         localstatups.put(SecondaryStat.IndieAsrR, new Pair(Integer.valueOf(this.indieAsrR), localDuration));
         break;
      case 100001264:
         if (primary) {
            localDuration = 0;
         }

         localstatups.put(SecondaryStat.IndieSpeed, new Pair(Integer.valueOf(this.indieSpeed), localDuration));
         localstatups.put(SecondaryStat.IndieJump, new Pair(Integer.valueOf(this.indieJump), localDuration));
         localstatups.put(SecondaryStat.IndieBooster, new Pair(-1, localDuration));
         if (applyto.getBuffedValue(SecondaryStat.ZeroAuraStr) != null) {
            applyto.cancelEffectFromBuffStat(SecondaryStat.ZeroAuraStr, 100001263);
         }
         break;
      case 100001272:
         bufftimeR = false;
         localstatups.clear();
         if (!primary) {
            localstatups.put(SecondaryStat.IgnorePCounter, new Pair(1, 2000));
            localstatups.put(SecondaryStat.NotDamaged, new Pair(1, 2000));
         } else {
            localstatups.put(SecondaryStat.ReviveOnce, new Pair(100, 0));
         }
         break;
      case 100001274:
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, localDuration));
         localstatups.put(SecondaryStat.IndieBlockSkill, new Pair(1, localDuration));
         var29 = applyto.getCooldowns().iterator();

         while(true) {
            do {
               if (!var29.hasNext()) {
                  if (applyto.getLevel() >= 200) {
                     SkillFactory.getSkill(100001281).getEffect(1).applyTo(applyto, false);
                  }
                  break label3948;
               }

               i = (MapleCoolDownValueHolder)var29.next();
               i = i.skillId;
            } while((i / 10000 == 40001 || i / 10000 == 10000) && i != 100001005 && i != 100001261 && i != 100001283);

            if (i != 100001274) {
               applyto.removeCooldown(i);
               applyto.getClient().getSession().writeAndFlush(CField.skillCooldown(i, 0));
            }
         }
      case 100001281:
         bufftimeR = false;
         localstatups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(this.indieDamR), localDuration));
         break;
      case 101120109:
         bufftimeR = false;
         localstatups.clear();
         if (primary) {
            if (applyto.getBuffedValue(400001045)) {
               localDuration = 0;
            }

            localstatups.put(SecondaryStat.ImmuneBarrier, new Pair((int)applyto.getSkillCustomValue0(101120109), localDuration));
         } else {
            aftercancel = true;
            localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 500));
         }
         break;
      case 131001004:
         if (!primary) {
            localstatups.put(SecondaryStat.PinkbeanRollingGrade, new Pair(1, 0));
         } else {
            localstatups.put(SecondaryStat.KeyDownMoving, new Pair(350, 0));
         }
         break;
      case 131001009:
         localstatups.put(SecondaryStat.IndieSpeed, new Pair(Integer.valueOf(this.indieSpeed), localDuration));
         localstatups.put(SecondaryStat.IndieExp, new Pair(30, localDuration));
         localstatups.put(SecondaryStat.IndiePadR, new Pair(Integer.valueOf(this.indiePadR), localDuration));
         localstatups.put(SecondaryStat.IndieMadR, new Pair(Integer.valueOf(this.indiePadR), localDuration));
         localstatups.put(SecondaryStat.PinkBeanFighting, new Pair(1, localDuration));
         break;
      case 131001010:
      case 131001011:
         if (!applyto.getBuffedValue(131001010)) {
            applyto.removeSkillCustomInfo(this.sourceid + 100);
         }

         if (applyto.getSkillCustomValue0(this.sourceid + 100) < (long)this.u2) {
            applyto.addSkillCustomInfo(this.sourceid + 100, 1L);
         }

         localstatups.put(SecondaryStat.PinkbeanYoYoAttackStack, new Pair((int)applyto.getSkillCustomValue0(this.sourceid + 100), localDuration));
         break;
      case 131001018:
         localstatups.put(SecondaryStat.IndieStatR, new Pair(applyto.getLevel() / this.y, localDuration));
         break;
      case 131001019:
         localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
         break;
      case 131001020:
         if (primary) {
            applyto.setSkillCustomInfo(this.sourceid, 0L, 0L);
         }

         applyto.addSkillCustomInfo(this.sourceid, 1L);
         localstatups.put(SecondaryStat.KeyDownMoving, new Pair(200, localDuration));
         break;
      case 131001023:
         localstatups.put(SecondaryStat.PinkBeanMatroCyca, new Pair(1, localDuration));
         localstatups.put(SecondaryStat.NotDamaged, new Pair(1, localDuration));
         break;
      case 131001026:
         aftercancel = true;
         if (primary && applyto.getSkillCustomValue0(131001026) < (long)this.x) {
            applyto.addSkillCustomInfo(131001026, 1L);
         }

         localstatups.put(SecondaryStat.PinkBeanMagicShow, new Pair((int)applyto.getSkillCustomValue0(131001026), 0));
         break;
      case 131001113:
         localstatups.put(SecondaryStat.MaxLevelBuff, new Pair(Integer.valueOf(this.indieMadR), localDuration));
         break;
      case 131001306:
         applyto.getStat().heal(applyto);
         break;
      case 135001005:
         localstatups.clear();
         if (primary) {
            if (applyfrom.getBuffedEffect(SecondaryStat.YetiAngerMode) == null) {
               while(applyfrom.getBuffedValue(135001005)) {
                  applyfrom.cancelEffect(this);
               }

               applyfrom.setSkillCustomInfo(13500, 1L, 0L);
               applyfrom.setSkillCustomInfo(135001007, 3L, 0L);
               SkillFactory.getSkill(135001007).getEffect(1).applyTo(applyfrom);
            }

            localstatups.clear();
            localstatups.put(SecondaryStat.IndieSpeed, new Pair(Integer.valueOf(this.indieSpeed), localDuration));
            localstatups.put(SecondaryStat.IndiePadR, new Pair(Integer.valueOf(this.indiePadR), localDuration));
            localstatups.put(SecondaryStat.YetiAngerMode, new Pair(1, localDuration));
         } else {
            aftercancel = true;
            localstatups.put(SecondaryStat.YetiAnger, new Pair(1, 0));
         }
         break;
      case 135001007:
         aftercancel = true;
         localstatups.clear();
         localstatups.put(SecondaryStat.VMatrixStackBuff, new Pair((int)applyfrom.getSkillCustomValue0(this.sourceid), 0));
         break;
      case 135001009:
         localstatups.clear();
         localstatups.put(SecondaryStat.LuckOfUnion, new Pair(1, localDuration));
         localstatups.put(SecondaryStat.IndieExp, new Pair(30, localDuration));
         localstatups.put(SecondaryStat.IndiePadR, new Pair(Integer.valueOf(this.indiePadR), localDuration));
         break;
      case 135001012:
         localstatups.clear();
         localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
         localstatups.put(SecondaryStat.IndieUnk1, new Pair(1, localDuration));
         break;
      case 135001013:
         localstatups.clear();
         value = applyfrom.getLevel() / this.y;
         localstatups.put(SecondaryStat.IndieStatR, new Pair(value, localDuration));
         break;
      case 135001015:
         localstatups.clear();
         localstatups.put(SecondaryStat.IndieExp, new Pair(50, localDuration));
         localstatups.put(SecondaryStat.YetiFriendsPePe, new Pair(1, localDuration));
         break;
      case 135001017:
         localstatups.clear();
         localstatups.put(SecondaryStat.YetiSpicy, new Pair(1, localDuration));
         break;
      case 142101004:
         if (applyto.getSkillLevel(142110009) <= 0) {
            localstatups.put(SecondaryStat.IndiePdd, new Pair(Integer.valueOf(this.indiePdd), localDuration));
         } else {
            SkillFactory.getSkill(142110009).getEffect(applyto.getSkillLevel(142110009)).applyTo(applyto, false);
         }
         break;
      case 142110009:
         localstatups.put(SecondaryStat.IndiePdd, new Pair(Integer.valueOf(this.indiePdd), 180000));
         localstatups.put(SecondaryStat.Stance, new Pair(this.stanceProp, 180000));
         break;
      case 142111010:
         localstatups.clear();
         localstatups.put(SecondaryStat.NewFlying, new Pair(1, 900));
         break;
      case 142121004:
         if (applyto.getSkillCustomValue0(142121004) > 0L) {
            localstatups.clear();
            localstatups.put(SecondaryStat.IndiePmdR, new Pair((int)applyto.getSkillCustomValue0(142121004), localDuration));
         }
         break;
      case 142121030:
         localstatups.put(SecondaryStat.NotDamaged, new Pair(1, 0));
         break;
      case 150011075:
         localstatups.put(SecondaryStat.IndieExp, new Pair(SkillFactory.getSkill(150011074).getEffect(1).getS() / 2, localDuration));
         localstatups.put(SecondaryStat.IndieDamR, new Pair(SkillFactory.getSkill(150011074).getEffect(1).getQ() / 2, localDuration));
         localstatups.put(SecondaryStat.IndieReduceCooltime, new Pair(SkillFactory.getSkill(150011074).getEffect(1).getW() / 2, localDuration));
         applyto.getClient().getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(3001532, 5000, "안녕! 우리는 새로운 곳을 발견해서 가보려고 하는 중이야. 어떤 곳일지 모두들 기대하고 있어...그런데 또 별볼일 없는 곳이면 어쩌지.....모두가 실망하는 걸 보긴 싫은데... 그렇다고 가지 않을 수는 없고...", ""));
         break;
      case 150011076:
         localstatups.put(SecondaryStat.IndieReduceCooltime, new Pair(SkillFactory.getSkill(150011074).getEffect(1).getW(), localDuration));
         applyto.getClient().getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(3001533, 5000, "이전 너와 교신한 후 1년 이상 지난 것이다, 치르.\n연락이 너무 뜸하다는 생각이 드는 것이다, 치르.\n바크바크 녀석처럼 마음만 있으면 된다는 생각을 하는 것은 아니라고 믿는다, 치르.", ""));
         break;
      case 150011077:
         localstatups.put(SecondaryStat.IndieDamR, new Pair(SkillFactory.getSkill(150011074).getEffect(1).getQ(), localDuration));
         applyto.getClient().getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(3001534, 5000, "헤이! 왓썹 브로. 롱타임노씨이지만 유와 내 하트가 뜨겁게 뛰고있으니 전혀 걱정하지 않았돠아!\r\n말이 나왔으니 오랜만에 내 뮤직을 리슨.", ""));
         break;
      case 150011078:
         localstatups.put(SecondaryStat.IndieExp, new Pair(SkillFactory.getSkill(150011074).getEffect(1).getS(), localDuration));
         applyto.getClient().getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(3001535, 3000, "앗, 반짝반짝한 걸 준 사람, 잘 있었냐! 나 요즘 밥 많이 먹었더니 이만큼 컸다! 어제는 넘어지고 울지도 않음! 헤헤…..(이건 비밀인데 이 근처에서 반짝반짝하는 것도 발견했다!)", ""));
         break;
      case 150030241:
         localstatups.put(SecondaryStat.IndieUnk1, new Pair(1, localDuration));
         localstatups.put(SecondaryStat.IndieUnk2, new Pair(1, localDuration));
         localstatups.put(SecondaryStat.IndieDarkness, new Pair(1, localDuration));
         break;
      case 151001004:
         localstatups.put(SecondaryStat.IndieFloating, new Pair(1, localDuration / 1000));
         localstatups.put(SecondaryStat.NewFlying, new Pair(1, localDuration / 1000));
         break;
      case 151101010:
         if (!applyto.getBuffedValue(this.sourceid)) {
            applyto.adelResonance = 0;
         }

         if (applyto.adelResonance < this.x) {
            ++applyto.adelResonance;
         }

         localstatups.put(SecondaryStat.IndieIgnoreMobPdpR, new Pair(this.y * applyto.adelResonance, localDuration));
         localstatups.put(SecondaryStat.AdelResonance, new Pair(applyto.adelResonance, localDuration));
         break;
      case 151111003:
         showEffect = false;
         break;
      case 151111005:
         bufftimeR = false;
         localstatups.clear();
         localstatups.put(SecondaryStat.Novility, new Pair(Integer.valueOf(this.level), localDuration));
         if (applyfrom.getSkillLevel(151120038) > 0) {
            localstatups.put(SecondaryStat.IndieDamR, new Pair(SkillFactory.getSkill(151120038).getEffect(1).getX(), localDuration));
         }

         if (!primary) {
            applyto.setSkillCustomInfo(151111005, (long)applyfrom.getId(), 0L);
         } else {
            applyto.setSkillCustomInfo(151111005, (long)applyto.getId(), 0L);
         }
         break;
      case 151121004:
         localDuration = 8000;
         if (applyfrom.getSkillLevel(151120039) > 0) {
            localDuration += 2000;
         }

         localstatups.put(SecondaryStat.IndieSuperStance, new Pair(1, localDuration));
         localstatups.put(SecondaryStat.IndieDamReduceR, new Pair(-this.x, localDuration));
         localstatups.put(SecondaryStat.AntiMagicShell, new Pair(1, localDuration));
         localstatups.put(SecondaryStat.DreamDowon, new Pair(1, localDuration));
         localstatups.put(SecondaryStat.IndieUnk1, new Pair(1, 390));
         localstatups.put(SecondaryStat.Dike, new Pair(1, 390));
         break;
      case 151121011:
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 3000 + (applyto.getSkillLevel(151120039) <= 0 ? 0 : 1000)));
         break;
      case 152000009:
         bufftimeR = false;
         pad2 = 0;
         switch(applyto.blessMarkSkill) {
         case 152000007:
            pad2 = 3;
            break;
         case 152110009:
            pad2 = 6;
            break;
         case 152120012:
            pad2 = 10;
         }

         if (primary && applyto.blessMark < pad2) {
            ++applyto.blessMark;
         }

         if (applyto.blessMark > 0) {
            up = 0;

            for(i = 0; i < applyto.blessMark; ++i) {
               FreudsProtection = 0;
               if (applyto.blessMarkSkill != 152000007) {
                  if (applyto.blessMarkSkill != 152110009) {
                     if (applyto.blessMarkSkill == 152120012) {
                        FreudsProtection = i >= 3 ? (i >= 6 ? (i >= 9 ? 10 : 6) : 4) : 2;
                     }
                  } else {
                     FreudsProtection = i >= 3 ? 4 : 2;
                  }
               } else {
                  FreudsProtection = 2;
               }

               up += FreudsProtection;
            }

            localstatups.put(SecondaryStat.IndiePad, new Pair(up, localDuration));
            localstatups.put(SecondaryStat.IndieMad, new Pair(up, localDuration));
            localstatups.put(SecondaryStat.BlessMark, new Pair(applyto.blessMark, localDuration));
         }
         break;
      case 152001002:
      case 152120003:
         bufftimeR = false;
         showEffect = false;
         localstatups.put(SecondaryStat.IncreaseJabelinDam, new Pair(this.y, 2000));
         localstatups.put(SecondaryStat.IndieUnk1, new Pair(1, 2000));
         if (applyto.getSkillLevel(152120012) <= 0) {
            if (applyto.getSkillLevel(152110009) <= 0) {
               if (applyto.getSkillLevel(152000007) > 0) {
                  applyto.blessMarkSkill = 152000007;
               }
            } else {
               applyto.blessMarkSkill = 152110009;
            }
         } else {
            applyto.blessMarkSkill = 152120012;
         }

         if (applyto.blessMarkSkill != 0) {
            SkillFactory.getSkill(152000009).getEffect(applyto.getSkillLevel(152000009)).applyTo(applyto);
         }
         break;
      case 152001005:
         bufftimeR = false;
         localstatups.put(SecondaryStat.NewFlying, new Pair(1, 930));
         localstatups.put(SecondaryStat.IndieFloating, new Pair(1, 930));
         break;
      case 152100010:
      case 152110008:
      case 152120014:
         aftercancel = true;
         bufftimeR = false;
         localstatups.put(SecondaryStat.CrystalBattery, new Pair(1, 10000));
         break;
      case 152111003:
         bufftimeR = false;
         localstatups.put(SecondaryStat.IndieBDR, new Pair(Integer.valueOf(this.indieBDR), localDuration));
         localstatups.put(SecondaryStat.IndieStance, new Pair(Integer.valueOf(this.indieStance), localDuration));
         localstatups.put(SecondaryStat.IndiePmdR, new Pair(Integer.valueOf(this.indiePmdR), localDuration));
         localstatups.put(SecondaryStat.NewFlying, new Pair(1, localDuration));
         localstatups.put(SecondaryStat.IndieFloating, new Pair(1, localDuration));
         localstatups.put(SecondaryStat.GloryWing, new Pair(1, localDuration));
         value = applyfrom.getSummons(400021068).size();
         if (value > 0) {
            localstatups.put(SecondaryStat.IndieDamR, new Pair(SkillFactory.getSkill(400021068).getEffect(applyfrom.getSkillLevel(400021068)).getU() * value, localDuration));
         }

         remove = new ArrayList();
         var38 = applyfrom.getSummons(400021068).iterator();

         MapleSummon mapleSummon;
         while(var38.hasNext()) {
            mapleSummon = (MapleSummon)var38.next();
            remove.add(mapleSummon);
         }

         var38 = remove.iterator();

         while(var38.hasNext()) {
            mapleSummon = (MapleSummon)var38.next();
            mapleSummon.removeSummon(applyfrom.getMap(), false);
         }

         applyto.blessMark = 10;
         SkillFactory.getSkill(152000009).getEffect(applyto.getSkillLevel(152000009)).applyTo(applyto, Integer.MAX_VALUE);
         applyto.canUseMortalWingBeat = true;
         break;
      case 152111007:
         bufftimeR = false;
         localstatups.put(SecondaryStat.HarmonyLink, new Pair(this.x, 15000));
         break;
      case 152121011:
         localstatups.put(SecondaryStat.IndieUnkIllium, new Pair(1, localDuration));
         localstatups.put(SecondaryStat.FastCharge, new Pair(1, localDuration));
         break;
      case 152121041:
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 2500));
         break;
      case 152121043:
         bufftimeR = false;
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, Math.max(1, 4000 + applyfrom.blessMark * 600)));
         break;
      case 154100003:
      case 154101009:
      case 154110003:
      case 154111004:
      case 154120003:
      case 154121003:
      case 154121009:
      case 154121011:
         localstatups.clear();
         localstatups.put(SecondaryStat.DarkSight, new Pair(Integer.valueOf(this.level), localDuration));
         break;
      case 154111000:
         localDuration = 0;
         localstatups.put(SecondaryStat.SummonChakri, new Pair(this.x, localDuration));
         break;
      case 154121004:
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, localDuration));
         break;
      case 155000007:
         localDuration = 0;
         localstatups.put(SecondaryStat.SpectorTransForm, new Pair(1, 0));
         localstatups.put(SecondaryStat.IndiePad, new Pair(30, 0));
         localstatups.put(SecondaryStat.IndieStance, new Pair(100, 0));
         break;
      case 155001001:
         localstatups.put(SecondaryStat.Speed, new Pair(this.speed * (!applyto.getBuffedValue(155121043) ? 1 : 2), localDuration));
         localstatups.put(SecondaryStat.IndieStance, new Pair(this.indieStance * (!applyto.getBuffedValue(155121043) ? 1 : 2), localDuration));
         break;
      case 155001103:
         if (localDuration > 1) {
            localstatups.put(SecondaryStat.IndieDamR, new Pair((localDuration - 1) * this.y, this.z * 1000));
         }
         break;
      case 155001205:
         localstatups.put(SecondaryStat.IndieFloating, new Pair(1, localDuration / 1000));
         localstatups.put(SecondaryStat.NewFlying, new Pair(1, localDuration / 1000));
         break;
      case 155101003:
         localstatups.put(SecondaryStat.IndiePad, new Pair(this.indiePad * (!applyto.getBuffedValue(155121043) ? 1 : 2), localDuration));
         localstatups.put(SecondaryStat.IndieCr, new Pair(this.indieCr * (!applyto.getBuffedValue(155121043) ? 1 : 2), localDuration));
         break;
      case 155111005:
         localstatups.put(SecondaryStat.IndieBooster, new Pair(!applyto.getBuffedValue(155121043) ? -1 : -2, localDuration));
         localstatups.put(SecondaryStat.IndieEvaR, new Pair(this.indieEvaR * (!applyto.getBuffedValue(155121043) ? 1 : 2), localDuration));
         break;
      case 155111306:
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 0));
         break;
      case 155120014:
         localstatups.put(SecondaryStat.FightJazz, new Pair((int)applyto.getSkillCustomValue0(155120015), localDuration));
         break;
      case 155121005:
         localstatups.put(SecondaryStat.IndieDamR, new Pair(this.indieDamR * (!applyto.getBuffedValue(155121043) ? 1 : 2), localDuration));
         localstatups.put(SecondaryStat.IndieBDR, new Pair(this.indieBDR * (!applyto.getBuffedValue(155121043) ? 1 : 2), localDuration));
         localstatups.put(SecondaryStat.IndieIgnoreMobPdpR, new Pair(SkillFactory.getSkill(155121102).getEffect(applyto.getSkillLevel(155121102)).s2 * (!applyto.getBuffedValue(155121043) ? 1 : 2), localDuration));
         break;
      case 155121043:
         localstatups.put(SecondaryStat.ChargeSpellAmplification, new Pair(1, localDuration));
         break;
      case 162001001:
         showEffect = false;
         break;
      case 162001005:
         localstatups.put(SecondaryStat.산_무등, new Pair(this.x, 0));
         break;
      case 162101000:
         localstatups.put(SecondaryStat.용맥_읽기, new Pair(1, 0));
         break;
      case 162110007:
         applyto.addHP(applyto.getStat().getCurrentMaxHp() * (long)this.u / 100L);
         applyto.addMP(applyto.getStat().getCurrentMaxMp(applyto) * (long)this.u / 100L);
         localstatups.put(SecondaryStat.IndiePmdR, new Pair(this.x, localDuration));
         if (applyto.getSkillLevel(162120037) > 0) {
            eff = SkillFactory.getSkill(162120037).getEffect(1);
            localstatups.put(SecondaryStat.IndieSpeed, new Pair(eff.x, localDuration));
            localstatups.put(SecondaryStat.IndieJump, new Pair(eff.y, localDuration));
         }
         break;
      case 162111001:
         eff = SkillFactory.getSkill(162111000).getEffect(this.getLevel());
         localstatups.put(SecondaryStat.IndieJump, new Pair(Integer.valueOf(eff.indieJump), localDuration));
         localstatups.put(SecondaryStat.IndieSpeed, new Pair(Integer.valueOf(eff.indieSpeed), localDuration));
         localstatups.put(SecondaryStat.IndieBooster, new Pair(-1, localDuration));
         break;
      case 162111002:
         localstatups.put(SecondaryStat.IndieSummon, new Pair(1, this.s2 * 1000));
         break;
      case 162111004:
         eff = SkillFactory.getSkill(162111003).getEffect(this.getLevel());
         localstatups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(eff.indieDamR), localDuration));
         break;
      case 162120038:
         if (primary) {
            applyto.setSkillCustomInfo(162120038, applyto.getStat().getCurrentMaxHp() * (long)this.x / 100L, 0L);
            localDuration += this.u * 1000;
         }

         localstatups.put(SecondaryStat.IndieUnk1, new Pair(2, localDuration));
         localstatups.put(SecondaryStat.IndieBarrier, new Pair((int)applyto.getSkillCustomValue0(162120038), localDuration));
         break;
      case 162121003:
         localstatups.put(SecondaryStat.흡수_강, new Pair(1, localDuration));
         break;
      case 162121006:
         localstatups.put(SecondaryStat.흡수_바람, new Pair(1, localDuration));
         break;
      case 162121009:
         localstatups.put(SecondaryStat.흡수_해, new Pair(1, localDuration));
         break;
      case 162121022:
         localstatups.put(SecondaryStat.IndieDamReduceR, new Pair(-this.x, this.q * 1000));
         localstatups.put(SecondaryStat.IndieUnk1, new Pair(1, this.q * 1000));
         localstatups.put(SecondaryStat.AntiMagicShell, new Pair(1, this.q * 1000));
         localstatups.put(SecondaryStat.DreamDowon, new Pair(1, this.q * 1000));
         if (applyto.getSkillLevel(162120038) > 0) {
            SkillFactory.getSkill(162120038).getEffect(1).applyTo(applyto, this.q * 1000);
         }
         break;
      case 162121043:
         localstatups.put(SecondaryStat.IndieStance, new Pair(Integer.valueOf(this.indieStance), localDuration));
         localstatups.put(SecondaryStat.IndieBDR, new Pair(Integer.valueOf(this.indieBDR), localDuration));
         localstatups.put(SecondaryStat.IndieCD, new Pair(this.x, localDuration));
         break;
      case 162121044:
         localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
         break;
      case 164001004:
         bufftimeR = false;
         localstatups.put(SecondaryStat.NewFlying, new Pair(1, 650));
         localstatups.put(SecondaryStat.IndieFloating, new Pair(1, 650));
         break;
      case 164101003:
         applyto.giveHoyoungGauge(this.sourceid);
         localstatups.put(SecondaryStat.Alterego, new Pair(1, localDuration));
         break;
      case 164101006:
         localstatups.put(SecondaryStat.DarkSight, new Pair(Integer.valueOf(this.level), 2000));
         break;
      case 164111007:
      case 164121006:
         applyto.giveHoyoungGauge(this.sourceid);
         break;
      case 164121007:
         applyto.giveHoyoungGauge(this.sourceid);
         localstatups.put(SecondaryStat.IndiePmdR, new Pair(Integer.valueOf(this.indiePmdR), localDuration));
         localstatups.put(SecondaryStat.ButterflyDream, new Pair(1, localDuration));
         break;
      case 164121008:
      case 400041050:
         applyto.giveHoyoungGauge(this.sourceid);
         break;
      case 164121041:
         localstatups.put(SecondaryStat.Sungi, new Pair(1, localDuration));
         break;
      case 164121042:
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, this.y * 1000));
         localstatups.put(SecondaryStat.DreamDowon, new Pair(1, this.y * 1000));
         break;
      case 400001010:
         applyto.setBlitzShield((int)(applyto.getStat().getCurrentMaxHp() * (long)this.x / 100L));
         localstatups.put(SecondaryStat.BlitzShield, new Pair(applyto.getBlitzShield(), localDuration));
         break;
      case 400001014:
      case 400001015:
         localstatups.clear();
         localstatups.put(SecondaryStat.HeavensDoor, new Pair(1, this.x * 1000));
         break;
      case 400001016:
         localstatups.clear();
         localstatups.put(SecondaryStat.DemonDamageAbsorbShield, new Pair((int)applyto.getSkillCustomValue0(400001016), localDuration));
         bufftimeR = false;
         break;
      case 400001017:
         localstatups.clear();
         if (!primary) {
            localstatups.put(SecondaryStat.IndieAllStatR, new Pair(Integer.valueOf(this.indieStatRBasic), 1500));
         }
         break;
      case 400001020:
         applyto.setSkillCustomInfo(2320048, (long)this.v, 0L);
         break;
      case 400001037:
         localstatups.put(SecondaryStat.MagicCircuitFullDrive, new Pair(1, localDuration));
         localstatups.put(SecondaryStat.IndieDamR, new Pair(this.y, localDuration));
         break;
      case 400001042:
         localstatups.clear();
         double d = (double)applyto.getBuffedValue(SecondaryStat.BasicStatUp) / 100.0D;
         double up = Math.floor(d * (double)applyto.getStat().getStr());
         size3 = (int)(up / 100.0D * (double)this.x);
         localstatups.put(SecondaryStat.IndieAllStat, new Pair(size3, localDuration));
         localstatups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(this.indieDamR), localDuration));
         localstatups.put(SecondaryStat.Bless5th2, new Pair(applyfrom.메이플용사, localDuration));
         break;
      case 400001043:
         localstatups.clear();
         localstatups.put(SecondaryStat.IndieDamR, new Pair((int)((long)this.q + applyfrom.getSkillCustomValue0(400001043)), localDuration));
         localstatups.put(SecondaryStat.Bless5th, new Pair(applyfrom.메이플용사, localDuration));
         break;
      case 400001044:
         localstatups.put(SecondaryStat.IndieDamR, new Pair(10, localDuration));
         localstatups.put(SecondaryStat.IndieDamageReduce, new Pair(10, localDuration));
         localstatups.put(SecondaryStat.Bless5th, new Pair(applyfrom.메이플용사, localDuration));
         break;
      case 400001045:
         var29 = applyto.getCooldowns().iterator();

         while(var29.hasNext()) {
            i = (MapleCoolDownValueHolder)var29.next();
            if (i.skillId != this.sourceid && GameConstants.isZero(i.skillId / 10000) && !SkillFactory.getSkill(i.skillId).isHyper() && applyto.skillisCooling(i.skillId)) {
               applyto.removeCooldown(i.skillId);
            }
         }

         localstatups.put(SecondaryStat.Bless5th2, new Pair(1, 0));
         localstatups.put(SecondaryStat.Bless5th, new Pair(applyfrom.메이플용사, localDuration));
         localstatups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(this.indiePad), localDuration));
         break;
      case 400001047:
         localstatups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(this.indieDamR), localDuration));
         localstatups.put(SecondaryStat.IndieStance, new Pair(Integer.valueOf(this.indieStance), localDuration));
         localstatups.put(SecondaryStat.Bless5th, new Pair(applyfrom.메이플용사, localDuration));
         break;
      case 400001048:
         localstatups.put(SecondaryStat.IndiePad, new Pair(Integer.valueOf(this.indiePad), localDuration));
         localstatups.put(SecondaryStat.IndieMad, new Pair(Integer.valueOf(this.indieMad), localDuration));
         localstatups.put(SecondaryStat.Bless5th, new Pair(applyfrom.메이플용사, localDuration));
         break;
      case 400001049:
         localstatups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(this.indieDamR), localDuration));
         localstatups.put(SecondaryStat.IndieStance, new Pair(Integer.valueOf(this.indieStance), localDuration));
         localstatups.put(SecondaryStat.Bless5th, new Pair(applyfrom.메이플용사, localDuration));
         break;
      case 400001050:
         localstatups.put(SecondaryStat.IndiePmdR, new Pair(Integer.valueOf(this.indiePmdR), localDuration));
         localstatups.put(SecondaryStat.Bless5th, new Pair(applyfrom.메이플용사, localDuration));
         break;
      case 400001052:
         localstatups.put(SecondaryStat.NewFlying, new Pair(1, (int)SkillFactory.getSkill(400001007).getEffect(this.level).t * 1000));
         break;
      case 400001061:
         localstatups.put(SecondaryStat.Lotus, new Pair(1, localDuration));
         localstatups.put(SecondaryStat.HeavensDoor, new Pair(1, localDuration));
         break;
      case 400001062:
         bufftimeR = false;
         cancel = false;
         localDuration = 3500;
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, localDuration));
         break;
      case 400011003:
         bounds = this.calculateBoundingBox(applyfrom.getTruePosition(), applyfrom.isFacingLeft());
         list = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.PLAYER));
         exit = false;
         if (applyfrom.getParty() != null) {
            Iterator var53 = list.iterator();

            while(var53.hasNext()) {
               MapleMapObject affectedmo = (MapleMapObject)var53.next();
               MapleCharacter affected = (MapleCharacter)affectedmo;
               if (affected.getParty() != null && applyfrom.getId() != affected.getId() && applyfrom.getParty().getId() == affected.getParty().getId()) {
                  localstatups.clear();
                  localstatups.put(SecondaryStat.HolyUnity, new Pair(affected.getId(), localDuration));
                  affected.getMap().broadcastMessage(affected, CField.EffectPacket.showEffect(affected, 0, this.sourceid, 1, 0, 0, (byte)(affected.getTruePosition().x <= pos.x ? 0 : 1), false, pos, (String)null, (Item)null), false);
                  applyto.setSkillCustomInfo(400011003, (long)affected.getId(), 0L);
                  SkillFactory.getSkill(400011021).getEffect(applyto.getSkillLevel(400011003)).applyTo(applyfrom, affected, primary, pos, newDuration, (byte)0, false);
                  exit = true;
                  break;
               }
            }
         }

         if (!exit) {
            applyto.setSkillCustomInfo(400011003, 0L, 0L);
            localstatups.clear();
            localstatups.put(SecondaryStat.HolyUnity, new Pair(applyfrom.getId(), localDuration));
         }
         break;
      case 400011006:
         localstatups.clear();
         localstatups.put(SecondaryStat.BonusAttack, new Pair(1, localDuration));
         localstatups.put(SecondaryStat.IndieCr, new Pair(Integer.valueOf(this.indieCr), localDuration));
         localstatups.put(SecondaryStat.IndiePmdR, new Pair(Integer.valueOf(this.indiePmdR), localDuration));
         break;
      case 400011010:
         if (!primary) {
            aftercancel = true;
         }

         value = Math.round((float)((100 - applyto.getStat().getHPPercent()) / this.u)) * this.x;
         localDuration = 0;
         if (value <= 0) {
            value = 1;
         }

         localstatups.put(SecondaryStat.DemonFrenzy, new Pair(value, localDuration));
         break;
      case 400011011:
         localstatups.clear();
         if (applyto.getId() != applyfrom.getId()) {
            localstatups.put(SecondaryStat.RhoAias, new Pair(SkillFactory.getSkill(400011011).getEffect(applyfrom.getSkillLevel(400011011)).getX(), localDuration));
         } else if (applyfrom.getBuffedEffect(SecondaryStat.RhoAias) == null && primary) {
            applyfrom.setRhoAias(this.y + this.w + this.z);
            localstatups.put(SecondaryStat.RhoAias, new Pair(this.x, localDuration));
         } else {
            applyfrom.cancelEffect(applyfrom.getBuffedEffect(400011011), (List)null, true);
            localstatups.put(SecondaryStat.RhoAias, new Pair(0, this.q2 * 1000));
            localstatups.put(SecondaryStat.IndiePmdR, new Pair(this.u, this.q2 * 1000));
            applyfrom.setRhoAias(0);
            if (applyto.getParty() != null) {
               var29 = applyto.getParty().getMembers().iterator();

               while(var29.hasNext()) {
                  MaplePartyCharacter pc = (MaplePartyCharacter)var29.next();
                  MapleCharacter victim;
                  if (pc.isOnline() && pc.getMapid() == applyto.getMapId() && pc.getChannel() == applyto.getClient().getChannel() && (victim = applyto.getClient().getChannelServer().getPlayerStorage().getCharacterByName(pc.getName())) != null && victim.getBuffedValue(400011011)) {
                     victim.cancelEffect(victim.getBuffedEffect(400011011));
                  }
               }
            }
         }
         break;
      case 400011016:
         starttime = System.currentTimeMillis();
         applyfrom.setCombo((short)Math.min(999, applyfrom.getCombo() + this.z));
         applyfrom.setLastCombo(starttime);
         s = applyfrom.getCombo();
         FreudsProtection = s / 50;
         applyfrom.getClient().getSession().writeAndFlush(CField.aranCombo(s));
         if (applyfrom.getSkillLevel(21000000) > 0 && FreudsProtection != s / 50) {
            SkillFactory.getSkill(21000000).getEffect(applyfrom.getSkillLevel(21000000)).applyTo(applyfrom, false);
         }

         localstatups.put(SecondaryStat.InstallMaha, new Pair(Integer.valueOf(this.level), localDuration));
         localstatups.put(SecondaryStat.IndiePadR, new Pair(Integer.valueOf(this.indiePadR), localDuration));
         break;
      case 400011021:
         localstatups.clear();
         localstatups.put(SecondaryStat.HolyUnity, new Pair(applyfrom.getId(), localDuration));
         break;
      case 400011027:
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 3000));
         break;
      case 400011038:
         localstatups.clear();
         localstatups.put(SecondaryStat.BloodFist, new Pair(1, localDuration));
         applyto.addCooldown(400011038, System.currentTimeMillis(), 12000L);
         applyto.getClient().getSession().writeAndFlush(CField.skillCooldown(400011038, 12000));
         break;
      case 400011047:
         indieList1.add(new Pair(!applyto.getBuffedValue(1301006) ? 0 : 1, !applyto.getBuffedValue(1301007) ? 0 : 1));
         if (!primary) {
            localstatups.put(SecondaryStat.IndieBarrier, new Pair((int)applyto.getSkillCustomValue0(400011048), (int)applyto.getBuffLimit(400011047)));
            localstatups.put(SecondaryStat.DarknessAura, new Pair(this.w, (int)applyto.getBuffLimit(400011047)));
         } else {
            applyto.removeSkillCustomInfo(400011047);
            applyto.removeSkillCustomInfo(400011048);
            localstatups.put(SecondaryStat.IndieBarrier, new Pair(0, localDuration));
            localstatups.put(SecondaryStat.DarknessAura, new Pair(this.w, localDuration));
         }
         break;
      case 400011052:
         aftercancel = true;
         localstatups.put(SecondaryStat.BlessedHammer, new Pair(applyto.getElementalCharge(), 0));
         break;
      case 400011053:
         localstatups.put(SecondaryStat.BlessedHammer2, new Pair(applyto.getElementalCharge(), SkillFactory.getSkill(400011052).getEffect(this.level).getV() * 1000));
         break;
      case 400011055:
         if (applyfrom.getBuffedValue(this.sourceid)) {
            return;
         }
         break;
      case 400011058:
         applyto.ignoreDraco = this.q2;
         localDuration = 30000;
         applyto.removeCooldown(400011079);
         localstatups.put(SecondaryStat.WillofSwordStrike, new Pair(applyto.ignoreDraco, localDuration));
         break;
      case 400011083:
         if (applyto.getBuffedValue(400011083)) {
            localstatups.clear();
            aftercancel = true;
            localstatups.put(SecondaryStat.SwordOfSoulLight, new Pair(1, localDuration));
         } else {
            localstatups.put(SecondaryStat.IndiePadR, new Pair(Integer.valueOf(this.indiePadR), localDuration));
            localstatups.put(SecondaryStat.IndieCr, new Pair(Integer.valueOf(this.indieCr), localDuration));
            localstatups.put(SecondaryStat.IndieIgnoreMobPdpR, new Pair(Integer.valueOf(this.indieIgnoreMobpdpR), localDuration));
            localstatups.put(SecondaryStat.SwordOfSoulLight, new Pair(2, localDuration));
         }
         break;
      case 400011088:
         SkillFactory.getSkill(400011089).getEffect(1).applyTo(applyto);
         break;
      case 400011089:
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 2500));
         if (applyto.getBuffedValue(400011088)) {
            skills = new ArrayList();
            skills.add(new RangeAttack(400011089, applyto.getPosition(), 0, 0, 5));
            applyto.getClient().getSession().writeAndFlush(CField.rangeAttack(400011088, skills));
            applyto.cancelEffect(applyto.getBuffedEffect(400011088));
         }
         break;
      case 400011091:
         localstatups.clear();
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 0));
         break;
      case 400011102:
         localstatups.put(SecondaryStat.DevilishPower, new Pair(Integer.valueOf(this.level), localDuration));
         break;
      case 400011108:
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, this.x));
         break;
      case 400011111:
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 3000));
         break;
      case 400011112:
         localstatups.put(SecondaryStat.Revenant, new Pair(1, localDuration));
         break;
      case 400011116:
         localstatups.put(SecondaryStat.AfterImageShock, new Pair((int)applyto.getSkillCustomValue0(400011116), localDuration));
         break;
      case 400011118:
         localstatups.put(SecondaryStat.DevilishPower, new Pair(1, localDuration));
         break;
      case 400011121:
         localstatups.put(SecondaryStat.BlizzardTempest, new Pair(1, this.q * 1000));
         break;
      case 400011123:
         localstatups.put(SecondaryStat.BlizzardTempest, new Pair(1, localDuration));
         break;
      case 400011127:
         if (!primary) {
            aftercancel = true;
            if (applyto.getBuffedEffect(SecondaryStat.IndieBarrier) == null) {
               localDuration = 10000;
            }

            showEffect = false;
            localstatups.clear();
            localstatups.put(SecondaryStat.IndieBarrier, new Pair((int)applyto.getSkillCustomValue0(400011127), localDuration));
         } else {
            applyto.setSkillCustomInfo(400011127, (long)((int)(applyto.getStat().getCurrentMaxHp() / 100L * (long)this.x)), 0L);
            localstatups.put(SecondaryStat.IndieDamR, new Pair(this.z, this.y * 1000));
            localstatups.put(SecondaryStat.IndieBarrier, new Pair((int)applyto.getSkillCustomValue0(400011127), localDuration));
            SkillFactory.getSkill(400011127).getEffect(applyto.getSkillLevel(400011127)).applyTo(applyto, false);
            bounds = this.calculateBoundingBox(applyfrom.getTruePosition(), applyfrom.isFacingLeft());
            list = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.PLAYER));
            if (applyfrom.getParty() != null) {
               var38 = list.iterator();

               while(var38.hasNext()) {
                  affectedmo = (MapleMapObject)var38.next();
                  affected = (MapleCharacter)affectedmo;
                  if (affected.getParty() != null && applyfrom.getId() != affected.getId() && applyfrom.getParty().getId() == affected.getParty().getId()) {
                     affected.setSkillCustomInfo(400011127, (long)((int)(affected.getStat().getCurrentMaxHp() / 100L * (long)this.x)), 0L);
                     SkillFactory.getSkill(400011127).getEffect(applyto.getSkillLevel(400011127)).applyTo(applyto, affected, false, pos, newDuration, (byte)0, false);
                  }
               }
            }
         }
         break;
      case 400011129:
         aftercancel = true;
         localstatups.put(SecondaryStat.RevenantDamage, new Pair(1, 0));
         break;
      case 400011134:
         localstatups.put(SecondaryStat.EgoWeapon, new Pair(1, 2000));
         break;
      case 400011136:
         localstatups.put(SecondaryStat.DevilishPower, new Pair(applyto.활성화된소드, localDuration));
         var29 = applyto.getMap().getAllMagicSword().iterator();

         while(var29.hasNext()) {
            MapleMagicSword mSword = (MapleMagicSword)var29.next();
            if (mSword.getChr().getId() == applyto.getId()) {
               applyto.getMap().removeMapObject(mSword);
               if (mSword.getSchedule() != null) {
                  mSword.getSchedule().cancel(true);
                  mSword.setSchedule((ScheduledFuture)null);
               }
            }
         }

         applyto.활성화된소드 = 0;
         break;
      case 400011142:
         localstatups.put(SecondaryStat.Cosmos, new Pair(applyfrom.getCosmicCount(), this.u * 1000));
         break;
      case 400020009:
         applyto.cancelEffectFromBuffStat(SecondaryStat.PsychicTornado);
         showEffect = false;
         break;
      case 400021003:
         if (primary) {
            var29 = applyto.getCooldowns().iterator();

            while(var29.hasNext()) {
               i = (MapleCoolDownValueHolder)var29.next();
               if (i.skillId != 400021003 && i.skillId == 400021132) {
               }
            }
         } else {
            bufftimeR = false;
         }

         localstatups.clear();
         if (!showEffect) {
            localDuration = SkillFactory.getSkill(400021003).getEffect(applyto.getSkillLevel(400021003)).duration;
         }

         localstatups.put(SecondaryStat.Pray, new Pair(Integer.valueOf(this.level), localDuration));
         break;
      case 400021005:
         value = !applyto.getBuffedValue(20040220) ? 20040219 : 20040220;
         long duration = applyto.getBuffLimit(value);

         while(applyto.getBuffedEffect(SecondaryStat.Larkness) != null) {
            applyto.cancelEffect(applyto.getBuffedEffect(SecondaryStat.Larkness));
         }

         applyto.setUseTruthDoor(true);
         SkillFactory.getSkill(20040220).getEffect(1).applyTo(applyfrom, applyto, false, pos, (int)duration, (byte)0, true);
         break;
      case 400021006:
         localstatups.clear();
         applyto.setSkillCustomInfo(this.sourceid, (long)applyfrom.getId(), 0L);
         localstatups.put(SecondaryStat.UnionAura, new Pair(applyfrom.getSkillLevel(400021006), localDuration));
         localstatups.put(SecondaryStat.YellowAura, new Pair(applyfrom.getSkillLevel(32001016), localDuration));
         localstatups.put(SecondaryStat.DrainAura, new Pair(applyfrom.getSkillLevel(32101009), localDuration));
         localstatups.put(SecondaryStat.BlueAura, new Pair(applyfrom.getSkillLevel(32111012), localDuration));
         localstatups.put(SecondaryStat.DarkAura, new Pair(applyfrom.getSkillLevel(32121017), localDuration));
         if (primary) {
            localstatups.put(SecondaryStat.DebuffAura, new Pair(applyfrom.getSkillLevel(32121018), localDuration));
            localstatups.put(SecondaryStat.IndieMad, new Pair(Integer.valueOf(this.indieMad), localDuration));
         }

         localstatups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(SkillFactory.getSkill(32121017).getEffect(applyfrom.getSkillLevel(32121017)).indieDamR), localDuration));
         localstatups.put(SecondaryStat.IndieSpeed, new Pair(Integer.valueOf(SkillFactory.getSkill(32001016).getEffect(applyfrom.getSkillLevel(32001016)).indieSpeed), localDuration));
         localstatups.put(SecondaryStat.IndieBooster, new Pair(-1, localDuration));
         localstatups.put(SecondaryStat.IndieBDR, new Pair(Integer.valueOf(SkillFactory.getSkill(32120060).getEffect(applyfrom.getSkillLevel(32120060)).indieBDR), localDuration));
         localstatups.put(SecondaryStat.IndieAsrR, new Pair(Integer.valueOf(SkillFactory.getSkill(32111012).getEffect(applyfrom.getSkillLevel(32111012)).asrR), localDuration));
         if (applyto.getBuffedValue(32001016)) {
            applyto.cancelEffectFromBuffStat(SecondaryStat.YellowAura, 32001016);
         }

         if (applyto.getBuffedValue(32101009)) {
            applyto.cancelEffectFromBuffStat(SecondaryStat.DrainAura, 32101009);
         }

         if (applyto.getBuffedValue(32111012)) {
            applyto.cancelEffectFromBuffStat(SecondaryStat.BlueAura, 32111012);
         }

         if (applyto.getBuffedValue(32121017)) {
            applyto.cancelEffectFromBuffStat(SecondaryStat.DarkAura, 32121017);
         }

         if (applyto.getBuffedValue(32121018) && applyto.getBuffedOwner(32121018) != applyto.getId()) {
            applyto.cancelEffectFromBuffStat(SecondaryStat.DebuffAura, 32121018);
         }
         break;
      case 400021008:
         if (!applyto.getBuffedValue(400021008)) {
            applyto.setSkillCustomInfo(400021009, 1L, 0L);
            applyto.setSkillCustomInfo(400021008, 0L, 0L);
         }

         if (applyto.getSkillCustomValue0(400021009) >= 3L) {
            applyto.setSkillCustomInfo(400021009, 3L, 0L);
         }

         localstatups.put(SecondaryStat.PsychicTornado, new Pair((int)applyto.getSkillCustomValue0(400021009), localDuration));
         break;
      case 400021012:
         localDuration = 10000;
         SecondaryStatValueHolder mbsvh = applyto.checkBuffStatValueHolder(SecondaryStat.IndiePmdR, 400021012);
         if (mbsvh == null) {
            localstatups.put(SecondaryStat.IndiePmdR, new Pair(5, localDuration));
         } else {
            localstatups.put(SecondaryStat.IndiePmdR, new Pair(mbsvh.value + 5, localDuration));
         }
         break;
      case 400021032:
      case 400021033:
         if (applyto.getBuffedValue(2321003)) {
            applyto.cancelEffectFromBuffStat(SecondaryStat.IndieSummon, 2321003);
         }
         break;
      case 400021047:
         aftercancel = true;
         break;
      case 400021052:
         localstatups.put(SecondaryStat.DamR, new Pair(Math.min(this.w + applyto.getStat().getTotalInt() / this.x, this.z), localDuration));
         break;
      case 400021061:
         localstatups.clear();
         localstatups.put(SecondaryStat.KeyDownMoving, new Pair(100, 0));
         break;
      case 400021068:
         localstatups.clear();
         localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
         break;
      case 400021070:
         applyfrom.peaceMaker = !primary ? --applyfrom.peaceMaker : this.w;
         applyto.addHP(applyfrom.getStat().getCurrentMaxHp() * 100L / (long)this.hp);
         localstatups.put(SecondaryStat.IndieDamR, new Pair(this.q2 + applyfrom.peaceMaker * this.w2, localDuration));
         break;
      case 400021071:
         if (primary) {
            aftercancel = true;
            localstatups.put(SecondaryStat.LuminousPerfusion, new Pair(applyto.getPerfusion(), 0));
         } else if (applyto.getPerfusion() >= this.x - 1) {
            if (applyto.getPerfusion() == this.x - 1) {
               localstatups.clear();
               applyto.setPerfusion(0);
               applyto.cancelEffect(this);
               if (applyto.getCooldownLimit(400021071) > 0L) {
                  applyto.removeCooldown(400021071);
               }
            }
         } else {
            applyto.setPerfusion(applyto.getPerfusion() + 1);
            aftercancel = true;
            localstatups.put(SecondaryStat.LuminousPerfusion, new Pair(applyto.getPerfusion(), 0));
         }
         break;
      case 400021073:
         localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
      case 400021077:
      case 400051045:
         break;
      case 400021087:
         localstatups.clear();
         localstatups.put(SecondaryStat.AbyssalLightning, new Pair(1, localDuration));
         SkillFactory.getSkill(400021088).getEffect(this.level).applyTo(applyto, false);
         break;
      case 400021088:
         localstatups.clear();
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 2000));
         break;
      case 400021092:
         localstatups.clear();
         localstatups.put(SecondaryStat.SalamanderMischief, new Pair(1, localDuration));
         break;
      case 400021093:
         localstatups.put(SecondaryStat.IndieMad, new Pair((int)((long)SkillFactory.getSkill(400021092).getEffect(applyfrom.getSkillLevel(400021092)).dot + applyto.getSkillCustomValue0(400021093) * (long)SkillFactory.getSkill(400021092).getEffect(applyfrom.getSkillLevel(400021092)).w2), localDuration));
         break;
      case 400021096:
         localstatups.put(SecondaryStat.LawOfGravity, new Pair(1, localDuration));
         break;
      case 400021099:
         if (applyto.getBuffedValue(400021099)) {
            localstatups.put(SecondaryStat.CrystalGate, new Pair(1, (int)applyto.getBuffLimit(400021099)));
         } else {
            localstatups.put(SecondaryStat.CrystalGate, new Pair(1, localDuration));
         }
         break;
      case 400021100:
         localstatups.put(SecondaryStat.IndieMad, new Pair(Integer.valueOf(this.indieMad), localDuration));
         break;
      case 400021105:
         localstatups.clear();
         up = (int)applyto.getSkillCustomValue0(400021107) * 1000;
         i = (int)applyto.getSkillCustomValue0(400021108) * 1000;
         FreudsProtection = 1 + up + i;
         if (up == i) {
            ++FreudsProtection;
         }

         HashMap<SecondaryStat, Pair<Integer, Integer>> hashMap = new HashMap();
         hashMap.put(SecondaryStat.LiberationOrb, new Pair(1, 0));
         applyto.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.cancelBuff(hashMap, applyto));
         applyto.setSkillCustomInfo(400021105, (long)(FreudsProtection % 10), 0L);
         applyto.setSkillCustomInfo(400021110, (long)this.v2, 0L);
         applyto.removeSkillCustomInfo(400021107);
         applyto.removeSkillCustomInfo(400021108);
         localstatups.put(SecondaryStat.LiberationOrbActive, new Pair(FreudsProtection, localDuration));
         break;
      case 400031005:
         localstatups.put(SecondaryStat.BonusAttack, new Pair(1, localDuration));
         break;
      case 400031006:
         if (primary) {
            applyto.trueSniping = this.x;
         }

         localstatups.put(SecondaryStat.TrueSniping, new Pair(applyto.trueSniping, !primary ? (int)applyto.getBuffLimit(this.sourceid) : localDuration));
         break;
      case 400031012:
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(this.x, 10000));
         break;
      case 400031014:
         if (applyto.getBuffedEffect(SecondaryStat.RideVehicle) == null) {
            SkillFactory.getSkill(33001001).getEffect(1).applyTo(applyto, 0);
            localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(this.x, 2000));
         }
         break;
      case 400031017:
         localstatups.put(SecondaryStat.RideVehicleExpire, new Pair(parseMountInfo(applyto, this.sourceid), localDuration));
         localstatups.put(SecondaryStat.IgnisRore, new Pair(10, localDuration));
         break;
      case 400031028:
         if (applyto.getBuffedEffect(SecondaryStat.AdvancedQuiver) != null) {
            applyto.cancelEffectFromBuffStat(SecondaryStat.AdvancedQuiver);
         }

         localstatups.put(SecondaryStat.QuiverFullBurst, new Pair(this.x, localDuration));
         break;
      case 400031030:
         localstatups.clear();
         localstatups.put(SecondaryStat.WindWall, new Pair(this.w, localDuration));
         break;
      case 400031034:
         if (applyto.getCooldownLimit(400031034) != 0L) {
            if (applyto.getBuffedEffect(SecondaryStat.NextAttackEnhance) == null) {
               value = (int)applyto.getSkillCustomValue0(this.sourceid);
               applyto.removeSkillCustomInfo(this.sourceid);
               if (value != 1000) {
                  if (value < 750) {
                     if (value < 500) {
                        if (value >= 250) {
                           localstatups.put(SecondaryStat.NextAttackEnhance, new Pair(25, 1000));
                        }
                     } else {
                        localstatups.put(SecondaryStat.NextAttackEnhance, new Pair(50, 1000));
                     }
                  } else {
                     localstatups.put(SecondaryStat.NextAttackEnhance, new Pair(75, 1000));
                  }
               } else {
                  localstatups.put(SecondaryStat.NextAttackEnhance, new Pair(100, 1000));
               }
            }
         } else {
            applyto.setSkillCustomInfo(this.sourceid, (long)applyto.렐릭게이지, 0L);
            localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 1200));
            localstatups.put(SecondaryStat.IgnorePCounter, new Pair(1, 1200));
            MapleCharacter.렐릭게이지(applyto.getClient(), this.sourceid);
         }
         break;
      case 400031036:
      case 400031037:
      case 400031038:
      case 400031039:
      case 400031040:
      case 400031041:
      case 400031042:
      case 400031043:
      case 400031067:
         if (this.sourceid == 400031038) {
            localstatups.put(SecondaryStat.IndieUnkIllium, new Pair(1, localDuration));
            localstatups.put(SecondaryStat.IndieDamReduceR, new Pair(-this.x, localDuration));
         }

         if (this.sourceid != 400031042) {
            if (this.sourceid != 400031039) {
               if (this.sourceid == 400031040) {
                  localstatups.put(SecondaryStat.IndieUnkIllium, new Pair(1, localDuration));
               }
            } else {
               localstatups.put(SecondaryStat.IndieUnkIllium, new Pair(1, localDuration));
            }
         } else {
            localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
            localstatups.put(SecondaryStat.IndieFloating, new Pair(1, localDuration));
         }

         if (applyfrom.getId() == applyto.getId()) {
            MapleCharacter.렐릭게이지(applyto.getClient(), this.sourceid);
            MapleCharacter.문양(applyto.getClient(), this.sourceid);
         }
         break;
      case 400031044:
         showEffect = false;
         localstatups.clear();
         if (!primary) {
            localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 1600));
         } else {
            localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 2600));
            localstatups.put(SecondaryStat.RoyalKnights, new Pair(1, localDuration));
         }
         break;
      case 400031047:
      case 400031049:
         MapleCharacter.렐릭게이지(applyto.getClient(), this.sourceid);
         MapleCharacter.문양(applyto.getClient(), this.sourceid);
         break;
      case 400031048:
         localDuration = 4000;
         showEffect = false;
         localstatups.put(SecondaryStat.RelikUnboundDischarge, new Pair(1, localDuration));
         break;
      case 400031050:
         showEffect = false;
         break;
      case 400031051:
         if (!primary) {
            return;
         }

         MapleCharacter.렐릭게이지(applyto.getClient(), this.sourceid);
         MapleCharacter.문양(applyto.getClient(), this.sourceid);
         break;
      case 400031053:
         localDuration = 0;
         aftercancel = true;
         localstatups.put(SecondaryStat.SilhouetteMirage, new Pair(1, 0));
         break;
      case 400031055:
         applyto.repeatingCrossbowCatridge = this.x;
         localstatups.put(SecondaryStat.RepeatingCrossbowCatridge, new Pair(this.x, localDuration));
         break;
      case 400031062:
         localstatups.clear();
         localstatups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(this.indieDamR), localDuration));
         localstatups.put(SecondaryStat.IndieStance, new Pair(Integer.valueOf(this.indieStance), localDuration));
         localstatups.put(SecondaryStat.ThanatosDescent, new Pair(1, localDuration));
         break;
      case 400031064:
         localstatups.clear();
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 6720));
         break;
      case 400031066:
         aftercancel = true;
         localstatups.clear();
         localstatups.put(SecondaryStat.GripOfAgony, new Pair((int)applyfrom.getSkillCustomValue0(400031066), 0));
         break;
      case 400041002:
         aftercancel = true;
         showEffect = false;
         localstatups.put(SecondaryStat.ShadowAssult, new Pair(3, 0));
         if (applyfrom.getSkillCustomValue0(400041002) > 0L) {
            applyfrom.removeCooldown(400041002);
         }

         applyfrom.setSkillCustomInfo(400041002, 3L, 0L);
         break;
      case 400041003:
         aftercancel = true;
         showEffect = false;
         localstatups.put(SecondaryStat.ShadowAssult, new Pair(2, 0));
         applyfrom.setSkillCustomInfo(400041002, 2L, 0L);
         break;
      case 400041004:
         aftercancel = true;
         showEffect = false;
         localstatups.put(SecondaryStat.ShadowAssult, new Pair(1, 0));
         applyfrom.setSkillCustomInfo(400041002, 1L, 0L);
         break;
      case 400041005:
         showEffect = false;
         applyfrom.setSkillCustomInfo(400041002, 0L, 0L);
         applyto.cancelEffectFromBuffStat(SecondaryStat.ShadowAssult);
         break;
      case 400041007:
         localstatups.clear();
         if (!primary) {
            localstatups.put(SecondaryStat.MegaSmasher, new Pair(1, localDuration));
            localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, localDuration));
         } else {
            localstatups.put(SecondaryStat.MegaSmasher, new Pair(-1, 90000));
         }
         break;
      case 400041008:
         if (applyto.getBuffedValue(400041008)) {
            return;
         }

         localstatups.put(SecondaryStat.ShadowSpear, new Pair(Integer.valueOf(this.level), localDuration));
         break;
      case 400041009:
         if (!primary) {
            localstatups.put(SecondaryStat.NotDamaged, new Pair(1, 3000));
         } else {
            localstatups.put(SecondaryStat.IndieDamageReduce, new Pair(this.y, 0));
            localstatups.put(SecondaryStat.KeyDownMoving, new Pair(80, 5500));
         }
         break;
      case 400041011:
         localstatups.put(SecondaryStat.IndieHpR, new Pair(Integer.valueOf(this.getIndieMhpR()), localDuration));
         break;
      case 400041012:
         localstatups.put(SecondaryStat.DamageDecreaseWithHP, new Pair(this.z, localDuration));
         localstatups.put(SecondaryStat.IndieAsrR, new Pair(Integer.valueOf(this.indieAsrR), localDuration));
         break;
      case 400041013:
         localstatups.put(SecondaryStat.IndieReduceCooltime, new Pair(this.x, localDuration));
         break;
      case 400041014:
         localstatups.put(SecondaryStat.IndiePmdR, new Pair(Integer.valueOf(this.indiePmdR), localDuration));
         break;
      case 400041015:
         localstatups.put(SecondaryStat.IndieHpR, new Pair(Integer.valueOf(this.getIndieMhpR()), localDuration));
         localstatups.put(SecondaryStat.DamageDecreaseWithHP, new Pair(this.z, localDuration));
         localstatups.put(SecondaryStat.IndieAsrR, new Pair(Integer.valueOf(this.indieAsrR), localDuration));
         localstatups.put(SecondaryStat.IndieReduceCooltime, new Pair(this.x, localDuration));
         localstatups.put(SecondaryStat.IndiePmdR, new Pair(Integer.valueOf(this.indiePmdR), localDuration));
         break;
      case 400041025:
      case 400041026:
      case 400041027:
         localstatups.put(SecondaryStat.NotDamaged, new Pair(1, this.s * 1000));
         break;
      case 400041032:
         localstatups.clear();
         if (!applyto.getBuffedValue(400041032) && applyto.getKeyValue(1544, "400041032") != 1L) {
            localstatups.put(SecondaryStat.IndieEvasion, new Pair(this.x, localDuration));
            localstatups.put(SecondaryStat.IndiePmdR, new Pair(this.y, localDuration));
            localstatups.put(SecondaryStat.IndieShotDamage, new Pair(this.z, localDuration));
            localstatups.put(SecondaryStat.ReadyToDie, new Pair(1, localDuration));
         } else {
            localstatups.put(SecondaryStat.IndieEvasion, new Pair(this.w, localDuration));
            localstatups.put(SecondaryStat.IndiePmdR, new Pair(this.q, localDuration));
            localstatups.put(SecondaryStat.IndieShotDamage, new Pair(this.s, localDuration));
            localstatups.put(SecondaryStat.ReadyToDie, new Pair(2, localDuration));
         }
         break;
      case 400041037:
         localstatups.put(SecondaryStat.IndiePmdR, new Pair(applyto.shadowBite, localDuration));
         applyto.shadowBite = 0;
         break;
      case 400041040:
         localstatups.clear();
         applyto.cancelEffect(this);
         if (!primary) {
            aftercancel = true;
            if (applyto.getMarkofPhantom() < this.x) {
               applyto.setMarkofPhantom(applyto.getMarkofPhantom() + 1);
            }

            localstatups.put(SecondaryStat.MarkOfPhantomDebuff, new Pair(applyto.getMarkofPhantom(), 0));
            localstatups.put(SecondaryStat.MarkOfPhantomStack, new Pair(applyto.getMarkofPhantom(), 0));
         } else {
            localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 2000));
            MapleMonster mob = applyto.getMap().getMonsterByOid(applyto.getMarkOfPhantomOid());
            applyto.setMarkofPhantom(0);
            applyto.setMarkOfPhantomOid(0);
            aftercancel = true;
            pos = mob == null ? applyto.getTruePosition() : mob.getTruePosition();
            remove = new ArrayList();
            remove.add(new RangeAttack(400041045, pos, 0, 0, 0));
            remove.add(new RangeAttack(400041045, pos, 0, 0, 0));
            remove.add(new RangeAttack(400041045, pos, 0, 0, 0));
            remove.add(new RangeAttack(400041045, pos, 0, 0, 0));
            remove.add(new RangeAttack(400041045, pos, 0, 0, 0));
            remove.add(new RangeAttack(400041045, pos, 0, 0, 0));
            remove.add(new RangeAttack(400041045, pos, 0, 0, 0));
            remove.add(new RangeAttack(400041046, pos, 0, 0, 0));
            applyto.getClient().getSession().writeAndFlush(CField.rangeAttack(400041040, remove));
         }
         break;
      case 400041047:
         localstatups.put(SecondaryStat.IndieDamR, new Pair(SkillFactory.getSkill(400041044).getEffect(applyfrom.getSkillLevel(400041044)).getS(), localDuration));
         break;
      case 400041048:
         localstatups.put(SecondaryStat.AltergoReinforce, new Pair(1, localDuration));
         break;
      case 400041053:
         applyto.cancelEffectFromBuffStat(SecondaryStat.SageWrathOfGods, 400041052);
         applyto.cancelEffectFromBuffStat(SecondaryStat.IndieSummon, 400041052);
         aftercancel = false;
         if (!primary) {
            localstatups.put(SecondaryStat.AdventOfGods, new Pair(1, 30000));
         } else {
            localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 5000));
            this.applyTo(applyto, false);
         }
         break;
      case 400041057:
         localstatups.put(SecondaryStat.PhotonRay, new Pair(1, localDuration));
         break;
      case 400041061:
         applyto.throwBlasting = this.x;
         localstatups.put(SecondaryStat.ThrowBlasting, new Pair(this.x, localDuration));
         break;
      case 400041063:
         localstatups.put(SecondaryStat.SageElementalClone, new Pair(1, localDuration));
         break;
      case 400041089:
         localstatups.clear();
         localstatups.put(SecondaryStat.ResonateUltimatum, new Pair(1, 50000));
         break;
      case 400051000:
         if (!primary) {
            aftercancel = true;
            localstatups.put(SecondaryStat.SelectDice, new Pair(localDuration, 0));
         }
         break;
      case 400051001:
         localstatups.clear();
         if (applyto.getBuffedValue(SecondaryStat.SelectDice) == null) {
            applyto.getMap().broadcastMessage(applyto, CField.EffectPacket.showDiceEffect(applyto, 0, 5111007, 2, -1, false), false);
            applyto.getClient().getSession().writeAndFlush(CField.EffectPacket.showDiceEffect(applyto, 0, 5111007, 2, -1, true));
            applyto.dropMessage(5, "로디드 다이스가 적용되어 있지 않아 2번 효과가 적용 됩니다.");
            localstatups.put(SecondaryStat.DiceRoll, new Pair(2, localDuration));
         } else {
            applyto.getMap().broadcastMessage(applyto, CField.EffectPacket.showDiceEffect(applyto, 0, 5111007, applyto.getBuffedValue(SecondaryStat.SelectDice), -1, false), false);
            applyto.getClient().getSession().writeAndFlush(CField.EffectPacket.showDiceEffect(applyto, 0, 5111007, applyto.getBuffedValue(SecondaryStat.SelectDice), -1, true));
            localstatups.put(SecondaryStat.DiceRoll, new Pair(applyto.getBuffedValue(SecondaryStat.SelectDice), localDuration));
         }
         break;
      case 400051002:
         applyto.transformEnergyOrb = this.w;
         localstatups.put(SecondaryStat.IndiePmdR, new Pair(Integer.valueOf(this.indiePmdR), localDuration));
         localstatups.put(SecondaryStat.Transform, new Pair(this.w, localDuration));
         break;
      case 400051003:
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 1500));
         break;
      case 400051010:
         var29 = applyto.getCooldowns().iterator();

         while(var29.hasNext()) {
            i = (MapleCoolDownValueHolder)var29.next();
            if (SkillFactory.getSkill(i.skillId) != null && !SkillFactory.getSkill(i.skillId).isHyper() && GameConstants.isEunWol(i.skillId / 10000) && !SkillFactory.getSkill(i.skillId).isNotCooltimeReset()) {
               applyto.removeCooldown(i.skillId);
            }
         }

         localstatups.put(SecondaryStat.IndiePmdR, new Pair(Integer.valueOf(this.indiePmdR), localDuration));
         localstatups.put(SecondaryStat.BonusAttack, new Pair(Integer.valueOf(this.level), localDuration));
         break;
      case 400051011:
         localstatups.clear();
         if (!showEffect) {
            if (applyfrom.getBuffedValue(SecondaryStat.IndieNotDamaged) == null) {
               value = applyto.getEnergyBurst() < 50 ? (applyto.getEnergyBurst() < 25 ? 0 : 1) : 2;
               localDuration = 6000 + this.s * value * 1000;
               applyto.setEnergyBurst(0);
               localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, localDuration));
            }
         } else {
            localstatups.put(SecondaryStat.EnergyBurst, new Pair(1, localDuration));
         }
         break;
      case 400051021:
         localDuration = 2000;
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, localDuration));
         break;
      case 400051024:
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 750));
         break;
      case 400051025:
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 1800));
         break;
      case 400051027:
         localstatups.put(SecondaryStat.IndieAsrR, new Pair(this.indieAsrR * localDuration, 10000));
         localstatups.put(SecondaryStat.IndieCr, new Pair(this.indieCr * localDuration, 10000));
         localstatups.put(SecondaryStat.IndieStance, new Pair(this.indieStance * localDuration, 10000));
         localstatups.put(SecondaryStat.IndiePmdR, new Pair(this.indiePmdR * localDuration, 10000));
         localstatups.put(SecondaryStat.BonusAttack, new Pair(localDuration, 10000));
         break;
      case 400051040:
         localDuration = 1320;
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, localDuration));
         break;
      case 400051041:
         localstatups.clear();
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 10000));
         localstatups.put(SecondaryStat.NotDamaged, new Pair(1, 10000));
         break;
      case 400051044:
         aftercancel = true;
         bufftimeR = false;
         if (applyto.striker3rdStack < 8) {
            if (applyto.striker3rdStack >= 8) {
               if (applyto.getSkillCustomValue(400051044) == null) {
                  System.err.println("여기감?");
                  skills = new ArrayList();
                  skills.add(new RangeAttack(400051044, applyto.getPosition(), !applyto.isFacingLeft() ? 0 : -1, 0, 0));
                  skills.add(new RangeAttack(400051045, applyto.getPosition(), !applyto.isFacingLeft() ? 0 : -1, 0, 0));
                  skills.add(new RangeAttack(400051045, applyto.getPosition(), !applyto.isFacingLeft() ? 0 : -1, 0, 0));
                  skills.add(new RangeAttack(400051045, applyto.getPosition(), !applyto.isFacingLeft() ? 0 : -1, 0, 0));
                  skills.add(new RangeAttack(400051045, applyto.getPosition(), !applyto.isFacingLeft() ? 0 : -1, 0, 0));
                  applyto.getClient().getSession().writeAndFlush(CField.rangeAttack(400051096, skills));
                  applyto.setSkillCustomInfo(400051044, 0L, (long)this.getCooldown(applyto));
                  applyto.striker3rdStack = 0;
                  applyto.cancelEffect(this);
               }
            } else {
               ++applyto.striker3rdStack;
               localstatups.put(SecondaryStat.Striker3rd, new Pair(applyto.striker3rdStack, 0));
            }
         } else {
            applyto.striker3rdStack = 8;
            localstatups.put(SecondaryStat.Striker3rd, new Pair(applyto.striker3rdStack, 0));
         }
         break;
      case 400051058:
         if (applyto.getBuffedValue(400051058)) {
            aftercancel = true;
            --applyto.striker4thAttack;
            if (applyto.striker4thAttack > 1) {
               localstatups.put(SecondaryStat.Striker4th, new Pair(applyto.striker4thAttack, (int)applyto.getBuffLimit(400051058)));
            } else {
               applyto.cancelEffectFromBuffStat(SecondaryStat.Striker4th);
            }
         } else {
            applyto.striker4thAttack = this.x;
            localstatups.put(SecondaryStat.Striker4th, new Pair(applyto.striker4thAttack, localDuration));
         }
         break;
      case 400051072:
         localstatups.put(SecondaryStat.IndieDamReduceR, new Pair(-this.w, 1000));
         localstatups.put(SecondaryStat.IndieFloating, new Pair(1, 1000));
         break;
      case 400051077:
         localstatups.put(SecondaryStat.IndieDamR, new Pair(Integer.valueOf(this.indieDamR), localDuration));
         break;
      case 400051078:
         if (!applyto.getBuffedValue(400051078)) {
            localstatups.put(SecondaryStat.IndieKeyDownMoving, new Pair(this.w, 3320));
         }
         break;
      case 400051094:
         localstatups.clear();
         localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 10000));
         localstatups.put(SecondaryStat.NotDamaged, new Pair(1, 10000));
         break;
      case 400051334:
         localstatups.clear();
         aftercancel = false;
         if (!primary) {
            localstatups.put(SecondaryStat.IndieNotDamaged, new Pair(1, 10000));
         } else {
            localstatups.put(SecondaryStat.MemoryOfSource, new Pair(Integer.valueOf(this.level), 30000));
            SkillFactory.getSkill(this.sourceid).getEffect(this.level).applyTo(applyto, false);
         }
         break;
      default:
         if (localDuration == Integer.MAX_VALUE) {
            localDuration = 0;
         }
      }

      if (this.getSummonMovementType() != null) {
         switch(this.sourceid) {
         case 1301013:
         case 5321003:
         case 14000027:
         case 14121003:
         case 32001014:
         case 32100010:
         case 32110017:
         case 32120019:
         case 33001007:
         case 33001008:
         case 33001009:
         case 33001010:
         case 33001011:
         case 33001012:
         case 33001013:
         case 33001014:
         case 33001015:
         case 35120002:
         case 80002888:
         case 101100101:
         case 131002015:
         case 131002022:
         case 131003022:
         case 131004022:
         case 131005022:
         case 131006022:
         case 151100002:
         case 162101012:
         case 164121011:
         case 400011013:
         case 400011014:
         case 400011065:
         case 400011078:
         case 400021071:
         case 400021095:
         case 400031008:
         case 400031009:
         case 400041033:
         case 400051011:
         case 400051017:
            break;
         case 36121014:
            if (primary) {
               localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
            }
            break;
         default:
            if (!GameConstants.isAfterRemoveSummonSkill(this.sourceid) && this.sourceid != 400021092) {
               if (this.sourceid != 400021047) {
                  localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
               } else if (!applyto.getBuffedValue(this.sourceid)) {
                  localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
               }
            } else if (this.sourceid == 400021069) {
               localstatups.put(SecondaryStat.IndieSummon, new Pair(1, localDuration));
            }
         }
      }

      if (GameConstants.MovedSkill(this.sourceid) || this.sourceid == 400001050 || this.sourceid == 400051000 || this.sourceid == 15001021 || this.sourceid == 63001004 || this.sourceid == 63001006 || localstatups.containsKey(SecondaryStat.ReviveOnce)) {
         showEffect = false;
      }

      var29 = this.statups.entrySet().iterator();

      while(var29.hasNext()) {
         Entry<SecondaryStat, Pair<Integer, Integer>> entry = (Entry)var29.next();
         if (!localstatups.containsKey(entry.getKey())) {
            localstatups.put((SecondaryStat)entry.getKey(), new Pair(((Pair)entry.getValue()).left, localDuration));
         }
      }

      if (this.sourceid == 5321054 && applyto.getKeyValue(51384, "ww_buck") != -1L) {
         Pair vz = (Pair)localstatups.get(SecondaryStat.IndiePmdR);
         localstatups.remove(SecondaryStat.IndiePmdR);
         vz.left = (Integer)vz.left * -1 + 100;
         localstatups.put(SecondaryStat.IndiePmdR, vz);
      }

      if (this.sourceid == 80002924 && applyto.getKeyValue(53714, "atk") != -1L) {
         localstatups.put(SecondaryStat.IndieDamR, new Pair((int)applyto.getKeyValue(53714, "atk"), 0));
      }

      if (this.sourceid == 80002419 && applyto.getKeyValue(800023, "indiepmer") > 0L) {
         localstatups.put(SecondaryStat.IndieDamR, new Pair((int)applyto.getKeyValue(800023, "indiepmer"), 0));
      }

      if (this.sourceid == 400001012) {
         applyto.cancelEffectFromBuffStat(SecondaryStat.IndieSummon, 3111005);
         applyto.cancelEffectFromBuffStat(SecondaryStat.IndieSummon, 3211005);
         applyto.cancelEffectFromBuffStat(SecondaryStat.IndieSummon, 3311009);
      }

      if (this.isMonsterRiding() && !localstatups.containsKey(SecondaryStat.RideVehicle)) {
         if (localDuration >= 2100000000 || localDuration < 0) {
            localDuration = 0;
         }

         localstatups.put(SecondaryStat.RideVehicle, new Pair(parseMountInfo(applyto, this.sourceid), 0));
      } else if (SkillFactory.getSkill(this.sourceid) != null && this.sourceid != 22171080 && SkillFactory.getSkill(this.sourceid).getVehicleID() > 0) {
         if (localDuration >= 2100000000 || localDuration < 0) {
            localDuration = 0;
         }

         localstatups.put(SecondaryStat.RideVehicle, new Pair(SkillFactory.getSkill(this.sourceid).getVehicleID(), localDuration));
         if (applyfrom.getMapId() == ServerConstants.warpMap && this.sourceid / 10000 == 8000) {
            applyfrom.getClient().send(CField.UIPacket.detailShowInfo("휴식 포인트 적립을 시작합니다.", 3, 20, 20));
            applyfrom.setSkillCustomInfo(applyfrom.getMapId(), 0L, 60000L);
         }
      }

      if (this.skill && !applyto.isHidden() && !isPetBuff && applyfrom.getId() == applyto.getId()) {
         if (applyto.getKeyValue(9999, "skillid") > 0L || applyto.getKeyValue(9999, "skillid2") > 0L || applyto.getKeyValue(9999, "skillid3") > 0L || applyto.getKeyValue(9999, "skillid4") > 0L || applyto.getKeyValue(9999, "skillid5") > 0L || applyto.getKeyValue(9999, "skillid6") > 0L) {
            applyto.addMP(70L);
         }

         if (this.sourceid >= 400041009 && this.sourceid <= 400041015 && showEffect && applyfrom.getId() == applyto.getId()) {
            applyto.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(applyto, 0, this.sourceid, 1, 0, 0, (byte)(applyto.getTruePosition().x <= pos.x ? 0 : 1), true, pos, (String)null, (Item)null));
            applyto.getMap().broadcastMessage(applyto, CField.EffectPacket.showEffect(applyto, 0, this.sourceid, 1, 0, 0, (byte)(applyto.getTruePosition().x <= pos.x ? 0 : 1), true, pos, (String)null, (Item)null), false);
         }

         if (!GameConstants.isLinkMap(applyto.getMapId()) && showEffect) {
            if (this.sourceid != 400051334 && !GameConstants.isKhali(applyto.getJob()) && (this.sourceid < 400041009 || this.sourceid > 400041015)) {
               applyto.getMap().broadcastMessage(applyto, CField.EffectPacket.showEffect(applyto, 0, this.sourceid, 1, 0, 0, (byte)(applyto.getTruePosition().x <= pos.x ? 0 : 1), false, pos, (String)null, (Item)null), false);
            }
         } else if (this.isPartyBuff(applyfrom, applyto)) {
            bounds = this.calculateBoundingBox(applyfrom.getTruePosition(), applyfrom.isFacingLeft());
            if (this.sourceid == 155001001 || this.sourceid == 155101003 || this.sourceid == 155111005 || this.sourceid == 155121005) {
               bounds = SkillFactory.getSkill(155121043).getEffect(1).calculateBoundingBox(applyfrom.getTruePosition(), applyfrom.isFacingLeft());
            }

            list = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.PLAYER));
            var38 = list.iterator();

            while(var38.hasNext()) {
               affectedmo = (MapleMapObject)var38.next();
               affected = (MapleCharacter)affectedmo;
               if (applyfrom.getId() != affected.getId()) {
                  this.applyBuffEffect(applyto, affected, primary, localDuration, pos, showEffect);
                  affected.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(affected, 0, this.sourceid, 4, 0, 0, (byte)(affected.getTruePosition().x <= pos.x ? 0 : 1), true, pos, (String)null, (Item)null));
                  affected.getMap().broadcastMessage(affected, CField.EffectPacket.showEffect(affected, 0, this.sourceid, 4, 0, 0, (byte)(affected.getTruePosition().x <= pos.x ? 0 : 1), false, pos, (String)null, (Item)null), false);
               }
            }
         }
      }

      if (localstatups.containsKey(SecondaryStat.JaguarSummoned)) {
         applyto.cancelEffectFromBuffStat(SecondaryStat.RideVehicle);
      }

      int[] FreudsProtections;
      array2 = FreudsProtections = new int[]{400001025, 400001026, 400001027, 400001028, 400001029, 400001030};
      up = array2.length;

      int i2;
      label3493:
      for(i = 0; i < up; ++i) {
         FreudsProtection = array2[i];
         if (this.sourceid == FreudsProtection) {
            int[] var71 = FreudsProtections;
            i2 = FreudsProtections.length;
            n = 0;

            while(true) {
               if (n >= i2) {
                  break label3493;
               }

               int s = var71[n];
               if (applyto.getBuffedValue(s)) {
                  applyto.cancelEffect(applyto.getBuffedEffect(s), (List)null, true);
               }

               ++n;
            }
         }
      }

      starttime = System.currentTimeMillis();
      exit = false;
      this.setStarttime((long)((int)(starttime % 1000000000L)));
      List<Pair<SecondaryStat, SecondaryStatValueHolder>> addV = new ArrayList();
      Iterator var72 = localstatups.entrySet().iterator();

      while(true) {
         Entry statup;
         while(true) {
            if (!var72.hasNext()) {
               if (exit) {
                  applyto.getClient().getSession().writeAndFlush(CWvsContext.enableActions(applyto, true, false));
                  return;
               }

               if (!addV.isEmpty()) {
                  applyto.getEffects().addAll(addV);
               }

               if (this.isHide()) {
                  applyto.getMap().broadcastMessage(applyto, CField.removePlayerFromMap(applyto.getId()), false);
               }

               if (this.sourceid >= 400041002 && this.sourceid <= 400041005 || this.sourceid == 11121014) {
                  showEffect = true;
               }

               if (showEffect && primary && this.getSummonMovementType() == null) {
                  if (SkillFactory.getSkill(this.sourceid) == null || this.sourceid != 24121003 && this.sourceid != 30010186 && !this.isHeroWill() && localstatups.size() <= 0 && !this.isMist() && this.damage <= 0 && SkillFactory.getSkill(this.sourceid).getType() != 41 && SkillFactory.getSkill(this.sourceid).getType() != 51) {
                     applyto.getClient().getSession().writeAndFlush(CWvsContext.enableActions(applyto, false, false));
                  } else {
                     applyto.getClient().getSession().writeAndFlush(CWvsContext.enableActions(applyto, true, false));
                  }
               }

               if (localstatups.size() <= 0) {
                  return;
               }

               switch(this.sourceid) {
               case 2003550:
               case 2003551:
               case 2023556:
               case 2023558:
               case 2023661:
               case 2023662:
               case 2023663:
               case 2023664:
               case 2023665:
               case 2023666:
               case 2024017:
               case 2024018:
               case 2450038:
               case 2450064:
               case 2450124:
               case 2450134:
               case 2450147:
               case 2450148:
               case 2450149:
                  if (primary) {
                     applyto.addCooldown(this.sourceid, System.currentTimeMillis(), (long)localDuration);
                  }
               }

               applyto.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(localstatups, this, applyto));
               applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.giveForeignBuff(applyto, localstatups, this), false);
               if (showEffect || this.sourceid == 3110001 || this.sourceid == 3210001 || this.sourceid == 3110012 || this.sourceid == 3101009) {
                  applyto.getClient().getSession().writeAndFlush(CWvsContext.enableActions(applyto, false, true));
               }

               if (this.sourceid == 21110016) {
                  applyto.getClient().getSession().writeAndFlush(CWvsContext.enableActions(applyto, true, false));
                  return;
               }

               if (this.sourceid != 21121058) {
                  if (this.sourceid != 400031005) {
                     return;
                  }

                  size3 = 0;

                  for(i2 = 33001007; i2 < 33001015; ++i2) {
                     if (GameConstants.getJaguarSummonId(i2) > 0 && GameConstants.getSelectJaguarSkillId(GameConstants.getMountItem(33001001, applyto)) != i2) {
                        MapleSummon tosummon = new MapleSummon(applyfrom, i2, new Point(applyto.getPosition().x + Randomizer.rand(-400, 400), applyto.getPosition().y), SummonMovementType.SUMMON_JAGUAR, (byte)0, localDuration);
                        tosummon.setPosition(new Point(applyto.getPosition().x + Randomizer.rand(-400, 400), applyto.getPosition().y));
                        applyfrom.getMap().spawnSummon(tosummon, localDuration);
                        applyfrom.addSummon(tosummon);
                        ++size3;
                     }

                     if (size3 == 6) {
                        return;
                     }
                  }
               }

               applyto.getClient().send(CField.aranCombo(500));
               return;
            }

            statup = (Entry)var72.next();
            if (applyto.getBuffedEffect((SecondaryStat)statup.getKey(), this.sourceid) == null) {
               break;
            }

            applyto.cancelEffect(this, Arrays.asList((SecondaryStat)statup.getKey()), true);
            if ((Integer)((Pair)statup.getValue()).right != 0 || aftercancel) {
               break;
            }

            exit = true;
         }

         if ((Integer)((Pair)statup.getValue()).right <= 0) {
            addV.add(new Pair(statup.getKey(), new SecondaryStatValueHolder(this, starttime, (Integer)((Pair)statup.getValue()).left, (Integer)((Pair)statup.getValue()).right, applyfrom.getId(), indieList1, indieList2)));
         } else {
            aftercancel = true;
            if (this.skill && SkillFactory.getSkill(this.sourceid) != null && !SkillFactory.getSkill(this.sourceid).isHyper() && bufftimeR && this.sourceid < 400000000 && statup.getKey() != SecondaryStat.IndieNotDamaged && statup.getKey() != SecondaryStat.NotDamaged && this.getSummonMovementType() == null) {
               ((Pair)statup.getValue()).right = this.alchemistModifyVal(applyfrom, (Integer)((Pair)statup.getValue()).right, false);
            }

            applyto.registerEffect(this, starttime, statup, false, applyfrom.getId(), indieList1, indieList2);
         }
      }
   }

   public void giveAnotherBuff(MapleCharacter applyto, MapleCharacter applyfrom, Map<SecondaryStat, Pair<Integer, Integer>> localstatups, boolean BuffTimeR) {
      ArrayList<Pair<SecondaryStat, SecondaryStatValueHolder>> addV = new ArrayList();
      ArrayList<Pair<Integer, Integer>> indieList1 = new ArrayList();
      ArrayList<Pair<Integer, Integer>> indieList2 = new ArrayList();
      long starttime = System.currentTimeMillis();
      boolean exit = false;
      Iterator var11 = localstatups.entrySet().iterator();

      while(var11.hasNext()) {
         Entry<SecondaryStat, Pair<Integer, Integer>> statup = (Entry)var11.next();
         if ((Integer)((Pair)statup.getValue()).right > 0) {
            if (this.skill && SkillFactory.getSkill(this.sourceid) != null && !SkillFactory.getSkill(this.sourceid).isHyper() && BuffTimeR && this.sourceid < 400000000 && statup.getKey() != SecondaryStat.IndieNotDamaged && statup.getKey() != SecondaryStat.NotDamaged) {
               ((Pair)statup.getValue()).right = this.alchemistModifyVal(applyfrom, (Integer)((Pair)statup.getValue()).right, false);
            }

            applyto.registerEffect(this, starttime, statup, false, applyfrom.getId(), indieList1, indieList2);
         } else {
            addV.add(new Pair((SecondaryStat)statup.getKey(), new SecondaryStatValueHolder(this, starttime, (Integer)((Pair)statup.getValue()).left, (Integer)((Pair)statup.getValue()).right, applyfrom.getId(), indieList1, indieList2)));
         }
      }

      if (exit) {
         applyto.getClient().getSession().writeAndFlush(CWvsContext.enableActions(applyto, true, false));
      } else {
         applyto.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(localstatups, this, applyto));
         applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.giveForeignBuff(applyto, localstatups, this), false);
      }
   }

   public int getMonsterRidingId() {
      return this.monsterRidingId;
   }

   public static final int parseMountInfo(MapleCharacter player, int skillid) {
      switch(skillid) {
      case 1004:
      case 10001004:
      case 20001004:
      case 20011004:
      case 20021004:
      case 20031004:
      case 30001004:
      case 30011004:
      case 50001004:
         if (player.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-122) != null) {
            return player.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-122).getItemId();
         } else {
            if (player.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-22) != null) {
               return player.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-22).getItemId();
            }

            return 0;
         }
      case 35001002:
      case 35111003:
      case 35120000:
         return 1932016;
      case 80002996:
         return 1932691;
      case 400031017:
         return 1932417;
      default:
         return GameConstants.getMountItem(skillid, player);
      }
   }

   private final int calcHPChange(MapleCharacter applyfrom, boolean primary) {
      int hpchange = 0;
      if (this.hp != 0 && applyfrom.getBuffedEffect(SecondaryStat.DebuffIncHp) == null) {
         if (!this.skill) {
            hpchange = primary ? hpchange + this.alchemistModifyVal(applyfrom, this.hp, true) : hpchange + this.hp;
            if (applyfrom.hasDisease(SecondaryStat.Undead)) {
               hpchange = applyfrom.getDisease(SecondaryStat.Undead).getMobskill().getSkillLevel() == 17 ? (hpchange *= -1) : (hpchange /= 2);
            }
         } else {
            hpchange += makeHealHP((double)this.hp / 100.0D, (double)applyfrom.getStat().getTotalMagic(), 3.0D, 5.0D);
            if (applyfrom.hasDisease(SecondaryStat.Undead)) {
               hpchange = -hpchange;
            }
         }
      }

      if (this.hpR != 0.0D && applyfrom.getBuffedEffect(SecondaryStat.DebuffIncHp) == null) {
         hpchange += (int)((double)applyfrom.getStat().getCurrentMaxHp() * this.hpR) / (applyfrom.hasDisease(SecondaryStat.Undead) ? 2 : 1);
         if (applyfrom.hasDisease(SecondaryStat.Undead) && applyfrom.getDisease(SecondaryStat.Undead).getMobskill().getSkillLevel() == 17) {
            hpchange *= -1;
         }
      }

      if (applyfrom.getBuffedValue(SecondaryStat.DemonFrenzy) != null) {
         hpchange /= 100;
      }

      if (this.hpRCon != 0.0D) {
         hpchange -= (int)((double)applyfrom.getStat().getCurrentMaxHp() * this.hpRCon);
      }

      if (applyfrom.getSkillCustomValue0(143143) == 1L) {
         hpchange /= 10;
      }

      MapleMonster seren;
      if (hpchange > 0 && applyfrom.getMapId() == 410002060 && (seren = applyfrom.getMap().getMonsterById(8880602)) != null && seren.getSerenTimetype() == 2) {
         hpchange = hpchange * 20 / 100;
      }

      if (primary && this.hpCon != 0) {
         hpchange -= this.hpCon;
      }

      if (applyfrom.getBuffedEffect(SecondaryStat.DebuffIncHp) == null) {
         switch(this.sourceid) {
         case 1211010:
            hpchange = (int)(applyfrom.getStat().getCurrentMaxHp() * (long)Math.max(10, this.x - applyfrom.getListonation() * 10) / 100L);
            break;
         case 1320019:
            hpchange = 0;
         }
      }

      if (applyfrom.getSkillLevel(30010242) > 0 && hpchange < 0 && (applyfrom.getBuffedEffect(SecondaryStat.DebuffIncHp) != null || applyfrom.getBuffedValue(SecondaryStat.DemonFrenzy) != null && applyfrom.getBuffedEffect(SecondaryStat.DemonFrenzy).getQ2() < applyfrom.getStat().getHPPercent()) && this.skill) {
         hpchange = hpchange / 100 * (100 - SkillFactory.getSkill(30010242).getEffect(1).getX());
      }

      if (hpchange < 0 && applyfrom.getBuffedValue(31221054)) {
         hpchange = 0;
      }

      return hpchange;
   }

   private static final int makeHealHP(double rate, double stat, double lowerfactor, double upperfactor) {
      return (int)(Math.random() * (double)((int)(stat * upperfactor * rate) - (int)(stat * lowerfactor * rate) + 1) + (double)((int)(stat * lowerfactor * rate)));
   }

   private final int calcMPChange(MapleCharacter applyfrom, boolean primary) {
      int mpchange = 0;
      if (this.mp != 0) {
         mpchange = primary ? mpchange + this.alchemistModifyVal(applyfrom, this.mp, false) : mpchange + this.mp;
      }

      if (this.mpR != 0.0D) {
         mpchange += (int)((double)applyfrom.getStat().getCurrentMaxMp(applyfrom) * this.mpR);
      }

      if (GameConstants.isDemonSlayer(applyfrom.getJob())) {
         mpchange = 0;
      }

      if (primary) {
         if (this.mpCon != 0 && !GameConstants.isDemonSlayer(applyfrom.getJob())) {
            mpchange = applyfrom.getBuffedValue(SecondaryStat.InfinityForce) == null && !applyfrom.energyCharge ? (int)((double)mpchange - (double)(this.mpCon - this.mpCon * applyfrom.getStat().mpconReduce / 100) * ((double)applyfrom.getStat().mpconPercent / 100.0D)) : 0;
         } else if (this.getForceCon() != 0 && GameConstants.isDemonSlayer(applyfrom.getJob())) {
            mpchange = applyfrom.getBuffedValue(SecondaryStat.InfinityForce) != null ? 0 : mpchange - this.getForceCon();
            if (applyfrom.getSkillLevel(31120048) > 0 && this.sourceid == 31121005) {
               mpchange /= 2;
            }

            if (applyfrom.getSkillLevel(31120051) > 0 && this.sourceid == 31121001) {
               mpchange /= 2;
            }

            if (applyfrom.getSkillLevel(31121054) > 0) {
               mpchange = mpchange * 8 / 10;
            }
         }
      }

      if (applyfrom.getBuffedValue(SecondaryStat.Overload) != null) {
         mpchange = 0;
      }

      if ((applyfrom.getBuffedValue(20040217) || applyfrom.getBuffedValue(20040219) || applyfrom.getBuffedValue(20040220)) && (GameConstants.isDarkSkills(this.sourceid) || (applyfrom.getBuffedValue(20040219) || applyfrom.getBuffedValue(20040220)) && (this.sourceid == 27121303 || this.sourceid == 27111303))) {
         mpchange = 0;
      }

      switch(this.sourceid) {
      case 1320019:
         mpchange = 0;
      default:
         return mpchange;
      }
   }

   public final int alchemistModifyVal(MapleCharacter chr, int val, boolean withX) {
      if (!this.skill) {
         return val * (100 + (withX ? chr.getStat().RecoveryUP : chr.getStat().BuffUP)) / 100;
      } else if (this.getSummonMovementType() != null) {
         return val * (100 + chr.getStat().BuffUP_Summon) / 100;
      } else {
         int bufftime = chr.getStat().BuffUP_Skill;
         if (GameConstants.isPhantom(chr.getJob()) && chr.getSkillLevel(24120050) > 0) {
            Iterator var5 = chr.getStolenSkills().iterator();

            while(var5.hasNext()) {
               Pair<Integer, Boolean> sk = (Pair)var5.next();
               if ((Integer)sk.left == this.sourceid) {
                  bufftime += 10;
                  break;
               }
            }
         }

         return val * (100 + bufftime) / 100;
      }
   }

   public final int calcPowerChange(MapleCharacter applyfrom, boolean primary) {
      int powerchange = 0;
      if (!primary) {
         return 0;
      } else {
         if (this.powerCon != 0 && GameConstants.isXenon(applyfrom.getJob())) {
            powerchange = applyfrom.getBuffedValue(SecondaryStat.AmaranthGenerator) == null && applyfrom.getBuffedValue(SecondaryStat.Overload) == null ? this.powerCon : 0;
         }

         return powerchange;
      }
   }

   public final void setSourceId(int newid) {
      this.sourceid = newid;
   }

   public final boolean isInflation() {
      return this.inflation > 0;
   }

   public final int getInflation() {
      return this.inflation;
   }

   private boolean isPartyBuff(MapleCharacter applyfrom, MapleCharacter applyto) {
      if (this.lt != null && this.rb != null && applyfrom.getMapId() != 450013700 && applyto.getMapId() != 450013700) {
         SecondaryStat[] var4 = new SecondaryStat[]{SecondaryStat.BasicStatUp, SecondaryStat.MaxLevelBuff};
         int var5 = var4.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            SecondaryStat buff = var4[var6];
            if (this.statups.containsKey(buff)) {
               return true;
            }
         }

         if (SkillFactory.getSkill(this.sourceid).isHyper() && (this.sourceid % 100 == 53 || this.sourceid == 25121132)) {
            int job = this.sourceid / 10000;
            if (job < 1000 && applyfrom.getJob() < 1000 && applyto.getJob() < 1000) {
               return true;
            }

            if (job >= 1000 && job < 2000 && applyfrom.getJob() >= 1000 && applyfrom.getJob() < 2000 && applyto.getJob() >= 1000 && applyto.getJob() < 2000) {
               return true;
            }

            if (job >= 2000 && job < 3000 && applyfrom.getJob() >= 1000 && applyfrom.getJob() < 3000 && applyto.getJob() >= 2000 && applyto.getJob() < 3000) {
               return true;
            }

            if (job >= 3000 && job < 4000 && applyfrom.getJob() >= 1000 && applyfrom.getJob() < 4000 && applyto.getJob() >= 3000 && applyto.getJob() < 4000) {
               return true;
            }
         }

         switch(this.sourceid) {
         case 1101006:
         case 1211011:
         case 1301006:
         case 1301007:
         case 1310016:
         case 2101001:
         case 2201001:
         case 2301002:
         case 2301004:
         case 2311001:
         case 2311003:
         case 2311009:
         case 2321005:
         case 2321007:
         case 2321052:
         case 2321055:
         case 3121002:
         case 3221002:
         case 3321022:
         case 4001005:
         case 4101004:
         case 4111001:
         case 4201003:
         case 4301003:
         case 5121009:
         case 5301003:
         case 5320008:
         case 12101000:
         case 13121005:
         case 14001022:
         case 14101003:
         case 15121005:
         case 21111012:
         case 22151003:
         case 22171054:
         case 27111006:
         case 27111101:
         case 32001003:
         case 32101003:
         case 33101005:
         case 33121004:
         case 51101004:
         case 51111008:
         case 131001009:
         case 131001013:
         case 131001113:
         case 135001009:
         case 152121043:
         case 400021077:
         case 400031038:
         case 400041011:
         case 400041012:
         case 400041013:
         case 400041014:
         case 400041015:
            return true;
         case 155001001:
         case 155101003:
         case 155111005:
         case 155121005:
            return applyfrom.getBuffedValue(155121043);
         default:
            if (!this.isHeal() && !this.isResurrection() && !this.isTimeLeap()) {
               return false;
            } else {
               return !this.isResurrection() && !this.isHeal() || !GameConstants.보스맵(applyfrom.getMapId()) || applyto.getDeathCount() > 0;
            }
         }
      } else {
         return false;
      }
   }

   public final boolean isHeal() {
      return this.skill && (this.sourceid == 9101000 || this.sourceid == 9001000);
   }

   public final boolean isResurrection() {
      return this.skill && (this.sourceid == 9001005 || this.sourceid == 9101005 || this.sourceid == 2321006 || this.sourceid == 1221016);
   }

   public final boolean isTimeLeap() {
      return this.skill && this.sourceid == 5121010;
   }

   public final short getHp() {
      return this.hp;
   }

   public final short getMp() {
      return this.mp;
   }

   public final double getHpR() {
      return this.hpR;
   }

   public final double getMpR() {
      return this.mpR;
   }

   public final byte getMastery() {
      return this.mastery;
   }

   public final short getWatk() {
      return this.pad;
   }

   public final short getMatk() {
      return this.mad;
   }

   public final short getMdef() {
      return this.mdef;
   }

   public final short getAcc() {
      return this.acc;
   }

   public final short getAvoid() {
      return this.avoid;
   }

   public final short getHands() {
      return this.hands;
   }

   public final short getSpeed() {
      return this.speed;
   }

   public final short getJump() {
      return this.jump;
   }

   public final short getPassiveSpeed() {
      return this.psdSpeed;
   }

   public final short getPassiveJump() {
      return this.psdJump;
   }

   public final int getDuration() {
      return this.duration;
   }

   public final int getSubTime() {
      return this.subTime;
   }

   public final Map<SecondaryStat, Pair<Integer, Integer>> getStatups() {
      return this.statups;
   }

   public final boolean sameSource(SecondaryStatEffect effect) {
      boolean sameSrc = this.sourceid == effect.sourceid;
      switch(this.sourceid) {
      case 32120013:
         sameSrc = effect.sourceid == 32001003;
         break;
      case 32120014:
         sameSrc = effect.sourceid == 32101003;
         break;
      case 32120015:
         sameSrc = effect.sourceid == 32111012;
         break;
      case 35120000:
         sameSrc = effect.sourceid == 35001002;
         break;
      case 35121013:
         sameSrc = effect.sourceid == 35111004;
      }

      return effect != null && sameSrc && this.skill == effect.skill;
   }

   public final int getCr() {
      return this.cr;
   }

   public final double getT() {
      return this.t;
   }

   public final int getU() {
      return this.u;
   }

   public final int getV() {
      return this.v;
   }

   public final void setV(int newvalue) {
      this.v = newvalue;
   }

   public final int getW() {
      return this.w;
   }

   public final int getX() {
      return this.x;
   }

   public final int addX(int b) {
      return this.x + b;
   }

   public final int getY() {
      return this.y;
   }

   public final void setY(int newvalue) {
      this.y = newvalue;
   }

   public final int getZ() {
      return this.z;
   }

   public final int getS() {
      return this.s;
   }

   public final short getDamage() {
      return this.damage;
   }

   public final short getPVPDamage() {
      return this.PVPdamage;
   }

   public final byte getAttackCount() {
      return this.attackCount;
   }

   public final byte getBulletCount() {
      return this.bulletCount;
   }

   public final int getBulletConsume() {
      return this.bulletConsume;
   }

   public final byte getMobCount() {
      return this.mobCount;
   }

   public final int getMoneyCon() {
      return this.moneyCon;
   }

   public boolean cantIgnoreCooldown() {
      switch(this.sourceid) {
      case 1320016:
      case 1320019:
      case 2121004:
      case 2221004:
      case 2321004:
      case 2321055:
      case 5121010:
      case 12111023:
      case 12111029:
      case 14111030:
      case 21121058:
      case 24111002:
      case 30001062:
      case 64121001:
      case 64121053:
      case 80000299:
      case 80000300:
      case 80000301:
      case 80000302:
      case 80000303:
      case 80001455:
      case 80001456:
      case 80001457:
      case 80001458:
      case 80001459:
      case 80001460:
      case 80001461:
      case 80001462:
      case 80001463:
      case 80001464:
      case 80001465:
      case 80001466:
      case 80001467:
      case 80001468:
      case 80001469:
      case 80001470:
      case 80001471:
      case 80001472:
      case 80001473:
      case 80001474:
      case 80001475:
      case 80001476:
      case 80001477:
      case 80001478:
      case 80001479:
      case 100001272:
      case 100001274:
      case 100001281:
      case 150011074:
      case 155111306:
      case 155121306:
      case 155121341:
      case 164121000:
      case 164121041:
      case 164121042:
         return true;
      default:
         return false;
      }
   }

   public boolean ignoreCooldown(MapleCharacter chra) {
      Skill skill = SkillFactory.getSkill(this.sourceid);
      return !skill.isHyper() && !skill.isVMatrix() && !skill.isNotCooltimeReset() && !this.cantIgnoreCooldown() ? Randomizer.isSuccess(chra.getStat().randCooldown) : false;
   }

   public final int getCooldown(MapleCharacter chra) {
      int localCooltime = 0;
      int ItemCooltime = 0;
      int minusCooltime = false;
      if (this.cooltime == 0 && this.cooltimeMS == 0) {
         return 0;
      } else {
         if (this.cooltime > 0) {
            if (this.cooltime < 5000) {
               return this.cooltime;
            }

            localCooltime = this.cooltime;
            ItemCooltime = chra.getStat().reduceCooltime * 1000;
            minusCooltime = false;
            if (chra.getBuffedEffect(SecondaryStat.IndieReduceCooltime) != null && !GameConstants.isBeginnerJob(this.sourceid / 10000) && !SkillFactory.getSkill(this.sourceid).isVMatrix() && this.sourceid / 1000 != 8000) {
               localCooltime -= localCooltime * chra.getBuffedValue(SecondaryStat.IndieReduceCooltime) / 100;
            }
         } else if (this.cooltimeMS > 0) {
            if (this.cooltimeMS < 5000) {
               return this.cooltimeMS;
            }

            if (this.cooltimeMS > 10000) {
               localCooltime = this.cooltimeMS - chra.getStat().reduceCooltime * 1000;
            } else {
               localCooltime = Math.max(5, this.cooltimeMS * (100 - chra.getStat().reduceCooltime * 5) / 100);
            }

            if (chra.getBuffedEffect(SecondaryStat.IndieReduceCooltime) != null && !GameConstants.isBeginnerJob(this.sourceid / 10000) && !SkillFactory.getSkill(this.sourceid).isVMatrix() && this.sourceid / 1000 != 8000) {
               localCooltime -= localCooltime * chra.getBuffedValue(SecondaryStat.IndieReduceCooltime) / 100;
            }
         }

         if (this.sourceid == 31121003 && chra.getBuffedEffect(SecondaryStat.InfinityForce) != null) {
            localCooltime /= 2;
         }

         if (chra.getSkillLevel(71000231) > 0) {
            localCooltime -= localCooltime * SkillFactory.getSkill(71000231).getEffect(chra.getSkillLevel(71000231)).coolTimeR / 100;
         }

         if (chra.getSkillLevel(1220051) > 0 && this.sourceid == 1221011) {
            localCooltime -= localCooltime * SkillFactory.getSkill(1220051).getEffect(1).coolTimeR / 100;
         }

         if (chra.getSkillLevel(2120051) > 0 && this.sourceid == 2121003) {
            localCooltime -= localCooltime * SkillFactory.getSkill(2120051).getEffect(1).coolTimeR / 100;
         }

         if (chra.getSkillLevel(14120046) > 0 && this.sourceid == 14121003) {
            localCooltime -= localCooltime * SkillFactory.getSkill(14120046).getEffect(1).coolTimeR / 100;
         }

         if (chra.getSkillLevel(22170087) > 0 && (this.sourceid == 22141012 || this.sourceid == 22140022)) {
            localCooltime -= localCooltime * SkillFactory.getSkill(22170087).getEffect(1).coolTimeR / 100;
         }

         if (chra.getSkillLevel(22170090) > 0 && this.sourceid == 22171063) {
            localCooltime -= localCooltime * SkillFactory.getSkill(22170090).getEffect(1).coolTimeR / 100;
         }

         if (chra.getSkillLevel(22170084) > 0 && this.sourceid == 22110023) {
            localCooltime -= localCooltime * SkillFactory.getSkill(22170084).getEffect(1).coolTimeR / 100;
         }

         if (chra.getSkillLevel(24120044) > 0 && this.sourceid == 24121005) {
            localCooltime -= localCooltime * SkillFactory.getSkill(24120044).getEffect(1).coolTimeR / 100;
         }

         if (chra.getSkillLevel(32120057) > 0 && this.sourceid == 32121004) {
            localCooltime -= localCooltime * SkillFactory.getSkill(32120057).getEffect(1).coolTimeR / 100;
         }

         if (chra.getSkillLevel(32120063) > 0 && this.sourceid == 32121006) {
            localCooltime -= localCooltime * SkillFactory.getSkill(32120063).getEffect(1).coolTimeR / 100;
         }

         if (chra.getSkillLevel(33120048) > 0 && (this.sourceid == 33111006 || this.sourceid == 33101215 || this.sourceid == 33121002)) {
            localCooltime -= localCooltime * SkillFactory.getSkill(33120048).getEffect(1).coolTimeR / 100;
         }

         if (chra.getSkillLevel(35120045) > 0 && this.sourceid == 35111002) {
            localCooltime -= localCooltime * SkillFactory.getSkill(35120045).getEffect(1).coolTimeR / 100;
         }

         if (chra.getSkillLevel(64120051) > 0 && this.sourceid == 64121001) {
            localCooltime -= localCooltime * SkillFactory.getSkill(64120051).getEffect(1).coolTimeR / 100;
         }

         if (chra.getSkillLevel(65120048) > 0 && this.sourceid == 65121002) {
            localCooltime -= localCooltime * SkillFactory.getSkill(65120048).getEffect(1).coolTimeR / 100;
         }

         if (chra.getSkillLevel(142120040) > 0 && this.sourceid == 142121004) {
            localCooltime -= localCooltime * SkillFactory.getSkill(142120040).getEffect(1).coolTimeR / 100;
         }

         if (chra.getSkillLevel(152120036) > 0 && this.sourceid == 152121004) {
            localCooltime -= localCooltime * SkillFactory.getSkill(152120036).getEffect(1).coolTimeR / 100;
         }

         if (chra.getSkillLevel(155120038) > 0 && this.sourceid == 155111306) {
            localCooltime -= localCooltime * SkillFactory.getSkill(155120038).getEffect(1).coolTimeR / 100;
         }

         if (chra.getSkillLevel(151120036) > 0 && this.sourceid == 151121003) {
            localCooltime -= localCooltime * SkillFactory.getSkill(151120036).getEffect(1).coolTimeR / 100;
         }

         if (localCooltime > 0 && chra.getBuffedValue(SecondaryStat.FixCooltime) != null && this.sourceid / 10000 != 8000 && this.sourceid / 10000 <= chra.getJob()) {
            localCooltime = chra.getBuffedValue(SecondaryStat.FixCooltime) * 1000;
         }

         if (chra.getSkillLevel(162120035) > 0 && this.sourceid == 162111005) {
            localCooltime -= (int)((double)localCooltime * ((double)SkillFactory.getSkill(162120035).getEffect(1).coolTimeR / 100.0D));
         }

         if (localCooltime <= 10000) {
            if (ItemCooltime > 0) {
               localCooltime = (int)((double)localCooltime - (double)ItemCooltime * 0.5D);
            }
         } else {
            int minusCooltime = localCooltime - 10000;
            if (ItemCooltime <= minusCooltime) {
               localCooltime -= ItemCooltime;
            } else {
               ItemCooltime -= minusCooltime;
               localCooltime -= minusCooltime;
               if (ItemCooltime > 0) {
                  localCooltime = (int)((double)localCooltime - (double)ItemCooltime * 0.5D);
               }
            }
         }

         if (localCooltime <= 5000) {
            localCooltime = Math.max(5000, localCooltime);
         }

         if (chra.getStat().coolTimeR > 0) {
            localCooltime -= (int)((double)this.cooltime * ((double)chra.getStat().coolTimeR / 100.0D));
         }

         return Math.max(0, localCooltime);
      }
   }

   public final int getBerserk() {
      return this.berserk;
   }

   public final boolean isHide() {
      return this.skill && (this.sourceid == 9001004 || this.sourceid == 9101004);
   }

   public final boolean isRecovery() {
      return this.skill && (this.sourceid == 1001 || this.sourceid == 10001001 || this.sourceid == 20001001 || this.sourceid == 20011001 || this.sourceid == 20021001 || this.sourceid == 11001 || this.sourceid == 35121005);
   }

   public final boolean isBerserk() {
      return this.skill && this.sourceid == 1320006;
   }

   public final boolean isMPRecovery() {
      return this.skill && this.sourceid == 5101005;
   }

   public final boolean isMonsterRiding_() {
      return this.skill && (this.sourceid == 1004 || this.sourceid == 10001004 || this.sourceid == 20001004 || this.sourceid == 20011004 || this.sourceid == 30001004 && this.sourceid >= 80001000 && this.sourceid <= 80001033 || this.sourceid == 80001037 || this.sourceid == 80001038 || this.sourceid == 80001039 || this.sourceid == 80001044 || this.sourceid >= 80001082 && this.sourceid <= 80001090 || this.sourceid == 30011159 || this.sourceid == 30011109 || this.sourceid == 1004 || this.sourceid == 10001004 || this.sourceid == 20001004 || this.sourceid == 20011004 || this.sourceid == 30001004 || this.sourceid == 20021004 || this.sourceid == 20031004 || this.sourceid == 30011004 || this.sourceid == 50001004 || this.sourceid == 35120000 || this.sourceid == 33001001 || this.sourceid == 35001002 || this.sourceid == 35111003);
   }

   public final boolean isMonsterRiding() {
      return this.skill && (this.isMonsterRiding_() || GameConstants.checkMountItem(this.sourceid) != 0);
   }

   public final boolean isMagicDoor() {
      return this.skill && (this.sourceid == 2311002 || this.sourceid % 10000 == 8001 || this.sourceid == 400001001);
   }

   public final boolean isMechDoor() {
      return this.skill && this.sourceid == 35101005;
   }

   public final boolean isCharge() {
      switch(this.sourceid) {
      case 1211003:
      case 1211008:
      case 11111007:
      case 12101005:
      case 15101006:
      case 21111005:
         return this.skill;
      default:
         return false;
      }
   }

   public final boolean isPoison() {
      return this.dot > 0 && this.dotTime > 0;
   }

   public final boolean isMist() {
      if (this.skill) {
         switch(this.sourceid) {
         case 1076:
         case 11076:
         case 2100010:
         case 2111003:
         case 2201009:
         case 2311011:
         case 4121015:
         case 4221006:
         case 12111005:
         case 12121005:
         case 21121057:
         case 22161003:
         case 22170093:
         case 24121052:
         case 25111206:
         case 32121006:
         case 35120002:
         case 35121052:
         case 36121007:
         case 37110002:
         case 61121105:
         case 61121116:
         case 80001455:
         case 100001261:
         case 101120206:
         case 151121041:
         case 152121041:
         case 155121006:
         case 162101010:
         case 162111000:
         case 162111003:
         case 162121018:
         case 400001017:
         case 400010010:
         case 400011098:
         case 400011100:
         case 400011135:
         case 400020002:
         case 400020046:
         case 400020051:
         case 400021041:
         case 400021049:
         case 400021050:
         case 400021104:
         case 400030002:
         case 400031012:
         case 400031039:
         case 400031040:
         case 400040008:
         case 400041008:
         case 400041041:
         case 400051025:
         case 400051076:
            return true;
         }
      }

      return false;
   }

   private final boolean isDispel() {
      return this.skill && (this.sourceid == 2311001 || this.sourceid == 9001000 || this.sourceid == 9101000);
   }

   private final boolean isHeroWill() {
      switch(this.sourceid) {
      case 1121011:
      case 1221012:
      case 1321010:
      case 2121008:
      case 2221008:
      case 2321009:
      case 3121009:
      case 3221008:
      case 3321024:
      case 4121009:
      case 4221008:
      case 4341008:
      case 5121008:
      case 5221010:
      case 5321008:
      case 21121008:
      case 22171004:
      case 22171069:
      case 23121008:
      case 24121009:
      case 25121211:
      case 27121010:
      case 32121008:
      case 33121008:
      case 35121008:
      case 36121009:
      case 37121007:
      case 61121015:
      case 61121220:
      case 64121005:
      case 65121010:
      case 151121006:
      case 152121010:
      case 154121006:
      case 155121009:
      case 164121010:
      case 400001009:
         return this.skill;
      default:
         return false;
      }
   }

   public final boolean isCombo() {
      switch(this.sourceid) {
      case 1101013:
      case 11111001:
         return this.skill;
      default:
         return false;
      }
   }

   public final boolean isMorph() {
      return this.morphId > 0;
   }

   public final byte getLevel() {
      return this.level;
   }

   public final SummonMovementType getSummonMovementType() {
      if (this.skill && SkillFactory.getSkill(this.sourceid) != null) {
         if (!GameConstants.isAngelicBlessSkill(SkillFactory.getSkill(this.sourceid)) && !GameConstants.isAngelicBlessBuffEffectItem(this.sourceid)) {
            switch(this.sourceid) {
            case 1121055:
            case 2111013:
            case 2211015:
            case 2311014:
            case 3111002:
            case 3111017:
            case 3120012:
            case 3211002:
            case 3211019:
            case 3220012:
            case 3221014:
            case 4111007:
            case 4211007:
            case 4341006:
            case 5211001:
            case 5211014:
            case 5220002:
            case 5220023:
            case 5220024:
            case 5220025:
            case 5221022:
            case 5221029:
            case 5320011:
            case 5321003:
            case 5321004:
            case 5321052:
            case 11111029:
            case 13111004:
            case 13111024:
            case 13120007:
            case 14121003:
            case 22171081:
            case 33101008:
            case 33111003:
            case 35101012:
            case 35111002:
            case 35111005:
            case 35111008:
            case 35111011:
            case 35120002:
            case 35121003:
            case 35121009:
            case 35121010:
            case 35121011:
            case 36121002:
            case 36121013:
            case 36121014:
            case 51121016:
            case 61111002:
            case 61111220:
            case 80002888:
            case 80002889:
            case 131001007:
            case 131001022:
            case 131001025:
            case 131001307:
            case 131002022:
            case 131003022:
            case 131003023:
            case 131004022:
            case 131004023:
            case 131005022:
            case 131005023:
            case 131006022:
            case 131006023:
            case 151100002:
            case 151111001:
            case 154110010:
            case 154121041:
            case 162101003:
            case 162101006:
            case 162121012:
            case 162121015:
            case 164121006:
            case 164121008:
            case 164121011:
            case 400001019:
            case 400001022:
            case 400001039:
            case 400001064:
            case 400011002:
            case 400011057:
            case 400011065:
            case 400021005:
            case 400021047:
            case 400021063:
            case 400021067:
            case 400021069:
            case 400021071:
            case 400021073:
            case 400021095:
            case 400031047:
            case 400031049:
            case 400031051:
            case 400041033:
            case 400041038:
            case 400041044:
            case 400041050:
            case 400041052:
            case 400051011:
            case 400051017:
            case 400051022:
               return SummonMovementType.STATIONARY;
            case 1301013:
            case 2121005:
            case 2211011:
            case 2221005:
            case 2321003:
            case 3111005:
            case 3211005:
            case 11001004:
            case 12000022:
            case 12001004:
            case 12100026:
            case 12110024:
            case 12111004:
            case 12120007:
            case 13001004:
            case 14001005:
            case 15001004:
            case 25121133:
            case 32001014:
            case 32100010:
            case 32110017:
            case 32120019:
            case 35111001:
            case 35111009:
            case 35111010:
            case 80001266:
            case 80001269:
            case 80001270:
            case 80001322:
            case 80001323:
            case 80001341:
            case 80001395:
            case 80001396:
            case 80001493:
            case 80001494:
            case 80001495:
            case 80001496:
            case 80001497:
            case 80001498:
            case 80001499:
            case 80001500:
            case 80001501:
            case 80001502:
            case 80001681:
            case 80001682:
            case 80001683:
            case 80001685:
            case 80001690:
            case 80001691:
            case 80001692:
            case 80001693:
            case 80001695:
            case 80001696:
            case 80001697:
            case 80001698:
            case 80001700:
            case 80001804:
            case 80001806:
            case 80001807:
            case 80001808:
            case 80001984:
            case 80001985:
            case 80002230:
            case 80002231:
            case 80002405:
            case 80002406:
            case 80002639:
            case 80002641:
            case 131003026:
            case 152001003:
            case 152121006:
            case 400001013:
            case 400001059:
            case 400011001:
            case 400011077:
            case 400011078:
            case 400011090:
            case 400021032:
            case 400021033:
            case 400021092:
            case 400031001:
            case 400051009:
            case 400051046:
               return SummonMovementType.FOLLOW;
            case 2111010:
            case 32111006:
            case 33001007:
            case 33001008:
            case 33001010:
            case 33001011:
            case 33001012:
            case 33001015:
            case 162101012:
            case 400011012:
            case 400011013:
            case 400011014:
               return SummonMovementType.WALK_STATIONARY;
            case 2311006:
            case 3121006:
            case 3221005:
            case 3311009:
            case 5211002:
            case 14000027:
            case 14100027:
            case 14110029:
            case 14120008:
            case 23111009:
            case 23111010:
            case 23111011:
            case 33111005:
            case 131002015:
            case 152101008:
            case 152121005:
            case 164111007:
            case 400001012:
               return SummonMovementType.BIRD_FOLLOW;
            case 5201012:
            case 5201013:
            case 5201014:
            case 5210015:
            case 5210016:
            case 5210017:
            case 5210018:
            case 5211019:
            case 12120013:
            case 12120014:
               return SummonMovementType.FLAME_SUMMON;
            case 14111024:
            case 14121055:
            case 14121056:
            case 131001017:
            case 131002017:
            case 131003017:
            case 400031007:
            case 400031008:
            case 400031009:
               return SummonMovementType.ShadowServant;
            case 33001009:
            case 33001013:
            case 33001014:
               return SummonMovementType.SUMMON_JAGUAR;
            case 101100100:
            case 101100101:
            case 400011006:
               return SummonMovementType.ZEROWEAPON;
            case 152101000:
            case 400011088:
            case 400041028:
               return SummonMovementType.ShadowServantExtend;
            case 400051038:
            case 400051052:
            case 400051053:
               return SummonMovementType.WALK_FOLLOWER;
            case 400051068:
               return SummonMovementType.FLY;
            default:
               return this.isAngel() ? SummonMovementType.FOLLOW : null;
            }
         } else {
            return SummonMovementType.FOLLOW;
         }
      } else {
         return null;
      }
   }

   public final boolean isAngel() {
      return GameConstants.isAngel(this.sourceid);
   }

   public final boolean isSkill() {
      return this.skill;
   }

   public final int getSourceId() {
      return this.sourceid;
   }

   public final boolean isSoaring() {
      return this.isSoaring_Normal() || this.isSoaring_Mount();
   }

   public final boolean isSoaring_Normal() {
      return this.skill && GameConstants.isBeginnerJob(this.sourceid / 10000) && this.sourceid % 10000 == 1026;
   }

   public final boolean isSoaring_Mount() {
      return this.skill && (GameConstants.isBeginnerJob(this.sourceid / 10000) && this.sourceid % 10000 == 1142 || this.sourceid == 80001089);
   }

   public final boolean makeChanceResult() {
      if (this.subprop != 100) {
         return this.subprop >= 100 || Randomizer.nextInt(100) < this.subprop;
      } else {
         return this.prop >= 100 || Randomizer.nextInt(100) < this.prop;
      }
   }

   public final short getProp() {
      return this.prop;
   }

   public final short getIgnoreMob() {
      return this.ignoreMobpdpR;
   }

   public final int getEnhancedHP() {
      return this.emhp;
   }

   public final int getEnhancedMP() {
      return this.emmp;
   }

   public final int getEnhancedWatk() {
      return this.epad;
   }

   public final int getEnhancedWdef() {
      return this.epdd;
   }

   public final int getEnhancedMdef() {
      return this.emdd;
   }

   public final short getDOT() {
      return this.dot;
   }

   public final short getDOTTime() {
      return this.dotTime;
   }

   public final short getCriticalDamage() {
      return this.criticaldamage;
   }

   public final short getASRRate() {
      return this.asrR;
   }

   public final short getTERRate() {
      return this.terR;
   }

   public final int getDAMRate() {
      return this.damR;
   }

   public final short getMesoRate() {
      return this.mesoR;
   }

   public final int getEXP() {
      return this.exp;
   }

   public final short getAttackX() {
      return this.padX;
   }

   public final short getMagicX() {
      return this.madX;
   }

   public final int getPercentHP() {
      return this.mhpR;
   }

   public final int getPercentMP() {
      return this.mmpR;
   }

   public final int getConsume() {
      return this.consumeOnPickup;
   }

   public final int getSelfDestruction() {
      return this.selfDestruction;
   }

   public final int getCharColor() {
      return this.charColor;
   }

   public final int getSpeedMax() {
      return this.speedMax;
   }

   public final int getAccX() {
      return this.accX;
   }

   public final int getMaxHpX() {
      return this.getMhpX();
   }

   public final int getMaxMpX() {
      return this.mmpX;
   }

   public short getIndieDamR() {
      return this.indieDamR;
   }

   public final List<Integer> getPetsCanConsume() {
      return this.petsCanConsume;
   }

   public final boolean isReturnScroll() {
      return this.skill && (this.sourceid == 80001040 || this.sourceid == 20021110);
   }

   public final boolean isMechChange() {
      switch(this.sourceid) {
      default:
         return false;
      }
   }

   public final int getRange() {
      return this.range;
   }

   public final short getER() {
      return this.er;
   }

   public final int getPrice() {
      return this.price;
   }

   public final int getExtendPrice() {
      return this.extendPrice;
   }

   public final byte getPeriod() {
      return this.period;
   }

   public final byte getReqGuildLevel() {
      return this.reqGuildLevel;
   }

   public final byte getEXPRate() {
      return this.expR;
   }

   public final short getLifeID() {
      return this.lifeId;
   }

   public final short getUseLevel() {
      return this.useLevel;
   }

   public final byte getSlotCount() {
      return this.slotCount;
   }

   public final short getStr() {
      return this.str;
   }

   public final short getStrX() {
      return this.strX;
   }

   public final short getDex() {
      return this.dex;
   }

   public final short getDexX() {
      return this.dexX;
   }

   public final short getInt() {
      return this.int_;
   }

   public final short getIntX() {
      return this.intX;
   }

   public final short getLuk() {
      return this.luk;
   }

   public final short getLukX() {
      return this.lukX;
   }

   public final short getComboConAran() {
      return this.comboConAran;
   }

   public final short getMPCon() {
      return this.mpCon;
   }

   public final short getMPConReduce() {
      return this.mpConReduce;
   }

   public final int getSoulMPCon() {
      return this.soulmpCon;
   }

   public final short getIndieMHp() {
      return this.indieMhp;
   }

   public final short getIndieMMp() {
      return this.indieMmp;
   }

   public final short getIndieAllStat() {
      return this.indieAllStat;
   }

   public final byte getType() {
      return this.type;
   }

   public int getBossDamage() {
      return this.getBdR();
   }

   public int getInterval() {
      return this.interval;
   }

   public ArrayList<Pair<Integer, Integer>> getAvailableMaps() {
      return this.availableMap;
   }

   public short getWDEFRate() {
      return this.pddR;
   }

   public short getMDEFRate() {
      return this.mddR;
   }

   public short getOnActive() {
      return this.onActive;
   }

   public boolean isDoubleDice() {
      switch(this.sourceid) {
      case 5120012:
      case 5220014:
      case 5320007:
      case 35120014:
         return true;
      default:
         return false;
      }
   }

   public int getPPCon() {
      return this.ppcon;
   }

   public int getPPRecovery() {
      return this.ppRecovery;
   }

   public int getEqskill1() {
      return this.eqskill1;
   }

   public void setEqskill1(int eqskill1) {
      this.eqskill1 = eqskill1;
   }

   public int getEqskill2() {
      return this.eqskill2;
   }

   public void setEqskill2(int eqskill2) {
      this.eqskill2 = eqskill2;
   }

   public int getEqskill3() {
      return this.eqskill3;
   }

   public void setEqskill3(int eqskill3) {
      this.eqskill3 = eqskill3;
   }

   public void setMaxHpX(int hp2) {
      this.setMhpX(hp2);
   }

   public short getPdd() {
      return this.pdd;
   }

   public void setPdd(short pdd) {
      this.pdd = pdd;
   }

   public short getIndiePmdR() {
      return this.indiePmdR;
   }

   public void setIndiePmdR(short IndiePmdR) {
      this.indiePmdR = IndiePmdR;
   }

   public int getQ() {
      return this.q;
   }

   public int getQ2() {
      return this.q2;
   }

   public int getV2() {
      return this.v2;
   }

   public int getMhpX() {
      return this.mhpX;
   }

   public void setMhpX(int mhpX) {
      this.mhpX = mhpX;
   }

   public short getLv2mhp() {
      return this.lv2mhp;
   }

   public void setLv2mhp(short lv2mhp) {
      this.lv2mhp = lv2mhp;
   }

   public Point getLt() {
      return this.lt;
   }

   public void setLt(Point lt) {
      this.lt = lt;
   }

   public long getStarttime() {
      return this.starttime;
   }

   public void setStarttime(long starttime) {
      this.starttime = starttime;
   }

   public short getBufftimeR() {
      return this.bufftimeR;
   }

   public void setBufftimeR(short bufftimeR) {
      this.bufftimeR = bufftimeR;
   }

   public short getLv2mmp() {
      return this.lv2mmp;
   }

   public void setLv2mmp(short lv2mmp) {
      this.lv2mmp = lv2mmp;
   }

   public short getIndieMhpR() {
      return this.indieMhpR;
   }

   public void setIndieMhpR(short indieMhpR) {
      this.indieMhpR = indieMhpR;
   }

   public int getS2() {
      return this.s2;
   }

   public void setS2(int s2) {
      this.s2 = s2;
   }

   public int getPPReq() {
      return this.ppReq;
   }

   public void setPPReq(int ppReq) {
      this.ppReq = ppReq;
   }

   public short getDotInterval() {
      return this.dotInterval;
   }

   public void setDotInterval(short dotInterval) {
      this.dotInterval = dotInterval;
   }

   public short getDotSuperpos() {
      return this.dotSuperpos;
   }

   public void setDotSuperpos(short dotSuperpos) {
      this.dotSuperpos = dotSuperpos;
   }

   public int getU2() {
      return this.u2;
   }

   public void setU2(int u2) {
      this.u2 = u2;
   }

   public short getIgnoreMobDamR() {
      return this.ignoreMobDamR;
   }

   public void setIgnoreMobDamR(short ignoreMobDamR) {
      this.ignoreMobDamR = ignoreMobDamR;
   }

   public short getKillRecoveryR() {
      return this.killRecoveryR;
   }

   public void setKillRecoveryR(short killRecoveryR) {
      this.killRecoveryR = killRecoveryR;
   }

   public int getW2() {
      return this.w2;
   }

   public void setW2(int w2) {
      this.w2 = w2;
   }

   public short getMaxDemonForce() {
      return this.mdf;
   }

   public short getForceCon() {
      return this.forceCon;
   }

   public void setForceCon(short forceCon) {
      this.forceCon = forceCon;
   }

   public short getBdR() {
      return this.bdR;
   }

   public void setBdR(short bdR) {
      this.bdR = bdR;
   }

   public double getExpRPerM() {
      return this.expRPerM;
   }

   public void setExpRPerM(double expRPerM) {
      this.expRPerM = expRPerM;
   }

   public short getHpFX() {
      return this.hpFX;
   }

   public void setHpFX(short hpFX) {
      this.hpFX = hpFX;
   }

   public short getSummonTimeR() {
      return this.summonTimeR;
   }

   public void setSummonTimeR(short summonTimeR) {
      this.summonTimeR = summonTimeR;
   }

   public short getTargetPlus() {
      return this.targetPlus;
   }

   public void setTargetPlus(short targetPlus) {
      this.targetPlus = targetPlus;
   }

   public short getTargetPlus_5th() {
      return this.targetPlus_5th;
   }

   public void setTargetPlus_5th(short targetPlus_5th) {
      this.targetPlus_5th = targetPlus_5th;
   }

   public short getIndieExp() {
      return this.IndieExp;
   }

   public short getSubprop() {
      return this.subprop;
   }

   public int getCoolRealTime() {
      return this.cooltime;
   }

   public final short getMPRCon() {
      return this.mpRCon;
   }

   public short getPsdSpeed() {
      return this.psdSpeed;
   }

   public void setPsdSpeed(short psdSpeed) {
      this.psdSpeed = psdSpeed;
   }

   public short getPsdJump() {
      return this.psdJump;
   }

   public void setPsdJump(short psdJump) {
      this.psdJump = psdJump;
   }

   public int getStanceProp() {
      return this.stanceProp;
   }

   public void setStanceProp(int stanceProp) {
      this.stanceProp = stanceProp;
   }

   public int getDamAbsorbShieldR() {
      return this.damAbsorbShieldR;
   }

   public void setDamAbsorbShieldR(int damAbsorbShieldR) {
      this.damAbsorbShieldR = damAbsorbShieldR;
   }

   public short getPdR() {
      return this.pdR;
   }

   public void setPdR(short pdR) {
      this.pdR = pdR;
   }

   public short getStrFX() {
      return this.strFX;
   }

   public void setStrFX(short strFX) {
      this.strFX = strFX;
   }

   public short getDexFX() {
      return this.dexFX;
   }

   public void setDexFX(short dexFX) {
      this.dexFX = dexFX;
   }

   public short getIntFX() {
      return this.intFX;
   }

   public void setIntFX(short intFX) {
      this.intFX = intFX;
   }

   public short getLukFX() {
      return this.lukFX;
   }

   public void setLukFX(short lukFX) {
      this.lukFX = lukFX;
   }

   public int getDamPlus() {
      return this.damPlus;
   }

   public short getPddX() {
      return this.pddX;
   }

   public short getMddX() {
      return this.mddX;
   }

   public short getArcX() {
      return this.arcX;
   }

   public short getNbdR() {
      return this.nbdR;
   }

   public short getStarX() {
      return this.starX;
   }

   public short getMdR() {
      return this.mdR;
   }

   public short getPadR() {
      return this.padR;
   }

   public short getMadR() {
      return this.madR;
   }

   public short getStrR() {
      return this.strR;
   }

   public short getDexR() {
      return this.dexR;
   }

   public short getIntR() {
      return this.intR;
   }

   public short getLukR() {
      return this.lukR;
   }

   public short getDropR() {
      return this.dropR;
   }

   public void setDropR(short dropR) {
      this.dropR = dropR;
   }

   public Point getRb() {
      return this.rb;
   }

   public void setRb(Point rb2) {
      this.rb = rb2;
   }

   public short getLv2pad() {
      return this.lv2pad;
   }

   public void setLv2pad(short lv2pad) {
      this.lv2pad = lv2pad;
   }

   public short getLv2mad() {
      return this.lv2mad;
   }

   public void setLv2mad(short lv2mad) {
      this.lv2mad = lv2mad;
   }

   public short getPddR() {
      return this.pddR;
   }

   public int getNocoolProps() {
      return this.nocoolProps;
   }

   public Point getLt2() {
      return this.lt2;
   }

   public void setLt2(Point lt2) {
      this.lt2 = lt2;
   }

   public Point getLt3() {
      return this.lt3;
   }

   public void setLt3(Point lt3) {
      this.lt3 = lt3;
   }

   public Point getRb2() {
      return this.rb2;
   }

   public void setRb2(Point rb2) {
      this.rb2 = rb2;
   }

   public Point getRb3() {
      return this.rb3;
   }

   public void setRb3(Point rb3) {
      this.rb3 = rb3;
   }

   public static class CancelDiseaseAction implements Runnable {
      private final WeakReference<MapleCharacter> target;
      private final Map<SecondaryStat, Pair<Integer, Integer>> statup;

      public CancelDiseaseAction(MapleCharacter target, Map<SecondaryStat, Pair<Integer, Integer>> statup) {
         this.target = new WeakReference(target);
         this.statup = statup;
      }

      public void run() {
         MapleCharacter realTarget = (MapleCharacter)this.target.get();
         if (realTarget != null) {
            realTarget.cancelDisease(this.statup, true);
         }

      }
   }
}
