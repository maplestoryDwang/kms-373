package server;

import client.DreamBreakerRank;
import client.MapleCharacter;
import client.SkillFactory;
import client.inventory.MapleInventoryIdentifier;
import connector.ConnectorServer;
import constants.GameConstants;
import constants.ServerConstants;
import constants.programs.AdminTool;
import constants.programs.GarbageDataBaseRemover;
import database.DatabaseConnection;
import handling.MapleSaveHandler;
import handling.auction.AuctionServer;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.channel.MapleGuildRanking;
import handling.channel.handler.MatrixHandler;
import handling.channel.handler.UnionHandler;
import handling.farm.FarmServer;
import handling.login.LoginInformationProvider;
import handling.login.LoginServer;
import handling.world.World;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import server.control.MapleEtcControl;
import server.control.MapleIndexTimer;
import server.events.MapleOxQuizFactory;
import server.field.boss.FieldSkillFactory;
import server.field.boss.lucid.Butterfly;
import server.field.boss.will.SpiderWeb;
import server.life.AffectedOtherSkillInfo;
import server.life.EliteMonsterGradeInfo;
import server.life.MapleLifeFactory;
import server.life.MobAttackInfoFactory;
import server.life.MobSkillFactory;
import server.life.PlayerNPC;
import server.marriage.MarriageManager;
import server.quest.MapleQuest;
import server.quest.QuestCompleteStatus;
import tools.CMDCommand;
import tools.packet.BossRewardMeso;
import tools.packet.CField;
import tools.packet.SLFCGPacket;

public class Start {
   public static transient ScheduledFuture<?> boss;
   public static long startTime = System.currentTimeMillis();
   public static final Start instance = new Start();
   public static AtomicInteger CompletedLoadingThreads = new AtomicInteger(0);

