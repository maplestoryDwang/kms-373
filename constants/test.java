package constants;

public class test {
   public static void main(String[] args) {
      int buffstat = 67108864;
      int pos = 3;

      for(int flag = 0; flag < 999; ++flag) {
         if (1 << 31 - flag % 32 == buffstat && pos == (byte)((int)Math.floor((double)(flag / 32)))) {
            System.out.println(flag);
         }

         if (1 << 31 - flag % 32 == buffstat && pos == (byte)((int)(4.0D - Math.floor((double)(flag / 32))))) {
            System.out.println("mob " + flag);
         }
      }

   }
}
