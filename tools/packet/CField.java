package tools.packet;

import client.AvatarLook;
import client.InnerSkillValueHolder;
import client.MapleCabinet;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleKeyLayout;
import client.MapleQuestStatus;
import client.MapleUnion;
import client.MatrixSkill;
import client.RangeAttack;
import client.SecondaryStat;
import client.SecondaryStatValueHolder;
import client.Skill;
import client.SkillMacro;
import client.VMatrix;
import client.inventory.AuctionHistory;
import client.inventory.AuctionItem;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleAndroid;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.MapleRing;
import constants.GameConstants;
import constants.ServerConstants;
import handling.SendPacketOpcode;
import handling.channel.handler.AttackInfo;
import handling.channel.handler.PlayerInteractionHandler;
import handling.world.World;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildAlliance;
import java.awt.Point;
import java.awt.Rectangle;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import scripting.EventInstanceManager;
import server.AdelProjectile;
import server.ChatEmoticon;
import server.DailyGiftItemInfo;
import server.DimentionMirrorEntry;
import server.MapleDueyActions;
import server.MapleItemInformationProvider;
import server.MapleTrade;
import server.QuickMoveEntry;
import server.Randomizer;
import server.WeekendMaple;
import server.enchant.EnchantFlag;
import server.enchant.StarForceStats;
import server.field.skill.MapleFieldAttackObj;
import server.field.skill.MapleMagicWreck;
import server.field.skill.MapleOrb;
import server.field.skill.MapleSecondAtom;
import server.field.skill.SecondAtom;
import server.field.skill.SpecialPortal;
import server.life.MapleHaku;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.maps.ForceAtom;
import server.maps.MapleAtom;
import server.maps.MapleDragon;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMist;
import server.maps.MapleNodes;
import server.maps.MapleReactor;
import server.maps.MapleRune;
import server.maps.MapleSpecialChair;
import server.maps.MapleSummon;
import server.maps.MechDoor;
import server.maps.SummonMovementType;
import server.movement.LifeMovementFragment;
import server.quest.MapleQuest;
import server.shops.MapleShop;
import tools.AttackPair;
import tools.HexTool;
import tools.Pair;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;

public class CField {
   public static byte[] getPacketFromHexString(String hex) {
      return HexTool.getByteArrayFromHexString(hex);
   }

