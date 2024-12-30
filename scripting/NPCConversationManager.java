package scripting;

import client.InnerSkillValueHolder;
import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.MapleQuestStatus;
import client.MapleShopLimit;
import client.MapleStat;
import client.SecondaryStat;
import client.SecondaryStatValueHolder;
import client.Skill;
import client.SkillEntry;
import client.SkillFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import client.inventory.MapleRing;
import constants.GameConstants;
import constants.KoreaCalendar;
import constants.ServerConstants;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.channel.handler.InterServerHandler;
import handling.channel.handler.PlayersHandler;
import handling.login.LoginInformationProvider;
import handling.world.World;
import handling.world.guild.MapleGuild;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import java.awt.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import javax.script.Invocable;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.InnerAbillity;
import server.MapleDonationSkill;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.SecondaryStatEffect;
import server.Timer;
import server.enchant.EnchantFlag;
import server.enchant.EquipmentEnchant;
import server.enchant.StarForceStats;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import server.life.MonsterDropEntry;
import server.maps.Event_DojoAgent;
import server.maps.MapleMap;
import server.marriage.MarriageDataEntry;
import server.quest.MapleQuest;
import server.quest.party.MapleNettPyramid;
import server.shops.MapleShopFactory;
import server.shops.MapleShopItem;
import tools.FileoutputUtil;
import tools.Pair;
import tools.StringUtil;
import tools.Triple;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.PacketHelper;
import tools.packet.SLFCGPacket;

public class NPCConversationManager extends AbstractPlayerInteraction {
   private String getText;
   private byte lastMsg = -1;
   private String script;
   private byte type;
   public boolean pendingDisposal = false;
   private Invocable iv;
   public static Map<Integer, String> hairlist = new HashMap();
   public static Map<Integer, String> facelist = new HashMap();

   public NPCConversationManager(MapleClient c, int npc, int questid, byte type, Invocable iv, String script) {
      super(c, npc, questid);
      this.type = type;
      this.iv = iv;
      this.script = script;
   }

