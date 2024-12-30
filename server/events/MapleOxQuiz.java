package server.events;

import client.MapleCharacter;
import client.MapleStat;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import server.Timer;
import server.maps.MapleMap;
import tools.Pair;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class MapleOxQuiz extends MapleEvent {
   private ScheduledFuture<?> oxSchedule;
   private ScheduledFuture<?> oxSchedule2;
   private int timesAsked = 0;
   private boolean finished = false;

   public MapleOxQuiz(int channel, MapleEventType type) {
      super(channel, type);
   }

   public void finished(MapleCharacter chr) {
   }

   private void resetSchedule() {
      if (this.oxSchedule != null) {
         this.oxSchedule.cancel(false);
         this.oxSchedule = null;
      }

      if (this.oxSchedule2 != null) {
         this.oxSchedule2.cancel(false);
         this.oxSchedule2 = null;
      }

   }

   public void onMapLoad(MapleCharacter chr) {
      super.onMapLoad(chr);
      if (chr.getMapId() == this.type.mapids[0] && !chr.isGM()) {
         chr.canTalk(false);
      }

   }

   public void reset() {
      super.reset();
      this.getMap(0).getPortal("join00").setPortalState(false);
      this.resetSchedule();
      this.timesAsked = 0;
   }

   public void unreset() {
      super.unreset();
      this.getMap(0).getPortal("join00").setPortalState(true);
      this.resetSchedule();
   }

   public void startEvent() {
      this.sendQuestion();
      this.finished = false;
   }

   public void sendQuestion() {
      this.sendQuestion(this.getMap(0));
   }

   public void sendQuestion(final MapleMap toSend) {
      final Entry<Pair<Integer, Integer>, MapleOxQuizFactory.MapleOxQuizEntry> question = MapleOxQuizFactory.getInstance().grabRandomQuestion();
      if (this.oxSchedule2 != null) {
         this.oxSchedule2.cancel(false);
      }

      this.oxSchedule2 = Timer.EventTimer.getInstance().schedule(new Runnable() {
         public void run() {
            int number = 0;
            Iterator var2 = toSend.getCharactersThreadsafe().iterator();

            while(true) {
               MapleCharacter chr;
               do {
                  if (!var2.hasNext()) {
                     if (toSend.getCharactersSize() - number > 1 && MapleOxQuiz.this.timesAsked != 10) {
                        toSend.broadcastMessage(CField.getClock(10));
                        return;
                     }

                     toSend.broadcastMessage(CWvsContext.serverNotice(6, "", "The event has ended"));
                     MapleOxQuiz.this.unreset();
                     var2 = toSend.getCharactersThreadsafe().iterator();

                     while(var2.hasNext()) {
                        chr = (MapleCharacter)var2.next();
                        if (chr != null && !chr.isGM() && chr.isAlive()) {
                           chr.canTalk(true);
                           MapleEvent.givePrize(chr);
                           MapleOxQuiz.this.warpBack(chr);
                        }
                     }

                     MapleOxQuiz.this.finished = true;
                     return;
                  }

                  chr = (MapleCharacter)var2.next();
               } while(!chr.isGM() && chr.isAlive());

               ++number;
            }
         }
      }, 10000L);
      if (this.oxSchedule != null) {
         this.oxSchedule.cancel(false);
      }

      this.oxSchedule = Timer.EventTimer.getInstance().schedule(new Runnable() {
         public void run() {
            if (!MapleOxQuiz.this.finished) {
               ++MapleOxQuiz.this.timesAsked;
               Iterator var1 = toSend.getCharactersThreadsafe().iterator();

               while(var1.hasNext()) {
                  MapleCharacter chr = (MapleCharacter)var1.next();
                  if (chr != null && !chr.isGM() && chr.isAlive()) {
                     if (!MapleOxQuiz.this.isCorrectAnswer(chr, ((MapleOxQuizFactory.MapleOxQuizEntry)question.getValue()).getAnswer())) {
                        chr.getStat().setHp(0L, chr);
                        chr.updateSingleStat(MapleStat.HP, 0L);
                     } else {
                        chr.gainExp(3000L, true, true, false);
                     }
                  }
               }

               MapleOxQuiz.this.sendQuestion();
            }
         }
      }, 20000L);
   }

   private boolean isCorrectAnswer(MapleCharacter chr, int answer) {
      double x = chr.getTruePosition().getX();
      double y = chr.getTruePosition().getY();
      if ((!(x > -234.0D) || !(y > -26.0D) || answer != 0) && (!(x < -234.0D) || !(y > -26.0D) || answer != 1)) {
         chr.dropMessage(6, "[Ox Quiz] Incorrect!");
         return false;
      } else {
         chr.dropMessage(6, "[Ox Quiz] Correct!");
         return true;
      }
   }
}
