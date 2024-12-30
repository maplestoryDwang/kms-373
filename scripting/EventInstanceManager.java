package scripting;

import client.MapleCharacter;
import client.MapleQuestStatus;
import client.MapleTrait;
import client.SkillFactory;
import handling.channel.ChannelServer;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildCharacter;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.script.ScriptException;
import server.MapleItemInformationProvider;
import server.Timer;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.quest.MapleQuest;
import tools.FileoutputUtil;
import tools.Pair;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.SLFCGPacket;

public class EventInstanceManager {
   private List<MapleCharacter> chars = new CopyOnWriteArrayList();
   private List<Integer> dced = new LinkedList();
   private List<MapleMonster> mobs = new LinkedList();
   private Map<Integer, Integer> killCount = new HashMap();
   private EventManager em;
   private int channel;
   private String name;
   private Properties props = new Properties();
   private long timeStarted = 0L;
   private long eventTime = 0L;
   private List<Integer> mapIds = new LinkedList();
   private List<Boolean> isInstanced = new LinkedList();
   private ScheduledFuture<?> eventTimer;
   private final ReentrantReadWriteLock mutex = new ReentrantReadWriteLock();
   private final Lock rL;
   private final Lock wL;
   private boolean disposed;

   public EventInstanceManager(EventManager em, String name, int channel) {
      this.rL = this.mutex.readLock();
      this.wL = this.mutex.writeLock();
      this.disposed = false;
      this.em = em;
      this.name = name;
      this.channel = channel;
   }

   public void registerPlayer(MapleCharacter chr) {
      if (!this.disposed && chr != null) {
         try {
            this.chars.add(chr);
            chr.setEventInstance(this);
            this.em.getIv().invokeFunction("playerEntry", this, chr);
         } catch (NullPointerException var3) {
            FileoutputUtil.outputFileError("Log_Script_Except.rtf", var3);
            var3.printStackTrace();
         } catch (Exception var4) {
            String var10001 = this.em.getName();
            FileoutputUtil.log("Log_Script_Except.rtf", "Event name" + var10001 + ", Instance name : " + this.name + ", method Name : playerEntry:\n" + var4);
            PrintStream var10000 = System.out;
            var10001 = this.em.getName();
            var10000.println("Event name" + var10001 + ", Instance name : " + this.name + ", method Name : playerEntry:\n" + var4);
         }

      }
   }

   public void changedMap(MapleCharacter chr, int mapid) {
      if (!this.disposed) {
         try {
            this.em.getIv().invokeFunction("changedMap", this, chr, mapid);
         } catch (NullPointerException var4) {
         } catch (Exception var5) {
            String var10001 = this.em.getName();
            FileoutputUtil.log("Log_Script_Except.rtf", "Event name : " + var10001 + ", Instance name : " + this.name + ", method Name : changedMap:\n" + var5);
            PrintStream var10000 = System.out;
            var10001 = this.em.getName();
            var10000.println("Event name" + var10001 + ", Instance name : " + this.name + ", method Name : changedMap:\n" + var5);
         }

      }
   }

   public void timeOut(long delay, final EventInstanceManager eim) {
      if (!this.disposed && eim != null) {
         this.eventTimer = Timer.EventTimer.getInstance().schedule(new Runnable() {
            public void run() {
               if (!EventInstanceManager.this.disposed && eim != null && EventInstanceManager.this.em != null) {
                  try {
                     EventInstanceManager.this.em.getIv().invokeFunction("scheduledTimeout", eim);
                  } catch (Exception var2) {
                     String var10001 = EventInstanceManager.this.em.getName();
                     FileoutputUtil.log("Log_Script_Except.rtf", "Event name" + var10001 + ", Instance name : " + EventInstanceManager.this.name + ", method Name : scheduledTimeout:\n" + var2);
                     PrintStream var10000 = System.out;
                     var10001 = EventInstanceManager.this.em.getName();
                     var10000.println("Event name" + var10001 + ", Instance name : " + EventInstanceManager.this.name + ", method Name : scheduledTimeout:\n" + var2);
                  }

               }
            }
         }, delay);
      }
   }

