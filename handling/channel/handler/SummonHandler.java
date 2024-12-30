package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.MatrixSkill;
import client.SecondaryStat;
import client.Skill;
import client.SkillFactory;
import client.SummonSkillEntry;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import handling.channel.ChannelServer;
import handling.world.World;
import handling.world.party.MaplePartyCharacter;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.SecondaryStatEffect;
import server.field.skill.SecondAtom;
import server.life.MapleMonster;
import server.maps.ForceAtom;
import server.maps.MapleAtom;
import server.maps.MapleDragon;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleSummon;
import server.maps.SummonMovementType;
import server.movement.LifeMovementFragment;
import tools.Pair;
import tools.Triple;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.MobPacket;

public class SummonHandler {
   public static final void MoveDragon(LittleEndianAccessor slea, MapleCharacter chr) {
      slea.skip(12);
      List<LifeMovementFragment> res = MovementParse.parseMovement(slea, 5);
      if (chr != null && chr.getDragon() != null && res.size() > 0) {
         Point pos = chr.getDragon().getPosition();
         MovementParse.updatePosition(res, chr.getDragon(), 0);
         if (!chr.isHidden()) {
            chr.getMap().broadcastMessage(chr, CField.moveDragon(chr.getDragon(), pos, res), chr.getTruePosition());
         }
      }

   }

   public static final void MoveSummon(LittleEndianAccessor slea, MapleCharacter chr) {
      if (chr != null && chr.getMap() != null) {
         MapleMapObject obj = chr.getMap().getMapObject(slea.readInt(), MapleMapObjectType.SUMMON);
         if (obj != null) {
            if (obj instanceof MapleDragon) {
               MoveDragon(slea, chr);
            } else {
               MapleSummon sum = (MapleSummon)obj;
               if (sum.getOwner().getId() == chr.getId() && sum.getSkillLevel() > 0 && sum.getMovementType() != SummonMovementType.STATIONARY) {
                  slea.skip(12);
                  List<LifeMovementFragment> res = MovementParse.parseMovement(slea, 4);
                  Point pos = sum.getPosition();
                  MovementParse.updatePosition(res, sum, 0);
                  if (res.size() > 0) {
                     chr.getMap().broadcastMessage(chr, CField.SummonPacket.moveSummon(chr.getId(), sum.getObjectId(), pos, res), sum.getTruePosition());
                  }

               }
            }
         }
      }
   }

   public static final void DamageSummon(LittleEndianAccessor slea, MapleCharacter chr) {
      int objectId = slea.readInt();
      int unkByte = slea.readByte();
      int damage = slea.readInt();
      int monsterIdFrom = slea.readInt();
      MapleSummon summon = chr.getMap().getSummonByOid(objectId);
      MapleMonster monster = chr.getMap().getMonsterById(monsterIdFrom);
      if (summon != null) {
         boolean remove = false;
         if (monster != null) {
            switch(summon.getSkill()) {
            case 13111024:
               monster.applyStatus(chr.getClient(), MonsterStatus.MS_Speed, new MonsterStatusEffect(13111024, (int)chr.getBuffLimit(13111024)), -100, chr.getBuffedEffect(SecondaryStat.IndieSummon, 13111024));
            }
         }

         if ((summon.isPuppet() || summon.getSkill() == 3221014) && summon.getOwner().getId() == chr.getId() && damage > 0) {
            summon.addHP(-damage);
            if (summon.getHP() <= 0) {
               remove = true;
            }

            chr.dropMessageGM(-8, "DamageSummon DMG : " + damage);
            chr.getMap().broadcastMessage(chr, CField.SummonPacket.damageSummon(chr.getId(), summon.getSkill(), damage, unkByte, monsterIdFrom), summon.getTruePosition());
            if (summon.getSkill() == 14000027) {
               summon.removeSummon(chr.getMap(), false);
            }
         }

         if (remove) {
            chr.cancelEffectFromBuffStat(SecondaryStat.IndieSummon);
         }

      }
   }

