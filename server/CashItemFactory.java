package server;

import constants.ServerConstants;
import database.DatabaseConnection;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.Pair;

public class CashItemFactory {
   private static final CashItemFactory instance = new CashItemFactory();
   private final List<Pair<Integer, Integer>> bestItems = new ArrayList();
   private final Map<Integer, CashItemInfo> itemStats = new HashMap();
   private final Map<Integer, List<Integer>> itemPackage = new HashMap();
   private final Map<Integer, CashItemInfo.CashModInfo> itemMods = new HashMap();
   private final Map<Integer, List<Integer>> openBox = new HashMap();
   private final MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wz") + "/Etc.wz"));

   public static final CashItemFactory getInstance() {
      return instance;
   }

   public void initialize() {
      List<MapleData> cccc = this.data.getData("Commodity.img").getChildren();
      Iterator var2 = cccc.iterator();

      while(var2.hasNext()) {
         MapleData field = (MapleData)var2.next();
         int SN = MapleDataTool.getIntConvert("SN", field, 0);
         CashItemInfo stats = new CashItemInfo(MapleDataTool.getIntConvert("ItemId", field, 0), MapleDataTool.getIntConvert("Count", field, 1), MapleDataTool.getIntConvert("Price", field, 0), SN, MapleDataTool.getIntConvert("Period", field, 0), MapleDataTool.getIntConvert("Gender", field, 2), MapleDataTool.getIntConvert("OnSale", field, 0) > 0 && MapleDataTool.getIntConvert("Price", field, 0) > 0, MapleDataTool.getIntConvert("LimitMax", field, 0));
         if (SN > 0) {
            this.itemStats.put(SN, stats);
         }
      }

      MapleData b = this.data.getData("CashPackage.img");
      Iterator var19 = b.getChildren().iterator();

      while(true) {
         MapleData c;
         do {
            if (!var19.hasNext()) {
               Connection con = null;
               PreparedStatement ps = null;
               ResultSet rs = null;

               try {
                  con = DatabaseConnection.getConnection();
                  ps = con.prepareStatement("SELECT * FROM cashshop_modified_items");
                  rs = ps.executeQuery();
                  if (rs.next()) {
                     CashItemInfo.CashModInfo ret = new CashItemInfo.CashModInfo(rs.getInt("serial"), rs.getInt("discount_price"), rs.getInt("mark"), rs.getInt("showup") > 0, rs.getInt("itemid"), rs.getInt("priority"), rs.getInt("package") > 0, rs.getInt("period"), rs.getInt("gender"), rs.getInt("count"), rs.getInt("meso"), rs.getInt("unk_1"), rs.getInt("unk_2"), rs.getInt("unk_3"), rs.getInt("extra_flags"));
                     this.itemMods.put(ret.sn, ret);
                     if (ret.showUp) {
                        CashItemInfo cc = (CashItemInfo)this.itemStats.get(ret.sn);
                        if (cc != null) {
                           ret.toCItem(cc);
                        }
                     }
                  }

                  rs.close();
                  ps.close();
                  con.close();
               } catch (Exception var16) {
                  var16.printStackTrace();
               } finally {
                  try {
                     if (con != null) {
                        con.close();
                     }

                     if (ps != null) {
                        ps.close();
                     }

                     if (rs != null) {
                        rs.close();
                     }
                  } catch (SQLException var15) {
                     var15.printStackTrace();
                  }

               }

               List<Integer> availableSN = new LinkedList();
               availableSN.add(20001141);
               availableSN.add(20001142);
               availableSN.add(20001143);
               availableSN.add(20001144);
               availableSN.add(20001145);
               availableSN.add(20001146);
               availableSN.add(20001147);
               this.openBox.put(5533003, availableSN);
               availableSN = new LinkedList();
               availableSN.add(20000462);
               availableSN.add(20000463);
               availableSN.add(20000464);
               availableSN.add(20000465);
               availableSN.add(20000466);
               availableSN.add(20000467);
               availableSN.add(20000468);
               availableSN.add(20000469);
               this.openBox.put(5533000, availableSN);
               availableSN = new LinkedList();
               availableSN.add(20800259);
               availableSN.add(20800260);
               availableSN.add(20800263);
               availableSN.add(20800264);
               availableSN.add(20800265);
               availableSN.add(20800267);
               this.openBox.put(5533001, availableSN);
               availableSN = new LinkedList();
               availableSN.add(20800270);
               availableSN.add(20800271);
               availableSN.add(20800272);
               availableSN.add(20800273);
               availableSN.add(20800274);
               this.openBox.put(5533002, availableSN);
               this.bestItems.clear();
               Iterator var28 = ServerConstants.CashMainInfo.iterator();

               while(var28.hasNext()) {
                  Pair<Integer, Integer> best = (Pair)var28.next();
                  CashItemInfo info = this.getItem((Integer)best.getRight());
                  if (info != null) {
                     this.bestItems.add(new Pair((Integer)best.getLeft(), (Integer)best.getRight()));
                  }
               }

               return;
            }

            c = (MapleData)var19.next();
         } while(c.getChildByPath("SN") == null);

         List<Integer> packageItems = new ArrayList();
         Iterator var6 = c.getChildByPath("SN").getChildren().iterator();

         while(var6.hasNext()) {
            MapleData d = (MapleData)var6.next();
            packageItems.add(MapleDataTool.getIntConvert(d));
         }

         this.itemPackage.put(Integer.parseInt(c.getName()), packageItems);
      }
   }

   public final CashItemInfo getSimpleItem(int sn) {
      return (CashItemInfo)this.itemStats.get(sn);
   }

   public final CashItemInfo getItem(int sn) {
      CashItemInfo stats = (CashItemInfo)this.itemStats.get(sn);
      CashItemInfo.CashModInfo z = this.getModInfo(sn);
      if (z != null && z.showUp) {
         return z.toCItem(stats);
      } else {
         return stats == null ? null : stats;
      }
   }

   public final List<Integer> getPackageItems(int itemId) {
      return (List)this.itemPackage.get(itemId);
   }

   public final CashItemInfo.CashModInfo getModInfo(int sn) {
      return (CashItemInfo.CashModInfo)this.itemMods.get(sn);
   }

   public final Collection<CashItemInfo.CashModInfo> getAllModInfo() {
      return this.itemMods.values();
   }

   public final Map<Integer, List<Integer>> getRandomItemInfo() {
      return this.openBox;
   }

   public final List<Pair<Integer, Integer>> getBestItems() {
      return this.bestItems;
   }
}
