package scripting;

import client.MapleCharacter;
import handling.channel.ChannelServer;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.concurrent.ScheduledFuture;
import javax.script.Invocable;
import javax.script.ScriptException;
import server.Randomizer;
import server.Timer;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.OverrideMonsterStats;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.maps.MapleMapObject;
import server.maps.MapleReactor;
import server.maps.MapleReactorFactory;
import tools.FileoutputUtil;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class EventManager {
   private static int[] eventChannel = new int[2];
   private Invocable iv;
   private int channel;
   private Map<String, EventInstanceManager> instances = new WeakHashMap();
   private Properties props = new Properties();
   private String name;

   public EventManager(ChannelServer cserv, Invocable iv, String name) {
      this.iv = iv;
      this.channel = cserv.getChannel();
      this.name = name;
   }

   public void cancel() {
      try {
         this.iv.invokeFunction("cancelSchedule", null);
      } catch (Exception var2) {
         System.out.println("Event name : " + this.name + ", method Name : cancelSchedule:\n" + var2);
         FileoutputUtil.log("Log_Script_Except.rtf", "Event name : " + this.name + ", method Name : cancelSchedule:\n" + var2);
      }

   }

   public ScheduledFuture<?> schedule(final String methodName, long delay) {
      return Timer.EventTimer.getInstance().schedule(new Runnable() {
         public void run() {
            try {
               EventManager.this.iv.invokeFunction(methodName, null);
            } catch (Exception var2) {
               System.out.println("Event name : " + EventManager.this.name + ", method Name : " + methodName + ":\n" + var2);
               FileoutputUtil.log("Log_Script_Except.rtf", "Event name : " + EventManager.this.name + ", method Name : " + methodName + ":\n" + var2);
            }

         }
      }, delay);
   }

   public ScheduledFuture<?> schedule(final String methodName, long delay, final EventInstanceManager eim) {
      return Timer.EventTimer.getInstance().schedule(new Runnable() {
         public void run() {
            try {
               EventManager.this.iv.invokeFunction(methodName, eim);
            } catch (Exception var2) {
               System.out.println("Event name : " + EventManager.this.name + ", method Name : " + methodName + ":\n" + var2);
               FileoutputUtil.log("Log_Script_Except.rtf", "Event name : " + EventManager.this.name + ", method Name : " + methodName + ":\n" + var2);
            }

         }
      }, delay);
   }

   public ScheduledFuture<?> scheduleAtTimestamp(final String methodName, long timestamp) {
      return Timer.EventTimer.getInstance().scheduleAtTimestamp(new Runnable() {
         public void run() {
            try {
               EventManager.this.iv.invokeFunction(methodName, null);
            } catch (ScriptException var2) {
               System.out.println("Event name : " + EventManager.this.name + ", method Name : " + methodName + ":\n" + var2);
            } catch (NoSuchMethodException var3) {
               System.out.println("Event name : " + EventManager.this.name + ", method Name : " + methodName + ":\n" + var3);
            }

         }
      }, timestamp);
   }

   public int getChannel() {
      return this.channel;
   }

   public ChannelServer getChannelServer() {
      return ChannelServer.getInstance(this.channel);
   }

   public EventInstanceManager getInstance(String name) {
      return (EventInstanceManager)this.instances.get(name);
   }

   public Collection<EventInstanceManager> getInstances() {
      return Collections.unmodifiableCollection(this.instances.values());
   }

   public EventInstanceManager newInstance(String name) {
      EventInstanceManager ret = new EventInstanceManager(this, name, this.channel);
      this.instances.put(name, ret);
      return ret;
   }

   public void disposeInstance(String name) {
      this.instances.remove(name);
      if (this.getProperty("state") != null && this.instances.size() == 0) {
         this.setProperty("state", "0");
      }

      if (this.getProperty("leader") != null && this.instances.size() == 0 && this.getProperty("leader").equals("false")) {
         this.setProperty("leader", "true");
      }

   }

   public Invocable getIv() {
      return this.iv;
   }

   public void setProperty(String key, String value) {
      this.props.setProperty(key, value);
   }

   public String getProperty(String key) {
      return this.props.getProperty(key);
   }

   public final Properties getProperties() {
      return this.props;
   }

   public String getName() {
      return this.name;
   }

   public void startInstance() {
      try {
         this.iv.invokeFunction("setup", null);
      } catch (Exception var2) {
         var2.printStackTrace();
         FileoutputUtil.log("Log_Script_Except.rtf", "Event name : " + this.name + ", method Name : setup:\n" + var2);
      }

   }

   public void startInstance_Solo(String mapid, MapleCharacter chr) {
      try {
         EventInstanceManager eim = (EventInstanceManager)this.iv.invokeFunction("setup", mapid);
         eim.registerPlayer(chr);
      } catch (Exception var4) {
         var4.printStackTrace();
         FileoutputUtil.log("Log_Script_Except.rtf", "Event name : " + this.name + ", method Name : setup:\n" + var4);
      }

   }

   public void startInstance_Party(String mapid, MapleCharacter chr) {
      try {
         EventInstanceManager eim = (EventInstanceManager)this.iv.invokeFunction("setup", mapid);
         eim.registerParty(chr.getParty(), chr.getMap());
      } catch (Exception var4) {
         var4.printStackTrace();
         FileoutputUtil.log("Log_Script_Except.rtf", "Event name : " + this.name + ", method Name : setup:\n" + var4);
      }

   }

   public void startInstance_Guild(String mapid, MapleCharacter chr) {
      try {
         EventInstanceManager eim = (EventInstanceManager)this.iv.invokeFunction("setup", mapid);
         eim.registerGuild(chr.getGuild(), chr.getMap());
      } catch (Exception var4) {
         var4.printStackTrace();
         FileoutputUtil.log("Log_Script_Except.rtf", "Event name : " + this.name + ", method Name : setup:\n" + var4);
      }

   }

   public void startInstance(MapleCharacter character, String leader) {
      try {
         EventInstanceManager eim = (EventInstanceManager)this.iv.invokeFunction("setup", null);
         eim.registerPlayer(character);
         eim.setProperty("leader", leader);
         eim.setProperty("guildid", String.valueOf(character.getGuildId()));
         this.setProperty("guildid", String.valueOf(character.getGuildId()));
      } catch (Exception var4) {
         System.out.println("Event name : " + this.name + ", method Name : setup-Guild:\n" + var4);
         FileoutputUtil.log("Log_Script_Except.rtf", "Event name : " + this.name + ", method Name : setup-Guild:\n" + var4);
      }

   }

   public void startInstance_CharID(MapleCharacter character) {
      try {
         EventInstanceManager eim = (EventInstanceManager)this.iv.invokeFunction("setup", character.getId());
         eim.registerPlayer(character);
      } catch (Exception var3) {
         System.out.println("Event name : " + this.name + ", method Name : setup-CharID:\n" + var3);
         FileoutputUtil.log("Log_Script_Except.rtf", "Event name : " + this.name + ", method Name : setup-CharID:\n" + var3);
      }

   }

   public void startInstance_CharMapID(MapleCharacter character) {
      try {
         EventInstanceManager eim = (EventInstanceManager)this.iv.invokeFunction("setup", character.getId(), character.getMapId());
         eim.registerPlayer(character);
      } catch (Exception var3) {
         System.out.println("Event name : " + this.name + ", method Name : setup-CharID:\n" + var3);
         FileoutputUtil.log("Log_Script_Except.rtf", "Event name : " + this.name + ", method Name : setup-CharID:\n" + var3);
      }

   }

   public void startInstance(MapleCharacter character) {
      try {
         EventInstanceManager eim = (EventInstanceManager)this.iv.invokeFunction("setup", null);
         eim.registerPlayer(character);
      } catch (Exception var3) {
         System.out.println("Event name : " + this.name + ", method Name : setup-character:\n" + var3);
         FileoutputUtil.log("Log_Script_Except.rtf", "Event name : " + this.name + ", method Name : setup-character:\n" + var3);
      }

   }

   public void startInstance(MapleParty party, MapleMap map) {
      this.startInstance(party, map, 255);
   }

   public void startInstance(MapleParty party, MapleMap map, int maxLevel) {
      try {
         int averageLevel = 0;
         int size = 0;
         Iterator var6 = party.getMembers().iterator();

         while(var6.hasNext()) {
            MaplePartyCharacter mpc = (MaplePartyCharacter)var6.next();
            if (mpc.isOnline() && mpc.getMapid() == map.getId() && mpc.getChannel() == map.getChannel()) {
               averageLevel += mpc.getLevel();
               ++size;
            }
         }

         if (size <= 0) {
            return;
         }

         EventInstanceManager eim = (EventInstanceManager)this.iv.invokeFunction("setup", Math.min(maxLevel, averageLevel / size), party.getId());
         eim.registerParty(party, map);
      } catch (ScriptException var8) {
         System.out.println("Event name : " + this.name + ", method Name : setup-partyid:\n" + var8);
         FileoutputUtil.log("Log_Script_Except.rtf", "Event name : " + this.name + ", method Name : setup-partyid:\n" + var8);
      } catch (Exception var9) {
         this.startInstance_NoID(party, map, var9);
      }

   }

   public void startInstance_NoID(MapleParty party, MapleMap map) {
      this.startInstance_NoID(party, map, (Exception)null);
   }

   public void startInstance_NoID(MapleParty party, MapleMap map, Exception old) {
      try {
         EventInstanceManager eim = (EventInstanceManager)this.iv.invokeFunction("setup", null);
         eim.registerParty(party, map);
      } catch (Exception var5) {
         String var10001 = this.name;
         System.out.println("Event name : " + var10001 + ", method Name : setup-party:\n" + var5);
         FileoutputUtil.log("Log_Script_Except.rtf", "Event name : " + this.name + ", method Name : setup-party:\n" + var5 + "\n" + (old == null ? "no old exception" : old));
      }

   }

   public void startInstance(EventInstanceManager eim, String leader) {
      try {
         this.iv.invokeFunction("setup", eim);
         eim.setProperty("leader", leader);
      } catch (Exception var4) {
         System.out.println("Event name : " + this.name + ", method Name : setup-leader:\n" + var4);
         FileoutputUtil.log("Log_Script_Except.rtf", "Event name : " + this.name + ", method Name : setup-leader:\n" + var4);
      }

   }

   public void warpAllPlayer(int from, int to) {
      MapleMap tomap = this.getMapFactory().getMap(to);
      MapleMap frommap = this.getMapFactory().getMap(from);
      List<MapleCharacter> list = frommap.getCharactersThreadsafe();
      if (tomap != null && frommap != null && list != null && frommap.getCharactersSize() > 0) {
         Iterator var6 = list.iterator();

         while(var6.hasNext()) {
            MapleMapObject mapleMapObject = (MapleMapObject)var6.next();
            ((MapleCharacter)mapleMapObject).changeMap(tomap, tomap.getPortal(0));
         }
      }

   }

   public MapleMapFactory getMapFactory() {
      return this.getChannelServer().getMapFactory();
   }

   public OverrideMonsterStats newMonsterStats() {
      return new OverrideMonsterStats();
   }

   public List<MapleCharacter> newCharList() {
      return new ArrayList();
   }

   public MapleMonster getMonster(int id) {
      return MapleLifeFactory.getMonster(id);
   }

   public MapleMonster getMonster(int id, boolean extreme) {
      return MapleLifeFactory.getMonster(id, extreme);
   }

   public MapleMonster getMonster(int id, boolean extreme, boolean hellMode) {
      return MapleLifeFactory.getMonster(id, extreme, hellMode);
   }

   public MapleReactor getReactor(int id) {
      return new MapleReactor(MapleReactorFactory.getReactor(id), id);
   }

   public void broadcastShip(int mapid, int effect, int mode) {
      this.getMapFactory().getMap(mapid).broadcastMessage(CField.boatPacket(effect, mode));
   }

   public void broadcastYellowMsg(String msg) {
      this.getChannelServer().broadcastPacket(CWvsContext.yellowChat(msg));
   }

   public void broadcastServerMsg(int type, String msg, boolean weather) {
      if (!weather) {
         this.getChannelServer().broadcastPacket(CWvsContext.serverNotice(type, "", msg));
      } else {
         Iterator var4 = this.getMapFactory().getAllMaps().iterator();

         while(var4.hasNext()) {
            MapleMap load = (MapleMap)var4.next();
            if (load.getCharactersSize() > 0) {
               load.startMapEffect(msg, type);
            }
         }
      }

   }

   public boolean scheduleRandomEvent() {
      boolean omg = false;

      for(int i = 0; i < eventChannel.length; ++i) {
         omg |= this.scheduleRandomEventInChannel(eventChannel[i]);
      }

      return omg;
   }

   public boolean scheduleRandomEventInChannel(int chz) {
      final ChannelServer cs = ChannelServer.getInstance(chz);
      if (cs != null && cs.getEvent() <= -1) {
         MapleEventType t = null;

         while(true) {
            while(t == null) {
               MapleEventType[] var4 = MapleEventType.values();
               int var5 = var4.length;

               for(int var6 = 0; var6 < var5; ++var6) {
                  MapleEventType x = var4[var6];
                  if (Randomizer.nextInt(MapleEventType.values().length) == 0 && x != MapleEventType.OxQuiz) {
                     t = x;
                     break;
                  }
               }
            }

            String msg = MapleEvent.scheduleEvent(t, cs);
            if (msg.length() > 0) {
               this.broadcastYellowMsg(msg);
               return false;
            }

            Timer.EventTimer.getInstance().schedule(new Runnable() {
               public void run() {
                  if (cs.getEvent() >= 0) {
                     MapleEvent.setEvent(cs, true);
                  }

               }
            }, 180000L);
            return true;
         }
      } else {
         return false;
      }
   }

   public void setWorldEvent() {
      for(int i = 0; i < eventChannel.length; ++i) {
         eventChannel[i] = Randomizer.nextInt(ChannelServer.getAllInstances().size() - 4) + 2 + i;
      }

   }
}
