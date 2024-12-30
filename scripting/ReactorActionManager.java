package scripting;

import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.channel.ChannelServer;
import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.life.MapleLifeFactory;
import server.maps.MapleReactor;
import server.maps.ReactorDropEntry;
import tools.packet.CField;

public class ReactorActionManager extends AbstractPlayerInteraction {
   private MapleReactor reactor;

   public ReactorActionManager(MapleClient c, MapleReactor reactor) {
      super(c, reactor.getReactorId(), c.getPlayer().getMapId());
      this.reactor = reactor;
   }

   public void dropItems(boolean suc) {
      this.dropItems(false, 0, 0, 0, 0, suc);
   }

   public void dropItems() {
      this.dropItems(false, 0, 0, 0, 0, false);
   }

   public void dropItems(boolean meso, int mesoChance, int minMeso, int maxMeso, boolean suc) {
      this.dropItems(meso, mesoChance, minMeso, maxMeso, 0, suc);
   }

   public void dropItems(boolean meso, int mesoChance, int minMeso, int maxMeso, int minItems, boolean suc) {
      List<ReactorDropEntry> chances = ReactorScriptManager.getInstance().getDrops(this.reactor.getReactorId());
      List<ReactorDropEntry> items = new LinkedList();
      int numItems = 0;
      Iterator iter = chances.iterator();

      while(iter.hasNext()) {
         ReactorDropEntry d = (ReactorDropEntry)iter.next();
         int suc2 = suc ? d.chance : d.chance - 15;
         if (Randomizer.isSuccess(suc2)) {
            ++numItems;
            items.add(d);
         }
      }

      while(items.size() < minItems) {
         items.add(new ReactorDropEntry(0, mesoChance, -1, 1, 1));
         ++numItems;
      }

      Point dropPos = this.reactor.getPosition();
      dropPos.x -= 12 * numItems;
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

      for(Iterator var13 = items.iterator(); var13.hasNext(); dropPos.x += 25) {
         ReactorDropEntry d = (ReactorDropEntry)var13.next();
         if (d.itemId == 0) {
            int range = maxMeso - minMeso;
            int mesoDrop = Randomizer.nextInt(range) + minMeso * ChannelServer.getInstance(this.getClient().getChannel()).getMesoRate();
            this.reactor.getMap().spawnMesoDrop(mesoDrop, dropPos, this.reactor, this.getPlayer(), false, (byte)0);
         } else {
            Item drop;
            if (GameConstants.getInventoryType(d.itemId) != MapleInventoryType.EQUIP) {
               drop = new Item(d.itemId, (short)0, (short)(d.Maximum != 1 ? Randomizer.rand(d.Minimum, d.Maximum) : 1), 0);
            } else {
               drop = ii.getEquipById(d.itemId);
            }

            int var10001 = this.reactor.getReactorId();
            drop.setGMLog("Dropped from reactor " + var10001 + " on map " + this.getPlayer().getMapId());

            try {
               Robot robot = new Robot();
               robot.delay(150);
               this.reactor.getMap().spawnItemDrop(this.reactor, this.getPlayer(), drop, dropPos, false, false);
            } catch (AWTException var17) {
            }
         }
      }

   }

   public void dropSingleItem(int itemId) {
      Item drop;
      if (GameConstants.getInventoryType(itemId) != MapleInventoryType.EQUIP) {
         drop = new Item(itemId, (short)0, (short)1, 0);
      } else {
         drop = MapleItemInformationProvider.getInstance().getEquipById(itemId);
      }

      int var10001 = this.reactor.getReactorId();
      drop.setGMLog("Dropped from reactor " + var10001 + " on map " + this.getPlayer().getMapId());
      this.reactor.getMap().spawnItemDrop(this.reactor, this.getPlayer(), drop, this.reactor.getPosition(), false, false);
   }

   public void spawnNpc(int npcId) {
      this.spawnNpc(npcId, this.getPosition());
   }

   public Point getPosition() {
      Point pos = this.reactor.getPosition();
      pos.y -= 10;
      return pos;
   }