   public void stopEventTimer() {
      this.eventTime = 0L;
      this.timeStarted = 0L;
      if (this.eventTimer != null) {
         this.eventTimer.cancel(false);
      }

   }

   public void restartEventTimerMillSecond(int time) {
      try {
         if (this.disposed) {
            return;
         }

         this.timeStarted = System.currentTimeMillis();
         this.eventTime = (long)time;
         if (this.eventTimer != null) {
            this.eventTimer.cancel(false);
         }

         this.eventTimer = null;
         Iterator var2 = this.getPlayers().iterator();

         while(var2.hasNext()) {
            MapleCharacter chr = (MapleCharacter)var2.next();
            chr.getClient().getSession().writeAndFlush(SLFCGPacket.milliTimer(time));
         }

         this.timeOut((long)time, this);
      } catch (Exception var4) {
         FileoutputUtil.outputFileError("Log_Script_Except.rtf", var4);
         PrintStream var10000 = System.out;
         String var10001 = this.em.getName();
         var10000.println("Event name" + var10001 + ", Instance name : " + this.name + ", method Name : restartEventTimerMillSecond:\n");
         var4.printStackTrace();
      }

   }

   public void restartEventTimer(long time) {
      this.restartEventTimer(time, false);
   }

   public void restartEventTimer(long time, int type) {
      try {
         if (this.disposed) {
            return;
         }

         this.timeStarted = System.currentTimeMillis();
         this.eventTime = time;
         if (this.eventTimer != null) {
            this.eventTimer.cancel(false);
         }

         this.eventTimer = null;
         int timesend = (int)time / 1000;
         Iterator var5 = this.getPlayers().iterator();

         while(true) {
            while(var5.hasNext()) {
               MapleCharacter chr = (MapleCharacter)var5.next();
               if (this.name.startsWith("PVP")) {
                  chr.getClient().getSession().writeAndFlush(CField.getPVPClock(Integer.parseInt(this.getProperty("type")), timesend));
               } else if (type != 4 && type != 5) {
                  chr.getClient().getSession().writeAndFlush(CField.getClock(timesend));
               } else {
                  chr.getClient().getSession().writeAndFlush(CField.getVanVanClock((byte)(type != 4 ? 1 : 0), timesend));
               }
            }

            this.timeOut(time, this);
            break;
         }
      } catch (Exception var7) {
         FileoutputUtil.outputFileError("Log_Script_Except.rtf", var7);
         PrintStream var10000 = System.out;
         String var10001 = this.em.getName();
         var10000.println("Event name" + var10001 + ", Instance name : " + this.name + ", method Name : restartEventTimer:\n");
         var7.printStackTrace();
      }

   }

   public void restartEventTimer(long time, boolean punchking) {
      try {
         if (this.disposed) {
            return;
         }

         this.timeStarted = System.currentTimeMillis();
         this.eventTime = time;
         if (this.eventTimer != null) {
            this.eventTimer.cancel(false);
         }

         this.eventTimer = null;
         int timesend = (int)time / 1000;
         Iterator var5 = this.getPlayers().iterator();

         while(var5.hasNext()) {
            MapleCharacter chr = (MapleCharacter)var5.next();
            if (this.name.startsWith("PVP")) {
               chr.getClient().getSession().writeAndFlush(CField.getPVPClock(Integer.parseInt(this.getProperty("type")), timesend));
            } else if (punchking) {
               chr.getClient().getSession().writeAndFlush(CField.getClockMilliEvent(this.timeStarted + this.eventTime));
            } else {
               chr.getClient().getSession().writeAndFlush(CField.getClock(timesend));
            }
         }

         this.timeOut(time, this);
      } catch (Exception var7) {
         FileoutputUtil.outputFileError("Log_Script_Except.rtf", var7);
         PrintStream var10000 = System.out;
         String var10001 = this.em.getName();
         var10000.println("Event name" + var10001 + ", Instance name : " + this.name + ", method Name : restartEventTimer:\n");
         var7.printStackTrace();
      }

   }

