package server.polofritto;

import client.MapleClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import server.Randomizer;
import server.Timer;
import tools.packet.CField;
import tools.packet.SLFCGPacket;

public class FrittoDancing {
   private int state;
   private ScheduledFuture<?> sc;
   private List<List<Integer>> waveData = new ArrayList();

   public FrittoDancing(int state) {
      this.state = state;
   }

   public void updateDefenseWave(MapleClient c) {
      c.getSession().writeAndFlush(SLFCGPacket.courtShipDanceState(this.state));
   }

   public void updateNewWave(final MapleClient c) {
      c.getSession().writeAndFlush(CField.environmentChange("defense/count", 16));
      Timer.EventTimer.getInstance().schedule(new Runnable() {
         public void run() {
            if (c.getPlayer().getMapId() == 993000400) {
               c.getSession().writeAndFlush(CField.environmentChange("killing/first/start", 16));
               c.getSession().writeAndFlush(SLFCGPacket.courtShipDanceCommand(FrittoDancing.this.waveData));
            }

         }
      }, 3000L);
   }

   public void finish(final MapleClient c) {
      if (this.sc != null) {
         this.sc.cancel(false);
      }

      c.getSession().writeAndFlush(CField.environmentChange("killing/clear", 16));
      Timer.EventTimer.getInstance().schedule(new Runnable() {
         public void run() {
            if (c != null && c.getPlayer() != null) {
               c.getSession().writeAndFlush(SLFCGPacket.SetIngameDirectionMode(false, true, false, false));
               c.getSession().writeAndFlush(SLFCGPacket.InGameDirectionEvent("", 10, 0));
               c.getPlayer().warp(993000601);
            }

         }
      }, 2000L);
   }

   public void insertWaveData() {
      List<Integer> waves = new ArrayList();

      for(int i = 0; i < 4; ++i) {
         waves.add(Randomizer.nextInt(4));
      }

      this.waveData.add(waves);
      List<Integer> waves2 = new ArrayList();

      int a;
      for(a = 0; a < 6; ++a) {
         waves2.add(Randomizer.nextInt(4));
      }

      this.waveData.add(waves2);

      ArrayList waves5;
      int i;
      for(a = 0; a < 3; ++a) {
         waves5 = new ArrayList();

         for(i = 0; i < 7; ++i) {
            waves5.add(Randomizer.nextInt(4));
         }

         this.waveData.add(waves5);
      }

      for(a = 0; a < 3; ++a) {
         waves5 = new ArrayList();

         for(i = 0; i < 8; ++i) {
            waves5.add(Randomizer.nextInt(4));
         }

         this.waveData.add(waves5);
      }

      for(a = 0; a < 2; ++a) {
         waves5 = new ArrayList();

         for(i = 0; i < 10; ++i) {
            waves5.add(Randomizer.nextInt(4));
         }

         this.waveData.add(waves5);
      }

   }

   public void start(final MapleClient c) {
      this.updateDefenseWave(c);
      this.insertWaveData();
      c.getPlayer().setKeyValue(15143, "score", "0");
      c.getSession().writeAndFlush(SLFCGPacket.SetIngameDirectionMode(true, false, false, false));
      c.getSession().writeAndFlush(SLFCGPacket.InGameDirectionEvent("", 10, 1));
      c.getSession().writeAndFlush(CField.environmentChange("PoloFritto/msg3", 20));
      c.getSession().writeAndFlush(CField.startMapEffect("달걀을 훔치려면 먼저 닭들을 속여야 해! 자, 나를 따라 구애의 춤을 춰!", 5120160, true));
      c.getSession().writeAndFlush(CField.getClock(60));
      this.updateNewWave(c);
      this.sc = Timer.EventTimer.getInstance().schedule(new Runnable() {
         public void run() {
            if (c.getPlayer().getMapId() == 993000400) {
               c.getSession().writeAndFlush(SLFCGPacket.SetIngameDirectionMode(false, true, false, false));
               c.getSession().writeAndFlush(SLFCGPacket.InGameDirectionEvent("", 10, 0));
               c.getPlayer().warp(993000601);
            }

         }
      }, 60000L);
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
