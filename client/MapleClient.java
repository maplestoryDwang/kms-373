package client;

import constants.ServerConstants;
import database.DatabaseConnection;
import database.DatabaseException;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.MapleMessenger;
import handling.world.MapleMessengerCharacter;
import handling.world.World;
import handling.world.guild.MapleGuildCharacter;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import handling.world.party.PartyOperation;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import java.io.PrintStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.script.ScriptEngine;
import javax.sql.rowset.serial.SerialBlob;
import server.Timer;
import server.games.BattleReverse;
import server.games.OneCardGame;
import server.maps.MapleMap;
import server.quest.MapleQuest;
import server.shops.IMaplePlayerShop;
import tools.CurrentTime;
import tools.FileoutputUtil;
import tools.MapleAESOFB;
import tools.Pair;
import tools.StringUtil;
import tools.data.LittleEndianAccessor;
import tools.packet.CWvsContext;
import tools.packet.LoginPacket;

public class MapleClient {
   private static final long serialVersionUID = 9179541993413738569L;
   public static final byte LOGIN_NOTLOGGEDIN = 0;
   public static final byte LOGIN_SERVER_TRANSITION = 1;
   public static final byte LOGIN_LOGGEDIN = 2;
   public static final byte CHANGE_CHANNEL = 3;
   public static final int DEFAULT_CHARSLOT = 48;
   public static final String CLIENT_KEY = "CLIENT";
   public static final AttributeKey<MapleClient> CLIENTKEY = AttributeKey.valueOf("mapleclient_netty");
   private transient MapleAESOFB send;
   private transient MapleAESOFB receive;
   public Map<Integer, Integer> mEncryptedOpcode = new LinkedHashMap();
   private transient Channel session;
   private MapleCharacter player;
   private int channel = 1;
   private int accId = -1;
   private int world;
   private int birthday;
   private int unionLevel = 0;
   private int point = 0;
   private int SecondPwUse = 0;
   private int saveOTPNum = 0;
   private int charslots = 48;
   private String discord = "";
   private long createchartime = 0L;
   private boolean loggedIn = false;
   private boolean serverTransition = false;
   private boolean sendOTP = false;
   private transient Calendar tempban = null;
   private String accountName;
   private String pwd;
   private String saveOTPDay;
   private String LIEDETECT;
   private int lieDectctCount;
   private int allowed;
   private transient long lastPong = 0L;
   private transient long lastPing = 0L;
   private boolean monitored = false;
   private boolean receiving = true;
   private boolean auction = false;
   private boolean cashShop = false;
   private boolean farm = false;
   private boolean gm;
   private byte greason = 1;
   private byte gender = -1;
   private byte nameChangeEnable = 0;
   public transient short loginAttempt = 0;
   private transient List<Integer> allowedChar = new LinkedList();
   private transient Set<String> macs = new HashSet();
   private transient Map<String, ScriptEngine> engines = new HashMap();
   private transient ScheduledFuture<?> idleTask = null;
   private transient String secondPassword;
   private transient String tempIP = "";
   private final transient Lock mutex = new ReentrantLock(true);
   private final transient Lock npc_mutex = new ReentrantLock();
   private long lastNpcClick = 0L;
   private long chatBlockedTime = 0L;
   private static final Lock login_mutex = new ReentrantLock(true);
   private final List<Integer> soulMatch = new ArrayList();
   private final Map<Integer, Pair<Short, Short>> charInfo = new LinkedHashMap();
   private boolean firstlogin = true;
   private Map<String, String> keyValues = new HashMap();
   private Map<String, String> keyValues_boss = new HashMap();
   private Map<Integer, List<Pair<String, String>>> customDatas = new HashMap();
   private Map<Integer, String> customKeyValue = new ConcurrentHashMap();
   private List<MapleCabinet> cabinet = new ArrayList();
   private List<MapleShopLimit> shops = new ArrayList();
   private Map<MapleQuest, MapleQuestStatus> quests = new ConcurrentHashMap();

   public MapleClient(Channel session, MapleAESOFB send, MapleAESOFB receive) {
      this.send = send;
      this.receive = receive;
      this.session = session;
   }

   public final MapleAESOFB getReceiveCrypto() {
      return this.receive;
   }

   public final MapleAESOFB getSendCrypto() {
      return this.send;
   }

   public final Channel getSession() {
      return this.session;
   }

   public final Lock getLock() {
      return this.mutex;
   }

   public final Lock getNPCLock() {
      return this.npc_mutex;
   }

   public MapleCharacter getPlayer() {
      return this.player;
   }

   public void setPlayer(MapleCharacter player) {
      this.player = player;
   }

   public void createdChar(int id) {
      this.allowedChar.add(id);
   }

   public final boolean login_Auth(int id) {
      return this.allowedChar.contains(id);
   }

   public boolean canMakeCharacter(int serverId) {
      return this.loadCharactersSize(serverId) < this.getCharacterSlots();
   }

   public final List<MapleCharacter> loadCharacters(int serverId) {
      List<MapleCharacter> chars = new LinkedList();
      Iterator var3 = this.loadCharactersInternal(serverId).iterator();

      while(var3.hasNext()) {
         MapleClient.CharNameAndId cni = (MapleClient.CharNameAndId)var3.next();
         MapleCharacter chr = MapleCharacter.loadCharFromDB(cni.id, this, false);
         chars.add(chr);
         this.charInfo.put(chr.getId(), new Pair(chr.getLevel(), chr.getJob()));
         this.allowedChar.add(chr.getId());
      }

      return chars;
   }

   public List<String> loadCharacterNames(int serverId) {
      List<String> chars = new LinkedList();
      Iterator var3 = this.loadCharactersInternal(serverId).iterator();

      while(var3.hasNext()) {
         MapleClient.CharNameAndId cni = (MapleClient.CharNameAndId)var3.next();
         chars.add(cni.name);
      }

      return chars;
   }

