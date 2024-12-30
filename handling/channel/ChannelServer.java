package handling.channel;

import client.MapleCharacter;
import constants.ServerType;
import handling.login.LoginServer;
import handling.netty.MapleNettyDecoder;
import handling.netty.MapleNettyEncoder;
import handling.netty.MapleNettyHandler;
import handling.world.CheaterData;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.mina.core.service.IoAcceptor;
import scripting.EventScriptManager;
import server.ServerProperties;
import server.events.MapleCoconut;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.events.MapleFitness;
import server.events.MapleOla;
import server.events.MapleOxQuiz;
import server.events.MapleSnowball;
import server.events.MapleSurvival;
import server.life.PlayerNPC;
import server.maps.AramiaFireWorks;
import server.maps.MapleMapFactory;
import tools.Pair;
import tools.packet.CWvsContext;

public class ChannelServer {
   public static long serverStartTime;
   private int expRate = 4;
   private int mesoRate = 2;
   private int dropRate = 2;
   private int cashRate = 1;
   private int traitRate = 1;
   private short port;
   private int channel;
   private int running_MerchantID = 0;
   private int flags = 0;
   private String serverMessage;
   private String ip;
   private String serverName;
   private boolean shutdown = false;
   private boolean finishedShutdown = false;
   private boolean MegaphoneMuteState = false;
   private boolean adminOnly = false;
   private PlayerStorage players;
   private IoAcceptor acceptor;
   private final MapleMapFactory mapFactory;
   private EventScriptManager eventSM;
   private AramiaFireWorks works = new AramiaFireWorks();
   private static final Map<Integer, ChannelServer> instances = new HashMap();
   private final List<PlayerNPC> playerNPCs = new LinkedList();
   private int eventmap = -1;
   private final List<List<Pair<Integer, MapleCharacter>>> soulmatch = new ArrayList();
   private final Map<MapleEventType, MapleEvent> events = new EnumMap(MapleEventType.class);
   private static ServerBootstrap bootstrap;
   public boolean 얼리기 = false;
   public boolean JuhunFever;
   public boolean is얼리기 = false;

   private ChannelServer(int channel) {
      this.channel = channel;
      this.mapFactory = new MapleMapFactory(channel);
   }

   public static Set<Integer> getAllInstance() {
      return new HashSet(instances.keySet());
   }

   public final void loadEvents() {
      if (this.events.size() == 0) {
         this.events.put(MapleEventType.CokePlay, new MapleCoconut(this.channel, MapleEventType.CokePlay));
         this.events.put(MapleEventType.Coconut, new MapleCoconut(this.channel, MapleEventType.Coconut));
         this.events.put(MapleEventType.Fitness, new MapleFitness(this.channel, MapleEventType.Fitness));
         this.events.put(MapleEventType.OlaOla, new MapleOla(this.channel, MapleEventType.OlaOla));
         this.events.put(MapleEventType.OxQuiz, new MapleOxQuiz(this.channel, MapleEventType.OxQuiz));
         this.events.put(MapleEventType.Snowball, new MapleSnowball(this.channel, MapleEventType.Snowball));
         this.events.put(MapleEventType.Survival, new MapleSurvival(this.channel, MapleEventType.Survival));
      }
   }

