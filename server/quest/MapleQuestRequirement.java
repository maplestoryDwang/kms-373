package server.quest;

import client.MapleCharacter;
import client.MapleQuestStatus;
import client.MapleTrait;
import client.Skill;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.GameConstants;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import tools.Pair;

public class MapleQuestRequirement implements Serializable {
   private static final long serialVersionUID = 9179541993413738569L;
   private MapleQuest quest;
   private MapleQuestRequirementType type;
   private int intStore;
   private String stringStore;
   private List<Pair<Integer, Integer>> dataStore;

   public MapleQuestRequirement(MapleQuest quest, MapleQuestRequirementType type, ResultSet rse) throws SQLException {
      this.type = type;
      this.quest = quest;
      switch(type) {
      case pet:
      case mbcard:
      case mob:
      case item:
      case quest:
      case skill:
      case job:
         this.dataStore = new LinkedList();
         String[] first = rse.getString("intStoresFirst").split(", ");
         String[] second = rse.getString("intStoresSecond").split(", ");
         if (first.length <= 0 && rse.getString("intStoresFirst").length() > 0) {
            this.dataStore.add(new Pair(Integer.parseInt(rse.getString("intStoresFirst")), Integer.parseInt(rse.getString("intStoresSecond"))));
         }

         for(int i = 0; i < first.length; ++i) {
            if (first[i].length() > 0 && second[i].length() > 0) {
               this.dataStore.add(new Pair(Integer.parseInt(first[i]), Integer.parseInt(second[i])));
            }
         }

         return;
      case partyQuest_S:
      case dayByDay:
      case normalAutoStart:
      case subJobFlags:
      case fieldEnter:
      case pettamenessmin:
      case npc:
      case questComplete:
      case pop:
      case interval:
      case mbmin:
      case lvmax:
      case lvmin:
         this.intStore = Integer.parseInt(rse.getString("stringStore"));
         break;
      case end:
         this.stringStore = rse.getString("stringStore");
      }

   }

   public boolean check(MapleCharacter c, Integer npcid) {
      int var8;
      int itemId;
      Iterator var15;
      Pair a;
      int state;
      switch(this.type) {
      case pet:
         var15 = this.dataStore.iterator();

         do {
            if (!var15.hasNext()) {
               return false;
            }

            a = (Pair)var15.next();
         } while(c.getPetById((Integer)a.getRight()) == -1);

         return true;
      case mbcard:
      case dayByDay:
      case normalAutoStart:
      case mbmin:
      default:
         return true;
      case mob:
         var15 = this.dataStore.iterator();

         do {
            if (!var15.hasNext()) {
               return true;
            }

            a = (Pair)var15.next();
            itemId = (Integer)a.getLeft();
            state = (Integer)a.getRight();
         } while(c.getQuest(this.quest).getMobKills(itemId) >= state);

         return false;
      case item:
         var15 = this.dataStore.iterator();

         short quantity;
         int count;
         do {
            if (!var15.hasNext()) {
               return true;
            }

            a = (Pair)var15.next();
            itemId = (Integer)a.getLeft();
            quantity = 0;
            MapleInventoryType iType = GameConstants.getInventoryType(itemId);

            Item item;
            for(Iterator var12 = c.getInventory(iType).listById(itemId).iterator(); var12.hasNext(); quantity += item.getQuantity()) {
               item = (Item)var12.next();
            }

            count = (Integer)a.getRight();
         } while(quantity >= count && (count > 0 || quantity <= 0));

         return false;
      case quest:
         var15 = this.dataStore.iterator();

         MapleQuestStatus q;
         do {
            do {
               do {
                  if (!var15.hasNext()) {
                     return true;
                  }

                  a = (Pair)var15.next();
                  q = c.getQuest(MapleQuest.getInstance((Integer)a.getLeft()));
                  state = (Integer)a.getRight();
               } while(state == 0);
            } while(q == null && state == 0);
         } while(q != null && q.getStatus() == state);

         return false;
      case skill:
         var15 = this.dataStore.iterator();

         Skill skil;
         label212:
         do {
            while(var15.hasNext()) {
               a = (Pair)var15.next();
               boolean acquire = (Integer)a.getRight() > 0;
               state = (Integer)a.getLeft();
               skil = SkillFactory.getSkill(state);
               if (!acquire) {
                  continue label212;
               }

               if (skil.isFourthJob()) {
                  if (c.getMasterLevel(skil) == 0) {
                     return false;
                  }
               } else if (c.getSkillLevel(skil) == 0) {
                  return false;
               }
            }

            return true;
         } while(c.getSkillLevel(skil) <= 0 && c.getMasterLevel(skil) <= 0);

         return false;
      case job:
         var15 = this.dataStore.iterator();

         do {
            if (!var15.hasNext()) {
               return false;
            }

            a = (Pair)var15.next();
         } while((Integer)a.getRight() != c.getJob() && !c.isGM());

         return true;
      case partyQuest_S:
         int[] partyQuests = new int[]{1200, 1201, 1202, 1203, 1204, 1205, 1206, 1300, 1301, 1302};
         int sRankings = 0;
         int[] var14 = partyQuests;
         var8 = partyQuests.length;

         for(itemId = 0; itemId < var8; ++itemId) {
            state = var14[itemId];
            String rank = c.getOneInfo(state, "rank");
            if (rank != null && rank.equals("S")) {
               ++sRankings;
            }
         }

         return sRankings >= 5;
      case subJobFlags:
         return c.getSubcategory() == this.intStore / 2;
      case fieldEnter:
         if (this.intStore > 0) {
            return this.intStore == c.getMapId();
         }

         return true;
      case pettamenessmin:
         MaplePet[] var7 = c.getPets();
         var8 = var7.length;

         for(itemId = 0; itemId < var8; ++itemId) {
            MaplePet pet = var7[itemId];
            if (pet.getSummoned() && pet.getCloseness() >= this.intStore) {
               return true;
            }
         }

         return false;
      case npc:
         return npcid == null || npcid == this.intStore;
      case questComplete:
         return c.getNumQuest() >= this.intStore;
      case pop:
         return c.getFame() >= this.intStore;
      case interval:
         return c.getQuest(this.quest).getStatus() != 2 || c.getQuest(this.quest).getCompletionTime() <= System.currentTimeMillis() - (long)(this.intStore * 60) * 1000L;
      case lvmax:
         return c.getLevel() <= this.intStore;
      case lvmin:
         return c.getLevel() >= this.intStore;
      case end:
         String timeStr = this.stringStore;
         if (timeStr != null && timeStr.length() > 0) {
            Calendar cal = Calendar.getInstance();
            cal.set(Integer.parseInt(timeStr.substring(0, 4)), Integer.parseInt(timeStr.substring(4, 6)), Integer.parseInt(timeStr.substring(6, 8)), Integer.parseInt(timeStr.substring(8, 10)), 0);
            return cal.getTimeInMillis() >= System.currentTimeMillis();
         }

         return true;
      case craftMin:
      case willMin:
      case charismaMin:
      case insightMin:
      case charmMin:
      case senseMin:
         return c.getTrait(MapleTrait.MapleTraitType.getByQuestName(this.type.name())).getLevel() >= this.intStore;
      }
   }

   public MapleQuestRequirementType getType() {
      return this.type;
   }

   public String toString() {
      return this.type.toString();
   }

   public List<Pair<Integer, Integer>> getDataStore() {
      return this.dataStore;
   }
}