   public void run() throws InterruptedException {
      System.setProperty("nashorn.args", "--no-deprecation-warning");
      DatabaseConnection.init();
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT * FROM auth_server_channel_ip");
            rs = ps.executeQuery();

            while(rs.next()) {
               ServerProperties.setProperty(rs.getString("name") + rs.getInt("channelid"), rs.getString("value"));
            }

            rs.close();
            ps.close();
            con.close();
         } catch (SQLException var32) {
            var32.printStackTrace();
            System.exit(0);
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
            } catch (SQLException var29) {
               var29.printStackTrace();
            }

         }

         if (Boolean.parseBoolean(ServerProperties.getProperty("world.admin"))) {
            ServerConstants.Use_Fixed_IV = false;
            System.out.println("[!!! Admin Only Mode Active !!!]");
         }

         System.setProperty("wz", "wz");

         try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("UPDATE accounts SET loggedin = 0, allowed = 0");
            ps.executeUpdate();
            ps.close();
            con.close();
         } catch (SQLException var30) {
            throw new RuntimeException("[EXCEPTION] Please check if the SQL server is active.");
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
            } catch (SQLException var28) {
               var28.printStackTrace();
            }

         }

         World.init();
         Timer.WorldTimer.getInstance().start();
         Timer.EtcTimer.getInstance().start();
         Timer.MapTimer.getInstance().start();
         Timer.MobTimer.getInstance().start();
         Timer.CloneTimer.getInstance().start();
         Timer.EventTimer.getInstance().start();
         Timer.BuffTimer.getInstance().start();
         Timer.PingTimer.getInstance().start();
         Timer.ShowTimer.getInstance().start();
         Date date = new Date();
         if (date.getDay() == 1) {
            GarbageDataBaseRemover.main();
         }

         ServerConstants.mirrors.add(new DimentionMirrorEntry("자유 전직", "", 200, 0, 0, "1541032", new ArrayList(Arrays.asList(4310086))));
         ServerConstants.mirrors.add(new DimentionMirrorEntry("닉네임변경", "", 10, 1, 1, "9062010", new ArrayList(Arrays.asList(4034803))));
         ServerConstants.mirrors.add(new DimentionMirrorEntry("계승 시스템", "", 300, 2, 2, "9062116", new ArrayList()));
         ServerConstants.mirrors.add(new DimentionMirrorEntry("무릉도장", "", 10, 3, 3, "9900004", new ArrayList()));
         ServerConstants.mirrors.add(new DimentionMirrorEntry("몬스터파크", "", 10, 4, 4, "9071003", new ArrayList()));
         ServerConstants.mirrors.add(new DimentionMirrorEntry("티어시스템", "", 100, 5, 5, "2007", new ArrayList()));
         ServerConstants.mirrors.add(new DimentionMirrorEntry("추천인시스템", "", 10, 6, 6, "3001931", new ArrayList()));
         ServerConstants.mirrors.add(new DimentionMirrorEntry("제작 및 강화", "", 100, 7, 7, "2400003", new ArrayList()));
         ServerConstants.mirrors.add(new DimentionMirrorEntry("안개 수련장", "", 200, 8, 8, "9062318", new ArrayList()));
         ServerConstants.mirrors.add(new DimentionMirrorEntry("커플 컨텐츠", "", 10, 9, 9, "9201000", new ArrayList()));
         ServerConstants.mirrors.add(new DimentionMirrorEntry("유니온 시스템", "", 109, 10, 10, "9010106", new ArrayList()));
         ServerConstants.mirrors.add(new DimentionMirrorEntry("룰렛 시스템", "", 109, 11, 11, "9000155", new ArrayList()));
         ServerConstants.quicks.add(new QuickMoveEntry(1, 2000, 0, 10, "주요 지역으로 캐릭터를 이동시켜 주는 #c<워프 시스템>#을 이용한다."));
         ServerConstants.WORLD_UI = ServerProperties.getProperty("login.serverUI");
         ServerConstants.ChangeMapUI = Boolean.parseBoolean(ServerProperties.getProperty("login.ChangeMapUI"));
         DreamBreakerRank.LoadRank();
         JamsuPoint();
         Butterfly.load();
         SpiderWeb.load();
         Setting.CashShopSetting();
         Start.AllLoding allLoding = new Start.AllLoding();
         allLoding.start();
         System.out.println("[Loading LOGIN]");
         LoginServer.run_startup_configurations();
         System.out.println("[Loading CHANNEL]");
         ChannelServer.startChannel_Main();
         System.out.println("[Loading CASH SHOP]");
         CashShopServer.run_startup_configurations();
         System.out.println("[Loading Farm]");
         FarmServer.run_startup_configurations();
         Runtime.getRuntime().addShutdownHook(new Thread(new Start.Shutdown()));
         PlayerNPC.loadAll();
         LoginServer.setOn();
         Timer.WorldTimer.getInstance().register(new MapleEtcControl(), 1000L);
         EliteMonsterGradeInfo.loadFromWZData();
         AffectedOtherSkillInfo.loadFromWZData();
         InnerAbillity.getInstance().load();
         Setting.setting();
         Setting.setting2();
         Setting.settingGoldApple();
         Setting.settingNeoPos();
         BossRewardMeso.Setting();
         Timer.WorldTimer.getInstance().register(new MapleIndexTimer(), 1000L);
         Timer.WorldTimer.getInstance().register(new MapleSaveHandler(), 10000L);
         (new AdminTool()).setVisible(true);
         ConnectorServer.run();
      } catch (Exception var34) {
      }

   }

   public static void main(String[] args) throws IOException, InterruptedException {
      instance.run();
   }

   public static void JamsuPoint() {
      Timer.WorldTimer tMan = Timer.WorldTimer.getInstance();
      Runnable r = new Runnable() {
         public void run() {
            Iterator var1 = ChannelServer.getAllInstances().iterator();

            label41:
            while(var1.hasNext()) {
               ChannelServer cserv = (ChannelServer)var1.next();
               Iterator var3 = cserv.getPlayerStorage().getAllCharacters().values().iterator();

               while(true) {
                  while(true) {
                     if (!var3.hasNext()) {
                        continue label41;
                     }

                     MapleCharacter mch = (MapleCharacter)var3.next();
                     if (mch.getMapId() != 120043000 && mch.getMapId() != 993215603) {
                        mch.JamsuTime = 0;
                        mch.isFirst = false;
                     } else {
                        if (!mch.isFirst) {
                           mch.getClient().send(CField.UIPacket.detailShowInfo("잠수 포인트 적립을 시작합니다.", 3, 20, 20));
                           mch.getClient().getSession().writeAndFlush(SLFCGPacket.playSE("Sound/MiniGame.img/14thTerra/reward"));
                           mch.isFirst = true;
                        }

                        if (mch.getClient().getKeyValue("jamsupoint") == null) {
                           mch.getClient().setKeyValue("jamsupoint", "0");
                        }

                        ++mch.JamsuTime;
                        if (mch.JamsuTime >= 60) {
                           mch.JamsuTime = 0;
                           ++mch.Jamsu5m;
                           long point = mch.getPlayer().getKeyValue(501368, "point");
                           point += 2L;
                           mch.getPlayer().setKeyValue(501368, "point", point.makeConcatWithConstants<invokedynamic>(point));
                           if (mch.Jamsu5m >= 5) {
                              mch.getClient().send(CField.UIPacket.detailShowInfo("잠수 포인트가 적립되었습니다. 잠수 포인트 : " + mch.getPlayer().getKeyValue(501368, "point"), 3, 20, 20));
                              mch.Jamsu5m = 0;
                           }
                        }
                     }
                  }
               }
            }

         }
      };
      tMan.register(r, 1000L);
   }

   private String getMachineIp() {
      InetAddress local = null;

      try {
         local = InetAddress.getLocalHost();
      } catch (UnknownHostException var3) {
         var3.printStackTrace();
      }

      if (local == null) {
         return "";
      } else {
         String ip = local.getHostAddress();
         return ip;
      }
   }

   private class AllLoding extends Thread {
      public void run() {
         Start.LoadingThread SkillLoader = new Start.LoadingThread(() -> {
            SkillFactory.load();
         }, "SkillLoader", this);
         Start.LoadingThread QuestLoader = new Start.LoadingThread(() -> {
            MapleQuest.initQuests();
            MapleLifeFactory.loadQuestCounts();
         }, "QuestLoader", this);
         Start.LoadingThread QuestCustomLoader = new Start.LoadingThread(() -> {
            MapleLifeFactory.loadNpcScripts();
            QuestCompleteStatus.run();
         }, "QuestCustomLoader", this);
         Start.LoadingThread ItemLoader = new Start.LoadingThread(() -> {
            MapleInventoryIdentifier.getInstance();
            CashItemFactory.getInstance().initialize();
            MapleItemInformationProvider.getInstance().runEtc();
            MapleItemInformationProvider.getInstance().runItems();
            AuctionServer.run_startup_configurations();
         }, "ItemLoader", this);
         Start.LoadingThread GuildRankingLoader = new Start.LoadingThread(() -> {
            MapleGuildRanking.getInstance().load();
         }, "GuildRankingLoader", this);
         Start.LoadingThread EtcLoader = new Start.LoadingThread(() -> {
            LoginInformationProvider.getInstance();
            RandomRewards.load();
            MapleOxQuizFactory.getInstance();
            UnionHandler.loadUnion();
         }, "EtcLoader", this);
         Start.LoadingThread MonsterLoader = new Start.LoadingThread(() -> {
            MobSkillFactory.getInstance();
            FieldSkillFactory.getInstance();
            MobAttackInfoFactory.getInstance();
         }, "MonsterLoader", this);
         Start.LoadingThread EmoticonLoader = new Start.LoadingThread(() -> {
            ChatEmoticon.LoadEmoticon();
         }, "EmoticonLoader", this);
         Start.LoadingThread MatrixLoader = new Start.LoadingThread(() -> {
            MatrixHandler.loadCore();
         }, "MatrixLoader", this);
         Start.LoadingThread MarriageLoader = new Start.LoadingThread(() -> {
            MarriageManager.getInstance();
         }, "MarriageLoader", this);
         Start.LoadingThread[] LoadingThreads = new Start.LoadingThread[]{SkillLoader, QuestLoader, QuestCustomLoader, ItemLoader, GuildRankingLoader, EtcLoader, MonsterLoader, MatrixLoader, MarriageLoader, EmoticonLoader};
         Start.LoadingThread[] var12 = LoadingThreads;
         int var13 = LoadingThreads.length;

         for(int var14 = 0; var14 < var13; ++var14) {
            Thread t = var12[var14];
            t.start();
         }

         synchronized(this) {
            try {
               this.wait();
            } catch (InterruptedException var20) {
               var20.printStackTrace();
            }
         }

         while(Start.CompletedLoadingThreads.get() != LoadingThreads.length) {
            synchronized(this) {
               try {
                  this.wait();
               } catch (InterruptedException var18) {
                  var18.printStackTrace();
               }
            }
         }

         World.Guild.load();
         GameConstants.isOpen = true;
         PrintStream var10000 = System.out;
         long var10001 = System.currentTimeMillis() - Start.startTime;
         var10000.println("[Fully Initialized in " + var10001 / 1000L + " seconds]");
         if (!ServerConstants.ConnectorSetting) {
            CMDCommand.main();
         }

      }
   }

   public static class Shutdown implements Runnable {
      public void run() {
         ShutdownServer.getInstance().run();
      }
   }

   private static class NotifyingRunnable implements Runnable {
      private String LoadingThreadName;
      private long StartTime;
      private Runnable WrappedRunnable;
      private final Object ToNotify;

      private NotifyingRunnable(Runnable r, Object o, String name) {
         this.WrappedRunnable = r;
         this.ToNotify = o;
         this.LoadingThreadName = name;
      }

      public void run() {
         this.StartTime = System.currentTimeMillis();
         this.WrappedRunnable.run();
         String var10001 = this.LoadingThreadName;
         System.out.println("[Loading Completed] " + var10001 + " | Completed in " + (System.currentTimeMillis() - this.StartTime) + " Milliseconds. (" + (Start.CompletedLoadingThreads.get() + 1) + "/10)");
         synchronized(this.ToNotify) {
            Start.CompletedLoadingThreads.incrementAndGet();
            this.ToNotify.notify();
         }
      }
   }

   private static class LoadingThread extends Thread {
      protected String LoadingThreadName;

      private LoadingThread(Runnable r, String t, Object o) {
         super(new Start.NotifyingRunnable(r, o, t));
         this.LoadingThreadName = t;
      }

      public synchronized void start() {
         System.out.println("[Loading...] Started " + this.LoadingThreadName + " Thread");
         super.start();
      }
   }
}
