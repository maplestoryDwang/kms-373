package server;

import client.MapleClient;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;

public class AdelProjectile extends MapleMapObject {
   private int projectileType;
   private int ownerId;
   private int targetId;
   private int skillId;
   private int duration;
   private int startX;
   private int startY;
   private int delay;
   private int idk2;
   private int createDelay;
   private Point point;
   private List<Integer> points = new ArrayList();

   public AdelProjectile(int projectileType, int ownerId, int targetId, int skillId, int duration, int startX, int startY, Point point, List<Integer> points) {
      this.projectileType = projectileType;
      this.ownerId = ownerId;
      this.targetId = targetId;
      this.skillId = skillId;
      this.duration = duration;
      this.startX = startX;
      this.startY = startY;
      this.point = point;
      this.points = points;
   }

   public MapleMapObjectType getType() {
      return MapleMapObjectType.ADEL_PROJECTILE;
   }

   public void sendSpawnData(MapleClient client) {
   }

   public void sendDestroyData(MapleClient client) {
   }

   public int getOwnerId() {
      return this.ownerId;
   }

   public void setOwnerId(int ownerId) {
      this.ownerId = ownerId;
   }

   public int getTargetId() {
      return this.targetId;
   }

   public void setTargetId(int targetId) {
      this.targetId = targetId;
   }

   public int getSkillId() {
      return this.skillId;
   }

   public void setSkillId(int skillId) {
      this.skillId = skillId;
   }

   public int getDuration() {
      return this.duration;
   }

   public void setDuration(int duration) {
      this.duration = duration;
   }

   public int getProjectileType() {
      return this.projectileType;
   }

   public void setProjectileType(int projectileType) {
      this.projectileType = projectileType;
   }

   public List<Integer> getPoints() {
      return this.points;
   }

   public void setPoints(List<Integer> points) {
      this.points = points;
   }

   public Point getPoint() {
      return this.point;
   }

   public void setPoint(Point point) {
      this.point = point;
   }

   public int getStartX() {
      return this.startX;
   }

   public void setStartX(int startX) {
      this.startX = startX;
   }

   public int getStartY() {
      return this.startY;
   }

   public void setStartY(int startY) {
      this.startY = startY;
   }

   public int getDelay() {
      return this.delay;
   }

   public void setDelay(int delay) {
      this.delay = delay;
   }

   public int getIdk2() {
      return this.idk2;
   }

   public void setIdk2(int idk2) {
      this.idk2 = idk2;
   }

   public int getCreateDelay() {
      return this.createDelay;
   }

   public void setCreateDelay(int createDelay) {
      this.createDelay = createDelay;
   }
}
