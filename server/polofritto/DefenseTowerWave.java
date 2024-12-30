package server.polofritto;

import client.MapleClient;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import server.Timer;
import server.life.MapleLifeFactory;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.SLFCGPacket;

public class DefenseTowerWave {
   private int wave;
   private int life;
   private ScheduledFuture<?> sc;
   private ScheduledFuture<?> sch;
   private List<List<Integer>> waveData = new ArrayList();

   public DefenseTowerWave(int wave, int life) {
      this.wave = wave;
      this.life = life;
   }

   public int getWave() {
      return this.wave;
   }

   public void setWave(int wave) {
      this.wave = wave;
   }

   public int getLife() {
      return this.life;
   }

   public void setLife(int life) {
      this.life = life;
   }

   public void updateDefenseWave(MapleClient c) {
      c.getSession().writeAndFlush(SLFCGPacket.setTowerDefenseWave(this.wave));
   }

   public void updateDefenseLife(MapleClient c) {
      c.getSession().writeAndFlush(SLFCGPacket.setTowerDefenseLife(this.life));
   }

   public void updateNewWave(final MapleClient c) {
      c.getSession().writeAndFlush(CField.environmentChange("defense/count", 16));
      Timer.EventTimer.getInstance().schedule(new Runnable() {
         public void run() {
            c.getSession().writeAndFlush(CField.environmentChange("defense/wave/" + DefenseTowerWave.this.wave, 16));
            c.getSession().writeAndFlush(CField.environmentChange("killing/first/start", 16));
            int time = 0;
            if (c.getPlayer().getMapId() == 993000100) {
               Iterator iterator = ((List)DefenseTowerWave.this.waveData.get(DefenseTowerWave.this.wave - 1)).iterator();

               while(iterator.hasNext()) {
                  int wave = (Integer)iterator.next();
                  Timer.EventTimer var10000 = Timer.EventTimer.getInstance();
                  Runnable var10001 = new Runnable(wave) {
                     // $FF: synthetic field
                     final int val$wave;
                     // $FF: synthetic field
                     final <undefinedtype> this$1;

                     {
                        this.this$1 = this$1;
                        this.val$wave = var2;
                     }

                     public void run() {
                        if (this.this$1.val$c.getPlayer().getMapId() == 993000100) {
                           this.this$1.val$c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(this.val$wave), new Point(363, 165));
                           this.this$1.val$c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(this.val$wave), new Point(332, -195));
                        }

                     }
                  };
                  ++time;
                  var10000.schedule(var10001, (long)(1000 * time));
               }
            }

         }
      }, 3000L);
      Timer.EventTimer.getInstance().schedule(new Runnable() {
         public void run() {
            DefenseTowerWave.this.checkFinish(c);
         }
      }, (long)((((List)this.waveData.get(this.wave - 1)).size() + 5) * 1000));
   }

   public void checkFinish(final MapleClient c) {
      this.sch = Timer.EventTimer.getInstance().register(new Runnable() {
         public void run() {
            if (c != null && c.getPlayer() != null && c.getPlayer().getMap() != null && c.getPlayer().getMap().getNumMonsters() == 0) {
               if (DefenseTowerWave.this.wave < 3 && c.getPlayer().getMapId() == 993000100) {
                  ++DefenseTowerWave.this.wave;
                  if (DefenseTowerWave.this.sch != null) {
                     DefenseTowerWave.this.sch.cancel(false);
                  }

                  c.getSession().writeAndFlush(CWvsContext.getTopMsg("WAVE를 막아냈습니다. 다음 WAVE를 준비해주세요."));
                  Timer.EventTimer.getInstance().schedule(new Runnable() {
                     // $FF: synthetic field
                     final <undefinedtype> this$1;

                     {
                        this.this$1 = this$1;
                     }

                     public void run() {
                        this.this$1.this$0.updateDefenseWave(this.this$1.val$c);
                        this.this$1.this$0.updateNewWave(this.this$1.val$c);
                     }
                  }, 2000L);
               } else {
                  if (DefenseTowerWave.this.sc != null) {
                     DefenseTowerWave.this.sc.cancel(true);
                  }

                  if (DefenseTowerWave.this.sch != null) {
                     DefenseTowerWave.this.sch.cancel(true);
                  }

                  c.getSession().writeAndFlush(CField.environmentChange("killing/clear", 16));
                  Timer.EventTimer.getInstance().schedule(new Runnable() {
                     // $FF: synthetic field
                     final <undefinedtype> this$1;

                     {
                        this.this$1 = this$1;
                     }

                     public void run() {
                        if (this.this$1.val$c != null && this.this$1.val$c.getPlayer() != null && this.this$1.val$c.getPlayer().getMapId() == 993000100) {
                           this.this$1.val$c.getPlayer().warp(993000600);
                        }

                     }
                  }, 2000L);
               }
            }

         }
      }, 1000L);
   }

   public void insertWaveData() {
      ArrayList<Integer> waves = new ArrayList();
      waves.add(9831000);
      waves.add(9831000);
      waves.add(9831000);
      waves.add(9831000);
      waves.add(9831000);
      waves.add(9831002);
      waves.add(9831002);
      waves.add(9831001);
      waves.add(9831001);
      waves.add(9831001);
      waves.add(9831001);
      waves.add(9831001);
      this.waveData.add(waves);
      ArrayList<Integer> waves2 = new ArrayList();
      waves2.add(9831006);
      waves2.add(9831006);
      waves2.add(9831006);
      waves2.add(9831006);
      waves2.add(9831006);
      waves2.add(9831008);
      waves2.add(9831008);
      waves2.add(9831007);
      waves2.add(9831007);
      waves2.add(9831007);
      waves2.add(9831007);
      waves2.add(9831007);
      this.waveData.add(waves2);
      ArrayList<Integer> waves3 = new ArrayList();
      waves3.add(9831012);
      waves3.add(9831012);
      waves3.add(9831012);
      waves3.add(9831012);
      waves3.add(9831014);
      this.waveData.add(waves3);
   }

   public void attacked(MapleClient c) {
      --this.life;
      this.updateDefenseLife(c);
      if (this.life <= 0) {
         if (this.sc != null) {
            this.sc.cancel(false);
         }

         if (this.sch != null) {
            this.sch.cancel(false);
         }

         c.getSession().writeAndFlush(CField.environmentChange("killing/fail", 16));
         if (c != null && c.getPlayer() != null && c.getPlayer().getMapId() == 993000100) {
            c.getPlayer().warp(993000600);
         }
      }

   }

   public void start(final MapleClient c) {
      this.updateDefenseWave(c);
      this.updateDefenseLife(c);
      this.insertWaveData();
      c.getSession().writeAndFlush(CField.startMapEffect("놈들이 겁도 없이 마을을 습격하는군! 모조리 해치워라!", 5120159, true));
      c.getSession().writeAndFlush(CField.getClock(300));
      this.updateNewWave(c);
      this.sc = Timer.EventTimer.getInstance().schedule(new Runnable() {
         public void run() {
            if (DefenseTowerWave.this.sc != null) {
               DefenseTowerWave.this.sc.cancel(true);
            }

            if (DefenseTowerWave.this.sch != null) {
               DefenseTowerWave.this.sch.cancel(true);
            }

            if (c.getPlayer().getMapId() == 993000100) {
               c.getPlayer().warp(993000600);
            }

         }
      }, 300000L);
   }

   public ScheduledFuture<?> getSch() {
      return this.sch;
   }

   public void setSch(ScheduledFuture<?> sch) {
      this.sch = sch;
   }

   public List<List<Integer>> getWaveData() {
      return this.waveData;
   }

   public void setWaveData(List<List<Integer>> waveData) {
      this.waveData = waveData;
   }

   public ScheduledFuture<?> getSc() {
      return this.sc;
   }

   public void setSc(ScheduledFuture<?> sc) {
      this.sc = sc;
   }
}