   public void startEventTimer(long time) {
      this.restartEventTimer(time, false);
   }

   public void startEventTimer(long time, boolean punchking) {
      this.restartEventTimer(time, punchking);
   }

   public boolean isTimerStarted() {
      return this.eventTime > 0L && this.timeStarted > 0L;
   }

   public long getTimeLeft() {
      return this.eventTime - (System.currentTimeMillis() - this.timeStarted);
   }

   public void registerParty(MapleParty party, MapleMap map) {
      if (!this.disposed) {
         Iterator var3 = party.getMembers().iterator();

         while(var3.hasNext()) {
            MaplePartyCharacter pc = (MaplePartyCharacter)var3.next();
            this.registerPlayer(map.getCharacterById(pc.getId()));
         }

      }
   }

   public void registerGuild(MapleGuild guild, MapleMap map) {
      if (!this.disposed) {
         Iterator var3 = guild.getMembers().iterator();

         while(var3.hasNext()) {
            MapleGuildCharacter pc = (MapleGuildCharacter)var3.next();
            this.registerPlayer(map.getCharacterById(pc.getId()));
         }

      }
   }

   public void unregisterPlayer(MapleCharacter chr) {
      if (this.disposed) {
         chr.setEventInstance((EventInstanceManager)null);
      } else {
         this.unregisterPlayer_NoLock(chr);
      }
   }

   private boolean unregisterPlayer_NoLock(MapleCharacter chr) {
      chr.setEventInstance((EventInstanceManager)null);
      if (this.disposed) {
         return false;
      } else if (this.chars.contains(chr)) {
         this.chars.remove(chr);
         return true;
      } else {
         return false;
      }
   }

   public final boolean disposeIfPlayerBelow(byte size, int towarp) {
      if (this.disposed) {
         return true;
      } else {
         MapleMap map = null;
         if (towarp > 0) {
            map = this.getMapFactory().getMap(towarp);
         }

         if (this.chars != null && this.chars.size() <= size) {
            List<MapleCharacter> chrs = new LinkedList(this.chars);
            Iterator var5 = chrs.iterator();

            while(var5.hasNext()) {
               MapleCharacter chr = (MapleCharacter)var5.next();
               if (chr != null) {
                  this.unregisterPlayer_NoLock(chr);
                  if (towarp > 0) {
                     chr.changeMap(map, map.getPortal(0));
                  }
               }
            }

            this.dispose_NoLock();
            return true;
         } else {
            return false;
         }
      }
   }

   public final void saveBossQuest(int points) {
      if (!this.disposed) {
         MapleCharacter chr;
         for(Iterator var2 = this.getPlayers().iterator(); var2.hasNext(); chr.getTrait(MapleTrait.MapleTraitType.will).addExp(points / 100, chr)) {
            chr = (MapleCharacter)var2.next();
            MapleQuestStatus record = chr.getQuestNAdd(MapleQuest.getInstance(150001));
            if (record.getCustomData() != null) {
               record.setCustomData(String.valueOf(points + Integer.parseInt(record.getCustomData())));
            } else {
               record.setCustomData(String.valueOf(points));
            }
         }

      }
   }

   public final void saveNX(int points) {
      if (!this.disposed) {
         Iterator var2 = this.getPlayers().iterator();

         while(var2.hasNext()) {
            MapleCharacter chr = (MapleCharacter)var2.next();
            chr.modifyCSPoints(1, points, true);
         }

      }
   }

   public List<MapleCharacter> getPlayers() {
      return (List)(this.disposed ? Collections.emptyList() : new ArrayList(this.chars));
   }

   public List<Integer> getDisconnected() {
      return this.dced;
   }

   public final int getPlayerCount() {
      return this.disposed ? 0 : this.chars.size();
   }

   public void registerMonster(MapleMonster mob) {
      if (!this.disposed) {
         this.mobs.add(mob);
         mob.setEventInstance(this);
      }
   }

