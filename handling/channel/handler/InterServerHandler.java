package handling.channel.handler;

import client.BuddylistEntry;
import client.CharacterNameAndId;
import client.InnerSkillValueHolder;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleCoolDownValueHolder;
import client.MapleQuestStatus;
import client.MapleStat;
import client.SecondaryStat;
import client.Skill;
import client.SkillFactory;
import client.inventory.AuctionItem;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.GameConstants;
import constants.KoreaCalendar;
import constants.ServerConstants;
import handling.RecvPacketOpcode;
import handling.auction.AuctionServer;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.AccountIdChannelPair;
import handling.world.CharacterTransfer;
import handling.world.MapleMessenger;
import handling.world.MapleMessengerCharacter;
import handling.world.PlayerBuffStorage;
import handling.world.World;
import handling.world.guild.MapleGuild;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import handling.world.party.PartyOperation;
import io.netty.channel.Channel;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import scripting.NPCScriptManager;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.maps.FieldLimitType;
import server.maps.MapleMap;
import server.quest.MapleQuest;
import server.quest.QuestCompleteStatus;
import tools.CurrentTime;
import tools.FileoutputUtil;
import tools.Pair;
import tools.StringUtil;
import tools.Triple;
import tools.TripleDESCipher;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CSPacket;
import tools.packet.CWvsContext;
import tools.packet.LoginPacket;
import tools.packet.SLFCGPacket;

public class InterServerHandler {
   public static final void EnterCS(MapleClient c, MapleCharacter chr) {
      chr.getClient().removeClickedNPC();
      NPCScriptManager.getInstance().dispose(chr.getClient());
      chr.getClient().getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      chr.getClient().getSession().writeAndFlush(CField.UIPacket.openUI(1271));
   }

   public static final void EnterCS(MapleClient c, MapleCharacter chr, boolean npc) {
      if (npc) {
         chr.getClient().removeClickedNPC();
         NPCScriptManager.getInstance().dispose(chr.getClient());
         chr.getClient().getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
         c.getSession().writeAndFlush(CField.UIPacket.openUI(1271));
      } else {
         if (chr.getMap() == null || chr.getEventInstance() != null || c.getChannelServer() == null) {
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            return;
         }

         if (World.getPendingCharacterSize() >= 10) {
            chr.dropMessage(1, "현재 서버가 혼잡하여 이동할 수 없습니다.");
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            return;
         }

         ChannelServer ch = ChannelServer.getInstance(c.getChannel());
         chr.changeRemoval();
         if (chr.getMessenger() != null) {
            MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(chr);
            World.Messenger.leaveMessenger(chr.getMessenger().getId(), messengerplayer);
         }

         PlayerBuffStorage.addBuffsToStorage(chr.getId(), chr.getAllBuffs());
         PlayerBuffStorage.addCooldownsToStorage(chr.getId(), chr.getCooldowns());
         World.ChannelChange_Data(new CharacterTransfer(chr), chr.getId(), -10);
         ch.removePlayer(chr);
         c.updateLoginState(3, c.getSessionIPAddress());
         chr.saveToDB(false, false);
         chr.getMap().removePlayer(chr);
         c.getSession().writeAndFlush(CField.getChannelChange(c, Integer.parseInt(CashShopServer.getIP().split(":")[1])));
         c.setPlayer((MapleCharacter)null);
         c.setReceiving(false);
      }

   }