   public final void run_startup_configurations() {
      this.setChannel(this.channel);

      try {
         this.expRate = Integer.parseInt(ServerProperties.getProperty("world.exp"));
         this.mesoRate = Integer.parseInt(ServerProperties.getProperty("world.meso"));
         this.serverMessage = ServerProperties.getProperty("world.serverMessage");
         this.serverName = ServerProperties.getProperty("login.serverName");
         this.flags = Integer.parseInt(ServerProperties.getProperty("world.flags", "0"));
         this.adminOnly = Boolean.parseBoolean(ServerProperties.getProperty("world.admin", "false"));
         this.eventSM = new EventScriptManager(this, ServerProperties.getProperty("channel.events").split(","));
         this.port = (short)(Short.parseShort(ServerProperties.getProperty("ports.channel")) + this.channel - 1);
      } catch (Exception var5) {
         throw new RuntimeException(var5);
      }

      this.ip = "185.213.243.67:" + this.port;
      NioEventLoopGroup nioEventLoopGroup1 = new NioEventLoopGroup();
      NioEventLoopGroup nioEventLoopGroup2 = new NioEventLoopGroup();

      try {
         bootstrap = new ServerBootstrap();
         ((ServerBootstrap)((ServerBootstrap)bootstrap.group(nioEventLoopGroup1, nioEventLoopGroup2).channel(NioServerSocketChannel.class)).childHandler(new ChannelInitializer<SocketChannel>() {
            public void initChannel(SocketChannel ch) throws Exception {
               ch.pipeline().addLast("decoder", new MapleNettyDecoder());
               ch.pipeline().addLast("encoder", new MapleNettyEncoder());
               ch.pipeline().addLast("handler", new MapleNettyHandler(ServerType.CHANNEL, ChannelServer.this.channel));
            }
         }).option(ChannelOption.SO_BACKLOG, 128)).childOption(ChannelOption.SO_SNDBUF, 4194304).childOption(ChannelOption.SO_KEEPALIVE, true);
         ChannelFuture f = bootstrap.bind(this.port).sync();
         PrintStream var10000 = System.out;
         Object var10001 = this.getChannel() == 1 ? 1 : (this.getChannel() == 2 ? "20세이상" : this.getChannel() - 1);
         var10000.println("[알림] 채널 " + var10001 + " 서버가 " + this.port + " 포트를 성공적으로 개방했습니다.");
         this.eventSM.init();
      } catch (InterruptedException var4) {
         System.out.println("[오류] 채널서버가 " + this.port + " 포트를 개방하는데 실패했습니다.");
         var4.printStackTrace();
      }

   }

   public final void shutdown() {
      if (!this.finishedShutdown) {
         this.broadcastPacket(CWvsContext.serverNotice(0, "", "This channel will now shut down."));
         this.shutdown = true;
         System.out.println("Channel " + this.channel + ", Saving characters...");
         this.getPlayerStorage().disconnectAll();
         System.out.println("Channel " + this.channel + ", Unbinding...");
         instances.remove(this.channel);
         this.setFinishShutdown();
      }
   }

   public final void unbind() {
      this.acceptor.unbind();
   }

   public final boolean hasFinishedShutdown() {
      return this.finishedShutdown;
   }

   public final MapleMapFactory getMapFactory() {
      return this.mapFactory;
   }

   public static final ChannelServer newInstance(int channel) {
      return new ChannelServer(channel);
   }

   public static final ChannelServer getInstance(int channel) {
      return (ChannelServer)instances.get(channel);
   }

   public final void addPlayer(MapleCharacter chr) {
      this.getPlayerStorage().registerPlayer(chr);
   }

   public final PlayerStorage getPlayerStorage() {
      if (this.players == null) {
         this.players = new PlayerStorage();
      }

      return this.players;
   }

   public final void removePlayer(MapleCharacter chr) {
      this.getPlayerStorage().deregisterPlayer(chr);
   }

   public final void removePlayer(int idz, int accIdz, String namez) {
      this.getPlayerStorage().deregisterPlayer(idz, accIdz, namez);
   }

   public final String getServerMessage() {
      return this.serverMessage;
   }

   public final void setServerMessage(String newMessage) {
      this.serverMessage = newMessage;
      this.broadcastPacket(CWvsContext.serverNotice(26, "", this.serverMessage));
   }

   public final void broadcastPacket(byte[] data) {
      this.getPlayerStorage().broadcastPacket(data);
   }

   public final void broadcastSmegaPacket(byte[] data) {
      this.getPlayerStorage().broadcastSmegaPacket(data);
   }

   public final void broadcastGMPacket(byte[] data) {
      this.getPlayerStorage().broadcastGMPacket(data);
   }

   public final int getExpRate() {
      return this.expRate;
   }

   public final void setExpRate(int expRate) {
      this.expRate = expRate;
   }

   public final int getCashRate() {
      return this.cashRate;
   }

   public final int getChannel() {
      return this.channel;
   }

   public final void setChannel(int channel) {
      instances.put(channel, this);
      LoginServer.addChannel(channel);
   }

   public static final ArrayList<ChannelServer> getAllInstances() {
      return new ArrayList(instances.values());
   }

   public final String getIP() {
      return this.ip;
   }

   public final boolean isShutdown() {
      return this.shutdown;
   }

   public final int getLoadedMaps() {
      return this.mapFactory.getLoadedMaps();
   }

   public final EventScriptManager getEventSM() {
      return this.eventSM;
   }

   public final void reloadEvents() {
      this.eventSM.cancel();
      this.eventSM = new EventScriptManager(this, ServerProperties.getProperty("channel.events").split(","));
      this.eventSM.init();
   }

   public final int getMesoRate() {
      return this.mesoRate;
   }

   public final void setMesoRate(int mesoRate) {
      this.mesoRate = mesoRate;
   }

