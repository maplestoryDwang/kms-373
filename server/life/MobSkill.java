package server.life;

import client.MapleCharacter;
import client.SecondaryStat;
import client.inventory.Item;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import handling.channel.ChannelServer;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import server.Obstacle;
import server.Randomizer;
import server.Timer;
import server.field.boss.demian.MapleFlyingSword;
import server.field.boss.lotus.MapleEnergySphere;
import server.field.boss.lucid.Butterfly;
import server.field.boss.lucid.FairyDust;
import server.field.boss.lucid.FieldLucid;
import server.field.boss.will.SpiderWeb;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleMist;
import server.maps.MapleNodes;
import server.maps.MapleReactor;
import tools.Pair;
import tools.Triple;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.MobPacket;
import tools.packet.SLFCGPacket;

public class MobSkill {
   private int skillId;
   private int skillLevel;
   private int mpCon;
   private int spawnEffect;
   private int hp;
   private int x;
   private int y;
   private int action;
   private long duration;
   private long interval;
   private long skillForbid;
   private float prop;
   private short limit;
   private List<Integer> toSummon = new ArrayList();
   private Point lt;
   private Point rb;
   private boolean summonOnce;
   private boolean onlyFsm;
   private boolean onlyOtherSkill;
   private boolean isMobGroup;
   private int skillAfter;
   private int otherSkillID;
   private int otherSkillLev;
   private int afterAttack;
   private int afterAttackCount;
   private int afterDead;
   private int force;

   public MobSkill(int skillId, int level) {
      this.skillId = skillId;
      this.skillLevel = level;
   }

   public void setOnce(boolean o) {
      this.summonOnce = o;
   }

   public boolean onlyOnce() {
      return this.summonOnce;
   }

   public void setMpCon(int mpCon) {
      this.mpCon = mpCon;
   }

   public void addSummons(List<Integer> toSummon) {
      this.toSummon = toSummon;
   }

   public void setSpawnEffect(int spawnEffect) {
      this.spawnEffect = spawnEffect;
   }

   public void setHp(int hp) {
      this.hp = hp;
   }

   public void setX(int x) {
      this.x = x;
   }

   public void setY(int y) {
      this.y = y;
   }

   public void setProp(float prop) {
      this.prop = prop;
   }

   public void setLtRb(Point lt, Point rb) {
      this.lt = lt;
      this.rb = rb;
   }

   public void setLimit(short limit) {
      this.limit = limit;
   }

   public boolean checkDealyBuff(MapleCharacter player, MapleMonster monster) {
      boolean use = true;
      switch(this.skillId) {
      case 133:
         if (this.skillLevel == 18 && monster.getId() == 8920103) {
            use = false;
         }
         break;
      case 183:
         if (this.skillLevel == 2 && monster.getId() == 8920102) {
            use = false;
         }
         break;
      case 186:
         if (this.skillLevel == 2 && monster.getId() == 8920101) {
            use = false;
         }
         break;
      case 201:
         if (this.skillLevel == 60 && monster.getId() == 8920100) {
            use = true;
         }
         break;
      case 252:
         if (monster.getId() == 8860000) {
            use = false;
         }
      }

      if (monster.getCustomValue0(1234567) == 1L) {
         use = false;
      }

      return use;
   }

   public boolean checkCurrentBuff(MapleCharacter player, MapleMonster monster) {
      boolean stop;
      stop = false;
      Iterator var5;
      Integer mobId;
      label200:
      switch(this.skillId) {
      case 100:
      case 110:
      case 150:
         stop = monster.isBuffed(MonsterStatus.MS_Pad);
         break;
      case 101:
      case 111:
      case 151:
         stop = monster.isBuffed(MonsterStatus.MS_Mad);
         break;
      case 102:
      case 112:
      case 152:
         stop = monster.isBuffed(MonsterStatus.MS_Pdr);
         break;
      case 103:
      case 113:
      case 153:
         stop = monster.isBuffed(MonsterStatus.MS_Mdr);
      case 104:
      case 105:
      case 106:
      case 107:
      case 108:
      case 109:
      case 114:
      case 115:
      case 116:
      case 117:
      case 118:
      case 119:
      case 120:
      case 121:
      case 122:
      case 123:
      case 124:
      case 125:
      case 126:
      case 127:
      case 128:
      case 130:
      case 131:
      case 132:
      case 134:
      case 135:
      case 136:
      case 137:
      case 138:
      case 139:
      case 146:
      case 147:
      case 148:
      case 149:
      case 154:
      case 155:
      case 156:
      case 157:
      case 158:
      case 159:
      case 160:
      case 161:
      case 162:
      case 163:
      case 164:
      case 165:
      case 166:
      case 167:
      case 168:
      case 169:
      case 171:
      case 172:
      case 173:
      case 174:
      case 175:
      case 177:
      case 178:
      case 179:
      case 180:
      case 181:
      case 182:
      case 185:
      case 187:
      case 188:
      case 189:
      case 190:
      case 192:
      case 193:
      case 194:
      case 195:
      case 196:
      case 197:
      case 198:
      case 199:
      case 202:
      case 204:
      case 205:
      case 206:
      case 207:
      case 208:
      case 209:
      case 210:
      case 211:
      case 212:
      case 213:
      case 215:
      case 216:
      case 217:
      case 218:
      case 219:
      case 220:
      case 221:
      case 222:
      case 223:
      case 224:
      case 225:
      case 226:
      case 227:
      case 228:
      case 229:
      case 230:
      case 231:
      case 232:
      case 233:
      case 234:
      case 235:
      case 236:
      case 237:
      case 239:
      case 240:
      case 243:
      case 244:
      case 245:
      case 246:
      case 247:
      case 248:
      case 249:
      case 250:
      case 251:
      default:
         break;
      case 129:
         if (monster.getHPPercent() > 50) {
            stop = true;
         }
         break;
      case 133:
         if (this.skillLevel == 18 && monster.getId() == 8920103) {
            stop = true;
         }
         break;
      case 140:
      case 141:
      case 142:
      case 143:
      case 144:
      case 145:
         stop = monster.isBuffed(MonsterStatus.MS_Hardskin) || monster.isBuffed(MonsterStatus.MS_PowerImmune) || monster.isBuffed(MonsterStatus.MS_MImmune) || monster.isBuffed(MonsterStatus.MS_PImmune);
         break;
      case 170:
         if ((monster.getId() == 8910000 || monster.getId() == 8910100) && this.skillLevel == 11) {
            int hp = monster.getId() == 8910100 ? 30 : 10;
            if (monster.getHPPercent() > hp) {
               stop = true;
            } else {
               monster.setNextSkill(170);
               monster.setNextSkillLvl(11);
            }
         }

         if (this.skillLevel == 49 || this.skillLevel == 51) {
            stop = true;
         }
         break;
      case 176:
         if (this.skillLevel >= 1 && this.skillLevel <= 4) {
            stop = true;
            var5 = monster.getMap().getAllReactorsThreadsafe().iterator();

            while(var5.hasNext()) {
               MapleMapObject remo = (MapleMapObject)var5.next();
               MapleReactor react = (MapleReactor)remo;
               if (react.getReactorId() >= 2708001 && react.getReactorId() <= 2708004 && react.getState() == 0) {
                  stop = false;
                  break;
               }
            }
         }
         break;
      case 183:
         if (this.skillLevel == 2 && monster.getId() == 8920102) {
            stop = true;
         }
         break;
      case 184:
         if (monster.getId() == 8910000) {
            if (this.skillLevel == 1 && (monster.getHPPercent() < 10 || monster.getHPPercent() <= 100 && monster.getHPPercent() >= 70)) {
               stop = true;
            }
         } else if (monster.getId() == 8910100 && (monster.getHPPercent() < 10 || monster.getHPPercent() <= 100 && monster.getHPPercent() >= 70)) {
            stop = true;
         }
         break;
      case 186:
         if (this.skillLevel == 2 && monster.getId() == 8920101) {
            stop = true;
         }
         break;
      case 191:
         if (monster.getHPPercent() <= 40 && monster.getHPPercent() >= 0) {
            stop = true;
         }
         break;
      case 200:
         if (this.skillLevel == 251) {
            if (monster.getCustomValue(8870100) != null && monster.getCustomValue0(8870100) > 0L) {
               stop = true;
            }
            break;
         } else {
            if (this.limit < 0) {
               this.limit = 0;
            }

            var5 = this.getSummons().iterator();

            do {
               if (!var5.hasNext()) {
                  break label200;
               }

               mobId = (Integer)var5.next();
            } while(player.getMap().countMonsterById(mobId) <= this.limit);

            return true;
         }
      case 201:
         if (this.skillLevel == 60) {
            if (monster.getId() == 8920100) {
               stop = true;
            }
            break;
         } else {
            if (this.limit < 0) {
               this.limit = 0;
            }

            if (this.skillLevel == 199) {
               this.limit = 1;
            }

            int mobId2 = false;
            var5 = this.getSummons().iterator();

            do {
               if (!var5.hasNext()) {
                  break label200;
               }

               mobId = (Integer)var5.next();
               int mobId2 = mobId;
            } while(player.getMap().countMonsterById(mobId) <= this.limit);

            return true;
         }
      case 203:
         if (monster.getId() == 8910100) {
            stop = true;
         } else if (monster.getHPPercent() > 40 || monster.getHPPercent() < 10) {
            stop = true;
         }
         break;
      case 214:
         if (this.skillLevel == 14) {
            stop = true;
         }
         break;
      case 238:
         if (monster.getCustomValue(23807) != null) {
            stop = true;
            break;
         }
      case 241:
         if (this.skillLevel == 1 || this.skillLevel == 2) {
            stop = monster.isBuffed(MonsterStatus.MS_PopulatusTimer);
         }
         break;
      case 242:
         if (this.skillLevel == 5) {
            stop = true;
         }
         break;
      case 252:
         if (monster.getId() == 8860000) {
            stop = true;
         }
      }

      if (monster.getCustomValue0(1234567) == 1L) {
         stop = true;
      }

      return stop;
   }

   public void applyEffect(MapleCharacter player, MapleMonster monster, boolean skill, boolean isFacingLeft) {
      this.applyEffect(player, monster, skill, isFacingLeft, 0);
   }

