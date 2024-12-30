package client.damage;

import client.MapleCharacter;
import client.PlayerStats;
import client.Skill;
import client.SkillFactory;
import client.StructPotentialItem;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MapleWeaponType;
import constants.GameConstants;
import handling.world.PlayerBuffValueHolder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import server.MapleItemInformationProvider;
import server.SecondaryStatEffect;
import server.StructSetItem;
import tools.Pair;

public class VerifyDamage {
   public static double CalculatePlayerStats(MapleCharacter player) {
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      int base_str = false;
      int base_dex = false;
      int base_int = false;
      int base_luk = false;
      int add_str = 0;
      int add_dex = 0;
      int add_int = 0;
      int add_luk = 0;
      int char_str = false;
      int char_dex = false;
      int char_int = false;
      int char_luk = false;
      int per_str = 0;
      int per_dex = 0;
      int per_int = 0;
      int per_luk = 0;
      int final_str = 0;
      int final_dex = 0;
      int final_int = 0;
      int final_luk = 0;
      int total_str = false;
      int total_dex = false;
      int total_int = false;
      int total_luk = false;
      int base_mhp = false;
      int base_mmp = false;
      int add_mhp = 0;
      int add_mmp = 0;
      int char_mhp = false;
      int char_mmp = false;
      int per_mhp = 0;
      int per_mmp = 0;
      int final_mhp = 0;
      int final_mmp = 0;
      int total_mhp = false;
      int total_mmp = false;
      int inc_meso = 0;
      int inc_drop = 0;
      int base_watk = 0;
      int base_matk = 0;
      int add_watk = 0;
      int add_matk = 0;
      int char_watk = false;
      int char_matk = false;
      int per_watk = 0;
      int per_matk = 0;
      int final_watk = 0;
      int final_matk = 0;
      int total_watk = false;
      int total_matk = false;
      int total_damage = 0;
      int base_boss_damage = 0;
      int per_boss_damage = 0;
      int base_ignore = false;
      int per_ignore = 0;
      int reduce_cooltime = 0;
      int main_stat = 0;
      int second_stat = 0;
      int joker_item_id = 0;
      double total_final_damage = 1.0D;
      Map<Integer, Byte> SetOptions = new HashMap();
      List<Pair<Integer, Integer>> potentials = new ArrayList();
      synchronized(player.getInventory(MapleInventoryType.EQUIPPED)) {
         Iterator itera = player.getInventory(MapleInventoryType.EQUIPPED).newList().iterator();

         while(true) {
            if (!itera.hasNext()) {
               break;
            }

            Equip equip = (Equip)itera.next();
            if (equip.getItemId() / 1000 == 1672) {
               Item android = player.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-27);
               if (android == null) {
                  continue;
               }
            }

            int potlevel = ii.getReqLevel(equip.getItemId()) / 10 - 1;
            potentials.add(new Pair(equip.getPotential1(), potlevel));
            potentials.add(new Pair(equip.getPotential2(), potlevel));
            potentials.add(new Pair(equip.getPotential3(), potlevel));
            potentials.add(new Pair(equip.getPotential4(), potlevel));
            potentials.add(new Pair(equip.getPotential5(), potlevel));
            potentials.add(new Pair(equip.getPotential6(), potlevel));
            Integer set = ii.getSetItemID(equip.getItemId());
            if (set != null && set > 0) {
               byte value = 1;
               if (SetOptions.containsKey(set)) {
                  value += (Byte)SetOptions.get(set);
               }

               SetOptions.put(set, value);
            }

            if (ii.getEquipStats(equip.getItemId()) != null && ii.getEquipStats(equip.getItemId()).get("MHPr") != null) {
               per_mhp += (Integer)ii.getEquipStats(equip.getItemId()).get("MHPr");
            }

            if (ii.getEquipStats(equip.getItemId()) != null && ii.getEquipStats(equip.getItemId()).get("MMPr") != null) {
               per_mmp += (Integer)ii.getEquipStats(equip.getItemId()).get("MMPr");
            }

            if (ii.isJokerToSetItem(equip.getItemId()) && joker_item_id > equip.getItemId()) {
               joker_item_id = equip.getItemId();
            }

            per_dex += equip.getAllStat();
            per_int += equip.getAllStat();
            per_str += equip.getAllStat();
            per_luk += equip.getAllStat();
            total_damage += equip.getTotalDamage();
            base_boss_damage += equip.getBossDamage();
            add_mhp += equip.getHp();
            add_mmp += equip.getMp();
            add_dex += equip.getDex();
            add_int += equip.getInt();
            add_str += equip.getStr();
            add_luk += equip.getLuk();
            add_watk += equip.getWatk();
            add_matk += equip.getMatk();
         }
      }

