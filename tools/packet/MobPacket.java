package tools.packet;

import client.AvatarLook;
import client.MapleCharacter;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import handling.SendPacketOpcode;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import server.Obstacle;
import server.Randomizer;
import server.SecondaryStatEffect;
import server.field.boss.FieldSkill;
import server.field.boss.demian.FlyingSwordNode;
import server.field.boss.demian.MapleDelayedAttack;
import server.field.boss.demian.MapleFlyingSword;
import server.field.boss.demian.MapleIncinerateObject;
import server.field.boss.dunkel.DunkelEliteBoss;
import server.field.boss.lotus.MapleEnergySphere;
import server.field.boss.lucid.Butterfly;
import server.field.boss.lucid.FairyDust;
import server.field.boss.will.SpiderWeb;
import server.life.Ignition;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.maps.MapleFoothold;
import server.maps.MapleMap;
import server.maps.MapleNodes;
import server.movement.LifeMovementFragment;
import tools.HexTool;
import tools.Pair;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;

public class MobPacket {
   public static byte[] Monster_Attack(MapleMonster monster, boolean afterAction, int skill, int level, int unk) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MONSTER_ATTACK.getValue());
      mplew.writeInt(monster.getObjectId());
      mplew.writeInt(skill);
      mplew.writeInt(level);
      mplew.writeInt(monster.getId());
      mplew.writeInt(unk);
      if (skill == 170) {
         switch(level) {
         case 62:
         case 64:
         case 65:
         case 66:
         case 77:
            mplew.write(afterAction);
            mplew.writeInt(monster.getPosition().x);
            mplew.writeInt(monster.getPosition().y);
         case 73:
            mplew.writeInt(monster.getPosition().x);
            mplew.writeInt(monster.getPosition().y);
         case 63:
         case 67:
         case 68:
         case 69:
         case 70:
         case 71:
         case 72:
         case 74:
         case 75:
         case 76:
         }
      } else if (skill == 253 && level == 1) {
         mplew.writeInt(monster.getPosition().x);
         mplew.writeInt(monster.getPosition().y);
      }

      return mplew.getPacket();
   }

   public static byte[] damageMonster(int oid, long damage, boolean heal) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
      mplew.writeInt(oid);
      mplew.write(heal ? 1 : 0);
      if (heal) {
         mplew.writeInt(damage);
         mplew.writeInt(-1);
      } else {
         mplew.writeLong(damage);
      }

      return mplew.getPacket();
   }

   public static byte[] damageFriendlyMob(MapleMonster mob, long damage, boolean display) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
      mplew.writeInt(mob.getObjectId());
      mplew.write(display ? 1 : 2);
      mplew.writeLong(damage);
      mplew.writeLong(mob.getHp());
      mplew.writeLong((long)((int)mob.getMobMaxHp()));
      return mplew.getPacket();
   }

   public static byte[] AfterAttackone(int oid, int type, boolean left) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MONSTER_AFTER_ATTACK_ONE.getValue());
      mplew.writeInt(oid);
      mplew.write(left);
      mplew.write(left);
      return mplew.getPacket();
   }

   public static byte[] setAfterAttack(int objectid, int afterAttack, int attackCount, int attackIdx, boolean left) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SET_MONSTER_AFTER_ATTACK.getValue());
      mplew.writeInt(objectid);
      mplew.writeShort(afterAttack);
      mplew.writeInt(attackCount);
      mplew.writeInt(attackIdx);
      mplew.write(left);
      return mplew.getPacket();
   }

   public static byte[] killMonster(int oid, int animation) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.KILL_MONSTER.getValue());
      mplew.writeInt(oid);
      mplew.write(animation);
      mplew.write((int)0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      if (animation == 9) {
         mplew.writeInt(-1);
      }

      return mplew.getPacket();
   }

   public static byte[] killMonster(int oid, int unk, int animation) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.KILL_MONSTER.getValue());
      mplew.writeInt(oid);
      mplew.write(animation);
      mplew.writeInt(0);
      mplew.writeInt(unk);
      if (animation == 9) {
         mplew.writeInt(-1);
      }

      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static byte[] suckMonster(int oid, int chr) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.KILL_MONSTER.getValue());
      mplew.writeInt(oid);
      mplew.write((int)9);
      mplew.writeInt(0);
      mplew.writeInt(chr);
      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static byte[] healMonster(int oid, long heal) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DAMAGE_MONSTER.getValue());
      mplew.writeInt(oid);
      mplew.write((int)0);
      mplew.writeLong(-heal);
      return mplew.getPacket();
   }

   public static byte[] healEffectMonster(int oid, int skillid, int skilllv) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.HEAL_EFFECT_MONSTER.getValue());
      mplew.writeInt(oid);
      mplew.writeShort(skillid);
      mplew.writeShort(skilllv);
      mplew.writeLong(0L);
      return mplew.getPacket();
   }

   public static byte[] showMonsterHP(int oid, int remhppercentage) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_MONSTER_HP.getValue());
      mplew.writeInt(oid);
      mplew.writeInt(remhppercentage);
      mplew.write((int)1);
      return mplew.getPacket();
   }

   public static byte[] showBossHP(MapleMonster mob) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
      mplew.write((int)6);
      mplew.writeInt(mob.getId());
      mplew.writeLong(mob.isEliteboss() ? mob.getHp() * (long)mob.bonusHp() : mob.getHp() + mob.getBarrier());
      mplew.writeLong(mob.isEliteboss() ? mob.getElitehp() : mob.getStats().getHp() * (long)mob.bonusHp());
      mplew.write(mob.getStats().getTagColor());
      mplew.write(mob.getStats().getTagBgColor());
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] showBossHP(int monsterId, long currentHp, long maxHp) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
      mplew.write((int)6);
      mplew.writeInt(monsterId);
      mplew.writeLong((long)((int)(currentHp <= 0L ? -1L : currentHp)));
      mplew.writeLong((long)((int)maxHp));
      mplew.write((int)6);
      mplew.write((int)5);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] moveMonster(byte bOption, int skill, long targetInfo, int tEncodedGatherDuration, int oid, Point startPos, Point startWobble, List<LifeMovementFragment> moves, List<Point> multiTargetForBall, List<Short> randTimeForAreaAttack, boolean cannotUseSkill) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MOVE_MONSTER.getValue());
      mplew.writeInt(oid);
      mplew.write(bOption);
      mplew.write(skill);
      mplew.writeLong(targetInfo);
      mplew.write(multiTargetForBall.size());

      int i;
      for(i = 0; i < multiTargetForBall.size(); ++i) {
         mplew.writeShort(((Point)multiTargetForBall.get(i)).x);
         mplew.writeShort(((Point)multiTargetForBall.get(i)).y);
      }

      mplew.write(randTimeForAreaAttack.size());

      for(i = 0; i < randTimeForAreaAttack.size(); ++i) {
         mplew.writeShort((Short)randTimeForAreaAttack.get(i));
      }

      mplew.writeInt(0);
      mplew.writeInt(tEncodedGatherDuration);
      mplew.writeInt(0);
      mplew.writePos(startPos);
      mplew.writePos(startWobble);
      PacketHelper.serializeMovementList(mplew, moves);
      mplew.write(cannotUseSkill);
      mplew.writeLong(0L);
      return mplew.getPacket();
   }

   public static byte[] spawnMonster(MapleMonster life, int spawnType, int link) {
      return spawnMonster(life, spawnType, link, 1);
   }

   public static byte[] spawnMonster(MapleMonster life, int spawnType, int link, int control) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER.getValue());
      mplew.write((int)0);
      mplew.writeInt(life.getObjectId());
      mplew.write(control);
      mplew.writeInt(life.getId());
      addMonsterStatus(mplew, life);
      mplew.writeShort(life.getPosition().x);
      mplew.writeShort(life.getPosition().y);
      mplew.write(life.getStance());
      if (life.getId() == 8910000 || life.getId() == 8910100) {
         mplew.write(life.getCustomValue(8910000) != null ? 1 : 0);
      }

      MapleFoothold fh = life.getMap().getFootholds().findBelow(life.getPosition());
      boolean flying = false;
      if (life.getStats() != null && life.getStats().getName() != null) {
         if (life.getStats().getName().contains("자폭")) {
            flying = true;
         }

         String var7 = life.getStats().getName();
         byte var8 = -1;
         switch(var7.hashCode()) {
         case 634903108:
            if (var7.equals("어둠의 집행자")) {
               var8 = 0;
            }
         default:
            switch(var8) {
            case 0:
               flying = true;
            }
         }
      }

      mplew.writeShort(flying ? 0 : (fh != null ? fh.getId() : life.getFh()));
      mplew.writeShort(flying ? 0 : (fh != null ? fh.getId() : life.getFh()));
      mplew.write(spawnType);
      if (spawnType == -3 || spawnType >= 0 && spawnType != 254) {
         mplew.writeInt(link);
      }

      mplew.write((int)255);
      mplew.writeLong(life.getHp());
      mplew.writeInt(0);
      mplew.writeInt(0);
      int per = life.getHPPercent();
      mplew.writeInt(!life.getStats().isMobZone() ? 0 : (per == 25 ? 4 : (per == 50 ? 3 : (per == 75 ? 2 : 1))));
      mplew.writeInt(0);
      mplew.writeInt(-1);
      mplew.writeInt(0);
      mplew.write(life.isFacingLeft());
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(life.getScale());
      mplew.writeInt(life.getEliteGrade());
      if (life.getEliteGrade() >= 0) {
         mplew.writeInt(life.getEliteGradeInfo().size());
         Iterator var13 = life.getEliteGradeInfo().iterator();

         while(var13.hasNext()) {
            Pair<Integer, Integer> info = (Pair)var13.next();
            mplew.writeInt((Integer)info.left);
            mplew.writeInt((Integer)info.right);
         }

         mplew.writeInt(life.getEliteType());
      }

      mplew.write(false);
      mplew.write(false);
      if (life.getId() != 8880101 && life.getId() != 8880111) {
         mplew.writeInt(0);
      } else {
         int size = 0;
         Iterator var15 = life.getMap().getAllMonster().iterator();

         MapleMonster monster;
         while(var15.hasNext()) {
            monster = (MapleMonster)var15.next();
            if (monster.getId() == 8880102) {
               ++size;
            }
         }

         mplew.writeInt(size);
         var15 = life.getMap().getAllMonster().iterator();

         while(var15.hasNext()) {
            monster = (MapleMonster)var15.next();
            if (monster.getId() == 8880102) {
               mplew.writeInt(5);
               mplew.writeInt(monster.getObjectId());
            }
         }
      }

      boolean avatarLook = life.getEliteGrade() >= 0;
      avatarLook = false;
      mplew.write(avatarLook);
      if (avatarLook) {
         AvatarLook a = AvatarLook.makeRandomAvatar();
         a.encodeUnpackAvatarLook(mplew);
      }

      boolean mulug = life.getMap().getId() / 10000 == 92507;
      mplew.writeShort(0);
      mplew.write(mulug);
      if (mulug) {
         mplew.writeInt(5000);
         mplew.write((int)0);
         mplew.writeInt(4);
         mplew.writeShort(0);
         mplew.write((int)0);
         mplew.writeInt(6000);
         mplew.writeZeroBytes(12);
         mplew.writeInt(3);
         mplew.writeZeroBytes(12);
         mplew.write((int)0);
         mplew.writeInt(1);
         mplew.writeInt(3);
         mplew.writeLong(0L);
      } else {
         mplew.writeLong(0L);
         mplew.write((int)0);
      }

      mplew.write(life.getStats().getSkeleton().size());
      Iterator var19 = life.getStats().getSkeleton().iterator();

      while(var19.hasNext()) {
         Triple<String, Integer, Integer> skeleton = (Triple)var19.next();
         mplew.writeMapleAsciiString((String)skeleton.left);
         mplew.write((Integer)skeleton.mid);
         mplew.writeInt((Integer)skeleton.right);
      }

      mplew.write((int)0);
      mplew.write((int)0);
      mplew.write((int)0);
      mplew.writeInt(life.getId() == 8220110 ? 1 : 0);
      if (life.getId() == 8220110) {
         mplew.writeInt(60000);
      }

      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      if (life.getId() == 8880102) {
         mplew.writeInt(0);
      }

      mplew.writeZeroBytes(20);
      return mplew.getPacket();
   }

   public static byte[] spawnMonsterFake(MapleMonster life, int spawnType, int link, int control, List<Pair<Integer, Integer>> list) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER.getValue());
      mplew.write(HexTool.getByteArrayFromHexString("00 73 00 00 00 01 E5 7F 87 00 01 00 80 6C CE 7B 41 00 00 A0 86 01 00 80 B1 4F 01 F0 55 00 00 C0 5D 00 00 2C 01 00 00 2C 01 00 00 0F 27 00 00 EE 02 00 00 00 CA 9A 3B C4 FF FF FF D2 00 00 00 00 75 2B 7D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 CF 02 11 00 05 00 00 00 00 FE FF 00 80 6C CE 7B 41 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 01 00 00 00 00 64 00 00 00 FF FF FF FF "));
      mplew.write(false);
      mplew.write(false);
      Iterator var7;
      if (life.getId() != 8880101 && life.getId() != 8880111) {
         mplew.writeInt(0);
      } else {
         int size = 0;
         var7 = life.getMap().getAllMonster().iterator();

         MapleMonster monster;
         while(var7.hasNext()) {
            monster = (MapleMonster)var7.next();
            if (monster.getId() == 8880102) {
               ++size;
            }
         }

         mplew.writeInt(size);
         var7 = life.getMap().getAllMonster().iterator();

         while(var7.hasNext()) {
            monster = (MapleMonster)var7.next();
            if (monster.getId() == 8880102) {
               mplew.writeInt(5);
               mplew.writeInt(monster.getObjectId());
            }
         }
      }

      boolean avatarLook = life.getEliteGrade() >= 0;
      avatarLook = false;
      mplew.write(avatarLook);
      if (avatarLook) {
         AvatarLook a = AvatarLook.makeRandomAvatar();
         a.encodeUnpackAvatarLook(mplew);
      }

      mplew.writeLong(0L);
      mplew.writeInt(0);
      mplew.write(life.getStats().getSkeleton().size());
      var7 = life.getStats().getSkeleton().iterator();

      while(var7.hasNext()) {
         Triple<String, Integer, Integer> skeleton = (Triple)var7.next();
         mplew.writeMapleAsciiString((String)skeleton.left);
         mplew.write((Integer)skeleton.mid);
         mplew.writeInt((Integer)skeleton.right);
      }

      mplew.write((int)0);
      mplew.write((int)0);
      mplew.write((int)0);
      mplew.writeInt(life.getId() == 8220110 ? 1 : 0);
      if (life.getId() == 8220110) {
         mplew.writeInt(60000);
      }

      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      if (life.getId() == 8880102) {
         mplew.writeInt(0);
      }

      return mplew.getPacket();
   }

   public static void addMonsterStatus(MaplePacketLittleEndianWriter mplew, MapleMonster life) {
      if (life.getStati().size() <= 1) {
      }

      mplew.write(life.getChangedStats() != null ? 1 : 0);
      if (life.getChangedStats() != null) {
         mplew.writeLong(life.getChangedStats().hp);
         mplew.writeInt(life.getChangedStats().mp);
         mplew.writeInt(life.getChangedStats().exp);
         mplew.writeInt(life.getChangedStats().watk);
         mplew.writeInt(life.getChangedStats().matk);
         mplew.writeInt(life.getChangedStats().PDRate);
         mplew.writeInt(life.getChangedStats().MDRate);
         mplew.writeInt(life.getChangedStats().acc);
         mplew.writeInt(life.getChangedStats().eva);
         mplew.writeInt(life.getChangedStats().pushed);
         mplew.writeInt(life.getChangedStats().speed);
         mplew.writeInt(life.getChangedStats().level);
         mplew.writeInt(2100000000);
         mplew.write((int)0);
      }

      if (life.getStati().isEmpty()) {
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
      } else {
         encodeTemporary(mplew, life, life.getStati(), (SecondaryStatEffect)null);
      }

   }

   public static byte[] controlMonster(MapleMonster life, boolean newSpawn, boolean aggro) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
      mplew.write(aggro ? 2 : 1);
      mplew.writeInt(life.getObjectId());
      mplew.write((int)1);
      mplew.writeInt(life.getId());
      addMonsterStatus(mplew, life);
      mplew.writeShort(life.getPosition().x);
      mplew.writeShort(life.getPosition().y);
      mplew.write(life.getStance());
      if (life.getId() == 8910000 || life.getId() == 8910100) {
         mplew.write(life.getCustomValue(8910000) != null ? 1 : 0);
      }

      mplew.writeShort(life.getFh());
      mplew.writeShort(life.getFh());
      mplew.write(newSpawn ? -2 : (life.isFake() ? -4 : -1));
      mplew.write((int)255);
      mplew.writeLong(life.getHp());
      mplew.writeInt(0);
      mplew.writeInt(life.getSeperateSoul());
      int per = life.getHPPercent();
      mplew.writeInt(!life.getStats().isMobZone() ? 0 : (per == 25 ? 4 : (per == 50 ? 3 : (per == 75 ? 2 : 1))));
      mplew.writeInt(0);
      mplew.writeInt(-1);
      mplew.writeInt(0);
      mplew.writeInt(-1);
      mplew.write(life.isFacingLeft());
      mplew.writeInt(0);
      mplew.writeInt(life.getScale());
      mplew.writeInt(life.getEliteGrade());
      if (life.getEliteGrade() >= 0) {
         mplew.writeInt(life.getEliteGradeInfo().size());
         Iterator var5 = life.getEliteGradeInfo().iterator();

         while(var5.hasNext()) {
            Pair<Integer, Integer> info = (Pair)var5.next();
            mplew.writeInt((Integer)info.left);
            mplew.writeInt((Integer)info.right);
         }

         mplew.writeInt(life.getEliteType());
      }

      mplew.write(false);
      mplew.write(false);
      Iterator var10;
      if (life.getId() != 8880101 && life.getId() != 8880111) {
         mplew.writeInt(0);
      } else {
         int size = 0;
         var10 = life.getMap().getAllMonster().iterator();

         MapleMonster monster;
         while(var10.hasNext()) {
            monster = (MapleMonster)var10.next();
            if (monster.getId() == 8880102) {
               ++size;
            }
         }

         mplew.writeInt(size);
         var10 = life.getMap().getAllMonster().iterator();

         while(var10.hasNext()) {
            monster = (MapleMonster)var10.next();
            if (monster.getId() == 8880102) {
               mplew.writeInt(5);
               mplew.writeInt(monster.getObjectId());
            }
         }
      }

      boolean avatarLook = life.getEliteGrade() >= 0;
      avatarLook = false;
      mplew.write(avatarLook);
      if (avatarLook) {
         AvatarLook a = AvatarLook.makeRandomAvatar();
         a.encodeUnpackAvatarLook(mplew);
      }

      mplew.writeLong(0L);
      mplew.writeInt(0);
      mplew.write(life.getStats().getSkeleton().size());
      var10 = life.getStats().getSkeleton().iterator();

      while(var10.hasNext()) {
         Triple<String, Integer, Integer> skeleton = (Triple)var10.next();
         mplew.writeMapleAsciiString((String)skeleton.left);
         mplew.write((Integer)skeleton.mid);
         mplew.writeInt((Integer)skeleton.right);
      }

      mplew.write((int)0);
      mplew.write((int)0);
      mplew.write((int)0);
      mplew.writeInt(life.getId() == 8220110 ? 1 : 0);
      if (life.getId() == 8220110) {
         mplew.writeInt(60000);
      }

      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      if (life.getId() == 8880102) {
         mplew.writeInt(0);
      }

      mplew.writeZeroBytes(10);
      return mplew.getPacket();
   }

   public static byte[] stopControllingMonster(int oid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());
      mplew.write((int)0);
      mplew.writeInt(oid);
      return mplew.getPacket();
   }

   public static byte[] makeMonsterReal(MapleMonster life) {
      return spawnMonster(life, -1, 0);
   }

   public static byte[] makeMonsterFake(MapleMonster life) {
      return spawnMonster(life, -4, 0);
   }

   public static byte[] makeMonsterEffect(MapleMonster life, int effect) {
      return spawnMonster(life, effect, 0);
   }

   public static byte[] moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills, int skillId, int skillLevel, int attackIdx) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MOVE_MONSTER_RESPONSE.getValue());
      mplew.writeInt(objectid);
      mplew.writeShort(moveid);
      mplew.write(useSkills);
      mplew.writeInt(currentMp);
      mplew.writeInt(skillId);
      mplew.writeShort(skillLevel);
      mplew.writeInt(attackIdx);
      mplew.writeInt(0);
      mplew.writeZeroBytes(7);
      return mplew.getPacket();
   }

   public static byte[] applyMonsterStatus(MapleMonster mons, List<Pair<MonsterStatus, MonsterStatusEffect>> mse, boolean fromMonster, SecondaryStatEffect effect) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      List<Pair<MonsterStatus, MonsterStatusEffect>> newstatups = PacketHelper.sortMBuffStats(mse);
      mplew.writeShort(SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
      mplew.writeInt(mons.getObjectId());
      encodeTemporary(mplew, mons, mse, effect);
      mplew.writeShort(0);
      mplew.write(getMse(mse, MonsterStatus.MS_Freeze) ? 2 : 0);
      Iterator var6 = mse.iterator();

      while(var6.hasNext()) {
         Pair<MonsterStatus, MonsterStatusEffect> stat = (Pair)var6.next();
         if (MonsterStatus.IsMovementAffectingStat((MonsterStatus)stat.getLeft())) {
            mplew.write((int)1);
            break;
         }
      }

      mplew.writeZeroBytes(50);
      return mplew.getPacket();
   }

   private static void encodeTemporary(MaplePacketLittleEndianWriter mplew, MapleMonster mob, List<Pair<MonsterStatus, MonsterStatusEffect>> mse, SecondaryStatEffect effect) {
      List<Pair<MonsterStatus, MonsterStatusEffect>> newstatups = PacketHelper.sortMBuffStats(mse);
      List<MonsterStatus> checked = new ArrayList();
      PacketHelper.writeMonsterMaskT(mplew, newstatups);
      Iterator var6 = newstatups.iterator();

      while(true) {
         Pair ms;
         Iterator var8;
         do {
            do {
               if (!var6.hasNext()) {
                  var6 = newstatups.iterator();

                  MonsterStatus mobStat;
                  while(var6.hasNext()) {
                     ms = (Pair)var6.next();
                     mobStat = (MonsterStatus)ms.getLeft();
                     if (!((MonsterStatusEffect)ms.getRight()).isMobskill() && mobStat.getFlag() <= MonsterStatus.MS_PopulatusInvincible.getFlag() && !((MonsterStatus)ms.getLeft()).isStacked() && !checked.contains(ms.getLeft())) {
                        mplew.writeInt(((MonsterStatusEffect)ms.getRight()).getValue());
                        mplew.writeInt(((MonsterStatusEffect)ms.getRight()).getSkill());
                        mplew.writeShort(((MonsterStatusEffect)ms.getRight()).getDuration() / 1000);
                        checked.add((MonsterStatus)ms.getLeft());
                     }
                  }

                  var6 = newstatups.iterator();

                  while(var6.hasNext()) {
                     ms = (Pair)var6.next();
                     mobStat = (MonsterStatus)ms.getLeft();
                     if (((MonsterStatusEffect)ms.getRight()).isMobskill() && mobStat.getFlag() <= MonsterStatus.MS_PopulatusInvincible.getFlag() && !((MonsterStatus)ms.getLeft()).isStacked() && !checked.contains(ms.getLeft())) {
                        mplew.writeInt(((MonsterStatusEffect)ms.getRight()).getValue());
                        mplew.writeShort(((MonsterStatusEffect)ms.getRight()).getSkill());
                        mplew.writeShort(((MonsterStatusEffect)ms.getRight()).getLevel());
                        if (((MonsterStatus)ms.getLeft()).getFlag() == MonsterStatus.MS_PopulatusTimer.getFlag()) {
                           mplew.writeShort(120);
                           mplew.writeInt(mob.getCustomTime(2412) != 0 ? mob.getCustomTime(2412) : ((MonsterStatusEffect)ms.getRight()).getDuration());
                        } else {
                           mplew.writeShort(((MonsterStatusEffect)ms.getRight()).getDuration() / 1000);
                        }

                        checked.add((MonsterStatus)ms.getLeft());
                     }
                  }

                  if (getMse(mse, MonsterStatus.MS_Pdr)) {
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_Mdr)) {
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_Speed)) {
                     mplew.write(Math.min(5, mob.getFreezingOverlap()));
                  }

                  if (getMse(mse, MonsterStatus.MS_Freeze)) {
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_PCounter)) {
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_MCounter)) {
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_PCounter) || getMse(mse, MonsterStatus.MS_MCounter)) {
                     mplew.writeInt(0);
                     mplew.write((int)0);
                     mplew.writeInt(1);
                  }

                  if (getMse(mse, MonsterStatus.MS_Puriaus)) {
                     mplew.writeInt(mob.getBuff(MonsterStatus.MS_Puriaus).getChr().getId());
                     mplew.writeInt(0);
                     mplew.writeInt(0);
                  }

                  MonsterStatusEffect ms;
                  Iterator var11;
                  Pair stat;
                  if (getMse(mse, MonsterStatus.MS_AdddamParty)) {
                     ms = null;
                     var11 = mse.iterator();

                     while(var11.hasNext()) {
                        stat = (Pair)var11.next();
                        if (stat.getLeft() == MonsterStatus.MS_AdddamParty) {
                           ms = (MonsterStatusEffect)stat.getRight();
                           break;
                        }
                     }

                     mplew.writeInt(ms != null ? (ms.getChr() != null ? ms.getChr().getId() : 0) : 0);
                     mplew.writeInt(ms != null ? (ms.getChr() != null ? (ms.getChr().getParty() == null ? 0 : ms.getChr().getParty().getId()) : 0) : 0);
                     mplew.writeInt(effect == null ? 0 : effect.getX());
                  }

                  if (getMse(mse, MonsterStatus.MS_Lifting)) {
                     ms = null;
                     var11 = mse.iterator();

                     while(var11.hasNext()) {
                        stat = (Pair)var11.next();
                        if (stat.getLeft() == MonsterStatus.MS_Lifting) {
                           ms = (MonsterStatusEffect)stat.getRight();
                           break;
                        }
                     }

                     mplew.writeInt(ms != null ? (ms.getChr() != null ? ms.getChr().getId() : 0) : 0);
                     mplew.writeInt(ms != null ? (ms.getChr() != null ? (ms.getChr().getParty() == null ? 0 : ms.getChr().getParty().getId()) : 0) : 0);
                     mplew.writeInt(effect == null ? 0 : effect.getX());
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_FixdamRBuff)) {
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_BossPropPlus)) {
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_ElementDarkness)) {
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_DeadlyCharge)) {
                     mplew.writeInt(0);
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_Incizing)) {
                     mplew.writeInt(mob.getBuff(MonsterStatus.MS_Incizing).getChr().getId());
                     mplew.writeInt(10);
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_BMageDebuff)) {
                     mplew.writeInt(mob.getBuff(MonsterStatus.MS_BMageDebuff).getChr().getId());
                  }

                  if (getMse(mse, MonsterStatus.MS_PvPHelenaMark)) {
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_MultiPMDR)) {
                     mplew.writeInt(mob.getBuff(MonsterStatus.MS_MultiPMDR).getValue() / 10L);
                  }

                  if (getMse(mse, MonsterStatus.MS_UnkFlameWizard)) {
                     ms = null;
                     var11 = mse.iterator();

                     while(var11.hasNext()) {
                        stat = (Pair)var11.next();
                        if (stat.getLeft() == MonsterStatus.MS_UnkFlameWizard) {
                           ms = (MonsterStatusEffect)stat.getRight();
                           break;
                        }
                     }

                     mplew.writeInt(ms != null ? (ms.getChr() != null ? ms.getChr().getId() : 0) : 0);
                     mplew.writeInt(0);
                     mplew.writeInt(100);
                     mplew.writeInt(1);
                  }

                  if (getMse(mse, MonsterStatus.MS_ElementResetBySummon)) {
                     mplew.writeInt(0);
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_DragonStrike)) {
                     mplew.writeInt(mob.getBuff(MonsterStatus.MS_DragonStrike).getChr().getId());
                  }

                  if (getMse(mse, MonsterStatus.MS_CurseMark)) {
                     mplew.writeInt(4);
                     mplew.writeInt(0);
                     mplew.writeInt(152120013);
                  }

                  if (getMse(mse, MonsterStatus.MS_PVPRude_Stack)) {
                     mplew.writeInt(1);
                  }

                  if (getMse(mse, MonsterStatus.MS_PopulatusRing)) {
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_Poison)) {
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_Ambush)) {
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_MultiDamSkill)) {
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_RWChoppingHammer)) {
                     mplew.writeInt(mob.getBuff(MonsterStatus.MS_RWChoppingHammer).getChr().getId());
                  }

                  if (getMse(mse, MonsterStatus.MS_TimeBomb)) {
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_Explosion)) {
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_Burned)) {
                     int count = 0;
                     List<Ignition> a = new ArrayList();
                     var8 = mob.getIgnitions().iterator();

                     Ignition ignition;
                     while(var8.hasNext()) {
                        ignition = (Ignition)var8.next();
                        if (ignition.getDamage() != 0L) {
                           a.add(ignition);
                        }
                     }

                     Collections.sort(a);
                     var8 = mob.getIgnitions().iterator();

                     while(var8.hasNext()) {
                        ignition = (Ignition)var8.next();
                        if (ignition.getDamage() == 0L) {
                           a.add(ignition);
                        }
                     }

                     mplew.write(a.size());

                     for(var8 = a.iterator(); var8.hasNext(); ++count) {
                        ignition = (Ignition)var8.next();
                        mplew.writeInt(ignition.getOwnerId());
                        mplew.writeInt(ignition.getSkill());
                        mplew.writeLong(ignition.getDamage());
                        mplew.writeInt(ignition.getInterval());
                        mplew.writeInt(ignition.getIgnitionKey());
                        mplew.writeInt(10000);
                        mplew.writeInt(ignition.getDuration() / 1000);
                        mplew.writeInt(count);
                        mplew.writeInt(count);
                        mplew.writeInt(0);
                        mplew.writeInt(0);
                        mplew.writeInt(0);
                        mplew.writeInt(300);
                     }
                  }

                  if (getMse(mse, MonsterStatus.MS_BalogDisable)) {
                     mplew.write((int)0);
                     mplew.write((int)0);
                  }

                  if (getMse(mse, MonsterStatus.MS_ExchangeAttack)) {
                     mplew.write((int)1);
                  }

                  if (getMse(mse, MonsterStatus.MS_AddBuffStat)) {
                     ms = null;
                     var11 = mse.iterator();

                     while(var11.hasNext()) {
                        stat = (Pair)var11.next();
                        if (stat.getLeft() == MonsterStatus.MS_AddBuffStat) {
                           ms = (MonsterStatusEffect)stat.getRight();
                           break;
                        }
                     }

                     byte a = (byte)((int)ms.getValue());
                     mplew.write(a);
                     if (a > 0) {
                        mplew.writeInt(0);
                        mplew.writeInt(0);
                        mplew.writeInt(0);
                        mplew.writeInt(0);
                     }
                  }

                  if (getMse(mse, MonsterStatus.MS_LinkTeam)) {
                     mplew.writeMapleAsciiString("");
                  }

                  if (getMse(mse, MonsterStatus.MS_Unk11)) {
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_Unk12)) {
                     mplew.writeLong(0L);
                  }

                  if (getMse(mse, MonsterStatus.MS_Unk17)) {
                     mplew.write((int)0);
                  }

                  if (getMse(mse, MonsterStatus.MS_Unk13)) {
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_Unk14)) {
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_Unk15)) {
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_Unk16)) {
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_SoulExplosion)) {
                     mplew.writeInt(0);
                     mplew.writeInt(0);
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_SeperateSoulP)) {
                     mplew.writeInt(50);
                     mplew.writeInt(mob.getObjectId());
                     mplew.writeShort(17425);
                     mplew.writeInt(43);
                     mplew.writeInt(mob.getBuff(MonsterStatus.MS_SeperateSoulP).getSkill());
                  }

                  if (getMse(mse, MonsterStatus.MS_SeperateSoulC)) {
                     mplew.writeInt(50);
                     mplew.writeInt(mob.getSeperateSoul());
                     mplew.writeShort(17425);
                     mplew.writeInt(43);
                  }

                  if (getMse(mse, MonsterStatus.MS_TrueSight)) {
                     mplew.writeInt(mob.getBuff(MonsterStatus.MS_TrueSight).getValue());
                     mplew.writeInt(mob.getBuff(MonsterStatus.MS_TrueSight).getSkill());
                     mplew.writeInt(0);
                     mplew.writeInt(0);
                     mplew.writeInt(0);
                     mplew.writeInt(0);
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_Laser)) {
                     mplew.writeInt(mob.getId() == 8880201 ? Randomizer.rand(1, 5) : 1);
                     mplew.writeShort(mob.getBuff(MonsterStatus.MS_Laser).getSkill());
                     mplew.writeShort(mob.getBuff(MonsterStatus.MS_Laser).getLevel());
                     mplew.writeInt(1955733479);
                     mplew.writeInt(0);
                     mplew.writeInt(mob.getId() == 8880201 ? Randomizer.rand(1, 300) : 45);
                  }

                  if (getMse(mse, MonsterStatus.MS_Unk10)) {
                     mplew.writeInt(0);
                     mplew.writeInt(0);
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_Unk18)) {
                     mplew.writeInt(0);
                     mplew.writeInt(0);
                  }

                  if (getMse(mse, MonsterStatus.MS_CriticalBind_N)) {
                     mplew.writeInt(-30);
                     mplew.writeInt(-20);
                  }

                  if (getMse(mse, MonsterStatus.MS_IndieUNK)) {
                     mplew.writeInt(mob.getBuff(MonsterStatus.MS_IndieUNK).getChr().getId());
                     mplew.writeInt(mob.getCustomValue0(2321007));
                     mplew.writeInt(30);
                  }

                  return;
               }

               ms = (Pair)var6.next();
            } while(!((MonsterStatus)ms.getLeft()).isStacked());
         } while(checked.contains(ms.getLeft()));

         mplew.writeInt(mob.getIndielist().size());
         var8 = mob.getIndielist().iterator();

         while(var8.hasNext()) {
            MonsterStatusEffect list = (MonsterStatusEffect)var8.next();
            mplew.writeInt(list.getSkill());
            mplew.writeInt(list.getValue());
            mplew.writeInt(list.getStartTime());
            mplew.writeInt(0);
            mplew.writeInt(list.getDuration());
            mplew.writeInt(0);
            mplew.writeInt(0);
         }

         checked.add((MonsterStatus)ms.getLeft());
      }
   }

   public static byte[] cancelMonsterStatus(MapleMonster mons, List<Pair<MonsterStatus, MonsterStatusEffect>> mse) {
      return cancelMonsterStatus(mons, mse, (List)null);
   }

   public static byte[] cancelMonsterStatus(MapleMonster mons, List<Pair<MonsterStatus, MonsterStatusEffect>> mse, List<MonsterStatusEffect> indielist) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CANCEL_MONSTER_STATUS.getValue());
      mplew.writeInt(mons.getObjectId());
      PacketHelper.writeMonsterMask(mplew, mse);
      Iterator var4;
      if (getMse(mse, MonsterStatus.MS_Burned)) {
         mplew.writeInt(0);
         mplew.writeInt(mse.size());
         var4 = mse.iterator();

         while(var4.hasNext()) {
            Pair<MonsterStatus, MonsterStatusEffect> stat = (Pair)var4.next();
            mplew.writeInt(((MonsterStatusEffect)stat.getRight()).getChr().getId());
            mplew.writeInt(((MonsterStatusEffect)stat.getRight()).getSkill());
            mplew.writeInt(0);
         }

         mplew.write((int)5);
      }

      mplew.write((int)0);
      if (indielist != null) {
         var4 = indielist.iterator();

         while(var4.hasNext()) {
            MonsterStatusEffect stats = (MonsterStatusEffect)var4.next();
            mplew.writeInt(1);
            mplew.writeInt(stats.getSkill());
            mplew.writeInt(stats.getValue());
            mplew.writeInt(stats.getStartTime());
            mplew.writeInt(0);
            mplew.writeInt(stats.getDuration());
            mplew.writeInt(0);
            mplew.writeInt(0);
         }
      }

      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] applyStatusAttack(int oid, int skillId, SecondaryStatEffect effect) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeInt(oid);
      mplew.writeInt(skillId);
      mplew.writeShort(effect == null ? 0 : effect.getDotInterval());
      mplew.write((int)1);
      mplew.writeInt(effect == null ? 0 : effect.getDuration() / 1000);
      return mplew.getPacket();
   }

   public static byte[] 서펜트마크(int skillid, int mobid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SERPENT_MARK.getValue());
      mplew.writeInt(skillid);
      mplew.write(false);
      mplew.writeInt(0);
      mplew.writeInt(mobid);
      mplew.writeInt(1);
      mplew.writeInt(1);
      mplew.writeInt(0);
      mplew.writeInt(770);
      return mplew.getPacket();
   }

   public static byte[] talkMonster(int oid, int itemId, String msg) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.TALK_MONSTER.getValue());
      mplew.writeInt(oid);
      mplew.writeInt(500);
      mplew.writeInt(itemId);
      mplew.write(itemId <= 0 ? 0 : 1);
      mplew.write(msg != null && msg.length() > 0 ? 1 : 0);
      if (msg != null && msg.length() > 0) {
         mplew.writeMapleAsciiString(msg);
      }

      mplew.writeInt(1);
      return mplew.getPacket();
   }

   public static byte[] removeTalkMonster(int oid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.REMOVE_TALK_MONSTER.getValue());
      mplew.writeInt(oid);
      return mplew.getPacket();
   }

   public static final byte[] SpeakingMonster(MapleMonster mob, int type, int unk2) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPEAK_MONSTER.getValue());
      mplew.writeInt(mob.getObjectId());
      mplew.writeInt(type);
      mplew.writeInt(unk2);
      return mplew.getPacket();
   }

   public static byte[] showMagnet(int mobid, boolean success) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_MAGNET.getValue());
      mplew.writeInt(mobid);
      mplew.write(success ? 1 : 0);
      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static byte[] catchMonster(int mobid, byte success) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CATCH_MONSTER.getValue());
      mplew.writeInt(mobid);
      mplew.write(success);
      mplew.write(success);
      return mplew.getPacket();
   }

   public static byte[] changePhase(MapleMonster mob) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CHANGE_PHASE.getValue());
      mplew.writeInt(mob.getObjectId());
      mplew.write(mob.getPhase());
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] changeMobZone(MapleMonster mob) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CHANGE_MOBZONE.getValue());
      mplew.writeInt(mob.getObjectId());
      mplew.writeInt(mob.getPhase());
      return mplew.getPacket();
   }

   public static byte[] dropCap() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DROP_STONE.getValue());
      int i = Randomizer.rand(0, 14);
      switch(i) {
      case 0:
         mplew.write(HexTool.getByteArrayFromHexString("09 00 43 61 70 45 66 66 65 63 74 00 00 00 00 09 00 00 00 B0 04 00 00 27 02 00 00 58 02 00 00 27 02 00 00 E8 03 00 00 27 02 00 00 20 03 00 00 27 02 00 00 90 01 00 00 27 02 00 00 40 06 00 00 27 02 00 00 08 07 00 00 27 02 00 00 C8 00 00 00 27 02 00 00 78 05 00 00 27 02 00 00"));
         break;
      case 1:
         mplew.write(HexTool.getByteArrayFromHexString("09 00 43 61 70 45 66 66 65 63 74 02 00 00 00 06 00 00 00 3A 06 00 00 27 02 00 00 AA 04 00 00 27 02 00 00 E2 03 00 00 27 02 00 00 32 FF FF FF 27 02 00 00 8A 01 00 00 27 02 00 00 FA FF FF FF 27 02 00 00"));
         break;
      case 2:
         mplew.write(HexTool.getByteArrayFromHexString("09 00 43 61 70 45 66 66 65 63 74 02 00 00 00 07 00 00 00 31 02 00 00 27 02 00 00 49 FE FF FF 27 02 00 00 F1 FB FF FF 27 02 00 00 D9 FF FF FF 27 02 00 00 81 FD FF FF 27 02 00 00 B9 FC FF FF 27 02 00 00 11 FF FF FF 27 02 00 00"));
         break;
      case 3:
         mplew.write(HexTool.getByteArrayFromHexString("09 00 43 61 70 45 66 66 65 63 74 02 00 00 00 07 00 00 00 BA 00 00 00 27 02 00 00 42 FB FF FF 27 02 00 00 4A 02 00 00 27 02 00 00 D2 FC FF FF 27 02 00 00 9A FD FF FF 27 02 00 00 2A FF FF FF 27 02 00 00 82 01 00 00 27 02 00 00"));
         break;
      case 4:
         mplew.write(HexTool.getByteArrayFromHexString("09 00 43 61 70 45 66 66 65 63 74 00 00 00 00 06 00 00 00 05 FD FF FF 27 02 00 00 95 FE FF FF 27 02 00 00 3D FC FF FF 27 02 00 00 ED 00 00 00 27 02 00 00 CD FD FF FF 27 02 00 00 B5 01 00 00 27 02 00 00"));
         break;
      case 5:
         mplew.write(HexTool.getByteArrayFromHexString("09 00 43 61 70 45 66 66 65 63 74 01 00 00 00 08 00 00 00 1A FC FF FF 27 02 00 00 3A FF FF FF 27 02 00 00 72 FE FF FF 27 02 00 00 52 FB FF FF 27 02 00 00 AA FD FF FF 27 02 00 00 02 00 00 00 27 02 00 00 CA 00 00 00 27 02 00 00 92 01 00 00 27 02 00 00"));
         break;
      case 6:
         mplew.write(HexTool.getByteArrayFromHexString("09 00 43 61 70 45 66 66 65 63 74 01 00 00 00 08 00 00 00 66 FF FF FF 27 02 00 00 46 FC FF FF 27 02 00 00 2E 00 00 00 27 02 00 00 0E FD FF FF 27 02 00 00 7E FB FF FF 27 02 00 00 9E FE FF FF 27 02 00 00 D6 FD FF FF 27 02 00 00 F6 00 00 00 27 02 00 00"));
         break;
      case 7:
         mplew.write(HexTool.getByteArrayFromHexString("09 00 43 61 70 45 66 66 65 63 74 00 00 00 00 08 00 00 00 B6 FE FF FF 27 02 00 00 7E FF FF FF 27 02 00 00 CE FA FF FF 27 02 00 00 EE FD FF FF 27 02 00 00 96 FB FF FF 27 02 00 00 46 00 00 00 27 02 00 00 0E 01 00 00 27 02 00 00 26 FD FF FF 27 02 00 00"));
         break;
      case 8:
         mplew.write(HexTool.getByteArrayFromHexString("09 00 43 61 70 45 66 66 65 63 74 01 00 00 00 09 00 00 00 23 00 00 00 27 02 00 00 93 FE FF FF 27 02 00 00 EB 00 00 00 27 02 00 00 03 FD FF FF 27 02 00 00 3B FC FF FF 27 02 00 00 5B FF FF FF 27 02 00 00 73 FB FF FF 27 02 00 00 B3 01 00 00 27 02 00 00 CB FD FF FF 27 02 00 00"));
         break;
      case 9:
         mplew.write(HexTool.getByteArrayFromHexString("09 00 43 61 70 45 66 66 65 63 74 01 00 00 00 08 00 00 00 E4 FD FF FF 27 02 00 00 8C FB FF FF 27 02 00 00 AC FE FF FF 27 02 00 00 3C 00 00 00 27 02 00 00 CC 01 00 00 27 02 00 00 54 FC FF FF 27 02 00 00 04 01 00 00 27 02 00 00 1C FD FF FF 27 02 00 00"));
         break;
      case 10:
         mplew.write(HexTool.getByteArrayFromHexString("09 00 43 61 70 45 66 66 65 63 74 00 00 00 00 07 00 00 00 5E 02 00 00 27 02 00 00 96 01 00 00 27 02 00 00 CE 00 00 00 27 02 00 00 1E FC FF FF 27 02 00 00 3E FF FF FF 27 02 00 00 76 FE FF FF 27 02 00 00 06 00 00 00 27 02 00 00"));
         break;
      case 11:
         mplew.write(HexTool.getByteArrayFromHexString("09 00 43 61 70 45 66 66 65 63 74 01 00 00 00 09 00 00 00 A8 00 00 00 27 02 00 00 30 FB FF FF 27 02 00 00 E0 FF FF FF 27 02 00 00 18 FF FF FF 27 02 00 00 88 FD FF FF 27 02 00 00 70 01 00 00 27 02 00 00 68 FA FF FF 27 02 00 00 F8 FB FF FF 27 02 00 00 50 FE FF FF 27 02 00 00"));
         break;
      case 12:
         mplew.write(HexTool.getByteArrayFromHexString("09 00 43 61 70 45 66 66 65 63 74 01 00 00 00 07 00 00 00 29 00 00 00 27 02 00 00 B9 01 00 00 27 02 00 00 09 FD FF FF 27 02 00 00 79 FB FF FF 27 02 00 00 61 FF FF FF 27 02 00 00 99 FE FF FF 27 02 00 00 D1 FD FF FF 27 02 00 00"));
         break;
      case 13:
         mplew.write(HexTool.getByteArrayFromHexString("09 00 43 61 70 45 66 66 65 63 74 00 00 00 00 07 00 00 00 8F FF FF FF 27 02 00 00 1F 01 00 00 27 02 00 00 77 03 00 00 27 02 00 00 CF 05 00 00 27 02 00 00 07 05 00 00 27 02 00 00 C7 FE FF FF 27 02 00 00 E7 01 00 00 27 02 00 00"));
         break;
      case 14:
         mplew.write(HexTool.getByteArrayFromHexString("09 00 43 61 70 45 66 66 65 63 74 01 00 00 00 08 00 00 00 8F FF FF FF 27 02 00 00 AF 02 00 00 27 02 00 00 1F 01 00 00 27 02 00 00 CF 05 00 00 27 02 00 00 07 05 00 00 27 02 00 00 57 00 00 00 27 02 00 00 3F 04 00 00 27 02 00 00 E7 01 00 00 27 02 00 00"));
      }

      return mplew.getPacket();
   }

   public static byte[] dropStone(String name, List<Point> data) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DROP_STONE.getValue());
      mplew.writeMapleAsciiString(name);
      mplew.writeInt(0);
      mplew.writeInt(data.size());
      Iterator var3 = data.iterator();

      while(var3.hasNext()) {
         Point a = (Point)var3.next();
         mplew.writeInt(a.x);
         mplew.writeInt(a.y);
      }

      return mplew.getPacket();
   }

   public static byte[] createObstacle(MapleMonster mob, List<Obstacle> obs, byte type) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CREATE_OBSTACLE.getValue());
      mplew.writeInt(0);
      mplew.writeInt(obs.size());
      mplew.write(type);
      if (type == 1) {
         mplew.writeInt(mob.getId());
         mplew.write((int)0);
         mplew.writeInt(mob.getPosition().x);
         mplew.writeInt(((Obstacle)obs.get(0)).getOldPosition().y);
         mplew.writeInt(((Obstacle)obs.get(0)).getHeight());
         mplew.writeInt(0);
      }

      if (type == 4) {
         mplew.writeInt(mob.getId());
         mplew.writeInt(mob.getPosition().x);
         mplew.writeInt(mob.getPosition().y);
         mplew.writeInt(2817);
         mplew.writeInt(0);
      }

      Iterator var4 = obs.iterator();

      while(var4.hasNext()) {
         Obstacle ob = (Obstacle)var4.next();
         mplew.write((int)1);
         mplew.writeInt(ob.getKey());
         mplew.writeInt(ob.getObjectId() == 0 ? Randomizer.nextInt() : ob.getObjectId());
         mplew.writeInt(ob.getOldPosition().x);
         mplew.writeInt(ob.getOldPosition().y);
         mplew.writeInt(ob.getNewPosition().x);
         mplew.writeInt(ob.getNewPosition().y);
         mplew.writeInt(ob.getRangeed());
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.write((int)0);
         mplew.writeInt(ob.getTrueDamage());
         mplew.writeInt(ob.getDelay());
         mplew.writeInt(ob.getHeight());
         mplew.writeInt(ob.getVperSec());
         mplew.writeInt(ob.getMaxP());
         mplew.writeInt(ob.getLength());
         mplew.writeInt(ob.getAngle());
         mplew.writeInt(ob.getUnk());
         if (type == 5) {
            mplew.writeInt(mob.getId());
            mplew.writeInt(mob.getPosition().x);
            mplew.writeInt(ob.getOldPosition().y);
            mplew.writeInt(ob.getHeight());
            mplew.writeInt(0);
         }
      }

      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static byte[] createObstacle2(MapleMonster mob, Obstacle ob, byte type) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CREATE_OBSTACLE2.getValue());
      mplew.write(type);
      mplew.write(ob != null);
      if (ob != null) {
         mplew.writeInt(ob.getKey());
         mplew.writeInt(ob.getObjectId());
         mplew.writeInt(ob.getOldPosition().x);
         mplew.writeInt(ob.getOldPosition().y);
         mplew.writeInt(ob.getNewPosition().x);
         mplew.writeInt(ob.getNewPosition().y);
         mplew.writeInt(ob.getRangeed());
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.write((int)0);
         mplew.writeInt(ob.getTrueDamage());
         mplew.writeInt(ob.getHeight());
         mplew.writeInt(ob.getDelay());
         mplew.writeInt(ob.getVperSec());
         mplew.writeInt(ob.getMaxP());
         mplew.writeInt(ob.getLength());
         mplew.writeInt(ob.getDistancePoints() - 10);
         mplew.writeLong(0L);
         mplew.writeInt(ob.getAngle());
         mplew.writeLong(0L);
         mplew.writeInt(ob.getUnk());
         mplew.write(false);
      }

      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeZeroBytes(10);
      return mplew.getPacket();
   }

   public static byte[] BlockAttack(MapleMonster mob, List<Integer> ids) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.BLOCK_ATTACK.getValue());
      mplew.writeInt(mob.getObjectId());
      mplew.writeInt(ids.size());
      Iterator var3 = ids.iterator();

      while(var3.hasNext()) {
         Integer a = (Integer)var3.next();
         mplew.writeInt(a);
      }

      return mplew.getPacket();
   }

   public static byte[] TeleportMonster(MapleMonster monster_, boolean afterAction, int type, Point point) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.TELEPORT_MONSTER.getValue());
      mplew.writeInt(monster_.getObjectId());
      mplew.write(afterAction);
      if (!afterAction) {
         mplew.writeInt(type);
         switch(type) {
         case 0:
         case 3:
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 14:
         case 16:
            mplew.writeInt(point.x);
            mplew.writeInt(point.y);
         case 1:
         case 2:
         case 13:
         case 15:
         default:
            break;
         case 4:
            mplew.writeInt(0);
         }
      }

      return mplew.getPacket();
   }

   public static byte[] createEnergySphere(int oid, int skillLevel, MapleEnergySphere mes) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ENERGY_SPHERE.getValue());
      mplew.writeInt(oid);
      mplew.writeInt(217);
      mplew.writeInt(skillLevel);
      mplew.write(mes.isOk());
      if (mes.isOk()) {
         mplew.writeInt(mes.getSize());
         mplew.write(mes.isDelayed());
         mplew.writeInt(mes.getY());
         mplew.writeInt(mes.getDensity());
         mplew.writeInt(mes.getFriction());
         mplew.writeInt(mes.getStartDelay());
         mplew.writeInt(mes.getDestroyDelay());

         for(int i = 0; i < mes.getSize(); ++i) {
            mplew.writeInt(mes.getObjectId() + i);
         }
      } else {
         mplew.writeInt(skillLevel == 21 ? -255 : 0);
         mplew.writeInt(skillLevel == 21 ? -255 : (mes.getX() < 0 ? mes.getX() : mes.getX() * -1));
         mplew.writeInt(1);
         mplew.writeInt(mes.getObjectId());
         mplew.writeInt(skillLevel == 21 ? mes.getX() : 25);
         mplew.writeInt(skillLevel == 21 ? mes.getY() : 25);
         mplew.writeInt(skillLevel == 21 ? mes.getRetitution() : 0);
         mplew.writeInt(mes.getRetitution());
         mplew.writeInt(mes.getDestroyDelay());
         mplew.writeInt(mes.getStartDelay());
         mplew.writeInt(0);
         mplew.write(mes.isNoGravity());
         mplew.write(mes.isNoDeleteFromOthers());
         if (skillLevel == 21) {
            mplew.writeInt(15);
            mplew.writeInt(15);
         }

         if (skillLevel == 3 || skillLevel == 4) {
            mplew.writeInt(5);
            mplew.writeInt(5);
         }

         if (mes.isNoDeleteFromOthers()) {
            mplew.writeInt(mes.getRetitution());
            mplew.writeInt(skillLevel == 17 ? 250 : (skillLevel == 15 ? 200 : 300));
            mplew.writeInt(24);
            mplew.writeInt(8);
         }
      }

      return mplew.getPacket();
   }

   public static byte[] createEnergySphere(int oid, int skillLevel, List<MapleEnergySphere> mese) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ENERGY_SPHERE.getValue());
      MapleEnergySphere mes = (MapleEnergySphere)mese.get(0);
      mplew.writeInt(oid);
      mplew.writeInt(217);
      mplew.writeInt(skillLevel);
      mplew.write(mes.isOk());
      if (mes.isOk()) {
         mplew.writeInt(mese.size());
         mplew.write(mes.isDelayed());
         System.out.println("mes.isDelayed() : " + mes.isDelayed());
         mplew.writeInt(mes.getY());
         mplew.writeInt(mes.getDensity());
         System.out.println("mes.getDensity() : " + mes.getDensity());
         mplew.writeInt(mes.getFriction());
         System.out.println("mes.getFriction() : " + mes.getFriction());
         mplew.writeInt(mes.getStartDelay());
         mplew.writeInt(mes.getDestroyDelay());
         System.out.println("mes.getStartDelay() : " + mes.getStartDelay());
         System.out.println("mes.getDestroyDelay() : " + mes.getDestroyDelay());
         Iterator var5 = mese.iterator();

         while(var5.hasNext()) {
            MapleEnergySphere ms = (MapleEnergySphere)var5.next();
            mplew.writeInt(ms.getObjectId());
         }

         System.out.println("타입1");
      } else {
         mplew.writeInt(skillLevel == 21 ? -255 : 0);
         mplew.writeInt(skillLevel == 21 ? -255 : (mes.getX() < 0 ? mes.getX() : mes.getX() * -1));
         mplew.writeInt(1);
         mplew.writeInt(mes.getObjectId());
         mplew.writeInt(skillLevel == 21 ? mes.getX() : 25);
         mplew.writeInt(skillLevel == 21 ? mes.getY() : 25);
         mplew.writeInt(skillLevel == 21 ? mes.getRetitution() : 0);
         mplew.writeInt(mes.getRetitution());
         mplew.writeInt(mes.getDestroyDelay());
         mplew.writeInt(mes.getStartDelay());
         mplew.writeInt(0);
         mplew.write(mes.isNoGravity());
         mplew.write(mes.isNoDeleteFromOthers());
         if (skillLevel == 21) {
            mplew.writeInt(15);
            mplew.writeInt(15);
         }

         if (skillLevel == 3 || skillLevel == 4) {
            mplew.writeInt(5);
            mplew.writeInt(5);
         }

         if (mes.isNoDeleteFromOthers()) {
            mplew.writeInt(mes.getRetitution());
            mplew.writeInt(skillLevel == 17 ? 250 : (skillLevel == 15 ? 200 : 300));
            mplew.writeInt(24);
            mplew.writeInt(8);
         }

         System.out.println("타입2");
      }

      return mplew.getPacket();
   }

   public static byte[] createEnergySpheres(int oid, int skillLevel, MapleEnergySphere mes) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ENERGY_SPHERE.getValue());
      mplew.writeInt(oid);
      mplew.writeInt(217);
      mplew.writeInt(skillLevel);
      mplew.write((int)0);
      mplew.writeInt(-255);
      mplew.writeInt(-255);
      mplew.writeInt(1);
      mplew.writeInt(mes.getObjectId());
      mplew.writeInt(mes.getX());
      mplew.writeInt(mes.getY());
      mplew.writeInt(mes.getRetitution());
      mplew.writeInt(mes.getRetitution());
      mplew.writeInt(mes.getDestroyDelay());
      mplew.writeInt(mes.getStartDelay());
      mplew.writeInt(0);
      mplew.write(mes.isNoGravity());
      mplew.write(mes.isNoDeleteFromOthers());
      mplew.writeInt(15);
      mplew.writeInt(15);
      return mplew.getPacket();
   }

   public static byte[] forcedSkillAction(int objectId, int value, boolean unk) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FORCE_ACTION.getValue());
      mplew.writeInt(objectId);
      mplew.writeInt(value);
      mplew.write(unk);
      return mplew.getPacket();
   }

   public static byte[] StopVanVanBind(int objectId) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.STOP_VANVAN_BIND.getValue());
      mplew.writeInt(objectId);
      return mplew.getPacket();
   }

   public static byte[] MobSkillDelay(int objectId, int skillID, int skillLv, int skillAfter, short sequenceDelay, List<Rectangle> skillRectInfo) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MOBSKILL_DELAY.getValue());
      mplew.writeInt(objectId);
      mplew.writeInt(skillAfter);
      mplew.writeInt(skillID);
      mplew.writeInt(skillLv);
      mplew.writeInt(skillID == 230 ? 900 : 0);
      if (skillRectInfo != null && !skillRectInfo.isEmpty()) {
         mplew.writeInt(sequenceDelay);
         mplew.writeInt(skillRectInfo.size());
         Iterator var7 = skillRectInfo.iterator();

         while(var7.hasNext()) {
            Rectangle rect = (Rectangle)var7.next();
            mplew.writeInt(rect.x);
            mplew.writeInt(rect.y);
            mplew.writeInt(rect.x + rect.width);
            mplew.writeInt(rect.y + rect.height);
         }
      } else {
         mplew.writeInt(0);
         mplew.writeInt(0);
      }

      return mplew.getPacket();
   }

   public static byte[] incinerateObject(MapleIncinerateObject mio, boolean isSpawn) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.STIGMA_INCINERATE_OBJECT.getValue());
      mplew.writeInt(isSpawn ? 0 : 1);
      if (isSpawn) {
         mplew.writeInt(mio.getX());
         mplew.writeInt(mio.getY());
         mplew.writeInt(2500);
         mplew.writeInt(1);
         mplew.writeMapleAsciiString("Map/Obj/BossDemian.img/demian/altar");
         mplew.write((int)0);
      }

      return mplew.getPacket();
   }

   public static byte[] FlyingSword(MapleFlyingSword mfs, boolean isCreate) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FLYING_SWORD_CREATE.getValue());
      mplew.write(isCreate);
      mplew.writeInt(mfs.getObjectId());
      if (isCreate) {
         mplew.write(mfs.getObjectType());
         mplew.write((int)4);
         mplew.writeInt(mfs.getOwner().getId());
         mplew.writeInt(mfs.getOwner().getTruePosition().x);
         mplew.writeInt(mfs.getOwner().getTruePosition().y);
      }

      return mplew.getPacket();
   }

   public static byte[] FlyingSwordNode(MapleFlyingSword mfs) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FLYING_SWORD_NODE.getValue());
      mplew.writeInt(mfs.getObjectId());
      mplew.writeInt(mfs.getTarget() == null ? 0 : mfs.getTarget().getId());
      mplew.write(mfs.isStop());
      mplew.writeInt(mfs.getNodes().size());
      Iterator var2 = mfs.getNodes().iterator();

      while(var2.hasNext()) {
         FlyingSwordNode fsn = (FlyingSwordNode)var2.next();
         mplew.write(fsn.getNodeType());
         mplew.writeShort(fsn.getPathIndex());
         mplew.writeShort(fsn.getNodeIndex());
         mplew.writeShort(fsn.getV());
         mplew.writeInt(fsn.getStartDelay());
         mplew.writeInt(fsn.getEndDelay());
         mplew.writeInt(fsn.getDuration());
         mplew.write(fsn.isHide());
         mplew.write(fsn.getCollisionType());
         mplew.writeInt(fsn.getPos().x);
         mplew.writeInt(fsn.getPos().y);
      }

      return mplew.getPacket();
   }

   public static byte[] FlyingSwordTarget(MapleFlyingSword mfs) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FLYING_SWORD_TARGET.getValue());
      mplew.writeInt(mfs.getObjectId());
      mplew.writeInt(mfs.getTarget().getId());
      return mplew.getPacket();
   }

   public static byte[] FlyingSwordMakeEnterRequest(MapleFlyingSword mfs) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FLYING_SWORD_MAKE_ENTER_REQUEST.getValue());
      mplew.writeInt(mfs.getObjectId());
      mplew.writeInt(mfs.getTarget().getId());
      return mplew.getPacket();
   }

   public static byte[] ZakumAttack(MapleMonster mob, String skeleton) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ZAKUM_ATTACK.getValue());
      mplew.writeInt(mob.getObjectId());
      mplew.writeMapleAsciiString(skeleton);
      return mplew.getPacket();
   }

   public static byte[] onDemianDelayedAttackCreate(int skillId, int skillLevel, MapleDelayedAttack mda) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DEMIAN_ATTACK_CREATE.getValue());
      mplew.writeInt(mda.getOwner().getObjectId());
      mplew.writeInt(skillId);
      mplew.writeInt(skillLevel);
      switch(skillLevel) {
      case 44:
      case 45:
      case 46:
      case 47:
         mplew.write(mda.isIsfacingLeft());
         mplew.writeInt(1);
         mplew.writeInt(mda.getObjectId());
         mplew.writeInt(mda.getPos().x);
         mplew.writeInt(mda.getPos().y);
      default:
         return mplew.getPacket();
      }
   }

   public static byte[] onDemianDelayedAttackCreate(MapleMonster mob, int skillId, int skillLevel, List<MapleDelayedAttack> mda) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DEMIAN_ATTACK_CREATE.getValue());
      mplew.writeInt(mob.getObjectId());
      mplew.writeInt(skillId);
      mplew.writeInt(skillLevel);
      switch(skillLevel) {
      case 42:
         mplew.write(mob.isFacingLeft() ? 1 : 0);
         mplew.writeInt(1);
         mplew.writeInt(mda.size());
         Iterator var5 = mda.iterator();

         while(var5.hasNext()) {
            MapleDelayedAttack att = (MapleDelayedAttack)var5.next();
            mplew.writeInt(att.getObjectId());
            mplew.writeInt(att.getPos().x);
            mplew.writeInt(att.getPos().y);
            mplew.writeInt(att.getAngle());
         }
      default:
         return mplew.getPacket();
      }
   }

   public static byte[] demianRunaway(MapleMonster monster, byte type, MobSkill mobSkill, int duration, boolean suc) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DEMIAN_RUNAWAY.getValue());
      mplew.writeInt(monster.getObjectId());
      mplew.write(type);
      switch(type) {
      case 0:
         mplew.writeInt(mobSkill.getSkillLevel());
         mplew.writeInt(duration);
         mplew.writeShort(0);
         mplew.write((int)1);
         break;
      case 1:
         mplew.write(suc ? 1 : 0);
         mplew.writeInt(suc ? 13 : 30);
         mplew.writeInt(suc ? 6 : 170);
         mplew.writeInt(suc ? 0 : 51);
      }

      return mplew.getPacket();
   }

   public static byte[] enableOnlyFsmAttack(MapleMonster mob, int skill, int unk) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ENABLE_ONLYFSM_ATTACK.getValue());
      mplew.writeInt(mob.getObjectId());
      mplew.writeInt(skill);
      mplew.writeInt(unk);
      return mplew.getPacket();
   }

   public static byte[] ChangePhaseDemian(MapleMonster mob, int unk) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DEMIAN_PHASE_CHANGE.getValue());
      mplew.writeInt(mob.getObjectId());
      mplew.writeInt(unk);
      return mplew.getPacket();
   }

   public static byte[] CorruptionChange(byte phase, int qty) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CORRUPTION_CHANGE.getValue());
      mplew.write(phase);
      mplew.writeInt(qty);
      return mplew.getPacket();
   }

   public static byte[] Lucid3rdPhase(int type) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.LUCID3_PHASE.getValue());
      mplew.writeInt(type);
      return mplew.getPacket();
   }

   public static byte[] jinHillahBlackHand(int objectId, int skillId, int skillLv, List<Triple<Point, Integer, List<Rectangle>>> points) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.JINHILLAH_BLACK_HAND.getValue());
      mplew.writeInt(objectId);
      mplew.writeInt(points.size());
      int i = 0;
      Iterator var6 = points.iterator();

      while(var6.hasNext()) {
         Triple<Point, Integer, List<Rectangle>> point = (Triple)var6.next();
         ++i;
         mplew.writeInt(1);
         mplew.writeInt(i);
         mplew.writeInt(skillId);
         mplew.writeInt(skillLv);
         mplew.write(true);
         mplew.writeInt(1);
         mplew.writePos((Point)point.left);
         mplew.writeInt((Integer)point.mid);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(((List)point.right).size());
         Iterator var8 = ((List)point.right).iterator();

         while(var8.hasNext()) {
            Rectangle rect = (Rectangle)var8.next();
            mplew.writeRect(rect);
         }
      }

      return mplew.getPacket();
   }

   public static byte[] jinHillahSpirit(int objectId, int cid, Rectangle rect, Point pos, int skillLv) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.JINHILLAH_SPIRIT.getValue());
      mplew.writeInt(objectId);
      mplew.writeInt(cid);
      mplew.writeInt(rect.x);
      mplew.writeInt(rect.y);
      mplew.writeInt(rect.width);
      mplew.writeInt(rect.height);
      mplew.writeInt(0);
      mplew.writeInt(Randomizer.rand(0, 210000000));
      mplew.writeInt(246);
      mplew.writeInt(skillLv);
      mplew.write(true);
      mplew.writeInt(1);
      mplew.writePos(new Point(pos.x, -260));
      int z = Randomizer.rand(4, 6);
      mplew.writeInt(250 * z);
      mplew.writeInt(0);
      mplew.writeInt(0);
      List<Rectangle> rectz = new ArrayList();

      for(int j = 0; j < z; ++j) {
         rectz.add(new Rectangle(Randomizer.rand(-100, 150), -80, Randomizer.rand(10, 15) * 5, 640));
      }

      mplew.writeInt(rectz.size());
      Iterator var10 = rectz.iterator();

      while(var10.hasNext()) {
         Rectangle recta = (Rectangle)var10.next();
         mplew.writeRect(recta);
      }

      return mplew.getPacket();
   }

   public static byte[] useFieldSkill(FieldSkill fieldSkill) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FIELD_SKILL.getValue());
      mplew.writeInt(fieldSkill.getSkillId());
      mplew.writeInt(fieldSkill.getSkillLevel());
      Iterator var4;
      switch(fieldSkill.getSkillId()) {
      case 100008:
         mplew.writeInt(fieldSkill.getSummonedSequenceInfoList().size());
         var4 = fieldSkill.getSummonedSequenceInfoList().iterator();

         while(var4.hasNext()) {
            FieldSkill.SummonedSequenceInfo info = (FieldSkill.SummonedSequenceInfo)var4.next();
            mplew.writeInt(info.getPosition().x);
            mplew.writeInt(info.getPosition().y);
         }
      case 100009:
      case 100010:
      case 100012:
      case 100015:
      case 100017:
      case 100018:
      case 100019:
      case 100021:
      case 100022:
      default:
         break;
      case 100011:
         mplew.writeInt(fieldSkill.getLaserInfoList().size());
         var4 = fieldSkill.getLaserInfoList().iterator();

         while(var4.hasNext()) {
            FieldSkill.LaserInfo info = (FieldSkill.LaserInfo)var4.next();
            mplew.writeInt(info.getPosition().x);
            mplew.writeInt(info.getPosition().y);
            mplew.writeInt(info.getUnk1());
            mplew.writeInt(info.getUnk2());
         }

         return mplew.getPacket();
      case 100013:
         mplew.writeInt(fieldSkill.getEnvInfo().size());
         var4 = fieldSkill.getEnvInfo().iterator();

         while(var4.hasNext()) {
            MapleNodes.Environment env = (MapleNodes.Environment)var4.next();
            mplew.writeInt(env.getX());
            mplew.writeInt(env.getY());
            mplew.writeMapleAsciiString(env.getName());
            mplew.writeInt(env.isShow() ? 1 : 0);
         }

         return mplew.getPacket();
      case 100014:
      case 100016:
         mplew.writeInt(fieldSkill.getThunderInfo().size());
         mplew.writeInt(fieldSkill.getSkillId() == 100016 ? 1400 : 2700);
         mplew.write((int)1);
         var4 = fieldSkill.getThunderInfo().iterator();

         while(var4.hasNext()) {
            FieldSkill.ThunderInfo th = (FieldSkill.ThunderInfo)var4.next();
            mplew.writePosInt(th.getStartPosition());
            mplew.writePosInt(th.getEndPosition());
            mplew.writeInt(th.getInfo());
            mplew.writeInt(th.getDelay());
         }
      case 100020:
         mplew.writeInt(0);
         mplew.writeInt(0);
         var4 = fieldSkill.getFootHolds().iterator();

         while(var4.hasNext()) {
            FieldSkill.FieldFootHold fh = (FieldSkill.FieldFootHold)var4.next();
            mplew.write(true);
            mplew.writeInt(fh.getDuration());
            mplew.writeInt(fh.getInterval());
            mplew.writeInt(fh.getAngleMin());
            mplew.writeInt(fh.getAngleMax());
            mplew.writeInt(fh.getAttackDelay());
            mplew.writeInt(fh.getZ() + fh.getSet());
            mplew.writeInt(fh.getZ());
            mplew.writeMapleAsciiString("");
            mplew.writeMapleAsciiString("");
            mplew.writeRect(fh.getRect());
            mplew.writeInt(fh.getPos().x);
            mplew.writeInt(fh.getPos().y);
            mplew.write(fh.isFacingLeft());
         }

         mplew.write(false);
         break;
      case 100023:
         int result = 2;
         mplew.writeInt(8880608);
         mplew.writeInt(1);
         mplew.writeInt(0);
         mplew.writeInt(100);
         mplew.writeInt(80);
         mplew.writeInt(240);
         mplew.writeInt(1530);
         mplew.writeInt(250);
         mplew.writeInt(result);

         for(int i = 0; i < result; ++i) {
            if (i == 0) {
               mplew.writeInt(Randomizer.rand(-864, 10));
               mplew.writeInt(Randomizer.rand(30, 915));
               mplew.writeInt(Randomizer.rand(810, 3420));
            } else {
               mplew.writeInt(Randomizer.rand(300, 915));
               mplew.writeInt(Randomizer.rand(-864, 10));
               mplew.writeInt(Randomizer.rand(810, 3420));
            }
         }

         return mplew.getPacket();
      case 100024:
         mplew.writeInt(7);
         mplew.writeInt(Randomizer.rand(0, 6));
         mplew.writeInt(3060);
         mplew.writeInt(2700);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
      }

      return mplew.getPacket();
   }

   public static byte[] fieldSkillRemove(int skillId, int skillLevel) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FIELD_SKILL_REMOVE.getValue());
      mplew.writeInt(skillId);
      mplew.writeInt(skillLevel);
      return mplew.getPacket();
   }

   public static byte[] mobBarrier(MapleMonster monster) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MONSTER_BARRIER.getValue());
      mplew.writeLong(PacketHelper.getTime(-1L));
      mplew.writeInt(monster.getObjectId());
      mplew.writeInt(monster.getShieldPercent());
      mplew.writeLong(monster.getStats().getHp());
      return mplew.getPacket();
   }

   public static byte[] mobBarrierEffect(int objectId, String eff, String sound, String ui) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MONSTER_BARRIER_EFFECT.getValue());
      mplew.writeInt(objectId);
      mplew.write(true);
      mplew.writeMapleAsciiString(eff);
      mplew.writeInt(1);
      mplew.write(true);
      mplew.writeMapleAsciiString(sound);
      mplew.write(true);
      mplew.writeMapleAsciiString(ui);
      mplew.writeInt(-1);
      mplew.writeShort(0);
      return mplew.getPacket();
   }

   public static byte[] monsterResist(MapleMonster monster, MapleCharacter player, int time, int skill) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MONSTER_RESIST.getValue());
      mplew.writeInt(monster.getObjectId());
      mplew.writeInt(2);
      mplew.writeInt(skill);
      mplew.writeShort(time);
      mplew.writeInt(player.getId());
      mplew.write(false);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] skillAttackEffect(int objectId, int skillId, int ownerId) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SKILL_ATTACK_EFFECT.getValue());
      mplew.writeInt(objectId);
      mplew.writeInt(skillId);
      mplew.writeInt(ownerId);
      mplew.writeInt(0);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] NujukDamage(MapleMonster monster, MapleCharacter chr, long damage, int skillid, int attackcount) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.NUJUK_MONSTER_DAMAGE.getValue());
      mplew.writeInt(monster.getObjectId());
      mplew.write((int)0);
      mplew.writeLong(damage);
      mplew.writeInt(chr.getId());
      mplew.writeInt(skillid);
      mplew.writeInt(1);
      mplew.writeInt(attackcount);
      mplew.writeLong(0L);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] NujukDamage2(MapleMonster monster, int skillid, long damage) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.NUJUK_MONSTER_DAMAGE2.getValue());
      mplew.writeInt(monster.getObjectId());
      mplew.writeInt(skillid);
      mplew.writeInt(damage);
      mplew.writeShort(0);
      return mplew.getPacket();
   }

   public static byte[] deathEffect(int oid, int skillid, int cid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SKILL_ATTACK_EFFECT.getValue());
      mplew.writeInt(oid);
      mplew.writeInt(skillid);
      mplew.writeInt(cid);
      mplew.writeLong(0L);
      return mplew.getPacket();
   }

   public static byte[] monsterForceMove(MapleMonster monster, Point pos) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MONSTER_FORCE_MOVE.getValue());
      mplew.writeInt(monster.getObjectId());
      mplew.writeInt(pos.x);
      mplew.writeInt(pos.y);
      return mplew.getPacket();
   }

   public static byte[] setMonsterProPerties(int oid, int unk0, int unk1, int unk2) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MONSTER_PROPERTIES.getValue());
      mplew.writeInt(oid);
      mplew.writeInt(unk0);
      mplew.writeInt(unk1);
      mplew.writeInt(unk2);
      return mplew.getPacket();
   }

   public static byte[] getSmartNotice(int monsterid, int unk0, int unk1, int unk2, String txt) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SMART_NOTICE.getValue());
      mplew.writeInt(unk0);
      mplew.writeInt(monsterid);
      mplew.writeInt(unk1);
      mplew.writeInt(unk2);
      mplew.writeMapleAsciiString(txt);
      return mplew.getPacket();
   }

   public static byte[] setAttackZakumArm(int oid, int type) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ZAKUM_ARM_ATTACK.getValue());
      mplew.writeInt(oid);
      mplew.writeInt(type);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] HillaDrainStart(int oid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.HILLA_HP_DRAIN_START.getValue());
      mplew.writeInt(oid);
      return mplew.getPacket();
   }

   public static byte[] HillaDrainEffect(int oid, List<Integer> mob) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.HILLA_HP_DRAIN_EFFECT.getValue());
      mplew.writeInt(oid);
      mplew.writeInt(mob.size());
      Iterator var3 = mob.iterator();

      while(var3.hasNext()) {
         Integer mobid = (Integer)var3.next();
         mplew.writeInt(mobid);
      }

      return mplew.getPacket();
   }

   public static byte[] HillaDrainActive(int oid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.HILLA_HP_DRAIN_ACTIVE.getValue());
      mplew.writeInt(oid);
      return mplew.getPacket();
   }

   public static byte[] ShowPierreEffect(MapleCharacter chr, MapleMonster mob) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_PIERRE_EFFECT.getValue());
      mplew.writeInt(chr.getId());
      mplew.writeShort(1);
      mplew.writeInt(mob.getObjectId());
      return mplew.getPacket();
   }

   public static byte[] LaserHandler(int oid, int type, int speed, int left) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.LASER_HANDLE.getValue());
      mplew.writeInt(oid);
      mplew.writeInt(type);
      mplew.writeInt(speed);
      mplew.write(left);
      return mplew.getPacket();
   }

   public static byte[] StigmaImage(MapleCharacter chr, boolean down) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ADD_STIGMA.getValue());
      mplew.writeInt(chr.getId());
      mplew.write(down ? 0 : 1);
      return mplew.getPacket();
   }

   public static byte[] AfterAttack(MapleMonster monster, int skillid, int skilllevel, boolean left, int unk1, int unk2) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DEMIAN_ATTACK_CREATE.getValue());
      mplew.writeInt(monster.getObjectId());
      mplew.writeInt(skillid);
      mplew.writeInt(skilllevel);
      mplew.write(left);
      mplew.writeInt(unk1);
      mplew.writeInt(unk2);
      int x = 0;
      if (skilllevel == 45) {
         x = left ? -680 : 680;
      } else if (skilllevel == 46) {
         x = left ? -482 : 482;
      } else if (skilllevel == 47 || skilllevel == 61) {
         x = left ? -600 : 600;
      }

      mplew.writeInt(monster.getPosition().x + x);
      mplew.writeInt(monster.getPosition().y);
      return mplew.getPacket();
   }

   public static byte[] AfterAttacklist(MapleMonster monster, int skillid, int skilllevel, boolean left, int unk1, List<Point> data) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DEMIAN_ATTACK_CREATE.getValue());
      mplew.writeInt(monster.getObjectId());
      mplew.writeInt(skillid);
      mplew.writeInt(skilllevel);
      mplew.write(left);
      mplew.writeInt(unk1);
      mplew.writeInt(data.size());
      int i = 0;

      for(Iterator var8 = data.iterator(); var8.hasNext(); ++i) {
         Point a = (Point)var8.next();
         mplew.writeInt(4 + i);
         mplew.writeInt(a.x);
         mplew.writeInt(a.y);
         mplew.writeInt(Randomizer.rand(80, 120));
      }

      return mplew.getPacket();
   }

   public static byte[] DemianTranscendenTalSet(MapleMonster demian, MapleMonster TranscendenMob) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DEMIAN_TRANSCENDENTAL_SET.getValue());
      mplew.writeInt(demian.getObjectId());
      mplew.writeInt(5);
      mplew.writeInt(TranscendenMob.getObjectId());
      return mplew.getPacket();
   }

   public static byte[] DemianTranscendenTalSet2(MapleMonster TranscendenMob) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DEMIAN_TRANSCENDENTAL_SET2.getValue());
      mplew.writeInt(TranscendenMob.getObjectId());
      mplew.writeInt(TranscendenMob.getController() != null ? TranscendenMob.getController().getObjectId() : 0);
      return mplew.getPacket();
   }

   public static byte[] ShowBlackMageSkill(int type) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_EFFECT.getValue());
      mplew.writeShort(type == 42570 ? 'Ꙋ' : '녊');
      mplew.writeShort(390);
      mplew.write((int)0);
      mplew.writeInt(2);
      mplew.writeShort(type == 42570 ? 0 : type);
      return mplew.getPacket();
   }

   public static byte[] UseSkill(int objectId, int value) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FORCE_ACTION.getValue());
      mplew.writeInt(objectId);
      mplew.writeInt(value >= 10 ? value - 10 : value);
      mplew.write(value >= 10 ? 1 : 0);
      return mplew.getPacket();
   }

   public static byte[] SpawnFieldSummon(int type, int unk, Point pos) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPAWN_PARTNER.getValue());
      mplew.writeInt(type);
      mplew.writeInt(unk);
      mplew.writePosInt(pos);
      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static byte[] FieldSummonAttack(int type, boolean useskill, Point pos, int objid, List<Pair<Long, Integer>> damageinfo) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPEICAL_SUMMON_ATTACK.getValue());
      mplew.writeInt(type);
      mplew.writeInt(useskill ? 1 : 0);
      mplew.writePosInt(pos);
      mplew.writeInt(objid);
      mplew.writeInt(damageinfo.size());
      int i = 1;

      for(Iterator var7 = damageinfo.iterator(); var7.hasNext(); ++i) {
         Pair<Long, Integer> info = (Pair)var7.next();
         mplew.writeInt(i);
         mplew.writeLong((Long)info.getLeft());
         mplew.writeInt((Integer)info.getRight());
      }

      return mplew.getPacket();
   }

   public static byte[] FieldSummonTeleport(Point pos, boolean left) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPEICAL_SUMMON_TELEPORT.getValue());
      mplew.writeInt(3);
      mplew.writePosInt(pos);
      mplew.write((int)1);
      return mplew.getPacket();
   }

   public static byte[] CreateObstacle2(List<Obstacle> obs) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CREATE_OBSTACLE2.getValue());
      mplew.write((int)5);
      Iterator var2 = obs.iterator();

      while(var2.hasNext()) {
         Obstacle ob = (Obstacle)var2.next();
         mplew.write((int)1);
         mplew.writeInt(ob.getKey());
         mplew.writeInt(Randomizer.nextInt());
         mplew.writeInt(ob.getOldPosition().x);
         mplew.writeInt(ob.getOldPosition().y);
         mplew.writeInt(ob.getNewPosition().x);
         mplew.writeInt(ob.getNewPosition().y);
         mplew.writeInt(ob.getRangeed());
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.write((int)0);
         mplew.writeInt(ob.getTrueDamage());
         mplew.writeInt(ob.getHeight());
         mplew.writeInt(ob.getVperSec());
         mplew.writeInt(ob.getMaxP());
         mplew.writeInt(ob.getLength());
         mplew.writeInt(ob.getAngle());
         mplew.writeInt(ob.getUnk());
         mplew.writeInt(ob.getDelay());
         mplew.writeLong(0L);
      }

      mplew.write((int)0);
      mplew.writeInt(4212352);
      mplew.writeLong(0L);
      return mplew.getPacket();
   }

   public static byte[] CreateObstacle3(List<Obstacle> obs) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CREATE_OBSTACLE.getValue());
      mplew.writeInt(0);
      mplew.writeInt(obs.size());
      mplew.write((int)0);
      Iterator var2 = obs.iterator();

      while(var2.hasNext()) {
         Obstacle ob = (Obstacle)var2.next();
         mplew.write((int)1);
         mplew.writeInt(ob.getKey());
         mplew.writeInt(Randomizer.nextInt());
         mplew.writeInt(ob.getOldPosition().x);
         mplew.writeInt(ob.getOldPosition().y);
         mplew.writeInt(ob.getNewPosition().x);
         mplew.writeInt(ob.getNewPosition().y);
         mplew.writeInt(ob.getRangeed());
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.write((int)0);
         mplew.writeInt(ob.getTrueDamage());
         mplew.writeInt(ob.getHeight());
         mplew.writeInt(ob.getVperSec());
         mplew.writeInt(ob.getMaxP());
         mplew.writeInt(ob.getLength());
         mplew.writeInt(ob.getAngle());
         mplew.writeInt(ob.getUnk());
         mplew.writeInt(ob.getDelay());
      }

      mplew.write((int)0);
      mplew.writeInt(4212352);
      mplew.writeLong(0L);
      return mplew.getPacket();
   }

   public static byte[] enableMulug(MapleMonster mob, int type) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MONSTER_MULUG.getValue());
      mplew.writeInt(mob.getObjectId());
      mplew.writeInt(type);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] enableMulug1(MapleMonster mob, int type) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MONSTER_MULUG1.getValue());
      mplew.writeInt(mob.getObjectId());
      mplew.writeInt(type);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] notDamage(int oid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MONSTER_NOTDAMGE.getValue());
      mplew.writeInt(oid);
      return mplew.getPacket();
   }

   public static byte[] notDamageEffect(int oid, int type) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MONSTER_NOTDAMGE_EFFECT.getValue());
      mplew.writeInt(oid);
      mplew.writeInt(type);
      return mplew.getPacket();
   }

   public static boolean getMse(List<Pair<MonsterStatus, MonsterStatusEffect>> mse, MonsterStatus stat) {
      Iterator var2 = mse.iterator();

      Pair st;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         st = (Pair)var2.next();
      } while(st == null || st.getLeft() != stat);

      return true;
   }

   public static class BossSeren {
      public static byte[] SerenChangeBackground(int code) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SEREN_BACKGROUND_CHANGE.getValue());
         mplew.writeInt(code);
         return mplew.getPacket();
      }

      public static byte[] SerenUserStunGauge(int max, int now) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SEREN_STUN_GAUGE.getValue());
         mplew.writeInt(max);
         mplew.writeInt(now);
         return mplew.getPacket();
      }

      public static byte[] SerenMobLazer(MapleMonster mob, int skilllv, int delaytime) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SEREN_MOB_LASER.getValue());
         mplew.writeInt(mob.getObjectId());
         mplew.writeInt(skilllv);
         mplew.writeInt(delaytime);
         List<Point> pos = new ArrayList();
         int rand;
         if (skilllv == 1) {
            rand = Randomizer.rand(2, 6);
            if (rand == 0) {
               pos.add(new Point(-2511, -780));
               pos.add(new Point(-1300, -2359));
               pos.add(new Point(673, -2618));
               pos.add(new Point(2252, -1407));
               pos.add(new Point(2511, 566));
               pos.add(new Point(1300, 2145));
               pos.add(new Point(-673, 2404));
               pos.add(new Point(-2252, 1193));
            } else if (rand == 1) {
               pos.add(new Point(-2252, -1407));
               pos.add(new Point(-673, -2618));
               pos.add(new Point(1300, -2359));
               pos.add(new Point(2511, -780));
               pos.add(new Point(2252, 1193));
               pos.add(new Point(673, 2404));
               pos.add(new Point(-1300, 2145));
               pos.add(new Point(-2511, 566));
            } else if (rand == 2) {
               pos.add(new Point(-2600, -107));
               pos.add(new Point(-1838, -1945));
               pos.add(new Point(0, -2707));
               pos.add(new Point(1838, -1945));
               pos.add(new Point(2600, -107));
               pos.add(new Point(1838, 1731));
               pos.add(new Point(0, 2493));
               pos.add(new Point(-1838, 1731));
            } else if (rand == 3) {
               pos.add(new Point(-2561, -558));
               pos.add(new Point(-1491, -2237));
               pos.add(new Point(451, -2668));
               pos.add(new Point(2130, -1598));
               pos.add(new Point(2561, 344));
               pos.add(new Point(1491, 2023));
               pos.add(new Point(-451, 2454));
               pos.add(new Point(-2130, 1384));
            } else if (rand == 4) {
               pos.add(new Point(-2130, -1598));
               pos.add(new Point(-451, -2668));
               pos.add(new Point(1491, -2237));
               pos.add(new Point(2561, -558));
               pos.add(new Point(2130, 1384));
               pos.add(new Point(451, 2454));
               pos.add(new Point(-1491, 2023));
               pos.add(new Point(-2561, 344));
            } else if (rand == 5) {
               pos.add(new Point(-1992, -1778));
               pos.add(new Point(-227, -2697));
               pos.add(new Point(1671, -2099));
               pos.add(new Point(2590, -334));
               pos.add(new Point(1992, 1564));
               pos.add(new Point(227, 2483));
               pos.add(new Point(-1671, 1885));
               pos.add(new Point(-2590, 120));
            } else if (rand == 6) {
               pos.add(new Point(-2443, -996));
               pos.add(new Point(-1099, -2463));
               pos.add(new Point(889, -2550));
               pos.add(new Point(2356, -1206));
               pos.add(new Point(2443, 782));
               pos.add(new Point(1099, 2249));
               pos.add(new Point(-889, 2336));
               pos.add(new Point(-2356, 992));
            }
         } else {
            pos.add(new Point(1300, 0));
            pos.add(new Point(1800, -651));
            pos.add(new Point(1300, -1400));
            pos.add(new Point(750, -3500));
            pos.add(new Point(-750, -3500));
            pos.add(new Point(-1900, -1700));
            pos.add(new Point(-1900, -500));
            pos.add(new Point(-800, 200));
            pos.add(new Point(-800, 900));
            pos.add(new Point(2200, 1600));
            pos.add(new Point(-10, 0));
            pos.add(new Point(40, 0));
         }

         mplew.writeInt(pos.size());

         for(rand = 0; rand < pos.size(); ++rand) {
            mplew.writeInt(0);
            mplew.writeInt(-107);
            mplew.writePosInt((Point)pos.get(rand));
         }

         return mplew.getPacket();
      }

      public static byte[] SerenTimer(int value, int... info) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SEREN_TIMER.getValue());
         mplew.writeInt(value);
         switch(value) {
         case 0:
            mplew.writeInt(info[0]);
            mplew.writeInt(info[1]);
            mplew.writeInt(info[2]);
            mplew.writeInt(info[3]);
            mplew.writeInt(info[4]);
            break;
         case 1:
            mplew.writeInt(info[0]);
            mplew.writeInt(info[1]);
            mplew.writeInt(info[2]);
            mplew.writeInt(info[3]);
            mplew.writeInt(info[4]);
            break;
         case 2:
            mplew.write(info[0]);
            break;
         case 3:
            mplew.writeInt(0);
         }

         return mplew.getPacket();
      }

      public static byte[] SerenSpawnOtherMist(int oid, boolean left, Point pos) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SEREN_SPAWN_OTHER_MIST.getValue());
         mplew.write((int)0);
         mplew.writeInt(263);
         mplew.writeInt(1);
         mplew.writeInt(oid);
         mplew.write(left);
         mplew.writePosInt(pos);
         return mplew.getPacket();
      }

      public static byte[] SerenUnk(int max, int now) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SEREN_UNK.getValue());
         mplew.writeInt(max);
         mplew.writeInt(now);
         return mplew.getPacket();
      }

      public static byte[] SerenChangePhase(String str, int type, MapleMonster mob) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SEREN_CHANGE_PHASE.getValue());
         mplew.writeInt(mob.getObjectId());
         mplew.writeMapleAsciiString(str);
         mplew.writeInt(type);
         mplew.writeInt(0);
         mplew.writeInt(1);
         mplew.writeInt(mob.getId());
         return mplew.getPacket();
      }
   }

   public static class BossPapuLatus {
      public static byte[] dropPaPul() {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.CREATE_OBSTACLE.getValue());
         int i = Randomizer.rand(0, 9);
         switch(i) {
         case 0:
            mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 08 00 00 00 00 01 3D 00 00 00 49 46 31 3A F8 01 00 00 0C FE FF FF F8 01 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 B5 02 00 00 00 00 00 00 73 00 00 00 01 00 00 00 A7 02 00 00 00 00 00 00 01 3D 00 00 00 4A 46 31 3A 79 01 00 00 0C FE FF FF 79 01 00 00 F8 FF FF FF 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 D8 02 00 00 00 00 00 00 73 00 00 00 03 00 00 00 EC 01 00 00 00 00 00 00 01 3C 00 00 00 4B 46 31 3A F1 FE FF FF 0C FE FF FF F1 FE FF FF B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 0A 02 00 00 00 00 00 00 66 00 00 00 01 00 00 00 A7 02 00 00 00 00 00 00 01 3C 00 00 00 4C 46 31 3A 56 01 00 00 0C FE FF FF 56 01 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 ED 01 00 00 00 00 00 00 72 00 00 00 02 00 00 00 A7 02 00 00 00 00 00 00 01 3C 00 00 00 4D 46 31 3A F9 FC FF FF 0C FE FF FF F9 FC FF FF B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 40 02 00 00 00 00 00 00 71 00 00 00 02 00 00 00 A7 02 00 00 00 00 00 00 01 3C 00 00 00 4E 46 31 3A C3 00 00 00 0C FE FF FF C3 00 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 D0 02 00 00 00 00 00 00 64 00 00 00 03 00 00 00 A7 02 00 00 00 00 00 00 01 3C 00 00 00 4F 46 31 3A BD FD FF FF 0C FE FF FF BD FD FF FF 0F 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 23 02 00 00 00 00 00 00 73 00 00 00 03 00 00 00 03 02 00 00 00 00 00 00 01 3C 00 00 00 50 46 31 3A F7 FD FF FF 0C FE FF FF F7 FD FF FF B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 94 02 00 00 00 00 00 00 7E 00 00 00 03 00 00 00 A7 02 00 00 00 00 00 00"));
            break;
         case 1:
            mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 06 00 00 00 00 01 3C 00 00 00 F0 5F 31 3A 0A 01 00 00 0C FE FF FF 0A 01 00 00 F8 FF FF FF 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 12 03 00 00 00 00 00 00 7A 00 00 00 02 00 00 00 EC 01 00 00 00 00 00 00 01 3C 00 00 00 F1 5F 31 3A D1 01 00 00 0C FE FF FF D1 01 00 00 1A 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 03 02 00 00 00 00 00 00 7D 00 00 00 02 00 00 00 0E 02 00 00 00 00 00 00 01 3C 00 00 00 F2 5F 31 3A 94 FD FF FF 0C FE FF FF 94 FD FF FF B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 F2 00 00 00 00 00 00 00 6B 00 00 00 01 00 00 00 A7 02 00 00 00 00 00 00 01 3C 00 00 00 F3 5F 31 3A F3 FE FF FF 0C FE FF FF F3 FE FF FF 2E 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 42 02 00 00 00 00 00 00 7F 00 00 00 03 00 00 00 22 02 00 00 00 00 00 00 01 3D 00 00 00 F4 5F 31 3A 32 FD FF FF 0C FE FF FF 32 FD FF FF B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 D6 02 00 00 00 00 00 00 6B 00 00 00 03 00 00 00 A7 02 00 00 00 00 00 00 01 3D 00 00 00 F5 5F 31 3A 56 03 00 00 0C FE FF FF 56 03 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 BB 02 00 00 00 00 00 00 69 00 00 00 03 00 00 00 A7 02 00 00 00 00 00 00"));
            break;
         case 2:
            mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 05 00 00 00 00 01 3C 00 00 00 6B 78 31 3A F1 01 00 00 0C FE FF FF F1 01 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 9E 02 00 00 00 00 00 00 74 00 00 00 01 00 00 00 A7 02 00 00 00 00 00 00 01 3C 00 00 00 6C 78 31 3A 22 01 00 00 0C FE FF FF 22 01 00 00 F8 FF FF FF 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 F9 00 00 00 00 00 00 00 72 00 00 00 02 00 00 00 EC 01 00 00 00 00 00 00 01 3C 00 00 00 6D 78 31 3A 24 FF FF FF 0C FE FF FF 24 FF FF FF B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 69 01 00 00 00 00 00 00 73 00 00 00 02 00 00 00 A7 02 00 00 00 00 00 00 01 3D 00 00 00 6E 78 31 3A 50 00 00 00 0C FE FF FF 50 00 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 58 01 00 00 00 00 00 00 67 00 00 00 03 00 00 00 A7 02 00 00 00 00 00 00 01 3D 00 00 00 6F 78 31 3A CA FC FF FF 0C FE FF FF CA FC FF FF B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 CD 00 00 00 00 00 00 00 6D 00 00 00 02 00 00 00 A7 02 00 00 00 00 00 00"));
            break;
         case 3:
            mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 09 00 00 00 00 01 3D 00 00 00 FA 90 31 3A 3D 00 00 00 0C FE FF FF 3D 00 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 C3 02 00 00 00 00 00 00 6F 00 00 00 01 00 00 00 A7 02 00 00 00 00 00 00 01 3C 00 00 00 FB 90 31 3A B1 02 00 00 0C FE FF FF B1 02 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 AF 01 00 00 00 00 00 00 6E 00 00 00 03 00 00 00 A7 02 00 00 00 00 00 00 01 3D 00 00 00 FC 90 31 3A E2 FE FF FF 0C FE FF FF E2 FE FF FF B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 FD 02 00 00 00 00 00 00 64 00 00 00 02 00 00 00 A7 02 00 00 00 00 00 00 01 3C 00 00 00 FD 90 31 3A AD 01 00 00 0C FE FF FF AD 01 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 54 01 00 00 00 00 00 00 74 00 00 00 02 00 00 00 A7 02 00 00 00 00 00 00 01 3C 00 00 00 FE 90 31 3A 50 00 00 00 0C FE FF FF 50 00 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 2A 01 00 00 00 00 00 00 7C 00 00 00 02 00 00 00 A7 02 00 00 00 00 00 00 01 3D 00 00 00 FF 90 31 3A EA FE FF FF 0C FE FF FF EA FE FF FF B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 44 02 00 00 00 00 00 00 71 00 00 00 02 00 00 00 A7 02 00 00 00 00 00 00 01 3D 00 00 00 00 91 31 3A 0E FF FF FF 0C FE FF FF 0E FF FF FF B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 5B 01 00 00 00 00 00 00 64 00 00 00 01 00 00 00 A7 02 00 00 00 00 00 00 01 3C 00 00 00 01 91 31 3A B3 FE FF FF 0C FE FF FF B3 FE FF FF F5 FF FF FF 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 B7 01 00 00 00 00 00 00 77 00 00 00 01 00 00 00 E9 01 00 00 00 00 00 00 01 3C 00 00 00 02 91 31 3A 36 FF FF FF 0C FE FF FF 36 FF FF FF B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 C5 02 00 00 00 00 00 00 76 00 00 00 01 00 00 00 A7 02 00 00 00 00 00 00"));
            break;
         case 4:
            mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 05 00 00 00 00 01 3C 00 00 00 90 A9 31 3A 74 00 00 00 0C FE FF FF 74 00 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 16 01 00 00 00 00 00 00 69 00 00 00 03 00 00 00 A7 02 00 00 00 00 00 00 01 3D 00 00 00 91 A9 31 3A EE 01 00 00 0C FE FF FF EE 01 00 00 1A 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 EF 01 00 00 00 00 00 00 6E 00 00 00 02 00 00 00 0E 02 00 00 00 00 00 00 01 3D 00 00 00 92 A9 31 3A 91 01 00 00 0C FE FF FF 91 01 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 06 02 00 00 00 00 00 00 77 00 00 00 03 00 00 00 A7 02 00 00 00 00 00 00 01 3D 00 00 00 93 A9 31 3A 4A 02 00 00 0C FE FF FF 4A 02 00 00 1A 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 97 02 00 00 00 00 00 00 78 00 00 00 01 00 00 00 0E 02 00 00 00 00 00 00 01 3C 00 00 00 94 A9 31 3A 5C 01 00 00 0C FE FF FF 5C 01 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 98 01 00 00 00 00 00 00 72 00 00 00 01 00 00 00 A7 02 00 00 00 00 00 00"));
            break;
         case 5:
            mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 07 00 00 00 00 01 3D 00 00 00 B1 95 33 3A 12 FD FF FF 0C FE FF FF 12 FD FF FF B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 0C 02 00 00 00 00 00 00 75 00 00 00 02 00 00 00 A7 02 00 00 00 00 00 00 01 3D 00 00 00 B2 95 33 3A 84 FC FF FF 0C FE FF FF 84 FC FF FF B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 41 01 00 00 00 00 00 00 67 00 00 00 03 00 00 00 A7 02 00 00 00 00 00 00 01 3D 00 00 00 B3 95 33 3A 98 01 00 00 0C FE FF FF 98 01 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 07 02 00 00 00 00 00 00 70 00 00 00 01 00 00 00 A7 02 00 00 00 00 00 00 01 3C 00 00 00 B4 95 33 3A 90 FD FF FF 0C FE FF FF 90 FD FF FF B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 8D 01 00 00 00 00 00 00 6C 00 00 00 02 00 00 00 A7 02 00 00 00 00 00 00 01 3C 00 00 00 B5 95 33 3A 9E FE FF FF 0C FE FF FF 9E FE FF FF B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 8B 02 00 00 00 00 00 00 6A 00 00 00 03 00 00 00 A7 02 00 00 00 00 00 00 01 3C 00 00 00 B6 95 33 3A 75 FD FF FF 0C FE FF FF 75 FD FF FF B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 EF 00 00 00 00 00 00 00 6E 00 00 00 03 00 00 00 A7 02 00 00 00 00 00 00 01 3D 00 00 00 B7 95 33 3A 1A 01 00 00 0C FE FF FF 1A 01 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 6F 02 00 00 00 00 00 00 67 00 00 00 02 00 00 00 A7 02 00 00 00 00 00 00"));
            break;
         case 6:
            mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 06 00 00 00 00 01 3C 00 00 00 98 AF 33 3A 5F FE FF FF 0C FE FF FF 5F FE FF FF B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 13 01 00 00 00 00 00 00 7E 00 00 00 01 00 00 00 A7 02 00 00 00 00 00 00 01 3C 00 00 00 99 AF 33 3A 80 FD FF FF 0C FE FF FF 80 FD FF FF 0F 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 F0 02 00 00 00 00 00 00 79 00 00 00 01 00 00 00 03 02 00 00 00 00 00 00 01 3C 00 00 00 9A AF 33 3A 70 03 00 00 0C FE FF FF 70 03 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 CD 01 00 00 00 00 00 00 82 00 00 00 01 00 00 00 A7 02 00 00 00 00 00 00 01 3C 00 00 00 9B AF 33 3A 70 03 00 00 0C FE FF FF 70 03 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 93 01 00 00 00 00 00 00 82 00 00 00 01 00 00 00 A7 02 00 00 00 00 00 00 01 3C 00 00 00 9C AF 33 3A AA 00 00 00 0C FE FF FF AA 00 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 6D 01 00 00 00 00 00 00 6B 00 00 00 01 00 00 00 A7 02 00 00 00 00 00 00 01 3C 00 00 00 9D AF 33 3A 3A FF FF FF 0C FE FF FF 3A FF FF FF 2E 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 58 02 00 00 00 00 00 00 80 00 00 00 03 00 00 00 22 02 00 00 00 00 00 00"));
            break;
         case 7:
            mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 06 00 00 00 00 01 3D 00 00 00 8D C8 33 3A 1F 02 00 00 0C FE FF FF 1F 02 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 A0 01 00 00 00 00 00 00 6A 00 00 00 01 00 00 00 A7 02 00 00 00 00 00 00 01 3D 00 00 00 8E C8 33 3A 00 03 00 00 0C FE FF FF 00 03 00 00 42 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 0E 01 00 00 00 00 00 00 77 00 00 00 01 00 00 00 36 02 00 00 00 00 00 00 01 3D 00 00 00 8F C8 33 3A DC 03 00 00 0C FE FF FF DC 03 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 EA 00 00 00 00 00 00 00 69 00 00 00 02 00 00 00 A7 02 00 00 00 00 00 00 01 3C 00 00 00 90 C8 33 3A D2 03 00 00 0C FE FF FF D2 03 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 2B 02 00 00 00 00 00 00 6B 00 00 00 02 00 00 00 A7 02 00 00 00 00 00 00 01 3D 00 00 00 91 C8 33 3A C4 FE FF FF 0C FE FF FF C4 FE FF FF F5 FF FF FF 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 07 02 00 00 00 00 00 00 71 00 00 00 02 00 00 00 E9 01 00 00 00 00 00 00 01 3D 00 00 00 92 C8 33 3A 5B 02 00 00 0C FE FF FF 5B 02 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 E2 02 00 00 00 00 00 00 68 00 00 00 02 00 00 00 A7 02 00 00 00 00 00 00"));
            break;
         case 8:
            mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 08 00 00 00 00 01 3D 00 00 00 03 E0 33 3A F2 FD FF FF 0C FE FF FF F2 FD FF FF B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 9C 01 00 00 00 00 00 00 77 00 00 00 03 00 00 00 A7 02 00 00 00 00 00 00 01 3D 00 00 00 04 E0 33 3A 87 02 00 00 0C FE FF FF 87 02 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 26 01 00 00 00 00 00 00 6F 00 00 00 01 00 00 00 A7 02 00 00 00 00 00 00 01 3D 00 00 00 05 E0 33 3A 32 FF FF FF 0C FE FF FF 32 FF FF FF 2E 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 0C 03 00 00 00 00 00 00 77 00 00 00 01 00 00 00 22 02 00 00 00 00 00 00 01 3C 00 00 00 06 E0 33 3A 73 03 00 00 0C FE FF FF 73 03 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 FA 01 00 00 00 00 00 00 6D 00 00 00 02 00 00 00 A7 02 00 00 00 00 00 00 01 3C 00 00 00 07 E0 33 3A C9 02 00 00 0C FE FF FF C9 02 00 00 42 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 D4 02 00 00 00 00 00 00 75 00 00 00 02 00 00 00 36 02 00 00 00 00 00 00 01 3C 00 00 00 08 E0 33 3A BF FD FF FF 0C FE FF FF BF FD FF FF 0F 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 45 02 00 00 00 00 00 00 65 00 00 00 01 00 00 00 03 02 00 00 00 00 00 00 01 3C 00 00 00 09 E0 33 3A BB FC FF FF 0C FE FF FF BB FC FF FF B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 F0 02 00 00 00 00 00 00 74 00 00 00 03 00 00 00 A7 02 00 00 00 00 00 00 01 3C 00 00 00 0A E0 33 3A 0E 01 00 00 0C FE FF FF 0E 01 00 00 F8 FF FF FF 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 E8 00 00 00 00 00 00 00 76 00 00 00 03 00 00 00 EC 01 00 00 00 00 00 00"));
            break;
         case 9:
            mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 0D 00 00 00 00 01 3C 00 00 00 E0 74 AE 35 74 FF FF FF 0C FE FF FF 74 FF FF FF B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 E0 00 00 00 00 00 00 00 78 00 00 00 01 00 00 00 A7 02 00 00 00 00 00 00 01 3C 00 00 00 E1 74 AE 35 7D FE FF FF 0C FE FF FF 7D FE FF FF B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 F2 01 00 00 00 00 00 00 8B 00 00 00 03 00 00 00 A7 02 00 00 00 00 00 00 01 3D 00 00 00 E2 74 AE 35 62 FF FF FF 0C FE FF FF 62 FF FF FF 2E 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 0D 03 00 00 00 00 00 00 77 00 00 00 02 00 00 00 22 02 00 00 00 00 00 00 01 3D 00 00 00 E3 74 AE 35 86 FE FF FF 0C FE FF FF 86 FE FF FF B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 B6 01 00 00 00 00 00 00 79 00 00 00 02 00 00 00 A7 02 00 00 00 00 00 00 01 3D 00 00 00 E4 74 AE 35 5B 02 00 00 0C FE FF FF 5B 02 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 DB 01 00 00 00 00 00 00 72 00 00 00 01 00 00 00 A7 02 00 00 00 00 00 00 01 3D 00 00 00 E5 74 AE 35 CB 00 00 00 0C FE FF FF CB 00 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 AE 01 00 00 00 00 00 00 75 00 00 00 03 00 00 00 A7 02 00 00 00 00 00 00 01 3D 00 00 00 E6 74 AE 35 42 02 00 00 0C FE FF FF 42 02 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 75 02 00 00 00 00 00 00 7B 00 00 00 02 00 00 00 A7 02 00 00 00 00 00 00 01 3C 00 00 00 E7 74 AE 35 36 02 00 00 0C FE FF FF 36 02 00 00 B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 8C 02 00 00 00 00 00 00 95 00 00 00 03 00 00 00 A7 02 00 00 00 00 00 00 01 3C 00 00 00 E8 74 AE 35 B9 FF FF FF 0C FE FF FF B9 FF FF FF B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 0D 01 00 00 00 00 00 00 81 00 00 00 02 00 00 00 A7 02 00 00 00 00 00 00 01 3D 00 00 00 E9 74 AE 35 71 FD FF FF 0C FE FF FF 71 FD FF FF 0F 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 DA 00 00 00 00 00 00 00 71 00 00 00 01 00 00 00 03 02 00 00 00 00 00 00 01 3C 00 00 00 EA 74 AE 35 79 FD FF FF 0C FE FF FF 79 FD FF FF B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 19 02 00 00 00 00 00 00 8D 00 00 00 03 00 00 00 A7 02 00 00 00 00 00 00 01 3C 00 00 00 EB 74 AE 35 AF FF FF FF 0C FE FF FF AF FF FF FF 2E 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 BD 02 00 00 00 00 00 00 7E 00 00 00 01 00 00 00 22 02 00 00 00 00 00 00 01 3D 00 00 00 EC 74 AE 35 9E FC FF FF 0C FE FF FF 9E FC FF FF B3 00 00 00 19 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 AC 02 00 00 00 00 00 00 71 00 00 00 01 00 00 00 A7 02 00 00 00 00 00 00"));
         }

         return mplew.getPacket();
      }

      public static final byte[] PapulLatusTime(int time, boolean patten, boolean del) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.PAPULATUS_HANDLE.getValue());
         mplew.writeInt(4);
         mplew.write(del ? 0 : 1);
         mplew.write(patten ? 1 : 0);
         mplew.writeInt(time + 2000);
         return mplew.getPacket();
      }

      public static final byte[] PapulLatusPincers(boolean first, int chrid, int type, int y) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.PAPULATUS_HANDLE.getValue());
         mplew.writeInt(2);
         if (first) {
            mplew.writeInt(6);

            for(int i = 0; i < 6; ++i) {
               mplew.writeInt(i);
               mplew.writeInt(1);
               mplew.writeLong(0L);
            }
         } else {
            mplew.writeInt(1);
            mplew.writeInt(type);
            mplew.writeInt(chrid > 0 ? 5 : 6);
            mplew.writeInt(y);
            mplew.writeInt(chrid);
         }

         return mplew.getPacket();
      }

      public static final byte[] PapulLatusLaser(boolean del, int type) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.PAPULATUS_HANDLE.getValue());
         if (del) {
            if (type == 1) {
               mplew.writeInt(1);
               mplew.writeInt(2000);
            } else if (type == 2) {
               mplew.writeInt(0);
               mplew.writeInt(0);
            }
         } else {
            switch(type) {
            case 1:
               mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 02 00 00 00 04 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 3E 40 05 00 00 00 01 00 00 00 00 00 C0 72 40 00 00 00 00 00 00 24 C0"));
               break;
            case 2:
               mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 02 00 00 00 02 00 00 00 01 00 00 00 00 00 40 60 40 00 00 00 00 00 00 34 C0 03 00 00 00 01 00 00 00 00 00 40 60 40 00 00 00 00 00 00 3E 40"));
               break;
            case 3:
               mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 02 00 00 00 06 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 59 40 07 00 00 00 01 00 00 00 00 00 C0 72 40 00 00 00 00 00 00 24 C0"));
            }
         }

         mplew.writeZeroBytes(10);
         return mplew.getPacket();
      }

      public static final byte[] PapulLatusTimePatten(int type, int hour, int min, int time, int... args) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.PAPULATUS_SPECIAL_PATTEN.getValue());
         mplew.write(type);
         switch(type) {
         case 0:
            mplew.writeInt(time);
            mplew.writeInt(hour);
            mplew.writeInt(min);
            mplew.writeInt(args.length);

            for(int i = 0; i < args.length; ++i) {
               mplew.writeInt(args[i]);
               mplew.write((int)0);
            }

            return mplew.getPacket();
         case 1:
            mplew.writeInt(hour);
            mplew.writeInt(min);
         }

         return mplew.getPacket();
      }
   }

   public static class BossWill {
      public static byte[] setMoonGauge(int max, int min) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.WILL_SET_MOONGAUGE.getValue());
         mplew.writeInt(max);
         mplew.writeInt(min);
         return mplew.getPacket();
      }

      public static byte[] addMoonGauge(int add) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.WILL_MOONGAUGE.getValue());
         mplew.writeInt(add);
         return mplew.getPacket();
      }

      public static byte[] cooldownMoonGauge(int length) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.WILL_COOLTIME_MOONGAUGE.getValue());
         mplew.writeInt(length);
         return mplew.getPacket();
      }

      public static byte[] createBulletEyes(int... args) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.WILL_CREATE_BULLETEYE.getValue());
         mplew.writeInt(args[0]);
         mplew.writeInt(args[1]);
         mplew.writeInt(args[2]);
         mplew.writeInt(args[3]);
         if (args[0] == 1) {
            mplew.writeInt(1800);
            mplew.writeInt(5);
            mplew.write(true);
            mplew.writeInt(args[4]);
            mplew.writeInt(args[5]);
            mplew.writeInt(args[6]);
            mplew.writeInt(args[7]);
         }

         return mplew.getPacket();
      }

      public static byte[] setWillHp(List<Integer> counts, MapleMap map, int mobId1, int mobId2, int mobId3) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.WILL_SET_HP.getValue());
         mplew.writeInt(counts.size());
         Iterator var6 = counts.iterator();

         while(var6.hasNext()) {
            int i = (Integer)var6.next();
            mplew.writeInt(i);
         }

         MapleMonster life1 = map.getMonsterById(mobId1);
         MapleMonster life2 = map.getMonsterById(mobId2);
         MapleMonster life3 = map.getMonsterById(mobId3);
         mplew.write(life1 != null);
         if (life1 != null) {
            mplew.writeInt(life1.getId());
            mplew.writeLong(life1.getHp());
            mplew.writeLong((long)((double)life1.getMobMaxHp() * life1.bonusHp()));
         }

         mplew.write(life2 != null);
         if (life2 != null) {
            mplew.writeInt(life2.getId());
            mplew.writeLong(life2.getHp());
            mplew.writeLong((long)((double)life2.getMobMaxHp() * life2.bonusHp()));
         }

         mplew.write(life3 != null);
         if (life3 != null) {
            mplew.writeInt(life3.getId());
            mplew.writeLong(life3.getHp());
            mplew.writeLong((long)((double)life3.getMobMaxHp() * life3.bonusHp()));
         }

         return mplew.getPacket();
      }

      public static byte[] setWillHp(List<Integer> counts) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.WILL_SET_HP2.getValue());
         mplew.writeInt(counts.size());
         Iterator var2 = counts.iterator();

         while(var2.hasNext()) {
            int i = (Integer)var2.next();
            mplew.writeInt(i);
         }

         return mplew.getPacket();
      }

      public static byte[] WillSpiderAttack(int id, int skill, int level, int type, List<Triple<Integer, Integer, Integer>> values) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.WILL_SPIDER_ATTACK.getValue());
         mplew.writeInt(id);
         mplew.writeInt(skill);
         mplew.writeInt(level);
         switch(level) {
         case 1:
         case 2:
         case 3:
         case 14:
            if (level == 14) {
               mplew.writeInt(type);
            }

            mplew.writeInt(level == 14 ? 9 : 4);
            mplew.writeInt(1200);
            mplew.writeInt(level == 14 ? 5000 : 9000);
            mplew.writeInt(level == 14 && type == 2 ? -60 : -40);
            mplew.writeInt(-600);
            mplew.writeInt(level == 14 && type == 2 ? 60 : 40);
            mplew.writeInt(10);
            mplew.writeInt(values.size());
            Iterator var6 = values.iterator();

            while(var6.hasNext()) {
               Triple<Integer, Integer, Integer> value = (Triple)var6.next();
               mplew.writeInt((Integer)value.left);
               mplew.writeInt((Integer)value.mid);
               mplew.writeInt((Integer)value.right);
               mplew.writeInt(0);
            }

            return mplew.getPacket();
         case 4:
            mplew.writeInt(type);
            mplew.write(type != 0);
            break;
         case 5:
            mplew.writeInt(2);
            if (type == 0) {
               mplew.write((int)0);
               mplew.writeInt(-690);
               mplew.writeInt(-455);
               mplew.writeInt(695);
               mplew.writeInt(160);
               mplew.write((int)1);
               mplew.writeInt(-690);
               mplew.writeInt(-2378);
               mplew.writeInt(695);
               mplew.writeInt(-2019);
            } else {
               mplew.write((int)0);
               mplew.writeInt(-690);
               mplew.writeInt(-2378);
               mplew.writeInt(695);
               mplew.writeInt(-2019);
               mplew.write((int)1);
               mplew.writeInt(-690);
               mplew.writeInt(-455);
               mplew.writeInt(695);
               mplew.writeInt(160);
            }
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 13:
         }

         return mplew.getPacket();
      }

      public static byte[] WillSpiderAttackPaten(int id, int paten, List<Pair<Integer, Integer>> spider) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.WILL_SPIDER_ATTACK.getValue());
         mplew.writeInt(id);
         mplew.writeInt(242);
         mplew.writeInt(14);
         mplew.writeInt(paten);
         mplew.writeInt(9);
         mplew.writeInt(1200);
         mplew.writeInt(9000);
         mplew.writeInt(-40);
         mplew.writeInt(-600);
         mplew.writeInt(40);
         mplew.writeInt(10);
         mplew.writeInt(spider.size());
         int i = 0;

         for(Iterator var5 = spider.iterator(); var5.hasNext(); ++i) {
            Pair<Integer, Integer> a = (Pair)var5.next();
            mplew.writeInt(i);
            mplew.writeInt((Integer)a.getLeft());
            mplew.writeInt((Integer)a.getRight());
            mplew.writeInt(0);
         }

         return mplew.getPacket();
      }

      public static byte[] willUseSpecial() {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.WILL_USE_SPECIAL.getValue());
         return mplew.getPacket();
      }

      public static byte[] willStun() {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.WILL_STUN.getValue());
         return mplew.getPacket();
      }

      public static byte[] willThirdOne() {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.WILL_THIRD_ONE.getValue());
         mplew.writeInt(0);
         return mplew.getPacket();
      }

      public static byte[] willSpider(boolean make, SpiderWeb web) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.WILL_SPIDER.getValue());
         mplew.writeInt(make ? 3 : 4);
         mplew.writeInt(web.getObjectId());
         mplew.writeInt(web.getPattern());
         mplew.writeInt(web.getX1());
         mplew.writeInt(web.getY1());
         switch(web.getPattern()) {
         case 0:
            mplew.writeInt(100);
            mplew.writeInt(100);
            return mplew.getPacket();
         case 1:
            mplew.writeInt(160);
            mplew.writeInt(160);
            return mplew.getPacket();
         case 2:
            mplew.writeInt(270);
            mplew.writeInt(270);
            return mplew.getPacket();
         default:
            mplew.writeInt(0);
            mplew.writeInt(0);
            return mplew.getPacket();
         }
      }

      public static byte[] teleport() {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.WILL_TELEPORT.getValue());
         mplew.writeInt(1);
         return mplew.getPacket();
      }

      public static byte[] posion(MapleCharacter player, int objid, int type, int x, int y) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.WILL_POISON.getValue());
         mplew.writeInt(objid);
         mplew.writeInt(player.getId());
         mplew.write(type);
         mplew.writeInt(x);
         mplew.writeInt(y);
         return mplew.getPacket();
      }

      public static byte[] AttackPoison(MapleCharacter player, int objid, int type, int x, int y) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.WILL_POISON_ATTACK.getValue());
         mplew.writeInt(objid);
         mplew.writeInt(player.getId());
         mplew.write(type);
         mplew.writeInt(x);
         mplew.writeInt(y);
         return mplew.getPacket();
      }

      public static byte[] removePoison(MapleCharacter player, int objid) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.WILL_POISON_REMOVE.getValue());
         mplew.writeInt(1);
         mplew.writeInt(objid);
         return mplew.getPacket();
      }
   }

   public static class BossLucid {
      public static byte[] createButterfly(int initId, List<Butterfly> butterflies) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.LUCID_BUTTERFLY_CREATE.getValue());
         mplew.writeInt(initId);
         mplew.writeInt(butterflies.size());
         Iterator var3 = butterflies.iterator();

         while(var3.hasNext()) {
            Butterfly butterfly = (Butterfly)var3.next();
            mplew.writeInt(butterfly.type);
            mplew.writeInt(butterfly.pos.x);
            mplew.writeInt(butterfly.pos.y);
         }

         return mplew.getPacket();
      }

      public static byte[] RemoveButterfly() {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.LUCID_BUTTERFLY_CREATE.getValue());
         mplew.writeInt(3);
         mplew.writeInt(3);
         mplew.writeInt(40);

         for(int i = 0; i < 40; ++i) {
            mplew.writeInt(0);
         }

         return mplew.getPacket();
      }

      public static byte[] AttackButterfly(List<Integer> chrid) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.LUCID_BUTTERFLY_CREATE.getValue());
         mplew.writeInt(2);
         mplew.writeInt(3);
         mplew.writeInt(chrid.size());
         Iterator var2 = chrid.iterator();

         while(var2.hasNext()) {
            Integer chra = (Integer)var2.next();
            mplew.writeInt(chra);
         }

         return mplew.getPacket();
      }

      public static byte[] setButterflyAction(Butterfly.Mode mode, int... args) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.LUCID_BUTTERFLY_ACTION.getValue());
         mplew.writeInt(mode.code);
         switch(mode) {
         case ADD:
            mplew.writeInt(args[0]);
            mplew.writeInt(args[1]);
            mplew.writeInt(args[2]);
            mplew.writeInt(args[3]);
            break;
         case MOVE:
            mplew.writeInt(args[0]);
            mplew.writeInt(args[1]);
            break;
         case ATTACK:
            mplew.writeInt(args[0]);
            mplew.writeInt(args[1]);
         }

         return mplew.getPacket();
      }

      public static byte[] createDragon(int phase, int posX, int posY, int createPosX, int createPosY, boolean isLeft) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.LUCID_DRAGON_CREATE.getValue());
         mplew.writeInt(phase);
         mplew.writeInt(posX);
         mplew.writeInt(posY);
         mplew.writeInt(createPosX);
         mplew.writeInt(createPosY);
         mplew.write(isLeft);
         return mplew.getPacket();
      }

      public static byte[] doFlowerTrapSkill(int level, int pattern, int x, int y, boolean flip) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.LUCID_DO_SKILL.getValue());
         mplew.writeInt(238);
         mplew.writeInt(level);
         mplew.writeInt(pattern);
         mplew.writeInt(x);
         mplew.writeInt(y);
         mplew.write(flip);
         return mplew.getPacket();
      }

      public static byte[] doLaserRainSkill(int startDelay, List<Integer> intervals) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.LUCID_DO_SKILL.getValue());
         mplew.writeInt(238);
         mplew.writeInt(5);
         mplew.writeInt(startDelay);
         mplew.writeInt(intervals.size());
         Iterator iterator = intervals.iterator();

         while(iterator.hasNext()) {
            int interval = (Integer)iterator.next();
            mplew.writeInt(interval);
         }

         return mplew.getPacket();
      }

      public static byte[] doFairyDustSkill(int level, List<FairyDust> fairyDust) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.LUCID_DO_SKILL.getValue());
         mplew.writeInt(238);
         mplew.writeInt(level);
         mplew.writeInt(fairyDust.size());
         Iterator var3 = fairyDust.iterator();

         while(var3.hasNext()) {
            FairyDust fd = (FairyDust)var3.next();
            mplew.writeInt(fd.scale);
            mplew.writeInt(fd.createDelay);
            mplew.writeInt(fd.moveSpeed);
            mplew.writeInt(fd.angle);
         }

         return mplew.getPacket();
      }

      public static byte[] doForcedTeleportSkill(int splitId) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.LUCID_DO_SKILL.getValue());
         mplew.writeInt(238);
         mplew.writeInt(6);
         mplew.writeInt(splitId);
         return mplew.getPacket();
      }

      public static byte[] doRushSkill() {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.LUCID_DO_SKILL.getValue());
         mplew.writeInt(238);
         mplew.writeInt(8);
         mplew.writeInt(0);
         return mplew.getPacket();
      }

      public static byte[] SpawnLucidDream(List<Point> pos) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.LUCID_DO_SKILL.getValue());
         mplew.writeInt(238);
         mplew.writeInt(12);
         mplew.writeInt(pos.size());
         Iterator var2 = pos.iterator();

         while(var2.hasNext()) {
            Point p = (Point)var2.next();
            mplew.writeInt(p.x);
            mplew.writeInt(p.y);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(p.x);
         }

         return mplew.getPacket();
      }

      public static byte[] setStainedGlassOnOff(boolean enable, List<String> tags) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.LUCID2_STAINED_GLASS_ON_OFF.getValue());
         mplew.write(enable);
         mplew.writeInt(tags.size());
         Iterator var3 = tags.iterator();

         while(var3.hasNext()) {
            String name = (String)var3.next();
            mplew.writeMapleAsciiString(name);
         }

         return mplew.getPacket();
      }

      public static byte[] breakStainedGlass(List<String> tags) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.LUCID2_STAINED_GLASS_BREAK.getValue());
         mplew.writeInt(tags.size());
         Iterator var2 = tags.iterator();

         while(var2.hasNext()) {
            String name = (String)var2.next();
            mplew.writeMapleAsciiString(name);
         }

         return mplew.getPacket();
      }

      public static byte[] setFlyingMode(boolean enable) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.LUCID2_SET_FLYING_MODE.getValue());
         mplew.write(enable);
         return mplew.getPacket();
      }

      public static byte[] changeStatueState(boolean placement, int gauge, boolean used) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.LUCID_STATUE_STATE_CHANGE.getValue());
         mplew.writeInt(placement ? 1 : 0);
         if (placement) {
            mplew.write(used);
         } else {
            mplew.writeInt(gauge);
            mplew.write(used);
         }

         return mplew.getPacket();
      }

      public static byte[] doShoot(int angle, int speed) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.LUCID2_WELCOME_BARRAGE.getValue());
         mplew.writeInt(0);
         mplew.writeInt(angle);
         mplew.writeInt(speed);
         return mplew.getPacket();
      }

      public static byte[] doBidirectionShoot(int angleRate, int speed, int interval, int shotCount) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.LUCID2_WELCOME_BARRAGE.getValue());
         mplew.writeInt(3);
         mplew.writeInt(angleRate);
         mplew.writeInt(speed);
         mplew.writeInt(interval);
         mplew.writeInt(shotCount);
         return mplew.getPacket();
      }

      public static byte[] doSpiralShoot(int type, int angle, int angleRate, int angleDiff, int speed, int interval, int shotCount, int bulletAngleRate, int bulletSpeedRate) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.LUCID2_WELCOME_BARRAGE.getValue());
         mplew.writeInt(type);
         mplew.writeInt(angle);
         mplew.writeInt(angleRate);
         mplew.writeInt(angleDiff);
         mplew.writeInt(speed);
         mplew.writeInt(interval);
         mplew.writeInt(shotCount);
         mplew.writeInt(bulletAngleRate);
         mplew.writeInt(bulletSpeedRate);
         return mplew.getPacket();
      }

      public static byte[] doWelcomeBarrageSkill(int type) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.LUCID2_WELCOME_BARRAGE.getValue());
         mplew.writeInt(type);
         return mplew.getPacket();
      }
   }

   public static class BossDunKel {
      public static byte[] eliteBossAttack(MapleMonster dunkel, List<DunkelEliteBoss> eliteBosses, MapleCharacter chr, boolean isLeft) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.WILL_SPIDER_ATTACK.getValue());
         Iterator var5 = eliteBosses.iterator();

         while(var5.hasNext()) {
            DunkelEliteBoss eb = (DunkelEliteBoss)var5.next();
            mplew.write(eb != null ? 1 : 0);
            if (eb == null) {
               return mplew.getPacket();
            }

            mplew.writeShort(eb.getbosscode());
            mplew.writeShort(eb.getv1());
            mplew.writeInt(eb.getUnk1());
            mplew.writeInt(eb.getUnk2());
            mplew.writeInt(eb.getUnk3());
            mplew.writeInt(eb.getUnk4());
            mplew.writeInt(dunkel.getObjectId());
            mplew.writeInt(chr != null ? chr.getId() : 0);
            mplew.writeInt(isLeft ? 0 : 1);
            mplew.writeInt(eb.getUnk5());
            mplew.writeInt(eb.getUnk6());
            mplew.writeInt(eb.getUnk7());
            mplew.writeShort(eb.getv2());
            mplew.writeShort(eb.getArrowvelocity());
            mplew.writeShort(eb.getv3());
            mplew.write(eb.getb1());
            mplew.write(eb.getb2());
            mplew.writePosInt(eb.getP1());
            mplew.writePosInt(eb.getP2());
            mplew.writePosInt(eb.getP3());
            mplew.writeShort(eb.getv4());

            for(int i = 0; i < eb.getv4(); ++i) {
               mplew.writePosInt(new Point(Randomizer.rand(-782, 774), 29));
            }

            mplew.writeShort(eb.getv5());
            if (eb.getv5() == 2) {
               mplew.write(HexTool.getByteArrayFromHexString("49 FD FF FF FB FF FF FF 00 00 00 00 E4 02 00 00 FB FF FF FF 01 00 00 00"));
            }
         }

         mplew.write((int)0);
         return mplew.getPacket();
      }
   }

   public static class BossDusk {
      public static byte[] handleDuskGauge(boolean decrease, int gauge, int full) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.DUSK_GAUGE.getValue());
         mplew.write(decrease);
         mplew.writeInt(gauge);
         mplew.writeInt(full);
         return mplew.getPacket();
      }

      public static byte[] spawnDrillAttack(int x, boolean left) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.FIELD_SKILL.getValue());
         mplew.writeInt(100020);
         mplew.writeInt(2);
         mplew.writeInt(0);
         mplew.writeInt(1501);
         mplew.write(true);
         mplew.writeInt(1501);
         mplew.writeInt(1);
         mplew.writeInt(35);
         mplew.writeInt(75);
         mplew.writeInt(1020);
         mplew.writeInt(6);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(-2185);
         mplew.writeInt(300);
         mplew.writeInt(-120);
         mplew.writeInt(120);
         mplew.writeInt(x);
         mplew.writeInt(-157);
         mplew.write(left);
         mplew.write((int)0);
         return mplew.getPacket();
      }

      public static byte[] spawnTempFoothold() {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.FIELD_SKILL.getValue());
         mplew.writeInt(100020);
         mplew.writeInt(1);
         mplew.writeInt(3);
         mplew.writeInt(23160);
         mplew.write(true);
         mplew.writeInt(20400);
         mplew.writeInt(2760);
         mplew.writeInt(75);
         mplew.writeInt(75);
         mplew.writeInt(1980);
         mplew.writeInt(5);
         mplew.writeInt(3);
         mplew.writeShort(2);
         mplew.writeInt(12642);
         mplew.writeInt(-2185);
         mplew.writeInt(300);
         mplew.writeInt(-100);
         mplew.writeInt(100);
         mplew.writeInt(-610);
         mplew.writeInt(-159);
         mplew.write((int)0);
         mplew.write((int)1);
         mplew.writeInt(21900);
         mplew.writeInt(660);
         mplew.writeInt(-81);
         mplew.writeInt(-81);
         mplew.writeInt(1980);
         mplew.writeInt(6);
         mplew.writeInt(3);
         mplew.write((int)2);
         mplew.write((int)0);
         mplew.writeInt(12898);
         mplew.writeInt(-2185);
         mplew.writeInt(300);
         mplew.writeInt(-100);
         mplew.writeInt(100);
         mplew.writeInt(600);
         mplew.writeInt(-159);
         mplew.write((int)0);
         mplew.write((int)0);
         return mplew.getPacket();
      }
   }
}