   public void unregisterMonster(MapleMonster mob) {
      mob.setEventInstance((EventInstanceManager)null);
      if (!this.disposed) {
         if (this.mobs.contains(mob)) {
            this.mobs.remove(mob);
         }

         if (this.mobs.isEmpty()) {
            try {
               this.em.getIv().invokeFunction("allMonstersDead", this);
            } catch (Exception var3) {
               String var10001 = this.em.getName();
               FileoutputUtil.log("Log_Script_Except.rtf", "Event name" + var10001 + ", Instance name : " + this.name + ", method Name : allMonstersDead:\n" + var3);
               PrintStream var10000 = System.out;
               var10001 = this.em.getName();
               var10000.println("Event name" + var10001 + ", Instance name : " + this.name + ", method Name : allMonstersDead:\n" + var3);
            }
         }

      }
   }

   public void playerKilled(MapleCharacter chr) {
      if (!this.disposed) {
         try {
            this.em.getIv().invokeFunction("playerDead", this, chr);
         } catch (Exception var3) {
            String var10001 = this.em.getName();
            FileoutputUtil.log("Log_Script_Except.rtf", "Event name" + var10001 + ", Instance name : " + this.name + ", method Name : playerDead:\n" + var3);
            PrintStream var10000 = System.out;
            var10001 = this.em.getName();
            var10000.println("Event name" + var10001 + ", Instance name : " + this.name + ", method Name : playerDead:\n" + var3);
         }

      }
   }

   public boolean revivePlayer(MapleCharacter chr) {
      if (this.disposed) {
         return false;
      } else {
         try {
            Object b = this.em.getIv().invokeFunction("playerRevive", this, chr);
            if (b instanceof Boolean) {
               return (Boolean)b;
            }
         } catch (Exception var3) {
            String var10001 = this.em.getName();
            FileoutputUtil.log("Log_Script_Except.rtf", "Event name" + var10001 + ", Instance name : " + this.name + ", method Name : playerRevive:\n" + var3);
            PrintStream var10000 = System.out;
            var10001 = this.em.getName();
            var10000.println("Event name" + var10001 + ", Instance name : " + this.name + ", method Name : playerRevive:\n" + var3);
         }

         return true;
      }
   }

   public void playerDisconnected(MapleCharacter chr, int idz) {
      if (!this.disposed) {
         byte ret;
         try {
            ret = ((Double)this.em.getIv().invokeFunction("playerDisconnected", this, chr)).byteValue();
         } catch (Exception var7) {
            ret = 0;
         }

         if (!this.disposed) {
            if (chr == null || chr.isAlive()) {
               this.dced.add(idz);
            }

            if (chr != null) {
               this.unregisterPlayer_NoLock(chr);
            }

            if (ret == 0) {
               if (this.getPlayerCount() <= 0) {
                  this.dispose_NoLock();
               }
            } else if (ret > 0 && this.getPlayerCount() < ret || ret < 0 && (this.isLeader(chr) || this.getPlayerCount() < ret * -1)) {
               List<MapleCharacter> chrs = new LinkedList(this.chars);
               Iterator var5 = chrs.iterator();

               while(var5.hasNext()) {
                  MapleCharacter player = (MapleCharacter)var5.next();
                  if (player.getId() != idz) {
                     this.removePlayer(player);
                  }
               }

               this.dispose_NoLock();
            }

         }
      }
   }

