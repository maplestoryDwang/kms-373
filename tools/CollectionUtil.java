package tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CollectionUtil {
   public static <T> List<T> copyFirst(List<T> list, int count) {
      List<T> ret = new ArrayList(list.size() < count ? list.size() : count);
      int i = 0;
      Iterator var4 = list.iterator();

      while(var4.hasNext()) {
         T elem = var4.next();
         ret.add(elem);
         if (i++ > count) {
            break;
         }
      }

      return ret;
   }
}
