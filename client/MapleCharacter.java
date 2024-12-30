package client;

import client.custom.inventory.CustomItem;
import client.damage.CalcDamage;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.ItemLoader;
import client.inventory.MapleAndroid;
import client.inventory.MapleImp;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MapleMount;
import client.inventory.MaplePet;
import client.inventory.MapleRing;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import constants.KoreaCalendar;
import constants.ServerConstants;
import database.DatabaseConnection;
import database.DatabaseException;
import handling.channel.ChannelServer;
import handling.channel.handler.AttackInfo;
import handling.channel.handler.MatrixHandler;
import handling.channel.handler.PlayerHandler;
import handling.login.LoginInformationProvider;
import handling.login.LoginServer;
import handling.world.CharacterTransfer;
import handling.world.MapleMessenger;
import handling.world.MapleMessengerCharacter;
import handling.world.PlayerBuffStorage;
import handling.world.PlayerBuffValueHolder;
import handling.world.World;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildCharacter;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import handling.world.party.PartyOperation;
import io.netty.channel.Channel;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.PrintStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import scripting.EventInstanceManager;
import scripting.NPCScriptManager;
import server.CashShop;
import server.ChatEmoticon;
import server.MapleChatEmoticon;
import server.MapleDonationSkill;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.MapleSavedEmoticon;
import server.MapleStorage;
import server.MapleTrade;
import server.Randomizer;
import server.SecondaryStatEffect;
import server.SkillCustomInfo;
import server.events.MapleBattleGroundCharacter;
import server.events.MapleTyoonKitchen;
import server.field.boss.MapleBossManager;
import server.field.boss.lucid.FieldLucid;
import server.field.skill.MapleFieldAttackObj;
import server.field.skill.MapleMagicWreck;
import server.field.skill.MapleSecondAtom;
import server.field.skill.SecondAtom;
import server.games.BattleReverse;
import server.games.BingoGame;
import server.games.ColorInvitationCard;
import server.games.DetectiveGame;
import server.games.MonsterPyramid;
import server.games.MultiYutGame;
import server.games.OXQuizGame;
import server.games.OneCardGame;
import server.life.AffectedOtherSkillInfo;
import server.life.Ignition;
import server.life.MapleHaku;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.life.PlayerNPC;
import server.maps.AnimatedMapleMapObject;
import server.maps.FieldLimitType;
import server.maps.MapleAtom;
import server.maps.MapleDoor;
import server.maps.MapleDragon;
import server.maps.MapleExtractor;
import server.maps.MapleFoothold;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleMist;
import server.maps.MapleSummon;
import server.maps.MechDoor;
import server.maps.SavedLocationType;
import server.maps.SummonMovementType;
import server.marriage.MarriageMiniBox;
import server.movement.LifeMovementFragment;
import server.polofritto.BountyHunting;
import server.polofritto.DefenseTowerWave;
import server.polofritto.FrittoDancing;
import server.polofritto.FrittoEagle;
import server.polofritto.FrittoEgg;
import server.quest.MapleQuest;
import server.quest.party.MapleNettPyramid;
import server.shops.IMaplePlayerShop;
import server.shops.MapleShop;
import tools.FileoutputUtil;
import tools.Pair;
import tools.StringUtil;
import tools.Triple;
import tools.data.LittleEndianAccessor;
import tools.data.MaplePacketLittleEndianWriter;
import tools.packet.BattleGroundPacket;
import tools.packet.CField;
import tools.packet.CSPacket;
import tools.packet.CWvsContext;
import tools.packet.MobPacket;
import tools.packet.PacketHelper;
import tools.packet.PetPacket;
import tools.packet.PlayerShopPacket;
import tools.packet.SLFCGPacket;
import tools.packet.SkillPacket;

public class MapleCharacter extends AnimatedMapleMapObject implements Serializable {
   private static final long serialVersionUID = 845748950829L;
   public int 엔젤릭버스터임시 = 0;
   public long 자동사냥;
   public boolean signofbomb = false;
   private int HowlingGaleCount = 0;
   private int YoyoCount = 0;
   private int WildGrenadierCount = 0;
   private int VerseOfRelicsCount = 0;
   private int BHGCCount = 0;
   private int createDate = 0;
   private int RandomPortal = 0;
   private int fwolfattackcount = 0;
   private int BlockCount = 0;
   private int BlockCoin = 0;
   private int MesoChairCount = 0;
   private int tempmeso = 0;
   private int eventcount = 0;
   private int duskGauge = 0;
   private long fwolfdamage = 0L;
   private long LastMovement = 0L;
   private long PlatformerStageEnter = 0L;
   private long AggressiveDamage = 0L;
   private boolean hasfwolfportal = false;
   private boolean isfwolfkiller = false;
   private boolean isWatchingWeb = false;
   private boolean oneMoreChance = false;
   private boolean isDuskBlind = false;
   private boolean eventkillmode = false;
   private boolean hottimeboss = false;
   private boolean hottimebosslastattack = false;
   private boolean hottimebossattackcheck = false;
   private String chairtext;
   private BingoGame BingoInstance = null;
   private OXQuizGame OXInstance = null;
   private DetectiveGame DetectiveGameInstance = null;
   private BattleReverse BattleReverseInstance = null;
   private OneCardGame oneCardInstance = null;
   private MultiYutGame multiYutInstance = null;
   private ColorInvitationCard ColorCardInstance = null;
   private MonsterPyramid monsterPyramidInstance = null;
   private List<PlatformerRecord> PfRecords = new ArrayList();
   private int blackmagewb = 1;
   private Point Resolution = new Point(0, 0);
   private long rainbowrushStartTime = 0L;
   private int RainBowRushTime = 0;
   private int CosmicCount;
   private int MomentumCount;
   private boolean israinbow = false;
   private boolean isnodeadRush = false;
   public long lastSaveTime = 0L;
   public long lastReportTime = 0L;
   public long lastMacroTime = System.currentTimeMillis();
   public Timer ConstentTimer;
   private int SkillId;
   private int SkillId2;
   private int SkillId3;
   private int SkillId4;
   private int SkillId5;
   private int SkillId6;
   public int 에테르소드 = 0;
   public int 에테르 = 0;
   public int 활성화된소드 = 0;
   public int 홀리워터 = 0;
   public int 홀리워터스택 = 0;
   public int 서펜트스톤 = 0;
   public int 서펜트스크류 = 0;
   public int 발할라검격 = 12;
   public int 리인카네이션 = 0;
   public int 플레시미라주스택 = 1;
   public int 스킬카운트 = 0;
   public int addBuffCheck = 0;
   public int 메이플용사 = 0;
   public long lastAstraTime = 0L;
   public long lastConcentrationTime;
   public long lastSilhouetteMirageCreateTime;
   public long lastSilhouetteMirageAttackTime;
   public long lastVerseOfRelicsTime;
   public long lastTimeleapTime;
   public long lastDemonicFrenzyTime;
   public long lastChainArtsFuryTime;
   public long lastFireArrowTime;
   public long lastThunderTime;
   public long lastChairPointTime;
   public long lastVamTime = 0L;
   public long lastAltergoTime = 0L;
   public long lastButterflyTime = 0L;
   public long lastUnionRaidTime = 0L;
   private String name;
   private String chalktext;
   private String BlessOfFairy_Origin;
   private String BlessOfEmpress_Origin;
   private String teleportname;
   private long exp;
   private long meso;
   private long lastCombo;
   private long lastfametime;
   private long keydown_skill;
   private long nextConsume;
   private long pqStartTime;
   private long lastBerserkTime;
   private long lastRecoveryTime;
   private long lastSummonTime;
   private long mapChangeTime;
   private long lastFairyTime;
   private long lastExceedTime = System.currentTimeMillis();
   private long lastHPTime;
   private long lastMPTime;
   private long lastDOTTime;
   private long monsterComboTime = 0L;
   private long lastBulletUsedTime = 0L;
   private long lastCreationTime = 0L;
   private byte deathcount;
   private byte gmLevel;
   private byte gender;
   private byte secondgender;
   private byte initialSpawnPoint;
   private byte skinColor;
   private byte secondSkinColor;
   private byte guildrank = 5;
   private byte allianceRank = 5;
   private byte cardStack;
   private byte wolfscore;
   private byte sheepscore;
   private byte pandoraBoxFever;
   private byte world;
   private byte fairyExp;
   private byte numClones;
   private byte subcategory;
   public byte RapidTimeDetect = 0;
   public byte RapidTimeStrength = 0;
   public byte acaneAim = 0;
   private short level;
   private short mulung_energy;
   private short combo;
   private short force;
   private short availableCP;
   private short fatigue;
   private short totalCP;
   private short hpApUsed;
   private short job;
   private short remainingAp;
   private short scrolledPosition;
   private short xenonSurplus = 0;
   private short kaiserCombo;
   private short monsterCombo = 0;
   private short forcingItem = 0;
   private int betaclothes = 0;
   private int zeroCubePosition = 0;
   private int moonGauge = 0;
   private int overloadCount = 0;
   private int exceed = 0;
   private int accountid;
   private int id;
   private transient CalcDamage calcDamage;
   public int batt = 0;
   public int clearWeb = 0;
   public int forceBlood = 0;
   public int fightJazzSkill = 0;
   public int nextBlessSkill = 0;
   public int empiricalStack = 0;
   public int adelResonance = 0;
   public int silhouetteMirage;
   public int repeatingCrossbowCatridge;
   public int dojoCoolTime = 0;
   public int dojoStartTime = 0;
   public MapleMonster empiricalKnowledge = null;
   private long dojoStopTime = 0L;
   private boolean deadEffect = false;
   private boolean noneDestroy = false;
   private boolean dojoStop = false;
   private int hair;
   private int basecolor = -1;
   private int addcolor;
   private int baseprob;
   private int secondbasecolor = -1;
   private int secondaddcolor;
   private int secondbaseprob;
   private int secondhair;
   private int face;
   private int secondface;
   private int demonMarking;
   private int mapid;
   private int fame;
   private int pvpExp;
   private int pvpPoints;
   private int totalWins;
   private int totalLosses;
   private int guildid = 0;
   private int fallcounter;
   private int maplepoints;
   private int nxcredit;
   private int acash;
   private int chair;
   private int itemEffect;
   private int points;
   private int vpoints;
   private int itcafetime;
   private int rank = 1;
   private int rankMove = 0;
   private int jobRank = 1;
   private int jobRankMove = 0;
   private int marriageId;
   private int marriageItemId;
   private int dotHP;
   private int honourExp;
   private int honorLevel;
   private int ignitionstack = 0;
   private int arcaneAim = 0;
   private int listonation = 0;
   private int elementalCharge = 0;
   private int lastElementalCharge = 0;
   private int reinCarnation = 0;
   private int transformCooldown = 0;
   private byte poisonStack = 0;
   private byte unityofPower = 0;
   private byte concentration = 0;
   private byte mortalBlow = 0;
   private byte death = 0;
   private byte royalStack = 0;
   private int beholderSkill1 = 0;
   private int beholderSkill2 = 0;
   private int barrier = 0;
   private int energyBurst = 0;
   private int trinity = 0;
   private int blitzShield = 0;
   private byte infinity = 1;
   private byte holyPountin = 0;
   private byte blessingAnsanble = 0;
   private byte quiverType = 0;
   private byte flip = 0;
   private byte holyMagicShell = 0;
   private byte antiMagicShell = 0;
   private byte blessofDarkness = 0;
   private int currentrep;
   private int dice = 0;
   private int holyPountinOid = 0;
   private int blackMagicAlter = 0;
   private int judgementType = 0;
   private int[] RestArrow = new int[2];
   private int[] deathCounts = new int[5];
   private List<Integer> weaponChanges = new ArrayList();
   private List<Integer> weaponChanges2 = new ArrayList();
   private List<Integer> posionNovas = new ArrayList();
   private List<Integer> exceptionList = new ArrayList();
   public Map<Integer, Integer> weaponChanges1 = new LinkedHashMap();
   public Map<Integer, Long> CheckAttackTime = new LinkedHashMap();
   private List<MapleMapItem> pickPocket = new ArrayList();
   private int mparkkillcount = 0;
   private int mparkcount = 0;
   private int mparkexp = 0;
   private int markofPhantom = 0;
   private int ultimateDriverCount = 0;
   private int markOfPhantomOid = 0;
   private int rhoAias = 0;
   private int perfusion = 0;
   private int spiritGuard = 0;
   private boolean mparkCharged = false;
   private MapleFieldAttackObj fao = null;
   public int throwBlasting = 0;
   public int striker4thAttack = 0;
   public int striker4thSkill = 0;
   public int revenant = 0;
   public int revenantCount = 0;
   public int photonRay = 0;
   public int weaponVarietyFinaleStack = 0;
   public int weaponVarietyFinale = 0;
   public int lawOfGravity = 0;
   public long lastInstallMahaTime = 0L;
   public long lastRecoverScrollGauge = 0L;
   public long cooldownforceBlood = 0L;
   public long cooldownEllision = 0L;
   public long lastDistotionTime = 0L;
   public long lastNemeaAttackTime = 0L;
   public long lastGerionAttackTime = 0L;
   public long lastBonusAttckTime = 0L;
   public long lastElementalGhostTime = 0L;
   public long lastDrainAuraTime = 0L;
   public long lastShardTime = 0L;
   public long lastPinPointRocketTime = 0L;
   public long lastDeathAttackTime = 0L;
   public long lastChargeEnergyTime = 0L;
   public long lastThrowBlastingTime = 0L;
   public long lastBlizzardTempestTime = 0L;
   public long lastRoyalKnightsTime = 0L;
   public long lastCrystalGateTime = 0L;
   public long lastSungiAttackTime = 0L;
   public long lastDisconnectTime = 0L;
   public int unstableMemorize = 0;
   public int ignoreDraco = 0;
   public int lastHowlingGaleObjectId = -1;
   public int scrollGauge = 0;
   public int shadowBite = 0;
   public int curseBound = 0;
   public int editionalTransitionAttack = 0;
   public int lastCardinalForce = 0;
   public int cardinalMark = 0;
   public int flameDischargeRegen = 0;
   public int striker3rdStack = 0;
   public int mascotFamilier = 0;
   public int shadowerDebuff = 0;
   public int shadowerDebuffOid = 0;
   public int maelstrom = 0;
   public int lastPoseType = 0;
   public int energy = 0;
   public int serpent;
   public int blessMark = 0;
   public int blessMarkSkill = 0;
   public int fightJazz = 0;
   public int guidedBullet = 0;
   public int graveObjectId = 0;
   public int 렐릭게이지 = 0;
   public int 에인션트가이던스 = 0;
   public int 문양 = 0;
   public int Serpent = 0;
   public int Serpent2 = 0;
   public boolean useChun = false;
   public boolean useJi = false;
   public boolean useIn = false;
   public boolean wingDagger = false;
   public boolean canUseMortalWingBeat = false;
   public boolean gagenominus = false;
   public String guildName;
   private String BattleGrondJobName;
   private Pair<Integer, Integer> recipe = new Pair(0, 0);
   private ScheduledFuture<?> PlatformerTimer;
   private ScheduledFuture<?> MesoChairTimer;
   private ScheduledFuture<?> secondaryStatEffectTimer;
   private ScheduledFuture<?> EventSkillTimer;
   public ScheduledFuture<?> revenantTimer = null;
   private int totalrep;
   private int coconutteam;
   private int followid;
   private int battleshipHP;
   private int challenge;
   private int guildContribution = 0;
   private int lastattendance = 0;
   private int storageNpc = 0;
   private int lastBossId = 0;
   private int TouchedRune;
   private boolean luminusMorph = false;
   private boolean useBuffFreezer = false;
   private boolean extremeMode = false;
   private boolean hellMode;
   private Point flameHeiz = null;
   private int lumimorphuse = 500000;
   private long lastTouchedRuneTime = 0L;
   private Point old;
   private long DotDamage = 0L;
   public long lastHowlingGaleTime;
   public long lastYoyoTime;
   public long lastWildGrenadierTime;
   public long lastRandomAttackTime = 0L;
   public long lastWeaponVarietyFinaleTime;
   private int[] rocks;
   private int[] savedLocations;
   private int[] regrocks;
   private int[] hyperrocks;
   private int[] remainingSp = new int[10];
   private int[] wishlist = new int[12];
   private transient AtomicInteger inst;
   private transient AtomicInteger insd;
   private transient List<LifeMovementFragment> lastres;
   private Map<String, String> keyValues = new HashMap();
   private Map<String, String> keyValues_boss = new HashMap();
   private List<Integer> lastmonthfameids;
   private List<Integer> lastmonthbattleids;
   private List<Integer> extendedSlots;
   private List<Integer> cashwishlist = new ArrayList();
   private List<MapleDoor> doors;
   private List<MechDoor> mechDoors;
   public MaplePet[] pets = new MaplePet[3];
   private Item attackitem;
   private List<Item> rebuy;
   private List<InnerSkillValueHolder> innerSkills;
   public List<InnerSkillValueHolder> innerCirculator = new ArrayList();
   private MapleImp[] imps = new MapleImp[3];
   private List<Equip> symbol;
   private List<Pair<Integer, Boolean>> stolenSkills = new ArrayList();
   private transient List<MapleMonster> controlled;
   private transient Set<MapleMapObject> visibleMapObjects;
   private transient MapleAndroid android;
   private transient MapleHaku haku;
   private Map<MapleQuest, MapleQuestStatus> quests;
   private Map<Integer, String> questinfo = new ConcurrentHashMap();
   private Map<Skill, SkillEntry> skills;
   private List<Triple<Skill, SkillEntry, Integer>> linkskills;
   private transient Map<Integer, Integer> customValue = null;
   private transient Map<Integer, SkillCustomInfo> customInfo = new LinkedHashMap();
   private List<Pair<SecondaryStat, SecondaryStatValueHolder>> effects;
   private transient List<MapleSummon> summons;
   private transient Map<Integer, MapleCoolDownValueHolder> coolDowns = new LinkedHashMap();
   private transient Map<SecondaryStat, MapleDiseases> diseases;
   public List<Pair<Integer, Integer>> object = new ArrayList();
   private CashShop cs;
   private BuddyList buddylist;
   private UnionList unions;
   public MapleClient client;
   private int bufftest = 0;
   private transient MapleParty party;
   private PlayerStats stats;
   private transient MapleMap map;
   private transient MapleShop shop;
   private transient MapleDragon dragon;
   private transient MapleExtractor extractor;
   private List<Core> cores = new ArrayList();
   private List<VMatrix> matrixs = new ArrayList();
   private List<MapleMannequin> hairRoom;
   private List<MapleMannequin> faceRoom;
   private List<MapleMannequin> skinRoom;
   private MapleStorage storage;
   private transient MapleTrade trade;
   private MapleMount mount;
   private MapleMessenger messenger;
   private transient IMaplePlayerShop playerShop;
   private boolean invincible;
   private boolean canTalk;
   private boolean followinitiator;
   private boolean followon;
   private boolean smega;
   public boolean petLoot;
   public boolean shield = false;
   private MapleGuildCharacter mgc;
   private transient EventInstanceManager eventInstance;
   private MapleInventory[] inventory;
   private List<AvatarLook> coodination = new ArrayList();
   private SkillMacro[] skillMacros = new SkillMacro[5];
   private EnumMap<MapleTrait.MapleTraitType, MapleTrait> traits;
   private MapleKeyLayout keylayout = new MapleKeyLayout();
   public long lastBHGCGiveTime = System.currentTimeMillis();
   public long lastDrainTime = System.currentTimeMillis();
   public long lastFreezeTime = System.currentTimeMillis();
   public long lastHealTime = System.currentTimeMillis();
   public long lastAngelTime = System.currentTimeMillis();
   public long lastInfinityTime = System.currentTimeMillis();
   public long lastCygnusTime = System.currentTimeMillis();
   public long lastSpiritGateTime = System.currentTimeMillis();
   public long lastDriveTime = System.currentTimeMillis();
   public long TriumphTime = 0L;
   private transient ScheduledFuture<?> itcafetimer;
   private transient ScheduledFuture<?> diabolicRecoveryTask;
   private transient ScheduledFuture<?> LastTouchedRune = null;
   public static transient ScheduledFuture<?> XenonSupplyTask = null;
   public boolean vh;
   List<Integer> allpetbufflist = new ArrayList(3);
   private List<SecondAtom> SaList = new ArrayList();
   private transient List<Integer> pendingExpiration = null;
   private transient Map<Skill, SkillEntry> pendingSkills = null;
   private transient Map<Integer, Integer> linkMobs;
   private boolean changed_wishlist;
   private boolean changed_trocklocations;
   private boolean changed_regrocklocations;
   private boolean changed_hyperrocklocations;
   private boolean changed_skillmacros;
   private boolean changed_savedlocations;
   private boolean changed_questinfo;
   private boolean changed_skills;
   private boolean changed_reports;
   private boolean changed_extendedSlots;
   private boolean innerskill_changed = true;
   private boolean fishing = false;
   private int premiumbuff = 0;
   private long premiumPeriod = 0L;
   public long DamageMeter = 0L;
   private String premium = "";
   private int reborns;
   private int apstorage;
   public boolean pvp = false;
   public boolean isTrade = false;
   public boolean isCatching = false;
   public boolean isCatched = false;
   public boolean isWolfShipWin = false;
   public boolean isVoting = false;
   public boolean isDead = false;
   public boolean isMapiaVote = false;
   public boolean isDrVote = false;
   public boolean isPoliceVote = false;
   private List<Item> auctionitems;
   public String mapiajob = "";
   public short blackRebirthPos = 0;
   public int voteamount = 0;
   public int getmapiavote = 0;
   public int getpolicevote = 0;
   public int getdrvote = 0;
   public int mbating = 0;
   public int CrystalCharge = 0;
   public int returnSc = 0;
   public int peaceMaker = 0;
   public long blackRebirth = 0L;
   public Equip blackRebirthScroll = null;
   public Equip returnscroll = null;
   public Equip choicepotential = null;
   public Item memorialcube = null;
   private int slowAttackCount = 0;
   public boolean isdressup = false;
   public boolean useBlackJack = false;
   private int LinkMobCount = 0;
   private int lastCharGuildId = 0;
   private int weddingGiftGive;
   private Point specialChairPoint = new Point();
   private boolean useTruthDoor = false;
   public int nettDifficult = 0;
   private transient MapleNettPyramid NettPyramid = null;
   private DefenseTowerWave defenseTowerWave = null;
   private BountyHunting bountyHunting = null;
   private FrittoEagle frittoEagle = null;
   private FrittoEgg frittoEgg = null;
   private FrittoDancing frittoDancing = null;
   private MapleTyoonKitchen Mtk = null;
   private MarriageMiniBox mg = null;
   private int graveTarget;
   public int bullet = 0;
   public int SerenStunGauge = 0;
   public int cylindergauge = 0;
   private List<MapleSavedEmoticon> savedEmoticon = new CopyOnWriteArrayList();
   private List<MapleChatEmoticon> emoticonTabs = new CopyOnWriteArrayList();
   private List<Triple<Long, Integer, Short>> emoticons = new CopyOnWriteArrayList();
   private List<Pair<Integer, Short>> emoticonBookMarks = new CopyOnWriteArrayList();
   private int spAttackCountMobId = 0;
   private int spCount = 0;
   private long spLastValidTime = 0L;
   private Pair<Integer, Integer> eqpSpCore = null;
   boolean dominant = false;
   public ScheduledFuture<?> rapidtimer1 = null;
   public ScheduledFuture<?> rapidtimer2 = null;
   public int trueSniping = 0;
   public int shadowAssault = 0;
   public int transformEnergyOrb = 0;
   private long damageMeter;
   private transient ScheduledFuture<?> mapTimeLimitTask;
   public boolean isMegaSmasherCharging = false;
   public boolean memoraizecheck = false;
   public long megaSmasherChargeStartTime = 0L;
   public long lastWindWallTime = 0L;
   public long lastArrowRain = 0L;
   public byte battAttackCount = 0;
   public byte mCount = 0;
   public boolean energyCharge = false;
   public int lightning = 0;
   public int siphonVitality = 0;
   public int armorSplit = 0;
   public int PPoint = 0;
   public int combination = 0;
   public int stackbuff = 0;
   public int combinationBuff = 0;
   public int BULLET_SKILL_ID = 0;
   public int SpectorGauge = 0;
   public int LinkofArk = 0;
   public int FlowofFight = 0;
   public int Stigma = 0;
   public int bulletParty;
   public int criticalGrowing = 0;
   public int criticalDamageGrowing = 0;
   public int bodyOfSteal = 0;
   public Timer MulungTimer;
   public TimerTask MulungTimerTask;
   public boolean Lotus = false;
   private long lastSpawnBlindMobtime = System.currentTimeMillis();
   public int erdacount = 0;
   public int orgelcount = 20;
   public boolean orgelTime = false;
   public byte SummonChakriStack = 0;
   public int JamsuTime = 0;
   public int Jamsu5m = 0;
   public boolean isFirst = false;
   public long WorldbossDamage = 0L;

   private MapleCharacter(boolean ChannelServer, boolean firstIngame) {
      this.setStance(this.bulletParty = 0);
      this.setPosition(new Point(0, 0));
      this.lastSaveTime = System.currentTimeMillis();
      this.inventory = new MapleInventory[MapleInventoryType.values().length];
      MapleInventoryType[] var3 = MapleInventoryType.values();
      int var4 = var3.length;

      int var5;
      for(var5 = 0; var5 < var4; ++var5) {
         MapleInventoryType type = var3[var5];
         this.inventory[type.ordinal()] = new MapleInventory(type);
      }

      this.quests = new ConcurrentHashMap();
      this.skills = new ConcurrentHashMap();
      this.linkskills = new ArrayList();
      this.stats = new PlayerStats();
      this.innerSkills = new LinkedList();
      this.setHairRoom(new ArrayList());
      this.setFaceRoom(new ArrayList());
      this.skinRoom = new ArrayList();

      int i;
      for(i = 0; i < this.remainingSp.length; ++i) {
         this.remainingSp[i] = 0;
      }

      this.traits = new EnumMap(MapleTrait.MapleTraitType.class);
      MapleTrait.MapleTraitType[] var8 = MapleTrait.MapleTraitType.values();
      var4 = var8.length;

      for(var5 = 0; var5 < var4; ++var5) {
         MapleTrait.MapleTraitType t = var8[var5];
         this.traits.put(t, new MapleTrait(t));
      }

      if (ChannelServer) {
         this.changed_reports = false;
         this.changed_skills = false;
         this.changed_wishlist = false;
         this.changed_trocklocations = false;
         this.changed_regrocklocations = false;
         this.changed_hyperrocklocations = false;
         this.changed_skillmacros = false;
         this.changed_savedlocations = false;
         this.changed_extendedSlots = false;
         this.changed_questinfo = false;
         this.canTalk = true;
         this.scrolledPosition = 0;
         this.lastCombo = 0L;
         this.mulung_energy = 0;
         this.combo = 0;
         this.force = 0;
         this.keydown_skill = 0L;
         this.nextConsume = 0L;
         this.pqStartTime = 0L;
         this.fairyExp = 0;
         this.cardStack = 0;
         this.mapChangeTime = 0L;
         this.lastRecoveryTime = 0L;
         this.lastBerserkTime = 0L;
         this.lastFairyTime = 0L;
         this.lastHPTime = 0L;
         this.lastMPTime = 0L;
         this.old = new Point(0, 0);
         this.coconutteam = 0;
         this.followid = 0;
         this.battleshipHP = 0;
         this.marriageItemId = 0;
         this.fallcounter = 0;
         this.challenge = 0;
         this.dotHP = 0;
         this.itcafetime = 0;
         this.lastSummonTime = 0L;
         this.invincible = false;
         this.followinitiator = false;
         this.followon = false;
         this.rebuy = new ArrayList();
         this.symbol = new ArrayList();
         this.setAuctionitems(new ArrayList());
         this.linkMobs = new HashMap();
         this.teleportname = "";
         this.smega = true;
         this.wishlist = new int[12];
         this.rocks = new int[10];
         this.regrocks = new int[5];
         this.hyperrocks = new int[13];
         this.extendedSlots = new ArrayList();
         this.effects = new CopyOnWriteArrayList();
         this.diseases = new ConcurrentHashMap();
         this.inst = new AtomicInteger(0);
         this.insd = new AtomicInteger(-1);
         this.doors = new ArrayList();
         this.mechDoors = new ArrayList();
         this.controlled = new CopyOnWriteArrayList();
         this.summons = new CopyOnWriteArrayList();
         this.visibleMapObjects = new CopyOnWriteArraySet();
         this.savedLocations = new int[SavedLocationType.values().length];

         for(i = 0; i < SavedLocationType.values().length; ++i) {
            this.savedLocations[i] = -1;
         }

         this.customValue = new HashMap();
         this.deathcount = -1;
         this.secondaryStatEffectTimer = server.Timer.BuffTimer.getInstance().register(new MapleCharacter.MapleCharacterManagement(), 1000L);
      }

   }

   public void handleAdditionalSkills(long time) {
      HashMap<SecondaryStat, MapleDiseases> remover = new HashMap();
      Iterator var7 = this.getDiseases().entrySet().iterator();

      Entry remove;
      while(var7.hasNext()) {
         remove = (Entry)var7.next();
         if (System.currentTimeMillis() - ((MapleDiseases)remove.getValue()).getStartTime() >= (long)((MapleDiseases)remove.getValue()).getDuration()) {
            remover.put((SecondaryStat)remove.getKey(), (MapleDiseases)remove.getValue());
         }
      }

      if (!remover.isEmpty()) {
         var7 = this.getDiseases().entrySet().iterator();

         while(var7.hasNext()) {
            remove = (Entry)var7.next();
            this.cancelDisease((SecondaryStat)remove.getKey());
         }
      }

      SecondaryStatEffect effect;
      HashMap statups9;
      if (this.getSkillLevel(22170074) > 0) {
         effect = SkillFactory.getSkill(22170074).getEffect(this.getSkillLevel(22170074));
         if (this.getStat().getMPPercent() >= effect.getX() && this.getStat().getMPPercent() <= effect.getY()) {
            if (!this.getBuffedValue(22170074)) {
               statups9 = new HashMap();
               statups9.put(SecondaryStat.IndieMadR, new Pair(Integer.valueOf(effect.getDamage()), 0));
               this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups9, effect, this));
               this.getEffects().add(new Pair(SecondaryStat.IndieMadR, new SecondaryStatValueHolder(effect, -1L, effect.getDamage(), 0, this.getId(), new ArrayList(), new ArrayList())));
            }
         } else if (this.getBuffedValue(22170074)) {
            this.cancelEffectFromBuffStat(SecondaryStat.IndieMadR, 22170074);
         }
      }

      SecondaryStatEffect eff;
      int 감소량;
      if (this.getBuffedValue(400001043) && time - this.lastCygnusTime >= 4000L) {
         eff = this.getBuffedEffect(400001043);
         감소량 = this.getBuffedValue(SecondaryStat.IndieDamR, 400001043);
         if (감소량 < eff.getW()) {
            this.setBuffedValue(SecondaryStat.Infinity, 400001043, 감소량 + eff.getDamage());
            this.addHP(this.getStat().getMaxHp() * (long)eff.getY() / 100L);
            this.lastCygnusTime = System.currentTimeMillis();
            statups9 = new HashMap();
            statups9.put(SecondaryStat.IndieDamR, new Pair(감소량 + eff.getDamage(), (int)this.getBuffLimit(400001043)));
            this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups9, eff, this));
         }
      }

      if (this.getBuffedValue(400001044) && time - this.lastCygnusTime >= 4000L) {
         eff = this.getBuffedEffect(400001044);
         감소량 = this.getBuffedValue(SecondaryStat.IndieDamR, 400001044);
         if (감소량 < eff.getW()) {
            this.setBuffedValue(SecondaryStat.IndieDamR, 400001044, 감소량 + eff.getDamage());
            this.addHP(this.getStat().getMaxHp() * (long)eff.getY() / 100L);
            this.lastCygnusTime = System.currentTimeMillis();
            statups9 = new HashMap();
            statups9.put(SecondaryStat.IndieDamR, new Pair(감소량 + eff.getDamage(), (int)this.getBuffLimit(400001044)));
            this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups9, eff, this));
         }
      }

      if (!this.getBuffedValue(33110014) && this.getSkillLevel(33110014) > 0) {
         SkillFactory.getSkill(33110014).getEffect(this.getSkillLevel(33110014)).applyTo(this, true);
      }

      Iterator var9;
      MapleMonster monster;
      Iterator var11;
      SecondaryStatEffect effect;
      if (this.getSkillLevel(2100009) > 0) {
         byte stack = 0;
         effect = SkillFactory.getSkill(2100009).getEffect(1);
         var9 = this.map.getAllMonstersThreadsafe().iterator();

         while(var9.hasNext()) {
            monster = (MapleMonster)var9.next();
            var11 = monster.getIgnitions().iterator();

            while(var11.hasNext()) {
               Ignition zz = (Ignition)var11.next();
               if (zz.getOwnerId() == this.getId() && stack < 5) {
                  ++stack;
               }

               if (stack == 5) {
                  break;
               }
            }

            if (stack == 5) {
               break;
            }
         }

         this.setPoisonStack(stack);
         HashMap<SecondaryStat, Pair<Integer, Integer>> statups5 = new HashMap();
         statups5.put(SecondaryStat.DotBasedBuff, new Pair(Integer.valueOf(stack), 0));
         effect.setDuration(0);
         if (stack > 0) {
            this.client.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups5, effect, this));
         } else {
            this.client.getSession().writeAndFlush(CWvsContext.BuffPacket.cancelBuff(statups5, this));
         }
      }

      MaplePartyCharacter pc;
      Iterator var22;
      int s;
      MapleCharacter owner;
      if (this.getSkillLevel(2300009) > 0) {
         감소량 = 0;
         if (this.getParty() != null) {
            var22 = this.getParty().getMembers().iterator();

            label798:
            while(true) {
               do {
                  do {
                     do {
                        do {
                           if (!var22.hasNext()) {
                              break label798;
                           }

                           pc = (MaplePartyCharacter)var22.next();
                           owner = this.getClient().getChannelServer().getPlayerStorage().getCharacterById(pc.getId());
                        } while(owner == null);
                     } while(!pc.isOnline());
                  } while(owner.getId() == this.getId());
               } while(!owner.getBuffedValue(2321005) && !owner.getBuffedValue(2301004) && !owner.getBuffedValue(2311003) && !owner.getBuffedValue(2311009) && !owner.getBuffedValue(2311009));

               ++감소량;
            }
         }

         if (this.getBuffedValue(2321005) || this.getBuffedValue(2301004) || this.getBuffedValue(2311003) || this.getBuffedValue(2311009) || this.getBuffedValue(2311009)) {
            ++감소량;
         }

         s = SkillFactory.getSkill(2300009).getEffect(1).getX();
         if (this.getSkillLevel(2320013) > 0) {
            s = SkillFactory.getSkill(2320013).getEffect(1).getX();
         }

         if (this.getBuffedEffect(SecondaryStat.BlessingAnsanble) == null) {
            if (감소량 > 0) {
               this.setSkillCustomInfo(2320013, (long)(감소량 * s), 0L);
               SkillFactory.getSkill(2300009).getEffect(1).applyTo(this);
            }
         } else if (this.getBuffedValue(SecondaryStat.BlessingAnsanble) != 감소량 * s) {
            this.setSkillCustomInfo(2320013, (long)(감소량 * s), 0L);
            if (this.getBuffedEffect(SecondaryStat.BlessingAnsanble) != null) {
               this.cancelEffect(this.getBuffedEffect(SecondaryStat.BlessingAnsanble));
            }

            if (감소량 > 0) {
               SkillFactory.getSkill(2300009).getEffect(1).applyTo(this);
            }
         }
      }

      MapleCharacter owner;
      if (this.getBuffedValue(51111008)) {
         if (this.getBuffedOwner(51111008) == this.getId()) {
            short needmp = this.getBuffedEffect(51111008).getMPCon();
            if (this.getStat().getMp() > (long)needmp) {
               this.addMP((long)(-this.getBuffedEffect(51111008).getMPCon()));
               this.PartyBuffCheck(SecondaryStat.MichaelSoulLink, 51111008);
            } else {
               this.cancelEffect(this.getBuffedEffect(51111008));
            }
         } else {
            owner = this.getMap().getCharacter(this.getBuffedOwner(51111008));
            if (owner == null) {
               this.cancelEffect(this.getBuffedEffect(51111008));
            } else if (!owner.getBuffedValue(51111008)) {
               this.cancelEffect(this.getBuffedEffect(51111008));
            }
         }
      }

      if (GameConstants.isMichael(this.getJob()) && this.getBuffedValue(400011011) && this.getRhoAias() > 0 && this.getParty() != null) {
         effect = this.getBuffedEffect(SecondaryStat.RhoAias);
         new HashMap();
         if (effect != null) {
            var9 = this.getParty().getMembers().iterator();

            label752:
            while(true) {
               while(true) {
                  MaplePartyCharacter pc;
                  MapleCharacter victim;
                  do {
                     do {
                        do {
                           do {
                              if (!var9.hasNext()) {
                                 break label752;
                              }

                              pc = (MaplePartyCharacter)var9.next();
                           } while(!pc.isOnline());
                        } while(pc.getMapid() != this.getMapId());
                     } while(pc.getChannel() != this.getClient().getChannel());
                  } while((victim = this.getClient().getChannelServer().getPlayerStorage().getCharacterByName(pc.getName())) == null);

                  if (this.getTruePosition().x + effect.getLt().x < victim.getTruePosition().x && this.getTruePosition().x - effect.getLt().x > victim.getTruePosition().x && this.getTruePosition().y + effect.getLt().y < victim.getTruePosition().y && this.getTruePosition().y - effect.getLt().y > victim.getTruePosition().y) {
                     if (!victim.getBuffedValue(400011011)) {
                        long duration = this.getBuffLimit(400011011);
                        effect.applyTo(this, victim, false, this.getPosition(), (int)duration, (byte)0, false);
                     }
                  } else if (victim.getBuffedValue(400011011)) {
                     victim.cancelEffect(victim.getBuffedEffect(400011011));
                  }
               }
            }
         }
      }

      if (this.getBuffedValue(400011127) && this.getBuffedValue(SecondaryStat.IndieBarrier) != null) {
         감소량 = (int)this.getSkillCustomValue0(400011127);
         long du = this.getBuffLimit(SecondaryStat.IndieBarrier, 400011127);
         this.setSkillCustomInfo(400011127, this.getSkillCustomValue0(400011127) - (long)(감소량 / 100 * 7), 0L);
         this.getBuffedEffect(400011127).applyTo(this, false, (int)du);
      }

      if (this.getSkillCustomValue(15003) == null) {
         this.에테르핸들러(this, 5, 0, false);
         this.setSkillCustomInfo(15003, 0L, 10000L);
      }

      if (this.getBuffedValue(151111005) && this.getParty() != null) {
         effect = this.getBuffedEffect(SecondaryStat.Novility);
         var22 = this.getParty().getMembers().iterator();

         label717:
         while(true) {
            while(true) {
               do {
                  do {
                     do {
                        do {
                           if (!var22.hasNext()) {
                              break label717;
                           }

                           pc = (MaplePartyCharacter)var22.next();
                        } while(!pc.isOnline());
                     } while(pc.getMapid() != this.getMapId());
                  } while(pc.getChannel() != this.getClient().getChannel());
               } while((owner = this.getClient().getChannelServer().getPlayerStorage().getCharacterByName(pc.getName())) == null);

               if (this.getTruePosition().x + effect.getLt().x < owner.getTruePosition().x && this.getTruePosition().x - effect.getLt().x > owner.getTruePosition().x && this.getTruePosition().y + effect.getLt().y < owner.getTruePosition().y && this.getTruePosition().y - effect.getLt().y > owner.getTruePosition().y) {
                  if (!owner.getBuffedValue(151111005)) {
                     long duration = this.getBuffLimit(151111005);
                     effect.applyTo(this, owner, false, this.getPosition(), (int)duration, (byte)0, false);
                  }
               } else if (owner.getBuffedValue(151111005)) {
                  owner.cancelEffect(owner.getBuffedEffect(151111005));
               }
            }
         }
      }

      if (GameConstants.isAdel(this.getJob())) {
         if (this.getSkillCustomValue0(151121004) > 0L) {
            this.addSkillCustomInfo(151121004, -1L);
         }
      } else if (GameConstants.isLara(this.getJob()) && this.getSkillCustomValue0(162121022) > 0L) {
         this.addSkillCustomInfo(162121022, -1L);
      }

      if (this.getBuffedEffect(SecondaryStat.IndieReduceCooltime) != null) {
         var7 = this.getCooldowns().iterator();

         while(var7.hasNext()) {
            MapleCoolDownValueHolder cooldown = (MapleCoolDownValueHolder)var7.next();
            if (!SkillFactory.getSkill(cooldown.skillId).isNotCooltimeReset()) {
               this.changeCooldown(cooldown.skillId, -this.getBuffedValue(SecondaryStat.IndieReduceCooltime) * 100);
            }
         }
      }

      if (this.getBuffedEffect(SecondaryStat.DotHealHPPerSecond) != null) {
         this.addHP(this.getStat().getCurrentMaxHp() / 100L * (long)this.getBuffedValue(SecondaryStat.DotHealHPPerSecond));
      }

      if (this.getBuffedEffect(SecondaryStat.DotHealMPPerSecond) != null) {
         this.addMP(this.getStat().getCurrentMaxMp(this) / 100L * (long)this.getBuffedValue(SecondaryStat.DotHealMPPerSecond));
      }

      MapleSummon summon;
      if (this.getBuffedValue(152101000) && (summon = this.getSummon(152101000)) != null) {
         this.client.send(CField.SummonPacket.ElementalRadiance(summon, 3));
      }

      SecondaryStatEffect effect;
      ArrayList stackskill;
      if (this.getBuffedValue(152111003) && this.getSkillLevel(152120008) > 0 && this.getSkillCustomValue(152111003) == null) {
         stackskill = new ArrayList();
         MapleAtom atom = new MapleAtom(false, this.getId(), 40, false, 152120008, this.getTruePosition().x, this.getTruePosition().y);
         effect = SkillFactory.getSkill(152120008).getEffect(this.getSkillLevel(152120008));
         int givebuffsize = 0;
         if (this.getParty() != null) {
            var11 = this.getParty().getMembers().iterator();

            while(var11.hasNext()) {
               MaplePartyCharacter pc = (MaplePartyCharacter)var11.next();
               MapleCharacter victim;
               if (pc.isOnline() && pc.getMapid() == this.getMapId() && pc.getChannel() == this.getClient().getChannel() && (victim = this.getClient().getChannelServer().getPlayerStorage().getCharacterByName(pc.getName())) != null && victim.getId() != this.getId() && this.getTruePosition().x + effect.getLt().x < victim.getTruePosition().x && this.getTruePosition().x - effect.getLt().x > victim.getTruePosition().x && this.getTruePosition().y + effect.getLt().y < victim.getTruePosition().y && this.getTruePosition().y - effect.getLt().y > victim.getTruePosition().y) {
                  server.maps.ForceAtom forceAtom = new server.maps.ForceAtom(1, Randomizer.rand(45, 49), Randomizer.rand(5, 6), Randomizer.rand(54, 184), 0);
                  stackskill.add(victim.getId());
                  atom.addForceAtom(forceAtom);
                  ++givebuffsize;
               }
            }

            if (!atom.getForceAtoms().isEmpty()) {
               atom.setDwTargets(stackskill);
               this.getMap().spawnMapleAtom(atom);
            }
         }

         stackskill = new ArrayList();
         atom = new MapleAtom(false, this.getId(), 40, true, 152120008, this.getTruePosition().x, this.getTruePosition().y);
         var11 = this.getMap().getAllMonster().iterator();

         while(var11.hasNext()) {
            MapleMonster monster = (MapleMonster)var11.next();
            if (this.getTruePosition().x + effect.getLt().x < monster.getTruePosition().x && this.getTruePosition().x - effect.getLt().x > monster.getTruePosition().x && this.getTruePosition().y + effect.getLt().y < monster.getTruePosition().y && this.getTruePosition().y - effect.getLt().y > monster.getTruePosition().y) {
               server.maps.ForceAtom forceAtom = new server.maps.ForceAtom(1, Randomizer.rand(45, 49), Randomizer.rand(5, 6), Randomizer.rand(54, 184), 0);
               stackskill.add(monster.getObjectId());
               atom.addForceAtom(forceAtom);
               ++givebuffsize;
               if (givebuffsize >= effect.getMobCount()) {
                  break;
               }
            }
         }

         if (!atom.getForceAtoms().isEmpty()) {
            atom.setDwTargets(stackskill);
            this.getMap().spawnMapleAtom(atom);
         }

         this.setSkillCustomInfo(152111003, 0L, 2000L);
      }

      if (GameConstants.isKadena(this.getJob()) && this.getSkillLevel(400041074) > 0 && this.getSkillCustomValue(400441774) == null) {
         effect = SkillFactory.getSkill(400041074).getEffect(this.getSkillLevel(400041074));
         PlayerHandler.Vmatrixstackbuff(this.getClient(), false, (LittleEndianAccessor)null);
         this.setSkillCustomInfo(400441774, 0L, (long)(effect.getX() * 1000));
      }

      if (GameConstants.isAngelicBuster(this.getJob())) {
         if (this.getBuffedValue(400051046) && this.getSkillCustomValue(400151046) == null) {
            effect = this.getBuffedEffect(400051046);
            MapleSummon summon2 = this.getSummon(400051046);
            if (summon2 != null) {
               if (!summon2.isSpecialSkill() && summon2.getEnergy() < 8) {
                  summon2.setEnergy(summon2.getEnergy() + 1);
                  this.getMap().broadcastMessage(CField.SummonPacket.ElementalRadiance(summon2, 2));
                  this.getClient().getSession().writeAndFlush(CField.SummonPacket.specialSummon(summon2, 2));
               }

               this.setSkillCustomInfo(400151046, 0L, (long)(effect.getU() * 1000));
            }
         }
      } else if (GameConstants.isZero(this.getJob())) {
         if (this.getSkillCustomValue0(101000201) > 0L) {
            감소량 = this.getSkillLevel(80000406) > 0 ? this.getSkillLevel(80000406) : 0;
            s = (this.getGender() == 1 ? 100 : 100) + 감소량 * 10 + (this.getGender() == 1 && this.getSkillLevel(101100203) > 0 ? 30 : 0);
            this.setSkillCustomInfo(101000201, this.getSkillCustomValue0(101000201) + 20L, 0L);
            if (this.getSkillCustomValue0(101000201) > (long)s) {
               this.setSkillCustomInfo(101000201, (long)s, 0L);
            }
         }

         if (this.getSkillCustomValue(1011135) == null) {
            if (this.getSkillCustomValue0(101112) > 0L && this.getGender() == 0 && this.getSkillCustomValue0(101112) < this.getStat().getCurrentMaxHp()) {
               감소량 = (int)(this.getSkillCustomValue0(101114) / 100L * 16L);
               this.setSkillCustomInfo(101112, this.getSkillCustomValue0(101112) + (long)감소량, 0L);
               if (this.getSkillCustomValue0(101112) >= this.getStat().getCurrentMaxHp()) {
                  this.setSkillCustomInfo(101112, (long)((int)this.getStat().getCurrentMaxHp()), 0L);
               }

               this.getClient().send(CField.ZeroTag(this, this.getGender(), (int)this.getSkillCustomValue0(101112), (int)this.getSkillCustomValue0(101115)));
            }

            if (this.getSkillCustomValue0(101113) > 0L && this.getGender() == 1 && this.getSkillCustomValue0(101113) < this.getStat().getCurrentMaxHp()) {
               감소량 = (int)(this.getSkillCustomValue0(101115) / 100L * 16L);
               this.setSkillCustomInfo(101113, this.getSkillCustomValue0(101113) + (long)감소량, 0L);
               if (this.getSkillCustomValue0(101113) >= this.getStat().getCurrentMaxHp()) {
                  this.setSkillCustomInfo(101113, (long)((int)this.getStat().getCurrentMaxHp()), 0L);
               }

               this.getClient().send(CField.ZeroTag(this, this.getGender(), (int)this.getSkillCustomValue0(101113), (int)this.getSkillCustomValue0(101114)));
            }

            this.setSkillCustomInfo(1011135, 0L, 4000L);
         }
      }

      if (this.getSkillCustomValue0(24209) > 0L && (this.getMapId() == 450008950 || this.getMapId() == 450008350)) {
         this.getMap().broadcastMessage(MobPacket.BossWill.AttackPoison(this, (int)this.getSkillCustomValue0(24219), 0, 0, 0));
         var7 = this.getMap().getAllChracater().iterator();

         while(var7.hasNext()) {
            MapleCharacter achr = (MapleCharacter)var7.next();
            if (this.getPosition().x - 150 < achr.getPosition().x && this.getPosition().x + 150 > achr.getPosition().x && achr.getId() != this.getId() && achr.isAlive() && achr.getBuffedEffect(SecondaryStat.NotDamaged) == null && achr.getBuffedEffect(SecondaryStat.IndieNotDamaged) == null) {
               achr.addHP(-(achr.getStat().getCurrentMaxHp() / 100L) * 44L);
               achr.getMap().broadcastMessage(CField.playerDamaged(achr.getId(), (int)(achr.getStat().getCurrentMaxHp() / 100L) * 44));
            }
         }
      }

      boolean givebuff;
      boolean connect;
      if (this.getBuffedValue(2311003)) {
         connect = this.getSkillCustomValue0(2311004) == 1L;
         givebuff = false;
         int ownerid = (int)this.getSkillCustomValue0(2311003);
         if (ownerid != this.getId()) {
            owner = this.getMap().getCharacterById(ownerid);
            SecondaryStatEffect effect = SkillFactory.getSkill(2311003).getEffect(20);
            if (owner != null && owner.getParty().getId() == this.getParty().getId()) {
               Rectangle box = this.getBuffedEffect(2311003).calculateBoundingBox(owner.getPosition(), owner.isFacingLeft());
               if ((owner.getMapId() != this.getMapId() || !box.contains(this.getPosition())) && this.getSkillCustomValue0(2311004) == 0L) {
                  givebuff = true;
                  this.setSkillCustomInfo(2311004, 1L, 0L);
                  this.dropMessage(5, "홀리 심볼을 시전한 캐릭터의 근처를 벗어났습니다.");
               } else if (owner.getMapId() == this.getMapId() && box.contains(this.getPosition()) && this.getSkillCustomValue0(2311004) == 1L) {
                  this.dropMessage(5, "홀리 심볼을 시전한 캐릭터의 근처를 들어왔습니다.");
                  this.setSkillCustomInfo(2311004, 0L, 0L);
                  givebuff = true;
               }
            } else if ((this.getParty() == null || owner == null) && this.getSkillCustomValue0(2311004) == 0L) {
               givebuff = true;
               this.setSkillCustomInfo(2311004, 1L, 0L);
               this.dropMessage(5, "홀리 심볼을 시전한 캐릭터 근처를 벗어났습니다.");
            }

            if (givebuff) {
               int du = (int)this.getBuffLimit(2311003);
               if (owner == null) {
                  effect.applyTo(this, this, this.getSkillCustomValue0(2311004) == 0L, this.getPosition(), du, (byte)0, false);
               } else {
                  effect.applyTo(owner, this, this.getSkillCustomValue0(2311004) == 0L, this.getPosition(), du, (byte)0, false);
               }
            }
         }
      }

      if (this.getBuffedValue(400011021)) {
         owner = this.getMap().getCharacterById(this.getBuffedOwner(400011021));
         givebuff = false;
         effect = SkillFactory.getSkill(400011003).getEffect(owner.getSkillLevel(400011003));
         if (owner != null && owner.getParty().getId() == this.getParty().getId()) {
            if (owner.getMapId() != this.getMapId() || owner.getTruePosition().x + effect.getLt().x >= this.getTruePosition().x || owner.getTruePosition().x - effect.getLt().x <= this.getTruePosition().x) {
               givebuff = true;
            }
         } else if (this.getParty() == null || owner == null) {
            givebuff = true;
         }

         if (givebuff) {
            owner.removeSkillCustomInfo(400011003);
            HashMap<SecondaryStat, Pair<Integer, Integer>> localstatups = new HashMap();
            localstatups.clear();
            localstatups.put(SecondaryStat.HolyUnity, new Pair(0, (int)owner.getBuffLimit(400011003)));
            owner.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(localstatups, effect, owner));

            while(this.getBuffedValue(400011021)) {
               this.cancelEffect(this.getBuffedEffect(400011021));
            }
         }
      }

      if (this.getBuffedValue(400011003) && this.getParty() != null) {
         connect = this.getSkillCustomValue0(400011003) > 0L;
         if (!connect) {
            this.ReHolyUnityBuff(this.getBuffedEffect(400011003));
         }
      }

      if (this.getBuffedValue(31121054) && this.getBuffedValue(31121007) && this.getSkillCustomValue(31121007) == null && this.skillisCooling(31121054)) {
         this.changeCooldown(31121054, -2000);
         this.setSkillCustomInfo(31121007, 0L, 3000L);
      }

      if (this.getBuffedValue(400011123) && this.getSkillCustomValue(400011123) == null) {
         stackskill = new ArrayList();
         effect = SkillFactory.getSkill(400011121).getEffect(this.getSkillLevel(400011121));
         var9 = this.getMap().getAllMonster().iterator();

         while(var9.hasNext()) {
            monster = (MapleMonster)var9.next();
            if (this.getTruePosition().x + effect.getLt().x < monster.getTruePosition().x && this.getTruePosition().x - effect.getLt().x > monster.getTruePosition().x && this.getTruePosition().y + effect.getLt().y < monster.getTruePosition().y && this.getTruePosition().y - effect.getLt().y > monster.getTruePosition().y) {
               int size = (int)monster.getCustomValue0(400011121);
               if (size < 6) {
                  monster.setCustomInfo(400011121, size + 1, 0);
               }

               if (monster.getCustomValue(400011122) == null) {
                  monster.setCustomInfo(400011122, 0, 10000);
               }

               stackskill.add(new Triple(monster.getObjectId(), (int)monster.getCustomValue0(400011121), monster.getCustomTime(400011122)));
            }
         }

         if (!stackskill.isEmpty()) {
            this.setSkillCustomInfo(400011123, 0L, 3000L);
            this.getMap().broadcastMessage(CField.getBlizzardTempest(stackskill));
         }
      }

      MapleMonster monster;
      if (this.Stigma >= 7 && this.getBuffedValue(SecondaryStat.NotDamaged) == null && this.getBuffedValue(SecondaryStat.IndieNotDamaged) == null) {
         label861: {
            var7 = this.getMap().getAllMonster().iterator();

            do {
               if (!var7.hasNext()) {
                  break label861;
               }

               monster = (MapleMonster)var7.next();
            } while(monster.getId() != 8880100 && monster.getId() != 8880110 && monster.getId() != 8880101 && monster.getId() != 8880111);

            MobSkill ms = MobSkillFactory.getMobSkill(237, 1);
            ms.applyEffect(this, monster, true, monster.isFacingLeft());
         }
      }

      if (this.getBuffedValue(SecondaryStat.RandAreaAttack) != null && this.getSkillCustomValue(80001762) == null) {
         List<MapleMapObject> objs = this.getMap().getMapObjectsInRange(this.getTruePosition(), 100000.0D, Arrays.asList(MapleMapObjectType.MONSTER));
         if (objs != null && objs.size() >= 1) {
            monster = (MapleMonster)this.getMap().getAllMonster().get(Randomizer.rand(0, objs.size() - 1));
            this.getMap().broadcastMessage(CField.thunderAttack(monster.getPosition().x, monster.getPosition().y, monster.getId()));
            this.setSkillCustomInfo(80001762, 0L, 10000L);
         }
      }

      if (GameConstants.isWildHunter(this.getJob())) {
         if (this.getSkillLevel(400031032) > 0 && this.getSkillCustomValue(400031132) == null) {
            PlayerHandler.Vmatrixstackbuff(this.getClient(), false, (LittleEndianAccessor)null);
            this.setSkillCustomInfo(400031132, 0L, 4500L);
         }
      } else if (GameConstants.isXenon(this.getJob()) && this.getSkillCustomValue(30020232) == null) {
         s = this.level >= 100 ? 20 : (this.level >= 60 ? 15 : (this.level >= 30 ? 10 : 5));
         if (this.getBuffedValue(SecondaryStat.Overload) != null) {
            connect = true;
         }

         if (s > this.getXenonSurplus()) {
            this.gainXenonSurplus((short)1, SkillFactory.getSkill(30020232));
         }

         if (this.getBuffedValue(SecondaryStat.Overload) == null) {
            this.setSkillCustomInfo(30020232, 0L, 4000L);
         } else {
            this.setSkillCustomInfo(30020232, 0L, 2000L);
         }
      } else if (this.getBuffedValue(37000006) && this.getSkillCustomValue(37000007) == null) {
         감소량 = (int)(this.getSkillCustomValue0(37000006) / 100L * (long)this.getBuffedEffect(37000006).getY() + (long)this.getBuffedEffect(37000006).getZ());
         this.setSkillCustomInfo(37000006, this.getSkillCustomValue0(37000006) - (long)감소량, 0L);
         this.setSkillCustomInfo(37000007, 0L, 3000L);
         if (this.getSkillCustomValue0(37000006) <= (long)감소량) {
            this.cancelEffectFromBuffStat(SecondaryStat.RwBarrier, 37000006);
            this.removeSkillCustomInfo(37000006);
         } else {
            statups9 = new HashMap();
            statups9.put(SecondaryStat.RwBarrier, new Pair((int)this.getSkillCustomValue0(37000006), 0));
            this.client.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups9, this.getBuffedEffect(37000006), this.getPlayer()));
         }

         if (this.getSkillLevel(37120049) > 0) {
            this.addHP((long)(감소량 / 2));
         }
      }

      if ((this.getSkillLevel(4200013) > 0 || this.getSkillLevel(422015) > 0) && this.getSkillCustomValue(4220015) == null) {
         effect = SkillFactory.getSkill(4200013).getEffect(this.getSkillLevel(4200013));
         if (this.getSkillLevel(4220015) > 0) {
            effect = SkillFactory.getSkill(4220015).getEffect(this.getSkillLevel(4220015));
         }

         this.criticalGrowing += effect.getX();
         if (this.criticalGrowing + this.getStat().critical_rate >= 100) {
            this.criticalGrowing = 100;
         }

         this.criticalDamageGrowing = Math.min(this.criticalDamageGrowing + effect.getW(), effect.getQ());
         effect.applyTo(this, false, 0);
         if (this.criticalGrowing + this.getStat().critical_rate >= 100 && this.criticalDamageGrowing >= effect.getQ()) {
            this.criticalGrowing = 0;
            this.criticalDamageGrowing = 0;
         }

         this.setSkillCustomInfo(4220015, 0L, 4000L);
      }

      if (GameConstants.isCain(this.getJob())) {
         if (this.getJob() >= 6310 && this.getSkillCustomValue(6310) == null) {
            this.handlePossession(1);
            this.setSkillCustomInfo(6310, 0L, 5000L);
         }

         stackskill = new ArrayList(Arrays.asList(63101004, 63111003, 63121002, 63121040));
         var22 = stackskill.iterator();

         while(var22.hasNext()) {
            Integer skillid = (Integer)var22.next();
            if (this.getSkillLevel(skillid) > 0) {
               this.handleStackskill(skillid, false);
            }
         }
      } else if (GameConstants.isLara(this.getJob())) {
         if (this.getSkillLevel(162101012) > 0) {
            effect = SkillFactory.getSkill(162101012).getEffect(this.getSkillLevel(162101012));
            if (this.getSkillCustomValue(162101112) == null) {
               if (this.getSkillCustomValue0(162101012) < (long)effect.getW2()) {
                  this.addSkillCustomInfo(162101012, 1L);
                  this.setSkillCustomInfo(162101112, 0L, (long)(effect.getZ() * 1000));
               }

               statups9 = new HashMap();
               statups9.put(SecondaryStat.산의씨앗, new Pair((int)this.getSkillCustomValue0(162101012), 0));
               this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups9, effect, this));
            }
         }

         if (this.getSkillLevel(162121042) > 0) {
            effect = SkillFactory.getSkill(162121042).getEffect(this.getSkillLevel(162121042));
            if (this.getSkillCustomValue(162121142) == null) {
               if (this.getSkillCustomValue0(162121042) < (long)effect.getU()) {
                  this.addSkillCustomInfo(162121042, 1L);
                  this.setSkillCustomInfo(162121142, 0L, (long)(effect.getW() * 1000));
               }

               statups9 = new HashMap();
               statups9.put(SecondaryStat.자유로운용맥, new Pair((int)this.getSkillCustomValue0(162121042), 0));
               this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups9, effect, this));
            }
         }
      }

   }

   public void checkMistStatus(MapleMist mist, List<MapleMonster> arrays, long time) {
      Iterator mobs;
      MapleCharacter chr;
      Iterator var13;
      MapleCharacter chr2;
      Iterator var16;
      if (mist.getSource() != null) {
         ArrayList applys;
         boolean heal;
         boolean active;
         SecondaryStatEffect eff;
         MapleMonster mob;
         MapleMonster mob2;
         switch(mist.getSource().getSourceId()) {
         case 2111013:
            mobs = this.map.getAllMonstersThreadsafe().iterator();

            while(true) {
               while(true) {
                  do {
                     if (!mobs.hasNext()) {
                        return;
                     }

                     mob = (MapleMonster)mobs.next();
                  } while(mob == null);

                  if (mist.getBox().contains(mob.getTruePosition()) && !mob.isBuffed(2111013)) {
                     if (mist.getOwner().getId() == this.getId()) {
                        applys = new ArrayList();
                        SecondaryStatEffect effect = SkillFactory.getSkill(2111013).getEffect(this.getSkillLevel(2111013));
                        applys.add(new Pair(MonsterStatus.MS_Burned, new MonsterStatusEffect(2111013, effect.getDOTTime(), 3678551L)));
                        mob.applyStatus(this.getClient(), applys, mist.getSource());
                     }
                  } else {
                     mob.cancelSingleStatus(mob.getBuff(2111003));
                  }
               }
            }
         case 4121015:
            mobs = arrays.iterator();
            List<Pair<MonsterStatus, MonsterStatusEffect>> applys2 = new ArrayList();
            if (this.getSkillLevel(4120046) > 0) {
               applys2.add(new Pair(MonsterStatus.MS_IndiePdr, new MonsterStatusEffect(4121015, mist.getSource().getDuration(), (long)(-mist.getSource().getW() - SkillFactory.getSkill(4120046).getEffect(1).getV()))));
               applys2.add(new Pair(MonsterStatus.MS_Pad, new MonsterStatusEffect(4121015, mist.getSource().getDuration(), (long)(-mist.getSource().getW() - SkillFactory.getSkill(4120046).getEffect(1).getV()))));
            } else {
               applys2.add(new Pair(MonsterStatus.MS_IndiePdr, new MonsterStatusEffect(4121015, mist.getSource().getDuration(), (long)(-mist.getSource().getW()))));
               applys2.add(new Pair(MonsterStatus.MS_Pad, new MonsterStatusEffect(4121015, mist.getSource().getDuration(), (long)(-mist.getSource().getW()))));
            }

            if (this.getSkillLevel(4120047) > 0) {
               applys2.add(new Pair(MonsterStatus.MS_Speed, new MonsterStatusEffect(4121015, mist.getSource().getDuration(), (long)(mist.getSource().getY() - SkillFactory.getSkill(4120047).getEffect(1).getS()))));
            } else {
               applys2.add(new Pair(MonsterStatus.MS_Speed, new MonsterStatusEffect(4121015, mist.getSource().getDuration(), (long)mist.getSource().getY())));
            }

            while(true) {
               do {
                  do {
                     if (!mobs.hasNext()) {
                        return;
                     }

                     mob2 = (MapleMonster)mobs.next();
                  } while(mob2.isBuffed(4121015));
               } while((!mob2.getStats().isBoss() || this.getSkillLevel(4120048) <= 0) && mob2.getStats().isBoss());

               if (mist.getBox().contains(mob2.getTruePosition())) {
                  if (mist.getOwner().getId() == this.getId() && !mob2.isBuffed(4121015)) {
                     mob2.applyStatus(this.getClient(), applys2, mist.getSource());
                  }
               } else if (mob2.isBuffed(4121015)) {
                  mob2.cancelStatus(applys2);
               }
            }
         case 4221006:
            mobs = arrays.iterator();

            while(mobs.hasNext()) {
               mob = (MapleMonster)mobs.next();
               if (!mob.isBuffed(4221006)) {
                  if (mist.getBox().contains(mob.getTruePosition())) {
                     if (mist.getOwner().getId() == this.getId()) {
                        mob.applyStatus(this.getClient(), MonsterStatus.MS_HitCritDamR, new MonsterStatusEffect(4221006, mist.getSource().getDuration()), mist.getSource().getX(), mist.getSource());
                     }
                  } else {
                     mob.cancelSingleStatus(mob.getBuff(4221006));
                  }
               }
            }

            if (mist.getBox().contains(this.getTruePosition())) {
               if (mist.getOwner().getId() == this.getId() && !this.getBuffedValue(4221006)) {
                  SkillFactory.getSkill(4221006).getEffect(mist.getSkillLevel()).applyTo(this, false);
               } else if (mist.getOwner().getParty() != null && mist.getOwner().getParty().getMemberById(this.getId()) != null && !this.getBuffedValue(4221006)) {
                  SkillFactory.getSkill(4221006).getEffect(mist.getSkillLevel()).applyTo(this, false);
               }
            }
            break;
         case 12121005:
            mobs = this.getMap().getAllChracater().iterator();

            while(true) {
               while(mobs.hasNext()) {
                  chr = (MapleCharacter)mobs.next();
                  if (mist.getBox().contains(chr.getTruePosition())) {
                     if (mist.getOwner().getId() == chr.getId()) {
                        SkillFactory.getSkill(mist.getSource().getSourceId()).getEffect(mist.getSkillLevel()).applyTo(chr, false, 4000);
                     } else if (mist.getOwner().getParty() != null && mist.getOwner().getParty().getMemberById(chr.getId()) != null) {
                        SkillFactory.getSkill(mist.getSource().getSourceId()).getEffect(mist.getSkillLevel()).applyTo(chr, false, 4000);
                     }
                  } else if (chr.getBuffedValue(mist.getSource().getSourceId())) {
                     while(chr.getBuffedValue(mist.getSource().getSourceId())) {
                        chr.cancelEffect(chr.getBuffedEffect(mist.getSource().getSourceId()));
                     }
                  }
               }

               return;
            }
         case 21121057:
            eff = SkillFactory.getSkill(mist.getSource().getSourceId()).getEffect(1);
            var13 = this.getMap().getAllChracater().iterator();

            while(true) {
               while(var13.hasNext()) {
                  chr2 = (MapleCharacter)var13.next();
                  if (mist.getOwner().getParty() != null) {
                     Iterator var21 = mist.getOwner().getParty().getMembers().iterator();

                     while(var21.hasNext()) {
                        MaplePartyCharacter chr3 = (MaplePartyCharacter)var21.next();
                        MapleCharacter chr4 = mist.getOwner().getClient().getChannelServer().getPlayerStorage().getCharacterByName(chr3.getName());
                        if (chr4 != null && mist.getBox().contains(chr4.getTruePosition())) {
                           chr4.addMPHP(chr4.getStat().getCurrentMaxHp() / 100L * (long)eff.getW(), chr4.getStat().getCurrentMaxMp(chr4) / 100L * (long)eff.getW());
                           chr4.dispelDebuffs();
                        }
                     }
                  } else if (mist.getOwner().getId() == chr2.getId() && mist.getBox().contains(chr2.getTruePosition())) {
                     chr2.addMPHP(chr2.getStat().getCurrentMaxHp() / 100L * (long)eff.getW(), chr2.getStat().getCurrentMaxMp(chr2) / 100L * (long)eff.getW());
                     chr2.dispelDebuffs();
                  }
               }

               return;
            }
         case 36121007:
            mobs = this.getMap().getAllChracater().iterator();

            while(true) {
               while(true) {
                  do {
                     if (!mobs.hasNext()) {
                        return;
                     }

                     chr = (MapleCharacter)mobs.next();
                  } while(!mist.getBox().contains(chr.getTruePosition()));

                  MapleCoolDownValueHolder cooldown;
                  if (mist.getOwner().getId() == chr.getId()) {
                     var16 = chr.getCooldowns().iterator();

                     while(var16.hasNext()) {
                        cooldown = (MapleCoolDownValueHolder)var16.next();
                        if (cooldown.skillId != 36121007 && !SkillFactory.getSkill(cooldown.skillId).isHyper() && cooldown.skillId < 400000000) {
                           chr.changeCooldown(cooldown.skillId, -2000);
                        }
                     }
                  } else if (mist.getOwner().getParty() != null && mist.getOwner().getParty().getMemberById(chr.getId()) != null && !chr.getBuffedValue(4221006)) {
                     var16 = chr.getCooldowns().iterator();

                     while(var16.hasNext()) {
                        cooldown = (MapleCoolDownValueHolder)var16.next();
                        if (cooldown.skillId != 36121007 && !SkillFactory.getSkill(cooldown.skillId).isHyper() && cooldown.skillId < 400000000) {
                           chr.changeCooldown(cooldown.skillId, -2000);
                        }
                     }
                  }
               }
            }
         case 80001431:
            mobs = arrays.iterator();

            while(mobs.hasNext()) {
               mob = (MapleMonster)mobs.next();
               if (!mob.isBuffed(mist.getSource().getSourceId()) && mist.getBox().contains(mob.getTruePosition()) && !mob.getStats().isBoss() && mist.getOwner().getId() == this.getId()) {
                  applys = new ArrayList();
                  applys.add(new Pair(MonsterStatus.MS_Burned, new MonsterStatusEffect(80001431, mist.getDuration(), mob.getStats().getHp() / 100L * 10L)));
                  mob.applyStatus(this.getClient(), applys, mist.getSource());
               }
            }

            return;
         case 100001261:
            mobs = this.getMap().getAllChracater().iterator();

            while(true) {
               while(true) {
                  do {
                     if (!mobs.hasNext()) {
                        mobs = arrays.iterator();

                        while(mobs.hasNext()) {
                           mob = (MapleMonster)mobs.next();
                           if (mist.getBox().contains(mob.getTruePosition()) && time - mob.lastDistotionTime >= (long)mist.getSource().getSubTime()) {
                              mob.lastDistotionTime = time;
                              mob.dispels();
                              mob.applyStatus(this.getClient(), MonsterStatus.MS_AdddamSkill, new MonsterStatusEffect(100001261, mist.getSource().getSubTime()), mist.getSource().getX(), mist.getSource());
                              if (!mob.getStats().isBoss()) {
                                 mob.applyStatus(this.getClient(), MonsterStatus.MS_Freeze, new MonsterStatusEffect(100001261, mist.getSource().getSubTime()), mist.getSource().getSubTime(), mist.getSource());
                              }
                           }
                        }

                        return;
                     }

                     chr = (MapleCharacter)mobs.next();
                  } while(!mist.getBox().contains(chr.getTruePosition()));

                  if (mist.getOwner().getId() == chr.getId() && !chr.getBuffedValue(mist.getSource().getSourceId())) {
                     SkillFactory.getSkill(mist.getSource().getSourceId()).getEffect(mist.getSkillLevel()).applyTo(chr, false, 4000);
                     if (time - chr.lastDistotionTime >= 4000L) {
                        chr.lastDistotionTime = time;
                        chr.dispelDebuffs();
                        SkillFactory.getSkill(100001261).getEffect(mist.getSkillLevel()).applyTo(chr, false);
                     }
                  } else if (mist.getOwner().getParty() != null && mist.getOwner().getParty().getMemberById(chr.getId()) != null && !chr.getBuffedValue(mist.getSource().getSourceId())) {
                     SkillFactory.getSkill(mist.getSource().getSourceId()).getEffect(mist.getSkillLevel()).applyTo(chr, false, 4000);
                     if (time - chr.lastDistotionTime >= 4000L) {
                        chr.lastDistotionTime = time;
                        chr.dispelDebuffs();
                        SkillFactory.getSkill(100001261).getEffect(mist.getSkillLevel()).applyTo(chr, false);
                     }
                  }
               }
            }
         case 162111000:
            eff = SkillFactory.getSkill(162111001).getEffect(mist.getSkillLevel());
            var13 = this.getMap().getAllChracater().iterator();

            while(var13.hasNext()) {
               chr2 = (MapleCharacter)var13.next();
               if (mist.getBox().contains(chr2.getTruePosition())) {
                  if (mist.getOwner().getId() == chr2.getId()) {
                     eff.applyTo(chr2, false, mist.getSource().getU2() * 1000);
                  } else if (mist.getOwner().getParty() != null && mist.getOwner().getParty().getMemberById(chr2.getId()) != null) {
                     eff.applyTo(chr2, false, mist.getSource().getX() * 1000);
                  }
               }
            }

            return;
         case 162111003:
            eff = SkillFactory.getSkill(162111004).getEffect(mist.getSkillLevel());
            var13 = this.getMap().getAllChracater().iterator();

            while(var13.hasNext()) {
               chr2 = (MapleCharacter)var13.next();
               heal = false;
               active = false;
               if (mist.getBufftime() <= System.currentTimeMillis()) {
                  heal = true;
                  mist.setBufftime(System.currentTimeMillis() + (long)(mist.getSource().getZ() * 1000));
               }

               if (mist.getBox().contains(chr2.getTruePosition())) {
                  if (mist.getOwner().getId() == chr2.getId()) {
                     eff.applyTo(chr2, false, mist.getSource().getW() * 1000);
                     active = true;
                  } else if (mist.getOwner().getParty() != null && mist.getOwner().getParty().getMemberById(chr2.getId()) != null) {
                     eff.applyTo(chr2, false, mist.getSource().getX() * 1000);
                     active = true;
                  }

                  if (heal && active && chr2.getStat().getCurrentMaxHp() > chr2.getStat().getHp()) {
                     chr2.addHP(chr2.getStat().getCurrentMaxHp() * (long)eff.getHp() / 100L);
                     chr2.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr2, 0, 162111003, 10, 0, 0, (byte)(chr2.isFacingLeft() ? 1 : 0), true, chr2.getTruePosition(), (String)null, (Item)null));
                     chr2.getMap().broadcastMessage(chr2, CField.EffectPacket.showEffect(chr2, 0, 162111003, 10, 0, 0, (byte)(chr2.isFacingLeft() ? 1 : 0), false, chr2.getTruePosition(), (String)null, (Item)null), false);
                  }
               }
            }

            return;
         case 162121043:
            eff = SkillFactory.getSkill(162121043).getEffect(mist.getSkillLevel());
            var13 = this.getMap().getAllChracater().iterator();

            while(var13.hasNext()) {
               chr2 = (MapleCharacter)var13.next();
               heal = false;
               active = false;
               if (mist.getBufftime() <= System.currentTimeMillis()) {
                  heal = true;
                  mist.setBufftime(System.currentTimeMillis() + 2000L);
               }

               if (mist.getBox().contains(chr2.getTruePosition())) {
                  if (mist.getOwner().getId() == chr2.getId()) {
                     if (!chr2.getBuffedValue(eff.getSourceId())) {
                        eff.applyTo(chr2, false, 0);
                     }

                     active = true;
                  }

                  if (heal && active) {
                  }
               } else if (chr2.getBuffedValue(eff.getSourceId())) {
                  chr2.cancelEffect(eff);
               }
            }

            var13 = arrays.iterator();

            while(var13.hasNext()) {
               mob2 = (MapleMonster)var13.next();
               if (!mob2.isBuffed(mist.getSource().getSourceId())) {
                  if (mist.getBox().contains(mob2.getTruePosition())) {
                     if (mist.getOwner().getId() == this.getId()) {
                        List<Pair<MonsterStatus, MonsterStatusEffect>> applys3 = new ArrayList();
                        applys3.add(new Pair(MonsterStatus.MS_Unk5, new MonsterStatusEffect(mist.getSource().getSourceId(), mist.getDuration(), (long)(-mist.getSource().getS()))));
                        mob2.applyStatus(this.getClient(), applys3, mist.getSource());
                     }
                  } else {
                     mob2.cancelSingleStatus(mob2.getBuff(mist.getSource().getSourceId()));
                  }
               }
            }

            return;
         case 400001017:
            if (mist.getBox().contains(this.getTruePosition())) {
               if (mist.getOwner().getId() == this.getId()) {
                  SkillFactory.getSkill(400001017).getEffect(mist.getSkillLevel()).applyTo(this, false);
               } else if (mist.getOwner().getParty() != null && mist.getOwner().getParty().getMemberById(this.getId()) != null) {
                  SkillFactory.getSkill(400001017).getEffect(mist.getSkillLevel()).applyTo(this, false);
               }
            }

            mobs = arrays.iterator();

            while(mobs.hasNext()) {
               mob = (MapleMonster)mobs.next();
               if (!mob.isBuffed(mist.getSource().getSourceId())) {
                  if (mist.getBox().contains(mob.getTruePosition())) {
                     if (mist.getOwner().getId() == this.getId()) {
                        applys = new ArrayList();
                        applys.add(new Pair(MonsterStatus.MS_IndiePdr, new MonsterStatusEffect(400001017, 4000, (long)(-mist.getSource().getZ()))));
                        applys.add(new Pair(MonsterStatus.MS_IndieMdr, new MonsterStatusEffect(400001017, 4000, (long)(-mist.getSource().getZ()))));
                        mob.applyStatus(this.getClient(), applys, mist.getSource());
                     }
                  } else {
                     mob.cancelSingleStatus(mob.getBuff(400001017));
                  }
               }
            }

            return;
         case 400031039:
         case 400031040:
            mobs = this.getMap().getAllChracater().iterator();

            while(true) {
               while(true) {
                  while(mobs.hasNext()) {
                     chr = (MapleCharacter)mobs.next();
                     if (mist.getBox().contains(chr.getTruePosition())) {
                        if (mist.getOwner().getId() == chr.getId() && !chr.getBuffedValue(mist.getSource().getSourceId())) {
                           SkillFactory.getSkill(mist.getSource().getSourceId()).getEffect(mist.getSkillLevel()).applyTo(chr, false, 4000);
                        } else if (mist.getOwner().getParty() != null && mist.getOwner().getParty().getMemberById(chr.getId()) != null && !chr.getBuffedValue(mist.getSource().getSourceId())) {
                           SkillFactory.getSkill(mist.getSource().getSourceId()).getEffect(mist.getSkillLevel()).applyTo(chr, false, 4000);
                        }
                     } else if (chr.getBuffedValue(mist.getSource().getSourceId())) {
                        chr.cancelEffectFromBuffStat(SecondaryStat.IndieUnkIllium);
                     }
                  }

                  return;
               }
            }
         }
      } else if (mist.getMobSkill() != null) {
         if (mist.getMobSkill().getSkillId() == 191) {
            MapleMonster mob3 = this.getMap().getMonsterById(8910000);
            if (mob3 == null) {
               mob3 = this.getMap().getMonsterById(8910100);
            }

            if (mob3 != null && mob3.getPosition().x <= mist.getPosition().x + 200 && mob3.getPosition().x >= mist.getPosition().x - 200) {
               EventInstanceManager eim = this.getEventInstance();
               if (eim != null) {
                  if (mist.getMobSkill().getSkillLevel() == 2) {
                     if (eim.getTimeLeft() < 595000L) {
                        eim.restartEventTimer(eim.getTimeLeft() + 5000L, 4);
                     }
                  } else if (mist.getMobSkill().getSkillLevel() == 1 && eim.getTimeLeft() > 5000L) {
                     eim.restartEventTimer(eim.getTimeLeft() - 5000L, 5);
                  }
               }
            }
         } else if (mist.getMobSkill().getSkillId() == 211) {
            mobs = this.getMap().getAllChracater().iterator();

            while(true) {
               do {
                  if (!mobs.hasNext()) {
                     return;
                  }

                  chr = (MapleCharacter)mobs.next();
               } while(!mist.getBox().contains(chr.getTruePosition()));

               var16 = AffectedOtherSkillInfo.getMistAffectedInfo().entrySet().iterator();

               while(var16.hasNext()) {
                  Entry<Integer, AffectedOtherSkillInfo> ao = (Entry)var16.next();
                  if ((Integer)ao.getKey() == mist.getMobSkill().getSkillLevel()) {
                     MobSkill msi = MobSkillFactory.getMobSkill(((AffectedOtherSkillInfo)ao.getValue()).getAffectedOtherSkillID(), ((AffectedOtherSkillInfo)ao.getValue()).getAffectedOtherSkillLev());
                     if (msi != null) {
                        msi.applyEffect(chr, mist.getMob(), true, mist.getMob().isFacingLeft());
                     }
                  }
               }
            }
         } else if (mist.getMobSkill().getSkillId() == 217) {
            Rectangle rec = new Rectangle(mist.getBox().x + 64, mist.getBox().y + 50, 128, 60);
            var13 = this.getMap().getAllChracater().iterator();

            while(var13.hasNext()) {
               chr2 = (MapleCharacter)var13.next();
               if (rec.contains(chr2.getTruePosition())) {
                  MobSkill msi2 = MobSkillFactory.getMobSkill(122, 1);
                  msi2.applyEffect(chr2, mist.getMob(), true, mist.getMob().isFacingLeft());
               }
            }
         }
      }

   }

   public void handleSummons(long time) {
      MapleSummon summon;
      if ((summon = this.getSummon(400051022)) != null) {
         int size = 0;
         Iterator var5 = this.getMap().getAllSummonsThreadsafe().iterator();

         while(var5.hasNext()) {
            MapleSummon sum = (MapleSummon)var5.next();
            if (sum.getOwner().getId() == this.getId() && summon.getSkill() == 400051023) {
               ++size;
            }
         }

         SecondaryStatEffect bir = SkillFactory.getSkill(400051022).getEffect(summon.getSkillLevel());
         SecondaryStatEffect birz = SkillFactory.getSkill(400051023).getEffect(summon.getSkillLevel());
         if (size < bir.getY()) {
            for(int i = 0; i < bir.getX(); ++i) {
               MapleSummon bird = new MapleSummon(this, birz, summon.getTruePosition(), SummonMovementType.BIRD_FOLLOW2);
               this.getMap().spawnSummon(bird, bir.getW() * 1000);
               this.addSummon(bird);
            }
         }
      }

      SecondaryStatEffect effect;
      if ((summon = this.getSummon(400041044)) != null && summon.getOwner().getParty() != null && this.getParty() != null && summon.getOwner().getParty().getId() == this.getParty().getId()) {
         effect = SkillFactory.getSkill(400041047).getEffect(summon.getSkillLevel());
         Rectangle box = new Rectangle(summon.getTruePosition().x - 320, summon.getTruePosition().y - 490, 640, 530);
         if (box.contains(this.getTruePosition()) && this.getBuffedValue(SecondaryStat.IndieDamR, 400041047) == null) {
            effect.applyTo(this, false);
         }
      }

      if (this.getBuffedValue(400011077)) {
         effect = SkillFactory.getSkill(400011077).getEffect(this.getSkillLevel(400011077));
         if (this.lastNemeaAttackTime == 0L) {
            this.lastNemeaAttackTime = System.currentTimeMillis();
         }

         if ((summon = this.getSummon(400011077)) != null && time - this.lastNemeaAttackTime >= (long)(effect.getX() * 1000)) {
            this.lastNemeaAttackTime = System.currentTimeMillis();
            this.getMap().broadcastMessage(CField.SummonPacket.DeathAttack(summon));
         } else if ((summon = this.getSummon(400011078)) != null && time - this.lastGerionAttackTime >= (long)(effect.getZ() * 1000)) {
            this.lastGerionAttackTime = System.currentTimeMillis();
            this.getMap().broadcastMessage(CField.SummonPacket.DeathAttack(summon));
         }
      }

   }

   public void handleHealSkills(long time) {
      if (this.getSkillLevel(31110009) > 0 && time - this.lastDrainAuraTime >= 4000L) {
         this.lastDrainAuraTime = time;
         this.addMP((long)SkillFactory.getSkill(31110009).getEffect(this.getSkillLevel(31110009)).getY(), true);
      }

      if (this.getSkillLevel(32101009) > 0 && time - this.lastDrainAuraTime >= 4000L) {
         this.lastDrainAuraTime = time;
         this.addHP(this.getStat().getCurrentMaxHp() * (long)SkillFactory.getSkill(32101009).getEffect(this.getSkillLevel(32101009)).getY() / 100L + (long)SkillFactory.getSkill(32101009).getEffect(this.getSkillLevel(32101009)).getHp());
      }

      SecondaryStatEffect selfRecovery;
      if (this.getSkillLevel(5100013) > 0) {
         selfRecovery = SkillFactory.getSkill(5100013).getEffect(this.getSkillLevel(5100013));
         if (this.lastHealTime == 0L) {
            this.lastHealTime = System.currentTimeMillis();
         }

         if (time - this.lastHealTime >= (long)(selfRecovery.getW() * 1000)) {
            this.lastHealTime = System.currentTimeMillis();
            this.addMPHP(this.getStat().getCurrentMaxHp() * (long)selfRecovery.getX() / 100L, this.getStat().getCurrentMaxMp(this) * (long)selfRecovery.getX() / 100L);
         }
      } else if (this.getSkillLevel(11110025) > 0) {
         selfRecovery = SkillFactory.getSkill(11110025).getEffect(this.getSkillLevel(11110025));
         if (this.lastHealTime == 0L) {
            this.lastHealTime = System.currentTimeMillis();
         }

         if (time - this.lastHealTime >= (long)(selfRecovery.getW() * 1000)) {
            this.lastHealTime = System.currentTimeMillis();
            this.addHP(this.getStat().getCurrentMaxHp() * (long)selfRecovery.getY() / 100L);
         }
      } else if (this.getSkillLevel(51110000) > 0) {
         selfRecovery = SkillFactory.getSkill(51110000).getEffect(this.getSkillLevel(51110000));
         if (this.lastHealTime == 0L) {
            this.lastHealTime = System.currentTimeMillis();
         }

         if (time - this.lastHealTime >= 4000L) {
            this.lastHealTime = System.currentTimeMillis();
            this.addMPHP((long)selfRecovery.getHp(), (long)selfRecovery.getMp());
         }
      } else if (this.getSkillLevel(61110006) > 0) {
         selfRecovery = SkillFactory.getSkill(61110006).getEffect(this.getSkillLevel(61110006));
         if (this.lastHealTime == 0L) {
            this.lastHealTime = System.currentTimeMillis();
         }

         if (time - this.lastHealTime >= (long)(selfRecovery.getW() * 1000)) {
            this.lastHealTime = System.currentTimeMillis();
            this.addMPHP(this.getStat().getCurrentMaxHp() * (long)selfRecovery.getX() / 100L, this.getStat().getCurrentMaxMp(this) * (long)selfRecovery.getX() / 100L);
         }
      }

      if (this.getBuffedValue(400041029)) {
         selfRecovery = SkillFactory.getSkill(400041029).getEffect(this.getSkillLevel(400041029));
         int consumeMP = (int)((double)this.getStat().getMaxMp() * ((double)selfRecovery.getQ() / 100.0D)) + selfRecovery.getY();
         if (this.getStat().getMp() < (long)consumeMP) {
            this.cancelEffectFromBuffStat(SecondaryStat.Overload);
         } else {
            this.addMP((long)(-consumeMP));
         }
      }

      if ((this.getBuffedValue(32111012) || this.getBuffedValue(400021006)) && this.getSkillCustomValue(32111112) == null) {
         int activeSkillid = this.getBuffedValue(400021006) ? 400021006 : 32111012;
         if (this.getBuffedOwner(activeSkillid) == this.getId() && this.getSkillLevel(32120062) > 0) {
            if (this.getParty() != null) {
               Iterator var12 = this.getParty().getMembers().iterator();

               while(var12.hasNext()) {
                  MaplePartyCharacter chr1 = (MaplePartyCharacter)var12.next();
                  MapleCharacter curChar = this.getClient().getChannelServer().getPlayerStorage().getCharacterById(chr1.getId());
                  if (curChar != null && curChar.getBuffedValue(activeSkillid)) {
                     Map<SecondaryStat, Pair<Integer, Integer>> statupz = new HashMap();
                     Iterator<Entry<SecondaryStat, MapleDiseases>> iterator2 = curChar.getDiseases().entrySet().iterator();
                     if (iterator2.hasNext()) {
                        Entry<SecondaryStat, MapleDiseases> d = (Entry)iterator2.next();
                        curChar.dispelDebuff(d);
                        statupz.put((SecondaryStat)d.getKey(), new Pair(((MapleDiseases)d.getValue()).getValue(), ((MapleDiseases)d.getValue()).getDuration()));
                     }

                     if (!statupz.isEmpty()) {
                        curChar.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(curChar, 0, 32120062, 4, 0, 0, (byte)(curChar.isFacingLeft() ? 1 : 0), true, curChar.getTruePosition(), (String)null, (Item)null));
                        curChar.getMap().broadcastMessage(curChar, CField.EffectPacket.showEffect(curChar, 0, 32120062, 4, 0, 0, (byte)(curChar.isFacingLeft() ? 1 : 0), false, curChar.getTruePosition(), (String)null, (Item)null), false);
                        curChar.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.cancelBuff(statupz, curChar));
                        curChar.getMap().broadcastMessage(curChar, CWvsContext.BuffPacket.cancelForeignBuff(curChar, statupz), false);
                     }
                  }
               }
            } else {
               Map<SecondaryStat, Pair<Integer, Integer>> statupz2 = new HashMap();
               Iterator<Entry<SecondaryStat, MapleDiseases>> iterator3 = this.getDiseases().entrySet().iterator();
               if (iterator3.hasNext()) {
                  Entry<SecondaryStat, MapleDiseases> d2 = (Entry)iterator3.next();
                  this.dispelDebuff(d2);
                  statupz2.put((SecondaryStat)d2.getKey(), new Pair(((MapleDiseases)d2.getValue()).getValue(), ((MapleDiseases)d2.getValue()).getDuration()));
               }

               if (!statupz2.isEmpty()) {
                  this.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(this, 0, 32120062, 4, 0, 0, (byte)(this.isFacingLeft() ? 1 : 0), true, this.getTruePosition(), (String)null, (Item)null));
                  this.getMap().broadcastMessage(this, CField.EffectPacket.showEffect(this, 0, 32120062, 4, 0, 0, (byte)(this.isFacingLeft() ? 1 : 0), false, this.getTruePosition(), (String)null, (Item)null), false);
                  this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.cancelBuff(statupz2, this));
                  this.getMap().broadcastMessage(this, CWvsContext.BuffPacket.cancelForeignBuff(this, statupz2), false);
               }
            }

            this.setSkillCustomInfo(32111112, 0L, 5000L);
         }
      }

      if (this.getBuffedValue(400021006)) {
         this.addMP((long)(-this.getBuffedEffect(400021006).getMPCon()));
      }

      SecondaryStat[] array = new SecondaryStat[]{SecondaryStat.YellowAura, SecondaryStat.DrainAura, SecondaryStat.BlueAura, SecondaryStat.DarkAura, SecondaryStat.DebuffAura, SecondaryStat.IceAura, SecondaryStat.FireAura};
      SecondaryStat[] var15 = array;
      int var17 = array.length;

      for(int var18 = 0; var18 < var17; ++var18) {
         SecondaryStat mpCon = var15[var18];
         if (this.getBuffedValue(mpCon) != null && this.getBuffedOwner(this.getBuffedEffect(mpCon).getSourceId()) == this.getId() && !this.getBuffedValue(400021006)) {
            this.addMP((long)(-this.getBuffedEffect(mpCon).getMPCon()));
         }
      }

   }

   public void handleSecondaryStats(long time, boolean force) {
      Map<SecondaryStatEffect, List<SecondaryStat>> stats = new ConcurrentHashMap();
      Iterator iter = this.effects.iterator();

      while(true) {
         while(true) {
            SecondaryStat stat;
            SecondaryStatValueHolder vh;
            SecondaryStatEffect eff;
            long remainDuration;
            label59:
            do {
               while(iter.hasNext()) {
                  Pair<SecondaryStat, SecondaryStatValueHolder> effect = (Pair)iter.next();
                  stat = (SecondaryStat)effect.left;
                  vh = (SecondaryStatValueHolder)effect.right;
                  if (stat != null && vh != null) {
                     eff = vh.effect;
                     remainDuration = (long)vh.localDuration - (time - vh.startTime);
                     continue label59;
                  }

                  iter.remove();
               }

               if (!stats.isEmpty()) {
                  Iterator var15 = stats.entrySet().iterator();

                  while(var15.hasNext()) {
                     Entry<SecondaryStatEffect, List<SecondaryStat>> stat2 = (Entry)var15.next();
                     if (this.client.getChannelServer() != null && this.client.getChannelServer().getPlayerStorage().getCharacterById(this.id) != null) {
                        this.cancelEffect((SecondaryStatEffect)stat2.getKey(), (List)stat2.getValue(), false, true);
                     }
                  }
               }

               return;
            } while(!force && (remainDuration > 0L || vh.localDuration == 0));

            if (stats.containsKey(eff)) {
               SecondaryStatEffect sf = null;
               Iterator var13 = stats.entrySet().iterator();

               while(var13.hasNext()) {
                  Entry<SecondaryStatEffect, List<SecondaryStat>> z = (Entry)var13.next();
                  if (eff == z.getKey()) {
                     sf = (SecondaryStatEffect)z.getKey();
                     break;
                  }
               }

               if (sf != null && sf.getSourceId() == eff.getSourceId()) {
                  ((List)stats.get(eff)).add(stat);
               }
            } else {
               stats.put(eff, new ArrayList(Collections.singleton(stat)));
            }
         }
      }
   }

   public MapleCharacter getPlayer() {
      return this;
   }

   public static MapleCharacter getDefault(MapleClient client, LoginInformationProvider.JobType type) {
      MapleCharacter ret = new MapleCharacter(false, false);
      ret.client = client;
      ret.map = null;
      ret.exp = 0L;
      ret.gmLevel = 0;
      ret.job = (short)type.id;
      ret.meso = 0L;
      ret.level = 1;
      ret.remainingAp = 0;
      ret.fame = 0;
      ret.accountid = client.getAccID();
      ret.buddylist = new BuddyList((byte)20);
      ret.unions = new UnionList();
      ret.stats.str = 12;
      ret.stats.dex = 5;
      ret.stats.int_ = 4;
      ret.stats.luk = 4;
      ret.stats.maxhp = 50L;
      ret.stats.hp = 50L;
      ret.stats.maxmp = 50L;
      ret.stats.mp = 50L;
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
         ps.setInt(1, ret.accountid);
         rs = ps.executeQuery();
         if (rs.next()) {
            ret.client.setAccountName(rs.getString("name"));
            ret.nxcredit = rs.getInt("nxCredit");
            ret.acash = 0;
            ret.maplepoints = rs.getInt("mPoints");
            ret.points = rs.getInt("points");
            ret.vpoints = rs.getInt("vpoints");
         }

         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var15) {
         System.err.println("Error getting character default" + var15);
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
         }

      }

      return ret;
   }

   public static final MapleCharacter ReconstructChr(CharacterTransfer ct, MapleClient client, boolean isChannel) {
      MapleCharacter ret = new MapleCharacter(true, false);
      ret.client = client;
      if (!isChannel) {
         ret.client.setChannel(ct.channel);
      }

      ret.id = ct.characterid;
      ret.name = ct.name;
      ret.level = ct.level;
      ret.fame = ct.fame;
      ret.setCalcDamage(new CalcDamage());
      ret.stats.str = ct.str;
      ret.stats.dex = ct.dex;
      ret.stats.int_ = ct.int_;
      ret.stats.luk = ct.luk;
      ret.stats.maxhp = ct.maxhp;
      ret.stats.maxmp = ct.maxmp;
      ret.stats.hp = ct.hp;
      ret.stats.mp = ct.mp;
      ret.customValue.putAll(ct.customValue);
      ret.customInfo.putAll(ct.customInfo);
      ret.chalktext = ct.chalkboard;
      ret.gmLevel = ct.gmLevel;
      ret.LinkMobCount = ct.LinkMobCount;
      ret.exp = ct.exp;
      ret.hpApUsed = ct.hpApUsed;
      ret.remainingSp = ct.remainingSp;
      ret.remainingAp = ct.remainingAp;
      ret.meso = ct.meso;
      ret.stolenSkills = ct.stolenSkills;
      ret.skinColor = ct.skinColor;
      ret.secondSkinColor = ct.secondSkinColor;
      ret.gender = ct.gender;
      ret.secondgender = ct.secondgender;
      ret.job = ct.job;
      ret.hair = ct.hair;
      ret.secondhair = ct.secondhair;
      ret.face = ct.face;
      ret.secondface = ct.secondface;
      ret.demonMarking = ct.demonMarking;
      ret.accountid = ct.accountid;
      ret.totalWins = ct.totalWins;
      ret.totalLosses = ct.totalLosses;
      client.setAccID(ct.accountid);
      ret.mapid = ct.mapid;
      ret.initialSpawnPoint = ct.initialSpawnPoint;
      ret.world = ct.world;
      ret.guildid = ct.guildid;
      ret.guildrank = ct.guildrank;
      ret.guildContribution = ct.guildContribution;
      ret.lastattendance = ct.lastattendance;
      ret.allianceRank = ct.alliancerank;
      ret.points = ct.points;
      ret.vpoints = ct.vpoints;
      ret.fairyExp = ct.fairyExp;
      ret.cardStack = ct.cardStack;
      ret.marriageId = ct.marriageId;
      ret.currentrep = ct.currentrep;
      ret.totalrep = ct.totalrep;
      ret.pvpExp = ct.pvpExp;
      ret.pvpPoints = ct.pvpPoints;
      ret.reborns = ct.reborns;
      ret.apstorage = ct.apstorage;
      if (ret.guildid > 0) {
         ret.mgc = new MapleGuildCharacter(ret);
      }

      ret.fatigue = ct.fatigue;
      ret.buddylist = new BuddyList(ct.buddysize);
      ret.setUnions(new UnionList());
      ret.subcategory = ct.subcategory;
      ret.keyValues.putAll(ct.keyValues);
      ret.keyValues_boss.putAll(ct.keyValues_boss);
      ret.emoticons = ct.emoticons;
      ret.emoticonTabs = ct.emoticonTabs;
      ret.savedEmoticon = ct.savedEmoticon;
      ret.unstableMemorize = ct.unstableMemorize;
      if (isChannel) {
         MapleMapFactory mapFactory = ChannelServer.getInstance(client.getChannel()).getMapFactory();
         ret.map = mapFactory.getMap(ret.mapid);
         if (ret.map == null) {
            ret.map = mapFactory.getMap(ServerConstants.warpMap);
         } else if (ret.map.getForcedReturnId() != 999999999 && ret.map.getForcedReturnMap() != null) {
            ret.map = ret.map.getForcedReturnMap();
         }

         MaplePortal portal = ret.map.getPortal(ret.initialSpawnPoint);
         if (portal == null) {
            portal = ret.map.getPortal(0);
            ret.initialSpawnPoint = 0;
         }

         ret.setPosition(portal.getPosition());
         int messengerid = ct.messengerid;
         if (messengerid > 0) {
            ret.messenger = World.Messenger.getMessenger(messengerid);
         }
      } else {
         ret.messenger = null;
      }

      int partyid = ct.partyid;
      if (partyid >= 0) {
         MapleParty party = World.Party.getParty(partyid);
         if (party != null && party.getMemberById(ret.id) != null) {
            ret.party = party;
         }
      }

      Iterator var10 = ct.Quest.entrySet().iterator();

      Entry t;
      while(var10.hasNext()) {
         t = (Entry)var10.next();
         MapleQuestStatus queststatus_from = (MapleQuestStatus)t.getValue();
         queststatus_from.setQuest((Integer)t.getKey());
         ret.quests.put(queststatus_from.getQuest(), queststatus_from);
      }

      var10 = ct.Skills.entrySet().iterator();

      while(var10.hasNext()) {
         t = (Entry)var10.next();
         ret.skills.put(SkillFactory.getSkill((Integer)t.getKey()), (SkillEntry)t.getValue());
      }

      var10 = ct.traits.entrySet().iterator();

      while(var10.hasNext()) {
         t = (Entry)var10.next();
         ((MapleTrait)ret.traits.get(t.getKey())).setExp((Integer)t.getValue());
      }

      ret.inventory = (MapleInventory[])ct.inventorys;
      ret.BlessOfFairy_Origin = ct.BlessOfFairy;
      ret.BlessOfEmpress_Origin = ct.BlessOfEmpress;
      ret.skillMacros = (SkillMacro[])ct.skillmacro;
      ret.coodination = ct.coodination;
      ret.keylayout = new MapleKeyLayout(ct.keymap);
      ret.questinfo = ct.InfoQuest;
      ret.savedLocations = ct.savedlocation;
      ret.wishlist = ct.wishlist;
      ret.rocks = ct.rocks;
      ret.regrocks = ct.regrocks;
      ret.hyperrocks = ct.hyperrocks;
      ret.buddylist.loadFromTransfer(ct.buddies);
      ret.unions.loadFromTransfer(ct.unions);
      ret.keydown_skill = 0L;
      ret.lastfametime = ct.lastfametime;
      ret.lastmonthfameids = ct.famedcharacters;
      ret.lastmonthbattleids = ct.battledaccs;
      ret.extendedSlots = ct.extendedSlots;
      ret.itcafetime = ct.itcafetime;
      ret.storage = (MapleStorage)ct.storage;
      ret.cs = (CashShop)ct.cs;
      client.setAccountName(ct.accountname);
      client.setSecondPassword(ct.secondPassword);
      ret.nxcredit = ct.nxCredit;
      ret.acash = ct.ACash;
      ret.maplepoints = ct.MaplePoints;
      ret.numClones = ct.clonez;
      ret.pets = ct.pets;
      ret.imps = ct.imps;
      ret.rebuy = ct.rebuy;
      ret.cores = ct.cores;
      ret.matrixs = ct.matrixs;
      ret.symbol = ct.symbol;
      ret.setAuctionitems(ct.auctionitems);
      ret.basecolor = ct.basecolor;
      ret.addcolor = ct.addcolor;
      ret.baseprob = ct.baseprob;
      ret.secondbasecolor = ct.secondbasecolor;
      ret.secondaddcolor = ct.secondaddcolor;
      ret.secondbaseprob = ct.secondbaseprob;
      ret.linkskills = ct.linkskills;
      ret.mount = new MapleMount(ret, ct.mount_itemid, PlayerStats.getSkillByJob(1004, ret.job), ct.mount_Fatigue, ct.mount_level, ct.mount_exp);
      ret.honourExp = ct.honourexp;
      ret.honorLevel = ct.honourlevel;
      ret.innerSkills = (List)ct.innerSkills;
      ret.returnscroll = (Equip)ct.returnscroll;
      ret.choicepotential = (Equip)ct.choicepotential;
      ret.memorialcube = (Item)ct.memorialcube;
      ret.returnSc = ct.returnSc;
      ret.lastCharGuildId = ct.lastCharGuildId;
      ret.betaclothes = ct.betaclothes;
      ret.energy = ct.energy;
      ret.energyCharge = ct.energycharge;
      ret.hairRoom = ct.hairRoom;
      ret.faceRoom = ct.faceRoom;
      ret.skinRoom = ct.skinRoom;
      ret.expirationTask(false, false);
      ret.stats.recalcLocalStats(true, ret);
      client.setTempIP(ct.tempIP);
      return ret;
   }

   public static MapleCharacter loadCharFromDB(int charid, MapleClient client, boolean channelserver) {
      MapleCharacter ret = new MapleCharacter(channelserver, channelserver);
      ret.client = client;
      ret.id = charid;
      PreparedStatement ps = null;
      PreparedStatement pse = null;
      ResultSet rs = null;
      Connection con = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
         ps.setInt(1, charid);
         rs = ps.executeQuery();
         if (!rs.next()) {
            rs.close();
            ps.close();
            throw new RuntimeException("Loading the Char Failed (char not found)");
         }

         ret.name = rs.getString("name");
         ret.level = rs.getShort("level");
         ret.fame = rs.getInt("fame");
         ret.MesoChairCount = rs.getInt("mesochair");
         ret.stats.str = rs.getShort("str");
         ret.stats.dex = rs.getShort("dex");
         ret.stats.int_ = rs.getShort("int");
         ret.stats.luk = rs.getShort("luk");
         ret.stats.maxhp = (long)rs.getInt("maxhp");
         ret.stats.maxmp = (long)rs.getInt("maxmp");
         ret.stats.hp = (long)rs.getInt("hp");
         ret.stats.mp = (long)rs.getInt("mp");
         ret.job = rs.getShort("job");
         ret.gmLevel = rs.getByte("gm");
         ret.LinkMobCount = rs.getInt("LinkMobCount");
         ret.exp = rs.getLong("exp");
         ret.hpApUsed = rs.getShort("hpApUsed");
         String[] sp = rs.getString("sp").split(",");

         for(int i = 0; i < ret.remainingSp.length; ++i) {
            ret.remainingSp[i] = 0;
         }

         ret.remainingAp = rs.getShort("ap");
         ret.meso = rs.getLong("meso");
         ret.skinColor = rs.getByte("skincolor");
         ret.secondSkinColor = rs.getByte("secondSkincolor");
         ret.gender = rs.getByte("gender");
         ret.secondgender = rs.getByte("secondgender");
         ret.hair = rs.getInt("hair");
         ret.basecolor = rs.getInt("basecolor");
         ret.addcolor = rs.getInt("addcolor");
         ret.baseprob = rs.getInt("baseprob");
         ret.secondbasecolor = rs.getInt("secondbasecolor");
         ret.secondaddcolor = rs.getInt("secondaddcolor");
         ret.secondbaseprob = rs.getInt("secondbaseprob");
         ret.secondhair = rs.getInt("secondhair");
         ret.face = rs.getInt("face");
         ret.secondface = rs.getInt("secondface");
         ret.demonMarking = rs.getInt("demonMarking");
         ret.accountid = rs.getInt("accountid");
         client.setAccID(ret.accountid);
         ret.mapid = rs.getInt("map");
         ret.initialSpawnPoint = rs.getByte("spawnpoint");
         ret.world = rs.getByte("world");
         ret.guildid = rs.getInt("guildid");
         ret.guildrank = rs.getByte("guildrank");
         ret.allianceRank = rs.getByte("allianceRank");
         ret.guildContribution = rs.getInt("guildContribution");
         ret.lastattendance = rs.getInt("lastattendance");
         ret.totalWins = rs.getInt("totalWins");
         ret.totalLosses = rs.getInt("totalLosses");
         ret.currentrep = rs.getInt("currentrep");
         ret.totalrep = rs.getInt("totalrep");
         if (ret.guildid > 0) {
            ret.mgc = new MapleGuildCharacter(ret);
         }

         ret.buddylist = new BuddyList(rs.getByte("buddyCapacity"));
         ret.setUnions(new UnionList());
         ret.honourExp = rs.getInt("honourExp");
         ret.honorLevel = rs.getInt("honourLevel");
         ret.subcategory = rs.getByte("subcategory");
         ret.mount = new MapleMount(ret, 0, PlayerStats.getSkillByJob(1004, ret.job), (byte)0, (byte)1, 0);
         ret.rank = rs.getInt("rank");
         ret.rankMove = rs.getInt("rankMove");
         ret.jobRank = rs.getInt("jobRank");
         ret.jobRankMove = rs.getInt("jobRankMove");
         ret.marriageId = rs.getInt("marriageId");
         ret.fatigue = rs.getShort("fatigue");
         ret.pvpExp = rs.getInt("pvpExp");
         ret.pvpPoints = rs.getInt("pvpPoints");
         ret.itcafetime = rs.getInt("itcafetime");
         ret.reborns = rs.getInt("reborns");
         ret.apstorage = rs.getInt("apstorage");
         ret.betaclothes = rs.getInt("betaclothes");
         long choiceId = rs.getLong("choicepotential");
         long memorialId = rs.getLong("memorialcube");
         long returnscroll = rs.getLong("returnscroll");
         ret.returnSc = rs.getInt("returnsc");
         String[] spCore;
         int type;
         int j;
         if (rs.getString("exceptionlist").length() > 0) {
            String[] exceptionList = rs.getString("exceptionlist").split(",");
            spCore = exceptionList;
            type = exceptionList.length;

            for(j = 0; j < type; ++j) {
               String str = spCore[j];
               ret.getExceptionList().add(Integer.parseInt(str));
            }
         }

         Iterator var49 = ret.traits.values().iterator();

         while(var49.hasNext()) {
            MapleTrait t = (MapleTrait)var49.next();
            t.setExp(rs.getInt(t.getType().name()));
         }

         int r;
         if (channelserver) {
            ChatEmoticon.LoadChatEmoticonTabs(ret);
            ChatEmoticon.LoadSavedChatEmoticon(ret);
            ChatEmoticon.LoadChatEmoticons(ret, ret.getEmoticonTabs());
            ret.setCalcDamage(new CalcDamage());
            MapleMapFactory mapFactory = ChannelServer.getInstance(client.getChannel()).getMapFactory();
            ret.map = mapFactory.getMap(ret.mapid);
            if (ret.map == null) {
               ret.map = mapFactory.getMap(ServerConstants.warpMap);
            }

            MaplePortal portal = ret.map.getPortal(ret.initialSpawnPoint);
            if (portal == null) {
               portal = ret.map.getPortal(0);
               ret.initialSpawnPoint = 0;
            }

            ret.setPosition(portal.getPosition());
            type = rs.getInt("party");
            if (type >= 0) {
               MapleParty party = World.Party.getParty(type);
               if (party != null && party.getMemberById(ret.id) != null) {
                  ret.party = party;
               }
            }

            String[] pets = rs.getString("pets").split(",");
            ps.close();
            rs.close();
            ps = con.prepareStatement("SELECT * FROM inventoryitemscash WHERE uniqueid = ?");

            for(r = 0; r < 3; ++r) {
               if (!pets[r].equals("-1")) {
                  int petid = Integer.parseInt(pets[r]);
                  ps.setInt(1, petid);
                  rs = ps.executeQuery();
                  if (rs.next()) {
                     MaplePet pet = MaplePet.loadFromDb(rs.getInt("itemid"), (long)petid, rs.getShort("position"));
                     ret.addPetBySlotId(pet, (byte)r);
                  }
               }
            }
         }

         ps.close();
         rs.close();
         MapleInventoryType[] var53;
         int maxlevel_2;
         MapleInventoryType type;
         Iterator var70;
         Entry mit;
         if (channelserver) {
            ps = con.prepareStatement("SELECT * FROM queststatus WHERE characterid = ?");
            ps.setInt(1, charid);
            rs = ps.executeQuery();

            int maxlevel_;
            while(rs.next()) {
               maxlevel_ = rs.getInt("quest");
               MapleQuest q = MapleQuest.getInstance(maxlevel_);
               byte stat = rs.getByte("status");
               MapleQuestStatus status = new MapleQuestStatus(q, stat);
               long cTime = rs.getLong("time");
               if (cTime > -1L) {
                  status.setCompletionTime(cTime * 1000L);
               }

               status.setForfeited(rs.getInt("forfeited"));
               status.setCustomData(rs.getString("customData"));
               ret.quests.put(q, status);
               pse = con.prepareStatement("SELECT * FROM queststatusmobs WHERE queststatusid = ?");
               pse.setInt(1, rs.getInt("queststatusid"));
               ResultSet rsMobs = pse.executeQuery();
               if (rsMobs.next()) {
                  status.setMobKills(rsMobs.getInt("mob"), rsMobs.getInt("count"));
               }

               pse.close();
               rsMobs.close();
            }

            ps.close();
            rs.close();
            ps = con.prepareStatement("SELECT * FROM inventoryslot where characterid = ?");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            if (!rs.next()) {
               rs.close();
               ps.close();
               throw new RuntimeException("No Inventory slot column found in SQL. [inventoryslot]");
            }

            ret.getInventory(MapleInventoryType.EQUIP).setSlotLimit(rs.getShort("equip"));
            ret.getInventory(MapleInventoryType.USE).setSlotLimit(rs.getShort("use"));
            ret.getInventory(MapleInventoryType.SETUP).setSlotLimit(rs.getShort("setup"));
            ret.getInventory(MapleInventoryType.ETC).setSlotLimit(rs.getShort("etc"));
            ret.getInventory(MapleInventoryType.CASH).setSlotLimit(rs.getShort("cash"));
            ret.getInventory(MapleInventoryType.CODY).setSlotLimit(rs.getShort("cody"));
            ps.close();
            rs.close();
            var53 = MapleInventoryType.values();
            maxlevel_2 = var53.length;

            for(type = 0; type < maxlevel_2; ++type) {
               type = var53[type];
               if (type.getType() != 0) {
                  var70 = ItemLoader.INVENTORY.loadItems(false, charid, type).entrySet().iterator();

                  while(var70.hasNext()) {
                     mit = (Entry)var70.next();
                     if (((Item)mit.getValue()).getInventoryId() == choiceId && choiceId > 0L) {
                        ret.choicepotential = (Equip)mit.getValue();
                     } else if (((Item)mit.getValue()).getInventoryId() == memorialId && memorialId > 0L) {
                        ret.memorialcube = (Item)mit.getValue();
                     } else if (((Item)mit.getValue()).getInventoryId() == returnscroll && returnscroll > 0L) {
                        ret.returnscroll = (Equip)mit.getValue();
                     } else {
                        ret.getInventory(type.getType()).addFromDB((Item)mit.getValue());
                     }

                     if (((Item)mit.getValue()).getPet() != null) {
                     }
                  }
               }
            }

            ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
            ps.setInt(1, ret.accountid);
            rs = ps.executeQuery();
            if (rs.next()) {
               ret.getClient().setAccountName(rs.getString("name"));
               ret.getClient().setSecondPassword(rs.getString("2ndpassword"));
               ret.nxcredit = rs.getInt("nxCredit");
               ret.acash = 0;
               ret.maplepoints = rs.getInt("mPoints");
               ret.points = rs.getInt("points");
               ret.vpoints = rs.getInt("vpoints");
               if (rs.getTimestamp("lastlogon") != null) {
                  Calendar cal = Calendar.getInstance();
                  cal.setTimeInMillis(rs.getTimestamp("lastlogon").getTime());
               }

               if (rs.getInt("banned") > 0) {
                  rs.close();
                  ps.close();
                  ret.getClient().getSession().close();
                  throw new RuntimeException("Loading a banned character");
               }

               rs.close();
               ps.close();
               ps = con.prepareStatement("UPDATE accounts SET lastlogon = CURRENT_TIMESTAMP() WHERE id = ?");
               ps.setInt(1, ret.accountid);
               ps.executeUpdate();
            } else {
               rs.close();
            }

            ps.close();

            try {
               ps = con.prepareStatement("SELECT * FROM questinfo WHERE characterid = ?");
               ps.setInt(1, charid);
               rs = ps.executeQuery();

               while(rs.next()) {
                  ret.questinfo.put(rs.getInt("quest"), rs.getString("customData"));
               }
            } finally {
               rs.close();
               ps.close();
            }

            ps = con.prepareStatement("SELECT * FROM characters WHERE accountid = ? AND level >= 70");
            ps.setInt(1, ret.getAccountID());
            rs = ps.executeQuery();

            while(true) {
               Skill skil;
               do {
                  do {
                     if (!rs.next()) {
                        rs.close();
                        ps.close();
                        ps = con.prepareStatement("SELECT skillid, skilllevel, masterlevel, expiration FROM skills WHERE characterid = ?");
                        ps.setInt(1, charid);
                        rs = ps.executeQuery();

                        while(rs.next()) {
                           maxlevel_ = rs.getInt("skillid");
                           skil = SkillFactory.getSkill(maxlevel_);
                           type = rs.getInt("skilllevel");
                           byte msl = rs.getByte("masterlevel");
                           if (skil != null) {
                              if (type > skil.getMaxLevel() && maxlevel_ < 92000000) {
                                 if (!skil.isBeginnerSkill() && skil.canBeLearnedBy(ret) && !skil.isSpecialSkill()) {
                                    ret.remainingSp[GameConstants.getSkillBookForSkill(maxlevel_)] = ret.remainingSp[GameConstants.getSkillBookForSkill(maxlevel_)] + type - skil.getMaxLevel();
                                 }

                                 type = (byte)skil.getMaxLevel();
                              }

                              if (msl > skil.getMaxLevel()) {
                                 msl = (byte)skil.getMaxLevel();
                              }

                              ret.skills.put(skil, new SkillEntry(type, msl, rs.getLong("expiration")));
                           } else if (skil == null && !GameConstants.isBeginnerJob(maxlevel_ / 10000) && maxlevel_ / 10000 != 900 && maxlevel_ / 10000 != 800 && maxlevel_ / 10000 != 9000) {
                              ret.remainingSp[GameConstants.getSkillBookForSkill(maxlevel_)] += type;
                           }
                        }

                        rs.close();
                        ps.close();

                        try {
                           ps = con.prepareStatement("SELECT * FROM core WHERE charid = ?", 1);
                           ps.setInt(1, ret.id);
                           rs = ps.executeQuery();

                           while(rs.next()) {
                              maxlevel_ = rs.getInt("coreid");
                              spCore = null;
                              Core core = new Core(rs.getLong("crcid"), maxlevel_, ret.id, rs.getInt("level"), rs.getInt("exp"), rs.getInt("state"), rs.getInt("maxlevel"), rs.getInt("skill1"), rs.getInt("skill2"), rs.getInt("skill3"), rs.getInt("position"), spCore);
                              ret.cores.add(core);
                              core.setId(ret.cores.indexOf(core));
                           }

                           ps.close();
                           rs.close();
                        } catch (Exception var44) {
                           var44.printStackTrace();
                        }

                        try {
                           ps = con.prepareStatement("SELECT * FROM matrix WHERE charid = ?", 1);
                           ps.setInt(1, ret.id);
                           rs = ps.executeQuery();

                           while(rs.next()) {
                              VMatrix matrix = new VMatrix(rs.getInt("id"), rs.getInt("position"), rs.getInt("level"), rs.getByte("unlock") == 1);
                              ret.matrixs.add(matrix);
                           }

                           ps.close();
                           rs.close();
                        } catch (Exception var43) {
                           var43.printStackTrace();
                        }

                        try {
                           ps = con.prepareStatement("SELECT * FROM coodination WHERE playerid = ? ORDER BY position ASC", 1);
                           ps.setInt(1, ret.id);
                           rs = ps.executeQuery();

                           while(rs.next()) {
                              AvatarLook a = AvatarLook.init(rs);
                              ret.coodination.add(a);
                           }

                           ps.close();
                           rs.close();
                        } catch (Exception var42) {
                           var42.printStackTrace();
                        }

                        ret.expirationTask(false, true);
                        ps = con.prepareStatement("SELECT * FROM characters WHERE accountid = ? ORDER BY level DESC");
                        ps.setInt(1, ret.accountid);
                        rs = ps.executeQuery();
                        maxlevel_ = 0;
                        maxlevel_2 = 0;

                        while(true) {
                           do {
                              do {
                                 if (!rs.next()) {
                                    if (ret.BlessOfFairy_Origin == null) {
                                       ret.BlessOfFairy_Origin = ret.name;
                                    }

                                    ret.skills.put(SkillFactory.getSkill(GameConstants.getBOF_ForJob(ret.job)), new SkillEntry(maxlevel_, (byte)0, -1L));
                                    if (SkillFactory.getSkill(GameConstants.getEmpress_ForJob(ret.job)) != null) {
                                       if (ret.BlessOfEmpress_Origin == null) {
                                          ret.BlessOfEmpress_Origin = ret.BlessOfFairy_Origin;
                                       }

                                       ret.skills.put(SkillFactory.getSkill(GameConstants.getEmpress_ForJob(ret.job)), new SkillEntry(maxlevel_2, (byte)0, -1L));
                                    }

                                    ps.close();
                                    rs.close();
                                    ps = con.prepareStatement("SELECT skill_id, skill_level, max_level, rank FROM inner_ability_skills WHERE player_id = ?");
                                    ps.setInt(1, charid);
                                    rs = ps.executeQuery();

                                    while(rs.next()) {
                                       ret.innerSkills.add(new InnerSkillValueHolder(rs.getInt("skill_id"), rs.getByte("skill_level"), rs.getByte("max_level"), rs.getByte("rank")));
                                    }

                                    ps.close();
                                    rs.close();
                                    ps = con.prepareStatement("SELECT * FROM skillmacros WHERE characterid = ?");
                                    ps.setInt(1, charid);

                                    SkillMacro macro;
                                    for(rs = ps.executeQuery(); rs.next(); ret.skillMacros[type] = macro) {
                                       type = rs.getInt("position");
                                       macro = new SkillMacro(rs.getInt("skill1"), rs.getInt("skill2"), rs.getInt("skill3"), rs.getString("name"), rs.getInt("shout"), type);
                                    }

                                    rs.close();
                                    ps.close();
                                    ps = con.prepareStatement("SELECT * FROM mannequins WHERE characterid = ?");
                                    ps.setInt(1, charid);
                                    rs = ps.executeQuery();

                                    while(rs.next()) {
                                       type = rs.getInt("type");
                                       MapleMannequin mq = new MapleMannequin(rs.getInt("value"), rs.getInt("baseProb"), rs.getInt("baseColor"), rs.getInt("addColor"));
                                       switch(type) {
                                       case 0:
                                          ret.hairRoom.add(mq);
                                       case 1:
                                          ret.faceRoom.add(mq);
                                       case 2:
                                          ret.skinRoom.add(mq);
                                       }
                                    }

                                    rs.close();
                                    ps.close();
                                    ps = con.prepareStatement("SELECT `key`,`type`,`action` FROM keymap WHERE characterid = ?");
                                    ps.setInt(1, charid);
                                    rs = ps.executeQuery();
                                    Map keyb = ret.keylayout.Layout();

                                    while(rs.next()) {
                                       keyb.put(rs.getInt("key"), new Pair(rs.getByte("type"), rs.getInt("action")));
                                    }

                                    rs.close();
                                    ps.close();
                                    ret.keylayout.unchanged();
                                    ps = con.prepareStatement("SELECT `locationtype`,`map` FROM savedlocations WHERE characterid = ?");
                                    ps.setInt(1, charid);

                                    for(rs = ps.executeQuery(); rs.next(); ret.savedLocations[rs.getInt("locationtype")] = rs.getInt("map")) {
                                    }

                                    rs.close();
                                    ps.close();
                                    ps = con.prepareStatement("SELECT `characterid_to`,`when` FROM famelog WHERE characterid = ? AND DATEDIFF(NOW(),`when`) < 30");
                                    ps.setInt(1, charid);
                                    rs = ps.executeQuery();
                                    ret.lastfametime = 0L;
                                    ret.lastmonthfameids = new ArrayList(31);

                                    while(rs.next()) {
                                       ret.lastfametime = Math.max(ret.lastfametime, rs.getTimestamp("when").getTime());
                                       ret.lastmonthfameids.add(rs.getInt("characterid_to"));
                                    }

                                    rs.close();
                                    ps.close();
                                    ps = con.prepareStatement("SELECT `accid_to`,`when` FROM battlelog WHERE accid = ? AND DATEDIFF(NOW(),`when`) < 30");
                                    ps.setInt(1, ret.accountid);
                                    rs = ps.executeQuery();
                                    ret.lastmonthbattleids = new ArrayList();

                                    while(rs.next()) {
                                       ret.lastmonthbattleids.add(rs.getInt("accid_to"));
                                    }

                                    rs.close();
                                    ps.close();
                                    ps = con.prepareStatement("SELECT * FROM keyvalue WHERE id = ?");
                                    ps.setInt(1, charid);
                                    rs = ps.executeQuery();

                                    while(rs.next()) {
                                       ret.keyValues.put(rs.getString("key"), rs.getString("value"));
                                    }

                                    rs.close();
                                    ps.close();
                                    ps = con.prepareStatement("SELECT * FROM keyvalue_boss WHERE id = ?");
                                    ps.setInt(1, charid);
                                    rs = ps.executeQuery();

                                    while(rs.next()) {
                                       ret.keyValues_boss.put(rs.getString("key"), rs.getString("value"));
                                    }

                                    rs.close();
                                    ps.close();
                                    ps = con.prepareStatement("SELECT `itemId` FROM extendedSlots WHERE characterid = ?");
                                    ps.setInt(1, charid);
                                    rs = ps.executeQuery();

                                    while(rs.next()) {
                                       ret.extendedSlots.add(rs.getInt("itemId"));
                                    }

                                    rs.close();
                                    ps.close();
                                    ret.buddylist.loadFromDb(ret.accountid);
                                    ret.storage = MapleStorage.loadStorage(ret.accountid);
                                    ret.getUnions().loadFromDb(ret.accountid);
                                    ret.cs = new CashShop(ret.accountid, charid, ret.getJob());
                                    ps = con.prepareStatement("SELECT sn FROM wishlist WHERE characterid = ?");
                                    ps.setInt(1, charid);
                                    rs = ps.executeQuery();

                                    for(j = 0; rs.next(); ++j) {
                                       ret.wishlist[j] = rs.getInt("sn");
                                    }

                                    while(j < 12) {
                                       ret.wishlist[j] = 0;
                                       ++j;
                                    }

                                    rs.close();
                                    ps.close();
                                    ps = con.prepareStatement("SELECT mapid FROM trocklocations WHERE characterid = ?");
                                    ps.setInt(1, charid);
                                    rs = ps.executeQuery();

                                    for(r = 0; rs.next(); ++r) {
                                       ret.rocks[r] = rs.getInt("mapid");
                                    }

                                    while(r < 10) {
                                       ret.rocks[r] = 999999999;
                                       ++r;
                                    }

                                    rs.close();
                                    ps.close();
                                    ps = con.prepareStatement("SELECT mapid FROM regrocklocations WHERE characterid = ?");
                                    ps.setInt(1, charid);
                                    rs = ps.executeQuery();

                                    for(r = 0; rs.next(); ++r) {
                                       ret.regrocks[r] = rs.getInt("mapid");
                                    }

                                    while(r < 5) {
                                       ret.regrocks[r] = 999999999;
                                       ++r;
                                    }

                                    rs.close();
                                    ps.close();
                                    ps = con.prepareStatement("SELECT mapid FROM hyperrocklocations WHERE characterid = ?");
                                    ps.setInt(1, charid);
                                    rs = ps.executeQuery();

                                    for(r = 0; rs.next(); ++r) {
                                       ret.hyperrocks[r] = rs.getInt("mapid");
                                    }

                                    while(r < 13) {
                                       ret.hyperrocks[r] = 999999999;
                                       ++r;
                                    }

                                    rs.close();
                                    ps.close();
                                    ps = con.prepareStatement("SELECT * from stolen WHERE characterid = ?");
                                    ps.setInt(1, charid);
                                    rs = ps.executeQuery();

                                    while(rs.next()) {
                                       ret.stolenSkills.add(new Pair(rs.getInt("skillid"), rs.getInt("chosen") > 0));
                                    }

                                    rs.close();
                                    ps.close();
                                    ps = con.prepareStatement("SELECT * FROM imps WHERE characterid = ?");
                                    ps.setInt(1, charid);
                                    rs = ps.executeQuery();

                                    for(r = 0; rs.next(); ++r) {
                                       ret.imps[r] = new MapleImp(rs.getInt("itemid"));
                                       ret.imps[r].setLevel(rs.getByte("level"));
                                       ret.imps[r].setState(rs.getByte("state"));
                                       ret.imps[r].setCloseness(rs.getShort("closeness"));
                                       ret.imps[r].setFullness(rs.getShort("fullness"));
                                    }

                                    rs.close();
                                    ps.close();
                                    ps = con.prepareStatement("SELECT * FROM mountdata WHERE characterid = ?");
                                    ps.setInt(1, charid);
                                    rs = ps.executeQuery();
                                    if (!rs.next()) {
                                       throw new RuntimeException("No mount data found on SQL column");
                                    }

                                    Item mount = ret.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-23);
                                    ret.mount = new MapleMount(ret, mount != null ? mount.getItemId() : 0, PlayerStats.getSkillByJob(1004, ret.job), rs.getByte("Fatigue"), rs.getByte("Level"), rs.getInt("Exp"));
                                    ps.close();
                                    rs.close();
                                    ret.stats.recalcLocalStats(true, ret);
                                    return ret;
                                 }
                              } while(rs.getInt("id") == charid);

                              if (GameConstants.isKOC(rs.getShort("job"))) {
                                 type = rs.getShort("level") / 5;
                                 if (type > 24) {
                                    type = 24;
                                 }

                                 if (type > maxlevel_2 || maxlevel_2 == 0) {
                                    maxlevel_2 = type;
                                    ret.BlessOfEmpress_Origin = rs.getString("name");
                                 }
                              }

                              type = rs.getShort("level") / 10;
                              if (type > 20) {
                                 type = 20;
                              }
                           } while(type <= maxlevel_ && maxlevel_ != 0);

                           maxlevel_ = type;
                           ret.BlessOfFairy_Origin = rs.getString("name");
                        }
                     }

                     maxlevel_ = GameConstants.getLinkedSkillByJob(rs.getShort("job"));
                  } while(maxlevel_ == 0);
               } while(ret.getName().equals(rs.getString("name")));

               skil = SkillFactory.getSkill(maxlevel_);
               type = skil.getId() == 80000110 ? (rs.getInt("level") >= 200 ? 5 : (rs.getInt("level") >= 180 ? 4 : (rs.getInt("level") >= 160 ? 3 : (rs.getInt("level") >= 140 ? 2 : 1)))) : (rs.getInt("level") >= 120 ? 2 : 1);
               boolean pass = false;
               var70 = ret.linkskills.iterator();

               while(var70.hasNext()) {
                  Triple<Skill, SkillEntry, Integer> a = (Triple)var70.next();
                  if (((Skill)a.getLeft()).getId() == maxlevel_ && ((SkillEntry)a.getMid()).skillevel >= type) {
                     pass = true;
                  }
               }

               r = skil.getId() == 80000110 ? 5 : 2;
               if (!pass) {
                  ret.linkskills.add(new Triple(skil, new SkillEntry(type, (byte)r, -1L), rs.getInt("id")));
               }
            }
         } else {
            var53 = MapleInventoryType.values();
            maxlevel_2 = var53.length;

            for(type = 0; type < maxlevel_2; ++type) {
               type = var53[type];
               if (type.getType() != 0) {
                  var70 = ItemLoader.INVENTORY.loadItems(false, charid, type).entrySet().iterator();

                  while(var70.hasNext()) {
                     mit = (Entry)var70.next();
                     ret.getInventory(type.getType()).addFromDB((Item)mit.getValue());
                     if (((Item)mit.getValue()).getPet() != null) {
                     }
                  }
               }
            }

            ret.stats.recalcPVPRank(ret);
         }
      } catch (SQLException var46) {
         var46.printStackTrace();
         FileoutputUtil.outputFileError("Log_Packet_Except.rtf", var46);
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
         } catch (SQLException var41) {
         }

      }

      return ret;
   }

   public static void saveNewCharToDB(MapleCharacter chr, LoginInformationProvider.JobType type, short db) {
      Connection con = null;
      PreparedStatement ps = null;
      PreparedStatement pse = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("INSERT INTO characters (level, str, dex, luk, `int`, hp, mp, maxhp, maxmp, sp, ap, skincolor, secondSkinColor, gender, secondgender, job, hair, secondhair, face, secondface, demonMarking, map, meso, party, buddyCapacity, subcategory, accountid, name, world, itcafetime, basecolor) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 1);
         ps.setInt(1, chr.level);
         PlayerStats stat = chr.stats;
         ps.setShort(2, stat.getStr());
         ps.setShort(3, stat.getDex());
         ps.setShort(4, stat.getInt());
         ps.setShort(5, stat.getLuk());
         ps.setLong(6, stat.getHp());
         ps.setLong(7, stat.getMp());
         ps.setLong(8, stat.getMaxHp());
         ps.setLong(9, stat.getMaxMp());
         StringBuilder sps = new StringBuilder();

         for(int i = 0; i < chr.remainingSp.length; ++i) {
            sps.append(chr.remainingSp[i]);
            sps.append(",");
         }

         String sp = sps.toString();
         ps.setString(10, sp.substring(0, sp.length() - 1));
         ps.setShort(11, chr.remainingAp);
         ps.setByte(12, chr.skinColor);
         ps.setByte(13, chr.secondSkinColor);
         ps.setByte(14, chr.gender);
         ps.setByte(15, chr.secondgender);
         ps.setShort(16, chr.job);
         ps.setInt(17, chr.hair);
         ps.setInt(18, chr.secondhair);
         ps.setInt(19, chr.face);
         ps.setInt(20, chr.secondface);
         ps.setInt(21, chr.demonMarking);
         if (db < 0 || db > 2) {
            db = 0;
         }

         ps.setInt(22, type.map);
         ps.setLong(23, chr.meso);
         ps.setInt(24, -1);
         ps.setByte(25, chr.buddylist.getCapacity());
         ps.setInt(26, db);
         ps.setInt(27, chr.getAccountID());
         ps.setString(28, chr.name);
         ps.setByte(29, chr.world);
         ps.setInt(30, chr.getInternetCafeTime());
         ps.setInt(31, -1);
         ps.executeUpdate();
         rs = ps.getGeneratedKeys();
         if (!rs.next()) {
            ps.close();
            rs.close();
            throw new DatabaseException("Inserting char failed.");
         }

         chr.id = rs.getInt(1);
         ps.close();
         rs.close();
         Iterator var10 = chr.quests.values().iterator();

         int j;
         while(var10.hasNext()) {
            MapleQuestStatus q = (MapleQuestStatus)var10.next();
            ps = con.prepareStatement("INSERT INTO queststatus (`queststatusid`, `characterid`, `quest`, `status`, `time`, `forfeited`, `customData`) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)", 1);
            ps.setInt(1, chr.id);
            ps.setInt(2, q.getQuest().getId());
            ps.setInt(3, q.getStatus());
            ps.setInt(4, (int)(q.getCompletionTime() / 1000L));
            ps.setInt(5, q.getForfeited());
            ps.setString(6, q.getCustomData());
            ps.execute();
            rs = ps.getGeneratedKeys();
            if (q.hasMobKills()) {
               rs.next();
               Iterator var12 = q.getMobKills().keySet().iterator();

               while(var12.hasNext()) {
                  j = (Integer)var12.next();
                  pse = con.prepareStatement("INSERT INTO queststatusmobs VALUES (DEFAULT, ?, ?, ?)");
                  pse.setInt(1, rs.getInt(1));
                  pse.setInt(2, j);
                  pse.setInt(3, q.getMobKills(j));
                  pse.execute();
                  pse.close();
               }
            }

            ps.close();
            rs.close();
         }

         ps = con.prepareStatement("INSERT INTO skills (characterid, skillid, skilllevel, masterlevel, expiration) VALUES (?, ?, ?, ?, ?)");
         ps.setInt(1, chr.id);
         var10 = chr.skills.entrySet().iterator();

         while(var10.hasNext()) {
            Entry<Skill, SkillEntry> skill = (Entry)var10.next();
            ps.setInt(2, ((Skill)skill.getKey()).getId());
            ps.setInt(3, ((SkillEntry)skill.getValue()).skillevel);
            ps.setByte(4, ((SkillEntry)skill.getValue()).masterlevel);
            ps.setLong(5, ((SkillEntry)skill.getValue()).expiration);
            ps.execute();
         }

         ps.close();
         ps = con.prepareStatement("INSERT INTO inventoryslot (characterid, `equip`, `use`, `setup`, `etc`, `cash`, `cody`) VALUES (?, ?, ?, ?, ?, ?, ?)");
         ps.setInt(1, chr.id);
         ps.setShort(2, (short)128);
         ps.setShort(3, (short)128);
         ps.setShort(4, (short)128);
         ps.setShort(5, (short)128);
         ps.setShort(6, (short)128);
         ps.setShort(7, (short)128);
         ps.execute();
         ps.close();
         ps = con.prepareStatement("INSERT INTO mountdata (characterid, `Level`, `Exp`, `Fatigue`) VALUES (?, ?, ?, ?)");
         ps.setInt(1, chr.id);
         ps.setByte(2, (byte)1);
         ps.setInt(3, 0);
         ps.setByte(4, (byte)0);
         ps.execute();
         ps.close();
         int[] array1 = new int[]{2, 3, 4, 5, 6, 7, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 29, 31, 33, 34, 35, 37, 38, 39, 40, 41, 43, 44, 45, 46, 47, 48, 50, 51, 56, 57, 59, 60, 61, 62, 63, 64, 65, 83, 1, 70};
         int[] array2 = new int[]{4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 4, 4, 4, 4, 4, 5, 5, 6, 6, 6, 6, 6, 6, 6, 0, 4, 4};
         int[] array3 = new int[]{10, 12, 13, 18, 23, 28, 8, 5, 0, 4, 27, 30, 32, 1, 24, 19, 14, 15, 52, 2, 25, 17, 11, 3, 20, 26, 16, 22, 9, 50, 51, 6, 31, 29, 7, 33, 53, 54, 100, 101, 102, 103, 104, 105, 106, 52, 46, 47};

         for(j = 0; j < array1.length; ++j) {
            ps = con.prepareStatement("INSERT INTO keymap (characterid, `key`, `type`, `action`) VALUES (?, ?, ?, ?)");
            ps.setInt(1, chr.id);
            ps.setInt(2, array1[j]);
            ps.setInt(3, array2[j]);
            ps.setInt(4, array3[j]);
            ps.execute();
            ps.close();
         }

         chr.saveInventory(con, true);
      } catch (Exception var24) {
         FileoutputUtil.outputFileError("Log_Packet_Except.rtf", var24);
         var24.printStackTrace();
         System.err.println("[charsave] Error saving character data");

         try {
            if (pse != null) {
               pse.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }

            if (con != null) {
               con.close();
            }
         } catch (SQLException var23) {
            FileoutputUtil.outputFileError("Log_Packet_Except.rtf", var23);
            var23.printStackTrace();
            System.err.println("[charsave] Error going back to autocommit mode");
         }
      } finally {
         try {
            if (pse != null) {
               pse.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }

            if (con != null) {
               con.close();
            }
         } catch (SQLException var22) {
            FileoutputUtil.outputFileError("Log_Packet_Except.rtf", var22);
            var22.printStackTrace();
            System.err.println("[charsave] Error going back to autocommit mode");
         }

      }

   }

   public void saveMannequinToDB(Connection con) {
      try {
         this.deleteWhereCharacterId(con, "DELETE FROM mannequins WHERE characterid = ?");
         Iterator var2 = this.getHairRoom().iterator();

         MapleMannequin skin;
         PreparedStatement ps;
         while(var2.hasNext()) {
            skin = (MapleMannequin)var2.next();
            ps = con.prepareStatement("INSERT INTO mannequins (value, baseprob, basecolor, addcolor, characterid, type) VALUES (?, ?, ?, ?, ?, ?)");
            ps.setInt(1, skin.getValue());
            ps.setInt(2, skin.getBaseProb());
            ps.setInt(3, skin.getBaseColor());
            ps.setInt(4, skin.getAddColor());
            ps.setInt(5, this.id);
            ps.setInt(6, 0);
            ps.execute();
            ps.close();
         }

         var2 = this.getFaceRoom().iterator();

         while(var2.hasNext()) {
            skin = (MapleMannequin)var2.next();
            ps = con.prepareStatement("INSERT INTO mannequins (value, baseprob, basecolor, addcolor, characterid, type) VALUES (?, ?, ?, ?, ?, ?)");
            ps.setInt(1, skin.getValue());
            ps.setInt(2, skin.getBaseProb());
            ps.setInt(3, skin.getBaseColor());
            ps.setInt(4, skin.getAddColor());
            ps.setInt(5, this.id);
            ps.setInt(6, 1);
            ps.execute();
            ps.close();
         }

         var2 = this.skinRoom.iterator();

         while(var2.hasNext()) {
            skin = (MapleMannequin)var2.next();
            ps = con.prepareStatement("INSERT INTO mannequins (value, baseprob, basecolor, addcolor, characterid, type) VALUES (?, ?, ?, ?, ?, ?)");
            ps.setInt(1, skin.getValue());
            ps.setInt(2, skin.getBaseProb());
            ps.setInt(3, skin.getBaseColor());
            ps.setInt(4, skin.getAddColor());
            ps.setInt(5, this.id);
            ps.setInt(6, 2);
            ps.execute();
            ps.close();
         }
      } catch (SQLException var5) {
         var5.printStackTrace();
      }

   }

   public void saveDummyToDB(Connection con) {
      try {
         PreparedStatement ps = con.prepareStatement("UPDATE characters SET choicepotential = ?, memorialcube = ?, returnscroll = ?, returnsc = ? WHERE id = ?", 1);
         if (this.choicepotential != null) {
            ps.setLong(1, this.choicepotential.getInventoryId());
         } else {
            ps.setLong(1, 0L);
         }

         if (this.memorialcube != null) {
            ps.setLong(2, this.memorialcube.getInventoryId());
         } else {
            ps.setLong(2, 0L);
         }

         if (this.returnscroll != null) {
            ps.setLong(3, this.returnscroll.getInventoryId());
         } else {
            ps.setLong(3, 0L);
         }

         ps.setInt(4, this.returnSc);
         ps.setInt(5, this.id);
         ps.executeUpdate();
         ps.close();
      } catch (SQLException var3) {
         var3.printStackTrace();
      }

   }

   public boolean saveCharToDB(Connection con) {
      synchronized(this) {
         boolean var10000;
         try {
            PreparedStatement ps = con.prepareStatement("UPDATE characters SET level = ?, fame = ?, str = ?, dex = ?, luk = ?, `int` = ?, exp = ?, hp = ?, mp = ?, maxhp = ?, maxmp = ?, sp = ?, ap = ?, gm = ?, skincolor = ?, secondSkincolor = ?, gender = ?, secondgender = ?, job = ?, hair = ?, basecolor = ?, addcolor = ?, baseprob = ?, secondhair = ?, face = ?, secondface = ?, demonMarking = ?, map = ?, meso = ?, hpApUsed = ?, spawnpoint = ?, party = ?, buddyCapacity = ?, subcategory = ?, marriageId = ?, currentrep = ?, totalrep = ?, fatigue = ?, charm = ?, charisma = ?, craft = ?, insight = ?, sense = ?, will = ?, totalwins = ?, totallosses = ?, pvpExp = ?, pvpPoints = ?, reborns = ?, apstorage = ?, name = ?, honourExp = ?, honourLevel = ?, soulcount = ?, itcafetime = ?, pets = ?, LinkMobCount = ?, secondbasecolor = ?, secondaddcolor = ?, secondbaseprob = ?, mesochair = ?, betaclothes = ?, exceptionlist = ? WHERE id = ?", 1);
            ps.setInt(1, this.level);
            ps.setInt(2, this.fame);
            ps.setShort(3, this.stats.getStr());
            ps.setShort(4, this.stats.getDex());
            ps.setShort(5, this.stats.getLuk());
            ps.setShort(6, this.stats.getInt());
            ps.setLong(7, this.exp);
            ps.setLong(8, this.stats.getHp() < 1L ? 50L : this.stats.getHp());
            ps.setLong(9, this.stats.getMp());
            ps.setLong(10, this.stats.getMaxHp());
            ps.setLong(11, this.stats.getMaxMp());
            StringBuilder sps = new StringBuilder();

            for(int i = 0; i < this.remainingSp.length; ++i) {
               sps.append(this.remainingSp[i]);
               sps.append(",");
            }

            String sp = sps.toString();
            ps.setString(12, sp.substring(0, sp.length() - 1));
            ps.setShort(13, this.remainingAp);
            ps.setByte(14, this.gmLevel);
            ps.setByte(15, this.skinColor);
            ps.setByte(16, this.secondSkinColor);
            ps.setByte(17, this.gender);
            ps.setByte(18, this.secondgender);
            ps.setShort(19, this.job);
            ps.setInt(20, this.hair);
            ps.setInt(21, this.basecolor);
            ps.setInt(22, this.addcolor);
            ps.setInt(23, this.baseprob);
            ps.setInt(24, this.secondhair);
            ps.setInt(25, this.face);
            ps.setInt(26, this.secondface);
            ps.setInt(27, this.demonMarking);
            if (this.map != null) {
               if (this.map.getForcedReturnId() != 999999999 && this.map.getForcedReturnMap() != null) {
                  ps.setInt(28, this.map.getForcedReturnId());
               } else {
                  ps.setInt(28, this.stats.getHp() < 1L ? this.map.getReturnMapId() : this.map.getId());
               }
            } else {
               ps.setInt(28, this.mapid);
            }

            ps.setLong(29, this.meso);
            ps.setShort(30, this.hpApUsed);
            if (this.map == null) {
               ps.setByte(31, (byte)0);
            } else {
               MaplePortal closest = this.map.findClosestSpawnpoint(this.getTruePosition());
               ps.setByte(31, (byte)(closest != null ? closest.getId() : 0));
            }

            ps.setInt(32, this.party == null ? -1 : this.party.getId());
            ps.setShort(33, this.buddylist == null ? 20 : (short)this.buddylist.getCapacity());
            ps.setByte(34, this.subcategory);
            ps.setInt(35, this.marriageId);
            ps.setInt(36, this.currentrep);
            ps.setInt(37, this.totalrep);
            ps.setShort(38, this.fatigue);
            ps.setInt(39, ((MapleTrait)this.traits.get(MapleTrait.MapleTraitType.charm)).getTotalExp());
            ps.setInt(40, ((MapleTrait)this.traits.get(MapleTrait.MapleTraitType.charisma)).getTotalExp());
            ps.setInt(41, ((MapleTrait)this.traits.get(MapleTrait.MapleTraitType.craft)).getTotalExp());
            ps.setInt(42, ((MapleTrait)this.traits.get(MapleTrait.MapleTraitType.insight)).getTotalExp());
            ps.setInt(43, ((MapleTrait)this.traits.get(MapleTrait.MapleTraitType.sense)).getTotalExp());
            ps.setInt(44, ((MapleTrait)this.traits.get(MapleTrait.MapleTraitType.will)).getTotalExp());
            ps.setInt(45, this.totalWins);
            ps.setInt(46, this.totalLosses);
            ps.setInt(47, this.pvpExp);
            ps.setInt(48, this.pvpPoints);
            ps.setInt(49, this.reborns);
            ps.setInt(50, this.apstorage);
            ps.setString(51, this.name);
            ps.setInt(52, this.honourExp);
            ps.setInt(53, this.honorLevel);
            ps.setInt(54, 0);
            ps.setInt(55, this.itcafetime);
            sps.delete(0, sps.toString().length());

            for(int j = 0; j < 3; ++j) {
               if (this.pets[j] != null) {
                  sps.append(this.pets[j].getUniqueId());
               } else {
                  sps.append("-1");
               }

               sps.append(",");
            }

            sp = sps.toString();
            ps.setString(56, sp.substring(0, sp.length() - 1));
            ps.setInt(57, this.LinkMobCount);
            ps.setInt(58, this.secondbasecolor);
            ps.setInt(59, this.secondaddcolor);
            ps.setInt(60, this.secondbaseprob);
            ps.setInt(61, this.MesoChairCount);
            ps.setInt(62, this.betaclothes);
            StringBuilder str = new StringBuilder();
            Iterator var7 = this.getExceptionList().iterator();

            while(var7.hasNext()) {
               Integer excep = (Integer)var7.next();
               sps.append(excep);
               sps.append(",");
            }

            String exp = str.toString();
            if (exp.length() > 0) {
               ps.setString(63, exp.substring(0, exp.length() - 1));
            } else {
               ps.setString(63, exp);
            }

            ps.setInt(64, this.id);
            if (ps.executeUpdate() < 1) {
               ps.close();
               var10000 = false;
               return var10000;
            }

            ps.close();
            var10000 = true;
         } catch (SQLException var10) {
            var10.printStackTrace();
            return false;
         }

         return var10000;
      }
   }

   public void savePetToDB(Connection con) {
      for(int i = 0; i < this.pets.length; ++i) {
         if (this.pets[i] != null) {
            this.pets[i].saveToDb(con);
         }
      }

   }

   public void saveMatrixToDB(Connection con) {
      try {
         this.deleteWhereCharacterId(con, "DELETE FROM matrix WHERE charid = ?");
         Iterator var2 = this.matrixs.iterator();

         while(var2.hasNext()) {
            VMatrix matrix = (VMatrix)var2.next();
            PreparedStatement ps = con.prepareStatement("INSERT INTO matrix (`level`, `position`, `id`, `unlock`, `charid`) VALUES (?, ?, ?, ?, ?)");
            ps.setInt(1, matrix.getLevel());
            ps.setInt(2, matrix.getPosition());
            ps.setInt(3, matrix.getId());
            ps.setByte(4, (byte)(matrix.isUnLock() ? 1 : 0));
            ps.setInt(5, this.id);
            ps.execute();
            ps.close();
         }
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   public void saveCoreToDB(Connection con) {
      try {
         this.deleteWhereCharacterId(con, "DELETE FROM core WHERE charid = ?");
         Iterator var2 = this.cores.iterator();

         while(var2.hasNext()) {
            Core core = (Core)var2.next();
            PreparedStatement ps = con.prepareStatement("INSERT INTO core (crcid, coreid, level, exp, state, maxlevel, skill1, skill2, skill3, position, islock, charid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setLong(1, core.getCrcId());
            ps.setInt(2, core.getCoreId());
            ps.setInt(3, core.getLevel());
            ps.setInt(4, core.getExp());
            ps.setInt(5, core.getState());
            ps.setInt(6, core.getMaxlevel());
            ps.setInt(7, core.getSkill1());
            ps.setInt(8, core.getSkill2());
            ps.setInt(9, core.getSkill3());
            ps.setInt(10, core.getPosition());
            ps.setInt(11, core.isLock() ? 1 : 0);
            ps.setInt(12, this.id);
            ps.execute();
            ps.close();
         }
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   public void saveSteelToDB(Connection con) {
      try {
         this.deleteWhereCharacterId(con, "DELETE FROM stolen WHERE characterid = ?");
         Iterator var2 = this.stolenSkills.iterator();

         while(var2.hasNext()) {
            Pair<Integer, Boolean> st = (Pair)var2.next();
            PreparedStatement ps = con.prepareStatement("INSERT INTO stolen (characterid, skillid, chosen) VALUES (?, ?, ?)");
            ps.setInt(1, this.id);
            ps.setInt(2, (Integer)st.left);
            ps.setInt(3, (Boolean)st.right ? 1 : 0);
            ps.execute();
            ps.close();
         }
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   public void saveMacroToDB(Connection con) {
      try {
         this.deleteWhereCharacterId(con, "DELETE FROM skillmacros WHERE characterid = ?");

         for(int i = 0; i < 5; ++i) {
            SkillMacro macro = this.skillMacros[i];
            if (macro != null) {
               PreparedStatement ps = con.prepareStatement("INSERT INTO skillmacros (characterid, skill1, skill2, skill3, name, shout, position) VALUES (?, ?, ?, ?, ?, ?, ?)");
               ps.setInt(1, this.id);
               ps.setInt(2, macro.getSkill1());
               ps.setInt(3, macro.getSkill2());
               ps.setInt(4, macro.getSkill3());
               ps.setString(5, macro.getName());
               ps.setInt(6, macro.getShout());
               ps.setInt(7, i);
               ps.execute();
               ps.close();
            }
         }
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   public void saveSlotToDB(Connection con) {
      try {
         this.deleteWhereCharacterId(con, "DELETE FROM inventoryslot WHERE characterid = ?");
         PreparedStatement ps = con.prepareStatement("INSERT INTO inventoryslot (characterid, `equip`, `use`, `setup`, `etc`, `cash`, `cody`) VALUES (?, ?, ?, ?, ?, ?, ?)");
         ps.setInt(1, this.id);
         ps.setShort(2, this.getInventory(MapleInventoryType.EQUIP).getSlotLimit());
         ps.setShort(3, this.getInventory(MapleInventoryType.USE).getSlotLimit());
         ps.setShort(4, this.getInventory(MapleInventoryType.SETUP).getSlotLimit());
         ps.setShort(5, this.getInventory(MapleInventoryType.ETC).getSlotLimit());
         ps.setShort(6, this.getInventory(MapleInventoryType.CASH).getSlotLimit());
         ps.setShort(7, this.getInventory(MapleInventoryType.CODY).getSlotLimit());
         ps.execute();
         ps.close();
      } catch (Exception var3) {
         var3.printStackTrace();
      }

   }

   public void saveQuestInfoToDB(Connection con) {
      try {
         this.deleteWhereCharacterId(con, "DELETE FROM questinfo WHERE characterid = ?");
         PreparedStatement ps = con.prepareStatement("INSERT INTO questinfo (`characterid`, `quest`, `customData`) VALUES (?, ?, ?)");
         ps.setInt(1, this.id);
         Iterator var3 = this.questinfo.entrySet().iterator();

         while(var3.hasNext()) {
            Entry<Integer, String> q = (Entry)var3.next();
            ps.setInt(2, (Integer)q.getKey());
            ps.setString(3, (String)q.getValue());
            ps.execute();
         }

         ps.close();
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   public void saveQuestStatusToDB(Connection con) {
      try {
         this.deleteWhereCharacterId(con, "DELETE FROM queststatus WHERE characterid = ?");
         PreparedStatement ps = con.prepareStatement("INSERT INTO queststatus (`queststatusid`, `characterid`, `quest`, `status`, `time`, `forfeited`, `customData`) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)", 1);
         ps.setInt(1, this.id);

         ResultSet rs;
         for(Iterator var3 = this.quests.values().iterator(); var3.hasNext(); rs.close()) {
            MapleQuestStatus q = (MapleQuestStatus)var3.next();
            ps.setInt(2, q.getQuest().getId());
            ps.setInt(3, q.getStatus());
            ps.setInt(4, (int)(q.getCompletionTime() / 1000L));
            ps.setInt(5, q.getForfeited());
            ps.setString(6, q.getCustomData());
            ps.execute();
            rs = ps.getGeneratedKeys();
            if (q.hasMobKills()) {
               rs.next();
               Iterator var6 = q.getMobKills().keySet().iterator();

               while(var6.hasNext()) {
                  int mob = (Integer)var6.next();
                  PreparedStatement pse = con.prepareStatement("INSERT INTO queststatusmobs VALUES (DEFAULT, ?, ?, ?)");
                  pse.setInt(1, rs.getInt(1));
                  pse.setInt(2, mob);
                  pse.setInt(3, q.getMobKills(mob));
                  pse.execute();
                  pse.close();
               }
            }
         }

         ps.close();
      } catch (Exception var9) {
         var9.printStackTrace();
      }

   }

   public void saveSkillToDB(Connection con) {
      try {
         this.deleteWhereCharacterId(con, "DELETE FROM skills WHERE characterid = ?");
         PreparedStatement ps = con.prepareStatement("INSERT INTO skills (characterid, skillid, skilllevel, masterlevel, expiration) VALUES (?, ?, ?, ?, ?)");
         ps.setInt(1, this.id);
         Iterator var3 = this.skills.entrySet().iterator();

         while(true) {
            Entry skill;
            do {
               if (!var3.hasNext()) {
                  ps.close();
                  return;
               }

               skill = (Entry)var3.next();
            } while(!GameConstants.isApplicableSkill(((Skill)skill.getKey()).getId()) && !SkillFactory.getSkill(((Skill)skill.getKey()).getId()).getName().startsWith("쓸만한"));

            ps.setInt(2, ((Skill)skill.getKey()).getId());
            ps.setInt(3, ((SkillEntry)skill.getValue()).skillevel);
            ps.setByte(4, ((SkillEntry)skill.getValue()).masterlevel);
            ps.setLong(5, ((SkillEntry)skill.getValue()).expiration);
            ps.execute();
         }
      } catch (Exception var5) {
         var5.printStackTrace();
      }
   }

   public void saveInnerToDB(Connection con) {
      try {
         if (this.innerSkills != null) {
            this.deleteWhereCharacterId(con, "DELETE FROM inner_ability_skills WHERE player_id = ?");
            PreparedStatement ps = con.prepareStatement("INSERT INTO inner_ability_skills (player_id, skill_id, skill_level, max_level, rank) VALUES (?, ?, ?, ?, ?)");
            ps.setInt(1, this.id);

            for(int i = 0; i < this.innerSkills.size(); ++i) {
               ps.setInt(2, ((InnerSkillValueHolder)this.innerSkills.get(i)).getSkillId());
               ps.setInt(3, ((InnerSkillValueHolder)this.innerSkills.get(i)).getSkillLevel());
               ps.setInt(4, ((InnerSkillValueHolder)this.innerSkills.get(i)).getMaxLevel());
               ps.setInt(5, ((InnerSkillValueHolder)this.innerSkills.get(i)).getRank());
               ps.execute();
            }

            ps.close();
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }

   }

   public void saveCSToDB(Connection con) {
      try {
         PreparedStatement ps = con.prepareStatement("UPDATE accounts SET nxCredit = ?, ACash = ?, mPoints = ? WHERE `id` = ?");
         ps.setInt(1, this.nxcredit);
         ps.setInt(2, 0);
         ps.setInt(3, this.maplepoints);
         ps.setInt(4, this.getAccountID());
         ps.executeUpdate();
         ps.close();
      } catch (Exception var3) {
         var3.printStackTrace();
      }

   }

   public void saveCoodinationToDB(Connection con) {
      try {
         this.deleteWhereCharacterId(con, "DELETE FROM coodination WHERE `playerid` = ?");
         PreparedStatement ps = con.prepareStatement("INSERT INTO coodination (`playerid`, `position`, `gender`, `skin`, `face`, `hair`, `equip1`, `equip2`, `equip3`, `equip4`, `equip5`, `equip6`, `equip7`, `equip8`, `equip9`, `weaponstickerid`, `weaponid`, `subweaponid`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
         ps.setInt(1, this.id);

         for(int i = 0; i < this.coodination.size(); ++i) {
            AvatarLook a = (AvatarLook)this.coodination.get(i);
            if (a != null) {
               a.save(i, ps);
               ps.execute();
            }
         }

         ps.close();
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   public void saveKeyValue_BossToDB(Connection con) {
      PreparedStatement ps = null;

      try {
         this.deleteWhereCharacterId(con, "DELETE FROM keyvalue_boss WHERE `id` = ?");
         ps = con.prepareStatement("INSERT INTO keyvalue_boss (`id`, `key`, `value`) VALUES (?, ?, ?)");
         ps.setInt(1, this.id);
         Iterator var3 = this.keyValues_boss.entrySet().iterator();

         while(var3.hasNext()) {
            Entry<String, String> keyValue = (Entry)var3.next();
            ps.setString(2, (String)keyValue.getKey());
            ps.setString(3, (String)keyValue.getValue());
            ps.execute();
         }

         ps.close();
      } catch (Exception var13) {
         var13.printStackTrace();
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (Exception var12) {
            var12.printStackTrace();
         }

      }

   }

   public void saveKeyValueToDB(Connection con) {
      try {
         this.deleteWhereCharacterId(con, "DELETE FROM keyvalue WHERE `id` = ?");
         PreparedStatement ps = con.prepareStatement("INSERT INTO keyvalue (`id`, `key`, `value`) VALUES (?, ?, ?)");
         ps.setInt(1, this.id);
         Iterator var3 = this.keyValues.entrySet().iterator();

         while(var3.hasNext()) {
            Entry<String, String> keyValue = (Entry)var3.next();
            ps.setString(2, (String)keyValue.getKey());
            ps.setString(3, (String)keyValue.getValue());
            ps.execute();
         }

         ps.close();
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   public void saveCooldownToDB(Connection con, boolean dc) {
      try {
         List<MapleCoolDownValueHolder> cd = this.getCooldowns();
         this.deleteWhereCharacterId(con, "DELETE FROM skills_cooldowns WHERE charid = ?");
         if (dc && cd.size() > 0) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO skills_cooldowns (charid, SkillID, StartTime, length) VALUES (?, ?, ?, ?)");
            ps.setInt(1, this.getId());
            Iterator var5 = cd.iterator();

            while(var5.hasNext()) {
               MapleCoolDownValueHolder cooling = (MapleCoolDownValueHolder)var5.next();
               ps.setInt(2, cooling.skillId);
               ps.setLong(3, cooling.startTime);
               ps.setLong(4, cooling.length);
               ps.execute();
            }

            ps.close();
         }
      } catch (Exception var7) {
         var7.printStackTrace();
      }

   }

   public void saveRockToDB(Connection con) {
      try {
         this.deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?");
         PreparedStatement ps = con.prepareStatement("INSERT INTO savedlocations (characterid, `locationtype`, `map`) VALUES (?, ?, ?)");
         ps.setInt(1, this.id);
         SavedLocationType[] var3 = SavedLocationType.values();
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            SavedLocationType savedLocationType = var3[var5];
            if (this.savedLocations[savedLocationType.getValue()] != -1) {
               ps.setInt(2, savedLocationType.getValue());
               ps.setInt(3, this.savedLocations[savedLocationType.getValue()]);
               ps.execute();
            }
         }

         ps.close();
      } catch (Exception var7) {
         var7.printStackTrace();
      }

   }

   public void saveExtendedSlotsToDB(Connection con) {
      try {
         if (this.changed_extendedSlots) {
            this.deleteWhereCharacterId(con, "DELETE FROM extendedSlots WHERE characterid = ?");
            Iterator var2 = this.extendedSlots.iterator();

            while(var2.hasNext()) {
               int i = (Integer)var2.next();
               if (this.getInventory(MapleInventoryType.ETC).findById(i) != null) {
                  PreparedStatement ps = con.prepareStatement("INSERT INTO extendedSlots(characterid, itemId) VALUES(?, ?) ");
                  ps.setInt(1, this.getId());
                  ps.setInt(2, i);
                  ps.execute();
               }
            }
         }
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   public void saveBuddyToDB(Connection con) {
      try {
         deleteWhereCharacterId(con, "DELETE FROM buddies WHERE accid = ? AND pending = 0", this.client.getAccID());
         PreparedStatement ps = con.prepareStatement("INSERT INTO buddies (accid, `buddyaccid`, `repname`, `pending`, `groupname`, `memo`) VALUES (?, ?, ?, 0, ?, ?)");
         ps.setInt(1, this.client.getAccID());
         Iterator var3 = this.buddylist.getBuddies().iterator();

         while(var3.hasNext()) {
            BuddylistEntry entry = (BuddylistEntry)var3.next();
            if (entry.isVisible()) {
               ps.setInt(2, entry.getAccountId());
               ps.setString(3, entry.getRepName());
               ps.setString(4, entry.getGroupName());
               ps.setString(5, entry.getMemo() == null ? "" : entry.getMemo());
               ps.execute();
            }
         }

         ps.close();
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   public void saveEmoticonToDB(Connection con) {
      try {
         this.deleteWhereCharacterId(con, "DELETE FROM emoticon WHERE charid = ?");
         Iterator var2 = this.emoticonTabs.iterator();

         while(var2.hasNext()) {
            MapleChatEmoticon em = (MapleChatEmoticon)var2.next();
            PreparedStatement ps = con.prepareStatement("INSERT INTO emoticon (charid, emoticonid, time, bookmarks) VALUES (?, ?, ?, ?)");
            ps.setInt(1, this.id);
            ps.setInt(2, em.getEmoticonid());
            ps.setLong(3, em.getTime());
            ps.setString(4, em.getBookmark());
            ps.execute();
            ps.close();
         }
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   public void saveSavedEmoticonToDB(Connection con) {
      try {
         this.deleteWhereCharacterId(con, "DELETE FROM emoticon_saved WHERE charid = ?");
         Iterator var2 = this.savedEmoticon.iterator();

         while(var2.hasNext()) {
            MapleSavedEmoticon em = (MapleSavedEmoticon)var2.next();
            PreparedStatement ps = con.prepareStatement("INSERT INTO emoticon_saved (charid, emoticonid, chat) VALUES (?, ?, ?)");
            ps.setInt(1, this.id);
            ps.setInt(2, em.getEmoticonid());
            ps.setString(3, em.getText());
            ps.execute();
            ps.close();
         }
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   private void deleteWhereCharacterId(Connection con, String sql) throws SQLException {
      deleteWhereCharacterId(con, sql, this.id);
   }

   public static void deleteWhereCharacterId(Connection con, String sql, int id) throws SQLException {
      PreparedStatement ps = con.prepareStatement(sql);
      ps.setInt(1, id);
      ps.executeUpdate();
      ps.close();
   }

   public void BlackMage3thDamage() {
      this.addHP(-this.getStat().getMaxHp());
      this.getMap().broadcastMessage(CField.EffectPacket.showFieldSkillEffect(this, 100006, (int)2));
      this.getMap().broadcastMessage(CField.EffectPacket.showFieldSkillEffect(this, -593945, (byte)0));
   }

   public void saveInventory(Connection con, boolean dc) throws SQLException {
      MapleInventoryType[] var3 = MapleInventoryType.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         MapleInventoryType type = var3[var5];
         if (type.getType() >= 2 && type.getType() < 6) {
            MapleInventory mapleInventory = this.getInventory(type.getType());
            synchronized(mapleInventory) {
               List<Item> items = this.getInventory(type.getType()).newList();
               if (this.memorialcube != null && type.getType() == 5) {
                  items.add(this.memorialcube);
               }

               if (this.returnscroll != null && type.getType() == 2) {
                  items.add(this.returnscroll);
               }

               if (con != null) {
                  ItemLoader.INVENTORY.saveItems(items, con, this.id, type, dc);
               } else {
                  ItemLoader.INVENTORY.saveItems(items, this.id, type, dc);
               }
            }
         }
      }

      ArrayList<Item> equips = new ArrayList();
      Iterator var13 = this.getInventory(MapleInventoryType.EQUIP).newList().iterator();

      Item item;
      while(var13.hasNext()) {
         item = (Item)var13.next();
         equips.add(item);
      }

      var13 = this.getInventory(MapleInventoryType.EQUIPPED).newList().iterator();

      while(var13.hasNext()) {
         item = (Item)var13.next();
         equips.add(item);
      }

      if (this.choicepotential != null) {
         equips.add(this.choicepotential);
      }

      if (con != null) {
         ItemLoader.INVENTORY.saveItems(equips, con, this.id, MapleInventoryType.EQUIP, dc);
      } else {
         ItemLoader.INVENTORY.saveItems(equips, this.id, MapleInventoryType.EQUIP, dc);
      }

      equips.clear();
      var13 = this.getInventory(MapleInventoryType.CODY).newList().iterator();

      while(var13.hasNext()) {
         item = (Item)var13.next();
         equips.add(item);
      }

      if (con != null) {
         ItemLoader.INVENTORY.saveItems(equips, con, this.id, MapleInventoryType.CODY, dc);
      } else {
         ItemLoader.INVENTORY.saveItems(equips, this.id, MapleInventoryType.CODY, dc);
      }

   }

   public void saveToDB(boolean dc, boolean fromcs) {
      Connection con = null;
      ReentrantLock LockObj = new ReentrantLock();
      LockObj.lock();

      try {
         con = DatabaseConnection.getConnection();
         con.setTransactionIsolation(1);
         con.setAutoCommit(false);
         if (this.saveCharToDB(con)) {
            this.saveCoreToDB(con);
            this.saveMatrixToDB(con);
            this.saveQuestInfoToDB(con);
            this.saveQuestStatusToDB(con);
            this.saveSkillToDB(con);
            this.saveInnerToDB(con);
            this.savePetToDB(con);
            this.saveSteelToDB(con);
            this.saveMacroToDB(con);
            this.saveSlotToDB(con);
            this.saveInventory(con, dc);
            this.saveCooldownToDB(con, dc);
            this.saveRockToDB(con);
            this.saveKeyValueToDB(con);
            this.saveKeyValue_BossToDB(con);
            if (fromcs) {
               this.saveCoodinationToDB(con);
            }

            this.saveDummyToDB(con);
            this.saveMannequinToDB(con);
            this.saveEmoticonToDB(con);
            this.saveSavedEmoticonToDB(con);
            PlayerNPC.updateByCharId(this);
            this.keylayout.saveKeys(con, this.id);
            this.mount.saveMount(con, this.id);
         }

         if (this.getUnions() != null) {
            this.getUnions().savetoDB(con, this.getAccountID());
         }

         if (this.storage != null) {
            this.storage.saveToDB(con);
         }

         if (this.client != null) {
            this.saveBuddyToDB(con);
            this.saveCSToDB(con);
            this.client.saveKeyValue_BossToDB(con);
            this.client.saveKeyValueToDB(con);
            this.client.saveCustomDataToDB(con);
            this.client.saveCustomKeyValueToDB(con);
            this.client.SaveQuest(con);
         }

         con.commit();
         this.lastSaveTime = System.currentTimeMillis();
      } catch (DatabaseException var21) {
         this.lastSaveTime = System.currentTimeMillis();
         FileoutputUtil.outputFileError("Log_Packet_Except.rtf", var21);
         var21.printStackTrace();

         try {
            con.rollback();
         } catch (Exception var20) {
            var20.printStackTrace();
         }
      } catch (Exception var22) {
         FileoutputUtil.outputFileError("Log_Packet_Except.rtf", var22);
         var22.printStackTrace();

         try {
            con.rollback();
         } catch (Exception var19) {
            var19.printStackTrace();
         }
      } finally {
         try {
            if (con != null) {
               con.setTransactionIsolation(4);
               con.setAutoCommit(true);
               con.close();
            }
         } catch (SQLException var18) {
            FileoutputUtil.outputFileError("Log_Packet_Except.rtf", var18);
            var18.printStackTrace();
         }

         LockObj.unlock();
      }

   }

   public final PlayerStats getStat() {
      return this.stats;
   }

   public final void QuestInfoPacket(MaplePacketLittleEndianWriter mplew) {
      mplew.writeShort(this.questinfo.size() + this.getClient().getCustomKeyValue().size());
      Iterator var2 = this.questinfo.entrySet().iterator();

      Entry q;
      while(var2.hasNext()) {
         q = (Entry)var2.next();
         mplew.writeInt((Integer)q.getKey());
         mplew.writeMapleAsciiString(q.getValue() == null ? "" : (String)q.getValue());
      }

      var2 = this.getClient().getCustomKeyValue().entrySet().iterator();

      while(var2.hasNext()) {
         q = (Entry)var2.next();
         mplew.writeInt((Integer)q.getKey());
         mplew.writeMapleAsciiString(q.getValue() == null ? "" : (String)q.getValue());
      }

   }

   public final void specialQustInfoPacket(MaplePacketLittleEndianWriter mplew) {
      Map<Integer, String> customQuestInfo = new HashMap();
      Integer var10001 = 15;
      String var10002 = this.client.getKeyValue("dailyGiftComplete");
      customQuestInfo.put(var10001, "count=" + var10002 + ";day=" + this.client.getKeyValue("dailyGiftDay") + ";date=" + this.getKeyValue(16700, "date"));
      mplew.writeInt(customQuestInfo.size());
      Iterator var3 = customQuestInfo.entrySet().iterator();

      while(var3.hasNext()) {
         Entry<Integer, String> q = (Entry)var3.next();
         mplew.writeInt((Integer)q.getKey());
         mplew.writeMapleAsciiString(q.getValue() == null ? "" : (String)q.getValue());
      }

   }

   public final void updateInfoQuest(int questid, String data) {
      this.questinfo.put(questid, data);
      this.changed_questinfo = true;
      this.client.getSession().writeAndFlush(CWvsContext.InfoPacket.updateInfoQuest(questid, data));
   }

   public final String getInfoQuest(int questid) {
      return this.questinfo.containsKey(questid) ? (String)this.questinfo.get(questid) : "";
   }

   public final int getNumQuest() {
      int i = 0;
      Iterator var2 = this.quests.values().iterator();

      while(var2.hasNext()) {
         MapleQuestStatus q = (MapleQuestStatus)var2.next();
         if (q.getStatus() == 2 && !q.isCustom()) {
            ++i;
         }
      }

      return i;
   }

   public final byte getQuestStatus(int quest) {
      MapleQuest qq = MapleQuest.getInstance(quest);
      return this.getQuestNoAdd(qq) == null ? 0 : this.getQuestNoAdd(qq).getStatus();
   }

   public final MapleQuestStatus getQuest(MapleQuest quest) {
      return !this.quests.containsKey(quest) ? new MapleQuestStatus(quest, 0) : (MapleQuestStatus)this.quests.get(quest);
   }

   public final void setQuestAdd(MapleQuest quest, byte status, String customData) {
      if (!this.quests.containsKey(quest)) {
         MapleQuestStatus stat = new MapleQuestStatus(quest, status);
         stat.setCustomData(customData);
         this.quests.put(quest, stat);
      }

   }

   public final MapleQuestStatus getQuestNAdd(MapleQuest quest) {
      if (!this.quests.containsKey(quest)) {
         MapleQuestStatus status = new MapleQuestStatus(quest, 0);
         this.quests.put(quest, status);
         return status;
      } else {
         return (MapleQuestStatus)this.quests.get(quest);
      }
   }

   public final MapleQuestStatus getQuestNoAdd(MapleQuest quest) {
      return !this.quests.containsKey(quest) ? null : (MapleQuestStatus)this.quests.get(quest);
   }

   public final void updateQuest(MapleQuestStatus quest) {
      this.updateQuest(quest, false);
   }

   public final void updateQuest(MapleQuestStatus quest, boolean update) {
      this.quests.put(quest.getQuest(), quest);
      if (update) {
         this.client.getSession().writeAndFlush(CWvsContext.InfoPacket.updateQuest(quest));
      }

   }

   public final Map<Integer, String> getInfoQuest_Map() {
      return this.questinfo;
   }

   public final Map<MapleQuest, MapleQuestStatus> getQuest_Map() {
      return this.quests;
   }

   public SecondaryStatEffect getBuffedEffect(SecondaryStat effect) {
      return this.getBuffedEffect(effect, this.getBuffSource(effect));
   }

   public boolean checkBuffStat(SecondaryStat stat) {
      if (stat == null) {
         return false;
      } else {
         Iterator var2 = this.effects.iterator();

         Pair eff;
         do {
            if (!var2.hasNext()) {
               return false;
            }

            eff = (Pair)var2.next();
         } while(stat != eff.left);

         return true;
      }
   }

   public SecondaryStatValueHolder checkBuffStatValueHolder(SecondaryStat stat) {
      if (stat != null && this.effects != null) {
         Iterator var2 = this.effects.iterator();

         Pair eff;
         do {
            if (!var2.hasNext()) {
               return null;
            }

            eff = (Pair)var2.next();
         } while(stat != eff.left);

         return (SecondaryStatValueHolder)eff.right;
      } else {
         return null;
      }
   }

   public SecondaryStatValueHolder checkBuffStatValueHolder(SecondaryStatEffect ef, Entry<SecondaryStat, Pair<Integer, Integer>> stat) {
      if (stat != null && this.effects != null) {
         Iterator var3 = this.effects.iterator();

         Pair eff;
         do {
            if (!var3.hasNext()) {
               return null;
            }

            eff = (Pair)var3.next();
         } while(stat.getKey() != eff.getLeft() || ef.getSourceId() != ((SecondaryStatValueHolder)eff.getRight()).effect.getSourceId());

         return (SecondaryStatValueHolder)eff.right;
      } else {
         return null;
      }
   }

   public SecondaryStatValueHolder checkBuffStatValueHolder(SecondaryStat stat, int skillId) {
      if (stat != null && this.effects != null) {
         Iterator var3 = this.effects.iterator();

         Pair eff;
         do {
            if (!var3.hasNext()) {
               return null;
            }

            eff = (Pair)var3.next();
         } while(stat != eff.left || skillId != ((SecondaryStatValueHolder)eff.right).effect.getSourceId());

         return (SecondaryStatValueHolder)eff.right;
      } else {
         return null;
      }
   }

   public SecondaryStatEffect getBuffedEffect(int skillId) {
      Iterator var2 = this.effects.iterator();

      Pair eff;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         eff = (Pair)var2.next();
      } while(skillId != ((SecondaryStatValueHolder)eff.right).effect.getSourceId());

      return ((SecondaryStatValueHolder)eff.right).effect;
   }

   public SecondaryStatEffect getBuffedEffect(SecondaryStat effect, int skillid) {
      if (!this.checkBuffStat(effect)) {
         return null;
      } else {
         SecondaryStatValueHolder mbsvh = this.checkBuffStatValueHolder(effect, skillid);
         return mbsvh == null ? null : mbsvh.effect;
      }
   }

   public Integer getBuffedValue(SecondaryStat effect) {
      SecondaryStatValueHolder mbsvh = this.checkBuffStatValueHolder(effect);
      return mbsvh == null ? null : mbsvh.value;
   }

   public Integer getBuffedValue(SecondaryStat effect, int skillId) {
      SecondaryStatValueHolder mbsvh = this.checkBuffStatValueHolder(effect, skillId);
      return mbsvh == null ? null : mbsvh.value;
   }

   public int getBuffedSkill(SecondaryStat effect) {
      SecondaryStatValueHolder mbsvh = this.checkBuffStatValueHolder(effect);
      return mbsvh == null ? 0 : mbsvh.value;
   }

   public boolean getBuffedValue(int skillid) {
      if (SkillFactory.getSkill(skillid) == null) {
         return false;
      } else {
         Iterator var2 = this.effects.iterator();

         Pair eff;
         do {
            if (!var2.hasNext()) {
               return false;
            }

            eff = (Pair)var2.next();
         } while(skillid != ((SecondaryStatValueHolder)eff.right).effect.getSourceId());

         return true;
      }
   }

   public final Integer getBuffedSkill_X(SecondaryStat effect) {
      SecondaryStatValueHolder mbsvh = this.checkBuffStatValueHolder(effect);
      return mbsvh == null ? null : mbsvh.effect.getX();
   }

   public final Integer getBuffedSkill_Y(SecondaryStat effect) {
      SecondaryStatValueHolder mbsvh = this.checkBuffStatValueHolder(effect);
      return mbsvh == null ? null : mbsvh.effect.getY();
   }

   public void setDressup(boolean isdress) {
      this.isdressup = isdress;
   }

   public boolean getDressup() {
      return this.isdressup;
   }

   public int getBuffSource(SecondaryStat stat) {
      SecondaryStatValueHolder mbsvh = this.checkBuffStatValueHolder(stat);
      return mbsvh == null ? 0 : mbsvh.effect.getSourceId();
   }

   public int getItemQuantity(int itemid, boolean checkEquipped) {
      int possesed = this.inventory[GameConstants.getInventoryType(itemid).ordinal()].countById(itemid);
      if (checkEquipped) {
         possesed += this.inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
      }

      return possesed;
   }

   public void setBuffedValue(SecondaryStat effect, int value) {
      SecondaryStatValueHolder mbsvh = this.checkBuffStatValueHolder(effect);
      if (mbsvh != null) {
         mbsvh.value = value;
         this.getStat().recalcLocalStats(this);
      }
   }

   public void setBuffedValue(SecondaryStat effect, int skillid, int value) {
      SecondaryStatValueHolder mbsvh = this.checkBuffStatValueHolder(effect);
      if (mbsvh != null && this.checkBuffStat(effect)) {
         if (skillid == -1) {
            if (this.checkBuffStatValueHolder(effect) != null) {
               this.checkBuffStatValueHolder(effect).value = value;
            }
         } else if (mbsvh.effect.getSourceId() == skillid) {
            mbsvh.value = value;
         }

         this.getStat().recalcLocalStats(this);
      }
   }

   public Long getBuffedStarttime(SecondaryStat effect) {
      SecondaryStatValueHolder mbsvh = this.checkBuffStatValueHolder(effect);
      return mbsvh == null ? null : mbsvh.startTime;
   }

   public void doRecovery() {
      SecondaryStatEffect bloodEffect = this.getBuffedEffect(SecondaryStat.Recovery);
      if (bloodEffect == null) {
         this.lastRecoveryTime = 0L;
      } else {
         this.prepareRecovery();
         if (this.stats.getHp() >= this.stats.getCurrentMaxHp()) {
            this.cancelEffectFromBuffStat(SecondaryStat.Recovery);
         } else {
            this.healHP((long)bloodEffect.getX());
         }

      }
   }

   public final boolean canRecover(long now) {
      return this.lastRecoveryTime > 0L && this.lastRecoveryTime + 5000L < now;
   }

   private void prepareRecovery() {
      this.lastRecoveryTime = System.currentTimeMillis();
   }

   public boolean canDOT(long now) {
      return this.lastDOTTime > 0L && this.lastDOTTime + 8000L < now;
   }

   public boolean hasDOT() {
      return this.dotHP > 0;
   }

   public void doDOT() {
      this.addHP((long)(-(this.dotHP * 4)));
      this.dotHP = 0;
      this.lastDOTTime = 0L;
   }

   public long getNeededExp() {
      return GameConstants.getExpNeededForLevel(this.level);
   }

   public void registerEffect(SecondaryStatEffect effect, long starttime, Entry<SecondaryStat, Pair<Integer, Integer>> statup, boolean silent, int cid) {
      this.registerEffect(effect, starttime, statup, silent, cid, new ArrayList(), new ArrayList());
   }

   public void registerEffect(SecondaryStatEffect effect, long starttime, Entry<SecondaryStat, Pair<Integer, Integer>> statup, boolean silent, int cid, List<Pair<Integer, Integer>> list1, List<Pair<Integer, Integer>> list2) {
      if (effect.isRecovery()) {
         this.prepareRecovery();
      } else if (effect.isMonsterRiding_()) {
         this.getMount().startSchedule();
      }

      int value = (Integer)((Pair)statup.getValue()).left;
      if (statup.getKey() != null && statup.getValue() != null) {
         this.effects.add(new Pair((SecondaryStat)statup.getKey(), new SecondaryStatValueHolder(effect, starttime, value, (Integer)((Pair)statup.getValue()).right, cid, list1, list2)));
      } else {
         System.out.println("NULL EFFECT : " + effect.getSourceId());
      }

      if (!silent) {
         this.stats.recalcLocalStats(this);
      }

   }

   public void updateEffect(SecondaryStatEffect effect, SecondaryStat stat, int addDuration) {
      if (stat != null && effect != null) {
         SecondaryStatValueHolder vh = this.checkBuffStatValueHolder(stat, effect.getSourceId());
         if (vh != null && this.getBuffedValue(stat, effect.getSourceId()) != null) {
            vh.localDuration += addDuration;
            Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
            statups.put(stat, new Pair(this.getBuffedValue(stat, effect.getSourceId()), (int)this.getBuffLimit(effect.getSourceId())));
            this.client.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, effect, this));
         }

      }
   }

   public void cancelBuffStat(int sourceId, List<Pair<SecondaryStat, SecondaryStatValueHolder>> statups, List<MapleSummon> summons, boolean overwrite) {
      int allSize = this.effects.size();
      this.effects.removeAll(statups);
      if (!overwrite && !statups.isEmpty()) {
         if (this.isGMName("GM하인즈")) {
            this.dropMessageGM(-8, "현재 버프 사이즈 : (" + allSize + " - " + statups.size() + " = " + this.effects.size() + ")");
         }

         this.getStat().recalcLocalStats(this);
      }

      Iterator var6 = summons.iterator();

      while(var6.hasNext()) {
         MapleSummon remove = (MapleSummon)var6.next();
         if (!GameConstants.isAfterRemoveSummonSkill(sourceId) && remove.getLastAttackTime() <= 0L) {
            remove.removeSummon(this.map, false);
         }
      }

   }

   public void setAttackerSkill(int attack) {
      this.setKeyValue(53714, "atk", attack.makeConcatWithConstants<invokedynamic>(attack));
      if (this.getBuffedEffect(80002924) != null) {
         this.autoDeregisterBuffStats(80002924, true);
      }

      SkillFactory.getSkill(80002924).getEffect(1).applyTo(this.getPlayer(), 0);
   }

   public void addAttackerSkill(int attack) {
      if (this.getKeyValue(53714, "atk") == -1L) {
         this.setKeyValue(53714, "atk", "0");
      }

      long k = this.getKeyValue(53714, "atk");
      this.setKeyValue(53714, "atk", ((long)attack + k).makeConcatWithConstants<invokedynamic>((long)attack + k));
      if (this.getBuffedEffect(80002924) != null) {
         this.autoDeregisterBuffStats(80002924, true);
      }

      SkillFactory.getSkill(80002924).getEffect(1).applyTo(this.getPlayer(), 0);
   }

   public Map<SecondaryStat, Pair<Integer, Integer>> autoDeregisterBuffStats(int sourceId, boolean overwrite) {
      HashMap<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
      ArrayList<Pair<SecondaryStat, SecondaryStatValueHolder>> removes = new ArrayList();
      ArrayList<MapleSummon> toRemove = new ArrayList();
      this.dropMessageGM(6, "cancelAutoBuff : " + sourceId);
      if (sourceId == 33001007) {
         sourceId = GameConstants.getSelectJaguarSkillId(GameConstants.getMountItem(33001001, this));
      }

      Iterator var6 = this.effects.iterator();

      while(var6.hasNext()) {
         Pair<SecondaryStat, SecondaryStatValueHolder> effect = (Pair)var6.next();
         SecondaryStatValueHolder vh = (SecondaryStatValueHolder)effect.right;
         if (vh != null && vh.effect.getSourceId() == sourceId) {
            removes.add(new Pair(effect.left, vh));
            statups.put((SecondaryStat)effect.left, new Pair(vh.value, vh.localDuration));
         }

         List summon;
         if (!GameConstants.isAfterRemoveSummonSkill(sourceId) && sourceId != 400021047 && (summon = this.getSummons(sourceId)) != null) {
            toRemove.addAll(summon);
         }
      }

      this.cancelBuffStat(sourceId, removes, toRemove, overwrite);
      return statups;
   }

   public Map<SecondaryStat, Pair<Integer, Integer>> deregisterBuffStats(int sourceId, List<SecondaryStat> stats, boolean overwrite, boolean auto) {
      HashMap<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
      ArrayList<Pair<SecondaryStat, SecondaryStatValueHolder>> removes = new ArrayList();
      ArrayList<MapleSummon> toRemove = new ArrayList();
      if (this.isGMName("GM하인즈")) {
         this.dropMessageGM(6, "cancelBuff : " + sourceId);
      }

      if (sourceId == 33001007) {
         sourceId = GameConstants.getSelectJaguarSkillId(GameConstants.getMountItem(33001001, this));
      }

      Iterator var8 = this.effects.iterator();

      while(var8.hasNext()) {
         Pair<SecondaryStat, SecondaryStatValueHolder> effect = (Pair)var8.next();
         SecondaryStatValueHolder vh = (SecondaryStatValueHolder)effect.right;
         if ((stats.contains(effect.left) || vh != null && ((SecondaryStat)effect.left).isStacked() && vh.effect.getSourceId() == sourceId && !overwrite && !auto) && vh.effect.getSourceId() == sourceId) {
            removes.add(new Pair(effect.left, vh));
            statups.put((SecondaryStat)effect.left, new Pair(vh.value, vh.localDuration));
         }

         List summon;
         if (!GameConstants.isAfterRemoveSummonSkill(sourceId) && sourceId != 400021047 && (summon = this.getSummons(sourceId)) != null) {
            toRemove.addAll(summon);
         }
      }

      this.cancelBuffStat(sourceId, removes, toRemove, overwrite);
      return statups;
   }

   public void cancelEffect(SecondaryStatEffect effect) {
      this.cancelEffect(effect, (List)null, false);
   }

   public void cancelEffect(SecondaryStatEffect effect, List<SecondaryStat> stats) {
      this.cancelEffect(effect, stats, false);
   }

   public void cancelEffect(SecondaryStatEffect effect, List<SecondaryStat> stats, boolean overwrite) {
      this.cancelEffect(effect, stats, overwrite, false);
   }

   public void cancelEffect(SecondaryStatEffect effect, List<SecondaryStat> stats, boolean overwrite, boolean auto) {
      if (effect != null) {
         new HashMap();
         Map statups;
         if (stats != null && !stats.isEmpty()) {
            statups = this.deregisterBuffStats(effect.getSourceId(), stats, overwrite, auto);
         } else {
            statups = this.autoDeregisterBuffStats(effect.getSourceId(), overwrite);
         }

         if (effect.isMonsterRiding()) {
            statups.put(SecondaryStat.RideVehicle, new Pair(SecondaryStatEffect.parseMountInfo(this, effect.getSourceId()), effect.getDuration()));
         }

         ArrayList mobList;
         if (effect.getSourceId() == 400001010) {
            mobList = new ArrayList();
            this.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(400001011, mobList, true, 0));
         }

         if (effect.getSourceId() == 80002633) {
            mobList = new ArrayList();
            this.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(80002634, mobList, true, 0));
         }

         if (effect.isMagicDoor()) {
            if (!this.getDoors().isEmpty()) {
               this.removeDoor();
               this.silentPartyUpdate();
            }
         } else if (effect.isMechDoor()) {
            if (!this.getMechDoors().isEmpty()) {
               this.removeMechDoor();
            }
         } else if (statups.containsKey(SecondaryStat.Reincarnation)) {
            if (this.getReinCarnation() <= 0) {
               this.changeCooldown(1320019, -SkillFactory.getSkill(1320019).getEffect(this.getSkillLevel(1320019)).getY() * 1000);
            }

            this.setReinCarnation(0);
         } else if (effect.isMonsterRiding_()) {
            this.getMount().cancelSchedule();
         } else if (statups.containsKey(SecondaryStat.BulletParty)) {
            this.bulletParty = 0;
         } else if (effect.getSourceId() == 400041029) {
            this.setXenonSurplus(this.getXenonSurplus() >= 20 ? 20 : this.getXenonSurplus(), SkillFactory.getSkill(30020232));
         } else if (effect.getSourceId() == 400051002) {
            this.transformEnergyOrb = 0;
         } else if (statups.containsKey(SecondaryStat.ElementalCharge) && !overwrite) {
            this.elementalCharge = 0;
            this.lastElementalCharge = 0;
            if (this.getSkillLevel(400011052) > 0 && this.getBuffedValue(SecondaryStat.BlessedHammer) != null) {
               this.cancelEffectFromBuffStat(SecondaryStat.BlessedHammer);
            }

            if (this.getSkillLevel(400011053) > 0 && this.getBuffedValue(SecondaryStat.BlessedHammer2) != null) {
               this.cancelEffectFromBuffStat(SecondaryStat.BlessedHammer2);
            }
         } else if (effect.getSourceId() == 400011052 && !overwrite) {
            this.elementalCharge = 0;
            this.lastElementalCharge = 0;
            statups.put(SecondaryStat.BlessedHammer, new Pair(0, 0));
            this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, effect, this));
            this.getMap().broadcastMessage(this, CWvsContext.BuffPacket.giveForeignBuff(this, statups, effect), false);
         } else if (effect.getSourceId() == 400011053 && !overwrite) {
            this.elementalCharge = 0;
            this.lastElementalCharge = 0;
            statups.put(SecondaryStat.BlessedHammer2, new Pair(0, 0));
            this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, effect, this));
            this.getMap().broadcastMessage(this, CWvsContext.BuffPacket.giveForeignBuff(this, statups, effect), false);
         }

         if (statups.containsKey(SecondaryStat.WeaponVariety) && !overwrite) {
            this.weaponChanges1.clear();
         }

         if (statups.containsKey(SecondaryStat.RideVehicle) && this.getSkillCustomValue(this.getMapId()) != null && this.getMapId() == ServerConstants.warpMap) {
            this.client.send(CField.UIPacket.detailShowInfo("휴식 포인트 적립을 그만둡니다.", 3, 20, 20));
            this.removeSkillCustomInfo(this.getMapId());
         }

         Iterator var7;
         Iterator var23;
         MapleCharacter curChar2;
         MaplePartyCharacter chr2;
         Iterator<MapleSummon> summons1 = this.getMap().getAllSummonsThreadsafe().iterator();
         MapleMonster monster;
         int size;
         int i;
         MapleSummon summon;
         int skillid;
         MapleSummon sum2;
         SecondAtom sa;
         MapleSummon su3;
         ArrayList sum;
         label895:
         switch(effect.getSourceId()) {
         case 1211014:
         case 2221054:
         case 32001016:
         case 32101009:
         case 32111012:
         case 32121017:
         case 32121018:
         case 63121044:
         case 100001263:
         case 100001264:
         case 400021006:
            if (this.getSkillCustomValue0(effect.getSourceId()) == (long)this.getId() && this.getParty() != null) {
               var7 = this.party.getMembers().iterator();

               while(var7.hasNext()) {
                  chr2 = (MaplePartyCharacter)var7.next();
                  if (chr2 != null && chr2.getMapid() == this.getMapId() && chr2.getChannel() == this.client.getChannel()) {
                     curChar2 = this.client.getChannelServer().getPlayerStorage().getCharacterByName(chr2.getName());
                     if (curChar2 != null && curChar2.getBuffedValue(effect.getSourceId())) {
                        curChar2.cancelEffect(effect);
                     }
                  }
               }
            }
            break;
         case 2321054:
            if (this.getBuffedEffect(400021032) != null || this.getBuffedEffect(400021033) != null) {
               var7 = this.getMap().getAllSummonsThreadsafe().iterator();

               label754:
               while(true) {
                  do {
                     do {
                        if (!var7.hasNext()) {
                           long bufftime;
                           if (this.getBuffedEffect(400021032) != null) {
                              bufftime = this.getBuffLimit(400021032);

                              while(this.getBuffedValue(400021032)) {
                                 this.cancelEffect(this.getBuffedEffect(400021032));
                              }

                              SkillFactory.getSkill(400021033).getEffect(this.getSkillLevel(400021032)).applyTo(this, false, (int)bufftime);
                           } else if (this.getBuffedEffect(400021033) != null) {
                              bufftime = this.getBuffLimit(400021033);

                              while(this.getBuffedValue(400021033)) {
                                 this.cancelEffect(this.getBuffedEffect(400021033));
                              }

                              SkillFactory.getSkill(400021032).getEffect(this.getSkillLevel(400021032)).applyTo(this, false, (int)bufftime);
                           }
                           break label754;
                        }

                        sum2 = (MapleSummon)var7.next();
                     } while(sum2.getOwner().getId() != this.getId());
                  } while(sum2.getSkill() != 400021032 && sum2.getSkill() != 400021033);

                  sum2.removeSummon(this.getMap(), false);
                  this.removeSummon(sum2);
               }
            }
            break;
         case 5220023:
         case 5220024:
         case 5220025:
         case 5221022:
         case 12120013:
         case 12120014:
         case 14111024:
         case 35111002:
         case 152101000:
         case 162101003:
         case 162101006:
         case 162121012:
         case 162121015:
            sum = new ArrayList();

            while(summons1.hasNext()) {
               sum2 = (MapleSummon)summons1.next();
               if (sum2.getOwner().getId() == this.getId() && sum2.getSkill() == effect.getSourceId()) {
                  sum.add(sum2);
               }
            }

            var23 = sum.iterator();

            while(true) {
               if (!var23.hasNext()) {
                  break label895;
               }

               su3 = (MapleSummon)var23.next();
               su3.removeSummon(this.map, false);
            }
         case 11101031:
            if (!overwrite) {
               if (!this.getBuffedValue(11121012)) {
                  if (this.getBuffedValue(11121011)) {
                     while(this.getBuffedValue(11121011)) {
                        this.cancelEffect(this.getBuffedEffect(11121011));
                     }

                     SkillFactory.getSkill(11001025).getEffect(this.getSkillLevel(11001025)).applyTo(this);
                  }
               } else {
                  while(this.getBuffedValue(11121012)) {
                     this.cancelEffect(this.getBuffedEffect(11121012));
                  }

                  SkillFactory.getSkill(11001024).getEffect(this.getSkillLevel(11001024)).applyTo(this);
               }
            }
            break;
         case 15001022:
            if (!overwrite && statups.containsKey(SecondaryStat.CygnusElementSkill)) {
               while(true) {
                  if (!this.getBuffedValue(effect.getSourceId())) {
                     break label895;
                  }

                  this.cancelEffect(this.getBuffedEffect(effect.getSourceId()));
               }
            }
         case 14121054:
            sum = new ArrayList();

            while(true) {
               do {
                  do {
                     if (!summons1.hasNext()) {
                        var23 = sum.iterator();

                        while(true) {
                           if (!var23.hasNext()) {
                              break label895;
                           }

                           su3 = (MapleSummon)var23.next();
                           su3.removeSummon(this.map, false);
                        }
                     }

                     sum2 = (MapleSummon)summons1.next();
                  } while(sum2.getOwner().getId() != this.getId());
               } while(sum2.getSkill() != 14121054 && sum2.getSkill() != 14121055 && sum2.getSkill() != 14121056);

               sum.add(sum2);
            }
         case 21111030:
            if (!overwrite) {
               this.setCombo((short)800);
               SkillFactory.getSkill(21000000).getEffect(this.getSkillLevel(21000000)).applyTo(this, false);
               this.getClient().getSession().writeAndFlush(CField.aranCombo(this.getCombo()));
            }
            break;
         case 25121133:
            if (!overwrite) {
               try {
                  while(summons1.hasNext()) {
                     summon = (MapleSummon)summons1.next();
                     if (summon.getOwner().getId() == this.getId() && summon.getSkill() == effect.getSourceId()) {
                        summon.removeSummon(this.getMap(), false);
                        sum2 = this.getSummon(summon.getSkill());
                        if (sum2 != null) {
                           this.removeSummon(summon);
                        }
                     }
                  }

                  if (effect.getSourceId() == 25121133) {
                     List<MapleMapItem> items = this.getMap().getAllItemsThreadsafe();
                     var23 = items.iterator();

                     while(var23.hasNext()) {
                        MapleMapItem j = (MapleMapItem)var23.next();
                        if (j.getItemId() == 4001847) {
                           j.expire(this.getMap());
                        }
                     }
                  }
               } catch (Throwable var13) {
               }
            }
            break;
         case 27101003:
            this.setBlessofDarkness((byte)0);
            break;
         case 33001007:
         case 33001008:
         case 33001009:
         case 33001010:
         case 33001011:
         case 33001012:
         case 33001013:
         case 33001014:
         case 33001015:
         case 400031005:
            while(true) {
               if (!summons1.hasNext()) {
                  break label895;
               }

               summon = (MapleSummon)summons1.next();
               if (summon.getOwner().getId() == this.getId() && summon.getSkill() >= 33001007 && summon.getSkill() <= 33001015) {
                  boolean remove = effect.getSourceId() == 400031005 ? summon.getSkill() != GameConstants.getSelectJaguarSkillId(GameConstants.getMountItem(33001001, this)) : summon.getSkill() == GameConstants.getSelectJaguarSkillId(GameConstants.getMountItem(33001001, this));
                  if (remove) {
                     summon.removeSummon(this.getMap(), false);
                     su3 = this.getSummon(summon.getSkill());
                     if (su3 != null) {
                        this.removeSummon(summon);
                     }
                  }
               }
            }
         case 35001002:
            if (overwrite) {
               statups.clear();
               statups.put(SecondaryStat.IndieSpeed, new Pair(30, 0));
               statups.put(SecondaryStat.IndieBooster, new Pair(-1, 0));
            }
            break;
         case 35111003:
            if (overwrite) {
               statups.clear();
               statups.put(SecondaryStat.CriticalIncrease, new Pair(1, 0));
            }
            break;
         case 37121004:
            var7 = this.getMap().getAllMonster().iterator();

            do {
               if (!var7.hasNext()) {
                  break label895;
               }

               monster = (MapleMonster)var7.next();
            } while(monster.getBuff(37121004) == null);

            monster.cancelStatus(MonsterStatus.MS_Freeze, monster.getBuff(37121004));
            break;
         case 51111008:
            if (!overwrite && this.getParty() != null) {
               var7 = this.party.getMembers().iterator();

               while(var7.hasNext()) {
                  chr2 = (MaplePartyCharacter)var7.next();
                  if (chr2 != null && chr2.getMapid() == this.getMapId() && chr2.getChannel() == this.client.getChannel()) {
                     curChar2 = this.client.getChannelServer().getPlayerStorage().getCharacterByName(chr2.getName());
                     if (curChar2 != null && curChar2.getBuffedValue(effect.getSourceId())) {
                        curChar2.cancelEffect(effect);
                     }
                  }
               }
            }
            break;
         case 61111008:
         case 61120008:
         case 61121053:
            if (this.getBuffedValue(SecondaryStat.StopForceAtominfo) != null) {
               int[] sa = new int[]{61101002, 61110211, 61120007, 61121217};
               size = sa.length;

               for(i = 0; i < size; ++i) {
                  Integer skill = sa[i];
                  if (this.getBuffedValue(skill)) {
                     this.cancelEffect(this.getBuffedEffect(skill));
                  }
               }

               if (effect.getSourceId() == 61111008) {
                  SkillFactory.getSkill(61110211).getEffect(this.getSkillLevel(61101002)).applyTo(this);
               } else {
                  SkillFactory.getSkill(61120007).getEffect(this.getSkillLevel(61120007)).applyTo(this);
               }
            }

            this.resetKaiserCombo();
            break;
         case 63101005:
            sum = new ArrayList();
            var23 = this.getMap().getAllSecondAtoms().iterator();

            MapleSecondAtom re;
            while(var23.hasNext()) {
               re = (MapleSecondAtom)var23.next();
               if (re.getSourceId() == 63101006 && re.getChr().getId() == this.getId()) {
                  sum.add(re);
               }
            }

            var23 = sum.iterator();

            while(var23.hasNext()) {
               re = (MapleSecondAtom)var23.next();
               this.getMap().removeSecondAtom(this, re.getObjectId());
            }

            this.removeSkillCustomInfo(effect.getSourceId());
            this.removeSkillCustomInfo(effect.getSourceId() + 1);
            break;
         case 65121101:
            if (!overwrite) {
               this.removeSkillCustomInfo(effect.getSourceId());
            }
            break;
         case 80001733:
            if (this.getBattleGroundChr() != null) {
               this.getBattleGroundChr().setAttackSpeed(this.getBattleGroundChr().getAttackSpeed() + 60);
               this.getBattleGroundChr().setTeam(2);
               this.getMap().broadcastMessage(this, BattleGroundPacket.UpdateAvater(this.getBattleGroundChr(), GameConstants.BattleGroundJobType(this.getBattleGroundChr())), false);
               this.getBattleGroundChr().setTeam(1);
               this.getClient().send(BattleGroundPacket.UpdateAvater(this.getBattleGroundChr(), GameConstants.BattleGroundJobType(this.getBattleGroundChr())));
            }
            break;
         case 80003046:
            this.getClient().send(CField.UIPacket.closeUI(1297));
            this.getClient().send(SLFCGPacket.FollowNpctoSkill(false, 9062524, 0));
            this.getClient().send(SLFCGPacket.FollowNpctoSkill(false, 9062525, 0));
            this.getClient().send(SLFCGPacket.FollowNpctoSkill(false, 9062526, 0));
            break;
         case 135001005:
            if (statups.containsKey(SecondaryStat.YetiAngerMode)) {
               this.removeSkillCustomInfo(135001005);
               SkillFactory.getSkill(135001005).getEffect(1).applyTo(this, false);
               if (this.getSkillCustomValue0(135001007) > 2L) {
                  this.setSkillCustomInfo(135001007, 2L, 0L);
                  SkillFactory.getSkill(135001007).getEffect(1).applyTo(this);
               }
            }
            break;
         case 151101006:
            skillid = 1;

            while(true) {
               if (skillid > 6) {
                  break label895;
               }

               this.getMap().broadcastMessage(SkillPacket.RemoveSubObtacle(this, skillid * 10));
               ++skillid;
            }
         case 152111003:
            this.cancelEffect(this.getBuffedEffect(152000009), (List)null, true);
            this.blessMark = 10;
            SkillFactory.getSkill(152000009).getEffect(this.getSkillLevel(152000009)).applyTo(this);
            SkillFactory.getSkill(152101000).getEffect(this.getSkillLevel(152101000)).applyTo(this);
            SkillFactory.getSkill(152101008).getEffect(this.getSkillLevel(152101008)).applyTo(this);
            break;
         case 152120014:
            summon = this.getSummon(152101000);
            if (summon != null) {
               summon.setEnergy(0);
               summon.getCrystalSkills().clear();
               this.getMap().broadcastMessage(CField.SummonPacket.transformSummon(summon, 2));
               this.getMap().broadcastMessage(CField.SummonPacket.ElementalRadiance(summon, 2));
               this.getMap().broadcastMessage(CField.SummonPacket.specialSummon(summon, 3));
            }
            break;
         case 152121005:
            SkillFactory.getSkill(152001003).getEffect(this.getSkillLevel(152001003)).applyTo(this);
            SkillFactory.getSkill(152101008).getEffect(this.getSkillLevel(152101008)).applyTo(this);

            while(true) {
               if (!summons1.hasNext()) {
                  break label895;
               }

               summon = (MapleSummon)summons1.next();
               if (summon.getOwner().getId() == this.getId() && summon.getSkill() == 152121006) {
                  summon.removeSummon(this.getMap(), false);
                  sum2 = this.getSummon(summon.getSkill());
                  if (sum2 != null) {
                     this.removeSummon(summon);
                  }
               }
            }
         case 162101000:
            var7 = this.SaList.iterator();

            while(var7.hasNext()) {
               sa = (SecondAtom)var7.next();
               this.client.send(CField.removeSecondAtom(this.getId(), sa.getObjectId()));
            }

            this.SaList.clear();
            this.removeSkillCustomInfo(9877654);
            break;
         case 162111002:
            var7 = this.getMap().getAllSecondAtomsThread().iterator();

            while(true) {
               if (!var7.hasNext()) {
                  break label895;
               }

               sa = (SecondAtom)var7.next();
               if (sa.getOwnerId() == this.getId() && sa.getSkillId() == effect.getSourceId()) {
                  this.getMap().broadcastMessage(CField.removeSecondAtom(this.getId(), sa.getObjectId()));
                  this.getMap().removeMapObject(sa);
               }
            }
         case 162121044:
            if (this.getBuffedValue(162121043)) {
               this.cancelEffect(this.getBuffedEffect(162121043));
            }

            var7 = this.getMap().getAllMonster().iterator();

            while(true) {
               if (!var7.hasNext()) {
                  break label895;
               }

               monster = (MapleMonster)var7.next();
               if (monster.getBuff(162121043) != null) {
                  monster.cancelSingleStatus(monster.getBuff(162121043));
               }
            }
         case 400001012:
            skillid = 0;
            if (GameConstants.isBowMaster(this.job)) {
               skillid = 3111005;
            } else if (GameConstants.isMarksMan(this.job)) {
               skillid = 3211005;
            } else if (GameConstants.isPathFinder(this.job)) {
               skillid = 3311009;
            }

            if (skillid > 0) {
               SkillFactory.getSkill(skillid).getEffect(this.getSkillLevel(skillid)).applyTo(this);
            }
            break;
         case 400001025:
         case 400001026:
         case 400001027:
         case 400001028:
         case 400001029:
         case 400001030:
            if (!overwrite) {
               SecondaryStatEffect effz = SkillFactory.getSkill(400001024).getEffect(this.getSkillLevel(400001024));
               this.client.send(CField.skillCooldown(effz.getSourceId(), effz.getY() * 1000));
               this.addCooldown(effz.getSourceId(), System.currentTimeMillis(), (long)(effz.getY() * 1000));
            }
            break;
         case 400001045:
            if (this.getBuffedValue(100000276)) {
               this.cancelEffect(this.getBuffedEffect(100000276));
               this.RapidTimeDetect = 10;
               SkillFactory.getSkill(100000276).getEffect(this.getSkillLevel(100000276)).applyTo(this);
            }

            if (this.getBuffedValue(100000277)) {
               this.cancelEffect(this.getBuffedEffect(100000277));
               this.RapidTimeStrength = 10;
               SkillFactory.getSkill(100000277).getEffect(this.getSkillLevel(100000277)).applyTo(this);
            }

            if (this.getBuffedValue(101120109)) {
               this.cancelEffect(this.getBuffedEffect(101120109));
               SkillFactory.getSkill(101120109).getEffect(this.getSkillLevel(101120109)).applyTo(this);
            }
            break;
         case 400011010:
            if (!this.skillisCooling(effect.getSourceId()) && !overwrite) {
               this.client.send(CField.skillCooldown(effect.getSourceId(), SkillFactory.getSkill(GameConstants.getLinkedSkill(effect.getSourceId())).getEffect(this.getSkillLevel(effect.getSourceId())).getZ() * 1000));
               this.addCooldown(effect.getSourceId(), System.currentTimeMillis(), (long)(SkillFactory.getSkill(GameConstants.getLinkedSkill(effect.getSourceId())).getEffect(this.getSkillLevel(effect.getSourceId())).getZ() * 1000));
            }
            break;
         case 400011011:
            if (this.getRhoAias() > 0 && !overwrite) {
               SkillFactory.getSkill(400011011).getEffect(this.getSkillLevel(400011011)).applyTo(this, false);
               this.setRhoAias(0);
            }
            break;
         case 400011012:
            while(true) {
               do {
                  do {
                     if (!summons1.hasNext()) {
                        break label895;
                     }

                     summon = (MapleSummon)summons1.next();
                  } while(summon.getOwner().getId() != this.getId());
               } while(summon.getSkill() != 400011013 && summon.getSkill() != 400011014);

               summon.removeSummon(this.getMap(), false);
               sum2 = this.getSummon(summon.getSkill());
               if (sum2 != null) {
                  this.removeSummon(summon);
               }
            }
         case 400011015:
            this.getClient().getSession().writeAndFlush(CField.rangeAttack(400011025, Arrays.asList(new RangeAttack(400011025, this.getPosition(), 0, 0, 7))));
            break;
         case 400011021:
            int[] array2 = new int[]{1221015, 1221054};
            int[] var21 = array2;
            i = array2.length;
            int var11 = 0;

            while(true) {
               if (var11 >= i) {
                  break label895;
               }

               int skillid2 = var21[var11];
               if (this.getBuffedValue(skillid2)) {
                  this.cancelEffect(this.getBuffedEffect(skillid2));
               }

               ++var11;
            }
         case 400011112:
            if (this.getSkillCustomValue0(400011112) > 0L) {
               this.setSkillCustomInfo(400011129, (long)SkillFactory.getSkill(400011112).getEffect(this.getSkillLevel(400011112)).getU(), 0L);
               SkillFactory.getSkill(400011129).getEffect(this.getSkillLevel(400011112)).applyTo(this, false);
            }
            break;
         case 400021087:
            if (!overwrite) {
               SkillFactory.getSkill(400021088).getEffect(this.getSkillLevel(400021087)).applyTo(this, false);
            }
            break;
         case 400021105:
            if (overwrite) {
               this.removeSkillCustomInfo(400021105);
               this.removeSkillCustomInfo(400021107);
               this.removeSkillCustomInfo(400021108);
               break;
            }
         case 400011123:
            if (!overwrite) {
               var7 = this.getMap().getAllMonster().iterator();

               while(var7.hasNext()) {
                  monster = (MapleMonster)var7.next();
                  size = (int)monster.getCustomValue0(400011121);
                  if (size > 0) {
                     monster.removeCustomInfo(400011121);
                  }

                  if (monster.getCustomValue(400011122) != null) {
                     monster.removeCustomInfo(400011122);
                  }
               }
            }
            break;
         default:
            if (GameConstants.isAfterRemoveSummonSkill(effect.getSourceId()) && !overwrite) {
               sum = new ArrayList();

               while(summons1.hasNext()) {
                  sum2 = (MapleSummon)summons1.next();
                  if (sum2.getOwner().getId() == this.getId() && sum2.getSkill() == effect.getSourceId()) {
                     sum.add(sum2);
                  }
               }

               var23 = sum.iterator();

               while(var23.hasNext()) {
                  su3 = (MapleSummon)var23.next();
                  su3.removeSummon(this.map, false);
               }
            }
         }

         boolean givebuff;
         if (!overwrite) {
            if (statups.containsKey(SecondaryStat.Infinity)) {
               this.infinity = 0;
            }

            if (statups.containsKey(SecondaryStat.TimeFastABuff)) {
               this.RapidTimeDetect = 0;
            }

            if (statups.containsKey(SecondaryStat.TimeFastBBuff)) {
               this.RapidTimeStrength = 0;
            }

            if (statups.containsKey(SecondaryStat.ArcaneAim)) {
               this.arcaneAim = 0;
            }

            if (statups.containsKey(SecondaryStat.StackBuff)) {
               this.stackbuff = 0;
            }

            if (statups.containsKey(SecondaryStat.BlessMark)) {
               this.blessMark = 0;
            }

            if (statups.containsKey(SecondaryStat.RwBarrier) || statups.containsKey(SecondaryStat.PowerTransferGauge)) {
               this.barrier = 0;
            }

            if (statups.containsKey(SecondaryStat.OverloadCount)) {
               this.overloadCount = 0;
            }

            if (statups.containsKey(SecondaryStat.Exceed)) {
               this.exceed = 0;
            }

            if (statups.containsKey(SecondaryStat.GloryWing)) {
               this.canUseMortalWingBeat = false;
            }

            if (effect.getSourceId() == 2311009) {
               this.holyMagicShell = 0;
            }

            if (effect.getSourceId() == 3110012) {
               this.concentration = 0;
            }

            if (effect.getSourceId() == 4221054) {
               this.flip = 0;
            }

            if (statups.containsKey(SecondaryStat.UnityOfPower)) {
               this.unityofPower = 0;
            }

            if (statups.containsKey(SecondaryStat.BlitzShield)) {
               this.blitzShield = 0;
            }

            if (statups.containsKey(SecondaryStat.IgnoreTargetDEF)) {
               this.lightning = 0;
            }

            if (effect.getSourceId() == 15111022) {
               this.cancelEffectFromBuffStat(SecondaryStat.IndieDamR, 15120003);
            }

            if (effect.getSourceId() == 36121007) {
               this.getMap().removeMist(36121007);
            }

            if (effect.getSourceId() == 15120003) {
               this.cancelEffectFromBuffStat(SecondaryStat.IndieDamR, 15111022);
            }

            if (effect.getSourceId() == 51001005 && !overwrite) {
               givebuff = false;
               --this.royalStack;
               if (this.royalStack > 0) {
                  givebuff = true;
                  SkillFactory.getSkill(51001005).getEffect(this.getSkillLevel(51001005)).applyTo(this, false, SkillFactory.getSkill(51001005).getEffect(this.getSkillLevel(51001005)).getX() * 1000);
               }

               if (this.getParty() != null) {
                  var23 = this.getParty().getMembers().iterator();

                  while(var23.hasNext()) {
                     MaplePartyCharacter chr1 = (MaplePartyCharacter)var23.next();
                     MapleCharacter curChar = this.getClient().getChannelServer().getPlayerStorage().getCharacterById(chr1.getId());
                     if (curChar != null && curChar.getBuffedValue(51111008)) {
                        curChar.cancelEffect(curChar.getBuffedEffect(51111008));
                        SkillFactory.getSkill(51111008).getEffect(this.getSkillLevel(51111008)).applyTo(this, curChar);
                     }
                  }
               }

               if (givebuff) {
                  return;
               }
            }

            if (effect.getSourceId() == 51111004 && this.getParty() != null) {
               var7 = this.getParty().getMembers().iterator();

               while(var7.hasNext()) {
                  chr2 = (MaplePartyCharacter)var7.next();
                  curChar2 = this.getClient().getChannelServer().getPlayerStorage().getCharacterById(chr2.getId());
                  if (curChar2 != null && curChar2.getBuffedValue(51111008)) {
                     curChar2.cancelEffectFromBuffStat(SecondaryStat.MichaelSoulLink);
                     SkillFactory.getSkill(51111008).getEffect(this.getSkillLevel(51111008)).applyTo(this, curChar2);
                  }
               }
            }

            if (effect.getSourceId() == 80002544 && !this.getBuffedValue(80002543)) {
               SkillFactory.getSkill(80002543).getEffect(1).applyTo(this, false);
            }

            if (effect.getSourceId() == 14120009) {
               this.siphonVitality = 0;
            }

            if (statups.containsKey(SecondaryStat.AdrenalinBoost)) {
               this.combo = 500;
            }

            if (effect.getSourceId() == 4221016) {
               this.shadowerDebuff = 0;
               this.shadowerDebuffOid = 0;
            }

            if (effect.getSourceId() == 80000268) {
               this.FlowofFight = 0;
            }

            if (effect.getSourceId() == 80000514) {
               this.LinkofArk = 0;
            }

            if (statups.containsKey(SecondaryStat.WeaponVariety)) {
               this.weaponChanges.clear();
            }

            if (statups.containsKey(SecondaryStat.Trinity)) {
               this.trinity = 0;
            }

            if (statups.containsKey(SecondaryStat.EnergyBurst)) {
               effect.applyTo(this, false);
               this.energyBurst = 0;
            }

            if (statups.containsKey(SecondaryStat.AntiMagicShell)) {
               this.antiMagicShell = 0;
            }

            if (statups.containsKey(SecondaryStat.EmpiricalKnowledge)) {
               this.empiricalKnowledge = null;
               this.empiricalStack = 0;
            }

            if (statups.containsKey(SecondaryStat.FightJazz)) {
               this.fightJazz = 0;
            }

            if (statups.containsKey(SecondaryStat.AdelResonance)) {
               this.adelResonance = 0;
            }

            if (statups.containsKey(SecondaryStat.WillofSwordStrike)) {
               this.ignoreDraco = 0;
            }

            if (effect.getSourceId() == 1301006) {
               this.cancelEffectFromBuffStat(SecondaryStat.DarknessAura);
            }

            if (effect.getSourceId() == 400021092 && !overwrite) {
               SkillFactory.getSkill(400021093).getEffect(effect.getLevel()).applyTo(this, false);
               this.removeSkillCustomInfo(400021092);
               this.removeSkillCustomInfo(400021093);
            }

            if (effect.getSourceId() == 400011047) {
               this.removeSkillCustomInfo(400011047);
               this.removeSkillCustomInfo(400011048);
               this.client.getSession().writeAndFlush(CField.rangeAttack(400011047, Arrays.asList(new RangeAttack(400011085, this.getTruePosition(), 0, 1, 1))));
               this.dropMessageGM(6, "전송됨");
            }

            if (statups.containsKey(SecondaryStat.Revenant)) {
               SkillFactory.getSkill(400011129).getEffect(effect.getLevel()).applyTo(this, false);
            }

            if (statups.containsKey(SecondaryStat.RoyalKnights)) {
               effect.applyTo(this, false);
            }

            if (GameConstants.getLinkedSkill(effect.getSourceId()) == 400001024 && this.getCooldownLimit(400001024) == 0L) {
               this.addCooldown(400001024, System.currentTimeMillis(), 240000L);
               this.client.getSession().writeAndFlush(CField.skillCooldown(400001024, 240000));
            }

            if (effect.getSourceId() == 11001030) {
               this.setCosmicCount(0);
            }
         }

         givebuff = false;
         switch(effect.getSourceId()) {
         case 20040216:
         case 20040217:
            givebuff = true;
            break;
         case 101120109:
            if (overwrite && statups.containsKey(SecondaryStat.ImmuneBarrier)) {
               givebuff = true;
            }
            break;
         case 152000009:
         case 152120003:
         case 400021099:
         case 400051058:
            if (overwrite) {
               givebuff = true;
            }
            break;
         case 162120038:
         case 400011127:
            if (overwrite) {
               if (statups.containsKey(SecondaryStat.IndieBarrier)) {
                  givebuff = true;
               }
            } else if (statups.containsKey(SecondaryStat.IndieBarrier)) {
               this.removeSkillCustomInfo(effect.getSourceId());
            }
         }

         if (!givebuff) {
            this.client.getSession().writeAndFlush(CWvsContext.BuffPacket.cancelBuff(statups, this));
            if (this.map != null) {
               this.map.broadcastMessage(this, CWvsContext.BuffPacket.cancelForeignBuff(this, statups), false);
               if (effect.isHide()) {
                  this.map.broadcastMessage(this, CField.spawnPlayerMapobject(this), false);
               }
            }
         }

         if (effect.getSourceId() == 35121013) {
            SkillFactory.getSkill(35121005).getEffect(this.getTotalSkillLevel(35121005)).applyTo(this, false);
         } else if (effect.getSourceId() == 400031017) {
            this.setSkillCustomInfo(23110005, 10L, 0L);
            SkillFactory.getSkill(23110004).getEffect(this.getSkillLevel(23110004)).applyTo(this);
         }

         if (statups.containsKey(SecondaryStat.RideVehicle)) {
            this.equipChanged();
            if (this.getBuffedEffect(80001242) != null) {
               this.cancelEffect(this.getBuffedEffect(80001242));
            }
         }

         if (statups.containsKey(SecondaryStat.SpectorTransForm)) {
            this.client.getSession().writeAndFlush(CWvsContext.enableActions(this));
         }

         if (effect.getSourceId() == 20040219) {
            if (this.getLuminusMorph()) {
               this.cancelEffectFromBuffStat(SecondaryStat.Larkness);
               this.setLuminusMorph(false);
               SkillFactory.getSkill(20040217).getEffect(1).applyTo(this, false);
               this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.LuminusMorph(this.getLuminusMorphUse(), this.getLuminusMorph()));
               this.setUseTruthDoor(false);
            } else {
               this.cancelEffectFromBuffStat(SecondaryStat.Larkness);
               this.setLuminusMorph(true);
               SkillFactory.getSkill(20040216).getEffect(1).applyTo(this, false);
               this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.LuminusMorph(this.getLuminusMorphUse(), this.getLuminusMorph()));
               this.setUseTruthDoor(false);
            }
         } else if (effect.getSourceId() == 20040220) {
            if (this.getLuminusMorph()) {
               this.cancelEffectFromBuffStat(SecondaryStat.Larkness);
               this.setLuminusMorph(false);
               SkillFactory.getSkill(20040217).getEffect(1).applyTo(this, false);
               this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.LuminusMorph(this.getLuminusMorphUse(), this.getLuminusMorph()));
               this.setUseTruthDoor(false);
            } else {
               this.cancelEffectFromBuffStat(SecondaryStat.Larkness);
               this.setLuminusMorph(true);
               SkillFactory.getSkill(20040216).getEffect(1).applyTo(this, false);
               this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.LuminusMorph(this.getLuminusMorphUse(), this.getLuminusMorph()));
               this.setUseTruthDoor(false);
            }
         }

      }
   }

   public void cancelEffectFromBuffStat(SecondaryStat stat) {
      if (this.checkBuffStatValueHolder(stat) != null) {
         this.cancelEffect(this.checkBuffStatValueHolder(stat).effect, Arrays.asList(stat));
      }

   }

   public void cancelEffectFromBuffStat(SecondaryStat stat, int from) {
      if (this.checkBuffStatValueHolder(stat, from) != null && this.checkBuffStatValueHolder(stat, from).effect.getSourceId() == from) {
         this.cancelEffect(this.checkBuffStatValueHolder(stat, from).effect, Arrays.asList(stat));
      }

   }

   public void dispel() {
      if (!this.isHidden()) {
         Iterator var1 = this.effects.iterator();

         while(var1.hasNext()) {
            Pair<SecondaryStat, SecondaryStatValueHolder> data = (Pair)var1.next();
            SecondaryStatValueHolder mbsvh = (SecondaryStatValueHolder)data.right;
            if (mbsvh.effect.isSkill() && !mbsvh.effect.isMorph() && !mbsvh.effect.isMonsterRiding() && !mbsvh.effect.isMechChange()) {
               this.cancelEffect(mbsvh.effect, Arrays.asList((SecondaryStat)data.left));
            }
         }
      }

   }

   public void dispelSkill(int skillid) {
      Iterator var2 = this.effects.iterator();

      while(var2.hasNext()) {
         Pair<SecondaryStat, SecondaryStatValueHolder> mbsvh = (Pair)var2.next();
         if (((SecondaryStatValueHolder)mbsvh.right).effect.isSkill() && ((SecondaryStatValueHolder)mbsvh.right).effect.getSourceId() == skillid) {
            this.cancelEffect(((SecondaryStatValueHolder)mbsvh.right).effect, Arrays.asList((SecondaryStat)mbsvh.left));
            break;
         }
      }

   }

   public void dispelSummons() {
      Iterator var1 = this.effects.iterator();

      while(var1.hasNext()) {
         Pair<SecondaryStat, SecondaryStatValueHolder> mbsvh = (Pair)var1.next();
         if (((SecondaryStatValueHolder)mbsvh.right).effect.getSummonMovementType() != null) {
            boolean cancel = true;
            switch(((SecondaryStatValueHolder)mbsvh.right).effect.getSourceId()) {
            case 32001014:
            case 32100010:
            case 32110017:
            case 32120019:
            case 152101000:
               cancel = false;
            }

            if (cancel) {
               this.cancelEffect(((SecondaryStatValueHolder)mbsvh.right).effect, Arrays.asList((SecondaryStat)mbsvh.left));
            }
         }
      }

   }

   public void cancelAllBuffs_() {
      this.getEffects().clear();
   }

   public void cancelAllBuffs() {
      Iterator var1 = this.effects.iterator();

      while(var1.hasNext()) {
         Pair<SecondaryStat, SecondaryStatValueHolder> data = (Pair)var1.next();
         SecondaryStatValueHolder mbsvh = (SecondaryStatValueHolder)data.right;
         this.cancelEffect(mbsvh.effect, Arrays.asList((SecondaryStat)data.left));
      }

   }

   public void cancelMorphs() {
      Iterator var1 = this.effects.iterator();

      while(var1.hasNext()) {
         Pair<SecondaryStat, SecondaryStatValueHolder> mbsvh = (Pair)var1.next();
         switch(((SecondaryStatValueHolder)mbsvh.right).effect.getSourceId()) {
         case 5111005:
         case 5121003:
         case 13111005:
         case 15111002:
         case 61111008:
         case 61120008:
         case 61121053:
            return;
         }

         if (((SecondaryStatValueHolder)mbsvh.right).effect.isMorph()) {
            this.cancelEffect(((SecondaryStatValueHolder)mbsvh.right).effect, Arrays.asList((SecondaryStat)mbsvh.left));
         }
      }

   }

   public int getMorphState() {
      Iterator var1 = this.effects.iterator();

      Pair mbsvh;
      do {
         if (!var1.hasNext()) {
            return -1;
         }

         mbsvh = (Pair)var1.next();
      } while(!((SecondaryStatValueHolder)mbsvh.right).effect.isMorph());

      return ((SecondaryStatValueHolder)mbsvh.right).effect.getSourceId();
   }

   public void silentGiveBuffs(List<PlayerBuffValueHolder> buffs) {
      if (buffs != null) {
         Iterator var2 = buffs.iterator();

         while(var2.hasNext()) {
            PlayerBuffValueHolder mbsvh = (PlayerBuffValueHolder)var2.next();
            mbsvh.effect.silentApplyBuff(this, mbsvh.startTime, mbsvh.statup, mbsvh.cid);
         }

      }
   }

   public List<PlayerBuffValueHolder> getAllBuffs() {
      List<PlayerBuffValueHolder> ret = new ArrayList();
      Map<Pair<Integer, Byte>, Integer> alreadyDone = new HashMap();
      Iterator var3 = this.effects.iterator();

      while(var3.hasNext()) {
         Pair<SecondaryStat, SecondaryStatValueHolder> mbsvh = (Pair)var3.next();
         Pair<Integer, Byte> key = new Pair(((SecondaryStatValueHolder)mbsvh.getRight()).effect.getSourceId(), ((SecondaryStatValueHolder)mbsvh.getRight()).effect.getLevel());
         if (alreadyDone.containsKey(key)) {
            ((PlayerBuffValueHolder)ret.get((Integer)alreadyDone.get(key))).statup.put((SecondaryStat)mbsvh.getLeft(), new Pair(((SecondaryStatValueHolder)mbsvh.getRight()).value, ((SecondaryStatValueHolder)mbsvh.getRight()).localDuration));
         } else {
            alreadyDone.put(key, ret.size());
            EnumMap<SecondaryStat, Pair<Integer, Integer>> list = new EnumMap(SecondaryStat.class);
            list.put((SecondaryStat)mbsvh.getLeft(), new Pair(((SecondaryStatValueHolder)mbsvh.getRight()).value, ((SecondaryStatValueHolder)mbsvh.right).localDuration));
            ret.add(new PlayerBuffValueHolder(((SecondaryStatValueHolder)mbsvh.getRight()).startTime, ((SecondaryStatValueHolder)mbsvh.getRight()).effect, list, ((SecondaryStatValueHolder)mbsvh.getRight()).localDuration, ((SecondaryStatValueHolder)mbsvh.getRight()).cid));
         }
      }

      return ret;
   }

   public int getSkillLevel(int skillid) {
      return this.getSkillLevel(SkillFactory.getSkill(skillid));
   }

   public int getTotalSkillLevel(int skillid) {
      return this.getTotalSkillLevel(SkillFactory.getSkill(skillid));
   }

   public final void handleOrbgain(AttackInfo attack, int skillid) {
      int 현재콤보어택개수 = this.getBuffedValue(SecondaryStat.ComboCounter);
      Skill combo = SkillFactory.getSkill(1101013);
      Skill advcombo = SkillFactory.getSkill(1120003);
      SecondaryStatEffect ceffect = SkillFactory.getSkill(1101013).getEffect(this.getTotalSkillLevel(1101013));
      this.getTotalSkillLevel(advcombo);
      int 플러스콤보어택 = 1;
      int suc = false;
      int suc;
      if (this.getSkillLevel(1110013) > 0) {
         suc = 80;
      } else {
         suc = 40;
      }

      if (this.getTotalSkillLevel(advcombo) > 0 && Randomizer.isSuccess(this.getSkillLevel(1120044) > 0 ? 100 : 80)) {
         ++플러스콤보어택;
      }

      if (this.getBuffedValue(400011073)) {
         suc /= 2;
      }

      if (Randomizer.isSuccess(suc)) {
         if (skillid == 1120013 || skillid == 400011073 || skillid == 400011074 || skillid == 400011075 || skillid == 400011076) {
            return;
         }

         현재콤보어택개수 += 플러스콤보어택;
         if (현재콤보어택개수 >= 11) {
            현재콤보어택개수 = 11;
         }

         EnumMap<SecondaryStat, Pair<Integer, Integer>> stat = new EnumMap(SecondaryStat.class);
         stat.put(SecondaryStat.ComboCounter, new Pair(현재콤보어택개수, 0));
         this.setBuffedValue(SecondaryStat.ComboCounter, 현재콤보어택개수);
         this.client.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(stat, combo.getEffect(this.getTotalSkillLevel(combo)), this));
         this.map.broadcastMessage(this, CWvsContext.BuffPacket.giveForeignBuff(this, stat, combo.getEffect(this.getTotalSkillLevel(combo))), false);
      }

   }

   public void handleOrbconsume(int skillId) {
      int howmany = 0;
      switch(skillId) {
      case 1111003:
         howmany = 2;
         if (this.getBuffedValue(SecondaryStat.ComboCostInc) != null) {
            howmany += (int)this.getSkillCustomValue0(1111003);
         }
         break;
      case 1111008:
         howmany = 1;
         break;
      case 400011027:
         howmany = 6;
      }

      Skill combo = SkillFactory.getSkill(1101013);
      if (this.getSkillLevel(combo) > 0) {
         SecondaryStatEffect ceffect = this.getBuffedEffect(SecondaryStat.ComboCounter);
         if (ceffect != null) {
            EnumMap<SecondaryStat, Pair<Integer, Integer>> stat = new EnumMap(SecondaryStat.class);
            stat.put(SecondaryStat.ComboCounter, new Pair(Math.max(1, this.getBuffedValue(SecondaryStat.ComboCounter) - howmany), 0));
            this.setBuffedValue(SecondaryStat.ComboCounter, Math.max(1, this.getBuffedValue(SecondaryStat.ComboCounter) - howmany));
            this.client.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(stat, ceffect, this));
            this.map.broadcastMessage(this, CWvsContext.BuffPacket.giveForeignBuff(this, stat, ceffect), false);
         }
      }
   }

   public void silentEnforceMaxHpMp() {
      this.stats.setMp(this.stats.getMp(), this);
      this.stats.setHp(this.stats.getHp(), true, this, false);
   }

   public void enforceMaxHpMp() {
      Map<MapleStat, Long> statups = new EnumMap(MapleStat.class);
      if (this.stats.getMp() > this.stats.getCurrentMaxMp(this)) {
         this.stats.setMp(this.stats.getMp(), this);
         statups.put(MapleStat.MP, this.stats.getMp());
      }

      if (this.stats.getHp() > this.stats.getCurrentMaxHp()) {
         this.stats.setHp(this.stats.getHp(), this);
         statups.put(MapleStat.HP, this.stats.getHp());
      }

      if (statups.size() > 0) {
         this.client.getSession().writeAndFlush(CWvsContext.updatePlayerStats(statups, this));
      }

   }

   public MapleMap getMap() {
      return this.map;
   }

   public void setMap(MapleMap newmap) {
      this.map = newmap;
   }

   public void setMap(int PmapId) {
      this.mapid = PmapId;
   }

   public int getMapId() {
      return this.map != null ? this.map.getId() : this.mapid;
   }

   public byte getInitialSpawnpoint() {
      return this.initialSpawnPoint;
   }

   public int getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public final String getBlessOfFairyOrigin() {
      return this.BlessOfFairy_Origin;
   }

   public final String getBlessOfEmpressOrigin() {
      return this.BlessOfEmpress_Origin;
   }

   public final short getLevel() {
      return this.level;
   }

   public final int getFame() {
      return this.fame;
   }

   public final int getFallCounter() {
      return this.fallcounter;
   }

   public final MapleClient getClient() {
      return this.client;
   }

   public final void setClient(MapleClient client) {
      this.client = client;
   }

   public long getExp() {
      return this.exp;
   }

   public short getRemainingAp() {
      return this.remainingAp;
   }

   public int getRemainingSp() {
      return this.remainingSp[GameConstants.getSkillBook(this.job, 0)];
   }

   public int getRemainingSp(int skillbook) {
      return this.remainingSp[skillbook];
   }

   public int[] getRemainingSps() {
      return this.remainingSp;
   }

   public int getRemainingSpSize() {
      int ret = 0;

      for(int i = 0; i < this.remainingSp.length; ++i) {
         if (this.remainingSp[i] > 0) {
            ++ret;
         }
      }

      return ret;
   }

   public short getHpApUsed() {
      return this.hpApUsed;
   }

   public boolean isHidden() {
      return this.getBuffedValue(9001004);
   }

   public boolean isDominant() {
      return this.dominant;
   }

   public void setDominant(boolean active) {
      this.dominant = active;
   }

   public void setHpApUsed(short hpApUsed) {
      this.hpApUsed = hpApUsed;
   }

   public byte getSkinColor() {
      return this.skinColor;
   }

   public void setSkinColor(byte skinColor) {
      this.skinColor = skinColor;
   }

   public byte getSecondSkinColor() {
      return this.secondSkinColor;
   }

   public void setSecondSkinColor(byte secondSkinColor) {
      this.secondSkinColor = secondSkinColor;
   }

   public short getJob() {
      return this.job;
   }

   public byte getGender() {
      return this.gender;
   }

   public byte getSecondGender() {
      return this.secondgender;
   }

   public int getHair() {
      return this.hair;
   }

   public int getSecondHair() {
      return this.secondhair;
   }

   public int getFace() {
      return this.face;
   }

   public int getSecondFace() {
      return this.secondface;
   }

   public int getDemonMarking() {
      return this.demonMarking;
   }

   public void setDemonMarking(int mark) {
      this.demonMarking = mark;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setExp(long exp) {
      this.exp = exp;
   }

   public void setHair(int hair) {
      this.hair = hair;
   }

   public void setSecondHair(int secondhair) {
      this.secondhair = secondhair;
   }

   public void setFace(int face) {
      this.face = face;
   }

   public void setSecondFace(int secondface) {
      this.secondface = secondface;
   }

   public void setFame(int fame) {
      this.fame = fame;
   }

   public void setFallCounter(int fallcounter) {
      this.fallcounter = fallcounter;
   }

   public Point getOldPosition() {
      return this.old;
   }

   public void setOldPosition(Point x) {
      this.old = x;
   }

   public void setRemainingAp(short remainingAp) {
      this.remainingAp = remainingAp;
   }

   public void setRemainingSp(int remainingSp) {
      this.remainingSp[GameConstants.getSkillBook(this.job, 0)] = remainingSp;
   }

   public void setRemainingSp(int remainingSp, int skillbook) {
      this.remainingSp[skillbook] = remainingSp;
   }

   public void setGender(byte gender) {
      this.gender = gender;
   }

   public void setSecondGender(byte secondgender) {
      this.secondgender = secondgender;
   }

   public void setInvincible(boolean invinc) {
      this.invincible = invinc;
   }

   public boolean isInvincible() {
      return this.invincible;
   }

   public BuddyList getBuddylist() {
      return this.buddylist;
   }

   public void addFame(int famechange) {
      this.fame += famechange;
      this.getTrait(MapleTrait.MapleTraitType.charm).addLocalExp(famechange);
   }

   public void updateFame() {
      this.updateSingleStat(MapleStat.FAME, (long)this.fame);
   }

   public void changeMapBanish(int mapid, String portal, String msg) {
      MapleMap map = this.client.getChannelServer().getMapFactory().getMap(mapid);
      this.changeMap(map, map.getPortal(portal), true);
   }

   public void warp(int Mapid) {
      ChannelServer cserv = this.getClient().getChannelServer();
      MapleMap target = cserv.getMapFactory().getMap(Mapid);
      this.changeMap(target, target.getPortal(0));
   }

   public void warpdelay(int Mapid, int Delay) {
      server.Timer.MapTimer.getInstance().schedule(() -> {
         ChannelServer cserv = this.getClient().getChannelServer();
         MapleMap target = cserv.getMapFactory().getMap(Mapid);
         this.changeMap(target, target.getPortal(0));
      }, (long)(Delay * 1000));
   }

   public void changeMap(int Mapid, Point pos) {
      MapleMap to = this.getClient().getChannelServer().getMapFactory().getMap(Mapid);
      this.changeMapInternal(to, pos, CField.getWarpToMap(to, 129, this), (MaplePortal)null, false);
   }

   public void changeMap(int Mapid, int portalid) {
      MapleMap to = this.getClient().getChannelServer().getMapFactory().getMap(Mapid);
      MaplePortal pto = to.getPortal(portalid);
      this.changeMapInternal(to, pto.getPosition(), CField.getWarpToMap(to, pto.getId(), this), (MaplePortal)null, false);
   }

   public void changeMap(MapleMap to, Point pos) {
      this.changeMapInternal(to, pos, CField.getWarpToMap(to, 129, this), (MaplePortal)null, false);
   }

   public void changeMap(MapleMap to, MaplePortal maplePortal, boolean banish) {
      this.changeMapInternal(to, to.getPortal(0).getPosition(), CField.getWarpToMap(to, 0, this), to.getPortal(0), banish);
   }

   public void changeMap(MapleMap to, MaplePortal pto) {
      this.changeMapInternal(to, pto.getPosition(), CField.getWarpToMap(to, pto.getId(), this), (MaplePortal)null, false);
   }

   public void changeMap(int mapId, MaplePortal pto) {
      MapleMap to = this.getClient().getChannelServer().getMapFactory().getMap(mapId);
      this.changeMapInternal(to, pto.getPosition(), CField.getWarpToMap(to, pto.getId(), this), (MaplePortal)null, false);
   }

   public void changeMapPortal(MapleMap to, MaplePortal pto) {
      this.changeMapInternal(to, pto.getPosition(), CField.getWarpToMap(to, pto.getId(), this), pto, false);
   }

   public void changeMapChannel(MapleMap to, int channel) {
      this.changeMapChannel(to, to.getPortal(0), channel);
   }

   private void changeMapChannel(MapleMap to, MaplePortal pto, int channel) {
      this.changeChannel(channel);
      this.changeMap(to, pto);
   }

   private void changeMapInternal(MapleMap to, Point pos, byte[] warpPacket, MaplePortal pto, boolean banish) {
      if (to != null) {
         int nowmapid = this.map.getId();
         if (this.eventInstance != null) {
            this.eventInstance.changedMap(this, to.getId());
         }

         if ((to.getId() == 800000000 || to.getId() == 740000000 || to.getId() == 500000000 || to.getId() == 270051100) && this.getV("d_map_" + to.getId()) == "0") {
            this.addKV("d_map_" + to.getId(), "1");
         }

         if (to.getId() == 450004550) {
            to.broadcastMessage(MobPacket.BossLucid.setStainedGlassOnOff(true, FieldLucid.STAINED_GLASS));
         }

         if (this.map.getId() == nowmapid) {
            if ((this.getDeathCount() > 0 || this.liveCounts() > 0) && !banish && to.getId() == this.map.getId()) {
               switch(this.map.getId()) {
               case 450004150:
               case 450004250:
               case 450004450:
               case 450004550:
               case 450004750:
               case 450004850:
                  int x = this.map.getId() != 450004150 && this.map.getId() != 450004450 && this.map.getId() != 450004750 ? (Randomizer.nextBoolean() ? 316 : 1027) : 157;
                  int y = this.map.getId() != 450004150 && this.map.getId() != 450004450 && this.map.getId() != 450004750 ? (Randomizer.nextBoolean() ? -855 : -842) : 48;
                  this.getMap().broadcastMessage(CField.Respawn(this.getId(), (int)this.getStat().getHp()));
                  this.client.getSession().writeAndFlush(CField.onUserTeleport(x, y));
                  break;
               case 450008150:
               case 450008250:
               case 450008350:
               case 450008750:
               case 450008850:
               case 450008950:
               case 450010500:
                  this.getTruePosition().x = 0;
                  this.getMap().broadcastMessage(CField.Respawn(this.getId(), (int)this.getStat().getHp()));
                  this.client.getSession().writeAndFlush(CField.onUserTeleport(0, this.getTruePosition().y));
                  break;
               case 450013700:
                  this.giveBlackMageBuff();
               case 450013100:
               case 450013300:
               case 450013500:
                  this.getTruePosition().x = 0;
                  this.getMap().broadcastMessage(CField.Respawn(this.getId(), (int)this.getStat().getHp()));
                  this.client.getSession().writeAndFlush(CField.onUserTeleport(0, this.getTruePosition().y));
                  break;
               default:
                  this.client.getSession().writeAndFlush(warpPacket);
               }
            } else {
               this.client.getSession().writeAndFlush(warpPacket);
            }

            boolean shouldChange = this.client.getChannelServer().getPlayerStorage().getCharacterById(this.getId()) != null;
            boolean shouldState = this.map.getId() == to.getId();
            if (shouldChange && shouldState) {
               to.setCheckStates(false);
            }

            if (shouldChange) {
               this.map.removePlayer(this);
               this.map = to;
               this.setPosition(pos);
               Map<MapleStat, Long> updates = new EnumMap(MapleStat.class);
               if (this.stats.getMp() == 0L) {
                  this.stats.setMp(this.stats.getCurrentMaxMp(this), this);
                  updates.put(MapleStat.MP, this.stats.getMp());
               }

               if (this.stats.getHp() == 0L) {
                  this.stats.setHp(this.stats.getCurrentMaxHp(), this);
                  updates.put(MapleStat.HP, this.stats.getHp());
               }

               if (!updates.isEmpty()) {
                  this.client.getSession().writeAndFlush(CWvsContext.updatePlayerStats(updates, this));
               }

               to.addPlayer(this);
               this.stats.relocHeal(this);
               if (shouldState) {
                  to.setCheckStates(true);
               }

               if (this.getBuffedValue(400051068)) {
                  this.MechCarrier(1000, true);
               }

               if (this.getSkillCustomValue(8910000) != null) {
                  this.client.send(CField.VonVonStopWatch(this.getSkillCustomTime(8910000)));
                  if ((to.getId() >= 105200110 && to.getId() <= 105200119 || to.getId() >= 105200510 && to.getId() <= 105200519) && to.getCustomValue(8910000) != null) {
                     int type = to.getCustomValue0(8910000);
                     this.client.send(CField.environmentChange("Pt0" + type + "gate", 2));
                  }
               }

               if (to.getId() != 272020300 && to.getId() != 272020310 && to.getId() != 272020500 && to.getId() != 272020600 && to.getId() != 272030410) {
                  if (to.getId() == 450008750) {
                     this.getClient().getSession().writeAndFlush(CField.portalTeleport(Randomizer.nextBoolean() ? "ptup" : "ptdown"));
                  }
               } else {
                  this.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8860003), new Point(73, 95));
                  this.getClient().getSession().writeAndFlush(CField.startMapEffect("자신 속의 추악한 내면을 마주한 기분이 어떠신지요?", 5120056, true));
               }

               if (to.getId() == 280030100 || to.getId() == 280030000 || to.getId() == 262031300 || to.getId() == 105200610) {
                  Iterator var14 = this.getMap().getAllMonster().iterator();

                  while(true) {
                     while(var14.hasNext()) {
                        MapleMonster monster = (MapleMonster)var14.next();
                        if ((monster.getId() == 8800002 || monster.getId() == 8800102) && monster.getSpecialtxt() != null) {
                           to.broadcastMessage(MobPacket.setMonsterProPerties(monster.getObjectId(), 1, 0, 0));
                        } else if (monster.getId() == 8870100) {
                           if (monster.getPhase() == 2) {
                              to.broadcastMessage(MobPacket.showMonsterHP(monster.getObjectId(), 30));
                           }
                        } else if ((monster.getId() == 8900002 || monster.getId() == 8900102) && monster.getSpecialtxt().equals(this.getName())) {
                           monster.switchController(this, true);
                           this.getMap().broadcastMessage(MobPacket.ShowPierreEffect(this, monster));
                        }
                     }

                     return;
                  }
               }
            }
         }

      }
   }

   public void cancelChallenge() {
      if (this.challenge != 0 && this.client.getChannelServer() != null) {
         MapleCharacter chr = this.client.getChannelServer().getPlayerStorage().getCharacterById(this.challenge);
         if (chr != null) {
            chr.dropMessage(6, this.getName() + " has denied your request.");
            chr.setChallenge(0);
         }

         this.dropMessage(6, "Denied the challenge.");
         this.challenge = 0;
      }

   }

   public void leaveMap(MapleMap map) {
      Iterator var2 = this.controlled.iterator();

      while(var2.hasNext()) {
         MapleMonster mons = (MapleMonster)var2.next();
         if (mons != null) {
            mons.setController((MapleCharacter)null);
            mons.setControllerHasAggro(false);
            map.updateMonsterController(mons);
         }
      }

      this.controlled.clear();
      this.visibleMapObjects.clear();
      if (this.chair != 0) {
         this.chair = 0;
      }

      this.clearLinkMid();
      this.cancelChallenge();
      if (!this.getMechDoors().isEmpty()) {
         this.removeMechDoor();
      }

      if (this.getTrade() != null) {
         MapleTrade.cancelTrade(this.getTrade(), this.client, this);
      }

   }

   public void changeJob(int newJob) {
      try {
         this.cancelEffectFromBuffStat(SecondaryStat.ShadowPartner);
         this.job = (short)newJob;
         this.updateSingleStat(MapleStat.JOB, (long)newJob);
         if (!GameConstants.isBeginnerJob(newJob)) {
            int[] remainingSp4;
            int skillBook4;
            if (!GameConstants.isEvan(newJob) && !GameConstants.isResist(newJob) && !GameConstants.isMercedes(newJob)) {
               if (GameConstants.isPhantom(this.job)) {
                  if (this.job == 2412) {
                     Skill skil1 = SkillFactory.getSkill(20031209);
                     this.changeSingleSkillLevel(skil1, 0, (byte)0);
                     Skill skil2 = SkillFactory.getSkill(20031210);
                     this.changeSingleSkillLevel(skil2, 1, (byte)skil2.getMaxLevel());
                  }

                  this.client.getSession().writeAndFlush(CField.updateCardStack(false, 0));
               } else {
                  remainingSp4 = this.remainingSp;
                  skillBook4 = GameConstants.getSkillBook(newJob, 0);
                  int var10002 = remainingSp4[skillBook4]++;
                  if (newJob % 10 >= 2) {
                     int[] remainingSp3 = this.remainingSp;
                     int skillBook3 = GameConstants.getSkillBook(newJob, 0);
                     remainingSp3[skillBook3] += 2;
                  }
               }
            } else {
               int changeSp = newJob != 2200 && newJob != 2210 && newJob != 2211 && newJob != 2213 ? 5 : 3;
               if (GameConstants.isResist(this.job) && newJob != 3100 && newJob != 3200 && newJob != 3300 && newJob != 3500) {
                  changeSp = 3;
               }

               int[] remainingSp = this.remainingSp;
               int skillBook = GameConstants.getSkillBook(newJob, 0);
               remainingSp[skillBook] += changeSp;
               this.client.getSession().writeAndFlush(CWvsContext.InfoPacket.getSPMsg((byte)changeSp, (short)newJob));
            }

            if (newJob % 10 >= 1 && this.level >= 60) {
               this.remainingAp = (short)(this.remainingAp + 5);
               this.updateSingleStat(MapleStat.AVAILABLEAP, (long)this.remainingAp);
            }

            if (!this.isGM()) {
               this.resetStatsByJob(true);
               if (!GameConstants.isEvan(newJob) && this.getLevel() > (newJob == 200 ? 8 : 10) && newJob % 100 == 0 && newJob % 1000 / 100 > 0) {
                  remainingSp4 = this.remainingSp;
                  skillBook4 = GameConstants.getSkillBook(newJob, 0);
                  remainingSp4[skillBook4] += 3 * (this.getLevel() - (newJob == 200 ? 8 : 10));
               }
            }

            this.updateSingleStat(MapleStat.AVAILABLESP, 0L);
         }

         if (GameConstants.isDemonAvenger(this.job)) {
            this.changeSkillLevel(30010230, (byte)1, (byte)1);
            this.changeSkillLevel(30010231, (byte)1, (byte)1);
            this.changeSkillLevel(30010232, (byte)1, (byte)1);
            this.changeSkillLevel(30010242, (byte)1, (byte)1);
         }

         if (GameConstants.isDemonSlayer(this.job)) {
            this.changeSkillLevel(30010111, (byte)1, (byte)1);
         }

         Iterator var12 = this.getUnions().getUnions().iterator();

         while(var12.hasNext()) {
            MapleUnion union = (MapleUnion)var12.next();
            if (union.getCharid() == this.id) {
               union.setJob(newJob);
            }
         }

         long maxhp = this.stats.getMaxHp();
         long maxmp = this.stats.getMaxMp();
         if (maxhp >= 500000L) {
            maxhp = 500000L;
         }

         if (maxmp >= 500000L) {
            maxmp = 500000L;
         }

         if (GameConstants.isDemonSlayer(this.job)) {
            maxmp = (long)GameConstants.getMPByJob(this);
         }

         if (this.job == 410) {
            this.changeSingleSkillLevel(SkillFactory.getSkill(4101011), 1, (byte)1);
         }

         if (this.job == 15510) {
            this.changeSingleSkillLevel(SkillFactory.getSkill(155101006), 1, (byte)1);
         }

         this.stats.setInfo(maxhp, maxmp, maxhp, maxmp);
         this.stats.recalcLocalStats(this);
         Map<MapleStat, Long> statup = new EnumMap(MapleStat.class);
         statup.put(MapleStat.MAXHP, this.stats.getCurrentMaxHp());
         statup.put(MapleStat.MAXMP, this.stats.getCurrentMaxMp(this));
         statup.put(MapleStat.HP, this.stats.getCurrentMaxHp());
         statup.put(MapleStat.MP, this.stats.getCurrentMaxMp(this));
         this.client.getSession().writeAndFlush(CWvsContext.updatePlayerStats(statup, this));
         this.map.broadcastMessage(this, CField.EffectPacket.showNormalEffect(this, 14, false), false);
         this.silentPartyUpdate();
         this.guildUpdate();
         this.AutoTeachSkill();
         if (this.dragon != null) {
            this.map.broadcastMessage(CField.removeDragon(this.id));
            this.dragon = null;
         }

         if (newJob >= 2200 && newJob <= 2218) {
            if (this.getBuffedValue(SecondaryStat.RideVehicle) != null) {
               this.cancelEffectFromBuffStat(SecondaryStat.RideVehicle);
            }

            this.makeDragon();
         }
      } catch (Exception var7) {
         FileoutputUtil.outputFileError("Log_Script_Except.rtf", var7);
      }

   }

   public void setSkillBuffTest(int buf) {
      this.bufftest = buf;
   }

   public int getSkillBuffTest() {
      return this.bufftest;
   }

   public void makeDragon() {
      this.dragon = new MapleDragon(this);
      this.map.broadcastMessage(CField.spawnDragon(this.dragon));
   }

   public MapleDragon getDragon() {
      return this.dragon;
   }

   public short getAp() {
      return this.remainingAp;
   }

   public void gainAp(short ap) {
      this.remainingAp += ap;
      this.updateSingleStat(MapleStat.AVAILABLEAP, (long)this.remainingAp);
   }

   public void gainSP(int sp) {
      int[] remainingSp = this.remainingSp;
      int skillBook = GameConstants.getSkillBook(this.job, 0);
      remainingSp[skillBook] += sp;
      this.updateSingleStat(MapleStat.AVAILABLESP, 0L);
      this.client.getSession().writeAndFlush(CWvsContext.InfoPacket.getSPMsg((byte)sp, this.job));
   }

   public void gainSP(int sp, int skillbook) {
      int[] remainingSp = this.remainingSp;
      remainingSp[skillbook] += sp;
      this.updateSingleStat(MapleStat.AVAILABLESP, 0L);
      this.client.getSession().writeAndFlush(CWvsContext.InfoPacket.getSPMsg((byte)sp, (short)0));
   }

   public void resetSP(int sp) {
      for(int i = 0; i < this.remainingSp.length; ++i) {
         this.remainingSp[i] = sp;
      }

      this.updateSingleStat(MapleStat.AVAILABLESP, 0L);
   }

   public void resetAPSP() {
      this.resetSP(0);
      this.gainAp((short)(-this.remainingAp));
   }

   public List<Integer> getProfessions() {
      List<Integer> prof = new ArrayList();

      for(int i = 9200; i <= 9204; ++i) {
         if (this.getProfessionLevel(this.id * 10000) > 0) {
            prof.add(i);
         }
      }

      return prof;
   }

   public int getProfessionLevel(int id) {
      String key = id + "lv";
      if (this.getV(key) == null) {
         this.addKV(key, "0");
      }

      if (Integer.parseInt(this.getV(id + "lv")) >= 13) {
         this.addKV(id + "lv", "12");
      }

      return Integer.parseInt(this.getV(key));
   }

   public int getProfessionExp(int id) {
      String key = id + "EXP";
      if (this.getV(key) == null) {
         this.addKV(key, "0");
      }

      return Integer.parseInt(this.getV(key));
   }

   public boolean addProfessionExp(int id, int expGain) {
      int ret = this.getProfessionLevel(id);
      if (ret <= 0) {
         return false;
      } else {
         int newExp = this.getProfessionExp(id) + expGain;
         String var10001;
         int var10002;
         if (newExp >= GameConstants.getProfessionEXP(ret)) {
            if (this.getV(id + "lv") != null) {
               var10001 = id + "lv";
               var10002 = Integer.parseInt(this.getV(id + "lv"));
               this.addKV(var10001, (var10002 + 1).makeConcatWithConstants<invokedynamic>(var10002 + 1));
               if (Integer.parseInt(this.getV(id + "lv")) >= 13) {
                  this.addKV(id + "lv", "12");
               }
            }

            this.changeProfessionLevelExp(id, Integer.parseInt(this.getV(id + "lv")), newExp - GameConstants.getProfessionEXP(ret));
            int traitGain = (int)Math.pow(2.0D, (double)(ret + 1));
            switch(id) {
            case 92000000:
               ((MapleTrait)this.traits.get(MapleTrait.MapleTraitType.sense)).addExp(traitGain, this);
               break;
            case 92010000:
               ((MapleTrait)this.traits.get(MapleTrait.MapleTraitType.will)).addExp(traitGain, this);
               break;
            case 92020000:
            case 92030000:
            case 92040000:
               ((MapleTrait)this.traits.get(MapleTrait.MapleTraitType.craft)).addExp(traitGain, this);
            }

            return true;
         } else {
            System.out.println("경험치 줌 : " + newExp + " : " + ret);
            if (this.getV(id + "EXP") != null) {
               var10001 = id + "EXP";
               var10002 = Integer.parseInt(this.getV(id + "EXP"));
               this.addKV(var10001, (var10002 + expGain).makeConcatWithConstants<invokedynamic>(var10002 + expGain));
            }

            this.changeProfessionLevelExp(id, ret, newExp);
            return false;
         }
      }
   }

   public void changeProfessionLevelExp(int id, int level, int exp) {
      int total = false;
      int total;
      if (exp >= 65536) {
         int plus = exp - '\uffff';
         total = ((level & '\uffff') << 24) + '\uffff';
         total += plus;
      } else {
         total = ((level & '\uffff') << 24) + (exp & '\uffff');
      }

      this.changeSingleSkillLevel(SkillFactory.getSkill(id), total, (byte)30);
   }

   public void changeSingleSkillLevel(Skill skill, int newLevel, byte newMasterlevel) {
      if (skill != null) {
         this.changeSingleSkillLevel(skill, newLevel, newMasterlevel, SkillFactory.getDefaultSExpiry(skill));
      }
   }

   public void changeSingleSkillLevel(Skill skill, int newLevel, byte newMasterlevel, long expiration) {
      Map<Skill, SkillEntry> list = new HashMap();
      boolean hasRecovery = false;
      boolean recalculate = false;
      if (this.changeSkillData(skill, newLevel, newMasterlevel, expiration)) {
         list.put(skill, new SkillEntry(newLevel, newMasterlevel, expiration));
         if (GameConstants.isRecoveryIncSkill(skill.getId())) {
            hasRecovery = true;
         }

         if (skill.getId() < 80000000) {
            recalculate = true;
         }
      }

      if (!list.isEmpty()) {
         this.client.getSession().writeAndFlush(CWvsContext.updateSkills(list));
         this.reUpdateStat(hasRecovery, recalculate);
      }
   }

   public void changeSkillsLevel(Map<Skill, SkillEntry> ss) {
      if (!ss.isEmpty()) {
         Map<Skill, SkillEntry> list = new HashMap();
         boolean hasRecovery = false;
         boolean recalculate = false;
         Iterator var5 = ss.entrySet().iterator();

         while(true) {
            Entry data;
            do {
               do {
                  if (!var5.hasNext()) {
                     if (list.isEmpty()) {
                        return;
                     }

                     this.client.getSession().writeAndFlush(CWvsContext.updateSkills(list));
                     this.reUpdateStat(hasRecovery, recalculate);
                     return;
                  }

                  data = (Entry)var5.next();
               } while(!this.changeSkillData((Skill)data.getKey(), ((SkillEntry)data.getValue()).skillevel, ((SkillEntry)data.getValue()).masterlevel, ((SkillEntry)data.getValue()).expiration));

               list.put((Skill)data.getKey(), (SkillEntry)data.getValue());
               if (GameConstants.isRecoveryIncSkill(((Skill)data.getKey()).getId())) {
                  hasRecovery = true;
               }
            } while(((Skill)data.getKey()).getId() >= 90000000 && !((Skill)data.getKey()).isVMatrix());

            recalculate = true;
         }
      }
   }

   private void reUpdateStat(boolean hasRecovery, boolean recalculate) {
      this.changed_skills = true;
      if (hasRecovery) {
         this.stats.relocHeal(this);
      }

      if (recalculate) {
         this.stats.recalcLocalStats(this);
      }

   }

   public boolean changeSkillData(Skill skill, int newLevel, byte newMasterlevel, long expiration) {
      if (skill == null) {
         return false;
      } else {
         if (newLevel < newMasterlevel) {
            newMasterlevel = (byte)newLevel;
         }

         if (newLevel == 0 && newMasterlevel == 0) {
            if (!this.skills.containsKey(skill)) {
               return false;
            }

            this.skills.remove(skill);
         } else {
            this.skills.put(skill, new SkillEntry(newLevel, newMasterlevel, expiration));
         }

         return true;
      }
   }

   public void changeSkillLevel(int skill, byte newLevel, byte newMasterLevel) {
      this.changeSkillLevel(SkillFactory.getSkill(skill), newLevel, newMasterLevel);
   }

   public void changeSkillLevel(Skill skill, byte newLevel, byte newMasterlevel) {
      this.changeSkillLevel_Skip(skill, newLevel, newMasterlevel);
   }

   public void changeSkillLevel_Skip(Skill skil, int skilLevel, byte masterLevel) {
      Map<Skill, SkillEntry> enry = new HashMap(1);
      enry.put(skil, new SkillEntry(skilLevel, masterLevel, -1L));
      this.changeSkillLevel_Skip(enry, true);
   }

   public void changeSkillLevel_Skip(Map<Skill, SkillEntry> skill, boolean write) {
      if (!skill.isEmpty()) {
         Map<Skill, SkillEntry> newL = new HashMap();
         Iterator var4 = skill.entrySet().iterator();

         while(true) {
            while(true) {
               Entry z;
               do {
                  if (!var4.hasNext()) {
                     if (write && !newL.isEmpty()) {
                        this.client.getSession().writeAndFlush(CWvsContext.updateSkills(newL));
                        this.getStat().recalcLocalStats(this);
                     }

                     return;
                  }

                  z = (Entry)var4.next();
               } while(z.getKey() == null);

               newL.put((Skill)z.getKey(), (SkillEntry)z.getValue());
               if (((SkillEntry)z.getValue()).skillevel <= 0 && ((SkillEntry)z.getValue()).masterlevel == 0) {
                  if (this.skills.containsKey(z.getKey())) {
                     this.skills.remove(z.getKey());
                  }
               } else {
                  this.skills.put((Skill)z.getKey(), (SkillEntry)z.getValue());
               }
            }
         }
      }
   }

   public void playerDead() {
      if (this.getEventInstance() != null) {
         this.getEventInstance().playerKilled(this);
      }

      this.checkSpecialCoreSkills("die", 0, (SecondaryStatEffect)null);
      this.dispelSummons();
      this.checkFollow();

      while(this.getBuffedValue(SecondaryStat.NotDamaged) != null) {
         this.cancelEffect(this.getBuffedEffect(SecondaryStat.NotDamaged));
      }

      while(this.getBuffedValue(SecondaryStat.IndieNotDamaged) != null) {
         this.cancelEffect(this.getBuffedEffect(SecondaryStat.IndieNotDamaged));
      }

      while(this.getBuffedValue(400011010)) {
         this.getClient().send(CField.skillCooldown(400011010, this.getBuffedEffect(400011010).getZ() * 1000));
         this.addCooldown(400011010, System.currentTimeMillis(), (long)(this.getBuffedEffect(400011010).getZ() * 1000));
         this.cancelEffect(this.getBuffedEffect(400011010));
      }

      while(this.hasDisease(SecondaryStat.CapDebuff)) {
         this.cancelDisease(SecondaryStat.CapDebuff);
      }

      int[] array = new int[]{400001025, 400001026, 400001027, 400001028, 400001029, 400001030};
      int[] var3 = array;
      int i = array.length;

      for(int var5 = 0; var5 < i; ++var5) {
         int FreudsProtection = var3[var5];
         if (this.getBuffedValue(FreudsProtection)) {
            this.cancelEffect(this.getBuffedEffect(FreudsProtection));
         }
      }

      this.dotHP = 0;
      this.lastDOTTime = 0L;
      this.BHGCCount = 0;
      this.HowlingGaleCount = 0;
      this.WildGrenadierCount = 0;
      this.useBuffFreezer = false;
      MapleMonster will = this.map.getMonsterById(8880300);
      if (will == null) {
         will = this.map.getMonsterById(8880340);
         if (will == null) {
            will = this.map.getMonsterById(8880301);
            if (will == null) {
               will = this.map.getMonsterById(8880341);
            }
         }
      }

      if (will != null) {
         if (will.isWillSpecialPattern()) {
            will.setWillSpecialPattern(false);
         }

         if (will.isUseSpecialSkill()) {
            will.setUseSpecialSkill(false);
         }
      }

      if ((this.getMapId() == 450008950 || this.getMapId() == 450008350) && this.getSkillCustomValue0(24209) > 0L) {
         Iterator var9 = this.getMap().getAllChracater().iterator();

         while(var9.hasNext()) {
            MapleCharacter achr = (MapleCharacter)var9.next();
            if (achr.getId() != this.getId()) {
               Map<SecondaryStat, Pair<Integer, Integer>> diseases = new EnumMap(SecondaryStat.class);
               diseases.put(SecondaryStat.WillPoison, new Pair(1, 7000));
               this.giveDebuff((Map)diseases, MobSkillFactory.getMobSkill(242, 9));
               achr.setSkillCustomInfo(24219, (long)Randomizer.rand(1, Integer.MAX_VALUE), 0L);
               achr.setSkillCustomInfo(24209, 1L, 0L);
               achr.setSkillCustomInfo(24220, 0L, 3000L);
               achr.getMap().broadcastMessage(MobPacket.BossWill.posion(achr, (int)achr.getSkillCustomValue0(24219), 0, 0, 0));
               server.Timer.MapTimer.getInstance().schedule(() -> {
                  achr.getMap().broadcastMessage(MobPacket.BossWill.removePoison(achr, (int)achr.getSkillCustomValue0(24219)));
                  achr.removeSkillCustomInfo(24219);
                  achr.removeSkillCustomInfo(24209);
                  achr.removeSkillCustomInfo(24220);
               }, 7000L);
               break;
            }
         }

         this.getMap().broadcastMessage(MobPacket.BossWill.removePoison(this, (int)this.getSkillCustomValue0(24219)));
         this.removeSkillCustomInfo(24219);
         this.removeSkillCustomInfo(24209);
         this.removeSkillCustomInfo(24220);
         i = Randomizer.rand(1, Integer.MAX_VALUE);
         Point pos = this.getPosition();
         this.getMap().broadcastMessage(MobPacket.BossWill.removePoison(this, (int)this.getSkillCustomValue0(24219)));
         this.getMap().broadcastMessage(MobPacket.BossWill.posion(this, i, 1, pos.x, pos.y));
         this.getMap().addWillPoison(pos);
         server.Timer.MapTimer.getInstance().schedule(() -> {
            this.getMap().broadcastMessage(MobPacket.BossWill.removePoison(this, i));
            this.getMap().removeWillPosion(pos);
         }, 7000L);
      }

      if (this.getMapId() == 863010240 || this.getMapId() == 863010330 || this.getMapId() == 863010430 || this.getMapId() == 863010600) {
         MapleCharacter pchr = this.getClient().getChannelServer().getPlayerStorage().getCharacterById(this.getParty().getLeader().getId());
         if (pchr == null) {
            this.dropMessage(5, "파티의 상태가 변경되어 골럭스 원정대가 해체됩니다.");
            this.setKeyValue(200106, "golrux_in", "0");
         } else if (pchr.getKeyValue(200106, "golrux_in") == 1L) {
            if (pchr.getKeyValue(200106, "golrux_dc") <= 0L) {
               pchr.setKeyValue(200106, "golrux_in", "0");
               this.warp(ServerConstants.warpMap);
               if (pchr.getId() != this.getId()) {
                  this.dropMessage(5, "데스카운트가 모두 소모되어 골럭스 원정대가 해체됩니다.");
                  pchr.dropMessage(5, "데스카운트가 모두 소모되어 골럭스 원정대가 해체됩니다.");
               } else {
                  this.dropMessage(5, "데스카운트가 모두 소모되어 골럭스 원정대가 해체됩니다.");
               }
            } else if (pchr.getId() != this.getId()) {
               pchr.setKeyValue(200106, "golrux_dc", String.valueOf(this.getKeyValue(200106, "golrux_dc") - 1L));
               pchr.dropMessage(5, "데스카운트가 " + this.getKeyValue(200106, "golrux_dc") + "만큼 남았습니다.");
               this.dropMessage(5, "데스카운트가 " + this.getKeyValue(200106, "golrux_dc") + "만큼 남았습니다.");
            } else {
               this.setKeyValue(200106, "golrux_dc", String.valueOf(this.getKeyValue(200106, "golrux_dc") - 1L));
               this.dropMessage(5, "데스카운트가 " + this.getKeyValue(200106, "golrux_dc") + "만큼 남았습니다.");
            }
         }
      }

      if (!this.inPVP()) {
         if (this.deathcount > 0) {
            this.setDeathCount((byte)(this.getDeathCount() - 1));
         } else {
            for(i = 4; i >= 0; --i) {
               if (this.deathCounts[i] != 2) {
                  this.deathCounts[i] = 2;
                  this.client.send(CField.JinHillah(3, this, this.getMap()));
                  this.getMap().broadcastMessage(CField.JinHillah(10, this, this.getMap()));
                  break;
               }
            }
         }

         if (this.deadEffect) {
            this.setDeadEffect(false);
            this.client.getSession().writeAndFlush(CField.setPlayerDead());
         }

         this.client.getSession().writeAndFlush(CField.OpenDeadUI(this, 1));
         if (this.eventInstance == null && this.deathcount < 0) {
            this.setKeyValue(210416, "TotalDeadTime", "1800");
            this.setKeyValue(210416, "NowDeadTime", "1800");
            this.setKeyValue(210416, "ExpDrop", "80");
            this.client.send(CField.ExpDropPenalty(true, 1800, 1800, 80, 80));
         }
      }

      if (!this.stats.checkEquipDurabilitys(this, -100)) {
         this.dropMessage(5, "An item has run out of durability but has no inventory room to go to.");
      }

   }

   public void updatePartyMemberHP() {
      if (this.party != null && this.client.getChannelServer() != null) {
         if (this.deathcount > 0) {
            this.getMap().broadcastMessage(CField.showDeathCount(this, this.deathcount));
         }

         int channel = this.client.getChannel();
         Iterator var2 = this.party.getMembers().iterator();

         while(var2.hasNext()) {
            MaplePartyCharacter partychar = (MaplePartyCharacter)var2.next();
            if (partychar != null && partychar.getMapid() == this.getMapId() && partychar.getChannel() == channel) {
               MapleCharacter other = this.client.getChannelServer().getPlayerStorage().getCharacterByName(partychar.getName());
               if (other != null) {
                  other.getClient().getSession().writeAndFlush(CField.updatePartyMemberHP(this.getId(), (int)this.stats.getHp(), (int)this.stats.getCurrentMaxHp()));
               }
            }
         }
      }

   }

   public void receivePartyMemberHP() {
      if (this.party != null) {
         if (this.deathcount > 0) {
            this.getMap().broadcastMessage(CField.showDeathCount(this, this.deathcount));
         }

         int channel = this.client.getChannel();
         Iterator var2 = this.party.getMembers().iterator();

         while(var2.hasNext()) {
            MaplePartyCharacter partychar = (MaplePartyCharacter)var2.next();
            if (partychar != null && partychar.getMapid() == this.getMapId() && partychar.getChannel() == channel) {
               MapleCharacter other = this.client.getChannelServer().getPlayerStorage().getCharacterByName(partychar.getName());
               if (other != null) {
                  this.client.getSession().writeAndFlush(CField.updatePartyMemberHP(other.getId(), (int)other.getStat().getHp(), (int)other.getStat().getCurrentMaxHp()));
               }
            }
         }

      }
   }

   public void healHP(long delta) {
      this.addHP(delta);
      this.client.getSession().writeAndFlush(CField.EffectPacket.showHealEffect(this, (int)delta, true));
      this.getMap().broadcastMessage(this, CField.EffectPacket.showHealEffect(this, (int)delta, false), false);
   }

   public void healMP(long delta) {
      this.addMP(delta);
      this.client.getSession().writeAndFlush(CField.EffectPacket.showHealEffect(this, (int)delta, true));
      this.getMap().broadcastMessage(this, CField.EffectPacket.showHealEffect(this, (int)delta, false), false);
   }

   public void playerIGDead() {
      this.stats.setHp(0L, this, true);
      this.updateSingleStat(MapleStat.HP, this.stats.getHp());
   }

   public void addHP(long delta) {
      this.addHP(delta, false, false);
   }

   public void addHP(long delta, int skillid) {
      if (this.isAlive() && this.getBattleGroundChr() == null) {
         if (delta > 0L && (this.getBuffedValue(SecondaryStat.StopPortion) != null || this.hasDisease(SecondaryStat.StopPortion) || this.getBuffedEffect(SecondaryStat.DebuffIncHp) != null)) {
            delta = 0L;
         }

         if (this.getBuffedEffect(SecondaryStat.NotDamaged) == null && this.getBuffedEffect(SecondaryStat.IndieNotDamaged) == null && skillid == 400011129) {
            this.stats.setHp(this.stats.getHp() + delta <= 0L ? 1L : this.stats.getHp() + delta, this);
            this.updateSingleStat(MapleStat.HP, this.stats.getHp());
         }

      }
   }

   public void addHP(long delta, boolean ign, boolean show) {
      if (this.isAlive() && this.getBattleGroundChr() == null) {
         if (!ign) {
            if (this.getBuffedValue(SecondaryStat.DemonFrenzy) != null && delta > 0L && this.getBuffedEffect(SecondaryStat.DemonFrenzy).getQ2() < this.getStat().getHPPercent()) {
               int w = SkillFactory.getSkill(400011010).getEffect(this.getSkillLevel(400011010)).getW();
               delta = delta / 10L * (long)w;
            }

            if (this.getSkillCustomValue0(143143) == 1L) {
               delta /= 10L;
            }

            if (this.getSkillCustomValue0(143145) == 1L) {
               delta /= 2L;
            }

            if (delta > 0L && (this.getBuffedValue(SecondaryStat.StopPortion) != null || this.hasDisease(SecondaryStat.StopPortion) || this.getBuffedEffect(SecondaryStat.DebuffIncHp) != null)) {
               delta = 0L;
            }

            if (this.getBuffedValue(SecondaryStat.HolyBlood) != null && delta > 0L) {
               delta -= delta / 100L * 99L;
            }
         }

         if (delta < 0L && this.getSkillLevel(101120109) > 0 && this.getGender() == 1) {
            SecondaryStatEffect immuneBarrier = SkillFactory.getSkill(101120109).getEffect(this.getSkillLevel(101120109));
            boolean destory = false;
            if (this.getBuffedValue(SecondaryStat.ImmuneBarrier) != null) {
               long duration = this.getBuffLimit(SecondaryStat.ImmuneBarrier, 101120109);
               if (this.getSkillCustomValue0(101120109) < delta) {
                  delta -= this.getSkillCustomValue0(101120109);
                  destory = true;
               } else {
                  this.setSkillCustomInfo(101120109, this.getSkillCustomValue0(101120109) - delta, 0L);
                  delta = 0L;
               }

               if (destory) {
                  while(this.getBuffedValue(101120109)) {
                     this.cancelEffect(this.getBuffedEffect(101120109));
                  }
               } else {
                  immuneBarrier.applyTo(this, true, (int)duration);
               }
            } else if (Randomizer.isSuccess(immuneBarrier.getX())) {
               this.setSkillCustomInfo(101120109, (long)((int)(this.getStat().getCurrentMaxHp() / 100L * (long)immuneBarrier.getX())), 0L);
               if (this.getSkillCustomValue0(101120109) < delta) {
                  delta -= this.getSkillCustomValue0(101120109);
                  destory = true;
               } else {
                  this.setSkillCustomInfo(101120109, this.getSkillCustomValue0(101120109) - delta, 0L);
                  delta = 0L;
               }

               immuneBarrier.applyTo(this, true);
               if (!destory) {
                  immuneBarrier.applyTo(this, false);
               }
            }
         }

         if (this.getBuffedEffect(SecondaryStat.NotDamaged) == null && this.getBuffedEffect(SecondaryStat.IndieNotDamaged) == null && delta <= 0L) {
            this.stats.setHp(this.stats.getHp() + delta, this);
            this.updateSingleStat(MapleStat.HP, this.stats.getHp());
         } else {
            this.stats.setHp(this.stats.getHp() + delta, this);
            this.updateSingleStat(MapleStat.HP, this.stats.getHp());
         }

         if (show && this.getStat().getHp() != this.getStat().getMaxHp()) {
            this.client.getSession().writeAndFlush(CField.EffectPacket.showHealEffect(this.client.getPlayer(), (int)delta, true));
         }

      }
   }

   public void addMP(long delta) {
      this.addMP(delta, false);
   }

   public void reloadChar() {
      this.getClient().getSession().writeAndFlush(CField.getCharInfo(this));
      this.getMap().removePlayer(this);
      this.getMap().addPlayer(this);
   }

   public void addMP(long delta, boolean ignore) {
      if (this.getBattleGroundChr() == null) {
         if (delta < 0L && GameConstants.isDemonSlayer(this.getJob()) || !GameConstants.isDemonSlayer(this.getJob()) || ignore) {
            if (this.getBuffedValue(SecondaryStat.Overload) != null && delta > 0L) {
               delta = 0L;
            }

            if (this.stats.setMp(this.stats.getMp() + delta, this)) {
               this.updateSingleStat(MapleStat.MP, this.stats.getMp());
            }
         }

      }
   }

   public void addMPHP(long hpDiff, long mpDiff) {
      Map<MapleStat, Long> statups = new EnumMap(MapleStat.class);
      if (this.getBattleGroundChr() == null) {
         if (this.getBuffedValue(SecondaryStat.DemonFrenzy) != null && hpDiff > 0L && this.getBuffedEffect(SecondaryStat.DemonFrenzy).getQ2() < this.getStat().getHPPercent()) {
            int w = SkillFactory.getSkill(400011010).getEffect(this.getSkillLevel(400011010)).getW();
            hpDiff = hpDiff / 10L * (long)w;
         }

         if (this.stats.setHp(this.stats.getHp() + hpDiff, this)) {
            statups.put(MapleStat.HP, this.stats.getHp());
         }

         if (mpDiff < 0L && GameConstants.isDemonSlayer(this.getJob()) || !GameConstants.isDemonSlayer(this.getJob())) {
            if (this.getBuffedValue(SecondaryStat.Overload) != null && mpDiff > 0L) {
               mpDiff = 0L;
            }

            if (this.stats.setMp(this.stats.getMp() + mpDiff, this)) {
               statups.put(MapleStat.MP, this.stats.getMp());
            }
         }

         if (statups.size() > 0) {
            this.client.getSession().writeAndFlush(CWvsContext.updatePlayerStats(statups, this));
         }

      }
   }

   public void updateZeroStats() {
      this.client.getSession().writeAndFlush(CWvsContext.updateZeroSecondStats(this));
   }

   public void updateAngelicStats() {
      this.client.getSession().writeAndFlush(CWvsContext.updateAngelicBusterInfo(this));
   }

   public void updateSingleStat(MapleStat stat, long newval) {
      this.updateSingleStat(stat, newval, false);
   }

   public void updateSingleStat(MapleStat stat, long newval, boolean itemReaction) {
      Map<MapleStat, Long> statup = new EnumMap(MapleStat.class);
      statup.put(stat, newval);
      this.client.getSession().writeAndFlush(CWvsContext.updatePlayerStats(statup, itemReaction, this));
   }

   public void gainExp(long total, boolean show, boolean inChat, boolean white) {
      try {
         long prevexp = this.getExp();
         long needed = this.getNeededExp();
         if (total > 0L) {
            this.stats.checkEquipLevels(this, (long)((int)total));
         }

         if (this.level >= ServerConstants.MaxLevel) {
            this.setExp(0L);
            total = 0L;
         } else {
            boolean leveled = false;
            long tot = this.exp + total;
            if (tot >= needed) {
               this.exp += total;
               leveled = true;

               while(this.exp >= needed && this.level < ServerConstants.MaxLevel) {
                  this.levelUp();
                  needed = this.getNeededExp();
                  if (this.level >= ServerConstants.MaxLevel) {
                     this.setExp(0L);
                  }
               }
            } else {
               this.exp += total;
            }
         }

         if (total != 0L) {
            if (this.exp < 0L) {
               if (total > 0L) {
                  this.setExp(needed);
               } else if (total < 0L) {
                  this.setExp(0L);
               }
            }

            this.updateSingleStat(MapleStat.EXP, this.getExp());
            if (show) {
               this.client.getSession().writeAndFlush(CWvsContext.InfoPacket.GainEXP_Others(total, inChat, white));
            }
         }
      } catch (Exception var13) {
         FileoutputUtil.outputFileError("Log_Script_Except.rtf", var13);
      }

   }

   public void gainExpMonster(long gain, boolean show, boolean white) {
      long total = gain;
      int flag = 0;
      int eventBonusExp = (int)((double)ServerConstants.EventBonusExp / 100.0D * (double)gain);
      int weddingExp = (int)((double)ServerConstants.WeddingExp / 100.0D * (double)gain);
      int partyExp = (int)((double)ServerConstants.PartyExp / 100.0D * (double)gain);
      int itemEquipExp = 0;
      int portionExp;
      int skillExp;
      if (this.getKeyValue(27040, "runnigtime") > 0L) {
         boolean equip = false;
         if (this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-31) != null && MapleItemInformationProvider.getInstance().getName(this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-31).getItemId()).startsWith("정령의")) {
            equip = true;
         }

         if (this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-17) != null && MapleItemInformationProvider.getInstance().getName(this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-17).getItemId()).startsWith("정령의")) {
            equip = true;
         }

         if (equip) {
            long runnigtime = this.getKeyValue(27040, "runnigtime");
            portionExp = (int)runnigtime / 3600;
            if (portionExp >= 2) {
               portionExp = 2;
            }

            skillExp = portionExp == 2 ? 30 : (portionExp == 1 ? 20 : 10);
            itemEquipExp += (int)((double)gain * ((double)skillExp / 100.0D));
         }
      }

      int pcExp = (int)((double)ServerConstants.PcRoomExp / 100.0D * (double)gain);
      int rainbowWeekExp = (int)((double)ServerConstants.RainbowWeekExp / 100.0D * (double)gain);
      int boomupExp = (int)((double)ServerConstants.BoomupExp / 100.0D * (double)gain);
      portionExp = (int)((double)ServerConstants.PortionExp / 100.0D * (double)gain);
      skillExp = 0;
      if (Calendar.getInstance().get(7) == 7 || Calendar.getInstance().get(7) == 1) {
         rainbowWeekExp = (int)(0.15D * (double)gain);
      }

      int buffExp;
      int restExp;
      if (this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-21) != null) {
         buffExp = this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-21).getItemId();
         if (buffExp >= 1143800 && buffExp <= 1143814) {
            restExp = 5 * (buffExp - 1143800 + 1);
            itemEquipExp += (int)((double)gain * ((double)restExp / 100.0D));
         }
      }

      if (this.getSkillLevel(20021110) > 0) {
         skillExp += (int)(gain * (long)SkillFactory.getSkill(20021110).getEffect(this.getSkillLevel(20021110)).getEXPRate() / 100L);
      }

      if (this.getStat().expBuffZero > 0.0D) {
         skillExp += (int)((double)gain * this.getStat().expBuffZero / 100.0D);
      }

      if (this.getStat().expBuffUnion > 0.0D) {
         skillExp += (int)((double)gain * this.getStat().expBuffUnion / 100.0D);
      }

      if (this.getSkillLevel(4120045) > 0) {
         skillExp += (int)(gain * (long)SkillFactory.getSkill(4120045).getEffect(this.getSkillLevel(4120045)).getX() / 100L);
      }

      if (this.getSkillLevel(80001040) > 0) {
         skillExp += (int)(gain * (long)SkillFactory.getSkill(80001040).getEffect(this.getSkillLevel(80001040)).getEXPRate() / 100L);
      }

      if (this.getSkillLevel(91000001) > 0) {
         skillExp += (int)(gain * (long)SkillFactory.getSkill(91000001).getEffect(this.getSkillLevel(91000001)).getEXPRate() / 100L);
      }

      if (this.getSkillLevel(131000016) > 0) {
         skillExp += (int)(gain * (long)SkillFactory.getSkill(131000016).getEffect(this.getSkillLevel(131000016)).getEXPRate() / 100L);
      }

      if (this.getSkillLevel(135000021) > 0) {
         skillExp += (int)(gain * (long)SkillFactory.getSkill(135000021).getEffect(this.getSkillLevel(135000021)).getEXPRate() / 100L);
      }

      if (this.getSkillLevel(80000602) > 0) {
         skillExp += (int)(gain * (long)SkillFactory.getSkill(80000602).getEffect(this.getSkillLevel(80000602)).getEXPRate() / 100L);
      }

      if (this.getSkillLevel(80000420) > 0) {
         skillExp += (int)(gain * (long)SkillFactory.getSkill(80000420).getEffect(this.getSkillLevel(80000420)).getEXPRate() / 100L);
      }

      if (this.getSkillLevel(80003045) > 0) {
         skillExp += (int)((double)gain * SkillFactory.getSkill(80003045).getEffect(this.getSkillLevel(80003045)).getExpRPerM() / 100.0D);
      }

      if (this.getSkillLevel(80000577) > 0) {
         skillExp += (int)(gain * (long)SkillFactory.getSkill(80000577).getEffect(this.getSkillLevel(80000577)).getEXPRate() / 100L);
      }

      if (this.getSkillLevel(80000589) > 0) {
         skillExp += (int)(gain * 20L / 100L);
      }

      int itemExp;
      if (this.getKeyValue(19019, "id") >= 1L) {
         buffExp = (int)this.getKeyValue(19019, "id");
         MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
         if (ii.getItemInformation(buffExp) != null) {
            itemExp = ii.getItemInformation(buffExp).nickSkill;
            if (SkillFactory.getSkill(itemExp) != null) {
               SecondaryStatEffect nickEffect = SkillFactory.getSkill(itemExp).getEffect(1);
               skillExp += (int)(gain * (long)nickEffect.getIndieExp() / 100L);
            }
         }
      }

      buffExp = 0;
      int valueExp;
      if (this.getBuffedEffect(SecondaryStat.DiceRoll) != null) {
         SecondaryStatEffect effect = this.getBuffedEffect(SecondaryStat.DiceRoll);
         int dice;
         if (this.getDice() >= 100) {
            itemExp = this.getDice() / 100;
            valueExp = (this.getDice() - itemExp * 100) / 10;
            dice = this.getDice() - this.getDice() / 10 * 10;
         } else {
            itemExp = 1;
            valueExp = this.getDice() / 10;
            dice = this.getDice() - valueExp * 10;
         }

         int value;
         if (dice != 6 && valueExp != 6 && itemExp != 6) {
            value = 0;
         } else if (dice == 6 && valueExp == 6 && itemExp == 6) {
            value = effect.getEXPRate() + 15;
         } else if ((dice != 6 || valueExp != 6) && (dice != 6 || itemExp != 6) && (itemExp != 6 || valueExp != 6)) {
            value = effect.getEXPRate();
         } else {
            value = effect.getEXPRate() + 10;
         }

         buffExp += (int)(gain * (long)value / 100L);
      }

      Iterator var41 = this.effects.iterator();

      while(var41.hasNext()) {
         Pair<SecondaryStat, SecondaryStatValueHolder> buff = (Pair)var41.next();
         if (buff.left == SecondaryStat.IndieExp) {
            buffExp += (int)(gain * (long)((SecondaryStatValueHolder)buff.right).value / 100L);
         }

         if (buff.left == SecondaryStat.HolySymbol) {
            buffExp += (int)(gain * (long)((SecondaryStatValueHolder)buff.right).value / 100L);
         }
      }

      if (this.hasDonationSkill(2311003)) {
         buffExp += (int)(gain * 1L);
      }

      restExp = (int)((double)ServerConstants.RestExp / 100.0D * (double)gain);
      itemExp = (int)((double)ServerConstants.ItemExp / 100.0D * (double)gain);
      valueExp = (int)((double)ServerConstants.ValueExp / 100.0D * (double)gain);
      long bonusExp = (long)((int)(this.getStat().expBuff / 100.0D * (double)gain));
      int ptym = this.getPlayer().getParty() == null ? 1 : this.getPlayer().getParty().getMembers().size();
      if (ptym == 6) {
         ptym = 5;
      }

      long bloodExp = 0L;
      if (this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-112) != null) {
         if (this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-112).getItemId() == 1114000) {
            bloodExp = (long)((int)((double)gain * 0.1D + (double)gain * 0.05D * (double)(ptym - 1)));
         } else if (this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-112).getItemId() == 1114317) {
            itemEquipExp += (int)((double)gain * 0.1D);
         }
      }

      if (this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-113) != null) {
         if (this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-113).getItemId() == 1114000) {
            bloodExp = (long)((int)((double)gain * 0.1D + (double)gain * 0.05D * (double)(ptym - 1)));
         } else if (this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-113).getItemId() == 1114317) {
            itemEquipExp += (int)((double)gain * 0.1D);
         }
      }

      if (this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-115) != null) {
         if (this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-115).getItemId() == 1114000) {
            bloodExp = (long)((int)((double)gain * 0.1D + (double)gain * 0.05D * (double)(ptym - 1)));
         } else if (this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-115).getItemId() == 1114317) {
            itemEquipExp += (int)((double)gain * 0.1D);
         }
      }

      if (this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-116) != null) {
         if (this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-116).getItemId() == 1114000) {
            bloodExp = (long)((int)((double)gain * 0.1D + (double)gain * 0.05D * (double)(ptym - 1)));
         } else if (this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-112).getItemId() == 1114317) {
            itemEquipExp += (int)((double)gain * 0.1D);
         }
      }

      int iceExp = (int)((double)ServerConstants.IceExp / 100.0D * (double)gain);
      Pair<Integer, Integer> burningExp = null;
      if (this.getMap().getAllNormalMonstersThreadsafe().size() > 0 && !this.getMap().isTown() && !GameConstants.로미오줄리엣(this.getMap().getId()) && !GameConstants.사냥컨텐츠맵(this.getId()) && this.getMap().isSpawnPoint()) {
         burningExp = new Pair((int)(gain * (long)this.getMap().getBurning() / 10L), this.getMap().getBurning() * 10);
      }

      int hpLiskExp = (int)((double)ServerConstants.HpLiskExp / 100.0D * (double)gain);
      int fieldBonusExp = (int)((double)ServerConstants.FieldBonusExp / 100.0D * (double)gain);
      int eventBonusExp2 = (int)((double)ServerConstants.EventBonusExp / 100.0D * (double)gain);
      int fieldBonusExp2 = (int)((double)ServerConstants.FieldBonusExp2 / 100.0D * (double)gain);
      if (eventBonusExp > 0) {
         ++flag;
         total = gain + (long)eventBonusExp;
      }

      if (weddingExp > 0) {
         flag += 16;
         total += (long)weddingExp;
      }

      if (partyExp > 0) {
         flag += 32;
         total += (long)partyExp;
      }

      if (itemEquipExp > 0) {
         flag += 64;
         total += (long)itemEquipExp;
      }

      if (pcExp > 0) {
         flag += 128;
         total += (long)pcExp;
      }

      if (rainbowWeekExp > 0) {
         flag += 256;
         total += (long)rainbowWeekExp;
      }

      if (boomupExp > 0) {
         flag += 512;
         total += (long)boomupExp;
      }

      if (portionExp > 0) {
         flag += 1024;
         total += (long)portionExp;
      }

      if (skillExp > 0) {
         flag += 2048;
         total += (long)skillExp;
      }

      if (buffExp > 0) {
         flag += 4096;
         total += (long)buffExp;
      }

      if (restExp > 0) {
         flag += 8192;
         total += (long)restExp;
      }

      if (itemExp > 0) {
         flag += 16384;
         total += (long)itemExp;
      }

      if (valueExp > 0) {
         flag += 131072;
         total += (long)valueExp;
      }

      if (bonusExp > 0L) {
         flag += 524288;
         total += bonusExp;
      }

      if (bloodExp > 0L) {
         flag += 1048576;
         total += bloodExp;
      }

      if (iceExp > 0) {
         flag += 2097152;
         total += (long)iceExp;
      }

      if (burningExp != null && (Integer)burningExp.left > 0) {
         flag += 4194304;
         total += (long)(Integer)burningExp.left;
      }

      if (hpLiskExp > 0) {
         flag += 8388608;
         total += (long)hpLiskExp;
      }

      if (fieldBonusExp > 0) {
         flag += 16777216;
         total += (long)fieldBonusExp;
      }

      if (eventBonusExp2 > 0) {
         flag += 67108864;
         total += (long)eventBonusExp2;
      }

      if (fieldBonusExp2 > 0) {
         flag += 268435456;
         total += (long)fieldBonusExp2;
      }

      if (gain > 0L && total < gain) {
         total = 2147483647L;
      }

      long arcaneStone = this.getKeyValue(1472, "exp") == -1L ? 0L : this.getKeyValue(1472, "exp");
      if (arcaneStone + total >= 20000000000000L) {
         this.setKeyValue(1472, "exp", "20000000000000");
      } else {
         this.setKeyValue(1472, "exp", (arcaneStone + total).makeConcatWithConstants<invokedynamic>(arcaneStone + total));
      }

      if (total > 0L) {
         this.stats.checkEquipLevels(this, total);
      }

      long needed = this.getNeededExp();
      if (this.level >= ServerConstants.MaxLevel) {
         this.setExp(0L);
         total = 0L;
      } else {
         boolean leveled = false;
         if (this.exp + total >= needed) {
            this.exp += total;

            while(this.exp >= needed) {
               this.levelUp();
               leveled = true;
               if (this.level >= ServerConstants.MaxLevel) {
                  this.setExp(0L);
                  total = 0L;
               }
            }
         } else {
            this.exp += total;
         }
      }

      if (gain != 0L) {
         if (this.exp < 0L) {
            if (gain > 0L) {
               this.setExp(this.getNeededExp());
            } else if (gain < 0L) {
               this.setExp(0L);
            }
         }

         this.updateSingleStat(MapleStat.EXP, this.getExp());
         if (show) {
            this.client.getSession().writeAndFlush(CWvsContext.InfoPacket.GainEXP_Monster(this, gain, white, (long)flag, eventBonusExp, weddingExp, partyExp, itemEquipExp, pcExp, rainbowWeekExp, boomupExp, portionExp, skillExp, buffExp, restExp, itemExp, valueExp, bonusExp, bloodExp, iceExp, burningExp, hpLiskExp, fieldBonusExp, eventBonusExp2, fieldBonusExp2));
         }
      }

   }

   public void forceReAddItem_NoUpdate(Item item, MapleInventoryType type) {
      this.getInventory(type).removeSlot(item.getPosition());
      this.getInventory(type).addFromDB(item);
   }

   public void forceReAddItem(Item item, MapleInventoryType type) {
      this.forceReAddItem_NoUpdate(item, type);
      if (type != MapleInventoryType.UNDEFINED) {
         this.client.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, type, item));
      }

   }

   public void silentPartyUpdate() {
      if (this.party != null) {
         World.Party.updateParty(this.party.getId(), PartyOperation.SILENT_UPDATE, new MaplePartyCharacter(this));
      }

   }

   public boolean isSuperGM() {
      return this.gmLevel >= ServerConstants.PlayerGMRank.SUPERGM.getLevel();
   }

   public boolean isIntern() {
      return this.gmLevel >= ServerConstants.PlayerGMRank.INTERN.getLevel();
   }

   public boolean isGM() {
      return this.gmLevel >= ServerConstants.PlayerGMRank.GM.getLevel();
   }

   public boolean isGMName(String name) {
      return this.gmLevel >= ServerConstants.PlayerGMRank.GM.getLevel() && this.name == name;
   }

   public boolean isAdmin() {
      return this.gmLevel >= ServerConstants.PlayerGMRank.ADMIN.getLevel();
   }

   public int getGMLevel() {
      return this.gmLevel;
   }

   public boolean hasGmLevel(int level) {
      return this.gmLevel >= level;
   }

   public void setGMLevel(byte level) {
      this.gmLevel = level;
   }

   public int getLinkMobCount() {
      return this.LinkMobCount;
   }

   public void setLinkMobCount(int count) {
      this.LinkMobCount = count > 9999 ? 9999 : count;
   }

   public void gainItem(int code, int quantity) {
      if (quantity >= 0) {
         MapleInventoryManipulator.addById(this.client, code, (short)quantity, StringUtil.getAllCurrentTime() + "에 gainItem로 얻은 아이템");
      } else {
         MapleInventoryManipulator.removeById(this.client, GameConstants.getInventoryType(this.id), this.id, -quantity, true, false);
      }

   }

   public void gainSpecialItem(int code, int quantity) {
      if (quantity >= 0) {
         MapleInventoryManipulator.addById(this.client, code, (short)quantity, StringUtil.getAllCurrentTime() + "에 gainSpecialItem로 얻은 아이템", true);
      } else {
         MapleInventoryManipulator.removeById(this.client, GameConstants.getInventoryType(this.id), this.id, -quantity, true, false);
      }

   }

   public final Equip gainItem(int id, short quantity, boolean randomStats, long period, String gm_log) {
      Equip equip = null;
      if (quantity >= 0) {
         MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
         MapleInventoryType type = GameConstants.getInventoryType(id);
         if (!MapleInventoryManipulator.checkSpace(this.client, id, quantity, "")) {
            return (Equip)equip;
         }

         if ((type.equals(MapleInventoryType.EQUIP) || type.equals(MapleInventoryType.CODY)) && !GameConstants.isThrowingStar(id) && !GameConstants.isBullet(id)) {
            Equip item = (Equip)ii.getEquipById(id);
            if (period > 0L) {
               item.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
            }

            String var10001 = StringUtil.getAllCurrentTime();
            item.setGMLog(var10001 + "에 " + gm_log);
            MapleInventoryManipulator.addbyItem(this.client, item);
            return item;
         }

         MapleClient var10000 = this.client;
         String var10006 = StringUtil.getAllCurrentTime();
         MapleInventoryManipulator.addById(var10000, id, quantity, "", (MaplePet)null, period, var10006 + "에 " + gm_log);
      } else {
         MapleInventoryManipulator.removeById(this.client, GameConstants.getInventoryType(id), id, -quantity, true, false);
      }

      this.client.getSession().writeAndFlush(CWvsContext.InfoPacket.getShowItemGain(id, quantity, true));
      return (Equip)equip;
   }

   public final MapleInventory getInventory(MapleInventoryType type) {
      return this.inventory[type.ordinal()];
   }

   public final MapleInventory getInventory(byte type) {
      return this.inventory[MapleInventoryType.getByType(type).ordinal()];
   }

   public final MapleInventory[] getInventorys() {
      return this.inventory;
   }

   public final void expirationTask(boolean pending, boolean firstLoad) {
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      if (pending) {
         Iterator var18;
         if (this.pendingExpiration != null) {
            var18 = this.pendingExpiration.iterator();

            while(var18.hasNext()) {
               Integer z = (Integer)var18.next();
               this.client.getSession().writeAndFlush(CWvsContext.InfoPacket.itemExpired(z));
               if (!firstLoad) {
                  Pair<Integer, String> replace = ii.replaceItemInfo(z);
                  if (replace != null && (Integer)replace.left > 0 && ((String)replace.right).length() > 0) {
                     this.dropMessageGM(5, (String)replace.right);
                  }
               }
            }
         }

         this.pendingExpiration = null;
         if (this.pendingSkills != null) {
            this.client.getSession().writeAndFlush(CWvsContext.updateSkills(this.pendingSkills));
            var18 = this.pendingSkills.keySet().iterator();

            while(var18.hasNext()) {
               Skill z2 = (Skill)var18.next();
               if (SkillFactory.getSkillName(z2.getId()).equals("")) {
                  this.client.getSession().writeAndFlush(CWvsContext.serverNotice(5, this.name, "[" + MapleItemInformationProvider.getInstance().getName(z2.getId()) + "] 스킬이 기간이 다 되어 사라졌습니다."));
               } else {
                  this.client.getSession().writeAndFlush(CWvsContext.serverNotice(5, this.name, "[" + SkillFactory.getSkillName(z2.getId()) + "] 스킬이 기간이 다 되어 사라졌습니다."));
               }
            }
         }

         this.pendingSkills = null;
      } else {
         MapleQuestStatus stat = this.getQuestNoAdd(MapleQuest.getInstance(122700));
         List<Integer> ret = new ArrayList();
         long currenttime = System.currentTimeMillis();
         List<Triple<MapleInventoryType, Item, Boolean>> toberemove = new ArrayList();
         List<Item> tobeunlock = new ArrayList();
         MapleInventoryType[] var10 = MapleInventoryType.values();
         int var11 = var10.length;

         Iterator theNewItem;
         label170:
         for(int var12 = 0; var12 < var11; ++var12) {
            MapleInventoryType inv = var10[var12];
            theNewItem = this.getInventory(inv).iterator();

            while(true) {
               while(true) {
                  if (!theNewItem.hasNext()) {
                     continue label170;
                  }

                  Item item = (Item)theNewItem.next();
                  long expiration = item.getExpiration();
                  if (expiration != -1L && !GameConstants.isPet(item.getItemId()) && currenttime > expiration || firstLoad && ii.isLogoutExpire(item.getItemId())) {
                     if (ItemFlag.LOCK.check(item.getFlag())) {
                        tobeunlock.add(item);
                     } else if (currenttime > expiration) {
                        toberemove.add(new Triple(inv, item, false));
                     }
                  } else if (item.getItemId() == 5000054 && item.getPet() != null && item.getPet().getSecondsLeft() <= 0) {
                     toberemove.add(new Triple(inv, item, false));
                  } else if (item.getPosition() == -59 && (stat == null || stat.getCustomData() == null || Long.parseLong(stat.getCustomData()) < currenttime)) {
                     toberemove.add(new Triple(inv, item, true));
                  }
               }
            }
         }

         Iterator var22 = toberemove.iterator();

         while(var22.hasNext()) {
            Triple<MapleInventoryType, Item, Boolean> itemz = (Triple)var22.next();
            Item item2 = (Item)itemz.getMid();
            this.getInventory((MapleInventoryType)itemz.getLeft()).removeItem(item2.getPosition(), item2.getQuantity(), false);
            if ((Boolean)itemz.getRight() && this.getInventory(GameConstants.getInventoryType(item2.getItemId())).getNextFreeSlot() > -1) {
               item2.setPosition(this.getInventory(GameConstants.getInventoryType(item2.getItemId())).getNextFreeSlot());
               this.getInventory(GameConstants.getInventoryType(item2.getItemId())).addFromDB(item2);
            } else {
               ret.add(item2.getItemId());
            }

            if (!firstLoad) {
               Pair<Integer, String> replace2 = ii.replaceItemInfo(item2.getItemId());
               if (replace2 != null && (Integer)replace2.left > 0) {
                  theNewItem = null;
                  Item theNewItem;
                  if (GameConstants.getInventoryType((Integer)replace2.left) == MapleInventoryType.EQUIP) {
                     theNewItem = ii.getEquipById((Integer)replace2.left);
                     theNewItem.setPosition(item2.getPosition());
                  } else {
                     theNewItem = new Item((Integer)replace2.left, item2.getPosition(), (short)1, 0);
                  }

                  this.getInventory((MapleInventoryType)itemz.getLeft()).addFromDB(theNewItem);
               }
            }
         }

         var22 = tobeunlock.iterator();

         while(var22.hasNext()) {
            Item itemz2 = (Item)var22.next();
            itemz2.setExpiration(-1L);
            itemz2.setFlag(itemz2.getFlag() - ItemFlag.LOCK.getValue());
         }

         this.pendingExpiration = ret;
         Map<Skill, SkillEntry> skilz = new HashMap();
         List<Skill> toberem = new ArrayList();
         Iterator var28 = this.skills.entrySet().iterator();

         while(var28.hasNext()) {
            Entry<Skill, SkillEntry> skil = (Entry)var28.next();
            if (((SkillEntry)skil.getValue()).expiration != -1L && currenttime > ((SkillEntry)skil.getValue()).expiration) {
               toberem.add((Skill)skil.getKey());
            }
         }

         for(var28 = toberem.iterator(); var28.hasNext(); this.changed_skills = true) {
            Skill skil2 = (Skill)var28.next();
            skilz.put(skil2, new SkillEntry(0, (byte)0, -1L));
            this.skills.remove(skil2);
         }

         this.pendingSkills = skilz;
         if (stat != null && stat.getCustomData() != null && Long.parseLong(stat.getCustomData()) < currenttime) {
            this.quests.remove(MapleQuest.getInstance(7830));
            this.quests.remove(MapleQuest.getInstance(122700));
         }

      }
   }

   public MapleShop getShop() {
      return this.shop;
   }

   public void setShop(MapleShop shop) {
      this.shop = shop;
   }

   public long getMeso() {
      return this.meso;
   }

   public final int[] getSavedLocations() {
      return this.savedLocations;
   }

   public int getSavedLocation(SavedLocationType type) {
      return this.savedLocations[type.getValue()];
   }

   public void saveLocation(SavedLocationType type) {
      this.savedLocations[type.getValue()] = this.getMapId();
      this.changed_savedlocations = true;
   }

   public void saveLocation(SavedLocationType type, int mapz) {
      this.savedLocations[type.getValue()] = mapz;
      this.changed_savedlocations = true;
   }

   public void clearSavedLocation(SavedLocationType type) {
      this.savedLocations[type.getValue()] = -1;
      this.changed_savedlocations = true;
   }

   public void gainMeso(long gain, boolean show) {
      this.gainMeso(gain, show, false);
   }

   public void gainMeso(long gain, boolean show, boolean inChat) {
      this.gainMeso(gain, show, inChat, false, false);
   }

   public void gainMeso(long gain, boolean show, boolean inChat, boolean isPet, boolean monster) {
      if (this.meso + gain < 0L) {
         this.client.getSession().writeAndFlush(CWvsContext.enableActions(this));
      } else {
         this.meso += gain;
         int jan = false;
         if (monster && this.getGuild() != null) {
         }

         Map<MapleStat, Long> statup = new EnumMap(MapleStat.class);
         statup.put(MapleStat.MESO, this.meso);
         if (isPet) {
            this.client.getSession().writeAndFlush(CWvsContext.updatePlayerStats(statup, false, false, this, true));
            this.client.getSession().writeAndFlush(CWvsContext.onMesoPickupResult((int)gain));
         } else {
            this.client.getSession().writeAndFlush(CWvsContext.updatePlayerStats(statup, true, false, this, false));
         }

         if (show) {
            this.client.getSession().writeAndFlush(CWvsContext.InfoPacket.showMesoGain(gain, isPet, inChat));
         }

         if (inChat) {
            this.client.getSession().writeAndFlush(CWvsContext.InfoPacket.showMesoGain(gain, inChat, 0));
         }

      }
   }

   public void controlMonster(MapleMonster monster, boolean aggro) {
      if (monster != null) {
         monster.setController(this);
         this.controlled.add(monster);
         if (monster.getOwner() == -1) {
            this.client.getSession().writeAndFlush(MobPacket.controlMonster(monster, false, aggro));
         } else if (monster.getOwner() == this.getId()) {
            this.client.getSession().writeAndFlush(MobPacket.controlMonster(monster, false, aggro));
         }

      }
   }

   public void stopControllingMonster(MapleMonster monster) {
      if (monster != null) {
         if (this.controlled.contains(monster)) {
            this.controlled.remove(monster);
         }

      }
   }

   public void checkMonsterAggro(MapleMonster monster) {
      if (monster != null) {
         if (monster.getController() == this) {
            monster.setControllerHasAggro(true);
         } else {
            monster.switchController(this, true);
         }

      }
   }

   public int getControlledSize() {
      return this.controlled.size();
   }

   public int getAccountID() {
      return this.accountid;
   }

   public void mobKilled(int id, int skillID) {
      Iterator var3 = this.quests.values().iterator();

      while(true) {
         MapleQuestStatus q;
         do {
            do {
               do {
                  do {
                     if (!var3.hasNext()) {
                        var3 = this.client.getQuests().values().iterator();

                        while(true) {
                           do {
                              do {
                                 do {
                                    do {
                                       if (!var3.hasNext()) {
                                          return;
                                       }

                                       q = (MapleQuestStatus)var3.next();
                                    } while(q.getStatus() != 1);
                                 } while(!q.hasMobKills());
                              } while(q.getQuest().getId() >= 100829 && q.getQuest().getId() <= 100861);
                           } while(q.getQuest().getId() >= 49000 && q.getQuest().getId() <= 49018);

                           if (q.mobKilled(id, skillID, this)) {
                              this.client.getSession().writeAndFlush(CWvsContext.InfoPacket.updateQuestMobKills(q));
                              if (q.getQuest().canComplete(this, (Integer)null)) {
                                 this.client.getSession().writeAndFlush(CWvsContext.getShowQuestCompletion(q.getQuest().getId()));
                              }
                           }
                        }
                     }

                     q = (MapleQuestStatus)var3.next();
                  } while(q.getStatus() != 1);
               } while(!q.hasMobKills());
            } while(q.getQuest().getId() >= 100829 && q.getQuest().getId() <= 100861);
         } while(q.getQuest().getId() >= 49000 && q.getQuest().getId() <= 49018);

         if (q.mobKilled(id, skillID, this)) {
            this.client.getSession().writeAndFlush(CWvsContext.InfoPacket.updateQuestMobKills(q));
            if (q.getQuest().canComplete(this, (Integer)null)) {
               this.client.getSession().writeAndFlush(CWvsContext.getShowQuestCompletion(q.getQuest().getId()));
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

   public final List<Pair<Integer, Long>> getCompletedMedals() {
      List<Pair<Integer, Long>> ret = new ArrayList();
      Iterator var2 = this.quests.values().iterator();

      while(var2.hasNext()) {
         MapleQuestStatus q = (MapleQuestStatus)var2.next();
         if (q.getStatus() == 2 && !q.isCustom() && !q.getQuest().isBlocked() && q.getQuest().getMedalItem() > 0 && GameConstants.getInventoryType(q.getQuest().getMedalItem()) == MapleInventoryType.EQUIP) {
            ret.add(new Pair(q.getQuest().getId(), q.getCompletionTime()));
         }
      }

      return ret;
   }

   public Map<Skill, SkillEntry> getSkills() {
      return Collections.unmodifiableMap(this.skills);
   }

   public int getTotalSkillLevel(Skill skill) {
      if (skill == null) {
         return 0;
      } else if (skill.getId() != 5011007 && skill.getId() != 400051038 && skill.getId() != 33000036 && skill.getId() != 80001770 && skill.getId() != 80002887 && skill.getId() != 80001242 && skill.getId() != 80001965 && skill.getId() != 80001966 && skill.getId() != 80001967 && skill.getId() != 155001205) {
         if (GameConstants.isAngelicBlessSkill(skill)) {
            return 1;
         } else if (GameConstants.isSaintSaverSkill(skill.getId())) {
            return 1;
         } else {
            if (skill.getId() == 155101204) {
               skill = SkillFactory.getSkill(155101104);
            }

            if (skill.getId() == 155111202) {
               skill = SkillFactory.getSkill(155111102);
            }

            if (skill.getId() == 11121014) {
               skill = SkillFactory.getSkill(11101031);
            }

            if (skill.getId() == 400011089) {
               skill = SkillFactory.getSkill(400011088);
            }

            if (skill.getId() == 4221019) {
               skill = SkillFactory.getSkill(4211006);
            }

            if (skill.getId() != 2311014 && skill.getId() != 2311015 && skill.getId() != 2321016) {
               SkillEntry ret = (SkillEntry)this.skills.get(skill);
               return ret != null && ret.skillevel > 0 ? Math.min(skill.getTrueMax(), ret.skillevel + (skill.isBeginnerSkill() ? 0 : this.stats.combatOrders + (skill.getMaxLevel() > 10 ? this.stats.incAllskill : 0) + this.stats.getSkillIncrement(skill.getId()))) : 0;
            } else {
               return 10;
            }
         }
      } else {
         return 1;
      }
   }

   public long getSkillExpiry(Skill skill) {
      if (skill == null) {
         return 0L;
      } else {
         SkillEntry ret = (SkillEntry)this.skills.get(skill);
         return ret != null && ret.skillevel > 0 ? ret.expiration : 0L;
      }
   }

   public int getSkillLevel(Skill skill) {
      if (skill == null) {
         return 0;
      } else {
         SkillEntry ret = (SkillEntry)this.skills.get(skill);
         if (ret != null && ret.skillevel > 0) {
            int skilllv = ret.skillevel;
            if (skill.combatOrders()) {
               int up = this.getBuffedValue(400001004) ? 1 : (this.getBuffedValue(1211011) ? 2 : 0);
               if (up > 0) {
                  skilllv += up;
               }
            }

            return skilllv;
         } else {
            return 0;
         }
      }
   }

   public byte getMasterLevel(int skill) {
      return this.getMasterLevel(SkillFactory.getSkill(skill));
   }

   public byte getMasterLevel(Skill skill) {
      SkillEntry ret = (SkillEntry)this.skills.get(skill);
      return ret == null ? 0 : ret.masterlevel;
   }

   public int getStarForce() {
      int force = 0;
      Iterator var2 = this.getInventory(MapleInventoryType.EQUIPPED).iterator();

      while(var2.hasNext()) {
         Item iv = (Item)var2.next();
         if (iv instanceof Equip) {
            force += ((Equip)iv).getEnhance();
         }
      }

      return force;
   }

   public long getSuccessorLevel() {
      return this.getKeyValue(100712, "point");
   }

   public long getAwakenLevel() {
      return this.getKeyValue(501215, "point");
   }

   public void setSuccessorLevel(long levelUp) {
      long level = this.getKeyValue(100712, "point");
      level += levelUp;
      this.setLevel((short)301);
      this.setFame((int)level);
      this.updateSingleStat(MapleStat.FAME, (long)this.getFame());
      this.setKeyValue(100712, "point", level.makeConcatWithConstants<invokedynamic>(level));
      String var10001 = this.name;
      World.Broadcast.broadcastMessage(CField.getGameMessage(6, "[계승알림] " + var10001 + "님이 계승자 레벨 " + this.getSuccessorLevel() + "을 달성하셨습니다."));
   }

   public void setAwakenLevel(long levelUp) {
      this.setSuccessor((int)levelUp);
      this.setKeyValue(501215, "point", levelUp.makeConcatWithConstants<invokedynamic>(levelUp));
      World.Broadcast.broadcastMessage(CField.getGameMessage(30, "[각성알림] " + this.name + "님이 《 " + levelUp + "차 각성 》을 하셨습니다."));
   }

   public void levelUp() {
      this.remainingAp = (short)(this.remainingAp + 5);
      this.stats.recalcLocalStats(this);
      long maxhp = this.stats.getMaxHp();
      long maxmp = this.stats.getMaxMp();
      if (GameConstants.isWarrior(this.job)) {
         if (GameConstants.isDemonAvenger(this.job)) {
            maxhp += (long)Randomizer.rand(110, 120);
         } else if (GameConstants.isZero(this.job)) {
            maxhp += (long)Randomizer.rand(80, 90);
         } else {
            maxhp += (long)Randomizer.rand(55, 60);
            maxmp += (long)Randomizer.rand(8, 10);
         }
      } else if (GameConstants.isMagician(this.job)) {
         if (GameConstants.isBattleMage(this.job)) {
            maxhp += (long)Randomizer.rand(40, 50);
            maxmp += (long)Randomizer.rand(17, 20);
         } else if (GameConstants.isLara(this.job)) {
            maxhp += (long)Randomizer.rand(20, 23);
            maxmp += (long)Randomizer.rand(33, 40);
         } else {
            maxhp += (long)Randomizer.rand(10, 13);
            maxmp += (long)Randomizer.rand(43, 50);
            if (this.getSkillLevel(20040221) > 0) {
               maxhp += 190L;
            }
         }
      } else {
         maxhp += (long)Randomizer.rand(20, 30);
         maxmp += (long)Randomizer.rand(15, 20);
      }

      this.exp -= this.getNeededExp();
      ++this.level;
      if (this.level >= 200) {
         String var10000 = FileoutputUtil.레벨업로그;
         int var10001 = this.getClient().getAccID();
         FileoutputUtil.log(var10000, "[레벨업] 계정번호 : " + var10001 + " | " + this.getName() + "이 " + this.level + "로 레벨업.");
      }

      boolean unionz = false;
      Iterator var6 = this.getUnions().getUnions().iterator();

      while(var6.hasNext()) {
         MapleUnion union = (MapleUnion)var6.next();
         if (union.getCharid() == this.id) {
            union.setLevel(this.level);
            unionz = true;
         }
      }

      if (!unionz) {
         if (this.level >= 60 && !GameConstants.isZero(this.job)) {
            this.getUnions().getUnions().add(new MapleUnion(this.id, this.level, this.job, 0, 0, -1, 0, this.name, this.getStarForce(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
         } else if (this.level >= 130 && GameConstants.isZero(this.job)) {
            this.getUnions().getUnions().add(new MapleUnion(this.id, this.level, this.job, 0, 0, -1, 0, this.name, this.getStarForce(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
         }
      }

      int linkSkill = GameConstants.getMyLinkSkill(this.getJob());
      if (linkSkill > 0 && this.getSkillLevel(linkSkill) != (this.getLevel() >= 120 ? 2 : 1)) {
         this.changeSkillLevel(linkSkill, (byte)(this.getLevel() >= 120 ? 2 : 1), (byte)2);
      }

      if (GameConstants.isYeti(this.job) || GameConstants.isPinkBean(this.job)) {
         this.AutoTeachSkill();
      }

      if (this.level == ServerConstants.MaxLevel) {
         this.exp = 0L;
      }

      if (this.level <= 100 && !GameConstants.isYeti(this.job) && !GameConstants.isPinkBean(this.job)) {
         this.autoJob();
      }

      if (this.level == 140) {
         this.getClient().getSession().writeAndFlush(CWvsContext.updateHyperPresets(this, 0, (byte)0));
         this.setKeyValue(2498, "hyperstats", "0");
      }

      if (GameConstants.isZero(this.job)) {
         if (this.level == 100) {
            this.changeSkillLevel(SkillFactory.getSkill(100000267), (byte)1, (byte)1);
         }

         if (this.level == 110) {
            this.changeSkillLevel(SkillFactory.getSkill(100001261), (byte)1, (byte)1);
         }

         if (this.level == 120) {
            this.changeSkillLevel(SkillFactory.getSkill(100001274), (byte)1, (byte)1);
         }

         if (this.level == 140) {
            this.changeSkillLevel(SkillFactory.getSkill(100001272), (byte)1, (byte)1);
         }

         if (this.level == 160) {
            this.changeSkillLevel(SkillFactory.getSkill(100001283), (byte)1, (byte)1);
         }

         if (this.level == 200) {
            this.changeSkillLevel(SkillFactory.getSkill(100001005), (byte)1, (byte)1);
         }
      }

      if (this.level == 200 && this.isGM()) {
         MatrixHandler.gainMatrix(this);
         MatrixHandler.gainVCoreLevel(this);
      }

      if (this.level > 300) {
         this.setSuccessorLevel(1L);
         if (this.getSuccessorLevel() == 10L) {
            this.level = 300;
            this.setAwakenLevel(1L);
         } else if (this.getSuccessorLevel() == 100L) {
            this.setAwakenLevel(2L);
         } else if (this.getSuccessorLevel() == 200L) {
            this.setAwakenLevel(3L);
         } else if (this.getSuccessorLevel() == 300L) {
            this.setAwakenLevel(4L);
         } else if (this.getSuccessorLevel() == 400L) {
            this.setAwakenLevel(5L);
         } else if (this.getSuccessorLevel() == 500L) {
            this.setAwakenLevel(6L);
         } else if (this.getSuccessorLevel() == 600L) {
            this.setAwakenLevel(7L);
         } else if (this.getSuccessorLevel() == 700L) {
            this.setAwakenLevel(8L);
         } else if (this.getSuccessorLevel() == 800L) {
            this.setAwakenLevel(9L);
         } else if (this.getSuccessorLevel() == 900L) {
            this.setAwakenLevel(10L);
         }
      }

      if (this.level >= 200) {
         if (this.getQuestStatus(1465) != 2) {
            this.forceCompleteQuest(1465);
            if (!GameConstants.isPinkBean(this.job) && !GameConstants.isYeti(this.job)) {
               MatrixHandler.gainMatrix(this);
               MatrixHandler.gainVCoreLevel(this);
               this.client.getSession().writeAndFlush(CField.environmentChange("Effect/5skill.img/screen", 16));
               this.client.getSession().writeAndFlush(CField.playSound("Sound/SoundEff.img/5thJob"));
            }

            if (GameConstants.isZero(this.job)) {
               this.changeSingleSkillLevel(SkillFactory.getSkill(100001005), 1, (byte)1);
            }
         }

         if (this.getQuestStatus(1466) != 2) {
            this.forceCompleteQuest(1466);
         }
      }

      if (this.level >= 250 && this.level % 25 == 0 && !this.isGM()) {
         StringBuilder sb = new StringBuilder("[공지] ");
         Item medal = this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-21);
         if (medal != null) {
            sb.append("<");
            sb.append(MapleItemInformationProvider.getInstance().getName(medal.getItemId()));
            sb.append("> ");
         }

         sb.append(this.getName());
         sb.append("님이 " + this.level + "레벨을 달성했습니다! 모두 축하해 주세요!");
         World.Broadcast.broadcastMessage(CField.getGameMessage(8, sb.toString()));
      }

      maxhp = Math.min(500000L, Math.abs(maxhp));
      maxmp = Math.min(500000L, Math.abs(maxmp));
      int itemid = 0;
      boolean change = false;
      if (this.level == 20 && GameConstants.isDualBlade(this.job)) {
         itemid = 1342000;
         change = true;
      }

      if (this.level == 100 || this.level == 10 || this.level == 30 || this.level == 60) {
         if (!GameConstants.isDemonSlayer(this.job) && !GameConstants.isDemonAvenger(this.job)) {
            if (GameConstants.isKaiser(this.job)) {
               itemid = 1352503;
               change = this.level >= 100;
            } else if (GameConstants.isAngelicBuster(this.job)) {
               itemid = 1352604;
               change = this.level >= 100;
            } else if (GameConstants.isCain(this.job)) {
               itemid = 1354013;
               change = this.level >= 100;
            } else if (GameConstants.isMichael(this.job)) {
               itemid = 1098003;
               change = this.level >= 100;
            } else if (GameConstants.isMercedes(this.job)) {
               itemid = 1352003;
               change = this.level >= 100;
            } else if (GameConstants.isEunWol(this.job)) {
               itemid = 1353103;
               change = this.level >= 100;
            } else if (GameConstants.isPhantom(this.job)) {
               itemid = 1352103;
               change = this.level >= 100;
            } else if (GameConstants.isPathFinder(this.job)) {
               itemid = 1353703;
               change = this.level >= 100;
            } else if (GameConstants.isHoyeong(this.job)) {
               itemid = 1353803;
               change = this.level >= 100;
            } else if (GameConstants.isArk(this.job)) {
               itemid = 1353603;
               change = this.level >= 100;
            } else if (GameConstants.isArk(this.job)) {
               itemid = 1353603;
               change = this.level >= 100;
            } else if (GameConstants.isIllium(this.job)) {
               itemid = 1353503;
               change = this.level >= 100;
            } else if (GameConstants.isBlaster(this.job)) {
               itemid = 1353403;
               change = this.level >= 100;
            } else if (GameConstants.isKadena(this.job)) {
               itemid = 1353303;
               change = this.level >= 100;
            } else if (GameConstants.isLuminous(this.job)) {
               itemid = 1352403;
               change = this.level >= 100;
            } else if (GameConstants.isKinesis(this.job)) {
               itemid = 1353203;
               change = this.level >= 100;
            } else if (GameConstants.isAdel(this.job)) {
               itemid = 1354003;
               change = this.level >= 100;
            } else if (GameConstants.isLara(this.job)) {
               itemid = 1354023;
               change = this.level >= 100;
            } else if (GameConstants.isKhali(this.job)) {
               itemid = 1354033;
               change = this.level >= 100;
            }
         } else {
            itemid = 1099004;
            change = this.level >= 100;
         }

         if (this.level == 10 && itemid != 0) {
            if (GameConstants.isAngelicBuster(this.job)) {
               itemid = 1352600;
            } else if (!GameConstants.isDemonAvenger(this.job) && !GameConstants.isDemonSlayer(this.job)) {
               itemid -= 3;
            } else {
               itemid = 1099000;
            }

            change = true;
         } else if (this.level == 30 && itemid != 0) {
            if (GameConstants.isAngelicBuster(this.job)) {
               itemid = 1352602;
            } else if (!GameConstants.isDemonAvenger(this.job) && !GameConstants.isDemonSlayer(this.job)) {
               itemid -= 3;
               ++itemid;
            } else {
               itemid = 1099002;
            }

            change = true;
         } else if (this.level == 60 && itemid != 0) {
            if (GameConstants.isAngelicBuster(this.job)) {
               itemid = 1352603;
            } else if (!GameConstants.isDemonAvenger(this.job) && !GameConstants.isDemonSlayer(this.job)) {
               itemid -= 3;
               itemid += 2;
            } else {
               itemid = 1099003;
            }

            change = true;
         }
      }

      if (change) {
         MapleInventory equip = this.getInventory(MapleInventoryType.EQUIPPED);
         Item ii = MapleItemInformationProvider.getInstance().getEquipById(itemid);
         ii.setPosition((short)-10);
         Equip equiped = (Equip)this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
         if (equiped != null) {
            this.getInventory(MapleInventoryType.EQUIPPED).removeSlot((short)-10);
         }

         equip.addFromDB(ii);
         this.client.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIPPED, ii));
      }

      if (GameConstants.isDemonSlayer(this.job)) {
         int force = 30;
         switch(this.job) {
         case 3110:
            force = 50;
            break;
         case 3111:
            force = 100;
            break;
         case 3112:
            force = 120;
         }

         maxmp = (long)force;
      } else if (GameConstants.isZero(this.job)) {
         maxmp = 100L;
      }

      Map<MapleStat, Long> statup = new EnumMap(MapleStat.class);
      this.stats.setInfo(maxhp, maxmp, this.getStat().getCurrentMaxHp(), this.getStat().getCurrentMaxMp(this));
      statup.put(MapleStat.MAXHP, maxhp);
      statup.put(MapleStat.MAXMP, maxmp);
      statup.put(MapleStat.HP, this.getStat().getHp());
      statup.put(MapleStat.MP, this.getStat().getMp());
      statup.put(MapleStat.EXP, this.exp);
      statup.put(MapleStat.LEVEL, (long)this.level);
      if (this.level <= 10) {
         PlayerStats stats = this.stats;
         stats.str += this.remainingAp;
         this.remainingAp = 0;
         statup.put(MapleStat.STR, (long)this.stats.getStr());
      }

      statup.put(MapleStat.AVAILABLEAP, (long)this.remainingAp);
      statup.put(MapleStat.AVAILABLESP, (long)this.remainingSp[GameConstants.getSkillBook(this.job, this.level)]);
      this.client.getSession().writeAndFlush(CWvsContext.updatePlayerStats(statup, this));
      this.map.broadcastMessage(this, CField.EffectPacket.showNormalEffect(this, 0, false), false);
      this.silentPartyUpdate();
      this.guildUpdate();
      this.getStat().recalcLocalStats(this);
      this.getStat().heal(this);
   }

   public boolean existPremium() {
      Connection con = null;
      ResultSet rs = null;
      PreparedStatement ps = null;
      boolean ret = false;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM premium WHERE accid = ?");
         ps.setInt(1, this.getAccountID());
         rs = ps.executeQuery();
         ret = rs.next();
         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var22) {
         var22.printStackTrace();
      } finally {
         if (rs != null) {
            try {
               rs.close();
            } catch (Exception var21) {
            }
         }

         if (ps != null) {
            try {
               ps.close();
            } catch (Exception var20) {
            }
         }

         if (con != null) {
            try {
               con.close();
            } catch (Exception var19) {
            }
         }

      }

      return ret;
   }

   public long getRemainPremium() {
      Connection con = null;
      ResultSet rs = null;
      PreparedStatement ps = null;
      long ret = 0L;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM premium WHERE accid = ?");
         ps.setInt(1, this.getAccountID());
         rs = ps.executeQuery();
         if (rs.next()) {
            ret = rs.getLong("period");
         }

         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var23) {
         var23.printStackTrace();
      } finally {
         if (rs != null) {
            try {
               rs.close();
            } catch (Exception var22) {
            }
         }

         if (ps != null) {
            try {
               ps.close();
            } catch (Exception var21) {
            }
         }

         if (con != null) {
            try {
               con.close();
            } catch (Exception var20) {
            }
         }

      }

      return ret;
   }

   public void forMatrix() {
      MatrixHandler.gainMatrix(this);
   }

   public void loadPremium() {
      Connection con = null;
      ResultSet rs = null;
      PreparedStatement ps = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM premium WHERE accid = ?");
         ps.setInt(1, this.getAccountID());
         rs = ps.executeQuery();
         if (rs.next()) {
            this.premium = rs.getString("name");
            this.premiumbuff = rs.getInt("buff");
            this.premiumPeriod = rs.getLong("period");
         }

         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var21) {
         var21.printStackTrace();
      } finally {
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

         if (con != null) {
            try {
               con.close();
            } catch (Exception var18) {
            }
         }

      }

   }

   public void gainPremium(int v3) {
      Date adate = new Date();
      Date bdate = new Date();
      if (this.premiumPeriod != 0L) {
         bdate.setTime(this.premiumPeriod + (long)(v3 * 24 * 60 * 60 * 1000));
         if (adate.getTime() > bdate.getTime()) {
            this.premiumPeriod = adate.getTime() + (long)(v3 * 24 * 60 * 60 * 1000);
            this.premium = "일반";
            this.premiumbuff = 80001535;
         } else {
            this.premiumPeriod = bdate.getTime() + (long)(v3 * 24 * 60 * 60 * 1000);
         }
      }

      Connection con = null;
      PreparedStatement ps = null;

      try {
         if (this.existPremium()) {
            if (this.getRemainPremium() > adate.getTime()) {
               con = DatabaseConnection.getConnection();
               ps = con.prepareStatement("UPDATE premium SET period = ? WHERE accid = ?");
               ps.setLong(1, this.getRemainPremium() + (long)(v3 * 24 * 60 * 60 * 1000));
               ps.setInt(2, this.getAccountID());
               ps.executeUpdate();
               ps.close();
               con.close();
            } else {
               con = DatabaseConnection.getConnection();
               ps = con.prepareStatement("UPDATE premium SET period = ? and `name` = ? and `buff` = ? WHERE accid = ?");
               ps.setLong(1, adate.getTime() + (long)(v3 * 24 * 60 * 60 * 1000));
               ps.setString(2, "일반");
               ps.setInt(3, 80001535);
               ps.setInt(4, this.getAccountID());
               ps.executeUpdate();
               ps.close();
               con.close();
            }
         } else {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("INSERT INTO premium(accid, name, buff, period) VALUES (?, ?, ?, ?)");
            ps.setInt(1, this.getAccountID());
            ps.setString(2, "일반");
            ps.setInt(3, 80001535);
            ps.setLong(4, (long)v3);
            ps.executeUpdate();
            ps.close();
            con.close();
         }
      } catch (SQLException var19) {
         var19.printStackTrace();
      } finally {
         if (ps != null) {
            try {
               ps.close();
            } catch (Exception var18) {
            }
         }

         if (con != null) {
            try {
               con.close();
            } catch (Exception var17) {
            }
         }

      }

   }

   public void setPremium(String v1, int v2, long v3) {
      if (SkillFactory.getSkill(this.premiumbuff) != null) {
         this.changeSingleSkillLevel(SkillFactory.getSkill(this.premiumbuff), 0, (byte)0);
      }

      this.premium = v1;
      this.premiumbuff = v2;
      this.premiumPeriod = v3;
      Connection con = null;
      PreparedStatement ps = null;

      try {
         if (this.existPremium()) {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("UPDATE premium SET name = ?, buff = ?, period = ? WHERE accid = ?");
            ps.setString(1, v1);
            ps.setInt(2, v2);
            ps.setLong(3, v3);
            ps.setInt(4, this.getAccountID());
            ps.executeUpdate();
            ps.close();
            con.close();
         } else {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("INSERT INTO premium(accid, name, buff, period) VALUES (?, ?, ?, ?)");
            ps.setInt(1, this.getAccountID());
            ps.setString(2, this.premium);
            ps.setInt(3, this.premiumbuff);
            ps.setLong(4, this.premiumPeriod);
            ps.executeUpdate();
            ps.close();
            con.close();
         }
      } catch (SQLException var20) {
         var20.printStackTrace();
      } finally {
         if (ps != null) {
            try {
               ps.close();
            } catch (Exception var19) {
            }
         }

         if (con != null) {
            try {
               con.close();
            } catch (Exception var18) {
            }
         }

      }

   }

   public String getPremium() {
      return this.premium;
   }

   public int getPremiumBuff() {
      return this.premiumbuff;
   }

   public Long getPremiumPeriod() {
      return this.premiumPeriod;
   }

   public void autoJob() {
      if (GameConstants.isZero(this.getJob())) {
         this.dropMessage(-1, "[알림] 시간의 초월자 '제로'로 전직하였습니다.");
         if (this.getLevel() >= 160 && this.getJob() == 10111) {
            this.changeJob(10112);
         } else if (this.getLevel() >= 140 && this.getJob() == 10110) {
            this.changeJob(10111);
         } else if (this.getLevel() >= 120 && this.getJob() == 10100) {
            this.changeJob(10110);
         }
      }

      if (this.getAutoJob() != null) {
         String autoJob8;
         byte var3;
         if (this.level == 20) {
            autoJob8 = this.getAutoJob();
            var3 = -1;
            switch(autoJob8.hashCode()) {
            case 51601:
               if (autoJob8.equals("430")) {
                  var3 = 0;
               }
               break;
            case 1539167:
               if (autoJob8.equals("2210")) {
                  var3 = 1;
               }
            }

            switch(var3) {
            case 0:
               this.dropMessage(-1, "[암흑을 기억하는자] 세미듀어러로 전직하였습니다.");
               this.changeJob(430);
               break;
            case 1:
               this.changeJob(2210);
            }
         } else if (this.level == 30) {
            autoJob8 = this.getAutoJob();
            var3 = -1;
            switch(autoJob8.hashCode()) {
            case 48656:
               if (autoJob8.equals("110")) {
                  var3 = 0;
               }
               break;
            case 48687:
               if (autoJob8.equals("120")) {
                  var3 = 1;
               }
               break;
            case 48718:
               if (autoJob8.equals("130")) {
                  var3 = 2;
               }
               break;
            case 49617:
               if (autoJob8.equals("210")) {
                  var3 = 3;
               }
               break;
            case 49648:
               if (autoJob8.equals("220")) {
                  var3 = 4;
               }
               break;
            case 49679:
               if (autoJob8.equals("230")) {
                  var3 = 5;
               }
               break;
            case 50578:
               if (autoJob8.equals("310")) {
                  var3 = 6;
               }
               break;
            case 50609:
               if (autoJob8.equals("320")) {
                  var3 = 7;
               }
               break;
            case 50640:
               if (autoJob8.equals("330")) {
                  var3 = 8;
               }
               break;
            case 51539:
               if (autoJob8.equals("410")) {
                  var3 = 9;
               }
               break;
            case 51570:
               if (autoJob8.equals("420")) {
                  var3 = 10;
               }
               break;
            case 51601:
               if (autoJob8.equals("430")) {
                  var3 = 13;
               }
               break;
            case 52500:
               if (autoJob8.equals("510")) {
                  var3 = 11;
               }
               break;
            case 52531:
               if (autoJob8.equals("520")) {
                  var3 = 12;
               }
               break;
            case 52562:
               if (autoJob8.equals("530")) {
                  var3 = 14;
               }
               break;
            case 1508415:
               if (autoJob8.equals("1110")) {
                  var3 = 15;
               }
               break;
            case 1509376:
               if (autoJob8.equals("1210")) {
                  var3 = 16;
               }
               break;
            case 1510337:
               if (autoJob8.equals("1310")) {
                  var3 = 17;
               }
               break;
            case 1511298:
               if (autoJob8.equals("1410")) {
                  var3 = 18;
               }
               break;
            case 1512259:
               if (autoJob8.equals("1510")) {
                  var3 = 19;
               }
               break;
            case 1538206:
               if (autoJob8.equals("2110")) {
                  var3 = 20;
               }
               break;
            case 1539167:
               if (autoJob8.equals("2210")) {
                  var3 = 21;
               }
               break;
            case 1540128:
               if (autoJob8.equals("2310")) {
                  var3 = 22;
               }
               break;
            case 1541089:
               if (autoJob8.equals("2410")) {
                  var3 = 23;
               }
               break;
            case 1542050:
               if (autoJob8.equals("2510")) {
                  var3 = 24;
               }
               break;
            case 1543972:
               if (autoJob8.equals("2710")) {
                  var3 = 25;
               }
               break;
            case 1567997:
               if (autoJob8.equals("3110")) {
                  var3 = 26;
               }
               break;
            case 1568028:
               if (autoJob8.equals("3120")) {
                  var3 = 27;
               }
               break;
            case 1568958:
               if (autoJob8.equals("3210")) {
                  var3 = 28;
               }
               break;
            case 1569919:
               if (autoJob8.equals("3310")) {
                  var3 = 29;
               }
               break;
            case 1571841:
               if (autoJob8.equals("3510")) {
                  var3 = 30;
               }
               break;
            case 1572802:
               if (autoJob8.equals("3610")) {
                  var3 = 31;
               }
               break;
            case 1573763:
               if (autoJob8.equals("3710")) {
                  var3 = 32;
               }
               break;
            case 1627579:
               if (autoJob8.equals("5110")) {
                  var3 = 33;
               }
               break;
            case 1657370:
               if (autoJob8.equals("6110")) {
                  var3 = 34;
               }
               break;
            case 1659292:
               if (autoJob8.equals("6310")) {
                  var3 = 37;
               }
               break;
            case 1660253:
               if (autoJob8.equals("6410")) {
                  var3 = 38;
               }
               break;
            case 1661214:
               if (autoJob8.equals("6510")) {
                  var3 = 35;
               }
               break;
            case 46851278:
               if (autoJob8.equals("14210")) {
                  var3 = 36;
               }
               break;
            case 46880108:
               if (autoJob8.equals("15110")) {
                  var3 = 39;
               }
               break;
            case 46881069:
               if (autoJob8.equals("15210")) {
                  var3 = 40;
               }
               break;
            case 46882991:
               if (autoJob8.equals("15410")) {
                  var3 = 43;
               }
               break;
            case 46883952:
               if (autoJob8.equals("15510")) {
                  var3 = 41;
               }
               break;
            case 46910860:
               if (autoJob8.equals("16210")) {
                  var3 = 42;
               }
               break;
            case 46912782:
               if (autoJob8.equals("16410")) {
                  var3 = 44;
               }
            }

            switch(var3) {
            case 0:
               this.dropMessage(-1, "[양손검술의 기사] 파이터로 전직하였습니다.");
               this.changeJob(110);
               break;
            case 1:
               this.dropMessage(-1, "[한손검술의 기사] 페이지로 전직하였습니다.");
               this.changeJob(120);
               break;
            case 2:
               this.dropMessage(-1, "[창술의 기사] 스피어맨로 전직하였습니다.");
               this.changeJob(130);
               break;
            case 3:
               this.dropMessage(-1, "[불*독] 위자드로 전직하였습니다.");
               this.changeJob(210);
               break;
            case 4:
               this.dropMessage(-1, "[얼음*번개] 위자드로 전직하였습니다.");
               this.changeJob(220);
               break;
            case 5:
               this.dropMessage(-1, "[힐*버프] 클레릭으로 전직하였습니다.");
               this.changeJob(230);
               break;
            case 6:
               this.dropMessage(-1, "[사격수] 헌터로 전직하였습니다.");
               this.changeJob(310);
               break;
            case 7:
               this.dropMessage(-1, "[명사수] 사수로 전직하였습니다.");
               this.changeJob(320);
               break;
            case 8:
               this.dropMessage(-1, "[저주와 고대의 힘] 패스파인더로 전직하였습니다.");
               this.changeJob(330);
               break;
            case 9:
               this.dropMessage(-1, "[표창 암살 입문기] 어쌔신로 전직하였습니다.");
               this.changeJob(410);
               break;
            case 10:
               this.dropMessage(-1, "[단도 암살 입문기] 시프로 전직하였습니다.");
               this.changeJob(420);
               break;
            case 11:
               this.dropMessage(-1, "[너클 입문기] 인파이터로 전직하였습니다.");
               this.changeJob(510);
               break;
            case 12:
               this.dropMessage(-1, "[건 입문기] 건슬링거로 전직하였습니다.");
               this.changeJob(520);
               break;
            case 13:
               this.dropMessage(-1, "[암흑 속의 과거] 듀어러로 전직하였습니다.");
               this.changeJob(431);
               break;
            case 14:
               this.dropMessage(-1, "[캐논 입문기] 캐논슈터로 전직하였습니다.");
               this.changeJob(530);
               break;
            case 15:
               this.dropMessage(-1, "[시그너스 입문기] 빛의 기사로 전직하였습니다.");
               this.changeJob(1110);
               break;
            case 16:
               this.dropMessage(-1, "[시그너스 입문기] 불의 기사로 전직하였습니다.");
               this.changeJob(1210);
               break;
            case 17:
               this.dropMessage(-1, "[시그너스 입문기] 바람의 기사로 전직하였습니다.");
               this.changeJob(1310);
               break;
            case 18:
               this.dropMessage(-1, "[시그너스 입문기] 어둠의 기사로 전직하였습니다.");
               this.changeJob(1410);
               break;
            case 19:
               this.dropMessage(-1, "[시그너스 입문기] 번개의 기사로 전직하였습니다.");
               this.changeJob(1510);
               break;
            case 20:
               this.dropMessage(-1, "[영웅의 본능] 아란으로 전직하였습니다.");
               this.changeJob(2110);
               break;
            case 21:
               this.dropMessage(-1, "[세번째 걸음] 에반으로 전직하였습니다.");
               this.changeJob(2211);
               break;
            case 22:
               this.dropMessage(-1, "[영웅의 본능] 메르세데스로 전직하였습니다.");
               this.changeJob(2310);
               break;
            case 23:
               this.dropMessage(-1, "[영웅의 본능] 팬텀으로 전직하였습니다.");
               this.changeJob(2410);
               break;
            case 24:
               this.dropMessage(-1, "[영웅의 본능] 은월으로 전직하였습니다.");
               this.changeJob(2510);
               break;
            case 25:
               this.dropMessage(-1, "[영웅의 본능] 루미너스로 전직하였습니다.");
               this.changeJob(2710);
               break;
            case 26:
               this.dropMessage(-1, "[레지스탕스 입문기] 데몬슬레이어로 전직하였습니다.");
               this.changeJob(3110);
               break;
            case 27:
               this.dropMessage(-1, "[레지스탕스 입문기] 데몬어벤져로 전직하였습니다.");
               this.changeJob(3120);
               break;
            case 28:
               this.dropMessage(-1, "[레지스탕스 입문기] 배틀메이지로 전직하였습니다.");
               this.changeJob(3210);
               break;
            case 29:
               this.dropMessage(-1, "[레지스탕스 입문기] 와일드헌터로 전직하였습니다.");
               this.changeJob(3310);
               break;
            case 30:
               this.dropMessage(-1, "[레지스탕스 입문기] 메카닉으로 전직하였습니다.");
               this.changeJob(3510);
               break;
            case 31:
               this.dropMessage(-1, "[레지스탕스 입문기] 제논으로 전직하였습니다.");
               this.changeJob(3610);
               break;
            case 32:
               this.dropMessage(-1, "[레지스탕스 입문기] 블래스터로 전직하였습니다.");
               this.changeJob(3710);
               break;
            case 33:
               this.dropMessage(-1, "[시그너스 단장] 빛의 기사로 전직하였습니다.");
               this.changeJob(5110);
               break;
            case 34:
               this.dropMessage(-1, "[노바 수련생] 카이저로 전직하였습니다.");
               this.changeJob(6110);
               break;
            case 35:
               this.dropMessage(-1, "[노바 수련생] 엔젤릭버스터로 전직하였습니다.");
               this.changeJob(6510);
               break;
            case 36:
               this.dropMessage(-1, "[염동력 황제] 키네시스로 전직하였습니다.");
               this.changeJob(14210);
               break;
            case 37:
               this.dropMessage(-1, "[밤의 추적자] 카인으로 전직하였습니다.");
               this.changeJob(6310);
               break;
            case 38:
               this.dropMessage(-1, "[노바의 귀재] 카데나로 전직하였습니다.");
               this.changeJob(6410);
               break;
            case 39:
               this.dropMessage(-1, "[검의 지휘자] 아델로 전직하였습니다.");
               this.changeJob(15110);
               break;
            case 40:
               this.dropMessage(-1, "[고대의 크리스탈] 일리움으로 전직하였습니다.");
               this.changeJob(15210);
               break;
            case 41:
               this.dropMessage(-1, "[심연의 분노] 아크로 전직하였습니다.");
               this.changeJob(15510);
               break;
            case 42:
               this.dropMessage(-1, "[낭만 풍수사] 라라로 전직하였습니다.");
               this.changeJob(16210);
               break;
            case 43:
               this.dropMessage(-1, "[복수의 바람] 칼리로 전직하였습니다.");
               this.changeJob(15410);
               break;
            case 44:
               this.dropMessage(-1, "[천방지축 도사] 호영으로 전직하였습니다.");
               this.changeJob(16410);
            }
         } else if (this.level == 40) {
            autoJob8 = this.getAutoJob();
            var3 = -1;
            switch(autoJob8.hashCode()) {
            case 1539167:
               if (autoJob8.equals("2210")) {
                  var3 = 0;
               }
            default:
               switch(var3) {
               case 0:
                  this.changeJob(2212);
               }
            }
         } else if (this.level == 50) {
            autoJob8 = this.getAutoJob();
            var3 = -1;
            switch(autoJob8.hashCode()) {
            case 1539167:
               if (autoJob8.equals("2210")) {
                  var3 = 0;
               }
            default:
               switch(var3) {
               case 0:
                  this.changeJob(2213);
               }
            }
         } else if (this.level == 55) {
            autoJob8 = this.getAutoJob();
            var3 = -1;
            switch(autoJob8.hashCode()) {
            case 51601:
               if (autoJob8.equals("430")) {
                  var3 = 0;
               }
            default:
               switch(var3) {
               case 0:
                  this.changeJob(432);
               }
            }
         } else if (this.level == 60) {
            autoJob8 = this.getAutoJob();
            var3 = -1;
            switch(autoJob8.hashCode()) {
            case 48656:
               if (autoJob8.equals("110")) {
                  var3 = 0;
               }
               break;
            case 48687:
               if (autoJob8.equals("120")) {
                  var3 = 1;
               }
               break;
            case 48718:
               if (autoJob8.equals("130")) {
                  var3 = 2;
               }
               break;
            case 49617:
               if (autoJob8.equals("210")) {
                  var3 = 3;
               }
               break;
            case 49648:
               if (autoJob8.equals("220")) {
                  var3 = 4;
               }
               break;
            case 49679:
               if (autoJob8.equals("230")) {
                  var3 = 5;
               }
               break;
            case 50578:
               if (autoJob8.equals("310")) {
                  var3 = 6;
               }
               break;
            case 50609:
               if (autoJob8.equals("320")) {
                  var3 = 7;
               }
               break;
            case 50640:
               if (autoJob8.equals("330")) {
                  var3 = 8;
               }
               break;
            case 51539:
               if (autoJob8.equals("410")) {
                  var3 = 9;
               }
               break;
            case 51570:
               if (autoJob8.equals("420")) {
                  var3 = 10;
               }
               break;
            case 51601:
               if (autoJob8.equals("430")) {
                  var3 = 13;
               }
               break;
            case 52500:
               if (autoJob8.equals("510")) {
                  var3 = 11;
               }
               break;
            case 52531:
               if (autoJob8.equals("520")) {
                  var3 = 12;
               }
               break;
            case 52562:
               if (autoJob8.equals("530")) {
                  var3 = 14;
               }
               break;
            case 1508415:
               if (autoJob8.equals("1110")) {
                  var3 = 32;
               }
               break;
            case 1509376:
               if (autoJob8.equals("1210")) {
                  var3 = 33;
               }
               break;
            case 1510337:
               if (autoJob8.equals("1310")) {
                  var3 = 34;
               }
               break;
            case 1511298:
               if (autoJob8.equals("1410")) {
                  var3 = 35;
               }
               break;
            case 1512259:
               if (autoJob8.equals("1510")) {
                  var3 = 36;
               }
               break;
            case 1538206:
               if (autoJob8.equals("2110")) {
                  var3 = 15;
               }
               break;
            case 1539167:
               if (autoJob8.equals("2210")) {
                  var3 = 16;
               }
               break;
            case 1540128:
               if (autoJob8.equals("2310")) {
                  var3 = 17;
               }
               break;
            case 1541089:
               if (autoJob8.equals("2410")) {
                  var3 = 18;
               }
               break;
            case 1542050:
               if (autoJob8.equals("2510")) {
                  var3 = 19;
               }
               break;
            case 1543972:
               if (autoJob8.equals("2710")) {
                  var3 = 20;
               }
               break;
            case 1567997:
               if (autoJob8.equals("3110")) {
                  var3 = 21;
               }
               break;
            case 1568028:
               if (autoJob8.equals("3120")) {
                  var3 = 22;
               }
               break;
            case 1568958:
               if (autoJob8.equals("3210")) {
                  var3 = 23;
               }
               break;
            case 1569919:
               if (autoJob8.equals("3310")) {
                  var3 = 24;
               }
               break;
            case 1571841:
               if (autoJob8.equals("3510")) {
                  var3 = 25;
               }
               break;
            case 1572802:
               if (autoJob8.equals("3610")) {
                  var3 = 26;
               }
               break;
            case 1573763:
               if (autoJob8.equals("3710")) {
                  var3 = 27;
               }
               break;
            case 1627579:
               if (autoJob8.equals("5110")) {
                  var3 = 28;
               }
               break;
            case 1657370:
               if (autoJob8.equals("6110")) {
                  var3 = 29;
               }
               break;
            case 1659292:
               if (autoJob8.equals("6310")) {
                  var3 = 30;
               }
               break;
            case 1660253:
               if (autoJob8.equals("6410")) {
                  var3 = 38;
               }
               break;
            case 1661214:
               if (autoJob8.equals("6510")) {
                  var3 = 31;
               }
               break;
            case 46851278:
               if (autoJob8.equals("14210")) {
                  var3 = 37;
               }
               break;
            case 46880108:
               if (autoJob8.equals("15110")) {
                  var3 = 39;
               }
               break;
            case 46881069:
               if (autoJob8.equals("15210")) {
                  var3 = 40;
               }
               break;
            case 46882991:
               if (autoJob8.equals("15410")) {
                  var3 = 43;
               }
               break;
            case 46883952:
               if (autoJob8.equals("15510")) {
                  var3 = 41;
               }
               break;
            case 46910860:
               if (autoJob8.equals("16210")) {
                  var3 = 42;
               }
               break;
            case 46912782:
               if (autoJob8.equals("16410")) {
                  var3 = 44;
               }
            }

            switch(var3) {
            case 0:
               this.dropMessage(-1, "[영혼 검술의 기사] 크루세이더로 전직하였습니다.");
               this.changeJob(111);
               break;
            case 1:
               this.dropMessage(-1, "[속성 검술의 기사] 나이트로 전직하였습니다.");
               this.changeJob(121);
               break;
            case 2:
               this.dropMessage(-1, "[드래곤 창술의 기사] 드래곤 나이트로 전직하였습니다.");
               this.changeJob(131);
               break;
            case 3:
               this.dropMessage(-1, "[불*독] 메이지로 전직하였습니다.");
               this.changeJob(211);
               break;
            case 4:
               this.dropMessage(-1, "[얼음*번개] 메이지로 전직하였습니다.");
               this.changeJob(221);
               break;
            case 5:
               this.dropMessage(-1, "[힐*버프] 프리스트로 전직하였습니다.");
               this.changeJob(231);
               break;
            case 6:
               this.dropMessage(-1, "[연쇄 사격수] 레인저로 전직하였습니다.");
               this.changeJob(311);
               break;
            case 7:
               this.dropMessage(-1, "[백발백중 명사수] 저격수로 전직하였습니다.");
               this.changeJob(321);
               break;
            case 8:
               this.dropMessage(-1, "[체이서의 길] 패스파인더로 전직하였습니다.");
               this.changeJob(331);
               break;
            case 9:
               this.dropMessage(-1, "[암살 전문가] 허밋로 전직하였습니다.");
               this.changeJob(411);
               break;
            case 10:
               this.dropMessage(-1, "[암흑자] 시프 마스터로 전직하였습니다.");
               this.changeJob(421);
               break;
            case 11:
               this.dropMessage(-1, "[드래곤 너클 파이터] 버커니어로 전직하였습니다.");
               this.changeJob(511);
               break;
            case 12:
               this.dropMessage(-1, "[건 마스터리] 발키리로 전직하였습니다.");
               this.changeJob(521);
               break;
            case 13:
               this.dropMessage(-1, "[암흑을 알아버린자] 슬래셔로 전직하였습니다.");
               this.changeJob(433);
               break;
            case 14:
               this.dropMessage(-1, "[캐논 마스터리] 캐논슈터로 전직하였습니다.");
               this.changeJob(531);
               break;
            case 15:
               this.dropMessage(-1, "[영웅의 깨달음] 아란으로 전직하였습니다.");
               this.changeJob(2111);
               break;
            case 16:
               this.dropMessage(-1, "[진화의 드래곤] 에반으로 전직하였습니다.");
               this.changeJob(2214);
               break;
            case 17:
               this.dropMessage(-1, "[영웅의 깨달음] 메르세데스로 전직하였습니다.");
               this.changeJob(2311);
               break;
            case 18:
               this.dropMessage(-1, "[영웅의 깨달음] 팬텀으로 전직하였습니다.");
               this.changeJob(2411);
               break;
            case 19:
               this.dropMessage(-1, "[영웅의 깨달음] 은월으로 전직하였습니다.");
               this.changeJob(2511);
               break;
            case 20:
               this.dropMessage(-1, "[영웅의 깨달음] 루미너스로 전직하였습니다.");
               this.changeJob(2711);
               break;
            case 21:
               this.dropMessage(-1, "[부활한 마족] 데몬슬레이어로 전직하였습니다.");
               this.changeJob(3111);
               break;
            case 22:
               this.dropMessage(-1, "[분노의 화신] 데몬어벤져로 전직하였습니다.");
               this.changeJob(3121);
               break;
            case 23:
               this.dropMessage(-1, "[레지스탕스 요원] 배틀메이지로 전직하였습니다.");
               this.changeJob(3211);
               break;
            case 24:
               this.dropMessage(-1, "[레지스탕스 요원] 와일드헌터로 전직하였습니다.");
               this.changeJob(3311);
               break;
            case 25:
               this.dropMessage(-1, "[레지스탕스 요원] 메카닉으로 전직하였습니다.");
               this.changeJob(3511);
               break;
            case 26:
               this.dropMessage(-1, "[레지스탕스 요원] 제논으로 전직하였습니다.");
               this.changeJob(3611);
               break;
            case 27:
               this.dropMessage(-1, "[레지스탕스 요원] 블래스터로 전직하였습니다.");
               this.changeJob(3711);
               break;
            case 28:
               this.dropMessage(-1, "[시그너스 단장] 빛의 기사로 전직하였습니다.");
               this.changeJob(5111);
               break;
            case 29:
               this.dropMessage(-1, "[노바의 수호자] 카이저로 전직하였습니다.");
               this.changeJob(6111);
               break;
            case 30:
               this.dropMessage(-1, "[밤의 추적자] 카인으로 전직하였습니다.");
               this.changeJob(6311);
               break;
            case 31:
               this.dropMessage(-1, "[노바의 수호자] 엔젤릭버스터로 전직하였습니다.");
               this.changeJob(6511);
               break;
            case 32:
               this.dropMessage(-1, "[시그너스 정식 기사] 소울 마스터로 전직하였습니다.");
               this.changeJob(1111);
               break;
            case 33:
               this.dropMessage(-1, "[시그너스 정식 기사] 플레임 위자드로 전직하였습니다.");
               this.changeJob(1211);
               break;
            case 34:
               this.dropMessage(-1, "[시그너스 정식 기사] 윈드 브레이커로 전직하였습니다.");
               this.changeJob(1311);
               break;
            case 35:
               this.dropMessage(-1, "[시그너스 정식 기사] 나이트 워커로 전직하였습니다.");
               this.changeJob(1411);
               break;
            case 36:
               this.dropMessage(-1, "[시그너스 정식 기사] 스트라이커로 전직하였습니다.");
               this.changeJob(1511);
               break;
            case 37:
               this.dropMessage(-1, "[염동력의 황제] 키네시스로 전직하였습니다.");
               this.changeJob(14211);
               break;
            case 38:
               this.dropMessage(-1, "[노바의 귀재] 카데나로 전직하였습니다.");
               this.changeJob(6411);
               break;
            case 39:
               this.dropMessage(-1, "[검의 지휘자] 아델로 전직하였습니다.");
               this.changeJob(15111);
               break;
            case 40:
               this.dropMessage(-1, "[고대의 크리스탈] 일리움으로 전직하였습니다.");
               this.changeJob(15211);
               break;
            case 41:
               this.dropMessage(-1, "[심연의 분노] 아크로 전직하였습니다.");
               this.changeJob(15511);
               break;
            case 42:
               this.dropMessage(-1, "[낭만 풍수사] 라라로 전직하였습니다.");
               this.changeJob(16211);
               break;
            case 43:
               this.dropMessage(-1, "[복수의 바람] 칼리로 전직하였습니다.");
               this.changeJob(15411);
               break;
            case 44:
               this.dropMessage(-1, "[천방지축 도사] 호영으로 전직하였습니다.");
               this.changeJob(16411);
            }
         } else if (this.level == 80) {
            autoJob8 = this.getAutoJob();
            var3 = -1;
            switch(autoJob8.hashCode()) {
            case 1539167:
               if (autoJob8.equals("2210")) {
                  var3 = 0;
               }
            default:
               switch(var3) {
               case 0:
                  this.changeJob(2215);
               }
            }
         } else if (this.level == 100) {
            autoJob8 = this.getAutoJob();
            var3 = -1;
            switch(autoJob8.hashCode()) {
            case 48656:
               if (autoJob8.equals("110")) {
                  var3 = 0;
               }
               break;
            case 48687:
               if (autoJob8.equals("120")) {
                  var3 = 1;
               }
               break;
            case 48718:
               if (autoJob8.equals("130")) {
                  var3 = 2;
               }
               break;
            case 49617:
               if (autoJob8.equals("210")) {
                  var3 = 3;
               }
               break;
            case 49648:
               if (autoJob8.equals("220")) {
                  var3 = 4;
               }
               break;
            case 49679:
               if (autoJob8.equals("230")) {
                  var3 = 5;
               }
               break;
            case 50578:
               if (autoJob8.equals("310")) {
                  var3 = 6;
               }
               break;
            case 50609:
               if (autoJob8.equals("320")) {
                  var3 = 7;
               }
               break;
            case 50640:
               if (autoJob8.equals("330")) {
                  var3 = 8;
               }
               break;
            case 51539:
               if (autoJob8.equals("410")) {
                  var3 = 9;
               }
               break;
            case 51570:
               if (autoJob8.equals("420")) {
                  var3 = 10;
               }
               break;
            case 51601:
               if (autoJob8.equals("430")) {
                  var3 = 13;
               }
               break;
            case 52500:
               if (autoJob8.equals("510")) {
                  var3 = 11;
               }
               break;
            case 52531:
               if (autoJob8.equals("520")) {
                  var3 = 12;
               }
               break;
            case 52562:
               if (autoJob8.equals("530")) {
                  var3 = 14;
               }
               break;
            case 1508415:
               if (autoJob8.equals("1110")) {
                  var3 = 32;
               }
               break;
            case 1509376:
               if (autoJob8.equals("1210")) {
                  var3 = 33;
               }
               break;
            case 1510337:
               if (autoJob8.equals("1310")) {
                  var3 = 34;
               }
               break;
            case 1511298:
               if (autoJob8.equals("1410")) {
                  var3 = 35;
               }
               break;
            case 1512259:
               if (autoJob8.equals("1510")) {
                  var3 = 36;
               }
               break;
            case 1538206:
               if (autoJob8.equals("2110")) {
                  var3 = 15;
               }
               break;
            case 1539167:
               if (autoJob8.equals("2210")) {
                  var3 = 16;
               }
               break;
            case 1540128:
               if (autoJob8.equals("2310")) {
                  var3 = 17;
               }
               break;
            case 1541089:
               if (autoJob8.equals("2410")) {
                  var3 = 18;
               }
               break;
            case 1542050:
               if (autoJob8.equals("2510")) {
                  var3 = 19;
               }
               break;
            case 1543972:
               if (autoJob8.equals("2710")) {
                  var3 = 20;
               }
               break;
            case 1567997:
               if (autoJob8.equals("3110")) {
                  var3 = 21;
               }
               break;
            case 1568028:
               if (autoJob8.equals("3120")) {
                  var3 = 22;
               }
               break;
            case 1568958:
               if (autoJob8.equals("3210")) {
                  var3 = 23;
               }
               break;
            case 1569919:
               if (autoJob8.equals("3310")) {
                  var3 = 24;
               }
               break;
            case 1571841:
               if (autoJob8.equals("3510")) {
                  var3 = 25;
               }
               break;
            case 1572802:
               if (autoJob8.equals("3610")) {
                  var3 = 26;
               }
               break;
            case 1573763:
               if (autoJob8.equals("3710")) {
                  var3 = 27;
               }
               break;
            case 1627579:
               if (autoJob8.equals("5110")) {
                  var3 = 28;
               }
               break;
            case 1657370:
               if (autoJob8.equals("6110")) {
                  var3 = 29;
               }
               break;
            case 1659292:
               if (autoJob8.equals("6310")) {
                  var3 = 30;
               }
               break;
            case 1660253:
               if (autoJob8.equals("6410")) {
                  var3 = 38;
               }
               break;
            case 1661214:
               if (autoJob8.equals("6510")) {
                  var3 = 31;
               }
               break;
            case 46851278:
               if (autoJob8.equals("14210")) {
                  var3 = 37;
               }
               break;
            case 46880108:
               if (autoJob8.equals("15110")) {
                  var3 = 39;
               }
               break;
            case 46881069:
               if (autoJob8.equals("15210")) {
                  var3 = 40;
               }
               break;
            case 46882991:
               if (autoJob8.equals("15410")) {
                  var3 = 43;
               }
               break;
            case 46883952:
               if (autoJob8.equals("15510")) {
                  var3 = 41;
               }
               break;
            case 46910860:
               if (autoJob8.equals("16210")) {
                  var3 = 42;
               }
               break;
            case 46912782:
               if (autoJob8.equals("16410")) {
                  var3 = 44;
               }
            }

            switch(var3) {
            case 0:
               this.dropMessage(-1, "[연쇄 검술의 마스터] 히어로로 전직하였습니다.");
               this.changeJob(112);
               break;
            case 1:
               this.dropMessage(-1, "[환상 검술의 마스터] 팔라딘로 전직하였습니다.");
               this.changeJob(122);
               break;
            case 2:
               this.dropMessage(-1, "[다크 드래곤 창술의 마스터] 다크 나이트로 전직하였습니다.");
               this.changeJob(132);
               break;
            case 3:
               this.dropMessage(-1, "[불*독 마스터] 아크메이지로 전직하였습니다.");
               this.changeJob(212);
               break;
            case 4:
               this.dropMessage(-1, "[얼음*번개 마스터] 아크메이지로 전직하였습니다.");
               this.changeJob(222);
               break;
            case 5:
               this.dropMessage(-1, "[힐*버프 마스터] 비숍으로 전직하였습니다.");
               this.changeJob(232);
               break;
            case 6:
               this.dropMessage(-1, "[화살 연사의 마스터] 보우 마스터로 전직하였습니다.");
               this.changeJob(312);
               break;
            case 7:
               this.dropMessage(-1, "[화살 파워의 마스터] 신궁로 전직하였습니다.");
               this.changeJob(322);
               break;
            case 8:
               this.dropMessage(-1, "[에인션트 보우의 달인] 패스파인더로 전직하였습니다.");
               this.changeJob(332);
               break;
            case 9:
               this.dropMessage(-1, "[연쇄 암살의 마스터] 나이트 로드로 전직하였습니다.");
               this.changeJob(412);
               break;
            case 10:
               this.dropMessage(-1, "[암흑의 암살 마스터] 섀도우로 전직하였습니다.");
               this.changeJob(422);
               break;
            case 11:
               this.dropMessage(-1, "[정령의 너클 파이터] 바이퍼로 전직하였습니다.");
               this.changeJob(512);
               break;
            case 12:
               this.dropMessage(-1, "[배틀 건 마스터리] 캡틴으로 전직하였습니다.");
               this.changeJob(522);
               break;
            case 13:
               this.dropMessage(-1, "[암흑을 조정하는자] 듀얼블레이드로 전직하였습니다.");
               this.changeJob(434);
               break;
            case 14:
               this.dropMessage(-1, "[파괴의 캐논 마스터리] 캐논슈터로 전직하였습니다.");
               this.changeJob(532);
               break;
            case 15:
               this.dropMessage(-1, "[영웅의 부활] 아란으로 전직하였습니다.");
               this.changeJob(2112);
               break;
            case 16:
               this.dropMessage(-1, "[강인한 드래곤] 에반으로 전직하였습니다.");
               this.changeJob(2217);
               break;
            case 17:
               this.dropMessage(-1, "[영웅의 부활] 메르세데스로 전직하였습니다.");
               this.changeJob(2312);
               break;
            case 18:
               this.dropMessage(-1, "[영웅의 부활] 팬텀으로 전직하였습니다.");
               this.changeJob(2412);
               this.changeSkillLevel(20031210, (byte)1, (byte)1);
               this.client.getSession().writeAndFlush(CField.updateCardStack(false, this.cardStack));
               break;
            case 19:
               this.dropMessage(-1, "[영웅의 부활] 은월으로 전직하였습니다.");
               this.changeJob(2512);
               break;
            case 20:
               this.dropMessage(-1, "[영웅의 부활] 루미너스로 전직하였습니다.");
               this.changeJob(2712);
               break;
            case 21:
               this.dropMessage(-1, "[레지스탕스의 영웅] 데몬슬레이어로 전직하였습니다.");
               this.changeJob(3112);
               break;
            case 22:
               this.dropMessage(-1, "[레지스탕스 영웅] 데몬어벤져로 전직하였습니다.");
               this.changeJob(3122);
               break;
            case 23:
               this.dropMessage(-1, "[레지스탕스의 영웅] 배틀메이지로 전직하였습니다.");
               this.changeJob(3212);
               break;
            case 24:
               this.dropMessage(-1, "[레지스탕스의 영웅] 와일드헌터로 전직하였습니다.");
               this.changeJob(3312);
               break;
            case 25:
               this.dropMessage(-1, "[레지스탕스의 영웅] 메카닉으로 전직하였습니다.");
               this.changeJob(3512);
               break;
            case 26:
               this.dropMessage(-1, "[레지스탕스의 영웅] 제논으로 전직하였습니다.");
               this.changeJob(3612);
               break;
            case 27:
               this.dropMessage(-1, "[레지스탕스의 영웅] 블래스터로 전직하였습니다.");
               this.changeJob(3712);
               break;
            case 28:
               this.dropMessage(-1, "[시그너스 단장] 빛의 기사로 전직하였습니다.");
               this.changeJob(5112);
               break;
            case 29:
               this.dropMessage(-1, "[용의 기사] 카이저로 전직하였습니다.");
               this.changeJob(6112);
               break;
            case 30:
               this.dropMessage(-1, "[밤의 추적자] 카인으로 전직하였습니다.");
               this.changeJob(6312);
               break;
            case 31:
               this.dropMessage(-1, "[전장의 아이돌] 엔젤릭버스터로 전직하였습니다.");
               this.changeJob(6512);
               break;
            case 32:
               this.dropMessage(-1, "[시그너스 영웅] 빛의 대정령으로 전직하였습니다.");
               this.changeJob(1112);
               break;
            case 33:
               this.dropMessage(-1, "[시그너스 영웅] 불의 대정령으로 전직하였습니다.");
               this.changeJob(1212);
               break;
            case 34:
               this.dropMessage(-1, "[시그너스 영웅] 바람의 대정령으로 전직하였습니다.");
               this.changeJob(1312);
               break;
            case 35:
               this.dropMessage(-1, "[시그너스 영웅] 어둠의 대정령으로 전직하였습니다.");
               this.changeJob(1412);
               break;
            case 36:
               this.dropMessage(-1, "[시그너스 영웅] 번개의 대정령으로 전직하였습니다.");
               this.changeJob(1512);
               break;
            case 37:
               this.dropMessage(-1, "[염동력의 황제] 키네시스로 전직하였습니다.");
               this.changeJob(14212);
               break;
            case 38:
               this.dropMessage(-1, "[노바의 귀재] 카데나로 전직하였습니다.");
               this.changeJob(6412);
               break;
            case 39:
               this.dropMessage(-1, "[검의 지휘자] 아델로 전직하였습니다.");
               this.changeJob(15112);
               break;
            case 40:
               this.dropMessage(-1, "[고대의 크리스탈] 일리움으로 전직하였습니다.");
               this.changeJob(15212);
               break;
            case 41:
               this.dropMessage(-1, "[심연의 분노] 아크로 전직하였습니다.");
               this.changeJob(15512);
               break;
            case 42:
               this.dropMessage(-1, "[낭만 풍수사] 라라로 전직하였습니다.");
               this.changeJob(16212);
               break;
            case 43:
               this.dropMessage(-1, "[복수의 바람] 칼리로 전직하였습니다.");
               this.changeJob(15412);
               break;
            case 44:
               this.dropMessage(-1, "[천방지축 도사] 호영으로 전직하였습니다.");
               this.changeJob(16412);
            }
         }
      }

   }

   public void setAutoJob(int jobid) {
      this.getQuestNAdd(MapleQuest.getInstance(111113)).setCustomData(String.valueOf(jobid));
   }

   public String getAutoJob() {
      return this.getQuestNoAdd(MapleQuest.getInstance(111113)) == null ? null : this.getQuestNoAdd(MapleQuest.getInstance(111113)).getCustomData();
   }

   public void changeKeybinding(int key, byte type, int action) {
      if (type != 0) {
         this.keylayout.Layout().put(key, new Pair(type, action));
      } else {
         this.keylayout.Layout().remove(key);
      }

   }

   public void sendMacros() {
      this.client.getSession().writeAndFlush(CField.getMacros(this.skillMacros));
   }

   public void updateMacros(int position, SkillMacro updateMacro) {
      this.skillMacros[position] = updateMacro;
      this.changed_skillmacros = true;
   }

   public final SkillMacro[] getMacros() {
      return this.skillMacros;
   }

   public final List<AvatarLook> getCoodination() {
      return this.coodination;
   }

   public void setMarriage(MarriageMiniBox mgs) {
      this.mg = mgs;
   }

   public MarriageMiniBox getMarriage() {
      return this.mg;
   }

   public void tempban(String reason, Calendar duration, int greason, boolean IPMac) {
      if (IPMac) {
         this.client.banMacs();
      }

      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         if (IPMac) {
            ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
            ps.setString(1, this.client.getSession().remoteAddress().toString().split(":")[0]);
            ps.execute();
            ps.close();
         }

         this.client.getSession().close();
         ps = con.prepareStatement("UPDATE accounts SET tempban = ?, banreason = ?, greason = ? WHERE id = ?");
         Timestamp TS = new Timestamp(duration.getTimeInMillis());
         ps.setTimestamp(1, TS);
         ps.setString(2, reason);
         ps.setInt(3, greason);
         ps.setInt(4, this.accountid);
         ps.execute();
         ps.close();
         con.close();
      } catch (SQLException var17) {
         System.err.println("Error while tempbanning" + var17);
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
         } catch (SQLException var16) {
         }

      }

   }

   public final boolean ban(String reason, boolean IPMac, boolean autoban, boolean hellban) {
      if (this.lastmonthfameids == null) {
         throw new RuntimeException("Trying to ban a non-loaded character (testhack)");
      } else {
         Connection con = null;
         PreparedStatement ps = null;
         Object rs = null;

         label155: {
            boolean var9;
            try {
               con = DatabaseConnection.getConnection();
               ps = con.prepareStatement("UPDATE accounts SET banned = ?, banreason = ? WHERE id = ?");
               ps.setInt(1, autoban ? 2 : 1);
               ps.setString(2, reason);
               ps.setInt(3, this.accountid);
               ps.execute();
               ps.close();
               if (IPMac) {
                  this.client.banMacs();
                  ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
                  ps.setString(1, this.client.getSessionIPAddress());
                  ps.execute();
                  ps.close();
                  if (hellban) {
                     PreparedStatement psa = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
                     psa.setInt(1, this.accountid);
                     ResultSet rsa = psa.executeQuery();
                     if (rsa.next()) {
                        PreparedStatement pss = con.prepareStatement("UPDATE accounts SET banned = ?, banreason = ? WHERE email = ? OR SessionIP = ?");
                        pss.setInt(1, autoban ? 2 : 1);
                        pss.setString(2, reason);
                        pss.setString(3, rsa.getString("email"));
                        pss.setString(4, this.client.getSessionIPAddress());
                        pss.execute();
                        pss.close();
                     }

                     rsa.close();
                     psa.close();
                  }
               }

               con.close();
               break label155;
            } catch (SQLException var19) {
               System.err.println("Error while banning" + var19);
               var9 = false;
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
               } catch (SQLException var18) {
               }

            }

            return var9;
         }

         this.client.getSession().close();
         return true;
      }
   }

   public static boolean ban(String id, String reason, boolean accountId, int gmlevel, boolean hellban) {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean ret;
      try {
         if (!id.matches("/[0-9]{1,3}\\..*")) {
            if (accountId) {
               ps = ((Connection)con).prepareStatement("SELECT id FROM accounts WHERE name = ?");
            } else {
               ps = ((Connection)con).prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            }

            ret = false;
            ps.setString(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
               int z = rs.getInt(1);
               PreparedStatement psb = ((Connection)con).prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE id = ? AND gm < ?");
               psb.setString(1, reason);
               psb.setInt(2, z);
               psb.setInt(3, gmlevel);
               psb.execute();
               psb.close();
               if (gmlevel > 100) {
                  PreparedStatement psa = ((Connection)con).prepareStatement("SELECT * FROM accounts WHERE id = ?");
                  psa.setInt(1, z);
                  ResultSet rsa = psa.executeQuery();
                  if (rsa.next()) {
                     String sessionIP = rsa.getString("sessionIP");
                     PreparedStatement pss;
                     if (sessionIP != null && sessionIP.matches("/[0-9]{1,3}\\..*")) {
                        pss = ((Connection)con).prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
                        pss.setString(1, sessionIP);
                        pss.execute();
                        pss.close();
                     }

                     if (rsa.getString("macs") != null) {
                        String[] macData = rsa.getString("macs").split(", ");
                        if (macData.length > 0) {
                           MapleClient.banMacs(macData);
                        }
                     }

                     if (hellban) {
                        pss = ((Connection)con).prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE email = ?" + (sessionIP == null ? "" : " OR SessionIP = ?"));
                        pss.setString(1, reason);
                        pss.setString(2, rsa.getString("email"));
                        if (sessionIP != null) {
                           pss.setString(3, sessionIP);
                        }

                        pss.execute();
                        pss.close();
                     }
                  }

                  rsa.close();
                  psa.close();
               }

               ret = true;
            }

            rs.close();
            ps.close();
            ((Connection)con).close();
            boolean var26 = ret;
            return var26;
         }

         ps = ((Connection)con).prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
         ps.setString(1, id);
         ps.execute();
         ps.close();
         ret = true;
      } catch (SQLException var24) {
         System.err.println("Error while banning" + var24);
         return false;
      } finally {
         try {
            if (con != null) {
               ((Connection)con).close();
            }

            if (ps != null) {
               ps.close();
            }

            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var23) {
         }

      }

      return ret;
   }

   public int getObjectId() {
      return this.getId();
   }

   public void setObjectId(int id) {
      throw new UnsupportedOperationException();
   }

   public MapleStorage getStorage() {
      return this.storage;
   }

   public void addVisibleMapObject(MapleMapObject mo) {
      this.getVisibleMapObjects().add(mo);
   }

   public void removeVisibleMapObject(MapleMapObject mo) {
      this.getVisibleMapObjects().remove(mo);
   }

   public boolean isMapObjectVisible(MapleMapObject mo) {
      return this.getVisibleMapObjects().contains(mo);
   }

   public String getChairText() {
      return this.chairtext;
   }

   public void setChairText(String chairtext) {
      this.chairtext = chairtext;
   }

   public ScheduledFuture<?> getMesoChairTimer() {
      return this.MesoChairTimer;
   }

   public void setMesoChairTimer(ScheduledFuture<?> a1) {
      this.MesoChairTimer = a1;
      this.tempmeso = 0;
   }

   public int getMesoChairCount() {
      return this.MesoChairCount > 999999999 ? 999999999 : this.MesoChairCount;
   }

   public ScheduledFuture<?> getEventSkillTimer() {
      return this.EventSkillTimer;
   }

   public void setEventSkillTimer(ScheduledFuture<?> a1) {
      this.EventSkillTimer = a1;
   }

   public void UpdateMesoChairCount(int a1) {
      if (this.tempmeso >= a1) {
         this.MesoChairTimer.cancel(true);
         this.MesoChairTimer = null;
         this.setChair(0);
         this.setChairText("");
         this.getClient().getSession().writeAndFlush(CField.cancelChair(-1, this));
         this.getMap().broadcastMessage(this, CField.showChair(this, 0), true);
      } else {
         this.MesoChairCount += 500;
         this.gainMeso(-500L, false);
         this.tempmeso += 500;
         this.getMap().broadcastMessage(SLFCGPacket.MesoChairPacket(this.getId(), 500, this.getChair()));
      }
   }

   public long getStarDustPoint2() {
      return Long.valueOf(this.getKeyValue(501661, "e"));
   }

   public long getStarDustCoin2() {
      return Long.valueOf(this.getKeyValue(501661, "point"));
   }

   public void AddStarDustPoint2(int a) {
      int add = 0;
      if (this.getKeyValue(501661, "e") + (long)a >= 100L) {
         if (this.getKeyValue(501661, "point") == 2147483647L) {
            return;
         }

         this.setKeyValue(501661, "e", String.valueOf(this.getKeyValue(501661, "e") + (long)a));

         while(this.getKeyValue(501661, "e") >= 100L) {
            ++add;
            this.setKeyValue(501661, "point", String.valueOf(this.getKeyValue(501661, "point") + 1L));
            this.setKeyValue(501661, "e", String.valueOf(this.getKeyValue(501661, "e") - 100L));
         }

         this.client.getSession().writeAndFlush(SLFCGPacket.StarDustIncrease((int)this.getKeyValue(501661, "e"), a, false, (int)this.getKeyValue(501661, "point"), add, this.getPosition()));
      } else {
         this.setKeyValue(501661, "e", String.valueOf(this.getKeyValue(501661, "e") + (long)a));
         this.client.getSession().writeAndFlush(SLFCGPacket.StarDustIncrease((int)this.getKeyValue(501661, "e"), a, false, (int)this.getKeyValue(501661, "point"), add, this.getPosition()));
      }

   }

   public void AddStarDustCoin2(int a) {
      this.setKeyValue(501661, "point", String.valueOf(this.getKeyValue(501661, "point") + (long)a));
      if (a < 0) {
         this.getClient().getSession().writeAndFlush(SLFCGPacket.StarDustIncrease((int)this.getKeyValue(501661, "e"), 0, false, (int)this.getKeyValue(501661, "point"), 0, this.getPosition()));
      } else {
         if (this.getKeyValue(501661, "point") == 2147483647L) {
            return;
         }

         this.getClient().getSession().writeAndFlush(SLFCGPacket.StarDustIncrease((int)this.getKeyValue(501661, "e"), 0, false, (int)this.getKeyValue(501661, "point"), 0, this.getPosition()));
      }

   }

   public long getStarDustPoint(int type) {
      switch(type) {
      case 1:
         return this.getKeyValue(501661, "total");
      case 2:
         return this.getKeyValue(501661, "total");
      default:
         return this.getKeyValue(501661, "total");
      }
   }

   public long getStarDustCoin(int type) {
      switch(type) {
      case 1:
         return this.getKeyValue(501661, "point");
      case 2:
         return this.getKeyValue(501661, "point");
      default:
         return this.getKeyValue(501661, "point");
      }
   }

   public void AddStarDustCoin(int type, int a) {
      int key = 100711;
      int max = 1000000;
      boolean var5;
      switch(type) {
      case 1:
         if (this.getKeyValue(501661, "today") >= 1000000L) {
            return;
         }

         if (this.getKeyValue(501661, "today") + (long)a >= 1000000L) {
            a = 1000000 - (int)(this.getKeyValue(100711, "today") + (long)a);
            return;
         }

         key = 100711;
         max = 1000000;
         break;
      case 2:
         key = 100712;
         var5 = true;
         break;
      case 3:
         if (this.getKeyValue(501661, "week") >= 10000L) {
            return;
         }

         if (this.getKeyValue(501661, "week") + (long)a >= 10000L) {
            a = 10000 - (int)(this.getKeyValue(501215, "week") + (long)a);
            return;
         }

         key = 501215;
         var5 = true;
      }

      this.setKeyValue(key, "point", String.valueOf(this.getKeyValue(key, "point") + (long)a));
      this.setKeyValue(key, type == 3 ? "week" : "today", String.valueOf(this.getKeyValue(key, type == 3 ? "week" : "today") + (long)a));
      if (a >= 0 && this.getKeyValue(key, "point") + (long)a > 2147483647L) {
         this.setKeyValue(key, "point", "2147483647");
      }

   }

   public void AddStarDustPoint(int type, int a, Point point) {
      int key = 100711;
      int max = 1000000;
      switch(type) {
      case 1:
         key = 100711;
         max = 1000000;
         break;
      case 2:
         key = 100712;
         max = 1000;
         break;
      case 3:
         key = 501215;
         max = 10000;
      }

      if (this.getKeyValue(key, type == 3 ? "week" : "today") == -1L) {
         this.setKeyValue(key, type == 3 ? "week" : "today", "0");
      }

      if (this.getKeyValue(key, "point") == -1L) {
         this.setKeyValue(key, "point", "0");
      }

      if (this.getKeyValue(key, "total") == -1L) {
         this.setKeyValue(key, "total", "0");
      }

      if (this.getKeyValue(key, "sum") == -1L) {
         this.setKeyValue(key, "sum", "0");
      }

      if (this.getKeyValue(key, "total") + (long)a >= (type == 1 ? 100L : 1L)) {
         if (this.getKeyValue(key, "point") == 2147483647L || this.getKeyValue(key, "point") < 0L || this.getKeyValue(key, type == 3 ? "week" : "today") >= (long)max) {
            return;
         }

         if (this.getKeyValue(key, type == 3 ? "week" : "today") + (long)a >= (long)max) {
            a = max - a;
         }

         if (a <= 0) {
            return;
         }

         this.setKeyValue(key, "point", String.valueOf(this.getKeyValue(key, "point") + (long)(a / (type == 1 ? 100 : 1))));
         this.setKeyValue(key, type == 3 ? "week" : "today", String.valueOf(this.getKeyValue(key, type == 3 ? "week" : "today") + (long)(a / (type == 1 ? 100 : 1))));
         this.setKeyValue(key, "sum", String.valueOf(this.getKeyValue(key, "sum") + (long)(a / (type == 1 ? 100 : 1))));
         this.setKeyValue(key, "total", String.valueOf(this.getKeyValue(key, "total") + (long)a));
      } else {
         this.setKeyValue(key, "total", String.valueOf(this.getKeyValue(key, "total") + (long)a));
      }

   }

   public void AddBloomingCoin(int add, Point point) {
      if (this.getKeyValue(100794, "point") < 0L) {
         this.setKeyValue(100794, "point", "0");
      }

      if (this.getKeyValue(100794, "today") < 0L) {
         this.setKeyValue(100794, "today", "0");
      }

      boolean lock = false;
      if (add > 0) {
         if (this.getKeyValue(100794, "today") >= (Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 3000L : 6000L)) {
            lock = true;
         }

         if (this.getKeyValue(100794, "today") + (long)add >= (Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 3000L : 6000L)) {
            lock = true;
            int minus = (int)(this.getKeyValue(100794, "today") + (long)add - (Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 3000L : 6000L));
            add -= minus;
            this.setKeyValue(100794, "point", (this.getKeyValue(100794, "point") + (long)add).makeConcatWithConstants<invokedynamic>(this.getKeyValue(100794, "point") + (long)add));
         }

         if (lock) {
            int var10003 = Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 3000 : 6000;
            this.setKeyValue(100794, "today", var10003.makeConcatWithConstants<invokedynamic>(var10003));
            this.setKeyValue(100794, "lock", "1");
            this.getClient().send(SLFCGPacket.StarDustUI("UI/UIWindowEvent.img/starDust_18th", this.getKeyValue(100794, "sum"), this.getKeyValue(100794, "point"), this.getKeyValue(100794, "lock") == 1L));
            return;
         }

         if (point != null) {
            this.client.getSession().writeAndFlush(SLFCGPacket.StarDustIncrease(0, 100, this.getKeyValue(100794, "today") >= (Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 3000L : 6000L), (int)this.getKeyValue(100794, "point"), 1, new Point(0, 0)));
         }
      }

      this.setKeyValue(100794, "point", (this.getKeyValue(100794, "point") + (long)add).makeConcatWithConstants<invokedynamic>(this.getKeyValue(100794, "point") + (long)add));
      if (add > 0) {
         this.setKeyValue(100794, "today", (this.getKeyValue(100794, "today") + (long)add).makeConcatWithConstants<invokedynamic>(this.getKeyValue(100794, "today") + (long)add));
      }

   }

   public int getBloomingCoin() {
      return (int)this.getKeyValue(100794, "point");
   }

   public void AddCoin(int questid, int add) {
      this.AddCoin(questid, add, false);
   }

   public void AddCoin(int questid, int add, boolean ig) {
      if (this.getKeyValue(questid, "point") < 0L) {
         this.setKeyValue(questid, "point", "0");
      }

      if (this.getKeyValue(questid, "today") < 0L) {
         this.setKeyValue(questid, "today", "0");
      }

      boolean lock = false;
      if (add > 0 && !ig) {
         if (this.getKeyValue(questid, "lock") == 1L) {
            return;
         }

         if (this.getKeyValue(questid, "today") >= (long)(Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 300 : 600)) {
            lock = true;
         }

         if (this.getKeyValue(questid, "today") + (long)add >= (long)(Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 300 : 600)) {
            lock = true;
            int minus = (int)(this.getKeyValue(questid, "today") + (long)add - (long)(Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 300 : 600));
            add -= minus;
            this.setKeyValue(questid, "point", (this.getKeyValue(questid, "point") + (long)add).makeConcatWithConstants<invokedynamic>(this.getKeyValue(questid, "point") + (long)add));
         }

         if (lock) {
            int var10003 = Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 300 : 600;
            this.setKeyValue(questid, "today", var10003.makeConcatWithConstants<invokedynamic>(var10003));
            this.setKeyValue(questid, "lock", "1");
            return;
         }
      }

      this.setKeyValue(questid, "point", (this.getKeyValue(questid, "point") + (long)add).makeConcatWithConstants<invokedynamic>(this.getKeyValue(questid, "point") + (long)add));
      if (add > 0 && !ig) {
         this.setKeyValue(questid, "today", (this.getKeyValue(questid, "today") + (long)add).makeConcatWithConstants<invokedynamic>(this.getKeyValue(questid, "today") + (long)add));
      }

   }

   public void AddCoinAcc(int questid, int add) {
      this.AddCoinAcc(questid, add, false);
   }

   public void AddCoinAcc(int questid, int add, boolean ig) {
      if (this.client.getCustomKeyValue(questid, "point") < 0L) {
         this.client.setCustomKeyValue(questid, "point", "0");
      }

      if (this.client.getCustomKeyValue(questid, "week") < 0L) {
         this.client.setCustomKeyValue(questid, "week", "0");
      }

      boolean lock = false;
      if (add > 0 && !ig) {
         if (this.client.getCustomKeyValue(questid, "lock") == 1L) {
            return;
         }

         if (this.client.getCustomKeyValue(questid, "week") >= 400L) {
            lock = true;
         }

         if (this.client.getCustomKeyValue(questid, "week") + (long)add >= 400L) {
            lock = true;
            int minus = (int)(this.client.getCustomKeyValue(questid, "week") + (long)add - 400L);
            add -= minus;
            this.client.setCustomKeyValue(questid, "point", (this.client.getCustomKeyValue(questid, "point") + (long)add).makeConcatWithConstants<invokedynamic>(this.client.getCustomKeyValue(questid, "point") + (long)add));
         }

         if (lock) {
            this.client.setCustomKeyValue(questid, "week", "400");
            this.client.setCustomKeyValue(questid, "lock", "1");
            return;
         }
      }

      this.client.setCustomKeyValue(questid, "point", (this.client.getCustomKeyValue(questid, "point") + (long)add).makeConcatWithConstants<invokedynamic>(this.client.getCustomKeyValue(questid, "point") + (long)add));
      if (add > 0 && !ig) {
         this.client.setCustomKeyValue(questid, "week", (this.client.getCustomKeyValue(questid, "week") + (long)add).makeConcatWithConstants<invokedynamic>(this.client.getCustomKeyValue(questid, "week") + (long)add));
      }

   }

   public int getCoin(int questid) {
      return (int)this.getKeyValue(questid, "point");
   }

   public void addDojoCoin(int a) {
      this.setKeyValue(3887, "point", String.valueOf(this.getKeyValue(3887, "point") + (long)a));
   }

   public Point getResolution() {
      return this.Resolution;
   }

   public void setResolution(int Width, int Height) {
      this.Resolution = new Point(Width, Height);
   }

   public DetectiveGame getDetectiveGame() {
      return this.DetectiveGameInstance;
   }

   public void setDetectiveGame(DetectiveGame a1) {
      this.DetectiveGameInstance = a1;
   }

   public OXQuizGame getOXGame() {
      return this.OXInstance;
   }

   public void setOXGame(OXQuizGame a1) {
      this.OXInstance = a1;
   }

   public BingoGame getBingoGame() {
      return this.BingoInstance;
   }

   public void setBingoGame(BingoGame a1) {
      this.BingoInstance = a1;
   }

   public BattleReverse getBattleReverseInstance() {
      return this.BattleReverseInstance;
   }

   public void setBattleReverseInstance(BattleReverse a1) {
      this.BattleReverseInstance = a1;
   }

   public boolean isAFK(long currenttick) {
      if (this.LastMovement != 0L && this.getMap().getId() == ServerConstants.fishMap && this.getChair() != 0) {
         long temp = currenttick - this.LastMovement;
         return temp / 1000L >= 60L;
      } else {
         return false;
      }
   }

   public void setLastMovement(long a) {
      this.LastMovement = a;
   }

   public void setPlatformerStageEnter(long a) {
      this.PlatformerStageEnter = a;
   }

   public long getPlatformerStageEnter() {
      return this.PlatformerStageEnter;
   }

   public void setPlatformerTimer(ScheduledFuture<?> a) {
      this.PlatformerTimer = a;
   }

   public ScheduledFuture<?> getPlatformerTimer() {
      return this.PlatformerTimer;
   }

   public int getBlockCount() {
      return this.BlockCount;
   }

   public void setBlockCount(int a1) {
      this.BlockCount = a1;
   }

   public void setBlockCoin(int a1) {
      this.BlockCoin = a1;
   }

   public int getBlockCoin() {
      return this.BlockCoin;
   }

   public void addBlockCoin(int a1) {
      this.BlockCoin += a1;
   }

   public int getRandomPortal() {
      return this.RandomPortal;
   }

   public void setRandomPortal(int a1) {
      this.RandomPortal = a1;
   }

   public boolean hasFWolfPortal() {
      return this.hasfwolfportal;
   }

   public void setFWolfPortal(boolean a1) {
      this.hasfwolfportal = a1;
   }

   public boolean isWatchingWeb() {
      return this.isWatchingWeb;
   }

   public void setWatchingWeb(boolean a1) {
      this.isWatchingWeb = a1;
   }

   public boolean isFWolfKiller() {
      return this.isfwolfkiller;
   }

   public void setFWolfKiller(boolean a1) {
      this.isfwolfkiller = a1;
   }

   public long getFWolfDamage() {
      return this.fwolfdamage;
   }

   public void setFWolfDamage(long a1) {
      this.fwolfdamage = a1;
   }

   public int getFWolfAttackCount() {
      return this.fwolfattackcount;
   }

   public void setFWolfAttackCount(int a1) {
      this.fwolfattackcount = a1;
   }

   public boolean isAlive() {
      return this.stats.getHp() > 0L;
   }

   public void sendDestroyData(MapleClient client) {
      client.getSession().writeAndFlush(CField.removePlayerFromMap(this.getObjectId()));
   }

   public void sendSpawnData(MapleClient client) {
      if (client.getPlayer().allowedToTarget(this)) {
         if (this.getMapId() != 921172000 && this.getMapId() != 921172100) {
            client.getSession().writeAndFlush(CField.spawnPlayerMapobject(this));
         }

         if (this.getKeyValue(190823, "grade") > 0L) {
            client.getSession().writeAndFlush(SLFCGPacket.SetupZodiacInfo());
            client.getSession().writeAndFlush(SLFCGPacket.ZodiacRankInfo(this.getId(), (int)this.getKeyValue(190823, "grade")));
         }

         client.getPlayer().receivePartyMemberHP();
         if (this.dragon != null) {
            client.getSession().writeAndFlush(CField.spawnDragon(this.dragon));
         }

         if (this.android != null) {
            client.getSession().writeAndFlush(CField.spawnAndroid(this, this.android));
         }

         if (this.getGuild() != null && this.getGuild().getCustomEmblem() != null && client.getAccID() != this.getAccountID()) {
            client.getSession().writeAndFlush(CField.loadGuildIcon(this));
         }

         Iterator var2 = this.summons.iterator();

         while(var2.hasNext()) {
            MapleSummon summon = (MapleSummon)var2.next();
            if (summon.getMovementType() != SummonMovementType.STATIONARY) {
               client.getSession().writeAndFlush(CField.SummonPacket.spawnSummon(summon, true));
               switch(summon.getSkill()) {
               case 5201012:
               case 5201013:
               case 5201014:
               case 5210015:
               case 5210016:
               case 5210017:
               case 5210018:
                  if (summon.getDebuffshell() <= 0) {
                     this.getMap().broadcastMessage(CField.SummonPacket.summonDebuff(summon.getOwner().getId(), summon.getObjectId()));
                  }
               }
            }
         }

         if (GameConstants.isAdel(client.getPlayer().getJob()) && client.getPlayer().getBuffedValue(151101006)) {
            client.getPlayer().에테르핸들러(client.getPlayer(), 0, 0, true);
         }

         if (GameConstants.isAdel(this.getJob()) && this.getBuffedValue(151101006)) {
            if (this.에테르소드 > 0 && this.에테르소드 <= 2) {
               this.getMap().broadcastMessage(this, SkillPacket.CreateSworldReadyObtacle(this, 15112, 2), false);
            } else if (this.에테르소드 > 0 && this.에테르소드 <= 4) {
               this.getMap().broadcastMessage(this, SkillPacket.CreateSworldReadyObtacle(this, 15112, 2), false);
               this.getMap().broadcastMessage(this, SkillPacket.CreateSworldReadyObtacle(this, 15112, 4), false);
            } else if (this.에테르소드 > 0 && this.에테르소드 <= 6) {
               this.getMap().broadcastMessage(this, SkillPacket.CreateSworldReadyObtacle(this, 15112, 2), false);
               this.getMap().broadcastMessage(this, SkillPacket.CreateSworldReadyObtacle(this, 15112, 4), false);
               this.getMap().broadcastMessage(this, SkillPacket.CreateSworldReadyObtacle(this, 15112, 6), false);
            }
         }

         if (this.getBuffedValue(SecondaryStat.RepeatEffect) != null) {
            int skillid = this.getBuffedEffect(SecondaryStat.RepeatEffect).getSourceId();
            if (GameConstants.isAngelicBlessBuffEffectItem(skillid)) {
               EnumMap<SecondaryStat, Pair<Integer, Integer>> statups = new EnumMap(SecondaryStat.class);
               statups.put(SecondaryStat.RepeatEffect, new Pair(1, 0));
               SecondaryStatEffect effect = MapleItemInformationProvider.getInstance().getItemEffect(skillid);
               this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, effect, this));
            }
         }
      }

   }

   public final void equipChanged() {
      if (this.map != null) {
         boolean second = false;
         if (GameConstants.isAngelicBuster(this.getJob())) {
            second = this.getDressup();
         }

         if (GameConstants.isZero(this.getJob())) {
            second = this.getGender() == 1;
         }

         this.map.broadcastMessage(this, CField.updateCharLook(this, second), false);
         this.stats.recalcLocalStats(this);
         if (this.getMessenger() != null) {
            World.Messenger.updateMessenger(this.getMessenger().getId(), this.getName(), this.client.getChannel());
         }

      }
   }

   public final MaplePet getPet(long index) {
      return this.pets[(int)index];
   }

   public void updatePet() {
      for(int i = 0; i < 3; ++i) {
         if (this.pets[i] != null) {
            this.getClient().getSession().writeAndFlush(PetPacket.updatePet(this, this.pets[i], this.getInventory(MapleInventoryType.CASH).getItem(this.pets[i].getInventoryPosition()), false, this.petLoot));
         }
      }

   }

   public void addPet(MaplePet pet) {
      for(int i = 0; i < 3; ++i) {
         if (this.pets[i] == null) {
            this.pets[i] = pet;
            return;
         }
      }

   }

   public void addPetBySlotId(MaplePet pet, int slotid) {
      if (this.pets[slotid] == null) {
         (this.pets[slotid] = pet).setPos(this.getPosition());
      }

   }

   public void setDotDamage(long dmg) {
      this.DotDamage = dmg;
   }

   public long getDotDamage() {
      return this.DotDamage;
   }

   public Point getFlameHeiz() {
      return this.flameHeiz;
   }

   public void setFlameHeiz(Point flameHeiz) {
      this.flameHeiz = flameHeiz;
   }

   public void removePet(MaplePet pet, boolean shiftLeft) {
      int slot = -1;

      int i;
      for(i = 0; i < 3; ++i) {
         if (this.pets[i] != null && this.pets[i].getUniqueId() == pet.getUniqueId()) {
            this.pets[i] = null;
            slot = i;
            break;
         }
      }

      if (shiftLeft && slot > -1) {
         for(i = slot; i < 3; ++i) {
            if (i != 2) {
               this.pets[i] = this.pets[i + 1];
            } else {
               this.pets[i] = null;
            }
         }
      }

   }

   public final int getPetIndex(MaplePet pet) {
      for(int i = 0; i < 3; ++i) {
         if (this.pets[i] != null && this.pets[i].getUniqueId() == pet.getUniqueId()) {
            return i;
         }
      }

      return -1;
   }

   public final int getPetIndex(long petId) {
      for(int i = 0; i < 3; ++i) {
         if (this.pets[i] != null && this.pets[i].getUniqueId() == petId) {
            return i;
         }
      }

      return -1;
   }

   public final List<MaplePet> getSummonedPets() {
      List<MaplePet> ret = new ArrayList();
      MaplePet[] var2 = this.pets;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         MaplePet pet = var2[var4];
         if (pet.getSummoned()) {
            ret.add(pet);
         }
      }

      return ret;
   }

   public final byte getPetById(int petId) {
      byte count = 0;
      MaplePet[] var3 = this.pets;
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         MaplePet pet = var3[var5];
         if (pet.getSummoned()) {
            if (pet.getPetItemId() == petId) {
               return count;
            }

            ++count;
         }
      }

      return -1;
   }

   public final MaplePet[] getPets() {
      return this.pets;
   }

   public final void unequipAllPets() {
      MaplePet[] var1 = this.pets;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         MaplePet pet = var1[var3];
         if (pet != null) {
            this.unequipPet(pet, true, false);
         }
      }

   }

   public void unequipPet(MaplePet pet, boolean shiftLeft, boolean hunger) {
      pet.setSummoned((byte)0);
      this.client.getSession().writeAndFlush(PetPacket.updatePet(this, pet, this.getInventory(MapleInventoryType.CASH).getItem(pet.getInventoryPosition()), true, this.petLoot));
      if (this.map != null) {
         this.map.broadcastMessage(this, PetPacket.showPet(this, pet, true, hunger), true);
      }

      this.removePet(pet, shiftLeft);
      this.client.getSession().writeAndFlush(CWvsContext.enableActions(this));
   }

   public final long getLastFameTime() {
      return this.lastfametime;
   }

   public final List<Integer> getFamedCharacters() {
      return this.lastmonthfameids;
   }

   public final List<Integer> getBattledCharacters() {
      return this.lastmonthbattleids;
   }

   public MapleCharacter.FameStatus canGiveFame(MapleCharacter from) {
      if (this.lastfametime >= System.currentTimeMillis() - 86400000L) {
         return MapleCharacter.FameStatus.NOT_TODAY;
      } else {
         return from != null && this.lastmonthfameids != null && !this.lastmonthfameids.contains(from.getId()) ? MapleCharacter.FameStatus.OK : MapleCharacter.FameStatus.NOT_THIS_MONTH;
      }
   }

   public void hasGivenFame(MapleCharacter to) {
      this.lastfametime = System.currentTimeMillis();
      this.lastmonthfameids.add(to.getId());
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("INSERT INTO famelog (characterid, characterid_to) VALUES (?, ?)");
         ps.setInt(1, this.getId());
         ps.setInt(2, to.getId());
         ps.execute();
         ps.close();
      } catch (SQLException var14) {
         PrintStream var10000 = System.err;
         String var10001 = this.getName();
         var10000.println("ERROR writing famelog for char " + var10001 + " to " + to.getName() + var14);
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
         }

      }

   }

   public boolean canBattle(MapleCharacter to) {
      return to != null && this.lastmonthbattleids != null && !this.lastmonthbattleids.contains(to.getAccountID());
   }

   public void hasBattled(MapleCharacter to) {
      this.lastmonthbattleids.add(to.getAccountID());
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("INSERT INTO battlelog (accid, accid_to) VALUES (?, ?)");
         ps.setInt(1, this.getAccountID());
         ps.setInt(2, to.getAccountID());
         ps.execute();
         ps.close();
      } catch (SQLException var14) {
         PrintStream var10000 = System.err;
         String var10001 = this.getName();
         var10000.println("ERROR writing battlelog for char " + var10001 + " to " + to.getName() + var14);
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
         }

      }

   }

   public final MapleKeyLayout getKeyLayout() {
      return this.keylayout;
   }

   public MapleParty getParty() {
      if (this.party == null) {
         return null;
      } else {
         if (this.party.isDisbanded()) {
            this.party = null;
         }

         return this.party;
      }
   }

   public byte getWorld() {
      return this.world;
   }

   public void setWorld(byte world) {
      this.world = world;
   }

   public void setParty(MapleParty party) {
      this.party = party;
   }

   public MapleTrade getTrade() {
      return this.trade;
   }

   public void setTrade(MapleTrade trade) {
      this.trade = trade;
   }

   public EventInstanceManager getEventInstance() {
      return this.eventInstance;
   }

   public void setEventInstance(EventInstanceManager eventInstance) {
      this.eventInstance = eventInstance;
   }

   public void addDoor(MapleDoor door) {
      this.doors.add(door);
   }

   public void clearDoors() {
      this.doors.clear();
   }

   public List<MapleDoor> getDoors() {
      return new ArrayList(this.doors);
   }

   public void addMechDoor(MechDoor door) {
      this.mechDoors.add(door);
   }

   public void clearMechDoors() {
      this.mechDoors.clear();
   }

   public List<MechDoor> getMechDoors() {
      return new ArrayList(this.mechDoors);
   }

   public void setSmega() {
      if (this.smega) {
         this.smega = false;
         this.dropMessage(5, "You have set megaphone to disabled mode");
      } else {
         this.smega = true;
         this.dropMessage(5, "You have set megaphone to enabled mode");
      }

   }

   public boolean getSmega() {
      return this.smega;
   }

   public List<MapleSummon> getSummons() {
      return this.summons;
   }

   public MapleSummon getSummon(int skillId) {
      Iterator var2 = this.summons.iterator();

      MapleSummon s;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         s = (MapleSummon)var2.next();
      } while(s.getSkill() != skillId);

      return s;
   }

   public List<MapleSummon> getSummons(int skillId) {
      List<MapleSummon> arr = new ArrayList();
      Iterator var3 = this.summons.iterator();

      while(var3.hasNext()) {
         MapleSummon s = (MapleSummon)var3.next();
         if (GameConstants.getLinkedSkill(s.getSkill()) == skillId) {
            arr.add(s);
         }
      }

      return arr;
   }

   public int getSummonsSize() {
      return this.summons.size();
   }

   public void addSummon(MapleSummon s) {
      this.summons.add(s);
   }

   public void removeSummon(MapleSummon s) {
      this.summons.remove(s);
      if (s.getSkill() == 400011065) {
         this.cooldownEllision = System.currentTimeMillis();
      }

   }

   public int getChair() {
      return this.chair;
   }

   public int getItemEffect() {
      return this.itemEffect;
   }

   public void setChair(int chair) {
      this.chair = chair;
      this.stats.relocHeal(this);
   }

   public void setItemEffect(int itemEffect) {
      this.itemEffect = itemEffect;
   }

   public MapleMapObjectType getType() {
      return MapleMapObjectType.PLAYER;
   }

   public int getCurrentRep() {
      return this.currentrep;
   }

   public int getTotalRep() {
      return this.totalrep;
   }

   public int getTotalWins() {
      return this.totalWins;
   }

   public int getTotalLosses() {
      return this.totalLosses;
   }

   public void increaseTotalWins() {
      ++this.totalWins;
   }

   public void increaseTotalLosses() {
      ++this.totalLosses;
   }

   public int getGuildId() {
      return this.guildid;
   }

   public byte getGuildRank() {
      return this.guildrank;
   }

   public int getGuildContribution() {
      return this.guildContribution;
   }

   public int getLastAttendance() {
      return this.lastattendance;
   }

   public void setLastAttendance(int _c) {
      this.lastattendance = _c;
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE characters SET lastattendance = ? WHERE id = ?");
         ps.setInt(1, this.lastattendance);
         ps.setInt(2, this.id);
         ps.execute();
         ps.close();
         con.close();
      } catch (SQLException var14) {
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
         } catch (Exception var13) {
         }

      }

      if (this.mgc != null) {
         this.mgc.setLastAttendance(_c);
      }

   }

   public void setGuildId(int _id) {
      this.guildid = _id;
      if (this.guildid > 0) {
         if (this.mgc == null) {
            this.mgc = new MapleGuildCharacter(this);
         } else {
            this.mgc.setGuildId(this.guildid);
         }
      } else {
         this.mgc = null;
         this.guildContribution = 0;
      }

   }

   public void setGuildRank(byte _rank) {
      this.guildrank = _rank;
      if (this.mgc != null) {
         this.mgc.setGuildRank(_rank);
      }

   }

   public void setGuildContribution(int _c) {
      this.guildContribution = _c;
      if (this.mgc != null) {
         this.mgc.setGuildContribution(_c);
      }

   }

   public MapleGuildCharacter getMGC() {
      return this.mgc;
   }

   public void setAllianceRank(byte rank) {
      this.allianceRank = rank;
      if (this.mgc != null) {
         this.mgc.setAllianceRank(rank);
      }

   }

   public byte getAllianceRank() {
      return this.allianceRank;
   }

   public MapleGuild getGuild() {
      return this.getGuildId() <= 0 ? null : World.Guild.getGuild(this.getGuildId());
   }

   public void setJob(int j) {
      this.job = (short)j;
   }

   public void guildUpdate() {
      if (this.guildid > 0) {
         this.mgc.setLevel(this.level);
         this.mgc.setJobId(this.job);
         if (this.level != 256) {
            World.Guild.memberLevelJobUpdate(this.mgc);
         }

      }
   }

   public void runRainBowRush() {
      this.RainBowRushTime = 0;
      this.israinbow = true;
      this.warp(993190000);
      this.getClient().getSession().writeAndFlush(CField.UIPacket.getRainBowRushSetting());
   }

   public void setRainbowRushStart(long time) {
      this.rainbowrushStartTime = time;
   }

   public int getRainbowRushTime() {
      return this.RainBowRushTime;
   }

   public void setRainbowRushTime(int time) {
      this.RainBowRushTime = time;
   }

   public void setRainBowRush(boolean b) {
      this.israinbow = b;
   }

   public boolean isRainBowRush() {
      return this.israinbow;
   }

   public boolean isNoDeadRush() {
      return this.isnodeadRush;
   }

   public void setNoDeadRush(boolean b) {
      this.isnodeadRush = b;
   }

   public void saveGuildStatus() {
      MapleGuild.setOfflineGuildStatus(this.guildid, this.guildrank, this.guildContribution, this.allianceRank, this.id);
   }

   public void modifyCSPoints(int type, int quantity) {
      this.modifyCSPoints(type, quantity, false);
   }

   public void setNXcredit(int nxcredit) {
      this.nxcredit = nxcredit;
   }

   public void modifyCSPoints(int type, int quantity, boolean show) {
      switch(type) {
      case 1:
         if (this.nxcredit + quantity < 0) {
            if (show) {
               this.dropMessage(-1, "You have gained the max cash. No cash will be awarded.");
            }

            return;
         }

         this.nxcredit += quantity;
         break;
      case 2:
         if (this.maplepoints + quantity < 0) {
            if (show) {
               this.dropMessage(-1, "You have gained the max cash. No cash will be awarded.");
            }

            return;
         }

         this.maplepoints += quantity;
      case 3:
      default:
         break;
      case 4:
         if (this.acash + quantity < 0) {
            if (show) {
               this.dropMessage(-1, "You have gained the max cash. No cash will be awarded.");
            }

            return;
         }

         this.acash += quantity;
      }

      if (show && quantity != 0) {
      }

   }

   public int getCSPoints(int type) {
      switch(type) {
      case 1:
         return this.nxcredit;
      case 2:
         return this.maplepoints;
      case 3:
      default:
         return 0;
      case 4:
         return this.acash;
      }
   }

   public final boolean hasEquipped(int itemid) {
      return this.inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid) >= 1;
   }

   public final boolean haveItem(int itemid, int quantity, boolean checkEquipped, boolean greaterOrEquals) {
      MapleInventoryType type = GameConstants.getInventoryType(itemid);
      int possesed = this.inventory[type.ordinal()].countById(itemid);
      if (checkEquipped && type == MapleInventoryType.EQUIP) {
         possesed += this.inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
      }

      if (greaterOrEquals) {
         return possesed >= quantity;
      } else {
         return possesed == quantity;
      }
   }

   public final boolean haveItem(int itemid, int quantity) {
      return this.haveItem(itemid, quantity, true, true);
   }

   public final boolean haveItem(int itemid) {
      return this.haveItem(itemid, 1, true, true);
   }

   public byte getBuddyCapacity() {
      return this.buddylist.getCapacity();
   }

   public void setBuddyCapacity(byte capacity) {
      this.buddylist.setCapacity(capacity);
      this.client.getSession().writeAndFlush(CWvsContext.BuddylistPacket.updateBuddyCapacity(capacity));
   }

   public MapleMessenger getMessenger() {
      return this.messenger;
   }

   public void setMessenger(MapleMessenger messenger) {
      this.messenger = messenger;
   }

   public void addCooldown(int skillId, long startTime, long length) {
      this.coolDowns.put(skillId, new MapleCoolDownValueHolder(skillId, startTime, length));
   }

   public void removeCooldown(int skillId) {
      if (this.coolDowns.containsKey(skillId)) {
         if (skillId == 400051047 || skillId == 400051048) {
            if (skillId == 400051047) {
               this.getWeaponChanges().clear();
            } else if (skillId == 400051048) {
               this.getWeaponChanges2().clear();
            }
         }

         this.coolDowns.remove(skillId);
         this.getClient().getSession().writeAndFlush(CField.skillCooldown(skillId, 0));
      }

   }

   public void changeCooldown(int skillId, int reduce) {
      if (this.coolDowns.containsKey(skillId)) {
         MapleCoolDownValueHolder mapleCoolDownValueHolder = (MapleCoolDownValueHolder)this.coolDowns.get(skillId);
         mapleCoolDownValueHolder.length += (long)reduce;
         this.getClient().getSession().writeAndFlush(CField.skillCooldown(skillId, (int)Math.max(0L, ((MapleCoolDownValueHolder)this.coolDowns.get(skillId)).length - (System.currentTimeMillis() - ((MapleCoolDownValueHolder)this.coolDowns.get(skillId)).startTime))));
         if (System.currentTimeMillis() - ((MapleCoolDownValueHolder)this.coolDowns.get(skillId)).startTime >= ((MapleCoolDownValueHolder)this.coolDowns.get(skillId)).length) {
            this.removeCooldown(skillId);
         }
      }

   }

   public boolean skillisCooling(int skillId) {
      return (!this.coolDowns.containsKey(skillId) || ((MapleCoolDownValueHolder)this.coolDowns.get(skillId)).startTime + ((MapleCoolDownValueHolder)this.coolDowns.get(skillId)).length - System.currentTimeMillis() >= 0L) && this.coolDowns.containsKey(skillId);
   }

   public long skillcool(int skillId) {
      return ((MapleCoolDownValueHolder)this.coolDowns.get(skillId)).startTime + ((MapleCoolDownValueHolder)this.coolDowns.get(skillId)).length - System.currentTimeMillis();
   }

   public void giveCoolDowns(int skillid, long starttime, long length) {
      this.addCooldown(skillid, starttime, length);
   }

   public void giveCoolDowns(List<MapleCoolDownValueHolder> cooldowns) {
      if (cooldowns != null) {
         Iterator var2 = cooldowns.iterator();

         while(var2.hasNext()) {
            MapleCoolDownValueHolder cooldown = (MapleCoolDownValueHolder)var2.next();
            this.coolDowns.put(cooldown.skillId, cooldown);
         }
      } else {
         Connection con = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT SkillID,StartTime,length FROM skills_cooldowns WHERE charid = ?");
            ps.setInt(1, this.getId());
            rs = ps.executeQuery();

            while(rs.next()) {
               if (rs.getLong("length") + rs.getLong("StartTime") - System.currentTimeMillis() > 0L) {
                  this.giveCoolDowns(rs.getInt("SkillID"), rs.getLong("StartTime"), rs.getLong("length"));
               }
            }

            ps.close();
            rs.close();
            this.deleteWhereCharacterId(con, "DELETE FROM skills_cooldowns WHERE charid = ?");
            con.close();
         } catch (SQLException var14) {
            System.err.println("Error while retriving cooldown from SQL storage");
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
            }

         }
      }

   }

   public int getCooldownSize() {
      return this.coolDowns.size();
   }

   public int getDiseaseSize() {
      return this.getDiseases().size();
   }

   public List<MapleCoolDownValueHolder> getCooldowns() {
      List<MapleCoolDownValueHolder> ret = new ArrayList();
      Iterator var2 = this.coolDowns.values().iterator();

      while(var2.hasNext()) {
         MapleCoolDownValueHolder mc = (MapleCoolDownValueHolder)var2.next();
         if (mc != null) {
            ret.add(mc);
         }
      }

      return ret;
   }

   public MapleDiseases getDisease(SecondaryStat d) {
      return (MapleDiseases)this.getDiseases().get(d);
   }

   public final boolean hasDisease(SecondaryStat dis) {
      return this.getDiseases().containsKey(dis);
   }

   public boolean hasDisease(SecondaryStat dis, int skillid, int skilllv) {
      Iterator var4 = this.getDiseases().entrySet().iterator();

      Entry disease;
      do {
         if (!var4.hasNext()) {
            return false;
         }

         disease = (Entry)var4.next();
      } while(((SecondaryStat)disease.getKey()).getFlag() != dis.getFlag() || ((MapleDiseases)disease.getValue()).getMobskill().getSkillId() != skillid || ((MapleDiseases)disease.getValue()).getMobskill().getSkillLevel() != skilllv);

      return true;
   }

   public void disease(int skillId, int mobSkillLevel) {
      MobSkill ms = MobSkillFactory.getMobSkill(skillId, mobSkillLevel);
      SecondaryStat disease = SecondaryStat.getBySkill(skillId);
      if (disease != null) {
         this.giveDebuff(disease, ms);
      }

   }

   public Integer getDebuffValue(SecondaryStat stat) {
      Iterator var2 = this.getDiseases().entrySet().iterator();

      Entry disease;
      do {
         if (!var2.hasNext()) {
            return -1;
         }

         disease = (Entry)var2.next();
      } while(disease.getKey() != stat);

      return ((MapleDiseases)disease.getValue()).getValue();
   }

   public void giveDebuff(SecondaryStat disease, MobSkill skill) {
      Map<SecondaryStat, Pair<Integer, Integer>> diseases = new HashMap();
      diseases.put(disease, new Pair(skill.getX(), (int)skill.getDuration()));
      this.giveDebuff((Map)diseases, skill);
   }

   public void giveDebuff(Map<SecondaryStat, Pair<Integer, Integer>> diseases, MobSkill skill) {
      if (this.map != null && skill != null) {
         Iterator<Entry<SecondaryStat, Pair<Integer, Integer>>> diseasez = diseases.entrySet().iterator();
         if (this.getBuffedValue(3211011)) {
            return;
         }

         boolean noshell = false;
         MapleDiseases md = null;

         while(true) {
            while(diseasez.hasNext()) {
               Entry<SecondaryStat, Pair<Integer, Integer>> disease = (Entry)diseasez.next();
               md = new MapleDiseases((Integer)((Pair)disease.getValue()).left, (Integer)((Pair)disease.getValue()).right, System.currentTimeMillis());
               switch((SecondaryStat)disease.getKey()) {
               case Stigma:
               case StopPortion:
                  noshell = true;
               }

               if (this.hasDisease((SecondaryStat)disease.getKey())) {
                  diseasez.remove();
               } else if (this.getBuffedValue(2211012) && this.antiMagicShell > 0 && !noshell) {
                  if (this.antiMagicShell == 1) {
                     diseasez.remove();
                     this.addCooldown(2211012, System.currentTimeMillis(), (long)SkillFactory.getSkill(2211012).getEffect(this.getSkillLevel(2211012)).getCooldown(this));
                     this.client.getSession().writeAndFlush(CField.skillCooldown(2211012, SkillFactory.getSkill(2211012).getEffect(this.getSkillLevel(2211012)).getCooldown(this)));
                     this.cancelEffect(this.getBuffedEffect(2211012));
                     SkillFactory.getSkill(2211012).getEffect(this.getSkillLevel(2211012)).applyTo(this, false);
                  } else if (this.antiMagicShell >= 10) {
                     diseasez.remove();
                  } else {
                     md.setMobskill(skill);
                     this.getDiseases().put((SecondaryStat)disease.getKey(), md);
                  }
               } else if (this.getBuffedValue(400001050) && this.getSkillCustomValue0(400001050) == 400001054L && !noshell) {
                  diseases.remove(disease.getKey());
                  SecondaryStatEffect effect6 = SkillFactory.getSkill(400001050).getEffect(this.getSkillLevel(400001050));
                  this.removeSkillCustomInfo(400001050);
                  long duration = this.getBuffLimit(400001050);
                  effect6.applyTo(this, (int)duration);
               } else {
                  HashMap statups;
                  if (this.antiMagicShell > 0 && this.getBuffedEffect(SecondaryStat.AntiMagicShell) != null && !noshell) {
                     diseasez.remove();
                     --this.antiMagicShell;
                     if (this.antiMagicShell == 0) {
                        this.cancelEffectFromBuffStat(SecondaryStat.AntiMagicShell);
                     } else {
                        statups = new HashMap();
                        statups.put(SecondaryStat.AntiMagicShell, new Pair(Integer.valueOf(this.antiMagicShell), (int)this.getBuffLimit(this.getBuffSource(SecondaryStat.AntiMagicShell))));
                        this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, this.getBuffedEffect(SecondaryStat.AntiMagicShell), this));
                     }
                  } else if (this.getSpiritGuard() > 0 && !noshell) {
                     diseasez.remove();
                     this.setSpiritGuard(this.getSpiritGuard() - 1);
                     if (this.getSpiritGuard() == 0) {
                        this.cancelEffectFromBuffStat(SecondaryStat.SpiritGuard);
                     } else {
                        statups = new HashMap();
                        statups.put(SecondaryStat.SpiritGuard, new Pair(this.getSpiritGuard(), (int)this.getBuffLimit(this.getBuffSource(SecondaryStat.SpiritGuard))));
                        this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, this.getBuffedEffect(SecondaryStat.SpiritGuard), this));
                     }
                  } else if (skill.getDuration() > 0L || disease.getKey() == SecondaryStat.Stigma) {
                     if (this.getBuffedValue(5220019) && !noshell) {
                        Iterator var7 = this.getSummons().iterator();

                        while(var7.hasNext()) {
                           MapleSummon sum = (MapleSummon)var7.next();
                           switch(sum.getSkill()) {
                           case 5201012:
                           case 5201013:
                           case 5201014:
                           case 5210015:
                           case 5210016:
                           case 5210017:
                           case 5210018:
                              if (sum.getDebuffshell() > 0) {
                                 diseasez.remove();
                                 sum.setDebuffshell(0);
                                 this.getMap().broadcastMessage(CField.SummonPacket.summonDebuff(this.getId(), sum.getObjectId()));
                                 return;
                              }
                           }
                        }
                     }

                     md.setMobskill(skill);
                     this.getDiseases().put((SecondaryStat)disease.getKey(), md);
                  }
               }
            }

            this.dropMessageGM(6, "Debuff" + diseases);
            if (!diseases.isEmpty()) {
               this.client.getSession().writeAndFlush(CWvsContext.BuffPacket.giveDisease(diseases, skill, this));
               this.map.broadcastMessage(this, CWvsContext.BuffPacket.giveForeignDeBuff(this, diseases), false);
               if (skill.getDuration() > 0L) {
                  SecondaryStatEffect.CancelDiseaseAction ca = new SecondaryStatEffect.CancelDiseaseAction(this, diseases);
                  md.setSchedule(server.Timer.BuffTimer.getInstance().schedule(() -> {
                     ca.run();
                  }, skill.getDuration()));
               } else {
                  this.client.getSession().writeAndFlush(CWvsContext.BuffPacket.cancelBuff(diseases, this));
                  this.map.broadcastMessage(this, CWvsContext.BuffPacket.cancelForeignBuff(this, diseases), false);
               }

               if (this.curseBound < 5 && this.getBuffedEffect(SecondaryStat.StopPortion) != null) {
                  ++this.curseBound;
                  Map<SecondaryStat, Pair<Integer, Integer>> statups2 = new HashMap();
                  statups2.put(SecondaryStat.StopPortion, new Pair(this.curseBound, (int)this.getBuffLimit(this.getBuffSource(SecondaryStat.StopPortion))));
                  this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups2, this.getBuffedEffect(SecondaryStat.StopPortion), this));
               }
            }
            break;
         }
      }

   }

   public void cancelDisease(SecondaryStat debuff) {
      if (this.diseases.containsKey(debuff)) {
         MobSkill msi = ((MapleDiseases)this.diseases.get(debuff)).getMobskill();
         Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
         statups.put(debuff, new Pair(msi.getX(), (int)msi.getDuration()));
         this.cancelDisease(statups, false);
      }

   }

   public void cancelDisease(Map<SecondaryStat, Pair<Integer, Integer>> statups, boolean autocancel) {
      Map<SecondaryStat, Pair<Integer, Integer>> cancelList = new HashMap();
      int skillid = 0;
      int skilllv = 0;
      Iterator var6;
      if (statups != null) {
         var6 = statups.entrySet().iterator();

         while(var6.hasNext()) {
            Entry<SecondaryStat, Pair<Integer, Integer>> statup = (Entry)var6.next();
            if (this.getDiseases().containsKey(statup.getKey())) {
               skillid = ((MapleDiseases)this.getDiseases().get(statup.getKey())).getMobskill().getSkillId();
               skilllv = ((MapleDiseases)this.getDiseases().get(statup.getKey())).getMobskill().getSkillLevel();
               if (((MapleDiseases)this.getDiseases().get(statup.getKey())).getSchedule() != null) {
                  ((MapleDiseases)this.getDiseases().get(statup.getKey())).getSchedule().cancel(true);
                  ((MapleDiseases)this.getDiseases().get(statup.getKey())).setSchedule((ScheduledFuture)null);
               }

               this.getDiseases().remove(statup.getKey());
               cancelList.put((SecondaryStat)statup.getKey(), (Pair)statup.getValue());
            }
         }
      }

      this.client.getSession().writeAndFlush(CWvsContext.BuffPacket.cancelBuff(cancelList, this));
      this.map.broadcastMessage(this, CWvsContext.BuffPacket.cancelForeignBuff(this, cancelList), false);
      if (autocancel) {
         MapleMonster mob;
         if (cancelList.containsKey(SecondaryStat.PapulCuss)) {
            var6 = this.getMap().getAllMonster().iterator();

            label216:
            while(true) {
               do {
                  do {
                     if (!var6.hasNext()) {
                        break label216;
                     }

                     mob = (MapleMonster)var6.next();
                  } while(mob == null);
               } while(mob.getId() != 8500021 && mob.getId() != 8500011 && mob.getId() != 8500001);

               if (mob.getCustomValue(2412) != null && mob.getBuff(MonsterStatus.MS_PopulatusTimer) != null) {
                  long duration = (long)(mob.getCustomTime(2412) - this.getSkillCustomTime(241));
                  if (duration > 30000L) {
                     duration = 30000L;
                  }

                  if (duration > 0L) {
                     MobSkill ms = MobSkillFactory.getMobSkill(241, mob.getHPPercent() >= 50 ? 2 : 1);
                     ms.setDuration(duration);
                     this.giveDebuff(mob.getHPPercent() >= 50 ? SecondaryStat.Seal : SecondaryStat.Stun, ms);
                  }
                  break;
               }
            }
         } else if (cancelList.containsKey(SecondaryStat.Contagion) || cancelList.containsKey(SecondaryStat.PapulBomb)) {
            int hit = 1;
            mob = null;
            Iterator var15 = this.getMap().getAllMonster().iterator();

            label192: {
               MapleMonster monster2;
               do {
                  do {
                     if (!var15.hasNext()) {
                        break label192;
                     }

                     monster2 = (MapleMonster)var15.next();
                  } while(monster2 == null);
               } while(monster2.getId() != 8880142 && monster2.getId() != 8880140 && monster2.getId() != 8880141 && monster2.getId() != 8880150 && monster2.getId() != 8880151 && monster2.getId() != 8880155 && monster2.getId() != 8500001 && monster2.getId() != 8500002 && monster2.getId() != 8500011 && monster2.getId() != 8500012 && monster2.getId() != 8500021 && monster2.getId() != 8500022);

               mob = monster2;
            }

            if (mob != null && skillid > 0 && skilllv > 0) {
               boolean solo = true;
               Iterator var17 = this.getParty().getMembers().iterator();

               MapleCharacter achr;
               MaplePartyCharacter chr1;
               while(var17.hasNext()) {
                  chr1 = (MaplePartyCharacter)var17.next();
                  achr = this.getClient().getChannelServer().getPlayerStorage().getCharacterById(chr1.getId());
                  if (achr != null && achr.getId() != this.getId() && this.getPosition().x - 150 <= achr.getPosition().x && this.getPosition().x + 150 >= achr.getPosition().x) {
                     ++hit;
                     solo = false;
                  }
               }

               if (solo) {
                  var17 = this.getMap().getLucidDream().iterator();

                  while(var17.hasNext()) {
                     Point p = (Point)var17.next();
                     if (p.x - 50 <= this.getPosition().x && p.x + 50 >= this.getPosition().x) {
                        ++hit;
                        break;
                     }
                  }
               }

               this.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(this, skillid == 238 ? 176 : skillid, skillid == 238 ? 38 : skilllv, 45, 0, 0, (byte)0, true, (Point)null, (String)null, (Item)null));
               this.getMap().broadcastMessage(this, CField.EffectPacket.showEffect(this, skillid == 238 ? 176 : skillid, skillid == 238 ? 38 : skilllv, 45, 0, 0, (byte)0, false, (Point)null, (String)null, (Item)null), false);
               var17 = this.getParty().getMembers().iterator();

               while(var17.hasNext()) {
                  chr1 = (MaplePartyCharacter)var17.next();
                  achr = this.getClient().getChannelServer().getPlayerStorage().getCharacterById(chr1.getId());
                  if (achr != null && this.getPosition().x - 150 <= achr.getPosition().x && this.getPosition().x + 150 >= achr.getPosition().x) {
                     int percent = cancelList.containsKey(SecondaryStat.Contagion) ? 180 : (cancelList.containsKey(SecondaryStat.PapulBomb) ? 80 : 0);
                     percent /= hit;
                     achr.getPercentDamage(mob, skillid, skilllv, percent, true);
                  }
               }
            }
         }
      }

      if (cancelList.containsKey(SecondaryStat.VampDeath)) {
         this.addHP(-this.getStat().getCurrentMaxHp());
      }

      if (cancelList.containsKey(SecondaryStat.Lapidification) && autocancel && skilllv == 999) {
         this.giveDebuff(SecondaryStat.Stun, MobSkillFactory.getMobSkill(123, 57));
      }

   }

   public void dispelDebuff(Entry<SecondaryStat, MapleDiseases> d) {
      if (this.hasDisease((SecondaryStat)d.getKey())) {
         this.cancelDisease((SecondaryStat)d.getKey());
      }

   }

   public void dispelDebuffs() {
      HashMap<SecondaryStat, Pair<Integer, Integer>> statupz = new HashMap();
      Iterator var2 = this.getDiseases().entrySet().iterator();

      while(var2.hasNext()) {
         Entry<SecondaryStat, MapleDiseases> d = (Entry)var2.next();
         this.dispelDebuff(d);
         statupz.put((SecondaryStat)d.getKey(), new Pair(((MapleDiseases)d.getValue()).getValue(), ((MapleDiseases)d.getValue()).getDuration()));
      }

      this.client.getSession().writeAndFlush(CWvsContext.BuffPacket.cancelBuff(statupz, this));
      this.map.broadcastMessage(this, CWvsContext.BuffPacket.cancelForeignBuff(this, statupz), false);
      this.client.getSession().writeAndFlush(CWvsContext.enableActions(this));
   }

   public void dispelDebuffs(MapleCharacter owner, int skillid) {
      HashMap<SecondaryStat, Pair<Integer, Integer>> statupz = new HashMap();
      Iterator var4 = this.getDiseases().entrySet().iterator();

      while(var4.hasNext()) {
         Entry<SecondaryStat, MapleDiseases> d = (Entry)var4.next();
         if (skillid == 2001556) {
            switch((SecondaryStat)d.getKey()) {
            case Seal:
            case Curse:
            case Poison:
            case Darkness:
            case Weakness:
               this.dispelDebuff(d);
               statupz.put((SecondaryStat)d.getKey(), new Pair(((MapleDiseases)d.getValue()).getValue(), ((MapleDiseases)d.getValue()).getDuration()));
            }
         } else {
            this.dispelDebuff(d);
            statupz.put((SecondaryStat)d.getKey(), new Pair(((MapleDiseases)d.getValue()).getValue(), ((MapleDiseases)d.getValue()).getDuration()));
         }
      }

      if (skillid == 2311001 && statupz.size() > 0) {
         SecondaryStatEffect effect = SkillFactory.getSkill(2311001).getEffect(owner.getSkillLevel(2311001));
         if (owner.skillisCooling(2311001)) {
            owner.changeCooldown(2311001, -(effect.getY() * 1000));
         }

         if (owner.skillisCooling(2311012)) {
            owner.changeCooldown(2311012, -effect.getDuration());
         }
      }

      if (!statupz.isEmpty()) {
         this.client.getSession().writeAndFlush(CWvsContext.BuffPacket.cancelBuff(statupz, this));
         this.map.broadcastMessage(this, CWvsContext.BuffPacket.cancelForeignBuff(this, statupz), false);
         this.client.getSession().writeAndFlush(CWvsContext.enableActions(this));
      }

   }

   public void cancelAllDebuffs() {
      this.getDiseases().clear();
   }

   public void setLevel(short level) {
      this.level = (short)(level - 1);
   }

   public void sendNote(String to, String msg, int type, int senderid) {
      this.sendNote(to, msg, 0, type, senderid);
   }

   public void sendNote(String to, String msg, int fame, int type, int senderid) {
      MapleCharacterUtil.sendNote(to, this.getName(), msg, fame, type, senderid);
   }

   public void sendNote(String to, String from, String msg, int fame, int type, int senderid) {
      MapleCharacterUtil.sendNote(to, from, msg, fame, type, senderid);
   }

   public void showNote() {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM notes WHERE `to`= ? AND `type` = 6", 1005, 1008);
         ps.setString(1, this.getName());
         rs = ps.executeQuery();
         rs.last();
         int count = rs.getRow();
         rs.first();
         this.client.getSession().writeAndFlush(CSPacket.showNotes(rs, count));
         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var13) {
         System.err.println("Unable to show note" + var13);
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
         }

      }

      this.showsendNote();
   }

   public void showsendNote() {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM `notes` WHERE `from`= ? AND `type` = 7", 1005, 1008);
         ps.setString(1, this.getName());
         rs = ps.executeQuery();
         rs.last();
         int count = rs.getRow();
         rs.first();
         this.client.getSession().writeAndFlush(CSPacket.showsendNotes(this, rs, count));
         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var13) {
         System.err.println("Unable to show note" + var13);
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
         }

      }

   }

   public void deleteNote(int id, int fame) {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT gift FROM notes WHERE `id`=?");
         ps.setInt(1, id);
         rs = ps.executeQuery();
         if (rs.next() && rs.getInt("gift") == fame && fame > 0) {
            this.updateSingleStat(MapleStat.FAME, (long)this.getFame());
            this.client.getSession().writeAndFlush(CWvsContext.InfoPacket.getShowFameGain(fame));
         }

         rs.close();
         ps.close();
         ps = con.prepareStatement("DELETE FROM notes WHERE `id`=?");
         ps.setInt(1, id);
         ps.execute();
         ps.close();
         con.close();
      } catch (SQLException var15) {
         System.err.println("Unable to delete note" + var15);
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
         }

      }

   }

   public int getMulungEnergy() {
      return this.mulung_energy;
   }

   public final short getCombo() {
      return this.combo;
   }

   public void setCombo(short combo) {
      this.combo = combo;
   }

   public final long getLastCombo() {
      return this.lastCombo;
   }

   public void setLastCombo(long combo) {
      this.lastCombo = combo;
   }

   public final boolean getUseTruthDoor() {
      return this.useTruthDoor;
   }

   public void setUseTruthDoor(boolean used) {
      this.useTruthDoor = used;
   }

   public final long getKeyDownSkill_Time() {
      return this.keydown_skill;
   }

   public void setKeyDownSkill_Time(long keydown_skill) {
      this.keydown_skill = keydown_skill;
   }

   public void setChalkboard(String text) {
      this.chalktext = text;
      if (this.map != null) {
         this.map.broadcastMessage(CSPacket.useChalkboard(this.getId(), text));
      }

   }

   public String getChalkboard() {
      return this.chalktext;
   }

   public MapleMount getMount() {
      return this.mount;
   }

   public int[] getWishlist() {
      return this.wishlist;
   }

   public void clearWishlist() {
      for(int i = 0; i < 12; ++i) {
         this.wishlist[i] = 0;
      }

      this.changed_wishlist = true;
   }

   public int getWishlistSize() {
      int ret = 0;

      for(int i = 0; i < 12; ++i) {
         if (this.wishlist[i] > 0) {
            ++ret;
         }
      }

      return ret;
   }

   public void setWishlist(int[] wl) {
      this.wishlist = wl;
      this.changed_wishlist = true;
   }

   public int[] getRocks() {
      return this.rocks;
   }

   public int getRockSize() {
      int ret = 0;

      for(int i = 0; i < 10; ++i) {
         if (this.rocks[i] != 999999999) {
            ++ret;
         }
      }

      return ret;
   }

   public void deleteFromRocks(int map) {
      for(int i = 0; i < 10; ++i) {
         if (this.rocks[i] == map) {
            this.rocks[i] = 999999999;
            this.changed_trocklocations = true;
            break;
         }
      }

   }

   public void addRockMap() {
      if (this.getRockSize() < 10) {
         this.rocks[this.getRockSize()] = this.getMapId();
         this.changed_trocklocations = true;
      }
   }

   public boolean isRockMap(int id) {
      for(int i = 0; i < 10; ++i) {
         if (this.rocks[i] == id) {
            return true;
         }
      }

      return false;
   }

   public int[] getRegRocks() {
      return this.regrocks;
   }

   public int getRegRockSize() {
      int ret = 0;

      for(int i = 0; i < 5; ++i) {
         if (this.regrocks[i] != 999999999) {
            ++ret;
         }
      }

      return ret;
   }

   public void deleteFromRegRocks(int map) {
      for(int i = 0; i < 5; ++i) {
         if (this.regrocks[i] == map) {
            this.regrocks[i] = 999999999;
            this.changed_regrocklocations = true;
            break;
         }
      }

   }

   public void addRegRockMap() {
      if (this.getRegRockSize() < 5) {
         this.regrocks[this.getRegRockSize()] = this.getMapId();
         this.changed_regrocklocations = true;
      }
   }

   public boolean isRegRockMap(int id) {
      for(int i = 0; i < 5; ++i) {
         if (this.regrocks[i] == id) {
            return true;
         }
      }

      return false;
   }

   public int[] getHyperRocks() {
      return this.hyperrocks;
   }

   public int getHyperRockSize() {
      int ret = 0;

      for(int i = 0; i < 13; ++i) {
         if (this.hyperrocks[i] != 999999999) {
            ++ret;
         }
      }

      return ret;
   }

   public void deleteFromHyperRocks(int map) {
      for(int i = 0; i < 13; ++i) {
         if (this.hyperrocks[i] == map) {
            this.hyperrocks[i] = 999999999;
            this.changed_hyperrocklocations = true;
            break;
         }
      }

   }

   public void addHyperRockMap() {
      if (this.getRegRockSize() < 13) {
         this.hyperrocks[this.getHyperRockSize()] = this.getMapId();
         this.changed_hyperrocklocations = true;
      }
   }

   public boolean isHyperRockMap(int id) {
      for(int i = 0; i < 13; ++i) {
         if (this.hyperrocks[i] == id) {
            return true;
         }
      }

      return false;
   }

   public List<LifeMovementFragment> getLastRes() {
      return this.lastres;
   }

   public void setLastRes(List<LifeMovementFragment> lastres) {
      this.lastres = lastres;
   }

   public void dropMessageGM(int type, String message) {
      if (this.isGM()) {
         this.dropMessage(type, message);
      }

   }

   public void dropMessage(int type, String message) {
      if (type == -1) {
         this.client.getSession().writeAndFlush(CWvsContext.getTopMsg(message));
      } else if (type == -2) {
         this.client.getSession().writeAndFlush(PlayerShopPacket.shopChat(this, this.name, this.id, message, 0));
      } else if (type == -3) {
         this.client.getSession().writeAndFlush(CField.getChatText(this, message, this.isSuperGM(), 0, (Item)null));
      } else if (type == -4) {
         this.client.getSession().writeAndFlush(CField.getChatText(this, message, this.isSuperGM(), 1, (Item)null));
      } else if (type == -5) {
         this.client.getSession().writeAndFlush(CField.getGameMessage(6, message));
      } else if (type == -6) {
         this.client.getSession().writeAndFlush(CField.getGameMessage(11, message));
      } else if (type == -7) {
         this.client.getSession().writeAndFlush(CWvsContext.getMidMsg(message, 0));
      } else if (type == -8) {
         this.client.getSession().writeAndFlush(CField.getGameMessage(8, message));
      } else {
         this.client.getSession().writeAndFlush(CWvsContext.serverNotice(type, this.name, message));
      }

   }

   public IMaplePlayerShop getPlayerShop() {
      return this.playerShop;
   }

   public void setPlayerShop(IMaplePlayerShop playerShop) {
      this.playerShop = playerShop;
   }

   public int getConversation() {
      return this.inst.get();
   }

   public void setConversation(int inst) {
      this.inst.set(inst);
   }

   public int getDirection() {
      return this.insd.get();
   }

   public void setDirection(int inst) {
      this.insd.set(inst);
   }

   public void addCP(int ammount) {
      this.totalCP += (short)ammount;
      this.availableCP += (short)ammount;
   }

   public void useCP(int ammount) {
      this.availableCP -= (short)ammount;
   }

   public int getAvailableCP() {
      return this.availableCP;
   }

   public int getTotalCP() {
      return this.totalCP;
   }

   public void resetCP() {
      this.totalCP = 0;
      this.availableCP = 0;
   }

   public boolean getCanTalk() {
      return this.canTalk;
   }

   public void canTalk(boolean talk) {
      this.canTalk = talk;
   }

   public double getEXPMod() {
      return this.stats.expMod;
   }

   public int getDropMod() {
      return this.stats.dropMod;
   }

   public int getCashMod() {
      return this.stats.cashMod;
   }

   public void setPoints(int p) {
      this.points = p;
   }

   public int getPoints() {
      return this.points;
   }

   public void setVPoints(int p) {
      this.vpoints = p;
   }

   public int getVPoints() {
      return this.vpoints;
   }

   public void gainVPoints(int vpoints) {
      this.vpoints += vpoints;
   }

   public CashShop getCashInventory() {
      return this.cs;
   }

   public void removeItem(int id, int quantity) {
      MapleInventoryManipulator.removeById(this.client, GameConstants.getInventoryType(id), id, -quantity, true, false);
      this.client.getSession().writeAndFlush(CWvsContext.InfoPacket.getShowItemGain(id, (short)quantity, true));
   }

   public void removeAll(int id) {
      this.removeAll(id, true);
   }

   public void removeAll(int id, boolean show) {
      MapleInventoryType type = GameConstants.getInventoryType(id);
      int possessed = this.getInventory(type).countById(id);
      if (possessed > 0) {
         MapleInventoryManipulator.removeById(this.getClient(), type, id, possessed, true, false);
         if (show) {
            this.getClient().getSession().writeAndFlush(CWvsContext.InfoPacket.getShowItemGain(id, (short)(-possessed), true));
         }
      }

      if (type == MapleInventoryType.EQUIP) {
         type = MapleInventoryType.EQUIPPED;
         possessed = this.getInventory(type).countById(id);
         if (possessed > 0) {
            MapleInventoryManipulator.removeById(this.getClient(), type, id, possessed, true, false);
            this.getClient().getSession().writeAndFlush(CWvsContext.InfoPacket.getShowItemGain(id, (short)(-possessed), true));
         }
      }

   }

   public Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> getRings(boolean equip) {
      MapleInventory iv = this.getInventory(MapleInventoryType.EQUIPPED);
      List<Item> equipped = iv.newList();
      Collections.sort(equipped);
      List<MapleRing> crings = new ArrayList();
      List<MapleRing> frings = new ArrayList();
      List<MapleRing> mrings = new ArrayList();
      Iterator var7 = equipped.iterator();

      while(true) {
         while(true) {
            Equip item;
            MapleRing ring;
            do {
               do {
                  Item ite;
                  if (!var7.hasNext()) {
                     if (equip) {
                        iv = this.getInventory(MapleInventoryType.EQUIP);
                        var7 = iv.list().iterator();

                        while(var7.hasNext()) {
                           ite = (Item)var7.next();
                           item = (Equip)ite;
                           if (item.getRing() != null) {
                              ring = item.getRing();
                              ring.setEquipped(false);
                              if (GameConstants.isFriendshipRing(item.getItemId())) {
                                 frings.add(ring);
                              } else if (GameConstants.isCrushRing(item.getItemId())) {
                                 crings.add(ring);
                              } else if (GameConstants.isMarriageRing(item.getItemId()) && this.getMarriageId() > 0) {
                                 mrings.add(ring);
                              }
                           }
                        }
                     }

                     Collections.sort(frings, new MapleRing.RingComparator());
                     Collections.sort(crings, new MapleRing.RingComparator());
                     Collections.sort(mrings, new MapleRing.RingComparator());
                     return new Triple(crings, frings, mrings);
                  }

                  ite = (Item)var7.next();
                  item = (Equip)ite;
               } while(item.getRing() == null);

               ring = item.getRing();
               ring.setEquipped(true);
            } while(!GameConstants.isEffectRing(item.getItemId()));

            if (equip) {
               if (GameConstants.isCrushRing(item.getItemId())) {
                  crings.add(ring);
               } else if (GameConstants.isFriendshipRing(item.getItemId())) {
                  frings.add(ring);
               } else if (GameConstants.isMarriageRing(item.getItemId())) {
                  mrings.add(ring);
               }
            } else if (crings.size() == 0 && GameConstants.isCrushRing(item.getItemId())) {
               crings.add(ring);
            } else if (frings.size() == 0 && GameConstants.isFriendshipRing(item.getItemId())) {
               frings.add(ring);
            } else if (mrings.size() == 0 && GameConstants.isMarriageRing(item.getItemId()) && this.getMarriageId() > 0) {
               mrings.add(ring);
            }
         }
      }
   }

   public int getFH() {
      MapleFoothold fh = this.getMap().getFootholds().findBelow(this.getTruePosition());
      return fh != null ? fh.getId() : 0;
   }

   public void startFairySchedule(boolean exp) {
      this.startFairySchedule(exp, false);
   }

   public void startFairySchedule(boolean exp, boolean equipped) {
      this.cancelFairySchedule(exp || this.stats.equippedFairy == 0);
      if (this.fairyExp <= 0) {
         this.fairyExp = (byte)this.stats.equippedFairy;
      }

      if (equipped && this.fairyExp < this.stats.equippedFairy * 3 && this.stats.equippedFairy > 0) {
         int var10002 = this.fairyExp + this.stats.equippedFairy;
         this.dropMessage(5, "정령의 펜던트를 착용한지 1시간이 지나 " + var10002 + "%의 추가 경험치를 획득합니다.");
      }

      this.lastFairyTime = System.currentTimeMillis();
   }

   public final boolean canFairy(long now) {
      return this.lastFairyTime > 0L && this.lastFairyTime + 3600000L < now;
   }

   public final boolean canHP(long now) {
      if (this.lastHPTime + 5000L < now) {
         this.lastHPTime = now;
         return true;
      } else {
         return false;
      }
   }

   public final boolean canMP(long now) {
      if (this.lastMPTime + 5000L < now) {
         this.lastMPTime = now;
         return true;
      } else {
         return false;
      }
   }

   public final boolean canHPRecover(long now) {
      if (this.stats.hpRecoverTime > 0 && this.lastHPTime + (long)this.stats.hpRecoverTime < now) {
         this.lastHPTime = now;
         return true;
      } else {
         return false;
      }
   }

   public final boolean canMPRecover(long now) {
      if (this.stats.mpRecoverTime > 0 && this.lastMPTime + (long)this.stats.mpRecoverTime < now) {
         this.lastMPTime = now;
         return true;
      } else {
         return false;
      }
   }

   public void cancelFairySchedule(boolean exp) {
      this.lastFairyTime = 0L;
      if (exp) {
         this.fairyExp = 0;
      }

   }

   public void doFairy() {
      if (this.fairyExp < this.stats.equippedFairy * 3 && this.stats.equippedFairy > 0) {
         this.fairyExp += (byte)this.stats.equippedFairy;
         this.dropMessage(5, "정령의 펜던트를 통해 " + this.fairyExp + "%의 추가경험치를 획득합니다.");
      }

      ((MapleTrait)this.traits.get(MapleTrait.MapleTraitType.will)).addExp(5, this);
      this.startFairySchedule(false, true);
   }

   public byte getFairyExp() {
      return this.fairyExp;
   }

   public int getTeam() {
      return this.coconutteam;
   }

   public void setTeam(int v) {
      this.coconutteam = v;
   }

   public void clearLinkMid() {
      this.linkMobs.clear();
      this.cancelEffectFromBuffStat(SecondaryStat.ArcaneAim);
   }

   public int getFirstLinkMid() {
      Iterator<Integer> iterator = this.linkMobs.keySet().iterator();
      if (iterator.hasNext()) {
         Integer lm = (Integer)iterator.next();
         return lm;
      } else {
         return 0;
      }
   }

   public Map<Integer, Integer> getAllLinkMid() {
      return this.linkMobs;
   }

   public void setLinkMid(int lm, int x) {
      this.linkMobs.put(lm, x);
   }

   public int getDamageIncrease(int lm) {
      return this.linkMobs.containsKey(lm) ? (Integer)this.linkMobs.get(lm) : 0;
   }

   public void setDragon(MapleDragon d) {
      this.dragon = d;
   }

   public MapleExtractor getExtractor() {
      return this.extractor;
   }

   public void setExtractor(MapleExtractor me) {
      this.removeExtractor();
      this.extractor = me;
   }

   public void removeExtractor() {
      if (this.extractor != null) {
         this.map.broadcastMessage(CField.removeExtractor(this.id));
         this.map.removeMapObject(this.extractor);
         this.extractor = null;
      }

   }

   public void setBlackMageWB(int v) {
      this.blackmagewb = v;
   }

   public int getBlackMageWB() {
      return this.blackmagewb;
   }

   public void giveBlackMageBuff() {
      this.getClient().send(CField.getSelectPower(5, 39));
      this.setSkillCustomInfo(80002625, 1L, 0L);
      SkillFactory.getSkill(80002625).getEffect(1).applyTo(this);
   }

   public void resetStatsN(int str, int dex, int int_, int luk) {
      Map<MapleStat, Long> stat = new EnumMap(MapleStat.class);
      this.stats.str = (short)str;
      this.stats.dex = (short)dex;
      this.stats.int_ = (short)int_;
      this.stats.luk = (short)luk;
      this.stats.recalcLocalStats(this);
      stat.put(MapleStat.STR, (long)str);
      stat.put(MapleStat.DEX, (long)dex);
      stat.put(MapleStat.INT, (long)int_);
      stat.put(MapleStat.LUK, (long)luk);
      this.client.getSession().writeAndFlush(CWvsContext.updatePlayerStats(stat, false, this));
   }

   public void resetStats(int str, int dex, int int_, int luk) {
      Map<MapleStat, Long> stat = new EnumMap(MapleStat.class);
      this.stats.recalcLocalStats(this);
      this.stats.str = 4;
      this.stats.dex = 4;
      this.stats.int_ = 4;
      this.stats.luk = 4;
      stat.put(MapleStat.STR, (long)str);
      stat.put(MapleStat.DEX, (long)dex);
      stat.put(MapleStat.INT, (long)int_);
      stat.put(MapleStat.LUK, (long)luk);
      stat.put(MapleStat.AVAILABLEAP, (long)this.getLevel() * 5L + 18L);
      this.setRemainingAp((short)(this.getLevel() * 5 + 18));
      this.client.getSession().writeAndFlush(CWvsContext.updatePlayerStats(stat, false, this));
   }

   public void resetStatDonation(int tf, int tft) {
      Map<MapleStat, Long> stat = new EnumMap(MapleStat.class);
      int total = this.stats.getStr() + this.stats.getDex() + this.stats.getLuk() + this.stats.getInt() + this.getRemainingAp();
      int tstat = tft == 1 ? tf * 200 : (tft == 2 ? tf * 100 : 4);
      if (tft == 2 && tf == 6) {
         tstat += 100;
      }

      total -= tstat;
      this.stats.str = (short)tstat;
      total -= tstat;
      this.stats.dex = (short)tstat;
      total -= tstat;
      this.stats.int_ = (short)tstat;
      total -= tstat;
      this.stats.luk = (short)tstat;
      this.setRemainingAp((short)total);
      this.stats.recalcLocalStats(this);
      stat.put(MapleStat.STR, (long)tstat);
      stat.put(MapleStat.DEX, (long)tstat);
      stat.put(MapleStat.INT, (long)tstat);
      stat.put(MapleStat.LUK, (long)tstat);
      stat.put(MapleStat.AVAILABLEAP, (long)total);
      this.client.getSession().writeAndFlush(CWvsContext.updatePlayerStats(stat, false, this));
   }

   public void resetStatsDV() {
      Map<MapleStat, Long> stat = new EnumMap(MapleStat.class);
      int apss = (this.getLevel() - 10) * 5 * 15;
      int total = (this.getLevel() - 10) * 5 + this.getRemainingAp();
      this.stats.setMaxHp(this.getStat().getMaxHp() - (long)apss, this);
      this.setRemainingAp((short)total);
      this.stats.recalcLocalStats(this);
      stat.put(MapleStat.MAXHP, this.getStat().getMaxHp());
      stat.put(MapleStat.AVAILABLEAP, (long)total);
      this.client.getSession().writeAndFlush(CWvsContext.updatePlayerStats(stat, false, this));
   }

   public byte getSubcategory() {
      if (this.job >= 430 && this.job <= 434) {
         return 1;
      } else if (GameConstants.isCannon(this.job)) {
         return 2;
      } else {
         return this.job != 0 && this.job != 400 ? 0 : this.subcategory;
      }
   }

   public void setSubcategory(int z) {
      this.subcategory = (byte)z;
   }

   public int itemQuantity(int itemid) {
      return this.getInventory(GameConstants.getInventoryType(itemid)).countById(itemid);
   }

   public long getNextConsume() {
      return this.nextConsume;
   }

   public void setNextConsume(long nc) {
      this.nextConsume = nc;
   }

   public int getRank() {
      return this.rank;
   }

   public int getRankMove() {
      return this.rankMove;
   }

   public int getJobRank() {
      return this.jobRank;
   }

   public int getJobRankMove() {
      return this.jobRankMove;
   }

   public void changeChannelMap(int channel, int map) {
      ChannelServer toch = ChannelServer.getInstance(channel);
      if (channel != this.client.getChannel() && toch != null && !toch.isShutdown()) {
         this.changeRemoval();
         ChannelServer ch = ChannelServer.getInstance(this.client.getChannel());
         if (this.getMessenger() != null) {
            World.Messenger.silentLeaveMessenger(this.getMessenger().getId(), new MapleMessengerCharacter(this));
         }

         PlayerBuffStorage.addBuffsToStorage(this.getId(), this.getAllBuffs());
         PlayerBuffStorage.addCooldownsToStorage(this.getId(), this.getCooldowns());
         this.getMap().removePlayer(this);
         this.map = toch.getMapFactory().getMap(map);
         World.ChannelChange_Data(new CharacterTransfer(this), this.getId(), channel);
         ch.removePlayer(this);
         this.client.updateLoginState(3, this.client.getSessionIPAddress());
         String s = this.client.getSessionIPAddress();
         LoginServer.addIPAuth(s.substring(s.indexOf(47) + 1, s.length()));
         this.client.getSession().writeAndFlush(CField.getChannelChange(this.client, Integer.parseInt(toch.getIP().split(":")[1])));
         this.saveToDB(true, false);
         this.client.setPlayer((MapleCharacter)null);
         if (OneCardGame.oneCardMatchingQueue.contains(this)) {
            OneCardGame.oneCardMatchingQueue.remove(this);
         }

         if (BattleReverse.BattleReverseMatchingQueue.contains(this)) {
            BattleReverse.BattleReverseMatchingQueue.remove(this);
         }

      }
   }

   public void changeChannel(int channel) {
      ChannelServer toch = ChannelServer.getInstance(channel);
      if (channel != this.client.getChannel() && toch != null && !toch.isShutdown()) {
         this.changeRemoval();
         ChannelServer ch = ChannelServer.getInstance(this.client.getChannel());
         if (this.getMessenger() != null) {
            World.Messenger.silentLeaveMessenger(this.getMessenger().getId(), new MapleMessengerCharacter(this));
         }

         if (this.getBuffedValue(SecondaryStat.EventSpecialSkill) != null) {
            this.cancelEffect(this.getBuffedEffect(SecondaryStat.EventSpecialSkill));
         }

         PlayerBuffStorage.addBuffsToStorage(this.getId(), this.getAllBuffs());
         PlayerBuffStorage.addCooldownsToStorage(this.getId(), this.getCooldowns());
         World.ChannelChange_Data(new CharacterTransfer(this), this.getId(), channel);
         ch.removePlayer(this);
         this.client.setChannel(channel);
         this.client.updateLoginState(3, this.client.getSessionIPAddress());
         String s = this.client.getSessionIPAddress();
         LoginServer.addIPAuth(s.substring(s.indexOf(47) + 1, s.length()));
         this.client.getSession().writeAndFlush(CField.getChannelChange(this.client, Integer.parseInt(toch.getIP().split(":")[1])));
         this.saveToDB(true, false);
         this.getMap().removePlayer(this);
         this.client.setPlayer((MapleCharacter)null);
         if (OneCardGame.oneCardMatchingQueue.contains(this)) {
            OneCardGame.oneCardMatchingQueue.remove(this);
         }

         if (BattleReverse.BattleReverseMatchingQueue.contains(this)) {
            BattleReverse.BattleReverseMatchingQueue.remove(this);
         }

      }
   }

   public void expandInventory(byte type, int amount) {
      MapleInventory inv = this.getInventory(MapleInventoryType.getByType(type));
      inv.addSlot((short)((byte)amount));
      this.client.getSession().writeAndFlush(CWvsContext.InventoryPacket.getSlotUpdate(type, (byte)inv.getSlotLimit()));
   }

   public boolean allowedToTarget(MapleCharacter other) {
      return other != null && (!other.isHidden() || this.getGMLevel() >= other.getGMLevel());
   }

   public int getFollowId() {
      return this.followid;
   }

   public void setFollowId(int fi) {
      this.followid = fi;
      if (fi == 0) {
         this.followinitiator = false;
         this.followon = false;
      }

   }

   public void setFollowInitiator(boolean fi) {
      this.followinitiator = fi;
   }

   public void setFollowOn(boolean fi) {
      this.followon = fi;
   }

   public boolean isFollowOn() {
      return this.followon;
   }

   public boolean isFollowInitiator() {
      return this.followinitiator;
   }

   public void checkFollow() {
      if (this.followid > 0) {
         if (this.followon) {
            this.map.broadcastMessage(CField.followEffect(this.id, 0, (Point)null));
            this.map.broadcastMessage(CField.followEffect(this.followid, 0, (Point)null));
         }

         MapleCharacter target = this.map.getCharacter(this.followid);
         this.client.getSession().writeAndFlush(CField.getGameMessage(11, "따라가기가 해제되었습니다."));
         if (target != null) {
            target.setFollowId(0);
            target.getClient().getSession().writeAndFlush(CField.getGameMessage(11, "따라가기가 해제되었습니다."));
            this.setFollowId(0);
         }

      }
   }

   public int getMarriageId() {
      return this.marriageId;
   }

   public void setMarriageId(int mi) {
      this.marriageId = mi;
   }

   public int getMarriageItemId() {
      return this.marriageItemId;
   }

   public void setMarriageItemId(int mi) {
      this.marriageItemId = mi;
   }

   public boolean isStaff() {
      return this.gmLevel >= ServerConstants.PlayerGMRank.INTERN.getLevel();
   }

   public boolean isDonator() {
      return this.gmLevel >= ServerConstants.PlayerGMRank.DONATOR.getLevel();
   }

   public boolean startPartyQuest(int questid) {
      boolean ret = false;
      MapleQuest q = MapleQuest.getInstance(questid);
      if (q != null && q.isPartyQuest()) {
         if (!this.quests.containsKey(q) || !this.questinfo.containsKey(questid)) {
            MapleQuestStatus status = this.getQuestNAdd(q);
            status.setStatus((byte)1);
            this.updateQuest(status);
            switch(questid) {
            case 1204:
               this.updateInfoQuest(questid, "min=0;sec=0;date=0000-00-00;have0=0;have1=0;have2=0;have3=0;rank=F;try=0;cmp=0;CR=0;VR=0");
               break;
            case 1206:
               this.updateInfoQuest(questid, "min=0;sec=0;date=0000-00-00;have0=0;have1=0;rank=F;try=0;cmp=0;CR=0;VR=0");
               break;
            case 1300:
            case 1301:
            case 1302:
               this.updateInfoQuest(questid, "min=0;sec=0;date=0000-00-00;have=0;rank=F;try=0;cmp=0;CR=0;VR=0;gvup=0;vic=0;lose=0;draw=0");
               break;
            case 1303:
               this.updateInfoQuest(questid, "min=0;sec=0;date=0000-00-00;have=0;have1=0;rank=F;try=0;cmp=0;CR=0;VR=0;vic=0;lose=0");
               break;
            default:
               this.updateInfoQuest(questid, "min=0;sec=0;date=0000-00-00;have=0;rank=F;try=0;cmp=0;CR=0;VR=0");
            }

            ret = true;
         }

         return ret;
      } else {
         return false;
      }
   }

   public String getOneInfo(int questid, String key) {
      if (this.questinfo.containsKey(questid) && key != null && MapleQuest.getInstance(questid) != null && MapleQuest.getInstance(questid).isPartyQuest()) {
         String[] split3 = ((String)this.questinfo.get(questid)).split(";");
         String[] var5 = split3;
         int var6 = split3.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            String x = var5[var7];
            String[] split2 = x.split("=");
            if (split2.length == 2 && split2[0].equals(key)) {
               return split2[1];
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public void updateOneInfo(int questid, String key, String value) {
      if (this.questinfo.containsKey(questid) && key != null && value != null && MapleQuest.getInstance(questid) != null && MapleQuest.getInstance(questid).isPartyQuest()) {
         String[] split = ((String)this.questinfo.get(questid)).split(";");
         boolean changed = false;
         StringBuilder newQuest = new StringBuilder();
         String[] var7 = split;
         int var8 = split.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            String x = var7[var9];
            String[] split2 = x.split("=");
            if (split2.length == 2) {
               if (split2[0].equals(key)) {
                  newQuest.append(key).append("=").append(value);
               } else {
                  newQuest.append(x);
               }

               newQuest.append(";");
               changed = true;
            }
         }

         this.updateInfoQuest(questid, changed ? newQuest.toString().substring(0, newQuest.toString().length() - 1) : newQuest.toString());
      }
   }

   public void updateSkillPacket() {
      this.client.getSession().writeAndFlush(CWvsContext.updateSkills(this.getSkills()));
   }

   public void updateLinkSkillPacket() {
      this.changeSingleSkillLevel(SkillFactory.getSkill(80000055), 0, (byte)10);
      this.changeSingleSkillLevel(SkillFactory.getSkill(80000329), 0, (byte)8);
      this.changeSingleSkillLevel(SkillFactory.getSkill(80002758), 0, (byte)6);
      this.changeSingleSkillLevel(SkillFactory.getSkill(80002762), 0, (byte)6);
      this.changeSingleSkillLevel(SkillFactory.getSkill(80002766), 0, (byte)6);
      this.changeSingleSkillLevel(SkillFactory.getSkill(80002770), 0, (byte)6);
      this.changeSingleSkillLevel(SkillFactory.getSkill(80002774), 0, (byte)6);
      List<Triple<Skill, SkillEntry, Integer>> skills = this.getLinkSkills();
      Iterator var2 = skills.iterator();

      while(true) {
         Triple linkskil;
         do {
            if (!var2.hasNext()) {
               return;
            }

            linkskil = (Triple)var2.next();
         } while(this.getSkillLevel(((Skill)linkskil.getLeft()).getId()) == 0);

         int totalskilllv = 0;
         if (((Skill)linkskil.getLeft()).getId() >= 80000066 && ((Skill)linkskil.getLeft()).getId() <= 80000070) {
            totalskilllv = ((SkillEntry)linkskil.getMid()).skillevel + this.getSkillLevel(80000055);
            this.changeSingleSkillLevel(SkillFactory.getSkill(80000055), totalskilllv, (byte)10);
         }

         if (((Skill)linkskil.getLeft()).getId() >= 80000333 && ((Skill)linkskil.getLeft()).getId() <= 80000335 || ((Skill)linkskil.getLeft()).getId() == 80000378) {
            totalskilllv = ((SkillEntry)linkskil.getMid()).skillevel + this.getSkillLevel(80000329);
            this.changeSingleSkillLevel(SkillFactory.getSkill(80000329), totalskilllv, (byte)8);
         }

         if (((Skill)linkskil.getLeft()).getId() >= 80002759 && ((Skill)linkskil.getLeft()).getId() <= 80002761) {
            totalskilllv = ((SkillEntry)linkskil.getMid()).skillevel + this.getSkillLevel(80002758);
            this.changeSingleSkillLevel(SkillFactory.getSkill(80002758), totalskilllv, (byte)6);
         }

         if (((Skill)linkskil.getLeft()).getId() >= 80002763 && ((Skill)linkskil.getLeft()).getId() <= 80002765) {
            totalskilllv = ((SkillEntry)linkskil.getMid()).skillevel + this.getSkillLevel(80002762);
            this.changeSingleSkillLevel(SkillFactory.getSkill(80002762), totalskilllv, (byte)6);
         }

         if (((Skill)linkskil.getLeft()).getId() >= 80002767 && ((Skill)linkskil.getLeft()).getId() <= 80002769) {
            totalskilllv = ((SkillEntry)linkskil.getMid()).skillevel + this.getSkillLevel(80002766);
            this.changeSingleSkillLevel(SkillFactory.getSkill(80002766), totalskilllv, (byte)6);
         }

         if (((Skill)linkskil.getLeft()).getId() >= 80002771 && ((Skill)linkskil.getLeft()).getId() <= 80002773) {
            totalskilllv = ((SkillEntry)linkskil.getMid()).skillevel + this.getSkillLevel(80002770);
            this.changeSingleSkillLevel(SkillFactory.getSkill(80002770), totalskilllv, (byte)6);
         }

         if (((Skill)linkskil.getLeft()).getId() >= 80002775 && ((Skill)linkskil.getLeft()).getId() <= 80002776 || ((Skill)linkskil.getLeft()).getId() == 80000000) {
            totalskilllv = ((SkillEntry)linkskil.getMid()).skillevel + this.getSkillLevel(80002774);
            this.changeSingleSkillLevel(SkillFactory.getSkill(80002774), totalskilllv, (byte)6);
         }

         this.getClient().getSession().writeAndFlush(CWvsContext.Linkskill(((Skill)linkskil.getLeft()).getId(), (Integer)linkskil.getRight(), this.getId(), ((SkillEntry)linkskil.getMid()).skillevel, totalskilllv));
      }
   }

   public void recalcPartyQuestRank(int questid) {
      if (MapleQuest.getInstance(questid) != null && MapleQuest.getInstance(questid).isPartyQuest()) {
         if (!this.startPartyQuest(questid)) {
            String oldRank = this.getOneInfo(questid, "rank");
            if (oldRank == null || oldRank.equals("S")) {
               return;
            }

            String newRank = null;
            if (oldRank.equals("A")) {
               newRank = "S";
            } else if (oldRank.equals("B")) {
               newRank = "A";
            } else if (oldRank.equals("C")) {
               newRank = "B";
            } else if (oldRank.equals("D")) {
               newRank = "C";
            } else {
               if (!oldRank.equals("F")) {
                  return;
               }

               newRank = "D";
            }

            List<Pair<String, Pair<String, Integer>>> questInfo = MapleQuest.getInstance(questid).getInfoByRank(newRank);
            if (questInfo == null) {
               return;
            }

            Iterator var5 = questInfo.iterator();

            while(var5.hasNext()) {
               Pair<String, Pair<String, Integer>> q = (Pair)var5.next();
               boolean found = false;
               String val = this.getOneInfo(questid, (String)((Pair)q.right).left);
               if (val == null) {
                  return;
               }

               boolean var9 = false;

               int vall;
               try {
                  vall = Integer.parseInt(val);
               } catch (NumberFormatException var11) {
                  return;
               }

               if (((String)q.left).equals("less")) {
                  found = vall < (Integer)((Pair)q.right).right;
               } else if (((String)q.left).equals("more")) {
                  found = vall > (Integer)((Pair)q.right).right;
               } else if (((String)q.left).equals("equal")) {
                  found = vall == (Integer)((Pair)q.right).right;
               }

               if (!found) {
                  return;
               }
            }

            this.updateOneInfo(questid, "rank", newRank);
         }

      }
   }

   public void tryPartyQuest(int questid) {
      if (MapleQuest.getInstance(questid) != null && MapleQuest.getInstance(questid).isPartyQuest()) {
         try {
            this.startPartyQuest(questid);
            this.pqStartTime = System.currentTimeMillis();
            this.updateOneInfo(questid, "try", String.valueOf(Integer.parseInt(this.getOneInfo(questid, "try")) + 1));
         } catch (Exception var3) {
            var3.printStackTrace();
            System.out.println("tryPartyQuest error");
         }

      }
   }

   public void endPartyQuest(int questid) {
      if (MapleQuest.getInstance(questid) != null && MapleQuest.getInstance(questid).isPartyQuest()) {
         try {
            this.startPartyQuest(questid);
            if (this.pqStartTime > 0L) {
               long changeTime = System.currentTimeMillis() - this.pqStartTime;
               int mins = (int)(changeTime / 1000L / 60L);
               int secs = (int)(changeTime / 1000L % 60L);
               int mins2 = Integer.parseInt(this.getOneInfo(questid, "min"));
               if (mins2 <= 0 || mins < mins2) {
                  this.updateOneInfo(questid, "min", String.valueOf(mins));
                  this.updateOneInfo(questid, "sec", String.valueOf(secs));
                  this.updateOneInfo(questid, "date", FileoutputUtil.CurrentReadable_Date());
               }

               int newCmp = Integer.parseInt(this.getOneInfo(questid, "cmp")) + 1;
               this.updateOneInfo(questid, "cmp", String.valueOf(newCmp));
               this.updateOneInfo(questid, "CR", String.valueOf((int)Math.ceil((double)newCmp * 100.0D / (double)Integer.parseInt(this.getOneInfo(questid, "try")))));
               this.recalcPartyQuestRank(questid);
               this.pqStartTime = 0L;
            }
         } catch (Exception var8) {
            var8.printStackTrace();
            System.out.println("endPartyQuest error");
         }

      }
   }

   public void havePartyQuest(int itemId) {
      int questid = false;
      int index = -1;
      short questid;
      switch(itemId) {
      case 1002571:
      case 1002572:
      case 1002573:
      case 1002574:
         questid = 1204;
         index = itemId - 1002571;
         break;
      case 1002798:
         questid = 1200;
         break;
      case 1022073:
         questid = 1202;
         break;
      case 1032060:
      case 1032061:
         questid = 1206;
         index = itemId - 1032060;
         break;
      case 1072369:
         questid = 1201;
         break;
      case 1082232:
         questid = 1203;
         break;
      case 1102226:
         questid = 1303;
         break;
      case 1102227:
         questid = 1303;
         index = 0;
         break;
      case 1122007:
         questid = 1301;
         break;
      case 1122010:
         questid = 1205;
         break;
      case 1122058:
         questid = 1302;
         break;
      case 3010018:
         questid = 1300;
         break;
      default:
         return;
      }

      if (MapleQuest.getInstance(questid) != null && MapleQuest.getInstance(questid).isPartyQuest()) {
         this.startPartyQuest(questid);
         this.updateOneInfo(questid, "have" + (index == -1 ? "" : index), "1");
      }
   }

   public void resetStatsByJob(boolean beginnerJob) {
      int baseJob = beginnerJob ? this.job % 1000 : this.job % 1000 / 100 * 100;
      boolean UA = this.getQuestNoAdd(MapleQuest.getInstance(111111)) != null;
      if (baseJob == 100) {
         this.resetStats(UA ? 4 : 35, 4, 4, 4);
      } else if (baseJob == 200) {
         this.resetStats(4, 4, UA ? 4 : 20, 4);
      } else if (baseJob != 300 && baseJob != 400) {
         if (baseJob == 500) {
            this.resetStats(4, UA ? 4 : 20, 4, 4);
         } else if (baseJob == 0) {
            this.resetStats(4, 4, 4, 4);
         }
      } else {
         this.resetStats(4, UA ? 4 : 25, 4, 4);
      }

   }

   public boolean hasSummon(int sourceid) {
      Iterator var2 = this.summons.iterator();

      MapleSummon summon;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         summon = (MapleSummon)var2.next();
      } while(summon.getSkill() != sourceid);

      return true;
   }

   public void removeDoor() {
      MapleDoor door = (MapleDoor)this.getDoors().iterator().next();
      Iterator var2 = door.getTarget().getCharactersThreadsafe().iterator();

      MapleCharacter chr;
      while(var2.hasNext()) {
         chr = (MapleCharacter)var2.next();
         door.sendDestroyData(chr.getClient());
      }

      var2 = door.getTown().getCharactersThreadsafe().iterator();

      while(var2.hasNext()) {
         chr = (MapleCharacter)var2.next();
         door.sendDestroyData(chr.getClient());
      }

      var2 = this.getDoors().iterator();

      while(var2.hasNext()) {
         MapleDoor destroyDoor = (MapleDoor)var2.next();
         door.getTarget().removeMapObject(destroyDoor);
         door.getTown().removeMapObject(destroyDoor);
      }

      this.clearDoors();
   }

   public void removeMechDoor() {
      Iterator var1 = this.getMechDoors().iterator();

      while(var1.hasNext()) {
         MechDoor destroyDoor = (MechDoor)var1.next();
         Iterator var3 = this.getMap().getCharactersThreadsafe().iterator();

         while(var3.hasNext()) {
            MapleCharacter chr = (MapleCharacter)var3.next();
            destroyDoor.sendDestroyData(chr.getClient());
         }

         this.getMap().removeMapObject(destroyDoor);
      }

      this.clearMechDoors();
   }

   public void changeRemoval() {
      this.changeRemoval(false);
   }

   public void changeRemoval(boolean dc) {
      this.dispelSummons();
      if (!dc) {
         this.cancelEffectFromBuffStat(SecondaryStat.Recovery);
      }

      if (this.playerShop != null && !dc) {
         this.playerShop.removeVisitor(this);
         if (this.playerShop.isOwner(this)) {
            this.playerShop.setOpen(true);
         }
      }

      if (!this.getDoors().isEmpty()) {
         this.removeDoor();
      }

      if (!this.getMechDoors().isEmpty()) {
         this.removeMechDoor();
      }

      NPCScriptManager.getInstance().dispose(this.client);
      this.cancelFairySchedule(false);
   }

   public String getTeleportName() {
      return this.teleportname;
   }

   public void setTeleportName(String tname) {
      this.teleportname = tname;
   }

   public int maxBattleshipHP(int skillid) {
      return this.getTotalSkillLevel(skillid) * 5000 + (this.getLevel() - 120) * 3000;
   }

   public int currentBattleshipHP() {
      return this.battleshipHP;
   }

   public void setBattleshipHP(int v) {
      this.battleshipHP = v;
   }

   public void decreaseBattleshipHP() {
      --this.battleshipHP;
   }

   public boolean isInTownMap() {
      if (this.getMap().isTown() && !FieldLimitType.VipRock.check(this.getMap().getFieldLimit()) && this.getEventInstance() == null) {
         int[] var1 = GameConstants.blockedMaps;
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            int i = var1[var3];
            if (this.getMapId() == i) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public void startPartySearch(List<Integer> jobs, int maxLevel, int minLevel, int membersNeeded) {
      Iterator var5 = this.map.getCharacters().iterator();

      while(var5.hasNext()) {
         MapleCharacter chr = (MapleCharacter)var5.next();
         if (chr.getId() != this.id && chr.getParty() == null && chr.getLevel() >= minLevel && chr.getLevel() <= maxLevel && (jobs.isEmpty() || jobs.contains(Integer.valueOf(chr.getJob()))) && (this.isGM() || !chr.isGM())) {
            if (this.party == null || this.party.getMembers().size() >= 6 || this.party.getMembers().size() >= membersNeeded) {
               break;
            }

            chr.setParty(this.party);
            World.Party.updateParty(this.party.getId(), PartyOperation.JOIN, new MaplePartyCharacter(chr));
            chr.receivePartyMemberHP();
            chr.updatePartyMemberHP();
         }
      }

   }

   public int getChallenge() {
      return this.challenge;
   }

   public void setChallenge(int c) {
      this.challenge = c;
   }

   public short getFatigue() {
      return this.fatigue;
   }

   public void setFatigue(int j) {
      this.fatigue = (short)Math.max(0, j);
      this.updateSingleStat(MapleStat.FATIGUE, (long)this.fatigue);
   }

   public void updateDamageSkin() {
      this.client.getSession().writeAndFlush(CWvsContext.updateDamageSkin(this));
   }

   public void fakeRelog() {
      this.client.getSession().writeAndFlush(CField.getCharInfo(this));
      MapleMap mapp = this.getMap();
      mapp.setCheckStates(false);
      mapp.removePlayer(this);
      mapp.addPlayer(this);
      mapp.setCheckStates(true);
   }

   public boolean canSummon() {
      return this.canSummon(5000);
   }

   public boolean canSummon(int g) {
      if (this.lastSummonTime + (long)g < System.currentTimeMillis()) {
         this.lastSummonTime = System.currentTimeMillis();
         return true;
      } else {
         return false;
      }
   }

   public int getIntNoRecord(int questID) {
      MapleQuestStatus stat = this.getQuestNoAdd(MapleQuest.getInstance(questID));
      return stat != null && stat.getCustomData() != null ? Integer.parseInt(stat.getCustomData()) : 0;
   }

   public int getIntRecord(int questID) {
      MapleQuestStatus stat = this.getQuestNAdd(MapleQuest.getInstance(questID));
      if (stat.getCustomData() == null) {
         stat.setCustomData("0");
      }

      return Integer.parseInt(stat.getCustomData());
   }

   public void updatePetAuto() {
      if (this.getIntNoRecord(122221) > 0) {
         this.client.getSession().writeAndFlush(CField.petAutoHP(this.getIntRecord(122221)));
      }

      if (this.getIntNoRecord(122223) > 0) {
         this.client.getSession().writeAndFlush(CField.petAutoMP(this.getIntRecord(122223)));
      }

      if (this.getKeyValue(9999, "skillid") == -1L) {
         this.setKeyValue(9999, "skillid", "0");
      }

      if (this.getKeyValue(9999, "skillid2") == -1L) {
         this.setKeyValue(9999, "skillid2", "0");
      }

      if (this.getKeyValue(9999, "skillid3") == -1L) {
         this.setKeyValue(9999, "skillid3", "0");
      }

      if (this.getKeyValue(9999, "skillid4") == -1L) {
         this.setKeyValue(9999, "skillid4", "0");
      }

      if (this.getKeyValue(9999, "skillid5") == -1L) {
         this.setKeyValue(9999, "skillid5", "0");
      }

      if (this.getKeyValue(9999, "skillid6") == -1L) {
         this.setKeyValue(9999, "skillid6", "0");
      }

      Channel var10000 = this.getClient().getSession();
      long var10002 = this.getKeyValue(9999, "skillid");
      var10000.writeAndFlush(CWvsContext.InfoPacket.showPetSkills(0, "0=" + var10002 + ";1=" + this.getKeyValue(9999, "skillid2")));
      var10000 = this.getClient().getSession();
      var10002 = this.getKeyValue(9999, "skillid3");
      var10000.writeAndFlush(CWvsContext.InfoPacket.showPetSkills(1, "10=" + var10002 + ";11=" + this.getKeyValue(9999, "skillid4")));
      var10000 = this.getClient().getSession();
      var10002 = this.getKeyValue(9999, "skillid5");
      var10000.writeAndFlush(CWvsContext.InfoPacket.showPetSkills(2, "20=" + var10002 + ";21=" + this.getKeyValue(9999, "skillid6")));
   }

   public void sendEnglishQuiz(String msg) {
   }

   public void setChangeTime() {
      this.mapChangeTime = System.currentTimeMillis();
   }

   public long getChangeTime() {
      return this.mapChangeTime;
   }

   public short getScrolledPosition() {
      return this.scrolledPosition;
   }

   public void setScrolledPosition(short s) {
      this.scrolledPosition = s;
   }

   public MapleTrait getTrait(MapleTrait.MapleTraitType t) {
      return (MapleTrait)this.traits.get(t);
   }

   public void forceCompleteQuest(int id) {
      MapleQuest.getInstance(id).forceComplete(this, 2007);
   }

   public List<Integer> getExtendedSlots() {
      return this.extendedSlots;
   }

   public int getExtendedSlot(int index) {
      return this.extendedSlots.size() > index && index >= 0 ? (Integer)this.extendedSlots.get(index) : -1;
   }

   public void changedExtended() {
      this.changed_extendedSlots = true;
   }

   public MapleAndroid getAndroid() {
      return this.android;
   }

   public void removeAndroid() {
      if (this.map != null) {
         this.map.broadcastMessage(CField.deactivateAndroid(this.id));
      }

      this.android = null;
   }

   public void setAndroid(MapleAndroid and) {
      this.android = and;
      if (this.map != null && and != null) {
         this.android.setStance(0);
         this.android.setPos(this.getPosition());
         this.map.broadcastMessage(this, CField.spawnAndroid(this, this.android), true);
         this.map.broadcastMessage(this, CField.showAndroidEmotion(this.getId(), Randomizer.nextInt(17) + 1), true);
      } else if (this.map != null && and == null) {
         this.map.broadcastMessage(this, CField.deactivateAndroid(this.getId()), true);
      }

   }

   public void updateAndroid() {
      if (this.map != null && this.android != null) {
         this.map.broadcastMessage(this, CField.spawnAndroid(this, this.android), true);
      } else if (this.map != null && this.android == null) {
         this.map.broadcastMessage(this, CField.deactivateAndroid(this.getId()), true);
      }

   }

   public List<Item> getRebuy() {
      return this.rebuy;
   }

   public MapleImp[] getImps() {
      return this.imps;
   }

   public int getBattlePoints() {
      return this.pvpPoints;
   }

   public int getTotalBattleExp() {
      return this.pvpExp;
   }

   public void setBattlePoints(int p) {
      if (p != this.pvpPoints) {
         this.client.getSession().writeAndFlush(CWvsContext.InfoPacket.getBPMsg(p - this.pvpPoints));
         this.updateSingleStat(MapleStat.BATTLE_POINTS, (long)p);
      }

      this.pvpPoints = p;
   }

   public void setTotalBattleExp(int p) {
      int previous = this.pvpExp;
      this.pvpExp = p;
      if (p != previous) {
         this.stats.recalcPVPRank(this);
         this.updateSingleStat(MapleStat.BATTLE_EXP, (long)this.stats.pvpExp);
         this.updateSingleStat(MapleStat.BATTLE_RANK, (long)this.stats.pvpRank);
      }

   }

   public boolean inPVP() {
      return this.eventInstance != null && this.eventInstance.getName().startsWith("PVP");
   }

   public void clearCooldowns(List<MapleCoolDownValueHolder> cooldowns) {
      Map<Integer, Integer> datas = new HashMap();
      Iterator var3 = cooldowns.iterator();

      while(var3.hasNext()) {
         MapleCoolDownValueHolder m = (MapleCoolDownValueHolder)var3.next();
         int skil = m.skillId;
         this.removeCooldown(skil);
         datas.put(skil, 0);
      }

      this.client.getSession().writeAndFlush(CField.skillCooldown(datas));
   }

   public void clearAllCooldowns() {
      Map<Integer, Integer> datas = new HashMap();
      Iterator var2 = this.getCooldowns().iterator();

      while(var2.hasNext()) {
         MapleCoolDownValueHolder m = (MapleCoolDownValueHolder)var2.next();
         int skil = m.skillId;
         if (skil != 80002282) {
            this.removeCooldown(skil);
            datas.put(skil, 0);
         }
      }

      this.client.getSession().writeAndFlush(CField.skillCooldown(datas));
   }

   public void clearAllCooldowns(int skillid) {
      Iterator var2 = this.getCooldowns().iterator();

      while(var2.hasNext()) {
         MapleCoolDownValueHolder m = (MapleCoolDownValueHolder)var2.next();
         int skil = m.skillId;
         if (skil != skillid && skil != 80002282) {
            this.removeCooldown(skil);
            this.client.getSession().writeAndFlush(CField.skillCooldown(skil, 0));
         }
      }

   }

   public void handleForceGain(int oid, int skillid) {
      if (this.getSkillCustomValue(31101002) == null) {
         this.handleForceGain(oid, skillid, 0);
      }

   }

   public void handleForceGain(int oid, int skillid, int extraForce) {
      int forceGain = 0;
      if (skillid >= 31001006 && skillid <= 31001008 || skillid == 31000004 || skillid >= 400011007 && skillid <= 400011009) {
         if (this.getLevel() >= 30 && this.getLevel() < 60) {
            forceGain = 3;
         } else if (this.getLevel() >= 60 && this.getLevel() < 100) {
            forceGain = 4;
         } else if (this.getLevel() >= 100) {
            forceGain = 5;
         }

         if (extraForce > 0) {
            forceGain *= 2;
         }

         if (this.getSkillLevel(31110009) > 0 && Randomizer.isSuccess(75)) {
            forceGain += 6;
         }
      }

      if (skillid == 30010111) {
         forceGain = 3;
      } else if (skillid == 31110008) {
         forceGain = 5;
      } else if (skillid == 31121052) {
         forceGain = 50;
         extraForce = 1;
      }

      if ((skillid == 400011077 || skillid == 400011078) && extraForce > 0) {
         forceGain = extraForce;
         extraForce = 0;
      }

      if (this.getCooldownLimit(31121054) > 0L) {
         this.forceBlood += extraForce > 0 ? extraForce : forceGain;
         if (this.forceBlood >= 50) {
            this.changeCooldown(31121054, -3000);
            this.forceBlood -= 50;
         }
      }

      if (forceGain > 0) {
         ++this.force;
         MapleAtom atom = new MapleAtom(true, oid, 0, true, skillid, this.getTruePosition().x, this.getTruePosition().y);
         atom.setDwUserOwner(this.id);
         server.maps.ForceAtom at = new server.maps.ForceAtom(extraForce > 0 ? 12 : 5, Randomizer.rand(35, 45), Randomizer.rand(4, 6), Randomizer.rand(35, 45), 0);
         at.setnAttackCount(forceGain);
         atom.addForceAtom(at);
         this.getClient().send(CField.createAtom(atom));
      }

   }

   public void afterAttack(AttackInfo attack) {
      int skillid = attack.skill;
      switch(this.getJob()) {
      case 110:
      case 111:
      case 112:
         if (!PlayerHandler.isFinisher(skillid) & this.getBuffedValue(SecondaryStat.ComboCounter) != null) {
            this.handleOrbgain(attack, skillid);
         }
      default:
         if (!this.isIntern()) {
            this.cancelEffectFromBuffStat(SecondaryStat.WindWalk);
            this.cancelEffectFromBuffStat(SecondaryStat.Infiltrate);
         }

      }
   }

   public void applyIceGage(int x) {
      this.updateSingleStat(MapleStat.ICE_GAGE, (long)x);
   }

   public Rectangle getBounds() {
      return new Rectangle(this.getTruePosition().x - 25, this.getTruePosition().y - 75, 50, 75);
   }

   public Map<Short, Integer> getEquips() {
      Map<Short, Integer> eq = new HashMap();
      Iterator var2 = this.inventory[MapleInventoryType.EQUIPPED.ordinal()].newList().iterator();

      while(var2.hasNext()) {
         Item item = (Item)var2.next();
         int itemId = item.getItemId();
         eq.put(item.getPosition(), itemId);
      }

      return eq;
   }

   public Map<Short, Integer> getSecondEquips() {
      Map<Short, Integer> eq = new HashMap();

      Item item;
      int itemId;
      for(Iterator var2 = this.inventory[MapleInventoryType.EQUIPPED.ordinal()].newList().iterator(); var2.hasNext(); eq.put(item.getPosition(), itemId)) {
         item = (Item)var2.next();
         itemId = item.getItemId();
         if (item instanceof Equip) {
         }
      }

      return eq;
   }

   public int getReborns() {
      return this.reborns;
   }

   public void setReborns(int data) {
      this.reborns = data;
   }

   public int getAPS() {
      return this.apstorage;
   }

   public void gainAPS(int aps) {
      this.apstorage += aps;
   }

   public void doReborn() {
      Map<MapleStat, Long> stat = new EnumMap(MapleStat.class);
      ++this.reborns;
      this.setLevel((short)12);
      this.setExp(0L);
      this.setRemainingAp((short)0);
      int oriStats = this.stats.getStr() + this.stats.getDex() + this.stats.getLuk() + this.stats.getInt();
      int str = Randomizer.rand(25, this.stats.getStr());
      int dex = Randomizer.rand(25, this.stats.getDex());
      int int_ = Randomizer.rand(25, this.stats.getInt());
      int luk = Randomizer.rand(25, this.stats.getLuk());
      int afterStats = str + dex + int_ + luk;
      int MAS = oriStats - afterStats + this.getRemainingAp();
      this.client.getPlayer().gainAPS(MAS);
      this.stats.recalcLocalStats(this);
      this.stats.setStr((short)str, this.client.getPlayer());
      this.stats.setDex((short)dex, this.client.getPlayer());
      this.stats.setInt((short)int_, this.client.getPlayer());
      this.stats.setLuk((short)luk, this.client.getPlayer());
      stat.put(MapleStat.STR, (long)str);
      stat.put(MapleStat.DEX, (long)dex);
      stat.put(MapleStat.INT, (long)int_);
      stat.put(MapleStat.LUK, (long)luk);
      stat.put(MapleStat.AVAILABLEAP, 0L);
      this.updateSingleStat(MapleStat.LEVEL, 11L);
      this.updateSingleStat(MapleStat.JOB, 0L);
      this.updateSingleStat(MapleStat.EXP, 0L);
      this.client.getSession().writeAndFlush(CWvsContext.updatePlayerStats(stat, false, this));
   }

   public List<InnerSkillValueHolder> getInnerSkills() {
      return this.innerSkills;
   }

   public int getHonourExp() {
      return this.honourExp;
   }

   public void setHonourExp(int exp) {
      this.honourExp = exp;
   }

   public int getHonorLevel() {
      if (this.honorLevel == 0) {
         ++this.honorLevel;
      }

      return this.honorLevel;
   }

   public int getHonourNextExp() {
      return this.getHonorLevel() == 0 ? 0 : (this.getHonorLevel() + 1) * 500;
   }

   public void setCardStack(byte amount) {
      this.cardStack = amount;
   }

   public byte getCardStack() {
      return this.cardStack;
   }

   public void setPetLoot(boolean status) {
      this.petLoot = status;
   }

   public boolean getPetLoot() {
      return this.petLoot;
   }

   public int getStorageNPC() {
      return this.storageNpc;
   }

   public void setStorageNPC(int id) {
      this.storageNpc = id;
   }

   public boolean getPvpStatus() {
      return this.pvp;
   }

   public void togglePvP() {
      this.pvp = !this.pvp;
   }

   public void enablePvP() {
      this.pvp = true;
   }

   public void disablePvP() {
      this.pvp = false;
   }

   public void addHonorExp(int amount) {
      this.setHonourExp(this.getHonourExp() + amount);
      this.client.getSession().writeAndFlush(CWvsContext.updateAzwanFame(this.getHonourExp()));
   }

   public void gainHonor(int honor) {
      this.addHonorExp(honor);
      if (this.getKeyValue(5, "show_honor") > 0L) {
         this.dropMessage(5, "명성치 " + honor + "을 얻었습니다.");
      }

   }

   public List<Integer> HeadTitle() {
      List<Integer> num_ = new ArrayList();
      num_.add(0);
      num_.add(0);
      num_.add(0);
      num_.add(0);
      num_.add(0);
      return num_;
   }

   public int getInternetCafeTime() {
      return this.itcafetime;
   }

   public void setInternetCafeTime(int itcafetime) {
      this.itcafetime = itcafetime;
   }

   public void InternetCafeTimer() {
      if (this.itcafetimer != null) {
         this.itcafetimer.cancel(false);
      }

      this.itcafetimer = server.Timer.CloneTimer.getInstance().register(new Runnable() {
         public void run() {
            if (MapleCharacter.this.getInternetCafeTime() < 1) {
               MapleCharacter.this.client.getSession().writeAndFlush(CField.getInternetCafe((byte)4, 0));
            } else {
               MapleCharacter.this.setInternetCafeTime(MapleCharacter.this.getInternetCafeTime() - 1);
            }
         }
      }, 60000L);
   }

   public short getMonsterCombo() {
      return this.monsterCombo;
   }

   public void setMonsterCombo(short count) {
      this.monsterCombo = count;
   }

   public void addMonsterCombo(short amount) {
      this.monsterCombo += amount;
   }

   public long getMonsterComboTime() {
      return this.monsterComboTime;
   }

   public void setMonsterComboTime(long count) {
      this.monsterComboTime = count;
   }

   public long getRuneTimeStamp() {
      return this.lastTouchedRuneTime;
   }

   public void setRuneTimeStamp(long lastTouchedRuneTime) {
      this.lastTouchedRuneTime = lastTouchedRuneTime;
   }

   public int getTouchedRune() {
      return this.TouchedRune;
   }

   public void setTouchedRune(int type) {
      this.TouchedRune = type;
   }

   public void cancelRapidTime(byte type) {
      if (type == 1) {
         if (this.rapidtimer1 != null) {
            this.rapidtimer1.cancel(false);
         }

         this.rapidtimer1 = server.Timer.BuffTimer.getInstance().schedule(new Runnable() {
            public void run() {
               MapleCharacter.this.changeSkillLevel(SkillFactory.getSkill(100000276), (byte)0, (byte)0);
            }
         }, 20000L);
      } else if (type == 2) {
         if (this.rapidtimer2 != null) {
            this.rapidtimer2.cancel(false);
         }

         this.rapidtimer2 = server.Timer.BuffTimer.getInstance().schedule(new Runnable() {
            public void run() {
               MapleCharacter.this.changeSkillLevel(SkillFactory.getSkill(100000277), (byte)0, (byte)0);
            }
         }, 20000L);
      }

   }

   public long getnHPoint() {
      try {
         return Long.parseLong(this.client.getKeyValue("nHpoint"));
      } catch (Exception var2) {
         return 0L;
      }
   }

   public long getnPPoint() {
      try {
         return Long.parseLong(this.client.getKeyValue("nPpoint"));
      } catch (Exception var2) {
         return 0L;
      }
   }

   public int getHgrade() {
      try {
         return Integer.parseInt(this.client.getKeyValue("hGrade"));
      } catch (Exception var2) {
         return 0;
      }
   }

   public void setHgrade(int a) {
      this.client.setKeyValue("hGrade", String.valueOf(a));
   }

   public String getHgrades() {
      switch(this.getHgrade()) {
      case 1:
         return "HEINZ.A";
      case 2:
         return "HEINZ.B";
      case 3:
         return "HEINZ.C";
      case 4:
         return "HEINZ.D";
      case 5:
         return "HEINZ.E";
      case 6:
         return "HEINZ.F";
      case 7:
         return "HEINZ.S";
      case 8:
         return "HEINZ.SS";
      case 9:
         return "HEINZ.SSS";
      case 10:
         return "HEINZ.SSS+";
      default:
         return "일반";
      }
   }

   public int getPgrade() {
      try {
         return Integer.parseInt(this.client.getKeyValue("pGrade"));
      } catch (Exception var2) {
         return 0;
      }
   }

   public void setPgrade(int a) {
      this.client.setKeyValue("pGrade", String.valueOf(a));
   }

   public String getPgrades() {
      String keyValue = this.client.getKeyValue("pGrade");
      byte var3 = -1;
      switch(keyValue.hashCode()) {
      case 49:
         if (keyValue.equals("1")) {
            var3 = 0;
         }
         break;
      case 50:
         if (keyValue.equals("2")) {
            var3 = 1;
         }
         break;
      case 51:
         if (keyValue.equals("3")) {
            var3 = 2;
         }
         break;
      case 52:
         if (keyValue.equals("4")) {
            var3 = 3;
         }
         break;
      case 53:
         if (keyValue.equals("5")) {
            var3 = 4;
         }
         break;
      case 54:
         if (keyValue.equals("6")) {
            var3 = 5;
         }
      }

      switch(var3) {
      case 0:
         return "비기닝";
      case 1:
         return "라이징";
      case 2:
         return "플라잉";
      case 3:
         return "샤이닝";
      case 4:
         return "아이돌";
      case 5:
         return "슈퍼스타";
      default:
         return "일반";
      }
   }

   public void gainnHPoint(int a) {
      this.client.setKeyValue("nHpoint", String.valueOf(this.getHPoint() + (long)a));
   }

   public void setnHPoint(int a) {
      this.client.setKeyValue("nHpoint", String.valueOf(a));
   }

   public void gainnPPoint(int a) {
      this.client.setKeyValue("nPpoint", String.valueOf(this.getPPoint() + (long)a));
   }

   public void setnPPoint(int a) {
      this.client.setKeyValue("nPpoint", String.valueOf(a));
   }

   public long getnDonationPoint() {
      try {
         return Long.parseLong(this.client.getKeyValue("nDpoint"));
      } catch (Exception var2) {
         return 0L;
      }
   }

   public void gainnDonationPoint(int a) {
      this.client.setKeyValue("nDpoint", String.valueOf(this.getnDonationPoint() + (long)a));
   }

   public void setnDonationPoint(int a) {
      this.client.setKeyValue("nDpoint", String.valueOf(a));
   }

   public long getHPoint() {
      try {
         return Long.parseLong(this.client.getKeyValue("HPoint"));
      } catch (Exception var2) {
         return 0L;
      }
   }

   public long getPPoint() {
      try {
         return Long.parseLong(this.client.getKeyValue("PPoint"));
      } catch (Exception var2) {
         return 0L;
      }
   }

   public void setEventMobCount(int a) {
      this.eventcount = a;
   }

   public int getEventMobCount() {
      return this.eventcount;
   }

   public void setEventKillingMode(boolean a) {
      this.eventkillmode = a;
   }

   public boolean getEventKillingMode() {
      return this.eventkillmode;
   }

   public void gainHPoint(int a) {
      if (a > 0) {
         this.gainnHPoint(a);
      }

      this.client.setKeyValue("HPoint", String.valueOf(this.getHPoint() + (long)a));
   }

   public void setHPoint(int a) {
      this.client.setKeyValue("HPoint", String.valueOf(a));
   }

   public void gainPPoint(int a) {
      if (a > 0) {
         this.gainnPPoint(a);
      }

      this.client.setKeyValue("PPoint", String.valueOf(this.getPPoint() + (long)a));
   }

   public void setPPoint(int a) {
      this.client.setKeyValue("PPoint", String.valueOf(a));
   }

   public long getDonationPoint() {
      try {
         return Long.parseLong(this.client.getKeyValue("DPoint"));
      } catch (Exception var2) {
         return 0L;
      }
   }

   public void gainDonationPoint(int a) {
      if (a > 0) {
         this.gainnDonationPoint(a);
      }

      this.client.setKeyValue("DPoint", String.valueOf(this.getDonationPoint() + (long)a));
      this.updateDonationPoint();
   }

   public void setDonationPoint(int a) {
      this.client.setKeyValue("DPoint", String.valueOf(a));
      this.updateDonationPoint();
   }

   public void updateDonationPoint() {
   }

   public int getBetaClothes() {
      return this.betaclothes;
   }

   public void pBetaClothes(int value) {
      this.betaclothes += value;
   }

   public void mBetaClothes(int value) {
      this.betaclothes -= value;
   }

   public int getArcaneAim() {
      return this.arcaneAim;
   }

   public void setArcaneAim(int a) {
      this.arcaneAim = a;
   }

   public static boolean updateNameChangeCoupon(MapleClient c) {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      boolean var5;
      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE accounts SET nameChange = ? WHERE id = ?");
         ps.setByte(1, c.getNameChangeEnable());
         ps.setInt(2, c.getAccID());
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
         }

      }

      return var5;
   }

   public static boolean saveNameChange(String name, int cid) {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      boolean var6;
      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE characters SET name = ? WHERE id = ?");
         ps.setString(1, name);
         ps.setInt(2, cid);
         ps.executeUpdate();
         ps.close();
         con.close();
         return true;
      } catch (SQLException var16) {
         var16.printStackTrace();
         var6 = false;
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
         }

      }

      return var6;
   }

   public Map<Integer, Integer> getSkillCustomValues2() {
      return this.customValue;
   }

   public void unchooseStolenSkill(int skillID) {
      if (!this.skillisCooling(20031208) && this.stolenSkills != null) {
         int stolenjob = GameConstants.getJobNumber(skillID);
         boolean changed = false;
         Iterator var4 = this.stolenSkills.iterator();

         while(var4.hasNext()) {
            Pair<Integer, Boolean> sk = (Pair)var4.next();
            if ((Boolean)sk.right && GameConstants.getJobNumber((Integer)sk.left) == stolenjob) {
               this.cancelStolenSkill((Integer)sk.left);
               sk.right = false;
               changed = true;
            }
         }

         if (changed) {
            Skill skil = SkillFactory.getSkill(skillID);
            this.changeSkillLevel_Skip(skil, this.getSkillLevel(skil), (byte)0);
            this.client.getSession().writeAndFlush(CField.replaceStolenSkill(GameConstants.getStealSkill(stolenjob), 0));
         }

      } else {
         this.dropMessage(-6, "[Loadout] The skill is under cooldown. Please wait.");
      }
   }

   public void cancelStolenSkill(int skillID) {
      Skill skk = SkillFactory.getSkill(skillID);
      SecondaryStatEffect eff = skk.getEffect(this.getTotalSkillLevel(skk));
      if (eff.getDuration() > 0 && !eff.getStatups().isEmpty()) {
         boolean party = false;
         switch(skillID) {
         case 1101006:
         case 1211011:
         case 1301006:
         case 1301007:
         case 2101001:
         case 2201001:
         case 2301002:
         case 2301004:
         case 2311001:
         case 2311003:
         case 2311009:
         case 2321005:
         case 2321007:
         case 2321055:
         case 3121002:
         case 3221002:
         case 3321022:
         case 4101004:
         case 4111001:
         case 4201003:
         case 5121009:
         case 5301003:
         case 5320008:
         case 12101000:
         case 13121005:
         case 14101003:
         case 15121005:
         case 21111012:
         case 22151003:
         case 22171054:
         case 27111006:
         case 27111101:
         case 32001003:
         case 32001016:
         case 32101003:
         case 32101009:
         case 32111012:
         case 32121017:
         case 33101005:
         case 33121004:
         case 51101004:
         case 51111008:
         case 61121009:
         case 100001263:
         case 100001264:
         case 131001009:
         case 131001013:
         case 131001113:
         case 152121043:
         case 400011003:
         case 400041011:
         case 400041012:
         case 400041013:
         case 400041014:
         case 400041015:
            party = true;
         }

         if (party) {
            Iterator var5 = this.map.getCharactersThreadsafe().iterator();

            while(var5.hasNext()) {
               MapleCharacter chr = (MapleCharacter)var5.next();
               if (chr.getBuffedValue(skillID)) {
                  chr.getClient().getSession().writeAndFlush(CField.getGameMessage(6, SkillFactory.getSkillName(skillID) + " 스킬을 사용 할 수 없어 효과가 제거되었습니다."));
                  chr.cancelEffect(eff);
               }
            }
         } else if (this.getBuffedValue(skillID)) {
            this.getClient().getSession().writeAndFlush(CField.getGameMessage(6, SkillFactory.getSkillName(skillID) + " 스킬을 사용 할 수 없어 효과가 제거되었습니다."));
            this.cancelEffect(eff);
         }
      }

   }

   public void chooseStolenSkill(int skillID) {
      if (!this.skillisCooling(20031208) && this.stolenSkills != null) {
         Pair<Integer, Boolean> dummy = new Pair(skillID, false);
         if (this.stolenSkills.contains(dummy)) {
            this.unchooseStolenSkill(skillID);
            ((Pair)this.stolenSkills.get(this.stolenSkills.indexOf(dummy))).right = true;
            this.addCooldown(GameConstants.getStealSkill(GameConstants.getJobNumber(skillID)), System.currentTimeMillis(), 30000L);
            this.client.getSession().writeAndFlush(CField.skillCooldown(GameConstants.getStealSkill(GameConstants.getJobNumber(skillID)), 30000));
            this.client.getSession().writeAndFlush(CField.replaceStolenSkill(GameConstants.getStealSkill(GameConstants.getJobNumber(skillID)), skillID));
         }

      } else {
         this.dropMessage(-6, "[Loadout] The skill is under cooldown. Please wait.");
      }
   }

   public void addStolenSkill(int skillID, int skillLevel) {
      if (!this.skillisCooling(20031208) && this.stolenSkills != null) {
         Pair<Integer, Boolean> dummy = new Pair(skillID, true);
         Skill skil = SkillFactory.getSkill(skillID);
         if (!this.stolenSkills.contains(dummy)) {
            dummy.right = false;
            skillLevel = Math.min(skil.getMaxLevel(), skillLevel);
            int jobid = GameConstants.getJobNumber(skillID);
            if (!this.stolenSkills.contains(dummy)) {
               int count = 0;
               skillLevel = Math.min(this.getSkillLevel(GameConstants.getStealSkill(jobid)), skillLevel);
               Iterator var7 = this.stolenSkills.iterator();

               while(var7.hasNext()) {
                  Pair<Integer, Boolean> sk = (Pair)var7.next();
                  if (GameConstants.getJobNumber((Integer)sk.left) == jobid) {
                     ++count;
                  }
               }

               if (count < GameConstants.getNumSteal(jobid)) {
                  this.stolenSkills.add(dummy);
                  this.changed_skills = true;
                  this.changeSkillLevel_Skip(skil, skillLevel, (byte)skillLevel);
                  this.client.getSession().writeAndFlush(CField.addStolenSkill(jobid, count, skillID, skillLevel));
               }
            }
         }

      } else {
         this.dropMessage(-6, "[Loadout] The skill is under cooldown. Please wait.");
      }
   }

   public void removeStolenSkill(int skillID) {
      if (!this.skillisCooling(20031208) && this.stolenSkills != null) {
         int jobid = GameConstants.getJobNumber(skillID);
         Pair<Integer, Boolean> dummy = new Pair(skillID, false);
         int count = -1;
         int cc = 0;

         for(int i = 0; i < this.stolenSkills.size(); ++i) {
            if ((Integer)((Pair)this.stolenSkills.get(i)).left == skillID) {
               if ((Boolean)((Pair)this.stolenSkills.get(i)).right) {
                  this.unchooseStolenSkill(skillID);
               }

               count = cc;
               break;
            }

            if (GameConstants.getJobNumber((Integer)((Pair)this.stolenSkills.get(i)).left) == jobid) {
               ++cc;
            }
         }

         if (count >= 0) {
            this.cancelStolenSkill(skillID);
            this.stolenSkills.remove(dummy);
            dummy.right = true;
            this.stolenSkills.remove(dummy);
            this.changed_skills = true;
            this.changeSkillLevel_Skip(SkillFactory.getSkill(skillID), 0, (byte)0);
            this.client.getSession().writeAndFlush(CField.removeStolenSkill(jobid, count));
            this.saveToDB(false, false);
         }

      } else {
         this.dropMessage(-6, "[Loadout] The skill is under cooldown. Please wait.");
      }
   }

   public List<Pair<Integer, Boolean>> getStolenSkills() {
      return this.stolenSkills;
   }

   public final void startDiabolicRecovery(SecondaryStatEffect eff) {
      server.Timer.BuffTimer tMan = server.Timer.BuffTimer.getInstance();
      final int regenHP = (int)((double)this.getStat().getCurrentMaxHp() * ((double)eff.getX() / 100.0D));
      if (this.diabolicRecoveryTask != null) {
         this.diabolicRecoveryTask.cancel(true);
         this.diabolicRecoveryTask = null;
      }

      Runnable r = new Runnable() {
         public void run() {
            if (MapleCharacter.this.isAlive()) {
               MapleCharacter.this.addHP((long)regenHP);
               if (MapleCharacter.this.getStat().getCurrentMaxHp() - (long)regenHP > 0L) {
                  MapleCharacter.this.client.getSession().writeAndFlush(CField.EffectPacket.showHealEffect(MapleCharacter.this.client.getPlayer(), (int)Math.min(MapleCharacter.this.getStat().getCurrentMaxHp() - (long)regenHP, (long)regenHP), true));
               }
            }

         }
      };
      this.diabolicRecoveryTask = tMan.register(r, (long)(eff.getW() * 1000));
      tMan.schedule(new Runnable() {
         public void run() {
            if (MapleCharacter.this.diabolicRecoveryTask != null) {
               MapleCharacter.this.diabolicRecoveryTask.cancel(true);
               MapleCharacter.this.diabolicRecoveryTask = null;
            }

         }
      }, (long)eff.getDuration());
   }

   public short getXenonSurplus() {
      return this.xenonSurplus;
   }

   public void setXenonSurplus(short amount, Skill skill) {
      int maxSupply = this.level >= 100 ? 20 : (this.level >= 60 ? 15 : (this.level >= 30 ? 10 : 5));
      if (this.getBuffedValue(SecondaryStat.Overload) != null) {
         maxSupply = 40;
      }

      if (this.xenonSurplus + amount > maxSupply) {
         this.updateXenonSurplus(this.xenonSurplus = (short)maxSupply, skill);
      } else {
         this.updateXenonSurplus(this.xenonSurplus = amount, skill);
      }
   }

   public void gainXenonSurplus(short amount, Skill skill) {
      int maxSupply = this.level >= 100 ? 20 : (this.level >= 60 ? 15 : (this.level >= 30 ? 10 : 5));
      if (this.getBuffedValue(SecondaryStat.Overload) != null) {
         maxSupply = 40;
      }

      if (this.xenonSurplus + amount > maxSupply) {
         this.updateXenonSurplus(this.xenonSurplus = (short)maxSupply, skill);
      } else {
         this.updateXenonSurplus(this.xenonSurplus += amount, skill);
      }
   }

   public void updateXenonSurplus(short amount, Skill skill) {
      int maxSupply = this.level >= 100 ? 20 : (this.level >= 60 ? 15 : (this.level >= 30 ? 10 : 5));
      if (this.getBuffedValue(SecondaryStat.Overload) != null) {
         maxSupply = 40;
      }

      if (amount > maxSupply) {
         amount = (short)maxSupply;
      }

      SecondaryStatEffect effect = SkillFactory.getSkill(30020232).getEffect(this.getTotalSkillLevel(skill));
      EnumMap<SecondaryStat, Pair<Integer, Integer>> statups = new EnumMap(SecondaryStat.class);
      statups.put(SecondaryStat.SurplusSupply, new Pair(Integer.valueOf(amount), 0));
      this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, effect, this));
   }

   public final void startXenonSupply(final Skill skill) {
      server.Timer.BuffTimer tMan = server.Timer.BuffTimer.getInstance();
      Runnable r = new Runnable() {
         public void run() {
            int maxSupply = MapleCharacter.this.level >= 100 ? 20 : (MapleCharacter.this.level >= 60 ? 15 : (MapleCharacter.this.level >= 30 ? 10 : 5));
            if (MapleCharacter.this.getBuffedValue(SecondaryStat.Overload) != null) {
               maxSupply = 40;
            }

            if (maxSupply > MapleCharacter.this.getXenonSurplus()) {
               MapleCharacter.this.gainXenonSurplus((short)1, skill);
            }

         }
      };
      if (this.client.isLoggedIn()) {
         XenonSupplyTask = tMan.register(r, 4000L);
      }

   }

   public void handleExceedAttack(int skillid) {
      int ownskillid = 0;
      this.setSkillCustomInfo(30010231, 0L, 10000L);
      switch(skillid) {
      case 31011000:
      case 31011004:
      case 31011005:
      case 31011006:
      case 31011007:
         ownskillid = 31011000;
         break;
      case 31201000:
      case 31201007:
      case 31201008:
      case 31201009:
      case 31201010:
         ownskillid = 31201000;
         break;
      case 31211000:
      case 31211007:
      case 31211008:
      case 31211009:
      case 31211010:
         ownskillid = 31211000;
         break;
      case 31221000:
      case 31221009:
      case 31221010:
      case 31221011:
      case 31221012:
         ownskillid = 31221000;
      }

      if (this.getSkillCustomValue0(30010232) != (long)ownskillid && this.getSkillCustomValue0(30010232) != 0L) {
         if (this.getSkillLevel(31220044) > 0) {
            if (this.getExceed() < 19) {
               this.gainExceed((short)1);
            }
         } else if (this.getExceed() < 20) {
            this.gainExceed((short)1);
         }
      }

      this.setSkillCustomInfo(30010232, (long)ownskillid, 0L);
      SecondaryStatEffect effect = SkillFactory.getSkill(skillid).getEffect(this.getTotalSkillLevel(skillid));
      EnumMap<SecondaryStat, Pair<Integer, Integer>> statups = new EnumMap(SecondaryStat.class);
      statups.put(SecondaryStat.ExceedOverload, new Pair(1, 10000));
      this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, effect, this));
   }

   public int getExceed() {
      return this.exceed;
   }

   public void setExceed(short amount) {
      this.exceed = amount;
      if (this.getSkillLevel(31220044) > 0 && this.exceed > 18) {
         this.exceed = 18;
      }

   }

   public void gainExceed(short amount) {
      this.exceed += amount;
      if (this.getSkillLevel(31220044) > 0 && this.exceed >= 18) {
         this.exceed = 18;
      }

      this.updateExceed(this.exceed);
   }

   public void updateExceed(int amount) {
      if (amount > 0) {
         SkillFactory.getSkill(30010230).getEffect(1).applyTo(this);
      }

   }

   public void setLuminusMorph(boolean morph) {
      this.luminusMorph = morph;
   }

   public boolean getLuminusMorph() {
      return this.luminusMorph;
   }

   public void setLuminusMorphUse(int use) {
      this.lumimorphuse = use;
   }

   public int getLuminusMorphUse() {
      return this.lumimorphuse;
   }

   public int getForcingItem() {
      return this.forcingItem;
   }

   public void setForcingItem(short forcingItem) {
      this.forcingItem = forcingItem;
   }

   public long getCooldownLimit(int skillid) {
      Iterator var2 = this.getCooldowns().iterator();

      MapleCoolDownValueHolder mcdvh;
      do {
         if (!var2.hasNext()) {
            return 0L;
         }

         mcdvh = (MapleCoolDownValueHolder)var2.next();
      } while(mcdvh.skillId != skillid);

      return mcdvh.length - (System.currentTimeMillis() - mcdvh.startTime);
   }

   public Long getBuffedStarttime(SecondaryStat effect, int skillid) {
      SecondaryStatValueHolder mbsvh = this.checkBuffStatValueHolder(effect);
      if (mbsvh == null) {
         return null;
      } else {
         if (skillid == -1) {
            if (mbsvh.effect != null) {
               return mbsvh.startTime;
            }
         } else {
            Iterator var4 = this.getEffects().iterator();

            while(var4.hasNext()) {
               Pair<SecondaryStat, SecondaryStatValueHolder> buff = (Pair)var4.next();
               if (((SecondaryStatValueHolder)buff.right).effect.getSourceId() == skillid) {
                  return ((SecondaryStatValueHolder)buff.getRight()).startTime;
               }
            }
         }

         return null;
      }
   }

   public long getBuffLimit(int skillid) {
      Iterator var2 = this.effects.iterator();

      Pair mcdvh;
      do {
         if (!var2.hasNext()) {
            return 0L;
         }

         mcdvh = (Pair)var2.next();
      } while(((SecondaryStatValueHolder)mcdvh.right).effect.getSourceId() != skillid);

      return (long)((SecondaryStatValueHolder)mcdvh.right).localDuration - (System.currentTimeMillis() - ((SecondaryStatValueHolder)mcdvh.right).startTime);
   }

   public long getBuffLimit(SecondaryStat s, int skillid) {
      Iterator var3 = this.effects.iterator();

      Pair mcdvh;
      do {
         if (!var3.hasNext()) {
            return 0L;
         }

         mcdvh = (Pair)var3.next();
      } while(s != mcdvh.left || ((SecondaryStatValueHolder)mcdvh.right).effect.getSourceId() != skillid);

      return (long)((SecondaryStatValueHolder)mcdvh.right).localDuration - (System.currentTimeMillis() - ((SecondaryStatValueHolder)mcdvh.right).startTime);
   }

   public void setFishing(boolean a) {
      this.fishing = a;
   }

   public boolean Fishing() {
      return this.fishing;
   }

   public byte getWolfScore() {
      return this.wolfscore;
   }

   public void setWolfScore(byte farmscore) {
      this.wolfscore = farmscore;
   }

   public byte getSheepScore() {
      return this.sheepscore;
   }

   public void setSheepScore(byte farmscore) {
      this.sheepscore = farmscore;
   }

   public void addWolfScore() {
      ++this.wolfscore;
   }

   public void addSheepScore() {
      --this.sheepscore;
   }

   public byte getPandoraBoxFever() {
      return this.pandoraBoxFever;
   }

   public void setPandoraBoxFever(byte pandoraBoxFever) {
      this.pandoraBoxFever = pandoraBoxFever;
   }

   public void addPandoraBoxFever(byte pandoraBoxFever) {
      this.pandoraBoxFever += pandoraBoxFever;
   }

   public void handleKaiserCombo(int skillid) {
      if (this.getKaiserCombo() < 1000) {
         int count = Randomizer.rand(2, 3);
         switch(skillid) {
         case 61101002:
         case 61110211:
            count = 6;
            break;
         case 61111100:
            count = 1;
            break;
         case 61120007:
         case 61121217:
         case 400011058:
         case 400011059:
         case 400011060:
         case 400011061:
            count = 8;
            break;
         case 61121052:
         case 61121105:
         case 61121222:
            count = 35;
            break;
         case 400011118:
         case 400011119:
         case 400011130:
            count = 0;
         }

         this.setKaiserCombo((short)(this.getKaiserCombo() + count));
      }

      SkillFactory.getSkill(61111008).getEffect(1).applyKaiserCombo(this, this.getKaiserCombo());
   }

   public void resetKaiserCombo() {
      this.setKaiserCombo((short)0);
      SkillFactory.getSkill(61111008).getEffect(1).applyKaiserCombo(this, this.getKaiserCombo());
   }

   public List<Core> getCore() {
      return this.cores;
   }

   public int getBaseColor() {
      return this.basecolor;
   }

   public void setBaseColor(int basecolor) {
      this.basecolor = basecolor;
   }

   public int getAddColor() {
      return this.addcolor;
   }

   public void setAddColor(int addcolor) {
      this.addcolor = addcolor;
   }

   public int getBaseProb() {
      return this.baseprob;
   }

   public void setBaseProb(int baseprob) {
      this.baseprob = baseprob;
   }

   public int getSecondBaseColor() {
      return this.secondbasecolor;
   }

   public void setSecondBaseColor(int basecolor) {
      this.secondbasecolor = basecolor;
   }

   public int getSecondAddColor() {
      return this.secondaddcolor;
   }

   public void setSecondAddColor(int addcolor) {
      this.secondaddcolor = addcolor;
   }

   public int getSecondBaseProb() {
      return this.secondbaseprob;
   }

   public void setSecondBaseProb(int baseprob) {
      this.secondbaseprob = baseprob;
   }

   public List<Integer> getCashWishList() {
      return this.cashwishlist;
   }

   public void addCashWishList(int id) {
      this.cashwishlist.add(id);
   }

   public void removeCashWishList(int id) {
      this.cashwishlist.remove(id);
   }

   public void giveHoyoungGauge(int skillid) {
      Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
      Skill sk = SkillFactory.getSkill(skillid);
      if (sk != null) {
         SecondaryStatEffect effect = SkillFactory.getSkill(164000010).getEffect(1);
         SecondaryStatEffect advance = null;
         if (this.job == 16411 || this.job == 16412) {
            advance = SkillFactory.getSkill(164110014).getEffect(1);
         }

         int max = true;
         int add = 0;
         switch(skillid) {
         case 164001000:
         case 164121003:
         case 400041066:
            if (this.useIn) {
               add = 0;
            } else {
               this.useIn = true;
               add = effect.getW();
            }

            this.scrollGauge += 15;
            this.energy += 10;
            break;
         case 164001001:
         case 164101003:
         case 164111007:
         case 164121006:
            this.energy = 0;
            if (advance != null) {
               this.scrollGauge = Math.min(this.scrollGauge + 200, 900);
            }
            break;
         case 164101000:
         case 164111003:
         case 400041065:
            if (this.useJi) {
               add = 0;
            } else {
               this.useJi = true;
               add = effect.getV();
            }

            this.scrollGauge += 15;
            this.energy += 10;
            break;
         case 164111000:
         case 164121000:
         case 400041064:
            if (this.useChun) {
               add = 0;
            } else {
               this.useChun = true;
               add = effect.getU();
            }

            this.scrollGauge += 15;
            this.energy += 10;
            break;
         case 164111008:
         case 164121007:
         case 164121008:
         case 400041050:
            this.scrollGauge = 0;
         }

         this.energy = Math.min(100, this.energy + add);
         if (this.useChun && this.useJi && this.useIn) {
            this.useChun = false;
            this.useJi = false;
            this.useIn = false;
            this.addHP(this.getStat().getCurrentMaxHp() / 100L * 3L);
            this.addMP(this.getStat().getCurrentMaxMp(this) / 100L * 3L);
            if (this.getSkillCustomValue(400041051) == null) {
               label99: {
                  Iterator var8 = this.getMap().getAllSummonsThreadsafe().iterator();

                  MapleSummon s;
                  do {
                     do {
                        if (!var8.hasNext()) {
                           break label99;
                        }

                        s = (MapleSummon)var8.next();
                     } while(s.getOwner().getId() != this.getId());
                  } while(s.getSkill() != 400041050 && s.getSkill() != 400041051);

                  this.getClient().getSession().writeAndFlush(CField.SummonPacket.summonRangeAttack(s, 400041051));
                  this.setSkillCustomInfo(400041051, 0L, 3000L);
               }
            }

            if (this.getBuffedEffect(SecondaryStat.SageElementalClone) != null) {
               List<Integer> skilllist = new ArrayList();
               skilllist.add(164111000);
               skilllist.add(164121000);
               skilllist.add(164101000);
               skilllist.add(164111003);
               skilllist.add(164001000);
               skilllist.add(164121003);
               Collections.addAll(skilllist, new Integer[0]);
               Collections.shuffle(skilllist);
               Iterator var12 = skilllist.iterator();

               while(var12.hasNext()) {
                  Integer skill = (Integer)var12.next();
                  if (this.skillisCooling(skill)) {
                     this.removeCooldown(skill);
                     break;
                  }
               }
            }
         }

         statups.put(SecondaryStat.HoyoungThirdProperty, new Pair(this.useChun ? 1 : 0, 0));
         this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, (SecondaryStatEffect)null, this));
         statups.clear();
         statups.put(SecondaryStat.TidalForce, new Pair(this.energy, 0));
         this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, (SecondaryStatEffect)null, this));
      }

   }

   public long getLastCreationTime() {
      return this.lastCreationTime;
   }

   public void setLastCreationTime(long time) {
      this.lastCreationTime = time;
   }

   public void 에테르핸들러(MapleCharacter chr, int gain, int skillid, boolean refresh) {
      if (refresh) {
         if (chr.에테르소드 > 0 && chr.에테르소드 <= 2) {
            chr.getMap().broadcastMessage(SkillPacket.CreateSworldReadyObtacle(chr, 15112, 2));
         } else if (chr.에테르소드 > 0 && chr.에테르소드 <= 4) {
            chr.getMap().broadcastMessage(SkillPacket.CreateSworldReadyObtacle(chr, 15112, 2));
            chr.getMap().broadcastMessage(SkillPacket.CreateSworldReadyObtacle(chr, 15112, 4));
         } else if (chr.에테르소드 > 0 && chr.에테르소드 <= 6) {
            chr.getMap().broadcastMessage(SkillPacket.CreateSworldReadyObtacle(chr, 15112, 2));
            chr.getMap().broadcastMessage(SkillPacket.CreateSworldReadyObtacle(chr, 15112, 4));
            chr.getMap().broadcastMessage(SkillPacket.CreateSworldReadyObtacle(chr, 15112, 6));
         }
      }

      if (chr.getSkillLevel(151100017) > 0) {
         int max = chr.getSkillLevel(151120012) > 0 ? 400 : 300;
         EnumMap statups;
         SecondaryStatEffect effect;
         if (gain < 0) {
            chr.에테르 += gain;
            statups = new EnumMap(SecondaryStat.class);
            statups.put(SecondaryStat.AdelGauge, new Pair(chr.에테르, 0));
            effect = SkillFactory.getSkill(151100017).getEffect(chr.getTotalSkillLevel(151100017));
            chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, effect, chr));
         } else if (chr.에테르 < max) {
            if (chr.getBuffedValue(400011109)) {
               SecondaryStatEffect effect2 = SkillFactory.getSkill(400011109).getEffect(chr.getTotalSkillLevel(400011109));
               int plus = gain * effect2.getX() / 100;
               gain += plus;
            }

            chr.에테르 += gain;
            if (chr.에테르 >= max) {
               chr.에테르 = max;
            }

            if (chr.에테르 >= 100 && chr.에테르소드 < 2 || chr.에테르 >= 200 && chr.에테르소드 < 4 || chr.에테르 >= 300 && chr.에테르소드 < 6) {
               chr.에테르소드 += 2;
               if (chr.getJob() != 15112 && chr.에테르소드 > 4) {
                  chr.에테르소드 = 4;
               }

               if (chr.getBuffedValue(151101006)) {
                  if (chr.에테르소드 != 1 && chr.에테르소드 >= 0) {
                     chr.getMap().broadcastMessage(SkillPacket.CreateSworldReadyObtacle(chr, 15112, chr.에테르소드));
                  } else {
                     chr.에테르소드 = 2;
                     chr.getMap().broadcastMessage(SkillPacket.CreateSworldReadyObtacle(chr, 15112, 2));
                  }
               }
            }

            statups = new EnumMap(SecondaryStat.class);
            statups.put(SecondaryStat.AdelGauge, new Pair(chr.에테르, 0));
            effect = SkillFactory.getSkill(151100017).getEffect(chr.getTotalSkillLevel(151100017));
            chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, effect, chr));
         }
      }

   }

   public void 에테르결정(MapleCharacter chr, Point pos, boolean nocooltime) {
      int size = 0;
      boolean spawn = true;
      Iterator var6 = (new ArrayList(chr.summons)).iterator();

      MapleSummon summon;
      while(var6.hasNext()) {
         summon = (MapleSummon)var6.next();
         if (summon.getSkill() == 151100002 && summon.getPosition().x - 350 <= pos.x && summon.getPosition().x + 350 >= pos.x && summon.getPosition().y - 70 <= pos.y && summon.getPosition().y + 70 >= pos.y) {
            spawn = false;
         }
      }

      if ((chr.getSkillCustomValue(151100002) == null || nocooltime) && spawn) {
         var6 = (new ArrayList(chr.summons)).iterator();

         while(var6.hasNext()) {
            summon = (MapleSummon)var6.next();
            if (summon.getSkill() == 151100002) {
               ++size;
            }

            if (size > 6) {
               summon.removeSummon(chr.getMap(), false);
            }
         }

         MapleSummon tosummon = new MapleSummon(chr, 151100002, pos, SummonMovementType.STATIONARY, (byte)0, 30000);
         tosummon.setPosition(pos);
         chr.addSummon(tosummon);
         tosummon.addHP(10000);
         chr.getMap().spawnSummon(tosummon, 30000);
         if (!nocooltime) {
            chr.setSkillCustomInfo(151100002, 0L, 4000L);
         }
      }

   }

   public static void 렐릭게이지(MapleClient c, int skillid) {
      Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
      if (c.getPlayer().렐릭게이지 <= 1000 && c.getPlayer().getSkillCustomValue(3321035) == null) {
         MapleCharacter player19;
         MapleCharacter player21;
         switch(skillid) {
         case 3011004:
         case 3300002:
         case 3321003:
            if (c.getPlayer().getSkillLevel(3320000) < 1) {
               player19 = c.getPlayer();
               player19.렐릭게이지 += 5;
               if (c.getPlayer().렐릭게이지 < 1000) {
                  player21 = c.getPlayer();
                  player21.에인션트가이던스 += 5;
               }
            } else {
               player19 = c.getPlayer();
               player19.렐릭게이지 += 10;
               if (c.getPlayer().렐릭게이지 < 1000) {
                  player21 = c.getPlayer();
                  player21.에인션트가이던스 += 10;
               }
            }
            break;
         case 3301003:
         case 3301004:
         case 3321004:
         case 3321005:
            if (c.getPlayer().getSkillLevel(3320000) < 1) {
               player19 = c.getPlayer();
               player19.렐릭게이지 += 10;
               if (c.getPlayer().렐릭게이지 < 1000) {
                  player21 = c.getPlayer();
                  player21.에인션트가이던스 += 10;
               }
            } else {
               player19 = c.getPlayer();
               player19.렐릭게이지 += 20;
               if (c.getPlayer().렐릭게이지 < 1000) {
                  player21 = c.getPlayer();
                  player21.에인션트가이던스 += 20;
               }
            }
            break;
         case 3301008:
         case 3311010:
            player19 = c.getPlayer();
            player19.렐릭게이지 -= 50;
            break;
         case 3311002:
         case 3311003:
         case 3321006:
         case 3321007:
            if (c.getPlayer().getSkillLevel(3320000) < 1) {
               player19 = c.getPlayer();
               player19.렐릭게이지 += 10;
               if (c.getPlayer().렐릭게이지 < 1000) {
                  player21 = c.getPlayer();
                  player21.에인션트가이던스 += 10;
               }
            } else {
               player19 = c.getPlayer();
               player19.렐릭게이지 += 20;
               if (c.getPlayer().렐릭게이지 < 1000) {
                  player21 = c.getPlayer();
                  player21.에인션트가이던스 += 20;
               }
            }
            break;
         case 3311009:
            if (c.getPlayer().getSkillLevel(3320000) > 0) {
               player19 = c.getPlayer();
               player19.렐릭게이지 += 10;
               if (c.getPlayer().렐릭게이지 < 1000) {
                  player21 = c.getPlayer();
                  player21.에인션트가이던스 += 10;
               }
            }
            break;
         case 3321012:
            player19 = c.getPlayer();
            player19.렐릭게이지 -= 100;
            break;
         case 3321015:
         case 3321017:
         case 3321019:
         case 3321021:
            player19 = c.getPlayer();
            player19.렐릭게이지 -= 150;
            break;
         case 3321035:
         case 3321036:
         case 3321037:
         case 3321038:
         case 3321039:
         case 3321040:
            player19 = c.getPlayer();
            player19.렐릭게이지 -= 65;
            c.getPlayer().setSkillCustomInfo(3321035, 0L, 1000L);
            break;
         case 400031034:
            c.getPlayer().렐릭게이지 = 0;
            break;
         case 400031036:
            if (c.getPlayer().getSkillCustomValue(400031036) != 0L) {
               player19 = c.getPlayer();
               player19.렐릭게이지 -= 300;
               c.getPlayer().setSkillCustomInfo(400031036, 0L, 0L);
            } else {
               player19 = c.getPlayer();
               player19.렐릭게이지 += 10;
               if (c.getPlayer().렐릭게이지 < 1000) {
                  player21 = c.getPlayer();
                  player21.에인션트가이던스 += 10;
               }
            }
            break;
         case 400031037:
         case 400031038:
         case 400031039:
         case 400031040:
            player19 = c.getPlayer();
            player19.렐릭게이지 -= 500;
            break;
         case 400031047:
         case 400031049:
         case 400031051:
            player19 = c.getPlayer();
            player19.렐릭게이지 -= 250;
            break;
         case 400031067:
            if (c.getPlayer().getSkillCustomValue(400031067) != 0L) {
               player19 = c.getPlayer();
               player19.렐릭게이지 -= 300;
               c.getPlayer().setSkillCustomInfo(400031067, 0L, 0L);
            } else {
               player19 = c.getPlayer();
               player19.렐릭게이지 += 10;
               if (c.getPlayer().렐릭게이지 < 1000) {
                  player21 = c.getPlayer();
                  player21.에인션트가이던스 += 10;
               }
            }
         }
      }

      if (c.getPlayer().렐릭게이지 >= 1000) {
         c.getPlayer().렐릭게이지 = 1000;
      } else if (c.getPlayer().렐릭게이지 <= 0) {
         c.getPlayer().렐릭게이지 = 0;
      }

      if (c.getPlayer().에인션트가이던스 >= 1000) {
         c.getPlayer().에인션트가이던스 = 0;
         SkillFactory.getSkill(3310006).getEffect(c.getPlayer().getSkillLevel(3310006)).applyTo(c.getPlayer());
      }

      statups.put(SecondaryStat.RelikGauge, new Pair(c.getPlayer().렐릭게이지, 0));
      c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, (SecondaryStatEffect)null, c.getPlayer()));
   }

   public static void 문양(MapleClient c, int skillid) {
      Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
      int 이전문양 = c.getPlayer().문양;
      switch(skillid) {
      case 3011004:
      case 3300002:
      case 3321003:
         c.getPlayer().문양 = 1;
         break;
      case 3311002:
      case 3311003:
      case 3321006:
      case 3321007:
         c.getPlayer().문양 = 3;
         break;
      case 3321005:
         c.getPlayer().문양 = 2;
         break;
      case 3321014:
      case 3321015:
      case 3321016:
      case 3321017:
      case 3321018:
      case 3321019:
      case 3321020:
      case 3321021:
      case 3321035:
      case 3321036:
      case 3321037:
      case 3321038:
      case 3321039:
      case 3321040:
      case 400031034:
      case 400031037:
      case 400031038:
      case 400031039:
      case 400031040:
      case 400031047:
      case 400031049:
      case 400031051:
         c.getPlayer().문양 = 0;
      }

      statups.put(SecondaryStat.CardinalMark, new Pair(c.getPlayer().문양, 0));
      if (이전문양 == 3 && 이전문양 != c.getPlayer().문양 && c.getPlayer().getSkillLevel(3320008) > 0) {
         c.getPlayer().setSkillCustomInfo(3320008, 6L, 0L);
         SkillFactory.getSkill(3320008).getEffect(c.getPlayer().getSkillLevel(3320008)).applyTo(c.getPlayer(), 7000);
      }

      if (이전문양 != c.getPlayer().문양) {
         int 쿨감량 = c.getPlayer().getSkillLevel(3320000) > 0 ? 1000 : 500;
         int targetskill = 0;

         for(int i = 0; i < 5; ++i) {
            switch(i) {
            case 0:
               targetskill = 3321036;
               break;
            case 1:
               targetskill = 3321014;
               break;
            case 2:
               targetskill = 3321012;
               break;
            case 3:
               targetskill = 3301008;
               break;
            case 4:
               targetskill = 3311010;
            }

            if (c.getPlayer().skillisCooling(targetskill)) {
               c.getPlayer().changeCooldown(targetskill, -쿨감량);
            }
         }
      }

      c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, (SecondaryStatEffect)null, c.getPlayer()));
   }

   public void SummonChakriHandler(MapleCharacter chr, Point pos, boolean nocooltime) {
      boolean spawn = true;
      Iterator var5 = (new ArrayList(chr.summons)).iterator();

      MapleSummon summon;
      while(var5.hasNext()) {
         summon = (MapleSummon)var5.next();
         if (summon.getSkill() == 154110010 && summon.getPosition().x - 350 <= pos.x && summon.getPosition().x + 350 >= pos.x && summon.getPosition().y - 70 <= pos.y && summon.getPosition().y + 70 >= pos.y) {
            spawn = false;
         }
      }

      if ((chr.getSkillCustomValue(154110010) == null || nocooltime) && spawn) {
         var5 = (new ArrayList(chr.summons)).iterator();

         while(true) {
            while(var5.hasNext()) {
               summon = (MapleSummon)var5.next();
               if (summon.getSkill() == 154110010) {
                  ++this.SummonChakriStack;
                  System.out.println("SummonChakriStack : " + this.SummonChakriStack);
               }

               if (this.SummonChakriStack > 6 && chr.getBuffedValue(SecondaryStat.ResonateUltimatum) == null) {
                  this.SummonChakriStack = 0;
                  summon.removeSummon(chr.getMap(), false);
               } else if (this.SummonChakriStack > 8) {
                  summon.removeSummon(chr.getMap(), false);
                  this.SummonChakriStack = 0;
                  System.out.println("SummonChakriStack removed: " + this.SummonChakriStack);
               }
            }

            MapleSummon tosummon = new MapleSummon(chr, 154110010, pos, SummonMovementType.STATIONARY, (byte)0, 30000);
            tosummon.setPosition(pos);
            chr.addSummon(tosummon);
            tosummon.addHP(10000);
            chr.getMap().spawnSummon(tosummon, 30000);
            if (!nocooltime) {
               chr.setSkillCustomInfo(154110010, 0L, 4000L);
            }
            break;
         }
      }

   }

   public void handleRemainIncense(int skillid, boolean install1) {
      MapleCharacter chr = this;
      if (this.getBuffedValue(63111009)) {
         int createcount = skillid != 63001100 && skillid != 63101100 && skillid != 400031061 ? (skillid != 63101104 && skillid != 400031064 ? (skillid == 63111105 ? 6 : (skillid != 63121102 && skillid != 63121103 && skillid != 63121141 ? 0 : 8)) : 2) : 1;
         SecondaryStatEffect effect2 = SkillFactory.getSkill(63111009).getEffect(this.getSkillLevel(63111009));
         int install2 = 0;
         List<MapleMagicWreck> remove = new ArrayList();
         Iterator var8 = this.getMap().getWrecks().iterator();

         while(true) {
            while(true) {
               MapleMagicWreck mw;
               do {
                  do {
                     if (!var8.hasNext()) {
                        if (install2 > 0 && !SkillFactory.getSkill(63111009).getSkillList2().contains(skillid)) {
                           if (install2 > 0) {
                              chr.getMap().broadcastMessage(CField.getWreckAttack(chr, remove));
                              var8 = remove.iterator();

                              while(var8.hasNext()) {
                                 mw = (MapleMagicWreck)var8.next();
                                 chr.getMap().RemoveMagicWreck(mw);
                              }
                           }
                        } else if (!install1 && !chr.skillisCooling(63111009) && createcount > 0) {
                           Rectangle bounds2 = SkillFactory.getSkill(63111010).getEffect(chr.getSkillLevel(63111010)).calculateBoundingBox(chr.getTruePosition(), chr.isFacingLeft());

                           for(int i = 0; i < createcount; ++i) {
                              MapleFoothold fh = null;
                              int i2 = 0;

                              while(fh == null) {
                                 fh = (MapleFoothold)chr.getMap().getFootholds().getAllRelevants().get(Randomizer.rand(0, chr.getMap().getFootholds().getAllRelevants().size() - 1));
                                 if (!bounds2.contains(fh.getPoint1()) && !bounds2.contains(fh.getPoint2())) {
                                    fh = null;
                                 }

                                 ++i2;
                                 if (i2 >= 100) {
                                    fh = (MapleFoothold)chr.getMap().getFootholds().getAllRelevants().get(Randomizer.rand(0, chr.getMap().getFootholds().getAllRelevants().size() - 1));
                                    break;
                                 }
                              }

                              if (fh != null) {
                                 MapleMagicWreck mw2 = new MapleMagicWreck(chr, 63111010, new Point(Randomizer.rand(fh.getPoint1().x, fh.getPoint2().x), fh.getPoint1().y), effect2.getW() * 1000);
                                 chr.getMap().spawnMagicWreck(mw2);
                              }
                           }

                           chr.addCooldown(63111009, System.currentTimeMillis(), 300L);
                           return;
                        }

                        return;
                     }

                     mw = (MapleMagicWreck)var8.next();
                  } while(mw.getChr().getId() != chr.getId());
               } while(mw.getSourceid() != 63111010);

               ++install2;
               Rectangle bounds = SkillFactory.getSkill(63111010).getEffect(chr.getSkillLevel(63111010)).calculateBoundingBox(mw.getTruePosition(), chr.isFacingLeft());
               Iterator var11 = chr.getMap().getAllMonster().iterator();

               while(var11.hasNext()) {
                  MapleMonster monster2 = (MapleMonster)var11.next();
                  if (bounds.contains(monster2.getPosition())) {
                     remove.add(mw);
                     break;
                  }
               }
            }
         }
      }
   }

   public void handlePossession(int type) {
      SecondaryStatEffect effect = SkillFactory.getSkill(63101001).getEffect(this.getSkillLevel(63101001));
      Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
      boolean givebuff = true;
      if (this.getSkillLevel(63120000) > 0) {
         effect = SkillFactory.getSkill(63120000).getEffect(this.getSkillLevel(63120000));
      }

      int add = type == 1 ? effect.getY() : (type == 2 ? effect.getX() : -100);
      if (this.getBuffedValue(400031062)) {
         add = type == 1 ? this.getBuffedEffect(400031062).getY() : (type == 2 ? this.getBuffedEffect(400031062).getX() : -100);
      }

      if (this.getSkillCustomValue0(63101001) == (long)((effect.getV() + 1) * 100) && type != 3) {
         givebuff = false;
      }

      if (givebuff) {
         this.addSkillCustomInfo(63101001, (long)add);
         if (this.getSkillCustomValue0(63101001) > (long)((effect.getV() + 1) * 100)) {
            this.setSkillCustomInfo(63101001, (long)((effect.getV() + 1) * 100), 0L);
         } else if (this.getSkillCustomValue0(63101001) < 0L) {
            this.setSkillCustomInfo(63101001, 0L, 0L);
         }

         statups.put(SecondaryStat.Malice, new Pair((int)this.getSkillCustomValue0(63101001), 0));
         this.getClient().send(CWvsContext.BuffPacket.giveBuff(statups, (SecondaryStatEffect)null, this));
      }

   }

   public void handleStackskill(int skillid, boolean use) {
      SecondaryStatEffect effect = SkillFactory.getSkill(skillid).getEffect(this.getSkillLevel(skillid));
      if (use) {
         this.addSkillCustomInfo(skillid, -1L);
         this.setSkillCustomInfo(skillid - 1, 0L, (long)(effect.getU() * 1000));
      } else if (this.getSkillCustomValue(skillid - 1) == null && this.getSkillCustomValue0(skillid) < (long)effect.getW()) {
         this.addSkillCustomInfo(skillid, 1L);
         this.setSkillCustomInfo(skillid - 1, 0L, (long)(effect.getU() * 1000));
      }

      this.getClient().send(CField.getScatteringShot(effect.getSourceId(), (int)this.getSkillCustomValue0(skillid), effect.getW(), effect.getU() * 1000));
   }

   public boolean handleCainSkillCooldown(int skillid) {
      boolean cool = true;
      int cskillid = 0;
      switch(skillid) {
      case 63100100:
         cskillid = 63100002;
         break;
      case 63101100:
         cskillid = 63110001;
         break;
      case 63101104:
         cskillid = 63101004;
         break;
      case 63110103:
      case 63111103:
         cskillid = 63111003;
         break;
      case 63121041:
         return false;
      case 63121102:
      case 63121103:
         cskillid = 63121002;
         break;
      case 63121141:
         cskillid = 63121040;
      }

      if (cskillid > 0) {
         if (this.getBuffedEffect(63101001) != null) {
            this.cancelEffect(this.getBuffedEffect(63101001));
         }

         cool = false;
         int cooltime = SkillFactory.getSkill(cskillid).getEffect(this.getSkillLevel(cskillid)).getCooldown(this);
         if (cooltime == 0) {
            cooltime = SkillFactory.getSkill(skillid).getEffect(this.getSkillLevel(cskillid)).getCooldown(this);
         }

         if (!this.skillisCooling(skillid) && !this.skillisCooling(cskillid)) {
            this.addCooldown(skillid, System.currentTimeMillis(), (long)cooltime);
            this.getClient().getSession().writeAndFlush(CField.skillCooldown(skillid, cooltime));
         }
      }

      return cool;
   }

   public void handleNatureFriend() {
      int skilllv = this.getSkillLevel(80003070) > 0 ? this.getSkillLevel(80003070) : this.getSkillLevel(160010001);
      int skillld = this.getSkillLevel(80003070) > 0 ? 80003058 : 160010001;
      if (!this.skillisCooling(skillld)) {
         SecondaryStatEffect effect = SkillFactory.getSkill(skillld).getEffect(skilllv);
         if (!this.getBuffedValue(80003070)) {
            this.setSkillCustomInfo(80003070, 0L, 0L);
         } else {
            this.addSkillCustomInfo(80003070, 1L);
         }

         if (this.getSkillCustomValue0(80003070) >= (long)effect.getX()) {
            this.addCooldown(skillld, System.currentTimeMillis(), (long)effect.getCooldown(this));
            this.getClient().getSession().writeAndFlush(CField.skillCooldown(skillld, effect.getCooldown(this)));
            this.cancelEffect(this.getBuffedEffect(80003070));
            SkillFactory.getSkill(skillld).getEffect(skilllv).applyTo(this);
         } else {
            SkillFactory.getSkill(80003070).getEffect(skilllv).applyTo(this);
         }

      }
   }

   public void addCoolTime(int skillid, int time) {
      this.addCooldown(skillid, System.currentTimeMillis(), (long)time);
      this.getClient().getSession().writeAndFlush(CField.skillCooldown(skillid, time));
   }

   public void handleSerpent(int type) {
      if (type == 1) {
         this.cancelEffectFromBuffStat(SecondaryStat.SerpentScrew, 400051015);
      }

   }

   public void handlePriorPrepaRation(int skillid, int type) {
      SecondaryStatEffect effect = SkillFactory.getSkill(skillid).getEffect(this.getSkillLevel(skillid));
      if (!this.getBuffedValue(skillid) && !this.skillisCooling(skillid)) {
         if (type == 1) {
            this.addSkillCustomInfo(80003016, 1L);
            if (this.getSkillCustomValue0(80003016) >= (long)effect.getX()) {
               this.removeSkillCustomInfo(80003016);
               this.addSkillCustomInfo(80003018, 1L);
               SkillFactory.getSkill(80003018).getEffect(this.getSkillLevel(skillid)).applyTo(this);
            }
         } else if (type == 2) {
            this.addSkillCustomInfo(80003017, 1L);
            if (this.getSkillCustomValue0(80003017) >= (long)effect.getY()) {
               this.removeSkillCustomInfo(80003017);
               this.addSkillCustomInfo(80003018, 1L);
               SkillFactory.getSkill(80003018).getEffect(this.getSkillLevel(skillid)).applyTo(this);
            }
         }

         if (this.getSkillCustomValue0(80003018) >= (long)effect.getW()) {
            this.removeSkillCustomInfo(80003016);
            this.removeSkillCustomInfo(80003017);
            this.removeSkillCustomInfo(80003018);
            this.cancelEffect(this.getBuffedEffect(80003018));
            SkillFactory.getSkill(skillid).getEffect(this.getSkillLevel(skillid)).applyTo(this);
            this.addCooldown(skillid, System.currentTimeMillis(), (long)SkillFactory.getSkill(skillid).getEffect(this.getSkillLevel(skillid)).getCooldown(this));
            this.client.getSession().writeAndFlush(CField.skillCooldown(skillid, SkillFactory.getSkill(skillid).getEffect(this.getSkillLevel(skillid)).getCooldown(this)));
         }
      }

   }

   public int getBullet() {
      return this.bullet;
   }

   public int getCylinderGauge() {
      return this.cylindergauge;
   }

   public void Cylinder(int skillid) {
      int MaxB = this.getJob() == 3712 ? 6 : (this.getJob() == 3711 ? 5 : (this.getJob() == 3710 ? 4 : (this.getJob() == 3700 ? 3 : 0)));
      if (MaxB != 0) {
         if (skillid == 0) {
            this.bullet = MaxB;
            this.cylindergauge = 0;
         } else {
            if (skillid != 37000010 && skillid != 37121004) {
               if (this.bullet > 0 && (skillid == 37111006 || skillid == 400011019 || skillid == 400011103 || skillid == 400011091 || skillid == 400011019 || skillid == 37001004 || skillid == 37000005 || skillid == 37000009 || skillid == 37100008 || skillid >= 37120014 && skillid <= 37120019)) {
                  --this.bullet;
               } else if (this.bullet < MaxB && (skillid == 37100002 || skillid == 37110001 || skillid == 37110004 || skillid == 37101000 || skillid == 37100002 || skillid == 37110001 || skillid == 37110004 || skillid == 37101000)) {
                  if ((skillid == 37100002 || skillid == 37110004 || skillid == 37110001) && this.getSkillLevel(37120011) > 0) {
                     this.bullet += 2;
                  } else {
                     ++this.bullet;
                  }
               }
            } else {
               this.bullet = MaxB;
            }

            if ((this.cylindergauge >= MaxB || skillid != 400011019 && skillid != 37000009 && skillid != 37100008) && (skillid < 37120014 || skillid > 37120019) && skillid != 37111006) {
               if (skillid == 37001002 || skillid == 37000011 || skillid == 37000012 || skillid == 37000013) {
                  this.cylindergauge = 0;
               }
            } else {
               ++this.cylindergauge;
            }
         }

         if (this.getBuffedValue(SecondaryStat.RWOverHeat) != null) {
            this.cylindergauge = 0;
         }

         if (this.cylindergauge > 6) {
            this.cylindergauge = 6;
         } else if (this.cylindergauge < 0) {
            this.cylindergauge = 0;
         }

         if (this.bullet > 6) {
            this.bullet = 6;
         } else if (this.bullet < 0) {
            this.bullet = 0;
         }

         this.CylinderBuff(skillid, false);
         if (skillid == 37001002 || skillid == 37000011 || skillid == 37000012 || skillid == 37000013) {
            SkillFactory.getSkill(skillid).getEffect(this.getSkillLevel(skillid)).applyTo(this);
         }

         if (this.bullet == 0) {
            this.Cylinder(37000010);
         }
      }

   }

   public void CylinderBuff(int skillid, boolean gaugereset) {
      Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
      if (gaugereset) {
         this.cylindergauge = 0;
      }

      if (skillid == 37121004 && !gaugereset) {
         SkillFactory.getSkill(37121004).getEffect(this.getSkillLevel(37121004)).applyTo(this);
      } else if (skillid == 400011103) {
         statups.put(SecondaryStat.RWCylinder, new Pair(1, 0));
         this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, SkillFactory.getSkill(skillid).getEffect(this.getSkillLevel(skillid)), this));
      } else {
         if (skillid == 0) {
            skillid = 37121004;
         }

         statups.put(SecondaryStat.RWCylinder, new Pair(1, 0));
         this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, SkillFactory.getSkill(skillid).getEffect(this.getSkillLevel(skillid)), this));
      }

   }

   public void givePPoint(int skillid) {
      if (GameConstants.isKinesis(this.getJob())) {
         int MaxPpoint = this.getJob() == 14200 ? 10 : (this.getJob() == 14210 ? 15 : (this.getJob() == 14211 ? 20 : 30));
         if (this.getSkillLevel(80000406) > 0) {
            MaxPpoint += this.getSkillLevel(80000406);
         }

         if (skillid <= 30) {
            this.PPoint += skillid;
         } else if (skillid != 142121032) {
            SecondaryStatEffect effects = SkillFactory.getSkill(skillid).getEffect(this.getSkillLevel(skillid));
            if (effects.getPPCon() > 0) {
               if (this.getBuffedValue(SecondaryStat.KinesisPsychicOver) != null) {
                  this.PPoint -= effects.getPPCon() <= 1 ? effects.getPPCon() : effects.getPPCon() / 2;
               } else {
                  this.PPoint -= effects.getPPCon();
               }
            } else if (effects.getPPRecovery() > 0) {
               this.PPoint += effects.getPPRecovery();
            } else if (skillid != 142120003 && skillid != 142101009 && skillid != 142120015 && skillid != 142001001) {
               if (skillid == 142001007) {
                  --this.PPoint;
               } else if (skillid == 400021048) {
                  this.PPoint -= 10;
               } else if (skillid == 142121030) {
                  this.PPoint = MaxPpoint;
               } else if (skillid == 142121008) {
                  this.PPoint += (MaxPpoint - this.PPoint) / 2;
               } else if (skillid == 400021074) {
                  this.PPoint -= 3;
               } else if (skillid == 400001051) {
                  effects = SkillFactory.getSkill(400001050).getEffect(this.getSkillLevel(400001050));
                  int a = 3000 * effects.getY() / 10000;
                  this.PPoint += a;
               }
            } else {
               ++this.PPoint;
            }
         }

         this.givePPoint((byte)0);
      }

   }

   public void givePPoint(byte count) {
      int MaxPpoint = this.getJob() == 14200 ? 10 : (this.getJob() == 14210 ? 15 : (this.getJob() == 14211 ? 20 : 30));
      if (this.getSkillLevel(80000406) > 0) {
         MaxPpoint += this.getSkillLevel(80000406);
      }

      this.PPoint += count;
      if (this.PPoint < 0) {
         this.PPoint = 0;
      }

      if (MaxPpoint < this.PPoint) {
         this.PPoint = MaxPpoint;
      }

      Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
      statups.put(SecondaryStat.KinesisPsychicPoint, new Pair(this.PPoint, 0));
      this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, (SecondaryStatEffect)null, this));
   }

   public byte getDeathCount() {
      return this.deathcount;
   }

   public void setDeathCount(byte de) {
      this.deathcount = de;
      this.getMap().broadcastMessage(CField.setDeathCount(this, this.deathcount));
      if (this.getMapId() == 450010500) {
         this.getClient().getSession().writeAndFlush(CField.JinHillah(3, this, this.getMap()));
      } else if (this.getMapId() != 262031310 && this.getMapId() != 262030310 && this.getMapId() != 262031300 && this.getMapId() != 262030100 && this.getMapId() != 262031100 && this.getMapId() != 262030200 && this.getMapId() != 262031200) {
         this.getClient().getSession().writeAndFlush(CField.getDeathCount(this.getDeathCount()));
      } else if (this.getMapId() == 262031300) {
         int deathcouint = 15 - this.getDeathCount();
         this.getClient().send(CWvsContext.onFieldSetVariable("TotalDeathCount", "15"));
         this.getClient().send(CWvsContext.onFieldSetVariable("DeathCount", deathcouint.makeConcatWithConstants<invokedynamic>(deathcouint)));
         MapleMonster hardHilla = this.getMap().getMonsterById(8870100);
         if (hardHilla != null) {
            hardHilla.addSkillCustomInfo(8877100, 1L);
            if (hardHilla.getCustomValue0(8877100) >= 6L) {
               hardHilla.setCustomInfo(8877100, 6, 0);
            }

            List<Pair<MonsterStatus, MonsterStatusEffect>> stats = new ArrayList();
            stats.add(new Pair(MonsterStatus.MS_HillaCount, new MonsterStatusEffect(114, 210000000, hardHilla.getCustomValue0(8877100))));
            hardHilla.applyMonsterBuff(hardHilla.getMap(), stats, MobSkillFactory.getMobSkill(114, 57));
         }
      }

      if (de > 0) {
         this.getMap().broadcastMessage(CField.showDeathCount(this, this.getDeathCount()));
      }

   }

   public List<Equip> getSymbol() {
      return this.symbol;
   }

   public void setSymbol(List<Equip> symbol) {
      this.symbol = symbol;
   }

   public List<Triple<Skill, SkillEntry, Integer>> getLinkSkills() {
      return this.linkskills;
   }

   public List<Pair<SecondaryStat, SecondaryStatValueHolder>> getEffects() {
      return this.effects;
   }

   public void setEffects(List<Pair<SecondaryStat, SecondaryStatValueHolder>> effects) {
      this.effects = effects;
   }

   public void elementalChargeHandler(int attack, int count) {
      if (this.elementalCharge < 5) {
         this.elementalCharge += count;
      }

      this.lastElementalCharge = attack;
      Skill skill = SkillFactory.getSkill(1200014);
      int skillLevel = this.getTotalSkillLevel(skill);
      SecondaryStatEffect effect = this.getBuffedEffect(SecondaryStat.ElementalCharge);
      if (effect == null) {
         effect = skill.getEffect(skillLevel);
      }

      if (effect.getSourceId() != 1220010 && this.getSkillLevel(1220010) > 0) {
         effect = SkillFactory.getSkill(1220010).getEffect(this.getSkillLevel(1220010));
      }

      effect.applyTo(this);
      if (this.getSkillLevel(400011052) > 0) {
         SkillFactory.getSkill(400011052).getEffect(this.getSkillLevel(400011052)).applyTo(this, false);
      }

   }

   public int getMparkexp() {
      return this.mparkexp;
   }

   public void setMparkexp(int mparkexp) {
      this.mparkexp = mparkexp;
   }

   public int getMparkcount() {
      return this.mparkcount;
   }

   public void setMparkcount(int mparkcount) {
      this.mparkcount = mparkcount;
   }

   public int getMparkkillcount() {
      return this.mparkkillcount;
   }

   public void setMparkkillcount(int mparkkillcount) {
      this.mparkkillcount = mparkkillcount;
   }

   public boolean isMparkCharged() {
      return this.mparkCharged;
   }

   public void setMparkCharged(boolean mparkCharged) {
      this.mparkCharged = mparkCharged;
   }

   public void removeKeyValue(int type, String key) {
      String questInfo = this.getInfoQuest(type);
      if (questInfo != null) {
         String[] split = questInfo.split(";");
         String[] var6 = split;
         int var7 = split.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            String s = var6[var8];
            if (s.startsWith(key + "=")) {
               String newkey = questInfo.replace(s + ";", "");
               this.updateInfoQuest(type, newkey);
               return;
            }
         }

         this.updateInfoQuest(type, questInfo);
      }
   }

   public void removeKeyValue(int type) {
      MapleQuest quest = MapleQuest.getInstance(type);
      if (quest != null) {
         this.updateInfoQuest(type, "");
         this.questinfo.remove(quest);
         Connection con = null;
         PreparedStatement ps = null;

         try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("DELETE FROM questinfo WHERE characterid = " + this.getId() + " AND quest = ?");
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

   public void setKeyValue(String key, String value) {
      this.keyValues.put(key, value);
   }

   public void setKeyValue(int type, String key, String value) {
      String questInfo = this.getInfoQuest(type);
      if (questInfo == null) {
         this.updateInfoQuest(type, key + "=" + value + ";");
      } else {
         String[] split = questInfo.split(";");
         String[] var7 = split;
         int var8 = split.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            String s = var7[var9];
            if (s.startsWith(key + "=")) {
               String newkey = questInfo.replace(s, key + "=" + value);
               this.updateInfoQuest(type, newkey);
               return;
            }
         }

         this.updateInfoQuest(type, questInfo + key + "=" + value + ";");
      }
   }

   public String getKeyValueStr(int type, String key) {
      String questInfo = this.getInfoQuest(type);
      if (questInfo == null) {
         return null;
      } else {
         String[] split = questInfo.split(";");
         String[] var6 = split;
         int var7 = split.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            String s = var6[var8];
            if (s.startsWith(key + "=")) {
               String newkey = s.replace(key + "=", "");
               String newkey2 = newkey.replace(";", "");
               return newkey2;
            }
         }

         return null;
      }
   }

   public String getKeyValue(String key) {
      return this.keyValues.containsKey(key) ? (String)this.keyValues.get(key) : null;
   }

   public long getKeyValue(int type, String key) {
      String questInfo = this.getInfoQuest(type);
      if (questInfo == null) {
         return -1L;
      } else {
         String[] split = questInfo.split(";");
         String[] var6 = split;
         int var7 = split.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            String s = var6[var8];
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

   public int getBHGCCount() {
      return this.BHGCCount;
   }

   public void setBHGCCount(int bHGCCount) {
      this.BHGCCount = bHGCCount;
   }

   public int getHowlingGaleCount() {
      return this.HowlingGaleCount;
   }

   public void setHowlingGaleCount(int howlingGaleCount) {
      this.HowlingGaleCount = howlingGaleCount;
   }

   public List<Item> getAuctionitems() {
      return this.auctionitems;
   }

   public void setAuctionitems(List<Item> auctionitems) {
      this.auctionitems = auctionitems;
   }

   public int getSlowAttackCount() {
      return this.slowAttackCount;
   }

   public void setSlowAttackCount(int slowAttackCount) {
      this.slowAttackCount = slowAttackCount;
   }

   public int getYoyoCount() {
      return this.YoyoCount;
   }

   public void setYoyoCount(int yoyoCount) {
      this.YoyoCount = yoyoCount;
   }

   public MapleHaku getHaku() {
      return this.haku;
   }

   public void setHaku(MapleHaku haku) {
      this.haku = haku;
   }

   public int getListonation() {
      return this.listonation;
   }

   public void setListonation(int listonation) {
      this.listonation = listonation;
   }

   public int getBeholderSkill1() {
      return this.beholderSkill1;
   }

   public void setBeholderSkill1(int beholderSkill1) {
      this.beholderSkill1 = beholderSkill1;
   }

   public int getBeholderSkill2() {
      return this.beholderSkill2;
   }

   public void setBeholderSkill2(int beholderSkill2) {
      this.beholderSkill2 = beholderSkill2;
   }

   public int getReinCarnation() {
      return this.reinCarnation;
   }

   public void setReinCarnation(int reinCarnation) {
      this.reinCarnation = reinCarnation;
   }

   public byte getPoisonStack() {
      return this.poisonStack;
   }

   public void setPoisonStack(byte poisonStack) {
      this.poisonStack = poisonStack;
   }

   public byte getInfinity() {
      return this.infinity;
   }

   public void setInfinity(byte infinity) {
      this.infinity = infinity;
   }

   public byte getHolyPountin() {
      return this.holyPountin;
   }

   public void setHolyPountin(byte holyPountin) {
      this.holyPountin = holyPountin;
   }

   public int getHolyPountinOid() {
      return this.holyPountinOid;
   }

   public void setHolyPountinOid(int holyPountinOid) {
      this.holyPountinOid = holyPountinOid;
   }

   public byte getBlessingAnsanble() {
      return this.blessingAnsanble;
   }

   public void setBlessingAnsanble(byte blessingAnsanble) {
      this.blessingAnsanble = blessingAnsanble;
   }

   public byte getQuiverType() {
      return this.quiverType;
   }

   public void setQuiverType(byte quiverType) {
      this.quiverType = quiverType;
   }

   public int[] getRestArrow() {
      return this.RestArrow;
   }

   public int getBarrier() {
      return this.barrier;
   }

   public void setBarrier(int barrier) {
      this.barrier = barrier;
   }

   public byte getFlip() {
      return this.flip;
   }

   public void setFlip(byte flip) {
      this.flip = flip;
   }

   public int getDice() {
      return this.dice;
   }

   public void setDice(int dice) {
      this.dice = dice;
   }

   public byte getUnityofPower() {
      return this.unityofPower;
   }

   public void setUnityofPower(byte unityofPower) {
      this.unityofPower = unityofPower;
   }

   public byte getConcentration() {
      return this.concentration;
   }

   public void setConcentration(byte concentration) {
      this.concentration = concentration;
   }

   public byte getMortalBlow() {
      return this.mortalBlow;
   }

   public void setMortalBlow(byte mortalBlow) {
      this.mortalBlow = mortalBlow;
   }

   public List<MapleMapItem> getPickPocket() {
      return this.pickPocket;
   }

   public void addPickPocket(MapleMapItem pickPocket) {
      this.pickPocket.add(pickPocket);
   }

   public void RemovePickPocket(MapleMapItem pickPocket) {
      this.pickPocket.remove(pickPocket);
   }

   public byte getHolyMagicShell() {
      return this.holyMagicShell;
   }

   public void setHolyMagicShell(byte holyMagicShell) {
      this.holyMagicShell = holyMagicShell;
   }

   public byte getAntiMagicShell() {
      return this.antiMagicShell;
   }

   public void setAntiMagicShell(byte antiMagicShell) {
      this.antiMagicShell = antiMagicShell;
   }

   public byte getBlessofDarkness() {
      return this.blessofDarkness;
   }

   public void setBlessofDarkness(byte blessofDarkness) {
      this.blessofDarkness = blessofDarkness;
   }

   public byte getDeath() {
      return this.death;
   }

   public void setDeath(byte death) {
      this.death = death;
   }

   public byte getRoyalStack() {
      return this.royalStack;
   }

   public void setRoyalStack(byte royalStack) {
      this.royalStack = royalStack;
   }

   public short getKaiserCombo() {
      return this.kaiserCombo;
   }

   public void setKaiserCombo(short kaiserCombo) {
      this.kaiserCombo = (short)Math.min(1000, kaiserCombo);
   }

   public List<Integer> getWeaponChanges() {
      return this.weaponChanges;
   }

   public void setWeaponChanges(List<Integer> weaponChanges) {
      this.weaponChanges = weaponChanges;
   }

   public List<Integer> getWeaponChanges2() {
      return this.weaponChanges2;
   }

   public void setWeaponChanges2(List<Integer> weaponChanges2) {
      this.weaponChanges2 = weaponChanges2;
   }

   public CalcDamage getCalcDamage() {
      return this.calcDamage;
   }

   public void setCalcDamage(CalcDamage calcDamage) {
      this.calcDamage = calcDamage;
   }

   public int getEnergyBurst() {
      return this.energyBurst;
   }

   public void setEnergyBurst(int energyBurst) {
      this.energyBurst = energyBurst;
   }

   public int getMarkofPhantom() {
      return this.markofPhantom;
   }

   public void setMarkofPhantom(int markofPhantom) {
      this.markofPhantom = markofPhantom;
   }

   public int getUltimateDriverCount() {
      return this.ultimateDriverCount;
   }

   public void setUltimateDriverCount(int ultimateDriverCount) {
      this.ultimateDriverCount = ultimateDriverCount;
   }

   public int getMarkOfPhantomOid() {
      return this.markOfPhantomOid;
   }

   public void setMarkOfPhantomOid(int markOfPhantomOid) {
      this.markOfPhantomOid = markOfPhantomOid;
   }

   public int getRhoAias() {
      return this.rhoAias;
   }

   public void setRhoAias(int rhoAias) {
      this.rhoAias = rhoAias;
   }

   public int getPerfusion() {
      return this.perfusion;
   }

   public void setPerfusion(int perfusion) {
      this.perfusion = perfusion;
   }

   public MapleFieldAttackObj getFao() {
      return this.fao;
   }

   public void setFao(MapleFieldAttackObj fao) {
      this.fao = fao;
   }

   public int getBlackMagicAlter() {
      return this.blackMagicAlter;
   }

   public void setBlackMagicAlter(int blackMagicAlter) {
      this.blackMagicAlter = blackMagicAlter;
   }

   public int getWildGrenadierCount() {
      return this.WildGrenadierCount;
   }

   public void setWildGrenadierCount(int wildGrenadierCount) {
      this.WildGrenadierCount = wildGrenadierCount;
   }

   public int getVerseOfRelicsCount() {
      return this.VerseOfRelicsCount;
   }

   public void setVerseOfRelicsCount(int VerseOfRelics) {
      this.VerseOfRelicsCount = VerseOfRelics;
   }

   public int getJudgementType() {
      return this.judgementType;
   }

   public void setJudgementType(int judgementType) {
      this.judgementType = judgementType;
   }

   public int getAllUnion() {
      int ret = 0;

      MapleUnion union;
      for(Iterator var2 = this.getUnions().getUnions().iterator(); var2.hasNext(); ret += union.getLevel()) {
         union = (MapleUnion)var2.next();
      }

      return ret;
   }

   public int getAllStarForce() {
      int starforce = 0;
      Iterator var2 = this.getInventory(MapleInventoryType.EQUIPPED).iterator();

      while(var2.hasNext()) {
         Item item = (Item)var2.next();
         Equip eq = (Equip)item;
         starforce += eq.getEnhance();
         if (GameConstants.isLongcoat(item.getItemId())) {
            starforce += eq.getEnhance();
         }
      }

      return starforce;
   }

   public long getUnionDamage() {
      long rate = 0L;
      Iterator var3 = this.getUnions().getUnions().iterator();

      while(var3.hasNext()) {
         MapleUnion union = (MapleUnion)var3.next();
         if (union.getName().contains(union.getName()) && union.getPosition() != -1) {
            int hob = false;
            double rate2 = GameConstants.UnionAttackerRate(union.getLevel());
            int a55 = union.getLevel() * union.getLevel() * union.getLevel();
            int ab = (int)(rate2 * (double)a55) + 12500;
            int a56 = union.getStarForce() * union.getStarForce() * union.getStarForce();
            int a57 = union.getStarForce() * union.getStarForce();
            int abc = (int)(GameConstants.UnionStarForceRate(union.getStarForce()) * (double)a56 + GameConstants.UnionStarForceRate2(union.getStarForce()) * (double)a57 + (double)(GameConstants.UnionStarForceRate3(union.getStarForce()) * union.getStarForce() + GameConstants.UnionStarForceRate4(union.getStarForce())));
            int hob = ab + abc;
            rate += (long)hob;
         }
      }

      return rate;
   }

   public int getUnionCoin() {
      if (this.client.getKeyValue("UnionCoin") == null) {
         this.client.setKeyValue("UnionCoin", "0");
      }

      return Integer.parseInt(this.client.getKeyValue("UnionCoin"));
   }

   public void setUnionCoin(int coin) {
      this.client.setKeyValue("UnionCoin", String.valueOf(coin));
   }

   public int getUnionCoinNujuk() {
      if (this.client.getKeyValue("UnionCoinNujuk") == null) {
         this.client.setKeyValue("UnionCoinNujuk", "0");
      }

      return Integer.parseInt(this.client.getKeyValue("UnionCoinNujuk"));
   }

   public void setUnionCoinNujuk(long coin) {
      this.client.setKeyValue("UnionCoinNujuk", String.valueOf(coin));
   }

   public long getUnionAllNujuk() {
      if (this.client.getKeyValue("UnionAllNujuk") == null) {
         this.client.setKeyValue("UnionAllNujuk", "0");
      }

      return Long.parseLong(this.client.getKeyValue("UnionAllNujuk"));
   }

   public void setUnionAllNujuk(long Nujuk) {
      this.client.setKeyValue("UnionAllNujuk", String.valueOf(Nujuk));
   }

   public long getUnionNujuk() {
      if (this.client.getKeyValue("UnionNujuk") == null) {
         this.client.setKeyValue("UnionNujuk", "0");
      }

      return Long.parseLong(this.client.getKeyValue("UnionNujuk"));
   }

   public void setUnionNujuk(long Nujuk) {
      if (Nujuk >= 2500000000000L) {
         Nujuk = 2500000000000L;
      }

      this.client.setKeyValue("UnionNujuk", String.valueOf(Nujuk));
   }

   public long getUnionEndTime() {
      if (this.client.getKeyValue("UnionEndTime") == null) {
         this.client.setKeyValue("UnionEndTime", "0");
      }

      return Long.parseLong(this.client.getKeyValue("UnionEndTime"));
   }

   public void setUnionEndTime(long time) {
      this.client.setKeyValue("UnionEndTime", String.valueOf(time));
   }

   public long getUnionEnterTime() {
      if (this.client.getKeyValue("UnionEnterTime") == null) {
         this.client.setKeyValue("UnionEnterTime", "0");
      }

      return Long.parseLong(this.client.getKeyValue("UnionEnterTime"));
   }

   public void setUnionEnterTime(long time) {
      this.client.setKeyValue("UnionEnterTime", String.valueOf(time));
   }

   public int getAllUnionCoin() {
      if (this.client.getKeyValue("유니온코인") == null) {
         this.client.setKeyValue("유니온코인", "0");
      }

      return Integer.parseInt(this.client.getKeyValue("유니온코인"));
   }

   public void setAllUnionCoin(int a) {
      this.setKeyValue(500629, "point", a.makeConcatWithConstants<invokedynamic>(a));
      this.client.setKeyValue("유니온코인", a.makeConcatWithConstants<invokedynamic>(a));
   }

   public void AddAllUnionCoin(int a) {
      if (this.client.getKeyValue("유니온코인") == null) {
         this.client.setKeyValue("유니온코인", "0");
      }

      this.setKeyValue(500629, "point", (Integer.parseInt(this.client.getKeyValue("유니온코인")) + a).makeConcatWithConstants<invokedynamic>(Integer.parseInt(this.client.getKeyValue("유니온코인")) + a));
      this.client.setKeyValue("유니온코인", (Integer.parseInt(this.client.getKeyValue("유니온코인")) + a).makeConcatWithConstants<invokedynamic>(Integer.parseInt(this.client.getKeyValue("유니온코인")) + a));
   }

   public void RefreshUnionRaid(boolean enter) {
      Calendar ocal = Calendar.getInstance();
      String years = ocal.get(1).makeConcatWithConstants<invokedynamic>(ocal.get(1));
      int var10000 = ocal.get(2);
      String months = (var10000 + 1).makeConcatWithConstants<invokedynamic>(var10000 + 1);
      String days = ocal.get(5).makeConcatWithConstants<invokedynamic>(ocal.get(5));
      String hours = ocal.get(11).makeConcatWithConstants<invokedynamic>(ocal.get(11));
      String mins = ocal.get(12).makeConcatWithConstants<invokedynamic>(ocal.get(12));
      String secs = ocal.get(13).makeConcatWithConstants<invokedynamic>(ocal.get(13));
      int yeal = ocal.get(1);
      int month = ocal.get(2) + 1;
      int dayt = ocal.get(5);
      int hour = ocal.get(11);
      int min = ocal.get(12);
      int sec = ocal.get(13);
      if (month < 10) {
         months = "0" + month;
      }

      if (dayt < 10) {
         days = "0" + dayt;
      }

      if (hour < 10) {
         hours = "0" + hour;
      }

      if (min < 10) {
         mins = "0" + min;
      }

      if (sec < 10) {
         secs = "0" + sec;
      }

      if (this.getUnionEndTime() > 0L) {
         long time = (System.currentTimeMillis() - this.getUnionEndTime()) / 1000L;
         long attackrate = time * this.getUnionDamage();
         long allnujuk = this.getUnionAllNujuk();
         long nujuk = this.getUnionNujuk();
         int coin = this.getUnionCoin();
         int coing = (int)this.getUnionEnterTime();
         if (nujuk < 2500000000000L) {
            this.setUnionNujuk(nujuk + attackrate);
         }

         this.setUnionAllNujuk(allnujuk + attackrate);
         int a;
         if ((allnujuk + nujuk) / 100000000000L - (long)coing != 0L) {
            a = (int)((allnujuk + nujuk) / 100000000000L - (long)coing);
            this.setUnionCoin(coin + a);
            this.setUnionEnterTime((long)(coing + a));
         }

         if (nujuk + allnujuk >= 10000000000000L) {
            a = (int)((allnujuk + nujuk) / 100000000000L - this.getUnionEnterTime());
            this.setUnionCoin(this.getUnionCoin() + a);
            this.setUnionNujuk(0L);
            this.setUnionAllNujuk(0L);
            this.setUnionEnterTime(0L);
            if (Integer.parseInt(this.client.getKeyValue("UnionLaidLevel")) < 5) {
               MapleClient var26 = this.client;
               int var10002 = Integer.parseInt(this.client.getKeyValue("UnionLaidLevel"));
               var26.setKeyValue("UnionLaidLevel", (var10002 + 1).makeConcatWithConstants<invokedynamic>(var10002 + 1));
            } else {
               this.client.setKeyValue("UnionLaidLevel", "1");
            }
         }

         years = (yeal % 100).makeConcatWithConstants<invokedynamic>(yeal % 100);
         this.setUnionEndTime(System.currentTimeMillis());
         if (this.getUnionCoin() >= GameConstants.UnionMaxCoin(this)) {
            if (enter) {
               this.updateInfoQuest(18098, "lastTime=" + years + months + days + hours + mins + secs + ";coin=" + GameConstants.UnionMaxCoin(this));
            }

            this.setUnionCoin(GameConstants.UnionMaxCoin(this));
         }
      }

   }

   public void setLastCharGuildId(int a) {
      this.lastCharGuildId = a;
   }

   public int getLastCharGuildId() {
      return this.lastCharGuildId;
   }

   public void removeAllEquip(int id, boolean show) {
      MapleInventoryType type = GameConstants.getInventoryType(id);
      int possessed = this.getInventory(type).countById(id);
      if (possessed > 0) {
         MapleInventoryManipulator.removeById(this.getClient(), type, id, possessed, true, false);
         if (show) {
            this.getClient().getSession().writeAndFlush(CWvsContext.InfoPacket.getShowItemGain(id, (short)(-possessed), true));
         }
      }

      if (type == MapleInventoryType.EQUIP) {
         type = MapleInventoryType.EQUIPPED;
         possessed = this.getInventory(type).countById(id);
         if (possessed > 0) {
            Item equip = this.getInventory(type).findById(id);
            if (equip != null) {
               this.getInventory(type).removeSlot(equip.getPosition());
               this.equipChanged();
               this.getClient().getSession().writeAndFlush(CWvsContext.InventoryPacket.clearInventoryItem(MapleInventoryType.EQUIP, equip.getPosition(), false));
            }
         }
      }

   }

   public void LoadPlatformerRecords() {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      this.PfRecords.clear();

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM platformerreocrd WHERE cid = ? ORDER BY stage ASC");
         ps.setInt(1, this.id);
         rs = ps.executeQuery();

         while(rs.next()) {
            int Stage = rs.getInt("stage");
            int ClearTime = rs.getInt("cleartime");
            int Stars = rs.getInt("star");
            this.PfRecords.add(new PlatformerRecord(Stage, ClearTime, Stars));
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
         }

      }

   }

   public void SavePlatformerRecords() {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         Iterator var4 = this.PfRecords.iterator();

         while(var4.hasNext()) {
            PlatformerRecord rec = (PlatformerRecord)var4.next();
            ps = con.prepareStatement("SELECT * FROM platformerreocrd WHERE cid = ? AND stage = ?");
            ps.setInt(1, this.id);
            ps.setInt(2, rec.getStage());
            rs = ps.executeQuery();
            if (rs.next()) {
               ps = con.prepareStatement("UPDATE platformerreocrd SET cleartime = ?, star = ? WHERE stage = ?");
               ps.setInt(1, rec.getClearTime());
               ps.setInt(2, rec.getStars());
               ps.setInt(3, rec.getStage());
               ps.executeUpdate();
            } else {
               this.SaveNewRecord(con, ps, rec);
            }

            ps.close();
            rs.close();
         }
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
               rs.close();
            }
         } catch (SQLException var13) {
         }

      }

   }

   public void setElementalCharge(int elementalCharge) {
      this.elementalCharge = elementalCharge;
   }

   public int getElementalCharge() {
      return this.elementalCharge;
   }

   public int getLastElementalCharge() {
      return this.lastElementalCharge;
   }

   public List<PlatformerRecord> getPlatformerRecords() {
      return this.PfRecords;
   }

   public void SaveNewRecord(Connection con, PreparedStatement ps, PlatformerRecord rec) {
      try {
         ps = con.prepareStatement("INSERT INTO platformerreocrd (cid, stage, cleartime, star) VALUES (?, ?, ?, ?)");
         ps.setInt(1, this.id);
         ps.setInt(2, rec.getStage());
         ps.setInt(3, rec.getClearTime());
         ps.setInt(4, rec.getStars());
         ps.executeUpdate();
         ps.close();
      } catch (SQLException var5) {
         var5.printStackTrace();
      }

   }

   public void RegisterPlatformerRecord(int Stage) {
      int time = (int)((System.currentTimeMillis() - this.PlatformerStageEnter) / 1000L);
      int star = 0;
      if (time <= GameConstants.StarInfo[Stage - 1][0]) {
         star = 3;
      } else if (time <= GameConstants.StarInfo[Stage - 1][1]) {
         star = 2;
      } else if (time <= GameConstants.StarInfo[Stage - 1][2]) {
         star = 1;
      }

      Iterator var4;
      if (this.PfRecords.size() < Stage) {
         this.PfRecords.add(new PlatformerRecord(Stage, time, star));
      } else {
         var4 = this.PfRecords.iterator();

         label55:
         while(true) {
            PlatformerRecord record;
            do {
               do {
                  if (!var4.hasNext()) {
                     break label55;
                  }

                  record = (PlatformerRecord)var4.next();
               } while(record.getStage() != Stage);
            } while(record.getClearTime() <= time && record.getStars() >= star);

            record.setClearTime(time);
            record.setStars(star);
         }
      }

      if (star > 0) {
         var4 = this.map.getAllCharactersThreadsafe().iterator();

         while(var4.hasNext()) {
            MapleCharacter chr = (MapleCharacter)var4.next();
            chr.dropMessage(-1, this.name + "님이 별 " + star + "개로 스테이지를 클리어하였습니다!");
         }

         if (this.getKeyValue(20190409, "Stage_" + Stage + "_Received") == -1L) {
            this.setKeyValue(20190409, "Stage_" + Stage + "_Received", "0");
         }

         if (this.getKeyValue(20190409, "Stage_" + Stage + "_Received") == 0L) {
            this.setKeyValue(20190409, "Stage_" + Stage + "_Received", "1");
         }
      }

      this.SavePlatformerRecords();
   }

   public void setWeddingGive(int l) {
      this.weddingGiftGive = l;
   }

   public void cancelTimer() {
      if (this.MulungTimer != null) {
         this.MulungTimerTask.cancel();
         this.MulungTimer.cancel();
         this.MulungTimer = null;
         this.MulungTimerTask = null;
      }

   }

   public int getDojoStartTime() {
      return this.dojoStartTime;
   }

   public void setDojoStartTime(int dojoStartTime) {
      this.dojoStartTime = dojoStartTime;
   }

   public boolean getDojoStop() {
      return this.dojoStop;
   }

   public void setDojoStop(boolean stop) {
      this.dojoStop = stop;
   }

   public int getWeddingGive() {
      return this.weddingGiftGive;
   }

   public long getDojoStopTime() {
      return this.dojoStopTime;
   }

   public void setDojoStopTime(long dojoStopTime) {
      this.dojoStopTime = dojoStopTime;
   }

   public long getDojoCoolTime() {
      return (long)this.dojoCoolTime;
   }

   public void setDojoCoolTime(int dojoCoolTime) {
      this.dojoCoolTime = dojoCoolTime;
   }

   public boolean isDeadEffect() {
      return this.deadEffect;
   }

   public void setDeadEffect(boolean deadEffect) {
      this.deadEffect = deadEffect;
   }

   public void applySkill(int skillid, int level) {
      SkillFactory.getSkill(skillid).getEffect(level).applyTo(this, true);
   }

   public int getZeroCubePosition() {
      return this.zeroCubePosition;
   }

   public void setZeroCubePosition(int zeroCubePosition) {
      this.zeroCubePosition = zeroCubePosition;
   }

   public Pair<Integer, Integer> getRecipe() {
      return this.recipe;
   }

   public void setRecipe(Pair<Integer, Integer> recipe) {
      this.recipe = recipe;
   }

   public int getTransformCooldown() {
      return this.transformCooldown;
   }

   public void setTransformCooldown(int transformCooldown) {
      this.transformCooldown = transformCooldown;
   }

   public Set<MapleMapObject> getVisibleMapObjects() {
      return this.visibleMapObjects;
   }

   public void setVisibleMapObjects(Set<MapleMapObject> visibleMapObjects) {
      this.visibleMapObjects = visibleMapObjects;
   }

   public Map<SecondaryStat, MapleDiseases> getDiseases() {
      return this.diseases;
   }

   public void setDiseases(Map<SecondaryStat, MapleDiseases> diseases) {
      this.diseases = diseases;
   }

   public boolean isNoneDestroy() {
      return this.noneDestroy;
   }

   public void setNoneDestroy(boolean noneDestroy) {
      this.noneDestroy = noneDestroy;
   }

   public List<Integer> getPosionNovas() {
      return this.posionNovas;
   }

   public long getDamageMeter() {
      return this.damageMeter;
   }

   public void setDamageMeter(long damageMeter) {
      this.damageMeter = damageMeter;
   }

   public void setPosionNovas(List<Integer> posionNovas) {
      this.posionNovas = posionNovas;
   }

   public int getLastBossId() {
      return this.lastBossId;
   }

   public void setLastBossId(int lastBossId) {
      this.lastBossId = lastBossId;
   }

   public int getMoonGauge() {
      return this.moonGauge;
   }

   public void setMoonGauge(int lunaGauge) {
      this.moonGauge = lunaGauge;
   }

   public OneCardGame getOneCardInstance() {
      return this.oneCardInstance;
   }

   public void setOneCardInstance(OneCardGame oneCardInstance) {
      this.oneCardInstance = oneCardInstance;
   }

   public MultiYutGame getMultiYutInstance() {
      return this.multiYutInstance;
   }

   public void setMultiYutInstance(MultiYutGame multiYutInstance) {
      this.multiYutInstance = multiYutInstance;
   }

   public ColorInvitationCard getColorCardInstance() {
      return this.ColorCardInstance;
   }

   public void setColorCardInstance(ColorInvitationCard ColorCardInstance) {
      this.ColorCardInstance = ColorCardInstance;
   }

   public boolean isOneMoreChance() {
      return this.oneMoreChance;
   }

   public void setOneMoreChance(boolean oneMoreChance) {
      this.oneMoreChance = oneMoreChance;
   }

   public List<Integer> getExceptionList() {
      return this.exceptionList;
   }

   public void setExceptionList(List<Integer> exceptionList) {
      this.exceptionList = exceptionList;
   }

   public int getTrinity() {
      return this.trinity;
   }

   public void setTrinity(int trinity) {
      this.trinity = trinity;
   }

   public UnionList getUnions() {
      return this.unions;
   }

   public void setUnions(UnionList unions) {
      this.unions = unions;
   }

   public Map<String, String> getKeyValues_boss() {
      return this.keyValues_boss;
   }

   public String getV_boss(String k) {
      return this.keyValues_boss.containsKey(k) ? (String)this.keyValues_boss.get(k) : null;
   }

   public void addKV_boss(String k, String v) {
      this.keyValues_boss.put(k, v);
   }

   public void removeV_boss(String k) {
      this.keyValues_boss.remove(k);
   }

   public void removeKeyValue_boss(int type, String key) {
      String questInfo = this.getInfoQuest(type);
      if (questInfo != null) {
         String[] split = questInfo.split(";");
         String[] var6 = split;
         int var7 = split.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            String s = var6[var8];
            if (s.startsWith(key + "=")) {
               String newkey = questInfo.replace(s + ";", "");
               this.updateInfoQuest(type, newkey);
               return;
            }
         }

         this.updateInfoQuest(type, questInfo);
      }
   }

   public void removeKeyValue_boss(int type) {
      MapleQuest quest = MapleQuest.getInstance(type);
      if (quest != null) {
         this.updateInfoQuest(type, "");
         this.questinfo.remove(quest);
         Connection con = null;
         PreparedStatement ps = null;

         try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("DELETE FROM questinfo WHERE characterid = " + this.getId() + " AND quest = ?");
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

   public void setKeyValue_boss(int type, String key, String value) {
      String questInfo = this.getInfoQuest(type);
      if (questInfo == null) {
         this.updateInfoQuest(type, key + "=" + value + ";");
      } else {
         String[] split = questInfo.split(";");
         String[] var7 = split;
         int var8 = split.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            String s = var7[var9];
            if (s.startsWith(key + "=")) {
               String newkey = questInfo.replace(s, key + "=" + value);
               this.updateInfoQuest(type, newkey);
               return;
            }
         }

         this.updateInfoQuest(type, questInfo + key + "=" + value + ";");
      }
   }

   public String getKeyValueStr_boss(int type, String key) {
      String questInfo = this.getInfoQuest(type);
      if (questInfo == null) {
         return null;
      } else {
         String[] split = questInfo.split(";");
         String[] var6 = split;
         int var7 = split.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            String s = var6[var8];
            if (s.startsWith(key + "=")) {
               String newkey = s.replace(key + "=", "");
               String newkey2 = newkey.replace(";", "");
               return newkey2;
            }
         }

         return null;
      }
   }

   public long getKeyValue_boss(int type, String key) {
      String questInfo = this.getInfoQuest(type);
      if (questInfo == null) {
         return -1L;
      } else {
         String[] split = questInfo.split(";");
         String[] var6 = split;
         int var7 = split.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            String s = var6[var8];
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

   public Map<String, String> getKeyValues() {
      return this.keyValues;
   }

   public String getV(String k) {
      return this.keyValues.containsKey(k) ? (String)this.keyValues.get(k) : null;
   }

   public void addKV(String k, String v) {
      this.keyValues.put(k, v);
   }

   public void removeV(String k) {
      this.keyValues.remove(k);
   }

   public Point getSpecialChairPoint() {
      return this.specialChairPoint;
   }

   public void setSpecialChairPoint(Point point) {
      this.specialChairPoint = point;
   }

   public void setNettPyramid(MapleNettPyramid mnp) {
      this.NettPyramid = mnp;
   }

   public MapleNettPyramid getNettPyramid() {
      return this.NettPyramid;
   }

   public final boolean isLeader() {
      return this.getParty() != null && this.getParty().getLeader().getId() == this.getId();
   }

   public void Message(String msg) {
      this.client.getSession().writeAndFlush(CField.getGameMessage(8, msg));
   }

   public void changeMap(MapleMap to) {
      this.changeMapInternal(to, to.getPortal(0).getPosition(), CField.getWarpToMap(to, 0, this), to.getPortal(0), false);
   }

   public void message(String msg) {
      this.client.getSession().writeAndFlush(CWvsContext.serverNotice(5, this.name, msg));
   }

   public final MapleMap getWarpMap(int map) {
      return this.getEventInstance() != null ? this.getEventInstance().getMapFactory().getMap(map) : ChannelServer.getInstance(this.client.getChannel()).getMapFactory().getMap(map);
   }

   public int getSpiritGuard() {
      return this.spiritGuard;
   }

   public void setSpiritGuard(int spiritGuard) {
      this.spiritGuard = spiritGuard;
   }

   public List<MapleMannequin> getHairRoom() {
      return this.hairRoom;
   }

   public void setHairRoom(List<MapleMannequin> hairRoom) {
      this.hairRoom = hairRoom;
   }

   public List<MapleMannequin> getFaceRoom() {
      return this.faceRoom;
   }

   public void setFaceRoom(List<MapleMannequin> faceRoom) {
      this.faceRoom = faceRoom;
   }

   public List<VMatrix> getMatrixs() {
      return this.matrixs;
   }

   public void setMatrixs(List<VMatrix> matrixs) {
      this.matrixs = matrixs;
   }

   public DefenseTowerWave getDefenseTowerWave() {
      return this.defenseTowerWave;
   }

   public void setDefenseTowerWave(DefenseTowerWave defenseTowerWave) {
      this.defenseTowerWave = defenseTowerWave;
   }

   public BountyHunting getBountyHunting() {
      return this.bountyHunting;
   }

   public void setBountyHunting(BountyHunting bountyhunting) {
      this.bountyHunting = bountyhunting;
   }

   public int getBlitzShield() {
      return this.blitzShield;
   }

   public void setBlitzShield(int blitzShield) {
      this.blitzShield = blitzShield;
   }

   public int[] getDeathCounts() {
      return this.deathCounts;
   }

   public void setDeathCounts(int[] deathCounts) {
      this.deathCounts = deathCounts;
   }

   public void resetDeathCounts() {
      for(int i = 0; i < this.deathCounts.length; ++i) {
         this.deathCounts[i] = 1;
      }

      this.getMap().broadcastMessage(CField.JinHillah(10, this, this.getMap()));
   }

   public int liveCounts() {
      int c = 0;

      for(int i = 0; i < this.deathCounts.length; ++i) {
         if (this.deathCounts[i] == 1) {
            ++c;
         }
      }

      return c;
   }

   public int DeadCounts() {
      int c = 0;

      for(int i = 0; i < this.deathCounts.length; ++i) {
         if (this.deathCounts[i] == 2 || this.deathCounts[i] == 0) {
            ++c;
         }
      }

      return c;
   }

   public int getCreateDate() {
      if (this.createDate > 0) {
         return this.createDate;
      } else {
         Connection con = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT * FROM characters where `id` = ?");
            ps.setInt(1, this.id);
            rs = ps.executeQuery();

            while(rs.next()) {
               Timestamp ts = rs.getTimestamp("createdate");
               if (ts != null) {
                  this.createDate = ts.toLocalDateTime().getYear() * 1000 + ts.toLocalDateTime().getMonth().getValue() * 100 + ts.toLocalDateTime().getDayOfMonth();
               }
            }
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
            } catch (SQLException var12) {
               var12.printStackTrace();
            }

         }

         return this.createDate;
      }
   }

   public void setCreateDate(int createDate) {
      this.createDate = createDate;
   }

   public boolean isUseBuffFreezer() {
      return this.useBuffFreezer;
   }

   public void setUseBuffFreezer(boolean useBuffFreezer) {
      this.useBuffFreezer = useBuffFreezer;
   }

   public void setSoulMP(Equip weapon) {
      if (weapon != null) {
         int soulSkillID = weapon.getSoulSkill();
         Skill soulSkill = SkillFactory.getSkill(soulSkillID);
         if (soulSkill != null && soulSkillID != 0) {
            if (this.getSkillLevel(soulSkillID) == 0) {
               this.changeSkillLevel(soulSkill, (byte)1, (byte)1);
            }

            SecondaryStatEffect effect = soulSkill.getEffect(1);
            if (this.getBuffedEffect(SecondaryStat.SoulMP) != null) {
               int soulCount = this.getBuffedValue(SecondaryStat.SoulMP);
               HashMap localstatups;
               if (soulCount < 1000) {
                  this.setBuffedValue(SecondaryStat.SoulMP, soulCount + Randomizer.rand(1, 3));
                  localstatups = new HashMap();
                  localstatups.put(SecondaryStat.SoulMP, new Pair(soulCount + Randomizer.rand(1, 3), 0));
                  this.client.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(localstatups, effect, this));
                  this.map.broadcastMessage(CWvsContext.BuffPacket.giveForeignBuff(this, localstatups, effect));
               }

               if (this.getBuffedValue(SecondaryStat.SoulMP) >= effect.getSoulMPCon() && this.getCooldownLimit(soulSkillID) == 0L && this.getBuffedEffect(SecondaryStat.FullSoulMP) == null) {
                  localstatups = new HashMap();
                  localstatups.put(SecondaryStat.FullSoulMP, new Pair(0, 0));
                  this.client.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(localstatups, effect, this));
                  this.map.broadcastMessage(CWvsContext.BuffPacket.giveForeignBuff(this, localstatups, effect));
                  Iterator var7 = localstatups.entrySet().iterator();

                  while(var7.hasNext()) {
                     Entry<SecondaryStat, Pair<Integer, Integer>> statup = (Entry)var7.next();
                     this.effects.add(new Pair((SecondaryStat)statup.getKey(), new SecondaryStatValueHolder(effect, System.currentTimeMillis(), this.getBuffedValue(SecondaryStat.SoulMP), 0, this.getId(), new ArrayList(), new ArrayList())));
                  }

                  this.client.getSession().writeAndFlush(CWvsContext.enableActions(this, true, false));
                  this.client.getSession().writeAndFlush(CWvsContext.enableActions(this, false, true));
               }
            } else {
               Map<SecondaryStat, Pair<Integer, Integer>> localstatups2 = new HashMap();
               localstatups2.put(SecondaryStat.SoulMP, new Pair(0, 0));
               this.client.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(localstatups2, effect, this));
               this.map.broadcastMessage(CWvsContext.BuffPacket.giveForeignBuff(this, localstatups2, effect));
               Iterator var10 = localstatups2.entrySet().iterator();

               while(var10.hasNext()) {
                  Entry<SecondaryStat, Pair<Integer, Integer>> statup2 = (Entry)var10.next();
                  this.effects.add(new Pair((SecondaryStat)statup2.getKey(), new SecondaryStatValueHolder(effect, System.currentTimeMillis(), 0, 0, this.getId(), new ArrayList(), new ArrayList())));
               }

               this.client.getSession().writeAndFlush(CWvsContext.enableActions(this, true, false));
               this.client.getSession().writeAndFlush(CWvsContext.enableActions(this, false, true));
            }
         }
      } else {
         this.cancelEffectFromBuffStat(SecondaryStat.SoulMP);
         this.cancelEffectFromBuffStat(SecondaryStat.FullSoulMP);
      }

   }

   public void useSoulSkill() {
      if (this.getBuffedEffect(SecondaryStat.SoulMP) != null) {
         SecondaryStatEffect effect = this.getBuffedEffect(SecondaryStat.SoulMP);
         if (effect.getSoulMPCon() <= this.getBuffedValue(SecondaryStat.SoulMP) && this.getBuffedEffect(SecondaryStat.FullSoulMP) != null) {
            this.setBuffedValue(SecondaryStat.SoulMP, this.getBuffedValue(SecondaryStat.SoulMP) - effect.getSoulMPCon());
            Map<SecondaryStat, Pair<Integer, Integer>> localstatups = new HashMap();
            localstatups.put(SecondaryStat.SoulMP, new Pair(this.getBuffedValue(SecondaryStat.SoulMP), 0));
            this.client.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(localstatups, effect, this));
            this.map.broadcastMessage(CWvsContext.BuffPacket.giveForeignBuff(this, localstatups, effect));
            this.cancelEffectFromBuffStat(SecondaryStat.IndieSummon, effect.getSourceId());
            this.cancelEffectFromBuffStat(SecondaryStat.FullSoulMP);
            this.client.getSession().writeAndFlush(CWvsContext.enableActions(this, true, false));
            this.client.getSession().writeAndFlush(CWvsContext.enableActions(this, false, true));
         }
      }

   }

   public List<MapleMannequin> getSkinRoom() {
      return this.skinRoom;
   }

   public void setSkinRoom(List<MapleMannequin> skinRoom) {
      this.skinRoom = skinRoom;
   }

   public int getOverloadCount() {
      return this.overloadCount;
   }

   public void setOverloadCount(int overloadCount) {
      this.overloadCount = overloadCount;
   }

   public MonsterPyramid getMonsterPyramidInstance() {
      return this.monsterPyramidInstance;
   }

   public void setMonsterPyramidInstance(MonsterPyramid monsterPyramidInstance) {
      this.monsterPyramidInstance = monsterPyramidInstance;
   }

   public FrittoEagle getFrittoEagle() {
      return this.frittoEagle;
   }

   public void setFrittoEagle(FrittoEagle frittoEagle) {
      this.frittoEagle = frittoEagle;
   }

   public FrittoEgg getFrittoEgg() {
      return this.frittoEgg;
   }

   public void setFrittoEgg(FrittoEgg frittoEgg) {
      this.frittoEgg = frittoEgg;
   }

   public FrittoDancing getFrittoDancing() {
      return this.frittoDancing;
   }

   public void setFrittoDancing(FrittoDancing frittoDancing) {
      this.frittoDancing = frittoDancing;
   }

   public boolean isDuskBlind() {
      return this.isDuskBlind;
   }

   public void setDuskBlind(boolean duskBlind) {
      this.isDuskBlind = duskBlind;
   }

   public int getDuskGauge() {
      return this.duskGauge;
   }

   public void setDuskGauge(int duskGauge) {
      this.duskGauge = duskGauge;
   }

   public ScheduledFuture<?> getSecondaryStatEffectTimer() {
      return this.secondaryStatEffectTimer;
   }

   public void getWorldGMMsg(MapleCharacter chr, String text) {
      Iterator var3 = ChannelServer.getAllInstances().iterator();

      while(var3.hasNext()) {
         ChannelServer cs = (ChannelServer)var3.next();
         Iterator var5 = cs.getPlayerStorage().getAllCharacters().values().iterator();

         while(var5.hasNext()) {
            MapleCharacter achr = (MapleCharacter)var5.next();
            if (achr.isGM()) {
               String var10002 = chr.getName();
               achr.dropMessageGM(6, "[GM알림] " + var10002 + "(이)가 " + text);
            }
         }
      }

   }

   public void AutoTeachSkillZero() {
      if (this.getJob() > 99) {
         String job = String.valueOf(this.getJob());
         int minus = Integer.parseInt(job.substring(job.length() - this.minusValue(), job.length()));
         this.TeachSkill(this.getJob() - minus + this.plusAlpha());
         if (minus > 0 && this.getJob() - this.plusAlpha() != 2000 && this.getJob() - this.plusAlpha() != 3000) {
            this.TeachSkillZero(this.getJob() - Integer.parseInt(job.substring(job.length() - 2, job.length())));

            for(int i = 0; i < this.getJob() % 10 + 1; ++i) {
               this.TeachSkillZero(this.getJob() - this.getJob() % 10 + i);
            }
         }
      }

   }

   public void TeachSkillZero(int job) {
      MapleDataProvider var10000 = MapleDataProviderFactory.getDataProvider(MapleDataProviderFactory.fileInWZPath("Skill.wz"));
      String var10001 = job.makeConcatWithConstants<invokedynamic>(job);
      MapleData data = var10000.getData(StringUtil.getLeftPaddedStr(var10001, '0', 3) + ".img");
      byte maxLevel = false;
      Iterator var4 = data.iterator();

      label83:
      while(true) {
         MapleData skill;
         do {
            if (!var4.hasNext()) {
               return;
            }

            skill = (MapleData)var4.next();
         } while(skill == null);

         Iterator var6 = skill.getChildren().iterator();

         while(true) {
            MapleData skillId;
            int skillid;
            byte maxLevel;
            do {
               do {
                  do {
                     if (!var6.hasNext()) {
                        continue label83;
                     }

                     skillId = (MapleData)var6.next();
                  } while(skillId.getName().equals("icon"));
               } while(skillId.getName().equals("maxLevel"));

               maxLevel = (byte)MapleDataTool.getIntConvert("maxLevel", skillId.getChildByPath("common"), 0);
               skillid = Integer.parseInt(skillId.getName());
               if (skillid == 100001005) {
                  maxLevel = 1;
               }
            } while(MapleDataTool.getIntConvert("invisible", skillId, 0) != 0 && skillid != 27001100 && skillid != 27001201 && skillid != 51121005 && skillid != 11121000 && skillid != 12121000 && skillid != 13121000 && skillid != 14121000 && skillid != 15121000);

            if (this.getLevel() >= MapleDataTool.getIntConvert("reqLev", skillId, 0) && SkillFactory.getSkill(skillid) != null) {
               this.changeSkillLevel(SkillFactory.getSkill(skillid), (byte)0, SkillFactory.getSkill(skillid).isFourthJob() ? (maxLevel == 180 ? 1 : maxLevel) : (byte)SkillFactory.getSkill(skillid).getMasterLevel());
               if (GameConstants.isPathFinder(this.getJob())) {
                  this.changeSkillLevel(SkillFactory.getSkill(3001007), (byte)0, (byte)0);
                  this.changeSkillLevel(SkillFactory.getSkill(3011004), (byte)0, (byte)20);
                  this.changeSkillLevel(SkillFactory.getSkill(3011005), (byte)0, (byte)10);
                  this.changeSkillLevel(SkillFactory.getSkill(3011006), (byte)0, (byte)10);
                  this.changeSkillLevel(SkillFactory.getSkill(3011007), (byte)0, (byte)10);
                  this.changeSkillLevel(SkillFactory.getSkill(3011008), (byte)0, (byte)10);
                  this.changeSkillLevel(SkillFactory.getSkill(3010002), (byte)0, (byte)20);
                  this.changeSkillLevel(SkillFactory.getSkill(1298), (byte)0, (byte)4);
               }
            }
         }
      }
   }

   public void AutoTeachSkill() {
      if (this.getJob() > 99) {
         String job = String.valueOf(this.getJob());
         int minus = Integer.parseInt(job.substring(job.length() - this.minusValue(), job.length()));
         this.TeachSkill(this.getJob() - minus + this.plusAlpha());
         if (minus > 0 && this.getJob() - this.plusAlpha() != 2000 && this.getJob() - this.plusAlpha() != 3000) {
            this.TeachSkill(this.getJob() - Integer.parseInt(job.substring(job.length() - 2, job.length())));

            for(int i = 0; i < this.getJob() % 10 + 1; ++i) {
               this.TeachSkill(this.getJob() - this.getJob() % 10 + i);
            }
         }

         if (GameConstants.isPathFinder(this.getJob())) {
            this.changeSkillLevel(SkillFactory.getSkill(3001007), (byte)0, (byte)0);
            this.changeSkillLevel(SkillFactory.getSkill(3011004), (byte)20, (byte)20);
            this.changeSkillLevel(SkillFactory.getSkill(3011005), (byte)10, (byte)10);
            this.changeSkillLevel(SkillFactory.getSkill(3011006), (byte)10, (byte)10);
            this.changeSkillLevel(SkillFactory.getSkill(3011007), (byte)10, (byte)10);
            this.changeSkillLevel(SkillFactory.getSkill(3011008), (byte)10, (byte)10);
            this.changeSkillLevel(SkillFactory.getSkill(3010002), (byte)20, (byte)20);
            this.changeSkillLevel(SkillFactory.getSkill(3010003), (byte)20, (byte)20);
            this.changeSkillLevel(SkillFactory.getSkill(1298), (byte)4, (byte)4);
         } else if (GameConstants.isAdel(this.getJob())) {
            if (this.getJob() == 15112) {
               this.changeSkillLevel(SkillFactory.getSkill(150021251), (byte)1, (byte)1);
               this.changeSkillLevel(SkillFactory.getSkill(150020079), (byte)1, (byte)1);
               this.changeSkillLevel(SkillFactory.getSkill(151001004), (byte)1, (byte)1);
            }
         } else if (!GameConstants.isSoulMaster(this.getJob()) && !GameConstants.isFlameWizard(this.getJob()) && !GameConstants.isWindBreaker(this.getJob()) && !GameConstants.isNightWalker(this.getJob()) && !GameConstants.isStriker(this.getJob())) {
            if (GameConstants.isMichael(this.getJob())) {
               this.changeSkillLevel(SkillFactory.getSkill(50000250), (byte)1, (byte)1);
            } else if (GameConstants.isDemonAvenger(this.getJob())) {
               this.changeSkillLevel(SkillFactory.getSkill(31011000), (byte)20, (byte)20);
               this.changeSkillLevel(SkillFactory.getSkill(31010002), (byte)10, (byte)10);
               this.changeSkillLevel(SkillFactory.getSkill(31010003), (byte)15, (byte)15);
               this.changeSkillLevel(SkillFactory.getSkill(31011001), (byte)20, (byte)20);
            } else if (GameConstants.isEvan(this.getJob()) && this.getLevel() >= 30) {
               this.changeSkillLevel(SkillFactory.getSkill(22110018), (byte)10, (byte)10);
            } else if (GameConstants.isZero(this.getJob())) {
               if (this.level >= 100) {
                  this.changeSkillLevel(SkillFactory.getSkill(100000267), (byte)1, (byte)1);
               }

               if (this.level >= 110) {
                  this.changeSkillLevel(SkillFactory.getSkill(100001261), (byte)1, (byte)1);
               }

               if (this.level >= 120) {
                  this.changeSkillLevel(SkillFactory.getSkill(100001274), (byte)1, (byte)1);
               }

               if (this.level >= 140) {
                  this.changeSkillLevel(SkillFactory.getSkill(100001272), (byte)1, (byte)1);
               }

               if (this.level >= 160) {
                  this.changeSkillLevel(SkillFactory.getSkill(100001283), (byte)1, (byte)1);
               }

               if (this.level >= 200) {
                  this.changeSkillLevel(SkillFactory.getSkill(100001005), (byte)1, (byte)1);
               }
            }
         } else {
            this.changeSkillLevel(SkillFactory.getSkill(10001244), (byte)1, (byte)1);
            this.changeSkillLevel(SkillFactory.getSkill(10000252), (byte)1, (byte)1);
            this.changeSkillLevel(SkillFactory.getSkill(10001253), (byte)1, (byte)1);
            this.changeSkillLevel(SkillFactory.getSkill(10001254), (byte)1, (byte)1);
            this.changeSkillLevel(SkillFactory.getSkill(10000250), (byte)1, (byte)1);
         }
      }

   }

   public int minusValue() {
      return !GameConstants.isEvan(this.getJob()) && !GameConstants.isMercedes(this.getJob()) && !GameConstants.isPhantom(this.getJob()) && !GameConstants.isLuminous(this.getJob()) && !GameConstants.isEunWol(this.getJob()) && !GameConstants.isAran(this.getJob()) && !GameConstants.isAngelicBuster(this.getJob()) && !GameConstants.isDemonSlayer(this.getJob()) && !GameConstants.isDemonAvenger(this.getJob()) && !GameConstants.isXenon(this.getJob()) && !GameConstants.isZero(this.getJob()) && !GameConstants.isKinesis(this.getJob()) && !GameConstants.isPinkBean(this.getJob()) && !GameConstants.isKaiser(this.getJob()) ? 2 : 3;
   }

   public int plusAlpha() {
      if (!GameConstants.isDemonAvenger(this.getJob()) && !GameConstants.isAngelicBuster(this.getJob()) && !GameConstants.isCannon(this.getJob()) && !GameConstants.isEvan(this.getJob()) && !GameConstants.isDemonSlayer(this.getJob())) {
         if (!GameConstants.isXenon(this.getJob()) && !GameConstants.isMercedes(this.getJob())) {
            if (GameConstants.isPhantom(this.getJob())) {
               return 3;
            } else if (GameConstants.isLuminous(this.getJob())) {
               return 4;
            } else {
               return GameConstants.isEunWol(this.getJob()) ? 5 : 0;
            }
         } else {
            return 2;
         }
      } else {
         return 1;
      }
   }

   public void TeachSkill(int job) {
      MapleDataProvider var10000 = MapleDataProviderFactory.getDataProvider(MapleDataProviderFactory.fileInWZPath("Skill.wz"));
      String var10001 = job.makeConcatWithConstants<invokedynamic>(job);
      MapleData data = var10000.getData(StringUtil.getLeftPaddedStr(var10001, '0', 3) + ".img");
      byte maxLevel = false;
      Iterator var4 = data.iterator();

      while(true) {
         label125:
         while(true) {
            MapleData skill;
            do {
               if (!var4.hasNext()) {
                  var4 = this.skills.entrySet().iterator();

                  while(var4.hasNext()) {
                     Entry<Skill, SkillEntry> skill2 = (Entry)var4.next();
                     if (!PacketHelper.jobskill(this, ((Skill)skill2.getKey()).getId()) && ((Skill)skill2.getKey()).getId() < 80000000 && ((Skill)skill2.getKey()).getId() < 400000000) {
                        this.changeSkillLevel((Skill)skill2.getKey(), (byte)0, (byte)0);
                     }
                  }

                  return;
               }

               skill = (MapleData)var4.next();
            } while(skill == null);

            Iterator var6 = skill.getChildren().iterator();

            while(true) {
               MapleData skillId;
               boolean learn;
               int skillid;
               byte maxLevel;
               do {
                  do {
                     do {
                        if (!var6.hasNext()) {
                           continue label125;
                        }

                        skillId = (MapleData)var6.next();
                     } while(skillId.getName().equals("icon"));
                  } while(skillId.getName().equals("maxLevel"));

                  learn = true;
                  maxLevel = (byte)MapleDataTool.getIntConvert("maxLevel", skillId.getChildByPath("common"), 0);
                  skillid = Integer.parseInt(skillId.getName());
                  if (skillid == 100001005) {
                     maxLevel = 1;
                  }

                  if (!PacketHelper.jobskill(this, skillid) || skillid == 22110016 || SkillFactory.getSkill(skillid).isHyper()) {
                     learn = false;
                     continue label125;
                  }

                  if (GameConstants.isCannon(this.getJob())) {
                     switch(skillid) {
                     case 5000000:
                     case 5000007:
                     case 5001002:
                     case 5001003:
                     case 5001005:
                     case 5001010:
                        this.changeSkillData(SkillFactory.getSkill(skillid), 0, (byte)0, 0L);
                        learn = false;
                     }
                  }

                  switch(skillid) {
                  case 3321003:
                  case 100000267:
                  case 100001005:
                  case 100001261:
                  case 100001272:
                  case 100001274:
                  case 100001283:
                     learn = false;
                  }
               } while(MapleDataTool.getIntConvert("invisible", skillId, 0) != 0 && skillid != 27001100 && skillid != 27001201 && skillid != 51121005 && skillid != 11121000 && skillid != 12121000 && skillid != 13121000 && skillid != 14121000 && skillid != 15121000);

               if (this.getLevel() >= MapleDataTool.getIntConvert("reqLev", skillId, 0) && SkillFactory.getSkill(skillid) != null && learn) {
                  this.changeSkillLevel(SkillFactory.getSkill(skillid), maxLevel == 180 ? 1 : maxLevel, SkillFactory.getSkill(skillid).isFourthJob() ? (maxLevel == 180 ? 1 : maxLevel) : maxLevel);
               }
            }
         }
      }
   }

   public Long getSkillCustomValue(int skillid) {
      if (!this.customInfo.containsKey(skillid)) {
         return null;
      } else if (skillid == 63111009 && ((SkillCustomInfo)this.customInfo.get(skillid)).getValue() <= 0L) {
         return null;
      } else {
         return ((SkillCustomInfo)this.customInfo.get(skillid)).getValue() < 0L ? null : ((SkillCustomInfo)this.customInfo.get(skillid)).getValue();
      }
   }

   public Integer getSkillCustomTime(int skillid) {
      return this.customInfo.containsKey(skillid) ? (int)(((SkillCustomInfo)this.customInfo.get(skillid)).getEndTime() - System.currentTimeMillis()) : null;
   }

   public long getSkillCustomValue0(int skillid) {
      return this.customInfo.containsKey(skillid) ? ((SkillCustomInfo)this.customInfo.get(skillid)).getValue() : 0L;
   }

   public void removeSkillCustomInfo(int skillid) {
      if (skillid == 993192800 && this.getMapId() == 993192800) {
         this.getClient().send(CField.PunchKingPacket(this, 0, 4));
         server.Timer.EventTimer.getInstance().schedule(() -> {
            this.warp(993192701);
         }, 1000L);
      }

      this.customInfo.remove(skillid);
      if (ServerConstants.warpMap == skillid && (this.getChair() > 0 || this.getBuffedEffect(SecondaryStat.RideVehicle) != null)) {
         boolean give = false;
         if (this.getBuffedEffect(SecondaryStat.RideVehicle) != null && this.getBuffedEffect(SecondaryStat.RideVehicle).getSourceId() / 10000 == 8000) {
            give = true;
         }

         if (this.getChair() > 0) {
            give = true;
         }

         if (this.getMapId() != ServerConstants.warpMap) {
            give = false;
         }

         if (give) {
            this.getClient().send(CField.UIPacket.detailShowInfo("휴식 포인트 1 획득!", 3, 20, 20));
            this.setSkillCustomInfo(ServerConstants.warpMap, 0L, 60000L);
            long var10003 = this.getKeyValue(100161, "point");
            this.setKeyValue(100161, "point", (var10003 + 2L).makeConcatWithConstants<invokedynamic>(var10003 + 2L));
         }
      }

   }

   public void setSkillCustomInfo(int skillid, long value, long time) {
      if (this.getSkillCustomValue(skillid) != null) {
         this.removeSkillCustomInfo(skillid);
      }

      this.customInfo.put(skillid, new SkillCustomInfo(value, time));
   }

   public void addSkillCustomInfo(int skillid, long value) {
      this.customInfo.put(skillid, new SkillCustomInfo(this.getSkillCustomValue0(skillid) + value, 0L));
   }

   public Map<Integer, SkillCustomInfo> getSkillCustomValues() {
      return this.customInfo;
   }

   public final void createSecondAtom(List<SecondAtom2> atoms, Point pos) {
      SecondAtom2 atom;
      for(Iterator var3 = atoms.iterator(); var3.hasNext(); this.getMap().spawnSecondAtom(this, new MapleSecondAtom(this, atom, pos))) {
         atom = (SecondAtom2)var3.next();
         if (this.getGraveTarget() > 0) {
            atom.setTarget(this.getGraveTarget());
         }
      }

   }

   public final void createSecondAtom(List<SecondAtom2> atoms, Point pos, boolean left) {
      SecondAtom2 atom;
      for(Iterator var4 = atoms.iterator(); var4.hasNext(); this.getMap().spawnSecondAtom(this, new MapleSecondAtom(this, atom, pos), left)) {
         atom = (SecondAtom2)var4.next();
         if (this.getGraveTarget() > 0) {
            atom.setTarget(this.getGraveTarget());
         }
      }

   }

   public final void createSecondAtom(int skillid, Point pos, int num) {
      Iterator var4 = SkillFactory.getSkill(skillid).getSecondAtoms().iterator();

      while(var4.hasNext()) {
         SecondAtom2 atom = (SecondAtom2)var4.next();
         if (this.getGraveTarget() > 0) {
            atom.setTarget(this.getGraveTarget());
         }

         MapleSecondAtom msa = new MapleSecondAtom(this, atom, pos);
         msa.setNumuse(true);
         msa.setNum(num);
         this.getMap().spawnSecondAtom(this, msa);
      }

   }

   public final void createSecondAtom(SecondAtom2 atom, Point pos, boolean respawn) {
      this.getMap().spawnSecondAtom(this, new MapleSecondAtom(this, atom, pos, respawn));
   }

   public int getGraveTarget() {
      return this.graveTarget;
   }

   public int getCustomItem(int id) {
      int size = (int)this.getKeyValue(100000, id.makeConcatWithConstants<invokedynamic>(id));
      if (size == -1) {
         size = 0;
         this.setKeyValue(100000, id.makeConcatWithConstants<invokedynamic>(id), "0");
      }

      return size;
   }

   public List<Integer> getCustomInventory() {
      List<Integer> inventory = new ArrayList();
      List<CustomItem> list = GameConstants.customItems;
      Iterator var3 = list.iterator();

      while(var3.hasNext()) {
         CustomItem item = (CustomItem)var3.next();
         int size = this.getCustomItem(item.getId());
         inventory.add(size);
      }

      return inventory;
   }

   public void addCustomItem(int id) {
      int size = this.getCustomItem(id);
      String var10002 = id.makeConcatWithConstants<invokedynamic>(id);
      ++size;
      this.setKeyValue(100000, var10002, size.makeConcatWithConstants<invokedynamic>(size));
   }

   public int equippedCustomItem(CustomItem.CustomItemType type) {
      int id = (int)this.getKeyValue(100000 + type.ordinal(), "equip");
      return id;
   }

   public void equipCustomItem(int id) {
      List<CustomItem> list = GameConstants.customItems;
      CustomItem ci = (CustomItem)list.get(id);
      this.setKeyValue(100000 + ci.getType().ordinal(), "equip", id.makeConcatWithConstants<invokedynamic>(id));
   }

   public void unequipCustomItem(int id) {
      List<CustomItem> list = GameConstants.customItems;
      CustomItem ci = (CustomItem)list.get(id);
      this.setKeyValue(100000 + ci.getType().ordinal(), "equip", "-1");
   }

   public void setGraveTarget(int graveTarget) {
      this.graveTarget = graveTarget;
   }

   public void GiveHolyUnityBuff(SecondaryStatEffect effect, int skillid) {
      SecondaryStatEffect effect2 = SkillFactory.getSkill(400011003).getEffect(this.getSkillLevel(400011003));
      int duration = effect.getDuration();
      int skilly = effect2.getY();
      int skillx = this.getStat().getTotalStr() / effect2.getW();
      if (skilly + skillx >= 100) {
         duration = 100;
      } else {
         duration = skilly + skillx;
      }

      if (this.getBuffedValue(400011003) && this.getSkillCustomValue0(400011003) > 0L) {
         Iterator var7 = this.getMap().getAllCharactersThreadsafe().iterator();

         while(var7.hasNext()) {
            MapleCharacter chr2 = (MapleCharacter)var7.next();
            if ((long)chr2.getId() == this.getSkillCustomValue0(400011003)) {
               SkillFactory.getSkill(skillid).getEffect(this.getSkillLevel(skillid)).applyTo(chr2, effect.getDuration() / 100 * duration);
               break;
            }
         }
      }

   }

   public int getOrder() {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      int order = 0;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
         ps.setInt(1, this.id);
         rs = ps.executeQuery();
         if (rs.next()) {
            order = rs.getInt("order");
         }

         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var14) {
         System.err.println("Error getting character default" + var14);
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
         } catch (Exception var13) {
         }

      }

      return order;
   }

   public int getBuffedOwner(int skillid) {
      if (SkillFactory.getSkill(skillid) == null) {
         return 0;
      } else {
         Iterator var2 = this.effects.iterator();

         Pair eff;
         do {
            if (!var2.hasNext()) {
               return 0;
            }

            eff = (Pair)var2.next();
         } while(skillid != ((SecondaryStatValueHolder)eff.right).effect.getSourceId());

         return ((SecondaryStatValueHolder)eff.right).cid;
      }
   }

   public final void PartyBuffCheck(SecondaryStat stat, int skillid) {
      boolean give = false;
      MapleCharacter chr = this;
      MapleCharacter buffowner = this.getClient().getChannelServer().getPlayerStorage().getCharacterById(this.getBuffedOwner(skillid));
      if (buffowner != null) {
         if (this.getMapId() == 450013700) {
            if (this.getBuffedValue(skillid)) {
               this.cancelEffectFromBuffStat(stat);
            }

            return;
         }

         SecondaryStatEffect effect = SkillFactory.getSkill(skillid).getEffect(buffowner.getSkillLevel(skillid));
         if (!buffowner.getBuffedValue(skillid) || buffowner.getMapId() != this.getMapId()) {
            this.cancelEffectFromBuffStat(stat);
         }

         if (this.getParty() == null && this.getBuffedOwner(skillid) != this.getId()) {
            this.cancelEffectFromBuffStat(stat);
         }

         if (this.getBuffedValue(skillid)) {
            if (skillid == 51111008 && GameConstants.isMichael(this.getJob()) && this.getSkillCustomValue(51111010) == null) {
               this.addHP(this.getStat().getCurrentMaxHp() / 100L);
               this.setSkillCustomInfo(51111010, 0L, 4000L);
            }

            if (buffowner.getParty() != null && this.getParty() != null) {
               if (buffowner.getParty().getId() != this.getParty().getId()) {
                  this.cancelEffectFromBuffStat(stat);
               }

               if (skillid == 51111008) {
                  int size = 0;
                  Iterator var8 = this.getParty().getMembers().iterator();

                  while(var8.hasNext()) {
                     MaplePartyCharacter chr2 = (MaplePartyCharacter)var8.next();
                     if (chr2.isOnline()) {
                        ++size;
                     }
                  }

                  if ((long)size != this.getSkillCustomValue0(51111009) && size <= 1) {
                     this.cancelEffectFromBuffStat(stat);
                     this.removeSkillCustomInfo(51111009);
                     SkillFactory.getSkill(skillid).getEffect(this.getSkillLevel(skillid)).applyTo(this);
                  }
               }

               Iterator var10 = buffowner.getParty().getMembers().iterator();

               while(true) {
                  while(true) {
                     MaplePartyCharacter chr3;
                     MapleCharacter chr4;
                     do {
                        do {
                           do {
                              do {
                                 if (!var10.hasNext()) {
                                    if (give) {
                                       buffowner.cancelEffect(buffowner.getBuffedEffect(skillid));
                                       SkillFactory.getSkill(skillid).getEffect(buffowner.getSkillLevel(skillid)).applyTo(buffowner);
                                    }

                                    return;
                                 }

                                 chr3 = (MaplePartyCharacter)var10.next();
                              } while(chr3.getMapid() != buffowner.getMapId());

                              chr4 = chr.getClient().getChannelServer().getPlayerStorage().getCharacterByName(chr3.getName());
                           } while(chr4 == null);
                        } while(!chr3.isOnline());
                     } while(chr.getId() == chr4.getId());

                     if (buffowner.getTruePosition().x + effect.getLt().x < chr4.getTruePosition().x && buffowner.getTruePosition().x - effect.getLt().x > chr4.getTruePosition().x && buffowner.getTruePosition().y + effect.getLt().y < chr4.getTruePosition().y && buffowner.getTruePosition().y - effect.getLt().y > chr4.getTruePosition().y) {
                        if (!chr4.getBuffedValue(skillid)) {
                           SkillFactory.getSkill(skillid).getEffect(buffowner.getSkillLevel(skillid)).applyTo(buffowner, chr4);
                           if (skillid == 51111008) {
                              give = true;
                           }
                        }
                     } else if (chr4.getBuffedOwner(skillid) != chr4.getId() && chr4.getBuffedValue(skillid)) {
                        chr4.cancelEffect(chr4.getBuffedEffect(skillid));
                        if (skillid == 51111008) {
                           give = true;
                        }
                     }
                  }
               }
            }
         }
      } else if (this.getBuffedValue(skillid) && this.getBuffedOwner(skillid) != this.getId()) {
         this.cancelEffect(this.getBuffedEffect(skillid));
      }

   }

   public final void MechCarrier(int delay, final boolean reset) {
      server.Timer.EtcTimer.getInstance().schedule(new Runnable() {
         public void run() {
            if (!MapleCharacter.this.getMap().getAllMonster().isEmpty()) {
               List<Triple<Integer, Integer, Integer>> a = new ArrayList();
               if (reset) {
                  MapleCharacter.this.setSkillCustomInfo(400051069, 0L, 0L);
               }

               Iterator var2 = MapleCharacter.this.getMap().getAllSummonsThreadsafe().iterator();

               while(var2.hasNext()) {
                  MapleSummon sum = (MapleSummon)var2.next();
                  if (sum.getOwner().getId() == MapleCharacter.this.getId() && sum.getSkill() == 400051068) {
                     Iterator var4 = MapleCharacter.this.getMap().getAllMonster().iterator();

                     MapleMonster mob;
                     while(var4.hasNext()) {
                        mob = (MapleMonster)var4.next();
                        if (sum.getTruePosition().x + MapleCharacter.this.getBuffedEffect(400051068).getLt().x < mob.getTruePosition().x && sum.getTruePosition().x - MapleCharacter.this.getBuffedEffect(400051068).getLt().x > mob.getTruePosition().x && sum.getTruePosition().y + MapleCharacter.this.getBuffedEffect(400051068).getLt().y < mob.getTruePosition().y && sum.getTruePosition().y - MapleCharacter.this.getBuffedEffect(400051068).getLt().y > mob.getTruePosition().y) {
                           a.add(new Triple(mob.getObjectId(), 400051069, 6000));
                           if ((long)a.size() == MapleCharacter.this.getSkillCustomValue0(400051068)) {
                              break;
                           }
                        }
                     }

                     while((long)a.size() < MapleCharacter.this.getSkillCustomValue0(400051068)) {
                        var4 = MapleCharacter.this.getMap().getAllMonster().iterator();

                        while(var4.hasNext()) {
                           mob = (MapleMonster)var4.next();
                           if (sum.getTruePosition().x + MapleCharacter.this.getBuffedEffect(400051068).getLt().x < mob.getTruePosition().x && sum.getTruePosition().x - MapleCharacter.this.getBuffedEffect(400051068).getLt().x > mob.getTruePosition().x && sum.getTruePosition().y + MapleCharacter.this.getBuffedEffect(400051068).getLt().y < mob.getTruePosition().y && sum.getTruePosition().y - MapleCharacter.this.getBuffedEffect(400051068).getLt().y > mob.getTruePosition().y) {
                              a.add(new Triple(mob.getObjectId(), 400051069, 6000));
                              if ((long)a.size() == MapleCharacter.this.getSkillCustomValue0(400051068)) {
                                 break;
                              }
                           }
                        }
                     }

                     MapleCharacter.this.getMap().broadcastMessage(SkillPacket.CreateSubObtacle(MapleCharacter.this.getClient().getPlayer(), sum, a, 10));
                     break;
                  }
               }
            }

         }
      }, (long)delay);
   }

   public int getSpellCount(int type) {
      int count = 0;
      if (type == 0 || type == 1) {
         count += (int)this.getSkillCustomValue0(155001100);
      }

      if (type == 0 || type == 2) {
         count += (int)this.getSkillCustomValue0(155101100);
      }

      if (type == 0 || type == 3) {
         count += (int)this.getSkillCustomValue0(155111102);
      }

      if (type == 0 || type == 4) {
         count += (int)this.getSkillCustomValue0(155121102);
      }

      return count;
   }

   public void addSpell(int skillid) {
      int spell = (int)this.getSkillCustomValue0(skillid);
      if (this.getSpellCount(0) < 5) {
         if (skillid == 155001100) {
            if (this.getBuffedValue(400051036)) {
               for(int i = spell; i < 5; ++i) {
                  ++spell;
                  this.setSkillCustomInfo(155001100, (long)spell, 0L);
                  this.setSkillCustomInfo(155001101, (long)spell, 0L);
               }

               this.onPacket(1);
            } else {
               ++spell;
               this.setSkillCustomInfo(155001100, (long)spell, 0L);
               this.setSkillCustomInfo(155001101, (long)spell, 0L);
               this.onPacket(1);
            }
         } else if (skillid != 155101100 && skillid != 155101101 && skillid != 155101112) {
            if (skillid != 155111102 && skillid != 155111111) {
               if (skillid == 155121102 && spell < 1) {
                  ++spell;
                  this.setSkillCustomInfo(155121102, (long)spell, 0L);
                  this.setSkillCustomInfo(155121103, (long)spell, 0L);
                  this.onPacket(8);
               }
            } else if (spell < 1) {
               ++spell;
               this.setSkillCustomInfo(155111102, (long)spell, 0L);
               this.setSkillCustomInfo(155111103, (long)spell, 0L);
               this.onPacket(4);
            }
         } else if (spell < 1) {
            ++spell;
            this.setSkillCustomInfo(155101100, (long)spell, 0L);
            this.setSkillCustomInfo(155101101, (long)spell, 0L);
            this.onPacket(2);
         }
      }

   }

   public void onPacket(int flag) {
      Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
      if ((flag & 1) != 0) {
         statups.put(SecondaryStat.PlainBuff, new Pair(1, 0));
      }

      if ((flag & 2) != 0) {
         statups.put(SecondaryStat.ScarletBuff, new Pair(1, 0));
      }

      if ((flag & 4) != 0) {
         statups.put(SecondaryStat.GustBuff, new Pair(1, 0));
      }

      if ((flag & 8) != 0) {
         statups.put(SecondaryStat.AbyssBuff, new Pair(1, 0));
      }

      SecondaryStatEffect effects = SkillFactory.getSkill(155000007).getEffect(this.getSkillLevel(155000007));
      if ((flag & 16) != 0) {
         this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, effects, this));
      } else {
         this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, effects, this));
      }

   }

   public void useSpell() {
      int flag = 0;
      if (this.getSkillCustomValue0(155001100) > 0L) {
         this.setSkillCustomInfo(155001101, this.getSkillCustomValue0(155001100), 0L);
         this.setSkillCustomInfo(155001100, 0L, 0L);
         flag |= 1;
      }

      if (this.getSkillCustomValue0(155101100) > 0L) {
         this.setSkillCustomInfo(155101101, this.getSkillCustomValue0(155101100), 0L);
         this.setSkillCustomInfo(155101100, 0L, 0L);
         flag |= 2;
      }

      if (this.getSkillCustomValue0(155111102) > 0L) {
         this.setSkillCustomInfo(155111103, this.getSkillCustomValue0(155111102), 0L);
         this.setSkillCustomInfo(155111102, 0L, 0L);
         flag |= 4;
      }

      if (this.getSkillCustomValue0(155121102) > 0L) {
         this.setSkillCustomInfo(155121103, this.getSkillCustomValue0(155121102), 0L);
         this.setSkillCustomInfo(155121102, 0L, 0L);
         flag |= 8;
      }

      this.onPacket(flag);
   }

   public int getMwSize(int skillid) {
      int size = 0;
      Iterator var3 = this.getMap().getAllFieldThreadsafe().iterator();

      while(var3.hasNext()) {
         MapleMagicWreck mw = (MapleMagicWreck)var3.next();
         if (mw.getChr().getId() == this.getId() && skillid == mw.getSourceid()) {
            ++size;
         }
      }

      return size;
   }

   public void MonsterQuest(int quest, int maxmob) {
      String q = "";
      int count = false;
      if (this.getQuest(MapleQuest.getInstance(quest)).getCustomData() == null) {
         MapleQuest.getInstance(quest).forceStart(this, 0, "1");
      } else {
         int count = Integer.parseInt(this.getQuest(MapleQuest.getInstance(quest)).getCustomData());
         ++count;
         if (count < 10) {
            q = "00" + count;
         } else if (count < 100) {
            q = "0" + count;
         } else {
            q = count.makeConcatWithConstants<invokedynamic>(count);
         }

         if (count >= maxmob) {
            if (maxmob < 10) {
               q = "00" + maxmob;
            } else if (maxmob < 100) {
               q = "0" + maxmob;
            } else {
               q = maxmob.makeConcatWithConstants<invokedynamic>(maxmob);
            }
         }

         MapleQuest.getInstance(quest).forceStart(this, 0, q);
      }

   }

   public void EnterMultiYutGame() {
      if (MultiYutGame.multiYutMagchingQueue.contains(this)) {
         if (this.ConstentTimer != null) {
            this.ConstentTimer.cancel();
            this.ConstentTimer = null;
         }

         MultiYutGame.multiYutMagchingQueue2.add(this);
         if (MultiYutGame.multiYutMagchingQueue2.size() == 2) {
            List<Integer> id = new ArrayList();
            Iterator var2 = MultiYutGame.multiYutMagchingQueue2.iterator();

            while(var2.hasNext()) {
               MapleCharacter chr = (MapleCharacter)var2.next();
               if (chr.getClient().getChannel() != 1) {
                  MultiYutGame.multiYutMagchingQueue2.remove(chr);
                  chr.changeChannel(1);
                  id.add(chr.getId());
               }
            }

            server.Timer.EtcTimer.getInstance().schedule(() -> {
               Iterator var1 = ChannelServer.getAllInstances().iterator();

               label45:
               while(var1.hasNext()) {
                  ChannelServer cserv = (ChannelServer)var1.next();
                  Iterator var3 = cserv.getPlayerStorage().getAllCharacters().values().iterator();

                  while(true) {
                     MapleCharacter player;
                     do {
                        if (!var3.hasNext()) {
                           continue label45;
                        }

                        player = (MapleCharacter)var3.next();
                     } while(player == null);

                     Iterator var5 = id.iterator();

                     while(var5.hasNext()) {
                        Integer idc = (Integer)var5.next();
                        if (idc == player.getId() && !MultiYutGame.multiYutMagchingQueue.contains(player)) {
                           MultiYutGame.multiYutMagchingQueue.add(player);
                        }
                     }
                  }
               }

               MultiYutGame.multiYutMagchingQueue2.clear();
               var1 = MultiYutGame.multiYutMagchingQueue.iterator();

               while(var1.hasNext()) {
                  MapleCharacter chrs = (MapleCharacter)var1.next();
                  chrs.getClient().send(SLFCGPacket.ContentsWaiting(chrs, 0, 11, 5, 1, 18));
                  chrs.warp(993189800);
                  chrs.getClient().send(CField.getClock(5));
               }

            }, 4000L);
         }
      }

   }

   public void EnterBattleReverse() {
      if (BattleReverse.BattleReverseMatchingQueue.contains(this)) {
         if (this.ConstentTimer != null) {
            this.ConstentTimer.cancel();
            this.ConstentTimer = null;
         }

         BattleReverse.BattleReverseMatchingQueue2.add(this);
         if (BattleReverse.BattleReverseMatchingQueue2.size() == 2) {
            List<Integer> id = new ArrayList();
            Iterator var2 = BattleReverse.BattleReverseMatchingQueue2.iterator();

            while(var2.hasNext()) {
               MapleCharacter chr = (MapleCharacter)var2.next();
               if (chr.getClient().getChannel() != 1) {
                  BattleReverse.BattleReverseMatchingQueue.remove(chr);
                  chr.changeChannel(1);
                  id.add(chr.getId());
               }
            }

            server.Timer.EtcTimer.getInstance().schedule(() -> {
               Iterator var1 = ChannelServer.getAllInstances().iterator();

               label36:
               while(var1.hasNext()) {
                  ChannelServer cserv = (ChannelServer)var1.next();
                  Iterator var3 = cserv.getPlayerStorage().getAllCharacters().values().iterator();

                  while(true) {
                     MapleCharacter player;
                     do {
                        if (!var3.hasNext()) {
                           continue label36;
                        }

                        player = (MapleCharacter)var3.next();
                     } while(player == null);

                     Iterator var5 = id.iterator();

                     while(var5.hasNext()) {
                        Integer idc = (Integer)var5.next();
                        if (idc == player.getId() && !BattleReverse.BattleReverseMatchingQueue.contains(player)) {
                           BattleReverse.BattleReverseMatchingQueue.add(player);
                        }
                     }
                  }
               }

               BattleReverse.BattleReverseMatchingQueue2.clear();
               BattleReverse.StartGame2();
            }, 4000L);
         }
      }

   }

   public void EnterMonsterPyramid() {
      if (MonsterPyramid.monsterPyramidMatchingQueue.contains(this)) {
         if (this.ConstentTimer != null) {
            this.ConstentTimer.cancel();
            this.ConstentTimer = null;
         }

         MonsterPyramid.monsterPyramidMatchingQueue2.add(this);
         if (MonsterPyramid.monsterPyramidMatchingQueue2.size() == 3) {
            List<Integer> id = new ArrayList();
            Iterator var2 = MonsterPyramid.monsterPyramidMatchingQueue2.iterator();

            while(var2.hasNext()) {
               MapleCharacter chr = (MapleCharacter)var2.next();
               if (chr.getClient().getChannel() != 1) {
                  MonsterPyramid.monsterPyramidMatchingQueue.remove(chr);
                  chr.changeChannel(1);
                  id.add(chr.getId());
               }
            }

            server.Timer.EtcTimer.getInstance().schedule(() -> {
               Iterator var1 = ChannelServer.getAllInstances().iterator();

               label36:
               while(var1.hasNext()) {
                  ChannelServer cserv = (ChannelServer)var1.next();
                  Iterator var3 = cserv.getPlayerStorage().getAllCharacters().values().iterator();

                  while(true) {
                     MapleCharacter player;
                     do {
                        if (!var3.hasNext()) {
                           continue label36;
                        }

                        player = (MapleCharacter)var3.next();
                     } while(player == null);

                     Iterator var5 = id.iterator();

                     while(var5.hasNext()) {
                        Integer idc = (Integer)var5.next();
                        if (idc == player.getId() && !MonsterPyramid.monsterPyramidMatchingQueue.contains(player)) {
                           MonsterPyramid.monsterPyramidMatchingQueue.add(player);
                        }
                     }
                  }
               }

               MonsterPyramid.monsterPyramidMatchingQueue2.clear();
               MonsterPyramid.StartGame(3);
            }, 4000L);
         }
      }

   }

   public void CancelWating(MapleCharacter chr, int type) {
      List<MapleCharacter> remover = new ArrayList();
      Iterator var4;
      MapleCharacter chr2;
      if (type == 18) {
         MultiYutGame.multiYutMagchingQueue.remove(chr);
         var4 = MultiYutGame.multiYutMagchingQueue.iterator();

         while(var4.hasNext()) {
            chr2 = (MapleCharacter)var4.next();
            if (chr.getId() != chr2.getId()) {
               remover.add(chr2);
               chr2.getClient().send(SLFCGPacket.ContentsWaiting(chr2, 0, 11, 5, 1, type));
            }
         }

         var4 = remover.iterator();

         while(var4.hasNext()) {
            chr2 = (MapleCharacter)var4.next();
            MultiYutGame.multiYutMagchingQueue.remove(chr2);
            MultiYutGame.multiYutMagchingQueue.remove(chr2);
         }

         chr.getClient().send(SLFCGPacket.ContentsWaiting(chr, 0, 11, 5, 1, type));
      } else if (type == 24) {
         MonsterPyramid.monsterPyramidMatchingQueue.remove(chr);
         var4 = MonsterPyramid.monsterPyramidMatchingQueue.iterator();

         while(var4.hasNext()) {
            chr2 = (MapleCharacter)var4.next();
            if (chr.getId() != chr2.getId()) {
               remover.add(chr2);
               chr2.getClient().send(SLFCGPacket.ContentsWaiting(chr2, 0, 11, 5, 1, type));
            }
         }

         var4 = remover.iterator();

         while(var4.hasNext()) {
            chr2 = (MapleCharacter)var4.next();
            MonsterPyramid.monsterPyramidMatchingQueue.remove(chr2);
            MonsterPyramid.monsterPyramidMatchingQueue2.remove(chr2);
         }

         chr.getClient().send(SLFCGPacket.ContentsWaiting(chr, 0, 11, 5, 1, type));
      } else if (type == 23) {
         BattleReverse.BattleReverseMatchingQueue.remove(chr);
         var4 = BattleReverse.BattleReverseMatchingQueue.iterator();

         while(var4.hasNext()) {
            chr2 = (MapleCharacter)var4.next();
            if (chr.getId() != chr2.getId()) {
               remover.add(chr2);
               chr2.getClient().send(SLFCGPacket.ContentsWaiting(chr2, 0, 11, 5, 1, type));
            }
         }

         var4 = remover.iterator();

         while(var4.hasNext()) {
            chr2 = (MapleCharacter)var4.next();
            BattleReverse.BattleReverseMatchingQueue.remove(chr2);
            BattleReverse.BattleReverseMatchingQueue2.remove(chr2);
         }

         chr.getClient().send(SLFCGPacket.ContentsWaiting(chr, 0, 11, 5, 1, type));
      }

   }

   public void startSound() {
      this.client.getSession().writeAndFlush(SLFCGPacket.playSE("MiniGame.img/multiBingo/3"));
      server.Timer.EtcTimer.getInstance().schedule(new Runnable() {
         public void run() {
            MapleCharacter.this.client.getSession().writeAndFlush(SLFCGPacket.playSE("MiniGame.img/multiBingo/2"));
         }
      }, 1000L);
      server.Timer.EtcTimer.getInstance().schedule(new Runnable() {
         public void run() {
            MapleCharacter.this.client.getSession().writeAndFlush(SLFCGPacket.playSE("MiniGame.img/multiBingo/1"));
         }
      }, 2000L);
      server.Timer.EtcTimer.getInstance().schedule(new Runnable() {
         public void run() {
            MapleCharacter.this.client.getSession().writeAndFlush(SLFCGPacket.playSE("MiniGame.img/multiBingo/start"));
         }
      }, 3000L);
   }

   public void setGuildName(String name) {
      this.guildName = name;
   }

   public String getGuildName() {
      return this.guildName;
   }

   public long getLastDisconnectTime() {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
         ps.setInt(1, this.id);
         rs = ps.executeQuery();
         if (rs.next()) {
            this.lastDisconnectTime = rs.getLong("lastDisconnectTime");
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

      return this.lastDisconnectTime;
   }

   public void setLastDisconnectTime(long lastDisconnectTime) {
      this.lastDisconnectTime = lastDisconnectTime;
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE characters SET lastDisconnectTime = ? WHERE id = ?");
         ps.setLong(1, this.lastDisconnectTime);
         ps.setInt(2, this.id);
         ps.execute();
         ps.close();
         con.close();
      } catch (SQLException var15) {
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
         } catch (Exception var14) {
         }

      }

   }

   public void ReHolyUnityBuff(SecondaryStatEffect effect) {
      Map<SecondaryStat, Pair<Integer, Integer>> localstatups = new HashMap();
      Rectangle bounds = effect.calculateBoundingBox(this.getTruePosition(), this.isFacingLeft());
      List<MapleMapObject> affecteds = this.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.PLAYER));
      if (this.getParty() != null) {
         Iterator var5 = affecteds.iterator();

         while(var5.hasNext()) {
            MapleMapObject affectedmo = (MapleMapObject)var5.next();
            MapleCharacter affected = (MapleCharacter)affectedmo;
            if (affected.getParty() != null && this.getId() != affected.getId() && this.getParty().getId() == affected.getParty().getId()) {
               localstatups.clear();
               localstatups.put(SecondaryStat.HolyUnity, new Pair(affected.getId(), (int)this.getBuffLimit(400011003)));
               this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(localstatups, effect, this));
               this.getMap().broadcastMessage(this, CWvsContext.BuffPacket.giveForeignBuff(this, localstatups, effect), false);
               affected.getMap().broadcastMessage(affected, CField.EffectPacket.showEffect(affected, 0, 400011003, 1, 0, 0, (byte)(affected.getTruePosition().x > affected.getPosition().x ? 1 : 0), false, affected.getPosition(), (String)null, (Item)null), false);
               this.setSkillCustomInfo(400011003, (long)affected.getId(), 0L);
               SkillFactory.getSkill(400011021).getEffect(this.getSkillLevel(400011003)).applyTo(this, affected, false, affected.getPosition(), (int)this.getBuffLimit(400011003), (byte)0, false);
               break;
            }
         }
      }

   }

   public Item getAttackitem() {
      return this.attackitem;
   }

   public void setAttackitem(Item attackitem) {
      this.attackitem = attackitem;
   }

   public void getAttackDealy(AttackInfo attack, boolean spskill) {
   }

   public void getYetiGauge(int skillid, int type) {
      if (this.getBuffedEffect(SecondaryStat.YetiAngerMode) == null && skillid != 999) {
         SecondaryStatEffect eff = SkillFactory.getSkill(skillid).getEffect(this.getSkillLevel(skillid));
         int up = 0;
         switch(skillid) {
         case 135001000:
         case 135001001:
         case 135001002:
            up = type == 1 ? 1 : (type == 2 ? 3 : 0);
         case 135001003:
         case 135001004:
         case 135001005:
         case 135001006:
         case 135001007:
         case 135001009:
         case 135001010:
         case 135001012:
         case 135001013:
         case 135001014:
         case 135001017:
         case 135001018:
         default:
            break;
         case 135001008:
         case 135001011:
         case 135001015:
         case 135001016:
         case 135001019:
            if (type != 1) {
               up = eff.getX();
            }

            if (this.getBuffedEffect(SecondaryStat.YetiSpicy) != null) {
               up += 10;
            }
         }

         if (skillid == 999) {
            up += 2;
         }

         if (up > 0) {
            this.addSkillCustomInfo(135001005, (long)up);
            if (this.getSkillCustomValue0(135001005) >= 300L) {
               this.setSkillCustomInfo(135001005, 300L, 0L);
            }

            SkillFactory.getSkill(135001005).getEffect(1).applyTo(this, false);
         }
      }

   }

   public void Recharge(int skillid) {
      switch(skillid) {
      case 65001100:
      case 65101100:
      case 65111007:
      case 65111100:
      case 65111101:
      case 65121008:
      case 65121100:
      case 65121101:
         if (skillid == 65111007) {
            skillid = 65111100;
         }

         if (skillid != 65111100) {
            this.getClient().send(CField.lockSkill(skillid));
         }

         int lv = this.getSkillLevel(65000003);
         int lv2 = this.getSkillLevel(65100005);
         int lv3 = this.getSkillLevel(65110006);
         int lv4 = this.getSkillLevel(65120006);
         SecondaryStatEffect eff = null;
         SecondaryStatEffect eff2 = null;
         SecondaryStatEffect eff3 = null;
         SecondaryStatEffect eff4 = null;
         if (lv > 0) {
            eff = SkillFactory.getSkill(65000003).getEffect(lv);
         }

         if (lv2 > 0) {
            eff2 = SkillFactory.getSkill(65100005).getEffect(lv2);
         }

         if (lv3 > 0) {
            eff3 = SkillFactory.getSkill(65110006).getEffect(lv3);
         }

         if (lv4 > 0) {
            eff4 = SkillFactory.getSkill(65120006).getEffect(lv4);
         }

         int prop = SkillFactory.getSkill(skillid).getEffect(this.getTotalSkillLevel(GameConstants.getLinkedSkill(skillid))).getOnActive();
         if (lv > 0) {
            prop += eff.getX();
         }

         if (skillid == 65111100) {
            prop = 30;
         }

         if (Randomizer.isSuccess(prop)) {
            this.getClient().getSession().writeAndFlush(CField.unlockSkill());
            this.getClient().getSession().writeAndFlush(CField.EffectPacket.showNormalEffect(this, 49, true));
            this.getMap().broadcastMessage(this, CField.EffectPacket.showNormalEffect(this, 49, false), false);
            this.removeSkillCustomInfo(65110006);
            if (lv4 > 0 && Randomizer.isSuccess(50)) {
               eff4.applyTo(this);
            }
         } else if (skillid != 65111100) {
            if (lv3 > 0) {
               this.setSkillCustomInfo(65110006, this.getSkillCustomValue0(65110006) + 1L, 0L);
               if (this.getSkillCustomValue0(65110006) == 2L) {
                  this.getClient().getSession().writeAndFlush(CField.unlockSkill());
                  this.removeSkillCustomInfo(65110006);
               }
            }

            if (lv4 > 0) {
               this.Recharge(skillid);
            }
         }
      default:
      }
   }

   public MapleBattleGroundCharacter getBattleGroundChr() {
      Iterator var1 = MapleBattleGroundCharacter.bchr.iterator();

      MapleBattleGroundCharacter gchr;
      do {
         if (!var1.hasNext()) {
            return null;
         }

         gchr = (MapleBattleGroundCharacter)var1.next();
      } while(gchr.getId() != this.getId());

      return gchr;
   }

   public String getBattleGrondJobName() {
      return this.BattleGrondJobName;
   }

   public void setBattleGrondJobName(String BattleGrondJobName) {
      this.BattleGrondJobName = BattleGrondJobName;
   }

   public void getPercentDamage(MapleMonster monster, int skillid, int skillLevel, int percent, boolean show) {
      if (this.isAlive() && this.getBuffedEffect(SecondaryStat.TrueSniping) == null && this.getBuffedEffect(SecondaryStat.Etherealform) == null && this.getBuffedEffect(SecondaryStat.IndieNotDamaged) == null && this.getBuffedEffect(SecondaryStat.NotDamaged) == null) {
         if (this.getBuffedEffect(SecondaryStat.HolyMagicShell) != null) {
            if (this.getHolyMagicShell() > 1) {
               this.setHolyMagicShell((byte)(this.getHolyMagicShell() - 1));
               Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
               statups.put(SecondaryStat.HolyMagicShell, new Pair(Integer.valueOf(this.getHolyMagicShell()), (int)this.getBuffLimit(this.getBuffSource(SecondaryStat.HolyMagicShell))));
               this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, this.getBuffedEffect(SecondaryStat.HolyMagicShell), this));
               this.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(this, 1, 0, 36, 0, 0, (byte)0, true, (Point)null, (String)null, (Item)null));
               this.getMap().broadcastMessage(this, CField.EffectPacket.showEffect(this, 1, 0, 36, 0, 0, (byte)0, false, (Point)null, (String)null, (Item)null), false);
            } else {
               this.cancelEffectFromBuffStat(SecondaryStat.HolyMagicShell);
            }
         } else if (this.getBuffedValue(4341052)) {
            this.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(this, 1, 0, 36, 0, 0, (byte)0, true, (Point)null, (String)null, (Item)null));
            this.getMap().broadcastMessage(this, CField.EffectPacket.showEffect(this, 1, 0, 36, 0, 0, (byte)0, false, (Point)null, (String)null, (Item)null), false);
         } else {
            int reduce;
            if (this.getBuffedEffect(SecondaryStat.WindWall) != null) {
               reduce = Math.max(0, this.getBuffedValue(SecondaryStat.WindWall) - 100 * this.getBuffedEffect(SecondaryStat.WindWall).getZ());
               if (reduce > 1) {
                  this.setBuffedValue(SecondaryStat.WindWall, reduce);
                  Map<SecondaryStat, Pair<Integer, Integer>> statups2 = new HashMap();
                  statups2.put(SecondaryStat.WindWall, new Pair(reduce, (int)this.getBuffLimit(400031030)));
                  this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups2, this.getBuffedEffect(SecondaryStat.WindWall), this));
               } else {
                  this.cancelEffectFromBuffStat(SecondaryStat.WindWall);
               }
            } else {
               reduce = 0;
               if (this.getBuffedEffect(SecondaryStat.IndieDamageReduce) != null) {
                  reduce = this.getBuffedValue(SecondaryStat.IndieDamageReduce);
               } else if (this.getBuffedEffect(SecondaryStat.IndieDamReduceR) != null) {
                  reduce = -this.getBuffedValue(SecondaryStat.IndieDamReduceR);
               }

               int minushp = -((int)(this.getStat().getCurrentMaxHp() * (long)(percent - reduce) / 100L));
               if (show) {
                  this.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(this, skillid, skillLevel, 45, 0, 0, (byte)0, true, (Point)null, (String)null, (Item)null));
                  this.getMap().broadcastMessage(this, CField.EffectPacket.showEffect(this, skillid, skillLevel, 45, 0, 0, (byte)0, false, (Point)null, (String)null, (Item)null), false);
               }

               this.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(this, 0, minushp, 36, 0, 0, (byte)0, true, (Point)null, (String)null, (Item)null));
               this.getMap().broadcastMessage(this, CField.EffectPacket.showEffect(this, 0, minushp, 36, 0, 0, (byte)0, false, (Point)null, (String)null, (Item)null), false);
               this.addHP((long)minushp);
            }
         }
      }

   }

   public long getAggressiveDamage() {
      return this.AggressiveDamage;
   }

   public void setAggressiveDamage(long AggressiveDamage) {
      this.AggressiveDamage = AggressiveDamage;
   }

   public void startLotteryretry() {
      server.Timer.EtcTimer.getInstance().schedule(new Runnable() {
         public void run() {
            MapleCharacter.this.client.send(CField.showEffect("defense/count"));
            MapleCharacter.this.startSound();
         }
      }, 3000L);
   }

   public void startLottery() {
      this.client.send(CField.showEffect("Map/Effect2.img/starplanet/default"));
      this.client.send(CField.showEffect("defense/count"));
      this.startSound();
      server.Timer.EtcTimer.getInstance().schedule(new Runnable() {
         public void run() {
            MapleCharacter.this.client.send(CField.showEffect("Map/Effect2.img/starplanet/1_1"));
            MapleCharacter.this.client.send(CField.ImageTalkNpc(9000198, 2500, "#b#h ##k! 자네 운이 상당히 좋구만! 축하한다네! 바로 다음것을 긁어보게나!"));
            MapleCharacter.this.startLotteryretry();
            MapleCharacter.this.startLottery2();
         }
      }, 3100L);
   }

   public void startLottery2() {
      server.Timer.EtcTimer.getInstance().schedule(new Runnable() {
         public void run() {
            MapleCharacter.this.client.send(CField.showEffect("Map/Effect2.img/starplanet/1_2"));
            MapleCharacter.this.client.send(CField.ImageTalkNpc(9000198, 2500, "#b#h ##k! 자네 운이 상당히 좋구만! 축하한다네! 바로 다음것을 긁어보게나!"));
            MapleCharacter.this.startLottery3();
            MapleCharacter.this.startLotteryretry();
         }
      }, 6200L);
   }

   public void startLottery3() {
      server.Timer.EtcTimer.getInstance().schedule(new Runnable() {
         public void run() {
            MapleCharacter.this.client.send(CField.showEffect("Map/Effect2.img/starplanet/1_3"));
            MapleCharacter.this.client.send(CField.ImageTalkNpc(9000198, 2500, "#b#h ##k! 자네 운이 상당히 좋구만! 축하한다네! 바로 다음것을 긁어보게나!"));
         }
      }, 6200L);
   }

   public void showNotes(int id) {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE `notes` SET `show` = ? WHERE `id` = ?");
         ps.setInt(1, 0);
         ps.setInt(2, id);
         ps.execute();
         ps.close();
         con.close();
      } catch (SQLException var14) {
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
         } catch (Exception var13) {
         }

      }

   }

   public void gainCabinetItem(int itemid, int count) {
      MapleCabinet cb = new MapleCabinet(this.client.getAccID(), itemid, count, "[Heinz]", "운영자가 보낸 선물입니다.보관 기간 내에 수령하지 않을 시 보관함에서 사라집니다.", PacketHelper.getKoreanTimestamp(System.currentTimeMillis() + 86400000L), 0);
      this.client.getCabiNet().add(cb);
      this.client.saveCabiNet(this.client.getCabiNet());
      this.client.send(CField.getMapleCabinetList(this.client.getCabiNet(), false, 0, true));
   }

   public void gainCabinetItemPlayer(int itemid, int count, int prioed, String st) {
      MapleCabinet cb = new MapleCabinet(this.client.getAccID(), itemid, count, "[Heinz]", st, PacketHelper.getKoreanTimestamp(System.currentTimeMillis() + (long)prioed * 24L * 60L * 60L * 1000L), 0);
      cb.setPlayerid(this.id);
      cb.setName(this.name);
      this.client.getCabiNet().add(cb);
      this.client.saveCabiNet(this.client.getCabiNet());
      this.client.send(CField.getMapleCabinetList(this.client.getCabiNet(), false, 0, true));
   }

   public int getSerenStunGauge() {
      return this.SerenStunGauge;
   }

   public void setSerenStunGauge(int SerenStunGauge) {
      this.SerenStunGauge = SerenStunGauge;
   }

   public void addSerenGauge(int add) {
      if (!this.hasDisease(SecondaryStat.SerenDebuff)) {
         this.SerenStunGauge += add;
         if (this.SerenStunGauge >= 1000) {
            this.SerenStunGauge = 0;
            EnumMap<SecondaryStat, Pair<Integer, Integer>> diseases = new EnumMap(SecondaryStat.class);
            diseases.put(SecondaryStat.GiveMeHeal, new Pair(1, 5000));
            diseases.put(SecondaryStat.SerenDebuff, new Pair(1, 5000));
            diseases.put(SecondaryStat.SerenDebuffUnk, new Pair(1, 5000));
            this.client.send(SLFCGPacket.playSound("Sound/Field.img/SerenDeath/effect"));
            this.client.send(SLFCGPacket.PoloFrittoEffect(4, "UI/UIWindow7.img/SerenDeath"));
            this.giveDebuff(SecondaryStat.GiveMeHeal, MobSkillFactory.getMobSkill(182, 3));
            MapleMonster seren;
            if (this.getMapId() == 410002060 && (seren = this.getMap().getMonsterById(8880602)) != null) {
               seren.setSerenMidNightSetTotalTime(seren.getSerenMidNightSetTotalTime() - 1);
               if (seren.getSerenMidNightSetTotalTime() <= 0) {
                  this.getMap().broadcastMessage(MobPacket.BossSeren.SerenTimer(1, 120, 120, 0, 120, seren.getSerenTimetype() == 4 ? -1 : 1));
                  this.getMap().killAllMonsters(false);
                  this.getMap().broadcastMessage(SLFCGPacket.BlackLabel("#fn나눔고딕 ExtraBold##fs32##r#e태양이 지지 않는다면 누구도 나에게 대항할 수 없다.", 100, 1500, 4, 0, 0, 1, 4));
                  Iterator var4 = this.getMap().getAllCharactersThreadsafe().iterator();

                  while(var4.hasNext()) {
                     MapleCharacter chr = (MapleCharacter)var4.next();
                     if (chr != null) {
                        chr.warpdelay(410000670, 7);
                     }
                  }
               } else {
                  switch(seren.getSerenTimetype()) {
                  case 1:
                     seren.setSerenNoonNowTime(seren.getSerenNoonNowTime() + 1);
                     seren.setSerenNoonTotalTime(seren.getSerenNoonTotalTime() + 1);
                     break;
                  case 2:
                     seren.setSerenSunSetNowTime(seren.getSerenSunSetNowTime() + 1);
                     seren.setSerenSunSetTotalTime(seren.getSerenSunSetTotalTime() + 1);
                  case 3:
                  default:
                     break;
                  case 4:
                     seren.setSerenDawnSetNowTime(seren.getSerenDawnSetNowTime() + 1);
                     seren.setSerenDawnSetTotalTime(seren.getSerenDawnSetTotalTime() + 1);
                  }

                  if (seren.getSerenTimetype() != 3) {
                     seren.AddSerenTotalTimeHandler(seren.getSerenTimetype(), 1, seren.getSerenTimetype() == 4 ? -1 : 1);
                  }
               }
            }
         }

         if (this.SerenStunGauge < 0) {
            this.SerenStunGauge = 0;
         }

         this.client.send(MobPacket.BossSeren.SerenUserStunGauge(1000, this.SerenStunGauge));
      }

   }

   public int getSuccessor() {
      return (int)this.getKeyValue(58377, "Successor");
   }

   public void setSuccessor(int Successors) {
      long Successors1 = this.getKeyValue(58377, "Successor") + 1L;
      this.level = (short)((int)((long)this.level + Successors1));
      this.setKeyValue(58377, "Successor", Successors.makeConcatWithConstants<invokedynamic>(Successors));
   }

   public void setEffect2(int index, int value) {
      this.setKeyValue(0, "effect-" + index, String.valueOf(value));
   }

   public int getEffectValue2(int index) {
      int value = (int)this.getKeyValue(0, "effect-" + index);
      if (value == -1) {
         this.setEffect2(index, 0);
         return 0;
      } else {
         return value;
      }
   }

   public List<Integer> getPrevBonusEffect2() {
      List<Integer> effects = new ArrayList();

      for(int i = 0; i < 5; ++i) {
         effects.add(this.getEffectValue2(i));
      }

      return effects;
   }

   public List<Integer> getsuccessorEffect() {
      List<Integer> effects2 = new ArrayList();
      int pmdR = 0;
      int Str = 0;
      int Dex = 0;
      int Int_ = 0;
      int Luk = 0;
      int successor = this.getSuccessor();
      if (successor > 0) {
         pmdR = successor == 10 ? 100 : (successor == 9 ? 90 : (successor == 8 ? 80 : (successor == 7 ? 70 : (successor == 6 ? 60 : (successor == 5 ? 50 : (successor == 4 ? 40 : (successor == 3 ? 30 : (successor == 2 ? 20 : 10))))))));
         Str = successor == 10 ? 15000 : (successor == 9 ? 10000 : (successor == 8 ? 9000 : (successor == 7 ? 8000 : (successor == 6 ? 7000 : (successor == 5 ? 6000 : (successor == 4 ? 5000 : (successor == 3 ? 4000 : (successor == 2 ? 3000 : 2000))))))));
         Dex = successor == 10 ? 15000 : (successor == 9 ? 10000 : (successor == 8 ? 9000 : (successor == 7 ? 8000 : (successor == 6 ? 7000 : (successor == 5 ? 6000 : (successor == 4 ? 5000 : (successor == 3 ? 4000 : (successor == 2 ? 3000 : 2000))))))));
         Int_ = successor == 10 ? 15000 : (successor == 9 ? 10000 : (successor == 8 ? 9000 : (successor == 7 ? 8000 : (successor == 6 ? 7000 : (successor == 5 ? 6000 : (successor == 4 ? 5000 : (successor == 3 ? 4000 : (successor == 2 ? 3000 : 2000))))))));
         Luk = successor == 10 ? 15000 : (successor == 9 ? 10000 : (successor == 8 ? 9000 : (successor == 7 ? 8000 : (successor == 6 ? 7000 : (successor == 5 ? 6000 : (successor == 4 ? 5000 : (successor == 3 ? 4000 : (successor == 2 ? 3000 : 2000))))))));
      }

      int Str_ = Str / 1;
      int Dex_ = Dex / 1;
      int Int__ = Int_ / 1;
      int Luk_ = Luk / 1;
      effects2.add(Str_);
      effects2.add(Dex_);
      effects2.add(Int__);
      effects2.add(Luk_);
      effects2.add(pmdR);
      return effects2;
   }

   public void setEffect(int index, int value) {
      this.setKeyValue(0, "effect-" + index, String.valueOf(value));
   }

   public int getEffectValue(int index) {
      int value = (int)this.getKeyValue(0, "effect-" + index);
      if (value == -1) {
         this.setEffect(index, 0);
         return 0;
      } else {
         return value;
      }
   }

   public List<Integer> getPrevBonusEffect() {
      List<Integer> effects = new ArrayList();

      for(int i = 0; i < 8; ++i) {
         effects.add(this.getEffectValue(i));
      }

      return effects;
   }

   public List<Integer> getBonusEffect() {
      List<Integer> effects = new ArrayList();
      int damR = 0;
      int expR = 0;
      int dropR = 0;
      int mesoR = 0;
      int crD = 0;
      int bdR = 0;
      int allStatR = 0;
      int pmdR = 0;
      int Pad = false;
      int Mad = false;
      long zodiacRank = this.getKeyValue(190823, "grade");
      if (zodiacRank > 0L) {
         dropR = (int)((long)dropR + 10L * zodiacRank);
         mesoR = (int)((long)mesoR + 10L * zodiacRank);
         crD = (int)((long)crD + 5L * zodiacRank);
         bdR = (int)((long)bdR + 5L * zodiacRank);
      }

      CustomItem.CustomItemType[] var14 = CustomItem.CustomItemType.values();
      int var15 = var14.length;

      for(int var16 = 0; var16 < var15; ++var16) {
         CustomItem.CustomItemType type = var14[var16];
         if (type.ordinal() != 0) {
            this.equippedCustomItem(type);
         }
      }

      int hGrade;
      if (this.getKeyValue(9919, "DamageTear") > 0L) {
         hGrade = (int)this.getKeyValue(9919, "DamageTear");
         damR += hGrade == 8 ? 240 : (hGrade == 7 ? 190 : (hGrade == 6 ? 145 : (hGrade == 5 ? 105 : (hGrade == 4 ? 75 : (hGrade == 3 ? 55 : (hGrade == 2 ? 35 : 10))))));
      }

      if (this.getKeyValue(9919, "ExpTear") > 0L) {
         hGrade = (int)this.getKeyValue(9919, "ExpTear");
         expR += hGrade == 8 ? 184 : (hGrade == 7 ? 124 : (hGrade == 6 ? 84 : (hGrade == 5 ? 54 : (hGrade == 4 ? 31 : (hGrade == 3 ? 23 : (hGrade == 2 ? 10 : 3))))));
      }

      if (this.getKeyValue(9919, "DropTear") > 0L) {
         hGrade = (int)this.getKeyValue(9919, "DropTear");
         dropR += hGrade == 8 ? 300 : (hGrade == 7 ? 260 : (hGrade == 6 ? 220 : (hGrade == 5 ? 180 : (hGrade == 4 ? 150 : (hGrade == 3 ? 120 : (hGrade == 2 ? 80 : 40))))));
      }

      if (this.getKeyValue(9919, "MesoTear") > 0L) {
         hGrade = (int)this.getKeyValue(9919, "MesoTear");
         mesoR += hGrade == 8 ? 300 : (hGrade == 7 ? 180 : (hGrade == 6 ? 120 : (hGrade == 5 ? 80 : (hGrade == 4 ? 60 : (hGrade == 3 ? 40 : (hGrade == 2 ? 30 : 10))))));
      }

      if (this.getKeyValue(9919, "CridamTear") > 0L) {
         hGrade = (int)this.getKeyValue(9919, "CridamTear");
         crD += hGrade == 8 ? 300 : (hGrade == 7 ? 200 : (hGrade == 6 ? 130 : (hGrade == 5 ? 80 : (hGrade == 4 ? 50 : (hGrade == 3 ? 30 : (hGrade == 2 ? 15 : 5))))));
      }

      if (this.getKeyValue(9919, "BossdamTear") > 0L) {
         hGrade = (int)this.getKeyValue(9919, "BossdamTear");
         bdR += hGrade == 8 ? 450 : (hGrade == 7 ? 300 : (hGrade == 6 ? 280 : (hGrade == 5 ? 180 : (hGrade == 4 ? 110 : (hGrade == 3 ? 60 : (hGrade == 2 ? 30 : 10))))));
      }

      if (this.getKeyValue(800023, "indiepmer") > 0L) {
         pmdR = (int)((long)pmdR + this.getKeyValue(800023, "indiepmer"));
      }

      if (this.getKeyValue(800023, "indiepmer3") > 0L) {
         damR = (int)((long)damR + this.getKeyValue(800023, "indiepmer3"));
      }

      if (this.getKeyValue(800023, "indiepmer2") > 0L) {
         bdR = (int)((long)bdR + this.getKeyValue(800023, "indiepmer2"));
      }

      hGrade = this.getHgrade();
      if (hGrade > 0) {
         pmdR += hGrade == 10 ? 55 : (hGrade == 9 ? 50 : (hGrade == 8 ? 45 : (hGrade == 7 ? 40 : (hGrade == 6 ? 35 : (hGrade == 5 ? 30 : (hGrade == 4 ? 25 : (hGrade == 3 ? 20 : (hGrade == 2 ? 15 : 10))))))));
      }

      effects.add(damR);
      effects.add(expR);
      effects.add(dropR);
      effects.add(mesoR);
      effects.add(crD);
      effects.add(bdR);
      effects.add(Integer.valueOf(allStatR));
      effects.add(pmdR);
      return effects;
   }

   public void SetUnionPriset(int priset) {
      int quest = priset == 3 ? 500011 : (priset == 4 ? 500012 : (priset == 5 ? 500013 : 0));
      if (quest > 0) {
         this.client.setKeyValue("prisetOpen" + priset, "1");
         if (this.getKeyValue(quest + 10, "endDate") != 1L) {
            this.setKeyValue(quest + 10, "endDate", "1");
         }

         this.updateInfoQuest(quest, "endDate=99/12/31/12/59");
      }

   }

   public void checkRestDayMonday() {
      KoreaCalendar kc = new KoreaCalendar();
      long keys = Long.parseLong(this.getV("EnterDayWeekMonday"));
      Calendar clear = new GregorianCalendar((int)keys / 10000, (int)(keys % 10000L / 100L) - 1, (int)keys % 100);
      Calendar ocal = Calendar.getInstance();
      int yeal = clear.get(1);
      int days = clear.get(5);
      int day2 = clear.get(7);
      int maxday = clear.getMaximum(5);
      int month = clear.get(2);
      int check = day2 == 7 ? 2 : (day2 == 6 ? 3 : (day2 == 5 ? 4 : (day2 == 4 ? 5 : (day2 == 3 ? 6 : (day2 == 2 ? 7 : (day2 == 1 ? 1 : 0))))));
      int afterday = days + check;
      if (afterday > maxday) {
         afterday -= maxday;
         ++month;
      }

      if (month > 12) {
         ++yeal;
         month = 1;
      }

      Calendar after = new GregorianCalendar(yeal, month, afterday);
      if (after.getTimeInMillis() < System.currentTimeMillis()) {
         this.removeV("EnterDayWeekMonday");
         String var10002 = kc.getYears();
         this.addKV("EnterDayWeekMonday", var10002 + kc.getMonths() + kc.getDays());
         this.removeKeyValue(34151);
         this.removeV("ArcQuest6");
         this.removeV("ArcQuest7");
         this.setKeyValue(100466, "Score", "0");
         this.setKeyValue(100466, "Floor", "0");
         Iterator var14 = this.getQuest_Map().entrySet().iterator();

         while(var14.hasNext()) {
            Entry<MapleQuest, MapleQuestStatus> quest = (Entry)var14.next();
            if (quest != null && ((MapleQuest)quest.getKey()).getName().contains("[주간 퀘스트]")) {
               ((MapleQuestStatus)quest.getValue()).setStatus((byte)0);
               ((MapleQuestStatus)quest.getValue()).setCustomData("");
               this.client.send(CWvsContext.InfoPacket.updateQuest((MapleQuestStatus)quest.getValue()));
            }
         }
      }

   }

   public boolean checkRestDay(boolean weekly, boolean acc) {
      KoreaCalendar kc = new KoreaCalendar();
      boolean checkd = true;
      String key = weekly ? "EnterDayWeek" : "EnterDay";
      List<Core> del = new ArrayList();
      Iterator var8 = this.getCore().iterator();

      Core m;
      while(var8.hasNext()) {
         m = (Core)var8.next();
         if (m.getPeriod() > 0L && m.getPeriod() <= System.currentTimeMillis()) {
            del.add(m);
         }
      }

      if (!del.isEmpty()) {
         var8 = del.iterator();

         while(var8.hasNext()) {
            m = (Core)var8.next();
            this.getCore().remove(m);
         }

         this.dropMessage(5, "특수 코어가 기간이 지나 삭제 되었습니다.");
         this.client.send(CWvsContext.UpdateCore(this));
      }

      if (weekly) {
         long keys = Long.parseLong(this.getV(key));
         if (acc) {
            keys = Long.parseLong(this.client.getKeyValue(key));
         }

         Calendar clear = new GregorianCalendar((int)keys / 10000, (int)(keys % 10000L / 100L) - 1, (int)keys % 100);
         Calendar ocal = Calendar.getInstance();
         int yeal = clear.get(1);
         int days = clear.get(5);
         int day = ocal.get(7);
         int day2 = clear.get(7);
         int maxday = clear.getMaximum(5);
         int month = clear.get(2);
         int check = day2 == 5 ? 7 : (day2 == 6 ? 6 : (day2 == 7 ? 5 : 0));
         int afterday;
         if (check == 0) {
            for(afterday = day2; afterday < 5; ++afterday) {
               ++check;
            }
         }

         afterday = days + check;
         if (afterday > maxday) {
            afterday -= maxday;
            ++month;
         }

         if (month > 12) {
            ++yeal;
            month = 1;
         }

         Calendar after = new GregorianCalendar(yeal, month, afterday);
         if (after.getTimeInMillis() > System.currentTimeMillis()) {
            checkd = false;
         }
      } else {
         String var10000;
         String var10001;
         if (acc) {
            var10000 = this.client.getKeyValue(key);
            var10001 = kc.getYears();
            if (var10000.equals(var10001 + kc.getMonths() + kc.getDays())) {
               checkd = false;
            }
         } else {
            var10000 = this.getV(key);
            var10001 = kc.getYears();
            if (var10000.equals(var10001 + kc.getMonths() + kc.getDays())) {
               checkd = false;
            }
         }
      }

      if (checkd) {
         if (!weekly && Long.parseLong(this.getV(key)) % 10000L / 100L != (long)kc.getMonth() && ServerConstants.Event_MapleLive) {
            for(int j = 501497; j <= 501522; ++j) {
            }
         }

         this.ResetData(weekly, acc);
      }

      return checkd;
   }

   public void ResetData(boolean weekly, boolean acc) {
      KoreaCalendar kc = new KoreaCalendar();
      String key = weekly ? "EnterDayWeek" : "EnterDay";
      String[] clientDateWeekKeyValues = new String[]{"UnionCoinNujuk", "WishCoin", "WishCoinWeekGain"};
      List<Pair<Integer, String>> DateWeekKeyValues = new ArrayList(Arrays.asList(new Pair(1068, "count")));
      List<Pair<Integer, String>> clientCustomDatasWeek = new ArrayList(Arrays.asList(new Pair(100795, "weekspoint")));
      List<Pair<Integer, String>> clientCustomKeyValuesWeek = new ArrayList(Arrays.asList(new Pair(501468, "week"), new Pair(501470, "weeklyF"), new Pair(501468, "reward")));
      String[] clientDateKeyValues = new String[]{"Tester0", "Tester1", "Tester2", "Tester3", "Tester4", "Tester5", "Tester6", "Tester7", "Tester8", "Tester9", "Tester10", "Tester11", "Tester12", "Tester13", "Tester14", "Tester15", "Tester9", "jump_1", "jump_2", "jump_3", "jump_4", "jump_5", "jump_6", "dailyGiftComplete", "mPark", "mpark_t", "BloomingReward", "TyKitchenReward", "minigame"};
      List<Pair<Integer, String>> DateKeyValues = new ArrayList(Arrays.asList(new Pair(100794, "today"), new Pair(100794, "lock"), new Pair(501367, "reward"), new Pair(210302, "GP")));
      List<Pair<Integer, String>> clientCustomDatas = new ArrayList(Arrays.asList(new Pair(238, "count"), new Pair(238, "T")));
      List<Pair<Integer, String>> clientCustomKeyValues = new ArrayList(Arrays.asList(new Pair(501470, "dailyF")));
      String[] ResetKeyValue = new String[]{"ArcQuest0", "ArcQuest1", "ArcQuest2", "ArcQuest3", "ArcQuest4", "ArcQuest5", "ArcQuest8", "AthQuest1", "DojoCount", "TyKitchenReward", "follower"};
      String var10002;
      if (acc) {
         this.client.removeKeyValue(key);
         MapleClient var10000 = this.client;
         var10002 = kc.getYears();
         var10000.setKeyValue(key, var10002 + kc.getMonths() + kc.getDays());
      } else {
         this.removeV(key);
         var10002 = kc.getYears();
         this.addKV(key, var10002 + kc.getMonths() + kc.getDays());
      }

      Iterator var14;
      Entry quest;
      String[] var20;
      Pair keyz;
      int var22;
      int var23;
      String keyValue;
      if (weekly) {
         if (acc) {
            var14 = this.getClient().getCustomKeyValue().entrySet().iterator();

            label238:
            while(true) {
               String bosslist;
               int i;
               while(var14.hasNext()) {
                  quest = (Entry)var14.next();
                  if ((Integer)quest.getKey() == 501367) {
                     bosslist = "";

                     for(i = 0; i < ServerConstants.NeoPosList.size(); ++i) {
                        bosslist = bosslist + "0";
                     }

                     this.client.setCustomKeyValue((Integer)quest.getKey(), "reward", bosslist);
                  } else if ((Integer)quest.getKey() >= 501470 && (Integer)quest.getKey() <= 501496) {
                     this.client.setCustomKeyValue((Integer)quest.getKey(), "state", "0");
                     this.client.setCustomKeyValue((Integer)quest.getKey(), "count", "0");
                  }
               }

               var14 = clientCustomDatasWeek.iterator();

               while(var14.hasNext()) {
                  keyz = (Pair)var14.next();
                  if (this.client.getCustomData((Integer)keyz.getLeft(), (String)keyz.getRight()) != null) {
                     this.client.setCustomData((Integer)keyz.getLeft(), (String)keyz.getRight(), "0");
                  }
               }

               var20 = clientDateWeekKeyValues;
               var22 = clientDateWeekKeyValues.length;

               for(var23 = 0; var23 < var22; ++var23) {
                  keyValue = var20[var23];
                  if (this.client.getKeyValue(keyValue) != null) {
                     if (!keyValue.equals("WishCoin")) {
                        this.client.setKeyValue(keyValue, "0");
                     } else {
                        String bosslist = "";

                        for(int i = 0; i < ServerConstants.NeoPosList.size(); ++i) {
                           bosslist = bosslist + "0";
                        }

                        this.client.setKeyValue("WishCoin", bosslist);
                     }
                  }
               }

               var14 = clientCustomKeyValuesWeek.iterator();

               while(true) {
                  while(true) {
                     do {
                        if (!var14.hasNext()) {
                           break label238;
                        }

                        keyz = (Pair)var14.next();
                     } while(this.client.getCustomKeyValueStr((Integer)keyz.getLeft(), (String)keyz.getRight()) == null);

                     if ((Integer)keyz.getLeft() == 501468 && ((String)keyz.getRight()).equals("reward")) {
                        bosslist = "";

                        for(i = 0; i < ServerConstants.NeoPosList.size(); ++i) {
                           bosslist = bosslist + "0";
                        }

                        this.client.setCustomKeyValue((Integer)keyz.getLeft(), (String)keyz.getRight(), bosslist);
                     } else {
                        this.client.setCustomKeyValue((Integer)keyz.getLeft(), (String)keyz.getRight(), "0");
                     }
                  }
               }
            }
         } else {
            var14 = DateWeekKeyValues.iterator();

            while(var14.hasNext()) {
               keyz = (Pair)var14.next();
               if (this.getKeyValue((Integer)keyz.getLeft(), (String)keyz.getRight()) >= 0L) {
                  this.setKeyValue((Integer)keyz.getLeft(), (String)keyz.getRight(), (Integer)keyz.getLeft() == 1068 ? "60" : "0");
               }
            }
         }
      } else if (acc) {
         var20 = clientDateKeyValues;
         var22 = clientDateKeyValues.length;

         for(var23 = 0; var23 < var22; ++var23) {
            keyValue = var20[var23];
            if (this.client.getKeyValue(keyValue) != null) {
               this.client.setKeyValue(keyValue, "0");
            }

            if (keyValue.equals("dailyGiftComplete")) {
               this.client.send(CWvsContext.updateDailyGift("count=0;date=" + GameConstants.getCurrentDate_NoTime()));
               this.client.send(CField.dailyGift(this.client.getPlayer(), 1, 0));
               if (kc.getDayt() == 1) {
                  this.client.setKeyValue("dailyGiftDay", "0");
               }
            }
         }

         var14 = clientCustomDatas.iterator();

         while(var14.hasNext()) {
            keyz = (Pair)var14.next();
            if (this.client.getCustomData((Integer)keyz.getLeft(), (String)keyz.getRight()) != null) {
               this.client.setCustomData((Integer)keyz.getLeft(), (String)keyz.getRight(), "0");
               if ((Integer)keyz.getLeft() == 238 && ((String)keyz.getRight()).equals("T")) {
                  this.client.setCustomData(238, "T", GameConstants.getCurrentFullDate());
               }
            }
         }

         var14 = this.client.getQuests().entrySet().iterator();

         label158:
         while(true) {
            do {
               if (!var14.hasNext()) {
                  var14 = clientCustomKeyValues.iterator();

                  while(var14.hasNext()) {
                     keyz = (Pair)var14.next();
                     if (this.client.getCustomKeyValueStr((Integer)keyz.getLeft(), (String)keyz.getRight()) != null) {
                        this.client.setCustomKeyValue((Integer)keyz.getLeft(), (String)keyz.getRight(), "0");
                     }
                  }
                  break label158;
               }

               quest = (Entry)var14.next();
            } while(((MapleQuest)quest.getKey()).getId() != 16011 && ((MapleQuest)quest.getKey()).getId() != 16012);

            ((MapleQuestStatus)quest.getValue()).setStatus((byte)0);
            ((MapleQuestStatus)quest.getValue()).setCustomData("");
            this.client.send(CWvsContext.InfoPacket.updateQuest((MapleQuestStatus)quest.getValue()));
         }
      } else {
         for(int i = 100829; i <= 100853; ++i) {
            this.removeKeyValue(i);
         }

         var14 = DateKeyValues.iterator();

         while(var14.hasNext()) {
            keyz = (Pair)var14.next();
            if (this.getKeyValue((Integer)keyz.getLeft(), (String)keyz.getRight()) > 0L) {
               this.setKeyValue((Integer)keyz.getLeft(), (String)keyz.getRight(), "0");
            }
         }

         var20 = ResetKeyValue;
         var22 = ResetKeyValue.length;

         for(var23 = 0; var23 < var22; ++var23) {
            keyValue = var20[var23];
            if (this.getV(keyValue) != null) {
               this.removeV(keyValue);
            }
         }
      }

      this.client.send(SLFCGPacket.StarDustUI("UI/UIWindowEvent.img/starDust_18th", this.getKeyValue(100794, "sum"), this.getKeyValue(100794, "point"), this.getKeyValue(100794, "lock") == 1L));
   }

   public List<Triple<String, String, String>> FuckingRanking(int type) {
      List<Triple<String, String, String>> data = new ArrayList();
      data.add(new Triple("", "", ""));
      String dbline = "";
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         if (type == 0) {
            dbline = "SELECT * FROM characters WHERE gm = 0 ORDER BY bounsfish DESC LIMIT 10";
            ps = con.prepareStatement(dbline);
            rs = ps.executeQuery();

            while(rs.next()) {
               data.add(new Triple(String.valueOf(rs.getInt("bounsfish")), rs.getString("name"), ""));
            }
         } else if (type == 1) {
            dbline = "SELECT * FROM characters WHERE gm = 0 ORDER BY bounsvdance DESC LIMIT 10";
            ps = con.prepareStatement(dbline);
            rs = ps.executeQuery();

            while(rs.next()) {
               data.add(new Triple(HelpTools.CalcComa(rs.getInt("bounsvdance")), rs.getString("name"), ""));
            }
         } else if (type == 2) {
            dbline = "SELECT * FROM characters WHERE gm = 0 ORDER BY meso DESC LIMIT 10";
            ps = con.prepareStatement(dbline);
            rs = ps.executeQuery();

            while(rs.next()) {
               data.add(new Triple(String.valueOf(rs.getInt("job")), rs.getString("name"), HelpTools.CalcComa(rs.getLong("meso"))));
            }
         } else if (type == 3) {
            dbline = "SELECT * FROM characters WHERE gm = 0 ORDER BY fame DESC LIMIT 10";
            ps = con.prepareStatement(dbline);
            rs = ps.executeQuery();

            while(rs.next()) {
               data.add(new Triple(String.valueOf(rs.getInt("job")), rs.getString("name"), HelpTools.CalcComa(rs.getInt("fame"))));
            }
         } else if (type == 4) {
            dbline = "SELECT * FROM characters WHERE gm = 0 ORDER BY level DESC LIMIT 10";
            ps = con.prepareStatement(dbline);
            rs = ps.executeQuery();

            while(rs.next()) {
               data.add(new Triple(String.valueOf(rs.getInt("job")), rs.getString("name"), String.valueOf(rs.getInt("level"))));
            }
         }

         rs.close();
         ps.close();
         con.close();
      } catch (Exception var16) {
         var16.printStackTrace();
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }

            if (ps != null) {
               ps.close();
            }

            if (con != null) {
               con.close();
            }
         } catch (Exception var15) {
            var15.printStackTrace();
         }

      }

      return data;
   }

   public Pair<Integer, Integer> getEquippedSpecialCore() {
      Iterator var1 = this.getCore().iterator();

      Core core;
      do {
         if (!var1.hasNext()) {
            return null;
         }

         core = (Core)var1.next();
      } while(core.getState() != 2 || core.getSpCoreOption() == null || core.getSpCoreOption().getSkillid() < 400007000 || core.getSpCoreOption().getSkillid() > 400007999);

      return new Pair(core.getCoreId(), core.getSpCoreOption().getSkillid());
   }

   public void resetSpecialCoreStat() {
      this.setSpLastValidTime(0L);
      this.setSpCount(0);
      this.setSpAttackCountMobId(0);
   }

   public void applySpecialCoreSkills(SpecialCoreOption spOption) {
      long time = System.currentTimeMillis();
      String effectType = spOption.getEffectType();
      byte var6 = -1;
      switch(effectType.hashCode()) {
      case -1759852260:
         if (effectType.equals("reduceCooltime")) {
            var6 = 2;
         }
         break;
      case 3198440:
         if (effectType.equals("heal")) {
            var6 = 1;
         }
         break;
      case 1192466847:
         if (effectType.equals("selfbuff")) {
            var6 = 0;
         }
      }

      switch(var6) {
      case 0:
         SecondaryStatEffect effect = SkillFactory.getSkill(spOption.getSkillid()).getEffect(spOption.getSkilllevel());
         if (effect != null) {
            effect.applyTo(this);
         }
         break;
      case 1:
         this.addHP(this.getStat().getCurrentMaxHp() * (long)spOption.getHeal_percent() / 100L);
         break;
      case 2:
         Iterator var7 = this.getCooldowns().iterator();

         label44:
         while(true) {
            MapleCoolDownValueHolder a;
            Skill skill;
            do {
               if (!var7.hasNext()) {
                  break label44;
               }

               a = (MapleCoolDownValueHolder)var7.next();
               skill = SkillFactory.getSkill(a.skillId);
            } while(skill != null && skill.isHyper() && a.skillId >= 400001000 && a.skillId <= 400059999);

            int reduc = (int)((System.currentTimeMillis() - a.startTime) * (long)spOption.getReducePercent() / 100L);
            this.changeCooldown(a.skillId, -reduc);
         }
      }

      this.addCooldown(spOption.getSkillid(), time, (long)spOption.getCooltime());
      this.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(this, "Effect/CharacterEff.img/VMatrixSP"));
   }

   public void checkSpecialCoreSkills(String condType, int mobObjectId, SecondaryStatEffect effect) {
      try {
         long time = System.currentTimeMillis();
         Pair<Integer, Integer> coredata = this.getEqpSpCore();
         if (coredata == null) {
            return;
         }

         int coreId = (Integer)coredata.left;
         int coreSkillId = (Integer)coredata.right;
         SpecialCoreOption spOption = ((Core)((Pair)MatrixHandler.getCores().get(coreId)).left).getSpCoreOption();
         if (spOption == null || !spOption.getCondType().equals(condType)) {
            return;
         }

         String condType2 = spOption.getCondType();
         byte var12 = -1;
         switch(condType2.hashCode()) {
         case -1800800788:
            if (condType2.equals("combokill")) {
               var12 = 6;
            }
            break;
         case -1589211055:
            if (condType2.equals("killCount")) {
               var12 = 2;
            }
            break;
         case -1346432708:
            if (condType2.equals("hitCount")) {
               var12 = 0;
            }
            break;
         case -691593305:
            if (condType2.equals("attackCount")) {
               var12 = 1;
            }
            break;
         case -545639018:
            if (condType2.equals("cooltime")) {
               var12 = 5;
            }
            break;
         case -297952807:
            if (condType2.equals("attackCountMob")) {
               var12 = 3;
            }
            break;
         case 99456:
            if (condType2.equals("die")) {
               var12 = 7;
            }
            break;
         case 3449685:
            if (condType2.equals("prob")) {
               var12 = 8;
            }
            break;
         case 3512122:
            if (condType2.equals("rune")) {
               var12 = 4;
            }
         }

         switch(var12) {
         case 0:
         case 1:
         case 2:
            if (spOption.getValidTime() > 0 && time > this.getSpLastValidTime() + (long)spOption.getValidTime()) {
               this.setSpCount(0);
            }

            if (!this.skillisCooling(coreSkillId)) {
               this.gainSpCount(1);
               if (this.getSpCount() >= spOption.getCount()) {
                  this.applySpecialCoreSkills(spOption);
                  this.gainSpCount(-spOption.getCount());
               }
            }

            this.setSpLastValidTime(time);
            break;
         case 3:
            if (spOption.getValidTime() > 0 && time > this.getSpLastValidTime() + (long)spOption.getValidTime()) {
               this.setSpCount(0);
               this.setSpAttackCountMobId(0);
            }

            if (mobObjectId != this.getSpAttackCountMobId()) {
               this.setSpAttackCountMobId(mobObjectId);
               this.setSpCount(0);
            } else if (!this.skillisCooling(coreSkillId)) {
               this.gainSpCount(1);
               if (this.getSpCount() >= spOption.getCount()) {
                  this.applySpecialCoreSkills(spOption);
                  this.gainSpCount(-spOption.getCount());
               }
            }

            this.setSpLastValidTime(time);
            break;
         case 4:
         case 5:
            if (!this.skillisCooling(coreSkillId) && effect != null) {
               this.applySpecialCoreSkills(spOption);
            }
            break;
         case 6:
            if (this.getMonsterCombo() > 0 && this.getMonsterCombo() % spOption.getCount() == 0 && !this.skillisCooling(coreSkillId)) {
               this.applySpecialCoreSkills(spOption);
            }

            this.setSpLastValidTime(time);
            break;
         case 7:
            if (!this.skillisCooling(coreSkillId)) {
               this.applySpecialCoreSkills(spOption);
            }
            break;
         case 8:
            if (spOption.getValidTime() > 0 && time > this.getSpLastValidTime() + (long)spOption.getValidTime()) {
               this.setSpLastValidTime(time);
            }

            if (!this.skillisCooling(coreSkillId)) {
               int succ = (int)(1.0D / spOption.getProb());
               if (Randomizer.isSuccess(succ, 1000000)) {
                  this.applySpecialCoreSkills(spOption);
               }
            }
         }
      } catch (Exception var14) {
         var14.printStackTrace();
      }

   }

   public int getSpAttackCountMobId() {
      return this.spAttackCountMobId;
   }

   public void setSpAttackCountMobId(int spAttackCountMobId) {
      this.spAttackCountMobId = spAttackCountMobId;
   }

   public int getSpCount() {
      return this.spCount;
   }

   public void setSpCount(int spCount) {
      this.spCount = spCount;
   }

   public void gainSpCount(int spCount) {
      this.spCount += spCount;
   }

   public long getSpLastValidTime() {
      return this.spLastValidTime;
   }

   public void setSpLastValidTime(long spLastValidTime) {
      this.spLastValidTime = spLastValidTime;
   }

   public Pair<Integer, Integer> getEqpSpCore() {
      return this.eqpSpCore;
   }

   public void setEqpSpCore(Pair<Integer, Integer> eqpSpCore) {
      if (this.eqpSpCore != eqpSpCore) {
         this.resetSpecialCoreStat();
         this.eqpSpCore = eqpSpCore;
      }

   }

   public List<MapleSavedEmoticon> getSavedEmoticon() {
      return this.savedEmoticon;
   }

   public void setSavedEmoticon(List<MapleSavedEmoticon> savedEmoticon) {
      this.savedEmoticon = savedEmoticon;
   }

   public List<MapleChatEmoticon> getEmoticonTabs() {
      return this.emoticonTabs;
   }

   public void setEmoticonTabs(List<MapleChatEmoticon> emoticons) {
      this.emoticonTabs = emoticons;
   }

   public List<Triple<Long, Integer, Short>> getEmoticons() {
      return this.emoticons;
   }

   public void setEmoticons(List<Triple<Long, Integer, Short>> emoticons) {
      this.emoticons = emoticons;
   }

   public void gainEmoticon(int tab) {
      if (!this.hasEmoticon(tab)) {
         short slot = (short)(this.getEmoticonTabs().size() + 1);
         MapleChatEmoticon em = new MapleChatEmoticon(this.getId(), tab, PacketHelper.getTime(-2L), (String)null);
         this.getEmoticonTabs().add(em);
         this.client.send(CField.getChatEmoticon((byte)0, slot, (short)0, tab, ""));
         this.getEmoticons().clear();
         ChatEmoticon.LoadChatEmoticons(this, this.getEmoticonTabs());
      }

   }

   public boolean hasEmoticon(int tab) {
      Iterator var2 = this.getEmoticonTabs().iterator();

      MapleChatEmoticon em;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         em = (MapleChatEmoticon)var2.next();
      } while(em.getEmoticonid() != tab);

      return true;
   }

   public List<Pair<Integer, Short>> getEmoticonBookMarks() {
      return this.emoticonBookMarks;
   }

   public void setEmoticonBookMarks(List<Pair<Integer, Short>> emoticonBookMarks) {
      this.emoticonBookMarks = emoticonBookMarks;
   }

   public short getEmoticonFreeSlot() {
      List<Short> slots = new ArrayList();
      Iterator var2 = this.getEmoticons().iterator();

      while(var2.hasNext()) {
         Triple<Long, Integer, Short> a = (Triple)var2.next();
         slots.add((Short)a.right);
      }

      for(short i = 1; i <= this.getEmoticons().size(); ++i) {
         if (!slots.contains(i)) {
            return i;
         }
      }

      return 1;
   }

   public void gainSuddenMission(int startquestid, int midquestid, boolean first) {
      long nowtime = first ? PacketHelper.getKoreanTimestamp(System.currentTimeMillis()) : this.getKeyValue(51351, "starttime");
      long endtime = first ? nowtime + 17980000000L : this.getKeyValue(51351, "endtime");
      if (first) {
         this.removeKeyValue(51351);
         this.setKeyValue(51351, "starttime", nowtime.makeConcatWithConstants<invokedynamic>(nowtime));
         this.setKeyValue(51351, "endtime", endtime.makeConcatWithConstants<invokedynamic>(endtime));
         this.setKeyValue(51351, "startquestid", startquestid.makeConcatWithConstants<invokedynamic>(startquestid));
         this.setKeyValue(51351, "midquestid", midquestid.makeConcatWithConstants<invokedynamic>(midquestid));
         this.setKeyValue(51351, "queststat", "2");
         int[] array = new int[]{49001, 49002, 49003, 49012, 49013, 49014, 49016};
         int[] var10 = array;
         int var11 = array.length;

         for(int var12 = 0; var12 < var11; ++var12) {
            Integer qid = var10[var12];
            MapleQuest.getInstance(qid).forfeit(this);
         }

         switch(startquestid) {
         case 49001:
         case 49002:
         case 49003:
         case 49012:
         case 49013:
         case 49014:
         case 49016:
            MapleQuest.getInstance(startquestid).forceStart(this, 0, "0");
         case 49004:
         case 49005:
         case 49006:
         case 49007:
         case 49008:
         case 49009:
         case 49010:
         case 49011:
         case 49015:
         }
      }

      this.getClient().send(CWvsContext.updateSuddenQuest(startquestid, true, endtime, ""));
      this.getClient().send(CWvsContext.updateSuddenQuest(midquestid, false, nowtime, "count=0;Quest=" + startquestid + ";state=1"));
      this.getClient().send(CWvsContext.updateSuddenQuest(midquestid, false, nowtime, "count=0;Quest=" + startquestid + ";state=2"));
   }

   public void CombokillHandler(MapleMonster monster, int type, int multikill) {
      if (monster != null) {
         if (multikill >= 3) {
            long comboexp = monster.getStats().getExp() * (long)multikill;
            float n22 = 0.0F;
            switch(multikill) {
            case 3:
               n22 = 0.03F;
               break;
            case 4:
               n22 = 0.08F;
               break;
            case 5:
               n22 = 0.15F;
               break;
            case 6:
               n22 = 0.198F;
               break;
            case 7:
               n22 = 0.252F;
               break;
            case 8:
               n22 = 0.312F;
               break;
            case 9:
               n22 = 0.378F;
               break;
            default:
               n22 = 0.45F;
            }

            if (this.getKeyValue(51351, "startquestid") == 49007L || this.getKeyValue(51351, "startquestid") == 49008L || this.getKeyValue(51351, "startquestid") == 49009L && this.getKeyValue(51351, "queststat") == 2L) {
               if (this.getKeyValue(51351, "multikill") < 0L) {
                  this.setKeyValue(51351, "multikill", "0");
               }

               long var10003 = this.getKeyValue(51351, "multikill");
               this.setKeyValue(51351, "multikill", (var10003 + 1L).makeConcatWithConstants<invokedynamic>(var10003 + 1L));
               int completecombo = this.getKeyValue(51351, "startquestid") == 49007L ? 15 : (this.getKeyValue(51351, "startquestid") == 49008L ? 25 : (this.getKeyValue(51351, "startquestid") == 49009L ? 50 : 0));
               if (this.getKeyValue(51351, "multikill") >= (long)completecombo) {
                  this.setKeyValue(51351, "queststat", "3");
                  this.getClient().send(CWvsContext.updateSuddenQuest((int)this.getKeyValue(51351, "midquestid"), false, PacketHelper.getKoreanTimestamp(System.currentTimeMillis()) + 600000000L, "count=1;Quest=" + this.getKeyValue(51351, "startquestid") + ";state=3;"));
               } else {
                  this.getClient().send(CWvsContext.updateSuddenQuest((int)this.getKeyValue(51351, "startquestid"), false, this.getKeyValue(51351, "endtime"), "MultiKC=" + this.getKeyValue(51351, "multikill") + ";"));
               }
            }

            this.getClient().getSession().writeAndFlush(CWvsContext.InfoPacket.multiKill(multikill > 10 ? 10 : multikill, comboexp));
            this.gainExp((long)((float)comboexp * n22), false, false, false);
         }

         if (System.currentTimeMillis() - this.getMonsterComboTime() > 8000L) {
            this.setMonsterCombo((short)0);
         } else if (!GameConstants.isContentsMap(this.getMapId())) {
            if (this.getMonsterCombo() < '썐') {
               this.addMonsterCombo((short)1);
            }

            if (this.getV("d_combo") == null) {
               this.addKV("d_combo", this.getMonsterCombo().makeConcatWithConstants<invokedynamic>(this.getMonsterCombo()));
            } else if ((long)this.getMonsterCombo() > Long.parseLong(this.getV("d_combo"))) {
               this.addKV("d_combo", this.getMonsterCombo().makeConcatWithConstants<invokedynamic>(this.getMonsterCombo()));
            }

            int itemId;
            if (this.getKeyValue(51351, "startquestid") == 49004L || this.getKeyValue(51351, "startquestid") == 49005L || this.getKeyValue(51351, "startquestid") == 49006L && this.getKeyValue(51351, "queststat") == 2L) {
               itemId = this.getKeyValue(51351, "startquestid") == 49006L ? 300 : (this.getKeyValue(51351, "startquestid") == 49005L ? 200 : (this.getKeyValue(51351, "startquestid") == 49004L ? 100 : 0));
               if (this.monsterCombo >= itemId) {
                  this.setKeyValue(51351, "queststat", "3");
                  this.getClient().send(CWvsContext.updateSuddenQuest((int)this.getKeyValue(51351, "midquestid"), false, PacketHelper.getKoreanTimestamp(System.currentTimeMillis()) + 600000000L, "count=1;Quest=" + this.getKeyValue(51351, "startquestid") + ";state=3;"));
               } else {
                  this.getClient().send(CWvsContext.updateSuddenQuest((int)this.getKeyValue(51351, "startquestid"), false, this.getKeyValue(51351, "endtime"), "ComboK=" + (this.monsterCombo + 1) + ";"));
               }
            }

            this.checkSpecialCoreSkills("combokill", monster.getObjectId(), (SecondaryStatEffect)null);
            this.getClient().getSession().writeAndFlush(CWvsContext.InfoPacket.comboKill(this.getMonsterCombo(), monster.getObjectId()));
            this.setMonsterComboTime(System.currentTimeMillis());
            if (this.getMonsterCombo() % 50 == 0) {
               if (this.getMonsterCombo() < 350) {
                  itemId = 2023484;
               } else if (this.getMonsterCombo() < 750) {
                  itemId = 2023494;
               } else if (this.getMonsterCombo() < 2000) {
                  itemId = 2023495;
               } else {
                  itemId = 2023669;
               }

               this.getMap().spawnMobPublicDrop(new Item(itemId, (short)0, (short)1, 0), monster.getTruePosition(), monster, this, (byte)0, 0);
            }
         }

      }
   }

   public void ZeroSkillCooldown(int skillid) {
      int nocool = 0;
      switch(skillid) {
      case 101100101:
         nocool = 101101100;
         break;
      case 101100201:
      case 101101200:
         nocool = 101101200;
         break;
      case 101110102:
      case 101110104:
         nocool = 101111100;
         break;
      case 101110200:
      case 101110203:
         nocool = 101110200;
         break;
      case 101120100:
      case 101120101:
      case 101120104:
      case 101120105:
         nocool = 101121100;
         break;
      case 101120201:
      case 101120204:
         nocool = 101121200;
      }

      int[] array = new int[]{101121200, 101110200, 101101200, 101101100, 101121100, 101111100};
      int[] var5 = array;
      int var6 = array.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         int cool = var5[var7];
         if (this.skillisCooling(cool) && cool != nocool) {
            this.changeCooldown(cool, -4000);
         }
      }

   }

   public void FeverTime(boolean start, boolean delay) {
      if (start && delay) {
         this.getClient().getSession().writeAndFlush(CField.FeverMessage((byte)1));
      } else if (start && !delay) {
         this.getClient().getSession().writeAndFlush(CField.FeverMessage((byte)2));
      } else if (!start && delay) {
         this.getClient().getSession().writeAndFlush(CField.FeverMessage((byte)3));
      } else if (!start && !delay) {
         this.getClient().getSession().writeAndFlush(CField.FeverMessage((byte)4));
      }

   }

   public Map<MapleQuest, MapleQuestStatus> getQuests() {
      return this.quests;
   }

   public void setQuests(Map<MapleQuest, MapleQuestStatus> quests) {
      this.quests = quests;
   }

   public MapleTyoonKitchen getMtk() {
      return this.Mtk;
   }

   public void setMtk(MapleTyoonKitchen Mtk) {
      this.Mtk = Mtk;
   }

   public List<SecondAtom> getSaList() {
      return this.SaList;
   }

   public void setSaList(List<SecondAtom> SaList) {
      this.SaList = SaList;
   }

   public void spawnSecondAtom(List<SecondAtom> list) {
      Iterator var2 = list.iterator();

      while(var2.hasNext()) {
         SecondAtom a = (SecondAtom)var2.next();
         if (!this.SaList.contains(a)) {
            this.setSkillCustomInfo(9877654, this.getSkillCustomValue0(9877654) + 1L, 0L);
            a.setObjectId((int)this.getSkillCustomValue0(9877654));
            this.SaList.add(a);
         }
      }

      this.client.send(CField.spawnSecondAtoms(this.getId(), list, 0));
   }

   public void removeSecondAtom(int objid) {
      Iterator var2 = this.SaList.iterator();

      while(var2.hasNext()) {
         SecondAtom sa = (SecondAtom)var2.next();
         if (sa != null && objid == sa.getObjectId()) {
            this.SaList.remove(sa);
            break;
         }
      }

      this.client.send(CField.removeSecondAtom(this.getId(), objid));
   }

   public SecondAtom getSecondAtom(int objid) {
      Iterator var2 = this.SaList.iterator();

      SecondAtom sa;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         sa = (SecondAtom)var2.next();
      } while(objid != sa.getObjectId());

      return sa;
   }

   public void dropShowInfo(String msg) {
      this.getClient().send(CWvsContext.getTopMsg(msg));
   }

   public final boolean getMonster(int mobid) {
      Iterator var2 = this.getMap().getAllMonster().iterator();

      MapleMonster mob;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         MapleMapObject obj = (MapleMapObject)var2.next();
         mob = (MapleMonster)obj;
      } while(mob.getId() != mobid);

      return true;
   }

   public final void spawnMob(int id, int x, int y) {
      this.spawnMob(id, 1, new Point(x, y));
   }

   private final void spawnMob(int id, int qty, Point pos) {
      for(int i = 0; i < qty; ++i) {
         this.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), pos);
      }

   }

   public void gainItemAllStat(int itemid, short quantity, short allstat, short wmtk) {
      Equip equip = new Equip(itemid, quantity, 0);
      equip.setStr(allstat);
      equip.setDex(allstat);
      equip.setInt(allstat);
      equip.setLuk(allstat);
      if (wmtk != -1) {
         equip.setWatk(wmtk);
         equip.setMatk(wmtk);
      }

      MapleInventoryManipulator.addFromDrop(this.client, equip, true);
   }

   public Map<Byte, Integer> getTotems() {
      Map<Byte, Integer> eq = new HashMap();
      Iterator var2 = this.inventory[MapleInventoryType.EQUIPPED.ordinal()].newList().iterator();

      while(var2.hasNext()) {
         Item item = (Item)var2.next();
         eq.put((byte)(item.getPosition() + 5000), item.getItemId());
      }

      return eq;
   }

   public void SoulLevelLoad(int accId, int chrId) throws SQLException {
      try {
         Connection con = DatabaseConnection.getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE `level` > 0 AND `accountid` = " + accId + " AND `id` != " + chrId + " ORDER BY `level` DESC LIMIT 1");
         ResultSet rs = ps.executeQuery();
         int wskill = false;

         int wlevel;
         for(wlevel = 0; rs.next(); wlevel = rs.getInt("level")) {
         }

         int wskill;
         if (wlevel < 10) {
            wskill = 0;
         } else if (wlevel > 200) {
            wskill = 20;
         } else {
            wskill = wlevel / 10;
         }

         int skillid = false;
         if (GameConstants.blessSkillJob(this.getJob()) != -1) {
            int skillid = GameConstants.blessSkillJob(this.getJob()) + 12;
            this.changeSingleSkillLevel(SkillFactory.getSkill(skillid), wskill, (byte)wskill);
         }

         rs.close();
         ps.close();
      } catch (SQLException var9) {
         var9.printStackTrace();
      }

   }

   public void EmpressLevelLoad(int accId, int chrId) throws SQLException {
      try {
         Connection con = DatabaseConnection.getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE `level` > 0 AND `accountid` = " + accId + " AND `id` != " + chrId + " AND `job` >= 1000 AND `job` <= 1512 ORDER BY `level` DESC LIMIT 1");
         ResultSet rs = ps.executeQuery();
         int wskill = false;

         int wlevel;
         for(wlevel = 0; rs.next(); wlevel = rs.getInt("level")) {
         }

         int wskill;
         if (wlevel < 5) {
            wskill = 0;
         } else if (wlevel > 150) {
            wskill = 30;
         } else {
            wskill = wlevel / 5;
         }

         PreparedStatement pss = con.prepareStatement("SELECT * FROM characters WHERE `level` > 0 AND `accountid` = " + accId + " AND `id` != " + chrId + " AND `job` >= 5000 AND `job` <= 5112 ORDER BY `level` DESC LIMIT 1");
         ResultSet rss = pss.executeQuery();
         int wskills = false;

         int wlevels;
         for(wlevels = 0; rss.next(); wlevels = rss.getInt("level")) {
         }

         int wskills;
         if (wlevels < 5) {
            wskills = 0;
         } else if (wlevels > 150) {
            wskills = 30;
         } else {
            wskills = wlevels / 5;
         }

         if (wskill < wskills) {
            wskill = wskills;
         }

         int skillid = false;
         if (GameConstants.blessSkillJob(this.getJob()) != -1) {
            int skillid = GameConstants.blessSkillJob(this.getJob()) + 73;
            this.changeSingleSkillLevel(SkillFactory.getSkill(skillid), wskill, (byte)wskill);
         }

         rs.close();
         ps.close();
      } catch (SQLException var13) {
         var13.printStackTrace();
      }

   }

   public boolean hasDonationSkill(int skillid) {
      if (this.getKeyValue(201910, "DonationSkill") < 0L) {
         this.setKeyValue(201910, "DonationSkill", "0");
      }

      MapleDonationSkill dskill = MapleDonationSkill.getBySkillId(skillid);
      if (dskill == null) {
         return false;
      } else {
         return (this.getKeyValue(201910, "DonationSkill") & (long)dskill.getValue()) != 0L;
      }
   }

   public void gainDonationSkills() {
      if (this.getKeyValue(201910, "DonationSkill") > 0L) {
         MapleDonationSkill[] var1 = MapleDonationSkill.values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            MapleDonationSkill stat = var1[var3];
            if ((this.getKeyValue(201910, "DonationSkill") & (long)stat.getValue()) != 0L) {
               this.getStat().setMp(this.getStat().getCurrentMaxMp(this), this);
               if (!this.getBuffedValue(stat.getSkillId())) {
                  SkillFactory.getSkill(stat.getSkillId()).getEffect(SkillFactory.getSkill(stat.getSkillId()).getMaxLevel()).applyTo(this, 0);
               }
            }
         }
      }

   }

   public void gainDonationSkill(int skillid) {
      if (this.getKeyValue(201910, "DonationSkill") < 0L) {
         this.setKeyValue(201910, "DonationSkill", "0");
      }

      MapleDonationSkill dskill = MapleDonationSkill.getBySkillId(skillid);
      if (dskill != null && (this.getKeyValue(201910, "DonationSkill") & (long)dskill.getValue()) == 0L) {
         int data = (int)this.getKeyValue(201910, "DonationSkill");
         data |= dskill.getValue();
         this.setKeyValue(201910, "DonationSkill", data.makeConcatWithConstants<invokedynamic>(data));
         SkillFactory.getSkill(skillid).getEffect(SkillFactory.getSkill(skillid).getMaxLevel()).applyTo(this, 0);
      }

   }

   public void teachSkill(int id, int level, byte masterlevel) {
      this.changeSingleSkillLevel(SkillFactory.getSkill(id), level, masterlevel);
   }

   public void teachSkill(int id, int level) {
      Skill skil = SkillFactory.getSkill(id);
      if (this.getSkillLevel(skil) > level) {
         level = this.getSkillLevel(skil);
      }

      this.changeSingleSkillLevel(skil, level, (byte)skil.getMaxLevel());
   }

   public void maxskill(int i) {
      if (GameConstants.isHoyeong(i) && this.getSkillLevel(160000076) < 10) {
         this.changeSkillLevel(SkillFactory.getSkill(160000076), (byte)10, (byte)10);
      }

      MapleDataProvider var10000 = MapleDataProviderFactory.getDataProvider(MapleDataProviderFactory.fileInWZPath("Skill.wz"));
      String var10001 = i.makeConcatWithConstants<invokedynamic>(i);
      MapleData data = var10000.getData(StringUtil.getLeftPaddedStr(var10001, '0', 3) + ".img");
      byte maxLevel = false;
      Iterator var4 = data.iterator();

      while(true) {
         MapleData skill;
         do {
            if (!var4.hasNext()) {
               return;
            }

            skill = (MapleData)var4.next();
         } while(skill == null);

         Iterator var6 = skill.getChildren().iterator();

         while(var6.hasNext()) {
            MapleData skillId = (MapleData)var6.next();
            if (!skillId.getName().equals("icon")) {
               byte maxLevel = (byte)MapleDataTool.getIntConvert("maxLevel", skillId.getChildByPath("common"), 0);
               if (maxLevel > 30) {
                  maxLevel = 30;
               }

               if (MapleDataTool.getIntConvert("invisible", skillId, 0) == 0 && this.getLevel() >= MapleDataTool.getIntConvert("reqLev", skillId, 0)) {
                  try {
                     this.changeSkillLevel(SkillFactory.getSkill(Integer.parseInt(skillId.getName())), maxLevel, maxLevel);
                  } catch (NumberFormatException var9) {
                  }
               }
            }
         }
      }
   }

   public void skillMaster() {
      MapleDataProvider var10000 = MapleDataProviderFactory.getDataProvider(MapleDataProviderFactory.fileInWZPath("Skill.wz"));
      String var10001 = this.getJob().makeConcatWithConstants<invokedynamic>(this.getJob());
      MapleData data = var10000.getData(StringUtil.getLeftPaddedStr(var10001, '0', 3) + ".img");
      this.dropMessage(5, "스킬마스터가 완료되었습니다.");
      if (this.getLevel() < 10) {
         this.dropMessage(1, "레벨 10 이상 부터 사용 할 수 있습니다.");
      } else {
         for(int i = 0; i < this.getJob() % 10 + 1; ++i) {
            this.maxskill(i + 1 == this.getJob() % 10 + 1 ? this.getJob() - this.getJob() % 100 : this.getJob() - (i + 1));
         }

         this.maxskill(this.getJob());
         if (GameConstants.isDemonAvenger(this.getJob())) {
            this.maxskill(3101);
         }

         if (GameConstants.isZero(this.getJob())) {
            int[] jobs = new int[]{10000, 10100, 10110, 10111, 10112};
            int[] var3 = jobs;
            int var4 = jobs.length;

            label74:
            for(int var5 = 0; var5 < var4; ++var5) {
               int job = var3[var5];
               data = MapleDataProviderFactory.getDataProvider(MapleDataProviderFactory.fileInWZPath("Skill.wz")).getData(job + ".img");
               Iterator var7 = data.iterator();

               while(true) {
                  MapleData skill;
                  do {
                     if (!var7.hasNext()) {
                        if (this.getLevel() >= 200) {
                           this.changeSingleSkillLevel(SkillFactory.getSkill(100001005), 1, (byte)1);
                        }
                        continue label74;
                     }

                     skill = (MapleData)var7.next();
                  } while(skill == null);

                  Iterator var9 = skill.getChildren().iterator();

                  while(var9.hasNext()) {
                     MapleData skillId = (MapleData)var9.next();
                     if (!skillId.getName().equals("icon")) {
                        byte maxLevel = (byte)MapleDataTool.getIntConvert("maxLevel", skillId.getChildByPath("common"), 0);
                        if (maxLevel < 0) {
                           maxLevel = 1;
                        }

                        if (maxLevel > 30) {
                           maxLevel = 30;
                        }

                        if (MapleDataTool.getIntConvert("invisible", skillId, 0) == 0 && this.getLevel() >= MapleDataTool.getIntConvert("reqLev", skillId, 0)) {
                           this.changeSingleSkillLevel(SkillFactory.getSkill(Integer.parseInt(skillId.getName())), maxLevel, maxLevel);
                        }
                     }
                  }
               }
            }
         }

         if (GameConstants.isKOC(this.getJob()) && this.getLevel() >= 100) {
            this.changeSkillLevel(11121000, (byte)30, (byte)30);
            this.changeSkillLevel(12121000, (byte)30, (byte)30);
            this.changeSkillLevel(13121000, (byte)30, (byte)30);
            this.changeSkillLevel(14121000, (byte)30, (byte)30);
            this.changeSkillLevel(15121000, (byte)30, (byte)30);
         }

      }
   }

   public void changeSkillLevel_Inner(int skill, byte newLevel, byte newMasterLevel) {
      this.changeSkillLevel_Inner(SkillFactory.getSkill(skill), newLevel, newMasterLevel);
   }

   public void changeSkillLevel_Inner(Skill skil, int skilLevel, byte masterLevel) {
      Map<Skill, SkillEntry> enry = new HashMap(1);
      enry.put(skil, new SkillEntry(skilLevel, masterLevel, -1L));
      this.changeSkillLevel_Skip(enry, false);
   }

   public List<MapleHyperStats> loadHyperStats(int pos) {
      LinkedList mhp = new LinkedList();

      try {
         Connection con = DatabaseConnection.getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT * FROM hyperstats WHERE charid = ? AND position = ?");
         ps.setInt(1, this.getId());
         ps.setInt(2, pos);
         ResultSet rs = ps.executeQuery();

         while(rs.next()) {
            mhp.add(new MapleHyperStats(pos, rs.getInt("skillid"), rs.getInt("skilllevel")));
         }

         rs.close();
         ps.close();
         con.close();
         return mhp;
      } catch (SQLException var6) {
         var6.printStackTrace();
         return null;
      }
   }

   public MapleHyperStats addHyperStats(int position, int skillid, int skilllevel) {
      try {
         Connection con = DatabaseConnection.getConnection();
         PreparedStatement pse = con.prepareStatement("INSERT INTO hyperstats (charid, position, skillid, skilllevel) VALUES (?, ?, ?, ?)");
         pse.setInt(1, this.getId());
         pse.setInt(2, position);
         pse.setInt(3, skillid);
         pse.setInt(4, skilllevel);
         pse.executeUpdate();
         pse.close();
         con.close();
      } catch (SQLException var6) {
         var6.printStackTrace();
         return null;
      }

      MapleHyperStats mhs = new MapleHyperStats(position, skillid, skilllevel);
      mhs.setPosition(position);
      mhs.setSkillid(skillid);
      mhs.setSkillLevel(skilllevel);
      return mhs;
   }

   public MapleHyperStats UpdateHyperStats(int position, int skillid, int skilllevel) {
      try {
         Connection con = DatabaseConnection.getConnection();
         PreparedStatement ps = null;
         ps = con.prepareStatement("UPDATE hyperstats SET skilllevel = ? WHERE charid = ? AND position = ? AND skillid = ?");
         ps.setInt(1, skilllevel);
         ps.setInt(2, this.getId());
         ps.setInt(3, position);
         ps.setInt(4, skillid);
         ps.executeUpdate();
         ps.close();
         con.close();
      } catch (SQLException var6) {
         var6.printStackTrace();
         return null;
      }

      MapleHyperStats mhs = new MapleHyperStats(position, skillid, skilllevel);
      mhs.setPosition(position);
      mhs.setSkillid(skillid);
      mhs.setSkillLevel(skilllevel);
      return mhs;
   }

   public void sethottimeboss(boolean check) {
      this.hottimeboss = check;
   }

   public boolean gethottimeboss() {
      return this.hottimeboss;
   }

   public boolean gethottimebosslastattack() {
      return this.hottimebosslastattack;
   }

   public void sethottimebossattackcheck(boolean check) {
      this.hottimebossattackcheck = check;
   }

   public boolean gethottimebossattackcheck() {
      return this.hottimebossattackcheck;
   }

   public void startMapTimeLimitTask(int time, final MapleMap to) {
      if (time <= 0) {
         time = 1;
      }

      this.client.getSession().write(CField.getClock(time));
      final MapleMap ourMap = this.getMap();
      time *= 1000;
      this.mapTimeLimitTask = server.Timer.MapTimer.getInstance().register(new Runnable() {
         public void run() {
            if (ourMap.getId() == 180000002) {
               MapleCharacter.this.getQuestNAdd(MapleQuest.getInstance(123455)).setCustomData(String.valueOf(System.currentTimeMillis()));
               MapleCharacter.this.getQuestNAdd(MapleQuest.getInstance(123456)).setCustomData("0");
            }

            MapleCharacter.this.changeMap(to, to.getPortal(0));
         }
      }, (long)time, (long)time);
   }

   public void 혁신의룰렛() {
      server.Timer.EtcTimer tm = server.Timer.EtcTimer.getInstance();
      final int f = Randomizer.nextInt(4);
      int s = Randomizer.rand(1, 2);
      final int t = Randomizer.nextInt(5);
      this.client.getSession().writeAndFlush(CField.showEffect("miro/frame"));
      this.client.getSession().writeAndFlush(CField.showEffect("miro/RR1/" + f));
      this.client.getSession().writeAndFlush(CField.showEffect("miro/RR2/" + s));
      this.client.getSession().writeAndFlush(CField.showEffect("miro/RR3/" + t));
      int[] ring = new int[]{1112585, 1112586, 1112663, 1112318, 1112319, 1112320};
      int[] pendent = new int[]{1123007, 1123008, 1123009, 1123010, 1123011, 1123012};
      final int itemid = s == 1 ? pendent[t] : ring[t];
      tm.schedule(new Runnable() {
         public void run() {
            MapleCharacter.this.아이템지급(f, t, itemid);
            MapleCharacter.this.client.getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(9000134, (byte)0, "#fn나눔고딕 Extrabold#아래에서 당신의 내면의 결과를 확인해보세요.\r\n\r\n#b혁신의 룰렛#k 에서 [#i" + itemid + "# #d#t" + itemid + "##k](이)가 소환 되었습니다.", "00 00", (byte)0));
         }
      }, 6000L);
   }

   public void 아이템지급(int f, int t, int itemid) {
      Equip 장비 = (Equip)MapleItemInformationProvider.getInstance().getEquipById(itemid);
      short str = 0;
      short dex = 0;
      short int_ = 0;
      short luk = 0;
      short unlimited = 0;
      boolean var10;
      switch(t) {
      case 0:
         switch(f) {
         case 0:
            str = 5;
            break;
         case 1:
            int_ = 5;
            break;
         case 2:
            dex = 5;
            break;
         case 3:
            luk = 5;
            break;
         case 4:
            if (this.getJob() < 510 && this.getJob() > 512) {
               if (this.getJob() < 520 && this.getJob() > 522) {
                  str = 5;
               } else {
                  dex = 5;
               }
            } else {
               str = 5;
            }
         }

         var10 = true;
      case 1:
         switch(f) {
         case 0:
            str = 10;
            break;
         case 1:
            dex = 10;
            break;
         case 2:
            int_ = 10;
            break;
         case 3:
            luk = 10;
         }

         unlimited = 5;
         break;
      case 2:
         switch(f) {
         case 0:
            str = 15;
            break;
         case 1:
            int_ = 15;
            break;
         case 2:
            dex = 15;
            break;
         case 3:
            luk = 15;
            break;
         case 4:
            if (this.getJob() < 510 && this.getJob() > 512) {
               if (this.getJob() < 520 && this.getJob() > 522) {
                  str = 15;
               } else {
                  dex = 15;
               }
            } else {
               str = 15;
            }
         }

         unlimited = 7;
         break;
      case 3:
         switch(f) {
         case 0:
            str = 20;
            break;
         case 1:
            int_ = 20;
            break;
         case 2:
            dex = 20;
            break;
         case 3:
            luk = 20;
            break;
         case 4:
            if (this.getJob() < 510 && this.getJob() > 512) {
               if (this.getJob() < 520 && this.getJob() > 522) {
                  str = 20;
               } else {
                  dex = 20;
               }
            } else {
               str = 20;
            }
         }

         var10 = true;
      case 4:
         switch(f) {
         case 0:
            str = 25;
            break;
         case 1:
            int_ = 25;
            break;
         case 2:
            dex = 25;
            break;
         case 3:
            luk = 25;
            break;
         case 4:
            if (this.getJob() < 510 && this.getJob() > 512) {
               if (this.getJob() < 520 && this.getJob() > 522) {
                  str = 25;
               } else {
                  dex = 25;
               }
            } else {
               str = 25;
            }
         }

         unlimited = 11;
         break;
      case 5:
         switch(f) {
         case 0:
            str = 30;
            break;
         case 1:
            int_ = 30;
            break;
         case 2:
            dex = 30;
            break;
         case 3:
            luk = 30;
            break;
         case 4:
            if (this.getJob() < 510 && this.getJob() > 512) {
               if (this.getJob() < 520 && this.getJob() > 522) {
                  str = 30;
               } else {
                  dex = 30;
               }
            } else {
               str = 30;
            }
         }

         unlimited = 15;
      }

      장비.setStr(str);
      장비.setDex(dex);
      장비.setInt(int_);
      장비.setLuk(luk);
      장비.setBossDamage((short)((byte)unlimited));
      장비.setTotalDamage((byte)unlimited);
      장비.setAllStat((byte)unlimited);
      MapleInventoryManipulator.addbyItem(this.client, 장비, false);
   }

   public void resetHyperStats(int position, int skillid) {
      try {
         Connection con = DatabaseConnection.getConnection();
         PreparedStatement ps = con.prepareStatement("DELETE FROM hyperstats WHERE charid = ? AND position = ?");
         ps.setInt(1, this.id);
         ps.setInt(2, position);
         ps.execute();
         ps.close();
         con.close();
      } catch (SQLException var5) {
         var5.printStackTrace();
      }

   }

   public void skillReset() {
      List<Skill> skillss = new ArrayList();
      Iterator var2 = this.skills.keySet().iterator();

      Skill i;
      while(var2.hasNext()) {
         i = (Skill)var2.next();
         skillss.add(i);
      }

      var2 = skillss.iterator();

      while(var2.hasNext()) {
         i = (Skill)var2.next();
         this.changeSkillLevel(i, (byte)0, (byte)0);
      }

   }

   public int getCosmicCount() {
      return this.CosmicCount;
   }

   public void setCosmicCount(int count) {
      SecondaryStatEffect eff = SkillFactory.getSkill(11001030).getEffect(this.getSkillLevel(11001030));
      SecondaryStatEffect eff2 = SkillFactory.getSkill(11001027).getEffect(this.getSkillLevel(11001027));
      this.CosmicCount = count;
      eff.applyTo(this, eff.getDuration());
      eff2.applyTo(this, eff.getDuration());
   }

   public int getMomentumCount() {
      return this.MomentumCount;
   }

   public void setMomentumCount(int count) {
      this.MomentumCount = count;
   }

   public long getLastSpawnBlindMobTime() {
      return this.lastSpawnBlindMobtime;
   }

   public void setLastSpawnBlindMobTime(long lastSpawnBlindMobtime) {
      this.lastSpawnBlindMobtime = lastSpawnBlindMobtime;
   }

   public boolean isExtremeMode() {
      return this.extremeMode;
   }

   public void setExtremeMode(boolean extremeMode) {
      this.extremeMode = extremeMode;
      Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
      statups.put(SecondaryStat.PmdReduce, new Pair(50, 0));
      this.client.getSession().writeAndFlush(CWvsContext.BuffPacket.cancelBuff(statups, this));
   }

   public boolean isHellMode() {
      return this.hellMode;
   }

   public void setHellMode(boolean extremeMode) {
      this.hellMode = extremeMode;
      Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
      statups.put(SecondaryStat.PmdReduce, new Pair(90, 0));
      this.client.getSession().writeAndFlush(CWvsContext.BuffPacket.cancelBuff(statups, this));
   }

   public void setAutoSkill(int skillId, boolean isAutoCast) {
      this.setKeyValue(1544, String.valueOf(skillId), String.valueOf(isAutoCast ? 1 : 0));
   }

   public boolean isAutoSkill(int skillId) {
      return this.getKeyValue(1544, String.valueOf(skillId)) == 1L;
   }

   public boolean hasSkill(int skillId) {
      return this.getSkillLevel(skillId) > 0;
   }

   private class MapleCharacterManagement implements Runnable {
      public void handleMobs(long time) {
         MapleMonster mob;
         if ((mob = MapleCharacter.this.getMap().getMonsterById(8870100)) != null && MapleCharacter.this.getDisease(SecondaryStat.VampDeath) != null) {
            MapleDiseases skill = MapleCharacter.this.getDisease(SecondaryStat.VampDeath);
            if (skill != null) {
               mob.heal(63000000L, 0L, true);
            }
         }

         if (MapleCharacter.this.getMap().getId() / 10000 == 92507) {
            if (MapleCharacter.this.getDojoStartTime() > 0 && MapleCharacter.this.getBuffedEffect(SecondaryStat.MobZoneState) == null) {
               Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
               statups.put(SecondaryStat.MobZoneState, new Pair(1, 0));
               MapleCharacter.this.client.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, (SecondaryStatEffect)null, MapleCharacter.this.getPlayer()));
            }

            if (!MapleCharacter.this.getDojoStop()) {
               MapleCharacter.this.setDojoStartTime(MapleCharacter.this.getDojoStartTime() + 1);
               if (MapleCharacter.this.getDojoStartTime() >= 900) {
                  MapleCharacter.this.getClient().getSession().writeAndFlush(CField.environmentChange("dojang/timeOver", 19));
                  MapleCharacter.this.MulungTimer = new Timer();
                  MapleCharacter.this.MulungTimerTask = new TimerTask() {
                     // $FF: synthetic field
                     final MapleCharacter.MapleCharacterManagement this$1;

                     {
                        this.this$1 = this$1;
                     }

                     public void run() {
                        this.this$1.this$0.warp(925020002);
                        this.this$1.this$0.dropMessage(5, "시간이 초과하여 무릉도장에서 퇴장합니다.");
                        this.this$1.this$0.cancelTimer();
                     }
                  };
                  MapleCharacter.this.MulungTimer.schedule(MapleCharacter.this.MulungTimerTask, 1000L);
               }
            }
         }

         Iterator var9 = MapleCharacter.this.getMap().getAllMonster().iterator();

         while(true) {
            MapleMonster mons;
            do {
               if (!var9.hasNext()) {
                  if ((mob = MapleCharacter.this.getMap().getMonsterById(8880000)) != null || (mob = MapleCharacter.this.getMap().getMonsterById(8880002)) != null || (mob = MapleCharacter.this.getMap().getMonsterById(8880010)) != null) {
                     short pix;
                     if (mob.getHPPercent() <= 25) {
                        pix = 190;
                     } else if (mob.getHPPercent() <= 50) {
                        pix = 290;
                     } else if (mob.getHPPercent() <= 75) {
                        pix = 330;
                     } else {
                        pix = 370;
                     }

                     boolean damaged = false;
                     if (MapleCharacter.this.getTruePosition().getX() <= mob.getTruePosition().getX() - (double)pix || MapleCharacter.this.getTruePosition().getX() >= mob.getTruePosition().getX() + (double)pix) {
                        damaged = true;
                     }

                     Map<SecondaryStat, Pair<Integer, Integer>> statups2 = new HashMap();
                     statups2.put(SecondaryStat.MobZoneState, new Pair(damaged ? 0 : 1, mob.getObjectId()));
                     MapleCharacter.this.client.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups2, (SecondaryStatEffect)null, MapleCharacter.this.getPlayer()));
                     MapleBossManager.changePhase(mob);
                     if (damaged && MapleCharacter.this.getBuffedEffect(SecondaryStat.NotDamaged) == null && MapleCharacter.this.getBuffedEffect(SecondaryStat.IndieNotDamaged) == null && !MapleCharacter.this.isGM()) {
                        int minushp = (int)(-MapleCharacter.this.getStat().getCurrentMaxHp() / 10L);
                        MapleCharacter.this.addHP(-MapleCharacter.this.getStat().getCurrentMaxHp() / 10L);
                        MapleCharacter.this.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(MapleCharacter.this.client.getPlayer(), 0, minushp, 36, 0, 0, (byte)0, true, (Point)null, (String)null, (Item)null));
                        MapleCharacter.this.getMap().broadcastMessage(MapleCharacter.this.client.getPlayer(), CField.EffectPacket.showEffect(MapleCharacter.this.client.getPlayer(), 0, minushp, 36, 0, 0, (byte)0, false, (Point)null, (String)null, (Item)null), false);
                     }
                  }

                  return;
               }

               mons = (MapleMonster)var9.next();
            } while((mons.getId() < 8220106 || mons.getId() > 8220108) && mons.getId() != 8220124);

            MapleCharacter.this.getMap().broadcastMessage(MobPacket.monsterForceMove(mons, mons.getController().getPosition()));
         }
      }

      public void handleEtcs(long time) {
         if (time - MapleCharacter.this.lastSaveTime >= 600000L && MapleCharacter.this.choicepotential == null && MapleCharacter.this.returnscroll == null && MapleCharacter.this.memorialcube == null) {
            MapleCharacter.this.saveToDB(false, false);
         }

         if (MapleCharacter.this.getKeyValue(1234, "ww_whim") != -1L && MapleCharacter.this.getKeyValue(1234, "ww_whim") > 0L && MapleCharacter.this.getStat().getHp() > 0L) {
            if (MapleCharacter.this.getSkillLevel(13120003) <= 1) {
               MapleCharacter.this.changeSingleSkillLevel(SkillFactory.getSkill(13120003), 30, (byte)30);
            }

            if (!MapleCharacter.this.getBuffedValue(13120003)) {
               SkillFactory.getSkill(13120003).getEffect(MapleCharacter.this.getSkillLevel(13120003)).applyTo(MapleCharacter.this.getPlayer(), false);
            }
         }

         if (MapleCharacter.this.getKeyValue(1234, "ww_buck") != -1L && MapleCharacter.this.getKeyValue(1234, "ww_buck") > 0L && MapleCharacter.this.getStat().getHp() > 0L) {
            if (MapleCharacter.this.getSkillLevel(5321054) < 1) {
               MapleCharacter.this.changeSingleSkillLevel(SkillFactory.getSkill(5321054), 1, (byte)1);
            }

            if (!MapleCharacter.this.getBuffedValue(5321054)) {
               SkillFactory.getSkill(5321054).getEffect(MapleCharacter.this.getSkillLevel(5321054)).applyTo(MapleCharacter.this.getPlayer(), false);
            }
         }

         if (MapleCharacter.this.getKeyValue(1234, "ww_cut") != -1L && MapleCharacter.this.getKeyValue(1234, "ww_cut") > 0L && MapleCharacter.this.getStat().getHp() > 0L) {
            if (MapleCharacter.this.getSkillLevel(4341002) < 1) {
               MapleCharacter.this.changeSingleSkillLevel(SkillFactory.getSkill(4341002), 1, (byte)1);
            }

            if (!MapleCharacter.this.getBuffedValue(4341002)) {
               SkillFactory.getSkill(4341002).getEffect(MapleCharacter.this.getSkillLevel(4341002)).applyTo(MapleCharacter.this.getPlayer(), false);
            }
         }

         if (MapleCharacter.this.getKeyValue(1234, "ww_magu") != -1L && MapleCharacter.this.getKeyValue(1234, "ww_magu") > 0L && MapleCharacter.this.getStat().getHp() > 0L) {
            if (MapleCharacter.this.getSkillLevel(2001002) < 10) {
               MapleCharacter.this.changeSingleSkillLevel(SkillFactory.getSkill(2001002), 10, (byte)10);
            }

            if (!MapleCharacter.this.getBuffedValue(2001002)) {
               SkillFactory.getSkill(2001002).getEffect(MapleCharacter.this.getSkillLevel(2001002)).applyTo(MapleCharacter.this.getPlayer(), false);
            }
         }

         if (MapleCharacter.this.getKeyValue(1234, "ww_holy") != -1L && MapleCharacter.this.getKeyValue(1234, "ww_holy") > 0L && MapleCharacter.this.getStat().getHp() > 0L) {
            if (MapleCharacter.this.getSkillLevel(2311003) < 20) {
               MapleCharacter.this.changeSingleSkillLevel(SkillFactory.getSkill(2311003), 20, (byte)20);
            }

            if (!MapleCharacter.this.getBuffedValue(2311003)) {
               SkillFactory.getSkill(2311003).getEffect(MapleCharacter.this.getSkillLevel(2311003)).applyTo(MapleCharacter.this.getPlayer());
            }
         }

         if (MapleCharacter.this.getKeyValue(1234, "ww_winb") != -1L && MapleCharacter.this.getKeyValue(1234, "ww_winb") > 0L && MapleCharacter.this.getStat().getHp() > 0L) {
            if (MapleCharacter.this.getSkillLevel(5121009) < 20) {
               MapleCharacter.this.changeSingleSkillLevel(SkillFactory.getSkill(5121009), 20, (byte)20);
            }

            if (!MapleCharacter.this.getBuffedValue(5121009)) {
               SkillFactory.getSkill(5121009).getEffect(MapleCharacter.this.getSkillLevel(5121009)).applyTo(MapleCharacter.this.getPlayer(), false);
            }
         }

         if (MapleCharacter.this.getKeyValue(1234, "ww_sharp") != -1L && MapleCharacter.this.getKeyValue(1234, "ww_sharp") > 0L && MapleCharacter.this.getStat().getHp() > 0L) {
            if (MapleCharacter.this.getSkillLevel(3121002) < 30) {
               MapleCharacter.this.changeSingleSkillLevel(SkillFactory.getSkill(3121002), 30, (byte)30);
            }

            if (!MapleCharacter.this.getBuffedValue(3121002)) {
               SkillFactory.getSkill(3121002).getEffect(MapleCharacter.this.getSkillLevel(3121002)).applyTo(MapleCharacter.this.getPlayer(), false);
            }
         }

         if (MapleCharacter.this.getKeyValue(53714, "atk") != -1L && MapleCharacter.this.getStat().getHp() > 0L) {
            if (MapleCharacter.this.getSkillLevel(80002924) != 1) {
               MapleCharacter.this.changeSingleSkillLevel(SkillFactory.getSkill(80002924), 1, (byte)1);
            }

            if (!MapleCharacter.this.getBuffedValue(80002924)) {
               SkillFactory.getSkill(80002924).getEffect(1).applyTo(MapleCharacter.this.getPlayer(), 0);
            }
         }

         MapleCharacter var10000;
         if (MapleCharacter.this.getKeyValue(210416, "TotalDeadTime") > 0L) {
            var10000 = MapleCharacter.this;
            long var10003 = MapleCharacter.this.getKeyValue(210416, "NowDeadTime");
            var10000.setKeyValue(210416, "NowDeadTime", (var10003 - 1L).makeConcatWithConstants<invokedynamic>(var10003 - 1L));
            MapleCharacter.this.client.send(CField.ExpDropPenalty(false, (int)MapleCharacter.this.getKeyValue(210416, "TotalDeadTime"), (int)MapleCharacter.this.getKeyValue(210416, "NowDeadTime"), 80, 80));
            if (MapleCharacter.this.getKeyValue(210416, "NowDeadTime") <= 0L) {
               MapleCharacter.this.client.send(CField.ExpDropPenalty(false, 0, 0, 0, 0));
               MapleCharacter.this.removeKeyValue(210416);
            }
         }

         MapleMap mapz;
         if (!MapleCharacter.this.haveItem(4033235) && MapleCharacter.this.getMapId() == 921170011) {
            mapz = ChannelServer.getInstance(MapleCharacter.this.getClient().getChannel()).getMapFactory().getMap(ServerConstants.warpMap);
            MapleCharacter.this.changeMap(mapz, mapz.getPortal(0));
         }

         if (!MapleCharacter.this.haveItem(1143195) && MapleCharacter.this.getMapId() == 101) {
            mapz = ChannelServer.getInstance(MapleCharacter.this.getClient().getChannel()).getMapFactory().getMap(ServerConstants.warpMap);
            MapleCharacter.this.changeMap(mapz, mapz.getPortal(0));
         }

         if (!MapleCharacter.this.haveItem(1143195) && MapleCharacter.this.getMapId() == 102) {
            mapz = ChannelServer.getInstance(MapleCharacter.this.getClient().getChannel()).getMapFactory().getMap(ServerConstants.warpMap);
            MapleCharacter.this.changeMap(mapz, mapz.getPortal(0));
         }

         if (!MapleCharacter.this.haveItem(1143196) && MapleCharacter.this.getMapId() == 103) {
            mapz = ChannelServer.getInstance(MapleCharacter.this.getClient().getChannel()).getMapFactory().getMap(ServerConstants.warpMap);
            MapleCharacter.this.changeMap(mapz, mapz.getPortal(0));
         }

         if (!MapleCharacter.this.haveItem(1143196) && MapleCharacter.this.getMapId() == 109) {
            mapz = ChannelServer.getInstance(MapleCharacter.this.getClient().getChannel()).getMapFactory().getMap(ServerConstants.warpMap);
            MapleCharacter.this.changeMap(mapz, mapz.getPortal(0));
         }

         if (!MapleCharacter.this.haveItem(1143197) && MapleCharacter.this.getMapId() == 105) {
            mapz = ChannelServer.getInstance(MapleCharacter.this.getClient().getChannel()).getMapFactory().getMap(ServerConstants.warpMap);
            MapleCharacter.this.changeMap(mapz, mapz.getPortal(0));
         }

         if (MapleCharacter.this.getMapId() == 100 && !MapleCharacter.this.haveItem(1143197) && !MapleCharacter.this.haveItem(1143197)) {
            mapz = ChannelServer.getInstance(MapleCharacter.this.getClient().getChannel()).getMapFactory().getMap(ServerConstants.warpMap);
            MapleCharacter.this.changeMap(mapz, mapz.getPortal(0));
         }

         if (MapleCharacter.this.getMapId() == 106 && !MapleCharacter.this.haveItem(1143198) && !MapleCharacter.this.haveItem(1143198)) {
            mapz = ChannelServer.getInstance(MapleCharacter.this.getClient().getChannel()).getMapFactory().getMap(ServerConstants.warpMap);
            MapleCharacter.this.changeMap(mapz, mapz.getPortal(0));
         }

         if (MapleCharacter.this.getMapId() == 107 && !MapleCharacter.this.haveItem(1143198) && !MapleCharacter.this.haveItem(1143198)) {
            mapz = ChannelServer.getInstance(MapleCharacter.this.getClient().getChannel()).getMapFactory().getMap(ServerConstants.warpMap);
            MapleCharacter.this.changeMap(mapz, mapz.getPortal(0));
         }

         if (MapleCharacter.this.getMapId() == 108 && !MapleCharacter.this.haveItem(1143199) && !MapleCharacter.this.haveItem(1143199)) {
            mapz = ChannelServer.getInstance(MapleCharacter.this.getClient().getChannel()).getMapFactory().getMap(ServerConstants.warpMap);
            MapleCharacter.this.changeMap(mapz, mapz.getPortal(0));
         }

         if (MapleCharacter.this.getMapId() == 104 && !MapleCharacter.this.haveItem(1143199) && !MapleCharacter.this.haveItem(1143199)) {
            mapz = ChannelServer.getInstance(MapleCharacter.this.getClient().getChannel()).getMapFactory().getMap(ServerConstants.warpMap);
            MapleCharacter.this.changeMap(mapz, mapz.getPortal(0));
         }

         long allnujuk;
         int level;
         int a;
         long var10002;
         int coing;
         if (MapleCharacter.this.getKeyValue(27040, "runnigtime") > 0L) {
            KoreaCalendar kc = new KoreaCalendar();
            int var48 = kc.getYeal() % 100;
            String date = var48 + "/" + kc.getMonths() + "/" + kc.getDays();
            allnujuk = MapleCharacter.this.getKeyValue(27040, "runnigtime");
            level = (int)allnujuk / 3600;
            if (level >= 2) {
               level = 2;
            }

            Item item = MapleCharacter.this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-17);
            Item item2 = MapleCharacter.this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-31);
            boolean itemexppendent = false;
            Item exppen = null;
            if (item != null) {
               itemexppendent = item.getItemId() == 1122017 || item.getItemId() == 1122155 || item.getItemId() == 1122215;
               if (itemexppendent) {
                  exppen = item;
               }
            }

            if (item2 != null && !itemexppendent) {
               itemexppendent = item2.getItemId() == 1122017 || item2.getItemId() == 1122155 || item2.getItemId() == 1122215;
               if (itemexppendent) {
                  exppen = item2;
               }
            }

            if ((long)kc.getDayt() == MapleCharacter.this.getKeyValue(27040, "equipday") && (long)kc.getMonth() == MapleCharacter.this.getKeyValue(27040, "equipmonth") && exppen != null) {
               coing = level == 2 ? 30 : (level == 1 ? 20 : 10);
               a = (int)allnujuk / 60;
               MapleCharacter.this.setKeyValue(27040, "runnigtime", (allnujuk + 1L).makeConcatWithConstants<invokedynamic>(allnujuk + 1L));
               if (MapleCharacter.this.getKeyValue(27040, "runnigtime") % 60L == 0L) {
                  MapleCharacter.this.getClient().send(CWvsContext.SpritPandent(exppen.getPosition(), false, level, coing, a));
               }
            } else {
               MapleCharacter.this.removeKeyValue(27040);
               if (itemexppendent) {
                  var48 = kc.getYeal() % 100;
                  String nowtime = var48 + kc.getMonths() + kc.getDays() + kc.getHours() + kc.getMins() + kc.getMins();
                  MapleCharacter.this.setKeyValue(27040, "runnigtime", "1");
                  MapleCharacter.this.setKeyValue(27040, "firstequiptime", nowtime);
                  MapleCharacter.this.setKeyValue(27040, "firstequiptimemil", System.currentTimeMillis().makeConcatWithConstants<invokedynamic>(System.currentTimeMillis()));
                  MapleCharacter.this.setKeyValue(27040, "equipday", kc.getDayt().makeConcatWithConstants<invokedynamic>(kc.getDayt()));
                  MapleCharacter.this.setKeyValue(27040, "equipmonth", kc.getMonths().makeConcatWithConstants<invokedynamic>(kc.getMonths()));
                  var10000 = MapleCharacter.this;
                  var10002 = exppen.getInventoryId();
                  var10000.updateInfoQuest(27039, var10002 + "=" + nowtime + "|" + nowtime + "|0|0|0");
                  MapleCharacter.this.getClient().send(CWvsContext.SpritPandent(exppen.getPosition(), true, 0, 10, 0));
                  var10000 = MapleCharacter.this;
                  var10002 = exppen.getInventoryId();
                  var10000.updateInfoQuest(27039, var10002 + "=" + nowtime + "|" + nowtime + "|0|10|0");
               }
            }
         }

         int questid;
         if ((MapleCharacter.this.getMap().isSpawnPoint() || MapleCharacter.this.getMap().isTown()) && !GameConstants.isContentsMap(MapleCharacter.this.getMap().getId()) && !GameConstants.보스맵(MapleCharacter.this.getMap().getId()) && !GameConstants.사냥컨텐츠맵(MapleCharacter.this.getMap().getId()) && !GameConstants.튜토리얼(MapleCharacter.this.getMap().getId()) && !GameConstants.로미오줄리엣(MapleCharacter.this.getMap().getId()) && !GameConstants.피라미드(MapleCharacter.this.getMap().getId())) {
            if (MapleCharacter.this.getV("lastSudden") == null) {
               MapleCharacter.this.addKV("lastSudden", System.currentTimeMillis().makeConcatWithConstants<invokedynamic>(System.currentTimeMillis()));
            }

            if (Randomizer.isSuccess(1, 1000) && Long.parseLong(MapleCharacter.this.getV("lastSudden")) < System.currentTimeMillis()) {
               boolean var35 = false;

               do {
                  questid = Randomizer.rand(49001, 49018);
               } while(questid == 49015);

               MapleCharacter.this.gainSuddenMission(questid, 49000, true);
               var10000 = MapleCharacter.this;
               var10002 = System.currentTimeMillis();
               var10000.addKV("lastSudden", (var10002 + 1000000000L).makeConcatWithConstants<invokedynamic>(var10002 + 1000000000L));
            }
         }

         if (MapleCharacter.this.getKeyValue(51351, "startquestid") == 49011L && MapleCharacter.this.getMap().getBurning() > 0 && MapleCharacter.this.getKeyValue(51351, "queststat") != 3L && !MapleCharacter.this.getMap().isTown() && !GameConstants.로미오줄리엣(MapleCharacter.this.getMapId()) && !GameConstants.사냥컨텐츠맵(MapleCharacter.this.getMapId()) && MapleCharacter.this.getMap().isSpawnPoint() && !GameConstants.isContentsMap(MapleCharacter.this.getMapId())) {
            MapleCharacter.this.setKeyValue(51351, "queststat", "3");
            MapleCharacter.this.getClient().send(CWvsContext.updateSuddenQuest((int)MapleCharacter.this.getKeyValue(51351, "midquestid"), false, PacketHelper.getKoreanTimestamp(System.currentTimeMillis()) + 600000000L, "count=1;Quest=" + MapleCharacter.this.getKeyValue(51351, "startquestid") + ";state=3;"));
            MapleCharacter.this.getClient().send(CWvsContext.updateSuddenQuest((int)MapleCharacter.this.getKeyValue(51351, "startquestid"), false, MapleCharacter.this.getKeyValue(51351, "endtime"), "BTField=1;"));
         }

         int[] var31;
         int var33;
         int[] array3;
         int skill2;
         if (MapleCharacter.this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-26) != null) {
            questid = MapleCharacter.this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-26).getItemId();
            if (questid != 1162000 && questid != 1162001 && questid != 1162002 && questid != 1162004) {
               int[] array = new int[]{80001544, 80001545, 80001546};
               int[] var6 = array;
               level = array.length;

               for(skill2 = 0; skill2 < level; ++skill2) {
                  int skill = var6[skill2];
                  if (MapleCharacter.this.getBuffedValue(skill)) {
                     MapleCharacter.this.cancelEffect(MapleCharacter.this.getBuffedEffect(skill));
                  }

                  if (MapleCharacter.this.getSkillLevel(skill) > 0) {
                     MapleCharacter.this.changeSkillLevel(skill, (byte)0, (byte)0);
                  }
               }
            } else {
               int skillid = questid == 1162000 ? 80001544 : (questid == 1162001 ? 80001545 : 80001546);
               if (SkillFactory.getSkill(skillid) != null && !MapleCharacter.this.getBuffedValue(skillid)) {
                  if (MapleCharacter.this.getSkillLevel(skillid) <= 0) {
                     MapleCharacter.this.changeSkillLevel(skillid, (byte)1, (byte)1);
                  }

                  SkillFactory.getSkill(skillid).getEffect(1).applyTo(MapleCharacter.this);
               }
            }
         } else {
            array3 = new int[]{80001544, 80001545, 80001546};
            var31 = array3;
            var33 = array3.length;

            for(level = 0; level < var33; ++level) {
               skill2 = var31[level];
               if (MapleCharacter.this.getBuffedValue(skill2)) {
                  MapleCharacter.this.cancelEffect(MapleCharacter.this.getBuffedEffect(skill2));
               }

               if (MapleCharacter.this.getSkillLevel(skill2) > 0) {
                  MapleCharacter.this.changeSkillLevel(skill2, (byte)0, (byte)0);
               }
            }
         }

         if (MapleCharacter.this.getSkillLevel(80001535) > 0) {
            if (!MapleCharacter.this.getBuffedValue(80001535)) {
               SkillFactory.getSkill(80001535).getEffect(1).applyTo(MapleCharacter.this);
            }
         } else if (MapleCharacter.this.getBuffedValue(80001535)) {
            MapleCharacter.this.cancelEffect(MapleCharacter.this.getBuffedEffect(80001535));
         }

         array3 = new int[]{80001537, 80001539};
         var31 = array3;
         var33 = array3.length;

         for(level = 0; level < var33; ++level) {
            skill2 = var31[level];
            if (MapleCharacter.this.getSkillLevel(skill2) > 0) {
               if (!MapleCharacter.this.getBuffedValue(skill2)) {
                  SkillFactory.getSkill(skill2).getEffect(1).applyTo(MapleCharacter.this);
               }
            } else if (MapleCharacter.this.getBuffedValue(skill2)) {
               MapleCharacter.this.cancelEffect(MapleCharacter.this.getBuffedEffect(skill2));
            }
         }

         if (MapleCharacter.this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-29) != null) {
            int itemid2 = MapleCharacter.this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-29).getItemId();
            if (itemid2 >= 1182001 && itemid2 <= 1182005) {
               if (MapleCharacter.this.getV("DawnItem") == null) {
                  MapleCharacter.this.addKV("DawnItem", itemid2.makeConcatWithConstants<invokedynamic>(itemid2));
               }

               if (!MapleCharacter.this.getBuffedValue(80001809) || Integer.parseInt(MapleCharacter.this.getV("DawnItem")) != itemid2) {
                  if (Integer.parseInt(MapleCharacter.this.getV("DawnItem")) != itemid2) {
                     MapleCharacter.this.addKV("DawnItem", itemid2.makeConcatWithConstants<invokedynamic>(itemid2));
                  }

                  if (MapleCharacter.this.getBuffedValue(80001809)) {
                     MapleCharacter.this.cancelEffect(MapleCharacter.this.getBuffedEffect(80001809));
                  }

                  SkillFactory.getSkill(80001809).getEffect(1).applyTo(MapleCharacter.this);
               }
            }
         } else if (MapleCharacter.this.getBuffedValue(80001809)) {
            MapleCharacter.this.cancelEffect(MapleCharacter.this.getBuffedEffect(80001809));
         }

         if (MapleCharacter.this.getClient().getKeyValue("UnionCoin") == null) {
            MapleCharacter.this.getClient().setKeyValue("UnionCoin", "0");
         }

         if (MapleCharacter.this.getClient().getKeyValue("UnionCoinNujuk") == null) {
            MapleCharacter.this.getClient().setKeyValue("UnionCoinNujuk", "0");
         }

         if (MapleCharacter.this.getClient().getKeyValue("UnionAllNujuk") == null) {
            MapleCharacter.this.getClient().setKeyValue("UnionAllNujuk", "0");
         }

         if (MapleCharacter.this.getClient().getKeyValue("UnionNujuk") == null) {
            MapleCharacter.this.getClient().setKeyValue("UnionNujuk", "0");
         }

         if (MapleCharacter.this.getClient().getKeyValue("UnionEndTime") == null) {
            MapleCharacter.this.getClient().setKeyValue("UnionEndTime", "0");
         }

         if (MapleCharacter.this.getClient().getKeyValue("UnionEnterTime") == null) {
            MapleCharacter.this.getClient().setKeyValue("UnionEnterTime", "0");
         }

         if (GameConstants.isUnionRaid(MapleCharacter.this.getMapId())) {
            allnujuk = MapleCharacter.this.getUnionAllNujuk();
            long nujuk = MapleCharacter.this.getUnionNujuk();
            long attackrate = MapleCharacter.this.getUnionDamage();
            int coin = MapleCharacter.this.getUnionCoin();
            coing = (int)MapleCharacter.this.getUnionEnterTime();
            MapleCharacter.this.setUnionAllNujuk(allnujuk + attackrate);
            if (nujuk < 2500000000000L) {
               MapleCharacter.this.setUnionNujuk(nujuk + attackrate);
            }

            if ((allnujuk + nujuk) / 100000000000L - (long)coing != 0L) {
               a = (int)((allnujuk + nujuk) / 100000000000L - (long)coing);
               MapleCharacter.this.setUnionCoin(coin + a);
               MapleCharacter.this.setUnionEnterTime((long)(coing + a));
               MapleCharacter.this.getClient().send(CField.setUnionRaidCoinNum(0, false));
               MapleCharacter.this.getClient().send(CField.setUnionRaidCoinNum(coin + 1, true));
            }

            MapleCharacter.this.getClient().send(CField.setUnionRaidScore(allnujuk + nujuk));
            long hp = 0L;
            long maxhp = 0L;
            long hp2 = 0L;
            long maxhp2 = 0L;
            int mobid = Integer.parseInt(MapleCharacter.this.getClient().getKeyValue("UnionLaidLevel")) == 1 ? 9833101 : (Integer.parseInt(MapleCharacter.this.getClient().getKeyValue("UnionLaidLevel")) == 2 ? 9833102 : (Integer.parseInt(MapleCharacter.this.getClient().getKeyValue("UnionLaidLevel")) == 3 ? 9833103 : (Integer.parseInt(MapleCharacter.this.getClient().getKeyValue("UnionLaidLevel")) == 4 ? 9833104 : (Integer.parseInt(MapleCharacter.this.getClient().getKeyValue("UnionLaidLevel")) == 5 ? 9833105 : 0))));
            int mobid2 = mobid + 100;
            int mobsize = 0;
            int flyingmob = 0;
            Iterator pos44x = MapleCharacter.this.getMap().getAllMonster().iterator();

            while(true) {
               MapleMonster monster;
               do {
                  if (!pos44x.hasNext()) {
                     if (MapleCharacter.this.getSkillCustomValue(1598857) == null) {
                        MapleCharacter.this.setSkillCustomInfo(1598857, 0L, 4400L);
                        MapleCharacter.this.getClient().send(CField.setUnionRaidCoinNum(coin, true));
                     }

                     pos44x = null;
                     MapleMonster i;
                     Point pos44;
                     int a2;
                     int type;
                     if (mobsize < 8) {
                        for(a2 = mobsize; a2 < 8; ++a2) {
                           type = Randomizer.rand(0, 10);
                           if (type == 0) {
                              pos44 = new Point(Randomizer.rand(1452, 1921), -596);
                           } else if (type == 1) {
                              pos44 = new Point(Randomizer.rand(1452, 1921), -440);
                           } else if (type == 2) {
                              pos44 = new Point(Randomizer.rand(1452, 1921), -284);
                           } else if (type == 3) {
                              pos44 = new Point(Randomizer.rand(1452, 1921), -128);
                           } else if (type == 4) {
                              pos44 = new Point(Randomizer.rand(2737, 3208), -596);
                           } else if (type == 5) {
                              pos44 = new Point(Randomizer.rand(2737, 3208), -440);
                           } else if (type == 6) {
                              pos44 = new Point(Randomizer.rand(2737, 3208), -284);
                           } else if (type == 7) {
                              pos44 = new Point(Randomizer.rand(2737, 3208), -128);
                           } else {
                              pos44 = new Point(Randomizer.rand(1597, 3199), 17);
                           }

                           i = MapleLifeFactory.getMonster(Randomizer.rand(9833106, 9833109));
                           i.getStats().setLevel((short)200);
                           i.setHp(10000000L);
                           i.getStats().setHp(10000000L);
                           i.setOwner(MapleCharacter.this.getId());
                           if (MapleCharacter.this.getMapId() == 921172000 || MapleCharacter.this.getMapId() == 921172100) {
                              MapleCharacter.this.getMap().spawnMonsterOnGroundBelow(i, pos44);
                           }
                        }
                     }

                     if (flyingmob < 3) {
                        for(a2 = flyingmob; a2 < 4; ++a2) {
                           type = Randomizer.rand(0, 10);
                           if (type == 0) {
                              pos44 = new Point(Randomizer.rand(1452, 1921), -596);
                           } else if (type == 1) {
                              pos44 = new Point(Randomizer.rand(1452, 1921), -440);
                           } else if (type == 2) {
                              pos44 = new Point(Randomizer.rand(1452, 1921), -284);
                           } else if (type == 3) {
                              pos44 = new Point(Randomizer.rand(1452, 1921), -128);
                           } else if (type == 4) {
                              pos44 = new Point(Randomizer.rand(2737, 3208), -596);
                           } else if (type == 5) {
                              pos44 = new Point(Randomizer.rand(2737, 3208), -440);
                           } else if (type == 6) {
                              pos44 = new Point(Randomizer.rand(2737, 3208), -284);
                           } else if (type == 7) {
                              pos44 = new Point(Randomizer.rand(2737, 3208), -128);
                           } else {
                              pos44 = new Point(Randomizer.rand(1597, 3199), 17);
                           }

                           i = MapleLifeFactory.getMonster(Randomizer.isSuccess(40) ? 9833111 : 9833110);
                           i.getStats().setLevel((short)200);
                           i.setHp(20000000L);
                           i.getStats().setHp(20000000L);
                           i.setOwner(MapleCharacter.this.getId());
                           if (MapleCharacter.this.getMapId() == 921172000 || MapleCharacter.this.getMapId() == 921172100) {
                              MapleCharacter.this.getMap().spawnMonsterOnGroundBelow(i, pos44);
                           }
                        }
                     }

                     MapleCharacter.this.getClient().send(CField.showUnionRaidHpUI(mobid, hp2, maxhp2, mobid2, hp, maxhp));
                     return;
                  }

                  monster = (MapleMonster)pos44x.next();
               } while(monster.getOwner() != MapleCharacter.this.getId());

               if (monster.getId() >= 9833106 && monster.getId() <= 9833111) {
                  if (monster.getId() != 9833110 && monster.getId() != 9833111) {
                     ++mobsize;
                  } else {
                     ++flyingmob;
                  }
               }

               if (monster.getId() == mobid) {
                  hp = monster.getMobMaxHp() - (allnujuk + nujuk);
                  maxhp = monster.getMobMaxHp();
                  if (hp <= 0L) {
                     MapleCharacter.this.getMap().killMonster(monster, -1);
                     MapleCharacter.this.setUnionAllNujuk(0L);
                     MapleCharacter.this.setUnionNujuk(0L);
                     MapleCharacter.this.setUnionEnterTime(0L);
                     if (Integer.parseInt(MapleCharacter.this.getClient().getKeyValue("UnionLaidLevel")) < 5) {
                        MapleClient var49 = MapleCharacter.this.getClient();
                        int var50 = Integer.parseInt(MapleCharacter.this.getClient().getKeyValue("UnionLaidLevel"));
                        var49.setKeyValue("UnionLaidLevel", (var50 + 1).makeConcatWithConstants<invokedynamic>(var50 + 1));
                     } else {
                        MapleCharacter.this.getClient().setKeyValue("UnionLaidLevel", "1");
                     }

                     mobid = Integer.parseInt(MapleCharacter.this.getClient().getKeyValue("UnionLaidLevel")) == 1 ? 9833101 : (Integer.parseInt(MapleCharacter.this.getClient().getKeyValue("UnionLaidLevel")) == 2 ? 9833102 : (Integer.parseInt(MapleCharacter.this.getClient().getKeyValue("UnionLaidLevel")) == 3 ? 9833103 : (Integer.parseInt(MapleCharacter.this.getClient().getKeyValue("UnionLaidLevel")) == 4 ? 9833104 : (Integer.parseInt(MapleCharacter.this.getClient().getKeyValue("UnionLaidLevel")) == 5 ? 9833105 : 0))));
                     MapleMonster m = MapleLifeFactory.getMonster(mobid);
                     m.setHp(10000000000000L);
                     m.getStats().setHp(10000000000000L);
                     m.setOwner(MapleCharacter.this.getId());
                     MapleCharacter.this.getMap().spawnMonsterOnGroundBelow(m, new Point(2320, 17));
                     m = MapleLifeFactory.getMonster(mobid + 100);
                     m.setHp(2500000000000L);
                     m.getStats().setHp(2500000000000L);
                     m.setOwner(MapleCharacter.this.getId());
                     MapleCharacter.this.getMap().spawnMonsterOnGroundBelow(m, new Point(2320, 17));
                  }
               } else if (monster.getId() == mobid2) {
                  hp2 = monster.getMobMaxHp() - nujuk;
                  maxhp2 = monster.getMobMaxHp();
                  if (hp2 <= 0L) {
                     hp2 = 1L;
                     if (monster.getBuff(MonsterStatus.MS_PowerImmune) == null) {
                        monster.damage(MapleCharacter.this.getClient().getPlayer(), maxhp2, false);
                     }
                  }
               }
            }
         }
      }

      public void handleInventorys(long time) {
         MapleInventory[] var3 = MapleCharacter.this.getInventorys();
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            MapleInventory inv = var3[var5];
            Iterator var7 = inv.list().iterator();

            while(var7.hasNext()) {
               Item item = (Item)var7.next();
               if (item.getExpiration() != -1L && item.getExpiration() <= time && !GameConstants.isPet(item.getItemId())) {
                  if (item.getPosition() < 0) {
                     MapleInventoryManipulator.unequip(MapleCharacter.this.getClient(), item.getPosition(), MapleCharacter.this.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot(), MapleInventoryType.EQUIP);
                  }

                  MapleInventoryManipulator.removeFromSlot(MapleCharacter.this.getClient(), GameConstants.getInventoryType(item.getItemId()), item.getPosition(), item.getQuantity(), false);
                  MapleCharacter.this.getPlayer().dropMessage(5, "아이템 [" + MapleItemInformationProvider.getInstance().getName(item.getItemId()) + "]의 사용기간이 지나서 아이템이 제거되었습니다.");
               }
            }
         }

      }

      public void handleCooldowns(long now) {
         List<MapleCoolDownValueHolder> cooldowns = new ArrayList();
         if (MapleCharacter.this.getCooldownSize() > 0) {
            Iterator var4 = MapleCharacter.this.getCooldowns().iterator();

            while(var4.hasNext()) {
               MapleCoolDownValueHolder m = (MapleCoolDownValueHolder)var4.next();
               if (m.startTime + m.length < now) {
                  cooldowns.add(m);
               }
            }
         }

         if (!cooldowns.isEmpty()) {
            MapleCharacter.this.clearCooldowns(cooldowns);
         }

         if (MapleCharacter.this.getCooldownLimit(31121054) > 0L && now - MapleCharacter.this.cooldownforceBlood >= 3000L) {
            MapleCharacter.this.changeCooldown(31121054, -2000);
         }

      }

      public void handleSkillOptions(long time) {
         SecondaryStatEffect effect;
         if (MapleCharacter.this.getSkillLevel(27110007) > 0 && MapleCharacter.this.getSkillCustomValue(27110007) == null) {
            MapleCharacter chr = MapleCharacter.this.client.getPlayer();
            effect = SkillFactory.getSkill(27110007).getEffect(MapleCharacter.this.getSkillLevel(27110007));
            Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
            statups.put(SecondaryStat.LifeTidal, new Pair(1, 0));
            if (MapleCharacter.this.getStat().getHPPercent() > MapleCharacter.this.getStat().getMPPercent()) {
               MapleCharacter.this.addMP((long)((int)((double)(MapleCharacter.this.getStat().getCurrentMaxMp(chr) / 100L) * effect.getT())));
            } else {
               MapleCharacter.this.addHP((long)((int)((double)(MapleCharacter.this.getStat().getCurrentMaxHp() / 100L) * effect.getT())));
            }

            if (!statups.isEmpty()) {
               MapleCharacter.this.client.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, effect, MapleCharacter.this.client.getPlayer()));
            }

            MapleCharacter.this.setSkillCustomInfo(27110007, 0L, 5000L);
         }

         if (MapleCharacter.this.getBuffedValue(32121018) || MapleCharacter.this.getBuffedValue(400021006)) {
            boolean read = true;
            if (MapleCharacter.this.getBuffedValue(400021006) && MapleCharacter.this.getBuffedOwner(400021006) != MapleCharacter.this.id) {
               read = false;
            }

            if (read) {
               effect = SkillFactory.getSkill(32121018).getEffect(MapleCharacter.this.getSkillLevel(32121018));
               if (!MapleCharacter.this.getBuffedValue(400021006)) {
                  MapleCharacter.this.addMP((long)(-effect.getMPCon()));
               }

               if (time - (MapleCharacter.this.getBuffedValue(400021006) ? MapleCharacter.this.checkBuffStatValueHolder(SecondaryStat.UnionAura, 400021006).startTime : MapleCharacter.this.checkBuffStatValueHolder(SecondaryStat.DebuffAura, 32121018).startTime) >= 2000L) {
                  Iterator var13 = MapleCharacter.this.getMap().getAllMonster().iterator();

                  while(var13.hasNext()) {
                     MapleMonster monster = (MapleMonster)var13.next();
                     if (MapleCharacter.this.getTruePosition().x + effect.getLt().x < monster.getTruePosition().x && MapleCharacter.this.getTruePosition().x - effect.getLt().x > monster.getTruePosition().x && MapleCharacter.this.getTruePosition().y + effect.getLt().y < monster.getTruePosition().y && MapleCharacter.this.getTruePosition().y - effect.getLt().y > monster.getTruePosition().y && monster.getBuff(MonsterStatus.MS_TrueSight) == null) {
                        List<Pair<MonsterStatus, MonsterStatusEffect>> applys = new ArrayList();
                        applys.add(new Pair(MonsterStatus.MS_BMageDebuff, new MonsterStatusEffect(effect.getSourceId(), effect.getDuration(), (long)effect.getX())));
                        if (MapleCharacter.this.getSkillLevel(32120061) > 0) {
                           SecondaryStatEffect effect2 = SkillFactory.getSkill(32120061).getEffect(1);
                           applys.add(new Pair(MonsterStatus.MS_TrueSight, new MonsterStatusEffect(effect2.getSourceId(), effect.getDuration(), (long)(-effect2.getS()))));
                           applys.add(new Pair(MonsterStatus.MS_IndieUNK, new MonsterStatusEffect(effect2.getSourceId(), effect.getDuration(), (long)(-effect2.getX()))));
                        }

                        monster.applyStatus(MapleCharacter.this.getClient(), applys, effect);
                     }
                  }
               }
            }
         }

         SecondaryStatEffect eff;
         if (MapleCharacter.this.getBuffedEffect(SecondaryStat.IceAura) != null && MapleCharacter.this.getJob() == 222) {
            MapleCharacter.this.addMP(-60L);
            eff = SkillFactory.getSkill(2221054).getEffect(1);
            Iterator var11 = MapleCharacter.this.getMap().getAllMonster().iterator();

            while(var11.hasNext()) {
               MapleMonster mob = (MapleMonster)var11.next();
               if (MapleCharacter.this.getTruePosition().x + eff.getLt().x < mob.getTruePosition().x && MapleCharacter.this.getTruePosition().x - eff.getLt().x > mob.getTruePosition().x && MapleCharacter.this.getTruePosition().y + eff.getLt().y < MapleCharacter.this.getTruePosition().y && MapleCharacter.this.getTruePosition().y - eff.getLt().y > mob.getTruePosition().y && mob.isAlive()) {
                  if (mob.getBuff(MonsterStatus.MS_Speed) == null && mob.getFreezingOverlap() > 0) {
                     mob.setFreezingOverlap(0);
                     if (mob.getFreezingOverlap() <= 0) {
                        mob.cancelStatus(MonsterStatus.MS_Speed, mob.getBuff(2221054));
                     }
                  }

                  if (mob.getFreezingOverlap() < 5) {
                     mob.setFreezingOverlap((byte)(mob.getFreezingOverlap() + 1));
                  }

                  MonsterStatusEffect effect3 = new MonsterStatusEffect(2221054, 8000);
                  mob.applyStatus(MapleCharacter.this.getClient(), MonsterStatus.MS_Speed, effect3, eff.getV(), eff);
               }
            }
         }

         if (MapleCharacter.this.getBuffedValue(SecondaryStat.Infinity) != null && time - MapleCharacter.this.lastInfinityTime >= 4000L && MapleCharacter.this.getInfinity() < 25) {
            MapleCharacter.this.setInfinity((byte)(MapleCharacter.this.getInfinity() + 1));
            MapleCharacter.this.setBuffedValue(SecondaryStat.Infinity, MapleCharacter.this.getInfinity());
            eff = SkillFactory.getSkill(MapleCharacter.this.getBuffSource(SecondaryStat.Infinity)).getEffect(MapleCharacter.this.getSkillLevel(MapleCharacter.this.getBuffSource(SecondaryStat.Infinity)));
            MapleCharacter.this.addHP((long)((double)MapleCharacter.this.getStat().getMaxHp() * 0.1D));
            MapleCharacter.this.addMP((long)((double)MapleCharacter.this.getStat().getMaxMp() * 0.1D));
            MapleCharacter.this.lastInfinityTime = System.currentTimeMillis();
            Map<SecondaryStat, Pair<Integer, Integer>> statups2 = new HashMap();
            statups2.put(SecondaryStat.Infinity, new Pair(Integer.valueOf(MapleCharacter.this.getInfinity()), (int)MapleCharacter.this.getBuffLimit(MapleCharacter.this.getBuffSource(SecondaryStat.Infinity))));
            MapleCharacter.this.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups2, eff, MapleCharacter.this.client.getPlayer()));
         }

      }

      public void run() {
         long time = System.currentTimeMillis();
         MapleCharacter.this.handleSecondaryStats(time, false);
         this.handleCooldowns(time);
         if (MapleCharacter.this.isAlive()) {
            if (MapleCharacter.this.canFairy(time)) {
               MapleCharacter.this.doFairy();
            }

            if (MapleCharacter.this.canDOT(time) && MapleCharacter.this.hasDOT()) {
               MapleCharacter.this.doDOT();
            }

            if (MapleCharacter.this.canRecover(time)) {
               MapleCharacter.this.doRecovery();
            }

            this.handleSkillOptions(time);
            MapleCharacter.this.handleAdditionalSkills(time);
            MapleCharacter.this.handleHealSkills(time);
            MapleCharacter.this.handleSummons(time);
            this.handleMobs(time);
            this.handleInventorys(time);
            this.handleEtcs(time);
         }

      }
   }

   public static enum FameStatus {
      OK,
      NOT_TODAY,
      NOT_THIS_MONTH;
   }
}
