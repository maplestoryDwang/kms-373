package handling.login.handler;

import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.PlayerStats;
import client.Skill;
import client.SkillEntry;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import connector.ConnectorClient;
import connector.ConnectorHandler;
import constants.GameConstants;
import constants.JobConstants;
import constants.KoreaCalendar;
import constants.ServerConstants;
import handling.SendPacketOpcode;
import handling.auction.AuctionServer;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.farm.FarmServer;
import handling.login.LoginInformationProvider;
import handling.login.LoginServer;
import handling.login.LoginWorker;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import server.MapleItemInformationProvider;
import server.quest.MapleQuest;
import tools.FileoutputUtil;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.LoginPacket;
import tools.packet.PacketHelper;

public class CharLoginHandler {
   private static final boolean loginFailCount(MapleClient c) {
      ++c.loginAttempt;
      return c.loginAttempt > 5;
   }

   public static final void login(LittleEndianAccessor slea, MapleClient c) {
      byte[] m = slea.read(6);
      StringBuilder sb = new StringBuilder();

      for(int i = 0; i < m.length; ++i) {
         sb.append(String.format("%02X%s", m[i], i < m.length - 1 ? "-" : ""));
      }

      slea.skip(15);
      boolean nexonTab = slea.readByte() == 1;
      String login = slea.readMapleAsciiString();
      String pwd = slea.readMapleAsciiString();
      int loginok = 0;
      boolean ipBan = c.hasBannedIP();
      boolean macBan = c.hasBannedMac();
      if (AutoRegister.getAccountExists(login)) {
         loginok = c.login(login, pwd, sb.toString(), ipBan || macBan);
      } else if (!nexonTab && !c.hasBannedIP() && !c.hasBannedMac()) {
         if (AutoRegister.createAccount(login, pwd, c.getSession().remoteAddress().toString())) {
            loginok = c.login(login, pwd, sb.toString(), ipBan || macBan);
            c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "[시스템]\r\n가입이 성공적으로 완료되었습니다!"));
            c.getSession().writeAndFlush(LoginPacket.getLoginFailed(21));
            return;
         }

