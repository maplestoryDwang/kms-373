package server.life;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleDiseases;
import client.MapleTrait;
import client.SecondaryStat;
import client.Skill;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import handling.channel.ChannelServer;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import java.awt.Point;
import java.awt.Rectangle;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import scripting.EventInstanceManager;
import server.MapleItemInformationProvider;
import server.Obstacle;
import server.Randomizer;
import server.SecondaryStatEffect;
import server.SkillCustomInfo;
import server.Timer;
import server.field.boss.FieldSkillFactory;
import server.field.boss.lotus.MapleEnergySphere;
import server.maps.FieldLimitType;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleNodes;
import server.polofritto.MapleRandomPortal;
import tools.Pair;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.MobPacket;
import tools.packet.SLFCGPacket;

public class MapleMonster extends AbstractLoadedMapleLife {
   private MapleMonsterStats stats;
   private ChangeableStats ostats = null;
   private long hp;
   private long nextKill = 0L;
   private long lastDropTime = 0L;
   private long barrier = 0L;
   private int mp;
   private byte carnivalTeam = -1;
   private byte phase = 0;
   private byte bigbangCount = 0;
   private MapleMap map;
   private WeakReference<MapleMonster> sponge = new WeakReference((Object)null);
   private int linkoid = 0;
   private int lastNode = -1;
   private int highestDamageChar = 0;
   private int linkCID = 0;
   private WeakReference<MapleCharacter> controller = new WeakReference((Object)null);
   private boolean fake = false;
   private boolean dropsDisabled = false;
   private boolean controllerHasAggro = false;
   private boolean demianChangePhase = false;
   private boolean extreme = false;
   private boolean hellMode = false;
   private final List<MapleMonster.AttackerEntry> attackers = new CopyOnWriteArrayList();
   private EventInstanceManager eventInstance;
   private MonsterListener listener = null;
   private byte[] reflectpack = null;
   private byte[] nodepack = null;
   private List<Pair<MonsterStatus, MonsterStatusEffect>> stati = new CopyOnWriteArrayList();
   private final LinkedList<MonsterStatusEffect> poisons = new LinkedList();
   private Map<MobSkill, Long> usedSkills = new HashMap();
   private int stolen = -1;
   private int seperateSoul = 0;
   private int airFrame = 0;
   private boolean shouldDropItem = false;
   private boolean killed = false;
   private boolean isseperated = false;
   private boolean isMobGroup = false;
   private boolean isSkillForbid = false;
   private boolean useSpecialSkill = false;
   private long lastReceivedMovePacket = System.currentTimeMillis();
   private long spawnTime = 0L;
   private long shield = 0L;
   private long shieldmax = 0L;
   private long lastBindTime = 0L;
   private long lastCriticalBindTime = 0L;
   private long elitehp = 0L;
   public int blizzardTempest = 0;
   private long lastSpecialAttackTime = System.currentTimeMillis();
   private long lastSeedCountedTime = System.currentTimeMillis();
   public long lastDistotionTime = System.currentTimeMillis();
   public long lastCapTime = 0L;
   public long astObstacleTime = System.currentTimeMillis();
   public long lastLaserTime = System.currentTimeMillis();
   public long lastObstacleTime = System.currentTimeMillis();
   public long lastRedObstacleTime = System.currentTimeMillis();
   public long lastChainTime = System.currentTimeMillis();
   public long lastSpearTime = System.currentTimeMillis();
   public long lastThunderTime = System.currentTimeMillis();
   public long lastEyeTime = System.currentTimeMillis();
   public long lastBWThunder = System.currentTimeMillis();
   public long lastBWBliThunder = System.currentTimeMillis();
   private int nextSkill = 0;
   private int nextSkillLvl = 0;
   private int freezingOverlap = 0;
   private int curseBound = 0;
   private int patten;
   private int owner = -1;
   private int scale = 100;
   private int eliteGrade = -1;
   private int eliteType = 0;
   private int spiritGate = 0;
   private int anotherByte = 0;
   private List<Integer> spawnList = new ArrayList();
   private List<Integer> willHplist = new ArrayList();
   private List<Ignition> ignitions = new CopyOnWriteArrayList();
   private List<MapleEnergySphere> spheres = new ArrayList();
   private List<Pair<Integer, Integer>> eliteGradeInfo = new ArrayList();
   private Map<Integer, Rectangle> rectangles = new LinkedHashMap();
   private ScheduledFuture<?> schedule = null;
   private transient Map<Integer, SkillCustomInfo> customInfo = new LinkedHashMap();
   private List<MonsterStatusEffect> indielist = new ArrayList();
   private boolean elitemonster;
   private boolean elitechmp;
   private boolean eliteboss;
   private boolean userunespawn;
   private String specialtxt;
   private int energycount;
   private int energyspeed;
   private int StigmaType;
   private int TotalStigma;
   private int SerenTimetype;
   private int SerenNoonTotalTime;
   private int SerenSunSetTotalTime;
   private int SerenMidNightSetTotalTime;
   private int SerenDawnSetTotalTime;
   private int SerenNoonNowTime;
   private int SerenSunSetNowTime;
   private int SerenMidNightSetNowTime;
   private int SerenDawnSetNowTime;
   private boolean energyleft;
   private boolean willSpecialPattern = false;
   private long lastStoneTime = System.currentTimeMillis();

   public MapleMonster(MapleMonster monster) {
      super(monster);
      this.initWithStats(monster.stats);
   }

   public MapleMonster(int id, MapleMonsterStats stats) {
      super(id);
      this.initWithStats(stats);
   }

   public MapleMonster(int id, MapleMonsterStats stats, boolean extreme, boolean hellMode) {
      super(id);
      this.initWithStats(stats, extreme, hellMode);
   }

   public double bonusHp() {
      double bonus = 1.0D;
      if (this.extreme) {
         switch(this.stats.getId()) {
         case 8644655:
         case 8645064:
         case 8645066:
         case 8880405:
         case 8880500:
         case 8880501:
         case 8880502:
         case 8880503:
         case 8880504:
         case 8880505:
         case 8880506:
         case 8880512:
         case 8880518:
         case 8880519:
         case 8880600:
         case 8880601:
         case 8880602:
         case 8880607:
         case 8880608:
         case 8880614:
            bonus *= 50.0D;
         }
      }

      if (this.isHellMode()) {
         switch(this.stats.getId()) {
         case 8644655:
         case 8645066:
         case 8880405:
         case 8880500:
         case 8880501:
         case 8880502:
         case 8880503:
         case 8880504:
         case 8880505:
         case 8880519:
         case 8880600:
         case 8880602:
            bonus *= 5000.0D;
         }
      }

      return bonus;
   }

   private final void initWithStats(MapleMonsterStats stats) {
      this.setStance(5);
      this.stats = stats;
      this.hp = (long)((double)stats.getHp() * this.bonusHp());
      this.mp = stats.getMp();
   }

   private final void initWithStats(MapleMonsterStats stats, boolean extreme, boolean hellMode) {
      this.setStance(5);
      this.stats = stats;
      this.extreme = extreme;
      this.hellMode = hellMode;
      this.hp = (long)((double)stats.getHp() * this.bonusHp());
      this.mp = stats.getMp();
   }

   public final List<MapleMonster.AttackerEntry> getAttackers() {
      if (this.attackers != null && this.attackers.size() > 0) {
         List<MapleMonster.AttackerEntry> ret = new ArrayList();
         Iterator var2 = this.attackers.iterator();

         while(var2.hasNext()) {
            MapleMonster.AttackerEntry e = (MapleMonster.AttackerEntry)var2.next();
            if (e != null) {
               ret.add(e);
            }
         }

         return ret;
      } else {
         return new ArrayList();
      }
   }

   public long getLastReceivedMovePacket() {
      return this.lastReceivedMovePacket;
   }

   public void receiveMovePacket() {
      this.lastReceivedMovePacket = System.currentTimeMillis();
   }

   public final MapleMonsterStats getStats() {
      return this.stats;
   }

   public final void disableDrops() {
      this.dropsDisabled = true;
   }

   public final boolean dropsDisabled() {
      return this.dropsDisabled;
   }

   public final void setSponge(MapleMonster mob) {
      this.sponge = new WeakReference(mob);
      if (this.linkoid <= 0) {
         this.linkoid = mob.getObjectId();
      }

   }

   public final void setMap(MapleMap map) {
      this.map = map;
      this.startDropItemSchedule();
   }

   public int getOwner() {
      return this.owner;
   }

   public void setOwner(int id) {
      this.owner = id;
   }

   public final long getHp() {
      return this.hp;
   }

   public final void setHp(long hp) {
      this.hp = hp;
   }

   public final void addHp(long hp, boolean brodcast) {
      this.hp = this.getHp() + hp;
      if (this.hp > this.getStats().getHp()) {
         this.hp = this.getStats().getHp();
      }

      if (brodcast) {
         this.getMap().broadcastMessage(MobPacket.showBossHP(this));
      }

      if (this.hp <= 0L) {
         this.map.killMonster(this, (MapleCharacter)this.controller.get(), true, false, (byte)1, 0);
      }

   }

   public final ChangeableStats getChangedStats() {
      return this.ostats;
   }

   public final long getMobMaxHp() {
      return this.stats.getHp();
   }

   public final int getMp() {
      return this.mp;
   }

   public final void setMp(int mp) {
      if (mp < 0) {
         mp = 0;
      }

      this.mp = mp;
   }

   public final int getMobMaxMp() {
      return this.ostats != null ? this.ostats.mp : this.stats.getMp();
   }

   public final long getMobExp() {
      return this.ostats != null ? this.ostats.exp : this.stats.getExp();
   }

   public final void setOverrideStats(OverrideMonsterStats ostats) {
      this.ostats = new ChangeableStats(this.stats, ostats);
      this.hp = ostats.getHp();
      this.mp = ostats.getMp();
   }

   public final void changeLevel(int newLevel) {
      this.changeLevel(newLevel, true);
   }

   public final void changeLevel(int newLevel, boolean pqMob) {
      if (this.stats.isChangeable()) {
         this.ostats = new ChangeableStats(this.stats, newLevel, pqMob);
         this.hp = this.ostats.getHp();
         this.mp = this.ostats.getMp();
      }
   }

   public final MapleMonster getSponge() {
      return (MapleMonster)this.sponge.get();
   }

   public final void damage(MapleCharacter from, long damage, boolean updateAttackTime) {
      this.damage(from, damage, updateAttackTime, 0);
   }