   public void monsterKilled(MapleCharacter chr, MapleMonster mob) {
      if (!this.disposed) {
         String var10001;
         try {
            int inc = (Integer)this.em.getIv().invokeFunction("monsterValue", this, mob.getId());
            if (this.disposed || chr == null) {
               return;
            }

            Integer kc = (Integer)this.killCount.get(chr.getId());
            if (kc == null) {
               kc = inc;
            } else {
               kc = kc + inc;
            }

            this.killCount.put(chr.getId(), kc);
         } catch (ScriptException var5) {
            var10001 = this.em == null ? "null" : this.em.getName();
            System.out.println("Event name" + var10001 + ", Instance name : " + this.name + ", method Name : monsterValue:\n" + var5);
            var10001 = this.em == null ? "null" : this.em.getName();
            FileoutputUtil.log("Log_Script_Except.rtf", "Event name" + var10001 + ", Instance name : " + this.name + ", method Name : monsterValue:\n" + var5);
         } catch (NoSuchMethodException var6) {
            var10001 = this.em == null ? "null" : this.em.getName();
            System.out.println("Event name" + var10001 + ", Instance name : " + this.name + ", method Name : monsterValue:\n" + var6);
            var10001 = this.em == null ? "null" : this.em.getName();
            FileoutputUtil.log("Log_Script_Except.rtf", "Event name" + var10001 + ", Instance name : " + this.name + ", method Name : monsterValue:\n" + var6);
         } catch (Exception var7) {
            var7.printStackTrace();
            FileoutputUtil.outputFileError("Log_Script_Except.rtf", var7);
         }

      }
   }

   public void monsterDamaged(MapleCharacter chr, MapleMonster mob, long damage) {
      List<Integer> mobs = Collections.unmodifiableList(Arrays.asList(9700037, 8850011));
      if (!this.disposed && mobs.contains(mob.getId())) {
         String var10001;
         try {
            this.em.getIv().invokeFunction("monsterDamaged", this, chr, mob.getId(), damage);
         } catch (ScriptException var7) {
            var10001 = this.em == null ? "null" : this.em.getName();
            System.out.println("Event name" + var10001 + ", Instance name : " + this.name + ", method Name : monsterValue:\n" + var7);
            var10001 = this.em == null ? "null" : this.em.getName();
            FileoutputUtil.log("Log_Script_Except.rtf", "Event name" + var10001 + ", Instance name : " + this.name + ", method Name : monsterValue:\n" + var7);
         } catch (NoSuchMethodException var8) {
            var10001 = this.em == null ? "null" : this.em.getName();
            System.out.println("Event name" + var10001 + ", Instance name : " + this.name + ", method Name : monsterValue:\n" + var8);
            var10001 = this.em == null ? "null" : this.em.getName();
            FileoutputUtil.log("Log_Script_Except.rtf", "Event name" + var10001 + ", Instance name : " + this.name + ", method Name : monsterValue:\n" + var8);
         } catch (Exception var9) {
            var9.printStackTrace();
            FileoutputUtil.outputFileError("Log_Script_Except.rtf", var9);
         }

      }
   }

   public void addPVPScore(MapleCharacter chr, int score) {
      if (!this.disposed) {
         String var10001;
         try {
            this.em.getIv().invokeFunction("addPVPScore", this, chr, score);
         } catch (ScriptException var4) {
            var10001 = this.em == null ? "null" : this.em.getName();
            System.out.println("Event name" + var10001 + ", Instance name : " + this.name + ", method Name : monsterValue:\n" + var4);
            var10001 = this.em == null ? "null" : this.em.getName();
            FileoutputUtil.log("Log_Script_Except.rtf", "Event name" + var10001 + ", Instance name : " + this.name + ", method Name : monsterValue:\n" + var4);
         } catch (NoSuchMethodException var5) {
            var10001 = this.em == null ? "null" : this.em.getName();
            System.out.println("Event name" + var10001 + ", Instance name : " + this.name + ", method Name : monsterValue:\n" + var5);
            var10001 = this.em == null ? "null" : this.em.getName();
            FileoutputUtil.log("Log_Script_Except.rtf", "Event name" + var10001 + ", Instance name : " + this.name + ", method Name : monsterValue:\n" + var5);
         } catch (Exception var6) {
            var6.printStackTrace();
            FileoutputUtil.outputFileError("Log_Script_Except.rtf", var6);
         }

      }
   }

   public int getKillCount(MapleCharacter chr) {
      if (this.disposed) {
         return 0;
      } else {
         Integer kc = (Integer)this.killCount.get(chr.getId());
         return kc == null ? 0 : kc;
      }
   }

