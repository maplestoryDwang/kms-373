package server.quest;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleQuestStatus;
import client.MapleStat;
import client.MapleTrait;
import client.Skill;
import client.SkillEntry;
import client.SkillFactory;
import client.inventory.InventoryException;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.GameConstants;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.RandomRewards;
import server.Randomizer;
import tools.FileoutputUtil;
import tools.Pair;
import tools.Triple;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.SLFCGPacket;

public class MapleQuestAction implements Serializable {
   private static final long serialVersionUID = 9179541993413738569L;
   private MapleQuestActionType type;
   private MapleQuest quest;
   private int intStore = 0;
   private List<Integer> applicableJobs = new ArrayList();
   private List<MapleQuestAction.QuestItem> items = null;
   private List<Triple<Integer, Integer, Integer>> skill = null;
   private List<Pair<Integer, Integer>> state = null;

   public MapleQuestAction(MapleQuestActionType type, ResultSet rse, MapleQuest quest, PreparedStatement pss, PreparedStatement psq, PreparedStatement psi) throws SQLException {
      this.type = type;
      this.quest = quest;
      this.intStore = rse.getInt("intStore");
      String[] jobs = rse.getString("applicableJobs").split(", ");
      if (jobs.length <= 0 && rse.getString("applicableJobs").length() > 0) {
         this.applicableJobs.add(Integer.parseInt(rse.getString("applicableJobs")));
      }

      String[] var9 = jobs;
      int var10 = jobs.length;

      for(int var11 = 0; var11 < var10; ++var11) {
         String j = var9[var11];
         if (j.length() > 0) {
            this.applicableJobs.add(Integer.parseInt(j));
         }
      }

      ResultSet rs;
      switch(type) {
      case item:
         this.items = new ArrayList();
         psi.setInt(1, rse.getInt("uniqueid"));
         rs = psi.executeQuery();

         while(rs.next()) {
            this.items.add(new MapleQuestAction.QuestItem(rs.getInt("itemid"), rs.getInt("count"), rs.getInt("period"), rs.getInt("gender"), rs.getInt("job"), rs.getInt("jobEx"), rs.getInt("prop")));
         }

         rs.close();
         break;
      case quest:
         this.state = new ArrayList();
         psq.setInt(1, rse.getInt("uniqueid"));
         rs = psq.executeQuery();

         while(rs.next()) {
            this.state.add(new Pair(rs.getInt("quest"), rs.getInt("state")));
         }

         rs.close();
         break;
      case skill:
         this.skill = new ArrayList();
         pss.setInt(1, rse.getInt("uniqueid"));
         rs = pss.executeQuery();

         while(rs.next()) {
            this.skill.add(new Triple(rs.getInt("skillid"), rs.getInt("skillLevel"), rs.getInt("masterLevel")));
         }

         rs.close();
      }

   }

   private static boolean canGetItem(MapleQuestAction.QuestItem item, MapleCharacter c) {
      if (item.gender != 2 && item.gender >= 0 && item.gender != c.getGender()) {
         return false;
      } else if (item.job <= 0) {
         return true;
      } else {
         List<Integer> code = getJobBy5ByteEncoding(item.job);
         boolean jobFound = false;
         Iterator iterator = code.iterator();

         while(iterator.hasNext()) {
            int codec = (Integer)iterator.next();
            if (codec / 100 == c.getJob() / 100) {
               jobFound = true;
               break;
            }
         }

         if (!jobFound && item.jobEx > 0) {
            List<Integer> codeEx = getJobBySimpleEncoding(item.jobEx);
            Iterator iterator1 = codeEx.iterator();

            while(iterator1.hasNext()) {
               int codec = (Integer)iterator1.next();
               if (codec / 100 % 10 == c.getJob() / 100 % 10) {
                  jobFound = true;
                  break;
               }
            }
         }

         return jobFound;
      }
   }

   public final boolean RestoreLostItem(MapleCharacter c, int itemid) {
      if (this.type == MapleQuestActionType.item) {
         Iterator var3 = this.items.iterator();

         while(var3.hasNext()) {
            MapleQuestAction.QuestItem item = (MapleQuestAction.QuestItem)var3.next();
            if (item.itemid == itemid) {
               if (!c.haveItem(item.itemid, item.count, true, false)) {
                  MapleClient var10000 = c.getClient();
                  int var10001 = item.itemid;
                  short var10002 = (short)item.count;
                  int var10003 = this.quest.getId();
                  MapleInventoryManipulator.addById(var10000, var10001, var10002, "Obtained from quest (Restored) " + var10003 + " on " + FileoutputUtil.CurrentReadable_Date());
               }

               return true;
            }
         }
      }

      return false;
   }

