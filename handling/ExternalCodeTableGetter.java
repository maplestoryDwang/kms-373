package handling;

import java.util.Properties;

public class ExternalCodeTableGetter {
   final Properties props;

   public ExternalCodeTableGetter(Properties properties) {
      this.props = properties;
   }

   private static final <T extends Enum<? extends WritableIntValueHolder>> T valueOf(String name, T[] values) {
      Enum[] var2 = values;
      int var3 = values.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         T val = var2[var4];
         if (val.name().equals(name)) {
            return val;
         }
      }

      return null;
   }

   private final <T extends Enum<? extends WritableIntValueHolder>> short getValue(String name, T[] values, short def) {
      String prop = this.props.getProperty(name);
      if (prop != null && prop.length() > 0) {
         String trimmed = prop.trim();
         String[] args = trimmed.split(" ");
         short base = 0;
         String offset;
         if (args.length == 2) {
            base = ((WritableIntValueHolder)valueOf(args[0], values)).getValue();
            if (base == def) {
               base = this.getValue(args[0], values, def);
            }

            offset = args[1];
         } else {
            offset = args[0];
         }

         return offset.length() > 2 && offset.substring(0, 2).equals("0x") ? (short)(Short.parseShort(offset.substring(2), 16) + base) : (short)(Short.parseShort(offset) + base);
      } else {
         return def;
      }
   }

   public static final <T extends Enum<? extends WritableIntValueHolder>> void populateValues(Properties properties, T[] values) {
      ExternalCodeTableGetter exc = new ExternalCodeTableGetter(properties);
      Enum[] var3 = values;
      int var4 = values.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         T code = var3[var5];
         ((WritableIntValueHolder)code).setValue(exc.getValue(code.name(), values, (short)-2));
      }

   }
}
