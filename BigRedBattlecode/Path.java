package bc19;

public class Path {
   public Tuple[] tupleList;
   public int index;
   // used for peek
   public int nextIndex;
   public int radialMovementDist;
   
   Path(Tuple[] list, int rDistance) {
      index = 0;
      tupleList = list;
      // the radialDistance >= tupleList.length because moving diagonally
      // is a distance of 2 not 1
      radialMovementDist = rDistance;
   }
   
   public Tuple nextLocation(int steps) {
      int s = steps;
      // Make sure the index+steps position is within
      // movement range
      while (index + s >= tupleList.length || 
               MapFunctions.radiusDistance(tupleList[index+s], tupleList[index])
               > stepsToRadius(steps)) {
         s--;
      }
      
      if (s == 0) return null;
      else {
         nextIndex = index = index + s;
         return tupleList[index];
      }
   }
   
   public Tuple destination() {
      return tupleList[tupleList.length-1];
   }

   public Tuple peekLocation(int steps) {
      int s = steps;
      while (index + s >= tupleList.length || 
               MapFunctions.radiusDistance(tupleList[index+s], tupleList[index])
               > stepsToRadius(steps)) {
         s--;
      }
      
      if (s == 0) return null;
      else {
         nextIndex = index + s;
         return tupleList[nextIndex];
      }
   }
   
   public int stepsToRadius(int steps) {
      if (steps == 1) {
         return 2;
      } else {
         return steps * steps;
      }
   }
   
   /** 
    * Add the prepend path to the beginning of this path[newStartInd....length-1]
    */
   public void prependPath(Path prepend, int newStartInd) {
      Tuple[] tempList = new Tuple[prepend.tupleList.length + 
                                   tupleList.length - newStartInd];
      int i;
      for (i = 0; i < prepend.tupleList.length; i++) {
         tempList[i] = prepend.tupleList[i];
      }
      
      // copy the remaining part of the original path
      for(int j = newStartInd; j < tupleList.length; j++) {
         tempList[i++] = tupleList[j];
      }
      
      tupleList = tempList;
      index = 0;
   }
   
   public String toString() {
      String msg = "";
      for (Tuple t : tupleList) {
         msg += t.toString();
      }
      
      return msg;
   }
}
