package bc19;

public class Directions {
   public static final Tuple[] dirs = new Tuple[] { new Tuple(0, -1), new Tuple(1, -1), 
            new Tuple(1, 0), new Tuple(1, 1), new Tuple(0, 1), new Tuple(-1, 1), 
            new Tuple(-1, 0), new Tuple(-1, -1) }; 
   
   public static final Tuple[] largeDirs = new Tuple[] {
            new Tuple(0, -2), new Tuple(2, 0), new Tuple(0, 2), new Tuple(-2, 0)
   };
   
   public static final int NORTH = 0;
   public static final int NORTH_EAST = 1;
   public static final int EAST = 2;
   public static final int SOUTH_EAST = 3;
   public static final int SOUTH = 4;
   public static final int SOUTH_WEST = 5;
   public static final int WEST = 6;
   public static final int NORTH_WEST = 7;
   
   /**
    * Orders the directions by how close they are to direction
    * orderDirs(NORTH) returns
    * [NORTH, NORTH_EAST, NORTH_WEST, EAST, WEST, SOUTH_EAST, SOUTH_WEST, SOUTH]
    * @param direction
    *    The specified direction by which to order the directions
    * @return
    *    The reordered directions
    */
   public static Tuple[] orderDirs(int direction) {
      Tuple[] orderedDirs = new Tuple[8];
      
      for (int i = 0; i < 8; i++) {
         // if odd, add, if even subtract
         int diff = (i % 2 == 1) ? 1 : -1;
         
         // odd and even numbers have same diff magnitude but opposite signs
         diff *= (i + 1) / 2;
         
         // do the + 8 to prevent negative numbers during mod
         orderedDirs[i] = dirs[(direction + diff + 8) % 8];
      }
      
      return orderedDirs;
   }
   
   public static Tuple[] orderSpeedyDirs(int direction) {
      Tuple[] temp = orderDirs(direction);
      Tuple[] returnDirs = null;
      int i = 0;
      
      switch (direction) {
         case 0:
            returnDirs = new Tuple[9];
            returnDirs[i] = largeDirs[0];
            break;
         case 1:
            returnDirs = new Tuple[10];
            for(i = 0; i < 2; i++) {
               returnDirs[i] = largeDirs[i];
            }
            break;
         case 2:
            returnDirs = new Tuple[9];
            returnDirs[i] = largeDirs[1];
            break;
         case 3:
            returnDirs = new Tuple[10];
            for(i = 0; i < 2; i++) {
               returnDirs[i] = largeDirs[i + 1];
            }
            break;
         case 4:
            returnDirs = new Tuple[9];
            returnDirs[i] = largeDirs[2];
            break;
         case 5:
            returnDirs = new Tuple[10];
            for(i = 0; i < 2; i++) {
               returnDirs[i] = largeDirs[i + 2];
            }
            break;
         case 6:
            returnDirs = new Tuple[9];
            returnDirs[i] = largeDirs[3];
            break;
         case 7:
            returnDirs = new Tuple[10];
            for(i = 0; i < 2; i++) {
               returnDirs[i] = largeDirs[(i + 3) % 4];
            }
            break;
      }
      
      for (int j = 0; j < temp.length; j++) {
         returnDirs[i++] = temp[j];
      }
      
      return returnDirs;
   } 
   
   /**
    * Returns the direction of end relative to start
    */
   public static int dirTowards(Tuple start, Tuple end) {
      int xDiff = end.x - start.x;
      int yDiff = end.y - start.y;
      
      if (xDiff > 0) xDiff = 1;
      else if (xDiff < 0) xDiff = -1;
      
      if (yDiff > 0) yDiff = 1;
      else if (yDiff < 0) yDiff = -1;
      
      Tuple directionTuple = new Tuple(xDiff, yDiff);
      for (int i = 0; i < dirs.length; i++) {
         if (directionTuple.equals(dirs[i])) {
            return i;
         }
      }
      
      return -1;
   }
   
   public static int tupleToDir(Tuple tup) {
      int xDiff = tup.x;
      int yDiff = tup.y;
      
      if (xDiff > 0) xDiff = 1;
      else if (xDiff < 0) xDiff = -1;
      
      if (yDiff > 0) yDiff = 1;
      else if (yDiff < 0) yDiff = -1;
      
      Tuple directionTuple = new Tuple(xDiff, yDiff);
      for (int i = 0; i < dirs.length; i++) {
         if (directionTuple.equals(dirs[i])) {
            return i;
         }
      }
      
      return -1;
   }
   
   public static int getPreviousDir(int dir) {
      return (dir - 1 + 8) % 8;
   }
}