         c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "[시스템]\r\n 1 아이피당 계정 1개를 생성하실 수 있습니다."));
         c.getSession().writeAndFlush(LoginPacket.getLoginFailed(21));
      }

      Calendar tempbannedTill = c.getTempBanCalendar();
      if (loginok == 0 && (ipBan || macBan) && !c.isGm()) {
         loginok = 3;
         if (macBan) {
            MapleCharacter.ban(c.getSession().remoteAddress().toString().split(":")[0], "Enforcing account ban, account " + login, false, 4, false);
         }
      }

      if (c.getSessionIPAddress().equals("127.0.0.1")) {
         c.loginAttempt = 0;
         LoginWorker.registerClient(c, login, pwd);
      } else {
         if (loginok != 0) {
            if (!loginFailCount(c)) {
               if (loginok == 7 && !c.isGm()) {
                  boolean disconnect = false;
                  Iterator var12 = ChannelServer.getAllInstances().iterator();

                  String var10000;
                  int var10001;
                  while(var12.hasNext()) {
                     ChannelServer cserv = (ChannelServer)var12.next();
                     Iterator var14 = cserv.getPlayerStorage().getAllCharacters().values().iterator();

                     while(var14.hasNext()) {
                        MapleCharacter players = (MapleCharacter)var14.next();
                        if (players != null && players.getClient().getAccID() == c.getAccID()) {
                           var10000 = FileoutputUtil.감지로그;
                           var10001 = players.getClient().getAccID();
                           FileoutputUtil.log(var10000, "[접속끊기] 계정번호 : " + var10001 + " | " + players.getName() + "(" + players.getId() + ")이 이미 접속 되있는 캐릭터가 있어서 접속이 끊김(로그인창에서 끊기 시도)");
                           players.getWorldGMMsg(players, "서버에 이미 접속이 되어있는 캐릭이 있어서 접속이 끊김");
                           players.getClient().getChannelServer().getPlayerStorage().deregisterPendingPlayer(players.getId());
                           players.getClient().getSession().close();
                           players.getClient().disconnect(players, true, true, false);
                           disconnect = true;
                        }
                     }
                  }

                  var12 = CashShopServer.getPlayerStorage().getAllCharacters().values().iterator();

                  MapleCharacter csplayer;
                  while(var12.hasNext()) {
                     csplayer = (MapleCharacter)var12.next();
                     if (csplayer != null && csplayer.getName() != null && csplayer.getClient().getAccID() == c.getAccID()) {
                        var10000 = FileoutputUtil.감지로그;
                        var10001 = csplayer.getClient().getAccID();
                        FileoutputUtil.log(var10000, "[접속끊기] 계정번호 : " + var10001 + " | " + csplayer.getName() + "(" + csplayer.getId() + ")이 이미 접속 되있는 캐릭터가 [캐시샵]에 있어서 접속이 끊김(로그인창에서 끊기 시도)");
                        csplayer.getWorldGMMsg(csplayer, "캐시샵에 이미 접속이 되어있는 캐릭이 있어서 접속이 끊김");
                        CashShopServer.getPlayerStorage().deregisterPendingPlayer(csplayer.getId());
                        csplayer.getClient().disconnect(csplayer, true, true, false);
                        csplayer.getClient().getSession().close();
                        disconnect = true;
                     }
                  }

                  var12 = AuctionServer.getPlayerStorage().getAllCharacters().values().iterator();

                  while(var12.hasNext()) {
                     csplayer = (MapleCharacter)var12.next();
                     if (csplayer != null && csplayer.getName() != null && csplayer.getClient().getAccID() == c.getAccID()) {
                        var10000 = FileoutputUtil.감지로그;
                        var10001 = csplayer.getClient().getAccID();
                        FileoutputUtil.log(var10000, "[접속끊기] 계정번호 : " + var10001 + " | " + csplayer.getName() + "(" + csplayer.getId() + ")이 이미 접속 되있는 캐릭터가 [경매장]에 있어서 접속이 끊김(로그인창에서 끊기 시도)");
                        csplayer.getWorldGMMsg(csplayer, "경매장에 이미 접속이 되어있는 캐릭이 있어서 접속이 끊김");
                        AuctionServer.getPlayerStorage().deregisterPendingPlayer(csplayer.getId());
                        csplayer.getClient().disconnect(csplayer, true, true, false);
                        csplayer.getClient().getSession().close();
                        disconnect = true;
                     }
                  }

                  var12 = FarmServer.getPlayerStorage().getAllCharacters().values().iterator();

                  while(var12.hasNext()) {
                     csplayer = (MapleCharacter)var12.next();
                     if (csplayer != null && csplayer.getName() != null && csplayer.getClient().getAccID() == c.getAccID()) {
                        var10000 = FileoutputUtil.감지로그;
                        var10001 = csplayer.getClient().getAccID();
                        FileoutputUtil.log(var10000, "[접속끊기] 계정번호 : " + var10001 + " | " + csplayer.getName() + "(" + csplayer.getId() + ")이 이미 접속 되있는 캐릭터가 [농장]에 있어서 접속이 끊김(로그인창에서 끊기 시도)");
                        csplayer.getWorldGMMsg(csplayer, "농장에 이미 접속이 되어있는 캐릭이 있어서 접속이 끊김");
                        FarmServer.getPlayerStorage().deregisterPendingPlayer(csplayer.getId());
                        csplayer.getClient().disconnect(csplayer, true, true, false);
                        csplayer.getClient().getSession().close();
                        disconnect = true;
                     }
                  }

                  c.loginAttempt = 0;
                  LoginWorker.registerClient(c, login, pwd);
               } else {
                  c.clearInformation();
                  c.getSession().writeAndFlush(LoginPacket.getLoginFailed(loginok));
               }
            } else {
               c.getSession().close();
            }
         } else if (tempbannedTill.getTimeInMillis() != 0L) {
            if (!loginFailCount(c)) {
               c.clearInformation();
               c.getSession().writeAndFlush(LoginPacket.getTempBan(PacketHelper.getTime(tempbannedTill.getTimeInMillis()), c.getBanReason()));
            } else {
               c.getSession().close();
            }
         } else {
            c.loginAttempt = 0;
            LoginWorker.registerClient(c, login, pwd);
         }

      }
   }

   public static final void HackShield(LittleEndianAccessor slea, MapleClient c) {
      c.getSession().writeAndFlush(LoginPacket.HackShield());
   }

   public static final void SessionCheck(LittleEndianAccessor slea, MapleClient c) {
      int pRequest = slea.readInt();
      int pResponse = pRequest ^ SendPacketOpcode.SESSION_CHECK.getValue();
      c.getSession().writeAndFlush(LoginPacket.SessionCheck(pResponse));
   }

   public static final void checkLoadWzData(LittleEndianAccessor slea, MapleClient c) {
      int type = slea.readInt();
      if (type == 22) {
         c.getSession().writeAndFlush(LoginPacket.enableLogin());
      }

   }

   public static void getLoginRequest(LittleEndianAccessor slea, MapleClient c) {
      short webStart = (short)slea.readByte();
      if (webStart == 1) {
         String token = slea.readMapleAsciiString();
         String[] sp = token.split(",");
         String login = "";
         String pwd = "";
         int loginok = c.login(sp[0], sp[1], "", true, c.hasBannedIP());
         if (loginok == 0) {
            c.setAccountName("");
            c.setPlayer((MapleCharacter)null);
            c.getSession().writeAndFlush(LoginPacket.getAuthSuccessRequest(c));
            c.getSession().writeAndFlush(LoginPacket.getCharEndRequest(c, "", "", false));
         } else {
            c.getSession().writeAndFlush(LoginPacket.getLoginFailed(20));
         }

         c.getSession().writeAndFlush(LoginPacket.checkLogin());
      } else {
         System.out.println("로그인시도 오류 발생?");
      }

   }

   public static final void ServerListRequest(MapleClient c, boolean leaving) {
      LocalDateTime time = LocalDateTime.now();
      if (time.getDayOfWeek().getValue() == 7) {
         c.getSession().writeAndFlush(LoginPacket.ChannelBackImg(true));
      } else {
         c.getSession().writeAndFlush(LoginPacket.ChannelBackImg(false));
      }

      c.getSession().writeAndFlush(LoginPacket.getServerList(0, LoginServer.getLoad()));
      c.getSession().writeAndFlush(LoginPacket.getEndOfServerList());
      c.getSession().writeAndFlush(LoginPacket.enableRecommended());
   }

   public static final void ServerStatusRequest(MapleClient c) {
      int numPlayer = LoginServer.getUsersOn();
      int userLimit = LoginServer.getUserLimit();
      if (numPlayer >= userLimit) {
         c.getSession().writeAndFlush(LoginPacket.getServerStatus(2));
      } else if (numPlayer * 2 >= userLimit) {
         c.getSession().writeAndFlush(LoginPacket.getServerStatus(1));
      } else {
         c.getSession().writeAndFlush(LoginPacket.getServerStatus(0));
      }

   }

   public static final void CharlistRequest(LittleEndianAccessor slea, MapleClient c) {
      boolean isFirstLogin = slea.readByte() == 0;
      int server = slea.readByte();
      int channel = slea.readByte() + 1;
      int gameend = slea.readByte();
      boolean check = false;
      if (gameend == 1) {
         slea.skip(1);
         String[] idpw = slea.readMapleAsciiString().split(",");
         byte[] m = slea.read(6);
         StringBuilder sb = new StringBuilder();

         for(int i = 0; i < m.length; ++i) {
            sb.append(String.format("%02X%s", m[i], i < m.length - 1 ? "-" : ""));
         }

         String login = idpw[0];
         String pwd = idpw[1];
         if (!isFirstLogin) {
            c.getSession().writeAndFlush(LoginPacket.getCharEndRequest(c, login, pwd, true));
            c.getSession().writeAndFlush(LoginPacket.getSelectedWorldResult(server));
         }

         c.updateLoginState(0, c.getSessionIPAddress(), login);
         int loginok = c.login(login, pwd, sb.toString(), false);
         c.updateLoginState(2, login);
         if (loginok != 0) {
            c.getSession().close();
         } else {
            c.setAccountName(idpw[0]);
            c.getSession().writeAndFlush(LoginPacket.getAuthSuccessRequest(c, idpw[0], idpw[1]));
            check = true;
         }
      }

      if (!c.isLoggedIn() && !check && !c.isGm()) {
         c.getSession().close();
      } else {
         List<MapleCharacter> chars = c.loadCharacters(server);
         if (chars != null && ChannelServer.getInstance(channel) != null) {
            c.setWorld(server);
            c.setChannel(channel);
            c.getSession().writeAndFlush(LoginPacket.getCharList(c, c.getSecondPassword(), chars, c.getCharacterSlots(), c.getNameChangeEnable()));
         } else {
            c.getSession().close();
         }

      }
   }

   public static final void SelectChannelList(MapleClient c, int world) {
      ConnectorClient var10000 = (ConnectorClient)ConnectorHandler.connectionList.get(c.getSessionIPAddress());
      if (ServerConstants.ConnectorSetting && !c.isGm() && !c.getSessionIPAddress().equals("125.184.5.40") && !c.getSessionIPAddress().equals("124.146.52.21") && !c.getSessionIPAddress().equals("1.246.243.142") && !c.getSessionIPAddress().equals("118.46.37.47")) {
         if (c.getAllowed() != 1) {
            c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "접속기를 통해 접속해주세요."));
            c.getSession().writeAndFlush(LoginPacket.getSelectedChannelFailed((byte)21, world));
            return;
         }

         if (c.getDiscord().length() <= 1) {
            c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "디스코드를 통해 아이디를 연동해주세요."));
            c.getSession().writeAndFlush(LoginPacket.getSelectedChannelFailed((byte)21, world));
            return;
         }
      }

      if (!GameConstants.isOpen) {
         c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "[시스템]\r\n데이터 로딩중입니다.\r\n잠시만 기다려주세요."));
         c.getSession().writeAndFlush(LoginPacket.getCharacterLoad());
         c.getSession().writeAndFlush(LoginPacket.getSelectedChannelFailed((byte)21, world));
      } else {
         Iterator var3 = ChannelServer.getAllInstances().iterator();

         while(var3.hasNext()) {
            ChannelServer csv = (ChannelServer)var3.next();
            if (csv.isShutdown()) {
               c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "[시스템]\r\n서버 종료중입니다."));
               c.getSession().writeAndFlush(LoginPacket.getSelectedChannelFailed((byte)21, world));
               return;
            }
         }

         c.getSession().writeAndFlush(LoginPacket.getSelectedChannelResult(world));
      }

   }

   public static final void CheckCharName(String name, MapleClient c) {
      c.getSession().writeAndFlush(LoginPacket.charNameResponse(name, !MapleCharacterUtil.canCreateChar(name, c.isGm()) || LoginInformationProvider.getInstance().isForbiddenName(name) && !c.isGm()));
   }

   public static final void CheckCharNameChange(LittleEndianAccessor slea, MapleClient c) {
      int cid = slea.readInt();
      String beforename = slea.readMapleAsciiString();
      String afterName = slea.readMapleAsciiString();
      c.setNameChangeEnable((byte)0);
      MapleCharacter.saveNameChange(afterName, cid);
      MapleCharacter.updateNameChangeCoupon(c);
      c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "캐릭터 이름이 성공적으로 변경되었습니다. 변경을 위해 다시 로그인 바랍니다."));
   }

   public static final void CreateChar(LittleEndianAccessor slea, MapleClient c) {
      int hairColor = -1;
      int hat = -1;
      int bottom = -1;
      int cape = -1;
      int faceMark = -1;
      int shield = -1;
      String name = slea.readMapleAsciiString();
      slea.skip(4);
      if (!MapleCharacterUtil.canCreateChar(name, false)) {
         System.out.println("char name hack: " + name);
      } else {
         slea.readInt();
         int job_type = slea.readInt();
         LoginInformationProvider.JobType job = LoginInformationProvider.JobType.getByType(job_type);
         if (job == null) {
            System.out.println("New job type found: " + job_type);
         } else {
            JobConstants.LoginJob[] var11 = JobConstants.LoginJob.values();
            int var12 = var11.length;

            for(int var13 = 0; var13 < var12; ++var13) {
               JobConstants.LoginJob j = var11[var13];
               if (j.getJobType() == job_type && j.getFlag() != JobConstants.LoginJob.JobFlag.ENABLED.getFlag()) {
                  c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "현재 이 직업군은 구현중이므로 생성하실 수 없습니다. \r\n ESC키를 눌러 다시 로그인하시길 바랍니다.)"));
                  return;
               }
            }

            short subcategory = slea.readShort();
            byte gender = slea.readByte();
            byte skin = slea.readByte();
            byte unk = slea.readByte();
            int face = slea.readInt();
            int hair = slea.readInt();
            if (job.faceMark) {
               faceMark = slea.readInt();
            }

            if (job.hat) {
               hat = slea.readInt();
            }

            int top = slea.readInt();
            if (job.bottom) {
               bottom = slea.readInt();
            }

            if (job.cape) {
               cape = slea.readInt();
            }

            int shoes = slea.readInt();
            int weapon = slea.readInt();
            if (slea.available() >= 4L) {
               shield = slea.readInt();
            }

            MapleCharacter newchar = MapleCharacter.getDefault(c, job);
            newchar.setWorld((byte)c.getWorld());
            newchar.setFace(face);
            newchar.setSecondFace(face);
            if (hairColor < 0) {
               hairColor = 0;
            }

            if (job != LoginInformationProvider.JobType.Mihile) {
               hair += hairColor;
            }

            newchar.setHair(hair);
            newchar.setSecondHair(hair);
            if (job == LoginInformationProvider.JobType.AngelicBuster) {
               newchar.setSecondFace(21173);
               newchar.setSecondHair(37141);
               newchar.setGender((byte)1);
            } else if (job == LoginInformationProvider.JobType.Zero) {
               newchar.setSecondGender((byte)1);
               newchar.setSecondFace(21290);
               newchar.setSecondHair(37623);
            }

            newchar.setGender(gender);
            newchar.setName(name);
            if (c.isGm()) {
               newchar.setGMLevel((byte)10);
            }

            newchar.setSkinColor(skin);
            if (faceMark < 0) {
               faceMark = 0;
            }

            newchar.setDemonMarking(faceMark);
            MapleItemInformationProvider li = MapleItemInformationProvider.getInstance();
            MapleInventory equip = newchar.getInventory(MapleInventoryType.EQUIPPED);
            if (job == LoginInformationProvider.JobType.Demon) {
               if (!LoginInformationProvider.getInstance().isEligibleItem(gender, 0, job.type, face) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 1, job.type, hair) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 2, job.type, faceMark) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 3, job.type, top) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 4, job.type, shoes) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 5, job.type, weapon) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 6, job.type, shield)) {
                  return;
               }
            } else if (job != LoginInformationProvider.JobType.ark && job != LoginInformationProvider.JobType.Xenon) {
               if (job == LoginInformationProvider.JobType.Hoyeong) {
                  if (!LoginInformationProvider.getInstance().isEligibleItem(gender, 0, job.type, face) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 1, job.type, hair) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 2, job.type, faceMark) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 3, job.type, top) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 4, job.type, cape) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 5, job.type, shoes) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 6, job.type, weapon)) {
                     return;
                  }
               } else if (job != LoginInformationProvider.JobType.Mercedes && job != LoginInformationProvider.JobType.Cain && job != LoginInformationProvider.JobType.Kadena && job != LoginInformationProvider.JobType.AngelicBuster && job != LoginInformationProvider.JobType.Kaiser && job != LoginInformationProvider.JobType.Resistance && job != LoginInformationProvider.JobType.DualBlade && job != LoginInformationProvider.JobType.Adventurer && job != LoginInformationProvider.JobType.Kinesis && job != LoginInformationProvider.JobType.Iliume && job != LoginInformationProvider.JobType.Adel && job != LoginInformationProvider.JobType.Cannoneer) {
                  if (job != LoginInformationProvider.JobType.Mihile && job != LoginInformationProvider.JobType.Evan && job != LoginInformationProvider.JobType.Aran) {
                     if ((job == LoginInformationProvider.JobType.Phantom || job == LoginInformationProvider.JobType.Luminous || job == LoginInformationProvider.JobType.Cygnus) && (!LoginInformationProvider.getInstance().isEligibleItem(gender, 0, job.type, face) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 1, job.type, hair) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 2, job.type, top) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 3, job.type, cape) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 4, job.type, shoes) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 5, job.type, weapon))) {
                        return;
                     }
                  } else if (!LoginInformationProvider.getInstance().isEligibleItem(gender, 0, job.type, face) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 1, job.type, hair) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 2, job.type, top) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 3, job.type, bottom) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 4, job.type, shoes) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 5, job.type, weapon)) {
                     return;
                  }
               } else if ((!LoginInformationProvider.getInstance().isEligibleItem(gender, 0, job.type, face) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 1, job.type, hair) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 2, job.type, top) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 3, job.type, shoes) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 4, job.type, weapon)) && job == LoginInformationProvider.JobType.Mercedes) {
                  if (skin != 0 && skin != 12) {
                     return;
                  }

                  return;
               }
            } else if (!LoginInformationProvider.getInstance().isEligibleItem(gender, 0, job.type, face) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 1, job.type, hair) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 2, job.type, faceMark) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 3, job.type, top) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 4, job.type, shoes) || !LoginInformationProvider.getInstance().isEligibleItem(gender, 5, job.type, weapon)) {
               return;
            }

            int[][] equips = new int[][]{{hat, -1}, {top, -5}, {bottom, -6}, {cape, -9}, {shoes, -7}, {weapon, -11}, {shield, -10}};
            int[][] skills = equips;
            int var27 = equips.length;

            int guidebook;
            for(guidebook = 0; guidebook < var27; ++guidebook) {
               int[] i = skills[guidebook];
               if (i[0] > 0) {
                  Item item = li.getEquipById(i[0], false);
                  item.setPosition((short)((byte)i[1]));
                  item.setGMLog("Chraracter Creation");
                  equip.addFromDB(item);
               }
            }

            skills = new int[][]{{80001152, 30001061}, {80001152, 1281}, {10001244, 10000252, 80001152, 10001253, 10001254}, {20000194, 20000297}, {20010022, 20010194, 20010294}, {20020109, 20021110, 20020111, 20020112}, {30010110, 30010185}, {20031208, 20040190, 20031203, 20031205, 20030206, 20031207, 20031209, 20031251, 20031260, 20030204}, new int[0], {50001214, 50000074}, {20040216, 20040217, 20040218, 20040219, 20040221, 20041222}, {60001216, 60001217}, {60011216, 60010217, 60011218, 60011219, 60011220, 60011221, 60011222}, new int[0], {30020232, 30020233, 30020234, 30020240, 30021235, 30021236, 30021237}, {100000279, 100000282, 100001262, 100001263, 100001264, 100001265, 100001266, 100001268, 100000271}, {20051284, 20050285, 20050286}, new int[0], {140000291, 14200, 14210, 14211, 14212, 140000292}, {60020216, 60021217, 6020218}, {150000017}, {150010079, 150011005, 150010241}, new int[0], {160000001}, new int[0], new int[0], new int[0], new int[0], new int[0]};
            int k;
            int var43;
            if (skills[job.type].length > 0) {
               Map<Skill, SkillEntry> ss = new HashMap();
               int[] var40 = skills[job.type];
               int var41 = var40.length;

               for(var43 = 0; var43 < var41; ++var43) {
                  k = var40[var43];
                  Skill s = SkillFactory.getSkill(k);
                  if (s != null) {
                     int maxLevel = s.getMaxLevel();
                     if (maxLevel < 1) {
                        maxLevel = s.getMasterLevel();
                     }

                     ss.put(s, new SkillEntry((byte)maxLevel, (byte)maxLevel, -1L));
                  }
               }

               if (job == LoginInformationProvider.JobType.EunWol) {
                  ss.put(SkillFactory.getSkill(25001000), new SkillEntry(0, (byte)25, -1L));
                  ss.put(SkillFactory.getSkill(25001002), new SkillEntry(0, (byte)25, -1L));
               }

               newchar.changeSkillLevel_Skip(ss, false);
            }

            int[][] guidebooks = new int[][]{{4161001, 0}, {4161047, 1}, {4161048, 2000}, {4161052, 2001}, {4161054, 3}, {4161079, 2002}};
            guidebook = 0;
            int[][] var42 = guidebooks;
            var43 = guidebooks.length;

            for(k = 0; k < var43; ++k) {
               int[] l = var42[k];
               if (newchar.getJob() == l[1]) {
                  guidebook = l[0];
               } else if (newchar.getJob() / 1000 == l[1]) {
                  guidebook = l[0];
               }
            }

            if (guidebook > 0) {
               newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(guidebook, (short)0, (short)1, 0));
            }

            if (job == LoginInformationProvider.JobType.Zero) {
               newchar.setLevel((short)101);
               newchar.setJob(10112);
               newchar.getStat().str = 518;
               newchar.getStat().maxhp = 6910L;
               newchar.getStat().hp = 6910L;
               newchar.getStat().maxmp = 100L;
               newchar.getStat().mp = 100L;
            }

            if (!MapleCharacterUtil.canCreateChar(name, c.isGm()) || LoginInformationProvider.getInstance().isForbiddenName(name) && !c.isGm() || !c.isGm() && !c.canMakeCharacter(c.getWorld())) {
               c.getSession().writeAndFlush(LoginPacket.addNewCharEntry(newchar, false));
            } else {
               MapleCharacter.saveNewCharToDB(newchar, job, subcategory);
               c.getSession().writeAndFlush(LoginPacket.addNewCharEntry(newchar, true));
               c.createdChar(newchar.getId());
            }

         }
      }
   }

   public static final void Character_WithoutSecondPassword(LittleEndianAccessor slea, MapleClient c) {
      int key = slea.readInt();
      short size = slea.readShort();
      byte[] newpass = slea.read(size);
      int charId = slea.readInt();
      if (c.isLoggedIn() && !loginFailCount(c) && c.login_Auth(charId) && ChannelServer.getInstance(c.getChannel()) != null) {
         byte[] nowpass = c.getSecondPassword().getBytes(Charset.forName("MS949"));

         int k;
         for(int i = 0; i < nowpass.length; ++i) {
            k = nowpass[i] & 255;
            k <<= 3;

            for(int j = 0; j < key; ++j) {
               k *= 2;
            }

            nowpass[i] = (byte)((byte)k - k / 255 * 255);
         }

         boolean login = true;
         if (nowpass.length == newpass.length) {
            for(k = 0; k < nowpass.length; ++k) {
               if (nowpass[k] != newpass[k]) {
                  login = false;
                  break;
               }
            }
         } else {
            login = false;
         }

         if ((!login || newpass.length < 4 || newpass.length > 16) && c.getSecondPw() != 1) {
            c.getSession().writeAndFlush(LoginPacket.secondPwError((byte)20));
         } else {
            if (c.getIdleTask() != null) {
               c.getIdleTask().cancel(true);
            }

            String s = c.getSessionIPAddress();
            LoginServer.putLoginAuth(charId, s.substring(s.indexOf(47) + 1, s.length()), c.getTempIP());
            c.updateLoginState(1, s);
            c.getSession().writeAndFlush(CField.getServerIP(c, Integer.parseInt(ChannelServer.getInstance(c.getChannel()).getIP().split(":")[1]), charId));
         }

      } else {
         c.getSession().close();
      }
   }

   public static final void LoginWithCreateCharacter(LittleEndianAccessor slea, MapleClient c) {
      int charId = slea.readInt();
      if (c.isLoggedIn() && !loginFailCount(c) && c.getSecondPassword() != null && c.login_Auth(charId) && ChannelServer.getInstance(c.getChannel()) != null) {
         if (c.getIdleTask() != null) {
            c.getIdleTask().cancel(true);
         }

         String s = c.getSessionIPAddress();
         LoginServer.putLoginAuth(charId, s.substring(s.indexOf(47) + 1, s.length()), c.getTempIP());
         c.updateLoginState(1, s);
         c.getSession().writeAndFlush(CField.getServerIP(c, Integer.parseInt(ChannelServer.getInstance(c.getChannel()).getIP().split(":")[1]), charId));
      } else {
         c.getSession().close();
      }
   }

   public static final void CreateUltimate(LittleEndianAccessor slea, MapleClient c) {
      if (c.isLoggedIn() && c.getPlayer() != null && c.getPlayer().getLevel() >= 120 && c.getPlayer().getMapId() == 130000000 && c.getPlayer().getQuestStatus(20734) == 0 && c.getPlayer().getQuestStatus(20616) == 2 && GameConstants.isKOC(c.getPlayer().getJob()) && c.canMakeCharacter(c.getPlayer().getWorld())) {
         String name = slea.readMapleAsciiString();
         int job = slea.readInt();
         if (job >= 110 && job <= 520 && job % 10 <= 0 && (job % 100 == 10 || job % 100 == 20 || job % 100 == 30) && job != 430) {
            int face = slea.readInt();
            int hair = slea.readInt();
            int hat = slea.readInt();
            int top = slea.readInt();
            int glove = slea.readInt();
            int shoes = slea.readInt();
            int weapon = slea.readInt();
            LoginInformationProvider.JobType jobType = LoginInformationProvider.JobType.Adventurer;
            jobType = LoginInformationProvider.JobType.UltimateAdventurer;
            if (LoginInformationProvider.getInstance().isEligibleItem(-1, job, jobType.type, hat) && LoginInformationProvider.getInstance().isEligibleItem(-1, job, jobType.type, top) && LoginInformationProvider.getInstance().isEligibleItem(-1, job, jobType.type, glove) && LoginInformationProvider.getInstance().isEligibleItem(-1, job, jobType.type, shoes) && LoginInformationProvider.getInstance().isEligibleItem(-1, job, jobType.type, weapon)) {
               MapleCharacter newchar = MapleCharacter.getDefault(c, jobType);
               newchar.setJob(job);
               newchar.setWorld(c.getPlayer().getWorld());
               newchar.setFace(face);
               newchar.setHair(hair);
               newchar.setName(name);
               newchar.setSkinColor((byte)3);
               newchar.setLevel((short)51);
               newchar.getStat().str = 4;
               newchar.getStat().dex = 4;
               newchar.getStat().int_ = 4;
               newchar.getStat().luk = 4;
               newchar.setRemainingAp((short)254);
               newchar.setRemainingSp(job / 100 == 2 ? 128 : 122);
               PlayerStats stat19 = newchar.getStat();
               stat19.maxhp += 150L;
               PlayerStats stat20 = newchar.getStat();
               stat20.maxmp += 125L;
               switch(job) {
               case 110:
               case 120:
               case 130:
                  PlayerStats stat21 = newchar.getStat();
                  stat21.maxhp += 600L;
                  PlayerStats stat22 = newchar.getStat();
                  stat22.maxhp += 2000L;
                  PlayerStats stat23 = newchar.getStat();
                  stat23.maxmp += 200L;
                  break;
               case 210:
               case 220:
               case 230:
                  PlayerStats stat24 = newchar.getStat();
                  stat24.maxmp += 600L;
                  PlayerStats stat25 = newchar.getStat();
                  stat25.maxhp += 500L;
                  PlayerStats stat26 = newchar.getStat();
                  stat26.maxmp += 2000L;
                  break;
               case 310:
               case 320:
               case 410:
               case 420:
               case 520:
                  PlayerStats stat27 = newchar.getStat();
                  stat27.maxhp += 500L;
                  PlayerStats stat28 = newchar.getStat();
                  stat28.maxmp += 250L;
                  PlayerStats stat29 = newchar.getStat();
                  stat29.maxhp += 900L;
                  PlayerStats stat30 = newchar.getStat();
                  stat30.maxmp += 600L;
                  break;
               case 510:
                  PlayerStats stat31 = newchar.getStat();
                  stat31.maxhp += 500L;
                  PlayerStats stat32 = newchar.getStat();
                  stat32.maxmp += 250L;
                  PlayerStats stat33 = newchar.getStat();
                  stat33.maxhp += 450L;
                  PlayerStats stat34 = newchar.getStat();
                  stat34.maxmp += 300L;
                  PlayerStats stat35 = newchar.getStat();
                  stat35.maxhp += 800L;
                  PlayerStats stat36 = newchar.getStat();
                  stat36.maxmp += 400L;
                  break;
               default:
                  return;
               }

               for(int i = 2490; i < 2507; ++i) {
                  newchar.setQuestAdd(MapleQuest.getInstance(i), (byte)2, (String)null);
               }

               newchar.setQuestAdd(MapleQuest.getInstance(29947), (byte)2, (String)null);
               newchar.setQuestAdd(MapleQuest.getInstance(111111), (byte)0, c.getPlayer().getName());
               Map<Skill, SkillEntry> ss = new HashMap();
               ss.put(SkillFactory.getSkill(1074 + job / 100), new SkillEntry(5, (byte)5, -1L));
               ss.put(SkillFactory.getSkill(80), new SkillEntry(1, (byte)1, -1L));
               newchar.changeSkillLevel_Skip(ss, false);
               MapleItemInformationProvider li = MapleItemInformationProvider.getInstance();
               int[] items = new int[]{1142257, hat, top, shoes, glove, weapon, hat + 1, top + 1, shoes + 1, glove + 1, weapon + 1};

               for(byte j = 0; j < items.length; ++j) {
                  Item item = li.getEquipById(items[j]);
                  item.setPosition((short)((byte)(j + 1)));
                  newchar.getInventory(MapleInventoryType.EQUIP).addFromDB(item);
               }

               newchar.getInventory(MapleInventoryType.USE).addItem(new Item(2000004, (short)0, (short)100, 0));
               newchar.getInventory(MapleInventoryType.USE).addItem(new Item(2000004, (short)0, (short)100, 0));
               c.getPlayer().fakeRelog();
               if (!MapleCharacterUtil.canCreateChar(name, c.isGm()) || LoginInformationProvider.getInstance().isForbiddenName(name) && !c.isGm()) {
                  c.getSession().writeAndFlush(CField.createUltimate(0));
               } else {
                  MapleCharacter.saveNewCharToDB(newchar, jobType, (short)0);
                  MapleQuest.getInstance(20734).forceComplete(c.getPlayer(), 1101000);
                  c.getSession().writeAndFlush(CField.createUltimate(1));
               }

            } else {
               c.getPlayer().dropMessage(1, "An error occured.");
               c.getSession().writeAndFlush(CField.createUltimate(0));
            }
         } else {
            c.getPlayer().dropMessage(1, "An error has occurred.");
            c.getSession().writeAndFlush(CField.createUltimate(0));
         }
      } else {
         c.getPlayer().dropMessage(1, "You have no character slots.");
         c.getSession().writeAndFlush(CField.createUltimate(0));
      }
   }

   public static final void DeleteChar(LittleEndianAccessor slea, MapleClient c) {
      String Secondpw_Client = slea.readMapleAsciiString();
      int Character_ID = slea.readInt();
      byte state = 0;
      System.err.println(c.getSecondPassword());
      if (c.getSecondPassword() != null && c.getSecondPw() != 0) {
         if (Secondpw_Client == null) {
            c.getSession().close();
            return;
         }
      } else if (!c.CheckSecondPassword(Secondpw_Client)) {
         state = 20;
      }

      if (state == 0) {
         state = (byte)c.deleteCharacter(Character_ID);
      }

      c.getSession().writeAndFlush(LoginPacket.deleteCharResponse(Character_ID, state));
   }

   public static final void checkSecondPassword(LittleEndianAccessor rh, MapleClient c) {
      String code = rh.readMapleAsciiString();
      if (!c.CheckSecondPassword(code)) {
         c.getSession().writeAndFlush(LoginPacket.secondPwError((byte)20));
      } else {
         c.getSession().writeAndFlush(LoginPacket.getSecondPasswordConfirm((byte)0));
      }

   }

   public static void NewPassWordCheck(MapleClient c) {
      Iterator var1 = ChannelServer.getAllInstances().iterator();

      String var10000;
      int var10001;
      while(var1.hasNext()) {
         ChannelServer cserv = (ChannelServer)var1.next();
         Iterator var3 = cserv.getPlayerStorage().getAllCharacters().values().iterator();

         while(var3.hasNext()) {
            MapleCharacter players = (MapleCharacter)var3.next();
            if (players != null && players.getClient().getAccID() == c.getAccID()) {
               players.getClient().getChannelServer().getPlayerStorage().deregisterPendingPlayer(players.getId());
               players.getClient().getChannelServer().getPlayerStorage().deregisterPlayer(players);
               players.getClient().getSession().close();
               players.getClient().disconnect(players, true, true, false);
               var10000 = FileoutputUtil.감지로그;
               var10001 = players.getClient().getAccID();
               FileoutputUtil.log(var10000, "[접속끊기] 계정번호 : " + var10001 + " | " + players.getName() + "(" + players.getId() + ")이 이미 접속 되있는 캐릭터가 있어서 접속이 끊김(캐릭터 선택창에서 시도)");
               players.getWorldGMMsg(players, "서버에 이미 접속이 되어있는 캐릭이 있어서 접속이 끊김");
            }
         }
      }

      var1 = CashShopServer.getPlayerStorage().getAllCharacters().values().iterator();

      MapleCharacter csplayer;
      while(var1.hasNext()) {
         csplayer = (MapleCharacter)var1.next();
         if (csplayer != null && csplayer.getName() != null && csplayer.getClient().getAccID() == c.getAccID()) {
            CashShopServer.getPlayerStorage().deregisterPendingPlayer(csplayer.getId());
            CashShopServer.getPlayerStorage().deregisterPlayer(csplayer);
            csplayer.getClient().disconnect(csplayer, true, true, false);
            csplayer.getClient().getSession().close();
            var10000 = FileoutputUtil.감지로그;
            var10001 = csplayer.getClient().getAccID();
            FileoutputUtil.log(var10000, "[접속끊기] 계정번호 : " + var10001 + " | " + csplayer.getName() + "(" + csplayer.getId() + ")이 이미 접속 되있는 캐릭터가 [캐시샵]에 있어서 접속이 끊김(캐릭터 선택창에서 시도)");
            csplayer.getWorldGMMsg(csplayer, "캐시샵에 이미 접속이 되어있는 캐릭이 있어서 접속이 끊김");
         }
      }

      var1 = AuctionServer.getPlayerStorage().getAllCharacters().values().iterator();

      while(var1.hasNext()) {
         csplayer = (MapleCharacter)var1.next();
         if (csplayer != null && csplayer.getName() != null && csplayer.getClient().getAccID() == c.getAccID()) {
            AuctionServer.getPlayerStorage().deregisterPendingPlayer(csplayer.getId());
            AuctionServer.getPlayerStorage().deregisterPlayer(csplayer);
            var10000 = FileoutputUtil.감지로그;
            var10001 = csplayer.getClient().getAccID();
            FileoutputUtil.log(var10000, "[접속끊기] 계정번호 : " + var10001 + " | " + csplayer.getName() + "(" + csplayer.getId() + ")이 이미 접속 되있는 캐릭터가 [경매장]에 있어서 접속이 끊김(캐릭터 선택창에서 시도)");
            csplayer.getClient().disconnect(csplayer, true, true, false);
            csplayer.getClient().getSession().close();
            csplayer.getWorldGMMsg(csplayer, "경매장에 이미 접속이 되어있는 캐릭이 있어서 접속이 끊김");
         }
      }

      var1 = FarmServer.getPlayerStorage().getAllCharacters().values().iterator();

      while(var1.hasNext()) {
         csplayer = (MapleCharacter)var1.next();
         if (csplayer != null && csplayer.getName() != null && csplayer.getClient().getAccID() == c.getAccID()) {
            var10000 = FileoutputUtil.감지로그;
            var10001 = csplayer.getClient().getAccID();
            FileoutputUtil.log(var10000, "[접속끊기] 계정번호 : " + var10001 + " | " + csplayer.getName() + "(" + csplayer.getId() + ")이 이미 접속 되있는 캐릭터가 [농장]에 있어서 접속이 끊김(캐릭터 선택창에서 시도)");
            csplayer.getWorldGMMsg(csplayer, "농장에 이미 접속이 되어있는 캐릭이 있어서 접속이 끊김");
            FarmServer.getPlayerStorage().deregisterPendingPlayer(csplayer.getId());
            FarmServer.getPlayerStorage().deregisterPlayer(csplayer);
            csplayer.getClient().getChannelServer().getPlayerStorage().deregisterPlayer(csplayer);
            csplayer.getClient().disconnect(csplayer, true, true, false);
            csplayer.getClient().getSession().close();
         }
      }

      c.getSession().writeAndFlush(LoginPacket.skipNewPasswordCheck(c));
      c.getSession().writeAndFlush(LoginPacket.NewSendPasswordWay(c));
      c.getSession().writeAndFlush(LoginPacket.enableLogin());
   }

   public static final void onlyRegisterSecondPassword(LittleEndianAccessor slea, MapleClient c) {
      String secondpw = slea.readMapleAsciiString();
      if (secondpw.length() >= 6 && secondpw.length() <= 16) {
         c.setSecondPassword(secondpw);
         c.getSession().writeAndFlush(LoginPacket.getSecondPasswordResult(true));
         c.updateSecondPassword();
      } else {
         c.getSession().writeAndFlush(LoginPacket.secondPwError((byte)20));
      }

   }

   public static final void ResetSecondPW(LittleEndianAccessor slea, MapleClient c) {
      c.setSecondPassword("초기화");
      c.updateSecondPassword();
      c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "2차 비밀번호 초기화가 완료 되었습니다."));
   }

   public static final void OTPSetting(LittleEndianAccessor slea, MapleClient c) {
      if (c.getSecondPw() == 0) {
         c.setSecondPw(1);
         c.getSession().writeAndFlush(LoginPacket.OTPChange(1));
         c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "2차 비밀번호 입력 기능이 비활성화 되었습니다."));
      } else {
         c.setSecondPw(0);
         c.getSession().writeAndFlush(LoginPacket.OTPChange(0));
         c.getSession().writeAndFlush(CWvsContext.serverNotice(1, "", "2차 비밀번호 입력 기능이 활성화 되었습니다."));
      }

   }

   public static final void InputOTP(LittleEndianAccessor slea, MapleClient c) throws UnsupportedEncodingException {
      int num = slea.readInt();
      if (num == c.getSaveOTPNum()) {
         KoreaCalendar kc = new KoreaCalendar();
         c.setCheckOTP(true);
         String var10001 = kc.getYears();
         c.setSaveOTPDay(var10001 + kc.getMonths() + kc.getDays());
         c.loginAttempt = 0;
         if (c.isGm()) {
            if (c.finishLogin() != 0) {
               c.getSession().writeAndFlush(LoginPacket.getLoginFailed(7));
               return;
            }

            c.getSession().writeAndFlush(LoginPacket.checkLogin());
            c.getSession().writeAndFlush(LoginPacket.getAuthSuccessRequest(c, c.getAccountName(), c.getPassword()));
            ServerListRequest(c, false);
         } else {
            LoginWorker.registerClient(c, c.getAccountName(), c.getPassword());
         }
      } else {
         c.setSaveOTPNum(0);
         c.setCheckOTP(false);
         c.getSession().writeAndFlush(LoginPacket.getLoginFailed(24));
      }

   }
}
