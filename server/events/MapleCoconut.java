package server.events;

import client.MapleCharacter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import server.Timer;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class MapleCoconut extends MapleEvent {
   private List<MapleCoconut.MapleCoconuts> coconuts = new LinkedList();
   private int[] coconutscore = new int[2];
   private int countBombing = 0;
   private int countFalling = 0;
   private int countStopped = 0;

   public MapleCoconut(int channel, MapleEventType type) {
      super(channel, type);
   }

   public void finished(MapleCharacter chr) {
   }

   public void reset() {
      super.reset();
      this.resetCoconutScore();
   }

   public void unreset() {
      super.unreset();
      this.resetCoconutScore();
      this.setHittable(false);
   }

   public void onMapLoad(MapleCharacter chr) {
      super.onMapLoad(chr);
   }

   public MapleCoconut.MapleCoconuts getCoconut(int id) {
      return id >= this.coconuts.size() ? null : (MapleCoconut.MapleCoconuts)this.coconuts.get(id);
   }

   public List<MapleCoconut.MapleCoconuts> getAllCoconuts() {
      return this.coconuts;
   }

   public void setHittable(boolean hittable) {
      Iterator var2 = this.coconuts.iterator();

      while(var2.hasNext()) {
         MapleCoconut.MapleCoconuts nut = (MapleCoconut.MapleCoconuts)var2.next();
         nut.setHittable(hittable);
      }

   }

   public int getBombings() {
      return this.countBombing;
   }

   public void bombCoconut() {
      --this.countBombing;
   }

   public int getFalling() {
      return this.countFalling;
   }

   public void fallCoconut() {
      --this.countFalling;
   }

   public int getStopped() {
      return this.countStopped;
   }

   public void stopCoconut() {
      --this.countStopped;
   }

   public int[] getCoconutScore() {
      return this.coconutscore;
   }

   public int getMapleScore() {
      return this.coconutscore[0];
   }

   public int getStoryScore() {
      return this.coconutscore[1];
   }

   public void addMapleScore() {
      int var10002 = this.coconutscore[0]++;
   }

   public void addStoryScore() {
      int var10002 = this.coconutscore[1]++;
   }

   public void resetCoconutScore() {
      this.coconutscore[0] = 0;
      this.coconutscore[1] = 0;
      this.countBombing = 80;
      this.countFalling = 401;
      this.countStopped = 20;
      this.coconuts.clear();

      for(int i = 0; i < 506; ++i) {
         this.coconuts.add(new MapleCoconut.MapleCoconuts());
      }

   }

   public void startEvent() {
      this.reset();
      this.setHittable(true);
      this.getMap(0).broadcastMessage(CWvsContext.serverNotice(5, "", "The event has started!!"));
      this.getMap(0).broadcastMessage(CField.getClock(300));
      Timer.EventTimer.getInstance().schedule(new Runnable() {
         public void run() {
            if (MapleCoconut.this.getMapleScore() == MapleCoconut.this.getStoryScore()) {
               MapleCoconut.this.bonusTime();
            } else {
               Iterator var1 = MapleCoconut.this.getMap(0).getCharactersThreadsafe().iterator();

               while(var1.hasNext()) {
                  MapleCharacter chr = (MapleCharacter)var1.next();
                  if (chr.getTeam() == (MapleCoconut.this.getMapleScore() > MapleCoconut.this.getStoryScore() ? 0 : 1)) {
                     chr.getClient().getSession().writeAndFlush(CField.showEffect("event/coconut/victory"));
                     chr.getClient().getSession().writeAndFlush(CField.playSound("Coconut/Victory"));
                  } else {
                     chr.getClient().getSession().writeAndFlush(CField.showEffect("event/coconut/lose"));
                     chr.getClient().getSession().writeAndFlush(CField.playSound("Coconut/Failed"));
                  }
               }

               MapleCoconut.this.warpOut();
            }

         }
      }, 300000L);
   }

   public void bonusTime() {
      this.getMap(0).broadcastMessage(CField.getClock(60));
      Timer.EventTimer.getInstance().schedule(new Runnable() {
         public void run() {
            Iterator var1;
            MapleCharacter chr;
            if (MapleCoconut.this.getMapleScore() == MapleCoconut.this.getStoryScore()) {
               var1 = MapleCoconut.this.getMap(0).getCharactersThreadsafe().iterator();

               while(var1.hasNext()) {
                  chr = (MapleCharacter)var1.next();
                  chr.getClient().getSession().writeAndFlush(CField.showEffect("event/coconut/lose"));
                  chr.getClient().getSession().writeAndFlush(CField.playSound("Coconut/Failed"));
               }

               MapleCoconut.this.warpOut();
            } else {
               var1 = MapleCoconut.this.getMap(0).getCharactersThreadsafe().iterator();

               while(var1.hasNext()) {
                  chr = (MapleCharacter)var1.next();
                  if (chr.getTeam() == (MapleCoconut.this.getMapleScore() > MapleCoconut.this.getStoryScore() ? 0 : 1)) {
                     chr.getClient().getSession().writeAndFlush(CField.showEffect("event/coconut/victory"));
                     chr.getClient().getSession().writeAndFlush(CField.playSound("Coconut/Victory"));
                  } else {
                     chr.getClient().getSession().writeAndFlush(CField.showEffect("event/coconut/lose"));
                     chr.getClient().getSession().writeAndFlush(CField.playSound("Coconut/Failed"));
                  }
               }

               MapleCoconut.this.warpOut();
            }

         }
      }, 60000L);
   }

   public void warpOut() {
      this.setHittable(false);
      Timer.EventTimer.getInstance().schedule(new Runnable() {
         public void run() {
            MapleCharacter chr;
            for(Iterator var1 = MapleCoconut.this.getMap(0).getCharactersThreadsafe().iterator(); var1.hasNext(); MapleCoconut.this.warpBack(chr)) {
               chr = (MapleCharacter)var1.next();
               if (MapleCoconut.this.getMapleScore() > MapleCoconut.this.getStoryScore() && chr.getTeam() == 0 || MapleCoconut.this.getStoryScore() > MapleCoconut.this.getMapleScore() && chr.getTeam() == 1) {
                  MapleEvent.givePrize(chr);
               }
            }

            MapleCoconut.this.unreset();
         }
      }, 10000L);
   }

   public static class MapleCoconuts {
      private int hits = 0;
      private boolean hittable = false;
      private boolean stopped = false;
      private long hittime = System.currentTimeMillis();

      public void hit() {
         this.hittime = System.currentTimeMillis() + 1000L;
         ++this.hits;
      }

      public int getHits() {
         return this.hits;
      }

      public void resetHits() {
         this.hits = 0;
      }

      public boolean isHittable() {
         return this.hittable;
      }

      public void setHittable(boolean hittable) {
         this.hittable = hittable;
      }

      public boolean isStopped() {
         return this.stopped;
      }

      public void setStopped(boolean stopped) {
         this.stopped = stopped;
      }

      public long getHitTime() {
         return this.hittime;
      }
   }
}
