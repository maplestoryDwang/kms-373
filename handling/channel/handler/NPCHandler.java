package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleQuestStatus;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import constants.ServerConstants;
import handling.SendPacketOpcode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import scripting.NPCConversationManager;
import scripting.NPCScriptManager;
import server.DimentionMirrorEntry;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStorage;
import server.life.MapleLifeFactory;
import server.life.MapleNPC;
import server.quest.MapleQuest;
import server.shops.MapleShop;
import tools.FileoutputUtil;
import tools.HexTool;
import tools.Pair;
import tools.data.LittleEndianAccessor;
import tools.data.MaplePacketLittleEndianWriter;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.SLFCGPacket;

public class NPCHandler {
   public static final void NPCAnimation(LittleEndianAccessor slea, MapleClient c) {
      if (c.getPlayer() != null) {
         if (c.getPlayer().getMapId() != 910143000) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(SendPacketOpcode.NPC_ACTION.getValue());
            int length = (int)slea.available();
            if (length == 10) {
               mplew.writeInt(slea.readInt());
               mplew.write(slea.readByte());
               mplew.write(slea.readByte());
               mplew.writeInt(slea.readInt());
            } else {
               if (length <= 10) {
                  return;
               }

               mplew.write(HexTool.getByteArrayFromHexString(slea.toString()));
            }

            c.getSession().writeAndFlush(mplew.getPacket());
         }
      }
   }

   public static final void NPCShop(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      byte bmode = slea.readByte();
      if (chr != null) {
         String var10000;
         int var10001;
         MapleShop shop;
         short slot;
         int itemId;
         short quantity;
         switch(bmode) {
         case 0:
            shop = chr.getShop();
            if (shop == null) {
               return;
            }

            slot = slea.readShort();
            ++slot;
            itemId = slea.readInt();
            quantity = slea.readShort();
            shop.buy(c, itemId, quantity, slot);
            var10000 = FileoutputUtil.엔피시상점구매로그;
            var10001 = c.getAccID();
            FileoutputUtil.log(var10000, "[상점아이템구매] 계정번호 : " + var10001 + " | " + c.getPlayer().getName() + "(" + c.getPlayer().getId() + ")이 " + c.getPlayer().getMapId() + " 에서 [" + shop.getShopString() + "] 상점 (" + MapleItemInformationProvider.getInstance().getName(itemId) + ")(" + itemId + ")를 " + quantity + "개 구매");
            break;
         case 1:
            shop = chr.getShop();
            if (shop == null) {
               return;
            }

            slot = slea.readShort();
            itemId = slea.readInt();
            quantity = slea.readShort();
            shop.sell(c, GameConstants.getInventoryType(itemId), slot, quantity);
            var10000 = FileoutputUtil.엔피시상점판매로그;
            var10001 = c.getAccID();
            FileoutputUtil.log(var10000, "[상점아이템판매] 계정번호 : " + var10001 + " | " + c.getPlayer().getName() + "(" + c.getPlayer().getId() + ")이 " + c.getPlayer().getMapId() + " 에서 [" + shop.getShopString() + "] 상점 (" + MapleItemInformationProvider.getInstance().getName(itemId) + ")(" + itemId + ")를 " + quantity + "개 판매");
            break;
         case 2:
            shop = chr.getShop();
            if (shop == null) {
               return;
            }

            slot = slea.readShort();
            shop.recharge(c, slot);
            break;
         default:
            chr.setConversation(0);
         }

      }
   }

   public static final void NPCTalk(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      if (chr != null && chr.getMap() != null) {
         MapleNPC npc = chr.getMap().getNPCByOid(slea.readInt());
         if (npc != null) {
            if (npc.hasShop()) {
               chr.setConversation(1);
               npc.sendShop(c);
               String var10000 = FileoutputUtil.엔피시상점로그;
               int var10001 = c.getAccID();
               FileoutputUtil.log(var10000, "[상점오픈] 계정번호 : " + var10001 + " | " + c.getPlayer().getName() + "(" + c.getPlayer().getId() + ")이 " + c.getPlayer().getMapId() + " 에서 상점엔피시 : " + MapleLifeFactory.getNPC(npc.getId()).getName() + "(" + npc.getId() + ")를 오픈");
            } else {
               NPCScriptManager.getInstance().start(c, npc.getId(), (String)null);
               MapleCharacter var4 = c.getPlayer();
               int var10002 = npc.getId();
               var4.dropMessageGM(6, "OpenNPC(" + var10002 + ") : " + npc.getName());
            }

         }
      }
   }

   public static final void QuestAction(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      byte action = slea.readByte();
      int quest = slea.readInt();
      if (chr != null) {
         MapleQuest q = MapleQuest.getInstance(quest);
         int npc;
         switch(action) {
         case 0:
            slea.readInt();
            int itemid = slea.readInt();
            q.RestoreLostItem(chr, itemid);
            break;
         case 1:
            npc = slea.readInt();
            if (!q.hasStartScript()) {
               q.start(chr, npc);
            }
            break;
         case 2:
            npc = slea.readInt();
            slea.readInt();
            if (q.hasEndScript() && (quest < 1115 || quest > 1124)) {
               return;
            }

            if (slea.available() >= 4L) {
               q.complete(chr, npc, slea.readInt(), false);
            } else {
               q.complete(chr, npc);
            }
            break;
         case 3:
            if (GameConstants.canForfeit(q.getId())) {
               q.forfeit(chr);
            } else {
               chr.dropMessage(1, "You may not forfeit this quest.");
            }
            break;
         case 4:
            npc = slea.readInt();
            if (quest >= 37151 && quest <= 37180) {
               NPCScriptManager.getInstance().startQuest(c, npc, quest);
            } else if (quest != 100114 && quest != 100188) {
               NPCScriptManager.getInstance().startQuest(c, npc, quest);
            } else {
               NPCScriptManager.getInstance().startQuest(c, npc, quest);
            }
            break;
         case 5:
            npc = slea.readInt();
            NPCScriptManager.getInstance().endQuest(c, npc, quest, false);
         }

      }
   }

   public static final void Storage(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
      byte mode = slea.readByte();
      if (chr != null) {
         MapleStorage storage = chr.getStorage();
         String var10000;
         int var10001;
         switch(mode) {
         case 3:
            if (c.CheckSecondPassword(slea.readMapleAsciiString())) {
               c.getPlayer().getStorage().sendStorage(c, chr.getStorageNPC());
            } else {
               c.getSession().writeAndFlush(CField.NPCPacket.getStorage((byte)1));
            }
            break;
         case 4:
            byte type = slea.readByte();
            byte slot = storage.getSlot(MapleInventoryType.getByType(type), slea.readByte());
            Item item = storage.takeOut(slot);
            if (item != null) {
               if (c.getPlayer().getInventory(MapleInventoryType.getByType(type)).getNextFreeSlot() <= -1) {
                  storage.store(item);
                  chr.dropMessage(1, "인벤토리의 공간이 부족합니다.");
                  c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               } else {
                  if (ItemFlag.KARMA_EQUIP.check(item.getFlag())) {
                     item.setFlag(item.getFlag() - ItemFlag.KARMA_EQUIP.getValue());
                  }

                  var10000 = FileoutputUtil.창고퇴고로그;
                  var10001 = c.getAccID();
                  FileoutputUtil.log(var10000, "[창고 퇴고] 계정 아이디 : " + var10001 + " | " + chr.getName() + "이 창고에서 " + MapleItemInformationProvider.getInstance().getName(item.getItemId()) + "을 " + item.getQuantity() + "개 퇴고.");
                  MapleInventoryManipulator.addbyItem(c, item, false);
                  storage.sendTakenOut(c, GameConstants.getInventoryType(item.getItemId()));
               }
            } else {
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            }
            break;
         case 5:
            short slot = slea.readShort();
            int itemId = slea.readInt();
            MapleInventoryType type = GameConstants.getInventoryType(itemId);
            short quantity = slea.readShort();
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if (quantity < 1) {
               return;
            }

            if (storage.isFull()) {
               c.getSession().writeAndFlush(CField.NPCPacket.getStorageFull());
               return;
            }

            if (chr.getInventory(type).getItem(slot) == null) {
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               return;
            }

            if (chr.getMeso() < 100L) {
               chr.dropMessage(1, "아이템을 맡기려면 100메소가 필요합니다.");
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               return;
            }

            Item item = chr.getInventory(type).getItem(slot).copy();
            if (GameConstants.isPet(item.getItemId())) {
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               return;
            }

            if (ii.isPickupRestricted(item.getItemId()) && storage.findById(item.getItemId()) != null) {
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               return;
            }

            if (item.getItemId() != itemId || item.getQuantity() < quantity && !GameConstants.isThrowingStar(itemId) && !GameConstants.isBullet(itemId)) {
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
               return;
            }

            if (GameConstants.isThrowingStar(itemId) || GameConstants.isBullet(itemId)) {
               quantity = item.getQuantity();
            }

            MapleInventoryManipulator.removeFromSlot(c, type, slot, quantity, false);
            var10000 = FileoutputUtil.창고입고로그;
            var10001 = c.getAccID();
            FileoutputUtil.log(var10000, "[창고 입고] 계정 아이디 : " + var10001 + " | " + chr.getName() + "이 인벤토리에서 " + ii.getName(item.getItemId()) + "(" + item.getItemId() + ")을 " + item.getQuantity() + "개 입고.");
            chr.gainMeso(-100L, false, false);
            item.setQuantity(quantity);
            storage.store(item);
            storage.sendStored(c, GameConstants.getInventoryType(itemId));
            break;
         case 6:
            storage.arrange();
            storage.update(c);
            break;
         case 7:
            long meso = slea.readLong();
            long storageMesos = storage.getMeso();
            long playerMesos = chr.getMeso();
            if (meso < 0L) {
               if (-meso <= playerMesos) {
                  storage.setMeso(storageMesos - meso);
                  chr.gainMeso(meso, false, false);
               }
            } else if (meso > 0L && meso <= storageMesos) {
               storage.setMeso(storageMesos - meso);
               chr.gainMeso(meso, false, false);
            }

            storage.sendMeso(c);
            break;
         case 8:
            storage.close();
            chr.setConversation(0);
            break;
         default:
            System.out.println("Unhandled Storage mode : " + mode);
         }

      }
   }

   public static final void NPCMoreTalk(LittleEndianAccessor slea, MapleClient c) {
      slea.skip(4);
      byte lastMsg = slea.readByte();
      if (lastMsg == 0) {
         slea.readInt();
         slea.readMapleAsciiString();
      } else if (lastMsg == 22) {
         return;
      }

      if (c.getPlayer() != null) {
         if (c.getPlayer().isWatchingWeb() && lastMsg == 22) {
            c.getPlayer().setWatchingWeb(false);
            c.getSession().writeAndFlush(SLFCGPacket.ChangeVolume(100, 1000));
         }

         byte action;
         if (lastMsg == 10 && slea.available() >= 4L) {
            slea.skip(2);
         } else if (lastMsg == 44) {
            action = slea.readByte();
            if (action == 0) {
               return;
            }
         } else if (lastMsg == 37) {
            NPCConversationManager npccm = NPCScriptManager.getInstance().getCM(c);
            if (npccm != null) {
               if (npccm.getType() != -1 && npccm.getType() != -2) {
                  NPCScriptManager.getInstance().startQuest(c, (byte)1, lastMsg, 0);
               } else {
                  NPCScriptManager.getInstance().action(c, (byte)1, lastMsg, 0);
               }
            }

            return;
         }

         action = slea.readByte();
         NPCConversationManager cm = NPCScriptManager.getInstance().getCM(c);
         if (cm != null) {
            if (c.getPlayer().getConversation() == 0) {
               cm.dispose();
            } else if (lastMsg == 6 && action == 0 && slea.available() < 1L) {
               c.removeClickedNPC();
               NPCScriptManager.getInstance().dispose(c);
               c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            } else {
               cm.setLastMsg((byte)-1);
               if (lastMsg == 4) {
                  if (action != 0) {
                     cm.setGetText(slea.readMapleAsciiString());
                     if (cm.getType() == 0) {
                        NPCScriptManager.getInstance().startQuest(c, action, lastMsg, -1);
                     } else if (cm.getType() == 1) {
                        NPCScriptManager.getInstance().endQuest(c, action, lastMsg, -1);
                     } else {
                        NPCScriptManager.getInstance().action(c, action, lastMsg, -1);
                     }
                  } else {
                     cm.dispose();
                  }
               } else {
                  int selection = -1;
                  int selection2 = -1;
                  if (slea.available() >= 6L && lastMsg == 20 && action == 3) {
                     int unk = slea.readByte();
                     int unk2 = slea.readByte();
                     if (unk == 0 && unk2 == 1) {
                        return;
                     }

                     if (unk == 0 && unk2 == 0) {
                        return;
                     }

                     NPCScriptManager.getInstance().startQuest(c, action, lastMsg, selection);
                     return;
                  }

                  if (slea.available() >= 4L) {
                     selection = slea.readInt();
                  } else if (slea.available() > 0L) {
                     if (GameConstants.isZero(c.getPlayer().getJob()) && lastMsg == 36) {
                        slea.skip(1);
                        if (action != 0) {
                           selection = slea.readByte();
                           selection2 = slea.readByte();
                        }
                     } else {
                        if (lastMsg == 10) {
                           slea.skip(1);
                        }

                        selection = slea.readByte();
                     }
                  }

                  if (lastMsg == 44) {
                     slea.skip(2);
                     int nMixBaseHairColor = slea.readInt();
                     int nMixAddHairColor = slea.readInt();
                     int nMixHairBaseProb = slea.readInt();
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
                     return;
                  }

                  if (lastMsg == 4 && selection == -1 && selection2 == -1) {
                     cm.dispose();
                     return;
                  }

                  if (selection >= -1 && action != -1) {
                     if (cm.getType() == 0) {
                        NPCScriptManager.getInstance().startQuest(c, action, lastMsg, selection);
                     } else if (cm.getType() == 1) {
                        NPCScriptManager.getInstance().endQuest(c, action, lastMsg, selection);
                     } else if (GameConstants.isZero(c.getPlayer().getJob()) && lastMsg == 36) {
                        NPCScriptManager.getInstance().zeroaction(c, action, lastMsg, selection, selection2);
                     } else {
                        NPCScriptManager.getInstance().action(c, action, lastMsg, selection);
                     }
                  } else {
                     cm.dispose();
                  }
               }

            }
         }
      }
   }

   public static final void repairAll(MapleClient c) {
      if (c.getPlayer().getMapId() == 240000000) {
         int price = 0;
         MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
         Map<Equip, Integer> eqs = new HashMap();
         MapleInventoryType[] types = new MapleInventoryType[]{MapleInventoryType.EQUIP, MapleInventoryType.EQUIPPED};
         MapleInventoryType[] var5 = types;
         int var6 = types.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            MapleInventoryType type = var5[var7];
            Iterator var9 = c.getPlayer().getInventory(type).newList().iterator();

            while(var9.hasNext()) {
               Item item = (Item)var9.next();
               if (item instanceof Equip) {
                  Equip eq = (Equip)item;
                  if (eq.getDurability() >= 0) {
                     Map<String, Integer> eqStats = ii.getEquipStats(eq.getItemId());
                     if (eqStats.containsKey("durability") && (Integer)eqStats.get("durability") > 0 && eq.getDurability() < (Integer)eqStats.get("durability")) {
                        double rPercentage = 100.0D - Math.ceil((double)eq.getDurability() * 1000.0D / (double)(Integer)eqStats.get("durability") * 10.0D);
                        eqs.put(eq, (Integer)eqStats.get("durability"));
                        price += (int)Math.ceil(rPercentage * ii.getPrice(eq.getItemId()) / (ii.getReqLevel(eq.getItemId()) < 70 ? 100.0D : 1.0D));
                     }
                  }
               }
            }
         }

         if (eqs.size() > 0 && c.getPlayer().getMeso() >= (long)price) {
            c.getPlayer().gainMeso((long)(-price), true);
            Iterator var15 = eqs.entrySet().iterator();

            while(var15.hasNext()) {
               Entry<Equip, Integer> eqqz = (Entry)var15.next();
               Equip ez = (Equip)eqqz.getKey();
               ez.setDurability((Integer)eqqz.getValue());
               c.getPlayer().forceReAddItem(ez.copy(), ez.getPosition() < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP);
            }

         }
      }
   }

   public static final void repair(LittleEndianAccessor slea, MapleClient c) {
      if (c.getPlayer().getMapId() == 240000000 && slea.available() >= 4L) {
         int position = slea.readInt();
         MapleInventoryType type = position < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP;
         Item item = c.getPlayer().getInventory(type).getItem((short)position);
         if (item != null) {
            Equip eq = (Equip)item;
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            Map<String, Integer> eqStats = ii.getEquipStats(item.getItemId());
            if (eq.getDurability() >= 0 && eqStats.containsKey("durability") && (Integer)eqStats.get("durability") > 0 && eq.getDurability() < (Integer)eqStats.get("durability")) {
               double rPercentage = 100.0D - Math.ceil((double)eq.getDurability() * 1000.0D / (double)(Integer)eqStats.get("durability") * 10.0D);
               int price = (int)Math.ceil(rPercentage * ii.getPrice(eq.getItemId()) / (ii.getReqLevel(eq.getItemId()) < 70 ? 100.0D : 1.0D));
               if (c.getPlayer().getMeso() >= (long)price) {
                  c.getPlayer().gainMeso((long)(-price), false);
                  eq.setDurability((Integer)eqStats.get("durability"));
                  c.getPlayer().forceReAddItem(eq.copy(), type);
               }
            }
         }
      }
   }

   public static final void UpdateQuest(LittleEndianAccessor slea, MapleClient c) {
      MapleQuest quest = MapleQuest.getInstance(slea.readInt());
      if (quest != null) {
         c.getPlayer().updateQuest(c.getPlayer().getQuest(quest), true);
      }

   }

   public static final void UseItemQuest(LittleEndianAccessor slea, MapleClient c) {
      short slot = slea.readShort();
      int itemId = slea.readInt();
      Item item = c.getPlayer().getInventory(MapleInventoryType.ETC).getItem(slot);
      int qid = slea.readInt();
      MapleQuest quest = MapleQuest.getInstance(qid);
      MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
      Pair<Integer, List<Integer>> questItemInfo = null;
      boolean found = false;
      Iterator var10 = c.getPlayer().getInventory(MapleInventoryType.ETC).iterator();

      while(var10.hasNext()) {
         Item i = (Item)var10.next();
         if (i.getItemId() / 10000 == 422) {
            questItemInfo = ii.questItemInfo(i.getItemId());
            if (questItemInfo != null && (Integer)questItemInfo.getLeft() == qid && questItemInfo.getRight() != null && ((List)questItemInfo.getRight()).contains(itemId)) {
               found = true;
               break;
            }
         }
      }

      if (quest != null && found && item != null && item.getQuantity() > 0 && item.getItemId() == itemId) {
         int newData = slea.readInt();
         MapleQuestStatus stats = c.getPlayer().getQuestNoAdd(quest);
         if (stats != null && stats.getStatus() == 1) {
            stats.setCustomData(String.valueOf(newData));
            c.getPlayer().updateQuest(stats, true);
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.ETC, slot, (short)1, false);
         }
      }

   }

   public static void quickMove(LittleEndianAccessor slea, MapleClient c) {
      NPCScriptManager.getInstance().start(c, slea.readInt());
   }

   public static void dimentionMirror(LittleEndianAccessor slea, MapleClient c) {
      slea.skip(4);
      int id = slea.readInt();
      Iterator var3 = ServerConstants.mirrors.iterator();

      while(var3.hasNext()) {
         DimentionMirrorEntry dm = (DimentionMirrorEntry)var3.next();
         if (dm.getId() == id) {
            c.removeClickedNPC();
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().writeAndFlush(CWvsContext.enableActions(c.getPlayer()));
            NPCScriptManager.getInstance().start(c, Integer.parseInt(dm.getScript()), (String)null);
         }
      }

   }
}
