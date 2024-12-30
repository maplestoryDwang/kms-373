package scripting;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleQuestStatus;
import client.MapleTrait;
import client.Skill;
import client.SkillFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.GameConstants;
import database.DatabaseConnection;
import handling.SendPacketOpcode;
import handling.channel.ChannelServer;
import handling.world.World;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildAlliance;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import java.awt.Point;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.enchant.EnchantFlag;
import server.enchant.EquipmentEnchant;
import server.enchant.StarForceStats;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.games.ColorInvitationCard;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.life.MobSkill;
import server.maps.Event_DojoAgent;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleReactor;
import server.maps.SavedLocationType;
import server.marriage.MarriageDataEntry;
import server.marriage.MarriageEventAgent;
import server.marriage.MarriageManager;
import server.marriage.MarriageTicketType;
import server.quest.MapleQuest;
import tools.FileoutputUtil;
import tools.HexTool;
import tools.Pair;
import tools.StringUtil;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.MobPacket;
import tools.packet.PetPacket;
import tools.packet.SLFCGPacket;

public abstract class AbstractPlayerInteraction {
   protected MapleClient c;
   protected int id;
   protected int id2;

   public AbstractPlayerInteraction(MapleClient c, int id, int id2) {
      this.c = c;
      this.id = id;
      this.id2 = id2;
   }

   public final MapleClient getClient() {
      return this.c;
   }

   public final MapleClient getC() {
      return this.c;
   }

   public MapleCharacter getChar() {
      return this.c.getPlayer();
   }

   public final ChannelServer getChannelServer() {
      return this.c.getChannelServer();
   }

   public final MapleCharacter getPlayer() {
      return this.c.getPlayer();
   }

   public final EventManager getEventManager(String event) {
      return this.c.getChannelServer().getEventSM().getEventManager(event);
   }

   public final EventInstanceManager getEventInstance() {
      return this.c.getPlayer().getEventInstance();
   }

   public final void warp(int map) {
      MapleMap mapz = this.getWarpMap(map);

      try {
         this.c.getPlayer().changeMap(mapz, mapz.getPortal(Randomizer.nextInt(mapz.getPortals().size())));
      } catch (Exception var4) {
         this.c.getPlayer().changeMap(mapz, mapz.getPortal(0));
      }

   }

   public final void dojowarp(int mapid) {
      this.c.getPlayer().cancelTimer();
      this.c.getPlayer().MulungTimer = new Timer();
      this.c.getPlayer().MulungTimerTask = new TimerTask() {
         public void run() {
            AbstractPlayerInteraction.this.c.getPlayer().getClient().getSession().writeAndFlush(CField.getDojoClockStop(false, 900));
            AbstractPlayerInteraction.this.c.getPlayer().setDojoStop(false);
            AbstractPlayerInteraction.this.c.getPlayer().cancelTimer();
         }
      };
      this.c.getPlayer().MulungTimer.schedule(this.c.getPlayer().MulungTimerTask, 30000L);
      this.c.getPlayer().setDojoStop(true);
      this.c.getPlayer().changeMap(this.getWarpMap(mapid), this.getWarpMap(mapid).getPortal(0));
      this.c.getPlayer().setDojoStartTime(0);
      this.c.getPlayer().getClient().getSession().writeAndFlush(CField.getDojoClock(900, 0));
      this.c.getPlayer().getClient().getSession().writeAndFlush(CField.getDojoClockStop(true, 900));
      this.c.getPlayer().getClient().getSession().writeAndFlush(CField.environmentChange("Map/Effect2.img/MuruengTime", 19));
   }

   public final void doJoWarpMap(final int mapid) {
      this.c.getPlayer().cancelTimer();
      this.c.getPlayer().setDojoStop(false);
      int stage = mapid % 92507 / 100;
      this.c.getPlayer().getClient().getSession().writeAndFlush(CField.getDojoClock(900, this.c.getPlayer().getDojoStartTime()));
      this.c.getPlayer().getClient().getSession().writeAndFlush(CField.environmentChange("Dojang/start", 5));
      this.c.getPlayer().getClient().getSession().writeAndFlush(CField.environmentChange("dojang/start/stage", 19));
      this.c.getPlayer().getClient().getSession().writeAndFlush(CField.environmentChange("dojang/start/number/" + stage, 19));
      if (stage - 1 > 1 && this.c.getPlayer().getKeyValue(100466, "Floor") < (long)(stage - 1)) {
         this.c.getPlayer().setKeyValue(100466, "Floor", (stage - 1).makeConcatWithConstants<invokedynamic>(stage - 1));
      }

      this.c.getPlayer().setKeyValue(3, "dojo", String.valueOf(stage - 1));
      this.c.getPlayer().getMap().setCustomInfo(92507, 1, 0);
      int monster = 9305599 + stage;
      int monster2 = 0;
      long bossHp = 0L;
      long bossHp2 = 0L;
      switch(mapid % 92507 / 100) {
      case 1:
         bossHp = 520000000L;
         break;
      case 2:
         bossHp = 574080000L;
         break;
      case 3:
         bossHp = 630720000L;
         break;
      case 4:
         bossHp = 693000000L;
         break;
      case 5:
         bossHp = 754920000L;
         break;
      case 6:
         bossHp = 1234200000L;
         break;
      case 7:
         bossHp = 1392300000L;
         break;
      case 8:
         bossHp = 1510500000L;
         break;
      case 9:
         bossHp = 1684600000L;
         break;
      case 10:
         monster = 9305619;
         bossHp = 10000000000L;
         this.gainItem(4319997, (short)50);
         break;
      case 11:
         bossHp = 4082400000L;
         break;
      case 12:
         monster = 9305617;
         bossHp = 4540450050L;
         break;
      case 13:
         bossHp = 4859325000L;
         break;
      case 14:
         monster = 9305611;
         bossHp = 5535000000L;
         break;
      case 15:
         monster = 9305628;
         bossHp = 6160050000L;
         break;
      case 16:
         monster = 9305682;
         bossHp = 6812100000L;
         break;
      case 17:
         monster = 9305683;
         bossHp = 7884000000L;
         break;
      case 18:
         monster = 9305614;
         bossHp = 9001125000L;
         break;
      case 19:
         monster = 9305620;
         bossHp = 9790200000L;
         break;
      case 20:
         monster = 9305609;
         bossHp = 150000000000L;
         monster2 = 9305641;
         bossHp2 = 500000000L;
         this.gainItem(4319997, (short)50);
         break;
      case 21:
         monster = 9305623;
         bossHp = 13053600000L;
         break;
      case 22:
         monster = 9305625;
         bossHp = 15913800000L;
         break;
      case 23:
         monster = 9305624;
         bossHp = 19035000000L;
         break;
      case 24:
         monster = 9305684;
         bossHp = 24242400000L;
         break;
      case 25:
         monster = 9305658;
         bossHp = 40550400000L;
         break;
      case 26:
         monster = 9305687;
         bossHp = 49704000000L;
         break;
      case 27:
         monster = 9305616;
         bossHp = 59649600000L;
         break;
      case 28:
         monster = 9305690;
         bossHp = 70617600000L;
         break;
      case 29:
         monster = 9305692;
         bossHp = 82425600000L;
         break;
      case 30:
         bossHp = 300000000000L;
         this.gainItem(4319997, (short)50);
         break;
      case 31:
         bossHp = 210824000000L;
         break;
      case 32:
         bossHp = 252652000000L;
         break;
      case 33:
         monster = 9305659;
         bossHp = 297600000000L;
         break;
      case 34:
         bossHp = 346492000000L;
         break;
      case 35:
         monster = 9305621;
         bossHp = 398664000000L;
         break;
      case 36:
         monster = 9305632;
         bossHp = 455100000000L;
         break;
      case 37:
         monster = 9305694;
         bossHp = 514976000000L;
         break;
      case 38:
         monster = 9305003;
         bossHp = 647496000000L;
         break;
      case 39:
         monster = 9305656;
         bossHp = 797184000000L;
         break;
      case 40:
         bossHp = 800000000000L;
         this.gainItem(4319997, (short)50);
         break;
      case 41:
         monster = 9305660;
         bossHp = 4000000000000L;
         break;
      case 42:
         monster = 9305661;
         bossHp = 6400000000000L;
         break;
      case 43:
         monster = 9305627;
         bossHp = 8400000000000L;
         break;
      case 44:
         monster = 9305622;
         bossHp = 10500000000000L;
         break;
      case 45:
         monster = 9305662;
         bossHp = 10500000000000L;
         break;
      case 46:
         monster = 9305635;
         bossHp = 21000000000000L;
         break;
      case 47:
         monster = 9305636;
         bossHp = 31500000000000L;
         break;
      case 48:
         monster = 9305637;
         bossHp = 42000000000000L;
         break;
      case 49:
         monster = 9305638;
         bossHp = 52500000000000L;
         break;
      case 50:
         monster = 9305695;
         bossHp = 52500000000000L;
         this.gainItem(4319997, (short)50);
         break;
      case 51:
         monster = 9305696;
         bossHp = 63000000000000L;
         break;
      case 52:
         monster = 9305663;
         bossHp = 73500000000000L;
         break;
      case 53:
         monster = 9305664;
         bossHp = 84000000000000L;
         break;
      case 54:
         monster = 9305665;
         bossHp = 94500000000000L;
         break;
      case 55:
         monster = 9305666;
         bossHp = 105000000000000L;
         break;
      case 56:
         monster = 9305667;
         bossHp = 115500000000000L;
         break;
      case 57:
         monster = 9305668;
         bossHp = 126000000000000L;
         break;
      case 58:
         monster = 9305669;
         bossHp = 136500000000000L;
         break;
      case 59:
         monster = 9305670;
         bossHp = 147000000000000L;
         break;
      case 60:
         monster = 9305671;
         bossHp = 157500000000000L;
         this.gainItem(4319997, (short)50);
         this.gainItem(4319996, (short)1);
         break;
      case 61:
         monster = 9305697;
         bossHp = 1680000000000000L;
         break;
      case 62:
         monster = 9305698;
         bossHp = 1785000000000000L;
         break;
      case 63:
         monster = 9305699;
         bossHp = 1890000000000000L;
         break;
      case 64:
         monster = 9305700;
         bossHp = 1995000000000000L;
         break;
      case 65:
         monster = 9305701;
         bossHp = 2100000000000000L;
         break;
      case 66:
         monster = 9305657;
         bossHp = 2205000000000000L;
         break;
      case 67:
         monster = 9305702;
         bossHp = 2310000000000000L;
         break;
      case 68:
         monster = 9305703;
         bossHp = 2415000000000000L;
         break;
      case 69:
         monster = 9305704;
         bossHp = 2520000000000000L;
         break;
      case 70:
         monster = 9305705;
         bossHp = 2100000000000000L;
         this.gainItem(4319997, (short)50);
         this.gainItem(4319996, (short)1);
         break;
      case 71:
         monster = 9305706;
         bossHp = 2625000000000000L;
         break;
      case 72:
         monster = 9305707;
         bossHp = 2730000000000000L;
         break;
      case 73:
         monster = 9305708;
         bossHp = 2730000000000000L;
         break;
      case 74:
         monster = 9305672;
         bossHp = 2730000000000000L;
         break;
      case 75:
         monster = 9305673;
         bossHp = 2730000000000000L;
         break;
      case 76:
         monster = 9305674;
         bossHp = 2730000000000000L;
         break;
      case 77:
         monster = 9305675;
         bossHp = 2730000000000000L;
         break;
      case 78:
         monster = 9305676;
         bossHp = 2730000000000000L;
         break;
      case 79:
         monster = 9305677;
         bossHp = 2730000000000000L;
         break;
      case 80:
         monster = 9305640;
         bossHp = 20000000000000000L;
         this.gainItem(4319997, (short)50);
         this.gainItem(4319996, (short)2);
         break;
      case 81:
         monster = 9305709;
         bossHp = 200000000000000000L;
         break;
      case 82:
         monster = 9305710;
         bossHp = 200000000000000000L;
         break;
      case 83:
         monster = 9305711;
         bossHp = 200000000000000000L;
         break;
      case 84:
         monster = 9305712;
         bossHp = 200000000000000000L;
         break;
      case 85:
         monster = 9305713;
         bossHp = 200000000000000000L;
         break;
      case 86:
         monster = 9305714;
         bossHp = 200000000000000000L;
         break;
      case 87:
         monster = 9305715;
         bossHp = 200000000000000000L;
         break;
      case 88:
         monster = 9305716;
         bossHp = 200000000000000000L;
         break;
      case 89:
         monster = 9305717;
         bossHp = 200000000000000000L;
         break;
      case 90:
         monster = 9305718;
         bossHp = 200000000000000000L;
         this.gainItem(4319997, (short)50);
         this.gainItem(4319996, (short)2);
         break;
      case 91:
         monster = 9305719;
         bossHp = 9000000000000000000L;
         break;
      case 92:
         monster = 9305720;
         bossHp = 9000000000000000000L;
         break;
      case 93:
         monster = 9305721;
         bossHp = 9000000000000000000L;
         break;
      case 94:
         monster = 9305722;
         bossHp = 900000000000000000L;
         break;
      case 95:
         monster = 9305723;
         bossHp = 900000000000000000L;
         break;
      case 96:
         monster = 9305724;
         bossHp = 900000000000000000L;
         break;
      case 97:
         monster = 9305725;
         bossHp = 900000000000000000L;
         break;
      case 98:
         monster = 9305726;
         bossHp = 900000000000000000L;
         break;
      case 99:
         monster = 9305727;
         bossHp = 900000000000000000L;
         this.gainItem(4319997, (short)50);
         this.gainItem(4319996, (short)3);
      }

      if (monster > 0) {
         this.c.getPlayer().setSkillCustomInfo(9920544, (long)monster, 0L);
         this.c.getPlayer().setSkillCustomInfo(9920546, bossHp, 0L);
      } else {
         this.c.getPlayer().removeSkillCustomInfo(9920544);
         this.c.getPlayer().removeSkillCustomInfo(9920546);
      }

      if (monster2 > 0) {
         this.c.getPlayer().setSkillCustomInfo(9920545, (long)monster2, 0L);
         this.c.getPlayer().setSkillCustomInfo(9920547, bossHp, 0L);
      } else {
         this.c.getPlayer().removeSkillCustomInfo(9920545);
         this.c.getPlayer().removeSkillCustomInfo(9920547);
      }

      this.c.getPlayer().MulungTimer = new Timer();
      this.c.getPlayer().MulungTimerTask = new TimerTask() {
         public void run() {
            if (AbstractPlayerInteraction.this.c.getPlayer().getMap().getAllMonstersThreadsafe().size() <= 0 && AbstractPlayerInteraction.this.c.getPlayer().getMap().getId() / 10000 == 92507) {
               AbstractPlayerInteraction.this.c.getPlayer().getMap().removeCustomInfo(92507);
               MapleMonster mob = null;
               mob = MapleLifeFactory.getMonster((int)AbstractPlayerInteraction.this.c.getPlayer().getSkillCustomValue0(9920544));
               mob.setHp(AbstractPlayerInteraction.this.c.getPlayer().getSkillCustomValue0(9920546));
               mob.getStats().setHp(AbstractPlayerInteraction.this.c.getPlayer().getSkillCustomValue0(9920546));
               AbstractPlayerInteraction.this.c.getPlayer().getMap().spawnMonsterOnGroundBelow(mob, new Point(Randomizer.rand(-365, 365), 7));
               if (mapid % 92507 / 100 == 9 || mapid % 92507 / 100 == 15 || mapid % 92507 / 100 == 56 || mapid % 92507 / 100 == 58) {
                  AbstractPlayerInteraction.this.c.getPlayer().getMap().broadcastMessage(MobPacket.enableMulug1(mob, 8));
                  AbstractPlayerInteraction.this.c.getPlayer().getMap().broadcastMessage(MobPacket.enableMulug(mob, 2));
               }

               if (AbstractPlayerInteraction.this.c.getPlayer().getSkillCustomValue0(9920542) > 0L) {
                  for(int i = 0; i < 5; ++i) {
                     MapleMonster mob2 = null;
                     mob2 = MapleLifeFactory.getMonster((int)AbstractPlayerInteraction.this.c.getPlayer().getSkillCustomValue0(9920545));
                     mob2.setHp(AbstractPlayerInteraction.this.c.getPlayer().getSkillCustomValue0(9920547));
                     mob2.getStats().setHp(AbstractPlayerInteraction.this.c.getPlayer().getSkillCustomValue0(9920547));
                     AbstractPlayerInteraction.this.c.getPlayer().getMap().spawnMonsterOnGroundBelow(mob2, new Point(Randomizer.rand(-365, 365), 7));
                  }
               }

               AbstractPlayerInteraction.this.c.getPlayer().cancelTimer();
            }

         }
      };
      this.c.getPlayer().MulungTimer.schedule(this.c.getPlayer().MulungTimerTask, 4000L);
   }