   public void dispose_NoLock() {
      if (!this.disposed && this.em != null) {
         String emN = this.em.getName();

         try {
            this.disposed = true;
            Iterator var2 = this.chars.iterator();

            while(var2.hasNext()) {
               MapleCharacter chr = (MapleCharacter)var2.next();
               chr.setEventInstance((EventInstanceManager)null);
            }

            this.chars.clear();
            this.chars = null;
            if (this.mobs.size() >= 1) {
               var2 = this.mobs.iterator();

               while(var2.hasNext()) {
                  MapleMonster mob = (MapleMonster)var2.next();
                  if (mob != null) {
                     mob.setEventInstance((EventInstanceManager)null);
                  }
               }
            }

            this.mobs.clear();
            this.mobs = null;
            this.killCount.clear();
            this.killCount = null;
            this.dced.clear();
            this.dced = null;
            this.timeStarted = 0L;
            this.eventTime = 0L;
            this.props.clear();
            this.props = null;

            for(int i = 0; i < this.mapIds.size(); ++i) {
               if ((Boolean)this.isInstanced.get(i)) {
                  this.getMapFactory().removeInstanceMap((Integer)this.mapIds.get(i));
               }
            }

            this.mapIds.clear();
            this.mapIds = null;
            this.isInstanced.clear();
            this.isInstanced = null;
            this.em.disposeInstance(this.name);
         } catch (Exception var4) {
            System.out.println("Caused by : " + emN + " instance name: " + this.name + " method: dispose:");
            var4.printStackTrace();
            FileoutputUtil.outputFileError("Log_Script_Except.rtf", var4);
         }

      }
   }

   public void dispose() {
      this.dispose_NoLock();
   }

   public ChannelServer getChannelServer() {
      return ChannelServer.getInstance(this.channel);
   }

   public List<MapleMonster> getMobs() {
      return this.mobs;
   }

   public final void giveAchievement(int type) {
      if (!this.disposed) {
         MapleCharacter var3;
         for(Iterator var2 = this.getPlayers().iterator(); var2.hasNext(); var3 = (MapleCharacter)var2.next()) {
         }

      }
   }

   public final void broadcastPlayerMsg(int type, String msg) {
      if (!this.disposed) {
         Iterator var3 = this.getPlayers().iterator();

         while(var3.hasNext()) {
            MapleCharacter chr = (MapleCharacter)var3.next();
            chr.dropMessage(type, msg);
         }

      }
   }

   public final void broadcastEnablePvP(MapleCharacter chr) {
      if (!this.disposed) {
         Iterator var2 = chr.getMap().getCharacters().iterator();

         while(var2.hasNext()) {
            MapleCharacter mc = (MapleCharacter)var2.next();
            mc.enablePvP();
         }

      }
   }

   public final List<Pair<Integer, String>> newPair() {
      return new ArrayList();
   }

   public void addToPair(List<Pair<Integer, String>> e, int e1, String e2) {
      e.add(new Pair(e1, e2));
   }

   public final List<Pair<Integer, MapleCharacter>> newPair_chr() {
      return new ArrayList();
   }

   public void addToPair_chr(List<Pair<Integer, MapleCharacter>> e, int e1, MapleCharacter e2) {
      e.add(new Pair(e1, e2));
   }

   public final void broadcastPacket(byte[] p) {
      if (!this.disposed) {
         Iterator var2 = this.getPlayers().iterator();

         while(var2.hasNext()) {
            MapleCharacter chr = (MapleCharacter)var2.next();
            chr.getClient().getSession().writeAndFlush(p);
         }

      }
   }

   public final void broadcastTeamPacket(byte[] p, int team) {
      if (!this.disposed) {
         Iterator var3 = this.getPlayers().iterator();

         while(var3.hasNext()) {
            MapleCharacter chr = (MapleCharacter)var3.next();
            if (chr.getTeam() == team) {
               chr.getClient().getSession().writeAndFlush(p);
            }
         }

      }
   }

