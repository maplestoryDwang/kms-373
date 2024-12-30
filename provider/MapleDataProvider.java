package provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MapleDataProvider {
   private File root;
   private MapleDataDirectoryEntry rootForNavigation;

   public MapleDataProvider(File fileIn) {
      this.root = fileIn;
      this.rootForNavigation = new MapleDataDirectoryEntry(fileIn.getName(), 0, 0, (MapleDataEntity)null);
      this.fillMapleDataEntitys(this.root, this.rootForNavigation);
   }

   private void fillMapleDataEntitys(File lroot, MapleDataDirectoryEntry wzdir) {
      File[] var3 = lroot.listFiles();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         File file = var3[var5];
         String fileName = file.getName();
         if (file.isDirectory() && !fileName.endsWith(".img")) {
            MapleDataDirectoryEntry newDir = new MapleDataDirectoryEntry(fileName, 0, 0, wzdir);
            wzdir.addDirectory(newDir);
            this.fillMapleDataEntitys(file, newDir);
         } else if (fileName.endsWith(".xml")) {
            wzdir.addFile(new MapleDataFileEntry(fileName.substring(0, fileName.length() - 4), 0, 0, wzdir));
         }
      }

   }

   public MapleData getData(String path) {
      File dataFile = new File(this.root, path + ".xml");
      File imageDataDir = new File(this.root, path);

      FileInputStream fis;
      try {
         fis = new FileInputStream(dataFile);
      } catch (FileNotFoundException var15) {
         throw new RuntimeException("Datafile " + path + " does not exist in " + this.root.getAbsolutePath());
      }

      MapleData domMapleData;
      try {
         domMapleData = new MapleData(fis, imageDataDir.getParentFile());
      } finally {
         try {
            fis.close();
         } catch (IOException var13) {
            throw new RuntimeException(var13);
         }
      }

      return domMapleData;
   }

   public MapleDataDirectoryEntry getRoot() {
      return this.rootForNavigation;
   }
}