   public void runStart(MapleCharacter c, Integer extSelection) {
      MapleQuestStatus status;
      int id;
      int period;
      Iterator var21;
      switch(this.type) {
      case item:
         Map<Integer, Integer> props = new HashMap();
         var21 = this.items.iterator();

         while(true) {
            MapleQuestAction.QuestItem item;
            do {
               do {
                  if (!var21.hasNext()) {
                     int selection = 0;
                     int extNum = 0;
                     if (props.size() > 0) {
                        selection = (Integer)props.get(Randomizer.nextInt(props.size()));
                     }

                     var21 = this.items.iterator();

                     while(true) {
                        while(true) {
                           do {
                              if (!var21.hasNext()) {
                                 return;
                              }

                              item = (MapleQuestAction.QuestItem)var21.next();
                           } while(!canGetItem(item, c));

                           id = item.itemid;
                           if (item.prop == -2) {
                              break;
                           }

                           if (item.prop == -1) {
                              if (extSelection == null || extSelection == extNum++) {
                                 break;
                              }
                           } else {
                              if (id != selection) {
                                 continue;
                              }
                              break;
                           }
                        }

                        short count = (short)item.count;
                        if (count < 0) {
                           try {
                              MapleInventoryManipulator.removeById(c.getClient(), GameConstants.getInventoryType(id), id, count * -1, true, false);
                           } catch (InventoryException var20) {
                              System.err.println("[h4x] Completing a quest without meeting the requirements" + var20);
                           }

                           c.getClient().getSession().writeAndFlush(CField.EffectPacket.showCharmEffect(c, id, 1, true, ""));
                        } else {
                           period = item.period / 1440;
                           String name = MapleItemInformationProvider.getInstance().getName(id);
                           if (id / 10000 == 114 && name != null && name.length() > 0) {
                              String msg = "[" + name + "] 훈장을 얻었습니다!!";
                              c.getClient().send(SLFCGPacket.getItemTopMsg(id, msg));
                              c.dropMessage(5, msg);
                           }

                           MapleInventoryManipulator.addById(c.getClient(), id, count, "", (MaplePet)null, (long)period, "퀘스트 보상을 통해 얻은 아이템 입니다. // 퀘스트 번호 : " + this.quest.getId() + " " + FileoutputUtil.CurrentReadable_Date(), false);
                           c.getClient().getSession().writeAndFlush(CField.EffectPacket.showCharmEffect(c, id, 1, true, ""));
                        }
                     }
                  }

                  item = (MapleQuestAction.QuestItem)var21.next();
               } while(item.prop <= 0);
            } while(!canGetItem(item, c));

            for(id = 0; id < item.prop; ++id) {
               props.put(props.size(), item.itemid);
            }
         }
      case quest:
         var21 = this.state.iterator();

         while(var21.hasNext()) {
            Pair<Integer, Integer> q = (Pair)var21.next();
            c.updateQuest(new MapleQuestStatus(MapleQuest.getInstance((Integer)q.left), (Integer)q.right));
         }

         return;
      case skill:
         Map<Skill, SkillEntry> sa = new HashMap();
         var21 = this.skill.iterator();

         while(true) {
            int skillLevel;
            Skill skillObject;
            boolean found;
            do {
               if (!var21.hasNext()) {
                  c.changeSkillsLevel(sa);
                  return;
               }

               Triple<Integer, Integer, Integer> skills = (Triple)var21.next();
               id = (Integer)skills.left;
               skillLevel = (Integer)skills.mid;
               period = (Integer)skills.right;
               skillObject = SkillFactory.getSkill(id);
               found = false;
               Iterator iterator = this.applicableJobs.iterator();

               while(iterator.hasNext()) {
                  int applicableJob = (Integer)iterator.next();
                  if (c.getJob() == applicableJob) {
                     found = true;
                     break;
                  }
               }
            } while(!skillObject.isBeginnerSkill() && !found);

            sa.put(skillObject, new SkillEntry((byte)Math.max(skillLevel, c.getSkillLevel(skillObject)), (byte)Math.max(period, c.getMasterLevel(skillObject)), SkillFactory.getDefaultSExpiry(skillObject)));
         }
      case exp:
         status = c.getQuest(this.quest);
         if (status.getForfeited() <= 0) {
            c.gainExp((long)(this.intStore * GameConstants.getExpRate_Quest(c.getLevel()) * c.getStat().questBonus * (c.getTrait(MapleTrait.MapleTraitType.sense).getLevel() * 3 / 10 + 100) / 100), true, true, true);
         }
         break;
      case nextQuest:
      case money:
         status = c.getQuest(this.quest);
         if (status.getForfeited() <= 0) {
            c.gainMeso((long)this.intStore, true, true);
         }
         break;
      case pop:
         status = c.getQuest(this.quest);
         if (status.getForfeited() <= 0) {
            int fameGain = this.intStore;
            c.updateSingleStat(MapleStat.FAME, (long)c.getFame());
            c.getClient().getSession().writeAndFlush(CWvsContext.InfoPacket.getShowFameGain(fameGain));
         }
         break;
      case buffItemID:
         status = c.getQuest(this.quest);
         if (status.getForfeited() <= 0) {
            int tobuff = this.intStore;
            if (tobuff > 0) {
               MapleItemInformationProvider.getInstance().getItemEffect(tobuff).applyTo(c, true);
            }
         }
         break;
      case sp:
         status = c.getQuest(this.quest);
         if (status.getForfeited() <= 0) {
            int sp_val = this.intStore;
            if (this.applicableJobs.size() > 0) {
               int finalJob = 0;
               Iterator iterator = this.applicableJobs.iterator();

               while(iterator.hasNext()) {
                  id = (Integer)iterator.next();
                  if (c.getJob() >= id && id > finalJob) {
                     finalJob = id;
                  }
               }

               if (finalJob == 0) {
                  c.gainSP(sp_val);
               } else {
                  c.gainSP(sp_val, GameConstants.getSkillBook(finalJob, 0));
               }
            } else {
               c.gainSP(sp_val);
            }
         }
         break;
      case charmEXP:
      case charismaEXP:
      case craftEXP:
      case insightEXP:
      case senseEXP:
      case willEXP:
         status = c.getQuest(this.quest);
         if (status.getForfeited() <= 0) {
            c.getTrait(MapleTrait.MapleTraitType.getByQuestName(this.type.name())).addExp(this.intStore, c);
         }
      }

   }

