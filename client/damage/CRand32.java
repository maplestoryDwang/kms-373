package client.damage;

import server.Randomizer;

public class CRand32 {
   private long seed1;
   private long seed2;
   private long seed3;
   private long oldSeed1;
   private long oldSeed2;
   private long oldSeed3;

   public CRand32() {
      int randInt = Randomizer.nextInt();
      this.Seed(randInt, randInt, randInt);
   }

   public long Random() {
      long seed1 = this.seed1;
      long seed2 = this.seed2;
      long seed3 = this.seed3;
      this.oldSeed1 = seed1;
      this.oldSeed2 = seed2;
      this.oldSeed3 = seed3;
      long newSeed1 = seed1 << 12 ^ seed1 >> 19 ^ (seed1 >> 6 ^ seed1 << 12) & 8191L;
      long newSeed2 = 16L * seed2 ^ seed2 >> 25 ^ (16L * seed2 ^ seed2 >> 23) & 127L;
      long newSeed3 = seed3 >> 11 ^ seed3 << 17 ^ (seed3 >> 8 ^ seed3 << 17) & 2097151L;
      this.seed1 = newSeed1;
      this.seed2 = newSeed2;
      this.seed3 = newSeed3;
      return (newSeed1 ^ newSeed2 ^ newSeed3) & 4294967295L;
   }

   public void Seed(int s1, int s2, int s3) {
      this.seed1 = (long)(s1 | 1048576);
      this.oldSeed1 = (long)(s1 | 1048576);
      this.seed2 = (long)(s2 | 4096);
      this.oldSeed2 = (long)(s2 | 4096);
      this.seed3 = (long)(s3 | 16);
      this.oldSeed3 = (long)(s3 | 16);
   }
}