   public void applyEffect(MapleCharacter player, final MapleMonster monster, boolean skill, boolean isFacingLeft, int RectCount) {
      try {
         Map<SecondaryStat, Pair<Integer, Integer>> diseases = new EnumMap(SecondaryStat.class);
         List<SecondaryStat> cancels = new ArrayList();
         List<Pair<MonsterStatus, MonsterStatusEffect>> stats = new ArrayList();
         boolean allchr = false;
         if (monster != null && player != null) {
            String var10002 = monster.getStats().getName();
            player.dropMessageGM(5, var10002 + "(" + monster.getId() + ")의 스킬 : " + this.skillId + " / " + this.skillLevel);
         }

         MapleCharacter mapleCharacter;
         Iterator var93;
         Iterator var96;
         MapleMapObject reactor1l;
         int type;
         int a;
         int i5;
         int i6;
         int percent;
         int ypos;
         int xdistance;
         ArrayList list2;
         MapleMonster toSpawn;
         MapleMist mapleMist;
         MapleMonster will2;
         MapleMonster will1;
         MapleCharacter mapleCharacter;
         int xpos;
         Iterator var106;
         Rectangle zone;
         List objects;
         label1871:
         switch(this.skillId) {
         case 100:
            stats.add(new Pair(MonsterStatus.MS_Pad, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            break;
         case 101:
            stats.add(new Pair(MonsterStatus.MS_Mad, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            break;
         case 102:
            stats.add(new Pair(MonsterStatus.MS_Pdr, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            break;
         case 103:
            stats.add(new Pair(MonsterStatus.MS_Mdr, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
         case 104:
         case 106:
         case 107:
         case 108:
         case 109:
         case 116:
         case 117:
         case 118:
         case 119:
         case 130:
         case 139:
         case 147:
         case 148:
         case 149:
         case 158:
         case 159:
         case 160:
         case 161:
         case 162:
         case 163:
         case 164:
         case 165:
         case 166:
         case 167:
         case 168:
         case 169:
         case 178:
         case 185:
         case 187:
         case 192:
         case 193:
         case 194:
         case 195:
         case 196:
         case 197:
         case 198:
         case 199:
         case 202:
         case 204:
         case 205:
         case 206:
         case 207:
         case 208:
         case 209:
         case 210:
         case 216:
         case 218:
         case 219:
         case 222:
         case 224:
         case 225:
         case 229:
         case 231:
         case 232:
         case 233:
         case 235:
         case 236:
         case 239:
         case 240:
         case 243:
         case 244:
         case 245:
         case 250:
         case 251:
         case 252:
         case 253:
         case 254:
         case 255:
         case 256:
         case 257:
         case 258:
         case 259:
         case 261:
         default:
            break;
         case 105:
            if (monster.getId() != 8840000 && monster.getId() != 8840007 && monster.getId() != 8840014) {
               if (this.lt != null && this.rb != null && skill && monster != null) {
                  objects = this.getObjectsInRange(monster, MapleMapObjectType.MONSTER);
                  var106 = objects.iterator();

                  MapleMapObject mons;
                  do {
                     if (!var106.hasNext()) {
                        break label1871;
                     }

                     mons = (MapleMapObject)var106.next();
                  } while(mons.getObjectId() == monster.getObjectId());

                  player.getMap().killMonster((MapleMonster)mons, player, true, false, (byte)1, 0);
                  monster.heal((long)this.getX(), (long)this.getY(), true);
               } else if (monster != null) {
                  monster.heal((long)this.getX(), (long)this.getY(), true);
               }
            } else {
               i6 = 0;
               var106 = player.getMap().getAllMonster().iterator();

               while(var106.hasNext()) {
                  toSpawn = (MapleMonster)var106.next();
                  if (toSpawn.getId() == 8840017) {
                     monster.getMap().killMonster(toSpawn, player, false, false, (byte)1);
                     ++i6;
                  }
               }

               monster.addHp((long)(this.getX() * i6), true);
            }
            break;
         case 110:
            stats.add(new Pair(MonsterStatus.MS_Pad, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            break;
         case 111:
            stats.add(new Pair(MonsterStatus.MS_Mad, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            break;
         case 112:
            stats.add(new Pair(MonsterStatus.MS_Pdr, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            break;
         case 113:
            stats.add(new Pair(MonsterStatus.MS_Mdr, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            break;
         case 114:
            if (this.lt != null && this.rb != null && skill && monster != null) {
               objects = this.getObjectsInRange(monster, MapleMapObjectType.MONSTER);
               xdistance = this.getX() / 1000 * (int)(950.0D + 3300.0D * Math.random());
               var96 = objects.iterator();

               while(true) {
                  if (!var96.hasNext()) {
                     break label1871;
                  }

                  MapleMapObject mons = (MapleMapObject)var96.next();
                  ((MapleMonster)mons).heal((long)xdistance, (long)this.getY(), true);
                  player.getMap().broadcastMessage(MobPacket.healEffectMonster(((MapleMonster)mons).getObjectId(), this.skillId, this.skillLevel));
               }
            } else {
               if (monster != null) {
                  monster.heal((long)this.getX(), (long)this.getY(), true);
                  player.getMap().broadcastMessage(MobPacket.healEffectMonster(monster.getObjectId(), this.skillId, this.skillLevel));
               }
               break;
            }
         case 115:
            stats.add(new Pair(MonsterStatus.MS_Speed, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            break;
         case 120:
            diseases.put(SecondaryStat.Seal, new Pair(this.x, (int)this.duration));
            if (this.skillLevel == 37) {
               diseases.put(SecondaryStat.KawoongDebuff, new Pair(20, (int)this.duration));
            }
            break;
         case 121:
            diseases.put(SecondaryStat.Darkness, new Pair(this.x, (int)this.duration));
            break;
         case 122:
            diseases.put(SecondaryStat.Weakness, new Pair(this.x, (int)this.duration));
            if (this.skillLevel == 20) {
               diseases.put(SecondaryStat.Slow, new Pair(60, (int)this.duration));
            }
            break;
         case 123:
            if (player.getMapId() != 921170004 && player.getMapId() != 921170011 && player.getMapId() != 100) {
               diseases.put(SecondaryStat.Stun, new Pair(1, (int)this.duration));
            }
            break;
         case 124:
            diseases.put(SecondaryStat.Curse, new Pair(this.x, (int)this.duration));
            break;
         case 125:
            diseases.put(SecondaryStat.Poison, new Pair(this.x, (int)this.duration));
            break;
         case 126:
            diseases.put(SecondaryStat.Slow, new Pair(this.x, (int)this.duration));
            if (this.skillLevel == 46) {
               allchr = true;
            }
            break;
         case 127:
            if (this.lt != null && this.rb != null && skill && monster != null && player != null) {
               var93 = this.getPlayersInRange(monster, player).iterator();

               while(true) {
                  if (!var93.hasNext()) {
                     break label1871;
                  }

                  mapleCharacter = (MapleCharacter)var93.next();
                  mapleCharacter.dispel();
               }
            } else {
               if (player != null) {
                  player.dispel();
               }
               break;
            }
         case 128:
            diseases.put(SecondaryStat.Attract, new Pair(this.x, (int)this.duration));
            break;
         case 129:
            if (monster != null && (monster.getEventInstance() == null || monster.getEventInstance().getName().indexOf("BossQuest") == -1)) {
               BanishInfo info = monster.getStats().getBanishInfo();
               if (info != null) {
                  if (this.lt != null && this.rb != null && skill && player != null) {
                     var93 = this.getPlayersInRange(monster, player).iterator();

                     while(var93.hasNext()) {
                        mapleCharacter = (MapleCharacter)var93.next();
                        mapleCharacter.changeMapBanish(info.getMap(), info.getPortal(), info.getMsg());
                        mapleCharacter.dropMessage(5, monster.getStats().getName() + "의 힘에 의해 다른장소로 쫓겨 납니다.");
                     }
                  } else if (player != null) {
                     player.changeMapBanish(info.getMap(), info.getPortal(), info.getMsg());
                  }
               }
            }
            break;
         case 131:
            MapleMist mist = null;
            if (monster.getId() / 1000 != 8950) {
               mist = new MapleMist(this.calculateBoundingBox(monster.getTruePosition(), true), monster, this, (int)this.duration);
            }

            if (mist != null && monster != null && monster.getMap() != null) {
               monster.getMap().spawnMist(mist, false);
            }
            break;
         case 132:
            diseases.put(SecondaryStat.ReverseInput, new Pair(1, (int)this.duration));
            break;
         case 133:
            diseases.put(SecondaryStat.Undead, new Pair(this.x, (int)this.duration));
            break;
         case 134:
            diseases.put(SecondaryStat.StopPortion, new Pair(this.x, (int)this.duration));
            break;
         case 135:
            diseases.put(SecondaryStat.StopMotion, new Pair(this.x, (int)this.duration));
            break;
         case 136:
            diseases.put(SecondaryStat.Fear, new Pair(this.x, (int)this.duration));
            break;
         case 137:
            diseases.put(SecondaryStat.Frozen, new Pair(this.x, (int)this.duration));
            break;
         case 138:
            diseases.put(SecondaryStat.DispelItemOption, new Pair(this.x, (int)this.duration));
            break;
         case 140:
            stats.add(new Pair(MonsterStatus.MS_PImmune, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            break;
         case 141:
            stats.add(new Pair(MonsterStatus.MS_MImmune, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            break;
         case 142:
            stats.add(new Pair(MonsterStatus.MS_Hardskin, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            break;
         case 143:
            stats.add(new Pair(MonsterStatus.MS_PImmune, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            stats.add(new Pair(MonsterStatus.MS_PCounter, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            break;
         case 144:
            stats.add(new Pair(MonsterStatus.MS_MImmune, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            stats.add(new Pair(MonsterStatus.MS_MCounter, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            break;
         case 145:
            System.out.println(this.x);
            stats.add(new Pair(MonsterStatus.MS_PImmune, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            stats.add(new Pair(MonsterStatus.MS_MImmune, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            stats.add(new Pair(MonsterStatus.MS_PCounter, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            stats.add(new Pair(MonsterStatus.MS_MCounter, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            if (monster.getId() == 8840000 || monster.getId() == 8840007 || monster.getId() == 8840014) {
               monster.getMap().broadcastMessage(MobPacket.getSmartNotice(monster.getId(), 1, 5, 0, "근 거리의 포위당한 반 레온이 반격을 시전합니다."));
            }
            break;
         case 146:
            if (monster.getId() != 8850010 && monster.getId() != 8850110) {
               if (monster.getId() != 8850010) {
                  stats.add(new Pair(MonsterStatus.MS_PowerImmune, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
               }
            } else {
               will2 = monster.getMap().getMonsterById(8850011);
               if (will2 == null) {
                  will2 = monster.getMap().getMonsterById(8850111);
               }

               if (will2 != null) {
                  stats.add(new Pair(MonsterStatus.MS_PowerImmune, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
                  will2.applyMonsterBuff(monster.getMap(), stats, this);
                  stats.clear();
               }
            }
            break;
         case 150:
            stats.add(new Pair(MonsterStatus.MS_Pad, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            break;
         case 151:
            stats.add(new Pair(MonsterStatus.MS_Mad, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            break;
         case 152:
            stats.add(new Pair(MonsterStatus.MS_Pdr, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            break;
         case 153:
            stats.add(new Pair(MonsterStatus.MS_Mdr, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            break;
         case 154:
            stats.add(new Pair(MonsterStatus.MS_Acc, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            break;
         case 155:
            stats.add(new Pair(MonsterStatus.MS_Eva, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            break;
         case 156:
            stats.add(new Pair(MonsterStatus.MS_Speed, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            break;
         case 157:
            stats.add(new Pair(MonsterStatus.MS_Seal, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            break;
         case 170:
            int x;
            switch(this.skillLevel) {
            case 1:
            case 3:
               if (monster.getId() != 8840000 && monster.getId() != 8840007 && monster.getId() != 8840014) {
                  monster.setPosition(player.getTruePosition());
                  player.getClient().getSession().writeAndFlush(MobPacket.TeleportMonster(monster, false, 2, monster.getTruePosition()));
               } else {
                  monster.getMap().broadcastMessage(MobPacket.getSmartNotice(monster.getId(), 1, 5, 0, monster.getStats().getName() + "이 자신에게 가장 큰 위협을 준 적에게 순간이동하여 다가갑니다."));
                  monster.setPosition(player.getTruePosition());
                  player.getClient().getSession().writeAndFlush(MobPacket.TeleportMonster(monster, false, 2, monster.getTruePosition()));
               }
               break;
            case 10:
               monster.setPosition(player.getTruePosition());
               monster.getMap().broadcastMessage(MobPacket.TeleportMonster(monster, false, 5, monster.getTruePosition()));
               break;
            case 13:
               List<Point> list = new ArrayList();
               list.add(new Point(810, 443));
               list.add(new Point(-2190, 443));
               list.add(new Point(-1690, 443));
               list.add(new Point(560, 443));
               list.add(new Point(-190, 443));
               list.add(new Point(-690, 443));
               list.add(new Point(-1940, 443));
               list.add(new Point(1310, 443));
               list.add(new Point(-1190, 443));
               list.add(new Point(1060, 443));
               list.add(new Point(-940, 443));
               list.add(new Point(-1440, 443));
               list.add(new Point(1560, 443));
               list.add(new Point(-440, 443));
               monster.getMap().broadcastMessage(MobPacket.dropStone("DropStone", list));
               if (isFacingLeft) {
                  monster.setPosition(new Point(965, 420));
                  monster.getMap().broadcastMessage(MobPacket.TeleportMonster(monster, false, 3, new Point(965, 420)));
               } else {
                  monster.setPosition(new Point(-1750, 420));
                  monster.getMap().broadcastMessage(MobPacket.TeleportMonster(monster, false, 3, new Point(-1750, 420)));
               }

               monster.getMap().broadcastMessage(CWvsContext.getTopMsg("벨룸이 깊은 숨을 들이쉽니다."));
               Timer.MobTimer.getInstance().schedule(() -> {
                  if (monster.isAlive()) {
                     monster.setSkillForbid(false);
                     monster.getMap().broadcastMessage(MobPacket.dropStone("DropStone", list));
                     monster.getMap().broadcastMessage(MobPacket.setAfterAttack(monster.getObjectId(), 9, 1, 21, isFacingLeft));
                  }

               }, this.getSkillForbid());
               Timer.MobTimer.getInstance().schedule(() -> {
                  if (monster.isAlive()) {
                     monster.getMap().broadcastMessage(MobPacket.dropStone("DropStone", list));
                  }

               }, this.getSkillForbid() + 10000L);
               break;
            case 42:
               x = isFacingLeft ? -900 : 900;
               if (monster.getMap().getFootholds().findBelow(new Point(monster.getPosition().x + x, monster.getPosition().y)) == null) {
                  x = 0;
               }

               monster.getMap().broadcastMessage(MobPacket.TeleportMonster(monster, false, 10, new Point(monster.getPosition().x + x, 17)));
               break;
            case 44:
               if (monster.getId() == 8880409) {
                  monster.getMap().broadcastMessage(MobPacket.TeleportMonster(monster, false, 12, new Point(Randomizer.rand(-700, 700), 16)));
               } else {
                  monster.getMap().broadcastMessage(MobPacket.TeleportMonster(monster, false, 12, new Point(Randomizer.rand(-40, 1400), 16)));
               }
               break;
            case 45:
               x = isFacingLeft ? -680 : 680;
               monster.getMap().broadcastMessage(MobPacket.TeleportMonster(monster, false, 12, new Point(monster.getPosition().x + x, 16)));
               break;
            case 46:
               x = isFacingLeft ? -482 : 482;
               monster.getMap().broadcastMessage(MobPacket.TeleportMonster(monster, false, 12, new Point(monster.getPosition().x + x, 16)));
               break;
            case 50:
               monster.getMap().broadcastMessage(MobPacket.TeleportMonster(monster, true, 1, new Point(monster.getPosition().x, 16)));
               monster.getMap().broadcastMessage(MobPacket.setAttackZakumArm(monster.getObjectId(), 7));
               monster.getMap().broadcastMessage(CField.enforceMSG("데미안이 타락한 세계수의 힘을 폭주시키기 전에 큰 피해를 입혀 저지해야 합니다.", 216, 9500000));
               monster.getMap().broadcastMessage(MobPacket.demianRunaway(monster, (byte)0, MobSkillFactory.getMobSkill(214, 14), 10000, false));
               monster.setCustomInfo(8880111, 1, 0);
               monster.setNextSkill(0);
               monster.setNextSkillLvl(0);
               monster.setSkillForbid(true);
               monster.setLastSkillUsed(MobSkillFactory.getMobSkill(214, 14), System.currentTimeMillis(), MobSkillFactory.getMobSkill(214, 14).getDuration());
               diseases.put(SecondaryStat.Lapidification, new Pair(16, 10000));
               allchr = true;
               Timer.MobTimer.getInstance().schedule(() -> {
                  if (monster.isAlive()) {
                     boolean suc = monster.getCustomValue0(8880111) != 2L;
                     monster.removeCustomInfo(8880111);
                     monster.removeCustomInfo(8880112);
                     if (suc) {
                        Iterator var3 = monster.getMap().getAllChracater().iterator();

                        while(var3.hasNext()) {
                           MapleCharacter chr = (MapleCharacter)var3.next();
                           chr.getPercentDamage(monster, this.skillId, this.skillLevel, 200, false);
                        }

                        Timer.MobTimer.getInstance().schedule(() -> {
                           if (monster.isAlive()) {
                              monster.getMap().broadcastMessage(MobPacket.setAttackZakumArm(monster.getObjectId(), 8));
                              monster.getMap().broadcastMessage(MobPacket.demianRunaway(monster, (byte)1, MobSkillFactory.getMobSkill(214, 14), 10000, true));
                              monster.setSkillForbid(false);
                           }

                        }, 500L);
                     } else {
                        monster.getMap().broadcastMessage(MobPacket.setAttackZakumArm(monster.getObjectId(), 8));
                        monster.setSkillForbid(false);
                     }
                  }

               }, 10000L);
            case 51:
               break;
            case 57:
               Point pos = new Point(monster.getPosition().x, monster.getPosition().y);
               pos.x = Randomizer.rand(-623, 623);
               if (monster.getId() != 8880343 && monster.getId() != 8880303) {
                  if (monster.getId() == 8880344 || monster.getId() == 8880304) {
                     pos.y = -2020;
                  }
               } else {
                  pos.y = 159;
               }

               monster.setLastSkillUsed(this, System.currentTimeMillis(), 10000L);
               monster.getMap().broadcastMessage(MobPacket.TeleportMonster(monster, false, 12, pos));
               break;
            case 58:
               monster.setPosition(player.getTruePosition());
               monster.getMap().broadcastMessage(MobPacket.TeleportMonster(monster, false, 2, monster.getTruePosition()));
               break;
            case 62:
               MapleCharacter rchar = monster.getController().getClient().getRandomCharacter();
               if (rchar != null) {
                  monster.getMap().broadcastMessage(MobPacket.TeleportMonster(monster, false, 2, rchar.getPosition()));
               }

               monster.setLastSkillUsed(this, System.currentTimeMillis(), 40000L);
               break;
            case 63:
               monster.setPosition(player.getTruePosition());
               player.getClient().getSession().writeAndFlush(MobPacket.TeleportMonster(monster, false, 2, monster.getTruePosition()));
               break;
            case 64:
               monster.setLastSkillUsed(this, System.currentTimeMillis(), 40000L);
               Point pos2 = null;
               MapleCharacter mapleCharacter1 = monster.getController().getClient().getRandomCharacter();
               if (mapleCharacter1 != null) {
                  if (monster.getCustomValue0(8880501) == 1L) {
                     i6 = 350;
                     if (monster.getId() == 8880500) {
                        i6 *= -1;
                     }

                     pos2 = new Point(i6, 85);
                     monster.removeCustomInfo(8880501);
                  }

                  monster.getMap().broadcastMessage(MobPacket.TeleportMonster(monster, false, 2, pos2 != null ? pos2 : mapleCharacter1.getPosition()));
               }
               break;
            case 77:
               monster.setPosition(monster.isFacingLeft() ? new Point(monster.getPosition().x - 400, monster.getPosition().y) : new Point(monster.getPosition().x + 400, monster.getPosition().y));
               player.getClient().getSession().writeAndFlush(MobPacket.TeleportMonster(monster, false, 16, monster.getTruePosition()));
               player.getMap().broadcastMessage(MobPacket.Monster_Attack(monster, false, 170, 77, 9));
               break;
            default:
               monster.setPosition(player.getTruePosition());
               player.getClient().getSession().writeAndFlush(MobPacket.TeleportMonster(monster, false, 2, monster.getTruePosition()));
            }
         case 171:
            diseases.put(SecondaryStat.TimeBomb, new Pair(this.x, (int)this.duration));
            break;
         case 172:
            diseases.put(SecondaryStat.Morph, new Pair(this.x, (int)this.duration));
            if (monster.getId() == 8850011 || monster.getId() == 8850111) {
               monster.getMap().broadcastMessage(MobPacket.getSmartNotice(monster.getId(), 0, 5, 1, "시그너스가 버프 스킬을 비웃으며 적을 리본돼지로 변이하려 합니다."));
            }
            break;
         case 173:
            diseases.put(SecondaryStat.DarkTornado, new Pair(this.x, (int)this.duration));
            break;
         case 174:
            if (this.skillLevel == 14) {
               this.x = 6;
            }

            diseases.put(SecondaryStat.Lapidification, new Pair(this.x, (int)this.duration));
            break;
         case 175:
            cancels.add(SecondaryStat.DeathMark);
            if (player.getDebuffValue(SecondaryStat.DeathMark) == 1) {
               diseases.put(SecondaryStat.DeathMark, new Pair(2, (int)this.duration));
            } else if (player.getDebuffValue(SecondaryStat.DeathMark) == 2) {
               diseases.put(SecondaryStat.DeathMark, new Pair(3, (int)this.duration));
            } else if (player.getDebuffValue(SecondaryStat.DeathMark) <= 2) {
               diseases.put(SecondaryStat.DeathMark, new Pair(1, (int)this.duration));
            }
            break;
         case 176:
            int value;
            int rId;
            value = 0;
            rId = 0;
            label1650:
            switch(this.skillLevel) {
            case 5:
               value = this.x;
               break;
            case 6:
               rId = 2708001;
               break;
            case 7:
               rId = 2708002;
               break;
            case 8:
               rId = 2708003;
               break;
            case 9:
               rId = 2708004;
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            default:
               break;
            case 25:
            case 26:
            case 33:
            case 34:
               type = monster.getId() % 10;
               int minx = type == 3 ? -85 : (type == 4 ? -215 : (type == 5 ? -340 : (type == 6 ? -465 : (type == 7 ? 60 : (type == 8 ? 200 : (type == 9 ? 350 : 500))))));
               var93 = player.getMap().getAllCharactersThreadsafe().iterator();

               while(true) {
                  do {
                     do {
                        do {
                           do {
                              if (!var93.hasNext()) {
                                 break label1650;
                              }

                              mapleCharacter = (MapleCharacter)var93.next();
                           } while(minx - 70 >= mapleCharacter.getPosition().x);
                        } while(minx + 70 <= mapleCharacter.getPosition().x);
                     } while(mapleCharacter.getPosition().y <= -15);
                  } while(!mapleCharacter.isAlive());

                  percent = monster.getId() >= 8800023 && monster.getId() <= 8800030 ? 50 : (monster.getId() >= 8800003 && monster.getId() <= 8800010 ? 90 : (monster.getId() >= 8800103 && monster.getId() <= 8800110 ? 100 : 0));
                  mapleCharacter.getPercentDamage(monster, this.skillId, this.skillLevel, percent, true);
               }
            case 27:
               type = monster.getId() % 10;
               var93 = player.getMap().getAllCharactersThreadsafe().iterator();

               label1648:
               while(true) {
                  do {
                     if (!var93.hasNext()) {
                        break label1648;
                     }

                     mapleCharacter = (MapleCharacter)var93.next();
                  } while(!mapleCharacter.isAlive());

                  boolean damage = false;
                  if (type != 3 && type != 7) {
                     if (type != 4 && type != 8) {
                        if ((type == 5 || type == 9) && (mapleCharacter.getFH() == 5 || mapleCharacter.getFH() == 4 || mapleCharacter.getFH() == 3 || mapleCharacter.getFH() == 6 || mapleCharacter.getFH() == 7 || mapleCharacter.getFH() == 8)) {
                           damage = true;
                        }
                     } else if (mapleCharacter.getFH() == 14 || mapleCharacter.getFH() == 13 || mapleCharacter.getFH() == 12 || mapleCharacter.getFH() == 11 || mapleCharacter.getFH() == 10 || mapleCharacter.getFH() == 9) {
                        damage = true;
                     }
                  } else if (mapleCharacter.getFH() == 20 || mapleCharacter.getFH() == 19 || mapleCharacter.getFH() == 18 || mapleCharacter.getFH() == 15 || mapleCharacter.getFH() == 16 || mapleCharacter.getFH() == 17) {
                     damage = true;
                  }

                  if (damage) {
                     mapleCharacter.getPercentDamage(monster, this.skillId, this.skillLevel, 100, true);
                  }
               }
            }

            MapleReactor reactor2l;
            if (rId != 0 && monster != null && player != null) {
               var93 = monster.getMap().getAllReactorsThreadsafe().iterator();

               while(var93.hasNext()) {
                  reactor1l = (MapleMapObject)var93.next();
                  reactor2l = (MapleReactor)reactor1l;
                  if (reactor2l.getReactorId() == rId) {
                     reactor2l.forceHitReactor((byte)1, player.getId());
                     break;
                  }
               }
            }

            if ((monster.getId() == 8860000 || monster.getId() == 8860005) && this.skillLevel >= 1 && this.skillLevel <= 4) {
               value = this.x;
               var93 = player.getMap().getAllReactorsThreadsafe().iterator();

               while(var93.hasNext()) {
                  reactor1l = (MapleMapObject)var93.next();
                  reactor2l = (MapleReactor)reactor1l;
                  if (reactor2l.getReactorId() == 2708001 && reactor2l.getState() == 0) {
                     reactor2l.forceHitReactor((byte)1, player.getId());
                     break;
                  }

                  if (reactor2l.getReactorId() == 2708002 && reactor2l.getState() == 0) {
                     reactor2l.forceHitReactor((byte)1, player.getId());
                     break;
                  }

                  if (reactor2l.getReactorId() == 2708003 && reactor2l.getState() == 0) {
                     reactor2l.forceHitReactor((byte)1, player.getId());
                     break;
                  }

                  if (reactor2l.getReactorId() == 2708004 && reactor2l.getState() == 0) {
                     reactor2l.forceHitReactor((byte)1, player.getId());
                     break;
                  }
               }
            }

            if (value > 0 && this.lt != null && this.rb != null && monster != null && player != null) {
               zone = this.calculateBoundingBox(new Point(0, 0), false);
               var106 = player.getMap().getAllCharactersThreadsafe().iterator();

               while(var106.hasNext()) {
                  mapleCharacter = (MapleCharacter)var106.next();
                  if (zone.contains(mapleCharacter.getTruePosition())) {
                     if (mapleCharacter.isAlive()) {
                        if (mapleCharacter.getBuffedEffect(SecondaryStat.HolyMagicShell) != null) {
                           if (mapleCharacter.getHolyMagicShell() > 1) {
                              mapleCharacter.setHolyMagicShell((byte)(mapleCharacter.getHolyMagicShell() - 1));
                              Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
                              statups.put(SecondaryStat.HolyMagicShell, new Pair(Integer.valueOf(mapleCharacter.getHolyMagicShell()), (int)mapleCharacter.getBuffLimit(mapleCharacter.getBuffSource(SecondaryStat.HolyMagicShell))));
                              mapleCharacter.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, mapleCharacter.getBuffedEffect(SecondaryStat.HolyMagicShell), mapleCharacter));
                              mapleCharacter.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(mapleCharacter, 1, 0, 36, 0, 0, (byte)0, true, (Point)null, (String)null, (Item)null));
                              mapleCharacter.getMap().broadcastMessage(mapleCharacter, CField.EffectPacket.showEffect(mapleCharacter, 1, 0, 36, 0, 0, (byte)0, false, (Point)null, (String)null, (Item)null), false);
                           } else {
                              mapleCharacter.cancelEffectFromBuffStat(SecondaryStat.HolyMagicShell);
                           }
                        } else if (mapleCharacter.getBuffedValue(4341052)) {
                           mapleCharacter.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(mapleCharacter, 1, 0, 36, 0, 0, (byte)0, true, (Point)null, (String)null, (Item)null));
                           mapleCharacter.getMap().broadcastMessage(mapleCharacter, CField.EffectPacket.showEffect(mapleCharacter, 1, 0, 36, 0, 0, (byte)0, false, (Point)null, (String)null, (Item)null), false);
                        } else if (mapleCharacter.getBuffedEffect(SecondaryStat.WindWall) != null) {
                           ypos = Math.max(0, mapleCharacter.getBuffedValue(SecondaryStat.WindWall) - 100 * mapleCharacter.getBuffedEffect(SecondaryStat.WindWall).getZ());
                           if (ypos > 1) {
                              mapleCharacter.setBuffedValue(SecondaryStat.WindWall, ypos);
                              Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
                              statups.put(SecondaryStat.WindWall, new Pair(ypos, (int)mapleCharacter.getBuffLimit(400031030)));
                              mapleCharacter.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, mapleCharacter.getBuffedEffect(SecondaryStat.WindWall), mapleCharacter));
                           } else {
                              mapleCharacter.cancelEffectFromBuffStat(SecondaryStat.WindWall);
                           }
                        } else if (mapleCharacter.getBuffedEffect(SecondaryStat.TrueSniping) == null && mapleCharacter.getBuffedEffect(SecondaryStat.Etherealform) == null && mapleCharacter.getBuffedEffect(SecondaryStat.IndieNotDamaged) == null && mapleCharacter.getBuffedEffect(SecondaryStat.NotDamaged) == null) {
                           ypos = 0;
                           if (mapleCharacter.getBuffedEffect(SecondaryStat.IndieDamageReduce) != null) {
                              ypos = mapleCharacter.getBuffedValue(SecondaryStat.IndieDamageReduce);
                           } else if (mapleCharacter.getBuffedEffect(SecondaryStat.IndieDamReduceR) != null) {
                              ypos = -mapleCharacter.getBuffedValue(SecondaryStat.IndieDamReduceR);
                           }

                           System.out.println(value);
                           if (ypos > 0) {
                              value -= value * ypos / 100;
                           }

                           xpos = -value;
                           mapleCharacter.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(mapleCharacter, this.skillId, this.skillLevel, 45, 0, 0, (byte)0, true, (Point)null, (String)null, (Item)null));
                           mapleCharacter.getMap().broadcastMessage(mapleCharacter, CField.EffectPacket.showEffect(mapleCharacter, this.skillId, this.skillLevel, 45, 0, 0, (byte)0, false, (Point)null, (String)null, (Item)null), false);
                           mapleCharacter.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(mapleCharacter, 0, xpos, 36, 0, 0, (byte)0, true, (Point)null, (String)null, (Item)null));
                           mapleCharacter.getMap().broadcastMessage(mapleCharacter, CField.EffectPacket.showEffect(mapleCharacter, 0, xpos, 36, 0, 0, (byte)0, false, (Point)null, (String)null, (Item)null), false);
                           mapleCharacter.addHP((long)xpos);
                        }
                     }

                     mapleCharacter.getClient().getSession().writeAndFlush(CField.screenAttack(monster.getId(), this.skillId, this.skillLevel, (long)value));
                  }
               }
            }
            break;
         case 177:
            diseases.put(SecondaryStat.VenomSnake, new Pair(this.x, (int)this.duration));
            break;
         case 179:
            if (monster.getId() == 8870000) {
               player.getMap().broadcastMessage(MobPacket.SpeakingMonster(monster, 3, 0));
            }

            this.setDuration(10000L);
            diseases.put(SecondaryStat.PainMark, new Pair(this.skillLevel, (int)this.duration));
            break;
         case 180:
            diseases.put(SecondaryStat.VampDeath, new Pair((int)this.duration, (int)this.duration));
            diseases.put(SecondaryStat.VampDeathSummon, new Pair((int)this.duration, (int)this.duration));
            break;
         case 181:
            allchr = true;
            SecondaryStat m = SecondaryStat.Magnet;
            m.setX(monster.getPosition().x);
            diseases.put(m, new Pair(this.x, (int)this.duration));
            m = SecondaryStat.MagnetArea;
            m.setX(monster.getPosition().x);
            diseases.put(m, new Pair(this.x, (int)this.duration));
            break;
         case 182:
            diseases.put(SecondaryStat.GiveMeHeal, new Pair(this.x, (int)this.duration));
            break;
         case 183:
            cancels.add(SecondaryStat.FireBomb);
            if (player.getDebuffValue(SecondaryStat.FireBomb) == 1) {
               diseases.put(SecondaryStat.FireBomb, new Pair(2, (int)this.duration));
            } else if (player.getDebuffValue(SecondaryStat.FireBomb) == 2) {
               diseases.put(SecondaryStat.FireBomb, new Pair(3, (int)this.duration));
            } else if (player.getDebuffValue(SecondaryStat.FireBomb) <= 2) {
               diseases.put(SecondaryStat.FireBomb, new Pair(1, (int)this.duration));
            }
            break;
         case 184:
            diseases.put(SecondaryStat.ReturnTeleport, new Pair(1, (int)this.duration));
            break;
         case 186:
            if (this.skillLevel != 1 && this.skillLevel != 4) {
               MapleMist mapleMist;
               if (this.skillLevel == 2) {
                  mapleMist = new MapleMist(this.calculateBoundingBox(monster.getPosition(), monster.isFacingLeft()), monster, this, (int)this.getDuration());
                  mapleMist.setPosition(monster.getPosition());
                  if (mapleMist != null) {
                     monster.getMap().spawnMist(mapleMist, false);
                  }
               } else {
                  Point point;
                  if (this.skillLevel == 3) {
                     mapleMist = new MapleMist(new Rectangle(-1870, 133, 1150, 463), monster, this, (int)this.getDuration());
                     point = isFacingLeft ? new Point(965, 443) : new Point(-1750, 443);
                     mapleMist.setPosition(point);
                     mapleMist.setCustomx(isFacingLeft ? 645 : -1375);
                     mapleMist.setOwnerId(monster.getObjectId());
                     monster.getMap().spawnMist(mapleMist, false);
                  } else if (this.skillLevel == 5) {
                     list2 = new ArrayList();
                     list2.add(new Point(810, 443));
                     list2.add(new Point(-2190, 443));
                     list2.add(new Point(-1690, 443));
                     list2.add(new Point(560, 443));
                     list2.add(new Point(-190, 443));
                     list2.add(new Point(-690, 443));
                     list2.add(new Point(-1940, 443));
                     list2.add(new Point(1310, 443));
                     list2.add(new Point(-1190, 443));
                     list2.add(new Point(1060, 443));
                     list2.add(new Point(-940, 443));
                     list2.add(new Point(-1440, 443));
                     list2.add(new Point(1560, 443));
                     list2.add(new Point(-440, 443));
                     monster.getMap().broadcastMessage(MobPacket.dropStone("DropStone", list2));
                     mapleMist = new MapleMist(new Rectangle(-1695, -357, 365, 463), monster, this, (int)this.getDuration());
                     Point pos3 = isFacingLeft ? new Point(965, 443) : new Point(-1750, 443);
                     mapleMist.setPosition(pos3);
                     mapleMist.setCustomx(isFacingLeft ? -1035 : 600);
                     mapleMist.setOwnerId(monster.getObjectId());
                     monster.getMap().spawnMist(mapleMist, false);
                  } else if (this.skillLevel == 6) {
                     mapleMist = new MapleMist(new Rectangle(-1870, 133, 1150, 463), monster, this, (int)this.getDuration());
                     point = isFacingLeft ? new Point(965, 443) : new Point(-1750, 443);
                     mapleMist.setPosition(point);
                     mapleMist.setCustomx(isFacingLeft ? -1035 : 600);
                     mapleMist.setOwnerId(monster.getObjectId());
                     monster.getMap().spawnMist(mapleMist, false);
                  } else if (this.skillLevel == 11) {
                     if (monster.getCustomValue0(18611) == 0L) {
                        monster.setNextSkill(0);
                        monster.setNextSkillLvl(0);
                        return;
                     }

                     monster.setCustomInfo(18611, 0, 0);
                     Timer.MapTimer.getInstance().schedule(() -> {
                        MapleMist DustMist = new MapleMist(new Rectangle(-664, -940, 1387, 803), monster, this, (int)this.getDuration());
                        DustMist.setPosition(new Point(-45, -157));
                        if (DustMist != null) {
                           monster.getMap().spawnMist(DustMist, true);
                        }

                     }, 2500L);
                  } else if (player != null && this.lt != null && this.rb != null) {
                     mapleMist = new MapleMist(this.calculateBoundingBox(player.getTruePosition(), player.isFacingLeft()), monster, this, (int)this.duration);
                     if (mapleMist != null) {
                        monster.getMap().spawnMist(mapleMist, true);
                     }
                  }
               }
            } else {
               list2 = new ArrayList();
               list2.addAll(monster.getMap().getAllChracater());
               Collections.addAll(list2, new MapleCharacter[0]);
               Collections.shuffle(list2);
               var106 = list2.iterator();

               MapleMist mapleMist;
               do {
                  if (!var106.hasNext()) {
                     break label1871;
                  }

                  mapleCharacter = (MapleCharacter)var106.next();
                  mapleMist = new MapleMist(this.calculateBoundingBox(new Point(mapleCharacter.getPosition().x, monster.getPosition().y), mapleCharacter.isFacingLeft()), monster, this, (int)this.getDuration());
                  mapleMist.setPosition(new Point(mapleCharacter.getPosition().x, monster.getPosition().y));
               } while(mapleMist == null);

               monster.getMap().spawnMist(mapleMist, false);
            }
            break;
         case 188:
            this.duration = 30000L;
            diseases.put(SecondaryStat.Stance, new Pair(100, (int)this.duration));
            diseases.put(SecondaryStat.Attract, new Pair(monster.getPosition().x < player.getPosition().x ? 1 : 2, (int)this.duration));
            diseases.put(SecondaryStat.Slow, new Pair(50, (int)this.duration));
            player.getClient().send(CField.ChangeFaceMotion(17, 30000));
            break;
         case 189:
            diseases.put(SecondaryStat.CapDebuff, new Pair(100, (int)this.duration));
            break;
         case 190:
            diseases.put(SecondaryStat.CapDebuff, new Pair(200, (int)this.duration));
            break;
         case 191:
            Point clockPos = new Point(Randomizer.rand(-1063, 720), monster.getTruePosition().y);
            Rectangle box = this.calculateBoundingBox(monster.getTruePosition(), true);
            MapleMist clock = new MapleMist(box, monster, this, (int)this.duration);
            clock.setPosition(clockPos);
            monster.getMap().spawnMist(clock, false);
            monster.getMap().broadcastMessage(CWvsContext.getTopMsg("시간의 틈새에 '균열'이 발생하였습니다."));
            break;
         case 200:
         case 201:
            if (monster == null) {
               return;
            }

            if (this.skillId == 201 && this.skillLevel == 40 || this.skillId == 201 && this.skillLevel == 237 || this.skillLevel == 162) {
               return;
            }

            if (this.skillId == 200) {
               if (this.skillLevel == 228) {
                  monster.getMap().broadcastMessage(MobPacket.getSmartNotice(monster.getId(), 0, 2, 8, "시그너스가 상대의 행동에 분노를 느낍니다."));
                  monster.getMap().broadcastMessage(CWvsContext.getTopMsg("시그너스가 상대의 행동에 분노를 느낍니다."));
               } else if (this.skillLevel == 223) {
                  monster.getMap().broadcastMessage(MobPacket.getSmartNotice(monster.getId(), 0, 2, 2, "시그너스가 자신의 기사단을 위해 신수를 소환합니다."));
                  monster.getMap().broadcastMessage(CWvsContext.getTopMsg("시그너스가 자신의 기사단을 위해 신수를 소환합니다."));
               }
            } else if (this.skillId == 201) {
               if (this.skillLevel == 160) {
                  monster.getMap().broadcastMessage(MobPacket.getSmartNotice(monster.getId(), 0, 2, 8, "시그너스가 상대의 행동에 분노를 느낍니다."));
               } else if (this.skillLevel == 159) {
                  monster.getMap().broadcastMessage(MobPacket.getSmartNotice(monster.getId(), 0, 2, 2, "시그너스가 자신의 기사단을 위해 신수를 소환합니다."));
               }
            }

            if (this.skillId == 201 && this.skillLevel == 199) {
               this.toSummon.clear();
               if (Randomizer.nextBoolean()) {
                  this.toSummon.add(8880170);
               } else {
                  this.toSummon.add(8880175);
                  this.toSummon.add(8880178);
                  this.toSummon.add(8880179);
               }
            }

            if (this.skillId == 201 && this.skillLevel == 211) {
               monster.getMap().broadcastMessage(MobPacket.getSmartNotice(monster.getId(), 1, 5, 0, monster.getStats().getName() + "이 적들의 잠재능력에 위협을 느껴 무효화 된 틈을타 부하 몬스터를 소환합니다."));
            } else if (this.skillId == 200 && this.skillLevel == 212 || this.skillId == 201 && this.skillLevel == 210) {
               monster.getMap().broadcastMessage(MobPacket.getSmartNotice(monster.getId(), 0, 5, 1, monster.getStats().getName() + "이 주변에 체력을 흡수할 몬스터가 없어 새롭게 소환을 시도합니다."));
               Timer.MapTimer.getInstance().schedule(() -> {
                  if (monster.getController() != null && this.skillId == 201 && this.skillLevel == 210) {
                     monster.setNextSkill(105);
                     monster.setNextSkillLvl(13);
                  }

               }, 5000L);
            }

            int i22 = 0;
            var93 = this.getSummons().iterator();

            while(true) {
               byte effect;
               boolean flying;
               short deadtime;
               byte deadtimekillmob;
               Integer mobId;
               label1522:
               while(true) {
                  do {
                     do {
                        do {
                           while(true) {
                              if (!var93.hasNext()) {
                                 break label1871;
                              }

                              mobId = (Integer)var93.next();
                              var96 = null;

                              try {
                                 toSpawn = MapleLifeFactory.getMonster(GameConstants.getCustomSpawnID(monster.getId(), mobId));
                                 break;
                              } catch (RuntimeException var85) {
                              }
                           }
                        } while(toSpawn == null);
                     } while(toSpawn.getStats().getName().contains("악몽의 골렘"));
                  } while(toSpawn.getStats().getName().equals("악몽의 골렘"));

                  if (toSpawn.getStats().getHp() < toSpawn.getHp()) {
                     toSpawn.setHp(toSpawn.getStats().getHp());
                  }

                  toSpawn.setPosition(monster.getTruePosition());
                  ypos = (int)monster.getTruePosition().getY();
                  xpos = (int)monster.getTruePosition().getX();
                  if (this.skillId == 200 && this.skillLevel == 251) {
                     stats.add(new Pair(MonsterStatus.MS_ExchangeAttack, new MonsterStatusEffect(200, 1000, 1L)));
                  } else if (this.skillId == 200 && this.skillLevel == 250) {
                     monster.setLastSkillUsed(this, System.currentTimeMillis(), (long)(Randomizer.rand(10, 20) * 1000));
                  }

                  if (this.getAfterDead() > 0) {
                     toSpawn.setHp(monster.getHp());
                     Iterator var108 = toSpawn.getSkills().iterator();

                     while(var108.hasNext()) {
                        MobSkill sk = (MobSkill)var108.next();
                        if (sk.getAfterDead() > 0) {
                           toSpawn.setLastSkillUsed(sk, System.currentTimeMillis(), 40000L);
                        }
                     }

                     if (toSpawn.getId() >= 8920000 && toSpawn.getId() <= 8920003 || toSpawn.getId() >= 8920100 && toSpawn.getId() <= 8920103) {
                        if (toSpawn.getId() != 8920000 && toSpawn.getId() != 8920100) {
                           if (toSpawn.getId() != 8920001 && toSpawn.getId() != 8920101) {
                              if (toSpawn.getId() != 8920002 && toSpawn.getId() != 8920102) {
                                 if (toSpawn.getId() == 8920003 || toSpawn.getId() == 8920103) {
                                    player.getMap().broadcastMessage(CField.removeMapEffect());
                                    player.getMap().broadcastMessage(CField.startMapEffect("내 고통을 느끼게 해줄게요.", 5120102, true));
                                 }
                              } else {
                                 player.getMap().broadcastMessage(CField.removeMapEffect());
                                 player.getMap().broadcastMessage(CField.startMapEffect("모두 불태워주마!", 5120100, true));
                              }
                           } else {
                              player.getMap().broadcastMessage(CField.removeMapEffect());
                              player.getMap().broadcastMessage(CField.startMapEffect("킥킥. 다 없애주지", 5120101, true));
                           }
                        } else {
                           player.getMap().broadcastMessage(CField.removeMapEffect());
                           player.getMap().broadcastMessage(CField.startMapEffect("내가 상대해주겠어요.", 5120099, true));
                        }
                     }

                     monster.getMap().killMonster(monster, player, false, false, (byte)0);
                  }

                  deadtime = 0;
                  deadtimekillmob = 0;
                  int randcount = false;
                  int i6 = false;
                  int maxx = false;
                  effect = 0;
                  flying = false;
                  switch(mobId) {
                  case 8500003:
                     toSpawn.setFh((int)Math.ceil(Math.random() * 19.0D));
                     ypos = -590;
                     break label1522;
                  case 8500004:
                     xpos = (int)(monster.getTruePosition().getX() + Math.ceil(Math.random() * 1000.0D) - 500.0D);
                     ypos = (int)monster.getTruePosition().getY();
                     break label1522;
                  case 8500009:
                  case 8500014:
                     deadtime = 6000;
                     ypos = 179;
                     if (i22 == 0) {
                        xpos = -562;
                     } else if (i22 == 1) {
                        xpos = -9;
                     } else if (i22 == 2) {
                        xpos = 662;
                     }
                     break label1522;
                  case 8500013:
                     xpos = monster.getPosition().x + Randomizer.rand(-500, 500);
                     ypos = monster.getPosition().y + Randomizer.rand(-150, 150);
                     break label1522;
                  case 8510100:
                     if (Math.ceil(Math.random() * 5.0D) == 1.0D) {
                        ypos = 78;
                        xpos = (int)(0.0D + Math.ceil(Math.random() * 5.0D)) + (Math.ceil(Math.random() * 2.0D) == 1.0D ? 180 : 0);
                     } else {
                        xpos = (int)(monster.getTruePosition().getX() + Math.ceil(Math.random() * 1000.0D) - 500.0D);
                     }
                     break label1522;
                  case 8610023:
                  case 8610024:
                  case 8610025:
                  case 8610026:
                  case 8610027:
                  case 8840015:
                  case 8870002:
                  case 8870103:
                  case 8870104:
                  case 8870106:
                  case 8870107:
                  case 8880201:
                     if (mobId == 8880201) {
                        deadtime = 30000;
                     }

                     xpos = monster.getPosition().x + Randomizer.rand(-500, 500);
                     if (monster.getMap().getLeft() > xpos) {
                        xpos = monster.getMap().getLeft() + 50;
                     } else if (monster.getMap().getRight() < xpos) {
                        xpos = monster.getMap().getRight() - 50;
                     }

                     ypos = monster.getPosition().y;
                     break label1522;
                  case 8820007:
                     break;
                  case 8870005:
                  case 8870105:
                     xpos = Randomizer.rand(-610, -550);
                     if (Randomizer.isSuccess(50)) {
                        xpos = Randomizer.rand(350, 450);
                     }

                     ypos = 196;
                     break label1522;
                  case 8880157:
                  case 8880164:
                  case 8880184:
                  case 8880185:
                     deadtime = 26000;
                     if (toSpawn.getId() == 8880157 || toSpawn.getId() == 8880164) {
                        xpos = Randomizer.rand(735, 1100);
                     }

                     monster.setLastSkillUsed(this, System.currentTimeMillis(), (long)(Randomizer.rand(30, 40) * 1000));
                     break label1522;
                  case 8880165:
                  case 8880168:
                  case 8880169:
                  case 8880175:
                  case 8880178:
                  case 8880179:
                     deadtime = 20000;
                     monster.setLastSkillUsed(this, System.currentTimeMillis(), (long)(Randomizer.rand(10, 15) * 1000));
                     break label1522;
                  case 8880306:
                  case 8880308:
                     xpos = monster.getMap().getMonsterById(mobId) != null ? -600 : 600;
                     ypos = 215;
                     deadtime = 23000;
                     break label1522;
                  case 8880315:
                  case 8880316:
                     flying = true;
                     deadtime = 10000;
                     break label1522;
                  case 8880408:
                     monster.getMap().broadcastMessage(CField.enforceMSG("힐라가 죽음의 밑바닥에서 스우의 사령을 끌어올리는 소리가 들린다.", 254, 6000));
                     break label1522;
                  case 8880409:
                     monster.getMap().broadcastMessage(CField.enforceMSG("힐라가 죽음의 밑바닥에서 데미안의 사령을 끌어올리는 소리가 들린다.", 254, 6000));
                     break label1522;
                  case 8920004:
                     deadtime = 10000;
                     xpos = Randomizer.rand(-854, 928);
                     break label1522;
                  case 8920005:
                  case 8920105:
                     toSpawn.setLastSkillUsed(MobSkillFactory.getMobSkill(188, 1), System.currentTimeMillis(), 5000L);
                     xpos = monster.getPosition().x + Randomizer.rand(-500, 500);
                     if (monster.getMap().getLeft() > xpos) {
                        xpos = monster.getMap().getLeft() + 50;
                     } else if (monster.getMap().getRight() < xpos) {
                        xpos = monster.getMap().getRight() - 50;
                     }

                     ypos = monster.getPosition().y;
                  default:
                     break label1522;
                  }
               }

               if (this.skillLevel == 238) {
                  flying = true;
                  if (i22 == 0) {
                     xpos = -580;
                     ypos = 80;
                  } else if (i22 == 1) {
                     xpos = -450;
                     ypos = -250;
                  } else if (i22 == 2) {
                     xpos = 450;
                     ypos = -250;
                  } else if (i22 == 3) {
                     xpos = 580;
                     ypos = 80;
                  }
               } else if (this.skillLevel == 239) {
                  flying = true;
                  if (i22 == 0) {
                     xpos = -580;
                     ypos = -2300;
                  } else if (i22 == 1) {
                     xpos = -450;
                     ypos = -2500;
                  } else if (i22 == 2) {
                     xpos = 450;
                     ypos = -2500;
                  } else if (i22 == 3) {
                     xpos = 580;
                     ypos = -2300;
                  }
               } else if (this.skillLevel == 240) {
                  flying = true;
                  if (i22 == 0) {
                     xpos = -580;
                     ypos = -40;
                  } else if (i22 == 1) {
                     xpos = 0;
                     ypos = -40;
                  } else if (i22 == 2) {
                     xpos = 580;
                     ypos = -40;
                  }
               }

               if (monster.getStats().getName().contains("어둠의 집행자")) {
                  flying = true;
               }

               switch(monster.getMap().getId()) {
               case 230040420:
                  if (xpos < -239) {
                     xpos = (int)(-239.0D + Math.ceil(Math.random() * 150.0D));
                  } else if (xpos > 371) {
                     xpos = (int)(371.0D - Math.ceil(Math.random() * 150.0D));
                  }
               }

               if (this.skillLevel == 139 || this.skillLevel == 83) {
                  flying = true;
                  switch(toSpawn.getId() % 10) {
                  case 3:
                     xpos = 507;
                     ypos = -248;
                     break;
                  case 4:
                     xpos = -420;
                     ypos = -421;
                     break;
                  case 5:
                     xpos = -511;
                     ypos = -250;
                  case 6:
                  default:
                     break;
                  case 7:
                     xpos = 417;
                     ypos = -423;
                  }
               }

               if (this.skillId == 201 && this.skillLevel == 49) {
                  xpos = player.getTruePosition().x;
                  ypos = player.getTruePosition().y;
                  deadtime = 5000;
               }

               int objectId = monster.getMap().spawnMonsterWithEffect(toSpawn, effect > 0 ? -1 : (this.getSpawnEffect() == 77 ? 1 : this.getSpawnEffect()), flying ? new Point(xpos, ypos) : monster.getMap().calcPointBelow(new Point(xpos, ypos - 1)));
               if (this.isMobGroup()) {
                  if (!monster.getSpawnList().contains(toSpawn.getId())) {
                     toSpawn.setMobGroup(true);
                     monster.getSpawnList().add(toSpawn.getId());
                     break label1871;
                  }
               } else {
                  if (deadtime > 0) {
                     toSpawn.setDeadTime(deadtime);
                  } else if (deadtimekillmob > 0) {
                     toSpawn.setDeadTimeKillmob(deadtimekillmob);
                  }

                  if (mobId == 8800117) {
                     Timer.MapTimer.getInstance().schedule(() -> {
                        if (monster.getController() != null && monster.getMap().getMonsterById(8800117) != null) {
                           monster.getMap().killMonster(8800117);
                        }

                     }, 10000L);
                     ++i22;
                  }
               }
            }
         case 203:
            MapleMap target = ChannelServer.getInstance(player.getClient().getChannel()).getMapFactory().getMap(monster.getMap().getId() + 10);
            target.resetFully();
            target.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(monster.getId() + 1), new Point(Randomizer.rand(-1026, 690), 245));
            int rand = Randomizer.rand(1, 9);
            monster.setCustomInfo(8910000, 0, 20000);
            monster.getMap().setCustomInfo(8910000, rand, 0);
            monster.getMap().broadcastMessage(CField.environmentChange("Pt0" + rand + "gate", 2));
            var93 = monster.getMap().getAllChracater().iterator();

            while(var93.hasNext()) {
               mapleCharacter = (MapleCharacter)var93.next();
               mapleCharacter.setSkillCustomInfo(8910000, 0L, 20000L);
            }

            monster.getMap().broadcastMessage(CField.VonVonStopWatch(20000));
            break;
         case 211:
            MapleMist mist3 = new MapleMist(this.calculateBoundingBox(monster.getPosition(), monster.isFacingLeft()), monster, this, 30000);
            mist3.setPosition(monster.getPosition());
            monster.getMap().spawnMist(mist3, false);
            break;
         case 212:
            stats.add(new Pair(MonsterStatus.MS_Pad, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            break;
         case 213:
            switch(this.skillLevel) {
            case 10:
               this.setMobSkillDelay(player, monster, 2220, (short)0, isFacingLeft);
            case 11:
            case 12:
            default:
               break label1871;
            case 13:
               this.setMobSkillDelay(player, monster, 2040, (short)0, isFacingLeft);
               break label1871;
            case 14:
               this.setMobSkillDelay(player, monster, 1950, (short)0, isFacingLeft);
               break label1871;
            case 15:
               this.setMobSkillDelay(player, monster, 2160, (short)0, isFacingLeft);
               break label1871;
            }
         case 214:
            int time = false;
            if (this.skillLevel == 13) {
               int time = 30000;
               if (monster.getId() >= 8800130 && monster.getId() <= 8800137) {
                  Timer.MapTimer.getInstance().schedule(() -> {
                     MapleMonster zakum = monster.getMap().getMonsterById(8800102);
                     if (zakum != null && zakum.getPhase() <= 2) {
                        monster.getMap().killMonster(monster.getId());
                        MapleMonster part = MapleLifeFactory.getMonster(monster.getId() - 27);
                        part.setF(1);
                        part.setFh(1);
                        part.setStance(5);
                        part.setPhase((byte)1);
                        monster.getMap().spawnMonsterWithEffectBelow(part, monster.getPosition(), -2);
                     }

                  }, (long)time);
               }

               player.getMap().broadcastMessage(MobPacket.demianRunaway(monster, (byte)0, this, time, false));
            } else if (this.skillLevel == 14) {
            }
            break;
         case 215:
            monster.setNextSkill(0);
            monster.setNextSkillLvl(0);
            monster.setLastSkillUsed(this, System.currentTimeMillis(), 30000L);
            monster.setSkillForbid(true);

            for(int z = 1; z <= 4; ++z) {
               Timer.MobTimer.getInstance().schedule(() -> {
                  monster.setNextSkill(0);
                  monster.setNextSkillLvl(0);
                  int[] obsplusx = new int[]{379, -5, -388};
                  int[] angle = new int[]{642, 514, 642};
                  int[] unk = new int[]{323, 0, 36};
                  List<Obstacle> obs2 = new ArrayList();

                  for(int iz = 0; iz < 3; ++iz) {
                     Obstacle ob = new Obstacle(this.skillLevel == 2 ? 58 : 59, new Point(monster.getPosition().x, -505), new Point(monster.getPosition().x + obsplusx[iz], this.skillLevel == 2 ? 16 : 17), 50, 90, 539, 105, 1, angle[iz], unk[iz]);
                     obs2.add(ob);
                  }

                  player.getMap().broadcastMessage(MobPacket.createObstacle(monster, obs2, (byte)4));
               }, (long)(z * 2000 - 1000));
               Timer.MobTimer.getInstance().schedule(() -> {
                  monster.setNextSkill(0);
                  monster.setNextSkillLvl(0);
                  int[] obsplusx = new int[]{496, 207, 0, -208, -497};
                  int[] angle = new int[]{714, 553, 514, 555, 714};
                  int[] unk = new int[]{316, 338, 0, 22, 44};
                  List<Obstacle> obs2 = new ArrayList();

                  for(int iz = 0; iz < 5; ++iz) {
                     Obstacle ob = new Obstacle(this.skillLevel == 2 ? 58 : 59, new Point(monster.getPosition().x, -505), new Point(monster.getPosition().x + obsplusx[iz], this.skillLevel == 2 ? 16 : 17), 50, 90, 964, 76, 1, angle[iz], unk[iz]);
                     obs2.add(ob);
                  }

                  player.getMap().broadcastMessage(MobPacket.createObstacle(monster, obs2, (byte)4));
               }, (long)(z * 2000));
            }

            Timer.MobTimer.getInstance().schedule(() -> {
               if (monster != null) {
                  monster.setNextSkill(0);
                  monster.setNextSkillLvl(0);
                  monster.setSkillForbid(false);
               }

            }, 11000L);
            break;
         case 217:
            this.setMobSkillDelay(player, monster, 1920, (short)0, isFacingLeft);
            break;
         case 220:
            stats.add(new Pair(MonsterStatus.MS_PImmune, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            stats.add(new Pair(MonsterStatus.MS_MImmune, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            stats.add(new Pair(MonsterStatus.MS_PCounter, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            stats.add(new Pair(MonsterStatus.MS_MCounter, new MonsterStatusEffect(this.skillId, (int)this.duration, (long)this.x)));
            break;
         case 221:
            diseases.put(SecondaryStat.Seal, new Pair(this.x, (int)this.duration));
            break;
         case 223:
            if (!monster.isBuffed(MonsterStatus.MS_Laser)) {
               monster.setCustomInfo(2286, 0, 60000);
               monster.setCustomInfo(22878, 0, Randomizer.rand(60000, 70000));
               monster.setEnergyspeed(1);
               monster.setEnergyleft(false);
               monster.setEnergycount(45);
               monster.setLastSkillUsed(this, System.currentTimeMillis(), 10000L);
               stats.add(new Pair(MonsterStatus.MS_Laser, new MonsterStatusEffect(this.skillId, (int)this.duration)));
            } else {
               var93 = monster.getSkills().iterator();

               while(var93.hasNext()) {
                  MobSkill sk = (MobSkill)var93.next();
                  if (sk.getSkillId() == 201) {
                     sk.applyEffect(player, monster, true, isFacingLeft);
                     return;
                  }
               }

               monster.setLastSkillUsed(this, System.currentTimeMillis(), (long)Randomizer.rand(5000, 10000));
            }
            break;
         case 226:
            player.getMap().broadcastMessage(CField.airBone(player, monster, this.skillId, this.skillLevel, -1400));
            break;
         case 227:
            MapleMist Swoomist = new MapleMist(this.calculateBoundingBox(monster.getTruePosition(), true), monster, this, (int)this.duration);
            if (Swoomist != null) {
               Swoomist.setPosition(monster.getPosition());
               monster.getMap().spawnMist(Swoomist, false);
            }
            break;
         case 228:
            switch(this.skillLevel) {
            case 6:
               monster.setSkillForbid(true);
               monster.setEnergyspeed(0);
               monster.getMap().broadcastMessage(MobPacket.LaserHandler(monster.getObjectId(), monster.getEnergycount(), monster.getEnergyspeed(), monster.isEnergyleft() ? 0 : 1));
               Timer.MobTimer.getInstance().schedule(() -> {
                  monster.setSkillForbid(false);
                  monster.setEnergyspeed(1);
                  monster.setEnergyleft(!monster.isEnergyleft());
                  monster.getMap().broadcastMessage(MobPacket.LaserHandler(monster.getObjectId(), monster.getEnergycount(), monster.getEnergyspeed(), monster.isEnergyleft() ? 0 : 1));
               }, 3000L);
               break;
            case 7:
               monster.setEnergyspeed(2);
               monster.setCustomInfo(2287, 0, 30000);
               monster.setCustomInfo(22878, 0, Randomizer.rand(60000, 70000));
               Timer.MobTimer.getInstance().schedule(() -> {
                  monster.setEnergyspeed(Randomizer.rand(3, 4));
               }, (long)Randomizer.rand(5000, 15000));
               Timer.MobTimer.getInstance().schedule(() -> {
                  monster.setEnergycount(monster.getEnergycount() + (monster.isEnergyleft() ? -(9 * monster.getEnergyspeed()) : 9 * monster.getEnergyspeed()));
                  monster.setEnergyspeed(1);
               }, 30000L);
            }
         case 230:
            if (monster != null && !monster.getRectangles().isEmpty()) {
               zone = (Rectangle)monster.getRectangles().get(RectCount);
               var106 = monster.getMap().getAllChracater().iterator();

               while(var106.hasNext()) {
                  mapleCharacter = (MapleCharacter)var106.next();
                  if (mapleCharacter != null && mapleCharacter.isAlive() && zone.x - 50 < mapleCharacter.getPosition().x && zone.x + 150 > mapleCharacter.getPosition().x && -16 >= mapleCharacter.getPosition().y && -316 <= mapleCharacter.getPosition().y) {
                     mapleCharacter.getPercentDamage(monster, this.skillId, this.skillLevel, 100, true);
                  }
               }
            }
            break;
         case 234:
            this.duration = (long)Randomizer.rand(5000, 10000);
            diseases.put(SecondaryStat.Contagion, new Pair(this.x, (int)this.duration));
            break;
         case 237:
            this.duration = 0L;
            ++player.Stigma;
            if (player.Stigma >= 7) {
               player.Stigma = 7;
            }

            Map<SecondaryStat, Pair<Integer, Integer>> dds = new HashMap();
            dds.put(SecondaryStat.Stigma, new Pair(player.Stigma, 0));
            player.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveDisease(dds, this, player));
            player.getMap().broadcastMessage(player, CWvsContext.BuffPacket.giveForeignDeBuff(player, dds), false);
            player.getMap().broadcastMessage(MobPacket.StigmaImage(player, false));
            player.getClient().getSession().writeAndFlush(SLFCGPacket.playSE("SoundEff/BossDemian/incStigma"));
            if (player.Stigma == 7 && player.getBuffedEffect(SecondaryStat.NotDamaged) == null && player.getBuffedEffect(SecondaryStat.IndieNotDamaged) == null) {
               player.Stigma = 0;
               player.getMap().setStigmaDeath(player.getMap().getStigmaDeath() + 1);
               player.playerIGDead();
               player.getMap().broadcastMessage(MobPacket.CorruptionChange((byte)0, player.getMap().getStigmaDeath()));
               player.getMap().broadcastMessage(CField.environmentChange("Effect/OnUserEff.img/demian/screen", 4));
               if (monster.getId() != 8880111 && monster.getId() != 8880101) {
                  player.getMap().broadcastMessage(CField.enforceMSG("낙인이 완성되어 데미안이 점점 어둠에 물들어 갑니다.", 216, 30000000));
               }

               Timer.MobTimer.getInstance().schedule(() -> {
                  if (monster.isAlive()) {
                     player.getMap().broadcastMessage(CField.enforceMSG(monster.getSpecialtxt(), 216, 30000000));
                  }

               }, 3000L);
               player.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.cancelBuff(dds, player));
               player.getMap().broadcastMessage(player, CWvsContext.BuffPacket.cancelForeignBuff(player, dds), false);
               if (player.getMap().getStigmaDeath() >= 7 && monster.getId() != 8880111 && monster.getId() != 8880101) {
                  monster.DemainChangePhase(player);
               } else {
                  MapleFlyingSword mapleFlyingSword = new MapleFlyingSword(1, monster);
                  player.getMap().spawnFlyingSword(mapleFlyingSword);
                  player.getMap().setNewFlyingSwordNode(mapleFlyingSword, monster.getTruePosition());
               }
            }

            return;
         case 238:
            switch(this.skillLevel) {
            case 1:
            case 2:
            case 3:
               player.getMap().broadcastMessage(MobPacket.BossLucid.doFlowerTrapSkill(this.skillLevel, Randomizer.nextInt(3), 1000, 48, Randomizer.nextBoolean()));
               break label1871;
            case 4:
            case 10:
               list2 = new ArrayList<FairyDust>() {
                  {
                     int u = 2640;
                     int x;
                     byte w2;
                     byte w;
                     byte v2;
                     short v;
                     short s2;
                     short s;
                     if (MobSkill.this.skillLevel == 4) {
                        s = 180;
                        s2 = 240;
                        v = 100;
                        v2 = 5;
                        w = 3;
                        w2 = 1;
                        x = 40;
                     } else {
                        s = 30;
                        s2 = 330;
                        v = 250;
                        v2 = 100;
                        w = 6;
                        w2 = 3;
                        x = 5;
                     }

                     int max = Randomizer.rand(w, w + w2);

                     for(int i = 0; i < max; ++i) {
                        x += Randomizer.nextInt(x);
                        this.add(new FairyDust(Randomizer.nextInt(3), u, v + Randomizer.nextInt(v2), x + Randomizer.rand(s, s2)));
                     }

                  }
               };
               monster.setLastSkillUsed(this, System.currentTimeMillis(), (long)(Randomizer.rand(10, 20) * 1000));
               player.getMap().broadcastMessage(MobPacket.BossLucid.doFairyDustSkill(this.skillLevel, list2));
               player.getMap().broadcastMessage(CField.enforceMSG("저 바람을 맞으면 꿈이 더 강해질 겁니다!", 222, 2000));
               break label1871;
            case 5:
               List<Integer> laserIntervals = new ArrayList<Integer>() {
                  {
                     for(int i = 0; i < 15; ++i) {
                        this.add(500);
                     }

                  }
               };
               monster.setCustomInfo(23807, 0, 10000);
               player.getMap().broadcastMessage(MobPacket.BossLucid.doLaserRainSkill(4500, laserIntervals));
               player.getMap().broadcastMessage(CField.enforceMSG("루시드가 강력한 공격을 사용하려 합니다!", 222, 2000));
               break label1871;
            case 6:
               player.getClient().getSession().writeAndFlush(MobPacket.BossLucid.doForcedTeleportSkill(Randomizer.nextInt(8)));
               break label1871;
            case 7:
               boolean isLeft = Randomizer.isSuccess(50);
               if (monster.getMap().getId() != 450004150 && monster.getMap().getId() != 450004450 && monster.getMap().getId() != 450003840) {
                  i6 = isLeft ? -138 : 1498;
                  xdistance = Randomizer.nextBoolean() ? -1312 : 238;
                  ypos = monster.getPosition().y;
                  player.getMap().broadcastMessage(MobPacket.BossLucid.createDragon(2, i6, ypos, i6, xdistance, isLeft));
               } else {
                  player.getMap().broadcastMessage(MobPacket.BossLucid.createDragon(1, 0, 0, 0, 0, isLeft));
               }

               monster.setCustomInfo(23807, 0, 20000);
               player.getMap().broadcastMessage(CField.enforceMSG("루시드가 강력한 소환수를 소환했습니다!", 222, 2000));
               break label1871;
            case 8:
               monster.setCustomInfo(23807, 0, 10000);
               player.getMap().broadcastMessage(CField.enforceMSG("루시드가 강력한 공격을 사용하려 합니다!", 222, 2000));
               player.getMap().broadcastMessage(MobPacket.BossLucid.doRushSkill());
               break label1871;
            case 9:
               var93 = monster.getMap().getAllMonster().iterator();

               while(var93.hasNext()) {
                  will1 = (MapleMonster)var93.next();
                  if (will1.getId() != 8880150 && will1.getId() != 8880151 && will1.getId() != 8880155) {
                     will1.getMap().killMonsterType(will1, 0);
                  }
               }

               monster.removeCustomInfo(8880140);
               player.getMap().broadcastMessage(MobPacket.BossLucid.RemoveButterfly());
               player.getMap().broadcastMessage(MobPacket.BossLucid.setStainedGlassOnOff(false, FieldLucid.STAINED_GLASS));
               player.getMap().broadcastMessage(MobPacket.BossLucid.setButterflyAction(Butterfly.Mode.MOVE, 600, -500));
               player.getMap().broadcastMessage(MobPacket.BossLucid.changeStatueState(true, 0, false));
               player.getMap().broadcastMessage(MobPacket.BossLucid.setFlyingMode(true));
               player.getMap().broadcastMessage(MobPacket.BossLucid.doBidirectionShoot(50, 20, 100000, 8));
               player.getMap().broadcastMessage(MobPacket.BossLucid.doSpiralShoot(4, 390, 225, 13, 3, 3500, 10, 10, 1));
               player.getMap().broadcastMessage(MobPacket.BossLucid.doSpiralShoot(5, 10, 15, 20, 40, 4000, 13, 0, 0));
               player.getMap().broadcastMessage(MobPacket.BossLucid.doWelcomeBarrageSkill(2));
               monster.setCustomInfo(23888, 1, 0);
               monster.setCustomInfo(23807, 0, 15700);
               Timer.MobTimer.getInstance().schedule(() -> {
                  monster.setCustomInfo(23888, 0, 0);
                  player.getMap().broadcastMessage(MobPacket.BossLucid.setStainedGlassOnOff(true, FieldLucid.STAINED_GLASS));
                  player.getMap().broadcastMessage(MobPacket.BossLucid.setFlyingMode(false));
                  player.getMap().broadcastMessage(MobPacket.BossLucid.changeStatueState(true, 0, true));
                  Iterator var2 = player.getMap().getAllCharactersThreadsafe().iterator();

                  while(var2.hasNext()) {
                     MapleCharacter chr1 = (MapleCharacter)var2.next();
                     chr1.getClient().send(CField.fireBlink(chr1.getId(), new Point(834, -573)));
                  }

               }, 15700L);
            case 11:
            default:
               break label1871;
            case 12:
               this.duration = (long)Randomizer.rand(7000, 10000);
               diseases.put(SecondaryStat.Contagion, new Pair(this.x, (int)this.duration));
               break label1871;
            }
         case 241:
            switch(this.skillLevel) {
            case 1:
            case 2:
               this.duration = 60000L;
               monster.getMap().broadcastMessage(MobPacket.SpeakingMonster(monster, 0, 0));
               monster.setCustomInfo(2412, 0, 60000);
               stats.add(new Pair(MonsterStatus.MS_PopulatusTimer, new MonsterStatusEffect(this.skillId, (int)this.duration, 1L)));
               MobSkill ms1 = MobSkillFactory.getMobSkill(241, 3);
               int j = Randomizer.rand(20000, 25000);
               ms1.setDuration((long)j);
               var93 = monster.getMap().getAllCharactersThreadsafe().iterator();

               while(var93.hasNext()) {
                  mapleCharacter = (MapleCharacter)var93.next();
                  mapleCharacter.setSkillCustomInfo(241, 0L, (long)j);
                  mapleCharacter.giveDebuff(SecondaryStat.PapulCuss, ms1);
               }

               int abd = 0;
               var93 = monster.getMap().monsterSpawn.iterator();

               while(var93.hasNext()) {
                  Spawns spawnPoint = (Spawns)var93.next();
                  percent = Randomizer.rand(8500007, 8500008);
                  MapleMonster monster3 = MapleLifeFactory.getMonster(percent);
                  monster.getMap().spawnMonsterOnGroundBelow(monster3, spawnPoint.getPosition());
                  ++abd;
                  if (abd == 7) {
                     break label1871;
                  }
               }
            case 3:
            case 6:
            default:
               break label1871;
            case 4:
               MapleCharacter chr3 = monster.getController().getClient().getRandomCharacter();
               if (chr3 != null) {
                  chr3.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr3, this.skillLevel, this.skillId, 73, 0, 0, (byte)0, true, (Point)null, (String)null, (Item)null));
                  chr3.getMap().broadcastMessage(chr3, CField.EffectPacket.showEffect(chr3, this.skillLevel, this.skillId, 73, 0, 0, (byte)0, false, (Point)null, (String)null, (Item)null), false);
                  Timer.MobTimer.getInstance().schedule(() -> {
                     chr3.getClient().send(CField.onUserTeleport(Randomizer.rand(-880, 1030), 100));
                     chr3.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr3, this.skillLevel, this.skillId, 73, 1, 0, (byte)0, true, (Point)null, (String)null, (Item)null));
                     chr3.getMap().broadcastMessage(chr3, CField.EffectPacket.showEffect(chr3, this.skillLevel, this.skillId, 73, 1, 0, (byte)0, false, (Point)null, (String)null, (Item)null), false);
                  }, 1000L);
               }
               break label1871;
            case 5:
               diseases.put(SecondaryStat.PapulBomb, new Pair(this.x, (int)this.duration));
               break label1871;
            case 7:
               if (monster.getBuff(MonsterStatus.MS_PopulatusTimer) != null) {
                  monster.cancelSingleStatus(monster.getBuff(MonsterStatus.MS_PopulatusTimer));
               }

               stats.add(new Pair(MonsterStatus.MS_PopulatusInvincible, new MonsterStatusEffect(this.skillId, 610000, (long)this.x)));
               var93 = monster.getMap().getAllReactor().iterator();

               while(var93.hasNext()) {
                  MapleReactor reactor = (MapleReactor)var93.next();
                  if (reactor != null && reactor.getReactorId() == 2208011) {
                     reactor.forceHitReactor((byte)1, 0);
                  }
               }

               monster.getMap().PapulratusPatan = 2;
               monster.getMap().PapulratusTime = 0;
               monster.SetPatten(0);
               monster.getMap().Papullatushour = Randomizer.rand(0, 12);
               monster.getMap().Papullatusminute = Randomizer.rand(0, 60);
               monster.getMap().Mapcoltime = 60;
               monster.getMap().respawn(true);
               a = Randomizer.rand(241, 246);
               int typeed = monster.getId() % 100 / 10;
               String difical = typeed == 0 ? "Easy" : (typeed == 1 ? "Normal" : "Chaos");

               for(int ab = 241; ab < 246; ++ab) {
                  if (difical.equals("Chaos")) {
                     monster.setCustomInfo(ab, a == ab ? 3 : Randomizer.rand(1, 3), 0);
                  } else if (difical.equals("Normal")) {
                     monster.setCustomInfo(ab, Randomizer.rand(1, 3), 0);
                  } else if (difical.equals("Easy")) {
                     monster.setCustomInfo(ab, a == ab ? 2 : 1, 0);
                  }
               }

               monster.getMap().broadcastMessage(CField.startMapEffect("파풀라투스가 시간 이동을 할 수 없도록 차원의 균열을 봉인해야 합니다.", 5120177, true));
               monster.getMap().broadcastMessage(MobPacket.BossPapuLatus.PapulLatusLaser(true, 0));
               monster.getMap().broadcastMessage(MobPacket.BossPapuLatus.PapulLatusTime(0, false, true));
               monster.getMap().broadcastMessage(MobPacket.BossPapuLatus.PapulLatusTimePatten(0, monster.getMap().Papullatushour, monster.getMap().Papullatusminute, monster.getMap().Mapcoltime, (int)monster.getCustomValue0(241), (int)monster.getCustomValue0(242), (int)monster.getCustomValue0(243), (int)monster.getCustomValue0(244), (int)monster.getCustomValue0(245), (int)monster.getCustomValue0(246)));
               Timer.MapTimer.getInstance().schedule(new Runnable() {
                  public void run() {
                     int hppercent = 0;

                     for(int ab = 241; ab <= 246; ++ab) {
                        switch((int)monster.getCustomValue0(ab)) {
                        case 1:
                           ++hppercent;
                           break;
                        case 2:
                           hppercent += 10;
                           break;
                        case 3:
                           hppercent += 100;
                        }
                     }

                     monster.getMap().PapulratusPatan = 0;
                     if (hppercent >= 100) {
                        hppercent = 100;
                     }

                     if (hppercent >= 30) {
                        monster.setLastSkillUsed(MobSkill.this, 0L, 0L);
                     }

                     if (hppercent > 0) {
                        long hp = monster.getStats().getHp() / 100L * (long)hppercent;
                        monster.addHp(hp, true);
                        monster.getMap().broadcastMessage(MobPacket.damageMonster(monster.getObjectId(), hp, true));
                     }

                     Iterator var5 = monster.getMap().getAllReactor().iterator();

                     while(var5.hasNext()) {
                        MapleReactor reactor = (MapleReactor)var5.next();
                        if (reactor != null && reactor.getReactorId() == 2208011) {
                           reactor.forceHitReactor((byte)0, 0);
                           break;
                        }
                     }

                     var5 = monster.getMap().getAllMonster().iterator();

                     while(true) {
                        MapleMonster monster2;
                        do {
                           do {
                              if (!var5.hasNext()) {
                                 monster.getMap().removeDrops();
                                 monster.SetPatten(120);
                                 monster.getMap().broadcastMessage(MobPacket.BossPapuLatus.PapulLatusLaser(false, 1));
                                 monster.getMap().broadcastMessage(MobPacket.BossPapuLatus.PapulLatusTime(monster.getPatten() * 1000, false, false));
                                 monster.cancelStatus(MonsterStatus.MS_PopulatusInvincible, monster.getBuff(MonsterStatus.MS_PopulatusInvincible));
                                 return;
                              }

                              monster2 = (MapleMonster)var5.next();
                           } while(monster2 == null);
                        } while(monster2.getId() != 8500003 && monster2.getId() != 8500004);

                        monster2.getMap().killMonster(monster2);
                     }
                  }
               }, 60000L);
               break label1871;
            case 8:
               diseases.put(SecondaryStat.Stun, new Pair(1, (int)this.duration));
               break label1871;
            }
         case 242:
            int id;
            switch(this.skillLevel) {
            case 1:
            case 2:
            case 3:
               int id = false;
               if (monster.getId() != 8880321 && monster.getId() != 8880322) {
                  if (monster.getId() != 8880323 && monster.getId() != 8880324) {
                     if (monster.getId() != 8880353 && monster.getId() != 8880354) {
                        id = monster.getId() - 8;
                     } else {
                        id = monster.getId() - 12;
                     }
                  } else {
                     id = monster.getId() - 22;
                  }
               } else {
                  id = monster.getId() - 18;
               }

               final int size = Randomizer.rand(17, 23);
               List<Triple<Integer, Integer, Integer>> idx = new ArrayList<Triple<Integer, Integer, Integer>>() {
                  {
                     for(int i = 1; i <= size; ++i) {
                        this.add(new Triple(i, 1800 * (1 + i / 6), -650 + 130 * Randomizer.rand(1, 9)));
                     }

                  }
               };
               player.getMap().broadcastMessage(MobPacket.BossWill.WillSpiderAttack(id, this.skillId, this.skillLevel, 0, idx));
               break;
            case 4:
               monster.getMap().broadcastMessage(CField.enforceMSG("눈동자를 공격해서 다른 공간에 달빛을 흘려보내세요. 어서 달빛 보호막을 생성해야 해요.", 245, 28000));
               monster.getMap().broadcastMessage(MobPacket.BossWill.willUseSpecial());
               list2 = new ArrayList<Integer>() {
                  {
                     for(int i = 0; i < 9; ++i) {
                        this.add(i);
                     }

                  }
               };
               var106 = monster.getMap().getAllMonstersThreadsafe().iterator();

               while(var106.hasNext()) {
                  toSpawn = (MapleMonster)var106.next();
                  toSpawn.getMap().broadcastMessage(MobPacket.BlockAttack(toSpawn, list2));
                  toSpawn.setSkillForbid(true);
                  toSpawn.setUseSpecialSkill(true);
                  if (toSpawn.getId() == 8880305) {
                     toSpawn.getMap().killMonster(toSpawn);
                  }
               }

               int bluecount = 0;
               int purplecount = 0;
               int reverge = monster.getController().getParty().getMembers().size() / 2;
               if (monster.getController() == null) {
                  reverge = player.getParty().getMembers().size() / 2;
               }

               for(var106 = monster.getMap().getAllCharactersThreadsafe().iterator(); var106.hasNext(); mapleCharacter.getClient().getSession().writeAndFlush(MobPacket.BossWill.WillSpiderAttack(monster.getId(), this.skillId, this.skillLevel, ypos, (List)null))) {
                  mapleCharacter = (MapleCharacter)var106.next();
                  ypos = Randomizer.rand(1, 2);
                  if (ypos == 1) {
                     ++bluecount;
                     if (bluecount > reverge) {
                        ypos = 2;
                        --bluecount;
                     }
                  } else {
                     ++purplecount;
                     if (purplecount > reverge) {
                        ypos = 1;
                        --purplecount;
                     }
                  }
               }

               var106 = monster.getMap().getAllMistsThreadsafe().iterator();

               while(var106.hasNext()) {
                  MapleMist mists = (MapleMist)var106.next();
                  if (mists.getMobSkill().getSkillId() == 242) {
                     monster.getMap().removeMist(mists);
                  }
               }

               monster.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880305), new Point(0, -2020));
               monster.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880305), new Point(0, 159));
               Timer.MobTimer.getInstance().schedule(() -> {
                  monster.getMap().killMonster(8880305);
                  ArrayList<MapleMist> mists = new ArrayList();
                  ArrayList<MapleCharacter> targets = new ArrayList();
                  Iterator var7 = monster.getMap().getAllMistsThreadsafe().iterator();

                  while(true) {
                     while(var7.hasNext()) {
                        MapleMist mi = (MapleMist)var7.next();
                        if (mi.getMobSkill() != null && mi.getMobSkill().getSkillId() == this.skillId && mi.getMobSkill().getSkillLevel() == this.skillLevel) {
                           mists.add(mi);
                        } else if (mi.getSource() != null && (mi.getSource().getSourceId() == 400031039 || mi.getSource().getSourceId() == 400031040)) {
                           mists.add(mi);
                        }
                     }

                     var7 = monster.getMap().getAllCharactersThreadsafe().iterator();

                     MapleCharacter chr;
                     while(var7.hasNext()) {
                        chr = (MapleCharacter)var7.next();
                        boolean add = true;
                        chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showWillEffect(chr, 1, this.skillId, this.skillLevel));
                        chr.getClient().getSession().writeAndFlush(MobPacket.BossWill.WillSpiderAttack(monster.getId(), this.skillId, this.skillLevel, 0, (List)null));
                        Iterator var10 = mists.iterator();

                        while(var10.hasNext()) {
                           MapleMist mist4 = (MapleMist)var10.next();
                           if (mist4.getBox().contains(chr.getTruePosition())) {
                              add = false;
                           }
                        }

                        if (add) {
                           targets.add(chr);
                        }
                     }

                     var7 = targets.iterator();

                     while(var7.hasNext()) {
                        chr = (MapleCharacter)var7.next();
                        if (chr.getBuffedEffect(SecondaryStat.HolyMagicShell) != null) {
                           if (chr.getHolyMagicShell() > 1) {
                              chr.setHolyMagicShell((byte)(chr.getHolyMagicShell() - 1));
                              HashMap<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
                              statups.put(SecondaryStat.HolyMagicShell, new Pair(Integer.valueOf(chr.getHolyMagicShell()), (int)chr.getBuffLimit(chr.getBuffSource(SecondaryStat.HolyMagicShell))));
                              chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, chr.getBuffedEffect(SecondaryStat.HolyMagicShell), chr));
                           } else {
                              chr.cancelEffectFromBuffStat(SecondaryStat.HolyMagicShell);
                           }
                        } else if (chr.getBuffedValue(4341052)) {
                           chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 1, 0, 36, 0, 0, (byte)0, true, (Point)null, (String)null, (Item)null));
                           chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 1, 0, 36, 0, 0, (byte)0, false, (Point)null, (String)null, (Item)null), false);
                        } else if (chr.getBuffedEffect(SecondaryStat.WindWall) != null) {
                           int windWall = Math.max(0, chr.getBuffedValue(SecondaryStat.WindWall) - 100 * chr.getBuffedEffect(SecondaryStat.WindWall).getZ());
                           if (windWall > 1) {
                              chr.setBuffedValue(SecondaryStat.WindWall, windWall);
                              HashMap<SecondaryStat, Pair<Integer, Integer>> statupsx = new HashMap();
                              statupsx.put(SecondaryStat.WindWall, new Pair(windWall, (int)chr.getBuffLimit(400031030)));
                              chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statupsx, chr.getBuffedEffect(SecondaryStat.WindWall), chr));
                           } else {
                              chr.cancelEffectFromBuffStat(SecondaryStat.WindWall);
                           }
                        } else if (chr.getBuffedEffect(SecondaryStat.TrueSniping) == null && chr.getBuffedEffect(SecondaryStat.Etherealform) == null && chr.getBuffedEffect(SecondaryStat.IndieNotDamaged) == null && chr.getBuffedEffect(SecondaryStat.NotDamaged) == null) {
                           chr.playerIGDead();
                        }
                     }

                     if (monster.isUseSpecialSkill()) {
                        ArrayList<Integer> newHpList = new ArrayList();
                        if (monster.getHPPercent() <= 67 && monster.getWillHplist().contains(666)) {
                           newHpList.add(333);
                           newHpList.add(3);
                        } else if (monster.getHPPercent() <= 34 && monster.getWillHplist().contains(333)) {
                           newHpList.add(3);
                        } else if (monster.getHPPercent() > 1 || !monster.getWillHplist().contains(3)) {
                           newHpList.add(666);
                           newHpList.add(333);
                           newHpList.add(3);
                        }

                        monster.setWillHplist(newHpList);
                        monster.getMap().broadcastMessage(MobPacket.BossWill.setWillHp(monster.getWillHplist(), monster.getMap(), monster.getId(), monster.getId() + 3, monster.getId() + 4));
                        MobSkillFactory.getMobSkill(242, 8).applyEffect(player, monster, skill, isFacingLeft);
                     } else {
                        var7 = monster.getMap().getAllMonstersThreadsafe().iterator();

                        while(var7.hasNext()) {
                           MapleMonster mob = (MapleMonster)var7.next();
                           mob.getMap().broadcastMessage(MobPacket.BlockAttack(mob, new ArrayList()));
                           mob.setSkillForbid(false);
                           mob.setUseSpecialSkill(false);
                        }

                        monster.setNextSkill(0);
                        monster.setNextSkillLvl(0);
                     }

                     Timer.MobTimer.getInstance().schedule(() -> {
                        MobSkillFactory.getMobSkill(242, 15).applyEffect(player, monster, skill, isFacingLeft);
                     }, 11000L);
                     return;
                  }
               }, 30000L);
               break;
            case 5:
               type = Randomizer.nextInt(2);
               monster.getMap().broadcastMessage(MobPacket.showBossHP(monster));
               monster.getMap().broadcastMessage(MobPacket.BossWill.WillSpiderAttack(monster.getId(), this.skillId, this.skillLevel, type, (List)null));
               Timer.MobTimer.getInstance().schedule(() -> {
                  if (monster.getId() == 8880300 || monster.getId() == 8880340) {
                     MapleMonster will1 = monster.getMap().getMonsterById(monster.getId() + 3);
                     MapleMonster will2 = monster.getMap().getMonsterById(monster.getId() + 4);
                     long hp1 = 0L;
                     long hp2 = 0L;
                     if (will1 != null) {
                        hp1 = will1.getHp();
                     }

                     if (will2 != null) {
                        hp2 = will2.getHp();
                     }

                     long newhp = Math.max(hp1, hp2);
                     monster.setHp(newhp);
                     if (will1 != null) {
                        will1.setHp(newhp);
                     }

                     if (will2 != null) {
                        will2.setHp(newhp);
                     }

                     monster.getMap().broadcastMessage(MobPacket.BossWill.setWillHp(monster.getWillHplist(), monster.getMap(), monster.getId(), monster.getId() + 3, monster.getId() + 4));
                  }

                  ArrayList<MapleMist> mists = new ArrayList();
                  ArrayList<MapleCharacter> targets = new ArrayList();
                  Iterator var16 = monster.getMap().getAllMistsThreadsafe().iterator();

                  while(true) {
                     MapleMist mi;
                     do {
                        do {
                           if (!var16.hasNext()) {
                              var16 = monster.getMap().getAllCharactersThreadsafe().iterator();

                              MapleCharacter chr;
                              while(var16.hasNext()) {
                                 chr = (MapleCharacter)var16.next();
                                 boolean add = true;
                                 chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showWillEffect(chr, 1, this.skillId, this.skillLevel));
                                 Iterator var11 = mists.iterator();

                                 while(var11.hasNext()) {
                                    MapleMist mist4 = (MapleMist)var11.next();
                                    if (mist4.getBox().contains(chr.getTruePosition())) {
                                       add = false;
                                    }
                                 }

                                 if (add) {
                                    targets.add(chr);
                                 }
                              }

                              var16 = targets.iterator();

                              while(true) {
                                 while(var16.hasNext()) {
                                    chr = (MapleCharacter)var16.next();
                                    if (chr.getBuffedEffect(SecondaryStat.HolyMagicShell) != null) {
                                       if (chr.getHolyMagicShell() > 1) {
                                          chr.setHolyMagicShell((byte)(chr.getHolyMagicShell() - 1));
                                          HashMap<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
                                          statups.put(SecondaryStat.HolyMagicShell, new Pair(Integer.valueOf(chr.getHolyMagicShell()), (int)chr.getBuffLimit(chr.getBuffSource(SecondaryStat.HolyMagicShell))));
                                          chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, chr.getBuffedEffect(SecondaryStat.HolyMagicShell), chr));
                                       } else {
                                          chr.cancelEffectFromBuffStat(SecondaryStat.HolyMagicShell);
                                       }
                                    } else if (chr.getBuffedValue(4341052)) {
                                       chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 1, 0, 36, 0, 0, (byte)0, true, (Point)null, (String)null, (Item)null));
                                       chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 1, 0, 36, 0, 0, (byte)0, false, (Point)null, (String)null, (Item)null), false);
                                    } else {
                                       int reduce;
                                       if (chr.getBuffedEffect(SecondaryStat.WindWall) != null) {
                                          reduce = Math.max(0, chr.getBuffedValue(SecondaryStat.WindWall) - 100 * chr.getBuffedEffect(SecondaryStat.WindWall).getZ());
                                          if (reduce > 0) {
                                             chr.setBuffedValue(SecondaryStat.WindWall, reduce);
                                             HashMap<SecondaryStat, Pair<Integer, Integer>> statupsx = new HashMap();
                                             statupsx.put(SecondaryStat.WindWall, new Pair(reduce, (int)chr.getBuffLimit(400031030)));
                                             chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statupsx, chr.getBuffedEffect(SecondaryStat.WindWall), chr));
                                          } else {
                                             chr.cancelEffectFromBuffStat(SecondaryStat.WindWall);
                                          }
                                       } else if (chr.getBuffedEffect(SecondaryStat.TrueSniping) == null && chr.getBuffedEffect(SecondaryStat.Etherealform) == null && chr.getBuffedEffect(SecondaryStat.IndieNotDamaged) == null && chr.getBuffedEffect(SecondaryStat.NotDamaged) == null) {
                                          reduce = 0;
                                          if (chr.getBuffedEffect(SecondaryStat.IndieDamageReduce) != null) {
                                             reduce = chr.getBuffedValue(SecondaryStat.IndieDamageReduce);
                                          } else if (chr.getBuffedEffect(SecondaryStat.IndieDamReduceR) != null) {
                                             reduce = -chr.getBuffedValue(SecondaryStat.IndieDamReduceR);
                                          }

                                          if (type == 0 && chr.getTruePosition().y > -455 && chr.getTruePosition().y < 300) {
                                             chr.addHP(-chr.getStat().getCurrentMaxHp() * (long)(100 - reduce) / 100L);
                                          } else if (type == 1 && chr.getTruePosition().y > -2500 && chr.getTruePosition().y < -1800) {
                                             chr.addHP(-chr.getStat().getCurrentMaxHp() * (long)(100 - reduce) / 100L);
                                          }
                                       }
                                    }
                                 }

                                 Timer.MobTimer.getInstance().schedule(() -> {
                                    double hppercent = (double)monster.getHp() * 100.0D / (double)monster.getStats().getHp();
                                    if (hppercent <= 66.6D && monster.getWillHplist().contains(666)) {
                                       MobSkillFactory.getMobSkill(242, 4).applyEffect(player, monster, skill, isFacingLeft);
                                    } else if (hppercent <= 33.3D && monster.getWillHplist().contains(333)) {
                                       MobSkillFactory.getMobSkill(242, 4).applyEffect(player, monster, skill, isFacingLeft);
                                    } else if (hppercent <= 0.3D && monster.getWillHplist().contains(3)) {
                                       MobSkillFactory.getMobSkill(242, 4).applyEffect(player, monster, skill, isFacingLeft);
                                    }

                                 }, 1000L);
                                 return;
                              }
                           }

                           mi = (MapleMist)var16.next();
                        } while(mi.getSource() == null);
                     } while(mi.getSource().getSourceId() != 400031039 && mi.getSource().getSourceId() != 400031040);

                     mists.add(mi);
                  }
               }, 3000L);
            case 6:
            default:
               break;
            case 7:
               monster.getMap().broadcastMessage(CField.enforceMSG("거짓의 거울은 공격을 반전시켜요. 균열이 나타나면 공격을 마주하세요.", 245, 26000));
               monster.getMap().broadcastMessage(MobPacket.BossWill.willUseSpecial());
               List<Integer> idss = new ArrayList<Integer>() {
                  {
                     for(int i = 0; i < 9; ++i) {
                        this.add(i);
                     }

                  }
               };
               var93 = monster.getMap().getAllMonstersThreadsafe().iterator();

               while(var93.hasNext()) {
                  will1 = (MapleMonster)var93.next();
                  will1.getMap().broadcastMessage(MobPacket.BlockAttack(will1, idss));
                  will1.setUseSpecialSkill(true);
                  will1.setSkillForbid(true);
                  will1.setNextSkill(0);
                  will1.setNextSkillLvl(0);
               }

               Timer.MobTimer.getInstance().schedule(() -> {
                  MobSkillFactory.getMobSkill(242, 14).applyEffect(player, monster, skill, isFacingLeft);
               }, 5000L);
               break;
            case 8:
               monster.getMap().broadcastMessage(MobPacket.BossWill.willStun());
               monster.getMap().broadcastMessage(CField.enforceMSG("지금이에요. 윌이 무방비 상태일 때 피해를 입혀야 해요.", 245, 12000));
               monster.setNextSkill(0);
               monster.setNextSkillLvl(0);
               var93 = monster.getMap().getAllMistsThreadsafe().iterator();

               while(var93.hasNext()) {
                  mapleMist = (MapleMist)var93.next();
                  if (mapleMist.getMobSkill().getSkillId() == 242) {
                     monster.getMap().broadcastMessage(CField.removeMist(mapleMist));
                     monster.getMap().removeMapObject(mapleMist);
                  }
               }

               if (monster.getId() != 8880300 && monster.getId() != 8880340) {
                  monster.getMap().broadcastMessage(MobPacket.forcedSkillAction(monster.getObjectId(), 2, false));
               } else {
                  will1 = monster.getMap().getMonsterById(monster.getId() + 3);
                  if (will1 != null) {
                     monster.getMap().broadcastMessage(MobPacket.forcedSkillAction(will1.getObjectId(), 3, false));
                  }

                  if ((will2 = monster.getMap().getMonsterById(monster.getId() + 4)) != null) {
                     monster.getMap().broadcastMessage(MobPacket.forcedSkillAction(will2.getObjectId(), 3, false));
                  }
               }

               Timer.MapTimer.getInstance().schedule(() -> {
                  Iterator var1 = monster.getMap().getAllMonstersThreadsafe().iterator();

                  while(var1.hasNext()) {
                     MapleMonster mob = (MapleMonster)var1.next();
                     mob.getMap().broadcastMessage(MobPacket.BlockAttack(mob, new ArrayList()));
                     mob.setSkillForbid(false);
                     mob.setUseSpecialSkill(false);
                  }

               }, 10000L);
               break;
            case 9:
               diseases.put(SecondaryStat.WillPoison, new Pair(1, 7000));
               player.setSkillCustomInfo(24219, (long)Randomizer.rand(1, Integer.MAX_VALUE), 0L);
               player.setSkillCustomInfo(24209, 1L, 0L);
               player.setSkillCustomInfo(24220, 0L, 3000L);
               player.getMap().broadcastMessage(MobPacket.BossWill.posion(player, (int)player.getSkillCustomValue0(24219), 0, 0, 0));
               Timer.MapTimer.getInstance().schedule(() -> {
                  if (player.getSkillCustomValue0(24219) > 0L) {
                     player.getMap().broadcastMessage(MobPacket.BossWill.removePoison(player, (int)player.getSkillCustomValue0(24219)));
                     player.removeSkillCustomInfo(24219);
                     player.removeSkillCustomInfo(24209);
                     player.removeSkillCustomInfo(24220);
                  }

               }, 7000L);
               break;
            case 10:
               player.getMap().broadcastMessage(MobPacket.BossWill.WillSpiderAttack(monster.getId(), this.skillId, this.skillLevel, 1, (List)null));
               break;
            case 11:
               player.getMap().broadcastMessage(MobPacket.BossWill.WillSpiderAttack(monster.getId(), this.skillId, this.skillLevel, 1, (List)null));
               break;
            case 12:
               id = 0;
               if (monster.getId() != 8880325 && monster.getId() != 8880326) {
                  if (monster.getId() != 8880327 && monster.getId() != 8880328) {
                     if (monster.getId() == 8880355 || monster.getId() == 8880356) {
                        id = monster.getId() - 12;
                     }
                  } else {
                     id = monster.getId() - 26;
                     if (player.getMapId() != 450008250 && player.getMapId() != 450008850) {
                        if (player.getMapId() == 450008350 || player.getMapId() == 450008950) {
                           id = player.getMapId() == 450008250 ? 8880302 : 8880342;
                           if (Randomizer.nextBoolean()) {
                              player.getMap().broadcastMessage(MobPacket.BossWill.createBulletEyes(0, id, -300, -400));
                           } else {
                              player.getMap().broadcastMessage(MobPacket.BossWill.createBulletEyes(0, id, -415, -400));
                              player.getMap().broadcastMessage(MobPacket.BossWill.createBulletEyes(0, id, 300, -400));
                           }
                        }
                     } else {
                        id = player.getMapId() == 450008250 ? 8880301 : 8880341;
                        if (Randomizer.nextBoolean()) {
                           player.getMap().broadcastMessage(MobPacket.BossWill.createBulletEyes(0, id, -492, -370));
                           player.getMap().broadcastMessage(MobPacket.BossWill.createBulletEyes(0, id, -8, -370));
                           player.getMap().broadcastMessage(MobPacket.BossWill.createBulletEyes(0, id, 501, -370));
                        } else {
                           player.getMap().broadcastMessage(MobPacket.BossWill.createBulletEyes(0, id, -300, -370));
                           player.getMap().broadcastMessage(MobPacket.BossWill.createBulletEyes(0, id, 300, -370));
                        }
                     }
                  }
               } else if (monster.getMap().getId() == 450008150) {
                  if (monster.getTruePosition().y < 0) {
                     id = 8880304;
                  } else {
                     id = 8880303;
                  }
               } else if (monster.getTruePosition().y < 0) {
                  id = 8880344;
               } else {
                  id = 8880343;
               }

               monster.setLastSkillUsed(this, System.currentTimeMillis(), this.getInterval());
               if (id != 8880341 && id != 8880342 && id != 8880302 && id != 8880301) {
                  if (Randomizer.nextBoolean()) {
                     player.getMap().broadcastMessage(MobPacket.BossWill.createBulletEyes(0, id, -250, -370));
                     player.getMap().broadcastMessage(MobPacket.BossWill.createBulletEyes(0, id, 250, -370));
                  } else {
                     player.getMap().broadcastMessage(MobPacket.BossWill.createBulletEyes(0, id, -470, -440));
                     player.getMap().broadcastMessage(MobPacket.BossWill.createBulletEyes(0, id, 470, -440));
                  }
               }

               if (monster.getTruePosition().y < 0) {
                  player.getMap().broadcastMessage(MobPacket.BossWill.createBulletEyes(1, id, 300, 100, -690, -2634, 695, -2019));
               } else {
                  player.getMap().broadcastMessage(MobPacket.BossWill.createBulletEyes(1, id, 300, 100, -690, -455, 695, 500));
               }
               break;
            case 13:
               int webSize = monster.getMap().getAllSpiderWeb().size();
               list2 = new ArrayList();
               if (webSize < 67) {
                  i5 = 0;
                  boolean respawn = false;
                  var96 = monster.getMap().getAllSpiderThreadsafe().iterator();

                  while(var96.hasNext()) {
                     SpiderWeb web = (SpiderWeb)var96.next();
                     list2.add(web.getNum());
                  }

                  Collections.sort(list2);

                  for(var96 = list2.iterator(); var96.hasNext(); ++i5) {
                     Integer liw = (Integer)var96.next();
                     if (liw != i5) {
                        monster.getMap().spawnSpiderWeb(new SpiderWeb(i5));
                        respawn = true;
                        break;
                     }
                  }

                  if (i5 < 67 && !respawn) {
                     monster.getMap().spawnSpiderWeb(new SpiderWeb(i5));
                  }
               }
               break;
            case 14:
               if (monster.getId() != 8880301 && monster.getId() != 8880341) {
                  return;
               }

               monster.setWillSpecialPattern(true);
               a = Randomizer.rand(0, 1);
               this.willSpider(monster.getController(), monster, a, false);
               Timer.MapTimer.getInstance().schedule(() -> {
                  this.willSpider(monster.getController(), monster, a == 0 ? 1 : 0, true);
               }, 11000L);
               Timer.MapTimer.getInstance().schedule(() -> {
                  if (monster.isWillSpecialPattern()) {
                     MobSkillFactory.getMobSkill(242, 8).applyEffect(player, monster, skill, isFacingLeft);
                     if (monster.getHPPercent() <= 50 && monster.getWillHplist().contains(500)) {
                        monster.setWillHplist(new ArrayList());
                        monster.getWillHplist().add(3);
                     } else if (monster.getHPPercent() <= 3 && monster.getWillHplist().contains(3)) {
                        monster.setWillHplist(new ArrayList());
                     } else {
                        monster.setWillHplist(new ArrayList());
                        monster.getWillHplist().add(500);
                        monster.getWillHplist().add(3);
                     }
                  } else {
                     Iterator var4 = monster.getMap().getAllMonstersThreadsafe().iterator();

                     while(var4.hasNext()) {
                        MapleMonster mob = (MapleMonster)var4.next();
                        mob.getMap().broadcastMessage(MobPacket.BlockAttack(mob, new ArrayList()));
                        mob.setUseSpecialSkill(false);
                        mob.setSkillForbid(false);
                     }
                  }

                  monster.getMap().broadcastMessage(MobPacket.BossWill.setWillHp(monster.getWillHplist()));
                  monster.setWillSpecialPattern(false);
               }, 30000L);
               break;
            case 15:
               var93 = monster.getMap().getAllCharactersThreadsafe().iterator();

               while(var93.hasNext()) {
                  mapleCharacter = (MapleCharacter)var93.next();
                  monster.getMap().broadcastMessage(CField.EffectPacket.showWillEffect(mapleCharacter, 0, this.skillId, this.skillLevel));
               }

               Timer.MobTimer.getInstance().schedule(() -> {
                  Iterator var2 = monster.getMap().getAllCharactersThreadsafe().iterator();

                  while(var2.hasNext()) {
                     MapleCharacter chr = (MapleCharacter)var2.next();
                     chr.getClient().getSession().writeAndFlush(CField.portalTeleport(Randomizer.nextBoolean() ? "ptup" : "ptdown"));
                     chr.getClient().getSession().writeAndFlush(MobPacket.BossWill.WillSpiderAttack(monster.getId(), this.skillId, this.skillLevel, Randomizer.nextInt(2), (List)null));
                  }

               }, 3000L);
            }
         case 246:
            List<Triple<Point, Integer, List<Rectangle>>> datas = new ArrayList();

            for(i6 = 0; i6 < 7; ++i6) {
               List<Rectangle> rectz = new ArrayList();
               int[] randXs = new int[]{0, 280, -560, 560, -280, -840, 840};
               ypos = Randomizer.nextInt(randXs.length);
               xpos = randXs[ypos];
               int delay = (ypos + 1) * 250;
               int[][][] rectXs = new int[][][]{{{-75, 50}, {13, -50}, {83, 72}, {83, 72}}, {{-81, 90}, {-59, -20}, {-25, 13}, {123, 31}, {138, -54}}, {{-78, 28}, {-13, -50}, {42, 81}, {75, -18}, {133, 4}}, {{-75, 50}, {13, -60}, {83, 72}, {83, 72}}, {{-81, 90}, {-59, -20}, {-25, 13}, {123, 31}, {138, -54}}, {{-78, 28}, {-13, -50}, {42, 81}, {75, -18}, {133, 4}}, {{-78, 28}, {-13, -50}, {42, 81}, {75, -18}, {133, 4}}};
               int[][] rectX = rectXs[ypos];

               for(int i6 = 0; i6 < rectX.length; ++i6) {
                  rectz.add(new Rectangle(rectX[i6][0], -80, rectX[i6][1], 640));
               }

               datas.add(new Triple(new Point(xpos, -260), delay, rectz));
            }

            monster.getMap().broadcastMessage(MobPacket.jinHillahBlackHand(monster.getObjectId(), this.skillId, this.skillLevel, datas));
            break;
         case 247:
            if (monster.getCustomValue0(24701) == 0L) {
               monster.setNextSkill(0);
               monster.setNextSkillLvl(0);
               return;
            }

            monster.setCustomInfo(24701, 0, 0);
            int Glasstime = monster.getHPPercent() >= 60 ? 150 : (monster.getHPPercent() >= 30 ? 120 : 100);
            monster.getMap().setSandGlassTime((long)Glasstime);
            monster.getMap().broadcastMessage(CField.JinHillah(4, monster.getController() == null ? player : monster.getController(), monster.getMap()));
            monster.setLastSkillUsed(this, System.currentTimeMillis(), (long)(Glasstime * 1000));
            var93 = monster.getMap().getAllCharactersThreadsafe().iterator();

            while(var93.hasNext()) {
               mapleCharacter = (MapleCharacter)var93.next();

               for(percent = 0; percent < mapleCharacter.getDeathCounts().length; ++percent) {
                  if (mapleCharacter.getDeathCounts()[percent] == 0) {
                     mapleCharacter.getDeathCounts()[percent] = 2;
                  }
               }

               mapleCharacter.getClient().getSession().writeAndFlush(CField.JinHillah(3, mapleCharacter, monster.getMap()));
               mapleCharacter.getMap().broadcastMessage(CField.JinHillah(10, mapleCharacter, monster.getMap()));
            }

            int sandCount = 0;

            for(var93 = monster.getMap().getAllCharactersThreadsafe().iterator(); var93.hasNext(); sandCount += mapleCharacter.liveCounts()) {
               mapleCharacter = (MapleCharacter)var93.next();
            }

            monster.getMap().setCandles((int)Math.round((double)sandCount * 0.5D));
            monster.getMap().broadcastMessage(CField.JinHillah(0, player, monster.getMap()));
            monster.getMap().setLightCandles(0);
            monster.getMap().broadcastMessage(CField.JinHillah(1, player, monster.getMap()));
            monster.getMap().broadcastMessage(CField.JinHillah(8, player, monster.getMap()));
            break;
         case 248:
            MapleMist mapleMist1 = new MapleMist(new Rectangle(Randomizer.rand(-400, 300), Randomizer.rand(-400, 100), 400, 400), monster, this, 8000);
            if (mapleMist1 != null) {
               monster.getMap().spawnMist(mapleMist1, false);
            }
            break;
         case 249:
            switch(this.skillLevel) {
            case 1:
               diseases.put(SecondaryStat.CurseOfCreation, new Pair(4, 6000));
               break;
            case 2:
               diseases.put(SecondaryStat.CurseOfDestruction, new Pair(10, 6000));
            }

            if (player.hasDisease(SecondaryStat.CurseOfCreation) && this.skillLevel == 2 || player.hasDisease(SecondaryStat.CurseOfDestruction) && this.skillLevel == 1) {
               player.setDeathCount((byte)(player.getDeathCount() - 1));
               player.getClient().getSession().writeAndFlush(CField.BlackMageDeathCountEffect());
               player.getClient().getSession().writeAndFlush(CField.getDeathCount(player.getDeathCount()));
               player.dispelDebuffs();
               if (player.getDeathCount() > 0) {
                  player.addHP(-player.getStat().getCurrentMaxHp() * 30L / 100L);
                  if (player.isAlive()) {
                     MobSkillFactory.getMobSkill(120, 39).applyEffect(player, monster, skill, isFacingLeft);
                  }
               } else {
                  player.addHP(-player.getStat().getCurrentMaxHp());
               }
            }
            break;
         case 260:
            switch(this.skillLevel) {
            case 1:
            case 3:
               int n = Randomizer.rand(2, 4);
               boolean bool1 = Randomizer.nextBoolean();

               for(int i1 = 0; i1 < n; ++i1) {
                  i6 = Randomizer.rand(-782, 772);
                  xdistance = Randomizer.rand(800, 820);
                  percent = bool1 ? i6 + xdistance : i6 - xdistance;
                  ypos = percent - i6 - 29;
                  if (ypos > 0) {
                     ypos *= -1;
                  }

                  Obstacle ob = new Obstacle(bool1 ? 81 : 82, new Point(percent, ypos), new Point(i6, 29), 110, 30, 0, 5, 2, 260, 1);
                  ob.setVperSec(1000);
                  monster.getMap().CreateObstacle2((MapleMonster)null, ob, (byte)5);
                  player.getClient().getSession().writeAndFlush(MobPacket.TeleportMonster(monster, true, 9, monster.getTruePosition()));
               }
            case 2:
            case 4:
            default:
               break label1871;
            case 5:
               List<Obstacle> obs = new ArrayList();
               int size_ = 11;
               int i3 = -1050;
               int y = false;

               for(i5 = 0; i5 < size_; ++i5) {
                  int i6 = 83;
                  i3 += Randomizer.rand(200, 280);
                  int y = Randomizer.rand(80, 150);
                  y += 80;
                  Obstacle ob = new Obstacle(i6, new Point(i3, -y), new Point(i3, 305), 80, 40, 0, Randomizer.rand(2, 3), Randomizer.rand(2, 3), y + 305, 0);
                  obs.add(ob);
               }

               monster.getMap().CreateObstacle(monster, obs);
               break label1871;
            }
         case 262:
            switch(this.skillLevel) {
            case 1:
               if (player.getSkillCustomValue(26201) == null) {
                  player.setSkillCustomInfo(26201, 0L, 1000L);
                  player.getClient().getSession().writeAndFlush(SLFCGPacket.MakeBlind(1, 255, 240, 240, 240, 400, 0));
                  Timer.BuffTimer.getInstance().schedule(() -> {
                     player.getClient().getSession().writeAndFlush(SLFCGPacket.MakeBlind(0, 0, 0, 0, 0, 300, 0));
                  }, 1000L);
               }
            default:
               break label1871;
            }
         case 263:
            monster.getMap().broadcastMessage(MobPacket.BossSeren.SerenSpawnOtherMist(monster.getObjectId(), isFacingLeft, monster.getPosition()));
            break;
         case 264:
            monster.getMap().broadcastMessage(MobPacket.BossSeren.SerenMobLazer(monster, this.skillLevel, this.skillLevel == 1 ? 1800 : 1500));
            break;
         case 265:
            monster.setCustomInfo(monster.getId(), 25, 0);
            monster.getMap().broadcastMessage(MobPacket.SpeakingMonster(monster, 4, 0));
            monster.getMap().broadcastMessage(MobPacket.HillaDrainStart(monster.getObjectId()));

            for(int k = 0; k < 8; ++k) {
               i6 = monster.getPosition().x + Randomizer.rand(-500, 500);
               if (monster.getMap().getLeft() > i6) {
                  i6 = monster.getMap().getLeft() + 50;
               } else if (monster.getMap().getRight() < i6) {
                  i6 = monster.getMap().getRight() - 50;
               }

               monster.getMap().spawnMonsterWithEffect(MapleLifeFactory.getMonster(8870106), 43, new Point(i6, monster.getPosition().y));
            }
         }

         if (stats.size() > 0 && monster != null) {
            if (player != null && this.lt != null && this.rb != null && skill) {
               monster.applyMonsterBuff(player.getMap(), stats, this);
               var93 = this.getObjectsInRange(monster, MapleMapObjectType.MONSTER).iterator();

               while(var93.hasNext()) {
                  reactor1l = (MapleMapObject)var93.next();
                  if (reactor1l.getObjectId() != monster.getObjectId()) {
                     ((MapleMonster)reactor1l).applyMonsterBuff(player.getMap(), stats, this);
                  }
               }
            } else {
               monster.applyMonsterBuff(monster.getMap(), stats, this);
            }
         }

         if (diseases.size() > 0 && player != null) {
            if (allchr) {
               var93 = monster.getMap().getAllChracater().iterator();

               while(var93.hasNext()) {
                  mapleCharacter = (MapleCharacter)var93.next();
                  mapleCharacter.giveDebuff((Map)diseases, this);
               }
            } else if (this.lt != null && this.rb != null && skill && monster != null) {
               for(var93 = this.getPlayersInRange(monster, player).iterator(); var93.hasNext(); mapleCharacter.giveDebuff((Map)diseases, this)) {
                  mapleCharacter = (MapleCharacter)var93.next();
                  if (!cancels.isEmpty()) {
                     var96 = cancels.iterator();

                     while(var96.hasNext()) {
                        SecondaryStat cancel = (SecondaryStat)var96.next();
                        if (mapleCharacter.hasDisease(cancel)) {
                           mapleCharacter.cancelDisease(cancel);
                        }
                     }
                  }
               }
            } else {
               player.giveDebuff((Map)diseases, this);
            }
         }

         if (monster != null) {
            monster.setMp(monster.getMp() - this.getMpCon());
         }
      } catch (Exception var86) {
         var86.printStackTrace();
      }

   }

   public int getSkillId() {
      return this.skillId;
   }

   public int getSkillLevel() {
      return this.skillLevel;
   }

   public int getMpCon() {
      return this.mpCon;
   }

   public List<Integer> getSummons() {
      return Collections.unmodifiableList(this.toSummon);
   }

   public int getSpawnEffect() {
      return this.spawnEffect;
   }

   public int getHP() {
      return this.hp;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public Point getLt() {
      return this.lt;
   }

   public Point getRb() {
      return this.rb;
   }

   public int getLimit() {
      return this.limit;
   }

   public boolean makeChanceResult() {
      return (double)this.prop >= 1.0D || Math.random() < (double)this.prop;
   }

   public Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft) {
      Point mylt;
      Point myrb;
      if (facingLeft) {
         mylt = new Point(this.lt.x + posFrom.x, this.lt.y + posFrom.y);
         myrb = new Point(this.rb.x + posFrom.x, this.rb.y + posFrom.y);
      } else {
         myrb = new Point(this.lt.x * -1 + posFrom.x, this.rb.y + posFrom.y);
         mylt = new Point(this.rb.x * -1 + posFrom.x, this.lt.y + posFrom.y);
      }

      return new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
   }

   private List<MapleCharacter> getPlayersInRange(Point pos, boolean facingleft, MapleCharacter player) {
      Rectangle bounds = this.calculateBoundingBox(pos, facingleft);
      List<MapleCharacter> players = new ArrayList();
      players.add(player);
      return player.getMap().getPlayersInRectAndInList(bounds, players);
   }

   private List<MapleCharacter> getPlayersInRange(MapleMonster monster, MapleCharacter player) {
      Rectangle bounds = this.calculateBoundingBox(monster.getTruePosition(), monster.isFacingLeft());
      List<MapleCharacter> players = new ArrayList();
      players.add(player);
      return monster.getMap().getPlayersInRectAndInList(bounds, players);
   }

   private List<MapleMapObject> getObjectsInRange(MapleMonster monster, MapleMapObjectType objectType) {
      Rectangle bounds = this.calculateBoundingBox(monster.getTruePosition(), monster.isFacingLeft());
      List<MapleMapObjectType> objectTypes = new ArrayList();
      objectTypes.add(objectType);
      return monster.getMap().getMapObjectsInRect(bounds, objectTypes);
   }

   public boolean isOnlyFsm() {
      return this.onlyFsm;
   }

   public void setOnlyFsm(boolean b) {
      this.onlyFsm = b;
   }

   public int getAction() {
      return this.action;
   }

   public void setAction(Integer valueOf) {
      this.action = valueOf;
   }

   public long getInterval() {
      return this.interval;
   }

   public void setInterval(long interval) {
      this.interval = interval;
   }

   public void setSkillAfter(int i) {
      this.skillAfter = i;
   }

   public int getSkillAfter() {
      return this.skillAfter;
   }

   public void setMobSkillDelay(MapleCharacter chr, MapleMonster monster, int skillAfter, short option, boolean isFacingLeft) {
      try {
         if (monster.getCustomValue0(1234567) == 1L) {
            return;
         }

         ArrayList<Rectangle> skillRectInfo = new ArrayList();
         ArrayList list;
         Iterator var8;
         byte option;
         int a;
         int i2;
         label152:
         switch(this.skillId) {
         case 136:
            switch(this.skillLevel) {
            case 26:
               skillAfter = Randomizer.rand(3200, 3600);
               chr.getClient().getSession().writeAndFlush(MobPacket.MobSkillDelay(monster.getObjectId(), this.skillId, this.skillLevel, skillAfter, option, skillRectInfo));
               break;
            default:
               chr.getClient().getSession().writeAndFlush(MobPacket.MobSkillDelay(monster.getObjectId(), this.skillId, this.skillLevel, skillAfter, option, skillRectInfo));
            }
         default:
            chr.getClient().getSession().writeAndFlush(MobPacket.MobSkillDelay(monster.getObjectId(), this.skillId, this.skillLevel, skillAfter, option, skillRectInfo));
            break;
         case 140:
         case 141:
            if (monster.getId() != 8860000 && monster.getId() != 8860005) {
               skillAfter = Randomizer.rand(7000, 8000);
            }

            chr.getClient().getSession().writeAndFlush(MobPacket.MobSkillDelay(monster.getObjectId(), this.skillId, this.skillLevel, skillAfter, option, skillRectInfo));
            break;
         case 145:
            switch(this.skillLevel) {
            case 12:
               if (monster.getId() == 8870000) {
                  chr.getMap().broadcastMessage(MobPacket.SpeakingMonster(monster, monster.getId() == 8870100 ? 3 : 2, 0));
               }
            default:
               chr.getClient().getSession().writeAndFlush(MobPacket.MobSkillDelay(monster.getObjectId(), this.skillId, this.skillLevel, skillAfter, option, skillRectInfo));
               break label152;
            }
         case 213:
            MapleMap map;
            int x;
            label166:
            switch(this.skillLevel) {
            case 10:
               skillRectInfo.add(new Rectangle(-300, -1022, 510, 875));
               monster.getMap().broadcastMessage(CField.enforceMSG("더스크 중심으로 주변의 에너지가 빠른 속도로 빨려 들어갑니다.", 249, 2000));
               Timer.MapTimer.getInstance().schedule(() -> {
                  MobSkill msi = MobSkillFactory.getMobSkill(252, 1);
                  MapleMist DustMist = new MapleMist(new Rectangle(-300, -1022, 510, 875), monster, msi, 5500);
                  DustMist.setPosition(new Point(-45, -157));
                  if (DustMist != null) {
                     monster.getMap().spawnMist(DustMist, true);
                  }

               }, 2220L);
            case 11:
            case 12:
            default:
               break;
            case 13:
               a = Randomizer.nextInt(100);
               skillRectInfo.add(new Rectangle(108, -771, 300, 805));
               if (a < 50) {
                  skillRectInfo.add(new Rectangle(-792, -771, 300, 805));
               } else {
                  skillRectInfo.add(new Rectangle(-192, -771, 300, 805));
               }

               skillRectInfo.add(new Rectangle(-492, -771, 300, 805));
               skillRectInfo.add(new Rectangle(408, -771, 300, 805));
               break;
            case 14:
               map = monster.getMap();
               i2 = 0;

               while(true) {
                  if (i2 >= 5) {
                     break label166;
                  }

                  x = Randomizer.nextInt(map.getRight() - map.getLeft()) + map.getLeft();
                  skillRectInfo.add(new Rectangle(x, -757, 200, 610));
                  ++i2;
               }
            case 15:
               map = monster.getMap();

               for(i2 = 0; i2 < 3; ++i2) {
                  x = Randomizer.nextInt(map.getRight() - map.getLeft()) + map.getLeft();
                  skillRectInfo.add(new Rectangle(x, -907, 360, 760));
               }

               Collections.shuffle(skillRectInfo);
            }

            chr.getClient().getSession().writeAndFlush(MobPacket.MobSkillDelay(monster.getObjectId(), this.skillId, this.skillLevel, skillAfter, option, skillRectInfo));
            break;
         case 215:
            chr.getMap().broadcastMessage(MobPacket.MobSkillDelay(monster.getObjectId(), this.skillId, this.skillLevel, skillAfter, option, skillRectInfo));
            break;
         case 217:
            switch(this.skillLevel) {
            case 6:
            case 13:
            case 14:
            case 16:
               list = new ArrayList();
               var8 = chr.getMap().getNodez().getEnvironments().iterator();

               while(var8.hasNext()) {
                  MapleNodes.Environment env = (MapleNodes.Environment)var8.next();
                  env.setShow(false);
               }

               chr.getMap().broadcastMessage(CField.getUpdateEnvironment(chr.getMap().getNodez().getEnvironments()));

               for(i2 = 0; i2 < 7; ++i2) {
                  if (Randomizer.isSuccess(50) && list.size() < 7) {
                     skillRectInfo.add(new Rectangle(-650 + 180 * i2, -81, 180, 65));
                     list.add(-650 + 180 * i2);
                  }
               }

               if (list.isEmpty()) {
                  i2 = Randomizer.rand(0, 7);
                  skillRectInfo.add(new Rectangle(-650 + 180 * i2, -81, 180, 65));
                  list.add(-650 + 180 * i2);
               }

               monster.setSkillForbid(true);
               monster.setCustomInfo(1234567, 1, 0);
               monster.setNextSkill(0);
               monster.setNextSkillLvl(0);
               chr.getMap().broadcastMessage(MobPacket.MobSkillDelay(monster.getObjectId(), this.skillId, this.skillLevel, skillAfter, option, skillRectInfo));
               i2 = 0;
               ArrayList<MapleEnergySphere> spawn = new ArrayList();

               for(Iterator var10 = list.iterator(); var10.hasNext(); ++i2) {
                  Integer x = (Integer)var10.next();
                  MapleEnergySphere mes = new MapleEnergySphere(true, 1, -500, 100000, 15, 10000, 0, list, i2);
                  mes.setCustomx(x);
                  chr.getMap().addMapObject(mes);
                  spawn.add(mes);
               }

               chr.getMap().spawnEnergySphereListTimer(monster.getObjectId(), this.skillLevel, spawn, 1700);
               Timer.MapTimer.getInstance().schedule(() -> {
                  monster.setSkillForbid(false);
                  monster.setUseSpecialSkill(false);
                  if (monster.getBuff(MonsterStatus.MS_Freeze) == null) {
                     int mistskillid = 0;
                     int mistskilllv = 0;
                     Iterator var4 = monster.getSkills().iterator();

                     while(var4.hasNext()) {
                        MobSkill ms = (MobSkill)var4.next();
                        if (ms.getSkillId() == 131) {
                           mistskillid = ms.getSkillId();
                           mistskilllv = ms.getSkillLevel();
                        }
                     }

                     MapleMist mist;
                     if (mistskillid != 0 && mistskilllv != 0 && (mist = new MapleMist(new Rectangle(-684, -29, 1365, 15), monster, MobSkillFactory.getMobSkill(mistskillid, mistskilllv), (int)MobSkillFactory.getMobSkill(mistskillid, mistskilllv).getDuration())) != null && monster != null && monster.getMap() != null && monster.isAlive()) {
                        monster.getMap().spawnMist(mist, false);
                     }
                  }

               }, (long)Randomizer.rand(4000, 10000));
               Timer.MapTimer.getInstance().schedule(() -> {
                  monster.removeCustomInfo(1234567);
                  Iterator var2 = chr.getMap().getNodez().getEnvironments().iterator();

                  while(var2.hasNext()) {
                     MapleNodes.Environment env = (MapleNodes.Environment)var2.next();
                     env.setShow(false);
                  }

                  chr.getMap().broadcastMessage(CField.getUpdateEnvironment(chr.getMap().getNodez().getEnvironments()));
               }, 12800L);
               break label152;
            case 7:
            case 8:
            case 15:
            case 17:
            case 23:
               chr.getMap().broadcastMessage(MobPacket.MobSkillDelay(monster.getObjectId(), this.skillId, this.skillLevel, skillAfter, option, skillRectInfo));
               Timer.MobTimer.getInstance().schedule(() -> {
                  chr.getMap().spawnEnergySphere(monster.getObjectId(), this.skillLevel, new MapleEnergySphere(monster.getTruePosition().x, 10, 20000, 0, true, true));
               }, 3000L);
            case 9:
            case 10:
            case 11:
            case 12:
            case 18:
            case 19:
            case 20:
            case 22:
            default:
               break label152;
            case 21:
               chr.getMap().broadcastMessage(MobPacket.MobSkillDelay(monster.getObjectId(), this.skillId, this.skillLevel, skillAfter, option, skillRectInfo));
               a = 0;

               while(true) {
                  if (a >= 3) {
                     break label152;
                  }

                  MapleEnergySphere mes = new MapleEnergySphere(0, 10, 30000, 1050, false, false);
                  mes.setX(Randomizer.rand(10, 150));
                  mes.setY(Randomizer.rand(100, 150));
                  chr.getMap().spawnEnergySphere(monster.getObjectId(), this.skillLevel, mes);
                  ++a;
               }
            }
         case 226:
            option = 0;
            skillRectInfo.add(new Rectangle(-592, -1016, this.rb.x - this.lt.x, this.rb.y - this.lt.y));
            skillRectInfo.add(new Rectangle(-79, -1016, this.rb.x - this.lt.x, this.rb.y - this.lt.y));
            skillRectInfo.add(new Rectangle(383, -1016, this.rb.x - this.lt.x, this.rb.y - this.lt.y));
            chr.getMap().broadcastMessage(MobPacket.MobSkillDelay(monster.getObjectId(), this.skillId, this.skillLevel, skillAfter, option, skillRectInfo));
            break;
         case 230:
            option = 0;
            monster.getRectangles().clear();

            for(a = 0; a < 10; ++a) {
               i2 = -659 + 180 * Randomizer.nextInt(7);
               skillRectInfo.add(new Rectangle(i2, -316, 120, 315));
            }

            Collections.shuffle(skillRectInfo);
            a = 10;

            for(var8 = skillRectInfo.iterator(); var8.hasNext(); --a) {
               Rectangle re = (Rectangle)var8.next();
               monster.getRectangles().put(a, re);
            }

            chr.getMap().broadcastMessage(MobPacket.MobSkillDelay(monster.getObjectId(), this.skillId, this.skillLevel, 960, option, skillRectInfo));
            break;
         case 238:
            monster.getMap().getLucidDream().clear();
            list = new ArrayList();
            if (monster.getMap().getId() != 450003920 && monster.getMap().getId() != 450004250 && monster.getMap().getId() != 450004550) {
               list.add(new Point(877, 44));
               list.add(new Point(264, 44));
               list.add(new Point(1610, 44));
            } else {
               list.add(new Point(670, -48));
               list.add(new Point(1168, -143));
               list.add(new Point(1046, -842));
            }

            list.remove(Randomizer.rand(0, 2));
            var8 = list.iterator();

            while(var8.hasNext()) {
               Point p = (Point)var8.next();
               monster.getMap().getLucidDream().add(p);
            }

            monster.getMap().broadcastMessage(MobPacket.BossLucid.SpawnLucidDream(list));
            chr.getClient().getSession().writeAndFlush(MobPacket.MobSkillDelay(monster.getObjectId(), this.skillId, this.skillLevel, skillAfter, option, skillRectInfo));
            break;
         case 241:
            chr.getMap().broadcastMessage(MobPacket.MobSkillDelay(monster.getObjectId(), this.skillId, this.skillLevel, 1, option, skillRectInfo));
         }

         chr.dropMessageGM(6, this.skillId + " / " + this.skillLevel + " 의 딜레이 패킷 보냄 : " + skillAfter);
      } catch (Exception var13) {
         var13.printStackTrace();
      }

   }

   public long getDuration() {
      return this.duration;
   }

   public void setDuration(long duration) {
      this.duration = duration;
   }

   public boolean isOnlyOtherSkill() {
      return this.onlyOtherSkill;
   }

   public void setOnlyOtherSkill(boolean onlyOtherSkill) {
      this.onlyOtherSkill = onlyOtherSkill;
   }

   public int getOtherSkillID() {
      return this.otherSkillID;
   }

   public void setOtherSkillID(int otherSkillID) {
      this.otherSkillID = otherSkillID;
   }

   public int getOtherSkillLev() {
      return this.otherSkillLev;
   }

   public void setOtherSkillLev(int otherSkillLev) {
      this.otherSkillLev = otherSkillLev;
   }

   public long getSkillForbid() {
      return this.skillForbid;
   }

   public void setSkillForbid(long skillForbid) {
      this.skillForbid = skillForbid;
   }

   public int getAfterAttack() {
      return this.afterAttack;
   }

   public void setAfterAttack(int afterAttack) {
      this.afterAttack = afterAttack;
   }

   public int getAfterAttackCount() {
      return this.afterAttackCount;
   }

   public void setAfterAttackCount(int afterAttackCount) {
      this.afterAttackCount = afterAttackCount;
   }

   public int getAfterDead() {
      return this.afterDead;
   }

   public void setAfterDead(int afterDead) {
      this.afterDead = afterDead;
   }

   public boolean isMobGroup() {
      return this.isMobGroup;
   }

   public void setMobGroup(boolean isMobGroup) {
      this.isMobGroup = isMobGroup;
   }

   public int getForce() {
      return this.force;
   }

   public void setForce(int force) {
      this.force = force;
   }

   public void willSpider(MapleCharacter player, MapleMonster monster, int type, boolean solo) {
      List<Pair<Integer, Integer>> spider = new ArrayList();
      if (type == 0) {
         spider.add(new Pair(1200, -480));
         spider.add(new Pair(1200, -80));
         spider.add(new Pair(1200, 320));
         spider.add(new Pair(2800, -320));
         spider.add(new Pair(2800, 80));
         spider.add(new Pair(2800, 480));
         spider.add(new Pair(4400, -550));
         spider.add(new Pair(4400, -150));
         spider.add(new Pair(4400, 250));
         spider.add(new Pair(7000, -470));
         spider.add(new Pair(7000, -70));
         spider.add(new Pair(7000, 330));
         spider.add(new Pair(8600, -320));
         spider.add(new Pair(8600, 80));
         spider.add(new Pair(8600, 480));
         spider.add(new Pair(10200, -150));
         spider.add(new Pair(10200, 250));
         spider.add(new Pair(10200, 650));
      } else {
         spider.add(new Pair(1200, -480));
         spider.add(new Pair(1200, -80));
         spider.add(new Pair(1200, 320));
         spider.add(new Pair(2800, -320));
         spider.add(new Pair(2800, 80));
         spider.add(new Pair(2800, 480));
         spider.add(new Pair(4400, -550));
         spider.add(new Pair(4400, -150));
         spider.add(new Pair(4400, 250));
         spider.add(new Pair(7000, -480));
         spider.add(new Pair(7000, -80));
         spider.add(new Pair(7000, 320));
         spider.add(new Pair(8600, -320));
         spider.add(new Pair(8600, 80));
         spider.add(new Pair(8600, 480));
         spider.add(new Pair(10200, -550));
         spider.add(new Pair(10200, -150));
         spider.add(new Pair(10200, 250));
      }

      if (solo) {
         spider.add(new Pair(12800, -480));
         spider.add(new Pair(12800, -80));
         spider.add(new Pair(12800, 320));
         spider.add(new Pair(14400, -320));
         spider.add(new Pair(14400, 80));
         spider.add(new Pair(14400, 480));
         spider.add(new Pair(16000, -550));
         spider.add(new Pair(16000, -150));
         spider.add(new Pair(16000, 250));
      }

      if (spider.size() > 0) {
         player.getMap().broadcastMessage(MobPacket.BossWill.WillSpiderAttackPaten(monster.getObjectId(), type, spider));
      }

   }
}