   public boolean checkEnd(MapleCharacter c, Integer extSelection) {
      switch(this.type) {
      case item:
         Map<Integer, Integer> props = new HashMap();
         Iterator var13 = this.items.iterator();

         while(true) {
            MapleQuestAction.QuestItem item;
            int id;
            do {
               do {
                  if (!var13.hasNext()) {
                     int selection = 0;
                     int extNum = 0;
                     if (props.size() > 0) {
                        selection = (Integer)props.get(Randomizer.nextInt(props.size()));
                     }

                     byte eq = 0;
                     byte use = 0;
                     byte setup = 0;
                     byte etc = 0;
                     byte cash = 0;
                     var13 = this.items.iterator();

                     while(true) {
                        while(true) {
                           do {
                              if (!var13.hasNext()) {
                                 if (c.getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() < eq) {
                                    c.dropMessage(1, "Please make space for your Equip inventory.");
                                    return false;
                                 }

                                 if (c.getInventory(MapleInventoryType.USE).getNumFreeSlot() < use) {
                                    c.dropMessage(1, "Please make space for your Use inventory.");
                                    return false;
                                 }

                                 if (c.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() < setup) {
                                    c.dropMessage(1, "Please make space for your Setup inventory.");
                                    return false;
                                 }

                                 if (c.getInventory(MapleInventoryType.ETC).getNumFreeSlot() < etc) {
                                    c.dropMessage(1, "Please make space for your Etc inventory.");
                                    return false;
                                 }

                                 if (c.getInventory(MapleInventoryType.CASH).getNumFreeSlot() < cash) {
                                    c.dropMessage(1, "Please make space for your Cash inventory.");
                                    return false;
                                 }

                                 return true;
                              }

                              item = (MapleQuestAction.QuestItem)var13.next();
                           } while(!canGetItem(item, c));

                           id = item.itemid;
                           if (item.prop == -2) {
                              break;
                           }

                           if (item.prop == -1) {
                              if (extSelection == null || extSelection == extNum++) {
                                 break;
                              }
                           } else {
                              if (id != selection) {
                                 continue;
                              }
                              break;
                           }
                        }

                        short count = (short)item.count;
                        if (count < 0) {
                           if (!c.haveItem(id, count, false, true)) {
                              c.dropMessage(1, "You are short of some item to complete quest.");
                              return false;
                           }
                        } else {
                           if (MapleItemInformationProvider.getInstance().isPickupRestricted(id) && c.haveItem(id, 1, true, false)) {
                              c.dropMessage(1, "You have this item already: " + MapleItemInformationProvider.getInstance().getName(id));
                              return false;
                           }

                           switch(GameConstants.getInventoryType(id)) {
                           case EQUIP:
                              ++eq;
                           case USE:
                              ++use;
                           case SETUP:
                              ++setup;
                           case ETC:
                              ++etc;
                           case CASH:
                              ++cash;
                           }
                        }
                     }
                  }

                  item = (MapleQuestAction.QuestItem)var13.next();
               } while(item.prop <= 0);
            } while(!canGetItem(item, c));

            for(id = 0; id < item.prop; ++id) {
               props.put(props.size(), item.itemid);
            }
         }
      case money:
         long meso = (long)this.intStore;
         if (c.getMeso() + meso < 0L) {
            return false;
         } else {
            if (meso < 0L && c.getMeso() < Math.abs(meso)) {
               c.dropMessage(1, "Insufficient meso.");
               return false;
            }

            return true;
         }
      default:
         return true;
      }
   }

