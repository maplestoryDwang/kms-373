package client;

import constants.GameConstants;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import server.life.MapleLifeFactory;
import server.quest.MapleQuest;

public class MapleQuestStatus implements Serializable {
   private static final long serialVersionUID = 91795419934134L;
   private transient MapleQuest quest;
   private byte status;
   private Map<Integer, Integer> killedMobs = null;
   private int npc;
   private long completionTime;
   private int forfeited = 0;
   private String customData;

   public MapleQuestStatus(MapleQuest quest, int status) {
      this.quest = quest;
      this.setStatus((byte)status);
      this.completionTime = System.currentTimeMillis();
      if (status == 1 && !quest.getRelevantMobs().isEmpty()) {
         this.registerMobs();
      }

   }

   public MapleQuestStatus(MapleQuest quest, byte status, int npc) {
      this.quest = quest;
      this.setStatus(status);
      this.setNpc(npc);
      this.completionTime = System.currentTimeMillis();
      if (status == 1 && !quest.getRelevantMobs().isEmpty()) {
         this.registerMobs();
      }

   }

   public final void setQuest(int qid) {
      this.quest = MapleQuest.getInstance(qid);
   }

   public final MapleQuest getQuest() {
      return this.quest;
   }

   public final byte getStatus() {
      return this.status;
   }

   public final void setStatus(byte status) {
      this.status = status;
   }

   public final int getNpc() {
      return this.npc;
   }

   public final void setNpc(int npc) {
      this.npc = npc;
   }

   public boolean isCustom() {
      return GameConstants.isCustomQuest(this.quest.getId());
   }

   private final void registerMobs() {
      this.killedMobs = new LinkedHashMap();
      Iterator iterator = this.quest.getRelevantMobs().keySet().iterator();

      while(iterator.hasNext()) {
         int i = (Integer)iterator.next();
         this.killedMobs.put(i, 0);
      }

   }

   private final int maxMob(int mobid) {
      Iterator var2 = this.quest.getRelevantMobs().entrySet().iterator();

      Entry qs;
      do {
         if (!var2.hasNext()) {
            return 0;
         }

         qs = (Entry)var2.next();
      } while((Integer)qs.getKey() != mobid);

      return (Integer)qs.getValue();
   }

   public final boolean mobKilled(int id, int skillID, MapleCharacter chr) {
      if (this.quest != null && this.quest.getSkillID() > 0 && this.quest.getSkillID() != skillID) {
         return false;
      } else {
         Integer mob = (Integer)this.killedMobs.get(id);
         if (mob != null) {
            int mo = this.maxMob(id);
            if (mob >= mo) {
               return false;
            } else {
               this.killedMobs.put(id, Math.min(mob + 1, mo));
               return true;
            }
         } else {
            Iterator var5 = this.killedMobs.entrySet().iterator();

            Entry mo;
            do {
               if (!var5.hasNext()) {
                  return false;
               }

               mo = (Entry)var5.next();
            } while(!this.questCount((Integer)mo.getKey(), id));

            int mobb = this.maxMob((Integer)mo.getKey());
            if ((Integer)mo.getValue() >= mobb) {
               return false;
            } else {
               if ((Integer)mo.getKey() == 9101025) {
                  int reqLevel = MapleLifeFactory.getMonster(id).getStats().getLevel();
                  if (reqLevel >= chr.getLevel() - 20 && reqLevel <= chr.getLevel() + 20) {
                     this.killedMobs.put((Integer)mo.getKey(), Math.min((Integer)mo.getValue() + 1, mobb));
                  }
               } else if ((Integer)mo.getKey() == 9101067) {
                  int scale = MapleLifeFactory.getMonster(id).getScale();
                  if (scale > 100) {
                     this.killedMobs.put((Integer)mo.getKey(), Math.min((Integer)mo.getValue() + 1, mobb));
                  }
               } else {
                  this.killedMobs.put((Integer)mo.getKey(), Math.min((Integer)mo.getValue() + 1, mobb));
               }

               return true;
            }
         }
      }
   }

   private final boolean questCount(int mo, int id) {
      if (MapleLifeFactory.getQuestCount(mo) != null) {
         Iterator iterator = MapleLifeFactory.getQuestCount(mo).iterator();

         while(iterator.hasNext()) {
            int i = (Integer)iterator.next();
            if (i == id || mo == 9101025) {
               return true;
            }
         }
      }

      return false;
   }

   public final void setMobKills(int id, int count) {
      if (this.killedMobs == null) {
         this.registerMobs();
      }

      this.killedMobs.put(id, count);
   }

   public final boolean hasMobKills() {
      if (this.killedMobs == null) {
         return false;
      } else {
         return this.killedMobs.size() > 0;
      }
   }

   public final int getMobKills(int id) {
      Integer mob = (Integer)this.killedMobs.get(id);
      return mob == null ? 0 : mob;
   }

   public final Map<Integer, Integer> getMobKills() {
      return this.killedMobs;
   }

   public final long getCompletionTime() {
      return this.completionTime;
   }

   public final void setCompletionTime(long completionTime) {
      this.completionTime = completionTime;
   }

   public final int getForfeited() {
      return this.forfeited;
   }

   public final void setForfeited(int forfeited) {
      if (forfeited >= this.forfeited) {
         this.forfeited = forfeited;
      } else {
         throw new IllegalArgumentException("Can't set forfeits to something lower than before.");
      }
   }

   public final void setCustomData(String customData) {
      this.customData = customData;
   }

   public final String getCustomData() {
      return this.customData;
   }
}