   public void sendConductExchange(String text) {
      if (this.lastMsg <= -1) {
         this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCConductExchangeTalk(this.id, text));
         this.lastMsg = 0;
      }
   }

   public void sendPacket(short a, String b) {
      this.c.getSession().writeAndFlush(SLFCGPacket.SendPacket(a, b));
   }

   public Invocable getIv() {
      return this.iv;
   }

   public String getScript() {
      return this.script;
   }

   public int getNpc() {
      return this.id;
   }

   public int getQuest() {
      return this.id2;
   }

   public byte getType() {
      return this.type;
   }

   public void safeDispose() {
      this.pendingDisposal = true;
   }

   public void dispose() {
      NPCScriptManager.getInstance().dispose(this.c);
   }

   public void askMapSelection(String sel) {
      if (this.lastMsg <= -1) {
         this.c.getSession().writeAndFlush(CField.NPCPacket.getMapSelection(this.id, sel));
         this.lastMsg = 17;
      }
   }

   public void sendNext(String text) {
      this.sendNext(text, this.id);
   }

   public void sendNext(String text, int id) {
      if (this.lastMsg <= -1) {
         if (text.contains("#L")) {
            this.sendSimple(text);
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(id, (byte)0, text, "00 01", (byte)0));
            this.lastMsg = 0;
         }
      }
   }

   public void sendPlayerToNpc(String text) {
      this.sendNextS(text, (byte)3, this.id);
   }

   public void StartSpiritSavior() {
   }

   public void StartBlockGame() {
      MapleClient c = this.getClient();
      c.getSession().writeAndFlush(CField.onUserTeleport(c.getPlayer(), 65535, 0));
      c.getSession().writeAndFlush(CField.UIPacket.IntroDisableUI(true));
      c.getSession().writeAndFlush(CField.UIPacket.IntroLock(true));
      Timer.EventTimer.getInstance().schedule(() -> {
         c.getSession().writeAndFlush(SLFCGPacket.BlockGameCommandPacket(1));
         c.getSession().writeAndFlush(SLFCGPacket.BlockGameCommandPacket(2));
         c.getSession().writeAndFlush(SLFCGPacket.BlockGameControlPacket(100, 10));
      }, 2000L);
   }

   public boolean setZodiacGrade(int grade) {
      if (this.c.getPlayer().getKeyValue(190823, "grade") >= (long)grade) {
         return false;
      } else {
         this.c.getPlayer().setKeyValue(190823, "grade", String.valueOf(grade));
         this.c.getPlayer().getMap().broadcastMessage(this.c.getPlayer(), SLFCGPacket.ZodiacRankInfo(this.c.getPlayer().getId(), grade), true);
         this.c.getSession().writeAndFlush(SLFCGPacket.playSE("Sound/MiniGame.img/Result_Yut"));
         this.showEffect(false, "Effect/CharacterEff.img/gloryonGradeup");
         return true;
      }
   }

   public void sendNextNoESC(String text) {
      this.sendNextS(text, (byte)1, this.id);
   }

   public void sendNextNoESC(String text, int id) {
      this.sendNextS(text, (byte)1, id);
   }

   public void sendNextS(String text, byte type) {
      this.sendNextS(text, type, 0);
   }

   public void sendNextS(String text, byte type, int id, int idd) {
      if (this.lastMsg <= -1) {
         if (text.contains("#L")) {
            this.sendSimpleS(text, type);
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(id, (byte)0, text, "00 01", type, idd));
            this.lastMsg = 0;
         }
      }
   }

   public void sendNextS(String text, byte type, int idd) {
      if (this.lastMsg <= -1) {
         if (text.contains("#L")) {
            this.sendSimpleS(text, type);
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(this.id, (byte)0, text, "00 01", type, idd));
            this.lastMsg = 0;
         }
      }
   }

   public void sendCustom(String text, int type, int idd) {
      if (this.lastMsg <= -1) {
         this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalks(this.id, (byte)0, text, "00 01", (byte)type, idd));
         this.lastMsg = 0;
      }
   }

   public void sendPrev(String text) {
      this.sendPrev(text, this.id);
   }

   public void sendPrev(String text, int id) {
      if (this.lastMsg <= -1) {
         if (text.contains("#L")) {
            this.sendSimple(text);
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(id, (byte)0, text, "01 00", (byte)0));
            this.lastMsg = 0;
         }
      }
   }

   public void sendPrevS(String text, byte type) {
      this.sendPrevS(text, type, 0);
   }

   public void sendPrevS(String text, byte type, int idd) {
      if (this.lastMsg <= -1) {
         if (text.contains("#L")) {
            this.sendSimpleS(text, type);
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(this.id, (byte)0, text, "01 00", type, idd));
            this.lastMsg = 0;
         }
      }
   }

   public void sendNextPrev(String text) {
      this.sendNextPrev(text, this.id);
   }

   public void sendNextPrev(String text, int id) {
      if (this.lastMsg <= -1) {
         if (text.contains("#L")) {
            this.sendSimple(text);
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(id, (byte)0, text, "01 01", (byte)0));
            this.lastMsg = 0;
         }
      }
   }

   public void EnterCS() {
      InterServerHandler.EnterCS(this.c, this.c.getPlayer(), false);
   }

   public void PlayerToNpc(String text) {
      this.sendNextPrevS(text, (byte)3);
   }

   public void sendNextPrevS(String text) {
      this.sendNextPrevS(text, (byte)3);
   }

   public String getMobName(int mobid) {
      MapleData data = null;
      MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wz") + "/String.wz"));
      String ret = "";
      new ArrayList();
      data = dataProvider.getData("Mob.img");
      List<Pair<Integer, String>> mobPairList = new LinkedList();
      Iterator var7 = data.getChildren().iterator();

      while(var7.hasNext()) {
         MapleData mobIdData = (MapleData)var7.next();
         mobPairList.add(new Pair(Integer.parseInt(mobIdData.getName()), MapleDataTool.getString(mobIdData.getChildByPath("name"), "NO-NAME")));
      }

      var7 = mobPairList.iterator();

      while(var7.hasNext()) {
         Pair<Integer, String> mobPair = (Pair)var7.next();
         if ((Integer)mobPair.getLeft() == mobid) {
            ret = (String)mobPair.getRight();
         }
      }

      return ret;
   }

   public void sendNextPrevS(String text, byte type) {
      this.sendNextPrevS(text, type, 0);
   }

   public void sendNextPrevS(String text, byte type, int id, int idd) {
      if (this.lastMsg <= -1) {
         if (text.contains("#L")) {
            this.sendSimpleS(text, type);
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(id, (byte)0, text, "01 01", type, idd));
            this.lastMsg = 0;
         }
      }
   }

   public void sendNextPrevS(String text, byte type, int idd) {
      if (this.lastMsg <= -1) {
         if (text.contains("#L")) {
            this.sendSimpleS(text, type);
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(this.id, (byte)0, text, "01 01", type, idd));
            this.lastMsg = 0;
         }
      }
   }

   public void sendDimensionGate(String text) {
      if (this.lastMsg <= -1) {
         if (text.contains("#L")) {
            this.sendSimple(text);
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(this.id, (byte)19, text, "00 00", (byte)0));
            this.lastMsg = 0;
         }
      }
   }

   public void sendOk(String text) {
      this.sendOk(text, this.id);
   }

   public void sendOk(String text, int id) {
      if (this.lastMsg <= -1) {
         if (text.contains("#L")) {
            this.sendSimple(text);
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(id, (byte)0, text, "00 00", (byte)0));
            this.lastMsg = 0;
         }
      }
   }

   public void sendOkS(String text, byte type) {
      this.sendOkS(text, type, 0);
   }

   public void sendOkS(String text, byte type, int idd) {
      if (this.lastMsg <= -1) {
         if (text.contains("#L")) {
            this.sendSimpleS(text, type);
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(idd, (byte)0, text, "00 00", type, idd));
            this.lastMsg = 0;
         }
      }
   }

   public void sendYesNo(String text) {
      this.sendYesNo(text, this.id);
   }

   public void sendYesNo(String text, int id) {
      if (this.lastMsg <= -1) {
         if (text.contains("#L")) {
            this.sendSimple(text);
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(id, (byte)3, text, "", (byte)0));
            this.lastMsg = 2;
         }
      }
   }

   public void sendYesNoS(String text, byte type) {
      this.sendYesNoS(text, type, 0);
   }

   public void sendYesNoS(String text, byte type, int idd) {
      if (this.lastMsg <= -1) {
         if (text.contains("#L")) {
            this.sendSimpleS(text, type);
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(this.id, (byte)3, text, "", type, idd));
            this.lastMsg = 2;
         }
      }
   }

   public void sendAcceptDecline(String text) {
      this.askAcceptDecline(text);
   }

   public void sendAcceptDeclineNoESC(String text) {
      this.askAcceptDeclineNoESC(text);
   }

   public void askAcceptDecline(String text) {
      this.askAcceptDecline(text, this.id);
   }

   public void askAcceptDecline(String text, int id) {
      if (this.lastMsg <= -1) {
         if (text.contains("#L")) {
            this.sendSimple(text);
         } else {
            this.lastMsg = 16;
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(id, this.lastMsg, text, "", (byte)0));
         }
      }
   }

   public void askPraticeReplace(String text) {
      this.askPraticeReplace(text, this.id);
   }

   public void askPraticeReplace(String text, int id) {
      if (this.lastMsg <= -1) {
         if (text.contains("#L")) {
            this.sendSimple(text);
         } else {
            this.lastMsg = 3;
            this.c.getSession().writeAndFlush(CField.NPCPacket.getPraticeReplace(id, this.lastMsg, text, "", (byte)0, 1));
         }
      }
   }

   public void askAcceptDeclineNoESC(String text) {
      this.askAcceptDeclineNoESC(text, this.id);
   }

   public void askAcceptDeclineNoESC(String text, int id) {
      if (this.lastMsg <= -1) {
         if (text.contains("#L")) {
            this.sendSimple(text);
         } else {
            this.lastMsg = 16;
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(id, this.lastMsg, text, "", (byte)1));
         }
      }
   }

   public void sendStyle(String text, int... args) {
      this.askAvatar(text, args);
   }

   public void askCustomMixHairAndProb(String text) {
      this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalkMixStyle(this.id, text, GameConstants.isZero(this.c.getPlayer().getJob()) ? this.c.getPlayer().getGender() == 1 : false, GameConstants.isAngelicBuster(this.c.getPlayer().getJob()) ? this.c.getPlayer().getDressup() : false));
      this.lastMsg = 44;
   }

   public void askAvatar(String text, int... args) {
      this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalkStyle(this.c.getPlayer(), this.id, text, args));
      this.lastMsg = 9;
   }

   public void askAvatar(String text, int[] args1, int[] args2) {
      if (this.lastMsg <= -1) {
         if (GameConstants.isZero(this.c.getPlayer().getJob())) {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalkStyleZero(this.id, text, args1, args2));
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalkStyle(this.c.getPlayer(), this.id, text, args1));
         }

         this.lastMsg = 9;
      }
   }

   public void askCoupon(int itemid, int... args) {
      this.c.send(CWvsContext.UseMakeUpCoupon(this.c.getPlayer(), itemid, args));
   }

   public void askAvatarAndroid(String text, int... args) {
      this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalkStyleAndroid(this.id, text, args));
   }

   public void sendSimple(String text) {
      this.sendSimple(text, this.id);
   }

   public void sendSimple(String text, int id) {
      if (this.lastMsg <= -1) {
         if (!text.contains("#L")) {
            this.sendNext(text);
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(id, (byte)6, text, "", (byte)0));
            this.lastMsg = 5;
         }
      }
   }

   public void sendSimpleS(String text, byte type) {
      this.sendSimpleS(text, type, 0);
   }

   public void sendSimpleS(String text, byte type, int idd) {
      if (this.lastMsg <= -1) {
         if (!text.contains("#L")) {
            this.sendNextS(text, type);
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(this.id, (byte)6, text, "", type, idd));
            this.lastMsg = 5;
         }
      }
   }

   public void sendSimpleS(String text, byte type, int id, int idd) {
      if (this.lastMsg <= -1) {
         if (!text.contains("#L")) {
            this.sendNextS(text, type);
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(id, (byte)6, text, "", type, idd));
            this.lastMsg = 5;
         }
      }
   }

   public void sendZeroPreview(int itemid, int type, int beta, int beta2, int alpha, int alpha2) {
      if (GameConstants.isZero(this.c.getPlayer().getJob())) {
         this.c.getSession().writeAndFlush(CField.NPCPacket.getStylePreview(itemid, 2, type, beta, beta2, alpha, alpha2));
      }

   }

   public void sendStyle(String text, int[] styles1, int[] styles2) {
      if (this.lastMsg <= -1) {
         if (GameConstants.isZero(this.c.getPlayer().getJob())) {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalkStyleZero(this.id, text, styles1, styles2));
            this.lastMsg = 36;
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalkStyle(this.c.getPlayer(), this.id, text, styles1));
            this.lastMsg = 9;
         }

      }
   }

   public void sendIllustYesNo(String text, int face, boolean isLeft) {
      if (this.lastMsg <= -1) {
         if (text.contains("#L")) {
            this.sendIllustSimple(text, face, isLeft);
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(this.id, (byte)28, text, "", (byte)0, face, true, isLeft));
            this.lastMsg = 28;
         }
      }
   }

   public void sendIllustSimple(String text, int face, boolean isLeft) {
      if (this.lastMsg <= -1) {
         if (!text.contains("#L")) {
            this.sendIllustNext(text, face, isLeft);
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(this.id, (byte)30, text, "", (byte)0, face, true, isLeft));
            this.lastMsg = 30;
         }
      }
   }

   public void sendIllustNext(String text, int face, boolean isLeft) {
      if (this.lastMsg <= -1) {
         if (text.contains("#L")) {
            this.sendIllustSimple(text, face, isLeft);
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(this.id, (byte)26, text, "00 01", (byte)0, face, true, isLeft));
            this.lastMsg = 26;
         }
      }
   }

   public void sendIllustPrev(String text, int face, boolean isLeft) {
      if (this.lastMsg <= -1) {
         if (text.contains("#L")) {
            this.sendIllustSimple(text, face, isLeft);
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(this.id, (byte)26, text, "01 00", (byte)0, face, true, isLeft));
            this.lastMsg = 26;
         }
      }
   }

   public void sendIllustNextPrev(String text, int face, boolean isLeft) {
      if (this.lastMsg <= -1) {
         if (text.contains("#L")) {
            this.sendIllustSimple(text, face, isLeft);
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(this.id, (byte)26, text, "01 01", (byte)0, face, true, isLeft));
            this.lastMsg = 26;
         }
      }
   }

   public void sendIllustOk(String text, int face, boolean isLeft) {
      if (this.lastMsg <= -1) {
         if (text.contains("#L")) {
            this.sendIllustSimple(text, face, isLeft);
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(this.id, (byte)26, text, "00 00", (byte)0, face, true, isLeft));
            this.lastMsg = 26;
         }
      }
   }

   public void sendGetNumber(int id, String text, int def, int min, int max) {
      if (this.lastMsg <= -1) {
         if (text.contains("#L")) {
            this.sendSimple(text);
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalkNum(id, text, def, min, max));
            this.lastMsg = 4;
         }
      }
   }

   public void sendFriendsYesNo(String text, int id) {
      if (this.lastMsg <= -1) {
         if (text.contains("#L")) {
            this.sendSimple(text);
         } else {
            this.c.send(CField.NPCPacket.getNPCTalk(id, (byte)3, text, "", (byte)36));
            this.lastMsg = 3;
         }
      }
   }

   public void sendGetNumber(String text, int def, int min, int max) {
      if (this.lastMsg <= -1) {
         if (text.contains("#L")) {
            this.sendSimple(text);
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalkNum(this.id, text, def, min, max));
            this.lastMsg = 4;
         }
      }
   }

   public void sendGetText(String text) {
      this.sendGetText(text, this.id);
   }

   public void sendGetText(String text, int id) {
      if (this.lastMsg <= -1) {
         if (text.contains("#L")) {
            this.sendSimple(text);
         } else {
            this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalkText(id, text));
            this.lastMsg = 3;
         }
      }
   }

   public void setGetText(String text) {
      this.getText = text;
   }

   public String getText() {
      return this.getText;
   }

   public void setZeroSecondHair(int hair) {
      this.getPlayer().setSecondHair(hair);
      this.getPlayer().updateZeroStats();
      this.getPlayer().equipChanged();
   }

   public void setZeroSecondFace(int face) {
      this.getPlayer().setSecondFace(face);
      this.getPlayer().updateZeroStats();
      this.getPlayer().equipChanged();
   }

   public void setZeroSecondSkin(int color) {
      this.getPlayer().setSecondSkinColor((byte)color);
      this.getPlayer().updateZeroStats();
      this.getPlayer().equipChanged();
   }

   public void setAngelicSecondHair(int hair) {
      this.getPlayer().setSecondHair(hair);
      this.getPlayer().updateAngelicStats();
      this.getPlayer().equipChanged();
   }

   public void setAngelicSecondFace(int face) {
      this.getPlayer().setSecondFace(face);
      this.getPlayer().updateAngelicStats();
      this.getPlayer().equipChanged();
   }

   public void setAngelicSecondSkin(int color) {
      this.getPlayer().setSecondSkinColor((byte)color);
      this.getPlayer().updateAngelicStats();
      this.getPlayer().equipChanged();
   }

   public void setHair(int hair) {
      this.getPlayer().setHair(hair);
      this.getPlayer().updateSingleStat(MapleStat.HAIR, (long)hair);
      this.getPlayer().equipChanged();
   }

   public void setFace(int face) {
      this.getPlayer().setFace(face);
      this.getPlayer().updateSingleStat(MapleStat.FACE, (long)face);
      this.getPlayer().equipChanged();
   }

   public void setSkin(int color) {
      this.getPlayer().setSkinColor((byte)color);
      this.getPlayer().updateSingleStat(MapleStat.SKIN, (long)color);
      this.getPlayer().equipChanged();
   }

   public int setRandomAvatar(int ticket, int... args_all) {
      this.gainItem(ticket, (short)-1);
      int args = args_all[Randomizer.nextInt(args_all.length)];
      if (args < 100) {
         this.c.getPlayer().setSkinColor((byte)args);
         this.c.getPlayer().updateSingleStat(MapleStat.SKIN, (long)args);
      } else if (args < 30000) {
         this.c.getPlayer().setFace(args);
         this.c.getPlayer().updateSingleStat(MapleStat.FACE, (long)args);
      } else {
         this.c.getPlayer().setHair(args);
         this.c.getPlayer().updateSingleStat(MapleStat.HAIR, (long)args);
      }

      this.c.getPlayer().equipChanged();
      return 1;
   }

   public int setAvatar(int ticket, int args) {
      if (hairlist.isEmpty() || facelist.isEmpty()) {
         Iterator var3 = MapleItemInformationProvider.getInstance().getAllItems().iterator();

         label54:
         while(true) {
            while(true) {
               if (!var3.hasNext()) {
                  break label54;
               }

               Pair<Integer, String> itemPair = (Pair)var3.next();
               if (!((String)itemPair.getRight()).toLowerCase().contains("헤어") && !((String)itemPair.getRight()).toLowerCase().contains("머리")) {
                  if (((String)itemPair.getRight()).toLowerCase().contains("얼굴")) {
                     facelist.put((Integer)itemPair.getLeft(), (String)itemPair.getRight());
                  }
               } else {
                  hairlist.put((Integer)itemPair.getLeft(), (String)itemPair.getRight());
               }
            }
         }
      }

      int mixranze = 0;
      if (args > 99999) {
         mixranze = args % 1000;
         args /= 1000;
      }

      if (hairlist.containsKey(args)) {
         if (this.c.getPlayer().getDressup()) {
            this.setAngelicSecondHair(args);
         } else {
            this.c.getPlayer().setHair(args);
            this.c.getPlayer().updateSingleStat(MapleStat.HAIR, (long)args);
         }
      } else if (facelist.containsKey(args)) {
         if (mixranze > 0) {
            String sum = args + mixranze;
            args = Integer.parseInt(sum);
         }

         if (this.c.getPlayer().getDressup()) {
            this.setAngelicSecondFace(args);
         } else {
            this.c.getPlayer().setFace(args);
            this.c.getPlayer().updateSingleStat(MapleStat.FACE, (long)args);
         }
      } else {
         if (args < 0) {
            args = 0;
         }

         if (this.c.getPlayer().getDressup()) {
            this.setAngelicSecondSkin(args);
         } else {
            this.c.getPlayer().setSkinColor((byte)args);
            this.c.getPlayer().updateSingleStat(MapleStat.SKIN, (long)args);
         }
      }

      this.c.getPlayer().equipChanged();
      return 1;
   }

   public int setZeroAvatar(int ticket, int args1, int args2) {
      int mixranze = 0;
      int mixranze2 = 0;
      if (args1 > 99999) {
         mixranze = args1 % 1000;
         args1 /= 1000;
      }

      if (args2 > 99999) {
         mixranze2 = args2 % 1000;
         args2 /= 1000;
      }

      if (!hairlist.containsKey(args1) && !hairlist.containsKey(args2)) {
         if (!facelist.containsKey(args1) && !facelist.containsKey(args2)) {
            if (ticket == 0) {
               this.c.getPlayer().setSkinColor((byte)args1);
               this.c.getPlayer().updateSingleStat(MapleStat.SKIN, (long)args1);
            } else {
               this.c.getPlayer().setSecondSkinColor((byte)args2);
               this.c.getPlayer().updateSingleStat(MapleStat.SKIN, (long)args2);
               this.c.getPlayer().fakeRelog();
            }
         } else {
            String sum;
            if (mixranze > 0) {
               sum = args1 + mixranze;
               args1 = Integer.parseInt(sum);
            }

            if (mixranze2 > 0) {
               sum = args2 + mixranze;
               args2 = Integer.parseInt(sum);
            }

            if (ticket == 0) {
               this.c.getPlayer().setFace(args1);
               this.c.getPlayer().updateSingleStat(MapleStat.FACE, (long)args1);
            } else {
               this.c.getPlayer().setSecondFace(args2);
               this.c.getPlayer().updateSingleStat(MapleStat.FACE, (long)args2);
               this.c.getPlayer().fakeRelog();
            }
         }
      } else if (ticket == 0) {
         this.c.getPlayer().setHair(args1);
         this.c.getPlayer().updateSingleStat(MapleStat.HAIR, (long)args1);
      } else {
         this.c.getPlayer().setSecondHair(args2);
         this.c.getPlayer().updateSingleStat(MapleStat.HAIR, (long)args2);
         this.c.getPlayer().fakeRelog();
      }

      this.c.getPlayer().equipChanged();
      return 1;
   }

   public void setFaceAndroid(int faceId) {
      this.c.getPlayer().getAndroid().setFace(faceId);
      this.c.getPlayer().updateAndroid();
   }

   public void setHairAndroid(int hairId) {
      this.c.getPlayer().getAndroid().setHair(hairId);
      this.c.getPlayer().updateAndroid();
   }

   public void setSkinAndroid(int color) {
      this.c.getPlayer().getAndroid().setSkin(color);
      this.c.getPlayer().updateAndroid();
   }

   public void sendStorage() {
      this.c.getPlayer().setStorageNPC(this.id);
      this.c.getSession().writeAndFlush(CField.NPCPacket.getStorage((byte)0));
   }

   public void openShop(int idd) {
      if (MapleShopFactory.getInstance().getShop(idd).getRechargeShop() == 1) {
         boolean active = false;
         boolean save = false;
         Calendar ocal = Calendar.getInstance();
         Iterator var5 = MapleShopFactory.getInstance().getShop(idd).getItems().iterator();

         label196:
         while(true) {
            MapleShopItem item;
            int maxday;
            int month;
            int day;
            do {
               if (!var5.hasNext()) {
                  if (active) {
                     MapleShopFactory.getInstance().clear();
                     MapleShopFactory.getInstance().getShop(this.id);
                  } else if (save) {
                     this.c.saveShopLimit(this.c.getShopLimit());
                  }
                  break label196;
               }

               item = (MapleShopItem)var5.next();
               maxday = ocal.getActualMaximum(5);
               month = ocal.get(2) + 1;
               day = ocal.get(5);
            } while(item.getReCharge() <= 0);

            Iterator var10 = this.c.getShopLimit().iterator();

            while(var10.hasNext()) {
               MapleShopLimit shl = (MapleShopLimit)var10.next();
               if (shl.getLastBuyMonth() > 0 && shl.getLastBuyDay() > 0) {
                  Calendar baseCal = new GregorianCalendar(ocal.get(1), shl.getLastBuyMonth(), shl.getLastBuyDay());
                  Calendar targetCal = new GregorianCalendar(ocal.get(1), month, day);
                  long diffSec = (targetCal.getTimeInMillis() - baseCal.getTimeInMillis()) / 1000L;
                  long diffDays = diffSec / 86400L;
                  if (shl.getItemid() == item.getItemId() && shl.getShopId() == MapleShopFactory.getInstance().getShop(idd).getId() && shl.getPosition() == item.getPosition() && diffDays >= 0L) {
                     shl.setLastBuyMonth(0);
                     shl.setLastBuyDay(0);
                     shl.setLimitCountAcc(0);
                     shl.setLimitCountChr(0);
                     save = true;
                     break;
                  }
               }
            }

            if (item.getReChargeDay() <= day && item.getReChargeMonth() <= month) {
               active = true;
               int afterday = day + item.getReCharge();
               if (afterday > maxday) {
                  afterday -= maxday;
                  ++month;
                  if (month > 12) {
                     month = 1;
                  }
               }

               Connection con = null;
               PreparedStatement ps = null;

               try {
                  con = DatabaseConnection.getConnection();
                  ps = con.prepareStatement("UPDATE shopitems SET rechargemonth = ?, rechargeday = ?, resetday = ? WHERE position = ? AND itemid = ? AND tab = ?");
                  ps.setInt(1, month);
                  ps.setInt(2, afterday);
                  ps.setInt(3, day);
                  ps.setInt(4, item.getPosition());
                  ps.setInt(5, item.getItemId());
                  ps.setByte(6, item.getTab());
                  ps.executeUpdate();
                  ps.close();
                  con.close();
               } catch (SQLException var26) {
                  var26.printStackTrace();
               } finally {
                  try {
                     if (con != null) {
                        con.close();
                     }

                     if (ps != null) {
                        ps.close();
                     }
                  } catch (SQLException var25) {
                     var25.printStackTrace();
                  }

               }
            }
         }
      }

      MapleShopFactory.getInstance().getShop(idd).sendShop(this.c);
   }

   public int gainGachaponItem(int id, int quantity) {
      return this.gainGachaponItem(id, quantity, this.c.getPlayer().getMap().getStreetName());
   }

   public int gainGachaponItem(int id, int quantity, String msg) {
      try {
         if (!MapleItemInformationProvider.getInstance().itemExists(id)) {
            return -1;
         } else {
            Item item = MapleInventoryManipulator.addbyId_Gachapon(this.c, id, (short)quantity);
            if (item == null) {
               return -1;
            } else {
               byte rareness = GameConstants.gachaponRareItem(item.getItemId());
               if (rareness > 0) {
                  World.Broadcast.broadcastMessage(CWvsContext.getGachaponMega(this.c.getPlayer().getName(), " : got a(n)", item, rareness, msg));
               }

               this.c.getSession().writeAndFlush(CField.EffectPacket.showCharmEffect(this.c.getPlayer(), id, 1, true, ""));
               return item.getItemId();
            }
         }
      } catch (Exception var6) {
         var6.printStackTrace();
         return -1;
      }
   }

   public void changeJob(int job) {
      this.c.getPlayer().changeJob(job);
   }

   public void startQuest(int idd) {
      MapleQuest.getInstance(idd).start(this.getPlayer(), this.id);
   }

   public void completeQuest(int idd) {
      MapleQuest.getInstance(idd).complete(this.getPlayer(), this.id);
   }

   public void forfeitQuest(int idd) {
      MapleQuest.getInstance(idd).forfeit(this.getPlayer());
   }

   public void forceStartQuest() {
      MapleQuest.getInstance(this.id2).forceStart(this.getPlayer(), this.getNpc(), (String)null);
   }

   public void forceStartQuest(int idd) {
      MapleQuest.getInstance(idd).forceStart(this.getPlayer(), this.getNpc(), (String)null);
   }

   public void forceStartQuest(String customData) {
      MapleQuest.getInstance(this.id2).forceStart(this.getPlayer(), this.getNpc(), customData);
   }

   public void forceCompleteQuest() {
      MapleQuest.getInstance(this.id2).forceComplete(this.getPlayer(), this.getNpc());
   }

   public void forceCompleteQuest(int idd) {
      MapleQuest.getInstance(idd).forceComplete(this.getPlayer(), this.getNpc());
   }

   public String getQuestCustomData(int id2) {
      return this.c.getPlayer().getQuestNAdd(MapleQuest.getInstance(id2)).getCustomData();
   }

   public void setQuestCustomData(int id2, String customData) {
      this.getPlayer().getQuestNAdd(MapleQuest.getInstance(id2)).setCustomData(customData);
   }

   public long getMeso() {
      return this.getPlayer().getMeso();
   }

   public void gainAp(int amount) {
      this.c.getPlayer().gainAp((short)amount);
   }

   public void expandInventory(byte type, int amt) {
      this.c.getPlayer().expandInventory(type, amt);
   }

   public void gainItemInStorages(int id) {
      Connection con = null;
      PreparedStatement ps = null;
      PreparedStatement ps2 = null;
      ResultSet rs = null;
      int itemid = 0;
      int str = 0;
      int dex = 0;
      int int_ = 0;
      int luk = 0;
      int watk = 0;
      int matk = 0;
      int hp = 0;
      int upg = 0;
      int slot = 0;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM cashstorages WHERE id = ? and charid = ?");
         ps.setInt(1, id);
         ps.setInt(2, this.c.getPlayer().getId());
         rs = ps.executeQuery();
         if (rs.next()) {
            itemid = rs.getInt("itemid");
            str = rs.getInt("str");
            dex = rs.getInt("dex");
            int_ = rs.getInt("int_");
            luk = rs.getInt("luk");
            watk = rs.getInt("watk");
            matk = rs.getInt("matk");
            hp = rs.getInt("maxhp");
            upg = rs.getInt("upg");
            slot = rs.getInt("slot");
         }

         ps.close();
         rs.close();
         ps2 = con.prepareStatement("DELETE FROM cashstorages WHERE id = ?");
         ps2.setInt(1, id);
         ps2.executeUpdate();
         ps2.close();
         con.close();
      } catch (SQLException var37) {
         var37.printStackTrace();
      } finally {
         if (ps != null) {
            try {
               ps.close();
            } catch (SQLException var36) {
               var36.printStackTrace();
            }
         }

         if (ps2 != null) {
            try {
               ps2.close();
            } catch (SQLException var35) {
               var35.printStackTrace();
            }
         }

         if (rs != null) {
            try {
               ps.close();
            } catch (SQLException var34) {
               var34.printStackTrace();
            }
         }

         if (con != null) {
            try {
               con.close();
            } catch (SQLException var33) {
               var33.printStackTrace();
            }
         }

      }

      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      Equip item = (Equip)ii.getEquipById(itemid);
      item.setStr((short)str);
      item.setDex((short)dex);
      item.setInt((short)int_);
      item.setLuk((short)luk);
      item.setWatk((short)watk);
      item.setMatk((short)matk);
      item.setHp((short)hp);
      item.setUpgradeSlots((byte)slot);
      item.setLevel((byte)upg);
      MapleInventoryManipulator.addbyItem(this.c, item);
   }

   public void StoreInStorages(int charid, int itemid, int str, int dex, int int_, int luk, int watk, int matk) {
      Connection con = null;
      PreparedStatement ps = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("INSERT INTO cashstorages (charid, itemid, str, dex, int_, luk, watk, matk) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
         ps.setInt(1, charid);
         ps.setInt(2, itemid);
         ps.setInt(3, str);
         ps.setInt(4, dex);
         ps.setInt(5, int_);
         ps.setInt(6, luk);
         ps.setInt(7, watk);
         ps.setInt(8, matk);
         ps.executeUpdate();
         ps.close();
         con.close();
      } catch (SQLException var24) {
         var24.printStackTrace();
      } finally {
         if (ps != null) {
            try {
               ps.close();
            } catch (SQLException var23) {
               var23.printStackTrace();
            }
         }

         if (con != null) {
            try {
               con.close();
            } catch (SQLException var22) {
               var22.printStackTrace();
            }
         }

      }

   }

   public String getCashStorages(int charid) {
      String ret = "";
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM cashstorages WHERE charid = ?");
         ps.setInt(1, charid);
         rs = ps.executeQuery();
         if (rs.next()) {
            ret = ret + "#L" + rs.getInt("id") + "##i" + rs.getInt("itemid") + "##z" + rs.getInt("itemid") + "#\r\n";
         }

         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var23) {
         var23.printStackTrace();
      } finally {
         if (rs != null) {
            try {
               rs.close();
            } catch (SQLException var22) {
               var22.printStackTrace();
            }
         }

         if (ps != null) {
            try {
               ps.close();
            } catch (SQLException var21) {
               var21.printStackTrace();
            }
         }

         if (con != null) {
            try {
               con.close();
            } catch (SQLException var20) {
               var20.printStackTrace();
            }
         }

      }

      return ret;
   }

   public String getCharacterList(int accountid) {
      String ret = "";
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM characters WHERE accountid = ?");
         ps.setInt(1, accountid);

         for(rs = ps.executeQuery(); rs.next(); ret = ret + "#L" + rs.getInt("id") + "#" + rs.getString("name") + "\r\n") {
         }

         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var23) {
         var23.printStackTrace();
      } finally {
         if (rs != null) {
            try {
               rs.close();
            } catch (SQLException var22) {
               var22.printStackTrace();
            }
         }

         if (ps != null) {
            try {
               ps.close();
            } catch (SQLException var21) {
               var21.printStackTrace();
            }
         }

         if (con != null) {
            try {
               con.close();
            } catch (SQLException var20) {
               var20.printStackTrace();
            }
         }

      }

      return ret;
   }

   public final void clearSkills() {
      Map<Skill, SkillEntry> skills = new HashMap(this.getPlayer().getSkills());
      Map<Skill, SkillEntry> newList = new HashMap();
      Iterator var3 = skills.entrySet().iterator();

      while(var3.hasNext()) {
         Entry<Skill, SkillEntry> skill = (Entry)var3.next();
         newList.put((Skill)skill.getKey(), new SkillEntry(0, (byte)0, -1L));
      }

      this.getPlayer().changeSkillsLevel(newList);
      newList.clear();
      skills.clear();
   }

   public final void skillmaster() {
      MapleDataProvider var10000 = MapleDataProviderFactory.getDataProvider(MapleDataProviderFactory.fileInWZPath("Skill.wz"));
      String var10001 = this.c.getPlayer().getJob().makeConcatWithConstants<invokedynamic>(this.c.getPlayer().getJob());
      MapleData data = var10000.getData(StringUtil.getLeftPaddedStr(var10001, '0', 3) + ".img");
      Iterator var2 = data.iterator();

      while(true) {
         MapleData skill;
         do {
            if (!var2.hasNext()) {
               if (GameConstants.isZero(this.c.getPlayer().getJob())) {
                  int[] jobs = new int[]{10000, 10100, 10110, 10111, 10112};
                  int[] var13 = jobs;
                  int var14 = jobs.length;

                  label73:
                  for(int var15 = 0; var15 < var14; ++var15) {
                     int job = var13[var15];
                     data = MapleDataProviderFactory.getDataProvider(MapleDataProviderFactory.fileInWZPath("Skill.wz")).getData(job + ".img");
                     Iterator var7 = data.iterator();

                     while(true) {
                        MapleData skill;
                        do {
                           if (!var7.hasNext()) {
                              if (this.c.getPlayer().getLevel() >= 200) {
                                 this.c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(100001005), 1, (byte)1);
                              }
                              continue label73;
                           }

                           skill = (MapleData)var7.next();
                        } while(skill == null);

                        Iterator var9 = skill.getChildren().iterator();

                        while(var9.hasNext()) {
                           MapleData skillId = (MapleData)var9.next();
                           if (!skillId.getName().equals("icon")) {
                              byte maxLevel = (byte)MapleDataTool.getIntConvert("maxLevel", skillId.getChildByPath("common"), 0);
                              if (maxLevel < 0) {
                                 maxLevel = 1;
                              }

                              if (MapleDataTool.getIntConvert("invisible", skillId, 0) == 0 && this.c.getPlayer().getLevel() >= MapleDataTool.getIntConvert("reqLev", skillId, 0)) {
                                 this.c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(Integer.parseInt(skillId.getName())), maxLevel, maxLevel);
                              }
                           }
                        }
                     }
                  }
               }

               if (GameConstants.isKOC(this.c.getPlayer().getJob()) && this.c.getPlayer().getLevel() >= 100) {
                  this.c.getPlayer().changeSkillLevel(11121000, (byte)30, (byte)30);
                  this.c.getPlayer().changeSkillLevel(12121000, (byte)30, (byte)30);
                  this.c.getPlayer().changeSkillLevel(13121000, (byte)30, (byte)30);
                  this.c.getPlayer().changeSkillLevel(14121000, (byte)30, (byte)30);
                  this.c.getPlayer().changeSkillLevel(15121000, (byte)30, (byte)30);
               }

               return;
            }

            skill = (MapleData)var2.next();
         } while(skill == null);

         Iterator var4 = skill.getChildren().iterator();

         while(var4.hasNext()) {
            MapleData skillId = (MapleData)var4.next();
            if (!skillId.getName().equals("icon")) {
               byte maxLevel = (byte)MapleDataTool.getIntConvert("maxLevel", skillId.getChildByPath("common"), 0);
               if (maxLevel < 0) {
                  maxLevel = 1;
               }

               if (MapleDataTool.getIntConvert("invisible", skillId, 0) == 0 && this.c.getPlayer().getLevel() >= MapleDataTool.getIntConvert("reqLev", skillId, 0)) {
                  this.c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(Integer.parseInt(skillId.getName())), maxLevel, maxLevel);
               }
            }
         }
      }
   }

   public static void writeLog(String path, String data, boolean writeafterend) {
      try {
         File fFile = new File(path);
         if (!fFile.exists()) {
            fFile.createNewFile();
         }

         FileOutputStream out = new FileOutputStream(path, true);
         long time = System.currentTimeMillis();
         SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
         String str = dayTime.format(new Date(time));
         String msg = "\r\n" + str + " | " + data;
         out.write(msg.getBytes());
         out.close();
         out.flush();
      } catch (IOException var10) {
         var10.printStackTrace();
      }

   }

   public void addBoss(String boss) {
      if (this.c.getPlayer().getParty() != null) {
         this.c.getPlayer().removeV_boss("bossPractice");
         KoreaCalendar kc = new KoreaCalendar();
         int var10000 = kc.getYeal() % 100;
         String today = var10000 + "/" + kc.getMonths() + "/" + kc.getDays();
         Iterator var4 = this.getPlayer().getParty().getMembers().iterator();

         while(var4.hasNext()) {
            MaplePartyCharacter chr = (MaplePartyCharacter)var4.next();
            MapleCharacter ch = this.c.getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if (ch != null) {
               ch.removeV_boss("bossPractice");
               ch.removeV_boss(boss);
               long var10002 = System.currentTimeMillis();
               ch.addKV_boss(boss, (var10002 + 1800000L).makeConcatWithConstants<invokedynamic>(var10002 + 1800000L));
               String var9 = FileoutputUtil.보스입장;
               int var10001 = this.c.getAccID();
               FileoutputUtil.log(var9, "[입장] 계정 번호 : " + var10001 + " | 파티번호 : " + chr.getPlayer().getParty().getId() + " | 캐릭터 : " + ch.getName() + "(" + ch.getId() + ") | 입장 보스 : " + boss);
               byte var8 = -1;
               switch(boss.hashCode()) {
               case -2018239135:
                  if (boss.equals("Normal_Populatus")) {
                     var8 = 4;
                  }
                  break;
               case -472131166:
                  if (boss.equals("Normal_Kawoong")) {
                     var8 = 2;
                  }
                  break;
               case -401177409:
                  if (boss.equals("Normal_JinHillah")) {
                     var8 = 23;
                  }
                  break;
               case -153131730:
                  if (boss.equals("Normal_Arkarium")) {
                     var8 = 11;
                  }
                  break;
               case 253322940:
                  if (boss.equals("Easy_Populatus")) {
                     var8 = 3;
                  }
                  break;
               case 354330098:
                  if (boss.equals("Black_Mage")) {
                     var8 = 26;
                  }
                  break;
               case 622236989:
                  if (boss.equals("Normal_Lucid")) {
                     var8 = 17;
                  }
                  break;
               case 634456222:
                  if (boss.equals("Hard_Demian")) {
                     var8 = 15;
                  }
                  break;
               case 634578532:
                  if (boss.equals("Normal_Zakum")) {
                     var8 = 0;
                  }
                  break;
               case 649264393:
                  if (boss.equals("Hard_Dunkel")) {
                     var8 = 25;
                  }
                  break;
               case 683009093:
                  if (boss.equals("Chaos_Pinkbean")) {
                     var8 = 13;
                  }
                  break;
               case 773302898:
                  if (boss.equals("Chaos_Horntail")) {
                     var8 = 9;
                  }
                  break;
               case 916262526:
                  if (boss.equals("Normal_Pinkbean")) {
                     var8 = 12;
                  }
                  break;
               case 1006556331:
                  if (boss.equals("Normal_Horntail")) {
                     var8 = 8;
                  }
                  break;
               case 1028523123:
                  if (boss.equals("Easy_Arkarium")) {
                     var8 = 10;
                  }
                  break;
               case 1086594120:
                  if (boss.equals("Chaos_Dusk")) {
                     var8 = 22;
                  }
                  break;
               case 1091918901:
                  if (boss.equals("Normal_VonLeon")) {
                     var8 = 6;
                  }
                  break;
               case 1310725798:
                  if (boss.equals("Hard_Will")) {
                     var8 = 20;
                  }
                  break;
               case 1405307649:
                  if (boss.equals("Normal_Dusk")) {
                     var8 = 21;
                  }
                  break;
               case 1405861930:
                  if (boss.equals("Normal_Will")) {
                     var8 = 19;
                  }
                  break;
               case 1463075864:
                  if (boss.equals("Easy_Lucid")) {
                     var8 = 16;
                  }
                  break;
               case 1569825849:
                  if (boss.equals("Hard_VonLeon")) {
                     var8 = 7;
                  }
                  break;
               case 1684226128:
                  if (boss.equals("Easy_VonLeon")) {
                     var8 = 5;
                  }
                  break;
               case 1880774029:
                  if (boss.equals("Normal_Dunkel")) {
                     var8 = 24;
                  }
                  break;
               case 1967822171:
                  if (boss.equals("Hard_Lotus")) {
                     var8 = 14;
                  }
                  break;
               case 1967984193:
                  if (boss.equals("Hard_Lucid")) {
                     var8 = 18;
                  }
                  break;
               case 1973986485:
                  if (boss.equals("Hard_Seren")) {
                     var8 = 27;
                  }
                  break;
               case 1984149632:
                  if (boss.equals("Normal_Hillah")) {
                     var8 = 1;
                  }
               }

               switch(var8) {
               case 0:
                  MapleQuest.getInstance(7003).forceStart(ch, 0, today);
                  MapleQuest.getInstance(7004).forceStart(ch, 0, "1");
                  break;
               case 1:
                  ch.updateInfoQuest(3981, "eNum=1;lastDate=" + today);
                  break;
               case 2:
                  MapleQuest.getInstance(3590).forceStart(ch, 0, today);
                  MapleQuest.getInstance(3591).forceStart(ch, 0, "1");
                  break;
               case 3:
               case 4:
                  MapleQuest.getInstance(7200).forceStart(ch, 0, today);
                  MapleQuest.getInstance(7201).forceStart(ch, 0, "1");
                  break;
               case 5:
               case 6:
               case 7:
                  ch.updateInfoQuest(7850, "eNum=1;lastDate=" + today);
                  break;
               case 8:
               case 9:
                  ch.updateInfoQuest(7312, "eNum=1;lastDate=" + today);
                  break;
               case 10:
               case 11:
                  ch.updateInfoQuest(7851, "eNum=1;lastDate=" + today);
                  break;
               case 12:
                  ch.setKeyValue(7403, "eNum", "1");
                  ch.setKeyValue(7403, "lastDate", today);
                  break;
               case 13:
                  ch.setKeyValue(7403, "eNumC", "1");
                  ch.setKeyValue(7403, "lastDateC", today);
                  break;
               case 14:
                  ch.setKeyValue(33126, "lastDate", today);
                  break;
               case 15:
                  ch.setKeyValue(34016, "lastDate", today);
                  break;
               case 16:
                  ch.setKeyValue(34364, "eNumE", "1");
                  ch.setKeyValue(34364, "lastDateE", today);
                  break;
               case 17:
                  ch.setKeyValue(34364, "eNum", "1");
                  ch.setKeyValue(34364, "lastDate", today);
                  break;
               case 18:
                  ch.setKeyValue(34364, "eNumH", "1");
                  ch.setKeyValue(34364, "lastDateH", today);
                  break;
               case 19:
                  ch.setKeyValue(35100, "lastDateN", today);
                  break;
               case 20:
                  ch.setKeyValue(35100, "lastDate", today);
                  break;
               case 21:
                  ch.setKeyValue(35137, "lastDateN", today);
                  ch.setKeyValue(35139, "lastDateH", today);
                  break;
               case 22:
                  ch.setKeyValue(35137, "lastDateN", today);
                  ch.setKeyValue(35139, "lastDateH", today);
                  break;
               case 23:
                  ch.setKeyValue(35260, "lastDate", today);
                  break;
               case 24:
                  ch.setKeyValue(35138, "lastDateN", today);
                  ch.setKeyValue(35140, "lastDateH", today);
                  break;
               case 25:
                  ch.setKeyValue(35138, "lastDateN", today);
                  ch.setKeyValue(35140, "lastDateH", today);
                  break;
               case 26:
                  ch.setKeyValue(35377, "lastDate", today);
                  break;
               case 27:
                  ch.setKeyValue(39932, "lastDate", today);
               }
            }
         }
      }

   }

   public void addBossPractice(String boss) {
      if (this.c.getPlayer().getParty() != null) {
         Iterator var2 = this.getPlayer().getParty().getMembers().iterator();

         while(var2.hasNext()) {
            MaplePartyCharacter chr = (MaplePartyCharacter)var2.next();
            MapleCharacter ch = this.c.getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if (ch != null) {
               ch.addKV_boss("bossPractice", "1");
               String var10000 = FileoutputUtil.보스입장;
               int var10001 = this.c.getAccID();
               FileoutputUtil.log(var10000, "[연습모드 입장] 계정 번호 : " + var10001 + " | 파티번호 : " + chr.getPlayer().getParty().getId() + " | 캐릭터 : " + this.c.getPlayer().getName() + "(" + this.c.getPlayer().getId() + ") | 입장 보스 : " + boss);
            }
         }
      }

   }

   public Object[] BossNotAvailableChrList(String boss, int limit) {
      Object[] arr = new Object[0];
      if (this.c.getPlayer().getParty() != null) {
         Iterator var4 = this.getPlayer().getParty().getMembers().iterator();

         while(var4.hasNext()) {
            MaplePartyCharacter chr = (MaplePartyCharacter)var4.next();
            Iterator var6 = ChannelServer.getAllInstances().iterator();

            while(var6.hasNext()) {
               ChannelServer channel = (ChannelServer)var6.next();
               MapleCharacter ch = channel.getPlayerStorage().getCharacterById(chr.getId());
               if (ch != null && ch.getGMLevel() < 6) {
                  String k = ch.getV(boss);
                  int key = k == null ? 0 : Integer.parseInt(ch.getV(boss));
                  if (key >= limit - 1) {
                     arr = add(arr, ch.getName());
                  }
               }
            }
         }
      }

      return arr;
   }

   public Object[] LevelNotAvailableChrList(int level) {
      Object[] arr = new Object[0];
      if (this.c.getPlayer().getParty() != null) {
         Iterator var3 = this.getPlayer().getParty().getMembers().iterator();

         while(var3.hasNext()) {
            MaplePartyCharacter chr = (MaplePartyCharacter)var3.next();
            Iterator var5 = ChannelServer.getAllInstances().iterator();

            while(var5.hasNext()) {
               ChannelServer channel = (ChannelServer)var5.next();
               MapleCharacter ch = channel.getPlayerStorage().getCharacterById(chr.getId());
               if (ch != null && ch.getGMLevel() < 6 && ch.getLevel() < level) {
                  arr = add(arr, ch.getName());
               }
            }
         }
      }

      return arr;
   }

   public static Object[] add(Object[] arr, Object... elements) {
      Object[] tempArr = new Object[arr.length + elements.length];
      System.arraycopy(arr, 0, tempArr, 0, arr.length);

      for(int i = 0; i < elements.length; ++i) {
         tempArr[arr.length + i] = elements[i];
      }

      return tempArr;
   }

   public boolean partyhaveItem(int itemid, int qty) {
      if (this.c.getPlayer().getParty() == null) {
         return false;
      } else {
         Iterator var3 = this.getPlayer().getParty().getMembers().iterator();

         while(var3.hasNext()) {
            MaplePartyCharacter chr = (MaplePartyCharacter)var3.next();
            Iterator var5 = ChannelServer.getAllInstances().iterator();

            while(var5.hasNext()) {
               ChannelServer channel = (ChannelServer)var5.next();
               MapleCharacter ch = channel.getPlayerStorage().getCharacterById(chr.getId());
               if (ch != null && ch.getGMLevel() <= 6) {
                  int getqty = this.itemQuantity(itemid);
                  if (getqty < qty) {
                     return false;
                  }
               }
            }
         }

         return true;
      }
   }

   public boolean isBossAvailable(String boss, int limit) {
      if (this.c.getPlayer().getParty() != null) {
         KoreaCalendar kc = new KoreaCalendar();
         int var10000 = kc.getYeal() % 100;
         String today = var10000 + "/" + kc.getMonths() + "/" + kc.getDays();
         Iterator<MaplePartyCharacter> iterator = this.getPlayer().getParty().getMembers().iterator();
         if (iterator.hasNext()) {
            MaplePartyCharacter chr = (MaplePartyCharacter)iterator.next();
            Iterator var7 = ChannelServer.getAllInstances().iterator();

            boolean enter;
            do {
               MapleCharacter ch;
               do {
                  do {
                     if (!var7.hasNext()) {
                        return true;
                     }

                     ChannelServer channel = (ChannelServer)var7.next();
                     ch = channel.getPlayerStorage().getCharacterById(chr.getId());
                  } while(ch == null);
               } while(ch.isGM());

               enter = false;
               boolean weekly = false;
               String CheckS = "lastDate";
               int checkenterquestid = 0;
               int checkclearquestid = 0;
               byte var16 = -1;
               switch(boss.hashCode()) {
               case -2143132136:
                  if (boss.equals("Easy_Cygnus")) {
                     var16 = 29;
                  }
                  break;
               case -2081986703:
                  if (boss.equals("Normal_Pierre")) {
                     var16 = 12;
                  }
                  break;
               case -2018239135:
                  if (boss.equals("Normal_Populatus")) {
                     var16 = 10;
                  }
                  break;
               case -1913703009:
                  if (boss.equals("Normal_Vellum")) {
                     var16 = 18;
                  }
                  break;
               case -1904448764:
                  if (boss.equals("Normal_VonBon")) {
                     var16 = 14;
                  }
                  break;
               case -1879005130:
                  if (boss.equals("Easy_Magnus")) {
                     var16 = 3;
                  }
                  break;
               case -659160966:
                  if (boss.equals("Chaos_Populatus")) {
                     var16 = 11;
                  }
                  break;
               case -655606275:
                  if (boss.equals("Chaos_Zakum")) {
                     var16 = 2;
                  }
                  break;
               case -472131166:
                  if (boss.equals("Normal_Kawoong")) {
                     var16 = 8;
                  }
                  break;
               case -435304109:
                  if (boss.equals("Normal_BloodyQueen")) {
                     var16 = 16;
                  }
                  break;
               case -401177409:
                  if (boss.equals("Normal_JinHillah")) {
                     var16 = 42;
                  }
                  break;
               case -153131730:
                  if (boss.equals("Normal_Arkarium")) {
                     var16 = 26;
                  }
                  break;
               case -31241684:
                  if (boss.equals("Chaos_BloodyQueen")) {
                     var16 = 17;
                  }
                  break;
               case 155113952:
                  if (boss.equals("GuildBoss")) {
                     var16 = 0;
                  }
                  break;
               case 253322940:
                  if (boss.equals("Easy_Populatus")) {
                     var16 = 9;
                  }
                  break;
               case 354330098:
                  if (boss.equals("Black_Mage")) {
                     var16 = 45;
                  }
                  break;
               case 622074967:
                  if (boss.equals("Normal_Lotus")) {
                     var16 = 31;
                  }
                  break;
               case 622236989:
                  if (boss.equals("Normal_Lucid")) {
                     var16 = 36;
                  }
                  break;
               case 634456222:
                  if (boss.equals("Hard_Demian")) {
                     var16 = 34;
                  }
                  break;
               case 634578532:
                  if (boss.equals("Normal_Zakum")) {
                     var16 = 1;
                  }
                  break;
               case 649264393:
                  if (boss.equals("Hard_Dunkel")) {
                     var16 = 44;
                  }
                  break;
               case 683009093:
                  if (boss.equals("Chaos_Pinkbean")) {
                     var16 = 28;
                  }
                  break;
               case 752639996:
                  if (boss.equals("Hard_Hillah")) {
                     var16 = 7;
                  }
                  break;
               case 773302898:
                  if (boss.equals("Chaos_Horntail")) {
                     var16 = 24;
                  }
                  break;
               case 871957240:
                  if (boss.equals("Chaos_Pierre")) {
                     var16 = 13;
                  }
                  break;
               case 888251181:
                  if (boss.equals("Hard_Magnus")) {
                     var16 = 5;
                  }
                  break;
               case 916262526:
                  if (boss.equals("Normal_Pinkbean")) {
                     var16 = 27;
                  }
                  break;
               case 1006556331:
                  if (boss.equals("Normal_Horntail")) {
                     var16 = 23;
                  }
                  break;
               case 1028523123:
                  if (boss.equals("Easy_Arkarium")) {
                     var16 = 25;
                  }
                  break;
               case 1040240934:
                  if (boss.equals("Chaos_Vellum")) {
                     var16 = 19;
                  }
                  break;
               case 1049495179:
                  if (boss.equals("Chaos_VonBon")) {
                     var16 = 15;
                  }
                  break;
               case 1086594120:
                  if (boss.equals("Chaos_Dusk")) {
                     var16 = 41;
                  }
                  break;
               case 1091918901:
                  if (boss.equals("Normal_VonLeon")) {
                     var16 = 21;
                  }
                  break;
               case 1310725798:
                  if (boss.equals("Hard_Will")) {
                     var16 = 39;
                  }
                  break;
               case 1405307649:
                  if (boss.equals("Normal_Dusk")) {
                     var16 = 40;
                  }
                  break;
               case 1405861930:
                  if (boss.equals("Normal_Will")) {
                     var16 = 38;
                  }
                  break;
               case 1463075864:
                  if (boss.equals("Easy_Lucid")) {
                     var16 = 35;
                  }
                  break;
               case 1569825849:
                  if (boss.equals("Hard_VonLeon")) {
                     var16 = 22;
                  }
                  break;
               case 1684226128:
                  if (boss.equals("Easy_VonLeon")) {
                     var16 = 20;
                  }
                  break;
               case 1855633811:
                  if (boss.equals("Normal_Cygnus")) {
                     var16 = 30;
                  }
                  break;
               case 1865965858:
                  if (boss.equals("Normal_Demian")) {
                     var16 = 33;
                  }
                  break;
               case 1880774029:
                  if (boss.equals("Normal_Dunkel")) {
                     var16 = 43;
                  }
                  break;
               case 1967822171:
                  if (boss.equals("Hard_Lotus")) {
                     var16 = 32;
                  }
                  break;
               case 1967984193:
                  if (boss.equals("Hard_Lucid")) {
                     var16 = 37;
                  }
                  break;
               case 1973986485:
                  if (boss.equals("Hard_Seren")) {
                     var16 = 46;
                  }
                  break;
               case 1984149632:
                  if (boss.equals("Normal_Hillah")) {
                     var16 = 6;
                  }
                  break;
               case 2119760817:
                  if (boss.equals("Normal_Magnus")) {
                     var16 = 4;
                  }
               }

               switch(var16) {
               case 0:
                  checkenterquestid = 7002;
                  checkclearquestid = 7001;
                  weekly = true;
                  break;
               case 1:
                  checkenterquestid = 7003;
                  break;
               case 2:
                  checkclearquestid = 15166;
                  break;
               case 3:
               case 4:
                  checkclearquestid = 3993;
                  break;
               case 5:
                  checkclearquestid = 3992;
                  break;
               case 6:
                  checkenterquestid = 3981;
                  break;
               case 7:
                  checkclearquestid = 3650;
                  break;
               case 8:
                  checkenterquestid = 3590;
                  break;
               case 9:
               case 10:
                  checkenterquestid = 7200;
                  break;
               case 11:
                  checkclearquestid = 3657;
                  break;
               case 12:
                  checkclearquestid = 30032;
                  break;
               case 13:
                  checkclearquestid = 30043;
                  break;
               case 14:
                  checkclearquestid = 30039;
                  break;
               case 15:
                  checkclearquestid = 30044;
                  break;
               case 16:
                  checkclearquestid = 30033;
                  break;
               case 17:
                  checkclearquestid = 30045;
                  break;
               case 18:
                  checkclearquestid = 30041;
                  break;
               case 19:
                  checkclearquestid = 30046;
                  break;
               case 20:
               case 21:
               case 22:
                  checkenterquestid = 7850;
                  break;
               case 23:
               case 24:
                  checkenterquestid = 7312;
                  break;
               case 25:
               case 26:
                  checkenterquestid = 7851;
                  break;
               case 27:
                  checkenterquestid = 7403;
                  checkclearquestid = 3652;
                  break;
               case 28:
                  checkenterquestid = 7403;
                  checkclearquestid = 3653;
                  CheckS = "lastDateC";
                  break;
               case 29:
               case 30:
                  checkclearquestid = 31199;
                  break;
               case 31:
                  checkclearquestid = '舗';
                  break;
               case 32:
                  checkenterquestid = '腦';
                  checkclearquestid = '舗';
                  break;
               case 33:
                  checkclearquestid = '蓡';
                  break;
               case 34:
                  checkenterquestid = '蓠';
                  checkclearquestid = '蓡';
                  break;
               case 35:
                  checkenterquestid = '蘼';
                  checkclearquestid = 3685;
                  CheckS = "lastDateE";
                  break;
               case 36:
                  checkenterquestid = '蘼';
                  checkclearquestid = 3685;
                  break;
               case 37:
                  checkenterquestid = '蘼';
                  checkclearquestid = 3685;
                  CheckS = "lastDateH";
                  break;
               case 38:
                  checkenterquestid = '褜';
                  checkclearquestid = 3658;
                  break;
               case 39:
                  checkenterquestid = '褜';
                  checkclearquestid = 3658;
                  CheckS = "lastDateN";
                  break;
               case 40:
                  checkenterquestid = '襁';
                  checkclearquestid = 3680;
                  CheckS = "lastDateN";
                  break;
               case 41:
                  checkenterquestid = '襃';
                  checkclearquestid = 3680;
                  CheckS = "lastDateH";
                  break;
               case 42:
                  checkenterquestid = '覼';
                  checkclearquestid = 3673;
                  break;
               case 43:
                  checkenterquestid = '襂';
                  checkclearquestid = 3681;
                  CheckS = "lastDateN";
                  break;
               case 44:
                  checkenterquestid = '襄';
                  checkclearquestid = 3681;
                  CheckS = "lastDateH";
                  break;
               case 45:
                  checkenterquestid = '許';
                  checkclearquestid = 3679;
                  break;
               case 46:
                  checkenterquestid = '鯼';
                  checkclearquestid = 3687;
               }

               if (checkenterquestid > 0 && chr.getPlayer().getKeyValueStr_boss(checkenterquestid, CheckS) != null && chr.getPlayer().getKeyValueStr_boss(checkenterquestid, CheckS).equals(today)) {
                  enter = true;
               }

               if (!enter) {
                  MapleQuestStatus quests = (MapleQuestStatus)chr.getPlayer().getQuest_Map().get(MapleQuest.getInstance(checkenterquestid));
                  if (quests != null && quests.getCustomData() != null && quests.getCustomData().equals(today)) {
                     enter = true;
                  }
               }

               if (checkclearquestid > 0 && !enter) {
                  if (weekly) {
                     if (chr.getPlayer().getKeyValueStr_boss(checkclearquestid, "lasttime") != null) {
                        String[] array = chr.getPlayer().getKeyValueStr_boss(checkclearquestid, "lasttime").split("/");
                        Calendar clear = new GregorianCalendar(Integer.parseInt("20" + array[0]), Integer.parseInt(array[1]) - 1, Integer.parseInt(array[2]));
                        Calendar ocal = Calendar.getInstance();
                        int yeal = clear.get(1);
                        int days = clear.get(5);
                        int day = ocal.get(7);
                        int day2 = clear.get(7);
                        int maxday = clear.getMaximum(5);
                        int month = clear.get(2);
                        int check = day2 == 5 ? 7 : (day2 == 6 ? 6 : (day2 == 7 ? 5 : 0));
                        int afterday;
                        if (check == 0) {
                           for(afterday = day2; afterday < 5; ++afterday) {
                              ++check;
                           }
                        }

                        afterday = days + check;
                        if (afterday > maxday) {
                           afterday -= maxday;
                           ++month;
                        }

                        if (month > 12) {
                           ++yeal;
                           month = 1;
                        }

                        Calendar after = new GregorianCalendar(yeal, month, afterday);
                        if (after.getTimeInMillis() > System.currentTimeMillis()) {
                           enter = true;
                        }
                     }
                  } else if (chr.getPlayer().getKeyValueStr_boss(checkclearquestid, "lasttime") != null && chr.getPlayer().getKeyValueStr_boss(checkclearquestid, "lasttime").equals(today)) {
                     enter = true;
                  }

                  if (!enter && ch.getV(boss) != null && Long.parseLong(ch.getV(boss)) - System.currentTimeMillis() >= 0L) {
                     enter = true;
                  }
               }
            } while(!enter);

            return false;
         }
      }

      return false;
   }

   public String isBossString(String boss) {
      String txt = "파티원 중 #r입장 조건#k을 충족하지 못하는 파티원이 있습니다.\r\n모든 파티원이 조건을 충족해야 입장이 가능합니다.\r\n\r\n";
      if (this.c.getPlayer().getParty() != null) {
         KoreaCalendar kc = new KoreaCalendar();
         int var10000 = kc.getYeal() % 100;
         String today = var10000 + "/" + kc.getMonths() + "/" + kc.getDays();
         Iterator var5 = this.getPlayer().getParty().getMembers().iterator();

         label297:
         while(var5.hasNext()) {
            MaplePartyCharacter chr = (MaplePartyCharacter)var5.next();
            Iterator var7 = ChannelServer.getAllInstances().iterator();

            while(true) {
               MapleCharacter ch;
               boolean enter;
               boolean weekly;
               char checkclearquestid;
               do {
                  do {
                     do {
                        do {
                           if (!var7.hasNext()) {
                              continue label297;
                           }

                           ChannelServer channel = (ChannelServer)var7.next();
                           ch = channel.getPlayerStorage().getCharacterById(chr.getId());
                        } while(ch == null);
                     } while(ch.isGM());

                     enter = false;
                     weekly = false;
                     String CheckS = "lastDate";
                     int checkenterquestid = 0;
                     checkclearquestid = 0;
                     byte var16 = -1;
                     switch(boss.hashCode()) {
                     case -2143132136:
                        if (boss.equals("Easy_Cygnus")) {
                           var16 = 28;
                        }
                        break;
                     case -2081986703:
                        if (boss.equals("Normal_Pierre")) {
                           var16 = 11;
                        }
                        break;
                     case -2018239135:
                        if (boss.equals("Normal_Populatus")) {
                           var16 = 9;
                        }
                        break;
                     case -1913703009:
                        if (boss.equals("Normal_Vellum")) {
                           var16 = 17;
                        }
                        break;
                     case -1904448764:
                        if (boss.equals("Normal_VonBon")) {
                           var16 = 13;
                        }
                        break;
                     case -1879005130:
                        if (boss.equals("Easy_Magnus")) {
                           var16 = 2;
                        }
                        break;
                     case -659160966:
                        if (boss.equals("Chaos_Populatus")) {
                           var16 = 10;
                        }
                        break;
                     case -655606275:
                        if (boss.equals("Chaos_Zakum")) {
                           var16 = 1;
                        }
                        break;
                     case -472131166:
                        if (boss.equals("Normal_Kawoong")) {
                           var16 = 7;
                        }
                        break;
                     case -435304109:
                        if (boss.equals("Normal_BloodyQueen")) {
                           var16 = 15;
                        }
                        break;
                     case -401177409:
                        if (boss.equals("Normal_JinHillah")) {
                           var16 = 41;
                        }
                        break;
                     case -153131730:
                        if (boss.equals("Normal_Arkarium")) {
                           var16 = 25;
                        }
                        break;
                     case -31241684:
                        if (boss.equals("Chaos_BloodyQueen")) {
                           var16 = 16;
                        }
                        break;
                     case 253322940:
                        if (boss.equals("Easy_Populatus")) {
                           var16 = 8;
                        }
                        break;
                     case 354330098:
                        if (boss.equals("Black_Mage")) {
                           var16 = 44;
                        }
                        break;
                     case 622074967:
                        if (boss.equals("Normal_Lotus")) {
                           var16 = 30;
                        }
                        break;
                     case 622236989:
                        if (boss.equals("Normal_Lucid")) {
                           var16 = 35;
                        }
                        break;
                     case 634456222:
                        if (boss.equals("Hard_Demian")) {
                           var16 = 33;
                        }
                        break;
                     case 634578532:
                        if (boss.equals("Normal_Zakum")) {
                           var16 = 0;
                        }
                        break;
                     case 649264393:
                        if (boss.equals("Hard_Dunkel")) {
                           var16 = 43;
                        }
                        break;
                     case 683009093:
                        if (boss.equals("Chaos_Pinkbean")) {
                           var16 = 27;
                        }
                        break;
                     case 752639996:
                        if (boss.equals("Hard_Hillah")) {
                           var16 = 6;
                        }
                        break;
                     case 773302898:
                        if (boss.equals("Chaos_Horntail")) {
                           var16 = 23;
                        }
                        break;
                     case 871957240:
                        if (boss.equals("Chaos_Pierre")) {
                           var16 = 12;
                        }
                        break;
                     case 888251181:
                        if (boss.equals("Hard_Magnus")) {
                           var16 = 4;
                        }
                        break;
                     case 916262526:
                        if (boss.equals("Normal_Pinkbean")) {
                           var16 = 26;
                        }
                        break;
                     case 1006556331:
                        if (boss.equals("Normal_Horntail")) {
                           var16 = 22;
                        }
                        break;
                     case 1028523123:
                        if (boss.equals("Easy_Arkarium")) {
                           var16 = 24;
                        }
                        break;
                     case 1040240934:
                        if (boss.equals("Chaos_Vellum")) {
                           var16 = 18;
                        }
                        break;
                     case 1049495179:
                        if (boss.equals("Chaos_VonBon")) {
                           var16 = 14;
                        }
                        break;
                     case 1086594120:
                        if (boss.equals("Chaos_Dusk")) {
                           var16 = 40;
                        }
                        break;
                     case 1091918901:
                        if (boss.equals("Normal_VonLeon")) {
                           var16 = 20;
                        }
                        break;
                     case 1310725798:
                        if (boss.equals("Hard_Will")) {
                           var16 = 38;
                        }
                        break;
                     case 1405307649:
                        if (boss.equals("Normal_Dusk")) {
                           var16 = 39;
                        }
                        break;
                     case 1405861930:
                        if (boss.equals("Normal_Will")) {
                           var16 = 37;
                        }
                        break;
                     case 1463075864:
                        if (boss.equals("Easy_Lucid")) {
                           var16 = 34;
                        }
                        break;
                     case 1569825849:
                        if (boss.equals("Hard_VonLeon")) {
                           var16 = 21;
                        }
                        break;
                     case 1684226128:
                        if (boss.equals("Easy_VonLeon")) {
                           var16 = 19;
                        }
                        break;
                     case 1855633811:
                        if (boss.equals("Normal_Cygnus")) {
                           var16 = 29;
                        }
                        break;
                     case 1865965858:
                        if (boss.equals("Normal_Demian")) {
                           var16 = 32;
                        }
                        break;
                     case 1880774029:
                        if (boss.equals("Normal_Dunkel")) {
                           var16 = 42;
                        }
                        break;
                     case 1967822171:
                        if (boss.equals("Hard_Lotus")) {
                           var16 = 31;
                        }
                        break;
                     case 1967984193:
                        if (boss.equals("Hard_Lucid")) {
                           var16 = 36;
                        }
                        break;
                     case 1973986485:
                        if (boss.equals("Hard_Seren")) {
                           var16 = 45;
                        }
                        break;
                     case 1984149632:
                        if (boss.equals("Normal_Hillah")) {
                           var16 = 5;
                        }
                        break;
                     case 2119760817:
                        if (boss.equals("Normal_Magnus")) {
                           var16 = 3;
                        }
                     }

                     switch(var16) {
                     case 0:
                        checkenterquestid = 7003;
                        break;
                     case 1:
                        checkclearquestid = 15166;
                        break;
                     case 2:
                     case 3:
                        checkclearquestid = 3993;
                        break;
                     case 4:
                        checkclearquestid = 3992;
                        break;
                     case 5:
                        checkenterquestid = 3981;
                        break;
                     case 6:
                        checkclearquestid = 3650;
                        break;
                     case 7:
                        checkenterquestid = 3590;
                        break;
                     case 8:
                     case 9:
                        checkenterquestid = 7200;
                        break;
                     case 10:
                        checkclearquestid = 3657;
                        break;
                     case 11:
                        checkclearquestid = 30032;
                        break;
                     case 12:
                        checkclearquestid = 30043;
                        break;
                     case 13:
                        checkclearquestid = 30039;
                        break;
                     case 14:
                        checkclearquestid = 30044;
                        break;
                     case 15:
                        checkclearquestid = 30033;
                        break;
                     case 16:
                        checkclearquestid = 30045;
                        break;
                     case 17:
                        checkclearquestid = 30041;
                        break;
                     case 18:
                        checkclearquestid = 30046;
                        break;
                     case 19:
                     case 20:
                     case 21:
                        checkenterquestid = 7850;
                        break;
                     case 22:
                     case 23:
                        checkenterquestid = 7312;
                        break;
                     case 24:
                     case 25:
                        checkenterquestid = 7851;
                        break;
                     case 26:
                        checkenterquestid = 7403;
                        checkclearquestid = 3652;
                        break;
                     case 27:
                        checkenterquestid = 7403;
                        checkclearquestid = 3653;
                        CheckS = "lastDateC";
                        break;
                     case 28:
                     case 29:
                        checkclearquestid = 31199;
                        break;
                     case 30:
                        checkclearquestid = '舗';
                        break;
                     case 31:
                        checkenterquestid = '腦';
                        checkclearquestid = '舗';
                        break;
                     case 32:
                        checkclearquestid = '蓡';
                        break;
                     case 33:
                        checkenterquestid = '蓠';
                        checkclearquestid = '蓡';
                        break;
                     case 34:
                        checkenterquestid = '蘼';
                        checkclearquestid = 3685;
                        CheckS = "lastDateE";
                        break;
                     case 35:
                        checkenterquestid = '蘼';
                        checkclearquestid = 3685;
                        break;
                     case 36:
                        checkenterquestid = '蘼';
                        checkclearquestid = 3685;
                        CheckS = "lastDateH";
                        break;
                     case 37:
                        checkenterquestid = '褜';
                        checkclearquestid = 3658;
                        CheckS = "lastDateN";
                        break;
                     case 38:
                        checkenterquestid = '褜';
                        checkclearquestid = 3658;
                        break;
                     case 39:
                        checkenterquestid = '襁';
                        checkclearquestid = 3680;
                        CheckS = "lastDateN";
                        break;
                     case 40:
                        checkenterquestid = '襃';
                        checkclearquestid = 3680;
                        CheckS = "lastDateH";
                        break;
                     case 41:
                        checkenterquestid = '覼';
                        checkclearquestid = 3673;
                        weekly = true;
                        break;
                     case 42:
                        checkenterquestid = '襂';
                        checkclearquestid = 3681;
                        CheckS = "lastDateN";
                        break;
                     case 43:
                        checkenterquestid = '襄';
                        checkclearquestid = 3681;
                        CheckS = "lastDateH";
                        break;
                     case 44:
                        checkenterquestid = '許';
                        checkclearquestid = 3679;
                        break;
                     case 45:
                        checkenterquestid = '鯼';
                        checkclearquestid = 3687;
                     }

                     if (checkenterquestid > 0) {
                        if (chr.getPlayer().getKeyValueStr(checkenterquestid, CheckS) != null && chr.getPlayer().getKeyValueStr(checkenterquestid, CheckS).equals(today)) {
                           enter = true;
                           txt = txt + "#b" + chr.getName() + "#k님 입장 가능 횟수 초과 하였습니다.\r\n";
                        }

                        if (!enter) {
                           MapleQuestStatus quests = (MapleQuestStatus)chr.getPlayer().getQuest_Map().get(MapleQuest.getInstance(checkenterquestid));
                           if (quests != null && quests.getCustomData() != null && quests.getCustomData().equals(today)) {
                              enter = true;
                              txt = txt + "#b" + chr.getName() + "#k님 입장 가능 횟수 초과 하였습니다.\r\n";
                           }
                        }
                     }
                  } while(checkclearquestid <= 0);
               } while(enter);

               if (weekly) {
                  if (chr.getPlayer().getKeyValueStr(checkclearquestid, "lasttime") != null) {
                     String[] array = chr.getPlayer().getKeyValueStr(checkclearquestid, "lasttime").split("/");
                     Calendar clear = new GregorianCalendar(Integer.parseInt("20" + array[0]), Integer.parseInt(array[1]) - 1, Integer.parseInt(array[2]));
                     Calendar ocal = Calendar.getInstance();
                     int yeal = clear.get(1);
                     int days = clear.get(5);
                     int day = ocal.get(7);
                     int day2 = clear.get(7);
                     int maxday = clear.getMaximum(5);
                     int month = clear.get(2);
                     int check = day2 == 5 ? 7 : (day2 == 6 ? 6 : (day2 == 7 ? 5 : 0));
                     int afterday;
                     if (check == 0) {
                        for(afterday = day2; afterday < 5; ++afterday) {
                           ++check;
                        }
                     }

                     afterday = days + check;
                     if (afterday > maxday) {
                        afterday -= maxday;
                        ++month;
                     }

                     if (month > 12) {
                        ++yeal;
                        month = 1;
                     }

                     Calendar after = new GregorianCalendar(yeal, month, afterday);
                     if (after.getTimeInMillis() > System.currentTimeMillis()) {
                        enter = true;
                        txt = txt + "#b" + chr.getName() + "#k님 입장 가능 횟수 초과 하였습니다.\r\n";
                     }
                  }
               } else if (chr.getPlayer().getKeyValueStr(checkclearquestid, "lasttime") != null && chr.getPlayer().getKeyValueStr(checkclearquestid, "lasttime").equals(today)) {
                  enter = true;
                  txt = txt + "#b" + chr.getName() + "#k님 입장 가능 횟수 초과 하였습니다.\r\n";
               }

               if (!enter && Long.parseLong(ch.getV(boss)) - System.currentTimeMillis() >= 0L) {
                  enter = true;
                  txt = txt + "#b" + chr.getName() + "#k님 #e#r" + (Long.parseLong(ch.getV(boss)) - System.currentTimeMillis()) / 1000L / 60L + "분" + (Long.parseLong(ch.getV(boss)) - System.currentTimeMillis()) / 1000L % 60L + "초#k#n 뒤에 입장 가능합니다.\r\n";
               }
            }
         }
      }

      return txt;
   }

   public boolean isLevelAvailable(int level) {
      if (this.c.getPlayer().getParty() == null) {
         return false;
      } else {
         Iterator var2 = this.getPlayer().getParty().getMembers().iterator();

         while(var2.hasNext()) {
            MaplePartyCharacter chr = (MaplePartyCharacter)var2.next();
            Iterator var4 = ChannelServer.getAllInstances().iterator();

            while(var4.hasNext()) {
               ChannelServer channel = (ChannelServer)var4.next();
               MapleCharacter ch = channel.getPlayerStorage().getCharacterById(chr.getId());
               if (ch != null && ch.getGMLevel() <= 6 && ch.getLevel() < level) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   public boolean hasSkill(int skillid) {
      Skill theSkill = SkillFactory.getSkill(skillid);
      if (theSkill != null) {
         return this.c.getPlayer().getSkillLevel(theSkill) > 0;
      } else {
         return false;
      }
   }

   public void showEffect(boolean broadcast, String effect) {
      if (broadcast) {
         this.c.getPlayer().getMap().broadcastMessage(CField.showEffect(effect));
      } else {
         this.c.getSession().writeAndFlush(CField.showEffect(effect));
      }

   }

   public void playSound(boolean broadcast, String sound) {
      if (broadcast) {
         this.c.getPlayer().getMap().broadcastMessage(CField.playSound(sound));
      } else {
         this.c.getSession().writeAndFlush(CField.playSound(sound));
      }

   }

   public void environmentChange(boolean broadcast, String env) {
      if (broadcast) {
         this.c.getPlayer().getMap().broadcastMessage(CField.environmentChange(env, 2));
      } else {
         this.c.getSession().writeAndFlush(CField.environmentChange(env, 2));
      }

   }

   public void updateBuddyCapacity(int capacity) {
      this.c.getPlayer().setBuddyCapacity((byte)capacity);
   }

   public int getBuddyCapacity() {
      return this.c.getPlayer().getBuddyCapacity();
   }

   public int partyMembersInMap() {
      int inMap = 0;
      if (this.getPlayer().getParty() == null) {
         return inMap;
      } else {
         Iterator var2 = this.getPlayer().getMap().getCharactersThreadsafe().iterator();

         while(var2.hasNext()) {
            MapleCharacter char2 = (MapleCharacter)var2.next();
            if (char2.getParty() != null && char2.getParty().getId() == this.getPlayer().getParty().getId()) {
               ++inMap;
            }
         }

         return inMap;
      }
   }

   public List<MapleCharacter> getPartyMembers() {
      if (this.getPlayer().getParty() == null) {
         return null;
      } else {
         List<MapleCharacter> chars = new LinkedList();
         Iterator var2 = this.getPlayer().getParty().getMembers().iterator();

         while(var2.hasNext()) {
            MaplePartyCharacter chr = (MaplePartyCharacter)var2.next();
            Iterator var4 = ChannelServer.getAllInstances().iterator();

            while(var4.hasNext()) {
               ChannelServer channel = (ChannelServer)var4.next();
               MapleCharacter ch = channel.getPlayerStorage().getCharacterById(chr.getId());
               if (ch != null) {
                  chars.add(ch);
               }
            }
         }

         return chars;
      }
   }

   public void warpPartyWithExp(int mapId, int exp) {
      if (this.getPlayer().getParty() == null) {
         this.warp(mapId, 0);
         this.gainExp((long)exp);
      } else {
         MapleMap target = this.getMap(mapId);
         Iterator var4 = this.getPlayer().getParty().getMembers().iterator();

         while(true) {
            MapleCharacter curChar;
            do {
               if (!var4.hasNext()) {
                  return;
               }

               MaplePartyCharacter chr = (MaplePartyCharacter)var4.next();
               curChar = this.c.getChannelServer().getPlayerStorage().getCharacterByName(chr.getName());
            } while((curChar.getEventInstance() != null || this.getPlayer().getEventInstance() != null) && curChar.getEventInstance() != this.getPlayer().getEventInstance());

            curChar.changeMap(target, target.getPortal(0));
            curChar.gainExp((long)exp, true, false, true);
         }
      }
   }

   public void warpPartyWithExpMeso(int mapId, int exp, int meso) {
      if (this.getPlayer().getParty() == null) {
         this.warp(mapId, 0);
         this.gainExp((long)exp);
         this.gainMeso((long)meso);
      } else {
         MapleMap target = this.getMap(mapId);
         Iterator var5 = this.getPlayer().getParty().getMembers().iterator();

         while(true) {
            MapleCharacter curChar;
            do {
               if (!var5.hasNext()) {
                  return;
               }

               MaplePartyCharacter chr = (MaplePartyCharacter)var5.next();
               curChar = this.c.getChannelServer().getPlayerStorage().getCharacterByName(chr.getName());
            } while((curChar.getEventInstance() != null || this.getPlayer().getEventInstance() != null) && curChar.getEventInstance() != this.getPlayer().getEventInstance());

            curChar.changeMap(target, target.getPortal(0));
            curChar.gainExp((long)exp, true, false, true);
            curChar.gainMeso((long)meso, true);
         }
      }
   }

   public MapleCharacter getChar(int id) {
      MapleCharacter chr = null;
      Iterator var3 = ChannelServer.getAllInstances().iterator();

      do {
         if (!var3.hasNext()) {
            return null;
         }

         ChannelServer cs = (ChannelServer)var3.next();
         chr = cs.getPlayerStorage().getCharacterById(id);
      } while(chr == null);

      return chr;
   }

   public void makeRing(int itemid, MapleCharacter chr) {
      try {
         MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
         Item item = ii.getEquipById(itemid);
         Item item1 = ii.getEquipById(itemid);
         item.setUniqueId(MapleInventoryIdentifier.getInstance());
         item1.setUniqueId(MapleInventoryIdentifier.getInstance());
         MapleRing.makeRing(itemid, chr, item.getUniqueId(), item1.getUniqueId());
         MapleRing.makeRing(itemid, this.getPlayer(), item1.getUniqueId(), item.getUniqueId());
         MapleInventoryManipulator.addbyItem(this.getClient(), item);
         MapleInventoryManipulator.addbyItem(chr.getClient(), item1);
         chr.reloadChar();
         this.c.getPlayer().reloadChar();
         this.sendOk("선택하신 반지를 제작 완료 하였습니다. 인벤토리를 확인해 봐주시길 바랍니다.");
         chr.dropMessage(5, this.getPlayer().getName() + "님으로 부터 반지가 도착 하였습니다. 인벤토리를 확인해 주시길 바랍니다.");
      } catch (Exception var6) {
         this.sendOk("반지를 제작하는데 오류가 발생 하였습니다.");
      }

   }

   public void makeRingRC(int itemid, MapleCharacter chr) {
      try {
         MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
         Item item = ii.getEquipById(itemid);
         Item item1 = ii.getEquipById(itemid);
         item.setUniqueId(MapleInventoryIdentifier.getInstance());
         Equip eitem = (Equip)item;
         eitem.setStr((short)300);
         eitem.setDex((short)300);
         eitem.setInt((short)300);
         eitem.setLuk((short)300);
         eitem.setWatk((short)300);
         eitem.setMatk((short)300);
         item1.setUniqueId(MapleInventoryIdentifier.getInstance());
         Equip eitem1 = (Equip)item1;
         eitem1.setStr((short)300);
         eitem1.setDex((short)300);
         eitem1.setInt((short)300);
         eitem1.setLuk((short)300);
         eitem1.setWatk((short)300);
         eitem1.setMatk((short)300);
         MapleRing.makeRing(itemid, chr, eitem.getUniqueId(), eitem1.getUniqueId());
         MapleRing.makeRing(itemid, this.getPlayer(), eitem1.getUniqueId(), eitem.getUniqueId());
         MapleInventoryManipulator.addbyItem(this.getClient(), item);
         MapleInventoryManipulator.addbyItem(chr.getClient(), item1);
         chr.reloadChar();
         this.c.getPlayer().reloadChar();
         this.sendOk("선택하신 반지를 제작 완료 하였습니다. 인벤토리를 확인해 봐주시길 바랍니다.");
         chr.dropMessage(5, this.getPlayer().getName() + "님으로 부터 반지가 도착 하였습니다. 인벤토리를 확인해 주시길 바랍니다.");
      } catch (Exception var8) {
         this.sendOk("반지를 제작하는데 오류가 발생 하였습니다.");
      }

   }

   public void makeRingHB(int itemid, MapleCharacter chr) {
      try {
         int asd = 300;
         int asd2 = 300;
         MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
         Item item = ii.getEquipById(itemid);
         Item item1 = ii.getEquipById(itemid);
         item.setUniqueId(MapleInventoryIdentifier.getInstance());
         Equip eitem = (Equip)item;
         eitem.setStr((short)asd);
         eitem.setDex((short)asd);
         eitem.setInt((short)asd);
         eitem.setLuk((short)asd);
         eitem.setWatk((short)asd2);
         eitem.setMatk((short)asd2);
         item1.setUniqueId(MapleInventoryIdentifier.getInstance());
         Equip eitem1 = (Equip)item1;
         eitem1.setStr((short)asd);
         eitem1.setDex((short)asd);
         eitem1.setInt((short)asd);
         eitem1.setLuk((short)asd);
         eitem1.setWatk((short)asd2);
         eitem1.setMatk((short)asd2);
         MapleRing.makeRing(itemid, chr, eitem.getUniqueId(), eitem1.getUniqueId());
         MapleRing.makeRing(itemid, this.getPlayer(), eitem1.getUniqueId(), eitem.getUniqueId());
         MapleInventoryManipulator.addbyItem(this.getClient(), item);
         MapleInventoryManipulator.addbyItem(chr.getClient(), item1);
         chr.reloadChar();
         this.c.getPlayer().reloadChar();
         this.sendOk("선택하신 반지를 제작 완료 하였습니다. 인벤토리를 확인해 봐주시길 바랍니다.");
         chr.dropMessage(5, this.getPlayer().getName() + "님으로 부터 반지가 도착 하였습니다. 인벤토리를 확인해 주시길 바랍니다.");
      } catch (Exception var10) {
         this.sendOk("반지를 제작하는데 오류가 발생 하였습니다.");
      }

   }

   public void MapiaStart(final MapleCharacter player, int time, final int morningmap, final int citizenmap1, final int citizenmap2, final int citizenmap3, final int citizenmap4, final int citizenmap5, final int citizenmap6, final int mapiamap, final int policemap, final int drmap, final int after, final int night, final int vote, int bating) {
      String[] job = new String[]{"시민", "마피아", "경찰", "의사", "시민", "시민", "마피아", "경찰", "시민", "마피아"};
      String name = "";
      final String mapia = "";
      final String police = "";
      final int playernum = 0;
      int citizennumber = 0;
      final MapleMap map = ChannelServer.getInstance(this.getClient().getChannel()).getMapFactory().getMap(morningmap);

      for(Iterator var24 = player.getMap().getCharacters().iterator(); var24.hasNext(); ++playernum) {
         MapleCharacter chr = (MapleCharacter)var24.next();
      }

      int[] iNumber = new int[playernum];

      int i;
      for(i = 1; i <= iNumber.length; iNumber[i - 1] = i++) {
      }

      int jo;
      for(i = 0; i < iNumber.length; ++i) {
         jo = (int)(Math.random() * (double)playernum);
         int t = iNumber[0];
         iNumber[0] = iNumber[jo];
         iNumber[jo] = t;
      }

      for(i = 0; i < iNumber.length; ++i) {
         System.out.print(iNumber[i] + ",");
      }

      jo = 0;
      map.names = "";
      map.mbating = bating * playernum;

      for(Iterator var35 = player.getMap().getCharacters().iterator(); var35.hasNext(); ++jo) {
         MapleCharacter chr = (MapleCharacter)var35.next();
         chr.warp(morningmap);
         String var10001 = map.names;
         map.names = var10001 + chr.getName() + ",";
         chr.mapiajob = job[iNumber[jo] - 1];
         if (chr.mapiajob.equals("마피아")) {
            mapia = mapia + chr.getName() + ",";
         } else if (chr.mapiajob.equals("경찰")) {
            police = police + chr.getName() + ",";
         } else if (chr.mapiajob.equals("시민")) {
            ++citizennumber;
         }

         chr.dropMessage(5, "잠시 후 마피아 게임이 시작됩니다. 총 배팅금은 " + bating * playernum + "메소 입니다.");
         chr.dropMessage(5, "당신의 직업은 " + job[iNumber[jo] - 1] + " 입니다.");
         chr.dropMessage(-1, time + "초 후 마피아 게임이 시작됩니다.");
      }

      final java.util.Timer m_timer = new java.util.Timer();
      TimerTask m_task = new TimerTask() {
         public void run() {
            MapleCharacter chr;
            for(Iterator var1 = player.getMap().getCharacters().iterator(); var1.hasNext(); chr.isVoting = false) {
               chr = (MapleCharacter)var1.next();
               if (chr.mapiajob == "마피아") {
                  chr.isMapiaVote = true;
                  chr.dropMessage(6, "마피아인 당신 동료는 " + mapia + " 들이 있습니다. 밤이되면 같이 의논하여 암살할 사람을 선택해 주시기 바랍니다.");
               } else if (chr.mapiajob == "경찰") {
                  chr.isPoliceVote = true;
                  chr.dropMessage(6, "경찰인 당신 동료는 " + police + " 들이 있습니다. 밤이되면 마피아같다는 사람을 지목하면 마피아인지 아닌지를 알 수 있습니다.");
               } else if (chr.mapiajob == "의사") {
                  chr.isDrVote = true;
                  chr.dropMessage(6, "당신은 하나밖에 없는 의사입니다. 당신에게 부여된 임무는 시민과 경찰을 살리는 것입니다. 밤이되면 마피아가 지목했을것 같은 사람을 선택하면 살리실 수 있습니다.");
               } else if (chr.mapiajob == "시민") {
                  chr.dropMessage(6, "당신은 시민입니다. 낮이되면 대화를 통해 마피아를 찾아내 투표로 처형시키면 됩니다.");
               }

               chr.getmapiavote = 0;
               chr.voteamount = 0;
               chr.getpolicevote = 0;
               chr.isDead = false;
               chr.isDrVote = true;
               chr.isMapiaVote = true;
               chr.isPoliceVote = true;
               chr.getdrvote = 0;
            }

            map.broadcastMessage(CWvsContext.serverNotice(1, "", "진행자>>낮이 되었습니다. 마피아를 찾아내 모두 처형하면 시민의 승리이며, 마피아가 경찰 또는 시민을 모두 죽일시 마피아의 승리입니다.(직업 : 시민,경찰,마피아,의사)"));
            map.playern = playernum;
            map.morningmap = morningmap;
            map.aftertime = after;
            map.nighttime = night;
            map.votetime = vote;
            map.citizenmap1 = citizenmap1;
            map.citizenmap2 = citizenmap2;
            map.citizenmap3 = citizenmap3;
            map.citizenmap4 = citizenmap4;
            map.citizenmap5 = citizenmap5;
            map.citizenmap6 = citizenmap6;
            map.MapiaIng = true;
            map.mapiamap = mapiamap;
            map.policemap = policemap;
            map.drmap = drmap;
            m_timer.cancel();
            map.MapiaMorning(player);
            map.MapiaChannel = player.getClient().getChannel();
         }
      };
      m_timer.schedule(m_task, (long)(time * 1000));
   }

   public void resetReactors() {
      this.getPlayer().getMap().resetReactors(this.c);
   }

   public void genericGuildMessage(int code) {
      this.c.getSession().writeAndFlush(CWvsContext.GuildPacket.genericGuildMessage((byte)code));
   }

   public void disbandGuild() {
      int gid = this.c.getPlayer().getGuildId();
      if (gid > 0 && this.c.getPlayer().getGuildRank() == 1) {
         World.Guild.disbandGuild(gid);
      }
   }

   public void increaseGuildCapacity(boolean trueMax) {
      if (this.c.getPlayer().getMeso() < 500000L && !trueMax) {
         this.c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "500,000 메소가 필요합니다."));
      } else {
         int gid = this.c.getPlayer().getGuildId();
         if (gid > 0) {
            if (World.Guild.increaseGuildCapacity(gid, trueMax)) {
               if (!trueMax) {
                  this.c.getPlayer().gainMeso(-500000L, true, true);
               }

               this.sendNext("증가되었습니다.");
            } else if (!trueMax) {
               this.sendNext("이미 한계치입니다. (Limit: 100)");
            } else {
               this.sendNext("이미 한계치입니다. (Limit: 200)");
            }

         }
      }
   }

   public void displayGuildRanks() {
      this.c.getSession().writeAndFlush(CWvsContext.GuildPacket.guildRankingRequest());
   }

   public boolean removePlayerFromInstance() {
      if (this.c.getPlayer().getEventInstance() != null) {
         this.c.getPlayer().getEventInstance().removePlayer(this.c.getPlayer());
         return true;
      } else {
         return false;
      }
   }

   public boolean isPlayerInstance() {
      return this.c.getPlayer().getEventInstance() != null;
   }

   public void changeStat(byte slot, int type, int amount) {
      Equip sel = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)slot);
      switch(type) {
      case 0:
         sel.setStr((short)amount);
         break;
      case 1:
         sel.setDex((short)amount);
         break;
      case 2:
         sel.setInt((short)amount);
         break;
      case 3:
         sel.setLuk((short)amount);
         break;
      case 4:
         sel.setHp((short)amount);
         break;
      case 5:
         sel.setMp((short)amount);
         break;
      case 6:
         sel.setWatk((short)amount);
         break;
      case 7:
         sel.setMatk((short)amount);
         break;
      case 8:
         sel.setWdef((short)amount);
         break;
      case 9:
         sel.setMdef((short)amount);
         break;
      case 10:
         sel.setAcc((short)amount);
         break;
      case 11:
         sel.setAvoid((short)amount);
         break;
      case 12:
         sel.setHands((short)amount);
         break;
      case 13:
         sel.setSpeed((short)amount);
         break;
      case 14:
         sel.setJump((short)amount);
         break;
      case 15:
         sel.setUpgradeSlots((byte)amount);
         break;
      case 16:
         sel.setViciousHammer((byte)amount);
         break;
      case 17:
         sel.setLevel((byte)amount);
         break;
      case 18:
         sel.setEnhance((byte)amount);
         break;
      case 19:
         sel.setPotential1(amount);
         break;
      case 20:
         sel.setPotential2(amount);
         break;
      case 21:
         sel.setPotential3(amount);
         break;
      case 22:
         sel.setPotential4(amount);
         break;
      case 23:
         sel.setPotential5(amount);
         break;
      case 24:
         sel.setOwner(this.getText());
      }

      this.c.getPlayer().equipChanged();
      this.c.getPlayer().fakeRelog();
   }

   public String searchCashItem(String t) {
      Pattern name2Pattern = Pattern.compile("^[가-힣a-zA-Z0-9]*$");
      if (!name2Pattern.matcher(t).matches()) {
         return "검색할 수 없는 아이템입니다.";
      } else {
         StringBuilder sb = new StringBuilder();
         Iterator var4 = MapleItemInformationProvider.getInstance().getAllEquips().iterator();

         while(var4.hasNext()) {
            Pair<Integer, String> item = (Pair)var4.next();
            if (((String)item.right).contains(t) && MapleItemInformationProvider.getInstance().isCash((Integer)item.left)) {
               sb.append("#b#L" + item.left + "# #i" + item.left + "##t" + item.left + "##l\r\n");
            }
         }

         return sb.toString();
      }
   }

   public void changeDamageSkin(int skinnum) {
      MapleQuest quest = MapleQuest.getInstance(7291);
      MapleQuestStatus queststatus = new MapleQuestStatus(quest, 1);
      String skinString = String.valueOf(skinnum);
      queststatus.setCustomData(skinString == null ? "0" : skinString);
      this.getPlayer().updateQuest(queststatus, true);
      this.getPlayer().dropMessage(5, "데미지 스킨이 변경되었습니다.");
      this.getPlayer().getMap().broadcastMessage(this.getPlayer(), CField.showForeignDamageSkin(this.getPlayer(), skinnum), false);
   }

   public void openDuey() {
      this.c.getPlayer().setConversation(2);
      this.c.getSession().writeAndFlush(CField.sendDuey((byte)9, (List)null, (List)null));
   }

   public void sendUI(int op) {
      this.c.getSession().writeAndFlush(CField.UIPacket.openUI(op));
   }

   public void sendRepairWindow() {
      this.c.getSession().writeAndFlush(CField.UIPacket.openUIOption(33, this.id));
   }

   public void sendNameChangeWindow() {
      this.c.getSession().writeAndFlush(CField.UIPacket.openUIOption(1110, 4034803));
   }

   public void sendProfessionWindow() {
      this.c.getSession().writeAndFlush(CField.UIPacket.openUI(42));
   }

   public final int getDojoPoints() {
      return this.dojo_getPts();
   }

   public final int getDojoRecord() {
      return this.c.getPlayer().getIntNoRecord(150101);
   }

   public void setDojoRecord(boolean reset) {
      if (reset) {
         this.c.getPlayer().getQuestNAdd(MapleQuest.getInstance(150101)).setCustomData("0");
         this.c.getPlayer().getQuestNAdd(MapleQuest.getInstance(150100)).setCustomData("0");
      } else {
         this.c.getPlayer().getQuestNAdd(MapleQuest.getInstance(150101)).setCustomData(String.valueOf(this.c.getPlayer().getIntRecord(150101) + 1));
      }

   }

   public boolean start_DojoAgent(boolean dojo, boolean party) {
      return dojo ? Event_DojoAgent.warpStartDojo(this.c.getPlayer(), party) : Event_DojoAgent.warpStartAgent(this.c.getPlayer(), party);
   }

   public final short getKegs() {
      return this.c.getChannelServer().getFireWorks().getKegsPercentage();
   }

   public void giveKegs(int kegs) {
      this.c.getChannelServer().getFireWorks().giveKegs(this.c.getPlayer(), kegs);
   }

   public final short getSunshines() {
      return this.c.getChannelServer().getFireWorks().getSunsPercentage();
   }

   public void addSunshines(int kegs) {
      this.c.getChannelServer().getFireWorks().giveSuns(this.c.getPlayer(), kegs);
   }

   public final short getDecorations() {
      return this.c.getChannelServer().getFireWorks().getDecsPercentage();
   }

   public void addDecorations(int kegs) {
      try {
         this.c.getChannelServer().getFireWorks().giveDecs(this.c.getPlayer(), kegs);
      } catch (Exception var3) {
         var3.printStackTrace();
      }

   }

   public void maxStats() {
      Map<MapleStat, Long> statup = new EnumMap(MapleStat.class);
      this.c.getPlayer().getStat().str = 32767;
      this.c.getPlayer().getStat().dex = 32767;
      this.c.getPlayer().getStat().int_ = 32767;
      this.c.getPlayer().getStat().luk = 32767;
      int overrDemon = GameConstants.isDemonSlayer(this.c.getPlayer().getJob()) ? GameConstants.getMPByJob(this.c.getPlayer()) : 500000;
      this.c.getPlayer().getStat().maxhp = 500000L;
      this.c.getPlayer().getStat().maxmp = (long)overrDemon;
      this.c.getPlayer().getStat().setHp(500000L, this.c.getPlayer());
      this.c.getPlayer().getStat().setMp((long)overrDemon, this.c.getPlayer());
      statup.put(MapleStat.STR, 32767L);
      statup.put(MapleStat.DEX, 32767L);
      statup.put(MapleStat.LUK, 32767L);
      statup.put(MapleStat.INT, 32767L);
      statup.put(MapleStat.HP, 500000L);
      statup.put(MapleStat.MAXHP, 500000L);
      statup.put(MapleStat.MP, (long)overrDemon);
      statup.put(MapleStat.MAXMP, (long)overrDemon);
      this.c.getPlayer().getStat().recalcLocalStats(this.c.getPlayer());
      this.c.getSession().writeAndFlush(CWvsContext.updatePlayerStats(statup, this.c.getPlayer()));
   }

   public boolean getSR(Triple<String, Map<Integer, String>, Long> ma, int sel) {
      if (((Map)ma.mid).get(sel) != null && ((String)((Map)ma.mid).get(sel)).length() > 0) {
         this.sendOk((String)((Map)ma.mid).get(sel));
         return true;
      } else {
         this.dispose();
         return false;
      }
   }

   public String getAllItem() {
      StringBuilder string = new StringBuilder();
      Iterator var2 = this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).list().iterator();

      while(var2.hasNext()) {
         Item item = (Item)var2.next();
         long var10001 = item.getUniqueId();
         string.append("#L" + var10001 + "##i " + item.getItemId() + "#\r\n");
      }

      return string.toString();
   }

   public Equip getEquip(int itemid) {
      return (Equip)MapleItemInformationProvider.getInstance().getEquipById(itemid);
   }

   public void setExpiration(Object statsSel, long expire) {
      if (statsSel instanceof Equip) {
         ((Equip)statsSel).setExpiration(System.currentTimeMillis() + expire * 24L * 60L * 60L * 1000L);
      }

   }

   public void setLock(Object statsSel) {
      if (statsSel instanceof Equip) {
         Equip eq = (Equip)statsSel;
         if (eq.getExpiration() == -1L) {
            eq.setFlag(eq.getFlag() | ItemFlag.LOCK.getValue());
         } else {
            eq.setFlag(eq.getFlag() | ItemFlag.UNTRADEABLE.getValue());
         }
      }

   }

   public boolean addFromDrop(Object statsSel) {
      if (!(statsSel instanceof Item)) {
         return false;
      } else {
         Item it = (Item)statsSel;
         return MapleInventoryManipulator.checkSpace(this.getClient(), it.getItemId(), it.getQuantity(), it.getOwner()) && MapleInventoryManipulator.addFromDrop(this.getClient(), it, false);
      }
   }

   public boolean replaceItem(int slot, int invType, Object statsSel, int offset, String type) {
      return this.replaceItem(slot, invType, statsSel, offset, type, false);
   }

   public boolean replaceItem(int slot, int invType, Object statsSel, int offset, String type, boolean takeSlot) {
      MapleInventoryType inv = MapleInventoryType.getByType((byte)invType);
      if (inv == null) {
         return false;
      } else {
         Item item = this.getPlayer().getInventory(inv).getItem((short)slot);
         if (item == null || statsSel instanceof Item) {
            item = (Item)statsSel;
         }

         if (offset > 0) {
            if (inv != MapleInventoryType.EQUIP) {
               return false;
            }

            Equip eq = (Equip)item;
            if (takeSlot) {
               if (eq.getUpgradeSlots() < 1) {
                  return false;
               }

               eq.setUpgradeSlots((byte)(eq.getUpgradeSlots() - 1));
               if (eq.getExpiration() == -1L) {
                  eq.setFlag(eq.getFlag() | ItemFlag.LOCK.getValue());
               } else {
                  eq.setFlag(eq.getFlag() | ItemFlag.UNTRADEABLE.getValue());
               }
            }

            if (type.equalsIgnoreCase("Slots")) {
               eq.setUpgradeSlots((byte)(eq.getUpgradeSlots() + offset));
               eq.setViciousHammer((byte)(eq.getViciousHammer() + offset));
            } else if (type.equalsIgnoreCase("Level")) {
               eq.setLevel((byte)(eq.getLevel() + offset));
            } else if (type.equalsIgnoreCase("Hammer")) {
               eq.setViciousHammer((byte)(eq.getViciousHammer() + offset));
            } else if (type.equalsIgnoreCase("STR")) {
               eq.setStr((short)(eq.getStr() + offset));
            } else if (type.equalsIgnoreCase("DEX")) {
               eq.setDex((short)(eq.getDex() + offset));
            } else if (type.equalsIgnoreCase("INT")) {
               eq.setInt((short)(eq.getInt() + offset));
            } else if (type.equalsIgnoreCase("LUK")) {
               eq.setLuk((short)(eq.getLuk() + offset));
            } else if (type.equalsIgnoreCase("HP")) {
               eq.setHp((short)(eq.getHp() + offset));
            } else if (type.equalsIgnoreCase("MP")) {
               eq.setMp((short)(eq.getMp() + offset));
            } else if (type.equalsIgnoreCase("WATK")) {
               eq.setWatk((short)(eq.getWatk() + offset));
            } else if (type.equalsIgnoreCase("MATK")) {
               eq.setMatk((short)(eq.getMatk() + offset));
            } else if (type.equalsIgnoreCase("WDEF")) {
               eq.setWdef((short)(eq.getWdef() + offset));
            } else if (type.equalsIgnoreCase("MDEF")) {
               eq.setMdef((short)(eq.getMdef() + offset));
            } else if (type.equalsIgnoreCase("ACC")) {
               eq.setAcc((short)(eq.getAcc() + offset));
            } else if (type.equalsIgnoreCase("Avoid")) {
               eq.setAvoid((short)(eq.getAvoid() + offset));
            } else if (type.equalsIgnoreCase("Hands")) {
               eq.setHands((short)(eq.getHands() + offset));
            } else if (type.equalsIgnoreCase("Speed")) {
               eq.setSpeed((short)(eq.getSpeed() + offset));
            } else if (type.equalsIgnoreCase("Jump")) {
               eq.setJump((short)(eq.getJump() + offset));
            } else if (type.equalsIgnoreCase("ItemEXP")) {
               eq.setItemEXP(eq.getItemEXP() + offset);
            } else if (type.equalsIgnoreCase("Expiration")) {
               eq.setExpiration(eq.getExpiration() + (long)offset);
            } else if (type.equalsIgnoreCase("Flag")) {
               eq.setFlag(eq.getFlag() + offset);
            }

            item = eq.copy();
         }

         MapleInventoryManipulator.removeFromSlot(this.getClient(), inv, (short)slot, item.getQuantity(), false);
         return MapleInventoryManipulator.addFromDrop(this.getClient(), item, false);
      }
   }

   public boolean replaceItem(int slot, int invType, Object statsSel, int upgradeSlots) {
      return this.replaceItem(slot, invType, statsSel, upgradeSlots, "Slots");
   }

   public boolean isCash(int itemId) {
      return MapleItemInformationProvider.getInstance().isCash(itemId);
   }

   public int getTotalStat(int itemId) {
      return MapleItemInformationProvider.getInstance().getTotalStat((Equip)MapleItemInformationProvider.getInstance().getEquipById(itemId));
   }

   public int getReqLevel(int itemId) {
      return MapleItemInformationProvider.getInstance().getReqLevel(itemId);
   }

   public SecondaryStatEffect getEffect(int buff) {
      return MapleItemInformationProvider.getInstance().getItemEffect(buff);
   }

   public void giveBuff(int skillid) {
      SkillFactory.getSkill(skillid).getEffect(1).applyTo(this.c.getPlayer());
   }

   public void buffGuild(int buff, int duration, String msg) {
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      if (ii.getItemEffect(buff) != null && this.getPlayer().getGuildId() > 0) {
         SecondaryStatEffect mse = ii.getItemEffect(buff);
         Iterator var6 = ChannelServer.getAllInstances().iterator();

         while(var6.hasNext()) {
            ChannelServer cserv = (ChannelServer)var6.next();
            Iterator var8 = cserv.getPlayerStorage().getAllCharacters().values().iterator();

            while(var8.hasNext()) {
               MapleCharacter chr = (MapleCharacter)var8.next();
               if (chr.getGuildId() == this.getPlayer().getGuildId()) {
                  mse.applyTo(chr, chr, true, chr.getTruePosition(), duration, (byte)0, true);
                  chr.dropMessage(5, "Your guild has gotten a " + msg + " buff.");
               }
            }
         }
      }

   }

   public long getRemainPremium(int accid) {
      Connection con = null;
      ResultSet rs = null;
      PreparedStatement ps = null;
      long ret = 0L;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM premium WHERE accid = ?");
         ps.setInt(1, accid);
         rs = ps.executeQuery();
         if (rs.next()) {
            ret = rs.getLong("period");
         }

         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var24) {
         var24.printStackTrace();
      } finally {
         if (rs != null) {
            try {
               rs.close();
            } catch (Exception var23) {
            }
         }

         if (ps != null) {
            try {
               ps.close();
            } catch (Exception var22) {
            }
         }

         if (con != null) {
            try {
               con.close();
            } catch (Exception var21) {
            }
         }

      }

      return ret;
   }

   public boolean existPremium(int aci) {
      Connection con = null;
      ResultSet rs = null;
      PreparedStatement ps = null;
      boolean ret = false;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM premium WHERE accid = ?");
         ps.setInt(1, aci);
         rs = ps.executeQuery();
         ret = rs.next();
         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var23) {
         var23.printStackTrace();
      } finally {
         if (rs != null) {
            try {
               rs.close();
            } catch (Exception var22) {
            }
         }

         if (ps != null) {
            try {
               ps.close();
            } catch (Exception var21) {
            }
         }

         if (con != null) {
            try {
               con.close();
            } catch (Exception var20) {
            }
         }

      }

      return ret;
   }

   public void gainAllAccountPremium(int v3, int v4) {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      ArrayList<Integer> chrs = new ArrayList();
      Date adate = new Date();

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM accounts");
         rs = ps.executeQuery();

         while(rs.next()) {
            chrs.add(rs.getInt("id"));
         }

         rs.close();
         ps.close();

         for(int i = 0; i < chrs.size(); ++i) {
            if (this.existPremium((Integer)chrs.get(i))) {
               if (this.getRemainPremium((Integer)chrs.get(i)) > adate.getTime()) {
                  ps = con.prepareStatement("UPDATE premium SET period = ? WHERE accid = ?");
                  ps.setLong(1, this.getRemainPremium((Integer)chrs.get(i)) + (long)(v3 * 24 * 60 * 60 * 1000));
                  ps.setInt(2, (Integer)chrs.get(i));
                  ps.executeUpdate();
                  ps.close();
               } else {
                  ps = con.prepareStatement("UPDATE premium SET period = ? and `name` = ? and `buff` = ? WHERE accid = ?");
                  ps.setLong(1, adate.getTime() + (long)(v3 * 24 * 60 * 60 * 1000));
                  ps.setString(2, "일반");
                  ps.setInt(3, 80001535);
                  ps.setInt(4, (Integer)chrs.get(i));
                  ps.executeUpdate();
                  ps.close();
               }
            } else {
               ps = con.prepareStatement("INSERT INTO premium(accid, name, buff, period) VALUES (?, ?, ?, ?)");
               ps.setInt(1, (Integer)chrs.get(i));
               ps.setString(2, "일반");
               ps.setInt(3, 80001535);
               ps.setLong(4, adate.getTime() + (long)(v3 * 24 * 60 * 60 * 1000));
               ps.executeUpdate();
               ps.close();
            }
         }

         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var25) {
         var25.printStackTrace();
      } finally {
         if (rs != null) {
            try {
               rs.close();
            } catch (Exception var24) {
            }
         }

         if (ps != null) {
            try {
               ps.close();
            } catch (Exception var23) {
            }
         }

         if (con != null) {
            try {
               con.close();
            } catch (Exception var22) {
            }
         }

      }

   }

   public void gainAccountPremium(String acc, int v3, boolean v4) {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Date adate = new Date();
      int accid = 0;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM accounts WHERE name = ?");
         ps.setString(1, acc);
         rs = ps.executeQuery();
         if (rs.next()) {
            accid = rs.getInt("id");
         }

         rs.close();
         ps.close();
         if (this.existPremium(accid)) {
            if (this.getRemainPremium(accid) > adate.getTime()) {
               ps = con.prepareStatement("UPDATE premium SET period = ? WHERE accid = ?");
               if (v4) {
                  ps.setLong(1, this.getRemainPremium(accid) + (long)(v3 * 24 * 60 * 60 * 1000));
               } else {
                  ps.setLong(1, this.getRemainPremium(accid) - (long)(v3 * 24 * 60 * 60 * 1000));
               }

               ps.setInt(2, accid);
               ps.executeUpdate();
               ps.close();
            } else if (v4) {
               ps = con.prepareStatement("UPDATE premium SET period = ? and `name` = ? and `buff` = ? WHERE accid = ?");
               ps.setLong(1, adate.getTime() + (long)(v3 * 24 * 60 * 60 * 1000));
               ps.setString(2, "일반");
               ps.setInt(3, 80001535);
               ps.setInt(4, accid);
               ps.executeUpdate();
               ps.close();
            }
         } else if (v4) {
            ps = con.prepareStatement("INSERT INTO premium(accid, name, buff, period) VALUES (?, ?, ?, ?)");
            ps.setInt(1, accid);
            ps.setString(2, "일반");
            ps.setInt(3, 80001535);
            ps.setLong(4, adate.getTime() + (long)(v3 * 24 * 60 * 60 * 1000));
            ps.executeUpdate();
            ps.close();
         }

         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var26) {
         var26.printStackTrace();
      } finally {
         if (rs != null) {
            try {
               rs.close();
            } catch (Exception var25) {
            }
         }

         if (ps != null) {
            try {
               ps.close();
            } catch (Exception var24) {
            }
         }

         if (con != null) {
            try {
               con.close();
            } catch (Exception var23) {
            }
         }

      }

   }

   public boolean createAlliance(String alliancename) {
      MapleParty pt = this.c.getPlayer().getParty();
      MapleCharacter otherChar = this.c.getChannelServer().getPlayerStorage().getCharacterById(pt.getMemberByIndex(1).getId());
      if (otherChar != null && otherChar.getId() != this.c.getPlayer().getId()) {
         try {
            return World.Alliance.createAlliance(alliancename, this.c.getPlayer().getId(), otherChar.getId(), this.c.getPlayer().getGuildId(), otherChar.getGuildId());
         } catch (Exception var5) {
            var5.printStackTrace();
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean addCapacityToAlliance() {
      try {
         MapleGuild gs = World.Guild.getGuild(this.c.getPlayer().getGuildId());
         if (gs != null && this.c.getPlayer().getGuildRank() == 1 && this.c.getPlayer().getAllianceRank() == 1 && World.Alliance.getAllianceLeader(gs.getAllianceId()) == this.c.getPlayer().getId() && World.Alliance.changeAllianceCapacity(gs.getAllianceId())) {
            this.gainMeso(-10000000L);
            return true;
         }
      } catch (Exception var2) {
         var2.printStackTrace();
      }

      return false;
   }

   public boolean disbandAlliance() {
      try {
         MapleGuild gs = World.Guild.getGuild(this.c.getPlayer().getGuildId());
         if (gs != null && this.c.getPlayer().getGuildRank() == 1 && this.c.getPlayer().getAllianceRank() == 1 && World.Alliance.getAllianceLeader(gs.getAllianceId()) == this.c.getPlayer().getId() && World.Alliance.disbandAlliance(gs.getAllianceId())) {
            return true;
         }
      } catch (Exception var2) {
         var2.printStackTrace();
      }

      return false;
   }

   public byte getLastMsg() {
      return this.lastMsg;
   }

   public final void setLastMsg(byte last) {
      this.lastMsg = last;
   }

   public final void maxAllSkills() {
      HashMap<Skill, SkillEntry> sa = new HashMap();
      Iterator var2 = SkillFactory.getAllSkills().iterator();

      while(var2.hasNext()) {
         Skill skil = (Skill)var2.next();
         if (GameConstants.isApplicableSkill(skil.getId()) && skil.getId() < 90000000) {
            sa.put(skil, new SkillEntry((byte)skil.getMaxLevel(), (byte)skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
         }
      }

      this.getPlayer().changeSkillsLevel(sa);
   }

   public final void resetStats(int str, int dex, int z, int luk) {
      this.c.getPlayer().resetStats(str, dex, z, luk);
   }

   public final boolean dropItem(int slot, int invType, int quantity) {
      MapleInventoryType inv = MapleInventoryType.getByType((byte)invType);
      return inv == null ? false : MapleInventoryManipulator.drop(this.c, inv, (short)slot, (short)quantity, true);
   }

   public final void setQuestRecord(Object ch, int questid, String data) {
      ((MapleCharacter)ch).getQuestNAdd(MapleQuest.getInstance(questid)).setCustomData(data);
   }

   public final void doWeddingEffect(Object ch) {
      final MapleCharacter chr = (MapleCharacter)ch;
      final MapleCharacter player = this.getPlayer();
      MapleMap var10000 = this.getMap();
      String var10001 = player.getName();
      var10000.broadcastMessage(CWvsContext.yellowChat(var10001 + ", do you take " + chr.getName() + " as your wife and promise to stay beside her through all downtimes, crashes, and lags?"));
      Timer.CloneTimer.getInstance().schedule(new Runnable() {
         public void run() {
            if (chr != null && player != null) {
               MapleMap var10000 = chr.getMap();
               String var10001 = chr.getName();
               var10000.broadcastMessage(CWvsContext.yellowChat(var10001 + ", do you take " + player.getName() + " as your husband and promise to stay beside him through all downtimes, crashes, and lags?"));
            } else {
               NPCConversationManager.this.warpMap(680000500, 0);
            }

         }
      }, 10000L);
      Timer.CloneTimer.getInstance().schedule(new Runnable() {
         public void run() {
            if (chr != null && player != null) {
               NPCConversationManager.this.setQuestRecord(player, 160001, "2");
               NPCConversationManager.this.setQuestRecord(chr, 160001, "2");
               NPCConversationManager var10000 = NPCConversationManager.this;
               String var10001 = player.getName();
               var10000.sendNPCText(var10001 + " and " + chr.getName() + ", I wish you two all the best on your " + chr.getClient().getChannelServer().getServerName() + " journey together!", 9201002);
               chr.getMap().startExtendedMapEffect("You may now kiss the bride, " + player.getName() + "!", 5120006);
               if (chr.getGuildId() > 0) {
                  World.Guild.guildPacket(chr.getGuildId(), CWvsContext.sendMarriage(false, chr.getName()));
               }

               if (player.getGuildId() > 0) {
                  World.Guild.guildPacket(player.getGuildId(), CWvsContext.sendMarriage(false, player.getName()));
               }
            } else {
               if (player != null) {
                  NPCConversationManager.this.setQuestRecord(player, 160001, "3");
                  NPCConversationManager.this.setQuestRecord(player, 160002, "0");
               } else if (chr != null) {
                  NPCConversationManager.this.setQuestRecord(chr, 160001, "3");
                  NPCConversationManager.this.setQuestRecord(chr, 160002, "0");
               }

               NPCConversationManager.this.warpMap(680000500, 0);
            }

         }
      }, 20000L);
   }

   public void putKey(int key, int type, int action) {
      this.getPlayer().changeKeybinding(key, (byte)type, action);
      this.getClient().getSession().writeAndFlush(CField.getKeymap(this.getPlayer().getKeyLayout()));
   }

   public void logDonator(String log, int previous_points) {
      StringBuilder logg = new StringBuilder();
      logg.append(MapleCharacterUtil.makeMapleReadable(this.getPlayer().getName()));
      logg.append(" [CID: ").append(this.getPlayer().getId()).append("] ");
      logg.append(" [Account: ").append(MapleCharacterUtil.makeMapleReadable(this.getClient().getAccountName())).append("] ");
      logg.append(log);
      logg.append(" [Previous: " + previous_points + "] [Now: " + this.getPlayer().getPoints() + "]");
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("INSERT INTO donorlog VALUES(DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?)");
         ps.setString(1, MapleCharacterUtil.makeMapleReadable(this.getClient().getAccountName()));
         ps.setInt(2, this.getClient().getAccID());
         ps.setString(3, MapleCharacterUtil.makeMapleReadable(this.getPlayer().getName()));
         ps.setInt(4, this.getPlayer().getId());
         ps.setString(5, log);
         ps.setString(6, FileoutputUtil.CurrentReadable_Time());
         ps.setInt(7, previous_points);
         ps.setInt(8, this.getPlayer().getPoints());
         ps.executeUpdate();
         ps.close();
         con.close();
      } catch (SQLException var16) {
         var16.printStackTrace();
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var15) {
            var15.printStackTrace();
         }

      }

      FileoutputUtil.log("Log_Donator.rtf", logg.toString());
   }

   public void doRing(String name, int itemid) {
      PlayersHandler.DoRing(this.getClient(), name, itemid);
   }

   public int getNaturalStats(int itemid, String it) {
      Map<String, Integer> eqStats = MapleItemInformationProvider.getInstance().getEquipStats(itemid);
      return eqStats != null && eqStats.containsKey(it) ? (Integer)eqStats.get(it) : 0;
   }

   public boolean isEligibleName(String t) {
      return MapleCharacterUtil.canCreateChar(t, this.getPlayer().isGM()) && (!LoginInformationProvider.getInstance().isForbiddenName(t) || this.getPlayer().isGM());
   }

   public String checkDrop(int mobId) {
      List<MonsterDropEntry> ranks = MapleMonsterInformationProvider.getInstance().retrieveDrop(mobId);
      if (ranks != null && ranks.size() > 0) {
         int num = 0;
         int itemId = false;
         int ch = false;
         StringBuilder name = new StringBuilder();

         for(int i = 0; i < ranks.size(); ++i) {
            MonsterDropEntry de = (MonsterDropEntry)ranks.get(i);
            if (de.chance > 0 && (de.questid <= 0 || de.questid > 0 && MapleQuest.getInstance(de.questid).getName().length() > 0)) {
               int itemId = de.itemId;
               if (num == 0) {
                  name.append("Drops for #o" + mobId + "#\r\n");
                  name.append("--------------------------------------\r\n");
               }

               String namez = "#z" + itemId + "#";
               if (itemId == 0) {
                  itemId = 4031041;
                  namez = de.Minimum * this.getClient().getChannelServer().getMesoRate() + " to " + de.Maximum * this.getClient().getChannelServer().getMesoRate() + " meso";
               }

               int ch = de.chance * this.getClient().getChannelServer().getDropRate();
               name.append(num + 1 + ") #v" + itemId + "#" + namez + " - " + Integer.valueOf(ch >= 999999 ? 1000000 : ch).doubleValue() / 10000.0D + "% chance. " + (de.questid > 0 && MapleQuest.getInstance(de.questid).getName().length() > 0 ? "Requires quest " + MapleQuest.getInstance(de.questid).getName() + " to be started." : "") + "\r\n");
               ++num;
            }
         }

         if (name.length() > 0) {
            return name.toString();
         }
      }

      return "No drops was returned.";
   }

   public String getLeftPadded(String in, char padchar, int length) {
      return StringUtil.getLeftPaddedStr(in, padchar, length);
   }

   public void handleDivorce() {
      if (this.getPlayer().getMarriageId() <= 0) {
         this.sendNext("Please make sure you have a marriage.");
      } else {
         int chz = World.Find.findChannel(this.getPlayer().getMarriageId());
         if (chz == -1) {
            Connection con = null;
            PreparedStatement ps = null;
            Object rs = null;

            label138: {
               try {
                  con = DatabaseConnection.getConnection();
                  ps = con.prepareStatement("UPDATE queststatus SET customData = ? WHERE characterid = ? AND (quest = ? OR quest = ?)");
                  ps.setString(1, "0");
                  ps.setInt(2, this.getPlayer().getMarriageId());
                  ps.setInt(3, 160001);
                  ps.setInt(4, 160002);
                  ps.executeUpdate();
                  ps.close();
                  ps = con.prepareStatement("UPDATE characters SET marriageid = ? WHERE id = ?");
                  ps.setInt(1, 0);
                  ps.setInt(2, this.getPlayer().getMarriageId());
                  ps.executeUpdate();
                  ps.close();
                  con.close();
                  break label138;
               } catch (SQLException var15) {
                  this.outputFileError(var15);
               } finally {
                  try {
                     if (con != null) {
                        con.close();
                     }

                     if (ps != null) {
                        ps.close();
                     }

                     if (rs != null) {
                        ((ResultSet)rs).close();
                     }
                  } catch (SQLException var14) {
                     var14.printStackTrace();
                  }

               }

               return;
            }

            this.setQuestRecord(this.getPlayer(), 160001, "0");
            this.setQuestRecord(this.getPlayer(), 160002, "0");
            this.getPlayer().setMarriageId(0);
            this.sendNext("You have been successfully divorced...");
         } else if (chz < -1) {
            this.sendNext("Please make sure your partner is logged on.");
         } else {
            MapleCharacter cPlayer = ChannelServer.getInstance(chz).getPlayerStorage().getCharacterById(this.getPlayer().getMarriageId());
            if (cPlayer != null) {
               cPlayer.dropMessage(1, "Your partner has divorced you.");
               cPlayer.setMarriageId(0);
               this.setQuestRecord(cPlayer, 160001, "0");
               this.setQuestRecord(this.getPlayer(), 160001, "0");
               this.setQuestRecord(cPlayer, 160002, "0");
               this.setQuestRecord(this.getPlayer(), 160002, "0");
               this.getPlayer().setMarriageId(0);
               this.sendNext("You have been successfully divorced...");
            } else {
               this.sendNext("An error occurred...");
            }

         }
      }
   }

   public String getReadableMillis(long startMillis, long endMillis) {
      return StringUtil.getReadableMillis(startMillis, endMillis);
   }

   public void sendUltimateExplorer() {
      this.getClient().getSession().writeAndFlush(CWvsContext.ultimateExplorer());
   }

   public void sendPendant(boolean b) {
      this.c.getSession().writeAndFlush(CWvsContext.pendantSlot(b));
   }

   public int getCompensation(String id) {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      int var5;
      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM compensationlog_confirmed WHERE chrname = ?");
         ps.setString(1, id);
         rs = ps.executeQuery();
         if (!rs.next()) {
            rs.close();
            ps.close();
            con.close();
            return 0;
         }

         var5 = rs.getInt("value");
      } catch (SQLException var16) {
         FileoutputUtil.outputFileError("Log_Script_Except.rtf", var16);
         return 0;
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var15) {
            var15.printStackTrace();
         }

      }

      return var5;
   }

   public boolean deleteCompensation(String id) {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      boolean var6;
      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("DELETE FROM compensationlog_confirmed WHERE chrname = ?");
         ps.setString(1, id);
         ps.executeUpdate();
         ps.close();
         con.close();
         boolean var5 = true;
         return var5;
      } catch (SQLException var16) {
         FileoutputUtil.outputFileError("Log_Script_Except.rtf", var16);
         var6 = false;
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var15) {
            var15.printStackTrace();
         }

      }

      return var6;
   }

   public void gainAPS(int gain) {
      this.getPlayer().gainAPS(gain);
   }

   public void forceCompleteQuest(MapleCharacter chr, int idd) {
      MapleQuest.getInstance(idd).forceComplete(chr, this.getNpc());
   }

   public void setInnerStats(MapleCharacter chr, int line) {
      InnerSkillValueHolder isvh = InnerAbillity.getInstance().renewSkill(0, false);
      chr.getInnerSkills().add(isvh);
      chr.changeSkillLevel(SkillFactory.getSkill(isvh.getSkillId()), isvh.getSkillLevel(), isvh.getSkillLevel());
      chr.getClient().getSession().writeAndFlush(CField.updateInnerPotential((byte)line, isvh.getSkillId(), isvh.getSkillLevel(), isvh.getRank()));
   }

   public void setInnerStats(int line) {
      InnerSkillValueHolder isvh = InnerAbillity.getInstance().renewSkill(0, false);
      this.c.getPlayer().getInnerSkills().add(isvh);
      this.c.getPlayer().changeSkillLevel(SkillFactory.getSkill(isvh.getSkillId()), isvh.getSkillLevel(), isvh.getSkillLevel());
      this.c.getSession().writeAndFlush(CField.updateInnerPotential((byte)line, isvh.getSkillId(), isvh.getSkillLevel(), isvh.getRank()));
   }

   public void openAuctionUI() {
      this.c.getSession().writeAndFlush(CField.UIPacket.openUI(161));
   }

   public void gainSponserItem(int item, String name, short allstat, short damage, byte upgradeslot) {
      if (GameConstants.isEquip(item)) {
         Equip Item = (Equip)MapleItemInformationProvider.getInstance().getEquipById(item);
         Item.setOwner(name);
         Item.setStr(allstat);
         Item.setDex(allstat);
         Item.setInt(allstat);
         Item.setLuk(allstat);
         Item.setWatk(damage);
         Item.setMatk(damage);
         Item.setUpgradeSlots(upgradeslot);
         MapleInventoryManipulator.addFromDrop(this.c, Item, false);
      } else {
         this.gainItem(item, allstat, (long)damage);
      }

   }

   public void askAvatar(String text, List<Integer> args) {
      this.c.getSession().writeAndFlush(CField.NPCPacket.getNPCTalkStyle(this.id, text, args));
      this.lastMsg = 9;
   }

   public void SearchItem(String text, int type) {
      NPCConversationManager cm = this;
      if (text.getBytes().length < 4) {
         this.sendOk("검색어는 두글자 이상으로 해주세요.");
         this.dispose();
      } else if (!text.contains("헤어") && !text.contains("얼굴")) {
         String kk = "";
         String chat = "";
         String nchat = "";
         MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
         int i = 0;
         Iterator var9 = ii.getAllEquips().iterator();

         while(true) {
            while(true) {
               Pair item;
               do {
                  if (!var9.hasNext()) {
                     if (i != 0) {
                        kk = kk + "총 " + i + "개 검색되었습니다. 추가 하실 항목을 선택해주세요.";
                        kk = kk + "\r\n#L0#항목 선택을 마칩니다.  \r\n#L1#항목을 재검색합니다.";
                        nchat = kk + chat;
                        cm.sendSimple(nchat);
                     } else {
                        kk = kk + "검색된 아이템이 없습니다.";
                        cm.sendOk(kk);
                        cm.dispose();
                     }

                     return;
                  }

                  item = (Pair)var9.next();
               } while(!((String)item.getRight()).toLowerCase().contains(text.toLowerCase()));

               String color = "#b";
               String isuse = "";
               if (cm.getPlayer().getCashWishList().contains(item.getLeft())) {
                  color = "#Cgray#";
                  isuse = " (선택된 항목)";
               }

               if (type == 1 && ii.isCash((Integer)item.getLeft()) && (Integer)item.getLeft() >= 1000000 && (Integer)item.getLeft() / 1000000 == 1) {
                  chat = chat + "\r\n" + color + "#L" + item.getLeft() + "##i" + item.getLeft() + " ##z" + item.getLeft() + "#" + isuse;
                  ++i;
               } else if (type == 0 && (Integer)item.getLeft() / 10000 >= 2 && (Integer)item.getLeft() / 10000 < 3) {
                  chat = chat + "\r\n" + color + "#L" + item.getLeft() + "##i" + item.getLeft() + " ##z" + item.getLeft() + "#" + isuse;
                  ++i;
               } else if (type == 2 && (Integer)item.getLeft() / 10000 >= 3 && (Integer)item.getLeft() / 10000 <= 5) {
                  chat = chat + "\r\n" + color + "#L" + item.getLeft() + "##i" + item.getLeft() + " ##z" + item.getLeft() + "#" + isuse;
                  ++i;
               }
            }
         }
      } else {
         this.sendOk("헤어, 얼굴 단어는 생략하고 검색해주세요.");
         this.dispose();
      }
   }

   public void sendPacket(String args) {
      this.c.getSession().writeAndFlush(PacketHelper.sendPacket(args));
   }

   public void enableMatrix() {
      MapleQuest quest = MapleQuest.getInstance(1465);
      MapleQuestStatus qs = this.c.getPlayer().getQuest(quest);
      if (quest != null && qs.getStatus() != 2) {
         qs.setStatus((byte)2);
         this.c.getPlayer().updateQuest(this.c.getPlayer().getQuest(quest), true);
      }

   }

   public void gainCorebit(int g) {
      this.getPlayer().setKeyValue(1477, "count", String.valueOf(this.getPlayer().getKeyValue(1477, "count") + (long)g));
   }

   public long getCorebit() {
      return this.getPlayer().getKeyValue(1477, "count");
   }

   public void setDeathcount(byte de) {
      this.c.getPlayer().setDeathCount(de);
      this.c.getSession().writeAndFlush(CField.getDeathCount(de));
   }

   public void UserSoulHandle(int selection) {
      Iterator var2 = this.c.getChannelServer().getSoulmatch().iterator();

      List souls;
      do {
         if (!var2.hasNext()) {
            this.c.getPlayer().dropMessageGM(6, "3");
            List<Pair<Integer, MapleCharacter>> chrs = new ArrayList();
            chrs.add(new Pair(selection, this.c.getPlayer()));
            this.c.getSession().writeAndFlush(CWvsContext.onUserSoulMatching(selection, chrs));
            if (selection == 0) {
               this.c.getPlayer().dropMessageGM(6, "4");
               this.c.getChannelServer().getSoulmatch().add(chrs);
            }

            return;
         }

         souls = (List)var2.next();
         this.c.getPlayer().dropMessageGM(6, "1");
      } while(souls.size() != 1 || (Integer)((Pair)souls.get(0)).left != 0 || selection != 0);

      souls.add(new Pair(selection, this.c.getPlayer()));
      this.c.getPlayer().dropMessageGM(6, "2 : " + souls.size());
      this.c.getSession().writeAndFlush(CWvsContext.onUserSoulMatching(selection, souls));
   }

   public void startExpRate(int hour) {
      this.c.getSession().writeAndFlush(CField.getClock(hour * 60 * 60));
      this.ExpRating();
      Timer.MapTimer.getInstance().schedule(new Runnable() {
         public void run() {
            NPCConversationManager.this.warp(1000000);
         }
      }, (long)(hour * 60 * 60 * 1000));
   }

   public void ExpRating() {
      Timer.BuffTimer.getInstance().schedule(new Runnable() {
         public void run() {
            if (NPCConversationManager.this.c.getPlayer().getMapId() == 925080000) {
               NPCConversationManager.this.c.getPlayer().gainExp(GameConstants.getExpNeededForLevel(NPCConversationManager.this.c.getPlayer().getLevel()) / 100L, true, false, false);
               NPCConversationManager.this.ExpRating();
            } else {
               NPCConversationManager.this.stopExpRate();
            }

         }
      }, 6000L);
   }

   public void stopExpRate() {
      this.c.getSession().writeAndFlush(CField.getClock(-1));
   }

   public int getFrozenMobCount() {
      return this.getPlayer().getLinkMobCount();
   }

   public void addFrozenMobCount(int a1) {
      int val = this.getFrozenMobCount() + a1 > 9999 ? 9999 : this.getFrozenMobCount() + a1;
      this.getPlayer().setLinkMobCount(val);
      this.getClient().getSession().writeAndFlush(SLFCGPacket.FrozenLinkMobCount(val));
      this.getClient().getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(1052230, 3500, "#face1# 몬스터수를 충전했어!", ""));
   }

   public long getStarDustCoin(int type) {
      return this.getPlayer().getStarDustCoin(type);
   }

   public void addStarDustCoin(int type, int a) {
      this.getPlayer().AddStarDustCoin(type, a);
   }

   public void openWeddingPresent(int type, int gender) {
      MarriageDataEntry dataEntry = this.getMarriageAgent().getDataEntry();
      if (dataEntry != null) {
         List gifts;
         if (type == 1) {
            this.c.getPlayer().setWeddingGive(gender);
            if (gender == 0) {
               gifts = dataEntry.getGroomWishList();
            } else {
               gifts = dataEntry.getBrideWishList();
            }

            this.c.getSession().writeAndFlush(CWvsContext.showWeddingWishGiveDialog(gifts));
         } else if (type == 2) {
            if (gender == 0) {
               gifts = dataEntry.getGroomPresentList();
            } else {
               gifts = dataEntry.getBridePresentList();
            }

            this.c.getSession().writeAndFlush(CWvsContext.showWeddingWishRecvDialog(gifts));
         }
      }

   }

   public void ShowDreamBreakerRanking() {
      this.c.getSession().writeAndFlush(SLFCGPacket.DreamBreakerRanking(this.c.getPlayer().getName()));
   }

   public void gainDonationSkill(int skillid) {
      if (this.c.getPlayer().getKeyValue(201910, "DonationSkill") < 0L) {
         this.c.getPlayer().setKeyValue(201910, "DonationSkill", "0");
      }

      MapleDonationSkill dskill = MapleDonationSkill.getBySkillId(skillid);
      if (dskill != null && (this.c.getPlayer().getKeyValue(201910, "DonationSkill") & (long)dskill.getValue()) == 0L) {
         int data = (int)this.c.getPlayer().getKeyValue(201910, "DonationSkill");
         data |= dskill.getValue();
         this.c.getPlayer().setKeyValue(201910, "DonationSkill", data.makeConcatWithConstants<invokedynamic>(data));
         SkillFactory.getSkill(skillid).getEffect(SkillFactory.getSkill(skillid).getMaxLevel()).applyTo(this.c.getPlayer(), 0);
      }

   }

   public boolean hasDonationSkill(int skillid) {
      if (this.c.getPlayer().getKeyValue(201910, "DonationSkill") < 0L) {
         this.c.getPlayer().setKeyValue(201910, "DonationSkill", "0");
      }

      MapleDonationSkill dskill = MapleDonationSkill.getBySkillId(skillid);
      if (dskill == null) {
         return false;
      } else {
         return (this.c.getPlayer().getKeyValue(201910, "DonationSkill") & (long)dskill.getValue()) != 0L;
      }
   }

   public String getItemNameById(int itemid) {
      String itemname = "";
      Iterator var3 = MapleItemInformationProvider.getInstance().getAllItems().iterator();

      while(var3.hasNext()) {
         Pair<Integer, String> itemPair = (Pair)var3.next();
         if ((Integer)itemPair.getLeft() == itemid) {
            itemname = (String)itemPair.getRight();
         }
      }

      return itemname;
   }

   public long getFWolfMeso() {
      if (this.c.getPlayer().getFWolfAttackCount() > 15) {
         long BaseMeso = 10000000L;
         long FWolfMeso = 0L;
         if (this.c.getPlayer().getFWolfDamage() >= 900000000000L) {
            FWolfMeso = BaseMeso * 100L;
         } else {
            float ratio = (float)(900000000000L / this.c.getPlayer().getFWolfDamage() * 100L);
            FWolfMeso = (long)((float)BaseMeso * ratio);
         }

         return FWolfMeso;
      } else {
         return (long)(100000 * this.c.getPlayer().getFWolfAttackCount());
      }
   }

   public long getFWolfEXP() {
      long expneed = GameConstants.getExpNeededForLevel(this.c.getPlayer().getLevel());
      long exp = 0L;
      if (this.c.getPlayer().getFWolfDamage() >= 37500000000000L) {
         exp = (long)((double)expneed * 0.25D);
      } else if (this.c.getPlayer().getFWolfDamage() >= 6250000000000L) {
         exp = (long)((double)expneed * 0.2D);
      } else if (this.c.getPlayer().getFWolfDamage() >= 625000000000L) {
         exp = (long)((double)expneed * 0.15D);
      } else {
         exp = (long)((double)expneed * 0.1D);
      }

      if (this.c.getPlayer().isFWolfKiller()) {
         exp = (long)((double)expneed * 0.5D);
      }

      return exp;
   }

   public void showDimentionMirror() {
      this.c.getSession().writeAndFlush(CField.dimentionMirror(ServerConstants.mirrors));
   }

   public void warpNettPyramid(boolean hard) {
      MapleNettPyramid.warpNettPyramid(this.c.getPlayer(), hard);
   }

   public void startDamageMeter() {
      this.c.getPlayer().setDamageMeter(0L);
      final MapleMap map = this.c.getChannelServer().getMapFactory().getMap(120000102);
      map.killAllMonsters(false);
      this.warp(120000102);
      this.c.getSession().writeAndFlush(CField.getClock(30));
      this.c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9063152, 3000, "20초에 허수아비가 소환되고 측정이 시작됩니다.", ""));
      final MapleMonster mob = MapleLifeFactory.getMonster(9305653);
      Timer.MapTimer.getInstance().schedule(new Runnable() {
         public void run() {
            map.spawnMonsterOnGroundBelow(mob, new Point(-140, 150));
         }
      }, 5000L);
      Timer.MapTimer.getInstance().schedule(new Runnable() {
         public void run() {
            NPCConversationManager.this.c.getPlayer().dropMessage(5, "누적 데미지 : " + NPCConversationManager.this.c.getPlayer().getDamageMeter());
            NPCConversationManager.updateDamageMeter(NPCConversationManager.this.c.getPlayer(), NPCConversationManager.this.c.getPlayer().getDamageMeter());
            NPCConversationManager.this.warp(123456788);
         }
      }, 25000L);
   }

   public static void updateDamageMeter(MapleCharacter chr, long damage) {
      try {
         Connection con = DatabaseConnection.getConnection();
         PreparedStatement ps = con.prepareStatement("DELETE FROM damagemeter WHERE cid = ?");
         ps.setInt(1, chr.getId());
         ps.executeUpdate();
         ps.close();
         ps = con.prepareStatement("INSERT INTO damagemeter(cid, name, damage) VALUES (?, ?, ?)");
         ps.setInt(1, chr.getId());
         ps.setString(2, chr.getName());
         ps.setLong(3, damage);
         ps.executeUpdate();
         ps.close();
         con.close();
         chr.setDamageMeter(0L);
      } catch (SQLException var5) {
         var5.printStackTrace();
      }

   }

   public String getDamageMeterRank(int limit) {
      String text = "#fn나눔고딕 Extrabold##fs13# ";

      try {
         Connection con = DatabaseConnection.getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT * FROM damagemeter ORDER BY damage DESC LIMIT " + limit);
         ResultSet rs = ps.executeQuery();

         for(int i = 1; rs.next(); ++i) {
            text = text + (i != 10 ? " " : "") + i + "위 " + rs.getString("name") + " #r" + this.Comma(rs.getLong("damage")) + "#e\r\n";
         }

         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var7) {
         var7.printStackTrace();
      }

      if (text.equals("#b")) {
         text = "#r아직까지 딜량 미터기를 갱신한 유저가 없습니다.";
      }

      return text;
   }

   public String DamageMeterRank() {
      String text = "#b";

      try {
         Connection con = DatabaseConnection.getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT * FROM damagemeter ORDER BY damage DESC LIMIT 10");
         ResultSet rs = ps.executeQuery();

         for(int i = 1; rs.next(); ++i) {
            text = text + "#r#e" + (i != 10 ? "0" : "") + i + "#n#b위 #r닉네임#b " + rs.getString("name") + " #r누적 데미지#b " + this.Comma(rs.getLong("damage")) + "\r\n";
         }

         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var6) {
         var6.printStackTrace();
      }

      if (text.equals("#b")) {
         text = "#r아직까지 딜량 미터기를 갱신한 유저가 없습니다.";
      }

      return text;
   }

   public boolean isDamageMeterRanker(int cid) {
      boolean value = false;

      try {
         Connection con = DatabaseConnection.getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT * FROM damagemeter ORDER BY damage DESC LIMIT 1");
         ResultSet rs = ps.executeQuery();
         if (rs.next() && rs.getInt("cid") == cid) {
            value = true;
         }

         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var6) {
         var6.printStackTrace();
      }

      return value;
   }

   public String Comma(long r) {
      String re = "";

      for(int i = String.valueOf(r).length(); i >= 1; --i) {
         if (i != 1 && i != String.valueOf(r).length() && i % 3 == 0) {
            re = re + ",";
         }

         re = re + String.valueOf(r).charAt(i - 1);
      }

      return (new StringBuilder()).append(re).reverse().toString();
   }

   public int getDamageMeterRankerId() {
      int value = -1;

      try {
         Connection con = DatabaseConnection.getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT * FROM damagemeter ORDER BY damage DESC LIMIT 1");
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
            value = rs.getInt("cid");
         }

         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var5) {
         var5.printStackTrace();
      }

      return value;
   }

   public final String getDiscord() {
      return this.c.getDiscord();
   }

   public void Entertuto(boolean black) {
      this.Entertuto(black, true, false);
   }

   public void Entertuto(boolean black, boolean effect) {
      this.Entertuto(black, effect, false);
   }

   public void Entertuto(boolean black, boolean effect, boolean blackflame) {
      this.c.send(CField.UIPacket.getDirectionStatus(true));
      this.c.send(SLFCGPacket.SetIngameDirectionMode(true, blackflame, false, false));
      if (effect) {
         Timer.EtcTimer.getInstance().schedule(() -> {
            this.c.getSession().writeAndFlush(CField.showSpineScreen(false, false, false, "Effect/Direction18.img/effect/adele/spine/etc/7/skeleton", "new", 0, true, "00"));
         }, 1000L);
      }

      Timer.EtcTimer.getInstance().schedule(() -> {
         if (black) {
            this.c.send(SLFCGPacket.MakeBlind(1, 255, 0, 0, 0, 1000, 0));
         } else {
            this.c.send(SLFCGPacket.MakeBlind(1, 200, 0, 0, 0, 1000, 0));
         }

      }, effect ? 4300L : 1000L);
      Timer.EtcTimer.getInstance().schedule(() -> {
         if (effect) {
            this.c.send(CField.showSpineScreen(false, true, false, "Effect/Direction18.img/effect/adele/spine/etc/5/skeleton", "new", 0, true, "5"));
            this.c.send(CField.showSpineScreen(false, true, false, "Effect/Direction18.img/effect/adele/spine/etc/6/skeleton", "new", 0, true, "6"));
         }

         this.c.send(SLFCGPacket.InGameDirectionEvent("", 1, 1000));
      }, effect ? 5000L : 1000L);
   }

   public void Endtuto() {
      this.Endtuto(true);
   }

   public void Endtuto(boolean effect) {
      if (effect) {
         this.c.send(CField.endscreen("5"));
         this.c.send(CField.endscreen("6"));
         this.c.getSession().writeAndFlush(CField.showSpineScreen(false, false, false, "Effect/Direction18.img/effect/adele/spine/etc/7/skeleton", "new", 0, true, "00"));
      }

      Timer.EtcTimer.getInstance().schedule(() -> {
         this.c.send(SLFCGPacket.MakeBlind(1, 0, 0, 0, 0, 1300, 0));
      }, effect ? 3500L : 1000L);
      Timer.EtcTimer.getInstance().schedule(() -> {
         this.c.send(CField.UIPacket.getDirectionStatus(false));
         this.c.send(SLFCGPacket.SetIngameDirectionMode(false, false, false, false));
      }, effect ? 5000L : 2000L);
   }

   public void sendScreenText(String str, boolean newwrite) {
      this.c.send(SLFCGPacket.InGameDirectionEvent(str, 12, newwrite ? 1 : 0));
   }

   public void EnterMonsterPark(int mapid) {
      int count = 0;

      for(int i = mapid; i < mapid + 500; i += 100) {
         count += this.c.getChannelServer().getMapFactory().getMap(i).getNumSpawnPoints();
      }

      this.c.getPlayer().setMparkcount(count);
   }

   public void moru(Equip item, Equip item2) {
      if (item.getMoru() != 0) {
         item2.setMoru(item.getMoru());
      } else {
         String lol = Integer.valueOf(item.getItemId()).toString();
         String ss = lol.substring(3, 7);
         item2.setMoru(Integer.parseInt(ss));
      }

      this.c.getSession().writeAndFlush(CWvsContext.InventoryPacket.getFusionAnvil(true, 5062400, 2028093));
      this.c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateEquipSlot(item2));
   }

   public String EqpItem() {
      String info = "";
      int i = 0;
      Iterator var3 = this.c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).iterator();

      while(var3.hasNext()) {
         Item item = (Item)var3.next();
         Equip Eqp = (Equip)item;
         if (Eqp != null) {
            if (Eqp.getMoru() > 0) {
               int itemid = Eqp.getItemId() / 10000 * 10000 + Eqp.getMoru();
               info = info + "#L" + Eqp.getItemId() + "# #i" + Eqp.getItemId() + "#  [ #i" + itemid + "# ]  #t" + Eqp.getItemId() + "# #r(모루)#k#b\r\n";
            }

            ++i;
         }
      }

      return info;
   }

   public void sendJobIlust(int type, boolean lumi) {
      if (this.lastMsg <= -1) {
         this.c.getSession().writeAndFlush(CField.NPCPacket.getIlust(this.id, type, lumi));
      }
   }

   public boolean checkDayItem(String s, int type) {
      MapleCharacter chr = this.c.getPlayer();
      KoreaCalendar kc = new KoreaCalendar();
      int var10000 = kc.getYeal() % 100;
      String today = var10000 + "/" + kc.getMonths() + "/" + kc.getDays();
      if (type == 0) {
         chr.addKV(s, today);
         return true;
      } else {
         if (type == 1 && chr.getV(s) != null) {
            String[] array = chr.getV(s).split("/");
            Calendar clear = new GregorianCalendar(Integer.parseInt("20" + array[0]), Integer.parseInt(array[1]) - 1, Integer.parseInt(array[2]));
            Calendar ocal = Calendar.getInstance();
            int yeal = clear.get(1);
            int days = clear.get(5);
            int day = ocal.get(7);
            int day2 = clear.get(7);
            int maxday = clear.getMaximum(5);
            int month = clear.get(2);
            int check = day2 == 5 ? 7 : (day2 == 6 ? 6 : (day2 == 7 ? 5 : 0));
            int afterday;
            if (check == 0) {
               for(afterday = day2; afterday < 5; ++afterday) {
                  ++check;
               }
            }

            afterday = days + check;
            if (afterday > maxday) {
               afterday -= maxday;
               ++month;
            }

            if (month > 12) {
               ++yeal;
               month = 1;
            }

            Calendar after = new GregorianCalendar(yeal, month, afterday);
            if (after.getTimeInMillis() > System.currentTimeMillis()) {
               return false;
            }
         }

         return true;
      }
   }

   public long ExpPocket(int type) {
      long t = 0L;
      long time = (System.currentTimeMillis() - Long.parseLong(this.c.getCustomData(247, "lastTime"))) / 1000L;
      if (time > 43200L) {
         time = 43200L;
      }

      long gainexp = time / 10L * (long)GameConstants.ExpPocket(this.c.getPlayer().getLevel());
      if (type == 1) {
         t = time;
      } else {
         t = gainexp;
      }

      return t;
   }

   public void SelectQuest(String quest, int quest1, int quest2, int count) {
      List<Integer> QuestList = new ArrayList();
      List<Integer> SelectQuest = new ArrayList();

      int questid;
      for(questid = quest1; questid < quest2; ++questid) {
         QuestList.add(questid);
      }

      while(SelectQuest.size() < count) {
         questid = (Integer)QuestList.get(Randomizer.rand(0, QuestList.size() - 1));
         boolean no = false;
         switch(questid) {
         case 35566:
         case 35567:
         case 35568:
         case 35569:
         case 35583:
         case 35584:
         case 35585:
         case 35586:
         case 35587:
         case 35588:
         case 35589:
         case 39109:
         case 39110:
         case 39120:
         case 39128:
         case 39129:
         case 39130:
         case 39137:
         case 39138:
         case 39139:
         case 39140:
            no = true;
            break;
         default:
            no = false;
         }

         while(SelectQuest.contains(questid) || no) {
            questid = (Integer)QuestList.get(Randomizer.rand(0, QuestList.size() - 1));
            switch(questid) {
            case 35566:
            case 35567:
            case 35568:
            case 35569:
            case 35583:
            case 35584:
            case 35585:
            case 35586:
            case 35587:
            case 35588:
            case 35589:
            case 39109:
            case 39110:
            case 39120:
            case 39128:
            case 39129:
            case 39130:
            case 39137:
            case 39138:
            case 39139:
            case 39140:
               no = true;
               break;
            default:
               no = false;
            }
         }

         SelectQuest.add(questid);
      }

      String q = "";

      for(int j = 0; j < SelectQuest.size(); ++j) {
         q = q + SelectQuest.get(j);
         if (j != SelectQuest.size() - 1) {
            q = q + ",";
         }
      }

      this.c.getPlayer().addKV(quest, q);
   }

   public int ReplaceQuest(String quest, int quest1, int quest2, int anotherquest) {
      List<Integer> QuestList = new ArrayList();
      List<Integer> SelectQuest = new ArrayList();
      List<Integer> MyQuest = new ArrayList();

      for(int i = quest1; i < quest2; ++i) {
         QuestList.add(i);
      }

      String[] KeyValue = this.c.getPlayer().getV(quest).split(",");

      int j;
      for(j = 0; j < KeyValue.length; ++j) {
         MyQuest.add(Integer.parseInt(KeyValue[j]));
      }

      for(j = 0; j < KeyValue.length; ++j) {
         if (Integer.parseInt(KeyValue[j]) != anotherquest) {
            SelectQuest.add(Integer.parseInt(KeyValue[j]));
         }
      }

      int questid = (Integer)QuestList.get(Randomizer.rand(0, QuestList.size() - 1));

      boolean no;
      do {
         questid = (Integer)QuestList.get(Randomizer.rand(0, QuestList.size() - 1));
         no = false;
         switch(questid) {
         case 35566:
         case 35567:
         case 35568:
         case 35569:
         case 35583:
         case 35584:
         case 35585:
         case 35586:
         case 35587:
         case 35588:
         case 35589:
         case 39109:
         case 39110:
         case 39120:
         case 39128:
         case 39129:
         case 39130:
         case 39137:
         case 39138:
         case 39139:
         case 39140:
            no = true;
            break;
         default:
            no = false;
         }
      } while(questid == anotherquest || SelectQuest.contains(questid) || no);

      SelectQuest.add(questid);
      String q = "";

      for(int k = 0; k < SelectQuest.size(); ++k) {
         q = q + SelectQuest.get(k);
         if (k != SelectQuest.size() - 1) {
            q = q + ",";
         }
      }

      this.c.getPlayer().addKV(quest, q);
      return questid;
   }

   public void cancelSkillsbuff() {
      Iterator var1 = this.c.getPlayer().getEffects().iterator();

      while(var1.hasNext()) {
         Pair<SecondaryStat, SecondaryStatValueHolder> data = (Pair)var1.next();
         SecondaryStatValueHolder mbsvh = (SecondaryStatValueHolder)data.right;
         if (SkillFactory.getSkill(mbsvh.effect.getSourceId()) != null && mbsvh.effect.getSourceId() != 80002282 && mbsvh.effect.getSourceId() != 2321055) {
            this.c.getPlayer().cancelEffect(mbsvh.effect, Arrays.asList((SecondaryStat)data.left));
         }
      }

   }

   public void getJobName() {
   }

   public void sendPQRanking(byte type) {
      List<String> info = new ArrayList();
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         if (type == 0) {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT * FROM characters WHERE gm = 0 ORDER BY level DESC LIMIT 100");
            rs = ps.executeQuery();

            while(rs.next()) {
               String var10001 = rs.getString("name");
               info.add(var10001 + ",레벨 / " + rs.getString("level") + ",계승 레벨 / " + rs.getString("fame") + ",(" + GameConstants.getJobNameById(Integer.parseInt(rs.getString("job"))) + ")");
            }
         }

         ps.close();
         con.close();
         rs.close();
      } catch (SQLException var15) {
         System.err.println("Error while unbanning" + var15);
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var14) {
            var14.printStackTrace();
         }

      }

      this.c.getSession().writeAndFlush(CField.PartyRankingInfo(info));
   }

   public void StarForceEnchant25(Equip equip) {
      Equip nEquip = equip;
      Equip zeroEquip = null;
      if (GameConstants.isAlphaWeapon(equip.getItemId())) {
         zeroEquip = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
      } else if (GameConstants.isBetaWeapon(equip.getItemId())) {
         zeroEquip = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
      }

      int max = GameConstants.isStarForceScroll(2049372);
      boolean isSuperiol = MapleItemInformationProvider.getInstance().isSuperial(equip.getItemId()).left != null;
      int reqLevel = this.getReqLevel(equip.getItemId());
      int maxEnhance;
      if (reqLevel < 95) {
         maxEnhance = isSuperiol ? 3 : 5;
      } else if (reqLevel <= 107) {
         maxEnhance = isSuperiol ? 5 : 8;
      } else if (reqLevel <= 119) {
         maxEnhance = isSuperiol ? 8 : 10;
      } else if (reqLevel <= 129) {
         maxEnhance = isSuperiol ? 10 : 15;
      } else if (reqLevel <= 139) {
         maxEnhance = isSuperiol ? 12 : 20;
      } else {
         maxEnhance = isSuperiol ? 15 : 25;
      }

      if (maxEnhance < max) {
         max = maxEnhance;
      }

      while(nEquip.getEnhance() < max) {
         StarForceStats starForceStats = EquipmentEnchant.starForceStats(nEquip);
         nEquip.setEnchantBuff((short)0);
         nEquip.setEnhance((byte)(nEquip.getEnhance() + 1));
         Iterator var9 = starForceStats.getStats().iterator();

         while(var9.hasNext()) {
            Pair<EnchantFlag, Integer> stat = (Pair)var9.next();
            if (EnchantFlag.Watk.check(((EnchantFlag)stat.left).getValue())) {
               nEquip.setEnchantWatk((short)(nEquip.getEnchantWatk() + (Integer)stat.right));
               if (zeroEquip != null) {
                  zeroEquip.setEnchantWatk((short)(zeroEquip.getEnchantWatk() + (Integer)stat.right));
               }
            }

            if (EnchantFlag.Matk.check(((EnchantFlag)stat.left).getValue())) {
               nEquip.setEnchantMatk((short)(nEquip.getEnchantMatk() + (Integer)stat.right));
               if (zeroEquip != null) {
                  zeroEquip.setEnchantMatk((short)(zeroEquip.getEnchantMatk() + (Integer)stat.right));
               }
            }

            if (EnchantFlag.Str.check(((EnchantFlag)stat.left).getValue())) {
               nEquip.setEnchantStr((short)(nEquip.getEnchantStr() + (Integer)stat.right));
               if (zeroEquip != null) {
                  zeroEquip.setEnchantStr((short)(zeroEquip.getEnchantStr() + (Integer)stat.right));
               }
            }

            if (EnchantFlag.Dex.check(((EnchantFlag)stat.left).getValue())) {
               nEquip.setEnchantDex((short)(nEquip.getEnchantDex() + (Integer)stat.right));
               if (zeroEquip != null) {
                  zeroEquip.setEnchantDex((short)(zeroEquip.getEnchantDex() + (Integer)stat.right));
               }
            }

            if (EnchantFlag.Int.check(((EnchantFlag)stat.left).getValue())) {
               nEquip.setEnchantInt((short)(nEquip.getEnchantInt() + (Integer)stat.right));
               if (zeroEquip != null) {
                  zeroEquip.setEnchantInt((short)(zeroEquip.getEnchantInt() + (Integer)stat.right));
               }
            }

            if (EnchantFlag.Luk.check(((EnchantFlag)stat.left).getValue())) {
               nEquip.setEnchantLuk((short)(nEquip.getEnchantLuk() + (Integer)stat.right));
               if (zeroEquip != null) {
                  zeroEquip.setEnchantLuk((short)(zeroEquip.getEnchantLuk() + (Integer)stat.right));
               }
            }

            if (EnchantFlag.Wdef.check(((EnchantFlag)stat.left).getValue())) {
               nEquip.setEnchantWdef((short)(nEquip.getEnchantWdef() + (Integer)stat.right));
               if (zeroEquip != null) {
                  zeroEquip.setEnchantWdef((short)(zeroEquip.getEnchantWdef() + (Integer)stat.right));
               }
            }

            if (EnchantFlag.Mdef.check(((EnchantFlag)stat.left).getValue())) {
               nEquip.setEnchantMdef((short)(nEquip.getEnchantMdef() + (Integer)stat.right));
               if (zeroEquip != null) {
                  zeroEquip.setEnchantMdef((short)(zeroEquip.getEnchantMdef() + (Integer)stat.right));
               }
            }

            if (EnchantFlag.Hp.check(((EnchantFlag)stat.left).getValue())) {
               nEquip.setEnchantHp((short)(nEquip.getEnchantHp() + (Integer)stat.right));
               if (zeroEquip != null) {
                  zeroEquip.setEnchantHp((short)(zeroEquip.getEnchantHp() + (Integer)stat.right));
               }
            }

            if (EnchantFlag.Mp.check(((EnchantFlag)stat.left).getValue())) {
               nEquip.setEnchantMp((short)(nEquip.getEnchantMp() + (Integer)stat.right));
               if (zeroEquip != null) {
                  zeroEquip.setEnchantMp((short)(zeroEquip.getEnchantMp() + (Integer)stat.right));
               }
            }

            if (EnchantFlag.Acc.check(((EnchantFlag)stat.left).getValue())) {
               nEquip.setEnchantAcc((short)(nEquip.getEnchantAcc() + (Integer)stat.right));
               if (zeroEquip != null) {
                  zeroEquip.setEnchantAcc((short)(zeroEquip.getEnchantAcc() + (Integer)stat.right));
               }
            }

            if (EnchantFlag.Avoid.check(((EnchantFlag)stat.left).getValue())) {
               nEquip.setEnchantAvoid((short)(nEquip.getEnchantAvoid() + (Integer)stat.right));
               if (zeroEquip != null) {
                  zeroEquip.setEnchantAvoid((short)(zeroEquip.getEnchantAvoid() + (Integer)stat.right));
               }
            }
         }
      }

      EquipmentEnchant.checkEquipmentStats(this.c.getPlayer().getClient(), nEquip);
      if (zeroEquip != null) {
         EquipmentEnchant.checkEquipmentStats(this.c.getPlayer().getClient(), zeroEquip);
      }

      this.c.getPlayer().forceReAddItem(nEquip, MapleInventoryType.getByType((byte)1));
      if (zeroEquip != null) {
         this.c.getPlayer().forceReAddItem(zeroEquip, MapleInventoryType.getByType((byte)1));
      }

   }

   public String World_boss_team_check(int charid) {
      String teamname = null;
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM world_boss_team WHERE characterid = ?");
         ps.setInt(1, charid);
         rs = ps.executeQuery();
         if (rs.next()) {
            teamname = rs.getString("teamname");
         }

         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var15) {
         FileoutputUtil.outputFileError("Log_Script_Except.rtf", var15);
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var14) {
            var14.printStackTrace();
         }

      }

      return teamname;
   }

   public void World_boss_team_insert(int teamid, String teamname, int accid, int charid, String charname) {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("INSERT INTO world_boss_team(teamid, teamname, accid, characterid, charname) VALUES (?, ?, ?, ?, ?)");
         ps.setInt(1, teamid);
         ps.setString(2, teamname);
         ps.setInt(3, accid);
         ps.setInt(4, charid);
         ps.setString(5, charname);
         ps.executeUpdate();
         ps.close();
         con.close();
      } catch (SQLException var18) {
         FileoutputUtil.outputFileError("Log_Script_Except.rtf", var18);
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var17) {
            var17.printStackTrace();
         }

      }

   }

   public void World_boss_team_update(int teamid, String teamname, int accid, int charid, String charname) {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE world_boss_team SET teamid = ?, teamname = ? where accid = ? and characterid = ? and charname = ?");
         ps.setInt(1, teamid);
         ps.setString(2, teamname);
         ps.setInt(3, accid);
         ps.setInt(4, charid);
         ps.setString(5, charname);
         ps.executeUpdate();
         ps.close();
         con.close();
      } catch (SQLException var18) {
         FileoutputUtil.outputFileError("Log_Script_Except.rtf", var18);
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var17) {
            var17.printStackTrace();
         }

      }

   }
}
