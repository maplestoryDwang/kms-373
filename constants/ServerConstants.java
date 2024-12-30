package constants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import server.DimentionMirrorEntry;
import server.QuickMoveEntry;
import server.ServerProperties;
import server.games.BingoGame;
import tools.Pair;
import tools.Triple;

public class ServerConstants {
   public static String mrank1 = null;
   public static String prank1 = null;
   public static String crank1 = null;
   public static String Gateway_IP = "175.207.0.33";
   public static final short MAPLE_VERSION = 1149;
   public static final byte MAPLE_PATCH = 1;
   public static boolean MAPLE_VERSION_IS_TEST = false;
   public static boolean Use_Fixed_IV = false;
   public static boolean Use_Localhost = false;
   public static boolean DEBUG_RECEIVE = false;
   public static boolean DEBUG_SEND = false;
   public static boolean DEBUG_CONNECTOR = false;
   public static final byte TRADE_TYPE = 1;
   public static boolean ServerTest = false;
   public static int starForceSalePercent = 30;
   public static int starForceSalePercents = 10;
   public static int starForcePlusProb = 5;
   public static int amazingscrollPlusProb = 5;
   public static int soulPlusProb = 2;
   public static int BuddyChatPort = Integer.parseInt(ServerProperties.getProperty("ports.buddy"));
   public static int EventBonusExp = Integer.parseInt(ServerProperties.getProperty("world.eventBonus"));
   public static int WeddingExp = Integer.parseInt(ServerProperties.getProperty("world.weddingBonus"));
   public static int PartyExp = Integer.parseInt(ServerProperties.getProperty("world.partyBonus"));
   public static int PcRoomExp = Integer.parseInt(ServerProperties.getProperty("world.pcBonus"));
   public static int RainbowWeekExp = Integer.parseInt(ServerProperties.getProperty("world.rainbowBonus"));
   public static int BoomupExp = Integer.parseInt(ServerProperties.getProperty("world.boomBonus"));
   public static int PortionExp = Integer.parseInt(ServerProperties.getProperty("world.portionBonus"));
   public static int RestExp = Integer.parseInt(ServerProperties.getProperty("world.restBonus"));
   public static int ItemExp = Integer.parseInt(ServerProperties.getProperty("world.itemBonus"));
   public static int ValueExp = Integer.parseInt(ServerProperties.getProperty("world.valueBonus"));
   public static int IceExp = Integer.parseInt(ServerProperties.getProperty("world.iceBonus"));
   public static int HpLiskExp = Integer.parseInt(ServerProperties.getProperty("world.hpLiskBonus"));
   public static int FieldBonusExp = Integer.parseInt(ServerProperties.getProperty("world.fieldBonus"));
   public static int EventBonusExp2 = Integer.parseInt(ServerProperties.getProperty("world.eventBonus2"));
   public static int FieldBonusExp2 = Integer.parseInt(ServerProperties.getProperty("world.fieldBonus2"));
   public static final byte check = 1;
   public static int StartMap = Integer.parseInt(ServerProperties.getProperty("world.startMap"));
   public static int warpMap = Integer.parseInt(ServerProperties.getProperty("world.warpMap"));
   public static int fishMap = 680000711;
   public static int csNpc = 9001174;
   public static int JuhunFever = 0;
   public static String WORLD_UI = "UI/UIWindowEvent.img/sundayMaple";
   public static String SUNDAY_TEXT = "#sunday# #fn나눔고딕 ExtraBold##fs20##fc0xFFFFFFFF#경험치 3배 쿠폰(15분) #fc0xFFFFD800#5개 #fc0xFFFFFFFF#지급!\\n#sunday# #fs20##fc0xFFFFFFFF#RISE 포인트 획득 가능량 #fc0xFFFFD800#2배!#fc0xFFFFFFFF#";
   public static String SUNDAY_DATE = "#fn나눔고딕 ExtraBold##fs15##fc0xFFB7EC00#2019년 12월 22일 일요일";
   public static String serverMessage = "";
   public static String mailid = "theblackmaplestory";
   public static String mailpw = "ejqmfforrkt3214!";
   public static boolean ChangeMapUI = false;
   public static boolean feverTime = false;
   public static int ReqDailyLevel = 33;
   public static List<BingoGame> BingoGameHolder = new ArrayList();
   public static List<QuickMoveEntry> quicks = new ArrayList();
   public static List<DimentionMirrorEntry> mirrors = new ArrayList();
   public static List<Pair<Integer, Long>> boss = new ArrayList();
   public static List<Pair<Integer, Long>> boss2 = new ArrayList();
   public static List<Pair<Integer, Integer>> CashMainInfo = new ArrayList();
   public static int SgoldappleSuc;
   public static List<Triple<Integer, Integer, Integer>> goldapple = new ArrayList();
   public static List<Triple<Integer, Integer, Integer>> Sgoldapple = new ArrayList();
   public static List<Pair<Integer, Integer>> NeoPosList = new ArrayList();
   public static List<Integer> FirstLogin = new ArrayList();
   public static boolean ConnectorSetting = false;
   public static int MaxLevel = 999;
   public static boolean Event_MapleLive = false;
   public static String SundayMapleUI = "UI/UIWindowEvent.img/sundayMaple";
   public static String SundayMapleTEXTLINE_1 = "";
   public static String SundayMapleTEXTLINE_2 = "";
   public static int reboottime = 0;
   public static Thread t = null;
   public static ScheduledFuture<?> ts = null;
   public static final String GM_IP = "127.0.0.1";
   public static final String GM_ID = "admin";
   public static final String GM_NAME = "GM하인즈";
   public static final int pice = 1;
   public static final int[] hour = new int[]{3, 6, 9, 12, 15, 18, 21, 24};

   public static enum CommandType {
      NORMAL(0),
      TRADE(1);

      private int level;

      private CommandType(int level) {
         this.level = level;
      }

      public int getType() {
         return this.level;
      }

      private static ServerConstants.CommandType[] $values() {
         return new ServerConstants.CommandType[]{NORMAL, TRADE};
      }

      static {
         ServerConstants.CommandType[] var0 = $values();
      }
   }

   public static enum PlayerGMRank {
      NORMAL('@', 0),
      DONATOR('#', 1),
      SUPERDONATOR('$', 2),
      INTERN('%', 3),
      GM('!', 4),
      SUPERGM('!', 5),
      ADMIN('!', 6);

      private char commandPrefix;
      private int level;

      private PlayerGMRank(char ch, int level) {
         this.commandPrefix = ch;
         this.level = level;
      }

      public char getCommandPrefix() {
         return this.commandPrefix;
      }

      public int getLevel() {
         return this.level;
      }

      private static ServerConstants.PlayerGMRank[] $values() {
         return new ServerConstants.PlayerGMRank[]{NORMAL, DONATOR, SUPERDONATOR, INTERN, GM, SUPERGM, ADMIN};
      }

      static {
         ServerConstants.PlayerGMRank[] var0 = $values();
      }
   }
}
