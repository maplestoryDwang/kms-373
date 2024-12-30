package handling.channel.handler;

import client.Core;
import client.DreamBreakerRank;
import client.InnerSkillValueHolder;
import client.MapleCabinet;
import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.MapleCoolDownValueHolder;
import client.MapleMannequin;
import client.MapleQuestStatus;
import client.MapleStat;
import client.MapleUnion;
import client.MatrixSkill;
import client.PlayerStats;
import client.RangeAttack;
import client.SecondAtom2;
import client.SecondaryStat;
import client.SecondaryStatValueHolder;
import client.Skill;
import client.SkillFactory;
import client.SkillMacro;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleAndroid;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import constants.ServerConstants;
import database.DatabaseConnection;
import handling.RecvPacketOpcode;
import handling.SendPacketOpcode;
import handling.channel.ChannelServer;
import handling.login.LoginInformationProvider;
import handling.world.World;
import handling.world.party.MapleParty;
import handling.world.party.MaplePartyCharacter;
import io.netty.channel.Channel;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import scripting.EventInstanceManager;
import scripting.NPCScriptManager;
import server.AdelProjectile;
import server.CashItemFactory;
import server.CashItemInfo;
import server.ChatEmoticon;
import server.DailyGiftItemInfo;
import server.InnerAbillity;
import server.MapleChatEmoticon;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.MapleSavedEmoticon;
import server.Obstacle;
import server.Randomizer;
import server.SecondaryStatEffect;
import server.Timer;
import server.events.MapleSnowball;
import server.field.boss.will.SpiderWeb;
import server.field.skill.MapleFieldAttackObj;
import server.field.skill.MapleMagicSword;
import server.field.skill.MapleMagicWreck;
import server.field.skill.MapleSecondAtom;
import server.field.skill.SecondAtom;
import server.field.skill.SpecialPortal;
import server.games.BloomingRace;
import server.games.DetectiveGame;
import server.life.Ignition;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MobAttackInfo;
import server.life.MobAttackInfoFactory;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.FieldLimitType;
import server.maps.ForceAtom;
import server.maps.MapleAtom;
import server.maps.MapleFoothold;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleMist;
import server.maps.MapleSpecialChair;
import server.maps.MapleSummon;
import server.maps.SummonMovementType;
import server.movement.LifeMovementFragment;
import server.polofritto.FrittoDancing;
import server.polofritto.FrittoEagle;
import server.quest.MapleQuest;
import tools.AttackPair;
import tools.CurrentTime;
import tools.FileoutputUtil;
import tools.Pair;
import tools.Triple;
import tools.data.LittleEndianAccessor;
import tools.data.MaplePacketLittleEndianWriter;
import tools.packet.CField;
import tools.packet.CSPacket;
import tools.packet.CWvsContext;
import tools.packet.MobPacket;
import tools.packet.PacketHelper;
import tools.packet.SLFCGPacket;
import tools.packet.SkillPacket;

public class PlayerHandler {
   public static long connectorCheckLong;
   static int Rank = 0;

   public static long getConnectorCheckLong() {
      ++connectorCheckLong;
      return connectorCheckLong;
   }

   public static long resetConnectorCheckLong() {
      connectorCheckLong = 0L;
      return connectorCheckLong;
   }

   public static boolean isFinisher(int skillid) {
      switch(skillid) {
      case 400011027:
         return true;
      default:
         return false;
      }
   }

   public static void ChangeSkillMacro(LittleEndianAccessor slea, MapleCharacter chr) {
      int num = slea.readByte();

      for(int i = 0; i < num; ++i) {
         String name = slea.readMapleAsciiString();
         int shout = slea.readByte();
         int skill1 = slea.readInt();
         int skill2 = slea.readInt();
         int skill3 = slea.readInt();
         SkillMacro macro = new SkillMacro(skill1, skill2, skill3, name, shout, i);
         chr.updateMacros(i, macro);
      }

   }

   public static final void ChangeKeymap(LittleEndianAccessor slea, MapleCharacter chr) {
      int type;
      int data;
      if (slea.available() > 8L && chr != null) {
         slea.skip(4);
         type = slea.readInt();
         if (type != 0) {
            return;
         }

         data = slea.readInt();

         for(int i = 0; i < data; ++i) {
            int key = slea.readInt();
            byte type = slea.readByte();
            int action = slea.readInt();
            if (type == 1 && action >= 1000) {
               Skill skil = SkillFactory.getSkill(action);
               if (skil != null && (!skil.isFourthJob() && !skil.isBeginnerSkill() && skil.isInvisible() && chr.getSkillLevel(skil) <= 0 || action >= 91000000 && action < 100000000)) {
                  continue;
               }
            }

            if (action != 26) {
               chr.changeKeybinding(key, type, action);
            }
         }
      } else if (chr != null) {
         type = slea.readInt();
         data = slea.readInt();
         switch(type) {
         case 1:
            if (data <= 0) {
               chr.getQuest_Map().remove(MapleQuest.getInstance(122221));
            } else {
               chr.getQuestNAdd(MapleQuest.getInstance(122221)).setCustomData(String.valueOf(data));
            }
            break;
         case 2:
            if (data <= 0) {
               chr.getQuest_Map().remove(MapleQuest.getInstance(122223));
            } else {
               chr.getQuestNAdd(MapleQuest.getInstance(122223)).setCustomData(String.valueOf(data));
            }
         }
      }

   }

   public static final void UseTitle(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      if (chr != null && chr.getMap() != null) {
         int itemId = slea.readInt();
         Item toUse = chr.getInventory(MapleInventoryType.SETUP).getItem((short)slea.readInt());
         if (toUse != null && (itemId == 0 || toUse.getItemId() == itemId)) {
            if (itemId <= 0) {
               chr.setKeyValue(19019, "id", "0");
               chr.setKeyValue(19019, "date", "0");
            } else {
               chr.setKeyValue(19019, "expired", "0");
               chr.setKeyValue(19019, "id", String.valueOf(itemId));
               chr.setKeyValue(19019, "date", "2079/01/01 00:00:00:000");
               chr.setQuestAdd(MapleQuest.getInstance(7290), (byte)1, String.valueOf(itemId));
            }

            chr.getMap().broadcastMessage(chr, CField.showTitle(chr.getId(), itemId), false);
            c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
            chr.getStat().recalcLocalStats(chr);
         }
      }
   }

   public static final void UseChair(int itemId, MapleClient c, MapleCharacter chr, LittleEndianAccessor slea) {
      int index = slea.readInt();
      slea.skip(1);
      Point pos = new Point(slea.readInt(), slea.readInt());
      if (chr != null && chr.getMap() != null) {
         if (GameConstants.isTextChair(itemId)) {
            String Special = slea.readMapleAsciiString();
            chr.setChairText(Special);
         }

         int quantity;
         if (itemId == 3015440 || itemId == 3015650 || itemId == 3015651) {
            quantity = slea.readInt();
            chr.getMap().broadcastMessage(SLFCGPacket.MesoChairPacket(chr.getId(), chr.getMesoChairCount(), itemId));
            ScheduledFuture<?> qwer = Timer.ShowTimer.getInstance().register(() -> {
               if (chr != null && chr.getChair() != 0) {
                  chr.UpdateMesoChairCount(quantity);
               }

            }, 2000L);
            chr.setMesoChairTimer(qwer);
         }

         chr.setChair(itemId);
         if (itemId / 100 == 30162) {
            List<MapleSpecialChair.MapleSpecialChairPlayer> players = new ArrayList();
            MapleSpecialChair chair = new MapleSpecialChair(itemId, new Rectangle(pos.x - 142, pos.y - 410, 284, 420), pos, chr, players);
            int[] randEmotions = new int[]{2, 10, 14, 17};
            chair.addPlayer(chr, randEmotions[Randomizer.nextInt(randEmotions.length)]);
            chair.addPlayer((MapleCharacter)null, -1);
            chair.addPlayer((MapleCharacter)null, -1);
            chair.addPlayer((MapleCharacter)null, -1);
            chr.getMap().spawnSpecialChair(chair);
         }

         chr.getMap().broadcastMessage(chr, CField.showChair(chr, itemId), false);
         if (itemId == FishingHandler.FishingChair && chr.getMapId() == FishingHandler.FishingMap) {
            quantity = chr.getItemQuantity(4035000, false);
            if (quantity > 0) {
               chr.setKeyValue(100393, "progress", "1");
               chr.setKeyValue(100393, "4035000", String.valueOf(quantity));
               c.getSession().writeAndFlush(CField.fishing(0));
            }
         }

         if (chr.getMapId() == ServerConstants.fishMap) {
            chr.lastChairPointTime = System.currentTimeMillis();
            chr.getClient().getSession().writeAndFlush(CField.UIPacket.detailShowInfo("의자에 앉아 있을시 1분마다 5포인트가 자동으로 수급됩니다.", false));
            chr.getClient().getSession().writeAndFlush(SLFCGPacket.playSE("Sound/MiniGame.img/14thTerra/reward"));
         }

         if (chr.getMapId() == 100000000) {
            chr.lastChairPointTime = System.currentTimeMillis();
            chr.getClient().getSession().writeAndFlush(CField.UIPacket.detailShowInfo("의자에 앉아 있을 시 1분마다 네오 젬 1개를 획득합니다.", false));
            chr.getClient().getSession().writeAndFlush(SLFCGPacket.playSE("Sound/MiniGame.img/14thTerra/reward"));
         }

         c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
      }
   }

   public static final void CancelChair(short id, MapleClient c, MapleCharacter chr) {
      if (id == -1) {
         int itemId = chr.getChair();
         if (chr.getMesoChairTimer() != null) {
            chr.getMesoChairTimer().cancel(true);
            chr.setMesoChairTimer((ScheduledFuture)null);
         }

         chr.setChairText((String)null);
         chr.setChair(0);
         if (itemId == 3010587 && chr.getBuffedValue(SecondaryStat.OnCapsule) != null) {
            chr.cancelEffect(chr.getBuffedEffect(SecondaryStat.OnCapsule));
         }

         if (itemId / 100 == 30162) {
            Iterator var4 = chr.getMap().getAllSpecialChairs().iterator();

            while(var4.hasNext()) {
               MapleSpecialChair chair = (MapleSpecialChair)var4.next();
               Iterator var6;
               MapleSpecialChair.MapleSpecialChairPlayer player;
               if (chair.getOwner().getId() == chr.getId()) {
                  chair.getPlayers().remove(chr);
                  var6 = chair.getPlayers().iterator();

                  while(var6.hasNext()) {
                     player = (MapleSpecialChair.MapleSpecialChairPlayer)var6.next();
                     if (player.getPlayer() != null) {
                        MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterById(player.getPlayer().getId());
                        if (target != null) {
                           target.setChair(0);
                           target.getMap().broadcastMessage(CField.specialChair(target, false, false, false, chair));
                           target.getClient().getSession().writeAndFlush(CField.cancelChair(-1, target));
                           if (target.getMap() != null) {
                              target.getMap().broadcastMessage(target, CField.showChair(target, 0), false);
                           }
                        }
                     }
                  }

                  chr.getMap().broadcastMessage(CField.specialChair(chr, false, false, false, chair));
                  chr.getMap().removeMapObject(chair);
                  break;
               }

               var6 = chair.getPlayers().iterator();

               while(var6.hasNext()) {
                  player = (MapleSpecialChair.MapleSpecialChairPlayer)var6.next();
                  if (player.getPlayer() != null && player.getPlayer().getId() == chr.getId()) {
                     player.setPlayer((MapleCharacter)null);
                     player.setEmotion(-1);
                     chr.getMap().broadcastMessage(CField.specialChair(chr, false, false, false, chair));
                  }
               }
            }
         }

         c.getSession().writeAndFlush(CField.cancelChair(-1, chr));
         if (chr.getMap() != null) {
            chr.getMap().broadcastMessage(chr, CField.showChair(chr, 0), false);
         }
      } else {
         chr.setChair(id);
         c.getSession().writeAndFlush(CField.cancelChair(id, chr));
      }

      if (chr.getMapId() == ServerConstants.fishMap) {
         chr.getClient().getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(3003302, 2500, "#face1#자동 포인트 수급이 종료됐어요!", ""));
         chr.getClient().getSession().writeAndFlush(SLFCGPacket.playSE("Sound/MiniGame.img/Timer"));
      }

      if (chr.getMapId() == 100000000) {
         c.send(CField.UIPacket.detailShowInfo("휴식 포인트 적립을 그만둡니다.", 3, 20, 20));
         chr.removeSkillCustomInfo(chr.getMapId());
      }

      c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
   }

   public static final void TrockAddMap(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      byte addrem = slea.readByte();
      byte vip = slea.readByte();
      if (vip == 1) {
         if (addrem == 0) {
            chr.deleteFromRegRocks(slea.readInt());
         } else if (addrem == 1) {
            if (!FieldLimitType.VipRock.check(chr.getMap().getFieldLimit())) {
               chr.addRegRockMap();
            } else {
               chr.dropMessage(1, "This map is not available to enter for the list.");
            }
         }
      } else if (vip == 2) {
         if (addrem == 0) {
            chr.deleteFromRocks(slea.readInt());
         } else if (addrem == 1) {
            if (!FieldLimitType.VipRock.check(chr.getMap().getFieldLimit())) {
               chr.addRockMap();
            } else {
               chr.dropMessage(1, "This map is not available to enter for the list.");
            }
         }
      } else if (vip == 3) {
         if (addrem == 0) {
            chr.deleteFromHyperRocks(slea.readInt());
         } else if (addrem == 1) {
            if (!FieldLimitType.VipRock.check(chr.getMap().getFieldLimit())) {
               chr.addHyperRockMap();
            } else {
               chr.dropMessage(1, "This map is not available to enter for the list.");
            }
         }
      }

      c.getSession().writeAndFlush(CSPacket.OnMapTransferResult(chr, vip, addrem == 0));
   }

   public static final void CharInfoRequest(int objectid, MapleClient c, MapleCharacter chr) {
      if (c.getPlayer() != null && c.getPlayer().getMap() != null) {
         MapleCharacter player = c.getPlayer().getMap().getCharacterById(objectid);
         if (player != null) {
            c.getSession().writeAndFlush(CWvsContext.charInfo(player, c.getPlayer().getId() == objectid));
            chr.setLastCharGuildId(player.getGuildId());
            byte[] img = player.getClient().getFarmImg();
            if (img != null) {
               c.getSession().writeAndFlush(CField.getPhotoResult(player.getClient(), img));
            }
         }

      }
   }

   public static final void TakeDamage(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      slea.skip(4);
      slea.skip(4);
      byte type = slea.readByte();
      slea.skip(1);
      slea.skip(1);
      int damage = slea.readInt();
      slea.skip(2);
      boolean isDeadlyAttack = false;
      boolean pPhysical = false;
      int oid = 0;
      int monsteridfrom = 0;
      int fake = 0;
      int mpattack = 0;
      int skillid = 0;
      int pID = 0;
      int pDMG = 0;
      byte direction = 0;
      byte pType = 0;
      Point pPos = new Point(0, 0);
      MapleMonster attacker = null;
      if (chr != null && chr.getMap() != null) {
         if ((!chr.isGM() || !chr.isInvincible()) && !chr.getBuffedValue(1320019)) {
            if (c.getPlayer().getBattleGroundChr() != null && c.getPlayer().getMapId() == 921174100) {
               if (c.getPlayer().getBattleGroundChr().getHp() - damage > 0) {
                  EnumMap<MapleStat, Long> hpmpupdate = new EnumMap(MapleStat.class);
                  c.getPlayer().getBattleGroundChr().setHp(c.getPlayer().getBattleGroundChr().getHp() - damage);
                  hpmpupdate.put(MapleStat.HP, (long)c.getPlayer().getBattleGroundChr().getHp());
                  if (hpmpupdate.size() > 0) {
                     c.getSession().writeAndFlush(CWvsContext.updatePlayerStats(hpmpupdate, false, c.getPlayer()));
                  }
               }

               c.getPlayer().getMap().broadcastMessage(CField.damagePlayer(240, damage, c.getPlayer().getId(), damage));
            } else {
               if (type == -5 && c.getPlayer().getBuffedValue(SecondaryStat.DarkSight) != null && !c.getPlayer().getBuffedValue(400001023) && c.getPlayer().getSkillCustomValue0(4001003) < 5L) {
                  c.getPlayer().setSkillCustomInfo(4001003, c.getPlayer().getSkillCustomValue0(4001003) + 1L, 0L);
               }

               if (damage > 0 && chr.getBuffedEffect(SecondaryStat.HolyMagicShell) != null) {
                  if (chr.getHolyMagicShell() > 1) {
                     chr.setHolyMagicShell((byte)(chr.getHolyMagicShell() - 1));
                     HashMap<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
                     statups.put(SecondaryStat.HolyMagicShell, new Pair(Integer.valueOf(chr.getHolyMagicShell()), (int)chr.getBuffLimit(chr.getBuffSource(SecondaryStat.HolyMagicShell))));
                     chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, chr.getBuffedEffect(SecondaryStat.HolyMagicShell), chr));
                  } else {
                     chr.cancelEffectFromBuffStat(SecondaryStat.HolyMagicShell);
                  }
               }

               int bof;
               if (Randomizer.isSuccess(50) && GameConstants.isXenon(chr.getJob()) && chr.getBuffedValue(SecondaryStat.AegisSystem) != null && chr.getSkillCustomValue(36110004) == null) {
                  MapleAtom atom = new MapleAtom(false, chr.getId(), 5, true, 36110004, chr.getTruePosition().x, chr.getTruePosition().y);
                  atom.setDwFirstTargetId(oid);
                  atom.addForceAtom(new ForceAtom(0, 35, 5, Randomizer.rand(80, 120), (short)Randomizer.rand(0, 500)));
                  atom.addForceAtom(new ForceAtom(0, 36, 5, Randomizer.rand(80, 120), (short)Randomizer.rand(0, 500)));
                  atom.addForceAtom(new ForceAtom(0, 37, 5, Randomizer.rand(80, 120), (short)Randomizer.rand(0, 500)));
                  if (chr.getSummon(400041044) != null && (new Rectangle(chr.getSummon(400041044).getTruePosition().x - 320, chr.getSummon(400041044).getTruePosition().y - 490, 640, 530)).contains(chr.getTruePosition())) {
                     for(bof = 0; bof < 5; ++bof) {
                        atom.addForceAtom(new ForceAtom(0, 38 + bof, 5, Randomizer.rand(80, 120), (short)Randomizer.rand(0, 500)));
                     }
                  }

                  c.getPlayer().getMap().spawnMapleAtom(atom);
                  chr.setSkillCustomInfo(36110004, 0L, 1500L);
               }

               chr.checkSpecialCoreSkills("hitCount", 0, (SecondaryStatEffect)null);
               PlayerStats stats = chr.getStat();
               SecondaryStatEffect eff;
               Skill bx;
               SecondaryStatEffect counterAttack;
               int dam;
               boolean damaged;
               if (type > -2) {
                  slea.skip(28);
                  slea.readInt();
                  slea.readInt();
                  monsteridfrom = slea.readInt();
                  slea.readByte();
                  slea.readInt();
                  slea.readInt();
                  int oid = slea.readInt();
                  attacker = chr.getMap().getMonsterByOid(oid);
                  direction = slea.readByte();
                  if (attacker == null) {
                     chr.addHP((long)(-damage));
                     MapleMonster ordi = MapleLifeFactory.getMonster(monsteridfrom);
                     MobAttackInfo attackInfo2 = MobAttackInfoFactory.getMobAttackInfo(ordi, 0);
                     if (attackInfo2 != null && attackInfo2.getDiseaseSkill() != 0) {
                        MobSkillFactory.getMobSkill(attackInfo2.getDiseaseSkill(), attackInfo2.getDiseaseLevel()).applyEffect(chr, ordi, false, false);
                     }

                     chr.getMap().broadcastMessage(chr, CField.damagePlayer(chr.getId(), type, damage, monsteridfrom, direction, skillid, pDMG, pPhysical, pID, pType, pPos, (byte)0, 0, fake), false);
                     return;
                  }

                  if (attacker.getId() != monsteridfrom || attacker.getLinkCID() > 0 || attacker.isFake() || attacker.getStats().isFriendly()) {
                     return;
                  }

                  if (chr.getBuffedValue(400051009) && (double)chr.getStat().getCurrentMaxHp() * 0.9D <= (double)damage) {
                     chr.cancelEffectFromBuffStat(SecondaryStat.IndieSummon, 400051009);
                     return;
                  }

                  MobAttackInfo attackInfo;
                  int 실드량;
                  if (chr.getBuffedValue(400031030) && (attackInfo = MobAttackInfoFactory.getMobAttackInfo(attacker, type)) != null) {
                     실드량 = attackInfo.getFixDamR();
                     dam = Math.max(0, chr.getBuffedValue(SecondaryStat.WindWall) - 실드량 * chr.getBuffedEffect(SecondaryStat.WindWall).getZ());
                     if (dam > 1) {
                        chr.setBuffedValue(SecondaryStat.WindWall, dam);
                        HashMap<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
                        statups.put(SecondaryStat.WindWall, new Pair(dam, (int)chr.getBuffLimit(400031030)));
                        chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, chr.getBuffedEffect(SecondaryStat.WindWall), chr));
                     } else {
                        chr.cancelEffectFromBuffStat(SecondaryStat.WindWall);
                     }

                     return;
                  }

                  SecondaryStatEffect eff;
                  if (chr.getBuffedEffect(SecondaryStat.SilhouetteMirage) != null && chr.silhouetteMirage > 0) {
                     eff = chr.getBuffedEffect(SecondaryStat.SilhouetteMirage);
                     MobAttackInfo attackInfo3 = MobAttackInfoFactory.getMobAttackInfo(attacker, type);
                     if (attackInfo3 != null && (attackInfo3.getFixDamR() >= 50 || chr.getStat().getHp() - (long)damage <= 0L)) {
                        --chr.silhouetteMirage;
                        eff.applyTo(chr, false);
                        return;
                     }
                  }

                  if (c.getPlayer().getBuffedValue(162120038)) {
                     실드량 = (int)chr.getSkillCustomValue0(162120038);
                     if (실드량 - damage <= 0) {
                        damage -= 실드량;
                        chr.cancelEffect(chr.getBuffedEffect(162120038));
                     } else {
                        chr.setSkillCustomInfo(162120038, (long)(실드량 - damage), 0L);
                        chr.getBuffedEffect(162120038).applyTo(chr, false, (int)chr.getBuffLimit(162120038));
                        damage = 0;
                     }
                  }

                  SecondaryStatEffect effect6;
                  if (damage > 0 && chr.getSkillLevel(5120011) > 0) {
                     effect6 = SkillFactory.getSkill(5120011).getEffect(chr.getSkillLevel(5120011));
                     if (Randomizer.isSuccess(effect6.getProp())) {
                        chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, 5120011, 4, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), true, chr.getPosition(), (String)null, (Item)null));
                        chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 0, 5120011, 4, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), false, chr.getPosition(), (String)null, (Item)null), false);
                        dam = damage * effect6.getY() / 100;
                        damage -= dam;
                     }
                  }

                  if (damage > 0 && chr.getSkillLevel(5110010) > 0) {
                     effect6 = SkillFactory.getSkill(5110010).getEffect(chr.getSkillLevel(5110010));
                     dam = damage * effect6.getDamAbsorbShieldR() / 100;
                     damage -= dam;
                  }

                  if (chr.getBuffedValue(SecondaryStat.SilhouetteMirage) != null) {
                     effect6 = SkillFactory.getSkill(400031053).getEffect(chr.getSkillLevel(400031053));
                     if ((long)damage >= chr.getStat().getCurrentMaxHp() / 100L * (long)effect6.getY() && chr.getSkillCustomValue0(400031053) > 0L) {
                        dam = damage * effect6.getQ() / 100;
                        damage -= dam;
                        chr.setSkillCustomInfo(400031053, chr.getSkillCustomValue0(400031053) - 1L, 0L);
                        chr.getBuffedEffect(400031053).applyTo(chr);
                     }
                  }

                  long duration;
                  if (chr.getBuffedValue(SecondaryStat.DemonDamageAbsorbShield) != null && damage > 0 && chr.getSkillCustomValue0(400001016) > 0L) {
                     effect6 = SkillFactory.getSkill(400001016).getEffect(chr.getSkillLevel(400001013));
                     chr.setSkillCustomInfo(400001016, chr.getSkillCustomValue0(400001016) - 1L, 0L);
                     if (chr.getSkillCustomValue0(400001016) <= 0L) {
                        chr.cancelEffectFromBuffStat(SecondaryStat.DemonDamageAbsorbShield);
                     } else {
                        duration = chr.getBuffLimit(400001016);
                        effect6.applyTo(chr, (int)duration);
                     }
                  }

                  long duration;
                  if (damage > 0 && attacker.getId() == 8870100 && attacker.getCustomValue0(8870100) > 0L) {
                     duration = attacker.getStats().getHp() / 100L * 5L;
                     attacker.heal((long)((int)duration), 0L, false);
                     attacker.getMap().broadcastMessage(MobPacket.HillaDrainActive(attacker.getObjectId()));
                     attacker.getMap().broadcastMessage(MobPacket.showBossHP(attacker));
                  }

                  HashMap statups;
                  if (chr.getSpiritGuard() > 0 && damage > 0) {
                     chr.setSpiritGuard(chr.getSpiritGuard() - 1);
                     if (chr.getSpiritGuard() == 0) {
                        chr.cancelEffectFromBuffStat(SecondaryStat.SpiritGuard);
                     } else {
                        statups = new HashMap();
                        statups.put(SecondaryStat.SpiritGuard, new Pair(chr.getSpiritGuard(), (int)chr.getBuffLimit(chr.getBuffSource(SecondaryStat.SpiritGuard))));
                        chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, chr.getBuffedEffect(SecondaryStat.SpiritGuard), chr));
                     }
                  }

                  if (chr.getBuffedValue(50001214)) {
                     SkillFactory.getSkill(50001214).getEffect(chr.getSkillLevel(50001214)).applyTo(chr, (int)chr.getBuffLimit(50001214));
                  }

                  if (chr.getBuffedValue(36121007)) {
                     chr.cancelEffectFromBuffStat(SecondaryStat.OnCapsule);
                  }

                  if (chr.getBuffedValue(151111005)) {
                     if (GameConstants.isAdel(chr.getJob()) && chr.getBuffedOwner(151111005) == chr.getId()) {
                        damaged = false;
                        실드량 = damage / 100 * chr.getBuffedEffect(151111005).getY();
                        c.send(CField.Novilityshiled(실드량));
                     } else {
                        MapleCharacter leader = chr.getMap().getCharacterById(chr.getBuffedOwner(151111005));
                        if (leader != null && leader.getBuffedValue(151111005) && (long)(실드량 = damage / 100 * leader.getBuffedEffect(151111005).getX()) < leader.getStat().getHp()) {
                           leader.addHP((long)(-실드량));
                           leader.getClient().send(CField.Novilityshiled(damage / 100 * leader.getBuffedEffect(151111005).getY()));
                        }
                     }
                  }

                  if (!attacker.getStats().isBoss() && chr.getBuffedValue(142001007) && chr.PPoint > 0) {
                     damage = (int)((double)damage - Math.floor((double)(damage * 60 / 100)));
                     chr.givePPoint(142001007);
                  }

                  if (!attacker.getStats().isBoss() && chr.getBuffedValue(SecondaryStat.DamAbsorbShield) != null) {
                     실드량 = chr.getBuffedEffect(SecondaryStat.DamAbsorbShield).getX();
                     if (chr.getSkillLevel(23120046) > 0) {
                        실드량 += SkillFactory.getSkill(23120046).getEffect(1).getX();
                     }

                     damage = (int)((double)damage - Math.floor((double)(damage * 실드량 / 100)));
                  }

                  MapleCharacter curChar;
                  if (chr.getBuffedValue(51111008) && chr.getBuffedOwner(51111008) != chr.getId()) {
                     int dam = false;
                     curChar = chr.getMap().getCharacterById(chr.getBuffedOwner(51111008));
                     if (curChar != null && curChar.getBuffedValue(51111008) && (dam = damage / 100 * 20) > 0 && (long)(실드량 = damage / 100 * curChar.getBuffedEffect(51111008).getQ()) < curChar.getStat().getHp() && (long)실드량 < curChar.getStat().getCurrentMaxHp() * (long)curChar.getBuffedEffect(51111008).getV()) {
                        curChar.getClient().send(CField.DamagePlayer2(실드량));
                        damage -= dam;
                     }
                  }

                  if (chr.getBuffedEffect(SecondaryStat.BodyOfSteal) != null && chr.bodyOfSteal < (eff = chr.getBuffedEffect(SecondaryStat.BodyOfSteal)).getY()) {
                     ++chr.bodyOfSteal;
                     statups = new HashMap();
                     statups.put(SecondaryStat.BodyOfSteal, new Pair(chr.bodyOfSteal, (int)chr.getBuffLimit(chr.getBuffSource(SecondaryStat.BodyOfSteal))));
                     chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, eff, chr));
                  }

                  SecondaryStatEffect revenge;
                  if (chr.getSkillLevel(1320011) > 0 && chr.getBuffedEffect(SecondaryStat.Beholder) != null && (revenge = SkillFactory.getSkill(1320011).getEffect(chr.getTotalSkillLevel(1320011))).makeChanceResult()) {
                     chr.getClient().getSession().writeAndFlush(CField.SummonPacket.BeholderRevengeAttack(chr, revenge.getDamage(), oid));
                  }

                  if (chr.getSkillLevel(400011047) > 0 && damage > 0 && (long)damage < chr.getStat().getCurrentMaxHp()) {
                     effect6 = SkillFactory.getSkill(400011047).getEffect(chr.getSkillLevel(400011047));
                     if (chr.getBuffedValue(400011047) && chr.getSkillCustomValue0(400011048) > 0L) {
                        duration = chr.getBuffLimit(400011047);
                        int shield = (int)chr.getSkillCustomValue0(400011048);
                        chr.setSkillCustomInfo(400011048, chr.getSkillCustomValue0(400011048) - (long)damage, 0L);
                        if (chr.getSkillCustomValue0(400011048) <= 0L) {
                           chr.setSkillCustomInfo(400011048, 0L, 0L);
                           damage -= shield;
                        } else {
                           damage = (int)((long)damage - chr.getSkillCustomValue0(400011048));
                        }

                        effect6.applyTo(chr, false, false);
                     }
                  }

                  if (chr.getBuffedValue(400011127) && chr.getSkillCustomValue0(400011127) > 0L && (long)damage < chr.getStat().getCurrentMaxHp()) {
                     duration = chr.getBuffLimit(SecondaryStat.IndieBarrier, 400011127);
                     chr.setSkillCustomInfo(400011127, chr.getSkillCustomValue0(400011127) - (long)damage, 0L);
                     damage = chr.getSkillCustomValue0(400011127) < (long)damage ? (int)((long)damage - chr.getSkillCustomValue0(400011127)) : 0;
                     if (chr.getSkillCustomValue0(400011127) <= 0L) {
                        chr.removeSkillCustomInfo(400011127);
                        chr.cancelEffectFromBuffStat(SecondaryStat.IndieBarrier);
                     } else {
                        chr.getBuffedEffect(400011127).applyTo(chr, false, (int)duration);
                     }
                  }

                  if (chr.getSkillLevel(37000006) > 0) {
                     실드량 = (int)chr.getSkillCustomValue0(37000006);
                     실드량 = chr.getSkillLevel(37120009) > 0 ? (damage /= 2) : damage / 100 * 4;
                     if ((long)실드량 > chr.getStat().getCurrentMaxHp()) {
                        실드량 = (int)chr.getStat().getCurrentMaxHp();
                     }

                     chr.setSkillCustomInfo(37000006, (long)실드량, 0L);
                     if (실드량 != 0) {
                        if (chr.getBuffedValue(37000006)) {
                           HashMap<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
                           statups.put(SecondaryStat.RwBarrier, new Pair((int)chr.getSkillCustomValue0(37000006), 0));
                           chr.getClient().send(CWvsContext.BuffPacket.giveBuff(statups, chr.getBuffedEffect(37000006), chr));
                        } else {
                           SkillFactory.getSkill(37000006).getEffect(chr.getSkillLevel(37000006)).applyTo(chr);
                        }
                     }
                  }

                  if (chr.getSkillLevel(5120011) > 0 && (counterAttack = SkillFactory.getSkill(5120011).getEffect(chr.getSkillLevel(5120011))).makeChanceResult()) {
                     counterAttack.applyTo(chr, false);
                  }

                  if (chr.getSkillLevel(5220012) > 0 && (counterAttack = SkillFactory.getSkill(5220012).getEffect(chr.getSkillLevel(5220012))).makeChanceResult()) {
                     counterAttack.applyTo(chr, false);
                  }

                  if (chr.getBuffedEffect(SecondaryStat.Dike) != null && chr.getBuffedEffect(151121011) == null && (long)((int)(System.currentTimeMillis() % 1000000000L)) - chr.getBuffedEffect(SecondaryStat.Dike).getStarttime() <= 1000L) {
                     damage = 0;
                     chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, 151121004, 10, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), true, chr.getPosition(), (String)null, (Item)null));
                     chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 0, 151121004, 10, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), false, chr.getPosition(), (String)null, (Item)null), false);
                  }

                  if (chr.getBuffedEffect(SecondaryStat.Etherealform) != null) {
                     if (GameConstants.isKinesis(chr.getJob())) {
                        chr.addHP((long)(-chr.getBuffedEffect(SecondaryStat.Etherealform).getY()));
                     } else {
                        chr.addMP((long)(-chr.getBuffedEffect(SecondaryStat.Etherealform).getX()));
                     }

                     damage = 0;
                  }

                  if (chr.getBuffedEffect(SecondaryStat.RoyalGuardPrepare) != null) {
                     c.getSession().writeAndFlush(CField.RoyalGuardDamage());
                     if (chr.getSkillLevel(51120003) > 0) {
                        SkillFactory.getSkill(51120003).getEffect(chr.getSkillLevel(51120003)).applyTo(chr, false);
                     }

                     if (chr.getRoyalStack() >= 3 && chr.getSkillLevel(51110009) > 0) {
                        if (chr.getRoyalStack() < 5 && chr.getBuffedValue(51001005)) {
                           chr.setRoyalStack((byte)(chr.getRoyalStack() + 1));
                        }

                        SkillFactory.getSkill(51001005).getEffect(chr.getSkillLevel(51001005)).applyTo(chr);
                     } else if (chr.getRoyalStack() <= 3 && chr.getSkillLevel(51001005) > 0) {
                        if (chr.getRoyalStack() < 3 && chr.getBuffedValue(51001005)) {
                           chr.setRoyalStack((byte)(chr.getRoyalStack() + 1));
                        }

                        SkillFactory.getSkill(51001005).getEffect(chr.getSkillLevel(51001005)).applyTo(chr);
                     }

                     chr.cancelEffectFromBuffStat(SecondaryStat.RoyalGuardPrepare);
                     if (chr.getParty() != null) {
                        Iterator var79 = chr.getParty().getMembers().iterator();

                        while(var79.hasNext()) {
                           MaplePartyCharacter chr1 = (MaplePartyCharacter)var79.next();
                           curChar = chr.getClient().getChannelServer().getPlayerStorage().getCharacterById(chr1.getId());
                           if (curChar != null && curChar.getBuffedValue(51111008)) {
                              curChar.cancelEffectFromBuffStat(SecondaryStat.MichaelSoulLink);
                              SkillFactory.getSkill(51111008).getEffect(chr.getSkillLevel(51111008)).applyTo(chr, curChar);
                           }
                        }
                     }
                  }

                  if (chr.getBuffedValue(400001050) && chr.getSkillCustomValue0(400001050) == 400001053L) {
                     effect6 = SkillFactory.getSkill(400001050).getEffect(chr.getSkillLevel(400001050));
                     chr.removeSkillCustomInfo(400001050);
                     duration = chr.getBuffLimit(400001050);
                     effect6.applyTo(chr, false, (int)duration);
                  }

                  if (type != -1 && damage > 0 && (attackInfo = MobAttackInfoFactory.getMobAttackInfo(attacker, type)) != null) {
                     MobSkill skill = MobSkillFactory.getMobSkill(attackInfo.getDiseaseSkill(), attackInfo.getDiseaseLevel());
                     if (skill != null && (damage == -1 || damage > 0)) {
                        skill.applyEffect(chr, attacker, false, attacker.isFacingLeft());
                     }

                     if (attacker.getId() == 8880504) {
                        if (chr.getSkillCustomValue0(80002625) == 1L) {
                           MobSkillFactory.getMobSkill(249, 1).applyEffect(chr, attacker, false, attacker.isFacingLeft());
                        } else if (chr.getSkillCustomValue0(80002625) == 2L) {
                           MobSkillFactory.getMobSkill(249, 2).applyEffect(chr, attacker, false, attacker.isFacingLeft());
                        }
                     }

                     if ((attacker.getId() == 8920002 || attacker.getId() == 8920102 && type == 1) && chr.hasDisease(SecondaryStat.FireBomb)) {
                        while(chr.hasDisease(SecondaryStat.FireBomb)) {
                           chr.cancelDisease(SecondaryStat.FireBomb);
                        }
                     }

                     attacker.setMp(attacker.getMp() - attackInfo.getMpCon());
                  }

                  skillid = slea.readInt();
                  pDMG = slea.readInt();
                  byte defType = slea.readByte();
                  slea.skip(1);
                  if (defType == 1 && (bof = chr.getTotalSkillLevel(bx = SkillFactory.getSkill(31110008))) > 0) {
                     eff = bx.getEffect(bof);
                     if (Randomizer.nextInt(100) <= eff.getX()) {
                        chr.addHP((long)eff.getY() * chr.getStat().getCurrentMaxHp() / 100L);
                        chr.handleForceGain(oid, 31110008);
                     }
                  }

                  if (skillid != 0 || chr.getSkillLevel(14120010) > 0) {
                     pPhysical = slea.readByte() > 0;
                     pID = slea.readInt();
                     pType = slea.readByte();
                     slea.skip(4);
                     if (slea.available() > 4L) {
                        pPos = slea.readPos();
                     }

                     if (pID != 14120010) {
                        attacker.damage(chr, (long)pDMG, true, skillid);
                     } else {
                        damage -= damage * SkillFactory.getSkill(pID).getEffect(chr.getSkillLevel(14120010)).getIgnoreMobDamR() / 100;
                     }

                     if (skillid == 31101003) {
                        attacker.applyStatus(c, MonsterStatus.MS_Stun, new MonsterStatusEffect(skillid, SkillFactory.getSkill(31101003).getEffect(chr.getSkillLevel(31101003)).getSubTime()), 1, SkillFactory.getSkill(31101003).getEffect(chr.getSkillLevel(31101003)));
                     }
                  }
               } else if (type == -3) {
                  if (chr.getMapId() == 993192600 && !chr.isGM()) {
                     c.getPlayer().giveDebuff(SecondaryStat.Stun, MobSkillFactory.getMobSkill(123, 94));
                  }

                  if (chr.getBuffedEffect(SecondaryStat.RoyalGuardPrepare) != null) {
                     c.getSession().writeAndFlush(CField.RoyalGuardDamage());
                     if (chr.getSkillLevel(51120003) > 0) {
                        SkillFactory.getSkill(51120003).getEffect(chr.getSkillLevel(51120003)).applyTo(chr, false);
                     }

                     if (chr.getRoyalStack() >= 3 && chr.getSkillLevel(51110009) > 0) {
                        if (chr.getRoyalStack() < 5) {
                           chr.setRoyalStack((byte)(chr.getRoyalStack() + 1));
                        }

                        SkillFactory.getSkill(51001005).getEffect(chr.getSkillLevel(51001005)).applyTo(chr);
                     } else if (chr.getRoyalStack() <= 3 && chr.getSkillLevel(51001005) > 0) {
                        if (chr.getRoyalStack() < 3) {
                           chr.setRoyalStack((byte)(chr.getRoyalStack() + 1));
                        }

                        SkillFactory.getSkill(51001005).getEffect(chr.getSkillLevel(51001005)).applyTo(chr);
                     }

                     chr.cancelEffectFromBuffStat(SecondaryStat.RoyalGuardPrepare);
                     if (chr.getParty() != null) {
                        Iterator var34 = chr.getParty().getMembers().iterator();

                        while(var34.hasNext()) {
                           MaplePartyCharacter chr1 = (MaplePartyCharacter)var34.next();
                           MapleCharacter curChar = chr.getClient().getChannelServer().getPlayerStorage().getCharacterById(chr1.getId());
                           if (curChar != null && curChar.getBuffedValue(51111008)) {
                              curChar.cancelEffectFromBuffStat(SecondaryStat.MichaelSoulLink);
                              SkillFactory.getSkill(51111008).getEffect(chr.getSkillLevel(51111008)).applyTo(chr, curChar);
                           }
                        }
                     }
                  }
               } else if (type == -4 && (c.getPlayer().getMapId() == 350060600 || c.getPlayer().getMapId() == 350060900)) {
                  c.getPlayer().giveDebuff(SecondaryStat.Slow, MobSkillFactory.getMobSkill(126, 3));
               }

               MapleCharacter buffowner;
               SecondaryStatEffect eff;
               if (damage == -1) {
                  if (chr.getBuffedValue(400041047)) {
                     buffowner = chr.getMap().getCharacterById(chr.getBuffedOwner(400041047));
                     if (buffowner != null) {
                        buffowner.gainXenonSurplus((short)1, SkillFactory.getSkill(400041047));
                     }
                  } else {
                     MapleCharacter leader;
                     if (chr.getBuffedValue(36121014) && (leader = chr.getMap().getCharacterById(chr.getBuffedOwner(36121014))) != null) {
                        leader.gainXenonSurplus((short)1, SkillFactory.getSkill(36121014));
                     }
                  }

                  if (GameConstants.isNightLord(chr.getJob())) {
                     fake = 4120002;
                  } else if (GameConstants.isShadower(chr.getJob())) {
                     fake = 4220002;
                  } else if (GameConstants.isMercedes(chr.getJob())) {
                     fake = chr.getSkillLevel(23110004) > 0 ? 23110004 : 23000001;
                  } else if (GameConstants.isPhantom(chr.getJob())) {
                     fake = 24110004;
                  } else if (GameConstants.isNightWalker(chr.getJob())) {
                     fake = 14120010;
                  } else if (GameConstants.isHoyeong(chr.getJob())) {
                     fake = 164101006;
                  } else if (GameConstants.isXenon(chr.getJob())) {
                     fake = 36120005;
                  } else if (GameConstants.isBowMaster(chr.getJob())) {
                     fake = 3120007;
                     if (chr.getSkillLevel(3110007) > 0 && SkillFactory.getSkill(3110007).getEffect(chr.getSkillLevel(3110007)).makeChanceResult()) {
                        chr.getClient().getSession().writeAndFlush(CField.getDotge());
                        chr.setSkillCustomInfo(3310005, 0L, 1000L);
                     }
                  } else if (GameConstants.isMarksMan(chr.getJob())) {
                     fake = 3220006;
                     if (chr.getSkillLevel(3210007) > 0 && SkillFactory.getSkill(3210007).getEffect(chr.getSkillLevel(3210007)).makeChanceResult()) {
                        chr.getClient().getSession().writeAndFlush(CField.getDotge());
                        chr.setSkillCustomInfo(3310005, 0L, 1000L);
                     }
                  } else if (GameConstants.isPathFinder(chr.getJob())) {
                     fake = 3320011;
                     if (chr.getSkillLevel(3310005) > 0 && SkillFactory.getSkill(3310005).getEffect(chr.getSkillLevel(3310005)).makeChanceResult()) {
                        chr.getClient().getSession().writeAndFlush(CField.getDotge());
                        chr.setSkillCustomInfo(3310005, 0L, 1000L);
                     }
                  } else if (GameConstants.isWildHunter(chr.getJob())) {
                     fake = 33101005;
                     if (chr.getSkillLevel(33110008) > 0) {
                        chr.getClient().getSession().writeAndFlush(CField.getDotge());
                        chr.setSkillCustomInfo(3310005, 0L, 1000L);
                     }
                  } else if (type == -1 && chr.getJob() == 122 && attacker != null && chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10) != null && chr.getTotalSkillLevel(1220006) > 0) {
                     eff = SkillFactory.getSkill(1220006).getEffect(chr.getTotalSkillLevel(1220006));
                     attacker.applyStatus(c, MonsterStatus.MS_Stun, new MonsterStatusEffect(1220006, eff.getDuration()), 1, eff);
                     fake = 1220006;
                  }

                  if (fake > 0) {
                     chr.getMap().broadcastMessage(chr, CField.facialExpression(chr, 2), false);
                  }
               } else if (damage < -1) {
                  c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                  return;
               }

               int hploss;
               int mploss;
               int skilllv;
               if (damage == 0) {
                  HashMap statups;
                  if (chr.getBuffedEffect(SecondaryStat.BlessingArmor) != null) {
                     if (chr.getBuffedValue(SecondaryStat.BlessingArmor) == 1) {
                        chr.addCooldown(1210016, System.currentTimeMillis(), (long)chr.getBuffedEffect(SecondaryStat.BlessingArmor).getCooldown(chr));
                        chr.cancelEffectFromBuffStat(SecondaryStat.BlessingArmor);
                        chr.cancelEffectFromBuffStat(SecondaryStat.BlessingArmorIncPad);
                     } else {
                        chr.setBuffedValue(SecondaryStat.BlessingArmor, chr.getBuffedValue(SecondaryStat.BlessingArmor) - 1);
                        statups = new HashMap();
                        statups.put(SecondaryStat.BlessingArmor, new Pair(chr.getBuffedValue(SecondaryStat.BlessingArmor), (int)chr.getBuffLimit(chr.getBuffSource(SecondaryStat.BlessingArmor))));
                        statups.put(SecondaryStat.BlessingArmorIncPad, new Pair(chr.getBuffedValue(SecondaryStat.BlessingArmorIncPad), (int)chr.getBuffLimit(chr.getBuffSource(SecondaryStat.BlessingArmorIncPad))));
                        chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, chr.getBuffedEffect(SecondaryStat.BlessingArmor), chr));
                     }
                  }

                  if (chr.getBuffedEffect(SecondaryStat.HolyMagicShell) != null) {
                     if (chr.getHolyMagicShell() > 1) {
                        chr.setHolyMagicShell((byte)(chr.getHolyMagicShell() - 1));
                        statups = new HashMap();
                        statups.put(SecondaryStat.HolyMagicShell, new Pair(Integer.valueOf(chr.getHolyMagicShell()), (int)chr.getBuffLimit(chr.getBuffSource(SecondaryStat.HolyMagicShell))));
                        chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, chr.getBuffedEffect(SecondaryStat.HolyMagicShell), chr));
                     } else {
                        chr.cancelEffectFromBuffStat(SecondaryStat.HolyMagicShell);
                     }
                  }

                  if (chr.getSkillLevel(13110026) > 0) {
                     SkillFactory.getSkill(13110026).getEffect(chr.getSkillLevel(13110026)).applyTo(chr, false);
                     return;
                  }

                  if (chr.getBuffedValue(36111003)) {
                     if (chr.stackbuff == 1) {
                        chr.cancelEffect(chr.getBuffedEffect(SecondaryStat.StackBuff));
                     } else {
                        chr.getBuffedEffect(SecondaryStat.StackBuff).applyTo(chr, false);
                     }
                  }

                  if (chr.getBuffedValue(SecondaryStat.DarkSight) == null && chr.getMapId() / 10000 != 10904 && chr.getMapId() / 10000 != 91013 && chr.getMapId() / 10000 != 91015) {
                     int[] var44 = new int[]{4210015, 4330001};
                     hploss = var44.length;

                     for(mploss = 0; mploss < hploss; ++mploss) {
                        skilllv = var44[mploss];
                        if (chr.getSkillLevel(skilllv) > 0 && chr.getSkillLevel(4001003) > 0 && Randomizer.isSuccess(SkillFactory.getSkill(skilllv).getEffect(chr.getSkillLevel(skilllv)).getX())) {
                           SkillFactory.getSkill(4001003).getEffect(chr.getSkillLevel(4001003)).applyTo(chr, false);
                           break;
                        }
                     }
                  }

                  if (chr.getSkillLevel(4330009) > 0 && chr.getSkillCustomValue(4330009) == null) {
                     SkillFactory.getSkill(4330009).getEffect(chr.getSkillLevel(4330009)).applyTo(chr, false);
                     chr.getClient().getSession().writeAndFlush(CField.getDotge());
                     chr.setSkillCustomInfo(4330009, 0L, 5000L);
                  }
               }

               if ((chr.getJob() == 2711 || chr.getJob() == 2712) && chr.getSkillLevel(27110007) > 0) {
                  bx = SkillFactory.getSkill(27110007);
                  bof = chr.getSkillLevel(bx);
                  EnumMap<SecondaryStat, Pair<Integer, Integer>> statups = new EnumMap(SecondaryStat.class);
                  if (chr.getStat().getHp() / chr.getStat().getCurrentMaxHp() * 100L < chr.getStat().getMp() / chr.getStat().getCurrentMaxMp(chr) * 100L) {
                     statups.put(SecondaryStat.LifeTidal, new Pair(2, 0));
                     c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, bx.getEffect(bof), chr));
                  } else if (chr.getStat().getHp() / chr.getStat().getCurrentMaxHp() * 100L > chr.getStat().getMp() / chr.getStat().getCurrentMaxMp(chr) * 100L && bof > 0) {
                     statups.put(SecondaryStat.LifeTidal, new Pair(1, 0));
                     c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, bx.getEffect(bof), chr));
                  }
               }

               SecondaryStatEffect blessingArmor;
               if (pPhysical && skillid == 1201007 && chr.getTotalSkillLevel(1201007) > 0) {
                  if ((damage -= pDMG) > 0) {
                     blessingArmor = SkillFactory.getSkill(1201007).getEffect(chr.getTotalSkillLevel(1201007));
                     long enemyDMG = Math.min((long)(damage * (blessingArmor.getY() / 100)), attacker.getMobMaxHp() / 2L);
                     if (enemyDMG > (long)pDMG) {
                        enemyDMG = (long)pDMG;
                     }

                     if (enemyDMG > 1000L) {
                        enemyDMG = 1000L;
                     }

                     attacker.damage(chr, enemyDMG, true, 1201007);
                  } else {
                     damage = 1;
                  }
               }

               if (damage > 0) {
                  if (attacker != null) {
                     if (attacker.getStats().getName().equals("윌")) {
                        switch(type) {
                        case 4:
                           c.getPlayer().setSkillCustomInfo(8880302, 0L, 5000L);
                           break;
                        case 5:
                           chr.setMoonGauge(Math.max(0, chr.getMoonGauge() - 3));
                           chr.getClient().getSession().writeAndFlush(MobPacket.BossWill.addMoonGauge(chr.getMoonGauge()));
                        }
                     }

                     if (attacker.getId() == 8900002 || attacker.getId() == 8900102) {
                        MobSkill ms = MobSkillFactory.getMobSkill(120, 1);
                        ms.setDuration(4000L);
                        c.getPlayer().giveDebuff(SecondaryStat.Seal, ms);
                     }

                     MapleMonster seren = chr.getMap().getMonsterById(8880602);
                     MapleMonster serenDawn = chr.getMap().getMonsterById(8880603);
                     switch(attacker.getId()) {
                     case 8880600:
                        chr.addSerenGauge(type == 2 ? 150 : (type == 1 ? 100 : 150));
                        break;
                     case 8880601:
                        chr.addSerenGauge(150);
                        break;
                     case 8880602:
                        chr.addSerenGauge(1000);
                        break;
                     case 8880603:
                        chr.addSerenGauge(100);
                        if (seren == null || serenDawn == null) {
                           seren.gainShield(seren.getStats().getHp() / 100L, seren.getShield() <= 0L, 0);
                        }

                        serenDawn.getMap().broadcastMessage(MobPacket.BossSeren.SerenChangePhase("Mob/8880603.img/info/shield", 2, serenDawn));
                        break;
                     case 8880604:
                        chr.addSerenGauge(100);
                        if (seren == null || serenDawn == null) {
                           seren.gainShield(seren.getStats().getHp() / 100L, seren.getShield() <= 0L, 0);
                        }

                        serenDawn.getMap().broadcastMessage(MobPacket.BossSeren.SerenChangePhase("Mob/8880603.img/info/shield", 2, serenDawn));
                        break;
                     case 8880605:
                     case 8880606:
                        if (seren == null || serenDawn == null) {
                           seren.gainShield(seren.getStats().getHp() / 100L, seren.getShield() <= 0L, 0);
                        }

                        serenDawn.getMap().broadcastMessage(MobPacket.BossSeren.SerenChangePhase("Mob/8880603.img/info/shield", 2, serenDawn));
                        break;
                     case 8880607:
                        chr.addSerenGauge(type == 2 ? 200 : (type == 4 ? 200 : 100));
                        break;
                     case 8880608:
                        chr.addSerenGauge(100);
                        break;
                     case 8880609:
                        chr.addSerenGauge(type == 2 ? 200 : (type == 4 ? 200 : 100));
                     case 8880610:
                     case 8880611:
                     case 8880612:
                     default:
                        break;
                     case 8880613:
                        MobSkill ms = MobSkillFactory.getMobSkill(120, 1);
                        ms.setDuration(3000L);
                        c.getPlayer().giveDebuff(SecondaryStat.Seal, ms);
                     }

                     Iterator var54 = chr.getMap().getAllSummonsThreadsafe().iterator();

                     while(var54.hasNext()) {
                        MapleSummon sum = (MapleSummon)var54.next();
                        MapleCharacter owner;
                        if ((owner = sum.getOwner()) != null) {
                           damaged = false;

                           try {
                              dam = (int)((double)owner.getBuffedEffect(sum.getSkill()).getX() * 0.01D) * damage;
                           } catch (NullPointerException var30) {
                              dam = 0;
                           }

                           ArrayList<Pair<Integer, List<Long>>> allDamage = new ArrayList();
                           ArrayList<Long> dmg = new ArrayList();
                           dmg.add((long)damage * 13L);
                           allDamage.add(new Pair(attacker.getObjectId(), dmg));
                           if (sum.getPosition().x - 370 < chr.getPosition().x && sum.getPosition().x + 370 > chr.getPosition().x && sum.getPosition().y - 235 < chr.getPosition().y && sum.getPosition().y + 235 > chr.getPosition().y) {
                              if (owner.getId() == chr.getId()) {
                                 if (attacker.getStats().getHp() / 2L > (long)(damage * 13)) {
                                    damaged = true;
                                 }
                              } else if (chr.getParty() != null && owner.getParty() != null && chr.getParty().getId() == owner.getParty().getId() && owner.getMapId() == chr.getMapId() && attacker.getStats().getHp() / 2L > (long)(damage * 13)) {
                                 damaged = true;
                              }
                           }

                           if (damaged) {
                              owner.getMap().broadcastMessage(CField.SummonPacket.summonAttack(sum, sum.getSkill(), (byte)-120, (byte)17, allDamage, owner.getLevel(), sum.getPosition(), true));
                              attacker.damage(owner, (long)dam, true);
                           }
                        }
                     }
                  } else if (type == -5) {
                     slea.skip(41);
                     bof = slea.readInt();
                     Iterator var49 = chr.getMap().getAllObstacle().iterator();

                     label884:
                     while(var49.hasNext()) {
                        Obstacle o = (Obstacle)var49.next();
                        if (o.getObjectId() == bof && o.isEffect()) {
                           MobSkill ms;
                           MobSkill ms;
                           switch(o.getKey()) {
                           case 48:
                           case 49:
                           case 50:
                           case 51:
                           case 52:
                              ms = MobSkillFactory.getMobSkill(123, 63);
                              ms.setDuration(2000L);
                              c.getPlayer().giveDebuff(SecondaryStat.Stun, ms);
                              break label884;
                           case 53:
                           case 54:
                           case 55:
                           case 56:
                           case 57:
                           case 58:
                           case 59:
                           case 60:
                           case 61:
                           case 62:
                           case 63:
                           case 64:
                           case 68:
                           case 69:
                           case 70:
                           case 71:
                           case 77:
                           case 78:
                           case 80:
                           case 81:
                           case 82:
                           default:
                              if (c.getPlayer().isGM()) {
                                 System.out.println("맵패턴 : " + o.getKey());
                              }
                              break label884;
                           case 65:
                           case 84:
                              ms = MobSkillFactory.getMobSkill(121, 1);
                              ms.setDuration(3000L);
                              c.getPlayer().giveDebuff(SecondaryStat.Darkness, ms);
                              break label884;
                           case 66:
                           case 72:
                           case 79:
                              skilllv = o.getKey() == 72 ? 85 : 1;
                              ms = MobSkillFactory.getMobSkill(123, skilllv);
                              ms.setDuration(3000L);
                              c.getPlayer().giveDebuff(SecondaryStat.Stun, ms);
                              break label884;
                           case 67:
                              ms = MobSkillFactory.getMobSkill(133, 1);
                              ms.setDuration(3000L);
                              c.getPlayer().giveDebuff(SecondaryStat.Undead, ms);
                              break label884;
                           case 73:
                              skilllv = o.getKey() == 73 ? 26 : 1;
                              ms = MobSkillFactory.getMobSkill(121, skilllv);
                              ms.setDuration(3000L);
                              c.getPlayer().giveDebuff(SecondaryStat.Weakness, ms);
                              break label884;
                           case 74:
                           case 83:
                              skilllv = o.getKey() == 74 ? 40 : 1;
                              ms = MobSkillFactory.getMobSkill(120, skilllv);
                              ms.setDuration(3000L);
                              c.getPlayer().giveDebuff(SecondaryStat.Seal, ms);
                              break label884;
                           case 75:
                              MobSkillFactory.getMobSkill(249, 1).applyEffect(chr, attacker, true, true);
                              break label884;
                           case 76:
                              ms = MobSkillFactory.getMobSkill(132, 1);
                              ms.setDuration(3000L);
                              c.getPlayer().giveDebuff(SecondaryStat.ReverseInput, ms);
                              break label884;
                           }
                        }
                     }
                  }

                  if (chr.getBuffedValue(SecondaryStat.Morph) != null) {
                     chr.cancelMorphs();
                  }

                  if (chr.getBuffedValue(1210016)) {
                     eff = SkillFactory.getSkill(1210016).getEffect(chr.getSkillLevel(1210016));
                     chr.addSkillCustomInfo(1210016, -1L);
                     if (chr.getSkillCustomValue0(1210016) > 0L) {
                        eff.applyTo(chr, (int)chr.getBuffLimit(1210016));
                     } else {
                        chr.cancelEffect(chr.getBuffedEffect(1210016));
                     }
                  } else if (!chr.getBuffedValue(1210016) && chr.getSkillLevel(1210016) > 0 && (blessingArmor = SkillFactory.getSkill(1210016).getEffect(chr.getSkillLevel(1210016))).makeChanceResult() && chr.getCooldownLimit(1210016) == 0L) {
                     chr.setSkillCustomInfo(1210016, (long)blessingArmor.getX(), 0L);
                     chr.addCooldown(1210016, System.currentTimeMillis(), (long)blessingArmor.getCooldown(chr));
                     blessingArmor.applyTo(chr, false);
                  }

                  if (chr.getBuffedValue(400011011)) {
                     buffowner = chr.getMap().getCharacter(chr.getBuffedOwner(400011011));
                     counterAttack = buffowner.getBuffedEffect(SecondaryStat.RhoAias);
                     if (buffowner.getRhoAias() > 1) {
                        buffowner.setRhoAias(buffowner.getRhoAias() - 1);
                        HashMap<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
                        statups.put(SecondaryStat.RhoAias, new Pair(counterAttack.getX(), (int)buffowner.getBuffLimit(400011011)));
                        buffowner.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, counterAttack, buffowner));
                        buffowner.getMap().broadcastMessage(buffowner, CWvsContext.BuffPacket.giveForeignBuff(buffowner, statups, counterAttack), false);
                     } else {
                        chr.cancelEffectFromBuffStat(SecondaryStat.RhoAias);
                     }
                  } else if (chr.getBuffedValue(SecondaryStat.SiphonVitality) != null) {
                     chr.setSkillCustomInfo(14120011, chr.getSkillCustomValue0(14120011) - (long)damage, 0L);
                     if (chr.getSkillCustomValue0(14120011) <= 0L) {
                        chr.setSkillCustomInfo(14120011, 0L, 0L);
                        chr.cancelEffectFromBuffStat(SecondaryStat.Protective);
                     } else {
                        SkillFactory.getSkill(14120011).getEffect(chr.getSkillLevel(14120009)).applyTo(chr, (int)chr.getBuffLimit(14120011));
                     }
                  }

                  if (chr.getBuffedValue(162001005)) {
                     chr.addMP(-40L);
                     chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, 162001005, 10, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), true, chr.getTruePosition(), (String)null, (Item)null));
                     chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 0, 162001005, 10, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), false, chr.getTruePosition(), (String)null, (Item)null), false);
                  }

                  boolean damaged = false;
                  if (type == -1 || type == 0) {
                     if (chr.getBuffedValue(SecondaryStat.MagicGaurd) == null && chr.getSkillLevel(27000003) <= 0 && chr.getSkillLevel(12000024) <= 0) {
                        if (chr.getBuffedEffect(SecondaryStat.PowerTransferGauge) != null) {
                           damaged = true;
                           if ((long)damage < chr.getStat().getCurrentMaxHp()) {
                              if (chr.getBarrier() < damage) {
                                 chr.addHP((long)(-(damage -= chr.getBarrier())));
                                 chr.setBarrier(0);
                                 chr.cancelEffectFromBuffStat(SecondaryStat.PowerTransferGauge);
                              } else {
                                 chr.setBarrier(chr.getBarrier() - damage);
                                 chr.getBuffedEffect(SecondaryStat.PowerTransferGauge).applyTo(chr, false, (int)chr.getBuffLimit(chr.getBuffSource(SecondaryStat.PowerTransferGauge)));
                              }
                           }
                        }
                     } else {
                        hploss = 0;
                        mploss = 0;
                        if (isDeadlyAttack) {
                           if (stats.getHp() > 1L) {
                              hploss = (int)(stats.getHp() - 1L);
                           }

                           if (stats.getMp() > 1L) {
                              mploss = (int)(stats.getMp() - 1L);
                           }

                           if (chr.getBuffedValue(SecondaryStat.Infinity) != null) {
                              mploss = 0;
                           }

                           chr.addMPHP((long)(-hploss), (long)(-mploss));
                        } else {
                           if (chr.getSkillLevel(27000003) > 0) {
                              Skill skill = SkillFactory.getSkill(27000003);
                              SecondaryStatEffect eff = skill.getEffect(chr.getSkillLevel(skill));
                              mploss = (int)((double)damage * ((double)eff.getX() / 100.0D)) + mpattack;
                           } else if (chr.getBuffedEffect(SecondaryStat.MagicGaurd) != null) {
                              mploss = (int)((double)damage * (chr.getBuffedValue(SecondaryStat.MagicGaurd).doubleValue() / 100.0D)) + mpattack;
                           }

                           hploss = damage - mploss;
                           if (chr.getBuffedValue(SecondaryStat.Infinity) != null) {
                              mploss = 0;
                           } else if (chr.getSkillLevel(12000024) > 0) {
                              eff = SkillFactory.getSkill(12000024).getEffect(chr.getSkillLevel(12000024));
                              mploss = (int)((double)damage * ((double)eff.getX() / 100.0D));
                              hploss = damage - mploss + mpattack;
                           } else if ((long)mploss > stats.getMp()) {
                              mploss = (int)stats.getMp();
                              hploss = damage - mploss + mpattack;
                           }

                           chr.addMPHP((long)(-hploss), (long)(-mploss));
                        }

                        damaged = true;
                     }

                     if (chr.getBuffedValue(152000009) && chr.blessMark > 0) {
                        damage = damage * 30 / 100;
                        --chr.blessMark;
                        if (chr.blessMark <= 0) {
                           chr.cancelEffect(chr.getBuffedEffect(152000009));
                        } else {
                           chr.getBuffedEffect(152000009).applyTo(chr, false);
                        }

                        chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, 152100011, 10, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), true, chr.getPosition(), (String)null, (Item)null));
                        chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 0, 152100011, 10, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), false, chr.getPosition(), (String)null, (Item)null), false);
                     }
                  }

                  if (!damaged) {
                     if (isDeadlyAttack) {
                        chr.addMPHP(stats.getHp() > 1L ? -(stats.getHp() - 1L) : 0L, stats.getMp() > 1L ? -(stats.getMp() - 1L) : 0L);
                     } else {
                        chr.addMPHP((long)(-damage), (long)(-mpattack));
                     }

                     if (chr.getBuffedValue(80001479)) {
                        chr.cancelEffectFromBuffStat(SecondaryStat.IndiePadR, 80001479);
                        chr.cancelEffectFromBuffStat(SecondaryStat.IndieMadR, 80001479);
                     }
                  } else if (chr.getBuffedValue(80001479)) {
                     chr.cancelEffectFromBuffStat(SecondaryStat.IndiePadR, 80001479);
                     chr.cancelEffectFromBuffStat(SecondaryStat.IndieMadR, 80001479);
                  }
               }

               byte offset = 0;
               bof = 0;
               if (slea.available() == 1L) {
                  offset = slea.readByte();
                  if (offset == 1 && slea.available() >= 4L) {
                     bof = slea.readInt();
                  }

                  if (offset < 0 || offset > 2) {
                     offset = 0;
                  }
               }

               chr.getMap().broadcastMessage(chr, CField.damagePlayer(chr.getId(), type, damage, monsteridfrom, direction, skillid, pDMG, pPhysical, pID, pType, pPos, offset, bof, fake), false);
            }
         } else {
            c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
         }
      } else {
         c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
      }
   }

   public static final void AranCombo(MapleClient c, MapleCharacter chr, int skillid) {
      if (chr != null && chr.getJob() >= 2000 && chr.getJob() <= 2112 && !chr.getBuffedValue(21111030)) {
         SecondaryStatEffect skill = SkillFactory.getSkill(skillid).getEffect(chr.getSkillLevel(skillid));
         int toAdd = skill.getAttackCount();
         short combo = chr.getCombo();
         long curr = System.currentTimeMillis();
         int ability = combo / 50;
         combo = (short)Math.min(30000, combo + toAdd);
         chr.setLastCombo(curr);
         if (combo >= 1000) {
            combo = 1000;
         }

         chr.setCombo(combo);
         c.getSession().writeAndFlush(CField.aranCombo(combo));
         if (chr.getSkillLevel(21000000) > 0 && ability != combo / 50) {
            SkillFactory.getSkill(21000000).getEffect(chr.getSkillLevel(21000000)).applyTo(chr, false);
         }

         if (combo >= 1000) {
            Skill ad = SkillFactory.getSkill(21111030);
            SecondaryStatEffect effect = ad.getEffect(1);
            effect.applyTo(chr, true);
         }
      }

   }

   public static void AndroidEar(MapleClient c, LittleEndianAccessor slea) {
      MapleAndroid android = c.getPlayer().getAndroid();
      if (android == null) {
         c.getPlayer().dropMessage(1, "알 수 없는 오류가 발생 하였습니다.");
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      } else {
         short slot = slea.readShort();
         Item item = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
         if (item != null && item.getItemId() == 2892000) {
            android.setEar(!android.getEar());
            c.getPlayer().updateAndroid();
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, true);
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
         } else {
            c.getPlayer().dropMessage(1, "알 수 없는 오류가 발생 하였습니다.");
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
         }

      }
   }

   public static final void LossAranCombo(MapleClient c, MapleCharacter chr, int toAdd) {
      if (chr != null && chr.getJob() >= 2000 && chr.getJob() <= 2112) {
         short combo = chr.getCombo();
         long curr = System.currentTimeMillis();
         if (combo <= 0) {
            combo = 0;
         }

         combo = (short)Math.min(30000, combo - toAdd);
         chr.setLastCombo(curr);
         chr.setCombo(combo);
         SkillFactory.getSkill(21000000).getEffect(chr.getSkillLevel(21000000)).applyTo(chr, false);
         c.getSession().writeAndFlush(CField.aranCombo(combo));
      }

   }

   public static final void BossWarp(LittleEndianAccessor slea, MapleCharacter chr) {
      slea.skip(8);
      int mapid = slea.readInt();
      MapleMap target = chr.getClient().getChannelServer().getMapFactory().getMap(mapid);
      int size = 0;
      if (chr.getParty() != null) {
         Iterator var6 = chr.getParty().getMembers().iterator();

         MapleCharacter curChar;
         MaplePartyCharacter chrz;
         while(var6.hasNext()) {
            chrz = (MaplePartyCharacter)var6.next();
            curChar = chr.getClient().getChannelServer().getPlayerStorage().getCharacterById(chrz.getId());
            if (curChar != null && curChar.getMapId() == chr.getMapId() && curChar.getClient().getChannel() == chr.getClient().getChannel()) {
               ++size;
            }
         }

         if (size == chr.getParty().getMembers().size()) {
            var6 = chr.getParty().getMembers().iterator();

            while(var6.hasNext()) {
               chrz = (MaplePartyCharacter)var6.next();
               curChar = chr.getClient().getChannelServer().getPlayerStorage().getCharacterById(chrz.getId());
               if (curChar != null && curChar.getClient().getChannel() == chr.getClient().getChannel()) {
                  curChar.getPlayer().setKeyValue(210406, "Return_BossMap", curChar.getMapId().makeConcatWithConstants<invokedynamic>(curChar.getMapId()));
                  curChar.changeMap(target, target.getPortal(0));
               }
            }
         } else {
            chr.dropMessage(5, "모든 파티원과 같은 곳에 있어야 이동이 가능 합니다.");
         }
      } else {
         chr.getPlayer().setKeyValue(210406, "Return_BossMap", chr.getMapId().makeConcatWithConstants<invokedynamic>(chr.getMapId()));
         chr.changeMap(target, target.getPortal(0));
      }

      chr.getClient().getSession().writeAndFlush(CWvsContext.enableActions(chr));
   }

   public static final void BossMatching(LittleEndianAccessor slea, MapleCharacter chr) {
      int type = slea.readInt();
      int semitype = slea.readInt();
      int mapid = -1;
      switch(type) {
      case 0:
         mapid = 105100100;
         break;
      case 1:
         mapid = 211042300;
         break;
      case 2:
      default:
         System.out.println("해당 보스와 연결된 맵이 없습니다. type : 0x" + Integer.toHexString(type).toUpperCase());
         break;
      case 3:
         mapid = 401060000;
         break;
      case 4:
         mapid = 262000000;
         break;
      case 5:
         mapid = 221030900;
         break;
      case 6:
         mapid = 220080000;
         break;
      case 7:
         mapid = 105200000;
         break;
      case 8:
         mapid = 105200000;
         break;
      case 9:
         mapid = 105200000;
         break;
      case 10:
         mapid = 105200000;
         break;
      case 11:
         mapid = 211070000;
         break;
      case 12:
         mapid = 240040700;
         break;
      case 13:
         mapid = 272000000;
         break;
      case 14:
         mapid = 270040000;
         break;
      case 15:
         mapid = 271041000;
         break;
      case 16:
         mapid = 350060300;
         break;
      case 17:
         mapid = 105300303;
         break;
      case 18:
         mapid = 450004000;
         break;
      case 19:
         mapid = 450007240;
         break;
      case 20:
         mapid = 450009301;
         break;
      case 21:
         mapid = 450011990;
         break;
      case 22:
         mapid = 450012200;
         break;
      case 23:
         mapid = 450012500;
      }

      if (mapid != -1) {
         MapleMap target = chr.getClient().getChannelServer().getMapFactory().getMap(mapid);
         if (chr.getParty() != null) {
            Iterator var6 = chr.getParty().getMembers().iterator();

            label55:
            while(true) {
               MapleCharacter curChar;
               do {
                  do {
                     if (!var6.hasNext()) {
                        break label55;
                     }

                     MaplePartyCharacter chrz = (MaplePartyCharacter)var6.next();
                     curChar = chr.getClient().getChannelServer().getPlayerStorage().getCharacterById(chrz.getId());
                  } while(curChar == null);
               } while(curChar.getMapId() != chr.getMapId() && curChar.getEventInstance() != chr.getEventInstance());

               curChar.getPlayer().setKeyValue(210406, "Return_BossMap", curChar.getMapId().makeConcatWithConstants<invokedynamic>(curChar.getMapId()));
               curChar.changeMap(target, target.getPortal(0));
            }
         } else {
            chr.getPlayer().setKeyValue(210406, "Return_BossMap", chr.getMapId().makeConcatWithConstants<invokedynamic>(chr.getMapId()));
            chr.changeMap(target, target.getPortal(0));
         }
      }

      chr.getClient().getSession().writeAndFlush(CField.UIPacket.closeUI(7));
      chr.getClient().getSession().writeAndFlush(CWvsContext.enableActions(chr));
   }

   public static final void UseItemEffect(int itemId, MapleClient c, MapleCharacter chr) {
      chr.setKeyValue(27038, "itemid", itemId.makeConcatWithConstants<invokedynamic>(itemId));
      Item toUse = chr.getInventory(MapleInventoryType.CASH).findById(itemId);
      if (toUse != null && toUse.getItemId() == itemId && toUse.getQuantity() >= 1) {
         if (itemId != 5510000) {
            chr.setItemEffect(itemId);
         }

         chr.getMap().broadcastMessage(chr, CField.itemEffect(chr.getId(), itemId), false);
      } else {
         c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
      }
   }

   public static final void CancelItemEffect(int id, MapleCharacter chr) {
      chr.cancelEffect(MapleItemInformationProvider.getInstance().getItemEffect(-id));
   }

   public static final void CancelBuffHandler(LittleEndianAccessor slea, MapleCharacter chr) {
      int sourceid = slea.readInt();
      if (sourceid == 13101022) {
         sourceid = chr.getBuffedEffect(SecondaryStat.TryflingWarm) != null ? chr.getBuffedEffect(SecondaryStat.TryflingWarm).getSourceId() : sourceid;
      }

      ArrayList<SecondaryStat> stats = new ArrayList();
      if (chr != null && chr.getMap() != null && SkillFactory.getSkill(sourceid) != null && sourceid != 400011091 && sourceid != 80003059) {
         int level = chr.getSkillLevel(GameConstants.getLinkedSkill(sourceid));
         SecondaryStatEffect effect = SkillFactory.getSkill(sourceid).getEffect(level);
         int cool;
         if (effect.getSourceId() == 1221054) {
            cool = (int)(chr.getBuffLimit(1221054) / (long)(effect.getY() * 1000));
            int reduce = cool * effect.getX() * 1000;
            chr.changeCooldown(1221054, -reduce);
            MapleCharacter holyunitychr = chr.getMap().getCharacter((int)chr.getSkillCustomValue0(400011003));
            if (holyunitychr != null) {
               while(holyunitychr.getBuffedValue(1221054)) {
                  holyunitychr.cancelEffect(holyunitychr.getBuffedEffect(1221054));
               }
            }
         }

         if (sourceid == 5221029) {
            chr.signofbomb = false;
         }

         if (effect.getSourceId() == 400051334) {
            chr.cancelEffectFromBuffStat(SecondaryStat.IndieNotDamaged, 400051334);
         } else {
            chr.cancelEffect(effect, stats);
         }

         chr.getMap().broadcastMessage(chr, CField.skillCancel(chr, sourceid), false);
         if (SkillFactory.getSkill(sourceid).isChargeSkill()) {
            chr.setKeyDownSkill_Time(System.currentTimeMillis());
         }

         if (effect.getCooldown(chr) > 0 && !chr.skillisCooling(sourceid) && GameConstants.isAfterCooltimeSkill(sourceid)) {
            chr.getClient().getSession().writeAndFlush(CField.skillCooldown(sourceid, effect.getCooldown(chr)));
            chr.addCooldown(sourceid, System.currentTimeMillis(), (long)effect.getCooldown(chr));
         }

         if (sourceid == 400041009) {
            SkillFactory.getSkill(400041009).getEffect(level).applyTo(chr, false, true);
            SkillFactory.getSkill(Randomizer.rand(400041011, 400041015)).getEffect(level).applyTo(chr, false, true);
         } else if (sourceid == 20031205) {
            cool = (int)(1500L * chr.getSkillCustomValue0(20031205));
            chr.getClient().getSession().writeAndFlush(CField.skillCooldown(sourceid, cool));
            chr.addCooldown(sourceid, System.currentTimeMillis(), (long)cool);
            chr.removeSkillCustomInfo(sourceid);
         } else if (sourceid == 25111005) {
            chr.getClient().getSession().writeAndFlush(CField.skillCooldown(25111012, 10000));
            chr.addCooldown(25111012, System.currentTimeMillis(), 10000L);
         }

      }
   }

   public static final void NameChanger(boolean isspcheck, LittleEndianAccessor slea, MapleClient c) {
      if (isspcheck) {
         String secondPassword = slea.readMapleAsciiString();
         if (c.CheckSecondPassword(secondPassword)) {
            c.getSession().writeAndFlush(CField.NameChanger((byte)9, 4034803));
         } else {
            c.getSession().writeAndFlush(CField.NameChanger((byte)10));
         }
      } else {
         int chrid = slea.readInt();
         byte status = slea.readByte();
         int itemuse = slea.readInt();
         String oriname = slea.readMapleAsciiString();
         String newname = slea.readMapleAsciiString();
         if (c.getPlayer().getId() != chrid) {
            c.getSession().writeAndFlush(CField.NameChanger((byte)2));
            return;
         }

         if (itemuse != 4034803) {
            c.getSession().writeAndFlush(CField.NameChanger((byte)2));
            return;
         }

         if (status != 1) {
            c.getSession().writeAndFlush(CField.NameChanger((byte)2));
            return;
         }

         if (!c.getPlayer().getName().equals(oriname)) {
            c.getSession().writeAndFlush(CField.NameChanger((byte)2));
            return;
         }

         if (c.getPlayer().haveItem(4034803, 1)) {
            if (MapleCharacterUtil.canCreateChar(newname)) {
               if (MapleCharacterUtil.isEligibleCharNameTwo(newname, c.getPlayer().isGM()) && !LoginInformationProvider.getInstance().isForbiddenName(newname)) {
                  if (MapleCharacterUtil.getIdByName(newname) == -1) {
                     MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4034803, 1, false, false);
                     c.getPlayer().setName(newname);
                     MapleCharacter.saveNameChange(newname, c.getPlayer().getId());
                     Iterator var8 = c.getPlayer().getUnions().getUnions().iterator();

                     while(var8.hasNext()) {
                        MapleUnion union = (MapleUnion)var8.next();
                        if (union.getCharid() == c.getPlayer().getId()) {
                           union.setName(newname);
                        }
                     }

                     c.getSession().writeAndFlush(CField.NameChanger((byte)0));
                     c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                  } else {
                     c.getSession().writeAndFlush(CField.NameChanger((byte)7));
                  }
               } else {
                  c.getSession().writeAndFlush(CField.NameChanger((byte)6));
               }
            } else {
               c.getSession().writeAndFlush(CField.NameChanger((byte)2));
            }
         } else {
            c.getSession().writeAndFlush(CField.NameChanger((byte)3));
         }
      }

   }

   public static final void CancelMech(LittleEndianAccessor slea, MapleCharacter chr) {
      if (chr != null) {
         int sourceid = slea.readInt();
         int level = slea.readInt();
         if (sourceid % 10000 < 1000 && SkillFactory.getSkill(sourceid) == null) {
            sourceid += 1000;
         }

         Skill skill = SkillFactory.getSkill(sourceid);
         if (skill != null) {
            if (skill.isChargeSkill()) {
               chr.setKeyDownSkill_Time(0L);
               chr.getMap().broadcastMessage(chr, CField.skillCancel(chr, sourceid), false);
            } else {
               chr.cancelEffect(skill.getEffect(level));
            }

            if (skill.getEffect(level).getCooldown(chr) > 0) {
               chr.getClient().getSession().writeAndFlush(CField.skillCooldown(sourceid, skill.getEffect(level).getCooldown(chr)));
               chr.addCooldown(sourceid, System.currentTimeMillis(), (long)skill.getEffect(level).getCooldown(chr));
            }

         }
      }
   }

   public static final void SkillEffect(LittleEndianAccessor slea, MapleCharacter chr) {
      int skillId = slea.readInt();
      int level = slea.readInt();
      slea.skip(4);
      int a = slea.readByte();
      if (a == 1) {
         slea.skip(8);
      }

      short display = slea.readShort();
      byte unk = slea.readByte();
      if (display == -1 && unk == -1) {
         slea.skip(1);
         display = slea.readShort();
         unk = slea.readByte();
      }

      chr.dropMessageGM(6, "SkillEffect SkillId : " + skillId);
      Skill skill = SkillFactory.getSkill(GameConstants.getLinkedSkill(skillId));
      if (chr != null && skill != null && chr.getMap() != null) {
         if (GameConstants.isCain(chr.getJob())) {
            chr.handleRemainIncense(skillId, true);
         } else if (GameConstants.isZero(chr.getJob()) && skillId == 101110102) {
            chr.ZeroSkillCooldown(skillId);
         }

         int skilllevel_serv = chr.getTotalSkillLevel(skill);
         if (skillId == 400031064) {
            chr.cancelEffect(SkillFactory.getSkill(400031062).getEffect(skilllevel_serv));
         }

         boolean apply = true;
         boolean cooltime = true;
         SecondaryStatEffect eff = skill.getEffect(skilllevel_serv);
         SecondaryStatEffect effect = SkillFactory.getSkill(skillId).getEffect(level);
         long du;
         if (skilllevel_serv > 0 && skilllevel_serv == level) {
            if (skill.isChargeSkill()) {
               chr.setKeyDownSkill_Time(System.currentTimeMillis());
            }

            switch(skillId) {
            case 33101005:
               chr.setLinkMid(slea.readInt(), 0);
               break;
            case 37121052:
               chr.setSkillCustomInfo(37121052, 1L, 0L);
               break;
            case 63121040:
               chr.handleStackskill(skillId, true);
               break;
            case 135001020:
               chr.setSkillCustomInfo(13500, 0L, 0L);
               SkillFactory.getSkill(135001005).getEffect(1).applyTo(chr, (int)chr.getBuffLimit(135001005));
               break;
            case 151121004:
               chr.setSkillCustomInfo(151121004, 8L, 0L);
               break;
            case 155111306:
            case 155121341:
            case 400051080:
            case 400051334:
               if (!chr.getBuffedValue(155000007)) {
                  SkillFactory.getSkill(155000007).getEffect(1).applyTo(chr);
               }

               if (skillId == 400051080) {
                  chr.setSkillCustomInfo(skillId, 1L, 0L);
               }
               break;
            case 162121022:
               chr.setSkillCustomInfo(162121022, (long)eff.getQ(), 0L);
               break;
            case 164121042:
               Map<MapleStat, Long> hpmpupdate = new EnumMap(MapleStat.class);
               du = (long)SkillFactory.getSkill(skillId).getEffect(chr.getSkillLevel(skillId)).getMPRCon();
               chr.getStat().setMp(chr.getStat().getMp() - chr.getStat().getCurrentMaxMp(chr) / 100L * du, chr);
               hpmpupdate.put(MapleStat.MP, chr.getStat().getMp());
               if (hpmpupdate.size() > 0) {
                  chr.getClient().getSession().writeAndFlush(CWvsContext.updatePlayerStats(hpmpupdate, false, chr));
               }

               chr.setSkillCustomInfo(164121042, (long)(eff.getY() - 1), 0L);
               break;
            case 400011091:
               eff = SkillFactory.getSkill(skillId).getEffect(chr.getSkillLevel(GameConstants.getLinkedSkill(skillId)));
               chr.combinationBuff = 15;
               SkillFactory.getSkill(37120012).getEffect(chr.getSkillLevel(37120012)).applyTo(chr, false);
               chr.setSkillCustomInfo(400011091, 1L, 0L);
               chr.Cylinder(skillId);
               break;
            case 400041009:
               chr.removeSkillCustomInfo(400041009);
               chr.removeSkillCustomInfo(400041010);
               break;
            case 400051040:
               chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 400051040, 400051040, 1, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), false, chr.getPosition(), (String)null, (Item)null), false);
            }
         }

         if (skillId - 64001009 < -2 || skillId - 64001009 > 2) {
            chr.getMap().broadcastMessage(chr, CField.skillEffect(chr, skillId, skilllevel_serv, display, unk), false);
            if ((skillId < 3321034 || skillId > 3321040) && (skillId < 400011110 || skillId > 400011111) && skillId != 400041053) {
               if (apply && skillId != 4341002 && skillId != 2321001 && skillId != 3111013 && skillId != 5311002 && skillId != 11121052 && skillId != 14121003 && skillId != 22171083) {
                  eff.applyTo(chr);
                  if (skillId == 21120018 || skillId == 21120019) {
                     int skillid = chr.getBuffedValue(21110016) ? 21110016 : (chr.getBuffedValue(21121058) ? 21121058 : 0);
                     if (skillid > 0) {
                        du = chr.getBuffLimit(skillid);
                        SkillFactory.getSkill(skillid).getEffect(chr.getSkillLevel(skillid)).applyTo(chr, false, (int)du);
                     }
                  }
               }
            } else {
               effect.applyTo(chr);
            }

            if (cooltime && eff != null && !eff.ignoreCooldown(chr) && !chr.skillisCooling(skillId) && !chr.skillisCooling(skill.getId()) && eff.getCooldown(chr) > 0 && skillId != 3111013 && skillId != 22171083 && !GameConstants.isAfterCooltimeSkill(skillId)) {
               chr.addCooldown(skillId, System.currentTimeMillis(), (long)eff.getCooldown(chr));
               chr.getClient().getSession().writeAndFlush(CField.skillCooldown(skillId, eff.getCooldown(chr)));
            }

         }
      }
   }

   public static final void SpecialMove(LittleEndianAccessor slea, MapleClient c, final MapleCharacter chr) {
      if (chr != null && chr.getMap() != null && slea.available() >= 9L) {
         if (chr.getPlayer().getMapId() != 120043000) {
            Point pos = slea.readPos();
            int skillid = slea.readInt();
            c.getPlayer().dropMessageGM(6, "skillid : " + skillid);
            if (skillid == 35001002) {
               SkillFactory.getSkill(30000227).getEffect(chr.getSkillLevel(1)).applyTo(chr);
            }

            SecondaryStatEffect eff;
            if (skillid == 23111008) {
               eff = SkillFactory.getSkill(skillid).getEffect(c.getPlayer().getSkillLevel(skillid));
               eff.applyTo(c.getPlayer(), eff.getDuration());
            }

            if (skillid == 100001284) {
               eff = SkillFactory.getSkill(skillid).getEffect(c.getPlayer().getSkillLevel(skillid));
               c.getSession().writeAndFlush(CField.skillCooldown(skillid, eff.getCooldown(c.getPlayer())));
            }

            if (skillid == 400021032) {
               eff = SkillFactory.getSkill(400021033).getEffect(c.getPlayer().getSkillLevel(400021033));
               MapleSummon tosummon = new MapleSummon(chr, 400021033, chr.getTruePosition(), SummonMovementType.FOLLOW, (byte)0, eff.getDuration());
               chr.addSummon(tosummon);
               chr.getMap().spawnSummon(tosummon, 30000);
            }

            if (skillid == 5221029) {
               chr.setSkillCustomInfo(5221029, 0L, 6000L);
               chr.signofbomb = true;
            }

            if (skillid == 61101002) {
               if (chr.getSkillLevel(61120007) > 0) {
                  skillid = 61120007;
               } else if (chr.getBuffedValue(SecondaryStat.Morph) != null) {
                  if (chr.getBuffSource(SecondaryStat.Morph) == 61111008) {
                     skillid = 61110211;
                  } else {
                     skillid = 61121217;
                  }
               }
            } else if (skillid != 12001028 && skillid != 12001027 && skillid != 12001029) {
               if (skillid == 51001006) {
                  skillid = 51001009;
               }
            } else {
               c.getPlayer().getMap().broadcastMessage(CField.FireWork(chr));
            }

            if ((skillid == 4221018 || skillid == 4211003) && chr.getBuffedValue(SecondaryStat.PickPocket) != null) {
               chr.cancelEffect(chr.getBuffedEffect(SecondaryStat.PickPocket));
            }

            if (GameConstants.isZeroSkill(skillid)) {
               slea.skip(1);
            }

            if (GameConstants.isKinesis(chr.getJob())) {
               chr.givePPoint(skillid);
            }

            if (skillid == 14001026) {
               eff = SkillFactory.getSkill(14110032).getEffect(chr.getSkillLevel(14110032));
               int count = chr.getMomentumCount();
               if (count < 3) {
                  chr.setMomentumCount(count + 1);
               }

               eff.applyTo(chr, eff.getDuration());
            }

            if (skillid == 5011007) {
               if (c.getPlayer().getKeyValue(7786, "sw") != -1L && c.getPlayer().getKeyValue(7786, "sw") == 1L) {
                  c.getPlayer().updateInfoQuest(7786, "sw=0");
                  c.getPlayer().setKeyValue(7786, "sw", "0");
               } else {
                  c.getPlayer().updateInfoQuest(7786, "sw=1");
                  c.getPlayer().setKeyValue(7786, "sw", "1");
               }

               chr.getMap().broadcastMessage(CField.MonkeyTogether(113, c.getPlayer().getKeyValue(7786, "sw") == 1L));
            }

            SecondaryStatEffect effect;
            if (skillid == 5311013) {
               Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
               long count = c.getPlayer().getSkillCustomValue(5311013);
               c.getPlayer().setSkillCustomInfo(5311013, count - 1L, 0L);
               effect = SkillFactory.getSkill(5311013).getEffect(1);
               statups.put(SecondaryStat.MiniCannonBall, new Pair((int)count, 0));
               c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, effect, c.getPlayer()));
               chr.getClient().getSession().writeAndFlush(CField.rangeAttack(5311013, Arrays.asList(new RangeAttack(5311014, chr.getTruePosition(), 1, 330, 1))));
            }

            int skillLevel = slea.readInt();
            Skill skill = SkillFactory.getSkill(skillid);
            if (skill == null || GameConstants.isAngel(skillid) && chr.getStat().equippedSummon % 10000 != skillid % 10000 || chr.inPVP() && skill.isPVPDisabled()) {
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            } else {
               boolean linkcool = true;
               if (GameConstants.isCain(chr.getJob())) {
                  linkcool = chr.handleCainSkillCooldown(skillid);
               }

               if ((!linkcool || chr.getTotalSkillLevel(GameConstants.getLinkedSkill(skillid)) > 0) && (chr.getTotalSkillLevel(GameConstants.getLinkedSkill(skillid)) == skillLevel || !linkcool) || GameConstants.isMulungSkill(skillid) || GameConstants.isPyramidSkill(skillid) || chr.getTotalSkillLevel(GameConstants.getLinkedSkill(skillid)) > 0 || GameConstants.isAngel(skillid) || GameConstants.isFusionSkill(skillid)) {
                  c.getPlayer().dropMessageGM(6, "skillid2 : " + skillid);
                  switch(skillid) {
                  case 2211012:
                  case 14111030:
                  case 27111101:
                  case 162111002:
                  case 400001025:
                  case 400001026:
                  case 400001027:
                  case 400001028:
                  case 400001029:
                  case 400001030:
                  case 400021122:
                     linkcool = false;
                  }

                  switch(skillid) {
                  case 11111029:
                     c.getPlayer().setCosmicCount(0);
                  default:
                     switch(skillid) {
                     case 1281:
                        c.getPlayer().warp(4000030);
                        c.getPlayer().dropMessage(5, "사우스페리로 귀환 하였습니다.");
                        break;
                     case 20031203:
                        c.getPlayer().warp(150000000);
                        c.getPlayer().dropMessage(5, "크리스탈가든 으로 귀환 하였습니다.");
                        break;
                     case 400001042:
                     case 400001043:
                     case 400001046:
                     case 400001047:
                     case 400001048:
                     case 400001049:
                     case 400001050:
                        --chr.메이플용사;
                        HashMap<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
                        statups.put(SecondaryStat.Bless5th2, new Pair(chr.메이플용사, 0));
                     }

                     skillLevel = chr.getTotalSkillLevel(GameConstants.getLinkedSkill(skillid));
                     effect = chr.inPVP() ? skill.getPVPEffect(skillLevel) : skill.getEffect(skillLevel);
                     if (effect.isMPRecovery() && chr.getStat().getHp() < chr.getStat().getMaxHp() / 100L * 10L) {
                        c.getPlayer().dropMessage(5, "You do not have the HP to use this skill.");
                        c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                     } else {
                        if (GameConstants.isNightWalker(chr.getJob()) && chr.getBuffedEffect(SecondaryStat.DarkSight) != null && skillid != 14001026 & skillid != 10001253) {
                           chr.cancelEffectFromBuffStat(SecondaryStat.DarkSight);
                        }

                        SecondaryStatEffect linkEffect = SkillFactory.getSkill(GameConstants.getLinkedSkill(skillid)).getEffect(skillLevel);
                        if (linkEffect.getCooldown(chr) > 0 && effect.getSourceId() != 35111002 && effect.getSourceId() != 151100002 && effect.getSourceId() != 20031205 && !effect.ignoreCooldown(chr) && (linkEffect.getSourceId() < 400041003 || linkEffect.getSourceId() > 400041005) && linkcool) {
                           if (chr.skillisCooling(linkEffect.getSourceId()) && !GameConstants.isCooltimeKeyDownSkill(skillid) && !GameConstants.isNoApplySkill(skillid) && !chr.getBuffedValue(skillid) && skillid != 155001104 && skillid != 155001204 && skillid != 400001010 && skillid != 400001011 && skillid != 5121055 && skillid != 400020046) {
                              c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                              return;
                           }

                           if (!chr.getBuffedValue(20040219) && !chr.getBuffedValue(20040220)) {
                              if (skillid == 25121133) {
                                 c.getSession().writeAndFlush(CField.skillCooldown(25121133, linkEffect.getCooldown(chr)));
                                 chr.addCooldown(25121133, System.currentTimeMillis(), (long)linkEffect.getCooldown(chr));
                              } else if ((!GameConstants.isAfterCooltimeSkill(skillid) || skillid == 33111013) && !chr.memoraizecheck) {
                                 c.getSession().writeAndFlush(CField.skillCooldown(skillid, linkEffect.getCooldown(chr)));
                                 chr.addCooldown(skillid, System.currentTimeMillis(), (long)linkEffect.getCooldown(chr));
                              }
                           } else if (skill.isHyper() || !GameConstants.isLuminous(skillid / 10000)) {
                              c.getSession().writeAndFlush(CField.skillCooldown(skillid, linkEffect.getCooldown(chr)));
                              chr.addCooldown(skillid, System.currentTimeMillis(), (long)linkEffect.getCooldown(chr));
                           }

                           if (chr.memoraizecheck) {
                              chr.memoraizecheck = false;
                           } else if (skillid == 400001021) {
                              chr.memoraizecheck = true;
                           }
                        } else if (skillid == 400011001 && !chr.getBuffedValue(400011001) && !chr.getBuffedValue(400011002)) {
                           c.getSession().writeAndFlush(CField.skillCooldown(skillid, linkEffect.getX() * 1000));
                        }

                        if (GameConstants.isPhantom(chr.getJob())) {
                           Iterator var10 = chr.getStolenSkills().iterator();

                           while(var10.hasNext()) {
                              Pair<Integer, Boolean> sk = (Pair)var10.next();
                              if ((Integer)sk.left == skillid && (Boolean)sk.right) {
                                 int cooltime = 0;
                                 switch((Integer)sk.left) {
                                 case 1121054:
                                 case 2321054:
                                    cooltime = 300;
                                    break;
                                 case 1221054:
                                    cooltime = 700;
                                    break;
                                 case 1321054:
                                 case 3221054:
                                    cooltime = 180;
                                    break;
                                 case 2121054:
                                 case 5121054:
                                    cooltime = 75;
                                    break;
                                 case 2221054:
                                    cooltime = 90;
                                    break;
                                 case 3121054:
                                 case 4121054:
                                    cooltime = 120;
                                    break;
                                 case 5221054:
                                    cooltime = 60;
                                 }

                                 if (cooltime > 0) {
                                    int cooltime = cooltime * 1000;
                                    c.getSession().writeAndFlush(CField.skillCooldown(skillid, cooltime));
                                    chr.addCooldown(skillid, System.currentTimeMillis(), (long)cooltime);
                                    break;
                                 }
                              }
                           }
                        }

                        if (skillid == 2321016 && c.getPlayer().skillisCooling(skillid)) {
                           SecondaryStatEffect effz = SkillFactory.getSkill(skillid).getEffect(chr.getSkillLevel(skillid));
                           int int_ = c.getPlayer().getStat().getTotalInt() / effz.getS();
                           c.getPlayer().changeCooldown(skillid, Math.max(-effz.getV2() * 1000, -effz.getW2() * int_ * 1000));
                        }

                        if (skillid == 31211004) {
                           chr.startDiabolicRecovery(effect);
                        }

                        if (GameConstants.isSoulSummonSkill(skillid)) {
                           chr.useSoulSkill();
                        }

                        if (skillid == 36121007) {
                           chr.getClient().getSession().writeAndFlush(CField.TimeCapsule(skillid));
                           chr.setChair(3010587);
                           chr.getMap().broadcastMessage(chr, CField.showChair(chr, 3010587), false);
                        }

                        final AttackInfo ret = new AttackInfo();
                        ret.skill = skillid;
                        ret.skilllevel = skillLevel;
                        GameConstants.calcAttackPosition(slea, ret);
                        int unk = slea.readShort();
                        int plus = slea.readShort();
                        chr.dropMessageGM(6, "unk : " + unk);
                        chr.dropMessageGM(6, "plus : " + plus);
                        slea.skip(3);
                        slea.skip(7);
                        byte rltype;
                        boolean facing2;
                        boolean active;
                        int skillid2;
                        int intervar;
                        if (skillid == 3101009) {
                           if (chr.getBuffedValue(3101009)) {
                              rltype = chr.getQuiverType();
                              if (!chr.getBuffedValue(3121016) && !chr.getBuffedValue(400031028)) {
                                 if (chr.getRestArrow()[rltype - 1] > 0) {
                                    chr.setQuiverType((byte)(rltype == 2 ? 1 : rltype + 1));
                                    facing2 = false;
                                    active = false;
                                    skillid2 = chr.getRestArrow()[0];
                                    intervar = chr.getRestArrow()[1];
                                    chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, 3101009, 57, chr.getQuiverType() - 1, 0, (byte)(chr.isFacingLeft() ? 1 : 0), true, chr.getPosition(), (String)null, (Item)null));
                                    chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 0, 3101009, 57, chr.getQuiverType() - 1, 0, (byte)(chr.isFacingLeft() ? 1 : 0), false, chr.getPosition(), (String)null, (Item)null), false);
                                    effect.applyTo(chr, false);
                                 } else {
                                    skillid2 = chr.getQuiverType() - 1;
                                    chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, 3101009, 57, skillid2, 0, (byte)(chr.isFacingLeft() ? 1 : 0), true, chr.getPosition(), (String)null, (Item)null));
                                    chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 0, 3101009, 57, skillid2, 0, (byte)(chr.isFacingLeft() ? 1 : 0), false, chr.getPosition(), (String)null, (Item)null), false);
                                    effect.applyTo(chr, false);
                                 }

                                 c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                                 return;
                              }

                              c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                              return;
                           }
                        } else if (skillid == 12101025) {
                           pos = slea.readPos();
                           slea.readPos();
                           slea.readByte();
                           rltype = slea.readByte();
                           c.send(CWvsContext.onSkillUseResult(0));
                           c.send(CField.fireBlink(chr.getId(), pos));
                           chr.getMap().broadcastMessage(chr, CField.fireBlinkMulti(chr, rltype != 0), false);
                        }

                        chr.checkFollow();
                        if (ret.plusPosition2 != null) {
                           pos = ret.plusPosition2;
                        }

                        if (skillid == 2111013) {
                           SkillFactory.getSkill(2111013).getEffect(chr.getSkillLevel(2111013)).applyTo(chr, chr.getPosition());
                        }

                        MapleSummon summon8;
                        int mobsize;
                        final Integer[] bufflist;
                        SecondaryStatEffect effect2;
                        ArrayList mses;
                        MapleAtom atom;
                        MapleSummon summon7;
                        List objs;
                        Point spawn_pos;
                        MapleAtom atom4;
                        SpecialPortal s3;
                        int mobid2;
                        MapleSummon summon4;
                        ArrayList moblist1;
                        Point point;
                        ArrayList toRemove;
                        Iterator var61;
                        int max;
                        Point pos4;
                        MapleMist mist3;
                        int i;
                        int bullet;
                        MapleCharacter chr6;
                        MapleMagicSword ms3;
                        ArrayList applys2;
                        MaplePartyCharacter chr2;
                        ArrayList monsters3;
                        MapleMonster mob4;
                        Iterator var84;
                        byte i4;
                        ArrayList monsters2;
                        Iterator lp;
                        byte size;
                        byte i10;
                        int randmob_remove;
                        Iterator var99;
                        MapleMist mist;
                        ArrayList mobs;
                        Iterator var105;
                        MapleMonster monster3;
                        Point pos2;
                        Iterator var112;
                        Iterator var117;
                        ArrayList allmesos;
                        MapleAtom atom5;
                        MapleSummon summon2;
                        MapleMist newmist3;
                        ForceAtom fa;
                        byte i6;
                        short y3;
                        MapleMist newmist;
                        HashMap statups;
                        SecondaryStatEffect enhance;
                        MapleAtom atom2;
                        MapleCharacter chr5;
                        byte tesla;
                        boolean use;
                        MaplePartyCharacter chr3;
                        MapleMonster mob;
                        short x3;
                        switch(skillid) {
                        case 1121010:
                           chr.handleOrbconsume(1121015);
                           effect.applyTo(chr);
                           break;
                        case 1121016:
                        case 1221014:
                        case 1321014:
                        case 51111005:
                           pos = slea.readPos();
                           chr.setPosition(pos);
                           rltype = slea.readByte();

                           for(tesla = 0; tesla < rltype; ++tesla) {
                              mob = chr.getMap().getMonsterByOid(slea.readInt());
                              if (mob != null) {
                                 mob.applyStatus(c, MonsterStatus.MS_MagicCrash, new MonsterStatusEffect(effect.getSourceId(), effect.getDuration()), effect.getDuration(), effect);
                              }
                           }

                           effect.applyToBuff(chr);
                           break;
                        case 1211013:
                           pos = slea.readPos();
                           chr.setPosition(pos);
                           rltype = slea.readByte();
                           SecondaryStatEffect bonusTime = null;
                           if (chr.getSkillLevel(1220043) > 0) {
                              bonusTime = SkillFactory.getSkill(1220043).getEffect(chr.getSkillLevel(1220043));
                           }

                           SecondaryStatEffect bonusChance = null;
                           if (chr.getSkillLevel(1220044) > 0) {
                              bonusChance = SkillFactory.getSkill(1220044).getEffect(chr.getSkillLevel(1220044));
                           }

                           enhance = null;
                           if (chr.getSkillLevel(1220045) > 0) {
                              enhance = SkillFactory.getSkill(1220045).getEffect(chr.getSkillLevel(1220045));
                           }

                           summon8 = null;

                           for(i4 = 0; i4 < rltype; ++i4) {
                              MapleMonster monster2 = chr.getMap().getMonsterByOid(slea.readInt());
                              mobs = new ArrayList();
                              if (monster2 != null) {
                                 MonsterStatus ms = MonsterStatus.MS_IndiePdr;
                                 mobs.add(new Triple(ms, new MonsterStatusEffect(skillid, effect.getDuration() + (bonusTime != null ? bonusTime.getDuration() : 0)), effect.getX() + (enhance != null ? enhance.getX() : 0)));
                                 ms = MonsterStatus.MS_IndieMdr;
                                 mobs.add(new Triple(ms, new MonsterStatusEffect(skillid, effect.getDuration() + (bonusTime != null ? bonusTime.getDuration() : 0)), effect.getX() + (enhance != null ? enhance.getX() : 0)));
                                 ms = MonsterStatus.MS_Pad;
                                 mobs.add(new Triple(ms, new MonsterStatusEffect(skillid, effect.getDuration() + (bonusTime != null ? bonusTime.getDuration() : 0)), effect.getX() + (enhance != null ? enhance.getX() : 0)));
                                 ms = MonsterStatus.MS_Mad;
                                 mobs.add(new Triple(ms, new MonsterStatusEffect(skillid, effect.getDuration() + (bonusTime != null ? bonusTime.getDuration() : 0)), effect.getX() + (enhance != null ? enhance.getX() : 0)));
                                 ms = MonsterStatus.MS_Blind;
                                 mobs.add(new Triple(ms, new MonsterStatusEffect(skillid, effect.getDuration() + (bonusTime != null ? bonusTime.getDuration() : 0)), effect.getZ() + (enhance != null ? enhance.getY() : 0)));
                                 applys2 = new ArrayList();
                                 lp = mobs.iterator();

                                 while(lp.hasNext()) {
                                    Triple<MonsterStatus, MonsterStatusEffect, Integer> status = (Triple)lp.next();
                                    if (status.left != null && status.mid != null && Randomizer.isSuccess(effect.getProp() + (bonusChance != null ? bonusChance.getProp() : 0))) {
                                       ((MonsterStatusEffect)status.mid).setValue((long)(Integer)status.right);
                                       applys2.add(new Pair((MonsterStatus)status.left, (MonsterStatusEffect)status.mid));
                                    }
                                 }

                                 monster2.applyStatus(c, applys2, effect);
                              }
                           }

                           effect.applyToBuff(chr);
                           break;
                        case 1221015:
                           chr.GiveHolyUnityBuff(effect, skillid);
                           effect.applyTo(chr);
                           break;
                        case 1221054:
                           chr.GiveHolyUnityBuff(effect, skillid);
                           effect.applyTo(chr);
                           break;
                        case 1321015:
                           chr.removeCooldown(1321013);
                           effect.applyToBuff(chr);
                           break;
                        case 2001009:
                           if (chr.getBuffedValue(2201009)) {
                              mobsize = chr.getPosition().y;
                              skillid2 = chr.getPosition().x;
                              intervar = skillid2 - 200;
                              mobid2 = (skillid2 - intervar) / 2 + intervar;
                              SecondaryStatEffect a4 = SkillFactory.getSkill(2201009).getEffect(chr.getSkillLevel(2201009));
                              if (Randomizer.isSuccess(60)) {
                                 MapleMist mist2 = new MapleMist(a4.calculateBoundingBox(new Point(skillid2, mobsize), chr.isFacingLeft()), chr, a4, 6000, (byte)(chr.isFacingLeft() ? 1 : 0));
                                 mist2.setDelay(1);
                                 chr.getMap().spawnMist(mist2, false);
                                 mist2 = new MapleMist(a4.calculateBoundingBox(new Point(intervar, mobsize), chr.isFacingLeft()), chr, a4, 6000, (byte)(chr.isFacingLeft() ? 1 : 0));
                                 mist2.setDelay(1);
                                 chr.getMap().spawnMist(mist2, false);
                                 mist2 = new MapleMist(a4.calculateBoundingBox(new Point(mobid2, mobsize), chr.isFacingLeft()), chr, a4, 6000, (byte)(chr.isFacingLeft() ? 1 : 0));
                                 mist2.setDelay(1);
                                 chr.getMap().spawnMist(mist2, false);
                              }

                              chr.getClient().getSession().writeAndFlush(CWvsContext.enableActions(chr));
                           } else {
                              effect.applyTo(chr);
                           }
                           break;
                        case 2121052:
                           moblist1 = new ArrayList();
                           tesla = slea.readByte();
                           active = false;

                           for(i6 = 0; i6 < tesla; ++i6) {
                              monster3 = chr.getMap().getMonsterByOid(slea.readInt());
                              moblist1.add(monster3);
                           }

                           slea.skip(3);
                           List<SecondAtom2> at = SkillFactory.getSkill(2121052).getSecondAtoms();
                           monsters2 = new ArrayList();
                           i = 0;

                           for(var99 = at.iterator(); var99.hasNext(); ++i) {
                              SecondAtom2 atom = (SecondAtom2)var99.next();
                              atom.setTarget(((MapleMonster)moblist1.get(0)).getObjectId());
                              if (i < 3) {
                                 monsters2.add(atom);
                              }
                           }

                           chr.setSkillCustomInfo(skillid, 3L, 0L);
                           chr.createSecondAtom((List)monsters2, slea.readIntPos(), chr.isFacingLeft());
                           effect.applyToBuff(chr);
                           break;
                        case 2301002:
                           mobsize = 0;
                           skillid2 = 4000;
                           pos = slea.readPos();
                           if (chr.getMapId() != 450013700 && chr.getParty() != null) {
                              var84 = chr.getParty().getMembers().iterator();

                              while(var84.hasNext()) {
                                 chr2 = (MaplePartyCharacter)var84.next();
                                 if (chr2.isOnline() && chr.getId() != chr2.getPlayer().getId() && chr2.getMapid() == chr.getMapId()) {
                                    chr6 = chr.getClient().getChannelServer().getPlayerStorage().getCharacterByName(chr2.getName());
                                    if (chr6 != null && chr2.getPlayer().isAlive() && chr.getTruePosition().x + 450 > chr6.getTruePosition().x && chr.getTruePosition().x - 450 < chr6.getTruePosition().x && chr.getTruePosition().y + 300 > chr6.getTruePosition().y && chr.getTruePosition().y - 300 < chr6.getTruePosition().y) {
                                       chr6.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr6, 0, skillid, 4, 0, 0, (byte)(chr6.getTruePosition().x > pos.x ? 1 : 0), true, chr6.getPosition(), (String)null, (Item)null));
                                       chr6.getMap().broadcastMessage(chr6, CField.EffectPacket.showEffect(chr6, 0, skillid, 4, 0, 0, (byte)(chr6.getTruePosition().x > pos.x ? 1 : 0), false, chr6.getPosition(), (String)null, (Item)null), false);
                                       ++mobsize;
                                       if (chr6.hasDisease(SecondaryStat.Undead)) {
                                          chr6.addHP((long)(-chr.getStat().AfterStatWatk(chr) * 3));
                                       } else {
                                          chr6.addHP((long)(chr.getStat().AfterStatWatk(chr) * 3));
                                       }

                                       if (chr6.getDisease(SecondaryStat.GiveMeHeal) != null) {
                                          chr6.cancelDisease(SecondaryStat.GiveMeHeal);
                                       }
                                    }
                                 }
                              }

                              if (mobsize > 1) {
                                 skillid2 -= 2000;
                              }
                           }

                           c.getSession().writeAndFlush(CField.skillCooldown(skillid, skillid2));
                           chr.addCooldown(skillid, System.currentTimeMillis(), (long)skillid2);
                           if (chr.hasDisease(SecondaryStat.Undead)) {
                              chr.addHP((long)(-chr.getStat().AfterStatWatk(chr) * 3));
                           } else {
                              chr.addHP((long)(chr.getStat().AfterStatWatk(chr) * 3));
                           }

                           effect.applyTo(chr);
                           break;
                        case 2321007:
                           mobsize = slea.readInt();
                           facing2 = chr.getBuffedValue(2321054);
                           long healhp = chr.getStat().getCurrentMaxHp() / 100L * 20L;
                           if (facing2) {
                              healhp = chr.getStat().getCurrentMaxHp() / 100L * 20L / 100L * 40L;
                           }

                           if (mobsize == 48 && chr.getParty() != null && chr.getMapId() != 450013700) {
                              var105 = chr.getParty().getMembers().iterator();

                              while(var105.hasNext()) {
                                 MaplePartyCharacter chr4 = (MaplePartyCharacter)var105.next();
                                 MapleCharacter curChar3 = chr.getClient().getChannelServer().getPlayerStorage().getCharacterById(chr4.getId());
                                 if (curChar3 != null && chr.getMapId() == curChar3.getMapId() && curChar3.isAlive()) {
                                    if (curChar3.hasDisease(SecondaryStat.Undead)) {
                                       curChar3.addHP(-healhp);
                                    } else {
                                       curChar3.addHP(healhp);
                                    }
                                 }
                              }
                           }

                           if (chr.isAlive()) {
                              if (chr.hasDisease(SecondaryStat.Undead)) {
                                 chr.addHP(-healhp);
                              } else {
                                 chr.addHP(healhp);
                              }
                           }

                           effect.applyTo(chr);
                           break;
                        case 2321015:
                           x3 = slea.readShort();
                           skillid2 = chr.getStat().getTotalInt() / 2500;

                           for(intervar = 0; intervar < x3; ++intervar) {
                              pos2 = slea.readIntPos();
                              newmist3 = new MapleMist(effect.calculateBoundingBox(pos2, chr.isFacingLeft()), c.getPlayer(), effect, 5000 + 5000 * skillid2, (byte)(chr.isFacingLeft() ? 1 : 0));
                              newmist3.setPosition(pos2);
                              c.getPlayer().getMap().spawnMist(newmist3, false);
                              effect.applyTo(chr);
                           }

                           chr.홀리워터스택 -= x3;
                           HashMap<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
                           statups.put(SecondaryStat.HolyWater, new Pair(chr.홀리워터스택, 0));
                           chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, (SecondaryStatEffect)null, chr));
                           break;
                        case 3011004:
                        case 3300002:
                        case 3321003:
                           slea.skip(4);
                           rltype = slea.readByte();
                           new ArrayList();
                           toRemove = new ArrayList();

                           for(mobid2 = 0; mobid2 < rltype; ++mobid2) {
                              toRemove.add(slea.readInt());
                           }

                           effect.applyTo(chr);
                           if (!toRemove.isEmpty()) {
                              atom2 = new MapleAtom(false, chr.getId(), 57, true, 3310004, chr.getTruePosition().x, chr.getTruePosition().y);
                              monsters2 = new ArrayList();
                              effect.applyTo(chr);
                              c.getSession().writeAndFlush(CWvsContext.enableActions(chr, true, false));
                              if (chr.문양 == 2 && chr.getSkillLevel(3310004) > 0) {
                                 SecondaryStatEffect editionalBlast = SkillFactory.getSkill(3310004).getEffect(chr.getSkillLevel(3310004));
                                 bullet = editionalBlast.getBulletCount();
                                 if (chr.getBuffedValue(3321034)) {
                                    ++bullet;
                                 }

                                 if (editionalBlast.makeChanceResult()) {
                                    for(byte i7 = 0; i7 < bullet; ++i7) {
                                       monsters2.add(0);
                                       atom2.addForceAtom(new ForceAtom(2, Randomizer.rand(10, 43), Randomizer.rand(1, 4), 0, 60, chr.getTruePosition()));
                                    }

                                    atom2.setSearchX1(500);
                                    atom2.setSearchY1(200);
                                    atom2.setnDuration(2);
                                    atom2.setSearchX(310);
                                    atom2.setSearchY(-67);
                                    atom2.setDwTargets(monsters2);
                                    chr.getMap().spawnMapleAtom(atom2);
                                 }
                              }

                              atom2 = new MapleAtom(false, chr.getId(), 56, true, skillid, chr.getTruePosition().x, chr.getTruePosition().y);
                              monsters2.clear();

                              for(i4 = 0; i4 < toRemove.size(); ++i4) {
                                 monsters2.add((Integer)toRemove.get(i4));
                                 atom2.addForceAtom(new ForceAtom(2, 23, 10, Randomizer.rand(5, 15), 60));
                              }

                              atom2.setDwTargets(monsters2);
                              chr.getMap().spawnMapleAtom(atom2);
                           }

                           MapleCharacter.문양(c, skillid);
                           break;
                        case 3311002:
                        case 3311003:
                        case 3321006:
                        case 3321007:
                           Vmatrixstackbuff(c, true, slea);
                           break;
                        case 4111009:
                        case 5201008:
                        case 14110031:
                        case 14111025:
                           mobsize = slea.readInt();
                           if (!MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, mobsize, effect.getBulletConsume(), false, true)) {
                              chr.dropMessage(5, "불릿이 부족합니다.");
                           } else {
                              effect.applyTo(chr, mobsize);
                           }

                           c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                           break;
                        case 4211006:
                        case 4221019:
                           if (chr.getBuffedValue(4221018)) {
                              SkillFactory.getSkill(4221020).getEffect(1).applyTo(chr, false, false);
                           }

                           chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, skillid, 1, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), true, chr.getTruePosition(), (String)null, (Item)null));
                           chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 0, skillid, 1, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), false, chr.getTruePosition(), (String)null, (Item)null), false);
                           effect2 = SkillFactory.getSkill(4210014).getEffect(1);
                           List<MapleMapObject> drops = c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getPosition(), 500000.0D, Arrays.asList(MapleMapObjectType.ITEM));
                           toRemove = new ArrayList();
                           var112 = c.getPlayer().getPickPocket().iterator();

                           MapleMapItem o;
                           while(var112.hasNext()) {
                              o = (MapleMapItem)var112.next();
                              boolean active2 = false;
                              var99 = c.getPlayer().getMap().getAllItems().iterator();

                              while(var99.hasNext()) {
                                 MapleMapItem o2 = (MapleMapItem)var99.next();
                                 if (o2.isPickpoket() && o2.getObjectId() == o.getObjectId()) {
                                    active2 = true;
                                    break;
                                 }
                              }

                              if (!active2) {
                                 toRemove.add(o);
                              }
                           }

                           var112 = toRemove.iterator();

                           while(var112.hasNext()) {
                              o = (MapleMapItem)var112.next();
                              c.getPlayer().getPickPocket().remove(o);
                           }

                           allmesos = new ArrayList();
                           max = effect.getBulletCount();
                           if (chr.getSkillLevel(4220045) > 0) {
                              max += SkillFactory.getSkill(4220045).getEffect(chr.getSkillLevel(4220045)).getBulletCount();
                           }

                           for(i = 0; i < drops.size(); ++i) {
                              MapleMapItem drop = (MapleMapItem)drops.get(i);
                              if (drop.isPickpoket() && drop.getOwner() == c.getPlayer().getId() && allmesos.size() < max) {
                                 allmesos.add(drop);
                              }
                           }

                           MapleAtom atom3 = new MapleAtom(false, chr.getId(), chr.getBuffedValue(4221018) ? 75 : 12, true, 4210014, chr.getTruePosition().x, chr.getTruePosition().y);
                           monsters3 = new ArrayList();
                           mobs = new ArrayList();
                           Iterator var90 = chr.getMap().getAllMonster().iterator();

                           while(var90.hasNext()) {
                              MapleMonster mob3 = (MapleMonster)var90.next();
                              if (chr.getTruePosition().x + effect2.getLt().x < mob3.getTruePosition().x && chr.getTruePosition().x - effect2.getLt().x > mob3.getTruePosition().x && chr.getTruePosition().y + 550 > mob3.getTruePosition().y && chr.getTruePosition().y - 550 < mob3.getTruePosition().y && mob3.isAlive()) {
                                 mobs.add(mob3);
                              }
                           }

                           if (!mobs.isEmpty()) {
                              int i8;
                              for(i8 = 0; i8 < allmesos.size(); ++i8) {
                                 randmob_remove = Randomizer.rand(0, Math.max(0, mobs.size() - 1));
                                 monsters3.add(mobs.get(randmob_remove) != null ? ((MapleMonster)mobs.get(randmob_remove)).getObjectId() : 0);
                                 atom3.addForceAtom(new ForceAtom(1, 42 + (Randomizer.nextBoolean() ? 1 : 0), 4, Randomizer.rand(10, 65), 300, ((MapleMapItem)allmesos.get(i8)).getTruePosition()));
                              }

                              for(i8 = 0; i8 < allmesos.size(); ++i8) {
                                 chr.getMap().broadcastMessage(CField.removeItemFromMap(((MapleMapItem)allmesos.get(i8)).getObjectId(), 0, chr.getId()));
                                 chr.getMap().removeMapObject((MapleMapObject)allmesos.get(i8));
                                 chr.RemovePickPocket((MapleMapItem)allmesos.get(i8));
                              }

                              effect.applyTo(chr);
                              c.getSession().writeAndFlush(CWvsContext.enableActions(chr, true, false));
                              if (monsters3.isEmpty()) {
                                 return;
                              }

                              SecondaryStatEffect eff2 = chr.getBuffedEffect(SecondaryStat.PickPocket);
                              atom3.setDwTargets(monsters3);
                              chr.getMap().spawnMapleAtom(atom3);
                              c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(eff2.getStatups(), eff2, chr));
                           }
                           break;
                        case 4221052:
                           c.getPlayer().setSkillCustomInfo(4221052, (long)chr.getPosition().x, 12000L);
                           c.getSession().writeAndFlush(CWvsContext.onSkillUseResult(skillid));
                           effect.applyTo(chr);
                           break;
                        case 4331006:
                           c.getSession().writeAndFlush(CWvsContext.onSkillUseResult(skillid));
                           effect.applyTo(chr);
                           break;
                        case 5111017:
                           chr.서펜트스톤 = 0;
                           chr.cancelEffectFromBuffStat(SecondaryStat.SerpentStone, 5111017);
                           SkillFactory.getSkill(5110020).getEffect(1).applyTo(chr, false, false);
                           break;
                        case 5121010:
                           bufflist = new Integer[]{5121010, 80002282, 2321055, 2023661, 2023662, 2023663, 2023664, 2023665, 2023666, 2450064, 2450134, 2450038, 2450124, 2450147, 2450148, 2450149, 2023558, 2003550, 2023556, 2003551};
                           Iterator var159;
                           if (chr.getParty() != null) {
                              var159 = chr.getParty().getMembers().iterator();

                              while(true) {
                                 MaplePartyCharacter pc;
                                 do {
                                    if (!var159.hasNext()) {
                                       return;
                                    }

                                    pc = (MaplePartyCharacter)var159.next();
                                 } while(pc.getPlayer().getBuffedValue(5121010));

                                 var112 = pc.getPlayer().getCooldowns().iterator();

                                 while(var112.hasNext()) {
                                    MapleCoolDownValueHolder i = (MapleCoolDownValueHolder)var112.next();
                                    if (!Arrays.asList(bufflist).contains(i.skillId) && !SkillFactory.getSkill(i.skillId).isHyper() && i.skillId / 10000 <= pc.getPlayer().getJob()) {
                                       pc.getPlayer().removeCooldown(i.skillId);
                                       pc.getPlayer().getClient().getSession().writeAndFlush(CField.skillCooldown(i.skillId, 0));
                                    }
                                 }

                                 pc.getPlayer().addCooldown(5121010, System.currentTimeMillis(), 180000L);
                                 SkillFactory.getSkill(5121010).getEffect(chr.getSkillLevel(5121010)).applyTo(pc.getPlayer(), false, false);
                              }
                           } else {
                              if (!chr.getBuffedValue(5121010)) {
                                 var159 = chr.getCooldowns().iterator();

                                 while(var159.hasNext()) {
                                    MapleCoolDownValueHolder i = (MapleCoolDownValueHolder)var159.next();
                                    if (!Arrays.asList(bufflist).contains(i.skillId) && !SkillFactory.getSkill(i.skillId).isHyper() && i.skillId / 10000 <= chr.getJob()) {
                                       chr.removeCooldown(i.skillId);
                                       chr.getClient().getSession().writeAndFlush(CField.skillCooldown(i.skillId, 0));
                                    }
                                 }

                                 chr.addCooldown(5121010, System.currentTimeMillis(), 180000L);
                                 SkillFactory.getSkill(5121010).getEffect(chr.getSkillLevel(5121010)).applyTo(chr, false, false);
                              }
                              break;
                           }
                        case 11111023:
                           pos = slea.readPos();
                           chr.setPosition(pos);
                           rltype = slea.readByte();

                           for(tesla = 0; tesla < rltype; ++tesla) {
                              mob = chr.getMap().getMonsterByOid(slea.readInt());
                              allmesos = new ArrayList();
                              max = effect.getDuration();
                              if (mob != null) {
                                 if (chr.getSkillLevel(11120043) > 0) {
                                    max += SkillFactory.getSkill(11120043).getEffect(1).getDuration();
                                 }

                                 allmesos.add(new Pair(MonsterStatus.MS_TrueSight, new MonsterStatusEffect(effect.getSourceId(), max, chr.getSkillLevel(11120045) > 0 ? (long)(-SkillFactory.getSkill(11120044).getEffect(1).getW()) : 0L)));
                                 allmesos.add(new Pair(MonsterStatus.MS_IndieUNK, new MonsterStatusEffect(effect.getSourceId(), max, (long)effect.getS())));
                                 if (chr.getSkillLevel(11120045) > 0) {
                                    allmesos.add(new Pair(MonsterStatus.MS_IndiePdr, new MonsterStatusEffect(effect.getSourceId(), max, (long)(-(effect.getV() + SkillFactory.getSkill(11120045).getEffect(1).getW())))));
                                    allmesos.add(new Pair(MonsterStatus.MS_IndieMdr, new MonsterStatusEffect(effect.getSourceId(), max, (long)(-(effect.getV() + SkillFactory.getSkill(11120045).getEffect(1).getW())))));
                                 } else {
                                    allmesos.add(new Pair(MonsterStatus.MS_IndiePdr, new MonsterStatusEffect(effect.getSourceId(), max, (long)(-effect.getV()))));
                                    allmesos.add(new Pair(MonsterStatus.MS_IndieMdr, new MonsterStatusEffect(effect.getSourceId(), max, (long)(-effect.getV()))));
                                 }

                                 mob.applyStatus(c, allmesos, effect);
                              }
                           }

                           effect.applyToBuff(chr);
                           break;
                        case 11121018:
                           chr.setCosmicCount(0);
                           moblist1 = new ArrayList();
                           mses = new ArrayList();
                           size = slea.readByte();

                           for(mobid2 = 0; mobid2 < size; ++mobid2) {
                              mses.add(slea.readInt());
                           }

                           slea.skip(3);
                           mobid2 = slea.readInt();
                           max = slea.readInt();
                           i = 0;
                           bullet = 0;
                           int[] x_ = new int[]{-156, -97, -2, 93, 152, 152, -93, -2, -97, -156};
                           int[] y_ = new int[]{-102, -183, -214, -183, -102, -102, 79, 110, 79, -2};

                           for(randmob_remove = 0; i < chr.getCosmicCount(); bullet += 60) {
                              randmob_remove = i < size ? (Integer)mses.get(i) : randmob_remove;
                              moblist1.add(new SecondAtom(42, c.getPlayer().getId(), randmob_remove, 420 + bullet, skillid, 2000, 0, 1, new Point(mobid2 + x_[i], max + y_[i]), Arrays.asList(0, 0)));
                              ++i;
                           }

                           chr.getMap().spawnSecondAtom(chr, moblist1, 0);
                           break;
                        case 11121054:
                           chr.setCosmicCount(0);
                           effect2 = SkillFactory.getSkill(skillid).getEffect(chr.getSkillLevel(skillid));
                           skillid2 = 0;

                           for(intervar = 0; intervar < chr.getCosmicCount(); ++intervar) {
                              skillid2 += 20000;
                           }

                           effect2.applyTo(chr, effect2.getDuration() + skillid2);
                           break;
                        case 12120013:
                           effect2 = SkillFactory.getSkill(skillid).getEffect(chr.getSkillLevel(skillid));
                           effect2.applyTo(chr, 300000);
                           break;
                        case 12120014:
                           effect2 = SkillFactory.getSkill(skillid).getEffect(chr.getSkillLevel(skillid));
                           effect2.applyTo(chr, 300000);
                           break;
                        case 13101022:
                           if (chr.getSkillLevel(13120003) > 0) {
                              SkillFactory.getSkill(13120003).getEffect(chr.getSkillLevel(13120003)).applyTo(chr);
                           } else if (chr.getSkillLevel(13110022) > 0) {
                              SkillFactory.getSkill(13110022).getEffect(chr.getSkillLevel(13110022)).applyTo(chr);
                           } else {
                              effect.applyTo(chr);
                           }
                           break;
                        case 14121054:
                           if (chr.getBuffedValue(14111024)) {
                              chr.cancelEffect(chr.getBuffedEffect(14111024));
                           }

                           SkillFactory.getSkill(14111024).getEffect(20).applyTo(chr);
                           summon2 = new MapleSummon(chr, 14121055, chr.getTruePosition(), SummonMovementType.ShadowServant, (byte)0, effect.getDuration());
                           chr.addSummon(summon2);
                           chr.getMap().spawnSummon(summon2, effect.getDuration());
                           summon4 = new MapleSummon(chr, 14121056, chr.getTruePosition(), SummonMovementType.ShadowServant, (byte)0, effect.getDuration());
                           chr.addSummon(summon4);
                           chr.getMap().spawnSummon(summon4, effect.getDuration());
                           effect.applyTo(chr);
                           break;
                        case 15121054:
                           chr.removeCooldown(15120003);
                           effect.applyTo(chr);
                           break;
                        case 22110013:
                           rltype = slea.readByte();

                           for(tesla = 0; tesla < rltype; ++tesla) {
                              mob = chr.getMap().getMonsterByOid(slea.readInt());
                              if (mob != null) {
                                 mob.applyStatus(c, MonsterStatus.MS_Weakness, new MonsterStatusEffect(effect.getSourceId(), effect.getDuration()), effect.getX(), effect);
                              }
                           }

                           chr.addCooldown(22111017, System.currentTimeMillis(), 2000L);
                           c.getSession().writeAndFlush(CField.skillCooldown(22111017, 2000));
                           effect.applyToBuff(chr);
                           break;
                        case 22140013:
                           chr.addCooldown(22111017, System.currentTimeMillis(), 2000L);
                           c.getSession().writeAndFlush(CField.skillCooldown(22111017, 2000));
                           effect.applyToBuff(chr);
                           break;
                        case 22141017:
                        case 22170070:
                           moblist1 = new ArrayList();
                           mses = new ArrayList();
                           List<MapleMapObject> mobjects = c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getPosition(), 600000.0D, Arrays.asList(MapleMapObjectType.MONSTER));
                           atom2 = new MapleAtom(false, chr.getId(), skillid == 22141017 ? 23 : 24, true, skillid, chr.getTruePosition().x, chr.getTruePosition().y);
                           var105 = chr.getMap().getAllFieldThreadsafe().iterator();

                           MapleMagicWreck re;
                           while(var105.hasNext()) {
                              re = (MapleMagicWreck)var105.next();
                              if (re.getChr().getId() == chr.getId() && re.getSourceid() == skillid) {
                                 atom2.addForceAtom(new ForceAtom(1, Randomizer.rand(40, 44), Randomizer.rand(3, 5), Randomizer.rand(7, 330), 500, re.getTruePosition()));
                                 moblist1.add(re);
                                 bullet = Randomizer.rand(0, mobjects.size() - 1);
                                 mses.add(((MapleMapObject)mobjects.get(bullet)).getObjectId());
                              }
                           }

                           if (mses.isEmpty()) {
                              effect.applyTo(chr);
                              return;
                           }

                           atom2.setDwTargets(mses);
                           chr.getMap().spawnMapleAtom(atom2);
                           c.getPlayer().getMap().broadcastMessage(CField.removeMagicWreck(chr, moblist1));
                           var105 = moblist1.iterator();

                           while(var105.hasNext()) {
                              re = (MapleMagicWreck)var105.next();
                              chr.getMap().getWrecks().remove(re);
                              chr.getMap().removeMapObject(re);
                           }

                           effect.applyTo(chr);
                           break;
                        case 22170064:
                           var117 = chr.getMap().getAllMistsThreadsafe().iterator();

                           while(var117.hasNext()) {
                              mist3 = (MapleMist)var117.next();
                              if (mist3.getSourceSkill() != null && mist3.getSourceSkill().getId() == 22170093 && chr.getId() == mist3.getOwnerId()) {
                                 chr.getMap().broadcastMessage(CField.removeMist(mist3));
                                 chr.getMap().removeMapObject(mist3);
                                 break;
                              }
                           }

                           SkillFactory.getSkill(22170093).getEffect(20).applyTo(chr);
                           chr.addCooldown(22111017, System.currentTimeMillis(), 2000L);
                           c.getSession().writeAndFlush(CField.skillCooldown(22111017, 2000));
                           c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.EffectPacket.showEffect(c.getPlayer(), 0, skillid, 1, 0, 0, (byte)0, false, (Point)null, (String)null, (Item)null), false);
                           break;
                        case 24121007:
                           pos = slea.readPos();
                           rltype = slea.readByte();
                           mses = new ArrayList();

                           for(intervar = 0; intervar < rltype; ++intervar) {
                              MapleMonster life = chr.getMap().getMonsterByOid(slea.readInt());
                              if (life != null) {
                                 if (life.isBuffed(MonsterStatus.MS_PImmune)) {
                                    mses.add(new Pair(MonsterStatus.MS_PImmune, life.getBuff(MonsterStatus.MS_PImmune)));
                                 }

                                 if (life.isBuffed(MonsterStatus.MS_MImmune)) {
                                    mses.add(new Pair(MonsterStatus.MS_MImmune, life.getBuff(MonsterStatus.MS_MImmune)));
                                 }

                                 if (life.isBuffed(MonsterStatus.MS_PCounter)) {
                                    mses.add(new Pair(MonsterStatus.MS_PCounter, life.getBuff(MonsterStatus.MS_PCounter)));
                                 }

                                 if (life.isBuffed(MonsterStatus.MS_MCounter)) {
                                    mses.add(new Pair(MonsterStatus.MS_MCounter, life.getBuff(MonsterStatus.MS_MCounter)));
                                 }

                                 if (mses.size() > 0) {
                                    life.cancelStatus(mses);
                                 }
                              }
                           }

                           effect.applyToBuff(chr);
                           break;
                        case 25100002:
                           effect.applyTo(chr);
                           rltype = slea.readByte();

                           for(tesla = 0; tesla < rltype; ++tesla) {
                              mob = chr.getMap().getMonsterByOid(slea.readInt());
                              allmesos = new ArrayList();
                              if (mob != null) {
                                 allmesos.add(new Pair(MonsterStatus.MS_Speed, new MonsterStatusEffect(effect.getSourceId(), 5000, (long)(-linkEffect.getS()))));
                                 mob.applyStatus(c, allmesos, effect);
                              }
                           }

                           return;
                        case 27111101:
                           pos = slea.readPos();
                           chr.setPosition(pos);
                           if (chr.getMapId() != 450013700 && chr.getParty() != null) {
                              var117 = chr.getParty().getMembers().iterator();

                              while(var117.hasNext()) {
                                 chr3 = (MaplePartyCharacter)var117.next();
                                 if (chr3.isOnline() && chr3.getMapid() == chr.getMapId()) {
                                    chr5 = chr.getClient().getChannelServer().getPlayerStorage().getCharacterByName(chr3.getName());
                                    if (chr5 != null && chr3.isOnline() && chr.getTruePosition().x + 450 > chr5.getTruePosition().x && chr.getTruePosition().x - 450 < chr5.getTruePosition().x && chr.getTruePosition().y + 400 > chr5.getTruePosition().y && chr.getTruePosition().y - 400 < chr5.getTruePosition().y) {
                                       chr5.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr5, 0, skillid, 4, 0, 0, (byte)(chr5.getTruePosition().x > pos.x ? 1 : 0), true, chr5.getPosition(), (String)null, (Item)null));
                                       chr5.getMap().broadcastMessage(chr5, CField.EffectPacket.showEffect(chr5, 0, skillid, 4, 0, 0, (byte)(chr5.getTruePosition().x > pos.x ? 1 : 0), false, chr5.getPosition(), (String)null, (Item)null), false);
                                       if (chr5.hasDisease(SecondaryStat.Undead)) {
                                          chr5.addHP((long)(-chr.getStat().AfterStatWatk(chr) * 8));
                                       } else {
                                          chr5.addHP((long)(chr.getStat().AfterStatWatk(chr) * 8));
                                       }

                                       if (chr5.getDisease(SecondaryStat.GiveMeHeal) != null) {
                                          chr5.cancelDisease(SecondaryStat.GiveMeHeal);
                                       }
                                    }
                                 }
                              }
                           }

                           if (chr.hasDisease(SecondaryStat.Undead)) {
                              chr.addHP((long)(-chr.getStat().AfterStatWatk(chr) * 8));
                           } else {
                              chr.addHP((long)(chr.getStat().AfterStatWatk(chr) * 8));
                           }

                           effect.applyTo(chr);
                           break;
                        case 27121012:
                           c.removeClickedNPC();
                           NPCScriptManager.getInstance().dispose(c);
                           c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                           NPCScriptManager.getInstance().start(c, 2007, "Luminus_Skill_Select2");
                           break;
                        case 30001061:
                           mobsize = slea.readInt();
                           mob4 = chr.getMap().getMonsterByOid(mobsize);
                           if (mob4 != null) {
                              active = mob4.getId() >= 9304000 && mob4.getId() < 9305000;
                              c.getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, skillid, 1, 0, 0, (byte)(active ? 1 : 0), true, (Point)null, (String)null, (Item)null));
                              chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 0, skillid, 1, 0, 0, (byte)(active ? 1 : 0), false, (Point)null, (String)null, (Item)null), chr.getTruePosition());
                              chr.getMap().broadcastMessage(MobPacket.catchMonster(mob4.getObjectId(), (byte)(active ? 1 : 0)));
                              if (!active) {
                                 chr.dropMessage(5, "몬스터의 체력이 너무 많아 포획할 수 없습니다.");
                              } else {
                                 mobid2 = GameConstants.getJaguarType(mob4.getId());
                                 String info = chr.getInfoQuest(23008);

                                 for(i = 0; i <= 8; ++i) {
                                    if (!info.contains(i + "=1") && i == mobid2) {
                                       info = info + i + "=1;";
                                    }
                                 }

                                 chr.updateInfoQuest(23008, info);
                                 chr.updateInfoQuest(123456, String.valueOf(mobid2 * 10));
                                 chr.getMap().killMonster(mob4, chr, true, false, (byte)1);
                                 c.getSession().writeAndFlush(CWvsContext.updateJaguar(chr));
                              }
                           }

                           SkillFactory.getSkill(33110014).getEffect(chr.getSkillLevel(33110014)).applyTo(chr, true);
                           chr.dropMessage(5, "포획에 성공하였습니다.");
                           break;
                        case 31101002:
                           chr.setSkillCustomInfo(31101002, 0L, 700L);
                           c.getSession().writeAndFlush(CField.skillCooldown(skillid, linkEffect.getCooldown(chr)));
                           chr.addCooldown(skillid, System.currentTimeMillis(), (long)linkEffect.getCooldown(chr));
                           break;
                        case 31221001:
                           pos = slea.readPos();
                           chr.setPosition(pos);
                           atom4 = new MapleAtom(false, chr.getId(), 3, true, 31221014, chr.getTruePosition().x, chr.getTruePosition().y);
                           mses = new ArrayList();

                           for(size = 0; size < 2; ++size) {
                              mses.add(slea.readInt());
                              atom4.addForceAtom(new ForceAtom(3, Randomizer.rand(10, 20), Randomizer.rand(20, 35), Randomizer.rand(50, 65), 660));
                           }

                           effect.applyTo(chr);
                           c.getSession().writeAndFlush(CWvsContext.enableActions(chr, true, false));
                           atom4.setDwTargets(mses);
                           chr.getMap().spawnMapleAtom(atom4);
                           break;
                        case 33001001:
                           effect.applyTo(chr);
                           break;
                        case 33001016:
                        case 33001025:
                        case 33101115:
                        case 33111015:
                        case 33121017:
                        case 33121255:
                           pos = slea.readPos();
                           chr.setPosition(pos);
                           if (skillid != 33001016 && skillid != 33111015 && skillid != 33101115) {
                              if (skillid == 33001025) {
                                 objs = chr.getMap().getMapObjectsInRange(chr.getTruePosition(), 500000.0D, Arrays.asList(MapleMapObjectType.MONSTER));
                                 mses = new ArrayList();
                                 if (!objs.isEmpty()) {
                                    for(intervar = 0; intervar < 10 && objs.size() > intervar; ++intervar) {
                                       mses.add((MapleMapObject)objs.get(intervar));
                                    }

                                    var84 = mses.iterator();

                                    while(var84.hasNext()) {
                                       MapleMapObject mobs2 = (MapleMapObject)var84.next();
                                       monsters2 = new ArrayList();
                                       MapleMonster mon = chr.getMap().getMonsterByOid(mobs2.getObjectId());
                                       monsters2.add(new Pair(MonsterStatus.MS_JaguarProvoke, new MonsterStatusEffect(33001025, mon.getStats().isBoss() ? effect.getDuration() / 2 : effect.getDuration(), 1L)));
                                       monsters2.add(new Pair(MonsterStatus.MS_DodgeBodyAttack, new MonsterStatusEffect(33001025, mon.getStats().isBoss() ? effect.getDuration() / 2 : effect.getDuration(), 1L)));
                                       ((MapleMonster)mobs2).applyStatus(c, monsters2, effect);
                                    }
                                 }
                              }
                           } else {
                              if (skillid == 33101115) {
                                 c.getSession().writeAndFlush(CField.skillCooldown(33101215, 7000));
                                 chr.addCooldown(33101215, System.currentTimeMillis(), 7000L);
                              }

                              if (chr.getSkillLevel(33120048) > 0) {
                                 chr.changeCooldown(skillid, -1000);
                              }
                           }

                           c.getSession().writeAndFlush(CField.jaguarAttack(skillid));
                           c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                           break;
                        case 33111013:
                        case 33121016:
                           pos4 = slea.readPos();
                           slea.skip(4);
                           facing2 = slea.readByte() == 1;
                           mist = new MapleMist(effect.calculateBoundingBox(pos4, facing2), c.getPlayer(), effect, effect.getDuration(), (byte)(facing2 ? 1 : 0));
                           mist.setPosition(pos4);
                           c.getPlayer().getMap().spawnMist(mist, false);
                           effect.applyTo(chr);
                           break;
                        case 35101002:
                        case 35110017:
                        case 35120017:
                           rltype = slea.readByte();
                           skillid2 = SkillFactory.getSkill(35101002).getEffect(skillLevel).getBulletCount();
                           if (chr.getSkillLevel(35120017) > 0) {
                              skillid2 += 5;
                           }

                           atom5 = new MapleAtom(false, chr.getId(), 20, true, skillid, chr.getTruePosition().x, chr.getTruePosition().y);
                           allmesos = new ArrayList();

                           for(i10 = 0; i10 < rltype; ++i10) {
                              allmesos.add(slea.readInt());
                              atom5.addForceAtom(new ForceAtom(2, 50, Randomizer.rand(10, 15), Randomizer.rand(0, 25), 500));
                           }

                           if (chr.getBuffedValue(35111003)) {
                              while(rltype < skillid2) {
                                 ++rltype;
                                 atom5.addForceAtom(new ForceAtom(2, 50, Randomizer.rand(10, 15), Randomizer.rand(0, 25), 500));
                              }
                           }

                           if (chr.getBuffedEffect(SecondaryStat.BombTime) != null) {
                              for(i10 = 0; i10 < chr.getBuffedEffect(SecondaryStat.BombTime).getX(); ++i10) {
                                 atom5.addForceAtom(new ForceAtom(2, 50, Randomizer.rand(10, 15), Randomizer.rand(0, 25), 500));
                              }
                           }

                           if (chr.getBuffedEffect(400051041) != null) {
                              for(i10 = 0; i10 < chr.getBuffedEffect(400051041).getX(); ++i10) {
                                 atom5.addForceAtom(new ForceAtom(2, 50, Randomizer.rand(10, 15), Randomizer.rand(0, 25), 500));
                                 SkillFactory.getSkill(400051094).getEffect(chr.getPlayer().getSkillLevel(400051041)).applyTo(chr.getPlayer(), false, false);
                              }
                           }

                           atom5.setDwTargets(allmesos);
                           chr.getMap().spawnMapleAtom(atom5);
                           effect.applyTo(chr);
                           c.getSession().writeAndFlush(CWvsContext.enableActions(chr, true, false));
                           break;
                        case 36111008:
                           c.getPlayer().gainXenonSurplus((short)10, SkillFactory.getSkill(30020232));
                           c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                           break;
                        case 37000010:
                        case 37001001:
                        case 37101001:
                        case 37111003:
                           chr.Cylinder(37000010);
                           effect.applyTo(chr);
                           break;
                        case 37120059:
                           moblist1 = new ArrayList();
                           if (chr.getSkillCustomValue0(37121052) >= 1L) {
                              moblist1.add(new RangeAttack(37120055, chr.getPosition(), -1, 0, 1));
                           }

                           if (chr.getSkillCustomValue0(37121052) >= 2L) {
                              moblist1.add(new RangeAttack(37120056, chr.getPosition(), -1, 0, 1));
                           }

                           if (chr.getSkillCustomValue0(37121052) >= 3L) {
                              moblist1.add(new RangeAttack(37120057, chr.getPosition(), -1, 0, 1));
                           }

                           if (chr.getSkillCustomValue0(37121052) >= 4L) {
                              moblist1.add(new RangeAttack(37120058, chr.getPosition(), -1, 0, 1));
                           }

                           chr.getClient().send(CField.rangeAttack(37121052, moblist1));
                           c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                           chr.removeSkillCustomInfo(37121052);
                           break;
                        case 37121005:
                           chr.cancelEffectFromBuffStat(SecondaryStat.RwBarrier, 37000006);
                           if (chr.getSkillCustomValue0(37000006) > 0L) {
                              chr.addHP(chr.getStat().getCurrentMaxHp() / 2L + chr.getSkillCustomValue0(skillid));
                           } else {
                              chr.addHP(chr.getStat().getCurrentMaxHp() / 2L);
                           }

                           if (chr.getSkillLevel(37120050) > 0) {
                              chr.changeCooldown(37121005, -40000);
                           }

                           chr.removeSkillCustomInfo(37000006);
                           effect.applyTo(chr);
                           break;
                        case 51111004:
                           effect.applyTo(chr);
                           if (chr.getParty() != null) {
                              var117 = chr.getParty().getMembers().iterator();

                              while(var117.hasNext()) {
                                 chr3 = (MaplePartyCharacter)var117.next();
                                 chr5 = chr.getClient().getChannelServer().getPlayerStorage().getCharacterById(chr3.getId());
                                 if (chr5 != null && chr5.getBuffedValue(51111008)) {
                                    chr5.cancelEffectFromBuffStat(SecondaryStat.MichaelSoulLink);
                                    SkillFactory.getSkill(51111008).getEffect(chr.getSkillLevel(51111008)).applyTo(chr, chr5);
                                 }
                              }
                           }
                           break;
                        case 51120057:
                           pos4 = slea.readPos();
                           slea.skip(4);
                           facing2 = slea.readByte() == 1;
                           active = false;
                           if (c.getPlayer().getInfoQuest(1544) == null) {
                              active = true;
                           } else if (c.getPlayer().getInfoQuest(1544).contains("51121009=0")) {
                              active = true;
                           }

                           if (active) {
                              enhance = SkillFactory.getSkill(51120057).getEffect(chr.getSkillLevel(51120057));
                              newmist3 = new MapleMist(enhance.calculateBoundingBox(pos4, facing2), c.getPlayer(), enhance, enhance.getDuration(), (byte)(facing2 ? 1 : 0));
                              newmist3.setPosition(pos4);
                              c.getPlayer().getMap().removeMist(skillid);
                              c.getPlayer().getMap().spawnMist(newmist3, false);
                           }

                           effect.applyTo(chr);
                           break;
                        case 63001002:
                           chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 0, skillid, 1, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), false, chr.getPosition(), (String)null, (Item)null), false);
                           effect.applyTo(chr);
                           break;
                        case 63001004:
                           chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 0, skillid, 1, 0, 0, slea.readByte(), false, new Point(slea.readShort(), slea.readShort()), (String)null, (Item)null), false);
                           effect.applyTo(chr);
                           break;
                        case 63101001:
                           chr.handlePossession(3);
                           effect.applyTo(chr);
                           break;
                        case 63101104:
                           objs = SkillFactory.getSkill(63101104).getSecondAtoms();
                           tesla = slea.readByte();
                           if (tesla >= 6) {
                              tesla = 5;
                           }

                           for(intervar = 0; intervar < tesla; ++intervar) {
                              ((SecondAtom2)objs.get(intervar)).setTarget(slea.readInt());
                           }

                           chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 0, skillid, 1, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), false, chr.getPosition(), (String)null, (Item)null), false);
                           chr.createSecondAtom(objs, chr.getPosition());
                           effect.applyTo(chr);
                           break;
                        case 63121002:
                           chr.handleStackskill(skillid, true);
                           break;
                        case 65111100:
                           pos = slea.readPos();
                           chr.setPosition(pos);
                           rltype = slea.readByte();
                           atom = new MapleAtom(false, chr.getId(), 3, true, 65111007, chr.getTruePosition().x, chr.getTruePosition().y);
                           toRemove = new ArrayList();
                           mobid2 = 0;
                           if (rltype == 1) {
                              mobid2 = slea.readInt();
                              toRemove.add(mobid2);
                              atom.addForceAtom(new ForceAtom(1, Randomizer.rand(10, 20), Randomizer.rand(40, 65), 0, 500));
                           }

                           for(i10 = 0; i10 < rltype; ++i10) {
                              toRemove.add(mobid2);
                              atom.addForceAtom(new ForceAtom(1, Randomizer.rand(10, 20), Randomizer.rand(40, 65), 0, 500));
                           }

                           effect.applyTo(chr);
                           c.getSession().writeAndFlush(CWvsContext.enableActions(chr, true, false));
                           chr.getClient().send(CField.lockSkill(skillid));
                           atom.setDwTargets(toRemove);
                           chr.getMap().spawnMapleAtom(atom);
                           chr.Recharge(skillid);
                           chr.getClient().send(CField.unlockSkill());
                           break;
                        case 101100101:
                           chr.ZeroSkillCooldown(skillid);
                           effect.applyTo(chr);
                           break;
                        case 131001017:
                           summon2 = chr.getSummon(131001017);
                           summon4 = chr.getSummon(131002017);
                           if (summon2 != null && summon4 != null) {
                              skillid = 131003017;
                           } else if (summon2 != null && summon4 == null) {
                              skillid = 131002017;
                           } else {
                              skillid = 131001017;
                           }

                           SkillFactory.getSkill(skillid).getEffect(1).applyTo(chr);
                           break;
                        case 131001019:
                           pos = slea.readPos();
                           mobsize = Randomizer.rand(effect.getX(), effect.getY());

                           for(skillid2 = 0; skillid2 < mobsize; ++skillid2) {
                              intervar = Randomizer.rand(pos.x - 700, pos.x + 700);
                              mobid2 = Randomizer.rand(pos.y - 400, pos.y + 400);
                              summon8 = new MapleSummon(chr, 131001019, new Point(intervar, mobid2), SummonMovementType.STATIONARY, (byte)0, effect.getDuration());
                              chr.getMap().spawnSummon(summon8, effect.getDuration());
                              chr.addSummon(summon8);
                           }

                           effect.applyTo(chr);
                           break;
                        case 131001026:
                           chr.removeSkillCustomInfo(skillid);
                           effect.applyTo(chr, false);
                           SkillFactory.getSkill(131003026).getEffect(1).applyTo(chr);
                           break;
                        case 131001106:
                           effect.applyTo(chr);
                           rltype = slea.readByte();
                           mses = null;
                           MonsterStatusEffect mse = new MonsterStatusEffect(skillid, effect.getDuration());

                           for(i6 = 0; i6 < rltype; ++i6) {
                              monster3 = chr.getMap().getMonsterByOid(slea.readInt());
                              List<Triple<MonsterStatus, MonsterStatusEffect, Integer>> statusz2 = new ArrayList();
                              if (monster3 != null) {
                                 MonsterStatus ms2 = MonsterStatus.MS_IndiePdr;
                                 statusz2.add(new Triple(ms2, mse, effect.getZ()));
                                 ms2 = MonsterStatus.MS_IndieMdr;
                                 statusz2.add(new Triple(ms2, mse, effect.getZ()));
                                 monsters3 = new ArrayList();
                                 Iterator var89 = statusz2.iterator();

                                 while(var89.hasNext()) {
                                    Triple<MonsterStatus, MonsterStatusEffect, Integer> status2 = (Triple)var89.next();
                                    if (status2.left != null && status2.mid != null && Randomizer.isSuccess(effect.getProp())) {
                                       ((MonsterStatusEffect)status2.mid).setValue((long)(Integer)status2.right);
                                       monsters3.add(new Pair((MonsterStatus)status2.left, (MonsterStatusEffect)status2.mid));
                                    }
                                 }

                                 monster3.applyStatus(c, monsters3, effect);
                              }
                           }

                           return;
                        case 131001107:
                        case 131001207:
                           pos4 = slea.readPos();
                           slea.skip(4);
                           facing2 = slea.readByte() == 1;
                           mist = new MapleMist(effect.calculateBoundingBox(pos4, facing2), c.getPlayer(), effect, effect.getDuration(), (byte)(facing2 ? 1 : 0));
                           mist.setPosition(pos4);
                           mist.setDelay(12);
                           c.getPlayer().getMap().spawnMist(mist, false);
                           effect.applyTo(chr);
                           break;
                        case 135001012:
                           newmist = new MapleMist(effect.calculateBoundingBox(ret.plusPosition2, ret.rlType == 1), c.getPlayer(), effect, effect.getDuration(), ret.rlType);
                           newmist.setPosition(ret.plusPosition2);
                           c.getPlayer().getMap().spawnMist(newmist, false);
                           effect.applyTo(chr);
                           break;
                        case 150011074:
                           mobsize = Randomizer.rand(150011075, 150011078);
                           SkillFactory.getSkill(mobsize).getEffect(1).applyTo(chr);
                           break;
                        case 151001001:
                           chr.getMap().broadcastMessage(SkillPacket.CreateSubObtacle(chr, 151001001));
                           break;
                        case 151100002:
                           if (c.getPlayer().getSkillCustomValue0(151121041) > 0L) {
                              c.getPlayer().addSkillCustomInfo(151121041, -1L);
                              pos4 = slea.readPos();
                              effect.applyTo(chr, pos4);
                           }
                           break;
                        case 151101003:
                           mobsize = slea.readInt();
                           summon4 = chr.getMap().getSummonByOid(mobsize);
                           if (summon4 != null) {
                              summon4.removeSummon(chr.getMap(), false);
                           }

                           if (chr.getSkillLevel(151120034) > 0) {
                              chr.에테르핸들러(chr, 20, skillid, false);
                           }
                           break;
                        case 151101006:
                           if (chr.getBuffedValue(skillid)) {
                              chr.cancelEffectFromBuffStat(SecondaryStat.Creation);
                              c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                           } else {
                              chr.에테르핸들러(chr, 0, skillid, true);
                              effect.applyTo(chr);
                           }
                           break;
                        case 151111002:
                           effect.applyTo(chr);
                           c.getSession().writeAndFlush(CWvsContext.onSkillUseResult(0));
                           break;
                        case 151111003:
                           chr.getMap().broadcastMessage(SkillPacket.RemoveSubObtacle(chr, (chr.에테르소드 - 1) * 10));
                           chr.getMap().broadcastMessage(SkillPacket.RemoveSubObtacle(chr, chr.에테르소드 * 10));
                           chr.에테르소드 -= 2;
                           if (chr.에테르소드 <= 0) {
                              chr.에테르소드 = 0;
                           }

                           if (chr.에테르소드 <= 0) {
                              chr.에테르소드 = 0;
                           }

                           if (chr.활성화된소드 <= 0) {
                              chr.활성화된소드 = 0;
                           }

                           chr.에테르핸들러(chr, -100, skillid, false);
                           ++chr.활성화된소드;
                           MapleMagicSword ms4 = new MapleMagicSword(chr, skillid, chr.활성화된소드, 40000, false);
                           chr.getMap().spawnMagicSword(ms4, chr, false);
                           ++chr.활성화된소드;
                           ms3 = new MapleMagicSword(chr, skillid, chr.활성화된소드, 40000, false);
                           chr.getMap().spawnMagicSword(ms3, chr, false);
                           break;
                        case 151111004:
                           chr.에테르핸들러(chr, -20, skillid, false);
                           effect.applyTo(chr);
                           break;
                        case 151121041:
                           var117 = ret.mistPoints.iterator();

                           while(var117.hasNext()) {
                              point = (Point)var117.next();
                              mist = new MapleMist(effect.calculateBoundingBox(point, c.getPlayer().isFacingLeft()), c.getPlayer(), effect, 1020, (byte)(c.getPlayer().isFacingLeft() ? 1 : 0));
                              mist.setPosition(point);
                              c.getPlayer().getMap().spawnMist(mist, false);
                           }

                           c.getPlayer().setSkillCustomInfo(151121041, 5L, 0L);
                           break;
                        case 152001001:
                        case 152120001:
                           pos = slea.readPos();
                           slea.skip(7);
                           atom4 = new MapleAtom(false, chr.getId(), 36, true, skillid, chr.getTruePosition().x, chr.getTruePosition().y);
                           atom4.setDwFirstTargetId(0);
                           fa = new ForceAtom(2, 50, 50, 0, 470);
                           chr.addSkillCustomInfo(skillid, 1L);
                           fa.setnAttackCount((int)chr.getSkillCustomValue0(skillid));
                           atom4.addForceAtom(fa);
                           atom4.setDwUnknownPoint(slea.readInt());
                           effect.applyTo(chr);
                           c.getSession().writeAndFlush(CWvsContext.enableActions(chr, true, false));
                           chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, skillid, 1, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), true, pos, (String)null, (Item)null));
                           chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 0, skillid, 1, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), false, pos, (String)null, (Item)null), false);
                           chr.getMap().spawnMapleAtom(atom4);
                           break;
                        case 152101003:
                           slea.skip(5);
                           x3 = slea.readShort();
                           y3 = slea.readShort();
                           intervar = 0;
                           mobid2 = 0;
                           var105 = chr.getSummons().iterator();

                           while(var105.hasNext()) {
                              MapleSummon summon5 = (MapleSummon)var105.next();
                              if (summon5.getSkill() == 152101000) {
                                 intervar = summon5.getObjectId();
                              } else if (summon5.getSkill() == 152101008) {
                                 mobid2 = summon5.getObjectId();
                              }
                           }

                           if (chr.getBuffedValue(152101008)) {
                              chr.getMap().broadcastMessage(CField.MarkinaMoveAttack(chr, mobid2));
                              chr.getMap().broadcastMessage(CField.CrystalControl(chr, mobid2, new Point(x3, y3), 152101008));
                           }

                           chr.getMap().broadcastMessage(CField.CrystalControl(chr, intervar, new Point(x3, y3), skillid));
                           break;
                        case 152101004:
                           slea.skip(5);
                           x3 = slea.readShort();
                           y3 = slea.readShort();
                           intervar = 0;
                           var112 = chr.getSummons().iterator();

                           while(var112.hasNext()) {
                              summon8 = (MapleSummon)var112.next();
                              if (summon8.getSkill() == 152101000) {
                                 intervar = summon8.getObjectId();
                              }
                           }

                           chr.getMap().broadcastMessage(CField.CrystalTeleport(chr, intervar, new Point(x3, y3), skillid));
                           break;
                        case 152110004:
                           slea.skip(9);
                           atom4 = new MapleAtom(false, chr.getId(), 37, true, 152110004, chr.getTruePosition().x, chr.getTruePosition().y);
                           atom4.setDwFirstTargetId(0);
                           fa = new ForceAtom(1, 46, 60, 7, 300);
                           chr.addSkillCustomInfo(skillid, 1L);
                           fa.setnAttackCount((int)chr.getSkillCustomValue0(skillid));
                           atom4.addForceAtom(fa);
                           atom4.setDwUnknownPoint(slea.readInt());
                           c.getSession().writeAndFlush(CField.skillCooldown(152110004, 200));
                           chr.addCooldown(152110004, System.currentTimeMillis(), 200L);
                           effect.applyTo(chr);
                           c.getSession().writeAndFlush(CWvsContext.enableActions(chr, true, false));
                           chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, skillid, 1, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), true, pos, (String)null, (Item)null));
                           chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 0, skillid, 1, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), false, pos, (String)null, (Item)null), false);
                           chr.getMap().spawnMapleAtom(atom4);
                           break;
                        case 152111003:
                           chr.cancelEffect(chr.getBuffedEffect(SecondaryStat.CrystalBattery));
                           chr.cancelEffect(chr.getBuffedEffect(152101000));
                           chr.cancelEffect(chr.getBuffedEffect(152101008));
                           effect.applyTo(chr);
                           break;
                        case 152121005:
                           chr.cancelEffect(chr.getBuffedEffect(152001003));
                           chr.cancelEffect(chr.getBuffedEffect(152101008));
                           effect.applyTo(chr);

                           for(mobsize = 0; mobsize < 5; ++mobsize) {
                              summon4 = new MapleSummon(chr, 152121006, new Point(chr.getPosition().x, chr.getPosition().y), SummonMovementType.FOLLOW, (byte)0, effect.getDuration());
                              chr.getMap().spawnSummon(summon4, effect.getDuration());
                              chr.addSummon(summon4);
                           }

                           return;
                        case 152121041:
                           pos4 = slea.readPos();
                           effect.applyTo(chr, pos4, (int)6000);
                           break;
                        case 155001103:
                           moblist1 = new ArrayList();
                           mses = new ArrayList();
                           toRemove = new ArrayList();
                           allmesos = new ArrayList();
                           i10 = slea.readByte();

                           for(i4 = 0; i4 < i10; ++i4) {
                              bullet = slea.readInt();
                              moblist1.add(bullet);
                              mses.add(bullet);
                              toRemove.add(bullet);
                              allmesos.add(bullet);
                           }

                           if (chr.getSpellCount(4) <= 0) {
                              moblist1.clear();
                           }

                           if (chr.getSpellCount(3) <= 0) {
                              mses.clear();
                           }

                           if (chr.getSpellCount(2) <= 0) {
                              toRemove.clear();
                           }

                           if (chr.getSpellCount(1) <= 0) {
                              allmesos.clear();
                           }

                           chr.getMap().broadcastMessage(SkillPacket.SpawnSpell(chr.getId(), moblist1, mses, toRemove, allmesos, 0));
                           chr.useSpell();
                           effect.applyTo(chr);
                           break;
                        case 155101006:
                           if (!chr.getBuffedValue(155000007)) {
                              SkillFactory.getSkill(155000007).getEffect(1).applyTo(chr);
                           } else {
                              chr.cancelEffect(SkillFactory.getSkill(155000007).getEffect(1));
                           }

                           c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                           break;
                        case 155111207:
                           moblist1 = new ArrayList();
                           new ArrayList();
                           slea.skip(3);
                           atom5 = new MapleAtom(false, chr.getId(), 48, true, 155111207, chr.getTruePosition().x, chr.getTruePosition().y);
                           var112 = chr.getMap().getAllFieldThreadsafe().iterator();

                           MapleMagicWreck wreck;
                           while(var112.hasNext()) {
                              wreck = (MapleMagicWreck)var112.next();
                              if (wreck != null) {
                                 ForceAtom at2 = new ForceAtom(0, Randomizer.rand(40, 49), 60, Randomizer.rand(6, 9), 0, wreck.getTruePosition());
                                 at2.setnMaxHitCount(8);
                                 atom5.addForceAtom(at2);
                                 moblist1.add(wreck);
                              }
                           }

                           atom5.setDwUserOwner(chr.getId());
                           chr.getMap().spawnMapleAtom(atom5);
                           var112 = moblist1.iterator();

                           while(var112.hasNext()) {
                              wreck = (MapleMagicWreck)var112.next();
                              chr.getMap().RemoveMagicWreck(wreck);
                           }

                           effect.applyTo(chr);
                           break;
                        case 160001075:
                        case 160011075:
                           use = false;
                           if (c.getPlayer().getKeyValue(7786, "sw") != 1L) {
                              use = true;
                              c.getPlayer().setKeyValue(7786, "sw", "1");
                           } else {
                              c.getPlayer().setKeyValue(7786, "sw", "0");
                           }

                           chr.getMap().broadcastMessage(CField.updateShapeShift(chr.getId(), use));
                           c.getSession().writeAndFlush(CWvsContext.enableActions(chr, true, false));
                           chr.equipChanged();
                           break;
                        case 162101001:
                        case 162121000:
                           if (chr.getSkillLevel(162110007) > 0) {
                              SkillFactory.getSkill(162110007).getEffect(c.getPlayer().getSkillLevel(162110007)).applyTo(c.getPlayer());
                           }
                           break;
                        case 162101003:
                        case 162121012:
                           pos = slea.readPos();
                           use = slea.readByte() == 1;
                           mses = new ArrayList();
                           if (chr.getBuffedValue(162121003)) {
                              chr.cancelEffect(chr.getBuffedEffect(162121003));
                           }

                           if (skillid == 162101003) {
                              for(intervar = 0; intervar < 8; ++intervar) {
                                 mses.add(new RangeAttack(162101004, c.getPlayer().getPosition(), 0, 0, 1));
                              }
                           } else {
                              for(intervar = 0; intervar < 3; ++intervar) {
                                 mses.add(new RangeAttack(162121014, c.getPlayer().getPosition(), 0, 0, 8));
                                 mses.add(new RangeAttack(162121013, c.getPlayer().getPosition(), 0, 0, 8));
                                 mses.add(new RangeAttack(162121013, c.getPlayer().getPosition(), 0, 0, 8));
                              }
                           }

                           effect.applyTo(chr, pos);
                           c.send(CField.rangeAttack(skillid, mses));
                           break;
                        case 162101006:
                        case 162121015:
                           pos = slea.readPos();
                           use = slea.readByte() == 1;
                           if (chr.getBuffedValue(162121006)) {
                              chr.cancelEffect(chr.getBuffedEffect(162121006));
                           }

                           effect.applyTo(chr, pos);
                           break;
                        case 162101010:
                        case 162111000:
                        case 162111003:
                        case 162121018:
                           if (skillid == 162121018 && chr.getBuffedValue(162121009)) {
                              chr.cancelEffect(chr.getBuffedEffect(162121009));
                           }

                           effect.applyTo(chr, ret.plusPosition2);
                           break;
                        case 162101012:
                           if (chr.getSkillCustomValue0(162101012) > 0L) {
                              chr.addSkillCustomInfo(162101012, -1L);
                              if (chr.getSkillCustomValue(162101112) == null) {
                                 chr.setSkillCustomInfo(162101112, 0L, (long)(effect.getZ() * 1000));
                              }
                           }

                           statups = new HashMap();
                           statups.put(SecondaryStat.산의씨앗, new Pair((int)chr.getSkillCustomValue0(162101012), 0));
                           chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, effect, chr));
                           effect.applyTo(chr);
                           break;
                        case 162111002:
                           slea.skip(4);
                           pos = slea.readIntPos();
                           moblist1 = new ArrayList();
                           if (chr.getMap().getSecondAtom(chr.getId(), skillid) != null) {
                              c.getSession().writeAndFlush(CWvsContext.onSkillUseResult(skillid));
                              moblist1.add(new SecondAtom(32, c.getPlayer().getId(), 0, 0, skillid, 90000, 0, 1, ret.plusPosition2, Arrays.asList(chr.getMap().getSecondAtom(chr.getId(), skillid).getObjectId())));
                              effect.applyTo(chr);
                              chr.getMap().spawnSecondAtom(chr, moblist1, 0);
                           } else {
                              moblist1.add(new SecondAtom(31, c.getPlayer().getId(), 0, 0, skillid, 10000, 0, 1, ret.plusPosition2, Arrays.asList(0)));
                              chr.getMap().spawnSecondAtom(chr, moblist1, 0);
                              c.getSession().writeAndFlush(CField.skillCooldown(skillid, linkEffect.getCooldown(chr)));
                              chr.addCooldown(skillid, System.currentTimeMillis(), (long)linkEffect.getCooldown(chr));
                           }
                           break;
                        case 162111005:
                        case 400021122:
                           if (skillid == 400021122) {
                              c.getSession().writeAndFlush(CField.skillCooldown(skillid, linkEffect.getCooldown(chr)));
                              chr.addCooldown(skillid, System.currentTimeMillis(), (long)linkEffect.getCooldown(chr));
                           }

                           moblist1 = new ArrayList();
                           mses = new ArrayList();
                           size = slea.readByte();

                           for(mobid2 = 0; mobid2 < size; ++mobid2) {
                              mses.add(slea.readInt());
                           }

                           mobid2 = 0;

                           for(max = 0; max < 7 + (skillid == 400021122 ? 0 : size); ++max) {
                              mobid2 = max < size ? (Integer)mses.get(max) : mobid2;
                              moblist1.add(new SecondAtom(22, c.getPlayer().getId(), mobid2, 1140, skillid, 4000, 0, 1, new Point(ret.plusPosition2.x + Randomizer.rand(-200, 200), ret.plusPosition2.y + Randomizer.rand(-350, -50)), Arrays.asList()));
                              if (skillid == 162111005 && max >= effect.getZ()) {
                                 break;
                              }
                           }

                           chr.getMap().spawnSecondAtom(chr, moblist1, 0);
                           break;
                        case 162121003:
                           mobsize = chr.getBuffedValue(162101003) ? 162101003 : 162121012;
                           if (chr.getBuffedValue(mobsize)) {
                              chr.cancelEffect(chr.getBuffedEffect(mobsize));
                           }

                           effect.applyTo(chr);
                           break;
                        case 162121006:
                           mobsize = chr.getBuffedValue(162101006) ? 162101006 : 162121015;
                           if (chr.getBuffedValue(mobsize)) {
                              chr.cancelEffect(chr.getBuffedEffect(mobsize));
                           }

                           effect.applyTo(chr);
                           break;
                        case 162121009:
                           mobsize = chr.getBuffedValue(162101009) ? 162101009 : 162121018;
                           if (chr.getBuffedValue(mobsize)) {
                              chr.cancelEffect(chr.getBuffedEffect(mobsize));
                           }

                           chr.getMap().removeMist(mobsize);
                           effect.applyTo(chr);
                           break;
                        case 162121010:
                           moblist1 = new ArrayList();
                           mses = new ArrayList();
                           size = slea.readByte();

                           for(mobid2 = 0; mobid2 < size; ++mobid2) {
                              mses.add(slea.readInt());
                           }

                           slea.skip(3);
                           mobid2 = slea.readInt();
                           max = slea.readInt();
                           i = 0;

                           for(bullet = 0; i < 5; ++i) {
                              bullet = i < size ? (Integer)mses.get(i) : bullet;
                              moblist1.add(new SecondAtom(23, c.getPlayer().getId(), bullet, 1200 + i * 120, skillid, 4000, 20 + i * 60, 1, new Point(ret.plusPosition2.x + (ret.rlType == 1 ? 55 : -55), ret.plusPosition2.y - 90), Arrays.asList(ret.plusPosition2.x, ret.plusPosition2.y)));
                           }

                           chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, 162121010, 1, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), true, ret.plusPosition2, (String)null, (Item)null));
                           chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 0, 162121010, 1, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), false, ret.plusPosition2, (String)null, (Item)null), false);
                           chr.getMap().spawnSecondAtom(chr, moblist1, 0);
                           break;
                        case 162121017:
                           var117 = chr.getMap().getAllMistsThreadsafe().iterator();

                           while(var117.hasNext()) {
                              mist3 = (MapleMist)var117.next();
                              if (mist3 != null && mist3.getSourceSkill() != null && mist3.getSourceSkill().getId() == 162121018) {
                                 chr.getMap().removeMistByOwner(chr, 162121018);
                                 break;
                              }
                           }

                           SkillFactory.getSkill(162121018).getEffect(skillLevel).applyTo(chr, ret.plusPosition2);
                        case 162101011:
                        case 162121019:
                           rltype = slea.readByte();

                           for(skillid2 = 0; skillid2 < rltype; ++skillid2) {
                              slea.readInt();
                           }

                           slea.skip(3);
                           mobsize = slea.readInt();
                           skillid2 = slea.readInt();
                           mist = chr.getMap().getMist(chr.getId(), skillid == 162101011 ? 162101010 : 162121018);
                           pos2 = mist != null ? mist.getPosition() : pos;
                           monsters2 = new ArrayList();

                           for(i = 0; i < (skillid == 162101011 ? 4 : 5); ++i) {
                              monsters2.add(new SecondAtom(21, c.getPlayer().getId(), 0, 0, skillid, 4000, 0, 1, new Point(pos2.x + Randomizer.rand(-250, 250), pos2.y + Randomizer.rand(-460, 0)), Arrays.asList(mobsize, skillid2)));
                           }

                           c.getPlayer().spawnSecondAtom(monsters2);
                           break;
                        case 162121042:
                           if (chr.getSkillCustomValue0(162121042) > 0L) {
                              chr.addSkillCustomInfo(162121042, -1L);
                              if (chr.getSkillCustomValue(162121142) == null) {
                                 chr.setSkillCustomInfo(162121142, 0L, (long)(effect.getW() * 1000));
                              }
                           }

                           statups = new HashMap();
                           statups.put(SecondaryStat.자유로운용맥, new Pair((int)chr.getSkillCustomValue0(162121042), 0));
                           chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, effect, chr));
                           mses = new ArrayList();
                           mses.add(new SecondAtom(20, c.getPlayer().getId(), 0, 0, 162101000, 0, 0, 0, pos, Arrays.asList(8)));
                           c.getPlayer().spawnSecondAtom(mses);
                           break;
                        case 162121043:
                           newmist = new MapleMist(effect.calculateBoundingBox(pos, ret.rlType == 1), c.getPlayer(), effect, effect.getDuration(), ret.rlType);
                           newmist.setPosition(pos);
                           newmist.setDelay(2);
                           chr.getMap().spawnMist(newmist, false);
                           SkillFactory.getSkill(162121044).getEffect(skillLevel).applyTo(c.getPlayer());
                           break;
                        case 164121011:
                        case 164121012:
                           c.getSession().writeAndFlush(CField.skillCooldown(164121006, 5000));
                           chr.addCooldown(164121006, System.currentTimeMillis(), 5000L);
                           if (skillid == 164121011) {
                              effect.applyTo(chr);
                           } else {
                              var117 = chr.getMap().getAllSummonsThreadsafe().iterator();

                              while(var117.hasNext()) {
                                 summon4 = (MapleSummon)var117.next();
                                 if (summon4.getSkill() == 164121011) {
                                    summon4.removeSummon(chr.getMap(), false);
                                    break;
                                 }
                              }

                              c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                           }
                           break;
                        case 164121015:
                           effect2 = SkillFactory.getSkill(164121008).getEffect(chr.getSkillLevel(164121008));
                           skillid2 = (int)chr.getSkillCustomValue0(164121009);
                           var84 = chr.getMap().getAllSummonsThreadsafe().iterator();

                           while(var84.hasNext()) {
                              summon7 = (MapleSummon)var84.next();
                              if (summon7 != null && summon7.getOwner().getId() == chr.getId() && summon7.getSkill() == 164121008) {
                                 summon7.removeSummon(chr.getMap(), false);
                                 chr.getSummons().remove(summon7);
                              }
                           }

                           if (chr.getParty() != null) {
                              var84 = chr.getParty().getMembers().iterator();

                              while(var84.hasNext()) {
                                 chr2 = (MaplePartyCharacter)var84.next();
                                 chr6 = chr.getClient().getChannelServer().getPlayerStorage().getCharacterById(chr2.getId());
                                 if (chr6 != null && chr6.getMapId() == chr.getMapId()) {
                                    chr6.addHP(chr6.getStat().getCurrentMaxHp() / 100L * (long)(skillid2 * effect2.getY()), true, false);
                                    chr6.addMP(chr6.getStat().getCurrentMaxMp(chr) / 100L * (long)(skillid2 * effect2.getY()));
                                 }
                              }
                           } else {
                              chr.addHP(chr.getStat().getCurrentMaxHp() / 100L * (long)(skillid2 * effect2.getY()), true, false);
                              chr.addMP(chr.getStat().getCurrentMaxMp(chr) / 100L * (long)(skillid2 * effect2.getY()));
                           }

                           chr.removeSkillCustomInfo(164121008);
                           chr.removeSkillCustomInfo(164121009);
                           break;
                        case 400001011:
                           moblist1 = new ArrayList();
                           chr.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(400001011, moblist1, true, 0));
                           chr.cancelEffect(chr.getBuffedEffect(400001010));
                           break;
                        case 400001021:
                           effect2 = SkillFactory.getSkill(c.getPlayer().unstableMemorize).getEffect(c.getPlayer().getSkillLevel(c.getPlayer().unstableMemorize));
                           c.getSession().writeAndFlush(CField.unstableMemorize(chr.unstableMemorize));
                           effect2.applyToBuff(chr);
                           c.getSession().writeAndFlush(CWvsContext.enableActions(chr, true, false));
                           break;
                        case 400001025:
                        case 400001026:
                        case 400001027:
                        case 400001028:
                        case 400001029:
                        case 400001030:
                           effect2 = SkillFactory.getSkill(400001024).getEffect(chr.getSkillLevel(400001024));
                           chr.getClient().send(CField.skillCooldown(effect2.getSourceId(), effect2.getCooldown(chr)));
                           chr.addCooldown(effect2.getSourceId(), System.currentTimeMillis(), (long)effect2.getCooldown(chr));
                           effect.applyTo(chr);
                           break;
                        case 400001043:
                           chr.removeSkillCustomInfo(skillid);
                           effect.applyTo(chr);
                           break;
                        case 400001050:
                           c.getPlayer().removeSkillCustomInfo(400001050);
                           effect.applyTo(chr);
                           break;
                        case 400001064:
                           pos = slea.readPos();
                           summon2 = new MapleSummon(chr, 400001064, chr.getTruePosition(), SummonMovementType.STATIONARY, (byte)0, effect.getDuration());
                           chr.getMap().spawnSummon(summon2, effect.getDuration());
                           chr.addSummon(summon2);
                           effect.applyTo(chr);
                           break;
                        case 400011010:
                           if (chr.getBuffedValue(skillid)) {
                              chr.cancelEffect(effect);
                              c.getSession().writeAndFlush(CField.skillCooldown(skillid, linkEffect.getZ() * 1000));
                              chr.addCooldown(skillid, System.currentTimeMillis(), (long)(linkEffect.getZ() * 1000));
                           } else {
                              effect.applyTo(chr);
                           }
                           break;
                        case 400011015:
                           if (chr.getBuffedValue(skillid)) {
                              var117 = chr.getMap().getAllMonster().iterator();

                              while(var117.hasNext()) {
                                 mob4 = (MapleMonster)var117.next();
                                 if (chr.getTruePosition().x + effect.getLt().x < mob4.getTruePosition().x && chr.getTruePosition().x - effect.getLt().x > mob4.getTruePosition().x && chr.getTruePosition().y + effect.getLt().y < mob4.getTruePosition().y && chr.getTruePosition().y - effect.getLt().y > mob4.getTruePosition().y && mob4.getBuff(MonsterStatus.MS_Speed) == null) {
                                    toRemove = new ArrayList();
                                    toRemove.add(new Pair(MonsterStatus.MS_Speed, new MonsterStatusEffect(effect.getSourceId(), 6000, -99L)));
                                    mob4.applyStatus(chr.getClient(), toRemove, effect);
                                 }
                              }

                              return;
                           }

                           effect.applyTo(chr);
                           break;
                        case 400011031:
                           if (chr.getBuffedValue(400011016)) {
                              chr.changeCooldown(400011031, -effect.getCooldown(chr) / 2);
                           }

                           effect.applyTo(chr);
                           break;
                        case 400011038:
                           effect.applyToBuff(chr);
                           break;
                        case 400011055:
                           chr.removeCooldown(11121052);
                           effect.applyTo(chr);
                           mobsize = 0;
                           int[] skills = new int[]{11001022, 11120019, 11110031, 11100034, 11121054};

                           for(intervar = 0; intervar < skills.length; ++intervar) {
                              if (chr.getSkillLevel(skills[intervar]) > 0) {
                                 if (skills[intervar] == 11001022) {
                                    mobsize += 2;
                                 } else if (skills[intervar] == 11121054) {
                                    if (chr.getBuffedValue(11121054)) {
                                       mobsize += 5;
                                    }
                                 } else {
                                    ++mobsize;
                                 }
                              }

                              if (mobsize > 10) {
                                 mobsize = 10;
                              }
                           }

                           chr.setCosmicCount(mobsize);
                           break;
                        case 400011087:
                           SkillFactory.getSkill(400011083).getEffect(1).applyTo(chr, (int)chr.getBuffLimit(400011083));
                           break;
                        case 400011103:
                           chr.setSkillCustomInfo(400011091, chr.getSkillCustomValue0(400011091) + 1L, 0L);
                           chr.Cylinder(skillid);
                           c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                           break;
                        case 400011108:
                           for(mobsize = 0; mobsize < 18; ++mobsize) {
                              ms3 = new MapleMagicSword(chr, skillid, chr.활성화된소드, 31000, true);
                              if (mobsize == 0) {
                                 chr.setSkillCustomInfo(400011108, 1L, 0L);
                              }

                              if (mobsize == 1) {
                                 chr.setSkillCustomInfo(400011108, 0L, 0L);
                              }

                              chr.getMap().spawnMagicSword(ms3, chr, true);
                           }

                           effect.applyTo(chr);
                           break;
                        case 400011112:
                           chr.setSkillCustomInfo(400011112, 0L, 0L);
                           effect.applyTo(chr);
                           break;
                        case 400011116:
                           chr.setSkillCustomInfo(400011116, (long)effect.getY(), 0L);
                           effect.applyTo(chr);
                           break;
                        case 400011131:
                           rltype = slea.readByte();
                           atom = new MapleAtom(false, chr.getId(), 67, true, 400011131, chr.getTruePosition().x, chr.getTruePosition().y);
                           toRemove = new ArrayList();

                           for(mobid2 = 0; mobid2 < rltype; ++mobid2) {
                              toRemove.add(slea.readInt());
                           }

                           short delay = slea.readShort();

                           for(max = 0; max < rltype; ++max) {
                              atom.addForceAtom(new ForceAtom(0, 1, 12, 62, delay, chr.getTruePosition()));
                           }

                           slea.skip(1);
                           atom.setDwUnknownByte(slea.readByte());
                           atom.setDwUnknownInteger(unk);
                           atom.setDwTargets(toRemove);
                           chr.getMap().spawnMapleAtom(atom);
                           Vmatrixstackbuff(c, true, (LittleEndianAccessor)null);
                           break;
                        case 400011135:
                           mobsize = slea.readInt();

                           for(skillid2 = 0; skillid2 < mobsize; ++skillid2) {
                              spawn_pos = new Point(slea.readInt(), slea.readInt());
                              effect.applyTo(chr, spawn_pos);
                           }

                           return;
                        case 400011142:
                           chr.setCosmicCount(0);
                           effect2 = SkillFactory.getSkill(skillid).getEffect(chr.getSkillLevel(skillid));
                           SkillFactory.getSkill(skillid).getEffect(skillLevel).applyTo(c.getPlayer(), effect2.getDuration());
                           break;
                        case 400020046:
                           pos4 = slea.readPos();
                           mist3 = new MapleMist(effect.calculateBoundingBox(pos4, chr.isFacingLeft()), c.getPlayer(), effect, effect.getDuration(), (byte)(chr.isFacingLeft() ? 1 : 0));
                           mist3.setPosition(pos4);
                           c.getPlayer().getMap().spawnMist(mist3, false);
                           effect.applyTo(chr);
                           break;
                        case 400021001:
                           mobsize = c.getPlayer().getPosition().x - 500;
                           skillid2 = c.getPlayer().getPosition().x + 500;
                           intervar = c.getPlayer().getPosition().y - 550;
                           mobid2 = c.getPlayer().getPosition().y - 150;
                           List<MapleMapObject> mobs_objects = c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getPosition(), 320000.0D, Arrays.asList(MapleMapObjectType.MONSTER));
                           i = 0;
                           bullet = 0;

                           for(int i = 0; i < mobs_objects.size(); ++i) {
                              MapleMonster mob = (MapleMonster)mobs_objects.get(i);
                              bullet = mob.getObjectId();
                              lp = mob.getIgnitions().iterator();
                              boolean moab = false;

                              while(lp.hasNext()) {
                                 Ignition zz = (Ignition)lp.next();
                                 if (zz.getOwnerId() == c.getPlayer().getId() && !moab) {
                                    moab = true;
                                    ++i;
                                 }
                              }
                           }

                           MapleAtom atom = new MapleAtom(false, chr.getId(), 28, true, 400021001, chr.getTruePosition().x, chr.getTruePosition().y);
                           applys2 = new ArrayList();

                           for(randmob_remove = 0; randmob_remove < 15 + i; ++randmob_remove) {
                              applys2.add(0);
                              ForceAtom atoms = new ForceAtom(bullet, Randomizer.rand(41, 44), Randomizer.rand(3, 4), Randomizer.rand(0, 360), 720, new Point(Randomizer.rand(mobsize, skillid2), Randomizer.rand(intervar, mobid2)));
                              List<Integer> mobid = new ArrayList();
                              mobid.add(bullet);
                              atom.setDwTargets(mobid);
                              atom.addForceAtom(atoms);
                           }

                           effect.applyTo(chr);
                           c.getSession().writeAndFlush(CWvsContext.enableActions(chr, true, false));
                           chr.getMap().spawnMapleAtom(atom);
                           break;
                        case 400021030:
                           effect2 = SkillFactory.getSkill(400021031).getEffect(skillLevel);
                           slea.skip(5);
                           skillid2 = slea.readInt();
                           toRemove = new ArrayList();

                           for(mobid2 = 0; mobid2 < skillid2; ++mobid2) {
                              Point poss = new Point(slea.readInt(), slea.readInt());
                              toRemove.add(poss);
                           }

                           mobid2 = 737;
                           max = -2310;
                           var61 = toRemove.iterator();

                           while(var61.hasNext()) {
                              Point poss2 = (Point)var61.next();
                              mobid2 += 350;
                              MapleMist mist4 = new MapleMist(effect2.calculateBoundingBox(poss2, chr.isFacingLeft()), chr, effect2, max, (byte)(chr.isFacingLeft() ? 1 : 0));
                              mist4.setDelay(26);
                              mist4.setPosition(poss2);
                              mist4.setEndTime(mobid2);
                              max += 350;
                              chr.getMap().spawnMist(mist4, false);
                           }

                           effect.applyTo(chr);
                           c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                           break;
                        case 400021047:
                           pos4 = slea.readPos();
                           point = slea.readPos();
                           int facing3 = slea.readShort();
                           slea.skip(1);
                           mobid2 = slea.readInt();
                           Vmatrixstackbuff(c, true, (LittleEndianAccessor)null);
                           effect.applyTo(chr);
                           if (mobid2 == 1) {
                              Vmatrixstackbuff(c, true, (LittleEndianAccessor)null);
                              summon8 = new MapleSummon(chr, 400021047, facing3 == 0 ? new Point(chr.getPosition().x + 700, chr.getPosition().y) : new Point(chr.getPosition().x - 600, chr.getPosition().y), SummonMovementType.STATIONARY, (byte)0, effect.getDuration());
                              chr.getMap().spawnSummon(summon8, effect.getDuration());
                              chr.addSummon(summon8);
                           }
                           break;
                        case 400021068:
                           pos4 = slea.readPos();
                           summon4 = chr.getSummon(152101000);
                           if (summon4 != null) {
                              if (!chr.getSummons().isEmpty()) {
                                 toRemove = new ArrayList();
                                 summon7 = null;
                                 summon8 = null;
                                 var61 = chr.getSummons().iterator();

                                 MapleSummon summon9;
                                 while(var61.hasNext()) {
                                    summon9 = (MapleSummon)var61.next();
                                    if (summon9.getSkill() == 400021068) {
                                       if (summon7 == null) {
                                          summon7 = summon9;
                                       } else if (summon8 == null) {
                                          summon8 = summon9;
                                       }

                                       if (summon7 != null && summon8 != null) {
                                          break;
                                       }
                                    }
                                 }

                                 if (summon7 != null && summon8 != null) {
                                    if (summon7.getStartTime() > summon8.getStartTime()) {
                                       toRemove.add(summon8);
                                    } else {
                                       toRemove.add(summon7);
                                    }

                                    var61 = toRemove.iterator();

                                    while(var61.hasNext()) {
                                       summon9 = (MapleSummon)var61.next();
                                       summon9.removeSummon(chr.getMap(), false);
                                    }
                                 }
                              }

                              Vmatrixstackbuff(c, true, (LittleEndianAccessor)null);
                              MapleSummon summon10 = new MapleSummon(chr, 400021068, pos4, SummonMovementType.ShadowServantExtend, (byte)7, effect.getDuration());
                              chr.addSummon(summon10);
                              chr.getMap().spawnSummon(summon10, effect.getDuration());
                              effect.applyTo(chr);
                           }
                           break;
                        case 400021087:
                           chr.removeSkillCustomInfo(400021087);
                           effect.applyTo(chr);
                           break;
                        case 400021088:
                           mobsize = ret.acrossPosition.height;
                           s3 = (SpecialPortal)chr.getMap().getMapObject(mobsize, MapleMapObjectType.SPECIAL_PORTAL);
                           if (s3 != null) {
                              chr.getMap().removeSpecialPortal(chr, s3);
                           }

                           chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, skillid, 10, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), true, chr.getTruePosition(), (String)null, (Item)null, ret));
                           break;
                        case 400021099:
                           if (!chr.getBuffedValue(400021099)) {
                              chr.setSkillCustomInfo(400021099, (long)effect.getProp(), 0L);
                           }

                           if (chr.getSkillCustomValue0(400021099) > 0L) {
                              chr.addSkillCustomInfo(400021099, -1L);
                           }

                           moblist1 = new ArrayList();
                           s3 = new SpecialPortal(chr.getId(), 1, 400021100, chr.getMapId(), ret.plusPosition2.x, ret.plusPosition2.y, (int)(chr.getBuffedValue(400021099) ? chr.getBuffLimit(400021099) : (long)effect.getDuration()));
                           s3.setObjectId((int)(900000000L + chr.getSkillCustomValue0(400021099)));
                           moblist1.add(s3);
                           chr.getClient().send(CField.createSpecialPortal(chr.getId(), moblist1));
                           effect.applyTo(chr, slea.readPos(), ret.rlType);
                           break;
                        case 400021123:
                           moblist1 = new ArrayList();
                           System.out.println(ret.rlType < 0);
                           moblist1.add(new RangeAttack(400021128, ret.plusPosition2, ret.rlType > 0 ? 1 : 0, 5850, 8));
                           moblist1.add(new RangeAttack(400021127, ret.plusPosition2, ret.rlType > 0 ? 1 : 0, 3150, 4));
                           moblist1.add(new RangeAttack(400021126, ret.plusPosition2, ret.rlType > 0 ? 1 : 0, 2250, 4));
                           moblist1.add(new RangeAttack(400021125, ret.plusPosition2, ret.rlType > 0 ? 1 : 0, 1560, 4));
                           moblist1.add(new RangeAttack(400021124, ret.plusPosition2, ret.rlType > 0 ? 1 : 0, 630, 4));
                           c.send(CField.rangeAttack(400021123, moblist1));
                           effect.applyTo(chr);
                           break;
                        case 400031000:
                           atom4 = new MapleAtom(false, chr.getId(), 27, true, 400031000, chr.getTruePosition().x, chr.getTruePosition().y);
                           mses = new ArrayList();
                           mses.add(0);
                           ForceAtom forceAtom = new ForceAtom(1, 40, 3, 90, 840);
                           atom4.addForceAtom(forceAtom);
                           effect.applyTo(chr);
                           c.getSession().writeAndFlush(CWvsContext.enableActions(chr, true, false));
                           atom4.setDwTargets(mses);
                           chr.getMap().spawnMapleAtom(atom4);
                           break;
                        case 400031022:
                           atom4 = new MapleAtom(false, chr.getId(), 34, true, 400031022, chr.getTruePosition().x, chr.getTruePosition().y);
                           mses = new ArrayList();

                           for(intervar = 0; intervar < effect.getX(); ++intervar) {
                              mses.add(0);
                              atom4.addForceAtom(new ForceAtom(Randomizer.nextBoolean() ? 1 : 3, Randomizer.rand(30, 60), 10, Randomizer.nextBoolean() ? Randomizer.rand(0, 5) : Randomizer.rand(180, 185), 720, chr.getTruePosition()));
                           }

                           effect.applyTo(chr);
                           c.getSession().writeAndFlush(CWvsContext.enableActions(chr, true, false));
                           atom4.setDwTargets(mses);
                           chr.getMap().spawnMapleAtom(atom4);
                           break;
                        case 400031051:
                           effect.applyTo(chr);
                           Point[] positions = new Point[]{new Point(chr.getTruePosition().x - effect.getX(), chr.getTruePosition().y), new Point(chr.getTruePosition().x + effect.getX(), chr.getTruePosition().y), new Point(chr.getTruePosition().x, chr.getTruePosition().y + effect.getY())};

                           for(skillid2 = 0; skillid2 < effect.getU(); ++skillid2) {
                              spawn_pos = positions[skillid2];
                              effect.applyTo(chr, false, spawn_pos);
                           }

                           return;
                        case 400031066:
                           objs = SkillFactory.getSkill(400031066).getSecondAtoms();
                           chr.removeSkillCustomInfo(400031066);
                           chr.removeSkillCustomInfo(400031067);
                           chr.removeSkillCustomInfo(400031068);
                           chr.createSecondAtom(objs, new Point(chr.getPosition().x, chr.getMap().getFootholds().findBelow(chr.getPosition()).getY1()));
                           chr.cancelEffect(effect);
                           break;
                        case 400041000:
                           mobsize = 0;
                           facing2 = false;
                           intervar = 0;
                           long damage = 0L;
                           long realduration2 = 0L;
                           long dotdamage = 0L;
                           long totaldamage = 0L;
                           Iterator var24 = chr.getMap().getAllMonster().iterator();

                           label1895:
                           do {
                              MapleMonster monster4;
                              do {
                                 do {
                                    if (!var24.hasNext()) {
                                       break label1895;
                                    }

                                    monster4 = (MapleMonster)var24.next();
                                 } while(monster4 == null);
                              } while(monster4.getBuff(MonsterStatus.MS_Burned) == null);

                              skillid2 = monster4.getBuff(MonsterStatus.MS_Burned).getSkill();
                              realduration2 = monster4.getBuff(MonsterStatus.MS_Burned).getStartTime() + (long)monster4.getBuff(MonsterStatus.MS_Burned).getDuration() - System.currentTimeMillis();
                              Iterator var26 = monster4.getIgnitions().iterator();

                              while(var26.hasNext()) {
                                 Ignition ig = (Ignition)var26.next();
                                 if (ig.getSkill() == skillid2) {
                                    intervar = ig.getInterval();
                                    dotdamage = ig.getDamage();
                                    break;
                                 }
                              }

                              totaldamage = dotdamage * (realduration2 / 10L) / (long)intervar;
                              chr.getMap().broadcastMessage(MobPacket.NujukDamage(monster4, chr, totaldamage, 400041000, 6));
                              if (monster4.getBuff(MonsterStatus.MS_Burned) != null) {
                                 monster4.cancelStatus(MonsterStatus.MS_Burned, monster4.getBuff(400040000));
                              }

                              ++mobsize;
                           } while(mobsize != 12);

                           effect.applyTo(chr);
                           break;
                        case 400041002:
                           effect2 = SkillFactory.getSkill(skillid).getEffect(chr.getSkillLevel(skillid));
                           if (chr.getSkillCustomValue0(skillid) > 0L) {
                              if (chr.getSkillCustomValue0(skillid) == 3L) {
                                 effect2 = SkillFactory.getSkill(400041003).getEffect(chr.getSkillLevel(skillid));
                              } else if (chr.getSkillCustomValue0(skillid) == 2L) {
                                 effect2 = SkillFactory.getSkill(400041004).getEffect(chr.getSkillLevel(skillid));
                              } else if (chr.getSkillCustomValue0(skillid) == 1L) {
                                 effect2 = SkillFactory.getSkill(400041005).getEffect(chr.getSkillLevel(skillid));
                              }

                              effect2.applyTo(chr);
                              chr.removeCooldown(skillid);
                           } else {
                              effect.applyTo(chr);
                           }
                           break;
                        case 400041021:
                           effect.applyTo(chr);
                           c.getSession().writeAndFlush(CWvsContext.onSkillUseResult(skillid));
                           break;
                        case 400041022:
                           rltype = slea.readByte();
                           chr.useBlackJack = false;
                           atom = new MapleAtom(false, chr.getId(), 33, true, 400041023, chr.getTruePosition().x, chr.getTruePosition().y);
                           chr.setSkillCustomInfo(400041080, 21L, 0L);
                           atom.setDwFirstTargetId(slea.readInt());
                           slea.skip(3);
                           atom.setSearchX(slea.readInt());
                           atom.setSearchX1(slea.readInt());

                           for(intervar = 0; intervar < 3; ++intervar) {
                              ForceAtom at3 = new ForceAtom(33, Randomizer.rand(30, 50), Randomizer.rand(5, 15), Randomizer.rand(55, 250), 760);
                              atom.addForceAtom(at3);
                           }

                           effect.applyTo(chr);
                           c.getSession().writeAndFlush(CWvsContext.enableActions(chr, true, false));
                           chr.getMap().spawnMapleAtom(atom);
                           if (plus == 2) {
                              chr.removeSkillCustomInfo(400041022);
                              chr.getMap().broadcastMessage(CField.SetForceAtomTarget(400041023, c.getPlayer().getId(), 3, atom.getDwFirstTargetId()));
                              chr.useBlackJack = true;
                           }
                           break;
                        case 400041024:
                           chr.useBlackJack = true;
                           c.getPlayer().getMap().broadcastMessage(CField.blackJack(c.getPlayer(), skillid, slea.readPos()));
                           break;
                        case 400041057:
                           chr.photonRay = 0;
                           effect.applyTo(chr);
                           break;
                        case 400041058:
                           slea.readInt();
                           slea.readInt();
                           slea.readInt();
                           slea.readByte();
                           chr.cancelEffect(chr.getBuffedEffect(SecondaryStat.PhotonRay));
                           bufflist = new Integer[]{-45, -70, -135, -165, -150, -35, -55, -50, -160, -48, -140, -167, -40, -170, -52, -10, -15, -170, -165, -175, -5, -20, -25, -160, -30, -155, -150, -32, -177, -38};
                           mses = new ArrayList<AdelProjectile>() {
                              {
                                 int i = 0;
                                 Iterator var5 = ret.attackObjects.iterator();

                                 while(var5.hasNext()) {
                                    Pair<Integer, Integer> object = (Pair)var5.next();
                                    MapleMapObject obj = chr.getMap().getMapObject((Integer)object.left, MapleMapObjectType.MONSTER);

                                    for(int a = 0; a < (Integer)object.right; ++a) {
                                       this.add(new AdelProjectile(9, chr.getId(), (Integer)object.left, 400041058, 10000, bufflist[i], 1, obj.getTruePosition(), Arrays.asList(bufflist)));
                                       ++i;
                                    }

                                    if (i >= bufflist.length) {
                                       break;
                                    }
                                 }

                              }
                           };
                           chr.getMap().spawnAdelProjectile(chr, mses, false);
                           break;
                        case 400041080:
                           mobsize = slea.readInt();
                           chr.removeSkillCustomInfo(400041022);
                           chr.useBlackJack = true;
                           c.getPlayer().getMap().broadcastMessage(CField.SetForceAtomTarget(400041023, c.getPlayer().getId(), 3, mobsize));
                           c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                           break;
                        case 400051025:
                           pos = new Point(slea.readInt(), slea.readInt());
                           c.getPlayer().getMap().broadcastMessage(CField.ICBM(false, skillid, effect.calculateBoundingBox(pos, c.getPlayer().isFacingLeft())));
                           effect.applyTo(chr, pos);
                           break;
                        case 400051068:
                           chr.removeSkillCustomInfo(400051069);
                           chr.setSkillCustomInfo(400051068, (long)effect.getX(), 0L);
                           chr.MechCarrier(2000, false);
                           effect.applyTo(chr);
                           break;
                        case 400051074:
                           chr.setSkillCustomInfo(400051074, 20L, 0L);
                           c.send(CField.fullMaker(20, 60000));
                           effect.applyTo(chr);
                           break;
                        default:
                           if (skillid == 64001012) {
                              for(mobsize = 64001007; mobsize < 64001012; ++mobsize) {
                                 while(chr.getBuffedValue(mobsize)) {
                                    chr.cancelEffect(chr.getBuffedEffect(mobsize));
                                 }
                              }

                              slea.skip(3);
                              rltype = slea.readByte();
                              pos = new Point(slea.readInt(), slea.readInt());
                              skillid2 = slea.readInt();
                              chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, skillid2, skillid, 1, 0, 0, rltype, true, pos, (String)null, (Item)null));
                              chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, skillid2, skillid, 1, 0, 0, rltype, false, pos, (String)null, (Item)null), false);
                              effect.applyTo(chr);
                           } else if (skillid == 400041041) {
                              var117 = chr.getMap().getAllMistsThreadsafe().iterator();

                              while(var117.hasNext()) {
                                 mist3 = (MapleMist)var117.next();
                                 if (mist3.getSourceSkill().getId() == 400041041) {
                                    chr.getMap().removeMist(400041041);
                                    break;
                                 }
                              }

                              effect.applyTo(c.getPlayer(), ret.plusPosition2, ret.rlType);
                           } else if (skillid == 131001025) {
                              pos = slea.readPos();
                              rltype = slea.readByte();
                              slea.skip(2);
                              skillid2 = slea.readInt();
                              mob = chr.getMap().getMonsterByOid(skillid2);
                              if (mob == null) {
                                 System.out.println("maelstrom error");
                              } else {
                                 chr.maelstrom = mob.getId();
                                 effect.applyTo(c.getPlayer(), mob.getTruePosition(), true, rltype);
                              }
                           } else {
                              if (skillid == 35111002) {
                                 mobsize = slea.readInt();
                                 chr.dropMessageGM(6, "type4 : " + mobsize);
                                 if (mobsize == 1) {
                                    pos = slea.readPos();
                                    effect.applyTo(chr, new Point(slea.readPos()));
                                    slea.readByte();
                                    effect.applyTo(chr, new Point(slea.readPos()));
                                 } else {
                                    tesla = slea.readByte();
                                    if (tesla == 2) {
                                       slea.skip(8);
                                    }

                                    pos = slea.readPos();
                                 }
                              }

                              if (skillid != 400021047 && skillid != 5210015) {
                                 if (slea.available() == 12L) {
                                    pos = slea.readPos();
                                 } else if (slea.available() == 11L) {
                                    slea.skip(4);
                                    pos = slea.readPos();
                                 } else if (slea.available() <= 9L && slea.available() >= 5L) {
                                    pos = slea.readPos();
                                 }
                              } else {
                                 pos = slea.readPos();
                              }

                              if (skill.getId() == 1121054) {
                                 chr.발할라검격 = 12;
                              }

                              if (effect.isMagicDoor()) {
                                 if (!FieldLimitType.MysticDoor.check(chr.getMap().getFieldLimit())) {
                                    effect.applyTo(c.getPlayer(), pos);
                                 }
                              } else if (skillid != 400011015 || !chr.getBuffedValue(400011015)) {
                                 slea.skip((int)(slea.available() - 3L));
                                 rltype = slea.readByte();
                                 effect.applyTo(c.getPlayer(), pos, rltype, true);
                              }

                              if (skill.getId() == 1121054 && chr.getBuffedEffect(SecondaryStat.ComboCounter) != null) {
                                 EnumMap<SecondaryStat, Pair<Integer, Integer>> stat = new EnumMap(SecondaryStat.class);
                                 stat.put(SecondaryStat.ComboCounter, new Pair(11, 0));
                                 chr.setBuffedValue(SecondaryStat.ComboCounter, 11);
                                 chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(stat, SkillFactory.getSkill(1101013).getEffect(chr.getSkillLevel(1101013)), chr));
                                 chr.getMap().broadcastMessage(chr, CWvsContext.BuffPacket.giveForeignBuff(chr, stat, SkillFactory.getSkill(1101013).getEffect(chr.getSkillLevel(1101013))), false);
                              } else if (skill.getId() == 400011012) {
                                 SkillFactory.getSkill(400011013).getEffect(chr.getSkillLevel(400011012)).applyTo(c.getPlayer(), pos);
                                 SkillFactory.getSkill(400011014).getEffect(chr.getSkillLevel(400011012)).applyTo(c.getPlayer(), pos);
                              }
                           }
                        }

                     }
                  }
               }
            }
         }
      } else {
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      }
   }

   public static void JupiterThunder(MapleCharacter player, LittleEndianAccessor slea, int type) {
      int skillid = slea.readInt();
      int unk0 = slea.readInt();
      int unk1 = slea.readInt();
      int unk2 = slea.readInt();
      Point pos = slea.readIntPos();
      int unk3 = slea.readInt();
      int unk4 = slea.readInt();
      SecondaryStatEffect effect = SkillFactory.getSkill(skillid).getEffect(player.getSkillLevel(skillid));
      if (player.getMap().getId() != ServerConstants.warpMap) {
         player.getMap().broadcastMessage(CField.CreateJupiterThunder(player, skillid, pos, unk3, unk4, effect.getX(), effect.getSubTime() / 1000, effect.getDuration() / 1000, unk0, unk1, unk2));
      }

      player.getClient().getSession().writeAndFlush(CField.skillCooldown(effect.getSourceId(), effect.getCooldown(player)));
      player.addCooldown(effect.getSourceId(), System.currentTimeMillis(), (long)effect.getCooldown(player));
   }

   public static final void closeRangeAttack(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr, boolean energy) {
      if (chr.getPlayer().getMapId() != 120043000) {
         if (chr != null) {
            if (chr.getMap() != null) {
               AttackInfo attack = DamageParse.parseDmgM(slea, chr, energy);
               if (attack == null) {
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               } else {
                  boolean mirror = chr.getBuffedValue(SecondaryStat.ShadowPartner) != null || chr.getBuffedValue(SecondaryStat.Buckshot) != null;
                  double maxdamage = (double)chr.getStat().getCurrentMaxBaseDamage();
                  Item shield = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
                  int skillLevel = false;
                  SecondaryStatEffect effect = null;
                  Skill skill = null;
                  if (!GameConstants.isLinkMap(chr.getMapId()) || attack.skill != 80001770) {
                     ArrayList mobList;
                     Iterator var25;
                     int pose;
                     MapleSummon tosummon;
                     MapleMist newmist;
                     if (attack.skill != 0) {
                        if (GameConstants.isKinesis(chr.getJob()) && attack.skill == 400021074) {
                           chr.givePPoint(attack.skill);
                        }

                        skill = SkillFactory.getSkill(attack.skill);
                        if (skill == null || GameConstants.isAngel(attack.skill) && chr.getStat().equippedSummon % 10000 != attack.skill % 10000) {
                           c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                           return;
                        }

                        int skillLevel = chr.getTotalSkillLevel(skill);
                        effect = attack.getAttackEffect(chr, skillLevel, skill);
                        if (effect == null) {
                           return;
                        }

                        if (attack.skill == 4221016 && chr.getBuffedValue(4221020)) {
                           chr.cancelEffectFromBuffStat(SecondaryStat.Murderous, 4221020);
                        }

                        Iterator iterator;
                        if (GameConstants.isKadena(chr.getJob())) {
                           if (chr.getBuffedValue(64001001)) {
                              chr.cancelEffectFromBuffStat(SecondaryStat.BeyondNextAttackProb);
                           } else if (attack.skill != 64121001) {
                              if (attack.skill == 64121002) {
                                 SkillFactory.getSkill(64120006).getEffect(chr.getSkillLevel(64120006)).applyTo(chr);
                              }
                           } else {
                              mobList = new ArrayList();
                              mobList.add(new Pair(0, 64121002));
                              mobList.add(new Pair(1, 64001002));
                              mobList.add(new Pair(1, 64001012));
                              mobList.add(new Pair(2, 64101002));
                              mobList.add(new Pair(2, 64101008));
                              mobList.add(new Pair(3, 64101001));
                              mobList.add(new Pair(4, 64111002));
                              mobList.add(new Pair(5, 64111003));
                              mobList.add(new Pair(6, 64111004));
                              mobList.add(new Pair(6, 64111012));
                              mobList.add(new Pair(7, 64121021));
                              mobList.add(new Pair(7, 64121022));
                              mobList.add(new Pair(7, 64121023));
                              mobList.add(new Pair(7, 64121024));
                              mobList.add(new Pair(8, 64121003));
                              mobList.add(new Pair(8, 64121011));
                              mobList.add(new Pair(8, 64121016));
                              if (chr.getBuffedEffect(SecondaryStat.WeaponVariety) == null) {
                                 chr.weaponChanges1.clear();
                              }

                              iterator = mobList.iterator();

                              while(iterator.hasNext()) {
                                 Pair<Integer, Integer> info = (Pair)iterator.next();
                                 if ((Integer)info.left != 0 && !chr.weaponChanges1.containsKey(info.left) && chr.weaponChanges1.size() < 8) {
                                    chr.weaponChanges1.put((Integer)info.left, (Integer)info.right);
                                 }
                              }

                              SkillFactory.getSkill(64120006).getEffect(chr.getSkillLevel(64120006)).applyTo(chr, Integer.MAX_VALUE);
                           }
                        }

                        if (attack.skill != 61121105 && attack.skill != 61121222 && attack.skill != 24121052) {
                           if (attack.skill - 64001009 >= -2 && attack.skill - 64001009 <= 2) {
                              effect.applyTo(chr, attack.chain, true, true);
                           } else if (attack.skill == 400011084) {
                              SkillFactory.getSkill(attack.skill).getEffect(attack.skilllevel).applyTo(chr, attack.position, false);
                           } else if (attack.skill != 400011050 && attack.skill != 400041002 && attack.skill != 400041003 && attack.skill != 400041004 && attack.skill != 400041005 && attack.skill != 400011081 && attack.skill != 400011058 && attack.skill != 400011109 && attack.skill != 151101010 && attack.skill != 151101008 && attack.skill != 151101007 && attack.skill != 151101006 && attack.skill != 31101002 && attack.skill != 51001005 && attack.summonattack == 0 && attack.skill != 400031066 && attack.skill != 400031064 && attack.skill != 400001010 && attack.skill != 400001011 && attack.skill != 400031000 && attack.skill != 400011089 && attack.skill != 400041021 && attack.skill != 400011121 && attack.skill != 27101202 && (attack.skill == 4341002 || GameConstants.isCooltimeKeyDownSkill(attack.skill) && chr.getCooldownLimit(GameConstants.getLinkedSkill(attack.skill)) == 0L || !GameConstants.isCooltimeKeyDownSkill(attack.skill) && !GameConstants.isNoDelaySkill(attack.skill) && !GameConstants.isNoApplySkill(attack.skill)) && attack.skill != 400011087 && attack.skill != 11121052 && attack.skill != 11121055 && attack.skill != 11121056 && attack.skill != 400011056 && attack.skill != 400011142 && !energy) {
                              if (GameConstants.isDemonSlash(attack.skill)) {
                                 skill.getEffect(attack.skilllevel).applyTo(chr);
                              } else if (attack.skill == 36101001) {
                                 skill.getEffect(attack.skilllevel).applyTo(chr, false);
                              } else if (!skill.isFinalAttack()) {
                                 effect.applyTo(chr, attack.position);
                              }
                           }
                        } else {
                           var25 = attack.mistPoints.iterator();

                           while(var25.hasNext()) {
                              Point mistPos = (Point)var25.next();
                              effect.applyTo(chr, false, mistPos);
                           }
                        }

                        int maxcount;
                        SecondaryStatEffect eff;
                        if (GameConstants.isPathFinder(chr.getJob())) {
                           if (attack.skill != 400031051 && attack.skill != 3301008 && attack.skill != 400031039 && attack.skill != 400031040 && attack.targets > 0) {
                              MapleCharacter.렐릭게이지(chr.getClient(), attack.skill);
                           }

                           if (attack.skill == 3321005 && attack.targets > 0 && c.getPlayer().문양 == 1) {
                              eff = SkillFactory.getSkill(3300005).getEffect(chr.getSkillLevel(3300005));
                              MapleAtom atom = new MapleAtom(false, chr.getId(), 57, true, 3300005, chr.getTruePosition().x, chr.getTruePosition().y);
                              List<MapleMapObject> objs = c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getTruePosition(), 400000.0D, Arrays.asList(MapleMapObjectType.MONSTER));
                              List<Integer> monsters = new ArrayList();
                              MapleMonster mob = null;
                              if (!objs.isEmpty() && eff.makeChanceResult()) {
                                 maxcount = eff.getBulletCount();
                                 if (chr.getBuffedValue(3321034)) {
                                    ++maxcount;
                                 }

                                 for(byte i = 0; i < maxcount; ++i) {
                                    if (objs.size() > 0 && mob == null) {
                                       mob = chr.getMap().getMonsterByOid(((MapleMapObject)objs.get(Randomizer.rand(0, objs.size() - 1))).getObjectId());
                                    }

                                    monsters.add(mob != null ? mob.getObjectId() : 0);
                                    atom.addForceAtom(new ForceAtom(1, Randomizer.rand(1, 43), Randomizer.rand(1, 4), 0, 60, chr.getTruePosition()));
                                 }

                                 atom.setDwTargets(monsters);
                                 atom.setSearchX(104);
                                 atom.setSearchY(2);
                                 atom.setSearchX1(500);
                                 atom.setSearchY1(200);
                                 atom.setnDuration(2);
                                 chr.getMap().spawnMapleAtom(atom);
                              }
                           }

                           if (attack.skill != 3011004 && attack.skill != 3300002 && attack.skill != 3321003) {
                              MapleCharacter.문양(chr.getClient(), attack.skill);
                           }
                        } else if (GameConstants.isEunWol(chr.getJob())) {
                           if (chr.getBuffedValue(25101009) && SkillFactory.getSkill(25100009).getSkillList().contains(attack.skill) && attack.targets > 0) {
                              pose = chr.getSkillLevel(25120110) > 0 ? 25120110 : 25100009;
                              int suc = chr.getSkillLevel(25120154) > 0 ? 35 : (chr.getSkillLevel(25120115) > 0 ? 25 : 15);
                              MapleAtom atom;
                              if (attack.skill == 400051079) {
                                 atom = new MapleAtom(false, chr.getId(), 13, true, 25120115, chr.getTruePosition().x, chr.getTruePosition().y);
                                 List<MapleMapObject> objs = chr.getMap().getMapObjectsInRange(chr.getTruePosition(), 500000.0D, Arrays.asList(MapleMapObjectType.MONSTER));
                                 List<Integer> targets = new ArrayList();
                                 maxcount = 0;

                                 while(true) {
                                    if (maxcount >= (attack.skill == 400051079 ? effect.getV2() / 3 : 1)) {
                                       atom.setnFoxSpiritSkillId(attack.skill);
                                       atom.setDwTargets(targets);
                                       chr.getMap().spawnMapleAtom(atom);
                                       break;
                                    }

                                    targets.add(objs.size() > maxcount ? ((MapleMapObject)objs.get(maxcount)).getObjectId() : 0);
                                    atom.addForceAtom(new ForceAtom(2, 15, 25, 50, 630));
                                    ++maxcount;
                                 }
                              } else if (Randomizer.isSuccess(suc)) {
                                 chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, pose, pose, 1, 0, 0, (byte)0, true, (Point)null, (String)null, (Item)null));
                                 chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, pose, pose, 1, 0, 0, (byte)0, false, (Point)null, (String)null, (Item)null), false);
                                 atom = new MapleAtom(false, chr.getId(), 13, true, pose == 25100009 ? 25100010 : 25120115, chr.getTruePosition().x, chr.getTruePosition().y);
                                 atom.addForceAtom(new ForceAtom(pose == 25100009 ? 1 : 2, Randomizer.rand(10, 20), Randomizer.rand(20, 40), Randomizer.rand(40, 50), 630));
                                 chr.getMap().spawnMapleAtom(atom);
                              }
                           }
                        } else if (GameConstants.isCain(chr.getJob())) {
                           chr.handleRemainIncense(attack.skill, false);
                        } else if (GameConstants.isMichael(chr.getJob())) {
                           if (!chr.skillisCooling(400011032) && (attack.skill == 400011032 || attack.skill == 400011033 || attack.skill == 400011034 || attack.skill == 400011035 || attack.skill == 400011036 || attack.skill == 400011037 || attack.skill == 400011067)) {
                              eff = SkillFactory.getSkill(400011032).getEffect(chr.getSkillLevel(400011032));
                              chr.setSkillCustomInfo(400011033, 0L, 5000L);
                              if (chr.getBuffedValue(SecondaryStat.RoyalGuardState) != null) {
                                 long duration = chr.getBuffLimit(51001005);
                                 duration += (long)(eff.getQ() * 1000);
                                 if (duration >= 12000L) {
                                    duration = 12000L;
                                 }

                                 SkillFactory.getSkill(51001005).getEffect(chr.getSkillLevel(51001005)).applyTo(chr, false, (int)duration);
                              }
                           }
                        } else if (GameConstants.isZero(chr.getJob())) {
                           if (chr.getSkillLevel(100000267) > 0) {
                              MapleMonster monster;
                              AttackPair a;
                              ArrayList mobList;
                              switch(attack.skill) {
                              case 101000100:
                              case 101000101:
                              case 101001100:
                              case 101100100:
                              case 101100101:
                              case 101101100:
                              case 101110100:
                              case 101110103:
                              case 101111100:
                              case 101120100:
                              case 101120102:
                              case 101120104:
                              case 101121100:
                                 if (chr.getGender() == 1) {
                                    if (chr.RapidTimeStrength < 10) {
                                       ++chr.RapidTimeStrength;
                                    }

                                    if (attack.asist == 0) {
                                       chr.ZeroSkillCooldown(attack.skill);
                                    }

                                    if (chr.getBuffedEffect(SecondaryStat.Bless5th) != null) {
                                       monster = null;
                                       iterator = attack.allDamage.iterator();
                                       if (iterator.hasNext()) {
                                          a = (AttackPair)iterator.next();
                                          monster = chr.getMap().getMonsterByOid(a.objectId);
                                       }

                                       if (chr.getSkillCustomValue0(400001050) > 0L && monster != null) {
                                          chr.removeSkillCustomInfo(400001050);
                                          mobList = new ArrayList();
                                          mobList.add(new Triple(monster.getObjectId(), 89, 0));
                                          chr.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(400001056, mobList, false, 0));
                                       } else {
                                          chr.setSkillCustomInfo(400001050, 1L, 0L);
                                       }
                                    }

                                    eff = SkillFactory.getSkill(100000277).getEffect(chr.getSkillLevel(100000267));
                                    eff.applyTo(chr);
                                 }
                                 break;
                              case 101000200:
                              case 101000201:
                              case 101001200:
                              case 101100200:
                              case 101100201:
                              case 101101200:
                              case 101110200:
                              case 101110202:
                              case 101110203:
                              case 101111200:
                              case 101120201:
                              case 101120202:
                              case 101120204:
                              case 101121200:
                                 if (chr.getGender() == 0) {
                                    if (chr.RapidTimeDetect < 10) {
                                       ++chr.RapidTimeDetect;
                                    }

                                    if (attack.asist == 0) {
                                       chr.ZeroSkillCooldown(attack.skill);
                                    }

                                    if (chr.getBuffedEffect(SecondaryStat.Bless5th) != null) {
                                       monster = null;
                                       iterator = attack.allDamage.iterator();
                                       if (iterator.hasNext()) {
                                          a = (AttackPair)iterator.next();
                                          monster = chr.getMap().getMonsterByOid(a.objectId);
                                       }

                                       if (chr.getSkillCustomValue0(400001050) > 0L && monster != null) {
                                          chr.removeSkillCustomInfo(400001050);
                                          mobList = new ArrayList();
                                          mobList.add(new Triple(monster.getObjectId(), 89, 0));
                                          chr.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(400001056, mobList, false, 0));
                                       } else {
                                          chr.setSkillCustomInfo(400001050, 1L, 0L);
                                       }
                                    }

                                    eff = SkillFactory.getSkill(100000276).getEffect(chr.getSkillLevel(100000267));
                                    eff.applyTo(chr);
                                 }
                              }
                           }

                           if (attack.skill == 101120104) {
                              eff = SkillFactory.getSkill(attack.skill).getEffect(c.getPlayer().getSkillLevel(101120104));
                              newmist = new MapleMist(eff.calculateBoundingBox(attack.position, c.getPlayer().isFacingLeft()), c.getPlayer(), eff, 5000, (byte)(c.getPlayer().isFacingLeft() ? 1 : 0));
                              newmist.setPosition(attack.position);
                              c.getPlayer().getMap().spawnMist(newmist, false);
                           } else if (attack.skill == 400011098 || attack.skill == 400011100) {
                              eff = SkillFactory.getSkill(attack.skill).getEffect(c.getPlayer().getSkillLevel(attack.skill));
                              newmist = new MapleMist(eff.calculateBoundingBox(chr.getPosition(), c.getPlayer().isFacingLeft()), c.getPlayer(), eff, eff.getCooldown(chr), (byte)(c.getPlayer().isFacingLeft() ? 1 : 0));
                              newmist.setPosition(chr.getPosition());
                              newmist.setDelay(0);
                              c.getPlayer().getMap().spawnMist(newmist, false);
                           }
                        } else if (GameConstants.isPinkBean(chr.getJob()) && chr.getBuffedEffect(SecondaryStat.PinkbeanMinibeenMove) != null && Randomizer.isSuccess(15)) {
                           pose = 0;
                           iterator = chr.getMap().getAllSummons(131002015).iterator();

                           while(iterator.hasNext()) {
                              MapleSummon s = (MapleSummon)iterator.next();
                              if (s.getOwner().getId() == chr.getId()) {
                                 ++pose;
                              }
                           }

                           if (pose < 3) {
                              tosummon = new MapleSummon(chr, 131002015, chr.getTruePosition(), SummonMovementType.BIRD_FOLLOW, (byte)0, effect.getDuration());
                              chr.addSummon(tosummon);
                              chr.getMap().spawnSummon(tosummon, 10000);
                           }
                        }

                        switch(attack.skill) {
                        case 61101002:
                        case 61110211:
                        case 61120007:
                        case 61121217:
                           effect = attack.getAttackEffect(chr, chr.getSkillLevel(attack.skill), skill);
                           DamageParse.applyAttack(attack, skill, c.getPlayer(), maxdamage, effect, mirror ? AttackType.NON_RANGED_WITH_MIRROR : AttackType.NON_RANGED, false, energy);
                           chr.cancelEffectFromBuffStat(SecondaryStat.StopForceAtominfo);
                           break;
                        case 164101002:
                        case 164111006:
                        case 164111010:
                        case 164121002:
                           SecondaryStatEffect dark = SkillFactory.getSkill(164101006).getEffect(chr.getSkillLevel(164101006));
                           dark.applyTo(chr);
                           break;
                        case 164121042:
                           chr.setSkillCustomInfo(164121042, chr.getSkillCustomValue0(164121042) - 1L, 0L);
                           if (chr.getSkillCustomValue0(164121042) <= 0L) {
                              chr.removeSkillCustomInfo(164121042);
                           }
                        }

                        if (!chr.skillisCooling(attack.skill) && (attack.skill == 155120000 || attack.skill == 155110000)) {
                           c.getSession().writeAndFlush(CField.skillCooldown(155001102, 2200));
                        }

                        SecondaryStatEffect eff2;
                        Item weapon_item;
                        String weapon_name;
                        if (chr.getSkillLevel(80002632) > 0) {
                           weapon_item = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
                           if (weapon_item != null) {
                              weapon_name = MapleItemInformationProvider.getInstance().getName(weapon_item.getItemId());
                              if (weapon_name != null && weapon_name.startsWith("제네시스 ") && !chr.getBuffedValue(80002632) && !chr.skillisCooling(80002632)) {
                                 eff2 = SkillFactory.getSkill(80002632).getEffect(chr.getSkillLevel(80002632));
                                 eff2.applyTo(chr);
                                 c.getSession().writeAndFlush(CField.skillCooldown(80002632, 90000));
                                 chr.addCooldown(80002632, System.currentTimeMillis(), 90000L);
                              }
                           }
                        }

                        if (chr.getSkillLevel(80002416) > 0) {
                           weapon_item = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
                           if (weapon_item != null) {
                              weapon_name = MapleItemInformationProvider.getInstance().getName(weapon_item.getItemId());
                              if (weapon_name != null && weapon_name.startsWith("아스칼론 ") && !chr.getBuffedValue(1221054) && !chr.skillisCooling(1221054) && c.getPlayer().getSkillCustomValue0(1221054) == 0L) {
                                 eff2 = SkillFactory.getSkill(1221054).getEffect(chr.getSkillLevel(1221054));
                                 chr.setSkillCustomInfo(1221054, 1L, 300000L);
                                 eff2.applyTo(chr);
                              } else if (weapon_name != null && weapon_name.startsWith("아스칼론 ") && !chr.getBuffedValue(32121017)) {
                                 eff2 = SkillFactory.getSkill(32121017).getEffect(chr.getSkillLevel(32121017));
                                 chr.addBuffCheck = 1;
                                 eff2.applyTo(chr);
                              }
                           }
                        }

                        if (GameConstants.isStriker(chr.getJob()) && attack.isLink && (attack.skill < 400051059 || attack.skill > 400051067) && attack.skill != 0 && chr.getCooldownLimit(15101028) == 0L) {
                           c.getSession().writeAndFlush(CField.getSeaWave(chr));
                        }

                        if (GameConstants.isKhali(chr.getJob()) && !chr.getBuffedValue(150030241)) {
                           eff = SkillFactory.getSkill(150030241).getEffect(chr.getSkillLevel(150030241));
                           eff.applyTo(chr, eff.getDuration());
                           chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, 150030241, 1, 0, 0, (byte)0, true, chr.getTruePosition(), (String)null, (Item)null));
                        }

                        if (GameConstants.isSoulMaster(chr.getJob()) && attack.skill != 400001018) {
                           SecondaryStatEffect eff;
                           if (GameConstants.isElementSoul(attack.skill)) {
                              pose = chr.getBuffedValue(SecondaryStat.PoseType);
                              eff = SkillFactory.getSkill(11001027).getEffect(chr.getSkillLevel(11001027));
                              eff2 = SkillFactory.getSkill(11001030).getEffect(chr.getSkillLevel(11001030));
                              int[] passiveSkill = new int[]{11001022, 11100034, 11110031, 11120019};
                              if (pose > 0 && attack.targets > 0 && chr.getBuffedValue(SecondaryStat.ElementSoul) != null) {
                                 int prop = 0;
                                 maxcount = 0;
                                 int count = c.getPlayer().getCosmicCount();
                                 if ((long)pose != chr.getSkillCustomValue0(11001022)) {
                                    for(int i = 0; i < passiveSkill.length; ++i) {
                                       if (chr.getSkillLevel(passiveSkill[i]) > 0) {
                                          SecondaryStatEffect skills = SkillFactory.getSkill(passiveSkill[i]).getEffect(chr.getSkillLevel(passiveSkill[i]));
                                          prop += skills.getProp();
                                          maxcount += skills.getX();
                                       }
                                    }

                                    if (chr.getBuffedValue(SecondaryStat.CosmicForge) != null) {
                                       SecondaryStatEffect ef = SkillFactory.getSkill(11121054).getEffect(chr.getSkillLevel(11121054));
                                       maxcount += ef.getX();
                                    }

                                    if (Randomizer.isSuccess(prop)) {
                                       if (count < maxcount) {
                                          c.getPlayer().setCosmicCount(count + 1);
                                       } else {
                                          c.getPlayer().setCosmicCount(maxcount);
                                       }
                                    }

                                    eff.applyTo(chr, eff.getDuration());
                                    eff2.applyTo(chr, eff2.getDuration());
                                 }
                              }

                              chr.setSkillCustomInfo(11001022, (long)pose, 0L);
                           }

                           if (chr.getBuffedValue(SecondaryStat.GlimmeringTime) != null) {
                              eff = SkillFactory.getSkill(11001024).getEffect(chr.getSkillLevel(11001024));
                              eff = SkillFactory.getSkill(11001025).getEffect(chr.getSkillLevel(11001025));
                              if (chr.getBuffedValue(11001025)) {
                                 chr.cancelEffect(chr.getBuffedEffect(11001025));
                                 eff.applyTo(chr, eff.getDuration());
                              } else {
                                 chr.cancelEffect(chr.getBuffedEffect(11001024));
                                 eff.applyTo(chr, eff.getDuration());
                              }
                           }

                           if (attack.skill == 11101030) {
                              chr.setCosmicCount(0);
                           }
                        }

                        boolean linkcool = true;
                        if (GameConstants.isCain(chr.getJob())) {
                           linkcool = chr.handleCainSkillCooldown(attack.skill);
                        }

                        switch(attack.skill) {
                        case 11121018:
                        case 24121005:
                        case 25111005:
                        case 25111012:
                        case 61101002:
                        case 155121004:
                        case 162111005:
                        case 162111006:
                        case 400011135:
                        case 400021122:
                        case 400031033:
                        case 400051008:
                           linkcool = false;
                           break;
                        case 21110018:
                           linkcool = false;
                           effect = SkillFactory.getSkill(21111019).getEffect(attack.skilllevel);
                           c.getSession().writeAndFlush(CField.skillCooldown(21111018, effect.getCooldown(chr)));
                           chr.addCooldown(21111018, System.currentTimeMillis(), (long)effect.getCooldown(chr));
                        }

                        if (GameConstants.isZero(chr.getJob()) && attack.asist == 1 && SkillFactory.getSkill(attack.skill).getCategoryIndex() != chr.getGender()) {
                           linkcool = false;
                        }

                        if (effect.getCooldown(chr) > 0 && !effect.ignoreCooldown(chr) && (attack.skill < 400041003 || attack.skill > 400041005) && linkcool && attack.skill != 1111016) {
                           if (!energy && chr.skillisCooling(effect.getSourceId()) && !GameConstants.isCooltimeKeyDownSkill(effect.getSourceId()) && !GameConstants.isNoApplySkill(effect.getSourceId()) && !GameConstants.isLinkedSkill(attack.skill) && !chr.getBuffedValue(effect.getSourceId()) && attack.skill != 15101028) {
                              c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                              return;
                           }

                           if (effect.getSourceId() != 15111022 && effect.getSourceId() != 15120003) {
                              if (effect.getSourceId() == 1321013) {
                                 if (!chr.getBuffedValue(1321015) && chr.getBuffedEffect(SecondaryStat.Reincarnation) == null) {
                                    c.getSession().writeAndFlush(CField.skillCooldown(effect.getSourceId(), effect.getCooldown(chr)));
                                    chr.addCooldown(effect.getSourceId(), System.currentTimeMillis(), (long)effect.getCooldown(chr));
                                 }
                              } else if (effect.getSourceId() == 11121055) {
                                 if (chr.getBuffedEffect(SecondaryStat.Ellision) == null) {
                                    c.getSession().writeAndFlush(CField.skillCooldown(effect.getSourceId(), effect.getCooldown(chr)));
                                    chr.addCooldown(effect.getSourceId(), System.currentTimeMillis(), (long)effect.getCooldown(chr));
                                 }
                              } else if (!chr.getBuffedValue(20040219) && !chr.getBuffedValue(20040220)) {
                                 if (attack.skill != 400011079 && attack.skill != 400011081 && attack.skill != 400011080 && attack.skill != 400011082) {
                                    if (!chr.skillisCooling(effect.getSourceId()) && !GameConstants.isAutoAttackSkill(effect.getSourceId()) && !GameConstants.isAfterCooltimeSkill(effect.getSourceId())) {
                                       c.getSession().writeAndFlush(CField.skillCooldown(effect.getSourceId(), effect.getCooldown(chr)));
                                       chr.addCooldown(effect.getSourceId(), System.currentTimeMillis(), (long)effect.getCooldown(chr));
                                    }
                                 } else if (chr.getBuffedEffect(SecondaryStat.WillofSwordStrike) != null) {
                                    if (chr.ignoreDraco > 0 && (attack.skill == 400011079 || attack.skill == 400011080)) {
                                       --chr.ignoreDraco;
                                       if (chr.ignoreDraco <= 0) {
                                          chr.cancelEffectFromBuffStat(SecondaryStat.WillofSwordStrike);
                                       } else {
                                          Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
                                          statups.put(SecondaryStat.WillofSwordStrike, new Pair(chr.ignoreDraco, (int)chr.getBuffLimit(chr.getBuffSource(SecondaryStat.WillofSwordStrike))));
                                          chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, chr.getBuffedEffect(SecondaryStat.WillofSwordStrike), chr));
                                       }
                                    }
                                 } else if (attack.skill == 400011079 || attack.skill == 400011080) {
                                    c.getSession().writeAndFlush(CField.skillCooldown(effect.getSourceId(), effect.getCooldown(chr)));
                                    chr.addCooldown(effect.getSourceId(), System.currentTimeMillis(), (long)effect.getCooldown(chr));
                                 }
                              } else if (skill.isHyper() || !GameConstants.isLuminous(effect.getSourceId() / 10000)) {
                                 c.getSession().writeAndFlush(CField.skillCooldown(effect.getSourceId(), effect.getCooldown(chr)));
                                 chr.addCooldown(effect.getSourceId(), System.currentTimeMillis(), (long)effect.getCooldown(chr));
                              }
                           } else if (!chr.getBuffedValue(15121054)) {
                              c.getSession().writeAndFlush(CField.skillCooldown(effect.getSourceId(), effect.getCooldown(chr)));
                              chr.addCooldown(effect.getSourceId(), System.currentTimeMillis(), (long)effect.getCooldown(chr));
                           }
                        }
                     }

                     if (!energy && (chr.getMapId() == 109060000 || chr.getMapId() == 109060002 || chr.getMapId() == 109060004) && attack.skill == 0) {
                        MapleSnowball.MapleSnowballs.hitSnowball(chr);
                     }

                     chr.checkFollow();
                     if (!chr.isHidden()) {
                        chr.getMap().broadcastMessage(chr, CField.addAttackInfo(0, chr, attack), chr.getTruePosition());
                     } else {
                        chr.getMap().broadcastGMMessage(chr, CField.addAttackInfo(0, chr, attack), false);
                     }

                     if (GameConstants.isBattleMage(chr.getJob())) {
                        if (chr.getBuffedValue(SecondaryStat.BMageDeath) != null && !chr.skillisCooling(32001114)) {
                           int duration = chr.getSkillLevel(32120019) > 0 ? 5000 : (chr.getSkillLevel(32110017) > 0 ? 6000 : (chr.getSkillLevel(32100010) > 0 ? 8000 : 9000));
                           if (chr.getSkillCustomValue0(32120019) > 0L) {
                              chr.setDeath((byte)0);
                              chr.addCooldown(32001114, System.currentTimeMillis(), (long)duration);
                              chr.removeSkillCustomInfo(32120019);
                              var25 = chr.getSummons().iterator();

                              while(var25.hasNext()) {
                                 tosummon = (MapleSummon)var25.next();
                                 if (tosummon.getSkill() == chr.getBuffSource(SecondaryStat.BMageDeath)) {
                                    chr.getClient().getSession().writeAndFlush(CField.SummonPacket.DeathAttack(tosummon));
                                    break;
                                 }
                              }

                              Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
                              statups.put(SecondaryStat.BMageDeath, new Pair(Integer.valueOf(chr.getDeath()), 0));
                              chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, chr.getBuffedEffect(SecondaryStat.BMageDeath), chr));
                           }
                        }
                     } else if (GameConstants.isBlaster(chr.getJob())) {
                        List<RangeAttack> skills = new ArrayList();
                        if (attack.skill == 37001000 || attack.skill == 37101000) {
                           skills.clear();
                           pose = chr.getSkillLevel(37100007) > 0 ? 37100007 : 37000007;
                           skills.add(new RangeAttack(pose, chr.getTruePosition(), (attack.facingleft >>> 4 & 15) == 0 ? 0 : 1, 0, 1));
                           chr.getClient().getSession().writeAndFlush(CField.rangeAttack(attack.skill, skills));
                        }

                        if (attack.skill == 37121000) {
                           skills.clear();
                           skills.add(new RangeAttack(37120001, chr.getTruePosition(), (attack.facingleft >>> 4 & 15) == 0 ? 0 : 1, 0, 1));
                           chr.getClient().send(CField.rangeAttack(attack.skill, skills));
                        }

                        if (attack.skill == 37001002 || attack.skill == 37000011 || attack.skill == 37000012 || attack.skill == 37000013) {
                           skills.clear();
                           skills.add(new RangeAttack(37000008, chr.getTruePosition(), (attack.facingleft >>> 4 & 15) == 0 ? 0 : 1, 0, 1));
                           if (chr.getJob() >= 3710) {
                              skills.add(new RangeAttack(37100009, chr.getTruePosition(), (attack.facingleft >>> 4 & 15) == 0 ? 0 : 1, 0, 1));
                           }

                           if (chr.getJob() >= 3711) {
                              skills.add(new RangeAttack(37110010, chr.getTruePosition(), (attack.facingleft >>> 4 & 15) == 0 ? 0 : 1, 0, 1));
                           }

                           if (chr.getJob() >= 3712) {
                              skills.add(new RangeAttack(37120013, chr.getTruePosition(), (attack.facingleft >>> 4 & 15) == 0 ? 0 : 1, 0, 1));
                           }

                           chr.getClient().send(CField.rangeAttack(attack.skill, skills));
                        }

                        if (attack.skill == 400011028 || attack.skill == 37121003) {
                           chr.combinationBuff = 10;
                           SkillFactory.getSkill(37120012).getEffect(c.getPlayer().getSkillLevel(37120012)).applyTo(c.getPlayer());
                        }

                        if (attack.skill == 400011019 || attack.skill == 37001004 || attack.skill == 37000005 || attack.skill == 37000009 || attack.skill == 37001002 || attack.skill == 37100008 || attack.skill == 37121004 || attack.skill >= 37120014 && attack.skill <= 37120019 || attack.skill == 37120023 || attack.skill == 37001002 || attack.skill == 37000011 || attack.skill == 37000012 || attack.skill == 37000013) {
                           chr.Cylinder(attack.skill);
                        }

                        if (chr.bullet > 0 && chr.getBuffedValue(400011017) && attack.skill != 400011019 && (attack.skill == 37001000 || attack.skill == 37101000 || attack.skill == 37110001 || attack.skill == 37120002 || attack.skill == 37121000 || attack.skill == 37121003 || attack.skill == 37121052)) {
                           mobList = new ArrayList();
                           chr.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(400011019, mobList, true, 0));
                        }

                        if (attack.skill == 37110001) {
                           var25 = chr.getMap().getAllMistsThreadsafe().iterator();

                           while(var25.hasNext()) {
                              newmist = (MapleMist)var25.next();
                              if (newmist.getSourceSkill().getId() == 37110002) {
                                 chr.getMap().removeMist(37110002);
                                 break;
                              }
                           }

                           SkillFactory.getSkill(37110002).getEffect(chr.getTotalSkillLevel(GameConstants.getLinkedSkill(attack.skill))).applyTo(chr, attack.position);
                        }

                        if (chr.getCylinderGauge() > 0) {
                           skills.clear();
                           skills.add(new RangeAttack(37000007, chr.getTruePosition(), (attack.facingleft >>> 4 & 15) == 0 ? 0 : 1, 0, 1));
                           chr.getClient().getSession().writeAndFlush(CField.rangeAttack(attack.skill, skills));
                        }
                     } else if (GameConstants.isPinkBean(chr.getJob()) && attack.skill == 131001010 && attack.summonattack == 0) {
                        Vmatrixstackbuff(c, true, (LittleEndianAccessor)null);
                     }

                     DamageParse.applyAttack(attack, skill, c.getPlayer(), maxdamage, effect, mirror ? AttackType.NON_RANGED_WITH_MIRROR : AttackType.NON_RANGED, false, energy);
                  }
               }
            }
         }
      }
   }

   public static final void BuffAttack(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      if (chr != null) {
         if (chr.getPlayer().getMapId() != 120043000) {
            if (chr.getMap() != null) {
               AttackInfo attack = DamageParse.parseDmgB(slea, chr);
               if (attack == null) {
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               } else {
                  boolean mirror = chr.getBuffedValue(SecondaryStat.ShadowPartner) != null || chr.getBuffedValue(SecondaryStat.Buckshot) != null;
                  double maxdamage = (double)chr.getStat().getCurrentMaxBaseDamage();
                  Item shield = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
                  int skillLevel = false;
                  SecondaryStatEffect effect = null;
                  Skill skill = null;
                  if (attack.skill != 0) {
                     skill = SkillFactory.getSkill(GameConstants.getLinkedSkill(attack.skill));
                     if (skill == null || GameConstants.isAngel(attack.skill) && chr.getStat().equippedSummon % 10000 != attack.skill % 10000) {
                        c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                        return;
                     }

                     int skillLevel = chr.getTotalSkillLevel(skill);
                     effect = attack.getAttackEffect(chr, skillLevel, skill);
                     if (effect == null) {
                        return;
                     }

                     if (GameConstants.isDemonAvenger(chr.getJob()) && GameConstants.isExceedAttack(attack.skill)) {
                        if (chr.getSkillLevel(31220044) > 0) {
                           if (chr.getExceed() < 18) {
                              chr.gainExceed((short)1);
                           }
                        } else if (chr.getExceed() < 20) {
                           chr.gainExceed((short)1);
                        }

                        chr.handleExceedAttack(attack.skill);
                     }

                     switch(attack.skill) {
                     case 61101002:
                     case 61110211:
                     case 61120007:
                     case 61121217:
                        effect = attack.getAttackEffect(chr, chr.getSkillLevel(attack.skill), skill);
                        DamageParse.applyAttack(attack, skill, c.getPlayer(), maxdamage, effect, mirror ? AttackType.NON_RANGED_WITH_MIRROR : AttackType.NON_RANGED, false, false);
                        chr.cancelEffectFromBuffStat(SecondaryStat.StopForceAtominfo);
                     default:
                        Item weapon_item;
                        String weapon_name;
                        SecondaryStatEffect effcts2;
                        if (chr.getSkillLevel(80002632) > 0) {
                           weapon_item = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
                           if (weapon_item != null) {
                              weapon_name = MapleItemInformationProvider.getInstance().getName(weapon_item.getItemId());
                              if (weapon_name != null && weapon_name.startsWith("제네시스 ") && !chr.skillisCooling(80002632) && !chr.getBuffedValue(80002632)) {
                                 effcts2 = SkillFactory.getSkill(80002632).getEffect(chr.getSkillLevel(80002632));
                                 effcts2.applyTo(chr);
                                 c.getSession().writeAndFlush(CField.skillCooldown(80002632, 90000));
                                 chr.addCooldown(80002632, System.currentTimeMillis(), 90000L);
                              }
                           }
                        }

                        if (chr.getSkillLevel(80002416) > 0) {
                           weapon_item = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
                           if (weapon_item != null) {
                              weapon_name = MapleItemInformationProvider.getInstance().getName(weapon_item.getItemId());
                              if (weapon_name != null && weapon_name.startsWith("아스칼론 ") && !chr.getBuffedValue(1221054) && !chr.skillisCooling(1221054) && c.getPlayer().getSkillCustomValue0(1221054) == 0L) {
                                 effcts2 = SkillFactory.getSkill(1221054).getEffect(chr.getSkillLevel(1221054));
                                 chr.setSkillCustomInfo(1221054, 1L, 300000L);
                                 effcts2.applyTo(chr);
                              } else if (weapon_name != null && weapon_name.startsWith("아스칼론 ") && !chr.getBuffedValue(32121017)) {
                                 effcts2 = SkillFactory.getSkill(32121017).getEffect(chr.getSkillLevel(32121017));
                                 chr.addBuffCheck = 1;
                                 effcts2.applyTo(chr);
                              }
                           }
                        }

                        if (effect.getCooldown(chr) > 0 && !effect.ignoreCooldown(chr) && attack.skill != 162111006) {
                           if (chr.skillisCooling(attack.skill) && !GameConstants.isCooltimeKeyDownSkill(attack.skill) && !GameConstants.isNoApplySkill(attack.skill) && !GameConstants.isLinkedSkill(attack.skill) && !chr.getBuffedValue(attack.skill)) {
                              c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                              return;
                           }

                           if (attack.skill != 15111022 && attack.skill != 15120003) {
                              if (attack.skill == 1321013) {
                                 if (!chr.getBuffedValue(1321015) && chr.getBuffedEffect(SecondaryStat.Reincarnation) == null) {
                                    c.getSession().writeAndFlush(CField.skillCooldown(attack.skill, effect.getCooldown(chr)));
                                    chr.addCooldown(attack.skill, System.currentTimeMillis(), (long)effect.getCooldown(chr));
                                 }
                              } else if (!chr.getBuffedValue(20040219) && !chr.getBuffedValue(20040220)) {
                                 if (!chr.skillisCooling(attack.skill)) {
                                    c.getSession().writeAndFlush(CField.skillCooldown(attack.skill, effect.getCooldown(chr)));
                                    chr.addCooldown(attack.skill, System.currentTimeMillis(), (long)effect.getCooldown(chr));
                                 }
                              } else if (skill.isHyper() || !GameConstants.isLuminous(attack.skill / 10000)) {
                                 c.getSession().writeAndFlush(CField.skillCooldown(attack.skill, effect.getCooldown(chr)));
                                 chr.addCooldown(attack.skill, System.currentTimeMillis(), (long)effect.getCooldown(chr));
                              }
                           } else if (!chr.getBuffedValue(15121054)) {
                              c.getSession().writeAndFlush(CField.skillCooldown(attack.skill, effect.getCooldown(chr)));
                              chr.addCooldown(attack.skill, System.currentTimeMillis(), (long)effect.getCooldown(chr));
                           }

                           if (GameConstants.isLinkedSkill(attack.skill) && !chr.skillisCooling(GameConstants.getLinkedSkill(attack.skill))) {
                              c.getSession().writeAndFlush(CField.skillCooldown(GameConstants.getLinkedSkill(attack.skill), effect.getCooldown(chr)));
                              chr.addCooldown(GameConstants.getLinkedSkill(attack.skill), System.currentTimeMillis(), (long)effect.getCooldown(chr));
                           }
                        }
                     }
                  }

                  if ((chr.getMapId() == 109060000 || chr.getMapId() == 109060002 || chr.getMapId() == 109060004) && attack.skill == 0) {
                     MapleSnowball.MapleSnowballs.hitSnowball(chr);
                  }

                  int numFinisherOrbs = 0;
                  Integer comboBuff = chr.getBuffedValue(SecondaryStat.ComboCounter);
                  if (isFinisher(attack.skill)) {
                     if (comboBuff != null) {
                        numFinisherOrbs = comboBuff - 1;
                     }

                     if (numFinisherOrbs <= 0) {
                        return;
                     }

                     chr.handleOrbconsume(attack.skill);
                  }

                  chr.checkFollow();
                  if (!chr.isHidden()) {
                     chr.getMap().broadcastMessage(chr, CField.addAttackInfo(4, chr, attack), chr.getTruePosition());
                  } else {
                     chr.getMap().broadcastGMMessage(chr, CField.addAttackInfo(4, chr, attack), false);
                  }

                  DamageParse.applyAttack(attack, skill, c.getPlayer(), maxdamage, effect, mirror ? AttackType.NON_RANGED_WITH_MIRROR : AttackType.NON_RANGED, true, false);
               }
            }
         }
      }
   }

   public static final void rangedAttack(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      if (chr.getPlayer().getMapId() != 120043000) {
         if (chr != null) {
            if (chr.getMap() != null) {
               AttackInfo attack = DamageParse.parseDmgR(slea, chr);
               if (attack == null) {
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               } else {
                  int bulletCount = 1;
                  int skillLevel = false;
                  SecondaryStatEffect effect = null;
                  Skill skill = null;
                  boolean AOE = attack.skill == 4111004;
                  boolean noBullet = chr.getJob() >= 300 && chr.getJob() <= 322 && chr.getTotalSkillLevel(3000002) > 0 || chr.getJob() >= 510 && chr.getJob() <= 512 || chr.getJob() >= 3500 && chr.getJob() <= 3512 || GameConstants.isCannon(chr.getJob()) || GameConstants.isPhantom(chr.getJob()) || GameConstants.isMercedes(chr.getJob()) || GameConstants.isZero(chr.getJob()) || GameConstants.isXenon(chr.getJob()) || GameConstants.isKaiser(chr.getJob()) || GameConstants.isAngelicBuster(chr.getJob()) || GameConstants.isKadena(chr.getJob()) || GameConstants.isPathFinder(chr.getJob()) || GameConstants.isYeti(chr.getJob());
                  if (!noBullet) {
                     if (chr.getSkillLevel(13100028) > 0) {
                        noBullet = true;
                     }

                     if (chr.getSkillLevel(4321002) > 0) {
                        noBullet = true;
                     }

                     if (chr.getSkillLevel(33100017) > 0) {
                        noBullet = true;
                     }

                     if (chr.getSkillLevel(4110016) > 0) {
                        noBullet = true;
                     }

                     if (chr.getSkillLevel(14110031) > 0) {
                        noBullet = true;
                     }
                  }

                  if (attack.skill != 0) {
                     skill = SkillFactory.getSkill(GameConstants.getLinkedSkill(attack.skill));
                     if (skill == null || GameConstants.isAngel(attack.skill) && chr.getStat().equippedSummon % 10000 != attack.skill % 10000) {
                        c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                        c.getPlayer().dropMessage(6, "Range Skill Null!");
                        return;
                     }

                     int skillLevel = chr.getTotalSkillLevel(skill);
                     effect = attack.getAttackEffect(chr, skillLevel, skill);
                     if (effect == null) {
                        return;
                     }

                     if (attack.skill != 61121105 && attack.skill != 61121222 && attack.skill != 24121052) {
                        if (attack.skill - 64001009 >= -2 && attack.skill - 64001009 <= 2) {
                           SkillFactory.getSkill(attack.skill).getEffect(skillLevel).applyTo(chr, attack.chain, true, true);
                        } else if (attack.summonattack == 0 && !GameConstants.isNoDelaySkill(attack.skill) && !GameConstants.isNoApplySkill(attack.skill) && !skill.isFinalAttack()) {
                           skill.getEffect(skillLevel).applyTo(chr, attack.position);
                        }
                     } else {
                        Iterator var12 = attack.mistPoints.iterator();

                        while(var12.hasNext()) {
                           Point mistPos = (Point)var12.next();
                           skill.getEffect(skillLevel).applyTo(chr, false, mistPos);
                        }
                     }

                     switch(attack.skill) {
                     case 1077:
                     case 1078:
                     case 1079:
                     case 11077:
                     case 11078:
                     case 11079:
                     case 3111004:
                     case 3211004:
                     case 4121003:
                     case 4121016:
                     case 4121017:
                     case 4121052:
                     case 4221003:
                     case 5121002:
                     case 11101004:
                     case 13101005:
                     case 13101020:
                     case 13111007:
                     case 14101006:
                     case 15111007:
                     case 21000004:
                     case 21001009:
                     case 21100004:
                     case 21100007:
                     case 21110004:
                     case 21110011:
                     case 21110027:
                     case 21110028:
                     case 21120006:
                     case 22110025:
                     case 33101002:
                     case 33101007:
                     case 33121001:
                     case 33121002:
                     case 33121052:
                     case 35121054:
                     case 51001004:
                     case 51111007:
                     case 51121008:
                     case 400010000:
                        AOE = true;
                        bulletCount = effect.getAttackCount();
                        break;
                     case 5211008:
                     case 5221017:
                     case 5221052:
                     case 13001020:
                     case 13111020:
                     case 13121002:
                     case 13121052:
                        bulletCount = effect.getAttackCount();
                        break;
                     case 5220023:
                     case 5220024:
                     case 5220025:
                     case 35111004:
                     case 35121005:
                     case 35121013:
                        AOE = true;
                        bulletCount = 6;
                        break;
                     default:
                        bulletCount = effect.getBulletCount();
                     }

                     if (noBullet && effect.getBulletCount() < effect.getAttackCount()) {
                        bulletCount = effect.getAttackCount();
                     }

                     SecondaryStatEffect effcts2;
                     Item weapon_item;
                     String weapon_name;
                     if (chr.getSkillLevel(80002632) > 0) {
                        weapon_item = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
                        if (weapon_item != null) {
                           weapon_name = MapleItemInformationProvider.getInstance().getName(weapon_item.getItemId());
                           if (weapon_name != null && weapon_name.startsWith("제네시스 ") && !chr.skillisCooling(80002632) && !chr.getBuffedValue(80002632)) {
                              effcts2 = SkillFactory.getSkill(80002632).getEffect(chr.getSkillLevel(80002632));
                              effcts2.applyTo(chr);
                              c.getSession().writeAndFlush(CField.skillCooldown(80002632, 90000));
                              chr.addCooldown(80002632, System.currentTimeMillis(), 90000L);
                           }
                        }
                     }

                     if (chr.getSkillLevel(80002416) > 0) {
                        weapon_item = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
                        if (weapon_item != null) {
                           weapon_name = MapleItemInformationProvider.getInstance().getName(weapon_item.getItemId());
                           if (weapon_name != null && weapon_name.startsWith("아스칼론 ") && !chr.getBuffedValue(1221054) && !chr.skillisCooling(1221054) && c.getPlayer().getSkillCustomValue0(1221054) == 0L) {
                              effcts2 = SkillFactory.getSkill(1221054).getEffect(chr.getSkillLevel(1221054));
                              chr.setSkillCustomInfo(1221054, 1L, 300000L);
                              effcts2.applyTo(chr);
                           } else if (weapon_name != null && weapon_name.startsWith("아스칼론 ") && !chr.getBuffedValue(32121017)) {
                              effcts2 = SkillFactory.getSkill(32121017).getEffect(chr.getSkillLevel(32121017));
                              chr.addBuffCheck = 1;
                              effcts2.applyTo(chr);
                           }
                        }
                     }

                     boolean linkcool = true;
                     if (GameConstants.isCain(chr.getJob())) {
                        linkcool = chr.handleCainSkillCooldown(attack.skill);
                     }

                     switch(attack.skill) {
                     case 400031033:
                        linkcool = false;
                     }

                     if (effect.getCooldown(chr) > 0 && !effect.ignoreCooldown(chr) && linkcool) {
                        if (chr.skillisCooling(effect.getSourceId()) && !GameConstants.isCooltimeKeyDownSkill(effect.getSourceId()) && !GameConstants.isNoApplySkill(attack.skill) && !GameConstants.isLinkedSkill(effect.getSourceId()) && !chr.getBuffedValue(effect.getSourceId())) {
                           c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                           return;
                        }

                        if (effect.getSourceId() != 15111022 && effect.getSourceId() != 15120003) {
                           if (effect.getSourceId() == 3221007) {
                              if (chr.getSkillLevel(3220051) == 0) {
                                 c.getSession().writeAndFlush(CField.skillCooldown(effect.getSourceId(), effect.getCooldown(chr)));
                                 chr.addCooldown(effect.getSourceId(), System.currentTimeMillis(), (long)effect.getCooldown(chr));
                              }
                           } else if (effect.getSourceId() == 1321013) {
                              if (!chr.getBuffedValue(1321015) && chr.getBuffedEffect(SecondaryStat.Reincarnation) == null) {
                                 c.getSession().writeAndFlush(CField.skillCooldown(effect.getSourceId(), effect.getCooldown(chr)));
                                 chr.addCooldown(effect.getSourceId(), System.currentTimeMillis(), (long)effect.getCooldown(chr));
                              }
                           } else if (!chr.getBuffedValue(20040219) && !chr.getBuffedValue(20040220)) {
                              if (!chr.skillisCooling(effect.getSourceId()) && !GameConstants.isAutoAttackSkill(effect.getSourceId()) && !GameConstants.isAfterCooltimeSkill(effect.getSourceId())) {
                                 c.getSession().writeAndFlush(CField.skillCooldown(effect.getSourceId(), effect.getCooldown(chr)));
                                 chr.addCooldown(effect.getSourceId(), System.currentTimeMillis(), (long)effect.getCooldown(chr));
                              }
                           } else if (skill.isHyper() || !GameConstants.isLuminous(effect.getSourceId() / 10000)) {
                              c.getSession().writeAndFlush(CField.skillCooldown(effect.getSourceId(), effect.getCooldown(chr)));
                              chr.addCooldown(effect.getSourceId(), System.currentTimeMillis(), (long)effect.getCooldown(chr));
                           }
                        } else if (!chr.getBuffedValue(15121054)) {
                           c.getSession().writeAndFlush(CField.skillCooldown(effect.getSourceId(), effect.getCooldown(chr)));
                           chr.addCooldown(effect.getSourceId(), System.currentTimeMillis(), (long)effect.getCooldown(chr));
                        }
                     }
                  }

                  if (attack.skill == 400031033) {
                     Vmatrixstackbuff(chr.getClient(), true, (LittleEndianAccessor)null);
                  }

                  if (GameConstants.isMercedes(c.getPlayer().getJob())) {
                     int skillid = 23111011;
                     SecondaryStatEffect eff = SkillFactory.getSkill(skillid).getEffect(c.getPlayer().getSkillLevel(skillid));
                     eff.applyTo(c.getPlayer(), eff.getDuration());
                  }

                  if (attack.skill == 4121017 && c.getPlayer().getCooldownLimit(4121020) == 0L) {
                     List<MapleMapObject> objs = c.getPlayer().getMap().getMapObjectsInRange(new Point(attack.position.x + ((attack.display & '耀') == 0 ? 230 : -230), attack.position.y), 200000.0D, Arrays.asList(MapleMapObjectType.MONSTER));
                     List<AdelProjectile> atoms = new ArrayList();
                     List<Point> points = new ArrayList();
                     int[] durations = new int[]{3110, 2930, 2990, 2870, 3050, 2810};
                     int[] startXs = new int[]{35, 0, 330, 150, 180, 210};
                     int[] createDelays = new int[]{1110, 930, 990, 870, 1050, 810};
                     int[] delays = new int[]{1230, 1050, 1110, 990, 1170, 930};
                     points.add(new Point(325, -145));
                     points.add(new Point(230, -176));
                     points.add(new Point(135, -145));
                     points.add(new Point(320, -45));
                     points.add(new Point(230, 0));
                     points.add(new Point(150, -45));

                     for(int i = 0; i < 6; ++i) {
                        Point diff = (Point)points.get(i);
                        AdelProjectile sword = new AdelProjectile(33, c.getPlayer().getId(), objs.size() == 0 ? 0 : ((MapleMapObject)objs.get(Randomizer.nextInt(objs.size()))).getObjectId(), 4121020, durations[i], (attack.facingleft & '耀') == 0 ? startXs[i] : -startXs[i], 1, new Point(attack.position.x + ((attack.facingleft & '耀') == 0 ? diff.x : -diff.x), attack.position.y + diff.y), new ArrayList());
                        sword.setCreateDelay(createDelays[i]);
                        sword.setDelay(delays[i]);
                        atoms.add(sword);
                     }

                     SkillFactory.getSkill(4121017).getEffect(c.getPlayer().getSkillLevel(4121017)).applyTo(c.getPlayer(), false);
                     c.getPlayer().getMap().spawnAdelProjectile(c.getPlayer(), atoms, false);
                     c.getPlayer().addCooldown(4121020, System.currentTimeMillis(), 5000L);
                  }

                  Integer ShadowPartner = chr.getBuffedValue(SecondaryStat.ShadowPartner);
                  if (ShadowPartner != null) {
                     bulletCount *= 2;
                  }

                  int projectile = 0;
                  int visProjectile = false;
                  Item ipp = chr.getInventory(MapleInventoryType.USE).getItem(attack.slot);
                  if (chr.getAttackitem() == null && ipp != null) {
                     chr.setAttackitem(ipp);
                  } else if (ipp != null && chr.getAttackitem().getItemId() != ipp.getItemId()) {
                     chr.setAttackitem(ipp);
                  }

                  int visProjectile;
                  int bulletConsume;
                  if (!AOE && chr.getBuffedValue(SecondaryStat.SoulArrow) == null && !noBullet) {
                     if (attack.item == 0 && ipp != null) {
                        attack.item = ipp.getItemId();
                     } else if (ipp == null || ipp.getItemId() != attack.item) {
                        return;
                     }

                     projectile = ipp.getItemId();
                     if (attack.csstar > 0) {
                        if (chr.getInventory(MapleInventoryType.CASH).getItem(attack.csstar) == null) {
                           return;
                        }

                        visProjectile = chr.getInventory(MapleInventoryType.CASH).getItem(attack.csstar).getItemId();
                        if (chr.getV("csstar") == null) {
                           chr.addKV("csstar", visProjectile.makeConcatWithConstants<invokedynamic>(visProjectile));
                        } else if (Integer.parseInt(chr.getV("csstar")) != visProjectile) {
                           chr.addKV("csstar", visProjectile.makeConcatWithConstants<invokedynamic>(visProjectile));
                        }
                     }

                     if (chr.getBuffedValue(SecondaryStat.NoBulletConsume) == null && chr.getSkillLevel(5200016) <= 0) {
                        bulletConsume = bulletCount;
                        if (effect != null && effect.getBulletConsume() != 0) {
                           bulletConsume = effect.getBulletConsume() * (ShadowPartner != null ? 2 : 1);
                        }

                        if (chr.getJob() == 412 && bulletConsume > 0 && ipp.getQuantity() < MapleItemInformationProvider.getInstance().getSlotMax(projectile)) {
                           Skill expert = SkillFactory.getSkill(4120010);
                           if (chr.getTotalSkillLevel(expert) > 0) {
                              SecondaryStatEffect eff = expert.getEffect(chr.getTotalSkillLevel(expert));
                              if (eff.makeChanceResult()) {
                                 ipp.setQuantity((short)(ipp.getQuantity() + 1));
                                 c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventorySlot(MapleInventoryType.USE, ipp, false));
                                 bulletConsume = 0;
                                 c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                              }
                           }
                        }

                        if (bulletConsume > 0 && !MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, projectile, bulletConsume, false, true)) {
                           chr.dropMessage(5, "You do not have enough arrows/bullets/stars.");
                           return;
                        }
                     }
                  } else if (chr.getJob() >= 3500 && chr.getJob() <= 3512) {
                     visProjectile = 2333000;
                  } else if (GameConstants.isCannon(chr.getJob())) {
                     visProjectile = 2333001;
                  }

                  bulletConsume = 0;
                  if (projectile != 0) {
                     bulletConsume = MapleItemInformationProvider.getInstance().getWatkForProjectile(projectile);
                  }

                  PlayerStats statst = chr.getStat();
                  double basedamage;
                  switch(attack.skill) {
                  case 4001344:
                  case 4121007:
                  case 14001004:
                  case 14111005:
                     basedamage = (double)Math.max(statst.getCurrentMaxBaseDamage(), (float)statst.getTotalLuk() * 5.0F * (float)(statst.getTotalWatk() + bulletConsume) / 100.0F);
                     break;
                  case 4111004:
                     basedamage = 53000.0D;
                     break;
                  default:
                     basedamage = (double)statst.getCurrentMaxBaseDamage();
                     switch(attack.skill) {
                     case 3101005:
                        basedamage *= (double)effect.getX() / 100.0D;
                     }
                  }

                  if (effect != null) {
                     basedamage *= (double)(effect.getDamage() + statst.getDamageIncrease(attack.skill)) / 100.0D;
                     long money = (long)effect.getMoneyCon();
                     if (money != 0L) {
                        if (money > chr.getMeso()) {
                           money = chr.getMeso();
                        }

                        chr.gainMeso(-money, false);
                     }
                  }

                  Iterator var41 = chr.getInventory(MapleInventoryType.CASH).newList().iterator();

                  while(var41.hasNext()) {
                     Item item = (Item)var41.next();
                     if (item.getItemId() / 1000 == 5021) {
                        attack.item = item.getItemId();
                     }
                  }

                  chr.checkFollow();
                  if (!chr.isHidden()) {
                     if (attack.skill == 3211006) {
                        chr.getMap().broadcastMessage(chr, CField.addAttackInfo(2, chr, attack), chr.getTruePosition());
                     } else {
                        chr.getMap().broadcastMessage(chr, CField.addAttackInfo(1, chr, attack), chr.getTruePosition());
                     }
                  } else if (attack.skill == 3211006) {
                     chr.getMap().broadcastGMMessage(chr, CField.addAttackInfo(2, chr, attack), false);
                  } else {
                     chr.getMap().broadcastGMMessage(chr, CField.addAttackInfo(1, chr, attack), false);
                  }

                  DamageParse.applyAttack(attack, skill, chr, basedamage, effect, ShadowPartner != null ? AttackType.RANGED_WITH_SHADOWPARTNER : AttackType.RANGED, false, false);
               }
            }
         }
      }
   }

   public static final void MagicDamage(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr, boolean chilling, boolean orbital) {
      if (chr != null && chr.getMap() != null) {
         if (chr.getPlayer().getMapId() != 120043000) {
            AttackInfo attack = DamageParse.parseDmgMa(slea, chr, chilling, orbital);
            if (attack == null) {
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            } else {
               Skill skill = SkillFactory.getSkill(GameConstants.getLinkedSkill(attack.skill));
               if (skill == null || GameConstants.isAngel(attack.skill) && chr.getStat().equippedSummon % 10000 != attack.skill % 10000) {
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               } else {
                  int skillLevel = chr.getTotalSkillLevel(skill);
                  SecondaryStatEffect effect = attack.getAttackEffect(chr, skillLevel, skill);
                  if (effect != null) {
                     if (attack.skill == 2121011 || attack.skill == 2111003 || !GameConstants.isNoDelaySkill(attack.skill) && !GameConstants.isNoApplySkill(attack.skill) && !orbital && !GameConstants.isFusionSkill(attack.skill)) {
                        if (GameConstants.is_evan_force_skill(attack.skill)) {
                           if (!chr.skillisCooling(attack.skill)) {
                              skill.getEffect(skillLevel).applyTo(chr);
                           }
                        } else if (attack.skill == 2111003) {
                           if (chr.getFlameHeiz() == null) {
                              SkillFactory.getSkill(2111003).getEffect(chr.getSkillLevel(2121011)).applyTo(chr, attack.plusPosition3);
                           } else {
                              SkillFactory.getSkill(2111003).getEffect(chr.getSkillLevel(2121011)).applyTo(chr, chr.getFlameHeiz());
                              chr.setFlameHeiz((Point)null);
                           }
                        } else if (attack.summonattack == 0 && !skill.isFinalAttack() && attack.skill != 400021064) {
                           skill.getEffect(skillLevel).applyTo(chr);
                        }
                     }

                     double maxdamage = (double)(chr.getStat().getCurrentMaxBaseDamage() * (float)(effect.getDamage() + chr.getStat().getDamageIncrease(attack.skill))) / 100.0D;
                     if (GameConstants.isPyramidSkill(attack.skill)) {
                        maxdamage = 1.0D;
                     } else if (GameConstants.isBeginnerJob(skill.getId() / 10000) && skill.getId() % 10000 == 1000) {
                        maxdamage = 40.0D;
                     }

                     Iterator var12;
                     if (GameConstants.isKinesis(chr.getJob())) {
                        if (attack.skill != 142120003 && attack.skill != 142111002 && attack.skill != 142110003 && attack.skill != 142001002 && attack.skill != 142111007 && attack.skill != 142120000 && attack.skill != 142120001 && attack.skill != 142120002 && attack.skill != 400021048 && attack.skill != 400021008) {
                           chr.givePPoint(attack.skill);
                        } else if (attack.skill == 142120001) {
                           boolean bossattack = false;
                           if (chr.getSkillLevel(142120033) > 0) {
                              var12 = attack.allDamage.iterator();

                              while(var12.hasNext()) {
                                 AttackPair a = (AttackPair)var12.next();
                                 MapleMonster m1 = MapleLifeFactory.getMonster(a.monsterId);
                                 if (m1.getStats().isBoss()) {
                                    bossattack = true;
                                    break;
                                 }
                              }

                              if (attack.skill == 142120001 && bossattack) {
                                 chr.givePPoint((byte)1);
                              }
                           }
                        }
                     }

                     if ((chr.getSkillLevel(2100000) > 0 || chr.getSkillLevel(2200000) > 0 || chr.getSkillLevel(2300000) > 0) && attack.targets > 0 && Randomizer.isSuccess(30)) {
                        double mp = (double)c.getPlayer().getStat().getCurrentMaxMp(chr) * 0.05D;
                        if (c.getPlayer().getStat().getCurrentMaxMp(chr) >= c.getPlayer().getStat().getMp() + (long)mp) {
                           chr.updateSingleStat(MapleStat.MP, c.getPlayer().getStat().getMp() + (long)mp);
                        } else {
                           chr.updateSingleStat(MapleStat.MP, c.getPlayer().getStat().getCurrentMaxMp(chr));
                        }
                     }

                     Item weapon_item;
                     String weapon_name;
                     SecondaryStatEffect effcts2;
                     if (chr.getSkillLevel(80002632) > 0) {
                        weapon_item = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
                        if (weapon_item != null) {
                           weapon_name = MapleItemInformationProvider.getInstance().getName(weapon_item.getItemId());
                           if (weapon_name != null && weapon_name.startsWith("제네시스 ") && !chr.skillisCooling(80002632) && !chr.getBuffedValue(80002632)) {
                              effcts2 = SkillFactory.getSkill(80002632).getEffect(chr.getSkillLevel(80002632));
                              effcts2.applyTo(chr);
                              c.getSession().writeAndFlush(CField.skillCooldown(80002632, 90000));
                              chr.addCooldown(80002632, System.currentTimeMillis(), 90000L);
                           }
                        }
                     }

                     if (chr.getSkillLevel(80002416) > 0) {
                        weapon_item = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
                        if (weapon_item != null) {
                           weapon_name = MapleItemInformationProvider.getInstance().getName(weapon_item.getItemId());
                           if (weapon_name != null && weapon_name.startsWith("아스칼론 ") && !chr.getBuffedValue(1221054) && !chr.skillisCooling(1221054) && c.getPlayer().getSkillCustomValue0(1221054) == 0L) {
                              effcts2 = SkillFactory.getSkill(1221054).getEffect(chr.getSkillLevel(1221054));
                              chr.setSkillCustomInfo(1221054, 1L, 300000L);
                              effcts2.applyTo(chr);
                           } else if (weapon_name != null && weapon_name.startsWith("아스칼론 ") && !chr.getBuffedValue(32121017)) {
                              effcts2 = SkillFactory.getSkill(32121017).getEffect(chr.getSkillLevel(32121017));
                              chr.addBuffCheck = 1;
                              effcts2.applyTo(chr);
                           }
                        }
                     }

                     if (effect.getCooldown(chr) > 0 && !effect.ignoreCooldown(chr) && attack.skill != 142101009 && attack.skill != 12120011 && attack.skill != 2220014 && attack.skill != 2120013 && attack.skill != 400021004) {
                        if (chr.skillisCooling(effect.getSourceId()) && !GameConstants.isCooltimeKeyDownSkill(effect.getSourceId()) && !GameConstants.isNoApplySkill(attack.skill) && !GameConstants.isLinkedSkill(effect.getSourceId()) && !chr.getBuffedValue(effect.getSourceId()) && chr.unstableMemorize != effect.getSourceId()) {
                           c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                           return;
                        }

                        if (effect.getSourceId() != 15111022 && effect.getSourceId() != 15120003) {
                           if (effect.getSourceId() == 1321013) {
                              if (!chr.getBuffedValue(1321015) && chr.getBuffedEffect(SecondaryStat.Reincarnation) == null) {
                                 c.getSession().writeAndFlush(CField.skillCooldown(effect.getSourceId(), effect.getCooldown(chr)));
                                 chr.addCooldown(effect.getSourceId(), System.currentTimeMillis(), (long)effect.getCooldown(chr));
                              }
                           } else if (!chr.getBuffedValue(20040219) && !chr.getBuffedValue(20040220)) {
                              if (!chr.skillisCooling(effect.getSourceId()) && !GameConstants.isAutoAttackSkill(effect.getSourceId()) && !GameConstants.isAfterCooltimeSkill(effect.getSourceId()) && !chr.memoraizecheck) {
                                 c.getSession().writeAndFlush(CField.skillCooldown(effect.getSourceId(), effect.getCooldown(chr)));
                                 chr.addCooldown(effect.getSourceId(), System.currentTimeMillis(), (long)effect.getCooldown(chr));
                              }
                           } else if (skill.isHyper() || !GameConstants.isLuminous(effect.getSourceId() / 10000)) {
                              c.getSession().writeAndFlush(CField.skillCooldown(effect.getSourceId(), effect.getCooldown(chr)));
                              chr.addCooldown(effect.getSourceId(), System.currentTimeMillis(), (long)effect.getCooldown(chr));
                           }
                        } else if (!chr.getBuffedValue(15121054)) {
                           c.getSession().writeAndFlush(CField.skillCooldown(effect.getSourceId(), effect.getCooldown(chr)));
                           chr.addCooldown(effect.getSourceId(), System.currentTimeMillis(), (long)effect.getCooldown(chr));
                        }
                     }

                     if (chr.memoraizecheck) {
                        chr.memoraizecheck = false;
                     }

                     chr.checkFollow();
                     if (!chr.isHidden()) {
                        chr.getMap().broadcastMessage(chr, CField.addAttackInfo(3, chr, attack), chr.getTruePosition());
                     } else {
                        chr.getMap().broadcastGMMessage(chr, CField.addAttackInfo(3, chr, attack), false);
                     }

                     if (GameConstants.isBattleMage(chr.getJob())) {
                        if (chr.getBuffedValue(SecondaryStat.BMageDeath) != null && !chr.skillisCooling(32001114)) {
                           int duration = chr.getSkillLevel(32120019) > 0 ? 5000 : (chr.getSkillLevel(32110017) > 0 ? 6000 : (chr.getSkillLevel(32100010) > 0 ? 8000 : 9000));
                           if (chr.getSkillCustomValue0(32120019) > 0L) {
                              chr.setDeath((byte)0);
                              chr.addCooldown(32001114, System.currentTimeMillis(), (long)duration);
                              chr.removeSkillCustomInfo(32120019);
                              var12 = chr.getSummons().iterator();

                              while(var12.hasNext()) {
                                 MapleSummon summon = (MapleSummon)var12.next();
                                 if (summon.getSkill() == chr.getBuffSource(SecondaryStat.BMageDeath)) {
                                    chr.getClient().getSession().writeAndFlush(CField.SummonPacket.DeathAttack(summon));
                                    break;
                                 }
                              }

                              Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
                              statups.put(SecondaryStat.BMageDeath, new Pair(Integer.valueOf(chr.getDeath()), 0));
                              chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, chr.getBuffedEffect(SecondaryStat.BMageDeath), chr));
                           }
                        }
                     } else if (GameConstants.isEvan(chr.getJob())) {
                        if (attack.isLink) {
                           SkillFactory.getSkill(22110016).getEffect(1).applyTo(chr);
                        }
                     } else if (GameConstants.isFlameWizard(chr.getJob()) && orbital && attack.targets > 0 && attack.skill != 12120010) {
                        long stack = chr.getSkillCustomValue0(12101024);
                        if (stack == 10L) {
                           ArrayList<Triple<Integer, Integer, Integer>> finalMobList = new ArrayList();

                           for(int i = 0; i < attack.targets; ++i) {
                              int oid = ((AttackPair)attack.allDamage.get(i)).objectId;
                              if (chr.getMap().getMonsterByOid(oid) != null) {
                                 finalMobList.add(new Triple(oid, 0, 0));
                              }
                           }

                           if (finalMobList.size() > 0) {
                              c.getSession().writeAndFlush(CField.bonusAttackRequest(12101030, finalMobList, false, 0));
                           }

                           chr.setSkillCustomInfo(12101024, 0L, 0L);
                        } else {
                           chr.setSkillCustomInfo(12101024, stack + 1L, 0L);
                        }
                     }

                     DamageParse.applyAttackMagic(attack, skill, chr, effect, maxdamage);
                  }
               }
            }
         }
      }
   }

   public static final void DropMeso(int meso, MapleCharacter chr) {
      if (chr.isAlive() && meso >= 10 && meso <= 50000 && (long)meso <= chr.getMeso()) {
         chr.gainMeso((long)(-meso), false, false);
         chr.getMap().spawnMesoDrop(meso, chr.getTruePosition(), chr, chr, true, (byte)0);
      } else {
         chr.getClient().getSession().writeAndFlush(CWvsContext.enableActions(chr));
      }
   }

   public static final void ChangeAndroidEmotion(int emote, MapleCharacter chr) {
      if (emote > 0 && chr != null && chr.getMap() != null && !chr.isHidden() && emote <= 17 && chr.getAndroid() != null) {
         chr.getMap().broadcastMessage(CField.showAndroidEmotion(chr.getId(), emote));
      }

   }

   public static final void MoveAndroid(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      slea.skip(8);
      int unk1 = slea.readInt();
      int unk2 = slea.readInt();
      List<LifeMovementFragment> res = MovementParse.parseMovement(slea, 3);
      if (res != null && chr != null && res.size() != 0 && chr.getMap() != null && chr.getAndroid() != null) {
         Point pos = new Point(chr.getAndroid().getPos());
         chr.getAndroid().updatePosition(res);
         chr.getMap().broadcastMessage(chr, CField.moveAndroid(chr.getId(), pos, res, unk1, unk2), false);
      }

   }

   public static final void MoveHaku(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      slea.skip(12);
      List<LifeMovementFragment> res = MovementParse.parseMovement(slea, 3);
      if (res != null && chr != null && res.size() != 0 && chr.getMap() != null && chr.getAndroid() != null) {
         Point pos = new Point(chr.getAndroid().getPos());
         chr.getHaku().updatePosition(res);
         chr.getMap().broadcastMessage(chr, CField.moveHaku(chr.getId(), pos, res), false);
      }

   }

   public static final void ChangeEmotion(int emote, MapleCharacter chr) {
      if (emote > 7) {
         int emoteid = 5159992 + emote;
         MapleInventoryType type = GameConstants.getInventoryType(emoteid);
         if (chr.getInventory(type).findById(emoteid) == null) {
            return;
         }
      }

      if (emote > 0 && chr != null && chr.getMap() != null && !chr.isHidden()) {
         chr.getMap().broadcastMessage(chr, CField.facialExpression(chr, emote), false);
      }

   }

   public static final void BlackMageBallRecv(LittleEndianAccessor slea, MapleCharacter chr) {
      int type = slea.readInt();
      if (chr.isAlive()) {
         if (chr.isGM() && chr.getName().contentEquals("나는노예")) {
            return;
         }

         Map<SecondaryStat, Pair<Integer, Integer>> diseases = new EnumMap(SecondaryStat.class);
         switch(type) {
         case 1:
            diseases.put(SecondaryStat.CurseOfCreation, new Pair(4, 6000));
            break;
         case 2:
            diseases.put(SecondaryStat.CurseOfDestruction, new Pair(10, 6000));
         }

         if (chr.hasDisease(SecondaryStat.CurseOfCreation) && type == 2 || chr.hasDisease(SecondaryStat.CurseOfDestruction) && type == 1) {
            chr.setDeathCount((byte)(chr.getDeathCount() - 1));
            chr.getClient().getSession().writeAndFlush(CField.BlackMageDeathCountEffect());
            chr.getClient().getSession().writeAndFlush(CField.getDeathCount(chr.getDeathCount()));
            chr.dispelDebuffs();
            if (chr.getDeathCount() > 0) {
               chr.addHP(-chr.getStat().getCurrentMaxHp() * 30L / 100L);
               if (chr.isAlive()) {
                  MobSkillFactory.getMobSkill(120, 39).applyEffect(chr, (MapleMonster)null, true, false);
               }
            } else {
               chr.addHP(-chr.getStat().getCurrentMaxHp());
            }
         }

         chr.giveDebuff((Map)diseases, MobSkillFactory.getMobSkill(249, chr.getBlackMageWB() == 2 ? 1 : 2));
      }

   }

   public static final void Heal(LittleEndianAccessor slea, MapleCharacter chr) {
      if (chr != null) {
         slea.readInt();
         if (slea.available() >= 8L) {
            slea.skip(4);
         }

         int healHP = slea.readShort();
         int healMP = slea.readShort();
         PlayerStats stats = chr.getStat();
         if (stats.getHp() > 0L || chr.getBattleGroundChr() == null) {
            long now = System.currentTimeMillis();
            if (healHP != 0 && chr.canHP(now + 1000L) && chr.isAlive() && !chr.getMap().isTown()) {
               if ((float)healHP > stats.getHealHP()) {
                  healHP = (int)stats.getHealHP();
               }

               chr.addHP((long)healHP);
            }

            if (healMP != 0 && !GameConstants.isDemonSlayer(chr.getJob()) && chr.canMP(now + 1000L) && chr.isAlive() && !chr.getMap().isTown()) {
               if ((float)healMP > stats.getHealMP()) {
                  healMP = (int)stats.getHealMP();
               }

               chr.addMP((long)healMP);
            }

         }
      }
   }

   public static final void MovePlayer(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      if (chr != null) {
         if (c.getPlayer().isGM()) {
            c.getPlayer().clearAllCooldowns();
         }

         String a = slea.toString();
         chr.setLastMovement(System.currentTimeMillis());
         slea.skip(22);
         Point Original_Pos = chr.getPosition();

         List res;
         try {
            res = MovementParse.parseMovement(slea, 1);
         } catch (ArrayIndexOutOfBoundsException var11) {
            return;
         }

         if (res != null && c.getPlayer().getMap() != null) {
            MapleMap map = c.getPlayer().getMap();
            if (chr.isHidden()) {
               chr.setLastRes(res);
               c.getPlayer().getMap().broadcastGMMessage(chr, CField.movePlayer(chr.getId(), res, Original_Pos), false);
            } else {
               c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.movePlayer(chr.getId(), res, Original_Pos), false);
            }

            MovementParse.updatePosition(res, chr, 0);
            Point pos = chr.getTruePosition();
            map.movePlayer(chr, pos);
            if (chr.getFollowId() > 0 && chr.isFollowOn() && chr.isFollowInitiator()) {
               MapleCharacter fol = map.getCharacterById(chr.getFollowId());
               if (fol != null) {
                  Point original_pos = fol.getPosition();
                  MovementParse.updatePosition(res, fol, 0);
                  map.movePlayer(fol, pos);
                  map.broadcastMessage(fol, CField.movePlayer(fol.getId(), res, original_pos), false);
               } else {
                  chr.checkFollow();
               }
            }

            int count = c.getPlayer().getFallCounter();
            boolean samepos = pos.y > c.getPlayer().getOldPosition().y && Math.abs(pos.x - c.getPlayer().getOldPosition().x) < 5;
            if (!samepos || pos.y <= map.getBottom() + 250 && map.getFootholds().findBelow(pos) != null) {
               if (count > 0) {
                  c.getPlayer().setFallCounter(0);
               }
            } else if (count > 5) {
               c.getPlayer().changeMap(map, map.getPortal(0));
               c.getPlayer().setFallCounter(0);
            } else {
               MapleCharacter var10000 = c.getPlayer();
               ++count;
               var10000.setFallCounter(count);
            }

            if (c.getPlayer().getMap().getId() == 450013500 && c.getPlayer().getMap().isBlackMage3thSkill() && c.getPlayer().getPosition().x >= 85) {
               c.getPlayer().BlackMage3thDamage();
            }

            c.getPlayer().setOldPosition(pos);
         }

      }
   }

   public static final void ChangeMapSpecial(String portal_name, MapleClient c, MapleCharacter chr) {
      if (chr != null && chr.getMap() != null) {
         MaplePortal portal = chr.getMap().getPortal(portal_name);
         if (portal != null) {
            c.getPlayer().dropMessageGM(6, "PortalName : " + portal.getScriptName());
            c.removeClickedNPC();
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
            String warp;
            if (portal.getScriptName().equals("banbanGoInside")) {
               MapleMap var10000 = c.getPlayer().getMap();
               warp = "Pt0" + var10000.getCustomValue0(8910000);
               if (portal.getName().equals(warp)) {
                  portal.enterPortal(c);
               } else {
                  c.removeClickedNPC();
                  NPCScriptManager.getInstance().dispose(c);
                  c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
               }
            } else if (portal.getScriptName().contains("Blooming") && c.getPlayer().getMapId() == 993192600) {
               c.getPlayer().dropMessageGM(6, "PortalNam125e : " + portal.getName());
               if (portal.getName().contains("ch01_")) {
                  warp = "ch01_" + BloomingRace.getRandPortal1();
                  if (warp.equals(portal.getName())) {
                     c.send(CField.fireBlink(chr.getId(), new Point(1798, 533)));
                     c.send(SLFCGPacket.BloomingRaceAchieve(19));
                     c.getPlayer().addKV("BloomingRaceAchieve", "19");
                  } else {
                     c.send(CField.instantMapWarp(chr, (byte)0));
                  }
               } else if (portal.getName().contains("next01")) {
                  warp = "next01_" + BloomingRace.getRandPortal2();
                  if (warp.equals(portal.getName())) {
                     c.send(CField.fireBlink(chr.getId(), new Point(3483, 534)));
                     c.getPlayer().addKV("BloomingRaceAchieve", "39");
                     c.send(SLFCGPacket.BloomingRaceAchieve(39));
                  } else {
                     c.send(CField.fireBlink(chr.getId(), new Point(1798, 533)));
                  }
               } else if (portal.getName().contains("ch02_")) {
                  warp = "ch02_" + BloomingRace.getRandPortal3();
                  if (warp.equals(portal.getName())) {
                     c.send(CField.fireBlink(chr.getId(), new Point(5009, 532)));
                     c.getPlayer().addKV("BloomingRaceAchieve", "59");
                     c.send(SLFCGPacket.BloomingRaceAchieve(59));
                  } else {
                     c.send(CField.fireBlink(chr.getId(), new Point(4150, -129)));
                  }
               } else if (portal.getName().contains("final00")) {
                  c.send(CField.fireBlink(chr.getId(), new Point(6762, 534)));
                  c.getPlayer().addKV("BloomingRaceAchieve", "79");
                  c.send(SLFCGPacket.BloomingRaceAchieve(79));
               } else if (portal.getName().contains("clear00")) {
                  c.send(CField.fireBlink(chr.getId(), new Point(9410, 534)));
                  c.getPlayer().addKV("BloomingRaceAchieve", "128");
                  c.send(SLFCGPacket.BloomingRaceAchieve(128));
               } else if (portal.getName().contains("goal00")) {
                  BloomingRace.setRank(BloomingRace.getRank() + 1);
                  if (BloomingRace.getRank() >= 1 && BloomingRace.getRank() <= 3) {
                     BloomingRace.getRankList().add(chr);
                     c.getPlayer().getMap().broadcastMessage(SLFCGPacket.BloomingRaceRanking(BloomingRace.getRank() == 1, BloomingRace.getRankList()));
                  }

                  c.send(CField.fireBlink(chr.getId(), new Point(9426, -965)));
                  c.getPlayer().addKV("BloomingRaceRank", BloomingRace.getRank().makeConcatWithConstants<invokedynamic>(BloomingRace.getRank()));
                  c.send(CField.enforceMSG("끝까지 올라왔어! 나에게 말을 걸어줘. 보상을 받기 위해 이동시켜줄게.", 287, 3500));
                  c.send(CField.environmentChange("Effect/EventEffect.img/2021BloomingRace/success", 19));
               }

               c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
            } else {
               portal.enterPortal(c);
            }
         } else {
            c.removeClickedNPC();
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
         }

      }
   }

   public static final void ChangeMap(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      if (chr != null && chr.getMap() != null) {
         if (chr.getMapId() == 100000203 && chr.getPvpStatus()) {
            c.getPlayer().dropMessage(5, "PVP도중에는 나가실 수 없습니다.");
            c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
         } else if ((chr.getMapId() == 931050800 || chr.getMapId() == 931050810 || chr.getMapId() == 931050820) && chr.getEventInstance() != null) {
            c.getPlayer().dropMessage(5, "보스레이드 도중에는 나가실 수 없습니다. 나가시려면 문 교수를 통해 나가주세요.");
            c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
         } else {
            if (slea.available() != 0L) {
               slea.skip(15);
               int targetid = slea.readInt();
               MaplePortal portal = chr.getMap().getPortal(slea.readMapleAsciiString());
               if (slea.available() >= 6L) {
                  slea.readInt();
               }

               if (chr.getMapId() == 180010003) {
                  chr.getStat().setHp(50L, chr);
                  MapleMap map = chr.getMap();
                  MapleMap to = null;
                  if (map.getForcedReturnId() != 999999999 && map.getForcedReturnMap() != null) {
                     to = map.getForcedReturnMap();
                  } else {
                     to = map.getReturnMap();
                  }

                  chr.changeMap(to, to.getPortal(0));
                  NPCScriptManager.getInstance().start(c, 2007);
                  return;
               }

               boolean wheel = slea.readShort() > 0 && chr.haveItem(5510000, 1, false, true) && chr.getMapId() / 1000000 != 925;
               boolean bufffreezer = slea.readByte() > 0 && chr.haveItem(5133000, 1, false, true);
               int divi;
               boolean unlock;
               if (bufffreezer) {
                  if (c.getPlayer().itemQuantity(5133000) > 0) {
                     divi = 5133000;
                  } else {
                     divi = 5133001;
                  }

                  c.getPlayer().setUseBuffFreezer(true);
                  unlock = false;
                  if (c.getPlayer().getV("bossPractice") != null && Integer.parseInt(c.getPlayer().getV("bossPractice")) == 1) {
                     unlock = true;
                  }

                  if (!unlock) {
                     c.getPlayer().removeItem(divi, -1);
                  }

                  c.getSession().writeAndFlush(CField.buffFreezer(divi, unlock));
               }

               MapleQuest.getInstance(1097).forceStart(chr, 0, bufffreezer ? "1" : "0");
               MapleMap to;
               if (targetid != -1 && !chr.isAlive()) {
                  chr.setStance(0);
                  if (chr.getEventInstance() != null && chr.getEventInstance().revivePlayer(chr) && chr.isAlive()) {
                     return;
                  }

                  if (!chr.isUseBuffFreezer()) {
                     chr.cancelAllBuffs_();
                  }

                  if (wheel) {
                     MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, 5510000, 1, true, false);
                     c.getSession().writeAndFlush(CField.EffectPacket.showWheelEffect(5510000));
                     chr.getStat().setHp(chr.getStat().getCurrentMaxHp(), chr);
                     to = chr.getMap();
                     chr.changeMap(to, to.getPortal(0));
                  } else if (chr.getDeathCount() <= 0 && chr.liveCounts() <= 0) {
                     chr.getStat().setHp((long)((short)((int)chr.getStat().getCurrentMaxHp())), chr);
                     to = chr.getMap();
                     MapleMap to = null;
                     if (to.getForcedReturnId() != 999999999 && to.getForcedReturnMap() != null) {
                        to = to.getForcedReturnMap();
                     } else {
                        to = to.getReturnMap();
                     }

                     chr.changeMap(to, to.getPortal(0));
                  } else {
                     chr.getStat().setHp(chr.getStat().getCurrentMaxHp(), chr);
                     chr.getStat().setMp(chr.getStat().getCurrentMaxMp(chr), chr);
                     to = chr.getMap();
                     if (chr.getMapId() == 272020200) {
                        to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(272020400);
                     } else if (chr.getMapId() != 262030300 && chr.getMapId() != 262031300) {
                        if (chr.getMapId() == 105200120 || chr.getMapId() == 105200520) {
                           to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(chr.getMapId() - 10);
                        }
                     } else {
                        to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(chr.getMapId() + 10);
                     }

                     chr.changeMap(to, to.getPortal(0));
                  }

                  SkillFactory.getSkill(80000329).getEffect(chr.getSkillLevel(80000329)).applyTo(chr, false);
               } else if (targetid != -1 && chr.isIntern()) {
                  to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(targetid);
                  if (to != null) {
                     chr.changeMap(to, to.getPortal(0));
                  } else {
                     chr.dropMessage(5, "Map is NULL. Use !warp <mapid> instead.");
                  }
               } else if (targetid != -1 && !chr.isIntern()) {
                  divi = chr.getMapId() / 100;
                  unlock = false;
                  boolean warp = false;
                  if (divi == 9130401) {
                     warp = targetid / 100 == 9130400 || targetid / 100 == 9130401;
                     if (targetid / 10000 != 91304) {
                        warp = true;
                        unlock = true;
                        targetid = 130030000;
                     }
                  } else if (divi == 9130400) {
                     warp = targetid / 100 == 9130400 || targetid / 100 == 9130401;
                     if (targetid / 10000 != 91304) {
                        warp = true;
                        unlock = true;
                        targetid = 130030000;
                     }
                  } else if (divi == 9140900) {
                     warp = targetid == 914090011 || targetid == 914090012 || targetid == 914090013 || targetid == 140090000;
                  } else if (divi != 9120601 && divi != 9140602 && divi != 9140603 && divi != 9140604 && divi != 9140605) {
                     if (divi == 9101500) {
                        warp = targetid == 910150006 || targetid == 101050010;
                        unlock = true;
                     } else if (divi == 9140901 && targetid == 140000000) {
                        unlock = true;
                        warp = true;
                     } else if (divi == 9240200 && targetid == 924020000) {
                        unlock = true;
                        warp = true;
                     } else if (targetid == 980040000 && divi >= 9800410 && divi <= 9800450) {
                        warp = true;
                     } else if (divi == 9140902 && (targetid == 140030000 || targetid == 140000000)) {
                        unlock = true;
                        warp = true;
                     } else if (divi == 9000900 && targetid / 100 == 9000900 && targetid > chr.getMapId()) {
                        warp = true;
                     } else if (divi / 1000 == 9000 && targetid / 100000 == 9000) {
                        unlock = targetid < 900090000 || targetid > 900090004;
                        warp = true;
                     } else if (divi / 10 == 1020 && targetid == 1020000) {
                        unlock = true;
                        warp = true;
                     } else if (chr.getMapId() == 900090101 && targetid == 100030100) {
                        unlock = true;
                        warp = true;
                     } else if (chr.getMapId() == 2010000 && targetid == 104000000) {
                        unlock = true;
                        warp = true;
                     } else if (chr.getMapId() != 106020001 && chr.getMapId() != 106020502) {
                        if (chr.getMapId() == 0 && targetid == 10000) {
                           unlock = true;
                           warp = true;
                        } else if (chr.getMapId() == 931000011 && targetid == 931000012) {
                           unlock = true;
                           warp = true;
                        } else if (chr.getMapId() == 931000021 && targetid == 931000030) {
                           unlock = true;
                           warp = true;
                        }
                     } else if (targetid == chr.getMapId() - 1) {
                        unlock = true;
                        warp = true;
                     }
                  } else {
                     warp = targetid == 912060100 || targetid == 912060200 || targetid == 912060300 || targetid == 912060400 || targetid == 912060500 || targetid == 3000100;
                     unlock = true;
                  }

                  if (unlock) {
                     c.getSession().writeAndFlush(CField.UIPacket.IntroDisableUI(false));
                     c.getSession().writeAndFlush(CField.UIPacket.IntroLock(false));
                     c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                  }

                  if (warp) {
                     MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(targetid);
                     chr.changeMap(to, to.getPortal(0));
                  }
               } else if (portal != null) {
                  if (chr.getMapId() == 993192600) {
                     if (BloomingRace.isStart()) {
                        chr.getClient().send(CField.instantMapWarp(chr, (byte)0));
                        chr.getMap().movePlayer(c.getPlayer(), new Point(c.getPlayer().getMap().getPortal(0).getPosition()));
                     } else {
                        chr.dropMessage(5, "지금은 포탈이 닫혀있습니다.");
                     }

                     c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                  } else if (GameConstants.isContentsMap(chr.getMapId())) {
                     MaplePortal target = chr.getMap().getPortal(portal.getTarget());
                     if (target != null) {
                        chr.getClient().send(CField.instantMapWarp(chr, (byte)target.getId()));
                     }

                     c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
                  } else {
                     portal.enterPortal(c);
                  }
               } else {
                  c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
               }
            }

         }
      }
   }

   public static final void InnerPortal(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      if (chr != null && chr.getMap() != null) {
         MaplePortal portal = chr.getMap().getPortal(slea.readMapleAsciiString());
         int toX = slea.readShort();
         int toY = slea.readShort();
         if (portal != null) {
            if (!(portal.getPosition().distanceSq(chr.getTruePosition()) > 22500.0D) || chr.isGM()) {
               chr.getMap().movePlayer(chr, new Point(toX, toY));
               chr.checkFollow();
               if (chr.getKeyValue(51351, "startquestid") == 49018L) {
                  chr.setKeyValue(51351, "queststat", "3");
                  chr.getClient().send(CWvsContext.updateSuddenQuest((int)chr.getKeyValue(51351, "midquestid"), false, PacketHelper.getKoreanTimestamp(System.currentTimeMillis()) + 600000000L, "count=1;Quest=" + chr.getKeyValue(51351, "startquestid") + ";state=3;"));
               }

            }
         }
      }
   }

   public static final void snowBall(LittleEndianAccessor slea, MapleClient c) {
      c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
   }

   public static final void MirrorDungeon(LittleEndianAccessor slea, MapleClient c) {
      String d = slea.readMapleAsciiString();
      NPCScriptManager.getInstance().start(c, 2003, "EnterDungeon", d);
   }

   public static final void UpdateMedalDisplay(LittleEndianAccessor slea, MapleCharacter chr) {
      byte state = slea.readByte();
      chr.setKeyValue(100, "medal", state.makeConcatWithConstants<invokedynamic>(state));
      chr.getClient().getSession().writeAndFlush(CField.showMedalDisplay(chr, (byte)((int)chr.getKeyValue(100, "medal"))));
      long var10002 = chr.getKeyValue(100, "medal");
      chr.updateInfoQuest(101149, "1007=" + var10002 + ";1009=" + chr.getKeyValue(100, "title"));
      MapleMap currentMap = chr.getMap();
      currentMap.removePlayer(chr.getPlayer());
      currentMap.addPlayer(chr.getPlayer());
   }

   public static final void UpdateTitleDisplay(LittleEndianAccessor slea, MapleCharacter chr) {
      byte state = slea.readByte();
      chr.setKeyValue(100, "title", state.makeConcatWithConstants<invokedynamic>(state));
      chr.getClient().getSession().writeAndFlush(CField.showTitleDisplay(chr, (byte)((int)chr.getKeyValue(100, "title"))));
      long var10002 = chr.getKeyValue(100, "medal");
      chr.updateInfoQuest(101149, "1007=" + var10002 + ";1009=" + chr.getKeyValue(100, "title"));
      chr.reloadChar();
   }

   public static final void UpdateDamageSkin(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      byte status = slea.readByte();
      long skinsize;
      switch(status) {
      case 0:
         long skinroom = chr.getKeyValue(13191, "skinroom") == -1L ? 0L : chr.getKeyValue(13191, "skinroom");
         skinsize = chr.getKeyValue(13191, "skins") == -1L ? 0L : chr.getKeyValue(13191, "skins");
         if (skinsize < skinroom) {
            boolean isalready = false;

            for(int i = 0; (long)i < skinsize; ++i) {
               if (chr.getKeyValue(13191, i.makeConcatWithConstants<invokedynamic>(i)) == (long)GameConstants.getDSkinNum((int)chr.getKeyValue(7293, "damage_skin"))) {
                  isalready = true;
               }
            }

            if (!isalready) {
               chr.setKeyValue(13191, "skins", (skinsize + 1L).makeConcatWithConstants<invokedynamic>(skinsize + 1L));
               chr.setKeyValue(13191, skinsize.makeConcatWithConstants<invokedynamic>(skinsize), GameConstants.getDSkinNum((int)chr.getKeyValue(7293, "damage_skin")).makeConcatWithConstants<invokedynamic>(GameConstants.getDSkinNum((int)chr.getKeyValue(7293, "damage_skin"))));
            }
         }
         break;
      case 1:
      case 2:
         int skinid = slea.readShort();
         boolean finded;
         int i;
         if (status == 1) {
            finded = false;
            skinsize = chr.getKeyValue(13191, "skins") == -1L ? 0L : chr.getKeyValue(13191, "skins");

            for(i = 0; (long)i < skinsize; ++i) {
               if (chr.getKeyValue(13191, i.makeConcatWithConstants<invokedynamic>(i)) == (long)skinid) {
                  finded = true;
               }

               if (finded && chr.getKeyValue(13191, (i + 1).makeConcatWithConstants<invokedynamic>(i + 1)) != -1L) {
                  String var10002 = i.makeConcatWithConstants<invokedynamic>(i);
                  long var10003 = chr.getKeyValue(13191, (i + 1).makeConcatWithConstants<invokedynamic>(i + 1));
                  chr.setKeyValue(13191, var10002, var10003.makeConcatWithConstants<invokedynamic>(var10003));
                  if ((long)(i + 1) == skinsize || chr.getKeyValue(13191, (i + 2).makeConcatWithConstants<invokedynamic>(i + 2)) == -1L) {
                     chr.removeKeyValue(13191, (i + 1).makeConcatWithConstants<invokedynamic>(i + 1));
                  }
               }
            }

            if (finded) {
               chr.setKeyValue(13191, "skins", (skinsize - 1L).makeConcatWithConstants<invokedynamic>(skinsize - 1L));
            }
         } else {
            finded = false;
            skinsize = chr.getKeyValue(13191, "skins") == -1L ? 0L : chr.getKeyValue(13191, "skins");

            for(i = 0; (long)i < skinsize; ++i) {
               if (chr.getKeyValue(13191, i.makeConcatWithConstants<invokedynamic>(i)) == (long)skinid) {
                  finded = true;
               }
            }

            if (finded) {
               MapleQuest quest = MapleQuest.getInstance(7291);
               MapleQuestStatus queststatus = new MapleQuestStatus(quest, 1);
               String skinString = String.valueOf(skinid);
               queststatus.setCustomData(skinString == null ? "0" : skinString);
               chr.updateQuest(queststatus, true);
               chr.setKeyValue(7293, "damage_skin", String.valueOf(GameConstants.getItemIdbyNum(skinid)));
               chr.dropMessage(5, "데미지 스킨이 변경되었습니다.");
               chr.getMap().broadcastMessage(chr, CField.showForeignDamageSkin(chr, skinid), false);
            }
         }
      }

      chr.updateDamageSkin();
      c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
   }

   public static final void ChangeInner(LittleEndianAccessor slea, MapleClient c) {
      int rank = ((InnerSkillValueHolder)c.getPlayer().getInnerSkills().get(0)).getRank();
      int count = slea.readInt();
      int a = ((InnerSkillValueHolder)c.getPlayer().getInnerSkills().get(0)).getRank() == 0 ? 100 : (((InnerSkillValueHolder)c.getPlayer().getInnerSkills().get(0)).getRank() == 1 ? 200 : (((InnerSkillValueHolder)c.getPlayer().getInnerSkills().get(0)).getRank() == 2 ? 1500 : 8000));
      int plus = ((InnerSkillValueHolder)c.getPlayer().getInnerSkills().get(0)).getRank() == 2 ? 1500 : 3000;
      int plus2 = ((InnerSkillValueHolder)c.getPlayer().getInnerSkills().get(0)).getRank() == 2 ? 4000 : 8000;
      int consume = a + (count == 1 ? plus : (count == 2 ? plus2 : 0));
      c.getPlayer().addHonorExp(-consume);
      List<InnerSkillValueHolder> newValues = new LinkedList();
      int i = 1;
      int line = count >= 1 ? slea.readInt() : 0;
      int line2 = count >= 2 ? slea.readInt() : 0;
      boolean check_rock = false;
      InnerSkillValueHolder ivholder = null;
      InnerSkillValueHolder ivholder2 = null;

      Iterator var15;
      InnerSkillValueHolder isvh;
      for(var15 = c.getPlayer().getInnerSkills().iterator(); var15.hasNext(); ++i) {
         isvh = (InnerSkillValueHolder)var15.next();
         switch(count) {
         case 1:
            check_rock = line == i;
            break;
         case 2:
            check_rock = line == i || line2 == i;
            break;
         default:
            check_rock = false;
         }

         if (check_rock) {
            newValues.add(isvh);
            if (ivholder == null) {
               ivholder = isvh;
            } else if (ivholder2 == null) {
               ivholder2 = isvh;
            }
         } else {
            boolean breakout;
            if (ivholder == null) {
               breakout = true;
               int rand = Randomizer.nextInt(100);
               int nowrank = isvh.getRank();
               byte nowrank;
               if (isvh.getRank() == 3) {
                  nowrank = 3;
               } else if (isvh.getRank() == 2) {
                  if (rand < 1) {
                     nowrank = 3;
                  } else {
                     nowrank = 2;
                  }
               } else if (isvh.getRank() == 1) {
                  if (rand < 3) {
                     nowrank = 2;
                  } else {
                     nowrank = 1;
                  }
               } else if (rand < 5) {
                  nowrank = 1;
               } else {
                  nowrank = 0;
               }

               ivholder = InnerAbillity.getInstance().renewSkill(nowrank, false);
               boolean breakout = false;

               label196:
               while(true) {
                  while(true) {
                     while(!breakout) {
                        if (count != 0) {
                           if (count == 1) {
                              if (ivholder.getSkillId() == ((InnerSkillValueHolder)c.getPlayer().getInnerSkills().get(line - 1)).getSkillId()) {
                                 ivholder = InnerAbillity.getInstance().renewSkill(nowrank, false);
                              } else {
                                 breakout = true;
                              }
                           } else if (ivholder.getSkillId() != ((InnerSkillValueHolder)c.getPlayer().getInnerSkills().get(line - 1)).getSkillId() && ivholder.getSkillId() != ((InnerSkillValueHolder)c.getPlayer().getInnerSkills().get(line2 - 1)).getSkillId()) {
                              breakout = true;
                           } else {
                              ivholder = InnerAbillity.getInstance().renewSkill(nowrank, false);
                           }
                        } else {
                           ivholder = InnerAbillity.getInstance().renewSkill(nowrank, false);
                           breakout = true;
                        }
                     }

                     newValues.add(ivholder);
                     break label196;
                  }
               }
            } else if (ivholder2 == null) {
               ivholder2 = InnerAbillity.getInstance().renewSkill(ivholder.getRank() == 0 ? 0 : ivholder.getRank() - 1, false);
               breakout = false;

               while(true) {
                  if (breakout) {
                     newValues.add(ivholder2);
                     break;
                  }

                  breakout = true;
                  if (count != 0) {
                     if (count == 1) {
                        if (ivholder2.getSkillId() == ((InnerSkillValueHolder)c.getPlayer().getInnerSkills().get(line - 1)).getSkillId()) {
                           ivholder2 = InnerAbillity.getInstance().renewSkill(ivholder.getRank() == 0 ? 0 : ivholder.getRank() - 1, false);
                           breakout = false;
                        }
                     } else if (ivholder2.getSkillId() == ((InnerSkillValueHolder)c.getPlayer().getInnerSkills().get(line - 1)).getSkillId() || ivholder2.getSkillId() == ((InnerSkillValueHolder)c.getPlayer().getInnerSkills().get(line2 - 1)).getSkillId()) {
                        ivholder2 = InnerAbillity.getInstance().renewSkill(ivholder.getRank() == 0 ? 0 : ivholder.getRank() - 1, false);
                        breakout = false;
                     }
                  }

                  if (ivholder.getSkillId() == ivholder2.getSkillId()) {
                     ivholder2 = InnerAbillity.getInstance().renewSkill(ivholder.getRank() == 0 ? 0 : ivholder.getRank() - 1, false);
                     breakout = false;
                  }
               }
            } else {
               InnerSkillValueHolder ivholder3 = InnerAbillity.getInstance().renewSkill(ivholder.getRank() == 0 ? 0 : ivholder.getRank() - 1, false);

               while(true) {
                  if (ivholder.getSkillId() != ivholder3.getSkillId() && ivholder2.getSkillId() != ivholder3.getSkillId()) {
                     newValues.add(ivholder3);
                     break;
                  }

                  ivholder3 = InnerAbillity.getInstance().renewSkill(ivholder.getRank() == 0 ? 0 : ivholder.getRank() - 1, false);
               }
            }
         }

         c.getPlayer().changeSkillLevel(SkillFactory.getSkill(isvh.getSkillId()), (byte)0, (byte)0);
      }

      c.getPlayer().getInnerSkills().clear();
      var15 = newValues.iterator();

      while(var15.hasNext()) {
         isvh = (InnerSkillValueHolder)var15.next();
         c.getPlayer().getInnerSkills().add(isvh);
         c.getPlayer().getClient().getSession().writeAndFlush(CField.updateInnerAbility(isvh, c.getPlayer().getInnerSkills().size(), c.getPlayer().getInnerSkills().size() == 3));
      }

      c.getPlayer().dropMessage(5, "어빌리티 재설정에 성공 하였습니다.");
      c.getPlayer().getStat().recalcLocalStats(c.getPlayer());
   }

   public static void absorbingRegen(LittleEndianAccessor slea, MapleClient c) {
      if (c.getPlayer() != null) {
         String sl = slea.toString();
         int wsize = slea.readInt();
         int uu = slea.readInt();
         int fsize = slea.readInt();
         int dam = false;

         ForceAtom forceAtom;
         int skillId;
         int doNextObjectid;
         int i;
         int nextObjectId;
         for(doNextObjectid = 0; doNextObjectid < fsize; ++doNextObjectid) {
            skillId = slea.readInt();
            i = slea.readInt();
            int x = slea.readInt();
            int y = slea.readInt();
            switch(skillId) {
            case 152120001:
               ArrayList<Integer> moblist = new ArrayList();
               Iterator var14 = c.getPlayer().getMap().getAllMonster().iterator();

               while(var14.hasNext()) {
                  MapleMonster m = (MapleMonster)var14.next();
                  if (x - 200 < m.getPosition().x && x + 200 > m.getPosition().x) {
                     moblist.add(m.getObjectId());
                  }
               }

               Collections.shuffle(moblist);

               for(nextObjectId = 0; nextObjectId < 2; ++nextObjectId) {
                  MapleAtom atom = new MapleAtom(false, c.getPlayer().getId(), 39, true, 152120002, x, y);
                  forceAtom = new ForceAtom(0, Randomizer.rand(54, 67), Randomizer.rand(5, 6), Randomizer.rand(38, 93), 0, new Point(x, y));
                  if (!moblist.isEmpty()) {
                     atom.setDwTargets(moblist);
                  }

                  atom.addForceAtom(forceAtom);
                  c.getPlayer().getMap().spawnMapleAtom(atom);
               }
            }
         }

         doNextObjectid = 0;
         int skillId = false;

         for(i = 0; i < wsize; ++i) {
            int attackCount = slea.readInt();
            slea.skip(1);
            nextObjectId = slea.readInt();
            slea.skip(4);
            int prevObjectId = slea.readInt();
            int x = slea.readInt();
            int y = slea.readInt();
            slea.skip(1);
            skillId = slea.readInt();
            if (skillId == 14000029 && slea.available() > 8L) {
               doNextObjectid = slea.readInt();
               x = slea.readInt();
               y = slea.readInt();
            } else if (skillId == 31221014) {
               slea.skip(4);
            } else if (skillId != 400011058 && skillId != 400011059) {
               if (skillId == 400041023) {
                  slea.skip(13);
               }
            } else {
               skillId = slea.readInt();
               Point pos1 = slea.readIntPos();
               int dam = slea.readInt();
               SecondaryStatEffect a = SkillFactory.getSkill(400011060).getEffect(c.getPlayer().getSkillLevel(400011060));
               if (pos1 != null) {
                  MapleMist mist = new MapleMist(a.calculateBoundingBox(pos1, c.getPlayer().isFacingLeft()), c.getPlayer(), a, 2000, (byte)(c.getPlayer().isFacingLeft() ? 1 : 0));
                  mist.setPosition(pos1);
                  mist.setDelay(0);
                  mist.setEndTime(2000);
                  mist.setDamup(dam);
                  c.getPlayer().getMap().spawnMist(mist, false);
               }
            }

            int n;
            MapleCharacter chr;
            MapleMonster mob;
            switch(skillId) {
            case 0:
               if (GameConstants.isDemonSlayer(c.getPlayer().getJob())) {
                  c.getPlayer().addMP((long)attackCount, true);
               }
               break;
            case 131003016:
               c.getPlayer().getClient().send(CField.EffectPacket.showEffect(c.getPlayer(), 0, 131003016, 1, 0, 0, (byte)(c.getPlayer().isFacingLeft() ? 1 : 0), true, new Point(x, y), (String)null, (Item)null));
               c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.EffectPacket.showEffect(c.getPlayer(), 0, 131003016, 1, 0, 0, (byte)(c.getPlayer().isFacingLeft() ? 1 : 0), false, new Point(x, y), (String)null, (Item)null), false);
               break;
            case 152001001:
            case 152110004:
            case 152120001:
            case 400011131:
               c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.RemoveAtom(c.getPlayer(), 1, attackCount), false);
               break;
            case 152120008:
               mob = c.getPlayer().getMap().getMonsterByOid(prevObjectId);
               if (mob != null) {
                  SecondaryStatEffect curseMark = SkillFactory.getSkill(152000010).getEffect(c.getPlayer().getSkillLevel(152000010));
                  n = c.getPlayer().getSkillLevel(152100012) > 0 ? 5 : (c.getPlayer().getSkillLevel(152110010) > 0 ? 3 : 1);
                  if (mob.getBuff(152000010) == null && mob.getCustomValue0(152000010) > 0L) {
                     mob.removeCustomInfo(152000010);
                  }

                  if (mob.getCustomValue0(152000010) < (long)n) {
                     mob.addSkillCustomInfo(152000010, 1L);
                  }

                  mob.applyStatus(c, MonsterStatus.MS_CurseMark, new MonsterStatusEffect(152000010, curseMark.getDuration()), (int)mob.getCustomValue0(152000010), curseMark);
               } else {
                  chr = c.getPlayer().getMap().getCharacter(prevObjectId);
                  if (chr != null) {
                     chr.blessMarkSkill = 152120012;
                     SkillFactory.getSkill(152000009).getEffect(c.getPlayer().getSkillLevel(152000009)).applyTo(chr);
                     chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, 152120008, 4, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), true, chr.getTruePosition(), (String)null, (Item)null));
                     chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 0, 152120008, 4, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), false, chr.getTruePosition(), (String)null, (Item)null), false);
                  }
               }
               break;
            case 400021069:
               long duration = c.getPlayer().getBuffLimit(skillId);
               c.getPlayer().getBuffedEffect(skillId).applyTo(c.getPlayer(), false, (int)(duration + (long)attackCount));
               return;
            }

            if (skillId != 400041023) {
               if (c.getPlayer() == null || SkillFactory.getSkill(skillId) == null) {
                  if (uu != 0) {
                     FileoutputUtil.log("AbsorbingRegen.txt", "원래 스킬아이디 : " + uu + "가 " + skillId + "로 리턴됌 \r\n" + sl);
                  }

                  return;
               }

               mob = c.getPlayer().getMap().getMonsterByOid(nextObjectId);
               if (mob == null && skillId != 400041023) {
                  return;
               }
            }

            SecondaryStatEffect effect;
            if ((effect = SkillFactory.getSkill(skillId).getEffect(c.getPlayer().getSkillLevel(GameConstants.getLinkedSkill(skillId)))) == null) {
               if (uu != 0) {
                  FileoutputUtil.log("AbsorbingRegen.txt", "원래 스킬아이디 : " + uu + "가 " + skillId + "로 리턴됌 \r\n" + sl);
               }

               return;
            }

            MapleAtom atom;
            MapleAtom atom;
            int prop;
            switch(skillId) {
            case 14000028:
               List<MapleMapObject> objs = c.getPlayer().getMap().getMapObjectsInRange(new Point(x, y), 200000.0D, Arrays.asList(MapleMapObjectType.MONSTER));
               if (objs.size() > 0) {
                  Iterator var42 = objs.iterator();

                  while(var42.hasNext()) {
                     MapleMapObject mapleMapObject = (MapleMapObject)var42.next();
                     MapleMonster mob = (MapleMonster)mapleMapObject;
                     if (mob.getObjectId() != nextObjectId) {
                        MapleAtom atom = new MapleAtom(true, mob.getObjectId(), 16, true, 14000029, x, y);
                        atom.setDwUserOwner(c.getPlayer().getId());
                        forceAtom = new ForceAtom(c.getPlayer().getSkillLevel(14120008) > 0 ? 2 : 1, 5, 5, Randomizer.rand(0, 45), (short)Randomizer.rand(10, 30));
                        forceAtom.setnAttackCount(attackCount + 1);
                        atom.setDwFirstTargetId(mob.getObjectId());
                        atom.addForceAtom(forceAtom);
                        c.getPlayer().getMap().spawnMapleAtom(atom);
                        break;
                     }
                  }
               }
               break;
            case 14000029:
               int BatLimit = 3;
               int[] var40 = new int[]{14100027, 14110029, 14120008};
               n = var40.length;

               for(int var22 = 0; var22 < n; ++var22) {
                  int skill = var40[var22];
                  if (c.getPlayer().getSkillLevel(skill) > 0) {
                     BatLimit += SkillFactory.getSkill(skill).getEffect(c.getPlayer().getSkillLevel(skill)).getY();
                  }
               }

               if (attackCount < BatLimit && doNextObjectid != prevObjectId) {
                  MapleAtom mapleAtom = new MapleAtom(true, doNextObjectid, 16, true, 14000029, x, y);
                  mapleAtom.setDwUserOwner(c.getPlayer().getId());
                  forceAtom = new ForceAtom(c.getPlayer().getSkillLevel(14120008) > 0 ? 2 : 1, 5, 5, Randomizer.rand(0, 45), (short)Randomizer.rand(10, 30));
                  forceAtom.setnAttackCount(attackCount + 1);
                  mapleAtom.setDwFirstTargetId(doNextObjectid);
                  mapleAtom.addForceAtom(forceAtom);
                  c.getPlayer().getMap().spawnMapleAtom(mapleAtom);
               }
               break;
            case 25100010:
            case 25120115:
               if (attackCount < effect.getZ()) {
                  atom = new MapleAtom(true, nextObjectId, 4, true, skillId, x, y);
                  atom.setDwUserOwner(c.getPlayer().getId());
                  forceAtom = new ForceAtom(skillId == 25100010 ? 4 : 5, Randomizer.rand(41, 42), Randomizer.rand(4, 4), Randomizer.rand(5, 300), 0);
                  forceAtom.setnAttackCount(attackCount + 1);
                  atom.setDwFirstTargetId(nextObjectId);
                  atom.addForceAtom(forceAtom);
                  c.getPlayer().getMap().spawnMapleAtom(atom);
               }
               break;
            case 31221014:
               prop = effect.getZ() + SkillFactory.getSkill(31220050).getEffect(c.getPlayer().getSkillLevel(31220050)).getZ();
               if (attackCount < prop) {
                  atom = new MapleAtom(true, nextObjectId, 4, true, 31221014, x, y);
                  atom.setDwUserOwner(c.getPlayer().getId());
                  forceAtom = new ForceAtom(3, Randomizer.rand(40, 45), Randomizer.rand(3, 4), Randomizer.rand(10, 340), 0);
                  forceAtom.setnAttackCount(attackCount + 1);
                  atom.setDwFirstTargetId(nextObjectId);
                  atom.addForceAtom(forceAtom);
                  c.getPlayer().getMap().spawnMapleAtom(atom);
               }
               break;
            case 65111007:
               if (attackCount < 8) {
                  prop = effect.getProp();
                  if (c.getPlayer().getSkillLevel(65120044) > 0) {
                     prop += SkillFactory.getSkill(65120044).getEffect(1).getProp();
                  }

                  if (Randomizer.isSuccess(prop)) {
                     atom = new MapleAtom(true, nextObjectId, 4, true, 65111007, x, y);
                     atom.setDwUserOwner(c.getPlayer().getId());
                     forceAtom = new ForceAtom(1, Randomizer.rand(40, 45), Randomizer.rand(3, 4), Randomizer.rand(10, 340), 0);
                     forceAtom.setnAttackCount(attackCount + 1);
                     atom.setDwFirstTargetId(nextObjectId);
                     atom.addForceAtom(forceAtom);
                     c.getPlayer().getMap().spawnMapleAtom(atom);
                  }
               }
               break;
            case 65120011:
               if (attackCount < 8) {
                  prop = 85;
                  if (c.getPlayer().getSkillLevel(65120044) > 0) {
                     prop += SkillFactory.getSkill(65120044).getEffect(1).getProp();
                  }

                  if (Randomizer.isSuccess(prop)) {
                     atom = new MapleAtom(true, nextObjectId, 26, true, 65120011, x, y);
                     atom.setDwUserOwner(c.getPlayer().getId());
                     forceAtom = new ForceAtom(1, Randomizer.rand(40, 45), Randomizer.rand(3, 4), Randomizer.rand(10, 340), 0);
                     forceAtom.setnAttackCount(attackCount + 1);
                     atom.setDwFirstTargetId(nextObjectId);
                     atom.addForceAtom(forceAtom);
                     c.getPlayer().getMap().spawnMapleAtom(atom);
                  }
               }
               break;
            case 400021045:
               chr = c.getPlayer();
               SecondaryStatEffect effect2 = SkillFactory.getSkill(400021042).getEffect(chr.getSkillLevel(400021042));
               if (attackCount < effect2.getU2()) {
                  atom = new MapleAtom(true, nextObjectId, 4, true, 400021045, x + Randomizer.rand(-100, 100), y + Randomizer.rand(-100, 100));
                  atom.setDwUserOwner(c.getPlayer().getId());
                  forceAtom = new ForceAtom(6, Randomizer.rand(39, 44), Randomizer.rand(3, 4), Randomizer.rand(13, 332), 0);
                  forceAtom.setnAttackCount(attackCount + 1);
                  atom.setDwFirstTargetId(nextObjectId);
                  atom.addForceAtom(forceAtom);
                  c.getPlayer().getMap().spawnMapleAtom(atom);
               }
               break;
            case 400041023:
               effect = SkillFactory.getSkill(400041022).getEffect(c.getPlayer().getSkillLevel(GameConstants.getLinkedSkill(skillId)));
               if (c.getPlayer().useBlackJack) {
                  c.getPlayer().addSkillCustomInfo(400041022, 1L);
                  if (c.getPlayer().getSkillCustomValue0(400041022) >= 3L) {
                     c.getPlayer().getMap().broadcastMessage(CField.blackJack(c.getPlayer(), 400041080, new Point(x, y)));
                  }
               } else if (attackCount < effect.getZ() && nextObjectId != 0) {
                  c.getPlayer().addSkillCustomInfo(400041080, -1L);
                  atom = new MapleAtom(true, nextObjectId, 33, true, 400041023, x, y);
                  atom.setDwUserOwner(c.getPlayer().getId());
                  forceAtom = new ForceAtom(33, Randomizer.rand(60, 70), Randomizer.rand(10, 10), Randomizer.rand(5, 360), 0);
                  forceAtom.setnAttackCount(attackCount + 1);
                  atom.setDwFirstTargetId(nextObjectId);
                  atom.addForceAtom(forceAtom);
                  c.getPlayer().getMap().spawnMapleAtom(atom);
               } else {
                  c.getPlayer().getMap().broadcastMessage(CField.blackJack(c.getPlayer(), 400041024, new Point(x, y)));
               }
            }
         }

      }
   }

   public static void ZeroScrollUI(int scroll, MapleClient c) {
      c.getSession().writeAndFlush(CField.ZeroScroll(scroll));
   }

   public static void ZeroScrollLucky(LittleEndianAccessor slea, MapleClient c) {
      int s_type = slea.readInt();
      int pos = slea.readShort();
      c.getPlayer().setZeroCubePosition(pos);
      Equip equip1 = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
      Equip equip2 = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
      if (equip1.getItemId() > 1560000 && equip1.getItemId() < 1570000 && equip2.getItemId() > 1572000 && equip2.getItemId() < 1573000) {
         InventoryHandler.UseUpgradeScroll((RecvPacketOpcode)null, (short)((byte)pos), (short)((byte)equip1.getPosition()), (byte)0, c, c.getPlayer());
         InventoryHandler.UseUpgradeScroll((RecvPacketOpcode)null, (short)((byte)pos), (short)((byte)equip2.getPosition()), (byte)0, c, c.getPlayer());
         c.getPlayer().setZeroCubePosition(-1);
      }

   }

   public static void ZeroScroll(LittleEndianAccessor slea, MapleClient c) {
      int s_type = slea.readInt();
      int pos = slea.readInt();
      slea.skip(8);
      int s_pos = slea.readInt();
      c.getSession().writeAndFlush(CField.ZeroScrollSend(s_pos));
   }

   public static void ZeroScrollStart(RecvPacketOpcode header, LittleEndianAccessor slea, MapleClient c) {
      c.getSession().writeAndFlush(CField.ZeroScrollStart());
   }

   public static void ZeroWeaponInfo(LittleEndianAccessor slea, MapleClient c) {
      MapleCharacter player = c.getPlayer();
      Item alpha = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
      int action = 1;
      int level = 0;
      int type = 0;
      int itemid = 0;
      int quantity = 0;
      switch(alpha.getItemId()) {
      case 1562000:
         type = 1;
         level = 100;
         break;
      case 1562001:
         type = 2;
         level = 110;
         break;
      case 1562002:
         type = 2;
         level = 120;
         break;
      case 1562003:
         type = 2;
         level = 130;
         break;
      case 1562004:
         type = 4;
         level = 140;
         break;
      case 1562005:
         type = 5;
         level = 150;
         break;
      case 1562006:
         type = 6;
         level = 160;
         break;
      case 1562007:
         type = 7;
         level = 160;
         itemid = 4310216;
         quantity = 1;
         break;
      case 1562008:
         type = 8;
         level = 200;
         itemid = 4310217;
         quantity = 1;
         break;
      case 1562009:
         type = 9;
         level = 200;
         itemid = 4310260;
         quantity = 1;
         break;
      case 1562010:
         action = 0;
         type = 0;
         level = 0;
      }

      if (player.getLevel() < level) {
         action = 0;
      }

      c.getSession().writeAndFlush(CField.WeaponInfo(type, level, action, alpha.getItemId(), itemid, quantity));
   }

   public static void ZeroWeaponLevelUp(LittleEndianAccessor slea, MapleClient c) {
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      slea.skip(7);
      Item alpha = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
      Item beta = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
      byte betatype = true;
      byte alphatype = true;
      Equip nalphatype = (Equip)alpha;
      Equip nbetatype = (Equip)beta;
      if (nbetatype.getItemId() == 1562007) {
         if (!c.getPlayer().haveItem(4310216, 1)) {
            return;
         }
      } else if (nbetatype.getItemId() == 1562008) {
         if (!c.getPlayer().haveItem(4310217, 1)) {
            return;
         }
      } else if (nbetatype.getItemId() == 1562009 && !c.getPlayer().haveItem(4310260, 1)) {
         return;
      }

      if (ii.getReqLevel(nbetatype.getItemId() + 1) > c.getPlayer().getLevel()) {
         c.getPlayer().dropMessage(1, "요구 레벨이 부족하여 무기 성장을 할 수 없습니다.");
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      } else {
         long fire1 = nalphatype.getFire();
         long fire2 = nbetatype.getFire();
         nalphatype.resetRebirth(ii.getReqLevel(nalphatype.getItemId()));
         nbetatype.resetRebirth(ii.getReqLevel(nbetatype.getItemId()));
         nbetatype.setItemId(nbetatype.getItemId() + 1);
         nalphatype.setItemId(nalphatype.getItemId() + 1);
         if (nbetatype.getItemId() == 1562001) {
            nalphatype.setWatk((short)100);
            nbetatype.setWatk((short)102);
            nbetatype.setWdef((short)80);
            nbetatype.setMdef((short)35);
            nalphatype.addUpgradeSlots((byte)7);
            nbetatype.addUpgradeSlots((byte)7);
         } else if (nbetatype.getItemId() == 1562002) {
            nalphatype.addWatk((short)3);
            nbetatype.addWatk((short)3);
            nbetatype.addWdef((short)10);
            nbetatype.addMdef((short)5);
         } else if (nbetatype.getItemId() == 1562003) {
            nalphatype.addWatk((short)2);
            nbetatype.addWatk((short)2);
            nbetatype.addWdef((short)10);
            nbetatype.addMdef((short)5);
         } else if (nbetatype.getItemId() == 1562004) {
            nalphatype.addWatk((short)7);
            nbetatype.addWatk((short)7);
            nbetatype.addWdef((short)10);
            nbetatype.addMdef((short)5);
         } else if (nbetatype.getItemId() == 1562005) {
            nalphatype.addStr((short)8);
            nalphatype.addDex((short)4);
            nalphatype.addWatk((short)5);
            nalphatype.addAcc((short)50);
            nalphatype.addUpgradeSlots((byte)1);
            nbetatype.addStr((short)8);
            nbetatype.addDex((short)4);
            nbetatype.addWatk((short)7);
            nbetatype.addWdef((short)10);
            nbetatype.addMdef((short)5);
            nbetatype.addAcc((short)50);
            nbetatype.addUpgradeSlots((byte)1);
         } else if (nbetatype.getItemId() == 1562006) {
            nalphatype.addStr((short)27);
            nalphatype.addDex((short)16);
            nalphatype.addWatk((short)18);
            nalphatype.addAcc((short)50);
            nbetatype.addStr((short)27);
            nbetatype.addDex((short)16);
            nbetatype.addWatk((short)18);
            nbetatype.addWdef((short)10);
            nbetatype.addMdef((short)5);
            nbetatype.addAcc((short)50);
         } else if (nbetatype.getItemId() == 1562007) {
            nalphatype.addStr((short)5);
            nalphatype.addDex((short)20);
            nalphatype.addWatk((short)34);
            nalphatype.addAcc((short)20);
            nalphatype.addBossDamage((byte)30);
            nalphatype.addIgnoreWdef((short)10);
            nbetatype.addStr((short)5);
            nbetatype.addDex((short)20);
            nbetatype.addWatk((short)34);
            nbetatype.addWdef((short)20);
            nbetatype.addMdef((short)10);
            nbetatype.addAcc((short)20);
            nbetatype.addBossDamage((byte)30);
            nbetatype.addIgnoreWdef((short)10);
         } else if (nbetatype.getItemId() == 1562008) {
            nalphatype.addStr((short)20);
            nalphatype.addDex((short)20);
            nalphatype.addWatk((short)34);
            nalphatype.addAcc((short)20);
            nbetatype.addStr((short)20);
            nbetatype.addDex((short)20);
            nbetatype.addWatk((short)34);
            nbetatype.addWdef((short)10);
            nbetatype.addMdef((short)10);
            nbetatype.addAcc((short)20);
            MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4310216, 1, false, false);
         } else if (nbetatype.getItemId() == 1562009) {
            nalphatype.addStr((short)40);
            nalphatype.addDex((short)40);
            nalphatype.addWatk((short)90);
            nalphatype.addAcc((short)20);
            nalphatype.addIgnoreWdef((short)10);
            nbetatype.addStr((short)40);
            nbetatype.addDex((short)40);
            nbetatype.addWatk((short)90);
            nbetatype.addWdef((short)40);
            nbetatype.addMdef((short)40);
            nbetatype.addAcc((short)20);
            nbetatype.addIgnoreWdef((short)10);
            MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4310217, 1, false, false);
         } else if (nbetatype.getItemId() == 1562010) {
            nalphatype.addStr((short)50);
            nalphatype.addDex((short)50);
            nalphatype.addWatk((short)44);
            nalphatype.addAcc((short)20);
            nbetatype.addStr((short)50);
            nbetatype.addDex((short)50);
            nbetatype.addWatk((short)45);
            nbetatype.addWdef((short)50);
            nbetatype.addMdef((short)50);
            nbetatype.addAcc((short)20);
            MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4310260, 1, false, false);
         }

         if (fire1 > 0L && fire2 > 0L) {
            nalphatype.refreshFire(nalphatype, fire1, false);
            nbetatype.refreshFire(nbetatype, fire2, false);
         }

         c.getSession().writeAndFlush(CField.WeaponLevelUp());
         c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, nalphatype));
         c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, nbetatype));
      }
   }

   public static void ZeroTag(LittleEndianAccessor slea, MapleClient c) {
      MapleCharacter player = c.getPlayer();
      slea.skip(4);
      player.setSkillCustomInfo(101000201, (long)slea.readInt(), 0L);
      long nowhp = player.getStat().getHp();
      player.getMap().broadcastMessage(CField.MultiTag(player));
      c.getSession().writeAndFlush(CField.ZeroTag(player, player.getSecondGender(), (int)nowhp, (int)player.getStat().getCurrentMaxHp()));
      if (player.getGender() == 0 && player.getSecondGender() == 1) {
         player.setGender((byte)1);
         player.setSecondGender((byte)0);
      } else if (player.getGender() == 1 && player.getSecondGender() == 0) {
         player.setGender((byte)0);
         player.setSecondGender((byte)1);
         player.armorSplit = 0;
      }

      player.getStat().recalcLocalStats(player);
      if (player.getGender() == 0) {
         if (player.getBuffedValue(SecondaryStat.ImmuneBarrier) != null) {
            while(player.getBuffedValue(101120109)) {
               player.cancelEffect(player.getBuffedEffect(101120109));
            }
         }

         player.setSkillCustomInfo(101112, (long)((int)nowhp), 0L);
         player.setSkillCustomInfo(101114, (long)((int)player.getStat().getCurrentMaxHp()), 0L);
         if (player.getSkillCustomValue0(101113) > 0L) {
            player.getStat().setHp(player.getSkillCustomValue0(101113), player);
         }
      } else {
         player.setSkillCustomInfo(101113, (long)((int)nowhp), 0L);
         player.setSkillCustomInfo(101115, (long)((int)player.getStat().getCurrentMaxHp()), 0L);
         if (player.getSkillCustomValue0(101112) > 0L) {
            player.getStat().setHp(player.getSkillCustomValue0(101112), player);
         }
      }

      player.getMap().broadcastMessage(player, CField.ZeroTagUpdateCharLook(player), player.getPosition());
   }

   public static void ZeroTagRemove(MapleClient c) {
      c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.MultiTagRemove(c.getPlayer().getId()), false);
   }

   public static void subActiveSkills(LittleEndianAccessor slea, MapleClient c) {
      int skillid = slea.readInt();
      switch(skillid) {
      case 1201012:
         slea.skip(4);
         int mobId = slea.readInt();
         SecondaryStatEffect effect = SkillFactory.getSkill(skillid).getEffect(c.getPlayer().getTotalSkillLevel(skillid));
         MapleMonster mob = c.getPlayer().getMap().getMonsterByOid(mobId);
         if (mob != null && effect.makeChanceResult()) {
            mob.applyStatus(c, MonsterStatus.MS_Freeze, new MonsterStatusEffect(skillid, effect.getDuration()), 1, effect);
         }

         return;
      default:
         if (c.getPlayer().isGM()) {
            c.getPlayer().dropMessage(5, "SUB ACTIVE SKILL : " + skillid);
         }

      }
   }

   public static void ZeroClothes(LittleEndianAccessor slea, MapleClient c) {
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      MapleCharacter chr = c.getPlayer();
      int kind = slea.readInt();
      byte check = slea.readByte();
      int dst = -100;
      int dst2 = -1500;
      int secondkind = false;
      int secondkind;
      switch(kind) {
      case 0:
         secondkind = 0;
         break;
      case 1:
      case 2:
      case 5:
      case 10:
      default:
         secondkind = kind;
         break;
      case 3:
         secondkind = 0;
         break;
      case 4:
         secondkind = 3;
         break;
      case 6:
         secondkind = 8;
         break;
      case 7:
         secondkind = 9;
         break;
      case 8:
         secondkind = 6;
         break;
      case 9:
         secondkind = 4;
         break;
      case 11:
         secondkind = 7;
         break;
      case 12:
         secondkind = 10;
      }

      int var10000 = dst - kind;
      int dst2 = dst2 - secondkind;
      Equip source = (Equip)ii.getEquipById((int)chr.getSkillCustomValue0(10112));
      Equip target2 = (Equip)chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)dst2);
      if (check == 1) {
         short id;
         if (target2 == null) {
            id = chr.getInventory(MapleInventoryType.CODY).getNextFreeSlot();
            chr.gainItem(source.getItemId(), 1);
            source = (Equip)chr.getInventory(MapleInventoryType.CODY).getItem((short)id);
            source.setFinalStrike(true);
            source.setPosition((short)dst2);
            chr.getInventory(MapleInventoryType.CODY).removeSlot((short)id);
            chr.getInventory(MapleInventoryType.EQUIPPED).addFromDB(source);
            c.send(CWvsContext.InventoryPacket.UpedateInventoryItem(dst2, id));
            chr.equipChanged();
         } else if (target2 != null) {
            MapleInventoryManipulator.unequip(c, (short)dst2, chr.getInventory(MapleInventoryType.CODY).getNextFreeSlot(), MapleInventoryType.CODY);
            id = chr.getInventory(MapleInventoryType.CODY).getNextFreeSlot();
            chr.gainItem(source.getItemId(), 1);
            source = (Equip)chr.getInventory(MapleInventoryType.CODY).getItem((short)id);
            source.setFinalStrike(true);
            chr.getInventory(MapleInventoryType.CODY).removeSlot((short)id);
            source.setPosition((short)dst2);
            chr.getInventory(MapleInventoryType.EQUIPPED).addFromDB(source);
            c.send(CWvsContext.InventoryPacket.UpedateInventoryItem(dst2, id));
            chr.equipChanged();
         }
      }

   }

   public static void FieldAttackObjAttack(LittleEndianAccessor slea, MapleCharacter chr) {
      short type = slea.readShort();
      int cid = slea.readInt();
      int key = slea.readInt();
      if (chr != null) {
         if (chr.getId() == cid) {
            short type2;
            if (type == 0) {
               byte type2 = slea.readByte();
               Point pos = slea.readPos();
               Point oldPos = null;
               if (type2 == 5) {
                  oldPos = slea.readPos();
               }

               type2 = slea.readShort();
               int sourceid = slea.readInt();
               int level = slea.readInt();
               int duration = slea.readInt();
               short unk2 = slea.readShort();
               chr.getMap().broadcastMessage(CField.B2BodyResult(chr, cid, type, (short)type2, key, pos, oldPos, type2, sourceid, level, duration, unk2, chr.isFacingLeft(), 0, 0, (String)null));
               chr.getMap().broadcastMessage(CField.spawnSubSummon(type, key));
            } else {
               int sourceid;
               if (type == 3) {
                  int sourceid = slea.readInt();
                  sourceid = slea.readInt();
                  int unk3 = slea.readInt();
                  int unk4 = slea.readInt();
                  chr.getMap().broadcastMessage(CField.B2BodyResult(chr, cid, type, (short)0, key, (Point)null, (Point)null, (short)0, sourceid, sourceid, 0, (short)0, chr.isFacingLeft(), unk3, unk4, (String)null));
                  chr.getMap().broadcastMessage(CField.spawnSubSummon(type, key));
               } else if (type == 4) {
                  Point pos = slea.readPos();
                  slea.skip(4);
                  if (slea.available() <= 0L) {
                     return;
                  }

                  sourceid = slea.readInt();
                  boolean facingleft = slea.readByte() == 1;
                  slea.skip(18);
                  type2 = slea.readShort();
                  short unk1 = slea.readShort();
                  short unk2 = slea.readShort();
                  byte unk3 = slea.readByte();
                  String unk = "";
                  if (unk3 > 0) {
                     unk = slea.readMapleAsciiString();
                  }

                  int unk4 = slea.readInt();
                  Point oldPos = new Point(slea.readInt(), slea.readInt());
                  MapleFieldAttackObj fao = new MapleFieldAttackObj(chr, sourceid, facingleft, pos, type2 * 1000);
                  if (chr.getFao() == null) {
                     chr.setFao(fao);
                  }

                  chr.getMap().broadcastMessage(CField.spawnSubSummon(type, key));
                  chr.getMap().broadcastMessage(CField.B2BodyResult(chr, cid, type, type2, key, pos, oldPos, unk1, sourceid, 0, 0, unk2, facingleft, unk3, unk4, unk));
                  if (sourceid == 400031033) {
                     Vmatrixstackbuff(chr.getClient(), true, (LittleEndianAccessor)null);
                  }
               }
            }

         }
      }
   }

   public static void FieldAttackObjAction(LittleEndianAccessor slea, MapleCharacter chr) {
      boolean isLeft = slea.readByte() > 0;
      int x = slea.readInt();
      int y = slea.readInt();
      boolean disable = chr.isDominant();
      Iterator var6 = chr.getMap().getMapObjectsInRange(chr.getTruePosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.FIELD)).iterator();

      while(var6.hasNext()) {
         MapleMapObject obj = (MapleMapObject)var6.next();
         MapleFieldAttackObj fao = (MapleFieldAttackObj)obj;
         if (fao.getChr().getId() == chr.getId()) {
            List<MapleFieldAttackObj> removes = new ArrayList();
            removes.add(fao);
            chr.getMap().broadcastMessage(CField.AttackObjPacket.ObjRemovePacketByList(removes));
            chr.getMap().removeMapObject(fao);
            break;
         }
      }

      if (chr.getFao() != null) {
         chr.getFao().setFacingleft(isLeft);
         chr.getFao().setPosition(new Point(x, y));
         chr.getMap().spawnFieldAttackObj(chr.getFao());
         chr.setDominant(!disable);
      }

   }

   public static void OrbitalFlame(LittleEndianAccessor slea, MapleClient c) {
      MapleCharacter chr = c.getPlayer();
      int tempskill = slea.readInt();
      byte level = slea.readByte();
      int direction = slea.readShort();
      int skillid = 0;
      int elementid = 0;
      int effect = 0;
      switch(tempskill) {
      case 12001020:
         skillid = 12000026;
         elementid = 12000022;
         effect = 1;
         break;
      case 12100020:
         skillid = 12100028;
         elementid = 12100026;
         effect = 2;
         break;
      case 12110020:
         skillid = 12110028;
         elementid = 12110024;
         effect = 3;
         break;
      case 12120006:
      case 12120018:
      case 12121056:
         skillid = 12120010;
         elementid = 12120007;
         effect = 4;
      }

      SecondaryStatEffect flame = SkillFactory.getSkill(tempskill).getEffect(level);
      SecondaryStatEffect orbital;
      if (flame != null && chr.getSkillLevel(elementid) > 0) {
         orbital = SkillFactory.getSkill(elementid).getEffect(chr.getSkillLevel(elementid));
         orbital.applyTo(chr, false);
      }

      orbital = SkillFactory.getSkill(skillid).getEffect(chr.getSkillLevel(skillid));
      orbital.applyTo(chr);
      MapleAtom atom = new MapleAtom(false, chr.getId(), 17, true, skillid, chr.getTruePosition().x, chr.getTruePosition().y);
      ForceAtom forceAtom = new ForceAtom(effect, 17, 17, 90, 0);
      forceAtom.setnMaxHitCount(flame.getMobCount());
      atom.addForceAtom(forceAtom);
      atom.setDwTargets(new ArrayList());
      if (chr.getBuffedEffect(SecondaryStat.AddRange) != null) {
         atom.setnArriveRange(flame.getRange() + chr.getBuffedValue(SecondaryStat.AddRange));
      } else {
         atom.setnArriveRange(flame.getRange());
      }

      atom.setnArriveDir(direction);
      chr.getMap().spawnMapleAtom(atom);
      chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, 12120006, 1, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), true, chr.getTruePosition(), (String)null, (Item)null));
      chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 0, 12120006, 1, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), false, chr.getTruePosition(), (String)null, (Item)null), false);
   }

   public static void VoydPressure(LittleEndianAccessor slea, MapleCharacter chr) {
      List<Byte> arrays = new ArrayList();
      byte size = slea.readByte();

      for(int i = 0; i < size; ++i) {
         arrays.add(slea.readByte());
      }

      chr.getMap().broadcastMessage(chr, CField.showVoydPressure(chr.getId(), arrays), false);
   }

   public static void absorbingSword(LittleEndianAccessor slea, MapleCharacter chr) {
      int skill = slea.readInt();
      int mobSize = slea.readInt();
      MapleAtom atom = new MapleAtom(false, chr.getId(), skill != 400011058 && skill != 400011059 ? 2 : 32, true, skill, chr.getTruePosition().x, chr.getTruePosition().y);
      ArrayList<Integer> monsters = new ArrayList();

      for(int i = 0; i < mobSize; ++i) {
         monsters.add(slea.readInt());
         atom.addForceAtom(new ForceAtom(chr.getBuffedValue(61121217) ? 4 : 2, 18, Randomizer.rand(20, 40), 0, (short)Randomizer.rand(1000, 1500)));
      }

      while(atom.getForceAtoms().size() < (!chr.getBuffedValue(61120007) && !chr.getBuffedValue(61121217) ? 3 : 5)) {
         atom.addForceAtom(new ForceAtom(chr.getBuffedValue(61121217) ? 4 : 2, 18, Randomizer.rand(20, 40), 0, (short)Randomizer.rand(1000, 1500)));
      }

      if (skill != 0) {
         chr.cancelEffectFromBuffStat(SecondaryStat.StopForceAtominfo);
         atom.setDwTargets(monsters);
         chr.getMap().spawnMapleAtom(atom);
      }

      if (skill == 400011058 || skill == 400011059) {
         chr.getClient().getSession().writeAndFlush(CField.skillCooldown(skill, SkillFactory.getSkill(400011058).getEffect(chr.getSkillLevel(400011058)).getCooldown(chr)));
         chr.addCooldown(skill, System.currentTimeMillis(), (long)SkillFactory.getSkill(400011058).getEffect(chr.getSkillLevel(400011058)).getCooldown(chr));
      }

   }

   public static void DressUpRequest(MapleCharacter chr, LittleEndianAccessor slea) {
      int code = slea.readInt();
      switch(code) {
      case 5010093:
         chr.setDressup(false);
         chr.getMap().broadcastMessage(CField.updateCharLook(chr, chr.getDressup()));
         chr.getMap().broadcastMessage(CField.updateDress(code, chr));
         break;
      case 5010094:
         chr.setDressup(true);
         chr.getMap().broadcastMessage(CField.updateCharLook(chr, chr.getDressup()));
         chr.getMap().broadcastMessage(CField.updateDress(code, chr));
      }

   }

   public static final void DressUpTime(LittleEndianAccessor rh, MapleClient c) {
      byte type = rh.readByte();
      if (type == 1) {
         if (GameConstants.isAngelicBuster(c.getPlayer().getJob())) {
            c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.updateCharLook(c.getPlayer(), c.getPlayer().getDressup()), false);
         }
      } else {
         c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.updateCharLook(c.getPlayer(), c.getPlayer().getDressup()), false);
      }

   }

   public static final void test(LittleEndianAccessor slea, MapleClient c) {
      slea.skip(6);
      c.getSession().writeAndFlush(CWvsContext.Test());
      c.getSession().writeAndFlush(CWvsContext.Test1());
   }

   public static final void PsychicGrabPreparation(LittleEndianAccessor slea, MapleClient c, boolean shot) {
      Map<Integer, List<PsychicGrabEntry>> grab = new LinkedHashMap();
      int skillid = slea.readInt();
      short unk_ = slea.readShort();
      int id = slea.readInt();
      int unk1 = slea.readInt();
      int subskillid;
      int b;
      int size;
      int i;
      if (!shot) {
         byte firstsize = 1;

         for(subskillid = 1; firstsize > 0; ++subskillid) {
            firstsize = slea.readByte();
            if (firstsize > 0) {
               int a = slea.readInt();
               b = slea.readInt();
               size = slea.readInt();
               i = slea.readInt();
               short secondsize = slea.readShort();
               slea.skip(2);
               byte unk = slea.readByte();
               Rectangle rect = new Rectangle(slea.readInt(), slea.readInt(), slea.readInt(), slea.readInt());
               grab.put(subskillid, new ArrayList());
               MapleMonster monster = c.getPlayer().getMap().getMonsterByOid(size);
               long mobhp = size > 0 && monster != null ? monster.getHp() : 100L;
               long mobmaxhp = size > 0 && monster != null ? monster.getMobMaxHp() : 100L;
               ((List)grab.get(subskillid)).add(new PsychicGrabEntry(firstsize, a, -1 - a + subskillid, size, secondsize, mobhp, mobmaxhp, unk, rect, i));
            }
         }

         c.getPlayer().getMap().broadcastMessage(CWvsContext.PsychicGrab(c.getPlayer().getId(), skillid, unk_, id, grab));
         grab.clear();
      } else if (skillid == 142110003 || skillid == 142110004 || skillid == 142120002 || skillid == 142120001) {
         List<Integer> grab_ = new ArrayList();
         subskillid = skillid - 2;
         byte a = slea.readByte();
         b = slea.readInt();
         if (skillid == 142110003 || skillid == 142120001) {
            subskillid = slea.readInt();
            slea.skip(4);
         }

         size = slea.readInt();

         for(i = 0; i < size; ++i) {
            grab_.add(slea.readInt());
         }

         c.getPlayer().getMap().broadcastMessage(CWvsContext.PsychicGrabAttack(c.getPlayer().getId(), skillid, subskillid, unk_, id, a, b, grab_));
      }

      c.getPlayer().givePPoint(skillid);
   }

   public static void MatrixSkill(LittleEndianAccessor slea, MapleClient c) {
      MapleCharacter chr = c.getPlayer();
      if (chr != null) {
         int skillid = slea.readInt();
         int level = slea.readInt();
         AttackInfo ret = new AttackInfo();
         ret.skill = skillid;
         ret.skilllevel = level;
         GameConstants.calcAttackPosition(slea, ret);
         int unk1 = slea.readInt();
         int unk2 = slea.readInt();
         byte dir = slea.readByte();
         int bullet = slea.readInt();
         slea.skip(11);
         slea.readPos();
         List<Integer> data = new ArrayList();
         boolean enable2 = slea.readByte() == 1;
         if (enable2) {
            data.add(slea.readInt());
            data.add(slea.readInt());
            data.add(slea.readInt());
            data.add(slea.readInt());
            data.add(slea.readInt());
            data.add(Integer.valueOf(slea.readByte()));
            data.add(slea.readInt());
            data.add(Integer.valueOf(slea.readByte()));
         }

         List<MatrixSkill> skills = GameConstants.matrixSkills(slea);
         Skill skill = SkillFactory.getSkill(skillid);
         SecondaryStatEffect effect = skill.getEffect(chr.getSkillLevel(skillid));
         c.getSession().writeAndFlush(CWvsContext.MatrixSkill(skillid, level, skills));
         chr.getMap().broadcastMessage(chr, CWvsContext.MatrixSkillMulti(chr, skillid, level, unk1, unk2, bullet, enable2, data, skills, dir), false);
         if (effect.getCooldown(chr) > 0) {
            c.getSession().writeAndFlush(CField.skillCooldown(skillid, effect.getCooldown(chr)));
            chr.addCooldown(skillid, System.currentTimeMillis(), (long)effect.getCooldown(chr));
         }

         if (!GameConstants.isNoApplySkill(skillid)) {
            effect.applyTo(chr, false);
         }

         if (skillid == 400031026) {
            chr.getMap().broadcastMessage(chr, CField.skillCancel(chr, 0), false);
         }

         if (skillid == 400021048) {
            chr.givePPoint(skillid);
         }

         if (GameConstants.isCain(c.getPlayer().getJob())) {
            c.getPlayer().handleRemainIncense(skillid, true);
         }

         switch(skillid) {
         case 3301008:
         case 3311010:
            MapleCharacter.렐릭게이지(c, skillid);
         case 12120023:
         default:
            break;
         case 37111006:
            chr.Cylinder(skillid);
            break;
         case 63101004:
         case 63111003:
            chr.handleCainSkillCooldown(skillid);
            chr.handleStackskill(skillid, true);
            break;
         case 63101100:
         case 63111103:
            chr.handleCainSkillCooldown(skillid);
            break;
         case 151100002:
            if (c.getPlayer().getSkillLevel(151120034) <= 0) {
               c.getPlayer().에테르핸들러(c.getPlayer(), -15, skillid, false);
            }
            break;
         case 151101001:
            chr.addSkillCustomInfo(151121041, 1L);
            break;
         case 400021070:
            chr.peaceMaker = effect.getW();
            break;
         case 400031056:
            if (chr.getBuffedEffect(SecondaryStat.RepeatingCrossbowCatridge) != null) {
               if (chr.repeatingCrossbowCatridge > 1) {
                  --chr.repeatingCrossbowCatridge;
                  Map<SecondaryStat, Pair<Integer, Integer>> map = new HashMap();
                  map.put(SecondaryStat.RepeatingCrossbowCatridge, new Pair(chr.repeatingCrossbowCatridge, (int)chr.getBuffLimit(chr.getBuffSource(SecondaryStat.RepeatingCrossbowCatridge))));
                  chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(map, chr.getBuffedEffect(SecondaryStat.RepeatingCrossbowCatridge), chr));
               } else {
                  chr.cancelEffectFromBuffStat(SecondaryStat.RepeatingCrossbowCatridge);
               }
            }
            break;
         case 400051003:
            if (chr.transformEnergyOrb <= 0) {
               return;
            }

            SecondaryStatEffect eff = SkillFactory.getSkill(400051002).getEffect(chr.getSkillLevel(400051002));
            --chr.transformEnergyOrb;
            if (chr.transformEnergyOrb == 0) {
               chr.transformEnergyOrb = -1;
            }

            Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
            statups.put(SecondaryStat.Transform, new Pair(chr.transformEnergyOrb, (int)chr.getBuffLimit(400051002)));
            c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, eff, chr));
            chr.getMap().broadcastMessage(chr, CWvsContext.BuffPacket.giveForeignBuff(chr, statups, eff), false);
            SkillFactory.getSkill(400051003).getEffect(chr.getSkillLevel(400051002)).applyTo(chr);
            break;
         case 400051008:
         case 400051042:
            Vmatrixstackbuff(c, true, slea);
            break;
         case 400051016:
            chr.lightning -= 2;
            if (chr.lightning < 0) {
               chr.lightning = 0;
            }

            SecondaryStatValueHolder lightning = chr.checkBuffStatValueHolder(SecondaryStat.CygnusElementSkill);
            if (lightning != null) {
               lightning.effect.applyTo(chr, false);
            }
         }

      }
   }

   public static void UpdateSymbol(LittleEndianAccessor slea, MapleClient c, int plus) {
      try {
         int pos = slea.readInt() * -1;
         Equip item = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)pos);
         boolean ArcneSymbol = GameConstants.isArcaneSymbol(item.getItemId());
         int nextupgrade = ArcneSymbol ? GameConstants.ArcaneNextUpgrade(item.getArcLevel()) : GameConstants.AutNextUpgrade(item.getArcLevel());
         long needmeso = ArcneSymbol ? (long)(12440000 + 6600000 * item.getArcLevel()) : GameConstants.NeedAutSymbolMeso(item.getArcLevel());
         if (item.getArcEXP() >= nextupgrade) {
            if (c.getPlayer().getMeso() >= needmeso) {
               int astats = 100;
               if (!ArcneSymbol) {
                  astats *= 2;
               }

               int var10000;
               if (ArcneSymbol) {
                  var10000 = 10 * (item.getArcLevel() + 2);
               } else {
                  var10000 = item.getArc() + 10;
               }

               c.getPlayer().gainMeso(-needmeso, false);
               item.setArcEXP(item.getArcEXP() - nextupgrade);
               item.setArcLevel(item.getArcLevel() + 1);
               item.setArc((short)(item.getArc() + 10));
               int stats;
               if (GameConstants.isXenon(c.getPlayer().getJob())) {
                  stats = 39;
                  if (!ArcneSymbol) {
                     stats *= 2;
                  }

                  item.setStr((short)(item.getStr() + stats));
                  item.setDex((short)(item.getDex() + stats));
                  item.setLuk((short)(item.getLuk() + stats));
               } else if (GameConstants.isDemonAvenger(c.getPlayer().getJob())) {
                  stats = 175;
                  if (!ArcneSymbol) {
                     stats *= 2;
                  }

                  item.setHp((short)(item.getHp() + stats));
               } else if (GameConstants.isWarrior(c.getPlayer().getJob())) {
                  item.setStr((short)(item.getStr() + astats));
               } else if (GameConstants.isMagician(c.getPlayer().getJob())) {
                  item.setInt((short)(item.getInt() + astats));
               } else if (!GameConstants.isArcher(c.getPlayer().getJob()) && !GameConstants.isCaptain(c.getPlayer().getJob()) && !GameConstants.isMechanic(c.getPlayer().getJob()) && !GameConstants.isAngelicBuster(c.getPlayer().getJob())) {
                  if (GameConstants.isThief(c.getPlayer().getJob())) {
                     item.setLuk((short)(item.getLuk() + astats));
                  } else if (GameConstants.isPirate(c.getPlayer().getJob())) {
                     item.setStr((short)(item.getStr() + astats));
                  }
               } else {
                  item.setDex((short)(item.getDex() + astats));
               }

               c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, item));
            } else {
               c.getPlayer().dropMessage(1, "메소가 부족합니다.");
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            }
         } else {
            c.getPlayer().dropMessage(1, "필요 성장치가 부족합니다.");
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
         }
      } catch (Exception var14) {
         var14.printStackTrace();
      }

   }

   public static void SymbolExp(LittleEndianAccessor slea, MapleClient c) {
      try {
         Equip source = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(slea.readShort());
         if (source == null) {
            return;
         }

         int baseid = GameConstants.isArcaneSymbol(source.getItemId()) ? -1600 : (GameConstants.isAuthenticSymbol(source.getItemId()) ? -1700 : 0);
         Equip target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)baseid);
         if (target != null && source.getItemId() != target.getItemId()) {
            target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)(baseid - 1));
         }

         if (target == null) {
            target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)(baseid - 1));
         }

         if (target != null && source.getItemId() != target.getItemId()) {
            target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)(baseid - 2));
         }

         if (target == null) {
            target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)(baseid - 2));
         }

         if (target != null && source.getItemId() != target.getItemId()) {
            target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)(baseid - 3));
         }

         if (target == null) {
            target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)(baseid - 3));
         }

         if (target != null && source.getItemId() != target.getItemId()) {
            target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)(baseid - 4));
         }

         if (target == null) {
            target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)(baseid - 4));
         }

         if (target != null && source.getItemId() != target.getItemId()) {
            target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)(baseid - 5));
         }

         if (target == null) {
            return;
         }

         if (source.getItemId() != target.getItemId()) {
            return;
         }

         target.setArcEXP(target.getArcEXP() + source.getArcEXP() / 2 + 1);
         c.getPlayer().getInventory(MapleInventoryType.EQUIP).removeSlot(source.getPosition());
         c.getSession().writeAndFlush(CWvsContext.InventoryPacket.clearInventoryItem(MapleInventoryType.EQUIP, source.getPosition(), false));
         c.getPlayer().getSymbol().remove(source);
         c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, target));
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   public static void SymbolMultiExp(LittleEndianAccessor slea, MapleClient c) {
      try {
         int itemid = slea.readInt();
         int count = slea.readInt();
         int havecount = slea.readInt();
         Equip target = null;
         if (GameConstants.isArcaneSymbol(itemid)) {
            target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-1600);
            if (target != null && itemid != target.getItemId()) {
               target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-1601);
            }

            if (target == null) {
               target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-1601);
            }

            if (target != null && itemid != target.getItemId()) {
               target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-1602);
            }

            if (target == null) {
               target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-1602);
            }

            if (target != null && itemid != target.getItemId()) {
               target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-1603);
            }

            if (target == null) {
               target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-1603);
            }

            if (target != null && itemid != target.getItemId()) {
               target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-1604);
            }

            if (target == null) {
               target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-1604);
            }

            if (target != null && itemid != target.getItemId()) {
               target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-1605);
            }
         } else {
            target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-1700);
            if (target != null && itemid != target.getItemId()) {
               target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-1701);
            }

            if (target == null) {
               target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-1701);
            }

            if (target != null && itemid != target.getItemId()) {
               target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-1702);
            }

            if (target == null) {
               target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-1702);
            }
         }

         if (target == null) {
            return;
         }

         if (itemid != target.getItemId()) {
            return;
         }

         List<Equip> removeitems = new ArrayList();
         Iterator var7 = c.getPlayer().getInventory(MapleInventoryType.EQUIP).lists().entrySet().iterator();

         while(var7.hasNext()) {
            Entry<Short, Item> item = (Entry)var7.next();
            if (((Item)item.getValue()).getItemId() == itemid && ((Equip)item.getValue()).getArcEXP() == 1 && ((Equip)item.getValue()).getArcLevel() == 1 && removeitems.size() < count) {
               Equip source = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((Short)item.getKey());
               removeitems.add(source);
            }
         }

         if (removeitems.size() != count) {
            FileoutputUtil.log("Log_Packet_Except.rtf", c.getPlayer().getName() + " 캐릭터 심볼 비정상 사용발견");
         }

         target.setArcEXP(target.getArcEXP() + removeitems.size());
         var7 = removeitems.iterator();

         while(var7.hasNext()) {
            Equip item = (Equip)var7.next();
            c.getPlayer().getInventory(MapleInventoryType.EQUIP).removeSlot(item.getPosition());
            c.getSession().writeAndFlush(CWvsContext.InventoryPacket.clearInventoryItem(MapleInventoryType.EQUIP, item.getPosition(), false));
            c.getPlayer().getSymbol().remove(item);
         }

         c.getSession().writeAndFlush(CWvsContext.InventoryPacket.updateInventoryItem(false, MapleInventoryType.EQUIP, target));
      } catch (Exception var10) {
         var10.printStackTrace();
      }

   }

   public static void UnlinkSkill(int skillid, MapleClient c) {
      HashMap skills;
      int i;
      Iterator var4;
      Triple a;
      if (skillid == 80000055) {
         skills = new HashMap();

         for(i = 80000066; i <= 80000070; ++i) {
            if (c.getPlayer().getTotalSkillLevel(i) > 0) {
               var4 = c.getPlayer().getLinkSkills().iterator();

               while(var4.hasNext()) {
                  a = (Triple)var4.next();
                  if (((Skill)a.left).getId() == i) {
                     skills.put(i, (Integer)a.right);
                  }
               }

               c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(i), 0, (byte)0);
               c.getSession().writeAndFlush(CWvsContext.Unlinkskillunlock(i, 0));
            }
         }

         c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(skillid), 0, (byte)0);
         c.getSession().writeAndFlush(CWvsContext.Unlocklinkskill(skillid, skills));
      } else {
         Iterator var6;
         Triple a;
         if (skillid == 80000329) {
            skills = new HashMap();

            for(i = 80000333; i <= 80000335; ++i) {
               if (c.getPlayer().getTotalSkillLevel(i) > 0) {
                  var4 = c.getPlayer().getLinkSkills().iterator();

                  while(var4.hasNext()) {
                     a = (Triple)var4.next();
                     if (((Skill)a.left).getId() == i) {
                        skills.put(i, (Integer)a.right);
                     }
                  }

                  c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(i), 0, (byte)0);
                  c.getSession().writeAndFlush(CWvsContext.Unlinkskillunlock(i, 0));
               }
            }

            if (c.getPlayer().getTotalSkillLevel(80000378) > 0) {
               skills.put(80000378, c.getPlayer().getId());
               var6 = c.getPlayer().getLinkSkills().iterator();

               while(var6.hasNext()) {
                  a = (Triple)var6.next();
                  if (((Skill)a.left).getId() == 80000378) {
                     skills.put(80000378, (Integer)a.right);
                  }
               }

               c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(80000378), 0, (byte)0);
               c.getSession().writeAndFlush(CWvsContext.Unlinkskillunlock(80000378, 0));
            }

            c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(skillid), 0, (byte)0);
            c.getSession().writeAndFlush(CWvsContext.Unlocklinkskill(skillid, skills));
         } else if (skillid == 80002758) {
            skills = new HashMap();

            for(i = 80002759; i <= 80002761; ++i) {
               if (c.getPlayer().getTotalSkillLevel(i) > 0) {
                  var4 = c.getPlayer().getLinkSkills().iterator();

                  while(var4.hasNext()) {
                     a = (Triple)var4.next();
                     if (((Skill)a.left).getId() == i) {
                        skills.put(i, (Integer)a.right);
                     }
                  }

                  c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(i), 0, (byte)0);
                  c.getSession().writeAndFlush(CWvsContext.Unlinkskillunlock(i, 0));
               }
            }

            c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(skillid), 0, (byte)0);
            c.getSession().writeAndFlush(CWvsContext.Unlocklinkskill(skillid, skills));
         } else if (skillid == 80002762) {
            skills = new HashMap();

            for(i = 80002763; i <= 80002765; ++i) {
               if (c.getPlayer().getTotalSkillLevel(i) > 0) {
                  var4 = c.getPlayer().getLinkSkills().iterator();

                  while(var4.hasNext()) {
                     a = (Triple)var4.next();
                     if (((Skill)a.left).getId() == i) {
                        skills.put(i, (Integer)a.right);
                     }
                  }

                  c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(i), 0, (byte)0);
                  c.getSession().writeAndFlush(CWvsContext.Unlinkskillunlock(i, 0));
               }
            }

            c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(skillid), 0, (byte)0);
            c.getSession().writeAndFlush(CWvsContext.Unlocklinkskill(skillid, skills));
         } else if (skillid == 80002766) {
            skills = new HashMap();

            for(i = 80002767; i <= 80002769; ++i) {
               if (c.getPlayer().getTotalSkillLevel(i) > 0) {
                  var4 = c.getPlayer().getLinkSkills().iterator();

                  while(var4.hasNext()) {
                     a = (Triple)var4.next();
                     if (((Skill)a.left).getId() == i) {
                        skills.put(i, (Integer)a.right);
                     }
                  }

                  c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(i), 0, (byte)0);
                  c.getSession().writeAndFlush(CWvsContext.Unlinkskillunlock(i, 0));
               }
            }

            c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(skillid), 0, (byte)0);
            c.getSession().writeAndFlush(CWvsContext.Unlocklinkskill(skillid, skills));
         } else if (skillid == 80002770) {
            skills = new HashMap();

            for(i = 80002771; i <= 80002773; ++i) {
               if (c.getPlayer().getTotalSkillLevel(i) > 0) {
                  var4 = c.getPlayer().getLinkSkills().iterator();

                  while(var4.hasNext()) {
                     a = (Triple)var4.next();
                     if (((Skill)a.left).getId() == i) {
                        skills.put(i, (Integer)a.right);
                     }
                  }

                  c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(i), 0, (byte)0);
                  c.getSession().writeAndFlush(CWvsContext.Unlinkskillunlock(i, 0));
               }
            }

            c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(skillid), 0, (byte)0);
            c.getSession().writeAndFlush(CWvsContext.Unlocklinkskill(skillid, skills));
         } else if (skillid == 80002774) {
            skills = new HashMap();

            for(i = 80002775; i <= 80002776; ++i) {
               if (c.getPlayer().getTotalSkillLevel(i) > 0) {
                  var4 = c.getPlayer().getLinkSkills().iterator();

                  while(var4.hasNext()) {
                     a = (Triple)var4.next();
                     if (((Skill)a.left).getId() == i) {
                        skills.put(i, (Integer)a.right);
                     }
                  }

                  c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(i), 0, (byte)0);
                  c.getSession().writeAndFlush(CWvsContext.Unlinkskillunlock(i, 0));
               }
            }

            if (c.getPlayer().getTotalSkillLevel(80000000) > 0) {
               skills.put(80000378, c.getPlayer().getId());
               var6 = c.getPlayer().getLinkSkills().iterator();

               while(var6.hasNext()) {
                  a = (Triple)var6.next();
                  if (((Skill)a.left).getId() == 80000000) {
                     skills.put(80000000, (Integer)a.right);
                  }
               }

               c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(80000000), 0, (byte)0);
               c.getSession().writeAndFlush(CWvsContext.Unlinkskillunlock(80000000, 0));
            }

            c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(skillid), 0, (byte)0);
            c.getSession().writeAndFlush(CWvsContext.Unlocklinkskill(skillid, skills));
         } else {
            c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(skillid), 0, (byte)0);
            c.getSession().writeAndFlush(CWvsContext.Unlinkskill(skillid, 0));
         }
      }

      c.getPlayer().getStat().recalcLocalStats(c.getPlayer());
   }

   public static void LinkSkill(int skillid, int sendid, int recvid, MapleClient c) {
      if (c.getPlayer().getTotalSkillLevel(skillid) > 0) {
         c.getPlayer().dropMessage(6, "동일한 링크를 중복해서 착용하실 수 없습니다.");
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      }

      int skilllevel = MapleCharacter.loadCharFromDB(sendid, c, false).getLevel() >= 120 ? 2 : 1;
      int totalskilllv = 0;
      int ordinarySkill = 0;
      byte odrinaryMaxLevel = 0;
      if (skillid >= 80000066 && skillid <= 80000070) {
         ordinarySkill = 80000055;
         odrinaryMaxLevel = 10;
      } else if ((skillid < 80000333 || skillid > 80000335) && skillid != 80000378) {
         if (skillid >= 80002759 && skillid <= 80002761) {
            ordinarySkill = 80002758;
            odrinaryMaxLevel = 6;
         } else if (skillid >= 80002763 && skillid <= 80002765) {
            ordinarySkill = 80002762;
            odrinaryMaxLevel = 6;
         } else if (skillid >= 80002767 && skillid <= 80002769) {
            ordinarySkill = 80002766;
            odrinaryMaxLevel = 6;
         } else if (skillid >= 80002771 && skillid <= 80002773) {
            ordinarySkill = 80002770;
            odrinaryMaxLevel = 6;
         } else if (skillid >= 80002775 && skillid <= 80002776 || skillid == 80000000) {
            ordinarySkill = 80002774;
            odrinaryMaxLevel = 6;
         }
      } else {
         ordinarySkill = 80000329;
         odrinaryMaxLevel = 8;
      }

      if (ordinarySkill > 0) {
         totalskilllv = skilllevel + c.getPlayer().getSkillLevel(ordinarySkill);
         c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(skillid), skilllevel, (byte)2);
         c.getSession().writeAndFlush(CWvsContext.Unlinkskillunlock(skillid, 1));
         c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(ordinarySkill), totalskilllv, odrinaryMaxLevel);
      } else {
         c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(skillid), skilllevel, (byte)2);
      }

      c.getSession().writeAndFlush(CWvsContext.Linkskill(skillid, sendid, c.getPlayer().getId(), skilllevel, totalskilllv));
      c.getPlayer().getStat().recalcLocalStats(c.getPlayer());
   }

   public static void IncreaseDuration(MapleCharacter player, int skillid) {
      if (skillid == 400051006 && player.bulletParty < 6) {
         SecondaryStatValueHolder bulletParty = player.checkBuffStatValueHolder(SecondaryStat.BulletParty);
         if (bulletParty != null) {
            ++player.bulletParty;
            player.updateEffect(bulletParty.effect, SecondaryStat.BulletParty, 1000);
         }
      }

   }

   public static void Respawn(MapleClient c) {
      MapleCharacter chr = c.getPlayer();
      MapleMap map;
      if (chr.getDeathCount() <= 0 && chr.liveCounts() <= 0) {
         chr.getStat().setHp(50L, chr);
         map = chr.getMap();
         MapleMap to = null;
         if (map.getForcedReturnId() != 999999999 && map.getForcedReturnMap() != null) {
            to = map.getForcedReturnMap();
         } else {
            to = map.getReturnMap();
         }

         chr.changeMap(to, to.getPortal(0));
      } else {
         map = chr.getMap();
         chr.changeMap(map, map.getPortal(0));
      }

   }

   public static void RainBowRushStart(MapleClient c) {
      if (c.getPlayer().isRainBowRush()) {
         c.getPlayer().setRainbowRushStart(System.currentTimeMillis());
         c.getSession().writeAndFlush(CField.UIPacket.getRainBowRushStart());
      }

   }

   public static void RainBowRushTimer(LittleEndianAccessor slea, MapleClient c) {
      if (c.getPlayer().isRainBowRush()) {
         int time = slea.readInt();
         c.getPlayer().setRainbowRushTime(time);
         if (time >= 127000) {
            RainBowRushDead(c);
         }
      }

   }

   public static void RainBowRushDead(MapleClient c) {
      if (c.getPlayer().isRainBowRush()) {
         int jam = 10 * c.getPlayer().getRainbowRushTime() / 1000 * 100 / 120 / 100;
         if (jam >= 10) {
            jam = 10;
         }

         c.getPlayer().AddStarDustCoin(2, jam);
         c.getSession().writeAndFlush(CField.UIPacket.getRainBowResult(jam, c.getPlayer().getRainbowRushTime()));
         c.getPlayer().setRainbowRushTime(0);
      }

   }

   public static void RainBowRushReturnMap(MapleClient c) {
      if (c.getPlayer().isRainBowRush()) {
         c.getPlayer().setRainBowRush(false);
         c.getPlayer().warp(ServerConstants.warpMap);
      }

   }

   public static void RespawnLucid(LittleEndianAccessor slea, MapleClient c) {
      MapleCharacter chr = c.getPlayer();
      slea.skip(1);
      if (chr.getDeathCount() <= 0 && chr.liveCounts() <= 0) {
         chr.getStat().setHp((long)((short)((int)chr.getStat().getCurrentMaxHp())), chr);
         MapleMap map = chr.getMap();
         MapleMap to = null;
         if (map.getForcedReturnId() != 999999999 && map.getForcedReturnMap() != null) {
            to = map.getForcedReturnMap();
         } else {
            to = map.getReturnMap();
         }

         chr.changeMap(to, to.getPortal(0));
      } else {
         boolean bufffreezer = slea.readByte() > 0 && chr.haveItem(5133000, 1, false, true);
         if (bufffreezer) {
            int buffFreezer;
            if (c.getPlayer().itemQuantity(5133000) > 0) {
               buffFreezer = 5133000;
            } else {
               buffFreezer = 5133001;
            }

            c.getPlayer().setUseBuffFreezer(true);
            boolean practice = false;
            if (c.getPlayer().getV("bossPractice") != null && Integer.parseInt(c.getPlayer().getV("bossPractice")) == 1) {
               practice = true;
            }

            if (!practice) {
               c.getPlayer().removeItem(buffFreezer, -1);
            }

            c.getSession().writeAndFlush(CField.buffFreezer(buffFreezer, practice));
         }

         MapleQuest.getInstance(1097).forceStart(chr, 0, bufffreezer ? "1" : "0");
         if (!chr.isUseBuffFreezer()) {
            chr.cancelAllBuffs_();
         }

         if (chr.getDeathCount() > 0) {
            c.getSession().writeAndFlush(CField.getDeathCount(chr.getDeathCount()));
         }

         chr.getStat().setHp(chr.getStat().getCurrentMaxHp(), chr);
         chr.getStat().setMp(chr.getStat().getCurrentMaxMp(chr), chr);
         chr.updateSingleStat(MapleStat.HP, chr.getStat().getCurrentMaxHp());
         chr.updateSingleStat(MapleStat.MP, chr.getStat().getCurrentMaxMp(chr));
         int respawnx;
         MaplePortal mp;
         switch(chr.getMapId()) {
         case 220080100:
         case 220080200:
         case 220080300:
            chr.getMap().broadcastMessage(CField.onUserTeleport(chr.getId(), -920, 160));
            break;
         case 350060400:
         case 350060500:
         case 350060600:
         case 350060700:
         case 350060800:
         case 350060900:
            respawnx = chr.getMapId() != 350060400 && chr.getMapId() != 350060700 ? -440 : -2;
            chr.getMap().broadcastMessage(CField.onUserTeleport(chr.getId(), respawnx, -23));
            if (chr.getMapId() == 350060400 || chr.getMapId() == 350060700) {
               Iterator var12 = chr.getMap().getAllMonster().iterator();

               while(var12.hasNext()) {
                  MapleMonster mob = (MapleMonster)var12.next();
                  if (mob.getId() != 8950100 && mob.getId() != 8950000) {
                     chr.getMap().killMonsterType(mob, 1);
                  }
               }
            }
            break;
         case 350160100:
         case 350160140:
            respawnx = chr.getMapId() != 350160100 && chr.getMapId() != 350160100 ? 187 : 212;
            chr.getMap().broadcastMessage(CField.onUserTeleport(chr.getId(), respawnx, 10));
            break;
         case 450004150:
         case 450004250:
         case 450004450:
         case 450004550:
         case 450004750:
         case 450004850:
            int x = chr.getMap().getId() != 450004150 && chr.getMap().getId() != 450004450 && chr.getMap().getId() != 450004750 ? (Randomizer.nextBoolean() ? 316 : 1027) : 157;
            int y = chr.getMap().getId() != 450004150 && chr.getMap().getId() != 450004450 && chr.getMap().getId() != 450004750 ? (Randomizer.nextBoolean() ? -855 : -842) : 48;
            chr.getMap().broadcastMessage(CField.onUserTeleport(chr.getId(), x, y));
            break;
         case 450008750:
            mp = chr.getMap().getPortal("sp");
            if (mp != null) {
               c.getSession().writeAndFlush(CField.onUserTeleport(mp.getPosition().x, chr.getPosition().y));
            } else {
               c.getSession().writeAndFlush(CField.onUserTeleport(chr.getPosition().x, chr.getPosition().y));
            }
            break;
         case 450013100:
            chr.getMap().broadcastMessage(CField.onUserTeleport(chr.getId(), -285, 85));
            break;
         case 450013300:
            chr.getMap().broadcastMessage(CField.onUserTeleport(chr.getId(), -461, 88));
            break;
         default:
            mp = chr.getMap().getPortal("sp");
            if (mp != null) {
               c.getSession().writeAndFlush(CField.onUserTeleport(mp.getPosition().x, mp.getPosition().y));
            } else {
               c.getSession().writeAndFlush(CField.onUserTeleport(chr.getPosition().x, chr.getPosition().y));
            }
         }

         SkillFactory.getSkill(80000329).getEffect(chr.getSkillLevel(80000329)).applyTo(chr, false);
         if (GameConstants.isDemonAvenger(chr.getJob())) {
            chr.updateExceed(chr.getExceed());
         }
      }

   }

   public static void megaSmasherRequest(LittleEndianAccessor slea, MapleClient c) {
      MapleCharacter chr = c.getPlayer();
      boolean start = slea.readByte() == 1;
      SecondaryStatEffect effect;
      if (start) {
         effect = SkillFactory.getSkill(400041007).getEffect(chr.getSkillLevel(400041007));
         chr.setSkillCustomInfo(400041007, System.currentTimeMillis(), 0L);
         chr.isMegaSmasherCharging = true;
         chr.getClient().send(CField.skillCooldown(400041007, effect.getCooldown(chr)));
         chr.addCooldown(400041007, System.currentTimeMillis(), (long)effect.getCooldown(chr));
      } else {
         if (!chr.isMegaSmasherCharging) {
            while(chr.getBuffedValue(400041007)) {
               chr.cancelEffect(chr.getBuffedEffect(400041007));
            }

            return;
         }

         chr.isMegaSmasherCharging = false;

         while(chr.getBuffedValue(400041007)) {
            chr.cancelEffect(chr.getBuffedEffect(400041007));
         }

         effect = SkillFactory.getSkill(400041007).getEffect(chr.getSkillLevel(400041007));
         int maxChargeTime = effect.getDuration() + effect.getZ() * 1000;
         int chargeTime = Math.min(maxChargeTime, effect.getDuration() + (int)((System.currentTimeMillis() - chr.getSkillCustomValue0(400041007)) / (long)(effect.getY() * 1000) * 1000L));
         effect.applyTo(chr, false, chargeTime);
         chr.setBuffedValue(SecondaryStat.MegaSmasher, 400041007, 1);
         chr.removeSkillCustomInfo(400041007);
         chr.getClient().getSession().writeAndFlush(CField.skillCooldown(400041007, effect.getCooldown(chr)));
         chr.addCooldown(400041007, System.currentTimeMillis(), (long)effect.getCooldown(chr));
      }

   }

   public static void SoulMatch(LittleEndianAccessor slea, MapleClient c) {
      slea.skip(4);
      int state = slea.readInt();
      if (state == 1) {
         c.getPlayer().dropMessage(6, "보스 입장이 시작됩니다.");
      }

      Iterator var3 = c.getChannelServer().getSoulmatch().iterator();

      while(var3.hasNext()) {
         List<Pair<Integer, MapleCharacter>> souls = (List)var3.next();
         Iterator var5 = souls.iterator();

         while(var5.hasNext()) {
            Pair<Integer, MapleCharacter> soulz = (Pair)var5.next();
            if (((MapleCharacter)soulz.right).equals(c.getPlayer())) {
               c.getChannelServer().getSoulmatch().remove(souls);
            }
         }
      }

      c.getSession().writeAndFlush(CField.UIPacket.closeUI(184));
   }

   public static void DailyGift(MapleClient c) {
      int date = Integer.parseInt(c.getKeyValue("dailyGiftDay"));
      int complete = Integer.parseInt(c.getKeyValue("dailyGiftComplete"));
      if (complete == 0) {
         if (date >= GameConstants.dailyItems.size()) {
            c.getSession().writeAndFlush(CField.dailyGift(c.getPlayer(), 3, 0));
            return;
         }

         DailyGiftItemInfo item = (DailyGiftItemInfo)GameConstants.dailyItems.get(date);
         int itemId = item.getItemId();
         int quantity = item.getQuantity();
         if (item.getItemId() == 0 && item.getSN() > 0) {
            CashItemInfo cashItem = CashItemFactory.getInstance().getItem(item.getSN());
            itemId = cashItem.getId();
            quantity = cashItem.getCount();
         }

         if (itemId == 4310291) {
            c.getPlayer().AddStarDustCoin(1, quantity);
         } else {
            if (!MapleInventoryManipulator.checkSpace(c, itemId, quantity, "")) {
               c.getSession().writeAndFlush(CField.dailyGift(c.getPlayer(), 7, 0));
               return;
            }

            Item addItem;
            if (GameConstants.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
               addItem = MapleItemInformationProvider.getInstance().getEquipById(itemId);
            } else {
               addItem = new Item(itemId, (short)0, (short)quantity, 0);
            }

            if (MapleItemInformationProvider.getInstance().isCash(itemId)) {
               addItem.setUniqueId(MapleInventoryIdentifier.getInstance());
            }

            MapleInventoryManipulator.addbyItem(c, addItem);
         }

         c.setKeyValue("dailyGiftDay", String.valueOf(date + 1));
         c.setKeyValue("dailyGiftComplete", "1");
         Channel var10000 = c.getSession();
         String var10001 = c.getKeyValue("dailyGiftComplete");
         var10000.writeAndFlush(CWvsContext.updateDailyGift("count=" + var10001 + ";day=" + c.getKeyValue("dailyGiftDay") + ";date=" + c.getPlayer().getKeyValue(16700, "date")));
         c.getSession().writeAndFlush(CField.dailyGift(c.getPlayer(), 2, itemId));
         c.getSession().writeAndFlush(CField.dailyGift(c.getPlayer(), 0, itemId));
      } else {
         c.getSession().writeAndFlush(CField.dailyGift(c.getPlayer(), 5, 0));
      }

   }

   public static void ShadowServentExtend(LittleEndianAccessor slea, MapleClient c) {
      int skillid = slea.readInt();
      Iterator var3 = c.getPlayer().getSummons().iterator();

      while(var3.hasNext()) {
         MapleSummon s = (MapleSummon)var3.next();
         if (s.getMovementType() == SummonMovementType.ShadowServantExtend && s.getChangePositionCount() < 3) {
            s.setChangePositionCount((byte)(s.getChangePositionCount() + 1));
            Point summonpos = s.getTruePosition();
            c.getSession().writeAndFlush(CField.ShadowServentExtend(summonpos));
            c.getSession().writeAndFlush(CField.ShadowServentRefresh(c.getPlayer(), s, 3 - s.getChangePositionCount()));
            c.send(CField.EffectPacket.showEffect(c.getPlayer(), skillid, skillid, 10, 0, 0, (byte)0, true, (Point)null, (String)null, (Item)null));
            c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.EffectPacket.showEffect(c.getPlayer(), skillid, skillid, 10, 0, 0, (byte)0, false, (Point)null, (String)null, (Item)null), false);
            s.setPosition(c.getPlayer().getTruePosition());
            c.getPlayer().setPosition(summonpos);
         }
      }

   }

   public static void joker(MapleClient c) {
      c.getPlayer().setSkillCustomInfo(400041010, c.getPlayer().getSkillCustomValue0(400041010) + 1L, 0L);

      for(int i = 0; i < 14; ++i) {
         MapleAtom atom = new MapleAtom(false, c.getPlayer().getId(), 1, true, 400041010, c.getPlayer().getTruePosition().x, c.getPlayer().getTruePosition().y);
         ForceAtom forceAtom = new ForceAtom(2, Randomizer.rand(16, 26), Randomizer.rand(7, 11), Randomizer.nextInt(4) + 5, 0);
         forceAtom.setnAttackCount(forceAtom.getnAttackCount() + 1);
         atom.addForceAtom(forceAtom);
         c.getPlayer().getMap().spawnMapleAtom(atom);
      }

   }

   public static void activePrayBuff(MapleClient c) {
      MapleCharacter player = c.getPlayer();
      MapleParty party = player.getParty();
      SecondaryStatEffect effect = player.getBuffedEffect(SecondaryStat.Pray);
      Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
      long starttime = System.currentTimeMillis();
      if (effect != null) {
         int int_ = player.getStat().getTotalInt();
         int at = 0;
         int incPMdR = effect.getQ() + Math.min(effect.getW(), int_ / effect.getQ2());
         int incRecovery = int_ / effect.getY();
         int incBooster = Math.min(int_ / effect.getU(), 3);
         int incRecoveryHP = Math.min(effect.getZ(), incRecovery + effect.getX());
         int incRecoveryMP = Math.min(effect.getZ(), incRecovery + effect.getX());
         incRecoveryHP = (int)((double)(player.getStat().getCurrentMaxHp() * (long)incRecoveryHP) * 0.01D);
         incRecoveryMP = (int)((double)(player.getStat().getCurrentMaxMp(player) * (long)incRecoveryMP) * 0.01D);
         if (player.isAlive()) {
            player.addMPHP((long)incRecoveryHP, (long)incRecoveryMP);
         }

         if (c.getPlayer().getKeyValue(800023, "indiepmer") > 0L) {
            at = (int)((long)at + c.getPlayer().getKeyValue(800023, "indiepmer"));
         }

         if (incPMdR > 0) {
            statups.put(SecondaryStat.IndiePmdR, new Pair(at + Integer.valueOf(incPMdR), 2000));
         }

         if (incBooster > 0) {
            statups.put(SecondaryStat.IndieBooster, new Pair(-incBooster, 2000));
         }

         player.cancelEffectFromBuffStat(SecondaryStat.IndiePmdR, effect.getSourceId());
         player.cancelEffectFromBuffStat(SecondaryStat.IndieBooster, effect.getSourceId());
         Iterator var14 = statups.entrySet().iterator();

         while(var14.hasNext()) {
            Entry<SecondaryStat, Pair<Integer, Integer>> statup = (Entry)var14.next();
            player.registerEffect(effect, starttime, statup, false, player.getId());
         }

         c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, effect, player));
         if (party != null) {
            var14 = party.getMembers().iterator();

            while(true) {
               MapleCharacter victim;
               do {
                  while(true) {
                     do {
                        MaplePartyCharacter pc;
                        do {
                           do {
                              do {
                                 if (!var14.hasNext()) {
                                    return;
                                 }

                                 pc = (MaplePartyCharacter)var14.next();
                              } while(!pc.isOnline());
                           } while(pc.getMapid() != player.getMapId());
                        } while(pc.getChannel() != c.getChannel());

                        victim = c.getChannelServer().getPlayerStorage().getCharacterByName(pc.getName());
                     } while(victim == null);

                     if (victim.isAlive() && player.getId() != victim.getId()) {
                        break;
                     }

                     if (!effect.calculateBoundingBox(player.getTruePosition(), player.isFacingLeft()).contains(victim.getTruePosition()) && victim.getBuffedValue(400021003)) {
                        victim.cancelEffectFromBuffStat(SecondaryStat.IndiePmdR, effect.getSourceId());
                        victim.cancelEffectFromBuffStat(SecondaryStat.IndieBooster, effect.getSourceId());
                     }
                  }
               } while(!effect.calculateBoundingBox(player.getTruePosition(), player.isFacingLeft()).contains(victim.getTruePosition()));

               incRecoveryHP = (int)((double)(victim.getStat().getMaxHp() * (long)incRecoveryHP) * 0.01D);
               incRecoveryMP = (int)((double)(victim.getStat().getMaxMp() * (long)incRecoveryMP) * 0.01D);
               victim.addMPHP((long)incRecoveryHP, (long)incRecoveryMP);
               statups.clear();
               if (incPMdR > 0) {
                  statups.put(SecondaryStat.IndiePmdR, new Pair(incPMdR, 2000));
               }

               if (incBooster > 0) {
                  statups.put(SecondaryStat.IndieBooster, new Pair(-incBooster, 2000));
               }

               victim.cancelEffectFromBuffStat(SecondaryStat.IndiePmdR, effect.getSourceId());
               victim.cancelEffectFromBuffStat(SecondaryStat.IndieBooster, effect.getSourceId());
               Iterator var17 = statups.entrySet().iterator();

               while(var17.hasNext()) {
                  Entry<SecondaryStat, Pair<Integer, Integer>> statup = (Entry)var17.next();
                  victim.registerEffect(effect, starttime, statup, false, victim.getId());
               }

               victim.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, effect, player));
            }
         }
      }

   }

   public static void InhumanSpeed(LittleEndianAccessor slea, MapleClient c) {
      int objectId = slea.readInt();
      slea.readInt();
      if (c.getPlayer().getBuffedValue(400031020) || c.getPlayer().getBuffedValue(400031021)) {
         MapleAtom atom = new MapleAtom(false, c.getPlayer().getId(), 31, true, c.getPlayer().getBuffedValue(400031020) ? 400031020 : 400031021, 0, 0);
         atom.setDwFirstTargetId(objectId);
         atom.addForceAtom(new ForceAtom(1, 12, 15, 70, 0));
         c.getPlayer().getMap().spawnMapleAtom(atom);
      }

   }

   public static void CreateKinesisPsychicArea(LittleEndianAccessor rm, MapleClient c) {
      int nAction = rm.readInt();
      int ActionSpeed = rm.readInt();
      int PsychicAreaKey = rm.readInt();
      int LocalKey = rm.readInt();
      int SkillID = rm.readInt();
      short SLV = rm.readShort();
      int DurationTime = rm.readInt();
      byte second = rm.readByte();
      short SkeletonFieldPathIdx = rm.readShort();
      short SkeletonAniIdx = rm.readShort();
      short SkeletonLoop = rm.readShort();
      int mask8 = rm.readInt();
      int mask9 = rm.readInt();
      SecondaryStatEffect eff = SkillFactory.getSkill(SkillID).getEffect(SLV);
      eff.applyTo(c.getPlayer(), false);
      c.getPlayer().getMap().broadcastMessage(CWvsContext.OnCreatePsychicArea(c.getPlayer().getId(), nAction, ActionSpeed, LocalKey, SkillID, SLV, PsychicAreaKey, DurationTime, second, SkeletonFieldPathIdx, SkeletonAniIdx, SkeletonLoop, mask8, mask9));
      if (SkillID != 142101009) {
         c.getPlayer().givePPoint(SkillID);
      }

      if (SkillID == 142121005) {
         c.getPlayer().setSkillCustomInfo(SkillID, 1L, 0L);
      }

      if (eff.getCooldown(c.getPlayer()) > 0) {
         c.getSession().writeAndFlush(CField.skillCooldown(SkillID, eff.getCooldown(c.getPlayer())));
         c.getPlayer().addCooldown(SkillID, System.currentTimeMillis(), (long)eff.getCooldown(c.getPlayer()));
      }

   }

   public static void touchMist(LittleEndianAccessor slea, MapleClient c) {
      MapleCharacter chr = c.getPlayer();
      if (chr != null && chr.getMap() != null) {
         slea.readByte();
         int objectId = slea.readInt();
         int skillId = slea.readInt();
         int x = slea.readInt();
         int y = slea.readInt();
         MapleMist mist = (MapleMist)chr.getMap().getMapObject(objectId, MapleMapObjectType.MIST);
         if (mist != null) {
            switch(skillId) {
            case 2311011:
               if (chr.getBuffedEffect(SecondaryStat.DebuffIncHp) == null) {
                  if (c.getPlayer().getHolyPountinOid() != objectId) {
                     c.getPlayer().setHolyPountin((byte)0);
                  } else {
                     c.getPlayer().setHolyPountin((byte)(c.getPlayer().getHolyPountin() + 1));
                  }

                  c.getPlayer().addHP(c.getPlayer().getStat().getMaxHp() / 100L * (long)mist.getSource().getX());
                  c.getPlayer().setHolyPountinOid(objectId);
               }
               break;
            case 2321015:
               chr.getMap().removeMist(mist);
               int plusduration = chr.getStat().getTotalInt() / 2500;
               c.getPlayer().addHP(c.getPlayer().getStat().getMaxHp() / 20L + c.getPlayer().getStat().getMaxHp() / 20L * (long)plusduration);
               break;
            case 162111000:
               SkillFactory.getSkill(80003059).getEffect(mist.getOwner().getSkillLevel(skillId)).applyTo(mist.getOwner(), chr);
               break;
            case 400051076:
               chr.getMap().removeMist(mist);
               if (chr.getSkillLevel(400051074) > 0) {
                  SkillFactory.getSkill(400051077).getEffect(chr.getSkillLevel(400051074)).applyTo(chr, false);
               }
            }

         }
      }
   }

   public static void UpdateJaguar(LittleEndianAccessor slea, MapleClient c) {
      slea.skip(4);
      int changed = slea.readInt();
      c.getPlayer().updateInfoQuest(123456, String.valueOf((changed + 1) * 10));
      c.getSession().writeAndFlush(CWvsContext.updateJaguar(c.getPlayer()));
      c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
   }

   public static void auraWeapon(LittleEndianAccessor slea, MapleClient c) {
      int skillid = slea.readInt();
   }

   public static void removeMist(LittleEndianAccessor slea, MapleClient c) {
      int skillid = slea.readInt();
      c.getPlayer().getMap().removeMist(skillid);
      c.getPlayer().cancelEffectFromBuffStat(SecondaryStat.IndiePadR, 80001455);
      c.getPlayer().cancelEffectFromBuffStat(SecondaryStat.IndieMadR, 80001455);
      if (skillid == 400031012) {
         ArrayList<Triple<Integer, Integer, Integer>> finalMobList = new ArrayList();
         c.getSession().writeAndFlush(CField.bonusAttackRequest(skillid + 1, finalMobList, true, 0));
      }

   }

   public static void PeaceMaker(LittleEndianAccessor slea, MapleClient c) {
      int skillid = slea.readInt();
      slea.skip(8);
      Point pos1 = slea.readIntPos();
      Point pos2 = slea.readIntPos();
      int count = slea.readInt();
      SkillFactory.getSkill(skillid).getEffect(c.getPlayer().getSkillLevel(skillid));
      int var10000 = SkillFactory.getSkill(400021070).getEffect(c.getPlayer().getSkillLevel(40021070)).getQ2() + slea.readInt() * SkillFactory.getSkill(400021070).getEffect(c.getPlayer().getSkillLevel(40021070)).getW2();
   }

   public static void PeaceMaker2(LittleEndianAccessor slea, MapleClient c) {
      int skillid = slea.readInt();
      c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.skillCancel(c.getPlayer(), skillid), false);
   }

   public static void DemonFrenzy(MapleClient c) {
      if (c.getPlayer().getBuffedEffect(400011010) != null) {
         SecondaryStatEffect Frenzy = c.getPlayer().getBuffedEffect(400011010);
         MapleFoothold fh = c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getTruePosition());
         if (Frenzy != null && c.getPlayer().getSkillCustomValue(400010010) == null && c.getPlayer().getPosition().y >= fh.getY1() && c.getPlayer().isAlive()) {
            if (c.getPlayer().getStat().getHp() > c.getPlayer().getStat().getCurrentMaxHp() * (long)Frenzy.getQ2() / 100L) {
               long hp = (long)Frenzy.getY();
               if (c.getPlayer().getStat().getHp() - hp > 0L) {
                  c.getPlayer().addHP(-hp, false, true);
               }
            }

            Point pos = new Point(c.getPlayer().getPosition().x, c.getPlayer().getPosition().y);
            SecondaryStatEffect a = SkillFactory.getSkill(400010010).getEffect(c.getPlayer().getSkillLevel(400010010));
            Rectangle bounds = a.calculateBoundingBox(new Point(c.getPlayer().getTruePosition().x, c.getPlayer().getTruePosition().y), c.getPlayer().isFacingLeft());
            MapleMist mist = new MapleMist(new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height), c.getPlayer(), a, 5000, (byte)(c.getPlayer().isFacingLeft() ? 1 : 0));
            mist.setPosition(pos);
            mist.setDelay(0);
            c.getPlayer().getMap().spawnMist(mist, false);
            c.getPlayer().setSkillCustomInfo(400010010, 0L, 1000L);
            Frenzy.applyTo(c.getPlayer(), false);
         }
      }
   }

   public static void keydownSkillManagement(LittleEndianAccessor slea, MapleClient c) {
      int skillid = slea.readInt();
      MapleCharacter chr = c.getPlayer();
      if (chr != null) {
         Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
         SecondaryStatEffect eff;
         if (skillid >= 3321034 && skillid <= 3321040) {
            eff = SkillFactory.getSkill(skillid).getEffect(chr.getSkillLevel(GameConstants.getLinkedSkill(skillid)));
            chr.energy -= eff.getForceCon();
            if (chr.energy < 0) {
               chr.energy = 0;
            }

            statups.put(SecondaryStat.RelikGauge, new Pair(chr.energy, 0));
            c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, (SecondaryStatEffect)null, chr));
         } else if (skillid == 400021086) {
            Vmatrixstackbuff(c, true, (LittleEndianAccessor)null);
         } else if (chr.getBuffedEffect(SecondaryStat.GrandCrossSize, skillid) != null) {
            eff = chr.getBuffedEffect(skillid);
            chr.addHP((long)(-((int)((double)chr.getStat().getHp() * eff.getT() / 100.0D))));
            Integer value = c.getPlayer().getBuffedValue(SecondaryStat.GrandCrossSize);
            if (c.getPlayer().getBuffLimit(400011072) <= 7000L && value == 1) {
               chr.setBuffedValue(SecondaryStat.GrandCrossSize, 2);
               statups.put(SecondaryStat.GrandCrossSize, new Pair(2, (int)chr.getBuffLimit(skillid)));
               statups.put(SecondaryStat.Speed, new Pair(-60, (int)chr.getBuffLimit(skillid)));
               c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, eff, chr));
               c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CWvsContext.BuffPacket.giveForeignBuff(c.getPlayer(), statups, eff), false);
            }
         } else if (skillid == 63121008) {
            eff = SkillFactory.getSkill(skillid).getEffect(chr.getSkillLevel(skillid));
            chr.addMP(-(chr.getStat().getCurrentMaxMp(chr) / 100L * (long)eff.getMPRCon()));
            if (chr.getSkillLevel(63120039) > 0) {
               eff = SkillFactory.getSkill(63120039).getEffect(chr.getSkillLevel(63120039));
               chr.addHP(chr.getStat().getCurrentMaxHp() / 100L * (long)eff.getX());
            }
         }

      }
   }

   public static void subSkillEffect(LittleEndianAccessor slea, MapleCharacter chr) {
      int newx = slea.readInt();
      int newy = slea.readInt();
      int oldx = slea.readInt();
      int oldy = slea.readInt();
      int delay = slea.readInt();
      int skillId = slea.readInt();
      int unk = slea.readInt();
      byte facingleft = slea.readByte();
      slea.skip(4);
      int objectId = slea.readInt();
      slea.skip(2);
      int num = -1;
      if (slea.available() >= 8L) {
         slea.readInt();
         num = slea.readInt();
      }

      Skill skill = SkillFactory.getSkill(GameConstants.getLinkedSkill(skillId));
      if (chr != null && skill != null && chr.getMap() != null) {
         byte skilllevel_serv = (byte)chr.getTotalSkillLevel(skill);
         if (skillId != 400031003 && skillId != 400031004) {
            if (skillId == 400031036) {
               chr.setSkillCustomInfo(400031036, 1L, 0L);
               MapleCharacter.렐릭게이지(chr.getClient(), skillId);
            } else if (skillId == 400031067) {
               chr.setSkillCustomInfo(400031067, 1L, 0L);
               MapleCharacter.렐릭게이지(chr.getClient(), skillId);
            } else if (skillId == 61111100 || skillId == 61111218 || skillId == 61111113) {
               if (chr.getSkillCustomValue(61111100) == null) {
                  chr.setSkillCustomInfo(61111100, (long)objectId, 0L);
               } else if (chr.getSkillCustomValue(61111110) == null) {
                  chr.setSkillCustomInfo(61111110, (long)objectId, 0L);
               } else if (chr.getSkillCustomValue(61111100) != null && chr.getSkillCustomValue(61111110) != null) {
                  chr.getMap().broadcastMessage(CField.removeProjectile((int)chr.getSkillCustomValue0(61111100)));
                  chr.setSkillCustomInfo(61111100, chr.getSkillCustomValue0(61111110), 0L);
                  chr.setSkillCustomInfo(61111110, (long)objectId, 0L);
               }
            }
         } else {
            SecondaryStatEffect yoyo = SkillFactory.getSkill(400031003).getEffect(1);
            if (chr.getSkillCustomValue(400031333) == null) {
               chr.setSkillCustomInfo(400031333, 0L, (long)(yoyo.getX() * 1000));
            }

            if (chr.getSkillCustomValue(400031334) != null) {
               chr.getMap().broadcastMessage(CField.removeProjectile((int)chr.getSkillCustomValue0(400031334)));
               chr.removeSkillCustomInfo(400031334);
            }

            if (chr.getSkillCustomValue(400031334) == null) {
               chr.setSkillCustomInfo(400031334, (long)objectId, 10000L);
            }

            Vmatrixstackbuff(chr.getClient(), true, slea, skillId == 400031003 ? 1 : 2);
         }

         chr.getMap().broadcastMessage(chr, CField.showProjectileEffect(chr, newx, newy, delay, skillId, skilllevel_serv, unk, facingleft, objectId, num), false);
         if (skillId != 14111024 && skillId != 14101028) {
            chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, skillId, 1, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), true, chr.getTruePosition(), (String)null, (Item)null));
         }

         if (!GameConstants.isLinkMap(chr.getMapId())) {
            chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 0, skillId, 1, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), false, chr.getTruePosition(), (String)null, (Item)null), false);
         }

         if (skill.getEffect(skilllevel_serv).getCooldown(chr) > 0 && chr.getCooldownLimit(skillId) == 0L) {
            chr.giveCoolDowns(skillId, System.currentTimeMillis(), (long)skill.getEffect(skilllevel_serv).getCooldown(chr));
            chr.getClient().getSession().writeAndFlush(CField.skillCooldown(skillId, skill.getEffect(skilllevel_serv).getCooldown(chr)));
         }

         if (skillId == 64101002) {
            chr.wingDagger = true;
         }

         skill.getEffect(skilllevel_serv).applyTo(chr, false);
      }
   }

   public static void cancelSubEffect(LittleEndianAccessor slea, MapleCharacter chr) {
      int objid = slea.readInt();
      chr.getMap().broadcastMessage(chr, CField.removeProjectileEffect(chr.getId(), objid), false);
      slea.readByte();
      int skillid = slea.readInt();
      if (skillid == 61111100) {
         if (chr.getSkillCustomValue(61111100) != null) {
            if (chr.getSkillCustomValue0(61111100) == (long)objid) {
               chr.removeSkillCustomInfo(61111100);
            }
         } else if (chr.getSkillCustomValue(61111110) != null && chr.getSkillCustomValue0(61111110) == (long)objid) {
            chr.removeSkillCustomInfo(61111110);
         }
      } else if (skillid == 400031036) {
         SkillFactory.getSkill(3311009).getEffect(chr.getSkillLevel(3311009)).applyTo(chr);
      }

   }

   public static void changeSubEffect(LittleEndianAccessor slea, MapleCharacter chr) {
      chr.getMap().broadcastMessage(chr, CField.updateProjectileEffect(chr.getId(), slea.readInt(), slea.readInt(), slea.readInt(), slea.readInt(), slea.readByte()), false);
   }

   public static void LinkofArk(LittleEndianAccessor slea, MapleCharacter player) {
   }

   public static void FlowOfFight(MapleCharacter player) {
      if (player != null) {
         if (player.getSkillLevel(80000268) > 0) {
            player.FlowofFight = Math.min(6, player.FlowofFight + 1);
            SkillFactory.getSkill(80000268).getEffect(player.getSkillLevel(80000268)).applyTo(player, false);
         } else if (player.getSkillLevel(150000017) > 0) {
            player.FlowofFight = Math.min(6, player.FlowofFight + 1);
            SkillFactory.getSkill(150000017).getEffect(player.getSkillLevel(150000017)).applyTo(player, false);
         }
      }

   }

   public static void TowerChair(LittleEndianAccessor slea, MapleClient c) {
      List<Integer> chairs = new ArrayList();

      int i;
      for(int a = 0; a < 6; ++a) {
         i = slea.readInt();
         if (i == 0) {
            break;
         }

         chairs.add(i);
      }

      StringBuilder sb = new StringBuilder();

      for(i = 0; i < chairs.size(); ++i) {
         sb.append(i);
         sb.append('=');
         sb.append(chairs.get(i));
         if (i != chairs.size() - 1) {
            sb.append(';');
         }
      }

      c.getPlayer().updateInfoQuest(7266, sb.toString());
      c.getSession().writeAndFlush(SLFCGPacket.TowerChairSaveDone());
   }

   public static void HandleCellClick(int number, MapleClient c) {
      if (c.getPlayer().getBingoGame().getRanking().contains(c.getPlayer())) {
      }

      int[][] table = c.getPlayer().getBingoGame().getTable(c.getPlayer());
      c.getSession().writeAndFlush(SLFCGPacket.BingoCheckNumber(number));
      int jj = false;

      int temp;
      int crossCnt;
      for(temp = 0; temp < 5; ++temp) {
         for(crossCnt = 0; crossCnt < 5; ++crossCnt) {
            if (table[crossCnt][temp] == number) {
               table[crossCnt][temp] = 255;
            }
         }
      }

      temp = 0;

      int rcrossCnt;
      for(crossCnt = 0; crossCnt < 5; ++crossCnt) {
         for(rcrossCnt = 0; rcrossCnt < 5; ++rcrossCnt) {
            if (table[rcrossCnt][crossCnt] == 255 || table[rcrossCnt][crossCnt] == 0) {
               ++temp;
            }
         }

         if (temp == 5) {
            c.getSession().writeAndFlush(SLFCGPacket.BingoDrawLine(crossCnt * 5, 0, number));
         }

         temp = 0;
      }

      temp = 0;

      for(crossCnt = 0; crossCnt < 5; ++crossCnt) {
         for(rcrossCnt = 0; rcrossCnt < 5; ++rcrossCnt) {
            if (table[crossCnt][rcrossCnt] == 255 || table[crossCnt][rcrossCnt] == 0) {
               ++temp;
            }
         }

         if (temp == 5) {
            c.getSession().writeAndFlush(SLFCGPacket.BingoDrawLine(crossCnt, 1, number));
         }

         temp = 0;
      }

      crossCnt = 0;
      rcrossCnt = 0;

      for(int i = 0; i < 5; ++i) {
         if (table[i][i] == 255 || table[i][i] == 0) {
            ++crossCnt;
         }

         if (table[i][4 - i] == 255 || table[i][4 - i] == 0) {
            ++rcrossCnt;
         }

         if (crossCnt == 5) {
            c.getSession().writeAndFlush(SLFCGPacket.BingoDrawLine(1, 2, number));
         }

         if (rcrossCnt == 5) {
            c.getSession().writeAndFlush(SLFCGPacket.BingoDrawLine(1, 3, number));
         }
      }

   }

   public static void HandleHundredDetectiveGame(LittleEndianAccessor slea, MapleClient c) {
      String input = String.valueOf(slea.readInt());
      DetectiveGame game = c.getPlayer().getDetectiveGame();
      game.getAnswer(c.getPlayer());
      String Answer = String.valueOf(c.getPlayer().getDetectiveGame().getAnswer(c.getPlayer()));
      int result = 0;

      for(int a = 0; a < 3; ++a) {
         char inputchar = input.charAt(a);
         char answerchar = Answer.charAt(a);
         if (inputchar == answerchar) {
            result += 10;
         } else if (Answer.contains(String.valueOf(inputchar))) {
            ++result;
         }
      }

      c.getPlayer().getDetectiveGame().addAttempt(c.getPlayer());
      c.getSession().writeAndFlush(SLFCGPacket.HundredDetectiveGameResult(Integer.valueOf(input), result));
      if (result == 30) {
         c.getPlayer().getDetectiveGame().addRank(c.getPlayer());
      }

   }

   public static void HandlePlatformerEnter(LittleEndianAccessor slea, MapleClient c) {
      int Stage = slea.readInt();
      int Map = 993001000 + Stage * 10;
      c.getPlayer().warp(Map);
      c.getSession().writeAndFlush(CField.getClock(600));
      if (c.getPlayer().getPlatformerTimer() != null) {
         c.getPlayer().getPlatformerTimer().cancel(false);
      }

      ScheduledFuture<?> a = Timer.ShowTimer.getInstance().schedule(() -> {
         if (c.getPlayer().getMapId() == Map) {
            c.getPlayer().warp(993001000);
         }

         c.getPlayer().setPlatformerTimer((ScheduledFuture)null);
      }, 600000L);
      c.getPlayer().setPlatformerTimer(a);
      c.getPlayer().setPlatformerStageEnter(System.currentTimeMillis());
      c.getSession().writeAndFlush(SLFCGPacket.PlatformerStageInfo(Stage));
      c.getSession().writeAndFlush(SLFCGPacket.playSE("Sound/MiniGame.img/multiBingo/start"));
      c.getSession().writeAndFlush(CField.environmentChange("event/start", 19));
      c.getSession().writeAndFlush(CField.UIPacket.closeUI(1112));
      MapleCharacter var10000 = c.getPlayer();
      long var10003 = c.getPlayer().getKeyValue(18838, "count");
      var10000.setKeyValue(18838, "count", (var10003 - 1L).makeConcatWithConstants<invokedynamic>(var10003 - 1L));
      switch(Stage) {
      case 1:
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(13, 0, 1000, 400, 0));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(11, 5));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070203, 2000, "대시는 가고 싶은 방향으로 방향키 연속! 두 번! 이다...후후...", ""));
         c.getSession().writeAndFlush(CField.enforceMSG("대시를 사용하여 골인 지점으로 가는거다!?", 215, 5000));
         break;
      case 2:
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(13, 0, 1000, 400, 0));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(11, 5));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070201, 2000, "앞의 장애물은 대시 중 점프로 쉽게 넘을 수 있을거야!", ""));
         c.getSession().writeAndFlush(CField.enforceMSG("대시 중 점프를 하면 높이, 멀리 뛸 수 있어!", 214, 5000));
         break;
      case 3:
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(13, 0, 1000, 400, 0));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(11, 5));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070201, 2000, "대시 중 방향키 위를 먼저 누르고 점프하면 더 쉬워! 편한 방식을 찾아 보자", ""));
         c.getSession().writeAndFlush(CField.enforceMSG("점프 중 방향키 위를 유지하면 높이 뛰어 오를 수 있어!", 214, 5000));
         break;
      case 4:
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(11, 3));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(13, 0, 0, 0, 200));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070202, 2000, "점프 중 방향키를 누르면 원하는 방향으로 공중 제어가 가능해.", ""));
         c.getSession().writeAndFlush(CField.enforceMSG("점프 중 좌우 방향키를 입력하면 공중에서 자세 제어가 가능해. 히힛.", 213, 5000));
         break;
      case 5:
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070201, 2000, "점프 중 방향키를 누르면 원하는 방향으로 공중 제어가 가능해.", ""));
         c.getSession().writeAndFlush(CField.enforceMSG("전에도 말했듯이 대시 중 방향키 위를 먼저 누르고 점프해도 괜찮아", 214, 5000));
         break;
      case 6:
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(11, 3));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(13, 0, 0, 0, 200));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070200, 2000, "음... 더러운 것에 닿는다고 해도 죽는건 아니다...", ""));
         c.getSession().writeAndFlush(CField.enforceMSG("공중에서 방향키 좌우를 눌러 더러운 것을 피해라.", 212, 5000));
         break;
      case 7:
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(13, 0, 1000, 400, 0));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(11, 5));
         break;
      case 8:
         NPCScriptManager.getInstance().start(c, "Obstacle");
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(15, 1000, 600, 0));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(13, 0, 1000, 300, 0));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(11, 5));
         break;
      case 9:
         NPCScriptManager.getInstance().start(c, "FootHoldMove");
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(13, 0, 1000, 400, 350));
         c.getSession().writeAndFlush(CField.enforceMSG("상하로 움직이는 발판을 이용해서 목적지에 도달해 봐. 히힛.", 213, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070202, 2000, "발판이 적절한 위치에 있을 때 점프하는게 좋을거야.\r\n막 뛰면 안 된다구.", ""));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070201, 2000, "점프는 예술과 기술이니까~", ""));
         break;
      case 10:
         c.getSession().writeAndFlush(CField.enforceMSG("상승하는 발판 위에서 더러운 것을 피해라.", 212, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070200, 2000, "우선 아래에 보이는 긴 발판에 올라서 봐라. 그럼 움직일 거야.", ""));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070200, 2000, "그 뒤는 알아서 해라.", ""));
         break;
      case 11:
         c.getSession().writeAndFlush(CField.enforceMSG("점프 기술을 활용하여 목적지에 도달해 보자~", 214, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070201, 2000, "점프 중 위키를 잘 활용해 봐. 의외로 지름길도 있으니 잘 찾아가고.", ""));
         break;
      case 12:
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(13, 0, 1000, 400, 0));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(11, 5));
         c.getSession().writeAndFlush(CField.enforceMSG("세 번째 발판에서 최대한 멀리 뛰어 봐~", 214, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070201, 2000, "세 번째 발판에서 최대한 멀리 뛰어 봐. 15미터 이상이면 합격!", ""));
         NPCScriptManager.getInstance().start(c, "Obstacle2");
         break;
      case 13:
         c.getSession().writeAndFlush(CField.enforceMSG("점프 기술을 활용해서 더러운 것을 피해 나아가라.", 212, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070200, 2000, "장애물과 장애물의 중간 지점 쯤에서 점프해 봐라.", ""));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070200, 2000, "못 넘는 곳이 있으면 점프중 방향키 위를 잊지 마.", ""));
         break;
      case 14:
         c.getSession().writeAndFlush(CField.enforceMSG("상승하는 발판 위에서 더러운 것을 피해라.", 212, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070200, 2000, "알지? 아래의 긴 발판으로 내려가 봐.", ""));
         break;
      case 15:
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(15, 1000, 600, 0));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(13, 0, 1000, 300, 0));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(11, 5));
         c.getSession().writeAndFlush(CField.enforceMSG("연속 대시 점프로 목적지에 도달하는거다!", 215, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070203, 2000, "대쉬 점프로 넘는거다!\r\n가끔은 연속 점프보다 잠깐 멈추는게 유리할 수도 있다!", ""));
         break;
      case 16:
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(11, 3));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(13, 0, 0, 0, 200));
         c.getSession().writeAndFlush(CField.enforceMSG("더러운 것을 피해서 목적지에 도달해 봐. 히힛.", 213, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070202, 2000, "여긴 두 가지 방법이 있다. 낙하하며 좌우키를 번갈아가며 누르는 것.", ""));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070202, 2000, "혹은 점프 중 반대 방향으로 힘을 주어 수직으로 떨어지는 것.", ""));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070202, 2000, "선택은 네 몫이야. 히힛.", ""));
         break;
      case 17:
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(13, 0, 1000, 400, 0));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(11, 5));
         c.getSession().writeAndFlush(CField.enforceMSG("낙하하는 운석을 피해 골인 지점까지 도달하는거다!", 215, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070203, 2000, "익숙한 곳일거다! 가끔은 과감히 맞으면서 돌파하는 것도 남자답지", ""));
         NPCScriptManager.getInstance().start(c, "Obstacle3");
         break;
      case 18:
         c.getSession().writeAndFlush(CField.enforceMSG("좌우의 곰을 30회 반복해서 터치하는거다!", 215, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070203, 2000, "좌우의 잠자는 곰에 반복해서 닿아라! 총 30회다!", ""));
         break;
      case 19:
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(13, 0, 1000, 400, 0));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(11, 5));
         c.getSession().writeAndFlush(CField.enforceMSG("나무를 이용하여 목적지에 도달해 봐~", 214, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070201, 2000, "큰 나무를 조심해. 부딪히면 아프니까... 나무 위로 올라설 수 있다는 걸 명심해 둬.", ""));
         break;
      case 20:
         c.getSession().writeAndFlush(CField.enforceMSG("발판이 사라지는 숲을 돌파해 봐~", 214, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070201, 2000, "인간들은 인내의 숲이란 곳에서 수련을 한다며?\r\n그런데 너무 쉬운 것 같더라. 히히.\r\n발판이 사라지는 정도면 재밌지 않겠어?.", ""));
         break;
      case 21:
         c.getSession().writeAndFlush(CField.enforceMSG("장애물을 피해 상쾌하게 달려가는거야! 히힛.", 213, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070202, 2000, "큰 나무는 그냥 지나갈 수 없어.\r\n하지만 점프로 올라갈 수 있지. 높이 점프할 땐 새를 조심하라구.", ""));
         break;
      case 22:
         c.getSession().writeAndFlush(CField.enforceMSG("공중 제어를 활용하여 발판을 올라 보자. 히힛.", 213, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070202, 2000, "힌트를 주자면... 높고 길게 뛰어서 힘이 다할때쯤 뛴 반대 방향으로 돌아와. 꽤 어려울거야.", ""));
         break;
      case 23:
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(15, 1000, 600, 0));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(13, 0, 1000, 300, 0));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(11, 5));
         c.getSession().writeAndFlush(CField.enforceMSG("종합적인 이동 능력을 시험해 봐라.", 212, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070200, 2000, "얼마나 수련이 잘 되었는지 확인해 봐라. 너무 괴로워서 울지도 모르겠군.", ""));
         break;
      case 24:
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(15, 1000, 600, 0));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(13, 0, 1000, 300, 0));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(11, 5));
         c.getSession().writeAndFlush(CField.enforceMSG("닿으면 사라지는 발판을 재빠르게 넘어가 봐. 히힛.", 213, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070202, 2000, "1초 정도 발판에 머무르면 사라지니까 조심해. 히힛.", ""));
         break;
      case 25:
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(13, 0, 1000, 400, 0));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(11, 5));
         c.getSession().writeAndFlush(CField.enforceMSG("연속 대시 점프로 장애물을 넘어가 봐~", 214, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070201, 2000, "너무 급하게 달려가지 말고 속도를 조절할 땐 조절해~", ""));
         break;
      case 26:
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(13, 0, 1000, 400, 0));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(11, 5));
         c.getSession().writeAndFlush(CField.enforceMSG("모든 점프 기술을 활용하여 더러운 것을 피해 가는거다!", 215, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070203, 2000, "더러운 것에 당하느니 천천히 생각하면서 가라!", ""));
         break;
      case 27:
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(11, 3));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(13, 0, 0, 0, 200));
         c.getSession().writeAndFlush(CField.enforceMSG("낙하 중 좌우 방향키로 공중 제어를 할 수 있다. 더러운 건 피해야지.", 212, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070200, 2000, "여긴... 음. 할 말이 없다. 하하.", ""));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070202, 2000, "실제로 돌파할 수 있긴 한거야?", ""));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070201, 2000, "한 사람이 있다고 하네요...", ""));
         break;
      case 28:
         c.getSession().writeAndFlush(CField.enforceMSG("점프 중 좌우 방향키로 소멸하는 발판을 돌파...해라.", 212, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070200, 2000, "나도 이런 정신나간 곳이 존재한다는게 믿기지 않는다.", ""));
         break;
      case 29:
         c.getSession().writeAndFlush(CField.enforceMSG("상승하는 발판에서 더러운 것을 피해라.", 212, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070200, 2000, "우선 아래에 있는 발판으로 내려가. 이 패턴 익숙하지?", ""));
         break;
      case 30:
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(15, 1000, 600, 0));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(13, 0, 0, 0, 200));
         c.getSession().writeAndFlush(CField.enforceMSG("독수리를 피해 골인 지점까지 도달해라.", 212, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070200, 2000, "독수리는 보기 보다 판정 영역이 작다. 할 수 있겠지?", ""));
         break;
      case 31:
         c.getSession().writeAndFlush(CField.enforceMSG("점프대로 점프하고 공중에서 제어해 봐. 히힛.", 213, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070202, 2000, "평범하게 생긴 발판도 밟으면 통통 튀어 오를 수 있다구. 히힛.", ""));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070202, 2000, "이 곳의 공중 제어는 조금 불쾌한 느낌일지도. 히힛.", ""));
         break;
      case 32:
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(13, 0, 1000, 400, 350));
         c.getSession().writeAndFlush(CField.enforceMSG("발판 위에서 중심을 잘 잡으며 잘 피해봐. 꼭 피해야 해.", 214, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070201, 2000, "발판 안은 위험해.", ""));
         NPCScriptManager.getInstance().start(c, "FootHoldMove2");
         break;
      case 33:
         c.getSession().writeAndFlush(CField.enforceMSG("더러운 것을 공중에서 화려하게 피해 봐~", 214, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070201, 2000, "상향 점프와 공중 제어를 잘 이용해야 해. 나처럼 섬세한 점프!", ""));
         break;
      case 34:
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(11, 3));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(13, 0, 0, 0, 200));
         c.getSession().writeAndFlush(CField.enforceMSG("공중에서 잘 움직이는 것 뿐이다. 알아서 피하고 싶을거다", 212, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070200, 2000, "세상엔 더러운 것도 위험한 것도 있다. 넌 이겨낼 수 있을거다.", ""));
         NPCScriptManager.getInstance().start(c, "Obstacle4");
         break;
      case 35:
         c.getSession().writeAndFlush(CField.enforceMSG("능력을 한 번 시험해 봐라.", 212, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070200, 2000, "이 곳은 종합적인 능력을 시험하는 곳이지. 길을 따라 가면 된다.", ""));
         break;
      case 36:
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(13, 0, 1000, 400, 350));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070203, 2000, "한 치의 실수도 용납되지 않는 곳이다. 잔인하군...", ""));
         NPCScriptManager.getInstance().start(c, "FootHoldMove3");
         break;
      case 37:
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(15, 1000, 600, 0));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(13, 0, 0, 0, 200));
         c.getSession().writeAndFlush(CField.enforceMSG("점프대를 이용해서 공중 자세 제어를 해 봐.", 212, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070201, 2000, "여기를 지나갈 수 있다면 트리플 악셀도 가능할거야.", ""));
         break;
      case 38:
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(15, 1000, 600, 0));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(13, 0, 1000, 300, 0));
         c.getSession().writeAndFlush(SLFCGPacket.CameraCtrl(11, 5));
         c.getSession().writeAndFlush(CField.enforceMSG("이동하는 발판 위에서 공중 제어 점프로 화려하게 피해 봐라.", 212, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070200, 2000, "앞에 보이는 발판에 올라서면 발판이 움직일거다.", ""));
         break;
      case 39:
         c.getSession().writeAndFlush(CField.enforceMSG("상승하는 발판 위에서 더러운 것을 피해내라.", 212, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070200, 2000, "일단 아래의 발판으로 내려가는건 알지?", ""));
         break;
      case 40:
         c.getSession().writeAndFlush(CField.enforceMSG("종합 시험이다. 멘탈 붕괴에 유의해라.", 212, 5000));
         c.getSession().writeAndFlush(SLFCGPacket.OnYellowDlg(9070202, 2000, "이 스테이지가 이렇게 어렵습니다~ 히힛.", ""));
      }

   }

   public static void HandlePlatformerExit(LittleEndianAccessor slea, MapleClient c) {
      switch(slea.readByte()) {
      case 12:
         if (c.getPlayer().getPlatformerTimer() != null) {
            c.getPlayer().getPlatformerTimer().cancel(false);
            c.getPlayer().setPlatformerTimer((ScheduledFuture)null);
         }

         c.getPlayer().setPlatformerStageEnter(0L);
         c.getPlayer().warp(993001000);
         return;
      case 18:
         c.removeClickedNPC();
         NPCScriptManager.getInstance().start(c, 2007, "union_rade");
         return;
      default:
         c.getPlayer().dropMessage(6, slea.readByte().makeConcatWithConstants<invokedynamic>(slea.readByte()));
      }
   }

   public static void HandleResolution(LittleEndianAccessor slea, MapleClient c) {
      switch(slea.readByte()) {
      case 1:
         c.getPlayer().setResolution(800, 600);
         return;
      case 2:
         c.getPlayer().setResolution(1024, 768);
         return;
      case 3:
         c.getPlayer().setResolution(1366, 768);
         return;
      case 4:
         c.getPlayer().setResolution(1280, 720);
         return;
      case 5:
         c.getPlayer().setResolution(1920, 1080);
         return;
      case 6:
         c.getPlayer().setResolution(1920, 1200);
         return;
      default:
         c.disconnect(true, false, false);
         c.getSession().close();
      }
   }

   public static void ExitSpecialGame(MapleClient c) throws SQLException {
      switch(c.getPlayer().getMapId()) {
      case 450001400:
         c.getPlayer().warp(450001550);
         c.send(CField.environmentChange("Map/Effect.img/killing/fail", 16));
         return;
      case 921171000:
         c.getPlayer().warp(921171100);
         long temp = Long.valueOf(c.getPlayer().getKeyValue(15901, "stage"));
         long temp2 = Long.valueOf(c.getPlayer().getKeyValue(15901, "selectedStage"));
         if (temp == temp2) {
            c.getSession().writeAndFlush(CField.environmentChange("Map/Effect2.img/event/gameover", 16));
         } else if (temp > temp2) {
            c.getSession().writeAndFlush(CField.environmentChange("Map/Effect3.img/hungryMuto/Clear", 16));
            c.getSession().writeAndFlush(SLFCGPacket.DreamBreakerMsg(temp - 1L + "스테이지 클리어!"));
         }

         DreamBreakerRank.EditRecord(c.getPlayer().getName(), Long.valueOf(c.getPlayer().getKeyValue(15901, "best")), Long.valueOf(c.getPlayer().getKeyValue(15901, "besttime")));
         return;
      case 921172000:
      case 921172100:
         Iterator var5 = c.getPlayer().getMap().getAllMonster().iterator();

         while(var5.hasNext()) {
            MapleMonster mon = (MapleMonster)var5.next();
            if (mon.getOwner() == c.getPlayer().getId()) {
               c.getPlayer().getMap().killMonster(mon, -1);
            }
         }

         c.getPlayer().setUnionEndTime(System.currentTimeMillis());
         if (c.getPlayer().getMapId() == 921172000) {
            c.getPlayer().warp(921172200);
         } else {
            c.getPlayer().warp(921172201);
         }

         return;
      case 921172300:
         c.getPlayer().warp(921172400);
         c.getSession().writeAndFlush(CField.environmentChange("Map/Effect2.img/event/gameover", 16));
         return;
      case 993192600:
         c.getPlayer().warp(993192501);
         return;
      case 993194500:
         c.getPlayer().warp(993194401);
         c.send(CField.environmentChange("Map/Effect.img/killing/fail", 16));
         return;
      default:
         c.getPlayer().dropMessageGM(6, "해당 버튼은 ExitSpecialGame에서 처리됩니다.");
      }
   }

   public static void HandleDreamBreakerSkill(MapleClient c, int SkillId) {
      try {
         int dream = (int)c.getPlayer().getKeyValue(15901, "dream");
         EventInstanceManager em = c.getPlayer().getEventInstance();
         if (em != null) {
            switch(SkillId) {
            case 0:
               if (dream >= 200) {
                  c.getPlayer().setKeyValue(15901, "dream", String.valueOf(dream - 200));
                  c.getSession().writeAndFlush(SLFCGPacket.DreamBreakerMsg("게이지 홀드! 5초동안 게이지의 이동이 멈춥니다!"));
                  em.setProperty("gaugeHold", "true");
                  Timer.MapTimer.getInstance().schedule(() -> {
                     em.setProperty("gaugeHold", "false");
                  }, 5000L);
               } else {
                  c.getSession().writeAndFlush(SLFCGPacket.DreamBreakerMsg("드림 포인트가 부족하여 스킬을 사용할 수 없습니다."));
               }
               break;
            case 1:
               if (dream < 300) {
                  c.getSession().writeAndFlush(SLFCGPacket.DreamBreakerMsg("드림 포인트가 부족하여 스킬을 사용할 수 없습니다."));
               } else {
                  c.getPlayer().setKeyValue(15901, "dream", String.valueOf(dream - 300));
                  List<MapleMonster> Orgels = new ArrayList();
                  Iterator var14 = c.getPlayer().getMap().getAllMonster().iterator();

                  while(var14.hasNext()) {
                     MapleMonster m = (MapleMonster)var14.next();
                     if (m.getId() >= 9833080 && m.getId() <= 9833084) {
                        Orgels.add(m);
                     }
                  }

                  if (Orgels.size() > 0) {
                     c.getPlayer().getMap().killMonster((MapleMonster)Orgels.get(Randomizer.nextInt(Orgels.size())), c.getPlayer(), false, false, (byte)1);
                     c.getSession().writeAndFlush(SLFCGPacket.DreamBreakerMsg("자각의 종소리를 울려 한 곳의 오르골이 깨어났습니다!"));
                  } else {
                     c.getSession().writeAndFlush(SLFCGPacket.DreamBreakerMsg("모든 오르골이 이미 깨어있는 상태입니다."));
                  }
               }
               break;
            case 2:
               if (dream >= 400) {
                  c.getPlayer().setKeyValue(15901, "dream", String.valueOf(dream - 400));
                  c.getSession().writeAndFlush(SLFCGPacket.DreamBreakerMsg("꿈속의 헝겊인형이 소환되어 몬스터들을 도발합니다!"));
                  MapleMonster m = MapleLifeFactory.getMonster(9833100);
                  m.setHp(m.getStats().getHp());
                  m.getStats().setHp(m.getStats().getHp());
                  c.getPlayer().getMap().spawnMonsterOnGroundBelow(m, c.getPlayer().getPosition());
               } else {
                  c.getSession().writeAndFlush(SLFCGPacket.DreamBreakerMsg("드림 포인트가 부족하여 스킬을 사용할 수 없습니다."));
               }
               break;
            case 3:
               if (dream < 900) {
                  c.getSession().writeAndFlush(SLFCGPacket.DreamBreakerMsg("드림 포인트가 부족하여 스킬을 사용할 수 없습니다."));
               } else {
                  c.getPlayer().setKeyValue(15901, "dream", String.valueOf(dream - 900));
                  c.getSession().writeAndFlush(SLFCGPacket.DreamBreakerMsg("숙면의 오르골을 공격하던 모든 몬스터가 사라졌습니다!"));
                  Iterator var4 = c.getPlayer().getMap().getAllMonster().iterator();

                  while(var4.hasNext()) {
                     MapleMonster m = (MapleMonster)var4.next();
                     switch(m.getId()) {
                     case 9833070:
                     case 9833071:
                     case 9833072:
                     case 9833073:
                     case 9833074:
                     case 9833080:
                     case 9833081:
                     case 9833082:
                     case 9833083:
                     case 9833084:
                     case 9833100:
                        break;
                     case 9833075:
                     case 9833076:
                     case 9833077:
                     case 9833078:
                     case 9833079:
                     case 9833085:
                     case 9833086:
                     case 9833087:
                     case 9833088:
                     case 9833089:
                     case 9833090:
                     case 9833091:
                     case 9833092:
                     case 9833093:
                     case 9833094:
                     case 9833095:
                     case 9833096:
                     case 9833097:
                     case 9833098:
                     case 9833099:
                     default:
                        c.getPlayer().getMap().killMonster(m, c.getPlayer(), false, false, (byte)1);
                     }
                  }

                  em.setProperty("stopSpawn", "true");
                  Timer.MapTimer.getInstance().schedule(() -> {
                     em.setProperty("stopSpawn", "false");
                  }, 10000L);
               }
            }

            c.getSession().writeAndFlush(SLFCGPacket.DreamBreakeLockSkill(SkillId));
            return;
         }
      } catch (Exception var10) {
         var10.printStackTrace();
         return;
      } finally {
         c.getSession().writeAndFlush(SLFCGPacket.DreamBreakeSkillRes());
      }

   }

   public static void HandleBingoClick(MapleClient c) {
      if (c.getPlayer().getBingoGame().getBingoTimer() != null && !c.getPlayer().getBingoGame().getBingoTimer().isCancelled()) {
         c.getPlayer().getBingoGame().addRank(c.getPlayer());
      }
   }

   public static void ExitBlockGame(LittleEndianAccessor rh, MapleClient c) {
      c.getSession().writeAndFlush(SLFCGPacket.BlockGameCommandPacket(3));
      c.getPlayer().setBlockCount(0);
      Timer.ShowTimer.getInstance().schedule(() -> {
         c.getSession().writeAndFlush(CField.UIPacket.IntroDisableUI(false));
         c.getSession().writeAndFlush(CField.UIPacket.IntroLock(false));
         ChannelServer cserv = c.getChannelServer();
         MapleMap target = cserv.getMapFactory().getMap(993017000);
         c.getPlayer().changeMap(target, target.getPortal(0));
         c.getPlayer().gainItem(4310185, c.getPlayer().getBlockCoin());
         c.getPlayer().setBlockCoin(0);
      }, 3500L);
   }

   public static void HandleBlockGameRes(LittleEndianAccessor rh, MapleClient c) {
      byte type = rh.readByte();
      if (type == 3) {
         c.getSession().writeAndFlush(SLFCGPacket.BlockGameCommandPacket(3));
         c.getPlayer().setBlockCount(0);
         Timer.ShowTimer.getInstance().schedule(() -> {
            c.getSession().writeAndFlush(CField.UIPacket.IntroDisableUI(false));
            c.getSession().writeAndFlush(CField.UIPacket.IntroLock(false));
            ChannelServer cserv = c.getChannelServer();
            MapleMap target = cserv.getMapFactory().getMap(993017000);
            c.getPlayer().changeMap(target, target.getPortal(0));
            c.getPlayer().gainItem(4310184, c.getPlayer().getBlockCoin());
            c.getPlayer().setBlockCoin(0);
         }, 3500L);
      } else {
         c.getPlayer().addBlockCoin(type == 2 ? 2 : 1);
         int block = c.getPlayer().getBlockCount() + 1;
         c.getPlayer().setBlockCount(block);
         if (block % 10 == 0) {
            int velocity = 100 + block / 10 * 30;
            int misplaceallowance = 1 + block / 10;
            switch(block) {
            case 70:
               c.getSession().writeAndFlush(SLFCGPacket.WeatherAddPacket(1));
               c.getSession().writeAndFlush(CField.musicChange("Bgm45/Time Is Gold"));
               break;
            case 100:
               c.getSession().writeAndFlush(CField.musicChange("Bgm45/Demian Spine"));
               break;
            case 120:
               c.getSession().writeAndFlush(SLFCGPacket.WeatherRemovePacket(1));
               c.getSession().writeAndFlush(SLFCGPacket.WeatherAddPacket(2));
            }

            c.getSession().writeAndFlush(SLFCGPacket.BlockGameControlPacket(velocity, misplaceallowance));
         }
      }

   }

   public static final void GuideWarp(LittleEndianAccessor slea, MapleCharacter chr) {
      byte type = slea.readByte();
      int i;
      switch(type) {
      case 0:
         chr.warp(slea.readInt());
      case 1:
      case 2:
      default:
         break;
      case 3:
         int size = slea.readInt();

         for(i = 0; i < size; ++i) {
            slea.readInt();
         }

         return;
      case 4:
         i = slea.readInt();
      }

   }

   public static final void useMannequin(LittleEndianAccessor slea, MapleCharacter chr) {
      slea.skip(4);
      byte type = slea.readByte();
      byte result = slea.readByte();
      byte slot = slea.readByte();
      byte temp = true;
      int itemId = 0;
      boolean second = false;
      if (slea.available() < 4L) {
         byte temp = slea.readByte();
         if (slea.available() >= 1L) {
            second = slea.readByte() == 1;
         }
      } else if (slea.available() == 4L) {
         itemId = slea.readInt();
      }

      if (GameConstants.isAngelicBuster(chr.getJob())) {
         second = chr.getDressup();
      }

      if (GameConstants.isZero(chr.getJob())) {
         second = chr.getGender() == 1 && chr.getSecondGender() == 0;
      }

      int[] banhair = new int[]{30070, 30071, 30072, 30073, 30074, 30075, 30076, 30077, 30080, 30081, 30082, 30083, 30084, 30085, 30086, 30087};
      MapleMannequin skin;
      int oldFace;
      if (type != 0) {
         if (type == 1) {
            if (result == 1) {
               if (itemId == 5680222) {
                  chr.getFaceRoom().add(new MapleMannequin(0, -1, -1, 0));
                  chr.removeItem(itemId, -1);
                  chr.getClient().getSession().writeAndFlush(CWvsContext.mannequin(type, result, (byte)3, (byte)chr.getFaceRoom().size(), (MapleMannequin)null));
                  chr.getClient().getSession().writeAndFlush(CWvsContext.mannequin(type, result, (byte)5, slot, (MapleMannequin)null));
               }
            } else if (result == 2) {
               skin = (MapleMannequin)chr.getFaceRoom().get(slot);
               skin.setValue(second ? chr.getSecondFace() : chr.getFace());
               chr.getClient().getSession().writeAndFlush(CWvsContext.mannequin(type, result, (byte)2, slot, skin));
               chr.getClient().getSession().writeAndFlush(CWvsContext.mannequinRes(type, result, 1));
            } else if (result == 3) {
               skin = (MapleMannequin)chr.getFaceRoom().get(slot);
               skin.setValue(0);
               skin.setBaseProb(-1);
               skin.setBaseColor(0);
               skin.setAddColor(0);
               chr.getClient().getSession().writeAndFlush(CWvsContext.mannequin(type, result, (byte)2, slot, skin));
               chr.getClient().getSession().writeAndFlush(CWvsContext.mannequinRes(type, result, 1));
            } else if (result == 4) {
               skin = (MapleMannequin)chr.getFaceRoom().get(slot);
               oldFace = second ? chr.getSecondFace() : chr.getFace();
               if (second) {
                  chr.setSecondFace(skin.getValue());
                  chr.updateSingleStat(MapleStat.FACE, (long)chr.getSecondFace());
               } else {
                  chr.setFace(skin.getValue());
                  chr.updateSingleStat(MapleStat.FACE, (long)chr.getFace());
               }

               skin.setValue(oldFace);
               chr.getClient().getSession().writeAndFlush(CWvsContext.mannequin(type, result, (byte)2, slot, skin));
               chr.getClient().getSession().writeAndFlush(CWvsContext.enableActions(chr));
               chr.getClient().getSession().writeAndFlush(CWvsContext.mannequin(type, result, (byte)4, slot, (MapleMannequin)null));
               chr.equipChanged();
            }
         } else if (type == 2) {
            if (result == 1) {
               if (itemId == 5680222) {
                  chr.getSkinRoom().add(new MapleMannequin(0, -1, -1, 0));
                  chr.removeItem(itemId, -1);
                  chr.getClient().getSession().writeAndFlush(CWvsContext.mannequin(type, result, (byte)3, (byte)chr.getSkinRoom().size(), (MapleMannequin)null));
                  chr.getClient().getSession().writeAndFlush(CWvsContext.mannequin(type, result, (byte)5, slot, (MapleMannequin)null));
               }
            } else {
               byte oldSkin;
               if (result == 2) {
                  skin = (MapleMannequin)chr.getSkinRoom().get(slot);
                  oldSkin = second ? chr.getSecondSkinColor() : chr.getSkinColor();
                  skin.setValue(oldSkin + 12000);
                  chr.getClient().getSession().writeAndFlush(CWvsContext.mannequin(type, result, (byte)2, slot, skin));
                  chr.getClient().getSession().writeAndFlush(CWvsContext.mannequinRes(type, result, 1));
               } else if (result == 3) {
                  skin = (MapleMannequin)chr.getSkinRoom().get(slot);
                  skin.setValue(0);
                  skin.setBaseProb(-1);
                  skin.setBaseColor(0);
                  skin.setAddColor(0);
                  chr.getClient().getSession().writeAndFlush(CWvsContext.mannequin(type, result, (byte)2, slot, skin));
                  chr.getClient().getSession().writeAndFlush(CWvsContext.mannequinRes(type, result, 1));
               } else if (result == 4) {
                  skin = (MapleMannequin)chr.getSkinRoom().get(slot);
                  oldSkin = second ? chr.getSecondSkinColor() : chr.getSkinColor();
                  if (second) {
                     chr.setSecondSkinColor((byte)(skin.getValue() - 12000));
                     chr.updateSingleStat(MapleStat.SKIN, (long)chr.getSecondSkinColor());
                  } else {
                     chr.setSkinColor((byte)(skin.getValue() - 12000));
                     chr.updateSingleStat(MapleStat.SKIN, (long)chr.getSkinColor());
                  }

                  skin.setValue(oldSkin);
                  chr.getClient().getSession().writeAndFlush(CWvsContext.mannequin(type, result, (byte)2, slot, skin));
                  chr.getClient().getSession().writeAndFlush(CWvsContext.enableActions(chr));
                  chr.getClient().getSession().writeAndFlush(CWvsContext.mannequin(type, result, (byte)4, slot, (MapleMannequin)null));
                  chr.equipChanged();
               }
            }
         }
      } else if (result == 1) {
         if (itemId == 5680222) {
            chr.getHairRoom().add(new MapleMannequin(0, -1, 0, 0));
            chr.removeItem(itemId, -1);
            chr.getClient().getSession().writeAndFlush(CWvsContext.mannequin(type, result, (byte)3, (byte)chr.getHairRoom().size(), (MapleMannequin)null));
            chr.getClient().getSession().writeAndFlush(CWvsContext.mannequin(type, result, (byte)5, slot, (MapleMannequin)null));
         }
      } else {
         int mBaseProb;
         int mBaseColor;
         int mAddColor;
         if (result != 2) {
            if (result == 3) {
               skin = (MapleMannequin)chr.getHairRoom().get(slot);
               skin.setValue(0);
               skin.setBaseProb(-1);
               skin.setBaseColor(0);
               skin.setAddColor(0);
               chr.getClient().getSession().writeAndFlush(CWvsContext.mannequin(type, result, (byte)2, slot, skin));
               chr.getClient().getSession().writeAndFlush(CWvsContext.mannequinRes(type, result, 1));
            } else if (result == 4) {
               skin = (MapleMannequin)chr.getHairRoom().get(slot);
               oldFace = second ? chr.getSecondHair() : chr.getHair();
               mBaseProb = second ? chr.getSecondBaseProb() : chr.getBaseProb();
               mBaseColor = second ? chr.getSecondBaseColor() : chr.getBaseColor();
               mAddColor = second ? chr.getSecondAddColor() : chr.getAddColor();
               if (second) {
                  chr.setSecondHair(skin.getValue());
                  chr.setSecondBaseProb(skin.getBaseProb());
                  chr.setSecondBaseColor(skin.getBaseColor());
                  chr.setSecondAddColor(skin.getAddColor());
                  chr.updateSingleStat(MapleStat.HAIR, (long)chr.getHair());
               } else {
                  chr.setHair(skin.getValue());
                  chr.setBaseProb(skin.getBaseProb());
                  chr.setBaseColor(skin.getBaseColor());
                  chr.setAddColor(skin.getAddColor());
                  chr.updateSingleStat(MapleStat.HAIR, (long)chr.getHair());
               }

               skin.setValue(oldFace);
               skin.setBaseProb(mBaseProb);
               skin.setBaseColor(mBaseColor);
               skin.setAddColor(mAddColor);
               chr.getClient().getSession().writeAndFlush(CWvsContext.mannequin(type, result, (byte)2, slot, skin));
               chr.getClient().getSession().writeAndFlush(CWvsContext.enableActions(chr));
               chr.getClient().getSession().writeAndFlush(CWvsContext.mannequin(type, result, (byte)4, slot, (MapleMannequin)null));
               chr.equipChanged();
            }
         } else {
            skin = (MapleMannequin)chr.getHairRoom().get(slot);
            int[] var10 = banhair;
            mBaseProb = banhair.length;
            mBaseColor = 0;

            while(true) {
               if (mBaseColor >= mBaseProb) {
                  skin.setValue(second ? chr.getSecondHair() : chr.getHair());
                  skin.setBaseProb(second ? chr.getSecondBaseProb() : chr.getBaseProb());
                  skin.setBaseColor(second ? chr.getSecondBaseColor() : chr.getBaseColor());
                  skin.setAddColor(second ? chr.getSecondAddColor() : chr.getAddColor());
                  chr.getClient().getSession().writeAndFlush(CWvsContext.mannequin(type, result, (byte)2, slot, skin));
                  chr.getClient().getSession().writeAndFlush(CWvsContext.mannequinRes(type, result, 1));
                  break;
               }

               mAddColor = var10[mBaseColor];
               if (chr.getHair() == mAddColor || second && chr.getSecondHair() == mAddColor) {
                  chr.dropMessage(1, "이 헤어는 저장하실 수 없습니다.");
                  return;
               }

               ++mBaseColor;
            }
         }
      }

   }

   public static void UseChooseAbility(LittleEndianAccessor slea, MapleClient c) {
      slea.skip(4);
      byte type = slea.readByte();
      if (c.getPlayer().innerCirculator != null) {
         if (type == 1) {
            Iterator var3 = c.getPlayer().getInnerSkills().iterator();

            InnerSkillValueHolder inner;
            while(var3.hasNext()) {
               inner = (InnerSkillValueHolder)var3.next();
               c.getPlayer().changeSkillLevel(inner.getSkillId(), (byte)0, (byte)0);
            }

            c.getPlayer().getInnerSkills().clear();
            var3 = c.getPlayer().innerCirculator.iterator();

            while(var3.hasNext()) {
               inner = (InnerSkillValueHolder)var3.next();
               c.getPlayer().getInnerSkills().add(inner);
               c.getPlayer().changeSkillLevel(inner.getSkillId(), inner.getSkillLevel(), inner.getMaxLevel());
               c.getSession().writeAndFlush(CField.updateInnerAbility(inner, c.getPlayer().getInnerSkills().size(), c.getPlayer().getInnerSkills().size() == 3));
            }
         }

         c.getPlayer().innerCirculator = null;
      }
   }

   public static void EnterDungen(LittleEndianAccessor mplew, MapleClient c) {
      String d = mplew.readMapleAsciiString();
      NPCScriptManager.getInstance().start(c, ServerConstants.csNpc, "EnterDungeon", d);
   }

   public static void ICBM(LittleEndianAccessor slea, MapleClient c) {
      slea.skip(4);
      int skill = slea.readInt();
      slea.skip(4);
      SecondaryStatEffect eff = SkillFactory.getSkill(skill).getEffect(c.getPlayer().getSkillLevel(skill));
      if (eff.getCooldown(c.getPlayer()) > 0 && c.getPlayer().getCooldownLimit(skill) == 0L) {
         c.getPlayer().addCooldown(skill, System.currentTimeMillis(), (long)eff.getCooldown(c.getPlayer()));
         c.getSession().writeAndFlush(CField.skillCooldown(skill, eff.getCooldown(c.getPlayer())));
         if (GameConstants.isLinkedSkill(skill) && c.getPlayer().getCooldownLimit(GameConstants.getLinkedSkill(skill)) == 0L) {
            c.getPlayer().addCooldown(GameConstants.getLinkedSkill(skill), System.currentTimeMillis(), (long)eff.getCooldown(c.getPlayer()));
            c.getSession().writeAndFlush(CField.skillCooldown(GameConstants.getLinkedSkill(skill), eff.getCooldown(c.getPlayer())));
         }
      }

      short size = slea.readShort();

      for(int i = 0; i < size; ++i) {
         Rectangle poz = new Rectangle(slea.readInt(), slea.readInt(), slea.readInt(), slea.readInt());
         MapleMist mist = new MapleMist(poz, c.getPlayer(), eff, 1300, (byte)(c.getPlayer().isFacingLeft() ? 1 : 0));
         mist.setDelay(0);
      }

   }

   public static void DimentionSword(LittleEndianAccessor slea, MapleClient c) {
      int skillId = slea.readInt();
      SecondaryStatEffect eff = SkillFactory.getSkill(skillId).getEffect(c.getPlayer().getSkillLevel(skillId));
      if (eff.getCooldown(c.getPlayer()) > 0 && c.getPlayer().getCooldownLimit(skillId) == 0L) {
         c.getPlayer().addCooldown(skillId, System.currentTimeMillis(), (long)eff.getCooldown(c.getPlayer()));
         c.getSession().writeAndFlush(CField.skillCooldown(skillId, eff.getCooldown(c.getPlayer())));
         if (GameConstants.isLinkedSkill(skillId) && c.getPlayer().getCooldownLimit(GameConstants.getLinkedSkill(skillId)) == 0L) {
            c.getPlayer().addCooldown(GameConstants.getLinkedSkill(skillId), System.currentTimeMillis(), (long)eff.getCooldown(c.getPlayer()));
            c.getSession().writeAndFlush(CField.skillCooldown(GameConstants.getLinkedSkill(skillId), eff.getCooldown(c.getPlayer())));
         }
      }

      if (skillId == 400011090 && c.getPlayer().getBuffedValue(400011090)) {
         int duration = eff.getDuration();
         duration += (int)(c.getPlayer().getBuffedStarttime(SecondaryStat.IndieSummon, 400011090) - System.currentTimeMillis());
         duration /= 5;
         c.getPlayer().cancelEffectFromBuffStat(SecondaryStat.IndieSummon, 400011090);
         SkillFactory.getSkill(400011102).getEffect(c.getPlayer().getSkillLevel(400011090)).applyTo(c.getPlayer(), false, duration);
      } else if (skillId == 400051046) {
         Iterator var4 = c.getPlayer().getMap().getAllSummonsThreadsafe().iterator();

         while(var4.hasNext()) {
            MapleSummon summon = (MapleSummon)var4.next();
            if (summon.getSkill() == 400051046 && c.getPlayer().getId() == summon.getOwner().getId()) {
               summon.setSpecialSkill(true);
               summon.setLastAttackTime(System.currentTimeMillis());
               c.getPlayer().getClient().getSession().writeAndFlush(CField.SummonPacket.DeathAttack(summon, 10));
               break;
            }
         }
      }

   }

   public static void cancelAfter(LittleEndianAccessor slea, MapleClient c) {
      int skillid = slea.readInt();
      byte type = slea.readByte();
      MapleCharacter chr = c.getPlayer();
      chr.getMap().broadcastMessage(chr, CField.skillCancel(chr, skillid), false);
      Skill skill = SkillFactory.getSkill(skillid);
      if (skillid == 400051041 && type > 0) {
         chr.cancelEffect(chr.getBuffedEffect(skillid));
         chr.cancelEffect(chr.getBuffedEffect(400051094));
      }

      if (skillid != 14121004 && skillid != 400021061) {
         SecondaryStatEffect effect;
         double cooldown;
         if (skillid != 151121004 && skillid != 162121022) {
            if (skillid == 164121042) {
               effect = SkillFactory.getSkill(skillid).getEffect(c.getPlayer().getSkillLevel(skillid));
               cooldown = (double)c.getPlayer().getSkillCustomValue0(164121042) * effect.getT() * 1000.0D;
               c.getPlayer().removeCooldown(skillid);
               c.getPlayer().addCooldown(skillid, System.currentTimeMillis(), (long)(effect.getCooldown(c.getPlayer()) - (int)cooldown));
               c.getSession().writeAndFlush(CField.skillCooldown(skillid, effect.getCooldown(c.getPlayer()) - (int)cooldown));
               c.getPlayer().removeSkillCustomInfo(164121042);
            }
         } else if (c.getPlayer().getSkillCustomValue0(skillid) > 0L) {
            effect = SkillFactory.getSkill(skillid).getEffect(c.getPlayer().getSkillLevel(skillid));
            c.getPlayer().removeCooldown(skillid);
            cooldown = (double)c.getPlayer().getSkillCustomValue0(skillid) * 3.5D * 1000.0D;
            c.getPlayer().addCooldown(skillid, System.currentTimeMillis(), (long)(effect.getCooldown(c.getPlayer()) - (int)cooldown));
            c.getSession().writeAndFlush(CField.skillCooldown(skillid, effect.getCooldown(c.getPlayer()) - (int)cooldown));
            c.getPlayer().removeSkillCustomInfo(skillid);
            if (skillid == 162121022 && c.getPlayer().getBuffedValue(162120038)) {
               c.getPlayer().cancelEffect(c.getPlayer().getBuffedEffect(162120038), (List)null, true);
               SkillFactory.getSkill(162120038).getEffect(1).applyTo(c.getPlayer(), false, 1000);
            }
         }
      } else if (chr.getBuffedEffect(skillid) != null) {
         chr.cancelEffect(chr.getBuffedEffect(skillid));
      }

      if (skill.isChargeSkill()) {
         chr.setKeyDownSkill_Time(0L);
      }

   }

   public static void autoSkill(LittleEndianAccessor slea, MapleClient c) {
      MapleCharacter character = c.getPlayer();
      int skillid = slea.readInt();
      character.setAutoSkill(skillid, !character.isAutoSkill(skillid));
   }

   public static void PoisonNova(LittleEndianAccessor slea, MapleClient c) {
      List<Integer> novas = new ArrayList();
      int size = slea.readInt();

      for(int i = 0; i < size; ++i) {
         novas.add(slea.readInt());
      }

      c.getPlayer().setPosionNovas(novas);
   }

   public static void useMoonGauge(MapleClient c) {
      if (c.getPlayer().getMapId() != 450008150 && c.getPlayer().getMapId() != 450008750) {
         if (c.getPlayer().getMapId() != 450008250 && c.getPlayer().getMapId() != 450008850) {
            if (c.getPlayer().getMapId() == 450008350 || c.getPlayer().getMapId() == 450008950) {
               c.getPlayer().setMoonGauge(Math.max(0, c.getPlayer().getMoonGauge() - 5));
               c.getPlayer().clearWeb = 2;
               c.getSession().writeAndFlush(MobPacket.BossWill.addMoonGauge(c.getPlayer().getMoonGauge()));
               c.getSession().writeAndFlush(MobPacket.BossWill.cooldownMoonGauge(5000));
               Timer.ShowTimer.getInstance().schedule(() -> {
                  c.getPlayer().clearWeb = 0;
               }, 5000L);
            }
         } else {
            c.getPlayer().cancelEffectFromBuffStat(SecondaryStat.DebuffIncHp);
            c.getPlayer().setMoonGauge(Math.max(0, c.getPlayer().getMoonGauge() - 50));
            c.getSession().writeAndFlush(MobPacket.BossWill.addMoonGauge(c.getPlayer().getMoonGauge()));
            c.getSession().writeAndFlush(MobPacket.BossWill.cooldownMoonGauge(7000));
            Timer.ShowTimer.getInstance().schedule(() -> {
               SkillFactory.getSkill(80002404).getEffect(1).applyTo(c.getPlayer(), true);
            }, 7000L);
         }
      } else {
         String name = c.getPlayer().getTruePosition().y > -1000 ? "ptup" : "ptdown";
         c.getPlayer().setMoonGauge(Math.max(0, c.getPlayer().getMoonGauge() - 45));
         c.getSession().writeAndFlush(MobPacket.BossWill.addMoonGauge(c.getPlayer().getMoonGauge()));
         c.getSession().writeAndFlush(MobPacket.BossWill.teleport());
         c.getSession().writeAndFlush(CField.portalTeleport(name));
      }

   }

   public static void touchSpider(LittleEndianAccessor slea, MapleClient c) {
      SpiderWeb web = (SpiderWeb)c.getPlayer().getMap().getMapObject(slea.readInt(), MapleMapObjectType.WEB);
      if (c.getPlayer().clearWeb > 0) {
         try {
            c.getPlayer().getMap().broadcastMessage(MobPacket.BossWill.willSpider(false, web));
            c.getPlayer().getMap().removeMapObject(web);
            --c.getPlayer().clearWeb;
         } catch (Throwable var4) {
         }
      } else if (c.getPlayer().getBuffedValue(SecondaryStat.NotDamaged) == null && c.getPlayer().getBuffedValue(SecondaryStat.IndieNotDamaged) == null && c.getPlayer().isAlive()) {
         c.send(CField.DamagePlayer2((int)(c.getPlayer().getStat().getCurrentMaxHp() / 100L) * 30));
         c.getPlayer().setSkillCustomInfo(8880302, 0L, 5000L);
         if (!c.getPlayer().hasDisease(SecondaryStat.Seal)) {
            MobSkill ms1 = MobSkillFactory.getMobSkill(120, 40);
            ms1.setDuration(5000L);
            c.getPlayer().giveDebuff(SecondaryStat.Seal, ms1);
         }
      }

   }

   public static void SkillToCrystal(LittleEndianAccessor slea, MapleClient c) {
      int skillId = slea.readInt();
      MapleSummon summon = c.getPlayer().getSummon(152101000);
      int attack = 0;
      int max = false;
      if (summon != null) {
         if (skillId != 152001001 && skillId != 152120001 && skillId != 152120002 && skillId != 152121004) {
            if (skillId == 152001002 || skillId == 152120003) {
               if (c.getPlayer().getSkillLevel(152110002) > 0) {
                  attack = 152110002;
               } else if (c.getPlayer().getSkillLevel(152100002) > 0) {
                  attack = 152100002;
               }
            }
         } else if (c.getPlayer().getSkillLevel(152110001) > 0) {
            attack = 152110001;
         } else if (c.getPlayer().getSkillLevel(152100001) > 0) {
            attack = 152100001;
         }

         int max = c.getPlayer().getSkillLevel(152110008) <= 0 ? 30 : 150;
         if (skillId != 152001001 && skillId != 152120001 && skillId != 152120002) {
            if (skillId == 152121004) {
               summon.setEnergy(Math.min(max, summon.getEnergy() + (c.getPlayer().getBuffedValue(SecondaryStat.FastCharge) != null ? 6 : 3)));
            } else if (skillId == 152001002 || skillId == 152120003) {
               summon.setEnergy(Math.min(max, summon.getEnergy() + (c.getPlayer().getBuffedValue(SecondaryStat.FastCharge) != null ? 4 : 2)));
            }
         } else {
            summon.setEnergy(Math.min(max, summon.getEnergy() + (c.getPlayer().getBuffedValue(SecondaryStat.FastCharge) != null ? 2 : 1)));
         }

         int cristalLevel = 152110008;
         if (c.getPlayer().getSkillLevel(152120014) > 0) {
            cristalLevel = 152120014;
         }

         if (summon.getEnergy() >= 150 && !c.getPlayer().getBuffedValue(cristalLevel)) {
            SkillFactory.getSkill(cristalLevel).getEffect(c.getPlayer().getSkillLevel(cristalLevel)).applyTo(c.getPlayer());
         }

         SecondaryStatEffect attackEff = SkillFactory.getSkill(attack).getEffect(c.getPlayer().getSkillLevel(attack));
         if (!c.getPlayer().skillisCooling(attack)) {
            c.getPlayer().addCooldown(attack, System.currentTimeMillis(), (long)attackEff.getCooldown(c.getPlayer()));
            c.getSession().writeAndFlush(CField.skillCooldown(attack, attackEff.getCooldown(c.getPlayer())));
            if (attack == 152110001) {
               c.getPlayer().getMap().broadcastMessage(CField.SummonPacket.specialSummon2(summon, attack));
            }

            c.getPlayer().getMap().broadcastMessage(CField.SummonPacket.ElementalRadiance(summon, 3));
         }

         if ((summon.getEnergy() < 30 || summon.getCrystalSkills().size() != 0) && (summon.getEnergy() < 60 || summon.getCrystalSkills().size() != 1) && (summon.getEnergy() < 90 || summon.getCrystalSkills().size() != 2) && (summon.getEnergy() < 150 || summon.getCrystalSkills().size() != 3)) {
            c.getPlayer().getMap().broadcastMessage(CField.SummonPacket.ElementalRadiance(summon, 2));
            c.getPlayer().getMap().broadcastMessage(CField.SummonPacket.specialSummon(summon, 2));
         } else {
            summon.getCrystalSkills().add(true);
            c.getPlayer().getMap().broadcastMessage(CField.SummonPacket.transformSummon(summon, 2));
            c.getPlayer().getMap().broadcastMessage(CField.SummonPacket.ElementalRadiance(summon, 2));
            c.getPlayer().getMap().broadcastMessage(CField.SummonPacket.specialSummon(summon, 3));
         }

         c.getPlayer().getMap().broadcastMessage(CField.SummonPacket.ElementalRadiance(summon, 3));
      }
   }

   public static void buffFreezer(LittleEndianAccessor slea, MapleClient c) {
      slea.skip(4);
      boolean use = slea.readByte() == 1;
      int buffFreezer;
      if (c.getPlayer().itemQuantity(5133000) > 0) {
         buffFreezer = 5133000;
      } else {
         buffFreezer = 5133001;
      }

      if (use) {
         c.getPlayer().setUseBuffFreezer(true);
         c.getPlayer().removeItem(buffFreezer, -1);
      }

      c.getSession().writeAndFlush(CField.buffFreezer(buffFreezer, use));
   }

   public static void quickSlot(LittleEndianAccessor slea, MapleClient c) {
      int i = 0;
      if (c.getPlayer() != null) {
         while(slea.available() >= 4L) {
            c.getPlayer().setKeyValue(333333, "quick" + i, String.valueOf(slea.readInt()));
            ++i;
         }
      }

   }

   public static void unlockTrinity(MapleClient c) {
      if (GameConstants.isAngelicBuster(c.getPlayer().getJob()) && c.getPlayer().getSkillLevel(65121101) > 0) {
         c.send(CField.unlockSkill());
         c.send(CField.EffectPacket.showNormalEffect(c.getPlayer(), 49, true));
         c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.EffectPacket.showNormalEffect(c.getPlayer(), 49, false), false);
      }

   }

   public static void checkCoreSecondpw(LittleEndianAccessor slea, MapleClient c) {
      int type = slea.readInt();
      if (type == 1) {
         String secondpw = slea.readMapleAsciiString();
         if (c.CheckSecondPassword(secondpw)) {
            c.getSession().writeAndFlush(CWvsContext.openCore());
         }
      }

   }

   public static void inviteChair(LittleEndianAccessor slea, MapleClient c) {
      int targetId = slea.readInt();
      MapleCharacter target = c.getPlayer().getMap().getCharacterById(targetId);
      if (target != null) {
         c.getSession().writeAndFlush(CField.inviteChair(7));
         target.getClient().getSession().writeAndFlush(CField.requireChair(c.getPlayer().getId()));
      } else {
         c.getSession().writeAndFlush(CField.inviteChair(8));
      }

   }

   public static void resultChair(LittleEndianAccessor slea, MapleClient c) {
      int targetId = slea.readInt();
      int result = slea.readInt();
      if (result == 7) {
         MapleCharacter target = c.getPlayer().getMap().getCharacterById(targetId);
         if (target != null) {
            c.getSession().writeAndFlush(CField.resultChair(target.getChair(), 0));
            MapleSpecialChair chair = null;
            Iterator var6 = target.getMap().getAllSpecialChairs().iterator();

            while(var6.hasNext()) {
               MapleSpecialChair chairz = (MapleSpecialChair)var6.next();
               if (chairz.getOwner().getId() == target.getId()) {
                  chair = chairz;
                  break;
               }
            }

            if (chair != null) {
               int[] randEmotions = new int[]{2, 10, 14, 17};
               chair.updatePlayer(c.getPlayer(), randEmotions[Randomizer.nextInt(randEmotions.length)]);
               c.getPlayer().setChair(target.getChair());
               c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.showChair(c.getPlayer(), c.getPlayer().getChair()), false);
               c.getPlayer().getMap().broadcastMessage(CField.specialChair(c.getPlayer(), true, false, true, chair));
            }
         } else {
            c.getSession().writeAndFlush(CField.resultChair(targetId, 1));
         }
      } else {
         c.getSession().writeAndFlush(CField.resultChair(targetId, 1));
      }

   }

   public static void bloodFist(LittleEndianAccessor slea, MapleClient c) {
      int skill = slea.readInt();
      c.getPlayer().addHP(-c.getPlayer().getStat().getHp() * (long)((Skill)Objects.requireNonNull(SkillFactory.getSkill(skill))).getEffect(c.getPlayer().getSkillLevel(skill)).getX() / 100L);
   }

   public static void updateMist(LittleEndianAccessor slea, MapleClient c) {
      int skillId = slea.readInt();
      int skillLevel = slea.readInt();
      Point pos = slea.readPos();
      if (skillId == 400031037) {
         skillId = 400031040;
      }

      SecondaryStatEffect effect = SkillFactory.getSkill(skillId).getEffect(skillLevel);
      Iterator var6 = c.getPlayer().getMap().getAllMistsThreadsafe().iterator();

      while(var6.hasNext()) {
         MapleMist mist = (MapleMist)var6.next();
         if (mist.getSource() != null && mist.getSource().getSourceId() == skillId) {
            c.getPlayer().getMap().broadcastMessage(CField.removeMist(mist));
            mist.setPosition(pos);
            mist.setBox(effect.calculateBoundingBox(pos, c.getPlayer().isFacingLeft()));
            c.getPlayer().getMap().broadcastMessage(CField.spawnMist(mist));
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer(), true, false));
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer(), false, true));
         }
      }

   }

   public static void BlesterSkill(LittleEndianAccessor slea, MapleClient c, boolean cancel) {
      int skillid = slea.readInt();
      SecondaryStatEffect eff = SkillFactory.getSkill(GameConstants.getLinkedSkill(skillid)).getEffect(c.getPlayer().getSkillLevel(GameConstants.getLinkedSkill(skillid)));
      if (c.getPlayer().getBuffedValue(SecondaryStat.RWCombination) == null) {
         c.getPlayer().acaneAim = 0;
      }

      if (skillid == 37121004 && cancel) {
         c.getPlayer().CylinderBuff(skillid, true);
         c.getPlayer().cancelEffectFromBuffStat(SecondaryStat.NotDamaged, 37121004);
      } else if (!cancel && (skillid == 37100002 || skillid == 37110001 || skillid == 37110004 || skillid == 37101000 || skillid == 37100002 || skillid == 37110001 || skillid == 37110004 || skillid == 37101000 || skillid == 37101001 || skillid == 37111003)) {
         if (skillid != 37101000 && skillid != 37101001 && skillid != 37111003) {
            if (c.getPlayer().bullet > 6) {
               c.getPlayer().bullet = 6;
            } else if (c.getPlayer().bullet < 0) {
               c.getPlayer().bullet = 0;
            }

            c.getPlayer().Cylinder(skillid);
         }

         if (skillid == 37100002 || skillid == 37110004) {
            eff.applyTo(c.getPlayer());
         }

         if (c.getPlayer().getSkillLevel(37110009) > 0) {
            if (c.getPlayer().getSkillLevel(37120012) > 0) {
               SkillFactory.getSkill(37120012).getEffect(c.getPlayer().getSkillLevel(37120012)).applyTo(c.getPlayer());
            } else {
               SkillFactory.getSkill(37110009).getEffect(c.getPlayer().getSkillLevel(37110009)).applyTo(c.getPlayer());
            }
         }
      }

   }

   public static void openHasteBox(LittleEndianAccessor slea, MapleCharacter chr) {
      byte state = slea.readByte();
      switch(state) {
      case 0:
         int id = slea.readInt();
         String[] boxIds = new String[]{"M1", "M2", "M3", "M4", "M5", "M6"};
         if (chr.getKeyValue(500862, boxIds[id]) != 1L) {
            chr.dropMessage(1, "오류가 발생했습니다. 문의하세요.");
            return;
         } else {
            int[][] items = new int[][]{{4001832, 500}, {4001126, 100}};
            int[] item = items[Randomizer.nextInt(items.length)];
            chr.setKeyValue(500862, "openBox", String.valueOf(chr.getKeyValue(500862, "openBox") + 1L));
            chr.setKeyValue(500862, "booster", String.valueOf(chr.getKeyValue(500862, "booster") + 1L));
            if (chr.getKeyValue(500862, "openBox") == 6L) {
               chr.setKeyValue(500862, "str", "오늘의 일일 미션을 모두 완료하였습니다!");
            } else {
               chr.setKeyValue(500862, "str", chr.getKeyValue(500862, "openBox") + "단계 상자 도전 중! 일일 미션 1개를 완료하세요!");
            }

            chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, item[0], item[1], 8, 0, 1, (byte)0, true, (Point)null, (String)null, (Item)null));
            chr.getClient().getSession().writeAndFlush(CField.NPCPacket.getNPCTalk(0, (byte)0, "#b#e<헤이스트 상자>#n#k에서 #b#e#i" + item[0] + ":# #t" + item[0] + ":# " + item[1] + "개#n#k를 획득하였다!", "00 01", (byte)57));
         }
      default:
      }
   }

   public static void spotlightBuff(LittleEndianAccessor slea, MapleClient c) {
      MapleCharacter chr = c.getPlayer();
      if (chr != null) {
         byte state = slea.readByte();
         int stack = slea.readInt();
         if (state == 1 && chr.getSkillLevel(400051018) > 0) {
            SkillFactory.getSkill(400051027).getEffect(chr.getSkillLevel(400051018)).applyTo(chr, false, stack);
         } else if (chr.getBuffedEffect(400051027) != null) {
            chr.cancelEffect(c.getPlayer().getBuffedEffect(400051027));
         }

      }
   }

   public static void bless5th(LittleEndianAccessor slea, MapleClient c) {
      MapleCharacter chr = c.getPlayer();
      if (chr != null) {
         int MAX_COUNT = true;
         long lastUsedTime = 0L;
         long COOLDOWN_TIME = 180000L;
         long currentTime = System.currentTimeMillis();
         if (chr.메이플용사 < 2) {
            ++chr.메이플용사;
         }

         int skill = slea.readInt();
         slea.skip(4);
         int stack = chr.메이플용사;
         boolean check = true;
         HashMap<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
         if (skill == 400001050) {
            int[] skills = new int[]{400001051, 400001053, 400001054, 400001055};
            chr.nextBlessSkill = skills[Randomizer.nextInt(skills.length)];
            Map<SecondaryStat, Pair<Integer, Integer>> localstatups = new HashMap();
            localstatups.put(SecondaryStat.Bless5th2, new Pair(stack, (int)chr.getBuffLimit(400001050)));
            c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(localstatups, chr.getBuffedEffect(400001050), chr));
            c.getSession().writeAndFlush(CField.V_BLESS(skill, check));
            c.getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, chr.nextBlessSkill, 1, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), true, (Point)null, (String)null, (Item)null));
            chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 0, chr.nextBlessSkill, 1, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), true, (Point)null, (String)null, (Item)null), false);
         }

         if (skill == 400001042 || skill == 400001043) {
            SecondaryStatEffect yoyo = SkillFactory.getSkill(400001042).getEffect(1);
            statups.put(SecondaryStat.Bless5th2, new Pair(stack, 0));
            c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, yoyo, c.getPlayer()));
            c.getSession().writeAndFlush(CField.V_BLESS(skill, check));
         }

      }
   }

   public static void showICBM(LittleEndianAccessor slea, MapleCharacter player) {
      player.getMap().broadcastMessage(player, CField.showICBM(player.getId(), slea.readInt(), slea.readInt()), false);
   }

   public static void arkGauge(int readInt, MapleCharacter chr) {
      if (chr != null && GameConstants.isArk(chr.getJob())) {
         if (chr.getBuffedValue(155000007)) {
            if (!chr.getBuffedValue(400051334) || chr.gagenominus) {
               chr.SpectorGauge = Math.max(0, chr.SpectorGauge - 23);
               HashMap statups;
               if (chr.SpectorGauge == 0) {
                  chr.SpectorGauge = -1;
                  statups = new HashMap();
                  statups.put(SecondaryStat.SpectorGauge, new Pair(1, 0));
                  chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, chr.getBuffedEffect(SecondaryStat.SpectorGauge), chr));
                  chr.addCooldown(155001008, System.currentTimeMillis(), 20000L);
                  chr.getClient().getSession().writeAndFlush(CField.skillCooldown(155001008, 20000));
                  chr.cancelEffect(SkillFactory.getSkill(155000007).getEffect(1));
                  chr.SpectorGauge = 0;
               } else {
                  statups = new HashMap();
                  statups.put(SecondaryStat.SpectorGauge, new Pair(1, 0));
                  chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, chr.getBuffedEffect(SecondaryStat.SpectorGauge), chr));
               }
            }
         } else if (!chr.skillisCooling(155001008)) {
            int plus = 13;
            if (chr.getSkillLevel(155120034) > 0) {
               plus = (int)((double)plus + (double)plus * 0.1D);
            }

            chr.SpectorGauge = Math.min(1000, readInt + plus);
            Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
            statups.put(SecondaryStat.SpectorGauge, new Pair(1, 0));
            chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, (SecondaryStatEffect)null, chr));
         }
      }

   }

   public static void quickPass(LittleEndianAccessor slea, MapleClient c) {
      int tt = slea.readInt();
      int type = slea.readInt();
      boolean left = slea.readByteToInt() == 1;
      if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() < 3) {
         c.getPlayer().dropMessage(1, "장비를 3칸 이상 비워주세요.");
      } else if (c.getPlayer().getDonationPoint() < 1500L) {
         c.getPlayer().dropMessage(1, "후원 포인트가 부족합니다.");
      } else {
         int i;
         if (left) {
            i = Integer.parseInt(c.getPlayer().getV("arcane_quest_" + (type + 2))) + 1;
            int i = (int)(c.getPlayer().getKeyValue(39051, "c" + type) + 1L);
            c.getPlayer().addKV("arcane_quest_" + (type + 2), String.valueOf(i));
            c.getPlayer().setKeyValue(39051, "c" + type, String.valueOf(i));
         } else {
            i = (int)(c.getPlayer().getKeyValue(39052, "c" + type) + 1L);
            MapleCharacter var10000;
            MapleCharacter var10003;
            switch(type) {
            case 0:
               c.getPlayer().dropMessage(1, "이용 불가능합니다.");
               return;
            case 1:
               c.getPlayer().addKV("muto", String.valueOf(Integer.parseInt(c.getPlayer().getV("muto")) + 1));
               c.getPlayer().setKeyValue(39052, "c1", String.valueOf(i));
               break;
            case 2:
               var10000 = c.getPlayer();
               var10003 = c.getPlayer();
               var10000.setKeyValue(20190131, "play", String.valueOf(var10003.getKeyValue(20190131, "play")).makeConcatWithConstants<invokedynamic>(String.valueOf(var10003.getKeyValue(20190131, "play"))));
               c.getPlayer().setKeyValue(39052, "c2", String.valueOf(i));
               break;
            case 3:
               var10000 = c.getPlayer();
               var10003 = c.getPlayer();
               var10000.setKeyValue(16215, "play", String.valueOf(var10003.getKeyValue(16215, "play")).makeConcatWithConstants<invokedynamic>(String.valueOf(var10003.getKeyValue(16215, "play"))));
               c.getPlayer().setKeyValue(39052, "c3", String.valueOf(i));
            }
         }

         c.getPlayer().gainItem(1712001 + type, 1);
         c.getPlayer().gainItem(1712001 + type, 1);
         c.getPlayer().gainItem(1712001 + type, 1);
         c.getPlayer().gainDonationPoint(-1500);
         c.getPlayer().dropMessage(1, "포인트를 사용하여 일일퀘스트를 완료했습니다.");
      }
   }

   public static void CannonBall(LittleEndianAccessor slea, MapleClient c) {
      int skillid = slea.readInt();
      int count = slea.readInt();
      HashMap<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
      SecondaryStatEffect cannonball = SkillFactory.getSkill(skillid).getEffect(1);
      statups.put(SecondaryStat.MiniCannonBall, new Pair(count, 0));
      c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, cannonball, c.getPlayer()));
   }

   public static void selectDice(LittleEndianAccessor slea, MapleClient c) {
      int dice = slea.readInt();
      c.getPlayer().setDice(dice);
      SecondaryStatEffect effect = SkillFactory.getSkill(400051000).getEffect(c.getPlayer().getTotalSkillLevel(400051000));
      effect.applyTo(c.getPlayer(), false, dice, true);
   }

   public static void battleStatistics(LittleEndianAccessor slea, MapleClient c) {
      c.getSession().writeAndFlush(CField.battleStatistics());
   }

   public static void goldCompleteByPass(MapleClient c) {
      try {
         if (Integer.parseInt(c.getCustomData(239, "day")) == Integer.parseInt(c.getCustomData(239, "cMaxDay"))) {
            c.getPlayer().dropMessage(1, "모든 날짜에 출석하였습니다.");
            return;
         }

         if (c.getPlayer().getDonationPoint() < 3000L) {
            c.getPlayer().dropMessage(1, "후원포인트 3000이 필요합니다.");
            return;
         }

         int value = CurrentTime.getDay() != 6 && CurrentTime.getDay() != 7 ? 1 : 2;
         int k = Math.min(135, Integer.parseInt(c.getCustomData(239, "day")) + value);
         Iterator var3 = GameConstants.chariotItems.iterator();

         label82: {
            Triple item;
            while(true) {
               if (!var3.hasNext()) {
                  break label82;
               }

               item = (Triple)var3.next();
               if (value == 1) {
                  if ((Integer)item.left == k) {
                     break;
                  }
               } else if ((Integer)item.left == k || (Integer)item.left == k - 1) {
                  break;
               }
            }

            if (!MapleInventoryManipulator.checkSpace(c, (Integer)item.mid, (Integer)item.right, "")) {
               c.getPlayer().dropMessage(1, "보상을 받기 위한 인벤토리의 공간이 부족합니다.");
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               return;
            }

            if ((Integer)item.mid == 4310291) {
               c.getPlayer().dropMessage(1, "보상이 지급되었습니다.");
               c.getPlayer().AddStarDustCoin(1, (Integer)item.right);
            } else {
               Item addItem;
               if (GameConstants.getInventoryType((Integer)item.mid) != MapleInventoryType.EQUIP && GameConstants.getInventoryType((Integer)item.mid) != MapleInventoryType.CODY) {
                  int quantity = (Integer)item.right;
                  addItem = new Item((Integer)item.mid, (short)0, (short)quantity);
               } else {
                  addItem = MapleItemInformationProvider.getInstance().getEquipById((Integer)item.mid);
               }

               if (addItem != null) {
                  MapleInventoryManipulator.addbyItem(c, addItem);
                  c.getPlayer().dropMessage(1, "보상이 지급되었습니다.");
               }
            }
         }

         c.setCustomData(239, "complete", "1");
         c.setCustomData(239, "day", String.valueOf(k));
         int j = Integer.parseInt(c.getCustomData(239, "passCount")) - value;
         c.setCustomData(239, "passCount", String.valueOf(j));
         StringBuilder z = new StringBuilder(c.getCustomData(240, "passDate"));
         c.getPlayer().gainDonationPoint(-3000);
         if (value == 2) {
            c.setCustomData(240, "passDate", z.replace(k - 2, k - 1, "1").toString());
         }

         c.setCustomData(240, "passDate", z.replace(k - 1, k, "1").toString());
         c.getSession().writeAndFlush(CField.getGameMessage(18, "황금마차 골든패스를 사용했습니다."));
         c.getPlayer().dropMessage(1, "출석을 완료하여 도장 " + value + "개를 찍었습니다.");
      } catch (Exception var7) {
         var7.printStackTrace();
      }

   }

   public static void eventUIResult(LittleEndianAccessor slea, MapleClient c) {
      short type = slea.readShort();
      int mapId = slea.readInt();
      int typed;
      int idx;
      int selection;
      switch(type) {
      case 17:
         c.removeClickedNPC();
         NPCScriptManager.getInstance().start(c, 2007, "union_raid");
         break;
      case 18:
         c.getPlayer().setKeyValue(18772, "id", String.valueOf(mapId));
         c.getPlayer().changeMap(921172100, 0);
         break;
      case 29:
         int objectId = slea.readInt();
         idx = slea.readInt();
         break;
      case 38:
         short id = slea.readShort();
         if (id == 0) {
            NPCScriptManager.getInstance().start(c, "hotelMaple");
         } else if (id == 1) {
            idx = slea.readInt();
            int[][] items = new int[][]{{2631527, 20}, {2631878, 1}, {2430218, 2}};
            if (items.length > idx && c.getPlayer().getKeyValue(501045, "lv") >= (long)(idx + 1)) {
               if (c.getPlayer().getKeyValue(501045, "reward" + idx) == 0L) {
                  if (MapleInventoryManipulator.checkSpace(c, items[idx][0], items[idx][1], "")) {
                     c.getPlayer().setKeyValue(501045, "reward" + idx, "1");
                     c.getPlayer().gainItem(items[idx][0], items[idx][1]);
                  } else {
                     c.getPlayer().dropMessage(1, "인벤토리의 공간이 부족합니다.");
                  }
               } else {
                  c.getPlayer().dropMessage(1, "이미 보상을 받았습니다.");
               }
            }
         } else if (id == 2) {
            idx = slea.readInt();
            NPCScriptManager.getInstance().start(c, idx, "hotelMapleSkill");
         }
         break;
      case 41:
         idx = slea.readInt();
         if (idx == 1200) {
            NPCScriptManager.getInstance().start(c, 3001604, "UI08");
         }

         if (idx == 1400) {
            MapleMap mapz = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(120043000);
            c.getPlayer().setDeathCount((byte)0);
            c.getPlayer().changeMap(mapz, mapz.getPortal(0));
            c.getPlayer().dispelDebuffs();
         }

         if (idx == 1100) {
            NPCScriptManager.getInstance().start(c, 3001604, "UI04");
         } else if (idx == 1101) {
            NPCScriptManager.getInstance().start(c, 3001604, "UI02");
         } else if (idx == 1102) {
            NPCScriptManager.getInstance().start(c, 3001604, "UI06");
         }

         if (idx == 1103) {
            NPCScriptManager.getInstance().start(c, 3001604, "UI05");
         } else if (idx == 1104) {
            NPCScriptManager.getInstance().start(c, 3001604, "UI07");
         } else if (idx == 1105) {
            NPCScriptManager.getInstance().start(c, 3001604, "UI03");
         }

         c.getSession().writeAndFlush(CField.playSound("Sound/SoundEff.img/glory_POA/finish"));
         break;
      case 46:
         if (c.getPlayer().getKeyValue(501229, "state") != 1L) {
            NPCScriptManager.getInstance().start(c, "NEO_Exploration1");
         }
         break;
      case 49:
         c.removeClickedNPC();
         NPCScriptManager.getInstance().dispose(c);
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
         typed = slea.readInt();
         if (typed == 0) {
            selection = slea.readInt();
            c.getPlayer().setSkillCustomInfo(501378, (long)selection, 0L);
            NPCScriptManager.getInstance().start(c, "BloomingForest_GiveItem");
         } else if (typed == 1) {
            if (c.getPlayer().getKeyValue(501378, "tuto") != 1L) {
               NPCScriptManager.getInstance().start(c, "BloomingForest_SkillTuto");
            } else {
               selection = slea.readInt();
               c.getPlayer().setSkillCustomInfo(501378, (long)selection, 0L);
               NPCScriptManager.getInstance().start(c, "BloomingForest_Skill");
            }
         } else if (typed == 2) {
            NPCScriptManager.getInstance().start(c, "BloomingForest_GiveSun");
         }
         break;
      case 51:
         if (c.canClickNPC() && NPCScriptManager.getInstance().getCM(c) == null) {
            typed = slea.readInt();
            selection = slea.readInt();
            c.getPlayer().setSkillCustomInfo(501378, (long)selection, 0L);
            if (typed == 0) {
               NPCScriptManager.getInstance().start(c, "MapleLive_DailyQuest");
            } else if (typed == 1) {
               NPCScriptManager.getInstance().start(c, "MapleLive_WeekQuest");
            } else if (typed == 2) {
               NPCScriptManager.getInstance().start(c, "MapleLive_MonthQuest");
            } else if (typed == 3) {
               NPCScriptManager.getInstance().start(c, "MapleLive_GiveItem");
            }
         } else {
            c.removeClickedNPC();
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
         }
         break;
      case 53:
         c.removeClickedNPC();
         NPCScriptManager.getInstance().start(c, 2012041);
         break;
      case 64:
         selection = slea.readInt();
         int number = slea.readInt();
         c.removeClickedNPC();
         NPCScriptManager.getInstance().dispose(c);
         c.getPlayer().getClient().getSession().writeAndFlush(CField.UIPacket.closeUI(1334));
         if (number == 0) {
            NPCScriptManager.getInstance().start(c, 3001930);
         } else if (number == 1) {
            NPCScriptManager.getInstance().start(c, 2510022);
         } else if (number == 2) {
            NPCScriptManager.getInstance().start(c, 1540011);
         } else if (number == 3) {
            NPCScriptManager.getInstance().start(c, 3006040);
         } else if (number == 4) {
            NPCScriptManager.getInstance().start(c, 9010022);
         } else {
            c.getPlayer().dropMessage(5, "아직 코딩 되지 않은 버튼입니다. (타입 : " + type + " / 버튼ID : " + number + ")");
         }
      }

      c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
   }

   public static void wiilMoon(LittleEndianAccessor slea, MapleClient c) {
      slea.skip(8);
      int state = slea.readInt();
      if (state == 1) {
         MapleMist moon = null;
         Iterator var5 = c.getPlayer().getMap().getAllMistsThreadsafe().iterator();

         MapleMist mist;
         while(var5.hasNext()) {
            mist = (MapleMist)var5.next();
            if (mist.getMobSkill() != null && mist.getMobSkill().getSkillId() == 242 && mist.getMobSkill().getSkillLevel() == 4) {
               moon = mist;
            }
         }

         Point pos = new Point(slea.readInt(), slea.readInt());
         if (moon != null) {
            if (pos.y < 0 && moon.getBox().y == -2301) {
               return;
            }

            if (pos.y >= 0 && moon.getBox().y == -122) {
               return;
            }
         }

         MapleMonster mob;
         if ((mob = c.getPlayer().getMap().getMonsterById(pos.y < 0 ? 8880304 : 8880303)) == null) {
            mob = c.getPlayer().getMap().getMonsterById(pos.y < 0 ? 8880344 : 8880343);
         }

         if (mob != null) {
            mist = new MapleMist(new Rectangle(-204, pos.y < 0 ? -2301 : -122, 408, 300), mob, MobSkillFactory.getMobSkill(242, 4), 30000);
            if (pos.y < 0) {
               mist.setPosition(new Point(0, -2021));
            } else {
               mist.setPosition(new Point(0, 158));
            }

            c.getPlayer().getMap().spawnMist(mist, false);
         }
      }

   }

   public static void removeSecondAtom(LittleEndianAccessor slea, MapleClient c) {
      int size = slea.readInt();

      for(int i = 0; i < size; ++i) {
         int objectId = slea.readInt();
         slea.skip(4);
         if (c.getPlayer() != null && c.getPlayer().getMap() != null) {
            if (c.getPlayer().getSecondAtom(objectId) != null) {
               c.getPlayer().removeSecondAtom(objectId);
            } else {
               c.getPlayer().getMap().removeSecondAtom(c.getPlayer(), objectId);
            }
         }
      }

   }

   public static void InfoSecondAtom(LittleEndianAccessor slea, MapleClient c) {
      int charid = slea.readInt();
      int type = slea.readInt();
      int objectId = slea.readInt();
      int info = slea.readInt();
      MapleSecondAtom atom = null;

      try {
         atom = c.getPlayer().getMap().getFindSecondAtoms(objectId);
      } catch (Exception var8) {
      }

      if (atom != null && atom.getSecondAtoms().getSourceId() == 63101006) {
         if (charid != info) {
            c.getPlayer().getMap().broadcastMessage(SkillPacket.AttackSecondAtom(c.getPlayer(), objectId, 1));
         } else if (charid == info) {
            atom.setLastAttackTime(System.currentTimeMillis());
         }
      }

   }

   public static void ropeConnect(LittleEndianAccessor slea, MapleClient c) {
      if (c.getPlayer() != null && c.getPlayer().getMap() != null) {
         int skillId = slea.readInt();
         short skilllv = slea.readShort();
         if (c.getPlayer().getSkillLevel(skillId) == skilllv) {
            int delay;
            int x;
            if (skillId == 4221052) {
               delay = slea.readInt();
               x = slea.readInt();
            } else if (slea.available() >= 12L) {
               delay = slea.readInt();
               x = slea.readInt();
               int y = slea.readInt();
               int unk = 0;
               if (slea.available() > 2L) {
                  unk = slea.readInt();
               }

               Point pos = new Point(x, y);
               c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.EffectPacket.showEffect(c.getPlayer(), delay, skillId, 1, unk, 0, (byte)0, false, pos, "", (Item)null), false);
            }
         }
      }

   }

   public static void DojangHandler(LittleEndianAccessor slea, MapleClient c) {
      int type = slea.readInt();
      if (type == 101) {
         c.getPlayer().getMap().broadcastMessage(CField.DojangFieldSetting(slea.readInt(), slea.readInt()));
      } else if (type == 102) {
         int seletedMonster = false;
         int[][] monsterList = new int[][]{{9834020, 9834021, 9834022, 9834023}, {9834024, 9834025, 9834026, 9834027}, {9834028, 9834029, 9834030, 9834031}};
         int unk = slea.readInt();
         if (unk == 2 || unk == 3) {
            MapleMap map = c.getPlayer().getMap();
            double range = Double.POSITIVE_INFINITY;
            byte animation = 1;
            Iterator var18 = map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER)).iterator();

            do {
               if (!var18.hasNext()) {
                  return;
               }

               MapleMapObject monstermo = (MapleMapObject)var18.next();
               MapleMonster mob = (MapleMonster)monstermo;
               map.killMonster(mob, c.getPlayer(), false, false, animation);
            } while(unk == 3);

            return;
         }

         int size = slea.readInt();
         int level = slea.readInt();
         slea.skip(8);
         int defense = slea.readInt();
         slea.skip(4);
         slea.skip(4);
         slea.skip(4);
         int attribute = slea.readInt();
         int monsterType = slea.readInt();
         long monsterHP = slea.readLong();
         int seletedMonster;
         if (monsterType == 0) {
            if (attribute == 1) {
               seletedMonster = monsterList[size][2];
            } else {
               seletedMonster = monsterList[size][3];
            }
         } else if (attribute == 1) {
            seletedMonster = monsterList[size][0];
         } else {
            seletedMonster = monsterList[size][1];
         }

         MapleMonster monster = MapleLifeFactory.getMonster(seletedMonster);
         monster.setHp(monsterHP);
         monster.changeLevel((short)level);
         c.getPlayer().getMap().spawnMonsterOnGroundBelow(monster, new Point(slea.readInt(), slea.readInt()));
      } else if (type == 103) {
         c.getPlayer().getStat().heal(c.getPlayer());
      }

   }

   public static void psychicUltimateRecv(LittleEndianAccessor slea, MapleClient c) {
      Skill skill = SkillFactory.getSkill(142121005);
      if (c.getPlayer() != null && c.getPlayer().getSkillLevel(142121005) > 0 && c.getPlayer().getSkillCustomValue0(142121005) == 1L) {
         c.getPlayer().removeSkillCustomInfo(142121005);
         SecondaryStatEffect effect = skill.getEffect(c.getPlayer().getSkillLevel(142121005));
         c.getPlayer().addCooldown(142121005, System.currentTimeMillis(), (long)effect.getCooldown(c.getPlayer()));
         c.getSession().writeAndFlush(CField.skillCooldown(142121005, effect.getCooldown(c.getPlayer())));
      }

   }

   public static void warpGuildMap(LittleEndianAccessor slea, MapleCharacter player) {
      if (player != null) {
         int id = slea.readInt();
         switch(id) {
         case 7860:
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            player.setKeyValue(id, "returnMap", player.getMapId().makeConcatWithConstants<invokedynamic>(player.getMapId()));
            player.warp(910001000);
            String var10003 = sdf.format((new Date()).getTime() + 1800000L);
            player.setKeyValue(id, "coolTime", var10003.replaceAll("-", "/").makeConcatWithConstants<invokedynamic>(var10003.replaceAll("-", "/")));
            break;
         case 26015:
            player.changeMap(200000301, 0);
         }

      }
   }

   public static void BlackMageRecv(LittleEndianAccessor slea, MapleClient c) {
      int type = slea.readInt();
      if (type != 3) {
         c.send(CField.getSelectPower(8, 39));
         SkillFactory.getSkill(80002625).getEffect(1).applyTo(c.getPlayer());
      }

      Timer.EtcTimer.getInstance().schedule(() -> {
         c.send(CField.getSelectPower(9, 39));
      }, 4000L);
   }

   public static void fpsShootRequest(LittleEndianAccessor slea, MapleClient c) {
      slea.skip(4);
      slea.skip(4);
      if (c.getPlayer() != null) {
         FrittoEagle eagle = c.getPlayer().getFrittoEagle();
         if (eagle != null) {
            int size = slea.readInt();
            eagle.shootResult(c);

            for(int i = 0; i < size; ++i) {
               int objectId = slea.readInt();
               slea.skip(14);
               MapleMap map = c.getPlayer().getMap();
               if (map != null) {
                  MapleMonster mob = map.getMonsterByOid(objectId);
                  if (mob != null) {
                     eagle.addScore(mob, c);
                     map.killMonster(mob);
                  }
               }
            }
         }
      }

   }

   public static void courtshipCommand(LittleEndianAccessor slea, MapleClient c) {
      boolean success = slea.readByte() == 1;
      if (success && c.getPlayer() != null) {
         FrittoDancing fd = c.getPlayer().getFrittoDancing();
         if (fd != null) {
            c.getPlayer().setKeyValue(15143, "score", String.valueOf(c.getPlayer().getKeyValue(15143, "score") + 1L));
            if (c.getPlayer().getKeyValue(15143, "score") >= 10L) {
               fd.finish(c);
            }
         }
      }

   }

   public static void forceInfo(LittleEndianAccessor slea, MapleClient c) {
      String str = slea.readMapleAsciiString();
      if (c.getPlayer().isGMName("GM하인즈")) {
         System.out.println("[Error] " + str);
         FileoutputUtil.log("Log_ForceAtom.rtf", str);
      }

   }

   public static void vSkillSpecial(LittleEndianAccessor slea, MapleClient c) {
      int skillId = slea.readInt();
      slea.skip(4);
      MapleCharacter chr = c.getPlayer();
      if (chr != null && chr.getMap() != null) {
         if (chr.getSkillLevel(GameConstants.getLinkedSkill(skillId)) >= 0) {
            c.getPlayer().dropMessageGM(6, "skillId : " + skillId);
            SecondaryStatEffect effect = SkillFactory.getSkill(skillId).getEffect(chr.getSkillLevel(GameConstants.getLinkedSkill(skillId)));
            int maxshiled;
            switch(skillId) {
            case 164121041:
               c.getPlayer().energy = Math.min(c.getPlayer().energy + c.getPlayer().getBuffedEffect(SecondaryStat.Sungi).getX(), 100);
               c.getPlayer().scrollGauge = Math.min(900, c.getPlayer().scrollGauge + c.getPlayer().getBuffedEffect(SecondaryStat.Sungi).getY());
               Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
               statups.put(SecondaryStat.TidalForce, new Pair(c.getPlayer().energy, 0));
               c.send(CWvsContext.BuffPacket.giveBuff(statups, (SecondaryStatEffect)null, c.getPlayer()));
               break;
            case 400001043:
               long duration = c.getPlayer().getBuffLimit(skillId);
               c.getPlayer().addHP(c.getPlayer().getStat().getCurrentMaxHp() / 100L * (long)effect.getY());
               if (c.getPlayer().getSkillCustomValue0(400001043) < (long)effect.getW()) {
                  c.getPlayer().setSkillCustomInfo(400001043, c.getPlayer().getSkillCustomValue0(400001043) + (long)effect.getDamage(), 0L);
               }

               effect.applyTo(c.getPlayer(), (int)duration);
               break;
            case 400001050:
               int[] Skill = new int[]{400001051, 400001053, 400001054, 400001055};
               int selskill = Skill[Randomizer.rand(0, Skill.length - 1)];
               c.getPlayer().setSkillCustomInfo(400001050, (long)selskill, 0L);
               c.getSession().writeAndFlush(CField.EffectPacket.showEffect(c.getPlayer(), 0, selskill, 1, 0, 0, (byte)(c.getPlayer().isFacingLeft() ? 1 : 0), true, c.getPlayer().getTruePosition(), (String)null, (Item)null));
               c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.EffectPacket.showEffect(c.getPlayer(), 0, selskill, 1, 0, 0, (byte)(c.getPlayer().isFacingLeft() ? 1 : 0), false, c.getPlayer().getTruePosition(), (String)null, (Item)null), false);
               long l1 = c.getPlayer().getBuffLimit(400001050);
               effect.applyTo(c.getPlayer(), false, (int)l1);
               if (selskill == 400001051) {
                  if (GameConstants.isDemonSlayer(c.getPlayer().getJob())) {
                     c.getPlayer().addMP(c.getPlayer().getStat().getCurrentMaxMp(c.getPlayer()) / 100L * (long)effect.getY(), true);
                  } else if (GameConstants.isKinesis(c.getPlayer().getJob())) {
                     c.getPlayer().givePPoint(400001051);
                  } else if (GameConstants.isDemonAvenger(c.getPlayer().getJob())) {
                     c.getPlayer().addHP(c.getPlayer().getStat().getCurrentMaxHp() / 100L * (long)effect.getY());
                  }
               }
               break;
            case 400011047:
               if (chr.getBuffedValue(400011047) && chr.getBuffedValue(1301007)) {
                  maxshiled = (int)(chr.getStat().getCurrentMaxHp() / 100L * (long)chr.getBuffedEffect(400011047).getY());
                  chr.setSkillCustomInfo(400011048, (long)maxshiled, 0L);
                  chr.getBuffedEffect(400011047).applyTo(chr, false);
               }
               break;
            case 400011109:
               c.getPlayer().addMP(-((long)((double)(c.getPlayer().getStat().getCurrentMaxMp(c.getPlayer()) / 100L) * effect.getMpR())));
               break;
            case 400021089:
               maxshiled = slea.readInt();
               int pos_x = chr.getTruePosition().x + Randomizer.rand(0, 50);
               int pos_y = chr.getTruePosition().y;
               List<SpecialPortal> sp = new ArrayList();
               SpecialPortal s3 = new SpecialPortal(chr.getId(), 2, 400021089, chr.getMapId(), pos_x, pos_y, (int)(chr.getBuffedValue(400021088) ? chr.getBuffLimit(400021088) : (long)effect.getDuration()));
               s3.setObjectId((int)(900000000L + chr.getSkillCustomValue0(400021088)));
               sp.add(s3);
               chr.getMap().spawnSpecialPortal(chr, sp);
               break;
            case 400021100:
               if (chr.getBuffedEffect(SecondaryStat.CrystalGate) != null && !chr.getBuffedValue(400021100)) {
                  effect.applyTo(chr, false);
               }
               break;
            case 400041031:
               c.getSession().writeAndFlush(CField.rangeAttack(effect.getSourceId(), Arrays.asList(new RangeAttack(400041031, c.getPlayer().getTruePosition(), 0, 0, 9))));
            }

         }
      }
   }

   public static void Revenant(LittleEndianAccessor slea, MapleClient c) {
      int id = slea.readInt();
      MapleCharacter chra = c.getPlayer();
      SecondaryStatEffect effect = SkillFactory.getSkill(400011112).getEffect(chra.getSkillLevel(400011112));
      long duration = chra.getBuffLimit(400011112);
      int savedamage = (int)chra.getSkillCustomValue0(400011112);
      if (savedamage > 0) {
         chra.setSkillCustomInfo(400011112, savedamage > 0 ? (long)(savedamage - savedamage / 100 * effect.getQ2()) : 0L, 0L);
         Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
         statups.put(SecondaryStat.Revenant, new Pair(1, (int)duration));
         chra.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, chra.getBuffedEffect(SecondaryStat.Revenant), chra));
      }

   }

   public static void Revenantend(LittleEndianAccessor slea, MapleClient c) {
      slea.skip(12);
      int skillid = slea.readInt();
      int minushp = slea.readInt();
      int plushp = slea.readInt();
      minushp += plushp;
      if (c.getPlayer().getSkillCustomValue0(400011129) > 0L) {
         c.getPlayer().addHP((long)(-minushp), 400011129);
      }

      c.getPlayer().setSkillCustomInfo(400011129, c.getPlayer().getSkillCustomValue0(400011129) - 1L, 0L);
      if (c.getPlayer().getSkillCustomValue0(400011129) <= 0L) {
         c.getPlayer().removeSkillCustomInfo(400011112);

         while(c.getPlayer().getBuffedValue(400011129)) {
            c.getPlayer().cancelEffect(c.getPlayer().getBuffedEffect(SecondaryStat.RevenantDamage));
         }
      } else {
         SkillFactory.getSkill(400011129).getEffect(c.getPlayer().getSkillLevel(400011112)).applyTo(c.getPlayer(), false);
      }

   }

   public static void photonRay(LittleEndianAccessor slea, MapleClient c) {
      int type = slea.readInt();
      MapleCharacter chr = c.getPlayer();
      if (chr != null) {
         if (chr.getBuffedEffect(SecondaryStat.PhotonRay) != null) {
            chr.photonRay = type;
            Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
            statups.put(SecondaryStat.PhotonRay, new Pair(1, (int)chr.getBuffLimit(400041057)));
            chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, chr.getBuffedEffect(SecondaryStat.PhotonRay), chr));
            chr.getMap().broadcastMessage(chr, CWvsContext.BuffPacket.giveForeignBuff(chr, statups, chr.getBuffedEffect(SecondaryStat.PhotonRay)), false);
         }

      }
   }

   public static void crystalGate(LittleEndianAccessor slea, MapleClient c) {
      int objectId = slea.readInt();
      Point pos = new Point(slea.readInt(), slea.readInt());
      MapleCharacter chr = c.getPlayer();
      if (chr != null && chr.getMap() != null && chr.getBuffedEffect(SecondaryStat.CrystalGate) != null && chr.getMap().getMapObject(objectId, MapleMapObjectType.SPECIAL_PORTAL) != null) {
         chr.getMap().movePlayer(chr, pos);
         chr.checkFollow();
      }
   }

   public static void cancelBuffForce(MapleClient c) {
      MapleCharacter chr = c.getPlayer();
      if (chr != null) {
      }

   }

   public static void CommandLockAction(LittleEndianAccessor slea, MapleClient c) {
      int skillid = slea.readInt();
   }

   public static void CommandLockAction2(LittleEndianAccessor slea, MapleClient c) {
      int skillid = slea.readInt();
      String info = c.getPlayer().getInfoQuest(1544);
      String info2 = c.getPlayer().getInfoQuest(7786);
      String a;
      SecondaryStatEffect effect;
      switch(skillid) {
      case 1101013:
         effect = SkillFactory.getSkill(1101013).getEffect(c.getPlayer().getTotalSkillLevel(1101013));
         if (c.getPlayer().getKeyValue(1544, skillid.makeConcatWithConstants<invokedynamic>(skillid)) != 1L) {
            c.getPlayer().setKeyValue(1544, skillid.makeConcatWithConstants<invokedynamic>(skillid), "1");
            c.getPlayer().setKeyValue(1548, "버프이펙트", "1");
         } else {
            c.getPlayer().setKeyValue(1544, skillid.makeConcatWithConstants<invokedynamic>(skillid), "0");
            c.getPlayer().setKeyValue(1548, "버프이펙트", "0");
         }

         if (c.getPlayer().getBuffedValue(SecondaryStat.ComboCounter) != null) {
            Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
            statups.put(SecondaryStat.ComboCounter, new Pair(c.getPlayer().getBuffedValue(SecondaryStat.ComboCounter), Integer.MAX_VALUE));
            c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, effect, c.getPlayer()));
            c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CWvsContext.BuffPacket.giveForeignBuff(c.getPlayer(), statups, effect), false);
         }

         return;
      case 1111015:
      case 1211017:
      case 1311017:
      case 15101021:
         if (c.getPlayer().getKeyValue(1544, skillid.makeConcatWithConstants<invokedynamic>(skillid)) != 1L) {
            c.getPlayer().setKeyValue(1544, skillid.makeConcatWithConstants<invokedynamic>(skillid), "1");
         } else {
            c.getPlayer().setKeyValue(1544, skillid.makeConcatWithConstants<invokedynamic>(skillid), "0");
         }

         return;
      case 5100015:
         if (c.getPlayer().getKeyValue(1544, skillid.makeConcatWithConstants<invokedynamic>(skillid)) != 1L) {
            c.getPlayer().setKeyValue(1544, skillid.makeConcatWithConstants<invokedynamic>(skillid), "1");
         } else {
            c.getPlayer().setKeyValue(1544, skillid.makeConcatWithConstants<invokedynamic>(skillid), "0");
         }

         Map<SecondaryStat, Pair<Integer, Integer>> localstatups = new HashMap();
         localstatups.put(SecondaryStat.EnergyCharged, new Pair(1, 0));
         c.getPlayer().getMap().broadcastMessage(CWvsContext.BuffPacket.giveForeignBuff(c.getPlayer(), localstatups, SkillFactory.getSkill(skillid).getEffect(1)));
         return;
      case 13111023:
         if (info == "") {
            c.getPlayer().updateInfoQuest(1544, "alba=1");
            c.getPlayer().getMap().broadcastMessage(CField.getRefreshQuestInfo(c.getPlayer().getId(), 1, skillid, 1));
         } else if (info.contains("=0")) {
            c.getPlayer().updateInfoQuest(1544, "alba=1");
            c.getPlayer().getMap().broadcastMessage(CField.getRefreshQuestInfo(c.getPlayer().getId(), 1, skillid, 1));
         } else {
            c.getPlayer().updateInfoQuest(1544, "alba=0");
            c.getPlayer().getMap().broadcastMessage(CField.getRefreshQuestInfo(c.getPlayer().getId(), 0, skillid, 1));
         }

         return;
      case 14001026:
         if (info == "") {
            c.getPlayer().updateInfoQuest(1544, "14001026=1;");
         } else if (info.contains("=0")) {
            c.getPlayer().updateInfoQuest(1544, "14001026=1;");
         } else {
            c.getPlayer().updateInfoQuest(1544, "14001026=0;");
         }

         return;
      case 14121052:
         if (info == "") {
            c.getPlayer().updateInfoQuest(1544, "14121052=1;");
            c.getPlayer().getMap().broadcastMessage(CField.getRefreshQuestInfo(c.getPlayer().getId(), 1, skillid, 0));
         } else if (info.contains("=0")) {
            c.getPlayer().updateInfoQuest(1544, "14121052=1;");
            c.getPlayer().getMap().broadcastMessage(CField.getRefreshQuestInfo(c.getPlayer().getId(), 1, skillid, 0));
         } else {
            c.getPlayer().updateInfoQuest(1544, "14121052=0;");
            c.getPlayer().getMap().broadcastMessage(CField.getRefreshQuestInfo(c.getPlayer().getId(), 0, skillid, 0));
         }

         return;
      case 20040217:
      case 20040219:
         if (c.getPlayer().getKeyValue(1544, skillid.makeConcatWithConstants<invokedynamic>(skillid)) != 1L) {
            c.getPlayer().setKeyValue(1544, skillid.makeConcatWithConstants<invokedynamic>(skillid), "1");
            c.getPlayer().getMap().broadcastMessage(CField.getRefreshQuestInfo(c.getPlayer().getId(), 1, skillid, 1));
         } else {
            c.getPlayer().setKeyValue(1544, skillid.makeConcatWithConstants<invokedynamic>(skillid), "0");
            c.getPlayer().getMap().broadcastMessage(CField.getRefreshQuestInfo(c.getPlayer().getId(), 0, skillid, 1));
         }

         return;
      case 30001068:
      case 35001006:
         int num = skillid == 35001006 ? 0 : 1;
         String str1 = "mc" + num;
         if (c.getPlayer().getKeyValue(21770, str1) != 1L) {
            c.getPlayer().setKeyValue(21770, str1, "1");
         } else {
            c.getPlayer().setKeyValue(21770, str1, "0");
         }

         return;
      case 32001016:
         if (info == "") {
            c.getPlayer().updateInfoQuest(1544, "32001016=1;");
            c.getPlayer().getMap().broadcastMessage(CField.getRefreshQuestInfo(c.getPlayer().getId(), 1, skillid, 0));
         } else if (info.contains("=0")) {
            c.getPlayer().updateInfoQuest(1544, "32001016=1;");
            c.getPlayer().getMap().broadcastMessage(CField.getRefreshQuestInfo(c.getPlayer().getId(), 1, skillid, 0));
         } else {
            c.getPlayer().updateInfoQuest(1544, "32001016=0;");
            c.getPlayer().getMap().broadcastMessage(CField.getRefreshQuestInfo(c.getPlayer().getId(), 0, skillid, 0));
         }

         return;
      case 35101002:
         if (info == "") {
            c.getPlayer().updateInfoQuest(1544, "35101002=1;");
         } else if (info.contains("=0")) {
            c.getPlayer().updateInfoQuest(1544, "35101002=1;");
         } else {
            c.getPlayer().updateInfoQuest(1544, "35101002=0;");
         }

         return;
      case 51121009:
         if (info == "") {
            c.getPlayer().updateInfoQuest(1544, "51121009=1;");
         } else if (info.contains("=0")) {
            c.getPlayer().updateInfoQuest(1544, "51121009=1;");
         } else {
            c.getPlayer().updateInfoQuest(1544, "51121009=0;");
         }

         return;
      case 80003016:
      case 80003025:
      case 80003046:
         String skillname = SkillFactory.getSkillName(skillid);
         int ui = skillid == 80003046 ? 1297 : 1291;
         if ((c.getPlayer().getMap().isTown() || !c.getPlayer().getMap().isLevelMob(c.getPlayer())) && !c.getPlayer().getBuffedValue(skillid)) {
            if (ui > 0) {
               c.getSession().writeAndFlush(CField.UIPacket.closeUI(ui));
            }

            c.getPlayer().dropMessage(5, "레벨 범위 몬스터가 없거나 " + skillname + "을 사용할 수 없는 곳입니다.");
         } else if (c.getPlayer().getBuffedValue(skillid)) {
            c.getPlayer().cancelEffect(c.getPlayer().getBuffedEffect(skillid));
            if (ui > 0) {
               if (skillid == 80003046) {
                  c.send(SLFCGPacket.FollowNpctoSkill(false, 9062524, 0));
                  c.send(SLFCGPacket.FollowNpctoSkill(false, 9062525, 0));
                  c.send(SLFCGPacket.FollowNpctoSkill(false, 9062526, 0));
               }

               c.getSession().writeAndFlush(CField.UIPacket.closeUI(ui));
            }

            c.getPlayer().dropMessage(5, skillname + "이 비활성화되었습니다.");
         } else {
            if (skillid == 80003046 && c.getPlayer().getKeyValue(100794, "today") >= (Calendar.getInstance().get(7) != 7 && Calendar.getInstance().get(7) != 1 ? 3000L : 6000L)) {
               c.getPlayer().dropMessage(5, "일일 제한 블루밍 코인을 모두 획득하여 플로라 블레싱을 활성화할 수 없습니다.");
               return;
            }

            SkillFactory.getSkill(skillid).getEffect(c.getPlayer().getSkillLevel(1)).applyTo(c.getPlayer(), 0);
            if (ui > 0) {
               c.getSession().writeAndFlush(CField.UIPacket.openUI(ui));
            }
         }

         return;
      case 100001266:
         a = "z0";
         if (c.getPlayer().getKeyValue(21770, a) != 1L) {
            c.getPlayer().setKeyValue(21770, a, "1");
         } else {
            c.getPlayer().setKeyValue(21770, a, "0");
         }

         return;
      case 135001003:
         a = "yt0";
         if (c.getPlayer().getKeyValue(21770, a) != 1L) {
            c.getPlayer().setKeyValue(21770, a, "1");
         } else {
            c.getPlayer().setKeyValue(21770, a, "0");
         }

         return;
      case 400011121:
         if (info == "") {
            c.getPlayer().updateInfoQuest(1544, "400011121=1;");
         } else if (info.contains("=0")) {
            c.getPlayer().updateInfoQuest(1544, "400011121=1;");
         } else {
            c.getPlayer().updateInfoQuest(1544, "400011121=0;");
         }

         return;
      case 400021095:
         a = "ev0";
         if (c.getPlayer().getKeyValue(21770, a) != 1L) {
            c.getPlayer().setKeyValue(21770, a, "1");
         } else {
            c.getPlayer().setKeyValue(21770, a, "0");
         }

         return;
      case 400031036:
         effect = SkillFactory.getSkill(400031036).getEffect(c.getPlayer().getTotalSkillLevel(400031036));
         if (c.getPlayer().getKeyValue(1544, skillid.makeConcatWithConstants<invokedynamic>(skillid)) != 1L) {
            c.getPlayer().setKeyValue(1544, skillid.makeConcatWithConstants<invokedynamic>(skillid), "1");
            c.getPlayer().setKeyValue(1548, "버프이펙트", "1");
         } else {
            c.getPlayer().setKeyValue(1544, skillid.makeConcatWithConstants<invokedynamic>(skillid), "0");
            c.getPlayer().setKeyValue(1548, "버프이펙트", "0");
         }

         return;
      case 400041032:
         effect = SkillFactory.getSkill(400041032).getEffect(c.getPlayer().getTotalSkillLevel(400041032));
         if (c.getPlayer().getKeyValue(1544, skillid.makeConcatWithConstants<invokedynamic>(skillid)) != 1L) {
            c.getPlayer().setKeyValue(1544, skillid.makeConcatWithConstants<invokedynamic>(skillid), "1");
            c.getPlayer().setKeyValue(1548, "버프이펙트", "1");
         } else {
            c.getPlayer().setKeyValue(1544, skillid.makeConcatWithConstants<invokedynamic>(skillid), "0");
            c.getPlayer().setKeyValue(1548, "버프이펙트", "0");
         }

         return;
      case 400041035:
         if (c.getPlayer().getKeyValue(1544, skillid.makeConcatWithConstants<invokedynamic>(skillid)) != 1L) {
            c.getPlayer().setKeyValue(1544, skillid.makeConcatWithConstants<invokedynamic>(skillid), "1");
            c.getPlayer().getMap().broadcastMessage(CField.getRefreshQuestInfo(c.getPlayer().getId(), 1, skillid, 3));
         } else {
            c.getPlayer().setKeyValue(1544, skillid.makeConcatWithConstants<invokedynamic>(skillid), "0");
            c.getPlayer().getMap().broadcastMessage(CField.getRefreshQuestInfo(c.getPlayer().getId(), 0, skillid, 3));
         }

         return;
      default:
         if (skillid == 164000010) {
            if (c.getPlayer().getKeyValue(21770, "at2") != 1L) {
               c.getPlayer().setKeyValue(21770, "at2", "1");
            } else {
               c.getPlayer().setKeyValue(21770, "at2", "0");
            }
         } else if (skillid == 164001004) {
            if (c.getPlayer().getKeyValue(21770, "at1") != 1L) {
               c.getPlayer().setKeyValue(21770, "at1", "1");
            } else {
               c.getPlayer().setKeyValue(21770, "at1", "0");
            }
         } else if (skillid == 164121005) {
            if (c.getPlayer().getKeyValue(21770, "at0") != 1L) {
               c.getPlayer().setKeyValue(21770, "at0", "1");
            } else {
               c.getPlayer().setKeyValue(21770, "at0", "0");
            }
         } else if (skillid == 151001004) {
            info = c.getPlayer().getInfoQuest(21770);
            if (info == "") {
               c.getPlayer().updateInfoQuest(21770, "lw0=1;");
            } else if (info.contains("=0")) {
               c.getPlayer().updateInfoQuest(21770, "lw0=1;");
            } else {
               c.getPlayer().updateInfoQuest(21770, "lw0=0;");
            }
         } else if (skillid == 30010110) {
            info = c.getPlayer().getInfoQuest(21770);
            if (info == "") {
               c.getPlayer().updateInfoQuest(21770, "ds0=1;");
            } else if (info.contains("=0")) {
               c.getPlayer().updateInfoQuest(21770, "ds0=1;");
            } else {
               c.getPlayer().updateInfoQuest(21770, "ds0=0;");
            }
         } else if (skillid != 37101001 && skillid != 37111003) {
            if (skillid != 155001103 && skillid != 155111207) {
               String id = "";
               List<Pair<Byte, Byte>> list = new ArrayList();
               info = c.getPlayer().getInfoQuest(21770);
               String[] info_ = info == "" ? null : info.split(";");
               info = "";

               int i;
               for(i = 0; i < 10; ++i) {
                  list.add(i, new Pair(-1, -1));
               }

               if (info_ != null) {
                  for(i = 0; i < info_.length; ++i) {
                     list.remove(Byte.parseByte(info_[i].split("=")[0]) - 1);
                     list.add(Byte.parseByte(info_[i].split("=")[0]) - 1, new Pair(Byte.parseByte(info_[i].split("=")[0]), Byte.parseByte(info_[i].split("=")[1])));
                  }
               }

               switch(skillid) {
               case 21001009:
                  id = "1";
                  break;
               case 21101011:
                  id = "2";
                  break;
               case 21101016:
                  id = "3";
                  break;
               case 21101017:
                  id = "4";
                  break;
               case 21111017:
                  id = "5";
                  break;
               case 21111019:
                  id = "6";
                  break;
               case 21111021:
                  id = "7";
                  break;
               case 21120019:
                  id = "9";
                  break;
               case 21120023:
                  id = "8";
                  break;
               case 400011031:
                  id = "10";
               }

               if (id == "") {
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                  return;
               }

               boolean changed = false;

               int j;
               for(j = 0; j < list.size(); ++j) {
                  if (((Byte)((Pair)list.get(j)).getLeft()).equals(Byte.parseByte(id))) {
                     byte value = (byte)((Byte)((Pair)list.get(j)).getRight() == 0 ? 1 : 0);
                     list.remove(j);
                     list.add(j, new Pair(Byte.parseByte(id), value));
                     changed = true;
                     break;
                  }
               }

               if (!changed) {
                  list.remove(Byte.parseByte(id) - 1);
                  list.add(Byte.parseByte(id) - 1, new Pair(Byte.parseByte(id), (byte)1));
               }

               for(j = 0; j < list.size(); ++j) {
                  if ((Byte)((Pair)list.get(j)).getLeft() != -1) {
                     if (info == "") {
                        info = ((Pair)list.get(j)).getLeft() + "=" + ((Pair)list.get(j)).getRight();
                     } else {
                        info = info + ";" + ((Pair)list.get(j)).getLeft() + "=" + ((Pair)list.get(j)).getRight();
                     }
                  }
               }

               c.getPlayer().updateInfoQuest(21770, info);
            } else if (c.getPlayer().getKeyValue(1544, String.valueOf(skillid)) == 1L) {
               c.getPlayer().setKeyValue(1544, String.valueOf(skillid), String.valueOf(0));
            } else {
               c.getPlayer().setKeyValue(1544, String.valueOf(skillid), String.valueOf(1));
            }
         } else {
            info = c.getPlayer().getInfoQuest(1544);
            info2 = skillid == 37101001 ? "bl1" : "bl0";
            if (c.getPlayer().getKeyValue(1544, info2.makeConcatWithConstants<invokedynamic>(info2)) != 1L) {
               c.getPlayer().setKeyValue(1544, info2.makeConcatWithConstants<invokedynamic>(info2), "1");
            } else {
               c.getPlayer().setKeyValue(1544, info2.makeConcatWithConstants<invokedynamic>(info2), "0");
            }
         }

      }
   }

   public static void Vmatrixstackbuff(MapleClient c, boolean use, LittleEndianAccessor slea) {
      Vmatrixstackbuff(c, use, slea, 0);
   }

   public static void Vmatrixstackbuff(MapleClient c, boolean use, LittleEndianAccessor slea, int count1) {
      if (c.getPlayer() != null) {
         HashMap<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
         int skillid = false;
         if (slea != null && slea.available() >= 4L && !use) {
            int var9 = slea.readInt();
         }

         long count;
         SecondaryStatEffect yoyo;
         if (GameConstants.isPinkBean(c.getPlayer().getJob())) {
            count = 0L;
            if (c.getPlayer().getSkillCustomValue(131001010) != null) {
               count = c.getPlayer().getSkillCustomValue(131001010);
            }

            if (use && count > 0L) {
               c.getPlayer().setSkillCustomInfo(131001010, count - 1L, 0L);
            } else if (!use && count < 8L) {
               c.getPlayer().setSkillCustomInfo(131001010, count + 1L, 0L);
            }

            count = c.getPlayer().getSkillCustomValue(131001010);
            yoyo = SkillFactory.getSkill(131001010).getEffect(1);
            statups.put(SecondaryStat.PinkbeanYoYoStack, new Pair((int)count, 0));
            c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, yoyo, c.getPlayer()));
         } else {
            long count;
            SecondaryStatEffect effect;
            if (c.getPlayer().getJob() == 1312) {
               effect = SkillFactory.getSkill(400031003).getEffect(1);
               count = 0L;
               if (!use && c.getPlayer().getSkillCustomValue(400031333) != null) {
                  return;
               }

               if (c.getPlayer().getSkillCustomValue(400031003) != null) {
                  count = c.getPlayer().getSkillCustomValue(400031003);
               }

               if (use) {
                  c.getPlayer().setSkillCustomInfo(400031003, count - (long)count1, 0L);
               } else if (!use && count < 3L) {
                  c.getPlayer().setSkillCustomInfo(400031003, count + 1L, 0L);
                  c.getPlayer().setSkillCustomInfo(400031333, 0L, (long)(effect.getX() * 1000));
               }

               count = c.getPlayer().getSkillCustomValue(400031003);
               statups.put(SecondaryStat.HowlingGale, new Pair((int)count, 0));
               c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, effect, c.getPlayer()));
            } else if (c.getPlayer().getJob() == 3312) {
               count = 0L;
               if (c.getPlayer().getSkillCustomValue(400031032) != null) {
                  count = c.getPlayer().getSkillCustomValue(400031032);
               }

               if (use && count > 0L) {
                  c.getPlayer().setSkillCustomInfo(400031032, count - 1L, 0L);
               } else if (!use && count < 8L) {
                  c.getPlayer().setSkillCustomInfo(400031032, count + 1L, 0L);
               }

               count = c.getPlayer().getSkillCustomValue(400031032);
               yoyo = SkillFactory.getSkill(400031032).getEffect(1);
               statups.put(SecondaryStat.WildGrenadier, new Pair((int)count, 0));
               c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, yoyo, c.getPlayer()));
            } else if (c.getPlayer().getJob() == 232) {
               count = 0L;
               if (c.getPlayer().getSkillCustomValue(400021086) != null) {
                  count = c.getPlayer().getSkillCustomValue(400021086);
               }

               if (use && count > 0L) {
                  c.getPlayer().setSkillCustomInfo(400021086, count - 1L, 0L);
               } else if (!use && count < 8L) {
                  c.getPlayer().setSkillCustomInfo(400021086, count + 1L, 0L);
               }

               count = c.getPlayer().getSkillCustomValue(400021086);
               yoyo = SkillFactory.getSkill(400021086).getEffect(1);
               statups.put(SecondaryStat.VMatrixStackBuff, new Pair((int)count, 0));
               c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, yoyo, c.getPlayer()));
            } else if (c.getPlayer().getJob() == 122) {
               count = 0L;
               if (c.getPlayer().getSkillCustomValue(400011131) != null) {
                  count = c.getPlayer().getSkillCustomValue(400011131);
               }

               if (use && count > 0L) {
                  c.getPlayer().setSkillCustomInfo(400011131, count - 1L, 0L);
               } else if (!use && count < 2L) {
                  c.getPlayer().setSkillCustomInfo(400011131, count + 1L, 0L);
               }

               count = c.getPlayer().getSkillCustomValue(400011131);
               yoyo = SkillFactory.getSkill(400011131).getEffect(1);
               statups.put(SecondaryStat.VMatrixStackBuff, new Pair((int)count, 0));
               c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, yoyo, c.getPlayer()));
            } else if (c.getPlayer().getJob() != 531 && c.getPlayer().getJob() != 532) {
               if (c.getPlayer().getJob() == 3212) {
                  count = 0L;
                  if (c.getPlayer().getSkillCustomValue(400021047) != null) {
                     count = c.getPlayer().getSkillCustomValue(400021047);
                  }

                  if (use && count > 0L) {
                     c.getPlayer().setSkillCustomInfo(400021047, count - 1L, 0L);
                  } else if (!use && count < 4L) {
                     c.getPlayer().setSkillCustomInfo(400021047, count + 1L, 0L);
                  }

                  count = c.getPlayer().getSkillCustomValue(400021047);
                  yoyo = SkillFactory.getSkill(400021047).getEffect(1);
                  statups.put(SecondaryStat.VMatrixStackBuff, new Pair((int)count, 0));
                  c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, yoyo, c.getPlayer()));
               } else if (c.getPlayer().getJob() == 512) {
                  effect = SkillFactory.getSkill(400051042).getEffect(c.getPlayer().getSkillLevel(400051042));
                  count = 0L;
                  if (c.getPlayer().getSkillCustomValue(400051042) != null) {
                     count = c.getPlayer().getSkillCustomValue(400051042);
                  }

                  if (use && count > 0L) {
                     c.getPlayer().setSkillCustomInfo(400051042, count - 1L, 0L);
                  } else if (!use && count < 6L) {
                     c.getPlayer().setSkillCustomInfo(400051042, count + 1L, 0L);
                  }

                  count = c.getPlayer().getSkillCustomValue(400051042);
                  statups.put(SecondaryStat.VMatrixStackBuff, new Pair((int)count, 0));
                  c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, effect, c.getPlayer()));
               } else if (GameConstants.isPathFinder(c.getPlayer().getJob())) {
                  effect = SkillFactory.getSkill(3321006).getEffect(c.getPlayer().getSkillLevel(3321006));
                  count = 0L;
                  if (c.getPlayer().getSkillCustomValue(3321006) != null) {
                     count = c.getPlayer().getSkillCustomValue(3321006);
                  }

                  if (use && count > 0L) {
                     c.getPlayer().setSkillCustomInfo(3321006, count - 1L, 0L);
                  } else if (!use && count < 5L) {
                     c.getPlayer().setSkillCustomInfo(3321006, count + 1L, 0L);
                  }

                  count = c.getPlayer().getSkillCustomValue(3321006);
                  statups.put(SecondaryStat.VMatrixStackBuff, new Pair((int)count, 0));
                  c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, effect, c.getPlayer()));
               } else if (c.getPlayer().getJob() == 15212) {
                  effect = SkillFactory.getSkill(400021068).getEffect(1);
                  count = 0L;
                  if (c.getPlayer().getSkillCustomValue(400021068) != null) {
                     count = c.getPlayer().getSkillCustomValue(400021068);
                  }

                  if (use && count > 0L) {
                     c.getPlayer().setSkillCustomInfo(400021068, count - 1L, 0L);
                  } else if (!use && count < 2L) {
                     c.getPlayer().setSkillCustomInfo(400021068, count + 1L, 0L);
                  }

                  count = c.getPlayer().getSkillCustomValue(400021068);
                  statups.put(SecondaryStat.VMatrixStackBuff, new Pair((int)count, 0));
                  c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, effect, c.getPlayer()));
               } else if (c.getPlayer().getJob() == 6412) {
                  effect = SkillFactory.getSkill(400041074).getEffect(1);
                  count = 0L;
                  if (c.getPlayer().getSkillCustomValue(400041074) != null) {
                     count = c.getPlayer().getSkillCustomValue(400041074);
                  }

                  if (use && count > 0L) {
                     c.getPlayer().setSkillCustomInfo(400041074, count - 1L, 0L);
                  } else if (!use && count < 3L) {
                     c.getPlayer().setSkillCustomInfo(400041074, count + 1L, 0L);
                  }

                  count = c.getPlayer().getSkillCustomValue(400041074);
                  statups.put(SecondaryStat.WeaponVarietyFinale, new Pair((int)count, 0));
                  c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, effect, c.getPlayer()));
               } else if (GameConstants.isYeti(c.getPlayer().getJob())) {
                  effect = SkillFactory.getSkill(135001007).getEffect(1);
                  count = 0L;
                  if (c.getPlayer().getSkillCustomValue(135001007) != null) {
                     count = c.getPlayer().getSkillCustomValue(135001007);
                  }

                  if (use && count > 0L) {
                     c.getPlayer().setSkillCustomInfo(135001007, count - 1L, 0L);
                  } else if (!use && count < (long)(c.getPlayer().getBuffedEffect(SecondaryStat.YetiAngerMode) != null ? 3 : 2)) {
                     c.getPlayer().setSkillCustomInfo(135001007, count + 1L, 0L);
                  }

                  effect.applyTo(c.getPlayer());
               }
            } else {
               count = 0L;
               if (c.getPlayer().getSkillCustomValue(5311013) != null) {
                  count = c.getPlayer().getSkillCustomValue(5311013);
               } else if (c.getPlayer().getSkillCustomValue(5311013) == null) {
                  c.getPlayer().setSkillCustomInfo(5311013, count, 0L);
               }

               if (c.getPlayer().getSkillCustomValue(5311013) != 5L) {
                  if (use && count > 0L) {
                     c.getPlayer().setSkillCustomInfo(5311013, count - 1L, 0L);
                  } else if (!use && count < 5L) {
                     c.getPlayer().setSkillCustomInfo(5311013, count + 1L, 0L);
                  }

                  count = c.getPlayer().getSkillCustomValue(5311013);
                  yoyo = SkillFactory.getSkill(5311013).getEffect(1);
                  statups.put(SecondaryStat.MiniCannonBall, new Pair((int)count, 0));
                  c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, yoyo, c.getPlayer()));
               }

               if (c.getPlayer().getJob() == 532) {
                  count = 0L;
                  if (c.getPlayer().getSkillCustomValue(400051008) != null) {
                     count = c.getPlayer().getSkillCustomValue(400051008);
                  }

                  if (use && count > 0L) {
                     c.getPlayer().setSkillCustomInfo(400051008, count - 1L, 0L);
                  } else if (!use && count < 3L) {
                     c.getPlayer().setSkillCustomInfo(400051008, count + 1L, 0L);
                  }

                  count = c.getPlayer().getSkillCustomValue(400051008);
                  yoyo = SkillFactory.getSkill(400051008).getEffect(1);
                  statups.put(SecondaryStat.VMatrixStackBuff, new Pair((int)count, 0));
                  c.getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, yoyo, c.getPlayer()));
               }
            }
         }

      }
   }

   public static void SilhouEtteMirage(MapleCharacter player, LittleEndianAccessor slea) {
      int skillid = slea.readInt();
      if (player.getBuffedValue(400031053) && player.getSkillCustomValue0(400031053) < 2L) {
         player.setSkillCustomInfo(400031053, player.getSkillCustomValue0(400031053) + 1L, 0L);
         player.getBuffedEffect(400031053).applyTo(player);
      }

   }

   public static void ChangeDragonImg(MapleCharacter chr, LittleEndianAccessor slea) {
      int unk = slea.readInt();
      int skillid = slea.readInt();
      int skilllevel = slea.readInt();
      chr.getMap().broadcastMessage(chr, CField.getDragonForm(chr, unk, skillid, skilllevel), false);
   }

   public static void AttackDragonImg(MapleCharacter chr, LittleEndianAccessor slea) {
      int skillid = slea.readInt();
      int skilllevel = slea.readInt();
      Point pos = slea.readIntPos();
      Point pos1 = slea.readIntPos();
      chr.getMap().broadcastMessage(chr, CField.getDragonAttack(chr, skillid, skilllevel, pos, pos1), false);
   }

   public static void PhantomShroud(LittleEndianAccessor slea, MapleClient c) {
      int skillid = slea.readInt();
      if (skillid == 20031205) {
         c.getPlayer().setSkillCustomInfo(skillid, c.getPlayer().getSkillCustomValue0(skillid) + 1L, 0L);
      }

   }

   public static void LiftBreak(MapleCharacter player, LittleEndianAccessor slea) {
      slea.skip(4);
      slea.skip(1);
      slea.skip(4);
      slea.skip(4);
      slea.skip(1);
      int skillid = slea.readInt();
      if (skillid == 64121013 || skillid == 64121014 || skillid == 64121015) {
         player.cancelEffect(player.getBuffedEffect(64120006), (List)null, true);
         SkillFactory.getSkill(64120006).getEffect(player.getSkillLevel(64120006)).applyTo(player);
      }

      if (skillid != 400051078 && skillid != 63121008) {
         slea.skip(4);
         slea.skip(4);
         slea.skip(2);
         slea.skip(4);
         slea.skip(4);
         slea.skip(1);
         int count = slea.readInt();
         slea.skip(4);
         if (skillid != 11121056 && skillid != 11121055) {
            SecondaryStatEffect effect = SkillFactory.getSkill(skillid).getEffect(player.getSkillLevel(skillid));
            if (effect != null) {
               player.changeCooldown(skillid, -((int)((double)count * effect.getT() * 1000.0D)));
            }
         }
      } else {
         slea.skip(4);
         slea.skip(4);
         slea.skip(2);
         slea.skip(4);
      }

   }

   public static void Magunmblow(LittleEndianAccessor slea, MapleClient c) {
      int skillid = slea.readInt();
      SecondaryStatEffect effect = SkillFactory.getSkill(skillid).getEffect(c.getPlayer().getSkillLevel(skillid));
      if (skillid == 37121052 && c.getPlayer().getSkillCustomValue(skillid) < 4L) {
         c.getPlayer().addSkillCustomInfo(skillid, 1L);
         effect.applyTo(c.getPlayer());
      } else if (skillid == 131001020) {
         effect.applyTo(c.getPlayer(), false);
      }

   }

   public static void activeRestoreBuff(MapleClient c) {
      if (c != null) {
         MapleCharacter player = c.getPlayer();
         if (player.getParty() != null) {
            MapleParty party = player.getParty();
            SecondaryStatEffect effect = player.getBuffedEffect(SecondaryStat.Restore);
            if (effect != null) {
               c.getPlayer().getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(c.getPlayer(), 0, 400011109, 7, 0, 0, (byte)(c.getPlayer().isFacingLeft() ? 1 : 0), true, c.getPlayer().getTruePosition(), (String)null, (Item)null));
               c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.EffectPacket.showEffect(c.getPlayer(), 0, 400011109, 7, 0, 0, (byte)(c.getPlayer().isFacingLeft() ? 1 : 0), false, c.getPlayer().getTruePosition(), (String)null, (Item)null), false);
               c.getPlayer().addHP((long)((double)(c.getPlayer().getStat().getCurrentMaxHp() / 100L) * effect.getT()));
               if (party != null) {
                  Iterator var4 = party.getMembers().iterator();

                  while(var4.hasNext()) {
                     MaplePartyCharacter pc = (MaplePartyCharacter)var4.next();
                     MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(pc.getName());
                     if (victim != null && victim.getId() != c.getPlayer().getId() && player.getTruePosition().x + effect.getLt().x < victim.getTruePosition().x && player.getTruePosition().x - effect.getLt().x > victim.getTruePosition().x && player.getTruePosition().y + effect.getLt().y < victim.getTruePosition().y && player.getTruePosition().y - effect.getLt().y > victim.getTruePosition().y) {
                        victim.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(victim, 0, 400011109, 4, 0, 0, (byte)(victim.isFacingLeft() ? 1 : 0), true, victim.getTruePosition(), (String)null, (Item)null));
                        victim.getMap().broadcastMessage(victim, CField.EffectPacket.showEffect(victim, 0, 400011109, 4, 0, 0, (byte)(victim.isFacingLeft() ? 1 : 0), false, victim.getTruePosition(), (String)null, (Item)null), false);
                        victim.addHP((long)((double)(victim.getStat().getCurrentMaxHp() / 100L) * effect.getT()));
                     }
                  }
               }
            }
         }
      }

   }

   public static void HarmonyLink(LittleEndianAccessor slea, MapleClient c) {
      List<MapleMonster> moblist = new ArrayList();
      List<MapleCharacter> chrlist = new ArrayList();
      new ArrayList();
      int skillid = slea.readInt();
      slea.readInt();
      Point pos = slea.readPos();
      int mobsize = slea.readInt();

      int chrsize;
      for(chrsize = 0; chrsize < mobsize; ++chrsize) {
         MapleMonster mob = c.getPlayer().getMap().getMonsterByOid(slea.readInt());
         if (mob != null) {
            moblist.add(mob);
         }
      }

      chrsize = slea.readInt();

      MapleCharacter chr;
      for(int j = 0; j < chrsize; ++j) {
         chr = c.getPlayer().getMap().getCharacterById(slea.readInt());
         if (chr != null) {
            chrlist.add(chr);
         }
      }

      if (skillid == 152111007) {
         SecondaryStatEffect curseMark;
         Iterator var14;
         MapleMonster mob;
         if (moblist != null) {
            for(var14 = moblist.iterator(); var14.hasNext(); mob.applyStatus(c, MonsterStatus.MS_CurseMark, new MonsterStatusEffect(152000010, curseMark.getDuration()), (int)mob.getCustomValue0(152000010), curseMark)) {
               mob = (MapleMonster)var14.next();
               curseMark = SkillFactory.getSkill(152000010).getEffect(c.getPlayer().getSkillLevel(152000010));
               int max = c.getPlayer().getSkillLevel(152100012) > 0 ? 5 : (c.getPlayer().getSkillLevel(152110010) > 0 ? 3 : 1);
               if (mob.getBuff(152000010) == null && mob.getCustomValue0(152000010) > 0L) {
                  mob.removeCustomInfo(152000010);
               }

               if (mob.getCustomValue0(152000010) < (long)max) {
                  mob.addSkillCustomInfo(152000010, 1L);
               }
            }
         }

         if (chrlist != null) {
            for(var14 = chrlist.iterator(); var14.hasNext(); SkillFactory.getSkill(152000009).getEffect(c.getPlayer().getSkillLevel(152000009)).applyTo(chr)) {
               chr = (MapleCharacter)var14.next();
               curseMark = SkillFactory.getSkill(skillid).getEffect(c.getPlayer().getSkillLevel(skillid));
               chr.addHP(chr.getStat().getCurrentMaxHp() / 100L * (long)curseMark.getDamage());
               chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, 152111007, 4, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), true, chr.getTruePosition(), (String)null, (Item)null));
               chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 0, 152111007, 4, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), false, chr.getTruePosition(), (String)null, (Item)null), false);
               if (c.getPlayer().getSkillLevel(152120012) > 0) {
                  chr.blessMarkSkill = 152120012;
               } else if (c.getPlayer().getSkillLevel(152110009) > 0) {
                  chr.blessMarkSkill = 152110009;
               } else if (c.getPlayer().getSkillLevel(152000007) > 0) {
                  chr.blessMarkSkill = 152000007;
               }
            }
         }
      }

   }

   public static void ForceAtomEffect(LittleEndianAccessor slea, MapleClient c) {
      int unk2 = false;
      boolean left = false;
      int type = slea.readInt();
      int atomid = slea.readInt();
      int unk = slea.readInt();
      if (type == 2) {
         int unk2 = slea.readInt();
         left = slea.readByte() == 1;
      }

      new Point(0, 0);
      new Point(0, 0);
      if (slea.available() > 4L) {
         Point var7 = slea.readPos();
      }

      if (slea.available() > 4L) {
         Point var8 = slea.readPos();
      }

   }

   public static void SelectReincarnation(LittleEndianAccessor slea, MapleClient c) {
      MapleCharacter chr = c.getPlayer();
      int type = slea.readInt();
      chr.리인카네이션 = type;
      SkillFactory.getSkill(1321020).getEffect(chr.getTotalSkillLevel(1321020)).applyTo(chr);
   }

   public static void SelectHolyUnity(LittleEndianAccessor slea, MapleClient c) {
      MapleCharacter chr = c.getPlayer().getMap().getCharacter(slea.readInt());
      MapleCharacter beforechr = c.getPlayer().getMap().getCharacter((int)c.getPlayer().getSkillCustomValue0(400011003));
      Point mypos = slea.readIntPos();
      Point targetpos = slea.readIntPos();
      if (!chr.getBuffedValue(400011021)) {
         SecondaryStatEffect effect = c.getPlayer().getBuffedEffect(SecondaryStat.HolyUnity);
         Rectangle box = effect.calculateBoundingBox(c.getPlayer().getPosition(), true);
         Rectangle box1 = effect.calculateBoundingBox(c.getPlayer().getPosition(), false);
         if (!box.contains(targetpos) && !box1.contains(targetpos)) {
            c.getPlayer().dropMessage(5, "파티원 " + chr.getName() + "님을 결속할 수 없습니다.");
         } else {
            if (beforechr != null) {
               while(beforechr.getBuffedValue(400011021)) {
                  beforechr.cancelEffect(beforechr.getBuffedEffect(400011021));
               }
            }

            Map<SecondaryStat, Pair<Integer, Integer>> localstatups = new HashMap();
            localstatups.clear();
            localstatups.put(SecondaryStat.HolyUnity, new Pair(chr.getId(), (int)c.getPlayer().getBuffLimit(400011003)));
            c.send(CWvsContext.BuffPacket.giveBuff(localstatups, effect, c.getPlayer()));
            c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CWvsContext.BuffPacket.giveForeignBuff(c.getPlayer(), localstatups, effect), false);
            chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 0, 400011003, 1, 0, 0, (byte)(chr.getTruePosition().x > chr.getPosition().x ? 1 : 0), false, chr.getPosition(), (String)null, (Item)null), false);
            c.getPlayer().setSkillCustomInfo(400011003, (long)chr.getId(), 0L);
            SkillFactory.getSkill(400011021).getEffect(c.getPlayer().getSkillLevel(400011003)).applyTo(c.getPlayer(), chr, false, chr.getPosition(), (int)c.getPlayer().getBuffLimit(400011003), (byte)0, false);
         }
      }

   }

   public static void TangyoonCooking(LittleEndianAccessor slea, MapleClient c) {
      MapleCharacter chr = c.getPlayer();
      int mobid = slea.readInt();
      int[] success = null;
      String Recipe = "";
      String talk = "";
      switch((int)chr.getKeyValue(2498, "TangyoonCooking")) {
      case 0:
         success = new int[]{9300654, 9300655, 9300656, 9300657, 9300658};
         Recipe = "돼지고기볶음";
         break;
      case 1:
         success = new int[]{9300659, 9300660, 9300661, 9300662, 9300663};
         Recipe = "달팽이요리";
         break;
      case 2:
         success = new int[]{9300664, 9300665, 9300666, 9300667, 9300668};
         Recipe = "해파리냉채";
         break;
      case 3:
         success = new int[]{9300669, 9300670, 9300671, 9300672, 9300673};
         Recipe = "버섯칼국수";
         break;
      case 4:
         success = new int[]{9300674, 9300675, 9300676, 9300677, 9300678};
         Recipe = "슬라임푸딩";
      }

      if (mobid == success[(int)chr.getKeyValue(2498, "TangyoonCookingClass") - 1]) {
         talk = "\"" + Recipe + "\"에 " + getMobName(mobid) + "를 넣기로 결정했다. 맛있는 요리를 기대해도 좋을 것 같다.";
      } else {
         talk = "\"" + Recipe + "\"에 " + getMobName(mobid) + "를 넣기로 결정했다. 어쩐지 냄비에서 이상한 냄새가 나는 것 같다.";
         c.getPlayer().setKeyValue(2498, "TangyoonBoss", "1");
      }

      c.getSession().writeAndFlush(CField.getGameMessage(11, talk));
      c.getSession().writeAndFlush(CWvsContext.getTopMsg(talk));
      if ((int)chr.getKeyValue(2498, "TangyoonCookingClass") == 4) {
         c.getSession().writeAndFlush(CField.UIPacket.greenShowInfo("\"" + c.getPlayer().getName() + "\"님께서 불꽃몬스터 사냥꾼으로 임명되었습니다."));
         MapleMap map = c.getChannelServer().getMapFactory().getMap(912080100);
         int a = false;

         for(int i = 0; i < 5; ++i) {
            int a = Randomizer.rand(-281, 725);
            map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9300679), new Point(a, 150));
         }

         c.getSession().writeAndFlush(CField.EffectPacket.showNormalEffect(c.getPlayer(), 54, true));
         GameConstants.TangYoonMobDelete(c, mobid, false, true, false);
      } else {
         GameConstants.TangyoonMobSpawn(c, mobid, false);
      }

   }

   public static String getMobName(int mobid) {
      MapleData data = null;
      MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wz") + "/String.wz"));
      String ret = "";
      data = dataProvider.getData("Mob.img");
      List<Pair<Integer, String>> mobPairList = new LinkedList();
      Iterator var5 = data.getChildren().iterator();

      while(var5.hasNext()) {
         MapleData mobIdData = (MapleData)var5.next();
         mobPairList.add(new Pair(Integer.parseInt(mobIdData.getName()), MapleDataTool.getString(mobIdData.getChildByPath("name"), "NO-NAME")));
      }

      var5 = mobPairList.iterator();

      while(var5.hasNext()) {
         Pair<Integer, String> mobPair = (Pair)var5.next();
         if ((Integer)mobPair.getLeft() == mobid) {
            ret = (String)mobPair.getRight();
         }
      }

      return ret;
   }

   public static void TangyoonSalt(LittleEndianAccessor slea, MapleClient c) {
      c.getSession().writeAndFlush(CField.achievementRatio(100));
      slea.skip(1);
      int salt = slea.readByte();
      if (salt != 1) {
         c.getPlayer().setKeyValue(2498, "TangyoonBoss", "1");
      }

      boolean fail = (int)c.getPlayer().getKeyValue(2498, "TangyoonBoss") == 1;
      String talk = "";
      int mobid = false;
      int x = false;
      int mobid;
      short x;
      if ((int)c.getPlayer().getKeyValue(2498, "TangyoonBoss") == 1) {
         talk = "끔찍한 맛의 요리를 만들었다. 냄새를 맡고 달려온 쓰레기통을 처치하자.";
         x = 1320;
         mobid = 9300681;
         fail = true;
      } else {
         talk = "맛있는 요리를 만드는데 성공했다. 냄새를 맡고 달려온 식신을 처치하자.";
         x = 523;
         mobid = 9300680;
         fail = false;
      }

      c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mobid), new Point(x, -69));
      c.getSession().writeAndFlush(TangyoonBOSS(fail));
      c.getSession().writeAndFlush(CField.getGameMessage(11, talk));
      c.getSession().writeAndFlush(CWvsContext.getTopMsg(talk));
      GameConstants.TangYoonMobDelete(c, mobid, false, false, true);
   }

   public static byte[] TangyoonBOSS(boolean fail) {
      MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
      mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
      mplew.write((int)17);
      if (fail) {
         mplew.writeMapleAsciiString("tangyoon/trash");
      } else {
         mplew.writeMapleAsciiString("tangyoon/food");
      }

      mplew.writeInt(3000);
      return mplew.getPacket();
   }

   public static void PyretBless(LittleEndianAccessor slea, MapleClient c) {
      int skillid = slea.readInt();
      if (c.getPlayer().getKeyValue(1548, "onoff") != 1L) {
         c.getPlayer().setKeyValue(1548, "onoff", "1");
      } else {
         c.getPlayer().setKeyValue(1548, "onoff", "0");
      }

   }

   public static void ZeroShockWave(LittleEndianAccessor slea, MapleClient c) {
      slea.skip(4);
      int skillid = slea.readInt();
      Point pos = slea.readPos();
      slea.skip(1);
      SecondaryStatEffect a = SkillFactory.getSkill(skillid).getEffect(c.getPlayer().getSkillLevel(skillid));
      MapleMist newmist = new MapleMist(a.calculateBoundingBox(pos, c.getPlayer().isFacingLeft()), c.getPlayer(), a, 3000, (byte)(c.getPlayer().isFacingLeft() ? 1 : 0));
      newmist.setPosition(pos);
      newmist.setDelay(0);
      c.getPlayer().getMap().spawnMist(newmist, false);
   }

   public static void PoisonLegion(LittleEndianAccessor slea, MapleClient c) {
      slea.skip(5);
      Point pos = slea.readPos();
      slea.skip(4);
      Point pos2 = slea.readPos();
      SecondaryStatEffect a = SkillFactory.getSkill(2111013).getEffect(c.getPlayer().getSkillLevel(2111013));
      MapleMist newmist = new MapleMist(a.calculateBoundingBox(pos, c.getPlayer().isFacingLeft()), c.getPlayer(), a, 6000, (byte)(c.getPlayer().isFacingLeft() ? 1 : 0));
      newmist.setPosition(pos);
      newmist.setDelay(95);
      c.getPlayer().getMap().spawnMist(newmist, false);
   }

   public static void ZeroLuckyScroll(MapleClient c, LittleEndianAccessor slea) {
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      slea.skip(4);
      int slot = slea.readShort();
      slea.skip(2);
      Item beta = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
      Item scroll = c.getPlayer().getInventory(MapleInventoryType.USE).getItem((short)slot);
      Equip nbetatype = (Equip)beta;
      switch(scroll.getItemId()) {
      case 2048900:
      case 2048901:
      case 2048902:
      case 2048903:
      case 2048904:
      case 2048905:
      case 2048906:
      case 2048907:
      case 2048912:
      case 2048913:
      case 2048915:
      case 2048918:
         if (!Randomizer.isSuccess(ii.getSuccess(scroll.getItemId(), c.getPlayer(), nbetatype))) {
            c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.showSoulScrollEffect(c.getPlayer().getId(), (byte)0, false, nbetatype), true);
         } else {
            MapleQuest quest = MapleQuest.getInstance(41907);
            String stringa = String.valueOf(GameConstants.getLuckyInfofromItemId(scroll.getItemId()));
            c.getPlayer().setKeyValue(46523, "luckyscroll", stringa);
            MapleQuestStatus queststatus = new MapleQuestStatus(quest, 1);
            queststatus.setCustomData(stringa == null ? "0" : stringa);
            c.getPlayer().updateQuest(queststatus, true);
            c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.showSoulScrollEffect(c.getPlayer().getId(), (byte)1, false, nbetatype), true);
            MapleInventoryManipulator.removeById(c.getPlayer().getClient(), MapleInventoryType.USE, scroll.getItemId(), 1, true, false);
         }
      case 2048908:
      case 2048909:
      case 2048910:
      case 2048911:
      case 2048914:
      case 2048916:
      case 2048917:
      default:
         c.getPlayer().getClient().send(CWvsContext.enableActions(c.getPlayer()));
      }
   }

   public static void KinesisGround(MapleClient c, LittleEndianAccessor slea) {
      int skillid = slea.readInt();
      short unk = slea.readShort();
      int posx = slea.readInt();
      slea.skip(1);
      int posy = slea.readInt();
      slea.skip(4);
      int size = slea.readShort();

      for(int i = 0; i < size; ++i) {
         int mobid = slea.readInt();
         MapleMonster monster = c.getPlayer().getMap().getMonsterByOid(mobid);
         SecondaryStatEffect eff = SkillFactory.getSkill(skillid).getEffect(c.getPlayer().getSkillLevel(skillid));
         List<Triple<MonsterStatus, MonsterStatusEffect, Long>> statusz = new ArrayList();
         List<Pair<MonsterStatus, MonsterStatusEffect>> applys = new ArrayList();
         if ((skillid == 142111006 || skillid == 142120003) && monster != null) {
            int effect = skillid == 142120003 ? 3 : 2;
            if (c.getPlayer().getSkillLevel(142120036) > 0) {
               effect *= 2;
            }

            if (monster.getBuff(skillid) == null) {
               statusz.add(new Triple(MonsterStatus.MS_PsychicGroundMark, new MonsterStatusEffect(skillid, eff.getDuration()), (long)(size * effect)));
               statusz.add(new Triple(MonsterStatus.MS_Speed, new MonsterStatusEffect(skillid, eff.getDuration()), (long)(-size)));
               statusz.add(new Triple(MonsterStatus.MS_IndieMdr, new MonsterStatusEffect(skillid, eff.getDuration()), (long)(-(size * effect))));
               statusz.add(new Triple(MonsterStatus.MS_IndiePdr, new MonsterStatusEffect(skillid, eff.getDuration()), (long)(-(size * effect))));
               Iterator var14 = statusz.iterator();

               while(var14.hasNext()) {
                  Triple<MonsterStatus, MonsterStatusEffect, Long> status = (Triple)var14.next();
                  if (status.left != null && status.mid != null) {
                     ((MonsterStatusEffect)status.mid).setValue((Long)status.right);
                     applys.add(new Pair((MonsterStatus)status.left, (MonsterStatusEffect)status.mid));
                  }
               }

               monster.applyStatus(c, applys, eff);
            }
         }
      }

      slea.skip(2);
      if (skillid == 400021008) {
         MapleCharacter player = c.getPlayer();
         SecondaryStatEffect eff = SkillFactory.getSkill(400021008).getEffect(player.getSkillLevel(400021008));
         player.setSkillCustomInfo(400021008, player.getSkillCustomValue0(400021008) + 1L, 0L);
         if (player.getSkillCustomValue0(400021008) >= 20L) {
            player.setSkillCustomInfo(400021008, 0L, 0L);
            player.setSkillCustomInfo(400021009, player.getSkillCustomValue0(400021009) + 1L, 0L);
            int duration = (int)player.getBuffLimit(400021008);
            eff.applyTo(player, duration);
         }
      }

   }

   public static void MedalReissuance(MapleClient c, LittleEndianAccessor slea) {
      int questid = slea.readInt();
      int itemid = slea.readInt();
      if (c.getPlayer().getQuestStatus(questid) == 2) {
         MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
         if (!MapleInventoryManipulator.checkSpace(c, itemid, 1, "")) {
            return;
         }

         c.getPlayer().gainItem(itemid, 1);
         c.getPlayer().gainMeso(-100L, false);
      }

   }

   public static void PinkBeanRollingGrade(MapleClient c, LittleEndianAccessor slea) {
      SkillFactory.getSkill(131001004).getEffect(c.getPlayer().getSkillLevel(131001004)).applyTo(c.getPlayer(), false);
   }

   public static final void handlePoisonRegion(LittleEndianAccessor slea, MapleClient c) {
      SecondaryStatEffect Effect = SkillFactory.getSkill(2111013).getEffect(c.getPlayer().getSkillLevel(2111013));
      byte Unk = slea.readByte();
      int Size = slea.readInt();

      for(int i = 0; i < Size; ++i) {
         Point Position = slea.readIntPos();
         MapleMist mist = new MapleMist(Effect.calculateBoundingBox(Position, c.getPlayer().isFacingLeft()), c.getPlayer(), Effect, 30000, (byte)(c.getPlayer().isFacingLeft() ? 1 : 0));
         mist.setPosition(Position);
         mist.setStartTime(System.currentTimeMillis());
         c.getPlayer().getMap().spawnMist(mist, false);
      }

   }

   public static void DebuffObjHit(MapleClient c, LittleEndianAccessor slea) {
      int type = slea.readInt();
      int id = slea.readInt();
      if (c.getPlayer().isAlive() && c.getPlayer().getBuffedEffect(SecondaryStat.NotDamaged) == null && c.getPlayer().getBuffedEffect(SecondaryStat.IndieNotDamaged) == null) {
         c.getPlayer().giveDebuff(SecondaryStat.GiveMeHeal, MobSkillFactory.getMobSkill(182, 1));
      }

   }

   public static void NoteHandle(MapleClient c, LittleEndianAccessor slea) {
      switch(slea.readByte()) {
      case 0:
         String name = slea.readMapleAsciiString();
         String msg = slea.readMapleAsciiString();
         int ch = World.Find.findChannel(name);
         MapleCharacter target = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(name);
         if (target != null) {
            target.getClient().send(CSPacket.NoteHandler(16, 0));
            c.getPlayer().sendNote(name, msg, 6, c.getPlayer().getId());
            c.getPlayer().sendNote(name, msg, 7, c.getPlayer().getId());
            c.getPlayer().showsendNote();
            c.send(CSPacket.NoteHandler(8, 0));
            int id = 0;
            Connection con = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
               con = DatabaseConnection.getConnection();
               ps = con.prepareStatement("SELECT * FROM `notes` WHERE `from` = ? AND `message` = ? AND `type` = 7");
               ps.setString(1, name);
               ps.setString(2, msg);
               rs = ps.executeQuery();
               if (rs.next()) {
                  id = rs.getInt("id");
               }

               rs.close();
               ps.close();
               con.close();
            } catch (SQLException var19) {
               System.err.println("Error getting character default" + var19);
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
               } catch (Exception var18) {
               }

               c.send(CSPacket.SendNote(id, target.getId(), c.getPlayer(), name, msg, System.currentTimeMillis()));
            }
         }
         break;
      case 4:
         c.getPlayer().showNote();
      }

   }

   public static void NoteHandler(MapleClient c, LittleEndianAccessor slea) {
      int id;
      switch(slea.readByte()) {
      case 2:
         slea.skip(4);
         id = slea.readInt();
         c.send(CSPacket.NoteHandler(11, id));
         c.getPlayer().deleteNote(id, 0);
         break;
      case 3:
         slea.skip(4);
         id = slea.readInt();
         c.send(CSPacket.NoteHandler(13, id));
         c.getPlayer().deleteNote(id, 0);
      case 4:
      default:
         break;
      case 5:
         slea.skip(1);
         id = slea.readInt();
         c.getPlayer().showNotes(id);
      }

   }

   public static void MapleCabiNet(MapleClient c, LittleEndianAccessor slea) {
      List cabinet;
      ArrayList remove;
      boolean auto;
      Iterator var15;
      boolean change;
      MapleCabinet add;
      Iterator var18;
      MapleCabinet cr;
      switch(slea.readInt()) {
      case 0:
         cabinet = c.getCabiNet();
         Collections.reverse(cabinet);
         if (!cabinet.isEmpty()) {
            remove = new ArrayList();
            auto = false;
            var15 = cabinet.iterator();

            label176:
            while(true) {
               do {
                  if (!var15.hasNext()) {
                     change = false;
                     var18 = remove.iterator();

                     while(var18.hasNext()) {
                        cr = (MapleCabinet)var18.next();
                        change = true;
                        cabinet.remove(cr);
                        if (auto) {
                           c.getPlayer().dropMessage(5, "[알림] 보관함에 있던 " + MapleItemInformationProvider.getInstance().getName(cr.getItemid()) + " (이) 가 보관 기간이 만료되어 삭제 되었습니다.");
                        }
                     }

                     if (change) {
                        Collections.reverse(cabinet);
                        c.saveCabiNet(cabinet);
                     }
                     break label176;
                  }

                  add = (MapleCabinet)var15.next();
               } while(add.getSaveTime() > PacketHelper.getKoreanTimestamp(System.currentTimeMillis()) && add.getDelete() != 1);

               if (add.getSaveTime() <= PacketHelper.getKoreanTimestamp(System.currentTimeMillis())) {
                  auto = true;
               }

               remove.add(add);
            }
         }

         c.send(CField.getMapleCabinetList(cabinet, false, 0, false));
         break;
      case 1:
         cabinet = c.getCabiNet();
         if (!cabinet.isEmpty()) {
            remove = new ArrayList();
            auto = false;
            var15 = cabinet.iterator();

            while(true) {
               do {
                  if (!var15.hasNext()) {
                     change = false;
                     var18 = remove.iterator();

                     while(var18.hasNext()) {
                        cr = (MapleCabinet)var18.next();
                        change = true;
                        cabinet.remove(cr);
                        if (auto) {
                           c.getPlayer().dropMessage(5, "[알림] 보관함에 있던 " + MapleItemInformationProvider.getInstance().getName(cr.getItemid()) + " (이) 가 보관 기간이 만료되어 삭제 되었습니다.");
                        }
                     }

                     if (change) {
                        c.saveCabiNet(cabinet);
                     }

                     return;
                  }

                  add = (MapleCabinet)var15.next();
               } while(add.getSaveTime() > PacketHelper.getKoreanTimestamp(System.currentTimeMillis()) && add.getDelete() != 1);

               if (add.getSaveTime() <= PacketHelper.getKoreanTimestamp(System.currentTimeMillis())) {
                  auto = true;
               }

               remove.add(add);
            }
         }
      case 2:
      case 3:
      default:
         break;
      case 4:
         int get = slea.readInt();
         boolean give = true;

         for(int i = 1; i <= 6; ++i) {
            if (c.getPlayer().getInventory(MapleInventoryType.getByType((byte)i)).getNextFreeSlot() <= -1) {
               give = false;
               break;
            }
         }

         if (give) {
            List<MapleCabinet> list1 = c.getCabiNet();
            List<MapleCabinet> look = new ArrayList();
            Collections.reverse(list1);
            if (((MapleCabinet)list1.get(get - 1)).getPlayerid() > 0) {
               if (((MapleCabinet)list1.get(get - 1)).getPlayerid() != c.getPlayer().getId()) {
                  MapleCharacter var10000 = c.getPlayer();
                  Object var10002 = list1.get(get - 1);
                  var10000.dropMessage(1, "해당 아이템은 <" + ((MapleCabinet)var10002).getName() + "> 캐릭터로 수령이 가능 합니다.");
                  c.send(CWvsContext.enableActions(c.getPlayer()));
                  return;
               }

               if (((MapleCabinet)list1.get(get - 1)).getItemid() / 1000000 != 1) {
                  c.getPlayer().gainItem(((MapleCabinet)list1.get(get - 1)).getItemid(), (short)((MapleCabinet)list1.get(get - 1)).getCount());
               } else {
                  Calendar ocal = Calendar.getInstance();
                  int day2 = ocal.get(7);
                  int check = day2 == 1 ? 1 : (day2 == 2 ? 7 : (day2 == 3 ? 6 : (day2 == 4 ? 5 : (day2 == 5 ? 4 : (day2 == 6 ? 3 : (day2 == 7 ? 2 : 0))))));
                  Equip item = (Equip)MapleItemInformationProvider.getInstance().getEquipById(((MapleCabinet)list1.get(get - 1)).getItemid());
                  Calendar targetCal = new GregorianCalendar(ocal.get(1), ocal.get(2), ocal.get(5));
                  item.setExpiration(targetCal.getTimeInMillis() + (long)(86400000 * check));
                  if (item.getItemId() == 1672083) {
                     item.setState((byte)20);
                     item.setLines((byte)3);
                     item.setPotential1(40601);
                     item.setPotential2(30291);
                     item.setPotential3(42061);
                     item.setPotential4(42060);
                     item.setPotential5(42060);
                     item.setEnhance((byte)15);
                  } else if (item.getItemId() == 1672085 || item.getItemId() == 1672086) {
                     item.setState((byte)20);
                     item.setLines((byte)2);
                     item.setPotential1(40601);
                     item.setPotential2(30291);
                     item.setEnhance((byte)15);
                  }

                  MapleInventoryManipulator.addbyItem(c, item);
               }
            } else {
               c.getPlayer().gainItem(((MapleCabinet)list1.get(get - 1)).getItemid(), (short)((MapleCabinet)list1.get(get - 1)).getCount());
            }

            c.send(CWvsContext.InfoPacket.getShowItemGain(((MapleCabinet)list1.get(get - 1)).getItemid(), (short)((MapleCabinet)list1.get(get - 1)).getCount(), true));
            var15 = c.getCabiNet().iterator();

            while(var15.hasNext()) {
               add = (MapleCabinet)var15.next();
               if (add.getItemid() == ((MapleCabinet)list1.get(get - 1)).getItemid() && add.getDelete() == 0) {
                  add.setDelete(1);
                  break;
               }
            }

            Collections.reverse(list1);
            c.saveCabiNet(list1);
            var15 = c.getCabiNet().iterator();

            while(var15.hasNext()) {
               add = (MapleCabinet)var15.next();
               if (add.getDelete() == 0) {
                  look.add(add);
               }
            }

            Collections.reverse(look);
            c.send(CField.getMapleCabinetList(look, true, get, false));
         } else {
            c.getPlayer().dropMessage(1, "인벤토리 공간을 비우고 다시 시도하여 주세요.");
            c.send(CWvsContext.enableActions(c.getPlayer()));
         }
      }

   }

   public static void ExpPocket(MapleClient c, LittleEndianAccessor slea) {
      c.removeClickedNPC();
      NPCScriptManager.getInstance().dispose(c);
      c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      NPCScriptManager.getInstance().start(c, 2007, "ExpPocket");
   }

   public static void ChatEmoticon(LittleEndianAccessor slea, MapleClient c) {
      byte type = slea.readByte();
      int emoticon;
      Iterator var11;
      MapleChatEmoticon a;
      switch(type) {
      case 1:
      case 9:
         short slot1 = slea.readShort();
         short slot2 = slea.readShort();
         c.getSession().writeAndFlush(CField.getChatEmoticon(type, slot1, slot2, 0, ""));
         break;
      case 2:
         short s1 = slea.readShort();
         c.getPlayer().getEmoticonTabs().remove(s1 - 1);
         c.getSession().writeAndFlush(CField.getChatEmoticon(type, s1, (short)0, 0, ""));
      case 3:
      case 4:
      case 7:
      default:
         break;
      case 5:
         emoticon = slea.readInt();
         short s2 = c.getPlayer().getEmoticonFreeSlot();
         c.getPlayer().getEmoticonBookMarks().add(new Pair(emoticon, s2));
         var11 = c.getPlayer().getEmoticonTabs().iterator();

         while(var11.hasNext()) {
            a = (MapleChatEmoticon)var11.next();
            if (emoticon / 10000 == a.getEmoticonid()) {
               a.getBookmarks().add(new Pair(emoticon, s2));
               break;
            }
         }

         c.getSession().writeAndFlush(CField.getChatEmoticon(type, s2, (short)0, emoticon, ""));
         break;
      case 6:
         emoticon = slea.readInt();
         var11 = c.getPlayer().getEmoticonBookMarks().iterator();

         while(var11.hasNext()) {
            Pair<Integer, Short> a = (Pair)var11.next();
            if ((Integer)a.left == emoticon) {
               c.getPlayer().getEmoticonBookMarks().remove(a);
               break;
            }
         }

         var11 = c.getPlayer().getEmoticonTabs().iterator();

         label39:
         while(var11.hasNext()) {
            a = (MapleChatEmoticon)var11.next();
            Iterator var13 = a.getBookmarks().iterator();

            while(var13.hasNext()) {
               Pair<Integer, Short> b = (Pair)var13.next();
               if ((Integer)b.left == emoticon) {
                  a.getBookmarks().remove(b);
                  break label39;
               }
            }
         }

         c.getSession().writeAndFlush(CField.getChatEmoticon(type, (short)0, (short)0, emoticon, ""));
         break;
      case 8:
         emoticon = slea.readInt();
         String str = slea.readMapleAsciiString();
         MapleSavedEmoticon em = new MapleSavedEmoticon(c.getPlayer().getId(), emoticon, str);
         c.getPlayer().getSavedEmoticon().add(em);
         c.getSession().writeAndFlush(CField.getChatEmoticon(type, (short)c.getPlayer().getSavedEmoticon().size(), (short)0, emoticon, str));
         break;
      case 10:
         short slot = slea.readShort();
         c.getPlayer().getSavedEmoticon().remove(slot - 1);
         c.getSession().writeAndFlush(CField.getChatEmoticon(type, slot, (short)0, 0, ""));
      }

      c.getPlayer().getEmoticons().clear();
      ChatEmoticon.LoadChatEmoticons(c.getPlayer(), c.getPlayer().getEmoticonTabs());
   }

   public static void applySpecialCoreSkills(MapleCharacter chr) {
      long time = System.currentTimeMillis();
      boolean eff = false;
      Pair<Integer, Integer> coredata = chr.getEquippedSpecialCore();
      if (coredata != null) {
         int coreId = (Integer)coredata.left;
         int skillId = (Integer)coredata.right;
         chr.dropMessageGM(5, coreId + "?" + skillId);
         SecondaryStatEffect effect;
         switch(coreId) {
         case 30000008:
         case 30000020:
            if (!chr.skillisCooling(skillId) && Randomizer.isSuccess(1, 1000)) {
               effect = SkillFactory.getSkill(skillId).getEffect(chr.getSkillLevel(chr.getSkillLevel(skillId)));
               effect.applyTo(chr);
               chr.addCooldown(skillId, time, 30000L);
               eff = true;
            }
            break;
         case 30000009:
         case 30000012:
         case 30000016:
         case 30000018:
         case 30000023:
            if (!chr.skillisCooling(skillId)) {
               effect = SkillFactory.getSkill(skillId).getEffect(chr.getSkillLevel(chr.getSkillLevel(skillId)));
               effect.applyTo(chr);
               chr.addCooldown(skillId, time, 120000L);
               eff = true;
            }
         case 30000010:
         case 30000011:
         case 30000013:
         case 30000014:
         case 30000015:
         case 30000017:
         case 30000019:
         case 30000021:
         case 30000022:
         }

         if (eff) {
            chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, "Effect/CharacterEff.img/VMatrixSP"));
         }

      }
   }

   public static void HitErdaSpectrum(MapleClient c, LittleEndianAccessor slea) {
      MapleCharacter chr = c.getPlayer();
      chr.addSkillCustomInfo(450001400, 10L);
      c.send(SLFCGPacket.ErdaSpectrumGauge((int)c.getPlayer().getSkillCustomValue0(450001400), (int)c.getPlayer().getSkillCustomValue0(8641018), 0));
      chr.getClient().send(SLFCGPacket.EventSkillOnEffect(chr.getPosition().x, chr.getPosition().y, 2, 10));
   }

   public static void ActErdaSpectrum(MapleClient c, LittleEndianAccessor slea) {
      c.getPlayer().getMap().setCustomInfo(450001400, 1, 0);
      c.getPlayer().removeSkillCustomInfo(450001401);
      c.getPlayer().getMap().killAllMonsters(true);
      MapleMonster monster = MapleLifeFactory.getMonster(8641018);
      c.getPlayer().getMap().spawnMonsterOnGroundBelow(monster, new Point(483, 47));
      monster.setController(c.getPlayer());
      monster.setSchedule(Timer.MobTimer.getInstance().register(() -> {
         c.getPlayer().addSkillCustomInfo(450001400, -1L);
         c.getPlayer().setSkillCustomInfo(450001402, (long)Randomizer.rand(0, 2), 0L);
         if (monster.getCustomValue(450001401) == null) {
            monster.setCustomInfo(450001401, 0, Randomizer.rand(7000, 10000));
            int x = Randomizer.rand(-100, 1000);
            c.send(SLFCGPacket.ErdaSpectrumArea(31, 23804, 0, 3000, -120, -154, 120, 5, x, 47));
         }

         c.send(SLFCGPacket.ErdaSpectrumGauge((int)c.getPlayer().getSkillCustomValue0(450001400), (int)c.getPlayer().getSkillCustomValue0(8641018), (int)c.getPlayer().getSkillCustomValue0(450001402)));
         if (c.getPlayer().getSkillCustomValue0(450001400) <= 0L) {
            c.getPlayer().removeSkillCustomInfo(450001402);
            c.getPlayer().getMap().killAllMonsters(true);
            c.getPlayer().getMap().setCustomInfo(450001400, 0, 0);
            c.send(SLFCGPacket.ErdaSpectrumType(2));
            c.getPlayer().setSkillCustomInfo(450001401, 0L, (long)Randomizer.rand(5000, 7000));
         }

      }, 1000L));
      c.send(SLFCGPacket.ErdaSpectrumType(3));
      c.send(CField.UIPacket.detailShowInfo("에르다 응집기가 활성화 됩니다. 응집기를 공격하면 응집기 색깔에 따른 에르다가 추출됩니다.", 3, 20, 0));
      c.send(CField.getGameMessage(11, "에르다 응집기가 활성화 됩니다. 응집기를 공격하면 응집기 색깔에 따른 에르다가 추출됩니다."));
   }

   public static void BallErdaSpectrum(MapleClient c, LittleEndianAccessor slea) {
      int type = slea.readInt();
      int count = slea.readInt();
      MapleMonster mob = c.getPlayer().getMap().getMonsterByOid(slea.readInt());
      int add = 0;
      if (type == 0) {
         if (mob.getId() == 8641019 || mob.getId() == 8641021) {
            add = mob.getId() == 8641019 ? 1 : 2;
         }
      } else if (mob.getId() == 8641020 || mob.getId() == 8641021) {
         add = mob.getId() == 8641020 ? 1 : 2;
      }

      c.getPlayer().addSkillCustomInfo(8641018, (long)add);
      c.send(SLFCGPacket.ErdaSpectrumGauge((int)c.getPlayer().getSkillCustomValue0(450001400), (int)c.getPlayer().getSkillCustomValue0(8641018), (int)c.getPlayer().getSkillCustomValue0(450001402)));
      c.getPlayer().getMap().killMonsterType(mob, 0);
      if (c.getPlayer().getSkillCustomValue0(8641018) >= 10L) {
         c.getPlayer().removeSkillCustomInfo(8641018);
         c.getPlayer().removeSkillCustomInfo(450001401);
         int warp = Randomizer.isSuccess(100) ? 450001450 : 450001500;
         c.getPlayer().warp(warp);
         if (warp == 450001450) {
            c.send(SLFCGPacket.ErdaSpectrumType(2));
            c.send(SLFCGPacket.ErdaSpectrumGauge((int)c.getPlayer().getSkillCustomValue0(450001400), 0, 50));
            c.send(SLFCGPacket.ErdaSpectrumSetting(120000, 0));
            c.send(CField.UIPacket.detailShowInfo("에르다에 의해 이끌려온 모양이다. 주변은 푸른 빛이 일렁이더니 이내 사라졌다.", 3, 20, 6));
            c.send(CField.startMapEffect("  에르다 응집기가 과부하가 되기 전에 주변에 몬스터가 증가하는 걸 막아주세요! ", 5120025, true));
            Timer.EtcTimer.getInstance().schedule(() -> {
               c.send(CField.removeMapEffect());
            }, 5000L);
         }
      }

   }

   public static void AfterCancel2(MapleClient c, LittleEndianAccessor slea) {
      int skillid = slea.readInt();
      int type = slea.readByte();
      if (skillid == 400011091 && c.getPlayer().getBuffedValue(skillid)) {
         c.getPlayer().cancelEffect(c.getPlayer().getBuffedEffect(skillid));
         c.getPlayer().cancelEffect(c.getPlayer().getBuffedEffect(37120012));
         c.getPlayer().combinationBuff = 10;
         SkillFactory.getSkill(37120012).getEffect(c.getPlayer().getSkillLevel(37120012)).applyTo(c.getPlayer(), false);
      }

   }

   public static void SpPortalUse(MapleClient c, LittleEndianAccessor slea) {
      int id = slea.readInt();
      String path = slea.readMapleAsciiString();
      c.send(CField.SpPortal(id, path));
   }

   public static void JobChange(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      if (c.getPlayer().getJob() / 100 == 4) {
         Equip test2 = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
         if (test2 != null) {
            chr.dropMessage(1, "방패,보조무기,블레이드 는 해제해주셔야합니다.");
            return;
         }
      }

      int BeforeJob = chr.getJob();
      int AfterJob = slea.readInt();
      int unk = slea.readByte();
      switch(BeforeJob) {
      case 112:
      case 122:
      case 132:
      case 212:
      case 222:
      case 232:
      case 312:
      case 322:
      case 333:
      case 412:
      case 422:
      case 434:
      case 512:
      case 522:
      case 532:
         if (BeforeJob / 100 == AfterJob / 100 && BeforeJob / 1000 == 0 && AfterJob / 1000 == 0) {
            long needmeso = 0L;
            if (c.getPlayer().getLevel() <= 105) {
               needmeso = 10000000L;
            } else {
               needmeso = (long)(10000000 + (c.getPlayer().getLevel() - 105) * (c.getPlayer().getLevel() - 105) * '썐');
            }

            if (needmeso > c.getPlayer().getMeso()) {
               chr.dropMessage(1, "자유전직을 하기 위한 메소가 부족합니다.");
               return;
            } else {
               Iterator var8 = c.getPlayer().getCore().iterator();

               while(var8.hasNext()) {
                  Core corez = (Core)var8.next();
                  corez.setState(1);
                  corez.setPosition(-1);
               }

               MatrixHandler.calcSkillLevel(c.getPlayer(), -1);
               chr.getClient().send(CWvsContext.UpdateCore(chr, 1));
               chr.getClient().send(CField.UIPacket.closeUI(3));
               chr.dispel();
               var8 = chr.getEffects().iterator();

               while(var8.hasNext()) {
                  Pair<SecondaryStat, SecondaryStatValueHolder> data = (Pair)var8.next();
                  SecondaryStatValueHolder mbsvh = (SecondaryStatValueHolder)data.right;
                  if (SkillFactory.getSkill(mbsvh.effect.getSourceId()) != null && mbsvh.effect.getSourceId() != 80002282 && mbsvh.effect.getSourceId() != 2321055) {
                     chr.cancelEffect(mbsvh.effect, Arrays.asList((SecondaryStat)data.left));
                  }
               }

               chr.gainMeso(-needmeso, false, true);
               var8 = chr.getSkills().keySet().iterator();

               while(var8.hasNext()) {
                  Skill sk = (Skill)var8.next();
                  if (PacketHelper.jobskill(c.getPlayer(), sk.getId())) {
                     chr.changeSkillLevel(sk.getId(), (byte)0, (byte)0);
                  }
               }

               c.getPlayer().AutoTeachSkillZero();
               chr.changeJob(AfterJob);
               c.getPlayer().AutoTeachSkill();
               c.removeClickedNPC();
               NPCScriptManager.getInstance().dispose(c);
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               return;
            }
         } else {
            chr.dropMessage(1, "직업 체인지 오류1");
            return;
         }
      default:
      }
   }

   public static void LaraPoint(MapleClient c, LittleEndianAccessor slea) {
      int type = slea.readByte();
      int type2 = slea.readInt();
      int pointT = slea.readByte();
      Point pos = slea.readIntPos();
      List<SecondAtom> atoms = new ArrayList();
      atoms.add(new SecondAtom(20, c.getPlayer().getId(), 0, 0, 162101000, 0, 0, 0, pos, Arrays.asList(Integer.valueOf(pointT), type2)));
      c.getPlayer().spawnSecondAtom(atoms);
   }

   public static void UseSecondAtom(MapleClient c, LittleEndianAccessor slea) {
      int type = slea.readInt();
      int objid = slea.readInt();
      int unk = slea.readInt();
      int unk1 = slea.readInt();
      int unk2 = slea.readInt();
      int unk3 = slea.readInt();
      int unk4 = slea.readInt();
      SecondAtom sa = c.getPlayer().getSecondAtom(objid);
      if (sa != null && unk4 == 1) {
         c.getSession().writeAndFlush(CField.skillCooldown(162121001, SkillFactory.getSkill(162121001).getEffect(c.getPlayer().getSkillLevel(162121001)).getU2() * 1000));
         c.getPlayer().addCooldown(162121001, System.currentTimeMillis(), (long)(SkillFactory.getSkill(162121001).getEffect(c.getPlayer().getSkillLevel(162121001)).getU2() * 1000));
      }

   }

   public static void Lotus(LittleEndianAccessor slea, MapleClient c) {
   }

   public static void Lotus2(LittleEndianAccessor slea, MapleClient c) {
   }

   public static void MemoryChoice(LittleEndianAccessor slea, MapleClient c) {
      int skillid = slea.readInt();
      c.getPlayer().getClient().getSession().writeAndFlush(SkillPacket.메모리초이스(skillid));
      c.getPlayer().unstableMemorize = skillid;
      c.getSession().writeAndFlush(CField.skillCooldown(400001063, 10000));
      c.getPlayer().addCooldown(400001063, System.currentTimeMillis(), 10000L);
   }

   public static void 믹스헤어(LittleEndianAccessor slea, MapleClient c) {
      slea.skip(2);
      slea.skip(4);
      slea.skip(2);
      int ordinaryColor = slea.readInt();
      int nMixBaseHairColor = true;
      int nMixAddHairColor = false;
      int nMixHairBaseProb = false;
      int nMixBaseHairColor;
      int nMixAddHairColor;
      int nMixHairBaseProb;
      if (ordinaryColor < 10000) {
         nMixBaseHairColor = 0;
         c.getPlayer().dropMessageGM(6, "ordinaryColorr 1 : " + ordinaryColor);
         nMixAddHairColor = ordinaryColor / 1000;
         nMixHairBaseProb = ordinaryColor % 100;
      } else {
         nMixBaseHairColor = ordinaryColor / 10000;
         c.getPlayer().dropMessageGM(6, "ordinaryColorr 2 : " + ordinaryColor);
         nMixAddHairColor = ordinaryColor / 1000 - nMixBaseHairColor * 10;
         nMixHairBaseProb = ordinaryColor % 100;
      }

      c.getPlayer().dropMessageGM(6, "nMixBaseHairColor : " + nMixBaseHairColor);
      c.getPlayer().dropMessageGM(6, "nMixAddHairColor : " + nMixAddHairColor);
      c.getPlayer().dropMessageGM(6, "nMixHairBaseProb : " + nMixHairBaseProb);
      if (GameConstants.isZero(c.getPlayer().getJob())) {
         if (c.getPlayer().getGender() == 1) {
            c.getPlayer().setSecondBaseColor(nMixBaseHairColor);
            c.getPlayer().setSecondAddColor(nMixAddHairColor);
            c.getPlayer().setSecondBaseProb(nMixHairBaseProb);
            c.getPlayer().updateZeroStats();
         } else {
            c.getPlayer().setBaseColor(nMixBaseHairColor);
            c.getPlayer().setAddColor(nMixAddHairColor);
            c.getPlayer().setBaseProb(nMixHairBaseProb);
         }
      } else if (GameConstants.isAngelicBuster(c.getPlayer().getJob())) {
         if (c.getPlayer().getDressup()) {
            c.getPlayer().setSecondBaseColor(nMixBaseHairColor);
            c.getPlayer().setSecondAddColor(nMixAddHairColor);
            c.getPlayer().setSecondBaseProb(nMixHairBaseProb);
            c.getPlayer().updateAngelicStats();
         } else {
            c.getPlayer().setBaseColor(nMixBaseHairColor);
            c.getPlayer().setAddColor(nMixAddHairColor);
            c.getPlayer().setBaseProb(nMixHairBaseProb);
         }
      } else {
         c.getPlayer().setBaseColor(nMixBaseHairColor);
         c.getPlayer().setAddColor(nMixAddHairColor);
         c.getPlayer().setBaseProb(nMixHairBaseProb);
      }

      c.getPlayer().equipChanged();
      c.removeClickedNPC();
      NPCScriptManager.getInstance().dispose(c);
      c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      c.getPlayer().fakeRelog();
   }
}
