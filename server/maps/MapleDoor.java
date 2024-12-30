package server.maps;

import client.MapleCharacter;
import client.MapleClient;
import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import server.MaplePortal;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class MapleDoor extends MapleMapObject {
   private WeakReference<MapleCharacter> owner;
   private MapleMap town;
   private MaplePortal townPortal;
   private MapleMap target;
   private int skillId;
   private int ownerId;
   private Point targetPosition;

   public MapleDoor(MapleCharacter owner, Point targetPosition, int skillId) {
      this.owner = new WeakReference(owner);
      this.ownerId = owner.getId();
      this.target = owner.getMap();
      this.targetPosition = targetPosition;
      this.setPosition(this.targetPosition);
      this.town = this.target.getReturnMap();
      this.townPortal = this.getFreePortal();
      this.skillId = skillId;
   }

   public MapleDoor(MapleDoor origDoor) {
      this.owner = new WeakReference((MapleCharacter)origDoor.owner.get());
      this.town = origDoor.town;
      this.townPortal = origDoor.townPortal;
      this.target = origDoor.target;
      this.targetPosition = new Point(origDoor.targetPosition);
      this.skillId = origDoor.skillId;
      this.ownerId = origDoor.ownerId;
      this.setPosition(this.townPortal.getPosition());
   }

   public final int getSkill() {
      return this.skillId;
   }

   public final int getOwnerId() {
      return this.ownerId;
   }

   private final MaplePortal getFreePortal() {
      List<MaplePortal> freePortals = new ArrayList();
      Iterator var2 = this.town.getPortals().iterator();

      while(var2.hasNext()) {
         MaplePortal port = (MaplePortal)var2.next();
         if (port.getType() == 6) {
            freePortals.add(port);
         }
      }

      Collections.sort(freePortals, new Comparator<MaplePortal>() {
         public final int compare(MaplePortal o1, MaplePortal o2) {
            if (o1.getId() < o2.getId()) {
               return -1;
            } else {
               return o1.getId() == o2.getId() ? 0 : 1;
            }
         }
      });
      var2 = this.town.getAllDoorsThreadsafe().iterator();

      while(var2.hasNext()) {
         MapleMapObject obj = (MapleMapObject)var2.next();
         MapleDoor door = (MapleDoor)obj;
         if (door.getOwner() != null && door.getOwner().getParty() != null && this.getOwner() != null && this.getOwner().getParty() != null && this.getOwner().getParty().getId() == door.getOwner().getParty().getId()) {
            return null;
         }

         freePortals.remove(door.getTownPortal());
      }

      if (freePortals.size() <= 0) {
         return null;
      } else {
         return (MaplePortal)freePortals.iterator().next();
      }
   }

   public final void sendSpawnData(MapleClient client) {
      if (this.getOwner() != null && this.target != null && client.getPlayer() != null) {
         if (this.target.getId() == client.getPlayer().getMapId() || this.getOwnerId() == client.getPlayer().getId() || this.getOwner() != null && this.getOwner().getParty() != null && client.getPlayer().getParty() != null && this.getOwner().getParty().getId() == client.getPlayer().getParty().getId()) {
            client.getSession().writeAndFlush(CField.spawnDoor(this.getOwnerId(), this.target.getId() == client.getPlayer().getMapId() ? this.targetPosition : this.townPortal.getPosition(), true));
            if (this.getOwner() != null && this.getOwner().getParty() != null && client.getPlayer().getParty() != null && (this.getOwnerId() == client.getPlayer().getId() || this.getOwner().getParty().getId() == client.getPlayer().getParty().getId())) {
               client.getSession().writeAndFlush(CWvsContext.PartyPacket.partyPortal(this.town.getId(), this.target.getId(), this.skillId, this.target.getId() == client.getPlayer().getMapId() ? this.targetPosition : this.townPortal.getPosition(), true));
            }

            client.getSession().writeAndFlush(CWvsContext.spawnPortal(this.town.getId(), this.target.getId(), this.skillId, this.target.getId() == client.getPlayer().getMapId() ? this.targetPosition : this.townPortal.getPosition()));
         }

      }
   }

   public final void sendDestroyData(MapleClient client) {
      if (client.getPlayer() != null && this.getOwner() != null && this.target != null) {
         if (this.target.getId() == client.getPlayer().getMapId() || this.getOwnerId() == client.getPlayer().getId() || this.getOwner() != null && this.getOwner().getParty() != null && client.getPlayer().getParty() != null && this.getOwner().getParty().getId() == client.getPlayer().getParty().getId()) {
            client.getSession().writeAndFlush(CField.removeDoor(this.getOwnerId(), false));
            if (this.getOwner() != null && this.getOwner().getParty() != null && client.getPlayer().getParty() != null && (this.getOwnerId() == client.getPlayer().getId() || this.getOwner().getParty().getId() == client.getPlayer().getParty().getId())) {
               client.getSession().writeAndFlush(CWvsContext.PartyPacket.partyPortal(999999999, 999999999, 0, new Point(-1, -1), false));
            }

            client.getSession().writeAndFlush(CWvsContext.spawnPortal(999999999, 999999999, 0, (Point)null));
         }

      }
   }

   public final void warp(MapleCharacter chr, boolean toTown) {
      if (chr.getId() == this.getOwnerId() || this.getOwner() != null && this.getOwner().getParty() != null && chr.getParty() != null && this.getOwner().getParty().getId() == chr.getParty().getId()) {
         if (!toTown) {
            chr.changeMap(this.target, this.target.findClosestPortal(this.targetPosition));
         } else {
            chr.changeMap(this.town, this.townPortal);
         }
      } else {
         chr.getClient().getSession().writeAndFlush(CWvsContext.enableActions(chr));
      }

   }

   public final MapleCharacter getOwner() {
      return (MapleCharacter)this.owner.get();
   }

   public final MapleMap getTown() {
      return this.town;
   }

   public final MaplePortal getTownPortal() {
      return this.townPortal;
   }

   public final MapleMap getTarget() {
      return this.target;
   }

   public final Point getTargetPosition() {
      return this.targetPosition;
   }

   public final MapleMapObjectType getType() {
      return MapleMapObjectType.DOOR;
   }
}
