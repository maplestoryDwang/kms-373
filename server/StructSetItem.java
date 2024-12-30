package server;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StructSetItem {
   public byte completeCount;
   public int setItemID;
   public boolean jokerPossible;
   public boolean zeroWeaponJokerPossible;
   public Map<Integer, StructSetItem.SetItem> items = new LinkedHashMap();
   public List<Integer> itemIDs = new ArrayList();

   public Map<Integer, StructSetItem.SetItem> getItems() {
      return new LinkedHashMap(this.items);
   }

   public static class SetItem {
      public int incPDD;
      public int incMDD;
      public int incSTR;
      public int incDEX;
      public int incINT;
      public int incLUK;
      public int incACC;
      public int incPAD;
      public int incMAD;
      public int incSpeed;
      public int incMHP;
      public int incMMP;
      public int incMHPr;
      public int incMMPr;
      public int incAllStat;
      public int option1;
      public int option2;
      public int option1Level;
      public int option2Level;
      public Map<Integer, Byte> activeSkills = new LinkedHashMap();
   }
}
