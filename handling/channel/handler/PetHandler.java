package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.SecondaryStat;
import client.Skill;
import client.SkillEntry;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.PetCommand;
import constants.GameConstants;
import io.netty.channel.Channel;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.StructSetItem;
import server.maps.FieldLimitType;
import server.movement.LifeMovementFragment;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.PetPacket;

public class PetHandler {
   public static void SpawnPet(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      slea.skip(4);
      byte slot = slea.readByte();
      slea.readByte();
      Item item = chr.getInventory(MapleInventoryType.CASH).getItem((short)slot);
      if (item == null) {
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      } else {
         if (ItemFlag.KARMA_USE.check(item.getFlag())) {
            item.setFlag(item.getFlag() - ItemFlag.KARMA_USE.getValue());
         }

         MaplePet pet = item.getPet();
         if (pet != null) {
            if (chr.getPetIndex(pet) != -1) {
               chr.unequipPet(pet, false, false);
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               updatePetSkills(chr, pet);
               return;
            }

            if (item.getExpiration() > System.currentTimeMillis()) {
               Point pos = chr.getPosition();
               pet.setPos(pos);
               if (chr.getMap().getFootholds() != null && chr.getMap().getFootholds().findBelow(pet.getPos()) != null) {
                  pet.setFh(chr.getMap().getFootholds().findBelow(pet.getPos()).getId());
               }

               pet.setStance(0);
               chr.addPet(pet);
               chr.getMap().broadcastMessage(chr, PetPacket.showPet(chr, pet, false, false), true);
               c.getSession().writeAndFlush(PetPacket.updatePet(c.getPlayer(), pet, c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(pet.getInventoryPosition()), false, c.getPlayer().getPetLoot()));
               updatePetSkills(chr, (MaplePet)null);
            } else {
               c.getPlayer().getInventory(MapleInventoryType.CASH).removeItem((short)slot);
               c.getSession().writeAndFlush(CWvsContext.InventoryPacket.clearInventoryItem(MapleInventoryType.CASH, (short)slot, false));
            }
         }

         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      }
   }

