package server;

import constants.GameConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RandomRewards {
   private static List<Integer> compiledGold = null;
   private static List<Integer> compiledSilver = null;
   private static List<Integer> compiledFishing = null;
   private static List<Integer> compiledRandomItem = null;
   private static List<Integer> compiledPeanut = null;
   private static List<Integer> compiledEvent = null;
   private static List<Integer> compiledEventC = null;
   private static List<Integer> compiledEventB = null;
   private static List<Integer> compiledEventA = null;
   private static List<Integer> compiledTheSeedItems = null;
   private static List<Integer> compiledDrops = null;
   private static List<Integer> compiledDropsB = null;
   private static List<Integer> compiledDropsA = null;
   private static List<Integer> tenPercent = null;

   private static void processRewards(List<Integer> returnArray, int[] list) {
      int lastitem = 0;

      for(int i = 0; i < list.length; ++i) {
         if (i % 2 == 0) {
            lastitem = list[i];
         } else {
            for(int j = 0; j < list[i]; ++j) {
               returnArray.add(lastitem);
            }
         }
      }

      Collections.shuffle(returnArray);
   }

   private static void processRewardsSimple(List<Integer> returnArray, int[] list) {
      for(int i = 0; i < list.length; ++i) {
         returnArray.add(list[i]);
      }

      Collections.shuffle(returnArray);
   }

   public static int getGoldBoxReward() {
      return (Integer)compiledGold.get(Randomizer.nextInt(compiledGold.size()));
   }

   public static int getSilverBoxReward() {
      return (Integer)compiledSilver.get(Randomizer.nextInt(compiledSilver.size()));
   }

   public static int getFishingReward() {
      return (Integer)compiledFishing.get(Randomizer.nextInt(compiledFishing.size()));
   }

   public static int getRandomReward() {
      return (Integer)compiledRandomItem.get(Randomizer.nextInt(compiledRandomItem.size()));
   }

   public static int getTheSeedReward() {
      return (Integer)compiledTheSeedItems.get(Randomizer.nextInt(compiledTheSeedItems.size()));
   }

   public static int getPeanutReward() {
      return (Integer)compiledPeanut.get(Randomizer.nextInt(compiledPeanut.size()));
   }

   public static int getEventReward() {
      int chance = Randomizer.nextInt(101);
      if (chance < 66) {
         return (Integer)compiledEventC.get(Randomizer.nextInt(compiledEventC.size()));
      } else if (chance < 86) {
         return (Integer)compiledEventB.get(Randomizer.nextInt(compiledEventB.size()));
      } else {
         return chance < 96 ? (Integer)compiledEventA.get(Randomizer.nextInt(compiledEventA.size())) : (Integer)compiledEvent.get(Randomizer.nextInt(compiledEvent.size()));
      }
   }

   public static int getDropReward() {
      int chance = Randomizer.nextInt(101);
      if (chance < 76) {
         return (Integer)compiledDrops.get(Randomizer.nextInt(compiledDrops.size()));
      } else {
         return chance < 96 ? (Integer)compiledDropsB.get(Randomizer.nextInt(compiledDropsB.size())) : (Integer)compiledDropsA.get(Randomizer.nextInt(compiledDropsA.size()));
      }
   }

   public static List<Integer> getTenPercent() {
      return tenPercent;
   }

   static void load() {
   }

   static {
      List<Integer> returnArray = new ArrayList();
      processRewards(returnArray, GameConstants.goldrewards);
      compiledGold = returnArray;
      returnArray = new ArrayList();
      processRewards(returnArray, GameConstants.silverrewards);
      compiledSilver = returnArray;
      returnArray = new ArrayList();
      processRewards(returnArray, GameConstants.fishingReward);
      compiledFishing = returnArray;
      returnArray = new ArrayList();
      processRewards(returnArray, GameConstants.randomReward);
      compiledRandomItem = returnArray;
      returnArray = new ArrayList();
      processRewards(returnArray, GameConstants.theSeedBoxReward);
      compiledTheSeedItems = returnArray;
      returnArray = new ArrayList();
      processRewards(returnArray, GameConstants.eventCommonReward);
      compiledEventC = returnArray;
      returnArray = new ArrayList();
      processRewards(returnArray, GameConstants.eventUncommonReward);
      compiledEventB = returnArray;
      returnArray = new ArrayList();
      processRewards(returnArray, GameConstants.eventRareReward);
      processRewardsSimple(returnArray, GameConstants.tenPercent);
      processRewardsSimple(returnArray, GameConstants.tenPercent);
      compiledEventA = returnArray;
      returnArray = new ArrayList();
      processRewards(returnArray, GameConstants.eventSuperReward);
      compiledEvent = returnArray;
      returnArray = new ArrayList();
      processRewards(returnArray, GameConstants.peanuts);
      compiledPeanut = returnArray;
      returnArray = new ArrayList();
      processRewardsSimple(returnArray, GameConstants.normalDrops);
      compiledDrops = returnArray;
      returnArray = new ArrayList();
      processRewardsSimple(returnArray, GameConstants.rareDrops);
      compiledDropsB = returnArray;
      returnArray = new ArrayList();
      processRewardsSimple(returnArray, GameConstants.superDrops);
      compiledDropsA = returnArray;
      returnArray = new ArrayList();
      processRewardsSimple(returnArray, GameConstants.tenPercent);
      tenPercent = returnArray;
   }
}
