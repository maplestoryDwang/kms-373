package server.events;

import client.MapleCharacter;
import java.util.Iterator;
import java.util.concurrent.ScheduledFuture;
import server.maps.MapleMap;

public class MapleSnowball extends MapleEvent {
   private MapleSnowball.MapleSnowballs[] balls = new MapleSnowball.MapleSnowballs[2];

   public MapleSnowball(int channel, MapleEventType type) {
      super(channel, type);
   }

   public void finished(MapleCharacter chr) {
   }

   public void unreset() {
      super.unreset();

      for(int i = 0; i < 2; ++i) {
         this.getSnowBall(i).resetSchedule();
         this.resetSnowBall(i);
      }

   }

   public void reset() {
      super.reset();
      this.makeSnowBall(0);
      this.makeSnowBall(1);
   }

   public void startEvent() {
      for(int i = 0; i < 2; ++i) {
         MapleSnowball.MapleSnowballs ball = this.getSnowBall(i);
         ball.broadcast(this.getMap(0), 0);
         ball.setInvis(false);
         ball.broadcast(this.getMap(0), 5);
      }

   }

   public void resetSnowBall(int teamz) {
      this.balls[teamz] = null;
   }

   public void makeSnowBall(int teamz) {
      this.resetSnowBall(teamz);
      this.balls[teamz] = new MapleSnowball.MapleSnowballs(teamz);
   }

   public MapleSnowball.MapleSnowballs getSnowBall(int teamz) {
      return this.balls[teamz];
   }

   public static class MapleSnowballs {
      private int position = 0;
      private final int team;
      private int startPoint = 0;
      private boolean invis = true;
      private boolean hittable = true;
      private int snowmanhp = 7500;
      private ScheduledFuture<?> snowmanSchedule = null;

      public MapleSnowballs(int team_) {
         this.team = team_;
      }

      public void resetSchedule() {
         if (this.snowmanSchedule != null) {
            this.snowmanSchedule.cancel(false);
            this.snowmanSchedule = null;
         }

      }

      public int getTeam() {
         return this.team;
      }

      public int getPosition() {
         return this.position;
      }

      public void setPositionX(int pos) {
         this.position = pos;
      }

      public void setStartPoint(MapleMap map) {
         ++this.startPoint;
         this.broadcast(map, this.startPoint);
      }

      public boolean isInvis() {
         return this.invis;
      }

      public void setInvis(boolean i) {
         this.invis = i;
      }

      public boolean isHittable() {
         return this.hittable && !this.invis;
      }

      public void setHittable(boolean b) {
         this.hittable = b;
      }

      public int getSnowmanHP() {
         return this.snowmanhp;
      }

      public void setSnowmanHP(int shp) {
         this.snowmanhp = shp;
      }

      public void broadcast(MapleMap map, int message) {
         MapleCharacter var4;
         for(Iterator var3 = map.getCharactersThreadsafe().iterator(); var3.hasNext(); var4 = (MapleCharacter)var3.next()) {
         }

      }

      public int getLeftX() {
         return this.position * 3 + 175;
      }

      public int getRightX() {
         return this.getLeftX() + 275;
      }

      public static final void hitSnowball(MapleCharacter chr) {
         int team = chr.getTruePosition().y > -80 ? 0 : 1;
         MapleSnowball sb = (MapleSnowball)chr.getClient().getChannelServer().getEvent(MapleEventType.Snowball);
         sb.getSnowBall(team);
      }
   }
}
