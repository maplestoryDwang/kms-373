package client.custom.inventory;

import java.util.ArrayList;
import java.util.List;
import tools.Pair;

public class CustomItem {
   private int id;
   private CustomItem.CustomItemType type;
   private String name;
   private List<Pair<CustomItem.CustomItemEffect, Integer>> effects;

   public CustomItem(int id, CustomItem.CustomItemType type, String name) {
      this.id = id;
      this.type = type;
      this.name = name;
      this.effects = new ArrayList();
   }

   public int getId() {
      return this.id;
   }

   public CustomItem.CustomItemType getType() {
      return this.type;
   }

   public String getName() {
      return this.name;
   }

   public List<Pair<CustomItem.CustomItemEffect, Integer>> getEffects() {
      return this.effects;
   }

   public void addEffects(CustomItem.CustomItemEffect effect, Integer value) {
      this.effects.add(new Pair(effect, value));
   }

   public static enum CustomItemType {
      None,
      보조장비,
      각인석;
   }

   public static enum CustomItemEffect {
      BdR,
      CrD,
      AllStatR,
      MesoR,
      DropR;
   }
}