   public static byte[] TangyoonMobList(int one, int two, int three, int four, int five) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.TANGYOON_LIST.getValue());
      mplew.writeInt(5);
      mplew.writeInt(one);
      mplew.writeInt(two);
      mplew.writeInt(three);
      mplew.writeInt(four);
      mplew.writeInt(five);
      return mplew.getPacket();
   }

   public static byte[] getServerIP(MapleClient c, int port, int clientId) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SERVER_IP.getValue());
      mplew.writeShort(0);
      mplew.writeShort(0);
      mplew.write(GameConstants.getServerIp(ServerConstants.Gateway_IP));
      mplew.writeShort(port);
      mplew.writeInt(clientId);
      mplew.writeMapleAsciiString("normal");
      mplew.writeMapleAsciiString("normal");
      mplew.write((int)1);
      mplew.writeInt(0);
      mplew.write((int)0);
      mplew.write((int)0);
      mplew.writeLong(1L);
      mplew.writeLong(0L);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] getChannelChange(MapleClient c, int port) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CHANGE_CHANNEL.getValue());
      mplew.write((int)1);
      mplew.write(GameConstants.getServerIp(ServerConstants.Gateway_IP));
      mplew.writeShort(port);
      return mplew.getPacket();
   }

   public static byte[] PsychicGrabPreparation(MapleCharacter chr, int skillid, short level, int unk, int speed, int unk1, int unk2, int unk3, int unk4) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public static byte[] getMacros(SkillMacro[] macros) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SKILL_MACRO.getValue());
      int count = 0;

      int i;
      for(i = 0; i < 5; ++i) {
         if (macros[i] != null) {
            ++count;
         }
      }

      mplew.write(count);

      for(i = 0; i < 5; ++i) {
         SkillMacro macro = macros[i];
         if (macro != null) {
            mplew.writeMapleAsciiString(macro.getName());
            mplew.write(macro.getShout());
            mplew.writeInt(macro.getSkill1());
            mplew.writeInt(macro.getSkill2());
            mplew.writeInt(macro.getSkill3());
         }
      }

      return mplew.getPacket();
   }

   public static byte[] getCharInfo(MapleCharacter chr) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue());
      mplew.writeInt(chr.getClient().getChannel() - 1);
      mplew.write((int)0);
      mplew.writeInt(0);
      mplew.write((int)1);
      mplew.writeInt(0);
      mplew.write((int)0);
      mplew.writeInt(1550);
      mplew.writeInt(2070);
      mplew.write((int)1);
      mplew.writeShort(ServerConstants.serverMessage.length() > 0 ? 1 : 0);
      if (ServerConstants.serverMessage.length() > 0) {
         mplew.writeMapleAsciiString(ServerConstants.serverMessage);
         mplew.writeMapleAsciiString(ServerConstants.serverMessage);
      }

      int seed1 = Randomizer.nextInt();
      int seed2 = Randomizer.nextInt();
      int seed3 = Randomizer.nextInt();
      chr.getCalcDamage().SetSeed(seed1, seed2, seed3);
      mplew.writeInt(seed1);
      mplew.writeInt(seed2);
      mplew.writeInt(seed3);
      PacketHelper.addCharacterInfo(mplew, chr);
      mplew.write(true);
      mplew.write((int)0);
      mplew.writeLong(PacketHelper.getTime(-2L));
      mplew.write((int)0);
      mplew.writeLong(PacketHelper.getTime(-2L));
      mplew.write(false);
      mplew.write(false);
      mplew.write((int)0);
      mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
      mplew.writeInt(100);
      mplew.write((int)0);
      mplew.write((int)0);
      mplew.write(GameConstants.isPhantom(chr.getJob()) ? 0 : 1);
      if (chr.getMapId() / 10 == 10520011 || chr.getMapId() / 10 == 10520051 || chr.getMapId() == 105200519) {
         mplew.write((int)0);
      }

      mplew.write((int)0);
      mplew.writeInt(0);
      mplew.write((int)0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.write(true);
      mplew.writeInt(-1);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(999999999);
      mplew.writeInt(999999999);
      mplew.writeMapleAsciiString("");
      boolean sundayMaple = false;
      Date time = new Date();
      int day = time.getDay();
      if (day != 4 && day != 5 && day != 6 && day == 0) {
      }

      mplew.write(sundayMaple);
      if (sundayMaple) {
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일");
         Date date = new Date();
         Calendar cal = Calendar.getInstance(Locale.KOREA);
         cal.setTime(date);
         cal.add(5, 7 - cal.get(7));
         String day1 = sdf.format(cal.getTime());
         cal.setTime(date);
         cal.add(5, 8 - cal.get(7));
         String day2 = sdf.format(cal.getTime());
         Calendar now = Calendar.getInstance();
         ServerConstants.SundayMapleTEXTLINE_2 = "#fn나눔고딕 ExtraBold##fc0xFFB7EC00#" + day1 + " ~ " + day2;
         String a = "";
         WeekendMaple[] var15 = WeekendMaple.values();
         int var16 = var15.length;

         for(int var17 = 0; var17 < var16; ++var17) {
            WeekendMaple maple = var15[var17];
            if (WeekendMaple.hasEvent(maple, GameConstants.getWeek_WeekendMaple())) {
               String days = maple.getDate() == 0 ? "토" : "일";
               a = a + "#sunday# #fn나눔고딕 ExtraBold##fs18##fc0xFFFAF4C0#" + maple.getEvent() + " (" + days + ")\r\n\r\n";
            }
         }

         ServerConstants.SundayMapleTEXTLINE_1 = a;
         mplew.writeMapleAsciiString(ServerConstants.SundayMapleUI);
         mplew.writeMapleAsciiString(ServerConstants.SundayMapleTEXTLINE_1);
         mplew.writeMapleAsciiString(ServerConstants.SundayMapleTEXTLINE_2);
         mplew.writeInt(60);
         mplew.writeInt(220);
      }

      mplew.writeInt(0);
      mplew.write(false);
      mplew.writeInt(0);
      mplew.writeInt(1);
      mplew.write(false);
      return mplew.getPacket();
   }

   public static byte[] getWarpToMap(MapleMap to, int spawnPoint, MapleCharacter chr) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue());
      mplew.writeInt(chr.getClient().getChannel() - 1);
      mplew.write((int)0);
      mplew.writeInt(0);
      mplew.write((int)2);
      mplew.writeInt(0);
      mplew.write((int)0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.write((int)0);
      mplew.writeShort(0);
      mplew.write((int)0);
      mplew.writeInt(to.getId());
      mplew.write(spawnPoint);
      mplew.writeInt(chr.getStat().getHp());
      mplew.write(false);
      mplew.write((int)0);
      mplew.write(GameConstants.보스맵(to.getId()) ? 1 : 1);
      mplew.writeLong(PacketHelper.getKoreanTimestamp(System.currentTimeMillis()));
      mplew.writeInt(100);
      mplew.write((int)0);
      mplew.write((int)0);
      mplew.write(GameConstants.isPhantom(chr.getJob()) ? 0 : 1);
      if (to.getId() / 10 == 10520011 || to.getId() / 10 == 10520051 || to.getId() == 105200519) {
         mplew.write((int)0);
      }

      mplew.write((int)0);
      mplew.write((int)0);
      mplew.write((int)0);
      mplew.writeInt(0);
      mplew.write((int)0);
      mplew.writeInt(0);
      mplew.writeShort(0);
      mplew.write(true);
      mplew.writeInt(-1);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(999999999);
      mplew.writeInt(999999999);
      mplew.writeMapleAsciiString("");
      mplew.write(false);
      mplew.writeInt(0);
      if (chr.getMap().getFieldType().equals("63")) {
         mplew.write((int)0);
      }

      mplew.write(false);
      mplew.write(true);
      mplew.writeZeroBytes(10);
      return mplew.getPacket();
   }

   public static byte[] showEquipEffect() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_EQUIP_EFFECT.getValue());
      return mplew.getPacket();
   }

   public static byte[] showEquipEffect(int team) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_EQUIP_EFFECT.getValue());
      mplew.writeShort(team);
      return mplew.getPacket();
   }

   public static byte[] multiChat(MapleCharacter chr, String chattext, int mode, Item item) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(item == null ? SendPacketOpcode.MULTICHAT.getValue() : SendPacketOpcode.MULTICHATITEM.getValue());
      mplew.write(mode);
      mplew.writeInt(chr.getAccountID());
      mplew.writeInt(chr.getId());
      mplew.writeMapleAsciiString(chr.getName());
      mplew.writeMapleAsciiString(chattext);
      PacketHelper.ChatPacket(mplew, chr.getName(), chattext);
      mplew.write(item != null);
      if (item != null) {
         PacketHelper.addItemInfo(mplew, item);
         mplew.writeMapleAsciiString(MapleItemInformationProvider.getInstance().getName(item.getItemId()));
      }

      mplew.writeZeroBytes(10);
      return mplew.getPacket();
   }

   public static byte[] getFindReplyWithCS(String target, boolean buddy) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
      mplew.write(buddy ? 72 : 9);
      mplew.writeMapleAsciiString(target);
      mplew.write((int)2);
      mplew.writeInt(-1);
      return mplew.getPacket();
   }

   public static byte[] getWhisper(String sender, int channel, String text, Item item) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
      mplew.write((int)18);
      mplew.writeInt(0);
      mplew.writeMapleAsciiString(sender);
      mplew.writeInt(0);
      mplew.writeShort(channel - 1);
      mplew.writeMapleAsciiString(text);
      PacketHelper.ChatPacket(mplew, sender, text);
      mplew.write(item != null);
      if (item != null) {
         PacketHelper.addItemInfo(mplew, item);
         mplew.writeMapleAsciiString(MapleItemInformationProvider.getInstance().getName(item.getItemId()));
      }

      mplew.writeZeroBytes(100);
      return mplew.getPacket();
   }

   public static byte[] getWhisperReply(String target, byte reply) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
      mplew.write((int)10);
      mplew.writeMapleAsciiString(target);
      mplew.write(reply);
      return mplew.getPacket();
   }

   public static byte[] getWhisperReply(String target, byte write, byte reply) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
      mplew.write(write);
      mplew.writeMapleAsciiString(target);
      mplew.write(reply);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] getFindReplyWithMap(String target, int mapid, boolean buddy) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
      mplew.write(buddy ? 72 : 9);
      mplew.writeMapleAsciiString(target);
      mplew.write((int)1);
      mplew.writeInt(mapid);
      mplew.writeZeroBytes(8);
      return mplew.getPacket();
   }

   public static byte[] getFindReply(String target, int channel, boolean buddy) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
      mplew.write(buddy ? 72 : 9);
      mplew.writeMapleAsciiString(target);
      mplew.write((int)3);
      mplew.writeInt(channel - 1);
      return mplew.getPacket();
   }

   public static final byte[] MapEff(String path) {
      return environmentChange(path, 12);
   }

   public static final byte[] MapNameDisplay(int mapid) {
      return environmentChange("maplemap/enter/" + mapid, 12);
   }

   public static final byte[] Aran_Start() {
      return environmentChange("Aran/balloon", 4);
   }

   public static byte[] getSelectPower(int type, int code) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.BLACK_MAGE_TAMPORARY_SKILL.getValue());
      mplew.writeInt(type);
      mplew.writeInt(code);
      switch(type) {
      case 8:
         mplew.writeInt(1);
         mplew.writeInt(80002623);
         mplew.writeInt(3);
         mplew.writeInt(1);
         mplew.writeInt(1278807629);
         break;
      case 9:
         mplew.writeInt(80002623);
      }

      mplew.writeZeroBytes(10);
      return mplew.getPacket();
   }

   public static byte[] FlagRaceSkill(int... args) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.BLACK_MAGE_TAMPORARY_SKILL.getValue());
      mplew.writeInt(args[0]);
      switch(args[0]) {
      case 5:
         mplew.writeInt(args[1]);
         mplew.writeInt(args[2]);
         mplew.writeInt(args[3]);
         mplew.writeInt(0);
      default:
         return mplew.getPacket();
      }
   }

   public static byte[] musicChange(String song) {
      return environmentChange(song, 7);
   }

   public static byte[] showEffect(String effect) {
      return environmentChange(effect, 4);
   }

   public static byte[] playSound(String sound) {
      return environmentChange(sound, 5);
   }

   public static byte[] environmentChange(String env, int mode) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
      mplew.write(mode);
      mplew.writeMapleAsciiString(env);
      if (mode != 4 && mode != 11 && mode != 19 && mode != 20 && mode != 16) {
         mplew.writeInt(100);
      }

      if (mode == 7 || mode == 19) {
         mplew.writeInt(0);
      }

      if (mode == 20) {
         mplew.write((int)7);
         mplew.write((int)1);
      }

      mplew.writeInt(-1);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] KaiserChangeColor(int cid, int color1, int color2, byte premium) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.KAISER_CHANGE_COLOR.getValue());
      mplew.writeInt(cid);
      mplew.writeInt(color1);
      mplew.writeInt(color2);
      mplew.write(premium);
      return mplew.getPacket();
   }

   public static byte[] trembleEffect(int type, int delay) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
      mplew.write((int)1);
      mplew.write(type);
      mplew.writeInt(delay);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] environmentMove(String env, int mode) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MOVE_ENV.getValue());
      mplew.writeMapleAsciiString(env);
      mplew.writeInt(mode);
      return mplew.getPacket();
   }

   public static byte[] getUpdateEnvironment(List<MapleNodes.Environment> list) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.UPDATE_ENV.getValue());
      mplew.writeInt(list.size());
      Iterator var2 = list.iterator();

      while(var2.hasNext()) {
         MapleNodes.Environment mp = (MapleNodes.Environment)var2.next();
         mplew.writeMapleAsciiString(mp.getName());
         mplew.write(false);
         mplew.writeInt(mp.isShow() ? 1 : 0);
         mplew.writeInt(mp.getX());
         mplew.writeInt(mp.getY());
      }

      return mplew.getPacket();
   }

   public static byte[] startMapEffect(String msg, int itemid, boolean active) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MAP_EFFECT.getValue());
      mplew.writeInt(itemid);
      if (active) {
         mplew.writeMapleAsciiString(msg);
         mplew.writeInt(itemid == 5120025 ? 3872 : (itemid != 5121101 && itemid != 5121041 ? (itemid != 5121112 && itemid != 5121113 && itemid != 5121114 && itemid != 5121115 ? 3 : 600) : 10));
         mplew.write((int)0);
      }

      return mplew.getPacket();
   }

   public static byte[] removeMapEffect() {
      return startMapEffect((String)null, 0, false);
   }

   public static byte[] getPVPClock(int type, int time) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
      mplew.write((int)3);
      mplew.write(type);
      mplew.writeInt(time);
      return mplew.getPacket();
   }

   public static byte[] getVanVanClock(byte type, int time) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
      mplew.write((int)5);
      mplew.write(type);
      mplew.writeInt(time);
      return mplew.getPacket();
   }

   public static byte[] getTrueRoomClock(int time) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
      mplew.write((int)2);
      mplew.writeInt(time);
      return mplew.getPacket();
   }

   public static byte[] getDojoClockStop(boolean stop, int time) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
      mplew.write((int)7);
      mplew.write(stop);
      mplew.writeInt(time);
      mplew.writeZeroBytes(10);
      return mplew.getPacket();
   }

   public static byte[] getDojoClock(int endtime, int starttime) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
      mplew.write((int)8);
      mplew.writeInt(endtime);
      mplew.writeInt(starttime);
      return mplew.getPacket();
   }

   public static byte[] getClock(int time) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
      mplew.write((int)2);
      mplew.writeInt(time);
      return mplew.getPacket();
   }

   public static final byte[] VonVonStopWatch(int timer) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
      mplew.write((int)4);
      mplew.writeInt(20000);
      mplew.writeInt(timer);
      return mplew.getPacket();
   }

   public static byte[] getClockMilliEvent(long time) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
      mplew.write((int)13);
      mplew.writeLong(PacketHelper.getTime(time));
      return mplew.getPacket();
   }

   public static byte[] getClockTime(int hour, int min, int sec) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
      mplew.write((int)1);
      mplew.write(hour);
      mplew.write(min);
      mplew.write(sec);
      return mplew.getPacket();
   }

   public static byte[] PunchKingPacket(MapleCharacter chr, int type, int... args) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.PUNCH_KING.getValue());
      mplew.writeInt(type);
      switch(type) {
      case 0:
      case 1:
         mplew.writeInt(args[0]);
         break;
      case 2:
         mplew.writeInt(args[0]);
         break;
      case 3:
         mplew.writeInt(args[0]);
         mplew.writeInt(args[1]);
      }

      return mplew.getPacket();
   }

   public static byte[] boatPacket(int effect, int mode) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.BOAT_MOVE.getValue());
      mplew.write(effect);
      mplew.write(mode);
      return mplew.getPacket();
   }

   public static byte[] stopClock() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.STOP_CLOCK.getValue());
      return mplew.getPacket();
   }

   public static byte[] achievementRatio(int amount) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ACHIEVEMENT_RATIO.getValue());
      mplew.writeInt(amount);
      return mplew.getPacket();
   }

   public static byte[] spawnPlayerMapobject(MapleCharacter chr) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPAWN_PLAYER.getValue());
      mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
      mplew.writeInt(0);
      mplew.writeInt(chr.getId());
      mplew.writeInt(chr.getGuildId());
      mplew.writeInt(chr.getLevel());
      mplew.writeMapleAsciiString(chr.getName());
      mplew.writeMapleAsciiString("");
      mplew.writeInt(chr.getGuildId());
      MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
      if (gs != null) {
         mplew.writeMapleAsciiString(gs.getName());
         mplew.writeShort(gs.getLogoBG());
         mplew.write(gs.getLogoBGColor());
         mplew.writeShort(gs.getLogo());
         mplew.write(gs.getLogoColor());
         mplew.writeInt(gs.getCustomEmblem() != null ? gs.getId() : 0);
         mplew.writeInt(gs.getCustomEmblem() != null ? 1 : 0);
      } else {
         mplew.writeLong(0L);
         mplew.writeLong(0L);
      }

      mplew.write(chr.getGender());
      mplew.writeInt(chr.getFame());
      mplew.writeInt(1);
      mplew.writeInt(0);
      mplew.write((int)0);
      mplew.writeInt(0);
      Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
      chr.getEffects().stream().forEach((effect) -> {
         if (effect.getLeft() != SecondaryStat.EnergyCharged) {
            statups.put((SecondaryStat)effect.getLeft(), new Pair(((SecondaryStatValueHolder)effect.getRight()).value, ((SecondaryStatValueHolder)effect.right).localDuration));
         }

      });
      if (chr.getKaiserCombo() > 0) {
         statups.put(SecondaryStat.SmashStack, new Pair(1, 0));
      }

      PacketHelper.writeBuffMask(mplew, (Collection)PacketHelper.sortBuffStats(statups));
      PacketHelper.encodeForRemote(mplew, statups, chr);
      mplew.writeShort(chr.getJob());
      mplew.writeShort(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      AvatarLook.encodeAvatarLook(mplew, chr, true, false);
      if (GameConstants.isZero(chr.getJob())) {
         AvatarLook.encodeAvatarLook(mplew, chr, true, true);
      }

      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.write((int)0);
      mplew.writeInt(0);
      mplew.writeInt(chr.getKeyValue(27038, "itemid") <= 0L ? 0L : chr.getKeyValue(27038, "itemid"));
      mplew.writeInt(0);
      mplew.writeInt(chr.getKeyValue(19019, "id"));
      mplew.write((int)0);
      MapleQuestStatus stat = chr.getQuestNoAdd(MapleQuest.getInstance(7291));
      mplew.writeInt(stat != null && stat.getCustomData() != null ? Integer.valueOf(stat.getCustomData()) : 0);
      mplew.writeInt(stat != null && stat.getCustomData() != null ? Integer.valueOf(stat.getCustomData()) : 0);
      mplew.writeInt(0);
      mplew.writeMapleAsciiString("");
      mplew.writeMapleAsciiString("");
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.write(true);
      mplew.writeInt(0);
      mplew.writeShort(-1);
      mplew.writeInt(chr.getChair());
      mplew.writeInt(0);
      mplew.writePos(chr.getSkillCustomValue(201212) != null ? new Point(chr.getPosition().x, chr.getPosition().y - 30) : chr.getTruePosition());
      mplew.write(chr.getSkillCustomValue(201212) != null ? 0 : chr.getStance());
      mplew.writeShort(chr.getSkillCustomValue(201212) != null ? 0 : chr.getFH());
      mplew.write(chr.getKeyValue(100, "medal") == 1L ? 1 : 0);
      mplew.write(chr.getKeyValue(100, "title") == 1L ? 1 : 0);
      mplew.write(chr.getChair() != 0);
      if (chr.getChair() != 0) {
         PacketHelper.chairPacket(mplew, chr, chr.getChair());
      }

      int petindex = 0;
      int i;
      if (chr.getPets().length > 0 && chr.getMapId() != ServerConstants.warpMap) {
         MaplePet[] var6 = chr.getPets();
         int var7 = var6.length;

         for(i = 0; i < var7; ++i) {
            MaplePet pet = var6[i];
            if (pet != null) {
               mplew.write(true);
               mplew.writeInt(petindex++);
               mplew.writeInt(pet.getPetItemId());
               mplew.writeMapleAsciiString(pet.getName());
               mplew.writeLong(pet.getUniqueId());
               mplew.writeShort(pet.getPos().x);
               mplew.writeShort(pet.getPos().y - 20);
               mplew.write(pet.getStance());
               mplew.writeShort(pet.getFh());
               mplew.writeInt(pet.getColor());
               mplew.writeShort(pet.getWonderGrade());
               mplew.writeShort(pet.getPetSize());
               mplew.write((int)0);
               mplew.write((int)0);
            }
         }
      }

      mplew.write(false);
      mplew.write(false);
      mplew.writeInt(chr.getMount().getLevel());
      mplew.writeInt(chr.getMount().getExp());
      mplew.writeInt(chr.getMount().getFatigue());
      mplew.write((int)0);
      PacketHelper.addAnnounceBox(mplew, chr);
      mplew.write(chr.getChalkboard() != null && chr.getChalkboard().length() > 0 ? 1 : 0);
      if (chr.getChalkboard() != null && chr.getChalkboard().length() > 0) {
         mplew.writeMapleAsciiString(chr.getChalkboard());
      }

      Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> rings = chr.getRings(false);
      addRingInfo(mplew, (List)rings.getLeft());
      addRingInfo(mplew, (List)rings.getMid());
      addMRingInfo(mplew, (List)rings.getRight(), chr);
      mplew.write(true);
      byte flag = 0;
      if (chr.getSkillLevel(1320016) > 0 && chr.getJob() == 132) {
         flag = (byte)(flag | 1);
      }

      if (GameConstants.isEvan(chr.getJob())) {
         flag = (byte)(flag | 2);
      }

      mplew.writeInt(0);
      mplew.write(flag);
      mplew.writeInt(0);
      if (GameConstants.isKaiser(chr.getJob())) {
         mplew.writeInt(chr.getKeyValue(12860, "extern") == -1L ? 0L : chr.getKeyValue(12860, "extern"));
         mplew.writeInt(chr.getKeyValue(12860, "inner") == -1L ? 1L : chr.getKeyValue(12860, "inner"));
         mplew.write(chr.getKeyValue(12860, "premium") == -1L ? 0 : (byte)((int)chr.getKeyValue(12860, "premium")));
      }

      mplew.writeInt(0);
      PacketHelper.addFarmInfo(mplew, chr.getClient(), 0);

      for(i = 0; i < 5; ++i) {
         mplew.write((int)-1);
      }

      mplew.writeInt(0);
      mplew.write((int)1);
      if (chr.getBuffedValue(SecondaryStat.RideVehicle) != null && chr.getBuffedValue(SecondaryStat.RideVehicle) == 1932249) {
         mplew.writeInt(0);
      }

      mplew.write((int)0);
      mplew.write((int)0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.write((int)0);
      mplew.write((int)0);
      mplew.write((int)0);
      mplew.write((int)0);
      mplew.write(chr.getBuffedEffect(SecondaryStat.KinesisPsychicEnergeShield) != null ? 1 : 0);
      mplew.write(chr.getKeyValue(1544, "20040217") == 1L);
      mplew.write(chr.getKeyValue(1544, "20040219") == 1L);
      mplew.write((int)0);
      mplew.writeInt(1051291);
      mplew.write(false);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.write((int)0);
      mplew.writeZeroBytes(50);
      return mplew.getPacket();
   }

   public static byte[] removePlayerFromMap(int cid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.REMOVE_PLAYER_FROM_MAP.getValue());
      mplew.writeInt(cid);
      return mplew.getPacket();
   }

   public static byte[] getChatText(MapleCharacter chr, String text, boolean whiteBG, int show, Item item) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(item == null ? SendPacketOpcode.CHATTEXT.getValue() : SendPacketOpcode.CHATTEXTITEM.getValue());
      int emoticon = 0;
      if (show == 11) {
         emoticon = Integer.parseInt(text.replace(":", ""));
         text = "";
      }

      mplew.writeInt(chr.getId());
      mplew.write(whiteBG ? 1 : 0);
      mplew.writeMapleAsciiString(text);
      PacketHelper.ChatPacket(mplew, chr.getName(), text);
      mplew.writeLong(0L);
      mplew.writeShort(0);
      mplew.write(show);
      mplew.write((int)0);
      if (item != null) {
         mplew.write((int)1);
         mplew.writeInt(1);
         mplew.write(true);
         PacketHelper.addItemInfo(mplew, item);
         mplew.writeMapleAsciiString(MapleItemInformationProvider.getInstance().getName(item.getItemId()));
      } else if (show == 11) {
         mplew.write((int)5);
         mplew.writeInt(emoticon);
      } else {
         mplew.write(false);
      }

      return mplew.getPacket();
   }

   public static byte[] getUniverseChat(boolean disableworldname, String name, String text) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MULTICHAT.getValue() + 2);
      mplew.write(disableworldname ? 1 : 0);
      mplew.writeMapleAsciiString(name);
      mplew.writeMapleAsciiString(text);
      return mplew.getPacket();
   }

   public static byte[] getScrollEffect(int chr, Equip.ScrollResult scrollSuccess, boolean legendarySpirit, int scrollid, int victimid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_SCROLL_EFFECT.getValue());
      mplew.writeInt(chr);
      switch(scrollSuccess) {
      case SUCCESS:
         mplew.write((int)1);
         mplew.write(legendarySpirit ? 1 : 0);
         mplew.writeInt(scrollid);
         mplew.writeInt(victimid);
         break;
      case FAIL:
         mplew.write((int)0);
         mplew.write(legendarySpirit ? 1 : 0);
         mplew.writeInt(scrollid);
         mplew.writeInt(victimid);
         break;
      case CURSE:
         mplew.write((int)2);
         mplew.write(legendarySpirit ? 1 : 0);
         mplew.writeInt(scrollid);
         mplew.writeInt(victimid);
      }

      return mplew.getPacket();
   }

   public static byte[] showMagnifyingEffect(int chr, short pos) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_MAGNIFYING_EFFECT.getValue());
      mplew.writeInt(chr);
      mplew.writeShort(pos);
      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static byte[] showPotentialReset(int chr, boolean success, int itemid, int equipId) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_POTENTIAL_RESET.getValue());
      mplew.writeInt(chr);
      mplew.write(success ? 1 : 0);
      mplew.writeInt(itemid);
      mplew.writeInt(0);
      mplew.writeInt(equipId);
      return mplew.getPacket();
   }

   public static byte[] getRedCubeStart(MapleCharacter chr, Item item, boolean up, int cubeId, int remainCount) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_REDCUBE_EFFECT.getValue());
      mplew.writeInt(chr.getId());
      mplew.write(up);
      mplew.writeInt(cubeId);
      mplew.writeInt(item.getPosition());
      mplew.writeInt(remainCount);
      PacketHelper.addItemInfo(mplew, item);
      return mplew.getPacket();
   }

   public static byte[] getCubeStart(MapleCharacter chr, Item item, boolean up, int cubeId, int remainCount) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      boolean adi = false;
      switch(cubeId) {
      case 2730000:
      case 2730001:
      case 2730002:
      case 2730004:
      case 2730005:
         adi = true;
      case 2730003:
      default:
         mplew.writeShort(adi ? SendPacketOpcode.SHOW_CUBE_EFFECT.getValue() + 1 : SendPacketOpcode.SHOW_CUBE_EFFECT.getValue());
         mplew.writeInt(chr.getId());
         mplew.write(up);
         mplew.writeInt(cubeId);
         mplew.writeInt(item.getPosition());
         mplew.writeInt(remainCount);
         PacketHelper.addItemInfo(mplew, item);
         return mplew.getPacket();
      }
   }

   public static byte[] getEditionalCubeStart(MapleCharacter chr, Item item, boolean up, int cubeId, int remainCount) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_EDITIONALCUBE_EFFECT.getValue());
      mplew.writeInt(chr.getId());
      mplew.write(up);
      mplew.writeInt(cubeId);
      mplew.writeInt(item.getPosition());
      mplew.writeInt(remainCount);
      PacketHelper.addItemInfo(mplew, item);
      return mplew.getPacket();
   }

   public static byte[] getWhiteCubeStart(MapleCharacter chr, Item item, boolean up, int cubeId, int cubePosition) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.WHITE_CUBE_WINDOW.getValue());
      mplew.writeLong(item.getInventoryId() <= 0L ? -1L : item.getInventoryId());
      mplew.write((int)1);
      PacketHelper.addItemInfo(mplew, item);
      mplew.writeInt(cubeId);
      mplew.writeInt(item.getPosition());
      mplew.writeInt(cubePosition);
      mplew.writeZeroBytes(10);
      return mplew.getPacket();
   }

   public static byte[] getBlackCubeStart(MapleCharacter chr, Item item, boolean up, int cubeId, int cubePosition, int remainCount) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.BLACK_CUBE_WINDOW.getValue());
      mplew.writeLong(item.getInventoryId() <= 0L ? -1L : item.getInventoryId());
      mplew.write((int)1);
      PacketHelper.addItemInfo(mplew, item);
      mplew.writeInt(cubeId);
      mplew.writeInt(item.getPosition());
      mplew.writeInt(remainCount);
      mplew.writeInt(cubePosition);
      mplew.writeZeroBytes(10);
      return mplew.getPacket();
   }

   public static byte[] getBlackCubeEffect(int cid, boolean up, int cubeId, int equipId) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_BLACKCUBE_EFFECT.getValue());
      mplew.writeInt(cid);
      mplew.write((int)1);
      mplew.writeInt(cubeId);
      mplew.writeInt(2460000);
      mplew.writeInt(equipId);
      return mplew.getPacket();
   }

   public static byte[] getAnvilStart(Item item) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
      mplew.write((int)0);
      mplew.write((int)0);
      mplew.write((int)2);
      mplew.write((int)0);
      mplew.write((int)3);
      mplew.write((int)1);
      mplew.writeShort(item.getPosition());
      mplew.write((int)0);
      mplew.write((int)0);
      mplew.write((int)1);
      mplew.writeShort(item.getPosition());
      PacketHelper.addItemInfo(mplew, item);
      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static byte[] showEnchanterEffect(int cid, byte result) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_ENCHANTER_EFFECT.getValue());
      mplew.writeInt(cid);
      mplew.write(result);
      return mplew.getPacket();
   }

   public static byte[] showSoulScrollEffect(int cid, byte result, boolean destroyed, Equip equip) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_SOULSCROLL_EFFECT.getValue());
      mplew.writeInt(cid);
      mplew.write(result);
      mplew.write(destroyed ? 1 : 0);
      mplew.writeInt(equip.getItemId());
      mplew.writeInt(equip.getSoulPotential());
      return mplew.getPacket();
   }

   public static byte[] showSoulEffect(MapleCharacter chr, byte on) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.SHOW_SOULEFFECT_RESPONSE.getValue());
      packet.writeInt(chr.getId());
      packet.write(on);
      return packet.getPacket();
   }

   public static byte[] showSoulEffect(MapleCharacter chr, byte use, int skillid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_SOUL_EFFECT.getValue());
      mplew.writeInt(use);
      mplew.writeInt(skillid);
      mplew.writeInt(chr.getId());
      return mplew.getPacket();
   }

   public static byte[] teslaTriangle(int cid, int sum1, int sum2, int sum3) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.TESLA_TRIANGLE.getValue());
      mplew.writeInt(cid);
      mplew.writeInt(sum1);
      mplew.writeInt(sum2);
      mplew.writeInt(sum3);
      return mplew.getPacket();
   }

   public static byte[] harvestResult(int cid, boolean success) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.HARVESTED.getValue());
      mplew.writeInt(cid);
      mplew.write(success ? 0 : 1);
      return mplew.getPacket();
   }

   public static byte[] playerDamaged(int cid, int dmg) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.PLAYER_DAMAGED.getValue());
      mplew.writeInt(cid);
      mplew.writeInt(dmg);
      mplew.write(false);
      return mplew.getPacket();
   }

   public static byte[] spawnDragon(MapleDragon d) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DRAGON_SPAWN.getValue());
      mplew.writeInt(d.getOwner());
      mplew.writeInt(d.getPosition().x);
      mplew.writeInt(d.getPosition().y);
      mplew.write(d.getStance());
      mplew.writeShort(0);
      mplew.writeShort(d.getJobId());
      return mplew.getPacket();
   }

   public static byte[] removeDragon(int chrid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DRAGON_REMOVE.getValue());
      mplew.writeInt(chrid);
      return mplew.getPacket();
   }

   public static byte[] moveDragon(MapleDragon d, Point startPos, List<LifeMovementFragment> moves) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DRAGON_MOVE.getValue());
      mplew.writeInt(d.getOwner());
      mplew.writeInt(0);
      mplew.writePos(startPos);
      mplew.writeInt(0);
      PacketHelper.serializeMovementList(mplew, moves);
      return mplew.getPacket();
   }

   public static byte[] spawnAndroid(MapleCharacter cid, MapleAndroid android) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ANDROID_SPAWN.getValue());
      mplew.writeInt(cid.getId());
      mplew.write(GameConstants.getAndroidType(android.getItemId()));
      mplew.writePos(cid.getSkillCustomValue(201212) != null ? new Point(android.getPos().x, android.getPos().y - 30) : android.getPos());
      mplew.write(cid.getSkillCustomValue(201212) != null ? 0 : cid.getStance());
      mplew.writeShort(cid.getSkillCustomValue(201212) != null ? 0 : cid.getFH());
      mplew.writeInt(0);
      mplew.writeShort(android.getSkin());
      mplew.writeShort(android.getHair());
      mplew.writeShort(0);
      mplew.writeShort(android.getFace());
      mplew.writeShort(0);
      mplew.writeMapleAsciiString(android.getName());
      mplew.writeInt(android.getEar() ? 0 : 1032024);
      mplew.writeLong(PacketHelper.getTime(-2L));

      for(short i = -1200; i > -1207; --i) {
         Item item = cid.getInventory(MapleInventoryType.EQUIPPED).getItem(i);
         mplew.writeInt(item != null ? item.getItemId() : 0);
      }

      return mplew.getPacket();
   }

   public static byte[] moveAndroid(int cid, Point pos, List<LifeMovementFragment> res, int unk1, int unk2) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ANDROID_MOVE.getValue());
      mplew.writeInt(cid);
      mplew.writeInt(0);
      mplew.writePos(pos);
      mplew.writeInt(0);
      PacketHelper.serializeMovementList(mplew, res);
      return mplew.getPacket();
   }

   public static byte[] showAndroidEmotion(int cid, int animation) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ANDROID_EMOTION.getValue());
      mplew.writeInt(cid);
      mplew.write((int)0);
      mplew.write(animation);
      return mplew.getPacket();
   }

   public static byte[] spawnHaku(MapleCharacter cid, MapleHaku haku) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.HAKU_SPAWN.getValue());
      mplew.writeInt(cid.getId());
      mplew.writeShort(1);
      mplew.writePos(haku.getPos());
      mplew.write(haku.getStance());
      mplew.writeShort(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] moveHaku(int cid, Point pos, List<LifeMovementFragment> res) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.HAKU_MOVE.getValue());
      mplew.writeInt(cid);
      mplew.writeInt(0);
      mplew.writePos(pos);
      mplew.writeInt(Integer.MAX_VALUE);
      PacketHelper.serializeMovementList(mplew, res);
      return mplew.getPacket();
   }

   public static byte[] updateAndroidLook(boolean itemOnly, MapleCharacter cid, MapleAndroid android) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ANDROID_UPDATE.getValue());
      mplew.writeInt(cid.getId());
      mplew.write(itemOnly ? 1 : 0);
      if (itemOnly) {
         for(short i = -1200; i > -1207; --i) {
            Item item = cid.getInventory(MapleInventoryType.EQUIPPED).getItem(i);
            mplew.writeInt(item != null ? item.getItemId() : 0);
         }
      } else {
         mplew.writeShort(0);
         mplew.writeShort(android.getHair() - 30000);
         mplew.writeShort(android.getFace() - 20000);
         mplew.writeMapleAsciiString(android.getName());
      }

      return mplew.getPacket();
   }

   public static byte[] deactivateAndroid(int cid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ANDROID_DEACTIVATED.getValue());
      mplew.writeInt(cid);
      return mplew.getPacket();
   }

   public static byte[] NameChanger(byte status) {
      return NameChanger(status, 0);
   }

   public static byte[] NameChanger(byte status, int itemid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.NAME_CHANGER.getValue());
      mplew.write(status);
      if (status == 9) {
         mplew.writeInt(itemid);
      }

      return mplew.getPacket();
   }

   public static byte[] movePlayer(int cid, List<LifeMovementFragment> moves, Point startPos) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MOVE_PLAYER.getValue());
      mplew.writeInt(cid);
      mplew.writeInt(0);
      mplew.writePos(startPos);
      mplew.writePos(new Point(0, 0));
      PacketHelper.serializeMovementList(mplew, moves);
      mplew.writeZeroBytes(100);
      return mplew.getPacket();
   }

   public static byte[] addAttackInfo(int type, MapleCharacter chr, AttackInfo attack) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      if (type == 0) {
         mplew.writeShort(SendPacketOpcode.CLOSE_RANGE_ATTACK.getValue());
      } else if (type != 1 && type != 2) {
         if (type == 3) {
            mplew.writeShort(SendPacketOpcode.MAGIC_ATTACK.getValue());
         } else {
            mplew.writeShort(SendPacketOpcode.BUFF_ATTACK.getValue());
         }
      } else {
         mplew.writeShort(SendPacketOpcode.RANGED_ATTACK.getValue());
      }

      mplew.writeInt(chr.getId());
      mplew.write(GameConstants.isEvan(chr.getJob()));
      mplew.write(attack.tbyte);
      mplew.writeInt(chr.getLevel());
      mplew.writeInt(attack.skilllevel);
      if (attack.skilllevel > 0) {
         mplew.writeInt(attack.skill);
      }

      if (GameConstants.isZeroSkill(attack.skill)) {
         mplew.write(attack.asist);
         if (attack.asist > 0) {
            mplew.writePos(attack.position);
         }
      }

      int passiveId;
      if ((type == 1 || type == 2) && (GameConstants.bullet_count_bonus(attack.skill) != 0 || GameConstants.attack_count_bonus(attack.skill) != 0)) {
         int passiveId = false;
         int passiveLv = false;
         int passiveLv;
         if (GameConstants.bullet_count_bonus(attack.skill) == 0) {
            if (GameConstants.attack_count_bonus(attack.skill) == 0) {
               passiveId = 0;
               passiveLv = 0;
            } else {
               passiveId = GameConstants.attack_count_bonus(attack.skill);
               passiveLv = chr.getSkillLevel(passiveId);
            }
         } else {
            passiveId = GameConstants.bullet_count_bonus(attack.skill);
            passiveLv = chr.getSkillLevel(passiveId);
         }

         mplew.writeInt(passiveLv);
         if (passiveLv != 0) {
            mplew.writeInt(passiveId);
         }
      }

      if (attack.skill == 80001850) {
         passiveId = chr.getSkillLevel(80001851);
         mplew.writeInt(passiveId);
         if (passiveId != 0) {
            mplew.writeInt(80001851);
         }
      }

      mplew.write(attack.skill != 5220023 && attack.skill != 5220024 && attack.skill != 5220025 && attack.skill != 95001000 && attack.skill != 21001008 ? attack.isShadowPartner : 4);
      mplew.write(attack.isBuckShot);
      mplew.writeInt(0);
      mplew.writeInt(attack.summonattack > 0 ? attack.summonattack : 0);
      mplew.writeInt(attack.count);
      mplew.write((int)0);
      if ((attack.isBuckShot & 2) != 0) {
         if (chr.getBuffedValue(SecondaryStat.Buckshot) == null) {
            mplew.writeInt(0);
            mplew.writeInt(0);
         } else {
            mplew.writeInt(chr.getBuffSource(SecondaryStat.Buckshot));
            mplew.writeInt(chr.getBuffedValue(SecondaryStat.Buckshot));
         }
      }

      if ((attack.isBuckShot & 8) != 0) {
         mplew.write(attack.skilllevel);
      }

      mplew.write(attack.display);
      mplew.write(attack.facingleft);
      mplew.write(attack.nMoveAction);
      if (GameConstants.isZero(chr.getJob()) && chr.getGender() == 1) {
         mplew.writeShort(0);
         mplew.writeShort(0);
      } else if (attack.position != null && !GameConstants.isZeroSkill(attack.skill) && attack.skill != 400031016) {
         mplew.writeShort(attack.position.x);
         mplew.writeShort(attack.position.y);
      } else {
         mplew.writeShort(0);
         mplew.writeShort(0);
      }

      mplew.write(attack.isLink);
      mplew.write(attack.bShowFixedDamage);
      mplew.write(attack.speed);
      mplew.write(chr.getStat().passive_mastery());
      mplew.writeInt(attack.item);
      Iterator var10 = attack.allDamage.iterator();

      while(true) {
         AttackPair oned;
         do {
            do {
               if (!var10.hasNext()) {
                  if (attack.skill == 2321001 || attack.skill == 2221052 || attack.skill == 11121052 || attack.skill == 12121054) {
                     mplew.writeInt(attack.charge);
                  }

                  if (GameConstants.is_super_nova_skill(attack.skill) || GameConstants.is_screen_attack(attack.skill) || attack.skill == 101000202 || attack.skill == 101000102 || GameConstants.is_thunder_rune(attack.skill) || attack.skill == 400041019 || attack.skill == 400031016 || attack.skill == 400041024 || GameConstants.sub_84ABA0(attack.skill) || attack.skill == 400021075 || attack.skill == 400001055 || attack.skill == 400001056) {
                     mplew.writeInt(attack.position.x);
                     mplew.writeInt(attack.position.y);
                  }

                  if (attack.skill == 80002452) {
                     mplew.writeInt(attack.position.x);
                     mplew.writeInt(attack.position.y);
                  }

                  if (GameConstants.sub_8327B0(attack.skill) && attack.skill != 13111020) {
                     mplew.writePos(attack.plusPosition2);
                  }

                  if (attack.skill != 63111004 && attack.skill != 63111104 && attack.skill != 80003017) {
                     if (attack.skill != 63111005 && attack.skill != 63111105 && attack.skill != 63111106 && attack.skill != 400051075) {
                        if (attack.skill == 400031059) {
                           mplew.writePosInt(attack.plusPosition2);
                        }
                     } else {
                        if (attack.skill == 400051075) {
                           mplew.write(attack.rlType);
                        }

                        mplew.writePosInt(attack.plusPosition2);
                     }
                  } else {
                     mplew.writePos(attack.position);
                  }

                  if (attack.skill == 51121009) {
                     mplew.write(attack.bShowFixedDamage);
                  }

                  if (attack.skill == 21120019 || attack.skill == 37121052 || GameConstants.is_shadow_assult(attack.skill) || attack.skill == 11121014 || attack.skill == 5101004) {
                     mplew.write(attack.plusPos);
                     mplew.writeInt(attack.plusPosition.x);
                     mplew.writeInt(attack.plusPosition.y);
                  }

                  if (GameConstants.sub_7FB860(attack.skill)) {
                     mplew.writePos(attack.position);
                     if (GameConstants.is_pathfinder_blast_skill(attack.skill)) {
                        mplew.writeInt(attack.skilllevel);
                        mplew.write((int)0);
                     }
                  }

                  if (GameConstants.sub_6F5530(attack.skill)) {
                     mplew.writeInt(attack.skilllevel);
                     mplew.write((int)0);
                  }

                  if (attack.skill == 155101104 || attack.skill == 155101204 || attack.skill == 400051042 || attack.skill == 151101003 || attack.skill == 151101004) {
                     mplew.write(attack.across);
                     if (attack.across) {
                        mplew.writeInt(attack.acrossPosition.width);
                        mplew.writeInt(attack.acrossPosition.height);
                     }
                  }

                  if (attack.skill == 23121011 || attack.skill == 80001913) {
                     mplew.write((int)0);
                  }

                  mplew.writeInt(0);
                  mplew.writeInt(0);
                  mplew.writeInt(0);
                  mplew.writeInt(0);
                  mplew.writeInt(0);
                  mplew.writeZeroBytes(10);
                  return mplew.getPacket();
               }

               oned = (AttackPair)var10.next();
            } while(oned.attack == null);

            mplew.writeInt(oned.objectId);
         } while(oned.objectId == 0);

         mplew.write((int)7);
         mplew.write((int)0);
         mplew.write((int)0);
         mplew.writeShort(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         Iterator var6;
         Pair eachd;
         if (attack.skill == 80001835) {
            mplew.write(oned.attack.size());
            var6 = oned.attack.iterator();

            while(var6.hasNext()) {
               eachd = (Pair)var6.next();
               mplew.writeLong((Long)eachd.left);
            }
         } else {
            var6 = oned.attack.iterator();

            label259:
            while(true) {
               while(true) {
                  if (!var6.hasNext()) {
                     break label259;
                  }

                  eachd = (Pair)var6.next();
                  if (!(Boolean)eachd.right && chr.getStat().getCritical_rate() < 100 && chr.getSkillCustomValue(3310005) == null && !Randomizer.isSuccess(chr.getStat().getCritical_rate())) {
                     mplew.writeLong((Long)eachd.left);
                  } else {
                     mplew.writeLong((Long)eachd.left | -9223372036854775807L);
                  }
               }
            }
         }

         if (sub_6F2500(attack.skill) > 0) {
            mplew.writeInt(0);
         }

         if (attack.skill == 37111005) {
            mplew.write(chr.getPosition().x < attack.position.x ? 1 : 0);
         } else if (attack.skill == 164001002) {
            mplew.writeInt(0);
         }
      }
   }

   public static int sub_6F2500(int a1) {
      if (a1 > 142111002) {
         if (a1 < 142120000 || a1 > 142120002 && a1 != 142120014) {
            return 0;
         }
      } else if (a1 != 142111002 && a1 != 142100010 && a1 != 142110003 && a1 != 142110015) {
         return 0;
      }

      return 1;
   }

   public static byte[] skillEffect(MapleCharacter from, int skillid, int level, short display, byte unk) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SKILL_EFFECT.getValue());
      mplew.writeInt(from.getId());
      mplew.writeInt(skillid);
      mplew.write(level);
      mplew.writeShort(display);
      mplew.write(unk);
      if (skillid == 13111020) {
         mplew.writePos(from.getTruePosition());
      }

      return mplew.getPacket();
   }

   public static byte[] skillCancel(MapleCharacter from, int skillid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CANCEL_SKILL_EFFECT.getValue());
      mplew.writeInt(from.getId());
      mplew.writeInt(skillid);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] damagePlayer(int skill, int monsteridfrom, int cid, int damage) {
      return damagePlayer(cid, skill, damage, monsteridfrom, (byte)0, 0, 0, false, 0, (byte)0, (Point)null, (byte)0, 0, 0);
   }

   public static byte[] damagePlayer(int cid, int type, int damage, int monsteridfrom, byte direction, int skillid, int pDMG, boolean pPhysical, int pID, byte pType, Point pPos, byte offset, int offset_d, int fake) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DAMAGE_PLAYER.getValue());
      mplew.writeInt(cid);
      mplew.write(type);
      mplew.writeInt(damage);
      mplew.write((int)0);
      mplew.write(false);
      mplew.write((int)0);
      if (type == -8) {
         mplew.writeInt(skillid);
         mplew.writeInt(pDMG);
         mplew.writeInt(0);
      } else if (type >= -1) {
         mplew.writeInt(monsteridfrom);
         mplew.write(direction);
         mplew.writeInt(0);
         mplew.writeInt(skillid);
         mplew.writeInt(pDMG);
         mplew.write((int)0);
         if (pDMG > 0) {
            mplew.write(pPhysical ? 1 : 0);
            mplew.writeInt(pID);
            mplew.write(pType);
            mplew.writePos(pPos);
         }

         mplew.write(offset);
         if ((offset & 1) != 0) {
            mplew.writeInt(offset_d);
         }
      }

      mplew.writeInt(damage);
      if (fake > 0) {
         mplew.writeInt(fake);
      }

      mplew.writeZeroBytes(10);
      return mplew.getPacket();
   }

   public static byte[] facialExpression(MapleCharacter from, int expression) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FACIAL_EXPRESSION.getValue());
      mplew.writeInt(from.getId());
      mplew.writeInt(expression);
      mplew.writeInt(-1);
      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static byte[] ChangeFaceMotion(int type, int time) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CHANGE_FACE_MOTION.getValue());
      mplew.writeInt(type);
      mplew.writeInt(time);
      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static byte[] itemEffect(int characterid, int itemid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_ITEM_EFFECT.getValue());
      mplew.writeInt(characterid);
      mplew.writeInt(itemid);
      return mplew.getPacket();
   }

   public static byte[] showTitle(int characterid, int itemid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_TITLE.getValue());
      mplew.writeInt(characterid);
      mplew.writeInt(itemid);
      mplew.write(false);
      return mplew.getPacket();
   }

   public static void specialChairPacket(MaplePacketLittleEndianWriter mplew, MapleSpecialChair chair) {
      mplew.writeInt(chair.getItemId());
      mplew.writeInt(chair.getPlayers().size());
      mplew.writeRect(chair.getRect());
      mplew.writeInt(chair.getPoint().x);
      mplew.writeInt(chair.getPoint().y);
      mplew.writeInt(chair.getPlayers().size());

      for(int i = 0; i < chair.getPlayers().size(); ++i) {
         boolean isCharEnable = ((MapleSpecialChair.MapleSpecialChairPlayer)chair.getPlayers().get(i)).getPlayer() != null;
         mplew.writeInt(isCharEnable ? ((MapleSpecialChair.MapleSpecialChairPlayer)chair.getPlayers().get(i)).getPlayer().getId() : 0);
         mplew.write(isCharEnable);
         mplew.writeInt(((MapleSpecialChair.MapleSpecialChairPlayer)chair.getPlayers().get(i)).getEmotion());
      }

   }

   public static byte[] specialChair(MapleCharacter chr, boolean isCreate, boolean isShow, boolean isUpdate, MapleSpecialChair myChair) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPECIAL_CHAIR.getValue());
      mplew.writeInt(chr.getId());
      mplew.write(isCreate);
      mplew.write(isShow);
      if (isShow) {
         mplew.writeInt(chr.getMap().getAllSpecialChairs().size());
         Iterator var6 = chr.getMap().getAllSpecialChairs().iterator();

         while(var6.hasNext()) {
            MapleSpecialChair chair = (MapleSpecialChair)var6.next();
            mplew.writeInt(chair.getObjectId());
            mplew.write(isCreate);
            if (isCreate) {
               specialChairPacket(mplew, chair);
            }
         }
      } else {
         mplew.writeInt(myChair.getObjectId());
         mplew.write(isUpdate);
         if (isUpdate) {
            specialChairPacket(mplew, myChair);
         }
      }

      return mplew.getPacket();
   }

   public static byte[] showChair(MapleCharacter chr, int itemid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_CHAIR.getValue());
      mplew.writeInt(chr.getId());
      mplew.writeInt(itemid);
      mplew.write(itemid != 0);
      if (itemid != 0) {
         PacketHelper.chairPacket(mplew, chr, itemid);
      }

      if (itemid == 3018599 || itemid == 3015798 || itemid == 3018352 || itemid == 3018464 || itemid == 3015520) {
         mplew.writeShort(0);
      }

      return mplew.getPacket();
   }

   public static byte[] updateCharLook(MapleCharacter chr, boolean DressUp) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_LOOK.getValue());
      mplew.writeInt(chr.getId());
      byte flag = 1;
      if (GameConstants.isZero(chr.getJob())) {
         flag = (byte)(flag + 8);
      }

      mplew.write(flag);
      AvatarLook.encodeAvatarLook(mplew, chr, false, DressUp);
      if (GameConstants.isZero(chr.getJob())) {
         AvatarLook.encodeAvatarLook(mplew, chr, false, !DressUp);
      }

      Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> rings = chr.getRings(false);
      addRingInfo(mplew, (List)rings.getLeft());
      addRingInfo(mplew, (List)rings.getMid());
      addMRingInfo(mplew, (List)rings.getRight(), chr);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeZeroBytes(10);
      return mplew.getPacket();
   }

   public static byte[] ZeroTagUpdateCharLook(MapleCharacter chr) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_LOOK.getValue());
      mplew.writeInt(chr.getId());
      byte flag = 1;
      if (GameConstants.isZero(chr.getJob())) {
         flag = (byte)(flag + 8);
      }

      mplew.write(flag);
      AvatarLook.encodeAvatarLook(mplew, chr, false, chr.getGender() == 1);
      if (GameConstants.isZero(chr.getJob())) {
         AvatarLook.encodeAvatarLook(mplew, chr, false, chr.getGender() != 1);
      }

      Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> rings = chr.getRings(false);
      addRingInfo(mplew, (List)rings.getLeft());
      addRingInfo(mplew, (List)rings.getMid());
      addMRingInfo(mplew, (List)rings.getRight(), chr);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeZeroBytes(10);
      return mplew.getPacket();
   }

   public static byte[] updatePartyMemberHP(int cid, int curhp, int maxhp) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.UPDATE_PARTYMEMBER_HP.getValue());
      mplew.writeInt(cid);
      mplew.writeInt(curhp);
      mplew.writeInt(maxhp);
      return mplew.getPacket();
   }

   public static byte[] loadGuildName(MapleCharacter chr) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.LOAD_GUILD_NAME.getValue());
      mplew.writeInt(chr.getId());
      if (chr.getGuildId() <= 0) {
         mplew.writeShort(0);
      } else {
         MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
         if (gs != null) {
            mplew.writeMapleAsciiString(gs.getName());
         } else {
            mplew.writeShort(0);
         }
      }

      return mplew.getPacket();
   }

   public static byte[] loadGuildIcon(MapleCharacter chr) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.LOAD_GUILD_ICON.getValue());
      mplew.writeInt(chr.getId());
      if (chr.getGuildId() <= 0) {
         mplew.writeZeroBytes(12);
         mplew.writeInt(0);
      } else {
         MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
         if (gs != null) {
            mplew.writeInt(gs.getId());
            mplew.writeMapleAsciiString(gs.getName());
            mplew.writeShort(gs.getLogoBG());
            mplew.write(gs.getLogoBGColor());
            mplew.writeShort(gs.getLogo());
            mplew.write(gs.getLogoColor());
            mplew.writeInt(gs.getCustomEmblem() != null && gs.getCustomEmblem().length > 0 ? 1 : 0);
            if (gs.getCustomEmblem() != null && gs.getCustomEmblem().length > 0) {
               mplew.writeInt(gs.getId());
               mplew.writeInt(gs.getCustomEmblem().length);
               mplew.write(gs.getCustomEmblem());
            }
         } else {
            mplew.writeZeroBytes(12);
            mplew.writeInt(0);
         }
      }

      return mplew.getPacket();
   }

   public static byte[] showHarvesting(int cid, int tool) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_HARVEST.getValue());
      mplew.writeInt(cid);
      if (tool > 0) {
         mplew.writeInt(1);
         mplew.writeInt(tool);
      } else {
         mplew.writeInt(0);
      }

      return mplew.getPacket();
   }

   public static byte[] cancelChair(int id, MapleCharacter chr) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CANCEL_CHAIR.getValue());
      mplew.writeInt(chr.getId());
      mplew.write(id != -1);
      if (id != -1) {
         mplew.writeShort(id);
      }

      return mplew.getPacket();
   }

   public static byte[] instantMapWarp(MapleCharacter chr, byte portal) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CURRENT_MAP_WARP.getValue());
      mplew.write((int)0);
      mplew.write(portal);
      mplew.writeInt(chr.getMapId() == 993192600 ? 12 : chr.getId());
      if (portal != 0) {
         mplew.writeShort(chr.getMap().getPortal(portal).getPosition().x);
         mplew.writeShort(chr.getMap().getPortal(portal).getPosition().y - 20);
      }

      mplew.writeZeroBytes(10);
      return mplew.getPacket();
   }

   public static byte[] sendHint(String hint, int width, int height) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.PLAYER_HINT.getValue());
      mplew.writeMapleAsciiString(hint);
      mplew.writeShort(width < 1 ? Math.max(hint.length() * 10, 40) : width);
      mplew.writeShort(Math.max(height, 5));
      mplew.write((int)1);
      return mplew.getPacket();
   }

   public static byte[] aranCombo(int value) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.AranCombo.getValue());
      mplew.writeInt(value);
      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static byte[] rechargeCombo(int value) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.AranCombo_RECHARGE.getValue());
      mplew.writeInt(value);
      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static byte[] getGameMessage(int type, String msg) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.GAME_MESSAGE.getValue());
      mplew.writeShort(type);
      mplew.writeMapleAsciiString(msg);
      return mplew.getPacket();
   }

   public static byte[] createUltimate(int amount) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CREATE_ULTIMATE.getValue());
      mplew.writeInt(amount);
      return mplew.getPacket();
   }

   public static byte[] harvestMessage(int oid, int msg) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.HARVEST_MESSAGE.getValue());
      mplew.writeInt(oid);
      mplew.writeInt(msg);
      return mplew.getPacket();
   }

   public static byte[] openBag(int index, int itemId, boolean firstTime) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.OPEN_BAG.getValue());
      mplew.writeInt(index);
      mplew.writeInt(itemId);
      mplew.writeShort(1);
      return mplew.getPacket();
   }

   public static byte[] fireBlink(int cid, Point pos) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CURRENT_MAP_WARP.getValue());
      mplew.write((int)0);
      mplew.write((int)2);
      mplew.writeInt(cid);
      mplew.writePos(pos);
      return mplew.getPacket();
   }

   public static byte[] fireBlinkMulti(MapleCharacter chr, boolean warp) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FIRE_BLINK.getValue());
      mplew.writeInt(chr.getId());
      mplew.write((int)0);
      mplew.write(warp ? 1 : 0);
      mplew.writeShort(5);
      mplew.writeShort(chr.getPosition().x);
      mplew.writeShort(chr.getPosition().y);
      mplew.writeShort(chr.getPosition().x);
      mplew.writeShort(chr.getPosition().y + 15);
      return mplew.getPacket();
   }

   public static byte[] CreateJupiterThunder(MapleCharacter chr, int skillid, Point pos, int... args) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPAWN_ORB.getValue());
      mplew.writeInt(chr.getId());
      mplew.writeInt(1);
      mplew.write((int)1);
      mplew.writeInt(1);
      mplew.writeInt(Randomizer.rand(2, 500000));
      mplew.writeInt(chr.getId());
      mplew.writePosInt(pos);
      mplew.writeInt(chr.isFacingLeft() ? -18 : 18);
      mplew.writeInt(args[1]);
      mplew.writeInt(skillid);
      mplew.writeInt(args[2]);
      mplew.writeInt(args[3]);
      mplew.writeInt(args[4]);
      mplew.writeInt(args[5]);
      mplew.writeInt(args[6]);
      mplew.writeInt(args[7]);
      return mplew.getPacket();
   }

   public static byte[] skillCooldown(int sid, int time) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.COOLDOWN.getValue());
      mplew.writeInt(1);
      mplew.writeInt(sid != 25121133 && sid != 400041051 && sid != 152110004 && sid != 400011135 && sid != 400001063 ? GameConstants.getLinkedSkill(sid) : sid);
      mplew.writeInt(time);
      return mplew.getPacket();
   }

   public static byte[] skillCooldown(Map<Integer, Integer> datas) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.COOLDOWN.getValue());
      mplew.writeInt(datas.size());
      Iterator var2 = datas.entrySet().iterator();

      while(var2.hasNext()) {
         Entry<Integer, Integer> data = (Entry)var2.next();
         mplew.writeInt(GameConstants.getLinkedSkill((Integer)data.getKey()));
         mplew.writeInt((Integer)data.getValue());
      }

      return mplew.getPacket();
   }

   public static byte[] dropItemFromMapObject(MapleMap map, MapleMapItem drop, Point dropfrom, Point dropto, byte mod, boolean pickPocket) {
      return dropItemFromMapObject(map, drop, dropfrom, dropto, mod, pickPocket, 0, (byte)0);
   }

   public static byte[] dropItemFromMapObject(MapleMap map, MapleMapItem drop, Point dropfrom, Point dropto, byte mod, boolean pickPocket, int delay, byte bloody) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DROP_ITEM_FROM_MAPOBJECT.getValue());
      boolean spitem = false;
      switch(drop.getItemId()) {
      case 2022570:
      case 2022571:
      case 2022572:
      case 2022573:
      case 2022574:
      case 4001847:
      case 4001849:
         spitem = true;
         break;
      case 2432391:
      case 2432392:
      case 2432393:
      case 2432394:
      case 2432395:
      case 2432396:
      case 2432397:
      case 2432398:
         if (map.getElitebossrewardtype() == 2 || map.getElitebossrewardtype() == 3) {
            drop.setFlyingDrop(true);
            drop.setTouchDrop(true);
            drop.setFlyingSpeed(150);
         }
         break;
      case 2632342:
      case 2632343:
      case 2632344:
         drop.setFlyingDrop(true);
         drop.setTouchDrop(true);
         drop.setFlyingSpeed(-235080451);
         spitem = true;
      }

      mplew.write(spitem ? 1 : 0);
      mplew.write(mod);
      mplew.writeInt(drop.getObjectId());
      mplew.write(drop.getMeso() > 0 ? 1 : 0);
      mplew.writeInt(drop.isFlyingDrop() ? 1 : 0);
      mplew.writeInt(drop.getFlyingSpeed());
      mplew.writeInt(drop.getFlyingAngle());
      mplew.writeInt(drop.getItemId());
      mplew.writeInt(drop.getOwner());
      mplew.write(drop.getDropType());
      mplew.writePos(dropto);
      mplew.writeInt(pickPocket ? 4048947 : 3072528);
      mplew.writeInt(0);
      mplew.writeLong(0L);
      mplew.writeInt(0);
      mplew.writeLong(0L);
      mplew.writeInt(0);
      mplew.writeLong(0L);
      mplew.write(drop.getItemId() / 1000000 == 1);
      mplew.write(bloody);
      mplew.write((int)0);
      mplew.write((int)0);
      if (mod != 2) {
         mplew.writePos(dropfrom);
         mplew.writeInt(delay);
      }

      mplew.write((drop.getDropType() != 3 || drop.getItemId() / 1000000 != 1) && drop.getItemId() != 2633609 ? 0 : 1);
      if (drop.getMeso() == 0) {
         PacketHelper.addExpirationTime(mplew, drop.getItem().getExpiration());
      }

      mplew.write(drop.isPlayerDrop() ? 0 : 1);
      mplew.write((int)0);
      mplew.writeShort(0);
      mplew.write((int)0);
      mplew.writeInt(drop.isTouchDrop() ? 1 : 0);
      if (drop.getItemId() / 1000000 == 1 && drop.getMeso() == 0 && drop.getEquip() != null) {
         if (drop.getEquip().getState() <= 4) {
            mplew.write(drop.getEquip().getState());
         } else if (drop.getEquip().getState() <= 20) {
            mplew.write(drop.getEquip().getState() - 16);
         } else {
            mplew.write((int)0);
         }
      } else {
         mplew.write((int)0);
      }

      mplew.write(drop.getItemId() == 2434851);
      mplew.write((int)0);
      mplew.writeInt(0);
      mplew.writeZeroBytes(10);
      return mplew.getPacket();
   }

   public static byte[] explodeDrop(int oid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
      mplew.write((int)4);
      mplew.writeInt(oid);
      mplew.writeShort(0);
      mplew.writeShort(655);
      return mplew.getPacket();
   }

   public static byte[] removeItemFromMap(int oid, int animation, int cid) {
      return removeItemFromMap(oid, animation, cid, 0);
   }

   public static byte[] removeItemFromMap(int oid, int animation, int cid, int index) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
      mplew.write(animation);
      mplew.writeInt(oid);
      switch(animation) {
      case 2:
      case 3:
      case 5:
         mplew.writeInt(cid);
         break;
      case 4:
         mplew.writeShort(0);
      }

      if (animation == 5 || animation == 7) {
         mplew.writeInt(index);
      }

      return mplew.getPacket();
   }

   public static byte[] spawnMist(MapleMist mist) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPAWN_MIST.getValue());
      mplew.writeInt(mist.getObjectId());
      mplew.write(mist.isMobMist() ? 1 : mist.isPoisonMist());
      mplew.writeInt(mist.getOwnerId());
      int skillId = mist.getSourceSkill() != null ? mist.getSourceSkill().getId() : (mist.getMobSkill() != null ? mist.getMobSkill().getSkillId() : 0);
      if (mist.getMobSkill() == null) {
         switch(skillId) {
         case 21121057:
            skillId = 21121068;
            break;
         case 400011058:
            skillId = 400011060;
         }

         mplew.writeInt(skillId);
      } else {
         mplew.writeInt(mist.getMobSkill().getSkillId());
      }

      if (mist.getMobSkill() != null) {
         mplew.writeShort(mist.getMobSkill().getSkillLevel());
      } else {
         mplew.writeShort(mist.getSkillLevel());
      }

      mplew.writeShort(mist.getSkillDelay());
      if (skillId == 186 && (mist.getSkillLevel() == 3 || mist.getSkillLevel() == 5 || mist.getSkillLevel() == 6)) {
         mplew.writeNRect(mist.getBox());
      } else {
         mplew.writeRect(mist.getBox());
         if (skillId == 162111000) {
            mplew.writeInt(mist.getPosition().x + (int)mist.getSource().getLt3().getX());
            mplew.writeInt(mist.getPosition().y + (int)mist.getSource().getLt3().getY());
            mplew.writeInt(mist.getPosition().x + (int)mist.getSource().getRb3().getX());
            mplew.writeInt(mist.getPosition().y + (int)mist.getSource().getRb3().getY());
         }
      }

      if (mist.getMobSkill() != null) {
         mplew.writeInt(mist.getMobSkill().getSkillId() != 186 && mist.getMobSkill().getSkillId() != 227 ? mist.isPoisonMist() : 8);
      } else {
         mplew.writeInt(mist.isPoisonMist());
      }

      if (mist.getTruePosition() != null) {
         mplew.writePos(mist.getTruePosition());
      } else if (mist.getPosition() != null) {
         mplew.writePos(mist.getPosition());
      } else if (mist.getOwner() != null) {
         mplew.writePos(mist.getOwner().getTruePosition());
      } else if (mist.getMob() != null) {
         mplew.writePos(mist.getMob().getTruePosition());
      } else if (mist.getMobSkill().getSkillId() == 183 && mist.getSkillLevel() == 13) {
         mplew.writePos(mist.getTruePosition());
      } else {
         mplew.writeShort(mist.getBox().x);
         mplew.writeShort(mist.getBox().y);
      }

      if (mist.getMobSkill() != null) {
         mplew.writeShort(skillId != 186 || mist.getSkillLevel() != 3 && mist.getSkillLevel() != 5 && mist.getSkillLevel() != 6 ? mist.getPosition().x : mist.getCustomx());
         mplew.writeShort(mist.getMobSkill().getForce());
      } else {
         mplew.writeInt(0);
      }

      mplew.writeInt(mist.getDamup() > 0 ? mist.getDamup() : (skillId == 131 && mist.getSkillLevel() == 28 ? 5 : 0));
      mplew.write(skillId == 131 && mist.getSkillLevel() == 28);
      mplew.writeInt(skillId == 400011060 ? 200 : (mist.getMob() != null && mist.getMob().getId() / 10000 == 895 ? 210 : (skillId == 217 && mist.getSkillLevel() == 21 ? 180 : (skillId == 186 && mist.getSkillLevel() == 3 ? 190 : 0))));
      if (mist.getSource() != null && sub_783400(mist.getSourceSkill().getId())) {
         mplew.write(mist.getRltype() == 0 ? 1 : 0);
      }

      mplew.writeInt(mist.getDuration());
      mplew.writeInt(0);
      mplew.writeInt(0);
      if (mist.getSourceSkill() != null && mist.getSourceSkill().getId() == 2111013) {
         mplew.write((int)1);
      } else {
         mplew.write(mist.getSource() != null ? mist.getSourceSkill().getId() == 151121041 : false);
      }

      mplew.write(false);
      mplew.writeZeroBytes(10);
      return mplew.getPacket();
   }

   public static boolean sub_783400(int a1) {
      if (a1 == 135001012) {
         return true;
      } else {
         boolean v1;
         if (a1 > 35121052) {
            if (a1 == 400020046) {
               return true;
            }

            v1 = a1 == 400020051;
         } else {
            if (a1 == 35121052 || a1 == 33111013 || a1 - 33111013 == 9999) {
               return true;
            }

            v1 = a1 - 33111013 == 10003;
         }

         if (!v1) {
            boolean v2;
            if (a1 > 131001207) {
               if (a1 == 152121041 || a1 == 400001017) {
                  return true;
               }

               v2 = a1 == 400041041;
            } else {
               if (a1 == 131001207 || a1 == 4121015 || a1 == 51120057) {
                  return true;
               }

               v2 = a1 == 131001107;
            }

            if (!v2) {
               return false;
            }
         }

         return true;
      }
   }

   public static byte[] removeMist(MapleMist mist) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.REMOVE_MIST.getValue());
      mplew.writeInt(mist.getObjectId());
      mplew.writeInt(0);
      if (mist.getSourceSkill() != null && mist.getSourceSkill().getId() == 2111003) {
         mplew.write((int)0);
      }

      return mplew.getPacket();
   }

   public static byte[] spawnDoor(int oid, Point pos, boolean animation) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPAWN_DOOR.getValue());
      mplew.write(animation ? 0 : 1);
      mplew.writeInt(oid);
      mplew.writePos(pos);
      return mplew.getPacket();
   }

   public static byte[] removeDoor(int oid, boolean animation) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.REMOVE_DOOR.getValue());
      mplew.write(animation ? 0 : 1);
      mplew.writeInt(oid);
      return mplew.getPacket();
   }

   public static byte[] spawnMechDoor(MechDoor md, boolean animated) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MECH_DOOR_SPAWN.getValue());
      mplew.write(animated ? 0 : 1);
      mplew.writeInt(md.getOwnerId());
      mplew.writePos(md.getTruePosition());
      mplew.write(md.getId());
      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static byte[] removeMechDoor(MechDoor md, boolean animated) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MECH_DOOR_REMOVE.getValue());
      mplew.write(animated ? 0 : 1);
      mplew.writeInt(md.getOwnerId());
      mplew.write(md.getId());
      return mplew.getPacket();
   }

   public static byte[] triggerReactor(MapleReactor reactor, int stance, int cid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.REACTOR_HIT.getValue());
      mplew.writeInt(reactor.getObjectId());
      mplew.write(reactor.getState());
      mplew.writePos(reactor.getTruePosition());
      mplew.writeShort(stance);
      mplew.write((int)0);
      mplew.write((int)7);
      mplew.writeShort(0);
      mplew.write((int)0);
      mplew.writeInt(cid);
      return mplew.getPacket();
   }

   public static byte[] triggerReactor1(MapleReactor reactor, int stance) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.REACTOR_HIT.getValue());
      mplew.writeInt(reactor.getObjectId());
      mplew.write(reactor.getState());
      mplew.writePos(reactor.getTruePosition());
      mplew.writeShort(0);
      mplew.write((int)0);
      mplew.writeInt(stance);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] spawnReactor(MapleReactor reactor) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.REACTOR_SPAWN.getValue());
      mplew.writeInt(reactor.getObjectId());
      mplew.writeInt(reactor.getReactorId());
      mplew.write(reactor.getState());
      mplew.writePos(reactor.getTruePosition());
      mplew.write(reactor.getFacingDirection());
      mplew.writeMapleAsciiString(reactor.getName());
      return mplew.getPacket();
   }

   public static byte[] destroyReactor(MapleReactor reactor) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.REACTOR_DESTROY.getValue());
      mplew.writeInt(reactor.getObjectId());
      mplew.write(false);
      mplew.write(reactor.getState());
      mplew.writePos(reactor.getPosition());
      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static byte[] makeExtractor(int cid, String cname, Point pos, int timeLeft, int itemId, int fee) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPAWN_EXTRACTOR.getValue());
      mplew.writeInt(cid);
      mplew.writeMapleAsciiString(cname);
      mplew.writeInt(pos.x);
      mplew.writeInt(pos.y);
      mplew.writeShort(timeLeft);
      mplew.writeInt(itemId);
      mplew.writeInt(fee);
      return mplew.getPacket();
   }

   public static byte[] removeExtractor(int cid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.REMOVE_EXTRACTOR.getValue());
      mplew.writeInt(cid);
      mplew.writeInt(1);
      return mplew.getPacket();
   }

   public static byte[] showChaosZakumShrine(boolean spawned, int time) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CHAOS_ZAKUM_SHRINE.getValue());
      mplew.write(spawned ? 1 : 0);
      mplew.writeInt(time);
      return mplew.getPacket();
   }

   public static byte[] showChaosHorntailShrine(boolean spawned, int time) {
      return showHorntailShrine(spawned, time);
   }

   public static byte[] showHorntailShrine(boolean spawned, int time) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.HORNTAIL_SHRINE.getValue());
      mplew.write(spawned ? 1 : 0);
      mplew.writeInt(time);
      return mplew.getPacket();
   }

   public static byte[] messengerInvite(String from, int messengerid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
      mplew.write((int)3);
      mplew.writeInt(messengerid);
      mplew.writeMapleAsciiString(from);
      mplew.write((int)1);
      mplew.writeInt(messengerid);
      mplew.writeInt(messengerid);
      mplew.writeInt(messengerid);
      return mplew.getPacket();
   }

   public static byte[] addMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
      mplew.write((int)0);
      mplew.write(position);
      mplew.writeInt(0);
      AvatarLook.encodeAvatarLook(mplew, chr, true, GameConstants.isZero(chr.getJob()) && chr.getGender() == 1);
      mplew.writeMapleAsciiString(from);
      mplew.write(channel);
      mplew.write(position);
      mplew.writeInt(chr.getJob());
      return mplew.getPacket();
   }

   public static byte[] removeMessengerPlayer(int position) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
      mplew.write((int)2);
      mplew.write(position);
      return mplew.getPacket();
   }

   public static byte[] updateMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
      mplew.write((int)8);
      mplew.write(position);
      AvatarLook.encodeAvatarLook(mplew, chr, true, GameConstants.isZero(chr.getJob()) && chr.getGender() == 1);
      mplew.writeMapleAsciiString(from);
      return mplew.getPacket();
   }

   public static byte[] joinMessenger(int position) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
      mplew.write((int)1);
      mplew.write(position);
      return mplew.getPacket();
   }

   public static byte[] messengerChat(String charname, String text) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
      mplew.write((int)6);
      mplew.writeMapleAsciiString(charname);
      mplew.writeMapleAsciiString(text);
      PacketHelper.ChatPacket(mplew, charname, text);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeShort(0);
      return mplew.getPacket();
   }

   public static byte[] ChrlistMap(List<MapleCharacter> chrs) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MESSENGER_SEARCH.getValue());
      mplew.write(chrs.size());
      Iterator var2 = chrs.iterator();

      while(var2.hasNext()) {
         MapleCharacter mapchr = (MapleCharacter)var2.next();
         mplew.writeInt(mapchr.getId());
         mplew.writeMapleAsciiString(mapchr.getName());
      }

      return mplew.getPacket();
   }

   public static byte[] messengerWhisperChat(String charname, String text) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
      mplew.write((int)15);
      mplew.writeMapleAsciiString(charname);
      mplew.writeMapleAsciiString(text);
      PacketHelper.ChatPacket(mplew, charname, text);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeShort(0);
      return mplew.getPacket();
   }

   public static byte[] messengerNote(String text, int mode, int mode2) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
      mplew.write(mode);
      mplew.writeMapleAsciiString(text);
      mplew.write(mode2);
      return mplew.getPacket();
   }

   public static byte[] messengerLike(short like, String charname, String othername) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
      mplew.writeShort(like);
      mplew.writeMapleAsciiString(charname);
      mplew.writeMapleAsciiString(othername);
      return mplew.getPacket();
   }

   public static byte[] resultSkill(MapleCharacter chr, int update) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.UPDATE_SKILLS.getValue());
      mplew.write((int)0);
      mplew.write((int)0);
      mplew.write((int)0);
      mplew.writeShort(chr.getMatrixs().size());
      Iterator var3 = chr.getMatrixs().iterator();

      while(var3.hasNext()) {
         VMatrix matrix = (VMatrix)var3.next();
         mplew.writeInt(matrix.getId());
         mplew.writeInt(matrix.getLevel());
         mplew.writeInt(matrix.getMaxLevel());
         mplew.writeLong(PacketHelper.getTime(-1L));
      }

      mplew.write(update);
      return mplew.getPacket();
   }

   public static byte[] messengerCharInfo(MapleCharacter chr) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
      mplew.write((int)12);
      mplew.writeMapleAsciiString(chr.getName());
      mplew.writeInt(chr.getLevel());
      mplew.writeShort(chr.getJob());
      mplew.writeShort(chr.getSubcategory());
      mplew.writeInt(chr.getFame());
      mplew.writeInt(0);
      if (chr.getGuildId() <= 0) {
         mplew.writeMapleAsciiString("-");
         mplew.writeMapleAsciiString("");
      } else {
         MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
         if (gs != null) {
            mplew.writeMapleAsciiString(gs.getName());
            if (gs.getAllianceId() > 0) {
               MapleGuildAlliance allianceName = World.Alliance.getAlliance(gs.getAllianceId());
               if (allianceName != null) {
                  mplew.writeMapleAsciiString(allianceName.getName());
               } else {
                  mplew.writeMapleAsciiString("");
               }
            } else {
               mplew.writeMapleAsciiString("");
            }
         } else {
            mplew.writeMapleAsciiString("-");
            mplew.writeMapleAsciiString("");
         }
      }

      mplew.write((int)1);
      return mplew.getPacket();
   }

   public static byte[] removeItemFromDuey(boolean remove, int Package) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DUEY.getValue());
      mplew.write((int)24);
      mplew.writeInt(Package);
      mplew.write(remove ? 3 : 4);
      return mplew.getPacket();
   }

   public static byte[] checkFailedDuey() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DUEY.getValue());
      mplew.write((int)9);
      mplew.write((int)-1);
      return mplew.getPacket();
   }

   public static byte[] sendDuey(byte operation, List<MapleDueyActions> packages, List<MapleDueyActions> expired) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DUEY.getValue());
      mplew.write(operation);
      if (packages == null) {
         mplew.write((int)0);
         mplew.write((int)0);
         mplew.write((int)0);
         return mplew.getPacket();
      } else {
         switch(operation) {
         case 9:
            mplew.write((int)1);
            break;
         case 10:
            mplew.write((int)0);
            mplew.write(packages.size());
            Iterator var4 = packages.iterator();

            MapleDueyActions dp;
            while(var4.hasNext()) {
               dp = (MapleDueyActions)var4.next();
               mplew.writeInt(dp.getPackageId());
               mplew.writeAsciiString(dp.getSender(), 13);
               mplew.writeLong((long)dp.getMesos());
               mplew.writeLong(PacketHelper.getTime(dp.getExpireTime()));
               mplew.write(dp.isQuick() ? 1 : 0);
               mplew.writeAsciiString(dp.getContent(), 100);
               mplew.writeZeroBytes(101);
               if (dp.getItem() != null) {
                  mplew.write((int)1);
                  PacketHelper.addItemInfo(mplew, dp.getItem());
               } else {
                  mplew.write((int)0);
               }
            }

            if (expired == null) {
               mplew.write((int)0);
               return mplew.getPacket();
            }

            mplew.write(expired.size());
            var4 = expired.iterator();

            while(var4.hasNext()) {
               dp = (MapleDueyActions)var4.next();
               mplew.writeInt(dp.getPackageId());
               mplew.writeAsciiString(dp.getSender(), 13);
               mplew.writeLong((long)dp.getMesos());
               if (dp.canReceive()) {
                  mplew.writeLong(PacketHelper.getTime(dp.getExpireTime()));
               } else {
                  mplew.writeLong(0L);
               }

               mplew.write(dp.isQuick() ? 1 : 0);
               mplew.writeAsciiString(dp.getContent(), 100);
               mplew.writeZeroBytes(101);
               if (dp.getItem() != null) {
                  mplew.write((int)1);
                  PacketHelper.addItemInfo(mplew, dp.getItem());
               } else {
                  mplew.write((int)0);
               }
            }
         }

         return mplew.getPacket();
      }
   }

   public static byte[] receiveParcel(String from, boolean quick) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DUEY.getValue());
      mplew.write((int)26);
      mplew.writeMapleAsciiString(from);
      mplew.write(quick ? 1 : 0);
      return mplew.getPacket();
   }

   public static byte[] getKeymap(MapleKeyLayout layout) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.KEYMAP.getValue());
      if (layout != null) {
         mplew.write((int)0);
         layout.writeData(mplew);
         mplew.write((int)1);
         mplew.write((int)1);
         mplew.writeZeroBytes(10);
      } else {
         mplew.write((int)1);
         mplew.write((int)1);
         mplew.write((int)1);
         mplew.writeZeroBytes(10);
      }

      return mplew.getPacket();
   }

   public static byte[] petAutoHP(int itemId) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.PET_AUTO_HP.getValue());
      mplew.writeInt(itemId);
      return mplew.getPacket();
   }

   public static byte[] petAutoMP(int itemId) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.PET_AUTO_MP.getValue());
      mplew.writeInt(itemId);
      return mplew.getPacket();
   }

   public static void addRingInfo(MaplePacketLittleEndianWriter mplew, List<MapleRing> rings) {
      mplew.write(rings.size());
      Iterator var2 = rings.iterator();

      while(var2.hasNext()) {
         MapleRing ring = (MapleRing)var2.next();
         mplew.writeLong(ring.getRingId());
         mplew.writeLong(ring.getPartnerRingId());
         mplew.writeInt(ring.getItemId());
      }

   }

   public static void addMRingInfo(MaplePacketLittleEndianWriter mplew, List<MapleRing> rings, MapleCharacter chr) {
      mplew.write(rings.size());
      Iterator var3 = rings.iterator();

      while(var3.hasNext()) {
         MapleRing ring = (MapleRing)var3.next();
         mplew.writeInt(chr.getId());
         mplew.writeInt(ring.getPartnerChrId());
         mplew.writeInt(ring.getItemId());
      }

   }

   public static byte[] updateInnerPotential(byte ability, int skill, int level, int rank) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ENABLE_INNER_ABILITY.getValue());
      mplew.write((int)1);
      mplew.write((int)1);
      mplew.writeShort(ability);
      mplew.writeInt(skill);
      mplew.writeShort(level);
      mplew.writeShort(rank);
      mplew.write((int)1);
      return mplew.getPacket();
   }

   public static byte[] updateInnerAbility(InnerSkillValueHolder skill, int index, boolean last) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.ENABLE_INNER_ABILITY.getValue());
      packet.write(last ? 1 : 0);
      packet.write((int)1);
      packet.writeShort(index);
      packet.writeInt(skill.getSkillId());
      packet.writeShort(skill.getSkillLevel());
      packet.writeShort(skill.getRank());
      packet.write(last ? 1 : 0);
      return packet.getPacket();
   }

   public static byte[] HeadTitle(List<Integer> num) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.HEAD_TITLE.getValue());
      Iterator var2 = num.iterator();

      while(var2.hasNext()) {
         Integer num_ = (Integer)var2.next();
         mplew.writeMapleAsciiString("");
         mplew.write(num_ == 0 ? -1 : num_);
      }

      return mplew.getPacket();
   }

   public static byte[] getInternetCafe(byte type, int time) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.INTERNET_CAFE.getValue());
      mplew.write(type);
      mplew.writeInt(time);
      return mplew.getPacket();
   }

   public static byte[] showSpineScreen(boolean isBinary, boolean isLoop, boolean isPostRender, String path, String animationName, int endDelay, boolean useKey, String key) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
      mplew.write((int)30);
      mplew.write(isBinary);
      mplew.write(isLoop);
      mplew.write(isPostRender);
      mplew.writeInt(endDelay);
      mplew.writeMapleAsciiString(path);
      mplew.writeMapleAsciiString(animationName);
      mplew.writeMapleAsciiString("");
      mplew.write((int)0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.write(useKey);
      if (useKey) {
         mplew.writeMapleAsciiString(key);
      }

      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] endscreen(String str) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
      mplew.write((int)31);
      mplew.writeMapleAsciiString(str);
      mplew.writeInt(0);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] showBlackOutScreen(int delay, String path, String animationName, int unk, int unk2, int unk3) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
      mplew.write((int)23);
      mplew.write((int)0);
      mplew.writeInt(delay);
      mplew.writeMapleAsciiString(path);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(unk);
      mplew.writeMapleAsciiString(animationName);
      mplew.writeInt(unk2);
      mplew.write(true);
      mplew.writeInt(unk3);
      mplew.write((int)0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] removeBlackOutScreen(int delay, String path) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
      mplew.write((int)23);
      mplew.write((int)2);
      mplew.writeInt(delay);
      mplew.writeMapleAsciiString(path);
      mplew.write((int)0);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] removeIntro(String animationName, int delay) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
      mplew.write((int)31);
      mplew.writeMapleAsciiString(animationName);
      mplew.writeInt(delay);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] spawnRune(MapleRune rune, boolean respawn) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(respawn ? SendPacketOpcode.RESPAWN_RUNE.getValue() : SendPacketOpcode.SPAWN_RUNE.getValue());
      mplew.writeInt(respawn ? 1 : 0);
      mplew.writeInt(0);
      mplew.writeInt(2);
      mplew.writeInt(rune.getRuneType());
      mplew.writeInt(rune.getPositionX());
      mplew.writeInt(rune.getPositionY());
      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static byte[] removeRune(MapleRune rune, MapleCharacter chr, int type) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.REMOVE_RUNE.getValue());
      mplew.writeInt(0);
      mplew.writeInt(chr.getId());
      mplew.writeInt(100);
      mplew.write((int)0);
      mplew.write(type);
      return mplew.getPacket();
   }

   public static byte[] RuneAction(int type, int time) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.RUNE_ACTION.getValue());
      mplew.writeInt(type);
      if (type != 8 && type != 9) {
         mplew.writeInt(time);
      } else {
         mplew.write((int)0);
         mplew.writeShort(1);
         String[] test = new String[]{"37 30 88 CF 28 00 00 00 35 50 F1 A0 3A A7 E4 BD 81 4C 10 11 E0 6E EC 4C DF 50 5D CE 93 C8 34 36 EC F6 2A 10 15 E9 EA 7E B8 47 0F 02 2C BA CE 0A", "8F 9A B9 A6 28 00 00 00 A2 67 73 C9 B8 AE 07 D4 31 F2 BC 9E 32 A7 DD FD 4F AC 2F A5 E7 3C B2 B8 A0 0C 7F A3 E6 4E BE 3E 68 2E 4B 22 1D 8A B3 F3", "79 8B F6 47 28 00 00 00 35 08 7E 37 2C 31 8D 34 41 6B 13 79 2E 97 C2 0F F9 E3 A7 83 03 B7 BF F0 11 4C D1 54 AA 18 1A 22 C2 6A 44 E6 38 7B F9 A8", "81 BB B6 FE 28 00 00 00 6A B9 AD 91 7A 82 B8 8C C8 E9 4F 49 01 2C 67 7D FB 07 63 93 85 FB 91 20 F4 F2 94 34 39 7D 5B FA 36 59 A0 A9 B1 7B 45 F1", "89 D7 E1 DC 28 00 00 00 3A 7B BF B3 4B 34 B0 AE DF 52 35 39 84 74 BB D1 A2 36 9F E0 96 10 EC 1D F7 05 63 03 42 65 A6 19 A9 74 C4 BC 6F 39 87 44", "4F 6B B7 BB 28 00 00 00 C3 6A 16 D4 D5 53 23 C9 B7 CF F7 1A 9F 63 90 B5 54 F4 88 9A 34 62 90 72 9E 48 11 59 DE A9 14 2E 25 D2 22 99 54 89 94 DA", "06 89 62 B1 28 00 00 00 56 22 D7 DE 5B 6B CA C3 18 4C 45 C7 AA 1F B3 B3 F9 B4 DD 43 B9 6B BA F4 E2 8E 92 4D F2 62 86 02 4B B8 4F EB 45 2C CB 39", "E7 4D 5A 9F 28 00 00 00 20 F6 83 F0 2C BF 96 ED 0A C7 E1 42 16 08 9C 2A 33 90 2B A2 6A AE DD 08 51 D7 4B 00 1B 6E 1F D5 C0 A7 B7 59 13 BC CE 85", "C2 0B 0B 44 28 00 00 00 89 51 9B 34 8C 9A 88 37 A2 32 6C 7F 11 FE 49 38 57 47 41 2D 3A 90 A3 65 39 73 B9 18 81 7E 8A FB FA 77 79 8E C2 D2 6F D0", "6E 73 EB 28 28 00 00 00 12 EA 2D 58 10 A3 52 5B 6B 92 76 A6 6D 45 F8 1F 47 14 38 0E 47 CE A8 FA 03 6B 43 43 F9 5C 32 E1 32 E6 23 D6 F9 2A 18 3E", "C2 0B 0B 44 28 00 00 00 89 51 9B 34 8C 9A 88 37 A2 32 6C 7F 11 FE 49 38 57 47 41 2D 3A 90 A3 65 39 73 B9 18 81 7E 8A FB FA 77 79 8E C2 D2 6F D0"};
         mplew.write(HexTool.getByteArrayFromHexString(test[Randomizer.rand(0, 10)]));
      }

      return mplew.getPacket();
   }

   public static byte[] showRuneEffect(int type) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.RUNE_EFFECT.getValue());
      mplew.writeInt(type);
      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static byte[] MultiTag(MapleCharacter chr) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ZERO_MUlTITAG.getValue());
      mplew.writeInt(chr.getId());
      AvatarLook.encodeAvatarLook(mplew, chr, false, chr.getGender() == 1);
      return mplew.getPacket();
   }

   public static byte[] MultiTagRemove(int cid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ZERO_MUlTITAG_REMOVE.getValue());
      mplew.writeInt(cid);
      return mplew.getPacket();
   }

   public static byte[] getWpGain(int gain) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
      mplew.write((int)36);
      mplew.writeInt(gain);
      return mplew.getPacket();
   }

   public static byte[] updateWP(int wp) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.WP_UPDATE.getValue());
      mplew.writeInt(wp);
      return mplew.getPacket();
   }

   public static byte[] ZeroScroll(int scroll) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ZERO_SCROLL.getValue());
      mplew.writeInt(scroll);
      if (scroll == 0) {
         mplew.writeInt(50000);
         mplew.writeInt(500);
      } else if (scroll == 1) {
         mplew.writeInt(100000);
         mplew.writeInt(600);
      }

      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] ZeroScrollSend(int scroll) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ZERO_SCROLL_SEND.getValue());
      mplew.writeShort(1);
      mplew.write((int)0);
      mplew.writeInt(scroll);
      return mplew.getPacket();
   }

   public static byte[] ZeroScrollStart() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ZERO_SCROLL_START.getValue());
      return mplew.getPacket();
   }

   public static byte[] WeaponInfo(int type, int level, int action, int weapon, int itemid, int quantity) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ZERO_WEAPON_INFO.getValue());
      mplew.write((int)1);
      mplew.write(action);
      mplew.writeInt(type);
      mplew.writeInt(level);
      mplew.writeInt(weapon + 10001);
      mplew.writeInt(weapon + 1);
      mplew.writeInt(type + 1);
      mplew.writeInt(itemid);
      mplew.writeInt(quantity);
      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static byte[] WeaponLevelUp() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ZERO_WEAPON_UPGRADE.getValue());
      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static byte[] Clothes(int value) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ZERO_TAG.getValue());
      mplew.write((int)0);
      mplew.write((int)1);
      mplew.writeInt(value);
      return mplew.getPacket();
   }

   public static byte[] ZeroTag(MapleCharacter chr, byte Gender, int nowhp, int maxhp) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.ZERO_TAG.getValue());
      int a = chr.getSkillLevel(80000406) > 0 ? chr.getSkillLevel(80000406) : 0;
      packet.writeShort(199);
      packet.write(Gender);
      packet.writeInt(nowhp);
      packet.writeInt(chr.getSkillCustomValue0(101000201));
      packet.writeInt(maxhp);
      packet.writeInt((Gender == 1 ? 100 : 100) + a * 10 + (Gender == 1 && chr.getSkillLevel(101100203) > 0 ? 30 : 0));
      return packet.getPacket();
   }

   public static byte[] Reaction() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
      mplew.write((int)0);
      mplew.writeShort(0);
      return mplew.getPacket();
   }

   public static byte[] OnOffFlipTheCoin(boolean on) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FlipTheCoin.getValue());
      mplew.write(on ? 1 : 0);
      return mplew.getPacket();
   }

   public static byte[] replaceStolenSkill(int base, int skill) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.REPLACE_SKILLS.getValue());
      mplew.write((int)1);
      mplew.write(skill > 0 ? 1 : 0);
      mplew.writeInt(base);
      mplew.writeInt(skill);
      return mplew.getPacket();
   }

   public static byte[] addStolenSkill(int jobNum, int index, int skill, int level) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.UPDATE_STOLEN_SKILLS.getValue());
      mplew.write((int)1);
      mplew.write((int)0);
      mplew.writeInt(jobNum);
      mplew.writeInt(index);
      mplew.writeInt(skill);
      mplew.writeInt(level);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] removeStolenSkill(int jobNum, int index) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.UPDATE_STOLEN_SKILLS.getValue());
      mplew.write((int)1);
      mplew.write((int)3);
      mplew.writeInt(jobNum);
      mplew.writeInt(index);
      return mplew.getPacket();
   }

   public static byte[] viewSkills(MapleCharacter chr) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.TARGET_SKILL.getValue());
      List<Integer> skillz = new ArrayList();
      Iterator i$ = chr.getSkills().keySet().iterator();

      while(i$.hasNext()) {
         Skill sk = (Skill)i$.next();
         if (sk.canBeLearnedBy(chr) && !chr.getStolenSkills().contains(new Pair(sk.getId(), true)) && !chr.getStolenSkills().contains(new Pair(sk.getId(), false))) {
            skillz.add(sk.getId());
         }
      }

      mplew.write((int)1);
      mplew.writeInt(chr.getId());
      mplew.writeInt(skillz.isEmpty() ? 2 : 4);
      mplew.writeInt(chr.getJob());
      mplew.writeInt(skillz.size());
      i$ = skillz.iterator();

      while(i$.hasNext()) {
         int i = (Integer)i$.next();
         mplew.writeInt(i);
      }

      return mplew.getPacket();
   }

   public static byte[] updateCardStack(boolean unk, int total) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.PHANTOM_CARD.getValue());
      mplew.write(unk);
      mplew.write(total);
      return mplew.getPacket();
   }

   public static byte[] showVoydPressure(int cid, List<Byte> arrays) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_VOYD_PRESSURE.getValue());
      mplew.writeInt(cid);
      mplew.write(arrays.size());
      Iterator var3 = arrays.iterator();

      while(var3.hasNext()) {
         Byte aray = (Byte)var3.next();
         mplew.write(aray);
      }

      return mplew.getPacket();
   }

   public static byte[] TheSidItem(int... args) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.THE_SEED_ITEM.getValue());
      mplew.writeShort(7);
      mplew.writeInt(2028272);
      mplew.write((int)1);
      mplew.writeInt(args.length);

      for(int i = 0; i < args.length; ++i) {
         mplew.writeInt(args[i]);
      }

      return mplew.getPacket();
   }

   public static byte[] showForeignDamageSkin(MapleCharacter chr, int skinid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_DAMAGE_SKIN.getValue());
      mplew.writeInt(chr.getId());
      mplew.writeInt(skinid);
      mplew.writeMapleAsciiString("");
      mplew.writeMapleAsciiString("");
      return mplew.getPacket();
   }

   public static byte[] updateDress(int code, MapleCharacter chr) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.UPDATE_DRESS.getValue());
      mplew.writeInt(chr.getId());
      mplew.writeInt(code);
      return mplew.getPacket();
   }

   public static byte[] showMedalDisplay(MapleCharacter chr, byte state) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MEDAL_DISPLAY.getValue());
      mplew.writeInt(chr.getId());
      mplew.write(state);
      return mplew.getPacket();
   }

   public static byte[] showTitleDisplay(MapleCharacter chr, byte state) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.TITLE_DISPLAY.getValue());
      mplew.writeInt(chr.getId());
      mplew.write(state);
      return mplew.getPacket();
   }

   public static byte[] keepDress(boolean isDress) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.KEEP_DRESSUP.getValue());
      mplew.write(isDress);
      mplew.write((int)1);
      return mplew.getPacket();
   }

   public static byte[] lockSkill(int skillid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.LOCK_SKILL.getValue());
      mplew.writeInt(skillid);
      return mplew.getPacket();
   }

   public static byte[] unlockSkill() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.UNLOCK_SKILL.getValue());
      return mplew.getPacket();
   }

   public static byte[] setPlayerDead() {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.SET_DEAD.getValue());
      packet.write((int)1);
      packet.writeInt(0);
      return packet.getPacket();
   }

   public static byte[] OpenDeadUI(MapleCharacter chr, int flag) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.OPEN_UI_DEAD.getValue());
      packet.writeInt(chr.getItemQuantity(5133000, false) <= 0 && !GameConstants.보스맵(chr.getMapId()) ? flag : 3);
      packet.write((int)0);
      packet.writeInt(!GameConstants.보스맵(chr.getMapId()) || chr.getDeathCount() < 0 && chr.liveCounts() < 0 ? 0 : 3);
      packet.writeInt(chr.getItemQuantity(5133000, false) > 0 ? 0 : -1);
      packet.write(!GameConstants.보스맵(chr.getMapId()) || chr.getDeathCount() < 0 && chr.liveCounts() < 0 ? 0 : 1);
      packet.writeInt(!GameConstants.보스맵(chr.getMapId()) || chr.getDeathCount() < 0 && chr.liveCounts() < 0 ? 0 : 30);
      packet.writeInt(!GameConstants.보스맵(chr.getMapId()) || chr.getDeathCount() < 0 && chr.liveCounts() < 0 ? 0 : 5);
      packet.write(GameConstants.보스맵(chr.getMapId()) && chr.getDeathCount() <= 0 && chr.liveCounts() <= 0 ? 1 : 0);
      return packet.getPacket();
   }

   public static byte[] BlackMageDeathCountEffect() {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.BLACKMAGE_DEATHCOUNT.getValue());
      packet.writeInt(0);
      return packet.getPacket();
   }

   public static byte[] Aggressive(List<Pair<String, Long>> a, MapleMap map) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.AGGRESSIVE.getValue());
      mplew.writeInt(a.size());
      Iterator var3 = a.iterator();

      while(var3.hasNext()) {
         Pair<String, Long> b = (Pair)var3.next();
         mplew.writeMapleAsciiString((String)b.getLeft());
      }

      return mplew.getPacket();
   }

   public static byte[] getDeathCount(byte count) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.DEATH_COUNT.getValue());
      packet.writeInt(count);
      return packet.getPacket();
   }

   public static byte[] setDeathCount(MapleCharacter chr, int count) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SET_DEATH_COUNT.getValue());
      mplew.writeInt(chr.getId());
      mplew.writeInt(count);
      return mplew.getPacket();
   }

   public static byte[] showDeathCount(MapleCharacter chr, int count) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_DEATH_COUNT.getValue());
      if (count <= 0) {
         mplew.writeShort(-1);
      } else {
         mplew.writeShort(count);
         mplew.writeShort(1);
         mplew.writeInt(chr.getId());
         mplew.writeInt(count);
      }

      return mplew.getPacket();
   }

   public static byte[] getPracticeMode(boolean practice) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.PRACTICE_MODE.getValue());
      packet.write(practice);
      return packet.getPacket();
   }

   public static byte[] enterAuction(MapleCharacter chr) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ENTER_AUCTION.getValue());
      PacketHelper.addCharacterInfo(mplew, chr);
      mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
      return mplew.getPacket();
   }

   public static byte[] dailyGift(MapleCharacter chr, int type, int itemId) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DAILY_GIFT.getValue());
      mplew.write(type != 1 ? 2 : 0);
      if (type != 1) {
         mplew.writeInt(type);
         mplew.writeInt(itemId);
      } else {
         mplew.write((int)1);
         mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
         mplew.writeLong(PacketHelper.getTime(-1L));
         mplew.writeInt(28);
         mplew.writeInt(2);
         mplew.writeInt(16700);
         mplew.writeInt(300);
         mplew.writeInt(GameConstants.dailyItems.size());
         Iterator var4 = GameConstants.dailyItems.iterator();

         while(var4.hasNext()) {
            DailyGiftItemInfo item = (DailyGiftItemInfo)var4.next();
            mplew.writeInt(item.getId());
            mplew.writeInt(item.getItemId());
            mplew.writeInt(item.getQuantity());
            mplew.write((int)1);
            mplew.writeInt(item.getSN() > 0 ? 0 : 10080);
            mplew.writeInt(item.getSN() > 0 ? 1 : 0);
            mplew.writeInt(item.getSN());
            mplew.writeShort(0);
         }

         mplew.writeInt(ServerConstants.ReqDailyLevel);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
      }

      return mplew.getPacket();
   }

   public static byte[] momentAreaOnOffAll(List<String> info) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.MOMENT_AREA_ON_OFF_ALL.getValue());
      packet.writeShort(0);
      packet.write(info.size() > 0 ? 1 : 0);
      if (info.size() > 0) {
         packet.writeInt(info.size());
         Iterator var2 = info.iterator();

         while(var2.hasNext()) {
            String list = (String)var2.next();
            packet.writeMapleAsciiString(list);
         }
      }

      return packet.getPacket();
   }

   public static byte[] onUserTeleport(int x, int y) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.USER_TELEPORT.getValue());
      packet.writeInt(x);
      packet.writeInt(y);
      return packet.getPacket();
   }

   public static byte[] Respawn(int cid, int hp) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.RESPAWN.getValue());
      packet.writeInt(cid);
      packet.writeInt(hp);
      return packet.getPacket();
   }

   public static byte[] showProjectileEffect(MapleCharacter chr, int x, int y, int delay, int skillId, int level, int unk, byte facingleft, int objectId, int number) {
      MaplePacketLittleEndianWriter pw = new MaplePacketLittleEndianWriter();
      pw.writeShort(SendPacketOpcode.SHOW_PROJECTILE_EFFECT.getValue());
      pw.writeInt(chr.getId());
      pw.writeInt(1);
      pw.writeInt(x);
      pw.writeInt(y);
      pw.writeInt(delay);
      pw.writeInt(skillId);
      pw.writeInt(unk);
      pw.writeInt(level);
      pw.write(facingleft);
      pw.writeInt(objectId);
      pw.writeInt(0);
      pw.writeInt(-1);
      pw.writeInt(0);
      pw.writeInt(number);
      return pw.getPacket();
   }

   public static byte[] updateProjectileEffect(int id, int unk1, int unk2, int unk3, int unk4, byte facingleft) {
      MaplePacketLittleEndianWriter pw = new MaplePacketLittleEndianWriter();
      pw.writeShort(SendPacketOpcode.UPDATE_PROJECTILE_EFFECT.getValue());
      pw.writeInt(id);
      pw.writeInt(unk1);
      pw.writeInt(unk2);
      pw.writeInt(unk3);
      pw.writeInt(unk4);
      pw.write(facingleft);
      return pw.getPacket();
   }

   public static byte[] removeProjectile(int unk) {
      MaplePacketLittleEndianWriter pw = new MaplePacketLittleEndianWriter();
      pw.writeShort(SendPacketOpcode.REMOVE_PROJECTILE.getValue());
      pw.writeInt(unk);
      return pw.getPacket();
   }

   public static byte[] removeProjectileEffect(int id, int unk) {
      MaplePacketLittleEndianWriter pw = new MaplePacketLittleEndianWriter();
      pw.writeShort(SendPacketOpcode.REMOVE_PROJECTILE_EFFECT.getValue());
      pw.writeInt(id);
      pw.writeInt(unk);
      return pw.getPacket();
   }

   public static byte[] bonusAttackRequest(int skillid, List<Triple<Integer, Integer, Integer>> mobList, boolean unk, int jaguarBleedingAttackCount, int... args) {
      MaplePacketLittleEndianWriter pw = new MaplePacketLittleEndianWriter();
      pw.writeShort(SendPacketOpcode.BONUS_ATTACK_REQUEST.getValue());
      pw.writeInt(skillid);
      pw.writeInt(mobList.size());
      pw.write(unk);
      pw.writeInt(skillid + 1 != 3321014 && skillid + 1 != 3321016 && skillid + 1 != 3321018 && skillid + 1 != 3321022 ? 0 : jaguarBleedingAttackCount);
      pw.writeInt(jaguarBleedingAttackCount);
      Iterator var6 = mobList.iterator();

      while(var6.hasNext()) {
         Triple<Integer, Integer, Integer> mob = (Triple)var6.next();
         pw.writeInt((Integer)mob.getLeft());
         pw.writeInt((Integer)mob.getMid());
         if (skillid == 400041030) {
            pw.writeInt((Integer)mob.getRight());
         }
      }

      if (skillid == 400051067 || skillid == 400051065) {
         pw.writeInt(args[0]);
         Rectangle rect = new Rectangle(args[1], args[2], args[3], args[4]);
         pw.writeRect(rect);
         pw.writeInt(-201);
         pw.writeInt(-214);
         pw.writeInt(244);
         pw.writeInt(101);
      }

      if (skillid == 400011133) {
         pw.writeInt(400011028);
      }

      return pw.getPacket();
   }

   public static byte[] ShadowServentExtend(Point newpos) {
      MaplePacketLittleEndianWriter pw = new MaplePacketLittleEndianWriter();
      pw.writeShort(SendPacketOpcode.SHADOW_SERVENT_EXTEND.getValue());
      pw.writeInt(newpos.x);
      pw.writeInt(newpos.y);
      pw.write((int)0);
      return pw.getPacket();
   }

   public static byte[] ShadowServentEffect(MapleCharacter chr, MapleSummon summon, int skillid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPECIAL_SUMMON.getValue());
      mplew.writeInt(chr.getId());
      mplew.writeInt(summon.getObjectId());
      mplew.writeInt(2);
      return mplew.getPacket();
   }

   public static byte[] ShadowServentRefresh(MapleCharacter chr, MapleSummon summon, int count) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ELEMENTAL_RADIANCE.getValue());
      mplew.writeInt(chr.getId());
      mplew.writeInt(summon.getObjectId());
      mplew.writeInt(2);
      mplew.writeInt(count);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] DebuffObjON(int[] list, boolean hard) {
      MaplePacketLittleEndianWriter pw = new MaplePacketLittleEndianWriter();
      pw.writeShort(SendPacketOpcode.DEBUFF_OBJECT.getValue());
      int[] arrayOfInt = list;
      int i = list.length;

      for(byte b = 0; b < i; ++b) {
         Integer a = arrayOfInt[b];
         pw.write((int)1);
         pw.writeInt(a);
         pw.writeInt(1);
         int var10001 = hard ? a : a * 10;
         pw.writeMapleAsciiString("sleepGas" + var10001);
         pw.writeMapleAsciiString("sleepGas");
      }

      pw.write((int)0);
      return pw.getPacket();
   }

   public static byte[] lightningUnionSubAttack(int attackskillid, int skillid, int skillLevel) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.LIGHTING_ATTACK.getValue());
      mplew.writeInt(attackskillid);
      mplew.writeInt(skillid);
      mplew.writeInt(skillLevel);
      return mplew.getPacket();
   }

   public static byte[] openUnionUI(MapleClient c) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      List<MapleUnion> equipped = new ArrayList();
      Iterator var3 = c.getPlayer().getUnions().getUnions().iterator();

      MapleUnion chr;
      while(var3.hasNext()) {
         chr = (MapleUnion)var3.next();
         if (chr.getPosition() != -1) {
            equipped.add(chr);
         }
      }

      mplew.writeShort(SendPacketOpcode.OPEN_UNION.getValue());
      mplew.writeInt(c.getPlayer().getUnionCoin());
      mplew.writeInt(0);
      mplew.writeInt(c.getPlayer().getUnions().getUnions().size());
      var3 = c.getPlayer().getUnions().getUnions().iterator();

      while(var3.hasNext()) {
         chr = (MapleUnion)var3.next();
         mplew.writeInt(1);
         mplew.writeInt(chr.getCharid());
         mplew.writeInt(chr.getLevel());
         mplew.writeInt(chr.getJob());
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(-1);
         mplew.writeInt(chr.getUnk3());
         mplew.writeMapleAsciiString(chr.getName());
      }

      mplew.writeInt(equipped.size());
      var3 = equipped.iterator();

      while(var3.hasNext()) {
         chr = (MapleUnion)var3.next();
         mplew.writeInt(1);
         mplew.writeInt(chr.getCharid());
         mplew.writeInt(chr.getLevel());
         mplew.writeInt(chr.getJob());
         mplew.writeInt(chr.getUnk1());
         mplew.writeInt(chr.getUnk2());
         mplew.writeInt(chr.getPosition());
         mplew.writeInt(chr.getUnk3());
         mplew.writeMapleAsciiString("");
      }

      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static byte[] finalAttackRequest(int attackCount, int skillId, int FinalAttackId, int weaponType, MapleMonster monster) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.FINAL_ATTACK_REQUEST.getValue());
      packet.writeInt(attackCount);
      packet.writeInt(skillId);
      packet.writeInt(FinalAttackId);
      packet.writeInt(weaponType);
      if (skillId > 0) {
         packet.writeInt(skillId == 154111006 ? 0 : 1);
         packet.writeInt(monster == null ? 0 : monster.getObjectId());
      } else {
         packet.writeInt(0);
      }

      return packet.getPacket();
   }

   public static byte[] RoyalGuardDamage() {
      MaplePacketLittleEndianWriter pw = new MaplePacketLittleEndianWriter();
      pw.writeShort(SendPacketOpcode.ROYAL_DAMAGE.getValue());
      pw.writeInt(1);
      return pw.getPacket();
   }

   public static byte[] DamagePlayer2(int dam) {
      MaplePacketLittleEndianWriter pw = new MaplePacketLittleEndianWriter();
      pw.writeShort(SendPacketOpcode.DAMAGE_PLAYER2.getValue());
      pw.writeInt(dam);
      return pw.getPacket();
   }

   public static byte[] EnterFieldPyschicInfo() {
      MaplePacketLittleEndianWriter pw = new MaplePacketLittleEndianWriter();
      pw.writeShort(SendPacketOpcode.ENTER_FIELD_PSYCHIC_INFO.getValue());
      pw.write((int)0);
      return pw.getPacket();
   }

   public static byte[] enforceMSG(String a, int id, int delay) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.ENFORCE_MSG.getValue());
      packet.writeMapleAsciiString(a);
      packet.writeInt(id);
      packet.writeInt(delay);
      packet.write((int)0);
      return packet.getPacket();
   }

   public static byte[] enforceMsgNPC(int npcid, int delay, String a) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.YELLOW_DLG.getValue());
      packet.writeInt(npcid);
      packet.writeInt(delay);
      packet.writeMapleAsciiString(a);
      packet.write((int)0);
      packet.write((int)0);
      packet.write((int)0);
      return packet.getPacket();
   }

   public static byte[] spawnSubSummon(short type, int key) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.SPAWN_SUB_SUMMON.getValue());
      packet.writeInt(type);
      packet.writeInt(key);
      return packet.getPacket();
   }

   public static byte[] jaguarAttack(int skillid) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.JAGUAR_ATTACK.getValue());
      packet.writeInt(skillid);
      return packet.getPacket();
   }

   public static byte[] B2BodyResult(MapleCharacter chr, int cid, short type, short type2, int key, Point pos, Point oldPos, short unk1, int sourceid, int level, int duration, short unk2, boolean isFacingLeft, int unk3, int unk4, String unk) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.B2BODY_RESULT.getValue());
      packet.writeShort(type);
      MapleSummon sum = null;
      packet.writeInt(cid);
      packet.writeInt(chr.getMapId());
      if (type == 0) {
         packet.writeShort(1);
         packet.writeInt(key);
         packet.write((int)type2);
         packet.write((int)0);
         packet.writePos(pos);
         if (type2 == 5) {
            packet.writePos(oldPos);
         } else if (type2 == 6) {
            packet.writeInt(0);
         }

         packet.writeShort(unk1);
         packet.writeInt(duration);
         packet.writeShort(unk2);
         packet.writeInt(sourceid);
         packet.writeShort(level);
         packet.write((int)0);
      } else if (type == 3) {
         packet.writeInt(chr.getId());
         packet.writeInt(sourceid);
         packet.writeInt(unk3);
         packet.writeInt(unk4);
      } else if (type == 4) {
         packet.writeShort(1);
         packet.write((int)0);
         packet.writePos(pos);
         packet.writeInt(10000);
         packet.writeShort(type2);
         packet.writeShort(unk1);
         packet.writeShort(unk2);
         packet.write(unk3);
         if (unk3 > 0) {
            packet.writeMapleAsciiString(unk);
         }

         packet.writeInt(unk4);
         packet.writeInt(sourceid);
         packet.write(isFacingLeft);
         packet.writeInt(0);
         packet.writeInt(0);
         packet.write((int)0);
         packet.writeInt(0);
         packet.writeInt(0);
         packet.writeInt(isFacingLeft ? -oldPos.x : oldPos.x);
         packet.writeInt(oldPos.y);
      }

      return packet.getPacket();
   }

   public static byte[] blackJack(MapleCharacter chr, int skillid, Point point) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.BLACKJACK.getValue());
      packet.writeInt(skillid);
      packet.writeInt(chr.getSkillLevel(GameConstants.getLinkedSkill(skillid)));
      packet.writeInt(1);
      packet.writeInt(point.x);
      packet.writeInt(point.y);
      if (skillid == 400041080) {
         packet.writeInt(chr.getSkillCustomValue0(400041080));
      }

      return packet.getPacket();
   }

   public static byte[] rangeAttack(int firstSkill, List<RangeAttack> skills) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.RANGE_ATTACK.getValue());
      packet.writeInt(firstSkill);
      packet.writeShort(skills.size());
      Iterator var3 = skills.iterator();

      while(var3.hasNext()) {
         RangeAttack skill = (RangeAttack)var3.next();
         packet.writeInt(0);
         packet.writeInt(skill.getSkillId());
         packet.writeInt(skill.getPosition().x);
         packet.writeInt(skill.getPosition().y);
         packet.writeShort(skill.getType());
         packet.writeInt(skill.getDelay());
         packet.writeInt(skill.getAttackCount());
         packet.writeInt(skill.getList().size());
         Iterator var5 = skill.getList().iterator();

         while(var5.hasNext()) {
            Integer list = (Integer)var5.next();
            packet.writeInt(list);
         }

         packet.writeInt(0);

         for(int i = 0; i < 0; ++i) {
            packet.writeInt(0);
         }

         packet.writeInt(0);
         packet.writeInt(0);
         packet.writeInt(0);
      }

      return packet.getPacket();
   }

   public static byte[] rangeAttackTest(int firstSkill, int attackSkill, int mistoid, List<RangeAttack> skills) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.RANGE_ATTACK.getValue());
      packet.writeInt(firstSkill);
      packet.writeShort(skills.size());
      packet.writeInt(attackSkill);
      Iterator var5 = skills.iterator();

      while(var5.hasNext()) {
         RangeAttack skill = (RangeAttack)var5.next();
         packet.writeInt(skill.getSkillId());
         packet.writeInt(skill.getPosition().x);
         packet.writeInt(skill.getPosition().y);
         packet.writeShort(skill.getType());
         packet.writeInt(skill.getDelay());
         packet.writeInt(skill.getAttackCount());
         packet.writeInt(skill.getList().size());
         Iterator var7 = skill.getList().iterator();

         while(var7.hasNext()) {
            Integer list = (Integer)var7.next();
            packet.writeInt(list);
         }

         packet.writeInt(0);
         packet.writeInt(0);
         packet.writeInt(mistoid);
         packet.writeInt(0);
         if (skill.getSkillId() != 400041079) {
            packet.writeInt(0);
         }
      }

      return packet.getPacket();
   }

   public static byte[] createMagicWreck(MapleMagicWreck mw) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.CREATE_MAGIC_WRECK.getValue());
      packet.writeInt(mw.getChr().getId());
      packet.writeInt(mw.getTruePosition().x);
      packet.writeInt(mw.getTruePosition().y);
      packet.writeInt(mw.getDuration());
      packet.writeInt(mw.getObjectId());
      packet.writeInt(mw.getSourceid());
      packet.writeInt(0);
      packet.writeInt(mw.getChr().getMwSize(mw.getSourceid()));
      return packet.getPacket();
   }

   public static byte[] removeMagicWreck(MapleCharacter chr, List<MapleMagicWreck> mws) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.REMOVE_MAGIC_WRECK.getValue());
      packet.writeInt(chr.getId());
      packet.writeInt(mws.size());
      packet.write((int)0);
      packet.write((int)0);
      Iterator var3 = mws.iterator();

      while(var3.hasNext()) {
         MapleMagicWreck mw = (MapleMagicWreck)var3.next();
         packet.writeInt(mw.getObjectId());
      }

      return packet.getPacket();
   }

   public static byte[] ForceAtomAttack(int atomid, int cid, int mobid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FORCE_ATOM_ATTACK.getValue());
      mplew.writeInt(atomid);
      mplew.writeInt(cid);
      mplew.writeInt(1);
      mplew.writeInt(1);
      mplew.writeInt(1);
      mplew.writeInt(mobid);
      return mplew.getPacket();
   }

   public static byte[] ForceAtomEffect(MapleCharacter chr, int atomid, int type, int unk, int unk2, boolean left, Point pos1, Point pos2) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FORCE_ATOM_ATTACK.getValue());
      mplew.writeInt(atomid);
      mplew.writeInt(chr.getId());
      mplew.writeInt(type);
      if (type == 3) {
         mplew.writeInt(unk);
         mplew.writePos(pos1);
         mplew.writePos(pos2);
      } else {
         mplew.writeInt(unk);
         mplew.writeInt(unk2);
         mplew.write(left);
         mplew.writePos(pos1);
         mplew.writePos(pos2);
      }

      return mplew.getPacket();
   }

   public static byte[] screenAttack(int mobId, int skillId, int skillLevel, long damage) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SCREEN_ATTACK.getValue());
      mplew.writeInt(mobId);
      mplew.writeInt(skillId);
      mplew.writeInt(skillLevel);
      mplew.writeLong(damage);
      return mplew.getPacket();
   }

   public static byte[] mutoSetTime(int time) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.HUGNRY_MUTO.getValue());
      mplew.writeInt(1);
      mplew.writeInt(time);
      return mplew.getPacket();
   }

   public static byte[] finishMuto() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.HUGNRY_MUTO.getValue());
      mplew.writeInt(2);
      return mplew.getPacket();
   }

   public static byte[] setMutoNewRecipe(int[] recipe, int length, EventInstanceManager eim) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.HUGNRY_MUTO.getValue());
      mplew.writeInt(3);
      mplew.writeInt(recipe[0]);
      mplew.writeInt(recipe[1]);
      mplew.writeInt(recipe[2]);
      mplew.writeInt(recipe[3]);
      mplew.writeInt(recipe[4]);
      mplew.writeInt(length);

      for(int i = 0; i < length; ++i) {
         if (eim.getProperty("recipeHidden" + i) != null) {
            mplew.writeInt(0);
         } else {
            mplew.writeInt(Integer.parseInt(eim.getProperty("recipeItem" + i)));
         }

         mplew.writeInt(Integer.parseInt(eim.getProperty("recipeReq" + i)));
         mplew.writeInt(Integer.parseInt(eim.getProperty("recipeCount" + i)));
      }

      return mplew.getPacket();
   }

   public static byte[] setMutoRecipe(int[] recipe, int length, MapleCharacter chr) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.HUGNRY_MUTO.getValue());
      mplew.writeInt(4);
      mplew.writeInt(recipe[0]);
      mplew.writeInt(recipe[1]);
      mplew.writeInt(recipe[2]);
      mplew.writeInt(length);

      for(int i = 0; i < length; ++i) {
         mplew.writeInt(Integer.parseInt(chr.getEventInstance().getProperty("recipeItem" + i)));
         mplew.writeInt(Integer.parseInt(chr.getEventInstance().getProperty("recipeReq" + i)));
         mplew.writeInt(Integer.parseInt(chr.getEventInstance().getProperty("recipeCount" + i)));
      }

      return mplew.getPacket();
   }

   public static byte[] addItemMuto(MapleCharacter chr) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.HUGNRY_MUTO.getValue());
      mplew.writeInt(5);
      mplew.writeInt(1);
      mplew.writeInt(chr.getId());
      mplew.writeInt((Integer)chr.getRecipe().left - 1599086);
      mplew.writeInt((Integer)chr.getRecipe().right);
      return mplew.getPacket();
   }

   public static byte[] ChainArtsFury(Point truePosition) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CHAINARTS_FURY.getValue());
      mplew.writeInt(truePosition.x);
      mplew.writeInt(truePosition.y);
      return mplew.getPacket();
   }

   public static byte[] ICBM(boolean cancel, int skillid, Rectangle calculateBoundingBox) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ICBM.getValue());
      mplew.writeInt(cancel ? 0 : 1);
      mplew.writeInt(skillid);
      mplew.writeInt(1);
      mplew.writeInt(0);
      mplew.writeInt(calculateBoundingBox.x);
      mplew.writeInt(calculateBoundingBox.y);
      mplew.writeInt(calculateBoundingBox.width);
      mplew.writeInt(calculateBoundingBox.height);
      return mplew.getPacket();
   }

   public static byte[] specialMapSound(String str) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPECIAL_MAP_SOUND.getValue());
      mplew.writeMapleAsciiString(str);
      return mplew.getPacket();
   }

   public static byte[] specialMapEffect(int type, boolean isEliteMonster, String bgm, String back, String effect, String obj, String tile) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPECIAL_MAP_EFFECT.getValue());
      mplew.writeInt(type);
      mplew.writeInt(isEliteMonster ? 1 : 0);
      mplew.writeInt(0);
      switch(type - 2) {
      case 0:
      case 3:
         mplew.writeMapleAsciiString(bgm);
         mplew.writeMapleAsciiString(back);
         mplew.writeMapleAsciiString(effect);
         break;
      case 1:
      case 2:
         mplew.write(true);
         mplew.writeMapleAsciiString(bgm);
         mplew.writeMapleAsciiString(back);
         mplew.writeMapleAsciiString(effect);
         mplew.writeMapleAsciiString(obj);
         mplew.writeMapleAsciiString(tile);
         mplew.write((int)0);
         break;
      case 4:
         mplew.writeMapleAsciiString(bgm);
      }

      return mplew.getPacket();
   }

   public static byte[] unstableMemorize(int skillId) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.UNSTABLE_MEMORIZE.getValue());
      mplew.writeInt(skillId);
      mplew.writeInt(4);
      return mplew.getPacket();
   }

   public static byte[] SpiritFlow(List<Pair<Integer, Integer>> skills) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPIRIT_FLOW.getValue());
      mplew.writeInt(skills.size());
      Iterator var2 = skills.iterator();

      while(var2.hasNext()) {
         Pair<Integer, Integer> skill = (Pair)var2.next();
         mplew.writeInt((Integer)skill.left);
         mplew.writeInt((Integer)skill.right);
      }

      return mplew.getPacket();
   }

   public static byte[] airBone(MapleCharacter chr, MapleMonster mob, int skill, int level, int end) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.AIRBONE.getValue());
      mplew.writeInt(chr.getId());
      mplew.writeInt(mob.getObjectId());
      mplew.writeInt(skill);
      mplew.writeInt(level);
      mplew.writeInt(end);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] poisonNova(MapleCharacter chr, List<Integer> novas) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.POISON_NOVA.getValue());
      mplew.writeInt(chr.getId());
      mplew.writeInt(novas.size());
      Iterator iterator = novas.iterator();

      while(iterator.hasNext()) {
         int nova = (Integer)iterator.next();
         mplew.writeInt(nova);
      }

      return mplew.getPacket();
   }

   public static byte[] runeCurse(String string, boolean delete) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.RUNE_CURSE.getValue());
      mplew.writeMapleAsciiString(string);
      mplew.writeInt(231);
      mplew.write(delete);
      mplew.write(delete);
      mplew.writeInt(50);
      mplew.writeInt(50);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] buffFreezer(int itemId, boolean use) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.BUFF_FREEZER.getValue());
      mplew.writeInt(itemId);
      mplew.write(use);
      return mplew.getPacket();
   }

   public static byte[] quickSlot(MapleCharacter chr) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.QUICK_SLOT.getValue());
      mplew.write(true);

      for(int i = 0; i < 32; ++i) {
         mplew.writeInt(chr.getKeyValue(333333, "quick" + i) < 0L ? 0L : chr.getKeyValue(333333, "quick" + i));
      }

      return mplew.getPacket();
   }

   public static byte[] ignitionBomb(int skillId, int objectId, Point pos) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.IGNITION_BOMB.getValue());
      mplew.writeInt(skillId);
      mplew.writeInt(pos.x);
      mplew.writeInt(pos.y);
      mplew.writeInt(objectId);
      mplew.writeInt(5);
      return mplew.getPacket();
   }

   public static byte[] quickMove(List<QuickMoveEntry> quicks) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.QUICK_MOVE.getValue());
      mplew.write(quicks.size());
      Iterator var2 = quicks.iterator();

      while(var2.hasNext()) {
         QuickMoveEntry quick = (QuickMoveEntry)var2.next();
         mplew.writeInt(quick.getType());
         mplew.writeInt(quick.getId());
         mplew.writeInt(quick.getIcon());
         mplew.writeInt(quick.getLevel());
         mplew.writeMapleAsciiString(quick.getDesc());
         mplew.writeLong(PacketHelper.getTime(-2L));
         mplew.writeLong(PacketHelper.getTime(-1L));
      }

      return mplew.getPacket();
   }

   public static byte[] dimentionMirror(List<DimentionMirrorEntry> quicks) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DIMENTION_MIRROR.getValue());
      mplew.writeInt(quicks.size());
      Iterator var2 = quicks.iterator();

      while(var2.hasNext()) {
         DimentionMirrorEntry quick = (DimentionMirrorEntry)var2.next();
         mplew.writeMapleAsciiString(quick.getName());
         mplew.writeMapleAsciiString(quick.getDesc());
         mplew.writeInt(quick.getLevel());
         mplew.writeInt(quick.getType());
         mplew.writeInt(quick.getId());
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeMapleAsciiString("");
         mplew.write(false);
         mplew.writeInt(quick.getItems().size());
         Iterator var4 = quick.getItems().iterator();

         while(var4.hasNext()) {
            Integer itemId = (Integer)var4.next();
            mplew.writeInt(itemId);
         }
      }

      return mplew.getPacket();
   }

   public static byte[] TimeCapsule(int motionid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.TIME_CAPSULE.getValue());
      mplew.writeInt(motionid);
      mplew.writeInt(1);
      return mplew.getPacket();
   }

   public static byte[] NettPyramidWave(int wave) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.NETT_PYRAMID_WAVE.getValue());
      mplew.writeInt(wave);
      return mplew.getPacket();
   }

   public static byte[] NettPyramidLife(int life) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.NETT_PYRAMID_LIFE.getValue());
      mplew.writeInt(life);
      return mplew.getPacket();
   }

   public static byte[] NettPyramidPoint(int point) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.NETT_PYRAMID_POINT.getValue());
      mplew.writeInt(point);
      return mplew.getPacket();
   }

   public static byte[] NettPyramidClear(boolean clear, int wave, int life, int point, int exp) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.NETT_PYRAMID_CLEAR.getValue());
      mplew.write(clear);
      mplew.writeInt(wave);
      mplew.writeInt(life);
      mplew.writeInt(point);
      mplew.writeInt(exp);
      return mplew.getPacket();
   }

   public static byte[] ImageTalkNpc(int npcid, int time, String message) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.YELLOW_DLG.getValue());
      mplew.writeInt(npcid);
      mplew.writeInt(time);
      mplew.writeMapleAsciiString(message);
      mplew.writeMapleAsciiString("");
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] inviteChair(int value) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.INVITE_CHAIR.getValue());
      mplew.writeInt(value);
      return mplew.getPacket();
   }

   public static byte[] requireChair(int id) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.REQUIRE_CHAIR.getValue());
      mplew.writeInt(id);
      return mplew.getPacket();
   }

   public static byte[] resultChair(int v1, int v2) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.RESULT_CHAIR.getValue());
      mplew.writeInt(v1);
      mplew.writeInt(v2);
      return mplew.getPacket();
   }

   public static byte[] fishing(int type) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FISHING.getValue());
      mplew.writeInt(type);
      switch(type) {
      case 2:
         mplew.write(HexTool.getByteArrayFromHexString("00 00 00 40 0B 16 40 40 00 00 00 00 00 00 00 2E 40 00 00 00 00 00 80 41 40 01 00 00 00 00 00 00 00 00 C0 58 40 04 00 00 00 00 00 00 00 F4 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 B8 0B 00 00 B8 0B 00 00 00 00 00 A0 99 99 B9 3F 00 00 00 A0 99 99 C9 3F 02 00 00 00 E8 03 00 00 D0 07 00 00 00 00 00 A0 99 99 A9 BF 00 00 00 00 00 00 00 00 03 00 00 00 F4 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"));
      default:
         return mplew.getPacket();
      }
   }

   public static byte[] fishingResult(int cid, int itemId) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FISHING_RESULT.getValue());
      mplew.writeInt(cid);
      mplew.writeInt(itemId);
      return mplew.getPacket();
   }

   public static byte[] ReturnSynthesizing() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.RETURN_SYNTHESIZING.getValue());
      mplew.writeInt(0);
      mplew.writeInt(2432805);
      mplew.writeInt(0);
      mplew.writeInt(-1509298872);
      return mplew.getPacket();
   }

   public static byte[] StigmaTime(int i) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.STIGMA_TIME.getValue());
      mplew.writeInt(i);
      return mplew.getPacket();
   }

   public static byte[] UseSkillWithUI(int unk, int skillid, int skilllevel) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.USE_SKILL_WITH_UI.getValue());
      mplew.writeInt(unk);
      if (unk > 0) {
         mplew.writeInt(unk);
         mplew.write(false);
         mplew.writeInt(1);
         mplew.write(false);
         mplew.writeInt(skillid);
         mplew.writeInt(skilllevel);
         mplew.writeZeroBytes(23);
      }

      return mplew.getPacket();
   }

   public static byte[] ActivePotionCooldown(int time) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ACTIVE_POTION_COOL.getValue());
      mplew.writeInt(time);
      return mplew.getPacket();
   }

   public static byte[] potionCooldown() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.POTION_COOLDOWN.getValue());
      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static byte[] JinHillah(int type, MapleCharacter chr, MapleMap map) {
      MaplePacketLittleEndianWriter mplew;
      mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.JIN_HILLAH.getValue());
      mplew.writeInt(type);
      label26:
      switch(type) {
      case 0:
         mplew.writeInt(map.getCandles());
         mplew.write(false);
         break;
      case 1:
         mplew.writeInt(map.getLightCandles());
      case 2:
      case 5:
      case 9:
      default:
         break;
      case 3:
         mplew.writeInt(chr.getDeathCounts().length);
         int i = 0;

         while(true) {
            if (i >= chr.getDeathCounts().length) {
               break label26;
            }

            mplew.writeInt(0);
            mplew.write(chr.getDeathCounts()[i]);
            ++i;
         }
      case 4:
         mplew.writeInt(map.getSandGlassTime() * 1000L);
         mplew.writeInt(247);
         mplew.writeInt(1);
         break;
      case 6:
         int x = Randomizer.rand(-700, 700);
         mplew.writeInt(x);
         chr.getMap().setCustomInfo(28002, x, 0);
         mplew.writeInt(266);
         mplew.writeInt(30);
         break;
      case 7:
         mplew.writeInt(30 - map.getReqTouched());
         break;
      case 8:
         mplew.write(map.getReqTouched() == 0);
         break;
      case 10:
         mplew.writeInt(5);
         mplew.writeInt(chr.getId());
         mplew.writeInt(chr.liveCounts());
      }

      mplew.writeZeroBytes(100);
      return mplew.getPacket();
   }

   public static byte[] showICBM(int id, int readInt, int readInt2) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHOW_ICBM.getValue());
      mplew.writeInt(id);
      mplew.writeInt(readInt);
      mplew.writeInt(readInt2);
      mplew.writeZeroBytes(10);
      return mplew.getPacket();
   }

   public static byte[] followEffect(int initiator, int replier, Point toMap) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FOLLOW_EFFECT.getValue());
      mplew.writeInt(initiator);
      mplew.writeInt(replier);
      if (replier == 0) {
         mplew.write(toMap == null ? 0 : 1);
         if (toMap != null) {
            mplew.writeInt(toMap.x);
            mplew.writeInt(toMap.y);
         }
      }

      return mplew.getPacket();
   }

   public static byte[] moveFollow(Point otherStart, Point myStart, Point otherEnd, List<LifeMovementFragment> moves) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FOLLOW_MOVE.getValue());
      mplew.writePos(otherStart);
      mplew.writePos(myStart);
      PacketHelper.serializeMovementList(mplew, moves);
      mplew.write((int)17);

      for(int i = 0; i < 8; ++i) {
         mplew.write((int)0);
      }

      mplew.write((int)0);
      mplew.writePos(otherEnd);
      mplew.writePos(otherStart);
      return mplew.getPacket();
   }

   public static byte[] getFollowMsg(int opcode) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FOLLOW_MSG.getValue());
      mplew.writeLong((long)opcode);
      return mplew.getPacket();
   }

   public static byte[] battleStatistics() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.BATTLE_STATISTICS.getValue());
      mplew.write((int)1);
      return mplew.getPacket();
   }

   public static byte[] createAtom(MapleAtom atom) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CREATE_FORCE_ATOM.getValue());
      mplew.write(atom.isByMob());
      if (atom.isByMob()) {
         mplew.writeInt(atom.getDwUserOwner());
      }

      mplew.writeInt(atom.getDwTargetId());
      mplew.writeInt(atom.getnForceAtomType());
      Iterator var2;
      if (atom.getnForceAtomType() != 36 && atom.getnForceAtomType() != 37) {
         switch(atom.getnForceAtomType()) {
         case 0:
         case 9:
         case 14:
         case 29:
         case 42:
            if (atom.getnForceAtomType() == 29 || atom.getnForceAtomType() == 42) {
               mplew.writeInt(atom.getnSkillId());
               if (atom.getnSkillId() == 400021069) {
                  mplew.writeInt(200);
               }
            }
            break;
         default:
            mplew.write(atom.isToMob());
            Integer dwTarget;
            label105:
            switch(atom.getnForceAtomType()) {
            case 2:
            case 3:
            case 6:
            case 7:
            case 11:
            case 12:
            case 13:
            case 17:
            case 19:
            case 20:
            case 23:
            case 24:
            case 25:
            case 27:
            case 28:
            case 30:
            case 32:
            case 34:
            case 38:
            case 39:
            case 40:
            case 41:
            case 47:
            case 48:
            case 49:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
            case 58:
            case 60:
            case 64:
            case 65:
            case 67:
            case 72:
            case 73:
            case 75:
               mplew.writeInt(atom.getDwTargets().size());
               var2 = atom.getDwTargets().iterator();

               while(true) {
                  if (!var2.hasNext()) {
                     break label105;
                  }

                  dwTarget = (Integer)var2.next();
                  mplew.writeInt(dwTarget);
               }
            case 4:
            case 5:
            case 8:
            case 9:
            case 10:
            case 14:
            case 15:
            case 16:
            case 18:
            case 21:
            case 22:
            case 26:
            case 29:
            case 31:
            case 33:
            case 35:
            case 36:
            case 37:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 50:
            case 51:
            case 59:
            case 61:
            case 62:
            case 63:
            case 66:
            case 68:
            case 69:
            case 70:
            case 71:
            case 74:
            default:
               if (atom.getnForceAtomType() == 62) {
                  mplew.writeInt(atom.getDwTargets().size());
                  var2 = atom.getDwTargets().iterator();

                  while(var2.hasNext()) {
                     dwTarget = (Integer)var2.next();
                     mplew.writeInt(dwTarget);
                  }
               } else {
                  mplew.writeInt(atom.getDwFirstTargetId());
               }
            }
         }
      }

      if (atom.getnForceAtomType() != 29 && atom.getnForceAtomType() != 42 && atom.getnForceAtomType() != 0) {
         mplew.writeInt(atom.getnSkillId());
      }

      var2 = atom.getForceAtoms().iterator();

      while(var2.hasNext()) {
         ForceAtom forceAtom = (ForceAtom)var2.next();
         mplew.write(true);
         mplew.writeInt(forceAtom.getnAttackCount());
         mplew.writeInt(0);
         mplew.writeInt(forceAtom.getnInc());
         mplew.writeInt(forceAtom.getnFirstImpact());
         mplew.writeInt(forceAtom.getnSecondImpact());
         mplew.writeInt(forceAtom.getnAngle());
         mplew.writeInt(forceAtom.getnStartDelay());
         mplew.writeInt(forceAtom.getnStartX());
         mplew.writeInt(forceAtom.getnStartY());
         mplew.writeInt(forceAtom.getDwCreateTime());
         mplew.writeInt(forceAtom.getnMaxHitCount());
         mplew.writeInt(forceAtom.getnEffectIdx());
         mplew.writeInt(atom.getnSkillId() != 400011058 && atom.getnSkillId() != 400011059 ? 0 : 2000);
      }

      mplew.write(false);
      switch(atom.getnForceAtomType()) {
      case 7:
         mplew.writeInt(atom.getnForcedTargetX());
         mplew.writeInt(atom.getnForcedTargetY());
         mplew.writeInt(atom.getnForcedTargetX() + 500);
         mplew.writeInt(atom.getnForcedTargetY() + 500);
         break;
      case 9:
         mplew.writeInt(-atom.getnForcedTargetX());
         mplew.writeInt(-atom.getnForcedTargetY());
         mplew.writeInt(atom.getnForcedTargetX());
         mplew.writeInt(atom.getnForcedTargetY());
         break;
      case 11:
         mplew.writeInt(atom.getnForcedTargetX() - 240);
         mplew.writeInt(atom.getnForcedTargetY() - 120);
         mplew.writeInt(atom.getnForcedTargetX() + 240);
         mplew.writeInt(atom.getnForcedTargetY() + 120);
         mplew.writeInt(atom.getnItemId());
         break;
      case 13:
         mplew.writeInt(25121005);
         break;
      case 15:
         mplew.writeInt(atom.getnForcedTargetX() - 10);
         mplew.writeInt(atom.getnForcedTargetY() - 10);
         mplew.writeInt(atom.getnForcedTargetX() + 10);
         mplew.writeInt(atom.getnForcedTargetY() + 10);
         mplew.write(false);
         break;
      case 29:
         mplew.writeInt(atom.getnForcedTargetX() - 100);
         mplew.writeInt(atom.getnForcedTargetY() - 100);
         mplew.writeInt(atom.getnForcedTargetX() + 100);
         mplew.writeInt(atom.getnForcedTargetY() + 100);
         mplew.writeInt(atom.getnForcedTargetX());
         mplew.writeInt(atom.getnForcedTargetY());
         break;
      case 33:
         mplew.writeInt(atom.getnForcedTargetX());
         mplew.writeInt(atom.getnForcedTargetY());
         mplew.writeInt(0);
         mplew.writeInt(atom.getSearchX());
         mplew.writeInt(atom.getSearchX1());
      }

      switch(atom.getnForceAtomType()) {
      case 4:
      case 16:
      case 20:
      case 26:
      case 30:
      case 61:
      case 64:
      case 67:
         mplew.writeInt(atom.getnForcedTargetX());
         mplew.writeInt(atom.getnForcedTargetY());
         mplew.writeZeroBytes(30);
      default:
         switch(atom.getnForceAtomType()) {
         case 17:
            mplew.writeInt(atom.getnArriveDir());
            mplew.writeInt(atom.getnArriveRange());
            break;
         case 18:
            mplew.writeInt(atom.getnForcedTargetX());
            mplew.writeInt(atom.getnForcedTargetY());
         case 19:
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
         case 26:
         case 29:
         case 30:
         case 31:
         case 32:
         case 33:
         case 35:
         case 38:
         case 40:
         case 41:
         case 43:
         case 44:
         case 45:
         case 46:
         case 47:
         case 48:
         case 51:
         case 52:
         case 53:
         case 54:
         case 55:
         case 56:
         default:
            break;
         case 27:
            mplew.writeInt(-atom.getnForcedTargetX());
            mplew.writeInt(-atom.getnForcedTargetY());
            mplew.writeInt(atom.getnForcedTargetX());
            mplew.writeInt(atom.getnForcedTargetY());
            mplew.writeInt(0);
            break;
         case 28:
         case 34:
            mplew.writeInt(atom.getnForcedTargetX() - 5000);
            mplew.writeInt(atom.getnForcedTargetY() - 5000);
            mplew.writeInt(atom.getnForcedTargetX() + 5000);
            mplew.writeInt(atom.getnForcedTargetY() + 5000);
            mplew.writeInt(20);
            break;
         case 36:
         case 39:
            mplew.writeInt(5);
            mplew.writeInt(550);
            mplew.writeInt(3);
            mplew.writeInt(-300);
            mplew.writeInt(-300);
            mplew.writeInt(300);
            mplew.writeInt(300);
            if (atom.getnForceAtomType() == 36) {
               mplew.writeInt(-50);
               mplew.writeInt(-50);
               mplew.writeInt(50);
               mplew.writeInt(50);
               mplew.writeInt(atom.getDwUnknownPoint());
            }
            break;
         case 37:
            mplew.writeInt(0);
            mplew.writeInt(-300);
            mplew.writeInt(-300);
            mplew.writeInt(300);
            mplew.writeInt(300);
            mplew.writeInt(200);
            mplew.writeInt(atom.getDwUnknownPoint());
            break;
         case 42:
            mplew.writeInt(atom.getnForcedTargetX() - 240);
            mplew.writeInt(atom.getnForcedTargetY() - 120);
            mplew.writeInt(atom.getnForcedTargetX() + 240);
            mplew.writeInt(atom.getnForcedTargetY() + 120);
         case 49:
            mplew.writeInt(atom.getnItemId());
            mplew.writeInt(atom.getDwSummonObjectId());
            mplew.writeInt(atom.getnForcedTargetX() - 50);
            mplew.writeInt(atom.getnForcedTargetY() - 100);
            mplew.writeInt(atom.getnForcedTargetX() + 50);
            mplew.writeInt(atom.getnForcedTargetY() + 100);
            break;
         case 50:
            mplew.writeInt(atom.getnForcedTargetX());
            mplew.writeInt(atom.getnForcedTargetY());
            mplew.writeInt(0);
            break;
         case 57:
         case 58:
            mplew.writeInt(-atom.getSearchX1());
            mplew.writeInt(-atom.getSearchY1());
            mplew.writeInt(atom.getSearchX1());
            mplew.writeInt(atom.getSearchY1());
            mplew.writeInt(atom.getnDuration());
            mplew.writeInt(atom.getSearchX());
            mplew.writeInt(atom.getSearchY());
         }

         if (atom.getnSkillId() != 25100010 && atom.getnSkillId() != 25120115) {
            if (atom.getnSkillId() == 400011131) {
               mplew.writeInt(atom.getDwUnknownInteger());
               mplew.write(atom.getDwUnknownByte());
            }
         } else {
            mplew.writeInt(atom.getnFoxSpiritSkillId());
         }

         mplew.writeZeroBytes(30);
         return mplew.getPacket();
      }
   }

   public static byte[] RemoveAtom(MapleCharacter chr, int type, int objid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.REMOVE_FORCE_ATOM.getValue());
      mplew.writeInt(type);
      mplew.writeInt(objid);
      mplew.writeInt(chr.getId());
      return mplew.getPacket();
   }

   public static byte[] onUIEventInfo(MapleClient c, boolean open) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      LocalDate startDate = LocalDate.of(2021, 2, 25);
      LocalDate finishDate = LocalDate.of(2021, 6, 16);
      mplew.writeShort(SendPacketOpcode.UI_EVENT_INFO.getValue());
      mplew.writeInt(100748);
      mplew.write(open);
      mplew.writeShort(open ? 0 : 1);
      mplew.writeLong(PacketHelper.getTime(finishDate.toEpochDay()));
      mplew.writeLong(PacketHelper.getTime(startDate.toEpochDay()));
      mplew.write((int)0);
      mplew.writeShort(0);
      mplew.writeInt(100748);
      mplew.writeLong(0L);
      mplew.write((int)0);
      mplew.writeInt(252);
      mplew.writeInt(253);
      mplew.writeInt(254);
      mplew.writeInt(3600);
      mplew.writeMapleAsciiString("chariotInfo4");
      mplew.writeMapleAsciiString("");
      mplew.writeMapleAsciiString("chariotInfo4");
      mplew.writeMapleAsciiString("chariotAttend");
      mplew.writeInt(0);
      mplew.writeInt(GameConstants.chariotItems.size());
      Iterator var5 = GameConstants.chariotItems.iterator();

      while(var5.hasNext()) {
         Triple<Integer, Integer, Integer> item = (Triple)var5.next();
         mplew.writeInt((Integer)item.left);
         mplew.writeInt((Integer)item.mid);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.write((int)0);
         mplew.writeInt(0);
         mplew.write((int)0);
         mplew.write((int)0);
      }

      mplew.writeInt(0);
      mplew.writeInt(1254);
      return mplew.getPacket();
   }

   public static byte[] onUIEventInfoSet() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.UI_EVENT_INFO_SET.getValue());
      mplew.write(HexTool.getByteArrayFromHexString("19 00 00 00 72 00 00 00 67 42 00 00 72 00 00 00 FA 38 00 00 E6 04 00 00 70 87 01 00 E2 04 00 00 59 A4 07 00 72 00 00 00 E1 39 00 00 E6 04 00 00 8C 89 01 00 72 00 00 00 0B 39 00 00 72 00 00 00 2B 39 00 00 72 00 00 00 8C A3 07 00 00 05 00 00 90 A5 07 00 72 00 00 00 69 3A 00 00 E6 04 00 00 C7 88 01 00 72 00 00 00 09 AB 00 00 72 00 00 00 FC 3C 00 00 72 00 00 00 B5 38 00 00 72 00 00 00 21 3F 00 00 E6 04 00 00 28 89 01 00 72 00 00 00 6B 3A 00 00 72 00 00 00 AF 38 00 00 72 00 00 00 84 3A 00 00 72 00 00 00 41 40 00 00 72 00 00 00 6D 3A 00 00 72 00 00 00 18 41 00 00 F0 04 00 00 DF A4 07 00 72 00 00 00 BA 38 00 00"));
      return mplew.getPacket();
   }

   public static byte[] onUIEventSet(int objectId, int windowId) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.UI_EVENT_SET.getValue());
      mplew.writeInt(objectId);
      mplew.writeInt(windowId);
      return mplew.getPacket();
   }

   public static byte[] portalTeleport(String name) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.PORTAL_TELEPORT.getValue());
      mplew.writeMapleAsciiString(name);
      return mplew.getPacket();
   }

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
            mplew.writeInt(atom.getSecondAtoms().getSourceId() == 2121052 ? 2 : (atom.getSecondAtoms().getSourceId() == 63101104 ? 1 : 0));
            mplew.writeInt(atom.getSecondAtoms().getDataIndex());
            mplew.writeInt(atom.isNumuse() ? atom.getNum() : i++);
            mplew.writeInt(atom.getSecondAtoms().getLocalOnly() == 1 ? atom.getSecondAtoms().getTarget() : atom.getChr().getId());
            mplew.writeInt(atom.getSecondAtoms().getTarget());
            mplew.writeInt(atom.getSecondAtoms().getCreateDelay());
            mplew.writeInt(atom.getSecondAtoms().getEnableDelay());
            mplew.writeInt(atom.getChr().isFacingLeft() ? -atom.getSecondAtoms().getRotate() : atom.getSecondAtoms().getRotate());
            mplew.writeInt(atom.getSecondAtoms().getSourceId());
            mplew.writeInt(0);
            mplew.writeInt(atom.getSecondAtoms().getSourceId() == 2121052 ? 1 : 0);
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

   public static byte[] spawnSecondAtoms(int cid, List<SecondAtom> tiles, int spawnType) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPAWN_SECOND_ATOMS.getValue());
      mplew.writeInt(cid);
      mplew.writeInt(tiles.size());
      int i = 1;

      for(Iterator var5 = tiles.iterator(); var5.hasNext(); ++i) {
         SecondAtom tile = (SecondAtom)var5.next();
         mplew.writeInt(tile.getObjectId());
         mplew.writeInt(tile.getSkillId() == 5201017 ? 11 : (tile.getSkillId() == 2121052 ? 3 : 0));
         mplew.writeInt(tile.getProjectileType());
         mplew.writeInt(i);
         mplew.writeInt(tile.getLocalOnly() == 1 ? tile.getTargetId() : tile.getOwnerId());
         mplew.writeInt(tile.getTargetId());
         mplew.writeInt(tile.getSkillId() == 5201017 ? 450 + (i - 1) * 120 : (tile.getSkillId() == 2121052 ? 480 : (tile.getSkillId() == 4121020 ? 810 + (i - 1) * 60 : (tile.getSkillId() == 162121010 ? 1110 : (tile.getSkillId() == 162111005 ? 600 : (tile.getSkillId() != 400051069 && tile.getSkillId() != 2311017 ? (tile.getSkillId() == 400041058 ? 150 : 0) : i * 120))))));
         mplew.writeInt(tile.getSkillId() == 5201017 ? 480 + (i - 1) * 120 : (tile.getSkillId() == 5121027 ? 1080 : (tile.getSkillId() == 2121052 ? 720 : (tile.getSkillId() == 4121020 ? 930 + (i - 1) * 60 : (tile.getSkillId() == 2311017 ? 60 + (i - 1) * 120 : tile.getDelay())))));
         mplew.writeInt(tile.getSkillId() == 162121010 ? 20 + (i - 1) * 60 : 0);
         mplew.writeInt(tile.getSkillId());
         mplew.writeInt(0);
         mplew.writeInt(tile.getSkillId() == 5201017 ? 10 : (tile.getSkillId() == 4121020 ? 30 : (tile.getSkillId() == 162111002 ? 20 : (tile.getSkillId() != 400051069 && tile.getSkillId() != 400021092 && tile.getSkillId() != 400051069 && tile.getSkillId() != 162101000 && tile.getSkillId() != 2121052 ? 0 : 1))));
         mplew.writeInt(tile.getDuration());
         mplew.writeInt(tile.getCustom());
         mplew.writeInt(tile.getMaxPerHit());
         mplew.writeInt(tile.getSkillId() == 400011047 ? 1 : 0);
         mplew.writeInt(0);
         mplew.writeInt(tile.getPoint().x);
         mplew.writeInt(tile.getPoint().y);
         mplew.write(tile.getSkillId() == 400011047 || tile.getSkillId() == 162101000);
         mplew.write(false);
         mplew.write(false);
         mplew.writeInt(tile.getPoints().size());
         Iterator iterator = tile.getPoints().iterator();

         while(iterator.hasNext()) {
            int point = (Integer)iterator.next();
            mplew.writeInt(point);
         }
      }

      mplew.writeInt(spawnType);
      return mplew.getPacket();
   }

   public static byte[] spawnAdelProjectiles(MapleCharacter chr, List<AdelProjectile> tiles, boolean infinity) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPAWN_SECOND_ATOMS.getValue());
      mplew.writeInt(chr.getId());
      mplew.writeInt(tiles.size());
      int i = 0;

      for(Iterator var5 = tiles.iterator(); var5.hasNext(); ++i) {
         AdelProjectile tile = (AdelProjectile)var5.next();
         mplew.writeInt(tile.getObjectId());
         mplew.writeInt(tile.getSkillId() == 5201017 ? 11 : (tile.getSkillId() == 400021122 ? 8 : (tile.getSkillId() == 4121020 ? 2 : (tile.getSkillId() != 63101104 && tile.getSkillId() != 162111005 && tile.getSkillId() != 2121052 ? 0 : 1))));
         mplew.writeInt(tile.getProjectileType());
         mplew.writeInt(i);
         mplew.writeInt(tile.getOwnerId());
         mplew.writeInt(tile.getSkillId() == 400011047 ? chr.getId() : tile.getTargetId());
         mplew.writeInt(tile.getCreateDelay());
         mplew.writeInt(tile.getDelay());
         mplew.writeInt(tile.getSkillId() != 63101104 && tile.getSkillId() != 400031063 && tile.getSkillId() != 4121020 ? 0 : tile.getStartX());
         mplew.writeInt(tile.getSkillId());
         mplew.writeInt(0);
         mplew.writeInt(tile.getSkillId() == 4121020 ? 30 : (tile.getSkillId() == 162111002 ? 20 : (tile.getSkillId() != 400051069 && tile.getSkillId() != 162101000 && tile.getSkillId() != 2121052 && tile.getSkillId() != 5201017 ? 0 : 1)));
         mplew.writeInt(tile.getDuration());
         mplew.writeInt(tile.getStartX());
         mplew.writeInt(tile.getStartY());
         mplew.writeInt(tile.getSkillId() != 162101000 && tile.getSkillId() != 2121052 ? (tile.getSkillId() == 400011047 ? 1 : 0) : 0);
         mplew.writeInt(tile.getIdk2());
         mplew.writeInt(tile.getPoint().x);
         mplew.writeInt(tile.getPoint().y);
         mplew.write(tile.getSkillId() == 400011119 || tile.getSkillId() == 162101000);
         mplew.write(false);
         mplew.write(false);
         mplew.writeInt(tile.getPoints().size());
         Iterator var7 = tile.getPoints().iterator();

         while(var7.hasNext()) {
            int point = (Integer)var7.next();
            mplew.writeInt(point);
         }
      }

      mplew.writeInt(infinity ? 1 : 0);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] removeSecondAtom(int cid, int objectId) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.REMOVE_SECOND_ATOM.getValue());
      mplew.writeInt(cid);
      mplew.writeInt(1);
      mplew.writeInt(objectId);
      mplew.writeInt(0);
      mplew.writeInt(1);
      return mplew.getPacket();
   }

   public static byte[] createSpecialPortal(int cid, List<SpecialPortal> lists) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CREATE_SPECIAL_PORTAL.getValue());
      mplew.writeInt(cid);
      mplew.writeInt(lists.size());
      Iterator var3 = lists.iterator();

      while(var3.hasNext()) {
         SpecialPortal list = (SpecialPortal)var3.next();
         mplew.writeInt(list.getOwnerId());
         mplew.writeInt(list.getObjectId());
         mplew.writeInt(list.getSkillType());
         mplew.writeInt(list.getSkillId());
         mplew.writeInt(list.getMapId());
         mplew.writeInt(list.getPointX());
         mplew.writeInt(list.getPointY());
         mplew.writeInt(list.getDuration());
      }

      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] removeSpecialPortal(int cid, List<SpecialPortal> lists) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.REMOVE_SPECIAL_PORTAL.getValue());
      mplew.writeInt(cid);
      mplew.writeInt(lists.size());
      Iterator var3 = lists.iterator();

      while(var3.hasNext()) {
         SpecialPortal list = (SpecialPortal)var3.next();
         mplew.writeInt(list.getObjectId());
      }

      return mplew.getPacket();
   }

   public static byte[] showUnionRaidHpUI(int mobid, long currenthp, long maxhp, int mobid2, long currenthp2, long maxhp2) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.UNION_RAID_HP.getValue());
      mplew.writeInt(mobid);
      mplew.writeLong(currenthp);
      mplew.writeLong(maxhp);
      mplew.writeInt(mobid2);
      mplew.writeLong(currenthp2);
      mplew.writeLong(maxhp2);
      return mplew.getPacket();
   }

   public static byte[] setUnionRaidScore(long score) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.UNION_RAID_SCORE.getValue());
      mplew.writeLong(score);
      return mplew.getPacket();
   }

   public static byte[] setUnionRaidCoinNum(int qty, boolean set) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.UNION_RAID_COIN.getValue());
      mplew.writeInt(qty);
      mplew.write(set);
      return mplew.getPacket();
   }

   public static byte[] showScrollOption(int itemId, int scrollId, StarForceStats es) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SCROLL_CHAT.getValue());
      mplew.writeInt(scrollId);
      mplew.writeInt(itemId);
      mplew.writeInt(es.getFlag());
      if (EnchantFlag.Watk.check(es.getFlag())) {
         mplew.writeInt((Integer)es.getFlag(EnchantFlag.Watk).right);
      }

      if (EnchantFlag.Matk.check(es.getFlag())) {
         mplew.writeInt((Integer)es.getFlag(EnchantFlag.Matk).right);
      }

      if (EnchantFlag.Str.check(es.getFlag())) {
         mplew.writeInt((Integer)es.getFlag(EnchantFlag.Str).right);
      }

      if (EnchantFlag.Dex.check(es.getFlag())) {
         mplew.writeInt((Integer)es.getFlag(EnchantFlag.Dex).right);
      }

      if (EnchantFlag.Int.check(es.getFlag())) {
         mplew.writeInt((Integer)es.getFlag(EnchantFlag.Int).right);
      }

      if (EnchantFlag.Luk.check(es.getFlag())) {
         mplew.writeInt((Integer)es.getFlag(EnchantFlag.Luk).right);
      }

      if (EnchantFlag.Wdef.check(es.getFlag())) {
         mplew.writeInt((Integer)es.getFlag(EnchantFlag.Wdef).right);
      }

      if (EnchantFlag.Mdef.check(es.getFlag())) {
         mplew.writeInt((Integer)es.getFlag(EnchantFlag.Mdef).right);
      }

      if (EnchantFlag.Hp.check(es.getFlag())) {
         mplew.writeInt((Integer)es.getFlag(EnchantFlag.Hp).right);
      }

      if (EnchantFlag.Mp.check(es.getFlag())) {
         mplew.writeInt((Integer)es.getFlag(EnchantFlag.Mp).right);
      }

      if (EnchantFlag.Acc.check(es.getFlag())) {
         mplew.writeInt((Integer)es.getFlag(EnchantFlag.Avoid).right);
      }

      if (EnchantFlag.Avoid.check(es.getFlag())) {
         mplew.writeInt((Integer)es.getFlag(EnchantFlag.Avoid).right);
      }

      return mplew.getPacket();
   }

   public static byte[] popupHomePage(String url) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.POPUP_HOMEPAGE.getValue());
      mplew.write((int)0);
      mplew.write((int)1);
      mplew.writeMapleAsciiString(url);
      return mplew.getPacket();
   }

   public static byte[] getTpAdd(int type, int count) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.TP_ADD.getValue());
      mplew.write(type);
      mplew.writeInt(count);
      return mplew.getPacket();
   }

   public static byte[] getPhotoResult(MapleClient c, byte[] farmImg) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.PHOTO_RESULT.getValue());
      mplew.writeInt(c.getAccID());
      mplew.writeInt(farmImg.length);
      if (farmImg.length > 0) {
         mplew.write(farmImg);
      }

      return mplew.getPacket();
   }

   public static byte[] updateGuildScore(int guildScore) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.UPDATE_GUILD_SCORE.getValue());
      mplew.writeInt(guildScore);
      return mplew.getPacket();
   }

   public static byte[] updateShapeShift(int id, boolean use) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHAPE_SHIFT.getValue());
      mplew.writeInt(id);
      mplew.write(use);
      return mplew.getPacket();
   }

   public static byte[] flowOfFight(int skillId) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FLOW_OF_FIGHT.getValue());
      mplew.writeInt(skillId);
      return mplew.getPacket();
   }

   public static byte[] spawnOrb(int ownerId, List<MapleOrb> orbs) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPAWN_ORB.getValue());
      mplew.writeInt(ownerId);
      mplew.writeInt(orbs.size());
      Iterator var3 = orbs.iterator();

      while(var3.hasNext()) {
         MapleOrb orb = (MapleOrb)var3.next();
         mplew.write(true);
         mplew.writeInt(orb.getOrbType());
         mplew.writeInt(orb.getObjectId());
         mplew.writeInt(orb.getPlayerId());
         mplew.writeInt(orb.getPos().x);
         mplew.writeInt(orb.getPos().y);
         mplew.writeInt(orb.getFacing());
         mplew.writeInt(orb.getUnk3());
         mplew.writeInt(orb.getSkillId());
         mplew.writeInt(orb.getAttackCount());
         mplew.writeInt(orb.getSubTime());
         mplew.writeInt(orb.getDuration());
         mplew.writeInt(orb.getDelay());
         mplew.writeInt(orb.getUnk1());
         mplew.writeInt(orb.getUnk2());
      }

      return mplew.getPacket();
   }

   public static byte[] removeOrb(int ownerId, List<MapleOrb> orbs) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.REMOVE_ORB.getValue());
      mplew.writeInt(ownerId);
      mplew.writeInt(orbs.size());
      Iterator var3 = orbs.iterator();

      while(var3.hasNext()) {
         MapleOrb orb = (MapleOrb)var3.next();
         mplew.writeInt(orb.getObjectId());
      }

      return mplew.getPacket();
   }

   public static byte[] moveOrb(int cid, int type, int objectId, int action, Point pos, int unk1, int unk2, int unk3) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MOVE_ORB.getValue());
      mplew.writeInt(cid);
      mplew.writeInt(type);
      mplew.writeInt(objectId);
      mplew.writeInt(action);
      if (type == 1) {
         mplew.writePos(pos);
         mplew.writeInt(unk1);
         mplew.writeInt(unk2);
         mplew.writeInt(unk3);
      }

      return mplew.getPacket();
   }

   public static byte[] fullMaker(int remainCount, int remainTime) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FULL_MAKER.getValue());
      mplew.write(remainCount > 0);
      if (remainCount > 0) {
         mplew.writeInt(remainCount);
         mplew.writeInt(remainTime);
      }

      return mplew.getPacket();
   }

   public static byte[] egoWeapon(int skillId, Point pos) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.EGO_WEAPON.getValue());
      mplew.writeInt(skillId);
      mplew.writeInt(pos.x);
      mplew.writeInt(pos.y);
      return mplew.getPacket();
   }

   public static byte[] getRefreshQuestInfo(int cid, int active, int skillid, int type) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.REFRESH_QUESTINFO.getValue() + type);
      packet.writeInt(cid);
      packet.write(active);
      packet.writeInt(skillid);
      return packet.getPacket();
   }

   public static byte[] getDotge() {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.DOTGE.getValue());
      return packet.getPacket();
   }

   public static byte[] getExpertThrow() {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.EXPERT_THROW.getValue());
      return packet.getPacket();
   }

   public static byte[] getScatteringShot(int skillid, int nowcount, int maxcount, int cool) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SCATTERING_SHOT.getValue());
      mplew.writeInt(skillid);
      mplew.writeInt(nowcount);
      mplew.writeInt(maxcount);
      mplew.writeInt(cool);
      return mplew.getPacket();
   }

   public static byte[] getSeaWave(MapleCharacter chr) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SCATTERING_SHOT.getValue());
      mplew.writeInt(chr.getId());
      mplew.write((int)1);
      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static byte[] getWreckAttack(MapleCharacter chr, List<MapleMagicWreck> mw) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.WRECK_ATTACK.getValue());
      mplew.writeInt(chr.getId());
      mplew.writeInt(mw.size());
      Iterator var3 = mw.iterator();

      while(var3.hasNext()) {
         MapleMagicWreck m = (MapleMagicWreck)var3.next();
         mplew.writeInt(m.getObjectId());
         mplew.writePosInt(m.getPosition());
      }

      return mplew.getPacket();
   }

   public static byte[] getDeathBlessStack(MapleCharacter chr, List<MapleMonster> monster) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DEATH_BLESS_STACK.getValue());
      mplew.write((int)1);
      mplew.writeInt(monster.size());
      Iterator var3 = monster.iterator();

      MapleMonster mob;
      while(var3.hasNext()) {
         mob = (MapleMonster)var3.next();
         mplew.writeInt(mob.getObjectId());
      }

      mplew.writeInt(monster.size());
      var3 = monster.iterator();

      while(var3.hasNext()) {
         mob = (MapleMonster)var3.next();
         mplew.writeInt(mob.getObjectId());
         mplew.writeInt(mob.getCustomValue0(63110011));
         mplew.writeInt(0);
         mplew.writeInt(mob.getCustomTime(63110011));
      }

      return mplew.getPacket();
   }

   public static byte[] getDeathBlessAttack(List<MapleMonster> monster, int skillid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DEATH_BLESS_ATTACK.getValue());
      mplew.writeInt(skillid);
      mplew.writeInt(0);
      mplew.writeInt(monster.size());
      Iterator var3 = monster.iterator();

      while(var3.hasNext()) {
         MapleMonster mob = (MapleMonster)var3.next();
         mplew.writeInt(mob.getObjectId());
         mplew.writeInt(1);
         mplew.writeInt(300);
      }

      return mplew.getPacket();
   }

   public static byte[] FireWork(MapleCharacter chr) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FIRE_WORK.getValue());
      mplew.writeInt(chr.getId());
      return mplew.getPacket();
   }

   public static byte[] NightWalkerShadowSpearBig(int x, int y) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SHADOW_SPEAR_BIG.getValue());
      mplew.writeInt(x);
      mplew.writeInt(y);
      return mplew.getPacket();
   }

   public static byte[] getLightOfCurigi(int skillid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.APPLY_LIGHT_OF_CURIGI.getValue());
      mplew.writeInt(skillid);
      return mplew.getPacket();
   }

   public static byte[] setFallingTime(int fallingspeed, int fallingtime) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FALLING_TIME.getValue());
      mplew.writeShort(fallingspeed);
      mplew.writeShort(fallingtime);
      return mplew.getPacket();
   }

   public static byte[] getBlizzardTempest(List<Triple<Integer, Integer, Integer>> lis) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.BlizzardTempest.getValue());
      mplew.writeInt(lis.size());
      Iterator var2 = lis.iterator();

      while(var2.hasNext()) {
         Triple<Integer, Integer, Integer> list = (Triple)var2.next();
         mplew.writeInt((Integer)list.getLeft());
         mplew.writeInt((Integer)list.getMid());
         mplew.writeInt((Integer)list.getRight());
      }

      return mplew.getPacket();
   }

   public static byte[] getDragonForm(MapleCharacter chr, int unk, int skillid, int skilllevel) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DRAGON_CHANGE.getValue());
      mplew.writeInt(chr.getId());
      mplew.writeInt(unk);
      mplew.writeInt(skillid);
      mplew.writeInt(skilllevel);
      return mplew.getPacket();
   }

   public static byte[] getDragonAttack(MapleCharacter chr, int skillid, int skilllevel, Point pos, Point pos2) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DRAGON_ATTACK.getValue());
      mplew.writeInt(chr.getId());
      mplew.writeInt(skillid);
      mplew.writeInt(skilllevel);
      mplew.writePosInt(pos);
      mplew.writePosInt(pos2);
      return mplew.getPacket();
   }

   public static byte[] getMechDoorCoolDown(MechDoor md) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MECH_DOOR_COOLDOWN.getValue());
      mplew.writeInt(md.getOwnerId());
      return mplew.getPacket();
   }

   public static byte[] RebolvingBunk(int cid, int oid, int mobcode, Point pos) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.REBOLVING_BUNK.getValue());
      mplew.writeInt(cid);
      mplew.write((int)1);
      mplew.write((int)1);
      mplew.writeInt(oid);
      mplew.writeInt(mobcode);
      mplew.writeInt(pos.x);
      mplew.writeInt(pos.y);
      return mplew.getPacket();
   }

   public static byte[] Novilityshiled(int shiled) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.NOVILITY_SHILED.getValue());
      mplew.writeInt(shiled);
      return mplew.getPacket();
   }

   public static byte[] getEarlySkillActive(int time) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.EARLY_SKILL_ACTIVE.getValue());
      mplew.writeInt(time);
      return mplew.getPacket();
   }

   public static final byte[] CrystalControl(MapleCharacter chr, int oid, Point pos, int skillid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DAMAGE_SUMMON.getValue());
      mplew.writeInt(chr.getId());
      mplew.writeInt(oid);
      mplew.writeInt(skillid);
      mplew.writeInt(skillid == 152101008 ? 20 : 1);
      mplew.writePosInt(pos);
      return mplew.getPacket();
   }

   public static final byte[] MarkinaMoveAttack(MapleCharacter chr, int oid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DAMAGE_SUMMON_2.getValue());
      mplew.writeInt(chr.getId());
      mplew.writeInt(oid);
      mplew.writeInt(9);
      return mplew.getPacket();
   }

   public static final byte[] CrystalTeleport(MapleCharacter chr, int oid, Point pos, int skillid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CRYSTAL_TELEPORT.getValue());
      mplew.writeInt(chr.getId());
      mplew.writeInt(oid);
      mplew.writePosInt(pos);
      return mplew.getPacket();
   }

   public static final byte[] BossMatchingChance(List<Pair<Integer, Integer>> list) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.BOSS_MATCHING_CHANCE.getValue());
      mplew.writeInt(list.size());
      Iterator var2 = list.iterator();

      while(var2.hasNext()) {
         Pair<Integer, Integer> li = (Pair)var2.next();
         mplew.writeInt((Integer)li.getLeft());
         mplew.writeInt((Integer)li.getRight());
      }

      return mplew.getPacket();
   }

   public static final byte[] ExpDropPenalty(boolean first, int totaltime, int nowtime, int exp, int drop) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.EXP_DROP_PENALTY.getValue());
      mplew.writeShort(first ? 0 : 1);
      mplew.writeInt(totaltime);
      mplew.writeInt(nowtime);
      mplew.writeInt(exp);
      mplew.writeInt(drop);
      return mplew.getPacket();
   }

   public static final byte[] PenaltyMsg(String msg, int type, int time, int type2) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.PENALTY_MSG.getValue());
      mplew.writeMapleAsciiString(msg);
      mplew.writeInt(type);
      mplew.writeInt(time);
      mplew.write((int)1);
      mplew.writeInt(type2);
      return mplew.getPacket();
   }

   public static final byte[] QuestMsg(String msg, int type, int time, int type2) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.QUEST_MSG.getValue());
      mplew.writeMapleAsciiString(msg);
      mplew.writeInt(type);
      mplew.writeInt(time);
      mplew.write((int)1);
      mplew.writeInt(type2);
      return mplew.getPacket();
   }

   public static byte[] getMapleCabinetList(List<MapleCabinet> mc, boolean give, int get, boolean show) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MAPLE_CABINET.getValue());
      if (show) {
         mplew.writeInt(11);
         mplew.writeInt(0);
      } else if (give) {
         mplew.writeInt(12);
         mplew.writeInt(get);
      } else {
         mplew.writeInt(9);
         mplew.writeInt(mc.size());
         if (mc.isEmpty()) {
            mplew.writeZeroBytes(100);
         }

         int i = mc.size();

         for(Iterator var6 = mc.iterator(); var6.hasNext(); --i) {
            MapleCabinet cb = (MapleCabinet)var6.next();
            mplew.write((int)1);
            mplew.writeInt(i);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeLong(cb.getSaveTime());
            mplew.writeMapleAsciiString(cb.getBigname());
            mplew.writeMapleAsciiString(cb.getSmallname());
            Item item = new Item(cb.getItemid(), (short)0, (short)cb.getCount(), 0, -1L);
            mplew.write((int)2);
            PacketHelper.addItemInfo(mplew, item);
         }
      }

      return mplew.getPacket();
   }

   public static byte[] getFieldSkillEffectAdd(int skillid, int skilllv, int mobid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FIELD_SKILL.getValue());
      mplew.writeInt(skillid);
      mplew.writeInt(skilllv);
      mplew.writeInt(mobid);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] getFieldSkillDuskAdd(int skillid, int skilllv, Point pos, boolean right, boolean sp) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FIELD_SKILL.getValue());
      if (sp) {
         mplew.writeInt(skillid);
         mplew.writeInt(skilllv);
         int ran = Randomizer.rand(0, 1);
         if (ran == 0) {
            mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 78 5A 00 00 01 8C 55 00 00 94 02 00 00 B4 FF FF FF B4 FF FF FF BC 07 00 00 03 00 00 00 00 00 00 00 02 00 61 31 00 00 77 F7 FF FF 2C 01 00 00 9C FF FF FF 64 00 00 00 16 03 00 00 61 FF FF FF 00 01 B0 4F 00 00 C8 0A 00 00 51 00 00 00 51 00 00 00 BC 07 00 00 04 00 00 00 00 00 00 00 02 00 61 32 00 00 77 F7 FF FF 2C 01 00 00 9C FF FF FF 64 00 00 00 0C FE FF FF 61 FF FF FF 00 00 00"));
         } else {
            mplew.write(HexTool.getByteArrayFromHexString("03 00 00 00 78 5A 00 00 01 B0 4F 00 00 C8 0A 00 00 4B 00 00 00 4B 00 00 00 BC 07 00 00 05 00 00 00 03 00 00 00 02 00 62 31 00 00 77 F7 FF FF 2C 01 00 00 9C FF FF FF 64 00 00 00 9E FD FF FF 61 FF FF FF 00 01 8C 55 00 00 94 02 00 00 AF FF FF FF AF FF FF FF BC 07 00 00 06 00 00 00 03 00 00 00 02 00 62 32 00 00 77 F7 FF FF 2C 01 00 00 9C FF FF FF 64 00 00 00 58 02 00 00 61 FF FF FF 00 00 00"));
         }
      } else {
         mplew.writeInt(skillid);
         mplew.writeInt(skilllv);
         mplew.writeInt(0);
         mplew.writeInt(1501);
         mplew.write((int)1);
         mplew.writeInt(1500);
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
         mplew.writeInt(pos.x);
         mplew.writeInt(pos.y);
         mplew.write(right);
         mplew.write((int)0);
      }

      return mplew.getPacket();
   }

   public static byte[] getDestoryedBackImg(String info1, String info2) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DESTORY_BACK_IMG.getValue());
      mplew.writeMapleAsciiString(info1);
      mplew.writeMapleAsciiString(info2);
      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static byte[] getFieldSkillAdd(int skillid, int skilllv, boolean remove) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(remove ? SendPacketOpcode.FIELD_SKILL_REMOVE.getValue() : SendPacketOpcode.FIELD_SKILL.getValue());
      mplew.writeInt(skillid);
      mplew.writeInt(skilllv);
      return mplew.getPacket();
   }

   public static byte[] getFieldSkillEffectAdd(int skillid, int skilllv, List<Point> startPoint) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FIELD_SKILL.getValue());
      mplew.writeInt(skillid);
      mplew.writeInt(skilllv);
      mplew.writeInt(startPoint.size());
      Iterator var4 = startPoint.iterator();

      while(var4.hasNext()) {
         Point sp = (Point)var4.next();
         mplew.writePosInt(sp);
      }

      return mplew.getPacket();
   }

   public static byte[] getFieldFootHoldAdd(int skillid, int skilllv, List<Triple<Point, String, Integer>> info, boolean remove) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(remove ? SendPacketOpcode.FIELD_SKILL_REMOVE.getValue() : SendPacketOpcode.FIELD_SKILL.getValue());
      mplew.writeInt(skillid);
      mplew.writeInt(skilllv);
      mplew.writeInt(info.size());
      Iterator var5 = info.iterator();

      while(var5.hasNext()) {
         Triple<Point, String, Integer> sinfo = (Triple)var5.next();
         mplew.writePosInt((Point)sinfo.getLeft());
         mplew.writeMapleAsciiString((String)sinfo.getMid());
         mplew.writeInt((Integer)sinfo.getRight());
      }

      return mplew.getPacket();
   }

   public static byte[] getFieldLaserAdd(int skillid, int skilllv, List<Triple<Point, Integer, Integer>> info) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FIELD_SKILL.getValue());
      mplew.writeInt(skillid);
      mplew.writeInt(skilllv);
      mplew.writeInt(info.size());
      Iterator var4 = info.iterator();

      while(var4.hasNext()) {
         Triple<Point, Integer, Integer> sinfo = (Triple)var4.next();
         mplew.writePosInt((Point)sinfo.getLeft());
         mplew.writeInt((Integer)sinfo.getMid());
         mplew.writeInt((Integer)sinfo.getRight());
      }

      return mplew.getPacket();
   }

   public static byte[] getFieldFinalLaserAdd(int skillid, int skilllv, List<Triple<Point, Point, Integer>> info, int delay) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.FIELD_SKILL.getValue());
      mplew.writeInt(skillid);
      mplew.writeInt(skilllv);
      mplew.writeInt(info.size());
      mplew.writeInt(skillid == 100016 ? 1400 : 2700);
      mplew.write((int)1);
      Iterator var5 = info.iterator();

      while(var5.hasNext()) {
         Triple<Point, Point, Integer> sinfo = (Triple)var5.next();
         mplew.writePosInt((Point)sinfo.getLeft());
         mplew.writePosInt((Point)sinfo.getMid());
         mplew.writeInt((Integer)sinfo.getRight());
         mplew.writeInt(delay);
      }

      return mplew.getPacket();
   }

   public static byte[] setMapOBJ(String str, int unk, int unk2, int unk3) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CHANNEL_BACK_IMG.getValue());
      mplew.write((int)1);
      mplew.writeMapleAsciiString(str);
      mplew.writeInt(unk);
      mplew.writeInt(unk2);
      mplew.write(unk3);
      return mplew.getPacket();
   }

   public static byte[] setSpecialMapEffect(String str, int unk, int unk2) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPECIAL_MAP_EFFECT_SET.getValue());
      mplew.writeMapleAsciiString(str);
      mplew.writeInt(unk);
      mplew.writeInt(unk2);
      return mplew.getPacket();
   }

   public static byte[] ChangeSpecialMapEffect(List<Pair<String, Integer>> str) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SPECIAL_MAP_EFFECT_CHANGE.getValue());
      mplew.writeInt(str.size());
      Iterator var2 = str.iterator();

      while(var2.hasNext()) {
         Pair<String, Integer> eff = (Pair)var2.next();
         mplew.writeMapleAsciiString((String)eff.getLeft());
         mplew.write((Integer)eff.getRight());
      }

      return mplew.getPacket();
   }

   public static byte[] getNowClock(int type) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
      mplew.write(type);
      mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis() + 120000L));
      return mplew.getPacket();
   }

   public static byte[] SetForceAtomTarget(int skillid, int unk, int size, int objid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SET_FORCE_ATOM_TARGET.getValue());
      mplew.writeInt(skillid);
      mplew.writeInt(unk);
      mplew.writeInt(size);
      mplew.writeInt(objid);
      return mplew.getPacket();
   }

   public static byte[] getChatEmoticon(byte type, short slot, short slot2, int emoticon, String a) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CHAT_EMOTICON.getValue());
      mplew.write(type);
      switch(type) {
      case 0:
         int size = ((List)ChatEmoticon.getEmoticons().get(emoticon)).size();
         mplew.writeShort(slot);
         mplew.writeInt(emoticon);
         mplew.writeInt(size);
         Iterator iterator = ((List)ChatEmoticon.getEmoticons().get(emoticon)).iterator();

         while(iterator.hasNext()) {
            int em = (Integer)iterator.next();
            mplew.writeInt(em);
            mplew.writeLong(PacketHelper.getTime(-2L));
            mplew.writeShort(0);
         }

         return mplew.getPacket();
      case 1:
      case 7:
      case 9:
         mplew.writeShort(slot);
         mplew.writeShort(slot2);
         break;
      case 2:
      case 3:
         mplew.writeShort(slot);
         break;
      case 4:
         mplew.writeShort(slot);
         mplew.writeZeroBytes(14);
         break;
      case 5:
         mplew.writeInt(emoticon);
         mplew.writeShort(slot);
         break;
      case 6:
         mplew.writeInt(emoticon);
         break;
      case 8:
         mplew.writeShort(slot);
         mplew.writeInt(emoticon);
         mplew.writeAsciiString(a, 25);
         break;
      case 10:
         mplew.writeShort(slot);
         break;
      case 11:
         mplew.writeInt(emoticon);
         mplew.writeMapleAsciiString(a);
      }

      return mplew.getPacket();
   }

   public static byte[] thunderAttack(int x, int y, int oid) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.THUNDER_ATTACK.getValue());
      mplew.writeInt(80001762);
      mplew.writeInt(1);
      mplew.writeInt(x);
      mplew.writeInt(y);
      mplew.writeInt(oid);
      return mplew.getPacket();
   }

   public static byte[] FeverMessage(byte mode) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.EQUIPMENT_ENCHANT_MESSAGE.getValue());
      mplew.writeInt(mode);
      return mplew.getPacket();
   }

   public static byte[] SpPortal(int mode, String path) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SP_PORTAL.getValue());
      mplew.writeInt(0);
      mplew.writeInt(mode);
      mplew.writeMapleAsciiString(path);
      return mplew.getPacket();
   }

   public static byte[] DojangRank(ResultSet ranks, ResultSet rank, int count) throws SQLException {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DOJANG_RANK.getValue());
      mplew.write(HexTool.getByteArrayFromHexString("00 CE 00 00 00 03 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 65 00 00 00 00 00 00 00 00 00 00 00 65 00 00 00 01 00 00 00 00 00 00 00 00 FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 65 00 00 00 00 00 00 00 00 00 00 00 65 00 00 00 03 00 00 00 00 "));
      mplew.writeInt(count);

      int var10000;
      int i;
      String timerecode;
      boolean avater;
      AvatarLook a;
      for(i = 1; i <= count; ++i) {
         mplew.writeInt(ranks.getInt("job"));
         mplew.writeInt(ranks.getInt("level"));
         var10000 = ranks.getInt("floor");
         timerecode = var10000 + ranks.getInt("time");
         mplew.writeInt(Integer.parseInt(timerecode));
         mplew.writeInt(i);
         mplew.writeMapleAsciiString(ranks.getString("name"));
         avater = i <= 3;
         mplew.write(avater);
         if (avater) {
            a = AvatarLook.init(ranks);
            a.encodeUnpackAvatarLook(mplew);
         }

         ranks.next();
      }

      mplew.writeInt(1);
      mplew.write((int)0);
      mplew.write((int)2);
      mplew.writeInt(count);

      for(i = 1; i <= count; ++i) {
         mplew.writeInt(rank.getInt("job"));
         mplew.writeInt(rank.getInt("level"));
         var10000 = rank.getInt("floor");
         timerecode = var10000 + rank.getInt("time");
         mplew.writeInt(Integer.parseInt(timerecode));
         mplew.writeInt(i);
         mplew.writeMapleAsciiString(rank.getString("name"));
         avater = i <= 3;
         mplew.write(avater);
         if (avater) {
            a = AvatarLook.init(rank);
            a.encodeUnpackAvatarLook(mplew);
         }

         rank.next();
      }

      return mplew.getPacket();
   }

   public static byte[] craftMake(int cid, int something, int time) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CRAFT_EFFECT.getValue());
      mplew.writeInt(cid);
      mplew.writeInt(something);
      mplew.writeInt(time);
      return mplew.getPacket();
   }

   public static byte[] craftFinished(int cid, int craftID, int ranking, int itemId, int quantity, int exp) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CRAFT_COMPLETE.getValue());
      mplew.writeInt(cid);
      mplew.writeInt(0);
      mplew.writeInt(craftID);
      mplew.writeInt(ranking);
      mplew.write((int)1);
      if (ranking == 25 || ranking == 21 || ranking == 26 || ranking == 27) {
         mplew.writeInt(itemId);
         mplew.writeInt(quantity);
      }

      mplew.writeInt(exp);
      return mplew.getPacket();
   }

   public static byte[] craftFinished2(int cid, int craftID, int ranking, List<Pair<Integer, Short>> itemlist) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CRAFT_COMPLETE.getValue());
      mplew.writeInt(cid);
      mplew.writeInt(1);
      mplew.writeInt(ranking);
      mplew.write((int)1);
      mplew.writeInt(itemlist.size());

      for(int i = 0; i < itemlist.size(); ++i) {
         mplew.writeInt(craftID);
         mplew.writeInt((Integer)((Pair)itemlist.get(i)).left);
         mplew.writeInt((Short)((Pair)itemlist.get(i)).right);
         mplew.writeLong(0L);
      }

      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static byte[] ItemMakerCooldown(int id, int cool) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ITEMMAKER_COOLDOWN.getValue());
      mplew.writeInt(id);
      mplew.writeInt(cool);
      return mplew.getPacket();
   }

   public static byte[] PangPangReactionReady(int skillid, int delay) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.PANGPANG_REACTION_READY.getValue());
      mplew.writeInt(skillid);
      mplew.writeInt(delay);
      return mplew.getPacket();
   }

   public static byte[] PangPangReactionAct(int time) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.PANGPANG_REACTION_ACT.getValue());
      mplew.writeInt(time);
      return mplew.getPacket();
   }

   public static byte[] PangPangReactionEnd(int time) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.PANGPANG_REACTION_END.getValue());
      mplew.writeInt(time);
      return mplew.getPacket();
   }

   public static byte[] onUserTeleport(MapleCharacter chr, int x, int y) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.USER_TELEPORT.getValue());
      packet.writeInt(chr.getId());
      packet.writeInt(x);
      packet.writeInt(y);
      return packet.getPacket();
   }

   public static byte[] onUserTeleport(int cid, int x, int y) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.TELEPORT_PLAYER.getValue());
      packet.writeInt(cid);
      packet.writeInt(x);
      packet.writeInt(y);
      return packet.getPacket();
   }

   public static byte[] MonkeyTogether(int type, boolean on) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.MONKEY_TOGETHER.getValue());
      packet.write(type);
      packet.write((int)43);
      packet.writeShort(0);
      packet.write(on);
      return packet.getPacket();
   }

   public static byte[] PartyRankingInfo(List<String> info) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.PARTY_RANK.getValue());
      packet.writeInt(-1);
      packet.writeInt(5);
      packet.writeInt(0);
      packet.writeInt(info.size());

      for(int i = 0; i < info.size(); ++i) {
         packet.writeInt(1);
         packet.writeInt(4);
         String[] player = ((String)info.get(i)).split(",");
         packet.writeMapleAsciiString(player[0]);
         packet.writeMapleAsciiString(player[1]);
         packet.writeMapleAsciiString(player[2]);
         packet.writeMapleAsciiString(player[3]);
      }

      return packet.getPacket();
   }

   public static byte[] DojangTraining() {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.DOJANG_TRAINING.getValue());
      packet.write(HexTool.getByteArrayFromHexString("C9 00 00 00 64 00 00 00 0F 27 00 00 EE 02 00 00 0A 00 00 00 01 00 00 00 2C 01 00 00 00 00 00 00 F4 01 00 00 07 00 00 00 00 F2 05 2A 01 00 00 00 00 E4 0B 54 02 00 00 00 00 AC 23 FC 06 00 00 00 00 74 3B A4 0B 00 00 00 00 5C B2 EC 22 00 00 00 00 88 52 6A 74 00 00 00 FF FF FF FF FF FF FF 7F 06 00 00 00 00 E8 76 48 17 00 00 00 00 10 A5 D4 E8 00 00 00 00 A0 72 4E 18 09 00 00 00 40 7A 10 F3 5A 00 00 00 80 C6 A4 7E 8D 03 00 FF FF FF FF FF FF FF 7F 03 00 00 00 00 00 00 00 04 00 00 00 24 0E 96 00 25 0E 96 00 26 0E 96 00 27 0E 96 00 01 00 00 00 04 00 00 00 28 0E 96 00 29 0E 96 00 2A 0E 96 00 2B 0E 96 00 02 00 00 00 04 00 00 00 2C 0E 96 00 2D 0E 96 00 2E 0E 96 00 2F 0E 96 00 03 00 00 00 01 00 00 00 01 00 00 00 01 00 00 00 8C 00 00 00 02 00 00 00 01 00 00 00 01 00 00 00 28 05 00 00 03 00 00 00 01 00 00 00 01 00 00 00 C8 00 00 00"));
      return packet.getPacket();
   }

   public static byte[] DojangFieldSetting(int forcetype, int fieldforce) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.DOJANG_TRAINING.getValue());
      packet.writeInt(202);
      packet.writeInt(forcetype);
      packet.writeInt(fieldforce);
      return packet.getPacket();
   }

   public static byte[] V_BLESS(int skillid, boolean check) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.V_BLESS.getValue());
      packet.writeInt(skillid);
      packet.write(check);
      return packet.getPacket();
   }

   public static byte[] Abyssal_Lightning(int skillid, int count, int x) {
      MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
      packet.writeShort(SendPacketOpcode.ABYSSAL_ATTACK.getValue());
      packet.writeInt(skillid);
      packet.writeInt(count);
      packet.writeInt(0);
      packet.writeInt(0);
      packet.write((int)0);

      for(int i = 0; i < count; ++i) {
         packet.writeInt(x);
         packet.writeInt(0);
      }

      return packet.getPacket();
   }

   public static class FarmPacket {
      public static byte[] onEnterFarm(MapleCharacter chr) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.ENTER_FARM.getValue());
         PacketHelper.addCharacterInfo(mplew, chr);

         for(int v13 = 0; v13 < 37500; v13 += 60) {
            mplew.writeInt(v13 < 3000 ? 4150001 : 0);
            mplew.writeInt(0);
            mplew.write((int)0);
            mplew.writeInt(0);
            mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
         }

         mplew.writeInt(14);
         mplew.writeInt(14);
         mplew.writeInt(0);
         mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
         return mplew.getPacket();
      }

      public static byte[] onSetFarmUser(MapleCharacter chr) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SET_FARM_USER.getValue());
         mplew.writeInt(chr.getClient().getAccID());
         mplew.writeInt(0);
         mplew.writeLong(0L);
         PacketHelper.addFarmInfo(mplew, chr.getClient(), 0);
         farmUserGameInfo(mplew, false, chr);
         PacketHelper.addFarmInfo(mplew, chr.getClient(), 0);
         farmUserGameInfo(mplew, false, chr);
         mplew.writeInt(0);
         mplew.writeInt(-1);
         mplew.write((int)0);
         return mplew.getPacket();
      }

      public static void farmUserGameInfo(MaplePacketLittleEndianWriter mplew, boolean unk, MapleCharacter chr) {
         mplew.write(unk);
         if (unk) {
            mplew.writeInt(chr.getClient().getWorld());
            mplew.writeMapleAsciiString("et");
            mplew.writeInt(chr.getId());
            mplew.writeMapleAsciiString(chr.getName());
         }

      }

      public static byte[] onFarmNotice(String str) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.FARM_NOTICE.getValue());
         mplew.writeMapleAsciiString(str);
         return mplew.getPacket();
      }

      public static byte[] onFarmSetInGameInfo(MapleCharacter chr) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.FARM_SET_INGAME_INFO.getValue());
         farmUserGameInfo(mplew, false, chr);
         return mplew.getPacket();
      }

      public static byte[] onFarmRequestSetInGameInfo(MapleCharacter chr) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.FARM_REQ_SET_INGAME_INFO.getValue());
         farmUserGameInfo(mplew, false, chr);
         mplew.writeInt(chr.getClient().getWorld());
         mplew.writeMapleAsciiString("The Black");
         mplew.writeInt(chr.getId());
         mplew.writeMapleAsciiString(chr.getName());
         return mplew.getPacket();
      }

      public static byte[] onFarmImgUpdate(MapleClient c, int length, byte[] img) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.FARM_IMG_UPDATE.getValue());
         mplew.writeInt(c.getAccID());
         mplew.writeInt(length);
         mplew.write(img);
         return mplew.getPacket();
      }
   }

   public static class AuctionPacket {
      public static void auctionHistory(MaplePacketLittleEndianWriter mplew, AuctionHistory history) {
         mplew.writeLong(history.getId());
         mplew.writeInt(history.getAuctionId());
         mplew.writeInt(history.getAccountId());
         mplew.writeInt(history.getCharacterId());
         mplew.writeInt(history.getItemId());
         mplew.writeInt(history.getState());
         mplew.writeLong(history.getPrice());
         mplew.writeLong(PacketHelper.getTime(history.getBuyTime()));
         mplew.writeInt(history.getDeposit());
         mplew.writeInt(history.getDeposit());
         mplew.writeInt(history.getQuantity());
         mplew.writeInt(history.getWorldId());
      }

      public static void auctionItem(MaplePacketLittleEndianWriter mplew, AuctionItem item) {
         mplew.writeInt(item.getAuctionId());
         mplew.writeInt(item.getAuctionType());
         mplew.writeInt(item.getState());
         mplew.writeInt(item.getWorldId());
         mplew.writeLong(item.getPrice());
         mplew.writeLong(item.getSecondPrice());
         mplew.writeLong(item.getDirectPrice());
         mplew.writeLong(item.getPrice());
         if (GameConstants.getInventoryType(item.getItem().getItemId()) == MapleInventoryType.CASH && item.getItem().getQuantity() > 1) {
            mplew.writeLong(Double.doubleToRawLongBits((double)(item.getPrice() / (long)item.getItem().getQuantity())));
         } else {
            mplew.writeLong(Double.doubleToRawLongBits((double)item.getPrice()));
         }

         mplew.writeLong(PacketHelper.getTime(item.getEndDate()));
         mplew.writeLong(PacketHelper.getTime(item.getRegisterDate()));
         mplew.writeInt(item.getDeposit());
         mplew.writeInt(item.getDeposit());
         mplew.writeInt(item.getsStype());
         mplew.writeInt(item.getBidWorld());
         mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
         mplew.write(item.getState() == 4);
         if (item.getState() == 4) {
            mplew.writeLong(0L);
            mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
            mplew.writeShort(0);
         }

      }

      public static byte[] AuctionCompleteItems(List<AuctionItem> items) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
         mplew.writeInt(51);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(items.size());
         Iterator var2 = items.iterator();

         while(true) {
            AuctionItem item;
            do {
               if (!var2.hasNext()) {
                  return mplew.getPacket();
               }

               item = (AuctionItem)var2.next();
               auctionHistory(mplew, item.getHistory());
               mplew.write(item.getState() <= 4 || item.getState() >= 7);
            } while(item.getState() > 4 && item.getState() < 7);

            auctionItem(mplew, item);
            PacketHelper.addItemInfo(mplew, item.getItem());
         }
      }

      public static byte[] AuctionCompleteItemUpdate(AuctionItem item) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
         mplew.writeInt(71);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeLong(item.getItem().getInventoryId());
         mplew.write(true);
         auctionHistory(mplew, item.getHistory());
         mplew.write(item.getState() <= 4 || item.getState() >= 7);
         if (item.getState() <= 4 || item.getState() >= 7) {
            auctionItem(mplew, item);
            PacketHelper.addItemInfo(mplew, item.getItem());
         }

         return mplew.getPacket();
      }

      public static byte[] AuctionCompleteItemUpdate(AuctionItem item, Item item2) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
         mplew.writeInt(71);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeLong(item.getItem().getInventoryId());
         mplew.write(true);
         auctionHistory(mplew, item.getHistory());
         mplew.write(true);
         auctionItem(mplew, item);
         PacketHelper.addItemInfo(mplew, item2);
         return mplew.getPacket();
      }

      public static byte[] AuctionBuyItemUpdate(AuctionItem item, boolean remain) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
         mplew.writeInt(73);
         mplew.writeInt(0);
         mplew.writeInt(item.getAuctionId());
         mplew.write(remain);
         if (remain) {
            auctionItem(mplew, item);
            PacketHelper.addItemInfo(mplew, item.getItem());
         }

         return mplew.getPacket();
      }

      public static byte[] AuctionSellingMyItems(List<AuctionItem> items) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
         mplew.writeInt(50);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(items.size());
         Iterator var2 = items.iterator();

         while(var2.hasNext()) {
            AuctionItem item = (AuctionItem)var2.next();
            auctionItem(mplew, item);
            PacketHelper.addItemInfo(mplew, item.getItem());
         }

         return mplew.getPacket();
      }

      public static byte[] AuctionStopSell(AuctionItem item) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
         mplew.writeInt(12);
         mplew.writeInt(0);
         mplew.writeInt(item.getAuctionId());
         return mplew.getPacket();
      }

      public static byte[] AuctionCompleteMesoResult() {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
         mplew.writeInt(30);
         mplew.writeInt(0);
         mplew.writeInt(0);
         return mplew.getPacket();
      }

      public static byte[] AuctionCompleteItemResult() {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
         mplew.writeInt(31);
         mplew.writeInt(0);
         mplew.writeInt(0);
         return mplew.getPacket();
      }

      public static byte[] AuctionWishlist(List<AuctionItem> wishItems) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
         mplew.writeInt(46);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(wishItems.size());
         Iterator var2 = wishItems.iterator();

         while(var2.hasNext()) {
            AuctionItem item = (AuctionItem)var2.next();
            auctionItem(mplew, item);
            PacketHelper.addItemInfo(mplew, item.getItem());
         }

         return mplew.getPacket();
      }

      public static byte[] AuctionBuyEquipResult(int type, int dwAuctionID) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
         mplew.writeInt(20);
         mplew.writeInt(type);
         mplew.writeInt(dwAuctionID);
         return mplew.getPacket();
      }

      public static byte[] AuctionBuyItemResult(int type, int dwAuctionID) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
         mplew.writeInt(21);
         mplew.writeInt(type);
         mplew.writeInt(dwAuctionID);
         return mplew.getPacket();
      }

      public static byte[] AuctionWishlistUpdate(int dwAuctionID) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
         mplew.writeInt(72);
         mplew.writeInt(0);
         mplew.writeInt(dwAuctionID);
         mplew.write(false);
         return mplew.getPacket();
      }

      public static byte[] AuctionWishlistDeleteResult(int dwAuctionId) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
         mplew.writeInt(47);
         mplew.writeInt(0);
         mplew.writeInt(dwAuctionId);
         return mplew.getPacket();
      }

      public static byte[] AuctionAddWishlist(AuctionItem item) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
         mplew.writeInt(72);
         mplew.writeInt(0);
         mplew.writeInt(item.getAuctionId());
         mplew.write(true);
         auctionItem(mplew, item);
         PacketHelper.addItemInfo(mplew, item.getItem());
         return mplew.getPacket();
      }

      public static byte[] AuctionOn() {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         return mplew.getPacket();
      }

      public static byte[] AuctionOff() {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
         mplew.writeInt(1);
         mplew.writeInt(0);
         mplew.writeInt(0);
         return mplew.getPacket();
      }

      public static byte[] AuctionWishlistResult(AuctionItem item) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
         mplew.writeInt(45);
         mplew.writeInt(0);
         mplew.writeInt(item.getAuctionId());
         return mplew.getPacket();
      }

      public static byte[] AuctionMarketPrice(List<AuctionItem> items) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
         mplew.writeInt(41);
         mplew.writeInt(1000);
         mplew.writeInt(0);
         mplew.writeShort(1);
         mplew.writeInt(items.size());
         List<AuctionItem> itemz = new CopyOnWriteArrayList();
         itemz.addAll(items);
         Iterator var3 = itemz.iterator();

         while(var3.hasNext()) {
            AuctionItem item = (AuctionItem)var3.next();
            item.setState(3);
            auctionItem(mplew, item);
            PacketHelper.addItemInfo(mplew, item.getItem());
         }

         mplew.writeZeroBytes(100);
         return mplew.getPacket();
      }

      public static byte[] AuctionSearchItems(List<AuctionItem> items) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
         mplew.writeInt(40);
         mplew.writeInt(1000);
         mplew.writeInt(0);
         mplew.writeShort(1);
         mplew.writeInt(items.size());
         Iterator var2 = items.iterator();

         while(var2.hasNext()) {
            AuctionItem item = (AuctionItem)var2.next();
            auctionItem(mplew, item);
            PacketHelper.addItemInfo(mplew, item.getItem());
         }

         mplew.writeZeroBytes(100);
         return mplew.getPacket();
      }

      public static byte[] AuctionSellItemUpdate(AuctionItem item) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
         mplew.writeInt(70);
         mplew.writeInt(0);
         mplew.writeInt(item.getAuctionId());
         mplew.write(true);
         auctionItem(mplew, item);
         PacketHelper.addItemInfo(mplew, item.getItem());
         return mplew.getPacket();
      }

      public static byte[] AuctionSellItem(AuctionItem item) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
         mplew.writeInt(10);
         mplew.writeInt(0);
         mplew.writeInt(item.getAuctionId());
         return mplew.getPacket();
      }

      public static byte[] AuctionReSellItem(AuctionItem item) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
         mplew.writeInt(11);
         mplew.writeInt(0);
         mplew.writeInt(item.getAuctionId());
         return mplew.getPacket();
      }
   }

   public static class InteractionPacket {
      public static byte[] getTradeInvite(MapleCharacter c, boolean isTrade) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
         mplew.write(PlayerInteractionHandler.Interaction.INVITE_TRADE.action);
         mplew.write((int)0);
         mplew.writeShort(0);
         mplew.write(isTrade ? 4 : 3);
         mplew.writeInt(0);
         mplew.writeMapleAsciiString(c.getName());
         mplew.writeInt(c.getId());
         mplew.writeInt(c.getJob());
         return mplew.getPacket();
      }

      public static byte[] getMarriageInvite(MapleCharacter c) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
         mplew.write(PlayerInteractionHandler.Interaction.INVITE_TRADE.action);
         mplew.write((int)0);
         mplew.writeShort(0);
         mplew.write((int)8);
         mplew.writeMapleAsciiString(c.getName());
         mplew.writeInt(c.getJob());
         mplew.writeInt(0);
         return mplew.getPacket();
      }

      public static byte[] getCashTradeInvite(MapleCharacter c) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
         mplew.write(PlayerInteractionHandler.Interaction.INVITE_TRADE.action);
         mplew.write((int)0);
         mplew.writeShort(0);
         mplew.write((int)7);
         mplew.writeMapleAsciiString(c.getName());
         mplew.writeInt(c.getJob());
         mplew.writeInt(0);
         return mplew.getPacket();
      }

      public static byte[] getTradeMesoSet(byte number, long meso) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
         mplew.write(PlayerInteractionHandler.Interaction.SET_MESO1.action);
         mplew.write((int)0);
         mplew.writeShort(0);
         mplew.write(number);
         mplew.writeLong(meso);
         return mplew.getPacket();
      }

      public static byte[] getTradeItemAdd(byte number, Item item) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
         mplew.write(PlayerInteractionHandler.Interaction.SET_ITEMS1.action);
         mplew.write((int)0);
         mplew.writeShort(0);
         mplew.write(number);
         mplew.write((int)item.getPosition());
         PacketHelper.addItemInfo(mplew, item);
         return mplew.getPacket();
      }

      public static byte[] getTradeStart(MapleClient c, MapleTrade trade, byte number, boolean isTrade) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
         mplew.write((int)20);
         mplew.write((int)0);
         mplew.writeShort(0);
         mplew.write(isTrade ? 4 : 3);
         mplew.write((int)2);
         mplew.write(number);
         if (number == 1) {
            mplew.write((int)0);
            AvatarLook.encodeAvatarLook(mplew, trade.getPartner().getChr(), false, false);
            mplew.writeMapleAsciiString(trade.getPartner().getChr().getName());
            mplew.writeShort(trade.getPartner().getChr().getJob());
            mplew.writeInt(0);
         }

         mplew.write(number);
         AvatarLook.encodeAvatarLook(mplew, c.getPlayer(), false, false);
         mplew.writeMapleAsciiString(c.getPlayer().getName());
         mplew.writeShort(c.getPlayer().getJob());
         mplew.writeInt(0);
         mplew.write((int)255);
         return mplew.getPacket();
      }

      public static byte[] getCashTradeStart(MapleClient c, MapleTrade trade, byte number) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
         mplew.write((int)20);
         mplew.write((int)0);
         mplew.writeShort(0);
         mplew.write((int)7);
         mplew.write((int)2);
         mplew.write(number);
         if (number == 1) {
            mplew.write((int)0);
            AvatarLook.encodeAvatarLook(mplew, trade.getPartner().getChr(), false, false);
            mplew.writeMapleAsciiString(trade.getPartner().getChr().getName());
            mplew.writeShort(trade.getPartner().getChr().getJob());
            mplew.writeInt(0);
         }

         mplew.write(number);
         AvatarLook.encodeAvatarLook(mplew, c.getPlayer(), false, false);
         mplew.writeMapleAsciiString(c.getPlayer().getName());
         mplew.writeShort(c.getPlayer().getJob());
         mplew.writeInt(0);
         mplew.write((int)0);
         mplew.write((int)255);
         return mplew.getPacket();
      }

      public static byte[] getTradeConfirmation() {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
         mplew.write(PlayerInteractionHandler.Interaction.CONFIRM_TRADE1.action);
         mplew.write((int)0);
         mplew.writeShort(0);
         return mplew.getPacket();
      }

      public static byte[] TradeMessage(byte UserSlot, byte message) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
         mplew.write(PlayerInteractionHandler.Interaction.EXIT.action);
         mplew.write((int)0);
         mplew.writeShort(0);
         mplew.write(UserSlot);
         mplew.write(message);
         return mplew.getPacket();
      }

      public static byte[] getTradeCancel(byte UserSlot) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
         mplew.write(PlayerInteractionHandler.Interaction.EXIT.action);
         mplew.write((int)0);
         mplew.writeShort(0);
         mplew.write(UserSlot);
         mplew.write((int)2);
         return mplew.getPacket();
      }
   }

   public static class NPCPacket {
      public static byte[] spawnNPC(MapleNPC life, boolean show) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SPAWN_NPC.getValue());
         mplew.writeInt(life.getObjectId());
         mplew.writeInt(life.getId());
         mplew.writeShort(life.getPosition().x);
         mplew.writeShort(life.getCy());
         mplew.writeLong(-1L);
         mplew.write((int)0);
         mplew.write(life.getF() == 1 ? 0 : 1);
         mplew.writeShort(life.getFh());
         mplew.writeShort(life.getRx0());
         mplew.writeShort(life.getRx1());
         mplew.write(show ? 1 : 0);
         mplew.writeInt(0);
         mplew.write((int)0);
         mplew.writeInt(0);
         mplew.writeInt(-1);
         mplew.writeLong(PacketHelper.getTime(-2L));
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeMapleAsciiString("");
         return mplew.getPacket();
      }

      public static byte[] getNPCTalks(int npc, byte msgType, String talk, String endBytes, byte type, int diffNPC) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
         mplew.writeInt(0);
         mplew.write((int)3);
         mplew.writeInt(0);
         mplew.write((int)1);
         mplew.write(msgType);
         mplew.writeShort(0);
         mplew.write((int)0);
         mplew.write((int)0);
         mplew.writeShort(type);
         mplew.write((int)1);
         if (diffNPC > 0) {
            mplew.writeInt(diffNPC);
         }

         mplew.writeMapleAsciiString(talk);
         mplew.write(HexTool.getByteArrayFromHexString(endBytes));
         mplew.writeInt(0);
         return mplew.getPacket();
      }

      public static byte[] spawnNPC2(MapleNPC life, boolean show) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SPAWN_NPC.getValue());
         mplew.writeInt(life.getObjectId());
         mplew.writeInt(life.getId());
         mplew.writeShort(life.getPosition().x);
         mplew.writeShort(life.getCy());
         mplew.writeLong(-1L);
         mplew.write((int)1);
         mplew.write((int)1);
         mplew.writeShort(life.getFh());
         mplew.writeShort(life.getRx0());
         mplew.writeShort(life.getRx1());
         mplew.write(show ? 1 : 0);
         mplew.writeInt(0);
         mplew.write((int)0);
         mplew.writeInt(-1);
         mplew.writeInt(0);
         mplew.writeInt(1000);
         mplew.writeMapleAsciiString("");
         mplew.write((int)0);
         return mplew.getPacket();
      }

      public static byte[] removeNPC(int objectid) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.REMOVE_NPC.getValue());
         mplew.writeInt(objectid);
         return mplew.getPacket();
      }

      public static byte[] removeNPCController(int objectid) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
         mplew.write((int)0);
         mplew.writeInt(objectid);
         return mplew.getPacket();
      }

      public static byte[] spawnNPCRequestController(MapleNPC life, boolean MiniMap) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
         mplew.write((int)1);
         mplew.writeInt(life.getObjectId());
         mplew.writeInt(life.getId());
         mplew.writeShort(life.getPosition().x);
         mplew.writeShort(life.getCy());
         mplew.writeLong(-1L);
         mplew.write(life.getF() == 1 ? 0 : 1);
         mplew.write(life.isLeft());
         mplew.writeShort(life.getFh());
         mplew.writeShort(life.getRx0());
         mplew.writeShort(life.getRx1());
         mplew.writeShort(MiniMap ? 1 : 0);
         mplew.writeInt(0);
         mplew.writeInt(-1);
         mplew.writeZeroBytes(11);
         return mplew.getPacket();
      }

      public static byte[] setNPCScriptable(List<Pair<Integer, String>> npcs) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.NPC_SCRIPTABLE.getValue());
         mplew.write(npcs.size());
         Iterator var2 = npcs.iterator();

         while(var2.hasNext()) {
            Pair<Integer, String> s = (Pair)var2.next();
            mplew.writeInt((Integer)s.left);
            mplew.writeMapleAsciiString((String)s.right);
            mplew.writeInt(0);
            mplew.writeInt(Integer.MAX_VALUE);
         }

         return mplew.getPacket();
      }

      public static byte[] setNPCMoveAction(int oid, String s) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.NPC_ACTION.getValue());
         mplew.writeInt(oid);
         mplew.write(HexTool.getByteArrayFromHexString(s));
         return mplew.getPacket();
      }

      public static byte[] setNPCMotion(int oid, int type) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.NPC_CHANGE_ACTION.getValue());
         mplew.writeInt(oid);
         mplew.writeInt(type);
         return mplew.getPacket();
      }

      public static byte[] getNPCTalk(int npc, byte msgType, String talk, String endBytes, byte type) {
         return getNPCTalk(npc, msgType, talk, endBytes, type, npc, false, false);
      }

      public static byte[] getNPCTalk(int npc, byte msgType, String talk, String endBytes, byte type, int npc2) {
         return getNPCTalk(npc, msgType, talk, endBytes, type, npc2, false, false);
      }

      public static byte[] getNPCTalk(int npc, byte msgType, String talk, String endBytes, byte type, int diffNPC, boolean illust, boolean isLeft) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
         boolean sp = false;
         switch(type) {
         case 5:
         case 17:
         case 23:
         case 37:
         case 57:
            sp = true;
         default:
            mplew.writeInt(0);
            mplew.write(sp ? 3 : 4);
            mplew.writeInt(type != 17 && type != 23 ? npc : 0);
            mplew.write(sp ? 1 : 0);
            mplew.write(msgType);
            if (sp) {
               mplew.writeInt(0);
            }

            mplew.write(type);
            mplew.write((int)0);
            mplew.write(!sp && (type & 4) == 0 ? 0 : 1);
            if (msgType == 0) {
               mplew.writeInt(1);
            }

            if ((type & 4) != 0 && type != 57) {
               if (diffNPC == 0) {
                  diffNPC = npc;
               }

               mplew.writeInt(diffNPC);
            }

            if (msgType == 19) {
               mplew.writeLong(5L);
            }

            mplew.writeMapleAsciiString(talk);
            if (msgType != 19) {
               if (msgType != 28 && msgType != 30) {
                  mplew.write(HexTool.getByteArrayFromHexString(endBytes));
                  if (type == 37 && talk.contains("꿈이 무너지")) {
                     mplew.writeInt(3000);
                  }
               }

               mplew.writeInt(illust ? npc : 0);
               if (illust) {
                  mplew.writeInt(diffNPC);
                  mplew.write(isLeft);
               }
            }

            return mplew.getPacket();
         }
      }

      public static byte[] getPraticeReplace(int npc, byte msgType, String talk, String endBytes, byte type, int unk) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
         mplew.writeInt(0);
         mplew.write((int)4);
         mplew.writeInt(npc);
         mplew.write((int)0);
         mplew.write(msgType);
         mplew.write(type);
         mplew.write(unk);
         mplew.write((int)0);
         mplew.writeMapleAsciiString(talk);
         return mplew.getPacket();
      }

      public static byte[] getNPCConductExchangeTalk(int npc, String msg) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
         mplew.writeInt(0);
         mplew.write((int)4);
         mplew.writeInt(npc);
         mplew.write((int)0);
         mplew.writeShort(3);
         mplew.writeShort(1);
         mplew.writeMapleAsciiString(msg);
         return mplew.getPacket();
      }

      public static byte[] getIlust(int npc, int type, boolean lumi) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
         mplew.writeInt(0);
         mplew.write((int)3);
         mplew.writeInt(0);
         mplew.write((int)1);
         mplew.writeInt(npc);
         mplew.writeInt(type);
         mplew.writeInt(lumi ? 0 : 1);
         if (lumi) {
            mplew.write((int)1);
            mplew.writeInt(1);
            mplew.writeInt(2);
            mplew.writeMapleAsciiString("빛의 길");
            mplew.writeMapleAsciiString("어둠의 길");
         } else {
            mplew.write((int)1);
            mplew.writeInt(1);
            mplew.writeInt(2);
            mplew.writeMapleAsciiString("데몬 어벤져");
            mplew.writeMapleAsciiString("데몬 슬레이어");
         }

         return mplew.getPacket();
      }

      public static byte[] getMapSelection(int npcid, String sel) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
         mplew.writeInt(0);
         mplew.write((int)4);
         mplew.writeInt(npcid);
         mplew.write((int)0);
         mplew.write((int)17);
         mplew.writeShort(0);
         mplew.write((int)0);
         mplew.write((int)0);
         mplew.writeInt(npcid == 2083006 ? 1 : 0);
         mplew.write((int)0);
         mplew.writeInt(npcid == 9010022 ? 1 : 0);
         mplew.writeMapleAsciiString(sel);
         return mplew.getPacket();
      }

      public static byte[] getNPCTalkMixStyle(int npcId, String talk, boolean isZeroBeta, boolean isAngelicBuster) {
         MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
         packet.writeShort(SendPacketOpcode.NPC_TALK.getValue());
         packet.writeInt(0);
         packet.write((int)4);
         packet.writeInt(npcId);
         packet.write((int)0);
         packet.write((int)44);
         packet.writeShort(0);
         packet.write((int)0);
         packet.writeInt(0);
         packet.write(isAngelicBuster ? 1 : 0);
         packet.writeInt(isZeroBeta ? 1 : 0);
         packet.writeInt(50);
         packet.writeMapleAsciiString(talk);
         return packet.getPacket();
      }

      public static byte[] getStylePreview(int itemid, int talk, int type, int beta, int beta2, int alpha, int alpha2) {
         MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
         packet.writeShort(SendPacketOpcode.BEAUTY_PREVIEW.getValue());
         packet.writeInt(itemid);
         packet.write((int)1);
         packet.write((int)1);
         packet.write((int)1);
         packet.writeInt(talk);
         packet.write(type);
         packet.write((int)2);
         packet.writeInt(beta);
         packet.writeInt(beta2);
         if (type == 4) {
            packet.write(HexTool.getByteArrayFromHexString("00 FF 00 00 FF 00 00"));
         }

         packet.write(type);
         packet.write((int)0);
         packet.writeInt(alpha);
         packet.writeInt(alpha2);
         if (type == 4) {
            packet.write(HexTool.getByteArrayFromHexString("00 FF 00 00 FF 00 00"));
         }

         return packet.getPacket();
      }

      public static byte[] getNPCTalkStyle(int npc, String talk, List<Integer> args) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
         mplew.writeInt(0);
         mplew.write((int)4);
         mplew.writeInt(npc);
         mplew.write((int)0);
         mplew.write((int)10);
         mplew.writeShort(0);
         mplew.write((int)0);
         mplew.write((int)0);
         mplew.write((int)0);
         mplew.writeMapleAsciiString(talk);
         mplew.writeInt(0);
         mplew.write(args.size());

         for(int i = 0; i < args.size(); ++i) {
            mplew.writeInt((Integer)args.get(i));
         }

         return mplew.getPacket();
      }

      public static byte[] getNPCTalkStyle(MapleCharacter chr, int npc, String talk, int... args) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
         mplew.writeInt(0);
         mplew.write((int)4);
         mplew.writeInt(npc);
         mplew.write((int)0);
         mplew.write((int)10);
         mplew.writeShort(0);
         mplew.write((int)0);
         mplew.write(GameConstants.isAngelicBuster(chr.getJob()) ? (chr.getDressup() ? 1 : 0) : 0);
         mplew.write(GameConstants.isZero(chr.getJob()) ? (chr.getGender() == 1 ? 1 : 0) : 0);
         mplew.writeMapleAsciiString(talk);
         mplew.write((int)0);
         mplew.writeInt(chr.getHair());
         mplew.write((int)-1);
         mplew.write((int)0);
         mplew.write((int)0);
         mplew.writeInt(chr.getFace());
         mplew.write(args.length);

         for(int i = 0; i < args.length; ++i) {
            mplew.writeInt(args[i]);
         }

         return mplew.getPacket();
      }

      public static byte[] getNPCTalkStyleAndroid(int npcId, String talk, int... args) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
         mplew.writeInt(0);
         mplew.write((int)4);
         mplew.writeInt(npcId);
         mplew.write((int)0);
         mplew.writeShort(11);
         mplew.writeShort(0);
         mplew.write((int)0);
         mplew.writeInt(0);
         mplew.writeMapleAsciiString(talk);
         mplew.write(args.length);
         System.err.println("args ===" + args.length);

         for(int i = 0; i < args.length; ++i) {
            mplew.writeInt(args[i]);
         }

         return mplew.getPacket();
      }

      public static byte[] getNPCTalkStyleZero(int npcId, String talk, int[] args1, int[] args2) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
         mplew.writeInt(0);
         mplew.write((int)4);
         mplew.writeInt(npcId);
         mplew.write((int)0);
         mplew.write((int)36);
         mplew.writeShort(0);
         mplew.write((int)0);
         mplew.writeMapleAsciiString(talk);
         mplew.writeZeroBytes(23);
         mplew.write(args1.length);

         int i;
         for(i = 0; i < args1.length; ++i) {
            mplew.writeInt(args1[i]);
         }

         mplew.writeInt(1);
         mplew.write(args2.length);

         for(i = 0; i < args2.length; ++i) {
            mplew.writeInt(args2[i]);
         }

         return mplew.getPacket();
      }

      public static byte[] getNPCTalkNum(int npc, String talk, int def, int min, int max) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
         mplew.writeInt(0);
         mplew.write((int)4);
         mplew.writeInt(npc);
         mplew.write((int)0);
         mplew.write((int)5);
         mplew.writeShort(0);
         mplew.write((int)0);
         mplew.writeMapleAsciiString(talk);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(def);
         mplew.writeInt(0);
         mplew.writeInt(min);
         mplew.writeInt(max);
         return mplew.getPacket();
      }

      public static byte[] getNPCTalkText(int npc, String talk) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
         mplew.writeInt(0);
         mplew.write((int)4);
         mplew.writeInt(npc);
         mplew.write((int)0);
         mplew.write((int)4);
         mplew.writeShort(4);
         mplew.write((int)0);
         mplew.writeInt(npc);
         mplew.writeMapleAsciiString(talk);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         return mplew.getPacket();
      }

      public static byte[] getEvanTutorial(String data) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
         mplew.writeInt(0);
         mplew.write((int)8);
         mplew.writeInt(0);
         mplew.write((int)1);
         mplew.write((int)1);
         mplew.writeShort(0);
         mplew.write((int)1);
         mplew.writeMapleAsciiString(data);
         mplew.writeInt(0);
         return mplew.getPacket();
      }

      public static byte[] getNPCShop(int sid, MapleShop shop, MapleClient c) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.OPEN_NPC_SHOP.getValue());
         mplew.writeInt(sid);
         mplew.write((int)0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(0);
         PacketHelper.addShopInfo(mplew, shop, c);
         return mplew.getPacket();
      }

      public static byte[] BossRewardSetting() {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.OPEN_NPC_SHOP.getValue() + 2);
         List<Triple<Integer, Integer, Integer>> list = BossRewardMeso.getLists();
         mplew.write((int)1);
         mplew.writeInt(1);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(2);
         mplew.writeInt(0);
         mplew.writeInt(0);
         mplew.writeInt(8);

         for(int i = 0; i < 90; ++i) {
            mplew.writeLong(PacketHelper.getTime(-2L));
            mplew.writeLong(PacketHelper.getKoreanTimestamp(System.currentTimeMillis() + 604800000L));
            mplew.writeInt(list.size());
            Iterator var3 = list.iterator();

            while(var3.hasNext()) {
               Triple<Integer, Integer, Integer> info = (Triple)var3.next();
               mplew.writeInt((Integer)info.getMid());
               mplew.writeInt((Integer)info.getRight());
               mplew.writeInt(0);
               mplew.writeInt((Integer)info.getLeft());
            }
         }

         return mplew.getPacket();
      }

      public static byte[] confirmShopTransactionItem(byte code, MapleShop shop, MapleClient c, int indexBought, int itemId, int quantity) {
         return confirmShopTransactionItem(code, shop, c, indexBought, itemId, false, false, quantity);
      }

      public static byte[] confirmShopTransactionItem(byte code, MapleShop shop, MapleClient c, int indexBought, int itemId) {
         return confirmShopTransactionItem(code, shop, c, indexBought, itemId, false, false, 999999);
      }

      public static byte[] ShopItemInfoReset(MapleShop shop, MapleClient c, int itemId, int bought, int itemposition) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SHOP_INFO_RESET.getValue());
         mplew.writeInt(shop.getId());
         mplew.writeShort(itemposition);
         mplew.writeInt(itemId);
         mplew.writeShort(bought);
         mplew.writeLong(PacketHelper.getTime(-2L));
         return mplew.getPacket();
      }

      public static byte[] confirmShopTransactionItem(byte code, MapleShop shop, MapleClient c, int indexBought, int itemId, boolean repurchase, boolean limit, int quantity) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.CONFIRM_SHOP_TRANSACTION.getValue());
         mplew.write(code);
         switch(code) {
         case 0:
            mplew.write(repurchase);
            if (repurchase) {
               mplew.writeInt(indexBought);
            } else {
               mplew.writeInt(itemId);
               mplew.writeInt(quantity);
               mplew.writeInt(0);
            }
         case 1:
         case 2:
         case 3:
         case 5:
         case 6:
         case 7:
         case 9:
         case 10:
         case 12:
         case 13:
         case 14:
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         case 20:
         case 23:
         case 25:
         case 26:
         case 28:
         case 29:
         case 31:
         case 32:
         default:
            break;
         case 4:
            mplew.writeInt(0);
            break;
         case 8:
         case 11:
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            PacketHelper.addShopInfo(mplew, shop, c);
            break;
         case 21:
         case 22:
            mplew.writeInt(0);
            break;
         case 24:
            mplew.writeInt(itemId);
            break;
         case 27:
            mplew.writeInt(0);
            break;
         case 30:
            mplew.writeInt(0);
            break;
         case 33:
            mplew.write(true);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(2);
            mplew.writeInt(0);
            PacketHelper.addShopInfo(mplew, shop, c);
         }

         return mplew.getPacket();
      }

      public static byte[] getStorage(int npcId, short slots, Collection<Item> items, long meso) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
         mplew.write((int)24);
         mplew.writeInt(npcId);
         mplew.write((int)128);
         mplew.writeLong(-1L);
         mplew.writeLong(meso);
         mplew.write(items.size());
         Iterator var6 = items.iterator();

         while(var6.hasNext()) {
            Item item = (Item)var6.next();
            PacketHelper.addItemInfo(mplew, item);
         }

         mplew.writeZeroBytes(5);
         return mplew.getPacket();
      }

      public static byte[] getStorage(byte status) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
         mplew.write((int)23);
         mplew.write(status);
         return mplew.getPacket();
      }

      public static byte[] getStorageFull() {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
         mplew.write((int)17);
         return mplew.getPacket();
      }

      public static byte[] mesoStorage(short slots, long meso) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
         mplew.write((int)19);
         mplew.write((int)slots);
         mplew.writeLong(2L);
         mplew.writeLong(meso);
         return mplew.getPacket();
      }

      public static byte[] arrangeStorage(short slots, Collection<Item> items, boolean changed) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
         mplew.write((int)15);
         mplew.write((int)slots);
         mplew.writeLong(140737488355452L);
         mplew.write(items.size());
         Iterator var4 = items.iterator();

         while(var4.hasNext()) {
            Item item = (Item)var4.next();
            PacketHelper.addItemInfo(mplew, item);
         }

         mplew.writeZeroBytes(5);
         return mplew.getPacket();
      }

      public static byte[] storeStorage(short slots, MapleInventoryType type, Collection<Item> items) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
         mplew.write((int)13);
         mplew.write((int)slots);
         mplew.writeLong(type.getBitfieldEncoding());
         mplew.write(items.size());
         Iterator var4 = items.iterator();

         while(var4.hasNext()) {
            Item item = (Item)var4.next();
            PacketHelper.addItemInfo(mplew, item);
         }

         return mplew.getPacket();
      }

      public static byte[] takeOutStorage(short slots, MapleInventoryType type, Collection<Item> items) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
         mplew.write((int)9);
         mplew.write((int)slots);
         mplew.writeLong(type.getBitfieldEncoding());
         mplew.write(items.size());
         Iterator var4 = items.iterator();

         while(var4.hasNext()) {
            Item item = (Item)var4.next();
            PacketHelper.addItemInfo(mplew, item);
         }

         return mplew.getPacket();
      }

      public static byte[] detailShowInfo1(String msg, int font, int size, long color) {
         MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
         packet.writeShort(SendPacketOpcode.DETAIL_SHOW_INFO.getValue());
         packet.writeInt(font);
         packet.writeInt(size);
         packet.writeLong(color);
         packet.write((int)0);
         packet.writeMapleAsciiString(msg);
         return packet.getPacket();
      }

      public static byte[] getShopLimit(int shopid, int position, int itemid, int buyer) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SHOP_LIMIT.getValue());
         mplew.writeInt(shopid);
         mplew.writeShort(position);
         mplew.writeInt(itemid);
         mplew.writeShort(buyer);
         mplew.writeLong(PacketHelper.getKoreanTimestamp(System.currentTimeMillis()));
         return mplew.getPacket();
      }

      public static byte[] setNpcNameInvisible(int npcid, boolean show) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.NPC_NAME_INVISIBLE.getValue());
         mplew.writeInt(npcid);
         mplew.write(show);
         return mplew.getPacket();
      }
   }

   public static class SummonPacket {
      public static byte[] spawnSummon(MapleSummon summon, boolean animated) {
         return spawnSummon(summon, animated, summon.getDuration());
      }

      public static byte[] spawnSummon(MapleSummon summon, boolean animated, int newDuration) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SPAWN_SUMMON.getValue());
         mplew.writeInt(summon.getOwner().getId());
         mplew.writeInt(summon.getObjectId());
         mplew.writeInt(summon.getSkill());
         mplew.writeInt(summon.getOwner().getLevel());
         mplew.writeInt(summon.getSkillLevel());
         mplew.writePos(summon.getPosition());
         mplew.write(summon.getSkill() != 5320011 && summon.getSkill() != 61101002 && summon.getSkill() != 101100100 && summon.getSkill() != 14000027 && summon.getSkill() != 22171081 && summon.getSkill() != 400051046 ? 4 : 5);
         int Foothold = 0;
         if (summon.getOwner().getMap().getFootholds().findBelow(summon.getPosition()) != null && summon.getSkill() != 14121003 && summon.getSkill() != 151100002 && summon.getSkill() != 400021068) {
            Foothold = (short)summon.getOwner().getMap().getFootholds().findBelow(summon.getPosition()).getId();
         }

         mplew.writeShort(Foothold);
         mplew.write(summon.getMovementType().getValue());
         mplew.write(summon.getSummonType());
         mplew.write(animated ? 1 : 0);
         mplew.writeInt(summon.getOwner().maelstrom);
         mplew.write((int)0);
         mplew.write((int)1);
         mplew.writeInt(summon.getOwner().getJob() == 1412 ? 14120008 : (summon.getSkill() == 12120007 ? 12000022 : 0));
         mplew.writeInt(0);
         mplew.write(summon.getSkill() == 400041028 || summon.getSkill() == 4341006 || summon.getMovementType() == SummonMovementType.ShadowServant || summon.getMovementType() == SummonMovementType.ShadowServantExtend && summon.getSkill() != 400011088 && summon.getSkill() != 152101000 && summon.getSkill() != 400021068);
         MapleCharacter chr = summon.getOwner();
         if (chr != null && (summon.getSkill() == 400041028 || summon.getSkill() == 4341006 || summon.getMovementType() == SummonMovementType.ShadowServant || summon.getMovementType() == SummonMovementType.ShadowServantExtend && summon.getSkill() != 400011088 && summon.getSkill() != 152101000 && summon.getSkill() != 400021068 && summon.getSkill() != 154121041)) {
            AvatarLook.encodeAvatarLook(mplew, chr, true, false);
            if (summon.getSkill() == 400041028) {
               mplew.writeInt(0);
               mplew.writeInt(0);
            } else if (summon.getSkill() == 14111024) {
               mplew.writeInt(200);
               mplew.writeInt(30);
            }
         }

         if (summon.getSkill() == 35111002) {
            ArrayList<Point> teslaz = new ArrayList();
            Iterator var7 = chr.getSummons().iterator();

            while(var7.hasNext()) {
               MapleSummon tesla = (MapleSummon)var7.next();
               if (tesla.getSkill() == 35111002) {
                  teslaz.add(new Point(tesla.getTruePosition()));
               }
            }

            if (teslaz.size() != 3) {
               mplew.write(false);
            } else {
               mplew.write(true);
               var7 = teslaz.iterator();

               while(var7.hasNext()) {
                  Point pos = (Point)var7.next();
                  mplew.writePos(pos);
               }
            }
         }

         if (summon.getSkill() == 151111001) {
            mplew.writeInt(-1);
         }

         if (isSpecial(summon.getSkill())) {
            if (summon.getSkill() == 131001017) {
               mplew.writeInt(400);
               mplew.writeInt(30);
            } else if (summon.getSkill() == 131002017) {
               mplew.writeInt(800);
               mplew.writeInt(60);
            } else if (summon.getSkill() == 131003017) {
               mplew.writeInt(1200);
               mplew.writeInt(90);
            } else {
               mplew.writeInt((summon.getSkill() - GameConstants.getLinkedSkill(summon.getSkill()) + 1) * 400);
               mplew.writeInt((summon.getSkill() - GameConstants.getLinkedSkill(summon.getSkill()) + 1) * 30);
            }
         }

         boolean special = summon.getOwner().getBuffedValue(400031005);
         if (GameConstants.isWildHunter(chr.getJob()) && summon.getSkill() == GameConstants.getSelectJaguarSkillId(GameConstants.getMountItem(33001001, chr))) {
            special = false;
         }

         mplew.write(special);
         mplew.writeInt(summon.getSkill() == 35111002 ? 90000 : (summon.getSkill() == 152101000 ? 0 : newDuration));
         mplew.write((int)1);
         mplew.writeInt(summon.getSummonRLType());
         mplew.writeInt(0);
         if (summon.getSkill() - 33001007 >= 0 && summon.getSkill() - 33001007 <= 8 && summon.getSkill() != 154121041) {
            mplew.write(special);
            mplew.writeInt(newDuration);
         }

         mplew.writeInt(summon.getSkill() == 162101012 ? 400 : 0);
         mplew.write(summon.isControlCrystal() || summon.getSkill() == 400021068 || summon.getSkill() == 400051046);
         if (summon.isControlCrystal() || summon.getSkill() == 400021068 || summon.getSkill() == 400051046) {
            mplew.writeInt(summon.getEnergy());
            if (summon.getEnergy() >= 150) {
               mplew.writeInt(4);
            } else if (summon.getEnergy() >= 90) {
               mplew.writeInt(3);
            } else if (summon.getEnergy() >= 60) {
               mplew.writeInt(2);
            } else if (summon.getEnergy() >= 30) {
               mplew.writeInt(1);
            } else {
               mplew.writeInt(0);
            }
         }

         mplew.writeInt(0);
         if (summon.getSkill() - 33001007 >= 0 && summon.getSkill() - 33001007 <= 8) {
            mplew.write((int)0);
         } else {
            mplew.writeInt(0);
         }

         mplew.writeInt(-1);
         mplew.write((int)0);
         mplew.write((int)0);
         if (summon.getSkill() == 400051011) {
            if (chr.getEnergyBurst() == 50) {
               mplew.writeInt(0);
            } else if (chr.getEnergyBurst() == 25) {
               mplew.writeInt(20);
            } else if (chr.getEnergyBurst() > 0) {
               mplew.writeInt(30000);
            } else {
               mplew.writeInt(100000);
            }
         }

         return mplew.getPacket();
      }

      private static boolean isSpecial(int a1) {
         boolean v2;
         if (a1 > 131003017) {
            if (a1 == 400031007) {
               return true;
            } else {
               v2 = a1 == 400041028;
               if (v2) {
                  return true;
               } else {
                  return a1 - 400031007 >= -2 && a1 - 400031007 <= 2;
               }
            }
         } else if (a1 == 131003017) {
            return true;
         } else if (a1 > 131001017) {
            v2 = a1 == 131002017;
            if (v2) {
               return true;
            } else {
               return a1 - 400031007 >= -2 && a1 - 400031007 <= 2;
            }
         } else if (a1 == 131001017 || a1 == 14111024 || a1 > 14121053 && a1 <= 14121056) {
            return true;
         } else {
            return a1 - 400031007 >= -2 && a1 - 400031007 <= 2;
         }
      }

      public static byte[] removeSummon(MapleSummon summon, boolean animated) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.REMOVE_SUMMON.getValue());
         mplew.writeInt(summon.getOwner().getId());
         mplew.writeInt(summon.getObjectId());
         if (animated) {
            switch(summon.getSkill()) {
            case 5321052:
            case 14121003:
            case 36121002:
            case 36121013:
            case 36121014:
            case 101100100:
            case 101100101:
            case 400051017:
               mplew.write((int)0);
               return mplew.getPacket();
            case 14000027:
            case 14111024:
            case 14121054:
            case 35121003:
               mplew.write((int)10);
               return mplew.getPacket();
            case 33101008:
            case 35111001:
            case 35111002:
            case 35111005:
            case 35111009:
            case 35111010:
            case 35111011:
            case 35121009:
            case 35121010:
            case 35121011:
               mplew.write((int)5);
               return mplew.getPacket();
            default:
               mplew.write((int)4);
            }
         } else if (summon.getSkill() != 14000027 && summon.getSkill() != 14100027 && summon.getSkill() != 14110029 && summon.getSkill() != 14120008) {
            mplew.write(summon.getSkill() == 35121003 ? 10 : 1);
         } else {
            mplew.write((int)16);
         }

         return mplew.getPacket();
      }

      public static byte[] moveSummon(int cid, int oid, Point startPos, List<LifeMovementFragment> moves) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.MOVE_SUMMON.getValue());
         mplew.writeInt(cid);
         mplew.writeInt(oid);
         mplew.writeInt(0);
         mplew.writePos(startPos);
         mplew.writeInt(0);
         PacketHelper.serializeMovementList(mplew, moves);
         return mplew.getPacket();
      }

      public static byte[] summonAttack(MapleSummon summon, int skillid, byte animation, byte tbyte, List<Pair<Integer, List<Long>>> allDamage, int level, Point pos, boolean darkFlare) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SUMMON_ATTACK.getValue());
         mplew.writeInt(summon.getOwner().getId());
         mplew.writeInt(summon.getObjectId());
         mplew.writeInt(summon.getOwner().getLevel());
         mplew.write(animation);
         mplew.write(tbyte);
         Iterator var9 = allDamage.iterator();

         label39:
         while(true) {
            Pair attackEntry;
            do {
               if (!var9.hasNext()) {
                  mplew.write(darkFlare ? 1 : 0);
                  mplew.write(summon.isNoapply());
                  mplew.writePos(pos);
                  mplew.writeInt(skillid);
                  mplew.write(false);
                  mplew.writePos(new Point(0, 0));
                  mplew.writeZeroBytes(10);
                  return mplew.getPacket();
               }

               attackEntry = (Pair)var9.next();
               mplew.writeInt((Integer)attackEntry.left);
            } while((Integer)attackEntry.left <= 0);

            mplew.write((int)7);
            Iterator var11 = ((List)attackEntry.right).iterator();

            while(true) {
               while(true) {
                  if (!var11.hasNext()) {
                     continue label39;
                  }

                  Long damage = (Long)var11.next();
                  if (summon.getOwner().getStat().getCritical_rate() < 100 && summon.getOwner().getSkillCustomValue(3310005) == null && !Randomizer.isSuccess(summon.getOwner().getStat().getCritical_rate())) {
                     mplew.writeLong(damage);
                  } else {
                     mplew.writeLong(damage | -9223372036854775807L);
                  }
               }
            }
         }
      }

      public static byte[] updateSummon(MapleSummon summon, int type) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.UPDATE_SUMMON.getValue());
         mplew.writeInt(summon.getOwner().getId());
         mplew.writeInt(summon.getObjectId());
         mplew.write(type == 99 ? 0 : type);
         mplew.writeInt(summon.getSkill());
         mplew.writeInt(type == 99 ? 1 : 0);
         return mplew.getPacket();
      }

      public static byte[] getSummonSkillAttackEffect(MapleSummon summon, int type, int skillid, int level, int unk1, int unk2, int bullet, Point pos1, Point pos2, Point pos3, int unk3, int unk4, int unk5, List<MatrixSkill> skills) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.UPDATE_SUMMON.getValue());
         mplew.writeInt(summon.getOwner().getId());
         mplew.writeInt(summon.getObjectId());
         mplew.write(type);
         mplew.writeInt(skillid);
         mplew.writeInt(skillid);
         mplew.writeInt(level);
         mplew.writeInt(unk1);
         mplew.writeInt(unk2);
         mplew.writeInt(bullet);
         mplew.writePos(pos1);
         mplew.writePosInt(pos2);
         mplew.writePosInt(pos3);
         mplew.write((int)0);
         mplew.writeInt(unk3);
         mplew.writeInt(unk4);
         mplew.write(unk5);
         mplew.writeInt(skills.size());
         Iterator var15 = skills.iterator();

         while(var15.hasNext()) {
            MatrixSkill skill = (MatrixSkill)var15.next();
            mplew.writeInt(skill.getSkill());
            mplew.writeInt(skill.getLevel());
            mplew.writeInt(skill.getUnk1());
            mplew.writeShort(skill.getUnk2());
            mplew.writePos(skill.getAngle());
            mplew.writeInt(skill.getUnk3());
            mplew.write(skill.getUnk4());
            mplew.write(skill.getUnk5());
            if (skill.getUnk5() > 0) {
               mplew.writeInt(skill.getX());
               mplew.writeInt(skill.getY());
            }

            mplew.write(skill.getUnk6());
            if (skill.getUnk6() > 0) {
               mplew.writeInt(skill.getX2());
               mplew.writeInt(skill.getY2());
            }
         }

         return mplew.getPacket();
      }

      public static byte[] summonSkill(int cid, int summonskillid, int newStance) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SUMMON_SKILL.getValue());
         mplew.writeInt(cid);
         mplew.writeInt(summonskillid);
         mplew.write(newStance);
         return mplew.getPacket();
      }

      public static byte[] JaguarAutoAttack(boolean on) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.JAGUAR_AUTO_ATTACK.getValue());
         mplew.write(on);
         return mplew.getPacket();
      }

      public static byte[] summonDebuff(int cid, int oid) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SUMMON_DEBUFF.getValue());
         mplew.writeInt(cid);
         mplew.writeInt(oid);
         mplew.write((int)0);
         return mplew.getPacket();
      }

      public static byte[] damageSummon(int cid, int summonskillid, int damage, int unkByte, int monsterIdFrom) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.DAMAGE_SUMMON.getValue());
         mplew.writeInt(cid);
         mplew.writeInt(summonskillid);
         mplew.writeInt(unkByte);
         mplew.writeInt(damage);
         mplew.writeInt(monsterIdFrom);
         mplew.writeInt(0);
         return mplew.getPacket();
      }

      public static byte[] damageSummon(MapleSummon summon) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.DAMAGE_SUMMON_2.getValue());
         mplew.writeInt(summon.getOwner().getId());
         mplew.writeInt(summon.getObjectId());
         mplew.writeInt(8);
         return mplew.getPacket();
      }

      public static byte[] BeholderRevengeAttack(MapleCharacter chr, short damage, int oid) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.BEHOLDER_REVENGE.getValue());
         mplew.writeInt(chr.getId());
         mplew.writeInt(damage);
         mplew.writeInt(oid);
         return mplew.getPacket();
      }

      public static byte[] transformSummon(MapleSummon summon, int type) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.TRANSFORM_SUMMON.getValue());
         mplew.writeInt(summon.getOwner().getId());
         mplew.writeInt(summon.getObjectId());
         mplew.writeInt(type);
         if (type == 2) {
            mplew.writeInt(summon.getCrystalSkills().size());

            for(int i = 1; i <= summon.getCrystalSkills().size(); ++i) {
               mplew.writeInt(i);
               mplew.writeInt((Boolean)summon.getCrystalSkills().get(i - 1) ? 1 : 0);
            }
         }

         return mplew.getPacket();
      }

      public static byte[] DeathAttack(MapleSummon summon) {
         return DeathAttack(summon, 0);
      }

      public static byte[] DeathAttack(MapleSummon summon, int skillvalue) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.DEATH_ATTACK.getValue());
         mplew.writeInt(summon.getOwner().getId());
         mplew.writeInt(summon.getObjectId());
         mplew.writeInt(skillvalue);
         return mplew.getPacket();
      }

      public static byte[] ElementalRadiance(MapleSummon summon, int type) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.ELEMENTAL_RADIANCE.getValue());
         mplew.writeInt(summon.getOwner().getId());
         mplew.writeInt(summon.getObjectId());
         mplew.writeInt(type);
         switch(type) {
         case 2:
            mplew.writeInt(summon.getEnergy());
            if (summon.getEnergy() >= 150) {
               mplew.writeInt(4);
            } else if (summon.getEnergy() >= 90) {
               mplew.writeInt(3);
            } else if (summon.getEnergy() >= 60) {
               mplew.writeInt(2);
            } else if (summon.getEnergy() >= 30) {
               mplew.writeInt(1);
            } else {
               mplew.writeInt(0);
            }
            break;
         case 5:
            mplew.writeInt(summon.getOwner().getBuffedEffect(400021061) != null ? 400021062 : 152110002);
            mplew.writeInt(summon.getOwner().getBuffedEffect(400021061) != null ? 1000 : 4000);
         }

         return mplew.getPacket();
      }

      public static byte[] specialSummon(MapleSummon summon, int type) {
         return specialSummon(summon, type, 0);
      }

      public static byte[] specialSummon(MapleSummon summon, int type, int skillid) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SPECIAL_SUMMON.getValue());
         mplew.writeInt(summon.getOwner().getId());
         mplew.writeInt(summon.getObjectId());
         mplew.writeInt(type);
         switch(type) {
         case 2:
            mplew.writeInt(summon.getEnergy());
            mplew.writeInt(summon.getEnergy());
            break;
         case 3:
            mplew.writeInt(0);
            break;
         case 4:
            mplew.writeInt(skillid);
         }

         return mplew.getPacket();
      }

      public static byte[] specialSummon2(MapleSummon summon, int skill) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SPECIAL_SUMMON2.getValue());
         mplew.writeInt(summon.getOwner().getId());
         mplew.writeInt(summon.getObjectId());
         mplew.writeInt(skill);
         return mplew.getPacket();
      }

      public static byte[] AbsorbentEdificeps(int cid, int oid, int combo, int stack) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SPECIAL_SUMMON.getValue());
         mplew.writeInt(cid);
         mplew.writeInt(oid);
         mplew.writeInt(2);
         mplew.writeInt(combo);
         mplew.writeInt(stack);
         return mplew.getPacket();
      }

      public static byte[] summonRangeAttack(MapleSummon summon, int skill) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SUMMON_RANGE_ATTACK.getValue());
         mplew.writeInt(summon.getOwner().getId());
         mplew.writeInt(summon.getObjectId());
         mplew.writeInt(skill);
         mplew.write(skill != 400041050 && skill != 400041051 ? 0 : 1);
         return mplew.getPacket();
      }
   }

   public static class AttackObjPacket {
      public static byte[] ObjCreatePacket(MapleFieldAttackObj fao) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SPAWN_FIELDATTACK_OBJ.getValue());
         mplew.writeInt(fao.getObjectId());
         mplew.writeInt(1);
         mplew.writeInt(fao.getChr().getId());
         mplew.writeInt(0);
         mplew.write(false);
         mplew.writeInt(fao.getTruePosition().x);
         mplew.writeInt(fao.getTruePosition().y);
         mplew.write(fao.isFacingleft());
         return mplew.getPacket();
      }

      public static byte[] ObjRemovePacketByOid(int objectid) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.REMOVE_FIELDATTACK_OBJ_KEY.getValue());
         mplew.writeInt(objectid);
         return mplew.getPacket();
      }

      public static byte[] ObjRemovePacketByList(List<MapleFieldAttackObj> removes) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.REMOVE_FIELDATTACK_OBJ_LIST.getValue());
         mplew.writeInt(removes.size());
         Iterator var2 = removes.iterator();

         while(var2.hasNext()) {
            MapleMapObject obj = (MapleMapObject)var2.next();
            mplew.writeInt(obj.getObjectId());
         }

         return mplew.getPacket();
      }

      public static byte[] OnSetAttack(MapleFieldAttackObj fao) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.FIELDATTACK_OBJ_ATTACK.getValue());
         mplew.writeInt(fao.getObjectId());
         mplew.writeInt(0);
         return mplew.getPacket();
      }
   }

   public static class UIPacket {
      public static byte[] greenShowInfo(String msg) {
         MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
         packet.writeShort(SendPacketOpcode.GREEN_SHOW_INFO.getValue());
         packet.write((int)1);
         packet.writeMapleAsciiString(msg);
         packet.write((int)1);
         return packet.getPacket();
      }

      public static byte[] detailShowInfo1(String msg, int font, int size, long color) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.DETAIL_SHOW_INFO.getValue());
         mplew.writeInt(font);
         mplew.writeInt(size);
         mplew.writeInt(color);
         mplew.writeInt(0);
         mplew.write((int)0);
         mplew.writeMapleAsciiString(msg);
         return mplew.getPacket();
      }

      public static byte[] detailShowInfo(String msg, int font, int size, int color) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.DETAIL_SHOW_INFO.getValue());
         mplew.writeInt(font);
         mplew.writeInt(size);
         mplew.writeInt(color);
         mplew.writeInt(0);
         mplew.write((int)0);
         mplew.writeMapleAsciiString(msg);
         return mplew.getPacket();
      }

      public static byte[] getRainBowRushSetting() {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.RAINBOW_RUSH.getValue());
         mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 48 E8 01 00 B8 0B 00 00 01 00 00 00 05 00 00 00 2C 01 00 00 10 27 00 00 10 27 00 00 00 00 00 00 9C FF FF FF 40 1F 00 00 88 13 00 00 01 00 00 00 16 00 00 00 C0 D4 01 00 01 00 00 00 00 00 00 00 00 6A E8 40 B0 AD 01 00 01 00 00 00 00 00 00 00 00 60 6D 40 28 9A 01 00 01 00 00 00 00 00 00 00 00 60 6D 40 A0 86 01 00 01 00 00 00 00 00 00 00 00 00 6E 40 18 73 01 00 01 00 00 00 00 00 00 00 00 40 70 40 90 5F 01 00 01 00 00 00 00 00 00 00 00 E0 70 40 08 4C 01 00 01 00 00 00 00 00 00 00 00 80 71 40 80 38 01 00 01 00 00 00 00 00 00 00 00 D0 71 40 F8 24 01 00 01 00 00 00 00 00 00 00 00 80 71 40 70 11 01 00 01 00 00 00 00 00 00 00 00 20 72 40 E8 FD 00 00 01 00 00 00 00 00 00 00 00 C0 72 40 D8 D6 00 00 01 00 00 00 00 00 00 00 00 00 74 40 C8 AF 00 00 01 00 00 00 00 00 00 00 00 E0 75 40 40 9C 00 00 01 00 00 00 00 00 00 00 00 00 79 40 B8 88 00 00 01 00 00 00 00 00 00 00 00 20 7C 40 30 75 00 00 01 00 00 00 00 00 00 00 00 40 7F 40 A8 61 00 00 01 00 00 00 00 00 00 00 00 C0 82 40 20 4E 00 00 01 00 00 00 00 00 00 00 00 E0 85 40 98 3A 00 00 01 00 00 00 00 00 00 00 00 00 89 40 10 27 00 00 01 00 00 00 00 00 00 00 00 40 8F 40 88 13 00 00 01 00 00 00 00 00 00 00 00 70 97 40 00 00 00 00 01 00 00 00 00 00 00 00 00 40 9F 40 01 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 F0 3F 15 00 00 00 28 9A 01 00 01 00 00 00 00 00 00 00 00 40 5A 40 A0 86 01 00 01 00 00 00 00 00 00 00 00 00 59 40 18 73 01 00 01 00 00 00 00 00 00 00 00 80 56 40 90 5F 01 00 01 00 00 00 00 00 00 00 00 80 56 40 08 4C 01 00 01 00 00 00 00 00 00 00 00 40 55 40 80 38 01 00 01 00 00 00 00 00 00 00 00 00 54 40 F8 24 01 00 01 00 00 00 00 00 00 00 00 C0 52 40 70 11 01 00 01 00 00 00 00 00 00 00 00 80 51 40 E8 FD 00 00 01 00 00 00 00 00 00 00 00 40 51 40 60 EA 00 00 01 00 00 00 00 00 00 00 00 C0 50 40 D8 D6 00 00 01 00 00 00 00 00 00 00 00 40 50 40 C8 AF 00 00 01 00 00 00 00 00 00 00 00 80 4F 40 40 9C 00 00 01 00 00 00 00 00 00 00 00 00 4E 40 B8 88 00 00 01 00 00 00 00 00 00 00 00 00 49 40 30 75 00 00 01 00 00 00 00 00 00 00 00 80 46 40 A8 61 00 00 01 00 00 00 00 00 00 00 00 80 41 40 20 4E 00 00 01 00 00 00 00 00 00 00 00 00 3E 40 98 3A 00 00 01 00 00 00 00 00 00 00 00 00 39 40 10 27 00 00 01 00 00 00 00 00 00 00 00 00 34 40 88 13 00 00 01 00 00 00 00 00 00 00 00 00 2E 40 00 00 00 00 01 00 00 00 00 00 00 00 00 00 24 40 0B 00 00 00 C0 D4 01 00 01 00 00 00 00 00 00 00 00 00 00 00 B0 AD 01 00 01 00 00 00 00 00 00 00 00 60 6D 40 A0 86 01 00 01 00 00 00 00 00 00 00 00 00 69 40 90 5F 01 00 01 00 00 00 00 00 00 00 00 C0 62 40 80 38 01 00 01 00 00 00 00 00 00 00 00 80 56 40 70 11 01 00 01 00 00 00 00 00 00 00 00 40 55 40 60 EA 00 00 01 00 00 00 00 00 00 00 00 00 54 40 50 C3 00 00 01 00 00 00 00 00 00 00 00 00 4E 40 40 9C 00 00 01 00 00 00 00 00 00 00 00 00 44 40 20 4E 00 00 01 00 00 00 00 00 00 00 00 00 34 40 00 00 00 00 01 00 00 00 00 00 00 00 00 00 24 40 15 00 00 00 28 9A 01 00 01 00 00 00 00 00 00 00 00 C0 72 40 A0 86 01 00 01 00 00 00 00 00 00 00 00 60 73 40 18 73 01 00 01 00 00 00 00 00 00 00 00 00 74 40 90 5F 01 00 01 00 00 00 00 00 00 00 00 A0 74 40 08 4C 01 00 01 00 00 00 00 00 00 00 00 40 75 40 80 38 01 00 01 00 00 00 00 00 00 00 00 E0 75 40 F8 24 01 00 01 00 00 00 00 00 00 00 00 80 76 40 70 11 01 00 01 00 00 00 00 00 00 00 00 20 77 40 E8 FD 00 00 01 00 00 00 00 00 00 00 00 60 78 40 60 EA 00 00 01 00 00 00 00 00 00 00 00 A0 79 40 D8 D6 00 00 01 00 00 00 00 00 00 00 00 E0 7A 40 C8 AF 00 00 01 00 00 00 00 00 00 00 00 20 7C 40 40 9C 00 00 01 00 00 00 00 00 00 00 00 60 7D 40 B8 88 00 00 01 00 00 00 00 00 00 00 00 A0 7E 40 30 75 00 00 01 00 00 00 00 00 00 00 00 E0 7F 40 A8 61 00 00 01 00 00 00 00 00 00 00 00 90 80 40 20 4E 00 00 01 00 00 00 00 00 00 00 00 30 81 40 98 3A 00 00 01 00 00 00 00 00 00 00 00 C0 82 40 10 27 00 00 01 00 00 00 00 00 00 00 00 50 84 40 88 13 00 00 01 00 00 00 00 00 00 00 00 E0 85 40 00 00 00 00 01 00 00 00 00 00 00 00 00 00 89 40 03 00 00 00 50 C3 00 00 01 00 00 00 00 00 00 00 00 00 49 40 40 9C 00 00 01 00 00 00 00 00 00 00 00 00 59 40 00 00 00 00 01 00 00 00 00 00 00 00 00 C0 72 40 02 00 00 00 B0 AD 01 00 01 00 00 00 00 00 00 00 00 00 F0 3F 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 03 00 00 00 90 5F 01 00 01 00 00 00 00 00 00 00 00 70 A7 40 60 EA 00 00 01 00 00 00 00 00 00 00 00 40 8F 40 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 E8 03 00 00 09 00 00 00 C0 D4 01 00 01 00 00 00 00 00 00 00 00 00 24 40 B0 AD 01 00 01 00 00 00 00 00 00 00 00 00 34 40 50 C3 00 00 01 00 00 00 00 00 00 00 00 00 24 40 C8 AF 00 00 01 00 00 00 00 00 00 00 00 00 1C 40 40 9C 00 00 01 00 00 00 00 00 00 00 00 00 18 40 B8 88 00 00 01 00 00 00 00 00 00 00 00 00 10 40 30 75 00 00 01 00 00 00 00 00 00 00 00 00 08 40 10 27 00 00 01 00 00 00 00 00 00 00 00 00 00 40 00 00 00 00 01 00 00 00 00 00 00 00 00 00 F0 3F 00 00 00 00 02 00 00 00 01 00 00 00 01 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 19 00 00 00"));
         return mplew.getPacket();
      }

      public static byte[] getRainBowRushStart() {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.RAINBOW_RUSH.getValue() + 1);
         mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 01 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"));
         return mplew.getPacket();
      }

      public static byte[] getRainBowResult(int jam, int time) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.RAINBOW_RUSH.getValue() + 6);
         mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 02 00 00 00 00 00 00 00 00 00 00 00"));
         mplew.writeInt(jam * 100);
         mplew.writeInt(jam * 100);
         mplew.writeInt(time);
         return mplew.getPacket();
      }

      public static byte[] getDirectionStatus(boolean enable) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.DIRECTION_STATUS.getValue());
         mplew.write(enable ? 1 : 0);
         return mplew.getPacket();
      }

      public static byte[] openUI(int type) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
         mplew.writeShort(SendPacketOpcode.OPEN_UI.getValue());
         mplew.writeInt(type);
         return mplew.getPacket();
      }

      public static byte[] closeUI(int type) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.CLOSE_UI.getValue());
         mplew.writeInt(type);
         return mplew.getPacket();
      }

      public static byte[] openUIOption(int type, int option) {
         return openUIOption(type, option, 0);
      }

      public static byte[] openUIOption(int type, int option, int option2) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(10);
         mplew.writeShort(SendPacketOpcode.OPEN_UI_OPTION.getValue());
         mplew.writeInt(type);
         mplew.writeInt(option);
         mplew.writeInt(option2);
         return mplew.getPacket();
      }

      public static byte[] IntroLock(boolean enable) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.CYGNUS_INTRO_LOCK.getValue());
         mplew.write(enable ? 1 : 0);
         mplew.writeInt(0);
         return mplew.getPacket();
      }

      public static byte[] IntroEnableUI(int wtf) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.CYGNUS_INTRO_ENABLE_UI.getValue());
         mplew.write(wtf > 0 ? 1 : 0);
         mplew.write((int)0);
         if (wtf > 0) {
            mplew.write(false);
            mplew.write(false);
         }

         return mplew.getPacket();
      }

      public static byte[] IntroDisableUI(boolean enable) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.CYGNUS_INTRO_DISABLE_UI.getValue());
         mplew.write(enable ? 1 : 0);
         return mplew.getPacket();
      }

      public static byte[] summonHelper(boolean summon) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SUMMON_HINT.getValue());
         mplew.write(summon ? 1 : 0);
         return mplew.getPacket();
      }

      public static byte[] summonMessage(int type) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SUMMON_HINT_MSG.getValue());
         mplew.write((int)1);
         mplew.writeInt(type);
         mplew.writeInt(7000);
         return mplew.getPacket();
      }

      public static byte[] summonMessage(String message) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SUMMON_HINT_MSG.getValue());
         mplew.write((int)0);
         mplew.writeMapleAsciiString(message);
         mplew.writeInt(200);
         mplew.writeShort(0);
         mplew.writeInt(10000);
         return mplew.getPacket();
      }

      public static byte[] getDirectionInfo(int type, int value) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.UserInGameDirectionEvent.getValue());
         mplew.write(type);
         mplew.writeLong((long)value);
         mplew.writeZeroBytes(10);
         return mplew.getPacket();
      }

      public static byte[] getDirectionInfo(String data, int value, int x, int y, int a, int b) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.UserInGameDirectionEvent.getValue());
         mplew.write((int)2);
         mplew.writeMapleAsciiString(data);
         mplew.writeInt(value);
         mplew.writeInt(x);
         mplew.writeInt(y);
         mplew.write(a);
         if (a > 0) {
            mplew.writeInt(0);
         }

         mplew.write(b);
         if (b > 1) {
            mplew.writeInt(0);
            mplew.write(a);
            mplew.write(b);
         }

         mplew.writeZeroBytes(10);
         return mplew.getPacket();
      }

      public static final byte[] playMovie(String data, boolean show) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.PLAY_MOVIE.getValue());
         mplew.writeMapleAsciiString(data);
         mplew.write(show ? 1 : 0);
         return mplew.getPacket();
      }

      public static byte[] detailShowInfo(String msg, boolean RuneSystem) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.DETAIL_SHOW_INFO.getValue());
         mplew.writeInt(3);
         mplew.writeInt(RuneSystem ? 17 : 20);
         mplew.writeInt(RuneSystem ? 0 : 4);
         mplew.writeInt(0);
         mplew.write(false);
         mplew.writeMapleAsciiString(msg);
         return mplew.getPacket();
      }

      public static byte[] OnSetMirrorDungeonInfo(boolean clear) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.MIRROR_DUNGEON_INFO.getValue());
         mplew.writeInt(clear ? 0 : GameConstants.dList.size());
         Iterator var2 = GameConstants.dList.iterator();

         while(var2.hasNext()) {
            Pair<String, String> d = (Pair)var2.next();
            mplew.writeMapleAsciiString((String)d.left);
            mplew.writeInt(0);
            mplew.writeMapleAsciiString((String)d.right);
         }

         return mplew.getPacket();
      }
   }

   public static class EffectPacket {
      public static byte[] showOrgelEffect(MapleCharacter chr, int skillid, Point position) {
         return showEffect(chr, 0, skillid, 80, 0, 0, (byte)0, true, position, (String)null, (Item)null);
      }

      public static byte[] showSummonEffect(MapleCharacter chr, int skillid, boolean own) {
         return showEffect(chr, 0, skillid, 4, 0, 0, (byte)0, own, (Point)null, (String)null, (Item)null);
      }

      public static byte[] showPortalEffect(int skillid) {
         return showEffect((MapleCharacter)null, 0, 0, 7, 0, 0, (byte)0, true, (Point)null, (String)null, (Item)null);
      }

      public static byte[] showDiceEffect(MapleCharacter chr, int oldskillid, int skillid, int subeffectid, int subeffectid2, boolean own) {
         return showEffect(chr, oldskillid, skillid, 6, subeffectid, subeffectid2, (byte)(chr.isFacingLeft() ? 1 : 0), own, chr.getTruePosition(), "", (Item)null);
      }

      public static byte[] showCharmEffect(MapleCharacter chr, int skillid, int subeffectid2, boolean own, String txt) {
         return showEffect(chr, 0, skillid, 8, 0, subeffectid2, (byte)0, own, (Point)null, txt, (Item)null);
      }

      public static byte[] showPetLevelUpEffect(MapleCharacter chr, int skillid, boolean own) {
         return showEffect(chr, 0, skillid, 9, 0, 0, (byte)0, own, (Point)null, (String)null, (Item)null);
      }

      public static byte[] showRewardItemEffect(MapleCharacter chr, int skillid, boolean own, String txt) {
         return showEffect(chr, 0, skillid, 20, 0, 0, (byte)0, own, (Point)null, txt, (Item)null);
      }

      public static byte[] showItemMakerEffect(MapleCharacter chr, int direction, boolean own) {
         return showEffect(chr, 0, 0, 22, 0, 0, (byte)direction, own, (Point)null, (String)null, (Item)null);
      }

      public static byte[] showWheelEffect(int skillid) {
         return showEffect((MapleCharacter)null, 0, 0, 27, 0, 0, (byte)0, true, (Point)null, (String)null, (Item)null);
      }

      public static byte[] showWZEffect(String txt) {
         return showEffect((MapleCharacter)null, 0, 0, 28, 0, 0, (byte)0, true, (Point)null, txt, (Item)null);
      }

      public static byte[] showWZEffect2(String txt) {
         return showEffect((MapleCharacter)null, 0, 0, 34, 0, 0, (byte)0, true, (Point)null, txt, (Item)null);
      }

      public static byte[] showEffect(MapleCharacter chr, String txt) {
         return showEffect(chr, 0, 0, 29, 0, 0, (byte)0, true, (Point)null, txt, (Item)null);
      }

      public static byte[] showHealEffect(MapleCharacter chr, int skillid, boolean own) {
         return showEffect(chr, 0, skillid, 37, 0, 0, (byte)0, own, (Point)null, (String)null, (Item)null);
      }

      public static byte[] showBoxEffect(MapleCharacter chr, int oldskillid, int skillid, boolean own) {
         return showEffect(chr, oldskillid, skillid, 53, 0, 0, (byte)0, own, (Point)null, (String)null, (Item)null);
      }

      public static byte[] showBurningFieldEffect(String txt) {
         return showEffect((MapleCharacter)null, 0, 0, 61, 0, 0, (byte)0, true, (Point)null, txt, (Item)null);
      }

      public static byte[] showNormalEffect(MapleCharacter chr, int effectid, boolean own) {
         return showEffect(chr, 0, 0, effectid, 0, 0, (byte)0, own, (Point)null, (String)null, (Item)null);
      }

      public static byte[] showWillEffect(MapleCharacter chr, int subeffectid, int skillid, int skillLevel) {
         return showEffect(chr, skillLevel, skillid, 73, subeffectid, 0, (byte)0, true, (Point)null, (String)null, (Item)null);
      }

      public static byte[] showFieldSkillEffect(int skillid, int skillLevel, int type) {
         return showEffect((MapleCharacter)null, skillLevel, skillid, 74, type, 0, (byte)0, true, (Point)null, (String)null, (Item)null);
      }

      public static byte[] showFieldSkillEffect(MapleCharacter chr, int skillid, int skillLevel) {
         return showEffect(chr, skillLevel, skillid, 46, 0, 0, (byte)0, true, (Point)null, (String)null, (Item)null);
      }

      public static byte[] showFieldSkillEffect(MapleCharacter chr, int skillid, byte skillLevel) {
         return showEffect(chr, skillLevel, skillid, 36, 0, 0, (byte)0, true, (Point)null, (String)null, (Item)null);
      }

      public static byte[] showEffect(MapleCharacter chr, int oldskillid, int skillid, int effectid, int subeffectid, int subeffectid2, byte direction, boolean own, Point pos, String txt, Item item) {
         return showEffect(chr, oldskillid, skillid, effectid, subeffectid, subeffectid2, direction, own, pos, txt, item, (AttackInfo)null);
      }

      public static byte[] showEffect(MapleCharacter chr, int oldskillid, int skillid, int effectid, int subeffectid, int subeffectid2, byte direction, boolean own, Point pos, String txt, Item item, AttackInfo at) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         if (own) {
            mplew.writeShort(SendPacketOpcode.SHOW_EFFECT.getValue());
         } else {
            mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
            mplew.writeInt(chr.getId());
         }

         mplew.write(effectid);
         boolean a;
         label316:
         switch(effectid) {
         case 1:
         case 2:
            if (effectid == 2) {
               mplew.writeInt(subeffectid2);
            }

            mplew.writeInt(skillid);
            mplew.writeInt(chr.getLevel());
            mplew.writeInt(chr.getTotalSkillLevel(skillid) == 0 ? 1 : chr.getTotalSkillLevel(skillid));
            if (skillid == 22170074) {
               mplew.write((int)0);
            }

            if (skillid == 1320016) {
               mplew.write(chr.getReinCarnation());
            }

            if (skillid == 4331006) {
               mplew.write((int)0);
               mplew.writeInt(0);
            }

            if (skillid == 400020010) {
               mplew.write((int)0);
               mplew.writeInt(0);
               mplew.writeInt(0);
               mplew.writeInt(0);
            }

            if (skillid == 3211010 || skillid == 3111010 || skillid == 1100012) {
               mplew.write(direction);
               mplew.writeInt(subeffectid2);
               mplew.writeInt(subeffectid2 > 0 ? pos.x : 0);
               mplew.writeInt(subeffectid2 > 0 ? pos.y : 0);
            }

            if (skillid == 64001000 || skillid > 64001006 && skillid <= 64001008) {
               mplew.write(direction);
            }

            if (skillid - 64001009 >= -2 && skillid - 64001009 <= 2) {
               mplew.write(direction);
               mplew.writeInt(chr.getFH());
               mplew.writeInt(pos.x);
               mplew.writeInt(pos.y);
            }

            if (skillid == 64001012) {
               mplew.write(direction);
               mplew.writeInt(pos.x);
               mplew.writeInt(pos.y);
               mplew.writeInt(oldskillid);
            }

            if (skillid == 30001062) {
               mplew.write((int)0);
               mplew.writeShort(pos.x);
               mplew.writeShort(pos.y);
            }

            if (skillid == 30001061) {
               mplew.write(direction);
            }

            if (skillid == 60001218 || skillid == 60011218 || skillid == 400001000) {
               mplew.writeInt(oldskillid);
               mplew.writeInt(pos.x);
               mplew.writeInt(pos.y);
               mplew.write(true);
            }

            if (skillid == 131003016) {
               mplew.write((int)0);
               mplew.writeInt(pos.x);
               mplew.writeInt(pos.y);
            }

            if (skillid == 400051025) {
               mplew.writeInt(pos.x);
               mplew.writeInt(pos.y);
            }

            if (skillid == 20041222 || skillid == 15001021 || skillid == 20051284 || skillid == 4211016 || skillid == 400041026 || skillid == 152001004) {
               mplew.writeInt(oldskillid);
               mplew.writeInt(pos.x);
               mplew.writeInt(pos.y);
               mplew.writeInt(subeffectid);
            }

            if (skillid == 4221052 || skillid == 65121052) {
               mplew.writeInt(0);
               mplew.writeInt(0);
            }

            if (GameConstants.sub_7F9870(skillid) > 0) {
               mplew.writeInt(0);
            }

            if (skillid == 400041019) {
               mplew.writeInt(0);
               mplew.writeInt(0);
            }

            if (skillid == 400041009) {
               mplew.writeInt(chr.getParty() != null ? chr.getParty().getId() : 0);
            }

            if (skillid - 400041011 >= -4 && skillid - 400041011 <= 4) {
               mplew.writeInt(chr.getParty() != null ? chr.getParty().getId() : 0);
            }

            if (skillid == 400041036) {
               mplew.writeInt(0);
               mplew.writeInt(0);
               mplew.writeInt(0);
               mplew.writeInt(0);
               mplew.writeInt(0);
               mplew.writeInt(0);
               mplew.writeInt(0);
               mplew.writeInt(0);
            }

            if (skillid != 63001002 && skillid != 63001004) {
               if (skillid == 63101104) {
                  mplew.writeInt(pos.x);
                  mplew.writeInt(pos.y);
                  mplew.write(direction);
               }
            } else {
               mplew.write(direction);
               mplew.writeInt(pos.x);
               mplew.writeInt(pos.y);
            }

            if (skillid != 152111005 && skillid != 152111006) {
               if (skillid == 80002393 || skillid == 80002394 || skillid == 80002395 || skillid == 80002421) {
                  mplew.writeInt(0);
               }

               if (GameConstants.sub_8242D0(skillid)) {
                  mplew.write((int)0);
               }
            }
            break;
         case 3:
            mplew.writeInt(skillid);
            mplew.writeInt(chr.getLevel());
            mplew.write(chr.getSkillLevel(GameConstants.getLinkedSkill(skillid)));
            break;
         case 4:
            mplew.writeInt(skillid);
            mplew.write(chr.getSkillLevel(GameConstants.getLinkedSkill(skillid)));
            if (skillid == 31111003) {
               mplew.writeInt(0);
            }

            if (skillid == 25121006) {
               mplew.writeInt(0);
            }
            break;
         case 5:
            mplew.writeInt(skillid);
            mplew.write(chr.getSkillLevel(GameConstants.getLinkedSkill(skillid)));
            mplew.writeInt(0);
            mplew.writeInt(0);
            break;
         case 6:
            mplew.writeInt(subeffectid);
            mplew.writeInt(subeffectid2);
            mplew.writeInt(skillid);
            mplew.write(chr.getSkillLevel(GameConstants.getLinkedSkill(skillid)));
            mplew.write(oldskillid);
            break;
         case 7:
            mplew.writeInt(skillid);
            mplew.write((int)0);
            break;
         case 8:
            mplew.write(subeffectid2);
            int j = 0;

            while(true) {
               if (j >= subeffectid2) {
                  break label316;
               }

               mplew.writeInt(oldskillid);
               mplew.writeInt(skillid);
               ++j;
            }
         case 9:
            mplew.write((int)0);
            mplew.writeInt(chr.getPetIndex((long)skillid));
            break;
         case 10:
            mplew.writeInt(skillid);
            if (GameConstants.sub_1F04F40(skillid)) {
               mplew.writeInt(pos.x);
               mplew.writeInt(pos.y);
               mplew.writeInt(chr.getSkillLevel(GameConstants.getLinkedSkill(skillid)));
            }

            if (skillid == 32111016) {
               mplew.writeInt(0);
               mplew.write((int)0);
               mplew.writeInt(0);
               mplew.writeInt(0);
               mplew.writeInt(0);
               mplew.writeInt(0);
            }

            if (skillid == 80002206 || skillid == 80000257 || skillid == 80000260 || skillid == 80002599) {
               mplew.writeInt(0);
               mplew.writeInt(0);
               mplew.writeInt(0);
            }

            if (skillid == 400021088) {
               mplew.writeInt(chr.getPosition().x);
               mplew.writeInt(chr.getPosition().y);
               mplew.writeInt(at.acrossPosition.x);
               mplew.writeInt(at.acrossPosition.y);
            } else if (skillid == 400031053) {
               mplew.writeInt(0);
               mplew.writeInt(chr.getPosition().x);
               mplew.writeInt(chr.getPosition().y);
            } else if (skillid == 36110005) {
               mplew.writeInt(pos.x);
               mplew.writeInt(pos.y);
               mplew.writeInt(subeffectid);
            }
         case 11:
         case 13:
         case 14:
         case 15:
         case 19:
         case 21:
         case 24:
         case 35:
         case 41:
         case 42:
         case 43:
         case 49:
         case 52:
         case 67:
         case 68:
         case 78:
         case 79:
         case 80:
         case 81:
         case 82:
         default:
            break;
         case 12:
            boolean i = false;
            mplew.write(i);
            mplew.write((int)0);
            mplew.write((int)0);
            mplew.writeInt(0);
            break;
         case 16:
            mplew.write(subeffectid);
            break;
         case 17:
            mplew.writeInt(skillid);
            break;
         case 18:
            mplew.writeMapleAsciiString(txt);
            break;
         case 20:
            mplew.writeInt(skillid);
            mplew.write(txt.length() > 0);
            if (txt.length() > 0) {
               mplew.writeMapleAsciiString(txt);
            }
            break;
         case 22:
            mplew.writeInt(direction);
            break;
         case 23:
            mplew.writeInt(0);
            break;
         case 25:
            mplew.writeInt(skillid);
            break;
         case 26:
            mplew.write(false);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeMapleAsciiString(txt);
            break;
         case 27:
            mplew.write(chr.getInventory(MapleInventoryType.CASH).countById(skillid));
            break;
         case 28:
            mplew.writeMapleAsciiString(txt);
            break;
         case 29:
            mplew.writeMapleAsciiString(txt);
            mplew.writeInt(1);
            break;
         case 30:
            a = false;
            mplew.write(a);
            if (a) {
               mplew.writeMapleAsciiString(txt);
               mplew.writeInt(0);
               mplew.writeInt(0);
            }
            break;
         case 31:
            mplew.writeMapleAsciiString(txt);
            mplew.writeInt(5000);
            mplew.writeInt(0);
            break;
         case 32:
            mplew.writeInt(skillid);
            mplew.writeMapleAsciiString(txt);
            break;
         case 33:
            mplew.writeMapleAsciiString(txt);
            break;
         case 34:
            mplew.writeMapleAsciiString(txt);
            mplew.writeInt(100);
            break;
         case 36:
            mplew.writeInt(skillid);
            mplew.write(oldskillid);
            mplew.write(false);
            mplew.writeInt(skillid);
            break;
         case 37:
            mplew.writeInt(skillid);
            break;
         case 38:
            mplew.writeMapleAsciiString(txt);
            mplew.write((int)1);
            mplew.writeInt(oldskillid);
            mplew.writeInt(subeffectid);
            if (subeffectid == 2) {
               mplew.writeInt(skillid);
            }
            break;
         case 39:
            mplew.writeInt(0);
            break;
         case 40:
            mplew.writeInt(0);
            break;
         case 44:
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.write((int)0);
            break;
         case 45:
            mplew.writeInt(oldskillid);
            mplew.writeInt(skillid);
            break;
         case 46:
            mplew.writeInt(skillid);
            mplew.writeInt(oldskillid);
            break;
         case 47:
            mplew.write(false);
            break;
         case 48:
            mplew.writeInt(0);
            mplew.writeInt(0);
            break;
         case 50:
            int b = 0;
            mplew.write((int)b);
            switch(b) {
            case 0:
            case 2:
            case 3:
               mplew.writeInt(0);
               break label316;
            case 1:
            case 4:
               mplew.writeInt(0);
            default:
               break label316;
            }
         case 51:
            mplew.writeInt(0);
            break;
         case 53:
            mplew.write((int)0);
            mplew.write((int)0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            break;
         case 54:
            mplew.writeInt(0);
            break;
         case 55:
            mplew.writeMapleAsciiString(txt);
            a = false;
            mplew.write(a);
            if (!a) {
               mplew.writeInt(oldskillid);
               mplew.writeInt(skillid);
               mplew.writeInt(1);
            } else {
               boolean bool = false;
               mplew.write(false);
            }
            break;
         case 56:
            boolean reset = false;
            mplew.write(reset);
            if (!reset) {
               mplew.writeInt(0);
               mplew.write((int)0);
            }
            break;
         case 57:
            mplew.writeInt(skillid);
            mplew.writeInt(subeffectid);
            mplew.writeInt(subeffectid2);
            break;
         case 58:
            mplew.writeInt(0);
            break;
         case 59:
            mplew.writeInt(pos.x);
            mplew.writeInt(pos.y);
            break;
         case 60:
            mplew.write(false);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeMapleAsciiString(txt);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            if (txt.length() > 0) {
               mplew.writeInt(0);
               mplew.writeInt(0);
            }
            break;
         case 61:
            mplew.writeMapleAsciiString(txt);
            mplew.writeInt(50);
            mplew.writeInt(1500);
            mplew.writeInt(4);
            mplew.writeInt(0);
            mplew.writeInt(-200);
            mplew.writeInt(1);
            mplew.writeInt(4);
            mplew.writeInt(2);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeMapleAsciiString("");
            mplew.writeInt(0);
            mplew.write((int)0);
            break;
         case 62:
            mplew.writeInt(skillid);
            mplew.writeInt(oldskillid);
            break;
         case 63:
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            break;
         case 64:
            mplew.writeInt(0);
            mplew.writeShort(0);
            mplew.writeShort(0);
            mplew.writeShort(0);
            mplew.writeShort(0);
            break;
         case 65:
            PacketHelper.addItemInfo(mplew, item);
            break;
         case 66:
            mplew.writeInt(skillid);
            mplew.writeMapleAsciiString(txt);
            break;
         case 69:
            mplew.writeInt(0);
            mplew.writeInt(0);
            break;
         case 70:
            boolean z = false;
            mplew.write(z);
            if (z) {
               mplew.writeInt(0);
            }
            break;
         case 71:
            mplew.writeShort(0);
            mplew.writeInt(0);
            mplew.write(false);
            mplew.write((int)0);
            mplew.write(false);
            break;
         case 72:
            mplew.writeInt(0);
            PacketHelper.addItemInfo(mplew, item);
            break;
         case 73:
            sub_1E4D510(mplew, subeffectid, skillid, oldskillid);
            break;
         case 74:
            sub_1E4DCD0(mplew, skillid, oldskillid, subeffectid);
            break;
         case 75:
            mplew.writeInt(0);
            break;
         case 76:
            mplew.writeMapleAsciiString(txt);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.write(false);
            mplew.writeInt(0);
            mplew.writeInt(0);
            break;
         case 77:
            mplew.writeMapleAsciiString(txt);
            break;
         case 83:
            mplew.writeInt(skillid);
            mplew.writeInt(chr.getId());
            mplew.writeInt(1);
            mplew.writeInt(pos.x);
            mplew.writeInt(pos.y);
            mplew.write((int)1);
            mplew.writeInt(1);
            mplew.writeInt(300);
            mplew.writeInt(0);
            mplew.writeInt(0);
         }

         mplew.writeZeroBytes(100);
         return mplew.getPacket();
      }

      public static byte[] ErdaIncrease(Point position) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.ERDA_INCREASE.getValue());
         mplew.writeInt(position.x);
         mplew.writeInt(position.y);
         mplew.writeInt(6);
         int a = Randomizer.rand(5, 8);
         mplew.writeInt(a);
         return mplew.getPacket();
      }

      public static byte[] Orgel1(int skillid) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.EVENT_SKILL_END.getValue());
         mplew.writeInt(skillid);
         return mplew.getPacket();
      }

      public static byte[] OrgelTime(int skillid, int time, int type) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.EVENT_SKILL_START.getValue());
         mplew.writeInt(skillid);
         mplew.writeInt(time);
         mplew.writeInt(type);
         return mplew.getPacket();
      }

      public static byte[] OrgelStart(int skillid, int type) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.EVENT_SKILL_EFFECT.getValue());
         mplew.writeInt(skillid);
         mplew.writeInt(type);
         mplew.writeInt(20);
         return mplew.getPacket();
      }

      public static byte[] sub_1E4D510(MaplePacketLittleEndianWriter mplew, int subeffectid, int skillid, int skillLevel) {
         mplew.write(subeffectid);
         mplew.writeInt(skillid);
         mplew.writeInt(skillLevel);
         return mplew.getPacket();
      }

      public static byte[] sub_1E4DCD0(MaplePacketLittleEndianWriter mplew, int skillId, int skillLv, int type) {
         mplew.writeInt(skillId);
         mplew.writeInt(skillLv);
         if (skillId == 100017) {
            mplew.writeShort(type);
         }

         return mplew.getPacket();
      }

      public static byte[] gainExp(long exp) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
         mplew.writeShort(SendPacketOpcode.SHOW_EFFECT.getValue());
         mplew.write((int)25);
         mplew.writeLong(exp);
         return mplew.getPacket();
      }
   }
}
