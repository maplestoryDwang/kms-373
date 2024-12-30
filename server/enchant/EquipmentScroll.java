package server.enchant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import tools.Pair;

public class EquipmentScroll {
   private int juhun;
   private int flag;
   private List<Pair<EnchantFlag, Integer>> stats = new ArrayList();
   private String name;

   public EquipmentScroll(String name, int juhun, List<Pair<EnchantFlag, Integer>> stats) {
      this.name = name;
      this.juhun = juhun;
      this.setStats(stats);
      this.setFlag();
   }

   public int getJuhun() {
      return this.juhun;
   }

   public void setJuhun(int juhun) {
      this.juhun = juhun;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Pair<EnchantFlag, Integer> getFlag(EnchantFlag flag) {
      Iterator var2 = this.stats.iterator();

      Pair stat;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         stat = (Pair)var2.next();
      } while(flag.getValue() != ((EnchantFlag)stat.left).getValue());

      return stat;
   }

   public void setFlag() {
      int flag = 0;
      if (this.getFlag(EnchantFlag.Watk) != null) {
         flag |= EnchantFlag.Watk.getValue();
      }

      if (this.getFlag(EnchantFlag.Matk) != null) {
         flag |= EnchantFlag.Matk.getValue();
      }

      if (this.getFlag(EnchantFlag.Str) != null) {
         flag |= EnchantFlag.Str.getValue();
      }

      if (this.getFlag(EnchantFlag.Dex) != null) {
         flag |= EnchantFlag.Dex.getValue();
      }

      if (this.getFlag(EnchantFlag.Int) != null) {
         flag |= EnchantFlag.Int.getValue();
      }

      if (this.getFlag(EnchantFlag.Luk) != null) {
         flag |= EnchantFlag.Luk.getValue();
      }

      if (this.getFlag(EnchantFlag.Wdef) != null) {
         flag |= EnchantFlag.Wdef.getValue();
      }

      if (this.getFlag(EnchantFlag.Mdef) != null) {
         flag |= EnchantFlag.Mdef.getValue();
      }

      if (this.getFlag(EnchantFlag.Hp) != null) {
         flag |= EnchantFlag.Hp.getValue();
      }

      if (this.getFlag(EnchantFlag.Mp) != null) {
         flag |= EnchantFlag.Mp.getValue();
      }

      if (this.getFlag(EnchantFlag.Acc) != null) {
         flag |= EnchantFlag.Acc.getValue();
      }

      if (this.getFlag(EnchantFlag.Avoid) != null) {
         flag |= EnchantFlag.Avoid.getValue();
      }

      this.flag = flag;
   }

   public int getFlag() {
      return this.flag;
   }

   public void setFlag(int flag) {
      this.flag = flag;
   }

   public List<Pair<EnchantFlag, Integer>> getStats() {
      return this.stats;
   }

   public void setStats(List<Pair<EnchantFlag, Integer>> stats) {
      Iterator var2 = stats.iterator();

      while(var2.hasNext()) {
         Pair<EnchantFlag, Integer> stat = (Pair)var2.next();
         this.stats.add(stat);
      }

   }
}
