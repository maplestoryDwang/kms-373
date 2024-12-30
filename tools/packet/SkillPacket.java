package tools.packet;

import client.MapleCharacter;
import client.SecondAtom2;
import handling.SendPacketOpcode;
import java.util.Iterator;
import java.util.List;
import server.Randomizer;
import server.field.skill.MapleMagicSword;
import server.field.skill.MapleSecondAtom;
import server.maps.MapleSummon;
import tools.HexTool;
import tools.Pair;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;

public class SkillPacket {
   public static byte[] createSecondAtom(List<MapleSecondAtom> msa) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPAWN_SECOND_ATOMS.getValue());
      mplew.writeInt(((MapleSecondAtom)msa.get(0)).getChr().getId());
      mplew.writeInt(msa.size());
      int i = 0;
      Iterator var3 = msa.iterator();

      while(true) {
         while(var3.hasNext()) {
            MapleSecondAtom atom = (MapleSecondAtom)var3.next();
            List<Integer> aCustom = atom.getSecondAtoms().getCustom();
            mplew.writeInt(atom.getObjectId());
            mplew.writeInt(atom.getSecondAtoms().getSourceId() == 63101104 ? 1 : 0);
            mplew.writeInt(atom.getSecondAtoms().getDataIndex());
            mplew.writeInt(atom.isNumuse() ? atom.getNum() : i++);
            mplew.writeInt(atom.getSecondAtoms().getLocalOnly() == 1 ? atom.getSecondAtoms().getTarget() : atom.getChr().getId());
            mplew.writeInt(atom.getSecondAtoms().getTarget());
            mplew.writeInt(atom.getSecondAtoms().getCreateDelay());
            mplew.writeInt(atom.getSecondAtoms().getEnableDelay());
            mplew.writeInt(atom.getChr().isFacingLeft() ? -atom.getSecondAtoms().getRotate() : atom.getSecondAtoms().getRotate());
            mplew.writeInt(atom.getSecondAtoms().getSourceId());
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(atom.getSecondAtoms().getSourceId() == 400031066 ? (long)atom.getSecondAtoms().getExpire() + atom.getChr().getSkillCustomValue0(400031066) * 1000L : (long)atom.getSecondAtoms().getExpire());
            mplew.writeInt(atom.getChr().isFacingLeft() ? -atom.getSecondAtoms().getRotate() : atom.getSecondAtoms().getRotate());
            mplew.writeInt(atom.getSecondAtoms().getAttackableCount());
            mplew.writeInt(atom.getSecondAtoms().getLocalOnly());
            mplew.writeInt(0);
            mplew.writeInt(atom.getPos().x + (atom.getChr().isFacingLeft() ? -atom.getSecondAtoms().getPos().x : atom.getSecondAtoms().getPos().x));
            mplew.writeInt(atom.getPos().y + atom.getSecondAtoms().getPos().y);
            mplew.write((int)0);
            mplew.write((int)0);
            mplew.write((int)0);
            if (atom.getSecondAtoms().getSourceId() == 400031063) {
               mplew.writeInt(1);
               mplew.writeInt(atom.getChr().isFacingLeft() ? 1 : 0);
            } else {
               mplew.writeInt(aCustom.size());
               Iterator var6 = aCustom.iterator();

               while(var6.hasNext()) {
                  Integer c = (Integer)var6.next();
                  mplew.writeInt(c);
               }
            }
         }

         mplew.writeInt(((MapleSecondAtom)msa.get(0)).getDataIndex() == 8 ? 1 : 0);
         return mplew.getPacket();
      }
   }

   public static byte[] createSecondAtom(List<MapleSecondAtom> msa, boolean left) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPAWN_SECOND_ATOMS.getValue());
      mplew.writeInt(((MapleSecondAtom)msa.get(0)).getChr().getId());
      mplew.writeInt(msa.size());
      int i = 0;
      Iterator var4 = msa.iterator();

      while(true) {
         while(var4.hasNext()) {
            MapleSecondAtom atom = (MapleSecondAtom)var4.next();
            List<Integer> aCustom = atom.getSecondAtoms().getCustom();
            mplew.writeInt(atom.getObjectId());
            mplew.writeInt(atom.getSecondAtoms().getSourceId() == 2121052 ? 2 : (atom.getSecondAtoms().getSourceId() == 63101104 ? 1 : 0));
            mplew.writeInt(atom.getSecondAtoms().getDataIndex());
            mplew.writeInt(atom.isNumuse() ? atom.getNum() : i++);
            mplew.writeInt(atom.getSecondAtoms().getLocalOnly() == 1 ? atom.getSecondAtoms().getTarget() : atom.getChr().getId());
            mplew.writeInt(atom.getSecondAtoms().getTarget());
            mplew.writeInt(atom.getSecondAtoms().getCreateDelay());
            mplew.writeInt(atom.getSecondAtoms().getEnableDelay());
            mplew.writeInt(left ? -atom.getSecondAtoms().getRotate() : atom.getSecondAtoms().getRotate());
            mplew.writeInt(atom.getSecondAtoms().getSourceId());
            mplew.writeInt(0);
            mplew.writeInt(atom.getSecondAtoms().getSourceId() == 2121052 ? 1 : 0);
            mplew.writeInt(atom.getSecondAtoms().getSourceId() == 400031066 ? (long)atom.getSecondAtoms().getExpire() + atom.getChr().getSkillCustomValue0(400031066) * 1000L : (long)atom.getSecondAtoms().getExpire());
            mplew.writeInt(left ? -atom.getSecondAtoms().getRotate() : atom.getSecondAtoms().getRotate());
            mplew.writeInt(atom.getSecondAtoms().getAttackableCount());
            mplew.writeInt(atom.getSecondAtoms().getLocalOnly());
            mplew.writeInt(0);
            mplew.writeInt(atom.getPos().x + (atom.getChr().isFacingLeft() ? -atom.getSecondAtoms().getPos().x : atom.getSecondAtoms().getPos().x));
            mplew.writeInt(atom.getPos().y + atom.getSecondAtoms().getPos().y);
            mplew.write((int)0);
            mplew.write((int)0);
            mplew.write((int)0);
            if (atom.getSecondAtoms().getSourceId() == 400031063) {
               mplew.writeInt(1);
               mplew.writeInt(left ? 1 : 0);
            } else {
               mplew.writeInt(aCustom.size());
               Iterator var7 = aCustom.iterator();

               while(var7.hasNext()) {
                  Integer c = (Integer)var7.next();
                  mplew.writeInt(c);
               }
            }
         }

         mplew.writeInt(((MapleSecondAtom)msa.get(0)).getDataIndex() == 8 ? 1 : 0);
         return mplew.getPacket();
      }
   }

   public static byte[] CreateSubObtacle(MapleCharacter chr, MapleSummon summon, List<Triple<Integer, Integer, Integer>> list, int type) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPAWN_SECOND_ATOMS.getValue());
      mplew.writeInt(chr.getId());
      mplew.writeInt(list.size());
      int i = 0;

      for(Iterator var6 = list.iterator(); var6.hasNext(); ++i) {
         Triple<Integer, Integer, Integer> a = (Triple)var6.next();
         mplew.writeInt(i + 1);
         mplew.writeInt(0);
         mplew.writeInt(type);
         mplew.writeInt(i);
         mplew.writeInt(chr.getId());
         mplew.writeInt((Integer)a.left);
         mplew.writeInt(i * 120);
         mplew.writeInt(i * 120 + 360);
         mplew.writeInt(0);
         mplew.writeInt((Integer)a.mid);
         mplew.writeInt(0);
         mplew.writeInt(1);
         mplew.writeInt((Integer)a.right);
         mplew.writeInt(summon.getPosition().x);
         mplew.writeInt(1);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(summon.getPosition().x + (summon.isFacingLeft() ? -150 : 150));
         mplew.writeInt(summon.getPosition().y - 180);
         mplew.writeShort(0);
         mplew.write((int)0);
         mplew.writeInt(0);
      }

      mplew.writeInt(2);
      return mplew.getPacket();
   }

   public static byte[] AttackSecondAtom(MapleCharacter chr, int objid, int count) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ATTACK_SECOND_ATOM.getValue());
      mplew.writeInt(chr.getId());
      mplew.writeInt(objid);
      mplew.writeInt(count);
      return mplew.getPacket();
   }

   public static byte[] removeSecondAtom(int cid, Pair<Integer, SecondAtom2> atom) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.REMOVE_SECOND_ATOM.getValue());
      mplew.writeInt(cid);
      mplew.writeInt(1);
      mplew.writeInt((Integer)atom.getLeft());
      mplew.writeInt(((SecondAtom2)atom.getRight()).getDataIndex() >= 1 && ((SecondAtom2)atom.getRight()).getDataIndex() <= 6 ? 1 : 0);
      mplew.writeInt(1);
      return mplew.getPacket();
   }

   public static byte[] CreateSworldReadyObtacle(MapleCharacter chr, int skillid, int count) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPAWN_SECOND_ATOMS.getValue());
      mplew.writeInt(chr.getId());
      mplew.writeInt(2);

      for(int i = 1; i <= 2; ++i) {
         mplew.writeInt(i == 1 ? (count - 1) * 10 : count * 10);
         mplew.writeInt(0);
         mplew.writeInt(i == 1 ? count - 1 : count);
         mplew.writeInt(0);
         mplew.writeInt(chr.getId());
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(skillid);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(1);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(chr.getPosition().x);
         mplew.writeInt(1);
         mplew.writeInt(0);
         mplew.writeShort(0);
         mplew.write((int)0);
      }

      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] AutoAttackObtacleSword(MapleCharacter chr, int sword, int id) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ATTACK_SECOND_ATOM.getValue());
      mplew.writeInt(chr.getId());
      mplew.writeInt(sword);
      if (id != 6 && id != 5) {
         if (id != 4 && id != 3) {
            if (id != 2 && id != 1) {
               mplew.writeInt(0);
            } else {
               mplew.writeInt(1);
            }
         } else {
            mplew.writeInt(2);
         }
      } else {
         mplew.writeInt(3);
      }

      return mplew.getPacket();
   }

   public static byte[] CreateSubObtacle(MapleCharacter chr, int skillid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPAWN_SECOND_ATOMS.getValue());
      mplew.writeInt(chr.getId());
      mplew.writeInt(5);

      for(int i = 1; i <= 5; ++i) {
         mplew.writeInt(i);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(i - 1);
         mplew.writeInt(chr.getId());
         mplew.writeInt(0);
         mplew.writeInt(i != 2 && i != 3 ? (i != 4 && i != 5 ? 0 : 240) : 120);
         mplew.writeInt(600);
         mplew.writeInt(i == 2 ? 15 : (i == 3 ? -15 : (i == 4 ? 30 : (i == 5 ? -30 : 0))));
         mplew.writeInt(skillid);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(2400);
         mplew.writeInt(0);
         mplew.writeInt(1);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(i == 1 ? chr.getPosition().x : (i == 2 ? chr.getPosition().x + 40 : (i == 3 ? chr.getPosition().x - 40 : (i == 4 ? chr.getPosition().x + 80 : (i == 5 ? chr.getPosition().x - 80 : 0)))));
         mplew.writeInt(i == 1 ? chr.getPosition().y - 110 : (i != 2 && i != 3 ? (i != 4 && i != 5 ? 0 : chr.getPosition().y - 90) : chr.getPosition().y - 100));
         mplew.write((int)0);
         mplew.write((int)0);
         mplew.write((int)0);
         mplew.writeInt(0);
      }

      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] RemoveSubObtacle(MapleCharacter chr, int id) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.REMOVE_SECOND_ATOM.getValue());
      mplew.writeInt(chr.getId());
      mplew.writeInt(1);
      mplew.writeInt(id);
      mplew.writeInt(0);
      mplew.writeInt(1);
      return mplew.getPacket();
   }

   public static byte[] CreateSworldObtacle(MapleMagicSword ms) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPAWN_SECOND_ATOMS.getValue());
      mplew.writeInt(ms.getChr().getId());
      mplew.writeInt(1);
      if (ms.core()) {
         mplew.writeInt(ms.getObjectId());
         mplew.writeInt(0);
         mplew.writeInt(8);
         mplew.writeInt(ms.getSwordCount());
         mplew.writeInt(ms.getChr().getId());
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(ms.getSourceid());
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(ms.getDuration());
         mplew.writeInt(ms.getChr().getPosition().x + Randomizer.rand(-500, 500));
         mplew.writeInt(ms.getChr().getPosition().y + Randomizer.rand(-300, 300));
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(ms.getChr().getPosition().x + Randomizer.rand(-500, 500));
         mplew.writeInt(ms.getChr().getPosition().y + Randomizer.rand(-300, 300));
         mplew.writeInt(0);
         mplew.writeShort(0);
         mplew.write((int)0);
         mplew.writeInt(ms.getChr().getSkillCustomValue0(400011108) > 0L ? 1 : 0);
      } else {
         mplew.writeInt(ms.getObjectId());
         mplew.writeInt(0);
         mplew.writeInt(7);
         mplew.writeInt(ms.getSwordCount());
         mplew.writeInt(ms.getChr().getId());
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(ms.getSourceid());
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(ms.getDuration());
         mplew.writeInt(ms.getChr().getPosition().x + (ms.getSwordCount() != 0 && ms.getSwordCount() != 2 && ms.getSwordCount() != 4 && ms.getSwordCount() != 6 && ms.getSwordCount() != 8 ? -30 : 30));
         mplew.writeInt(ms.getChr().getPosition().y);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(ms.getChr().getPosition().x + (ms.getSwordCount() != 0 && ms.getSwordCount() != 2 && ms.getSwordCount() != 4 && ms.getSwordCount() != 6 && ms.getSwordCount() != 8 ? -30 : 30));
         mplew.writeInt(ms.getChr().getPosition().y);
         mplew.writeShort(0);
         mplew.write((int)0);
         mplew.writeInt(3);
         switch(ms.getSwordCount()) {
         case 0:
         case 2:
         case 4:
         case 6:
         case 8:
         case 10:
         case 12:
         case 14:
         case 16:
         case 18:
         case 20:
            mplew.write(HexTool.getByteArrayFromHexString("79 FF FF FF 5B FF FF FF 6A FF FF FF "));
            break;
         case 1:
         case 3:
         case 5:
         case 7:
         case 9:
         case 11:
         case 13:
         case 15:
         case 17:
         case 19:
         case 21:
            mplew.write(HexTool.getByteArrayFromHexString("87 00 00 00 A5 00 00 00 96 00 00 00 "));
            break;
         default:
            mplew.writeZeroBytes(12);
         }

         mplew.writeInt(0);
      }

      return mplew.getPacket();
   }

   public static byte[] 권술호접지몽(MapleCharacter chr) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CREATE_FORCE_ATOM.getValue());
      mplew.write((int)0);
      mplew.writeInt(chr.getId());
      mplew.writeInt(61);
      mplew.writeInt(0);
      mplew.write((int)0);
      mplew.writeInt(164120007);
      if (Randomizer.isSuccess(35)) {
         mplew.write(HexTool.getByteArrayFromHexString("01 D2 00 00 00 01 00 00 00 2C 00 00 00 03 00 00 00 F5 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 A3 60 31 15 00 00 00 00 00 00 00 00 00 00 00 00 01 D3 00 00 00 02 00 00 00 2A 00 00 00 04 00 00 00 7A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 A3 60 31 15 00 00 00 00 00 00 00 00 00 00 00 00 01 D4 00 00 00 03 00 00 00 2A 00 00 00 04 00 00 00 7C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 A3 60 31 15 00 00 00 00 00 00 00 00 00 00 00 00 01 D5 00 00 00 04 00 00 00 2C 00 00 00 04 00 00 00 12 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 A3 60 31 15 00 00 00 00 00 00 00 00 00 00 00 00 01 D6 00 00 00 05 00 00 00 2C 00 00 00 03 00 00 00 65 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 A3 60 31 15 00 00 00 00 00 00 00 00 00 00 00 00 00"));
      } else if (Randomizer.isSuccess(35)) {
         mplew.write(HexTool.getByteArrayFromHexString("01 D7 00 00 00 01 00 00 00 2C 00 00 00 03 00 00 00 23 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 9A 64 31 15 00 00 00 00 00 00 00 00 00 00 00 00 01 D8 00 00 00 02 00 00 00 29 00 00 00 03 00 00 00 32 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 9A 64 31 15 00 00 00 00 00 00 00 00 00 00 00 00 01 D9 00 00 00 03 00 00 00 28 00 00 00 03 00 00 00 28 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 9A 64 31 15 00 00 00 00 00 00 00 00 00 00 00 00 01 DA 00 00 00 04 00 00 00 2A 00 00 00 04 00 00 00 7F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 9A 64 31 15 00 00 00 00 00 00 00 00 00 00 00 00 01 DB 00 00 00 05 00 00 00 2C 00 00 00 03 00 00 00 3A 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 9A 64 31 15 00 00 00 00 00 00 00 00 00 00 00 00 00"));
      } else {
         mplew.write(HexTool.getByteArrayFromHexString("01 DC 00 00 00 01 00 00 00 2B 00 00 00 04 00 00 00 C7 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 92 68 31 15 00 00 00 00 00 00 00 00 00 00 00 00 01 DD 00 00 00 02 00 00 00 28 00 00 00 04 00 00 00 F3 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 92 68 31 15 00 00 00 00 00 00 00 00 00 00 00 00 01 DE 00 00 00 03 00 00 00 2C 00 00 00 03 00 00 00 5E 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 92 68 31 15 00 00 00 00 00 00 00 00 00 00 00 00 01 DF 00 00 00 04 00 00 00 2B 00 00 00 04 00 00 00 C3 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 92 68 31 15 00 00 00 00 00 00 00 00 00 00 00 00 01 E0 00 00 00 05 00 00 00 28 00 00 00 03 00 00 00 5A 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 92 68 31 15 00 00 00 00 00 00 00 00 00 00 00 00 00"));
      }

      mplew.writeInt(chr.getPosition().x);
      mplew.writeInt(chr.getPosition().y);
      return mplew.getPacket();
   }

   public static byte[] SpawnSpell(int cid, List<Integer> moblist1, List<Integer> moblist2, List<Integer> moblist3, List<Integer> moblist4, int count) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPAWN_BULLET.getValue());
      mplew.writeInt(cid);
      mplew.writeInt(155001103);
      mplew.writeInt(4);
      mplew.writeInt(46);
      mplew.writeInt(155121003);
      mplew.writeInt(moblist1.size());

      int i;
      for(i = 0; i < moblist1.size(); ++i) {
         mplew.writeInt((Integer)moblist1.get(i));
         mplew.write((int)1);
         mplew.writeInt(2 + i);
         mplew.writeInt(200 + i);
         mplew.writeInt(0);
         mplew.writeInt(Randomizer.rand(35, 57));
         mplew.writeInt(Randomizer.rand(5, 6));
         mplew.writeInt(Randomizer.rand(39, 67));
         mplew.writeInt(Randomizer.rand(750, 910));
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt((int)System.currentTimeMillis());
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.write((int)0);
      }

      mplew.writeInt(45);
      mplew.writeInt(155111003);
      mplew.writeInt(moblist2.size());

      for(i = 0; i < moblist2.size(); ++i) {
         mplew.writeInt((Integer)moblist2.get(i));
         mplew.write((int)1);
         mplew.writeInt(2 + i);
         mplew.writeInt(200 + i);
         mplew.writeInt(0);
         mplew.writeInt(Randomizer.rand(35, 57));
         mplew.writeInt(Randomizer.rand(5, 6));
         mplew.writeInt(Randomizer.rand(39, 67));
         mplew.writeInt(Randomizer.rand(750, 910));
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt((int)System.currentTimeMillis());
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.write((int)0);
      }

      mplew.writeInt(44);
      mplew.writeInt(155101002);
      mplew.writeInt(moblist3.size());

      for(i = 0; i < moblist3.size(); ++i) {
         mplew.writeInt((Integer)moblist3.get(i));
         mplew.write((int)1);
         mplew.writeInt(2 + i);
         mplew.writeInt(200 + i);
         mplew.writeInt(0);
         mplew.writeInt(Randomizer.rand(35, 57));
         mplew.writeInt(Randomizer.rand(5, 6));
         mplew.writeInt(Randomizer.rand(39, 67));
         mplew.writeInt(Randomizer.rand(750, 910));
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt((int)System.currentTimeMillis());
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.write((int)0);
      }

      mplew.writeInt(43);
      mplew.writeInt(155001000);
      mplew.writeInt(moblist4.size());

      for(i = 0; i < moblist4.size(); ++i) {
         mplew.writeInt((Integer)moblist4.get(i));
         mplew.write((int)1);
         mplew.writeInt(2 + i);
         mplew.writeInt(i);
         mplew.writeInt(0);
         mplew.writeInt(Randomizer.rand(35, 57));
         mplew.writeInt(Randomizer.rand(5, 6));
         mplew.writeInt(Randomizer.rand(39, 67));
         mplew.writeInt(Randomizer.rand(750, 910));
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt((int)System.currentTimeMillis());
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.write((int)0);
         mplew.writeInt(0);
      }

      return mplew.getPacket();
   }

   public static byte[] 메모리초이스(int skillid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MEMORY_CHOICE.getValue());
      mplew.writeInt(skillid);
      return mplew.getPacket();
   }
}
