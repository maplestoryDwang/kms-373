package handling;

import client.MapleCharacter;
import client.SecondaryStat;
import client.SkillFactory;
import client.inventory.AuctionHistory;
import client.inventory.AuctionItem;
import constants.KoreaCalendar;
import constants.ServerConstants;
import database.DatabaseConnection;
import handling.auction.AuctionServer;
import handling.auction.handler.AuctionHistoryIdentifier;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.channel.MapleGuildRanking;
import handling.farm.FarmServer;
import handling.world.World;
import handling.world.guild.MapleGuild;
import java.awt.Point;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import scripting.EventManager;
import scripting.NPCScriptManager;
import server.games.BattleGroundGameHandler;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import tools.CurrentTime;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.PacketHelper;
import tools.packet.SLFCGPacket;

public class MapleSaveHandler implements Runnable {
   private long lastSaveAuctionTime = 0L;
   private long lastClearDropTime = 0L;
   public static int date;
   public static int count;
   public static int month;
   private boolean minigamegive;
   private boolean give = false;
   private boolean worldboss = false;
   private boolean show = false;
   public static Map<String, Integer> ids = new HashMap();
   public static Map<String, String> todayKeyValues = new HashMap();
   public static String[][] weekKeyValues = new String[][]{{"db_lastweek", "0"}, {"dojo", "0"}, {"dojo_time", "0"}};
   public static String[] clientDateKeyValues = new String[]{"dailyGiftComplete", "hotelMapleToday", "ht", "mPark", "mpark_t", "jump_1", "jump_2", "jump_3", "day_reborn_1", "day_reborn_2", "day_qitem", "day_summer_a", "day_summer_e", "day_colorlens", "day_MedalC", "day_MedalD", "minigame"};
   public static List<Integer> updateAuctionClients = new ArrayList();

   public MapleSaveHandler() {
      date = CurrentTime.getDay();
      month = CurrentTime.getMonth();
      count = 0;
      this.lastSaveAuctionTime = System.currentTimeMillis();
      this.lastClearDropTime = System.currentTimeMillis();
      System.out.println("[Loading Completed] MapleSaveHandler Start");
      Iterator var1 = ChannelServer.getInstance(1).getEventSM().getEvents().keySet().iterator();

      while(var1.hasNext()) {
         String event = (String)var1.next();
         todayKeyValues.put(event, "0");
      }

   }

   public static void runningAuctionItems(long time) {
      updateAuctionClients.clear();
      Iterator items = AuctionServer.getItems().values().iterator();

      while(items.hasNext()) {
         AuctionItem aItem = (AuctionItem)items.next();
         if (aItem.getEndDate() < time && aItem.getState() == 0) {
            aItem.setState(4);
            AuctionHistory history = new AuctionHistory();
            history.setAuctionId(aItem.getAuctionId());
            history.setAccountId(aItem.getAccountId());
            history.setCharacterId(aItem.getCharacterId());
            history.setItemId(aItem.getItem().getItemId());
            history.setState(aItem.getState());
            history.setPrice(aItem.getPrice());
            history.setBuyTime(System.currentTimeMillis());
            history.setDeposit(aItem.getDeposit());
            history.setQuantity(aItem.getItem().getQuantity());
            history.setWorldId(aItem.getWorldId());
            history.setId((long)AuctionHistoryIdentifier.getInstance());
            aItem.setHistory(history);
            updateAuctionClients.add(aItem.getAccountId());
         }
      }

   }

