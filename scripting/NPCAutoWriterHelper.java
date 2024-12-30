package scripting;

import client.MapleClient;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import provider.MapleData;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;

public class NPCAutoWriterHelper {
   private int npcID;
   private MapleClient ha;
   private FileOutputStream out = null;

   public NPCAutoWriterHelper(int id, MapleClient ha) {
      this.npcID = id;
      this.ha = ha;
   }

   public final boolean checkFileExist() {
      try {
         if ((new File("Scripts/npc/" + this.npcID + ".js")).exists()) {
            return true;
         }
      } catch (Exception var2) {
         var2.printStackTrace();
      }

      return false;
   }

   public static final String getNPCName(int id) {
      return MapleDataTool.getString(id + "/name", MapleDataProviderFactory.getDataProvider(new File("wz/String.wz")).getData("Npc.img"), "MISSINGNO");
   }

   public static final String getNPCFunc(int id) {
      return MapleDataTool.getString(id + "/func", MapleDataProviderFactory.getDataProvider(new File("wz/String.wz")).getData("Npc.img"), "MISSINGNO");
   }

   public final String getNpcName() {
      return MapleDataTool.getString(this.npcID + "/name", MapleDataProviderFactory.getDataProvider(new File("wz/String.wz")).getData("Npc.img"), "MISSINGNO");
   }

   public final String getNpcFunc() {
      return MapleDataTool.getString(this.npcID + "/func", MapleDataProviderFactory.getDataProvider(new File("wz/String.wz")).getData("Npc.img"), "MISSINGNO");
   }

   public final String addInfo(int id) {
      String a = "#d";
      a = a + "엔피시 이름 : " + getNPCName(id);
      a = a + "\r\n#r";
      if (!"MISSINGNO".equals(getNPCFunc(id))) {
         a = a + "엔피시 설명 : " + getNPCFunc(id) + "\r\n";
      }

      a = a + "\r\n#k";
      Iterator var3 = MapleDataProviderFactory.getDataProvider(new File("wz/String.wz")).getData("Npc.img").getChildByPath(id.makeConcatWithConstants<invokedynamic>(id)).getChildren().iterator();

      while(var3.hasNext()) {
         MapleData d = (MapleData)var3.next();
         if (!d.getName().equals("name") && !d.getName().equals("func")) {
            a = a + d.getName() + " : " + (String)d.getData() + "\r\n";
         }
      }

      return a;
   }

   public final void doMain() {
      try {
         if (this.checkFileExist()) {
            return;
         }

         this.out = new FileOutputStream("Scripts/npc/" + this.npcID + ".js");
      } catch (FileNotFoundException var2) {
         this.dropMessage("파일을 작성하는데 실패했습니다. 서버프로그램에 파일 쓰기 권한이 있는지 확인해 주세요.");
         var2.printStackTrace();
      } catch (NullPointerException var3) {
         this.dropMessage("파일을 작성하는데 실패했습니다. 널 포인터 오류가 발생했습니다.");
         var3.printStackTrace();
      } catch (Exception var4) {
         var4.printStackTrace();
      }

   }

   public final void dropMessage(String text) {
      this.ha.getPlayer().dropMessage(1, text);
   }

   public final void writeLine(String text) {
      if (this.out != null) {
         try {
            this.out.write(text.getBytes(Charset.forName("UTF-8")));
         } catch (Exception var3) {
            var3.printStackTrace();
         }
      }

   }

   public final void newLine() {
      if (this.out != null) {
         try {
            this.out.write(System.getProperty("line.separator").getBytes());
         } catch (Exception var2) {
            var2.printStackTrace();
         }
      }

   }

   public final void closeFile() {
      try {
         this.out.close();
      } catch (Exception var2) {
         var2.printStackTrace();
      }

   }
}