   public void runEnd(MapleCharacter c, Integer extSelection) {
      int id;
      int period;
      Iterator var19;
      switch(this.type) {
      case item:
         Map<Integer, Integer> props = new HashMap();
         var19 = this.items.iterator();

         while(true) {
            MapleQuestAction.QuestItem item;
            do {
               do {
                  if (!var19.hasNext()) {
                     int selection = 0;
                     int extNum = 0;
                     if (props.size() > 0) {
                        selection = (Integer)props.get(Randomizer.nextInt(props.size()));
                     }

                     var19 = this.items.iterator();

                     while(true) {
                        while(true) {
                           do {
                              if (!var19.hasNext()) {
                                 return;
                              }

                              item = (MapleQuestAction.QuestItem)var19.next();
                           } while(!canGetItem(item, c));

                           id = item.itemid;
                           if (item.prop == -2) {
                              break;
                           }

                           if (item.prop == -1) {
                              if (extSelection == null || extSelection == extNum++) {
                                 break;
                              }
                           } else {
                              if (id != selection) {
                                 continue;
                              }
                              break;
                           }
                        }

                        short count = (short)item.count;
                        if (count < 0) {
                           MapleInventoryManipulator.removeById(c.getClient(), GameConstants.getInventoryType(id), id, count * -1, true, false);
                           c.getClient().getSession().writeAndFlush(CField.EffectPacket.showCharmEffect(c, id, 1, true, ""));
                        } else {
                           period = item.period / 1440;
                           String name = MapleItemInformationProvider.getInstance().getName(id);
                           if (id / 10000 == 114 && name != null && name.length() > 0) {
                              String msg = "[" + name + "] 훈장을 획득 했습니다!!";
                              c.getClient().send(SLFCGPacket.getItemTopMsg(id, msg));
                              c.dropMessage(5, msg);
                           }

                           MapleInventoryManipulator.addById(c.getClient(), id, count, "", (MaplePet)null, (long)period, "");
                           c.getClient().getSession().writeAndFlush(CField.EffectPacket.showCharmEffect(c, id, 1, true, ""));
                        }
                     }
                  }

                  item = (MapleQuestAction.QuestItem)var19.next();
               } while(item.prop <= 0);
            } while(!canGetItem(item, c));

            for(id = 0; id < item.prop; ++id) {
               props.put(props.size(), item.itemid);
            }
         }
      case quest:
         var19 = this.state.iterator();

         while(var19.hasNext()) {
            Pair<Integer, Integer> q = (Pair)var19.next();
            c.updateQuest(new MapleQuestStatus(MapleQuest.getInstance((Integer)q.left), (Integer)q.right));
         }

         return;
      case skill:
         Map<Skill, SkillEntry> sa = new HashMap();
         var19 = this.skill.iterator();

         while(true) {
            int skillLevel;
            Skill skillObject;
            boolean found;
            do {
               if (!var19.hasNext()) {
                  c.changeSkillsLevel(sa);
                  return;
               }

               Triple<Integer, Integer, Integer> skills = (Triple)var19.next();
               id = (Integer)skills.left;
               skillLevel = (Integer)skills.mid;
               period = (Integer)skills.right;
               skillObject = SkillFactory.getSkill(id);
               found = false;
               Iterator iterator = this.applicableJobs.iterator();

               while(iterator.hasNext()) {
                  int applicableJob = (Integer)iterator.next();
                  if (c.getJob() == applicableJob) {
                     found = true;
                     break;
                  }
               }
            } while(!skillObject.isBeginnerSkill() && !found);

            sa.put(skillObject, new SkillEntry((byte)Math.max(skillLevel, c.getSkillLevel(skillObject)), (byte)Math.max(period, c.getMasterLevel(skillObject)), SkillFactory.getDefaultSExpiry(skillObject)));
         }
      case exp:
         c.gainExp((long)(this.intStore * GameConstants.getExpRate_Quest(c.getLevel()) * c.getStat().questBonus * (c.getTrait(MapleTrait.MapleTraitType.sense).getLevel() * 3 / 10 + 100) / 100), true, true, true);
      case nextQuest:
      default:
         break;
      case money:
         c.gainMeso((long)this.intStore, true, true);
         break;
      case pop:
         int fameGain = this.intStore;
         c.updateSingleStat(MapleStat.FAME, (long)c.getFame());
         c.getClient().getSession().writeAndFlush(CWvsContext.InfoPacket.getShowFameGain(fameGain));
         break;
      case buffItemID:
         int tobuff = this.intStore;
         if (tobuff > 0) {
            MapleItemInformationProvider.getInstance().getItemEffect(tobuff).applyTo(c, true);
         }
         break;
      case sp:
         int sp_val = this.intStore;
         if (this.applicableJobs.size() > 0) {
            int finalJob = 0;
            Iterator iterator = this.applicableJobs.iterator();

            while(iterator.hasNext()) {
               id = (Integer)iterator.next();
               if (c.getJob() >= id && id > finalJob) {
                  finalJob = id;
               }
            }

            if (finalJob == 0) {
               c.gainSP(sp_val);
            } else {
               c.gainSP(sp_val, GameConstants.getSkillBook(finalJob, 0));
            }
         } else {
            c.gainSP(sp_val);
         }
         break;
      case charmEXP:
      case charismaEXP:
      case craftEXP:
      case insightEXP:
      case senseEXP:
      case willEXP:
         c.getTrait(MapleTrait.MapleTraitType.getByQuestName(this.type.name())).addExp(this.intStore, c);
      }

   }

