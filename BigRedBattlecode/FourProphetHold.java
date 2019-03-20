package bc19;

public class FourProphetHold {
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


      if (myRobot.turn == 4 && myRobot.castleNumber == 1) {

            Tuple enemyCastle = new Tuple(16,23);
            myRobot.signal(40000 + enemyCastle.x * 100 + enemyCastle.y, 2);

      }

      if (myRobot.turn <= 2 && myRobot.castleNumber == 1) {
         BuildAction prophet =
                  myRobot.buildRobo(myRobot.SPECS.PROPHET, myRobot.enemyDirection);
         if (prophet != null) {
            return prophet;
         }
      }


      return null;
   }

   public static Action prophet(MyRobot myRobot) {
      Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);

      if (myRobot.turn == 1) {
         Prophet.initialize(myRobot);
         myRobot.enemyCastleLoc = new Tuple(16,23);
      }


      // at all turns the first thing to do is see if there are enemies
      // close by so that you can attack them
      Action action = Prophet.kite(myRobot);
      if (action != null) {
         return action;
      }


      // castle will only give you signals during the first 6 turns
      if (!myRobot.moveMode) {
         Prophet.reactSignalHold(myRobot);
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
               myRobot.log("Cleared, now holding");
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