   public static final void Pet_AutoPotion(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      slea.skip(1);
      slea.readInt();
      short slot = slea.readShort();
      if (chr != null && chr.isAlive() && chr.getBuffedEffect(SecondaryStat.DebuffIncHp) == null && chr.getMap() != null && !chr.hasDisease(SecondaryStat.StopPortion) && chr.getBuffedValue(SecondaryStat.StopPortion) == null) {
         Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
         if (toUse != null && toUse.getQuantity() >= 1 && toUse.getItemId() == slea.readInt() && chr.getBuffedEffect(SecondaryStat.Reincarnation) == null) {
            long time = System.currentTimeMillis();
            if (chr.getNextConsume() <= time) {
               if (!FieldLimitType.PotionUse.check(chr.getMap().getFieldLimit()) && MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId()).applyTo(chr, true)) {
                  if (toUse.getItemId() != 2000054) {
                     MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
                  }

                  if (chr.getMap().getConsumeItemCoolTime() > 0) {
                     chr.setNextConsume(time + (long)(chr.getMap().getConsumeItemCoolTime() * 1000));
                  }
               }

            }
         }
      }
   }

   public static final void PetChat(int petid, short command, String text, MapleCharacter chr) {
      if (chr != null && chr.getMap() != null && chr.getPet((long)petid) != null) {
         chr.getMap().broadcastMessage(chr, PetPacket.petChat(chr.getId(), command, text, (byte)petid), true);
      }
   }

   public static final void PetCommand(MaplePet pet, PetCommand petCommand, MapleClient c, MapleCharacter chr) {
      if (petCommand != null) {
         byte petIndex = (byte)chr.getPetIndex(pet);
         boolean success = false;
         if (Randomizer.nextInt(99) <= petCommand.getProbability()) {
            success = true;
            if (pet.getCloseness() < 30000) {
               int newCloseness = pet.getCloseness() + petCommand.getIncrease() * c.getChannelServer().getTraitRate();
               if (newCloseness > 30000) {
                  newCloseness = 30000;
               }

               pet.setCloseness(newCloseness);
               if (newCloseness >= GameConstants.getClosenessNeededForLevel(pet.getLevel() + 1)) {
                  pet.setLevel(pet.getLevel() + 1);
                  c.getSession().writeAndFlush(CField.EffectPacket.showPetLevelUpEffect(c.getPlayer(), pet.getPetItemId(), true));
                  chr.getMap().broadcastMessage(CField.EffectPacket.showPetLevelUpEffect(c.getPlayer(), pet.getPetItemId(), false));
               }

               c.getSession().writeAndFlush(PetPacket.updatePet(chr, pet, chr.getInventory(MapleInventoryType.CASH).getItem(pet.getInventoryPosition()), false, chr.getPetLoot()));
            }
         }

         chr.getMap().broadcastMessage(PetPacket.commandResponse(chr.getId(), (byte)petCommand.getSkillId(), petIndex, success));
      }
   }

   public static void PetFood(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      int previousFullness = 100;
      c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      MaplePet[] var4 = chr.getPets();
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         MaplePet pet = var4[var6];
         if (pet != null && pet.getFullness() < previousFullness) {
            int previousFullness = pet.getFullness();
            slea.skip(6);
            int itemId = slea.readInt();
            boolean gainCloseness = false;
            if (Randomizer.nextInt(99) <= 50) {
               gainCloseness = true;
            }

            int newCloseness2;
            if (pet.getFullness() < 100) {
               newCloseness2 = pet.getFullness() + 30;
               if (newCloseness2 > 100) {
                  newCloseness2 = 100;
               }

               pet.setFullness(newCloseness2);
               int index = chr.getPetIndex(pet);
               if (gainCloseness && pet.getCloseness() < 30000) {
                  int newCloseness = pet.getCloseness() + 1;
                  if (newCloseness > 30000) {
                     newCloseness = 30000;
                  }

                  pet.setCloseness(newCloseness);
                  if (newCloseness >= GameConstants.getClosenessNeededForLevel(pet.getLevel() + 1)) {
                     pet.setLevel(pet.getLevel() + 1);
                     c.getSession().writeAndFlush(CField.EffectPacket.showPetLevelUpEffect(c.getPlayer(), pet.getPetItemId(), true));
                     chr.getMap().broadcastMessage(CField.EffectPacket.showPetLevelUpEffect(c.getPlayer(), pet.getPetItemId(), false));
                  }
               }

               c.getSession().writeAndFlush(PetPacket.updatePet(chr, pet, chr.getInventory(MapleInventoryType.CASH).getItem(pet.getInventoryPosition()), false, chr.getPetLoot()));
               chr.getMap().broadcastMessage(c.getPlayer(), PetPacket.commandResponse(chr.getId(), (byte)1, (byte)index, true), true);
            } else {
               if (gainCloseness) {
                  newCloseness2 = pet.getCloseness() - 1;
                  if (newCloseness2 < 0) {
                     newCloseness2 = 0;
                  }

                  pet.setCloseness(newCloseness2);
                  if (newCloseness2 < GameConstants.getClosenessNeededForLevel(pet.getLevel())) {
                     pet.setLevel(pet.getLevel() - 1);
                  }
               }

               c.getSession().writeAndFlush(PetPacket.updatePet(chr, pet, chr.getInventory(MapleInventoryType.CASH).getItem(pet.getInventoryPosition()), false, chr.getPetLoot()));
               chr.getMap().broadcastMessage(chr, PetPacket.commandResponse(chr.getId(), (byte)1, (byte)chr.getPetIndex(pet), false), true);
            }

            MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, false);
            return;
         }
      }

   }

   public static final void MovePet(LittleEndianAccessor slea, MapleCharacter chr) {
      int petId = slea.readInt();
      slea.skip(13);
      List<LifeMovementFragment> res = MovementParse.parseMovement(slea, 3);
      if (res != null && chr != null && res.size() != 0 && chr.getMap() != null) {
         MaplePet pet = chr.getPet((long)petId);
         if (pet == null) {
            return;
         }

         pet.updatePosition(res);
         chr.getMap().broadcastMessage(chr, PetPacket.movePet(chr.getId(), pet.getUniqueId(), (byte)petId, res, pet.getPos()), false);
         if (chr.getStat().pickupRange <= 0.0D || chr.inPVP()) {
            return;
         }

         chr.setScrolledPosition((short)0);
      }

   }

   public static void ChangePetBuff(LittleEndianAccessor slea, MapleCharacter chr) {
      int type = slea.readInt();
      int skillsize = slea.readInt();
      int skillId = slea.readInt();
      int mode = slea.readByte();
      MaplePet pet = chr.getPet((long)type);
      if (pet == null) {
         chr.dropMessage(1, "펫이 존재하지 않습니다.");
         chr.getClient().getSession().writeAndFlush(CWvsContext.enableActions(chr));
      } else {
         if (chr.getKeyValue(9999, "skillid") == -1L) {
            chr.setKeyValue(9999, "skillid", "0");
         }

         if (chr.getKeyValue(9999, "skillid2") == -1L) {
            chr.setKeyValue(9999, "skillid2", "0");
         }

         if (chr.getKeyValue(9999, "skillid3") == -1L) {
            chr.setKeyValue(9999, "skillid3", "0");
         }

         if (chr.getKeyValue(9999, "skillid4") == -1L) {
            chr.setKeyValue(9999, "skillid4", "0");
         }

         if (chr.getKeyValue(9999, "skillid5") == -1L) {
            chr.setKeyValue(9999, "skillid5", "0");
         }

         if (chr.getKeyValue(9999, "skillid6") == -1L) {
            chr.setKeyValue(9999, "skillid6", "0");
         }

         if (type == 0 && mode == 0 && skillsize == 0) {
            pet.setBuffSkillId(skillId);
            chr.setKeyValue(9999, "skillid", skillId.makeConcatWithConstants<invokedynamic>(skillId));
         } else if (type == 0 && skillsize == 1) {
            pet.setBuffSkillId2(skillId);
            chr.setKeyValue(9999, "skillid2", skillId.makeConcatWithConstants<invokedynamic>(skillId));
         } else if (type == 1 && skillsize == 0) {
            pet.setBuffSkillId(skillId);
            chr.setKeyValue(9999, "skillid3", skillId.makeConcatWithConstants<invokedynamic>(skillId));
         } else if (type == 1 && skillsize == 1) {
            pet.setBuffSkillId2(skillId);
            chr.setKeyValue(9999, "skillid4", skillId.makeConcatWithConstants<invokedynamic>(skillId));
         } else if (type == 2 && skillsize == 0) {
            pet.setBuffSkillId(skillId);
            chr.setKeyValue(9999, "skillid5", skillId.makeConcatWithConstants<invokedynamic>(skillId));
         } else if (type == 2 && skillsize == 1) {
            pet.setBuffSkillId2(skillId);
            chr.setKeyValue(9999, "skillid6", skillId.makeConcatWithConstants<invokedynamic>(skillId));
         }

         Channel var10000 = chr.getClient().getSession();
         long var10002 = chr.getKeyValue(9999, "skillid");
         var10000.writeAndFlush(CWvsContext.InfoPacket.showPetSkills(0, "0=" + var10002 + ";1=" + chr.getKeyValue(9999, "skillid2")));
         var10000 = chr.getClient().getSession();
         var10002 = chr.getKeyValue(9999, "skillid3");
         var10000.writeAndFlush(CWvsContext.InfoPacket.showPetSkills(1, "10=" + var10002 + ";11=" + chr.getKeyValue(9999, "skillid4")));
         var10000 = chr.getClient().getSession();
         var10002 = chr.getKeyValue(9999, "skillid5");
         var10000.writeAndFlush(CWvsContext.InfoPacket.showPetSkills(2, "20=" + var10002 + ";21=" + chr.getKeyValue(9999, "skillid6")));
         chr.getClient().getSession().writeAndFlush(PetPacket.updatePet(chr, pet, chr.getInventory(MapleInventoryType.CASH).getItem(pet.getInventoryPosition()), false, chr.getPetLoot()));
      }
   }

   public static void petExceptionList(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      int petindex = slea.readInt();
      byte size = slea.readByte();
      MaplePet pet = chr.getPet((long)petindex);
      if (pet == null) {
         chr.dropMessage(1, "펫이 존재하지 않습니다.");
         c.getSession().writeAndFlush(CWvsContext.enableActions(chr));
      } else {
         pet.setExceptionList("");
         String list = "";
         int i = 0;

         while(i < size) {
            list = list + slea.readInt();
            ++i;
            if (size > 1 && size != i) {
               list = list + ",";
            }
         }

         pet.setExceptionList(list);
         c.getSession().writeAndFlush(PetPacket.petExceptionList(chr, pet));
      }
   }

   public static void updatePetSkills(MapleCharacter player, MaplePet unequip) {
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      Map<Skill, SkillEntry> newL = new HashMap();
      List<Integer> petItemIds = new ArrayList();

      int level;
      for(level = 0; level < 3; ++level) {
         if (player.getPet((long)level) != null) {
            petItemIds.add(player.getPet((long)level).getPetItemId());
         }
      }

      Iterator var9;
      Entry skill;
      if (unequip != null) {
         level = 0;
         StructSetItem setItem = ii.getSetItem(ii.getSetItemID(unequip.getPetItemId()));
         Iterator var7 = petItemIds.iterator();

         while(var7.hasNext()) {
            int petId = (Integer)var7.next();
            if (ii.getSetItemID(petId) == ii.getSetItemID(unequip.getPetItemId())) {
               ++level;
            }
         }

         if (setItem != null) {
            var7 = setItem.items.entrySet().iterator();

            label128:
            while(true) {
               while(true) {
                  if (!var7.hasNext()) {
                     break label128;
                  }

                  Entry<Integer, StructSetItem.SetItem> set = (Entry)var7.next();
                  if ((Integer)set.getKey() <= level) {
                     var9 = ((StructSetItem.SetItem)set.getValue()).activeSkills.entrySet().iterator();

                     while(var9.hasNext()) {
                        skill = (Entry)var9.next();
                        newL.put(SkillFactory.getSkill((Integer)skill.getKey()), new SkillEntry((Byte)skill.getValue(), (byte)SkillFactory.getSkill((Integer)skill.getKey()).getMasterLevel(), -1L));
                        switch((Integer)skill.getKey()) {
                        case 80000589:
                        case 80001535:
                        case 80001536:
                        case 80001537:
                        case 80001538:
                        case 80001539:
                           player.changeSkillLevel((Integer)skill.getKey(), (byte)1, (byte)1);
                        }
                     }
                  } else {
                     var9 = ((StructSetItem.SetItem)set.getValue()).activeSkills.entrySet().iterator();

                     while(var9.hasNext()) {
                        skill = (Entry)var9.next();
                        newL.put(SkillFactory.getSkill((Integer)skill.getKey()), new SkillEntry(-1, (byte)0, -1L));
                        switch((Integer)skill.getKey()) {
                        case 80000589:
                        case 80001535:
                        case 80001536:
                        case 80001537:
                        case 80001538:
                        case 80001539:
                           player.changeSkillLevel((Integer)skill.getKey(), (byte)-1, (byte)0);
                        }
                     }
                  }
               }
            }
         }
      } else {
         Iterator var13 = petItemIds.iterator();

         label109:
         while(true) {
            int level2;
            StructSetItem setItem2;
            do {
               if (!var13.hasNext()) {
                  break label109;
               }

               int petId2 = (Integer)var13.next();
               level2 = 0;
               setItem2 = ii.getSetItem(ii.getSetItemID(petId2));
            } while(setItem2 == null);

            var9 = setItem2.itemIDs.iterator();

            while(var9.hasNext()) {
               int setItemId = (Integer)var9.next();
               if (petItemIds.contains(setItemId)) {
                  ++level2;
               }
            }

            var9 = setItem2.items.entrySet().iterator();

            while(true) {
               while(true) {
                  if (!var9.hasNext()) {
                     continue label109;
                  }

                  skill = (Entry)var9.next();
                  Iterator var11;
                  Entry skill2;
                  if ((Integer)skill.getKey() <= level2) {
                     var11 = ((StructSetItem.SetItem)skill.getValue()).activeSkills.entrySet().iterator();

                     while(var11.hasNext()) {
                        skill2 = (Entry)var11.next();
                        newL.put(SkillFactory.getSkill((Integer)skill2.getKey()), new SkillEntry((Byte)skill2.getValue(), (byte)SkillFactory.getSkill((Integer)skill2.getKey()).getMasterLevel(), -1L));
                        switch((Integer)skill2.getKey()) {
                        case 80000589:
                        case 80001535:
                        case 80001536:
                        case 80001537:
                        case 80001538:
                        case 80001539:
                           player.changeSkillLevel((Integer)skill2.getKey(), (byte)1, (byte)1);
                        }
                     }
                  } else {
                     var11 = ((StructSetItem.SetItem)skill.getValue()).activeSkills.entrySet().iterator();

                     while(var11.hasNext()) {
                        skill2 = (Entry)var11.next();
                        newL.put(SkillFactory.getSkill((Integer)skill2.getKey()), new SkillEntry(-1, (byte)0, -1L));
                        switch((Integer)skill2.getKey()) {
                        case 80000589:
                        case 80001535:
                        case 80001536:
                        case 80001537:
                        case 80001538:
                        case 80001539:
                           player.changeSkillLevel((Integer)skill2.getKey(), (byte)-1, (byte)0);
                        }
                     }
                  }
               }
            }
         }
      }

      if (!newL.isEmpty()) {
         player.getClient().getSession().writeAndFlush(CWvsContext.updateSkills(newL));
      }

   }
}