   public static final void Loggedin(int playerid, MapleClient c) {
      try {
         ChannelServer channelServer = c.getChannelServer();
         CharacterTransfer transfer = channelServer.getPlayerStorage().getPendingCharacter(playerid);
         MapleCharacter player;
         String str;
         if (transfer == null) {
            player = MapleCharacter.loadCharFromDB(playerid, c, true);
            Pair<String, String> ip = LoginServer.getLoginAuth(playerid);
            str = c.getSessionIPAddress();
            if (ip == null || !str.substring(str.indexOf(47) + 1).equals(ip.left)) {
               if (ip != null) {
                  LoginServer.putLoginAuth(playerid, (String)ip.left, (String)ip.right);
               }

               c.disconnect(true, false, false);
               c.getSession().close();
               return;
            }

            if (c.getAccID() != player.getAccountID()) {
               c.disconnect(true, false, false);
               c.getSession().close();
               return;
            }

            c.setTempIP((String)ip.right);
            if (World.Find.findChannel(playerid) >= 0) {
               c.disconnect(true, false, false);
               c.getSession().close();
               return;
            }
         } else {
            player = MapleCharacter.ReconstructChr(transfer, c, true);
         }

         c.setPlayer(player);
         c.setAccID(player.getAccountID());
         if (GameConstants.isZero(c.getPlayer().getJob())) {
            if (c.getPlayer().getGender() == 1) {
               c.getPlayer().setGender((byte)0);
               c.getPlayer().setSecondGender((byte)1);
            } else {
               c.getPlayer().setGender((byte)0);
               c.getPlayer().setSecondGender((byte)1);
            }
         }

         c.loadKeyValues();
         c.loadCustomDatas();
         if (!c.CheckIPAddress()) {
            c.disconnect(true, false, false);
            c.getSession().close();
            return;
         }

         channelServer.removePlayer(player);
         World.isCharacterListConnected(player.getName(), c.loadCharacterNames(c.getWorld()));
         c.updateLoginState(2, c.getSessionIPAddress());
         channelServer.addPlayer(player);
         int[] bossquests = new int[]{34167, 34165, 34166, 34164, 34561, 38101, 31686, 34966, 38214, 38213, 33565, 31851, 31833, 3496, 3470, 38214, 30007, 3170, 31179, 3521, 31152, 34015, 33294, 34330, 34585, 35632, 35731, 35815, 34478, 36013, 34331, 34478, 100114, 16013, 34120, 34218, 34330, 34331, 34478, 34269, 34272, 34585, 34586, 6500, 1465, 1466, 26607, 1484, 39921, 16059, 16015, 16013, 100114, 34128, 39013, 34772, 39034, 34269, 34271, 34272, 34243, 39204, 15417, 34377, 34477, 34450};
         int[] var31 = bossquests;
         int k = bossquests.length;

         int skillid;
         int k;
         for(skillid = 0; skillid < k; ++skillid) {
            k = var31[skillid];
            if (player.getQuestStatus(k) != 2) {
               if (k != 1465 && k != 1466) {
                  MapleQuest.getInstance(k).forceComplete(player, 0, false);
               } else if (player.getLevel() >= 200) {
                  MapleQuest.getInstance(k).forceComplete(player, 0, false);
               }
            }
         }

         if (c.getPlayer().getKeyValue(39160, "start") <= 0L) {
            c.getPlayer().setKeyValue(39160, "start", "1");
            c.getPlayer().setKeyValue(39165, "start", "1");
         }

         if (c.getPlayer().getKeyValueStr(34271, "02") == null) {
            c.getPlayer().setKeyValue(34271, "02", "h0");
            c.getPlayer().setKeyValue(34271, "20", "h0");
            c.getPlayer().setKeyValue(34271, "30", "h0");
            c.getPlayer().setKeyValue(34271, "21", "h0");
            c.getPlayer().setKeyValue(34271, "31", "h0");
            c.getPlayer().setKeyValue(34271, "23", "h0");
            c.getPlayer().setKeyValue(34271, "32", "h1");
            c.getPlayer().setKeyValue(34271, "33", "h0");
            c.getPlayer().setKeyValue(34271, "52", "h0");
            c.getPlayer().setKeyValue(34271, "34", "h0");
            c.getPlayer().setKeyValue(34271, "35", "h0");
            c.getPlayer().setKeyValue(34271, "53", "h1");
            c.getPlayer().setKeyValue(34271, "36", "h0");
            c.getPlayer().setKeyValue(34271, "18", "h0");
            c.getPlayer().setKeyValue(34271, "34", "h0");
            c.getPlayer().setKeyValue(34271, "54", "h0");
            c.getPlayer().setKeyValue(34271, "28", "h0");
            c.getPlayer().setKeyValue(34271, "29", "h0");
         }

         if (player.getQuestStatus(30023) != 1 && player.getQuestStatus(30024) != 1 && player.getQuestStatus(30025) != 1 && player.getQuestStatus(30026) != 1) {
            MapleQuest.getInstance(30023).forceStart(player, 0, "10");
            MapleQuest.getInstance(30024).forceStart(player, 0, "10");
            MapleQuest.getInstance(30025).forceStart(player, 0, "10");
            MapleQuest.getInstance(30026).forceStart(player, 0, "10");
         }

         if (GameConstants.isEvan(player.getJob())) {
            if (player.getQuestStatus(22130) != 2 && player.getJob() != 2001) {
               MapleQuest.getInstance(22130).forceComplete(player, 0);
            }
         } else if (GameConstants.isEunWol(player.getJob())) {
            if (player.getQuestStatus(1542) != 2) {
               MapleQuest.getInstance(1542).forceComplete(player, 0);
            }
         } else {
            int k;
            if (GameConstants.isArk(player.getJob())) {
               for(k = 34940; k < 34960; ++k) {
                  MapleQuest.getInstance(k).forceComplete(player, 0);
               }
            } else if (GameConstants.isKadena(player.getJob())) {
               for(k = 34600; k < 34650; ++k) {
                  MapleQuest.getInstance(k).forceComplete(player, 0);
               }
            }
         }

         String[] str;
         if (player.getClient().getKeyValue("LevelUpGive") == null) {
            player.getClient().setKeyValue("LevelUpGive", "0000000000000000");
         } else {
            str = player.getClient().getKeyValue("LevelUpGive").split("");

            for(k = 0; k < 16; ++k) {
               if (Integer.parseInt(str[k]) == 1) {
                  skillid = '\uea60' + k;
                  if (player.getQuestStatus(skillid) != 2) {
                     player.forceCompleteQuest(skillid);
                  }
               }
            }
         }

         if (player.getClient().getKeyValue("GrowQuest") == null) {
            player.getClient().setKeyValue("GrowQuest", "0000000000");
         } else {
            str = player.getClient().getKeyValue("GrowQuest").split("");

            for(k = 0; k < 10; ++k) {
               skillid = '썐' + k;
               if (Integer.parseInt(str[k]) == 1) {
                  if (player.getQuestStatus(skillid) != 1 && player.getQuestStatus(skillid) != 2) {
                     MapleQuest.getInstance(skillid).forceStart(c.getPlayer(), 0, "");
                  }
               } else if (Integer.parseInt(str[k]) == 2 && player.getQuestStatus(skillid) != 2) {
                  player.forceCompleteQuest(skillid);
               }
            }
         }

         if (player.getClient().getKeyValue("BloomingTuto") != null) {
            if (player.getClient().getKeyValue("BloomingSkill") != null) {
               str = player.getClient().getKeyValue("BloomingSkill");
               String[] ab = str.split("");
               skillid = 80003036;

               for(k = 0; k < ab.length; ++k) {
                  player.setKeyValue(501378, k.makeConcatWithConstants<invokedynamic>(k), ab[k].makeConcatWithConstants<invokedynamic>(ab[k]));
                  if (Integer.parseInt(ab[k]) > 0) {
                     player.changeSkillLevel(skillid + k, (byte)Integer.parseInt(ab[k]), (byte)3);
                  }
               }
            }

            if (player.getClient().getKeyValue("Bloomingbloom") != null) {
               player.setKeyValue(501367, "bloom", player.getClient().getKeyValue("Bloomingbloom"));
            }

            if (player.getClient().getKeyValue("BloomingSkillPoint") != null) {
               player.setKeyValue(501378, "sp", player.getClient().getKeyValue("BloomingSkillPoint"));
            }

            if (player.getClient().getKeyValue("BloomingReward") != null) {
               player.setKeyValue(501367, "reward", player.getClient().getKeyValue("BloomingReward"));
            }

            if (player.getClient().getKeyValue("week") != null) {
               player.setKeyValue(501367, "week", player.getClient().getKeyValue("week"));
            }

            if (player.getClient().getKeyValue("getReward") != null) {
               player.setKeyValue(501367, "getReward", player.getClient().getKeyValue("getReward"));
            }

            if (player.getClient().getKeyValue("BloomingSkilltuto") != null) {
               player.setKeyValue(501378, "tuto", "1");
            }

            if (player.getClient().getKeyValue("BloominggiveSun") != null) {
               player.setKeyValue(501367, "giveSun", "1");
            }

            if (player.getClient().getKeyValue("Bloomingflower") != null) {
               player.setKeyValue(501387, "flower", player.getClient().getKeyValue("Bloomingflower"));
            }

            if (Integer.parseInt(player.getClient().getKeyValue("BloomingTuto")) > 1 && player.getQuestStatus(501394) != 2) {
               player.forceCompleteQuest(501394);
            }

            if (Integer.parseInt(player.getClient().getKeyValue("BloomingTuto")) > 2) {
               if (player.getQuestStatus(501375) != 2) {
                  player.forceCompleteQuest(501375);
               }

               if (player.getKeyValue(501375, "start") != 1L) {
                  player.setKeyValue(501375, "start", "1");
               }
            }

            if (Integer.parseInt(player.getClient().getKeyValue("BloomingTuto")) > 3) {
               if (player.getQuestStatus(501376) != 2) {
                  player.forceCompleteQuest(501376);
               }

               if (player.getKeyValue(501376, "start") != 1L) {
                  player.setKeyValue(501376, "start", "1");
               }
            }
         }

         Iterator iterator = QuestCompleteStatus.completeQuests.iterator();

         while(iterator.hasNext()) {
            k = (Integer)iterator.next();
            if (player.getQuestStatus(k) != 2) {
               MapleQuest.getInstance(k).forceComplete(player, 0, false);
            }
         }

         if (c.getPlayer().getKeyValue(125, "date") != (long)GameConstants.getCurrentDate_NoTime()) {
            c.getPlayer().setKeyValue(125, "date", String.valueOf(GameConstants.getCurrentDate_NoTime()));
            int pirodo = 0;
            switch(1) {
            case 1:
               pirodo = 100;
               break;
            case 2:
               pirodo = 120;
               break;
            case 3:
               pirodo = 160;
               break;
            case 4:
               pirodo = 200;
               break;
            case 5:
               pirodo = 240;
               break;
            case 6:
               pirodo = 280;
               break;
            case 7:
               pirodo = 320;
               break;
            case 8:
               pirodo = 360;
            }

            c.getPlayer().setKeyValue(123, "pp", String.valueOf(pirodo));
         }

         String var10000 = FileoutputUtil.접속로그;
         int var10001 = player.getClient().getAccID();
         FileoutputUtil.log(var10000, "[접속] 계정번호 : " + var10001 + " | " + player.getName() + "(" + player.getId() + ")이 접속.");
         if (player.getKeyValue(18771, "rank") == -1L || player.getKeyValue(18771, "rank") == 100L) {
            player.setKeyValue(18771, "rank", "101");
         }

         if (c.getKeyValue("rank") == null) {
            c.setKeyValue("rank", String.valueOf(player.getKeyValue(18771, "rank")));
         }

         if ((long)Integer.parseInt(c.getKeyValue("rank")) < player.getKeyValue(18771, "rank")) {
            c.setKeyValue("rank", String.valueOf(player.getKeyValue(18771, "rank")));
         }

         if ((long)Integer.parseInt(c.getKeyValue("rank")) > player.getKeyValue(18771, "rank")) {
            player.setKeyValue(18771, "rank", c.getKeyValue("rank"));
         }

         if (player.getInnerSkills().size() == 0) {
            player.getInnerSkills().add(new InnerSkillValueHolder(70000004, (byte)1, (byte)1, (byte)0));
            player.getInnerSkills().add(new InnerSkillValueHolder(70000004, (byte)1, (byte)1, (byte)0));
            player.getInnerSkills().add(new InnerSkillValueHolder(70000004, (byte)1, (byte)1, (byte)0));
         }

         player.LoadPlatformerRecords();
         player.giveCoolDowns(PlayerBuffStorage.getCooldownsFromStorage(player.getId()));
         Item ordinary;
         if (player.choicepotential != null && player.memorialcube != null) {
            ordinary = player.getInventory(MapleInventoryType.EQUIP).getItem(player.choicepotential.getPosition());
            if (ordinary != null) {
               player.choicepotential.setInventoryId(ordinary.getInventoryId());
            }
         }

         if (c.getKeyValue("PNumber") == null) {
            c.setKeyValue("PNumber", "0");
         }

         if (player.returnscroll != null) {
            ordinary = player.getInventory(MapleInventoryType.EQUIP).getItem(player.returnscroll.getPosition());
            if (ordinary != null) {
               player.returnscroll.setInventoryId(ordinary.getInventoryId());
            }
         }

         player.showNote();
         player.showsendNote();
         c.getSession().writeAndFlush(CField.getCharInfo(player));
         c.getSession().writeAndFlush(CSPacket.enableCSUse());
         c.getSession().writeAndFlush(SLFCGPacket.SetupZodiacInfo());
         if (player.getKeyValue(190823, "grade") == -1L) {
            player.setKeyValue(190823, "grade", "0");
         }

         c.send(LoginPacket.debugClient());
         List<Pair<Integer, Integer>> list = new ArrayList();
         list.add(new Pair(8500002, 3655));
         list.add(new Pair(8500012, 3656));
         list.add(new Pair(8500022, 3657));
         list.add(new Pair(8644612, 0));
         list.add(new Pair(8644650, 3680));
         list.add(new Pair(8644655, 3682));
         list.add(new Pair(8645009, 3681));
         list.add(new Pair(8645066, 3683));
         list.add(new Pair(8800002, 3654));
         list.add(new Pair(8800022, 6994));
         list.add(new Pair(8800102, 15166));
         list.add(new Pair(8810018, 3789));
         list.add(new Pair(8810122, 3790));
         list.add(new Pair(8810214, 3651));
         list.add(new Pair(8820001, 3652));
         list.add(new Pair(8820212, 3653));
         list.add(new Pair(8840000, 3794));
         list.add(new Pair(8840007, 3793));
         list.add(new Pair(8840014, 3795));
         list.add(new Pair(8850005, 31095));
         list.add(new Pair(8850006, 31096));
         list.add(new Pair(8850007, 31097));
         list.add(new Pair(8850008, 31098));
         list.add(new Pair(8850009, 31099));
         list.add(new Pair(8850011, 31196));
         list.add(new Pair(8850111, 31199));
         list.add(new Pair(8860000, 3792));
         list.add(new Pair(8860005, 3791));
         list.add(new Pair(8870000, 3649));
         list.add(new Pair(8870100, 3650));
         list.add(new Pair(8880000, 3992));
         list.add(new Pair(8880002, 3993));
         list.add(new Pair(8880010, 3996));
         list.add(new Pair(8880100, 3663));
         list.add(new Pair(8880101, 34018));
         list.add(new Pair(8880110, 3662));
         list.add(new Pair(8880111, 34017));
         list.add(new Pair(8880140, 3659));
         list.add(new Pair(8880141, 3660));
         list.add(new Pair(8880142, 3684));
         list.add(new Pair(8880150, 34354));
         list.add(new Pair(8880151, 3661));
         list.add(new Pair(8880153, 34356));
         list.add(new Pair(8880155, 3685));
         list.add(new Pair(8880156, 34349));
         list.add(new Pair(8880167, 34368));
         list.add(new Pair(8880177, 34369));
         list.add(new Pair(8880200, 3591));
         list.add(new Pair(8880301, 3666));
         list.add(new Pair(8880302, 3658));
         list.add(new Pair(8880303, 3664));
         list.add(new Pair(8880304, 3665));
         list.add(new Pair(8880341, 3669));
         list.add(new Pair(8880342, 3670));
         list.add(new Pair(8880343, 3667));
         list.add(new Pair(8880344, 3668));
         list.add(new Pair(8880400, 3671));
         list.add(new Pair(8880405, 3672));
         list.add(new Pair(8880410, 3673));
         list.add(new Pair(8880415, 3674));
         list.add(new Pair(8880502, 3676));
         list.add(new Pair(8880503, 3677));
         list.add(new Pair(8880505, 3675));
         list.add(new Pair(8880518, 3679));
         list.add(new Pair(8880519, 3678));
         list.add(new Pair(8880600, 3686));
         list.add(new Pair(8880602, 3687));
         list.add(new Pair(8880614, 3687));
         list.add(new Pair(8881000, 0));
         list.add(new Pair(8900000, 30043));
         list.add(new Pair(8900001, 30043));
         list.add(new Pair(8900002, 30043));
         list.add(new Pair(8900003, 30043));
         list.add(new Pair(8900100, 30032));
         list.add(new Pair(8900101, 30032));
         list.add(new Pair(8900102, 30032));
         list.add(new Pair(8900103, 0));
         list.add(new Pair(8910000, 30044));
         list.add(new Pair(8910100, 30039));
         list.add(new Pair(8920000, 30045));
         list.add(new Pair(8920001, 30045));
         list.add(new Pair(8920002, 30045));
         list.add(new Pair(8920003, 30045));
         list.add(new Pair(8920006, 30045));
         list.add(new Pair(8920100, 30033));
         list.add(new Pair(8920101, 30033));
         list.add(new Pair(8920102, 30033));
         list.add(new Pair(8920103, 30033));
         list.add(new Pair(8920106, 30033));
         list.add(new Pair(8930000, 30046));
         list.add(new Pair(8930100, 30041));
         list.add(new Pair(8950000, 33261));
         list.add(new Pair(8950001, 33262));
         list.add(new Pair(8950002, 33263));
         list.add(new Pair(8950100, 33301));
         list.add(new Pair(8950101, 33302));
         list.add(new Pair(8950102, 33303));
         list.add(new Pair(9101078, 15172));
         list.add(new Pair(9101190, 0));
         list.add(new Pair(9309200, 0));
         list.add(new Pair(9309201, 0));
         list.add(new Pair(9309203, 0));
         list.add(new Pair(9309205, 0));
         list.add(new Pair(9309207, 0));
         c.send(CField.BossMatchingChance(list));
         c.getSession().writeAndFlush(CSPacket.enableCSUse());
         player.updateLinkSkillPacket();
         player.silentGiveBuffs(PlayerBuffStorage.getBuffsFromStorage(player.getId()));
         int[] ZeroQuest;
         int[] arrayOfInt1;
         if (player.getCooldownSize() > 0) {
            ZeroQuest = new int[]{80002282, 2321055, 2023661, 2023662, 2023663, 2023664, 2023665, 2023666, 2450064, 2450134, 2450038, 2450124, 2450147, 2450148, 2450149, 2023558, 2003550, 2023556, 2003551};
            arrayOfInt1 = ZeroQuest;
            k = ZeroQuest.length;

            for(byte b = 0; b < k; ++b) {
               Integer a = arrayOfInt1[b];
               if (!player.getBuffedValue(a)) {
                  Iterator var12 = player.getCooldowns().iterator();

                  while(var12.hasNext()) {
                     MapleCoolDownValueHolder mapleCoolDownValueHolder = (MapleCoolDownValueHolder)var12.next();
                     if (mapleCoolDownValueHolder.skillId == a) {
                        if (MapleItemInformationProvider.getInstance().getItemEffect(a) != null) {
                           MapleItemInformationProvider.getInstance().getItemEffect(a).applyTo(player, false, (int)(mapleCoolDownValueHolder.length + mapleCoolDownValueHolder.startTime - System.currentTimeMillis()));
                        } else {
                           SkillFactory.getSkill(a).getEffect(player.getSkillLevel(a)).applyTo(player, false, (int)(mapleCoolDownValueHolder.length + mapleCoolDownValueHolder.startTime - System.currentTimeMillis()));
                        }
                        break;
                     }
                  }
               }
            }
         }

         if (player.returnscroll != null) {
            c.getSession().writeAndFlush(CWvsContext.returnEffectConfirm(player.returnscroll, player.returnSc));
            c.getSession().writeAndFlush(CWvsContext.returnEffectModify(player.returnscroll, player.returnSc));
         }

         if (player.choicepotential != null && player.memorialcube != null) {
            c.getSession().writeAndFlush(CField.getBlackCubeStart(player, player.choicepotential, false, player.memorialcube.getItemId(), player.memorialcube.getPosition(), player.getItemQuantity(5062010, false)));
         }

         if (GameConstants.isBlaster(player.getJob())) {
            player.Cylinder(0);
         }

         if (player.isGM() && !player.getBuffedValue(9001004)) {
         }

         if (GameConstants.isZero(player.getJob())) {
            ZeroQuest = new int[]{31686, 31198, 41908, 41909, 41907, 32550, 33565, 3994, 6000, 39001, 40000, 40001, 7049, 40002, 40003, 40004, 40100, 40101, 6995, 40102, 40103, 40104, 40105, 40106, 40107, 40200, 40108, 40201, 40109, 40202, 40110, 40203, 40111, 40204, 40050, 40112, 40205, 40051, 40206, 40052, 40207, 40300, 40704, 40053, 40208, 40301, 7783, 40054, 40209, 40302, 40705, 40055, 40210, 40303, 40056, 40304, 7600, 40800, 40057, 40305, 40801, 40058, 40306, 40059, 40307, 40400, 40060, 40308, 40401, 40061, 40309, 40402, 40960, 40062, 40310, 40403, 40930, 40961, 40063, 40404, 40900, 40931, 40962, 40405, 7887, 40901, 40932, 40963, 40406, 40902, 40933, 40964, 40407, 40500, 40903, 40934, 40408, 40501, 40904, 40409, 40502, 7860, 40905, 40503, 7892, 7707, 40504, 40505, 40970, 40506, 40940, 40971, 41250, 41312, 40600, 40910, 40941, 40972, 41251, 40601, 40911, 40942, 40973, 41252, 40602, 40912, 40943, 40974, 41253, 41315, 41408, 40603, 40913, 40944, 41254, 41316, 40604, 40914, 41255, 41317, 40605, 41256, 40606, 41257, 41350, 40607, 40700, 41103, 41258, 41351, 40701, 40980, 41104, 41352, 40702, 40950, 41105, 41353, 40703, 40920, 40951, 41106, 41261, 41354, 40921, 40952, 40922, 40953, 41263, 40923, 40954, 41264, 41357, 40924, 41358, 41111, 41359, 41050, 41360, 41114, 41269, 41300, 41115, 41270, 41301, 41363, 41302, 41364, 41303, 41365, 41055, 41304, 41366, 41305, 41925, 41306, 41926, 41307, 41400, 41370, 41401};
            arrayOfInt1 = ZeroQuest;
            k = ZeroQuest.length;

            for(int var40 = 0; var40 < k; ++var40) {
               int questid = arrayOfInt1[var40];
               if (questid != 41907 && player.getQuestStatus(questid) != 2) {
                  MapleQuest.getInstance(questid).forceComplete(player, 0);
               }

               if (questid == 41907 && player.getQuestStatus(questid) != 1) {
                  MapleQuest quest = MapleQuest.getInstance(41907);
                  MapleQuestStatus queststatus = new MapleQuestStatus(quest, 1);
                  queststatus.setCustomData("0");
                  player.updateQuest(queststatus, true);
               }
            }

            if (player.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11) != null) {
               Equip eq = (Equip)player.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
               Equip eq2 = (Equip)player.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
               if (eq.getPotential1() != eq2.getPotential1()) {
                  eq.setPotential1(eq2.getPotential1());
               }

               if (eq.getPotential2() != eq2.getPotential2()) {
                  eq.setPotential2(eq2.getPotential2());
               }

               if (eq.getPotential3() != eq2.getPotential3()) {
                  eq.setPotential3(eq2.getPotential3());
               }

               if (eq.getPotential4() != eq2.getPotential4()) {
                  eq.setPotential4(eq2.getPotential4());
               }

               if (eq.getPotential5() != eq2.getPotential5()) {
                  eq.setPotential5(eq2.getPotential5());
               }

               if (eq.getPotential6() != eq2.getPotential6()) {
                  eq.setPotential6(eq2.getPotential6());
               }
            }
         }

         player.addKV("bossPractice", "0");
         if (player.getKeyValue(1477, "count") == -1L) {
            player.setKeyValue(1477, "count", "0");
         }

         if (player.getKeyValue(19019, "id") == -1L) {
            player.setKeyValue(19019, "id", "0");
         }

         if (player.getKeyValue(7293, "damage_skin") == -1L) {
            player.setKeyValue(7293, "damage_skin", "2438159");
         }

         if (player.getKeyValue(501619, "count") == -1L) {
            player.setKeyValue(501619, "count", "999");
         }

         if (player.getKeyValue(100711, "point") == -1L) {
            player.setKeyValue(100711, "point", "0");
            player.setKeyValue(100711, "sum", "0");
            player.setKeyValue(100711, "date", String.valueOf(GameConstants.getCurrentDate_NoTime()));
            player.setKeyValue(100711, "today", "0");
            player.setKeyValue(100711, "total", "0");
            player.setKeyValue(100711, "lock", "0");
         }

         if (player.getKeyValue(100711, "date") != (long)GameConstants.getCurrentDate_NoTime()) {
            player.setKeyValue(100711, "date", String.valueOf(GameConstants.getCurrentDate_NoTime()));
            player.setKeyValue(100711, "today", "0");
            player.setKeyValue(100711, "lock", "0");
         }

         if (player.getKeyValue(100712, "point") == -1L) {
            player.setKeyValue(100712, "point", "0");
            player.setKeyValue(100712, "sum", "0");
            player.setKeyValue(100712, "date", String.valueOf(GameConstants.getCurrentDate_NoTime()));
            player.setKeyValue(100712, "today", "0");
            player.setKeyValue(100712, "total", "0");
            player.setKeyValue(100712, "lock", "0");
         }

         if (player.getKeyValue(16700, "date") != (long)GameConstants.getCurrentDate_NoTime()) {
            player.setKeyValue(16700, "count", "0");
            player.setKeyValue(16700, "date", String.valueOf(GameConstants.getCurrentDate_NoTime()));
         }

         if (player.getKeyValue(100712, "date") != (long)GameConstants.getCurrentDate_NoTime()) {
            player.setKeyValue(100712, "date", String.valueOf(GameConstants.getCurrentDate_NoTime()));
            player.setKeyValue(100712, "today", "0");
            player.setKeyValue(100712, "lock", "0");
         }

         if (player.getKeyValue(501215, "point") == -1L) {
            player.setKeyValue(501215, "point", "0");
            player.setKeyValue(501215, "sum", "0");
            player.setKeyValue(501215, "date", String.valueOf(GameConstants.getCurrentDate_NoTime()));
            player.setKeyValue(501215, "week", "0");
            player.setKeyValue(501215, "total", "0");
            player.setKeyValue(501215, "lock", "0");
         }

         if (player.getKeyValue(501045, "point") == -1L) {
            player.setKeyValue(501045, "point", "0");
            player.setKeyValue(501045, "lv", "1");
            player.setKeyValue(501045, "sp", "0");
            player.setKeyValue(501045, "reward0", "0");
            player.setKeyValue(501045, "reward1", "0");
            player.setKeyValue(501045, "reward2", "0");
            player.setKeyValue(501045, "mapTuto", "2");
            player.setKeyValue(501045, "skillTuto", "1");
            player.setKeyValue(501045, "payTuto", "1");
         }

         player.setKeyValue(501092, "lv", "9");
         if (player.getKeyValue(501046, "start") == -1L) {
            player.setKeyValue(501046, "start", "1");

            for(k = 0; k < 9; ++k) {
               player.setKeyValue(501046, String.valueOf(k), "0");
            }
         }

         if (c.getKeyValue("dailyGiftDay") == null) {
            c.setKeyValue("dailyGiftDay", "0");
         }

         if (c.getKeyValue("dailyGiftComplete") == null) {
            c.setKeyValue("dailyGiftComplete", "0");
         }

         if (player.getKeyValue(501385, "date") != (long)GameConstants.getCurrentDate_NoTime()) {
            player.setKeyValue(501385, "count", "0");
            player.setKeyValue(501385, "date", String.valueOf(GameConstants.getCurrentDate_NoTime()));
         }

         Iterator var42 = AuctionServer.getItems().values().iterator();

         while(var42.hasNext()) {
            AuctionItem auctionItem = (AuctionItem)var42.next();
            if (auctionItem.getAccountId() == c.getAccID() && auctionItem.getState() == 3 && auctionItem.getHistory().getState() == 3) {
               player.getClient().getSession().writeAndFlush(CWvsContext.AlarmAuction(0, auctionItem));
            }
         }

         if (player.getKeyValue(19019, "id") > 0L && !player.haveItem((int)player.getKeyValue(19019, "id"))) {
            player.setKeyValue(19019, "id", "0");
            player.getMap().broadcastMessage(player, CField.showTitle(player.getId(), 0), false);
         }

         if (player.getKeyValue(210416, "TotalDeadTime") > 0L) {
            player.getClient().send(CField.ExpDropPenalty(true, (int)player.getKeyValue(210416, "TotalDeadTime"), (int)player.getKeyValue(210416, "NowDeadTime"), 80, 80));
         }

         short questid;
         MapleQuest quest;
         MapleQuestStatus queststatus;
         if (player.getClient().getKeyValue("UnionQuest1") != null) {
            questid = 16011;
            if (Integer.parseInt(player.getClient().getKeyValue("UnionQuest1")) == 1) {
               player.forceCompleteQuest(16011);
            } else {
               quest = MapleQuest.getInstance(questid);
               player.getQuest_Map().remove(quest);
               queststatus = new MapleQuestStatus(quest, 0);
               queststatus.setStatus((byte)0);
               queststatus.setCustomData("");
               player.getClient().send(CWvsContext.InfoPacket.updateQuest(queststatus));
            }
         }

         if (player.getClient().getKeyValue("UnionQuest2") != null) {
            questid = 16012;
            if (Integer.parseInt(player.getClient().getKeyValue("UnionQuest2")) == 1) {
               player.forceCompleteQuest(questid);
            } else {
               quest = MapleQuest.getInstance(questid);
               player.getQuest_Map().remove(quest);
               queststatus = new MapleQuestStatus(quest, 0);
               queststatus.setStatus((byte)0);
               queststatus.setCustomData("");
               player.getClient().send(CWvsContext.InfoPacket.updateQuest(queststatus));
            }
         }

         boolean itemexppendent = false;
         KoreaCalendar kc = new KoreaCalendar();
         int var95 = kc.getYeal() % 100;
         String nowtime = var95 + kc.getMonths() + kc.getDays() + kc.getHours() + kc.getMins() + kc.getMins();
         Item item = player.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-17);
         Item item2 = player.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-31);
         Item exppen = null;
         if (item != null) {
            itemexppendent = item.getItemId() == 1122017 || item.getItemId() == 1122155 || item.getItemId() == 1122215;
            if (itemexppendent) {
               exppen = item;
            }
         }

         if (item2 != null && !itemexppendent) {
            itemexppendent = item2.getItemId() == 1122017 || item2.getItemId() == 1122155 || item2.getItemId() == 1122215;
            if (itemexppendent) {
               exppen = item2;
            }
         }

         int id;
         int linkSkill;
         long var10002;
         if (itemexppendent) {
            if ((long)kc.getDayt() == player.getKeyValue(27040, "equipday") && (long)kc.getMonth() == player.getKeyValue(27040, "equipmonth")) {
               long runnigtime = player.getKeyValue(27040, "runnigtime");
               id = (int)runnigtime / 3600;
               if (id >= 2) {
                  id = 2;
               }

               int expplus = id == 2 ? 30 : (id == 1 ? 20 : 10);
               linkSkill = (int)runnigtime / 60;
               long outtime = (System.currentTimeMillis() - player.getKeyValue(27040, "firstequiptimemil")) / 1000L - player.getKeyValue(27040, "runnigtime");
               var10002 = exppen.getInventoryId();
               player.updateInfoQuest(27039, var10002 + "=" + player.getKeyValue(27040, "firstequiptime") + "|" + nowtime + "|" + outtime + "|0|0");
               player.getClient().send(CWvsContext.SpritPandent(exppen.getPosition(), true, id, expplus, linkSkill));
               var10002 = exppen.getInventoryId();
               player.updateInfoQuest(27039, var10002 + "=" + player.getKeyValue(27040, "firstequiptime") + "|" + nowtime + "|" + outtime + "|" + expplus + "|" + linkSkill);
            } else {
               player.removeKeyValue(27040);
               player.setKeyValue(27040, "runnigtime", "1");
               player.setKeyValue(27040, "firstequiptime", nowtime);
               player.setKeyValue(27040, "firstequiptimemil", System.currentTimeMillis().makeConcatWithConstants<invokedynamic>(System.currentTimeMillis()));
               player.setKeyValue(27040, "equipday", kc.getDayt().makeConcatWithConstants<invokedynamic>(kc.getDayt()));
               player.setKeyValue(27040, "equipmonth", kc.getMonth().makeConcatWithConstants<invokedynamic>(kc.getMonth()));
               var10002 = exppen.getInventoryId();
               player.updateInfoQuest(27039, var10002 + "=" + nowtime + "|" + nowtime + "|0|0|0");
               player.getClient().send(CWvsContext.SpritPandent(exppen.getPosition(), true, 0, 10, 0));
               var10002 = exppen.getInventoryId();
               player.updateInfoQuest(27039, var10002 + "=" + nowtime + "|" + nowtime + "|0|10|0");
            }
         }

         String var99;
         if (player.getV("EnterDay") == null) {
            var99 = kc.getYears();
            player.addKV("EnterDay", var99 + kc.getMonths() + kc.getDays());
         } else {
            player.checkRestDay(false, false);
         }

         if (player.getV("EnterDayWeek") == null) {
            var99 = kc.getYears();
            player.addKV("EnterDayWeek", var99 + kc.getMonths() + kc.getDays());
         } else {
            player.checkRestDay(true, false);
         }

         if (player.getV("EnterDayWeekMonday") == null) {
            var99 = kc.getYears();
            player.addKV("EnterDayWeekMonday", var99 + kc.getMonths() + kc.getDays());
         } else {
            player.checkRestDayMonday();
         }

         MapleClient var96;
         if (player.getClient().getKeyValue("EnterDay") == null) {
            var96 = player.getClient();
            var99 = kc.getYears();
            var96.setKeyValue("EnterDay", var99 + kc.getMonths() + kc.getDays());
         } else {
            player.checkRestDay(false, true);
         }

         if (player.getClient().getKeyValue("EnterDayWeek") == null) {
            var96 = player.getClient();
            var99 = kc.getYears();
            var96.setKeyValue("EnterDayWeek", var99 + kc.getMonths() + kc.getDays());
         } else {
            player.checkRestDay(true, true);
         }

         int coin;
         if (player.getClient().getKeyValue("WishCoin") == null) {
            String bosslist = "";

            for(coin = 0; coin < ServerConstants.NeoPosList.size(); ++coin) {
               bosslist = bosslist + "0";
            }

            player.getClient().setKeyValue("WishCoin", bosslist);
            player.getClient().setKeyValue("WishCoinWeekGain", "0");
            player.getClient().setKeyValue("WishCoinGain", "0");
         }

         c.setCabiNet(new ArrayList());
         c.loadCabinet();
         if (!c.getCabiNet().isEmpty()) {
            c.send(CField.getMapleCabinetList(c.getCabiNet(), false, 0, true));
         }

         List<Triple<Integer, Integer, String>> eventInfo = new ArrayList();
         eventInfo.add(new Triple(100662, 993187300, "HundredShooting"));
         eventInfo.add(new Triple(100661, 993074000, "jumping"));
         eventInfo.add(new Triple(100796, 993192500, "BloomingRace"));
         eventInfo.add(new Triple(100199, 993026900, "NewYear"));
         c.send(SLFCGPacket.EventInfoPut(eventInfo));
         c.setShopLimit(new ArrayList());
         c.loadShopLimit();
         player.RefreshUnionRaid(true);
         if (player.getClient().getKeyValue("유니온코인") != null) {
            coin = Integer.parseInt(player.getClient().getKeyValue("유니온코인"));
            if ((long)coin != player.getKeyValue(500629, "point")) {
               player.setKeyValue(500629, "point", coin.makeConcatWithConstants<invokedynamic>(coin));
            }
         }

         if (player.getClient().getKeyValue("presetNo") != null) {
            coin = Integer.parseInt(player.getClient().getKeyValue("presetNo"));
            if ((long)coin != player.getKeyValue(500630, "presetNo")) {
               player.setKeyValue(500630, "presetNo", coin.makeConcatWithConstants<invokedynamic>(coin));
            }
         }

         for(coin = 3; coin < 6; ++coin) {
            if (player.getClient().getKeyValue("prisetOpen" + coin) != null) {
               id = Integer.parseInt(player.getClient().getKeyValue("prisetOpen" + coin));
               if (id == 1) {
                  player.SetUnionPriset(coin);
               }
            }
         }

         for(coin = 0; coin < 8; ++coin) {
            id = (int)(500627L + player.getKeyValue(500630, "presetNo"));
            if (player.getKeyValue(18791, coin.makeConcatWithConstants<invokedynamic>(coin)) == -1L && player.getClient().getCustomData(id, coin.makeConcatWithConstants<invokedynamic>(coin)) != null) {
               player.setKeyValue(id, coin.makeConcatWithConstants<invokedynamic>(coin), player.getClient().getCustomData(id, coin.makeConcatWithConstants<invokedynamic>(coin)));
            }
         }

         player.gainEmoticon(1008);
         player.gainEmoticon(1009);
         player.gainEmoticon(1010);
         if (player.getKeyValue(51351, "startquestid") > 0L) {
            player.gainSuddenMission((int)player.getKeyValue(51351, "startquestid"), (int)player.getKeyValue(51351, "midquestid"), false);
         }

         int a;
         int j;
         int sm;
         int m;
         int check;
         int k;
         if (player.getV("GuildBless") != null) {
            long keys = Long.parseLong(player.getV("GuildBless"));
            Calendar clear = new GregorianCalendar((int)keys / 10000, (int)(keys % 10000L / 100L) - 1, (int)keys % 100);
            Calendar ocal = Calendar.getInstance();
            k = clear.get(1);
            a = clear.get(5);
            j = clear.get(7);
            sm = clear.getMaximum(5);
            m = clear.get(2);
            check = j == 7 ? 2 : (j == 6 ? 3 : (j == 5 ? 4 : (j == 4 ? 5 : (j == 3 ? 6 : (j == 2 ? 7 : (j == 1 ? 1 : 0))))));
            int afterday = a + check;
            if (afterday > sm) {
               afterday -= sm;
               ++m;
            }

            if (m > 12) {
               ++k;
               m = 1;
            }

            Calendar after = new GregorianCalendar(k, m, afterday);
            if (after.getTimeInMillis() < System.currentTimeMillis()) {
               MapleQuest quest = MapleQuest.getInstance(26000);
               player.getQuest_Map().remove(quest);
               MapleQuestStatus queststatus = new MapleQuestStatus(quest, 0);
               queststatus.setStatus((byte)0);
               queststatus.setCustomData("");
               player.getClient().send(CWvsContext.InfoPacket.updateQuest(queststatus));
            }
         }

         if (!ServerConstants.feverTime && Calendar.getInstance().get(7) != 7) {
            ServerConstants.feverTime = false;
         } else {
            ServerConstants.feverTime = true;
            player.FeverTime(true, false);
         }

         if (player.getClient().getCustomData(501368, "spoint") != null) {
            coin = Integer.parseInt(player.getClient().getCustomData(501368, "spoint"));
            if ((long)coin != player.getKeyValue(501368, "point")) {
               player.setKeyValue(501368, "point", coin.makeConcatWithConstants<invokedynamic>(coin));
            }
         }

         c.getPlayer().loadPremium();
         StringBuilder sb3 = new StringBuilder();
         sb3.append(CurrentTime.getYear());
         sb3.append(StringUtil.getLeftPaddedStr(String.valueOf(CurrentTime.getMonth()), '0', 2));
         sb3.append(StringUtil.getLeftPaddedStr(String.valueOf(CurrentTime.getDate()), '0', 2));
         if (player.haveItem(2438697)) {
            if (player.getV("d_day_t") == null) {
               player.addKV("d_day_t", "0");
            }

            if (player.getV("d_daycheck") == null) {
               player.addKV("d_daycheck", "0");
            }

            if (Long.parseLong(player.getV("d_day_t")) < Long.parseLong(sb3.toString())) {
               player.addKV("d_day_t", sb3.toString());
               var10002 = Long.parseLong(player.getV("d_daycheck"));
               player.addKV("d_daycheck", (var10002 + 1L).makeConcatWithConstants<invokedynamic>(var10002 + 1L));
            }
         }

         String bTime;
         if (GameConstants.isYeti(player.getJob()) || GameConstants.isPinkBean(player.getJob())) {
            MapleQuest quest = MapleQuest.getInstance(7291);
            MapleQuestStatus queststatus = new MapleQuestStatus(quest, 1);
            bTime = String.valueOf(GameConstants.isPinkBean(player.getJob()) ? 292 : 293);
            queststatus.setCustomData(bTime == null ? "0" : bTime);
            player.updateQuest(queststatus, true);
            player.setKeyValue(7293, "damage_skin", GameConstants.isPinkBean(player.getJob()) ? "2633220" : "2633218");
            if (player.getSkillLevel(80000602) <= 0) {
               player.changeSkillLevel(80000602, (byte)1, (byte)1);
            }
         }

         Item weapon = player.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
         if (weapon != null && player.getBuffedEffect(SecondaryStat.SoulMP) == null) {
            player.setSoulMP((Equip)weapon);
         }

         boolean cgr = false;
         Iterator var70 = World.Guild.getGuilds(1, 999, 1, 999, 1, 999).iterator();

         while(var70.hasNext()) {
            MapleGuild mg = (MapleGuild)var70.next();
            if (mg.getRequest(player.getId()) != null) {
               c.getPlayer().setKeyValue(26015, "name", mg.getName());
               cgr = true;
            }
         }

         if (!cgr || c.getPlayer().getGuild() != null) {
            c.getPlayer().setKeyValue(26015, "name", "");
         }

         if (player.getKeyValue(333333, "quick0") > 0L) {
            c.getSession().writeAndFlush(CField.quickSlot(player));
         }

         if (c.getCustomData(252, "count") == null) {
            c.setCustomData(252, "count", "0");
         }

         if (c.getCustomData(252, "T") != null && !c.getCustomData(252, "T").equals("0")) {
            bTime = c.getCustomData(252, "T");
            String cTime = GameConstants.getCurrentFullDate();
            a = Integer.parseInt(bTime.substring(8, 10));
            j = Integer.parseInt(bTime.substring(10, 12));
            sm = Integer.parseInt(cTime.substring(8, 10));
            m = Integer.parseInt(cTime.substring(10, 12));
            if (sm - a == 1 && m >= j || sm - a > 1) {
               c.setCustomData(252, "count", "3600");
            }
         } else {
            c.setCustomData(252, "count", "0");
            c.setCustomData(252, "T", GameConstants.getCurrentFullDate());
         }

         if (c.getCustomData(253, "day") == null) {
            c.setCustomData(253, "day", "0");
         }

         if (c.getCustomData(253, "complete") == null) {
            c.setCustomData(253, "complete", "0");
         }

         if (c.getCustomData(253, "bMaxDay") == null) {
            c.setCustomData(253, "bMaxDay", "135");
         }

         if (c.getCustomData(253, "cMaxDay") == null) {
            c.setCustomData(253, "cMaxDay", "135");
         }

         if (c.getCustomData(253, "lastDate") == null) {
            c.setCustomData(253, "lastDate", "20/12/31");
         }

         if (c.getCustomData(253, "passCount") == null) {
            c.setCustomData(253, "passCount", "135");
         }

         if (player.getKeyValue(0, "Boss_Level") < 0L) {
            player.setKeyValue(0, "Boss_Level", "0");
         }

         if (player.getKeyValue(100, "medal") < 0L) {
            player.setKeyValue(100, "medal", "1");
         }

         if (player.getKeyValue(100, "title") < 0L) {
            player.setKeyValue(100, "title", "1");
         }

         var10002 = player.getKeyValue(100, "medal");
         player.updateInfoQuest(101149, "1007=" + var10002 + ";1009=" + player.getKeyValue(100, "title"));
         if (player.getKeyValue(3, "dojo") < 0L) {
            player.setKeyValue(3, "dojo", "0");
         }

         if (player.getKeyValue(3, "dojo_time") < 0L) {
            player.setKeyValue(3, "dojo_time", "0");
         }

         if (c.getCustomData(254, "passDate") == null) {
            StringBuilder str = new StringBuilder();

            for(k = 0; k < 63; ++k) {
               str.append("0");
            }

            c.setCustomData(254, "passDate", str.toString());
         }

         MatrixHandler.calcSkillLevel(player, -1);
         linkSkill = GameConstants.getMyLinkSkill(player.getJob());
         if (linkSkill > 0 && player.getSkillLevel(linkSkill) != (player.getLevel() >= 120 ? 2 : 1)) {
            player.changeSkillLevel(linkSkill, (byte)(player.getLevel() >= 120 ? 2 : 1), (byte)2);
         }

         if (c.getCustomData(238, "T") == null || c.getCustomData(238, "T").equals("0")) {
            c.setCustomData(238, "count", "0");
            c.setCustomData(238, "T", GameConstants.getCurrentFullDate());
         }

         if (GameConstants.isWildHunter(player.getJob())) {
            boolean change = false;
            a = 9304000;

            while(true) {
               if (a > 9304008) {
                  if (change) {
                     c.getSession().writeAndFlush(CWvsContext.updateJaguar(player));
                  }

                  if (player.getKeyValue(190823, "grade") == 0L) {
                     player.setKeyValue(190823, "grade", "1");
                  }
                  break;
               }

               j = GameConstants.getJaguarType(a);
               String info = player.getInfoQuest(23008);

               for(m = 0; m <= 8; ++m) {
                  if (!info.contains(m + "=1") && m == j) {
                     info = info + m + "=1;";
                  }
               }

               player.updateInfoQuest(23008, info);
               player.updateInfoQuest(123456, String.valueOf(j * 10));
               change = true;
               ++a;
            }
         }

         MapleQuestStatus stat = player.getQuestNoAdd(MapleQuest.getInstance(122700));
         c.getSession().writeAndFlush(CWvsContext.pendantSlot(true));
         c.getSession().writeAndFlush(CWvsContext.temporaryStats_Reset());
         player.getMap().addPlayer(player);
         c.getSession().writeAndFlush(CWvsContext.setBossReward(player));
         c.getSession().writeAndFlush(CWvsContext.onSessionValue("kill_count", "0"));
         Channel var97 = c.getSession();
         String var98 = c.getKeyValue("dailyGiftComplete");
         var97.writeAndFlush(CWvsContext.updateDailyGift("count=" + var98 + ";date=" + player.getKeyValue(16700, "date")));
         c.getSession().writeAndFlush(CField.dailyGift(player, 1, 0));

         try {
            int[] buddyIds = player.getBuddylist().getBuddyIds();
            World.Buddy.loggedOn(player.getName(), player.getId(), c.getChannel(), c.getAccID(), buddyIds);
            if (player.getParty() != null) {
               MapleParty party = player.getParty();
               World.Party.updateParty(party.getId(), PartyOperation.LOG_ONOFF, new MaplePartyCharacter(player));
            }

            AccountIdChannelPair[] onlineBuddies = World.Find.multiBuddyFind(player.getBuddylist(), player.getId(), buddyIds);
            AccountIdChannelPair[] var82 = onlineBuddies;
            m = onlineBuddies.length;

            for(check = 0; check < m; ++check) {
               AccountIdChannelPair onlineBuddy = var82[check];
               player.getBuddylist().get(onlineBuddy.getAcountId()).setChannel(onlineBuddy.getChannel());
            }

            player.getBuddylist().setChanged(true);
            c.getSession().writeAndFlush(CWvsContext.BuddylistPacket.updateBuddylist(player.getBuddylist().getBuddies(), (BuddylistEntry)null, (byte)21));
            MapleMessenger messenger = player.getMessenger();
            if (messenger != null) {
               World.Messenger.silentJoinMessenger(messenger.getId(), new MapleMessengerCharacter(c.getPlayer()));
               World.Messenger.updateMessenger(messenger.getId(), c.getPlayer().getName(), c.getChannel());
            }

            if (player.getGuildId() > 0) {
               MapleGuild gs = World.Guild.getGuild(player.getGuildId());
               if (gs != null) {
                  if (gs.getLastResetDay() == 0) {
                     var98 = kc.getYears();
                     gs.setLastResetDay(Integer.parseInt(var98 + kc.getMonths() + kc.getDays()));
                  }

                  World.Guild.setGuildMemberOnline(player.getMGC(), true, c.getChannel());
                  c.getSession().writeAndFlush(CWvsContext.GuildPacket.showGuildInfo(player));
                  c.getSession().writeAndFlush(CWvsContext.GuildPacket.guildLoadAattendance());
                  List<byte[]> packetList = World.Alliance.getAllianceInfo(gs.getAllianceId(), true);
                  if (packetList != null) {
                     Iterator var91 = packetList.iterator();

                     while(var91.hasNext()) {
                        byte[] pack = (byte[])var91.next();
                        if (pack != null) {
                           c.getSession().writeAndFlush(pack);
                        }
                     }
                  }
               } else {
                  player.setGuildId(0);
                  player.setGuildRank((byte)5);
                  player.setAllianceRank((byte)5);
                  player.saveGuildStatus();
               }
            }
         } catch (Exception var28) {
            var28.printStackTrace();
         }

         CharacterNameAndId pendingBuddyRequest = player.getBuddylist().pollPendingRequest();
         if (pendingBuddyRequest != null) {
            player.getBuddylist().put(new BuddylistEntry(pendingBuddyRequest.getName(), pendingBuddyRequest.getRepName(), pendingBuddyRequest.getAccId(), pendingBuddyRequest.getId(), pendingBuddyRequest.getGroupName(), -1, false, pendingBuddyRequest.getLevel(), pendingBuddyRequest.getJob(), pendingBuddyRequest.getMemo()));
            c.getSession().writeAndFlush(CWvsContext.BuddylistPacket.requestBuddylistAdd(pendingBuddyRequest.getId(), pendingBuddyRequest.getAccId(), pendingBuddyRequest.getName(), pendingBuddyRequest.getLevel(), pendingBuddyRequest.getJob(), c, pendingBuddyRequest.getGroupName(), pendingBuddyRequest.getMemo()));
         }

         player.getClient().getSession().writeAndFlush(CWvsContext.serverMessage("", channelServer.getServerMessage()));
         player.sendMacros();
         player.updatePartyMemberHP();
         player.startFairySchedule(false);
         player.gainDonationSkills();
         c.getSession().writeAndFlush(CField.getKeymap(player.getKeyLayout()));
         c.getSession().writeAndFlush(CWvsContext.OnClaimSvrStatusChanged(true));
         player.updatePetAuto();
         player.expirationTask(true, transfer == null);
         c.getSession().writeAndFlush(CWvsContext.setUnion(c));
         player.getStat().recalcLocalStats(player);

         for(j = 0; j < 5; ++j) {
            c.getSession().writeAndFlush(CWvsContext.unionFreeset(c, j));
         }

         c.getPlayer().updateSingleStat(MapleStat.FATIGUE, (long)c.getPlayer().getFatigue());
         if (player.getStat().equippedSummon > 0) {
            SkillFactory.getSkill(player.getStat().equippedSummon).getEffect(1).applyTo(player, true);
         }

         c.getSession().writeAndFlush(CField.HeadTitle(player.HeadTitle()));
         PetHandler.updatePetSkills(player, (MaplePet)null);
         c.getSession().writeAndFlush(CWvsContext.initSecurity());
         c.getSession().writeAndFlush(CWvsContext.updateSecurity());
         String towerchair = c.getPlayer().getInfoQuest(7266);
         if (!towerchair.equals("")) {
            c.getPlayer().updateInfoQuest(7266, towerchair);
         }

         c.getSession().writeAndFlush(SLFCGPacket.StarDustUI("UI/UIWindowEvent.img/2020neoCoin", 1L, 1L, false));
         if (player.getClient().isFirstLogin() && !player.isGM() && !player.getName().equals("오브")) {
            player.getClient().setLogin(false);
         }

         if (c.getPlayer().getPremiumPeriod() > LocalDateTime.now().toInstant(ZoneOffset.UTC).getEpochSecond()) {
            Skill skill = SkillFactory.getSkill(c.getPlayer().getPremiumBuff());
            if (skill != null) {
               if (player.getSkillLevel(skill) < 1) {
                  c.getPlayer().changeSingleSkillLevel(skill, skill.getMaxLevel(), (byte)skill.getMasterLevel());
               }

               long td = (c.getPlayer().getPremiumPeriod() - LocalDateTime.now().toInstant(ZoneOffset.UTC).getEpochSecond()) / 86400000L;
               c.getPlayer().dropMessage(5, "프리미엄 버프가 " + td + "일 남았습니다.");
               SkillFactory.getSkill(c.getPlayer().getPremiumBuff()).getEffect(1).applyTo(player, false);
            }
         }

         if (player.getKeyValue(501045, "point") == -1L) {
            player.setKeyValue(501045, "point", "0");
            player.setKeyValue(501045, "lv", "1");
            player.setKeyValue(501045, "sp", "0");
            player.setKeyValue(501045, "reward0", "0");
            player.setKeyValue(501045, "reward1", "0");
            player.setKeyValue(501045, "reward2", "0");
            player.setKeyValue(501045, "mapTuto", "2");
            player.setKeyValue(501045, "skillTuto", "1");
            player.setKeyValue(501045, "payTuto", "1");
         }

         if (player.getKeyValue(501092, "point") == -1L) {
            player.setKeyValue(501092, "point", "0");
            player.setKeyValue(501092, "sp", "0");
            player.setKeyValue(501092, "mapTuto", "2");
            player.setKeyValue(501092, "skillTuto", "1");
            player.setKeyValue(501092, "payTuto", "1");
         }

         for(sm = 0; sm <= 7; ++sm) {
            if (c.getPlayer().getKeyValue(2018207, "medalSkill_" + sm) == 1L) {
               Skill skill = SkillFactory.getSkill(80001535 + sm);
               if (skill != null) {
                  if (player.getSkillLevel(skill) < 1) {
                     c.getPlayer().changeSingleSkillLevel(skill, skill.getMaxLevel(), (byte)skill.getMasterLevel());
                  }

                  SkillFactory.getSkill(80001535 + sm).getEffect(1).applyTo(player, false);
               }
            }
         }

         int[][] medals = new int[][]{{1143507, 80001543}, {1143508, 80001544}, {1143509, 80001545}};

         for(m = 0; m < medals.length; ++m) {
            if (c.getPlayer().haveItem(medals[m][0])) {
               Skill skill = SkillFactory.getSkill(medals[m][1]);
               if (skill != null) {
                  if (player.getSkillLevel(skill) < 1) {
                     c.getPlayer().changeSingleSkillLevel(skill, skill.getMaxLevel(), (byte)skill.getMasterLevel());
                  }

                  SkillFactory.getSkill(medals[m][1]).getEffect(1).applyTo(player, false);
               }
            }
         }

         player.changeSkillLevel(80003023, (byte)1, (byte)1);
         if (c.getPlayer().getKeyValue(20210113, "orgelonoff") == 1L) {
            c.getPlayer().setKeyValue(20210113, "orgelonoff", "0");
            c.getPlayer().updateInfoQuest(100720, "count=0;fever=0;");
         }

         player.getClient().send(CField.UIPacket.closeUI(3));
         c.getSession().writeAndFlush(SLFCGPacket.SetIngameDirectionMode(true, false, false, false));
         c.getSession().writeAndFlush(SLFCGPacket.SetIngameDirectionMode(false, true, false, false));
         if (ServerConstants.ServerTest && !player.haveItem(2431138)) {
            player.gainItem(2431138, 1);
         }

         if ((new Date()).getHours() == 19 && (new Date()).getMinutes() < 55 || (new Date()).getHours() == 22 && (new Date()).getMinutes() < 55) {
            player.getClient().getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(2012041, 3500, "팀 대항전이 시작되었다네 참여를 하고 싶으면 나에게 말을 걸어 주게나.", ""));
         }
      } catch (Exception var29) {
         var29.printStackTrace();
      }

   }

   public static final void ChangeChannel(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr, boolean room) {
      try {
         if (chr != null && chr.getEventInstance() == null && chr.getMap() != null && !FieldLimitType.ChannelSwitch.check(chr.getMap().getFieldLimit())) {
            if (World.getPendingCharacterSize() >= 10) {
               chr.dropMessage(1, "채널 이동중인 사람이 많습니다. 잠시 후 시도해주세요.");
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            } else {
               int chc = slea.readByte() + 1;
               int mapid = 0;
               if (room) {
                  mapid = slea.readInt();
               }

               slea.readInt();
               if (!World.isChannelAvailable(chc)) {
                  chr.dropMessage(1, "현재 해당 채널이 혼잡하여 이동하실 수 없습니다.");
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               } else if (!room || mapid >= 910000001 && mapid <= 910000022) {
                  if (room) {
                     if (chr.getMapId() == mapid) {
                        if (c.getChannel() == chc) {
                           c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                        } else {
                           chr.changeChannel(chc);
                        }

                     } else {
                        if (c.getChannel() != chc) {
                           chr.changeChannel(chc);
                        }

                        MapleMap warpz = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(mapid);
                        if (warpz != null) {
                           chr.changeMap(warpz, warpz.getPortal("out00"));
                        } else {
                           chr.dropMessage(1, "현재 해당 채널이 혼잡하여 이동하실 수 없습니다.");
                           c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                        }

                     }
                  } else {
                     chr.changeChannel(chc);
                  }
               } else {
                  chr.dropMessage(1, "현재 해당 채널이 혼잡하여 이동하실 수 없습니다.");
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               }
            }
         } else {
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
         }
      } catch (Exception var7) {
         var7.printStackTrace();
      }
   }

   public static void getGameQuitRequest(RecvPacketOpcode header, LittleEndianAccessor rh, MapleClient c) {
      String account = null;
      if (header == RecvPacketOpcode.GAME_EXIT) {
         rh.skip(8);
      } else {
         account = rh.readMapleAsciiString();
      }

      if (account == null || account.equals("")) {
         account = c.getAccountName();
      }

      if (c != null) {
         if (account != null && header != RecvPacketOpcode.GAME_EXIT) {
            if (!c.isLoggedIn() && !c.getAccountName().equals(account)) {
               c.disconnect(true, false, false);
               c.getSession().close();
            } else {
               c.disconnect(true, false, false);
               c.getSession().writeAndFlush(LoginPacket.getKeyGuardResponse(account + "," + c.getPassword(account)));
            }
         } else {
            c.disconnect(true, false, false);
            c.getSession().close();
         }
      }
   }

   public static void sendOpcodeEncryption(MapleClient c) {
      c.mEncryptedOpcode.clear();
      byte[] aKey = new byte[24];
      String key = "G0dD@mnN#H@ckEr!";

      for(int i = 0; i < key.length(); ++i) {
         aKey[i] = (byte)key.charAt(i);
      }

      System.arraycopy(aKey, 0, aKey, 16, 8);
      List<Integer> aUsed = new ArrayList();
      String sOpcode = "1351|";
      int startOpcode = 194;
      int size = 0;

      for(int i = startOpcode; i < 2000; ++i) {
         int nNum;
         for(nNum = Randomizer.rand(startOpcode, 9999); aUsed.contains(nNum); nNum = Randomizer.rand(startOpcode, 9999)) {
         }

         String sNum = String.format("%d", nNum);
         c.mEncryptedOpcode.put(nNum, i);
         aUsed.add(nNum);
         if (i > startOpcode) {
            sOpcode = sOpcode + "|" + sNum;
         } else {
            sOpcode = sOpcode + sNum;
         }

         ++size;
         if (size >= 1351) {
            break;
         }
      }

      aUsed.clear();
      TripleDESCipher pCipher = new TripleDESCipher(aKey);

      try {
         byte[] aEncrypt = pCipher.Encrypt(sOpcode.getBytes());
         c.getSession().writeAndFlush(LoginPacket.OnOpcodeEncryption(aEncrypt));
      } catch (Exception var10) {
         var10.printStackTrace();
      }

   }
}
