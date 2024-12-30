package tools.packet;

import client.AvatarLook;
import client.MapleCharacter;
import client.MapleClient;
import constants.GameConstants;
import constants.JobConstants;
import handling.SendPacketOpcode;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tools.HexTool;
import tools.data.MaplePacketLittleEndianWriter;

public class LoginPacket {
   private static final String version;

   public static final byte[] initializeConnection(short mapleVersion, byte[] sendIv, byte[] recvIv, boolean ingame) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      int ret = 0;
      int ret = ret ^ mapleVersion & 32767;
      ret ^= 32768;
      ret ^= 65536;
      String.valueOf(ret);
      int packetsize = ingame ? 16 : 44 + version.length();
      mplew.writeShort(packetsize);
      if (!ingame) {
         mplew.writeShort(291);
         mplew.writeMapleAsciiString(version);
         mplew.write(recvIv);
         mplew.write(sendIv);
         mplew.write((int)2);
         mplew.write((int)0);
      }

      mplew.writeShort(291);
      mplew.writeInt(mapleVersion);
      mplew.write(recvIv);
      mplew.write(sendIv);
      mplew.write((int)2);
      if (!ingame) {
         mplew.writeInt(mapleVersion * 100 + 1);
         mplew.writeInt(mapleVersion * 100 + 1);
         mplew.writeInt(0);
         mplew.write(false);
         mplew.write(false);
      }

