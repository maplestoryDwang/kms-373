package server.events;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import handling.world.World;
import server.MapleInventoryManipulator;
import server.RandomRewards;
import server.Randomizer;
import server.Timer;
import server.maps.FieldLimitType;
import server.maps.MapleMap;
import server.maps.SavedLocationType;
import tools.FileoutputUtil;
import tools.StringUtil;
import tools.packet.CField;
import tools.packet.CWvsContext;

public abstract class MapleEvent {
   protected MapleEventType type;
   protected int channel;
   protected int playerCount = 0;
   protected boolean isRunning = false;

   public MapleEvent(int channel, MapleEventType type) {
      this.channel = channel;
      this.type = type;
   }

   public void incrementPlayerCount() {
      ++this.playerCount;
      if (this.playerCount == 250) {
         setEvent(ChannelServer.getInstance(this.channel), true);
      }

   }

   public MapleEventType getType() {
      return this.type;
   }

   public boolean isRunning() {
      return this.isRunning;
   }

   public MapleMap getMap(int i) {
      return this.getChannelServer().getMapFactory().getMap(this.type.mapids[i]);
   }

   public ChannelServer getChannelServer() {
      return ChannelServer.getInstance(this.channel);
   }

   public void broadcast(byte[] packet) {
      for(int i = 0; i < this.type.mapids.length; ++i) {
         this.getMap(i).broadcastMessage(packet);
      }

   }

   public static void givePrize(MapleCharacter chr) {
      int reward = RandomRewards.getEventReward();
      int cs;
      if (reward == 0) {
         cs = Randomizer.nextInt(9000000) + 1000000;
         chr.gainMeso((long)cs, true, false);
      } else if (reward == 1) {
         cs = Randomizer.nextInt(4000) + 1000;
         chr.modifyCSPoints(1, cs, true);
      } else if (reward == 2) {
         chr.setVPoints(chr.getVPoints() + 1);
      } else if (reward != 3 && reward != 4) {
         int max_quantity = 1;
         switch(reward) {
         case 2022121:
            max_quantity = 10;
            break;
         case 4031307:
         case 5050000:
            max_quantity = 5;
            break;
         case 5062000:
            max_quantity = 3;
            break;
         case 5220000:
            max_quantity = 25;
         }

         int quantity = (max_quantity > 1 ? Randomizer.nextInt(max_quantity) : 0) + 1;
         if (MapleInventoryManipulator.checkSpace(chr.getClient(), reward, quantity, "")) {
            MapleInventoryManipulator.addById(chr.getClient(), reward, (short)quantity, "Event prize on " + FileoutputUtil.CurrentReadable_Date());
         } else {
            givePrize(chr);
         }
      }

   }

   public abstract void finished(MapleCharacter var1);

   public abstract void startEvent();

   public void onMapLoad(MapleCharacter chr) {
      if (FieldLimitType.Event.check(chr.getMap().getFieldLimit()) && FieldLimitType.Event2.check(chr.getMap().getFieldLimit())) {
      }

   }

   public void warpBack(MapleCharacter chr) {
      int map = chr.getSavedLocation(SavedLocationType.EVENT);
      if (map <= -1) {
         map = 104000000;
      }

      MapleMap mapp = chr.getClient().getChannelServer().getMapFactory().getMap(map);
      chr.changeMap(mapp, mapp.getPortal(0));
   }

   public void reset() {
      this.isRunning = true;
      this.playerCount = 0;
   }

   public void unreset() {
      this.isRunning = false;
      this.playerCount = 0;
   }

   public static final void setEvent(ChannelServer cserv, boolean auto) {
      if (auto && cserv.getEvent() > -1) {
         MapleEventType[] var2 = MapleEventType.values();
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            MapleEventType t = var2[var4];
            final MapleEvent e = cserv.getEvent(t);
            if (e.isRunning) {
               int[] var7 = e.type.mapids;
               int var8 = var7.length;

               for(int var9 = 0; var9 < var8; ++var9) {
                  int i = var7[var9];
                  if (cserv.getEvent() == i) {
                     World.Broadcast.broadcastMessage(CWvsContext.serverNotice(0, "", "Entries for the event are now closed!"));
                     e.broadcast(CWvsContext.serverNotice(0, "", "The event will start in 30 seconds!"));
                     e.broadcast(CField.getClock(30));
                     Timer.EventTimer.getInstance().schedule(new Runnable() {
                        public void run() {
                           e.startEvent();
                        }
                     }, 30000L);
                     break;
                  }
               }
            }
         }
      }

      cserv.setEvent(-1);
   }

   public static final void mapLoad(MapleCharacter chr, int channel) {
      if (chr != null) {
         MapleEventType[] var2 = MapleEventType.values();
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            MapleEventType t = var2[var4];
            MapleEvent e = ChannelServer.getInstance(channel).getEvent(t);
            if (e != null && e.isRunning) {
               if (chr.getMapId() == 109050000) {
                  e.finished(chr);
               }

               for(int i = 0; i < e.type.mapids.length; ++i) {
                  if (chr.getMapId() == e.type.mapids[i]) {
                     e.onMapLoad(chr);
                     if (i == 0) {
                        e.incrementPlayerCount();
                     }
                  }
               }
            }
         }

      }
   }

   public static final void onStartEvent(MapleCharacter chr) {
      MapleEventType[] var1 = MapleEventType.values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         MapleEventType t = var1[var3];
         MapleEvent e = chr.getClient().getChannelServer().getEvent(t);
         if (e.isRunning) {
            int[] var6 = e.type.mapids;
            int var7 = var6.length;

            for(int var8 = 0; var8 < var7; ++var8) {
               int i = var6[var8];
               if (chr.getMapId() == i) {
                  e.startEvent();
                  setEvent(chr.getClient().getChannelServer(), false);
                  chr.dropMessage(5, String.valueOf(t) + " ?대깽?멸? ?쒖옉?섏뿀?듬땲??");
               }
            }
         }
      }

   }

   public static final String scheduleEvent(MapleEventType event, ChannelServer cserv) {
      if (cserv.getEvent() == -1 && cserv.getEvent(event) != null) {
         int[] var2 = cserv.getEvent(event).type.mapids;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            int i = var2[var4];
            if (cserv.getMapFactory().getMap(i).getCharactersSize() > 0) {
               return "The event is already running.";
            }
         }

         cserv.setEvent(cserv.getEvent(event).type.mapids[0]);
         cserv.getEvent(event).reset();
         String var10002 = cserv.getServerName();
         World.Broadcast.broadcastMessage(CWvsContext.serverNotice(0, "", "Hello " + var10002 + "! Let's play a " + StringUtil.makeEnumHumanReadable(event.name()) + " event in channel " + cserv.getChannel() + "! Change to channel " + cserv.getChannel() + " and use @event command!"));
         return "";
      } else {
         return "The event must not have been already scheduled.";
      }
   }
}
