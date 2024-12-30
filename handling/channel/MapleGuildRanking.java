package handling.channel;

import handling.world.World;
import handling.world.guild.MapleGuild;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import tools.Triple;

public class MapleGuildRanking {
   private static MapleGuildRanking instance = new MapleGuildRanking();
   private List<MapleGuildRanking.GuildRankingInfo> honorRank = new LinkedList();
   private List<MapleGuildRanking.GuildRankingInfo> flagRaceRank = new LinkedList();
   private List<MapleGuildRanking.GuildRankingInfo> culvertRank = new LinkedList();
   private static long lastReloadTime = 0L;

   public static MapleGuildRanking getInstance() {
      return instance;
   }

   public void load() {
      this.reload();
   }

   public List<MapleGuildRanking.GuildRankingInfo> getHonorRank() {
      return this.honorRank;
   }

   private void reload() {
      this.honorRank.clear();
      this.flagRaceRank.clear();
      this.culvertRank.clear();
      List<Triple<String, Integer, Integer>> honorranks = new ArrayList();
      new ArrayList();
      List<Triple<String, Integer, Integer>> culvertRanks = new ArrayList();
      Iterator var4 = World.Guild.getGuilds().iterator();

      while(var4.hasNext()) {
         MapleGuild g = (MapleGuild)var4.next();
         if (g.getWeekReputation() > 0) {
            honorranks.add(new Triple(g.getName(), g.getWeekReputation(), g.getId()));
         }

         if (g.getGuildScore() > 0.0D) {
            culvertRanks.add(new Triple(g.getName(), (int)g.getGuildScore(), g.getId()));
         }
      }

      String names;
      int chridtmp;
      int chrpointtmp;
      int i;
      int j;
      for(i = 0; i < honorranks.size() - 1; ++i) {
         for(j = 0; j < honorranks.size() - i - 1; ++j) {
            if ((Integer)((Triple)honorranks.get(j)).getMid() < (Integer)((Triple)honorranks.get(j + 1)).getMid()) {
               names = (String)((Triple)honorranks.get(j + 1)).getLeft();
               chridtmp = (Integer)((Triple)honorranks.get(j + 1)).getMid();
               chrpointtmp = (Integer)((Triple)honorranks.get(j + 1)).getRight();
               honorranks.set(j + 1, (Triple)honorranks.get(j));
               honorranks.set(j, new Triple(names, chridtmp, chrpointtmp));
            }
         }
      }

      for(i = 0; i < culvertRanks.size() - 1; ++i) {
         for(j = 0; j < culvertRanks.size() - i - 1; ++j) {
            if ((Integer)((Triple)culvertRanks.get(j)).getMid() < (Integer)((Triple)culvertRanks.get(j + 1)).getMid()) {
               names = (String)((Triple)culvertRanks.get(j + 1)).getLeft();
               chridtmp = (Integer)((Triple)culvertRanks.get(j + 1)).getMid();
               chrpointtmp = (Integer)((Triple)culvertRanks.get(j + 1)).getRight();
               culvertRanks.set(j + 1, (Triple)culvertRanks.get(j));
               culvertRanks.set(j, new Triple(names, chridtmp, chrpointtmp));
            }
         }
      }

      Iterator var11 = honorranks.iterator();

      Triple list;
      while(var11.hasNext()) {
         list = (Triple)var11.next();
         this.honorRank.add(new MapleGuildRanking.GuildRankingInfo((String)list.getLeft(), (Integer)list.getMid(), (Integer)list.getRight()));
      }

      var11 = culvertRanks.iterator();

      while(var11.hasNext()) {
         list = (Triple)var11.next();
         this.culvertRank.add(new MapleGuildRanking.GuildRankingInfo((String)list.getLeft(), (Integer)list.getMid(), (Integer)list.getRight()));
      }

   }

   public List<MapleGuildRanking.GuildRankingInfo> getCulvertRank() {
      return this.culvertRank;
   }

   public void setCulvertRank(List<MapleGuildRanking.GuildRankingInfo> culvertRank) {
      this.culvertRank = culvertRank;
   }

   public List<MapleGuildRanking.GuildRankingInfo> getFlagRaceRank() {
      return this.flagRaceRank;
   }

   public void setFlagRaceRank(List<MapleGuildRanking.GuildRankingInfo> flagRaceRank) {
      this.flagRaceRank = flagRaceRank;
   }

   public static class GuildRankingInfo {
      private String name;
      private int score;
      private int id;

      public GuildRankingInfo(String name, int score, int id) {
         this.name = name;
         this.score = score;
         this.id = id;
      }

      public String getName() {
         return this.name;
      }

      public int getScore() {
         return this.score;
      }

      public int getId() {
         return this.id;
      }

      public void setId(int gid) {
         this.id = gid;
      }
   }
}