   private static List<Integer> getJobBy5ByteEncoding(int encoded) {
      List<Integer> ret = new ArrayList();
      if ((encoded & 1) != 0) {
         ret.add(0);
      }

      if ((encoded & 2) != 0) {
         ret.add(100);
      }

      if ((encoded & 4) != 0) {
         ret.add(200);
      }

      if ((encoded & 8) != 0) {
         ret.add(300);
      }

      if ((encoded & 16) != 0) {
         ret.add(400);
      }

      if ((encoded & 32) != 0) {
         ret.add(500);
      }

      if ((encoded & 1024) != 0) {
         ret.add(1000);
      }

      if ((encoded & 2048) != 0) {
         ret.add(1100);
      }

      if ((encoded & 4096) != 0) {
         ret.add(1200);
      }

      if ((encoded & 8192) != 0) {
         ret.add(1300);
      }

      if ((encoded & 16384) != 0) {
         ret.add(1400);
      }

      if ((encoded & '耀') != 0) {
         ret.add(1500);
      }

      if ((encoded & 131072) != 0) {
         ret.add(2001);
         ret.add(2200);
      }

      if ((encoded & 1048576) != 0) {
         ret.add(2000);
         ret.add(2001);
      }

      if ((encoded & 2097152) != 0) {
         ret.add(2100);
      }

      if ((encoded & 4194304) != 0) {
         ret.add(2001);
         ret.add(2200);
      }

      if ((encoded & 1073741824) != 0) {
         ret.add(3000);
         ret.add(3200);
         ret.add(3300);
         ret.add(3500);
      }

      return ret;
   }

   private static List<Integer> getJobBySimpleEncoding(int encoded) {
      List<Integer> ret = new ArrayList();
      if ((encoded & 1) != 0) {
         ret.add(200);
      }

      if ((encoded & 2) != 0) {
         ret.add(300);
      }

      if ((encoded & 4) != 0) {
         ret.add(400);
      }

      if ((encoded & 8) != 0) {
         ret.add(500);
      }

      return ret;
   }

   public MapleQuestActionType getType() {
      return this.type;
   }

   public String toString() {
      return this.type.toString();
   }

   public List<Triple<Integer, Integer, Integer>> getSkills() {
      return this.skill;
   }

   public List<MapleQuestAction.QuestItem> getItems() {
      return this.items;
   }

   public static class QuestItem {
      public int itemid;
      public int count;
      public int period;
      public int gender;
      public int job;
      public int jobEx;
      public int prop;

      public QuestItem(int itemid, int count, int period, int gender, int job, int jobEx, int prop) {
         if (RandomRewards.getTenPercent().contains(itemid)) {
            count += Randomizer.nextInt(3);
         }

         this.itemid = itemid;
         this.count = count;
         this.period = period;
         this.gender = gender;
         this.job = job;
         this.jobEx = jobEx;
         this.prop = prop;
      }
   }
}
