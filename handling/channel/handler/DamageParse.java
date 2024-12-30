package handling.channel.handler;

import client.MapleCharacter;
import client.MapleSkillManager;
import client.PlayerStats;
import client.RandomSkillEntry;
import client.RangeAttack;
import client.SecondAtom2;
import client.SecondaryStat;
import client.SecondaryStatValueHolder;
import client.Skill;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import handling.world.party.MaplePartyCharacter;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import server.Randomizer;
import server.SecondaryStatEffect;
import server.Timer;
import server.field.skill.MapleMagicWreck;
import server.field.skill.MapleSecondAtom;
import server.field.skill.SecondAtom;
import server.life.Element;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterStats;
import server.maps.ForceAtom;
import server.maps.MapleAtom;
import server.maps.MapleFoothold;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleMist;
import server.maps.MapleSummon;
import server.maps.SummonMovementType;
import tools.AttackPair;
import tools.Pair;
import tools.Triple;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.MobPacket;
import tools.packet.SkillPacket;

public class DamageParse {
   public static void applyAttack(final AttackInfo attack, Skill theSkill, final MapleCharacter player, double maxDamagePerMonster, SecondaryStatEffect effect, AttackType attack_type, boolean BuffAttack, boolean energy) {
      if (attack.summonattack == 0) {
      }

      player.checkSpecialCoreSkills("prob", 0, effect);
      if (attack.skill != 0) {
         player.checkSpecialCoreSkills("cooltime", 0, effect);
         if (effect == null) {
            player.getClient().getSession().writeAndFlush(CWvsContext.enableActions(player));
            return;
         }

         if (GameConstants.isMulungSkill(attack.skill)) {
            if (player.getMapId() / 10000 != 92502) {
               return;
            }

            if (player.getMulungEnergy() < 10000) {
               return;
            }
         } else {
            label5843: {
               if (GameConstants.isPyramidSkill(attack.skill)) {
                  if (player.getMapId() / 1000000 == 926) {
                     break label5843;
                  }
               } else if (!GameConstants.isInflationSkill(attack.skill) || player.getBuffedValue(SecondaryStat.Inflation) != null) {
                  break label5843;
               }

               return;
            }
         }
      }

      long totDamage = 0L;
      MapleMap map = player.getMap();
      long totDamageToOneMonster = 0L;
      long hpMob = 0L;
      PlayerStats stats = player.getStat();
      int multikill = 0;
      boolean afterimageshockattack = false;
      MapleMonster monster = null;
      ArrayList<Triple<Integer, Integer, Integer>> finalMobList = new ArrayList();
      Iterator var25 = attack.allDamage.iterator();

      while(true) {
         SecondaryStatEffect mark;
         int i2;
         int skillid;
         SecondaryStatEffect ceffect;
         int y;
         boolean debinrear;
         SecondaryStatEffect shadowBite;
         MapleAtom atom;
         SecondaryStatEffect eff;
         ForceAtom forceAtom;
         boolean active;
         MapleAtom atom;
         int fora;
         ArrayList mobList;
         ArrayList statusz;
         int var10000;
         do {
            int i;
            SecondaryStatEffect concentration;
            while(true) {
               Object oned;
               MapleMonsterStats monsterstats;
               long fixeddmg;
               int i;
               int skill;
               ArrayList monsters;
               do {
                  do {
                     do {
                        if (!var25.hasNext()) {
                           int trychance;
                           int sungi_skill;
                           int maxcount;
                           if (attack.skill == 5121013 || attack.skill == 5221013 || attack.skill == 400051040) {
                              if (player.getSkillLevel(5121013) > 0 && attack.skill == 5121013 && player.getSkillLevel(400051040) > 0 && player.getCooldownLimit(400051040) <= 8000L) {
                                 player.getClient().getSession().writeAndFlush(CField.skillCooldown(400051040, 8000));
                                 player.addCooldown(400051040, System.currentTimeMillis(), 8000L);
                              } else if (player.getSkillLevel(5221013) > 0 && attack.skill == 5221013) {
                                 if (player.getSkillLevel(400051040) > 0 && player.getCooldownLimit(400051040) <= 8000L) {
                                    player.getClient().getSession().writeAndFlush(CField.skillCooldown(400051040, 8000));
                                    player.addCooldown(400051040, System.currentTimeMillis(), 8000L);
                                 }

                                 int[] array2 = new int[]{5210015, 5210016, 5210017, 5210018, 5220014, 5211007, 5221022, 5220023, 5220024, 5220025};
                                 int[] var84 = array2;
                                 sungi_skill = array2.length;

                                 for(maxcount = 0; maxcount < sungi_skill; ++maxcount) {
                                    trychance = var84[maxcount];
                                    if (player.skillisCooling(trychance)) {
                                       player.changeCooldown(trychance, (int)(-(player.getCooldownLimit(trychance) / 2L)));
                                    }
                                 }
                              } else if (player.getSkillLevel(400051040) > 0 && attack.skill == 400051040 && player.getSkillLevel(5121013) > 0 && player.getCooldownLimit(5121013) <= 8000L) {
                                 player.getClient().getSession().writeAndFlush(CField.skillCooldown(5121013, 8000));
                                 player.addCooldown(5121013, System.currentTimeMillis(), 8000L);
                              } else if (player.getSkillLevel(400051040) > 0 && attack.skill == 400051040 && player.getSkillLevel(5221013) > 0 && player.getCooldownLimit(5221013) <= 8000L) {
                                 player.getClient().getSession().writeAndFlush(CField.skillCooldown(5221013, 8000));
                                 player.addCooldown(5221013, System.currentTimeMillis(), 8000L);
                              }
                           }

                           if (!finalMobList.isEmpty()) {
                              player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(33000036, finalMobList, false, 0));
                           }

                           SecondaryStatEffect a;
                           Iterator var91;
                           int sungi_skill;
                           int i;
                           ArrayList monsters;
                           if (totDamage > 0L) {
                              if (player.getMapId() == 993000500) {
                                 player.setFWolfDamage(player.getFWolfDamage() + totDamage);
                                 player.setFWolfAttackCount(player.getFWolfAttackCount() + 1);
                                 player.dropMessageGM(5, "total damage : " + player.getFWolfDamage());
                              }

                              if (!GameConstants.isKaiser(player.getJob()) && monster != null) {
                                 WFinalAttackRequest(player, attack.skill, monster);
                              }

                              if (attack.skill == 5321001) {
                                 if (player.skillisCooling(5311004)) {
                                    player.changeCooldown(5311004, (int)(-(player.getCooldownLimit(5311004) / 2L)));
                                 }

                                 if (player.skillisCooling(5311005)) {
                                    player.changeCooldown(5311005, (int)(-(player.getCooldownLimit(5311005) / 2L)));
                                 }

                                 if (player.skillisCooling(5320007)) {
                                    player.changeCooldown(5320007, (int)(-(player.getCooldownLimit(5320007) / 2L)));
                                 }
                              }

                              if (GameConstants.isArcher(player.getJob()) && SkillFactory.getSkill(400031021).getSkillList().contains(attack.skill) && player.getSkillLevel(400031020) > 0 && player.getCooldownLimit(400031020) > 0L) {
                                 player.setVerseOfRelicsCount(player.getVerseOfRelicsCount() + 1);
                                 a = SkillFactory.getSkill(400031021).getEffect(player.getSkillLevel(400031020));
                                 if (System.currentTimeMillis() - player.lastVerseOfRelicsTime >= (long)(a.getSubTime() / 100) && player.getVerseOfRelicsCount() >= 10) {
                                    player.lastVerseOfRelicsTime = System.currentTimeMillis();
                                    player.setVerseOfRelicsCount(0);
                                    a.applyTo(player, false, 1000);
                                 }
                              }

                              if (attack.skill == 400011056 && player.getBuffedValue(SecondaryStat.Ellision) != null && player.getSkillCustomValue(400011065) == null) {
                                 MapleSummon ellision = player.getSummon(400011065);
                                 if (ellision == null) {
                                    boolean rltype = (attack.facingleft >>> 4 & 15) == 8;
                                    MapleSummon summon4 = new MapleSummon(player, 400011065, attack.position, SummonMovementType.STATIONARY, (byte)(rltype ? 1 : 0), effect.getDuration());
                                    player.getMap().spawnSummon(summon4, effect.getDuration());
                                    player.addSummon(summon4);
                                 } else {
                                    ellision.setEnergy(ellision.getEnergy() + 1);
                                    if (ellision.getEnergy() % 5 == 0) {
                                       ellision.setEnergy(0);
                                       player.getMap().broadcastMessage(CField.SummonPacket.transformSummon(ellision, 1));
                                    }
                                 }
                              }

                              if (GameConstants.isLuminous(player.getJob())) {
                                 if ((player.getBuffedValue(20040216) || player.getBuffedValue(20040219) || player.getBuffedValue(20040220)) && (GameConstants.isLightSkills(attack.skill) || (player.getBuffedValue(20040219) || player.getBuffedValue(20040220)) && (attack.skill == 27121303 || attack.skill == 27111303))) {
                                    player.addHP(player.getStat().getMaxHp() / 100L);
                                 }

                                 if (!player.getBuffedValue(20040216) && !player.getBuffedValue(20040217) && !player.getBuffedValue(20040219) && !player.getBuffedValue(20040220)) {
                                    if (GameConstants.isLightSkills(attack.skill)) {
                                       player.setLuminusMorphUse(1);
                                       SkillFactory.getSkill(20040217).getEffect(1).applyTo(player, false);
                                       player.setLuminusMorph(false);
                                    } else if (GameConstants.isDarkSkills(attack.skill)) {
                                       player.setLuminusMorphUse(9999);
                                       SkillFactory.getSkill(20040216).getEffect(1).applyTo(player, false);
                                       player.setLuminusMorph(true);
                                    }

                                    player.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.LuminusMorph(player.getLuminusMorphUse(), player.getLuminusMorph()));
                                 } else if (!player.getBuffedValue(20040219) && !player.getBuffedValue(20040220)) {
                                    if (player.getLuminusMorph()) {
                                       if (GameConstants.isLightSkills(attack.skill)) {
                                          if (player.getLuminusMorphUse() - GameConstants.isLightSkillsGaugeCheck(attack.skill) <= 0) {
                                             if (player.getSkillLevel(20040219) > 0) {
                                                player.cancelEffectFromBuffStat(SecondaryStat.Larkness);
                                                SkillFactory.getSkill(20040219).getEffect(1).applyTo(player, false);
                                             } else {
                                                player.cancelEffectFromBuffStat(SecondaryStat.Larkness);
                                                player.setLuminusMorph(false);
                                                SkillFactory.getSkill(20040217).getEffect(1).applyTo(player, false);
                                             }
                                          } else {
                                             player.setLuminusMorphUse(player.getLuminusMorphUse() - GameConstants.isLightSkillsGaugeCheck(attack.skill));
                                          }

                                          if (!player.getBuffedValue(20040219) && !player.getBuffedValue(20040220) && player.getLuminusMorph()) {
                                             player.cancelEffectFromBuffStat(SecondaryStat.Larkness);
                                             SkillFactory.getSkill(20040216).getEffect(1).applyTo(player, false);
                                          }
                                       }
                                    } else if (GameConstants.isDarkSkills(attack.skill)) {
                                       if (player.getLuminusMorphUse() + GameConstants.isDarkSkillsGaugeCheck(player, attack.skill) >= 10000) {
                                          if (player.getSkillLevel(20040219) > 0) {
                                             player.cancelEffectFromBuffStat(SecondaryStat.Larkness);
                                             SkillFactory.getSkill(20040220).getEffect(1).applyTo(player, false);
                                          } else {
                                             player.cancelEffectFromBuffStat(SecondaryStat.Larkness);
                                             player.setLuminusMorph(true);
                                             SkillFactory.getSkill(20040216).getEffect(1).applyTo(player, false);
                                          }
                                       } else {
                                          player.setLuminusMorphUse(player.getLuminusMorphUse() + GameConstants.isDarkSkillsGaugeCheck(player, attack.skill));
                                       }

                                       if (!player.getBuffedValue(20040219) && !player.getBuffedValue(20040220) && !player.getLuminusMorph()) {
                                          player.cancelEffectFromBuffStat(SecondaryStat.Larkness);
                                          SkillFactory.getSkill(20040217).getEffect(1).applyTo(player, false);
                                       }
                                    }

                                    player.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.LuminusMorph(player.getLuminusMorphUse(), player.getLuminusMorph()));
                                 }
                              }

                              if (attack.skill == 27121303 && player.getSkillLevel(400021071) > 0) {
                                 boolean give = false;
                                 if (player.getPerfusion() < SkillFactory.getSkill(400021071).getEffect(player.getSkillLevel(400021071)).getX() - 1) {
                                    give = true;
                                 } else if (player.getPerfusion() >= SkillFactory.getSkill(400021071).getEffect(player.getSkillLevel(400021071)).getX() - 1 && player.skillisCooling(400021071)) {
                                    give = true;
                                 }

                                 if (give) {
                                    SkillFactory.getSkill(400021071).getEffect(player.getSkillLevel(400021071)).applyTo(player, false);
                                 }
                              }

                              MaplePartyCharacter pc;
                              MapleCharacter chr;
                              if (player.getBuffedValue(32101009) && player.getSkillCustomValue(32111119) == null && player.getId() == player.getBuffedOwner(32101009)) {
                                 player.addHP(totDamage / 100L * (long)player.getBuffedEffect(32101009).getX());
                                 player.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(player, 0, 32101009, 10, 0, 0, (byte)(player.isFacingLeft() ? 1 : 0), true, player.getTruePosition(), (String)null, (Item)null));
                                 player.getMap().broadcastMessage(player, CField.EffectPacket.showEffect(player, 0, 32101009, 10, 0, 0, (byte)(player.isFacingLeft() ? 1 : 0), false, player.getTruePosition(), (String)null, (Item)null), false);
                                 player.setSkillCustomInfo(32111119, 0L, 5000L);
                                 if (player.getParty() != null) {
                                    var91 = player.getParty().getMembers().iterator();

                                    while(var91.hasNext()) {
                                       pc = (MaplePartyCharacter)var91.next();
                                       if (pc.getId() != player.getId() && pc.isOnline() && (chr = player.getClient().getChannelServer().getPlayerStorage().getCharacterById(pc.getId())) != null && chr.getBuffedValue(32101009) && chr.getId() != player.getId()) {
                                          chr.addHP(totDamage / 100L * (long)player.getBuffedEffect(32101009).getX());
                                          if (chr.getDisease(SecondaryStat.GiveMeHeal) != null) {
                                             chr.cancelDisease(SecondaryStat.GiveMeHeal);
                                          }

                                          chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, 32101009, 10, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), true, chr.getTruePosition(), (String)null, (Item)null));
                                          chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 0, 32101009, 10, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), false, chr.getTruePosition(), (String)null, (Item)null), false);
                                       }
                                    }
                                 }
                              }

                              if (player.getBuffedValue(31121002) && System.currentTimeMillis() - player.lastVamTime >= (long)player.getBuffedEffect(31121002).getY() && player.getBuffedEffect(SecondaryStat.DebuffIncHp) == null) {
                                 player.lastVamTime = System.currentTimeMillis();
                                 player.addHP(Math.min((long)player.getBuffedEffect(31121002).getW(), totDamage * (long)player.getBuffedEffect(31121002).getX() / 100L));
                                 if (player.getParty() != null) {
                                    var91 = player.getParty().getMembers().iterator();

                                    while(var91.hasNext()) {
                                       pc = (MaplePartyCharacter)var91.next();
                                       if (pc.getId() != player.getId() && pc.isOnline() && (chr = player.getClient().getChannelServer().getPlayerStorage().getCharacterById(pc.getId())) != null && chr.isAlive() && chr.getBuffedEffect(SecondaryStat.DebuffIncHp) == null) {
                                          chr.addHP(totDamage * (long)player.getBuffedEffect(31121002).getX() / 100L);
                                       }
                                    }
                                 }
                              }

                              if (attack.skill == 400011131) {
                                 player.getClient().getSession().writeAndFlush(CField.rangeAttack(400011131, Arrays.asList(new RangeAttack(400011132, attack.attackPosition2, 0, 0, 1))));
                              }

                              if (monster != null && player.getBuffedValue(65121011) && attack.skill != 65120011 && attack.skill != 65111007 && attack.skill != 60011216 && attack.skill < 400000000) {
                                 i = player.getBuffedEffect(65121011).getProp();
                                 if (attack.skill == 65121100) {
                                    i += player.getBuffedEffect(65121011).getZ();
                                 }

                                 if (player.getBuffedEffect(SecondaryStat.SoulExalt) != null) {
                                    i += player.getBuffedValue(SecondaryStat.SoulExalt);
                                 }

                                 if (Randomizer.isSuccess(i)) {
                                    for(sungi_skill = 0; sungi_skill < 2; ++sungi_skill) {
                                       MapleAtom atom = new MapleAtom(false, player.getId(), 25, true, 65120011, player.getTruePosition().x, player.getTruePosition().y);
                                       monsters = new ArrayList();
                                       monsters.add(0);
                                       atom.addForceAtom(new ForceAtom(1, Randomizer.rand(15, 16), Randomizer.rand(27, 34), Randomizer.rand(31, 36), 0));
                                       atom.setDwFirstTargetId(monster.getObjectId());
                                       atom.setDwTargets(monsters);
                                       player.getMap().spawnMapleAtom(atom);
                                    }
                                 }
                              }

                              if (Arrays.asList(4121013, 4121017, 4121052, 4001344, 4101008, 4111010, 4111015).contains(attack.skill)) {
                                 if (player.getBuffedEffect(SecondaryStat.ThrowBlasting) != null && attack.skill != 400041061 && attack.skill != 400041079) {
                                    a = player.getBuffedEffect(SecondaryStat.ThrowBlasting);
                                    sungi_skill = Randomizer.rand(a.getS(), a.getW());
                                    player.throwBlasting -= sungi_skill;
                                    player.getClient().getSession().writeAndFlush(CField.rangeAttack(400041061, Arrays.asList(new RangeAttack(400041079, attack.attackPosition2, 0, 0, 3))));
                                    if (player.throwBlasting <= 0) {
                                       player.cancelEffectFromBuffStat(SecondaryStat.ThrowBlasting);
                                    } else {
                                       HashMap<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
                                       statups.put(SecondaryStat.ThrowBlasting, new Pair(player.throwBlasting, (int)player.getBuffLimit(player.getBuffSource(SecondaryStat.ThrowBlasting))));
                                       player.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, player.getBuffedEffect(SecondaryStat.ThrowBlasting), player));
                                    }
                                 } else if (player.getSkillLevel(400041061) > 0 && attack.skill != 400041061 && attack.skill != 400041062) {
                                    a = SkillFactory.getSkill(400041061).getEffect(player.getSkillLevel(400041061));
                                    if (System.currentTimeMillis() - player.lastThrowBlastingTime >= (long)(a.getU() * 1000)) {
                                       player.lastThrowBlastingTime = System.currentTimeMillis();
                                       player.getClient().getSession().writeAndFlush(CField.rangeAttack(400041061, Arrays.asList(new RangeAttack(400041079, attack.attackPosition2, 0, 0, 3))));
                                    }
                                 }
                              }
                           }

                           if (player.getBuffSource(SecondaryStat.DrainHp) == 20031210) {
                              player.addHP(totDamage * (long)player.getBuffedValue(SecondaryStat.DrainHp) / 100L);
                           }

                           if (player.getSkillLevel(1200014) > 0) {
                              player.elementalChargeHandler(attack.skill, 1);
                           }

                           if (attack.skill == 155121306) {
                              if (!player.getBuffedValue(155000007)) {
                                 SkillFactory.getSkill(155000007).getEffect(1).applyTo(player);
                              }

                              SkillFactory.getSkill(155121006).getEffect(attack.skilllevel).applyTo(player, monster.getPosition(), false);
                           }

                           int ck;
                           if ((!GameConstants.사출기(attack.skill) || attack.targets > 0 && player.getMapId() == 921170011 || attack.targets > 0 && player.getMapId() == 921170004) && (attack.targets > 0 && player.getKeyValue(99999, "tripling") > 0L && attack.skill != 1311020 && !GameConstants.isTryFling(attack.skill) && attack.skill != 400031031 && attack.skill != 400031001 && attack.skill != 13111020 && attack.skill != 13121054 && attack.skill != 13101022 && attack.skill != 13110022 && attack.skill != 13120003 && attack.skill != 13111020 && attack.skill != 400001018 || attack.targets > 0 && player.getMapId() == 921170011 || attack.targets > 0 && player.getMapId() == 921170004 || attack.targets > 0 && attack.skill != 1311020 && player.getBuffedEffect(SecondaryStat.TryflingWarm) != null && !GameConstants.isTryFling(attack.skill) && attack.skill != 400031031 && attack.skill != 400031001 && attack.skill != 13111020 && attack.skill != 13121054 && attack.skill != 13101022 && attack.skill != 13110022 && attack.skill != 13120003 && attack.skill != 13111020 && attack.skill != 400001018 && attack.targets > 0)) {
                              ck = 0;
                              if (player.getKeyValue(99999, "tripling") <= 0L && player.getMapId() != 921170004 && player.getMapId() != 921170011 && player.getKeyValue(1234, "ww_whim") <= 0L && player.getSkillLevel(SkillFactory.getSkill(13120003)) <= 0) {
                                 if (player.getSkillLevel(SkillFactory.getSkill(13110022)) > 0) {
                                    ck = 13110022;
                                 } else if (player.getSkillLevel(SkillFactory.getSkill(13101022)) > 0) {
                                    ck = 13100022;
                                 }
                              } else {
                                 ck = 13120003;
                                 if (player.getSkillLevel(SkillFactory.getSkill(13120003)) < 30) {
                                    player.teachSkill(13120003, 30);
                                 }
                              }

                              if (player.isGM()) {
                              }

                              if (ck != 0) {
                                 Skill trskill = SkillFactory.getSkill(ck);
                                 if (Randomizer.rand(1, 100) <= (ck == 13100022 ? 5 : (ck == 13110022 ? 10 : 20))) {
                                    if (ck == 13120003) {
                                       var10000 = 13120010;
                                    } else {
                                       ck = ck == 13110022 ? 13110027 : 13100027;
                                    }

                                    if (player.getSkillLevel(ck) <= 0) {
                                       player.changeSkillLevel(SkillFactory.getSkill(ck), (byte)player.getSkillLevel(trskill), (byte)player.getSkillLevel(trskill));
                                    }
                                 }

                                 List<MapleMapObject> objs = player.getMap().getMapObjectsInRange(player.getTruePosition(), 500000.0D, Arrays.asList(MapleMapObjectType.MONSTER));
                                 SecondaryStatEffect eff = trskill.getEffect(player.getSkillLevel(ck));
                                 maxcount = eff.getX() + (int)(player.getKeyValue(99999, "triplingBonus") > 0L ? player.getKeyValue(99999, "triplingBonus") * 1L : 0L);
                                 if (objs.size() > 0) {
                                    trychance = 100;
                                    if (player.getSkillLevel(13120044) > 0) {
                                       trychance += SkillFactory.getSkill(13120044).getEffect(1).getProp();
                                    }

                                    if (attack.skill == 400031004 || attack.skill == 400031003) {
                                       trychance /= 2;
                                    }

                                    if (player.getMapId() == 925080000) {
                                       maxcount = 1;
                                       if (player.자동사냥 < System.currentTimeMillis()) {
                                          maxcount = 5;
                                          player.자동사냥 = System.currentTimeMillis() + 30000L;
                                       }
                                    }

                                    if (player.getMapId() == 921170004 || player.getMapId() == 921170011) {
                                       trychance = 100;
                                       maxcount = 2;
                                       if (player.자동사냥 + 5000L < System.currentTimeMillis()) {
                                          player.자동사냥 = System.currentTimeMillis();
                                          maxcount = 5;
                                       }
                                    }

                                    if (Randomizer.isSuccess(trychance)) {
                                       MapleAtom atom = new MapleAtom(false, player.getId(), 7, true, ck, player.getTruePosition().x, player.getTruePosition().y);
                                       monsters = new ArrayList();

                                       for(skill = 0; skill < Randomizer.rand(1 + (int)(player.getKeyValue(99999, "triplingBonus") > 0L ? player.getKeyValue(99999, "triplingBonus") * 1L : 0L), maxcount); ++skill) {
                                          debinrear = Randomizer.isSuccess(eff.getSubprop());
                                          monsters.add(((MapleMapObject)objs.get(Randomizer.nextInt(objs.size()))).getObjectId());
                                          atom.addForceAtom(new ForceAtom(debinrear ? 3 : 1, Randomizer.rand(41, 49), Randomizer.rand(4, 8), Randomizer.nextBoolean() ? Randomizer.rand(171, 174) : Randomizer.rand(6, 9), (short)Randomizer.rand(42, 47)));
                                       }

                                       atom.setDwTargets(monsters);
                                       player.getMap().spawnMapleAtom(atom);
                                    }
                                 }
                              }
                           }

                           if (attack.skill == 400031032 || attack.skill == 400051042) {
                              PlayerHandler.Vmatrixstackbuff(player.getClient(), true, (LittleEndianAccessor)null);
                           }

                           MapleMist newmist;
                           if (attack.skill == 400051075) {
                              player.setSkillCustomInfo(400051074, player.getSkillCustomValue0(400051074) - 1L, 0L);
                              if (attack.targets <= 0) {
                                 ck = 0;
                                 var91 = player.getMap().getAllMistsThreadsafe().iterator();

                                 while(var91.hasNext()) {
                                    newmist = (MapleMist)var91.next();
                                    if (newmist.getOwnerId() == player.getId()) {
                                       ++ck;
                                    }
                                 }

                                 if (ck < 2) {
                                    a = SkillFactory.getSkill(400051076).getEffect(player.getSkillLevel(400051074));
                                    Rectangle bounds = a.calculateBoundingBox(new Point(attack.plusPosition2.x, attack.plusPosition2.y), player.isFacingLeft());
                                    MapleMist mist3 = new MapleMist(bounds, player, a, 20000, (byte)0);
                                    mist3.setPosition(new Point(attack.plusPosition2.x, attack.plusPosition2.y));
                                    mist3.setDelay(0);
                                    player.getMap().spawnMist(mist3, false);
                                 }
                              }

                              if (player.getSkillCustomValue0(400051074) <= 0L) {
                                 player.getMap().removeMist(400051076);
                                 player.getMap().removeMist(400051076);
                              }

                              player.getClient().send(CField.fullMaker((int)player.getSkillCustomValue0(400051074), player.getSkillCustomValue0(400051074) <= 0L ? 0 : '\uea60'));
                           }

                           SecondaryStatEffect sungi;
                           if (totDamage > 0L && player.getSkillLevel(3210013) > 0) {
                              sungi = SkillFactory.getSkill(3210013).getEffect(player.getSkillLevel(3210013));
                              player.setBarrier((int)Math.min(player.getStat().getCurrentMaxHp() * (long)sungi.getZ() / 100L, totDamage * (long)sungi.getY() / 100L));
                              sungi.applyTo(player, false);
                           }

                           if (totDamage > 0L && player.getBuffedValue(65101002)) {
                              sungi = SkillFactory.getSkill(65101002).getEffect(player.getSkillLevel(65101002));
                              player.setBarrier((int)Math.min(player.getStat().getCurrentMaxHp(), totDamage * (long)sungi.getY() / 100L));
                              player.setBarrier((int)Math.min(player.getStat().getCurrentMaxHp(), 99999L));
                              sungi.applyTo(player, false, (int)player.getBuffLimit(65101002));
                           }

                           if (GameConstants.isDemonSlash(attack.skill) && player.getSkillLevel(31120045) > 0 && !player.getBuffedValue(31120045)) {
                              SkillFactory.getSkill(31120045).getEffect(1).applyTo(player, false);
                           }

                           if (player.getBuffedValue(400031044) && player.getSkillCustomValue(400031044) == null && monster != null) {
                              player.setGraveTarget(player.getObjectId());

                              for(ck = 0; ck < Randomizer.rand(1, 4); ++ck) {
                                 player.createSecondAtom(SkillFactory.getSkill(400031045).getSecondAtoms(), monster.getPosition());
                              }

                              player.setSkillCustomInfo(400031044, 0L, (long)((int)(SkillFactory.getSkill(400031044).getEffect(player.getSkillLevel(400031044)).getT() * 1000.0D)));
                           }

                           if (GameConstants.isAran(player.getJob()) && attack.skill != 400011122 && player.getBuffedValue(400011121) && System.currentTimeMillis() - player.lastBlizzardTempestTime >= 500L) {
                              player.lastBlizzardTempestTime = System.currentTimeMillis();
                              player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(400011122, new ArrayList(), true, 0));
                              ArrayList<Pair<Integer, Integer>> lists = new ArrayList<Pair<Integer, Integer>>() {
                                 {
                                    Iterator var3 = attack.allDamage.iterator();

                                    while(var3.hasNext()) {
                                       AttackPair pair = (AttackPair)var3.next();
                                       MapleMonster mob = player.getMap().getMonsterByOid(pair.objectId);
                                       if (mob != null) {
                                          if (mob.blizzardTempest < 6) {
                                             ++mob.blizzardTempest;
                                          }

                                          this.add(new Pair(pair.objectId, mob.blizzardTempest));
                                       }
                                    }

                                 }
                              };
                              player.getClient().getSession().writeAndFlush(CWvsContext.blizzardTempest(lists));
                           }

                           if (attack.skill == 4341002) {
                              effect.applyTo(player, false);
                           }

                           if (attack.skill == 4341011 && player.getCooldownLimit(4341002) > 0L) {
                              sungi = SkillFactory.getSkill(4341011).getEffect(attack.skilllevel);
                              player.changeCooldown(4341002, (int)(-player.getCooldownLimit(4341002) * (long)sungi.getX() / 100L));
                           }

                           if (player.getBuffedValue(155101008) && attack.skill != 155121006 && attack.skill != 155121007 && attack.skill != 155100009 && attack.skill != 155001000 && attack.skill != 155121004 && attack.skill != 400051035 && attack.skill != 400051334 && player.getBuffedEffect(SecondaryStat.SpectorTransForm) != null) {
                              a = SkillFactory.getSkill(155101008).getEffect(155101008);
                              sungi_skill = a.getZ();
                              sungi_skill = player.getBuffedValue(400051036) ? player.getBuffedEffect(400051036).getX() : 0;
                              monsters = new ArrayList();
                              MapleAtom atom = new MapleAtom(false, player.getId(), 47, true, 155100009, 0, 0);

                              for(ck = 0; ck < attack.targets && monsters.size() < sungi_skill; ++ck) {
                                 monsters.add(0);
                                 atom.addForceAtom(new ForceAtom(0, 1, Randomizer.rand(5, 10), 270, (short)Randomizer.rand(75, 95)));
                              }

                              for(ck = 0; ck < sungi_skill; ++ck) {
                                 monsters.add(0);
                                 atom.addForceAtom(new ForceAtom(0, 1, Randomizer.rand(5, 10), 270, (short)Randomizer.rand(75, 95)));
                              }

                              atom.setDwTargets(monsters);
                              player.getMap().spawnMapleAtom(atom);
                           }

                           if (player.getBuffedValue(400031030) && System.currentTimeMillis() - player.lastWindWallTime >= (long)(player.getBuffedEffect(400031030).getW2() * 1000)) {
                              player.lastWindWallTime = System.currentTimeMillis();
                              MapleAtom atom = new MapleAtom(false, player.getId(), 51, true, 400031031, player.getTruePosition().x, player.getTruePosition().y);

                              for(i = 0; i < player.getBuffedEffect(400031030).getQ2(); ++i) {
                                 atom.addForceAtom(new ForceAtom(Randomizer.nextBoolean() ? 1 : 3, Randomizer.rand(30, 60), 10, Randomizer.nextBoolean() ? Randomizer.rand(0, 5) : Randomizer.rand(180, 185), 0));
                              }

                              atom.setDwFirstTargetId(0);
                              player.getMap().spawnMapleAtom(atom);
                           }

                           SecondaryStatEffect Exceed;
                           if (player.getSkillLevel(80002762) > 0 && (Exceed = SkillFactory.getSkill(80002762).getEffect(player.getSkillLevel(80002762))).makeChanceResult()) {
                              Exceed.applyTo(player, false);
                           }

                           if (attack.skill == 5221015 || attack.skill == 151121001) {
                              player.cancelEffect(effect);
                              SkillFactory.getSkill(attack.skill).getEffect(attack.skilllevel).applyTo(player, false);
                           }

                           if (attack.skill == 61121104 || attack.skill == 61121124 || attack.skill == 61121221 || attack.skill == 61121223 || attack.skill == 61121225) {
                              SkillFactory.getSkill(61121116).getEffect(attack.skilllevel).applyTo(player, false);
                           }

                           if (player.getBuffedValue(SecondaryStat.Steal) != null && monster != null) {
                              monster.handleSteal(player);
                              if (Randomizer.isSuccess(10) && monster.getCustomValue0(4201017) == 0L) {
                                 monster.setCustomInfo(4201017, 1, 0);
                                 Item toDrop = new Item(2431835, (short)1, (short)1, 0);
                                 player.getMap().spawnItemDrop(monster, player, toDrop, monster.getPosition(), true, true);
                              }
                           }

                           SecondaryStatEffect mischief;
                           if (player.getSkillLevel(4330007) > 0 && !GameConstants.is_forceAtom_attack_skill(attack.skill) && (mischief = SkillFactory.getSkill(4330007).getEffect(player.getSkillLevel(4330007))).makeChanceResult()) {
                              player.addHP(player.getStat().getCurrentMaxHp() / 100L * (long)mischief.getX());
                           }

                           if (totDamage > 0L && player.getSkillLevel(4200013) > 0 && !GameConstants.is_forceAtom_attack_skill(attack.skill)) {
                              sungi = SkillFactory.getSkill(4200013).getEffect(player.getSkillLevel(4200013));
                              if (player.getSkillLevel(4220015) > 0) {
                                 sungi = SkillFactory.getSkill(4220015).getEffect(player.getSkillLevel(4220015));
                              }

                              if (player.criticalGrowing == 100 && player.criticalDamageGrowing >= sungi.getQ()) {
                                 player.criticalGrowing = 0;
                                 player.criticalDamageGrowing = 0;
                                 player.setSkillCustomInfo(4220015, 0L, 4000L);
                              } else if (player.criticalGrowing + player.getStat().critical_rate >= 100) {
                                 player.criticalGrowing = 100;
                              } else {
                                 player.criticalGrowing += sungi.getX();
                                 player.criticalDamageGrowing = Math.min(player.criticalDamageGrowing + sungi.getW(), sungi.getQ());
                                 player.setSkillCustomInfo(4220015, 0L, 4000L);
                              }

                              sungi.applyTo(player, false, 0);
                           }

                           if (totDamage > 0L && player.getSkillLevel(5220055) > 0) {
                              sungi = SkillFactory.getSkill(5220055).getEffect(player.getSkillLevel(5220055));
                              if (!player.getBuffedValue(5220055)) {
                                 if (sungi.makeChanceResult()) {
                                    sungi.applyTo(player, false);
                                 }
                              } else if (player.getBuffedValue(SecondaryStat.QuickDraw) != null && attack.skill != 5220023 && attack.skill != 5220024 && attack.skill != 5220025 && attack.skill != 5220020 && attack.skill != 5221004 && attack.skill != 400051006) {
                                 player.cancelEffectFromBuffStat(SecondaryStat.QuickDraw, 5220055);
                              }
                           }

                           if (player.getBuffedValue(15001022) && attack.skill != 15111022 && attack.skill != 15120003 && attack.skill != 400051016) {
                              mark = player.getBuffedEffect(15001022);
                              sungi_skill = mark.getProp();
                              maxcount = mark.getV();
                              int[] var130 = new int[]{15000023, 15100025, 15110026, 15120008};
                              int var128 = var130.length;

                              for(i = 0; i < var128; ++i) {
                                 skill = var130[i];
                                 if (player.getSkillLevel(skill) > 0) {
                                    sungi_skill += SkillFactory.getSkill(skill).getEffect(player.getSkillLevel(skill)).getProp();
                                    maxcount += SkillFactory.getSkill(skill).getEffect(player.getSkillLevel(skill)).getV();
                                 }
                              }

                              if (Randomizer.nextInt(100) < sungi_skill && player.lightning < maxcount) {
                                 ++player.lightning;
                              }

                              if (player.lightning < 0) {
                                 player.lightning = 0;
                              }

                              SecondaryStatValueHolder lightning;
                              if ((lightning = player.checkBuffStatValueHolder(SecondaryStat.CygnusElementSkill)) != null) {
                                 lightning.effect.applyTo(player, false);
                              }
                           }

                           if (attack.skill == 21000006 || attack.skill == 21000007 || attack.skill == 21001010) {
                              if (player.getSkillLevel(21120021) > 0 && player.getSkillCustomValue(21120021) == null) {
                                 SkillFactory.getSkill(21120021).getEffect(player.getSkillLevel(21120021)).applyTo(player, false);
                                 player.setSkillCustomInfo(21120021, 0L, 3000L);
                              } else if (player.getSkillLevel(21100015) > 0 && player.getSkillCustomValue(21120021) == null) {
                                 SkillFactory.getSkill(21100015).getEffect(player.getSkillLevel(21100015)).applyTo(player, false);
                                 player.setSkillCustomInfo(21120021, 0L, 3000L);
                              }
                           }

                           if (attack.skill == 400011079) {
                              player.getClient().getSession().writeAndFlush(CField.rangeAttack(400011079, Arrays.asList(new RangeAttack(400011081, player.getTruePosition(), 0, 0, 0))));
                           }

                           if (attack.skill == 400011080) {
                              player.getClient().getSession().writeAndFlush(CField.rangeAttack(400011080, Arrays.asList(new RangeAttack(400011082, player.getTruePosition(), 0, 0, 0))));
                           }

                           if (player.getSkillLevel(400011134) > 0 && attack.targets > 0) {
                              sungi = SkillFactory.getSkill(400011134).getEffect(player.getSkillLevel(400011134));
                              if (player.getGender() == 0 && !player.getBuffedValue(400011134) && player.getCooldownLimit(400011134) == 0L) {
                                 sungi.applyTo(player, false);
                              } else if (player.getGender() == 1 && player.getMap().getMist(player.getId(), 400011035) == null && player.getCooldownLimit(400011135) == 0L && !player.skillisCooling(400011135)) {
                                 a = SkillFactory.getSkill(400011135).getEffect(player.getSkillLevel(400011134));
                                 player.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(player, 0, 400011135, 1, 0, 0, (byte)(player.isFacingLeft() ? 1 : 0), true, player.getTruePosition(), (String)null, (Item)null));
                                 player.getMap().broadcastMessage(player, CField.EffectPacket.showEffect(player, 0, 400011135, 1, 0, 0, (byte)(player.isFacingLeft() ? 1 : 0), false, player.getTruePosition(), (String)null, (Item)null), false);
                                 newmist = new MapleMist(a.calculateBoundingBox(monster.getPosition(), player.isFacingLeft()), player, a, 2000, (byte)(player.isFacingLeft() ? 1 : 0));
                                 newmist.setPosition(monster.getPosition());
                                 player.getMap().spawnMist(newmist, false);
                                 newmist = new MapleMist(a.calculateBoundingBox(monster.getPosition(), player.isFacingLeft()), player, a, 2000, (byte)(player.isFacingLeft() ? 1 : 0));
                                 newmist.setPosition(monster.getPosition());
                                 player.getMap().spawnMist(newmist, false);
                                 newmist = new MapleMist(a.calculateBoundingBox(monster.getPosition(), player.isFacingLeft()), player, a, 2000, (byte)(player.isFacingLeft() ? 1 : 0));
                                 newmist.setPosition(monster.getPosition());
                                 player.getMap().spawnMist(newmist, false);
                                 player.addCooldown(400011135, System.currentTimeMillis(), (long)SkillFactory.getSkill(400011134).getEffect(player.getSkillLevel(400011134)).getCooldown(player));
                                 player.getClient().getSession().writeAndFlush(CField.skillCooldown(400011135, SkillFactory.getSkill(400011134).getEffect(player.getSkillLevel(400011134)).getCooldown(player)));
                              }
                           }

                           if (player.getBuffedEffect(SecondaryStat.CrystalGate) != null) {
                              sungi = player.getBuffedEffect(SecondaryStat.CrystalGate);
                              if ((double)(System.currentTimeMillis() - player.lastCrystalGateTime) >= sungi.getT() * 1000.0D) {
                                 player.lastCrystalGateTime = System.currentTimeMillis();
                                 player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(400021111, new ArrayList(), true, 0));
                              }
                           }

                           Iterator var58;
                           if (GameConstants.isKaiser(player.getJob())) {
                              if (monster != null && !player.getBuffedValue(400011118) && player.skillisCooling(400011118) && player.getSkillCustomValue(400011118) == null) {
                                 sungi = SkillFactory.getSkill(400011118).getEffect(player.getSkillLevel(400011118));
                                 if (attack.skill != 400011119 && attack.skill != 400011120) {
                                    ArrayList<SecondAtom> atoms = new ArrayList();
                                    atoms.add(new SecondAtom(13, player.getId(), monster.getId(), 360, 400011120, 10000, 18, 1, new Point((int)player.getTruePosition().getX(), (int)player.getTruePosition().getY() - 200), new ArrayList()));
                                    atoms.add(new SecondAtom(13, player.getId(), monster.getId(), 360, 400011120, 10000, 18, 1, new Point((int)player.getTruePosition().getX() - 200, (int)player.getTruePosition().getY() - 100), new ArrayList()));
                                    atoms.add(new SecondAtom(13, player.getId(), monster.getId(), 360, 400011120, 10000, 18, 1, new Point((int)player.getTruePosition().getX(), (int)player.getTruePosition().getY() + 200), new ArrayList()));
                                    atoms.add(new SecondAtom(13, player.getId(), monster.getId(), 360, 400011120, 10000, 18, 1, new Point((int)player.getTruePosition().getX() + 200, (int)player.getTruePosition().getY() - 100), new ArrayList()));
                                    atoms.add(new SecondAtom(13, player.getId(), monster.getId(), 360, 400011120, 10000, 18, 1, new Point((int)player.getTruePosition().getX() - 200, (int)player.getTruePosition().getY() + 100), new ArrayList()));
                                    atoms.add(new SecondAtom(13, player.getId(), monster.getId(), 360, 400011120, 10000, 18, 1, new Point((int)player.getTruePosition().getX() + 200, (int)player.getTruePosition().getY() + 100), new ArrayList()));
                                    player.setSkillCustomInfo(400011118, 0L, (long)(sungi.getV2() * 1000));
                                    player.getMap().spawnSecondAtom(player, atoms, 0);
                                 }
                              } else if (attack.skill == 400011118) {
                                 player.getMap().spawnSecondAtom(player, Arrays.asList(new SecondAtom(11, player.getId(), 0, 400011119, 5000, 0, -1, attack.position, new ArrayList())), 0);
                              } else if (attack.skill != 400011118 && !player.skillisCooling(400111119)) {
                                 var58 = player.getMap().getAllSecondAtomsThreadsafe().iterator();

                                 while(var58.hasNext()) {
                                    SecondAtom at = (SecondAtom)var58.next();
                                    if (at.getSkillId() == 400011119) {
                                       player.addCooldown(400111119, System.currentTimeMillis(), (long)((int)SkillFactory.getSkill(400011118).getEffect(player.getSkillLevel(400011118)).getT() * 1000));
                                       player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(400011130, new ArrayList(), true, 0));
                                       break;
                                    }
                                 }
                              }
                           }

                           List objs;
                           ArrayList monsters;
                           int i;
                           if (attack.skill >= 400051059 && attack.skill <= 400051067 && attack.skill != 400051065 && attack.skill != 400051067) {
                              SkillFactory.getSkill(400051058).getEffect(player.getSkillLevel(400051058)).applyTo(player);
                              if (attack.isLink || attack.skill == 400051066) {
                                 SkillFactory.getSkill(400051044).getEffect(player.getSkillLevel(400051044)).applyTo(player);
                                 objs = player.getMap().getMapObjectsInRange(player.getTruePosition(), 500000.0D, Arrays.asList(MapleMapObjectType.MONSTER));
                                 if (objs.size() > 0) {
                                    monsters = new ArrayList();

                                    for(i = 1; i <= objs.size(); ++i) {
                                       monsters.add(new Triple(((MapleMapObject)objs.get(Randomizer.nextInt(objs.size()))).getObjectId(), 134 + (i - 1) * 70, 0));
                                       if (i >= (player.getBuffedValue(400051058) ? effect.getW() : effect.getV2())) {
                                          break;
                                       }
                                    }

                                    player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(attack.skill == 400051066 ? 400051067 : 400051065, monsters, false, 0, player.getBuffedValue(400051058) ? 3 : 1, attack.position.x, attack.position.y, player.getBuffedValue(400051058) ? 445 : 565, player.getBuffedValue(400051058) ? 315 : 510));
                                 }
                              }
                           }

                           ArrayList sungi_skills;
                           if (attack.skill == 400041069) {
                              sungi_skills = new ArrayList();
                              sungi_skills.add(new RangeAttack(400041070, attack.position, -1, 900, 1));
                              sungi_skills.add(new RangeAttack(400041071, attack.position, -1, 1680, 1));
                              sungi_skills.add(new RangeAttack(400041072, attack.position, -1, 2460, 1));
                              sungi_skills.add(new RangeAttack(400041070, attack.position, -1, 3120, 1));
                              sungi_skills.add(new RangeAttack(400041071, attack.position, -1, 3600, 1));
                              sungi_skills.add(new RangeAttack(400041072, attack.position, -1, 3960, 1));
                              sungi_skills.add(new RangeAttack(400041070, attack.position, -1, 4200, 1));
                              sungi_skills.add(new RangeAttack(400041071, attack.position, -1, 4440, 1));
                              sungi_skills.add(new RangeAttack(400041072, attack.position, -1, 4620, 1));
                              sungi_skills.add(new RangeAttack(400041070, attack.position, -1, 4800, 1));
                              sungi_skills.add(new RangeAttack(400041071, attack.position, -1, 4920, 1));
                              sungi_skills.add(new RangeAttack(400041073, attack.position, -1, 5370, 1));
                              player.getClient().getSession().writeAndFlush(CField.rangeAttack(attack.skill, sungi_skills));
                           }

                           if (player.getSkillLevel(400011048) > 0 && !player.skillisCooling(400011048) && attack.targets > 0 && SkillFactory.getSkill(400011048).getSkillList().contains(attack.skill)) {
                              mischief = SkillFactory.getSkill(400011048).getEffect(player.getSkillLevel(400011048));
                              monsters = new ArrayList();
                              i = 400011048;
                              monsters.add(new RangeAttack(i, player.getTruePosition(), 1, 0, 1));
                              player.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(player, 0, i, 1, 0, 0, (byte)(player.isFacingLeft() ? 1 : 0), true, player.getPosition(), (String)null, (Item)null));
                              player.getMap().broadcastMessage(player, CField.EffectPacket.showEffect(player, 0, i, 1, 0, 0, (byte)(player.isFacingLeft() ? 1 : 0), false, player.getPosition(), (String)null, (Item)null), false);
                              player.getClient().getSession().writeAndFlush(CField.rangeAttack(i, monsters));
                              player.addCooldown(400011048, System.currentTimeMillis(), (long)mischief.getCooldown(player));
                              player.getClient().getSession().writeAndFlush(CField.skillCooldown(400011048, mischief.getCooldown(player)));
                           }

                           if (player.getSkillLevel(400041075) > 0 && player.getCooldownLimit(400041075) == 0L && (attack.skill == 4341004 || attack.skill == 4341009)) {
                              mischief = SkillFactory.getSkill(400041075).getEffect(player.getSkillLevel(400041075));
                              if (attack.skill == 4341004) {
                                 player.getClient().getSession().writeAndFlush(CField.rangeAttack(attack.skill, Arrays.asList(new RangeAttack(400041076, attack.position, 0, 0, 6))));
                              } else if (attack.skill == 4341009) {
                                 monsters = new ArrayList();
                                 monsters.add(new RangeAttack(400041078, attack.position, 0, 0, 6));
                                 player.getClient().getSession().writeAndFlush(CField.rangeAttack(attack.skill, monsters));
                              }

                              player.getClient().send(CField.skillCooldown(mischief.getSourceId(), mischief.getCooldown(player)));
                              player.addCooldown(mischief.getSourceId(), System.currentTimeMillis(), (long)mischief.getCooldown(player));
                           }

                           int i;
                           if (player.getSkillLevel(23110004) > 0 && attack.isLink && attack.charge == 0 && !player.getBuffedValue(400031017)) {
                              int[] var65 = new int[]{23121052, 400031007, 23111002, 23121002};
                              i = var65.length;

                              for(i = 0; i < i; ++i) {
                                 ck = var65[i];
                                 if (player.skillisCooling(ck)) {
                                    if (ck == 400031007) {
                                       player.setSkillCustomInfo(400031007, 0L, (long)(player.getSkillCustomTime(400031007) - 1000));
                                    } else {
                                       player.changeCooldown(ck, -1000);
                                    }
                                 }
                              }

                              if (!player.getBuffedValue(23110004)) {
                                 player.removeSkillCustomInfo(23110005);
                              }

                              if (player.getSkillCustomValue0(23110005) < 10L) {
                                 player.setSkillCustomInfo(23110005, player.getSkillCustomValue0(23110005) + 1L, 0L);
                              }

                              SkillFactory.getSkill(23110004).getEffect(player.getSkillLevel(23110004)).applyTo(player);
                           }

                           if (attack.skill == 23121000 && player.getBuffedValue(23110004) && !player.getBuffedValue(400031017)) {
                              SkillFactory.getSkill(23110004).getEffect(player.getSkillLevel(23110004)).applyTo(player, false);
                           }

                           if (totDamage > 0L && attack.isLink && player.getSkillLevel(400051044) > 0 && (attack.skill < 400051059 || attack.skill > 400051067) && attack.skill != 0 || attack.skill == 400051096) {
                              if (player.getBuffedValue(400051044)) {
                                 if (player.getBuffedValue(SecondaryStat.Striker3rd) <= 8) {
                                    if (player.striker3rdStack >= 8 && attack.skill == 400051096) {
                                       player.striker3rdStack = 0;
                                    }

                                    SkillFactory.getSkill(400051044).getEffect(player.getSkillLevel(400051044)).applyTo(player, false);
                                 }
                              } else {
                                 SkillFactory.getSkill(400051044).getEffect(player.getSkillLevel(400051044)).applyTo(player, false);
                              }
                           }

                           if (GameConstants.isAngelicBuster(player.getJob()) && totDamage > 0L) {
                              player.Recharge(attack.skill);
                              if (attack.skill == 65121007 || attack.skill == 65121008) {
                                 SkillFactory.getSkill(65121101).getEffect(player.getSkillLevel(65121101)).applyTo(player);
                              }
                           }

                           if (player.getBuffedValue(400031006) && attack.skill == 400031010) {
                              --player.trueSniping;
                              if (player.trueSniping <= 0) {
                                 player.cancelEffectFromBuffStat(SecondaryStat.TrueSniping);
                              } else {
                                 player.getBuffedEffect(400031006).applyTo(player, false);
                              }
                           }

                           if (attack.skill == 4331003 && (hpMob <= 0L || totDamageToOneMonster < hpMob)) {
                              return;
                           }

                           if (hpMob > 0L && totDamageToOneMonster > 0L) {
                              player.afterAttack(attack);
                           }

                           if (player.getBuffedValue(400031007) && totDamageToOneMonster > 0L && player.getSkillCustomValue(400031007) == null) {
                              player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(400031011, new ArrayList(), true, 0));
                              player.setSkillCustomInfo(400031007, 0L, (long)(player.getBuffedEffect(400031007).getS2() * 1000));
                           }

                           int i;
                           if ((attack.skill == 11121052 || attack.skill == 11121055 || attack.skill == 400011056) && player.getSkillLevel(400011048) > 0 && player.getCooldownLimit(400011048) > 0L) {
                              mischief = SkillFactory.getSkill(400011048).getEffect(player.getSkillLevel(400011048));
                              i = attack.hits;
                              if (player.getBuffedEffect(SecondaryStat.Buckshot) != null) {
                                 i /= 2;
                              }

                              player.changeCooldown(400011048, -(mischief.getZ() / i));
                           }

                           if (attack.skill == 11121157) {
                              effect.applyTo(player, false);
                           }

                           ArrayList attackk;
                           if (attack.targets > 0 && player.getBuffedValue(400051010) && !GameConstants.is_forceAtom_attack_skill(attack.skill) && player.getSkillCustomValue(400051010) == null && attack.skill != 25121055 && attack.skill != 25111012) {
                              Skill skill = SkillFactory.getSkill(400051010);
                              Exceed = null;
                              attackk = new ArrayList();
                              List<RandomSkillEntry> rse = skill.getRSE();
                              var25 = rse.iterator();

                              while(var25.hasNext()) {
                                 RandomSkillEntry info = (RandomSkillEntry)var25.next();
                                 if (Randomizer.isSuccess(info.getProb())) {
                                    if (info.getSkillList().size() > 0) {
                                       attackk.addAll(info.getSkillList());
                                    } else {
                                       attackk.add(new Pair(info.getSkillId(), 0));
                                    }

                                    if (info.getSkillId() != 25121055 && info.getSkillId() != 25111012) {
                                       player.getClient().getSession().writeAndFlush(CField.SpiritFlow(attackk));
                                    } else {
                                       sungi_skill = info.getSkillId() == 25121055 ? 25121030 : 25111012;
                                       if (player.getSkillLevel(25121055) < 1) {
                                          player.changeSkillLevel(25121055, (byte)1, (byte)1);
                                       }

                                       Exceed = SkillFactory.getSkill(info.getSkillId()).getEffect(sungi_skill);
                                       MapleMist mist = new MapleMist(Exceed.calculateBoundingBox(player.getPosition(), player.isFacingLeft()), player, Exceed, 3000, (byte)(player.isFacingLeft() ? 1 : 0));
                                       mist.setPosition(player.getPosition());
                                       player.getMap().spawnMist(mist, false);
                                    }

                                    player.setSkillCustomInfo(400051010, 0L, (long)(player.getBuffedEffect(400051010).getX() * 1000));
                                    break;
                                 }
                              }
                           }

                           Iterator var76;
                           if (GameConstants.isNightWalker(player.getJob())) {
                              if (!GameConstants.is_forceAtom_attack_skill(attack.skill) && player.getBuffedEffect(SecondaryStat.DarkSight) != null && attack.summonattack == 0) {
                                 player.cancelEffectFromBuffStat(SecondaryStat.DarkSight);
                              }
                           } else if (!GameConstants.isKadena(player.getJob()) && !GameConstants.isHoyeong(player.getJob()) && !GameConstants.isKhali(player.getJob()) && player.getBuffedValue(SecondaryStat.DarkSight) != null && !player.getBuffedValue(400001023)) {
                              int prop = 0;
                              if (player.getSkillLevel(4210015) > 0) {
                                 prop = SkillFactory.getSkill(4210015).getEffect(player.getSkillLevel(4210015)).getProp();
                              }

                              if (player.getSkillLevel(4330001) > 0) {
                                 prop = SkillFactory.getSkill(4330001).getEffect(player.getSkillLevel(4330001)).getProp();
                              }

                              if (player.getSkillLevel(154121009) > 0) {
                                 prop = 100;
                              }

                              var76 = player.getMap().getAllMistsThreadsafe().iterator();

                              while(var76.hasNext()) {
                                 MapleMist mist = (MapleMist)var76.next();
                                 if (mist.getOwnerId() == player.getId() && mist.getSource().getSourceId() == 4221006 && mist.getBox().contains(player.getTruePosition())) {
                                    prop = 100;
                                    break;
                                 }
                              }

                              if (player.getSkillCustomValue0(4221052) != 0L && (long)player.getPosition().x <= player.getSkillCustomValue0(4221052) + 300L && (long)player.getPosition().x >= player.getSkillCustomValue0(4221052) - 300L) {
                                 prop = 100;
                              }

                              if (!Randomizer.isSuccess(prop)) {
                                 player.cancelEffectFromBuffStat(SecondaryStat.DarkSight);
                              }
                           } else if (player.getBuffedValue(SecondaryStat.DarkSight) != null && !player.getBuffedValue(400001023) && !GameConstants.is_forceAtom_attack_skill(attack.skill) && !GameConstants.isDarkSightDispelSkill(attack.skill) && attack.skill < 400000000) {
                              player.cancelEffectFromBuffStat(SecondaryStat.DarkSight);
                           }

                           SecondaryStatEffect combatRecovery;
                           if (player.getSkillLevel(101110205) > 0 && player.getGender() == 0 && totDamageToOneMonster > 0L && (combatRecovery = SkillFactory.getSkill(101110205).getEffect(player.getSkillLevel(101110205))).makeChanceResult()) {
                              player.addMP((long)combatRecovery.getZ());
                           }

                           MapleSummon summon;
                           MapleAtom atom;
                           ForceAtom ft;
                           if (totDamage > 0L && player.getBuffedValue(400021073) && (summon = player.getSummon(400021073)) != null && summon.getEnergy() < 22) {
                              switch(attack.skill) {
                              case 22110014:
                              case 22110024:
                              case 22110025:
                              case 22111011:
                              case 22140014:
                              case 22140015:
                              case 22140023:
                              case 22140024:
                              case 22141011:
                              case 22170064:
                              case 22170065:
                              case 22170066:
                              case 22170067:
                              case 22170093:
                              case 22170094:
                              case 22171063:
                              case 22171083:
                              case 22171095:
                              case 400021013:
                                 if (!summon.getMagicSkills().contains(attack.skill)) {
                                    summon.getMagicSkills().add(attack.skill);
                                    summon.setEnergy(Math.min(22, summon.getEnergy() + 8));
                                    atom = new MapleAtom(true, summon.getObjectId(), 29, true, 400021073, summon.getTruePosition().x, summon.getTruePosition().y);
                                    atom.setDwUserOwner(summon.getOwner().getId());
                                    monsters = new ArrayList();
                                    monsters.add(0);
                                    atom.addForceAtom(new ForceAtom(5, 37, Randomizer.rand(5, 10), 62, 0));
                                    atom.setDwTargets(monsters);
                                    player.getMap().spawnMapleAtom(atom);
                                    player.getClient().getSession().writeAndFlush(CField.SummonPacket.ElementalRadiance(summon, 2));
                                    player.getClient().getSession().writeAndFlush(CField.SummonPacket.specialSummon(summon, 2));
                                    if (summon.getEnergy() >= 22) {
                                       player.getClient().getSession().writeAndFlush(CField.SummonPacket.damageSummon(summon));
                                    }
                                 }
                                 break;
                              case 22110022:
                              case 22110023:
                              case 22111012:
                              case 22170060:
                              case 22170070:
                              case 400021012:
                              case 400021014:
                              case 400021015:
                                 atom = new MapleAtom(true, summon.getObjectId(), 29, true, 400021073, summon.getTruePosition().x, summon.getTruePosition().y);
                                 atom.setDwUserOwner(summon.getOwner().getId());
                                 atom.setDwFirstTargetId(0);
                                 ft = new ForceAtom(5, 37, Randomizer.rand(5, 10), 62, 0);
                                 atom.addForceAtom(ft);
                                 player.getMap().spawnMapleAtom(atom);
                                 summon.setEnergy(Math.min(SkillFactory.getSkill(400021073).getEffect(player.getSkillLevel(400021073)).getX(), summon.getEnergy() + 5));
                                 player.getClient().getSession().writeAndFlush(CField.SummonPacket.ElementalRadiance(summon, 2));
                                 player.getClient().getSession().writeAndFlush(CField.SummonPacket.specialSummon(summon, 2));
                                 if (summon.getEnergy() >= SkillFactory.getSkill(400021073).getEffect(player.getSkillLevel(400021073)).getX()) {
                                    player.getClient().getSession().writeAndFlush(CField.SummonPacket.damageSummon(summon));
                                 }
                              }
                           }

                           if (player.getBuffedEffect(SecondaryStat.ComboInstict) != null && (attack.skill == 1121008 || attack.skill == 1120017)) {
                              player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(400011074, new ArrayList(), true, 0));
                              player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(400011075, new ArrayList(), true, 0));
                              player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(400011076, new ArrayList(), true, 0));
                           }

                           Iterator var73;
                           if (player.getBuffedValue(400001037) && attack.skill != 400001038 && System.currentTimeMillis() - player.lastAngelTime >= (long)(player.getBuffedEffect(400001037).getZ() * 1000)) {
                              player.lastAngelTime = System.currentTimeMillis();
                              sungi_skills = new ArrayList();
                              i = 0;

                              for(var73 = attack.allDamage.iterator(); var73.hasNext(); ++i) {
                                 AttackPair a = (AttackPair)var73.next();
                                 sungi_skills.add(new Triple(a.objectId, 291 + 70 * i, 0));
                              }

                              player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(400001038, sungi_skills, true, 0));
                           }

                           if (attack.skill == 155120000 && player.getSkillLevel(400051047) > 0 && player.getCooldownLimit(400051047) == 0L) {
                              player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(400051047, new ArrayList(), true, 0));
                           }

                           if (attack.skill == 155120001 && player.getSkillLevel(400051047) > 0 && player.getCooldownLimit(400051048) == 0L) {
                              player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(400051048, new ArrayList(), true, 0));
                           }

                           if (attack.skill == 3321014 || attack.skill == 3321016 || attack.skill == 3321018 || attack.skill == 3321020) {
                              player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(attack.skill + 1, new ArrayList(), true, 600));
                           }

                           if (attack.skill == 400041042) {
                              player.getClient().getSession().writeAndFlush(CField.rangeAttack(attack.skill, Arrays.asList(new RangeAttack(400041043, attack.position, 1, 0, 0))));
                           }

                           if (player.getBuffedValue(21101005) && totDamageToOneMonster > 0L && player.getBuffedEffect(SecondaryStat.DebuffIncHp) == null) {
                              player.addHP(player.getStat().getCurrentMaxHp() * (long)player.getBuffedEffect(21101005).getX() / 100L);
                           }

                           if (SkillFactory.getSkill(attack.skill) != null && player.getSkillLevel(1310009) > 0 && !SkillFactory.getSkill(attack.skill).isFinalAttack() && totDamageToOneMonster > 0L && player.getBuffedEffect(SecondaryStat.DebuffIncHp) == null && SkillFactory.getSkill(1310009).getEffect(player.getSkillLevel(1310009)).makeChanceResult()) {
                              player.addHP(player.getStat().getCurrentMaxHp() * (long)SkillFactory.getSkill(1310009).getEffect(player.getSkillLevel(1310009)).getX() / 100L);
                           }

                           if (totDamageToOneMonster > 0L && player.getBuffedEffect(SecondaryStat.DebuffIncHp) == null && player.getBuffedValue(1321054)) {
                              player.addHP(player.getStat().getCurrentMaxHp() * (long)player.getBuffedEffect(1321054).getX() / 100L);
                           }

                           if (player.getSkillLevel(60030241) > 0 || player.getSkillLevel(80003015) > 0) {
                              i = player.getSkillLevel(60030241) > 0 ? 60030241 : (player.getSkillLevel(80003015) > 0 ? 80003015 : 0);
                              if (i > 0 && monster != null) {
                                 if (monster.getStats().isBoss()) {
                                    if (monster.isAlive()) {
                                       player.handlePriorPrepaRation(i, 2);
                                    }
                                 } else if (!monster.isAlive()) {
                                    player.handlePriorPrepaRation(i, 1);
                                 }
                              }
                           }

                           if (player.getSkillLevel(400051015) > 0) {
                              if (monster.getStats().isBoss()) {
                                 if (monster.isAlive()) {
                                    ++player.Serpent;
                                 }
                              } else if (!monster.isAlive()) {
                                 ++player.Serpent2;
                                 player.Serpent += 3;
                              } else if (monster.isAlive()) {
                                 player.Serpent += 3;
                              }

                              if (player.Serpent2 > 40) {
                                 player.Serpent2 = 0;
                                 player.changeCooldown(400051015, -10000);
                              }
                           }

                           if (player.getSkillLevel(150010241) > 0 && player.getSkillCustomValue(80000514) == null) {
                              SkillFactory.getSkill(150010241).getEffect(player.getSkillLevel(150010241)).applyTo(player);
                              player.setSkillCustomInfo(80000514, 0L, 3000L);
                           } else if (player.getSkillLevel(80000514) > 0 && player.getSkillCustomValue(80000514) == null) {
                              SkillFactory.getSkill(80000514).getEffect(player.getSkillLevel(80000514)).applyTo(player);
                              player.setSkillCustomInfo(80000514, 0L, 3000L);
                           }

                           if (MapleSkillManager.isKhaliVoydSkills(attack.skill) && monster != null && player.getSkillLevel(154001003) > 0 && player.getSkillCustomValue(154001003) == null) {
                              SkillFactory.getSkill(154121009).getEffect(player.getSkillLevel(154121009)).applyTo(player);
                              player.setSkillCustomInfo(154121009, 0L, 3000L);
                           }

                           if (MapleSkillManager.isKhaliAttackSkills(attack.skill) && attack.targets > 0 && System.currentTimeMillis() - player.lastAstraTime >= 14000L) {
                              player.lastAstraTime = System.currentTimeMillis();
                              player.getClient().getSession().writeAndFlush(CField.getLightOfCurigi(400041087));
                           }

                           MapleSummon s;
                           if (player.getBuffedValue(SecondaryStat.SummonChakri) != null && player.getBuffedValue(SecondaryStat.ResonateUltimatum) == null) {
                              var58 = player.getMap().getAllSummons(154110010).iterator();

                              while(var58.hasNext()) {
                                 s = (MapleSummon)var58.next();
                                 if (s != null && s.getOwner().getId() == player.getId() && MapleSkillManager.isKhaliHexSkills(attack.skill) && monster != null) {
                                    attackk = new ArrayList();
                                    attackk.add(new SecondAtom(43, player.getId(), monster.getId(), 154110001, 5000, 0, 3, new Point(s.getTruePosition().x, s.getTruePosition().y), Arrays.asList(0)));
                                    player.spawnSecondAtom(attackk);
                                    s.removeSummon(player.getMap(), false);
                                    player.getSummons().remove(s);
                                    player.SummonChakriStack = 0;
                                 }
                              }
                           }

                           if (player.getBuffedValue(SecondaryStat.ResonateUltimatum) != null) {
                              var58 = player.getMap().getAllSummonsThreadsafe().iterator();

                              label4303:
                              while(true) {
                                 do {
                                    do {
                                       do {
                                          do {
                                             if (!var58.hasNext()) {
                                                break label4303;
                                             }

                                             s = (MapleSummon)var58.next();
                                          } while(s == null);
                                       } while(s.getOwner().getId() != player.getId());
                                    } while(!MapleSkillManager.isKhaliHexSkills(attack.skill));
                                 } while(monster == null);

                                 attackk = new ArrayList();

                                 for(i = 0; i < 2; ++i) {
                                    attackk.add(new SecondAtom(44, player.getId(), monster.getId(), 400041091, 5000, 0, 3, new Point(s.getTruePosition().x, s.getTruePosition().y), Arrays.asList(0)));
                                 }

                                 player.spawnSecondAtom(attackk);
                                 s.removeSummon(player.getMap(), false);
                                 player.getSummons().remove(s);
                                 player.SummonChakriStack = 0;
                              }
                           }

                           if (player.getBuffedValue(63121044) && player.getSkillCustomValue(80113017) == null && (long)player.getId() != player.getSkillCustomValue0(63121044)) {
                              mischief = null;
                              List<MapleMapObject> objs = player.getMap().getMapObjectsInRange(player.getTruePosition(), 600000.0D, Arrays.asList(MapleMapObjectType.MONSTER));
                              if (objs.size() > 0) {
                                 for(i = 1; i <= objs.size(); ++i) {
                                    MapleMonster mob = (MapleMonster)objs.get(Randomizer.nextInt(objs.size()));
                                    if (mob != null && mob.isAlive()) {
                                       RangeAttack rg = new RangeAttack(80003017, mob.getTruePosition(), 0, 0, 1);
                                       rg.getList().add(mob.getObjectId());
                                       player.getClient().getSession().writeAndFlush(CField.rangeAttack(80003017, Arrays.asList(rg)));
                                       player.setSkillCustomInfo(80113017, 0L, (long)(player.getBuffedEffect(63121044).getY() * 1000));
                                       break;
                                    }
                                 }
                              }
                           }

                           if (attack.targets > 0) {
                              int fallingspeed = 30;
                              i = GameConstants.getFallingTime(attack.skill);
                              if (i != -1) {
                                 player.getClient().getSession().writeAndFlush(CField.setFallingTime(fallingspeed, i));
                              }
                           }

                           boolean givebuff;
                           if (monster != null) {
                              if (monster.getBuff(MonsterStatus.MS_DarkLightning) != null && attack.skill != 400021113 && attack.skill != 32110020 && attack.skill != 32111016 && attack.skill != 32101001 && attack.skill != 32111015 && attack.skill != 400021088) {
                                 sungi_skills = new ArrayList();
                                 sungi_skills.add(new Triple(monster.getObjectId(), 0, 0));
                                 player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(32110020, sungi_skills, false, 0));
                                 givebuff = true;
                                 if (player.getBuffedValue(400021087)) {
                                    i = SkillFactory.getSkill(400021087).getEffect(player.getSkillLevel(400021087)).getS2();
                                    if (player.getSkillCustomValue0(410021087) < (long)(i - 1)) {
                                       player.addSkillCustomInfo(410021087, 1L);
                                       givebuff = false;
                                    } else {
                                       player.removeSkillCustomInfo(410021087);
                                    }
                                 }

                                 if (givebuff) {
                                    monster.cancelStatus(MonsterStatus.MS_DarkLightning, monster.getBuff(MonsterStatus.MS_DarkLightning));
                                 }
                              }

                              if (GameConstants.isAran(player.getJob())) {
                                 if (SkillFactory.getSkill(400011122).getSkillList().contains(attack.skill) && monster.getCustomValue0(400011121) > 0L && monster.getCustomTime(400011122) != null && player.getBuffedValue(400011123)) {
                                    sungi_skills = new ArrayList();
                                    if (monster.getCustomValue0(400011121) < 6L) {
                                       monster.addSkillCustomInfo(400011121, 1L);
                                    }

                                    sungi_skills.add(new Triple(monster.getObjectId(), (int)monster.getCustomValue0(400011121), monster.getCustomTime(400011122)));
                                    player.getMap().broadcastMessage(CField.getBlizzardTempest(sungi_skills));
                                    monsters = new ArrayList();
                                    player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(400011122, monsters, true, 0));
                                 } else if (attack.skill == 400011121) {
                                    sungi_skills = new ArrayList();
                                    var76 = player.getMap().getAllMonster().iterator();

                                    while(var76.hasNext()) {
                                       MapleMonster m3 = (MapleMonster)var76.next();
                                       if (m3 != null && m3.getCustomValue0(400011121) > 0L && m3.getCustomTime(400011122) != null) {
                                          sungi_skills.add(new Triple(m3.getObjectId(), (int)m3.getCustomValue0(400011121), m3.getCustomTime(400011122)));
                                       }
                                    }

                                    SkillFactory.getSkill(400011123).getEffect(player.getSkillLevel(400011121)).applyTo(player);
                                    if (!sungi_skills.isEmpty()) {
                                       player.getMap().broadcastMessage(CField.getBlizzardTempest(sungi_skills));
                                    }
                                 }
                              }
                           }

                           if (effect != null && player.getBuffedValue(SecondaryStat.OverloadMana) != null && !GameConstants.is_forceAtom_attack_skill(attack.skill) && !effect.isMist()) {
                              if (GameConstants.isKinesis(player.getJob())) {
                                 player.addHP((long)((int)(-(player.getStat().getCurrentMaxHp() * (long)player.getBuffedEffect(SecondaryStat.OverloadMana).getY() / 100L))));
                              } else {
                                 player.addMP((long)((int)(-(player.getStat().getCurrentMaxMp(player) * (long)player.getBuffedEffect(SecondaryStat.OverloadMana).getX() / 100L))));
                              }
                           }

                           if (GameConstants.isCaptain(player.getJob()) && attack.skill == 400051073) {
                              player.getClient().getSession().writeAndFlush(CField.rangeAttack(400051081, Arrays.asList(new RangeAttack(400051081, monster.getPosition(), 0, 0, 1))));
                           }

                           Iterator var110;
                           MapleAtom atom;
                           MapleMonster mob;
                           if (GameConstants.isAdel(player.getJob())) {
                              if (player.getBuffedValue(151101013) && player.getSkillCustomValue(151101013) == null && attack.targets > 0 && (attack.skill == 151101000 || attack.skill == 151111000 || attack.skill == 151121000 || attack.skill == 151121002)) {
                                 player.addMP((long)(-player.getBuffedEffect(151101013).getY()));
                                 player.getMap().broadcastMessage(SkillPacket.CreateSubObtacle(player, 151001001));
                                 player.setSkillCustomInfo(151101013, 0L, 8000L);
                              }

                              if (player.getSkillLevel(151100017) > 0 && attack.targets > 0 && (attack.skill == 151101000 || attack.skill == 151111000 || attack.skill == 151121000 || attack.skill == 151121002)) {
                                 player.에테르핸들러(player, 12, attack.skill, false);
                              }

                              if (player.getBuffedValue(151101006) && player.에테르소드 > 0 && player.getSkillCustomValue(151101016) == null && attack.targets > 0 && (attack.skill == 151101000 || attack.skill == 151111000 || attack.skill == 151121000 || attack.skill == 151121002)) {
                                 for(int i = 1; i <= player.에테르소드; ++i) {
                                    player.getMap().broadcastMessage(SkillPacket.AutoAttackObtacleSword(player, i * 10, i == 1 ? player.에테르소드 : 0));
                                 }

                                 if (player.getJob() == 15112) {
                                    player.setSkillCustomInfo(151101016, 0L, 1500L);
                                 } else if (player.getJob() == 15111) {
                                    player.setSkillCustomInfo(151101016, 0L, 5500L);
                                 } else {
                                    player.setSkillCustomInfo(151101016, 0L, 9500L);
                                 }
                              }

                              if (monster != null && attack != null) {
                                 switch(attack.skill) {
                                 case 151101007:
                                 case 151101008:
                                 case 151101009:
                                    if (Randomizer.isSuccess(30) && attack.targets > 0) {
                                       player.에테르결정(player, monster.getTruePosition(), false);
                                    }
                                    break;
                                 case 151111002:
                                    if (Randomizer.isSuccess(40) && attack.targets > 0) {
                                       player.에테르결정(player, monster.getTruePosition(), false);
                                    }
                                    break;
                                 case 151111003:
                                    if (Randomizer.isSuccess(15) && attack.targets > 0) {
                                       player.에테르결정(player, monster.getTruePosition(), false);
                                    }
                                    break;
                                 case 151121003:
                                    if (Randomizer.isSuccess(50) && attack.targets > 0) {
                                       player.에테르결정(player, monster.getTruePosition(), false);
                                    }
                                    break;
                                 case 400011108:
                                    if (Randomizer.isSuccess(5) && attack.targets > 0) {
                                       player.에테르결정(player, monster.getTruePosition(), false);
                                    }
                                 }
                              }

                              if (attack.skill != 151101003 && (attack.skill != 151101004 || attack.targets <= 0)) {
                                 if (attack.skill == 151121001 && monster != null) {
                                    player.setSkillCustomInfo(151121001, (long)monster.getObjectId(), 0L);
                                    if (player.getSkillCustomValue0(151121001) != (long)monster.getObjectId()) {
                                       player.removeSkillCustomInfo(151121001);
                                    }

                                    SkillFactory.getSkill(151121001).getEffect(player.getSkillLevel(151121001)).applyTo(player);
                                 }
                              } else {
                                 if (!player.getBuffedValue(151101010)) {
                                    player.removeSkillCustomInfo(151101010);
                                 }

                                 SkillFactory.getSkill(151101010).getEffect(player.getSkillLevel(151101003)).applyTo(player);
                              }
                           } else if (GameConstants.isCain(player.getJob()) && attack.targets > 0) {
                              if (attack.targets > 0) {
                                 if (player.getBuffedValue(400031062) && !player.skillisCooling(400031063)) {
                                    boolean facingleft = (attack.facingleft >>> 4 & 15) == 8;

                                    for(i = 0; i < player.getBuffedEffect(400031062).getMobCount(); ++i) {
                                       i = attack.position.x;
                                       i = attack.position.y - Randomizer.rand(50, 210);
                                       i = facingleft ? i + Randomizer.rand(50, 200) : i - Randomizer.rand(50, 200);
                                       player.createSecondAtom(SkillFactory.getSkill(400031063).getSecondAtoms(), new Point(i, i), facingleft);
                                    }

                                    player.addCooldown(400031063, System.currentTimeMillis(), (long)player.getBuffedEffect(400031062).getSubTime());
                                 }

                                 if (player.getSkillCustomValue(63101114) == null && (SkillFactory.getSkill(63101001).getSkillList().contains(attack.skill) || SkillFactory.getSkill(63101001).getSkillList2().contains(attack.skill))) {
                                    player.handlePossession(2);
                                    if (attack.skill == 63101004) {
                                       player.setSkillCustomInfo(63101114, 0L, 1000L);
                                    }
                                 }

                                 if (player.getBuffedValue(63101005)) {
                                    mischief = SkillFactory.getSkill(63101005).getEffect(player.getSkillLevel(63101005));
                                    if (SkillFactory.getSkill(63101005).getSkillList().contains(attack.skill) || SkillFactory.getSkill(63101005).getSkillList2().contains(attack.skill)) {
                                       if (player.getSkillCustomValue0(63101006) > 0L) {
                                          var76 = player.getMap().getAllSecondAtoms().iterator();

                                          while(var76.hasNext()) {
                                             MapleSecondAtom at = (MapleSecondAtom)var76.next();
                                             if (at.getSourceId() == 63101006 && at.getChr().getId() == player.getId() && (long)(mischief.getU() * 1000) - (System.currentTimeMillis() - at.getLastAttackTime()) <= 0L) {
                                                player.getMap().broadcastMessage(SkillPacket.AttackSecondAtom(player, at.getObjectId(), 1));
                                             }
                                          }
                                       }

                                       player.addSkillCustomInfo(63101005, 1L);
                                       if (player.getSkillCustomValue0(63101005) >= 7L && player.getSkillCustomValue0(63101006) < (long)mischief.getQ()) {
                                          player.addSkillCustomInfo(63101006, 1L);
                                          player.removeSkillCustomInfo(63101005);
                                          player.createSecondAtom(63101006, player.getPosition(), (int)player.getSkillCustomValue0(63101006) - 1);
                                       }
                                    }
                                 }

                                 if (attack.skill == 63001000 || attack.skill == 63101003 || attack.skill == 63111002) {
                                    RangeAttack rand = new RangeAttack(63001001, attack.position, 0, 0, 1);
                                    monsters = new ArrayList();
                                    monsters.add(rand);
                                    player.getClient().getSession().writeAndFlush(CField.rangeAttack(attack.skill, monsters));
                                 }

                                 if (player.getBuffedValue(400031062) && player.getSkillCustomValue(400031063) == null && attack.skill != 63111012 && attack.skill != 63101006 && attack.skill != 63111010) {
                                    mischief = SkillFactory.getSkill(400031062).getEffect(player.getSkillLevel(400031062));
                                    player.addSkillCustomInfo(400031062, 1L);
                                    if (player.getSkillCustomValue0(400031062) >= (long)mischief.getW()) {
                                       player.getClient().getSession().writeAndFlush(CField.rangeAttack(mischief.getSourceId(), Arrays.asList(new RangeAttack(400031063, monster.getPosition(), 0, 0, 1))));
                                       player.removeSkillCustomInfo(400031062);
                                       player.setSkillCustomInfo(400031063, 0L, (long)mischief.getSubTime());
                                    }
                                 }

                                 if (player.getSkillLevel(400031066) > 0 && monster != null) {
                                    mischief = SkillFactory.getSkill(400031066).getEffect(player.getSkillLevel(400031066));
                                    givebuff = false;
                                    if (monster.getStats().isBoss()) {
                                       player.addSkillCustomInfo(400031067, 1L);
                                       if (player.getSkillCustomValue0(400031067) >= (long)mischief.getQ2()) {
                                          player.removeSkillCustomInfo(400031067);
                                          givebuff = true;
                                       }
                                    } else if (!monster.isAlive()) {
                                       player.addSkillCustomInfo(400031068, 1L);
                                       if (player.getSkillCustomValue0(400031068) >= (long)mischief.getQ2()) {
                                          player.removeSkillCustomInfo(400031068);
                                          givebuff = true;
                                       }
                                    }

                                    if (givebuff) {
                                       player.addSkillCustomInfo(400031066, 1L);
                                       if (player.getSkillCustomValue0(400031066) >= (long)mischief.getU()) {
                                          player.setSkillCustomInfo(400031066, (long)mischief.getU(), 0L);
                                       }

                                       mischief.applyTo(player);
                                    }
                                 }
                              }
                           } else if (GameConstants.isBlaster(player.getJob())) {
                              if (afterimageshockattack) {
                                 mischief = SkillFactory.getSkill(400011116).getEffect(player.getSkillLevel(400011116));
                                 long duration = player.getBuffLimit(400011116);
                                 player.setSkillCustomInfo(400011116, player.getSkillCustomValue0(400011116) - 1L, 0L);
                                 if (player.getSkillCustomValue0(400011116) > 0L) {
                                    mischief.applyTo(player, false, (int)duration);
                                 } else {
                                    player.cancelEffectFromBuffStat(SecondaryStat.AfterImageShock, 400011116);
                                 }
                              }
                           } else if (GameConstants.isDemonAvenger(player.getJob())) {
                              if (!player.getBuffedValue(30010230)) {
                                 player.updateExceed(player.getExceed());
                              }

                              if (attack.skill == 31221052) {
                                 player.gainExceed((short)5);
                              }

                              if (GameConstants.isExceedAttack(attack.skill)) {
                                 if (player.getSkillLevel(31220044) > 0) {
                                    if (player.getExceed() < 19) {
                                       player.gainExceed((short)1);
                                    }
                                 } else if (player.getExceed() < 20) {
                                    player.gainExceed((short)1);
                                 }

                                 player.handleExceedAttack(attack.skill);
                              }

                              if (player.getBuffedValue(31221054)) {
                                 player.addHP(stats.getCurrentMaxHp() / 100L * 5L);
                              }

                              if (attack.skill == 31211001) {
                                 player.addHP(stats.getCurrentMaxHp() / 100L * 10L, true, false);
                              } else if (attack.skill == 400011062) {
                                 player.addHP(stats.getCurrentMaxHp() / 100L * 10L, true, false);
                                 player.getClient().getSession().writeAndFlush(CField.skillCooldown(400011038, effect.getCooldown(player)));
                              } else if (attack.skill == 400011063) {
                                 player.addHP(stats.getCurrentMaxHp() / 100L * 15L, true, false);
                                 player.getClient().getSession().writeAndFlush(CField.skillCooldown(400011038, effect.getCooldown(player)));
                              } else if (attack.skill == 400011064) {
                                 player.addHP(stats.getCurrentMaxHp() / 100L * 20L, true, false);
                                 player.getClient().getSession().writeAndFlush(CField.skillCooldown(400011038, effect.getCooldown(player)));
                              }

                              if (SkillFactory.getSkill(attack.skill) != null && attack.targets > 0 && attack.skill != 31220007 && !SkillFactory.getSkill(attack.skill).isFinalAttack() && monster != null) {
                                 mischief = SkillFactory.getSkill(31010002).getEffect(player.getSkillLevel(31010002));
                                 Exceed = SkillFactory.getSkill(30010230).getEffect(player.getSkillLevel(30010230));
                                 i = mischief.getX();
                                 if (player.getSkillLevel(31210006) > 0) {
                                    i = SkillFactory.getSkill(31210006).getEffect(player.getSkillLevel(31210006)).getX();
                                 }

                                 if (GameConstants.isExceedAttack(attack.skill)) {
                                    int minusper = false;
                                    i = player.getExceed() / Exceed.getZ() * Exceed.getY();
                                    if (player.getSkillLevel(31210005) > 0 && i > 0 && (i -= SkillFactory.getSkill(31210005).getEffect(player.getSkillLevel(31210005)).getX()) < 0) {
                                       i = 0;
                                    }

                                    if (i > 0) {
                                       i -= i;
                                    }
                                 }

                                 player.addHP(stats.getCurrentMaxHp() / 100L * (long)i);
                              }
                           } else if (GameConstants.isBattleMage(player.getJob())) {
                              if (player.getBuffedEffect(SecondaryStat.AbyssalLightning) != null && player.getSkillCustomValue(400021113) == null && monster != null) {
                                 objs = player.getMap().getMapObjectsInRange(player.getTruePosition(), 100000.0D, Arrays.asList(MapleMapObjectType.MONSTER));
                                 if (objs != null && objs.size() >= 0) {
                                    MapleMonster mob__ = (MapleMonster)player.getMap().getAllMonster().get(Randomizer.rand(0, objs.size()));
                                    attackk = new ArrayList();
                                    attackk.add(new Triple(monster.getObjectId(), 0, 0));
                                    player.getClient().getSession().writeAndFlush(CField.Abyssal_Lightning(400021113, 5, mob__.getPosition().x));
                                    player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(400021113, attackk, false, 0));
                                    player.setSkillCustomInfo(400021113, 0L, 20000L);
                                 }
                              }
                           } else if (GameConstants.isWildHunter(player.getJob())) {
                              if (player.getBuffedValue(SecondaryStat.JaguarSummoned) != null) {
                                 player.setSkillCustomInfo(33001001, 0L, 10000L);
                                 player.getClient().send(CField.SummonPacket.JaguarAutoAttack(true));
                              }

                              if (attack.skill == 33121214 && player.getSkillCustomValue(33121214) == null) {
                                 mischief = SkillFactory.getSkill(33121214).getEffect(player.getSkillLevel(33121214));
                                 monsters = new ArrayList();
                                 attackk = new ArrayList();
                                 i = 0;
                                 var25 = attack.allDamage.iterator();

                                 while(var25.hasNext()) {
                                    AttackPair oned1 = (AttackPair)var25.next();
                                    monsters.add(oned1.objectId);
                                 }

                                 List<MapleMapObject> objs = player.getMap().getMapObjectsInRange(player.getTruePosition(), 400000.0D, Arrays.asList(MapleMapObjectType.MONSTER));
                                 var91 = player.getMap().getMapObjectsInRange(player.getTruePosition(), 400000.0D, Arrays.asList(MapleMapObjectType.MONSTER)).iterator();

                                 label4222:
                                 while(true) {
                                    MapleMonster mob;
                                    do {
                                       if (!var91.hasNext()) {
                                          break label4222;
                                       }

                                       MapleMapObject m = (MapleMapObject)var91.next();
                                       mob = (MapleMonster)m;
                                    } while(mob == null);

                                    boolean attacked = true;
                                    Iterator var135 = monsters.iterator();

                                    while(var135.hasNext()) {
                                       Integer a2 = (Integer)var135.next();
                                       if (a2 == mob.getObjectId()) {
                                          attacked = false;
                                          break;
                                       }
                                    }

                                    if (attacked) {
                                       attackk.add(mob);
                                       ++i;
                                       if (i >= mischief.getQ()) {
                                          break;
                                       }
                                    }
                                 }

                                 if (!attackk.isEmpty()) {
                                    var91 = attackk.iterator();

                                    while(var91.hasNext()) {
                                       MapleMonster m = (MapleMonster)var91.next();
                                       player.getClient().getSession().writeAndFlush(CField.rangeAttack(attack.skill, Arrays.asList(new RangeAttack(33121019, m.getPosition(), 1, 0, 1))));
                                    }

                                    player.setSkillCustomInfo(attack.skill, 0L, (long)(mischief.getY() * 1000));
                                 }
                              }
                           } else if (GameConstants.isMichael(player.getJob())) {
                              if (player.skillisCooling(400011032) && player.getSkillCustomValue(400011033) == null) {
                                 sungi_skills = new ArrayList();
                                 player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(400011033, sungi_skills, true, 0));
                                 player.setSkillCustomInfo(400011033, 0L, 5000L);
                              }
                           } else if (GameConstants.isEunWol(player.getJob())) {
                              if (player.getSkillLevel(20050285) > 0) {
                                 Exceed = SkillFactory.getSkill(20050285).getEffect(1);
                                 if (!GameConstants.is_forceAtom_attack_skill(attack.skill)) {
                                    player.addHP(player.getStat().getCurrentMaxHp() / 100L * (long)Exceed.getX());
                                 }
                              }

                              if (player.getBuffedValue(25121133) && monster != null) {
                                 Exceed = SkillFactory.getSkill(25121133).getEffect(1);
                                 if (Exceed.makeChanceResult()) {
                                    Item toDrop2 = new Item(2434851, (short)0, (short)1, 0);
                                    monster.getMap().spawnItemDrop(monster, player, toDrop2, new Point(monster.getTruePosition().x, monster.getTruePosition().y), true, false);
                                 }
                              }
                           } else {
                              ForceAtom forceAtom;
                              if (GameConstants.isIllium(player.getJob())) {
                                 if (attack.skill == 152110004) {
                                    sungi_skills = new ArrayList();
                                    atom = new MapleAtom(false, player.getObjectId(), 38, true, 152120016, player.getPosition().x, player.getPosition().y);

                                    for(i = 0; i < 3; ++i) {
                                       sungi_skills.add(monster.getObjectId());
                                       forceAtom = new ForceAtom(2, Randomizer.rand(40, 48), Randomizer.rand(5, 6), Randomizer.rand(14, 256), 0);
                                       atom.addForceAtom(forceAtom);
                                    }

                                    atom.setDwTargets(sungi_skills);
                                    player.getMap().spawnMapleAtom(atom);
                                 }
                              } else if (GameConstants.isKadena(player.getJob())) {
                                 if (attack.skill == 64121020 && player.getSkillLevel(400041074) > 0) {
                                    HashMap<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
                                    Exceed = SkillFactory.getSkill(400041074).getEffect(player.getSkillLevel(400041074));
                                    player.setSkillCustomInfo(64121020, player.getSkillCustomValue0(64121020) + 1L, 0L);
                                    if (player.getSkillCustomValue0(64121020) > (long)Exceed.getW()) {
                                       player.setSkillCustomInfo(64121020, (long)Exceed.getW(), 0L);
                                    }

                                    statups.put(SecondaryStat.WeaponVarietyFinale, new Pair((int)player.getSkillCustomValue0(400041074), 0));
                                    player.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, Exceed, player));
                                    if (player.getSkillCustomValue0(400041074) > 0L && player.getSkillCustomValue0(64121020) >= (long)Exceed.getW()) {
                                       player.removeSkillCustomInfo(64121020);
                                       PlayerHandler.Vmatrixstackbuff(player.getClient(), true, (LittleEndianAccessor)null);
                                       player.getClient().getSession().writeAndFlush(CField.rangeAttack(400041074, Arrays.asList(new RangeAttack(400041074, monster != null ? monster.getPosition() : attack.position, 0, 0, 5))));
                                    }
                                 }
                              } else if (GameConstants.isYeti(player.getJob())) {
                                 if (monster != null) {
                                    if (player.getBuffedValue(135001015) && player.getSkillCustomValue(135001015) == null && SkillFactory.getSkill(135001015).getSkillList().contains(attack.skill)) {
                                       sungi_skills = new ArrayList();
                                       atom = new MapleAtom(false, player.getObjectId(), 60, true, 135002015, player.getPosition().x, player.getPosition().y);
                                       var73 = player.getMap().getAllMonster().iterator();

                                       while(var73.hasNext()) {
                                          MapleMonster mob = (MapleMonster)var73.next();
                                          sungi_skills.add(mob.getObjectId());
                                       }

                                       for(i = 0; i < 3; ++i) {
                                          forceAtom = new ForceAtom(1, 42 + i, 3, Randomizer.rand(59, 131), 0);
                                          atom.addForceAtom(forceAtom);
                                          player.getYetiGauge(135001015, 0);
                                       }

                                       atom.setDwTargets(sungi_skills);
                                       player.getMap().spawnMapleAtom(atom);
                                       player.setSkillCustomInfo(135001015, 0L, (long)(player.getBuffedEffect(135001015).getY() * 1000));
                                    }

                                    player.getYetiGauge(attack.skill, monster.getStats().isBoss() ? 2 : 0);
                                 }

                                 if (attack.skill == 135001007 || attack.skill == 135001010) {
                                    PlayerHandler.Vmatrixstackbuff(player.getClient(), true, (LittleEndianAccessor)null);
                                 }
                              } else if (GameConstants.isZero(player.getJob())) {
                                 if (player.getSkillLevel(101110205) > 0 && attack.targets > 0 && player.getGender() == 0 && Randomizer.isSuccess((mischief = SkillFactory.getSkill(101110205).getEffect(101110205)).getY())) {
                                    player.getClient().send(CField.getTpAdd(20, mischief.getZ()));
                                    player.getMap().broadcastMessage(player, CField.EffectPacket.showEffect(player, 0, 101110205, 4, 0, 0, (byte)(player.isFacingLeft() ? 1 : 0), false, player.getTruePosition(), (String)null, (Item)null), false);
                                 }
                              } else if (GameConstants.isPinkBean(player.getJob()) && monster != null && player.getSkillLevel(131000016) > 0 && player.getSkillCustomValue(131000016) == null && attack.skill != 131000016 && Randomizer.isSuccess(50)) {
                                 mischief = SkillFactory.getSkill(131003016).getEffect(1);
                                 monsters = new ArrayList();
                                 MapleAtom atom = new MapleAtom(false, player.getObjectId(), 65, true, 131003016, player.getPosition().x, player.getPosition().y);
                                 var110 = player.getMap().getAllMonster().iterator();

                                 while(var110.hasNext()) {
                                    mob = (MapleMonster)var110.next();
                                    if (mischief.calculateBoundingBox(attack.position, (attack.facingleft >>> 4 & 15) == 0).contains(mob.getPosition())) {
                                       monsters.add(mob.getObjectId());
                                    }
                                 }

                                 for(i = 0; i < 4; ++i) {
                                    ForceAtom forceAtom = new ForceAtom(1, Randomizer.rand(40, 44), Randomizer.rand(3, 4), Randomizer.rand(25, 345), 0);
                                    forceAtom.setnAttackCount(15 + i);
                                    atom.addForceAtom(forceAtom);
                                 }

                                 atom.setDwTargets(monsters);
                                 player.getMap().spawnMapleAtom(atom);
                                 player.setSkillCustomInfo(131000016, 0L, 10000L);
                              }
                           }

                           if (player.getBuffedValue(13121017) && attack.skill != 13121017 && !GameConstants.isTryFling(attack.skill) && attack.targets > 0 && Randomizer.isSuccess(30)) {
                              atom = new MapleAtom(false, player.getId(), 8, true, 13121017, player.getTruePosition().x, player.getTruePosition().y);
                              atom.setDwFirstTargetId(0);
                              ft = new ForceAtom(1, 1, Randomizer.rand(5, 7), 270, 66);
                              ft.setnStartY(ft.getnStartY() + Randomizer.rand(-110, 110));
                              atom.addForceAtom(ft);
                              player.getMap().spawnMapleAtom(atom);
                           }

                           if (attack.skill == 155001000) {
                              SkillFactory.getSkill(155001001).getEffect(attack.skilllevel).applyTo(player, false);
                           }

                           if (attack.skill == 155101002) {
                              SkillFactory.getSkill(155101003).getEffect(attack.skilllevel).applyTo(player, false);
                           }

                           if (attack.skill == 155111003) {
                              SkillFactory.getSkill(155111005).getEffect(attack.skilllevel).applyTo(player, false);
                           }

                           if (attack.skill == 155121003) {
                              SkillFactory.getSkill(155121005).getEffect(attack.skilllevel).applyTo(player, false);
                           }

                           MapleMist mist;
                           if (GameConstants.isHoyeong(player.getJob())) {
                              player.giveHoyoungGauge(attack.skill);
                              if (player.getBuffedValue(SecondaryStat.ButterflyDream) != null && SkillFactory.getSkill(164121007).getSkillList().contains(attack.skill) && player.getSkillCustomValue(164121007) == null) {
                                 player.getMap().broadcastMessage(SkillPacket.권술호접지몽(player));
                                 player.setSkillCustomInfo(164121007, 0L, 1000L);
                              }

                              if (player.getBuffedValue(400041052) && attack.targets != 0) {
                                 player.setInfinity((byte)(player.getInfinity() + 1));
                                 if (player.getInfinity() == 12) {
                                    var58 = player.getSummons().iterator();

                                    while(var58.hasNext()) {
                                       s = (MapleSummon)var58.next();
                                       if (s.getSkill() == 400041052) {
                                          player.setInfinity((byte)0);
                                          player.getClient().getSession().writeAndFlush(CField.SummonPacket.DeathAttack(s, Randomizer.rand(8, 10)));
                                          break;
                                       }
                                    }
                                 }
                              }
                           } else if (GameConstants.isArk(player.getJob())) {
                              if (attack.targets > 0 && (attack.skill == 155001100 || attack.skill == 155101100 || attack.skill == 155101101 || attack.skill == 155101112 || attack.skill == 155111102 || attack.skill == 155121102 || attack.skill == 155111111 || attack.skill == 155101212 || attack.skill == 155111211 || attack.skill == 155101214 || attack.skill == 155121215)) {
                                 player.addSpell(attack.skill);
                              }

                              if (player.getSkillCustomValue0(400051080) > 0L) {
                                 if (attack.skill == 400051080) {
                                    player.removeSkillCustomInfo(attack.skill);
                                 }

                                 if (SkillFactory.getSkill(400051080).getSkillList().contains(attack.skill)) {
                                    player.getClient().send(CField.getEarlySkillActive(600));
                                 } else if (SkillFactory.getSkill(400051080).getSkillList2().contains(attack.skill)) {
                                    player.getClient().send(CField.getEarlySkillActive(180));
                                 }
                              }

                              if (player.skillisCooling(400051047) || player.skillisCooling(400051048)) {
                                 mischief = SkillFactory.getSkill(400051047).getEffect(player.getSkillLevel(400051047));
                                 if (player.skillisCooling(400051047)) {
                                    if (SkillFactory.getSkill(400051047).getSkillList().contains(attack.skill) && !player.getWeaponChanges().contains(attack.skill)) {
                                       player.getWeaponChanges().add(attack.skill);
                                       player.changeCooldown(400051047, -(mischief.getX() * 1000));
                                    }
                                 } else if (player.skillisCooling(400051048) && SkillFactory.getSkill(400051048).getSkillList().contains(attack.skill) && !player.getWeaponChanges2().contains(attack.skill)) {
                                    player.getWeaponChanges2().add(attack.skill);
                                    player.changeCooldown(400051048, -(mischief.getX() * 1000));
                                 }
                              }

                              if (attack.isLink) {
                                 if (player.getBuffedValue(SecondaryStat.FightJazz) == null) {
                                    player.setSkillCustomInfo(155120015, 0L, 0L);
                                 }

                                 if (player.getSkillCustomValue0(155120015) <= 2L) {
                                    player.setSkillCustomInfo(155120015, player.getSkillCustomValue0(155120015) + 1L, 0L);
                                 }

                                 SkillFactory.getSkill(155120014).getEffect(player.getSkillLevel(155120014)).applyTo(player);
                              }

                              if (attack.skill != 155101100 && attack.skill != 155101101) {
                                 if (attack.skill == 155101112) {
                                    player.getClient().getSession().writeAndFlush(CField.rangeAttack(attack.skill, Arrays.asList(new RangeAttack(155101015, attack.position, 0, 0, 1))));
                                 } else if (attack.skill == 155121102) {
                                    player.getClient().getSession().writeAndFlush(CField.rangeAttack(attack.skill, Arrays.asList(new RangeAttack(155121002, attack.position, 0, 0, 1))));
                                 }
                              } else {
                                 player.getClient().getSession().writeAndFlush(CField.rangeAttack(attack.skill, Arrays.asList(new RangeAttack(155101013, attack.position, 0, 0, 1))));
                              }

                              if (attack.skill == 155121003 && monster != null) {
                                 mischief = SkillFactory.getSkill(155121004).getEffect(GameConstants.getLinkedSkill(155121004));
                                 mist = new MapleMist(mischief.calculateBoundingBox(monster.getTruePosition(), player.isFacingLeft()), player, mischief, 3000, (byte)(player.isFacingLeft() ? 1 : 0));
                                 mist.setDelay(0);
                                 player.getMap().spawnMist(mist, false);
                              }
                           }

                           if (player.getSkillLevel(400041063) > 0 && attack.targets > 0 && !player.skillisCooling(400041067)) {
                              sungi_skills = new ArrayList();
                              Point pos = null;
                              Point pos1 = null;
                              var110 = player.getMap().getAllMonster().iterator();

                              while(var110.hasNext()) {
                                 mob = (MapleMonster)var110.next();
                                 if (mob.isAlive() && player.getPosition().x + 500 >= mob.getPosition().x && player.getPosition().x - 500 <= mob.getPosition().x && player.getPosition().y + 300 >= mob.getPosition().y && player.getPosition().y - 300 <= mob.getPosition().y) {
                                    if (pos == null) {
                                       pos = mob.getPosition();
                                    } else if (pos1 == null) {
                                       pos1 = mob.getPosition();
                                       break;
                                    }
                                 }
                              }

                              if (pos == null) {
                                 pos = player.getPosition();
                              } else if (pos1 == null) {
                                 pos1 = pos;
                              }

                              Integer[] skills = new Integer[]{164001000, 164001002, 164101000, 164111000, 164111003, 164111008, 164121000, 164121003, 164121005};
                              if (Arrays.asList(skills).contains(attack.skill)) {
                                 sungi = SkillFactory.getSkill(400041063).getEffect(player.getSkillLevel(400041063));
                                 if (!player.useChun) {
                                    sungi_skills.add(400041064);
                                 }

                                 if (!player.useJi) {
                                    sungi_skills.add(400041065);
                                 }

                                 if (!player.useIn) {
                                    sungi_skills.add(400041066);
                                 }

                                 Collections.addAll(sungi_skills, new Integer[0]);
                                 Collections.shuffle(sungi_skills);
                                 if (player.getBuffedEffect(SecondaryStat.SageElementalClone) != null && System.currentTimeMillis() - player.lastSungiAttackTime >= (long)(sungi.getV2() * 1000)) {
                                    if (!sungi_skills.isEmpty()) {
                                       player.lastSungiAttackTime = System.currentTimeMillis();
                                       i = 0;

                                       for(Iterator m = sungi_skills.iterator(); m.hasNext(); ++i) {
                                          sungi_skill = (Integer)m.next();
                                          player.getClient().getSession().writeAndFlush(CField.rangeAttack(sungi_skill, Arrays.asList(new RangeAttack(sungi_skill, i == 0 ? pos : pos1, 1, 0, 1))));
                                       }

                                       player.addCooldown(400041067, System.currentTimeMillis(), 2000L);
                                       player.getClient().getSession().writeAndFlush(CField.skillCooldown(400041067, 2000));
                                    }
                                 } else if (player.getBuffedEffect(SecondaryStat.SageElementalClone) == null && System.currentTimeMillis() - player.lastSungiAttackTime >= (long)(sungi.getQ() * 1000)) {
                                    int i = 0;
                                    if (!sungi_skills.isEmpty()) {
                                       player.lastSungiAttackTime = System.currentTimeMillis();
                                       sungi_skill = (Integer)sungi_skills.get(Randomizer.nextInt(sungi_skills.size()));
                                       player.getClient().getSession().writeAndFlush(CField.rangeAttack(sungi_skill, Arrays.asList(new RangeAttack(sungi_skill, i == 0 ? pos : pos1, 1, 0, 1))));
                                       i = i + 1;
                                    }

                                    player.addCooldown(400041067, System.currentTimeMillis(), 5000L);
                                    player.getClient().getSession().writeAndFlush(CField.skillCooldown(400041067, 5000));
                                 }
                              }
                           }

                           var58 = player.getMap().getAllMistsThreadsafe().iterator();

                           while(var58.hasNext()) {
                              mist = (MapleMist)var58.next();
                              if (mist.getSource() != null && mist.getSourceSkill().getId() == attack.skill) {
                                 return;
                              }
                           }

                           if (attack.targets > 0 && player.getSkillCustomValue0(400031053) > 0L && player.getBuffedValue(400031053) && player.getSkillCustomValue(400031054) == null && !GameConstants.is_forceAtom_attack_skill(attack.skill)) {
                              mischief = player.getBuffedEffect(SecondaryStat.SilhouetteMirage);
                              atom = new MapleAtom(false, player.getId(), 56, true, 400031054, player.getTruePosition().x, player.getTruePosition().y);
                              player.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(player, 0, 400031053, 10, 0, 0, (byte)(player.isFacingLeft() ? 1 : 0), true, player.getPosition(), (String)null, (Item)null));
                              player.getMap().broadcastMessage(player, CField.EffectPacket.showEffect(player, 0, 400031053, 10, 0, 0, (byte)(player.isFacingLeft() ? 1 : 0), false, player.getPosition(), (String)null, (Item)null), false);

                              for(i = 0; i < mischief.getBulletCount(); ++i) {
                                 atom.addForceAtom(new ForceAtom(3, Randomizer.rand(21, 29), Randomizer.rand(15, 16), Randomizer.rand(170, 172), (short)(90 + i * 210)));
                              }

                              player.setSkillCustomInfo(400031054, 0L, (long)((int)player.getBuffedEffect(400031053).getT() * 1000));
                              atom.setDwFirstTargetId(0);
                              player.getMap().spawnMapleAtom(atom);
                           }

                           if (attack.skill == 64001009 || attack.skill == 64001010 || attack.skill == 64001011 || attack.skill == 1100012 || attack.skill == 3111010 || attack.skill == 3211010) {
                              if (attack.skill != 1100012 && attack.skill != 3111010 && attack.skill != 3211010) {
                                 player.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(player, attack.skill, attack.skill, 1, 0, 0, (byte)((attack.facingleft >>> 4 & 15) == 8 ? 1 : 0), true, attack.chain, (String)null, (Item)null));
                                 player.getMap().broadcastMessage(player, CField.EffectPacket.showEffect(player, attack.skill, attack.skill, 1, 0, 0, (byte)((attack.facingleft >>> 4 & 15) == 8 ? 1 : 0), false, attack.chain, (String)null, (Item)null), false);
                              } else {
                                 player.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(player, attack.skill, attack.skill, 1, 0, monster != null ? monster.getObjectId() : 0, (byte)((attack.facingleft >>> 4 & 15) == 8 ? 1 : 0), true, monster != null ? monster.getPosition() : attack.position, (String)null, (Item)null));
                                 player.getMap().broadcastMessage(player, CField.EffectPacket.showEffect(player, attack.skill, attack.skill, 1, 0, monster != null ? monster.getObjectId() : 0, (byte)((attack.facingleft >>> 4 & 15) == 8 ? 1 : 0), false, monster != null ? monster.getPosition() : attack.position, (String)null, (Item)null), false);
                              }
                           }

                           if (totDamage > 0L && player.getBuffedValue(400031047) && !player.getBuffedValue(400031048)) {
                              SkillFactory.getSkill(400031048).getEffect(player.getSkillLevel(400031047)).applyTo(player, false);
                           }

                           if (attack.skill == 400021092 && player.getBuffedEffect(400021092) != null) {
                              mischief = player.getBuffedEffect(400021092);
                              if (player.getSkillCustomValue0(400021093) < (long)mischief.getZ()) {
                                 player.addSkillCustomInfo(400021093, 1L);
                              }

                              HashMap<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
                              statups.put(SecondaryStat.SalamanderMischief, new Pair((int)player.getSkillCustomValue0(400021093), (int)player.getBuffLimit(400021092)));
                              player.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, mischief, player));
                           }

                           if (attack.targets > 0 && (player.getBuffedValue(400031049) || player.getBuffedValue(400031051)) && player.getSkillCustomValue(400031049) == null) {
                              var58 = player.getMap().getAllSummonsThreadsafe().iterator();

                              label3924:
                              while(true) {
                                 do {
                                    do {
                                       if (!var58.hasNext()) {
                                          break label3924;
                                       }

                                       s = (MapleSummon)var58.next();
                                    } while(s.getOwner().getId() != player.getId());
                                 } while(s.getSkill() != 400031049 && s.getSkill() != 400031051);

                                 if (s != null && attack.skill != 400031049 && attack.skill != 400031050 && attack.skill != 400031051) {
                                    player.getClient().getSession().writeAndFlush(CField.SummonPacket.summonRangeAttack(s, s.getSkill()));
                                    player.setSkillCustomInfo(400031049, 0L, s.getSkill() == 400031051 ? 5000L : 1200L);
                                 }
                              }
                           }

                           if (GameConstants.isNightLord(player.getJob()) && !GameConstants.is_forceAtom_attack_skill(attack.skill) && attack.skill < 400001001 && Randomizer.isSuccess(SkillFactory.getSkill(4110012).getEffect(player.getSkillLevel(4110012)).getProp())) {
                              player.getClient().send(CField.getExpertThrow());
                              player.setSkillCustomInfo(4110012, 1L, 0L);
                           }

                           if (player.getBuffedValue(11101031) && attack.summonattack == 0) {
                              if (GameConstants.isPollingmoonAttackskill(attack.skill)) {
                                 SkillFactory.getSkill(11121011).getEffect(20).applyTo(player);
                                 if (player.getSkillLevel(400011048) > 0 && player.skillisCooling(400011048) && attack.targets > 0) {
                                    player.changeCooldown(400011048, -300);
                                 }
                              } else if (GameConstants.isRisingsunAttackskill(attack.skill)) {
                                 SkillFactory.getSkill(11121012).getEffect(20).applyTo(player);
                                 if (player.getSkillLevel(400011048) > 0 && player.skillisCooling(400011048) && attack.targets > 0) {
                                    player.changeCooldown(400011048, -300);
                                 }
                              }
                           }

                           if (multikill > 0) {
                              player.CombokillHandler(monster, 1, multikill);
                           }

                           return;
                        }

                        oned = var25.next();
                        monster = map.getMonsterByOid(((AttackPair)oned).objectId);
                     } while(monster == null);
                  } while(monster.getLinkCID() > 0);

                  totDamageToOneMonster = 0L;
                  hpMob = monster.getMobMaxHp();
                  monsterstats = monster.getStats();
                  fixeddmg = (long)monsterstats.getFixedDamage();
               } while(monster.getId() >= 9833070 && monster.getId() <= 9833074);

               Iterator var31 = ((AttackPair)oned).attack.iterator();

               while(var31.hasNext()) {
                  Pair<Long, Boolean> eachde = (Pair)var31.next();
                  long eachd = (Long)eachde.left;
                  if (fixeddmg != -1L) {
                     eachd = monsterstats.getOnlyNoramlAttack() ? (attack.skill != 0 ? 0L : fixeddmg) : fixeddmg;
                  }

                  totDamageToOneMonster += eachd;
                  player.checkSpecialCoreSkills("attackCount", monster.getObjectId(), effect);
                  player.checkSpecialCoreSkills("attackCountMob", monster.getObjectId(), effect);
               }

               totDamage += totDamageToOneMonster;
               if (!player.gethottimebossattackcheck()) {
                  player.sethottimebossattackcheck(true);
               }

               if (monster.getId() != 8900002 && monster.getId() != 8900102) {
                  player.checkMonsterAggro(monster);
               }

               if (attack.skill != 0 && !SkillFactory.getSkill(attack.skill).isChainAttack() && !effect.isMist() && effect.getSourceId() != 400021030 && !GameConstants.isLinkedSkill(attack.skill) && !GameConstants.isNoApplySkill(attack.skill) && !GameConstants.isNoDelaySkill(attack.skill) && !monster.getStats().isBoss() && player.getTruePosition().distanceSq(monster.getTruePosition()) > GameConstants.getAttackRange(effect, player.getStat().defRange)) {
                  player.dropMessageGM(-5, "타겟이 범위를 벗어났습니다.");
               }

               int count;
               int attackid;
               if (monster != null && player.getBuffedValue(SecondaryStat.PickPocket) != null) {
                  SecondaryStatEffect eff = player.getBuffedEffect(SecondaryStat.PickPocket);
                  switch(attack.skill) {
                  case 0:
                  case 4001334:
                  case 4201004:
                  case 4201005:
                  case 4201012:
                  case 4211002:
                  case 4211004:
                  case 4211011:
                  case 4221007:
                  case 4221010:
                  case 4221014:
                  case 4221016:
                  case 4221017:
                  case 4221052:
                  case 400041002:
                  case 400041003:
                  case 400041004:
                  case 400041005:
                  case 400041025:
                  case 400041026:
                  case 400041027:
                  case 400041039:
                  case 400041069:
                     skill = SkillFactory.getSkill(4211006).getEffect(player.getSkillLevel(4211006)).getBulletCount();
                     i = eff.getProp();
                     count = 0;
                     if (player.getSkillLevel(4220045) > 0) {
                        i += SkillFactory.getSkill(4220045).getEffect(player.getSkillLevel(4220045)).getProp();
                        skill += SkillFactory.getSkill(4220045).getEffect(player.getSkillLevel(4220045)).getBulletCount();
                     }

                     if (attack.skill == 4221007) {
                        i /= 2;
                     }

                     i = 0;

                     for(; i < attack.hits; ++i) {
                        if (Randomizer.isSuccess(i)) {
                           ++count;
                        }
                     }

                     for(i = 0; i < count; ++i) {
                        Point pos = new Point(monster.getTruePosition().x, monster.getTruePosition().y);
                        int delay = 208;
                        i2 = 120 * i;
                        y = delay + i2;
                        if (count % 2 == 0) {
                           attackid = count / 2;
                           if (i < attackid) {
                              pos.x -= 18 * (attackid - i);
                           } else if (i >= attackid) {
                              pos.x += 18 * (i - attackid);
                           }
                        } else {
                           attackid = count / 2;
                           if (i < attackid) {
                              pos.x -= 18 * (attackid - i);
                           } else if (i > attackid) {
                              pos.x += 18 * (i - attackid);
                           }
                        }

                        if (player.getPickPocket().size() < skill) {
                           player.getMap().spawnMesoDrop(1, player.getMap().calcDropPos(pos, monster.getTruePosition()), monster, player, false, (byte)0, y);
                           player.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(eff.getStatups(), eff, player));
                        }
                     }
                  }
               }

               Integer[] 격투스킬 = new Integer[]{5101012, 5111002, 5121016};
               SecondaryStatEffect magicWreck;
               if (player.getBuffedValue(SecondaryStat.SeaSerpent) != null) {
                  magicWreck = SkillFactory.getSkill(5111017).getEffect(1);
                  i = 0;
                  count = magicWreck.getU();
                  if (Arrays.asList(격투스킬).contains(attack.skill)) {
                     if (player.getSkillLevel(5110016) > 0) {
                        i = 5111021;
                        if (player.getSkillLevel(5120029) > 0) {
                           i = 5121023;
                        }
                     }

                     if (!player.skillisCooling(i) && !player.getBuffedValue(5110020)) {
                        player.getClient().getSession().writeAndFlush(CField.rangeAttack(5101017, Arrays.asList(new RangeAttack(i, player.getPosition(), (attack.facingleft >>> 4 & 15) == 0 ? 0 : 1, 0, 1))));
                        if (player.서펜트스톤 < count) {
                           ++player.서펜트스톤;
                           magicWreck.applyTo(player, false, false);
                        }
                     } else if (player.getBuffedValue(5110020)) {
                        player.cancelEffectFromBuffStat(SecondaryStat.IndieSummon, 5110020);
                        player.getClient().getSession().writeAndFlush(CField.rangeAttack(5101017, Arrays.asList(new RangeAttack(5121023, player.getPosition(), (attack.facingleft >>> 4 & 15) == 0 ? 0 : 1, 0, 1))));
                        player.getClient().getSession().writeAndFlush(CField.rangeAttack(5110018, Arrays.asList(new RangeAttack(5111019, player.getPosition(), (attack.facingleft >>> 4 & 15) == 0 ? 0 : 1, 0, 1))));
                     }
                  } else if (attack.skill == 5121007) {
                     if (!player.skillisCooling(5121025) && !player.getBuffedValue(5110020)) {
                        player.getClient().getSession().writeAndFlush(CField.rangeAttack(5101017, Arrays.asList(new RangeAttack(5121025, player.getPosition(), (attack.facingleft >>> 4 & 15) == 0 ? 0 : 1, 0, 1))));
                        if (player.서펜트스톤 < count) {
                           ++player.서펜트스톤;
                           magicWreck.applyTo(player, false, false);
                        }
                     } else if (player.getBuffedValue(5110020)) {
                        player.cancelEffectFromBuffStat(SecondaryStat.IndieSummon, 5110020);
                        List<SecondAtom> atoms = new ArrayList();
                        atoms.add(new SecondAtom(36, player.getId(), 0, 5121027, 5000, 0, 15, new Point(monster.getPosition().x, monster.getPosition().y), Arrays.asList(0)));
                        player.spawnSecondAtom(atoms);
                     }
                  }
               }

               if (GameConstants.isViper(player.getJob()) && player.isAutoSkill(5111017) && player.서펜트스톤 >= 5) {
                  SecondaryStatEffect SerpentStone = SkillFactory.getSkill(5111017).getEffect(player.getSkillLevel(5111017));
                  player.서펜트스톤 = 0;
                  player.cancelEffect(SerpentStone);
                  SkillFactory.getSkill(5110020).getEffect(player.getSkillLevel(5110020)).applyTo(player, player.getPosition());
               }

               if (player.getSkillLevel(5120028) > 0) {
                  SkillFactory.getSkill(5120028).getEffect(player.getSkillLevel(5120028)).applyTo(player, false, false);
               }

               if (GameConstants.isAngelicBuster(player.getPlayer().getJob()) && attack.skill == 65121003) {
                  SkillFactory.getSkill(65121012).getEffect(player.getPlayer().getSkillLevel(65121012)).applyTo(player.getPlayer(), false, false);
               }

               if (GameConstants.isFPMage(player.getJob()) && attack.skill == 2121052) {
                  monsters = new ArrayList();
                  Iterator var152 = player.getMap().getAllMonster().iterator();

                  label4935:
                  while(true) {
                     MapleMonster mob;
                     do {
                        if (!var152.hasNext()) {
                           SecondAtom2 at = (SecondAtom2)SkillFactory.getSkill(2121052).getSecondAtoms().get(0);
                           i = 0;

                           while(true) {
                              if (i >= 2) {
                                 break label4935;
                              }

                              if (player.getSkillCustomValue0(2121052) < (long)effect.getX()) {
                                 player.addSkillCustomInfo(2121052, 1L);
                                 if (!monsters.isEmpty()) {
                                    at.setTarget(((MapleMonster)monsters.get(0)).getObjectId());
                                 }

                                 player.createSecondAtom(at, new Point(monster.getPosition().x + 150, monster.getPosition().y + 150), true);
                              }

                              ++i;
                           }
                        }

                        mob = (MapleMonster)var152.next();
                     } while(!effect.calculateBoundingBox(monster.getPosition(), true).contains(mob.getPosition()) && !effect.calculateBoundingBox(monster.getPosition(), false).contains(mob.getPosition()));

                     monsters.add(mob);
                  }
               }

               int probability = true;
               if (player.getSkillLevel(5100015) > 0) {
                  if (player.getBuffedEffect(SecondaryStat.EnergyCharged) == null) {
                     if (player.getSkillLevel(5120018) > 0) {
                        SkillFactory.getSkill(5120018).getEffect(player.getSkillLevel(5120018)).applyTo(player, false);
                     } else if (player.getSkillLevel(5110014) > 0) {
                        SkillFactory.getSkill(5110014).getEffect(player.getSkillLevel(5110014)).applyTo(player, false);
                     } else {
                        SkillFactory.getSkill(5100015).getEffect(player.getSkillLevel(5100015)).applyTo(player, false);
                     }
                  }

                  HashMap<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
                  concentration = player.getBuffedEffect(SecondaryStat.EnergyCharged);
                  count = concentration.getZ();
                  attackid = concentration.getX();
                  if (monster.getStats().isBoss()) {
                     attackid *= 2;
                  }

                  if (!player.energyCharge && attack.skill != 400051015) {
                     player.energy = Math.min(count, player.energy + attackid);
                     if (player.energy == count) {
                        player.energyCharge = true;
                     }
                  } else {
                     skillid = 0;
                     if (attack.skill == 400051015) {
                        skillid = effect.getX() / attack.targets;
                        if (monster.getStats().isBoss()) {
                           skillid = skillid * (100 - effect.getZ()) / 100;
                        }
                     } else if (attack.skill != 0) {
                        skillid = effect.getForceCon() / attack.targets;
                     }

                     player.energy = Math.max(0, player.energy - skillid);
                     if (player.energy == 0) {
                        player.energyCharge = false;
                     }
                  }

                  statups.put(SecondaryStat.EnergyCharged, new Pair(player.energy, 0));
                  player.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, player.getBuffedEffect(SecondaryStat.EnergyCharged), player));
                  player.getMap().broadcastMessage(player, CWvsContext.BuffPacket.giveForeignBuff(player, statups, player.getBuffedEffect(SecondaryStat.EnergyCharged)), false);
               }

               if (monster != null) {
                  monster.setNextSkill(260);
                  monster.setNextSkillLvl(3);
               }

               ArrayList WeponList;
               if (attack.skill == 2100001) {
                  WeponList = new ArrayList();
                  concentration = SkillFactory.getSkill(2100001).getEffect(player.getSkillLevel(2100001));
                  WeponList.add(new Pair(MonsterStatus.MS_Burned, new MonsterStatusEffect(attack.skill, concentration.getDOTTime(), (long)concentration.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L)));
                  monster.applyStatus(player.getClient(), WeponList, effect);
               }

               if (attack.skill == 1121015) {
                  WeponList = new ArrayList();
                  concentration = SkillFactory.getSkill(1121015).getEffect(player.getSkillLevel(1121015));
                  WeponList.add(new Pair(MonsterStatus.MS_Burned, new MonsterStatusEffect(attack.skill, 60000, (long)concentration.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L)));
                  monster.applyStatus(player.getClient(), WeponList, effect);
               }

               boolean givebuff;
               SecondaryStatEffect effect2;
               ForceAtom forceAtom;
               if (player.getBuffedValue(SecondaryStat.QuiverCatridge) != null && attack.skill != 400031021 && attack.skill != 95001000 && attack.skill != 3111013 && attack.skill != 3100010 && attack.skill != 400031000) {
                  boolean adquiver = player.getBuffedValue(SecondaryStat.AdvancedQuiver) != null;
                  debinrear = player.getBuffedValue(SecondaryStat.QuiverFullBurst) != null;
                  givebuff = false;
                  ceffect = SkillFactory.getSkill(3101009).getEffect(player.getSkillLevel(3101009));
                  shadowBite = SkillFactory.getSkill(3101009).getEffect(player.getSkillLevel(3101009));
                  if (player.getSkillLevel(3121016) > 0) {
                     shadowBite = SkillFactory.getSkill(3121016).getEffect(player.getSkillLevel(3121016));
                  }

                  if (debinrear) {
                     effect2 = SkillFactory.getSkill(3120022).getEffect(player.getSkillLevel(3120022));
                     eff = player.getBuffedEffect(SecondaryStat.QuiverFullBurst);
                     if (Randomizer.isSuccess(shadowBite.getW()) || player.getBuffedEffect(SecondaryStat.DebuffIncHp) == null) {
                        player.addHP((long)((int)((double)player.getStat().getCurrentMaxHp() * ((double)effect2.getX() / 100.0D))));
                     }

                     monster.applyStatus(player.getClient(), MonsterStatus.MS_Burned, new MonsterStatusEffect(shadowBite.getSourceId(), shadowBite.getDuration()), shadowBite.getDOT(), effect);
                     MapleAtom atom = new MapleAtom(false, player.getId(), 10, true, 3100010, player.getTruePosition().x, player.getTruePosition().y);
                     atom.setDwFirstTargetId(0);
                     ForceAtom forceAtom = new ForceAtom(0, Randomizer.rand(10, 20), Randomizer.rand(5, 10), Randomizer.rand(4, 301), (short)Randomizer.rand(20, 48));
                     atom.addForceAtom(forceAtom);
                     player.getMap().spawnMapleAtom(atom);
                     if (System.currentTimeMillis() - player.lastFireArrowTime >= 2000L) {
                        player.lastFireArrowTime = System.currentTimeMillis();
                        MapleAtom atom2 = new MapleAtom(false, player.getId(), 50, true, 400031029, monster.getTruePosition().x, monster.getTruePosition().y);

                        for(int i = 0; i < eff.getY(); ++i) {
                           atom2.addForceAtom(new ForceAtom(1, Randomizer.rand(30, 60), 10, Randomizer.nextBoolean() ? Randomizer.rand(10, 15) : Randomizer.rand(190, 195), (short)(i * 100)));
                        }

                        atom2.setDwFirstTargetId(0);
                        player.getMap().spawnMapleAtom(atom2);
                     }
                  } else {
                     switch(player.getQuiverType()) {
                     case 1:
                        if (!Randomizer.isSuccess(attack.skill == 400030002 ? shadowBite.getU() * 2 : shadowBite.getU())) {
                           break;
                        }

                        atom = new MapleAtom(false, player.getId(), 10, true, 3100010, player.getTruePosition().x, player.getTruePosition().y);
                        atom.setDwFirstTargetId(0);
                        forceAtom = new ForceAtom(0, Randomizer.rand(10, 20), Randomizer.rand(5, 10), Randomizer.rand(4, 301), (short)Randomizer.rand(20, 48));
                        atom.addForceAtom(forceAtom);
                        player.getMap().spawnMapleAtom(atom);
                     case 2:
                        effect2 = SkillFactory.getSkill(3120022).getEffect(player.getSkillLevel(3120022));
                        if (Randomizer.isSuccess(effect2.getW()) && player.getBuffedEffect(SecondaryStat.DebuffIncHp) == null) {
                           atom = new MapleAtom(false, player.getId(), 10, true, 3100010, player.getTruePosition().x, player.getTruePosition().y);
                           atom.setDwFirstTargetId(0);
                           forceAtom = new ForceAtom(0, Randomizer.rand(10, 20), Randomizer.rand(5, 10), Randomizer.rand(4, 301), (short)Randomizer.rand(20, 48));
                           atom.addForceAtom(forceAtom);
                           player.getMap().spawnMapleAtom(atom);
                           player.addHP((long)((int)((double)player.getStat().getCurrentMaxHp() * ((double)effect2.getX() / 100.0D))));
                        }
                     }
                  }

                  if (player.getQuiverType() == 0) {
                     player.setQuiverType((byte)1);
                  }

                  if (player.getRestArrow()[player.getQuiverType() - 1] == 0) {
                     if (player.getRestArrow()[0] == 0 && player.getRestArrow()[1] == 0) {
                        givebuff = true;
                     } else {
                        player.setQuiverType((byte)(player.getQuiverType() == 2 ? 1 : player.getQuiverType() + 1));
                        active = false;
                        int arrowcount1 = false;
                        y = player.getRestArrow()[0];
                        i2 = player.getRestArrow()[1];
                        player.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(player, 0, 3101009, 57, player.getQuiverType() - 1, 0, (byte)(player.isFacingLeft() ? 1 : 0), true, player.getPosition(), (String)null, (Item)null));
                        player.getMap().broadcastMessage(player, CField.EffectPacket.showEffect(player, 0, 3101009, 57, player.getQuiverType() - 1, 0, (byte)(player.isFacingLeft() ? 1 : 0), false, player.getPosition(), (String)null, (Item)null), false);
                     }
                  }

                  if (!adquiver && !debinrear) {
                     ceffect.applyTo(player, givebuff, false);
                  }
               }

               if (player.getBuffedValue(SecondaryStat.ShadowBatt) != null && (attack.skill == 14121001 || attack.skill == 14101028 || attack.skill == 14111020 || attack.skill == 14101020 || attack.skill == 14001020) && attack.targets > 0) {
                  concentration = SkillFactory.getSkill(14000027).getEffect(player.getSkillLevel(14001027));
                  count = 3;
                  attackid = concentration.getProp();
                  int[] var186 = new int[]{14100027, 14110029, 14120008};
                  y = var186.length;

                  for(i2 = 0; i2 < y; ++i2) {
                     fora = var186[i2];
                     if (player.getSkillLevel(fora) > 0) {
                        attackid += SkillFactory.getSkill(fora).getEffect(player.getSkillLevel(fora)).getProp();
                        count += SkillFactory.getSkill(fora).getEffect(player.getSkillLevel(fora)).getY();
                     }
                  }

                  mobList = new ArrayList();
                  if (player.getSummonsSize() > 0) {
                     Iterator var188 = player.getSummons().iterator();

                     while(var188.hasNext()) {
                        MapleSummon summon2 = (MapleSummon)var188.next();
                        if (summon2.getSkill() == 14000027) {
                           mobList.add(summon2);
                        }
                     }
                  }

                  if (player.battAttackCount <= -1) {
                     player.battAttackCount = 0;
                  }

                  ++player.battAttackCount;
                  if (mobList.size() > 0 && Randomizer.isSuccess(attackid)) {
                     MapleSummon deleted = (MapleSummon)mobList.get(Randomizer.nextInt(mobList.size()));
                     atom = new MapleAtom(false, player.getId(), 15, true, 14000028, deleted.getTruePosition().x, deleted.getTruePosition().y);
                     atom.setDwFirstTargetId(monster.getObjectId());
                     forceAtom = new ForceAtom(player.getSkillLevel(14120008) > 0 ? 2 : 1, 1, 5, Randomizer.rand(45, 90), 500);
                     atom.addForceAtom(forceAtom);
                     player.getMap().spawnMapleAtom(atom);
                     if (deleted != null) {
                        deleted.removeSummon(player.getMap(), false, false);
                        player.removeSummon(deleted);
                     }
                  }

                  if (player.battAttackCount == concentration.getZ()) {
                     player.battAttackCount = 0;
                     if (mobList.size() < count) {
                        concentration.applyTo(player, (Point)player.getPosition(), false, (int)60000);
                     }
                  }

                  int var10002 = mobList.size();
                  player.dropMessageGM(-8, "서몬 사이즈 : " + var10002 + " / 배트 제한 : " + count);
               }

               if ((GameConstants.isDarkAtackSkill(attack.skill) || attack.summonattack != 0) && attack.skill != 14000027 && attack.skill != 14000028 && attack.skill != 14000029) {
                  player.changeCooldown(14121003, -500);
               }

               if (attack.skill == 14000028) {
                  player.addHP(player.getStat().getCurrentMaxHp() / 100L);
               }

               if (GameConstants.isAngelicBuster(player.getJob())) {
                  MapleSummon s;
                  if (attack.skill != 400051011 && player.getBuffedValue(400051011) && attack.skill != 60011216 && (s = player.getSummon(400051011)) != null) {
                     MapleAtom atom = new MapleAtom(true, monster.getObjectId(), 29, true, 400051011, monster.getTruePosition().x, monster.getTruePosition().y);
                     atom.setDwUserOwner(player.getId());
                     atom.setDwFirstTargetId(monster.getObjectId());
                     atom.addForceAtom(new ForceAtom(Randomizer.rand(1, 3), Randomizer.rand(30, 50), Randomizer.rand(3, 10), Randomizer.rand(4, 301), 0));
                     atom.addForceAtom(new ForceAtom(Randomizer.rand(1, 3), Randomizer.rand(30, 50), Randomizer.rand(3, 10), Randomizer.rand(4, 301), 0));
                     player.getMap().spawnMapleAtom(atom);
                     player.setEnergyBurst(player.getEnergyBurst() + 1);
                     HashMap statups;
                     if (player.getEnergyBurst() == 50) {
                        player.setBuffedValue(SecondaryStat.EnergyBurst, 3);
                        statups = new HashMap();
                        statups.put(SecondaryStat.EnergyBurst, new Pair(player.getBuffedValue(SecondaryStat.EnergyBurst), (int)player.getBuffLimit(400051011)));
                        player.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, player.getBuffedEffect(SecondaryStat.EnergyBurst), player));
                        player.getClient().getSession().writeAndFlush(CField.SummonPacket.updateSummon(s, 15));
                     } else if (player.getEnergyBurst() == 25) {
                        player.setBuffedValue(SecondaryStat.EnergyBurst, 2);
                        statups = new HashMap();
                        statups.put(SecondaryStat.EnergyBurst, new Pair(player.getBuffedValue(SecondaryStat.EnergyBurst), (int)player.getBuffLimit(400051011)));
                        player.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, player.getBuffedEffect(SecondaryStat.EnergyBurst), player));
                        player.getClient().getSession().writeAndFlush(CField.SummonPacket.updateSummon(s, 14));
                     }
                  }

                  MapleSummon summon3;
                  if (player.getBuffedValue(400051046) && player.getSkillCustomValue(400051046) == null && (summon3 = player.getSummon(400051046)) != null) {
                     player.getClient().getSession().writeAndFlush(CField.SummonPacket.DeathAttack(summon3, Randomizer.rand(8, 9)));
                  }
               }

               if (GameConstants.isKadena(player.getJob())) {
                  WeponList = new ArrayList();
                  WeponList.add(new Pair(0, 64121002));
                  WeponList.add(new Pair(1, 64001002));
                  WeponList.add(new Pair(1, 64001013));
                  WeponList.add(new Pair(2, 64101002));
                  WeponList.add(new Pair(2, 64101008));
                  WeponList.add(new Pair(3, 64101001));
                  WeponList.add(new Pair(4, 64111002));
                  WeponList.add(new Pair(5, 64111003));
                  WeponList.add(new Pair(6, 64111004));
                  WeponList.add(new Pair(6, 64111012));
                  WeponList.add(new Pair(7, 64121021));
                  WeponList.add(new Pair(7, 64121022));
                  WeponList.add(new Pair(7, 64121023));
                  WeponList.add(new Pair(7, 64121024));
                  WeponList.add(new Pair(8, 64121003));
                  WeponList.add(new Pair(8, 64121011));
                  WeponList.add(new Pair(8, 64121016));
                  debinrear = false;
                  givebuff = false;
                  attackid = 0;
                  if (attack.skill == 64121002 || attack.skill == 64001002 || attack.skill == 64001013 || attack.skill == 64101002 || attack.skill == 64101008 || attack.skill == 64101001 || attack.skill == 64111002 || attack.skill == 64111003 || attack.skill == 64111004 || attack.skill == 64111012 || attack.skill == 64121021 || attack.skill == 64121022 || attack.skill == 64121023 || attack.skill == 64121024 || attack.skill == 64121003 || attack.skill == 64121011 || attack.skill == 64121016) {
                     Iterator var192 = WeponList.iterator();

                     while(var192.hasNext()) {
                        Pair info = (Pair)var192.next();
                        if (attack.skill == (Integer)info.getRight()) {
                           if (player.getBuffedEffect(SecondaryStat.WeaponVariety) == null) {
                              player.weaponChanges1.clear();
                              player.removeSkillCustomInfo(6412);
                           }

                           if ((Integer)info.left != 0 && !player.weaponChanges1.containsKey(info.left) && player.weaponChanges1.size() < 8) {
                              player.weaponChanges1.put((Integer)info.left, (Integer)info.right);
                              debinrear = true;
                              givebuff = true;
                           }

                           if (player.getSkillCustomValue0(6412) != (long)(Integer)info.getLeft()) {
                              givebuff = true;
                              debinrear = true;
                           }

                           if (player.weaponChanges1.size() == 1) {
                              debinrear = false;
                           }

                           if (givebuff) {
                              if (player.getSkillLevel(64120006) > 0) {
                                 SkillFactory.getSkill(64120006).getEffect(player.getSkillLevel(64120006)).applyTo(player, false);
                                 attackid = 64120006;
                              } else if (player.getSkillLevel(64110005) > 0) {
                                 SkillFactory.getSkill(64110005).getEffect(player.getSkillLevel(64110005)).applyTo(player, false);
                                 attackid = 64110005;
                              } else if (player.getSkillLevel(64100004) > 0) {
                                 SkillFactory.getSkill(64100004).getEffect(player.getSkillLevel(64100004)).applyTo(player, false);
                                 attackid = 64100004;
                              }
                           }

                           if (debinrear && System.currentTimeMillis() - player.lastBonusAttckTime > 500L) {
                              player.lastBonusAttckTime = System.currentTimeMillis();
                              player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(attackid == 64120006 ? 64121020 : (attackid == 64110005 ? 64111013 : 64101009), finalMobList, true, 0));
                           }

                           if (givebuff) {
                              player.setSkillCustomInfo(6412, (long)(Integer)info.getLeft(), 0L);
                           }
                        }
                     }
                  }

                  if (player.getBuffedValue(64121053) && attack.skill != 64121055 && player.getSkillCustomValue(64121055) == null) {
                     mobList = new ArrayList();
                     mobList.add(new Triple(monster.getObjectId(), 60, 0));
                     player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(64121055, mobList, false, 0));
                     player.setSkillCustomInfo(64121055, 0L, 100L);
                  }

                  if ((attack.skill == 64121002 || attack.skill == 64121052 || attack.skill == 64121012 || attack.skill == 400041036) && player.getSkillCustomValue(400441774) != null && player.getSkillCustomTime(400441774) > 0) {
                     effect2 = SkillFactory.getSkill(400041074).getEffect(player.getSkillLevel(400041074));
                     skillid = attack.skill == 400041036 ? effect2.getZ() * 1000 : effect2.getSubTime();
                     if (player.getSkillCustomTime(400441774) - skillid <= 0) {
                        player.setSkillCustomInfo(400441774, 0L, 5L);
                     } else {
                        player.setSkillCustomInfo(400441774, 0L, (long)(player.getSkillCustomTime(400441774) - skillid));
                     }
                  }
               }

               if (GameConstants.isFusionSkill(attack.skill) && attack.targets > 0 && player.getSkillCustomValue(22170070) == null) {
                  magicWreck = player.getSkillLevel(22170070) > 0 ? SkillFactory.getSkill(22170070).getEffect(player.getSkillLevel(22170070)) : SkillFactory.getSkill(22141017).getEffect(player.getSkillLevel(22141017));
                  if (player.getMap().getWrecks().size() < 15) {
                     i = Randomizer.rand(-100, 150);
                     count = Randomizer.rand(-50, 70);
                     MapleMagicWreck mw = new MapleMagicWreck(player, magicWreck.getSourceId(), new Point(monster.getTruePosition().x + i, monster.getTruePosition().y + count), 20000);
                     player.getMap().spawnMagicWreck(mw);
                     player.setSkillCustomInfo(22170070, 0L, player.getSkillLevel(22170070) > 0 ? 400L : 600L);
                  }
               }

               Integer[] skills = new Integer[]{3100001, 3100010, 3101009, 3111015, 3111016, 3120021};
               if (player.getBuffedValue(SecondaryStat.FlashMirage) == null || Arrays.asList(skills).contains(attack.skill)) {
                  break;
               }

               concentration = SkillFactory.getSkill(3111015).getEffect(player.getSkillLevel(3111015));
               count = concentration.getU();
               attackid = concentration.getW();
               if (player.getSkillLevel(3120021) > 0) {
                  count = SkillFactory.getSkill(3120021).getEffect(player.getSkillLevel(3120021)).getU() * 2;
                  attackid = SkillFactory.getSkill(3120021).getEffect(player.getSkillLevel(3120021)).getW();
               }

               if (player.플레시미라주스택 < count) {
                  if (attack.skill == 13120003) {
                     continue;
                  }

                  ++player.플레시미라주스택;
               } else if (player.플레시미라주스택 == count) {
                  player.플레시미라주스택 = 1;

                  for(skillid = 0; skillid < attackid; ++skillid) {
                     List<MapleMapObject> objs = player.getMap().getMapObjectsInRange(player.getTruePosition(), 500000.0D, Arrays.asList(MapleMapObjectType.MONSTER));
                     if (objs.size() > 0) {
                        statusz = new ArrayList();
                        Point pos = new Point(attack.attackPosition.x + Randomizer.rand(-250, 250), attack.attackPosition.y + Randomizer.rand(-250, 250));
                        statusz.add(new SecondAtom(39, player.getId(), 0, 3111016, 3000, 0, 6, pos, Arrays.asList(0)));
                        player.spawnSecondAtom(statusz);
                        player.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(player, 0, 3111015, 83, 0, 0, (byte)0, true, pos, (String)null, (Item)null));
                        player.getMap().broadcastMessage(player, CField.EffectPacket.showEffect(player, 0, 3111015, 83, 0, 0, (byte)0, false, pos, (String)null, (Item)null), false);
                     }
                  }
               }

               SkillFactory.getSkill(3111015).getEffect(player.getSkillLevel(3111015)).applyTo(player, false, false);
               break;
            }

            if (!player.getBuffedValue(15121054) && (attack.skill == 15111022 || attack.skill == 15120003) && player.lightning > 2 && player.getBuffedEffect(SecondaryStat.CygnusElementSkill, 15001022) != null) {
               concentration = SkillFactory.getSkill(attack.skill).getEffect(attack.skilllevel);
               concentration.applyTo(player, false);
               player.cancelEffectFromBuffStat(SecondaryStat.IgnoreTargetDEF, 15001022);
               player.cancelEffectFromBuffStat(SecondaryStat.IndiePmdR, 15001022);
            }

            if (player.getSkillLevel(3110001) > 0 && attack.skill != 95001000 && attack.skill != 3100010 && attack.skill != 400031029 && !GameConstants.is_forceAtom_attack_skill(attack.skill)) {
               SkillFactory.getSkill(3110001).getEffect(player.getSkillLevel(3110001)).applyTo(player, false, false);
            }

            if (player.getSkillLevel(3210001) > 0 && attack.skill != 95001000 && attack.skill != 3100010 && attack.skill != 400031029) {
               SkillFactory.getSkill(3210001).getEffect(player.getSkillLevel(3210001)).applyTo(player, false, false);
            }

            if (player.getSkillLevel(3110012) > 0 && attack.skill != 95001000 && attack.skill != 3100010 && attack.skill != 400031029) {
               concentration = SkillFactory.getSkill(3110012).getEffect(player.getSkillLevel(3110012));
               if (System.currentTimeMillis() - player.lastConcentrationTime >= (long)concentration.getY()) {
                  player.lastConcentrationTime = System.currentTimeMillis();
                  if (player.getConcentration() < 100) {
                     player.setConcentration((byte)(player.getConcentration() + concentration.getX()));
                  }

                  if (player.getBuffedValue(3110012) && player.getConcentration() < player.getSkillLevel(3110012) || !player.getBuffedValue(3110012)) {
                     concentration.applyTo(player, false, false);
                  }
               }
            }

            if (player.getJob() == 112 && (attack.skill == 1101011 || attack.skill == 1111010 || attack.skill == 1121008 || attack.skill == 1121015) && attack.targets > 0 && player.getBuffedValue(1121054) && player.발할라검격 >= 3) {
               for(i = 0; i < 3; ++i) {
                  MapleSummon summon4 = new MapleSummon(player, 1121055, attack.attackPosition, SummonMovementType.STATIONARY, (byte)0, 10000);
                  player.getMap().spawnSummon(summon4, 10000);
                  player.addSummon(summon4);
                  --player.발할라검격;
               }

               concentration = SkillFactory.getSkill(1121054).getEffect(player.getSkillLevel(1121054));
               HashMap<SecondaryStat, Pair<Integer, Integer>> statups3 = new HashMap();
               statups3.put(SecondaryStat.Stance, new Pair(100, (int)player.skillcool(1121054) - 90000));
               player.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups3, concentration, player));
            }

            ArrayList monsters;
            MapleAtom atom;
            if (attack.skill == 3301008 && attack.targets > 0) {
               atom = new MapleAtom(true, player.getId(), 58, true, 3301009, player.getTruePosition().x, player.getTruePosition().y);
               atom.setDwUserOwner(player.getId());
               monsters = new ArrayList();
               monsters.add(0);
               monsters.add(0);
               atom.addForceAtom(new ForceAtom(1, Randomizer.rand(21, 25), Randomizer.rand(2, 4), Randomizer.rand(17, 21), 120, player.getTruePosition()));
               atom.addForceAtom(new ForceAtom(1, Randomizer.rand(21, 25), Randomizer.rand(2, 4), Randomizer.rand(17, 21), 120, player.getTruePosition()));
               atom.setSearchX1(650);
               atom.setSearchY1(250);
               atom.setnDuration(2);
               atom.setSearchX(560);
               atom.setSearchY(2);
               atom.setDwTargets(monsters);
               player.getMap().spawnMapleAtom(atom);
            }

            if (attack.skill == 3321036 && attack.targets > 0 && Randomizer.isSuccess(30)) {
               atom = new MapleAtom(true, player.getId(), 58, true, 3321037, player.getTruePosition().x, player.getTruePosition().y);
               atom.setDwUserOwner(player.getId());
               monsters = new ArrayList();
               monsters.add(0);
               monsters.add(0);
               atom.addForceAtom(new ForceAtom(3, Randomizer.rand(21, 25), Randomizer.rand(2, 4), Randomizer.rand(17, 21), 120, new Point(monster.getTruePosition().x + Randomizer.rand(-150, 150), monster.getTruePosition().y + Randomizer.rand(-200, 100))));
               atom.addForceAtom(new ForceAtom(3, Randomizer.rand(21, 25), Randomizer.rand(2, 4), Randomizer.rand(17, 21), 120, new Point(monster.getTruePosition().x + Randomizer.rand(-150, 150), monster.getTruePosition().y + Randomizer.rand(-200, 100))));
               atom.setDwTargets(monsters);
               atom.setSearchX1(650);
               atom.setSearchY1(250);
               atom.setnDuration(2);
               atom.setSearchX(560);
               atom.setSearchY(2);
               player.getMap().spawnMapleAtom(atom);
            }

            if (GameConstants.isKaiser(player.getJob()) && attack.skill != 61120018 && attack.skill != 0 && !player.getBuffedValue(61111008) && !player.getBuffedValue(61120008) && !player.getBuffedValue(61121053)) {
               player.handleKaiserCombo(attack.skill);
            }
         } while(totDamageToOneMonster <= 0L && attack.skill != 1221011 && attack.skill != 21120006 && attack.skill != 164001001);

         if (GameConstants.isDemonSlayer(player.getJob()) && attack.skill != 31101002) {
            if (attack.skill != 31101002 && player.getSkillLevel(30010111) > 0) {
               if (Randomizer.isSuccess(1)) {
                  totDamageToOneMonster *= 2L;
                  player.addHP((long)((double)player.getStat().getCurrentMaxHp() * 0.05D));
               }

               if (monster.getHp() <= totDamageToOneMonster) {
                  player.handleForceGain(monster.getObjectId(), 30010111);
               }
            }

            player.handleForceGain(monster.getObjectId(), attack.skill, monster.getStats().isBoss() ? 1 : 0);
         }

         ArrayList statusz2;
         ArrayList statusz1;
         if (GameConstants.isPhantom(player.getJob()) && (player.getSkillLevel(24120002) > 0 || player.getSkillLevel(24100003) > 0)) {
            Skill noir = SkillFactory.getSkill(24120002);
            Skill blanc = SkillFactory.getSkill(24100003);
            ceffect = null;
            skillid = player.getTotalSkillLevel(noir);
            active = true;
            if (skillid > 0) {
               ceffect = noir.getEffect(skillid);
            } else if (player.getSkillLevel(blanc) > 0) {
               ceffect = blanc.getEffect(player.getTotalSkillLevel(blanc));
            } else {
               active = false;
            }

            if (attack.skill == 24120055 || attack.skill == noir.getId() || attack.skill == blanc.getId() || attack.skill == 24121011) {
               active = false;
            }

            if (attack.skill == 400041010) {
               active = true;
            }

            if (active) {
               if (player.getCardStack() < (skillid > 0 ? 40 : 20)) {
                  player.setCardStack((byte)(player.getCardStack() + 1));
                  player.getClient().getSession().writeAndFlush(CField.updateCardStack(false, player.getCardStack()));
               }

               atom = new MapleAtom(false, player.getId(), 1, true, skillid > 0 ? 24120002 : 24100003, player.getTruePosition().x, player.getTruePosition().y);
               atom.setDwFirstTargetId(monster.getObjectId());
               atom.addForceAtom(new ForceAtom(2, Randomizer.rand(15, 29), Randomizer.rand(7, 11), Randomizer.rand(0, 9), 0));
               player.getMap().spawnMapleAtom(atom);
            }

            if (skillid > 0 && SkillFactory.getSkill(24121011).getSkillList().contains(attack.skill) && player.getSkillCustomValue(24121011) == null) {
               eff = SkillFactory.getSkill(24121011).getEffect(player.getSkillLevel(24120002));
               statusz1 = new ArrayList();
               statusz2 = new ArrayList();
               int i = 0;
               Iterator var211 = attack.allDamage.iterator();

               while(var211.hasNext()) {
                  AttackPair oned1 = (AttackPair)var211.next();
                  statusz1.add(oned1.objectId);
               }

               var211 = player.getMap().getAllMonster().iterator();

               while(var211.hasNext()) {
                  Object mob = var211.next();
                  boolean attacked = true;
                  Iterator var46 = statusz1.iterator();

                  while(var46.hasNext()) {
                     Object a2 = var46.next();
                     if ((Integer)a2 == ((MapleMapObject)mob).getObjectId()) {
                        attacked = false;
                        break;
                     }
                  }

                  if (attacked && eff.calculateBoundingBox(player.getPosition(), (attack.facingleft >>> 4 & 15) == 0).contains(((MapleMapObject)mob).getPosition())) {
                     statusz2.add(((MapleMapObject)mob).getObjectId());
                     ++i;
                     if (i >= ceffect.getY()) {
                        break;
                     }
                  }
               }

               if (!statusz2.isEmpty()) {
                  MapleAtom atom6 = new MapleAtom(false, player.getId(), 73, true, 24121011, player.getTruePosition().x, player.getTruePosition().y);
                  atom6.setDwTargets(statusz2);
                  Iterator var224 = statusz2.iterator();

                  while(var224.hasNext()) {
                     Integer objectId = (Integer)var224.next();
                     atom6.addForceAtom(new ForceAtom(1, Randomizer.rand(15, 21), Randomizer.rand(7, 11), Randomizer.rand(0, 9), 0));
                  }

                  player.getMap().spawnMapleAtom(atom6);
                  player.setSkillCustomInfo(24121011, 0L, (long)(ceffect.getW() * 1000));
               }
            }

            if (player.getSkillLevel(400041040) > 0) {
               eff = SkillFactory.getSkill(400041040).getEffect(player.getSkillLevel(400041040));
               if (SkillFactory.getSkill(400041040).getSkillList().contains(attack.skill) || attack.skill / 10000 != 2400 && attack.skill / 10000 != 2410 && attack.skill / 10000 != 2411 && attack.skill / 10000 != 2412 && attack.skill < 400000000) {
                  if (attack.skill != 24001000 && attack.skill != 24111000) {
                     if (attack.skill != 24121000 && attack.skill != 24121005 && attack.skill != 400041055 && attack.skill != 400041056) {
                        player.addSkillCustomInfo(400341040, 1L);
                        if (player.getSkillCustomValue0(400341040) >= (long)eff.getW()) {
                           player.setMarkOfPhantomOid(monster.getObjectId());
                           player.removeSkillCustomInfo(400341040);
                           eff.applyTo(player, false);
                        }
                     } else {
                        player.setUltimateDriverCount(player.getUltimateDriverCount() + 1);
                        if (player.getUltimateDriverCount() >= eff.getY()) {
                           player.setMarkOfPhantomOid(monster.getObjectId());
                           player.setUltimateDriverCount(0);
                           eff.applyTo(player, false);
                        }
                     }
                  } else {
                     player.setMarkOfPhantomOid(monster.getObjectId());
                     eff.applyTo(player, false);
                  }
               }
            }
         }

         if (player.getSkillLevel(80002762) > 0) {
            if (player.getBuffedEffect(SecondaryStat.EmpiricalKnowledge) != null && player.empiricalKnowledge != null) {
               if (map.getMonsterByOid(player.empiricalKnowledge.getObjectId()) != null) {
                  if (monster.getObjectId() != player.empiricalKnowledge.getObjectId() && monster.getMobMaxHp() > player.empiricalKnowledge.getMobMaxHp()) {
                     player.empiricalStack = 0;
                     player.empiricalKnowledge = monster;
                  }
               } else {
                  player.empiricalStack = 0;
                  player.empiricalKnowledge = monster;
               }
            } else if (player.empiricalKnowledge != null) {
               if (monster.getMobMaxHp() > player.empiricalKnowledge.getMobMaxHp()) {
                  player.empiricalKnowledge = monster;
               }
            } else {
               player.empiricalKnowledge = monster;
            }
         }

         debinrear = false;
         SecondaryStatEffect markOf;
         if (player.getSkillLevel(101120207) > 0) {
            markOf = SkillFactory.getSkill(101120207).getEffect(player.getSkillLevel(101120207));
            if (player.getGender() == 0 && markOf.makeChanceResult()) {
               debinrear = true;
               player.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(player, 0, 101120207, 4, 0, 0, (byte)(player.isFacingLeft() ? 1 : 0), true, player.getTruePosition(), (String)null, (Item)null));
               player.getMap().broadcastMessage(player, CField.EffectPacket.showEffect(player, 0, 101120207, 4, 0, 0, (byte)(player.isFacingLeft() ? 1 : 0), false, player.getTruePosition(), (String)null, (Item)null), false);
               player.addHP(player.getStat().getCurrentMaxHp() / 100L * (long)markOf.getX());
            }
         }

         monster.damage(player, totDamageToOneMonster, true, attack.skill);
         markOf = player.getBuffedEffect(SecondaryStat.MarkofNightLord);
         Item nk = player.getInventory(MapleInventoryType.USE).getItem(attack.slot);
         int rand;
         Iterator var208;
         ArrayList monsters;
         Integer objectId;
         if (markOf != null && nk != null) {
            if (player.getSkillLevel(4120018) > 0) {
               markOf = SkillFactory.getSkill(4120018).getEffect(player.getSkillLevel(4120018));
            }

            int bulletCount = markOf.getBulletCount();
            if (attack.skill != 400041038 && attack.skill != 4100012 && attack.skill != 4120019) {
               if (monster.isBuffed(markOf.getSourceId()) || !monster.isAlive() && Randomizer.isSuccess(80)) {
                  if (attack.skill != 400041020 || attack.skill == 400041020 && Randomizer.isSuccess(SkillFactory.getSkill(400041020).getEffect(player.getSkillLevel(400041020)).getW())) {
                     List<MapleMapObject> objs = player.getMap().getMapObjectsInRange(player.getTruePosition(), 400000.0D, Arrays.asList(MapleMapObjectType.MONSTER));
                     statusz1 = new ArrayList();

                     for(y = 0; y < bulletCount; ++y) {
                        rand = objs.size() < 1 ? -1 : Randomizer.nextInt(objs.size());
                        if (rand >= 0) {
                           if (objs.size() < bulletCount) {
                              if (y < objs.size()) {
                                 statusz1.add(((MapleMapObject)objs.get(y)).getObjectId());
                              } else {
                                 statusz1.add(((MapleMapObject)objs.get(rand)).getObjectId());
                              }
                           } else if (objs.size() > 1) {
                              statusz1.add(((MapleMapObject)objs.get(rand)).getObjectId());
                              objs.remove(rand);
                           }
                        }
                     }

                     if (statusz1.isEmpty()) {
                        for(y = 0; y < bulletCount; ++y) {
                           statusz1.add(0);
                        }
                     }

                     MapleAtom atom = new MapleAtom(true, monster.getObjectId(), 11, true, monster.isBuffed(4120018) ? 4120019 : 4100012, monster.getTruePosition().x, monster.getTruePosition().y);
                     atom.setDwUserOwner(player.getId());
                     atom.setDwTargets(statusz1);
                     atom.setnItemId(player.getV("csstar") != null ? Integer.parseInt(player.getV("csstar")) : nk.getItemId());
                     if (statusz1.size() > 0) {
                        var208 = statusz1.iterator();

                        while(var208.hasNext()) {
                           objectId = (Integer)var208.next();
                           ForceAtom forceAtom = new ForceAtom(2, Randomizer.rand(41, 44), Randomizer.rand(3, 4), Randomizer.rand(67, 292), 200);
                           atom.addForceAtom(forceAtom);
                        }

                        player.getMap().spawnMapleAtom(atom);
                     }

                     if (monster.isBuffed(markOf.getSourceId())) {
                        monster.cancelSingleStatus(monster.getBuff(markOf.getSourceId()), markOf.getSourceId());
                     }
                  }
               } else if (attack.skill != 4111003) {
                  if (player.getSkillLevel(4120018) > 0) {
                     markOf = SkillFactory.getSkill(4120018).getEffect(1);
                     markOf.setDuration(20000);
                  } else {
                     markOf = SkillFactory.getSkill(4100011).getEffect(1);
                     markOf.setDuration(20000);
                  }

                  if (markOf.makeChanceResult() && monster.isAlive()) {
                     monsters = new ArrayList();
                     monsters.add(new Pair(MonsterStatus.MS_Burned, new MonsterStatusEffect(markOf.getSourceId(), markOf.getDOTTime(), 0L)));
                     monster.applyStatus(player.getClient(), monsters, markOf);
                  }
               }
            }
         }

         if (monster.getId() >= 9500650 && monster.getId() <= 9500654 && totDamageToOneMonster > 0L && player.getGuild() != null) {
            player.getGuild().updateGuildScore(totDamageToOneMonster);
         }

         if (monster.isBuffed(MonsterStatus.MS_PCounter) && player.getBuffedEffect(SecondaryStat.IgnorePImmune) == null && player.getBuffedEffect(SecondaryStat.IgnorePCounter) == null && player.getBuffedEffect(SecondaryStat.IgnoreAllCounter) == null && player.getBuffedEffect(SecondaryStat.IgnoreAllImmune) == null && !SkillFactory.getSkill(attack.skill).isIgnoreCounter() && !energy) {
            player.addHP(-monster.getBuff(MonsterStatus.MS_PCounter).getValue());
         }

         if (SkillFactory.getSkill(164101003).getSkillList().contains(attack.skill) && player.getBuffedEffect(SecondaryStat.Alterego) != null && System.currentTimeMillis() - player.lastAltergoTime >= 1500L) {
            List<MapleMapObject> objs = player.getMap().getMapObjectsInRange(player.getTruePosition(), 1000000.0D, Arrays.asList(MapleMapObjectType.MONSTER));
            player.lastAltergoTime = System.currentTimeMillis();
            atom = new MapleAtom(false, player.getId(), 60, true, 164101004, player.getTruePosition().x, player.getTruePosition().y);
            statusz = new ArrayList();
            fora = player.getBuffedValue(400041048) ? 12 : 3;
            rand = 0;
            var208 = objs.iterator();

            while(var208.hasNext()) {
               MapleMapObject o = (MapleMapObject)var208.next();
               statusz.add(o.getObjectId());
               ++rand;
               if (rand >= fora) {
                  break;
               }
            }

            while(rand < fora) {
               statusz.add(monster.getObjectId());
               ++rand;
            }

            var208 = statusz.iterator();

            while(var208.hasNext()) {
               objectId = (Integer)var208.next();
               atom.addForceAtom(new ForceAtom(player.getBuffedValue(400041048) ? 1 : 0, Randomizer.rand(40, 49), 3, Randomizer.rand(45, 327), 0));
            }

            atom.setDwTargets(statusz);
            player.getMap().spawnMapleAtom(atom);
         }

         if (!monster.isAlive()) {
            ++multikill;
         }

         if (player.getBuffedValue(400001050) && player.getSkillCustomValue0(400001050) == 400001055L) {
            shadowBite = SkillFactory.getSkill(400001050).getEffect(player.getSkillLevel(400001050));
            player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(400001055, new ArrayList(), true, 0));
            player.removeSkillCustomInfo(400001050);
            long duration = player.getBuffLimit(400001050);
            shadowBite.applyTo(player, false, (int)duration);
         }

         MapleAtom atom;
         if (attack.skill == 164001001) {
            atom = new MapleAtom(true, monster.getObjectId(), 63, true, 164001001, monster.getTruePosition().x, monster.getTruePosition().y);
            atom.setDwUserOwner(player.getId());
            atom.setDwFirstTargetId(player.getId());
            atom.setDwTargetId(monster.getObjectId());
            atom.addForceAtom(new ForceAtom(1, 5, 30, 0, 0));
            player.getMap().spawnMapleAtom(atom);
         }

         if (attack.skill == 164001002 && monster != null && monster.getBuff(164001001) != null) {
            monster.cancelSingleStatus(monster.getBuff(164001001));
         }

         if (player.getBuffedEffect(SecondaryStat.ButterflyDream) != null && System.currentTimeMillis() - player.lastButterflyTime >= (long)(player.getBuffedEffect(SecondaryStat.ButterflyDream).getX() * 1000)) {
            player.lastButterflyTime = System.currentTimeMillis();
            atom = new MapleAtom(false, player.getObjectId(), 63, true, 164001001, player.getTruePosition().x, player.getTruePosition().y);
            atom.setDwFirstTargetId(0);
            atom.addForceAtom(new ForceAtom(1, 42, 3, 136, 0));
            player.getMap().spawnMapleAtom(atom);
         }

         if (attack.skill == 400011047 && player.getBuffedValue(400011047)) {
            player.getMap().broadcastMessage(MobPacket.skillAttackEffect(monster.getObjectId(), attack.skill, player.getId()));
            player.setGraveTarget(player.getObjectId());
            player.createSecondAtom(SkillFactory.getSkill(400011047).getSecondAtoms(), monster.getPosition());
         }

         if (effect != null && monster.isAlive()) {
            statusz = new ArrayList();
            statusz1 = new ArrayList();
            statusz2 = new ArrayList();
            new ArrayList();
            ArrayList<Pair<MonsterStatus, MonsterStatusEffect>> applys = new ArrayList();
            ArrayList<Pair<MonsterStatus, MonsterStatusEffect>> applys1 = new ArrayList();
            ArrayList<Pair<MonsterStatus, MonsterStatusEffect>> applys2 = new ArrayList();
            SecondaryStatEffect effect2;
            SecondaryStatEffect eff2;
            int sk;
            switch(attack.skill) {
            case 1101012:
               statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 1L));
               break;
            case 1121015:
               statusz.add(new Triple(MonsterStatus.MS_Incizing, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 1L));
               break;
            case 1201011:
               statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(attack.skill, effect.getDOTTime()), (long)effect.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L));
               break;
            case 1201012:
               statusz.add(new Triple(MonsterStatus.MS_Speed, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)effect.getX()));
               break;
            case 1201013:
               statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 1L));
               break;
            case 1211008:
               statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 1L));
               break;
            case 1221004:
               if (!monster.getStats().isBoss()) {
                  statusz.add(new Triple(MonsterStatus.MS_Seal, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)effect.getDuration()));
               }
               break;
            case 1221052:
            case 11121004:
            case 11121013:
            case 14121004:
            case 31121006:
            case 31221003:
            case 36121053:
            case 64121001:
            case 151121040:
            case 155121007:
            case 155121306:
            case 400001008:
            case 400011121:
               sk = 0;
               if (attack.skill == 400011121) {
                  monster.setCustomInfo(400011121, 1, 0);
                  monster.setCustomInfo(400011122, 0, 10000);
               }

               if (attack.skill == 400011015) {
                  var10000 = effect.getW() * 1000;
               } else {
                  sk = effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration();
               }

               if (attack.skill == 14121004) {
                  if ((sk += attack.targets * 1000) > effect.getS() * 1000) {
                     sk = effect.getS() * 1000;
                  }
               } else if (attack.skill == 64121001) {
                  sk = effect.getDuration();
               }

               statusz.add(new Triple(MonsterStatus.MS_Freeze, new MonsterStatusEffect(attack.skill, sk), 1L));
               break;
            case 1301012:
               statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, 1000), 1L));
               break;
            case 2111007:
               statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getDOTTime()), 1L));
               statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(attack.skill, effect.getDOTTime()), (long)effect.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L));
               break;
            case 2121055:
               statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(attack.skill, effect.getDOTTime()), (long)effect.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L));
               break;
            case 2201004:
            case 2201008:
            case 2201009:
            case 2211002:
            case 2211006:
            case 2211010:
            case 2220014:
            case 2221003:
            case 2221011:
            case 2221012:
            case 2221054:
            case 400020002:
               if (attack.skill != 2221011) {
                  statusz.add(new Triple(MonsterStatus.MS_Speed, new MonsterStatusEffect(attack.skill, effect.getDuration()), (long)effect.getV()));
                  if (monster.getBuff(MonsterStatus.MS_Speed) == null && monster.getFreezingOverlap() > 0) {
                     monster.setFreezingOverlap(0);
                  }

                  if (monster.getFreezingOverlap() < 5) {
                     monster.setFreezingOverlap((byte)(monster.getFreezingOverlap() + 1));
                  }
               }

               if (attack.skill != 2221011) {
                  statusz.add(new Triple(MonsterStatus.MS_Freeze, new MonsterStatusEffect(attack.skill, 13000), 1L));
               }

               statusz.add(new Triple(MonsterStatus.MS_IndiePdr, new MonsterStatusEffect(attack.skill, 13000), (long)effect.getX()));
               statusz.add(new Triple(MonsterStatus.MS_IndieMdr, new MonsterStatusEffect(attack.skill, 13000), (long)effect.getY()));
               break;
            case 2201005:
            case 2211003:
            case 2211011:
            case 2221006:
               if (monster.getBuff(MonsterStatus.MS_Speed) == null && monster.getFreezingOverlap() > 0) {
                  monster.setFreezingOverlap(0);
               }

               if (monster.getFreezingOverlap() > 0) {
                  monster.setFreezingOverlap((byte)(monster.getFreezingOverlap() - 1));
                  if (monster.getFreezingOverlap() <= 0) {
                     monster.cancelStatus(MonsterStatus.MS_Speed, monster.getBuff(2201008));
                  } else {
                     statusz.add(new Triple(MonsterStatus.MS_Speed, new MonsterStatusEffect(2201008, 8000), -75L));
                  }
               }

               if (attack.skill == 2221006) {
                  statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getDuration()), 1L));
               }
               break;
            case 2211007:
            case 2311007:
               statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getDuration()), 1L));
               break;
            case 2221052:
            case 400021031:
            case 400021094:
               if (monster.getBuff(MonsterStatus.MS_Speed) == null && monster.getFreezingOverlap() > 0) {
                  monster.setFreezingOverlap(0);
               }

               if (attack.skill == 400021094) {
                  monster.addSkillCustomInfo(400021094, 1L);
                  if (monster.getFreezingOverlap() > 0) {
                     if (attack.skill == 400021094 && monster.getCustomValue0(400021094) >= 5L) {
                        monster.removeCustomInfo(400021094);
                        monster.setFreezingOverlap((byte)(monster.getFreezingOverlap() - 1));
                        statusz.add(new Triple(MonsterStatus.MS_Speed, new MonsterStatusEffect(2201008, 8000), -75L));
                     }
                  } else {
                     monster.cancelStatus(MonsterStatus.MS_Speed, monster.getBuff(2201008));
                  }
               }

               if (monster.getFreezingOverlap() > 0) {
                  monster.setFreezingOverlap((byte)(monster.getFreezingOverlap() - 1));
                  statusz.add(new Triple(MonsterStatus.MS_Speed, new MonsterStatusEffect(2201008, 8000), -75L));
               } else if (monster.getFreezingOverlap() <= 0) {
                  monster.cancelStatus(MonsterStatus.MS_Speed, monster.getBuff(2201008));
               }
               break;
            case 2301010:
               statusz.add(new Triple(MonsterStatus.MS_IndieMdr, new MonsterStatusEffect(attack.skill, 60000), (long)effect.getX()));
               break;
            case 3101005:
               statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 1L));
               break;
            case 3111003:
               statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(attack.skill, effect.getDOTTime()), (long)effect.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L));
               break;
            case 3121014:
               statusz.add(new Triple(MonsterStatus.MS_DebuffHealing, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)effect.getX()));
               statusz.add(new Triple(MonsterStatus.MS_Speed, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)effect.getW()));
               break;
            case 3121052:
               statusz.add(new Triple(MonsterStatus.MS_IndieUNK2, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)effect.getS()));
               break;
            case 3201008:
               statusz.add(new Triple(MonsterStatus.MS_Speed, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)effect.getZ()));
               break;
            case 4111003:
               if (!monster.getStats().isBoss()) {
                  statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(attack.skill, effect.getDuration()), (long)effect.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L));
                  statusz.add(new Triple(MonsterStatus.MS_Web, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 1L));
               }
               break;
            case 4121016:
               statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(attack.skill, effect.getDOTTime()), (long)effect.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L));
               break;
            case 4121017:
               sk = effect.getX();
               if (player.getSkillLevel(4120045) > 0) {
                  sk += SkillFactory.getSkill(4120045).getEffect(1).getX();
               }

               statusz.add(new Triple(MonsterStatus.MS_Showdown, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)sk));
               break;
            case 4201004:
               if (!monster.getStats().isBoss()) {
                  statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 1L));
               }
               break;
            case 4221010:
               statusz1.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(attack.skill, effect.getDOTTime()), (long)effect.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L));
               break;
            case 4321002:
               statusz2.add(new Triple(MonsterStatus.MS_AdddamParty, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 10L));
               break;
            case 4321004:
               statusz.add(new Triple(MonsterStatus.MS_RiseByToss, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 100L));
               break;
            case 4331006:
               statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 1L));
               break;
            case 4341011:
               statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(attack.skill, effect.getDOTTime()), (long)effect.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L));
               break;
            case 5011002:
               statusz.add(new Triple(MonsterStatus.MS_Speed, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)effect.getZ()));
               break;
            case 5111002:
               statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 1L));
               break;
            case 5121001:
               statusz.add(new Triple(MonsterStatus.MS_DragonStrike, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)effect.getX()));
               break;
            case 5310011:
               statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(attack.skill, effect.getDOTTime()), (long)effect.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L));
               statusz.add(new Triple(MonsterStatus.MS_AdddamSkill, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)effect.getZ()));
               break;
            case 5311002:
               if (attack.charge == 1000) {
                  statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 1L));
               }
               break;
            case 5311010:
               statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(attack.skill, effect.getDOTTime()), (long)effect.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L));
               statusz.add(new Triple(MonsterStatus.MS_Puriaus, new MonsterStatusEffect(attack.skill, effect.getDOTTime()), (long)effect.getZ()));
               break;
            case 13121052:
            case 400031022:
               statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(attack.skill, effect.getDOTTime()), (long)effect.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L));
               break;
            case 21100002:
               statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 1L));
               break;
            case 21100013:
               statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 1L));
               break;
            case 21101016:
               statusz.add(new Triple(MonsterStatus.MS_RiseByToss, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 100L));
               break;
            case 21110011:
            case 21110024:
            case 21110025:
            case 21111017:
               if (!monster.getStats().isBoss()) {
                  statusz.add(new Triple(MonsterStatus.MS_Freeze, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 1L));
               }
               break;
            case 23111002:
               statusz.add(new Triple(MonsterStatus.MS_Puriaus, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)effect.getX()));
               break;
            case 23120013:
               statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 1L));
               break;
            case 23121002:
               sk = effect.getY();
               if (player.getSkillLevel(23120050) > 0) {
                  eff2 = SkillFactory.getSkill(23120050).getEffect(1);
                  sk -= eff2.getX();
               }

               statusz.add(new Triple(MonsterStatus.MS_IndiePdr, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)(-sk)));
               statusz.add(new Triple(MonsterStatus.MS_IndieMdr, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)(-sk)));
               break;
            case 23121003:
               statusz.add(new Triple(MonsterStatus.MS_AdddamSkill2, new MonsterStatusEffect(23121000, effect.getDuration()), (long)effect.getX()));
               statusz.add(new Triple(MonsterStatus.MS_DodgeBodyAttack, new MonsterStatusEffect(attack.skill, effect.getDuration()), 1L));
               break;
            case 24121010:
               effect2 = SkillFactory.getSkill(24121003).getEffect(GameConstants.getLinkedSkill(24121003));
               statusz.add(new Triple(MonsterStatus.MS_IndiePdr, new MonsterStatusEffect(attack.skill, effect2.getSubTime() > 0 ? effect2.getSubTime() : effect2.getDuration()), (long)(-effect2.getY())));
               break;
            case 25100011:
            case 25101003:
            case 25101004:
            case 25111004:
               statusz.add(new Triple(MonsterStatus.MS_BahamutLightElemAddDam, new MonsterStatusEffect(25100011, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)effect.getX()));
               break;
            case 25111206:
               statusz.add(new Triple(MonsterStatus.MS_Freeze, new MonsterStatusEffect(attack.skill, effect.getDuration()), 1L));
               break;
            case 25120003:
               statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getDuration()), 1L));
               break;
            case 25121006:
               statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(attack.skill, effect.getDOTTime()), (long)effect.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L));
               break;
            case 25121007:
               if (monster.getStats().getCategory() != 1 && monster.getId() != 8880502 && monster.getId() != 8644650 && monster.getId() != 8644655 && monster.getId() != 8880342 && monster.getId() != 8880302) {
                  if (monster.getBuff(MonsterStatus.MS_SeperateSoulC) == null && monster.getBuff(MonsterStatus.MS_SeperateSoulP) == null) {
                     statusz.add(new Triple(MonsterStatus.MS_SeperateSoulP, new MonsterStatusEffect(attack.skill, effect.getDuration()), (long)effect.getLevel()));
                  }
               } else {
                  statusz.add(new Triple(MonsterStatus.MS_IndieUNK, new MonsterStatusEffect(attack.skill, effect.getDuration()), (long)effect.getLevel()));
               }
               break;
            case 31101002:
               statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getDuration()), 1L));
               break;
            case 31101003:
               statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 1L));
               break;
            case 31111001:
               statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 1L));
               break;
            case 31111005:
               statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(attack.skill, effect.getDOTTime()), (long)effect.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L));
               break;
            case 31121000:
               statusz.add(new Triple(MonsterStatus.MS_RiseByToss, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 100L));
               break;
            case 31121001:
               statusz.add(new Triple(MonsterStatus.MS_Speed, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)effect.getX()));
               break;
            case 31121003:
               statusz.add(new Triple(MonsterStatus.MS_IndiePdr, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)(-effect.getX())));
               statusz.add(new Triple(MonsterStatus.MS_IndieMdr, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)(-effect.getX())));
               statusz.add(new Triple(MonsterStatus.MS_Pad, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)(-effect.getX())));
               statusz.add(new Triple(MonsterStatus.MS_Mad, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)(-effect.getX())));
               statusz.add(new Triple(MonsterStatus.MS_Blind, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)(-effect.getZ())));
               statusz.add(new Triple(MonsterStatus.MS_Showdown, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)effect.getW()));
               break;
            case 31211011:
               statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 1L));
               break;
            case 31221002:
               statusz.add(new Triple(MonsterStatus.MS_IndiePdr, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)(-effect.getY())));
               break;
            case 33101215:
               statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 1L));
               break;
            case 36001000:
            case 36101000:
            case 36101001:
            case 36101008:
            case 36101009:
            case 36111000:
            case 36111001:
            case 36111002:
            case 36111009:
            case 36111010:
            case 36121000:
            case 36121001:
            case 36121011:
            case 36121012:
            case 400041007:
               if (player.getSkillLevel(36110005) > 0) {
                  if (monster.getBuff(MonsterStatus.MS_Explosion) == null && monster.getAirFrame() > 0) {
                     monster.setAirFrame(0);
                  }

                  if ((effect2 = SkillFactory.getSkill(36110005).getEffect(player.getSkillLevel(36110005))).makeChanceResult()) {
                     monster.setAirFrame(monster.getAirFrame() + 1);
                     if (monster.getAirFrame() >= 4) {
                        monster.setAirFrame(0);
                        monster.cancelSingleStatus(monster.getBuff(36110005), 36110005);
                        map.broadcastMessage(CField.ignitionBomb(36110005, monster.getObjectId(), monster.getTruePosition()));
                     } else {
                        statusz.add(new Triple(MonsterStatus.MS_Explosion, new MonsterStatusEffect(36110005, effect2.getDuration()), (long)monster.getAirFrame()));
                        statusz.add(new Triple(MonsterStatus.MS_Blind, new MonsterStatusEffect(36110005, effect2.getDuration()), (long)(-effect2.getX()) * (long)monster.getAirFrame()));
                        statusz.add(new Triple(MonsterStatus.MS_Eva, new MonsterStatusEffect(36110005, effect2.getDuration()), (long)(-effect2.getX()) * (long)monster.getAirFrame()));
                     }
                  }
               }
               break;
            case 37110002:
               statusz.add(new Triple(MonsterStatus.MS_BlessterDamage, new MonsterStatusEffect(37110002, SkillFactory.getSkill(37110001).getEffect(GameConstants.getLinkedSkill(37110002)).getDuration() * 2), (long)SkillFactory.getSkill(37110001).getEffect(GameConstants.getLinkedSkill(37110002)).getX()));
               break;
            case 37121004:
               statusz.add(new Triple(MonsterStatus.MS_Freeze, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 1L));
               break;
            case 51120057:
            case 51121007:
            case 51121009:
               statusz.add(new Triple(MonsterStatus.MS_Blind, new MonsterStatusEffect(attack.skill, attack.skill == 51120057 ? 10000 : (effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration())), (long)(-effect.getX())));
               break;
            case 51121052:
               statusz.add(new Triple(MonsterStatus.MS_DeadlyCharge, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)effect.getX()));
               break;
            case 61101101:
            case 61111217:
               statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 1L));
               break;
            case 61111100:
            case 61111113:
            case 61111218:
               statusz.add(new Triple(MonsterStatus.MS_Speed, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)effect.getZ()));
               break;
            case 61111101:
            case 61111219:
               statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 1L));
               break;
            case 61121100:
            case 400011079:
            case 400011080:
            case 400011081:
            case 400011082:
               statusz.add(new Triple(MonsterStatus.MS_Speed, new MonsterStatusEffect(61121100, 10000), -50L));
               break;
            case 63121006:
            case 63121007:
               effect2 = SkillFactory.getSkill(63121006).getEffect(player.getSkillLevel(63121006));
               statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(effect2.getSourceId(), effect2.getDOTTime()), (long)effect2.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L));
               break;
            case 64001000:
               statusz.add(new Triple(MonsterStatus.MS_Speed, new MonsterStatusEffect(attack.skill, effect.getS2()), (long)(-effect.getX())));
               break;
            case 64001001:
               statusz.add(new Triple(MonsterStatus.MS_Speed, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)(-effect.getX())));
               break;
            case 64001009:
            case 64001010:
            case 64001011:
               effect2 = SkillFactory.getSkill(64120000).getEffect(player.getSkillLevel(attack.skill));
               statusz.add(new Triple(MonsterStatus.MS_Speed, new MonsterStatusEffect(effect2.getSourceId(), effect2.getSubTime() > 0 ? effect2.getSubTime() : effect2.getDuration()), -10L));
               break;
            case 64111003:
               statusz.add(new Triple(MonsterStatus.MS_IndiePdr, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)effect.getW()));
               statusz.add(new Triple(MonsterStatus.MS_IndieMdr, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)effect.getW()));
               break;
            case 64121011:
               statusz.add(new Triple(MonsterStatus.MS_Pad, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)(-effect.getU())));
               statusz.add(new Triple(MonsterStatus.MS_Mad, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)(-effect.getU())));
               break;
            case 65101100:
               statusz.add(new Triple(MonsterStatus.MS_Explosion, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 1L));
               break;
            case 65121002:
               statusz.add(new Triple(MonsterStatus.MS_AdddamParty, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), (long)effect.getY()));
               break;
            case 65121100:
               statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration()), 1L));
               break;
            case 100001283:
               statusz.add(new Triple(MonsterStatus.MS_Freeze, new MonsterStatusEffect(attack.skill, effect.getDuration()), 1L));
               break;
            case 131001213:
               statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(attack.skill, SkillFactory.getSkill(131001013).getEffect(1).getV2() * 1000), (long)SkillFactory.getSkill(131001013).getEffect(1).getW() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L));
               break;
            case 131001313:
               statusz.add(new Triple(MonsterStatus.MS_Blind, new MonsterStatusEffect(attack.skill, SkillFactory.getSkill(131001013).getEffect(1).getPsdJump() * 1000), (long)SkillFactory.getSkill(131001013).getEffect(1).getY()));
               break;
            case 135001012:
               statusz.add(new Triple(MonsterStatus.MS_IndieUNK, new MonsterStatusEffect(attack.skill, effect.getDuration()), 1L));
               statusz.add(new Triple(MonsterStatus.MS_Speed, new MonsterStatusEffect(attack.skill, effect.getDuration()), -50L));
               break;
            case 151111002:
               statusz.add(new Triple(MonsterStatus.MS_TimeBomb, new MonsterStatusEffect(attack.skill, effect.getDuration()), (long)effect.getX()));
               break;
            case 164001001:
               statusz.add(new Triple(MonsterStatus.MS_RWChoppingHammer, new MonsterStatusEffect(attack.skill, effect.getDuration()), 1L));
               break;
            case 164111008:
               statusz.add(new Triple(MonsterStatus.MS_IndiePdr, new MonsterStatusEffect(attack.skill, effect.getDuration()), (long)(-effect.getY())));
               if (!monster.getStats().isBoss()) {
                  statusz.add(new Triple(MonsterStatus.MS_RWLiftPress, new MonsterStatusEffect(attack.skill, effect.getDuration()), (long)(2400500 + Randomizer.rand(0, 2))));
               }
               break;
            case 164121005:
               statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(attack.skill, effect.getDOTTime()), (long)effect.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L));
               break;
            case 164121044:
               statusz.add(new Triple(MonsterStatus.MS_Freeze, new MonsterStatusEffect(attack.skill, 11000), 1L));
               break;
            case 400011015:
               statusz.add(new Triple(MonsterStatus.MS_Freeze, new MonsterStatusEffect(400011024, 10000), (long)effect.getDuration()));
               break;
            case 400021001:
               statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(attack.skill, effect.getDOTTime()), (long)effect.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L));
               if (player.getBuffedEffect(SecondaryStat.WizardIgnite) != null && player.getBuffedEffect(SecondaryStat.WizardIgnite).makeChanceResult()) {
                  SkillFactory.getSkill(2100010).getEffect(player.getSkillLevel(2101010)).applyTo(player, monster.getTruePosition());
               }
               break;
            default:
               if (player.getSkillLevel(5110000) > 0) {
                  eff2 = SkillFactory.getSkill(5110000).getEffect(player.getSkillLevel(5110000));
                  if (eff2.makeChanceResult()) {
                     applys.add(new Pair(MonsterStatus.MS_Stun, new MonsterStatusEffect(5110000, 1000, 1L)));
                  }
               } else if (player.getBuffedValue(1111003)) {
                  applys.add(new Pair(MonsterStatus.MS_CriticalBind_N, new MonsterStatusEffect(1111003, 20000, 1L)));
               } else {
                  label5501: {
                     if (player.getBuffedValue(5311004)) {
                        effect2 = SkillFactory.getSkill(5311004).getEffect(player.getSkillLevel(5311004));
                        if (player.getSkillCustomValue0(5311004) == 2L) {
                           if (effect2.makeChanceResult()) {
                              applys.add(new Pair(MonsterStatus.MS_Speed, new MonsterStatusEffect(effect2.getSourceId(), effect2.getV() * 1000, -30L)));
                           }
                           break label5501;
                        }

                        if (player.getSkillCustomValue0(5311004) == 4L) {
                           applys.add(new Pair(MonsterStatus.MS_Burned, new MonsterStatusEffect(effect2.getSourceId(), effect2.getDOTTime(), (long)effect2.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L)));
                           break label5501;
                        }
                     }

                     if (player.getBuffedEffect(SecondaryStat.SnowCharge) != null) {
                        applys.add(new Pair(MonsterStatus.MS_Speed, new MonsterStatusEffect(player.getBuffSource(SecondaryStat.SnowCharge), player.getBuffedEffect(SecondaryStat.SnowCharge).getY() * (monster.getStats().isBoss() ? 500 : 1000), (long)(-player.getBuffedEffect(SecondaryStat.SnowCharge).getQ() / (monster.getStats().isBoss() ? 2 : 1)))));
                     } else if (player.getSkillLevel(25110210) > 0) {
                        eff2 = SkillFactory.getSkill(25110210).getEffect(player.getSkillLevel(25110210));
                        if (eff2.makeChanceResult()) {
                           applys.add(new Pair(MonsterStatus.MS_Acc, new MonsterStatusEffect(25110210, eff2.getDuration(), (long)(-eff2.getX()))));
                           applys.add(new Pair(MonsterStatus.MS_Eva, new MonsterStatusEffect(25110210, eff2.getDuration(), -40L)));
                           applys.add(new Pair(MonsterStatus.MS_AdddamSkill2, new MonsterStatusEffect(25110210, eff2.getDuration(), (long)eff2.getX())));
                        }
                     } else {
                        if (GameConstants.isPathFinder(player.getJob())) {
                           effect2 = SkillFactory.getSkill(3320001).getEffect(player.getSkillLevel(3320001));
                           if ((attack.skill == 3011004 || attack.skill == 3300002 || attack.skill == 3321003 || attack.skill == 3321005) && player.getBuffedValue(3320008) && effect2 != null && SkillFactory.getSkill(3320008).getEffect(player.getSkillLevel(3320008)).makeChanceResult()) {
                              player.setSkillCustomInfo(3320008, player.getSkillCustomValue0(3320008) - 1L, 0L);
                              if (player.getSkillCustomValue0(3320008) == 0L) {
                                 player.cancelEffectFromBuffStat(SecondaryStat.BonusAttack);
                              } else {
                                 SkillFactory.getSkill(3320008).getEffect(player.getSkillLevel(3320008)).applyTo(player, (int)player.getBuffLimit(3320008));
                              }

                              if (monster.getBuff(MonsterStatus.MS_BossPropPlus) == null) {
                                 monster.removeCustomInfo(3320008);
                              }

                              if (monster.getCustomValue0(3320008) < 5L) {
                                 monster.addSkillCustomInfo(3320008, 1L);
                              }

                              applys.add(new Pair(MonsterStatus.MS_BossPropPlus, new MonsterStatusEffect(3320001, effect2.getDuration(), monster.getCustomValue0(3320008))));
                           }

                           switch(attack.skill) {
                           case 3321007:
                           case 3321016:
                           case 3321018:
                              if (monster.getBuff(MonsterStatus.MS_BossPropPlus) == null) {
                                 monster.removeCustomInfo(3320008);
                              }

                              if (monster.getCustomValue0(3320008) < 5L) {
                                 monster.addSkillCustomInfo(3320008, 1L);
                              }

                              applys.add(new Pair(MonsterStatus.MS_BossPropPlus, new MonsterStatusEffect(3320001, effect2.getDuration(), monster.getCustomValue0(3320008))));
                              break;
                           case 3321020:
                              monster.setCustomInfo(3320008, 5, 0);
                              applys.add(new Pair(MonsterStatus.MS_BossPropPlus, new MonsterStatusEffect(3320001, effect2.getDuration(), monster.getCustomValue0(3320008))));
                           }
                        }

                        if (GameConstants.isKadena(player.getJob())) {
                           eff2 = SkillFactory.getSkill(64120007).getEffect(player.getSkillLevel(64120007));
                           if (player.getSkillLevel(64120007) > 0 && eff2 != null && eff2.makeChanceResult()) {
                              MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(eff2.getSourceId(), eff2.getDOTTime(), (long)((int)((long)eff2.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L)));
                              applys2.add(new Pair(MonsterStatus.MS_Burned, monsterStatusEffect));
                           }
                        }

                        int resist = SkillFactory.getSkill(101120110).getEffect(player.getSkillLevel(101120110)).getW() * 1000;
                        if (player.getSkillLevel(101120110) > 0 && player.getGender() == 1) {
                           if (System.currentTimeMillis() - monster.getLastCriticalBindTime() > (long)resist) {
                              if (monster.getStats().isBoss()) {
                                 statusz.add(new Triple(MonsterStatus.MS_Freeze, new MonsterStatusEffect(101120110, SkillFactory.getSkill(101120110).getEffect(player.getSkillLevel(101120110)).getDuration()), (long)SkillFactory.getSkill(101120110).getEffect(player.getSkillLevel(101120110)).getDuration()));
                              } else {
                                 statusz.add(new Triple(MonsterStatus.MS_CriticalBind_N, new MonsterStatusEffect(101120110, SkillFactory.getSkill(101120110).getEffect(player.getSkillLevel(101120110)).getDuration()), 1L));
                              }

                              monster.setLastCriticalBindTime(System.currentTimeMillis());
                           } else if (player.getSkillCustomValue(101120110) == null && monster.getStats().isBoss()) {
                              player.setSkillCustomInfo(101120110, 0L, 10000L);
                              player.getClient().getSession().writeAndFlush(MobPacket.monsterResist(monster, player, (int)(((long)resist - (System.currentTimeMillis() - monster.getLastCriticalBindTime())) / 1000L), 101120110));
                           }
                        }
                     }
                  }
               }
            }

            sk = 0;
            boolean enhance = false;
            int[] array2 = new int[]{4110011, 4210010, 4320005};
            int[] array3 = array2;
            int suc = array2.length;

            int dot;
            int venom;
            for(dot = 0; dot < suc; ++dot) {
               venom = array3[dot];
               if (player.getSkillLevel(venom) > 0) {
                  sk = venom;
               }
            }

            int var54;
            int fatal;
            if (sk > 0 && attack.skill != 4111003) {
               array3 = new int[]{4120011, 4220011, 4340012};
               int[] var249 = array3;
               venom = array3.length;

               for(var54 = 0; var54 < venom; ++var54) {
                  fatal = var249[var54];
                  if (player.getSkillLevel(fatal) > 0) {
                     enhance = true;
                     sk = fatal;
                  }
               }

               SecondaryStatEffect venomEffect = SkillFactory.getSkill(sk).getEffect(player.getSkillLevel(sk));
               MonsterStatusEffect monsterStatusEffect2 = new MonsterStatusEffect(venomEffect.getSourceId(), venomEffect.getDOTTime());
               if (venomEffect.makeChanceResult()) {
                  if (monster.isBuffed(MonsterStatus.MS_Burned)) {
                     if (monster.getBurnedBuffSize(sk) < (enhance ? venomEffect.getDotSuperpos() : 1)) {
                        statusz.add(new Triple(MonsterStatus.MS_Burned, monsterStatusEffect2, (long)venomEffect.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L));
                     }
                  } else {
                     statusz.add(new Triple(MonsterStatus.MS_Burned, monsterStatusEffect2, (long)venomEffect.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L));
                  }
               }
            }

            SecondaryStatEffect elementSoul;
            if (player.getSkillLevel(101110103) > 0) {
               elementSoul = SkillFactory.getSkill(101110103).getEffect(player.getSkillLevel(101110103));
               if (player.getGender() == 1 && Randomizer.isSuccess(((SecondaryStatEffect)elementSoul).getProp())) {
                  if (monster.getBuff(101110103) == null && monster.getCustomValue0(101110103) > 0L) {
                     monster.removeCustomInfo(101110103);
                  }

                  if (monster.getCustomValue0(101110103) < 5L) {
                     monster.setCustomInfo(101110103, (int)monster.getCustomValue0(101110103) + 1, 0);
                  }

                  statusz.add(new Triple(MonsterStatus.MS_MultiPMDR, new MonsterStatusEffect(101110103, ((SecondaryStatEffect)elementSoul).getDuration()), (long)((SecondaryStatEffect)elementSoul).getY() * monster.getCustomValue0(101110103)));
               }
            }

            if (player.getSkillLevel(101120207) > 0) {
               elementSoul = SkillFactory.getSkill(101120207).getEffect(player.getSkillLevel(101120207));
               if (player.getGender() == 0 && debinrear) {
                  statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(101120207, ((SecondaryStatEffect)elementSoul).getDOTTime()), (long)((SecondaryStatEffect)elementSoul).getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L));
               }
            }

            if (player.getBuffedValue(SecondaryStat.BleedingToxin) != null) {
               shadowBite = player.getBuffedEffect(SecondaryStat.BleedingToxin);
               if (shadowBite != null && shadowBite.makeChanceResult()) {
                  MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(shadowBite.getSourceId(), shadowBite.getDOTTime());
                  statusz2.add(new Triple(MonsterStatus.MS_Burned, monsterStatusEffect, (long)shadowBite.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L));
               }
            } else if (player.getBuffedValue(SecondaryStat.ElementDarkness) != null) {
               shadowBite = player.getBuffedEffect(SecondaryStat.ElementDarkness);
               suc = shadowBite.getProp();
               dot = shadowBite.getDOT();
               int[] var253 = new int[]{14100026, 14110028, 14120007};
               var54 = var253.length;

               for(fatal = 0; fatal < var54; ++fatal) {
                  int pas = var253[fatal];
                  SecondaryStatEffect eff1;
                  if (player.getSkillLevel(pas) > 0 && (eff1 = SkillFactory.getSkill(pas).getEffect(player.getSkillLevel(pas))) != null) {
                     suc += eff1.getProp();
                     dot += eff1.getDOT();
                  }
               }

               if (shadowBite != null && Randomizer.isSuccess(suc)) {
                  if (monster.getBuff(MonsterStatus.MS_ElementDarkness) == null) {
                     monster.setCustomInfo(14001021, 0, 0);
                  }

                  if (monster.getCustomValue0(14001021) < 5L) {
                     monster.addSkillCustomInfo(14001021, 1L);
                  }

                  if (player.getBuffedValue(14121052)) {
                     monster.setCustomInfo(14001021, 5, 0);
                  }

                  applys.add(new Pair(MonsterStatus.MS_Burned, new MonsterStatusEffect(shadowBite.getSourceId(), shadowBite.getDOTTime(), (long)((int)((long)shadowBite.getDOT() * totDamageToOneMonster * monster.getCustomValue0(14001021) / (long)attack.allDamage.size() / 1000L)))));
                  applys.add(new Pair(MonsterStatus.MS_ElementDarkness, new MonsterStatusEffect(shadowBite.getSourceId(), shadowBite.getDOTTime(), monster.getCustomValue0(14001021))));
                  if (player.getSkillLevel(14120009) > 0 && player.getBuffedEffect(SecondaryStat.Protective) == null) {
                     SkillFactory.getSkill(14120009).getEffect(player.getSkillLevel(14120009)).applyTo(player, false);
                  }
               }
            } else if (GameConstants.isCain(player.getJob()) && player.getSkillLevel(63110011) > 0 && monster != null) {
               elementSoul = SkillFactory.getSkill(63110011).getEffect(player.getSkillLevel(63110011));
               if (SkillFactory.getSkill(63110011).getSkillList().contains(attack.skill) && monster.isAlive()) {
                  boolean cast = true;
                  if ((attack.skill == 63101104 || attack.skill == 63121141) && monster.getCustomValue(attack.skill) != null) {
                     cast = false;
                  }

                  if (cast) {
                     monster.setCustomInfo(63110011, (int)monster.getCustomValue0(63110011) + 1, elementSoul.getDuration());
                     if (monster.getCustomValue0(63110011) >= (long)elementSoul.getX()) {
                        monster.setCustomInfo(63110011, elementSoul.getX(), elementSoul.getDuration());
                     }

                     ArrayList<MapleMonster> moblist = new ArrayList();
                     Iterator var257 = player.getMap().getAllMonster().iterator();

                     while(var257.hasNext()) {
                        MapleMonster mob = (MapleMonster)var257.next();
                        if (mob.getCustomValue0(63110011) > 0L) {
                           moblist.add(mob);
                        }
                     }

                     player.getClient().send(CField.getDeathBlessStack(player, moblist));
                     if (attack.skill == 63101104 || attack.skill == 63121141) {
                        monster.setCustomInfo(attack.skill, 0, 3000);
                     }
                  }
               } else if (SkillFactory.getSkill(63110011).getSkillList2().contains(attack.skill)) {
                  if (player.getBuffedValue(63111013)) {
                     player.handlePossession(10);
                  }

                  if (monster.isAlive() && monster.getCustomValue0(63110011) > 0L) {
                     monster.setCustomInfo(63110011, (int)monster.getCustomValue0(63110011) - 1, elementSoul.getDuration());
                     ArrayList<MapleMonster> moblist = new ArrayList();
                     moblist.add(monster);
                     player.handlePossession(2);
                     player.getClient().send(CField.getDeathBlessAttack(moblist, 63111012));
                     moblist.clear();
                     Iterator var254 = player.getMap().getAllMonster().iterator();

                     while(var254.hasNext()) {
                        MapleMonster mob = (MapleMonster)var254.next();
                        if (mob.getCustomValue0(63110011) > 0L) {
                           moblist.add(mob);
                        }
                     }

                     player.getClient().send(CField.getDeathBlessStack(player, moblist));
                     SkillFactory.getSkill(63111013).getEffect(player.getSkillLevel(63110011)).applyTo(player);
                  }

                  if (player.getSkillLevel(63120001) > 0) {
                     if (monster.getStats().isBoss() && monster.isAlive()) {
                        player.addHP(player.getStat().getCurrentMaxHp() / 100L * (long)SkillFactory.getSkill(63111013).getEffect(player.getSkillLevel(63111013)).getX());
                     } else if (!monster.isAlive()) {
                        player.addSkillCustomInfo(63111013, 1L);
                        if (player.getSkillCustomValue0(63111013) >= (long)SkillFactory.getSkill(63111013).getEffect(player.getSkillLevel(63111013)).getV()) {
                           player.removeSkillCustomInfo(63111013);
                           player.addHP(player.getStat().getCurrentMaxHp() / 100L * (long)SkillFactory.getSkill(63111013).getEffect(player.getSkillLevel(63111013)).getV());
                        }
                     }
                  }
               }
            }

            if (player.getSkillLevel(400041000) > 0 && attack.skill != 400041000 && Randomizer.isSuccess(50)) {
               elementSoul = SkillFactory.getSkill(400041000).getEffect(player.getSkillLevel(400041000));
               applys2.add(new Pair(MonsterStatus.MS_Burned, new MonsterStatusEffect(400040000, 8000, (long)((int)((long)((SecondaryStatEffect)elementSoul).getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L)))));
            }

            Iterator var247 = statusz.iterator();

            label5412:
            while(true) {
               Triple status;
               while(true) {
                  do {
                     do {
                        do {
                           if (!var247.hasNext()) {
                              var247 = statusz1.iterator();

                              while(true) {
                                 while(true) {
                                    do {
                                       do {
                                          do {
                                             if (!var247.hasNext()) {
                                                var247 = statusz2.iterator();

                                                while(true) {
                                                   while(true) {
                                                      do {
                                                         do {
                                                            do {
                                                               if (!var247.hasNext()) {
                                                                  elementSoul = player.getBuffedEffect(11001022);
                                                                  if (elementSoul != null && elementSoul.makeChanceResult()) {
                                                                     applys.add(new Pair(MonsterStatus.MS_Stun, new MonsterStatusEffect(elementSoul.getSourceId(), elementSoul.getSubTime(), (long)elementSoul.getSubTime())));
                                                                  }

                                                                  if (attack.skill == 13111021 && attack.hits == 2) {
                                                                     applys.add(new Pair(MonsterStatus.MS_MultiDamSkill, new MonsterStatusEffect(effect.getSourceId(), effect.getDuration(), (long)effect.getX())));
                                                                  }

                                                                  if (monster != null && monster.isAlive()) {
                                                                     if (!applys.isEmpty()) {
                                                                        monster.applyStatus(player.getClient(), applys, effect);
                                                                     }

                                                                     if (!applys1.isEmpty()) {
                                                                        monster.applyStatus(player.getClient(), applys1, effect);
                                                                     }

                                                                     if (!applys2.isEmpty()) {
                                                                        monster.applyStatus(player.getClient(), applys2, effect);
                                                                     }
                                                                  }

                                                                  if (!applys.isEmpty() && player.getSkillLevel(80002770) > 0) {
                                                                     SkillFactory.getSkill(80002770).getEffect(player.getSkillLevel(80002770)).applyTo(player, false);
                                                                  }
                                                                  break label5412;
                                                               }

                                                               status = (Triple)var247.next();
                                                            } while(status.left == null);
                                                         } while(status.mid == null);
                                                      } while(((MonsterStatusEffect)status.mid).shouldCancel(System.currentTimeMillis()));

                                                      if (status.left == MonsterStatus.MS_Burned && (Long)status.right < 0L) {
                                                         status.right = (Long)status.right & 4294967295L;
                                                      }

                                                      if (((MonsterStatusEffect)status.mid).getSkill() == 51121009) {
                                                         if (Randomizer.isSuccess(effect.getY())) {
                                                            break;
                                                         }
                                                      } else if (((MonsterStatusEffect)status.mid).getSkill() == 64121016) {
                                                         if (Randomizer.isSuccess(effect.getS2())) {
                                                            break;
                                                         }
                                                      } else if (effect.makeChanceResult()) {
                                                         break;
                                                      }

                                                      if (attack.skill == 1211008) {
                                                         ((MonsterStatusEffect)status.mid).setValue((Long)status.right);
                                                         applys2.add(new Pair(status.left, status.mid));
                                                      }
                                                   }

                                                   ((MonsterStatusEffect)status.mid).setValue((Long)status.right);
                                                   applys2.add(new Pair(status.left, status.mid));
                                                }
                                             }

                                             status = (Triple)var247.next();
                                          } while(status.left == null);
                                       } while(status.mid == null);
                                    } while(((MonsterStatusEffect)status.mid).shouldCancel(System.currentTimeMillis()));

                                    if (status.left == MonsterStatus.MS_Burned && (Long)status.right < 0L) {
                                       status.right = (Long)status.right & 4294967295L;
                                    }

                                    if (((MonsterStatusEffect)status.mid).getSkill() == 51121009) {
                                       if (Randomizer.isSuccess(effect.getY())) {
                                          break;
                                       }
                                    } else if (((MonsterStatusEffect)status.mid).getSkill() == 64121016) {
                                       if (Randomizer.isSuccess(effect.getS2())) {
                                          break;
                                       }
                                    } else if (effect.makeChanceResult()) {
                                       break;
                                    }

                                    if (attack.skill == 1211008) {
                                       ((MonsterStatusEffect)status.mid).setValue((Long)status.right);
                                       applys1.add(new Pair(status.left, status.mid));
                                    }
                                 }

                                 ((MonsterStatusEffect)status.mid).setValue((Long)status.right);
                                 applys1.add(new Pair(status.left, status.mid));
                              }
                           }

                           status = (Triple)var247.next();
                        } while(status.left == null);
                     } while(status.mid == null);
                  } while(((MonsterStatusEffect)status.mid).shouldCancel(System.currentTimeMillis()));

                  if (status.left == MonsterStatus.MS_Burned && (Long)status.right < 0L) {
                     status.right = (Long)status.right & 4294967295L;
                  }

                  if (((MonsterStatusEffect)status.mid).getSkill() == 51121009) {
                     if (Randomizer.isSuccess(effect.getY())) {
                        break;
                     }
                  } else if (((MonsterStatusEffect)status.mid).getSkill() == 64121016) {
                     if (Randomizer.isSuccess(effect.getS2())) {
                        break;
                     }
                  } else if (effect.makeChanceResult()) {
                     break;
                  }

                  if (attack.skill == 1211008) {
                     ((MonsterStatusEffect)status.mid).setValue((Long)status.right);
                     applys.add(new Pair(status.left, status.mid));
                  }
               }

               ((MonsterStatusEffect)status.mid).setValue((Long)status.right);
               applys.add(new Pair(status.left, status.mid));
            }
         }

         if (player.getBuffedValue(SecondaryStat.BMageDeath) != null && player.skillisCooling(32001114) && GameConstants.isBMDarkAtackSkill(attack.skill) && player.getBuffedValue(SecondaryStat.AttackCountX) != null) {
            player.changeCooldown(32001114, -500);
         }

         if (player.getBuffedValue(SecondaryStat.BMageDeath) != null && (!monster.isAlive() || monster.getStats().isBoss()) && attack.skill != player.getBuffSource(SecondaryStat.BMageDeath)) {
            y = player.getBuffedValue(SecondaryStat.AttackCountX) != null ? 1 : (player.getLevel() >= 100 ? 6 : (player.getLevel() > 60 ? 8 : 10));
            if (player.getDeath() < y) {
               player.setDeath((byte)(player.getDeath() + 1));
               if (player.getDeath() >= y) {
                  player.setSkillCustomInfo(32120019, 1L, 0L);
               }

               HashMap<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
               statups.put(SecondaryStat.BMageDeath, new Pair(Integer.valueOf(player.getDeath()), 0));
               player.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, player.getBuffedEffect(SecondaryStat.BMageDeath), player));
            }
         }

         if (GameConstants.isHolyAttack(attack.skill) && monster.isBuffed(MonsterStatus.MS_ElementResetBySummon)) {
            monster.cancelStatus(MonsterStatus.MS_ElementResetBySummon, monster.getBuff(MonsterStatus.MS_ElementResetBySummon));
         }

         if (monster.isBuffed(MonsterStatus.MS_JaguarBleeding) && attack.targets > 0 && monster.getBuff(MonsterStatus.MS_JaguarBleeding) != null && (attack.skill == 33001105 || attack.skill == 33001205 || attack.skill == 33101113 || attack.skill == 33101213 || attack.skill == 33111112 || attack.skill == 33111212 || attack.skill == 33121114 || attack.skill == 33121214)) {
            mobList = new ArrayList();
            mobList.add(new Triple(monster.getObjectId(), 60, 0));
            player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(33000036, mobList, false, monster.getAnotherByte()));
         }

         Iterator var209;
         if (player.getSkillLevel(400011116) > 0 && SkillFactory.getSkill(400011116).getSkillList().contains(attack.skill)) {
            shadowBite = SkillFactory.getSkill(400011116).getEffect(player.getSkillLevel(400011116));
            if (player.getBuffedValue(400011116)) {
               afterimageshockattack = true;
               y = 0;
               statusz = new ArrayList();

               for(var209 = attack.allDamage.iterator(); var209.hasNext(); ++y) {
                  AttackPair a = (AttackPair)var209.next();
                  statusz.add(new Triple(a.objectId, 120 + 70 * y, 0));
               }

               player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(400011133, statusz, false, 0));
            } else if (!player.getBuffedValue(400011116) && attack.targets > 0 && player.skillisCooling(400011116) && !player.skillisCooling(400011117)) {
               monsters = new ArrayList();
               i2 = 0;
               var209 = player.getMap().getMapObjectsInRange(player.getPosition(), 100000.0D, Arrays.asList(MapleMapObjectType.MONSTER)).iterator();

               while(var209.hasNext()) {
                  MapleMapObject o = (MapleMapObject)var209.next();
                  MapleMonster mon = (MapleMonster)o;
                  monsters.clear();
                  monsters.add(new Triple(mon.getObjectId(), 120 + 70 * i2, 0));
                  ++i2;
                  if (i2 == 7) {
                     break;
                  }

                  player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(400011117, monsters, false, mon.getObjectId()));
               }

               player.addCooldown(400011117, System.currentTimeMillis(), (long)(shadowBite.getX() * 1000));
               player.getClient().getSession().writeAndFlush(CField.skillCooldown(400011117, shadowBite.getX() * 1000));
            }
         }

         if (player.getBuffedValue(400031000)) {
            player.getMap().broadcastMessage(CField.ForceAtomAttack(1, player.getId(), monster.getObjectId()));
         }

         if (attack.skill != 400041035 && attack.skill != 400041036 && player.getBuffedValue(400041035) && System.currentTimeMillis() - player.lastChainArtsFuryTime >= 1000L) {
            player.lastChainArtsFuryTime = System.currentTimeMillis();
            player.getMap().broadcastMessage(CField.ChainArtsFury(monster.getTruePosition()));
         }

         if (player.getBuffedValue(400011016)) {
            shadowBite = player.getBuffedEffect(400011016);
            if (System.currentTimeMillis() - player.lastInstallMahaTime >= (long)(shadowBite.getX() * 1000)) {
               player.lastInstallMahaTime = System.currentTimeMillis();
               player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(400011020, new ArrayList(), true, 0));
            }
         }

         if (attack.skill == 31211001 && player.getBuffedEffect(SecondaryStat.DebuffIncHp) == null) {
            player.addHP(player.getStat().getCurrentMaxHp() * (long)effect.getY() / 100L);
         }

         if (totDamage > 0L && attack.skill == 4221016 && player.getSkillLevel(400041025) > 0) {
            if (player.shadowerDebuffOid == 0) {
               player.shadowerDebuff = Math.min(3, player.shadowerDebuff + 1);
               player.shadowerDebuffOid = monster.getObjectId();
            } else if (player.shadowerDebuffOid != monster.getObjectId()) {
               player.shadowerDebuff = 1;
               player.shadowerDebuffOid = monster.getObjectId();
            } else {
               player.shadowerDebuff = Math.min(3, player.shadowerDebuff + 1);
            }

            effect.applyTo(player);
         }

         if (attack.skill == 400041026) {
            player.shadowerDebuff = 0;
            player.shadowerDebuffOid = 0;
            SkillFactory.getSkill(400041026).getEffect(player.getSkillLevel(400041025)).applyTo(player);
            player.cancelEffectFromBuffStat(SecondaryStat.ShadowerDebuff, 4221016);
         }

         if (attack.skill == 5221015) {
            player.guidedBullet = monster.getObjectId();
            if (player.getKeyValue(1544, String.valueOf(5221029)) == 1L) {
               MapleSummon summon4 = new MapleSummon(player, 5221029, monster.getPosition(), SummonMovementType.STATIONARY, (byte)0, 60000);
               player.getMap().spawnSummon(summon4, 60000);
               player.addSummon(summon4);
            }
         }

         if (attack.skill == 151121001) {
            player.graveObjectId = monster.getObjectId();
         }

         if (player.getBuffedValue(400031002) && attack.skill != 400030002 && (player.lastArrowRain == 0L || player.lastArrowRain < System.currentTimeMillis())) {
            shadowBite = player.getBuffedEffect(400031002);
            SkillFactory.getSkill(400030002).getEffect(shadowBite.getLevel()).applyTo(player, monster.getTruePosition(), (int)(shadowBite.getT() * 1000.0D));
            player.lastArrowRain = System.currentTimeMillis() + (long)(shadowBite.getX() * 1000);
         }

         if (player.getBuffedValue(400041008) && (GameConstants.isDarkAtackSkill(attack.skill) || attack.summonattack != 0)) {
            shadowBite = SkillFactory.getSkill(400040008).getEffect(player.getSkillLevel(400040008));
            MapleMist mist = new MapleMist(shadowBite.calculateBoundingBox(monster.getPosition(), player.isFacingLeft()), player, shadowBite, 2000, (byte)(player.isFacingLeft() ? 1 : 0));
            mist.setPosition(monster.getPosition());
            player.getMap().spawnMist(mist, false);
            i2 = 0;
            var209 = player.getMap().getAllMistsThreadsafe().iterator();

            while(var209.hasNext()) {
               MapleMist mist2 = (MapleMist)var209.next();
               if (mist2.getOwnerId() == player.getId() && mist.getSourceSkill().getId() == 400040008) {
                  ++i2;
                  if (i2 == 9) {
                     break;
                  }
               }
            }

            if (player.getSkillCustomValue(400041019) == null && i2 >= 9) {
               player.getMap().broadcastMessage(CField.NightWalkerShadowSpearBig(monster.getTruePosition().x, monster.getTruePosition().y));
               player.setSkillCustomInfo(400041019, 0L, 3000L);
            }
         }

         if (player.getSkillLevel(32101009) > 0 && !monster.isAlive() && player.getBuffedEffect(SecondaryStat.DebuffIncHp) == null) {
            player.addHP(player.getStat().getCurrentMaxHp() * (long)SkillFactory.getSkill(32101009).getEffect(player.getSkillLevel(32101009)).getKillRecoveryR() / 100L);
         }

         if (attack.skill == 400041037) {
            shadowBite = SkillFactory.getSkill(400041037).getEffect(player.getSkillLevel(400041037));
            if (player.getBuffedValue(SecondaryStat.ShadowBatt) != null) {
               atom = new MapleAtom(false, player.getId(), 15, true, 14000028, monster.getTruePosition().x, monster.getTruePosition().y);
               atom.setDwFirstTargetId(monster.getObjectId());
               forceAtom = new ForceAtom(player.getSkillLevel(14120008) > 0 ? 2 : 1, 1, 5, Randomizer.rand(45, 90), 500);
               atom.addForceAtom(forceAtom);
               player.getMap().spawnMapleAtom(atom);
            }

            if (!monster.isAlive() || monster.getStats().isBoss()) {
               player.shadowBite = Math.min(shadowBite.getQ(), player.shadowBite + (monster.getStats().isBoss() ? shadowBite.getW() : shadowBite.getY()));
               atom = new MapleAtom(true, monster.getObjectId(), 42, true, 400041037, monster.getTruePosition().x, monster.getTruePosition().y);
               atom.setDwUserOwner(player.getId());
               atom.setDwFirstTargetId(0);
               atom.addForceAtom(new ForceAtom(2, 42, 6, 33, (short)Randomizer.rand(2500, 3000)));
               player.getMap().spawnMapleAtom(atom);
               if (player.shadowBite > 0 && !player.getBuffedValue(400041037)) {
                  SkillFactory.getSkill(400041037).getEffect(attack.skilllevel).applyTo(player, false);
               }
            }
         }

         if (attack.skill == 155100009 && player.getSkillLevel(155111207) > 0 && Randomizer.isSuccess((mark = SkillFactory.getSkill(155111207).getEffect(player.getSkillLevel(155111207))).getS()) && player.getMwSize(155111207) < (player.getKeyValue(1544, "155111207") == 1L ? mark.getY() : mark.getZ())) {
            skillid = monster.getPosition().x + Randomizer.rand(-100, 100);
            y = monster.getPosition().y + Randomizer.rand(-70, 30);
            MapleMagicWreck mw = new MapleMagicWreck(player, mark.getSourceId(), new Point(skillid, y), mark.getQ() * 1000);
            player.getMap().spawnMagicWreck(mw);
         }

         if (GameConstants.isIllium(player.getJob()) && monster.getBuff(MonsterStatus.MS_CurseMark) != null && attack.skill != 152001002 && attack.skill != 152120003 && attack.skill != 152120002 && attack.skill != 152120016) {
            skillid = player.getSkillLevel(152120013) > 0 ? 152120013 : (player.getSkillLevel(152110010) > 0 ? 152110010 : 152100012);
            monsters = new ArrayList();
            monsters.add(new Triple(monster.getObjectId(), 120, 0));
            player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(skillid, monsters, false, 0));
         }

         if (player.getBuffedValue(400051007) && attack.skill != 400051007 && attack.skill != 400051013 && System.currentTimeMillis() - player.lastThunderTime >= (long)(player.getBuffedEffect(400051007).getY() * 1000)) {
            player.lastThunderTime = System.currentTimeMillis();
            player.getClient().getSession().writeAndFlush(CField.lightningUnionSubAttack(attack.skill, 400051007, player.getSkillLevel(400051007)));
         }

         if (player.getBuffedValue(80002890) && attack.skill != 80002890 && attack.skill != 80002890 && System.currentTimeMillis() - player.lastThunderTime >= (long)player.getBuffedEffect(80002890).getCooldown(player)) {
            player.lastThunderTime = System.currentTimeMillis();
            player.getClient().getSession().writeAndFlush(CField.rangeAttack(80002890, Arrays.asList(new RangeAttack(80002890, attack.position, 0, 0, 1))));
         }

         if (player.getBuffedValue(4221054) && player.getFlip() < 5) {
            player.setFlip((byte)(player.getFlip() + 1));
            SkillFactory.getSkill(4221054).getEffect(player.getSkillLevel(4221054)).applyTo(player, false, false);
         }

         if (attack.skill == 5311002) {
            player.cancelEffectFromBuffStat(SecondaryStat.KeyDownTimeIgnore, 5310008);
         } else if (player.getSkillLevel(5311002) > 0 && !player.getBuffedValue(5310008) && attack.skill != 400051008) {
            SkillFactory.getSkill(5310008).getEffect(player.getSkillLevel(5311002)).applyTo(player, false);
         }

         if (player.getBuffedValue(SecondaryStat.PinPointRocket) != null && attack.skill != 36001005 && System.currentTimeMillis() - player.lastPinPointRocketTime >= (long)(player.getBuffedEffect(SecondaryStat.PinPointRocket).getX() * 1000)) {
            player.lastPinPointRocketTime = System.currentTimeMillis();
            atom = new MapleAtom(false, player.getId(), 6, true, 36001005, player.getTruePosition().x, player.getTruePosition().y);
            monsters = new ArrayList();

            for(i2 = 0; i2 < player.getBuffedEffect(SecondaryStat.PinPointRocket).getBulletCount(); ++i2) {
               monsters.add(0);
               atom.addForceAtom(new ForceAtom(0, 19, Randomizer.rand(20, 40), Randomizer.rand(40, 200), 0));
            }

            atom.setDwTargets(monsters);
            player.getMap().spawnMapleAtom(atom);
         }

         if (attack.skill == 400011060 && monster != null && player.getBuffedEffect(SecondaryStat.WillofSwordStrike) == null) {
            effect.applyTo(player, false, monster.getTruePosition());
         }
      }
   }

   private static void parseFinalAttack(MapleCharacter player, AttackInfo attack, MapleMonster monster) {
      int[][] finalAttackReq = new int[][]{{1100002, 1120013}, {1200002, 0}, {1300002, 0}, {2121007, 2120013}, {2221007, 2220014}, {3100001, 3120008}, {3200001, 0}, {4341054, 0}, {5121013, 0}, {5220020, 0}, {5311004, 1}, {11101002, 0}, {21100010, 21120012}, {22000015, 22110021}, {23100006, 23120012}, {31220007, 0}, {32121004, 32121011}, {33100009, 33120011}, {37000007, 0}, {51100002, 51120002}};
      Item weapon = player.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
      int[][] var5 = finalAttackReq;
      int var6 = finalAttackReq.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         int[] skill = var5[var7];
         if (weapon != null && attack.skill / 10000 != 8000 && attack.skill / 10000 <= player.getJob()) {
            int finalSkill = GameConstants.getLinkedSkill(skill[0]);
            int advSkill = GameConstants.getLinkedSkill(skill[1]);
            SecondaryStatEffect eff = null;
            if (SkillFactory.getSkill(advSkill) != null && player.getSkillLevel(advSkill) > 0) {
               eff = SkillFactory.getSkill(advSkill).getEffect(player.getSkillLevel(advSkill));
            } else if (SkillFactory.getSkill(finalSkill) != null && player.getSkillLevel(finalSkill) > 0) {
               eff = SkillFactory.getSkill(finalSkill).getEffect(player.getSkillLevel(finalSkill));
            }

            Integer value;
            if (advSkill == attack.skill || finalSkill == attack.skill || (advSkill == 2120013 || advSkill == 2220014 || advSkill == 32121011) && !player.skillisCooling(finalSkill) || finalSkill == 4341054 && player.getBuffedEffect(SecondaryStat.WindBreakerFinal) == null || finalSkill == 5311004 && ((value = player.getBuffedValue(SecondaryStat.Roulette)) == null || value != 1)) {
               break;
            }

            if (eff != null) {
               int chance = eff.getProp();
               int attackCount = Math.max(eff.getAttackCount(), 1);
               if (player.getBuffedEffect(SecondaryStat.FinalAttackProp) != null) {
                  chance += player.getBuffedValue(SecondaryStat.FinalAttackProp);
               }

               if (eff.getSourceId() == 1120013 && player.getSkillLevel(1120048) > 0) {
                  chance += SkillFactory.getSkill(1120048).getEffect(player.getSkillLevel(1120048)).getProp();
               }

               if (player.getBuffedValue(33121054)) {
                  chance = 100;
               }

               if (Randomizer.isSuccess(chance)) {
                  player.getClient().getSession().writeAndFlush(CField.finalAttackRequest(attackCount, attack.skill, eff.getSourceId(), (weapon.getItemId() - 1000000) / 10000, monster));
               }
               break;
            }
         }
      }

   }

   public static final void applyAttackMagic(AttackInfo attack, Skill theSkill, MapleCharacter player, SecondaryStatEffect effect, double maxDamagePerHit) {
      if (attack.summonattack == 0) {
      }

      player.checkSpecialCoreSkills("prob", 0, effect);
      int up;
      int i;
      if (attack.skill != 0) {
         if (effect == null) {
            player.getClient().getSession().writeAndFlush(CWvsContext.enableActions(player));
            return;
         }

         player.checkSpecialCoreSkills("cooltime", 0, effect);
         if (GameConstants.isMulungSkill(attack.skill)) {
            if (player.getMapId() / 10000 != 92502) {
               return;
            }

            if (player.getMulungEnergy() < 10000) {
               return;
            }
         } else if (GameConstants.isPyramidSkill(attack.skill)) {
            if (player.getMapId() / 1000000 != 926) {
               return;
            }
         } else if (GameConstants.isInflationSkill(attack.skill)) {
            if (player.getBuffedValue(SecondaryStat.Inflation) == null) {
               return;
            }
         } else if (!GameConstants.isNoApplySkill(attack.skill)) {
            SecondaryStatEffect oldEffect = SkillFactory.getSkill(attack.skill).getEffect(attack.skilllevel);
            int target = oldEffect.getMobCount();
            Iterator var12 = player.getSkills().keySet().iterator();

            int bulletBonus;
            while(var12.hasNext()) {
               Skill skill = (Skill)var12.next();
               bulletBonus = player.getSkillLevel(skill);
               if (bulletBonus > 0 && skill.getId() != attack.skill) {
                  SecondaryStatEffect bonusEffect = skill.getEffect(bulletBonus);
                  target += bonusEffect.getTargetPlus();
                  target += bonusEffect.getTargetPlus_5th();
               }
            }

            if (oldEffect.getMobCount() > 0 && player.getSkillLevel(70000047) > 0) {
               target += SkillFactory.getSkill(70000047).getEffect(player.getSkillLevel(70000047)).getTargetPlus();
            }

            boolean useBulletCount = oldEffect.getBulletCount() > 1;
            int attackCount = useBulletCount ? oldEffect.getBulletCount() : oldEffect.getAttackCount();
            bulletBonus = GameConstants.bullet_count_bonus(attack.skill);
            int attackBonus = GameConstants.attack_count_bonus(attack.skill);
            if (bulletBonus != 0 && useBulletCount) {
               if (player.getSkillLevel(bulletBonus) > 0) {
                  attackCount += SkillFactory.getSkill(bulletBonus).getEffect(player.getSkillLevel(bulletBonus)).getBulletCount();
               }
            } else if (attackBonus != 0 && !useBulletCount && player.getSkillLevel(attackBonus) > 0) {
               attackCount += SkillFactory.getSkill(attackBonus).getEffect(player.getSkillLevel(attackBonus)).getAttackCount();
            }

            Integer plusCount;
            if ((plusCount = player.getBuffedValue(SecondaryStat.Buckshot)) != null) {
               attackCount *= plusCount;
            }

            if (player.getBuffedEffect(SecondaryStat.ShadowPartner) != null || player.getBuffedEffect(SecondaryStat.Larkness) != null) {
               attackCount *= 2;
            }

            if (player.getSkillLevel(3220015) > 0 && attackCount >= 2) {
               attackCount += SkillFactory.getSkill(3220015).getEffect(player.getSkillLevel(3220015)).getX();
            }

            if (player.getBuffedEffect(SecondaryStat.VengeanceOfAngel) != null && attack.skill == 2321007) {
               attackCount += player.getBuffedEffect(SecondaryStat.VengeanceOfAngel).getY();
            }

            Integer attackCountX = player.getBuffedValue(SecondaryStat.AttackCountX);
            int[] blowSkills = new int[]{32001000, 32101000, 32111002, 32121002, 400021007};
            if (attackCountX != null) {
               int[] var18 = blowSkills;
               int var19 = blowSkills.length;

               for(up = 0; up < var19; ++up) {
                  i = var18[up];
                  if (attack.skill == i) {
                     attackCount += attackCountX;
                  }
               }
            }

            if (attack.targets > target) {
               player.dropMessageGM(-5, attack.skill + " 몹 개체수 > 클라이언트 계산 : " + attack.targets + " / 서버 계산 : " + target);
               player.dropMessageGM(-6, "개체수가 계산값보다 많습니다.");
            }

            if (attack.hits > attackCount) {
               player.dropMessageGM(-5, attack.skill + " 공격 횟수 > 클라이언트 계산 : " + attack.hits + " / 서버 계산 : " + attackCount);
               player.dropMessageGM(-6, "공격 횟수가 계산값보다 많습니다.");
            }
         }
      }

      MapleMonster monster = null;
      PlayerStats stats = player.getStat();
      if (player.getBuffedValue(SecondaryStat.ElementalReset) != null) {
         Element var10000 = Element.NEUTRAL;
      } else {
         theSkill.getElement();
      }

      double MaxDamagePerHit = 0.0D;
      long totDamage = 0L;
      boolean heiz = false;
      int multikill = 0;
      short CriticalDamage = stats.critical_rate;
      MapleMap map = player.getMap();
      Iterator var50 = attack.allDamage.iterator();

      while(true) {
         long totDamageToOneMonster;
         do {
            MapleMonsterStats monsterstats;
            long fixeddmg;
            byte overallAttackCount;
            AttackPair mon;
            do {
               do {
                  do {
                     if (!var50.hasNext()) {
                        SecondaryStatEffect iceAge;
                        MapleMist mist;
                        if (attack.skill == 400021096 && !monster.isAlive()) {
                           iceAge = SkillFactory.getSkill(400021104).getEffect(player.getSkillLevel(400021096));
                           mist = new MapleMist(iceAge.calculateBoundingBox(player.getPosition(), player.isFacingLeft()), player, iceAge, iceAge.getDuration(), (byte)(player.isFacingLeft() ? 1 : 0));
                           mist.setPosition(monster.getPosition());
                           mist.setDelay(0);
                           player.getMap().spawnMist(mist, false);
                        }

                        if (player.getBuffedValue(SecondaryStat.OverloadMana) != null && !GameConstants.is_forceAtom_attack_skill(attack.skill) && !effect.isMist()) {
                           if (GameConstants.isKinesis(player.getJob())) {
                              player.addHP((long)((int)(-(player.getStat().getCurrentMaxHp() * (long)player.getBuffedEffect(SecondaryStat.OverloadMana).getY() / 100L))));
                           } else {
                              player.addMP((long)((int)(-(player.getStat().getCurrentMaxMp(player) * (long)player.getBuffedEffect(SecondaryStat.OverloadMana).getX() / 100L))));
                           }
                        }

                        SecondaryStatEffect arcaneAim;
                        if (player.getSkillLevel(2120010) > 0 && (arcaneAim = SkillFactory.getSkill(2120010).getEffect(player.getSkillLevel(2120010))).makeChanceResult()) {
                           if (player.getArcaneAim() < 5) {
                              player.setArcaneAim(player.getArcaneAim() + 1);
                           }

                           arcaneAim.applyTo(player, false);
                        }

                        if (player.getSkillLevel(2220010) > 0 && (arcaneAim = SkillFactory.getSkill(2220010).getEffect(player.getSkillLevel(2220010))).makeChanceResult()) {
                           if (player.getArcaneAim() < 5) {
                              player.setArcaneAim(player.getArcaneAim() + 1);
                           }

                           arcaneAim.applyTo(player, false);
                        }

                        if (player.getSkillLevel(2320011) > 0 && (arcaneAim = SkillFactory.getSkill(2320011).getEffect(player.getSkillLevel(2320011))).makeChanceResult()) {
                           if (player.getArcaneAim() < 5) {
                              player.setArcaneAim(player.getArcaneAim() + 1);
                           }

                           arcaneAim.applyTo(player, false);
                        }

                        HashMap statups;
                        int i;
                        MapleAtom atom;
                        if (totDamage > 0L) {
                           if (player.getMapId() == 993000500) {
                              player.setFWolfDamage(player.getFWolfDamage() + totDamage);
                              player.setFWolfAttackCount(player.getFWolfAttackCount() + 1);
                           }

                           MFinalAttackRequest(player, attack.skill, monster);
                           if (attack.skill == 2321007) {
                              ++player.홀리워터;
                              if (player.홀리워터 == 7) {
                                 player.홀리워터 = 0;
                                 ++player.홀리워터스택;
                                 if (player.홀리워터스택 > 5) {
                                    player.홀리워터스택 = 5;
                                 }

                                 statups = new HashMap();
                                 statups.put(SecondaryStat.HolyWater, new Pair(player.홀리워터스택, 0));
                                 player.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, (SecondaryStatEffect)null, player));
                              }
                           }

                           if (!GameConstants.isLuminous(player.getJob())) {
                              if (GameConstants.isEvan(player.getJob())) {
                                 if (attack.isLink && player.getSkillLevel(22110016) > 0) {
                                    SkillFactory.getSkill(22110016).getEffect(player.getSkillLevel(22110016)).applyTo(player);
                                 }
                              } else if (GameConstants.isLara(player.getJob()) && attack.targets > 0 && (attack.skill == 162001000 || attack.skill == 162121021)) {
                                 if (player.getSkillLevel(162000003) > 0 && Randomizer.isSuccess(SkillFactory.getSkill(162000003).getEffect(player.getSkillLevel(162000003)).getProp())) {
                                    player.getClient().getSession().writeAndFlush(CField.rangeAttack(attack.skill, Arrays.asList(new RangeAttack(162001004, attack.position, 0, 0, 1))));
                                 }

                                 if (player.getBuffedValue(162121003)) {
                                    iceAge = SkillFactory.getSkill(162120002).getEffect(player.getSkillLevel(162120002));
                                    if (player.getSkillCustomValue(162121004) == null) {
                                       player.getClient().getSession().writeAndFlush(CField.rangeAttack(162121004, Arrays.asList(new RangeAttack(162121004, attack.position, 0, 0, 1))));
                                       player.setSkillCustomInfo(162121004, 0L, (long)((int)iceAge.getT() * 1000));
                                    }
                                 }

                                 if (player.getBuffedValue(162121006)) {
                                    iceAge = SkillFactory.getSkill(162120005).getEffect(player.getSkillLevel(162120005));
                                    if (player.getSkillCustomValue(162121007) == null) {
                                       player.getClient().getSession().writeAndFlush(CField.rangeAttack(162121007, Arrays.asList(new RangeAttack(162121007, attack.position, 0, 0, 1))));
                                       player.setSkillCustomInfo(162121007, 0L, (long)((int)iceAge.getT() * 1000));
                                    }
                                 }

                                 if (player.getBuffedValue(162121009)) {
                                    iceAge = SkillFactory.getSkill(162120008).getEffect(player.getSkillLevel(162120008));
                                    if (player.getSkillCustomValue(162121010) == null) {
                                       player.getClient().getSession().writeAndFlush(CField.rangeAttack(162121010, Arrays.asList(new RangeAttack(162121010, attack.position, 0, 0, 5))));
                                       player.setSkillCustomInfo(162121010, 0L, (long)((int)iceAge.getT() * 1000));
                                    }
                                 }
                              }
                           } else {
                              if ((player.getBuffedValue(20040216) || player.getBuffedValue(20040219) || player.getBuffedValue(20040220)) && (GameConstants.isLightSkills(attack.skill) || (player.getBuffedValue(20040219) || player.getBuffedValue(20040220)) && (attack.skill == 27121303 || attack.skill == 27111303))) {
                                 player.addHP(player.getStat().getMaxHp() / 100L);
                              }

                              SecondaryStatEffect dark;
                              if (player.getSkillLevel(27120005) > 0 && (dark = SkillFactory.getSkill(27120005).getEffect(player.getSkillLevel(27120005))).makeChanceResult()) {
                                 if (player.stackbuff < dark.getX()) {
                                    ++player.stackbuff;
                                 }

                                 dark.applyTo(player, false);
                              }

                              if (player.getBuffedValue(400021105) && (GameConstants.isLightSkills(attack.skill) || attack.skill == 27121303 || GameConstants.isDarkSkills(attack.skill)) && player.getSkillLevel(400021105) > 0 && player.getSkillCustomValue(400021109) == null) {
                                 iceAge = SkillFactory.getSkill(400021105).getEffect(player.getSkillLevel(400021105));
                                 i = 0;
                                 if (player.getSkillCustomValue0(400021105) == 2L) {
                                    i = 400021110;
                                 } else if (player.getSkillCustomValue0(400021105) == 1L) {
                                    i = 400021109;
                                 }

                                 player.getClient().getSession().writeAndFlush(CField.rangeAttack(400021105, Arrays.asList(new RangeAttack(i, attack.position, 1, 0, 1))));
                                 player.setSkillCustomInfo(400021109, 0L, (long)iceAge.getU2());
                                 player.addSkillCustomInfo(400021110, -1L);
                                 if (player.getSkillCustomValue0(400021110) <= 0L) {
                                    player.cancelEffect(iceAge);
                                 }
                              }

                              if (!player.getBuffedValue(20040216) && !player.getBuffedValue(20040217) && !player.getBuffedValue(20040219) && !player.getBuffedValue(20040220)) {
                                 if (GameConstants.isLightSkills(attack.skill)) {
                                    player.setLuminusMorphUse(1);
                                    SkillFactory.getSkill(20040217).getEffect(1).applyTo(player, false);
                                    player.setLuminusMorph(false);
                                 } else if (GameConstants.isDarkSkills(attack.skill)) {
                                    player.setLuminusMorphUse(9999);
                                    SkillFactory.getSkill(20040216).getEffect(1).applyTo(player, false);
                                    player.setLuminusMorph(true);
                                 }

                                 player.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.LuminusMorph(player.getLuminusMorphUse(), player.getLuminusMorph()));
                              } else if (!player.getBuffedValue(20040219) && !player.getBuffedValue(20040220)) {
                                 HashMap statups;
                                 if (player.getLuminusMorph()) {
                                    if (GameConstants.isLightSkills(attack.skill)) {
                                       if (!player.getBuffedValue(20040219) && !player.getBuffedValue(20040220) && !player.getBuffedValue(400021105) && GameConstants.isLightSkills(attack.skill) && player.getSkillLevel(400021105) > 0 && !player.skillisCooling(400021106)) {
                                          iceAge = SkillFactory.getSkill(400021105).getEffect(player.getSkillLevel(400021105));
                                          if (player.getSkillCustomValue0(400021107) < (long)iceAge.getU()) {
                                             player.setSkillCustomInfo(400021107, player.getSkillCustomValue0(400021107) + 1L, 0L);
                                          }

                                          player.getClient().getSession().writeAndFlush(CField.rangeAttack(400021105, Arrays.asList(new RangeAttack(400021107, attack.position, 1, 0, 1))));
                                          player.addCooldown(400021106, System.currentTimeMillis(), (long)(iceAge.getX() * 1000));
                                          player.getClient().getSession().writeAndFlush(CField.skillCooldown(400021106, iceAge.getX() * 1000));
                                          statups = new HashMap();
                                          statups.put(SecondaryStat.LiberationOrb, new Pair(1, 0));
                                          player.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, iceAge, player));
                                       }

                                       if (player.getLuminusMorphUse() - GameConstants.isLightSkillsGaugeCheck(attack.skill) <= 0) {
                                          if (player.getSkillLevel(20040219) > 0) {
                                             player.cancelEffectFromBuffStat(SecondaryStat.Larkness);
                                             SkillFactory.getSkill(20040219).getEffect(1).applyTo(player, false);
                                             player.setUseTruthDoor(false);
                                          } else {
                                             player.cancelEffectFromBuffStat(SecondaryStat.Larkness);
                                             player.setLuminusMorph(false);
                                             SkillFactory.getSkill(20040217).getEffect(1).applyTo(player, false);
                                          }
                                       } else {
                                          player.setLuminusMorphUse(player.getLuminusMorphUse() - GameConstants.isLightSkillsGaugeCheck(attack.skill));
                                       }

                                       if (!player.getBuffedValue(20040219) && !player.getBuffedValue(20040220) && player.getLuminusMorph()) {
                                          player.cancelEffectFromBuffStat(SecondaryStat.Larkness);
                                          SkillFactory.getSkill(20040216).getEffect(1).applyTo(player, false);
                                       }
                                    }
                                 } else if (GameConstants.isDarkSkills(attack.skill)) {
                                    if (!player.getBuffedValue(400021105) && GameConstants.isDarkSkills(attack.skill) && player.getSkillLevel(400021105) > 0 && !player.skillisCooling(400021106)) {
                                       iceAge = SkillFactory.getSkill(400021105).getEffect(player.getSkillLevel(400021105));
                                       if (player.getSkillCustomValue0(400021108) < (long)iceAge.getU()) {
                                          player.setSkillCustomInfo(400021108, player.getSkillCustomValue0(400021108) + 1L, 0L);
                                       }

                                       player.getClient().getSession().writeAndFlush(CField.rangeAttack(400021105, Arrays.asList(new RangeAttack(400021108, attack.position, 1, 0, 1))));
                                       player.addCooldown(400021106, System.currentTimeMillis(), (long)(iceAge.getX() * 1000));
                                       player.getClient().getSession().writeAndFlush(CField.skillCooldown(400021106, iceAge.getX() * 1000));
                                       statups = new HashMap();
                                       statups.put(SecondaryStat.LiberationOrb, new Pair(1, 0));
                                       player.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, iceAge, player));
                                    }

                                    if (player.getLuminusMorphUse() + GameConstants.isDarkSkillsGaugeCheck(player, attack.skill) >= 10000) {
                                       if (player.getSkillLevel(20040219) > 0) {
                                          player.cancelEffectFromBuffStat(SecondaryStat.Larkness);
                                          SkillFactory.getSkill(20040220).getEffect(1).applyTo(player, false);
                                          player.setUseTruthDoor(false);
                                       } else {
                                          player.cancelEffectFromBuffStat(SecondaryStat.Larkness);
                                          player.setLuminusMorph(true);
                                          SkillFactory.getSkill(20040216).getEffect(1).applyTo(player, false);
                                       }
                                    } else {
                                       player.setLuminusMorphUse(player.getLuminusMorphUse() + GameConstants.isDarkSkillsGaugeCheck(player, attack.skill));
                                    }

                                    if (!player.getBuffedValue(20040219) && !player.getBuffedValue(20040220) && !player.getLuminusMorph()) {
                                       player.cancelEffectFromBuffStat(SecondaryStat.Larkness);
                                       SkillFactory.getSkill(20040217).getEffect(1).applyTo(player, false);
                                    }
                                 }

                                 player.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.LuminusMorph(player.getLuminusMorphUse(), player.getLuminusMorph()));
                              }
                           }

                           if (attack.targets > 0 && player.getBuffedEffect(SecondaryStat.Triumph) != null && attack.skill != 2311017 && System.currentTimeMillis() - player.TriumphTime >= 2000L) {
                              List<MapleMapObject> objs = player.getMap().getMapObjectsInRange(player.getTruePosition(), 500000.0D, Arrays.asList(MapleMapObjectType.MONSTER));
                              if (objs.size() > 0) {
                                 List<SecondAtom> atoms = new ArrayList();
                                 atoms.add(new SecondAtom(37, player.getId(), monster.getId(), 2311017, 4000, 0, 1, new Point((int)player.getTruePosition().getX(), (int)player.getTruePosition().getY() - 120), Arrays.asList(0)));
                                 atoms.add(new SecondAtom(37, player.getId(), monster.getId(), 2311017, 4000, 0, 1, new Point((int)player.getTruePosition().getX() + 20, (int)player.getTruePosition().getY() - 116), Arrays.asList(0)));
                                 atoms.add(new SecondAtom(37, player.getId(), monster.getId(), 2311017, 4000, 0, 1, new Point((int)player.getTruePosition().getX() - 20, (int)player.getTruePosition().getY() - 120), Arrays.asList(0)));
                                 atoms.add(new SecondAtom(37, player.getId(), monster.getId(), 2311017, 4000, 0, 1, new Point((int)player.getTruePosition().getX() + 10, (int)player.getTruePosition().getY() - 116), Arrays.asList(0)));
                                 atoms.add(new SecondAtom(37, player.getId(), monster.getId(), 2311017, 4000, 0, 1, new Point((int)player.getTruePosition().getX() - 10, (int)player.getTruePosition().getY() - 120), Arrays.asList(0)));
                                 player.spawnSecondAtom(atoms);
                                 player.TriumphTime = System.currentTimeMillis();
                              }
                           }

                           if (attack.skill == 27121303 && player.getSkillLevel(400021071) > 0) {
                              boolean give = false;
                              if (player.getPerfusion() < SkillFactory.getSkill(400021071).getEffect(player.getSkillLevel(400021071)).getX() - 1) {
                                 give = true;
                              } else if (player.getPerfusion() >= SkillFactory.getSkill(400021071).getEffect(player.getSkillLevel(400021071)).getX() - 1 && player.skillisCooling(400021071)) {
                                 give = true;
                              }

                              if (give) {
                                 SkillFactory.getSkill(400021071).getEffect(player.getSkillLevel(400021071)).applyTo(player, false);
                              }
                           } else if (!GameConstants.isKinesis(player.getJob())) {
                              if (GameConstants.isIllium(player.getJob()) && attack.skill == 400021061 && attack.targets > 0) {
                                 SkillFactory.getSkill(152000009).getEffect(player.getSkillLevel(152000009)).applyTo(player, false);
                              }
                           } else {
                              if (player.getSkillLevel(142110011) > 0 && attack.skill != 142110011 && !attack.allDamage.isEmpty()) {
                                 switch(attack.skill) {
                                 case 142001000:
                                 case 142001002:
                                 case 142100000:
                                 case 142100001:
                                 case 142101003:
                                 case 142101009:
                                 case 142110000:
                                 case 142110001:
                                 case 142111007:
                                 case 142120002:
                                 case 142120030:
                                 case 142121005:
                                 case 142121030:
                                    break;
                                 default:
                                    atom = new MapleAtom(false, player.getId(), 22, true, 142110011, player.getTruePosition().x, player.getTruePosition().y);

                                    for(i = 0; i < attack.targets; ++i) {
                                       if (SkillFactory.getSkill(142110011).getEffect(player.getSkillLevel(142110011)).makeChanceResult() && attack.skill != 142001000 && attack.skill != 142100000 && attack.skill != 142110000) {
                                          atom.addForceAtom(new ForceAtom(0, 21, 9, 68, 960));
                                       }
                                    }

                                    if (!atom.getForceAtoms().isEmpty()) {
                                       atom.setDwFirstTargetId(0);
                                       player.getMap().spawnMapleAtom(atom);
                                    }
                                 }
                              }

                              if (attack.skill == 142121004) {
                                 up = 0;
                                 Iterator var58 = attack.allDamage.iterator();

                                 while(var58.hasNext()) {
                                    AttackPair att = (AttackPair)var58.next();
                                    MapleMonster m = MapleLifeFactory.getMonster(att.monsterId);
                                    if (m != null) {
                                       up += m.getStats().isBoss() ? effect.getW() : effect.getIndiePmdR();
                                    }
                                 }

                                 if (player.getSkillLevel(142120041) > 0) {
                                    up *= 2;
                                 }

                                 player.setSkillCustomInfo(142121004, (long)up, 0L);
                                 if (player.getSkillCustomValue0(142121004) >= (long)effect.getW()) {
                                    player.setSkillCustomInfo(142121004, (long)effect.getW(), 0L);
                                 }

                                 SkillFactory.getSkill(142121004).getEffect(player.getSkillLevel(142121004)).applyTo(player);
                              }

                              if (attack.skill == 400021075 && monster != null) {
                                 player.givePPoint((byte)1);
                              } else if (attack.skill == 142121005) {
                                 player.givePPoint((byte)-1);
                              }
                           }

                           if (player.getBuffedValue(32101009) && player.getSkillCustomValue(32111119) == null && player.getId() == player.getBuffedOwner(32101009)) {
                              player.addHP(totDamage / 100L * (long)player.getBuffedEffect(32101009).getX());
                              player.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(player, 0, 32101009, 10, 0, 0, (byte)(player.isFacingLeft() ? 1 : 0), true, player.getTruePosition(), (String)null, (Item)null));
                              player.getMap().broadcastMessage(player, CField.EffectPacket.showEffect(player, 0, 32101009, 10, 0, 0, (byte)(player.isFacingLeft() ? 1 : 0), false, player.getTruePosition(), (String)null, (Item)null), false);
                              player.setSkillCustomInfo(32111119, 0L, 5000L);
                              if (player.getParty() != null) {
                                 var50 = player.getParty().getMembers().iterator();

                                 while(var50.hasNext()) {
                                    MaplePartyCharacter pc = (MaplePartyCharacter)var50.next();
                                    MapleCharacter chr;
                                    if (pc.getId() != player.getId() && pc.isOnline() && (chr = player.getClient().getChannelServer().getPlayerStorage().getCharacterById(pc.getId())) != null && chr.getBuffedValue(32101009) && chr.getId() != player.getId()) {
                                       chr.addHP(totDamage / 100L * (long)player.getBuffedEffect(32101009).getX());
                                       if (chr.getDisease(SecondaryStat.GiveMeHeal) != null) {
                                          chr.cancelDisease(SecondaryStat.GiveMeHeal);
                                       }

                                       chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, 32101009, 10, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), true, chr.getTruePosition(), (String)null, (Item)null));
                                       chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 0, 32101009, 10, 0, 0, (byte)(chr.isFacingLeft() ? 1 : 0), false, chr.getTruePosition(), (String)null, (Item)null), false);
                                    }
                                 }
                              }
                           }
                        }

                        if (player.getBuffedValue(400021092) && player.getSkillCustomValue0(400021092) != 1L) {
                           MapleSummon sum = player.getSummon(400021092);
                           mon = null;
                           List<MapleMapObject> objs = player.getMap().getMapObjectsInRange(player.getTruePosition(), 800000.0D, Arrays.asList(MapleMapObjectType.MONSTER));
                           MapleMonster mon = player.getMap().getMonsterByOid(((MapleMapObject)objs.get(Randomizer.nextInt(objs.size()))).getObjectId());
                           if (sum != null && mon != null) {
                              player.setGraveTarget(mon.getObjectId());
                              player.createSecondAtom(SkillFactory.getSkill(400021092).getSecondAtoms(), sum.getPosition());
                              player.getMap().broadcastMessage(CField.SummonPacket.updateSummon(sum, 99));
                              player.setSkillCustomInfo(400021092, 1L, 0L);
                           }
                        }

                        MapleSummon summon;
                        if (totDamage > 0L && player.getBuffedValue(400021073) && (summon = player.getSummon(400021073)) != null && summon.getEnergy() < 22) {
                           switch(attack.skill) {
                           case 22110014:
                           case 22110024:
                           case 22110025:
                           case 22111011:
                           case 22140014:
                           case 22140015:
                           case 22140023:
                           case 22140024:
                           case 22141011:
                           case 22170064:
                           case 22170065:
                           case 22170066:
                           case 22170067:
                           case 22170093:
                           case 22170094:
                           case 22171063:
                           case 22171083:
                           case 22171095:
                           case 400021013:
                              if (!summon.getMagicSkills().contains(attack.skill)) {
                                 summon.getMagicSkills().add(attack.skill);
                                 summon.setEnergy(Math.min(22, summon.getEnergy() + 3));
                                 atom = new MapleAtom(true, summon.getObjectId(), 29, true, 400021073, summon.getTruePosition().x, summon.getTruePosition().y);
                                 atom.setDwUserOwner(summon.getOwner().getId());
                                 atom.setDwFirstTargetId(0);
                                 atom.addForceAtom(new ForceAtom(5, 37, Randomizer.rand(5, 10), 62, 0));
                                 player.getMap().spawnMapleAtom(atom);
                                 player.getClient().getSession().writeAndFlush(CField.SummonPacket.ElementalRadiance(summon, 2));
                                 player.getClient().getSession().writeAndFlush(CField.SummonPacket.specialSummon(summon, 2));
                                 if (summon.getEnergy() >= 22) {
                                    player.getClient().getSession().writeAndFlush(CField.SummonPacket.damageSummon(summon));
                                 }
                              }
                              break;
                           case 22110022:
                           case 22110023:
                           case 22111012:
                           case 22170060:
                           case 22170070:
                           case 400021012:
                           case 400021014:
                           case 400021015:
                              atom = new MapleAtom(true, summon.getObjectId(), 29, true, 400021073, summon.getTruePosition().x, summon.getTruePosition().y);
                              atom.setDwUserOwner(summon.getOwner().getId());
                              atom.setDwFirstTargetId(0);
                              atom.addForceAtom(new ForceAtom(5, 37, Randomizer.rand(5, 10), 62, 0));
                              player.getMap().spawnMapleAtom(atom);
                              player.getClient().getSession().writeAndFlush(CField.SummonPacket.ElementalRadiance(summon, 2));
                              player.getClient().getSession().writeAndFlush(CField.SummonPacket.specialSummon(summon, 2));
                              if (summon.getEnergy() >= 22) {
                                 player.getClient().getSession().writeAndFlush(CField.SummonPacket.damageSummon(summon));
                              }
                           }
                        }

                        if (player.getBuffedValue(400001050) && player.getSkillCustomValue0(400001050) == 400001055L) {
                           iceAge = SkillFactory.getSkill(400001050).getEffect(player.getSkillLevel(400001050));
                           player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(400001055, new ArrayList(), true, 0));
                           player.removeSkillCustomInfo(400001050);
                           long duration = player.getBuffLimit(400001050);
                           iceAge.applyTo(player, false, (int)duration);
                        }

                        if (attack.skill == 2121003) {
                           var50 = player.getMap().getAllMistsThreadsafe().iterator();

                           while(var50.hasNext()) {
                              mist = (MapleMist)var50.next();
                              if (mist.getSource() != null && mist.getSource().getSourceId() == 2111003) {
                                 player.getMap().removeMist(mist.getSource().getSourceId());
                                 if (player.getCooldownLimit(2121011) > 0L) {
                                    player.removeCooldown(2121011);
                                 }
                              }
                           }
                        }

                        if (totDamage > 0L && attack.skill >= 400021013 && attack.skill <= 400021016) {
                           SkillFactory.getSkill(400021012).getEffect(attack.skilllevel).applyTo(player, false);
                        }

                        SecondaryStatEffect stst;
                        if (player.getSkillLevel(80002762) > 0 && (stst = SkillFactory.getSkill(80002762).getEffect(player.getSkillLevel(80002762))).makeChanceResult()) {
                           stst.applyTo(player, false);
                        }

                        if (player.getSkillLevel(150010241) > 0 && player.getSkillCustomValue(80000514) == null) {
                           SkillFactory.getSkill(150010241).getEffect(player.getSkillLevel(150010241)).applyTo(player);
                           player.setSkillCustomInfo(80000514, 0L, 3000L);
                        } else if (player.getSkillLevel(80000514) > 0 && player.getSkillCustomValue(80000514) == null) {
                           SkillFactory.getSkill(80000514).getEffect(player.getSkillLevel(80000514)).applyTo(player);
                           player.setSkillCustomInfo(80000514, 0L, 3000L);
                        }

                        if (attack.skill == 152121007 && player.getBuffedEffect(152111003) != null) {
                           player.canUseMortalWingBeat = false;
                           statups = new HashMap();
                           statups.put(SecondaryStat.GloryWing, new Pair(1, (int)player.getBuffLimit(152111003)));
                           player.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, player.getBuffedEffect(152111003), player));
                        }

                        if (attack.skill == 400021012) {
                           ArrayList<RangeAttack> skills = new ArrayList();
                           skills.add(new RangeAttack(400021013, attack.position, 0, 0, 0));
                           skills.add(new RangeAttack(400021014, attack.position, 0, 0, 0));
                           skills.add(new RangeAttack(400021015, attack.position, 0, 0, 0));
                           player.getClient().getSession().writeAndFlush(CField.rangeAttack(attack.skill, skills));
                        }

                        if (player.getBuffedEffect(SecondaryStat.CrystalGate) != null) {
                           iceAge = player.getBuffedEffect(SecondaryStat.CrystalGate);
                           if ((double)(System.currentTimeMillis() - player.lastCrystalGateTime) >= iceAge.getT() * 1000.0D) {
                              player.lastCrystalGateTime = System.currentTimeMillis();
                              player.getClient().getSession().writeAndFlush(CField.bonusAttackRequest(400021111, new ArrayList(), true, 0));
                           }
                        }

                        if (player.getBuffedValue(80002890) && attack.skill != 80002890 && attack.skill != 80002890 && System.currentTimeMillis() - player.lastThunderTime >= (long)player.getBuffedEffect(80002890).getCooldown(player)) {
                           player.lastThunderTime = System.currentTimeMillis();
                           player.getClient().getSession().writeAndFlush(CField.rangeAttack(80002890, Arrays.asList(new RangeAttack(80002890, attack.position, 0, 0, 1))));
                        }

                        Rectangle Rectanglebox2;
                        if (GameConstants.isFPMage(player.getJob()) && (SkillFactory.getSkill(2111013).getSkillList2().contains(attack.skill) || attack.skill == 2111014)) {
                           Rectangle Rectanglebox1 = effect.calculateBoundingBox(attack.position, player.isFacingLeft());
                           Rectanglebox2 = effect.calculateBoundingBox(attack.position, player.isFacingLeft());
                           if (attack.skill == 2111014) {
                              Rectanglebox1 = effect.calculateBoundingBox(new Point(attack.position.x, attack.position.y + 170), true, 100);
                              Rectanglebox2 = effect.calculateBoundingBox(new Point(attack.position.x, attack.position.y + 170), false, 100);
                           }

                           Iterator var72 = player.getMap().getAllMistsThreadsafe().iterator();

                           label1087:
                           while(true) {
                              MapleMist mist;
                              do {
                                 do {
                                    if (!var72.hasNext()) {
                                       break label1087;
                                    }

                                    mist = (MapleMist)var72.next();
                                 } while(mist.getSourceSkill().getId() != 2111013);
                              } while(!Rectanglebox1.contains(mist.getPosition()) && !Rectanglebox2.contains(mist.getPosition()));

                              if (mist.getStartTime() + 1500L < System.currentTimeMillis()) {
                                 player.getClient().getSession().writeAndFlush(CField.rangeAttackTest(2111013, attack.skill, mist.getObjectId(), Arrays.asList(new RangeAttack(2111014, new Point(mist.getPosition().x, mist.getPosition().y - 170), 0, 240, 1))));
                                 Timer.MapTimer.getInstance().schedule(() -> {
                                    player.getMap().removeMist(mist);
                                 }, 300L);
                              }
                           }
                        }

                        if (attack.skill == 400021002) {
                           iceAge = SkillFactory.getSkill(400020002).getEffect(player.getSkillLevel(400021002));
                           Rectanglebox2 = effect.calculateBoundingBox(player.getTruePosition(), player.isFacingLeft());

                           for(i = 0; i < player.getMap().getFootholds().getAllRelevants().size(); ++i) {
                              MapleFoothold fh = (MapleFoothold)player.getMap().getFootholds().getAllRelevants().get(i);
                              int rx = fh.getPoint2().x - fh.getPoint1().x;
                              if (Rectanglebox2.contains(fh.getPoint1()) || Rectanglebox2.contains(fh.getPoint2())) {
                                 if (rx / 200 > 1) {
                                    for(int i2 = 0; i2 <= rx / 200; ++i2) {
                                       boolean active = true;
                                       if (active) {
                                          iceAge.applyTo(player, false, new Point(fh.getPoint1().x + i2 * 200, fh.getPoint1().y + 30));
                                       }
                                    }
                                 } else {
                                    boolean active = true;
                                    Iterator var75 = player.getMap().getAllMistsThreadsafe().iterator();

                                    while(var75.hasNext()) {
                                       MapleMist mist = (MapleMist)var75.next();
                                       if (mist.getPosition().x - 200 < fh.getPoint1().x && mist.getPosition().x + 200 > fh.getPoint2().x && fh.getPoint1().y - 70 < mist.getPosition().y && fh.getPoint1().y + 70 > mist.getPosition().y) {
                                          active = false;
                                          break;
                                       }

                                       if (mist.getBox().contains(fh.getPoint1()) || mist.getBox().contains(fh.getPoint2())) {
                                          active = false;
                                          break;
                                       }
                                    }

                                    if (active) {
                                       iceAge.applyTo(player, false, new Point(fh.getPoint1().x, fh.getPoint1().y + 30));
                                    }
                                 }
                              }
                           }
                        }

                        if (multikill > 0) {
                           player.CombokillHandler(monster, 1, multikill);
                        }

                        return;
                     }

                     mon = (AttackPair)var50.next();
                     monster = map.getMonsterByOid(mon.objectId);
                  } while(monster == null);
               } while(monster.getLinkCID() > 0);

               boolean Tempest = false;
               totDamageToOneMonster = 0L;
               monsterstats = monster.getStats();
               fixeddmg = (long)monsterstats.getFixedDamage();
               overallAttackCount = 0;
            } while(monster.getId() >= 9833070 && monster.getId() <= 9833074);

            Iterator var29 = mon.attack.iterator();

            while(var29.hasNext()) {
               Pair<Long, Boolean> eachde = (Pair)var29.next();
               long eachd = (Long)eachde.left;
               ++overallAttackCount;
               if (fixeddmg != -1L) {
                  eachd = monsterstats.getOnlyNoramlAttack() ? 0L : fixeddmg;
               } else if (monsterstats.getOnlyNoramlAttack()) {
                  eachd = 0L;
               }

               totDamageToOneMonster += eachd;
               player.checkSpecialCoreSkills("attackCount", monster.getObjectId(), effect);
               player.checkSpecialCoreSkills("attackCountMob", monster.getObjectId(), effect);
            }

            totDamage += totDamageToOneMonster;
            if (!player.gethottimebossattackcheck()) {
               player.sethottimebossattackcheck(true);
            }

            if (monster.getId() != 8900002 && monster.getId() != 8900102) {
               player.checkMonsterAggro(monster);
            }

            if (attack.skill != 0 && !SkillFactory.getSkill(attack.skill).isChainAttack() && !effect.isMist() && effect.getSourceId() != 400021030 && !GameConstants.isLinkedSkill(attack.skill) && !GameConstants.isNoApplySkill(attack.skill) && !GameConstants.isNoDelaySkill(attack.skill) && !monster.getStats().isBoss() && player.getTruePosition().distanceSq(monster.getTruePosition()) > GameConstants.getAttackRange(effect, player.getStat().defRange)) {
               player.dropMessageGM(-5, "타겟이 범위를 벗어났습니다.");
            }

            if (player.getSkillLevel(80002762) > 0) {
               if (player.getBuffedEffect(SecondaryStat.EmpiricalKnowledge) != null && player.empiricalKnowledge != null) {
                  if (map.getMonsterByOid(player.empiricalKnowledge.getObjectId()) != null) {
                     if (monster.getObjectId() != player.empiricalKnowledge.getObjectId() && monster.getMobMaxHp() > player.empiricalKnowledge.getMobMaxHp()) {
                        player.empiricalStack = 0;
                        player.empiricalKnowledge = monster;
                     }
                  } else {
                     player.empiricalStack = 0;
                     player.empiricalKnowledge = monster;
                  }
               } else if (player.empiricalKnowledge != null) {
                  if (monster.getMobMaxHp() > player.empiricalKnowledge.getMobMaxHp()) {
                     player.empiricalKnowledge = monster;
                  }
               } else {
                  player.empiricalKnowledge = monster;
               }
            }
         } while(totDamageToOneMonster <= 0L && attack.skill != 27101101);

         monster.damage(player, totDamageToOneMonster, true, attack.skill);
         if (monster.getId() >= 9500650 && monster.getId() <= 9500654 && totDamageToOneMonster > 0L && player.getGuild() != null) {
            player.getGuild().updateGuildScore(totDamageToOneMonster);
         }

         if ((!GameConstants.사출기(attack.skill) || player.getMapId() == 921170004 || player.getMapId() == 921170011) && (attack.targets > 0 && player.getKeyValue(99999, "tripling") > 0L && attack.skill != 1311020 && !GameConstants.isTryFling(attack.skill) && attack.skill != 400031031 && attack.skill != 400031001 && attack.skill != 13111020 && attack.skill != 13121054 && attack.skill != 13101022 && attack.skill != 13110022 && attack.skill != 13120003 && attack.skill != 13111020 && attack.skill != 400001018 || player.getMapId() == 921170004 || player.getMapId() == 921170011 || attack.targets > 0 && attack.skill != 1311020 && player.getBuffedEffect(SecondaryStat.TryflingWarm) != null && !GameConstants.isTryFling(attack.skill) && attack.skill != 400031031 && attack.skill != 400031001 && attack.skill != 13111020 && attack.skill != 13121054 && attack.skill != 13101022 && attack.skill != 13110022 && attack.skill != 13120003 && attack.skill != 13111020 && attack.skill != 400001018 && attack.targets > 0)) {
            int skillid = 0;
            if (player.getKeyValue(99999, "tripling") <= 0L && player.getSkillLevel(SkillFactory.getSkill(13120003)) <= 0) {
               if (player.getSkillLevel(SkillFactory.getSkill(13110022)) > 0) {
                  skillid = 13110022;
               } else if (player.getSkillLevel(SkillFactory.getSkill(13101022)) > 0) {
                  skillid = 13100022;
               }
            } else {
               skillid = 13120003;
               if (player.getSkillLevel(SkillFactory.getSkill(13120003)) < 30) {
                  player.teachSkill(13120003, 30);
               }
            }

            if (skillid != 0) {
               Skill trskill = SkillFactory.getSkill(skillid);
               if (Randomizer.rand(1, 100) <= (skillid == 13100022 ? 5 : (skillid == 13110022 ? 10 : 20))) {
                  if (skillid == 13120003) {
                     int var101 = 13120010;
                  } else {
                     skillid = skillid == 13110022 ? 13110027 : 13100027;
                  }

                  if (player.getSkillLevel(skillid) <= 0) {
                     player.changeSkillLevel(SkillFactory.getSkill(skillid), (byte)player.getSkillLevel(trskill), (byte)player.getSkillLevel(trskill));
                  }
               }

               List<MapleMapObject> objs = player.getMap().getMapObjectsInRange(player.getTruePosition(), 500000.0D, Arrays.asList(MapleMapObjectType.MONSTER));
               SecondaryStatEffect eff = trskill.getEffect(player.getSkillLevel(skillid));
               int maxcount = eff.getX() + (int)(player.getKeyValue(99999, "triplingBonus") > 0L ? player.getKeyValue(99999, "triplingBonus") * 1L : 0L);
               if (objs.size() > 0) {
                  int trychance = 100;
                  if (player.getSkillLevel(13120044) > 0) {
                     trychance += SkillFactory.getSkill(13120044).getEffect(1).getProp();
                  }

                  if (attack.skill == 400031004 || attack.skill == 400031003) {
                     trychance /= 2;
                  }

                  if (Randomizer.isSuccess(trychance) || player.getMapId() == 921170004 || player.getMapId() == 921170011) {
                     MapleAtom atom = new MapleAtom(false, player.getId(), 7, true, skillid, player.getTruePosition().x, player.getTruePosition().y);
                     ArrayList<Integer> monsters = new ArrayList();
                     if (player.getMapId() == 921170004 || player.getMapId() == 921170011) {
                        maxcount = 1;
                        if (player.자동사냥 < System.currentTimeMillis()) {
                           maxcount = 5;
                           player.자동사냥 = System.currentTimeMillis() + 30000L;
                        }
                     }

                     int i = 0;

                     while(true) {
                        if (i >= Randomizer.rand(1 + (int)(player.getKeyValue(99999, "triplingBonus") > 0L ? player.getKeyValue(99999, "triplingBonus") * 1L : 0L), maxcount)) {
                           atom.setDwTargets(monsters);
                           player.getMap().spawnMapleAtom(atom);
                           break;
                        }

                        boolean upgrade = Randomizer.isSuccess(eff.getSubprop());
                        monsters.add(((MapleMapObject)objs.get(Randomizer.nextInt(objs.size()))).getObjectId());
                        atom.addForceAtom(new ForceAtom(upgrade ? 3 : 1, Randomizer.rand(41, 49), Randomizer.rand(4, 8), Randomizer.nextBoolean() ? Randomizer.rand(171, 174) : Randomizer.rand(6, 9), (short)Randomizer.rand(42, 47)));
                        ++i;
                     }
                  }
               }
            }
         }

         if (player.getBuffedValue(400021073)) {
            new ArrayList();
            MapleSummon s = null;
            Iterator var89 = player.getSummons().iterator();

            while(var89.hasNext()) {
               MapleSummon summon2 = (MapleSummon)var89.next();
               if (summon2.getSkill() == 400021073) {
                  s = summon2;
               }
            }

            if (s == null) {
               player.dropMessage(6, "Zodiac Ray Null Point");
            } else {
               MapleAtom atom = new MapleAtom(true, monster.getObjectId(), 29, true, 400021073, monster.getTruePosition().x, monster.getTruePosition().y);
               atom.setDwUserOwner(player.getId());
               atom.setDwFirstTargetId(0);
               atom.addForceAtom(new ForceAtom(5, 37, Randomizer.rand(5, 10), 62, 0));
               player.getMap().spawnMapleAtom(atom);
               player.setEnergyBurst(player.getEnergyBurst() + 5);
               player.getClient().getSession().writeAndFlush(CField.SummonPacket.updateSummon(s, 13));
               HashMap<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
               statups.put(SecondaryStat.IndieSummon, new Pair(player.getEnergyBurst() + 21, (int)player.getBuffLimit(400021073)));
               player.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, player.getBuffedEffect(400021073), player));
               if (player.getEnergyBurst() > 22) {
                  player.getClient().getSession().writeAndFlush(CField.SummonPacket.damageSummon(s));
                  player.setEnergyBurst(0);
               }
            }
         }

         int by;
         if (GameConstants.isFusionSkill(attack.skill) && attack.targets > 0 && player.getSkillCustomValue(22170070) == null) {
            SecondaryStatEffect magicWreck = player.getSkillLevel(22170070) > 0 ? SkillFactory.getSkill(22170070).getEffect(player.getSkillLevel(22170070)) : SkillFactory.getSkill(22141017).getEffect(player.getSkillLevel(22141017));
            if (player.getMap().getWrecks().size() < 15) {
               by = Randomizer.rand(-100, 150);
               int y = Randomizer.rand(-50, 70);
               MapleMagicWreck mw = new MapleMagicWreck(player, magicWreck.getSourceId(), new Point(monster.getTruePosition().x + by, monster.getTruePosition().y + y), 20000);
               player.getMap().spawnMagicWreck(mw);
               player.setSkillCustomInfo(22170070, 0L, player.getSkillLevel(22170070) > 0 ? 400L : 600L);
            }
         }

         if (player.getSkillLevel(32101009) > 0 && !monster.isAlive() && player.getBuffedEffect(SecondaryStat.DebuffIncHp) == null) {
            player.addHP(player.getStat().getCurrentMaxHp() * (long)SkillFactory.getSkill(32101009).getEffect(player.getSkillLevel(32101009)).getKillRecoveryR() / 100L);
         }

         if (monster.isBuffed(MonsterStatus.MS_MCounter) && player.getBuffedEffect(SecondaryStat.IgnorePImmune) == null && player.getBuffedEffect(SecondaryStat.IgnorePCounter) == null && player.getBuffedEffect(SecondaryStat.IgnoreAllCounter) == null && player.getBuffedEffect(SecondaryStat.IgnoreAllImmune) == null && !SkillFactory.getSkill(attack.skill).isIgnoreCounter()) {
            player.addHP(-monster.getBuff(MonsterStatus.MS_MCounter).getValue());
         }

         String var10002 = SkillFactory.getSkill(attack.skill).getName();
         player.dropMessageGM(5, "매직 스킬(" + var10002 + ") : " + attack.skill);
         switch(attack.skill) {
         case 2101004:
         case 2111002:
         case 2121005:
         case 2121006:
         case 2121007:
         case 400021001:
            if (player.getBuffedEffect(SecondaryStat.WizardIgnite) != null && player.getBuffedEffect(SecondaryStat.WizardIgnite).makeChanceResult()) {
               SkillFactory.getSkill(2100010).getEffect(player.getSkillLevel(2101010)).applyTo(player, monster.getTruePosition());
            }
         }

         if (effect != null && monster.isAlive()) {
            ArrayList<Triple<MonsterStatus, MonsterStatusEffect, Long>> statusz = new ArrayList();
            ArrayList<Pair<MonsterStatus, MonsterStatusEffect>> applys = new ArrayList();
            boolean suc = effect.makeChanceResult();
            SecondaryStatEffect bonusTime;
            switch(attack.skill) {
            case 2101004:
            case 2111002:
            case 2121005:
            case 2121006:
            case 2121007:
               if (attack.skill == 2121006) {
                  statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getDuration()), 1L));
                  statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(attack.skill, effect.getDOTTime()), (long)effect.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L));
               }
               break;
            case 2101005:
               statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(attack.skill, effect.getDOTTime()), (long)effect.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L));
               break;
            case 2111003:
               if (!monster.isBuffed(2121011)) {
                  bonusTime = null;
                  SecondaryStatEffect bonusDam = null;
                  if (player.getSkillLevel(2120044) > 0) {
                     bonusTime = SkillFactory.getSkill(2120044).getEffect(player.getSkillLevel(2120044));
                  }

                  if (player.getSkillLevel(2120045) > 0) {
                     bonusDam = SkillFactory.getSkill(2120045).getEffect(player.getSkillLevel(2120045));
                  }

                  player.setDotDamage((long)effect.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L);
                  statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(attack.skill, effect.getDOTTime() + (bonusTime != null ? bonusTime.getDOTTime() : 0)), player.getDotDamage()));
               }
               break;
            case 2121011:
               player.setDotDamage((long)effect.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L);
               statusz.add(new Triple(MonsterStatus.MS_Showdown, new MonsterStatusEffect(attack.skill, effect.getDuration()), 1L));
               statusz.add(new Triple(MonsterStatus.MS_Speed, new MonsterStatusEffect(attack.skill, effect.getDuration()), (long)effect.getX()));
               statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(attack.skill, effect.getDOTTime()), player.getDotDamage()));
               player.setFlameHeiz(monster.getTruePosition());
               heiz = true;
               break;
            case 2121055:
               suc = true;
               statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(attack.skill, effect.getDOTTime()), (long)effect.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L));
               break;
            case 2201004:
            case 2201008:
            case 2201009:
            case 2211002:
            case 2211006:
            case 2211010:
            case 2220014:
            case 2221003:
            case 2221011:
            case 2221012:
            case 2221054:
            case 400020002:
               if (attack.skill != 2221011) {
                  statusz.add(new Triple(MonsterStatus.MS_Speed, new MonsterStatusEffect(attack.skill, 10000), (long)effect.getV()));
                  if (monster.getBuff(MonsterStatus.MS_Speed) == null && monster.getFreezingOverlap() > 0) {
                     monster.setFreezingOverlap(0);
                  }

                  if (monster.getFreezingOverlap() < 5) {
                     monster.setFreezingOverlap((byte)(monster.getFreezingOverlap() + 1));
                  }
               }

               if (attack.skill == 2221011) {
                  statusz.add(new Triple(MonsterStatus.MS_Freeze, new MonsterStatusEffect(attack.skill, 13000), 1L));
               }

               statusz.add(new Triple(MonsterStatus.MS_IndiePdr, new MonsterStatusEffect(attack.skill, 13000), (long)effect.getX()));
               statusz.add(new Triple(MonsterStatus.MS_IndieMdr, new MonsterStatusEffect(attack.skill, 13000), (long)effect.getY()));
               break;
            case 2201005:
            case 2211003:
            case 2211011:
            case 2221006:
               if (monster.getBuff(MonsterStatus.MS_Speed) == null && monster.getFreezingOverlap() > 0) {
                  monster.setFreezingOverlap(0);
               }

               if (monster.getFreezingOverlap() > 0) {
                  monster.setFreezingOverlap((byte)(monster.getFreezingOverlap() - 1));
                  if (monster.getFreezingOverlap() <= 0) {
                     monster.cancelStatus(MonsterStatus.MS_Speed, monster.getBuff(2201008));
                  } else {
                     statusz.add(new Triple(MonsterStatus.MS_Speed, new MonsterStatusEffect(2201008, 8000), -75L));
                  }
               }

               if (attack.skill == 2221006) {
                  statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getDuration()), 1L));
               }
               break;
            case 2221052:
            case 400021031:
            case 400021094:
               if (monster.getBuff(MonsterStatus.MS_Speed) == null && monster.getFreezingOverlap() > 0) {
                  monster.setFreezingOverlap(0);
               }

               if (attack.skill == 400021094) {
                  monster.addSkillCustomInfo(400021094, 1L);
                  if (monster.getFreezingOverlap() > 0) {
                     if (attack.skill == 400021094 && monster.getCustomValue0(400021094) >= 5L) {
                        monster.removeCustomInfo(400021094);
                        monster.setFreezingOverlap((byte)(monster.getFreezingOverlap() - 1));
                        statusz.add(new Triple(MonsterStatus.MS_Speed, new MonsterStatusEffect(2201008, 8000), -75L));
                     }
                  } else {
                     monster.cancelStatus(MonsterStatus.MS_Speed, monster.getBuff(2201008));
                  }
               }

               if (monster.getFreezingOverlap() > 0) {
                  monster.setFreezingOverlap((byte)(monster.getFreezingOverlap() - 1));
                  statusz.add(new Triple(MonsterStatus.MS_Speed, new MonsterStatusEffect(2201008, 8000), -75L));
               } else if (monster.getFreezingOverlap() <= 0) {
                  monster.cancelStatus(MonsterStatus.MS_Speed, monster.getBuff(2201008));
               }
               break;
            case 2311004:
               statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getDuration()), 1L));
               break;
            case 2321007:
               bonusTime = SkillFactory.getSkill(attack.skill).getEffect(player.getSkillLevel(attack.skill));
               if (monster.getBuff(MonsterStatus.MS_IndieUNK) == null) {
                  monster.removeCustomInfo(attack.skill);
               }

               if (monster.getCustomValue0(attack.skill) < (long)bonusTime.getQ()) {
                  monster.addSkillCustomInfo(attack.skill, 1L);
               }

               statusz.add(new Triple(MonsterStatus.MS_IndieUNK, new MonsterStatusEffect(attack.skill, 20000), monster.getCustomValue0(attack.skill)));
               break;
            case 12111022:
               statusz.add(new Triple(MonsterStatus.MS_Freeze, new MonsterStatusEffect(attack.skill, effect.getDuration()), (long)effect.getDuration()));
               break;
            case 27101101:
               statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getDuration()), 1L));
               break;
            case 27121052:
            case 162121041:
               statusz.add(new Triple(MonsterStatus.MS_Freeze, new MonsterStatusEffect(attack.skill, effect.getDuration()), (long)effect.getDuration()));
               break;
            case 32101001:
            case 32111016:
            case 400021088:
               if (attack.skill == 32101001) {
                  statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, SkillFactory.getSkill(32111016).getEffect(player.getSkillLevel(32111016)).getDuration()), 1L));
               }

               statusz.add(new Triple(MonsterStatus.MS_DarkLightning, new MonsterStatusEffect(32111016, SkillFactory.getSkill(32111016).getEffect(player.getSkillLevel(32111016)).getDuration()), 1L));
               break;
            case 32121004:
            case 32121011:
               statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(attack.skill, effect.getDuration()), 1L));
               break;
            case 142001000:
            case 142100000:
            case 142100001:
            case 142110000:
            case 142110001:
               statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(142110000, effect.getDOTTime()), 1L));
               break;
            case 142120002:
               applys.add(new Pair(MonsterStatus.MS_IndieMdr, new MonsterStatusEffect(attack.skill, effect.getDuration(), (long)(-effect.getX()))));
               applys.add(new Pair(MonsterStatus.MS_IndiePdr, new MonsterStatusEffect(attack.skill, effect.getDuration(), (long)(-effect.getX()))));
               break;
            case 142121031:
               statusz.add(new Triple(MonsterStatus.MS_Freeze, new MonsterStatusEffect(attack.skill, attack.skill == 400011015 ? effect.getW() * 1000 : (effect.getSubTime() > 0 ? effect.getSubTime() : effect.getDuration())), (long)effect.getDuration()));
               break;
            case 400021028:
               statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(attack.skill, effect.getDOTTime()), (long)effect.getDOT() * totDamageToOneMonster / (long)attack.allDamage.size() / 10000L));
               break;
            case 400021096:
               applys.add(new Pair(MonsterStatus.MS_Treasure, new MonsterStatusEffect(attack.skill, effect.getDuration(), (long)player.getId())));
            }

            Iterator var98 = statusz.iterator();

            while(var98.hasNext()) {
               Triple<MonsterStatus, MonsterStatusEffect, Long> status = (Triple)var98.next();
               if (status.left != null && status.mid != null && suc) {
                  if (status.left == MonsterStatus.MS_Burned && (Long)status.right < 0L) {
                     status.right = (Long)status.right & 4294967295L;
                  }

                  ((MonsterStatusEffect)status.mid).setValue((Long)status.right);
                  applys.add(new Pair((MonsterStatus)status.left, (MonsterStatusEffect)status.mid));
               }
            }

            if (monster != null && monster.isAlive()) {
               monster.applyStatus(player.getClient(), applys, effect);
            }

            if (GameConstants.isHolyAttack(attack.skill) && monster.isBuffed(MonsterStatus.MS_ElementResetBySummon)) {
               monster.cancelStatus(MonsterStatus.MS_ElementResetBySummon, monster.getBuff(MonsterStatus.MS_ElementResetBySummon));
            }
         }

         if (player.getSkillLevel(60030241) > 0 || player.getSkillLevel(80003015) > 0) {
            by = player.getSkillLevel(60030241) > 0 ? 60030241 : (player.getSkillLevel(80003015) > 0 ? 80003015 : 0);
            if (by > 0 && monster != null) {
               if (monster.getStats().isBoss()) {
                  if (monster.isAlive()) {
                     player.handlePriorPrepaRation(by, 2);
                  }
               } else if (!monster.isAlive()) {
                  player.handlePriorPrepaRation(by, 1);
               }
            }
         }

         if (player.getBuffedValue(SecondaryStat.BMageDeath) != null && player.skillisCooling(32001114) && GameConstants.isBMDarkAtackSkill(attack.skill) && player.getBuffedValue(SecondaryStat.AttackCountX) != null) {
            player.changeCooldown(32001114, -500);
         }

         if (player.getBuffedValue(SecondaryStat.BMageDeath) != null && (!monster.isAlive() || monster.getStats().isBoss()) && attack.skill != player.getBuffSource(SecondaryStat.BMageDeath)) {
            by = player.getBuffedValue(SecondaryStat.AttackCountX) != null ? 1 : (player.getLevel() >= 100 ? 6 : (player.getLevel() > 60 ? 8 : 10));
            if (player.getDeath() < by) {
               player.setDeath((byte)(player.getDeath() + 1));
               if (player.getDeath() >= by) {
                  player.setSkillCustomInfo(32120019, 1L, 0L);
               }

               HashMap<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
               statups.put(SecondaryStat.BMageDeath, new Pair(Integer.valueOf(player.getDeath()), 0));
               player.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, player.getBuffedEffect(SecondaryStat.BMageDeath), player));
            }
         }

         if (attack.skill == 400021096 && !player.getBuffedValue(400021096)) {
            player.lawOfGravity = monster.getObjectId();
            effect.applyTo(player);
         }

         if (attack.skill == 400021098) {
            player.getMap().broadcastMessage(MobPacket.skillAttackEffect(monster.getObjectId(), attack.skill, player.getId()));
         }

         if (attack.skill == 2121003 && monster.getBurnedBuffSize() >= 5) {
            player.changeCooldown(2121003, -2000);
            if (player.getCooldownLimit(2121011) > 0L) {
               player.removeCooldown(2121011);
            }
         }

         if (!monster.isAlive()) {
            ++multikill;
         }
      }
   }

   public static final AttackInfo parseDmgMa(LittleEndianAccessor lea, MapleCharacter chr, boolean chilling, boolean orbital) {
      AttackInfo ret = new AttackInfo();
      if (orbital) {
         ret.skill = lea.readInt();
         ret.skilllevel = lea.readInt();
         lea.skip(4);
         lea.skip(4);
         lea.skip(4);
      }

      lea.skip(1);
      ret.tbyte = lea.readByte();
      ret.targets = (byte)(ret.tbyte >>> 4 & 15);
      ret.hits = (byte)(ret.tbyte & 15);
      ret.skill = lea.readInt();
      ret.skilllevel = lea.readInt();

      try {
         if (orbital) {
            lea.skip(1);
         }

         lea.skip(4);
         lea.skip(4);
         GameConstants.attackBonusRecv(lea, ret);
         GameConstants.calcAttackPosition(lea, ret);
         if (orbital) {
            if (GameConstants.sub_57D400(ret.skill)) {
               ret.charge = lea.readInt();
            }
         } else if (GameConstants.is_keydown_skill(ret.skill)) {
            ret.charge = lea.readInt();
         }

         ret.isShadowPartner = lea.readByte();
         ret.isBuckShot = lea.readByte();
         ret.display = lea.readByte();
         ret.facingleft = lea.readByte();
         lea.skip(4);
         ret.attacktype = lea.readByte();
         if (GameConstants.is_evan_force_skill(ret.skill)) {
            lea.readByte();
         }

         ret.speed = lea.readByte();
         ret.lastAttackTickCount = lea.readInt();
         int chillingoid = false;
         if (chilling) {
            int var18 = lea.readInt();
         }

         lea.readInt();
         if (orbital || ret.skill == 22140024) {
            lea.skip(4);
         }

         ret.allDamage = new ArrayList();

         for(int i = 0; i < ret.targets; ++i) {
            int oid = lea.readInt();
            lea.readByte();
            lea.readByte();
            lea.readByte();
            lea.readByte();
            lea.readByte();
            int monsterId = lea.readInt();
            lea.readByte();
            Point pos1 = lea.readPos();
            Point pos2 = lea.readPos();
            if (!orbital) {
               lea.skip(1);
            }

            ArrayList<Pair<Long, Boolean>> allDamageNumbers = new ArrayList();
            long damage;
            if (ret.skill == 80001835) {
               int cc = lea.readByte();

               for(int ii = 0; ii < cc; ++ii) {
                  damage = lea.readLong();
                  if (damage < 0L) {
                     damage &= 4294967295L;
                  }

                  allDamageNumbers.add(new Pair(damage, false));
               }
            } else {
               lea.readShort();
               lea.skip(4);
               lea.skip(4);
               lea.readByte();

               for(int j = 0; j < ret.hits; ++j) {
                  damage = lea.readLong();
                  if (damage < 0L) {
                     damage &= 4294967295L;
                  }

                  allDamageNumbers.add(new Pair(damage, false));
               }
            }

            lea.skip(4);
            lea.skip(4);
            if (ret.skill == 37111005) {
               lea.skip(1);
            }

            if (ret.skill == 142120001 || ret.skill == 142120002 || ret.skill == 142110003) {
               lea.skip(8);
            }

            GameConstants.attackSkeletonImage(lea, ret);
            ret.allDamage.add(new AttackPair(oid, monsterId, pos1, pos2, allDamageNumbers));
         }

         ret.position = lea.readPos();
         if (ret.skill == 32111016) {
            ret.plusPosition3 = lea.readPos();
         }

         if (ret.skill == 22140024) {
            lea.skip(4);
         }

         byte posType = lea.readByte();
         if (!orbital && posType != 0) {
            ret.plusPosition = lea.readPos();
            ret.plusPosition2 = lea.readPos();
            if (ret.skill == 12100029) {
               lea.skip(4);
            } else if (ret.skill == 2121003) {
               int size = lea.readByte();

               for(int i = 0; i < size; ++i) {
                  lea.skip(4);
               }
            } else if (ret.skill == 2111003) {
               lea.skip(1);
               ret.plusPosition3 = lea.readPos();
            } else {
               ret.isLink = lea.readByte() == 1;
               ret.nMoveAction = lea.readByte();
               ret.bShowFixedDamage = lea.readByte();
            }
         }
      } catch (Exception var17) {
      }

      return ret;
   }

   public static final AttackInfo parseDmgB(LittleEndianAccessor lea, MapleCharacter chr) {
      AttackInfo ret = new AttackInfo();
      lea.skip(1);
      ret.tbyte = lea.readByte();
      ret.targets = (byte)(ret.tbyte >>> 4 & 15);
      ret.hits = (byte)(ret.tbyte & 15);
      ret.skill = lea.readInt();
      ret.skilllevel = lea.readInt();

      try {
         lea.skip(4);
         lea.skip(4);
         GameConstants.attackBonusRecv(lea, ret);
         GameConstants.calcAttackPosition(lea, ret);
         if (ret.skill != 0 && (GameConstants.is_keydown_skill(ret.skill) || GameConstants.is_super_nova_skill(ret.skill))) {
            ret.charge = lea.readInt();
         }

         if (GameConstants.sub_883680(ret.skill) || ret.skill == 5300007 || ret.skill == 27120211 || ret.skill == 400031003 || ret.skill == 400031004 || ret.skill == 64101008) {
            lea.skip(4);
         }

         if (GameConstants.isZeroSkill(ret.skill)) {
            ret.asist = lea.readByte();
         }

         if (GameConstants.sub_57DCA0(ret.skill)) {
            lea.skip(4);
         }

         ret.isShadowPartner = lea.readByte();
         ret.isBuckShot = lea.readByte();
         ret.display = lea.readByte();
         ret.facingleft = lea.readByte();
         lea.readInt();
         ret.attacktype = lea.readByte();
         ret.speed = lea.readByte();
         ret.lastAttackTickCount = lea.readInt();
         lea.readInt();
         if (ret.skill == 5111009) {
            lea.skip(1);
         } else if (ret.skill == 25111005) {
            lea.skip(4);
         }

         ret.allDamage = new ArrayList();

         for(int i = 0; i < ret.targets; ++i) {
            int oid = lea.readInt();
            lea.readByte();
            lea.readByte();
            lea.readByte();
            lea.readByte();
            lea.readByte();
            int monsterId = lea.readInt();
            lea.readByte();
            Point pos1 = lea.readPos();
            Point pos2 = lea.readPos();
            lea.skip(2);
            lea.readInt();
            lea.readInt();
            lea.readByte();
            ArrayList<Pair<Long, Boolean>> allDamageNumbers = new ArrayList();

            for(int j = 0; j < ret.hits; ++j) {
               long damage = lea.readLong();
               if (damage < 0L) {
                  damage &= 4294967295L;
               }

               allDamageNumbers.add(new Pair(damage, false));
            }

            lea.skip(4);
            lea.skip(4);
            if (ret.skill == 37111005) {
               lea.skip(1);
            }

            GameConstants.attackSkeletonImage(lea, ret);
            ret.allDamage.add(new AttackPair(oid, monsterId, pos1, pos2, allDamageNumbers));
         }

         ret.position = lea.readPos();
      } catch (Exception var13) {
      }

      return ret;
   }

   public static final AttackInfo parseDmgM(LittleEndianAccessor lea, MapleCharacter chr, boolean dot) {
      AttackInfo ret = new AttackInfo();
      lea.skip(1);
      ret.tbyte = lea.readByte();
      ret.targets = (byte)(ret.tbyte >>> 4 & 15);
      ret.hits = (byte)(ret.tbyte & 15);
      ret.skill = lea.readInt();
      ret.skilllevel = lea.readInt();
      chr.dropMessageGM(6, "ret.skill : " + ret.skill);

      try {
         if (!dot) {
            ret.isLink = lea.readByte() == 1;
         }

         lea.skip(4);
         lea.skip(4);
         GameConstants.attackBonusRecv(lea, ret);
         GameConstants.calcAttackPosition(lea, ret);
         if ((ret.skill == 11121056 || GameConstants.is_keydown_skill(ret.skill) || GameConstants.is_super_nova_skill(ret.skill)) && ret.skill != 35121015) {
            ret.charge = lea.readInt();
         }

         if (GameConstants.sub_883680(ret.skill) || ret.skill == 5300007 || ret.skill == 27120211 || ret.skill == 400031003 || ret.skill == 400031004 || ret.skill == 64101008 || ret.skill == 400031067) {
            lea.skip(4);
         }

         if (GameConstants.isZeroSkill(ret.skill)) {
            ret.asist = lea.readByte();
            lea.readInt();
         }

         if (GameConstants.sub_57DCA0(ret.skill) || ret.skill == 11111220 || ret.skill == 11121201 || ret.skill == 11121202) {
            ret.summonattack = lea.readInt();
         }

         if (ret.skill == 400031010 || ret.skill == 80002823) {
            lea.skip(4);
            lea.skip(4);
         }

         if (ret.skill == 400041019 || ret.skill == 14101028 || ret.skill == 14101029) {
            lea.skip(4);
            lea.skip(4);
         }

         if (ret.skill == 15101028 || ret.skill == 11101029 || ret.skill == 11101030 || ret.skill == 11111230 || ret.skill == 11111130) {
            lea.skip(4);
         }

         ret.isShadowPartner = lea.readByte();
         ret.isBuckShot = lea.readByte();
         ret.display = lea.readByte();
         ret.facingleft = lea.readByte();
         lea.readInt();
         ret.attacktype = lea.readByte();
         ret.speed = lea.readByte();
         ret.lastAttackTickCount = lea.readInt();
         lea.readInt();
         if (ret.skill != 400051018 && ret.skill != 400051019 && ret.skill != 400051020 && ret.skill != 400051027) {
            lea.readInt();
         }

         if (ret.skill != 5111009 && ret.skill != 21120022 && ret.skill != 21121016 && ret.skill != 21121017) {
            if (ret.skill != 25111005 && ret.skill != 33000036 && ret.skill != 80001762 && ret.skill != 400021131 && ret.skill != 400021130) {
               if (ret.isLink && ret.skill == 23121011 || ret.skill == 80001913) {
                  lea.skip(1);
               }
            } else {
               lea.skip(4);
            }
         } else {
            lea.skip(1);
         }

         ret.allDamage = new ArrayList();
         if (SkillFactory.getSkill(ret.skill) != null && SkillFactory.getSkill(ret.skill).isFinalAttack() && ret.skill != 400001038) {
            lea.skip(1);
         }

         int size;
         int z;
         for(size = 0; size < ret.targets; ++size) {
            z = lea.readInt();
            lea.readByte();
            lea.readByte();
            lea.readByte();
            lea.readByte();
            byte ab = lea.readByte();
            int monsterId = lea.readInt();
            lea.readByte();
            Point pos1 = lea.readPos();
            Point pos2 = lea.readPos();
            lea.skip(2);
            lea.readInt();
            lea.readInt();
            lea.readByte();
            ArrayList<Pair<Long, Boolean>> allDamageNumbers = new ArrayList();

            for(int j = 0; j < ret.hits; ++j) {
               long damage = lea.readLong();
               if (damage < 0L) {
                  damage &= 4294967295L;
               }

               allDamageNumbers.add(new Pair(damage, false));
            }

            lea.skip(4);
            lea.skip(4);
            if (ret.skill == 37111005 || ret.skill == 400021029) {
               lea.skip(1);
            }

            GameConstants.attackSkeletonImage(lea, ret);
            ret.allDamage.add(new AttackPair(z, monsterId, pos1, pos2, allDamageNumbers));
         }

         ret.position = GameConstants.is_super_nova_skill(ret.skill) ? lea.readPos() : (ret.skill == 101000102 ? lea.readPos() : (ret.skill != 400031016 && ret.skill != 400041024 && ret.skill != 80002452 && !GameConstants.sub_84ABA0(ret.skill) ? lea.readPos() : lea.readPos()));
         if (GameConstants.sub_849720(ret.skill)) {
            lea.skip(4);
            ret.position = lea.readPos();
            lea.skip(1);
         }

         if (ret.skill == 21121057) {
            lea.readPos();
         }

         if (GameConstants.sub_846930(ret.skill) > 0 || GameConstants.sub_847580(ret.skill)) {
            lea.skip(1);
         }

         if (ret.skill == 400031059) {
            lea.skip(4);
            ret.plusPosition2 = lea.readPos();
         }

         if (ret.skill == 21120019 || ret.skill == 37121052 || GameConstants.is_shadow_assult(ret.skill) || ret.skill == 11121014 || ret.skill == 5101004) {
            ret.plusPos = lea.readByte();
            ret.plusPosition = new Point(lea.readInt(), lea.readInt());
         }

         if (ret.skill == 61121105 || ret.skill == 61121222 || ret.skill == 24121052) {
            for(short count = lea.readShort(); count > 0; --count) {
               ret.mistPoints.add(new Point(lea.readShort(), lea.readShort()));
            }
         }

         if (ret.skill == 14111006) {
            lea.skip(2);
            lea.skip(2);
         } else if (ret.skill == 80002686) {
            size = lea.readInt();

            for(z = 0; z < size; ++z) {
               lea.skip(4);
            }
         }
      } catch (Exception var15) {
      }

      return ret;
   }

   public static final AttackInfo parseDmgR(LittleEndianAccessor lea, MapleCharacter chr) {
      AttackInfo ret = new AttackInfo();
      byte specialType = lea.readByte();
      lea.skip(1);
      ret.tbyte = lea.readByte();
      ret.targets = (byte)(ret.tbyte >>> 4 & 15);
      ret.hits = (byte)(ret.tbyte & 15);
      ret.skill = lea.readInt();
      ret.skilllevel = lea.readInt();

      try {
         ret.isLink = lea.readByte() == 1;
         lea.skip(4);
         lea.skip(4);
         GameConstants.attackBonusRecv(lea, ret);
         GameConstants.calcAttackPosition(lea, ret);
         if (GameConstants.is_keydown_skill(ret.skill)) {
            ret.charge = lea.readInt();
         }

         if (GameConstants.isZeroSkill(ret.skill)) {
            ret.asist = lea.readByte();
         }

         if (GameConstants.sub_57DCA0(ret.skill) || ret.skill == 14121001 || ret.skill == 14101020) {
            ret.summonattack = lea.readInt();
         }

         ret.isShadowPartner = lea.readByte();
         ret.isBuckShot = lea.readByte();
         lea.skip(4);
         lea.skip(1);
         if (specialType == 1) {
            lea.readInt();
            lea.readShort();
            lea.readShort();
         }

         ret.display = lea.readByte();
         ret.facingleft = lea.readByte();
         lea.skip(4);
         ret.attacktype = lea.readByte();
         if (ret.skill == 36111010 || ret.skill == 80001915) {
            lea.skip(4);
            lea.skip(4);
            lea.skip(4);
         }

         ret.speed = lea.readByte();
         ret.lastAttackTickCount = lea.readInt();
         lea.readInt();
         lea.skip(4);
         if (SkillFactory.getSkill(ret.skill) != null && SkillFactory.getSkill(ret.skill).isFinalAttack()) {
            lea.skip(1);
         }

         ret.csstar = lea.readShort();
         lea.skip(1);
         lea.skip(2);
         lea.skip(2);
         lea.skip(2);
         ret.AOE = lea.readShort();
         ret.allDamage = new ArrayList();

         for(int i = 0; i < ret.targets; ++i) {
            int oid = lea.readInt();
            lea.readByte();
            lea.readByte();
            lea.readByte();
            lea.readByte();
            lea.readByte();
            int monsterId = lea.readInt();
            lea.readByte();
            Point pos1 = lea.readPos();
            Point pos2 = lea.readPos();
            lea.skip(2);
            lea.readInt();
            lea.readInt();
            lea.readByte();
            ArrayList<Pair<Long, Boolean>> allDamageNumbers = new ArrayList();

            for(int j = 0; j < ret.hits; ++j) {
               long damage = lea.readLong();
               if (damage < 0L) {
                  damage &= 4294967295L;
               }

               allDamageNumbers.add(new Pair(damage, false));
            }

            lea.skip(4);
            lea.skip(4);
            GameConstants.attackSkeletonImage(lea, ret);
            ret.allDamage.add(new AttackPair(oid, monsterId, pos1, pos2, allDamageNumbers));
         }

         ret.position = lea.readPos();
         if (ret.skill - 64001009 >= -2 && ret.skill - 64001009 <= 2) {
            lea.skip(1);
            ret.chain = lea.readPos();
            return ret;
         }

         ret.bShowFixedDamage = lea.readByte();
         ret.nMoveAction = lea.readByte();
         if (GameConstants.sub_846930(ret.skill) > 0 || GameConstants.sub_847580(ret.skill)) {
            lea.skip(1);
         }

         if (GameConstants.isWildHunter(ret.skill / 10000)) {
            ret.plusPosition = lea.readPos();
         }

         if (GameConstants.sub_8327B0(ret.skill) && ret.skill != 13111020) {
            ret.plusPosition2 = lea.readPos();
         }

         if (ret.skill == 23121002 || ret.skill == 80001914) {
            lea.skip(1);
         }

         if (lea.available() <= 0L) {
            return ret;
         }
      } catch (Exception var14) {
      }

      return ret;
   }

   public static void WFinalAttackRequest(MapleCharacter chr, int skillid, MapleMonster monster) {
      if (SkillFactory.getSkill(skillid) != null) {
         int finalattackid;
         boolean skilllv;
         byte weaponidx;
         byte skilllv;
         if (chr.getJob() == 512 && chr.skillisCooling(5121013)) {
            if (skillid == 5001002 || skillid == 5101004 || skillid == 5111009 || skillid == 5121007 || skillid == 5121020) {
               finalattackid = SkillFactory.getSkill(5121013).getFinalAttackIdx();
               skilllv = false;
               skilllv = (byte)chr.getSkillLevel(5121013);
               finalattackid = 5120021;
               weaponidx = (byte)(chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11).getItemId() / 10000 % 100);
               chr.getClient().getSession().writeAndFlush(CField.finalAttackRequest(SkillFactory.getSkill(finalattackid).getEffect(skilllv).getAttackCount(), skillid, finalattackid, weaponidx, monster));
               return;
            }
         } else {
            if (GameConstants.isExceedAttack(skillid)) {
               finalattackid = SkillFactory.getSkill(31220007).getFinalAttackIdx();
               skilllv = false;
               skilllv = (byte)chr.getSkillLevel(31220007);
               finalattackid = 31220007;
               weaponidx = (byte)(chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11).getItemId() / 10000 % 100);
               chr.getClient().getSession().writeAndFlush(CField.finalAttackRequest(SkillFactory.getSkill(finalattackid).getEffect(skilllv).getAttackCount(), skillid, finalattackid, weaponidx, monster));
               return;
            }

            if (GameConstants.isAran(chr.getJob()) && chr.getBuffedValue(chr.getSkillLevel(21120021) > 0 ? 21120021 : 21100015)) {
               finalattackid = SkillFactory.getSkill(chr.getSkillLevel(21120021) > 0 ? 21120021 : 21100015).getFinalAttackIdx();
               skilllv = false;
               skilllv = (byte)chr.getSkillLevel(chr.getSkillLevel(21120021) > 0 ? 21120021 : 21100015);
               finalattackid = chr.getSkillLevel(21120021) > 0 ? 21120021 : 21100015;
               weaponidx = (byte)(chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11).getItemId() / 10000 % 100);
               chr.getClient().getSession().writeAndFlush(CField.finalAttackRequest(SkillFactory.getSkill(finalattackid).getEffect(skilllv).getAttackCount(), skillid, finalattackid, weaponidx, monster));
               return;
            }

            if (GameConstants.isZero(chr.getJob()) && skillid == 101000101) {
               finalattackid = SkillFactory.getSkill(101000102).getFinalAttackIdx();
               skilllv = false;
               skilllv = (byte)chr.getSkillLevel(101000102);
               finalattackid = 101000102;
               weaponidx = (byte)(chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10).getItemId() / 10000 % 100);
               chr.getClient().getSession().writeAndFlush(CField.finalAttackRequest(SkillFactory.getSkill(finalattackid).getEffect(skilllv).getAttackCount(), skillid, finalattackid, weaponidx, monster));
               return;
            }

            if (GameConstants.isCannon(chr.getJob()) && chr.getBuffedValue(5311004) && chr.getSkillCustomValue0(5311004) == 1L) {
               finalattackid = SkillFactory.getSkill(5311004).getFinalAttackIdx();
               skilllv = false;
               skilllv = (byte)chr.getSkillLevel(5311004);
               finalattackid = 5310004;
               weaponidx = (byte)(chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11).getItemId() / 10000 % 100);
               chr.getClient().getSession().writeAndFlush(CField.finalAttackRequest(SkillFactory.getSkill(finalattackid).getEffect(skilllv).getAttackCount(), skillid, finalattackid, weaponidx, monster));
               return;
            }
         }

         finalattackid = SkillFactory.getSkill(skillid).getFinalAttackIdx();
         skilllv = 0;
         if (chr.getJob() == 3212) {
            skilllv = (byte)chr.getSkillLevel(32121004);
            finalattackid = 32121011;
         }

         if (finalattackid > 0) {
            if (skillid == 1001005 && chr.getSkillLevel(finalattackid) <= 0) {
               if (chr.getSkillLevel(1200002) > 0) {
                  finalattackid = 1200002;
               } else if (chr.getSkillLevel(1300002) > 0) {
                  finalattackid = 1300002;
               }
            }

            if (finalattackid == 1100002) {
               if (chr.getSkillLevel(1120013) > 0) {
                  finalattackid = 1120013;
               }
            } else if (finalattackid == 51100002) {
               if (chr.getSkillLevel(51120002) > 0) {
                  finalattackid = 51120002;
               }
            } else if (finalattackid == 5120021) {
               finalattackid = 5121013;
            }

            if (skilllv == 0) {
               skilllv = (byte)chr.getSkillLevel(finalattackid);
            }

            if (SkillFactory.getSkill(finalattackid).getEffect(skilllv) == null) {
               return;
            }

            int prop = SkillFactory.getSkill(finalattackid).getEffect(skilllv).getProp();
            if ((finalattackid == 1100002 || finalattackid == 1120013) && chr.getSkillLevel(1120048) > 0) {
               prop = (byte)(prop + 15);
            } else if (finalattackid == 32121011) {
               prop = 60;
            }

            if (chr.getBuffedValue(33121054)) {
               prop = 100;
            }

            if (Randomizer.isSuccess(prop)) {
               byte weaponidx = (byte)(chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11).getItemId() / 10000 % 100);
               chr.getClient().getSession().writeAndFlush(CField.finalAttackRequest(SkillFactory.getSkill(finalattackid).getEffect(skilllv).getAttackCount(), skillid, finalattackid, weaponidx, monster));
            } else if (monster != null) {
               chr.getClient().getSession().writeAndFlush(CField.finalAttackRequest(0, skillid, 0, 0, monster));
            }
         }
      }

   }

   public static void MFinalAttackRequest(MapleCharacter chr, int skillid, MapleMonster monster) {
      if (SkillFactory.getSkill(skillid) != null) {
         int finalattackid = SkillFactory.getSkill(skillid).getFinalAttackIdx();
         byte skilllv = 0;
         if (chr.getJob() == 212) {
            skilllv = (byte)chr.getSkillLevel(2121007);
            finalattackid = 2120013;
         } else if (chr.getJob() == 222) {
            skilllv = (byte)chr.getSkillLevel(2221007);
            finalattackid = 2220014;
         } else if (chr.getJob() == 3212) {
            skilllv = (byte)chr.getSkillLevel(32121011);
            finalattackid = 32121011;
         }

         if (finalattackid > 0) {
            if (skilllv == 0) {
               skilllv = (byte)chr.getSkillLevel(finalattackid);
            }

            if (Randomizer.isSuccess((byte)SkillFactory.getSkill(finalattackid).getEffect(skilllv).getProp())) {
               byte weaponidx = (byte)(chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11).getItemId() / 10000 % 100);
               chr.getClient().getSession().writeAndFlush(CField.finalAttackRequest(SkillFactory.getSkill(finalattackid).getEffect(skilllv).getAttackCount(), skillid, finalattackid, weaponidx, monster));
            } else {
               chr.getClient().getSession().writeAndFlush(CField.finalAttackRequest(0, skillid, 0, 0, monster));
            }
         }
      }

   }
}