   public void spawnGolrux(long hp, int mobid, int mapid, int x, int y) {
      try {
         MapleMap mapz = this.getWarpMap(mapid);
         mapz.resetFully();
         MapleMonster mob = MapleLifeFactory.getMonster(mobid);
         mob.setHp(hp);
         mob.getStats().setHp(hp);
         mapz.spawnMonsterOnGroundBelow(mob, new Point(x, y));
      } catch (Exception var9) {
         var9.printStackTrace();
      }

   }

   public final void warp_Instanced(int map) {
      MapleMap mapz = this.getMap_Instanced(map);

      try {
         this.c.getPlayer().changeMap(mapz, mapz.getPortal(Randomizer.nextInt(mapz.getPortals().size())));
      } catch (Exception var4) {
         this.c.getPlayer().changeMap(mapz, mapz.getPortal(0));
      }

   }

   public final void warp(int map, int portal) {
      MapleMap mapz = this.getWarpMap(map);
      if (portal != 0 && map == this.c.getPlayer().getMapId()) {
         Point portalPos = new Point(this.c.getPlayer().getMap().getPortal(portal).getPosition());
         if (portalPos.distanceSq(this.getPlayer().getTruePosition()) < 90000.0D) {
            this.c.getSession().writeAndFlush(CField.instantMapWarp(this.c.getPlayer(), (byte)portal));
            this.c.getPlayer().checkFollow();
            this.c.getPlayer().getMap().movePlayer(this.c.getPlayer(), portalPos);
         } else {
            this.c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
         }
      } else {
         this.c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
      }

   }

   public final void warpS(int map, int portal) {
      MapleMap mapz = this.getWarpMap(map);
      this.c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
   }

   public final void setDamageSkin(int itemid) {
      MapleQuest quest = MapleQuest.getInstance(7291);
      MapleQuestStatus queststatus = new MapleQuestStatus(quest, 1);
      int skinnum = GameConstants.getDSkinNum(itemid);
      String skinString = String.valueOf(skinnum);
      queststatus.setCustomData(skinString == null ? "0" : skinString);
      this.c.getPlayer().updateQuest(queststatus, true);
      this.c.getPlayer().setKeyValue(7293, "damage_skin", String.valueOf(itemid));
      this.c.getPlayer().dropMessage(5, "데미지 스킨이 변경되었습니다.");
      this.c.getPlayer().getMap().broadcastMessage(this.c.getPlayer(), CField.showForeignDamageSkin(this.c.getPlayer(), skinnum), false);
      this.c.getPlayer().updateDamageSkin();
   }

   public final void warp(int map, String portal) {
      MapleMap mapz = this.getWarpMap(map);
      if (map == 109060000 || map == 109060002 || map == 109060004) {
         portal = mapz.getSnowballPortal();
      }

      if (map == this.c.getPlayer().getMapId()) {
         Point portalPos = new Point(this.c.getPlayer().getMap().getPortal(portal).getPosition());
         if (portalPos.distanceSq(this.getPlayer().getTruePosition()) < 90000.0D) {
            this.c.getPlayer().checkFollow();
            this.c.getSession().writeAndFlush(CField.instantMapWarp(this.c.getPlayer(), (byte)this.c.getPlayer().getMap().getPortal(portal).getId()));
            this.c.getPlayer().getMap().movePlayer(this.c.getPlayer(), new Point(this.c.getPlayer().getMap().getPortal(portal).getPosition()));
         } else {
            this.c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
         }
      } else {
         this.c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
      }

   }

   public final void warpS(int map, String portal) {
      MapleMap mapz = this.getWarpMap(map);
      if (map == 109060000 || map == 109060002 || map == 109060004) {
         portal = mapz.getSnowballPortal();
      }

      this.c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
   }

   public final void warpMap(int mapid, int portal) {
      MapleMap map = this.getMap(mapid);
      Iterator var4 = this.c.getPlayer().getMap().getCharactersThreadsafe().iterator();

      while(var4.hasNext()) {
         MapleCharacter chr = (MapleCharacter)var4.next();
         chr.changeMap(map, map.getPortal(portal));
      }

   }

   public final void playPortalSE() {
      this.c.getSession().writeAndFlush(CField.EffectPacket.showPortalEffect(0));
   }

   private final MapleMap getWarpMap(int map) {
      return ChannelServer.getInstance(this.c.getChannel()).getMapFactory().getMap(map);
   }

   public final MapleMap getMap() {
      return this.c.getPlayer().getMap();
   }

   public final MapleMap getMap(int map) {
      return this.getWarpMap(map);
   }

   public final MapleMap getMap_Instanced(int map) {
      return this.c.getPlayer().getEventInstance() == null ? this.getMap(map) : this.c.getPlayer().getEventInstance().getMapInstance(map);
   }

   public void spawnMonster(int id, int qty) {
      this.spawnMob(id, qty, this.c.getPlayer().getTruePosition(), 1);
   }