      mplew.write((int)4);
      return mplew.getPacket();
   }

   public static final byte[] getHotfix() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.HOTFIX.getValue());
      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static final byte[] SessionCheck(int value) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
      mplew.writeShort(SendPacketOpcode.SESSION_CHECK.getValue());
      mplew.writeInt(value);
      return mplew.getPacket();
   }

   public static byte[] OnOpcodeEncryption(byte[] aBuffer) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.OPCODE_ENCRYPTION.getValue());
      mplew.writeInt(aBuffer.length);
      mplew.write(aBuffer);
      return mplew.getPacket();
   }

   public static final byte[] debugClient() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DEBUG_CLIENT.getValue());
      int size1 = true;
      mplew.write(HexTool.getByteArrayFromHexString("1C 00 00 00 01 00 00 00 6C 00 00 00 05 00 00 00 03 00 41 6C 6C 06 00 00 00 91 00 00 00 00 00 00 00 03 00 41 6C 6C 0A 00 00 00 9B 00 00 00 B4 1B 32 01 03 00 41 6C 6C 0B 00 00 00 9D 00 00 00 01 00 00 00 03 00 41 6C 6C 0F 00 00 00 A3 00 00 00 E8 80 00 00 18 00 50 72 6F 6A 65 63 74 7C 43 65 6E 74 65 72 7C 57 6F 72 6C 64 49 44 7C 30 10 00 00 00 A3 00 00 00 E8 80 00 00 18 00 50 72 6F 6A 65 63 74 7C 43 65 6E 74 65 72 7C 57 6F 72 6C 64 49 44 7C 33 11 00 00 00 A3 00 00 00 E8 80 00 00 18 00 50 72 6F 6A 65 63 74 7C 43 65 6E 74 65 72 7C 57 6F 72 6C 64 49 44 7C 35 12 00 00 00 A3 00 00 00 E8 80 00 00 19 00 50 72 6F 6A 65 63 74 7C 43 65 6E 74 65 72 7C 57 6F 72 6C 64 49 44 7C 31 36 13 00 00 00 A4 00 00 00 78 00 00 00 18 00 50 72 6F 6A 65 63 74 7C 43 65 6E 74 65 72 7C 57 6F 72 6C 64 49 44 7C 30 14 00 00 00 A4 00 00 00 78 00 00 00 18 00 50 72 6F 6A 65 63 74 7C 43 65 6E 74 65 72 7C 57 6F 72 6C 64 49 44 7C 33 15 00 00 00 A4 00 00 00 96 00 00 00 18 00 50 72 6F 6A 65 63 74 7C 43 65 6E 74 65 72 7C 57 6F 72 6C 64 49 44 7C 35 16 00 00 00 A4 00 00 00 96 00 00 00 19 00 50 72 6F 6A 65 63 74 7C 43 65 6E 74 65 72 7C 57 6F 72 6C 64 49 44 7C 31 36 17 00 00 00 AF 00 00 00 00 00 00 00 03 00 41 6C 6C 18 00 00 00 B1 00 00 00 00 00 00 00 03 00 41 6C 6C 1A 00 00 00 6F 00 00 00 01 00 00 00 03 00 41 6C 6C 1B 00 00 00 AC 00 00 00 01 00 00 00 03 00 41 6C 6C 1C 00 00 00 B0 00 00 00 00 00 00 00 03 00 41 6C 6C 1D 00 00 00 B2 00 00 00 1E 00 00 00 03 00 41 6C 6C 1E 00 00 00 B9 00 00 00 00 00 00 00 03 00 41 6C 6C 2A 00 00 00 BA 00 00 00 05 00 00 00 03 00 41 6C 6C 2B 00 00 00 A2 00 00 00 01 00 00 00 18 00 50 72 6F 6A 65 63 74 7C 43 65 6E 74 65 72 7C 57 6F 72 6C 64 49 44 7C 30 2C 00 00 00 A2 00 00 00 01 00 00 00 18 00 50 72 6F 6A 65 63 74 7C 43 65 6E 74 65 72 7C 57 6F 72 6C 64 49 44 7C 33 2F 00 00 00 A3 00 00 00 30 75 00 00 19 00 50 72 6F 6A 65 63 74 7C 43 65 6E 74 65 72 7C 57 6F 72 6C 64 49 44 7C 34 39 30 00 00 00 A3 00 00 00 30 75 00 00 19 00 50 72 6F 6A 65 63 74 7C 43 65 6E 74 65 72 7C 57 6F 72 6C 64 49 44 7C 34 38 31 00 00 00 A3 00 00 00 30 75 00 00 19 00 50 72 6F 6A 65 63 74 7C 43 65 6E 74 65 72 7C 57 6F 72 6C 64 49 44 7C 35 32 35 00 00 00 C5 00 00 00 88 13 00 00 03 00 41 6C 6C 38 00 00 00 C6 00 00 00 00 00 00 00 03 00 41 6C 6C 39 00 00 00 CB 00 00 00 00 00 00 00 03 00 41 6C 6C"));
      return mplew.getPacket();
   }

   public static final byte[] HackShield() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.HACKSHIELD.getValue());
      mplew.write((int)1);
      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static final byte[] enableLogin() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
      mplew.writeShort(SendPacketOpcode.ENABLE_LOGIN.getValue());
      return mplew.getPacket();
   }

   public static final byte[] checkLogin() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CHECK_LOGIN.getValue());
      mplew.write((int)0);
      mplew.write((int)1);
      mplew.write((int)1);
      return mplew.getPacket();
   }

   public static final byte[] successLogin() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SUCCESS_LOGIN.getValue());
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(1);
      mplew.writeInt(1);
      return mplew.getPacket();
   }

   public static final byte[] getPing() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
      mplew.writeShort(SendPacketOpcode.PING.getValue());
      return mplew.getPacket();
   }

   public static final byte[] getAuthSuccessRequest(MapleClient client, String id, String pwd) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
      mplew.write((int)0);
      mplew.writeShort(0);
      mplew.writeMapleAsciiString(id);
      mplew.writeLong(772063122L);
      mplew.writeInt(client.getAccID());
      mplew.write((int)0);
      mplew.writeInt(130);
      mplew.writeInt(0);
      mplew.writeInt(22);
      mplew.write((int)3);
      mplew.write(client.getChatBlockedTime() > 0L ? 1 : 0);
      mplew.writeLong(client.getChatBlockedTime());
      mplew.write((int)1);
      mplew.writeShort(0);
      mplew.write((int)0);
      mplew.write(true);
      mplew.write((int)35);
      JobConstants.LoginJob[] var4 = JobConstants.LoginJob.values();
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         JobConstants.LoginJob j = var4[var6];
         mplew.write(j.getFlag());
         mplew.writeShort(j.getFlag());
      }

      mplew.write((int)0);
      mplew.writeInt(-1);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static final byte[] getLoginOtp(int what) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);
      mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
      mplew.writeInt(23);
      mplew.writeShort(what);
      return mplew.getPacket();
   }

   public static final byte[] getLoginFailed(int reason) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);
      mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
      mplew.writeInt(reason);
      mplew.writeShort(0);
      return mplew.getPacket();
   }

   public static final byte[] getPermBan(byte reason) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);
      mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
      mplew.writeShort(2);
      mplew.writeInt(0);
      mplew.writeShort(reason);
      mplew.write(HexTool.getByteArrayFromHexString("01 01 01 01 00"));
      return mplew.getPacket();
   }

   public static final byte[] getTempBan(long timestampTill, byte reason) {
      MaplePacketLittleEndianWriter w = new MaplePacketLittleEndianWriter(17);
      w.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
      w.write((int)2);
      w.write(HexTool.getByteArrayFromHexString("00 00 00 00 00"));
      w.write(reason);
      w.writeLong(timestampTill);
      return w.getPacket();
   }

   public static final byte[] deleteCharResponse(int cid, int state) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.DELETE_CHAR_RESPONSE.getValue());
      mplew.writeInt(cid);
      mplew.write(state);
      if (state == 69) {
         mplew.write((int)0);
         mplew.write((int)0);
         mplew.write((int)0);
         mplew.write((int)0);
         mplew.writeInt(0);
      } else if (state == 71) {
         mplew.write((int)0);
      }

      mplew.write((int)0);
      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static final byte[] secondPwError(byte mode) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
      mplew.writeShort(SendPacketOpcode.SECONDPW_ERROR.getValue());
      mplew.write(mode);
      return mplew.getPacket();
   }

   public static byte[] enableRecommended() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ENABLE_RECOMMENDED.getValue());
      mplew.writeInt(47);
      return mplew.getPacket();
   }

   public static byte[] sendRecommended(int world, String message) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SEND_RECOMMENDED.getValue());
      mplew.write(message != null ? 1 : 0);
      if (message != null) {
         mplew.writeInt(world);
         mplew.writeMapleAsciiString(message);
      }

      return mplew.getPacket();
   }

   public static final byte[] getServerList(int serverId, Map<Integer, Integer> channelLoad) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SERVERLIST.getValue());
      mplew.write(serverId);
      String worldName = LoginServer.getServerName();
      mplew.writeMapleAsciiString(worldName);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.write((int)0);
      mplew.write((int)1);
      mplew.writeMapleAsciiString(LoginServer.getEventMessage());
      int lastChannel = 1;
      Set<Integer> channels = channelLoad.keySet();

      int j;
      for(j = 30; j > 0; --j) {
         if (channels.contains(j)) {
            lastChannel = j;
            break;
         }
      }

      mplew.write((int)0);
      mplew.write(lastChannel);

      for(j = 1; j <= lastChannel; ++j) {
         int load;
         if (ChannelServer.getInstance(j) != null) {
            load = Math.max(1, ChannelServer.getInstance(j).getPlayerStorage().getAllCharacters().size());
         } else {
            load = 1;
         }

         mplew.writeMapleAsciiString(worldName + (j == 1 ? "-" + j : (j == 2 ? "- 20세이상" : "-" + (j - 1))));
         mplew.writeInt(load);
         mplew.write(serverId);
         mplew.write(j - 1);
         mplew.write((int)0);
      }

      mplew.writeShort(0);
      mplew.writeInt(0);
      mplew.write((int)1);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static final byte[] LeavingTheWorld() {
      MaplePacketLittleEndianWriter w = new MaplePacketLittleEndianWriter();
      w.writeShort(SendPacketOpcode.LEAVING_WORLD.getValue());
      w.write((int)3);
      w.writeMapleAsciiString("main");
      w.write((int)1);
      w.writeZeroBytes(8);
      w.writeMapleAsciiString("sub");
      w.writeZeroBytes(9);
      w.writeMapleAsciiString("sub_2");
      w.writeZeroBytes(9);
      w.write((int)1);
      return w.getPacket();
   }

   public static final byte[] getEndOfServerList() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SERVERLIST.getValue());
      mplew.write((int)255);
      int advertisement = 0;
      mplew.write((int)advertisement);

      for(int i = 0; i < advertisement; ++i) {
         mplew.writeMapleAsciiString("");
         mplew.writeMapleAsciiString("");
         mplew.writeInt(5000);
         mplew.writeInt(310);
         mplew.writeInt(60);
         mplew.writeInt(235);
         mplew.writeInt(538);
      }

      mplew.write((int)0);
      mplew.writeInt(-1);
      mplew.writeInt(0);
      return mplew.getPacket();
   }

   public static final byte[] getServerStatus(int status) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SERVERSTATUS.getValue());
      mplew.writeShort(status);
      return mplew.getPacket();
   }

   public static final byte[] checkOTP(int status) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CHECK_OTP.getValue());
      mplew.write(status);
      return mplew.getPacket();
   }

   public static final byte[] getCharList(MapleClient c, String secondpw, List<MapleCharacter> chars, int charslots, byte nameChange) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CHARLIST.getValue());
      mplew.write((int)0);
      mplew.writeMapleAsciiString("");
      mplew.writeInt(1);
      mplew.writeInt(1);
      mplew.writeInt(1);
      mplew.writeInt(charslots);
      mplew.write((int)0);
      mplew.write((int)0);
      mplew.writeInt(0);
      mplew.writeLong(PacketHelper.getKoreanTimestamp(System.currentTimeMillis()));
      mplew.write((int)0);
      mplew.writeInt(chars.size());
      Iterator var6 = chars.iterator();

      MapleCharacter chr;
      while(var6.hasNext()) {
         chr = (MapleCharacter)var6.next();
         mplew.writeInt(chr.getId());
      }

      mplew.write(chars.size());
      var6 = chars.iterator();

      while(var6.hasNext()) {
         chr = (MapleCharacter)var6.next();
         addCharEntry(mplew, chr, !chr.isGM() && chr.getLevel() >= 30, false);
      }

      mplew.write((secondpw == null || secondpw.length() <= 0) && c.getSecondPw() != 1 ? (secondpw != null && secondpw.length() <= 0 ? 2 : 0) : 1);
      mplew.write(c.getSecondPw());
      mplew.write(c.getSecondPw() == 1 ? 0 : 1);
      mplew.writeInt(charslots);
      mplew.writeInt(0);
      mplew.writeInt(-1);
      mplew.writeLong(PacketHelper.getKoreanTimestamp(System.currentTimeMillis()));
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeShort(0);
      mplew.writeInt(1);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeShort(0);
      mplew.writeInt(1);
      return mplew.getPacket();
   }

   public static final byte[] addNewCharEntry(MapleCharacter chr, boolean worked) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.ADD_NEW_CHAR_ENTRY.getValue());
      mplew.write(worked ? 0 : 1);
      mplew.writeInt(0);
      addCharEntry(mplew, chr, false, false);
      mplew.write((int)0);
      mplew.writeInt(-1);
      mplew.write((int)0);
      return mplew.getPacket();
   }

   public static final byte[] charNameResponse(String charname, boolean nameUsed) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CHAR_NAME_RESPONSE.getValue());
      mplew.writeMapleAsciiString(charname);
      mplew.write(nameUsed ? 1 : 0);
      return mplew.getPacket();
   }

   private static final void addCharEntry(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean ranking, boolean viewAll) {
      PacketHelper.addCharStats(mplew, chr);
      mplew.writeInt(0);
      mplew.writeLong(0L);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(1);
      mplew.write((int)0);
      if (GameConstants.isZero(chr.getJob())) {
         byte gender = chr.getGender();
         byte secondGender = chr.getSecondGender();
         chr.setGender((byte)0);
         chr.setSecondGender((byte)1);
         AvatarLook.encodeAvatarLook(mplew, chr, true, false);
         chr.setGender((byte)1);
         chr.setSecondGender((byte)0);
         AvatarLook.encodeAvatarLook(mplew, chr, true, true);
         chr.setGender(gender);
         chr.setSecondGender(secondGender);
      } else {
         AvatarLook.encodeAvatarLook(mplew, chr, true, false);
      }

   }

   public static final byte[] getSecondPasswordConfirm(byte op) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.AUTH_STATUS_WITH_SPW.getValue());
      mplew.write(op);
      if (op == 0) {
         mplew.write((int)1);
         mplew.write((int)35);
         JobConstants.LoginJob[] var2 = JobConstants.LoginJob.values();
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            JobConstants.LoginJob j = var2[var4];
            mplew.write(j.getFlag());
            mplew.writeShort(j.getFlag());
         }
      }

      return mplew.getPacket();
   }

   public static byte[] NewSendPasswordWay(MapleClient c) {
      MaplePacketLittleEndianWriter w = new MaplePacketLittleEndianWriter();
      w.writeShort(SendPacketOpcode.NEW_PASSWORD_CHECK.getValue());
      int a = c.getSecondPassword() != null && !c.getSecondPassword().equals("초기화") ? (c.getSecondPassword() != null ? 1 : 0) : 0;
      w.write(a);
      w.write((int)0);
      return w.getPacket();
   }

   public static byte[] skipNewPasswordCheck(MapleClient c) {
      MaplePacketLittleEndianWriter w = new MaplePacketLittleEndianWriter();
      w.writeShort(SendPacketOpcode.SKIP_NEW_PASSWORD_CHECK.getValue());
      w.write((int)1);
      return w.getPacket();
   }

   public static final byte[] getSecondPasswordResult(boolean success) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.AUTH_STATUS_WITH_SPW_RESULT.getValue());
      mplew.write(success ? 0 : 20);
      return mplew.getPacket();
   }

   public static final byte[] MapleExit() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.MAPLE_EXIT.getValue());
      return mplew.getPacket();
   }

   public static byte[] ChannelBackImg(boolean isSunday) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CHANNEL_BACK_IMG.getValue());
      if (isSunday) {
         mplew.writeMapleAsciiString("default");
      } else {
         mplew.writeMapleAsciiString("default");
      }

      return mplew.getPacket();
   }

   public static byte[] getSelectedChannelFailed(byte data, int ch) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SELECT_CHANNEL_LIST.getValue());
      mplew.write(data);
      mplew.writeShort(0);
      mplew.writeInt(ch);
      mplew.writeInt(-1);
      return mplew.getPacket();
   }

   public static byte[] getCharacterLoad() {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.CHARACTER_LOAD.getValue());
      mplew.writeInt(10);
      mplew.writeInt(483224);
      mplew.writeInt(0);
      mplew.writeInt(0);
      mplew.writeInt(3);
      return mplew.getPacket();
   }

   public static byte[] getSelectedChannelResult(int ch) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SELECT_CHANNEL_LIST.getValue());
      mplew.write((int)0);
      mplew.writeShort(0);
      mplew.writeInt(ch);
      mplew.writeInt(ch == 47 ? 1 : -1);
      return mplew.getPacket();
   }

   public static byte[] getSelectedWorldResult(int world) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.SELECTED_WORLD.getValue());
      mplew.writeInt(world);
      return mplew.getPacket();
   }

   public static final byte[] getKeyGuardResponse(String Key) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.LOG_OUT.getValue());
      mplew.writeMapleAsciiString(Key);
      return mplew.getPacket();
   }

   public static final byte[] OTPChange(int type) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.OTP_CHANGE.getValue());
      mplew.write((int)3);
      mplew.write(type);
      return mplew.getPacket();
   }

   public static final byte[] getAuthSuccessRequest(MapleClient client) {
      MaplePacketLittleEndianWriter w = new MaplePacketLittleEndianWriter();
      w.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
      w.write((int)0);
      w.writeMapleAsciiString(client.getAccountName());
      w.writeLong(-1L);
      w.writeInt(client.getAccID());
      w.write(client.isGm() ? 1 : 0);
      w.writeInt(client.isGm() ? 512 : 0);
      w.writeInt(10);
      w.writeInt(20);
      w.write((int)99);
      w.write(client.getChatBlockedTime() > 0L ? 1 : 0);
      w.writeLong(client.getChatBlockedTime());
      w.write((int)1);
      w.write((int)0);
      w.writeMapleAsciiString("");
      w.write(true);
      w.write((int)35);
      JobConstants.LoginJob[] var2 = JobConstants.LoginJob.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         JobConstants.LoginJob j = var2[var4];
         w.write(j.getFlag());
         w.writeShort(j.getFlag());
      }

      w.write((int)0);
      w.writeInt(-1);
      return w.getPacket();
   }

   public static final byte[] getCharEndRequest(MapleClient client, String Acc, String Pwd, boolean Charlist) {
      MaplePacketLittleEndianWriter w = new MaplePacketLittleEndianWriter();
      w.writeShort(SendPacketOpcode.CHAR_END_REQUEST.getValue());
      w.write((int)0);
      w.writeInt(client.getAccID());
      w.write(client.isGm() ? 1 : 0);
      w.writeInt(client.isGm() ? 32 : 0);
      w.writeInt(10);
      w.writeInt(20);
      w.write((int)99);
      w.write(client.getChatBlockedTime() > 0L ? 1 : 0);
      w.writeLong(client.getChatBlockedTime());
      w.writeMapleAsciiString(Pwd);
      w.writeMapleAsciiString(Acc);
      w.writeMapleAsciiString("");
      w.write(true);
      w.write((int)35);
      JobConstants.LoginJob[] var5 = JobConstants.LoginJob.values();
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         JobConstants.LoginJob j = var5[var7];
         w.write(j.getFlag());
         w.writeShort(j.getFlag());
      }

      w.write((int)0);
      w.writeInt(-1);
      w.write(Charlist);
      w.write((int)0);
      return w.getPacket();
   }

   static {
      int ret = 0;
      int ret = ret ^ 1149;
      ret ^= 32768;
      ret ^= 65536;
      version = String.valueOf(ret);
   }
}
