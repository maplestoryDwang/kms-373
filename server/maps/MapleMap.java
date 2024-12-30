package server.maps;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import client.SecondaryStat;
import client.SkillFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import constants.KoreaCalendar;
import constants.ServerConstants;
import handling.channel.ChannelServer;
import handling.world.World;
import handling.world.party.MaplePartyCharacter;
import handling.world.party.PartyOperation;
import java.awt.Point;
import java.awt.Rectangle;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import scripting.EventInstanceManager;
import scripting.EventManager;
import scripting.NPCScriptManager;
import server.AdelProjectile;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.Obstacle;
import server.Randomizer;
import server.SecondaryStatEffect;
import server.SkillCustomInfo;
import server.Timer;
import server.events.MapleBattleGroundCharacter;
import server.field.boss.MapleBossManager;
import server.field.boss.demian.FlyingSwordNode;
import server.field.boss.demian.MapleDelayedAttack;
import server.field.boss.demian.MapleFlyingSword;
import server.field.boss.demian.MapleIncinerateObject;
import server.field.boss.lotus.MapleEnergySphere;
import server.field.boss.will.SpiderWeb;
import server.field.skill.MapleFieldAttackObj;
import server.field.skill.MapleMagicSword;
import server.field.skill.MapleMagicWreck;
import server.field.skill.MapleOrb;
import server.field.skill.MapleSecondAtom;
import server.field.skill.SecondAtom;
import server.field.skill.SpecialPortal;
import server.games.BattleGroundGameHandler;
import server.games.BloomingRace;
import server.life.EliteMonsterGradeInfo;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import server.life.MapleNPC;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.life.MonsterDropEntry;
import server.life.OverrideMonsterStats;
import server.life.SpawnPoint;
import server.life.SpawnPointAreaBoss;
import server.life.Spawns;
import server.polofritto.FrittoDancing;
import server.polofritto.MapleRandomPortal;
import server.quest.MapleQuest;
import tools.FileoutputUtil;
import tools.Pair;
import tools.StringUtil;
import tools.packet.BattleGroundPacket;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.MobPacket;
import tools.packet.PacketHelper;
import tools.packet.PetPacket;
import tools.packet.SLFCGPacket;
import tools.packet.SkillPacket;

public final class MapleMap {
   private final Map<MapleMapObjectType, ConcurrentHashMap<Integer, MapleMapObject>> mapobjects;
   private final List<MapleCharacter> characters = new CopyOnWriteArrayList();
   private ScheduledFuture<?> schedule;
   private int runningOid = 1;
   private final Lock runningOidLock = new ReentrantLock();
   public final List<Spawns> monsterSpawn = new ArrayList();
   private final Map<Integer, MaplePortal> portals = new HashMap();
   private MapleFootholdTree footholds = null;
   private final AtomicInteger spawnedMonstersOnMap = new AtomicInteger(0);
   private List<MapleMonster> RealSpawns = new ArrayList();
   private float monsterRate;
   private float recoveryRate;
   private MapleMapEffect mapEffect;
   private String fieldType = "";
   private byte channel;
   private boolean isBlackMage3thSkilled = false;
   private short decHP = 0;
   private short createMobInterval = 3000;
   private short top = 0;
   private short bottom = 0;
   private short left = 0;
   private short right = 0;
   private int consumeItemCoolTime = 0;
   private int protectItem = 0;
   private int decHPInterval = 10000;
   private int mapid;
   private int returnMapId;
   private int timeLimit;
   private int lucidCount = 0;
   private int lucidUseCount = 0;
   private int fieldLimit;
   private int maxRegularSpawn = 0;
   private int fixedMob;
   private int forcedReturnMap = 999999999;
   private int instanceid = -1;
   private int candles = 0;
   private int lightCandles = 0;
   private int reqTouched = 0;
   private int lvForceMove = 0;
   private int lvLimit = 0;
   private int permanentWeather = 0;
   private int partyBonusRate = 0;
   private int burning = 10;
   private int burningDecreasetime = 0;
   private int runeCurse = 0;
   private int stigmaDeath = 0;
   private int BattleGroundTimer = 0;
   private int BattleGroundMainTimer = 0;
   private boolean town;
   private boolean clock;
   private boolean personalShop;
   private boolean everlast = false;
   private boolean dropsDisabled = false;
   private boolean gDropsDisabled = false;
   private boolean soaring = false;
   private boolean squadTimer = false;
   private boolean isSpawns = true;
   private boolean checkStates = true;
   private boolean firstUserEnter = true;
   private boolean bingoGame = false;
   private boolean isEliteField = false;
   private String mapName;
   private String streetName;
   private String onUserEnter;
   private String 날짜;
   private String onFirstUserEnter;
   private String speedRunLeader = "";
   private List<Integer> dced = new ArrayList();
   private ScheduledFuture<?> squadSchedule;
   private ScheduledFuture<?> catchstart = null;
   private ScheduledFuture<?> eliteBossSchedule;
   private long speedRunStart = 0L;
   private long lastSpawnTime = 0L;
   private long lastHurtTime = 0L;
   private long timer = 0L;
   private long sandGlassTime = 0L;
   public long lastStigmaTime = 0L;
   public long lastIncinerateTime = 0L;
   public long burningIncreasetime = System.currentTimeMillis();
   private MapleNodes nodes;
   private List<MapleMagicWreck> wrecks = new ArrayList();
   private MapleRune rune;
   private Map<Integer, List<Integer>> monsterDefense = new LinkedHashMap();
   public String[] name = new String[10];
   public int voteamount = 0;
   public int runeCurseTime = 0;
   public boolean dead = false;
   public boolean MapiaIng = false;
   public boolean eliteBossAppeared = false;
   public String names = "";
   public String deadname = "";
   public int MapiaChannel;
   public int aftertime;
   public int nighttime;
   public int votetime;
   public int nightnumber = 0;
   public int eliteRequire = 0;
   public int killCount = 0;
   public int eliteCount = 0;
   public int citizenmap1;
   public int citizenmap2;
   public int citizenmap3;
   public int citizenmap4;
   public int citizenmap5;
   public int citizenmap6;
   public int mapiamap;
   public int policemap;
   public int drmap;
   public int morningmap;
   public int playern;
   public int mbating;
   private transient Map<Integer, SkillCustomInfo> customInfo = new LinkedHashMap();
   public static List<Pair<Integer, Point>> uniconflower = new ArrayList();
   private List<Point> willpoison = new ArrayList();
   private List<Point> LucidDream = new ArrayList();
   public int elitetime = 0;
   public int EliteMobCount = 0;
   public int EliteMobCommonCount = 0;
   public int elitebossrewardtype = 0;
   public int elitechmpcount = 0;
   public int elitechmptype = 0;
   public int PapulratusTime = 0;
   public int PapulratusPatan = 0;
   public int Papullatushour = 0;
   public int Papullatusminute = 0;
   public int Mapcoltime = 0;
   public int barrierArc = 0;
   public int barrierAut = 0;
   private boolean elitebossmap;
   private boolean elitebossrewardmap;
   private boolean eliteChmpmap;
   private boolean elitechmpfinal;
   public int partyquest = 0;
   public int moonstak = 0;
   public int Monstermarble = 0;
   public int mooncake = 0;
   public int rpportal = 0;
   public int RPTicket = 0;
   public int KerningPQ = 0;

   public MapleMap(int mapid, int channel, int returnMapId, float monsterRate) {
      this.mapid = mapid;
      this.channel = (byte)channel;
      this.returnMapId = returnMapId;
      this.eliteRequire = Randomizer.rand(500, 1500);
      if (this.returnMapId == 999999999) {
         this.returnMapId = mapid;
      }

      if (GameConstants.getPartyPlay(mapid) > 0) {
         this.monsterRate = (monsterRate - 1.0F) * 2.5F + 1.0F;
      } else {
         this.monsterRate = monsterRate;
      }

      this.monsterRate *= 2.0F;
      Map<MapleMapObjectType, ConcurrentHashMap<Integer, MapleMapObject>> objsMap = new ConcurrentHashMap();
      MapleMapObjectType[] var6 = MapleMapObjectType.values();
      int var7 = var6.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         MapleMapObjectType type = var6[var8];
         objsMap.put(type, new ConcurrentHashMap());
      }

      this.mapobjects = Collections.unmodifiableMap(objsMap);
      this.schedule = Timer.BuffTimer.getInstance().register(new MapleMap.MapleMapManagement(), 1000L);
      this.날짜 = GameConstants.getDate();
   }

   public void checkDropItems(long time) {
      if (this.getAllItemsThreadsafe().size() > 0) {
         Iterator items = this.getAllItemsThreadsafe().iterator();

         while(items.hasNext()) {
            MapleMapItem item = (MapleMapItem)items.next();
            if (item.shouldExpire(time)) {
               item.expire(this);
            } else if (item.shouldFFA(time)) {
               item.setDropType((byte)2);
            }
         }
      }

   }

   public final void setSpawns(boolean fm) {
      this.isSpawns = fm;
   }

   public final boolean getSpawns() {
      return this.isSpawns;
   }

   public final void setFixedMob(int fm) {
      this.fixedMob = fm;
   }

   public final void setForceMove(int fm) {
      this.lvForceMove = fm;
   }

   public final int getForceMove() {
      return this.lvForceMove;
   }

   public final void setLevelLimit(int fm) {
      this.lvLimit = fm;
   }

   public final int getLevelLimit() {
      return this.lvLimit;
   }

   public final void setReturnMapId(int rmi) {
      this.returnMapId = rmi;
   }

   public final void setSoaring(boolean b) {
      this.soaring = b;
   }

   public void MapiaMorning(final MapleCharacter player) {
      this.broadcastMessage(CField.getClock(this.aftertime));
      final java.util.Timer m_timer = new java.util.Timer();
      TimerTask m_task = new TimerTask() {
         public void run() {
            m_timer.cancel();
            MapleMap.this.MapiaVote(player);
         }
      };
      m_timer.schedule(m_task, (long)(this.aftertime * 1000));
   }

   public void MapiaVote(final MapleCharacter player) {
      if (this.nightnumber == 0) {
         this.MapiaCompare(player);
      } else {
         this.broadcastMessage(CField.musicChange("Wps.img/VOTE"));
         this.broadcastMessage(CField.getClock(this.votetime));
         this.broadcastMessage(CWvsContext.serverNotice(5, "", "투표를 진행하시기 바랍니다. 제한시간은 30초 입니다."));
         this.names = "";

         MapleCharacter chr;
         for(Iterator var2 = this.getCharacters().iterator(); var2.hasNext(); chr.isVoting = true) {
            chr = (MapleCharacter)var2.next();
            String var10001 = this.names;
            this.names = var10001 + chr.getName() + ",";
         }

         int i = false;
         final java.util.Timer m_timer = new java.util.Timer();
         TimerTask m_task = new TimerTask() {
            public void run() {
               m_timer.cancel();
               MapleMap.this.MapiaCompare(player);
            }
         };
         m_timer.schedule(m_task, (long)(this.votetime * 1000));
      }

   }

   public void MapiaComparable(final MapleCharacter player) {
      int playernum = 0;

      for(Iterator var3 = this.getCharacters().iterator(); var3.hasNext(); ++playernum) {
         MapleCharacter chr = (MapleCharacter)var3.next();
      }

      int i = 0;
      int ii = 0;
      int iii = 0;
      int citizen = 0;
      String deadname = "";
      String deadjob = "";
      String guessname = "";
      Iterator var10 = this.getCharacters().iterator();

      MapleCharacter chr;
      while(var10.hasNext()) {
         chr = (MapleCharacter)var10.next();
         if (chr.getpolicevote == 1 && !chr.isDead && chr.mapiajob == "마피아") {
            ++iii;
         }

         if (chr.getmapiavote == 1 && !chr.isDead) {
            if (chr.getdrvote < 1 && !chr.isDead) {
               chr.isDead = true;
               deadname = chr.getName();
               deadjob = chr.mapiajob;
               chr.warp(910141020);
               chr.dropMessage(1, "당신은 마피아에게 암살 당하였습니다.");
               ++i;
            } else {
               chr.dropMessage(6, "의사가 당신을 살렸습니다.");
               ++ii;
            }
         }

         if (chr.mapiajob == "시민" && !chr.isDead) {
            ++citizen;
         }
      }

      var10 = this.getCharacters().iterator();

      while(var10.hasNext()) {
         chr = (MapleCharacter)var10.next();
         if (iii > 0) {
            chr.dropMessage(6, "경찰은 마피아를 찾았습니다.");
         } else {
            chr.dropMessage(5, "경찰은 마피아를 찾지 못하였습니다.");
         }

         if (i == 0) {
            if (ii > 0) {
               chr.dropMessage(6, "의사는 마피아가 암살하려던 사람을 살렸습니다.");
            } else {
               chr.dropMessage(5, "마피아는 아무도 죽이지 못하였습니다.");
            }
         } else {
            chr.dropMessage(5, "의사는 아무도 살리지 못했습니다.");
            chr.dropMessage(5, "마피아는 " + deadname + "님을 죽였습니다. 그의 직업은 " + deadjob + " 이었습니다.");
         }
      }

      if (citizen == 0) {
         final java.util.Timer m_timer = new java.util.Timer();
         TimerTask m_task = new TimerTask() {
            public void run() {
               m_timer.cancel();
               MapleMap.this.MapiaWin(player);
            }
         };
         m_timer.schedule(m_task, 15000L);
      } else {
         this.MapiaMorning(player);
      }

   }

   public void MapiaWin(MapleCharacter player) {
      long fuck = (long)ChannelServer.getInstance(player.getClient().getChannel()).getMapFactory().getMap(234567899).mbating;
      int fuckingmapia = 0;
      ChannelServer.getInstance(player.getClient().getChannel()).getMapFactory().getMap(234567899).mbating = 0;
      this.MapiaIng = false;
      this.nightnumber = 0;
      int rand;
      if (this.MapiaChannel == 1) {
         int chan = 20;
         World.Broadcast.broadcastMessage(CWvsContext.serverNotice(8, "", "[마피아 알림] " + chan + "세이상 채널에서 마피아의 승리로 게임이 종료 되었습니다."));
      } else {
         rand = this.MapiaChannel + 1;
         World.Broadcast.broadcastMessage(CWvsContext.serverNotice(8, "", "[마피아 알림] " + rand + "채널에서 마피아의 승리로 게임이 종료 되었습니다."));
      }

      rand = Randomizer.rand(50, 100);
      Iterator var6 = this.getCharacters().iterator();

      MapleCharacter chr;
      while(var6.hasNext()) {
         chr = (MapleCharacter)var6.next();
         if (chr.mapiajob.equals("마피아")) {
            ++fuckingmapia;
         }

         chr.isDead = false;
         chr.isDrVote = false;
         chr.isMapiaVote = false;
         chr.isPoliceVote = false;
         chr.getdrvote = 0;
         chr.getmapiavote = 0;
         chr.getpolicevote = 0;
         chr.voteamount = 0;
         chr.dropMessage(5, "수고하셨습니다. 이번 게임은 마피아의 승리입니다!!");
      }

      for(var6 = this.getCharacters().iterator(); var6.hasNext(); chr.warp(910141020)) {
         chr = (MapleCharacter)var6.next();
         if (chr.mapiajob.equals("마피아")) {
            chr.gainMeso(fuck / (long)fuckingmapia, false);
            chr.dropMessage(6, "마피아 게임 승리 보상으로 " + fuck / (long)fuckingmapia + "메소를 지급해드렸습니다.");
         }
      }

   }

   public void CitizenWin(MapleCharacter player) {
      long fuck = (long)ChannelServer.getInstance(player.getClient().getChannel()).getMapFactory().getMap(234567899).mbating;
      int fucks = 0;
      ChannelServer.getInstance(player.getClient().getChannel()).getMapFactory().getMap(234567899).mbating = 0;
      this.MapiaIng = false;
      int rand;
      if (this.MapiaChannel == 1) {
         int chan = 20;
         World.Broadcast.broadcastMessage(CWvsContext.serverNotice(8, "", "[마피아 알림] " + chan + "세이상 채널에서 시민의 승리로 게임이 종료 되었습니다."));
      } else {
         rand = this.MapiaChannel + 1;
         World.Broadcast.broadcastMessage(CWvsContext.serverNotice(8, "", "[마피아 알림] " + rand + " 채널에서 시민의 승리로 게임이 종료 되었습니다."));
      }

      rand = Randomizer.rand(10, 80);
      int rand2 = Randomizer.rand(30, 100);
      Iterator var7 = this.getCharacters().iterator();

      MapleCharacter chr;
      while(var7.hasNext()) {
         chr = (MapleCharacter)var7.next();
         if (!chr.mapiajob.equals("마피아")) {
            fuck += (long)chr.mbating;
         }

         if (!chr.isDead) {
            ++fucks;
         }

         chr.isDead = false;
         chr.isDrVote = false;
         chr.isMapiaVote = false;
         chr.isPoliceVote = false;
         chr.getdrvote = 0;
         chr.getmapiavote = 0;
         chr.getpolicevote = 0;
         chr.voteamount = 0;
         chr.dropMessage(5, "수고하셨습니다. 이번 게임은 시민의 승리입니다!!");
      }

      for(var7 = this.getCharacters().iterator(); var7.hasNext(); chr.warp(910141020)) {
         chr = (MapleCharacter)var7.next();
         if (!chr.mapiajob.equals("마피아") && !chr.isDead) {
            chr.gainMeso(fuck / (long)fucks, false);
            chr.dropMessage(6, "마피아 게임 승리 보상으로 " + fuck / (long)fucks + "메소를 지급해드렸습니다.");
         }
      }

      this.nightnumber = 0;
   }

   public void MapiaCompare(MapleCharacter player) {
      int[] voteamount = new int[this.playern];
      String[] charinfo = new String[2];
      int j = 0;
      Iterator var5 = this.getCharacters().iterator();

      while(var5.hasNext()) {
         MapleCharacter chr = (MapleCharacter)var5.next();
         if (!chr.isDead) {
            voteamount[j] = chr.voteamount;
            ++j;
         }
      }

      int mapia = 0;
      Arrays.sort(voteamount);

      try {
         Iterator var11 = this.getCharacters().iterator();

         MapleCharacter chr;
         while(var11.hasNext()) {
            chr = (MapleCharacter)var11.next();
            if (chr.voteamount == voteamount[this.playern - 1]) {
               charinfo[0] = chr.getName();
               charinfo[1] = chr.mapiajob;
            }
         }

         if (voteamount[this.playern - 1] == voteamount[this.playern - 2]) {
            for(var11 = this.getCharacters().iterator(); var11.hasNext(); chr.dropMessage(5, "잠시 후 밤이 됩니다.")) {
               chr = (MapleCharacter)var11.next();
               if (this.nightnumber == 0) {
                  chr.dropMessage(6, "첫째날 낮이 지나고 밤이 찾아옵니다.");
               } else {
                  chr.dropMessage(6, "투표 결과 아무도 죽지 않았습니다.");
               }
            }

            this.MapiaNight(player);
         } else {
            var11 = this.getCharacters().iterator();

            while(var11.hasNext()) {
               chr = (MapleCharacter)var11.next();
               if (charinfo[0] == chr.getName()) {
                  chr.dropMessage(1, "진행자>>당신은 투표 결과로 인해 처형당하였습니다.");
                  chr.isDead = true;
               } else {
                  chr.dropMessage(6, "투표 결과 " + charinfo[0] + " 님이 처형당했습니다.");
                  chr.dropMessage(6, charinfo[0] + " 님의 직업은 " + charinfo[1] + " 입니다.");
                  chr.dropMessage(5, "잠시 후 밤이 됩니다.");
               }

               if (chr.mapiajob == "마피아" && !chr.isDead) {
                  ++mapia;
               }
            }

            if (mapia == 0) {
               this.CitizenWin(player);
            } else {
               this.MapiaNight(player);
            }
         }

      } catch (Exception var9) {
         if (this.MapiaChannel == 1) {
            int chana = 20;
            World.Broadcast.broadcastMessage(CWvsContext.serverNotice(8, "", "[마피아 알림] " + chana + "세이상 채널에서 게임이 다시 활성화 되었습니다."));
         } else {
            int chana = this.MapiaChannel + 1;
            World.Broadcast.broadcastMessage(CWvsContext.serverNotice(8, "", "[마피아 알림] " + chana + " 채널에서 게임이 다시 활성화 되었습니다."));
         }

         this.MapiaIng = false;
         this.nightnumber = 0;
         Iterator var13 = this.getCharacters().iterator();

         while(var13.hasNext()) {
            MapleCharacter chr = (MapleCharacter)var13.next();
            chr.warp(ServerConstants.warpMap);
            chr.dropMessage(1, "오류 입니다. 운영자에게 문의 해 주세요.");
         }

      }
   }

   public void MapiaNight(final MapleCharacter player) {
      final int[] maps = new int[]{this.citizenmap1, this.citizenmap2, this.citizenmap3, this.citizenmap4, this.citizenmap5, this.citizenmap6};
      ++this.nightnumber;
      final java.util.Timer m_timer = new java.util.Timer();
      final List<MapleCharacter> chars = new ArrayList();
      TimerTask m_task = new TimerTask() {
         int status = 0;

         public void run() {
            int citizen = 0;
            Iterator var2;
            MapleCharacter chr;
            if (this.status == 0) {
               MapleMap.this.names = "";
               var2 = MapleMap.this.getCharacters().iterator();

               while(var2.hasNext()) {
                  chr = (MapleCharacter)var2.next();
                  if (!chr.isDead) {
                     chars.add(chr);
                     MapleMap var10000 = MapleMap.this;
                     String var10001 = var10000.names;
                     var10000.names = var10001 + chr.getName() + ",";
                     chr.isDrVote = false;
                     chr.isMapiaVote = false;
                     chr.isPoliceVote = false;
                     chr.getdrvote = 0;
                     chr.getmapiavote = 0;
                     chr.getpolicevote = 0;
                     chr.voteamount = 0;
                     if (chr.mapiajob == "시민") {
                        chr.warp(maps[citizen]);
                        chr.dropMessage(5, MapleMap.this.nightnumber + "번째 밤이 되었습니다. 마피아, 경찰, 의사가 투표를 모두 할때까지 잠시만 기다려 주세요.");
                        ++citizen;
                     } else if (chr.mapiajob == "마피아") {
                        chr.warp(MapleMap.this.mapiamap);
                        chr.isMapiaVote = true;
                        chr.dropMessage(5, MapleMap.this.nightnumber + "번째 밤이 되었습니다. 바로 옆의 엔피시를 통해 암살할 사람을 지목해 주세요. 제한시간은 " + MapleMap.this.nighttime + "초 입니다.");
                     } else if (chr.mapiajob == "경찰") {
                        chr.warp(MapleMap.this.policemap);
                        chr.isPoliceVote = true;
                        chr.dropMessage(5, MapleMap.this.nightnumber + "번째 밤이 되었습니다. 바로 옆의 엔피시를 통해 마피아 일것 같다는 사람을 지목 해 주세요. 제한시간은 " + MapleMap.this.nighttime + "초 입니다.");
                     } else if (chr.mapiajob == "의사") {
                        chr.warp(MapleMap.this.drmap);
                        chr.isDrVote = true;
                        chr.dropMessage(5, MapleMap.this.nightnumber + "번째 밤이 되었습니다. 바로 옆의 엔피시를 통해 살리고 싶은 사람을 지목 해 주세요. 제한시간은 " + MapleMap.this.nighttime + "초 입니다.");
                     }

                     chr.getClient().getSession().writeAndFlush(CField.getClock(MapleMap.this.nighttime));
                  }
               }

               this.status = 1;
            } else if (this.status == 1) {
               var2 = chars.iterator();

               while(var2.hasNext()) {
                  chr = (MapleCharacter)var2.next();
                  if (!chr.isDead) {
                     chr.isVoting = false;
                     chr.warp(MapleMap.this.morningmap);
                     chr.dropMessage(6, "아침이 되었습니다. 투표 결과를 발표하겠습니다.");
                  }
               }

               m_timer.cancel();
               chars.clear();
               MapleMap.this.MapiaComparable(player);
            }

         }
      };
      m_timer.schedule(m_task, 3000L, (long)(this.nighttime * 1000));
   }

   public final boolean canSoar() {
      return this.soaring;
   }

   public final void toggleDrops() {
      this.dropsDisabled = !this.dropsDisabled;
   }

   public final void setDrops(boolean b) {
      this.dropsDisabled = b;
   }

   public final void toggleGDrops() {
      this.gDropsDisabled = !this.gDropsDisabled;
   }

   public final int getId() {
      return this.mapid;
   }

   public final MapleMap getReturnMap() {
      return ChannelServer.getInstance(this.channel).getMapFactory().getMap(this.returnMapId);
   }

   public final int getReturnMapId() {
      return this.returnMapId;
   }

   public boolean isBlackMage3thSkill() {
      return this.isBlackMage3thSkilled;
   }

   public void setBlackMage3thSkill(boolean f) {
      this.isBlackMage3thSkilled = f;
   }

   public final int getForcedReturnId() {
      return this.forcedReturnMap;
   }

   public final MapleMap getForcedReturnMap() {
      return ChannelServer.getInstance(this.channel).getMapFactory().getMap(this.forcedReturnMap);
   }

   public final void setForcedReturnMap(int map) {
      this.forcedReturnMap = map;
   }

   public final float getRecoveryRate() {
      return this.recoveryRate;
   }

   public final void setRecoveryRate(float recoveryRate) {
      this.recoveryRate = recoveryRate;
   }

   public final int getFieldLimit() {
      return this.fieldLimit;
   }

   public final void setFieldLimit(int fieldLimit) {
      this.fieldLimit = fieldLimit;
   }

   public final String getFieldType() {
      return this.fieldType;
   }

   public final void setFieldType(String fieldType) {
      this.fieldType = fieldType;
   }

   public final void setCreateMobInterval(short createMobInterval) {
      this.createMobInterval = createMobInterval;
   }

   public final void setTimeLimit(int timeLimit) {
      this.timeLimit = timeLimit;
   }

   public final void setMapName(String mapName) {
      this.mapName = mapName;
   }

   public final String getMapName() {
      return this.mapName;
   }

   public final String getStreetName() {
      return this.streetName;
   }

   public final void setFirstUserEnter(String onFirstUserEnter) {
      this.onFirstUserEnter = onFirstUserEnter;
   }

   public final void setUserEnter(String onUserEnter) {
      this.onUserEnter = onUserEnter;
   }

   public final String getFirstUserEnter() {
      return this.onFirstUserEnter;
   }

   public final String getUserEnter() {
      return this.onUserEnter;
   }

   public final boolean hasClock() {
      return this.clock;
   }

   public final void setClock(boolean hasClock) {
      this.clock = hasClock;
   }

   public final boolean isTown() {
      return this.town;
   }

   public final boolean isLevelMob(MapleCharacter chr) {
      boolean already = false;
      Iterator var3 = this.getAllMonster().iterator();

      while(var3.hasNext()) {
         MapleMonster monster = (MapleMonster)var3.next();
         if (chr.isGM()) {
            return true;
         }

         if (!monster.getStats().isBoss() && monster.getStats().getLevel() - 21 <= chr.getLevel() && chr.getLevel() <= monster.getStats().getLevel() + 21) {
            already = true;
            break;
         }
      }

      return already;
   }

   public final void setTown(boolean town) {
      this.town = town;
   }

   public final boolean allowPersonalShop() {
      return this.personalShop;
   }

   public final void setPersonalShop(boolean personalShop) {
      this.personalShop = personalShop;
   }

   public final void setStreetName(String streetName) {
      this.streetName = streetName;
   }

   public final void setEverlast(boolean everlast) {
      this.everlast = everlast;
   }

   public final boolean getEverlast() {
      return this.everlast;
   }

   public final int getHPDec() {
      return this.decHP;
   }

   public final void setHPDec(int delta) {
      if (delta > 0 || this.mapid == 749040100) {
         this.lastHurtTime = System.currentTimeMillis();
      }

      this.decHP = (short)delta;
   }

   public final int getHPDecInterval() {
      return this.decHPInterval;
   }

   public final void setHPDecInterval(int delta) {
      this.decHPInterval = delta;
   }

   public final int getHPDecProtect() {
      return this.protectItem;
   }

   public final void setHPDecProtect(int delta) {
      this.protectItem = delta;
   }

   public final int getCurrentPartyId() {
      Iterator ltr = this.characters.iterator();

      MapleCharacter chr;
      do {
         if (!ltr.hasNext()) {
            return -1;
         }

         chr = (MapleCharacter)ltr.next();
      } while(chr.getParty() == null);

      return chr.getParty().getId();
   }

   public final void addMapObject(MapleMapObject mapobject) {
      this.runningOidLock.lock();

      int newOid;
      try {
         newOid = ++this.runningOid;
      } finally {
         this.runningOidLock.unlock();
      }

      mapobject.setObjectId(newOid);
      ((ConcurrentHashMap)this.mapobjects.get(mapobject.getType())).put(newOid, mapobject);
   }

   private void spawnAndAddRangedMapObject(MapleMapObject mapobject, MapleMap.DelayedPacketCreation packetbakery) {
      this.addMapObject(mapobject);
      Iterator itr = this.characters.iterator();

      while(true) {
         MapleCharacter chr;
         do {
            if (!itr.hasNext()) {
               return;
            }

            chr = (MapleCharacter)itr.next();
         } while(mapobject.getType() != MapleMapObjectType.MIST && !(chr.getTruePosition().distanceSq(mapobject.getTruePosition()) <= GameConstants.maxViewRangeSq()));

         if (mapobject.getType() == MapleMapObjectType.MONSTER) {
            MapleMonster mob = (MapleMonster)mapobject;
            if (mob.getOwner() != -1) {
               if (mob.getOwner() == chr.getId()) {
                  packetbakery.sendPackets(chr.getClient());
               }
            } else {
               packetbakery.sendPackets(chr.getClient());
            }
         } else {
            packetbakery.sendPackets(chr.getClient());
         }

         chr.addVisibleMapObject(mapobject);
      }
   }

   public final void removeMapObject(MapleMapObject obj) {
      ((ConcurrentHashMap)this.mapobjects.get(obj.getType())).remove(obj.getObjectId());
   }

   public final Point calcPointBelow(Point initial) {
      MapleFoothold fh = this.footholds.findBelow(initial);
      if (fh == null) {
         return null;
      } else {
         int dropY = fh.getY1();
         if (!fh.isWall() && fh.getY1() != fh.getY2()) {
            double s1 = (double)Math.abs(fh.getY2() - fh.getY1());
            double s2 = (double)Math.abs(fh.getX2() - fh.getX1());
            if (fh.getY2() < fh.getY1()) {
               dropY = fh.getY1() - (int)(Math.cos(Math.atan(s2 / s1)) * (double)Math.abs(initial.x - fh.getX1()) / Math.cos(Math.atan(s1 / s2)));
            } else {
               dropY = fh.getY1() + (int)(Math.cos(Math.atan(s2 / s1)) * (double)Math.abs(initial.x - fh.getX1()) / Math.cos(Math.atan(s1 / s2)));
            }
         }

         if (initial.x < this.getLeft()) {
            initial.x = this.getLeft() + 100;
         } else if (initial.x > this.getRight()) {
            initial.x = this.getRight() - 100;
         }

         return new Point(initial.x, dropY);
      }
   }

   public final Point calcDropPos(Point initial, Point fallback) {
      Point ret = this.calcPointBelow(new Point(initial.x, initial.y - 50));
      return ret == null ? fallback : ret;
   }

   private void dropFromMonster(MapleCharacter chr, MapleMonster mob, boolean instanced) {
      if (mob != null && chr != null && ChannelServer.getInstance(this.channel) != null) {
         byte d = 1;
         Point pos = new Point(0, mob.getTruePosition().y);
         double showdown = 100.0D;
         MonsterStatusEffect mse = mob.getBuff(MonsterStatus.MS_Showdown);
         if (mse != null) {
            showdown += (double)mse.getValue();
         }

         MapleMonsterInformationProvider mi = MapleMonsterInformationProvider.getInstance();
         List<MonsterDropEntry> derp = new ArrayList();
         derp.addAll(mi.retrieveDrop(mob.getId()));
         List<MonsterDropEntry> customs = new ArrayList();
         int itemId;
         if (chr.getParty() != null && (mob.isHellMode() || mob.isExtreme())) {
            itemId = chr.getParty().getMembers().size();
            int quantity = itemId == 1 ? 6 : (itemId == 2 ? 5 : (itemId == 3 ? 4 : (itemId == 4 ? 3 : (itemId == 5 ? 2 : (itemId == 6 ? 1 : 0)))));
            switch(mob.getId()) {
            case 8644655:
            case 8645066:
            case 8880405:
            case 8880518:
            case 8880614:
               if (mob.isHellMode()) {
                  customs.add(new MonsterDropEntry(4319996, 9999999, quantity, quantity, 0, 0));
               } else if (mob.isExtreme()) {
                  customs.add(new MonsterDropEntry(4319995, 9999999, quantity, quantity, 0, 0));
               }
            }
         }

         if (mob.getId() == 9390612 || mob.getId() == 9390610 || mob.getId() == 9390611 || mob.getId() == 8645066) {
            MapleCharacter pchr = chr.getClient().getChannelServer().getPlayerStorage().getCharacterById(chr.getParty().getLeader().getId());
            if (pchr == null) {
               chr.dropMessage(5, "파티의 상태가 변경되어 골럭스 원정대가 해체됩니다.");
               chr.setKeyValue(200106, "golrux_in", "0");
            } else {
               switch((int)pchr.getKeyValue(200106, "golrux_diff")) {
               case 1:
                  customs.add(new MonsterDropEntry(4000000, 499999, 1, 5, 0));
                  customs.add(new MonsterDropEntry(4000001, 499999, 2, 6, 0));
                  customs.add(new MonsterDropEntry(4000002, 499999, 3, 7, 0));
                  break;
               case 2:
                  customs.add(new MonsterDropEntry(4000000, 499999, 1, 5, 0));
                  customs.add(new MonsterDropEntry(4000001, 499999, 2, 6, 0));
                  customs.add(new MonsterDropEntry(4000002, 499999, 3, 7, 0));
                  break;
               case 3:
                  customs.add(new MonsterDropEntry(4000000, 499999, 1, 5, 0));
                  customs.add(new MonsterDropEntry(4000001, 499999, 2, 6, 0));
                  customs.add(new MonsterDropEntry(4000002, 499999, 3, 7, 0));
                  break;
               case 4:
                  customs.add(new MonsterDropEntry(4000000, 499999, 1, 5, 0));
                  customs.add(new MonsterDropEntry(4000001, 499999, 2, 6, 0));
                  customs.add(new MonsterDropEntry(4000002, 499999, 3, 7, 0));
               }
            }
         }

         if (mob.isEliteboss()) {
            customs.add(new MonsterDropEntry(2432398, 1000000, 1, 15, 0));
         }

         if (mob.isElitemonster() && !mob.isUserunespawn()) {
            customs.add(new MonsterDropEntry(5062009, 1000000, 1, 15, 0));
            customs.add(new MonsterDropEntry(5062010, 1000000, 1, 5, 0));
            customs.add(new MonsterDropEntry(5062500, 1000000, 1, 5, 0));
            customs.add(new MonsterDropEntry(4001832, 500000, 10, 100, 0));
            customs.add(new MonsterDropEntry(2435719, 250000, 1, 2, 0));
            customs.add(new MonsterDropEntry(4021031, 100000, 1, 10, 0));
            customs.add(new MonsterDropEntry(4310012, 100000, 5, 30, 0));
         }

         if (mob.getCustomValue0(1) == 1L) {
            customs.add(new MonsterDropEntry(5062009, 1000000, 10, 30, 0));
            customs.add(new MonsterDropEntry(5062010, 1000000, 10, 30, 0));
            customs.add(new MonsterDropEntry(5062500, 1000000, 10, 30, 0));
            customs.add(new MonsterDropEntry(5062503, 500000, 5, 10, 0));
            customs.add(new MonsterDropEntry(5069000, 250000, 1, 1, 0));
            customs.add(new MonsterDropEntry(5069001, 100000, 1, 1, 0));
            customs.add(new MonsterDropEntry(4310012, 100000, 50, 100, 0));
            customs.add(new MonsterDropEntry(4001832, 1000000, 50, 300, 0));
            customs.add(new MonsterDropEntry(4021031, 1000000, 5, 30, 0));
            customs.add(new MonsterDropEntry(4310016, 1000000, 1, 1, 0));
            customs.add(new MonsterDropEntry(5064400, 500000, 1, 1, 0));
            customs.add(new MonsterDropEntry(4310005, 250000, 1, 3, 0));
            customs.add(new MonsterDropEntry(2049752, 100000, 1, 1, 0));
            customs.add(new MonsterDropEntry(2048716, 100000, 1, 5, 0));
            customs.add(new MonsterDropEntry(2048717, 250000, 1, 5, 0));
            customs.add(new MonsterDropEntry(2048753, 100000, 1, 2, 0));
         }

         if (chr.getBuffedValue(80003046)) {
            customs.add(new MonsterDropEntry(2633343, chr.isGM() ? 9999999 : '썐', 1, 1, 0));
         }

         switch(mob.getId()) {
         case 9300173:
            customs.add(new MonsterDropEntry(4001161, 50000, 1, 1, 0));
            break;
         case 9300175:
            customs.add(new MonsterDropEntry(4001169, 10000000, 1, 1, 0));
            break;
         case 9300452:
         case 9300453:
            customs.add(new MonsterDropEntry(4001528, 50000, 1, 1, 0));
         }

         if (chr.getV("arcane_quest_2") != null && Integer.parseInt(chr.getV("arcane_quest_2")) >= 0 && Integer.parseInt(chr.getV("arcane_quest_2")) < 6) {
            switch(mob.getId()) {
            case 8641000:
               customs.add(new MonsterDropEntry(4034922, 1000000, 1, 1, 0));
               break;
            case 8641001:
               customs.add(new MonsterDropEntry(4034923, 1000000, 1, 1, 0));
               break;
            case 8641002:
               customs.add(new MonsterDropEntry(4034924, 1000000, 1, 1, 0));
               break;
            case 8641003:
               customs.add(new MonsterDropEntry(4034925, 1000000, 1, 1, 0));
               break;
            case 8641004:
               customs.add(new MonsterDropEntry(4034926, 1000000, 1, 1, 0));
               break;
            case 8641005:
               customs.add(new MonsterDropEntry(4034927, 1000000, 1, 1, 0));
               break;
            case 8641006:
               customs.add(new MonsterDropEntry(4034928, 1000000, 1, 1, 0));
               break;
            case 8641007:
               customs.add(new MonsterDropEntry(4034929, 1000000, 1, 1, 0));
               break;
            case 8641008:
               customs.add(new MonsterDropEntry(4034930, 1000000, 1, 1, 0));
            }
         }

         if (chr.getV("arcane_quest_3") != null && Integer.parseInt(chr.getV("arcane_quest_3")) >= 0 && Integer.parseInt(chr.getV("arcane_quest_3")) < 4) {
            switch(mob.getId()) {
            case 8642000:
            case 8642001:
            case 8642002:
            case 8642003:
            case 8642004:
            case 8642005:
            case 8642006:
            case 8642007:
            case 8642008:
            case 8642009:
            case 8642010:
            case 8642011:
            case 8642012:
            case 8642013:
            case 8642014:
            case 8642015:
               customs.add(new MonsterDropEntry(4036571, 1000000, 1, 1, 0));
            }
         }

         if (chr.getV("arcane_quest_4") != null && Integer.parseInt(chr.getV("arcane_quest_4")) >= 0 && Integer.parseInt(chr.getV("arcane_quest_4")) < 4) {
            switch(mob.getId()) {
            case 8643000:
            case 8643001:
            case 8643002:
            case 8643003:
            case 8643004:
            case 8643005:
            case 8643006:
            case 8643007:
            case 8643008:
            case 8643009:
            case 8643010:
            case 8643011:
            case 8643012:
               customs.add(new MonsterDropEntry(4036572, 1000000, 1, 1, 0));
            }
         }

         if (chr.getV("arcane_quest_5") != null && Integer.parseInt(chr.getV("arcane_quest_5")) >= 0 && Integer.parseInt(chr.getV("arcane_quest_5")) < 4) {
            switch(mob.getId()) {
            case 8644000:
            case 8644001:
            case 8644002:
            case 8644003:
            case 8644004:
            case 8644005:
            case 8644006:
               customs.add(new MonsterDropEntry(4036573, 1000000, 1, 1, 0));
               break;
            case 8644007:
            case 8644008:
            case 8644009:
            case 8644010:
               customs.add(new MonsterDropEntry(4036574, 1000000, 1, 1, 0));
            }
         }

         if (chr.getV("arcane_quest_6") != null && Integer.parseInt(chr.getV("arcane_quest_6")) >= 0 && Integer.parseInt(chr.getV("arcane_quest_6")) < 4) {
            switch(mob.getId()) {
            case 8644400:
               customs.add(new MonsterDropEntry(4036333, 1000000, 1, 1, 0));
               break;
            case 8644401:
               customs.add(new MonsterDropEntry(4036333, 1000000, 1, 1, 0));
               break;
            case 8644402:
               customs.add(new MonsterDropEntry(4036329, 1000000, 1, 1, 0));
               customs.add(new MonsterDropEntry(4036330, 1000000, 1, 1, 0));
               break;
            case 8644403:
               customs.add(new MonsterDropEntry(4036330, 1000000, 1, 1, 0));
               customs.add(new MonsterDropEntry(4036331, 1000000, 1, 1, 0));
               break;
            case 8644404:
               customs.add(new MonsterDropEntry(4036330, 1000000, 1, 1, 0));
               break;
            case 8644405:
               customs.add(new MonsterDropEntry(4036332, 1000000, 1, 1, 0));
               break;
            case 8644406:
               customs.add(new MonsterDropEntry(4036332, 1000000, 1, 1, 0));
               break;
            case 8644407:
               customs.add(new MonsterDropEntry(4036334, 1000000, 1, 1, 0));
            case 8644408:
            case 8644409:
            case 8644411:
            default:
               break;
            case 8644410:
               customs.add(new MonsterDropEntry(4036335, 1000000, 1, 1, 0));
               break;
            case 8644412:
               customs.add(new MonsterDropEntry(4036336, 1000000, 1, 1, 0));
            }
         }

         if (chr.getV("arcane_quest_7") != null && Integer.parseInt(chr.getV("arcane_quest_7")) >= 0 && Integer.parseInt(chr.getV("arcane_quest_7")) < 4) {
            switch(mob.getId()) {
            case 8644500:
               customs.add(new MonsterDropEntry(4036398, 1000000, 1, 1, 0));
               break;
            case 8644501:
               customs.add(new MonsterDropEntry(4036399, 1000000, 1, 1, 0));
               break;
            case 8644502:
               customs.add(new MonsterDropEntry(4036400, 1000000, 1, 1, 0));
               break;
            case 8644503:
               customs.add(new MonsterDropEntry(4036401, 1000000, 1, 1, 0));
               break;
            case 8644504:
               customs.add(new MonsterDropEntry(4036402, 1000000, 1, 1, 0));
               break;
            case 8644505:
               customs.add(new MonsterDropEntry(4036403, 1000000, 1, 1, 0));
               break;
            case 8644506:
               customs.add(new MonsterDropEntry(4036404, 1000000, 1, 1, 0));
               break;
            case 8644507:
               customs.add(new MonsterDropEntry(4036405, 1000000, 1, 1, 0));
               break;
            case 8644508:
               customs.add(new MonsterDropEntry(4036406, 1000000, 1, 1, 0));
               break;
            case 8644509:
               customs.add(new MonsterDropEntry(4036407, 1000000, 1, 1, 0));
               break;
            case 8644510:
               customs.add(new MonsterDropEntry(4036406, 1000000, 1, 1, 0));
               break;
            case 8644511:
               customs.add(new MonsterDropEntry(4036407, 1000000, 1, 1, 0));
            }
         }

         itemId = 1182193;
         String var29 = this.날짜;
         byte var14 = -1;
         switch(var29.hashCode()) {
         case 44552:
            if (var29.equals("금")) {
               var14 = 2;
            }
            break;
         case 47785:
            if (var29.equals("목")) {
               var14 = 3;
            }
            break;
         case 49688:
            if (var29.equals("수")) {
               var14 = 4;
            }
            break;
         case 50900:
            if (var29.equals("월")) {
               var14 = 6;
            }
            break;
         case 51068:
            if (var29.equals("일")) {
               var14 = 0;
            }
            break;
         case 53664:
            if (var29.equals("토")) {
               var14 = 1;
            }
            break;
         case 54868:
            if (var29.equals("화")) {
               var14 = 5;
            }
         }

         switch(var14) {
         case 0:
            ++itemId;
         case 1:
            ++itemId;
         case 2:
            ++itemId;
         case 3:
            ++itemId;
         case 4:
            ++itemId;
         case 5:
            ++itemId;
         case 6:
            break;
         default:
            itemId = 0;
         }

         if (itemId != 0) {
            customs.add(new MonsterDropEntry(itemId, 5, 1, 1, 0));
         }

         int[] items = new int[]{1004422, 1004423, 1004424, 1004425, 1004426, 1052882, 1052887, 1052888, 1052889, 1052890, 1073030, 1073035, 1073032, 1073033, 1073034, 1082636, 1082637, 1082638, 1082639, 1082640, 1102775, 1102794, 1102795, 1102796, 1102797, 1152174, 1152179, 1152176, 1152177, 1152178, 1212115, 1213017, 1222109, 1232109, 1242116, 1242120, 1262017, 1272016, 1282016, 1292017, 1302333, 1312199, 1322250, 1332274, 1342101, 1362135, 1372222, 1382259, 1402251, 1412177, 1422184, 1432214, 1442268, 1452252, 1462239, 1472261, 1482216, 1492231, 1522138, 1532144, 1582017, 1592019};
         int[] items2 = new int[]{1004808, 1004809, 1004810, 1004811, 1004812, 1053063, 1053064, 1053065, 1053066, 1053067, 1073158, 1073159, 1073160, 1073161, 1073162, 1082695, 1082696, 1082697, 1082698, 1082699, 1102940, 1102941, 1102942, 1102943, 1102944, 1152196, 1152197, 1152198, 1152199, 1152200, 1212120, 1213018, 1222113, 1232113, 1242121, 1242122, 1262039, 1272017, 1282017, 1292018, 1302343, 1312203, 1322255, 1332279, 1342104, 1362140, 1372228, 1382265, 1402259, 1412181, 1422189, 1432218, 1442274, 1452257, 1462243, 1472265, 1482221, 1492235, 1522143, 1532150, 1582023, 1592020};
         if (mob.isExtreme() || mob.isHellMode()) {
            int[] var15;
            int var16;
            int var17;
            int item;
            switch(mob.getId()) {
            case 8880177:
            case 8880302:
               var15 = items2;
               var16 = items2.length;

               for(var17 = 0; var17 < var16; ++var17) {
                  item = var15[var17];
                  customs.add(new MonsterDropEntry(item, 125, 1, 1, 0));
               }
            case 8880101:
            case 8950002:
               var15 = items;
               var16 = items.length;

               for(var17 = 0; var17 < var16; ++var17) {
                  item = var15[var17];
                  customs.add(new MonsterDropEntry(item, 250, 1, 1, 0));
               }
            }
         }

         Iterator var31 = ServerConstants.NeoPosList.iterator();

         while(var31.hasNext()) {
            Pair<Integer, Integer> list = (Pair)var31.next();
            if ((Integer)list.getLeft() == mob.getId()) {
               if (ServerConstants.Event_MapleLive) {
                  customs.add(new MonsterDropEntry(2633609, 9999999, (Integer)list.getRight(), (Integer)list.getRight(), 0, 0));
               }
               break;
            }
         }

         List<MonsterDropEntry> finals = new ArrayList();
         List<MonsterDropEntry> realfinals = new ArrayList();
         finals.addAll(derp);
         finals.addAll(customs);
         if (GameConstants.isContentsMap(this.getId()) || chr.getMapId() / 100000 == 9530 || chr.getMapId() / 100000 == 9540 || mob.getId() >= 9833935 && mob.getId() <= 9833946 || mob.getId() >= 9833947 && mob.getId() <= 9833958) {
            finals.clear();
         }

         double dropBuff = chr.getStat().dropBuff;
         if (mob.getStats().isBoss()) {
            dropBuff = Math.min(dropBuff, 300.0D);
         } else {
            dropBuff = Math.min(dropBuff, 200.0D);
         }

         if (Calendar.getInstance().get(7) == 7) {
            dropBuff += 15.0D;
         }

         if (chr.getSkillLevel(80001536) > 0) {
            dropBuff += 20.0D;
         }

         Iterator var19 = finals.iterator();

         while(true) {
            MonsterDropEntry de;
            do {
               if (!var19.hasNext()) {
                  if (realfinals != null && !realfinals.isEmpty()) {
                     Collections.shuffle(realfinals);
                     boolean mesoDropped = false;
                     Iterator var38 = realfinals.iterator();

                     while(true) {
                        while(true) {
                           MonsterDropEntry de;
                           do {
                              if (!var38.hasNext()) {
                                 return;
                              }

                              de = (MonsterDropEntry)var38.next();
                           } while(de.itemId == mob.getStolen());

                           if (de.privated == 0 && (mob.getStats().isBoss() || de.itemId == 4001847 || de.itemId == 4001849 || de.itemId == 2434851)) {
                              if (chr.getParty() != null) {
                                 Iterator var22 = chr.getParty().getMembers().iterator();

                                 while(var22.hasNext()) {
                                    MaplePartyCharacter pc = (MaplePartyCharacter)var22.next();
                                    if (pc.isOnline() && pc.getId() != chr.getId()) {
                                       MapleCharacter player = chr.getClient().getChannelServer().getPlayerStorage().getCharacterById(pc.getId());
                                       if (player != null) {
                                          this.drop(mesoDropped, de, mob, player, pos, d, realfinals.size());
                                       }
                                    }
                                 }
                              }

                              this.drop(mesoDropped, de, mob, chr, pos, d, realfinals.size());
                              ++d;
                           } else {
                              this.drop(mesoDropped, de, mob, chr, pos, d, realfinals.size());
                              ++d;
                           }
                        }
                     }
                  }

                  return;
               }

               de = (MonsterDropEntry)var19.next();
            } while(de.itemId == mob.getStolen());

            double var10000;
            double d1;
            if (GameConstants.getInventoryType(de.itemId) == MapleInventoryType.EQUIP && (mob.isExtreme() || mob.isHellMode())) {
               if (de.chance < 250) {
                  var10000 = (double)de.chance;
               } else {
                  var10000 = 250.0D * dropBuff / 100.0D * showdown / 100.0D;
               }
            } else {
               d1 = (double)de.chance * dropBuff / 100.0D * showdown / 100.0D;
            }

            if (chr.getParty() != null) {
               Iterator var23 = chr.getParty().getMembers().iterator();

               label406:
               while(true) {
                  while(true) {
                     MapleCharacter player;
                     do {
                        MaplePartyCharacter pc;
                        do {
                           do {
                              if (!var23.hasNext()) {
                                 break label406;
                              }

                              pc = (MaplePartyCharacter)var23.next();
                           } while(!pc.isOnline());
                        } while(pc.getId() == chr.getId());

                        player = chr.getClient().getChannelServer().getPlayerStorage().getCharacterById(pc.getId());
                     } while(player == null);

                     if (GameConstants.getInventoryType(de.itemId) == MapleInventoryType.EQUIP && (mob.isExtreme() || mob.isHellMode())) {
                        if (de.chance < 250) {
                           var10000 = (double)de.chance;
                        } else {
                           var10000 = 250.0D * dropBuff / 100.0D * showdown / 100.0D;
                        }
                     } else {
                        d1 = (double)de.chance * dropBuff / 100.0D * showdown / 100.0D;
                     }
                  }
               }
            }

            if (GameConstants.getInventoryType(de.itemId) != MapleInventoryType.EQUIP || !mob.isExtreme() && !mob.isHellMode()) {
               d1 = (double)de.chance * dropBuff / 100.0D * showdown / 100.0D;
            } else {
               d1 = de.chance < 250 ? (double)de.chance : 250.0D * dropBuff / 100.0D * showdown / 100.0D;
            }

            if (chr.getKeyValue(210416, "TotalDeadTime") > 0L) {
               d1 = (double)((long)(d1 * 0.2D));
            }

            if (chr.getMapId() == 921170004) {
               d1 /= 5.0D;
            }

            if (Randomizer.nextInt(999999) < (int)d1) {
               realfinals.add(de);
            }
         }
      }
   }

   public void drop(boolean mesoDropped, MonsterDropEntry de, MapleMonster mob, MapleCharacter chr, Point pos, byte d) {
      this.drop(mesoDropped, de, mob, chr, pos, d, 0);
   }

   public void drop(boolean mesoDropped, MonsterDropEntry de, MapleMonster mob, MapleCharacter chr, Point pos, byte d, int total) {
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      byte droptype = (byte)(mob.getStats().isExplosiveReward() && mob.getStats().isBoss() ? 3 : (chr.getParty() != null ? 1 : 0));
      int mobpos = mob.getTruePosition().x;
      int cmServerrate = ChannelServer.getInstance(this.channel).getMesoRate();
      if (GameConstants.isYeti(chr.getJob()) || GameConstants.isPinkBean(chr.getJob())) {
         cmServerrate = 1;
      }

      if (!mesoDropped || droptype == 3 || de.itemId != 0) {
         if (de.questid <= 0 || chr.getQuestStatus(de.questid) == 1) {
            String[] wishCoinCheck;
            int a;
            Iterator var14;
            Pair list;
            if (de.itemId == 2633304) {
               if (Integer.parseInt(chr.getClient().getKeyValue("WishCoinWeekGain")) >= 400) {
                  return;
               }

               wishCoinCheck = chr.getClient().getKeyValue("WishCoin").split("");
               a = 0;

               for(var14 = ServerConstants.NeoPosList.iterator(); var14.hasNext(); ++a) {
                  list = (Pair)var14.next();
                  if ((Integer)list.getLeft() == mob.getId() && Integer.parseInt(wishCoinCheck[a]) == 1) {
                     return;
                  }
               }
            }

            if (de.itemId == 2633609) {
               if (chr.getClient().getCustomKeyValueStr(501468, "reward") == null) {
                  String bosslist = "";

                  for(a = 0; a < ServerConstants.NeoPosList.size(); ++a) {
                     bosslist = bosslist + "0";
                  }

                  chr.getClient().setCustomKeyValue(501468, "reward", bosslist);
               }

               wishCoinCheck = chr.getClient().getCustomKeyValueStr(501468, "reward").split("");
               a = 0;

               for(var14 = ServerConstants.NeoPosList.iterator(); var14.hasNext(); ++a) {
                  list = (Pair)var14.next();
                  if ((Integer)list.getLeft() == mob.getId() && Integer.parseInt(wishCoinCheck[a]) == 1) {
                     return;
                  }
               }
            }

            Point poss = new Point(mob.getTruePosition().x, mob.getTruePosition().y);
            if (total > 5) {
               a = total / 2;
               if (d < a) {
                  poss.x -= 30 * (a - d);
               } else if (d >= a) {
                  poss.x += 30 * (d - a);
               }

               pos = poss;
            } else if (droptype == 3) {
               pos.x = mobpos + (d % 2 == 0 ? 40 * (d + 1) / 2 : -(40 * d / 2));
            } else {
               pos.x = mobpos + (d % 2 == 0 ? 20 * (d + 1) / 2 : -(20 * d / 2));
            }

            MapleFoothold fh2 = this.footholds.findBelow(pos);
            if (fh2 != null) {
               if (fh2.getX1() > pos.x) {
                  pos.x = fh2.getX1();
               } else if (fh2.getX2() < pos.x) {
                  pos.x = fh2.getX2();
               }
            }

            if (de.itemId == 0) {
               int mesos = Randomizer.nextInt(1 + Math.abs(de.Maximum - de.Minimum)) + de.Minimum;
               if (mesos > 0) {
                  if (GameConstants.isLinkMap(chr.getMapId())) {
                     chr.gainMeso((long)mesos, true);
                  } else {
                     double mesobuff = Math.min(chr.getStat().mesoBuff, 300.0D);
                     if (Calendar.getInstance().get(7) == 1) {
                        mesobuff += 15.0D;
                     }

                     if (chr.getSkillLevel(80001535) > 0) {
                        mesobuff += 20.0D;
                     }

                     this.spawnMobMesoDrop((int)((double)mesos * mesobuff / 100.0D * (double)chr.getDropMod() * (double)cmServerrate), this.calcDropPos(pos, mob.getTruePosition()), mob, chr, false, droptype);
                  }

                  mesoDropped = true;
               }
            } else if (!GameConstants.isLinkMap(chr.getMapId())) {
               Item idrop;
               if (GameConstants.getInventoryType(de.itemId) != MapleInventoryType.EQUIP) {
                  int range = Math.abs(de.Maximum - de.Minimum);
                  idrop = new Item(de.itemId, (short)0, (short)(de.Maximum != 1 ? Randomizer.nextInt(range <= 0 ? 1 : range) + de.Minimum : 1), 0);
                  if (mob.isExtreme() || mob.isHellMode()) {
                     switch(mob.getId()) {
                     case 8880101:
                     case 8880177:
                     case 8880302:
                     case 8950002:
                        idrop.setQuantity((short)(idrop.getQuantity() * 2));
                     }
                  }
               } else {
                  idrop = ii.getEquipById(de.itemId);
                  if (mob.isExtreme() || mob.isHellMode()) {
                     int[] items = new int[]{1004422, 1004423, 1004424, 1004425, 1004426, 1052882, 1052887, 1052888, 1052889, 1052890, 1073030, 1073035, 1073032, 1073033, 1073034, 1082636, 1082637, 1082638, 1082639, 1082640, 1102775, 1102794, 1102795, 1102796, 1102797, 1152174, 1152179, 1152176, 1152177, 1152178, 1212115, 1213017, 1222109, 1232109, 1242116, 1242120, 1262017, 1272016, 1282016, 1292017, 1302333, 1312199, 1322250, 1332274, 1342101, 1362135, 1372222, 1382259, 1402251, 1412177, 1422184, 1432214, 1442268, 1452252, 1462239, 1472261, 1482216, 1492231, 1522138, 1532144, 1582017, 1592019};
                     int[] items2 = new int[]{1004808, 1004809, 1004810, 1004811, 1004812, 1053063, 1053064, 1053065, 1053066, 1053067, 1073158, 1073159, 1073160, 1073161, 1073162, 1082695, 1082696, 1082697, 1082698, 1082699, 1102940, 1102941, 1102942, 1102943, 1102944, 1152196, 1152197, 1152198, 1152199, 1152200, 1212120, 1213018, 1222113, 1232113, 1242121, 1242122, 1262039, 1272017, 1282017, 1292018, 1302343, 1312203, 1322255, 1332279, 1342104, 1362140, 1372228, 1382265, 1402259, 1412181, 1422189, 1432218, 1442274, 1452257, 1462243, 1472265, 1482221, 1492235, 1522143, 1532150, 1582023, 1592020};
                     int[] var17 = items;
                     int var18 = items.length;

                     int var19;
                     int item;
                     Equip equip;
                     for(var19 = 0; var19 < var18; ++var19) {
                        item = var17[var19];
                        if (item == idrop.getItemId()) {
                           equip = (Equip)idrop;
                           equip.addTotalDamage((byte)10);
                        }
                     }

                     var17 = items2;
                     var18 = items2.length;

                     for(var19 = 0; var19 < var18; ++var19) {
                        item = var17[var19];
                        if (item == idrop.getItemId()) {
                           equip = (Equip)idrop;
                           equip.addTotalDamage((byte)10);
                        }
                     }

                     Equip equip;
                     switch(mob.getId()) {
                     case 8880101:
                        if (idrop.getItemId() == 1022278 || idrop.getItemId() == 1672077) {
                           equip = (Equip)idrop;
                           equip.addTotalDamage((byte)10);
                        }
                        break;
                     case 8880177:
                        if (idrop.getItemId() == 1132308) {
                           equip = (Equip)idrop;
                           equip.addTotalDamage((byte)10);
                        }
                        break;
                     case 8880302:
                        if (idrop.getItemId() == 1162080 || idrop.getItemId() == 1162081 || idrop.getItemId() == 1162082 || idrop.getItemId() == 1162083) {
                           equip = (Equip)idrop;
                           equip.addTotalDamage((byte)15);
                        }
                        break;
                     case 8950002:
                        if (idrop.getItemId() == 1012632) {
                           equip = (Equip)idrop;
                           equip.addTotalDamage((byte)10);
                        }
                     }
                  }
               }

               String var10001 = StringUtil.getAllCurrentTime();
               idrop.setGMLog(var10001 + "에 " + this.mapid + "맵에서 " + mob.getId() + "를 잡고 얻은 아이템.");
               if ((mob.getId() == 8820212 || mob.getId() == 8880302 || mob.getId() == 8880342 || mob.getId() == 8880150 || mob.getId() == 8880151 || mob.getId() == 8880155 || mob.getId() == 8644650 || mob.getId() == 8644655 || mob.getId() == 8880405 || mob.getId() == 8645009 || mob.getId() == 8645039 || mob.getId() == 8880504 || mob.getId() == 8880602 || mob.getId() == 8880614 || mob.getId() == 8820101 || mob.getId() == 8820001 || mob.getId() == 8860005 || mob.getId() == 8860000 || mob.getId() == 8810018 || mob.getId() == 8810122 || mob.getId() == 8840007 || mob.getId() == 8840000 || mob.getId() == 8840014 || mob.getId() == 8800102 || mob.getId() == 8500012 || mob.getId() == 8500002 || mob.getId() == 8880200 || mob.getId() == 8870000 || mob.getId() == 8800002 || mob.getId() == 8500022 || mob.getId() == 8870100 || mob.getId() == 8910100 || mob.getId() == 8930100 || mob.getId() == 8910000 || mob.getId() == 8930000 || mob.getId() == 8850011 || mob.getId() == 8850111 || mob.getId() == 8950102 || mob.getId() == 8950002 || mob.getId() == 8880111 || mob.getId() == 8880101 || mob.getId() >= 8900100 && mob.getId() <= 8900102 || mob.getId() >= 8900000 && mob.getId() <= 8900002 || mob.getId() >= 8920000 && mob.getId() <= 8920003 || mob.getId() >= 8920100 && mob.getId() <= 8920103 || mob.getId() == 8920106 || mob.getId() == 8900103 || mob.getId() == 8880157 || mob.getId() == 8880167 || mob.getId() == 8880177 || mob.getId() == 8880614) && chr.getPlayer() != null) {
                  String var10000 = FileoutputUtil.보스획득아이템;
                  int var31 = chr.getClient().getAccID();
                  FileoutputUtil.log(var10000, "[획득] 계정 번호 : " + var31 + " | 파티번호 : " + chr.getPlayer().getParty().getId() + " | 캐릭터 : " + chr.getName() + "(" + chr.getId() + ") | 보스 클리어 " + MapleLifeFactory.getMonster(mob.getId()).getStats().getName() + "(" + mob.getId() + ") | 드롭 : " + MapleItemInformationProvider.getInstance().getName(idrop.getItemId()) + "(" + idrop.getItemId() + ")를 [" + idrop.getQuantity() + "]개 획득");
               }

               if (idrop.getItemId() == 4001886) {
                  idrop.setBossid(mob.getId());
               }

               if (de.privated != 0 || !mob.getStats().isBoss() && idrop.getItemId() != 4001847 && idrop.getItemId() != 4001849 && idrop.getItemId() != 2434851 && mob.getScale() <= 100) {
                  this.spawnMobDrop(idrop, this.calcDropPos(pos, mob.getTruePosition()), mob, chr, droptype, de.questid);
               } else {
                  this.spawnMobPublicDrop(idrop, this.calcDropPos(pos, mob.getTruePosition()), mob, chr, droptype, de.questid);
               }
            }

         }
      }
   }

   public void removeMonster(MapleMonster monster) {
      if (monster != null) {
         if (this.RealSpawns.contains(monster)) {
            this.RealSpawns.remove(monster);
         }

         this.broadcastMessage(MobPacket.killMonster(monster.getObjectId(), 0));
         this.removeMapObject(monster);
         this.spawnedMonstersOnMap.decrementAndGet();
         monster.killed();
      }
   }

   public void killMonster(MapleMonster monster, int effect) {
      if (monster != null) {
         monster.setHp(0L);
         if (monster.getLinkCID() <= 0 && !GameConstants.isContentsMap(this.getId())) {
            monster.spawnRevives(this);
         }

         if (this.RealSpawns.contains(monster)) {
            this.RealSpawns.remove(monster);
         }

         this.broadcastMessage(MobPacket.killMonster(monster.getObjectId(), effect));
         this.removeMapObject(monster);
         this.spawnedMonstersOnMap.decrementAndGet();
         monster.killed();
      }
   }

   public void killMonster(MapleMonster monster) {
      if (monster != null) {
         monster.setHp(0L);
         if (this.RealSpawns.contains(monster)) {
            this.RealSpawns.remove(monster);
         }

         if (monster.getLinkCID() <= 0 && !GameConstants.isContentsMap(this.getId())) {
            monster.spawnRevives(this);
         }

         this.broadcastMessage(MobPacket.killMonster(monster.getObjectId(), monster.getStats().getSelfD() < 0 ? 1 : monster.getStats().getSelfD()));
         this.removeMapObject(monster);
         this.spawnedMonstersOnMap.decrementAndGet();
         monster.killed();
      }
   }

   public void killAllMonster(MapleCharacter chr) {
      Iterator var2 = this.getAllMonstersThreadsafe().iterator();

      while(var2.hasNext()) {
         MapleMonster mob = (MapleMonster)var2.next();
         this.killMonster(mob, chr, false, false, (byte)0);
      }

   }

   public final void killMonster(MapleMonster monster, MapleCharacter chr, boolean withDrops, boolean second, byte animation) {
      this.killMonster(monster, chr, withDrops, second, animation, 0);
   }

   public final void killMonster(final MapleMonster monster, final MapleCharacter chr, boolean withDrops, boolean second, byte animation, int lastSkill) {
      boolean norespawn = false;
      MapleMapFactory name;
      int maxmob;
      int i;
      int i;
      if (chr.getBattleGroundChr() != null) {
         int point = 0;
         int exp = 0;
         if (monster.getStats().getName().contains("버섯")) {
            point = 30;
            exp = 25;
            norespawn = true;
         } else if (monster.getStats().getName().contains("돼지")) {
            point = 60;
            exp = 38;
            norespawn = true;
         } else if (monster.getStats().getName().contains("이블아이")) {
            point = 75;
            exp = 45;
            norespawn = true;
         } else if (monster.getStats().getName().contains("도라지")) {
            point = 100;
            exp = 80;
            norespawn = true;
         } else if (monster.getStats().getName().contains("천록")) {
            point = 140;
            exp = 120;
            norespawn = true;
         } else if (monster.getStats().getName().contains("크로노스")) {
            point = 200;
            exp = 170;
            norespawn = true;
         } else if (monster.getStats().getName().contains("파이렛")) {
            point = 260;
            exp = 220;
            norespawn = true;
         } else if (monster.getStats().getName().contains("요괴선사")) {
            point = 276;
            exp = 142;
            norespawn = true;
         } else if (monster.getStats().getName().contains("데스테니")) {
            point = 698;
            exp = 340;
            norespawn = true;
         } else if (monster.getStats().getName().contains("드래곤킹")) {
            point = 4000;
            exp = 1500;
            norespawn = true;
         }

         EventManager em2 = chr.getClient().getChannelServer().getEventSM().getEventManager("KerningPQ");
         if (monster.getId() == 9300912) {
            em2.setProperty("stage5", "clear");
            this.broadcastMessage(CField.environmentChange("Map/Effect.img/quest/party/clear", 4));
            this.broadcastMessage(CField.achievementRatio(100));
         }

         if (monster.getId() == 9300008 || monster.getId() == 9300014) {
            name = chr.getClient().getChannelServer().getMapFactory();
            maxmob = 0;

            for(i = 0; i < 5; ++i) {
               maxmob += name.getMap(922010401 + i).getNumMonsters();
            }

            if (maxmob == 0) {
               this.broadcastMessage(CField.environmentChange("Map/Effect.img/quest/party/clear", 4));
               this.broadcastMessage(CField.achievementRatio(25));
               this.broadcastMessage(CField.startMapEffect("다크아이와 쉐도우 아이를 모두 퇴치하였습니다. 엘로그린 벌룬에게 말을 걸어 다음 스테이지로 이동해주세요!", 5120018, true));
            }
         }

         if (monster.getId() == 9300010) {
            ++this.RPTicket;
            if (this.RPTicket == 4) {
               this.resetFully();
            } else if (this.RPTicket == 8) {
               this.resetFully();
            } else if (this.RPTicket == 12) {
               this.broadcastMessage(CField.environmentChange("Map/Effect.img/quest/party/clear", 4));
               this.broadcastMessage(CField.achievementRatio(50));
            }
         }

         if (monster.getId() == 9300012) {
            this.broadcastMessage(CField.environmentChange("Map/Effect.img/quest/party/clear", 4));
            this.broadcastMessage(CField.achievementRatio(100));
         }

         Timer.MapTimer.getInstance().schedule(new Runnable() {
            public void run() {
               if (!monster.getStats().getName().contains("드래곤킹")) {
                  MapleMap.this.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(monster.getId()), new Point(monster.getPosition().x, !monster.getStats().getName().contains("도라지") && !monster.getStats().getName().contains("요괴선사") ? monster.getPosition().y : monster.getPosition().y - 300));
               }

            }
         }, 7000L);
         chr.getBattleGroundChr().setExp(chr.getBattleGroundChr().getExp() + exp);
         chr.getBattleGroundChr().setMoney(chr.getBattleGroundChr().getMoney() + point);
         i = chr.getBattleGroundChr().getJobType();
         switch(chr.getBattleGroundChr().getJobType()) {
         case 6:
         case 7:
            ++i;
         case 8:
         default:
            break;
         case 9:
            i = 14;
            break;
         case 10:
            i = 16;
            break;
         case 11:
            i = 23;
            break;
         case 12:
            i = 24;
         }

         chr.updateSingleStat(MapleStat.EXP, (long)chr.getBattleGroundChr().getExp());
         chr.getBattleGroundChr().setTeam(2);
         this.broadcastMessage(chr, BattleGroundPacket.UpdateAvater(chr.getBattleGroundChr(), i), false);
         chr.getBattleGroundChr().setTeam(1);
         chr.getClient().send(BattleGroundPacket.UpdateAvater(chr.getBattleGroundChr(), i));
         if (point > 0) {
            chr.getClient().send(BattleGroundPacket.ShowPoint(monster, point));
         }
      }

      long var10003;
      if (chr.getPlayer().getKeyValue(100711, "point") > 9999L) {
         chr.getPlayer().setKeyValue(100711, "point", "9999");
      } else {
         ++chr.erdacount;
         if (chr.erdacount == 100) {
            chr.erdacount = 0;
            MapleCharacter var10000 = chr.getPlayer();
            var10003 = chr.getPlayer().getKeyValue(100711, "point");
            var10000.setKeyValue(100711, "point", (var10003 + 1L).makeConcatWithConstants<invokedynamic>(var10003 + 1L));
         }
      }

      if (monster.getBuff(MonsterStatus.MS_Treasure) != null) {
         MapleCharacter buffowner = null;
         Iterator var33 = this.getAllCharactersThreadsafe().iterator();

         while(var33.hasNext()) {
            MapleCharacter chr1 = (MapleCharacter)var33.next();
            if (chr.getBuffedValue(400021096)) {
               buffowner = chr1;
               break;
            }
         }

         if (buffowner != null) {
            SecondaryStatEffect a = SkillFactory.getSkill(400021104).getEffect(buffowner.getSkillLevel(400021096));
            MapleMist newmist = new MapleMist(a.calculateBoundingBox(buffowner.getPosition(), buffowner.isFacingLeft()), buffowner, a, (int)buffowner.getBuffLimit(400021096), (byte)(buffowner.isFacingLeft() ? 1 : 0));
            newmist.setPosition(monster.getPosition());
            newmist.setDelay(0);
            buffowner.getMap().spawnMist(newmist, false);
         }
      }

      if (GameConstants.isKhali(chr.getJob())) {
         int chance = 45;
         int random = Randomizer.rand(0, 100);
         if (chr.getSkillLevel(154120010) > 0) {
            chance = 70;
         }

         if ((chr.getBuffedValue(154111000) || monster.getStats().isBoss()) && Randomizer.isSuccess(chance)) {
            chr.SummonChakriHandler(chr, monster.getTruePosition(), false);
         }
      }

      if (this.mapid == 930000100 && this.getAllMonstersThreadsafe().size() == 0) {
         this.broadcastMessage(CField.environmentChange("Map/Effect.img/quest/party/clear", 4));
         this.broadcastMessage(CField.achievementRatio(15));
      }

      if (this.Monstermarble == 20) {
         this.killAllMonsters(true);
         this.broadcastMessage(CField.environmentChange("Map/Effect.img/quest/party/clear", 4));
         this.broadcastMessage(CField.startMapEffect("모든 몬스터 구슬을 획득하였습니다. 파티장이 엘린에게 말을 걸어 다음 스테이지로 이동하여 주세요.", 5120023, true));
         this.broadcastMessage(CField.achievementRatio(50));
      }

      if (monster.getId() == 9300182) {
         this.broadcastMessage(CField.environmentChange("Map/Effect.img/quest/party/clear", 4));
         this.broadcastMessage(CField.startMapEffect("포이즌 골렘을 퇴치하였습니다. 오른쪽의 포탈로 퇴장하실 수 있습니다.", 5120023, true));
         this.broadcastMessage(CField.achievementRatio(100));
      }

      if (this.mapid == 921160200 && this.getAllMonstersThreadsafe().size() == 0) {
         this.broadcastMessage(CField.environmentChange("Map/Effect.img/quest/party/clear", 4));
         this.broadcastMessage(CField.achievementRatio(20));
      } else if (this.mapid == 921160400 && this.getAllMonstersThreadsafe().size() == 0) {
         this.broadcastMessage(CField.environmentChange("Map/Effect.img/quest/party/clear", 4));
         this.broadcastMessage(CField.achievementRatio(50));
      }

      if (monster.getId() == 9300454) {
         this.broadcastMessage(CField.environmentChange("Map/Effect.img/quest/party/clear", 4));
         this.broadcastMessage(CField.achievementRatio(100));
         this.spawnNpc(9020006, new Point(-1639, -181));
      }

      MapleMonster mob;
      int x;
      MapleMonster Flower;
      MapleMonster m;
      MapleMonster m;
      MapleMapObject reactor1l;
      int[] Mmoby;
      MapleMonster mob;
      int[] itemArray;
      int[] Mmobx;
      MapleMonster EliteChmp;
      Item toDrop;
      Iterator var75;
      if (this.partyquest == 1) {
         itemArray = new int[]{9300900, 9300901};
         Mmobx = new int[]{-902, -759, -553, -416, -185, -42, 155, 329, 544, 700};
         Mmoby = new int[]{213, 273};
         if (this.getAllMonstersThreadsafe().size() == 0) {
            for(i = 0; i < Mmobx.length; ++i) {
               for(maxmob = 0; maxmob < Mmoby.length - 1; ++maxmob) {
                  mob = MapleLifeFactory.getMonster(itemArray[0]);
                  mob = MapleLifeFactory.getMonster(itemArray[1]);
                  this.spawnMonsterOnGroundBelow(mob, new Point(Mmobx[i], Mmoby[maxmob]));
                  this.spawnMonsterOnGroundBelow(mob, new Point(Mmobx[i], Mmoby[maxmob]));
               }
            }
         }
      } else if (this.partyquest == 2) {
         itemArray = new int[]{9300903, 9300904, 9300905};
         Mmobx = new int[]{585, -919, 599, -879};
         Mmoby = new int[]{-838, 693, -835, 568};
         int[] Mmobid3x = new int[]{659, -925, -947, 587};
         int[] Mmobid1y = new int[]{-356, -267, -536, -539};
         int[] Mmobid2y = new int[]{-448, -374, -640, -627};
         int[] Mmobid3y = new int[]{-444, -495, -387, -263};
         if (this.getAllMonstersThreadsafe().size() == 2) {
            for(x = 0; x < 4; ++x) {
               Flower = MapleLifeFactory.getMonster(itemArray[0]);
               m = MapleLifeFactory.getMonster(itemArray[1]);
               m = MapleLifeFactory.getMonster(itemArray[2]);
               this.spawnMonsterOnGroundBelow(Flower, new Point(Mmobx[x], Mmobid1y[x]));
               this.spawnMonsterOnGroundBelow(m, new Point(Mmoby[x], Mmobid2y[x]));
               this.spawnMonsterOnGroundBelow(m, new Point(Mmobid3x[x], Mmobid3y[x]));
            }
         }

         if (monster.getId() == 9300903 || monster.getId() == 9300904 || monster.getId() == 9300905) {
            ++this.moonstak;
         }

         if (this.getMoonCake() > 30 && this.getMoonCake() < 40) {
            this.broadcastMessage(CField.startMapEffect("의 떡 찧기가 탄력을 받기 시작했군!", 5120016, true));
         } else if (this.getMoonCake() > 60 && this.getMoonCake() < 70) {
            this.broadcastMessage(CField.startMapEffect("오오, 의 떡 찧기에 불이 붙었군!", 5120016, true));
         } else if (this.getMoonCake() > 79 && this.getMoonCake() < 100) {
            ++this.partyquest;
         }

         if (this.moonstak % 2 == 0) {
            this.moonstak = 0;
            EliteChmp = MapleLifeFactory.getMonster(9300906);
            toDrop = new Item(4001101, (short)0, (short)3, 0);
            this.spawnMobDrop(toDrop, new Point(-213, -192), EliteChmp, chr, (byte)1, 0);
            this.spawnMobPublicDrop(toDrop, new Point(-213, -192), EliteChmp, chr, (byte)2, 0);
            var75 = this.getMapObjectsInRange(chr.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER)).iterator();

            while(var75.hasNext()) {
               reactor1l = (MapleMapObject)var75.next();
               m = (MapleMonster)reactor1l;
               if (m.getId() == EliteChmp.getId()) {
                  this.killMonster(m, chr, true, false, (byte)1);
                  break;
               }
            }
         } else if (this.partyquest == 3) {
            this.killAllMonsters(true);
            this.broadcastMessage(CField.environmentChange("Map/Effect.img/quest/party/clear", 4));
            this.broadcastMessage(CField.startMapEffect("의 떡 80개를 다 모았군, 냠냠 정말 맛있군. 어흥이님을 찾아와.", 5120016, true));
         }

         if (monster.getId() == 9300906) {
            EliteChmp = MapleLifeFactory.getMonster(9300906);
            this.spawnMonsterOnGroundBelow(EliteChmp, new Point(-213, -192));
         }
      }

      if (this.getId() == 450001400) {
         chr.addSkillCustomInfo(450001400, 1L);
         chr.getClient().send(SLFCGPacket.ErdaSpectrumGauge((int)chr.getSkillCustomValue0(450001400), (int)chr.getSkillCustomValue0(8641018), (int)chr.getSkillCustomValue0(450001402)));
         chr.getClient().send(SLFCGPacket.EventSkillOnEffect(monster.getPosition().x, monster.getPosition().y, 2, 1));
      }

      if (this.RealSpawns.contains(monster)) {
         this.RealSpawns.remove(monster);
      }

      if (chr.getSkillLevel(160010001) > 0 || chr.getSkillLevel(80003058) > 0) {
         chr.handleNatureFriend();
      }

      int rand;
      boolean alreadyspawn;
      boolean map;
      if (monster.isExtreme() || monster.isHellMode()) {
         itemArray = new int[0];
         map = false;
         rand = Randomizer.rand(0, 1000);
         int prob = 50;
         switch(monster.getId()) {
         case 8644655:
            itemArray = new int[]{1113132, 1162084, 1132309};
            break;
         case 8645066:
            itemArray = new int[]{1113130, 1162086, 1012635};
            break;
         case 8880405:
            itemArray = new int[]{1113133, 1162085, 1032317};
            break;
         case 8880518:
            itemArray = new int[]{1113131, 1162087, 1182286, 1122431};
            break;
         case 8880614:
            itemArray = new int[]{1190303, 1122151, 1029999};
         }

         if (itemArray.length > 0) {
            alreadyspawn = false;
            i = Randomizer.rand(0, itemArray.length - 1);
            if (itemArray[i] <= 0) {
               maxmob = itemArray[0];
            } else {
               maxmob = itemArray[i];
            }

            MapleItemInformationProvider ii7 = MapleItemInformationProvider.getInstance();
            Item toDrop;
            if (GameConstants.getInventoryType(maxmob) == MapleInventoryType.EQUIP) {
               toDrop = ii7.getEquipById(maxmob);
            } else {
               toDrop = new Item(maxmob, (short)0, (short)1, 0);
            }

            if (toDrop != null && prob >= rand) {
               this.spawnMobPublicDrop(toDrop, monster.getTruePosition(), monster, chr, (byte)0, 0);
            }
         }
      }

      this.removeMapObject(monster);
      monster.killed();
      this.spawnedMonstersOnMap.decrementAndGet();
      chr.mobKilled(monster.getId(), lastSkill);
      boolean instanced = monster.getEventInstance() != null || this.getEMByMap() != null;

      MapleMonster mapleMonster;
      boolean animation;
      int size;
      String q;
      int k;
      int type;
      int j;
      Iterator var85;
      try {
         monster.killBy(chr, lastSkill);
         if (animation >= 0) {
            if (monster.getId() == 8220110) {
               this.broadcastMessage(MobPacket.killMonster(monster.getObjectId(), 80, 1));
            } else {
               this.broadcastMessage(MobPacket.killMonster(monster.getObjectId(), animation));
               if (monster.getSeperateSoul() > 0) {
                  return;
               }
            }
         }

         Iterator var50;
         if (GameConstants.isLinkMap(this.mapid)) {
            rand = chr.getLinkMobCount();
            chr.setLinkMobCount(rand - 1);
            chr.getClient().getSession().writeAndFlush(SLFCGPacket.FrozenLinkMobCount(rand - 1));
            if (rand <= 0) {
               chr.setLinkMobCount(0);
               var50 = this.getAllMonstersThreadsafe().iterator();

               while(var50.hasNext()) {
                  MapleMapObject monstermo = (MapleMapObject)var50.next();
                  mob = (MapleMonster)monstermo;
                  if (mob.getOwner() == chr.getId()) {
                     mob.setHp(0L);
                     this.broadcastMessage(MobPacket.killMonster(mob.getObjectId(), 1));
                     this.removeMapObject(mob);
                     mob.killed();
                  }
               }
            }
         }

         if (monster.getId() == 9300166) {
            animation = true;
         }

         chr.checkSpecialCoreSkills("killCount", monster.getObjectId(), (SecondaryStatEffect)null);
         if (monster.getId() >= 8850000 && monster.getId() <= 8850004 || monster.getId() >= 8850100 && monster.getId() <= 8850104) {
            MapleMonster cygnus;
            if (this.getMonsterById(8850011) != null) {
               cygnus = this.getMonsterById(8850011);
               this.broadcastMessage(CWvsContext.getTopMsg("시그너스가 자신의 심복이 당한것에 분노하여 모든것을 파괴하려 합니다."));
               this.broadcastMessage(MobPacket.setAfterAttack(cygnus.getObjectId(), 4, 1, 17, cygnus.isFacingLeft()));
            } else if (this.getMonsterById(8850111) != null) {
               cygnus = this.getMonsterById(8850111);
               this.broadcastMessage(CWvsContext.getTopMsg("시그너스가 자신의 심복이 당한것에 분노하여 모든것을 파괴하려 합니다."));
               this.broadcastMessage(MobPacket.setAfterAttack(cygnus.getObjectId(), 4, 1, 17, cygnus.isFacingLeft()));
            }
         }

         String[] qlist = new String[]{"TangYoons", "river", "chewchew", "rehelen", "arcana", "moras", "esfera"};
         String[] var52 = qlist;
         maxmob = qlist.length;

         for(i = 0; i < maxmob; ++i) {
            String qlis = var52[i];
            if (chr.getV(qlis + "_" + monster.getId() + "_isclear") != null && chr.getV(qlis + "_" + monster.getId() + "_count") != null && chr.getV(qlis + "_" + monster.getId() + "_mobq") != null && chr.getV(qlis + "_" + monster.getId() + "_isclear").equals("0") && Integer.parseInt(chr.getV(qlis + "_" + monster.getId() + "_count")) < Integer.parseInt(chr.getV(qlis + "_" + monster.getId() + "_mobq"))) {
               x = Integer.parseInt(chr.getV(qlis + "_" + monster.getId() + "_count")) + 1;
               chr.addKV(qlis + "_" + monster.getId() + "_count", x.makeConcatWithConstants<invokedynamic>(x));
               String var10002 = monster.getStats().getName();
               chr.dropMessage(-1, var10002 + " " + chr.getV(qlis + "_" + monster.getId() + "_count") + " / " + chr.getV(qlis + "_" + monster.getId() + "_mobq"));
               if (Integer.parseInt(chr.getV(qlis + "_" + monster.getId() + "_count")) >= Integer.parseInt(chr.getV(qlis + "_" + monster.getId() + "_mobq"))) {
                  chr.addKV(qlis + "_" + monster.getId() + "_isclear", "1");
               }
            }
         }

         boolean party;
         if (chr.getGuild() != null && chr.getGuildId() > 0) {
            party = false;
            if (chr.getParty() == null) {
               World.Guild.gainContribution(chr.getGuildId(), GameConstants.GPboss(monster.getId(), party, false), chr.getId());
            } else {
               if (chr.getParty().getMembers().size() >= 2) {
                  Iterator var56 = chr.getParty().getMembers().iterator();

                  while(var56.hasNext()) {
                     MaplePartyCharacter partychar = (MaplePartyCharacter)var56.next();
                     if (partychar != null && partychar.getMapid() == chr.getMapId() && partychar.getChannel() == this.channel) {
                        MapleCharacter other = chr.getClient().getChannelServer().getPlayerStorage().getCharacterByName(partychar.getName());
                        if (other.getId() != chr.getId() && other != null && other.getGuild() != null && other.getMapId() == chr.getMapId()) {
                           if (other.getGuildId() == chr.getGuildId()) {
                              party = true;
                              World.Guild.gainContribution(other.getGuildId(), GameConstants.GPboss(monster.getId(), party, false), other.getId());
                           } else {
                              World.Guild.gainContribution(other.getGuildId(), GameConstants.GPboss(monster.getId(), party, false), other.getId());
                           }
                        }
                     }
                  }
               }

               World.Guild.gainContribution(chr.getGuildId(), GameConstants.GPboss(monster.getId(), party, false), chr.getId());
            }
         }

         int Rand;
         if (this.isEliteChmpmap()) {
            party = false;
            alreadyspawn = this.isElitechmpfinal();
            List<Integer> killmoblist = new ArrayList();
            if (this.getElitechmptype() == 0) {
               for(Rand = 0; Rand <= 5; ++Rand) {
                  killmoblist.add(8220100 + Rand);
               }
            } else if (this.getElitechmptype() == 1) {
               for(Rand = 0; Rand <= 2; ++Rand) {
                  killmoblist.add(8220106 + Rand);
               }
            } else if (this.getElitechmptype() == 2) {
               for(Rand = 0; Rand <= 1; ++Rand) {
                  killmoblist.add(8220122 + Rand);
               }
            } else if (this.getElitechmptype() == 3) {
               for(Rand = 0; Rand <= 1; ++Rand) {
                  killmoblist.add(8220110 + Rand);
               }
            } else if (this.getElitechmptype() == 4) {
               for(Rand = 0; Rand <= 1; ++Rand) {
                  killmoblist.add(8220124 + Rand);
               }
            }

            Iterator var83 = killmoblist.iterator();

            while(var83.hasNext()) {
               Integer list = (Integer)var83.next();
               if (list == monster.getId()) {
                  this.setElitechmpcount(this.getElitechmpcount() - 1);
                  if (this.getElitechmpcount() <= 0) {
                     party = true;
                     break;
                  }
               }
            }

            if (!alreadyspawn && party) {
               if (this.getElitechmptype() == 2) {
                  this.setElitechmpcount(1);
                  this.setElitechmpfinal(true);
                  Rand = 8220123;
                  EliteChmp = MapleLifeFactory.getMonster(Rand);
                  EliteChmp.setEliteGrade(1);
                  EliteChmp.setEliteType(4);
                  EliteChmp.getStats().setLevel(chr.getLevel());
                  EliteChmp.setHp(monster.getStats().getHp() * 3L);
                  EliteChmp.setElitechmp(true);
                  this.spawnMonsterOnGroundBelow(EliteChmp, monster.getPosition());
                  this.broadcastMessage(CField.startMapEffect("숨어있던 어둠 늑대가 나타났습니다.", 5120124, true));
               } else if (this.getElitechmptype() == 4) {
                  this.setElitechmpcount(10);
                  this.setElitechmpfinal(true);

                  for(Rand = 0; Rand < 10; ++Rand) {
                     x = 8220124;
                     Flower = MapleLifeFactory.getMonster(x);
                     Flower.setEliteGrade(1);
                     Flower.setEliteType(4);
                     Flower.getStats().setLevel(chr.getLevel());
                     Flower.setScale(50);
                     Flower.setHp(monster.getStats().getHp() / 2L);
                     Flower.setElitechmp(true);
                     this.spawnMonsterOnGroundBelow(Flower, new Point(monster.getPosition().x + Randomizer.rand(-150, 150), monster.getPosition().y));
                  }

                  this.broadcastMessage(CField.startMapEffect("다크 가고일이 미니 다크 가고일로 분리되었습니다.", 5120124, true));
               }
            } else if (party && alreadyspawn) {
               this.setEliteChmpmap(false);
               this.setElitechmpcount(0);
               this.setElitechmptype(0);
               ++this.eliteCount;
               var83 = this.getAllMonster().iterator();

               while(true) {
                  while(var83.hasNext()) {
                     EliteChmp = (MapleMonster)var83.next();
                     if (monster.getId() != EliteChmp.getId() && (EliteChmp.getId() == 8220123 || EliteChmp.getId() == 8220122 || EliteChmp.getId() == 8220124 || EliteChmp.getId() == 8220125 || EliteChmp.getId() >= 8220100 && EliteChmp.getId() <= 8220108)) {
                        this.killMonster(EliteChmp.getId());
                     } else if (EliteChmp.getId() == 8220110) {
                        EliteChmp.setHp(0L);
                        if (this.RealSpawns.contains(monster)) {
                           this.RealSpawns.remove(monster);
                        }

                        this.broadcastMessage(MobPacket.killMonster(EliteChmp.getObjectId(), 80, 1));
                        this.removeMapObject(EliteChmp);
                        EliteChmp.killed();
                        this.spawnedMonstersOnMap.decrementAndGet();
                     }
                  }

                  for(Rand = 0; Rand < 3; ++Rand) {
                     x = monster.getPosition().x + Randomizer.rand(-300, 300);
                     size = monster.getPosition().y;
                     MapleFoothold fh = this.getFootholds().findBelow(new Point(x, size));
                     if (fh != null) {
                        this.spawnMobPublicDrop(new Item(2023927, (short)0, (short)1, 0), new Point(x, size), monster, chr, (byte)0, 0);
                     }
                  }

                  monster.setCustomInfo(1, 1, 0);
                  if (!monster.isElitemonster() && (chr.getKeyValue(51351, "startquestid") == 49012L || chr.getKeyValue(51351, "startquestid") == 49013L || chr.getKeyValue(51351, "startquestid") == 49014L)) {
                     Rand = chr.getKeyValue(51351, "startquestid") == 49012L ? 1 : (chr.getKeyValue(51351, "startquestid") == 49013L ? 2 : 3);
                     q = "";
                     size = Integer.parseInt(chr.getQuest(MapleQuest.getInstance((int)chr.getKeyValue(51351, "startquestid"))).getCustomData());
                     ++size;
                     if (size < 10) {
                        q = "00" + size;
                     } else if (size < 100) {
                        q = "0" + size;
                     } else {
                        q = size.makeConcatWithConstants<invokedynamic>(size);
                     }

                     if (size >= Rand) {
                        if (Rand < 10) {
                           q = "00" + Rand;
                        } else if (Rand < 100) {
                           q = "0" + Rand;
                        } else {
                           q = Rand.makeConcatWithConstants<invokedynamic>(Rand);
                        }
                     }

                     MapleQuest.getInstance((int)chr.getKeyValue(51351, "startquestid")).forceStart(chr, 0, q);
                     if (size >= Rand) {
                        chr.setKeyValue(51351, "queststat", "3");
                        chr.getClient().send(CWvsContext.updateSuddenQuest((int)chr.getKeyValue(51351, "midquestid"), false, PacketHelper.getKoreanTimestamp(System.currentTimeMillis()) + 600000000L, "count=1;Quest=" + chr.getKeyValue(51351, "startquestid") + ";state=3;"));
                     }
                  }

                  this.broadcastMessage(CField.startMapEffect(this.eliteCount <= 15 ? "어두운 기운이 사라지지 않아 이곳을 음산하게 만들고 있습니다." : "이곳이 어둠으로 가득차 곧 무슨일이 일어날 듯 합니다.", 5120124, true));
                  break;
               }
            }
         }

         Item toDrop;
         if (chr.getBuffedEffect(SecondaryStat.SoulMP) != null) {
            Item toDrop = new Item(4001536, (short)0, (short)Randomizer.rand(1, 5), 0);
            toDrop = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
            if (toDrop != null) {
               this.spawnSoul(monster, chr, toDrop, monster.getPosition(), toDrop);
            }
         }

         if ((monster.getId() == 8500003 || monster.getId() == 8500004 || monster.getId() == 8500007 || monster.getId() == 8500008) && Randomizer.isSuccess(100)) {
            party = false;
            if (monster.getId() != 8500007 && monster.getId() != 8500008) {
               i = Randomizer.rand(2437659, 2437664);
            } else {
               i = Randomizer.rand(2437606, 2437607);
            }

            toDrop = new Item(i, (short)0, (short)1, 0);
            this.spawnItemDrop(monster, chr, toDrop, new Point(monster.getTruePosition().x, monster.getTruePosition().y), true, false);
         }

         if (chr.getClient().getQuestStatus(100825) == 2 && (chr.isGM() || monster.getStats().getLevel() - 21 <= chr.getLevel() && chr.getLevel() <= monster.getStats().getLevel() + 21 && this.isSpawnPoint() && !GameConstants.isContentsMap(this.getId()) && !GameConstants.보스맵(this.getId()) && !GameConstants.사냥컨텐츠맵(this.getId()) && !GameConstants.튜토리얼(this.getId()) && !GameConstants.로미오줄리엣(this.getId()) && !GameConstants.피라미드(this.getId()))) {
            if (!monster.getStats().isBoss() && !monster.isElitemonster() && !monster.isElitechmp() && !monster.isEliteboss()) {
            }

            if (chr.getBuffedValue(80003064) && chr.getSkillCustomValue0(100857) <= 0L) {
               if (chr.getKeyValue(100857, "feverCnt") >= 10L) {
                  while(chr.getBuffedValue(80003064)) {
                     chr.cancelEffect(chr.getBuffedEffect(80003064));
                  }
               } else if (10L - chr.getKeyValue(100857, "feverCnt") > 0L || chr.isGM()) {
                  if (chr.getKeyValue(100857, "feverCnt") > 3L) {
                     chr.setKeyValue(100857, "state", "1");
                  }

                  if (chr.getKeyValue(100857, "count") <= 0L) {
                     chr.setKeyValue(100857, "count", "0");
                  }

                  chr.setKeyValue(100857, "count", (chr.getKeyValue(100857, "count") + (chr.isGM() ? 1L : 1L)).makeConcatWithConstants<invokedynamic>(chr.getKeyValue(100857, "count") + (chr.isGM() ? 1L : 1L)));
                  if (chr.getKeyValue(100857, "count") == 250L) {
                     if (Randomizer.isSuccess(50)) {
                        chr.getClient().send(CField.enforceMsgNPC(9062549, 3000, "신나는 #r팡팡 리액션#k을 하기까지\r\n절반 정도 왔어! 준비하라고!"));
                     } else {
                        chr.getClient().send(CField.enforceMsgNPC(9062549, 3000, "벌써 #r250마리#k의 몬스터를 처치!\r\n나한테 사냥을 배워서 그런가?\r\n정말 빠른걸!"));
                     }
                  } else if (chr.getKeyValue(100857, "count") >= 500L) {
                     if (chr.getClient().getCustomKeyValue(501567, "fever") < 0L) {
                        chr.getClient().setCustomKeyValue(501567, "fever", "0");
                     }

                     if (chr.getClient().getCustomKeyValue(501567, "fever") < 200L) {
                        MapleClient var112 = chr.getClient();
                        var10003 = chr.getClient().getCustomKeyValue(501567, "fever");
                        var112.setCustomKeyValue(501567, "fever", (var10003 + 1L).makeConcatWithConstants<invokedynamic>(var10003 + 1L));
                     }

                     chr.getClient().send(CField.enforceMSG("메이플 팡팡 리액션 타임! 어서 후원받은 팡팡 주머니를 터뜨려!", 341, 20000));
                     chr.getClient().send(CField.enforceMsgNPC(9062549, 3000, "후원이 터지는 이 순간!\r\n멋진 #r팡팡 리액션#k을 보여줘!"));
                     chr.setKeyValue(100857, "count", "0");
                     var10003 = chr.getKeyValue(100857, "feverCnt");
                     chr.setKeyValue(100857, "feverCnt", (var10003 + 1L).makeConcatWithConstants<invokedynamic>(var10003 + 1L));
                     chr.getClient().send(CField.PangPangReactionReady(80003064, 2900));
                     Timer.EtcTimer.getInstance().schedule(() -> {
                        chr.getClient().send(CField.PangPangReactionAct(60000));
                        chr.setSkillCustomInfo(100857, 35L, 60000L);
                        Timer.EtcTimer.getInstance().schedule(() -> {
                           if (chr.getSkillCustomValue(100857) != null) {
                              chr.getClient().send(CField.enforceMsgNPC(9062549, 3000, "정말 모두의 속이 다 시원해지는 #r팡팡 리액션#k이었어!"));
                              chr.getClient().send(CField.UIPacket.detailShowInfo("오늘의 리액션 횟수 : " + chr.getKeyValue(100857, "feverCnt") + "/10회", 3, 20, 20));
                              chr.dropMessage(5, "오늘의 리액션 횟수 : " + chr.getKeyValue(100857, "feverCnt") + "/10회");
                              chr.cancelEffect(chr.getBuffedEffect(SecondaryStat.EventSpecialSkill));
                              SkillFactory.getSkill(80003064).getEffect(1).applyTo(chr);
                              chr.removeSkillCustomInfo(100857);
                              Iterator var2 = this.getAllMonster().iterator();

                              while(var2.hasNext()) {
                                 MapleMonster mons = (MapleMonster)var2.next();
                                 if (mons.getId() == 9833965 && mons.getOwner() == chr.getId()) {
                                    this.killMonsterType(mons, 1);
                                 }
                              }
                           }

                        }, 60000L);
                     }, 2800L);

                     for(i = 1; i <= 5; ++i) {
                        Timer.EtcTimer.getInstance().schedule(() -> {
                           int a = 0;
                           Iterator var4 = this.monsterSpawn.iterator();

                           while(var4.hasNext()) {
                              Spawns s = (Spawns)var4.next();
                              MapleMonster mon = MapleLifeFactory.getMonster(9833965);
                              mon.getStats().setLevel(monster.getStats().getLevel());
                              mon.setHp(monster.getStats().getHp() * 2L);
                              mon.getStats().setExp(monster.getStats().getExp() * 2L);
                              mon.setOwner(chr.getId());
                              this.spawnMonsterOnGroundBelow(mon, s.getPosition());
                              ++a;
                              if (a == 7) {
                                 break;
                              }
                           }

                        }, (long)(3500 * i));
                     }
                  }
               }
            }
         }

         MapleMonster mapleMonster1;
         if (monster.getId() == 9833965 && chr.getSkillCustomValue0(100857) > 0L) {
            chr.addSkillCustomInfo(100857, -1L);
            if (chr.getSkillCustomValue0(100857) <= 0L) {
               chr.getClient().send(CField.enforceMsgNPC(9062549, 3000, "정말 모두의 속이 다 시원해지는 #r팡팡 리액션#k이었어!"));
               chr.getClient().send(CField.UIPacket.detailShowInfo("오늘의 리액션 횟수 : " + chr.getKeyValue(100857, "feverCnt") + "/10회", 3, 20, 20));
               chr.dropMessage(5, "오늘의 리액션 횟수 : " + chr.getKeyValue(100857, "feverCnt") + "/10회");

               while(chr.getBuffedValue(80003064)) {
                  chr.cancelEffect(chr.getBuffedEffect(80003064));
               }

               SkillFactory.getSkill(80003064).getEffect(1).applyTo(chr);
               var50 = this.getAllMonster().iterator();

               while(var50.hasNext()) {
                  mapleMonster1 = (MapleMonster)var50.next();
                  if (mapleMonster1.getId() == 9833965 && mapleMonster1.getOwner() == chr.getId()) {
                     this.killMonsterType(mapleMonster1, 1);
                  }
               }

               chr.removeSkillCustomInfo(100857);
               chr.getClient().send(CField.PangPangReactionEnd(5000));
            }
         }

         if (chr.getKeyValue(51351, "startquestid") >= 49001L && chr.getKeyValue(51351, "startquestid") <= 49003L && monster.getStats().getLevel() - 21 <= chr.getLevel() && chr.getLevel() <= monster.getStats().getLevel() + 21 && this.isSpawnPoint() && !GameConstants.isContentsMap(this.getId()) && !GameConstants.보스맵(this.getId()) && !GameConstants.사냥컨텐츠맵(this.getId()) && !GameConstants.튜토리얼(this.getId()) && !GameConstants.로미오줄리엣(this.getId()) && !GameConstants.피라미드(this.getId())) {
            i = chr.getKeyValue(51351, "startquestid") == 49001L ? 100 : (chr.getKeyValue(51351, "startquestid") == 49002L ? 200 : 300);
            String q = "";
            i = Integer.parseInt(chr.getQuest(MapleQuest.getInstance((int)chr.getKeyValue(51351, "startquestid"))).getCustomData());
            ++i;
            if (i < 10) {
               q = "00" + i;
            } else if (i < 100) {
               q = "0" + i;
            } else {
               q = i.makeConcatWithConstants<invokedynamic>(i);
            }

            if (i >= i) {
               if (i < 10) {
                  q = "00" + i;
               } else if (i < 100) {
                  q = "0" + i;
               } else {
                  q = i.makeConcatWithConstants<invokedynamic>(i);
               }
            }

            MapleQuest.getInstance((int)chr.getKeyValue(51351, "startquestid")).forceStart(chr, 0, q);
            if (i >= i) {
               chr.setKeyValue(51351, "queststat", "3");
               chr.getClient().send(CWvsContext.updateSuddenQuest((int)chr.getKeyValue(51351, "midquestid"), false, PacketHelper.getKoreanTimestamp(System.currentTimeMillis()) + 600000000L, "count=1;Quest=" + chr.getKeyValue(51351, "startquestid") + ";state=3;"));
            }
         }

         name = null;
         if (monster.getBuff(2111010) != null) {
            for(maxmob = 0; maxmob < 2; ++maxmob) {
               MapleSummon tosummon = new MapleSummon(chr, 2111010, monster.getTruePosition(), SummonMovementType.WALK_STATIONARY, (byte)0, (int)chr.getBuffLimit(2111010));
               if (chr.getSummons(2111010).size() < 10) {
                  chr.getMap().spawnSummon(tosummon, (int)chr.getBuffLimit(2111010));
                  chr.addSummon(tosummon);
               }
            }
         }

         if (monster.getId() == 8500001 || monster.getId() == 8500002 || monster.getId() == 8500011 || monster.getId() == 8500012 || monster.getId() == 8500021 || monster.getId() == 8500022) {
            maxmob = monster.getId() % 10;
            if (maxmob == 2) {
               this.broadcastMessage(MobPacket.BossPapuLatus.PapulLatusLaser(true, 0));
               this.broadcastMessage(MobPacket.BossPapuLatus.PapulLatusTime(0, false, true));
            }
         }

         if (chr.getV("bossPractice") != null && chr.getV("bossPractice").equals("1")) {
            withDrops = false;
         }

         if (monster.isExtreme() && name != null) {
            String var86 = "익스트림" + name;
         }

         if (this.isElitebossrewardmap() && this.getElitebossrewardtype() == 1 && monster.getId() == 8220027 && Randomizer.isSuccess(chr.isGM() ? 100 : 60)) {
            toDrop = new Item(2432398, (short)0, (short)1, 0);
            this.spawnItemDrop(monster, chr, toDrop, new Point(monster.getTruePosition().x + 25, monster.getTruePosition().y), true, false);
         }

         if (!this.isElitebossmap() && !this.isEliteChmpmap() && !monster.isElitemonster() && !this.isElitebossrewardmap() && !monster.isEliteboss() && !monster.getStats().isBoss() && monster.getStats().getLevel() > 100 && monster.getStats().getLevel() - 21 <= chr.getLevel() && chr.getLevel() <= monster.getStats().getLevel() + 21 && this.isSpawnPoint()) {
            this.setEliteMobCommonCount(this.EliteMobCommonCount + 1);
         } else if (!this.isElitebossmap() && !this.isElitebossrewardmap() && monster.isElitemonster()) {
            if (!monster.isUserunespawn()) {
               this.setEliteCount(this.eliteCount + 1);
            }

            if (chr.getKeyValue(51351, "startquestid") == 49012L || chr.getKeyValue(51351, "startquestid") == 49013L || chr.getKeyValue(51351, "startquestid") == 49014L) {
               maxmob = chr.getKeyValue(51351, "startquestid") == 49012L ? 1 : (chr.getKeyValue(51351, "startquestid") == 49013L ? 2 : 3);
               String q = "";
               Rand = Integer.parseInt(chr.getQuest(MapleQuest.getInstance((int)chr.getKeyValue(51351, "startquestid"))).getCustomData());
               ++Rand;
               if (Rand < 10) {
                  q = "00" + Rand;
               } else if (Rand < 100) {
                  q = "0" + Rand;
               } else {
                  q = Rand.makeConcatWithConstants<invokedynamic>(Rand);
               }

               if (Rand >= maxmob) {
                  if (maxmob < 10) {
                     q = "00" + maxmob;
                  } else if (maxmob < 100) {
                     q = "0" + maxmob;
                  } else {
                     q = maxmob.makeConcatWithConstants<invokedynamic>(maxmob);
                  }
               }

               MapleQuest.getInstance((int)chr.getKeyValue(51351, "startquestid")).forceStart(chr, 0, q);
               if (Rand >= maxmob) {
                  chr.setKeyValue(51351, "queststat", "3");
                  chr.getClient().send(CWvsContext.updateSuddenQuest((int)chr.getKeyValue(51351, "midquestid"), false, PacketHelper.getKoreanTimestamp(System.currentTimeMillis()) + 600000000L, "count=1;Quest=" + chr.getKeyValue(51351, "startquestid") + ";state=3;"));
               }
            }

            if (this.getEliteCount() < 5) {
               this.broadcastMessage(CField.startMapEffect("어두운 기운이 사라지지 않아 이곳을 음산하게 만들고 있습니다.", 5120124, true));
            } else if (this.getEliteCount() < 10) {
               this.broadcastMessage(CField.startMapEffect("이곳이 어두운 기운으로 가득차 곧 무슨 일이 일어날 듯 합니다.", 5120124, true));
            }
         }

         if (monster.getId() == 8800002) {
            World.Broadcast.broadcastMessage(CField.getGameMessage(4, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 노말 자쿰을 격파하였습니다!"));
         }

         if (monster.getId() == 8860000) {
            World.Broadcast.broadcastMessage(CField.getGameMessage(8, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 노말 아카이럼을 격파하였습니다!"));
         }

         if (monster.getId() == 8880000) {
            World.Broadcast.broadcastMessage(CField.getGameMessage(8, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 하드 매그너스를 격파하였습니다!"));
         }

         if (monster.getId() == 8500022) {
            World.Broadcast.broadcastMessage(CField.getGameMessage(8, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 카오스 파풀라투스를 격파하였습니다!"));
         }

         if (monster.getId() == 8950102) {
            World.Broadcast.broadcastMessage(CField.getGameMessage(8, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 노말 스우를 격파하였습니다!"));
         }

         if (monster.getId() == 8950002) {
            World.Broadcast.broadcastMessage(CField.getGameMessage(8, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 하드 스우를 격파하였습니다!"));
         }

         if (monster.getId() == 8880111) {
            World.Broadcast.broadcastMessage(CField.getGameMessage(8, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 노말 데미안을 격파하였습니다!"));
         }

         if (monster.getId() == 8880101) {
            World.Broadcast.broadcastMessage(CField.getGameMessage(8, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 하드 데미안을 격파하였습니다!"));
         }

         if (monster.getId() == 8880151) {
            World.Broadcast.broadcastMessage(CField.getGameMessage(8, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 노말 루시드를 격파하였습니다!"));
         }

         if (monster.getId() == 8880153) {
            World.Broadcast.broadcastMessage(CField.getGameMessage(8, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 하드 루시드를 격파하였습니다!"));
         }

         if (monster.getId() == 8880302) {
            World.Broadcast.broadcastMessage(CField.getGameMessage(8, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 하드 윌을 격파하였습니다!"));
         }

         if (monster.getId() == 8644655) {
            if (monster.isHellMode()) {
               World.Broadcast.broadcastMessage(CField.getGameMessage(8, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 헬 더스크를 격파하였습니다."));
            } else if (monster.isExtreme()) {
               World.Broadcast.broadcastMessage(CField.getGameMessage(8, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 익스트림 더스크를 격파하였습니다."));
            } else {
               World.Broadcast.broadcastMessage(CField.getGameMessage(8, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 카오스 더스크를 격파하였습니다."));
            }
         }

         if (monster.getId() == 8645066) {
            if (monster.isHellMode()) {
               World.Broadcast.broadcastMessage(CField.getGameMessage(8, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 헬 듄켈을 격파하였습니다."));
            } else if (monster.isExtreme()) {
               World.Broadcast.broadcastMessage(CField.getGameMessage(8, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 익스트림 듄켈을 격파하였습니다."));
            } else {
               World.Broadcast.broadcastMessage(CField.getGameMessage(8, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 친위대장 듄켈을 격파하였습니다."));
            }
         }

         if (monster.getId() == 8880405) {
            if (monster.isHellMode()) {
               World.Broadcast.broadcastMessage(CField.getGameMessage(8, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 헬 진힐라를 격파하였습니다."));
            } else if (monster.isExtreme()) {
               World.Broadcast.broadcastMessage(CField.getGameMessage(8, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 익스트림 진힐라를 격파하였습니다."));
            } else {
               World.Broadcast.broadcastMessage(CField.getGameMessage(8, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 고통의 미궁 : 진힐라를 격파하였습니다."));
            }
         }

         if (monster.getId() == 8880504) {
            if (monster.isHellMode()) {
               World.Broadcast.broadcastMessage(CField.getGameMessage(8, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 헬 검은 마법사를 격파하였습니다."));
            } else if (monster.isExtreme()) {
               World.Broadcast.broadcastMessage(CField.getGameMessage(8, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 익스트림 검은 마법사를 격파하였습니다."));
            } else {
               World.Broadcast.broadcastMessage(CField.getGameMessage(8, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 검은 마법사를 격파하였습니다."));
            }
         }

         if (monster.getId() == 8880602) {
            if (monster.isHellMode()) {
               World.Broadcast.broadcastMessage(CField.getGameMessage(8, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 헬 세렌을 격파하였습니다."));
            } else if (monster.isExtreme()) {
               World.Broadcast.broadcastMessage(CField.getGameMessage(8, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 익스트림 세렌을 격파하였습니다."));
            } else {
               World.Broadcast.broadcastMessage(CField.getGameMessage(8, "[" + chr.getParty().getLeader().getName() + "]님의 파티가 선택받은 세렌을 격파하였습니다."));
            }
         }

         if (this.getCustomValue(8222222) == null && this.EliteMobCommonCount >= 700) {
            this.EliteMobCommonCount = 0;
            if (this.getEliteCount() == 5 && monster.getEliteGrade() <= -1 && this.isSpawnPoint() && !this.isEliteChmpmap() && !this.isElitebossmap() && !this.isElitebossrewardmap()) {
               type = Randomizer.rand(0, 4);
               int j = false;
               String txt = "";
               this.setEliteChmpmap(true);
               this.setElitechmptype(type);
               this.setCustomInfo(8222222, 0, 600000);
               MapleMonster mapleMonster;
               int m;
               switch(type) {
               case 0:
                  j = 8220100;
                  txt = "어둠 속에서 점차 성장하는 블랙 크레센도 슬라임이 나타납니다.";
                  mapleMonster1 = MapleLifeFactory.getMonster(j);
                  this.setElitechmpfinal(true);
                  mapleMonster1.setEliteGrade(1);
                  mapleMonster1.setEliteType(4);
                  mapleMonster1.getStats().setLevel(chr.getLevel());
                  mapleMonster1.setHp((long)((double)(monster.getStats().getHp() * 25L) * mapleMonster1.bonusHp()));
                  mapleMonster1.setElitehp(mapleMonster1.getHp());
                  this.setElitechmpcount(3);
                  this.spawnMonsterOnGroundBelow(mapleMonster1, monster.getTruePosition());
                  break;
               case 1:
                  for(i = 0; i < 20; ++i) {
                     m = Randomizer.rand(0, this.monsterSpawn.size() - 1);
                     j = Randomizer.rand(8220106, 8220108);
                     mapleMonster = MapleLifeFactory.getMonster(j);
                     mapleMonster.setEliteGrade(1);
                     mapleMonster.setEliteType(4);
                     mapleMonster.getStats().setLevel(chr.getLevel());
                     mapleMonster.setHp((long)((double)(monster.getStats().getHp() * 10L) * mapleMonster.bonusHp()));
                     mapleMonster.setElitehp(mapleMonster.getHp());
                     this.spawnMonsterOnGroundBelow(mapleMonster, ((Spawns)this.monsterSpawn.get(m)).getPosition());
                  }

                  this.setElitechmpfinal(true);
                  this.setElitechmpcount(20);
                  txt = "어둠 속에서 그림자 나비 떼들이 몰려옵니다.";
                  break;
               case 2:
                  for(i = 0; i < 5; ++i) {
                     m = Randomizer.rand(0, this.monsterSpawn.size() - 1);
                     j = 8220122;
                     mapleMonster = MapleLifeFactory.getMonster(j);
                     mapleMonster.setEliteGrade(1);
                     mapleMonster.setEliteType(4);
                     mapleMonster.getStats().setLevel(chr.getLevel());
                     mapleMonster.setHp((long)((double)(monster.getStats().getHp() * 30L) * mapleMonster.bonusHp()));
                     mapleMonster.setElitehp(mapleMonster.getHp());
                     this.spawnMonsterOnGroundBelow(mapleMonster, ((Spawns)this.monsterSpawn.get(m)).getPosition());
                  }

                  this.setElitechmpfinal(false);
                  this.setElitechmpcount(5);
                  txt = "어둠의 기운을 간직한 구체가 생겨났습니다. 모두 격파하고 숨어있는 어둠 늑대를 처치하세요.";
                  break;
               case 3:
                  Rand = Randomizer.rand(0, this.monsterSpawn.size() - 1);
                  Flower = MapleLifeFactory.getMonster(8220110);
                  Flower.setEliteGrade(1);
                  Flower.setEliteType(4);
                  Flower.setHp(15L);
                  Flower.getStats().setLevel(chr.getLevel());
                  this.spawnMonsterOnGroundBelow(Flower, ((Spawns)this.monsterSpawn.get(Rand)).getPosition());

                  for(k = 0; k < 15; ++k) {
                     j = 8220111;
                     Rand = Randomizer.rand(0, this.monsterSpawn.size() - 1);
                     mapleMonster = MapleLifeFactory.getMonster(j);
                     mapleMonster.setEliteGrade(1);
                     mapleMonster.setEliteType(4);
                     mapleMonster.getStats().setLevel(chr.getLevel());
                     mapleMonster.setHp((long)((double)(monster.getStats().getHp() * 20L) * mapleMonster.bonusHp()));
                     mapleMonster.setElitehp(mapleMonster.getHp());
                     this.spawnMonsterOnGroundBelow(mapleMonster, ((Spawns)this.monsterSpawn.get(Rand)).getPosition());
                  }

                  this.setElitechmpfinal(true);
                  this.setElitechmpcount(15);
                  txt = "환상의 꽃이 피어났습니다. 꽃을 공격하는 킬러 비를 모두 처치하세요.";
                  break;
               case 4:
                  j = 8220125;
                  EliteChmp = MapleLifeFactory.getMonster(j);
                  EliteChmp.setEliteGrade(1);
                  EliteChmp.setEliteType(4);
                  EliteChmp.getStats().setLevel(chr.getLevel());
                  EliteChmp.setHp((long)((double)(monster.getStats().getHp() * 50L) * EliteChmp.bonusHp()));
                  EliteChmp.setElitehp(EliteChmp.getHp());
                  this.spawnMonsterOnGroundBelow(EliteChmp, monster.getTruePosition());
                  this.setElitechmpcount(1);
                  this.setElitechmpfinal(false);
                  txt = "어둠 속에서 나타난 다크 가고일을 퇴치하고 분리되는 미니 다크 가고일도 모두 처치하세요. ";
               }

               this.broadcastMessage(CField.startMapEffect(txt, 5120124, true));
               Timer.MapTimer.getInstance().schedule(() -> {
                  if (this.isEliteChmpmap()) {
                     String failtxt = "";
                     if (this.getElitechmptype() == 0) {
                        failtxt = "블랙 크레센도 슬라임 처치에 실패하였습니다. 슬라임이 어둠의 그림자에 스며듭니다.";
                     } else if (this.getElitechmptype() == 1) {
                        failtxt = "그림자 나비 처치에 실패하였습니다. 나비들이 어둠의 그림자에 스며듭니다.";
                     } else if (this.getElitechmptype() == 2) {
                        failtxt = "어둠 늑대 처치에 실패하였습니다. 늑대가 어둠의 그림자에 스며듭니다.";
                     } else if (this.getElitechmptype() == 3) {
                        failtxt = "환상의 꽃 보호에 실패하였습니다. 킬러 비가 환상의 꽃의 기운을 빼앗고 사라집니다.";
                     } else if (this.getElitechmptype() == 4) {
                        failtxt = "다크 가고일 처치에 실패하였습니다. 가고일들이 어둠의 그림자에 스며듭니다";
                     }

                     Iterator var3 = this.getAllMonster().iterator();

                     while(true) {
                        while(var3.hasNext()) {
                           MapleMonster mob = (MapleMonster)var3.next();
                           if (mob.getId() != 8220123 && mob.getId() != 8220111 && mob.getId() != 8220122 && mob.getId() != 8220124 && mob.getId() != 8220125 && (mob.getId() < 8220100 || mob.getId() > 8220108)) {
                              if (mob.getId() == 8220110) {
                                 mob.setHp(0L);
                                 if (this.RealSpawns.contains(monster)) {
                                    this.RealSpawns.remove(monster);
                                 }

                                 this.broadcastMessage(MobPacket.killMonster(mob.getObjectId(), 80, 1));
                                 this.removeMapObject(mob);
                                 mob.killed();
                                 this.spawnedMonstersOnMap.decrementAndGet();
                              }
                           } else {
                              this.killMonster(mob.getId());
                           }
                        }

                        this.broadcastMessage(CField.startMapEffect(failtxt, 5120124, true));
                        this.setEliteChmpmap(false);
                        this.setElitechmpcount(0);
                        this.setElitechmptype(0);
                        break;
                     }
                  }

               }, 60000L);
            } else if (this.getEliteCount() < 20 && monster.getStats().getLevel() >= 100 && this.isSpawnPoint() && !this.isElitebossmap() && !this.isElitebossrewardmap() && !this.isEliteChmpmap() && !monster.isElitemonster() && monster.getEliteGrade() < 0) {
               alreadyspawn = false;
               var85 = chr.getMap().getAllMonster().iterator();

               label1521: {
                  do {
                     if (!var85.hasNext()) {
                        break label1521;
                     }

                     mob = (MapleMonster)var85.next();
                  } while(!mob.isElitemonster() && !mob.isEliteboss());

                  alreadyspawn = true;
               }

               if (!alreadyspawn) {
               }
            }
         }
      } catch (Exception var24) {
         FileoutputUtil.log("Log_Kill.txt", var24.makeConcatWithConstants<invokedynamic>(var24));
      }

      if (monster.isEliteboss()) {
         this.stopEliteBossMap();
         this.setElitebossrewardmap(true);
         this.startEliteBossReward();
      }

      map = false;
      int nextmob = false;
      name = null;
      if (monster.getId() == 8910001) {
         MapleMap warp = ChannelServer.getInstance(chr.getClient().getChannel()).getMapFactory().getMap(chr.getMapId() - 10);
         this.broadcastMessage(CWvsContext.getTopMsg("시공간 붕괴 실패! 잠시 후, 원래 세계로 돌아갑니다."));
         if (chr.getParty() != null) {
            var85 = chr.getParty().getMembers().iterator();

            while(var85.hasNext()) {
               MaplePartyCharacter chrs = (MaplePartyCharacter)var85.next();
               chrs.getPlayer().getClient().send(CField.VonVonStopWatch(0));
               chrs.getPlayer().removeSkillCustomInfo(8910000);
            }
         }

         Timer.MapTimer.getInstance().schedule(new Runnable() {
            public void run() {
               Iterator var1 = MapleMap.this.getAllCharactersThreadsafe().iterator();

               while(var1.hasNext()) {
                  MapleCharacter chr = (MapleCharacter)var1.next();
                  MapleMap zz = ChannelServer.getInstance(chr.getClient().getChannel()).getMapFactory().getMap(chr.getMapId() - 10);
                  chr.changeMap(zz, zz.getPortal(0));
               }

            }
         }, 2000L);
      }

      if (monster.getId() / 1000 == 9800 && monster.getMobExp() == 0L) {
         DecimalFormat formatter = new DecimalFormat("###,###");
         chr.setMparkkillcount(chr.getMparkkillcount() + 1);
         i = chr.getMparkexp() / chr.getMparkcount() * chr.getMparkkillcount();
         chr.getClient().getSession().writeAndFlush(CField.removeMapEffect());
         chr.getClient().getSession().writeAndFlush(CField.startMapEffect("경험치 보상 " + formatter.format((long)i) + " 누적!", 5120162, true));
      }

      alreadyspawn = false;
      boolean killall = false;
      KoreaCalendar kc = new KoreaCalendar();
      int var113 = kc.getYeal() % 100;
      q = var113 + "/" + kc.getMonths() + "/" + kc.getDays();
      Iterator var77;
      MaplePartyCharacter pc;
      Iterator var109;
      if (monster.getId() != 8820212 && monster.getId() != 8880302 && monster.getId() != 8880342 && monster.getId() != 8880150 && monster.getId() != 8880151 && monster.getId() != 8880155 && monster.getId() != 8644650 && monster.getId() != 8644655 && monster.getId() != 8880405 && monster.getId() != 8645009 && monster.getId() != 8645039 && monster.getId() != 8880504 && monster.getId() != 8880602 && monster.getId() != 8880614 && monster.getId() != 8820101 && monster.getId() != 8820001 && monster.getId() != 8860005 && monster.getId() != 8860000 && monster.getId() != 8810018 && monster.getId() != 8810122 && monster.getId() != 8840007 && monster.getId() != 8840000 && monster.getId() != 8840014 && monster.getId() != 8800102 && monster.getId() != 8500012 && monster.getId() != 8500002 && monster.getId() != 8880200 && monster.getId() != 8870000 && monster.getId() != 8800002 && monster.getId() != 8500022 && monster.getId() != 8870100 && monster.getId() != 8910100 && monster.getId() != 8930100 && monster.getId() != 8910000 && monster.getId() != 8930000 && monster.getId() != 8850011 && monster.getId() != 8850111 && monster.getId() != 8950102 && monster.getId() != 8950002 && monster.getId() != 8880111 && monster.getId() != 8880101 && (monster.getId() < 8900100 || monster.getId() > 8900102) && (monster.getId() < 8900000 || monster.getId() > 8900002) && (monster.getId() < 8920000 || monster.getId() > 8920003) && (monster.getId() < 8920100 || monster.getId() > 8920103) && monster.getId() != 8920106 && monster.getId() != 8900103) {
         if (monster.getId() == 8880010 || monster.getId() == 8880002 || monster.getId() == 8880000) {
            alreadyspawn = true;
            this.broadcastMessage(CWvsContext.getTopMsg("매그너스가 사망하여 더이상 구와르의 힘이 방출되지 않습니다."));
            if (chr.getParty() != null) {
               var109 = chr.getParty().getMembers().iterator();

               while(var109.hasNext()) {
                  MaplePartyCharacter chrs = (MaplePartyCharacter)var109.next();
                  chrs.getPlayer().updateInfoQuest(monster.getId() == 8880002 ? 3993 : 3992, "count=1;lasttime=" + q);
               }
            }
         }
      } else {
         alreadyspawn = true;
         killall = true;
         if (chr.getParty() != null && !monster.isExtreme() && !monster.isHellMode()) {
            int quest = 0;
            switch(monster.getId()) {
            case 8500002:
            case 8500012:
               quest = 3655;
               break;
            case 8500022:
               quest = 3657;
               break;
            case 8644650:
            case 8644655:
               quest = 3680;
               break;
            case 8645009:
            case 8645039:
               quest = 3681;
               break;
            case 8800002:
               quest = 3654;
               break;
            case 8800102:
               quest = 15166;
               break;
            case 8810018:
            case 8810122:
               quest = 3789;
               break;
            case 8820001:
               quest = 3652;
               break;
            case 8820101:
               quest = 3653;
               break;
            case 8820212:
               quest = 3653;
               break;
            case 8840000:
            case 8840007:
            case 8840014:
               quest = 3793;
               break;
            case 8850011:
            case 8850111:
               quest = 31199;
               break;
            case 8860000:
            case 8860005:
               quest = 3792;
               break;
            case 8870000:
               quest = 3649;
               break;
            case 8870100:
               quest = 3650;
               break;
            case 8880101:
            case 8880111:
               quest = '蓡';
               break;
            case 8880150:
            case 8880151:
            case 8880155:
               quest = 3685;
               break;
            case 8880200:
               quest = 3591;
               break;
            case 8880302:
            case 8880342:
               quest = 3658;
               break;
            case 8880405:
               quest = 3673;
               break;
            case 8880504:
               quest = 3679;
               break;
            case 8880614:
               quest = 3687;
               break;
            case 8900000:
            case 8900001:
            case 8900002:
               quest = 30043;
               break;
            case 8900103:
               quest = 30032;
               break;
            case 8910000:
               quest = 30044;
               break;
            case 8910100:
               quest = 30039;
               break;
            case 8920000:
            case 8920001:
            case 8920002:
            case 8920003:
               quest = 30045;
               break;
            case 8920106:
               quest = 30033;
               break;
            case 8930000:
               quest = 30046;
               break;
            case 8930100:
               quest = 30041;
               break;
            case 8950002:
            case 8950102:
               quest = '舗';
            }

            this.broadcastMessage(SLFCGPacket.ClearObstacles());
            var77 = chr.getParty().getMembers().iterator();

            while(var77.hasNext()) {
               pc = (MaplePartyCharacter)var77.next();
               if (pc.getPlayer() != null && chr.getMapId() == pc.getPlayer().getMapId() && pc.isOnline()) {
                  String var114 = FileoutputUtil.보스클리어;
                  int var10001 = pc.getPlayer().getClient().getAccID();
                  FileoutputUtil.log(var114, "[클리어] 계정 번호 : " + var10001 + " | 파티번호 : " + pc.getPlayer().getParty().getId() + " | 캐릭터 : " + pc.getPlayer().getName() + "(" + pc.getPlayer().getId() + ") | 클리어 보스 : " + MapleLifeFactory.getMonster(monster.getId()).getStats().getName() + "(" + monster.getId() + ")");
                  if (quest != 0) {
                     if (quest == '蓡') {
                        pc.getPlayer().Stigma = 0;
                        Map<SecondaryStat, Pair<Integer, Integer>> dds = new HashMap();
                        dds.put(SecondaryStat.Stigma, new Pair(pc.getPlayer().Stigma, 0));
                        pc.getPlayer().getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.cancelBuff(dds, pc.getPlayer()));
                        pc.getPlayer().getMap().broadcastMessage(pc.getPlayer(), CWvsContext.BuffPacket.cancelForeignBuff(pc.getPlayer(), dds), false);
                     }

                     if (pc.getPlayer().getClient().getQuestStatus(50007) == 1) {
                        if (quest == 30033) {
                           pc.getPlayer().getClient().setCustomKeyValue(50007, "m1", "1");
                        } else if (quest == 30032) {
                           pc.getPlayer().getClient().setCustomKeyValue(50007, "m2", "1");
                        } else if (quest == 30039) {
                           pc.getPlayer().getClient().setCustomKeyValue(50007, "m3", "1");
                        } else if (quest == 30041) {
                           pc.getPlayer().getClient().setCustomKeyValue(50007, "m4", "1");
                        }
                     }

                     pc.getPlayer().removeSkillCustomInfo(143143);
                     if (withDrops) {
                        pc.getPlayer().updateInfoQuest(quest, "count=1;lasttime=" + q);
                     }
                  }
               }
            }
         }
      }

      if (monster.getId() == 8880602 || monster.getId() == 8880140 || monster.getId() == 8880141 || monster.getId() == 8880142) {
         alreadyspawn = true;
         this.killAllMonsters(true);
      }

      if (monster.getId() == 8850111 || monster.getId() == 8850011) {
         this.broadcastMessage(CWvsContext.getTopMsg("시그너스를 퇴치하셨습니다. 좌측의 출구를 통해 퇴장하실 수 있습니다."));
      }

      if (monster.getId() == 8930000) {
         animation = true;
         this.killMonster(8930001);
      }

      if (monster.getId() == 8880200) {
         this.killMonster(8880201);
         this.killMonster(8880202);
      }

      if (monster.getId() == 8880300 || monster.getId() == 8880303 || monster.getId() == 8880304 || monster.getId() == 8880340 || monster.getId() == 8880343 || monster.getId() == 8880344) {
         this.broadcastMessage(CField.enforceMSG("윌이 진지해졌네요. 거울 속 깊은 곳에 윌의 진심이 비춰질 것 같아요.", 245, 7000));
      }

      if (monster.getId() == 8880301 || monster.getId() == 8880341) {
         this.broadcastMessage(CField.enforceMSG("윌이 여유가 없어졌군요. 거울 세계의 가장 깊은 곳이 드러날 것 같아요.", 245, 7000));
      }

      if (monster.getId() == 8644650 || monster.getId() == 8644655) {
         this.broadcastMessage(CField.getDestoryedBackImg("die", "dead"));
      }

      if (monster.getId() == 8880342 || monster.getId() == 8880302) {
         var109 = this.getAllSpiderWeb().iterator();

         while(var109.hasNext()) {
            SpiderWeb web = (SpiderWeb)var109.next();
            this.broadcastMessage(MobPacket.BossWill.willSpider(false, web));
            this.removeMapObject(web);
         }

         var109 = this.getAllMonster().iterator();

         while(var109.hasNext()) {
            m = (MapleMonster)var109.next();
            m.killed();
         }
      }

      if (monster.getId() == 8880505) {
         this.killAllMonsters(true);
         this.broadcastMessage(CField.enforceMSG("창조와 파괴의 기사가 쓰러져 검은 마법사에게로 가는 길이 열린다.", 265, 3000));
      }

      if (monster.getId() == 8880502) {
         this.killAllMonsters(true);
         this.broadcastMessage(CField.enforceMSG("검은 마법사로부터 알 수 없는 기운이 뿜어져 나와 어둠의 왕좌를 삼킨다.", 265, 3000));
      }

      if (monster.getId() == 8880503) {
         this.killAllMonsters(false);
         this.broadcastMessage(CField.enforceMSG("압도적인 기운에 의해 주변의 모든 것이 순식간에 소멸해간다.", 265, 3000));
      }

      if (monster.getId() == 8880504) {
         this.killAllMonsters(false);
         this.broadcastMessage(SLFCGPacket.MakeBlind(1, 255, 240, 240, 240, 1000, 0));
      }

      if (monster.getId() == 8860002 || monster.getId() == 8860006) {
         toDrop = new Item(2002058, (short)0, (short)1, 0);
         this.spawnItemDrop(monster, chr, toDrop, new Point(monster.getTruePosition().x, monster.getTruePosition().y), true, false);
      }

      if (monster.getId() >= 8920000 && monster.getId() <= 8920003 && withDrops) {
         this.RewardCheck(8920000, 8920003, 8920006, new Point(34, 135));
         alreadyspawn = true;
      } else if (monster.getId() >= 8900000 && monster.getId() <= 8900002 && withDrops) {
         this.RewardCheck(8900000, 8900002, 8900003, new Point(570, 551));
         alreadyspawn = true;
      } else if (monster.getId() >= 8920100 && monster.getId() <= 8920103 && withDrops) {
         this.RewardCheck(8920100, 8920103, 8920106, new Point(34, 135));
         alreadyspawn = true;
      } else if (monster.getId() >= 8900100 && monster.getId() <= 8900102 && withDrops) {
         this.RewardCheck(8900100, 8900102, 8900103, new Point(570, 551));
         alreadyspawn = true;
      } else if (monster.getId() == 8810122) {
         Flower = MapleLifeFactory.getMonster(8890000);
         this.spawnMonsterOnGroundBelow(Flower, new Point(106, 260));
         this.killMonsterDealy(Flower);
         Flower = MapleLifeFactory.getMonster(8890002);
         this.spawnMonsterOnGroundBelow(Flower, new Point(209, 260));
         this.killMonsterDealy(Flower);
         alreadyspawn = true;
      } else if (monster.getId() == 8820101) {
         size = (int)monster.getCustomValue0(8820101) - 1;
         if (size > 0) {
            withDrops = false;
            m = MapleLifeFactory.getMonster(8820101);
            m.setCustomInfo(8820101, (int)monster.getCustomValue0(8820101) - 1, 0);
            m.setHp(size == 2 ? 23100000000L : (size == 1 ? 33600000000L : 0L));
            m.getStats().setHp(size == 2 ? 23100000000L : (size == 1 ? 33600000000L : 0L));
            m.setPosition(monster.getPosition());
            this.spawnMonsterDelay(m, -2, 2500);
         } else {
            alreadyspawn = true;
            withDrops = true;
         }
      } else if (this.getId() != 270050100 && this.getId() != 270051100) {
         if (monster.getId() != 8820002 && monster.getId() != 8860005) {
            if (monster.getId() == 8880111 || monster.getId() == 8880101) {
               alreadyspawn = true;
            }
         } else {
            alreadyspawn = true;
         }
      } else {
         size = 0;
         var77 = this.getAllMonster().iterator();

         while(var77.hasNext()) {
            m = (MapleMonster)var77.next();
            if (m.getId() != 8820014 && m.getId() != 8820000 && m.getId() != 8820009 && m.getId() != 8820114 && m.getId() != 8820100 && m.getId() != 8820109) {
               ++size;
            }
         }

         if (size == 0) {
            var77 = this.getAllMonster().iterator();

            label1322:
            while(true) {
               do {
                  if (!var77.hasNext()) {
                     var77 = this.getAllMonster().iterator();

                     while(true) {
                        do {
                           if (!var77.hasNext()) {
                              break label1322;
                           }

                           m = (MapleMonster)var77.next();
                        } while(m.getId() != 8820008 && m.getId() != 8820000 && m.getId() != 8820014 && m.getId() != 8820108 && m.getId() != 8820100 && m.getId() != 8820114);

                        this.killMonster(m, chr, withDrops, second, (byte)1);
                     }
                  }

                  m = (MapleMonster)var77.next();
               } while(m.getId() != 8820008 && m.getId() != 8820000 && m.getId() != 8820014 && m.getId() != 8820108 && m.getId() != 8820100 && m.getId() != 8820114);

               this.killMonster(m, chr, withDrops, second, (byte)1);
            }
         }
      }

      if (alreadyspawn) {
         var109 = this.getAllChracater().iterator();

         while(var109.hasNext()) {
            MapleCharacter chr2 = (MapleCharacter)var109.next();
            chr2.dispelDebuffs();
         }
      }

      if (monster.getId() == 9390612 || monster.getId() == 9390610 || monster.getId() == 9390611 || monster.getId() == 8645066) {
         MapleCharacter pchr = chr.getClient().getChannelServer().getPlayerStorage().getCharacterById(chr.getParty().getLeader().getId());
         if (pchr == null) {
            chr.dropMessage(5, "파티의 상태가 변경되어 골럭스 원정대가 해체됩니다.");
            chr.setKeyValue(200106, "golrux_in", "0");
         } else if (pchr.getKeyValue(200106, "golrux_in") == 1L && pchr.getKeyValue(200106, "golrux_enter") == 1L) {
            if (pchr.getId() != chr.getId()) {
               chr.setKeyValue(200106, "golrux_clear", "1");
               pchr.setKeyValue(200106, "golrux_clear", "1");
            } else {
               chr.setKeyValue(200106, "golrux_clear", "1");
            }

            chr.dropMessage(5, "골럭스의 일부분을 처치하셨습니다.");
         }
      }

      int[] linkMobs = new int[]{9010152, 9010153, 9010154, 9010155, 9010156, 9010157, 9010158, 9010159, 9010160, 9010161, 9010162, 9010163, 9010164, 9010165, 9010166, 9010167, 9010168, 9010169, 9010170, 9010171, 9010172, 9010173, 9010174, 9010175, 9010176, 9010177, 9010178, 9010179, 9010180, 9010181};
      int[] var90 = linkMobs;
      type = linkMobs.length;

      label1261:
      for(j = 0; j < type; ++j) {
         int linkMob = var90[j];
         if (monster.getId() == linkMob) {
            if (chr.getLinkMobCount() <= 0) {
               Iterator var91 = this.getAllMonstersThreadsafe().iterator();

               while(true) {
                  if (!var91.hasNext()) {
                     break label1261;
                  }

                  MapleMapObject monstermo = (MapleMapObject)var91.next();
                  MapleMonster mob = (MapleMonster)monstermo;
                  if (mob.getOwner() == chr.getId()) {
                     mob.setHp(0L);
                     this.broadcastMessage(MobPacket.killMonster(mob.getObjectId(), 1));
                     this.removeMapObject(mob);
                     mob.killed();
                  }
               }
            } else {
               mapleMonster = MapleLifeFactory.getMonster(monster.getId());
               mapleMonster.setOwner(chr.getId());
               this.spawnMonsterOnGroundBelow(mapleMonster, monster.getTruePosition());
               break;
            }
         }
      }

      if (chr.getMap().getId() / 10000 == 92507 && this.getAllMonster().size() <= 0) {
         k = (chr.getMapId() - 925070000) / 100;
         chr.removeSkillCustomInfo(92507);
         MapleReactor reactor2l;
         if (k >= 31 && k <= 39) {
            chr.setSkillCustomInfo(92507000, chr.getSkillCustomValue0(92507000) + 1L, 0L);
            if (chr.getSkillCustomValue0(92507000) >= (k != 37 && k != 38 && k != 39 ? 2L : 3L)) {
               chr.removeSkillCustomInfo(92507000);
               var75 = chr.getMap().getAllReactorsThreadsafe().iterator();

               while(var75.hasNext()) {
                  reactor1l = (MapleMapObject)var75.next();
                  reactor2l = (MapleReactor)reactor1l;
                  if (reactor2l.getReactorId() == 2508000 && reactor2l.getState() == 0) {
                     reactor2l.forceHitReactor((byte)1, chr.getId());
                     break;
                  }
               }

               chr.setDojoStop(true);
               chr.removeSkillCustomInfo(92507);
               chr.setKeyValue(3, "dojo_time", String.valueOf(chr.getDojoStartTime()));
               chr.getClient().getSession().writeAndFlush(CWvsContext.getTopMsg("상대를 격파하였습니다. 10초간 타이머가 정지됩니다."));
               chr.getClient().getSession().writeAndFlush(CWvsContext.getTopMsg("포탈을 통해 다음 스테이지로 이동해야 클리어 기록이 랭킹에 등록 됩니다."));
               chr.getClient().getSession().writeAndFlush(CField.getDojoClockStop(true, 900 - chr.getDojoStartTime()));
               chr.getClient().getSession().writeAndFlush(CField.environmentChange("Dojang/cleard", 5));
               chr.getClient().getSession().writeAndFlush(CField.environmentChange("dojang/end/clear", 19));
               chr.getStat().heal(chr);
               chr.MulungTimer = new java.util.Timer();
               chr.MulungTimerTask = new TimerTask() {
                  public void run() {
                     chr.setDojoStop(false);
                     if (chr.getMap().getId() / 10000 == 92507) {
                        chr.getClient().getSession().writeAndFlush(CField.getDojoClockStop(false, 900 - chr.getDojoStartTime()));
                     }

                     chr.MulungTimer = null;
                     chr.cancelTimer();
                  }
               };
               chr.MulungTimer.schedule(chr.MulungTimerTask, 10000L);
            } else {
               Timer.EtcTimer.getInstance().schedule(() -> {
                  MapleMonster mob2 = MapleLifeFactory.getMonster(monster.getId());
                  mob2.setHp(monster.getMobMaxHp());
                  mob2.getStats().setHp(monster.getMobMaxHp());
                  chr.getMap().spawnMonsterWithEffectBelow(mob2, monster.getPosition(), -1);
               }, 1500L);
            }
         } else {
            var75 = chr.getMap().getAllReactorsThreadsafe().iterator();

            while(var75.hasNext()) {
               reactor1l = (MapleMapObject)var75.next();
               reactor2l = (MapleReactor)reactor1l;
               if (reactor2l.getReactorId() == 2508000 && reactor2l.getState() == 0) {
                  reactor2l.forceHitReactor((byte)1, chr.getId());
                  break;
               }
            }

            if (k == 1 || k == 13 || k == 34 || k == 35 || k == 38 || k == 39 || k == 43 || k == 52 || k == 54) {
               type = chr.getMapId();
               chr.getClient().send(SLFCGPacket.OnBomb(176, 39, monster.getPosition()));
               Timer.EtcTimer.getInstance().schedule(() -> {
                  if (monster.getPosition().x + 150 > chr.getPosition().x && monster.getPosition().x - 150 < chr.getPosition().x && type == chr.getMapId()) {
                     chr.addHP(-(chr.getStat().getCurrentMaxHp() / 100L) * 10L);
                     chr.getClient().send(CField.DamagePlayer2((int)(chr.getStat().getCurrentMaxHp() / 100L) * 10));
                     chr.getClient().send(CField.EffectPacket.showEffect(chr, 0, 176, 45, 39, 0, (byte)(chr.isFacingLeft() ? 1 : 0), true, chr.getTruePosition(), (String)null, (Item)null));
                  }

               }, 2300L);
            }

            chr.setDojoStop(true);
            chr.setKeyValue(3, "dojo_time", String.valueOf(chr.getDojoStartTime()));
            chr.getClient().getSession().writeAndFlush(CWvsContext.getTopMsg("포탈을 통해 다음 스테이지로 이동해야 클리어 기록이 랭킹에 등록 됩니다."));
            chr.getClient().getSession().writeAndFlush(CWvsContext.getTopMsg("상대를 격파하였습니다. 10초간 타이머가 정지됩니다."));
            chr.getClient().getSession().writeAndFlush(CField.getDojoClockStop(true, 900 - chr.getDojoStartTime()));
            chr.getClient().getSession().writeAndFlush(CField.environmentChange("Dojang/cleard", 5));
            chr.getClient().getSession().writeAndFlush(CField.environmentChange("dojang/end/clear", 19));
            chr.getStat().heal(chr);
            chr.MulungTimer = new java.util.Timer();
            chr.MulungTimerTask = new TimerTask() {
               public void run() {
                  chr.setDojoStop(false);
                  if (chr.getMap().getId() / 10000 == 92507) {
                     chr.getClient().getSession().writeAndFlush(CField.getDojoClockStop(false, 900 - chr.getDojoStartTime()));
                  }

                  chr.MulungTimer = null;
                  chr.cancelTimer();
               }
            };
            chr.MulungTimer.schedule(chr.MulungTimerTask, 10000L);
         }
      }

      if (chr.getParty() != null && GameConstants.price.containsKey(monster.getId())) {
         var77 = chr.getParty().getMembers().iterator();

         while(var77.hasNext()) {
            pc = (MaplePartyCharacter)var77.next();
            MapleCharacter pz = chr.getClient().getChannelServer().getPlayerStorage().getCharacterByName(pc.getName());
            if (pz != null) {
               pz.setLastBossId(monster.getId());
            }
         }
      }

      if (monster.getBuffToGive() > -1) {
         k = monster.getBuffToGive();
         SecondaryStatEffect buff = MapleItemInformationProvider.getInstance().getItemEffect(k);
         Iterator itr = this.characters.iterator();

         while(itr.hasNext()) {
            MapleCharacter mc = (MapleCharacter)itr.next();
            if (mc.isAlive()) {
               buff.applyTo(mc, true);
               switch(monster.getId()) {
               case 8810018:
               case 8810122:
               case 8810214:
               case 8820001:
                  mc.getClient().getSession().writeAndFlush(CField.EffectPacket.showNormalEffect(mc, 14, true));
                  this.broadcastMessage(mc, CField.EffectPacket.showNormalEffect(mc, 14, false), false);
               }
            }
         }
      }

      k = monster.getId();
      if (!monster.getStats().isBoss() && chr.getBuffedValue(SecondaryStat.Reincarnation) != null && chr.getReinCarnation() > 0) {
         chr.setReinCarnation(chr.getReinCarnation() - 1);
         if (chr.getReinCarnation() == 0) {
            chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, 1320019, 1, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), true, chr.getPosition(), (String)null, (Item)null));
            chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 0, 1320019, 1, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), false, chr.getPosition(), (String)null, (Item)null), false);
         }

         Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
         statups.put(SecondaryStat.Reincarnation, new Pair(1, (int)chr.getBuffLimit(chr.getBuffSource(SecondaryStat.Reincarnation))));
         chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, chr.getBuffedEffect(SecondaryStat.Reincarnation), chr));
      }

      if (k / 100000 == 98 && chr.getMapId() / 10000000 == 95 && this.getNumMonsters() == 0) {
         switch(chr.getMapId() % 1000 / 100) {
         case 0:
         case 1:
         case 2:
         case 3:
         case 4:
            chr.getClient().getSession().writeAndFlush(CField.showEffect("monsterPark/clear"));
            chr.getClient().getSession().writeAndFlush(SLFCGPacket.playSE("Party1/Cleard"));
            break;
         case 5:
            if (chr.getMapId() % 1000 == 500) {
               chr.getClient().getSession().writeAndFlush(CField.showEffect("monsterPark/clearF"));
               chr.getClient().getSession().writeAndFlush(SLFCGPacket.playSE("Party1/Cleard"));
               chr.dropMessage(-1, "모든 스테이지를 클리어 하셨습니다. 포탈을 통해 밖으로 이동해주세요.");
            }
            break;
         case 6:
            chr.getClient().getSession().writeAndFlush(CField.showEffect("monsterPark/clearF"));
         }
      }

      if (this.rune == null && !chr.getBuffedValue(80002282) && !this.isTown() && this.getAllMonster().size() > 0 && this.isSpawnPoint() && !GameConstants.isContentsMap(this.getId()) && !GameConstants.보스맵(this.getId()) && !GameConstants.사냥컨텐츠맵(this.getId()) && !GameConstants.튜토리얼(this.getId()) && !GameConstants.로미오줄리엣(this.getId()) && !GameConstants.피라미드(this.getId())) {
         type = Randomizer.rand(0, this.monsterSpawn.size() - 1);
         MapleReactor ract = (MapleReactor)this.getAllReactor().get(0);
         if (ract != null) {
            while(type == ract.getSpawnPointNum()) {
               type = Randomizer.rand(0, this.monsterSpawn.size() - 1);
            }
         }

         Point poss = ((Spawns)this.monsterSpawn.get(type)).getPosition();
         MapleRune rune = new MapleRune(Randomizer.rand(0, 9), poss.x, poss.y, this);
         rune.setSpawnPointNum(type);
         this.spawnRune(rune);
      }

      if (chr.getV("bossPractice") != null && Integer.parseInt(chr.getV("bossPractice")) == 1) {
         withDrops = false;
      }

      if (withDrops) {
         this.dropFromMonster(chr, monster, instanced);
      }

      if (monster.getEventInstance() != null) {
         monster.getEventInstance().monsterKilled(chr, monster);
      } else {
         EventInstanceManager em = chr.getEventInstance();
         if (em != null) {
            em.monsterKilled(chr, monster);
         }
      }

   }

   public List<MapleReactor> getAllReactor() {
      return this.getAllReactorsThreadsafe();
   }

   public List<MapleReactor> getAllReactorsThreadsafe() {
      ArrayList<MapleReactor> ret = new ArrayList();
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.REACTOR)).values().iterator();

      while(var2.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var2.next();
         ret.add((MapleReactor)mmo);
      }

      return ret;
   }

   public List<MapleSummon> getAllSummonsThreadsafe() {
      ArrayList<MapleSummon> ret = new ArrayList();
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.SUMMON)).values().iterator();

      while(var2.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var2.next();
         if (mmo instanceof MapleSummon) {
            ret.add((MapleSummon)mmo);
         }
      }

      return ret;
   }

   public List<MapleSummon> getAllSummons(int skillId) {
      ArrayList<MapleSummon> ret = new ArrayList();
      Iterator var3 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.SUMMON)).values().iterator();

      while(var3.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var3.next();
         if (mmo instanceof MapleSummon && ((MapleSummon)mmo).getSkill() == skillId) {
            ret.add((MapleSummon)mmo);
         }
      }

      return ret;
   }

   public List<MapleFlyingSword> getAllFlyingSwordsThreadsafe() {
      ArrayList<MapleFlyingSword> ret = new ArrayList();
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.SWORD)).values().iterator();

      while(var2.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var2.next();
         ret.add((MapleFlyingSword)mmo);
      }

      return ret;
   }

   public List<MapleMapObject> getAllDoor() {
      return this.getAllDoorsThreadsafe();
   }

   public List<MapleMapObject> getAllDoorsThreadsafe() {
      ArrayList<MapleMapObject> ret = new ArrayList();
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.DOOR)).values().iterator();

      while(var2.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var2.next();
         if (mmo instanceof MapleDoor) {
            ret.add(mmo);
         }
      }

      return ret;
   }

   public List<MapleMapObject> getAllMechDoorsThreadsafe() {
      ArrayList<MapleMapObject> ret = new ArrayList();
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.DOOR)).values().iterator();

      while(var2.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var2.next();
         if (mmo instanceof MechDoor) {
            ret.add(mmo);
         }
      }

      return ret;
   }

   public List<MapleMapObject> getAllMerchant() {
      return this.getAllHiredMerchantsThreadsafe();
   }

   public List<MapleMapObject> getAllHiredMerchantsThreadsafe() {
      ArrayList<MapleMapObject> ret = new ArrayList();
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.HIRED_MERCHANT)).values().iterator();

      while(var2.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var2.next();
         ret.add(mmo);
      }

      return ret;
   }

   public List<MapleSpecialChair> getAllSpecialChairs() {
      ArrayList<MapleSpecialChair> ret = new ArrayList();
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.SPECIAL_CHAIR)).values().iterator();

      while(var2.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var2.next();
         ret.add((MapleSpecialChair)mmo);
      }

      return ret;
   }

   public List<MapleCharacter> getAllChracater() {
      return this.getAllCharactersThreadsafe();
   }

   public List<MapleCharacter> getAllCharactersThreadsafe() {
      ArrayList<MapleCharacter> ret = new ArrayList();
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.PLAYER)).values().iterator();

      while(var2.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var2.next();
         ret.add((MapleCharacter)mmo);
      }

      return ret;
   }

   public List<MapleMagicSword> getAllMagicSword() {
      ArrayList<MapleMagicSword> ret = new ArrayList();
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.MagicSword)).values().iterator();

      while(var2.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var2.next();
         ret.add((MapleMagicSword)mmo);
      }

      return ret;
   }

   public MapleRandomPortal getPoloFrittoPortal() {
      Iterator var1 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.RANDOM_PORTAL)).values().iterator();

      MapleRandomPortal p;
      do {
         if (!var1.hasNext()) {
            return null;
         }

         MapleMapObject mmo = (MapleMapObject)var1.next();
         p = (MapleRandomPortal)mmo;
      } while(p.getPortalType() != 2);

      return p;
   }

   public MapleRandomPortal getFireWolfPortal() {
      Iterator var1 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.RANDOM_PORTAL)).values().iterator();

      MapleRandomPortal p;
      do {
         if (!var1.hasNext()) {
            return null;
         }

         MapleMapObject mmo = (MapleMapObject)var1.next();
         p = (MapleRandomPortal)mmo;
      } while(p.getPortalType() != 3);

      return p;
   }

   public SecondAtom getSecondAtom(int playerId, int skillId) {
      Iterator var3 = this.getAllSecondAtomsThreadsafe().iterator();

      SecondAtom atom;
      do {
         if (!var3.hasNext()) {
            return null;
         }

         atom = (SecondAtom)var3.next();
      } while(atom == null || atom.getOwnerId() != playerId || atom.getSkillId() != skillId);

      return atom;
   }

   public SecondAtom getSecondAtomOid(int playerId, int oid) {
      Iterator var3 = this.getAllSecondAtomsThreadsafe().iterator();

      SecondAtom atom;
      do {
         if (!var3.hasNext()) {
            return null;
         }

         atom = (SecondAtom)var3.next();
      } while(atom == null || atom.getOwnerId() != playerId || atom.getObjectId() != oid);

      return atom;
   }

   public List<SecondAtom> getSecondAtoms(int playerId, int skillId) {
      ArrayList<SecondAtom> ret = new ArrayList();
      Iterator var4 = this.getAllSecondAtomsThreadsafe().iterator();

      while(var4.hasNext()) {
         SecondAtom atom = (SecondAtom)var4.next();
         if (atom != null && atom.getOwnerId() == playerId && atom.getSkillId() == skillId) {
            ret.add(atom);
         }
      }

      return ret;
   }

   public List<SecondAtom> getAllSecondAtomsThreadsafe() {
      ArrayList<SecondAtom> ret = new ArrayList();
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.A_SECOND_ATOM)).values().iterator();

      while(var2.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var2.next();
         ret.add((SecondAtom)mmo);
      }

      return ret;
   }

   public List<SecondAtom> getAllSecondAtomsThread() {
      ArrayList<SecondAtom> ret = new ArrayList();
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.A_SECOND_ATOM)).values().iterator();

      while(var2.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var2.next();
         ret.add((SecondAtom)mmo);
      }

      return ret;
   }

   public List<MapleMonster> getAllMonster() {
      return this.getAllMonstersThreadsafe();
   }

   public List<MapleMonster> getAllMonstersThreadsafe() {
      ArrayList<MapleMonster> ret = new ArrayList();
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.MONSTER)).values().iterator();

      while(var2.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var2.next();
         ret.add((MapleMonster)mmo);
      }

      return ret;
   }

   public List<MapleMonster> getAllNormalMonstersThreadsafe() {
      ArrayList<MapleMonster> ret = new ArrayList();
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.MONSTER)).values().iterator();

      while(var2.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var2.next();
         MapleMonster monster = (MapleMonster)mmo;
         if (!monster.getStats().isBoss()) {
            ret.add(monster);
         }
      }

      return ret;
   }

   public List<Integer> getAllUniqueMonsters() {
      ArrayList<Integer> ret = new ArrayList();
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.MONSTER)).values().iterator();

      while(var2.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var2.next();
         int theId = ((MapleMonster)mmo).getId();
         if (!ret.contains(theId)) {
            ret.add(theId);
         }
      }

      return ret;
   }

   public final void killAllMonsters(boolean animate) {
      Iterator var2 = this.getAllMonstersThreadsafe().iterator();

      while(var2.hasNext()) {
         MapleMapObject mapleMapObject = (MapleMapObject)var2.next();
         MapleMonster monster = (MapleMonster)mapleMapObject;
         if (this.RealSpawns.contains(monster)) {
            this.RealSpawns.remove(monster);
         }

         monster.setHp(0L);
         this.broadcastMessage(MobPacket.killMonster(monster.getObjectId(), animate ? 1 : 0));
         this.broadcastMessage(MobPacket.stopControllingMonster(monster.getObjectId()));
         this.removeMapObject(monster);
         monster.killed();
         this.spawnedMonstersOnMap.decrementAndGet();
      }

   }

   public final void killAllMonsters(MapleCharacter chr) {
      Iterator var2 = this.getAllMonstersThreadsafe().iterator();

      while(var2.hasNext()) {
         MapleMapObject mapleMapObject = (MapleMapObject)var2.next();
         MapleMonster monster = (MapleMonster)mapleMapObject;
         if (monster.getOwner() == chr.getId()) {
            if (this.RealSpawns.contains(monster)) {
               this.RealSpawns.remove(monster);
            }

            monster.setHp(0L);
            this.broadcastMessage(MobPacket.killMonster(monster.getObjectId(), 0));
            this.broadcastMessage(MobPacket.stopControllingMonster(monster.getObjectId()));
            this.removeMapObject(monster);
            monster.killed();
            this.spawnedMonstersOnMap.decrementAndGet();
         }
      }

   }

   public final void killMonster(int monsId) {
      Iterator var2 = this.getAllMonstersThreadsafe().iterator();

      while(var2.hasNext()) {
         MapleMapObject mapleMapObject = (MapleMapObject)var2.next();
         MapleMonster mob = (MapleMonster)mapleMapObject;
         if (mob.getId() == monsId) {
            if (this.RealSpawns.contains(mob)) {
               this.RealSpawns.remove(mob);
            }

            mob.setHp(0L);
            this.broadcastMessage(MobPacket.killMonster(mob.getObjectId(), 1));
            this.broadcastMessage(MobPacket.stopControllingMonster(mob.getObjectId()));
            this.removeMapObject(mob);
            mob.killed();
            this.spawnedMonstersOnMap.decrementAndGet();
         }
      }

   }

   public final void killMonsterDealy(MapleMonster mob) {
      if (mob != null && mob.isAlive()) {
         if (this.RealSpawns.contains(mob)) {
            this.RealSpawns.remove(mob);
         }

         mob.setHp(0L);
         this.broadcastMessage(MobPacket.killMonster(mob.getObjectId(), 1));
         this.broadcastMessage(MobPacket.stopControllingMonster(mob.getObjectId()));
         this.removeMapObject(mob);
         mob.killed();
         this.spawnedMonstersOnMap.decrementAndGet();
      }

   }

   public final void killMonsterType(MapleMonster mob, int type) {
      if (mob != null && mob.isAlive()) {
         if (this.RealSpawns.contains(mob)) {
            this.RealSpawns.remove(mob);
         }

         this.removeMapObject(mob);
         mob.killed();
         this.spawnedMonstersOnMap.decrementAndGet();
         this.broadcastMessage(MobPacket.killMonster(mob.getObjectId(), type));
         this.broadcastMessage(MobPacket.stopControllingMonster(mob.getObjectId()));
      }

   }

   public final void limitReactor(int rid, int num) {
      List<MapleReactor> toDestroy = new ArrayList();
      Map<Integer, Integer> contained = new LinkedHashMap();
      Iterator var5 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.REACTOR)).values().iterator();

      while(var5.hasNext()) {
         MapleMapObject obj = (MapleMapObject)var5.next();
         MapleReactor mr = (MapleReactor)obj;
         if (contained.containsKey(mr.getReactorId())) {
            if ((Integer)contained.get(mr.getReactorId()) >= num) {
               toDestroy.add(mr);
            } else {
               contained.put(mr.getReactorId(), (Integer)contained.get(mr.getReactorId()) + 1);
            }
         } else {
            contained.put(mr.getReactorId(), 1);
         }
      }

      var5 = toDestroy.iterator();

      while(var5.hasNext()) {
         MapleReactor mr2 = (MapleReactor)var5.next();
         this.destroyReactor(mr2.getObjectId());
      }

   }

   public final void destroyReactors(int first, int last) {
      ArrayList<MapleReactor> toDestroy = new ArrayList();
      Iterator var4 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.REACTOR)).values().iterator();

      while(var4.hasNext()) {
         MapleMapObject obj = (MapleMapObject)var4.next();
         MapleReactor mr = (MapleReactor)obj;
         if (mr.getReactorId() >= first && mr.getReactorId() <= last) {
            toDestroy.add(mr);
         }
      }

      var4 = toDestroy.iterator();

      while(var4.hasNext()) {
         MapleReactor mr = (MapleReactor)var4.next();
         this.destroyReactor(mr.getObjectId());
      }

   }

   public final void destroyReactor(int oid) {
      final MapleReactor reactor = this.getReactorByOid(oid);
      if (reactor != null) {
         this.broadcastMessage(CField.destroyReactor(reactor));
         reactor.setAlive(false);
         this.removeMapObject(reactor);
         reactor.setTimerActive(false);
         if (reactor.getDelay() > 0) {
            Timer.MapTimer.getInstance().schedule(new Runnable() {
               public final void run() {
                  MapleMap.this.respawnReactor(reactor);
               }
            }, (long)reactor.getDelay());
         }

      }
   }

   public final void reloadReactors() {
      List<MapleReactor> toSpawn = new ArrayList();
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.REACTOR)).values().iterator();

      while(var2.hasNext()) {
         MapleMapObject obj = (MapleMapObject)var2.next();
         MapleReactor reactor = (MapleReactor)obj;
         this.broadcastMessage(CField.destroyReactor(reactor));
         reactor.setAlive(false);
         reactor.setTimerActive(false);
         toSpawn.add(reactor);
      }

      var2 = toSpawn.iterator();

      while(var2.hasNext()) {
         MapleReactor r = (MapleReactor)var2.next();
         this.removeMapObject(r);
         this.respawnReactor(r);
      }

   }

   public final void resetReactors(MapleClient c) {
      this.setReactorState((byte)0, c);
   }

   public final void setReactorState(MapleClient c) {
      this.setReactorState((byte)1, c);
   }

   public final void setReactorState(byte state, MapleClient c) {
      Iterator var3 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.REACTOR)).values().iterator();

      while(var3.hasNext()) {
         MapleMapObject obj = (MapleMapObject)var3.next();
         ((MapleReactor)obj).forceHitReactor(state, c == null ? 0 : c.getPlayer().getId());
      }

   }

   public final void setReactorDelay(int state) {
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.REACTOR)).values().iterator();

      while(var2.hasNext()) {
         MapleMapObject obj = (MapleMapObject)var2.next();
         ((MapleReactor)obj).setDelay(state);
      }

   }

   public final void shuffleReactors() {
      this.shuffleReactors(0, 9999999);
   }

   public final void shuffleReactors(int first, int last) {
      List<Point> points = new ArrayList();
      Iterator var4 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.REACTOR)).values().iterator();

      MapleMapObject obj;
      MapleReactor mr;
      while(var4.hasNext()) {
         obj = (MapleMapObject)var4.next();
         mr = (MapleReactor)obj;
         if (mr.getReactorId() >= first && mr.getReactorId() <= last) {
            points.add(mr.getPosition());
         }
      }

      Collections.shuffle(points);
      var4 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.REACTOR)).values().iterator();

      while(var4.hasNext()) {
         obj = (MapleMapObject)var4.next();
         mr = (MapleReactor)obj;
         if (mr.getReactorId() >= first && mr.getReactorId() <= last) {
            mr.setPosition((Point)points.remove(points.size() - 1));
         }
      }

   }

   public final void updateMonsterController(MapleMonster monster) {
      if (monster.isAlive() && monster.getLinkCID() <= 0 && !monster.getStats().isEscort()) {
         if (monster.getController() != null) {
            if (monster.getController().getMap() == this && !(monster.getController().getTruePosition().distanceSq(monster.getTruePosition()) > monster.getRange())) {
               return;
            }

            monster.getController().stopControllingMonster(monster);
         }

         if (monster.getStats().isMobZone()) {
            byte phase;
            if (monster.getHPPercent() > 75) {
               phase = 1;
            } else if (monster.getHPPercent() > 50) {
               phase = 2;
            } else if (monster.getHPPercent() > 25) {
               phase = 3;
            } else {
               phase = 4;
            }

            if (monster.getPhase() != phase) {
               monster.setPhase(phase);
               this.broadcastMessage(MobPacket.changePhase(monster));
               this.broadcastMessage(MobPacket.changeMobZone(monster));
            }
         }

         int mincontrolled = -1;
         MapleCharacter newController = null;
         Iterator ltr = this.characters.iterator();

         while(true) {
            MapleCharacter chr;
            do {
               do {
                  if (!ltr.hasNext()) {
                     if (newController != null) {
                        if (monster.isFirstAttack()) {
                           newController.controlMonster(monster, true);
                           monster.setControllerHasAggro(true);
                        } else {
                           newController.controlMonster(monster, false);
                        }
                     }

                     return;
                  }

                  chr = (MapleCharacter)ltr.next();
               } while(chr.isHidden());
            } while(chr.getControlledSize() >= mincontrolled && mincontrolled != -1);

            if (chr.getTruePosition().distanceSq(monster.getTruePosition()) <= monster.getRange()) {
               if (monster.getOwner() == -1) {
                  mincontrolled = chr.getControlledSize();
                  newController = chr;
               } else if (monster.getOwner() == chr.getId()) {
                  mincontrolled = chr.getControlledSize();
                  newController = chr;
               }
            }
         }
      }
   }

   public final MapleMapObject getMapObject(int oid, MapleMapObjectType type) {
      return (MapleMapObject)((ConcurrentHashMap)this.mapobjects.get(type)).get(oid);
   }

   public final boolean containsNPC(int npcid) {
      Iterator itr = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.NPC)).values().iterator();

      MapleNPC n;
      do {
         if (!itr.hasNext()) {
            return false;
         }

         n = (MapleNPC)itr.next();
      } while(n.getId() != npcid);

      return true;
   }

   public MapleNPC getNPCById(int id) {
      Iterator itr = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.NPC)).values().iterator();

      MapleNPC n;
      do {
         if (!itr.hasNext()) {
            return null;
         }

         n = (MapleNPC)itr.next();
      } while(n.getId() != id);

      return n;
   }

   public MapleMonster getMonsterById(int id) {
      MapleMonster ret = null;
      Iterator itr = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.MONSTER)).values().iterator();

      while(itr.hasNext()) {
         MapleMonster n = (MapleMonster)itr.next();
         if (n.getId() == id) {
            ret = n;
            break;
         }
      }

      return ret;
   }

   public int countOrgelById(boolean purple) {
      int ret = 0;
      Iterator itr = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.MONSTER)).values().iterator();

      while(itr.hasNext()) {
         MapleMonster n = (MapleMonster)itr.next();
         if (n.getId() / 10 == (purple ? 983308 : 983307)) {
            ++ret;
         }
      }

      return ret;
   }

   public int countMonsterById(int id) {
      int ret = 0;
      Iterator itr = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.MONSTER)).values().iterator();

      while(itr.hasNext()) {
         MapleMonster n = (MapleMonster)itr.next();
         if (n.getId() == id) {
            ++ret;
         }
      }

      return ret;
   }

   public MapleReactor getReactorById(int id) {
      MapleReactor ret = null;
      Iterator itr = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.REACTOR)).values().iterator();

      while(itr.hasNext()) {
         MapleReactor n = (MapleReactor)itr.next();
         if (n.getReactorId() == id) {
            ret = n;
            break;
         }
      }

      return ret;
   }

   public final MapleMonster getMonsterByOid(int oid) {
      MapleMapObject mmo = this.getMapObject(oid, MapleMapObjectType.MONSTER);
      return mmo == null ? null : (MapleMonster)mmo;
   }

   public final MapleSummon getSummonByOid(int oid) {
      MapleMapObject mmo = this.getMapObject(oid, MapleMapObjectType.SUMMON);
      return mmo == null ? null : (MapleSummon)mmo;
   }

   public final MapleNPC getNPCByOid(int oid) {
      MapleMapObject mmo = this.getMapObject(oid, MapleMapObjectType.NPC);
      return mmo == null ? null : (MapleNPC)mmo;
   }

   public final MapleReactor getReactorByOid(int oid) {
      MapleMapObject mmo = this.getMapObject(oid, MapleMapObjectType.REACTOR);
      return mmo == null ? null : (MapleReactor)mmo;
   }

   public final MapleReactor getReactorByName(String name) {
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.REACTOR)).values().iterator();

      MapleReactor mr;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         MapleMapObject obj = (MapleMapObject)var2.next();
         mr = (MapleReactor)obj;
      } while(!mr.getName().equalsIgnoreCase(name));

      return mr;
   }

   public final void spawnNpc(int id, Point pos) {
      MapleNPC npc = MapleLifeFactory.getNPC(id);
      npc.setPosition(pos);
      npc.setCy(pos.y);
      npc.setRx0(pos.x + 50);
      npc.setRx1(pos.x - 50);
      npc.setFh(this.getFootholds().findBelow(pos).getId());
      npc.setCustom(true);
      this.addMapObject(npc);
      this.broadcastMessage(CField.NPCPacket.spawnNPC(npc, true));
   }

   public final void removeNpc(int npcid) {
      Iterator itr = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.NPC)).values().iterator();

      while(true) {
         MapleNPC npc;
         do {
            do {
               if (!itr.hasNext()) {
                  return;
               }

               npc = (MapleNPC)itr.next();
            } while(!npc.isCustom());
         } while(npcid != -1 && npc.getId() != npcid);

         this.broadcastMessage(CField.NPCPacket.removeNPCController(npc.getObjectId()));
         this.broadcastMessage(CField.NPCPacket.removeNPC(npc.getObjectId()));
         itr.remove();
      }
   }

   public final void hideNpc(int npcid) {
      Iterator itr = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.NPC)).values().iterator();

      while(true) {
         MapleNPC npc;
         do {
            if (!itr.hasNext()) {
               return;
            }

            npc = (MapleNPC)itr.next();
         } while(npcid != -1 && npc.getId() != npcid);

         this.broadcastMessage(CField.NPCPacket.removeNPCController(npc.getObjectId()));
         this.broadcastMessage(CField.NPCPacket.removeNPC(npc.getObjectId()));
      }
   }

   public final void spawnReactorOnGroundBelow(MapleReactor mob, Point pos) {
      mob.setPosition(pos);
      mob.setCustom(true);
      this.spawnReactor(mob);
   }

   public final void spawnMonster_sSack(MapleMonster mob, Point pos, int spawnType) {
      mob.setPosition(this.calcPointBelow(new Point(pos.x, pos.y - 1)) == null ? new Point(pos.x, pos.y) : this.calcPointBelow(new Point(pos.x, pos.y - 1)));
      this.spawnMonster(mob, spawnType);
   }

   public final void spawnMonsterOnGroundBelow(MapleMonster mob, Point pos) {
      this.spawnMonster_sSack(mob, pos, mob.getId() == 8880512 ? 1 : -2);
   }

   public final void spawnMonsterOnGroundBelowBlackMage(MapleMonster mob, Point pos) {
      mob.setFh(3);
      mob.setF(3);
      this.spawnMonster_sSack(mob, pos, mob.getId() == 8880512 ? 1 : -2);
   }

   public final int spawnMonsterWithEffectBelow(MapleMonster mob, Point pos, int effect) {
      Point spos = this.calcPointBelow(new Point(pos.x, pos.y - 1));
      return this.spawnMonsterWithEffect(mob, effect, spos);
   }

   public final void spawnZakum(MapleCharacter chr, int x, int y) {
      Point pos = new Point(x, y);
      MapleMonster mainb = MapleLifeFactory.getMonster(8800002);
      Point spos = this.calcPointBelow(new Point(pos.x, pos.y));
      EventInstanceManager eim = chr.getEventInstance();
      if (eim != null) {
         int[] zakpart = new int[]{8800003, 8800004, 8800005, 8800006, 8800007, 8800008, 8800009, 8800010};
         int[] var9 = zakpart;
         int var10 = zakpart.length;

         for(int var11 = 0; var11 < var10; ++var11) {
            int i = var9[var11];
            MapleMonster part = MapleLifeFactory.getMonster(i);
            part.setF(1);
            part.setFh(1);
            part.setStance(5);
            part.setPhase((byte)1);
            eim.registerMonster(part);
            eim.getMapInstance(0).spawnMonsterWithEffectBelow(part, spos, -2);
            part.getMap().broadcastMessage(MobPacket.changePhase(part));
         }

         mainb.setCustomInfo(8800002, 0, 3000);
         mainb.setCustomInfo(8800003, 2, 0);
         mainb.setPhase((byte)1);
         mainb.setSpecialtxt("properties");
         eim.registerMonster(mainb);
         eim.getMapInstance(0).spawnMonsterWithEffectBelow(mainb, spos, -1);
         this.broadcastMessage(MobPacket.setMonsterProPerties(mainb.getObjectId(), 1, 0, 0));
         this.broadcastMessage(MobPacket.getSmartNotice(mainb.getId(), 0, 5, 1, "자쿰이 팔을 들어 내려칠 준비를 합니다"));
         mainb.getMap().broadcastMessage(MobPacket.changePhase(mainb));
      }

   }

   public final void spawnChaosZakum(MapleCharacter chr, int x, int y) {
      Point pos = new Point(x, y);
      MapleMonster mainb = MapleLifeFactory.getMonster(8800102);
      Point spos = this.calcPointBelow(new Point(pos.x, pos.y));
      EventInstanceManager eim = chr.getEventInstance();
      if (eim != null) {
         int[] zakpart = new int[]{8800103, 8800104, 8800105, 8800106, 8800107, 8800108, 8800109, 8800110};
         int[] var9 = zakpart;
         int var10 = zakpart.length;

         for(int var11 = 0; var11 < var10; ++var11) {
            int i = var9[var11];
            MapleMonster part = MapleLifeFactory.getMonster(i);
            part.setF(1);
            part.setFh(1);
            part.setStance(5);
            part.setPhase((byte)1);
            eim.registerMonster(part);
            eim.getMapInstance(0).spawnMonsterWithEffectBelow(part, spos, -2);
            part.getMap().broadcastMessage(MobPacket.changePhase(part));
         }

         mainb.setCustomInfo(8800002, 0, 3000);
         mainb.setCustomInfo(8800003, 2, 0);
         mainb.setPhase((byte)1);
         mainb.setSpecialtxt("properties");
         eim.registerMonster(mainb);
         eim.getMapInstance(0).spawnMonsterWithEffectBelow(mainb, spos, -1);
         this.broadcastMessage(MobPacket.setMonsterProPerties(mainb.getObjectId(), 1, 0, 0));
         this.broadcastMessage(MobPacket.getSmartNotice(mainb.getId(), 0, 5, 1, "자쿰이 팔을 들어 내려칠 준비를 합니다"));
         mainb.getMap().broadcastMessage(MobPacket.changePhase(mainb));
      }

   }

   public final void spawnFakeMonsterOnGroundBelow(MapleMonster mob, Point pos) {
      Point spos = this.calcPointBelow(new Point(pos.x, pos.y - 1));
      --spos.y;
      mob.setPosition(spos);
      this.spawnFakeMonster(mob);
   }

   private void checkRemoveAfter(MapleMonster monster) {
      int ra = monster.getStats().getRemoveAfter();
      if (ra > 0 && monster.getLinkCID() <= 0) {
         monster.registerKill((long)(ra * 1000));
      }

   }

   public final void spawnRevives(final MapleMonster monster, final int oid) {
      monster.setMap(this);
      this.checkRemoveAfter(monster);
      monster.setLinkOid(oid);
      if (monster.getHp() <= 0L) {
         MapleMonster mob = MapleLifeFactory.getMonster(monster.getId());
         if (mob != null) {
            monster.setHp(mob.getStats().getHp());
            monster.getStats().setHp(mob.getStats().getHp());
         }
      }

      this.spawnAndAddRangedMapObject(monster, new MapleMap.DelayedPacketCreation() {
         public final void sendPackets(MapleClient c) {
            if (monster.getOwner() == -1) {
               c.getSession().writeAndFlush(MobPacket.spawnMonster(monster, monster.getStats().getSummonType() <= 1 ? -3 : monster.getStats().getSummonType(), oid));
            } else if (monster.getOwner() == c.getPlayer().getId()) {
               c.getSession().writeAndFlush(MobPacket.spawnMonster(monster, monster.getStats().getSummonType() <= 1 ? -3 : monster.getStats().getSummonType(), oid));
            }

         }
      });
      this.updateMonsterController(monster);
      this.spawnedMonstersOnMap.incrementAndGet();
      if (monster.getId() >= 9833070 && monster.getId() <= 9833074) {
         Timer.MapTimer.getInstance().schedule(() -> {
            if (monster != null && monster.isAlive() && !this.getAllCharactersThreadsafe().isEmpty()) {
               MapleCharacter player = (MapleCharacter)this.getAllCharactersThreadsafe().get(0);
               if (player != null) {
                  Point pos = monster.getTruePosition();
                  this.killMonster(monster, player, false, false, (byte)1);
                  MapleMonster mob = MapleLifeFactory.getMonster(monster.getId() + 10);
                  mob.setHp(GameConstants.getDreamBreakerHP((int)player.getKeyValue(15901, "stage")));
                  this.spawnMonsterOnGroundBelow(mob, pos);
               }
            }

         }, 35000L);
      }

      if (monster.getId() != 8500001 && monster.getId() != 8500011 && monster.getId() != 8500021) {
         if (monster.getId() != 8500002 && monster.getId() != 8500012 && monster.getId() != 8500022) {
            if (monster.getId() == 8810001 || monster.getId() == 8810000 || monster.getId() == 8810100 || monster.getId() == 8810101) {
               this.broadcastMessage(CWvsContext.serverNotice(6, "", "깊은 동굴 속에서 거대한 생물체가 다가오고 있습니다."));
            }
         } else {
            if (monster.getId() == 8500022) {
               monster.getStats().setHp(184600000000L);
               monster.setHp(184600000000L);
            }

            monster.SetPatten(60);
            monster.getMap().broadcastMessage(MobPacket.BossPapuLatus.PapulLatusTime(monster.getPatten() * 1000, false, false));
         }
      } else {
         monster.SetPatten(120);
         if (monster.getId() == 8500021) {
            monster.getStats().setHp(553000000000L);
            monster.setHp(553000000000L);
         }

         MobSkillFactory.getMobSkill(241, 7).setOnce(false);
         monster.getMap().broadcastMessage(MobPacket.BossPapuLatus.PapulLatusLaser(false, 1));
         monster.getMap().broadcastMessage(MobPacket.BossPapuLatus.PapulLatusTime(monster.getPatten() * 1000, false, false));
         monster.getMap().broadcastMessage(MobPacket.BossPapuLatus.PapulLatusPincers(true, 0, 0, 0));
         this.PapulratusTime = 50;
      }

   }

   public final void spawnMonsterDelay(MapleMonster monster, int spawnType, int delay) {
      Timer.MapTimer.getInstance().schedule(() -> {
         this.spawnMonster(monster, spawnType, false);
      }, (long)delay);
   }

   public final void spawnMonsterDelayid(int mobid, int spawnType, int delay, int x, int y) {
      Timer.MapTimer.getInstance().schedule(() -> {
         MapleMonster mob = MapleLifeFactory.getMonster(mobid);
         mob.setPosition(new Point(x, y));
         this.spawnMonster(mob, spawnType, false);
      }, (long)delay);
   }

   public final void spawnMonster(MapleMonster monster, int spawnType) {
      this.spawnMonster(monster, spawnType, false);
   }

   public final void spawnMonster(final MapleMonster monster, final int spawnType, final boolean overwrite) {
      Iterator var6;
      MapleMonster mapleMonster;
      if (monster.getId() == 8880101 || monster.getId() == 8880111) {
         boolean alreday = false;
         var6 = this.getAllMonster().iterator();

         label256: {
            do {
               if (!var6.hasNext()) {
                  break label256;
               }

               mapleMonster = (MapleMonster)var6.next();
            } while(mapleMonster.getId() != 8880101 && mapleMonster.getId() != 8880111);

            alreday = true;
         }

         if (alreday) {
            return;
         }
      }

      monster.setMap(this);
      monster.setSpawnTime(System.currentTimeMillis());
      MapleMonster mob;
      if (monster.getHp() <= 0L && (mob = MapleLifeFactory.getMonster(monster.getId())) != null) {
         monster.setHp(mob.getStats().getHp());
         monster.getStats().setHp(mob.getStats().getHp());
      }

      this.checkRemoveAfter(monster);
      new ArrayList();
      this.spawnAndAddRangedMapObject(monster, new MapleMap.DelayedPacketCreation() {
         public final void sendPackets(MapleClient c) {
            if (monster.getOwner() == -1) {
               c.getSession().writeAndFlush(MobPacket.spawnMonster(monster, monster.getStats().getSummonType() > 1 && monster.getStats().getSummonType() != 27 && !overwrite ? monster.getStats().getSummonType() : spawnType, 0));
            } else if (monster.getOwner() == c.getPlayer().getId()) {
               c.getSession().writeAndFlush(MobPacket.spawnMonster(monster, monster.getStats().getSummonType() > 1 && monster.getStats().getSummonType() != 27 && !overwrite ? monster.getStats().getSummonType() : spawnType, 0));
            }

         }
      });
      this.updateMonsterController(monster);
      if (monster.getOwner() < 0) {
         this.spawnedMonstersOnMap.incrementAndGet();
      }

      if (monster.getSeperateSoul() == 0) {
         if (monster.getId() == 8900000 || monster.getId() == 8900100) {
            monster.lastCapTime = 1L;
         }

         MobSkill mobSkill;
         if (monster.getId() == 8880300 || monster.getId() == 8880340) {
            monster.getWillHplist().add(666);
            monster.getWillHplist().add(333);
            monster.getWillHplist().add(3);
            this.broadcastMessage(MobPacket.BossWill.setWillHp(monster.getWillHplist(), this, monster.getId(), monster.getId() + 3, monster.getId() + 4));
            monster.setCustomInfo(24205, 0, 61000);
            var6 = monster.getStats().getSkills().iterator();

            while(var6.hasNext()) {
               mobSkill = (MobSkill)var6.next();
               monster.setLastSkillUsed(mobSkill, System.currentTimeMillis(), 99999999L);
            }
         }

         if (monster.getId() == 8880301 || monster.getId() == 8880341) {
            monster.getWillHplist().add(500);
            monster.getWillHplist().add(3);
            this.broadcastMessage(MobPacket.BossWill.setWillHp(monster.getWillHplist()));
         }

         if (monster.getId() != 8880000 && monster.getId() != 8880002 && monster.getId() != 8880010) {
            if (monster.getId() / 1000 == 8900 && monster.getId() % 10 < 3 && this.getNumMonsters() <= 2 && monster.getSeperateSoul() <= 0) {
               MapleBossManager.pierreHandler(monster);
            } else if (monster.getId() != 8950000 && monster.getId() != 8950001 && monster.getId() != 8950002 && monster.getId() != 8950100 && monster.getId() != 8950101 && monster.getId() != 8950102) {
               if (monster.getId() != 8880100 && monster.getId() != 8880110 && monster.getId() != 8880101 && monster.getId() != 8880111) {
                  if (monster.getId() == 8880512) {
                     MapleBossManager.blackMageHandler(monster);
                  } else if (monster.getId() != 8644650 && monster.getId() != 8644655) {
                     if (monster.getId() != 8645009 && monster.getId() != 8645066) {
                        if (!monster.getStats().getName().contains("세렌") && monster.getId() != 8880605) {
                           if (monster.getId() != 8880140 && monster.getId() != 8880141 && monster.getId() != 8880143 && monster.getId() != 8880150 && monster.getId() != 8880151 && monster.getId() != 8880153 && monster.getId() != 8880155 && monster.getId() != 8880166 && monster.getId() != 8880158) {
                              if (monster.getId() == 8880302 || monster.getId() == 8880342) {
                                 monster.setSchedule(Timer.MobTimer.getInstance().register(() -> {
                                    if ((monster.getEventInstance() == null || !monster.isAlive()) && this.getAllChracater().size() <= 0) {
                                       monster.getSchedule().cancel(true);
                                       monster.setSchedule((ScheduledFuture)null);
                                       monster.getMap().killMonster(monster);
                                    } else {
                                       MobSkill web = MobSkillFactory.getMobSkill(242, 13);

                                       for(int i = 0; i < Randomizer.rand(1, 2); ++i) {
                                          web.applyEffect(monster.getController(), monster, true, monster.isFacingLeft());
                                       }

                                    }
                                 }, 6000L));
                              }
                           } else {
                              MapleBossManager.LucidHandler(monster);
                           }
                        } else if (monster.getId() == 8880602) {
                           MapleBossManager.SerenHandler(monster);
                        } else {
                           var6 = monster.getSkills().iterator();

                           while(var6.hasNext()) {
                              mobSkill = (MobSkill)var6.next();
                              monster.setLastSkillUsed(mobSkill, System.currentTimeMillis(), 5000L);
                           }

                           Timer.MapTimer.getInstance().schedule(() -> {
                              MapleBossManager.SerenHandler(monster);
                           }, 5000L);
                        }
                     } else {
                        Timer.MapTimer.getInstance().schedule(() -> {
                           MapleBossManager.dunkelHandler(monster, this);
                        }, 2000L);
                     }
                  } else {
                     monster.setCustomInfo(100023, 1, 0);
                     this.setCustomInfo(8644650, 0, 55000);
                     MapleBossManager.duskHandler(monster, this);
                  }
               } else {
                  int time = 28000;
                  if (monster.getId() != 8880101 && monster.getId() != 8880111) {
                     MapleFlyingSword mapleFlyingSword = new MapleFlyingSword(0, monster);
                     monster.getMap().spawnFlyingSword(mapleFlyingSword);
                     monster.getMap().setNewFlyingSwordNode(mapleFlyingSword, monster.getTruePosition());
                  } else {
                     monster.setHp((long)((double)monster.getHp() * 0.3D));
                     this.broadcastMessage(MobPacket.CorruptionChange((byte)0, this.getStigmaDeath()));
                     mapleMonster = MapleLifeFactory.getMonster(8880102);
                     mapleMonster.getStats().setSpeed(80);
                     this.spawnMonsterWithEffect(mapleMonster, MobSkillFactory.getMobSkill(201, 182).getSpawnEffect(), monster.getPosition());
                     time = 18000;
                  }

                  this.broadcastMessage(CField.StigmaTime(28000));
                  this.broadcastMessage(CField.enforceMSG("데미안이 누구에게 낙인을 새길지 알 수 없습니다.", 216, 30000000));
                  Timer.MapTimer.getInstance().schedule(() -> {
                     MapleBossManager.demianHandler(monster);
                  }, (long)time);
               }
            } else {
               ArrayList stats = new ArrayList();
               Iterator var12 = monster.getSkills().iterator();

               while(var12.hasNext()) {
                  MobSkill sk = (MobSkill)var12.next();
                  if (sk.getSkillId() == 223) {
                     monster.setLastSkillUsed(sk, System.currentTimeMillis(), 13000L);
                     Timer.MobTimer.getInstance().schedule(() -> {
                        monster.setCustomInfo(2286, 0, 60000);
                        monster.setCustomInfo(22878, 0, Randomizer.rand(60000, 70000));
                        monster.setEnergyspeed(1);
                        monster.setEnergyleft(false);
                        monster.setEnergycount(45);
                        stats.add(new Pair(MonsterStatus.MS_Laser, new MonsterStatusEffect(223, (int)sk.getDuration())));
                        monster.applyMonsterBuff(this, stats, sk);
                     }, 3000L);
                     break;
                  }
               }

               monster.setSchedule(Timer.MapTimer.getInstance().register(() -> {
                  if ((monster.getEventInstance() == null || !monster.isAlive()) && this.getAllChracater().size() <= 0) {
                     monster.getSchedule().cancel(true);
                     monster.setSchedule((ScheduledFuture)null);
                     monster.getMap().killMonster(monster);
                  } else {
                     MapleBossManager.lotusHandler(monster);
                  }
               }, 4000L));
            }
         } else {
            monster.setSchedule(Timer.MobTimer.getInstance().register(() -> {
               if (monster.getMap().getAllChracater().size() <= 0) {
                  monster.getSchedule().cancel(true);
                  monster.setSchedule((ScheduledFuture)null);
                  monster.getMap().killMonsterType(monster, 0);
               } else {
                  MapleBossManager.magnusHandler(monster, 0, 0);
               }
            }, 3000L));
         }

         MobSkill msi;
         if (monster.getId() == 8880400 || monster.getId() == 8880405 || monster.getId() == 8880415) {
            msi = MobSkillFactory.getMobSkill(247, 1);
            monster.setCustomInfo(24701, 1, 0);
            msi.applyEffect(monster.getController(), monster, true, true);
            monster.setLastSkillUsed(msi, System.currentTimeMillis(), 99999000L);
            MapleBossManager.JinHillaGlassTime(monster, 150);
         }

         if (monster.getId() == 8880500) {
            monster.setLastSkillUsed(MobSkillFactory.getMobSkill(170, 62), System.currentTimeMillis(), 40000L);
         }

         if (monster.getId() == 8880501) {
            monster.setLastSkillUsed(MobSkillFactory.getMobSkill(170, 64), System.currentTimeMillis(), 40000L);
         }

         if (monster.getId() == 8930000) {
            msi = MobSkillFactory.getMobSkill(170, 13);
            msi.setHp(40);
         }

         if (monster.getId() == 8644658 || monster.getId() == 8644659) {
            monster.setCustomInfo(100020, 0, 12000);
            monster.setCustomInfo(100021, 0, 7000);
         }

         if (monster.getId() >= 9833935 && monster.getId() <= 9833946) {
            monster.setCustomInfo(monster.getId(), monster.getId() == 9833946 ? 250 : (monster.getId() != 9833944 && monster.getId() != 9833945 ? 50 : 100), 0);
         }

         if (monster.getId() >= 9833947 && monster.getId() <= 9833958) {
            monster.setCustomInfo(monster.getId(), 10, 0);
         }

         if (monster.getId() >= 9833070 && monster.getId() <= 9833074) {
            Timer.MapTimer.getInstance().schedule(() -> {
               MapleCharacter player;
               if (monster != null && monster.isAlive() && !this.getAllCharactersThreadsafe().isEmpty() && (player = (MapleCharacter)this.getAllCharactersThreadsafe().get(0)) != null) {
                  Point pos = monster.getTruePosition();
                  this.killMonster(monster, player, false, false, (byte)1);
                  MapleMonster mob2 = MapleLifeFactory.getMonster(monster.getId() + 10);
                  mob2.setHp(GameConstants.getDreamBreakerHP((int)player.getKeyValue(15901, "stage")));
                  this.spawnMonsterOnGroundBelow(mob2, pos);
               }

            }, 35000L);
         }
      }

   }

   public final int spawnMonsterWithEffect(final MapleMonster monster, final int effect, Point pos) {
      try {
         monster.setMap(this);
         monster.setPosition(pos);
         if (monster.getHp() <= 0L) {
            MapleMonster mob = MapleLifeFactory.getMonster(monster.getId());
            if (mob != null) {
               monster.setHp(mob.getStats().getHp());
               monster.getStats().setHp(mob.getStats().getHp());
            }
         }

         this.spawnAndAddRangedMapObject(monster, new MapleMap.DelayedPacketCreation() {
            public final void sendPackets(MapleClient c) {
               c.getSession().writeAndFlush(MobPacket.spawnMonster(monster, effect, 0));
            }
         });
         this.updateMonsterController(monster);
         this.spawnedMonstersOnMap.incrementAndGet();
         if (monster.getSeperateSoul() > 0) {
            return 0;
         } else {
            if (monster.getId() >= 8900000 && monster.getId() <= 8900002 || monster.getId() >= 8900100 && monster.getId() <= 8900103) {
               MapleBossManager.pierreHandler(monster);
               if (monster.getId() == 8900002 || monster.getId() == 8900102) {
                  Iterator<MapleCharacter> iterator = this.getAllChracater().iterator();
                  if (iterator.hasNext()) {
                     MapleCharacter chr = (MapleCharacter)iterator.next();
                     MapleCharacter chrs = chr.getClient().getRandomCharacter();
                     monster.switchController(chrs, true);
                     String var10001 = chrs.getName();
                     monster.setSpecialtxt(var10001.makeConcatWithConstants<invokedynamic>(var10001));
                     String name = monster.getId() == 8900102 ? "피에르" : "카오스 피에르";
                     chrs.getMap().broadcastMessage(CWvsContext.getTopMsg(name + "가 [" + chrs.getName() + "]를 추격합니다."));
                     this.broadcastMessage(MobPacket.ShowPierreEffect(chrs, monster));
                  }
               }
            } else if (monster.getId() == 8910000) {
               monster.setSchedule(Timer.MapTimer.getInstance().register(() -> {
                  if (monster.getHPPercent() <= 10) {
                     List<Obstacle> obs = new ArrayList();

                     for(int i = 0; i < Randomizer.rand(4, 7); ++i) {
                        int key = Randomizer.rand(1, 6) + 21;
                        int x = Randomizer.rand(1, 1920) - 1140;
                        Obstacle ob = new Obstacle(key, new Point(x, -210), new Point(x, 820), 25, key != 22 && key != 25 ? (key != 23 && key != 26 ? 33 : 50) : 100, Randomizer.rand(1100, 1500), Randomizer.rand(0, 128), 3, 653);
                        obs.add(ob);
                     }

                     monster.getMap().broadcastMessage(MobPacket.createObstacle(monster, obs, (byte)0));
                  }

               }, 3000L));
            } else if (monster.getId() == 8880102) {
               int[] demians = new int[]{8880101, 8880111};
               MapleMonster demian = null;
               int[] var6 = demians;
               int var7 = demians.length;

               for(int var8 = 0; var8 < var7; ++var8) {
                  int ids = var6[var8];
                  demian = this.getMonsterById(ids);
                  if (demian != null) {
                     break;
                  }
               }

               if (demian != null) {
                  this.broadcastMessage(MobPacket.DemianTranscendenTalSet(demian, monster));
                  this.broadcastMessage(MobPacket.DemianTranscendenTalSet2(monster));
               }
            }

            return monster.getObjectId();
         }
      } catch (Exception var10) {
         return -1;
      }
   }

   public final void spawnFakeMonster(final MapleMonster monster) {
      monster.setMap(this);
      monster.setFake(true);
      if (monster.getHp() <= 0L) {
         MapleMonster mob = MapleLifeFactory.getMonster(monster.getId());
         if (mob != null) {
            monster.setHp(mob.getStats().getHp());
            monster.getStats().setHp(mob.getStats().getHp());
         }
      }

      this.spawnAndAddRangedMapObject(monster, new MapleMap.DelayedPacketCreation() {
         public final void sendPackets(MapleClient c) {
            c.getSession().writeAndFlush(MobPacket.spawnMonster(monster, -4, 0));
         }
      });
      this.updateMonsterController(monster);
      this.spawnedMonstersOnMap.incrementAndGet();
   }

   public final void spawnDelayedAttack(final MobSkill skill, final MapleDelayedAttack mda) {
      this.spawnAndAddRangedMapObject(mda, new MapleMap.DelayedPacketCreation() {
         public void sendPackets(MapleClient c) {
            c.getSession().writeAndFlush(MobPacket.onDemianDelayedAttackCreate(skill.getSkillId(), skill.getSkillLevel(), mda));
         }
      });
   }

   public final void spawnDelayedAttack(MapleMonster mob, MobSkill skill, List<MapleDelayedAttack> mda) {
      Iterator var4 = mda.iterator();

      while(var4.hasNext()) {
         MapleDelayedAttack att = (MapleDelayedAttack)var4.next();
         this.addMapObject(att);
      }

      this.broadcastMessage(MobPacket.onDemianDelayedAttackCreate(mob, skill.getSkillId(), skill.getSkillLevel(), mda));
   }

   public final void spawnMapleAtom(MapleAtom atom) {
      this.broadcastMessage(CField.createAtom(atom));
   }

   public final void spawnRune(final MapleRune rune) {
      rune.setMap(this);
      this.rune = rune;
      this.spawnAndAddRangedMapObject(rune, new MapleMap.DelayedPacketCreation() {
         public void sendPackets(MapleClient c) {
            c.getSession().writeAndFlush(CField.spawnRune(rune, false));
         }
      });
   }

   public final void spawnReactor(final MapleReactor reactor) {
      reactor.setMap(this);
      this.spawnAndAddRangedMapObject(reactor, new MapleMap.DelayedPacketCreation() {
         public final void sendPackets(MapleClient c) {
            c.getSession().writeAndFlush(CField.spawnReactor(reactor));
         }
      });
   }

   private void respawnReactor(MapleReactor reactor) {
      reactor.setState((byte)0);
      reactor.setAlive(true);
      this.spawnReactor(reactor);
   }

   public final void spawnDoor(final MapleDoor door) {
      this.spawnAndAddRangedMapObject(door, new MapleMap.DelayedPacketCreation() {
         public final void sendPackets(MapleClient c) {
            door.sendSpawnData(c);
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
         }
      });
   }

   public final void spawnMechDoor(MechDoor door) {
      this.spawnAndAddRangedMapObject(door, (c) -> {
         c.getSession().writeAndFlush(CField.spawnMechDoor(door, true));
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      });
      Timer.MapTimer tMan = Timer.MapTimer.getInstance();
      ScheduledFuture<?> schedule = null;
      door.setSchedule((ScheduledFuture)schedule);
      if (door.getDuration() > 0) {
         door.setSchedule(tMan.schedule(() -> {
            this.broadcastMessage(CField.removeMechDoor(door, true));
            this.removeMapObject(door);
         }, (long)door.getDuration()));
      }

   }

   public final void spawnSummon(MapleSummon summon) {
      summon.updateMap(this);
      this.spawnAndAddRangedMapObject(summon, (c) -> {
         if (summon != null && c.getPlayer() != null && (!summon.isChangedMap() || summon.getOwner().getId() == c.getPlayer().getId())) {
            c.getSession().writeAndFlush(CField.SummonPacket.spawnSummon(summon, true));
         }

      });
   }

   public final void spawnOrb(MapleOrb orb) {
      this.spawnAndAddRangedMapObject(orb, (c) -> {
         orb.sendSpawnData(c);
      });
   }

   public final void removeOrb(int playerId, MapleOrb orb) {
      if (orb != null) {
         this.removeMapObject(orb);
      }

      this.broadcastMessage(CField.removeOrb(playerId, Arrays.asList(orb)));
   }

   public final void spawnSpecialPortal(MapleCharacter chr, List<SpecialPortal> objects) {
      Iterator var3 = objects.iterator();

      while(var3.hasNext()) {
         SpecialPortal object = (SpecialPortal)var3.next();
         this.addMapObject(object);
         Timer.MapTimer.getInstance().schedule(() -> {
            this.removeSpecialPortal(chr, object);
         }, (long)object.getDuration());
      }

      this.broadcastMessage(CField.createSpecialPortal(chr.getId(), objects));
   }

   public final List<SpecialPortal> SpecialPortalSize(int ownerId) {
      List<SpecialPortal> ret = new ArrayList();
      Iterator var3 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.SPECIAL_PORTAL)).values().iterator();

      while(var3.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var3.next();
         SpecialPortal object = (SpecialPortal)mmo;
         if (object.getOwnerId() == ownerId) {
            ret.add(object);
         }
      }

      return ret;
   }

   public final void removeSpecialPortal(MapleCharacter chr, SpecialPortal object) {
      if (object != null) {
         this.removeMapObject(object);
         chr.addSkillCustomInfo(object.getSkillId(), -1L);
      }

      this.broadcastMessage(CField.removeSpecialPortal(chr.getId(), Arrays.asList(object)));
   }

   public final void removeSpecialPortal(MapleCharacter chr, List<SpecialPortal> lists) {
      Iterator var3 = lists.iterator();

      while(var3.hasNext()) {
         SpecialPortal object = (SpecialPortal)var3.next();
         this.removeMapObject(object);
      }

      this.broadcastMessage(CField.removeSpecialPortal(chr.getId(), lists));
   }

   public final void spawnSecondAtom(MapleCharacter chr, List<SecondAtom> tiles, int spawnType) {
      Iterator var4 = tiles.iterator();

      while(var4.hasNext()) {
         SecondAtom tile = (SecondAtom)var4.next();
         this.addMapObject(tile);
      }

      this.broadcastMessage(CField.spawnSecondAtoms(chr.getId(), tiles, spawnType));
   }

   public final void spawnSecondAtom(MapleCharacter chr, MapleSecondAtom atom) {
      List<MapleSecondAtom> at = Arrays.asList(atom);
      this.spawnAndAddRangedMapObject(atom, (c) -> {
         c.getSession().writeAndFlush(SkillPacket.createSecondAtom(at));
      });
   }

   public final void spawnSecondAtom(MapleCharacter chr, MapleSecondAtom atom, boolean left) {
      List<MapleSecondAtom> at = Arrays.asList(atom);
      this.spawnAndAddRangedMapObject(atom, (c) -> {
         c.getSession().writeAndFlush(SkillPacket.createSecondAtom(at, left));
      });
   }

   public List<MapleSecondAtom> getAllSecondAtoms() {
      ArrayList<MapleSecondAtom> ret = new ArrayList();
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.SECOND_ATOM)).values().iterator();

      while(var2.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var2.next();
         ret.add((MapleSecondAtom)mmo);
      }

      return ret;
   }

   public MapleSecondAtom getFindSecondAtoms(int objectid) {
      MapleSecondAtom ret = null;
      Iterator var3 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.SECOND_ATOM)).values().iterator();

      while(var3.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var3.next();
         if (objectid == mmo.getObjectId()) {
            ret = (MapleSecondAtom)mmo;
            break;
         }
      }

      return ret;
   }

   public final void removeSecondAtom(MapleCharacter chr, int objectId) {
      MapleMapObject Aobject = this.getMapObject(objectId, MapleMapObjectType.A_SECOND_ATOM);
      if (Aobject != null) {
         SecondAtom sa = chr.getMap().getSecondAtomOid(chr.getId(), objectId);
         if (sa != null) {
            if (sa.getSkillId() == 162111002 && chr.getBuffedValue(162111002)) {
               return;
            }

            this.removeMapObject(Aobject);
         }
      } else {
         MapleMapObject object = this.getMapObject(objectId, MapleMapObjectType.SECOND_ATOM);
         if (object != null) {
            MapleSecondAtom remover = chr.getMap().getFindSecondAtoms(objectId);
            if (remover != null) {
               if (GameConstants.isDarkKnight(chr.getJob())) {
                  if (chr.getBuffedValue(400011047)) {
                     int healhp = (int)(chr.getStat().getCurrentMaxHp() / 100L * (long)chr.getBuffedEffect(400011047).getX());
                     int makeshiled = healhp / 100 * chr.getBuffedEffect(400011047).getV();
                     int maxshiled = (int)(chr.getStat().getCurrentMaxHp() / 100L * (long)chr.getBuffedEffect(400011047).getY());
                     chr.setSkillCustomInfo(400011047, chr.getSkillCustomValue0(400011047) + 1L, 0L);
                     if (chr.getSkillCustomValue0(400011047) > (long)chr.getBuffedEffect(400011047).getS()) {
                        chr.setSkillCustomInfo(400011047, (long)chr.getBuffedEffect(400011047).getS(), 0L);
                     }

                     if (chr.getStat().getHPPercent() == 100) {
                        chr.setSkillCustomInfo(400011048, chr.getSkillCustomValue0(400011048) + (long)makeshiled, 0L);
                        if (chr.getSkillCustomValue0(400011048) > (long)maxshiled) {
                           chr.setSkillCustomInfo(400011048, (long)maxshiled, 0L);
                        }
                     } else {
                        chr.addHP((long)healhp);
                     }

                     chr.getBuffedEffect(400011047).applyTo(chr, false);
                  }
               } else if (GameConstants.isFlameWizard(chr.getJob())) {
                  if (remover.getSecondAtoms().getSourceId() == 400021092) {
                     MapleSummon sum = chr.getSummon(400021092);
                     if (sum != null) {
                        chr.getMap().broadcastMessage(CField.SummonPacket.updateSummon(sum, 0));
                        chr.removeSkillCustomInfo(400021092);
                     }
                  }
               } else if (GameConstants.isCain(chr.getJob()) && remover.getSecondAtoms().getSourceId() == 63101006) {
                  chr.addSkillCustomInfo(63101006, -1L);
               }

               this.removeMapObject(object);
               if (remover.getSchedule() != null) {
                  remover.getSchedule().cancel(true);
                  remover.setSchedule((ScheduledFuture)null);
               }
            }
         } else if (GameConstants.isMechanic(chr.getJob())) {
            chr.setSkillCustomInfo(400051069, chr.getSkillCustomValue0(400051069) + 1L, 0L);
            if (chr.getBuffedValue(400051068) && chr.getSkillCustomValue0(400051068) == chr.getSkillCustomValue0(400051069)) {
               chr.setSkillCustomInfo(400051068, chr.getSkillCustomValue0(400051068) + (long)chr.getBuffedEffect(400051068).getY(), 0L);
               chr.MechCarrier(1000, true);
            }
         }
      }

      this.broadcastMessage(CField.removeSecondAtom(chr.getId(), objectId));
   }

   public final void spawnRandomPortal(MapleRandomPortal portal) {
   }

   public final void spawnSpiderWeb(final SpiderWeb web) {
      this.spawnAndAddRangedMapObject(web, new MapleMap.DelayedPacketCreation() {
         public void sendPackets(MapleClient c) {
            web.sendSpawnData(c);
         }
      });
   }

   public final void spawnSummon(final MapleSummon summon, final int duration) {
      summon.updateMap(this);
      summon.setStartTime(System.currentTimeMillis());
      this.spawnAndAddRangedMapObject(summon, new MapleMap.DelayedPacketCreation() {
         public void sendPackets(MapleClient c) {
            if (summon != null && c.getPlayer() != null && (!summon.isChangedMap() || summon.getOwner().getId() == c.getPlayer().getId())) {
               c.getSession().writeAndFlush(CField.SummonPacket.spawnSummon(summon, true, duration));
            }

         }
      });
      if (summon.getSkill() == 152101000) {
         if (summon.getOwner().getSkillCustomValue0(152101000) > 0L) {
            summon.setEnergy((int)summon.getOwner().getSkillCustomValue0(152101000));
         }

         summon.getOwner().getClient().getSession().writeAndFlush(CField.SummonPacket.transformSummon(summon, 2));
      }

      if (duration > 0 && duration < Integer.MAX_VALUE && summon.getSkill() != 400051046 && summon.getSummonType() != 7 || summon.getSkill() == 400021068 && summon.getMovementType() != SummonMovementType.SUMMON_JAGUAR) {
         Timer.MapTimer.getInstance().schedule(new Runnable() {
            public void run() {
               summon.removeSummon(MapleMap.this, false, false);
            }
         }, (long)(duration + 1000));
      }

   }

   public final void spawnSummon(final MapleSummon summon, final int duration, final MapleCharacter chr) {
      summon.updateMap(this);
      summon.setStartTime(System.currentTimeMillis());
      this.spawnAndAddRangedMapObject(summon, new MapleMap.DelayedPacketCreation() {
         public void sendPackets(MapleClient c) {
            if (summon != null && c.getPlayer() != null && (!summon.isChangedMap() || summon.getOwner().getId() == c.getPlayer().getId())) {
               c.getSession().writeAndFlush(CField.SummonPacket.spawnSummon(summon, true, duration));
            }

         }
      });
      if (summon.getSkill() == 152101000) {
         if (summon.getOwner().getSkillCustomValue0(152101000) > 0L) {
            summon.setEnergy((int)summon.getOwner().getSkillCustomValue0(152101000));
         }

         summon.getOwner().getClient().getSession().writeAndFlush(CField.SummonPacket.transformSummon(summon, 2));
      }

      if (duration > 0 && duration < Integer.MAX_VALUE && summon.getSkill() != 400051046 && summon.getSummonType() != 7 || summon.getSkill() == 400021068 && summon.getMovementType() != SummonMovementType.SUMMON_JAGUAR) {
         Timer.MapTimer.getInstance().schedule(new Runnable() {
            public void run() {
               summon.removeSummon(MapleMap.this, false, false, chr);
            }
         }, (long)(duration + 1000));
      }

   }

   public final void spawnExtractor(final MapleExtractor ex) {
      this.spawnAndAddRangedMapObject(ex, new MapleMap.DelayedPacketCreation() {
         public void sendPackets(MapleClient c) {
            ex.sendSpawnData(c);
         }
      });
   }

   public final void spawnSpecialChair(final MapleSpecialChair ex) {
      this.spawnAndAddRangedMapObject(ex, new MapleMap.DelayedPacketCreation() {
         public void sendPackets(MapleClient c) {
            ex.sendSpawnData(c);
         }
      });
   }

   public final void spawnMagicWreck(final MapleMagicWreck mw) {
      final List<MapleMagicWreck> mws = new ArrayList();
      this.getWrecks().add(mw);
      this.spawnAndAddRangedMapObject(mw, new MapleMap.DelayedPacketCreation() {
         public void sendPackets(MapleClient c) {
            mw.sendSpawnData(c);
         }
      });
      Timer.MapTimer tMan = Timer.MapTimer.getInstance();
      ScheduledFuture<?> schedule = null;
      mw.setSchedule((ScheduledFuture)schedule);
      if (mw.getDuration() > 0) {
         mw.setSchedule(tMan.schedule(new Runnable() {
            public void run() {
               MapleMap.this.broadcastMessage(CField.removeMagicWreck(mw.getChr(), mws));
               MapleMap.this.removeMapObject(mw);
               MapleMap.this.getWrecks().remove(mw);
            }
         }, (long)mw.getDuration()));
      }

   }

   public final void RemoveMagicWreck(MapleMagicWreck mw) {
      List<MapleMagicWreck> mws = new ArrayList();
      mws.add(mw);
      this.getWrecks().remove(mw);
      this.broadcastMessage(CField.removeMagicWreck(mw.getChr(), mws));
      this.removeMapObject(mw);
   }

   public final void spawnFlyingSword(final MapleFlyingSword mfs) {
      this.spawnAndAddRangedMapObject(mfs, new MapleMap.DelayedPacketCreation() {
         public void sendPackets(MapleClient c) {
            mfs.sendSpawnData(c);
         }
      });
   }

   public final void removeAllFlyingSword() {
      Iterator var1 = this.getAllFlyingSwordsThreadsafe().iterator();

      while(var1.hasNext()) {
         MapleFlyingSword sword = (MapleFlyingSword)var1.next();
         this.broadcastMessage(MobPacket.FlyingSword(sword, false));
         this.removeMapObject(sword);
      }

   }

   public final void setNewFlyingSwordNode(final MapleFlyingSword mfs, Point point) {
      FlyingSwordNode msn = new FlyingSwordNode(1, 0, 0, 30, 0, 0, 0, false, 0, new Point(point.x, -180));
      List<FlyingSwordNode> nodes = new ArrayList();
      nodes.add(msn);
      mfs.setNodes(nodes);
      this.broadcastMessage(MobPacket.FlyingSwordNode(mfs));
      mfs.updateTarget(this);
      Timer.MapTimer.getInstance().schedule(new Runnable() {
         public void run() {
            if (mfs != null) {
               mfs.updateFlyingSwordNode(MapleMap.this);
            }

         }
      }, 3500L);
   }

   public final void spawnIncinerateObject(final MapleIncinerateObject mio) {
      this.spawnAndAddRangedMapObject(mio, new MapleMap.DelayedPacketCreation() {
         public void sendPackets(MapleClient c) {
            mio.sendSpawnData(c);
         }
      });
      Timer.MapTimer tMan = Timer.MapTimer.getInstance();
      mio.setSchedule(tMan.schedule(new Runnable() {
         public void run() {
            MapleMap.this.broadcastMessage(MobPacket.incinerateObject(mio, false));
            MapleMap.this.removeMapObject(mio);
         }
      }, 10000L));
   }

   public final void spawnFieldAttackObj(final MapleFieldAttackObj fao) {
      this.spawnAndAddRangedMapObject(fao, new MapleMap.DelayedPacketCreation() {
         public void sendPackets(MapleClient c) {
            fao.sendSpawnData(c);
            fao.onSetAttack(c);
         }
      });
      Timer.MapTimer tMan = Timer.MapTimer.getInstance();
      ScheduledFuture<?> schedule = null;
      fao.setSchedule((ScheduledFuture)schedule);
      if (fao.getDuration() > 0) {
         fao.setSchedule(tMan.schedule(new Runnable() {
            public void run() {
               MapleMap.this.broadcastMessage(CField.AttackObjPacket.ObjRemovePacketByOid(fao.getObjectId()));
               MapleMap.this.removeMapObject(fao);
            }
         }, (long)fao.getDuration()));
      }

   }

   public void spawnEnergySphere(final int objectId, final int skillLevel, final MapleEnergySphere sp) {
      this.spawnAndAddRangedMapObject(sp, new MapleMap.DelayedPacketCreation() {
         public void sendPackets(MapleClient c) {
            MapleMap.this.broadcastMessage(MobPacket.createEnergySphere(objectId, skillLevel, sp));
         }
      });
   }

   public final void spawnAdelProjectile(MapleCharacter chr, List<AdelProjectile> tiles, boolean infinity) {
      Iterator var4 = tiles.iterator();

      while(var4.hasNext()) {
         AdelProjectile tile = (AdelProjectile)var4.next();
         this.addMapObject(tile);
         Iterator var6 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.ADEL_PROJECTILE)).values().iterator();

         while(var6.hasNext()) {
            MapleMapObject mmo = (MapleMapObject)var6.next();
            if (tile.getObjectId() == mmo.getObjectId()) {
               chr.object.add(new Pair(tile.getSkillId(), tile.getObjectId()));
            }
         }
      }

      this.broadcastMessage(CField.spawnAdelProjectiles(chr, tiles, infinity));
   }

   public void spawnEnergySphereTimer(int objectId, int skillLevel, MapleEnergySphere sp, int time) {
      Timer.MapTimer.getInstance().schedule(() -> {
         this.spawnAndAddRangedMapObject(sp, new MapleMap.DelayedPacketCreation() {
            public void sendPackets(MapleClient c) {
               MapleMap.this.broadcastMessage(MobPacket.createEnergySphere(objectId, skillLevel, sp));
            }
         });
      }, (long)time);
   }

   public void spawnEnergySphereListTimer(int objectId, int skillLevel, List<MapleEnergySphere> sps, int time) {
      Timer.MapTimer.getInstance().schedule(() -> {
         this.broadcastMessage(MobPacket.createEnergySphere(objectId, skillLevel, sps));
      }, (long)time);
   }

   public final void spawnMist(final MapleMist mist, boolean fake) {
      this.spawnAndAddRangedMapObject(mist, new MapleMap.DelayedPacketCreation() {
         public void sendPackets(MapleClient c) {
            mist.sendSpawnData(c);
         }
      });
      if (mist.getStartTime() == 0L) {
         mist.setStartTime(System.currentTimeMillis());
      }

   }

   public final MapleEnergySphere getEnergySphere(int objid) {
      Iterator var2 = this.getAllEnergySphere().iterator();

      MapleEnergySphere mse;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         mse = (MapleEnergySphere)var2.next();
      } while(mse.getObjectId() != objid);

      return mse;
   }

   public final MapleFlyingSword getFlyingSword(int objid) {
      Iterator var2 = this.getAllFlyingSwordsThreadsafe().iterator();

      MapleFlyingSword mfs;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         mfs = (MapleFlyingSword)var2.next();
      } while(mfs.getObjectId() != objid);

      return mfs;
   }

   public final MapleMist getMist(int ownerId, int skillId) {
      Iterator var3 = this.getAllMistsThreadsafe().iterator();

      MapleMist mist;
      do {
         if (!var3.hasNext()) {
            return null;
         }

         mist = (MapleMist)var3.next();
      } while(mist.getSourceSkill() == null || mist.getSourceSkill().getId() != skillId || mist.getOwnerId() != ownerId);

      return mist;
   }

   public final void removeMist(int skillid) {
      Iterator var2 = this.getAllMistsThreadsafe().iterator();

      while(var2.hasNext()) {
         MapleMist mist = (MapleMist)var2.next();
         if (mist.getSourceSkill() != null && mist.getSourceSkill().getId() == skillid) {
            this.broadcastMessage(CField.removeMist(mist));
            this.removeMapObject(mist);
         }
      }

   }

   public final void removeMistByOwner(MapleCharacter chr, int skillid) {
      Iterator var3 = this.getAllMistsThreadsafe().iterator();

      while(var3.hasNext()) {
         MapleMist mist = (MapleMist)var3.next();
         if (mist.getSourceSkill() != null && mist.getOwnerId() == chr.getId() && mist.getSourceSkill().getId() == skillid) {
            this.broadcastMessage(CField.removeMist(mist));
            this.removeMapObject(mist);
         }
      }

   }

   public final void removeMist(MapleMist mist) {
      if (this.getMapObject(mist.getObjectId(), MapleMapObjectType.MIST) != null) {
         this.broadcastMessage(CField.removeMist(mist));
         this.removeMapObject(mist);
      }

   }

   public final void disappearingItemDrop(MapleMapObject dropper, MapleCharacter owner, Item item, Point pos) {
      Point droppos = this.calcDropPos(pos, pos);
      MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner, (byte)1, false);
      this.broadcastMessage(CField.dropItemFromMapObject(this, drop, dropper.getTruePosition(), droppos, (byte)3, owner.getBuffedEffect(SecondaryStat.PickPocket) != null), drop.getTruePosition());
   }

   public final void spawnMesoDrop(int meso, Point position, MapleMapObject dropper, MapleCharacter owner, boolean playerDrop, byte droptype) {
      this.spawnMesoDrop(meso, position, dropper, owner, playerDrop, droptype, 0);
   }

   public final void spawnMesoDrop(int meso, Point position, final MapleMapObject dropper, final MapleCharacter owner, boolean playerDrop, byte droptype, final int delay) {
      final Point droppos = this.calcDropPos(position, position);
      final MapleMapItem mdrop = new MapleMapItem(meso, droppos, dropper, owner, droptype, playerDrop);
      if (delay > 0 && owner.getBuffedEffect(SecondaryStat.PickPocket) != null) {
         int max = SkillFactory.getSkill(4211006).getEffect(owner.getSkillLevel(4211006)).getBulletCount();
         if (owner.getSkillLevel(4220045) > 0) {
            max += SkillFactory.getSkill(4220045).getEffect(owner.getSkillLevel(4220045)).getBulletCount();
         }

         if (owner.getPickPocket().size() < max) {
            mdrop.setPickpoket(true);
            owner.addPickPocket(mdrop);
         }
      }

      this.spawnAndAddRangedMapObject(mdrop, new MapleMap.DelayedPacketCreation() {
         public void sendPackets(MapleClient c) {
            c.getSession().writeAndFlush(CField.dropItemFromMapObject(owner.getMap(), mdrop, dropper.getTruePosition(), droppos, (byte)1, owner.getBuffedEffect(SecondaryStat.PickPocket) != null, delay, (byte)(owner.getBuffedValue(4221018) ? 4 : 0)));
         }
      });
      if (!this.everlast) {
         mdrop.registerExpire(120000L);
         if (droptype == 0 || droptype == 1) {
            mdrop.registerFFA(30000L);
         }
      }

   }

   public final void spawnMobMesoDrop(int meso, final Point position, final MapleMapObject dropper, final MapleCharacter owner, boolean playerDrop, byte droptype) {
      final MapleMapItem mdrop = new MapleMapItem(meso, position, dropper, owner, droptype, playerDrop);
      this.spawnAndAddRangedMapObject(mdrop, new MapleMap.DelayedPacketCreation() {
         public void sendPackets(MapleClient c) {
            c.getSession().writeAndFlush(CField.dropItemFromMapObject(owner.getMap(), mdrop, dropper.getTruePosition(), position, (byte)1, owner.getBuffedEffect(SecondaryStat.PickPocket) != null));
         }
      });
      boolean magnetpet = false;

      for(int i = 0; i < owner.getPets().length; ++i) {
         if (owner.getPets()[i] != null && (owner.getPets()[i].getPetItemId() == 5000930 || owner.getPets()[i].getPetItemId() == 5000931 || owner.getPets()[i].getPetItemId() == 5000932)) {
            magnetpet = true;
         }
      }

      mdrop.registerExpire(120000L);
      if (droptype == 0 || droptype == 1) {
         mdrop.registerFFA(30000L);
      }

   }

   public final void spawnFlyingDrop(final MapleCharacter chr, final Point startPos, final Point dropPos, Item idrop) {
      final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, (MapleMapObject)null, chr, (byte)2, false, 0);
      mdrop.setFlyingSpeed(Randomizer.rand(50, 150));
      mdrop.setFlyingAngle(Randomizer.rand(55, 199));
      mdrop.setFlyingDrop(true);
      mdrop.setTouchDrop(true);
      this.spawnAndAddRangedMapObject(mdrop, new MapleMap.DelayedPacketCreation() {
         public void sendPackets(MapleClient c) {
            c.getSession().writeAndFlush(CField.dropItemFromMapObject(chr.getMap(), mdrop, startPos, dropPos, (byte)1, false));
         }
      });
      mdrop.registerExpire(120000L);
      this.activateItemReactors(mdrop, chr.getClient());
   }

   public final void spawnMobFlyingDrop(final MapleCharacter chr, final MapleMonster mob, final Point dropPos, Item idrop) {
      final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, mob, chr, (byte)2, false, 0);
      mdrop.setFlyingSpeed(150);
      mdrop.setFlyingAngle(Randomizer.rand(55, 199));
      mdrop.setTouchDrop(true);
      this.spawnAndAddRangedMapObject(mdrop, new MapleMap.DelayedPacketCreation() {
         public void sendPackets(MapleClient c) {
            c.getSession().writeAndFlush(CField.dropItemFromMapObject(chr.getMap(), mdrop, mob.getTruePosition(), dropPos, (byte)1, false));
         }
      });
      mdrop.registerExpire(120000L);
      this.activateItemReactors(mdrop, chr.getClient());
   }

   public final void spawnMobDrop(Item idrop, final Point dropPos, final MapleMonster mob, final MapleCharacter chr, byte droptype, final int questid) {
      final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, mob, chr, droptype, false, questid);
      if (idrop.getItemId() / 1000000 == 1 && !GameConstants.isArcaneSymbol(idrop.getItemId()) && !GameConstants.isAuthenticSymbol(idrop.getItemId()) && idrop.getItemId() / 1000 != 1162 && idrop.getItemId() / 1000 != 1182) {
         Equip eq = (Equip)idrop;
         List<Pair<Integer, Integer>> random = new ArrayList();
         random.add(new Pair(1, 6000));
         random.add(new Pair(2, 1950));
         random.add(new Pair(3, 50));
         random.add(new Pair(4, 2000));
         int type = GameConstants.getWeightedRandom(random);
         if (type != 4) {
            eq.setState((byte)type);
            eq.setLines((byte)(Randomizer.isSuccess(80) ? 2 : 3));
            mdrop.setEquip(eq);
         }
      }

      if (mdrop.getItemId() != 4001536) {
         this.spawnAndAddRangedMapObject(mdrop, new MapleMap.DelayedPacketCreation() {
            public void sendPackets(MapleClient c) {
               if (c != null && (questid <= 0 || c.getPlayer().getQuestStatus(questid) == 1) && mob != null && dropPos != null) {
                  c.getSession().writeAndFlush(CField.dropItemFromMapObject(chr.getMap(), mdrop, mob.getPosition(), dropPos, (byte)1, chr.getBuffedEffect(SecondaryStat.PickPocket) != null));
               }

            }
         });
      }

      mdrop.registerExpire(120000L);
      if (droptype == 0 || droptype == 1) {
         mdrop.registerFFA(30000L);
      }

      this.activateItemReactors(mdrop, chr.getClient());
   }

   public final void spawnMobPublicDrop(Item idrop, final Point dropPos, final MapleMonster mob, final MapleCharacter chr, byte droptype, final int questid) {
      final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, mob, chr, (byte)3, false, questid);
      switch(idrop.getItemId()) {
      case 2022570:
      case 2434851:
      case 4001847:
      case 4001849:
         mdrop.setTouchDrop(true);
         mdrop.setDropType((byte)0);
         break;
      case 2023484:
      case 2023494:
      case 2023495:
      case 2023669:
      case 2023927:
         mdrop.setTouchDrop(true);
      }

      if (idrop.getItemId() / 1000000 == 1 && !GameConstants.isArcaneSymbol(idrop.getItemId()) && !GameConstants.isAuthenticSymbol(idrop.getItemId()) && idrop.getItemId() / 1000 != 1162 && idrop.getItemId() / 1000 != 1182) {
         Equip eq = (Equip)idrop;
         List<Pair<Integer, Integer>> random = new ArrayList();
         random.add(new Pair(1, 6000));
         random.add(new Pair(2, 1950));
         random.add(new Pair(3, 50));
         random.add(new Pair(4, 2000));
         int type = GameConstants.getWeightedRandom(random);
         if (type != 4) {
            eq.setState((byte)type);
            eq.setLines((byte)(Randomizer.isSuccess(80) ? 2 : 3));
            mdrop.setEquip(eq);
         }
      }

      mdrop.setPublicDropId(chr.getClient().getAccID());
      if (mdrop.getItemId() != 4001536) {
         this.spawnAndAddRangedMapObject(mdrop, new MapleMap.DelayedPacketCreation() {
            public void sendPackets(MapleClient c) {
               if (c != null && (questid <= 0 || c.getPlayer().getQuestStatus(questid) == 1) && mob != null && dropPos != null && c.getAccID() == chr.getClient().getAccID()) {
                  c.getSession().writeAndFlush(CField.dropItemFromMapObject(chr.getMap(), mdrop, mob.getPosition(), dropPos, (byte)1, chr.getBuffedEffect(SecondaryStat.PickPocket) != null));
               }

            }
         });
      }

      mdrop.registerExpire(120000L);
      if (droptype == 0 || droptype == 1) {
         mdrop.registerFFA(30000L);
      }

      this.activateItemReactors(mdrop, chr.getClient());
   }

   public final void spawnRandDrop() {
      if (this.mapid == 910000000 && this.channel == 1) {
         Iterator var1 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.ITEM)).values().iterator();

         MapleMapObject o;
         do {
            if (!var1.hasNext()) {
               Timer.MapTimer.getInstance().schedule(new Runnable() {
                  public void run() {
                     Point pos = new Point(Randomizer.nextInt(800) + 531, -806);
                     int theItem = Randomizer.nextInt(1000);
                     int itemid = false;
                     int itemidx;
                     if (theItem < 950) {
                        itemidx = GameConstants.normalDrops[Randomizer.nextInt(GameConstants.normalDrops.length)];
                     } else if (theItem < 990) {
                        itemidx = GameConstants.rareDrops[Randomizer.nextInt(GameConstants.rareDrops.length)];
                     } else {
                        itemidx = GameConstants.superDrops[Randomizer.nextInt(GameConstants.superDrops.length)];
                     }

                     MapleMap.this.spawnAutoDrop(itemidx, pos);
                  }
               }, 20000L);
               return;
            }

            o = (MapleMapObject)var1.next();
         } while(!((MapleMapItem)o).isRandDrop());

      }
   }

   public final void spawnAutoDrop(int itemid, Point pos) {
      Item idrop = null;
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      if (GameConstants.getInventoryType(itemid) == MapleInventoryType.EQUIP) {
         idrop = ii.getEquipById(itemid);
      } else {
         idrop = new Item(itemid, (short)0, (short)1, 0);
      }

      idrop.setGMLog("Dropped from auto  on " + this.mapid);
      MapleMapItem mdrop = new MapleMapItem(pos, idrop);
      this.spawnAndAddRangedMapObject(mdrop, new MapleMap.DelayedPacketCreation() {
         public void sendPackets(MapleClient c) {
         }
      });
      if (itemid / 10000 != 291) {
         mdrop.registerExpire(120000L);
      }

   }

   public final void spawnItemDrop(final MapleMapObject dropper, final MapleCharacter owner, Item item, Point pos, boolean ffaDrop, boolean playerDrop) {
      final Point droppos = this.calcDropPos(pos, pos);
      Equip equip = null;
      if (item.getType() == 1) {
         equip = (Equip)item;
      }

      final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner, (byte)2, playerDrop, equip);
      if (item.getItemId() == 2434851 || item.getItemId() == 4001849 || item.getItemId() == 4001847 || item.getItemId() == 2023927 || item.getItemId() == 2022570) {
         drop.setTouchDrop(true);
      }

      try {
         this.spawnAndAddRangedMapObject(drop, new MapleMap.DelayedPacketCreation() {
            public void sendPackets(MapleClient c) {
               c.getSession().writeAndFlush(CField.dropItemFromMapObject(owner.getMap(), drop, dropper.getTruePosition(), droppos, (byte)1, false));
            }
         });
         this.broadcastMessage(CField.dropItemFromMapObject(owner.getMap(), drop, dropper.getTruePosition(), droppos, (byte)0, false));
         if (!this.everlast) {
            drop.registerExpire(120000L);
            this.activateItemReactors(drop, owner.getClient());
         }
      } catch (Exception var11) {
         var11.printStackTrace();
      }

   }

   private void activateItemReactors(MapleMapItem drop, MapleClient c) {
      Item item = drop.getItem();
      Iterator var4 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.REACTOR)).values().iterator();

      while(var4.hasNext()) {
         MapleMapObject o = (MapleMapObject)var4.next();
         MapleReactor react = (MapleReactor)o;
         if (react.getReactorType() == 100 && item.getItemId() == GameConstants.getCustomReactItem(react.getReactorId(), (Integer)react.getReactItem().getLeft()) && (Integer)react.getReactItem().getRight() == item.getQuantity() && react.getArea().contains(drop.getTruePosition()) && !react.isTimerActive()) {
            Timer.MapTimer.getInstance().schedule(new MapleMap.ActivateItemReactor(drop, react, c), 5000L);
            react.setTimerActive(true);
            break;
         }
      }

   }

   public int getItemsSize() {
      return ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.ITEM)).size();
   }

   public int getExtractorSize() {
      return ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.EXTRACTOR)).size();
   }

   public List<MapleMapItem> getAllItems() {
      return this.getAllItemsThreadsafe();
   }

   public List<MapleMapItem> getAllItemsThreadsafe() {
      ArrayList<MapleMapItem> ret = new ArrayList();
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.ITEM)).values().iterator();

      while(var2.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var2.next();
         ret.add((MapleMapItem)mmo);
      }

      return ret;
   }

   public Point getPointOfItem(int itemid) {
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.ITEM)).values().iterator();

      MapleMapItem mm;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         MapleMapObject mmo = (MapleMapObject)var2.next();
         mm = (MapleMapItem)mmo;
      } while(mm.getItem() == null || mm.getItem().getItemId() != itemid);

      return mm.getPosition();
   }

   public List<MapleEnergySphere> getAllEnergySphere() {
      ArrayList<MapleEnergySphere> ret = new ArrayList();
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.ENERGY)).values().iterator();

      while(var2.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var2.next();
         ret.add((MapleEnergySphere)mmo);
      }

      return ret;
   }

   public List<MapleMist> getAllMistsThreadsafe() {
      ArrayList<MapleMist> ret = new ArrayList();
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.MIST)).values().iterator();

      while(var2.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var2.next();
         ret.add((MapleMist)mmo);
      }

      return ret;
   }

   public final void returnEverLastItem(MapleCharacter chr) {
      Iterator var2 = this.getAllItemsThreadsafe().iterator();

      while(var2.hasNext()) {
         MapleMapObject o = (MapleMapObject)var2.next();
         MapleMapItem item = (MapleMapItem)o;
         if (item.getOwner() == chr.getId()) {
            item.setPickedUp(true);
            this.broadcastMessage(CField.removeItemFromMap(item.getObjectId(), 2, chr.getId()), item.getTruePosition());
            if (item.getMeso() > 0) {
               chr.gainMeso((long)item.getMeso(), false);
            } else {
               MapleInventoryManipulator.addFromDrop(chr.getClient(), item.getItem(), false);
            }

            this.removeMapObject(item);
         }
      }

      this.spawnRandDrop();
   }

   public final void talkMonster(String msg, int itemId, int objectid) {
      if (itemId > 0) {
         this.startMapEffect(msg, itemId, false);
      }

      this.broadcastMessage(MobPacket.talkMonster(objectid, itemId, msg));
      this.broadcastMessage(MobPacket.removeTalkMonster(objectid));
   }

   public final void startMapEffect(String msg, int itemId) {
      this.startMapEffect(msg, itemId, false, 30000);
   }

   public final void startMapEffect(String msg, int itemId, boolean jukebox) {
      this.startMapEffect(msg, itemId, jukebox, 30000);
   }

   public final void startMapEffect(String msg, int itemId, int time) {
      this.startMapEffect(msg, itemId, false, time);
   }

   public final void startMapEffect(String msg, int itemId, boolean jukebox, int time) {
      if (this.mapEffect == null) {
         this.mapEffect = new MapleMapEffect(msg, itemId);
         this.mapEffect.setJukebox(jukebox);
         this.broadcastMessage(this.mapEffect.makeStartData());
         Timer.MapTimer.getInstance().schedule(new Runnable() {
            public void run() {
               if (MapleMap.this.mapEffect != null) {
                  MapleMap.this.broadcastMessage(MapleMap.this.mapEffect.makeDestroyData());
                  MapleMap.this.mapEffect = null;
               }

            }
         }, jukebox ? 300000L : (long)time);
      }
   }

   public final void startExtendedMapEffect(final String msg, final int itemId) {
      this.broadcastMessage(CField.startMapEffect(msg, itemId, true));
      Timer.MapTimer.getInstance().schedule(new Runnable() {
         public void run() {
            MapleMap.this.broadcastMessage(CField.removeMapEffect());
            MapleMap.this.broadcastMessage(CField.startMapEffect(msg, itemId, false));
         }
      }, 60000L);
   }

   public final void startSimpleMapEffect(String msg, int itemId) {
      this.broadcastMessage(CField.startMapEffect(msg, itemId, true));
   }

   public final void startJukebox(String msg, int itemId) {
      this.startMapEffect(msg, itemId, true);
   }

   public final void addPlayer(final MapleCharacter chr) {
      ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.PLAYER)).put(chr.getObjectId(), chr);
      this.characters.add(chr);
      chr.setChangeTime();
      if (GameConstants.isZero(chr.getJob())) {
         Item weapon = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
         Item subWeapon = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
         if (weapon != null && subWeapon != null && weapon.getItemId() / 1000 == 1562 && subWeapon.getItemId() / 1000 == 1572) {
            chr.getInventory(MapleInventoryType.EQUIPPED).move((short)-10, (short)-11, (short)1);
            chr.getClient().getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, weapon));
            chr.getClient().getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, subWeapon));
         }
      }

      chr.setSkillCustomInfo(201212, 0L, 3000L);
      if (chr.getSkillCustomValue0(400051074) > 0L) {
         chr.removeSkillCustomInfo(400051074);
         chr.getClient().send(CField.fullMaker(0, 0));
      }

      if (chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-27) != null && chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-28) != null && chr.getMapId() != ServerConstants.warpMap) {
         if (chr.getAndroid() == null) {
            chr.setAndroid(chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-27).getAndroid());
         } else {
            chr.updateAndroid();
         }
      }

      if (GameConstants.isDemonAvenger(chr.getJob())) {
         EnumMap<SecondaryStat, Pair<Integer, Integer>> statups = new EnumMap(SecondaryStat.class);
         statups.put(SecondaryStat.LifeTidal, new Pair(3, 0));
         chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, (SecondaryStatEffect)null, chr));
      }

      if (GameConstants.isTeamMap(this.mapid) && !chr.inPVP()) {
         chr.setTeam(this.getAndSwitchTeam() ? 0 : 1);
      }

      if (chr.unstableMemorize > 0) {
         chr.getClient().getSession().writeAndFlush(SkillPacket.메모리초이스(chr.unstableMemorize));
      }

      Iterator var26 = chr.getMap().getAllMagicSword().iterator();

      while(var26.hasNext()) {
         MapleMagicSword mo = (MapleMagicSword)var26.next();
         if (mo != null && mo.getChr().getId() == chr.getId()) {
            --chr.활성화된소드;
            mo.setSchedule((ScheduledFuture)null);
            this.removeMapObject(mo);
         }
      }

      byte[] packet = CField.spawnPlayerMapobject(chr);
      byte[] packet2 = SLFCGPacket.SetupZodiacInfo();
      byte[] packet3 = SLFCGPacket.ZodiacRankInfo(chr.getId(), (int)chr.getKeyValue(190823, "grade"));
      if (!chr.isHidden()) {
         this.broadcastMessage(chr, packet, false);
         if (chr.getKeyValue(190823, "grade") > 0L) {
            this.broadcastMessage(chr, packet2, true);
            this.broadcastMessage(chr, packet3, true);
         }
      } else {
         this.broadcastGMMessage(chr, packet, false);
      }

      int i;
      Iterator s;
      if (GameConstants.isPhantom(chr.getJob())) {
         chr.getClient().getSession().writeAndFlush(CField.updateCardStack(false, chr.getCardStack()));
      } else if (GameConstants.isCain(chr.getJob())) {
         if (chr.getBuffedValue(63101005)) {
            List<MapleSecondAtom> remove = new ArrayList();
            i = 0;
            s = chr.getMap().getAllSecondAtoms().iterator();

            MapleSecondAtom at;
            while(s.hasNext()) {
               at = (MapleSecondAtom)s.next();
               if (at.getSourceId() == 63101006 && at.getChr().getId() == chr.getId()) {
                  int du = (int)((long)at.getSecondAtoms().getExpire() - System.currentTimeMillis() - at.getStartTime());
                  i += du > 0 ? du / 5700 : 0;
                  if (i >= 6) {
                     i = 6;
                  }

                  remove.add(at);
               }
            }

            s = remove.iterator();

            while(s.hasNext()) {
               at = (MapleSecondAtom)s.next();
               chr.getMap().removeSecondAtom(chr, at.getObjectId());
            }

            chr.removeSkillCustomInfo(63101006);
            if (i > 0) {
               chr.setSkillCustomInfo(63101005, (long)i, 0L);
            }
         }
      } else if (!chr.getSaList().isEmpty()) {
         chr.getSaList().clear();
         chr.removeSkillCustomInfo(9877654);
      }

      this.sendObjectPlacement(chr);
      chr.getClient().getSession().writeAndFlush(packet);
      if (chr.getGuild() != null && chr.getGuild().getCustomEmblem() != null) {
         this.broadcastMessage(chr, CField.loadGuildIcon(chr), false);
      }

      int eventskillid;
      if (chr.getDeathCount() > 0 && chr.getEventInstance() != null) {
         if (chr.getMapId() >= 450013000 && chr.getMapId() <= 450013800) {
            chr.getClient().getSession().writeAndFlush(CField.UIPacket.openUI(1204));
         } else if (chr.getMapId() == 450010500) {
            chr.getClient().getSession().writeAndFlush(CField.JinHillah(3, chr, this));
         } else if (chr.getMapId() != 262031310 && chr.getMapId() != 262030310 && chr.getMapId() != 262031300 && chr.getMapId() != 262030100 && chr.getMapId() != 262031100 && chr.getMapId() != 262030200 && chr.getMapId() != 262031200) {
            chr.getClient().getSession().writeAndFlush(CField.getDeathCount(chr.getDeathCount()));
         } else if (chr.getMapId() == 262031300) {
            eventskillid = 15 - chr.getDeathCount();
            chr.getClient().send(CWvsContext.onFieldSetVariable("TotalDeathCount", "15"));
            chr.getClient().send(CWvsContext.onFieldSetVariable("DeathCount", eventskillid.makeConcatWithConstants<invokedynamic>(eventskillid)));
         }
      }

      eventskillid = chr.getSkillLevel(80003064) > 0 ? 80003064 : (chr.getSkillLevel(80003046) > 0 ? 80003046 : (chr.getSkillLevel(80003025) > 0 ? 80003025 : (chr.getSkillLevel(80003016) > 0 ? 80003016 : 0)));
      if (eventskillid > 0) {
         String skillname = SkillFactory.getSkillName(eventskillid);
         int ui = eventskillid == 80003046 ? 1297 : (eventskillid == 80003016 ? 1291 : 0);
         boolean cast = true;
         if (eventskillid == 80003046 && chr.getKeyValue(100794, "today") >= (Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 3000L : 6000L)) {
            cast = false;
         }

         if (!chr.getMap().isSpawnPoint() && !chr.getMap().isLevelMob(chr) && chr.getBuffedValue(eventskillid)) {
            if (ui > 0) {
               chr.getClient().getSession().writeAndFlush(CField.UIPacket.closeUI(ui));
               if (eventskillid == 80003046) {
                  chr.getClient().send(SLFCGPacket.FollowNpctoSkill(false, 9062524, 0));
                  chr.getClient().send(SLFCGPacket.FollowNpctoSkill(false, 9062525, 0));
                  chr.getClient().send(SLFCGPacket.FollowNpctoSkill(false, 9062526, 0));
               }
            }

            chr.cancelEffect(chr.getBuffedEffect(SecondaryStat.EventSpecialSkill));
            chr.dropMessage(5, "레벨 범위 몬스터가 없거나 " + skillname + "을 사용할 수 없는 곳입니다.");
         } else if (chr.getMap().isSpawnPoint() && chr.getMap().isLevelMob(chr) && cast) {
            SkillFactory.getSkill(eventskillid).getEffect(chr.getSkillLevel(1)).applyTo(chr, 0);
            if (ui > 0) {
               chr.getClient().getSession().writeAndFlush(CField.UIPacket.openUI(ui));
            }
         }
      }

      if (this.isTown()) {
      }

      int i;
      GameConstants.achievementRatio(chr.getClient(), this.mapid);
      ArrayList remove;
      ArrayList eff;
      label661:
      switch(this.mapid) {
      case 109090300:
         chr.getClient().getSession().writeAndFlush(CField.showEquipEffect(chr.isCatching ? 1 : 0));
         break;
      case 310070140:
      case 310070220:
      case 310070450:
         int npcid = this.getId() == 310070140 ? 2155116 : (this.getId() == 310070220 ? 2155117 : 2155118);
         int questid = this.getId() == 310070140 ? '飌' : (this.getId() == 310070220 ? '飕' : '飰');
         Point pos = this.getId() == 310070140 ? new Point(1493, -62) : (this.getId() == 310070220 ? new Point(1352, -459) : new Point(483, -573));
         boolean already = false;
         Iterator itr = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.NPC)).values().iterator();

         MapleNPC npc;
         while(itr.hasNext()) {
            npc = (MapleNPC)itr.next();
            if (npc.getId() == npcid && npc.getOwner() == chr) {
               already = true;
               break;
            }
         }

         if (chr.getQuestStatus(questid) == 1 && !already) {
            npc = MapleLifeFactory.getNPC(npcid);
            npc.setPosition(pos);
            npc.setCy(pos.y);
            npc.setRx0(pos.x);
            npc.setRx1(pos.x);
            npc.setFh(this.getFootholds().findBelow(pos).getId());
            npc.setCustom(true);
            npc.setLeft(true);
            npc.setOwner(chr);
            this.addMapObject(npc);
            chr.getClient().send(CField.NPCPacket.spawnNPCRequestController(npc, true));
         } else if (already && chr.getQuestStatus(questid) != 1) {
            while(true) {
               do {
                  do {
                     if (!itr.hasNext()) {
                        break label661;
                     }

                     npc = (MapleNPC)itr.next();
                  } while(!npc.isCustom());
               } while(npcid != -1 && npc.getId() != npcid);

               if (npc.getOwner().getId() == chr.getId()) {
                  this.broadcastMessage(CField.NPCPacket.removeNPCController(npc.getObjectId()));
                  this.broadcastMessage(CField.NPCPacket.removeNPC(npc.getObjectId()));
                  itr.remove();
               }
            }
         }
         break;
      case 350160100:
      case 350160200:
         chr.getClient().getSession().writeAndFlush(CField.UseSkillWithUI(13, 80001974, 1));
         chr.getClient().getSession().writeAndFlush(MobPacket.CorruptionChange((byte)0, this.getStigmaDeath()));
         break;
      case 350160140:
      case 350160240:
         chr.getClient().getSession().writeAndFlush(CField.UseSkillWithUI(13, 80001974, 1));
         chr.getClient().getSession().writeAndFlush(MobPacket.CorruptionChange((byte)0, this.getStigmaDeath()));
         break;
      case 450002011:
      case 450002012:
      case 450002013:
      case 450002014:
      case 450002015:
      case 450002021:
      case 450002200:
      case 450002201:
      case 450002301:
      case 921170050:
      case 921170100:
      case 921171200:
      case 954080200:
      case 954080300:
      case 993000868:
      case 993000869:
      case 993000870:
      case 993000871:
      case 993000872:
      case 993000873:
      case 993000874:
      case 993000875:
      case 993000877:
         chr.getClient().getSession().writeAndFlush(CField.momentAreaOnOffAll(Collections.singletonList("swim01")));
         break;
      case 450004150:
      case 450004250:
      case 450004450:
      case 450004550:
         chr.getClient().getSession().writeAndFlush(MobPacket.BossLucid.changeStatueState(false, this.getLucidCount(), false));
         break;
      case 450008150:
      case 450008750:
         chr.getClient().getSession().writeAndFlush(MobPacket.BossWill.setMoonGauge(100, 45));
         chr.getClient().getSession().writeAndFlush(MobPacket.BossWill.addMoonGauge(chr.getMoonGauge()));
         break;
      case 450008250:
      case 450008850:
         chr.getClient().getSession().writeAndFlush(MobPacket.BossWill.setMoonGauge(100, 50));
         chr.getClient().getSession().writeAndFlush(MobPacket.BossWill.addMoonGauge(chr.getMoonGauge()));
         break;
      case 450008350:
      case 450008950:
         chr.getClient().getSession().writeAndFlush(MobPacket.BossWill.setMoonGauge(100, 25));
         chr.getClient().getSession().writeAndFlush(MobPacket.BossWill.addMoonGauge(chr.getMoonGauge()));
         break;
      case 809000101:
      case 809000201:
         chr.getClient().getSession().writeAndFlush(CField.showEquipEffect());
         break;
      case 912080100:
         final java.util.Timer tangTimer = new java.util.Timer();
         GameConstants.TangyoonMobSpawn(chr.getClient(), 0, true);
         chr.getClient().getSession().writeAndFlush(CField.getClock(1200));
         chr.removeKeyValue(2498, "TangyoonBoss");
         chr.removeKeyValue(2498, "TangyoonCooking");
         chr.removeKeyValue(2498, "TangyoonCookingClass");
         TimerTask tangTask = new TimerTask() {
            public void run() {
               GameConstants.TangyoonCookingClass(chr.getClient(), 0);
               tangTimer.cancel();
            }
         };
         tangTimer.schedule(tangTask, 4000L);
         break;
      case 993000400:
         chr.setFrittoDancing(new FrittoDancing(2));
         chr.getFrittoDancing().start(chr.getClient());
         break;
      case 993026900:
         chr.getClient().send(SLFCGPacket.ContentsWaiting(chr, 0, 11, 5, 1, 26));
         if (chr.getMap().getCustomTime(chr.getMap().getId()) != null) {
            chr.getClient().send(CField.getClock(chr.getMap().getCustomTime(chr.getMap().getId()) / 1000));
         }
         break;
      case 993192000:
         int flower = (int)chr.getKeyValue(501387, "flower");
         if (flower < 0) {
            flower = 0;
         }

         String str = String.valueOf(flower);
         chr.getClient().send(CField.setMapOBJ("all", 0, 0, 0));
         chr.getClient().send(CField.setMapOBJ(str, 1, 0, 0));
         if (this.getCustomValue(993192000) == null) {
            chr.getClient().send(CField.setSpecialMapEffect("bloomingSun", 0, 0));
            chr.getClient().send(CField.setSpecialMapEffect("bloomingWind", 0, 0));
            eff = new ArrayList();
            eff.add(new Pair("bloomingSun", 0));
            eff.add(new Pair("bloomingWind", 0));
            this.broadcastMessage(CField.ChangeSpecialMapEffect(eff));
         } else {
            boolean bool1 = (new Date()).getHours() % 2 == 0;
            if (this.getCustomTime(993192000) != null) {
               chr.getClient().send(CField.setSpecialMapEffect("bloomingSun", 1, 1));
               chr.getClient().send(CField.setSpecialMapEffect("bloomingWind", 1, 1));
               i = bool1 ? 2024011 : 2024012;
               MapleItemInformationProvider.getInstance().getItemEffect(i).applyTo(chr, true);
               chr.getClient().send(CField.setSpecialMapEffect("bloomingSun", !bool1 ? 1 : 0, !bool1 ? 1 : 0));
               chr.getClient().send(CField.setSpecialMapEffect("bloomingWind", !bool1 ? 1 : 0, !bool1 ? 1 : 0));
               remove = new ArrayList();
               remove.add(new Pair("bloomingSun", bool1 ? 1 : 0));
               remove.add(new Pair("bloomingWind", bool1 ? 0 : 1));
               chr.getClient().send(CField.ChangeSpecialMapEffect(remove));
               chr.getClient().send(CField.startMapEffect(bool1 ? "따사로운 봄 햇살이 쏟아져 내립니다." : "기분 좋게 시원한 봄바람이 살랑입니다.", bool1 ? 5121112 : 5121113, true));
            }
         }
         break;
      case 993194000:
         if (this.getCustomValue(993194000) == null) {
            chr.getClient().send(CField.setSpecialMapEffect("studioBlue", 0, 0));
            chr.getClient().send(CField.setSpecialMapEffect("studioPink", 0, 0));
            eff = new ArrayList();
            eff.add(new Pair("studioBlue", 0));
            eff.add(new Pair("studioPink", 0));
            this.broadcastMessage(CField.ChangeSpecialMapEffect(eff));
         } else {
            boolean sun = (new Date()).getHours() % 2 == 0;
            if (this.getCustomTime(993194000) != null) {
               chr.getClient().send(CField.setSpecialMapEffect("studioBlue", 1, 1));
               chr.getClient().send(CField.setSpecialMapEffect("studioPink", 1, 1));
               i = sun ? 2024017 : 2024018;
               MapleItemInformationProvider.getInstance().getItemEffect(i).applyTo(chr, true);
               chr.getClient().send(CField.setSpecialMapEffect("studioBlue", !sun ? 1 : 0, !sun ? 1 : 0));
               chr.getClient().send(CField.setSpecialMapEffect("studioPink", !sun ? 1 : 0, !sun ? 1 : 0));
               remove = new ArrayList();
               remove.add(new Pair("studioBlue", sun ? 1 : 0));
               remove.add(new Pair("studioPink", sun ? 0 : 1));
               chr.getClient().send(CField.ChangeSpecialMapEffect(remove));
               chr.getClient().send(CField.startMapEffect(sun ? "시청자로부터 블루 하트 선물이 쏟아집니다!" : "시청자로부터 핑크 하트 선물이 쏟아집니다!", sun ? 5121114 : 5121115, true));
            }
         }
         break;
      default:
         int[] array = new int[]{80001427, 80001428, 80001432, 80001762, 80001757, 80001755, 80001878, 80002888, 80002889, 80002890};
         int[] var40 = array;
         int var22 = array.length;

         for(int var23 = 0; var23 < var22; ++var23) {
            Integer skillid = var40[var23];
            if (chr.getBuffedValue(skillid)) {
               chr.cancelEffect(chr.getBuffedEffect(skillid));
            }
         }

         if (GameConstants.isMechanic(chr.getJob())) {
            remove = new ArrayList();
            Iterator var42 = chr.getSummons(35111002).iterator();

            MapleSummon ss;
            while(var42.hasNext()) {
               ss = (MapleSummon)var42.next();
               remove.add(ss);
            }

            var42 = remove.iterator();

            while(var42.hasNext()) {
               ss = (MapleSummon)var42.next();
               ss.removeSummon(this, false);
            }
         }

         chr.getClient().getSession().writeAndFlush(CField.UseSkillWithUI(0, 0, 0));
         chr.getClient().getSession().writeAndFlush(MobPacket.CorruptionChange((byte)0, 0));
      }

      if (chr.getV("bossPractice") != null && Integer.parseInt(chr.getV("bossPractice")) == 1) {
         chr.getClient().send(CField.getPracticeMode(true));
      }

      if (chr.getSkillCustomValue(9110) != null) {
         if (chr.getSkillCustomTime(9110) > 0 && (chr.getMapId() / 100000 == 9530 || chr.getMapId() / 100000 == 9540)) {
            chr.getClient().send(CField.getClock(chr.getSkillCustomTime(9110) / 1000));
         } else if (chr.getSkillCustomTime(9110) > 0) {
            chr.removeSkillCustomInfo(9110);
         }
      }

      if (chr.Stigma > 0 && !GameConstants.보스맵(this.getId())) {
         chr.Stigma = 0;
         Map<SecondaryStat, Pair<Integer, Integer>> dds = new HashMap();
         dds.put(SecondaryStat.Stigma, new Pair(chr.Stigma, 0));
         chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.cancelBuff(dds, chr));
         this.broadcastMessage(chr, CWvsContext.BuffPacket.cancelForeignBuff(chr, dds), false);
      }

      if (chr.getBuffedValue(80002404) && this.getId() != 450008850 && this.getId() != 450008250) {
         chr.cancelEffect(chr.getBuffedEffect(80002404));
      }

      for(i = 0; i < 3; ++i) {
         if (chr.getPet((long)i) != null) {
            chr.getClient().getSession().writeAndFlush(PetPacket.updatePet(chr, chr.getPet((long)i), chr.getInventory(MapleInventoryType.CASH).getItem(chr.getPet((long)i).getInventoryPosition()), false, chr.getPetLoot()));
            if (chr.getMapId() != ServerConstants.warpMap) {
               chr.getPet((long)i).setPos(chr.getPosition());
               this.broadcastMessage(chr, PetPacket.showPet(chr, chr.getPet((long)i), false, false), true);
            }
         }
      }

      if (chr.getAndroid() != null) {
         chr.getAndroid().setPos(chr.getPosition());
         this.broadcastMessage(CField.spawnAndroid(chr, chr.getAndroid()));
      }

      if (chr.getParty() != null) {
         chr.silentPartyUpdate();
         chr.getClient().getSession().writeAndFlush(CWvsContext.PartyPacket.updateParty(chr.getClient().getChannel(), chr.getParty(), PartyOperation.SILENT_UPDATE, (MaplePartyCharacter)null, chr.getPlayer()));
         chr.updatePartyMemberHP();
         chr.receivePartyMemberHP();
      }

      if (!this.onFirstUserEnter.isEmpty() && this.isFirstUserEnter()) {
         this.setFirstUserEnter(false);
         MapScriptMethods.startScript_FirstUser(chr.getClient(), this.onFirstUserEnter);
      }

      if (!this.onUserEnter.isEmpty()) {
         MapScriptMethods.startScript_User(chr.getClient(), this.onUserEnter);
      }

      List<MapleSummon> allSummons = chr.getSummons();
      s = allSummons.iterator();

      while(true) {
         MapleSummon summon;
         do {
            if (!s.hasNext()) {
               if (this.mapEffect != null) {
                  this.mapEffect.sendStartData(chr.getClient());
               }

               if (chr.getBuffedValue(SecondaryStat.RideVehicle) != null && !GameConstants.isResist(chr.getJob()) && FieldLimitType.Mount.check(this.fieldLimit)) {
                  chr.cancelEffectFromBuffStat(SecondaryStat.RideVehicle);
               }

               if (chr.getEventInstance() != null && chr.getEventInstance().isTimerStarted()) {
                  if (chr.inPVP()) {
                     chr.getClient().getSession().writeAndFlush(CField.getPVPClock(Integer.parseInt(chr.getEventInstance().getProperty("type")), (int)(chr.getEventInstance().getTimeLeft() / 1000L)));
                  } else {
                     chr.getClient().getSession().writeAndFlush(CField.getClock((int)(chr.getEventInstance().getTimeLeft() / 1000L)));
                  }
               }

               if (this.hasClock()) {
                  Calendar cal = Calendar.getInstance();
                  chr.getClient().getSession().writeAndFlush(CField.getClockTime(cal.get(11), cal.get(12), cal.get(13)));
               }

               if (this.getMapTimer() > 0L) {
                  chr.getClient().getSession().writeAndFlush(CField.getClock((int)((this.getMapTimer() - System.currentTimeMillis()) / 1000L)));
               }

               chr.getClient().getSession().writeAndFlush(CField.specialChair(chr, true, true, true, (MapleSpecialChair)null));
               if (this.isElitebossmap()) {
                  chr.getClient().send(CField.getClock(this.elitetime));
                  this.broadcastMessage(CField.specialMapEffect(2, true, "Bgm36.img/RoyalGuard", "Effect/EliteMobEff.img/eliteMonsterFrame", "Effect/EliteMobEff.img/eliteMonsterEffect", "", ""));
               } else if (this.isElitebossrewardmap()) {
                  if (this.getCustomValue(210403) != null) {
                     this.broadcastMessage(SLFCGPacket.milliTimer(this.getCustomTime(210403)));
                  }

                  this.broadcastMessage(CField.specialMapEffect(3, true, "Bgm36.img/HappyTimeShort", "Map/Map/Map9/924050000.img/back", "Effect/EliteMobEff.img/eliteBonusStage", "", ""));
               }

               if (this.burning > 0 && this.getAllNormalMonstersThreadsafe().size() > 0 && !this.isTown() && !GameConstants.로미오줄리엣(this.getId()) && !GameConstants.사냥컨텐츠맵(this.getId()) && this.isSpawnPoint() && !GameConstants.isContentsMap(this.getId())) {
                  chr.getClient().getSession().writeAndFlush(CField.playSound("Sound/FarmSE.img/boxResult"));
                  chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showBurningFieldEffect("#fn나눔고딕 ExtraBold##fs26#          버닝 " + this.burning + "단계 : 경험치 " + this.burning * 10 + "% 추가지급!!          "));
                  if (chr.getKeyValue(51351, "startquestid") == 49011L) {
                     chr.setKeyValue(51351, "queststat", "3");
                     chr.getClient().send(CWvsContext.updateSuddenQuest((int)chr.getKeyValue(51351, "midquestid"), false, PacketHelper.getKoreanTimestamp(System.currentTimeMillis()) + 600000000L, "count=1;Quest=" + chr.getKeyValue(51351, "startquestid") + ";state=3;"));
                     chr.getClient().send(CWvsContext.updateSuddenQuest((int)chr.getKeyValue(51351, "startquestid"), false, chr.getKeyValue(51351, "endtime"), "BTField=1;"));
                  }
               }

               if (this.rune != null && !chr.getBuffedValue(80002282) && this.runeCurse > 0 && this.isSpawnPoint() && !GameConstants.보스맵(this.mapid) && !GameConstants.isContentsMap(this.mapid)) {
                  chr.getClient().getSession().writeAndFlush(CField.runeCurse("룬을 해방하여 엘리트 보스의 저주를 풀어야 합니다!!\\n저주 " + this.runeCurse + "단계 :  경험치 획득, 드롭률 " + this.getRuneCurseDecrease() + "% 감소 효과 적용 중", false));
               }

               if (chr.getKeyValue(210416, "TotalDeadTime") > 0L && this.isSpawnPoint()) {
                  chr.getClient().getSession().writeAndFlush(CField.PenaltyMsg("경험치 획득, 드롭률 80% 감소 효과 적용 중!\r\n호신부적 아이템을 사용하면 즉시 해제할 수 있습니다.", 338, 10000, 180));
               }

               if (GameConstants.isYeti(chr.getJob())) {
                  if (chr.getMapId() == 993191400) {
                     NPCScriptManager.getInstance().start(chr.getClient(), 2007, "YetiTuto0");
                  }
               } else if (GameConstants.isPinkBean(chr.getJob()) && chr.getMapId() == 927030090) {
                  NPCScriptManager.getInstance().start(chr.getClient(), 2007, "PinkBeanTuto0");
               }

               if (this.getNumMonsters() > 0 && (this.mapid == 280030001 || this.mapid == 240060201 || this.mapid == 280030000 || this.mapid == 240060200 || this.mapid == 220080001 || this.mapid == 541020800 || this.mapid == 541010100)) {
                  String music = "Bgm09/TimeAttack";
                  switch(this.mapid) {
                  case 240060200:
                  case 240060201:
                     music = "Bgm14/HonTale";
                     break;
                  case 280030000:
                  case 280030001:
                     music = "Bgm06/FinalFight";
                  }

                  chr.getClient().getSession().writeAndFlush(CField.musicChange(music));
               }

               if (GameConstants.isEvan(chr.getJob()) && chr.getJob() >= 2200) {
                  if (chr.getDragon() == null) {
                     chr.makeDragon();
                  } else {
                     chr.getDragon().setPosition(chr.getPosition());
                  }

                  if (chr.getDragon() != null) {
                     this.broadcastMessage(CField.spawnDragon(chr.getDragon()));
                  }
               }

               if (this.permanentWeather > 0) {
                  chr.getClient().getSession().writeAndFlush(CField.startMapEffect("", this.permanentWeather, false));
               }

               if (this.getNodez().getEnvironments().size() > 0 && this.mapid != 450004250 && this.mapid != 450004550 && this.mapid != 450003920) {
                  chr.getClient().getSession().writeAndFlush(CField.getUpdateEnvironment(this.getNodez().getEnvironments()));
               }

               if (chr.getBuffedValue(SecondaryStat.RepeatEffect) != null) {
                  i = chr.getBuffedEffect(SecondaryStat.RepeatEffect).getSourceId();
                  if (GameConstants.isAngelicBlessBuffEffectItem(i)) {
                     EnumMap<SecondaryStat, Pair<Integer, Integer>> statups = new EnumMap(SecondaryStat.class);
                     statups.put(SecondaryStat.RepeatEffect, new Pair(1, 0));
                     this.broadcastMessage(CWvsContext.BuffPacket.giveForeignBuff(chr, statups, chr.getBuffedEffect(SecondaryStat.RepeatEffect)));
                  }
               }

               for(i = 0; i < 8; ++i) {
                  chr.setEffect(i, 0);
               }

               if (chr.getSkillCustomValue0(60524) > 0L) {
                  chr.setSkillCustomInfo(60524, 0L, 0L);
               }

               return;
            }

            summon = (MapleSummon)s.next();
         } while(summon.getMovementType() == SummonMovementType.STATIONARY && summon.getSkill() != 152101000);

         summon.setPosition(chr.getTruePosition());
         chr.addVisibleMapObject(summon);
         this.spawnSummon(summon);
      }
   }

   public int getNumItems() {
      return ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.ITEM)).size();
   }

   public int getNumMonsters() {
      return ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.MONSTER)).size();
   }

   public final EventManager getEMByMap() {
      String em = null;
      switch(this.mapid) {
      case 105100300:
         em = "BossBalrog_NORMAL";
         return ChannelServer.getInstance(this.channel).getEventSM().getEventManager(em);
      case 105100400:
         em = "BossBalrog_EASY";
         return ChannelServer.getInstance(this.channel).getEventSM().getEventManager(em);
      case 211070100:
      case 211070101:
      case 211070110:
         em = "VonLeonBattle";
         return ChannelServer.getInstance(this.channel).getEventSM().getEventManager(em);
      case 240060200:
         em = "HorntailBattle";
         return ChannelServer.getInstance(this.channel).getEventSM().getEventManager(em);
      case 240060201:
         em = "ChaosHorntail";
         return ChannelServer.getInstance(this.channel).getEventSM().getEventManager(em);
      case 270050100:
         em = "PinkBeanBattle";
         return ChannelServer.getInstance(this.channel).getEventSM().getEventManager(em);
      case 271040100:
         em = "CygnusBattle";
         return ChannelServer.getInstance(this.channel).getEventSM().getEventManager(em);
      case 280030000:
         em = "ZakumBattle";
         return ChannelServer.getInstance(this.channel).getEventSM().getEventManager(em);
      case 280030001:
         em = "ChaosZakum";
         return ChannelServer.getInstance(this.channel).getEventSM().getEventManager(em);
      case 551030200:
         em = "ScarTarBattle";
         return ChannelServer.getInstance(this.channel).getEventSM().getEventManager(em);
      case 802000111:
         em = "NamelessMagicMonster";
         return ChannelServer.getInstance(this.channel).getEventSM().getEventManager(em);
      case 802000211:
         em = "Vergamot";
         return ChannelServer.getInstance(this.channel).getEventSM().getEventManager(em);
      case 802000311:
         em = "2095_tokyo";
         return ChannelServer.getInstance(this.channel).getEventSM().getEventManager(em);
      case 802000411:
         em = "Dunas";
         return ChannelServer.getInstance(this.channel).getEventSM().getEventManager(em);
      case 802000611:
         em = "Nibergen";
         return ChannelServer.getInstance(this.channel).getEventSM().getEventManager(em);
      case 802000711:
         em = "Dunas2";
         return ChannelServer.getInstance(this.channel).getEventSM().getEventManager(em);
      case 802000801:
      case 802000802:
      case 802000803:
         em = "CoreBlaze";
         return ChannelServer.getInstance(this.channel).getEventSM().getEventManager(em);
      case 802000821:
      case 802000823:
         em = "Aufhaven";
         return ChannelServer.getInstance(this.channel).getEventSM().getEventManager(em);
      default:
         return null;
      }
   }

   public final void removePlayer(MapleCharacter chr) {
      if (this.everlast) {
         this.returnEverLastItem(chr);
      }

      this.characters.remove(chr);
      this.removeMapObject(chr);
      chr.checkFollow();
      chr.removeExtractor();
      this.broadcastMessage(CField.removePlayerFromMap(chr.getId()));
      if (this.characters.size() == 0) {
         this.setFirstUserEnter(true);
      }

      List<MapleSummon> allSummons = chr.getSummons();
      Iterator var3 = allSummons.iterator();

      while(var3.hasNext()) {
         MapleSummon summon = (MapleSummon)var3.next();
         if (summon.getSkill() == 152101000) {
            chr.CrystalCharge = summon.getEnergy();
         } else if (summon.getMovementType() != SummonMovementType.STATIONARY) {
            summon.removeSummon(this, true);
         }
      }

      this.checkStates(chr.getName());
      if (this.mapid == 109020001) {
         chr.canTalk(true);
      }

      chr.leaveMap(this);
   }

   public final void broadcastMessage(byte[] packet) {
      this.broadcastMessage((MapleCharacter)null, packet, Double.POSITIVE_INFINITY, (Point)null);
   }

   public final void broadcastMessage(MapleCharacter source, byte[] packet, boolean repeatToSource) {
      this.broadcastMessage(repeatToSource ? null : source, packet, Double.POSITIVE_INFINITY, source.getTruePosition());
   }

   public final void broadcastMessage(byte[] packet, Point rangedFrom) {
      this.broadcastMessage((MapleCharacter)null, packet, GameConstants.maxViewRangeSq(), rangedFrom);
   }

   public final void broadcastMessage(MapleCharacter source, byte[] packet, Point rangedFrom) {
      this.broadcastMessage(source, packet, GameConstants.maxViewRangeSq(), rangedFrom);
   }

   public void broadcastMessage(MapleCharacter source, byte[] packet, double rangeSq, Point rangedFrom) {
      Iterator itr = this.characters.iterator();

      while(itr.hasNext()) {
         MapleCharacter chr = (MapleCharacter)itr.next();
         if (chr != source) {
            if (rangeSq < Double.POSITIVE_INFINITY) {
               if (rangedFrom.distanceSq(chr.getTruePosition()) <= rangeSq) {
                  chr.getClient().getSession().writeAndFlush(packet);
               }
            } else {
               chr.getClient().getSession().writeAndFlush(packet);
            }
         }
      }

   }

   private void sendObjectPlacement(MapleCharacter c) {
      if (c != null) {
         Iterator var2 = this.getMapObjectsInRange(c.getTruePosition(), c.getRange(), GameConstants.rangedMapobjectTypes).iterator();

         while(true) {
            MapleMapObject o;
            do {
               if (!var2.hasNext()) {
                  return;
               }

               o = (MapleMapObject)var2.next();
            } while(o.getType() == MapleMapObjectType.REACTOR && !((MapleReactor)o).isAlive());

            o.sendSpawnData(c.getClient());
            c.addVisibleMapObject(o);
         }
      }
   }

   public final List<MaplePortal> getPortalsInRange(Point from, double rangeSq) {
      List<MaplePortal> ret = new ArrayList();
      Iterator var5 = this.portals.values().iterator();

      while(var5.hasNext()) {
         MaplePortal type = (MaplePortal)var5.next();
         if (from.distanceSq(type.getPosition()) <= rangeSq && type.getTargetMapId() != this.mapid && type.getTargetMapId() != 999999999) {
            ret.add(type);
         }
      }

      return ret;
   }

   public final List<MapleMapObject> getMapObjectsInRange(Point from, double rangeSq) {
      List<MapleMapObject> ret = new ArrayList();
      MapleMapObjectType[] var5 = MapleMapObjectType.values();
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         MapleMapObjectType type = var5[var7];
         Iterator itr = ((ConcurrentHashMap)this.mapobjects.get(type)).values().iterator();

         while(itr.hasNext()) {
            MapleMapObject mmo = (MapleMapObject)itr.next();
            if (from.distanceSq(mmo.getTruePosition()) <= rangeSq) {
               ret.add(mmo);
            }
         }
      }

      return ret;
   }

   public List<MapleMapObject> getItemsInRange(Point from, double rangeSq) {
      return this.getMapObjectsInRange(from, rangeSq, Arrays.asList(MapleMapObjectType.ITEM));
   }

   public final List<MapleMapObject> getMapObjectsInRange(Point from, double rangeSq, List<MapleMapObjectType> MapObject_types) {
      List<MapleMapObject> ret = new ArrayList();
      Iterator var6 = MapObject_types.iterator();

      while(var6.hasNext()) {
         MapleMapObjectType type = (MapleMapObjectType)var6.next();
         Iterator itr = ((ConcurrentHashMap)this.mapobjects.get(type)).values().iterator();

         while(itr.hasNext()) {
            MapleMapObject mmo = (MapleMapObject)itr.next();
            if (from.distanceSq(mmo.getTruePosition()) <= rangeSq) {
               ret.add(mmo);
            }
         }
      }

      return ret;
   }

   public final List<MapleMapObject> getMapObjectsInRect(Rectangle box, List<MapleMapObjectType> MapObject_types) {
      List<MapleMapObject> ret = new ArrayList();
      Iterator var4 = MapObject_types.iterator();

      while(var4.hasNext()) {
         MapleMapObjectType type = (MapleMapObjectType)var4.next();
         Iterator itr = ((ConcurrentHashMap)this.mapobjects.get(type)).values().iterator();

         while(itr.hasNext()) {
            MapleMapObject mmo = (MapleMapObject)itr.next();
            if (box.contains(mmo.getTruePosition())) {
               ret.add(mmo);
            }
         }
      }

      return ret;
   }

   public final List<MapleCharacter> getCharactersIntersect(Rectangle box) {
      List<MapleCharacter> ret = new ArrayList();
      Iterator itr = this.characters.iterator();

      while(itr.hasNext()) {
         MapleCharacter chr = (MapleCharacter)itr.next();
         if (chr.getBounds().intersects(box)) {
            ret.add(chr);
         }
      }

      return ret;
   }

   public final List<MapleCharacter> getPlayersInRectAndInList(Rectangle box, List<MapleCharacter> chrList) {
      List<MapleCharacter> character = new LinkedList();
      Iterator ltr = this.characters.iterator();

      while(ltr.hasNext()) {
         MapleCharacter a = (MapleCharacter)ltr.next();
         if (chrList.contains(a) && box.contains(a.getTruePosition())) {
            character.add(a);
         }
      }

      return character;
   }

   public final void addPortal(MaplePortal myPortal) {
      this.portals.put(myPortal.getId(), myPortal);
   }

   public final MaplePortal getPortal(String portalname) {
      Iterator var2 = this.portals.values().iterator();

      MaplePortal port;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         port = (MaplePortal)var2.next();
      } while(!port.getName().equals(portalname));

      return port;
   }

   public final MaplePortal getPortal(int portalid) {
      return (MaplePortal)this.portals.get(portalid);
   }

   public final List<MaplePortal> getPortalSP() {
      List<MaplePortal> res = new LinkedList();
      Iterator var2 = this.portals.values().iterator();

      while(var2.hasNext()) {
         MaplePortal port = (MaplePortal)var2.next();
         if (port.getName().equals("sp")) {
            res.add(port);
         }
      }

      return res;
   }

   public final void resetPortals() {
      Iterator var1 = this.portals.values().iterator();

      while(var1.hasNext()) {
         MaplePortal port = (MaplePortal)var1.next();
         port.setPortalState(true);
      }

   }

   public final void setFootholds(MapleFootholdTree footholds) {
      this.footholds = footholds;
   }

   public final MapleFootholdTree getFootholds() {
      return this.footholds;
   }

   public final int getNumSpawnPoints() {
      return this.monsterSpawn.size();
   }

   public final void loadMonsterRate(boolean first) {
      this.createMobInterval = 2500;
      int spawnSize = this.monsterSpawn.size();
      if (spawnSize < 60 && this.partyBonusRate <= 0) {
         this.maxRegularSpawn = (int)Math.ceil((double)((float)spawnSize * this.monsterRate));
      } else {
         this.maxRegularSpawn = Math.round((float)spawnSize / this.monsterRate);
      }

      if (this.fixedMob > 0) {
         this.maxRegularSpawn = this.fixedMob;
      } else if (this.maxRegularSpawn <= 2) {
         this.maxRegularSpawn = 2;
      } else if (this.maxRegularSpawn > spawnSize) {
         this.maxRegularSpawn = Math.max(10, spawnSize);
      }

      if (this.maxRegularSpawn < 100) {
         this.maxRegularSpawn = 100;
      }

      Collection<Spawns> newSpawn = new LinkedList();
      Collection<Spawns> newBossSpawn = new LinkedList();
      Iterator var5 = this.monsterSpawn.iterator();

      while(var5.hasNext()) {
         Spawns s = (Spawns)var5.next();
         if (s.getCarnivalTeam() < 2) {
            if (s.getMonster().isBoss()) {
               newBossSpawn.add(s);
            } else {
               newSpawn.add(s);
            }
         }
      }

      this.monsterSpawn.clear();
      this.monsterSpawn.addAll(newBossSpawn);
      this.monsterSpawn.addAll(newSpawn);
      this.monsterSpawn.addAll(newSpawn);
      this.monsterSpawn.addAll(newSpawn);
      if (first && spawnSize > 0) {
         this.lastSpawnTime = System.currentTimeMillis();
         if (this.barrierArc == 0 && this.barrierAut == 0 && this.getId() != 310070200 && this.getId() != 310070210 && this.getId() != 310070220) {
            this.createMobInterval = (short)(this.createMobInterval / 3);
         } else if (this.getId() != 450006000 && this.getId() != 450006010 && this.getId() != 450006020 && this.getId() != 450006030 && this.getId() != 450006040) {
            this.createMobInterval = (short)(this.createMobInterval - 2000);
         } else if (this.getId() == 450006000 || this.getId() == 450006010 || this.getId() == 450006020 || this.getId() == 450006030 || this.getId() == 450006040) {
            this.createMobInterval = (short)(this.createMobInterval + 2000);
         }

         if (GameConstants.isForceRespawn(this.mapid)) {
            this.createMobInterval = 0;
         }

         this.respawn(false);
      }

   }

   public final SpawnPoint addMonsterSpawn(MapleMonster monster, int mobTime, byte carnivalTeam, String msg) {
      Point newpos = this.calcPointBelow(monster.getPosition());
      --newpos.y;
      SpawnPoint sp = new SpawnPoint(monster, newpos, mobTime, carnivalTeam, msg);
      if (carnivalTeam > -1) {
         this.monsterSpawn.add(0, sp);
      } else {
         this.monsterSpawn.add(sp);
      }

      return sp;
   }

   public final void addAreaMonsterSpawn(MapleMonster monster, Point pos1, Point pos2, Point pos3, int mobTime, String msg, boolean shouldSpawn) {
      pos1 = this.calcPointBelow(pos1);
      pos2 = this.calcPointBelow(pos2);
      pos3 = this.calcPointBelow(pos3);
      if (monster == null) {
         System.out.println(this.mapid + "맵의 addAreaMonsterSpawn의 몹 데이터가 없음.");
      } else {
         if (pos1 != null) {
            --pos1.y;
         }

         if (pos2 != null) {
            --pos2.y;
         }

         if (pos3 != null) {
            --pos3.y;
         }

         if (pos1 == null && pos2 == null && pos3 == null) {
            int var10001 = this.mapid;
            System.out.println("WARNING: mapid " + var10001 + ", monster " + monster.getId() + " could not be spawned.");
         } else {
            if (pos1 != null) {
               if (pos2 == null) {
                  pos2 = new Point(pos1);
               }

               if (pos3 == null) {
                  pos3 = new Point(pos1);
               }
            } else if (pos2 != null) {
               if (pos1 == null) {
                  pos1 = new Point(pos2);
               }

               if (pos3 == null) {
                  pos3 = new Point(pos2);
               }
            } else if (pos3 != null) {
               if (pos1 == null) {
                  pos1 = new Point(pos3);
               }

               if (pos2 == null) {
                  pos2 = new Point(pos3);
               }
            }

            this.monsterSpawn.add(new SpawnPointAreaBoss(monster, pos1, pos2, pos3, mobTime, msg, shouldSpawn));
         }
      }
   }

   public final List<MapleCharacter> getCharacters() {
      return this.getCharactersThreadsafe();
   }

   public final List<MapleCharacter> getCharactersThreadsafe() {
      List<MapleCharacter> chars = new ArrayList();
      Iterator itr = this.characters.iterator();

      while(itr.hasNext()) {
         MapleCharacter chr = (MapleCharacter)itr.next();
         chars.add(chr);
      }

      return chars;
   }

   public final MapleCharacter getCharacterByName(String id) {
      Iterator itr = this.characters.iterator();

      MapleCharacter mc;
      do {
         if (!itr.hasNext()) {
            return null;
         }

         mc = (MapleCharacter)itr.next();
      } while(!mc.getName().equalsIgnoreCase(id));

      return mc;
   }

   public final MapleCharacter getCharacterById_InMap(int id) {
      return this.getCharacterById(id);
   }

   public final MapleCharacter getCharacterById(int id) {
      Iterator itr = this.characters.iterator();

      MapleCharacter mc;
      do {
         if (!itr.hasNext()) {
            return null;
         }

         mc = (MapleCharacter)itr.next();
      } while(mc.getId() != id);

      return mc;
   }

   public final void updateMapObjectVisibility(MapleCharacter chr, MapleMapObject mo) {
      if (chr != null) {
         if (!chr.isMapObjectVisible(mo)) {
            System.out.println(mo.getType());
            if (mo.getType() == MapleMapObjectType.MIST || mo.getType() == MapleMapObjectType.EXTRACTOR || mo.getType() == MapleMapObjectType.SUMMON || mo.getType() == MapleMapObjectType.RUNE || mo.getType() == MapleMapObjectType.MagicSword || mo instanceof MechDoor || mo.getTruePosition().distanceSq(chr.getTruePosition()) <= mo.getRange()) {
               chr.addVisibleMapObject(mo);
               mo.sendSpawnData(chr.getClient());
            }
         } else if (!(mo instanceof MechDoor) && mo.getType() != MapleMapObjectType.MIST && mo.getType() != MapleMapObjectType.EXTRACTOR && mo.getType() != MapleMapObjectType.SUMMON && mo.getType() != MapleMapObjectType.RUNE && mo.getType() != MapleMapObjectType.MagicSword && mo.getTruePosition().distanceSq(chr.getTruePosition()) > mo.getRange()) {
            chr.removeVisibleMapObject(mo);
            mo.sendDestroyData(chr.getClient());
         } else if (mo.getType() == MapleMapObjectType.MONSTER && chr.getPosition().distanceSq(mo.getPosition()) <= GameConstants.maxViewRangeSq()) {
            this.updateMonsterController((MapleMonster)mo);
         }

      }
   }

   public void moveMonster(MapleMonster monster, Point reportedPos) {
      monster.setPosition(reportedPos);
      Iterator itr = this.characters.iterator();

      while(itr.hasNext()) {
         MapleCharacter mc = (MapleCharacter)itr.next();
         this.updateMapObjectVisibility(mc, monster);
      }

   }

   public void movePlayer(MapleCharacter player, Point newPosition) {
      player.setPosition(newPosition);
      Iterator var3 = player.getVisibleMapObjects().iterator();

      while(true) {
         MapleMapObject mo;
         while(var3.hasNext()) {
            mo = (MapleMapObject)var3.next();
            if (mo != null && this.getMapObject(mo.getObjectId(), mo.getType()) == mo) {
               this.updateMapObjectVisibility(player, mo);
            } else if (mo != null) {
               player.getVisibleMapObjects().remove(mo);
            }
         }

         var3 = this.getMapObjectsInRange(player.getTruePosition(), player.getRange()).iterator();

         while(var3.hasNext()) {
            mo = (MapleMapObject)var3.next();
            if (mo != null && !player.getVisibleMapObjects().contains(mo) && mo.getType() != MapleMapObjectType.MagicSword) {
               mo.sendSpawnData(player.getClient());
               player.getVisibleMapObjects().add(mo);
            }
         }

         return;
      }
   }

   public MaplePortal findClosestSpawnpoint(Point from) {
      MaplePortal closest = this.getPortal(0);
      double shortestDistance = Double.POSITIVE_INFINITY;
      Iterator var5 = this.portals.values().iterator();

      while(var5.hasNext()) {
         MaplePortal portal = (MaplePortal)var5.next();
         double distance = portal.getPosition().distanceSq(from);
         if (portal.getType() >= 0 && portal.getType() <= 2 && distance < shortestDistance && portal.getTargetMapId() == 999999999) {
            closest = portal;
            shortestDistance = distance;
         }
      }

      return closest;
   }

   public MaplePortal findClosestPortal(Point from) {
      MaplePortal closest = this.getPortal(0);
      double shortestDistance = Double.POSITIVE_INFINITY;
      Iterator var5 = this.portals.values().iterator();

      while(var5.hasNext()) {
         MaplePortal portal = (MaplePortal)var5.next();
         double distance = portal.getPosition().distanceSq(from);
         if (distance < shortestDistance) {
            closest = portal;
            shortestDistance = distance;
         }
      }

      return closest;
   }

   public String spawnDebug() {
      StringBuilder sb = new StringBuilder("Mobs in map : ");
      sb.append(this.getNumMonsters());
      sb.append(" spawnedMonstersOnMap: ");
      sb.append(this.spawnedMonstersOnMap);
      sb.append(" spawnpoints: ");
      sb.append(this.monsterSpawn.size());
      sb.append(" maxRegularSpawn: ");
      sb.append(this.maxRegularSpawn);
      sb.append(" monster rate: ");
      sb.append(this.monsterRate);
      sb.append(" fixed: ");
      sb.append(this.fixedMob);
      return sb.toString();
   }

   public int characterSize() {
      return this.characters.size();
   }

   public final int getMapObjectSize() {
      return this.mapobjects.size() + this.getCharactersSize() - this.characters.size();
   }

   public final int getCharactersSize() {
      int ret = 0;

      for(Iterator ltr = this.characters.iterator(); ltr.hasNext(); ++ret) {
         MapleCharacter chr = (MapleCharacter)ltr.next();
      }

      return ret;
   }

   public MapleCharacter getCharacter(int cid) {
      MapleCharacter ret = null;
      Iterator ltr = this.characters.iterator();

      MapleCharacter chr;
      do {
         if (!ltr.hasNext()) {
            return (MapleCharacter)ret;
         }

         chr = (MapleCharacter)ltr.next();
      } while(chr.getId() != cid);

      return chr;
   }

   public Collection<MaplePortal> getPortals() {
      return Collections.unmodifiableCollection(this.portals.values());
   }

   public void setPartyCount(int count) {
      this.partyquest = count;
   }

   public final int getPartyCount() {
      return this.partyquest;
   }

   public void setMoonCake(int count) {
      this.mooncake = count;
   }

   public final int getMoonCake() {
      return this.mooncake;
   }

   public void setKerningPQ(int count) {
      this.KerningPQ = count;
   }

   public final int getKerningPQ() {
      return this.KerningPQ;
   }

   public void setRPTicket(int count) {
      this.RPTicket = count;
   }

   public final int getRPTicket() {
      return this.RPTicket;
   }

   public void setrpportal(int count) {
      this.rpportal = count;
   }

   public final int getrpportal() {
      return this.rpportal;
   }

   public void setMonstermarble(int count) {
      this.Monstermarble = count;
   }

   public final int getMonstermarble() {
      return this.Monstermarble;
   }

   public void partyrespawn() {
      if (this.partyquest < 4) {
         ++this.partyquest;
      } else {
         this.partyquest = 0;
      }

   }

   public void respawn(boolean force) {
      this.respawn(force, System.currentTimeMillis());
   }

   public void respawn(boolean force, long now) {
      if (this.KerningPQ != 1) {
         if (!this.eliteBossAppeared) {
            if (this.partyquest == 0) {
               this.lastSpawnTime = now;
               int num = this.getNumMonsters();
               if (this.spawnedMonstersOnMap.get() != num) {
                  this.spawnedMonstersOnMap.set(num);
               }

               int numShouldSpawn;
               int spawned;
               if (force) {
                  numShouldSpawn = this.monsterSpawn.size() - this.spawnedMonstersOnMap.get();
                  if (numShouldSpawn > 0) {
                     spawned = 0;
                     Iterator var7 = this.monsterSpawn.iterator();

                     Spawns spawnPoint;
                     while(var7.hasNext()) {
                        spawnPoint = (Spawns)var7.next();
                        if (spawnPoint.getMonster().getLevel() < 200 && this.createMobInterval != 8000) {
                           this.maxRegularSpawn = 200;
                        }
                     }

                     var7 = this.monsterSpawn.iterator();

                     while(var7.hasNext()) {
                        spawnPoint = (Spawns)var7.next();
                        spawnPoint.spawnMonster(this);
                        ++spawned;
                        if (spawned >= numShouldSpawn) {
                           break;
                        }
                     }
                  }
               } else {
                  numShouldSpawn = (GameConstants.isForceRespawn(this.mapid) ? this.monsterSpawn.size() : this.maxRegularSpawn) - this.spawnedMonstersOnMap.get();
                  if (numShouldSpawn > 0) {
                     spawned = 0;
                     List<Spawns> randomSpawn = new ArrayList(this.monsterSpawn);
                     Collections.shuffle(randomSpawn);
                     List<Spawns> realSpawn = new ArrayList();
                     Iterator var9 = randomSpawn.iterator();

                     while(var9.hasNext()) {
                        Spawns spawnPoint = (Spawns)var9.next();
                        if (spawnPoint.getMonster().getLevel() < 200 && this.createMobInterval != 8000) {
                           this.maxRegularSpawn = 200;
                        }

                        if (GameConstants.isForceRespawn(this.mapid)) {
                           this.maxRegularSpawn = 400;
                        }

                        if (spawnPoint.shouldSpawn(this.lastSpawnTime)) {
                           realSpawn.add(spawnPoint);
                           spawnPoint.spawnMonster(this);
                           ++spawned;
                           if (spawned >= numShouldSpawn) {
                              break;
                           }
                        }
                     }
                  }
               }

            }
         }
      }
   }

   public String getSnowballPortal() {
      int[] teamss = new int[2];
      Iterator var2 = this.characters.iterator();

      while(var2.hasNext()) {
         MapleCharacter chr = (MapleCharacter)var2.next();
         int var10002;
         if (chr.getTruePosition().y > -80) {
            var10002 = teamss[0]++;
         } else {
            var10002 = teamss[1]++;
         }
      }

      if (teamss[0] > teamss[1]) {
         return "st01";
      } else {
         return "st00";
      }
   }

   public boolean isDisconnected(int id) {
      return this.dced.contains(id);
   }

   public void addDisconnected(int id) {
      this.dced.add(id);
   }

   public void resetDisconnected() {
      this.dced.clear();
   }

   public final void disconnectAll() {
      Iterator var1 = this.getCharactersThreadsafe().iterator();

      while(var1.hasNext()) {
         MapleCharacter chr = (MapleCharacter)var1.next();
         if (!chr.isGM()) {
            chr.getClient().disconnect(true, false);
            chr.getClient().getSession().close();
         }
      }

   }

   public List<MapleNPC> getAllNPCs() {
      return this.getAllNPCsThreadsafe();
   }

   public List<MapleNPC> getAllNPCsThreadsafe() {
      ArrayList<MapleNPC> ret = new ArrayList();
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.NPC)).values().iterator();

      while(var2.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var2.next();
         ret.add((MapleNPC)mmo);
      }

      return ret;
   }

   public final void resetNPCs() {
      this.removeNpc(-1);
   }

   public final void resetPQ(int level) {
      this.resetFully();
      Iterator var2 = this.getAllMonstersThreadsafe().iterator();

      while(var2.hasNext()) {
         MapleMonster mons = (MapleMonster)var2.next();
         mons.changeLevel(level, true);
      }

      this.resetSpawnLevel(level);
   }

   public final void resetSpawnLevel(int level) {
      Iterator var2 = this.monsterSpawn.iterator();

      while(var2.hasNext()) {
         Spawns spawn = (Spawns)var2.next();
         if (spawn instanceof SpawnPoint) {
            ((SpawnPoint)spawn).setLevel(level);
         }
      }

   }

   public final void resetFully() {
      this.resetFully(true);
   }

   public final void resetFully(boolean respawn) {
      this.killAllMonsters(true);
      this.reloadReactors();
      this.removeDrops();
      this.removeMists();
      this.resetNPCs();
      this.resetSpawns();
      this.resetDisconnected();
      this.cancelSquadSchedule(true);
      this.resetPortals();
      this.setFirstUserEnter(true);
      this.resetEnvironment();
      this.removeAllFlyingSword();
      this.setLucidCount(0);
      this.setLucidUseCount(0);
      this.setReqTouched(0);
      this.setStigmaDeath(0);
      this.PapulratusPatan = 0;
      this.PapulratusTime = 0;
      this.Papullatushour = 0;
      this.Papullatusminute = 0;
      this.Mapcoltime = 0;
      this.customInfo.clear();
      Iterator var2 = this.getAllSpiderWeb().iterator();

      while(var2.hasNext()) {
         SpiderWeb web = (SpiderWeb)var2.next();
         this.broadcastMessage(MobPacket.BossWill.willSpider(false, web));
         this.removeMapObject(web);
      }

      switch(this.getId()) {
      case 220080100:
      case 220080200:
      case 220080300:
         var2 = this.getAllReactor().iterator();

         while(var2.hasNext()) {
            MapleReactor reactor = (MapleReactor)var2.next();
            if (reactor != null && reactor.getReactorId() != 2208011 && reactor.getReactorId() != 2201004) {
               reactor.forceHitReactor((byte)1, 0);
            }
         }
      default:
         if (respawn) {
            this.respawn(true);
         }

      }
   }

   public void resetSpiderWeb() {
      Iterator var1 = this.getAllSpiderWeb().iterator();

      while(var1.hasNext()) {
         SpiderWeb web = (SpiderWeb)var1.next();
         this.broadcastMessage(MobPacket.BossWill.willSpider(false, web));
         this.removeMapObject(web);
      }

   }

   public final void cancelSquadSchedule(boolean interrupt) {
      this.squadTimer = false;
      this.checkStates = true;
      if (this.squadSchedule != null) {
         this.squadSchedule.cancel(interrupt);
         this.squadSchedule = null;
      }

   }

   public final void removeDrops() {
      List<MapleMapItem> items = this.getAllItemsThreadsafe();
      Iterator var2 = items.iterator();

      while(var2.hasNext()) {
         MapleMapItem i = (MapleMapItem)var2.next();
         i.expire(this);
      }

   }

   public final void removeMists() {
      List<MapleMist> mists = this.getAllMistsThreadsafe();
      Iterator var2 = mists.iterator();

      while(var2.hasNext()) {
         MapleMist m = (MapleMist)var2.next();
         this.broadcastMessage(CField.removeMist(m));
         this.removeMapObject(m);
      }

   }

   public final void resetAllSpawnPoint(int mobid, int mobTime) {
      Collection<Spawns> sss = new LinkedList(this.monsterSpawn);
      this.resetFully();
      this.monsterSpawn.clear();
      Iterator var4 = sss.iterator();

      while(var4.hasNext()) {
         Spawns s = (Spawns)var4.next();
         MapleMonster newMons = MapleLifeFactory.getMonster(mobid);
         newMons.setF(s.getF());
         newMons.setFh(s.getFh());
         newMons.setPosition(s.getPosition());
         this.addMonsterSpawn(newMons, mobTime, (byte)-1, (String)null);
      }

      this.loadMonsterRate(true);
   }

   public final void resetSpawns() {
      boolean changed = false;
      Iterator sss = this.monsterSpawn.iterator();

      while(sss.hasNext()) {
         if (((Spawns)sss.next()).getCarnivalId() > -1) {
            sss.remove();
            changed = true;
         }
      }

      this.setSpawns(true);
      if (changed) {
         this.loadMonsterRate(true);
      }

   }

   public final boolean makeCarnivalSpawn(int team, MapleMonster newMons, int num) {
      MapleNodes.MonsterPoint ret = null;
      Iterator var5 = this.getNodez().getMonsterPoints().iterator();

      while(var5.hasNext()) {
         MapleNodes.MonsterPoint mp = (MapleNodes.MonsterPoint)var5.next();
         if (mp.team == team || mp.team == -1) {
            Point newpos = this.calcPointBelow(new Point(mp.x, mp.y));
            --newpos.y;
            boolean found = false;
            Iterator var9 = this.monsterSpawn.iterator();

            while(var9.hasNext()) {
               Spawns s = (Spawns)var9.next();
               if (s.getCarnivalId() > -1 && (mp.team == -1 || s.getCarnivalTeam() == mp.team) && s.getPosition().x == newpos.x && s.getPosition().y == newpos.y) {
                  found = true;
                  break;
               }
            }

            if (!found) {
               ret = mp;
               break;
            }
         }
      }

      if (ret != null) {
         newMons.setCy(ret.cy);
         newMons.setF(0);
         newMons.setFh(ret.fh);
         newMons.setRx0(ret.x + 50);
         newMons.setRx1(ret.x - 50);
         newMons.setPosition(new Point(ret.x, ret.y));
         newMons.setHide(false);
         SpawnPoint sp = this.addMonsterSpawn(newMons, 1, (byte)team, (String)null);
         sp.setCarnival(num);
      }

      return ret != null;
   }

   public final boolean makeCarnivalReactor(int team, int num) {
      MapleReactor old = this.getReactorByName(team + num);
      if (old != null && old.getState() < 5) {
         return false;
      } else {
         Point guardz = null;
         List<MapleReactor> react = this.getAllReactorsThreadsafe();
         Iterator var6 = this.getNodez().getGuardians().iterator();

         while(var6.hasNext()) {
            Pair<Point, Integer> guard = (Pair)var6.next();
            if ((Integer)guard.right == team || (Integer)guard.right == -1) {
               boolean found = false;
               Iterator var9 = react.iterator();

               while(var9.hasNext()) {
                  MapleReactor r = (MapleReactor)var9.next();
                  if (r.getTruePosition().x == ((Point)guard.left).x && r.getTruePosition().y == ((Point)guard.left).y && r.getState() < 5) {
                     found = true;
                     break;
                  }
               }

               if (!found) {
                  guardz = (Point)guard.left;
                  break;
               }
            }
         }

         if (guardz != null) {
            MapleReactor my = new MapleReactor(MapleReactorFactory.getReactor(9980000 + team), 9980000 + team);
            my.setState((byte)1);
            my.setName(team + num);
            this.spawnReactorOnGroundBelow(my, guardz);
         }

         return guardz != null;
      }
   }

   public final void blockAllPortal() {
      Iterator var1 = this.portals.values().iterator();

      while(var1.hasNext()) {
         MaplePortal p = (MaplePortal)var1.next();
         p.setPortalState(false);
      }

   }

   public boolean getAndSwitchTeam() {
      return this.getCharactersSize() % 2 != 0;
   }

   public int getChannel() {
      return this.channel;
   }

   public int getConsumeItemCoolTime() {
      return this.consumeItemCoolTime;
   }

   public void setConsumeItemCoolTime(int ciit) {
      this.consumeItemCoolTime = ciit;
   }

   public void setPermanentWeather(int pw) {
      this.permanentWeather = pw;
   }

   public int getPermanentWeather() {
      return this.permanentWeather;
   }

   public void checkStates(String chr) {
      if (this.checkStates) {
         EventManager em = this.getEMByMap();
         int size = this.getCharactersSize();
         if (em != null && em.getProperty("state") != null && size == 0) {
            em.setProperty("state", "0");
            if (em.getProperty("leader") != null) {
               em.setProperty("leader", "true");
            }
         }

      }
   }

   public void setCheckStates(boolean b) {
      this.checkStates = b;
   }

   public void setNodes(MapleNodes mn) {
      this.setNodez(mn);
   }

   public final List<MapleNodes.MaplePlatform> getPlatforms() {
      return this.getNodez().getPlatforms();
   }

   public Collection<MapleNodes.MapleNodeInfo> getNodes() {
      return this.getNodez().getNodes();
   }

   public MapleNodes.MapleNodeInfo getNode(int index) {
      return this.getNodez().getNode(index);
   }

   public boolean isLastNode(int index) {
      return this.getNodez().isLastNode(index);
   }

   public final List<Rectangle> getAreas() {
      return this.getNodez().getAreas();
   }

   public final Rectangle getArea(int index) {
      return this.getNodez().getArea(index);
   }

   public final void changeEnvironment(String ms, int type) {
      this.broadcastMessage(CField.environmentChange(ms, type));
   }

   public final int getNumPlayersInArea(int index) {
      return this.getNumPlayersInRect(this.getArea(index));
   }

   public final int getNumPlayersInRect(Rectangle rect) {
      int ret = 0;
      Iterator ltr = this.characters.iterator();

      while(ltr.hasNext()) {
         if (rect.contains(((MapleCharacter)ltr.next()).getTruePosition())) {
            ++ret;
         }
      }

      return ret;
   }

   public final int getNumPlayersItemsInArea(int index) {
      return this.getNumPlayersItemsInRect(this.getArea(index));
   }

   public final int getNumPlayersItemsInRect(Rectangle rect) {
      int ret = this.getNumPlayersInRect(rect);
      Iterator var3 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.ITEM)).values().iterator();

      while(var3.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var3.next();
         if (rect.contains(mmo.getTruePosition())) {
            ++ret;
         }
      }

      return ret;
   }

   public void broadcastGMMessage(MapleCharacter source, byte[] packet, boolean repeatToSource) {
      this.broadcastGMMessage(repeatToSource ? null : source, packet);
   }

   private void broadcastGMMessage(MapleCharacter source, byte[] packet) {
      if (source == null) {
         Iterator itr = this.characters.iterator();

         while(itr.hasNext()) {
            MapleCharacter chr = (MapleCharacter)itr.next();
            if (source == null) {
               if (chr.isStaff()) {
                  chr.getClient().getSession().writeAndFlush(packet);
               }
            } else if (chr != source && chr.getGMLevel() >= source.getGMLevel()) {
               chr.getClient().getSession().writeAndFlush(packet);
            }
         }
      }

   }

   public final List<Pair<Integer, Integer>> getMobsToSpawn() {
      return this.getNodez().getMobsToSpawn();
   }

   public final List<Integer> getSkillIds() {
      return this.getNodez().getSkillIds();
   }

   public final boolean canSpawn(long now) {
      return this.lastSpawnTime > 0L && this.lastSpawnTime + (long)this.createMobInterval < now;
   }

   public final boolean canHurt(long now) {
      if (this.lastHurtTime > 0L && this.lastHurtTime + (long)this.decHPInterval < now) {
         this.lastHurtTime = now;
         return true;
      } else {
         return false;
      }
   }

   public final void resetShammos(final MapleClient c) {
      this.killAllMonsters(true);
      this.broadcastMessage(CWvsContext.serverNotice(5, "", "A player has moved too far from Shammos. Shammos is going back to the start."));
      Timer.EtcTimer.getInstance().schedule(new Runnable() {
         public void run() {
            if (c.getPlayer() != null) {
               c.getPlayer().changeMap(MapleMap.this, MapleMap.this.getPortal(0));
               if (MapleMap.this.getCharactersThreadsafe().size() > 1) {
                  MapScriptMethods.startScript_FirstUser(c, "shammos_Fenter");
               }
            }

         }
      }, 500L);
   }

   public int getInstanceId() {
      return this.instanceid;
   }

   public void setInstanceId(int ii) {
      this.instanceid = ii;
   }

   public int getPartyBonusRate() {
      return this.partyBonusRate;
   }

   public void setPartyBonusRate(int ii) {
      this.partyBonusRate = ii;
   }

   public short getTop() {
      return this.top;
   }

   public short getBottom() {
      return this.bottom;
   }

   public short getLeft() {
      return this.left;
   }

   public short getRight() {
      return this.right;
   }

   public void setTop(int ii) {
      this.top = (short)ii;
   }

   public void setBottom(int ii) {
      this.bottom = (short)ii;
   }

   public void setLeft(int ii) {
      this.left = (short)ii;
   }

   public void setRight(int ii) {
      this.right = (short)ii;
   }

   public List<Pair<Point, Integer>> getGuardians() {
      return this.getNodez().getGuardians();
   }

   public MapleNodes.DirectionInfo getDirectionInfo(int i) {
      return this.getNodez().getDirection(i);
   }

   public Collection<MapleCharacter> getNearestPvpChar(Point attacker, double maxRange, double maxHeight, boolean isLeft, Collection<MapleCharacter> chr) {
      Collection<MapleCharacter> character = new LinkedList();
      Iterator var9 = this.characters.iterator();

      while(var9.hasNext()) {
         MapleCharacter a = (MapleCharacter)var9.next();
         if (chr.contains(a.getClient().getPlayer())) {
            Point attackedPlayer = a.getPosition();
            MaplePortal Port = a.getMap().findClosestSpawnpoint(a.getPosition());
            Point nearestPort = Port.getPosition();
            attackedPlayer.distance(nearestPort);
            double distanceX = attacker.distance(attackedPlayer.getX(), attackedPlayer.getY());
            if (isLeft) {
               if (attacker.x < attackedPlayer.x && distanceX < maxRange && distanceX > 1.0D && (double)attackedPlayer.y >= (double)attacker.y - maxHeight && (double)attackedPlayer.y <= (double)attacker.y + maxHeight) {
                  character.add(a);
               }
            } else if (attacker.x > attackedPlayer.x && distanceX < maxRange && distanceX > 1.0D && (double)attackedPlayer.y >= (double)attacker.y - maxHeight && (double)attackedPlayer.y <= (double)attacker.y + maxHeight) {
               character.add(a);
            }
         }
      }

      return character;
   }

   public void startCatch() {
      if (this.catchstart == null) {
         this.broadcastMessage(CField.getClock(180));
         this.catchstart = Timer.MapTimer.getInstance().schedule(new Runnable() {
            public void run() {
               MapleMap.this.broadcastMessage(CWvsContext.serverNotice(1, "", "[술래잡기 알림]\r\n제한시간 2분이 지나 양이 승리하였습니다!\r\n모든 분들은 게임 보상맵으로 이동됩니다."));
               Iterator var1 = MapleMap.this.getCharacters().iterator();

               while(var1.hasNext()) {
                  MapleCharacter chr = (MapleCharacter)var1.next();
                  chr.getStat().setHp(chr.getStat().getMaxHp(), chr);
                  chr.updateSingleStat(MapleStat.HP, chr.getStat().getMaxHp());
                  if (chr.isCatching) {
                     chr.changeMap(chr.getClient().getChannelServer().getMapFactory().getMap(910040005), (MaplePortal)chr.getClient().getChannelServer().getMapFactory().getMap(910040005).getPortalSP().get(0));
                     chr.isWolfShipWin = false;
                  } else {
                     chr.changeMap(chr.getClient().getChannelServer().getMapFactory().getMap(910040004), (MaplePortal)chr.getClient().getChannelServer().getMapFactory().getMap(910040004).getPortalSP().get(0));
                     chr.isWolfShipWin = true;
                  }
               }

               MapleMap.this.stopCatch();
            }
         }, 180000L);
      }

   }

   public void stopCatch() {
      if (this.catchstart != null) {
         this.catchstart.cancel(true);
         this.catchstart = null;
      }

   }

   public List<MapleMonster> getAllButterFly() {
      ArrayList<MapleMonster> ret = new ArrayList();
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.MONSTER)).values().iterator();

      while(true) {
         MapleMonster monster;
         do {
            label39:
            do {
               while(var2.hasNext()) {
                  MapleMapObject mmo = (MapleMapObject)var2.next();
                  monster = (MapleMonster)mmo;
                  if (monster.getId() != 8880175 && monster.getId() != 8880178 && monster.getId() != 8880179 || this.mapid != 450004250 && this.mapid != 450004550) {
                     continue label39;
                  }

                  ret.add(monster);
               }

               return ret;
            } while(monster.getId() != 8880165 && monster.getId() != 8880168 && monster.getId() != 8880169);
         } while(this.mapid != 450004150 && this.mapid != 450004450);

         ret.add(monster);
      }
   }

   public final void killMonsters(List<MapleMonster> mon) {
      Iterator var2 = mon.iterator();

      while(var2.hasNext()) {
         MapleMonster monster = (MapleMonster)var2.next();
         if (this.RealSpawns.contains(monster)) {
            this.RealSpawns.remove(monster);
         }

         monster.setHp(0L);
         this.broadcastMessage(MobPacket.killMonster(monster.getObjectId(), 0));
         this.removeMapObject(monster);
         monster.killed();
         this.spawnedMonstersOnMap.decrementAndGet();
      }

   }

   public long getMapTimer() {
      return this.timer;
   }

   public void setMapTimer(long timer) {
      this.timer = timer;
   }

   public final void spawnSoul(MapleMapObject dropper, MapleCharacter chr, Item item, Point pos, Item weapon) {
      Point droppos = this.calcDropPos(pos, pos);
      MapleMapItem drop = new MapleMapItem(item, droppos, dropper, chr, (byte)0, true);
      this.broadcastMessage(CField.dropItemFromMapObject(chr.getMap(), drop, dropper.getPosition(), droppos, (byte)0, false));
      this.broadcastMessage(CField.removeItemFromMap(drop.getObjectId(), 2, chr.getId(), 0));
      chr.setSoulMP((Equip)weapon);
   }

   public int getBurning() {
      return this.burning;
   }

   public void setBurning(int burning) {
      this.burning = burning;
   }

   public long getBurningIncreasetime() {
      return this.burningIncreasetime;
   }

   public void setBurningIncreasetime(long burningtime) {
      this.burningIncreasetime = burningtime;
   }

   public int getBurningDecreasetime() {
      return this.burningDecreasetime;
   }

   public void setBurningDecreasetime(int burningtime) {
      this.burningDecreasetime = burningtime;
   }

   public List<Rectangle> makeRandomSplitAreas(Point position, Point lt, Point rb, int i, boolean b) {
      List<Rectangle> splitArea = new ArrayList();

      for(byte count = 0; count < i; ++count) {
         splitArea.add(new Rectangle());
      }

      return splitArea;
   }

   public MapleNodes getNodez() {
      return this.nodes;
   }

   public void setNodez(MapleNodes nodes) {
      this.nodes = nodes;
   }

   public void updateEnvironment(List<String> updateLists) {
      Iterator var2 = this.getNodez().getEnvironments().iterator();

      while(var2.hasNext()) {
         MapleNodes.Environment ev = (MapleNodes.Environment)var2.next();
         if (updateLists.contains(ev.getName())) {
            ev.setShow(true);
         } else {
            ev.setShow(false);
         }
      }

      this.broadcastMessage(CField.getUpdateEnvironment(this.getNodez().getEnvironments()));
   }

   public void resetEnvironment() {
      Iterator var1 = this.getNodez().getEnvironments().iterator();

      while(var1.hasNext()) {
         MapleNodes.Environment ev = (MapleNodes.Environment)var1.next();
         ev.setShow(false);
      }

      this.broadcastMessage(CField.getUpdateEnvironment(this.getNodez().getEnvironments()));
   }

   public List<MapleMagicWreck> getAllFieldThreadsafe() {
      ArrayList<MapleMagicWreck> ret = new ArrayList();
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.WRECK)).values().iterator();

      while(var2.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var2.next();
         ret.add((MapleMagicWreck)mmo);
      }

      return ret;
   }

   public List<MapleMagicWreck> getWrecks() {
      return this.wrecks;
   }

   public void setWrecks(List<MapleMagicWreck> wrecks) {
      this.wrecks = wrecks;
   }

   public boolean isFirstUserEnter() {
      return this.firstUserEnter;
   }

   public void setFirstUserEnter(boolean firstUserEnter) {
      this.firstUserEnter = firstUserEnter;
   }

   public int getRuneCurse() {
      return this.runeCurse;
   }

   public void setRuneCurse(int runeCurse) {
      this.runeCurse = runeCurse;
   }

   public int getRuneCurseDecrease() {
      switch(this.runeCurse) {
      case 1:
         return 50;
      case 2:
         return 65;
      case 3:
         return 80;
      case 4:
         return 100;
      default:
         return 0;
      }
   }

   public boolean isBingoGame() {
      return this.bingoGame;
   }

   public void setBingoGame(boolean bingoGame) {
      this.bingoGame = bingoGame;
   }

   public boolean isEliteField() {
      return this.isEliteField;
   }

   public void setEliteField(boolean isEliteField) {
      this.isEliteField = isEliteField;
   }

   public MapleRune getRune() {
      return this.rune;
   }

   public void setRune(MapleRune rune) {
      this.rune = rune;
   }

   public MapleMonster makePyramidMonster(MapleMonster monster, long hp, int level, int exp) {
      MapleMonster mob = MapleLifeFactory.getMonster(monster.getId());
      OverrideMonsterStats ostats = new OverrideMonsterStats();
      ostats.setOHp(hp);
      ostats.setOMp(mob.getMobMaxMp());
      ostats.setOExp((long)exp);
      mob.setOverrideStats(ostats);
      mob.setPosition(monster.getTruePosition());
      mob.setFh(monster.getFh());
      mob.getStats().setLevel((short)level);
      return mob;
   }

   public void addmonsterDefense(Map<Integer, List<Integer>> info) {
      this.monsterDefense.putAll(info);
   }

   public Map<Integer, List<Integer>> getmonsterDefense() {
      return this.monsterDefense;
   }

   public ScheduledFuture<?> getEliteBossSchedule() {
      return this.eliteBossSchedule;
   }

   public void setEliteBossSchedule(ScheduledFuture<?> eliteBossSchedule) {
      this.eliteBossSchedule = eliteBossSchedule;
   }

   public int getStigmaDeath() {
      return this.stigmaDeath;
   }

   public void setStigmaDeath(int stigmaDeath) {
      this.stigmaDeath = stigmaDeath;
   }

   public int getCandles() {
      return this.candles;
   }

   public void setCandles(int candles) {
      this.candles = candles;
   }

   public int getLightCandles() {
      return this.lightCandles;
   }

   public void setLightCandles(int lightCandles) {
      this.lightCandles = lightCandles;
   }

   public int getReqTouched() {
      return this.reqTouched;
   }

   public void setReqTouched(int reqTouched) {
      this.reqTouched = reqTouched;
   }

   public long getSandGlassTime() {
      return this.sandGlassTime;
   }

   public void setSandGlassTime(long sandGlassTime) {
      this.sandGlassTime = sandGlassTime;
   }

   public int getLucidCount() {
      return this.lucidCount;
   }

   public void setLucidCount(int lucidCount) {
      this.lucidCount = lucidCount;
   }

   public int getLucidUseCount() {
      return this.lucidUseCount;
   }

   public void setLucidUseCount(int lucidUseCount) {
      this.lucidUseCount = lucidUseCount;
   }

   public boolean isSpawnPoint() {
      boolean 몹젠 = false;
      switch(this.getId()) {
      case 220080100:
      case 220080200:
      case 220080300:
         return false;
      default:
         if (this.mapid != 220080200 && this.mapid != 105200300 && !GameConstants.로미오줄리엣(this.mapid)) {
            int i = 0;
            Iterator var3 = this.monsterSpawn.iterator();

            while(var3.hasNext()) {
               Spawns spawnPoint = (Spawns)var3.next();
               ++i;
               if (i == 1) {
                  몹젠 = true;
                  break;
               }
            }

            return 몹젠;
         } else {
            return false;
         }
      }
   }

   public final void spawnMagicSword(final MapleMagicSword ms, final MapleCharacter chr, final boolean core) {
      this.spawnAndAddRangedMapObject(ms, new MapleMap.DelayedPacketCreation() {
         public void sendPackets(MapleClient c) {
            MapleMap.this.broadcastMessage(SkillPacket.CreateSworldObtacle(ms));
         }
      });
      Timer.MapTimer tMan = Timer.MapTimer.getInstance();
      ScheduledFuture<?> schedule = null;
      ms.setSchedule((ScheduledFuture)schedule);
      ms.setSchedule(tMan.schedule(new Runnable() {
         public void run() {
            if (!core) {
               --chr.활성화된소드;
               if (chr.활성화된소드 < 0) {
                  chr.활성화된소드 = 0;
               }
            }

            MapleMap.this.removeMapObject(ms);
         }
      }, (long)(ms.getDuration() - 500)));
   }

   public Integer getCustomValue(int skillid) {
      return this.customInfo.containsKey(skillid) ? (int)((SkillCustomInfo)this.customInfo.get(skillid)).getValue() : null;
   }

   public Integer getCustomTime(int skillid) {
      return this.customInfo.containsKey(skillid) ? (int)(((SkillCustomInfo)this.customInfo.get(skillid)).getEndTime() - System.currentTimeMillis()) : null;
   }

   public int getCustomValue0(int skillid) {
      return this.customInfo.containsKey(skillid) ? (int)((SkillCustomInfo)this.customInfo.get(skillid)).getValue() : 0;
   }

   public void removeCustomInfo(int skillid) {
      this.customInfo.remove(skillid);
   }

   public void setCustomInfo(int skillid, int value, int time) {
      if (this.getCustomValue(skillid) != null) {
         this.removeCustomInfo(skillid);
      }

      this.customInfo.put(skillid, new SkillCustomInfo((long)value, (long)time));
   }

   public Map<Integer, SkillCustomInfo> getCustomValues() {
      return this.customInfo;
   }

   public void makeEliteMonster(int mobid, int grade, Point pos, boolean rune, boolean eliteboss) {
      MapleMonster monster = null;
      Iterator var8 = this.getAllMonster().iterator();

      while(var8.hasNext()) {
         MapleMonster m = (MapleMonster)var8.next();
         if (m != null && !m.isElitemonster() && !m.isEliteboss() && !m.getStats().isBoss()) {
            monster = m;
            break;
         }
      }

      MapleMonster eliteMonster = MapleLifeFactory.getMonster(mobid);
      if (monster == null) {
         monster = MapleLifeFactory.getMonster(mobid);
      }

      eliteMonster.setScale(200);
      int scale = Randomizer.nextInt(3);
      eliteMonster.setEliteGrade(scale);
      eliteMonster.setEliteType(1);
      eliteMonster.getStats().setLevel(monster.getStats().getLevel());
      EliteMonsterGradeInfo eg = null;
      int rand;
      switch(scale) {
      case 0:
         rand = Randomizer.rand(0, EliteMonsterGradeInfo.getFirstGradeInfo().size() - 1);
         eg = (EliteMonsterGradeInfo)EliteMonsterGradeInfo.getFirstGradeInfo().get(rand);
         eliteMonster.setHp(monster.getStats().getHp() * 15L);
         break;
      case 1:
         rand = Randomizer.rand(0, EliteMonsterGradeInfo.getSecondGradeInfo().size() - 1);
         eg = (EliteMonsterGradeInfo)EliteMonsterGradeInfo.getSecondGradeInfo().get(rand);
         eliteMonster.setHp(monster.getStats().getHp() * 20L);
         break;
      case 2:
         rand = Randomizer.rand(0, EliteMonsterGradeInfo.getThirdGradeInfo().size() - 1);
         eg = (EliteMonsterGradeInfo)EliteMonsterGradeInfo.getThirdGradeInfo().get(rand);
         eliteMonster.setHp(monster.getStats().getHp() * 30L);
      }

      eliteMonster.getEliteGradeInfo().add(new Pair(eg.getSkillid(), eg.getSkilllv()));
      eliteMonster.setCustomInfo(9999, 0, Randomizer.rand(3000, 10000));
      if (rune) {
         eliteMonster.setUserunespawn(true);
      } else {
         eliteMonster.setUserunespawn(false);
         this.setCustomInfo(8222222, 0, 600000);
      }

      eliteMonster.setElitemonster(true);
      eliteMonster.setElitehp(eliteMonster.getHp());
      this.spawnMonsterOnGroundBelow(eliteMonster, pos);
      this.broadcastMessage(CField.startMapEffect("어두운 기운과 함께 강력한 몬스터가 출현합니다.", 5120124, true));
      this.broadcastMessage(CField.specialMapSound("Field.img/eliteMonster/Regen"));
   }

   public int getEliteRequire() {
      return this.eliteRequire;
   }

   public void setEliteRequire(int eliteRequire) {
      this.eliteRequire = eliteRequire;
   }

   public int getEliteCount() {
      return this.eliteCount;
   }

   public void setEliteCount(int eliteCount) {
      this.eliteCount = eliteCount;
   }

   public int getEliteMobCount() {
      return this.EliteMobCount;
   }

   public void setEliteMobCount(int EliteMobCount) {
      this.EliteMobCount = EliteMobCount;
   }

   public int getEliteMobCommonCount() {
      return this.EliteMobCommonCount;
   }

   public void setEliteMobCommonCount(int EliteMobCommonCount) {
      this.EliteMobCommonCount = EliteMobCommonCount;
   }

   public int getElitebossrewardtype() {
      return this.elitebossrewardtype;
   }

   public void setElitebossrewardtype(int elitebossrewardtype) {
      this.elitebossrewardtype = elitebossrewardtype;
   }

   public boolean isElitebossmap() {
      return this.elitebossmap;
   }

   public void setElitebossmap(boolean elitebossmap) {
      this.elitebossmap = elitebossmap;
   }

   public boolean isElitebossrewardmap() {
      return this.elitebossrewardmap;
   }

   public void setElitebossrewardmap(boolean elitebossrewardmap) {
      this.elitebossrewardmap = elitebossrewardmap;
   }

   public int getElitechmpcount() {
      return this.elitechmpcount;
   }

   public void setElitechmpcount(int elitechmpcount) {
      this.elitechmpcount = elitechmpcount;
   }

   public boolean isEliteChmpmap() {
      return this.eliteChmpmap;
   }

   public void setEliteChmpmap(boolean eliteChmpmap) {
      this.eliteChmpmap = eliteChmpmap;
   }

   public int getElitechmptype() {
      return this.elitechmptype;
   }

   public void setElitechmptype(int elitechmptype) {
      this.elitechmptype = elitechmptype;
   }

   public boolean isElitechmpfinal() {
      return this.elitechmpfinal;
   }

   public void setElitechmpfinal(boolean elitechmpfinal) {
      this.elitechmpfinal = elitechmpfinal;
   }

   public int getElitetime() {
      return this.elitetime;
   }

   public void setElitetime(int elitetime) {
      this.elitetime = elitetime;
   }

   public void startEliteBossMap() {
      this.elitetime = 1800;
      this.broadcastMessage(CField.getClock(1800));
   }

   public void stopEliteBossMap() {
      this.elitetime = 0;
      this.killAllMonsters(true);
      this.setElitebossmap(false);
      this.broadcastMessage(CField.stopClock());
      this.broadcastMessage(CField.specialMapEffect(0, false, "", "", "", "", ""));
   }

   public void 미믹보상() {
      this.setElitebossrewardtype(1);
      this.broadcastMessage(CField.enforceMSG("왜인지 일반 공격으로 맞으면 아이템을 떨어트릴 것 같은 기분이 드는걸. 아이템은 직접 주워야 해.", 145, 7000));
      int i = 0;
      Iterator var2 = this.monsterSpawn.iterator();

      while(var2.hasNext()) {
         Spawns spawnPoint = (Spawns)var2.next();
         MapleMonster monster = MapleLifeFactory.getMonster(8220027);
         monster.getStats().setFirstAttack(false);
         this.spawnMonsterOnGroundBelow(monster, spawnPoint.getPosition());
         ++i;
         if (i == 5) {
            break;
         }
      }

   }

   public void 공중미믹보상() {
      this.setElitebossrewardtype(2);
      this.broadcastMessage(CField.enforceMSG("착한 모험가들에게 선물을 주지! 내가 던지는 아이템을 잘 받아 봐!", 146, 7000));
      int i = 0;
      Iterator var2 = this.monsterSpawn.iterator();

      while(var2.hasNext()) {
         Spawns spawnPoint = (Spawns)var2.next();
         MapleMonster monster = MapleLifeFactory.getMonster(8220028);
         monster.getStats().setFirstAttack(false);
         this.spawnMonsterOnGroundBelow(monster, spawnPoint.getPosition());
         ++i;
         if (i == 4) {
            break;
         }
      }

   }

   public void 하늘보상() {
      this.setElitebossrewardtype(3);
      this.broadcastMessage(CField.enforceMSG("하늘에서 갑자기 아이템이 떨어지고 있어요! 닿으면 얻을 수 있을것 같아요!", 162, 7000));
   }

   public void 엘보보상맵캔슬() {
      Timer.MapTimer.getInstance().schedule(new Runnable() {
         public final void run() {
            List<MapleMapItem> items = MapleMap.this.getAllItemsThreadsafe();
            Iterator var2 = items.iterator();

            while(var2.hasNext()) {
               MapleMapItem i = (MapleMapItem)var2.next();
               if (i.getItemId() >= 2432391 && i.getItemId() <= 2432398) {
                  i.expire(MapleMap.this);
               }
            }

            MapleMap.this.killAllMonsters(true);
            MapleMap.this.setElitebossmap(false);
            MapleMap.this.setElitebossrewardmap(false);
            MapleMap.this.setEliteMobCommonCount(0);
            MapleMap.this.setEliteMobCount(0);
            MapleMap.this.setElitebossrewardtype(0);
            MapleMap.this.broadcastMessage(CField.showEffect("Map/Effect2.img/event/gameover"));
            MapleMap.this.broadcastMessage(SLFCGPacket.playSE("MiniGame.img/multiBingo/gameover"));
            MapleMap.this.broadcastMessage(CField.stopClock());
            MapleMap.this.broadcastMessage(CField.specialMapEffect(0, false, "", "", "", "", ""));
         }
      }, 30000L);
   }

   public void startEliteBossReward() {
      this.elitetime = 0;
      this.broadcastMessage(CField.specialMapEffect(3, false, "Bgm36.img/HappyTimeShort", "Map/Map/Map9/924050000.img/back", "Effect/EliteMobEff.img/eliteBonusStage", "", ""));
      this.broadcastMessage(SLFCGPacket.playSE("MiniGame.img/multiBingo/3"));
      Timer.MapTimer.getInstance().schedule(new Runnable() {
         public void run() {
            MapleMap.this.broadcastMessage(SLFCGPacket.playSE("MiniGame.img/multiBingo/2"));
         }
      }, 3000L);
      Timer.MapTimer.getInstance().schedule(new Runnable() {
         public void run() {
            MapleMap.this.broadcastMessage(SLFCGPacket.playSE("MiniGame.img/multiBingo/1"));
         }
      }, 5000L);
      Timer.MapTimer.getInstance().schedule(new Runnable() {
         public void run() {
            int rand = Randomizer.rand(1, 3);
            if (rand == 1) {
               MapleMap.this.미믹보상();
            } else if (rand == 2) {
               MapleMap.this.공중미믹보상();
            } else if (rand == 3) {
               MapleMap.this.하늘보상();
            }

            MapleMap.this.setCustomInfo(210403, 0, 30000);
            MapleMap.this.broadcastMessage(SLFCGPacket.milliTimer(30000));
            MapleMap.this.엘보보상맵캔슬();
            MapleMap.this.broadcastMessage(SLFCGPacket.playSE("MiniGame.img/multiBingo/start"));
         }
      }, 6000L);
   }

   public int getPapulratusTime() {
      return this.PapulratusTime;
   }

   public void setPapulratusTime(int PapulratusTime) {
      this.PapulratusTime = PapulratusTime;
   }

   public int getPapulratusPatan() {
      return this.PapulratusPatan;
   }

   public void setPapulratusPatan(int PapulratusPatan) {
      this.PapulratusPatan = PapulratusPatan;
   }

   public int getPapullatushour() {
      return this.Papullatushour;
   }

   public void setPapullatushour(int Papullatushour) {
      this.Papullatushour = Papullatushour;
   }

   public int getPapullatusminute() {
      return this.Papullatusminute;
   }

   public void setPapullatusminute(int Papullatusminute) {
      this.Papullatusminute = Papullatusminute;
   }

   public ScheduledFuture<?> getSchedule() {
      return this.schedule;
   }

   public void setSchedule(ScheduledFuture<?> schedule) {
      this.schedule = schedule;
   }

   public void RewardCheck(int monsterid, int monsterid2, int spawnmonsterid, Point pos) {
      int size = 0;
      Iterator var6 = this.getAllMonster().iterator();

      while(var6.hasNext()) {
         MapleMonster m = (MapleMonster)var6.next();
         if (m.getId() >= monsterid && m.getId() <= monsterid2 && m.getSeperateSoul() <= 0) {
            ++size;
         }
      }

      if (size == 0) {
         this.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(spawnmonsterid), pos);
      }

   }

   public final void CreateObstacle(MapleMonster monster, List<Obstacle> obs) {
      Iterator var3 = obs.iterator();

      while(var3.hasNext()) {
         Obstacle ob = (Obstacle)var3.next();
         this.addMapObject(ob);
      }

      this.broadcastMessage(MobPacket.createObstacle(monster, obs, (byte)0));
   }

   public final void CreateObstacle2(MapleMonster monster, Obstacle ob, byte type) {
      this.addMapObject(ob);
      this.broadcastMessage(MobPacket.createObstacle2(monster, ob, type));
   }

   public List<Obstacle> getAllObstacle() {
      ArrayList<Obstacle> ret = new ArrayList();
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.OBSTACLE)).values().iterator();

      while(var2.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var2.next();
         ret.add((Obstacle)mmo);
      }

      return ret;
   }

   public List<SpiderWeb> getAllSpiderThreadsafe() {
      ArrayList<SpiderWeb> ret = new ArrayList();
      Iterator var2 = ((ConcurrentHashMap)this.mapobjects.get(MapleMapObjectType.WEB)).values().iterator();

      while(var2.hasNext()) {
         MapleMapObject mmo = (MapleMapObject)var2.next();
         ret.add((SpiderWeb)mmo);
      }

      return ret;
   }

   public final List<SpiderWeb> getAllSpiderWeb() {
      return this.getWebInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.WEB));
   }

   public final List<SpiderWeb> getWebInRange(Point from, double rangeSq, List<MapleMapObjectType> MapObject_types) {
      List<MapleMapObject> mapobject = this.getMapObjectsInRange(from, rangeSq);
      List<SpiderWeb> webs = new ArrayList();

      for(int i = 0; i < mapobject.size(); ++i) {
         if (((MapleMapObject)mapobject.get(i)).getType() == MapleMapObjectType.WEB) {
            webs.add((SpiderWeb)mapobject.get(i));
         }
      }

      return webs;
   }

   public List<Point> getWillPoison() {
      return this.willpoison;
   }

   public void addWillPoison(Point pos) {
      this.willpoison.add(pos);
   }

   public void removeWillPosion(Point pos) {
      this.willpoison.remove(pos);
   }

   public void resetWillPosion() {
      this.willpoison.clear();
   }

   public void getDuskObtacles(MapleMonster monster, int rand) {
      List<Obstacle> obs = new ArrayList();
      Obstacle ob = null;
      if (rand == 0) {
         ob = new Obstacle(65, new Point(291, -1055), new Point(291, -157), 36, 15, 0, 164, 800, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(67, new Point(412, -1055), new Point(412, -157), 36, 15, 0, 478, 400, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(66, new Point(-226, -1055), new Point(-226, -157), 36, 15, 0, 897, 600, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(67, new Point(574, -1055), new Point(574, -157), 36, 15, 0, 1476, 400, 1, 898, 0);
         obs.add(ob);
      } else if (rand == 1) {
         ob = new Obstacle(67, new Point(373, -1055), new Point(373, -157), 36, 15, 0, 294, 401, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(67, new Point(-139, -1055), new Point(-139, -157), 36, 15, 0, 866, 401, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(66, new Point(554, -1055), new Point(554, -157), 36, 15, 0, 846, 601, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(67, new Point(-370, -1055), new Point(-370, -157), 36, 15, 0, 916, 400, 1, 898, 0);
         obs.add(ob);
      } else if (rand == 2) {
         ob = new Obstacle(66, new Point(-187, -1055), new Point(-187, -157), 36, 15, 0, 151, 601, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(67, new Point(-569, -1055), new Point(-569, -157), 36, 15, 0, 1047, 400, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(65, new Point(-472, -1055), new Point(-472, -157), 36, 15, 0, 1333, 800, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(65, new Point(-206, -1055), new Point(-206, -157), 36, 15, 0, 1124, 800, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(67, new Point(-123, -1055), new Point(-123, -157), 36, 15, 0, 689, 401, 1, 898, 0);
         obs.add(ob);
      } else if (rand == 3) {
         ob = new Obstacle(67, new Point(-545, -1055), new Point(-545, -157), 36, 15, 0, 352, 401, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(67, new Point(563, -1055), new Point(563, -157), 36, 15, 0, 1407, 401, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(66, new Point(-151, -1055), new Point(-151, -157), 36, 15, 0, 407, 600, 1, 898, 0);
         obs.add(ob);
      } else if (rand == 4) {
         ob = new Obstacle(65, new Point(-151, -1055), new Point(-151, -157), 36, 15, 0, 1133, 801, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(66, new Point(-69, -1055), new Point(-69, -157), 36, 15, 0, 641, 600, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(67, new Point(-7, -1055), new Point(-7, -157), 36, 15, 0, 1442, 401, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(67, new Point(315, -1055), new Point(315, -157), 36, 15, 0, 1280, 401, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(66, new Point(137, -1055), new Point(137, -157), 36, 15, 0, 1398, 601, 1, 898, 0);
         obs.add(ob);
      } else if (rand == 5) {
         ob = new Obstacle(66, new Point(580, -1055), new Point(580, -157), 36, 15, 0, 787, 600, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(66, new Point(230, -1055), new Point(230, -157), 36, 15, 0, 209, 600, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(65, new Point(-405, -1055), new Point(-405, -157), 36, 15, 0, 448, 801, 1, 898, 0);
         obs.add(ob);
      } else if (rand == 6) {
         ob = new Obstacle(66, new Point(-318, -1055), new Point(-318, -157), 36, 15, 0, 662, 600, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(66, new Point(-438, -1055), new Point(-438, -157), 36, 15, 0, 1351, 601, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(66, new Point(-614, -1055), new Point(-614, -157), 36, 15, 0, 437, 600, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(66, new Point(41, -1055), new Point(41, -157), 36, 15, 0, 794, 600, 1, 898, 0);
         obs.add(ob);
      } else if (rand == 7) {
         ob = new Obstacle(66, new Point(-96, -1055), new Point(-96, -157), 36, 15, 0, 692, 601, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(65, new Point(-58, -1055), new Point(-58, -157), 36, 15, 0, 798, 801, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(67, new Point(33, -1055), new Point(33, -157), 36, 15, 0, 330, 400, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(65, new Point(-37, -1055), new Point(-37, -157), 36, 15, 0, 1028, 800, 1, 898, 0);
         obs.add(ob);
      } else if (rand == 8) {
         ob = new Obstacle(67, new Point(-358, -1055), new Point(-358, -157), 36, 15, 0, 323, 401, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(67, new Point(-395, -1055), new Point(-395, -157), 36, 15, 0, 263, 400, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(65, new Point(-391, -1055), new Point(-391, -157), 36, 15, 0, 908, 801, 1, 898, 0);
         obs.add(ob);
      } else if (rand == 9) {
         ob = new Obstacle(65, new Point(-176, -1055), new Point(-176, -157), 36, 15, 0, 638, 800, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(65, new Point(199, -1055), new Point(199, -157), 36, 15, 0, 603, 801, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(66, new Point(-128, -1055), new Point(-128, -157), 36, 15, 0, 577, 600, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(66, new Point(35, -1055), new Point(35, -157), 36, 15, 0, 841, 600, 1, 898, 0);
         obs.add(ob);
      } else if (rand == 10) {
         ob = new Obstacle(67, new Point(-25, -1055), new Point(-25, -157), 36, 15, 0, 156, 400, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(66, new Point(-416, -1055), new Point(-416, -157), 36, 15, 0, 1284, 601, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(65, new Point(147, -1055), new Point(147, -157), 36, 15, 0, 261, 800, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(65, new Point(-273, -1055), new Point(-273, -157), 36, 15, 0, 1092, 801, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(67, new Point(612, -1055), new Point(612, -157), 36, 15, 0, 323, 401, 1, 898, 0);
         obs.add(ob);
      } else if (rand == 11) {
         ob = new Obstacle(65, new Point(-374, -1055), new Point(-374, -157), 36, 15, 0, 999, 800, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(67, new Point(-157, -1055), new Point(-157, -157), 36, 15, 0, 245, 400, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(66, new Point(45, -1055), new Point(45, -157), 36, 15, 0, 283, 601, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(66, new Point(376, -1055), new Point(376, -157), 36, 15, 0, 623, 600, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(67, new Point(431, -1055), new Point(431, -157), 36, 15, 0, 1298, 401, 1, 898, 0);
         obs.add(ob);
      } else if (rand == 12) {
         ob = new Obstacle(66, new Point(-41, -1055), new Point(-41, -157), 36, 15, 0, 1397, 601, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(67, new Point(-194, -1055), new Point(-194, -157), 36, 15, 0, 304, 400, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(67, new Point(-114, -1055), new Point(-114, -157), 36, 15, 0, 1057, 401, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(65, new Point(-495, -1055), new Point(-495, -157), 36, 15, 0, 1165, 800, 1, 898, 0);
         obs.add(ob);
      } else if (rand == 13) {
         ob = new Obstacle(67, new Point(136, -1055), new Point(136, -157), 36, 15, 0, 795, 400, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(65, new Point(-162, -1055), new Point(-162, -157), 36, 15, 0, 999, 801, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(65, new Point(578, -1055), new Point(578, -157), 36, 15, 0, 613, 801, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(65, new Point(79, -1055), new Point(79, -157), 36, 15, 0, 474, 801, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(65, new Point(-614, -1055), new Point(-614, -157), 36, 15, 0, 215, 800, 1, 898, 0);
         obs.add(ob);
      } else if (rand == 14) {
         ob = new Obstacle(65, new Point(-192, -1055), new Point(-192, -157), 36, 15, 0, 1015, 801, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(65, new Point(548, -1055), new Point(548, -157), 36, 15, 0, 1160, 801, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(67, new Point(-180, -1055), new Point(-180, -157), 36, 15, 0, 528, 400, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(65, new Point(-614, -1055), new Point(-614, -157), 36, 15, 0, 1009, 801, 1, 898, 0);
         obs.add(ob);
      } else if (rand == 15) {
         ob = new Obstacle(66, new Point(-243, -1055), new Point(-243, -157), 36, 15, 0, 740, 601, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(66, new Point(372, -1055), new Point(372, -157), 36, 15, 0, 1289, 601, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(66, new Point(14, -1055), new Point(14, -157), 36, 15, 0, 1386, 600, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(65, new Point(612, -1055), new Point(612, -157), 36, 15, 0, 297, 801, 1, 898, 0);
         obs.add(ob);
      } else if (rand == 16) {
         ob = new Obstacle(67, new Point(199, -1055), new Point(199, -157), 36, 15, 0, 873, 400, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(65, new Point(248, -1055), new Point(248, -157), 36, 15, 0, 683, 800, 1, 898, 0);
         obs.add(ob);
      } else if (rand == 17) {
         ob = new Obstacle(65, new Point(-411, -1055), new Point(-411, -157), 36, 15, 0, 733, 801, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(66, new Point(432, -1055), new Point(432, -157), 36, 15, 0, 284, 601, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(66, new Point(-614, -1055), new Point(-614, -157), 36, 15, 0, 896, 601, 1, 898, 0);
         obs.add(ob);
      } else if (rand == 18) {
         ob = new Obstacle(66, new Point(-211, -1055), new Point(-211, -157), 36, 15, 0, 1292, 600, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(66, new Point(237, -1055), new Point(237, -157), 36, 15, 0, 606, 600, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(66, new Point(-310, -1055), new Point(-310, -157), 36, 15, 0, 1002, 601, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(66, new Point(338, -1055), new Point(338, -157), 36, 15, 0, 1087, 601, 1, 898, 0);
         obs.add(ob);
      } else if (rand == 19) {
         ob = new Obstacle(65, new Point(-229, -1055), new Point(-229, -157), 36, 15, 0, 763, 800, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(65, new Point(-165, -1055), new Point(-165, -157), 36, 15, 0, 136, 800, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(67, new Point(-408, -1055), new Point(-408, -157), 36, 15, 0, 370, 400, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(66, new Point(-411, -1055), new Point(-411, -157), 36, 15, 0, 401, 600, 1, 898, 0);
         obs.add(ob);
      } else if (rand == 20) {
         ob = new Obstacle(66, new Point(-246, -1055), new Point(-246, -157), 36, 15, 0, 528, 601, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(65, new Point(607, -1055), new Point(607, -157), 36, 15, 0, 1018, 801, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(65, new Point(-114, -1055), new Point(-114, -157), 36, 15, 0, 575, 801, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(65, new Point(-133, -1055), new Point(-133, -157), 36, 15, 0, 485, 800, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(66, new Point(-63, -1055), new Point(-63, -157), 36, 15, 0, 685, 600, 1, 898, 0);
         obs.add(ob);
      } else if (rand == 21) {
         ob = new Obstacle(67, new Point(570, -1055), new Point(570, -157), 36, 15, 0, 697, 401, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(67, new Point(-256, -1055), new Point(-256, -157), 36, 15, 0, 1078, 401, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(67, new Point(361, -1055), new Point(361, -157), 36, 15, 0, 1285, 400, 1, 898, 0);
         obs.add(ob);
         ob = new Obstacle(66, new Point(612, -1055), new Point(612, -157), 36, 15, 0, 796, 601, 1, 898, 0);
         obs.add(ob);
      }

      if (!obs.isEmpty()) {
         this.CreateObstacle(monster, obs);
      }

   }

   public int getMobsSize(int mobid) {
      int number = 0;
      Iterator var3 = this.getAllMonstersThreadsafe().iterator();

      while(var3.hasNext()) {
         MapleMapObject obj = (MapleMapObject)var3.next();
         MapleMonster mob = (MapleMonster)obj;
         if (mob.getId() == mobid) {
            ++number;
         }
      }

      return number;
   }

   public void getObtacles(int rand) {
      List<Obstacle> obs = new ArrayList();
      Obstacle ob;
      if (this.getId() == 450013300) {
         if (rand == 0) {
            ob = new Obstacle(77, new Point(-987, -600), new Point(-983, -239), 95, 50, 0, 487, 1000, 123, 7, 360);
            obs.clear();
            obs.add(ob);
            this.broadcastMessage(MobPacket.CreateObstacle2(obs));
            ob = new Obstacle(77, new Point(-731, -600), new Point(-973, 88), 95, 50, 0, 923, 1000, 110, 5, 728);
            obs.clear();
            obs.add(ob);
            this.broadcastMessage(MobPacket.CreateObstacle2(obs));
            ob = new Obstacle(77, new Point(-296, -600), new Point(-961, 88), 95, 50, 0, 1388, 1000, 141, 7, 973);
            obs.clear();
            obs.add(ob);
            this.broadcastMessage(MobPacket.CreateObstacle2(obs));
            ob = new Obstacle(77, new Point(119, -600), new Point(-569, 88), 95, 50, 0, 1797, 1000, 132, 5, 973);
            obs.clear();
            obs.add(ob);
            this.broadcastMessage(MobPacket.CreateObstacle2(obs));
            ob = new Obstacle(77, new Point(486, -600), new Point(-202, 88), 95, 50, 0, 2277, 1000, 130, 5, 973);
            obs.clear();
            obs.add(ob);
            this.broadcastMessage(MobPacket.CreateObstacle2(obs));
            ob = new Obstacle(77, new Point(849, -600), new Point(161, 88), 95, 50, 0, 2770, 1000, 137, 6, 973);
            obs.clear();
            obs.add(ob);
            this.broadcastMessage(MobPacket.CreateObstacle2(obs));
         } else {
            ob = new Obstacle(77, new Point(323, -600), new Point(-365, 88), 95, 50, 0, 415, 1000, 85, 5, 973);
            obs.clear();
            obs.add(ob);
            this.broadcastMessage(MobPacket.CreateObstacle2(obs));
            ob = new Obstacle(77, new Point(-48, -600), new Point(-736, 88), 95, 50, 0, 878, 1000, 130, 5, 973);
            obs.clear();
            obs.add(ob);
            this.broadcastMessage(MobPacket.CreateObstacle2(obs));
            ob = new Obstacle(77, new Point(-481, -600), new Point(-966, 88), 95, 50, 0, 1302, 1000, 148, 6, 840);
            obs.clear();
            obs.add(ob);
            this.broadcastMessage(MobPacket.CreateObstacle2(obs));
            ob = new Obstacle(77, new Point(-879, -600), new Point(-977, 88), 95, 50, 0, 1737, 1000, 112, 5, 694);
            obs.clear();
            obs.add(ob);
            this.broadcastMessage(MobPacket.CreateObstacle2(obs));
         }
      } else if (rand == 0) {
         ob = new Obstacle(77, new Point(-853, -600), new Point(-976, 85), 95, 50, 0, 498, 1000, 129, 6, 695);
         obs.clear();
         obs.add(ob);
         this.broadcastMessage(MobPacket.CreateObstacle2(obs));
         ob = new Obstacle(77, new Point(-407, -600), new Point(-964, 85), 95, 50, 0, 977, 1000, 102, 5, 883);
         obs.clear();
         obs.add(ob);
         this.broadcastMessage(MobPacket.CreateObstacle2(obs));
         ob = new Obstacle(77, new Point(-102, -600), new Point(-787, 85), 95, 50, 0, 1397, 1000, 90, 6, 969);
         obs.clear();
         obs.add(ob);
         this.broadcastMessage(MobPacket.CreateObstacle2(obs));
         ob = new Obstacle(77, new Point(178, -600), new Point(-507, 85), 95, 50, 0, 1802, 1000, 111, 5, 969);
         obs.clear();
         obs.add(ob);
         this.broadcastMessage(MobPacket.CreateObstacle2(obs));
         ob = new Obstacle(77, new Point(569, -600), new Point(-116, 85), 95, 50, 0, 2299, 1000, 141, 6, 969);
         obs.clear();
         obs.add(ob);
         this.broadcastMessage(MobPacket.CreateObstacle2(obs));
         ob = new Obstacle(77, new Point(897, -600), new Point(482, -185), 95, 50, 0, 2714, 1000, 107, 7, 587);
         obs.clear();
         obs.add(ob);
         this.broadcastMessage(MobPacket.CreateObstacle2(obs));
      } else {
         ob = new Obstacle(77, new Point(-938, -600), new Point(-979, 85), 95, 50, 0, 425, 1000, 90, 6, 685);
         obs.clear();
         obs.add(ob);
         this.broadcastMessage(MobPacket.CreateObstacle2(obs));
         ob = new Obstacle(77, new Point(-594, -600), new Point(-969, 85), 95, 50, 0, 844, 1000, 130, 6, 779);
         obs.clear();
         obs.add(ob);
         this.broadcastMessage(MobPacket.CreateObstacle2(obs));
         ob = new Obstacle(77, new Point(-179, -600), new Point(-864, 85), 95, 50, 0, 1254, 1000, 101, 6, 969);
         obs.clear();
         obs.add(ob);
         this.broadcastMessage(MobPacket.CreateObstacle2(obs));
         ob = new Obstacle(77, new Point(245, -600), new Point(-440, 85), 95, 50, 0, 1669, 1000, 131, 7, 969);
         obs.clear();
         obs.add(ob);
         this.broadcastMessage(MobPacket.CreateObstacle2(obs));
         ob = new Obstacle(77, new Point(604, -600), new Point(94, -90), 95, 50, 0, 2128, 1000, 94, 6, 721);
         obs.clear();
         obs.add(ob);
         this.broadcastMessage(MobPacket.CreateObstacle2(obs));
         ob = new Obstacle(77, new Point(908, -600), new Point(223, 85), 95, 50, 0, 2574, 1000, 144, 7, 969);
         obs.clear();
         obs.add(ob);
         this.broadcastMessage(MobPacket.CreateObstacle2(obs));
      }

   }

   public void BloomingMoment(boolean active, boolean justshow) {
      boolean sun = (new Date()).getHours() % 2 == 0;
      if (((new Date()).getMinutes() >= 30 && (new Date()).getMinutes() < 40 || justshow) && (this.getCustomValue(993192000) == null || justshow)) {
         int buffid = sun ? 2024011 : 2024012;
         if (!justshow) {
            this.setCustomInfo(993192000, 0, 600000);
         }

         if (this.getCharactersSize() > 0) {
            Iterator var5 = this.getAllChracater().iterator();

            while(var5.hasNext()) {
               MapleCharacter chr = (MapleCharacter)var5.next();
               if (chr != null && !chr.getBuffedValue(buffid)) {
                  MapleItemInformationProvider.getInstance().getItemEffect(buffid).applyTo(chr, true);
               }
            }

            if (!sun) {
               this.broadcastMessage(CField.setSpecialMapEffect("bloomingSun", 1, 1));
               this.broadcastMessage(CField.setSpecialMapEffect("bloomingWind", 1, 1));
            }

            List<Pair<String, Integer>> eff = new ArrayList();
            eff.add(new Pair("bloomingSun", !active ? 0 : (sun ? 1 : 0)));
            eff.add(new Pair("bloomingWind", !active ? 0 : (sun ? 0 : 1)));
            this.broadcastMessage(CField.ChangeSpecialMapEffect(eff));
            if (active) {
               this.broadcastMessage(CField.startMapEffect(sun ? "따사로운 봄 햇살이 쏟아져 내립니다." : "기분 좋게 시원한 봄바람이 살랑입니다.", sun ? 5121112 : 5121113, true));
            }
         }
      }

   }

   public int getBattleGroundTimer() {
      return this.BattleGroundTimer;
   }

   public void setBattleGroundTimer(int BattleGroundTimer) {
      this.BattleGroundTimer = BattleGroundTimer;
   }

   public int getBattleGroundMainTimer() {
      return this.BattleGroundMainTimer;
   }

   public void setBattleGroundMainTimer(int BattleGroundMainTimer) {
      this.BattleGroundMainTimer = BattleGroundMainTimer;
   }

   public List<Point> getLucidDream() {
      return this.LucidDream;
   }

   public void setLucidDream(List<Point> LucidDream) {
      this.LucidDream = LucidDream;
   }

   public int getBarrierArc() {
      return this.barrierArc;
   }

   public void setBarrierArc(int barrierArc) {
      this.barrierArc = barrierArc;
   }

   public int getBarrierAut() {
      return this.barrierAut;
   }

   public void setBarrierAut(int barrierAut) {
      this.barrierAut = barrierAut;
   }

   public List<MapleMonster> getRealSpawns() {
      return this.RealSpawns;
   }

   public void setRealSpawns(List<MapleMonster> RealSpawns) {
      this.RealSpawns = RealSpawns;
   }

   private class MapleMapManagement implements Runnable {
      public void handleBurningStatus(long time) {
         if (MapleMap.this.getAllNormalMonstersThreadsafe().size() > 0 && !MapleMap.this.isTown() && !GameConstants.로미오줄리엣(MapleMap.this.getId()) && !GameConstants.사냥컨텐츠맵(MapleMap.this.getId()) && MapleMap.this.isSpawnPoint() && !GameConstants.보스맵(MapleMap.this.getId()) && !GameConstants.isContentsMap(MapleMap.this.getId())) {
            if (MapleMap.this.getAllCharactersThreadsafe().size() == 0) {
               if (MapleMap.this.getBurning() < 10) {
                  if (time - MapleMap.this.getBurningIncreasetime() > 500000L) {
                     MapleMap.this.setBurningIncreasetime(time);
                     MapleMap.this.setBurning(MapleMap.this.getBurning() + 1);
                  }
               } else if (MapleMap.this.getBurning() == 10) {
                  MapleMap.this.setBurningIncreasetime(time);
               }
            } else if (MapleMap.this.getBurning() > 0 && time - MapleMap.this.getBurningIncreasetime() > 1200000L) {
               MapleMap.this.setBurningIncreasetime(time);
               MapleMap.this.setBurning(MapleMap.this.getBurning() - 1);
               if (MapleMap.this.getBurning() > 0) {
                  MapleMap.this.broadcastMessage(CField.EffectPacket.showBurningFieldEffect("#fn나눔고딕 ExtraBold##fs26#          버닝 " + MapleMap.this.burning + "단계 : 경험치 " + MapleMap.this.burning * 10 + "% 추가지급!!          "));
               } else {
                  MapleMap.this.broadcastMessage(CField.EffectPacket.showBurningFieldEffect("#fn나눔고딕 ExtraBold##fs26#          버닝필드 소멸!          "));
               }
            }
         }

      }

      public void handleCharacters(long time) {
         Iterator chrs = MapleMap.this.getAllCharactersThreadsafe().iterator();

         label132:
         while(true) {
            MapleCharacter chr;
            do {
               if (!chrs.hasNext()) {
                  return;
               }

               chr = (MapleCharacter)chrs.next();
               if (chr.isAlive()) {
                  int webSize;
                  if (MapleMap.this.getId() >= 105200520 && MapleMap.this.getId() < 105200530 && chr.getBuffedEffect(SecondaryStat.NotDamaged) == null && chr.getBuffedEffect(SecondaryStat.IndieNotDamaged) == null) {
                     webSize = (int)(-(chr.getStat().getCurrentMaxHp() / 5L));
                     chr.addHP((long)webSize);
                     chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, webSize, 36, 0, 0, (byte)0, true, (Point)null, (String)null, (Item)null));
                     chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 0, webSize, 36, 0, 0, (byte)0, false, (Point)null, (String)null, (Item)null), false);
                  }

                  if (chr.getMapId() > 450008000 && chr.getMapId() < 450009000 && MapleMap.this.getNumMonsters() > 0 && chr.getMoonGauge() < 100 && chr.getSkillCustomValue(8880302) == null) {
                     webSize = chr.getMap().getAllSpiderWeb().size();
                     if (webSize < 67) {
                        chr.setMoonGauge(Math.min(chr.getMoonGauge() + (chr.isGM() ? 100 : 2), 100));
                     }

                     chr.getClient().getSession().writeAndFlush(MobPacket.BossWill.addMoonGauge(chr.getMoonGauge()));
                  }

                  if (chr.getBuffedValue(13111024) || chr.getBuffedValue(13120007)) {
                     new ArrayList();
                     int skillid = chr.getBuffedValue(13120007) ? 13120007 : 13111024;
                     MapleSummon summon = chr.getSummon(skillid);
                     Rectangle bounds = chr.getBuffedEffect(skillid).calculateBoundingBox(summon.getTruePosition(), summon.isFacingLeft());
                     if (summon != null) {
                        Iterator var9 = chr.getMap().getAllMonster().iterator();

                        while(var9.hasNext()) {
                           MapleMonster mob = (MapleMonster)var9.next();
                           if (bounds.contains(mob.getTruePosition())) {
                              if (skillid == 13120007) {
                                 mob.applyStatus(chr.getClient(), MonsterStatus.MS_IndiePdr, new MonsterStatusEffect(skillid, 4000), -chr.getBuffedEffect(skillid).getW(), chr.getBuffedEffect(skillid));
                              }

                              mob.applyStatus(chr.getClient(), MonsterStatus.MS_Speed, new MonsterStatusEffect(skillid, 4000), -100, chr.getBuffedEffect(skillid));
                           }
                        }
                     }
                  }
               }

               if (GameConstants.isDemonAvenger(chr.getJob()) && chr.getSkillCustomValue(30010231) == null) {
                  Map<SecondaryStat, Pair<Integer, Integer>> cancelList = new HashMap();
                  cancelList.put(SecondaryStat.ExceedOverload, new Pair(1, 0));
                  chr.getClient().send(CWvsContext.BuffPacket.cancelBuff(cancelList, chr));
               }
            } while(chr.getSkillCustomValues().size() <= 0);

            Map<Integer, SkillCustomInfo> customInfo = new LinkedHashMap();
            customInfo.putAll(chr.getSkillCustomValues());
            Iterator var13 = customInfo.entrySet().iterator();

            while(true) {
               while(true) {
                  Entry sci;
                  do {
                     if (!var13.hasNext()) {
                        continue label132;
                     }

                     sci = (Entry)var13.next();
                  } while(!((SkillCustomInfo)sci.getValue()).canCancel(System.currentTimeMillis()));

                  chr.removeSkillCustomInfo((Integer)sci.getKey());
                  if ((Integer)sci.getKey() == 33001001) {
                     chr.getClient().send(CField.SummonPacket.JaguarAutoAttack(false));
                  } else if ((Integer)sci.getKey() == 9110 && (chr.getMapId() / 100000 == 9530 || chr.getMapId() / 100000 == 9540)) {
                     chr.warp(951000000);
                  } else if ((Integer)sci.getKey() != 24220 || chr.getMapId() != 450008950 && chr.getMapId() != 450008350) {
                     if ((Integer)sci.getKey() == 450001401 && MapleMap.this.getId() == 450001400) {
                     }
                  } else {
                     chr.playerIGDead();
                  }
               }
            }
         }
      }

      public void handleMists(long time) {
         List<MapleMist> toRemove = new ArrayList();
         Iterator mists = MapleMap.this.getAllMistsThreadsafe().iterator();

         while(mists.hasNext()) {
            MapleMist mistx = (MapleMist)mists.next();
            int endTime = mistx.getEndTime();
            int duration = mistx.getDuration();
            boolean isEnd = false;
            if (endTime > 0) {
               isEnd = time - mistx.getStartTime() >= (long)endTime;
            } else if (duration > 0) {
               isEnd = time - mistx.getStartTime() >= (long)duration;
            }

            if (isEnd) {
               if (mistx.getSourceSkill() != null) {
                  if (mistx.getSourceSkill().getId() == 400041008 && mistx.getDuration() != 1800) {
                     int spearSize = 0;
                     Iterator var10 = MapleMap.this.getAllMistsThreadsafe().iterator();

                     label89:
                     while(true) {
                        MapleMist mistz;
                        do {
                           if (!var10.hasNext()) {
                              MapleMist spear = new MapleMist(new Rectangle(mistx.getBox().x, mistx.getBox().y + 25, mistx.getBox().width, mistx.getBox().height + 25), mistx.getOwner(), SkillFactory.getSkill(spearSize >= 5 ? 400041008 : 400040008).getEffect(mistx.getSkillLevel()), 1800, (byte)0);
                              spear.setPosition(mistx.getTruePosition());
                              MapleMap.this.spawnMist(spear, false);
                              break label89;
                           }

                           mistz = (MapleMist)var10.next();
                        } while(mistz.getSourceSkill().getId() != 400040008 && mistz.getSourceSkill().getId() != 400041008);

                        ++spearSize;
                     }
                  } else if (mistx.getSourceSkill().getId() == 400051025) {
                     MapleMist icbm = new MapleMist(new Rectangle(mistx.getBox().x, mistx.getBox().y, mistx.getBox().width, mistx.getBox().height), mistx.getOwner(), SkillFactory.getSkill(400051026).getEffect(mistx.getSkillLevel()), 15000, (byte)0);
                     icbm.setPosition(mistx.getTruePosition());
                     icbm.setDelay(0);
                     MapleMap.this.spawnMist(icbm, false);
                  }
               }

               toRemove.add(mistx);
            } else if (mistx.getOwner() != null) {
               if (MapleMap.this.getCharacter(mistx.getOwner().getId()) == null) {
                  toRemove.add(mistx);
               }
            } else if (mistx.getMob() != null && MapleMap.this.getMonsterByOid(mistx.getMob().getObjectId()) == null) {
               toRemove.add(mistx);
            }

            if (mistx.getOwner() != null && !toRemove.contains(mistx)) {
               mistx.getOwner().checkMistStatus(mistx, MapleMap.this.getAllMonstersThreadsafe(), time);
            } else if (mistx.isMobMist() && mistx.getMob() != null) {
               MapleCharacter chrs = mistx.getMob().getController();
               if (chrs != null) {
                  chrs.checkMistStatus(mistx, MapleMap.this.getAllMonstersThreadsafe(), time);
               }
            }
         }

         Iterator var12 = toRemove.iterator();

         while(var12.hasNext()) {
            MapleMist mist = (MapleMist)var12.next();
            MapleMap.this.broadcastMessage(CField.removeMist(mist));
            MapleMap.this.removeMapObject(mist);
         }

      }

      public void handleMobs(long time) {
         if (MapleMap.this.canSpawn(time)) {
            MapleMap.this.respawn(false, time);
         }

         Iterator lifes;
         boolean damage;
         int rand;
         int itemid;
         Iterator pos2;
         if (MapleMap.this.getElitebossrewardtype() == 2 && MapleMap.this.isElitebossrewardmap() && MapleMap.this.getCustomValue(81111111) == null) {
            MapleMap.this.setCustomInfo(81111111, 0, Randomizer.rand(200, 1000));
            MapleCharacter chr = null;
            lifes = MapleMap.this.getAllChracater().iterator();
            if (lifes.hasNext()) {
               MapleCharacter chr2 = (MapleCharacter)lifes.next();
               chr = chr2;
            }

            pos2 = MapleMap.this.getAllMonster().iterator();

            label717:
            while(true) {
               MapleMonster mapleMonster;
               do {
                  if (!pos2.hasNext()) {
                     break label717;
                  }

                  mapleMonster = (MapleMonster)pos2.next();
               } while(mapleMonster.getId() != 8220028);

               int[] itemlist = new int[]{2432391, 2432392, 2432393, 2432394, 2432395, 2432396, 2432397};
               damage = false;

               for(rand = 0; rand < 4; ++rand) {
                  int Randomxx = (int)Math.floor(Math.random() * (double)itemlist.length);
                  itemid = itemlist[Randomxx];
                  Item toDrop = new Item(itemid, (short)0, (short)1, 0);
                  MapleMap.this.spawnFlyingDrop(chr, mapleMonster.getPosition(), mapleMonster.getPosition(), toDrop);
                  if (Randomizer.isSuccess(5)) {
                     toDrop = new Item(2432398, (short)0, (short)1, 0);
                     MapleMap.this.spawnFlyingDrop(chr, mapleMonster.getPosition(), mapleMonster.getPosition(), toDrop);
                  }
               }
            }
         }

         Iterator iterator;
         MapleCharacter chrxx;
         boolean inside;
         if (MapleMap.this.getElitebossrewardtype() == 3 && MapleMap.this.isElitebossrewardmap() && MapleMap.this.getCustomValue(81111111) == null) {
            MapleMap.this.setCustomInfo(81111111, 0, Randomizer.rand(200, 1000));
            int min_x = MapleMap.this.getLeft();
            int min_y = MapleMap.this.getBottom();
            int max_x = MapleMap.this.getRight();
            int max_y = MapleMap.this.getTop();
            iterator = MapleMap.this.getAllChracater().iterator();
            if (iterator.hasNext()) {
               chrxx = (MapleCharacter)iterator.next();
               int[] itemlistxx = new int[]{2432391, 2432392, 2432393, 2432394, 2432395, 2432396, 2432397};
               inside = false;

               for(int i = 0; i < Randomizer.rand(30, 60); ++i) {
                  Point pos = new Point(Randomizer.rand(min_x, max_x), max_y - 500);
                  itemid = (int)Math.floor(Math.random() * (double)itemlistxx.length);
                  int itemidx = itemlistxx[itemid];
                  Item toDropx = new Item(itemidx, (short)0, (short)1, 0);
                  MapleMap.this.spawnFlyingDrop(chrxx, pos, pos, toDropx);
                  if (Randomizer.isSuccess(4)) {
                     toDropx = new Item(2432398, (short)0, (short)1, 0);
                     MapleMap.this.spawnFlyingDrop(chrxx, pos, pos, toDropx);
                  }
               }
            }
         }

         Iterator var17;
         MapleCharacter allchr;
         if (MapleMap.this.getId() == 450008350 || MapleMap.this.getId() == 450008950) {
            var17 = MapleMap.this.getAllChracater().iterator();

            label688:
            while(true) {
               do {
                  if (!var17.hasNext()) {
                     break label688;
                  }

                  allchr = (MapleCharacter)var17.next();
                  allchr.getClient().getSession().writeAndFlush(MobPacket.BossWill.setMoonGauge(100, 25));
                  pos2 = allchr.getMap().getWillPoison().iterator();
               } while(allchr.getMap().getWillPoison().isEmpty());

               while(pos2.hasNext()) {
                  Point posx = (Point)pos2.next();
                  if (posx.x - 100 < allchr.getPosition().x && posx.x + 100 > allchr.getPosition().x && allchr.isAlive() && allchr.getBuffedEffect(SecondaryStat.NotDamaged) == null && allchr.getBuffedEffect(SecondaryStat.IndieNotDamaged) == null) {
                     allchr.addHP(-(allchr.getStat().getCurrentMaxHp() / 100L) * 44L);
                     allchr.getMap().broadcastMessage(CField.playerDamaged(allchr.getId(), (int)(allchr.getStat().getCurrentMaxHp() / 100L) * 44));
                  }
               }
            }
         }

         if (MapleMap.this.rune != null && !MapleMap.this.getAllCharactersThreadsafe().isEmpty() && MapleMap.this.isSpawnPoint() && !GameConstants.보스맵(MapleMap.this.mapid) && !GameConstants.isContentsMap(MapleMap.this.mapid) && MapleMap.this.getRuneCurse() < 4) {
            ++MapleMap.this.runeCurseTime;
            if (MapleMap.this.runeCurseTime >= 120) {
               MapleMap.this.runeCurseTime = 0;
               MapleMap.this.setRuneCurse(MapleMap.this.getRuneCurse() + 1);
               var17 = MapleMap.this.getAllCharactersThreadsafe().iterator();

               while(var17.hasNext()) {
                  allchr = (MapleCharacter)var17.next();
                  if (!allchr.getBuffedValue(80002282)) {
                     allchr.getClient().send(CField.runeCurse("룬을 해방하여 엘리트 보스의 저주를 풀어야 합니다!!\\n저주 " + MapleMap.this.getRuneCurse() + "단계 :  경험치 획득, 드롭률 " + MapleMap.this.getRuneCurseDecrease() + "% 감소 효과 적용 중", true));
                  }
               }
            }
         }

         MapleMonster monster;
         int typeed;
         if ((monster = MapleMap.this.getMonsterById(8220028)) != null && !MapleMap.this.isElitebossrewardmap()) {
            int[] itemlistx = new int[]{2432391, 2432392, 2432393, 2432394, 2432395, 2432396, 2432397};
            int Random = false;

            for(int ix = 0; ix < 3; ++ix) {
               int Randomx = (int)Math.floor(Math.random() * (double)itemlistx.length);
               typeed = itemlistx[Randomx];
               Item toDropxx = new Item(typeed, (short)0, (short)1, 0);
               MapleMap.this.spawnFlyingDrop(monster.getController(), monster.getPosition(), monster.getPosition(), toDropxx);
            }

            monster.addSkillCustomInfo(8220028, -1L);
            if (monster.getCustomValue0(8220028) <= 0L) {
               MapleMap.this.killMonsterType(monster, 1);
            }
         }

         if ((MapleMap.this.getMonsterById(8880100) != null || MapleMap.this.getMonsterById(8880101) != null || MapleMap.this.getMonsterById(8880110) != null || MapleMap.this.getMonsterById(8880111) != null) && time - MapleMap.this.lastIncinerateTime >= 30000L) {
            if (MapleMap.this.lastIncinerateTime != 0L) {
               MapleMap.this.spawnIncinerateObject(new MapleIncinerateObject(Randomizer.nextInt(MapleMap.this.getRight() - MapleMap.this.getLeft()) + MapleMap.this.getLeft(), 16));
            }

            MapleMap.this.lastIncinerateTime = time;
         }

         lifes = MapleMap.this.getAllMonstersThreadsafe().iterator();

         while(lifes.hasNext()) {
            MapleMonster life = (MapleMonster)lifes.next();
            if (life.getCustomValues().size() > 0) {
               Map<Integer, SkillCustomInfo> customInfo = new LinkedHashMap();
               customInfo.putAll(life.getCustomValues());
               iterator = customInfo.entrySet().iterator();

               while(iterator.hasNext()) {
                  Entry<Integer, SkillCustomInfo> sci = (Entry)iterator.next();
                  if (((SkillCustomInfo)sci.getValue()).canCancel(System.currentTimeMillis())) {
                     life.removeCustomInfo((Integer)sci.getKey());
                     if ((Integer)sci.getKey() == 400011122) {
                        life.removeCustomInfo(400011121);
                     } else if ((Integer)sci.getKey() == 24205) {
                        MobSkill msi = MobSkillFactory.getMobSkill(242, 5);
                        msi.applyEffect((MapleCharacter)null, life, true, life.isFacingLeft());
                        life.setCustomInfo(24205, 0, 120000);
                     }
                  }
               }
            }

            List<Pair<MonsterStatus, MonsterStatusEffect>> cancelsf = new ArrayList();
            iterator = life.getStati().iterator();

            while(iterator.hasNext()) {
               Pair<MonsterStatus, MonsterStatusEffect> cancel = (Pair)iterator.next();
               if (!MapleMap.this.getAllChracater().contains(((MonsterStatusEffect)cancel.getRight()).getChr()) && !((MonsterStatusEffect)cancel.getRight()).isMobskill()) {
                  cancelsf.add(new Pair((MonsterStatus)cancel.getLeft(), (MonsterStatusEffect)cancel.getRight()));
                  break;
               }
            }

            if (!cancelsf.isEmpty()) {
               life.cancelStatus(cancelsf);
            }

            if ((life.getId() == 8950100 || life.getId() == 8950000) && life.getBuff(MonsterStatus.MS_Laser) != null && !life.isSkillForbid()) {
               if (Randomizer.isSuccess(1) && life.getCustomValue(2287) == null && life.getCustomValue(22878) == null) {
                  MobSkillFactory.getMobSkill(228, 7).applyEffect(life.getController(), life, true, true);
               }

               if (Randomizer.isSuccess(3) && life.getCustomValue(2286) == null && life.getCustomValue(2287) == null) {
                  MobSkillFactory.getMobSkill(228, 6).applyEffect(life.getController(), life, true, true);
                  life.setCustomInfo(2286, 0, 60000);
               } else {
                  int plus = 9;
                  typeed = plus * life.getEnergyspeed();
                  if (life.isEnergyleft() && typeed > 0) {
                     typeed *= -1;
                  }

                  life.setEnergycount(life.getEnergycount() + typeed);
                  if (life.isEnergyleft()) {
                     if (life.getEnergycount() <= 0) {
                        life.setEnergycount(360 - life.getEnergycount());
                     }
                  } else if (life.getEnergycount() >= 360) {
                     life.setEnergycount(life.getEnergycount() - 360);
                  }

                  life.getMap().broadcastMessage(MobPacket.LaserHandler(life.getObjectId(), life.getEnergycount(), life.getEnergyspeed(), life.isEnergyleft() ? 0 : 1));
               }
            }

            boolean damaged;
            HashMap statups;
            if (life.getId() == 8880503) {
               iterator = MapleMap.this.getAllChracater().iterator();

               while(iterator.hasNext()) {
                  chrxx = (MapleCharacter)iterator.next();
                  if (chrxx != null) {
                     inside = false;
                     damaged = false;
                     byte phase;
                     if (life.getHPPercent() <= 33) {
                        phase = 3;
                     } else if (life.getHPPercent() <= 66) {
                        phase = 2;
                     } else {
                        phase = 1;
                     }

                     if (life.getPhase() != phase) {
                        life.setPhase((byte)phase);
                        MapleMap.this.broadcastMessage(MobPacket.changePhase(life));
                        MapleMap.this.broadcastMessage(MobPacket.changeMobZone(life));
                     }

                     short pix;
                     if (phase == 3) {
                        pix = 160;
                     } else if (phase == 2) {
                        pix = 275;
                     } else {
                        pix = 410;
                     }

                     if (chrxx.getSkillCustomValue(143144) == null) {
                        if (chrxx.getTruePosition().getX() > life.getTruePosition().getX() - (double)pix && chrxx.getTruePosition().getX() < life.getTruePosition().getX() + (double)pix) {
                           inside = true;
                        }

                        statups = new HashMap();
                        statups.put(SecondaryStat.MobZoneState, new Pair(inside ? 1 : 0, life.getObjectId()));
                        chrxx.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, (SecondaryStatEffect)null, chrxx.getPlayer()));
                        chrxx.setSkillCustomInfo(143145, !inside ? 1L : 0L, 0L);
                        if (!inside) {
                           chrxx.setSkillCustomInfo(143144, 0L, 100L);
                        }
                     }
                  }
               }
            }

            Iterator var43;
            MapleMonster m;
            MapleMonster monster1;
            Iterator var50;
            MapleMonster mx;
            if (life.getId() == 8870100) {
               if (life.getCustomValue(8870100) != null && life.getCustomValue0(8870100) > 0L) {
                  life.addSkillCustomInfo(8870100, -1L);
                  List<Integer> list = new ArrayList();
                  var43 = MapleMap.this.getAllMonster().iterator();

                  while(var43.hasNext()) {
                     monster1 = (MapleMonster)var43.next();
                     if (monster1.getId() != 8870100) {
                        list.add(monster1.getObjectId());
                     }
                  }

                  if (list.size() > 0) {
                     long healhp = life.getStats().getHp() / 100L * 5L * (long)list.size();
                     if (healhp + life.getHp() >= life.getStats().getHp()) {
                        life.setHp(life.getStats().getHp());
                     } else {
                        life.setHp(life.getHp() + healhp);
                     }

                     MapleMap.this.broadcastMessage(MobPacket.HillaDrainEffect(life.getObjectId(), list));
                     MapleMap.this.broadcastMessage(MobPacket.HillaDrainActive(life.getObjectId()));
                     MapleMap.this.broadcastMessage(MobPacket.showBossHP(life));
                  }
               }

               typeed = 0;
               m = null;
               var50 = MapleMap.this.getAllMonster().iterator();

               while(var50.hasNext()) {
                  mx = (MapleMonster)var50.next();
                  if (mx.getId() == 8870107) {
                     ++typeed;
                  } else if (mx.getId() == 8870100) {
                     m = mx;
                  }
               }

               if (typeed <= 0 && m != null) {
                  m.setLastSkillUsed(MobSkillFactory.getMobSkill(200, 251), System.currentTimeMillis(), (long)(Randomizer.rand(30, 40) * 1000));

                  while(m.getBuff(MonsterStatus.MS_ExchangeAttack) != null) {
                     m.cancelStatus(MonsterStatus.MS_ExchangeAttack, m.getBuff(MonsterStatus.MS_ExchangeAttack));
                  }
               }
            }

            MapleMonster demian;
            if (MapleMap.this.getId() == 240060200 || MapleMap.this.getId() == 240060201) {
               demian = null;
               damage = false;
               var50 = MapleMap.this.getAllMonster().iterator();

               while(var50.hasNext()) {
                  mx = (MapleMonster)var50.next();
                  if (mx.getId() >= 8810002 && mx.getId() <= 8810009 || mx.getId() >= 8810102 && mx.getId() <= 8810109) {
                     damage = false;
                     break;
                  }

                  if (mx.getId() == 8810018 || mx.getId() == 8810122) {
                     demian = mx;
                     damage = true;
                  }
               }

               if (damage) {
                  MapleMap.this.killMonster(demian, demian.getController(), true, true, (byte)0);
               }
            }

            MapleCharacter chrx;
            if (MapleMap.this.getId() == 450008750) {
               iterator = MapleMap.this.getAllMonster().iterator();

               label555:
               while(true) {
                  do {
                     while(true) {
                        do {
                           do {
                              if (!iterator.hasNext()) {
                                 break label555;
                              }

                              m = (MapleMonster)iterator.next();
                           } while(m.getId() == 8880315);
                        } while(m.getId() == 8880316);

                        if (m.getPosition().y > 0) {
                           break;
                        }

                        if (m.getController().getPosition().y > 0) {
                           var50 = MapleMap.this.getAllChracater().iterator();

                           while(var50.hasNext()) {
                              chrx = (MapleCharacter)var50.next();
                              if (chrx.getPosition().y < 0) {
                                 m.switchController(chrx, true);
                              }
                           }
                        }
                     }
                  } while(m.getController().getPosition().y >= 0);

                  var50 = MapleMap.this.getAllChracater().iterator();

                  while(var50.hasNext()) {
                     chrx = (MapleCharacter)var50.next();
                     if (chrx.getPosition().y > 0) {
                        m.switchController(chrx, true);
                     }
                  }
               }
            }

            if (life.getId() == 8880315 || life.getId() == 8880316) {
               if (life.getPosition().y > -2300 && life.getPosition().y < -2020) {
                  if (life.getController().getPosition().y > 0) {
                     iterator = life.getMap().getAllChracater().iterator();

                     while(iterator.hasNext()) {
                        chrxx = (MapleCharacter)iterator.next();
                        if (chrxx != null && chrxx.getPosition().y < -2000) {
                           life.switchController(chrxx, false);
                           break;
                        }
                     }
                  }
               } else if (life.getPosition().y < 300 && life.getPosition().y > -500 && life.getController().getPosition().y < 0) {
                  iterator = life.getMap().getAllChracater().iterator();

                  while(iterator.hasNext()) {
                     chrxx = (MapleCharacter)iterator.next();
                     if (chrxx != null && chrxx.getPosition().y > 0) {
                        life.switchController(chrxx, false);
                        break;
                     }
                  }
               }
            }

            if (life.getId() == 8880102) {
               if (life.getCustomValue(8880102) == null && life.getController() != null && MapleMap.this.getAllChracater().size() > 1) {
                  MapleCharacter other = null;
                  var43 = MapleMap.this.getAllChracater().iterator();

                  while(var43.hasNext()) {
                     MapleCharacter chrs = (MapleCharacter)var43.next();
                     if (chrs.getId() != life.getId()) {
                        other = chrs;
                        break;
                     }
                  }

                  if (other != null) {
                     life.setCustomInfo(8880102, 0, 10000);
                     life.switchController(other, true);
                  }
               }

               demian = null;
               var43 = MapleMap.this.getAllMonster().iterator();

               while(var43.hasNext()) {
                  monster1 = (MapleMonster)var43.next();
                  if (monster1.getId() == 8880101 || monster1.getId() == 8880111) {
                     demian = monster1;
                     break;
                  }
               }

               if (demian != null) {
                  short pixx;
                  if (demian.getPhase() == 2) {
                     pixx = 100;
                  } else if (demian.getPhase() == 3) {
                     pixx = 130;
                  } else if (demian.getPhase() == 4) {
                     pixx = 150;
                  } else {
                     pixx = 50;
                  }

                  var50 = MapleMap.this.getAllCharactersThreadsafe().iterator();

                  while(var50.hasNext()) {
                     chrx = (MapleCharacter)var50.next();
                     if (chrx.getSkillCustomValue(143144) == null) {
                        damaged = false;
                        if (chrx.getTruePosition().getX() > life.getTruePosition().getX() - (double)pixx && chrx.getTruePosition().getX() < life.getTruePosition().getX() + (double)pixx) {
                           damaged = true;
                        }

                        statups = new HashMap();
                        statups.put(SecondaryStat.MobZoneState, new Pair(damaged ? 1 : 0, demian.getObjectId()));
                        chrx.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, (SecondaryStatEffect)null, chrx.getPlayer()));
                        chrx.setSkillCustomInfo(143143, damaged ? 1L : 0L, 0L);
                        if (damaged) {
                           chrx.getPercentDamage(demian, 999, 999, 5, true);
                           chrx.setSkillCustomInfo(143144, 0L, 100L);
                        }
                     }
                  }
               }
            }

            if ((life.getId() == 8500001 || life.getId() == 8500002 || life.getId() == 8500011 || life.getId() == 8500012 || life.getId() == 8500021 || life.getId() == 8500022) && life != null) {
               if (life.getPatten() > 0) {
                  life.SetPatten(life.getPatten() - 1);
                  if (MapleMap.this.PapulratusPatan == 0) {
                     if (life.getPatten() <= 0) {
                        MapleMap.this.PapulratusPatan = 1;
                        MapleMap.this.PapulratusTime = 0;
                        life.SetPatten(30);
                        MapleMap.this.broadcastMessage(CField.ActivePotionCooldown(6));
                        MapleMap.this.broadcastMessage(MobPacket.SpeakingMonster(life, life.getId() != 8500001 && life.getId() != 8500011 && life.getId() != 8500021 ? 2 : 1, 0));
                        MapleMap.this.broadcastMessage(MobPacket.BossPapuLatus.PapulLatusLaser(true, 0));
                        MapleMap.this.broadcastMessage(MobPacket.setAttackZakumArm(life.getObjectId(), 4));
                        MapleMap.this.broadcastMessage(MobPacket.BossPapuLatus.PapulLatusTime(life.getPatten() * 1000, true, false));
                     }
                  } else if (MapleMap.this.PapulratusPatan == 1) {
                     if (life.getPatten() <= 0) {
                        MapleMap.this.PapulratusPatan = 0;
                        MapleMap.this.PapulratusTime = 50;
                        life.SetPatten(120);
                        MapleMap.this.broadcastMessage(CField.ActivePotionCooldown(0));
                        MapleMap.this.broadcastMessage(MobPacket.setAttackZakumArm(life.getObjectId(), 5));
                        typeed = life.getId() % 100 / 10;
                        String difical = typeed == 0 ? "Easy" : (typeed == 1 ? "Normal" : "Chaos");
                        rand = difical.equals("Easy") ? Randomizer.rand(1, 2) : Randomizer.rand(1, 3);
                        MapleMap.this.broadcastMessage(MobPacket.BossPapuLatus.PapulLatusLaser(false, rand));
                        MapleMap.this.broadcastMessage(MobPacket.BossPapuLatus.PapulLatusTime(life.getPatten() * 1000, false, false));
                     } else if (life.getSeperateSoul() <= 0 && life.getMap().PapulratusPatan == 1 && life.getCustomValue(8500001) == null) {
                        MapleMap.this.broadcastMessage(MobPacket.BossPapuLatus.dropPaPul());
                        life.setCustomInfo(8500001, 0, 2000);
                     }
                  }
               }

               if (MapleMap.this.getId() == 105200120 || MapleMap.this.getId() == 105200510 || MapleMap.this.getId() == 105200510 || MapleMap.this.PapulratusPatan == 1) {
                  iterator = MapleMap.this.getAllCharactersThreadsafe().iterator();

                  while(iterator.hasNext()) {
                     chrxx = (MapleCharacter)iterator.next();
                     if (chrxx.isAlive() && chrxx.getBuffedValue(SecondaryStat.NotDamaged) == null && chrxx.getBuffedValue(SecondaryStat.IndieNotDamaged) == null) {
                        chrxx.addHP(-(chrxx.getStat().getCurrentMaxHp() * 10L) / 100L);
                        chrxx.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chrxx, 0, (int)(-(chrxx.getStat().getCurrentMaxHp() * 10L)) / 100, 36, 0, 0, (byte)0, true, (Point)null, (String)null, (Item)null));
                        chrxx.getMap().broadcastMessage(chrxx, CField.EffectPacket.showEffect(chrxx, 0, (int)(-(chrxx.getStat().getCurrentMaxHp() * 10L)) / 100, 36, 0, 0, (byte)0, false, (Point)null, (String)null, (Item)null), false);
                     }
                  }
               }
            }

            if (life.isBuffed(MonsterStatus.MS_Burned)) {
               MonsterStatusEffect buff = life.getBuff(MonsterStatus.MS_Burned);
               if (buff != null && life.getHp() > 1L && buff.getInterval() > 0 && buff.getChr() != null) {
                  damage = true;
                  if (buff.getInterval() > 1000 && time - buff.getLastPoisonTime() < (long)buff.getInterval()) {
                     damage = false;
                  }

                  if (damage) {
                     buff.setLastPoisonTime(time);
                     life.damage(buff.getChr(), Math.min(life.getHp() - 1L, buff.getValue()), true);
                     if (buff.getSkill() == 25121006 && buff.getChr().getSkillLevel(25121006) > 0 && buff.getChr().isAlive()) {
                        buff.getChr().getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(buff.getChr(), 0, 25121006, 4, 0, 0, (byte)(buff.getChr().isFacingLeft() ? 1 : 0), true, buff.getChr().getPosition(), (String)null, (Item)null));
                        buff.getChr().getMap().broadcastMessage(buff.getChr(), CField.EffectPacket.showEffect(buff.getChr(), 0, 25121006, 4, 0, 0, (byte)(buff.getChr().isFacingLeft() ? 1 : 0), false, buff.getChr().getPosition(), (String)null, (Item)null), false);
                        buff.getChr().addHP(buff.getValue() * (long)SkillFactory.getSkill(buff.getSkill()).getEffect(buff.getChr().getSkillLevel(25121006)).getX() / 100L);
                     }
                  }
               }
            }
         }

      }

      public void run() {
         long time = System.currentTimeMillis();
         if (MapleMap.this.characterSize() > 0) {
            MapleMap.this.checkDropItems(time);
            this.handleMobs(time);
            this.handleCharacters(time);
            this.handleMists(time);
            if (MapleMap.this.BattleGroundMainTimer > 0) {
               --MapleMap.this.BattleGroundMainTimer;
               if (MapleMap.this.BattleGroundMainTimer == 539) {
                  MapleMap.this.broadcastMessage(CField.ImageTalkNpc(9001153, 5000, "무적 상태가 해제 되었습니다. 플레이어들끼리 전투가 가능합니다!"));
                  BattleGroundGameHandler.setNotDamage(false);
               } else if (MapleMap.this.BattleGroundMainTimer == 330) {
                  MapleMap.this.broadcastMessage(CField.environmentChange("Map/Effect2.img/PvP/Boss", 16));
                  MapleMap.this.broadcastMessage(CField.ImageTalkNpc(9001153, 5000, "보스존에 #r홀로 드래곤#k이 스폰 되었습니다. 처치시 막대한 #b경험치와 골드#k를 얻을 수 있습니다!"));
                  MapleMap.this.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9303206), new Point(-380, -58));
               } else if (MapleMap.this.BattleGroundMainTimer == 4) {
                  MapleMap.this.broadcastMessage(CField.environmentChange("Map/Effect2.img/PvP/Start", 16));
                  MapleMap.this.broadcastMessage(SLFCGPacket.playSE("Sound/MiniGame.img/BattlePvp/Start"));
               }

               if (MapleMap.this.BattleGroundMainTimer <= 0) {
                  MapleMap.this.BattleGroundMainTimer = 0;
                  BattleGroundGameHandler.EndPlayGamez(MapleMap.this);
               }
            }
         }

         this.handleBurningStatus(time);
         if (MapleMap.this.PapulratusTime > 0) {
            --MapleMap.this.PapulratusTime;
            if (MapleMap.this.PapulratusTime == 10) {
               MapleMap.this.broadcastMessage(MobPacket.BossPapuLatus.PapulLatusLaser(true, 0));
            }

            if (MapleMap.this.PapulratusTime <= 0) {
               MapleMap.this.PapulratusTime = 0;
               if (MapleMap.this.getAllMonster().size() > 0 && MapleMap.this.PapulratusPatan == 0) {
                  MapleMap.this.PapulratusTime = 50;
                  MapleMap.this.broadcastMessage(MobPacket.BossPapuLatus.PapulLatusLaser(false, Randomizer.rand(1, 3)));
               }
            }
         }

         if (MapleMap.this.Mapcoltime > 0) {
            --MapleMap.this.Mapcoltime;
            if (MapleMap.this.Mapcoltime <= 0) {
               MapleMap.this.Mapcoltime = 0;
            }
         }

         if (MapleMap.this.BattleGroundTimer > 0) {
            --MapleMap.this.BattleGroundTimer;
            if (MapleMap.this.BattleGroundTimer <= 0) {
               MapleMap.this.BattleGroundTimer = 0;
               BattleGroundGameHandler.StartGame(MapleMap.this);
            }

            MapleMap.this.broadcastMessage(BattleGroundPacket.SelectAvaterClock(MapleMap.this.BattleGroundTimer));
         }

         if (MapleMap.this.getCustomValues().size() > 0) {
            Map<Integer, SkillCustomInfo> customInfo = new LinkedHashMap();
            customInfo.putAll(MapleMap.this.getCustomValues());
            Iterator iterator = customInfo.entrySet().iterator();

            label195:
            while(true) {
               int cooltime;
               int endtime;
               ArrayList remove;
               do {
                  while(true) {
                     Entry sci;
                     do {
                        do {
                           if (!iterator.hasNext()) {
                              return;
                           }

                           sci = (Entry)iterator.next();
                        } while(!((SkillCustomInfo)sci.getValue()).canCancel(System.currentTimeMillis()));

                        MapleMap.this.removeCustomInfo((Integer)sci.getKey());
                     } while(MapleMap.this.getChannel() != 1);

                     if ((Integer)sci.getKey() == 921174101) {
                        break;
                     }

                     if ((Integer)sci.getKey() == 993192000) {
                        remove = new ArrayList();
                        remove.add(new Pair("bloomingSun", 0));
                        remove.add(new Pair("bloomingWind", 0));
                        MapleMap.this.broadcastMessage(CField.ChangeSpecialMapEffect(remove));
                        MapleMap.this.broadcastMessage(CField.removeMapEffect());
                        MapleMap.this.broadcastMessage(CField.setSpecialMapEffect("bloomingSun", 0, 0));
                        MapleMap.this.broadcastMessage(CField.setSpecialMapEffect("bloomingWind", 0, 0));
                     } else {
                        Iterator var13;
                        MapleCharacter chrx;
                        if ((Integer)sci.getKey() == 993192500) {
                           if (MapleMap.this.getCharactersSize() < 5) {
                              var13 = MapleMap.this.getAllChracater().iterator();

                              while(var13.hasNext()) {
                                 chrx = (MapleCharacter)var13.next();
                                 chrx.warp(Integer.parseInt(chrx.getPlayer().getV("returnM")) > 0 ? Integer.parseInt(chrx.getPlayer().getV("returnM")) : 993192000);
                                 chrx.dropMessage(5, "<블루밍 레이스> 게임 시작에 필요한 인원이 부족하여 게임이 종료 됩니다.");
                              }

                              return;
                           }

                           BloomingRace.ReadyRace();
                        } else if ((Integer)sci.getKey() == 993192600) {
                           MapleMap.this.broadcastMessage(SLFCGPacket.BloomingRaceHandler(3));
                           Timer.EventTimer.getInstance().schedule(() -> {
                              BloomingRace.StartRace();
                           }, 4000L);
                        } else if ((Integer)sci.getKey() == 993192601) {
                           var13 = MapleMap.this.getAllChracater().iterator();

                           while(var13.hasNext()) {
                              chrx = (MapleCharacter)var13.next();
                              chrx.warp(993192501);
                           }
                        } else if ((Integer)sci.getKey() != 993192602 && (Integer)sci.getKey() != 993192603 && (Integer)sci.getKey() != 993192604) {
                           if ((Integer)sci.getKey() == 993026900) {
                              if (MapleMap.this.getCharactersSize() < 4) {
                                 var13 = MapleMap.this.getAllChracater().iterator();

                                 while(var13.hasNext()) {
                                    chrx = (MapleCharacter)var13.next();
                                    chrx.warp(Integer.parseInt(chrx.getPlayer().getV("returnM")));
                                    chrx.dropMessage(5, "<싸워라! 전설의 귀환> 게임 시작에 필요한 인원이 부족하여 게임이 종료 됩니다.");
                                 }

                                 return;
                              }

                              BattleGroundGameHandler.Ready();
                           } else if ((Integer)sci.getKey() == 921174100) {
                              remove = new ArrayList();
                              Timer.EtcTimer.getInstance().schedule(() -> {
                                 List<MapleBattleGroundCharacter> list = new ArrayList();
                                 Iterator var3 = MapleMap.this.getAllChracater().iterator();

                                 MapleBattleGroundCharacter a;
                                 while(var3.hasNext()) {
                                    MapleCharacter chr = (MapleCharacter)var3.next();
                                    if (chr.getBattleGroundChr() != null) {
                                       a = chr.getBattleGroundChr();
                                       if (a.isAlive()) {
                                          list.add(a);
                                       }

                                       remove.add(a);
                                    }
                                 }

                                 Collections.sort(list);
                                 int i = 1;

                                 for(Iterator var7 = list.iterator(); var7.hasNext(); ++i) {
                                    a = (MapleBattleGroundCharacter)var7.next();
                                    a.getChr().addKV("BattlePVPRank", i.makeConcatWithConstants<invokedynamic>(i));
                                    a.getChr().addKV("BattlePVPLevel", a.getLevel().makeConcatWithConstants<invokedynamic>(a.getLevel()));
                                    a.getChr().addKV("BattlePVPKill", a.getKill().makeConcatWithConstants<invokedynamic>(a.getKill()));
                                 }

                                 MapleMap.this.broadcastMessage(CField.UIPacket.detailShowInfo("게임이 종료 되었습니다 잠시 후 퇴장맵으로 이동 됩니다.", 3, 20, 20));
                                 MapleMap.this.broadcastMessage(CField.getClock(5));
                              }, 2000L);
                              Timer.EtcTimer.getInstance().schedule(() -> {
                                 MapleMap.this.resetFully();
                                 Iterator var1 = MapleMap.this.getAllChracater().iterator();

                                 while(var1.hasNext()) {
                                    MapleCharacter chr = (MapleCharacter)var1.next();
                                    chr.dispel();
                                    chr.warp(921174002);
                                 }

                                 MapleBattleGroundCharacter.bchr.clear();
                              }, 4000L);
                           }
                        } else if (MapleMap.this.getCharactersSize() > 0) {
                           String name = (Integer)sci.getKey() == 993192602 ? "trick01" : ((Integer)sci.getKey() == 993192603 ? "trick02" : "trick03");
                           List<String> str = Arrays.asList(name);
                           List<Integer> foothold = new ArrayList();
                           cooltime = (Integer)sci.getKey() == 993192602 ? 4300 : ((Integer)sci.getKey() == 993192603 ? 3500 : 3500);
                           switch((Integer)sci.getKey()) {
                           case 993192602:
                              foothold.add(726);
                              foothold.add(727);
                              foothold.add(728);
                              foothold.add(729);
                              foothold.add(736);
                              foothold.add(737);
                              break;
                           case 993192603:
                              foothold.add(681);
                              foothold.add(682);
                              foothold.add(683);
                              break;
                           case 993192604:
                              foothold.add(649);
                              foothold.add(650);
                              foothold.add(656);
                              foothold.add(716);
                              foothold.add(717);
                              foothold.add(718);
                           }

                           endtime = (Integer)sci.getKey() == 993192602 ? 2500 : ((Integer)sci.getKey() == 993192603 ? 3500 : 3500);
                           MapleMap.this.broadcastMessage(SLFCGPacket.FootHoldOnOff(foothold, true));
                           MapleMap.this.broadcastMessage(SLFCGPacket.FootHoldOnOffEffect(str, true));
                           Timer.EventTimer.getInstance().schedule(() -> {
                              MapleMap.this.setCustomInfo((Integer)sci.getKey(), 0, endtime);
                              MapleMap.this.broadcastMessage(SLFCGPacket.FootHoldOnOff(foothold, false));
                              MapleMap.this.broadcastMessage(SLFCGPacket.FootHoldOnOffEffect(str, false));
                           }, (long)cooltime);
                        }
                     }
                  }
               } while(MapleMap.this.getCustomValue0(921174102) >= 4);

               remove = new ArrayList();
               int basex1 = -1401;
               int basex2 = 380;
               cooltime = basex1 + MapleMap.this.getCustomValue0(921174102) * 165;
               endtime = basex2 - MapleMap.this.getCustomValue0(921174102) * 165;
               MapleMap.this.broadcastMessage(CField.getFieldSkillAdd(100008, 1, true));
               MapleMap.this.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880507), new Point(basex1 + MapleMap.this.getCustomValue0(921174102) * 165, -1423));
               MapleMap.this.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880508), new Point(basex2 - MapleMap.this.getCustomValue0(921174102) * 165, -1423));
               MapleMap.this.setCustomInfo(921174102, MapleMap.this.getCustomValue0(921174102) + 1, 0);
               Iterator var11 = MapleMap.this.getAllChracater().iterator();

               while(true) {
                  MapleCharacter chr;
                  do {
                     if (!var11.hasNext()) {
                        if (MapleMap.this.getCustomValue0(921174102) < 4) {
                           remove.add(new Point(basex1 + MapleMap.this.getCustomValue0(921174102) * 165, -1423));
                           remove.add(new Point(basex2 - MapleMap.this.getCustomValue0(921174102) * 165, -1423));
                           MapleMap.this.broadcastMessage(CField.getFieldSkillEffectAdd(100008, 1, remove));
                           MapleMap.this.setCustomInfo(921174101, 0, 15000);
                        }
                        continue label195;
                     }

                     chr = (MapleCharacter)var11.next();
                  } while(cooltime <= chr.getPosition().x && endtime >= chr.getPosition().x);

                  chr.getClient().send(CField.fireBlink(chr.getId(), new Point(-530, -1423)));
               }
            }
         }
      }
   }

   private interface DelayedPacketCreation {
      void sendPackets(MapleClient var1);
   }

   private class ActivateItemReactor implements Runnable {
      private MapleMapItem mapitem;
      private MapleReactor reactor;
      private MapleClient c;

      public ActivateItemReactor(MapleMapItem mapitem, MapleReactor reactor, MapleClient c) {
         this.mapitem = mapitem;
         this.reactor = reactor;
         this.c = c;
      }

      public void run() {
         if (this.mapitem != null && this.mapitem == MapleMap.this.getMapObject(this.mapitem.getObjectId(), this.mapitem.getType()) && !this.mapitem.isPickedUp()) {
            this.mapitem.expire(MapleMap.this);
            this.reactor.hitReactor(this.c);
            this.reactor.setTimerActive(false);
            if (this.reactor.getDelay() > 0) {
               Timer.MapTimer.getInstance().schedule(new Runnable() {
                  // $FF: synthetic field
                  final MapleMap.ActivateItemReactor this$1;

                  {
                     this.this$1 = this$1;
                  }

                  public void run() {
                     this.this$1.reactor.forceHitReactor((byte)0, this.this$1.c.getPlayer().getId());
                  }
               }, (long)this.reactor.getDelay());
            }
         } else {
            this.reactor.setTimerActive(false);
         }

      }
   }
}