   public void run() {
      long time = System.currentTimeMillis();
      runningAuctionItems(time);
      if (time - this.lastSaveAuctionTime >= 900000L) {
         ++count;
         AuctionServer.saveItems();
         int var10001 = count * 15 / 60;
         System.out.println("[알림] 서버 오픈 이후 " + var10001 + "시간 " + count * 15 % 60 + "분 경과하였습니다.");
         this.lastSaveAuctionTime = time;
      }

      Iterator<ChannelServer> channels = ChannelServer.getAllInstances().iterator();
      int dd = CurrentTime.getDay();
      int mon = CurrentTime.getDate();
      if (date != dd) {
         date = dd;
         ids.clear();
         reset(channels);
         month = mon;
      }

      if (time - this.lastClearDropTime >= 3600000L) {
         this.lastClearDropTime = time;
         MapleMonsterInformationProvider.getInstance().clearDrops();
         AuctionServer.saveItems();
         System.out.println("드롭 데이터를 초기화했습니다.");
      }

      ChannelServer cs;
      Iterator em;
      MapleCharacter chr;
      label463:
      while(channels.hasNext()) {
         cs = (ChannelServer)channels.next();
         em = cs.getPlayerStorage().getAllCharacters().values().iterator();

         while(true) {
            while(true) {
               do {
                  if (!em.hasNext()) {
                     continue label463;
                  }

                  chr = (MapleCharacter)em.next();
               } while(!chr.isAlive());

               List<Integer> prevEffects = chr.getPrevBonusEffect();
               List<Integer> curEffects = chr.getBonusEffect();
               List<Integer> prevEffects2 = chr.getPrevBonusEffect2();
               List<Integer> curEffects2 = chr.getsuccessorEffect();

               int i;
               for(i = 0; i < curEffects2.size(); ++i) {
                  if (prevEffects2.get(i) != curEffects2.get(i)) {
                     chr.cancelEffectFromBuffStat(SecondaryStat.IndieStr, 80001538);
                     chr.cancelEffectFromBuffStat(SecondaryStat.IndieDex, 80001538);
                     chr.cancelEffectFromBuffStat(SecondaryStat.IndieInt, 80001538);
                     chr.cancelEffectFromBuffStat(SecondaryStat.IndieLuk, 80001538);
                     chr.cancelEffectFromBuffStat(SecondaryStat.IndiePmdR, 80001538);
                     SkillFactory.getSkill(80001538).getEffect(1).applyTo(chr);
                     chr.getStat().recalcLocalStats(chr);
                     break;
                  }
               }

               for(i = 0; i < curEffects.size(); ++i) {
                  if (prevEffects.get(i) != curEffects.get(i)) {
                     chr.cancelEffectFromBuffStat(SecondaryStat.IndieDamR, 80002419);
                     chr.cancelEffectFromBuffStat(SecondaryStat.IndieExp, 80002419);
                     chr.cancelEffectFromBuffStat(SecondaryStat.DropRate, 80002419);
                     chr.cancelEffectFromBuffStat(SecondaryStat.MesoUp, 80002419);
                     chr.cancelEffectFromBuffStat(SecondaryStat.IndieCD, 80002419);
                     chr.cancelEffectFromBuffStat(SecondaryStat.IndieBDR, 80002419);
                     chr.cancelEffectFromBuffStat(SecondaryStat.IndieAllStatR, 80002419);
                     chr.cancelEffectFromBuffStat(SecondaryStat.IndiePmdR, 80002419);
                     SkillFactory.getSkill(80002419).getEffect(1).applyTo(chr);
                     if (chr.getSuccessor() > 0) {
                        chr.cancelEffectFromBuffStat(SecondaryStat.IndieDamR, 80002416);
                        chr.cancelEffectFromBuffStat(SecondaryStat.IndieCD, 80002416);
                        chr.cancelEffectFromBuffStat(SecondaryStat.IndieBDR, 80002416);
                        chr.cancelEffectFromBuffStat(SecondaryStat.IndiePmdR, 80002416);
                        SkillFactory.getSkill(80002416).getEffect(10).applyTo(chr);
                     }

                     chr.getStat().recalcLocalStats(chr);
                     break;
                  }
               }
            }
         }
      }

      if ((new Date()).getHours() >= 10 && (new Date()).getHours() <= 24) {
         if (((new Date()).getMinutes() == 15 || (new Date()).getMinutes() == 45) && !this.minigamegive) {
            this.minigamegive = true;
            if ((new Date()).getHours() == 21) {
               BattleGroundGameHandler.BattleGroundIinviTation();
            }
         } else if ((new Date()).getMinutes() != 15 && (new Date()).getMinutes() != 45 && this.minigamegive) {
            this.minigamegive = false;
         }
      }

      Iterator var20;
      if (ServerConstants.Event_MapleLive && ((new Date()).getHours() >= 10 && (new Date()).getHours() <= 24 || (new Date()).getHours() < 2)) {
         if ((new Date()).getMinutes() == 29 && !this.show) {
            this.show = true;
            String s = (new Date()).getHours() % 2 == 0 ? "블루" : "핑크";
            String msg = "메이플 LIVE " + s + " 스튜디오 생방송이 시작됩니다 방송 시작 1분 전!";
            var20 = ChannelServer.getAllInstances().iterator();

            while(var20.hasNext()) {
               ChannelServer cserv = (ChannelServer)var20.next();
               Iterator var26 = cserv.getPlayerStorage().getAllCharacters().values().iterator();

               while(var26.hasNext()) {
                  MapleCharacter player = (MapleCharacter)var26.next();
                  if (player != null && player.getName() != null) {
                     player.getClient().send(CField.QuestMsg(msg, 343, 30000, 100825));
                  }
               }
            }
         } else if (this.show) {
            this.show = false;
         }
      }

      Iterator var16;
      ChannelServer cserv;
      Iterator var24;
      MapleCharacter hp;
      if ((new Date()).getHours() == 21 && (new Date()).getMinutes() == 0 && !this.give) {
         this.give = true;
         List<MapleCharacter> chrs = new ArrayList();
         em = ChannelServer.getAllInstances().iterator();

         while(em.hasNext()) {
            ChannelServer cserv = (ChannelServer)em.next();
            var24 = cserv.getPlayerStorage().getAllCharacters().values().iterator();

            while(var24.hasNext()) {
               MapleCharacter player = (MapleCharacter)var24.next();
               if (player != null && player.getName() != null && !chrs.contains(player)) {
                  chrs.add(player);
               }
            }
         }

         em = CashShopServer.getPlayerStorage().getAllCharacters().values().iterator();

         while(em.hasNext()) {
            chr = (MapleCharacter)em.next();
            if (chr != null && chr.getName() != null && !chrs.contains(chr)) {
               chrs.add(chr);
            }
         }

         em = AuctionServer.getPlayerStorage().getAllCharacters().values().iterator();

         while(em.hasNext()) {
            chr = (MapleCharacter)em.next();
            if (chr != null && chr.getName() != null && !chrs.contains(chr)) {
               chrs.add(chr);
            }
         }

         em = FarmServer.getPlayerStorage().getAllCharacters().values().iterator();

         while(em.hasNext()) {
            chr = (MapleCharacter)em.next();
            if (chr != null && chr.getName() != null && !chrs.contains(chr)) {
               chrs.add(chr);
            }
         }

         em = chrs.iterator();

         while(em.hasNext()) {
            chr = (MapleCharacter)em.next();
            chr.gainCabinetItem(2434311, 1);
            chr.dropMessage(1, "[HOT] Heinz 핫타임 - 접속 보상이 지급되었습니다. 메이플 보관함을 확인해주세요.");
            chr.dropMessage(6, "[HOT] Heinz 핫타임 - 접속 보상이 지급되었습니다. 메이플 보관함을 확인해주세요.");
         }
      } else if ((new Date()).getHours() != 21 && (new Date()).getMinutes() != 0 && this.give) {
         this.give = false;
      } else if ((new Date()).getHours() != 21 && (new Date()).getMinutes() != 0 && this.give) {
         this.give = false;
      } else if (((new Date()).getHours() == 19 && (new Date()).getMinutes() == 0 || (new Date()).getHours() == 22 && (new Date()).getMinutes() == 0) && !this.worldboss) {
         this.worldboss = true;
         var16 = ChannelServer.getAllInstances().iterator();

         while(var16.hasNext()) {
            cserv = (ChannelServer)var16.next();
            var20 = cserv.getPlayerStorage().getAllCharacters().values().iterator();

            while(var20.hasNext()) {
               hp = (MapleCharacter)var20.next();
               hp.getClient().getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(2012041, 3500, "월드보스 팀대항전이 시작되었다네 참여를 하고 싶으면 나에게 말을 걸어 주게나.", ""));
            }
         }
      } else if (((new Date()).getHours() == 20 && (new Date()).getMinutes() == 0 || (new Date()).getHours() == 23 && (new Date()).getMinutes() == 0) && !this.worldboss) {
         this.worldboss = true;
         int outmap = 100000000;
         em = null;
         chr = null;
         var24 = ChannelServer.getAllInstances().iterator();

         while(var24.hasNext()) {
            ChannelServer cserv = (ChannelServer)var24.next();
            MapleMonster mob = MapleLifeFactory.getMonster(9832024);
            cserv.getMapFactory().getMap(350111399).killAllMonsters(true);
            mob.setHp(9000000000000000000L);
            cserv.getMapFactory().getMap(350111399).spawnMonsterOnGroundBelow(mob, new Point(446, 473));
            Iterator var31 = cserv.getPlayerStorage().getAllCharacters().values().iterator();

            while(var31.hasNext()) {
               MapleCharacter player = (MapleCharacter)var31.next();
               if (player.getMapId() == 993059200) {
                  EventManager em = player.getClient().getChannelServer().getEventSM().getEventManager("WorldBoss");
                  if (em == null) {
                     player.dropMessage(5, "오류가 발생하였습니다.");
                     player.changeChannelMap(1, outmap);
                  } else {
                     player.getClient().removeClickedNPC();
                     NPCScriptManager.getInstance().dispose(player.getClient());
                     player.getClient().getSession().writeAndFlush(CWvsContext.enableActions(player));
                     NPCScriptManager.getInstance().start(player.getClient(), 2008, "WorldBoss");
                  }
               }
            }
         }
      } else if (((new Date()).getHours() == 19 && (new Date()).getMinutes() == 1 || (new Date()).getHours() == 22 && (new Date()).getMinutes() == 1) && this.worldboss) {
         this.worldboss = false;
      } else if (((new Date()).getHours() == 20 && (new Date()).getMinutes() == 1 || (new Date()).getHours() == 23 && (new Date()).getMinutes() == 1) && this.worldboss) {
         this.worldboss = false;
      }