   private List<MapleClient.CharNameAndId> loadCharactersInternal(int serverId) {
      List<MapleClient.CharNameAndId> chars = new LinkedList();
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT id, name, gm FROM characters WHERE accountid = ? AND world = ? ORDER BY `order` ASC");
         ps.setInt(1, this.accId);
         ps.setInt(2, serverId);
         rs = ps.executeQuery();

         while(rs.next()) {
            chars.add(new MapleClient.CharNameAndId(rs.getString("name"), rs.getInt("id")));
            LoginServer.getLoginAuth(rs.getInt("id"));
         }

         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var15) {
         System.err.println("error loading characters internal");
         var15.printStackTrace();
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var14) {
            var14.printStackTrace();
         }

      }

      return chars;
   }

   private int loadCharactersSize(int serverId) {
      int chars = 0;
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT count(*) FROM characters WHERE accountid = ? AND world = ?");
         ps.setInt(1, this.accId);
         ps.setInt(2, serverId);
         rs = ps.executeQuery();
         if (rs.next()) {
            chars = rs.getInt(1);
         }

         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var15) {
         System.err.println("error loading characters internal");
         var15.printStackTrace();
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var14) {
            var14.printStackTrace();
         }

      }

      return chars;
   }

   public boolean isLoggedIn() {
      return this.loggedIn && this.accId >= 0;
   }

   private Calendar getTempBanCalendar(ResultSet rs) throws SQLException {
      Calendar lTempban = Calendar.getInstance();
      if (rs.getLong("tempban") == 0L) {
         lTempban.setTimeInMillis(0L);
         return lTempban;
      } else {
         Calendar today = Calendar.getInstance();
         lTempban.setTimeInMillis(rs.getTimestamp("tempban").getTime());
         if (today.getTimeInMillis() < lTempban.getTimeInMillis()) {
            return lTempban;
         } else {
            lTempban.setTimeInMillis(0L);
            return lTempban;
         }
      }
   }

   public Calendar getTempBanCalendar() {
      return this.tempban;
   }

   public byte getBanReason() {
      return this.greason;
   }

   public void ban() {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE id = ?");
         ps.setString(1, "꺼지세요 ㅋㅋ");
         ps.setInt(2, this.accId);
         ps.executeUpdate();
         ps.close();
      } catch (SQLException var13) {
         System.err.println("Error while banning" + var13);
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var12) {
            var12.printStackTrace();
         }

      }

   }

   public boolean hasBannedIP() {
      boolean ret = false;
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT COUNT(*) FROM ipbans WHERE ? LIKE CONCAT(ip, '%')");
         ps.setString(1, this.getSessionIPAddress());
         rs = ps.executeQuery();
         rs.next();
         if (rs.getInt(1) > 0) {
            ret = true;
         }

         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var14) {
         System.err.println("Error checking ip bans" + var14);
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var13) {
            var13.printStackTrace();
         }

      }

      return ret;
   }

   public boolean hasBannedMac() {
      if (this.macs.isEmpty()) {
         return false;
      } else {
         boolean ret = false;
         int i = false;
         Connection con = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            con = DatabaseConnection.getConnection();
            StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM macbans WHERE mac IN (");

            int i;
            for(i = 0; i < this.macs.size(); ++i) {
               sql.append("?");
               if (i != this.macs.size() - 1) {
                  sql.append(", ");
               }
            }

            sql.append(")");
            ps = con.prepareStatement(sql.toString());
            i = 0;
            Iterator var7 = this.macs.iterator();

            while(var7.hasNext()) {
               String mac = (String)var7.next();
               ++i;
               ps.setString(i, mac);
            }

            rs = ps.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
               ret = true;
            }

            rs.close();
            ps.close();
            con.close();
         } catch (SQLException var17) {
            System.err.println("Error checking mac bans" + var17);
         } finally {
            try {
               if (con != null) {
                  con.close();
               }

               if (ps != null) {
                  ps.close();
               }

               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var16) {
               var16.printStackTrace();
            }

         }

         return ret;
      }
   }

   private void loadMacsIfNescessary() throws SQLException {
      if (this.macs.isEmpty()) {
         Connection con = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT macs FROM accounts WHERE id = ?");
            ps.setInt(1, this.accId);
            rs = ps.executeQuery();
            if (!rs.next()) {
               rs.close();
               ps.close();
               throw new RuntimeException("No valid account associated with this client.");
            }

            if (rs.getString("macs") != null) {
               String[] macData = rs.getString("macs").split(", ");
               String[] var5 = macData;
               int var6 = macData.length;

               for(int var7 = 0; var7 < var6; ++var7) {
                  String mac = var5[var7];
                  if (!mac.equals("")) {
                     this.macs.add(mac);
                  }
               }
            }

            rs.close();
            ps.close();
            con.close();
         } catch (Exception var19) {
            try {
               if (con != null) {
                  con.close();
               }

               if (ps != null) {
                  ps.close();
               }

               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var18) {
               var18.printStackTrace();
            }
         } finally {
            try {
               if (con != null) {
                  con.close();
               }

               if (ps != null) {
                  ps.close();
               }

               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var17) {
               var17.printStackTrace();
            }

         }
      }

   }

   public void banMacs() {
      try {
         this.loadMacsIfNescessary();
         if (this.macs.size() > 0) {
            String[] macBans = new String[this.macs.size()];
            int z = 0;

            for(Iterator var3 = this.macs.iterator(); var3.hasNext(); ++z) {
               String mac = (String)var3.next();
               macBans[z] = mac;
            }

            banMacs(macBans);
         }
      } catch (SQLException var5) {
         var5.printStackTrace();
      }

   }

   public static final void banMacs(String[] macs) {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         List<String> filtered = new LinkedList();
         ps = con.prepareStatement("SELECT filter FROM macfilters");
         rs = ps.executeQuery();

         while(rs.next()) {
            filtered.add(rs.getString("filter"));
         }

         rs.close();
         ps.close();
         ps = con.prepareStatement("INSERT INTO macbans (mac) VALUES (?)");
         String[] var5 = macs;
         int var6 = macs.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            String mac = var5[var7];
            boolean matched = false;
            Iterator var10 = filtered.iterator();

            while(var10.hasNext()) {
               String filter = (String)var10.next();
               if (mac.matches(filter)) {
                  matched = true;
                  break;
               }
            }

            if (!matched) {
               ps.setString(1, mac);

               try {
                  ps.executeUpdate();
               } catch (SQLException var21) {
               }
            }
         }

         ps.close();
      } catch (SQLException var22) {
         System.err.println("Error banning MACs" + var22);
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            var20.printStackTrace();
         }

      }

   }

   public int finishLogin() {
      this.updateLoginState(2, this.getSessionIPAddress());
      return 0;
   }

   public void clearInformation() {
      this.accountName = null;
      this.accId = -1;
      this.secondPassword = null;
      this.gm = false;
      this.loggedIn = false;
      this.greason = 1;
      this.tempban = null;
      this.gender = -1;
   }

   public void SaveQuest(Connection con) {
      PreparedStatement ps = null;
      PreparedStatement pse = null;
      PreparedStatement pse1 = null;
      ResultSet rs = null;

      try {
         if (this.quests != null) {
            pse1 = con.prepareStatement("DELETE FROM acc_queststatus WHERE `accid` = ?");
            pse1.setInt(1, this.accId);
            pse1.executeUpdate();
            pse1.close();
            Iterator var6 = this.quests.values().iterator();

            while(var6.hasNext()) {
               MapleQuestStatus q = (MapleQuestStatus)var6.next();
               ps = con.prepareStatement("INSERT INTO acc_queststatus (`queststatusid`, `accid`, `quest`, `status`, `time`, `forfeited`, `customData`) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)", 1);
               ps.setInt(1, this.accId);
               ps.setInt(2, q.getQuest().getId());
               ps.setInt(3, q.getStatus());
               ps.setInt(4, (int)(q.getCompletionTime() / 1000L));
               ps.setInt(5, q.getForfeited());
               ps.setString(6, q.getCustomData());
               ps.execute();
               rs = ps.getGeneratedKeys();
               if (q.hasMobKills()) {
                  rs.next();
                  Iterator iterator = q.getMobKills().keySet().iterator();

                  while(iterator.hasNext()) {
                     int mob = (Integer)iterator.next();
                     pse = con.prepareStatement("INSERT INTO acc_queststatusmobs VALUES (DEFAULT, ?, ?, ?)");
                     pse.setInt(1, rs.getInt(1));
                     pse.setInt(2, mob);
                     pse.setInt(3, q.getMobKills(mob));
                     pse.execute();
                     pse.close();
                  }
               }

               ps.close();
               rs.close();
            }
         }
      } catch (Exception var18) {
         var18.printStackTrace();
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }

            if (pse != null) {
               pse.close();
            }

            if (pse1 != null) {
               pse1.close();
            }

            if (rs != null) {
               pse.close();
            }
         } catch (Exception var17) {
         }

      }

   }

   public void loadCustomDatas() {
      PreparedStatement ps = null;
      ResultSet rs = null;
      Connection con = null;
      this.customDatas.clear();

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM account_customdata WHERE accid = ?");
         ps.setInt(1, this.accId);

         int id;
         for(rs = ps.executeQuery(); rs.next(); ((List)this.customDatas.get(id)).add(new Pair(rs.getString("key"), rs.getString("value")))) {
            id = rs.getInt("id");
            if (!this.customDatas.containsKey(id)) {
               this.customDatas.put(id, new ArrayList());
            }
         }
      } catch (Exception var21) {
         var21.printStackTrace();
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var20) {
            var20.printStackTrace();
         }

         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            var19.printStackTrace();
         }

         if (con != null) {
            try {
               con.close();
            } catch (SQLException var18) {
               var18.printStackTrace();
            }
         }

      }

   }

   public void loadCustomKeyValue() {
      PreparedStatement ps = null;
      ResultSet rs = null;
      Connection con = null;
      this.customKeyValue.clear();

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM acc_questinfo WHERE accid = ?");
         ps.setInt(1, this.accId);
         rs = ps.executeQuery();

         while(rs.next()) {
            this.customKeyValue.put(rs.getInt("quest"), rs.getString("key"));
         }
      } catch (Exception var21) {
         var21.printStackTrace();
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var20) {
            var20.printStackTrace();
         }

         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            var19.printStackTrace();
         }

         if (con != null) {
            try {
               con.close();
            } catch (SQLException var18) {
               var18.printStackTrace();
            }
         }

      }

   }

   public void loadQuest() {
      PreparedStatement ps = null;
      PreparedStatement pse = null;
      ResultSet rs = null;
      Connection con = null;
      this.quests.clear();

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM acc_queststatus WHERE accid = ?");
         ps.setInt(1, this.accId);
         rs = ps.executeQuery();

         while(rs.next()) {
            int id = rs.getInt("quest");
            MapleQuest q = MapleQuest.getInstance(id);
            byte stat = rs.getByte("status");
            MapleQuestStatus status = new MapleQuestStatus(q, stat);
            long cTime = rs.getLong("time");
            if (cTime > -1L) {
               status.setCompletionTime(cTime * 1000L);
            }

            status.setForfeited(rs.getInt("forfeited"));
            status.setCustomData(rs.getString("customData"));
            this.quests.put(q, status);
            pse = con.prepareStatement("SELECT * FROM acc_queststatusmobs WHERE queststatusid = ?");
            pse.setInt(1, rs.getInt("queststatusid"));
            ResultSet rsMobs = pse.executeQuery();
            if (rsMobs.next()) {
               status.setMobKills(rsMobs.getInt("mob"), rsMobs.getInt("count"));
            }

            pse.close();
            rsMobs.close();
         }
      } catch (Exception var28) {
         var28.printStackTrace();
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var27) {
            var27.printStackTrace();
         }

         try {
            if (rs != null) {
               rs.close();
            }

            if (pse != null) {
               pse.close();
            }
         } catch (SQLException var26) {
            var26.printStackTrace();
         }

         if (con != null) {
            try {
               con.close();
            } catch (SQLException var25) {
               var25.printStackTrace();
            }
         }

      }

   }

   public void loadKeyValues_Boss() {
      PreparedStatement ps = null;
      ResultSet rs = null;
      Connection con = null;
      this.keyValues.clear();

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM acckeyvalue_boss WHERE id = ?");
         ps.setInt(1, this.accId);
         rs = ps.executeQuery();

         while(rs.next()) {
            this.keyValues.put(rs.getString("key"), rs.getString("value"));
         }
      } catch (Exception var21) {
         var21.printStackTrace();
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var20) {
            var20.printStackTrace();
         }

         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            var19.printStackTrace();
         }

         if (con != null) {
            try {
               con.close();
            } catch (SQLException var18) {
               var18.printStackTrace();
            }
         }

      }

   }

   public void loadKeyValues() {
      PreparedStatement ps = null;
      ResultSet rs = null;
      Connection con = null;
      this.keyValues.clear();

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM acckeyvalue WHERE id = ?");
         ps.setInt(1, this.accId);
         rs = ps.executeQuery();

         while(rs.next()) {
            this.keyValues.put(rs.getString("key"), rs.getString("value"));
         }
      } catch (Exception var21) {
         var21.printStackTrace();
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var20) {
            var20.printStackTrace();
         }

         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            var19.printStackTrace();
         }

         if (con != null) {
            try {
               con.close();
            } catch (SQLException var18) {
               var18.printStackTrace();
            }
         }

      }

   }

   public int login(String login, String pwd, String mac, boolean ipMacBanned) {
      boolean ipBan = this.hasBannedIP();
      boolean macBan = this.hasBannedMac();
      return this.login(login, pwd, mac, false, ipBan || macBan);
   }

   public int login(String login, String pwd, String mac, boolean weblogin, boolean ipMacBanned) {
      int loginok = 5;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Connection con = null;

      try {
         try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT * FROM accounts WHERE name = ?");
            ps.setString(1, login);
            rs = ps.executeQuery();
            if (rs.next()) {
               int banned = rs.getInt("banned");
               String passhash = rs.getString("password");
               String oldSession = rs.getString("SessionIP");
               this.accountName = login;
               this.accId = rs.getInt("id");
               this.secondPassword = rs.getString("2ndpassword");
               this.gm = rs.getInt("gm") > 0;
               this.greason = rs.getByte("greason");
               this.tempban = this.getTempBanCalendar(rs);
               this.gender = rs.getByte("gender");
               this.nameChangeEnable = rs.getByte("nameChange");
               this.point = rs.getInt("points");
               this.chatBlockedTime = rs.getLong("chatBlockedTime");
               this.discord = rs.getString("code");
               this.SecondPwUse = rs.getInt("SecondPwUse");
               this.saveOTPNum = rs.getInt("saveOTPNum");
               this.saveOTPDay = rs.getString("saveOTPDay");
               this.allowed = rs.getByte("allowed");
               byte gmc = rs.getByte("gm");
               byte var44;
               if (weblogin) {
                  rs.close();
                  ps.close();
                  con.close();
                  var44 = 5;
                  return var44;
               }

               byte loginok;
               if (ServerConstants.ConnectorSetting && !this.isGm() && !this.getSessionIPAddress().equals("125.184.5.40") && !this.getSessionIPAddress().equals("1.246.243.142") && this.allowed != 1 && gmc < 1) {
                  this.session.write(CWvsContext.serverNotice(1, "", "접속기를 통해 접속해주세요."));
                  rs.close();
                  ps.close();
                  loginok = 20;
                  var44 = loginok;
                  return var44;
               }

               rs.close();
               ps.close();
               if (banned > 0 && !this.gm) {
                  loginok = 3;
               } else {
                  if (banned == -1) {
                     this.unban();
                  }

                  if (!pwd.equals(passhash)) {
                     this.loggedIn = false;
                     loginok = 4;
                     var44 = loginok;
                     return var44;
                  }

                  this.pwd = pwd;
                  loginok = 0;
                  byte loginstate = this.getLoginState();
                  if (loginstate > 0) {
                     this.loggedIn = false;
                     loginok = 7;
                  } else {
                     boolean updatePasswordHash = false;
                     if (passhash == null || passhash.isEmpty()) {
                        if (oldSession != null && !oldSession.isEmpty()) {
                           this.loggedIn = this.getSessionIPAddress().equals(oldSession);
                           loginok = this.loggedIn ? 0 : 4;
                           updatePasswordHash = this.loggedIn;
                        } else {
                           loginok = 4;
                           this.loggedIn = false;
                        }
                     }
                  }
               }
            }

            rs.close();
            ps.close();
            con.close();
         } catch (SQLException var41) {
            System.err.println("ERROR" + var41);
         }

         return loginok;
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var40) {
            var40.printStackTrace();
         }

         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var39) {
            var39.printStackTrace();
         }

         if (con != null) {
            try {
               con.close();
            } catch (SQLException var38) {
               var38.printStackTrace();
            }
         }

      }
   }

   public boolean CheckSecondPassword(String in) {
      boolean allow = false;
      if (in.equals(this.secondPassword)) {
         allow = true;
      }

      return allow;
   }

   private void unban() {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE accounts SET banned = 0, banreason = '' WHERE id = ?");
         ps.setInt(1, this.accId);
         ps.executeUpdate();
         ps.close();
         con.close();
      } catch (SQLException var13) {
         System.err.println("Error while unbanning" + var13);
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var12) {
            var12.printStackTrace();
         }

      }

   }

   public static final byte unban(String charname) {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT accountid from characters where name = ?");
         ps.setString(1, charname);
         rs = ps.executeQuery();
         if (!rs.next()) {
            rs.close();
            ps.close();
            byte var18 = -1;
            return var18;
         }

         int accid = rs.getInt(1);
         rs.close();
         ps.close();
         ps = con.prepareStatement("UPDATE accounts SET banned = 0, banreason = '' WHERE id = ?");
         ps.setInt(1, accid);
         ps.executeUpdate();
         ps.close();
         con.close();
      } catch (SQLException var16) {
         System.err.println("Error while unbanning" + var16);
         byte var5 = -2;
         return var5;
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var15) {
            var15.printStackTrace();
         }

      }

      return 0;
   }

   public void updateMacs(String macData) {
      String[] var2 = macData.split(", ");
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String mac = var2[var4];
         this.macs.add(mac);
      }

      StringBuilder newMacData = new StringBuilder();
      Iterator iter = this.macs.iterator();

      while(iter.hasNext()) {
         newMacData.append((String)iter.next());
         if (iter.hasNext()) {
            newMacData.append(", ");
         }
      }

      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE accounts SET macs = ? WHERE id = ?");
         ps.setString(1, newMacData.toString());
         ps.setInt(2, this.accId);
         ps.executeUpdate();
         ps.close();
         con.close();
      } catch (SQLException var16) {
         System.err.println("Error saving MACs" + var16);
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var15) {
            var15.printStackTrace();
         }

      }

   }

   public void setAccID(int id) {
      this.accId = id;
   }

   public int getAccID() {
      return this.accId;
   }

   public final void updateLoginState(int newstate, String SessionID) {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE accounts SET loggedin = ?, SessionIP = ?, lastlogin = CURRENT_TIMESTAMP() WHERE id = ?");
         ps.setInt(1, newstate);
         ps.setString(2, SessionID);
         ps.setInt(3, this.getAccID());
         ps.executeUpdate();
         ps.close();
         con.close();
      } catch (SQLException var15) {
         System.err.println("error updating login state" + var15);
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var14) {
            var14.printStackTrace();
         }

      }

      if (newstate == 0) {
         this.loggedIn = false;
         this.serverTransition = false;
      } else {
         this.serverTransition = newstate == 1 || newstate == 3;
         this.loggedIn = !this.serverTransition;
      }

   }

   public final void updateLoginState(int newstate, String SessionID, String accname) {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE accounts SET loggedin = ?, SessionIP = ?, lastlogin = CURRENT_TIMESTAMP() WHERE name = ?");
         ps.setInt(1, newstate);
         ps.setString(2, SessionID);
         ps.setString(3, accname);
         ps.executeUpdate();
         ps.close();
         con.close();
      } catch (SQLException var16) {
         System.err.println("error updating login state" + var16);
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var15) {
            var15.printStackTrace();
         }

      }

      if (newstate == 0) {
         this.loggedIn = false;
         this.serverTransition = false;
      } else {
         this.serverTransition = newstate == 1 || newstate == 3;
         this.loggedIn = !this.serverTransition;
      }

   }

   public final void updateSecondPassword() {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE `accounts` SET `2ndpassword` = ? WHERE id = ?");
         ps.setString(1, this.secondPassword);
         ps.setInt(2, this.accId);
         ps.executeUpdate();
         ps.close();
         con.close();
      } catch (SQLException var13) {
         System.err.println("error updating login state" + var13);
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var12) {
            var12.printStackTrace();
         }

      }

   }

   public final byte getLoginState() {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      byte var5;
      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT loggedin, lastlogin FROM accounts WHERE id = ?");
         ps.setInt(1, this.getAccID());
         rs = ps.executeQuery();
         if (!rs.next()) {
            ps.close();
            throw new DatabaseException("Everything sucks ?꾩씠??: " + this.getAccID());
         }

         byte state = rs.getByte("loggedin");
         if ((state == 1 || state == 3) && rs.getTimestamp("lastlogin").getTime() + 20000L < System.currentTimeMillis()) {
            state = 0;
            this.updateLoginState(state, (String)null);
         }

         rs.close();
         ps.close();
         if (state == 2) {
            this.loggedIn = true;
         } else {
            this.loggedIn = false;
         }

         var5 = state;
      } catch (SQLException var14) {
         this.loggedIn = false;
         throw new DatabaseException("error getting login state", var14);
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var13) {
            var13.printStackTrace();
         }

      }

      return var5;
   }

   public final boolean checkBirthDate(int date) {
      return this.birthday == date;
   }

   public final void removalTask(boolean shutdown) {
      try {
         this.player.cancelAllBuffs_();
         this.player.cancelAllDebuffs();
         MapleQuestStatus stat1;
         MapleQuestStatus stat2;
         if (this.player.getMarriageId() > 0) {
            stat1 = this.player.getQuestNoAdd(MapleQuest.getInstance(160001));
            stat2 = this.player.getQuestNoAdd(MapleQuest.getInstance(160002));
            if (stat1 != null && stat1.getCustomData() != null && (stat1.getCustomData().equals("2_") || stat1.getCustomData().equals("2"))) {
               if (stat2 != null && stat2.getCustomData() != null) {
                  stat2.setCustomData("0");
               }

               stat1.setCustomData("3");
            }
         }

         if (this.player.getMapId() == 180000002 && !this.player.isIntern()) {
            stat1 = this.player.getQuestNAdd(MapleQuest.getInstance(123455));
            stat2 = this.player.getQuestNAdd(MapleQuest.getInstance(123456));
            if (stat1.getCustomData() == null) {
               stat1.setCustomData(String.valueOf(System.currentTimeMillis()));
            } else if (stat2.getCustomData() == null) {
               stat2.setCustomData("0");
            } else {
               int seconds = Integer.parseInt(stat2.getCustomData()) - (int)((System.currentTimeMillis() - Long.parseLong(stat1.getCustomData())) / 1000L);
               if (seconds < 0) {
                  seconds = 0;
               }

               stat2.setCustomData(String.valueOf(seconds));
            }
         }

         this.player.changeRemoval(true);
         if (this.player.getEventInstance() != null) {
            this.player.getEventInstance().playerDisconnected(this.player, this.player.getId());
         }

         IMaplePlayerShop shop = this.player.getPlayerShop();
         if (shop != null) {
            shop.removeVisitor(this.player);
            if (shop.isOwner(this.player)) {
               if (shop.getShopType() == 1 && shop.isAvailable() && !shutdown) {
                  shop.setOpen(true);
               } else {
                  shop.closeShop(true, !shutdown);
               }
            }
         }

         this.player.setMessenger((MapleMessenger)null);
         if (this.player.getMap() != null) {
            if (!shutdown && (this.getChannelServer() == null || !this.getChannelServer().isShutdown())) {
               if (this.player.isAlive()) {
                  switch(this.player.getMapId()) {
                  case 220080001:
                  case 541010100:
                  case 541020800:
                     this.player.getMap().addDisconnected(this.player.getId());
                  }
               }
            } else {
               int questID = -1;
               switch(this.player.getMapId()) {
               case 105100300:
               case 105100400:
                  questID = 160106;
                  break;
               case 211070000:
               case 211070100:
               case 211070101:
               case 211070110:
                  questID = 160107;
                  break;
               case 240060200:
                  questID = 160100;
                  break;
               case 240060201:
                  questID = 160103;
                  break;
               case 270050100:
                  questID = 160101;
                  break;
               case 271040100:
                  questID = 160109;
                  break;
               case 280030000:
                  questID = 160101;
                  break;
               case 280030001:
                  questID = 160102;
                  break;
               case 551030200:
                  questID = 160108;
               }

               if (questID > 0) {
                  this.player.getQuestNAdd(MapleQuest.getInstance(questID)).setCustomData("0");
               }
            }

            this.player.getMap().removePlayer(this.player);
         }
      } catch (Throwable var5) {
         FileoutputUtil.outputFileError("Log_AccountStuck.rtf", var5);
      }

   }

   public String getPassword(String login) {
      String password = null;
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM accounts WHERE name = ?");
         ps.setString(1, login);
         rs = ps.executeQuery();
         if (rs.next()) {
            password = rs.getString("password");
         }

         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var15) {
         var15.printStackTrace();
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var14) {
            var14.printStackTrace();
         }

      }

      return password;
   }

   public final void disconnect(boolean RemoveInChannelServer, boolean fromCS) {
      this.disconnect(RemoveInChannelServer, fromCS, false);
   }

   public final void disconnect(boolean RemoveInChannelServer, boolean fromCS, boolean shutdown) {
      if (this.player != null) {
         String var10000 = FileoutputUtil.접속종료로그;
         int var10001 = this.player.getClient().getAccID();
         FileoutputUtil.log(var10000, "[종료] 계정번호 : " + var10001 + " | " + this.player.getName() + "(" + this.player.getId() + ")이 종료.");
         MapleMap map = this.player.getMap();
         MapleParty party = this.player.getParty();
         String namez = this.player.getName();
         int idz = this.player.getId();
         int messengerid = this.player.getMessenger() == null ? 0 : this.player.getMessenger().getId();
         int gid = this.player.getGuildId();
         BuddyList bl = this.player.getBuddylist();
         MaplePartyCharacter chrp = new MaplePartyCharacter(this.player);
         MapleMessengerCharacter chrm = new MapleMessengerCharacter(this.player);
         MapleGuildCharacter chrg = this.player.getMGC();
         this.removalTask(shutdown);
         LoginServer.getLoginAuth(this.player.getId());
         this.player.setLastDisconnectTime(System.currentTimeMillis());
         this.player.saveToDB(true, fromCS);
         if (OneCardGame.oneCardMatchingQueue.contains(this.player)) {
            OneCardGame.oneCardMatchingQueue.remove(this.player);
         }

         if (BattleReverse.BattleReverseMatchingQueue.contains(this.player)) {
            BattleReverse.BattleReverseMatchingQueue.remove(this.player);
         }

         if (this.player.getSecondaryStatEffectTimer() != null) {
            this.player.getSecondaryStatEffectTimer().cancel(true);
         } else {
            System.out.println("NULL TIMER");
         }

         if (shutdown) {
            this.player = null;
            this.receiving = false;
            return;
         }

         PrintStream var34;
         String var35;
         if (!fromCS) {
            ChannelServer ch = ChannelServer.getInstance(map == null ? this.channel : map.getChannel());
            int chz = World.Find.findChannel(idz);
            if (chz < -1) {
               this.disconnect(RemoveInChannelServer, true);
               return;
            }

            try {
               if (chz == -1 || ch == null || ch.isShutdown()) {
                  this.player = null;
                  return;
               }

               if (messengerid > 0) {
                  World.Messenger.leaveMessenger(messengerid, chrm);
               }

               if (party != null) {
                  chrp.setOnline(false);
                  World.Party.updateParty(party.getId(), PartyOperation.LOG_ONOFF, chrp);
                  if (map != null && party.getLeader().getId() == idz) {
                     MaplePartyCharacter lchr = null;
                     Iterator var17 = party.getMembers().iterator();

                     label551:
                     while(true) {
                        MaplePartyCharacter pchr;
                        do {
                           do {
                              do {
                                 if (!var17.hasNext()) {
                                    if (lchr != null) {
                                       World.Party.updateParty(party.getId(), PartyOperation.CHANGE_LEADER_DC, lchr);
                                    }
                                    break label551;
                                 }

                                 pchr = (MaplePartyCharacter)var17.next();
                              } while(pchr == null);
                           } while(map.getCharacterById(pchr.getId()) == null);
                        } while(lchr != null && lchr.getLevel() >= pchr.getLevel());

                        lchr = pchr;
                     }
                  }
               }

               if (bl != null) {
                  if (!this.serverTransition) {
                     World.Buddy.loggedOff(namez, idz, this.channel, this.accId, bl.getBuddyIds());
                  } else {
                     World.Buddy.loggedOn(namez, idz, this.channel, this.accId, bl.getBuddyIds());
                  }
               }

               if (gid > 0 && chrg != null) {
                  World.Guild.setGuildMemberOnline(chrg, false, -1);
               }
            } catch (Exception var31) {
               var31.printStackTrace();
               FileoutputUtil.outputFileError("Log_AccountStuck.rtf", var31);
               var34 = System.err;
               var35 = getLogMessage(this, "ERROR");
               var34.println(var35 + var31);
            } finally {
               if (RemoveInChannelServer && ch != null) {
                  ch.removePlayer(idz, this.accId, namez);
               }

               this.player = null;
            }
         } else {
            int ch = World.Find.findChannel(idz);
            if (ch > 0) {
               this.disconnect(RemoveInChannelServer, false);
               return;
            }

            try {
               if (party != null) {
                  chrp.setOnline(false);
                  World.Party.updateParty(party.getId(), PartyOperation.LOG_ONOFF, chrp);
               }

               if (!this.serverTransition) {
                  World.Buddy.loggedOff(namez, idz, this.channel, this.accId, bl.getBuddyIds());
               } else {
                  World.Buddy.loggedOn(namez, idz, this.channel, this.accId, bl.getBuddyIds());
               }

               if (gid > 0 && chrg != null) {
                  World.Guild.setGuildMemberOnline(chrg, false, -1);
               }

               if (this.player != null) {
                  this.player.setMessenger((MapleMessenger)null);
               }
            } catch (Exception var29) {
               var29.printStackTrace();
               FileoutputUtil.outputFileError("Log_AccountStuck.rtf", var29);
               var34 = System.err;
               var35 = getLogMessage(this, "ERROR");
               var34.println(var35 + var29);
            } finally {
               if (RemoveInChannelServer && ch > 0) {
                  CashShopServer.getPlayerStorage().deregisterPlayer(idz, this.accId, namez);
               }

               this.player = null;
            }
         }
      }

      if (!this.serverTransition && this.isLoggedIn()) {
         this.updateLoginState(0, this.getSessionIPAddress());
      }

      this.engines.clear();
   }

   public final void disconnect(MapleCharacter player, boolean RemoveInChannelServer, boolean fromCS, boolean shutdown) {
      if (player != null) {
         String var10000 = FileoutputUtil.접속종료로그;
         int var10001 = player.getClient().getAccID();
         FileoutputUtil.log(var10000, "[종료] 계정번호 : " + var10001 + " | " + player.getName() + "(" + player.getId() + ")이 종료 2번째에서 처리.");
         MapleMap map = player.getMap();
         MapleParty party = player.getParty();
         String namez = player.getName();
         int idz = player.getId();
         int messengerid = player.getMessenger() == null ? 0 : player.getMessenger().getId();
         int gid = player.getGuildId();
         BuddyList bl = player.getBuddylist();
         MaplePartyCharacter chrp = new MaplePartyCharacter(player);
         MapleMessengerCharacter chrm = new MapleMessengerCharacter(player);
         MapleGuildCharacter chrg = player.getMGC();
         this.removalTask(shutdown);
         LoginServer.getLoginAuth(player.getId());
         player.setLastDisconnectTime(System.currentTimeMillis());
         player.saveToDB(true, fromCS);
         if (OneCardGame.oneCardMatchingQueue.contains(player)) {
            OneCardGame.oneCardMatchingQueue.remove(player);
         }

         if (BattleReverse.BattleReverseMatchingQueue.contains(player)) {
            BattleReverse.BattleReverseMatchingQueue.remove(player);
         }

         if (player.getSecondaryStatEffectTimer() != null) {
            player.getSecondaryStatEffectTimer().cancel(true);
         } else {
            System.out.println("NULL TIMER");
         }

         PrintStream var35;
         String var36;
         if (!fromCS) {
            ChannelServer ch = ChannelServer.getInstance(map == null ? this.channel : map.getChannel());
            int chz = World.Find.findChannel(idz);
            if (chz < -1) {
               this.disconnect(RemoveInChannelServer, true);
               return;
            }

            try {
               if (chz == -1 || ch == null || ch.isShutdown()) {
                  player = null;
                  return;
               }

               if (messengerid > 0) {
                  World.Messenger.leaveMessenger(messengerid, chrm);
               }

               if (party != null) {
                  chrp.setOnline(false);
                  World.Party.updateParty(party.getId(), PartyOperation.LOG_ONOFF, chrp);
                  if (map != null && party.getLeader().getId() == idz) {
                     MaplePartyCharacter lchr = null;
                     Iterator var18 = party.getMembers().iterator();

                     label508:
                     while(true) {
                        MaplePartyCharacter pchr;
                        do {
                           do {
                              do {
                                 if (!var18.hasNext()) {
                                    if (lchr != null) {
                                       World.Party.updateParty(party.getId(), PartyOperation.CHANGE_LEADER_DC, lchr);
                                    }
                                    break label508;
                                 }

                                 pchr = (MaplePartyCharacter)var18.next();
                              } while(pchr == null);
                           } while(map.getCharacterById(pchr.getId()) == null);
                        } while(lchr != null && lchr.getLevel() >= pchr.getLevel());

                        lchr = pchr;
                     }
                  }
               }

               if (bl != null) {
                  if (!this.serverTransition) {
                     World.Buddy.loggedOff(namez, idz, this.channel, this.accId, bl.getBuddyIds());
                  } else {
                     World.Buddy.loggedOn(namez, idz, this.channel, this.accId, bl.getBuddyIds());
                  }
               }

               if (gid > 0 && chrg != null) {
                  World.Guild.setGuildMemberOnline(chrg, false, -1);
               }
            } catch (Exception var32) {
               var32.printStackTrace();
               FileoutputUtil.outputFileError("Log_AccountStuck.rtf", var32);
               var35 = System.err;
               var36 = getLogMessage(this, "ERROR");
               var35.println(var36 + var32);
            } finally {
               if (RemoveInChannelServer && ch != null) {
                  ch.removePlayer(idz, this.accId, namez);
               }

               player = null;
            }
         } else {
            int ch = World.Find.findChannel(idz);

            try {
               if (party != null) {
                  chrp.setOnline(false);
                  World.Party.updateParty(party.getId(), PartyOperation.LOG_ONOFF, chrp);
               }

               if (!this.serverTransition) {
                  World.Buddy.loggedOff(namez, idz, this.channel, this.accId, bl.getBuddyIds());
               } else {
                  World.Buddy.loggedOn(namez, idz, this.channel, this.accId, bl.getBuddyIds());
               }

               if (gid > 0 && chrg != null) {
                  World.Guild.setGuildMemberOnline(chrg, false, -1);
               }

               if (player != null) {
                  player.setMessenger((MapleMessenger)null);
               }
            } catch (Exception var30) {
               var30.printStackTrace();
               FileoutputUtil.outputFileError("Log_AccountStuck.rtf", var30);
               var35 = System.err;
               var36 = getLogMessage(this, "ERROR");
               var35.println(var36 + var30);
            } finally {
               if (RemoveInChannelServer && ch > 0) {
                  CashShopServer.getPlayerStorage().deregisterPlayer(idz, this.accId, namez);
               }

               player = null;
            }
         }
      }

      this.updateLoginState(0, this.getSessionIPAddress());
      this.engines.clear();
   }

   public final String getSessionIPAddress() {
      return this.session.remoteAddress().toString().split(":")[0].split("/")[1];
   }

   public final boolean CheckIPAddress() {
      if (this.accId < 0) {
         return false;
      } else {
         Connection con = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT SessionIP, banned FROM accounts WHERE id = ?");
            ps.setInt(1, this.accId);
            rs = ps.executeQuery();
            boolean canlogin = false;
            if (rs.next()) {
               String sessionIP = rs.getString("SessionIP");
               if (sessionIP != null) {
                  canlogin = this.getSessionIPAddress().equals(sessionIP.split(":")[0]);
               }

               if (rs.getInt("banned") > 0) {
                  canlogin = false;
               }
            }

            rs.close();
            ps.close();
            con.close();
            boolean var17 = canlogin;
            return var17;
         } catch (SQLException var15) {
            System.out.println("Failed in checking IP address for client.");
         } finally {
            try {
               if (con != null) {
                  con.close();
               }

               if (ps != null) {
                  ps.close();
               }

               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var14) {
               var14.printStackTrace();
            }

         }

         return true;
      }
   }

   public final void DebugMessage(StringBuilder sb) {
   }

   public final int getChannel() {
      return this.channel;
   }

   public final ChannelServer getChannelServer() {
      return ChannelServer.getInstance(this.channel);
   }

   public final int deleteCharacter(int cid) {
      Connection con = null;
      PreparedStatement ps = null;
      PreparedStatement ps2 = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT guildid, guildrank, familyid, name FROM characters WHERE id = ? AND accountid = ?");
         ps.setInt(1, cid);
         ps.setInt(2, this.accId);
         rs = ps.executeQuery();
         if (!rs.next()) {
            rs.close();
            ps.close();
            byte var25 = 9;
            return var25;
         }

         String name = rs.getString("name");
         if (rs.getInt("guildid") > 0) {
            if (rs.getInt("guildrank") == 1) {
               rs.close();
               ps.close();
               byte var26 = 10;
               return var26;
            }

            World.Guild.deleteGuildCharacter(rs.getInt("guildid"), cid);
         }

         rs.close();
         ps.close();
         ps2 = con.prepareStatement("SELECT email, macs FROM accounts WHERE id = ?");
         ps2.setInt(1, this.accId);
         ResultSet rs2 = ps2.executeQuery();
         if (!rs2.next()) {
            ps2.close();
            rs2.close();
         }

         String email = rs2.getString("email");
         String macs = rs2.getString("macs");
         ps2.close();
         rs2.close();
         String data = "캐릭터명 : " + name + " (AccountID : " + this.accId + ", AccountName : " + this.getAccountName() + ") 이 삭제됨.\r\n";
         data.makeConcatWithConstants<invokedynamic>(data, email, macs);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM characters WHERE id = ?", cid);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM hiredmerch WHERE characterid = ?", cid);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM mountdata WHERE characterid = ?", cid);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM inventoryitems WHERE characterid = ?", cid);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM inventoryitemsuse WHERE characterid = ?", cid);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM inventoryitemssetup WHERE characterid = ?", cid);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM inventoryitemsetc WHERE characterid = ?", cid);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM inventoryitemscash WHERE characterid = ?", cid);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM inventoryitemscody WHERE characterid = ?", cid);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM famelog WHERE characterid = ?", cid);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM famelog WHERE characterid_to = ?", cid);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM dueypackages WHERE RecieverId = ?", cid);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM wishlist WHERE characterid = ?", cid);
         ps = con.prepareStatement("DELETE FROM buddies WHERE repname = ?");
         ps.setString(1, name);
         ps.executeUpdate();
         ps.close();
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM keymap WHERE characterid = ?", cid);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM trocklocations WHERE characterid = ?", cid);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM regrocklocations WHERE characterid = ?", cid);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM hyperrocklocations WHERE characterid = ?", cid);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?", cid);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM skills WHERE characterid = ?", cid);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM mountdata WHERE characterid = ?", cid);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM skillmacros WHERE characterid = ?", cid);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM trocklocations WHERE characterid = ?", cid);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM questinfo WHERE characterid = ?", cid);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM queststatus WHERE characterid = ?", cid);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM inventoryslot WHERE characterid = ?", cid);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM extendedSlots WHERE characterid = ?", cid);
         MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM `unions` WHERE id = ?", cid);
         con.close();
         byte var11 = 0;
         return var11;
      } catch (Exception var23) {
         FileoutputUtil.outputFileError("Log_Packet_Except.rtf", var23);
         var23.printStackTrace();
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (ps2 != null) {
               ps2.close();
            }

            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var22) {
            var22.printStackTrace();
         }

      }

      return 10;
   }

   public final byte getGender() {
      return this.gender;
   }

   public final void setGender(byte gender) {
      this.gender = gender;
   }

   public final String getSecondPassword() {
      return this.secondPassword;
   }

   public final void setSecondPassword(String secondPassword) {
      this.secondPassword = secondPassword;
   }

   public final String getAccountName() {
      return this.accountName;
   }

   public final void setAccountName(String accountName) {
      this.accountName = accountName;
   }

   public final String getPassword() {
      return this.pwd;
   }

   public final void setPassword(String pwd) {
      this.pwd = pwd;
   }

   public final void setChannel(int channel) {
      this.channel = channel;
   }

   public final int getWorld() {
      return this.world;
   }

   public final void setWorld(int world) {
      this.world = world;
   }

   public final int getLatency() {
      return (int)(this.lastPong - this.lastPing);
   }

   public final long getLastPong() {
      return this.lastPong;
   }

   public final long getLastPing() {
      return this.lastPing;
   }

   public final void pongReceived() {
      this.lastPong = System.currentTimeMillis();
   }

   public final void sendPing() {
      this.lastPing = System.currentTimeMillis();
      this.session.writeAndFlush(LoginPacket.getPing());
      Timer.PingTimer.getInstance().schedule(new Runnable() {
         public void run() {
            try {
               if (MapleClient.this.getLatency() < 0) {
                  MapleClient.this.disconnect(true, false);
                  MapleClient.this.getSession().close();
               }
            } catch (NullPointerException var2) {
            }

         }
      }, 60000L);
   }

   public static final String getLogMessage(MapleClient cfor, String message) {
      return getLogMessage(cfor, message);
   }

   public static final String getLogMessage(MapleCharacter cfor, String message) {
      return getLogMessage(cfor == null ? null : cfor.getClient(), message);
   }

   public static final String getLogMessage(MapleCharacter cfor, String message, Object... parms) {
      return getLogMessage(cfor == null ? null : cfor.getClient(), message, parms);
   }

   public static final String getLogMessage(MapleClient cfor, String message, Object... parms) {
      StringBuilder builder = new StringBuilder();
      if (cfor != null) {
         if (cfor.getPlayer() != null) {
            builder.append("<");
            builder.append(MapleCharacterUtil.makeMapleReadable(cfor.getPlayer().getName()));
            builder.append(" (cid: ");
            builder.append(cfor.getPlayer().getId());
            builder.append(")> ");
         }

         if (cfor.getAccountName() != null) {
            builder.append("(Account: ");
            builder.append(cfor.getAccountName());
            builder.append(") ");
         }
      }

      builder.append(message);
      Object[] var4 = parms;
      int var5 = parms.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         Object parm = var4[var6];
         int start = builder.indexOf("{}");
         builder.replace(start, start + 2, parm.toString());
      }

      return builder.toString();
   }

   public static final int findAccIdForCharacterName(String charName) {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
         ps.setString(1, charName);
         rs = ps.executeQuery();
         int ret = -1;
         if (rs.next()) {
            ret = rs.getInt("accountid");
         }

         rs.close();
         ps.close();
         con.close();
         int var5 = ret;
         return var5;
      } catch (SQLException var15) {
         System.err.println("findAccIdForCharacterName SQL error");
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var14) {
            var14.printStackTrace();
         }

      }

      return -1;
   }

   public final Set<String> getMacs() {
      return Collections.unmodifiableSet(this.macs);
   }

   public final boolean isGm() {
      return this.gm;
   }

   public final void setScriptEngine(String name, ScriptEngine e) {
      this.engines.put(name, e);
   }

   public final ScriptEngine getScriptEngine(String name) {
      return (ScriptEngine)this.engines.get(name);
   }

   public final void removeScriptEngine(String name) {
      this.engines.remove(name);
   }

   public final ScheduledFuture<?> getIdleTask() {
      return this.idleTask;
   }

   public final void setIdleTask(ScheduledFuture<?> idleTask) {
      this.idleTask = idleTask;
   }

   public boolean isFirstLogin() {
      return this.firstlogin;
   }

   public void setLogin(boolean a) {
      this.firstlogin = a;
   }

   public int getCharacterSlots() {
      if (this.charslots != 30) {
         return this.charslots;
      } else {
         Connection con = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT * FROM character_slots WHERE accid = ? AND worldid = ?");
            ps.setInt(1, this.accId);
            ps.setInt(2, this.world);
            rs = ps.executeQuery();
            if (rs.next()) {
               this.charslots = rs.getInt("charslots");
            } else {
               PreparedStatement psu = con.prepareStatement("INSERT INTO character_slots (accid, worldid, charslots) VALUES (?, ?, ?)");
               psu.setInt(1, this.accId);
               psu.setInt(2, this.world);
               psu.setInt(3, this.charslots);
               psu.executeUpdate();
               psu.close();
            }

            rs.close();
            ps.close();
            con.close();
         } catch (SQLException var13) {
            var13.printStackTrace();
         } finally {
            try {
               if (con != null) {
                  con.close();
               }

               if (ps != null) {
                  ps.close();
               }

               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var12) {
               var12.printStackTrace();
            }

         }

         return this.charslots;
      }
   }

   public boolean gainCharacterSlot() {
      if (this.getCharacterSlots() > 48) {
         return false;
      } else {
         ++this.charslots;
         Connection con = null;
         PreparedStatement ps = null;
         Object rs = null;

         boolean var5;
         try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("UPDATE character_slots SET charslots = ? WHERE worldid = ? AND accid = ?");
            ps.setInt(1, this.charslots);
            ps.setInt(2, this.world);
            ps.setInt(3, this.accId);
            ps.executeUpdate();
            ps.close();
            con.close();
            return true;
         } catch (SQLException var15) {
            var15.printStackTrace();
            var5 = false;
         } finally {
            try {
               if (con != null) {
                  con.close();
               }

               if (ps != null) {
                  ps.close();
               }

               if (rs != null) {
                  ((ResultSet)rs).close();
               }
            } catch (SQLException var14) {
               var14.printStackTrace();
            }

         }

         return var5;
      }
   }

   public static final byte unbanIPMacs(String charname) {
      Connection con = null;
      PreparedStatement ps = null;
      PreparedStatement psa = null;
      ResultSet rs = null;

      byte var6;
      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT accountid from characters where name = ?");
         ps.setString(1, charname);
         rs = ps.executeQuery();
         if (!rs.next()) {
            rs.close();
            ps.close();
            byte var26 = -1;
            return var26;
         }

         int accid = rs.getInt(1);
         rs.close();
         ps.close();
         ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
         ps.setInt(1, accid);
         rs = ps.executeQuery();
         if (rs.next()) {
            String sessionIP = rs.getString("sessionIP");
            String macs = rs.getString("macs");
            rs.close();
            ps.close();
            byte ret = 0;
            if (sessionIP != null) {
               psa = con.prepareStatement("DELETE FROM ipbans WHERE ip like ?");
               psa.setString(1, sessionIP);
               psa.execute();
               psa.close();
               ++ret;
            }

            if (macs != null) {
               String[] macz = macs.split(", ");
               String[] var10 = macz;
               int var11 = macz.length;
               int var12 = 0;

               while(true) {
                  if (var12 >= var11) {
                     ++ret;
                     break;
                  }

                  String mac = var10[var12];
                  if (!mac.equals("")) {
                     psa = con.prepareStatement("DELETE FROM macbans WHERE mac = ?");
                     psa.setString(1, mac);
                     psa.execute();
                     psa.close();
                  }

                  ++var12;
               }
            }

            con.close();
            byte var28 = ret;
            return var28;
         }

         rs.close();
         ps.close();
         var6 = -1;
         return var6;
      } catch (SQLException var24) {
         System.err.println("Error while unbanning" + var24);
         var6 = -2;
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (psa != null) {
               psa.close();
            }

            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var23) {
            var23.printStackTrace();
         }

      }

      return var6;
   }

   public static final byte unHellban(String charname) {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      byte var5;
      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT accountid from characters where name = ?");
         ps.setString(1, charname);
         rs = ps.executeQuery();
         if (rs.next()) {
            int accid = rs.getInt(1);
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
            ps.setInt(1, accid);
            rs = ps.executeQuery();
            if (!rs.next()) {
               rs.close();
               ps.close();
               var5 = -1;
               return var5;
            }

            String sessionIP = rs.getString("sessionIP");
            String email = rs.getString("email");
            rs.close();
            ps.close();
            ps = con.prepareStatement("UPDATE accounts SET banned = 0, banreason = '' WHERE email = ?" + (sessionIP == null ? "" : " OR sessionIP = ?"));
            ps.setString(1, email);
            if (sessionIP != null) {
               ps.setString(2, sessionIP);
            }

            ps.execute();
            ps.close();
            con.close();
            byte var7 = 0;
            return var7;
         }

         rs.close();
         ps.close();
         byte var4 = -1;
         return var4;
      } catch (SQLException var19) {
         System.err.println("Error while unbanning" + var19);
         var5 = -2;
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var18) {
            var18.printStackTrace();
         }

      }

      return var5;
   }

   public boolean isMonitored() {
      return this.monitored;
   }

   public void setMonitored(boolean m) {
      this.monitored = m;
   }

   public boolean isReceiving() {
      return this.receiving;
   }

   public void setReceiving(boolean m) {
      this.receiving = m;
   }

   public boolean canClickNPC() {
      return this.lastNpcClick + 500L < System.currentTimeMillis();
   }

   public void setClickedNPC() {
      this.lastNpcClick = System.currentTimeMillis();
   }

   public void removeClickedNPC() {
      this.lastNpcClick = 0L;
   }

   public final Timestamp getCreated() {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Timestamp ret;
      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT createdat FROM accounts WHERE id = ?");
         ps.setInt(1, this.getAccID());
         rs = ps.executeQuery();
         if (rs.next()) {
            ret = rs.getTimestamp("createdat");
            rs.close();
            ps.close();
            con.close();
            Timestamp var5 = ret;
            return var5;
         }

         rs.close();
         ps.close();
         ret = null;
      } catch (SQLException var15) {
         throw new DatabaseException("error getting create", var15);
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var14) {
            var14.printStackTrace();
         }

      }

      return ret;
   }

   public String getTempIP() {
      return this.tempIP;
   }

   public void setTempIP(String s) {
      this.tempIP = s;
   }

   public final byte getNameChangeEnable() {
      return this.nameChangeEnable;
   }

   public final void setNameChangeEnable(byte nickName) {
      this.nameChangeEnable = nickName;
   }

   public boolean isLocalhost() {
      return ServerConstants.Use_Localhost;
   }

   public boolean isAuction() {
      return this.auction;
   }

   public void setAuction(boolean auction) {
      this.auction = auction;
   }

   public int getPoint() {
      return this.point;
   }

   public void setPoint(int point) {
      this.point = point;
   }

   public void order(LittleEndianAccessor slea) {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         int accId = slea.readInt();
         if (accId != this.accId) {
            this.session.close();
         }

         slea.readByte();
         int size = slea.readInt();

         for(int i = 0; i < size; ++i) {
            int id = slea.readInt();

            try {
               con = DatabaseConnection.getConnection();
               ps = con.prepareStatement("UPDATE characters SET `order` = ? WHERE `id` = ?");
               ps.setInt(1, i);
               ps.setInt(2, id);
               ps.executeUpdate();
               ps.close();
            } catch (SQLException var23) {
               var23.printStackTrace();
            }
         }

         try {
            if (con != null) {
               con.close();
            }
         } catch (SQLException var22) {
            var22.printStackTrace();
         }
      } catch (Exception var24) {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var21) {
            var21.printStackTrace();
         }
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var20) {
            var20.printStackTrace();
         }

      }

   }

   public Map<String, String> getKeyValues_Boss() {
      return this.keyValues_boss;
   }

   public void setKeyValues_Boss(Map<String, String> keyValues) {
      this.keyValues_boss = keyValues;
   }

   public String getKeyValue_Boss(String key) {
      return this.keyValues_boss.containsKey(key) ? (String)this.keyValues_boss.get(key) : null;
   }

   public void setKeyValue_Boss(String key, String value) {
      this.keyValues_boss.put(key, value);
   }

   public void removeKeyValue_Boss(String key) {
      this.keyValues_boss.remove(key);
   }

   public void saveKeyValue_BossToDB(Connection con) {
      try {
         if (this.keyValues_boss != null) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM acckeyvalue_boss WHERE `id` = ?");
            ps.setInt(1, this.accId);
            ps.executeUpdate();
            ps.close();
            PreparedStatement pse = con.prepareStatement("INSERT INTO acckeyvalue_boss (`id`, `key`, `value`) VALUES (?, ?, ?)");
            pse.setInt(1, this.accId);
            Iterator var4 = this.keyValues_boss.entrySet().iterator();

            while(var4.hasNext()) {
               Entry<String, String> keyValue = (Entry)var4.next();
               pse.setString(2, (String)keyValue.getKey());
               pse.setString(3, (String)keyValue.getValue());
               pse.execute();
            }

            pse.close();
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }

   }

   public Map<String, String> getKeyValues() {
      return this.keyValues;
   }

   public void setKeyValues(Map<String, String> keyValues) {
      this.keyValues = keyValues;
   }

   public void saveKeyValueToDB(Connection con) {
      try {
         if (this.keyValues != null) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM acckeyvalue WHERE `id` = ?");
            ps.setInt(1, this.accId);
            ps.executeUpdate();
            ps.close();
            PreparedStatement pse = con.prepareStatement("INSERT INTO acckeyvalue (`id`, `key`, `value`) VALUES (?, ?, ?)");
            pse.setInt(1, this.accId);
            Iterator var4 = this.keyValues.entrySet().iterator();

            while(var4.hasNext()) {
               Entry<String, String> keyValue = (Entry)var4.next();
               pse.setString(2, (String)keyValue.getKey());
               pse.setString(3, (String)keyValue.getValue());
               pse.execute();
            }

            pse.close();
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }

   }

   public void saveCustomDataToDB(Connection con) {
      PreparedStatement ps = null;
      PreparedStatement pse = null;

      try {
         if (this.customDatas != null) {
            ps = con.prepareStatement("DELETE FROM account_customdata WHERE `accid` = ?");
            ps.setInt(1, this.accId);
            ps.executeUpdate();
            ps.close();
            pse = con.prepareStatement("INSERT INTO account_customdata (`accid`, `id`, `key`, `value`) VALUES (?, ?, ?, ?)");
            pse.setInt(1, this.accId);
            Iterator var4 = this.customDatas.entrySet().iterator();

            while(var4.hasNext()) {
               Entry<Integer, List<Pair<String, String>>> customData = (Entry)var4.next();
               pse.setInt(2, (Integer)customData.getKey());
               Iterator var6 = ((List)customData.getValue()).iterator();

               while(var6.hasNext()) {
                  Pair<String, String> data = (Pair)var6.next();
                  pse.setString(3, (String)data.left);
                  pse.setString(4, (String)data.right);
                  pse.execute();
               }
            }

            pse.close();
         }
      } catch (Exception var16) {
         var16.printStackTrace();
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }

            if (pse != null) {
               pse.close();
            }
         } catch (Exception var15) {
         }

      }

   }

   public void saveCustomKeyValueToDB(Connection con) {
      PreparedStatement ps = null;
      PreparedStatement pse = null;

      try {
         if (this.customKeyValue != null) {
            ps = con.prepareStatement("DELETE FROM acc_questinfo WHERE `accid` = ?");
            ps.setInt(1, this.accId);
            ps.executeUpdate();
            ps.close();
            pse = con.prepareStatement("INSERT INTO acc_questinfo (`accid`, `quest`, `key`) VALUES (?, ?, ?)");
            pse.setInt(1, this.accId);
            Iterator var4 = this.customKeyValue.entrySet().iterator();

            while(var4.hasNext()) {
               Entry<Integer, String> q = (Entry)var4.next();
               pse.setInt(2, (Integer)q.getKey());
               pse.setString(3, (String)q.getValue());
               pse.execute();
            }

            pse.close();
         }
      } catch (Exception var14) {
         var14.printStackTrace();
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }

            if (pse != null) {
               pse.close();
            }
         } catch (Exception var13) {
         }

      }

   }

   public String getKeyValue(String key) {
      return this.keyValues.containsKey(key) ? (String)this.keyValues.get(key) : null;
   }

   public void setKeyValue(String key, String value) {
      this.keyValues.put(key, value);
   }

   public void removeKeyValue(String key) {
      this.keyValues.remove(key);
   }

   public Map<Integer, List<Pair<String, String>>> getCustomDatas() {
      return this.customDatas;
   }

   public String getCustomData(int id, String key) {
      if (this.customDatas.containsKey(id)) {
         Iterator var3 = ((List)this.customDatas.get(id)).iterator();

         while(var3.hasNext()) {
            Pair<String, String> datas = (Pair)var3.next();
            if (((String)datas.left).equals(key)) {
               return (String)datas.right;
            }
         }
      }

      return null;
   }

   public void setCustomData(int id, String key, String value) {
      if (this.customDatas.containsKey(id)) {
         Iterator var4 = ((List)this.customDatas.get(id)).iterator();

         while(var4.hasNext()) {
            Pair<String, String> datas = (Pair)var4.next();
            if (((String)datas.getLeft()).equals(key)) {
               datas.right = value;
               this.session.writeAndFlush(CWvsContext.InfoPacket.updateClientInfoQuest(id, key + "=" + value));
               this.session.writeAndFlush(CWvsContext.InfoPacket.updateInfoQuest(id, key + "=" + value));
               return;
            }
         }

         ((List)this.customDatas.get(id)).add(new Pair(key, value));
      } else {
         List<Pair<String, String>> datas = new ArrayList();
         datas.add(new Pair(key, value));
         this.customDatas.put(id, datas);
      }

      this.session.writeAndFlush(CWvsContext.InfoPacket.updateClientInfoQuest(id, key + "=" + value));
      this.session.writeAndFlush(CWvsContext.InfoPacket.updateInfoQuest(id, key + "=" + value));
   }

   public long getChatBlockedTime() {
      return this.chatBlockedTime;
   }

   public void setChatBlockedTime(long chatBlockedTime) {
      this.chatBlockedTime = chatBlockedTime;
   }

   public boolean isFarm() {
      return this.farm;
   }

   public void setFarm(boolean farm) {
      this.farm = farm;
   }

   public boolean isCashShop() {
      return this.cashShop;
   }

   public void setCashShop(boolean shop) {
      this.cashShop = shop;
   }

   public byte[] getFarmImg() {
      byte[] farmImg = new byte[0];
      PreparedStatement ps = null;
      ResultSet rs = null;
      Connection con = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM accounts WHERE name = ?");
         ps.setString(1, this.accountName);
         rs = ps.executeQuery();
         if (rs.next()) {
            Blob img = rs.getBlob("farmImg");
            if (img != null) {
               farmImg = img.getBytes(1L, (int)img.length());
            }
         }
      } catch (SQLException var22) {
         var22.printStackTrace();
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            var21.printStackTrace();
         }

         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            var20.printStackTrace();
         }

         if (con != null) {
            try {
               con.close();
            } catch (SQLException var19) {
               var19.printStackTrace();
            }
         }

      }

      return farmImg;
   }

   public final void setFarmImg(byte[] farmImg) {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         Blob blob = null;
         if (farmImg != null) {
            blob = new SerialBlob(farmImg);
         }

         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE accounts SET farmImg = ? WHERE `id` = ?");
         ps.setBlob(1, blob);
         ps.setInt(2, this.accId);
         ps.execute();
         ps.close();
         con.close();
      } catch (SQLException var14) {
         var14.printStackTrace();
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var13) {
            var13.printStackTrace();
         }

      }

   }

   public final boolean isAllowedClient() {
      return this.accountName == null ? false : false;
   }

   public int allChargePoint() {
      try {
         return Integer.parseInt(this.getCustomData(5, "amount"));
      } catch (Exception var2) {
         return 0;
      }
   }

   public int chargePoint() {
      int charge = 0;
      int month = CurrentTime.getMonth() + 1;
      int year = CurrentTime.getYear();

      for(int i = 0; i < 3; ++i) {
         --month;
         if (month <= 0) {
            month += 12;
            --year;
         }

         String var10000 = String.valueOf(year);
         String str = var10000 + StringUtil.getLeftPaddedStr(String.valueOf(month), '0', 2);
         String chargeMonth = this.getCustomData(4, str);
         if (chargeMonth != null) {
            charge += Integer.parseInt(chargeMonth);
         }
      }

      return charge;
   }

   public int getMVPGrade() {
      int totalAmount = this.allChargePoint();
      int lastThreeMonth = this.chargePoint();
      if (lastThreeMonth >= 1500000) {
         return 8;
      } else if (lastThreeMonth >= 900000) {
         return 7;
      } else if (lastThreeMonth >= 600000) {
         return 6;
      } else if (lastThreeMonth >= 300000) {
         return 5;
      } else if (totalAmount >= 300000) {
         return 4;
      } else if (totalAmount >= 200000) {
         return 3;
      } else if (totalAmount < 100000) {
         return totalAmount >= 10000 ? 1 : 0;
      } else {
         return 2;
      }
   }

   public void send(byte[] p) {
      this.getSession().writeAndFlush(p);
   }

   public String getDiscord() {
      return this.discord;
   }

   public int getAllowed() {
      return this.allowed;
   }

   public int getSecondPw() {
      return this.SecondPwUse;
   }

   public void setSecondPw(int SecondPwUse) {
      this.SecondPwUse = SecondPwUse;
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE accounts SET SecondPwUse = ? WHERE id = ?");
         ps.setInt(1, this.SecondPwUse);
         ps.setInt(2, this.getAccID());
         ps.execute();
         ps.close();
         con.close();
      } catch (SQLException var13) {
         throw new DatabaseException("error", var13);
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var12) {
            var12.printStackTrace();
         }

      }

   }

   public void setCheckOTP(boolean a) {
      this.sendOTP = a;
   }

   public boolean getCheckOTP() {
      return this.sendOTP;
   }

   public int getSaveOTPNum() {
      return this.saveOTPNum;
   }

   public void setSaveOTPNum(int saveOTPNum) {
      this.saveOTPNum = saveOTPNum;
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE accounts SET saveOTPNum = ? WHERE id = ?");
         ps.setInt(1, this.saveOTPNum);
         ps.setInt(2, this.getAccID());
         ps.execute();
         ps.close();
         con.close();
      } catch (SQLException var13) {
         throw new DatabaseException("error", var13);
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var12) {
            var12.printStackTrace();
         }

      }

   }

   public String getEmail() {
      String email = "";
      PreparedStatement ps = null;
      ResultSet rs = null;
      Connection con = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM accounts WHERE name = ?");
         ps.setString(1, this.accountName);
         rs = ps.executeQuery();
         if (rs.next()) {
            email = rs.getString("email");
         }
      } catch (SQLException var22) {
         var22.printStackTrace();
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            var21.printStackTrace();
         }

         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            var20.printStackTrace();
         }

         if (con != null) {
            try {
               con.close();
            } catch (SQLException var19) {
               var19.printStackTrace();
            }
         }

      }

      return email;
   }

   public String getLIEDETECT() {
      return this.LIEDETECT;
   }

   public void setLIEDETECT(String LIEDETECT) {
      this.LIEDETECT = LIEDETECT;
   }

   public int getLieDectctCount() {
      return this.lieDectctCount;
   }

   public void setLieDectctCount(int lieDectctCount) {
      this.lieDectctCount = lieDectctCount;
   }

   public MapleCharacter getRandomCharacter() {
      MapleCharacter chr = null;
      List<MapleCharacter> players = new ArrayList();
      if (this.getPlayer().getMap().getCharacters().size() <= 0) {
         return null;
      } else {
         players.addAll(this.getPlayer().getMap().getCharacters());
         Collections.addAll(players, new MapleCharacter[0]);
         Collections.shuffle(players);
         Iterator var3 = players.iterator();

         while(var3.hasNext()) {
            MapleCharacter chr3 = (MapleCharacter)var3.next();
            if (chr3.isAlive()) {
               chr = chr3;
               break;
            }
         }

         return chr;
      }
   }

   public void loadCabinet() {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM cabinet WHERE accountid = ?");
         ps.setInt(1, this.accId);
         rs = ps.executeQuery();

         while(rs.next()) {
            MapleCabinet cb = new MapleCabinet(rs.getInt("accountid"), rs.getInt("itemid"), rs.getInt("count"), rs.getString("bigname"), rs.getString("smallname"), rs.getLong("savetime"), rs.getInt("delete"));
            cb.setPlayerid(rs.getInt("playerid"));
            cb.setName(rs.getString("name"));
            this.getCabiNet().add(cb);
         }

         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var13) {
         var13.printStackTrace();
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }

            if (con != null) {
               con.close();
            }
         } catch (Exception var12) {
         }

      }

   }

   public List<MapleCabinet> getCabiNet() {
      return this.cabinet;
   }

   public void setCabiNet(List<MapleCabinet> cabinet) {
      this.cabinet = cabinet;
   }

   public void saveCabiNet(List<MapleCabinet> cabinet) {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         if (this.getPlayer() != null) {
            if (cabinet.isEmpty()) {
               MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM `cabinet` WHERE `accountid` = ?", this.accId);
               return;
            }

            MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM `cabinet` WHERE `accountid` = ?", this.accId);
            ps = null;
            Iterator var5 = cabinet.iterator();

            while(var5.hasNext()) {
               MapleCabinet cn = (MapleCabinet)var5.next();
               if (cn != null) {
                  ps = con.prepareStatement("INSERT INTO `cabinet` (`accountid`, `itemid`, `count`, `bigname`, `smallname`, `savetime`, `delete`, `playerid`, `name`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
                  ps.setLong(1, (long)this.accId);
                  ps.setInt(2, cn.getItemid());
                  ps.setInt(3, cn.getCount());
                  ps.setString(4, cn.getBigname());
                  ps.setString(5, cn.getSmallname());
                  ps.setLong(6, cn.getSaveTime());
                  ps.setInt(7, cn.getDelete());
                  ps.setInt(8, cn.getPlayerid());
                  ps.setString(9, cn.getName());
                  ps.execute();
                  ps.close();
               }
            }

            con.close();
            return;
         }
      } catch (SQLException var17) {
         var17.printStackTrace();
         return;
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }

            if (con != null) {
               con.close();
            }
         } catch (Exception var16) {
         }

      }

   }

   public void loadShopLimit() {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM shopitemlimit WHERE accountid = ?");
         ps.setInt(1, this.accId);
         rs = ps.executeQuery();

         while(rs.next()) {
            MapleShopLimit shops = new MapleShopLimit(rs.getInt("accountid"), rs.getInt("charid"), rs.getInt("shopid"), rs.getInt("itemid"), rs.getInt("position"), rs.getInt("limitcountacc"), rs.getInt("limitcountchr"), rs.getInt("lastbuymonth"), rs.getInt("lastbuyday"));
            this.getShopLimit().add(shops);
         }

         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var13) {
         var13.printStackTrace();
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }

            if (con != null) {
               con.close();
            }
         } catch (Exception var12) {
         }

      }

   }

   public List<MapleShopLimit> getShopLimit() {
      return this.shops;
   }

   public void setShopLimit(List<MapleShopLimit> shops) {
      this.shops = shops;
   }

   public void saveShopLimit(List<MapleShopLimit> shoplimit) {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         if (this.getPlayer() != null) {
            if (this.shops.isEmpty()) {
               return;
            }

            MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM `shopitemlimit` WHERE `accountid` = ?", this.accId);
            ps = null;
            Iterator var5 = shoplimit.iterator();

            while(var5.hasNext()) {
               MapleShopLimit sl = (MapleShopLimit)var5.next();
               if (sl != null) {
                  ps = con.prepareStatement("INSERT INTO `shopitemlimit` (`accountid`, `charid`, `shopid`, `itemid`, `position`, `limitcountacc`, `limitcountchr`, `lastbuymonth`, `lastbuyday`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
                  ps.setLong(1, (long)this.accId);
                  ps.setInt(2, sl.getCharId());
                  ps.setInt(3, sl.getShopId());
                  ps.setInt(4, sl.getItemid());
                  ps.setInt(5, sl.getPosition());
                  ps.setInt(6, sl.getLimitCountAcc());
                  ps.setInt(7, sl.getLimitCountChr());
                  ps.setInt(8, sl.getLastBuyMonth());
                  ps.setInt(9, sl.getLastBuyDay());
                  ps.execute();
                  ps.close();
               }
            }

            con.close();
            return;
         }
      } catch (SQLException var17) {
         var17.printStackTrace();
         return;
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }

            if (con != null) {
               con.close();
            }
         } catch (Exception var16) {
         }

      }

   }

   public String getSaveOTPDay() {
      return this.saveOTPDay;
   }

   public void setSaveOTPDay(String saveOTPDay) {
      this.saveOTPDay = saveOTPDay;
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE accounts SET saveOTPDay = ? WHERE id = ?");
         ps.setString(1, this.saveOTPDay);
         ps.setInt(2, this.getAccID());
         ps.execute();
         ps.close();
         con.close();
      } catch (SQLException var13) {
         throw new DatabaseException("error", var13);
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var12) {
            var12.printStackTrace();
         }

      }

   }

   public void setCharCreatetime(long time) {
      this.createchartime = time;
      Connection con = null;
      PreparedStatement ps = null;
      PreparedStatement ps2 = null;
      ResultSet rs1 = null;

      try {
         con = DatabaseConnection.getConnection();
         ps2 = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
         ps2.setInt(1, this.getAccID());
         rs1 = ps2.executeQuery();
         ps = con.prepareStatement("UPDATE accounts SET createTime = ? WHERE id = ?");
         ps.setLong(1, this.createchartime);
         ps.setInt(2, this.getAccID());
         ps.executeUpdate();
         ps.close();
         rs1.close();
         ps2.close();
         con.close();
      } catch (SQLException var16) {
         var16.printStackTrace();
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (ps2 != null) {
               ps2.close();
            }

            if (rs1 != null) {
               rs1.close();
            }
         } catch (Exception var15) {
         }

      }

   }

   public long getCharCreatetime() {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
         ps.setInt(1, this.getAccID());
         rs = ps.executeQuery();
         if (rs.next()) {
            this.createchartime = rs.getLong("createTime");
         }

         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var13) {
         System.err.println("Error getting character default" + var13);
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }

            if (con != null) {
               con.close();
            }
         } catch (Exception var12) {
         }

      }

      return this.createchartime;
   }

   public boolean isFirstlogin() {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
         ps.setInt(1, this.getAccID());
         rs = ps.executeQuery();
         if (rs.next()) {
            this.firstlogin = rs.getInt("FirstLogin") > 0;
         }

         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var13) {
         System.err.println("Error getting character default" + var13);
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }

            if (con != null) {
               con.close();
            }
         } catch (Exception var12) {
         }

      }

      return this.firstlogin;
   }

   public void setFirstlogin(boolean firstlogin) {
      this.firstlogin = firstlogin;
      Connection con = null;
      PreparedStatement ps = null;
      PreparedStatement ps2 = null;
      ResultSet rs1 = null;

      try {
         con = DatabaseConnection.getConnection();
         ps2 = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
         ps2.setInt(1, this.getAccID());
         rs1 = ps2.executeQuery();
         ps = con.prepareStatement("UPDATE accounts SET FirstLogin = ? WHERE id = ?");
         ps.setInt(1, firstlogin ? 1 : 0);
         ps.setInt(2, this.getAccID());
         ps.executeUpdate();
         ps.close();
         rs1.close();
         ps2.close();
         con.close();
      } catch (SQLException var15) {
         var15.printStackTrace();
      } finally {
         try {
            if (con != null) {
               con.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (ps2 != null) {
               ps2.close();
            }

            if (rs1 != null) {
               rs1.close();
            }
         } catch (Exception var14) {
         }

      }

   }

   public long getCustomKeyValue(int type, String key) {
      String questInfo = this.getInfoQuest(type);
      if (questInfo == null) {
         return -1L;
      } else {
         String[] data = questInfo.split(";");
         String[] var5 = data;
         int var6 = data.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            String s = var5[var7];
            if (s.startsWith(key + "=")) {
               String newkey = s.replace(key + "=", "");
               String newkey2 = newkey.replace(";", "");
               long dd = Long.valueOf(newkey2);
               return dd;
            }
         }

         return -1L;
      }
   }

   public String getCustomKeyValueStr(int type, String key) {
      String questInfo = this.getInfoQuest(type);
      if (questInfo == null) {
         return null;
      } else {
         String[] data = questInfo.split(";");
         String[] var5 = data;
         int var6 = data.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            String s = var5[var7];
            if (s.startsWith(key + "=")) {
               String newkey = s.replace(key + "=", "");
               String newkey2 = newkey.replace(";", "");
               return newkey2;
            }
         }

         return null;
      }
   }

   public void setCustomKeyValue(int id, String key, String value) {
      String questInfo = this.getInfoQuest(id);
      if (questInfo == null) {
         this.customKeyValue.put(id, key + "=" + value + ";");
         this.getSession().writeAndFlush(CWvsContext.InfoPacket.updateInfoQuest(id, key + "=" + value + ";"));
      } else {
         String[] data = questInfo.split(";");
         String[] var6 = data;
         int var7 = data.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            String s = var6[var8];
            if (s.startsWith(key + "=")) {
               String newkey = questInfo.replace(s, key + "=" + value);
               this.customKeyValue.put(id, newkey);
               this.getSession().writeAndFlush(CWvsContext.InfoPacket.updateInfoQuest(id, newkey));
               return;
            }
         }

         this.customKeyValue.put(id, questInfo + key + "=" + value + ";");
         this.getSession().writeAndFlush(CWvsContext.InfoPacket.updateInfoQuest(id, questInfo + key + "=" + value + ";"));
      }
   }

   public void removeCustomKeyValue(int type) {
      MapleQuest quest = MapleQuest.getInstance(type);
      if (quest != null) {
         this.send(CWvsContext.InfoPacket.updateInfoQuest(type, ""));
         this.customKeyValue.remove(type);
         Connection con = null;
         PreparedStatement ps = null;

         try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("DELETE FROM acc_questinfo WHERE accid = " + this.accId + " AND quest = ?");
            ps.setInt(1, type);
            ps.executeUpdate();
            ps.close();
         } catch (SQLException var14) {
            var14.printStackTrace();
         } finally {
            try {
               if (con != null) {
                  con.close();
               }

               if (ps != null) {
                  ps.close();
               }
            } catch (Exception var13) {
            }

         }

      }
   }

   public final List<MapleQuestStatus> getStartedQuests() {
      List<MapleQuestStatus> ret = new LinkedList();
      Iterator var2 = this.quests.values().iterator();

      while(var2.hasNext()) {
         MapleQuestStatus q = (MapleQuestStatus)var2.next();
         if (q.getStatus() == 1 && !q.isCustom() && !q.getQuest().isBlocked()) {
            ret.add(q);
         }
      }

      return ret;
   }

   public final List<MapleQuestStatus> getCompletedQuests() {
      List<MapleQuestStatus> ret = new LinkedList();
      Iterator var2 = this.quests.values().iterator();

      while(var2.hasNext()) {
         MapleQuestStatus q = (MapleQuestStatus)var2.next();
         if (q.getStatus() == 2 && !q.isCustom() && !q.getQuest().isBlocked()) {
            ret.add(q);
         }
      }

      return ret;
   }

   public final void updateQuest(MapleQuestStatus quest, boolean update) {
      this.quests.put(quest.getQuest(), quest);
      if (update) {
         this.getSession().writeAndFlush(CWvsContext.InfoPacket.updateQuest(quest));
      }

   }

   public final byte getQuestStatus(int quest) {
      MapleQuest qq = MapleQuest.getInstance(quest);
      return this.getQuestNoAdd(qq) == null ? 0 : this.getQuestNoAdd(qq).getStatus();
   }

   public final MapleQuestStatus getQuestNoAdd(MapleQuest quest) {
      return !this.quests.containsKey(quest) ? null : (MapleQuestStatus)this.quests.get(quest);
   }

   public final MapleQuestStatus getQuest(MapleQuest quest) {
      return !this.quests.containsKey(quest) ? new MapleQuestStatus(quest, 0) : (MapleQuestStatus)this.quests.get(quest);
   }

   public Map<MapleQuest, MapleQuestStatus> getQuests() {
      return this.quests;
   }

   public final String getInfoQuest(int questid) {
      return this.customKeyValue.containsKey(questid) ? (String)this.customKeyValue.get(questid) : "";
   }

   public Map<Integer, String> getCustomKeyValue() {
      return this.customKeyValue;
   }

   public final boolean getConnectorCheck() {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      boolean connectorAllowedCheck = true;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT gm, allowed FROM accounts WHERE id = ?");
         ps.setInt(1, this.accId);
         rs = ps.executeQuery();
         if (rs.next()) {
            if (rs.getInt("allowed") == 1) {
               connectorAllowedCheck = true;
            } else if (rs.getInt("gm") == 1) {
               connectorAllowedCheck = true;
            } else {
               connectorAllowedCheck = false;
            }
         }
      } catch (Exception var22) {
      } finally {
         if (con != null) {
            try {
               con.close();
            } catch (Exception var21) {
            }
         }

         if (rs != null) {
            try {
               rs.close();
            } catch (Exception var20) {
            }
         }

         if (ps != null) {
            try {
               ps.close();
            } catch (Exception var19) {
            }
         }

      }

      return connectorAllowedCheck;
   }

   protected static final class CharNameAndId {
      public final String name;
      public final int id;

      public CharNameAndId(String name, int id) {
         this.name = name;
         this.id = id;
      }
   }
}