   public final void spawnMobOnMap(int id, int qty, int x, int y, int map) {
      for(int i = 0; i < qty; ++i) {
         this.getMap(map).spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), new Point(x, y));
      }

   }

   public final void spawnMob(int id, int x, int y, int effect, boolean show) {
      MapleMonster m = MapleLifeFactory.getMonster(id);
      m.setHp(m.getStats().getHp());
      m.getStats().setHp(m.getStats().getHp());
      this.c.getPlayer().getMap().spawnMonsterWithEffect(m, effect, new Point(x, y));
   }

   public final void spawnMob(int id, int qty, int x, int y) {
      this.spawnMob(id, qty, new Point(x, y), 1);
   }

   public final void spawnMob(int id, int x, int y) {
      this.spawnMob(id, 1, new Point(x, y), 1);
   }

   public final void spawnMobIncrease(int id, int x, int y, int hp) {
      this.spawnMob(id, 1, new Point(x, y), hp);
   }

   public final void spawnLinkMob(int id, int x, int y) {
      MapleMonster m = MapleLifeFactory.getMonster(id);
      m.setHp(m.getStats().getHp());
      m.getStats().setHp(m.getStats().getHp());
      m.setOwner(this.c.getPlayer().getId());
      this.c.getPlayer().getMap().spawnMonsterOnGroundBelow(m, new Point(x, y));
   }

   private final void spawnMob(int id, int qty, Point pos, int hp) {
      for(int i = 0; i < qty; ++i) {
         MapleMonster m = MapleLifeFactory.getMonster(id);
         m.setHp(m.getStats().getHp() * (long)hp);
         m.getStats().setHp(m.getStats().getHp() * (long)hp);
         this.c.getPlayer().getMap().spawnMonsterOnGroundBelow(m, pos);
      }

   }

   public final void killMob(int ids) {
      this.c.getPlayer().getMap().killMonster(ids);
   }

   public final void killAllMob() {
      this.c.getPlayer().getMap().killAllMonsters(true);
   }

   public final void addHP(int delta) {
      this.c.getPlayer().addHP((long)delta);
   }

   public final int getPlayerStat(String type) {
      if (type.equals("LVL")) {
         return this.c.getPlayer().getLevel();
      } else if (type.equals("STR")) {
         return this.c.getPlayer().getStat().getStr();
      } else if (type.equals("DEX")) {
         return this.c.getPlayer().getStat().getDex();
      } else if (type.equals("INT")) {
         return this.c.getPlayer().getStat().getInt();
      } else if (type.equals("LUK")) {
         return this.c.getPlayer().getStat().getLuk();
      } else if (type.equals("HP")) {
         return (int)this.c.getPlayer().getStat().getHp();
      } else if (type.equals("MP")) {
         return (int)this.c.getPlayer().getStat().getMp();
      } else if (type.equals("MAXHP")) {
         return (int)this.c.getPlayer().getStat().getMaxHp();
      } else if (type.equals("MAXMP")) {
         return (int)this.c.getPlayer().getStat().getMaxMp();
      } else if (type.equals("RAP")) {
         return this.c.getPlayer().getRemainingAp();
      } else if (type.equals("RSP")) {
         return this.c.getPlayer().getRemainingSp();
      } else if (type.equals("GID")) {
         return this.c.getPlayer().getGuildId();
      } else if (type.equals("GRANK")) {
         return this.c.getPlayer().getGuildRank();
      } else if (type.equals("ARANK")) {
         return this.c.getPlayer().getAllianceRank();
      } else if (type.equals("GM")) {
         return this.c.getPlayer().isGM() ? 1 : 0;
      } else if (type.equals("ADMIN")) {
         return this.c.getPlayer().isAdmin() ? 1 : 0;
      } else if (type.equals("GENDER")) {
         return this.c.getPlayer().getGender();
      } else if (type.equals("FACE")) {
         return this.c.getPlayer().getFace();
      } else if (type.equals("HAIR")) {
         return this.c.getPlayer().getHair();
      } else {
         return type.equals("SECONDHAIR") ? this.c.getPlayer().getSecondHair() : -1;
      }
   }

   public final boolean isAngelicBuster() {
      return GameConstants.isAngelicBuster(this.c.getPlayer().getJob());
   }

   public final boolean isZero() {
      return GameConstants.isZero(this.c.getPlayer().getJob());
   }

   public final String getName() {
      return this.c.getPlayer().getName();
   }

   public final boolean haveItem(int itemid) {
      return this.haveItem(itemid, 1);
   }

   public final boolean haveItem(int itemid, int quantity) {
      return this.haveItem(itemid, quantity, false, true);
   }

   public final boolean haveItem(int itemid, int quantity, boolean checkEquipped, boolean greaterOrEquals) {
      return this.c.getPlayer().haveItem(itemid, quantity, checkEquipped, greaterOrEquals);
   }

   public final boolean canHold() {
      for(int i = 1; i <= 5; ++i) {
         if (this.c.getPlayer().getInventory(MapleInventoryType.getByType((byte)i)).getNextFreeSlot() <= -1) {
            return false;
         }
      }

      return true;
   }

   public final boolean canHoldSlots(int slot) {
      for(int i = 1; i <= 5; ++i) {
         if (this.c.getPlayer().getInventory(MapleInventoryType.getByType((byte)i)).isFull(slot)) {
            return false;
         }
      }

      return true;
   }

   public final boolean canHold(int itemid) {
      return this.c.getPlayer().getInventory(GameConstants.getInventoryType(itemid)).getNextFreeSlot() > -1;
   }

   public final boolean canHold(int itemid, int quantity) {
      return MapleInventoryManipulator.checkSpace(this.c, itemid, quantity, "");
   }

   public final MapleQuestStatus getQuestRecord(int id) {
      return this.c.getPlayer().getQuestNAdd(MapleQuest.getInstance(id));
   }

   public final MapleQuestStatus getQuestNoRecord(int id) {
      return this.c.getPlayer().getQuestNoAdd(MapleQuest.getInstance(id));
   }

   public final byte getQuestStatus(int id) {
      return this.c.getPlayer().getQuestStatus(id);
   }

   public final boolean isQuestActive(int id) {
      return this.getQuestStatus(id) == 1;
   }

   public final boolean isQuestFinished(int id) {
      return this.getQuestStatus(id) == 2;
   }

   public final void showQuestMsg(String msg) {
      this.c.getSession().writeAndFlush(CWvsContext.showQuestMsg("", msg));
   }

   public final void forceStartQuest(int id, String data) {
      MapleQuest.getInstance(id).forceStart(this.c.getPlayer(), 0, data);
   }

   public final void forceStartQuest(int id, int data, boolean filler) {
      MapleQuest.getInstance(id).forceStart(this.c.getPlayer(), 0, filler ? String.valueOf(data) : null);
   }

   public void forceStartQuest(int id) {
      MapleQuest.getInstance(id).forceStart(this.c.getPlayer(), 0, (String)null);
   }

   public void forceCompleteQuest(int id) {
      MapleQuest.getInstance(id).forceComplete(this.getPlayer(), 0);
   }

   public void forceStartQuest(int id, boolean acc) {
      MapleQuest.getInstance(id).forceStart(this.c.getPlayer(), 0, (String)null, acc);
   }

   public void forceCompleteQuest(int id, boolean acc) {
      MapleQuest.getInstance(id).forceComplete(this.getPlayer(), 0, acc);
   }

   public final byte getQuestStatus(int id, boolean acc) {
      return acc ? this.c.getQuestStatus(id) : this.c.getPlayer().getQuestStatus(id);
   }

   public void spawnNpc(int npcId) {
      this.c.getPlayer().getMap().spawnNpc(npcId, this.c.getPlayer().getPosition());
   }

   public final void spawnNpc(int npcId, int x, int y) {
      this.c.getPlayer().getMap().spawnNpc(npcId, new Point(x, y));
   }

   public final void spawnNpc(int npcId, Point pos) {
      this.c.getPlayer().getMap().spawnNpc(npcId, pos);
   }

   public final int spawnNpc2(int npcId, Point pos) {
      MapleNPC npc = MapleLifeFactory.getNPC(npcId);
      npc.setPosition(pos);
      npc.setCy(pos.y);
      npc.setRx0(pos.x);
      npc.setRx1(pos.x);
      npc.setFh(this.c.getPlayer().getMap().getFootholds().findBelow(pos).getId());
      npc.setCustom(true);
      if (npcId == 9062342 || npcId == 9062474 || npcId == 9062454 || npcId == 9072305 || npcId == 9072306 || npcId == 9072302 || npcId == 9072303) {
         npc.setLeft(true);
      }

      npc.setOwner(this.c.getPlayer());
      this.c.getPlayer().getMap().addMapObject(npc);
      this.c.getSession().writeAndFlush(CField.NPCPacket.spawnNPCRequestController(npc, true));
      return npc.getObjectId();
   }

   public final int spawnNpc2(int npcId, Point pos, boolean left) {
      MapleNPC npc = MapleLifeFactory.getNPC(npcId);
      npc.setPosition(pos);
      npc.setCy(pos.y);
      npc.setRx0(pos.x);
      npc.setRx1(pos.x);
      npc.setFh(this.c.getPlayer().getMap().getFootholds().findBelow(pos).getId());
      npc.setCustom(true);
      npc.setLeft(left);
      npc.setOwner(this.c.getPlayer());
      this.c.getPlayer().getMap().addMapObject(npc);
      this.c.getSession().writeAndFlush(CField.NPCPacket.spawnNPCRequestController(npc, true));
      return npc.getObjectId();
   }

   public final void AllInvisibleName(boolean show) {
      Iterator var2 = this.getMap().getAllNPCs().iterator();

      while(var2.hasNext()) {
         MapleNPC npc = (MapleNPC)var2.next();
         if (npc != null) {
            this.c.send(CField.NPCPacket.setNpcNameInvisible(npc.getId(), show));
         }
      }

   }

   public void NpcAction(int oid, String txt) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.NPC_ACTION.getValue());
      mplew.writeInt(oid);
      mplew.write(HexTool.getByteArrayFromHexString(txt));
      this.c.getSession().writeAndFlush(mplew.getPacket());
   }

   public final void removeNpc(int mapid, int npcId) {
      this.c.getChannelServer().getMapFactory().getMap(mapid).removeNpc(npcId);
   }

   public final void removeNpc(int npcId) {
      int objctid = 0;
      Iterator itr = this.c.getPlayer().getMap().getAllNPCsThreadsafe().iterator();

      MapleNPC npc;
      while(itr.hasNext()) {
         npc = (MapleNPC)itr.next();
         if (npc.getOwner() != null && npc.getOwner().getId() == this.c.getPlayer().getId() && npcId == npc.getId()) {
            objctid = npc.getObjectId();
            break;
         }
      }

      itr = this.c.getPlayer().getMap().getAllNPCsThreadsafe().iterator();

      while(itr.hasNext()) {
         npc = (MapleNPC)itr.next();
         if (npc.getObjectId() == objctid) {
            this.c.getPlayer().getMap().broadcastMessage(CField.NPCPacket.removeNPCController(npc.getObjectId()));
            this.c.getPlayer().getMap().broadcastMessage(CField.NPCPacket.removeNPC(npc.getObjectId()));
            itr.remove();
            break;
         }
      }

      if (objctid == 0) {
         this.c.getPlayer().getMap().removeNpc(npcId);
      }

   }

   public final void forceStartReactor(int mapid, int id) {
      MapleMap map = this.c.getChannelServer().getMapFactory().getMap(mapid);
      Iterator var4 = map.getAllReactorsThreadsafe().iterator();

      while(var4.hasNext()) {
         MapleMapObject remo = (MapleMapObject)var4.next();
         MapleReactor react = (MapleReactor)remo;
         if (react.getReactorId() == id) {
            react.forceStartReactor(this.c);
            break;
         }
      }

   }

   public final void destroyReactor(int mapid, int id) {
      MapleMap map = this.c.getChannelServer().getMapFactory().getMap(mapid);
      Iterator var4 = map.getAllReactorsThreadsafe().iterator();

      while(var4.hasNext()) {
         MapleMapObject remo = (MapleMapObject)var4.next();
         MapleReactor react = (MapleReactor)remo;
         if (react.getReactorId() == id) {
            react.hitReactor(this.c);
            break;
         }
      }

   }

   public final void hitReactor(int mapid, int id) {
      MapleMap map = this.c.getChannelServer().getMapFactory().getMap(mapid);
      Iterator var4 = map.getAllReactorsThreadsafe().iterator();

      while(var4.hasNext()) {
         MapleMapObject remo = (MapleMapObject)var4.next();
         MapleReactor react = (MapleReactor)remo;
         if (react.getReactorId() == id) {
            react.hitReactor(this.c);
            break;
         }
      }

   }

   public final int getJob() {
      return this.c.getPlayer().getJob();
   }

   public final void gainNX(int amount) {
      this.c.getPlayer().modifyCSPoints(1, amount, true);
   }

   public final void gainItemPeriod(int id, short quantity, int period) {
      this.gainItem(id, quantity, false, (long)period, -1, "");
   }

   public final void gainItemPeriod(int id, short quantity, long period, String owner) {
      this.gainItem(id, quantity, false, period, -1, owner);
   }

   public final void gainItem(int id, short quantity) {
      if (Math.floor((double)(id / 1000000)) == 1.0D) {
         for(int i = 0; i < quantity; ++i) {
            this.gainItem(id, (short)1, false, 0L, -1, "");
         }
      } else {
         this.gainItem(id, quantity, false, 0L, -1, "");
      }

   }

   public final void gainItemSilent(int id, short quantity) {
      this.gainItem(id, quantity, false, 0L, -1, "", this.c, false);
   }

   public final void gainItem(int id, short quantity, boolean randomStats) {
      this.gainItem(id, quantity, randomStats, 0L, -1, "");
   }

   public final void gainItem(int id, short quantity, boolean randomStats, int slots) {
      this.gainItem(id, quantity, randomStats, 0L, slots, "");
   }

   public final void gainItem(int id, short quantity, long period) {
      this.gainItem(id, quantity, false, period, -1, "");
   }

   public final void gainItem(int id, short quantity, boolean randomStats, long period, int slots) {
      this.gainItem(id, quantity, randomStats, period, slots, "");
   }

   public final void gainItem(int id, short quantity, boolean randomStats, long period, int slots, String owner) {
      this.gainItem(id, quantity, randomStats, period, slots, owner, this.c);
   }

   public final void gainItem(int id, short quantity, boolean randomStats, long period, int slots, String owner, MapleClient cg) {
      this.gainItem(id, quantity, randomStats, period, slots, owner, cg, true);
   }

   public final void gainItem(int id, short quantity, boolean randomStats, long period, int slots, String owner, MapleClient cg, boolean show) {
      if (quantity >= 0) {
         MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
         MapleInventoryType type = GameConstants.getInventoryType(id);
         if (!MapleInventoryManipulator.checkSpace(cg, id, quantity, "")) {
            return;
         }

         if (type.equals(MapleInventoryType.EQUIP) && !GameConstants.isThrowingStar(id) && !GameConstants.isBullet(id)) {
            Equip item = (Equip)ii.getEquipById(id);
            if (period > 0L) {
               item.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
            }

            if (slots > 0) {
               item.setUpgradeSlots((byte)(item.getUpgradeSlots() + slots));
            }

            if (owner != null) {
               item.setOwner(owner);
            }

            if (ii.isCash(id)) {
               item.setUniqueId(MapleInventoryIdentifier.getInstance());
            }

            if (id == 1142249) {
               item.setEnhance((byte)20);
            }

            if (id == 1142374) {
               item.setEnhance((byte)30);
            }

            int[] pcAb = new int[]{1212121, 1213028, 1222114, 1232114, 1242123, 1242124, 1262040, 1302344, 1312204, 1322256, 1332280, 1342105, 1362141, 1372229, 1382266, 1402260, 1412182, 1422190, 1432219, 1442276, 1452258, 1462244, 1472266, 1482222, 1492236, 1522144, 1532148, 1582027, 1272021, 1282022, 1592028, 1292028};
            int[] pcArc = new int[]{1212131, 1213030, 1222124, 1232124, 1242123, 1242144, 1262053, 1302359, 1312215, 1322266, 1332291, 1342110, 1362151, 1372239, 1382276, 1402271, 1412191, 1422199, 1432229, 1442287, 1452269, 1462254, 1472277, 1482234, 1492247, 1522154, 1532159, 1582046, 1272043, 1282043, 1592037, 1292030};
            int[] var15 = pcAb;
            int var16 = pcAb.length;

            int var17;
            int pcAr;
            StarForceStats statz;
            Iterator var20;
            Pair stat;
            for(var17 = 0; var17 < var16; ++var17) {
               pcAr = var15[var17];
               if (pcAr == id) {
                  item.setPotential1(60056);
                  item.setPotential2(60057);
                  item.setPotential3(60058);

                  while(item.getEnhance() < 17) {
                     statz = EquipmentEnchant.starForceStats(item);
                     item.setEnhance((byte)(item.getEnhance() + 1));
                     var20 = statz.getStats().iterator();

                     while(var20.hasNext()) {
                        stat = (Pair)var20.next();
                        if (EnchantFlag.Watk.check(((EnchantFlag)stat.left).getValue())) {
                           item.setWatk((short)(item.getWatk() + (Integer)stat.right));
                        }

                        if (EnchantFlag.Matk.check(((EnchantFlag)stat.left).getValue())) {
                           item.setMatk((short)(item.getMatk() + (Integer)stat.right));
                        }

                        if (EnchantFlag.Str.check(((EnchantFlag)stat.left).getValue())) {
                           item.setStr((short)(item.getStr() + (Integer)stat.right));
                        }

                        if (EnchantFlag.Dex.check(((EnchantFlag)stat.left).getValue())) {
                           item.setDex((short)(item.getDex() + (Integer)stat.right));
                        }

                        if (EnchantFlag.Int.check(((EnchantFlag)stat.left).getValue())) {
                           item.setInt((short)(item.getInt() + (Integer)stat.right));
                        }

                        if (EnchantFlag.Luk.check(((EnchantFlag)stat.left).getValue())) {
                           item.setLuk((short)(item.getLuk() + (Integer)stat.right));
                        }

                        if (EnchantFlag.Wdef.check(((EnchantFlag)stat.left).getValue())) {
                           item.setWdef((short)(item.getWdef() + (Integer)stat.right));
                        }

                        if (EnchantFlag.Mdef.check(((EnchantFlag)stat.left).getValue())) {
                           item.setMdef((short)(item.getMdef() + (Integer)stat.right));
                        }

                        if (EnchantFlag.Hp.check(((EnchantFlag)stat.left).getValue())) {
                           item.setHp((short)(item.getHp() + (Integer)stat.right));
                        }

                        if (EnchantFlag.Mp.check(((EnchantFlag)stat.left).getValue())) {
                           item.setMp((short)(item.getMp() + (Integer)stat.right));
                        }

                        if (EnchantFlag.Acc.check(((EnchantFlag)stat.left).getValue())) {
                           item.setAcc((short)(item.getAcc() + (Integer)stat.right));
                        }

                        if (EnchantFlag.Avoid.check(((EnchantFlag)stat.left).getValue())) {
                           item.setAvoid((short)(item.getAvoid() + (Integer)stat.right));
                        }
                     }
                  }
               }
            }

            var15 = pcArc;
            var16 = pcArc.length;

            for(var17 = 0; var17 < var16; ++var17) {
               pcAr = var15[var17];
               if (pcAr == id) {
                  item.setPotential1(60057);
                  item.setPotential2(60085);
                  item.setPotential3(60086);

                  while(item.getEnhance() < 15) {
                     statz = EquipmentEnchant.starForceStats(item);
                     item.setEnhance((byte)(item.getEnhance() + 1));
                     var20 = statz.getStats().iterator();

                     while(var20.hasNext()) {
                        stat = (Pair)var20.next();
                        if (EnchantFlag.Watk.check(((EnchantFlag)stat.left).getValue())) {
                           item.setWatk((short)(item.getWatk() + (Integer)stat.right));
                        }

                        if (EnchantFlag.Matk.check(((EnchantFlag)stat.left).getValue())) {
                           item.setMatk((short)(item.getMatk() + (Integer)stat.right));
                        }

                        if (EnchantFlag.Str.check(((EnchantFlag)stat.left).getValue())) {
                           item.setStr((short)(item.getStr() + (Integer)stat.right));
                        }

                        if (EnchantFlag.Dex.check(((EnchantFlag)stat.left).getValue())) {
                           item.setDex((short)(item.getDex() + (Integer)stat.right));
                        }

                        if (EnchantFlag.Int.check(((EnchantFlag)stat.left).getValue())) {
                           item.setInt((short)(item.getInt() + (Integer)stat.right));
                        }

                        if (EnchantFlag.Luk.check(((EnchantFlag)stat.left).getValue())) {
                           item.setLuk((short)(item.getLuk() + (Integer)stat.right));
                        }

                        if (EnchantFlag.Wdef.check(((EnchantFlag)stat.left).getValue())) {
                           item.setWdef((short)(item.getWdef() + (Integer)stat.right));
                        }

                        if (EnchantFlag.Mdef.check(((EnchantFlag)stat.left).getValue())) {
                           item.setMdef((short)(item.getMdef() + (Integer)stat.right));
                        }

                        if (EnchantFlag.Hp.check(((EnchantFlag)stat.left).getValue())) {
                           item.setHp((short)(item.getHp() + (Integer)stat.right));
                        }

                        if (EnchantFlag.Mp.check(((EnchantFlag)stat.left).getValue())) {
                           item.setMp((short)(item.getMp() + (Integer)stat.right));
                        }

                        if (EnchantFlag.Acc.check(((EnchantFlag)stat.left).getValue())) {
                           item.setAcc((short)(item.getAcc() + (Integer)stat.right));
                        }

                        if (EnchantFlag.Avoid.check(((EnchantFlag)stat.left).getValue())) {
                           item.setAvoid((short)(item.getAvoid() + (Integer)stat.right));
                        }
                     }
                  }
               }
            }

            String var10001 = StringUtil.getAllCurrentTime();
            item.setGMLog(var10001 + "에 (" + this.id + " / " + this.id2 + ")로부터 gainItem으로 얻은 아이템");
            String name = ii.getName(id);
            if (id / 10000 == 114 && name != null && name.length() > 0) {
            }

            MapleInventoryManipulator.addbyItem(cg, item.copy());
         } else {
            MapleInventoryManipulator.addById(cg, id, quantity, owner == null ? "" : owner, (MaplePet)null, period, "(" + this.id + " / " + this.id2 + ")로부터 gainItem으로 얻은 아이템");
         }
      } else {
         this.c.getPlayer().removeItem(id, quantity);
      }

      if (show && quantity > 0) {
         cg.getSession().writeAndFlush(CWvsContext.InfoPacket.getShowItemGain(id, quantity, true));
      }

   }

   public final boolean removeItem(int id) {
      if (MapleInventoryManipulator.removeById_Lock(this.c, GameConstants.getInventoryType(id), id)) {
         this.c.getSession().writeAndFlush(CField.EffectPacket.showCharmEffect(this.c.getPlayer(), id, 1, true, ""));
         return true;
      } else {
         return false;
      }
   }

   public final void changeMusic(String songName) {
      this.getPlayer().getMap().broadcastMessage(CField.musicChange(songName));
   }

   public final void changeMusic(boolean cast, String songName) {
      if (cast) {
         this.getPlayer().getMap().broadcastMessage(CField.musicChange(songName));
      } else {
         this.getPlayer().getClient().getSession().writeAndFlush(CField.musicChange(songName));
      }

   }

   public final void worldMessage(int type, String message) {
      World.Broadcast.broadcastMessage(CWvsContext.serverNotice(type, "", message));
   }

   public final void playerMessage(String message) {
      this.playerMessage(5, message);
   }

   public final void mapMessage(String message) {
      this.mapMessage(5, message);
   }

   public final void guildMessage(String message) {
      this.guildMessage(5, message);
   }

   public final void playerMessage(int type, String message) {
      this.c.getPlayer().dropMessage(type, message);
   }

   public final void mapMessage(int type, String message) {
      this.c.getPlayer().getMap().broadcastMessage(CWvsContext.serverNotice(type, "", message));
   }

   public final void guildMessage(int type, String message) {
      if (this.getPlayer().getGuildId() > 0) {
         World.Guild.guildPacket(this.getPlayer().getGuildId(), CWvsContext.serverNotice(type, "", message));
      }

   }

   public final MapleGuild getGuild() {
      return this.getGuild(this.getPlayer().getGuildId());
   }

   public final MapleGuild getGuild(int guildid) {
      return World.Guild.getGuild(guildid);
   }

   public final MapleGuildAlliance getAlliance(int allianceId) {
      return World.Alliance.getAlliance(allianceId);
   }

   public void setNewAlliance(int gid, int x) {
      World.Alliance.setNewAlliance(gid, x);
   }

   public final MapleParty getParty() {
      return this.c.getPlayer().getParty();
   }

   public final int getCurrentPartyId(int mapid) {
      return this.getMap(mapid).getCurrentPartyId();
   }

   public final boolean isLeader() {
      if (this.getPlayer().getParty() == null) {
         return false;
      } else {
         return this.getParty().getLeader().getId() == this.c.getPlayer().getId();
      }
   }

   public final boolean isAllPartyMembersAllowedJob(int job) {
      if (this.c.getPlayer().getParty() == null) {
         return false;
      } else {
         Iterator var2 = this.c.getPlayer().getParty().getMembers().iterator();

         MaplePartyCharacter mem;
         do {
            if (!var2.hasNext()) {
               return true;
            }

            mem = (MaplePartyCharacter)var2.next();
         } while(mem.getJobId() / 100 == job);

         return false;
      }
   }

   public final boolean allMembersHere() {
      if (this.c.getPlayer().getParty() == null) {
         return false;
      } else {
         Iterator var1 = this.c.getPlayer().getParty().getMembers().iterator();

         MapleCharacter chr;
         do {
            if (!var1.hasNext()) {
               return true;
            }

            MaplePartyCharacter mem = (MaplePartyCharacter)var1.next();
            chr = this.c.getPlayer().getMap().getCharacterById(mem.getId());
         } while(chr != null);

         return false;
      }
   }

   public final void setBossRaid(byte count) {
      Iterator var2 = this.c.getPlayer().getParty().getMembers().iterator();

      while(var2.hasNext()) {
         MaplePartyCharacter mem = (MaplePartyCharacter)var2.next();
         MapleCharacter chr = this.getChannelServer().getPlayerStorage().getCharacterById(mem.getId());
         chr.getClient().getSession().writeAndFlush(CField.UIPacket.closeUI(62));
         chr.setDeathCount(count);
         chr.getClient().getSession().writeAndFlush(CField.getDeathCount(count));
      }

   }

   public final long getKeyValue(int id, String key) {
      return this.c.getPlayer().getKeyValue(id, key);
   }

   public final void setKeyValue(int id, String key, String value) {
      this.c.getPlayer().setKeyValue(id, key, value);
   }

   public final void removeKeyValue(int id, String key) {
      this.c.getPlayer().removeKeyValue(id, key);
   }

   public final void warpParty(int mapId) {
      if (this.getPlayer().getParty() != null && this.getPlayer().getParty().getMembers().size() != 1) {
         MapleMap target = this.getMap(mapId);
         int cMap = this.getPlayer().getMapId();
         Iterator var4 = this.getPlayer().getParty().getMembers().iterator();

         while(true) {
            MapleCharacter curChar;
            do {
               do {
                  do {
                     if (!var4.hasNext()) {
                        return;
                     }

                     MaplePartyCharacter chr = (MaplePartyCharacter)var4.next();
                     curChar = this.getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
                  } while(curChar == null);
               } while(curChar.getMapId() != cMap && curChar.getEventInstance() != this.getPlayer().getEventInstance());

               curChar.changeMap(target, target.getPortal(0));
               if (curChar.getMapId() >= 105200310 && curChar.getMapId() <= 105200319 || curChar.getMapId() >= 105200710 && curChar.getMapId() <= 105200719) {
                  curChar.getClient().send(CField.startMapEffect("잠든 블러디 퀸에게 말을 걸어보자.", 5120025, true));
               }

               if (curChar.getMapId() >= 105200110 && curChar.getMapId() <= 105200119 || curChar.getMapId() >= 105200510 && curChar.getMapId() <= 105200519) {
                  curChar.getClient().send(CField.startMapEffect("차원의 틈에서 반반을 소환하자.", 5120025, true));
               }
            } while((curChar.getMapId() < 105200210 || curChar.getMapId() > 105200219) && (curChar.getMapId() < 105200610 || curChar.getMapId() > 105200616));

            curChar.getClient().send(CField.environmentChange("rootabyss/firework", 19));
            curChar.getClient().send(CField.startMapEffect("피에르의 티파티에 온 것을 진심으로 환영한다네!", 5120098, true));
         }
      } else {
         this.warp(mapId, 0);
      }
   }

   public final void warpParty(int mapId, int portal) {
      if (this.getPlayer().getParty() != null && this.getPlayer().getParty().getMembers().size() != 1) {
         boolean rand = portal < 0;
         MapleMap target = this.getMap(mapId);
         int cMap = this.getPlayer().getMapId();
         Iterator var6 = this.getPlayer().getParty().getMembers().iterator();

         while(true) {
            MapleCharacter curChar;
            do {
               do {
                  if (!var6.hasNext()) {
                     return;
                  }

                  MaplePartyCharacter chr = (MaplePartyCharacter)var6.next();
                  curChar = this.getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
               } while(curChar == null);
            } while(curChar.getMapId() != cMap && curChar.getEventInstance() != this.getPlayer().getEventInstance());

            if (rand) {
               try {
                  curChar.changeMap(target, target.getPortal(Randomizer.nextInt(target.getPortals().size())));
               } catch (Exception var10) {
                  curChar.changeMap(target, target.getPortal(0));
               }
            } else {
               curChar.changeMap(target, target.getPortal(portal));
            }
         }
      } else {
         if (portal < 0) {
            this.warp(mapId);
         } else {
            this.warp(mapId, portal);
         }

      }
   }

   public final void warpParty_Instanced(int mapId) {
      if (this.getPlayer().getParty() != null && this.getPlayer().getParty().getMembers().size() != 1) {
         MapleMap target = this.getMap_Instanced(mapId);
         int cMap = this.getPlayer().getMapId();
         Iterator var4 = this.getPlayer().getParty().getMembers().iterator();

         while(true) {
            MapleCharacter curChar;
            do {
               do {
                  if (!var4.hasNext()) {
                     return;
                  }

                  MaplePartyCharacter chr = (MaplePartyCharacter)var4.next();
                  curChar = this.getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
               } while(curChar == null);
            } while(curChar.getMapId() != cMap && curChar.getEventInstance() != this.getPlayer().getEventInstance());

            curChar.changeMap(target, target.getPortal(0));
         }
      } else {
         this.warp_Instanced(mapId);
      }
   }

   public void gainMeso(long gain) {
      this.c.getPlayer().gainMeso(gain, true, true);
   }

   public void gainExp(long gain) {
      this.c.getPlayer().gainExp(gain, true, true, true);
   }

   public void gainExpR(long gain) {
      this.c.getPlayer().gainExp(gain * (long)this.c.getChannelServer().getExpRate(), true, true, true);
   }

   public final void givePartyItems(int id, short quantity, List<MapleCharacter> party) {
      MapleCharacter chr;
      for(Iterator var4 = party.iterator(); var4.hasNext(); chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showCharmEffect(this.c.getPlayer(), id, 1, true, ""))) {
         chr = (MapleCharacter)var4.next();
         if (quantity >= 0) {
            MapleInventoryManipulator.addById(chr.getClient(), id, quantity, "Received from party interaction " + id + " (" + this.id2 + ")");
         } else {
            MapleInventoryManipulator.removeById(chr.getClient(), GameConstants.getInventoryType(id), id, -quantity, true, false);
         }
      }

   }

   public void addPartyTrait(String t, int e, List<MapleCharacter> party) {
      Iterator var4 = party.iterator();

      while(var4.hasNext()) {
         MapleCharacter chr = (MapleCharacter)var4.next();
         chr.getTrait(MapleTrait.MapleTraitType.valueOf(t)).addExp(e, chr);
      }

   }

   public void addPartyTrait(String t, int e) {
      if (this.getPlayer().getParty() != null && this.getPlayer().getParty().getMembers().size() != 1) {
         int cMap = this.getPlayer().getMapId();
         Iterator var4 = this.getPlayer().getParty().getMembers().iterator();

         while(true) {
            MapleCharacter curChar;
            do {
               do {
                  if (!var4.hasNext()) {
                     return;
                  }

                  MaplePartyCharacter chr = (MaplePartyCharacter)var4.next();
                  curChar = this.getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
               } while(curChar == null);
            } while(curChar.getMapId() != cMap && curChar.getEventInstance() != this.getPlayer().getEventInstance());

            curChar.getTrait(MapleTrait.MapleTraitType.valueOf(t)).addExp(e, curChar);
         }
      } else {
         this.addTrait(t, e);
      }
   }

   public void addTrait(String t, int e) {
      this.getPlayer().getTrait(MapleTrait.MapleTraitType.valueOf(t)).addExp(e, this.getPlayer());
   }

   public final void givePartyItems(int id, short quantity) {
      this.givePartyItems(id, quantity, false);
   }

   public final void givePartyItems(int id, short quantity, boolean removeAll) {
      if (this.getPlayer().getParty() != null && this.getPlayer().getParty().getMembers().size() != 1) {
         int cMap = this.getPlayer().getMapId();
         Iterator var5 = this.getPlayer().getParty().getMembers().iterator();

         while(true) {
            MapleCharacter curChar;
            do {
               do {
                  if (!var5.hasNext()) {
                     return;
                  }

                  MaplePartyCharacter chr = (MaplePartyCharacter)var5.next();
                  curChar = this.getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
               } while(curChar == null);
            } while(curChar.getMapId() != cMap && curChar.getEventInstance() != this.getPlayer().getEventInstance());

            this.gainItem(id, (short)(removeAll ? -curChar.itemQuantity(id) : quantity), false, 0L, 0, "", curChar.getClient());
         }
      } else {
         this.gainItem(id, (short)(removeAll ? -this.getPlayer().itemQuantity(id) : quantity));
      }
   }

   public final void givePartyExp_PQ(int maxLevel, double mod, List<MapleCharacter> party) {
      Iterator var5 = party.iterator();

      while(var5.hasNext()) {
         MapleCharacter chr = (MapleCharacter)var5.next();
         int amount = (int)Math.round((double)(GameConstants.getExpNeededForLevel(chr.getLevel() > maxLevel ? maxLevel + (maxLevel - chr.getLevel()) / 10 : chr.getLevel()) / (long)Math.min(chr.getLevel(), maxLevel)) / 5.0D / mod * 2.0D);
         chr.gainExp((long)(amount * this.c.getChannelServer().getExpRate()), true, true, true);
      }

   }

   public final void gainExp_PQ(int maxLevel, double mod) {
      int amount = (int)Math.round((double)(GameConstants.getExpNeededForLevel(this.getPlayer().getLevel() > maxLevel ? maxLevel + this.getPlayer().getLevel() / 10 : this.getPlayer().getLevel()) / (long)Math.min(this.getPlayer().getLevel(), maxLevel)) / 10.0D / mod);
      this.gainExp((long)(amount * this.c.getChannelServer().getExpRate()));
   }

   public final void givePartyExp_PQ(int maxLevel, double mod) {
      int cMap;
      if (this.getPlayer().getParty() != null && this.getPlayer().getParty().getMembers().size() != 1) {
         cMap = this.getPlayer().getMapId();
         Iterator var5 = this.getPlayer().getParty().getMembers().iterator();

         while(true) {
            MapleCharacter curChar;
            do {
               do {
                  if (!var5.hasNext()) {
                     return;
                  }

                  MaplePartyCharacter chr = (MaplePartyCharacter)var5.next();
                  curChar = this.getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
               } while(curChar == null);
            } while(curChar.getMapId() != cMap && curChar.getEventInstance() != this.getPlayer().getEventInstance());

            int amount = (int)Math.round((double)(GameConstants.getExpNeededForLevel(curChar.getLevel() > maxLevel ? maxLevel + curChar.getLevel() / 10 : curChar.getLevel()) / (long)Math.min(curChar.getLevel(), maxLevel)) / 10.0D / mod);
            curChar.gainExp((long)(amount * this.c.getChannelServer().getExpRate()), true, true, true);
         }
      } else {
         cMap = (int)Math.round((double)(GameConstants.getExpNeededForLevel(this.getPlayer().getLevel() > maxLevel ? maxLevel + this.getPlayer().getLevel() / 10 : this.getPlayer().getLevel()) / (long)Math.min(this.getPlayer().getLevel(), maxLevel)) / 10.0D / mod);
         this.gainExp((long)(cMap * this.c.getChannelServer().getExpRate()));
      }
   }

   public final void givePartyExp(int amount, List<MapleCharacter> party) {
      Iterator var3 = party.iterator();

      while(var3.hasNext()) {
         MapleCharacter chr = (MapleCharacter)var3.next();
         chr.gainExp((long)(amount * this.c.getChannelServer().getExpRate()), true, true, true);
      }

   }

   public final void givePartyExp(int amount) {
      if (this.getPlayer().getParty() != null && this.getPlayer().getParty().getMembers().size() != 1) {
         int cMap = this.getPlayer().getMapId();
         Iterator var3 = this.getPlayer().getParty().getMembers().iterator();

         while(true) {
            MapleCharacter curChar;
            do {
               do {
                  if (!var3.hasNext()) {
                     return;
                  }

                  MaplePartyCharacter chr = (MaplePartyCharacter)var3.next();
                  curChar = this.getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
               } while(curChar == null);
            } while(curChar.getMapId() != cMap && curChar.getEventInstance() != this.getPlayer().getEventInstance());

            curChar.gainExp((long)(amount * this.c.getChannelServer().getExpRate()), true, true, true);
         }
      } else {
         this.gainExp((long)(amount * this.c.getChannelServer().getExpRate()));
      }
   }

   public final void givePartyNX(int amount, List<MapleCharacter> party) {
      Iterator var3 = party.iterator();

      while(var3.hasNext()) {
         MapleCharacter chr = (MapleCharacter)var3.next();
         chr.modifyCSPoints(1, amount, true);
      }

   }

   public final void givePartyNX(int amount) {
      if (this.getPlayer().getParty() != null && this.getPlayer().getParty().getMembers().size() != 1) {
         int cMap = this.getPlayer().getMapId();
         Iterator var3 = this.getPlayer().getParty().getMembers().iterator();

         while(true) {
            MapleCharacter curChar;
            do {
               do {
                  if (!var3.hasNext()) {
                     return;
                  }

                  MaplePartyCharacter chr = (MaplePartyCharacter)var3.next();
                  curChar = this.getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
               } while(curChar == null);
            } while(curChar.getMapId() != cMap && curChar.getEventInstance() != this.getPlayer().getEventInstance());

            curChar.modifyCSPoints(1, amount, true);
         }
      } else {
         this.gainNX(amount);
      }
   }

   public final void endPartyQuest(int amount, List<MapleCharacter> party) {
      Iterator var3 = party.iterator();

      while(var3.hasNext()) {
         MapleCharacter chr = (MapleCharacter)var3.next();
         chr.endPartyQuest(amount);
      }

   }

   public final void endPartyQuest(int amount) {
      if (this.getPlayer().getParty() != null && this.getPlayer().getParty().getMembers().size() != 1) {
         int cMap = this.getPlayer().getMapId();
         Iterator var3 = this.getPlayer().getParty().getMembers().iterator();

         while(true) {
            MapleCharacter curChar;
            do {
               do {
                  if (!var3.hasNext()) {
                     return;
                  }

                  MaplePartyCharacter chr = (MaplePartyCharacter)var3.next();
                  curChar = this.getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
               } while(curChar == null);
            } while(curChar.getMapId() != cMap && curChar.getEventInstance() != this.getPlayer().getEventInstance());

            curChar.endPartyQuest(amount);
         }
      } else {
         this.getPlayer().endPartyQuest(amount);
      }
   }

   public final void removeFromParty(int id, List<MapleCharacter> party) {
      Iterator var3 = party.iterator();

      while(var3.hasNext()) {
         MapleCharacter chr = (MapleCharacter)var3.next();
         int possesed = chr.getInventory(GameConstants.getInventoryType(id)).countById(id);
         if (possesed > 0) {
            MapleInventoryManipulator.removeById(this.c, GameConstants.getInventoryType(id), id, possesed, true, false);
            chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showCharmEffect(this.c.getPlayer(), id, 1, true, ""));
         }
      }

   }

   public final void removeFromParty(int id) {
      this.givePartyItems(id, (short)0, true);
   }

   public final void useSkill(int skill, int level) {
      if (level > 0) {
         SkillFactory.getSkill(skill).getEffect(level).applyTo(this.c.getPlayer(), false);
      }
   }

   public final void useItem(int id) {
      MapleItemInformationProvider.getInstance().getItemEffect(id).applyTo(this.c.getPlayer(), true);
      this.c.getSession().writeAndFlush(CWvsContext.InfoPacket.getStatusMsg(id));
   }

   public final void cancelItem(int id) {
      this.c.getPlayer().cancelEffect(MapleItemInformationProvider.getInstance().getItemEffect(id));
   }

   public final int getMorphState() {
      return this.c.getPlayer().getMorphState();
   }

   public final void removeAll(int id) {
      this.c.getPlayer().removeAll(id);
   }

   public final void gainCloseness(int closeness, int index) {
      MaplePet pet = this.getPlayer().getPet((long)index);
      if (pet != null) {
         pet.setCloseness(pet.getCloseness() + closeness * this.getChannelServer().getTraitRate());
         this.getClient().getSession().writeAndFlush(PetPacket.updatePet(this.c.getPlayer(), pet, this.getPlayer().getInventory(MapleInventoryType.CASH).getItem(pet.getInventoryPosition()), false, this.c.getPlayer().getPetLoot()));
      }

   }

   public final void gainClosenessAll(int closeness) {
      MaplePet[] var2 = this.getPlayer().getPets();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         MaplePet pet = var2[var4];
         if (pet != null && pet.getSummoned()) {
            pet.setCloseness(pet.getCloseness() + closeness);
            this.getClient().getSession().writeAndFlush(PetPacket.updatePet(this.c.getPlayer(), pet, this.getPlayer().getInventory(MapleInventoryType.CASH).getItem(pet.getInventoryPosition()), false, this.c.getPlayer().getPetLoot()));
         }
      }

   }

   public final void resetMap(int mapid) {
      this.getMap(mapid).resetFully();
   }

   public final void openNpc(int id) {
      this.c.removeClickedNPC();
      NPCScriptManager.getInstance().dispose(this.c);
      NPCScriptManager.getInstance().start(this.getClient(), id);
   }

   public final void openNpc(String script) {
      this.getClient().removeClickedNPC();
      NPCScriptManager.getInstance().dispose(this.c);
      Iterator var2 = MapleLifeFactory.getNpcScripts().entrySet().iterator();

      while(var2.hasNext()) {
         Entry<Integer, String> data = (Entry)var2.next();
         if (((String)data.getValue()).equals(script)) {
            NPCScriptManager.getInstance().start(this.getClient(), (Integer)data.getKey(), script);
         }
      }

   }

   public void openNpc(int npc, String script) {
      this.getClient().removeClickedNPC();
      NPCScriptManager.getInstance().dispose(this.c);
      NPCScriptManager.getInstance().start(this.c, npc, script);
   }

   public final void openNpc(MapleClient cg, int id) {
      cg.removeClickedNPC();
      NPCScriptManager.getInstance().dispose(cg);
      NPCScriptManager.getInstance().start(cg, id);
   }

   public final int getMapId() {
      return this.c.getPlayer().getMap().getId();
   }

   public final boolean haveMonster(int mobid) {
      Iterator var2 = this.c.getPlayer().getMap().getAllMonstersThreadsafe().iterator();

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

   public final boolean haveMonster() {
      return this.c.getPlayer().getMap().getNumMonsters() > 0;
   }

   public final int getChannelNumber() {
      return this.c.getChannel();
   }

   public final int getMonsterCount(int mapid) {
      return this.c.getChannelServer().getMapFactory().getMap(mapid).getNumMonsters();
   }

   public final void teachSkill(int id, int level, byte masterlevel) {
      this.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(id), level, masterlevel);
   }

   public final void teachSkill(int id, int level) {
      Skill skil = SkillFactory.getSkill(id);
      if (this.getPlayer().getSkillLevel(skil) > level) {
         level = this.getPlayer().getSkillLevel(skil);
      }

      this.getPlayer().changeSingleSkillLevel(skil, level, (byte)skil.getMaxLevel());
   }

   public final int getPlayerCount(int mapid) {
      return this.c.getChannelServer().getMapFactory().getMap(mapid).getCharactersSize();
   }

   public final boolean dojoAgent_NextMap(boolean dojo, boolean fromresting) {
      return dojo ? Event_DojoAgent.warpNextMap(this.c.getPlayer(), fromresting, this.c.getPlayer().getMap()) : Event_DojoAgent.warpNextMap_Agent(this.c.getPlayer(), fromresting);
   }

   public final boolean dojoAgent_NextMap(boolean dojo, boolean fromresting, int mapid) {
      return dojo ? Event_DojoAgent.warpNextMap(this.c.getPlayer(), fromresting, this.getMap(mapid)) : Event_DojoAgent.warpNextMap_Agent(this.c.getPlayer(), fromresting);
   }

   public final int dojo_getPts() {
      return this.c.getPlayer().getIntNoRecord(150100);
   }

   public final MapleEvent getEvent(String loc) {
      return this.c.getChannelServer().getEvent(MapleEventType.valueOf(loc));
   }

   public final int getSavedLocation(String loc) {
      Integer ret = this.c.getPlayer().getSavedLocation(SavedLocationType.fromString(loc));
      return ret != null && ret != -1 ? ret : 100000000;
   }

   public final void saveLocation(String loc) {
      this.c.getPlayer().saveLocation(SavedLocationType.fromString(loc));
   }

   public final void saveReturnLocation(String loc) {
      this.c.getPlayer().saveLocation(SavedLocationType.fromString(loc), this.c.getPlayer().getMap().getReturnMap().getId());
   }

   public final void clearSavedLocation(String loc) {
      this.c.getPlayer().clearSavedLocation(SavedLocationType.fromString(loc));
   }

   public final void showInstruction(String msg, int width, int height) {
      this.c.getSession().writeAndFlush(CField.sendHint(msg, width, height));
   }

   public final String getInfoQuest(int id) {
      return this.c.getPlayer().getInfoQuest(id);
   }

   public final void updateInfoQuest(int id, String data) {
      this.c.getPlayer().updateInfoQuest(id, data);
   }

   public final boolean getEvanIntroState(String data) {
      return this.getInfoQuest(22013).equals(data);
   }

   public final void updateEvanIntroState(String data) {
      this.updateInfoQuest(22013, data);
   }

   public final void Aran_Start() {
      this.c.getSession().writeAndFlush(CField.Aran_Start());
   }

   public final void evanTutorial(String data, int v1) {
      this.c.getSession().writeAndFlush(CField.NPCPacket.getEvanTutorial(data));
   }

   public final void showWZEffect(String data) {
      this.c.getSession().writeAndFlush(CField.EffectPacket.showWZEffect(data));
   }

   public final void EarnTitleMsg(String data) {
      this.c.getSession().writeAndFlush(CWvsContext.getTopMsg(data));
   }

   public final void EnableUI(short i) {
      this.c.getSession().writeAndFlush(CField.UIPacket.IntroEnableUI(i));
   }

   public final void DisableUI(boolean enabled) {
      this.c.getSession().writeAndFlush(CField.UIPacket.IntroDisableUI(enabled));
   }

   public final void MovieClipIntroUI(boolean enabled) {
      this.c.getSession().writeAndFlush(CField.UIPacket.IntroDisableUI(enabled));
      this.c.getSession().writeAndFlush(CField.UIPacket.IntroLock(enabled));
   }

   public MapleInventoryType getInvType(int i) {
      return MapleInventoryType.getByType((byte)i);
   }

   public String getItemName(int id) {
      return MapleItemInformationProvider.getInstance().getName(id);
   }

   public void gainPet(int id, String name, int level, int closeness, int fullness, long period, short flags) {
      if (level > 30) {
         level = 30;
      }

      if (closeness > 30000) {
         closeness = 30000;
      }

      if (fullness > 100) {
         fullness = 100;
      }

      try {
         MapleInventoryManipulator.addById(this.c, id, (short)1, "", MaplePet.createPet(id, name, level, closeness, fullness, MapleInventoryIdentifier.getInstance(), id == 5000054 ? (int)period : 0, flags), 45L, "Pet from interaction " + id + " (" + this.id2 + ") on " + FileoutputUtil.CurrentReadable_Date());
      } catch (NullPointerException var10) {
         var10.printStackTrace();
      }

   }

   public void removeSlot(int invType, byte slot, short quantity) {
      MapleInventoryManipulator.removeFromSlot(this.c, this.getInvType(invType), (short)slot, quantity, true);
   }

   public void gainGP(int gp) {
      if (this.getPlayer().getGuildId() > 0) {
         World.Guild.gainContribution(this.getPlayer().getGuildId(), gp);
      }
   }

   public int getGP() {
      return this.getPlayer().getGuildId() <= 0 ? 0 : World.Guild.getGP(this.getPlayer().getGuildId());
   }

   public void showMapEffect(String path) {
      this.getClient().getSession().writeAndFlush(CField.MapEff(path));
   }

   public int itemQuantity(int itemid) {
      return this.getPlayer().itemQuantity(itemid);
   }

   public EventInstanceManager getDisconnected(String event) {
      EventManager em = this.getEventManager(event);
      if (em == null) {
         return null;
      } else {
         Iterator var3 = em.getInstances().iterator();

         EventInstanceManager eim;
         do {
            if (!var3.hasNext()) {
               return null;
            }

            eim = (EventInstanceManager)var3.next();
         } while(!eim.isDisconnected(this.c.getPlayer()) || eim.getPlayerCount() <= 0);

         return eim;
      }
   }

   public boolean isAllReactorState(int reactorId, int state) {
      boolean ret = false;
      Iterator var4 = this.getMap().getAllReactorsThreadsafe().iterator();

      while(var4.hasNext()) {
         MapleReactor r = (MapleReactor)var4.next();
         if (r.getReactorId() == reactorId) {
            ret = r.getState() == state;
         }
      }

      return ret;
   }

   public long getCurrentTime() {
      return System.currentTimeMillis();
   }

   public void spawnMonster(int id) {
      this.spawnMonster(id, 1, this.getPlayer().getTruePosition());
   }

   public void spawnMonster(int id, int x, int y) {
      this.spawnMonster(id, 1, new Point(x, y));
   }

   public void spawnMonster(int id, int qty, int x, int y) {
      this.spawnMonster(id, qty, new Point(x, y));
   }

   public void spawnMonster(int id, int qty, Point pos) {
      for(int i = 0; i < qty; ++i) {
         MapleMonster monster = MapleLifeFactory.getMonster(id);
         if (monster.getId() >= 8920100 && monster.getId() <= 8920103 || monster.getId() >= 8920000 && monster.getId() <= 8920003) {
            Iterator var6 = monster.getSkills().iterator();

            while(var6.hasNext()) {
               MobSkill sk = (MobSkill)var6.next();
               if (sk.getAfterDead() > 0) {
                  monster.setLastSkillUsed(sk, System.currentTimeMillis(), 40000L);
               }
            }
         }

         if (monster.getId() != 8910100 && monster.getId() != 8910000) {
            this.getMap().spawnMonsterOnGroundBelow(monster, pos);
         } else {
            server.Timer.MapTimer.getInstance().schedule(() -> {
               this.getMap().spawnMonsterWithEffect(monster, -1, pos);
            }, 2000L);
         }
      }

   }

   public void sendNPCText(String text, int npc) {
      this.getMap().broadcastMessage(CField.NPCPacket.getNPCTalk(npc, (byte)0, text, "00 00", (byte)0));
   }

   public boolean getTempFlag(int flag) {
      return (this.c.getChannelServer().getTempFlag() & flag) == flag;
   }

   public void logPQ(String text) {
   }

   public void outputFileError(Throwable t) {
      FileoutputUtil.outputFileError("Log_Script_Except.rtf", t);
   }

   public void trembleEffect(int type, int delay) {
      this.c.getSession().writeAndFlush(CField.trembleEffect(type, delay));
   }

   public int nextInt(int arg0) {
      return Randomizer.nextInt(arg0);
   }

   public MapleQuest getQuest(int arg0) {
      return MapleQuest.getInstance(arg0);
   }

   public final MapleInventory getInventory(int type) {
      return this.c.getPlayer().getInventory(MapleInventoryType.getByType((byte)type));
   }

   public int randInt(int arg0) {
      return Randomizer.nextInt(arg0);
   }

   public void sendDirectionStatus(int key, int value) {
      this.c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(key, value));
      this.c.getSession().writeAndFlush(CField.UIPacket.getDirectionStatus(true));
   }

   public void sendDirectionInfo(String data) {
      this.c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(data, 2000, 0, -100, 0, 0));
      this.c.getSession().writeAndFlush(CField.UIPacket.getDirectionInfo(1, 2000));
   }

   public void addEquip(short pos, int itemid, short watk, short wdef, short mdef, byte upslot, short hp, short mp) {
      MapleInventory equip = this.c.getPlayer().getInventory(MapleInventoryType.EQUIPPED);
      Item item = MapleItemInformationProvider.getInstance().getEquipById(itemid);
      Equip eq = (Equip)item;
      eq.setWatk(watk);
      eq.setWdef(wdef);
      eq.setMdef(mdef);
      eq.setMp(mp);
      eq.setHp(hp);
      if (itemid == 1099004) {
         eq.setStr((short)12);
         eq.setDex((short)12);
      }

      if (itemid == 1098002) {
         eq.setStr((short)7);
         eq.setDex((short)7);
      }

      if (itemid == 1098003) {
         eq.setStr((short)12);
         eq.setDex((short)12);
      }

      eq.setUpgradeSlots(upslot);
      eq.setExpiration(-1L);
      eq.setPosition(pos);
      equip.addFromDB(eq);
      this.c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, item));
   }

   public final void timeMoveMap(int n, int n2, int n3) {
      this.warp(n, 0);
      this.getClient().send(CField.getClock(n3));
      server.Timer.CloneTimer.getInstance().schedule(new a(this, n, n2), (long)(n3 * 1000));
   }

   public final void TimeMoveMap(int n, int n2, int n3) {
      this.timeMoveMap(n, n2, n3);
   }

   public final void scheduleTimeMoveMap(int destid, int fromid, int time, boolean reset) {
      MapleMap dest = this.c.getChannelServer().getMapFactory().getMap(destid);
      MapleMap from = this.c.getChannelServer().getMapFactory().getMap(fromid);
      from.broadcastMessage(CField.getClock(time));
      from.setMapTimer(System.currentTimeMillis() + (long)time * 1000L);
      server.Timer.CloneTimer tMan = server.Timer.CloneTimer.getInstance();
      Runnable r = () -> {
         List<MapleCharacter> chr = new ArrayList();
         Iterator var5 = from.getAllChracater().iterator();

         MapleMapObject i;
         while(var5.hasNext()) {
            i = (MapleMapObject)var5.next();
            chr.add((MapleCharacter)i);
         }

         var5 = chr.iterator();

         while(var5.hasNext()) {
            MapleCharacter chrz = (MapleCharacter)var5.next();
            chrz.setDeathCount((byte)0);
            chrz.changeMap(dest, dest.getPortal(0));
         }

         if (reset) {
            from.resetFully();
            from.resetReactors(this.c);
            from.killAllMonsters(false);
            from.setMapTimer(0L);
            var5 = from.getAllItems().iterator();

            while(var5.hasNext()) {
               i = (MapleMapObject)var5.next();
               from.removeMapObject(i);
            }
         }

      };
      tMan.schedule(r, (long)time * 1000L);
   }

   public final void scheduleTimeMoveDojang(int destid, int fromid, int time, final boolean reset) {
      final MapleMap dest = this.c.getChannelServer().getMapFactory().getMap(destid);
      final MapleMap from = this.c.getChannelServer().getMapFactory().getMap(fromid);
      from.broadcastMessage(CField.getClock(time));
      from.setMapTimer(System.currentTimeMillis() + (long)time * 1000L);
      server.Timer.CloneTimer tMan = server.Timer.CloneTimer.getInstance();
      Runnable r = new Runnable() {
         public void run() {
            List<MapleCharacter> chr = new ArrayList();
            Iterator var2 = from.getAllChracater().iterator();

            MapleMapObject i;
            while(var2.hasNext()) {
               i = (MapleMapObject)var2.next();
               chr.add((MapleCharacter)i);
            }

            var2 = chr.iterator();

            while(var2.hasNext()) {
               MapleCharacter chrz = (MapleCharacter)var2.next();
               chrz.setDeathCount((byte)0);
               chrz.changeMap(dest, dest.getPortal(0));
               chrz.setKeyValue(3, "dojang_m", "-1");
               chrz.setKeyValue(3, "dojang", "-1");
               NPCScriptManager.getInstance().start(chrz.getClient(), 2007);
            }

            if (reset) {
               from.resetFully();
               from.resetReactors(AbstractPlayerInteraction.this.c);
               from.killAllMonsters(false);
               from.setMapTimer(0L);
               var2 = from.getAllItems().iterator();

               while(var2.hasNext()) {
                  i = (MapleMapObject)var2.next();
                  from.removeMapObject(i);
               }
            }

         }
      };
      tMan.schedule(r, (long)time * 1000L);
   }

   public void teleport(int portal) {
      this.c.getSession().writeAndFlush(CField.instantMapWarp(this.c.getPlayer(), (byte)portal));
      this.c.getPlayer().getMap().movePlayer(this.c.getPlayer(), new Point(this.c.getPlayer().getMap().getPortal(portal).getPosition()));
      if (this.c.getPlayer().getMapId() == 450004550) {
         SkillFactory.getSkill(80002255).getEffect(1).applyTo(this.c.getPlayer());
         this.c.getPlayer().addHP(-(this.c.getPlayer().getStat().getCurrentMaxHp() * 50L) / 100L);
      }

      this.c.getSession().writeAndFlush(CWvsContext.enableActions(this.c.getPlayer()));
   }

   public void fakeRelog() {
      this.c.getSession().writeAndFlush(CField.getCharInfo(this.c.getPlayer()));
      this.c.getPlayer().updateSkillPacket();
      this.c.getPlayer().updateLinkSkillPacket();
   }

   public void updateChar() {
      MapleMap currentMap = this.c.getPlayer().getMap();
      currentMap.removePlayer(this.c.getPlayer());
      currentMap.addPlayer(this.c.getPlayer());
   }

   public void updateKeyValue(int charid, int qid, String keyvalue) {
      Connection con = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE queststatus SET customData = ? WHERE quest = ? AND characterid = ?");
         ps.setString(1, keyvalue);
         ps.setInt(2, qid);
         ps.setInt(2, charid);
         ps.executeUpdate();
         ps.close();
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

            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var15) {
            var15.printStackTrace();
         }

      }

   }

   public String DojangText() {
      StringBuilder str = (new StringBuilder()).append("무릉도장 랭킹입니다. 상위 50등 까지만 랭킹에 보여집니다.\r\n");
      List<Triple<Integer, Integer, String>> data = this.DojoRank();

      for(int i = 0; i < data.size(); ++i) {
         str.append(i + 1 + "등 : #b" + (String)((Triple)data.get(i)).right + "#k #d" + ((Triple)data.get(i)).left + "층#k " + ((Triple)data.get(i)).mid + "초\r\n");
         if (i >= 99) {
            break;
         }
      }

      return str.toString();
   }

   public String charName(int id) {
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT name FROM characters WHERE id = ?");
         ps.setInt(1, id);
         rs = ps.executeQuery();
         if (rs.next()) {
            String var5 = rs.getString("name");
            return var5;
         }

         rs.close();
         ps.close();
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

            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var15) {
            var15.printStackTrace();
         }

      }

      return null;
   }

   public List<Triple<Integer, Integer, String>> DojoRank() {
      List<Triple<Integer, Integer, String>> file = new ArrayList();
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      int i;
      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("SELECT * FROM keyvalue WHERE `key` = ?");
         ps.setString(1, "dojo");
         rs = ps.executeQuery();

         while(rs.next()) {
            i = Integer.parseInt(rs.getString("value"));
            PreparedStatement ps1 = con.prepareStatement("SELECT * FROM keyvalue WHERE `key` = ? AND `id` = ?");
            ps1.setString(1, "dojo_time");
            ps1.setInt(2, rs.getInt("id"));
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) {
               int timez = Integer.parseInt(rs.getString("value"));
               String name = this.charName(rs.getInt("id"));
               if (i > 0 && timez > 0 && name != null) {
                  file.add(new Triple(i, timez, name));
               }
            }

            rs1.close();
            ps1.close();
            if (file.size() == 50) {
               break;
            }
         }

         rs.close();
         ps.close();
      } catch (SQLException var18) {
         var18.printStackTrace();
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

      for(i = 0; i < file.size(); ++i) {
         for(int j = i + 1; j < file.size(); ++j) {
            Triple temp;
            if ((Integer)((Triple)file.get(i)).left < (Integer)((Triple)file.get(j)).left) {
               temp = (Triple)file.get(i);
               file.set(i, (Triple)file.get(j));
               file.set(j, temp);
            } else if (((Triple)file.get(i)).left == ((Triple)file.get(j)).left && (Integer)((Triple)file.get(i)).mid > (Integer)((Triple)file.get(j)).mid) {
               temp = (Triple)file.get(i);
               file.set(i, (Triple)file.get(j));
               file.set(j, temp);
            }
         }
      }

      return file;
   }

   public void spawnMonster2(int id, int qty) {
      for(int i = 0; i < qty; ++i) {
         MapleMonster m = MapleLifeFactory.getMonster(id);
         m.setHp(m.getStats().getHp());
         m.getStats().setHp(m.getStats().getHp());
         this.c.getPlayer().getMap().spawnMonsterOnGroundBelow(m, this.c.getPlayer().getTruePosition());
      }

   }

   public void openNpcCustom(MapleClient c, int id, String custom) {
      c.removeClickedNPC();
      NPCScriptManager.getInstance().dispose(c);
      NPCScriptManager.getInstance().start(this.getClient(), id, custom);
   }

   public final int checkWeddingReservation() {
      int ret = this.checkWeddingInternal();
      if (ret > 0) {
         return ret;
      } else {
         MarriageDataEntry data = MarriageManager.getInstance().getMarriage(this.c.getPlayer().getMarriageId());
         return data.getWeddingStatus() > 0 ? 8 : 0;
      }
   }

   public final int checkWeddingStart() {
      return this.checkWeddingInternal();
   }

   private int checkWeddingInternal() {
      if (this.c.getPlayer().getParty() == null) {
         return 1;
      } else if (this.c.getPlayer().getParty().getMembers().size() != 2) {
         return 2;
      } else if (this.c.getPlayer().getMarriageId() <= 0) {
         return 3;
      } else {
         MarriageDataEntry data = MarriageManager.getInstance().getMarriage(this.c.getPlayer().getMarriageId());
         if (data == null) {
            return 7;
         } else {
            boolean foundGroom = false;
            boolean foundBride = false;
            Iterator var4 = this.c.getPlayer().getParty().getMembers().iterator();

            while(var4.hasNext()) {
               MaplePartyCharacter mem = (MaplePartyCharacter)var4.next();
               MapleCharacter chr = this.c.getPlayer().getMap().getCharacterById(mem.getId());
               if (chr == null) {
                  return 4;
               }

               if (chr.getId() == data.getGroomId()) {
                  foundGroom = true;
               } else if (chr.getId() == data.getBrideId()) {
                  foundBride = true;
               }
            }

            if (foundGroom && foundBride) {
               if (data.getStatus() != 1) {
                  return 6;
               } else {
                  return 0;
               }
            } else {
               return 5;
            }
         }
      }
   }

   public MarriageDataEntry getMarriageData() {
      return this.getMarriageData(this.c.getPlayer().getMarriageId());
   }

   public MarriageDataEntry getMarriageData(int marriageId) {
      return MarriageManager.getInstance().getMarriage(marriageId);
   }

   public MarriageEventAgent getMarriageAgent() {
      return this.getMarriageAgent(this.c.getChannel());
   }

   public MarriageEventAgent getMarriageAgent(int channel) {
      return MarriageManager.getInstance().getEventAgent(channel);
   }

   public void sendWeddingWishListInputDlg() {
      this.c.getSession().writeAndFlush(CWvsContext.showWeddingWishInputDialog());
   }

   public final int getInvSlots(int i) {
      return this.c.getPlayer().getInventory(MapleInventoryType.getByType((byte)i)).getNumFreeSlot();
   }

   public final void partyMessage(int type, String message) {
      if (this.c.getPlayer().getParty() != null) {
         World.Party.partyPacket(this.c.getPlayer().getParty().getId(), CWvsContext.serverNotice(type, "", message), (MaplePartyCharacter)null);
      }

   }

   public final int makeWeddingReservation(int itemId) {
      int ret = this.checkWeddingReservation();
      if (ret > 0) {
         return ret;
      } else {
         MarriageDataEntry data = this.getMarriageData(this.getPlayer().getMarriageId());
         data.setWeddingStatus(1);
         if (itemId == 5251004) {
            data.setTicketType(MarriageTicketType.CheapTicket);
         } else if (itemId == 5251005) {
            data.setTicketType(MarriageTicketType.SweetieTicket);
         } else if (itemId == 5251006) {
            data.setTicketType(MarriageTicketType.PremiumTicket);
         }

         MapleCharacter chr = this.c.getPlayer().getMap().getCharacterById(this.getPlayer().getGender() == 1 ? data.getGroomId() : data.getBrideId());
         if (chr != null) {
            NPCScriptManager.getInstance().start(chr.getClient(), 9201013);
         }

         this.sendWeddingWishListInputDlg();
         return 0;
      }
   }

   public final void EnterColorfulInvitationCard() {
      MapleCharacter chr = this.c.getPlayer();
      if (chr != null) {
         chr.warp(993190800);
         this.c.getPlayer().setColorCardInstance((ColorInvitationCard)null);
         ColorInvitationCard Card = new ColorInvitationCard(chr);
         this.c.getPlayer().setColorCardInstance(Card);
         this.c.send(SLFCGPacket.SetIngameDirectionMode(true, false, false, false));
         this.c.send(SLFCGPacket.InGameDirectionEvent("", 10, 1));
         this.c.send(SLFCGPacket.ColorCardPacket.ColorCardState(1));
         this.c.send(SLFCGPacket.ColorCardPacket.ColorCardSetting2(800, 50));
         this.c.send(SLFCGPacket.ColorCardPacket.ColorCardSetting());
         this.c.send(SLFCGPacket.ColorCardPacket.ColorCardSetting1());
         this.c.send(SLFCGPacket.ColorCardPacket.ColorCardKind(this.c.getPlayer().getColorCardInstance().getCardList()));
         this.c.send(SLFCGPacket.ColorCardPacket.ColorCardState(2));
         server.Timer.EventTimer.getInstance().schedule(() -> {
            this.c.send(CField.getClock(90));
            this.c.send(SLFCGPacket.ColorCardPacket.ColorCardState(3));
         }, 3000L);
         server.Timer.EventTimer.getInstance().schedule(() -> {
            if (chr != null) {
               this.c.send(SLFCGPacket.ColorCardPacket.ColorCardState(5));
            }

         }, 93000L);
         server.Timer.EventTimer.getInstance().schedule(() -> {
            if (chr != null) {
               this.c.send(SLFCGPacket.ColorCardPacket.ColorCardResult(this.c.getPlayer().getColorCardInstance()));
            }

         }, 95000L);
         server.Timer.EventTimer.getInstance().schedule(() -> {
            if (chr != null) {
               this.c.send(SLFCGPacket.SetIngameDirectionMode(false, true, false, false));
               chr.warp(993191100);
            }

         }, 100000L);
      }

   }

   public Equip JoinNubGuild(Equip nEquip, int starforce) {
      label63:
      while(true) {
         if (nEquip.getEnhance() < starforce) {
            StarForceStats statz = EquipmentEnchant.starForceStats(nEquip);
            nEquip.setEnchantBuff((short)0);
            nEquip.setEnhance((byte)(nEquip.getEnhance() + 1));
            Iterator var4 = statz.getStats().iterator();

            while(true) {
               if (!var4.hasNext()) {
                  continue label63;
               }

               Pair<EnchantFlag, Integer> stat = (Pair)var4.next();
               if (EnchantFlag.Watk.check(((EnchantFlag)stat.left).getValue())) {
                  nEquip.setEnchantWatk((short)(nEquip.getEnchantWatk() + (Integer)stat.right));
               }

               if (EnchantFlag.Matk.check(((EnchantFlag)stat.left).getValue())) {
                  nEquip.setEnchantMatk((short)(nEquip.getEnchantMatk() + (Integer)stat.right));
               }

               if (EnchantFlag.Str.check(((EnchantFlag)stat.left).getValue())) {
                  nEquip.setEnchantStr((short)(nEquip.getEnchantStr() + (Integer)stat.right));
               }

               if (EnchantFlag.Dex.check(((EnchantFlag)stat.left).getValue())) {
                  nEquip.setEnchantDex((short)(nEquip.getEnchantDex() + (Integer)stat.right));
               }

               if (EnchantFlag.Int.check(((EnchantFlag)stat.left).getValue())) {
                  nEquip.setEnchantInt((short)(nEquip.getEnchantInt() + (Integer)stat.right));
               }

               if (EnchantFlag.Luk.check(((EnchantFlag)stat.left).getValue())) {
                  nEquip.setEnchantLuk((short)(nEquip.getEnchantLuk() + (Integer)stat.right));
               }

               if (EnchantFlag.Wdef.check(((EnchantFlag)stat.left).getValue())) {
                  nEquip.setEnchantWdef((short)(nEquip.getEnchantWdef() + (Integer)stat.right));
               }

               if (EnchantFlag.Mdef.check(((EnchantFlag)stat.left).getValue())) {
                  nEquip.setEnchantMdef((short)(nEquip.getEnchantMdef() + (Integer)stat.right));
               }

               if (EnchantFlag.Hp.check(((EnchantFlag)stat.left).getValue())) {
                  nEquip.setEnchantHp((short)(nEquip.getEnchantHp() + (Integer)stat.right));
               }

               if (EnchantFlag.Mp.check(((EnchantFlag)stat.left).getValue())) {
                  nEquip.setEnchantMp((short)(nEquip.getEnchantMp() + (Integer)stat.right));
               }

               if (EnchantFlag.Acc.check(((EnchantFlag)stat.left).getValue())) {
                  nEquip.setEnchantAcc((short)(nEquip.getEnchantAcc() + (Integer)stat.right));
               }

               if (EnchantFlag.Avoid.check(((EnchantFlag)stat.left).getValue())) {
                  nEquip.setEnchantAvoid((short)(nEquip.getEnchantAvoid() + (Integer)stat.right));
               }
            }
         }

         return nEquip;
      }
   }

   public final void CheckTime() {
      if (this.c.getPlayer().getMap().PapulratusPatan == 2) {
         int 구역 = 0;
         switch(this.c.getPlayer().getMap().Papullatushour) {
         case 1:
         case 2:
            구역 = 241;
            break;
         case 3:
         case 4:
            구역 = 242;
            break;
         case 5:
         case 6:
            구역 = 243;
            break;
         case 7:
         case 8:
            구역 = 244;
            break;
         case 9:
         case 10:
            구역 = 245;
            break;
         case 11:
         case 12:
            구역 = 246;
         }

         if (this.c.getPlayer().getMap().Papullatushour % 2 == 0 && this.c.getPlayer().getMap().Papullatusminute > 0) {
            if (구역 == 246) {
               구역 = 241;
            } else {
               ++구역;
            }
         }

         Iterator var2 = this.c.getPlayer().getMap().getAllMonster().iterator();

         MapleMonster monster;
         do {
            do {
               if (!var2.hasNext()) {
                  return;
               }

               monster = (MapleMonster)var2.next();
            } while(monster.getCustomValue0(구역) == 4L);
         } while(monster.getId() != 8500001 && monster.getId() != 8500011 && monster.getId() != 8500021);

         monster.setCustomInfo(구역, 4, 0);
         monster.getMap().broadcastMessage(CField.startMapEffect("차원의 균열을 봉인했습니다.", 5120177, true));
         monster.getMap().broadcastMessage(MobPacket.BossPapuLatus.PapulLatusTimePatten(0, monster.getMap().Papullatushour, monster.getMap().Papullatusminute, monster.getMap().Mapcoltime, (int)monster.getCustomValue0(241), (int)monster.getCustomValue0(242), (int)monster.getCustomValue0(243), (int)monster.getCustomValue0(244), (int)monster.getCustomValue0(245), (int)monster.getCustomValue0(246)));
      }

   }

   public final void spawnLinkMobsetHP(int id, int x, int y, long nowhp, long maxhp) {
      MapleMonster m = MapleLifeFactory.getMonster(id);
      m.setHp(nowhp);
      m.getStats().setHp(maxhp);
      m.setOwner(this.c.getPlayer().getId());
      this.c.getPlayer().getMap().spawnMonsterOnGroundBelow(m, new Point(x, y));
   }

   public final void EnterPunchKing() {
      this.c.getPlayer().removeSkillCustomInfo(993192800);
      this.c.getPlayer().removeSkillCustomInfo(993192801);
      this.warp(993192800, "sp");
      this.c.getPlayer().getMap().resetFully();
      server.Timer.EventTimer.getInstance().schedule(() -> {
         this.c.getSession().writeAndFlush(CField.environmentChange("Effect/EventEffect.img/ChallengeMission/Count/3", 16));
         this.c.getSession().writeAndFlush(SLFCGPacket.playSE("Sound/MiniGame.img/multiBingo/3"));
         server.Timer.EventTimer.getInstance().schedule(() -> {
            this.c.getSession().writeAndFlush(CField.environmentChange("Effect/EventEffect.img/ChallengeMission/Count/2", 16));
            this.c.getSession().writeAndFlush(SLFCGPacket.playSE("Sound/MiniGame.img/multiBingo/2"));
         }, 1000L);
         server.Timer.EventTimer.getInstance().schedule(() -> {
            this.c.getSession().writeAndFlush(CField.environmentChange("Effect/EventEffect.img/ChallengeMission/Count/1", 16));
            this.c.getSession().writeAndFlush(SLFCGPacket.playSE("Sound/MiniGame.img/multiBingo/1"));
         }, 2000L);
         server.Timer.EventTimer.getInstance().schedule(() -> {
            this.c.getSession().writeAndFlush(CField.environmentChange("Effect/EventEffect.img/ChallengeMission/start", 16));
            this.c.getSession().writeAndFlush(SLFCGPacket.playSE("Sound/MiniGame.img/multiBingo/start"));
            server.Timer.EventTimer.getInstance().schedule(() -> {
               this.c.send(CField.getNowClock(13));
               this.c.send(CField.PunchKingPacket(this.c.getPlayer(), 1, 1));
               this.c.getPlayer().setSkillCustomInfo(993192800, 0L, 120000L);
               this.c.getPlayer().setSkillCustomInfo(993192801, 1L, 0L);

               int i;
               for(i = 9833935; i <= 9833946; ++i) {
                  this.c.getPlayer().removeSkillCustomInfo(i);
               }

               for(i = 9833947; i <= 9833958; ++i) {
                  this.c.getPlayer().removeSkillCustomInfo(i);
               }

               this.c.setCustomData(100795, "point", "0");
               MapleMonster boss = MapleLifeFactory.getMonster(9833935);
               boss.setOwner(this.c.getPlayer().getId());
               this.c.getPlayer().getMap().spawnMonsterOnGroundBelow(boss, new Point(20, 581));

               for(int i2 = -140; i2 <= 180; i2 += 80) {
                  MapleMonster mob = MapleLifeFactory.getMonster(9833947);
                  mob.setOwner(this.c.getPlayer().getId());
                  this.c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9833947), new Point(i2, 581));
               }

            }, 1000L);
         }, 3000L);
      }, 2000L);
   }

   public void JoinNubGuild() {
      MapleGuild guild = World.Guild.getGuild(1);
      if (guild != null) {
         this.c.getPlayer().setGuildId(guild.getId());
         this.c.getPlayer().setGuildRank((byte)3);
         this.c.getPlayer().saveGuildStatus();
         guild.addGuildMember(this.c.getPlayer(), guild);
         this.c.send(CWvsContext.GuildPacket.showGuildInfo(this.c.getPlayer()));
         this.c.getPlayer().getMap().broadcastMessage(CField.loadGuildIcon(this.c.getPlayer()));
         this.c.send(CWvsContext.GuildPacket.guildLoadAattendance());
         this.c.getPlayer().dropMessage(5, "`" + guild.getName() + "` 길드에 가입 되었습니다.");
         this.c.send(CWvsContext.GuildPacket.showGuildInfo(this.c.getPlayer()));
         this.c.getSession().writeAndFlush(CWvsContext.GuildPacket.guildLoadAattendance());
      }

   }

   public final void EnterErda() {
      MapleMap map = this.c.getChannelServer().getMapFactory().getMap(450001400);
      map.resetFully();
      map.setCustomInfo(450001400, 1, 0);
      map.killAllMonsters(true);
      this.warp(450001400, "sp");
      this.c.getPlayer().setSkillCustomInfo(450001401, 0L, (long)Randomizer.rand(5000, 7000));
      this.c.send(SLFCGPacket.ErdaSpectrumGauge(0, 0, 0));
      this.c.send(SLFCGPacket.ErdaSpectrumType(2));
      this.c.send(SLFCGPacket.ErdaSpectrumSetting(600000, 10));
      this.c.send(CField.startMapEffect("   주변 몬스터를 사냥하면 획득할 수 있는 에르다를 모아 에르다 응집기를 가동해주세요!  ", 5120025, true));
      server.Timer.EtcTimer.getInstance().schedule(() -> {
         this.c.getPlayer().getMap().removeCustomInfo(450001400);
         this.c.send(CField.removeMapEffect());
      }, 3000L);
   }

   public void SaveDojoRank(MapleCharacter chr) {
      Connection con = null;
      PreparedStatement ps = null;

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("DELETE FROM dojorankings WHERE `playerid` = ?");
         ps.setInt(1, chr.getId());
         ps.executeUpdate();
         ps = con.prepareStatement("INSERT INTO dojorankings (`playerid`, `name`, `job`, `level`, `gender`, `skin`, `face`, `hair`, `addColor`, `baseProb`,`equip1`, `equip2`, `equip3`, `equip4`, `equip5`, `equip6`, `equip7`, `equip8`, `equip9`, `weaponstickerid`, `weaponid`, `subweaponid`, `floor`, `time`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
         ps.setInt(1, chr.getId());
         ps.setString(2, chr.getName());
         ps.setInt(3, chr.getJob());
         ps.setInt(4, chr.getLevel());
         ps.setInt(5, chr.getGender());
         ps.setInt(6, chr.getSkinColor());
         ps.setInt(7, chr.getFace());
         ps.setInt(8, chr.getBaseColor() != -1 ? chr.getHair() / 10 * 10 + chr.getBaseColor() : chr.getHair());
         ps.setInt(9, chr.getAddColor());
         ps.setInt(10, chr.getBaseProb());

         for(int i = 1; i <= 9; ++i) {
            int eq = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)(-100 - i)) != null ? chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)(-100 - i)).getItemId() : (chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)(-i)) != null ? chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)(-i)).getItemId() : -1);
            ps.setInt(10 + i, eq);
         }

         ps.setInt(20, chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-111) != null ? chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-111).getItemId() : 1702099);
         ps.setInt(21, chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11) != null ? chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11).getItemId() : -1);
         ps.setInt(22, chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10) != null ? chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10).getItemId() : -1);
         ps.setInt(23, (int)chr.getKeyValue(3, "dojo"));
         ps.setInt(24, (int)(1000L - chr.getKeyValue(3, "dojo_time")));
         ps.executeUpdate();
         ps.close();
      } catch (Exception var14) {
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

      chr.removeKeyValue(3);
   }

   public void ShowDojoRank() {
      Connection con = null;
      PreparedStatement ps = null;
      PreparedStatement ps1 = null;
      ResultSet rs = null;
      ResultSet rs1 = null;
      MapleCharacter chr = this.c.getPlayer();

      try {
         con = DatabaseConnection.getConnection();
         ps = con.prepareStatement("UPDATE dojorankings SET job = ?, level = ?, gender = ?, skin = ?, face = ?, hair = ?, addColor = ?, baseProb = ?, equip1 = ?, equip2 = ?, equip3 = ?, equip4 = ?, equip5 = ?, equip6 = ?, equip7 = ?, equip8 = ?, equip9 = ?, weaponstickerid = ?, weaponid = ?, subweaponid = ? WHERE playerid = ?");
         ps.setInt(1, chr.getJob());
         ps.setInt(2, chr.getLevel());
         ps.setInt(3, chr.getGender());
         ps.setInt(4, chr.getSkinColor());
         ps.setInt(5, chr.getFace());
         ps.setInt(6, chr.getBaseColor() != -1 ? chr.getHair() / 10 * 10 + chr.getBaseColor() : chr.getHair());
         ps.setInt(7, chr.getAddColor());
         ps.setInt(8, chr.getBaseProb());

         int count;
         for(count = 1; count <= 9; ++count) {
            int eq = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)(-100 - count)) != null ? chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)(-100 - count)).getItemId() : (chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)(-count)) != null ? chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)(-count)).getItemId() : -1);
            ps.setInt(8 + count, eq);
         }

         ps.setInt(18, chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-111) != null ? chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-111).getItemId() : 1702099);
         ps.setInt(19, chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11) != null ? chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11).getItemId() : -1);
         ps.setInt(20, chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10) != null ? chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10).getItemId() : -1);
         ps.setInt(21, chr.getId());
         ps.executeUpdate();
         ps = con.prepareStatement("SELECT * FROM dojorankings order by floor DESC, time DESC", 1005, 1008);
         rs = ps.executeQuery();
         rs.last();
         count = rs.getRow();
         rs.first();
         ps1 = con.prepareStatement("SELECT * FROM dojorankings order by floor DESC, time DESC", 1005, 1008);
         rs1 = ps1.executeQuery();
         rs1.last();
         rs1.first();
         this.c.send(CField.DojangRank(rs, rs1, count));
         rs.close();
         ps.close();
         con.close();
      } catch (SQLException var19) {
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

            if (rs1 != null) {
               rs.close();
            }

            if (ps1 != null) {
               ps1.close();
            }
         } catch (SQLException var18) {
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

            if (rs1 != null) {
               rs.close();
            }

            if (ps1 != null) {
               ps1.close();
            }
         } catch (SQLException var17) {
         }

      }

   }

   public final void MakePmdrItem(int itemid, int count) {
      if (this.c.getPlayer().getKeyValue(800023, "indiepmer") <= 0L) {
         this.c.getPlayer().setKeyValue(800023, "indiepmer", "0");
      }

      this.c.getPlayer().setKeyValue(800023, "indiepmer", String.valueOf(this.c.getPlayer().getKeyValue(800023, "indiepmer") + (long)count));
   }

   public final void MakePmdrItem2(int itemid, int count) {
      if (this.c.getPlayer().getKeyValue(800023, "indiepmer2") <= 0L) {
         this.c.getPlayer().setKeyValue(800023, "indiepmer2", "0");
      }

      this.c.getPlayer().setKeyValue(800023, "indiepmer2", String.valueOf(this.c.getPlayer().getKeyValue(800023, "indiepmer2") + (long)count));
   }

   public final void MakePmdrItem3(int itemid, int count) {
      if (this.c.getPlayer().getKeyValue(800023, "indiepmer3") <= 0L) {
         this.c.getPlayer().setKeyValue(800023, "indiepmer3", "0");
      }

      this.c.getPlayer().setKeyValue(800023, "indiepmer3", String.valueOf(this.c.getPlayer().getKeyValue(800023, "indiepmer3") + (long)count));
   }
}