      if ((new Date()).getHours() == 19 && (new Date()).getMinutes() == 50 || (new Date()).getHours() == 20 && (new Date()).getMinutes() == 0 || (new Date()).getHours() == 21 && (new Date()).getMinutes() == 50 || (new Date()).getHours() == 22 && (new Date()).getMinutes() == 0) {
         var16 = ChannelServer.getAllInstances().iterator();

         label267:
         while(var16.hasNext()) {
            cserv = (ChannelServer)var16.next();
            var20 = cserv.getPlayerStorage().getAllCharacters().values().iterator();

            while(true) {
               while(true) {
                  if (!var20.hasNext()) {
                     continue label267;
                  }

                  hp = (MapleCharacter)var20.next();
                  if ((new Date()).getHours() == 19 && (new Date()).getMinutes() == 50) {
                     if (hp.getSkillCustomValue0(20200720) == 0L) {
                        hp.FeverTime(true, true);
                        hp.setSkillCustomInfo(20200720, 1L, 0L);
                     }
                  } else if ((new Date()).getHours() == 20 && (new Date()).getMinutes() == 0) {
                     ServerConstants.feverTime = true;
                     cserv.JuhunFever(true);
                     if (hp.getSkillCustomValue0(20200720) == 0L || hp.getSkillCustomValue0(20200720) == 1L) {
                        hp.FeverTime(true, false);
                        hp.setSkillCustomInfo(20200720, 2L, 0L);
                     }
                  } else if ((new Date()).getHours() == 21 && (new Date()).getMinutes() == 50) {
                     if (hp.getSkillCustomValue0(20200720) == 0L || hp.getSkillCustomValue0(20200720) == 1L || hp.getSkillCustomValue0(20200720) == 2L) {
                        hp.FeverTime(false, true);
                        hp.setSkillCustomInfo(20200720, 3L, 0L);
                     }
                  } else if ((new Date()).getHours() == 22 && (new Date()).getMinutes() == 0) {
                     ServerConstants.feverTime = false;
                     cserv.JuhunFever(false);
                     if (hp.getSkillCustomValue0(20200720) == 0L || hp.getSkillCustomValue0(20200720) == 1L || hp.getSkillCustomValue0(20200720) == 2L || hp.getSkillCustomValue0(20200720) == 3L) {
                        hp.FeverTime(false, false);
                        hp.setSkillCustomInfo(20200720, 4L, 0L);
                     }
                  }
               }
            }
         }
      }

