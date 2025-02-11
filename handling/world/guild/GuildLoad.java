package handling.world.guild;

import handling.world.World;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GuildLoad {
   public static final int NumSavingThreads = 6;
   private static final GuildLoad.TimingThread[] Threads = new GuildLoad.TimingThread[6];
   private static final AtomicInteger Distribute;

   public static void QueueGuildForLoad(int hm) {
      int Current = Distribute.getAndIncrement() % 6;
      Threads[Current].getRunnable().Queue(hm);
   }

   public static void Execute(Object ToNotify) {
      int i;
      for(i = 0; i < Threads.length; ++i) {
         Threads[i].getRunnable().SetToNotify(ToNotify);
      }

      for(i = 0; i < Threads.length; ++i) {
         Threads[i].start();
      }

   }

   static {
      for(int i = 0; i < Threads.length; ++i) {
         Threads[i] = new GuildLoad.TimingThread(new GuildLoad.GuildLoadRunnable());
      }

      Distribute = new AtomicInteger(0);
   }

   private static class TimingThread extends Thread {
      private final GuildLoad.GuildLoadRunnable ext;

      public TimingThread(GuildLoad.GuildLoadRunnable r) {
         super(r);
         this.ext = r;
      }

      public GuildLoad.GuildLoadRunnable getRunnable() {
         return this.ext;
      }
   }

   private static class GuildLoadRunnable implements Runnable {
      private Object ToNotify;
      private ArrayBlockingQueue<Integer> Queue = new ArrayBlockingQueue(1000);

      public void run() {
         while(true) {
            try {
               if (!this.Queue.isEmpty()) {
                  World.Guild.addLoadedGuild(new MapleGuild((Integer)this.Queue.take()));
                  continue;
               }

               synchronized(this.ToNotify) {
                  this.ToNotify.notify();
               }
            } catch (InterruptedException var4) {
               Logger.getLogger(GuildLoad.class.getName()).log(Level.SEVERE, (String)null, var4);
            }

            return;
         }
      }

      private void Queue(Integer hm) {
         this.Queue.add(hm);
      }

      private void SetToNotify(Object o) {
         if (this.ToNotify == null) {
            this.ToNotify = o;
         }

      }
   }
}