   public MapleReactor getReactor() {
      return this.reactor;
   }

   public void spawnFakeMonster(int id) {
      this.spawnFakeMonster(id, 1, this.getPosition());
   }

   public void spawnFakeMonster(int id, int x, int y) {
      this.spawnFakeMonster(id, 1, new Point(x, y));
   }

   public void spawnFakeMonster(int id, int qty) {
      this.spawnFakeMonster(id, qty, this.getPosition());
   }

   public void spawnFakeMonster(int id, int qty, int x, int y) {
      this.spawnFakeMonster(id, qty, new Point(x, y));
   }

   private void spawnFakeMonster(int id, int qty, Point pos) {
      for(int i = 0; i < qty; ++i) {
         this.reactor.getMap().spawnFakeMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), pos);
      }

   }

   public void killAll() {
      this.reactor.getMap().killAllMonsters(true);
   }

   public void killMonster(int monsId) {
      this.reactor.getMap().killMonster(monsId);
   }

   public void spawnMonster(int id) {
      this.spawnMonster(id, 1, this.getPosition());
   }

   public void spawnMonster(int id, int qty) {
      this.spawnMonster(id, qty, this.getPosition());
   }

   public void cancelHarvest(boolean succ) {
      this.getPlayer().setFatigue((byte)(this.getPlayer().getFatigue() + 1));
      this.getPlayer().getMap().broadcastMessage(this.getPlayer(), CField.showHarvesting(this.getPlayer().getId(), 0), false);
      this.getPlayer().getMap().broadcastMessage(CField.harvestResult(this.getPlayer().getId(), succ));
   }

   public void processGather() {
      this.doHarvest();
   }

   public void doHarvest() {
      int pID = this.getReactor().getReactorId() < 200000 ? 92000000 : 92010000;
      String pName = this.getReactor().getReactorId() < 200000 ? "채집" : "채광";
      if (this.getPlayer().getFatigue() < 200 && this.getPlayer().getStat().harvestingTool > 0 && !(this.getReactor().getTruePosition().distanceSq(this.getPlayer().getTruePosition()) > 10000.0D)) {
         int he = this.getPlayer().getProfessionLevel(pID);
         if (he > 0) {
            Item item = this.getInventory(1).getItem((short)this.getPlayer().getStat().harvestingTool);
            if (item != null && (item.getItemId() / 10000 | 0) == (this.getReactor().getReactorId() < 200000 ? 150 : 151)) {
               int hm = this.getReactor().getReactorId() % 100;
               int successChance = 90 + (he - hm) * 10;
               if (this.getReactor().getReactorId() % 100 == 10) {
                  hm = 1;
                  successChance = 100;
               } else if (this.getReactor().getReactorId() % 100 == 11) {
                  hm = 10;
                  successChance -= 40;
               }

               this.getPlayer().getStat().checkEquipDurabilitys(this.getPlayer(), -1, true);
               int masteryIncrease = (hm - he) * 2 + 20;
               boolean succ = this.randInt(100) < successChance;
               if (!succ) {
                  masteryIncrease /= 10;
               } else if (this.getReactor().getReactorId() < 200000) {
                  this.addTrait("sense", 5);
                  if (Randomizer.nextInt(10) == 0) {
                     this.dropSingleItem(2440000);
                  }

                  if (Randomizer.nextInt(100) == 0) {
                     this.dropSingleItem(4032933);
                  }
               } else {
                  this.addTrait("insight", 5);
                  if (Randomizer.nextInt(10) == 0) {
                     this.dropSingleItem(2440001);
                  }
               }

               this.cancelHarvest(succ);
               this.playerMessage(-5, pName + "의 숙련도가 증가하였습니다. (+" + masteryIncrease + ")");
               if (this.getPlayer().addProfessionExp(pID, masteryIncrease)) {
                  this.playerMessage(-5, pName + "의 레벨이 증가 하였습니다.");
               }

               this.dropItems(succ);
            }
         }
      } else {
         this.c.getPlayer().dropMessage(5, "피로도가 부족하여" + pName + "을 할 수 없습니다.");
      }
   }
}