   public static void SummonAttack(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      if (chr != null && chr.isAlive() && chr.getMap() != null) {
         MapleMap map = chr.getMap();
         MapleMapObject obj = map.getMapObject(slea.readInt(), MapleMapObjectType.SUMMON);
         if (obj != null && obj instanceof MapleSummon) {
            MapleSummon summon = (MapleSummon)obj;
            SummonSkillEntry sse = SkillFactory.getSummonData(summon.getSkill());
            if (summon.getSkill() / 1000000 != 35 && summon.getSkill() != 400011065 && summon.getSkill() != 12120013 && summon.getSkill() != 33101008 && summon.getSkill() != 400041038 && sse == null) {
               chr.dropMessageGM(5, "Error in processing attack." + summon.getSkill());
            } else {
               slea.skip(4);
               int skillid = slea.readInt();
               if (summon.getSkill() != skillid) {
                  chr.dropMessage(5, "skill data unmatched.");
               }

               int skillid2 = slea.readInt();
               chr.dropMessageGM(6, "skillid : " + skillid);
               if (skillid == 12120013) {
                  skillid2 = 400021043;
               }

               slea.skip(1);
               switch(skillid2) {
               case 1301014:
               case 1310018:
               case 1311019:
               case 1321024:
               case 1321025:
                  SecondaryStatEffect eff = SkillFactory.getSkill(skillid2).getEffect(c.getPlayer().getSkillLevel(skillid2));
                  c.getSession().writeAndFlush(CField.skillCooldown(1301014, eff.getCooldown(c.getPlayer())));
                  break;
               case 152110001:
                  slea.skip(4);
               }

               slea.readByte();
               slea.readByte();
               slea.readInt();
               byte animation = slea.readByte();
               byte tbyte = slea.readByte();
               byte numAttacked = (byte)(tbyte >>> 4 & 15);
               byte hits = (byte)(tbyte & 15);
               slea.readByte();
               if (summon.getSkill() == 35111002) {
                  slea.skip(12);
               }

               if (chr.signofbomb) {
                  slea.skip(4);
               }

               slea.skip(4);
               Point pos = slea.readPos();
               byte a = slea.readByte();
               if (a != 0) {
                  slea.skip(2);
                  slea.skip(2);
               }

               slea.skip(4);
               slea.skip(2);
               slea.skip(4);
               slea.skip(4);
               List<Pair<Integer, List<Long>>> allDamage = new ArrayList();
               long damage = 0L;
               long totDamageToOneMonster = 0L;

               for(int i = 0; i < numAttacked; ++i) {
                  int objectId = slea.readInt();
                  chr.dropMessageGM(6, "objectId : " + objectId);
                  slea.readInt();
                  slea.readByte();
                  slea.readByte();
                  slea.readByte();
                  slea.readByte();
                  slea.readByte();
                  slea.readInt();
                  slea.readByte();
                  slea.skip(4);
                  slea.skip(4);
                  slea.readInt();
                  slea.skip(2);
                  slea.readInt();
                  slea.readInt();
                  slea.readByte();
                  List<Long> damages = new ArrayList();

                  for(int j = 0; j < hits; ++j) {
                     totDamageToOneMonster += damage;
                     damage = slea.readLong();
                     chr.dropMessageGM(6, "damage  : " + damage);
                     if (damage < 0L) {
                        damage &= 4294967295L;
                     }

                     damages.add(damage);
                  }

                  slea.skip(4);
                  GameConstants.attackSkeletonImage(slea, new AttackInfo());
                  allDamage.add(new Pair(objectId, damages));
               }

               chr.dropMessageGM(6, "animation  : " + animation);
               map.broadcastMessage(chr, CField.SummonPacket.summonAttack(summon, skillid2 != 0 ? skillid2 : skillid, animation, tbyte, allDamage, chr.getLevel(), pos, false), summon.getTruePosition());
               Skill summonSkill = SkillFactory.getSkill(summon.getSkill());
               SecondaryStatEffect summonEffect = summonSkill.getEffect(summon.getSkillLevel());
               if (summonEffect == null) {
                  chr.dropMessage(5, "Error in attack.");
               } else {
                  if (skillid == 35111002 && animation == 24) {
                     Iterator<MapleSummon> summons1 = chr.getMap().getAllSummonsThreadsafe().iterator();
                     ArrayList sum = new ArrayList();

                     while(summons1.hasNext()) {
                        MapleSummon summon2 = (MapleSummon)summons1.next();
                        if (summon2.getOwner().getId() == chr.getId() && summon2.getSkill() == skillid) {
                           sum.add(summon2);
                        }
                     }

                     Iterator var44 = sum.iterator();

                     while(var44.hasNext()) {
                        MapleSummon s = (MapleSummon)var44.next();
                        s.removeSummon(chr.getMap(), false);
                     }
                  }

                  int n3 = 0;
                  int n4 = false;
                  int killmobsize = 0;
                  int attackbossmob = 0;
                  int plustime = 0;
                  MapleMonster mob = null;
                  Iterator var28 = allDamage.iterator();

                  while(true) {
                     do {
                        Pair attackEntry;
                        do {
                           if (!var28.hasNext()) {
                              SecondaryStatEffect subSummonEffect;
                              if (summon.getSkill() == 12120013) {
                                 subSummonEffect = SkillFactory.getSkill(400021042).getEffect(chr.getSkillLevel(400021042));
                                 if (subSummonEffect.getCooldown(chr) > 0) {
                                    chr.addCooldown(skillid2, System.currentTimeMillis(), (long)subSummonEffect.getCooldown(chr));
                                    c.getSession().writeAndFlush(CField.skillCooldown(skillid2, subSummonEffect.getCooldown(chr)));
                                 }

                                 c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
                              } else if (skillid2 == 152101006) {
                                 if (summon.getCrystalSkills().size() > 0 && (Boolean)summon.getCrystalSkills().get(0)) {
                                    summon.getCrystalSkills().set(0, false);
                                    c.getSession().writeAndFlush(CField.SummonPacket.transformSummon(summon, 2));
                                    c.getSession().writeAndFlush(CField.SummonPacket.ElementalRadiance(summon, 3));
                                 }
                              } else if (skillid == 35121011) {
                                 summon.removeSummon(map, false);
                              } else if (skillid == 400021068 || skillid2 == 400021062) {
                                 c.getPlayer().getMap().broadcastMessage(CField.SummonPacket.ElementalRadiance(summon, 5));
                              }

                              if (!GameConstants.isCaptain(c.getPlayer().getJob()) && summon.getSkill() != 2211011 && summon.getSkill() != 3221014 && summon.getSkill() != 25121133 && summon.getSkill() != 400021068 && skillid != 400021095 && skillid != 164121008 && skillid != 36121002 && skillid != 36121013 && skillid != 5320011 && skillid != 5321004 && skillid != 5211014 && skillid != 14121003 && skillid != 400021071 && (skillid < 33001007 || skillid > 33001015) && summonEffect.getCooldown(c.getPlayer()) > 0 && c.getPlayer().getCooldownLimit(summon.getSkill()) == 0L) {
                                 switch(skillid) {
                                 case 4111007:
                                 case 4211007:
                                 case 4221052:
                                    break;
                                 default:
                                    c.getPlayer().addCooldown(summon.getSkill(), System.currentTimeMillis(), (long)summonEffect.getCooldown(c.getPlayer()));
                                    c.getSession().writeAndFlush(CField.skillCooldown(summon.getSkill(), summonEffect.getCooldown(c.getPlayer())));
                                 }
                              }

                              if (SkillFactory.getSkill(skillid2) != null && summon.getSkill() != 25121133 && summon.getSkill() != 3221014) {
                                 subSummonEffect = SkillFactory.getSkill(skillid2).getEffect(chr.getSkillLevel(skillid2));
                                 if (subSummonEffect.getCooldown(c.getPlayer()) > 0 && c.getPlayer().getCooldownLimit(skillid2) == 0L) {
                                    c.getPlayer().addCooldown(skillid2, System.currentTimeMillis(), (long)subSummonEffect.getCooldown(c.getPlayer()));
                                    c.getSession().writeAndFlush(CField.skillCooldown(skillid2, subSummonEffect.getCooldown(c.getPlayer())));
                                 }
                              }

                              if (summon.getSkill() == 400041038) {
                                 summon.removeSummon(c.getPlayer().getMap(), false);
                                 c.getPlayer().cancelEffectFromBuffStat(SecondaryStat.IndieSummon, summon.getSkill());
                              } else if (GameConstants.isPathFinder(chr.getJob()) && summon.getSkill() != 400031051) {
                                 MapleCharacter.렐릭게이지(chr.getClient(), skillid);
                              } else if (summon.getSkill() == 400051046) {
                                 if (!summon.isSpecialSkill() && summon.getEnergy() < 8) {
                                    summon.setEnergy(summon.getEnergy() + 1);
                                    chr.getMap().broadcastMessage(CField.SummonPacket.ElementalRadiance(summon, 2));
                                    chr.getClient().getSession().writeAndFlush(CField.SummonPacket.specialSummon(summon, 2));
                                 }
                              } else if (summon.getSkill() == 400011065) {
                                 summon.removeSummon(c.getPlayer().getMap(), false);
                                 chr.setSkillCustomInfo(400011065, 0L, 5000L);
                              }

                              if (killmobsize > 0) {
                                 chr.CombokillHandler(mob, 1, killmobsize);
                              }

                              return;
                           }

                           attackEntry = (Pair)var28.next();
                           mob = map.getMonsterByOid((Integer)attackEntry.left);
                        } while(mob == null);

                        Long toDamage;
                        for(Iterator var30 = ((List)attackEntry.right).iterator(); var30.hasNext(); totDamageToOneMonster += toDamage) {
                           toDamage = (Long)var30.next();
                        }

                        List<Triple<MonsterStatus, MonsterStatusEffect, Long>> statusz = new ArrayList();
                        List<Triple<MonsterStatus, MonsterStatusEffect, Long>> statusz2 = new ArrayList();
                        SecondaryStatEffect curseMark;
                        switch(skillid) {
                        case 1301014:
                        case 1310018:
                        case 1311019:
                        case 1321024:
                        case 1321025:
                           statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(skillid, summonEffect.getSubTime() > 0 ? summonEffect.getSubTime() : summonEffect.getDuration()), 1L));
                           break;
                        case 2111010:
                           statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(skillid, summonEffect.getDOTTime()), (long)summonEffect.getDOT() * totDamageToOneMonster / (long)allDamage.size() / 10000L));
                           summon.removeSummon(map, false);
                           chr.removeSummon(summon);
                        case 2121005:
                        default:
                           break;
                        case 2221005:
                           statusz.add(new Triple(MonsterStatus.MS_Speed, new MonsterStatusEffect(skillid, summonEffect.getSubTime() > 0 ? summonEffect.getSubTime() : summonEffect.getDuration()), (long)summonEffect.getV()));
                           if (mob.getFreezingOverlap() < 5) {
                              mob.setFreezingOverlap((byte)(mob.getFreezingOverlap() + 1));
                           }
                           break;
                        case 2321003:
                           statusz.add(new Triple(MonsterStatus.MS_ElementResetBySummon, new MonsterStatusEffect(skillid, summonEffect.getSubTime() > 0 ? summonEffect.getSubTime() : summonEffect.getDuration()), (long)summonEffect.getX()));
                           break;
                        case 3111005:
                           if (Randomizer.rand(0, 100) < 41) {
                              statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(skillid, 5000), 1L));
                           }
                           break;
                        case 3211005:
                           if (!mob.getStats().isBoss()) {
                              statusz.add(new Triple(MonsterStatus.MS_Freeze, new MonsterStatusEffect(skillid, 5000), (long)summonEffect.getX()));
                           }
                           break;
                        case 3221014:
                           statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(skillid, summonEffect.getSubTime() > 0 ? summonEffect.getSubTime() : summonEffect.getDuration()), 1L));
                           break;
                        case 3311009:
                           if (chr.getSkillLevel(3320000) > 0) {
                              curseMark = SkillFactory.getSkill(3320000).getEffect(1);
                              if (curseMark != null) {
                                 Map<SecondaryStat, Pair<Integer, Integer>> statups = new HashMap();
                                 int max2 = curseMark.getU();
                                 int add = curseMark.getS();
                                 if (chr.energy < max2) {
                                    chr.energy = Math.min(max2, chr.energy + add);
                                 }

                                 statups.put(SecondaryStat.RelikGauge, new Pair(chr.energy, 0));
                                 chr.getClient().getSession().writeAndFlush(CWvsContext.BuffPacket.giveBuff(statups, (SecondaryStatEffect)null, chr));
                              }
                           }
                           break;
                        case 23111009:
                           statusz.add(new Triple(MonsterStatus.MS_Burned, new MonsterStatusEffect(skillid, summonEffect.getDOTTime()), (long)summonEffect.getDOT() * totDamageToOneMonster / (long)allDamage.size() / 10000L));
                           break;
                        case 35111002:
                           statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(skillid, 5000), 1L));
                           break;
                        case 61111002:
                        case 61111220:
                           statusz.add(new Triple(MonsterStatus.MS_Speed, new MonsterStatusEffect(skillid, summonEffect.getSubTime() > 0 ? summonEffect.getSubTime() : summonEffect.getDuration()), (long)summonEffect.getX()));
                           break;
                        case 152101000:
                           if (chr.getSkillLevel(152100012) > 0) {
                              curseMark = SkillFactory.getSkill(152000010).getEffect(chr.getSkillLevel(152000010));
                              int max = chr.getSkillLevel(152100012) > 0 ? 5 : (chr.getSkillLevel(152110010) > 0 ? 3 : 1);
                              if (mob.getBuff(152000010) == null && mob.getCustomValue0(152000010) > 0L) {
                                 mob.removeCustomInfo(152000010);
                              }

                              if (mob.getCustomValue0(152000010) < (long)max) {
                                 mob.addSkillCustomInfo(152000010, 1L);
                              }

                              statusz.add(new Triple(MonsterStatus.MS_CurseMark, new MonsterStatusEffect(152000010, curseMark.getDuration(), (long)curseMark.getY() * mob.getCustomValue0(152000010)), mob.getCustomValue0(152000010)));
                           }
                           break;
                        case 164121008:
                           chr.setSkillCustomInfo(164121008, chr.getSkillCustomValue0(164121008) + 1L, 0L);
                           if (chr.getSkillCustomValue0(164121008) >= (long)summonEffect.getZ()) {
                              chr.removeSkillCustomInfo(164121008);
                              if (chr.getSkillCustomValue0(164121009) < (long)summonEffect.getX()) {
                                 chr.setSkillCustomInfo(164121009, chr.getSkillCustomValue0(164121009) + (long)(mob.getStats().isBoss() ? 3 : 1), 0L);
                                 if (chr.getSkillCustomValue0(164121009) > (long)summonEffect.getX()) {
                                    chr.setSkillCustomInfo(164121009, (long)summonEffect.getX(), 0L);
                                 }
                              }
                           }

                           chr.getMap().broadcastMessage(CField.SummonPacket.AbsorbentEdificeps(chr.getId(), obj.getObjectId(), 10, 5));
                           break;
                        case 400011077:
                        case 400011078:
                           int force = skillid == 400011077 ? summonEffect.getS() : 12;
                           c.getPlayer().handleForceGain(mob.getObjectId(), skillid, force);
                           break;
                        case 400021033:
                           curseMark = SkillFactory.getSkill(400021032).getEffect(c.getPlayer().getSkillLevel(400021052));
                           statusz.add(new Triple(MonsterStatus.MS_ElementResetBySummon, new MonsterStatusEffect(skillid, summonEffect.getSubTime() > 0 ? summonEffect.getSubTime() : summonEffect.getDuration()), (long)curseMark.getQ2()));
                           break;
                        case 400021067:
                           statusz.add(new Triple(MonsterStatus.MS_Speed, new MonsterStatusEffect(skillid, summonEffect.getSubTime() > 0 ? summonEffect.getSubTime() : summonEffect.getDuration()), (long)summonEffect.getV()));
                           if (mob.getFreezingOverlap() < 5) {
                              mob.setFreezingOverlap((byte)(mob.getFreezingOverlap() + (numAttacked == 1 ? summonEffect.getZ() : 1)));
                           }
                           break;
                        case 400021069:
                           if (!chr.getBuffedValue(32121056) && chr.getBuffedValue(400021069)) {
                              double nowPlus = mob.getStats().isBoss() ? (double)summonEffect.getZ() : (mob.getHp() <= totDamageToOneMonster ? 0.2D : 0.0D);
                              nowPlus *= 1000.0D;
                              plustime += (int)nowPlus;
                              if (nowPlus > 0.0D) {
                                 MapleAtom atom = new MapleAtom(true, mob.getObjectId(), 29, true, 400021069, mob.getTruePosition().x, mob.getTruePosition().y);
                                 atom.setDwUserOwner(chr.getId());
                                 atom.setDwFirstTargetId(0);
                                 ForceAtom fr = new ForceAtom(4, 37, Randomizer.rand(5, 10), 62, 0);
                                 fr.setnAttackCount((int)nowPlus);
                                 atom.addForceAtom(fr);
                                 chr.getClient().send(CField.createAtom(atom));
                              }
                           }
                           break;
                        case 400051023:
                           summonEffect = SkillFactory.getSkill(400051022).getEffect(chr.getSkillLevel(400051023));
                           if (mob.getBuff(MonsterStatus.MS_FixdamRBuff) == null && mob.getCustomValue0(400051023) > 0L) {
                              mob.setCustomInfo(400051023, 0, 0);
                           }

                           if (mob.getCustomValue0(400051023) < 10L) {
                              mob.addSkillCustomInfo(400051023, 1L);
                           }

                           statusz.add(new Triple(MonsterStatus.MS_FixdamRBuff, new MonsterStatusEffect(skillid, summonEffect.getS2() * 1000), mob.getCustomValue0(400051023)));
                        }

                        List<Pair<MonsterStatus, MonsterStatusEffect>> applys = new ArrayList();
                        List<Pair<MonsterStatus, MonsterStatusEffect>> applys2 = new ArrayList();
                        Iterator var56 = statusz.iterator();

                        Triple status;
                        while(var56.hasNext()) {
                           status = (Triple)var56.next();
                           if (status.left != null && status.mid != null && summonEffect.makeChanceResult()) {
                              if (status.left == MonsterStatus.MS_Burned && (Long)status.right < 0L) {
                                 status.right = (Long)status.right & 4294967295L;
                              }

                              ((MonsterStatusEffect)status.mid).setValue((Long)status.right);
                              applys.add(new Pair((MonsterStatus)status.left, (MonsterStatusEffect)status.mid));
                           }
                        }

                        var56 = statusz2.iterator();

                        while(var56.hasNext()) {
                           status = (Triple)var56.next();
                           if (status.left != null && status.mid != null && summonEffect.makeChanceResult()) {
                              if (status.left == MonsterStatus.MS_Burned && (Long)status.right < 0L) {
                                 status.right = (Long)status.right & 4294967295L;
                              }

                              ((MonsterStatusEffect)status.mid).setValue((Long)status.right);
                              applys2.add(new Pair((MonsterStatus)status.left, (MonsterStatusEffect)status.mid));
                           }
                        }

                        mob.applyStatus(c, applys, summonEffect);
                        if (applys2.size() > 0) {
                           mob.applyStatus(c, applys2, summonEffect);
                        }

                        statusz.clear();
                        if (chr.getBuffedValue(SecondaryStat.BMageDeath) != null && GameConstants.isBMDarkAtackSkill(skillid) && chr.getBuffedValue(SecondaryStat.AttackCountX) != null) {
                           chr.setSkillCustomInfo(32001014, 0L, -500L);
                        }

                        switch(skillid2) {
                        case 33001016:
                           if (mob.getBuff(MonsterStatus.MS_JaguarBleeding) == null && mob.getAnotherByte() > 0) {
                              mob.setAnotherByte(0);
                           }

                           chr.addHP(chr.getStat().getCurrentMaxHp() * (long)SkillFactory.getSkill(33001016).getEffect(chr.getSkillLevel(33001016)).getQ() / 100L);
                           if (mob.getAnotherByte() == 0) {
                              mob.setAnotherByte(1);
                              statusz.add(new Triple(MonsterStatus.MS_JaguarBleeding, new MonsterStatusEffect(33000036, 15000), (long)mob.getAnotherByte()));
                           } else if (Randomizer.isSuccess(30)) {
                              if (mob.getAnotherByte() < 3) {
                                 mob.setAnotherByte(mob.getAnotherByte() + 1);
                              }

                              statusz.add(new Triple(MonsterStatus.MS_JaguarBleeding, new MonsterStatusEffect(33000036, 15000), (long)mob.getAnotherByte()));
                           }
                           break;
                        case 33101115:
                           if (mob.getBuff(MonsterStatus.MS_JaguarBleeding) == null && mob.getAnotherByte() > 0) {
                              mob.setAnotherByte(0);
                           }

                           statusz.add(new Triple(MonsterStatus.MS_Stun, new MonsterStatusEffect(skillid, 3000), 1L));
                           if (mob.getAnotherByte() == 0) {
                              mob.setAnotherByte(1);
                              statusz.add(new Triple(MonsterStatus.MS_JaguarBleeding, new MonsterStatusEffect(33000036, 15000), (long)mob.getAnotherByte()));
                           } else if (Randomizer.isSuccess(80)) {
                              if (mob.getAnotherByte() < 3) {
                                 mob.setAnotherByte(mob.getAnotherByte() + 1);
                              }

                              statusz.add(new Triple(MonsterStatus.MS_JaguarBleeding, new MonsterStatusEffect(33000036, 15000), (long)mob.getAnotherByte()));
                           }
                           break;
                        case 33111015:
                           if (mob.getBuff(MonsterStatus.MS_JaguarBleeding) == null && mob.getAnotherByte() > 0) {
                              mob.setAnotherByte(0);
                           }

                           if (mob.getAnotherByte() == 0) {
                              mob.setAnotherByte(1);
                              statusz.add(new Triple(MonsterStatus.MS_JaguarBleeding, new MonsterStatusEffect(33000036, 15000), (long)mob.getAnotherByte()));
                           } else if (Randomizer.isSuccess(40)) {
                              if (mob.getAnotherByte() < 3) {
                                 mob.setAnotherByte(mob.getAnotherByte() + 1);
                              }

                              statusz.add(new Triple(MonsterStatus.MS_JaguarBleeding, new MonsterStatusEffect(33000036, 15000), (long)mob.getAnotherByte()));
                           }
                           break;
                        case 33121017:
                           statusz2.add(new Triple(MonsterStatus.MS_Freeze, new MonsterStatusEffect(33121017, 10000), 1L));
                           statusz2.add(new Triple(MonsterStatus.MS_Smite, new MonsterStatusEffect(33121017, 10000), 1L));
                           if (mob.getBuff(MonsterStatus.MS_JaguarBleeding) == null && mob.getAnotherByte() > 0) {
                              mob.setAnotherByte(0);
                           }

                           if (mob.getAnotherByte() < 3) {
                              mob.setAnotherByte(mob.getAnotherByte() + 1);
                           }

                           statusz.add(new Triple(MonsterStatus.MS_JaguarBleeding, new MonsterStatusEffect(33000036, 15000), (long)mob.getAnotherByte()));
                           break;
                        case 33121255:
                           if (mob.getBuff(MonsterStatus.MS_JaguarBleeding) == null && mob.getAnotherByte() > 0) {
                              mob.setAnotherByte(0);
                           }

                           if (mob.getAnotherByte() < 3) {
                              mob.setAnotherByte(mob.getAnotherByte() + 1);
                           }

                           statusz.add(new Triple(MonsterStatus.MS_JaguarBleeding, new MonsterStatusEffect(33000036, 15000), (long)mob.getAnotherByte()));
                           break;
                        default:
                           if (skillid >= 33001007 && skillid <= 33001015 && Randomizer.isSuccess(15)) {
                              if (mob.getBuff(MonsterStatus.MS_JaguarBleeding) == null && mob.getAnotherByte() > 0) {
                                 mob.setAnotherByte(0);
                              }

                              if (mob.getAnotherByte() < 3) {
                                 mob.setAnotherByte(mob.getAnotherByte() + 1);
                              }

                              statusz.add(new Triple(MonsterStatus.MS_JaguarBleeding, new MonsterStatusEffect(33000036, 15000), (long)mob.getAnotherByte()));
                           }
                        }

                        applys.clear();
                        var56 = statusz.iterator();

                        while(var56.hasNext()) {
                           status = (Triple)var56.next();
                           if (status.left != null && status.mid != null) {
                              if (status.left == MonsterStatus.MS_Burned && (Long)status.right < 0L) {
                                 status.right = (Long)status.right & 4294967295L;
                              }

                              ((MonsterStatusEffect)status.mid).setValue((Long)status.right);
                              applys.add(new Pair((MonsterStatus)status.left, (MonsterStatusEffect)status.mid));
                           }
                        }

                        var56 = statusz2.iterator();

                        while(var56.hasNext()) {
                           status = (Triple)var56.next();
                           if (status.left != null && status.mid != null) {
                              if (status.left == MonsterStatus.MS_Burned && (Long)status.right < 0L) {
                                 status.right = (Long)status.right & 4294967295L;
                              }

                              ((MonsterStatusEffect)status.mid).setValue((Long)status.right);
                              applys2.add(new Pair((MonsterStatus)status.left, (MonsterStatusEffect)status.mid));
                           }
                        }

                        mob.applyStatus(c, applys, summonEffect);
                        if (applys2.size() > 0) {
                           mob.applyStatus(c, applys2, summonEffect);
                        }

                        if (sse != null && sse.delay > 0 && summon.getMovementType() != SummonMovementType.STATIONARY && summon.getMovementType() != SummonMovementType.CIRCLE_STATIONARY && summon.getMovementType() != SummonMovementType.WALK_STATIONARY && chr.getTruePosition().distanceSq(mob.getTruePosition()) > 400000.0D) {
                        }

                        mob.damage(chr, totDamageToOneMonster, true);
                        chr.checkMonsterAggro(mob);
                        if (mob.getStats().isBoss()) {
                           ++attackbossmob;
                        }
                     } while(mob.isAlive());

                     chr.getClient().getSession().writeAndFlush(MobPacket.killMonster(mob.getObjectId(), 1));
                     if (mob.isBuffed(summonSkill.getId()) && GameConstants.isBattleMage(chr.getJob())) {
                        byte size = 0;
                        Iterator var60 = map.getAllSummonsThreadsafe().iterator();

                        while(var60.hasNext()) {
                           MapleSummon sum = (MapleSummon)var60.next();
                           if (sum.getSkill() == skillid) {
                              ++size;
                           }
                        }

                        if (size < 10 && skillid != 32001014 && skillid != 2111010 && skillid != 32100010 && skillid != 32110017 && skillid != 32120019 && !summon.isNoapply()) {
                           summonEffect.applyTo(chr, false);
                        }
                     }

                     ++n3;
                     ++killmobsize;
                  }
               }
            }
         }
      }
   }

   public static final void RemoveSummon(LittleEndianAccessor slea, MapleClient c) {
      MapleMapObject obj = c.getPlayer().getMap().getMapObject(slea.readInt(), MapleMapObjectType.SUMMON);
      if (obj != null && obj instanceof MapleSummon) {
         MapleSummon summon = (MapleSummon)obj;
         if (summon.getSkill() == 2211015) {
            summon.removeSummon(c.getPlayer().getMap(), false);
         } else if (summon.getOwner().getId() == c.getPlayer().getId() && summon.getSkillLevel() > 0) {
            if (summon.getSkill() != 35111002 && summon.getSkill() != 400031049 && summon.getSkill() != 1301013) {
               if (summon.getSkill() == 400021047 || summon.getSkill() == 400021063 || summon.getSkill() == 400031047 || summon.getSkill() == 400041033 || summon.getSkill() == 400041034) {
                  byte type = slea.readByte();
                  int skillid = slea.readInt();
                  int level = slea.readInt();
                  int unk1 = slea.readInt();
                  int unk2 = slea.readInt();
                  int bullet = slea.readInt();
                  Point pos1 = slea.readPos();
                  Point pos2 = slea.readIntPos();
                  Point pos3 = slea.readIntPos();
                  int unk3 = slea.readInt();
                  int unk4 = slea.readInt();
                  int unk5 = slea.readByte();
                  slea.skip(4);
                  List<MatrixSkill> skills = GameConstants.matrixSkills(slea);
                  c.getSession().writeAndFlush(CWvsContext.MatrixSkill(skillid, level, skills));
                  c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.SummonPacket.getSummonSkillAttackEffect(summon, type, skillid, level, unk1, unk2, bullet, pos1, pos2, pos3, unk3, unk4, unk5, skills), false);
               }

               if (summon.getSkill() != 400031047 && summon.getSkill() != 400041048 && summon.getSkill() != 400021047 && summon.getSkill() != 400021063) {
                  summon.removeSummon(c.getPlayer().getMap(), false);
                  if (summon.getSkill() != 35121011 && summon.getSkill() != 400051011) {
                     c.getPlayer().cancelEffect(SkillFactory.getSkill(summon.getSkill()).getEffect(c.getPlayer().getSkillLevel(summon.getSkill())));
                  }

                  if (summon.getSkill() == 2211011) {
                     SecondaryStatEffect eff = SkillFactory.getSkill(2211015).getEffect(c.getPlayer().getSkillLevel(2211015));
                     eff.applyTo(c.getPlayer(), eff.getDuration());
                  }

               }
            }
         }
      }
   }

   public static final void SubSummon(LittleEndianAccessor slea, MapleCharacter chr) {
      if (chr != null && chr.getMap() != null) {
         MapleMapObject obj = chr.getMap().getMapObject(slea.readInt(), MapleMapObjectType.SUMMON);
         if (obj != null && obj instanceof MapleSummon) {
            MapleSummon sum = (MapleSummon)obj;
            if (sum != null && sum.getOwner().getId() == chr.getId() && sum.getSkillLevel() > 0 && chr.isAlive()) {
               int skillid;
               SecondaryStatEffect eff = SkillFactory.getSkill(sum.getSkill()).getEffect(sum.getSkillLevel());
               Iterator var6;
               MaplePartyCharacter pchr2;
               MapleCharacter player;
               int rand;
               Skill bHealing;
               Iterator var18;
               MaplePartyCharacter pc;
               int j;
               int ch;
               label292:
               switch(sum.getSkill()) {
               case 1301013:
                  bHealing = SkillFactory.getSkill(slea.readInt());
                  int bHealingLvl = chr.getTotalSkillLevel(bHealing);
                  ch = chr.getTotalSkillLevel(1310013);
                  if (bHealingLvl > 0 && bHealing != null) {
                     SecondaryStatEffect healEffect = bHealing.getEffect(bHealingLvl);
                     if (bHealing.getId() == 1310016) {
                        healEffect.applyTo(chr, true);
                        chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showSummonEffect(chr, 1310016, true));
                        chr.getMap().broadcastMessage(chr, CField.EffectPacket.showSummonEffect(chr, 1310016, false), false);
                        chr.getMap().broadcastMessage(CField.SummonPacket.summonSkill(chr.getId(), sum.getObjectId(), 14));
                     } else if (bHealing.getId() == 1301013) {
                        rand = healEffect.getHp();
                        if (chr.getSkillLevel(1320045) > 0) {
                           rand += (int)(chr.getStat().getCurrentMaxHp() / 100L * (long)SkillFactory.getSkill(1320045).getEffect(1).getX());
                        }

                        chr.addHP((long)rand);
                        chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showSummonEffect(chr, 1301013, true));
                        chr.getMap().broadcastMessage(chr, CField.EffectPacket.showSummonEffect(chr, 1301013, false), false);
                        chr.getMap().broadcastMessage(CField.SummonPacket.summonSkill(chr.getId(), sum.getObjectId(), 15));
                     }
                     break;
                  }

                  return;
               case 5210015:
                  slea.skip(18);
                  skillid = slea.readInt();
                  List<SecondAtom> atoms = new ArrayList();
                  atoms.add(new SecondAtom(34, chr.getId(), skillid, 5201017, 4000, 0, 1, new Point(sum.getTruePosition().x, sum.getTruePosition().y), Arrays.asList(0)));
                  atoms.add(new SecondAtom(34, chr.getId(), skillid, 5201017, 4000, 0, 1, new Point(sum.getTruePosition().x, sum.getTruePosition().y), Arrays.asList(0)));
                  chr.spawnSecondAtom(atoms);
                  break;
               case 25121133:
                  eff = SkillFactory.getSkill(25121209).getEffect(chr.getSkillLevel(25121209));
                  chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, 0, 25121209, 2, 0, 25121133, (byte)(chr.isFacingLeft() ? 1 : 0), true, (Point)null, (String)null, (Item)null));
                  chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, 0, 25121209, 2, 0, 25121133, (byte)(chr.isFacingLeft() ? 1 : 0), false, (Point)null, (String)null, (Item)null), false);
                  eff.applyTo(chr);
                  break;
               case 35111008:
               case 35120002:
                  if (!chr.canSummon(eff.getX() * 1000)) {
                     return;
                  }

                  if (chr.getParty() != null) {
                     var18 = chr.getParty().getMembers().iterator();

                     while(var18.hasNext()) {
                        pc = (MaplePartyCharacter)var18.next();
                        ch = World.Find.findChannel(pc.getId());
                        if (ch > 0) {
                           player = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(pc.getId());
                           if (player != null) {
                              player.addHP((long)((int)((double)(player.getStat().getCurrentMaxHp() * (long)eff.getHp()) / 100.0D)));
                              player.getClient().getSession().writeAndFlush(CField.EffectPacket.showSummonEffect(player, sum.getSkill(), true));
                              player.getMap().broadcastMessage(player, CField.EffectPacket.showSummonEffect(player, sum.getSkill(), false), false);
                           }
                        }
                     }
                  } else {
                     chr.addHP((long)((int)((double)(chr.getStat().getCurrentMaxHp() * (long)eff.getHp()) / 100.0D)));
                     chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showSummonEffect(chr, sum.getSkill(), true));
                     chr.getMap().broadcastMessage(chr, CField.EffectPacket.showSummonEffect(chr, sum.getSkill(), false), false);
                  }

                  List<Pair<MonsterStatus, MonsterStatusEffect>> datas = new ArrayList();
                  datas.add(new Pair(MonsterStatus.MS_IndiePdr, new MonsterStatusEffect(sum.getSkill(), sum.getDuration(), (long)(-SkillFactory.getSkill(35111008).getEffect(sum.getSkillLevel()).getW()))));
                  datas.add(new Pair(MonsterStatus.MS_IndieMdr, new MonsterStatusEffect(sum.getSkill(), sum.getDuration(), (long)(-SkillFactory.getSkill(35111008).getEffect(sum.getSkillLevel()).getW()))));
                  var6 = chr.getMap().getAllMonstersThreadsafe().iterator();

                  while(true) {
                     if (!var6.hasNext()) {
                        break label292;
                     }

                     MapleMonster mob = (MapleMonster)var6.next();
                     if (!mob.isBuffed(sum.getSkill())) {
                        mob.applyStatus(chr.getClient(), datas, eff);
                     }
                  }
               case 35121009:
                  if (!chr.canSummon(eff.getX() * 1000)) {
                     return;
                  }

                  skillid = slea.readInt();
                  if (sum.getSkill() != skillid) {
                     return;
                  }

                  var6 = chr.getMap().getAllSummonsThreadsafe().iterator();

                  MapleSummon summon;
                  do {
                     if (!var6.hasNext()) {
                        break label292;
                     }

                     summon = (MapleSummon)var6.next();
                  } while(summon.getSkill() != sum.getSkill());

                  slea.skip(1);
                  slea.readInt();

                  for(j = 0; j < 3; ++j) {
                     MapleSummon tosummon = new MapleSummon(chr, SkillFactory.getSkill(35121011).getEffect(sum.getSkillLevel()), new Point(sum.getTruePosition().x, sum.getTruePosition().y - 5), SummonMovementType.WALK_STATIONARY);
                     chr.getMap().spawnSummon(tosummon, 5000);
                  }

                  return;
               case 36121014:
                  if (chr.getParty() != null) {
                     var18 = chr.getParty().getMembers().iterator();

                     while(true) {
                        if (!var18.hasNext()) {
                           break label292;
                        }

                        pc = (MaplePartyCharacter)var18.next();
                        MapleCharacter chr2 = chr.getClient().getChannelServer().getPlayerStorage().getCharacterByName(pc.getName());
                        if (chr2 != null && pc.isOnline() && sum.getTruePosition().x + eff.getLt().x < chr2.getTruePosition().x && sum.getTruePosition().x - eff.getLt().x > chr2.getTruePosition().x && sum.getTruePosition().y + eff.getLt().y < chr2.getTruePosition().y && sum.getTruePosition().y - eff.getLt().y > chr2.getTruePosition().y) {
                           SkillFactory.getSkill(36121014).getEffect(sum.getSkillLevel()).applyTo(chr, chr2, false);
                        }
                     }
                  } else {
                     if (sum.getTruePosition().x + eff.getLt().x < chr.getTruePosition().x && sum.getTruePosition().x - eff.getLt().x > chr.getTruePosition().x && sum.getTruePosition().y + eff.getLt().y < chr.getTruePosition().y && sum.getTruePosition().y - eff.getLt().y > chr.getTruePosition().y) {
                        SkillFactory.getSkill(36121014).getEffect(sum.getSkillLevel()).applyTo(chr, false);
                     }
                     break;
                  }
               case 152101000:
                  bHealing = SkillFactory.getSkill(slea.readInt());
                  if (bHealing.getId() == 152111007 && sum.getCrystalSkills().size() >= 2 && (Boolean)sum.getCrystalSkills().get(1)) {
                     chr.getClient().getSession().writeAndFlush(CField.SummonPacket.transformSummon(sum, 1));
                     bHealing.getEffect(chr.getSkillLevel(152111007)).applyTo(chr, false);
                     sum.getCrystalSkills().set(1, false);
                     chr.getClient().getSession().writeAndFlush(CField.SummonPacket.transformSummon(sum, 2));
                     chr.getClient().getSession().writeAndFlush(CField.SummonPacket.ElementalRadiance(sum, 3));
                  }
                  break;
               case 400001013:
                  chr.setSkillCustomInfo(400001016, 2L, 0L);
                  eff = SkillFactory.getSkill(400001016).getEffect(chr.getSkillLevel(400001013));
                  eff.applyTo(chr);
                  break;
               case 400021032:
                  SecondaryStatEffect pismaker = SkillFactory.getSkill(400021052).getEffect(chr.getSkillLevel(400021032));
                  if (chr.getMapId() != 450013700 && chr.getParty() != null) {
                     var6 = chr.getParty().getMembers().iterator();

                     while(var6.hasNext()) {
                        pchr2 = (MaplePartyCharacter)var6.next();
                        player = chr.getClient().getChannelServer().getPlayerStorage().getCharacterById(pchr2.getId());
                        if (player != null) {
                           player.addHP(player.getStat().getCurrentMaxHp() / 100L * 10L);
                           pismaker.applyTo(chr, player);
                        }
                     }
                  }

                  if (chr.getParty() == null) {
                     pismaker.applyTo(chr);
                  }

                  chr.getMap().broadcastMessage(CField.SummonPacket.summonSkill(chr.getId(), sum.getObjectId(), 14));
                  break;
               case 400041038:
                  Item nk = null;

                  for(short position = 0; position < chr.getInventory(MapleInventoryType.USE).newList().size(); ++position) {
                     nk = (Item)chr.getInventory(MapleInventoryType.USE).newList().get(position);
                     if (nk != null && nk.getItemId() / 10000 == 207) {
                        break;
                     }
                  }

                  if (nk != null) {
                     List<MapleMapObject> objs = chr.getMap().getMapObjectsInRange(sum.getTruePosition(), 500000.0D, Arrays.asList(MapleMapObjectType.MONSTER));
                     List<Integer> monsters = new ArrayList();

                     for(j = 0; j < eff.getMobCount(); ++j) {
                        if (objs.size() <= 1) {
                           rand = 1;
                        } else {
                           rand = Randomizer.nextInt(objs.size());
                        }

                        if (objs.size() < eff.getBulletCount()) {
                           if (j < objs.size()) {
                              monsters.add(((MapleMapObject)objs.get(j)).getObjectId());
                           }
                        } else if (objs.size() > 1) {
                           monsters.add(((MapleMapObject)objs.get(rand)).getObjectId());
                           objs.remove(rand);
                        }
                     }

                     if (monsters.size() > 0) {
                        new ArrayList();
                        MapleAtom atom = new MapleAtom(false, chr.getId(), 49, true, 400041038, sum.getTruePosition().x, sum.getTruePosition().y - 400);
                        atom.setDwSummonObjectId(sum.getObjectId());
                        int key = 0;
                        Iterator var11 = monsters.iterator();

                        while(var11.hasNext()) {
                           Integer m = (Integer)var11.next();

                           for(int k = 0; k < eff.getBulletCount(); ++k) {
                              atom.addForceAtom(new ForceAtom(2, Randomizer.rand(40, 44), Randomizer.rand(3, 4), 360 / (monsters.size() * 5 + 7) * key, 200));
                              ++key;
                           }
                        }

                        for(int l = 0; l < eff.getX(); ++l) {
                           atom.addForceAtom(new ForceAtom(2, Randomizer.rand(40, 44), Randomizer.rand(3, 4), 360 / (monsters.size() * 5 + 7) * key, 200));
                           ++key;
                        }

                        atom.setDwTargets(monsters);
                        atom.setnItemId(chr.getV("csstar") != null ? Integer.parseInt(chr.getV("csstar")) : nk.getItemId());
                        chr.getMap().spawnMapleAtom(atom);
                        chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, sum.getSkill(), sum.getSkill(), 4, 0, 0, (byte)0, true, (Point)null, (String)null, (Item)null));
                        chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, sum.getSkill(), sum.getSkill(), 4, 0, 0, (byte)0, false, (Point)null, (String)null, (Item)null), false);
                     }
                  }
                  break;
               case 400041044:
                  Rectangle box = new Rectangle(sum.getTruePosition().x - 320, sum.getTruePosition().y - 490, 640, 530);
                  if (chr.getParty() != null) {
                     var6 = chr.getParty().getMembers().iterator();

                     while(var6.hasNext()) {
                        pchr2 = (MaplePartyCharacter)var6.next();
                        player = chr.getClient().getChannelServer().getPlayerStorage().getCharacterByName(pchr2.getName());
                        if (player != null && pchr2.isOnline() && box.contains(player.getPosition())) {
                           SkillFactory.getSkill(400041047).getEffect(sum.getSkillLevel()).applyTo(chr, player);
                        }
                     }
                  } else if (box.contains(chr.getPosition())) {
                     SkillFactory.getSkill(400041047).getEffect(sum.getSkillLevel()).applyTo(chr);
                  }
               }

               if (GameConstants.isAngel(sum.getSkill()) && chr.getBuffedEffect(SecondaryStat.RepeatEffect) != null) {
                  if (sum.getSkill() % 10000 == 1087) {
                     MapleItemInformationProvider.getInstance().getItemEffect(2022747).applyTo(chr, true);
                  } else if (sum.getSkill() % 10000 == 1085) {
                     MapleItemInformationProvider.getInstance().getItemEffect(2022746).applyTo(chr, true);
                  } else if (sum.getSkill() % 10000 == 1090) {
                     MapleItemInformationProvider.getInstance().getItemEffect(2022764).applyTo(chr, true);
                  } else if (sum.getSkill() % 10000 == 1179) {
                     MapleItemInformationProvider.getInstance().getItemEffect(2022823).applyTo(chr, true);
                  } else {
                     MapleItemInformationProvider.getInstance().getItemEffect(2022746).applyTo(chr, true);
                  }

                  skillid = chr.getBuffedEffect(SecondaryStat.RepeatEffect).getSourceId();
                  chr.getClient().getSession().writeAndFlush(CField.EffectPacket.showSummonEffect(chr, sum.getSkill(), true));
                  chr.getMap().broadcastMessage(chr, CField.EffectPacket.showSummonEffect(chr, sum.getSkill(), false), false);
                  EnumMap<SecondaryStat, Pair<Integer, Integer>> statups = new EnumMap(SecondaryStat.class);
                  statups.put(SecondaryStat.RepeatEffect, new Pair(1, 0));
                  SecondaryStatEffect effect = MapleItemInformationProvider.getInstance().getItemEffect(skillid);
                  chr.getMap().broadcastMessage(CWvsContext.BuffPacket.giveForeignBuff(chr, statups, effect));
               }

            }
         }
      }
   }

   public static void replaceSummon(LittleEndianAccessor slea, MapleClient c) {
      int skillId = slea.readInt();
      Iterator var3 = c.getPlayer().getSummons().iterator();

      while(var3.hasNext()) {
         MapleSummon s = (MapleSummon)var3.next();
         if (GameConstants.getLinkedSkill(s.getSkill()) == skillId) {
            c.getPlayer().getMap().broadcastMessage(CField.SummonPacket.removeSummon(s, false));
            s.setPosition(c.getPlayer().getTruePosition());
            c.getPlayer().getMap().broadcastMessage(CField.SummonPacket.spawnSummon(s, true));
         } else if (skillId == 400031005 && s.getSkill() >= 33001007 && s.getSkill() <= 33001015 && s.getSkill() != GameConstants.getSelectJaguarSkillId(GameConstants.getMountItem(33001001, c.getPlayer()))) {
            c.getPlayer().getMap().broadcastMessage(CField.SummonPacket.removeSummon(s, false));
            s.setPosition(new Point(c.getPlayer().getPosition().x + Randomizer.rand(-400, 400), c.getPlayer().getPosition().y));
            c.getPlayer().getMap().broadcastMessage(CField.SummonPacket.spawnSummon(s, true));
         }
      }

   }

   public static void effectSummon(LittleEndianAccessor slea, MapleClient c) {
      int objectId = slea.readInt();
      if (c.getPlayer() != null && c.getPlayer().getMap() != null) {
         MapleSummon target = c.getPlayer().getMap().getSummonByOid(objectId);
         if (target != null) {
            slea.skip(8);
            int skill = slea.readInt();
            int type = skill == 400011054 ? 11 : (skill == 400021066 ? 9 : 0);
            if (SkillFactory.getSkill(skill) == null) {
               return;
            }

            if (type > 0) {
               c.getPlayer().getMap().broadcastMessage(CField.SummonPacket.summonSkill(c.getPlayer().getId(), target.getObjectId(), type));
            }

            SkillFactory.getSkill(skill).getEffect(c.getPlayer().getSkillLevel(skill)).applyTo(c.getPlayer(), true);
            target.SetNoapply(true);
         }

      }
   }

   public static void cancelEffectSummon(LittleEndianAccessor slea, MapleClient c) {
      int objectId = slea.readInt();
      MapleSummon target = c.getPlayer().getMap().getSummonByOid(objectId);
      if (target != null) {
         target.SetNoapply(false);
         if (target.getSkill() == 400051046) {
            if (target.getLastAttackTime() > 0L && !c.getPlayer().getBuffedValue(400051046)) {
               target.removeSummon(c.getPlayer().getMap(), false);
            }

            target.setLastAttackTime(0L);
         }
      }

   }

   public static void specialSummon(LittleEndianAccessor slea, MapleClient c) {
      int objectId = slea.readInt();
      MapleCharacter chr = c.getPlayer();
      if (chr != null && chr.getMap() != null) {
         MapleSummon target = chr.getMap().getSummonByOid(objectId);
         if (target != null && target.getSkill() != 152101000) {
         }

      }
   }

   public static void specialSummon5th(LittleEndianAccessor slea, MapleClient c) {
      int objectId = slea.readInt();
      Point pos = slea.readPos();
      int skillid5th = slea.readInt();
      int skillid = slea.readInt();
      MapleCharacter chr = c.getPlayer();
      if (chr != null && chr.getMap() != null) {
         MapleSummon target = chr.getMap().getSummonByOid(objectId);
         if (target != null && System.currentTimeMillis() - target.getLastAttackTime() > (long)SkillFactory.getSkill(skillid).getEffect(c.getPlayer().getSkillLevel(skillid)).getCooldown(chr)) {
            target.setLastAttackTime(System.currentTimeMillis());
            c.getPlayer().getMap().broadcastMessage(CField.SummonPacket.specialSummon(target, 4, skillid));
         }

      }
   }

   public static void mechCarrier(LittleEndianAccessor slea, MapleClient c) {
   }
}