   public final MapleMap createInstanceMap(int mapid) {
      if (this.disposed) {
         return null;
      } else {
         int assignedid = EventScriptManager.getNewInstanceMapId();
         this.mapIds.add(assignedid);
         this.isInstanced.add(true);
         return this.getMapFactory().CreateInstanceMap(mapid, true, true, true, assignedid);
      }
   }

   public final MapleMap createInstanceMapS(int mapid) {
      if (this.disposed) {
         return null;
      } else {
         int assignedid = EventScriptManager.getNewInstanceMapId();
         this.mapIds.add(assignedid);
         this.isInstanced.add(true);
         return this.getMapFactory().CreateInstanceMap(mapid, false, false, false, assignedid);
      }
   }

   public final MapleMap setInstanceMap(int mapid) {
      if (this.disposed) {
         return this.getMapFactory().getMap(mapid);
      } else {
         this.mapIds.add(mapid);
         this.isInstanced.add(false);
         return this.getMapFactory().getMap(mapid);
      }
   }

   public final MapleMapFactory getMapFactory() {
      return this.getChannelServer().getMapFactory();
   }

   public final void sendPunchKing(MapleMap map, int type, int data) {
   }

   public final MapleMap getMapInstance(int args) {
      if (this.disposed) {
         return null;
      } else {
         try {
            boolean instanced = false;
            int trueMapID = true;
            int trueMapID;
            if (args >= this.mapIds.size()) {
               trueMapID = args;
            } else {
               trueMapID = (Integer)this.mapIds.get(args);
               instanced = (Boolean)this.isInstanced.get(args);
            }

            MapleMap map = null;
            if (!instanced) {
               map = this.getMapFactory().getMap(trueMapID);
               if (map == null) {
                  return null;
               }

               if (map.getCharactersSize() == 0 && this.em.getProperty("shuffleReactors") != null && this.em.getProperty("shuffleReactors").equals("true")) {
                  map.shuffleReactors();
               }
            } else {
               map = this.getMapFactory().getInstanceMap(trueMapID);
               if (map == null) {
                  return null;
               }

               if (map.getCharactersSize() == 0 && this.em.getProperty("shuffleReactors") != null && this.em.getProperty("shuffleReactors").equals("true")) {
                  map.shuffleReactors();
               }
            }

            return map;
         } catch (NullPointerException var5) {
            FileoutputUtil.outputFileError("Log_Script_Except.rtf", var5);
            var5.printStackTrace();
            return null;
         }
      }
   }

   public final void schedule(final String methodName, long delay) {
      if (!this.disposed) {
         Timer.EventTimer.getInstance().schedule(new Runnable() {
            public void run() {
               if (!EventInstanceManager.this.disposed && EventInstanceManager.this != null && EventInstanceManager.this.em != null) {
                  try {
                     EventInstanceManager.this.em.getIv().invokeFunction(methodName, EventInstanceManager.this);
                  } catch (NullPointerException var2) {
                  } catch (Exception var3) {
                     PrintStream var10000 = System.out;
                     String var10001 = EventInstanceManager.this.em.getName();
                     var10000.println("Event name" + var10001 + ", Instance name : " + EventInstanceManager.this.name + ", method Name : " + methodName + ":\n" + var3);
                     var10001 = EventInstanceManager.this.em.getName();
                     FileoutputUtil.log("Log_Script_Except.rtf", "Event name" + var10001 + ", Instance name : " + EventInstanceManager.this.name + ", method Name(schedule) : " + methodName + " :\n" + var3);
                  }

               }
            }
         }, delay);
      }
   }

   public final String getName() {
      return this.name;
   }

   public final void setProperty(String key, String value) {
      if (!this.disposed) {
         this.props.setProperty(key, value);
      }
   }

   public final Object setProperty(String key, String value, boolean prev) {
      return this.disposed ? null : this.props.setProperty(key, value);
   }

   public final String getProperty(String key) {
      return this.disposed ? "" : this.props.getProperty(key);
   }

   public final Properties getProperties() {
      return this.props;
   }

