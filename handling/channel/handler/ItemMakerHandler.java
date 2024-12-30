package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleQuestStatus;
import client.MapleTrait;
import client.PlayerStats;
import client.SkillEntry;
import client.SkillFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import scripting.ReactorScriptManager;
import server.ItemMakerFactory;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.SecondaryStatEffect;
import server.maps.MapleExtractor;
import server.maps.MapleReactor;
import server.quest.MapleQuest;
import tools.FileoutputUtil;
import tools.Pair;
import tools.Triple;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class ItemMakerHandler {
   private static final Map<String, Integer> craftingEffects = new HashMap();

   public static final void ItemMaker(LittleEndianAccessor slea, MapleClient c) {
      int makerType = slea.readInt();
      switch(makerType) {
      case 1:
         int toCreate = slea.readInt();
         int enchant;
         ItemMakerFactory.GemCreateEntry gem;
         if (GameConstants.isGem(toCreate)) {
            gem = ItemMakerFactory.getInstance().getGemInfo(toCreate);
            if (gem == null) {
               return;
            }

            if (!hasSkill(c, gem.getReqSkillLevel())) {
               return;
            }

            if (c.getPlayer().getMeso() < (long)gem.getCost()) {
               return;
            }

            enchant = getRandomGem(gem.getRandomReward());
            if (c.getPlayer().getInventory(GameConstants.getInventoryType(enchant)).isFull()) {
               return;
            }

            int taken = checkRequiredNRemove(c, gem.getReqRecipes());
            if (taken == 0) {
               return;
            }

            c.getPlayer().gainMeso((long)(-gem.getCost()), false);
            MapleInventoryManipulator.addById(c, enchant, (short)((byte)(taken == enchant ? 9 : 1)), "Made by Gem " + toCreate + " on " + FileoutputUtil.CurrentReadable_Date());
            c.getSession().writeAndFlush(CField.EffectPacket.showItemMakerEffect(c.getPlayer(), 0, true));
            c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.EffectPacket.showItemMakerEffect(c.getPlayer(), 0, false), false);
         } else if (GameConstants.isOtherGem(toCreate)) {
            gem = ItemMakerFactory.getInstance().getGemInfo(toCreate);
            if (gem == null) {
               return;
            }

            if (!hasSkill(c, gem.getReqSkillLevel())) {
               return;
            }

            if (c.getPlayer().getMeso() < (long)gem.getCost()) {
               return;
            }

            if (c.getPlayer().getInventory(GameConstants.getInventoryType(toCreate)).isFull()) {
               return;
            }

            if (checkRequiredNRemove(c, gem.getReqRecipes()) == 0) {
               return;
            }

            c.getPlayer().gainMeso((long)(-gem.getCost()), false);
            if (GameConstants.getInventoryType(toCreate) == MapleInventoryType.EQUIP) {
               MapleInventoryManipulator.addbyItem(c, MapleItemInformationProvider.getInstance().getEquipById(toCreate));
            } else {
               MapleInventoryManipulator.addById(c, toCreate, (short)1, "Made by Gem " + toCreate + " on " + FileoutputUtil.CurrentReadable_Date());
            }

            c.getSession().writeAndFlush(CField.EffectPacket.showItemMakerEffect(c.getPlayer(), 0, true));
            c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.EffectPacket.showItemMakerEffect(c.getPlayer(), 0, false), false);
         } else {
            boolean stimulator = slea.readByte() > 0;
            int numEnchanter = slea.readInt();
            ItemMakerFactory.ItemMakerCreateEntry create = ItemMakerFactory.getInstance().getCreateInfo(toCreate);
            if (create == null) {
               return;
            }

            if (numEnchanter > create.getTUC()) {
               return;
            }

            if (!hasSkill(c, create.getReqSkillLevel())) {
               return;
            }

            if (c.getPlayer().getMeso() < (long)create.getCost()) {
               return;
            }

            if (c.getPlayer().getInventory(GameConstants.getInventoryType(toCreate)).isFull()) {
               return;
            }

            if (checkRequiredNRemove(c, create.getReqItems()) == 0) {
               return;
            }

            c.getPlayer().gainMeso((long)(-create.getCost()), false);
            MapleItemInformationProvider mapleItemInformationProvider1 = MapleItemInformationProvider.getInstance();
            Equip toGive = (Equip)mapleItemInformationProvider1.getEquipById(toCreate);
            if (stimulator || numEnchanter > 0) {
               if (c.getPlayer().haveItem(create.getStimulator(), 1, false, true)) {
                  MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, create.getStimulator(), 1, false, false);
               }

               for(int i = 0; i < numEnchanter; ++i) {
                  enchant = slea.readInt();
                  if (c.getPlayer().haveItem(enchant, 1, false, true)) {
                     Map<String, Integer> stats = mapleItemInformationProvider1.getEquipStats(enchant);
                     if (stats != null) {
                        addEnchantStats(stats, toGive);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, enchant, 1, false, false);
                     }
                  }
               }
            }

            if (stimulator && Randomizer.nextInt(10) == 0) {
               c.getPlayer().dropMessage(5, "The item was overwhelmed by the stimulator.");
            } else {
               MapleInventoryManipulator.addbyItem(c, toGive);
               c.getSession().writeAndFlush(CField.EffectPacket.showItemMakerEffect(c.getPlayer(), 0, true));
               c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.EffectPacket.showItemMakerEffect(c.getPlayer(), 0, false), false);
            }
         }
      case 2:
      default:
         break;
      case 3:
         int etc = slea.readInt();
         if (c.getPlayer().haveItem(etc, 100, false, true)) {
            MapleInventoryManipulator.addById(c, getCreateCrystal(etc), (short)1, "Made by Maker " + etc + " on " + FileoutputUtil.CurrentReadable_Date());
            MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, etc, 100, false, false);
            c.getSession().writeAndFlush(CField.EffectPacket.showItemMakerEffect(c.getPlayer(), 0, true));
            c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.EffectPacket.showItemMakerEffect(c.getPlayer(), 0, false), false);
         }
         break;
      case 4:
         int itemId = slea.readInt();
         slea.readInt();
         byte slot = (byte)slea.readInt();
         Item toUse = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)slot);
         if (toUse == null || toUse.getItemId() != itemId || toUse.getQuantity() < 1) {
            return;
         }

         MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
         if (!ii.isDropRestricted(itemId) && !ii.isAccountShared(itemId)) {
            int[] arrayOfInt = getCrystal(itemId, ii.getReqLevel(itemId));
            MapleInventoryManipulator.addById(c, arrayOfInt[0], (short)((byte)arrayOfInt[1]), "Made by disassemble " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.EQUIP, (short)slot, (short)1, false);
         }

         c.getSession().writeAndFlush(CField.EffectPacket.showItemMakerEffect(c.getPlayer(), 0, true));
         c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.EffectPacket.showItemMakerEffect(c.getPlayer(), 0, false), false);
      }

   }

   private static final int getCreateCrystal(int etc) {
      short level = MapleItemInformationProvider.getInstance().getItemMakeLevel(etc);
      int itemid;
      if (level >= 31 && level <= 50) {
         itemid = 4260000;
      } else if (level >= 51 && level <= 60) {
         itemid = 4260001;
      } else if (level >= 61 && level <= 70) {
         itemid = 4260002;
      } else if (level >= 71 && level <= 80) {
         itemid = 4260003;
      } else if (level >= 81 && level <= 90) {
         itemid = 4260004;
      } else if (level >= 91 && level <= 100) {
         itemid = 4260005;
      } else if (level >= 101 && level <= 110) {
         itemid = 4260006;
      } else if (level >= 111 && level <= 120) {
         itemid = 4260007;
      } else {
         if (level < 121) {
            throw new RuntimeException("Invalid Item Maker id");
         }

         itemid = 4260008;
      }

      return itemid;
   }

   private static final int[] getCrystal(int itemid, int level) {
      int[] all = new int[]{-1, 0};
      if (level >= 31 && level <= 50) {
         all[0] = 4260000;
      } else if (level >= 51 && level <= 60) {
         all[0] = 4260001;
      } else if (level >= 61 && level <= 70) {
         all[0] = 4260002;
      } else if (level >= 71 && level <= 80) {
         all[0] = 4260003;
      } else if (level >= 81 && level <= 90) {
         all[0] = 4260004;
      } else if (level >= 91 && level <= 100) {
         all[0] = 4260005;
      } else if (level >= 101 && level <= 110) {
         all[0] = 4260006;
      } else if (level >= 111 && level <= 120) {
         all[0] = 4260007;
      } else {
         if (level < 121 || level > 200) {
            throw new RuntimeException("Invalid Item Maker type" + level);
         }

         all[0] = 4260008;
      }

      if (!GameConstants.isWeapon(itemid) && !GameConstants.isOverall(itemid)) {
         all[1] = Randomizer.rand(3, 7);
      } else {
         all[1] = Randomizer.rand(5, 11);
      }

      return all;
   }

   private static final void addEnchantStats(Map<String, Integer> stats, Equip item) {
      Integer s = (Integer)stats.get("PAD");
      if (s != null && s != 0) {
         item.setWatk((short)(item.getWatk() + s));
      }

      s = (Integer)stats.get("MAD");
      if (s != null && s != 0) {
         item.setMatk((short)(item.getMatk() + s));
      }

      s = (Integer)stats.get("ACC");
      if (s != null && s != 0) {
         item.setAcc((short)(item.getAcc() + s));
      }

      s = (Integer)stats.get("EVA");
      if (s != null && s != 0) {
         item.setAvoid((short)(item.getAvoid() + s));
      }

      s = (Integer)stats.get("Speed");
      if (s != null && s != 0) {
         item.setSpeed((short)(item.getSpeed() + s));
      }

      s = (Integer)stats.get("Jump");
      if (s != null && s != 0) {
         item.setJump((short)(item.getJump() + s));
      }

      s = (Integer)stats.get("MaxHP");
      if (s != null && s != 0) {
         item.setHp((short)(item.getHp() + s));
      }

      s = (Integer)stats.get("MaxMP");
      if (s != null && s != 0) {
         item.setMp((short)(item.getMp() + s));
      }

      s = (Integer)stats.get("STR");
      if (s != null && s != 0) {
         item.setStr((short)(item.getStr() + s));
      }

      s = (Integer)stats.get("DEX");
      if (s != null && s != 0) {
         item.setDex((short)(item.getDex() + s));
      }

      s = (Integer)stats.get("INT");
      if (s != null && s != 0) {
         item.setInt((short)(item.getInt() + s));
      }

      s = (Integer)stats.get("LUK");
      if (s != null && s != 0) {
         item.setLuk((short)(item.getLuk() + s));
      }

      s = (Integer)stats.get("randOption");
      short str;
      short dex;
      if (s != null && s != 0) {
         str = item.getMatk();
         dex = item.getWatk();
         if (dex > 0) {
            item.setWatk((short)(Randomizer.nextBoolean() ? dex + s : dex - s));
         }

         if (str > 0) {
            item.setMatk((short)(Randomizer.nextBoolean() ? str + s : str - s));
         }
      }

      s = (Integer)stats.get("randStat");
      if (s != null && s != 0) {
         str = item.getStr();
         dex = item.getDex();
         int luk = item.getLuk();
         int int_ = item.getInt();
         if (str > 0) {
            item.setStr((short)(Randomizer.nextBoolean() ? str + s : str - s));
         }

         if (dex > 0) {
            item.setDex((short)(Randomizer.nextBoolean() ? dex + s : dex - s));
         }

         if (int_ > 0) {
            item.setInt((short)(Randomizer.nextBoolean() ? int_ + s : int_ - s));
         }

         if (luk > 0) {
            item.setLuk((short)(Randomizer.nextBoolean() ? luk + s : luk - s));
         }
      }

   }

   private static final int getRandomGem(List<Pair<Integer, Integer>> rewards) {
      List<Integer> items = new ArrayList();
      Iterator var2 = rewards.iterator();

      while(var2.hasNext()) {
         Pair<Integer, Integer> p = (Pair)var2.next();
         int itemid = (Integer)p.getLeft();

         for(int i = 0; i < (Integer)p.getRight(); ++i) {
            items.add(itemid);
         }
      }

      return (Integer)items.get(Randomizer.nextInt(items.size()));
   }

   private static final int checkRequiredNRemove(MapleClient c, List<Pair<Integer, Integer>> recipe) {
      int itemid = 0;
      Iterator var3 = recipe.iterator();

      Pair p;
      while(var3.hasNext()) {
         p = (Pair)var3.next();
         if (!c.getPlayer().haveItem((Integer)p.getLeft(), (Integer)p.getRight(), false, true)) {
            return 0;
         }
      }

      var3 = recipe.iterator();

      while(var3.hasNext()) {
         p = (Pair)var3.next();
         itemid = (Integer)p.getLeft();
         MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType(itemid), itemid, (Integer)p.getRight(), false, false);
      }

      return itemid;
   }

   private static final boolean hasSkill(MapleClient c, int reqlvl) {
      c.getPlayer().getStat();
      return c.getPlayer().getSkillLevel(SkillFactory.getSkill(PlayerStats.getSkillByJob(1007, c.getPlayer().getJob()))) >= reqlvl;
   }

   public static final void UseRecipe(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      if (chr != null && chr.isAlive() && chr.getMap() != null) {
         slea.readInt();
         byte slot = (byte)slea.readShort();
         int itemId = slea.readInt();
         Item toUse = chr.getInventory(MapleInventoryType.USE).getItem((short)slot);
         if (toUse != null && toUse.getQuantity() >= 1 && toUse.getItemId() == itemId && itemId / 10000 == 251) {
            if (MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId()).applyTo(chr)) {
               MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short)slot, (short)1, false);
            }

         } else {
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
         }
      } else {
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      }
   }

   public static final void MakeExtractor(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      if (chr != null && chr.isAlive() && chr.getMap() != null) {
         int itemId = slea.readInt();
         int fee = slea.readInt();
         Item toUse = chr.getInventory(MapleInventoryType.SETUP).findById(itemId);
         chr.setExtractor(new MapleExtractor(chr, itemId, fee, chr.getFH()));
         chr.getMap().spawnExtractor(chr.getExtractor());
      } else {
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      }
   }

   public static final void UseBag(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      if (chr != null && chr.isAlive() && chr.getMap() != null) {
         slea.readInt();
         byte slot = (byte)slea.readShort();
         int itemId = slea.readInt();
         byte inv = slea.readByte();
         Item toUse = chr.getInventory(MapleInventoryType.getByType(inv)).getItem((short)slot);
         if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId || itemId / 10000 != 433 && itemId / 10000 != 265 && itemId / 10000 != 308) {
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
         } else {
            boolean firstTime = !chr.getExtendedSlots().contains(itemId);
            c.getSession().writeAndFlush(CField.openBag(chr.getExtendedSlots().indexOf(itemId), itemId, firstTime));
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
         }
      } else {
         c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
      }
   }

   public static final void CheckHarvest(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      MapleReactor reactor = c.getPlayer().getMap().getReactorByOid(slea.readInt());
      c.getSession().writeAndFlush(CField.harvestMessage(reactor.getObjectId(), 14));
   }

   public static final void StartHarvest(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      MapleReactor reactor = c.getPlayer().getMap().getReactorByOid(slea.readInt());
      if (reactor != null && reactor.isAlive() && reactor.getReactorId() <= 200015 && chr.getStat().harvestingTool > 0 && !(reactor.getTruePosition().distanceSq(chr.getTruePosition()) > 10000.0D) && c.getPlayer().getFatigue() < 100) {
         Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)c.getPlayer().getStat().harvestingTool);
         if (item != null && ((Equip)item).getDurability() != 0) {
            MapleQuestStatus marr = c.getPlayer().getQuestNAdd(MapleQuest.getInstance(122501));
            if (marr.getCustomData() == null) {
               marr.setCustomData("0");
            }

            long lastTime = Long.parseLong(marr.getCustomData());
            if (lastTime + 5000L > System.currentTimeMillis()) {
               c.getPlayer().dropMessage(5, "아직 채집을 할 수 없습니다.");
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            } else {
               marr.setCustomData(String.valueOf(System.currentTimeMillis()));
               c.getSession().writeAndFlush(CField.harvestMessage(reactor.getObjectId(), 14));
            }
         } else {
            c.getPlayer().getStat().handleProfessionTool(c.getPlayer());
         }
      } else {
         c.getPlayer().dropMessage(1, "아직 채집을 할 수 없습니다.");
      }
   }

   public static final void StopHarvest(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      MapleReactor reactor = c.getPlayer().getMap().getReactorByOid(slea.readInt());
      Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)c.getPlayer().getStat().harvestingTool);
      if (item != null && ((Equip)item).getDurability() != 0) {
         c.getPlayer().getMap().destroyReactor(reactor.getObjectId());
         ReactorScriptManager.getInstance().act(c, reactor);
      } else {
         c.getPlayer().getStat().handleProfessionTool(c.getPlayer());
      }
   }

   public static void getSpecialStat(LittleEndianAccessor slea, MapleClient c) {
      try {
         String skillid = slea.readMapleAsciiString();
         int level1 = slea.readInt();
         int level2 = slea.readInt();
         int rate;
         if (!skillid.startsWith("9200") && !skillid.startsWith("9201")) {
            if (skillid.equals("honorLeveling")) {
               c.getSession().writeAndFlush(CWvsContext.updateSpecialStat(skillid, level1, level2, c.getPlayer().getHonourNextExp()));
               return;
            }

            rate = Math.max(0, 100 - (level1 + 1 - c.getPlayer().getProfessionLevel(Integer.parseInt(skillid))) * 20);
         } else {
            rate = 100;
         }

         c.getSession().writeAndFlush(CWvsContext.updateSpecialStat(skillid, level1, level2, rate));
      } catch (NumberFormatException var6) {
      }

   }

   public static final void CraftEffect(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      if (chr.getMapId() == 910001000 || chr.getMap().getExtractorSize() > 0) {
         String effect = slea.readMapleAsciiString();
         Integer profession = (Integer)craftingEffects.get(effect);
         if (profession != null && (c.getPlayer().getProfessionLevel(profession) > 0 || profession == 92040000 && chr.getMap().getExtractorSize() > 0)) {
            int time = slea.readInt();
            if (time > 6000 || time < 3000) {
               time = 4000;
            }

            c.getSession().writeAndFlush(CField.EffectPacket.showEffect(chr, time, 0, 38, effect.endsWith("Extract") ? 1 : 0, 0, (byte)0, true, (Point)null, effect, (Item)null));
            chr.getMap().broadcastMessage(chr, CField.EffectPacket.showEffect(chr, time, 0, 38, effect.endsWith("Extract") ? 1 : 0, 0, (byte)0, false, (Point)null, effect, (Item)null), false);
         }

      }
   }

   public static final void CraftMake(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      if (chr.getMapId() == 910001000 || chr.getMap().getExtractorSize() > 0) {
         int something = slea.readInt();
         int time = slea.readInt();
         if (time > 6000 || time < 3000) {
            time = 4000;
         }

         chr.getMap().broadcastMessage(CField.craftMake(chr.getId(), something, time));
      }
   }

   public static final void CraftComplete(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      int type = slea.readInt();
      int toGet = 0;
      int expGain = false;
      int fatigue = 0;
      int craftID = 0;
      int itemsize = 0;
      int extractorId = 0;
      int theLevl = 0;
      short quantity = true;
      SkillFactory.CraftingEntry ce = null;
      if (type == 1) {
         extractorId = slea.readInt();
         itemsize = slea.readInt();
      } else {
         craftID = slea.readInt();
         ce = SkillFactory.getCraft(craftID);
         if (chr.getMapId() != 910001000 && (craftID != 92028000 || craftID != 92028001 || craftID != 92049000 || chr.getMap().getExtractorSize() <= 0) || ce == null || chr.getFatigue() >= 200) {
            return;
         }

         System.out.println(craftID / 10000 * 10000);
         theLevl = c.getPlayer().getProfessionLevel(craftID / 10000 * 10000);
         if (theLevl < 0 && craftID != 92049000 && craftID != 92028001 && craftID != 92028000) {
            return;
         }
      }

      ItemMakerHandler.CraftRanking cr = ItemMakerHandler.CraftRanking.GOOD;
      Triple triple;
      int toGet;
      short quantity;
      boolean passed;
      boolean down;
      if (type == 1) {
         List<Pair<Integer, Short>> itemlist = new ArrayList();
         passed = false;
         down = false;
         triple = null;

         for(int abc = 0; abc < itemsize; ++abc) {
            craftID = slea.readInt();
            int itemId = slea.readInt();
            long invId = slea.readLong();
            int reqLevel = ii.getReqLevel(itemId);
            Item item = chr.getInventory(MapleInventoryType.EQUIP).findByInventoryId(invId, itemId);
            int invtype = slea.readInt();
            int position = slea.readInt();
            if (item == null) {
               item = c.getPlayer().getInventory(MapleInventoryType.getByType((byte)invtype)).getItem((short)position);
            }

            if (chr.getInventory(MapleInventoryType.ETC).isFull()) {
               return;
            }

            if (extractorId > 0) {
               MapleCharacter extract = chr.getMap().getCharacterById(extractorId);
               if (extract == null || extract.getExtractor() == null) {
                  return;
               }

               MapleExtractor extractor = extract.getExtractor();
               if (extractor.owner != chr.getId()) {
                  if (chr.getMeso() < (long)extractor.fee) {
                     return;
                  }

                  SecondaryStatEffect eff = ii.getItemEffect(extractor.itemId);
                  if (eff != null && eff.getUseLevel() < reqLevel) {
                     return;
                  }

                  chr.gainMeso((long)(-extractor.fee), true);
                  MapleCharacter owner = chr.getMap().getCharacterById(extractor.owner);
                  if (owner != null && owner.getMeso() < (long)(Integer.MAX_VALUE - extractor.fee)) {
                     owner.gainMeso((long)extractor.fee, false);
                  }
               }
            }

            toGet = 4031016;
            quantity = (short)Randomizer.rand(3, !GameConstants.isWeapon(itemId) && !GameConstants.isOverall(itemId) ? 7 : 11);
            if (reqLevel <= 60) {
               toGet = 4021013;
            } else if (reqLevel <= 90) {
               toGet = 4021014;
            } else if (reqLevel <= 120) {
               toGet = 4021015;
            }

            cr = ItemMakerHandler.CraftRanking.COOL;
            itemlist.add(new Pair(toGet, quantity));
            MapleInventoryManipulator.addById(c, toGet, quantity, "Made by disassemble " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.EQUIP, item.getPosition(), (short)1, false);
         }

         c.send(CField.craftFinished2(chr.getId(), craftID, cr.i, itemlist));
      } else {
         int var10000;
         boolean bool;
         if (craftID == 92028001) {
            int itemId = slea.readInt();
            long invId1 = slea.readLong();
            long invId2 = slea.readLong();
            ii.getReqLevel(itemId);
            Equip item1 = (Equip)chr.getInventory(MapleInventoryType.EQUIP).findByInventoryIdOnly(invId1, itemId);
            Equip item2 = (Equip)chr.getInventory(MapleInventoryType.EQUIP).findByInventoryIdOnly(invId2, itemId);

            for(short s = 0; s < chr.getInventory(MapleInventoryType.EQUIP).getSlotLimit(); ++s) {
               Item item = chr.getInventory(MapleInventoryType.EQUIP).getItem(s);
               if (item != null && item.getItemId() == itemId && item != item1 && item != item2) {
                  if (item1 == null) {
                     item1 = (Equip)item;
                  } else if (item2 == null) {
                     item2 = (Equip)item;
                     break;
                  }
               }
            }

            if (item1 == null || item2 == null) {
               return;
            }

            int potentialState = true;
            int potentialChance = theLevl * 2;
            if (item1.getState() > 0 && item2.getState() > 0) {
               potentialChance = 100;
            } else if (item1.getState() > 0 || item2.getState() > 0) {
               potentialChance *= 2;
            }

            if (item1.getState() == item2.getState() && item1.getState() > 17) {
               byte var52 = item1.getState();
            }

            Equip newEquip = (Equip)ii.getEquipById(itemId);
            ii.getTotalStat(newEquip);
            cr = ItemMakerHandler.CraftRanking.COOL;
            if (Randomizer.nextInt(100) < (newEquip.getUpgradeSlots() <= 0 && potentialChance < 100 ? potentialChance / 2 : potentialChance)) {
            }

            toGet = newEquip.getItemId();
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.EQUIP, item1.getPosition(), (short)1, false);
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.EQUIP, item2.getPosition(), (short)1, false);
            MapleInventoryManipulator.addbyItem(c, newEquip);
         } else {
            if (ce.needOpenItem && chr.getSkillLevel(craftID) <= 0) {
               return;
            }

            Iterator var15 = ce.reqItems.entrySet().iterator();

            Entry e;
            while(var15.hasNext()) {
               e = (Entry)var15.next();
               if (!chr.haveItem((Integer)e.getKey(), (Integer)e.getValue())) {
                  return;
               }
            }

            var15 = ce.targetItems.iterator();

            while(var15.hasNext()) {
               Triple<Integer, Integer, Integer> triple = (Triple)var15.next();
               if (!MapleInventoryManipulator.checkSpace(c, (Integer)triple.left, (Integer)triple.mid, "")) {
                  return;
               }
            }

            var15 = ce.reqItems.entrySet().iterator();

            while(var15.hasNext()) {
               e = (Entry)var15.next();
               MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType((Integer)e.getKey()), (Integer)e.getKey(), (Integer)e.getValue(), false, false);
            }

            if (Randomizer.nextInt(100) < 100 - (ce.reqSkillLevel - theLevl) * 20 || craftID / 10000 <= 9201) {
               HashMap sa = new HashMap();

               while(true) {
                  passed = false;
                  Iterator var17 = ce.targetItems.iterator();

                  while(var17.hasNext()) {
                     triple = (Triple)var17.next();
                     if (Randomizer.nextInt(100) < (Integer)triple.right) {
                        toGet = (Integer)triple.left;
                        quantity = ((Integer)triple.mid).shortValue();
                        Item receive = null;
                        cr = ItemMakerHandler.CraftRanking.COOL;
                        if (GameConstants.getInventoryType(toGet) == MapleInventoryType.EQUIP) {
                           Equip first = (Equip)ii.getEquipById(toGet);
                           if (Randomizer.nextInt(100) < theLevl * 2) {
                              first = ii.randomizeStats(first);
                              cr = ItemMakerHandler.CraftRanking.COOL;
                           }

                           if (Randomizer.nextInt(100) < theLevl * (first.getUpgradeSlots() > 0 ? 2 : 1)) {
                              first.resetPotential();
                              cr = ItemMakerHandler.CraftRanking.COOL;
                           }

                           receive = first;
                           first.setFlag((short)ItemFlag.CRAFTED.getValue());
                        } else {
                           receive = new Item(toGet, (short)0, quantity, (short)ItemFlag.KARMA_EQUIP.getValue());
                        }

                        if (ce.period > 0) {
                           ((Item)receive).setExpiration(System.currentTimeMillis() + (long)(ce.period * '\uea60'));
                        }

                        ((Item)receive).setOwner(chr.getName());
                        ((Item)receive).setGMLog("Crafted from " + craftID + " on " + FileoutputUtil.CurrentReadable_Date());
                        MapleInventoryManipulator.addFromDrop(c, (Item)receive, true, false, false);
                        if (ce.needOpenItem) {
                           byte mLevel = chr.getMasterLevel(craftID);
                           if (mLevel == 1) {
                              sa.put(ce, new SkillEntry(0, (byte)0, SkillFactory.getDefaultSExpiry(ce)));
                           } else if (mLevel > 1) {
                              sa.put(ce, new SkillEntry(Integer.MAX_VALUE, (byte)(chr.getMasterLevel(craftID) - 1), SkillFactory.getDefaultSExpiry(ce)));
                           }
                        }

                        int fatigue = ce.incFatigability;
                        if (ce.incSkillProficiency == 0) {
                           var10000 = (fatigue * 20 - (ce.reqSkillLevel - theLevl) * 2) * 1;
                        } else {
                           byte var56 = ce.incSkillProficiency;
                        }

                        chr.getTrait(MapleTrait.MapleTraitType.craft).addExp(cr.craft, chr);
                        passed = true;
                        break;
                     }
                  }

                  if (passed) {
                     cr = ItemMakerHandler.CraftRanking.COOL;
                     chr.changeSkillsLevel(sa);
                     down = false;
                     var10000 = ce.reqSkillLevel - theLevl;
                  }
               }
            }

            short quantity = 0;
            cr = ItemMakerHandler.CraftRanking.FAIL;
            bool = false;
            var10000 = ce.reqSkillLevel - theLevl;
            String s = "연금술";
            switch(craftID / 10000) {
            case 9200:
               s = "채집";
               break;
            case 9201:
               s = "채광";
               break;
            case 9202:
               s = "장비 제작";
               break;
            case 9203:
               s = "장신구";
            }

            int expGain = 30000;
            chr.dropMessage(-5, s + "의 숙련도가 증가 하였습니다. (+" + expGain + ")");
            if (chr.addProfessionExp(craftID / 10000 * 10000, expGain)) {
               chr.dropMessage(-5, s + "의 레벨이 증가 하였습니다.");
            }

            MapleQuest.getInstance(2550).forceStart(c.getPlayer(), 9031000, "1");
            chr.setFatigue(chr.getFatigue() + fatigue);
            chr.getMap().broadcastMessage(CField.craftFinished(chr.getId(), craftID, cr.i, toGet, quantity, expGain));
         }

         bool = false;
         var10000 = ce.reqSkillLevel - theLevl;
      }
   }

   static {
      craftingEffects.put("Effect/BasicEff.img/professions/herbalism", 92000000);
      craftingEffects.put("Effect/BasicEff.img/professions/mining", 92010000);
      craftingEffects.put("Effect/BasicEff.img/professions/herbalismExtract", 92000000);
      craftingEffects.put("Effect/BasicEff.img/professions/miningExtract", 92010000);
      craftingEffects.put("Effect/BasicEff.img/professions/equip_product", 92020000);
      craftingEffects.put("Effect/BasicEff.img/professions/acc_product", 92030000);
      craftingEffects.put("Effect/BasicEff.img/professions/alchemy", 92040000);
   }

   public static enum CraftRanking {
      FAIL(21, 0),
      SOSO(25, 30),
      GOOD(26, 40),
      COOL(27, 50);

      public int i;
      public int craft;

      private CraftRanking(int i, int craft) {
         this.i = i;
         this.craft = craft;
      }
   }
}