      Iterator var65 = SetOptions.keySet().iterator();

      label319:
      while(true) {
         Integer setId;
         StructSetItem set;
         int i;
         do {
            if (!var65.hasNext()) {
               var65 = potentials.iterator();

               while(var65.hasNext()) {
                  Pair<Integer, Integer> potential = (Pair)var65.next();
                  int lv = (Integer)potential.right;
                  if (lv < 0) {
                     lv = 0;
                  }

                  if ((Integer)potential.left != 0 && ii.getPotentialInfo((Integer)potential.left) != null && ii.getPotentialInfo((Integer)potential.left).get(lv) != null) {
                     StructPotentialItem pot = (StructPotentialItem)ii.getPotentialInfo((Integer)potential.left).get(lv);
                     add_mhp += pot.incMHP / (GameConstants.isDemonAvenger(player.getJob()) ? 2 : 1);
                     add_mmp += pot.incMMP;
                     per_mhp += pot.incMHPr / (GameConstants.isDemonAvenger(player.getJob()) ? 2 : 1);
                     per_mmp += pot.incMMPr;
                     inc_meso += pot.incMesoProp;
                     inc_drop += pot.incRewardProp;
                     reduce_cooltime += pot.reduceCooltime;
                     add_dex += pot.incDEX;
                     add_int += pot.incINT;
                     add_str += pot.incSTR;
                     add_luk += pot.incLUK;
                     per_dex += pot.incDEXr;
                     per_int += pot.incINTr;
                     per_str += pot.incSTRr;
                     per_luk += pot.incLUKr;
                     per_watk += pot.incPADr;
                     per_matk += pot.incMADr;
                     if (!pot.boss) {
                        total_damage += pot.incDAMr;
                     } else {
                        per_boss_damage += pot.incDAMr;
                     }

                     per_ignore += pot.ignoreTargetDEF;
                  }
               }

               List<SecondaryStatEffect> effects = new ArrayList();
               Set<Skill> skills = player.getSkills().keySet();
               Iterator var110 = skills.iterator();

               while(var110.hasNext()) {
                  Skill skill = (Skill)var110.next();
                  int skillLevel = player.getTotalSkillLevel(skill);
                  if (skill.getPsd() == 1) {
                     effects.add(skill.getEffect(skillLevel));
                  }
               }

               var110 = player.getAllBuffs().iterator();

               while(var110.hasNext()) {
                  PlayerBuffValueHolder skill = (PlayerBuffValueHolder)var110.next();
                  effects.add(skill.effect);
               }

               var110 = effects.iterator();

               while(true) {
                  SecondaryStatEffect effect;
                  Skill skill;
                  do {
                     do {
                        do {
                           if (!var110.hasNext()) {
                              int base_str = player.getStat().getStr();
                              int base_dex = player.getStat().getDex();
                              int base_int = player.getStat().getInt();
                              int base_luk = player.getStat().getLuk();
                              int base_mhp = (int)player.getStat().getMaxHp();
                              int base_mmp = (int)player.getStat().getMaxMp();
                              int char_str = base_str + add_str;
                              int char_dex = base_dex + add_dex;
                              int char_int = base_int + add_int;
                              int char_luk = base_luk + add_luk;
                              int char_mhp = base_mhp + add_mhp;
                              int char_mmp = base_mmp + add_mmp;
                              int char_watk = base_watk + add_watk;
                              int char_matk = base_matk + add_matk;
                              int total_str = (int)((double)char_str * (1.0D + (double)per_str / 100.0D)) + final_str;
                              int total_dex = (int)((double)char_dex * (1.0D + (double)per_dex / 100.0D)) + final_dex;
                              int total_int = (int)((double)char_int * (1.0D + (double)per_int / 100.0D)) + final_int;
                              int total_luk = (int)((double)char_luk * (1.0D + (double)per_luk / 100.0D)) + final_luk;
                              int total_mhp = (int)((double)char_mhp * (1.0D + (double)per_mhp / 100.0D)) + final_mhp;
                              int total_mmp = (int)((double)char_mmp * (1.0D + (double)per_mmp / 100.0D)) + final_mmp;
                              int total_watk = (int)((double)char_watk * (1.0D + (double)per_watk / 100.0D)) + final_watk;
                              int total_matk = (int)((double)char_matk * (1.0D + (double)per_matk / 100.0D)) + final_matk;
                              Item weapon_item = player.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
                              int job = player.getJob();
                              MapleWeaponType weapon = weapon_item == null ? MapleWeaponType.NOT_A_WEAPON : GameConstants.getWeaponType(weapon_item.getItemId());
                              switch(weapon) {
                              case STAFF:
                              case WAND:
                                 main_stat = total_int;
                                 second_stat = total_luk;
                                 break;
                              case BOW:
                              case CROSSBOW:
                              case GUN:
                                 main_stat = total_dex;
                                 second_stat = total_str;
                                 break;
                              case CLAW:
                              case DAGGER:
                              case KATARA:
                                 if ((job < 400 || job > 434) && (job < 1400 || job > 1412)) {
                                    main_stat = total_str;
                                    second_stat = total_dex;
                                 } else {
                                    main_stat = total_luk;
                                    second_stat = total_dex + total_str;
                                 }
                                 break;
                              case KNUCKLE:
                                 main_stat = total_str;
                                 second_stat = total_dex;
                                 break;
                              case TUNER:
                                 main_stat = total_str;
                                 second_stat = total_dex;
                                 break;
                              case NOT_A_WEAPON:
                                 if (job >= 500 && job <= 522 || job >= 1500 && job <= 1512 || job >= 3500 && job <= 3512) {
                                    main_stat = total_str;
                                    second_stat = total_dex;
                                 }
                                 break;
                              default:
                                 main_stat = total_str;
                                 second_stat = total_dex;
                              }

                              boolean magician = job >= 200 && job <= 232 || job >= 1200 && job <= 1212 || job >= 2200 && job <= 2218 || job >= 3200 && job <= 3212;
                              boolean is_mage_constant = job >= 200 && job <= 232 || GameConstants.isFlameWizard(job);
                              double constant_job = is_mage_constant ? 1.2D : (GameConstants.isXenon(job) ? 0.875D : 1.0D);
                              double maxbasedamage = ((double)main_stat * 4.0D + (double)second_stat) * 0.01D * (double)(magician ? char_matk : char_watk) * (double)weapon.getMaxDamageMultiplier() * constant_job * (1.0D + (double)(magician ? per_matk : per_watk) / 100.0D) * (1.0D + (double)total_damage / 100.0D) * total_final_damage;
                              return maxbasedamage;
                           }

                           effect = (SecondaryStatEffect)var110.next();
                           skill = SkillFactory.getSkill(effect.getSourceId());
                        } while(effect.getLevel() <= 0);
                     } while(skill.getPsdSkills() == null);
                  } while(skill.getPsdSkills().size() != 0);

                  for(i = 0; i < (String.valueOf(skill.getId()).startsWith("7000") ? 2 : 1); ++i) {
                     add_str += effect.getStrX() + effect.getStr();
                     add_dex += effect.getDexX() + effect.getDex();
                     add_int += effect.getIntX() + effect.getInt();
                     add_luk += effect.getLukX() + effect.getLuk();
                     add_mhp += effect.getMaxHpX();
                     add_mmp += effect.getMaxMpX();
                     per_mhp += effect.getPercentHP();
                     per_mmp += effect.getPercentMP();
                     per_watk += effect.getWatk();
                     per_matk += effect.getMatk();
                     add_watk += effect.getAttackX();
                     add_matk += effect.getMagicX();
                     add_watk += effect.getWatk();
                     add_matk += effect.getMatk();
                     final_str += effect.getStrFX();
                     final_dex += effect.getDexFX();
                     final_int += effect.getIntFX();
                     final_luk += effect.getLukFX();
                     total_damage += effect.getDAMRate();
                     total_final_damage *= 1.0D + (double)effect.getPdR() / 100.0D;
                     total_final_damage *= 1.0D + (double)effect.getMdR() / 100.0D;
                     per_mhp += effect.getHpFX();
                     switch(skill.getId()) {
                     case 12100027:
                     case 12120009:
                        add_matk += effect.getX();
                        break;
                     case 12110025:
                        total_final_damage *= 1.0D + (double)effect.getZ() / 100.0D;
                     }
                  }
               }
            }

            setId = (Integer)var65.next();
            set = ii.getSetItem(setId);
         } while(set == null);

         Map<Integer, StructSetItem.SetItem> itemz = set.getItems();
         Iterator var112;
         if (set.jokerPossible && joker_item_id > 0 && (Byte)SetOptions.get(setId) < set.completeCount) {
            var112 = set.itemIDs.iterator();

            while(var112.hasNext()) {
               i = (Integer)var112.next();
               if (GameConstants.isWeapon(i) && GameConstants.isWeapon(joker_item_id)) {
                  SetOptions.put(setId, (byte)((Byte)SetOptions.get(setId) + 1));
                  break;
               }

               if (!GameConstants.isWeapon(i) && !GameConstants.isWeapon(joker_item_id) && i / 10000 == joker_item_id / 10000 && player.getInventory(MapleInventoryType.EQUIPPED).findById(i) == null) {
                  SetOptions.put(setId, (byte)((Byte)SetOptions.get(setId) + 1));
                  break;
               }
            }
         }

         var112 = itemz.entrySet().iterator();

         while(true) {
            Entry ent;
            do {
               if (!var112.hasNext()) {
                  continue label319;
               }

               ent = (Entry)var112.next();
            } while((Integer)ent.getKey() > (Byte)SetOptions.get(setId));

            StructSetItem.SetItem se = (StructSetItem.SetItem)ent.getValue();
            add_str += se.incSTR + se.incAllStat;
            add_dex += se.incDEX + se.incAllStat;
            add_int += se.incINT + se.incAllStat;
            add_luk += se.incLUK + se.incAllStat;
            add_watk += se.incPAD;
            add_matk += se.incMAD;
            int[][] options = new int[][]{{se.option1, se.option1Level}, {se.option2, se.option2Level}};
            int[][] var73 = options;
            int var74 = options.length;

            for(int var75 = 0; var75 < var74; ++var75) {
               int[] option = var73[var75];
               if (ii.getPotentialInfo(option[0]) != null) {
                  potentials.add(new Pair(option[0], option[1]));
               }
            }

            add_mhp += se.incMHP;
            add_mmp += se.incMMP;
            per_mhp += se.incMHPr;
            per_mmp += se.incMMPr;
         }
      }
   }

   public static float getMaxBaseDamage(MapleCharacter player) {
      PlayerStats stat = player.getStat();
      stat.recalcLocalStats(player);
      return stat.getCurrentMaxBaseDamage();
   }

   public static float getMinBaseDamage(MapleCharacter player) {
      PlayerStats stat = player.getStat();
      stat.recalcLocalStats(player);
      return (float)stat.calculateMinBaseDamage(player);
   }
}
