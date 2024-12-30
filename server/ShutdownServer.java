package server;

import handling.auction.AuctionServer;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.World;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import server.marriage.MarriageManager;
import tools.packet.CWvsContext;

public class ShutdownServer implements ShutdownServerMBean {
   public static final ShutdownServer instance = new ShutdownServer();
   public static AtomicInteger CompletedLoadingThreads = new AtomicInteger(0);
   public long startTime = 0L;
   public int mode = 0;

   public static ShutdownServer getInstance() {
      return instance;
   }

   public void shutdown() {
      this.run();
   }

   public void run() {
      this.startTime = System.currentTimeMillis();
      if (this.mode == 0) {
         World.Broadcast.broadcastMessage(CWvsContext.serverNotice(0, "", "서버가 곧 종료됩니다. 안전한 저장을 위해 게임을 종료해주세요."));
         World.Guild.save();
         World.Alliance.save();
         AuctionServer.saveItems();
         MarriageManager.getInstance().saveAll();
         System.out.println("Shutdown 1 has completed.");
         ++this.mode;
      } else if (this.mode == 1) {
         ++this.mode;
         System.out.println("Shutdown 2 commencing...");
         World.Broadcast.broadcastMessage(CWvsContext.serverNotice(0, "", "서버가 종료됩니다. 안전한 저장을 위해 게임을 종료해주세요."));
         ShutdownServer.AllShutdown sd = new ShutdownServer.AllShutdown();
         sd.start();
      }

   }

   private class AllShutdown extends Thread {
      public void run() {
         List<ShutdownServer.LoadingThread> loadingThreads = new ArrayList();
         Integer[] array = (Integer[])ChannelServer.getAllInstance().toArray(new Integer[0]);
         Integer[] var4 = array;
         int var5 = array.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            int i = var4[var6];

            try {
               ShutdownServer.LoadingThread thread = new ShutdownServer.LoadingThread(new Runnable(i) {
                  // $FF: synthetic field
                  final int val$i;
                  // $FF: synthetic field
                  final ShutdownServer.AllShutdown this$1;

                  {
                     this.this$1 = this$1;
                     this.val$i = var2;
                  }

                  public void run() {
                     ChannelServer cs = ChannelServer.getInstance(this.val$i);
                     cs.shutdown();
                  }
               }, "Channel " + i, this);
               loadingThreads.add(thread);
            } catch (Exception var25) {
               var25.printStackTrace();
            }
         }

         Iterator var26 = loadingThreads.iterator();

         while(var26.hasNext()) {
            Thread t = (Thread)var26.next();
            t.start();
         }

         synchronized(this) {
            try {
               this.wait();
            } catch (InterruptedException var23) {
               var23.printStackTrace();
            }
         }

         while(ShutdownServer.CompletedLoadingThreads.get() != loadingThreads.size()) {
            synchronized(this) {
               try {
                  this.wait();
               } catch (InterruptedException var21) {
                  var21.printStackTrace();
               }
            }
         }

         Timer.WorldTimer.getInstance().stop();
         Timer.MapTimer.getInstance().stop();
         Timer.MobTimer.getInstance().stop();
         Timer.BuffTimer.getInstance().stop();
         Timer.CloneTimer.getInstance().stop();
         Timer.EventTimer.getInstance().stop();
         Timer.EtcTimer.getInstance().stop();
         Timer.PingTimer.getInstance().stop();
         LoginServer.shutdown();
         CashShopServer.shutdown();
         AuctionServer.shutdown();
         PrintStream var10000 = System.out;
         long var10001 = System.currentTimeMillis() - ShutdownServer.this.startTime;
         var10000.println("[Fully Shutdowned in " + var10001 / 1000L + " seconds]");
         System.out.println("Shutdown 2 has finished.");

         try {
            Thread.sleep(1000L);
         } catch (Exception var19) {
         } finally {
            System.exit(0);
         }

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
         System.out.println("[Loading Completed] " + var10001 + " | Completed in " + (System.currentTimeMillis() - this.StartTime) + " Milliseconds. (" + (ShutdownServer.CompletedLoadingThreads.get() + 1) + "/10)");
         synchronized(this.ToNotify) {
            ShutdownServer.CompletedLoadingThreads.incrementAndGet();
            this.ToNotify.notify();
         }
      }
   }

   private static class LoadingThread extends Thread {
      protected String LoadingThreadName;

      private LoadingThread(Runnable r, String t, Object o) {
         super(new ShutdownServer.NotifyingRunnable(r, o, t));
         this.LoadingThreadName = t;
      }

      public synchronized void start() {
         System.out.println("[Loading...] Started " + this.LoadingThreadName + " Thread");
         super.start();
      }
   }
}