   public final void damage(MapleCharacter from, long damage, boolean updateAttackTime, int lastSkill) {
      if (from != null && damage > 0L && this.isAlive() && this.getId() != 9305681) {
         if (from.isGM()) {
            damage = 10000000000000L;
         }

         MapleMonster will;
         if (this.getId() == 8880153) {
            if (this.getCustomValue(8881154) != null) {
               this.setHp(this.getStats().getHp() * 10L / 100L);
               this.map.broadcastMessage(MobPacket.showBossHP(this));
               return;
            }
         } else if (this.getId() != 8880303 && this.getId() != 8880304 && this.getId() != 8880343 && this.getId() != 8880344) {
            if (this.getId() == 8880301 || this.getId() == 8880341) {
               long nowhp = this.hp - damage;
               double hppercent = (double)nowhp * 100.0D / (double)this.getStats().getHp();
               MobSkill msi;
               if (hppercent <= 50.0D && this.getWillHplist().contains(500)) {
                  this.setHp(this.getMobMaxHp() * 50L / 100L);
                  this.map.broadcastMessage(MobPacket.showBossHP(this));
                  if (this.getCustomValue(2420701) == null && !this.isSkillForbid) {
                     if (this.schedule != null) {
                        this.schedule.cancel(true);
                     }

                     msi = MobSkillFactory.getMobSkill(242, 7);
                     msi.applyEffect((MapleCharacter)null, this, true, this.isFacingLeft());
                     this.setCustomInfo(2420701, 0, 120000);
                  }

                  return;
               }

               if (hppercent <= 0.3D && this.getWillHplist().contains(3)) {
                  this.setHp((long)((double)this.getMobMaxHp() * this.bonusHp() * 0.3D / 100.0D));
                  this.map.broadcastMessage(MobPacket.showBossHP(this));
                  if (this.getCustomValue(2420702) == null && !this.isSkillForbid) {
                     if (this.schedule != null) {
                        this.schedule.cancel(true);
                     }

                     msi = MobSkillFactory.getMobSkill(242, 7);
                     msi.applyEffect((MapleCharacter)null, this, true, this.isFacingLeft());
                     this.setCustomInfo(2420702, 0, 120000);
                  }

                  return;
               }
            }
         } else {
            will = this.map.getMonsterById(8880300);
            if (will == null) {
               will = this.map.getMonsterById(8880340);
            }

            long nowhp = this.hp - damage;
            double hppercent = (double)nowhp * 100.0D / (double)this.getStats().getHp();
            if (hppercent <= 66.6D && will.getWillHplist().contains(666)) {
               this.setHp((long)((double)this.getMobMaxHp() * 66.6D / 100.0D));
               this.map.broadcastMessage(MobPacket.showBossHP(this));
               return;
            }

            if (hppercent <= 33.3D && will.getWillHplist().contains(333)) {
               this.setHp((long)((double)this.getMobMaxHp() * this.bonusHp() * 33.3D / 100.0D));
               this.map.broadcastMessage(MobPacket.showBossHP(this));
               return;
            }

            if (hppercent <= 0.3D && will.getWillHplist().contains(3)) {
               this.setHp((long)((double)this.getMobMaxHp() * 0.3D / 100.0D));
               this.map.broadcastMessage(MobPacket.showBossHP(this));
               return;
            }
         }

         if ((this.getId() == 8880342 || this.getId() == 8880302) && from.getSkillCustomValue0(24209) > 0L) {
            from.setSkillCustomInfo(24220, 0L, 3000L);
         }

         will = null;
         MapleMonster linkMob = this.map.getMonsterById(this.stats.getHpLinkMob());
         if (linkMob != null) {
            linkMob.damage(from, damage, updateAttackTime, lastSkill);
         } else {
            Object attacker;
            if (from.getParty() != null) {
               attacker = new MapleMonster.PartyAttackerEntry(from.getParty().getId());
            } else {
               attacker = new MapleMonster.SingleAttackerEntry(from);
            }

            boolean replaced = false;
            Iterator var22 = this.getAttackers().iterator();

            while(var22.hasNext()) {
               MapleMonster.AttackerEntry aentry = (MapleMonster.AttackerEntry)var22.next();
               if (aentry != null && aentry.equals(attacker)) {
                  attacker = aentry;
                  replaced = true;
                  break;
               }
            }

            if (!replaced) {
               this.attackers.add(attacker);
            }

            if (GameConstants.isUnionRaid(from.getMapId())) {
               switch(this.getId()) {
               case 9833201:
               case 9833202:
               case 9833203:
               case 9833204:
               case 9833205:
                  this.hp = 1L;
                  var22 = this.getMap().getAllMonster().iterator();

                  while(var22.hasNext()) {
                     MapleMonster monster = (MapleMonster)var22.next();
                     if (monster.getOwner() == from.getId() && monster.getId() == this.getId() - 100 && monster.getBuff(MonsterStatus.MS_PowerImmune) == null) {
                        List<Pair<MonsterStatus, MonsterStatusEffect>> stats = new ArrayList();
                        stats.add(new Pair(MonsterStatus.MS_PowerImmune, new MonsterStatusEffect(146, 210000000, 1L)));
                        monster.applyMonsterBuff(monster.getMap(), stats, MobSkillFactory.getMobSkill(146, 13));
                        break;
                     }
                  }

                  return;
               }
            }

            if ((this.getId() == 9390612 || this.getId() == 9390610 || this.getId() == 9390911 || this.getId() == 8645066) && from.getKeyValue(200106, "golrux_in") == 1L) {
               from.setKeyValue(200106, "golrux_dmg", (from.getKeyValue(200106, "golrux_dmg") + damage).makeConcatWithConstants<invokedynamic>(from.getKeyValue(200106, "golrux_dmg") + damage));
            }

            int maxcount;
            int Random;
            int mobsize;
            int size;
            if (this.getId() == 8880305) {
               List<Obstacle> obs = new ArrayList();
               if (Randomizer.isSuccess(30)) {
                  for(Random = 0; Random < Randomizer.rand(1, 3); ++Random) {
                     mobsize = Randomizer.rand(63, 64);
                     maxcount = Randomizer.nextInt(1200) - 600;
                     size = this.getTruePosition().y > 0 ? -2020 : 159;
                     Obstacle ob = new Obstacle(mobsize, new Point(maxcount, size - 601), new Point(maxcount, size), 40, mobsize == 64 ? 0 : 60, 1208, 111, 3, 599);
                     obs.add(ob);
                  }

                  this.map.CreateObstacle(this, obs);
               }

            } else {
               if (from.getBuffedEffect(SecondaryStat.Reincarnation) != null && this.stats.isBoss() && from.getReinCarnation() > 0) {
                  from.setReinCarnation(from.getReinCarnation() - 1);
                  if (from.getReinCarnation() == 0) {
                     from.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(from, 0, 1320019, 1, 0, 0, (byte)(from.isFacingLeft() ? 1 : 0), true, from.getPosition(), (String)null, (Item)null));
                     from.getMap().broadcastMessage(from, CField.EffectPacket.showEffect(from, 0, 1320019, 1, 0, 0, (byte)(from.isFacingLeft() ? 1 : 0), false, from.getPosition(), (String)null, (Item)null), false);
                  }

                  Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
                  statups.put(SecondaryStat.Reincarnation, new Pair(1, (int)from.getBuffLimit(from.getBuffSource(SecondaryStat.Reincarnation))));
                  from.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, from.getBuffedEffect(SecondaryStat.Reincarnation), from));
               }

               if (this.getSeperateSoul() > 0) {
                  if (from.getMap() != null && from.getMap().getMonsterByOid(this.getSeperateSoul()) != null) {
                     from.getMap().getMonsterByOid(this.getSeperateSoul()).damage(from, damage, updateAttackTime);
                  }

               } else {
                  if (from.getMap().getId() == 270051100 || from.getMap().getId() == 270050100) {
                     boolean choas = from.getMap().getId() != 270050100;
                     if (this.getId() >= 8820002 + (choas ? 100 : 0) && this.getId() <= 8820006 + (choas ? 100 : 0) && from.getMap().getMonsterById(8820014 + (choas ? 100 : 0)) != null) {
                        from.getMap().getMonsterById(8820014 + (choas ? 100 : 0)).getStats().setTagColor(4);
                        from.getMap().getMonsterById(8820014 + (choas ? 100 : 0)).getStats().setTagBgColor(1);
                        if (this.getHp() <= damage) {
                           from.getMap().getMonsterById(8820014 + (choas ? 100 : 0)).damage(from, this.getHp(), updateAttackTime);
                        } else {
                           from.getMap().getMonsterById(8820014 + (choas ? 100 : 0)).damage(from, damage, updateAttackTime);
                        }
                     }
                  }

                  if (this.map.isElitebossrewardmap() && this.map.getElitebossrewardtype() == 1 && this.getId() == 8220027) {
                     int[] itemlist = new int[]{2432391, 2432392, 2432393, 2432394, 2432395, 2432396, 2432397};
                     int Random = false;
                     Random = (int)Math.floor(Math.random() * (double)itemlist.length);

                     for(mobsize = 0; mobsize < Randomizer.rand(1, 3); ++mobsize) {
                        Random = (int)Math.floor(Math.random() * (double)itemlist.length);
                        maxcount = itemlist[Random];
                        Item toDrop = new Item(maxcount, (short)0, (short)1, 0);
                        this.map.spawnItemDrop(this, this.getController(), toDrop, new Point(this.getTruePosition().x + (mobsize == 1 ? 25 : (mobsize == 2 ? 50 : 0)), this.getTruePosition().y), true, false);
                     }
                  }

                  ((MapleMonster.AttackerEntry)attacker).addDamage(from, damage, updateAttackTime);
                  if (this.getId() == 9832024) {
                     if (from.WorldbossDamage < 1000000000L) {
                        from.WorldbossDamage += damage;
                     } else {
                        from.WorldbossDamage += (long)((int)Math.floor((double)(from.WorldbossDamage / 1000000000L)));
                     }
                  }

                  NumberFormat Number = NumberFormat.getInstance();
                  if (this.getMobMaxHp() > 2147483647L && this.getId() == 9833376) {
                     from.DamageMeter += damage;
                     from.dropMessage(-1, "누적 데미지 : " + from.DamageMeter);
                     from.getClient().getSession().writeAndFlush(CField.getGameMessage(9, "누적 데미지 : " + from.DamageMeter));
                  }

                  if (from.getMapId() == 120000102) {
                     from.setDamageMeter(from.getDamageMeter() + damage);
                  }

                  MapleDiseases cap = from.getDisease(SecondaryStat.CapDebuff);
                  Iterator var33;
                  Iterator var40;
                  MapleMonster.AttackerEntry mattacker;
                  MapleMonster.AttackingMapleCharacter cattacker;
                  if (cap != null && (cap.getValue() == 100 && (this.getId() == 8900001 || this.getId() == 8900101) || cap.getValue() == 200 && (this.getId() == 8900002 || this.getId() == 8900102))) {
                     if ((double)this.hp < (double)this.stats.getHp() * this.bonusHp()) {
                        this.hp = (long)Math.min((double)(this.hp + damage), (double)this.stats.getHp() * this.bonusHp());
                        if (this.sponge.get() == null && this.hp > 0L) {
                           switch(this.stats.getHPDisplayType()) {
                           case 0:
                              this.map.broadcastMessage(MobPacket.showBossHP(this), this.getTruePosition());
                              break;
                           case 1:
                              this.map.broadcastMessage(from, MobPacket.damageFriendlyMob(this, 1L, true), false);
                              break;
                           case 2:
                              this.map.broadcastMessage(MobPacket.showMonsterHP(this.getObjectId(), this.getHPPercent()));
                              break;
                           case 3:
                              var33 = this.getAttackers().iterator();

                              while(true) {
                                 do {
                                    if (!var33.hasNext()) {
                                       return;
                                    }

                                    mattacker = (MapleMonster.AttackerEntry)var33.next();
                                 } while(mattacker == null);

                                 var40 = mattacker.getAttackers().iterator();

                                 while(var40.hasNext()) {
                                    cattacker = (MapleMonster.AttackingMapleCharacter)var40.next();
                                    if (cattacker != null && cattacker.getAttacker().getMap() == from.getMap() && cattacker.getLastAttackTime() >= System.currentTimeMillis() - 4000L) {
                                       cattacker.getAttacker().getClient().getSession().writeAndFlush(MobPacket.showMonsterHP(this.getObjectId(), this.getHPPercent()));
                                    }
                                 }
                              }
                           }
                        }
                     }

                  } else {
                     if (GameConstants.isAggressIveMonster(this.getId())) {
                        from.setAggressiveDamage(from.getAggressiveDamage() + damage);
                     }

                     if (this.getId() == 8880504) {
                        if (from.getMap().getMonsterById(8880519) != null) {
                           from.getMap().getMonsterById(8880519).damage(from, damage, updateAttackTime);
                        }

                     } else {
                        if (from.getMap().getId() / 10000 == 92507) {
                           mobsize = from.getMap().getId() % 92507 / 100;
                           if (this.getCustomValue0(this.getId()) == 1L) {
                              return;
                           }

                           if ((mobsize == 9 || mobsize == 13 || mobsize == 15 || mobsize == 45) && damage >= this.getHp() && this.getCustomValue0(this.getId()) == 0L) {
                              this.setCustomInfo(this.getId(), 1, 0);
                              from.getMap().broadcastMessage(MobPacket.notDamage(this.getObjectId()));
                              from.getMap().broadcastMessage(MobPacket.notDamageEffect(this.getObjectId(), 0));
                              this.setHp(1L);
                              from.getClient().getSession().writeAndFlush(MobPacket.showMonsterHP(this.getObjectId(), this.getHPPercent()));
                              Timer.EtcTimer.getInstance().schedule(() -> {
                                 this.map.killMonster(this, from, true, false, (byte)1, lastSkill);
                              }, 5000L);
                              return;
                           }
                        }

                        MapleMonster mob;
                        if (from.getBuffedValue(80002902) && !this.isAlive() && (this.getId() == 8644653 || this.getId() == 8644656 || this.getId() == 8644654 || this.getId() == 8644657)) {
                           from.setSkillCustomInfo(8644651, (long)((int)from.getSkillCustomValue0(8644651) - 20), 0L);
                           from.getClient().send(MobPacket.BossDusk.handleDuskGauge(true, (int)from.getSkillCustomValue0(8644651), 1000));
                           if (from.getSkillCustomValue0(8644651) <= 0L) {
                              from.setSkillCustomInfo(8644650, 1L, 0L);
                              from.setSkillCustomInfo(8644651, 0L, 0L);

                              while(from.getBuffedValue(80002902)) {
                                 from.cancelEffect(from.getBuffedEffect(80002902));
                              }

                              var33 = this.getMap().getAllMonster().iterator();

                              label922:
                              while(true) {
                                 do {
                                    do {
                                       if (!var33.hasNext()) {
                                          break label922;
                                       }

                                       mob = (MapleMonster)var33.next();
                                    } while(mob.getOwner() != from.getId());
                                 } while(mob.getId() != 8644653 && mob.getId() != 8644656 && mob.getId() != 8644654 && mob.getId() != 8644657);

                                 this.getMap().killMonster(mob);
                              }
                           }
                        }

                        if ((this.getId() == 8880111 || this.getId() == 8880101) && this.getCustomValue0(8880111) == 1L) {
                           this.addSkillCustomInfo(8880112, damage);
                           if (this.getCustomValue0(8880112) >= 1000000000L) {
                              this.getMap().broadcastMessage(MobPacket.demianRunaway(this, (byte)1, MobSkillFactory.getMobSkill(214, 14), 0, false));
                              this.setCustomInfo(8880111, 2, 0);
                           }
                        }

                        if (this.getId() >= 9833101 && this.getId() <= 9833105) {
                           from.setUnionNujuk(from.getUnionNujuk() + damage);
                        } else if (this.shield > 0L) {
                           this.shield -= damage;
                           this.map.broadcastMessage(MobPacket.showBossHP(this));
                           this.getMap().broadcastMessage(MobPacket.mobBarrier(this));
                        } else {
                           if (this.map.getId() / 10000 == 92507) {
                              this.map.broadcastMessage(MobPacket.showMonsterHP(this.getObjectId(), this.getHPPercent()));
                           }

                           if (this.getHPPercent() <= 30 && (this.getId() == 8880100 || this.getId() == 8880110)) {
                              this.DemainChangePhase(from);
                           } else if (this.getId() == 8880153 && this.getHPPercent() <= 10 && this.getCustomValue0(8881153) <= 0L) {
                              this.setHp(this.getStats().getHp() * 10L / 100L);
                              this.map.broadcastMessage(MobPacket.showBossHP(this));
                              this.setCustomInfo(8881153, 1, 0);
                              this.setCustomInfo(8881154, 0, 5000);
                              this.getMap().broadcastMessage(CWvsContext.getTopMsg("루시드가 체력을 회복합니다."));
                              Timer.MobTimer.getInstance().schedule(() -> {
                                 this.addHp(this.getStats().getHp() * 5L / 100L, true);
                                 this.getMap().broadcastMessage(MobPacket.healEffectMonster(this.getObjectId(), 114, 82));
                              }, 5000L);
                           } else if (this.getId() == 8641018 && this.getCustomValue(8641018) == null && !GameConstants.is_forceAtom_attack_skill(lastSkill)) {
                              mobsize = this.getController().getSkillCustomValue0(450001402) == 0L ? 8641019 : (this.getController().getSkillCustomValue0(450001402) == 1L ? 8641020 : 8641021);
                              mob = MapleLifeFactory.getMonster(mobsize);
                              this.getMap().spawnMonsterOnGroundBelow(mob, new Point(this.getPosition().x + Randomizer.rand(-150, 150), this.getPosition().y));
                              this.setCustomInfo(8641018, 0, 3000);
                           } else {
                              Iterator var36;
                              MapleMonster m;
                              MapleMonster m;
                              if (this.getId() == 8800002 || this.getId() == 8800102) {
                                 mobsize = 0;
                                 var36 = this.getMap().getAllMonster().iterator();

                                 label877:
                                 while(true) {
                                    do {
                                       if (!var36.hasNext()) {
                                          if (this.getHPPercent() <= 20 || mobsize <= 0 && this.getId() == 8800002) {
                                             this.getMap().broadcastMessage(MobPacket.setMonsterProPerties(this.getObjectId(), 0, 0, 0));
                                          }

                                          if (this.getSpecialtxt() != null && (this.getHPPercent() <= 20 || mobsize <= 0 && this.getId() == 8800002)) {
                                             this.setSpecialtxt((String)null);
                                             this.getMap().broadcastMessage(MobPacket.setMonsterProPerties(this.getObjectId(), 0, 0, 0));
                                             List<MapleNodes.Environment> envs = new ArrayList();
                                             var40 = this.getMap().getNodez().getEnvironments().iterator();

                                             while(var40.hasNext()) {
                                                MapleNodes.Environment env = (MapleNodes.Environment)var40.next();
                                                if (env.getName().contains("zdc")) {
                                                   env.setShow(false);
                                                   envs.add(env);
                                                }
                                             }

                                             this.getMap().broadcastMessage(CField.getUpdateEnvironment(envs));
                                             var40 = this.getMap().getAllMonster().iterator();

                                             while(true) {
                                                while(true) {
                                                   if (!var40.hasNext()) {
                                                      break label877;
                                                   }

                                                   m = (MapleMonster)var40.next();
                                                   if (m.getId() == 8800002 || m.getId() == 8800102) {
                                                      m.setPhase((byte)3);
                                                   }

                                                   if (m.getId() >= 8800003 && m.getId() <= 8800010 || m.getId() >= 8800103 && m.getId() <= 8800110) {
                                                      this.getMap().killMonster(m.getId());
                                                   } else if (m.getId() >= 8800130 && m.getId() <= 8800137) {
                                                      this.getMap().killMonster(m.getId());
                                                   } else if (m.getId() == 8800117 || m.getId() == 8800120) {
                                                      this.getMap().killMonster(m.getId());
                                                   }
                                                }
                                             }
                                          }
                                          break label877;
                                       }

                                       m = (MapleMonster)var36.next();
                                    } while((m.getId() < 8800003 || m.getId() > 8800010) && (m.getId() < 8800103 || m.getId() > 8800110));

                                    ++mobsize;
                                 }
                              }

                              boolean send;
                              MapleMonster m;
                              if (from.getMapId() == 993192800 && (this.getId() >= 9833935 && this.getId() <= 9833946 || this.getId() >= 9833947 && this.getId() <= 9833958)) {
                                 send = from.getMap().getAllMonster().size() == 1 && this.getHp() - damage <= 0L;
                                 maxcount = (int)from.getSkillCustomValue0(993192801);
                                 if (this.getHp() - damage <= 0L) {
                                    if (this.getCustomValue0(this.getId()) > 0L) {
                                       from.getClient().setCustomData(100795, "point", ((long)Integer.parseInt(from.getClient().getCustomData(100795, "point")) + this.getCustomValue0(this.getId())).makeConcatWithConstants<invokedynamic>((long)Integer.parseInt(from.getClient().getCustomData(100795, "point")) + this.getCustomValue0(this.getId())));
                                       from.getClient().send(CField.PunchKingPacket(from, 3, this.getObjectId(), (int)this.getCustomValue0(this.getId())));
                                       from.getClient().send(CField.PunchKingPacket(from, 2, Integer.parseInt(from.getClient().getCustomData(100795, "point"))));
                                    }
                                 } else {
                                    from.setSkillCustomInfo(this.getId(), from.getSkillCustomValue0(this.getId()) + damage, 0L);
                                    long totaldamage = from.getSkillCustomValue(this.getId());
                                    if (totaldamage > this.getHp()) {
                                       totaldamage = this.getHp();
                                    }

                                    if (totaldamage >= this.getStats().getHp() / 100L * 10L) {
                                       int addpoint;
                                       for(addpoint = 0; totaldamage > 0L; ++addpoint) {
                                          totaldamage -= this.getStats().getHp() * 10L / 100L;
                                       }

                                       from.removeSkillCustomInfo(this.getId());
                                       int plus = (int)this.getCustomValue0(this.getId()) / 10;
                                       if (plus <= 0) {
                                          plus = 1;
                                       }

                                       int totaladdpoint = plus * addpoint;
                                       if ((long)totaladdpoint > this.getCustomValue0(this.getId())) {
                                          totaladdpoint = (int)this.getCustomValue0(this.getId());
                                       }

                                       this.addSkillCustomInfo(this.getId(), (long)(-totaladdpoint));
                                       from.getClient().setCustomData(100795, "point", (Integer.parseInt(from.getClient().getCustomData(100795, "point")) + totaladdpoint).makeConcatWithConstants<invokedynamic>(Integer.parseInt(from.getClient().getCustomData(100795, "point")) + totaladdpoint));
                                       from.getClient().send(CField.PunchKingPacket(from, 3, this.getObjectId(), totaladdpoint));
                                       from.getClient().send(CField.PunchKingPacket(from, 2, Integer.parseInt(from.getClient().getCustomData(100795, "point"))));
                                    }
                                 }

                                 if (send) {
                                    if (maxcount + 1 > 12) {
                                       from.getClient().send(CField.PunchKingPacket(from, 0, 4));
                                       Timer.EventTimer.getInstance().schedule(() -> {
                                          if (from.getMapId() == 993192800) {
                                             from.warp(993192701);
                                          }

                                       }, 3000L);
                                    } else {
                                       from.getClient().send(SLFCGPacket.OnYellowDlg(9062507, 100, "#rSTAGE " + (maxcount + 1) + "#k 시작한담!", ""));
                                       m = MapleLifeFactory.getMonster(9833935 + maxcount);
                                       m.setOwner(from.getId());
                                       this.getMap().spawnMonsterOnGroundBelow(m, new Point(20, 581));

                                       for(int i = -140; i <= 180; i += 80) {
                                          m = MapleLifeFactory.getMonster(9833947 + maxcount);
                                          m.setOwner(from.getId());
                                          this.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9833947 + maxcount), new Point(i, 581));
                                       }

                                       ++maxcount;
                                       from.setSkillCustomInfo(993192801, (long)maxcount, 0L);
                                       from.getClient().send(CField.PunchKingPacket(from, 1, maxcount));
                                    }
                                 }
                              }

                              if (this.stats.getSelfD() != -1) {
                                 this.hp -= damage;
                                 if (this.hp > 0L) {
                                    if (this.hp < (long)this.stats.getSelfDHp()) {
                                       this.map.killMonster(this, from, false, false, this.stats.getSelfD(), lastSkill);
                                    } else {
                                       var33 = this.getAttackers().iterator();

                                       while(var33.hasNext()) {
                                          mattacker = (MapleMonster.AttackerEntry)var33.next();
                                          var40 = mattacker.getAttackers().iterator();

                                          while(var40.hasNext()) {
                                             cattacker = (MapleMonster.AttackingMapleCharacter)var40.next();
                                             if (cattacker.getAttacker().getMap() == from.getMap() && cattacker.getLastAttackTime() >= System.currentTimeMillis() - 4000L) {
                                                cattacker.getAttacker().getClient().getSession().writeAndFlush(MobPacket.showMonsterHP(this.getObjectId(), this.getHPPercent()));
                                             }
                                          }
                                       }
                                    }
                                 } else {
                                    this.map.killMonster(this, from, true, false, (byte)1, lastSkill);
                                 }
                              } else {
                                 if (this.sponge.get() != null && ((MapleMonster)this.sponge.get()).hp > 0L) {
                                    MapleMonster var10000;
                                    if (this.getStats().getHp() < damage) {
                                       var10000 = (MapleMonster)this.sponge.get();
                                       var10000.hp -= this.getStats().getHp();
                                    } else {
                                       var10000 = (MapleMonster)this.sponge.get();
                                       var10000.hp -= damage;
                                    }

                                    send = true;
                                    if (((MapleMonster)this.sponge.get()).hp <= 0L && (((MapleMonster)this.sponge.get()).getId() == 8810122 || ((MapleMonster)this.sponge.get()).getId() == 8810018)) {
                                       var36 = this.getMap().getAllMonster().iterator();

                                       while(var36.hasNext()) {
                                          m = (MapleMonster)var36.next();
                                          if (m.getId() != ((MapleMonster)this.sponge.get()).getId()) {
                                             this.getMap().killMonsterType(m, 1);
                                          }
                                       }

                                       send = false;
                                    }

                                    if (((MapleMonster)this.sponge.get()).hp <= 0L) {
                                       ((MapleMonster)this.sponge.get()).hp = 1L;
                                       this.map.broadcastMessage(MobPacket.showBossHP(((MapleMonster)this.sponge.get()).getId(), -1L, ((MapleMonster)this.sponge.get()).getMobMaxHp()));
                                       if (send) {
                                          this.map.killMonster((MapleMonster)this.sponge.get(), from, true, false, (byte)1, lastSkill);
                                       }
                                    } else {
                                       this.map.broadcastMessage(MobPacket.showBossHP((MapleMonster)this.sponge.get()));
                                    }
                                 }

                                 if (this.hp > 0L) {
                                    if (this.barrier > 0L) {
                                       if (this.barrier >= damage) {
                                          this.barrier -= damage;
                                       } else {
                                          this.barrier = 0L;
                                          this.hp -= damage - this.barrier;
                                       }
                                    } else {
                                       this.hp -= damage;
                                    }

                                    if (this.eventInstance != null) {
                                       this.eventInstance.monsterDamaged(from, this, damage);
                                    } else {
                                       EventInstanceManager em = from.getEventInstance();
                                       if (em != null) {
                                          em.monsterDamaged(from, this, damage);
                                       }
                                    }

                                    if (this.getStats().isMobZone()) {
                                       byte phase;
                                       if (this.getId() != 8880101 && this.getId() != 8880111) {
                                          if (this.getId() != 8644650 && this.getId() != 8644655 && this.getId() != 8880503) {
                                             if (this.getHPPercent() > 75) {
                                                phase = 1;
                                             } else if (this.getHPPercent() > 50) {
                                                phase = 2;
                                             } else if (this.getHPPercent() > 25) {
                                                phase = 3;
                                             } else {
                                                phase = 4;
                                             }

                                             if (this.phase != phase) {
                                                this.setPhase(phase);
                                             }

                                             this.map.broadcastMessage(MobPacket.changePhase(this));
                                             this.map.broadcastMessage(MobPacket.changeMobZone(this));
                                          }
                                       } else {
                                          if (this.getHPPercent() <= 10) {
                                             phase = 4;
                                          } else if (this.getHPPercent() <= 15) {
                                             phase = 3;
                                          } else if (this.getHPPercent() <= 20) {
                                             phase = 2;
                                          } else {
                                             phase = 1;
                                          }

                                          maxcount = phase == 2 ? 2 : (phase == 4 ? 3 : 1);
                                          size = 0;
                                          Iterator var49 = this.getMap().getAllMonster().iterator();

                                          while(var49.hasNext()) {
                                             m = (MapleMonster)var49.next();
                                             if (m.getId() == 8880102) {
                                                ++size;
                                             }
                                          }

                                          if (size < maxcount) {
                                             m = MapleLifeFactory.getMonster(8880102);
                                             m.getStats().setSpeed(80);
                                             this.getMap().spawnMonsterWithEffect(m, MobSkillFactory.getMobSkill(201, 182).getSpawnEffect(), this.getPosition());
                                          }

                                          if (this.phase != phase) {
                                             this.setPhase(phase);
                                          }

                                          this.map.broadcastMessage(MobPacket.changePhase(this));
                                          this.map.broadcastMessage(MobPacket.changeMobZone(this));
                                       }
                                    }

                                    if (this.hp > 0L) {
                                       maxcount = (100 - this.getHPPercent()) / 10;
                                       if (this.map.getLucidCount() + this.map.getLucidUseCount() < maxcount && (this.getId() == 8880140 || this.getId() == 8880141 || this.getId() == 8880150 || this.getId() == 8880151)) {
                                          if (this.map.getLucidCount() < 3) {
                                             this.map.setLucidCount(this.map.getLucidCount() + 1);
                                             this.map.broadcastMessage(CField.enforceMSG("나팔동상 근처에서 '채집'키를 눌러 사용하면 루시드의 힘을 억제할 수 있습니다!", 222, 2000));
                                          }

                                          this.map.broadcastMessage(MobPacket.BossLucid.changeStatueState(false, this.map.getLucidCount(), false));
                                       }

                                       List hps;
                                       MapleMonster will;
                                       switch(this.getId()) {
                                       case 8880300:
                                       case 8880303:
                                       case 8880304:
                                          will = this.map.getMonsterById(8880300);
                                          if (will != null) {
                                             hps = will.getWillHplist();
                                             this.map.broadcastMessage(MobPacket.BossWill.setWillHp(hps, this.map, 8880300, 8880303, 8880304));
                                          }
                                          break;
                                       case 8880301:
                                       case 8880341:
                                          this.map.broadcastMessage(MobPacket.BossWill.setWillHp(this.getWillHplist()));
                                          break;
                                       case 8880340:
                                       case 8880343:
                                       case 8880344:
                                          will = this.map.getMonsterById(8880340);
                                          if (will != null) {
                                             hps = will.getWillHplist();
                                             this.map.broadcastMessage(MobPacket.BossWill.setWillHp(hps, this.map, 8880340, 8880343, 8880344));
                                          }
                                       }
                                    }

                                    if (this.sponge.get() == null && this.hp > 0L) {
                                       switch(this.stats.getHPDisplayType()) {
                                       case 0:
                                          this.map.broadcastMessage(MobPacket.showBossHP(this), this.getTruePosition());
                                          if (this.getId() == 8870100 && this.getHPPercent() <= 50) {
                                             this.setPhase((byte)2);
                                             this.map.broadcastMessage(MobPacket.showMonsterHP(this.getObjectId(), this.getHPPercent()));
                                          }
                                          break;
                                       case 1:
                                          this.map.broadcastMessage(from, MobPacket.damageFriendlyMob(this, 1L, true), false);
                                          break;
                                       case 2:
                                          this.map.broadcastMessage(MobPacket.showMonsterHP(this.getObjectId(), this.getHPPercent()));
                                          break;
                                       case 3:
                                          var33 = this.getAttackers().iterator();

                                          label720:
                                          while(true) {
                                             do {
                                                if (!var33.hasNext()) {
                                                   break label720;
                                                }

                                                mattacker = (MapleMonster.AttackerEntry)var33.next();
                                             } while(mattacker == null);

                                             var40 = mattacker.getAttackers().iterator();

                                             while(var40.hasNext()) {
                                                cattacker = (MapleMonster.AttackingMapleCharacter)var40.next();
                                                if (cattacker != null && cattacker.getAttacker().getMap() == from.getMap() && cattacker.getLastAttackTime() >= System.currentTimeMillis() - 4000L) {
                                                   cattacker.getAttacker().getClient().getSession().writeAndFlush(MobPacket.showMonsterHP(this.getObjectId(), this.getHPPercent()));
                                                }
                                             }
                                          }
                                       }
                                    }

                                    if (this.getStats().isBoss() && from.getSkillLevel(131001026) > 0 && !from.getBuffedValue(131003026) && !from.skillisCooling(131001026)) {
                                       SkillFactory.getSkill(131001026).getEffect(1).applyTo(from);
                                    }

                                    if (this.hp <= 0L) {
                                       if (GameConstants.isExecutionSkill(lastSkill) && from.skillisCooling(63001002)) {
                                          from.removeCooldown(63001002);
                                       }

                                       if (from.getSkillLevel(131001026) > 0 && !from.getBuffedValue(131003026) && !from.skillisCooling(131001026)) {
                                          SkillFactory.getSkill(131001026).getEffect(1).applyTo(from);
                                       }

                                       if (lastSkill != 400011027 && lastSkill != 64121011) {
                                          if (GameConstants.isYeti(from.getJob())) {
                                             if (lastSkill != 135001000 && lastSkill != 135001001 && lastSkill != 135001002) {
                                                if (this.getBuff(135001012) != null) {
                                                   from.getYetiGauge(999, 0);
                                                }
                                             } else {
                                                from.getYetiGauge(lastSkill, 1);
                                             }
                                          }
                                       } else {
                                          this.map.broadcastMessage(MobPacket.deathEffect(this.getObjectId(), lastSkill, from.getId()));
                                       }

                                       if (this.stats.getHPDisplayType() == 0) {
                                          this.map.broadcastMessage(MobPacket.showBossHP(this.getId(), -1L, (long)((double)this.getMobMaxHp() * this.bonusHp())), this.getTruePosition());
                                       }

                                       this.map.killMonster(this, from, true, false, (byte)1, lastSkill);
                                       if (from.getMonsterCombo() == 0) {
                                          from.setMonsterComboTime(System.currentTimeMillis());
                                       }

                                       if (from.getKeyValue(16700, "count") < 300L) {
                                          from.setKeyValue(16700, "count", String.valueOf(from.getKeyValue(16700, "count") + 1L));
                                       }

                                       if (GameConstants.isExecutionSkill(lastSkill) && from.skillisCooling(63001002)) {
                                          from.removeCooldown(63001002);
                                       }
                                    }
                                 }
                              }

                              this.startDropItemSchedule();
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public int getHPPercent() {
      return !this.elitemonster && !this.eliteboss ? (int)Math.ceil((double)this.hp * 100.0D / (double)this.getStats().getHp()) : (int)Math.ceil((double)this.hp * 100.0D / (double)this.elitehp);
   }

   public double getHPPercentDouble() {
      return Math.ceil((double)this.hp * 100.0D / (double)this.getMobMaxHp());
   }

   public final void heal(long hp, long mp, boolean broadcast) {
      if (this.getBuff(MonsterStatus.MS_DebuffHealing) != null) {
         hp -= hp / 100L * this.getBuff(MonsterStatus.MS_DebuffHealing).getValue();
      }

      if (hp < 0L) {
         hp = 0L;
      }

      long TotalHP = this.getHp() + hp;
      long TotalMP = (long)this.getMp() + mp;
      if (TotalHP >= this.getMobMaxHp()) {
         this.setHp(this.getMobMaxHp());
      } else {
         this.setHp(TotalHP);
      }

      if (TotalMP >= (long)this.getMp()) {
         this.setMp(this.getMp());
      } else {
         this.setMp((int)TotalMP);
      }

      if (broadcast) {
         this.map.broadcastMessage(MobPacket.healMonster(this.getObjectId(), hp));
      } else if (this.sponge.get() != null) {
         MapleMonster var10000 = (MapleMonster)this.sponge.get();
         var10000.hp += hp;
      }

   }

   public final void killed() {
      Iterator var1 = this.stati.iterator();

      while(var1.hasNext()) {
         Pair<MonsterStatus, MonsterStatusEffect> skill = (Pair)var1.next();
         if (((MonsterStatusEffect)skill.getRight()).getSchedule() != null && !((MonsterStatusEffect)skill.getRight()).getSchedule().isDone()) {
            ((MonsterStatusEffect)skill.getRight()).getSchedule().cancel(true);
         }
      }

      if (this.listener != null) {
         this.listener.monsterKilled();
      }

      if (this.getSchedule() != null) {
         this.getSchedule().cancel(true);
      }

      this.listener = null;
   }

   private final void giveExpToCharacter(MapleCharacter attacker, long exp, boolean highestDamage, int numExpSharers, byte pty, byte Class_Bonus_EXP_PERCENT, byte Premium_Bonus_EXP_PERCENT, int lastskillID) {
      int[] linkMobs = new int[]{9010152, 9010153, 9010154, 9010155, 9010156, 9010157, 9010158, 9010159, 9010160, 9010161, 9010162, 9010163, 9010164, 9010165, 9010166, 9010167, 9010168, 9010169, 9010170, 9010171, 9010172, 9010173, 9010174, 9010175, 9010176, 9010177, 9010178, 9010179, 9010180, 9010181};
      int[] var11 = linkMobs;
      int var12 = linkMobs.length;

      for(int var13 = 0; var13 < var12; ++var13) {
         int linkMob = var11[var13];
         if (this.getId() == linkMob) {
            double plus = 1.0E-4D;
            plus *= (double)(276 - attacker.getLevel()) * 0.025D;
            exp = (long)((int)((double)GameConstants.getExpNeededForLevel(attacker.getLevel()) * plus));
         }
      }

      if (attacker.getMapId() == 1 || attacker.getMapId() == 2 || attacker.getMapId() == 3) {
         exp *= 100000L;
      }

      if (exp > 0L) {
         MonsterStatusEffect ms = this.getBuff(MonsterStatus.MS_Showdown);
         if (ms != null) {
            exp += (long)((int)((double)(exp * ms.getValue()) / 100.0D));
         }

         if (attacker.hasDisease(SecondaryStat.Curse)) {
            exp /= 2L;
         }

         if (attacker.getLevel() <= 500) {
            exp *= GameConstants.getDate_WeekendMaple() >= 0 ? 2000L : 1000L;
         } else if (attacker.getLevel() <= 150) {
            exp *= GameConstants.getDate_WeekendMaple() >= 0 ? 2000L : 1000L;
         } else if (attacker.getLevel() <= 210) {
            exp *= GameConstants.getDate_WeekendMaple() >= 0 ? 1000L : 500L;
         } else if (attacker.getLevel() <= 220) {
            exp *= GameConstants.getDate_WeekendMaple() >= 0 ? 240L : 120L;
         } else if (attacker.getLevel() <= 230) {
            exp *= GameConstants.getDate_WeekendMaple() >= 0 ? 200L : 100L;
         } else if (attacker.getLevel() <= 240) {
            exp *= GameConstants.getDate_WeekendMaple() >= 0 ? 120L : 60L;
         } else if (attacker.getLevel() <= 250) {
            exp *= GameConstants.getDate_WeekendMaple() >= 0 ? 100L : 50L;
         } else if (attacker.getLevel() <= 260) {
            exp *= GameConstants.getDate_WeekendMaple() >= 0 ? 50L : 25L;
         } else if (attacker.getLevel() <= 270) {
            exp *= GameConstants.getDate_WeekendMaple() >= 0 ? 30L : 15L;
         } else if (attacker.getLevel() <= 280) {
            exp *= GameConstants.getDate_WeekendMaple() >= 0 ? 20L : 10L;
         } else if (attacker.getLevel() <= 290) {
            exp *= GameConstants.getDate_WeekendMaple() >= 0 ? 10L : 5L;
         } else if (attacker.getLevel() <= 300) {
            exp *= GameConstants.getDate_WeekendMaple() >= 0 ? 7L : 5L;
         } else if (attacker.getLevel() <= 500) {
            exp *= GameConstants.getDate_WeekendMaple() >= 0 ? 5L : 5L;
         } else {
            exp *= 5L;
         }

         exp *= !GameConstants.isPinkBean(attacker.getJob()) && !GameConstants.isYeti(attacker.getJob()) ? 2L : 4L;
         if (!attacker.getBuffedValue(80002282) && attacker.getMap().getRuneCurse() > 0 && !GameConstants.보스맵(this.getMap().getId()) && !GameConstants.isContentsMap(this.getMap().getId())) {
            attacker.getClient().getSession().writeAndFlush(CField.runeCurse("룬을 해방하여 엘리트 보스의 저주를 풀어야 합니다!!\\n저주 " + attacker.getMap().getRuneCurse() + "단계 :  경험치 획득, 드롭률 " + attacker.getMap().getRuneCurseDecrease() + "% 감소 효과 적용 중", false));
            exp -= exp * (long)attacker.getMap().getRuneCurseDecrease() / 100L;
         }

         if (attacker.getLevel() >= 200) {
            int level = 20;
            if (attacker.getMap().isSpawnPoint() && !this.getStats().isBoss() && (this.getStats().getLevel() - level > attacker.getLevel() || attacker.getLevel() > this.getStats().getLevel() + level)) {
               exp -= exp * 80L / 100L;
               if (attacker.getSkillCustomValue0(60524) == 0L) {
                  attacker.setSkillCustomInfo(60524, 1L, 0L);
                  attacker.getClient().getSession().writeAndFlush(CField.UIPacket.detailShowInfo("레벨 범위를 벗어난 몬스터를 사냥 시 경험치와 메소 획득량이 크게 감소합니다.", 3, 20, 20));
               }
            }
         }

         if (this.isBuffed(MonsterStatus.MS_SeperateSoulP) || this.isBuffed(MonsterStatus.MS_SeperateSoulC)) {
            exp *= 2L;
         }

         if (attacker.getKeyValue(210416, "TotalDeadTime") > 0L) {
            exp = (long)((double)exp * 0.2D);
         }

         if (this.getId() >= 9830000 && this.getId() <= 9830018 || this.getId() >= 9831000 && this.getId() <= 9831014) {
            if (attacker.getLevel() <= 210) {
               exp = (long)((int)((double)GameConstants.getExpNeededForLevel(attacker.getLevel()) * 0.005D));
            } else if (attacker.getLevel() <= 230) {
               exp = (long)((int)((double)GameConstants.getExpNeededForLevel(attacker.getLevel()) * 1.0E-4D));
            } else {
               exp = (long)((int)((double)GameConstants.getExpNeededForLevel(attacker.getLevel()) * 1.0E-5D));
            }
         }

         if (attacker.getMapId() == 921170004) {
            exp /= 5L;
         }

         attacker.gainExpMonster(exp, true, highestDamage);
         attacker.getTrait(MapleTrait.MapleTraitType.charisma).addExp(this.stats.getCharismaEXP(), attacker);
      }

   }

   public final int killBy(MapleCharacter killer, int lastSkill) {
      if (this.killed) {
         return 1;
      } else {
         this.killed = true;
         long totalBaseExp = this.getMobExp();
         MapleMonster.AttackerEntry highest = null;
         long highdamage = 0L;
         List<MapleMonster.AttackerEntry> list = this.getAttackers();
         Iterator var9 = list.iterator();

         MapleMonster.AttackerEntry attackEntry;
         while(var9.hasNext()) {
            attackEntry = (MapleMonster.AttackerEntry)var9.next();
            if (attackEntry != null && attackEntry.getDamage() > highdamage) {
               highest = attackEntry;
               highdamage = attackEntry.getDamage();
            }
         }

         var9 = list.iterator();

         while(var9.hasNext()) {
            attackEntry = (MapleMonster.AttackerEntry)var9.next();
            if (attackEntry != null) {
               attackEntry.killedMob(this.getMap(), totalBaseExp, attackEntry == highest, lastSkill);
            }
         }

         MapleCharacter controll = (MapleCharacter)this.controller.get();
         if (controll != null) {
            controll.getClient().getSession().writeAndFlush(MobPacket.stopControllingMonster(this.getObjectId()));
            controll.stopControllingMonster(this);
         }

         byte count;
         int v1;
         if (killer != null && killer.getPosition() != null && killer.getKeyValue(501661, "point") < 99999L && (this.getId() < 8644101 || this.getId() > 8644112)) {
            v1 = Randomizer.nextInt(2) + 1;
            count = 1;
            killer.AddStarDustPoint2(v1 * count);
         }

         MapleMonster mons;
         boolean set;
         long var10003;
         if (killer != null && killer.getPosition() != null && (this.getId() < 8644101 || this.getId() > 8644112) && this.getStats().getLevel() >= killer.getLevel() - 20 && this.getStats().getLevel() <= killer.getLevel() + 20) {
            if (killer.getBuffedValue(80003025)) {
               int maximum = 2000;
               if (killer.getKeyValue(100722, "today") < (long)maximum) {
                  set = Randomizer.isSuccess(10);
                  if (set) {
                     var10003 = killer.getKeyValue(100722, "today");
                     killer.setKeyValue(100722, "today", (var10003 + 1L).makeConcatWithConstants<invokedynamic>(var10003 + 1L));
                     var10003 = killer.getKeyValue(100722, "cnt");
                     killer.setKeyValue(100722, "cnt", (var10003 + 1L).makeConcatWithConstants<invokedynamic>(var10003 + 1L));
                     killer.AddStarDustPoint(1, 100, this.getTruePosition());
                     killer.getClient().getSession().writeAndFlush(SLFCGPacket.SkillfromMonsterEffect(80003025, 0, this.getTruePosition().x, this.getTruePosition().y));
                     int nowcoin = (int)killer.getKeyValue(100722, "cnt");
                     if (nowcoin == 5) {
                        if (Randomizer.isSuccess(15)) {
                           killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 2000, "나는 널 그리워 할꺼야.\r\n 항상..."));
                        } else if (Randomizer.isSuccess(15)) {
                           killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 2000, "에르다를 조금만 더 모으면 거대한 마법의 종이 나타날 거야."));
                        } else if (Randomizer.isSuccess(15)) {
                           killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 2000, "아아, 이 세계가 에르다로 가득해."));
                        } else if (Randomizer.isSuccess(15)) {
                           killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 2000, "네오 캐슬을 지나~\r\n늪을 건너~"));
                        } else if (Randomizer.isSuccess(15)) {
                           killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 2000, "네오 캐슬에 다시 오고 싶다면 9와 4분의 3번째 에르다를 찾으렴."));
                        } else {
                           killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 2000, "부디 눈꽃 순록들에게\r\n종소리가 닿기를..."));
                        }
                     } else if (nowcoin == 10) {
                        List<MapleMonster> monsters = this.getMap().getAllMonster();
                        killer.setKeyValue(100722, "cnt", "0");
                        killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 900, "에르다의 기운이 모여서\r\n거대한 #r마법의 종#k이 나타났어."));
                        killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 900, "어서 종을 울려서\r\n#b네오 스톤#k을 모아보렴."));
                        mons = MapleLifeFactory.getMonster(9833905);
                        mons.setOwner(killer.getId());
                        this.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9833905), this.getPosition());
                     }
                  }
               }
            }

            if (killer.getBuffedEffect(SecondaryStat.EventSpecialSkill, 80003016) != null) {
               if (killer.getEventKillingMode()) {
                  if (killer.getKeyValue(100711, "today") < 20000L) {
                     if (killer.getEventMobCount() > 0) {
                        killer.setEventMobCount(killer.getEventMobCount() - 1);
                        killer.AddStarDustPoint(1, 100, this.getTruePosition());
                        killer.getClient().getSession().writeAndFlush(SLFCGPacket.EventSkillSetCount(80003016, killer.getEventMobCount()));
                        killer.getClient().getSession().writeAndFlush(SLFCGPacket.SkillfromMonsterEffect(80003016, 0, this.getTruePosition().x, this.getTruePosition().y));
                        if (killer.getEventMobCount() <= 0) {
                           killer.setEventKillingMode(false);
                           killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 1300, "마법구슬의 힘으로 네오 스톤을\r\n#r모두#k 찾았어!"));
                           killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 1300, "이것이 #b에르다#k로 빚어낸 마법!\r\n너무 아름다워..."));
                           killer.getClient().getSession().writeAndFlush(SLFCGPacket.EventSkillEnd(80003016));
                           killer.getEventSkillTimer().cancel(true);
                           killer.setEventSkillTimer((ScheduledFuture)null);
                        }
                     } else {
                        killer.setEventKillingMode(false);
                        killer.getClient().getSession().writeAndFlush(SLFCGPacket.DreamBreakerMsg("오늘 획득 가능한 네오 스톤을 모두 획득햇습니다."));
                        killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 1300, "후후, 오늘은 이 정도면 충분할 것 같아. 고마워!"));
                        killer.getClient().getSession().writeAndFlush(SLFCGPacket.EventSkillEnd(80003016));
                        killer.cancelEffectFromBuffStat(SecondaryStat.EventSpecialSkill, 80003016);
                        killer.getClient().getSession().writeAndFlush(CField.UIPacket.closeUI(1291));
                        killer.dropMessage(5, "르네의 마법구슬이 비활성화되었습니다.");
                     }
                  } else {
                     killer.setEventKillingMode(false);
                     killer.getClient().getSession().writeAndFlush(SLFCGPacket.EventSkillEnd(80003016));
                     killer.getClient().getSession().writeAndFlush(SLFCGPacket.DreamBreakerMsg("오늘 획득 가능한 네오 스톤을 모두 획득햇습니다."));
                     killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 1300, "후후, 오늘은 이 정도면 충분할 것 같아. 고마워!"));
                     killer.cancelEffectFromBuffStat(SecondaryStat.EventSpecialSkill, 80003016);
                     killer.getClient().getSession().writeAndFlush(CField.UIPacket.closeUI(1291));
                     killer.dropMessage(5, "르네의 마법구슬이 비활성화되었습니다.");
                  }
               } else {
                  boolean v = Randomizer.isSuccess2(30);
                  if ((Randomizer.isSuccess(5) || v) && !this.getStats().isBoss()) {
                     if (killer.getKeyValue(100711, "today") < 30000L) {
                        killer.AddStarDustPoint(1, 100, this.getTruePosition());
                        if (killer.getKeyValue(100708, "fever") == -1L) {
                           killer.setKeyValue(100708, "fever", "0");
                        }

                        if (v) {
                           killer.getClient().getSession().writeAndFlush(SLFCGPacket.SkillfromMonsterEffect(80003016, 1, this.getTruePosition().x, this.getTruePosition().y));
                           killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 1300, "마법구슬에 #b에르다#k 급속충전된거같아! 준비해!"));
                           killer.setKeyValue(100708, "fever", "100");
                        } else {
                           killer.getClient().getSession().writeAndFlush(SLFCGPacket.SkillfromMonsterEffect(80003016, 0, this.getTruePosition().x, this.getTruePosition().y));
                           var10003 = killer.getKeyValue(100708, "fever");
                           killer.setKeyValue(100708, "fever", (var10003 + 5L).makeConcatWithConstants<invokedynamic>(var10003 + 5L));
                        }

                        if (killer.getKeyValue(100708, "fever") == 25L) {
                           killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 1300, "마법구슬의 #b에르다#k가 차오르기 시작했어."));
                        } else if (killer.getKeyValue(100708, "fever") == 50L) {
                           killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 1300, "마법구슬에 #b에르다#k가 절반정도 찬거같아. 조금더 힘내!"));
                        } else if (killer.getKeyValue(100708, "fever") == 75L) {
                           killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 1300, "마법구슬에 #b에르다#k가 거의 차오른거같아. 화이팅!"));
                        } else if (killer.getKeyValue(100708, "fever") >= 100L) {
                           killer.setKeyValue(100708, "fever", "0");
                           if (!killer.getEventKillingMode()) {
                              count = 30;
                              killer.setEventKillingMode(true);
                              killer.setEventMobCount(count);
                              killer.getClient().getSession().writeAndFlush(SLFCGPacket.DreamBreakerMsg("르네의 마법구슬이 빛을 발하며 온 몸에 에르다의 힘이 깃듭니다"));
                              killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 1300, "마법구슬이 가득 차오른 지금!\r\n#b네오 크리스탈 파워!#k"));
                              killer.getClient().getSession().writeAndFlush(SLFCGPacket.EventSkillEffect(80003016, 30000));
                              killer.getClient().getSession().writeAndFlush(SLFCGPacket.EventSkillStart(80003016, 30000));
                              killer.getClient().getSession().writeAndFlush(SLFCGPacket.EventSkillSetCount(80003016, count));
                              ScheduledFuture qwer;
                              if (killer.getEventSkillTimer() == null) {
                                 qwer = Timer.ShowTimer.getInstance().schedule(() -> {
                                    if (killer != null) {
                                       killer.setEventKillingMode(false);
                                       killer.getClient().getSession().writeAndFlush(SLFCGPacket.EventSkillEnd(80003016));
                                       killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 1300, "마법구슬의 힘으로 네오 스톤을\r\n#r일부#k 찾았어!"));
                                       killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 1300, "이것이 #b에르다#k로 빚어낸 마법!\r\n너무 아름다워..."));
                                       killer.getEventSkillTimer().cancel(true);
                                       killer.setEventSkillTimer((ScheduledFuture)null);
                                    }

                                 }, 30000L);
                                 killer.setEventSkillTimer(qwer);
                              } else {
                                 killer.getEventSkillTimer().cancel(true);
                                 killer.setEventSkillTimer((ScheduledFuture)null);
                                 qwer = Timer.ShowTimer.getInstance().schedule(() -> {
                                    if (killer != null) {
                                       killer.setEventKillingMode(false);
                                       killer.getClient().getSession().writeAndFlush(SLFCGPacket.EventSkillEnd(80003016));
                                       killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 1300, "마법구슬의 힘으로 네오 스톤을\r\n#r일부#k 찾았어!"));
                                       killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 1300, "이것이 #b에르다#k로 빚어낸 마법!\r\n너무 아름다워..."));
                                       killer.getEventSkillTimer().cancel(true);
                                       killer.setEventSkillTimer((ScheduledFuture)null);
                                    }

                                 }, 30000L);
                                 killer.setEventSkillTimer(qwer);
                              }
                           }
                        } else {
                           killer.getClient().getSession().writeAndFlush(SLFCGPacket.DreamBreakerMsg("에르다를 찾아 네오 스톤 1개를 찾았습니다. (구슬 게이지: " + killer.getKeyValue(100708, "fever") + "%)"));
                        }
                     } else {
                        killer.getClient().getSession().writeAndFlush(SLFCGPacket.EventSkillEnd(80003016));
                        killer.getClient().getSession().writeAndFlush(SLFCGPacket.DreamBreakerMsg("오늘 획득 가능한 네오 스톤을 모두 획득햇습니다."));
                        killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 1300, "후후, 오늘은 이 정도면 충분할 것 같아. 고마워!"));
                        killer.cancelEffectFromBuffStat(SecondaryStat.EventSpecialSkill, 80003016);
                        killer.getClient().getSession().writeAndFlush(CField.UIPacket.closeUI(1291));
                        killer.dropMessage(11, "르네의 마법구슬이 비활성화되었습니다.");
                     }
                  }

                  if (killer.getKeyValue(100711, "today") >= 30000L) {
                     killer.getClient().getSession().writeAndFlush(SLFCGPacket.EventSkillEnd(80003016));
                     killer.getClient().getSession().writeAndFlush(SLFCGPacket.DreamBreakerMsg("오늘 획득 가능한 네오 스톤을 모두 획득햇습니다."));
                     killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 1300, "후후, 오늘은 이 정도면 충분할 것 같아. 고마워!"));
                     killer.cancelEffectFromBuffStat(SecondaryStat.EventSpecialSkill, 80003016);
                     killer.getClient().getSession().writeAndFlush(CField.UIPacket.closeUI(1291));
                     killer.dropMessage(11, "르네의 마법구슬이 비활성화되었습니다.");
                  }
               }
            }
         }

         if (killer.getBuffedValue(80003025)) {
            if (this.getId() == 9833905) {
               var10003 = killer.getKeyValue(100722, "today");
               killer.setKeyValue(100722, "today", (var10003 + 20L).makeConcatWithConstants<invokedynamic>(var10003 + 20L));
               killer.AddStarDustPoint(1, 2000, this.getTruePosition());
               if (Randomizer.isSuccess(5)) {
                  killer.getClient().getSession().writeAndFlush(SLFCGPacket.EventSkillOn(this.getId()));
                  killer.AddStarDustPoint(1, 2000, this.getTruePosition());
                  var10003 = killer.getKeyValue(100722, "today");
                  killer.setKeyValue(100722, "today", (var10003 + 20L).makeConcatWithConstants<invokedynamic>(var10003 + 20L));
                  Timer.ShowTimer.getInstance().schedule(() -> {
                     if (killer != null) {
                        if (Randomizer.isSuccess(50)) {
                           killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 700, "와! 눈꽃 순록들이 달려오고 있어!"));
                           killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 2000, "눈꽃 순록들이 달리는 모습!\r\n너무 아름다웠어!"));
                        } else {
                           killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 700, "종 소리가 울려 퍼지는 지금!\r\n눈꽃 순록 러쉬!"));
                           killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 2000, "고마워.\r\n눈꽃 순록들도 즐거워 하고 있어."));
                        }
                     }

                  }, 1500L);
               }
            }

            if (killer.getKeyValue(100722, "today") >= 2000L) {
               killer.getClient().getSession().writeAndFlush(SLFCGPacket.DreamBreakerMsg("오늘 획득 가능한 네오 스톤을 모두 획득햇습니다."));
               killer.getClient().getSession().writeAndFlush(CField.enforceMsgNPC(9062453, 1300, "후후, 오늘은 이 정도면 충분할 것 같아. 고마워!"));
               killer.cancelEffectFromBuffStat(SecondaryStat.EventSpecialSkill, 80003025);
               killer.dropMessage(5, "르네와 마법의 종 스킬이 비활성화되었습니다.");
            }
         }

         MapleMap target;
         if (!FieldLimitType.Event.check(this.map.getFieldLimit()) && !this.map.isEliteField() && !this.map.isElitebossmap() && !this.map.isElitebossrewardmap() && !this.map.isElitechmpfinal() && !GameConstants.isContentsMap(this.getMap().getId()) && !GameConstants.보스맵(this.getMap().getId()) && !GameConstants.사냥컨텐츠맵(this.getMap().getId()) && !GameConstants.튜토리얼(this.getMap().getId()) && !GameConstants.로미오줄리엣(this.getMap().getId()) && !GameConstants.피라미드(this.getMap().getId()) && (this.getStats().getLevel() >= killer.getLevel() - 20 && this.getStats().getLevel() <= killer.getLevel() + 20 || killer.isGM())) {
            this.map.setCustomInfo(9930005, this.map.getCustomValue0(9930005) + 1, 0);
            if (this.map.getCustomValue0(9930005) >= 5000) {
               this.map.setCustomInfo(9930005, 0, 0);
            }

            if (this.map.getCustomValue0(9930005) >= 3000) {
               if (Randomizer.nextInt(10000) < 3 && this.map.getPoloFrittoPortal() == null || killer.isGM()) {
                  target = killer.getClient().getChannelServer().getMapFactory().getMap(993000000);
                  MapleMap target2 = killer.getClient().getChannelServer().getMapFactory().getMap(993000100);
                  MapleRandomPortal portal;
                  if (target.characterSize() != 0 && target2.characterSize() != 0) {
                     portal = new MapleRandomPortal(2, this.getTruePosition(), this.map.getId(), killer.getId(), false);
                     this.map.spawnRandomPortal(portal);
                  } else {
                     portal = new MapleRandomPortal(2, this.getTruePosition(), this.map.getId(), killer.getId(), Randomizer.nextBoolean());
                     this.map.spawnRandomPortal(portal);
                  }
               }

               if (Randomizer.nextInt(10000) < 2 && this.map.getFireWolfPortal() == null) {
                  MapleRandomPortal portal = new MapleRandomPortal(3, this.getTruePosition(), this.map.getId(), killer.getId(), false);
                  portal.setPortalType(3);
                  this.map.spawnRandomPortal(portal);
               }
            }
         }

         if (killer.getMapId() == 993000500) {
            target = ChannelServer.getInstance(killer.getClient().getChannel()).getMapFactory().getMap(993000600);
            Iterator var23 = killer.getMap().getAllChracater().iterator();

            while(var23.hasNext()) {
               MapleCharacter chr = (MapleCharacter)var23.next();
               chr.changeMap(target, target.getPortal(0));
               chr.setFWolfKiller(true);
               if (chr.getQuestStatus(16407) == 1) {
                  chr.forceCompleteQuest(16407);
               }
            }
         }

         if (this.getSeperateSoul() <= 0) {
            if (!GameConstants.isContentsMap(this.getMap().getId())) {
               this.spawnRevives(this.getMap());
            }

            if (this.eventInstance != null) {
               this.eventInstance.unregisterMonster(this);
               this.eventInstance = null;
            }

            this.hp = 0L;
            MapleMonster oldSponge = this.getSponge();
            this.sponge = new WeakReference((Object)null);
            if (oldSponge != null && oldSponge.isAlive()) {
               set = true;
               Iterator var29 = this.map.getAllMonstersThreadsafe().iterator();

               while(var29.hasNext()) {
                  MapleMapObject mon = (MapleMapObject)var29.next();
                  mons = (MapleMonster)mon;
                  if (mons.isAlive() && mons.getObjectId() != oldSponge.getObjectId() && mons.getStats().getLevel() > 1 && mons.getObjectId() != this.getObjectId() && (mons.getSponge() == oldSponge || mons.getLinkOid() == oldSponge.getObjectId())) {
                     set = false;
                     break;
                  }
               }

               int dealy = false;
               if (set) {
                  this.map.killMonster(oldSponge, killer, true, false, (byte)1);
               }
            }

            this.reflectpack = null;
            this.nodepack = null;
            this.cancelDropItem();
         }

         v1 = this.highestDamageChar;
         this.highestDamageChar = 0;
         return v1;
      }
   }

   public final void spawnRevives(MapleMap map) {
      List<Integer> toSpawn = this.stats.getRevives();
      if (toSpawn != null && this.getLinkCID() <= 0) {
         AbstractLoadedMapleLife spongy = null;
         Iterator var4;
         int i;
         MapleMonster mob;
         switch(this.getId()) {
         case 6160003:
         case 8820002:
         case 8820003:
         case 8820004:
         case 8820005:
         case 8820006:
         case 8820102:
         case 8820103:
         case 8820104:
         case 8820105:
         case 8820106:
         case 8840000:
         case 8850011:
            break;
         case 8810026:
         case 8810130:
         case 8820008:
         case 8820009:
         case 8820010:
         case 8820011:
         case 8820012:
         case 8820013:
         case 8820108:
         case 8820109:
         case 8820110:
         case 8820111:
         case 8820112:
         case 8820113:
            ArrayList<MapleMonster> mobs = new ArrayList();
            Iterator var10 = toSpawn.iterator();

            while(var10.hasNext()) {
               int i = (Integer)var10.next();
               MapleMonster mob = MapleLifeFactory.getMonster(i);
               mob.setPosition(this.getTruePosition());
               if (this.eventInstance != null) {
                  this.eventInstance.registerMonster(mob);
               }

               if (this.dropsDisabled()) {
                  mob.disableDrops();
               }

               switch(mob.getId()) {
               case 8810018:
               case 8810122:
               case 8820009:
               case 8820010:
               case 8820011:
               case 8820012:
               case 8820013:
               case 8820014:
               case 8820109:
               case 8820110:
               case 8820111:
               case 8820112:
               case 8820113:
               case 8820114:
                  spongy = mob;
                  break;
               default:
                  mobs.add(mob);
               }
            }

            if (spongy != null && map.getMonsterById(spongy.getId()) == null) {
               map.spawnMonster((MapleMonster)spongy, -2);

               for(var10 = mobs.iterator(); var10.hasNext(); mob.setSponge((MapleMonster)spongy)) {
                  mob = (MapleMonster)var10.next();
                  if (mob.getId() != 8820000 && mob.getId() != 8820100) {
                     int type = (mob.getId() < 8810002 || mob.getId() > 8810009) && (mob.getId() < 8810102 || mob.getId() > 8810109) ? -1 : -2;
                     map.spawnMonster(mob, type);
                  } else {
                     map.spawnMonsterDelay(mob, -1, 4300);
                  }
               }
            }
            break;
         case 8820014:
         case 8820114:
            var4 = toSpawn.iterator();

            while(true) {
               while(var4.hasNext()) {
                  i = (Integer)var4.next();
                  mob = MapleLifeFactory.getMonster(i);
                  if (this.eventInstance != null) {
                     this.eventInstance.registerMonster(mob);
                  }

                  mob.setPosition(this.getTruePosition());
                  if (this.dropsDisabled()) {
                     mob.disableDrops();
                  }

                  if (mob.getId() != 8820001 && mob.getId() != 8820101) {
                     map.spawnMonster(mob, -2);
                  } else {
                     if (mob.getId() == 8820101) {
                        mob.setHp(12600000000L);
                        mob.getStats().setHp(12600000000L);
                        mob.setCustomInfo(mob.getId(), 3, 0);
                     }

                     map.spawnMonsterDelay(mob, -2, 2300);
                  }
               }

               return;
            }
         default:
            var4 = toSpawn.iterator();

            while(true) {
               do {
                  if (!var4.hasNext()) {
                     return;
                  }

                  i = (Integer)var4.next();
                  mob = MapleLifeFactory.getMonster(i);
                  if (this.eventInstance != null) {
                     this.eventInstance.registerMonster(mob);
                  }

                  mob.setPosition(this.getTruePosition());
                  if (this.dropsDisabled()) {
                     mob.disableDrops();
                  }

                  if (mob.getId() == 8220102 || mob.getId() == 8220104) {
                     if (map.getAllMonster().size() > 0) {
                        Iterator<MapleMonster> iterator = map.getAllMonster().iterator();
                        if (iterator.hasNext()) {
                           MapleMonster mons = (MapleMonster)iterator.next();
                           mob.setHp((long)((double)(mons.getStats().getHp() * (long)(mob.getId() == 8220102 ? 30 : 50)) * mob.bonusHp()));
                        }
                     } else {
                        mob.setHp((long)((double)(mob.getStats().getHp() * (long)(mob.getId() == 8220102 ? 30 : 50)) * mob.bonusHp()));
                     }
                  }

                  map.spawnRevives(mob, this.getObjectId());
               } while(mob.getId() != 8880316 && mob.getId() != 8880318);

               mob.setDeadTimeKillmob(3000);
            }
         }

      }
   }

   public final boolean isAlive() {
      return this.hp > 0L;
   }

   public final void setCarnivalTeam(byte team) {
      this.carnivalTeam = team;
   }

   public final byte getCarnivalTeam() {
      return this.carnivalTeam;
   }

   public final MapleCharacter getController() {
      return (MapleCharacter)this.controller.get();
   }

   public final void setController(MapleCharacter controller) {
      this.controller = new WeakReference(controller);
   }

   public final void switchController(MapleCharacter newController, boolean immediateAggro) {
      MapleCharacter controllers = this.getController();
      if (controllers != newController) {
         if (controllers != null) {
            controllers.stopControllingMonster(this);
            controllers.getClient().getSession().writeAndFlush(MobPacket.stopControllingMonster(this.getObjectId()));
         }

         newController.controlMonster(this, immediateAggro);
         this.setController(newController);
         if (immediateAggro) {
            this.setControllerHasAggro(true);
         }

      }
   }

   public final void addListener(MonsterListener listener) {
      this.listener = listener;
   }

   public final boolean isControllerHasAggro() {
      return this.controllerHasAggro;
   }

   public final void setControllerHasAggro(boolean controllerHasAggro) {
      this.controllerHasAggro = controllerHasAggro;
   }

   public final void sendSpawnData(MapleClient client) {
      if (this.isAlive() && (this.owner < 0 || this.owner == client.getPlayer().getId())) {
         if (this.getOwner() == -1) {
            client.getSession().writeAndFlush(MobPacket.spawnMonster(this, this.fake && this.linkCID <= 0 ? -4 : -1, 0));
         } else if (this.getOwner() == client.getPlayer().getId()) {
            client.getSession().writeAndFlush(MobPacket.spawnMonster(this, this.fake && this.linkCID <= 0 ? -4 : -1, 0));
         }

         if (this.map != null && !this.stats.isEscort() && client.getPlayer() != null && client.getPlayer().getTruePosition().distanceSq(this.getTruePosition()) <= (double)GameConstants.maxViewRangeSq_Half()) {
            this.map.updateMonsterController(this);
         }

      }
   }

   public final void sendDestroyData(MapleClient client) {
      if (this.stats.isEscort() && this.getEventInstance() != null && this.lastNode >= 0) {
         this.map.resetShammos(client);
      } else {
         client.getSession().writeAndFlush(MobPacket.killMonster(this.getObjectId(), 0));
         if (this.getController() != null && client.getPlayer() != null && client.getPlayer().getId() == this.getController().getId()) {
            client.getPlayer().stopControllingMonster(this);
         }
      }

   }

   public final String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.stats.getName());
      sb.append("(");
      sb.append(this.getId());
      sb.append(") (Level ");
      sb.append(this.stats.getLevel());
      sb.append(") at (X");
      sb.append(this.getTruePosition().x);
      sb.append("/ Y");
      sb.append(this.getTruePosition().y);
      sb.append(") with ");
      sb.append(this.getHp());
      sb.append("/ ");
      sb.append(this.getMobMaxHp());
      sb.append("hp, ");
      sb.append(this.getMp());
      sb.append("/ ");
      sb.append(this.getMobMaxMp());
      sb.append(" mp, oid: ");
      sb.append(this.getObjectId());
      sb.append(" || Controller : ");
      MapleCharacter chr = (MapleCharacter)this.controller.get();
      sb.append(chr != null ? chr.getName() : "none");
      return sb.toString();
   }

   public final MapleMapObjectType getType() {
      return MapleMapObjectType.MONSTER;
   }

   public final EventInstanceManager getEventInstance() {
      return this.eventInstance;
   }

   public final void setEventInstance(EventInstanceManager eventInstance) {
      this.eventInstance = eventInstance;
   }

   public final ElementalEffectiveness getEffectiveness(Element e) {
      return this.stats.getEffectiveness(e);
   }

   public final void setTempEffectiveness(final Element e, long milli) {
      this.stats.setEffectiveness(e, ElementalEffectiveness.WEAK);
      Timer.EtcTimer.getInstance().schedule(new Runnable() {
         public void run() {
            MapleMonster.this.stats.removeEffectiveness(e);
         }
      }, milli);
   }

   public final boolean isBuffed(MonsterStatus status) {
      Iterator itr = this.stati.iterator();

      Pair skill;
      do {
         if (!itr.hasNext()) {
            return false;
         }

         skill = (Pair)itr.next();
      } while(skill == null || skill.getLeft() != status);

      return true;
   }

   public final boolean isBuffed(int skillid) {
      Iterator itr = this.stati.iterator();

      Pair skill;
      do {
         if (!itr.hasNext()) {
            return false;
         }

         skill = (Pair)itr.next();
      } while(skill == null || ((MonsterStatusEffect)skill.getRight()).getSkill() != skillid);

      return true;
   }

   public final MonsterStatusEffect getBuff(MonsterStatus status) {
      Iterator itr = this.stati.iterator();

      Pair skill;
      do {
         if (!itr.hasNext()) {
            return null;
         }

         skill = (Pair)itr.next();
      } while(skill.getLeft() != status);

      return (MonsterStatusEffect)skill.getRight();
   }

   public final MonsterStatusEffect getBuff(int skillid) {
      Iterator itr = this.stati.iterator();

      Pair skill;
      do {
         if (!itr.hasNext()) {
            return null;
         }

         skill = (Pair)itr.next();
      } while(((MonsterStatusEffect)skill.getRight()).getSkill() != skillid);

      return (MonsterStatusEffect)skill.getRight();
   }

   public final void cancelSingleStatus(MonsterStatusEffect stat, int skillid) {
      if (stat != null && this.isAlive()) {
         List<Pair<MonsterStatus, MonsterStatusEffect>> cancelsf = new ArrayList();
         Iterator itr = this.stati.iterator();

         while(itr.hasNext()) {
            Pair<MonsterStatus, MonsterStatusEffect> skill = (Pair)itr.next();
            if (((MonsterStatusEffect)skill.getRight()).getSkill() == skillid) {
               cancelsf.add(new Pair((MonsterStatus)skill.getLeft(), (MonsterStatusEffect)skill.getRight()));
               this.cancelStatus(cancelsf);
               break;
            }
         }

      }
   }

   public final int getBurnedBuffSize(int skillid) {
      int size = 0;
      Iterator itr = this.stati.iterator();

      while(itr.hasNext()) {
         Pair<MonsterStatus, MonsterStatusEffect> skill = (Pair)itr.next();
         if (((MonsterStatusEffect)skill.getRight()).getSkill() == skillid && skill.getLeft() == MonsterStatus.MS_Burned) {
            ++size;
         }
      }

      return size;
   }

   public final int getBurnedBuffSize() {
      int size = 0;
      Iterator itr = this.stati.iterator();

      while(itr.hasNext()) {
         Pair<MonsterStatus, MonsterStatusEffect> skill = (Pair)itr.next();
         if (skill.getLeft() == MonsterStatus.MS_Burned) {
            ++size;
         }
      }

      return size;
   }

   public final void setFake(boolean fake) {
      this.fake = fake;
   }

   public final boolean isFake() {
      return this.fake;
   }

   public final MapleMap getMap() {
      return this.map;
   }

   public final List<MobSkill> getSkills() {
      return this.stats.getSkills();
   }

   public final boolean hasSkill(int skillId, int level) {
      return this.stats.hasSkill(skillId, level);
   }

   public final long getLastSkillUsed(int skillId, int skillLevel) {
      Iterator var3 = this.usedSkills.entrySet().iterator();

      Entry kvp;
      do {
         if (!var3.hasNext()) {
            return 0L;
         }

         kvp = (Entry)var3.next();
      } while(((MobSkill)kvp.getKey()).getSkillId() != skillId || ((MobSkill)kvp.getKey()).getSkillLevel() != skillLevel);

      return (Long)kvp.getValue();
   }

   public final void setLastSkillUsed(MobSkill msi, long now, long cooltime) {
      if ((this.getId() == 8880101 || this.getId() == 8880111) && msi.getSkillId() == 170) {
         cooltime *= 2L;
      }

      this.usedSkills.put(msi, now + cooltime);
   }

   public final byte getNoSkills() {
      return this.stats.getNoSkills();
   }

   public final boolean isFirstAttack() {
      return this.stats.isFirstAttack();
   }

   public final int getBuffToGive() {
      return this.stats.getBuffToGive();
   }

   public void applyStatus(MapleClient c, List<Pair<MonsterStatus, MonsterStatusEffect>> datas, SecondaryStatEffect effect) {
      int igkey = false;
      boolean bind = false;
      boolean alreadybind = false;
      this.RemoveStati((MonsterStatus)null, (MonsterStatusEffect)null, false);
      Pair<MonsterStatus, MonsterStatusEffect> re = null;
      Iterator var8 = datas.iterator();

      while(true) {
         while(var8.hasNext()) {
            Pair<MonsterStatus, MonsterStatusEffect> data = (Pair)var8.next();
            Ignition ig = null;
            int maxSuperPos = ((MonsterStatusEffect)data.getRight()).getSkill() != 4120011 && ((MonsterStatusEffect)data.getRight()).getSkill() != 4220011 && ((MonsterStatusEffect)data.getRight()).getSkill() != 4340012 ? effect.getDotSuperpos() : 3;
            int dotSuperpos = 0;
            Iterator var13 = this.getIgnitions().iterator();

            while(var13.hasNext()) {
               Ignition ign = (Ignition)var13.next();
               if (ign.getSkill() == ((MonsterStatusEffect)data.getRight()).getSkill()) {
                  ++dotSuperpos;
               }
            }

            if (data.getLeft() == MonsterStatus.MS_Freeze) {
               re = data;
               bind = true;
               if (this.getBuff(MonsterStatus.MS_Freeze) != null && effect.getSourceId() != 100001283 && effect.getSourceId() != 101120110) {
                  alreadybind = true;
                  continue;
               }
            }

            MapleMonster.CancelStatusAction action = new MapleMonster.CancelStatusAction(this, (MonsterStatus)data.getLeft(), (MonsterStatusEffect)data.getRight());
            ScheduledFuture<?> schedule = Timer.BuffTimer.getInstance().schedule(() -> {
               action.run();
            }, (long)((MonsterStatusEffect)data.getRight()).getDuration());
            ((MonsterStatusEffect)data.getRight()).setSchedule(schedule);
            ((MonsterStatusEffect)data.getRight()).setChr(c.getPlayer());
            ((MonsterStatusEffect)data.getRight()).setStati((MonsterStatus)data.getLeft());
            if (this.isBuffed((MonsterStatus)data.getLeft())) {
               if (data.getLeft() == MonsterStatus.MS_Burned) {
                  if (dotSuperpos >= maxSuperPos) {
                     this.cancelStatus((MonsterStatus)data.getLeft(), (MonsterStatusEffect)data.getRight());
                  }
               } else {
                  this.cancelStatus((MonsterStatus)data.getLeft(), (MonsterStatusEffect)data.getRight());
               }
            }

            if (((MonsterStatus)data.getLeft()).isStacked()) {
               MonsterStatusEffect already = null;
               Iterator var16 = this.getIndielist().iterator();

               while(var16.hasNext()) {
                  MonsterStatusEffect alreadyindie = (MonsterStatusEffect)var16.next();
                  if (alreadyindie.getSkill() == ((MonsterStatusEffect)data.getRight()).getSkill()) {
                     already = alreadyindie;
                     break;
                  }
               }

               if (already != null) {
                  this.getIndielist().remove(already);
               }

               this.getIndielist().add((MonsterStatusEffect)data.getRight());
            }

            if (data.getLeft() == MonsterStatus.MS_Burned) {
               long value = 0L;
               dotSuperpos = 0;
               Iterator var25 = this.getIgnitions().iterator();

               while(var25.hasNext()) {
                  Ignition ign = (Ignition)var25.next();
                  if (ign.getSkill() == ((MonsterStatusEffect)data.getRight()).getSkill()) {
                     ++dotSuperpos;
                     value = ign.getDamage();
                     break;
                  }
               }

               if (effect.getDotSuperpos() == 0 && dotSuperpos <= 0 || dotSuperpos < maxSuperPos) {
                  int interval = effect.getDotInterval();
                  int du = ((MonsterStatusEffect)data.getRight()).getDuration();
                  if (c.getPlayer().getSkillLevel(2110000) > 0) {
                     SecondaryStatEffect extremeMagic = SkillFactory.getSkill(2110000).getEffect(c.getPlayer().getSkillLevel(2110000));
                     ((MonsterStatusEffect)data.getRight()).setDuration(((MonsterStatusEffect)data.getRight()).getDuration() * (100 + extremeMagic.getX()) / 100);
                  }

                  if (effect.getDotInterval() > 0 && ((MonsterStatusEffect)data.getRight()).getValue() > 0L) {
                     ((MonsterStatusEffect)data.getRight()).setLastPoisonTime(System.currentTimeMillis());
                     ((MonsterStatusEffect)data.getRight()).setInterval(interval);
                  }

                  ig = new Ignition(c.getPlayer().getId(), ((MonsterStatusEffect)data.getRight()).getSkill(), value > 0L ? value : ((MonsterStatusEffect)data.getRight()).getValue(), interval, du);
                  ((MonsterStatusEffect)data.getRight()).setKey(ig.getIgnitionKey());
                  int var20 = ig.getIgnitionKey();
               }
            } else if (data.getLeft() == MonsterStatus.MS_SeperateSoulP && this.getStats().getCategory() != 1) {
               MapleMonster mob = MapleLifeFactory.getMonster(this.getId());
               mob.setSeperateSoul(this.getObjectId());
               c.getPlayer().getMap().spawnMonsterOnGroundBelow(mob, this.getPosition());
               mob.applyStatus(c, MonsterStatus.MS_SeperateSoulC, (MonsterStatusEffect)data.getRight(), (int)((MonsterStatusEffect)data.getRight()).getValue() + 2000, effect);
               mob.applyStatus(c, MonsterStatus.MS_Stun, (MonsterStatusEffect)data.getRight(), (int)((MonsterStatusEffect)data.getRight()).getValue() + 2000, effect);
               if (effect.getDuration() > 0) {
                  Timer.MobTimer.getInstance().schedule(() -> {
                     try {
                        mob.getMap().killMonster(mob, c.getPlayer(), false, false, (byte)0);
                     } catch (Exception var3) {
                        var3.printStackTrace();
                     }

                  }, (long)effect.getDuration());
               } else {
                  mob.getMap().killMonster(mob, c.getPlayer(), false, false, (byte)0);
               }
            }

            if (this.getStats().isBoss() && data.getLeft() == MonsterStatus.MS_Stun) {
               re = data;
               this.setCustomInfo(1, 1, 0);
            } else if ((!this.isResist() || effect.getSourceId() == 100001283 || effect.getSourceId() == 101120110) && data.getLeft() == MonsterStatus.MS_Freeze) {
               this.stati.add(new Pair((MonsterStatus)data.getLeft(), (MonsterStatusEffect)data.getRight()));
            } else if (data.getLeft() != MonsterStatus.MS_Freeze) {
               this.stati.add(new Pair((MonsterStatus)data.getLeft(), (MonsterStatusEffect)data.getRight()));
               if (ig != null) {
                  this.getIgnitions().add(ig);
               }
            }
         }

         if (bind && effect.getSourceId() != 100001283 && effect.getSourceId() != 101120110) {
            if (!alreadybind && this.getStats().getIgnoreMovable() <= 0) {
               if (!this.isResist()) {
                  this.setResist(System.currentTimeMillis());
               } else {
                  datas.remove(re);
                  c.getSession().writeAndFlush(MobPacket.monsterResist(this, c.getPlayer(), (int)(90L - (System.currentTimeMillis() - this.lastBindTime) / 1000L), effect.getSourceId()));
                  if (effect.getSourceId() == 64121001) {
                     c.getPlayer().cancelEffect(effect);
                     effect.applyTo(c.getPlayer(), false, 5000);
                  }
               }
            } else {
               if (this.getStats().getIgnoreMovable() > 0) {
                  c.send(CWvsContext.serverNotice(5, "", this.getStats().getIgnoreMoveableMsg()));
               }

               datas.remove(re);
            }
         }

         if (re != null && this.getCustomValue0(1) == 1L) {
            this.removeCustomInfo(1);
            datas.remove(re);
         }

         if (!datas.isEmpty()) {
            this.map.broadcastMessage(MobPacket.applyMonsterStatus(this, datas, false, effect));
            if (effect.getSourceId() == 37121004) {
               this.map.broadcastMessage(CField.RebolvingBunk(c.getPlayer().getId(), this.getObjectId(), this.getId(), this.getPosition()));
            }
         }

         return;
      }
   }

   public void applyStatus(MapleClient c, MonsterStatus status, MonsterStatusEffect effect, int value, SecondaryStatEffect eff) {
      if (!this.isBuffed(MonsterStatus.MS_PCounter) && !this.isBuffed(MonsterStatus.MS_MCounter) && !this.isBuffed(MonsterStatus.MS_PImmune) && !this.isBuffed(MonsterStatus.MS_MImmune)) {
         if (effect.getSkill() == 80001227 || status != MonsterStatus.MS_Stun && status != MonsterStatus.MS_Seal || !this.getStats().isBoss() || this.isBuffed(MonsterStatus.MS_SeperateSoulC)) {
            if (c != null && effect != null && eff != null) {
               int igkey = false;
               int dotSuperpos = 0;
               int maxSuperPos = effect.getSkill() != 4120011 && effect.getSkill() != 4220011 && effect.getSkill() != 4340012 ? eff.getDotSuperpos() : 3;
               this.RemoveStati((MonsterStatus)null, (MonsterStatusEffect)null, false);
               if (status == MonsterStatus.MS_Burned) {
                  long value3 = 0L;
                  Iterator var11 = this.getIgnitions().iterator();

                  while(var11.hasNext()) {
                     Ignition ig = (Ignition)var11.next();
                     if (ig.getSkill() == effect.getSkill()) {
                        ++dotSuperpos;
                        value3 = ig.getDamage();
                     }
                  }

                  if (dotSuperpos < eff.getDotSuperpos()) {
                     if (eff.getDotInterval() > 0 && effect.getValue() > 0L) {
                        effect.setLastPoisonTime(System.currentTimeMillis());
                        effect.setInterval(eff.getDotInterval());
                     }

                     Ignition ig = new Ignition(c.getPlayer().getId(), effect.getSkill(), value3 > 0L ? value3 : effect.getValue(), eff.getDotInterval(), effect.getDuration());
                     this.getIgnitions().add(ig);
                     effect.setKey(ig.getIgnitionKey());
                     int var14 = ig.getIgnitionKey();
                  }
               }

               if (status == MonsterStatus.MS_SeperateSoulP && this.getStats().getCategory() != 1) {
                  MapleMonster mob = MapleLifeFactory.getMonster(this.getId());
                  c.getPlayer().getMap().spawnMonsterOnGroundBelow(mob, this.getPosition());
                  mob.setSeperateSoul(this.getObjectId());
                  mob.applyStatus(c, MonsterStatus.MS_SeperateSoulC, effect, value, eff);
                  mob.applyStatus(c, MonsterStatus.MS_Stun, effect, value, eff);
                  if (effect.getDuration() > 0) {
                     try {
                        Timer.MobTimer.getInstance().schedule(() -> {
                           try {
                              mob.getMap().killMonster(mob, c.getPlayer(), false, false, (byte)0);
                           } catch (Exception var3) {
                              var3.printStackTrace();
                           }

                        }, (long)effect.getDuration());
                     } catch (Exception var13) {
                        var13.printStackTrace();
                     }
                  } else {
                     mob.getMap().killMonster(mob, c.getPlayer(), false, false, (byte)0);
                  }
               }

               if (this.isBuffed(status)) {
                  if (status == MonsterStatus.MS_Burned) {
                     if (dotSuperpos >= maxSuperPos) {
                        this.cancelStatus(status, effect);
                     }
                  } else {
                     this.cancelStatus(status, effect);
                  }
               }

               if (status.isStacked()) {
                  MonsterStatusEffect already = null;
                  Iterator var10 = this.getIndielist().iterator();

                  while(var10.hasNext()) {
                     MonsterStatusEffect alreadyindie = (MonsterStatusEffect)var10.next();
                     if (alreadyindie.getSkill() == effect.getSkill()) {
                        already = alreadyindie;
                        break;
                     }
                  }

                  if (already != null) {
                     this.getIndielist().remove(already);
                  }

                  this.getIndielist().add(effect);
               }

               MapleMonster.CancelStatusAction action = new MapleMonster.CancelStatusAction(this, status, effect);
               ScheduledFuture<?> schedule = Timer.BuffTimer.getInstance().schedule(() -> {
                  action.run();
               }, (long)effect.getDuration());
               effect.setSchedule(schedule);
               effect.setValue((long)value);
               effect.setStati(status);
               effect.setChr(c.getPlayer());
               effect.setCid(c.getPlayer().getId());
               this.stati.add(new Pair(status, effect));
               List<Pair<MonsterStatus, MonsterStatusEffect>> applys = new ArrayList();
               applys.add(new Pair(status, effect));
               this.map.broadcastMessage(MobPacket.applyMonsterStatus(this, applys, false, eff));
               if (effect.getDuration() < 0) {
                  this.cancelStatus(status, effect);
               }

            }
         }
      }
   }

   public int getSeperateSoul() {
      return this.seperateSoul;
   }

   private void setSeperateSoul(int id) {
      this.seperateSoul = id;
   }

   public final void cancelStatus(List<Pair<MonsterStatus, MonsterStatusEffect>> cancels) {
      MapleCharacter con = this.getController();
      List<Pair<MonsterStatus, MonsterStatusEffect>> cancelsf = new ArrayList();
      List<MonsterStatusEffect> removeindie = new ArrayList();
      Iterator var5 = cancels.iterator();

      while(true) {
         while(var5.hasNext()) {
            Pair<MonsterStatus, MonsterStatusEffect> cancel = (Pair)var5.next();
            cancelsf.add(new Pair((MonsterStatus)cancel.getLeft(), (MonsterStatusEffect)cancel.getRight()));
            this.RemoveStati((MonsterStatus)cancel.getLeft(), (MonsterStatusEffect)cancel.getRight(), true);
            ScheduledFuture<?> schedule = ((MonsterStatusEffect)cancel.getRight()).getSchedule();
            if (schedule != null && !schedule.isCancelled()) {
               schedule.cancel(true);
            }

            if (((MonsterStatus)cancel.getLeft()).isStacked()) {
               removeindie.add((MonsterStatusEffect)cancel.getRight());
               this.getIndielist().remove(cancel.getRight());
            }

            if (((MonsterStatusEffect)cancel.getRight()).getSkill() != 12101024 && ((MonsterStatusEffect)cancel.getRight()).getSkill() != 12121002) {
               if (((MonsterStatusEffect)cancel.getRight()).getSkill() == 11121004) {
                  this.map.broadcastMessage(CField.ignitionBomb(11121013, this.getObjectId(), this.getTruePosition()));
               }
            } else {
               this.map.broadcastMessage(CField.ignitionBomb(12100029, this.getObjectId(), this.getTruePosition()));
            }
         }

         if (!cancelsf.isEmpty()) {
            this.map.broadcastMessage(MobPacket.cancelMonsterStatus(this, cancelsf, removeindie), this.getTruePosition());
            this.map.broadcastMessage(MobPacket.applyMonsterStatus(this, this.stati, false, (SecondaryStatEffect)null));
         }

         return;
      }
   }

   public final void cancelStatus(MonsterStatus stat, MonsterStatusEffect effect) {
      this.cancelStatus(stat, effect, false);
   }

   public final void cancelStatus(MonsterStatus stat, MonsterStatusEffect effect, boolean autocancel) {
      MonsterStatusEffect mse = null;
      Iterator var5 = this.stati.iterator();

      while(var5.hasNext()) {
         Pair<MonsterStatus, MonsterStatusEffect> ms = (Pair)var5.next();
         if (ms != null && effect != null && ms.getLeft() == stat && ((MonsterStatusEffect)ms.getRight()).getSkill() == effect.getSkill()) {
            mse = (MonsterStatusEffect)ms.getRight();
            break;
         }
      }

      if (stat != null && this.stati != null && this.isBuffed(stat)) {
         if (mse != null && this.isAlive()) {
            MapleCharacter con = this.getController();
            List<Pair<MonsterStatus, MonsterStatusEffect>> cancels = new ArrayList();
            cancels.add(new Pair(stat, mse));
            Iterator var7 = this.stati.iterator();

            while(var7.hasNext()) {
               Pair<MonsterStatus, MonsterStatusEffect> list = (Pair)var7.next();
               if (((MonsterStatusEffect)list.getRight()).getSkill() == mse.getSkill()) {
                  cancels.add(new Pair((MonsterStatus)list.getLeft(), (MonsterStatusEffect)list.getRight()));
               }
            }

            ScheduledFuture<?> schedule = mse.getSchedule();
            if (schedule != null && !schedule.isCancelled()) {
               schedule.cancel(false);
            }

            List<MonsterStatusEffect> removeindie = new ArrayList();
            if (stat.isStacked()) {
               removeindie.add(mse);
               this.getIndielist().remove(mse);
            }

            if (removeindie.size() < 2) {
               removeindie.clear();
            }

            this.RemoveStati(stat, mse, true);
            if (!cancels.isEmpty()) {
               if (con != null) {
                  this.map.broadcastMessage(con, MobPacket.cancelMonsterStatus(this, cancels, removeindie), this.getTruePosition());
                  con.getClient().getSession().writeAndFlush(MobPacket.cancelMonsterStatus(this, cancels, removeindie));
               } else {
                  this.map.broadcastMessage(MobPacket.cancelMonsterStatus(this, cancels), this.getTruePosition());
               }

               if (autocancel && mse.getSkill() == 241 && MonsterStatus.MS_PopulatusTimer.getFlag() == stat.getFlag() && (this.getId() == 8500021 || this.getId() == 8500011 || this.getId() == 8500001)) {
                  MobSkill ms = MobSkillFactory.getMobSkill(241, this.getHPPercent() >= 50 ? 2 : 1);
                  this.setLastSkillUsed(ms, System.currentTimeMillis(), ms.getInterval() + 20000L);
                  Iterator var10 = this.getMap().getAllChracater().iterator();

                  while(var10.hasNext()) {
                     MapleCharacter chr = (MapleCharacter)var10.next();
                     if (chr.hasDisease(SecondaryStat.PapulCuss) && chr.getSkillCustomValue(241) != null) {
                        chr.cancelDisease(SecondaryStat.PapulCuss);
                        int du = chr.getSkillCustomTime(241);
                        if (du > 30000) {
                           du = 30000;
                        }

                        ms.setDuration((long)du);
                        chr.giveDebuff(this.getHPPercent() >= 50 ? SecondaryStat.Seal : SecondaryStat.Stun, ms);
                     }
                  }
               }

               if (con != null) {
                  if (mse.getSkill() != 12101024 && mse.getSkill() != 12121002) {
                     if (mse.getSkill() == 11121004) {
                        this.map.broadcastMessage(CField.ignitionBomb(11121013, this.getObjectId(), this.getTruePosition()));
                        this.map.broadcastMessage(con, CField.EffectPacket.showEffect(con, 0, 11121013, 10, 0, 0, (byte)0, true, this.getTruePosition(), (String)null, (Item)null), false);
                        con.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(con, 0, 11121013, 10, 0, 0, (byte)0, false, this.getTruePosition(), (String)null, (Item)null));
                     }
                  } else {
                     this.map.broadcastMessage(CField.ignitionBomb(12100029, this.getObjectId(), this.getTruePosition()));
                     this.map.broadcastMessage(CField.EffectPacket.showEffect(con, 0, 12100029, 10, 0, 0, (byte)0, true, this.getTruePosition(), (String)null, (Item)null));
                  }
               }
            }

         }
      }
   }

   public final void cancelSingleStatus(MonsterStatusEffect stat) {
      if (stat != null && this.isAlive()) {
         this.cancelStatus(stat.getStati(), stat);
      }
   }

   public final void dispels() {
      Iterator var1 = this.stati.iterator();

      while(var1.hasNext()) {
         Pair<MonsterStatus, MonsterStatusEffect> stat = (Pair)var1.next();
         switch((MonsterStatus)stat.getLeft()) {
         case MS_Pad:
         case MS_Pdr:
         case MS_Mad:
         case MS_Mdr:
         case MS_Acc:
         case MS_Eva:
         case MS_Speed:
         case MS_Powerup:
         case MS_Magicup:
         case MS_PGuardup:
         case MS_MGuardup:
         case MS_PImmune:
         case MS_MImmune:
         case MS_Hardskin:
         case MS_PowerImmune:
            if (((MonsterStatusEffect)stat.getRight()).getValue() > 0L) {
               this.cancelStatus((MonsterStatus)stat.getLeft(), (MonsterStatusEffect)stat.getRight());
            }
         }
      }

   }

   public int getLinkOid() {
      return this.linkoid;
   }

   public void setLinkOid(int lo) {
      this.linkoid = lo;
   }

   public final List<Pair<MonsterStatus, MonsterStatusEffect>> getStati() {
      return this.stati;
   }

   public final int getStolen() {
      return this.stolen;
   }

   public final void setStolen(int s) {
      this.stolen = s;
   }

   public final void setLastNode(int lastNode) {
      this.lastNode = lastNode;
   }

   public final int getLastNode() {
      return this.lastNode;
   }

   public final void cancelDropItem() {
      this.lastDropTime = 0L;
   }

   public final void startDropItemSchedule() {
      this.cancelDropItem();
      if (this.stats.getDropItemPeriod() > 0 && this.isAlive()) {
         this.shouldDropItem = false;
         this.lastDropTime = System.currentTimeMillis();
      }
   }

   public boolean shouldDrop(long now) {
      return this.lastDropTime > 0L && this.lastDropTime + (long)(this.stats.getDropItemPeriod() * 1000) < now;
   }

   public void doDropItem(long now) {
      switch(this.getId()) {
      case 9300061:
         int itemId = 4001101;
         if (this.isAlive() && this.map != null) {
            if (this.shouldDropItem) {
               this.map.spawnAutoDrop(itemId, this.getTruePosition());
            } else {
               this.shouldDropItem = true;
            }
         }

         this.lastDropTime = now;
         return;
      default:
         this.cancelDropItem();
      }
   }

   public byte[] getNodePacket() {
      return this.nodepack;
   }

   public void setNodePacket(byte[] np) {
      this.nodepack = np;
   }

   public void registerKill(long next) {
      this.nextKill = System.currentTimeMillis() + next;
   }

   public boolean shouldKill(long now) {
      return this.nextKill > 0L && now > this.nextKill;
   }

   public int getLinkCID() {
      return this.linkCID;
   }

   public void setLinkCID(int lc) {
      this.linkCID = lc;
      if (lc > 0) {
      }

   }

   public void applyMonsterBuff(MapleMap map, List<Pair<MonsterStatus, MonsterStatusEffect>> stats, MobSkill mobSkill) {
      if (!this.isBuffed(MonsterStatus.MS_PCounter) && !this.isBuffed(MonsterStatus.MS_MCounter) && !this.isBuffed(MonsterStatus.MS_PImmune) && !this.isBuffed(MonsterStatus.MS_MImmune)) {
         Iterator var4 = stats.iterator();

         while(var4.hasNext()) {
            Pair<MonsterStatus, MonsterStatusEffect> e = (Pair)var4.next();
            ((MonsterStatusEffect)e.getRight()).setLevel(mobSkill.getSkillLevel());
            int time = 0;
            if (((MonsterStatus)e.getLeft()).getFlag() == MonsterStatus.MS_ExchangeAttack.getFlag() || ((MonsterStatus)e.getLeft()).getFlag() == MonsterStatus.MS_PopulatusTimer.getFlag()) {
               time = '\uea60';
            }

            MapleMonster.CancelStatusAction action = new MapleMonster.CancelStatusAction(this, (MonsterStatus)e.getLeft(), (MonsterStatusEffect)e.getRight());
            ScheduledFuture<?> schedule = Timer.BuffTimer.getInstance().schedule(() -> {
               action.run();
            }, time > 0 ? (long)time : (long)((MonsterStatusEffect)e.getRight()).getDuration());
            ((MonsterStatusEffect)e.getRight()).setMobskill(true);
            if (this.isBuffed((MonsterStatus)e.getLeft())) {
               this.cancelStatus((MonsterStatus)e.getLeft(), (MonsterStatusEffect)e.getRight());
            }

            ((MonsterStatusEffect)e.getRight()).setSchedule(schedule);
            this.stati.add(new Pair((MonsterStatus)e.getLeft(), (MonsterStatusEffect)e.getRight()));
         }

         map.broadcastMessage(MobPacket.applyMonsterStatus(this, stats, true, (SecondaryStatEffect)null));
         if (mobSkill.getDuration() < 0L) {
            this.cancelStatus(stats);
         }

      }
   }

   public int getNextSkill() {
      return this.nextSkill;
   }

   public void setNextSkill(int nextSkill) {
      this.nextSkill = nextSkill;
   }

   public int getNextSkillLvl() {
      return this.nextSkillLvl;
   }

   public void setNextSkillLvl(int nextSkillLvl) {
      this.nextSkillLvl = nextSkillLvl;
   }

   public boolean isResist() {
      return System.currentTimeMillis() - this.lastBindTime < 90000L;
   }

   public long getResist() {
      return this.lastBindTime;
   }

   public void setResist(long time) {
      this.lastBindTime = time;
   }

   public int getAirFrame() {
      return this.airFrame;
   }

   public void setAirFrame(int airFrame) {
      this.airFrame = airFrame;
   }

   public long getSpawnTime() {
      return this.spawnTime;
   }

   public void setSpawnTime(long spawnTime) {
      this.spawnTime = spawnTime;
   }

   public byte getPhase() {
      return this.phase;
   }

   public void setPhase(byte phase) {
      this.phase = phase;
   }

   public int getFreezingOverlap() {
      return this.freezingOverlap;
   }

   public void setFreezingOverlap(int freezingOverlap) {
      this.freezingOverlap = freezingOverlap;
   }

   public boolean isMobGroup() {
      return this.isMobGroup;
   }

   public void setMobGroup(boolean isMobGroup) {
      this.isMobGroup = isMobGroup;
   }

   public List<Integer> getSpawnList() {
      return this.spawnList;
   }

   public void setSpawnList(List<Integer> spawnList) {
      this.spawnList = spawnList;
   }

   public boolean isSkillForbid() {
      return this.isSkillForbid;
   }

   public void setSkillForbid(boolean isSkillForbid) {
      this.isSkillForbid = isSkillForbid;
   }

   public List<Integer> getWillHplist() {
      return this.willHplist;
   }

   public void setWillHplist(List<Integer> willHplist) {
      this.willHplist = willHplist;
   }

   public boolean isUseSpecialSkill() {
      return this.useSpecialSkill;
   }

   public void setUseSpecialSkill(boolean useSpecialSkill) {
      this.useSpecialSkill = useSpecialSkill;
   }

   public List<MapleEnergySphere> getSpheres() {
      return this.spheres;
   }

   public void setSpheres(List<MapleEnergySphere> spheres) {
      this.spheres = spheres;
   }

   public int getScale() {
      return this.scale;
   }

   public void setScale(int scale) {
      this.scale = scale;
   }

   public int getEliteGrade() {
      return this.eliteGrade;
   }

   public void setEliteGrade(int eliteGrade) {
      this.eliteGrade = eliteGrade;
   }

   public List<Pair<Integer, Integer>> getEliteGradeInfo() {
      return this.eliteGradeInfo;
   }

   public void setEliteGradeInfo(List<Pair<Integer, Integer>> eliteGradeInfo) {
      this.eliteGradeInfo = eliteGradeInfo;
   }

   public int getEliteType() {
      return this.eliteType;
   }

   public void setEliteType(int eliteType) {
      this.eliteType = eliteType;
   }

   public int getCurseBound() {
      return this.curseBound;
   }

   public void setCurseBound(int curseBound) {
      this.curseBound = curseBound;
   }

   public byte getBigbangCount() {
      return this.bigbangCount;
   }

   public void setBigbangCount(byte bigbangCount) {
      this.bigbangCount = bigbangCount;
   }

   public int getSpiritGate() {
      return this.spiritGate;
   }

   public void setSpiritGate(int spiritGate) {
      this.spiritGate = spiritGate;
   }

   public List<Ignition> getIgnitions() {
      return this.ignitions;
   }

   public void setIgnitions(List<Ignition> ignitions) {
      this.ignitions = ignitions;
   }

   public ScheduledFuture<?> getSchedule() {
      return this.schedule;
   }

   public void setSchedule(ScheduledFuture<?> schedule) {
      this.schedule = schedule;
   }

   public long getBarrier() {
      return this.barrier;
   }

   public void setBarrier(long barrier) {
      this.barrier = barrier;
   }

   public int getAnotherByte() {
      return this.anotherByte;
   }

   public void setAnotherByte(int anotherByte) {
      this.anotherByte = anotherByte;
   }

   public boolean isDemianChangePhase() {
      return this.demianChangePhase;
   }

   public void setDemianChangePhase(boolean demianChangePhase) {
      this.demianChangePhase = demianChangePhase;
   }

   public long getLastCriticalBindTime() {
      return this.lastCriticalBindTime;
   }

   public void setLastCriticalBindTime(long lastCriticalBindTime) {
      this.lastCriticalBindTime = lastCriticalBindTime;
   }

   public long getLastSpecialAttackTime() {
      return this.lastSpecialAttackTime;
   }

   public void setLastSpecialAttackTime(long lastSpecialAttackTime) {
      this.lastSpecialAttackTime = lastSpecialAttackTime;
   }

   public boolean isExtreme() {
      return this.extreme;
   }

   public void setExtreme(boolean extreme) {
      this.extreme = extreme;
   }

   public boolean isHellMode() {
      return this.hellMode;
   }

   public void setHellMode(boolean HellMode) {
      this.hellMode = HellMode;
   }

   public long getLastSeedCountedTime() {
      return this.lastSeedCountedTime;
   }

   public void setLastSeedCountedTime(long lastSeedCountedTime) {
      this.lastSeedCountedTime = lastSeedCountedTime;
   }

   public Long getCustomValue(int skillid) {
      return this.customInfo.containsKey(skillid) ? ((SkillCustomInfo)this.customInfo.get(skillid)).getValue() : null;
   }

   public Integer getCustomTime(int skillid) {
      return this.customInfo.containsKey(skillid) ? (int)(((SkillCustomInfo)this.customInfo.get(skillid)).getEndTime() - System.currentTimeMillis()) : null;
   }

   public long getCustomValue0(int skillid) {
      return this.customInfo.containsKey(skillid) ? ((SkillCustomInfo)this.customInfo.get(skillid)).getValue() : 0L;
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

   public void addSkillCustomInfo(int skillid, long value) {
      this.customInfo.put(skillid, new SkillCustomInfo(this.getCustomValue0(skillid) + value, 0L));
   }

   public Map<Integer, SkillCustomInfo> getCustomValues() {
      return this.customInfo;
   }

   public final void RemoveStati(MonsterStatus stat, MonsterStatusEffect effect, boolean ig) {
      List<Pair<MonsterStatus, MonsterStatusEffect>> remove = new ArrayList();
      List<Ignition> removes = new ArrayList();
      Iterator<Pair<MonsterStatus, MonsterStatusEffect>> itr = this.stati.iterator();
      Iterator<Ignition> igitr = this.getIgnitions().iterator();
      Pair skill;
      Ignition ignition;
      if (ig && stat != null && effect != null) {
         while(itr.hasNext()) {
            skill = (Pair)itr.next();
            if (stat == skill.getLeft() && ((MonsterStatusEffect)skill.getRight()).getSkill() == effect.getSkill()) {
               remove.add(skill);
               break;
            }
         }

         if (stat == MonsterStatus.MS_Burned) {
            while(igitr.hasNext()) {
               ignition = (Ignition)igitr.next();
               if (effect.getSkill() == ignition.getSkill()) {
                  removes.add(ignition);
                  break;
               }
            }
         }
      }

      while(itr.hasNext()) {
         skill = (Pair)itr.next();
         if (System.currentTimeMillis() >= ((MonsterStatusEffect)skill.getRight()).getStartTime() + (long)((MonsterStatusEffect)skill.getRight()).getDuration()) {
            remove.add(new Pair((MonsterStatus)skill.getLeft(), (MonsterStatusEffect)skill.getRight()));
         }
      }

      while(igitr.hasNext()) {
         ignition = (Ignition)igitr.next();
         if (System.currentTimeMillis() >= ignition.getStartTime() + (long)ignition.getDuration()) {
            removes.add(ignition);
         }
      }

      if (!remove.isEmpty()) {
         this.stati.removeAll(remove);
      }

      if (!removes.isEmpty()) {
         this.getIgnitions().removeAll(removes);
      }

   }

   public final void handleSteal(MapleCharacter chr) {
      double showdown = 100.0D;
      MonsterStatusEffect mse = this.getBuff(MonsterStatus.MS_Showdown);
      if (mse != null) {
         showdown += (double)mse.getValue();
      }

      Skill steal = SkillFactory.getSkill(4201004);
      int level = chr.getTotalSkillLevel(steal);
      int chServerrate = ChannelServer.getInstance(chr.getClient().getChannel()).getDropRate();
      if (level > 0 && !this.getStats().isBoss() && this.stolen == -1 && steal.getEffect(level).makeChanceResult()) {
         MapleMonsterInformationProvider mi = MapleMonsterInformationProvider.getInstance();
         List<MonsterDropEntry> de = mi.retrieveDrop(this.getId());
         if (de == null) {
            this.stolen = 0;
            return;
         }

         List<MonsterDropEntry> dropEntry = new ArrayList(de);
         Collections.shuffle(dropEntry);
         Iterator var11 = dropEntry.iterator();

         while(var11.hasNext()) {
            MonsterDropEntry d = (MonsterDropEntry)var11.next();
            if (d.itemId > 0 && d.questid == 0 && d.itemId / 10000 != 238 && Randomizer.nextInt(999999) < (int)((double)(10 * d.chance * chServerrate * chr.getDropMod()) * chr.getStat().dropBuff / 100.0D * showdown / 100.0D)) {
               Item idrop;
               if (GameConstants.getInventoryType(d.itemId) == MapleInventoryType.EQUIP) {
                  idrop = MapleItemInformationProvider.getInstance().getEquipById(d.itemId);
               } else {
                  idrop = new Item(d.itemId, (short)0, (short)(d.Maximum != 1 ? Randomizer.nextInt(d.Maximum - d.Minimum) + d.Minimum : 1), 0);
               }

               this.stolen = d.itemId;
               this.map.spawnMobDrop(idrop, this.map.calcDropPos(this.getPosition(), this.getTruePosition()), this, chr, (byte)0, 0);
               break;
            }
         }
      } else {
         this.stolen = 0;
      }

   }

   public List<MonsterStatusEffect> getIndielist() {
      return this.indielist;
   }

   public void setIndielist(List<MonsterStatusEffect> indielist) {
      this.indielist = indielist;
   }

   public boolean isElitemonster() {
      return this.elitemonster;
   }

   public void setElitemonster(boolean elitemonster) {
      this.elitemonster = elitemonster;
   }

   public boolean isEliteboss() {
      return this.eliteboss;
   }

   public void setEliteboss(boolean eliteboss) {
      this.eliteboss = eliteboss;
   }

   public boolean isUserunespawn() {
      return this.userunespawn;
   }

   public void setUserunespawn(boolean userunespawn) {
      this.userunespawn = userunespawn;
   }

   public String getSpecialtxt() {
      return this.specialtxt;
   }

   public void setSpecialtxt(String specialtxt) {
      this.specialtxt = specialtxt;
   }

   public int getPatten() {
      return this.patten;
   }

   public void SetPatten(int a) {
      this.patten = a;
   }

   public void setDeadTime(int time) {
      Timer.MapTimer.getInstance().schedule(() -> {
         if (this != null) {
            this.getMap().killMonsterType(this, 0);
         }

      }, (long)time);
   }

   public void setDeadTimeKillmob(int time) {
      Timer.MapTimer.getInstance().schedule(() -> {
         if (this != null) {
            this.getMap().killMonster(this);
         }

      }, (long)time);
   }

   public int getEnergycount() {
      return this.energycount;
   }

   public void setEnergycount(int energycount) {
      this.energycount = energycount;
   }

   public boolean isEnergyleft() {
      return this.energyleft;
   }

   public void setEnergyleft(boolean energyleft) {
      this.energyleft = energyleft;
   }

   public int getEnergyspeed() {
      return this.energyspeed;
   }

   public void setEnergyspeed(int energyspeed) {
      this.energyspeed = energyspeed;
   }

   public Map<Integer, Rectangle> getRectangles() {
      return this.rectangles;
   }

   public void setRectangles(Map<Integer, Rectangle> rectangles) {
      this.rectangles = rectangles;
   }

   public int getStigmaType() {
      return this.StigmaType;
   }

   public void setStigmaType(int StigmaType) {
      this.StigmaType = StigmaType;
   }

   public int getTotalStigma() {
      return this.TotalStigma;
   }

   public void setTotalStigma(int TotalStigma) {
      this.TotalStigma = TotalStigma;
   }

   public void DemainChangePhase(MapleCharacter from) {
      if (!this.demianChangePhase) {
         this.map.removeAllFlyingSword();
         this.map.broadcastMessage(CField.enforceMSG("데미안이 완전한 어둠을 손에 넣었습니다.", 216, 30000000));
         this.map.broadcastMessage(MobPacket.ChangePhaseDemian(this, 79));
         this.demianChangePhase = true;
         if (from.getEventInstance() != null) {
            from.getEventInstance().monsterKilled(from, this);
         }

         Timer.MapTimer.getInstance().schedule(() -> {
            this.map.killMonsterType(this, 0);
         }, 6000L);
      }

   }

   public int getSerenNoonTotalTime() {
      return this.SerenNoonTotalTime;
   }

   public void setSerenNoonTotalTime(int SerenNoonTotalTime) {
      this.SerenNoonTotalTime = SerenNoonTotalTime;
   }

   public int getSerenSunSetTotalTime() {
      return this.SerenSunSetTotalTime;
   }

   public void setSerenSunSetTotalTime(int SerenSunSetTotalTime) {
      this.SerenSunSetTotalTime = SerenSunSetTotalTime;
   }

   public int getSerenMidNightSetTotalTime() {
      return this.SerenMidNightSetTotalTime;
   }

   public void setSerenMidNightSetTotalTime(int SerenMidNightSetTotalTime) {
      this.SerenMidNightSetTotalTime = SerenMidNightSetTotalTime;
   }

   public int getSerenDawnSetTotalTime() {
      return this.SerenDawnSetTotalTime;
   }

   public void setSerenDawnSetTotalTime(int SerenDawnSetTotalTime) {
      this.SerenDawnSetTotalTime = SerenDawnSetTotalTime;
   }

   public int getSerenNoonNowTime() {
      return this.SerenNoonNowTime;
   }

   public void setSerenNoonNowTime(int SerenNoonNowTime) {
      this.SerenNoonNowTime = SerenNoonNowTime;
   }

   public int getSerenSunSetNowTime() {
      return this.SerenSunSetNowTime;
   }

   public void setSerenSunSetNowTime(int SerenSunSetNowTime) {
      this.SerenSunSetNowTime = SerenSunSetNowTime;
   }

   public int getSerenMidNightSetNowTime() {
      return this.SerenMidNightSetNowTime;
   }

   public void setSerenMidNightSetNowTime(int SerenMidNightSetNowTime) {
      this.SerenMidNightSetNowTime = SerenMidNightSetNowTime;
   }

   public int getSerenDawnSetNowTime() {
      return this.SerenDawnSetNowTime;
   }

   public void setSerenDawnSetNowTime(int SerenDawnSetNowTime) {
      this.SerenDawnSetNowTime = SerenDawnSetNowTime;
   }

   public int getSerenTimetype() {
      return this.SerenTimetype;
   }

   public void setSerenTimetype(int SerenTimetype) {
      this.SerenTimetype = SerenTimetype;
   }

   public void ResetSerenTime(boolean show) {
      this.SerenTimetype = 1;
      this.SerenNoonNowTime = 110;
      this.SerenNoonTotalTime = 110;
      this.SerenSunSetNowTime = 110;
      this.SerenSunSetTotalTime = 110;
      this.SerenMidNightSetNowTime = 30;
      this.SerenMidNightSetTotalTime = 30;
      this.SerenDawnSetNowTime = 110;
      this.SerenDawnSetTotalTime = 110;
      if (show) {
         this.getMap().broadcastMessage(MobPacket.BossSeren.SerenTimer(0, 360000, this.SerenNoonTotalTime, this.SerenSunSetTotalTime, this.SerenMidNightSetTotalTime, this.SerenDawnSetTotalTime));
      }

   }

   public void AddSerenTotalTimeHandler(int type, int add, int turn) {
      this.getMap().broadcastMessage(MobPacket.BossSeren.SerenTimer(1, this.SerenNoonTotalTime, this.SerenSunSetTotalTime, this.SerenMidNightSetTotalTime, this.SerenDawnSetTotalTime, turn));
   }

   public void AddSerenTimeHandler(int type, int add) {
      int nowtime = false;
      label106:
      switch(type) {
      case 1:
         this.SerenNoonNowTime += add;
         break;
      case 2:
         this.SerenSunSetNowTime += add;
         Iterator var4 = this.getMap().getAllChracater().iterator();

         while(true) {
            if (!var4.hasNext()) {
               break label106;
            }

            MapleCharacter chr = (MapleCharacter)var4.next();
            if (chr.isAlive() && chr.getBuffedValue(SecondaryStat.NotDamaged) == null && chr.getBuffedValue(SecondaryStat.IndieNotDamaged) == null) {
               int minushp = (int)(-chr.getStat().getCurrentMaxHp() / 100L);
               chr.addHP((long)minushp);
               chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, minushp, 36, 0, 0, (byte)0, true, (Point)null, (String)null, (Item)null));
            }
         }
      case 3:
         this.SerenMidNightSetNowTime += add;
         break;
      case 4:
         this.SerenDawnSetNowTime += add;
      }

      int nowtime = type == 4 ? this.SerenDawnSetNowTime : (type == 3 ? this.SerenMidNightSetNowTime : (type == 2 ? this.SerenSunSetNowTime : this.SerenNoonNowTime));
      MapleMonster seren = null;
      int[] serens = new int[]{8880603, 8880607, 8880609, 8880612};
      int[] var13 = serens;
      int var7 = serens.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         int ids = var13[var8];
         seren = this.getMap().getMonsterById(ids);
         if (seren != null) {
            break;
         }
      }

      if (nowtime == 3) {
         Iterator var14 = this.getMap().getAllMonster().iterator();

         while(var14.hasNext()) {
            MapleMonster mob = (MapleMonster)var14.next();
            if (mob.getId() == seren.getId() + 1) {
               this.getMap().broadcastMessage(MobPacket.ChangePhaseDemian(mob, 79));
               this.getMap().killMonsterType(mob, 2);
            }
         }
      }

      if (nowtime <= 0 && seren != null) {
         Point pos = seren.getPosition();
         this.getMap().broadcastMessage(MobPacket.BossSeren.SerenTimer(2, 1));
         this.setCustomInfo(8880603, 1, 0);
         this.getMap().broadcastMessage(MobPacket.BossSeren.SerenChangePhase("Mob/" + seren.getId() + ".img/skill3", 0, seren));
         Iterator var17 = this.getMap().getAllMonster().iterator();

         while(true) {
            MapleMonster mob;
            do {
               if (!var17.hasNext()) {
                  ++this.SerenTimetype;
                  if (this.SerenTimetype > 4) {
                     this.SerenTimetype = 1;
                  }

                  this.getMap().broadcastMessage(CWvsContext.serverNotice(5, "", "시간이 흐르고 태양 또한 정해진 순환에 따라 변화합니다."));
                  switch(this.SerenTimetype) {
                  case 1:
                     this.addHp(this.shield, false);
                     this.shield = -1L;
                     this.shieldmax = -1L;
                     this.getMap().broadcastMessage(MobPacket.showBossHP(this));
                     this.getMap().broadcastMessage(MobPacket.mobBarrier(this));
                     this.getMap().broadcastMessage(CWvsContext.serverNotice(5, "", "정오가 시작됨과 동시에 남아있는 여명의 기운이 세렌을 회복시킵니다."));
                     this.SerenNoonNowTime = this.SerenNoonTotalTime;
                     break;
                  case 2:
                     this.getMap().broadcastMessage(CWvsContext.serverNotice(5, "", "황혼의 불타는 듯한 석양이 회복 효율을 낮추고 지속적으로 피해를 입힙니다."));
                     this.SerenSunSetNowTime = this.SerenSunSetTotalTime;
                     break;
                  case 3:
                     this.getMap().broadcastMessage(CWvsContext.serverNotice(5, "", "태양이 저물어 빛을 잃고 자정이 시작됩니다."));
                     this.SerenMidNightSetNowTime = this.SerenMidNightSetTotalTime;
                     break;
                  case 4:
                     this.getMap().broadcastMessage(CWvsContext.serverNotice(5, "", "태양이 서서히 떠올라 빛과 희망이 시작되는 여명이 다가옵니다."));
                     this.SerenDawnSetNowTime = this.SerenDawnSetTotalTime;
                  }

                  Timer.MapTimer.getInstance().schedule(() -> {
                     this.getMap().broadcastMessage(SLFCGPacket.ClearObstacles());
                     FieldSkillFactory.getInstance();
                     this.getMap().broadcastMessage(MobPacket.useFieldSkill(FieldSkillFactory.getFieldSkill(100024, 1)));
                  }, 500L);
                  Timer.MapTimer.getInstance().schedule(() -> {
                     int nextid = type == 4 ? 8880607 : (type == 3 ? 8880603 : (type == 2 ? 8880612 : 8880609));
                     this.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(nextid), pos);
                     this.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(nextid + 1), new Point(-49, 305));
                     if (nextid == 8880603) {
                        this.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880605), pos);
                        MapleMonster totalseren = this.getMap().getMonsterById(8880602);
                        if (totalseren != null) {
                           totalseren.gainShield(totalseren.getStats().getHp() * 15L / 100L, totalseren.getShield() <= 0L, 0);
                        }
                     }

                     this.getMap().broadcastMessage(MobPacket.BossSeren.SerenTimer(2, 0));
                     this.setCustomInfo(8880603, 0, 0);
                     this.getMap().broadcastMessage(MobPacket.BossSeren.SerenChangeBackground(this.SerenTimetype));
                  }, 3560L);
                  return;
               }

               mob = (MapleMonster)var17.next();
            } while(mob.getId() != seren.getId() && mob.getId() != 8880605 && mob.getId() != 8880606 && mob.getId() != 8880611);

            this.getMap().broadcastMessage(MobPacket.ChangePhaseDemian(mob, 79));
            this.getMap().killMonsterType(mob, 2);
         }
      }
   }

   public void gainShield(long energy, boolean first, int delayremove) {
      this.shield += energy;
      if (first) {
         this.shield = energy;
         this.shieldmax = energy;
         if (delayremove > 0) {
            Timer.EtcTimer.getInstance().schedule(() -> {
               this.shield = 0L;
               this.shieldmax = 0L;
               this.getMap().broadcastMessage(MobPacket.mobBarrier(this));
            }, (long)(delayremove * 1000));
         }
      }

      this.getMap().broadcastMessage(MobPacket.mobBarrier(this));
   }

   public long getShield() {
      return this.shield;
   }

   public void setShield(long shield) {
      this.shield = shield;
   }

   public long getShieldmax() {
      return this.shieldmax;
   }

   public void setShieldmax(long shieldmax) {
      this.shieldmax = shieldmax;
   }

   public int getShieldPercent() {
      return (int)Math.ceil((double)this.shield * 100.0D / (double)this.shieldmax);
   }

   public boolean isWillSpecialPattern() {
      return this.willSpecialPattern;
   }

   public void setWillSpecialPattern(boolean willSpecialPattern) {
      this.willSpecialPattern = willSpecialPattern;
   }

   public long getElitehp() {
      return this.elitehp;
   }

   public void setElitehp(long elitehp) {
      this.elitehp = elitehp;
   }

   public boolean isElitechmp() {
      return this.elitechmp;
   }

   public void setElitechmp(boolean elitechmp) {
      this.elitechmp = elitechmp;
   }

   public long getLastStoneTime() {
      return this.lastStoneTime;
   }

   public void setLastStoneTime(long lastStoneTime) {
      this.lastStoneTime = lastStoneTime;
   }

   private interface AttackerEntry {
      List<MapleMonster.AttackingMapleCharacter> getAttackers();

      void addDamage(MapleCharacter var1, long var2, boolean var4);

      long getDamage();

      boolean contains(MapleCharacter var1);

      void killedMob(MapleMap var1, long var2, boolean var4, int var5);
   }

   private class PartyAttackerEntry implements MapleMonster.AttackerEntry {
      private long totDamage = 0L;
      private final Map<Integer, MapleMonster.OnePartyAttacker> attackers = new HashMap(6);
      private int partyid;

      public PartyAttackerEntry(int partyid) {
         this.partyid = partyid;
      }

      public List<MapleMonster.AttackingMapleCharacter> getAttackers() {
         List<MapleMonster.AttackingMapleCharacter> ret = new ArrayList(this.attackers.size());
         Iterator var2 = this.attackers.entrySet().iterator();

         while(var2.hasNext()) {
            Entry<Integer, MapleMonster.OnePartyAttacker> entry = (Entry)var2.next();
            MapleCharacter chr = MapleMonster.this.map.getCharacterById((Integer)entry.getKey());
            if (chr != null) {
               ret.add(new MapleMonster.AttackingMapleCharacter(chr, ((MapleMonster.OnePartyAttacker)entry.getValue()).lastAttackTime));
            }
         }

         return ret;
      }

      private final Map<MapleCharacter, MapleMonster.OnePartyAttacker> resolveAttackers() {
         Map<MapleCharacter, MapleMonster.OnePartyAttacker> ret = new HashMap(this.attackers.size());
         Iterator var2 = this.attackers.entrySet().iterator();

         while(var2.hasNext()) {
            Entry<Integer, MapleMonster.OnePartyAttacker> aentry = (Entry)var2.next();
            MapleCharacter chr = MapleMonster.this.map.getCharacterById((Integer)aentry.getKey());
            if (chr != null) {
               ret.put(chr, (MapleMonster.OnePartyAttacker)aentry.getValue());
            }
         }

         return ret;
      }

      public final boolean contains(MapleCharacter chr) {
         return this.attackers.containsKey(chr.getId());
      }

      public final long getDamage() {
         return this.totDamage;
      }

      public void addDamage(MapleCharacter from, long damage, boolean updateAttackTime) {
         MapleMonster.OnePartyAttacker oldPartyAttacker = (MapleMonster.OnePartyAttacker)this.attackers.get(from.getId());
         if (oldPartyAttacker != null) {
            oldPartyAttacker.damage += damage;
            oldPartyAttacker.lastKnownParty = from.getParty();
            if (updateAttackTime) {
               oldPartyAttacker.lastAttackTime = System.currentTimeMillis();
            }
         } else {
            MapleMonster.OnePartyAttacker onePartyAttacker = new MapleMonster.OnePartyAttacker(from.getParty(), damage);
            this.attackers.put(from.getId(), onePartyAttacker);
            if (!updateAttackTime) {
               onePartyAttacker.lastAttackTime = 0L;
            }
         }

         this.totDamage += damage;
      }

      public final void killedMob(MapleMap map, long baseExp, boolean mostDamage, int lastSkill) {
         MapleCharacter highest = null;
         long highestDamage = 0L;
         long iexp = 0L;
         Map<MapleCharacter, MapleMonster.ExpMap> expMap = new HashMap(6);
         Iterator var12 = this.resolveAttackers().entrySet().iterator();

         Entry attacker;
         label108:
         while(var12.hasNext()) {
            attacker = (Entry)var12.next();
            MapleParty party = ((MapleMonster.OnePartyAttacker)attacker.getValue()).lastKnownParty;
            double addedPartyLevel = 0.0D;
            byte added_partyinc = 0;
            byte Class_Bonus_EXP = 0;
            byte Premium_Bonus_EXP = 0;
            List<MapleCharacter> expApplicable = new ArrayList();
            Iterator var21 = party.getMembers().iterator();

            while(true) {
               MapleCharacter pchr;
               do {
                  do {
                     MaplePartyCharacter partychar;
                     do {
                        if (!var21.hasNext()) {
                           long iDamage = ((MapleMonster.OnePartyAttacker)attacker.getValue()).damage;
                           if (iDamage > highestDamage) {
                              highest = (MapleCharacter)attacker.getKey();
                              highestDamage = iDamage;
                           }

                           double innerBaseExp = (double)(baseExp * iDamage / this.totDamage);
                           if (expApplicable.size() <= 1) {
                              Class_Bonus_EXP = 0;
                           }

                           Iterator var33 = expApplicable.iterator();

                           while(var33.hasNext()) {
                              MapleCharacter expReceiver = (MapleCharacter)var33.next();
                              iexp = expMap.get(expReceiver) == null ? 0L : ((MapleMonster.ExpMap)expMap.get(expReceiver)).exp;
                              double levelMod = (double)expReceiver.getLevel() / addedPartyLevel * 0.4D;
                              iexp += (long)((int)Math.round(((((MapleCharacter)attacker.getKey()).getId() == expReceiver.getId() ? 0.6D : 0.0D) + levelMod) * innerBaseExp));
                              expMap.put(expReceiver, new MapleMonster.ExpMap(iexp, (byte)(expApplicable.size() + added_partyinc), Class_Bonus_EXP, Premium_Bonus_EXP));
                           }
                           continue label108;
                        }

                        partychar = (MaplePartyCharacter)var21.next();
                     } while(((MapleCharacter)attacker.getKey()).getLevel() - partychar.getLevel() > 5 && MapleMonster.this.stats.getLevel() - partychar.getLevel() > 5);

                     pchr = map.getCharacterById(partychar.getId());
                  } while(pchr == null);
               } while(!pchr.isAlive());

               boolean enable = true;
               int[] linkMobs = new int[]{9010152, 9010153, 9010154, 9010155, 9010156, 9010157, 9010158, 9010159, 9010160, 9010161, 9010162, 9010163, 9010164, 9010165, 9010166, 9010167, 9010168, 9010169, 9010170, 9010171, 9010172, 9010173, 9010174, 9010175, 9010176, 9010177, 9010178, 9010179, 9010180, 9010181};
               int[] var26 = linkMobs;
               int var27 = linkMobs.length;

               for(int var28 = 0; var28 < var27; ++var28) {
                  int linkMob = var26[var28];
                  if (MapleMonster.this.getId() == linkMob && pchr.getId() != ((MapleCharacter)attacker.getKey()).getId()) {
                     enable = false;
                  }
               }

               if (enable) {
                  expApplicable.add(pchr);
                  addedPartyLevel += (double)pchr.getLevel();
                  if (pchr.getStat().equippedWelcomeBackRing && Premium_Bonus_EXP == 0) {
                     Premium_Bonus_EXP = 80;
                  }

                  if (pchr.getStat().hasPartyBonus && added_partyinc < 4 && map.getPartyBonusRate() <= 0) {
                     ++added_partyinc;
                  }
               }
            }
         }

         var12 = expMap.entrySet().iterator();

         while(var12.hasNext()) {
            attacker = (Entry)var12.next();
            MapleMonster.ExpMap expmap = (MapleMonster.ExpMap)attacker.getValue();
            MapleMonster.this.giveExpToCharacter((MapleCharacter)attacker.getKey(), expmap.exp, mostDamage ? attacker.getKey() == highest : false, expMap.size(), expmap.ptysize, expmap.Class_Bonus_EXP, expmap.Premium_Bonus_EXP, lastSkill);
         }

      }

      public final int hashCode() {
         int prime = true;
         int resultx = 1;
         int result = 31 * resultx + this.partyid;
         return result;
      }

      public final boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (obj == null) {
            return false;
         } else if (this.getClass() != obj.getClass()) {
            return false;
         } else {
            MapleMonster.PartyAttackerEntry other = (MapleMonster.PartyAttackerEntry)obj;
            return this.partyid == other.partyid;
         }
      }
   }

   private final class SingleAttackerEntry implements MapleMonster.AttackerEntry {
      private long damage = 0L;
      private int chrid;
      private long lastAttackTime;

      public SingleAttackerEntry(MapleCharacter from) {
         this.chrid = from.getId();
      }

      public void addDamage(MapleCharacter from, long damage, boolean updateAttackTime) {
         if (this.chrid == from.getId()) {
            this.damage += damage;
            if (updateAttackTime) {
               this.lastAttackTime = System.currentTimeMillis();
            }
         }

      }

      public final List<MapleMonster.AttackingMapleCharacter> getAttackers() {
         MapleCharacter chr = MapleMonster.this.map.getCharacterById(this.chrid);
         return chr != null ? Collections.singletonList(new MapleMonster.AttackingMapleCharacter(chr, this.lastAttackTime)) : Collections.emptyList();
      }

      public boolean contains(MapleCharacter chr) {
         return this.chrid == chr.getId();
      }

      public long getDamage() {
         return this.damage;
      }

      public void killedMob(MapleMap map, long baseExp, boolean mostDamage, int lastSkill) {
         MapleCharacter chr = map.getCharacterById(this.chrid);
         if (chr != null && chr.isAlive()) {
            MapleMonster.this.giveExpToCharacter(chr, baseExp, mostDamage, 1, (byte)0, (byte)0, (byte)0, lastSkill);
         }

      }

      public int hashCode() {
         return this.chrid;
      }

      public final boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (obj == null) {
            return false;
         } else if (this.getClass() != obj.getClass()) {
            return false;
         } else {
            MapleMonster.SingleAttackerEntry other = (MapleMonster.SingleAttackerEntry)obj;
            return this.chrid == other.chrid;
         }
      }
   }

   private static class AttackingMapleCharacter {
      private MapleCharacter attacker;
      private long lastAttackTime;

      public AttackingMapleCharacter(MapleCharacter attacker, long lastAttackTime) {
         this.attacker = attacker;
         this.lastAttackTime = lastAttackTime;
      }

      public final long getLastAttackTime() {
         return this.lastAttackTime;
      }

      public final void setLastAttackTime(long lastAttackTime) {
         this.lastAttackTime = lastAttackTime;
      }

      public final MapleCharacter getAttacker() {
         return this.attacker;
      }
   }

   public static class CancelStatusAction implements Runnable {
      private final WeakReference<MapleMonster> target;
      private final MonsterStatus status;
      private final MonsterStatusEffect effect;

      public CancelStatusAction(MapleMonster target, MonsterStatus status, MonsterStatusEffect effect) {
         this.target = new WeakReference(target);
         this.status = status;
         this.effect = effect;
      }

      public void run() {
         MapleMonster realTarget = (MapleMonster)this.target.get();
         if (realTarget != null && realTarget.isAlive()) {
            realTarget.cancelStatus(this.status, this.effect, true);
         }

      }
   }

   private static final class OnePartyAttacker {
      public MapleParty lastKnownParty;
      public long damage;
      public long lastAttackTime;

      public OnePartyAttacker(MapleParty lastKnownParty, long damage) {
         this.lastKnownParty = lastKnownParty;
         this.damage = damage;
         this.lastAttackTime = System.currentTimeMillis();
      }
   }

   private static final class ExpMap {
      public final long exp;
      public final byte ptysize;
      public final byte Class_Bonus_EXP;
      public final byte Premium_Bonus_EXP;

      public ExpMap(long exp, byte ptysize, byte Class_Bonus_EXP, byte Premium_Bonus_EXP) {
         this.exp = exp;
         this.ptysize = ptysize;
         this.Class_Bonus_EXP = Class_Bonus_EXP;
         this.Premium_Bonus_EXP = Premium_Bonus_EXP;
      }
   }
}