   public final void leftParty(MapleCharacter chr) {
      if (!this.disposed) {
         try {
            this.em.getIv().invokeFunction("leftParty", this, chr);
         } catch (Exception var3) {
            PrintStream var10000 = System.out;
            String var10001 = this.em.getName();
            var10000.println("Event name" + var10001 + ", Instance name : " + this.name + ", method Name : leftParty:\n" + var3);
            var10001 = this.em.getName();
            FileoutputUtil.log("Log_Script_Except.rtf", "Event name" + var10001 + ", Instance name : " + this.name + ", method Name : leftParty:\n" + var3);
         }

      }
   }

   public final void disbandParty() {
      if (!this.disposed) {
         try {
            this.em.getIv().invokeFunction("disbandParty", this);
         } catch (Exception var2) {
            PrintStream var10000 = System.out;
            String var10001 = this.em.getName();
            var10000.println("Event name" + var10001 + ", Instance name : " + this.name + ", method Name : disbandParty:\n" + var2);
            var10001 = this.em.getName();
            FileoutputUtil.log("Log_Script_Except.rtf", "Event name" + var10001 + ", Instance name : " + this.name + ", method Name : disbandParty:\n" + var2);
         }

      }
   }

   public final void finishPQ() {
      if (!this.disposed) {
         try {
            this.em.getIv().invokeFunction("clearPQ", this);
         } catch (Exception var2) {
            PrintStream var10000 = System.out;
            String var10001 = this.em.getName();
            var10000.println("Event name" + var10001 + ", Instance name : " + this.name + ", method Name : clearPQ:\n" + var2);
            var10001 = this.em.getName();
            FileoutputUtil.log("Log_Script_Except.rtf", "Event name" + var10001 + ", Instance name : " + this.name + ", method Name : clearPQ:\n" + var2);
         }

      }
   }

   public final void removePlayer(MapleCharacter chr) {
      if (!this.disposed) {
         try {
            this.em.getIv().invokeFunction("playerExit", this, chr);
         } catch (Exception var3) {
            PrintStream var10000 = System.out;
            String var10001 = this.em.getName();
            var10000.println("Event name" + var10001 + ", Instance name : " + this.name + ", method Name : playerExit:\n" + var3);
            var10001 = this.em.getName();
            FileoutputUtil.log("Log_Script_Except.rtf", "Event name" + var10001 + ", Instance name : " + this.name + ", method Name : playerExit:\n" + var3);
         }

      }
   }

   public void onMapLoad(MapleCharacter chr) {
      if (!this.disposed) {
         try {
            this.em.getIv().invokeFunction("onMapLoad", this, chr);
         } catch (ScriptException var3) {
            PrintStream var10000 = System.out;
            String var10001 = this.em.getName();
            var10000.println("Event name" + var10001 + ", Instance name : " + this.name + ", method Name : onMapLoad:\n" + var3);
            var10001 = this.em.getName();
            FileoutputUtil.log("Log_Script_Except.rtf", "Event name" + var10001 + ", Instance name : " + this.name + ", method Name : onMapLoad:\n" + var3);
         } catch (NoSuchMethodException var4) {
         }

      }
   }

   public boolean isLeader(MapleCharacter chr) {
      return chr != null && chr.getParty() != null && chr.getParty().getLeader().getId() == chr.getId();
   }

   public boolean isDisconnected(MapleCharacter chr) {
      return !this.disposed && this.dced.contains(chr.getId());
   }

   public void removeDisconnected(int id) {
      if (!this.disposed) {
         this.dced.remove(id);
      }
   }

   public EventManager getEventManager() {
      return this.em;
   }

   public void applyBuff(MapleCharacter chr, int id) {
      MapleItemInformationProvider.getInstance().getItemEffect(id).applyTo(chr, true);
      chr.getClient().getSession().writeAndFlush(CWvsContext.InfoPacket.getStatusMsg(id));
   }

   public void applySkill(MapleCharacter chr, int id) {
      SkillFactory.getSkill(id).getEffect(1).applyTo(chr, false);
   }
}
