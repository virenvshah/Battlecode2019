package bc19;

public class FourProphetRush {
   public static Action run(MyRobot myRobot) {
      if (myRobot.me.unit == myRobot.SPECS.CASTLE) {
         return castle(myRobot);
      } else if (myRobot.me.unit == myRobot.SPECS.PROPHET) {
         return prophet(myRobot);
      } else {
         return null;
      }
   }
   
   public static Action castle(MyRobot myRobot) {
      myRobot.log("Turn " + myRobot.turn);
      if (myRobot.turn == 1) {
         Castle.initialize(myRobot);
      }
      
      // get ally castle locations
      if (myRobot.turn <= 3) {
         Castle.findCastleLocations(myRobot);
      } 
      
      if (myRobot.turn == 4) {

      }
      
      if (myRobot.turn == 4 && myRobot.castleNumber == 1) {
         myRobot.log("Other enemy size is " + myRobot.otherEnemyCastleLocs.size());
         if (myRobot.otherEnemyCastleLocs.size() == 0) {
            myRobot.signal(59999, 2);
         } else if (myRobot.otherEnemyCastleLocs.size() == 1) {
            Tuple enemyCastle = myRobot.otherEnemyCastleLocs.get(0);
            myRobot.signal(50000 + enemyCastle.x * 100 + enemyCastle.y, 2);
         } else {
            Tuple enemyCastle = myRobot.otherEnemyCastleLocs.get(0);
            myRobot.signal(40000 + enemyCastle.x * 100 + enemyCastle.y, 2);
         }
      }
      
      if (myRobot.turn <= 4 && myRobot.castleNumber == 1) {
         BuildAction prophet = 
                  myRobot.buildRobo(myRobot.SPECS.PROPHET, myRobot.enemyDirection);
         if (prophet != null) {
            return prophet;
         }
      }
      
      if (myRobot.turn == 5 && myRobot.castleNumber == 1) {
         if (myRobot.otherEnemyCastleLocs.size() == 2) {
            Tuple enemyCastle = myRobot.otherEnemyCastleLocs.get(1);
            myRobot.signal(50000 + enemyCastle.x * 100 + enemyCastle.y, 2);
         }
      }
      
      
      return null;
   }
   
   public static Action prophet(MyRobot myRobot) {
      Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);
      
      if (myRobot.turn == 1) {
         Prophet.initialize(myRobot);
      }
      
      
      // at all turns the first thing to do is see if there are enemies
      // close by so that you can attack them
      Action action = Prophet.kite(myRobot);
      if (action != null) {
         return action;
      }
      
      
      // castle will only give you signals during the first 6 turns
      if (!myRobot.moveMode) {
         Prophet.reactSignalRush(myRobot);
      }
      // charge towards enemy castles
      else {
         // if the castle is visible, check if it is destroyed
         if (MapFunctions.radiusDistance(myLoc, myRobot.enemyCastleLoc) <= 64) {
            // if it is destroyed, change enemyCastleLoc to the next enemy castle
            // location
            Robot r = GeneralUnit.getUnitAtLoc(myRobot, myRobot.enemyCastleLoc);
            // castle destroyed
            if (r == null || r.unit != myRobot.SPECS.CASTLE) {
               myRobot.log("Need to change enemy castle location");
               for (Tuple t : myRobot.otherEnemyCastleLocs) {
                  myRobot.log(t.toString());
               }
               myRobot.enemyCastleLoc = myRobot.otherEnemyCastleLocs.get(0);
               // for some reason remove(index) doesn't seem to transpile to js
               myRobot.otherEnemyCastleLocs.remove(myRobot.enemyCastleLoc);
            }
         }
         
         Tuple tup = MapFunctions.moveTo(myRobot, myRobot.enemyCastleLoc, 2, 0);
         if (tup != null) {
            return myRobot.move(tup.x, tup.y);
         }
      }
      
      return null;
   }
}