   public final int getDropRate() {
      return this.dropRate;
   }

   public static final void startChannel_Main() {
      serverStartTime = System.currentTimeMillis();

      try {
         for(int i = 0; i < Integer.parseInt(ServerProperties.getProperty("channel.count", "0")); ++i) {
            newInstance(i + 1).run_startup_configurations();
         }
      } catch (Exception var1) {
         System.out.println("[오류] 채널 서버 오픈이 실패했습니다.");
         var1.printStackTrace();
      }

   }

   public final void toggleMegaphoneMuteState() {
      this.MegaphoneMuteState = !this.MegaphoneMuteState;
   }

   public final boolean getMegaphoneMuteState() {
      return this.MegaphoneMuteState;
   }

   public int getEvent() {
      return this.eventmap;
   }

   public final void setEvent(int ze) {
      this.eventmap = ze;
   }

   public MapleEvent getEvent(MapleEventType t) {
      return (MapleEvent)this.events.get(t);
   }

   public final Collection<PlayerNPC> getAllPlayerNPC() {
      return this.playerNPCs;
   }

   public final void addPlayerNPC(PlayerNPC npc) {
      if (!this.playerNPCs.contains(npc)) {
         this.playerNPCs.add(npc);
         this.getMapFactory().getMap(npc.getMapId()).addMapObject(npc);
      }
   }

   public final void removePlayerNPC(PlayerNPC npc) {
      if (this.playerNPCs.contains(npc)) {
         this.playerNPCs.remove(npc);
         this.getMapFactory().getMap(npc.getMapId()).removeMapObject(npc);
      }

   }

   public final String getServerName() {
      return this.serverName;
   }

   public final void setServerName(String sn) {
      this.serverName = sn;
   }

   public final String getTrueServerName() {
      return this.serverName.substring(0, this.serverName.length() - 3);
   }

   public final int getPort() {
      return this.port;
   }

   public static final Set<Integer> getChannelServer() {
      return new HashSet(instances.keySet());
   }

   public final void setShutdown() {
      this.shutdown = true;
      System.out.println("Channel " + this.channel + " has set to shutdown and is closing Hired Merchants...");
   }

   public final void setFinishShutdown() {
      this.finishedShutdown = true;
      System.out.println("Channel " + this.channel + " has finished shutdown.");
   }

   public final boolean isAdminOnly() {
      return this.adminOnly;
   }

   public static final int getChannelCount() {
      return instances.size();
   }

   public final int getTempFlag() {
      return this.flags;
   }

   public static Map<Integer, Integer> getChannelLoad() {
      Map<Integer, Integer> ret = new HashMap();
      Iterator var1 = instances.values().iterator();

      while(var1.hasNext()) {
         ChannelServer cs = (ChannelServer)var1.next();
         ret.put(cs.getChannel(), cs.getConnectedClients());
      }

      return ret;
   }

   public int getConnectedClients() {
      return this.getPlayerStorage().getConnectedClients();
   }

   public List<CheaterData> getCheaters() {
      List<CheaterData> cheaters = this.getPlayerStorage().getCheaters();
      Collections.sort(cheaters);
      return cheaters;
   }

   public List<CheaterData> getReports() {
      List<CheaterData> cheaters = this.getPlayerStorage().getReports();
      Collections.sort(cheaters);
      return cheaters;
   }

   public void broadcastMessage(byte[] message) {
      this.broadcastPacket(message);
   }

   public void broadcastSmega(byte[] message) {
      this.broadcastSmegaPacket(message);
   }

   public void broadcastGMMessage(byte[] message) {
      this.broadcastGMPacket(message);
   }

   public AramiaFireWorks getFireWorks() {
      return this.works;
   }

   public int getTraitRate() {
      return this.traitRate;
   }

   public boolean isMyChannelConnected(String charName) {
      return this.getPlayerStorage().getCharacterByName(charName) != null;
   }

   public List<List<Pair<Integer, MapleCharacter>>> getSoulmatch() {
      return this.soulmatch;
   }

   public boolean 얼리기() {
      return this.is얼리기;
   }

   public void 얼리기(boolean a) {
      this.is얼리기 = a;
   }

   public boolean is얼리기() {
      return this.얼리기;
   }

   public void is얼리기(boolean a) {
      this.얼리기 = a;
   }

   public boolean JuhunFever() {
      return this.JuhunFever;
   }

   public void JuhunFever(boolean JuhunFever) {
      this.JuhunFever = JuhunFever;
   }
}