      while(channels.hasNext()) {
         cs = (ChannelServer)channels.next();
         em = cs.getPlayerStorage().getAllCharacters().values().iterator();

         while(em.hasNext()) {
            chr = (MapleCharacter)em.next();
            if (updateAuctionClients.contains(chr.getClient().getAccID())) {
               chr.getClient().getSession().writeAndFlush(CWvsContext.AlarmAuction(0, (AuctionItem)null));
               updateAuctionClients.remove(chr.getClient().getAccID());
            }
         }
      }

   }

   public static void reset(Iterator<ChannelServer> channels) {
      KoreaCalendar kc = new KoreaCalendar();
      ArrayList<MapleGuild> noblessPoint = new ArrayList();
      Iterator var5 = World.Guild.getGuilds().iterator();

      MapleGuild ps1;
      int accid;
      int itemid;
      int itemid2;
      int i2;
      while(var5.hasNext()) {
         ps1 = (MapleGuild)var5.next();
         ps1.setBeforeAttance(ps1.getAfterAttance());
         ps1.setAfterAttance(0);
         long keys = (long)ps1.getLastResetDay();
         GregorianCalendar clear = new GregorianCalendar((int)keys / 10000, (int)(keys % 10000L / 100L) - 1, (int)keys % 100);
         Calendar ocal = Calendar.getInstance();
         accid = clear.get(1);
         itemid = clear.get(5);
         itemid2 = ocal.get(7);
         i2 = clear.get(7);
         int maxday = clear.getMaximum(5);
         int month = clear.get(2);
         int check = i2 == 7 ? 2 : (i2 == 6 ? 3 : (i2 == 5 ? 4 : (i2 == 4 ? 5 : (i2 == 3 ? 6 : (i2 == 2 ? 7 : (i2 == 1 ? 1 : 0))))));
         int afterday = itemid + check;
         if (afterday > maxday) {
            afterday -= maxday;
            ++month;
         }

         if (month > 12) {
            ++accid;
            month = 1;
         }

         if ((new GregorianCalendar(accid, month, afterday)).getTimeInMillis() < System.currentTimeMillis()) {
            if (ps1.getGuildScore() > 0.0D) {
               noblessPoint.add(ps1);
            }

            int[] arrn = new int[]{91001022, 91001023, 91001024, 91001025};
            int n = arrn.length;

            for(int i = 0; i < n; ++i) {
               Integer skillid = arrn[i];
               if (ps1.getSkillLevel(skillid) > 0) {
                  ps1.getGuildSkills().remove(skillid);
                  ps1.broadcast(CWvsContext.GuildPacket.guildSkillPurchased(ps1.getId(), skillid, 0, -1L, "메이플 운영자", "메이플 운영자"));
               }
            }

            ps1.writeToDB(false);
            ps1.setWeekReputation(0);
            ps1.setNoblessSkillPoint(0);
            String var10001 = kc.getYears();
            ps1.setLastResetDay(Integer.parseInt(var10001 + kc.getMonths() + kc.getDays()));
            ps1.broadcast(CWvsContext.GuildPacket.showGuildInfo(ps1));
         }
      }

      Connection connection;
      if (Calendar.getInstance().get(7) == 2) {
         connection = null;
         PreparedStatement preparedStatement1 = null;
         PreparedStatement ps1 = null;
         ResultSet resultSet1 = null;
         ResultSet rs1 = null;

         try {
            connection = DatabaseConnection.getConnection();
            preparedStatement1 = connection.prepareStatement("SELECT * FROM dojorankings order by floor DESC, time DESC", 1005, 1008);
            resultSet1 = preparedStatement1.executeQuery();
            resultSet1.last();
            resultSet1.first();

            for(int i = 1; i <= resultSet1.getRow(); ++i) {
               int id = resultSet1.getInt("playerid");
               String name = "";
               accid = 0;
               itemid = 4001780;
               itemid2 = 4319998;
               if (World.getChar(id) != null) {
                  World.getChar(id).gainCabinetItemPlayer(itemid, 1, 7, "무릉도장 랭커 보상 입니다. 보관 기간 내에 수령하지 않을 시 보관함에서 사라집니다.");
                  World.getChar(id).gainCabinetItemPlayer(itemid2, 1, 7, "무릉도장 랭커 보상 입니다. 보관 기간 내에 수령하지 않을 시 보관함에서 사라집니다.");
                  World.getChar(id).setKeyValue(100466, "Score", "0");
                  World.getChar(id).setKeyValue(100466, "Floor", "0");
               } else {
                  ps1 = connection.prepareStatement("UPDATE questInfo SET customData = ? WHERE quest = ? and characterid = ?");
                  ps1.setString(1, "Score=0;Floor=0;");
                  ps1.setInt(2, 100466);
                  ps1.setInt(3, id);
                  ps1.executeUpdate();
                  ps1 = connection.prepareStatement("SELECT * FROM characters WHERE id = ?");
                  ps1.setInt(1, id);
                  rs1 = ps1.executeQuery();
                  if (rs1.next()) {
                     accid = rs1.getInt("accountid");
                     name = rs1.getString("name");
                  }

                  for(i2 = 0; i2 < 2; ++i2) {
                     ps1 = connection.prepareStatement("INSERT INTO `cabinet` (`accountid`, `itemid`, `count`, `bigname`, `smallname`, `savetime`, `delete`, `playerid`, `name`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
                     ps1.setLong(1, (long)accid);
                     ps1.setInt(2, i2 == 0 ? itemid : itemid2);
                     ps1.setInt(3, 1);
                     ps1.setString(4, "[Heinz]");
                     ps1.setString(5, "무릉도장 랭커 보상 입니다. 보관 기간 내에 수령하지 않을 시 보관함에서 사라집니다.");
                     ps1.setLong(6, PacketHelper.getKoreanTimestamp(System.currentTimeMillis() + 604800000L));
                     ps1.setInt(7, 0);
                     ps1.setInt(8, id);
                     ps1.setString(9, name);
                     ps1.execute();
                  }
               }

               if (i >= 3) {
                  break;
               }

               resultSet1.next();
            }

            resultSet1.close();
            preparedStatement1.close();
            ps1 = connection.prepareStatement("DELETE FROM dojorankings");
            ps1.executeUpdate();
            ps1.close();
            rs1.close();
            connection.close();
         } catch (SQLException var51) {
         } finally {
            try {
               if (connection != null) {
                  connection.close();
               }

               if (preparedStatement1 != null) {
                  preparedStatement1.close();
               }

               if (resultSet1 != null) {
                  resultSet1.close();
               }

               if (rs1 != null) {
                  rs1.close();
               }

               if (ps1 != null) {
                  ps1.close();
               }
            } catch (SQLException var47) {
            }

            World.Broadcast.broadcastMessage(CWvsContext.serverMessage(6, 1, "", "무릉도장 랭킹이 갱신 되었습니다.", true));
         }
      }

      if (Calendar.getInstance().get(7) == 4) {
         ServerConstants.starForceSalePercent = 10;
      } else {
         ServerConstants.starForceSalePercent = 0;
      }

      if (!noblessPoint.isEmpty()) {
         Collections.sort(noblessPoint);
         int skilllv = 60;
         Iterator var56 = noblessPoint.iterator();

         while(var56.hasNext()) {
            MapleGuild g = (MapleGuild)var56.next();
            if (skilllv < 10) {
               skilllv = 10;
            }

            g.setNoblessSkillPoint(skilllv);
            if (skilllv > 10) {
               skilllv -= 3;
            }

            g.setGuildScore(0.0D);
            g.broadcast(CWvsContext.GuildPacket.showGuildInfo(g));
         }
      }

      MapleGuildRanking.getInstance().load();
      connection = null;
      ps1 = null;
      ResultSet rs = null;

      try {
         connection = DatabaseConnection.getConnection();
         Object ps = connection.prepareStatement("SELECT * FROM characters");
         rs = ps1.executeQuery();

         while(rs.next()) {
            ids.put(rs.getString("name"), rs.getInt("id"));
         }

         ps1.close();
         rs.close();
         connection.close();
      } catch (SQLException var49) {
         var49.printStackTrace();
      } finally {
         try {
            if (connection != null) {
               connection.close();
            }

            if (ps1 != null) {
               ps1.close();
            }

            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var48) {
            var48.printStackTrace();
         }

      }

      while(channels.hasNext()) {
         ChannelServer cs = (ChannelServer)channels.next();
         Iterator iterator = cs.getPlayerStorage().getAllCharacters().values().iterator();

         while(iterator.hasNext()) {
            MapleCharacter chr = (MapleCharacter)iterator.next();
            chr.checkRestDay(false, false);
            chr.checkRestDay(false, true);
            chr.checkRestDay(true, false);
            chr.checkRestDay(true, true);
            chr.checkRestDayMonday();
            ResetHandler(chr);
            if (Calendar.getInstance().get(7) == 7) {
               ServerConstants.feverTime = true;
               chr.FeverTime(true, false);
            } else {
               ServerConstants.feverTime = false;
            }
         }
      }

      Iterator chrs = AuctionServer.getPlayerStorage().getAllCharacters().values().iterator();

      while(chrs.hasNext()) {
         MapleCharacter chr = (MapleCharacter)chrs.next();
         chr.checkRestDay(false, false);
         chr.checkRestDay(false, true);
         chr.checkRestDay(true, false);
         chr.checkRestDay(true, true);
         chr.checkRestDayMonday();
         ResetHandler(chr);
      }

   }

   public static void ResetHandler(MapleCharacter chr) {
   }

   static {
      todayKeyValues.put("muto", "0");
      todayKeyValues.put("arcane_quest_2", "-1");
      todayKeyValues.put("arcane_quest_3", "-1");
      todayKeyValues.put("arcane_quest_4", "-1");
      todayKeyValues.put("arcane_quest_5", "-1");
      todayKeyValues.put("arcane_quest_6", "-1");
      todayKeyValues.put("arcane_quest_7", "-1");
      todayKeyValues.put("NettPyramid", "0");
      todayKeyValues.put("linkMobCount", "0");
      ids = new HashMap();
      todayKeyValues = new HashMap();
      weekKeyValues = new String[][]{{"db_lastweek", "0"}, {"dojo", "0"}, {"dojo_time", "0"}};
      clientDateKeyValues = new String[]{"dailyGiftComplete", "hotelMapleToday", "ht", "mPark", "mpark_t", "jump_1", "jump_2", "jump_3", "day_reborn_1", "day_reborn_2", "day_qitem", "day_summer_a", "day_summer_e", "day_colorlens", "day_MedalC", "day_MedalD", "minigame"};
      updateAuctionClients = new ArrayList();
      todayKeyValues.put("muto", "0");
      todayKeyValues.put("arcane_quest_2", "-1");
      todayKeyValues.put("arcane_quest_3", "-1");
      todayKeyValues.put("arcane_quest_4", "-1");
      todayKeyValues.put("arcane_quest_5", "-1");
      todayKeyValues.put("arcane_quest_6", "-1");
      todayKeyValues.put("arcane_quest_7", "-1");
      todayKeyValues.put("NettPyramid", "0");
      todayKeyValues.put("linkMobCount", "0");
   }
}
