package provider;

import java.awt.Point;
import java.awt.image.BufferedImage;

public class MapleDataTool {
   public static String getString(MapleData data) {
      return data.getType() == MapleDataType.INT ? String.valueOf(getInt(data)) : (String)data.getData();
   }

   public static String getString(MapleData data, String def) {
      if (data != null && data.getData() != null) {
         return data.getType() != MapleDataType.STRING && !(data.getData() instanceof String) ? String.valueOf(getInt(data)) : (String)data.getData();
      } else {
         return def;
      }
   }

   public static String getString(String path, MapleData data) {
      return getString(data.getChildByPath(path));
   }

   public static String getString(String path, MapleData data, String def) {
      return getString(data != null && data.getChildByPath(path) != null ? data.getChildByPath(path) : null, def);
   }

   public static double getDouble(MapleData data) {
      return (Double)data.getData();
   }

   public static double getDouble(MapleData data, double def) {
      if (data != null && data.getData() != null) {
         if (data.getType() == MapleDataType.STRING) {
            return Double.parseDouble(getString(data));
         } else if (data.getType() == MapleDataType.SHORT) {
            return Double.valueOf((double)(Short)data.getData());
         } else if (data.getType() == MapleDataType.LONG) {
            return (double)(Long)data.getData();
         } else {
            double buffer2 = (Double)data.getData();
            return buffer2;
         }
      } else {
         return def;
      }
   }

   public static double getDouble(String path, MapleData data, double def) {
      return data == null ? def : getDouble(data.getChildByPath(path), def);
   }

   public static float getFloat(MapleData data) {
      return (Float)data.getData();
   }

   public static float getFloat(MapleData data, float def) {
      return data != null && data.getData() != null ? (Float)data.getData() : def;
   }

   public static int getInt(MapleData data) {
      if (data.getType() == MapleDataType.STRING) {
         return Integer.parseInt(getString(data));
      } else if (data.getType() == MapleDataType.SHORT) {
         return Integer.valueOf((Short)data.getData());
      } else if (data.getType() == MapleDataType.LONG) {
         return (int)(Long)data.getData();
      } else {
         int buffer2 = (Integer)data.getData();
         return buffer2;
      }
   }

   public static int getInt(MapleData data, int def) {
      if (data != null && data.getData() != null) {
         if (data.getType() == MapleDataType.STRING) {
            return Integer.parseInt(getString(data));
         } else if (data.getType() == MapleDataType.SHORT) {
            return Integer.valueOf((Short)data.getData());
         } else if (data.getType() == MapleDataType.LONG) {
            return (int)(Long)data.getData();
         } else {
            int buffer2 = (Integer)data.getData();
            return buffer2;
         }
      } else {
         return def;
      }
   }

   public static long getLong(MapleData data, int def) {
      if (data != null && data.getData() != null) {
         if (data.getType() == MapleDataType.STRING) {
            return Long.parseLong(getString(data));
         } else if (data.getType() == MapleDataType.SHORT) {
            return Long.valueOf((long)(Short)data.getData());
         } else if (data.getType() == MapleDataType.INT) {
            return Long.valueOf((long)((Short)data.getData()).intValue());
         } else {
            Long buffer = (Long)data.getData();
            Long buffer2 = (Long)data.getData();
            return buffer2;
         }
      } else {
         return (long)def;
      }
   }

   public static int getInt(String path, MapleData data) {
      return getInt(data.getChildByPath(path));
   }

   public static int getIntConvert(MapleData data) {
      return data.getType() == MapleDataType.STRING ? Integer.parseInt(getString(data)) : getInt(data);
   }

   public static int getIntConvert(String path, MapleData data) {
      MapleData d = data.getChildByPath(path);
      return d.getType() == MapleDataType.STRING ? Integer.parseInt(getString(d)) : getInt(d);
   }

   public static int getInt(String path, MapleData data, int def) {
      return data == null ? def : getInt(data.getChildByPath(path), def);
   }

   public static int getIntConvert(String path, MapleData data, int def) {
      return data == null ? def : getIntConvert(data.getChildByPath(path), def);
   }

   public static long getLongConvert(String path, MapleData data, int def) {
      return data == null ? (long)def : getLongConvert(data.getChildByPath(path), def);
   }

   public static int getIntConvert(MapleData d, int def) {
      if (d == null) {
         return def;
      } else {
         try {
            if (d.getType() == MapleDataType.STRING) {
               String dd = getString(d);
               if (dd.endsWith("%")) {
                  dd = dd.substring(0, dd.length() - 1);
               }

               try {
                  return Integer.parseInt(dd);
               } catch (NumberFormatException var4) {
                  return def;
               }
            } else {
               return getInt(d, def);
            }
         } catch (Exception var5) {
            var5.printStackTrace();
            return 0;
         }
      }
   }

   public static long getLongConvert(MapleData d, int def) {
      if (d == null) {
         return (long)def;
      } else if (d.getType() == MapleDataType.STRING) {
         String dd = getString(d);
         if (dd.endsWith("%")) {
            dd = dd.substring(0, dd.length() - 1);
         }

         try {
            return Long.parseLong(dd);
         } catch (NumberFormatException var4) {
            return (long)def;
         }
      } else {
         return getLong(d, def);
      }
   }

   public static BufferedImage getImage(MapleData data) {
      return ((MapleCanvas)data.getData()).getImage();
   }

   public static Point getPoint(MapleData data) {
      return (Point)data.getData();
   }

   public static Point getPoint(String path, MapleData data) {
      return getPoint(data.getChildByPath(path));
   }

   public static Point getPoint(String path, MapleData data, Point def) {
      MapleData pointData = data.getChildByPath(path);
      return pointData == null ? def : getPoint(pointData);
   }

   public static String getFullDataPath(MapleData data) {
      String path = "";

      for(Object myData = data; myData != null; myData = ((MapleDataEntity)myData).getParent()) {
         String var10000 = ((MapleDataEntity)myData).getName();
         path = var10000 + "/" + path;
      }

      return path.substring(0, path.length() - 1);
   }
}
