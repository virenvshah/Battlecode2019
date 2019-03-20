package bc19;

public class Church {
   public static void initialize(MyRobot myRobot) {
      Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);
      myRobot.karboDepots = myRobot.getTypedTiles(myRobot.tileNodeMap, 1);
      myRobot.fuelDepots = myRobot.getTypedTiles(myRobot.tileNodeMap, 2);
      myRobot.numKarbo = myRobot.karboDepots.size();
      myRobot.numFuel = myRobot.fuelDepots.size();
      myRobot.karbLimit = 120;
      
      int[] types = {myRobot.SPECS.PILGRIM};
      int signal = GeneralUnit.getSignalFrom(myRobot, types);
      myRobot.log("Signal for church from pilgrim is " + signal);
      
      
      
      if (signal / 10000 <= 2) {
         myRobot.castleLoc = new Tuple((signal % 10000) / 100, signal % 100);
         myRobot.enemyCastleLoc = myRobot.getEnemyLoc(myRobot.castleLoc,
                  myRobot.karboDepots);
         myRobot.log("CHURCH ememy castle location is " + myRobot.enemyCastleLoc.toString());
         myRobot.enemyDirection = Directions.dirTowards(myRobot.castleLoc, 
                  myRobot.enemyCastleLoc);
         // in the beginning its the only enemy we know of
         myRobot.closestEnemyLoc = myRobot.enemyCastleLoc;  
         if (signal / 10000 == 2) {
            myRobot.closeToEnemyMine = true;
            myRobot.karbLimit = 75;
         } else {
            myRobot.closeToEnemyMine = false;
            myRobot.karbLimit = 120;
         }
      }
      
      for (int i = 0; i < myRobot.karboDepots.size(); i++) {
         TileNode karbTile = myRobot.karboDepots.get(i);
         Robot unit = GeneralUnit.getUnitAtLoc(myRobot, karbTile.location);
         if (MapFunctions.radiusDistance(myLoc, karbTile.location) > 100 ||
                  (myRobot.visibleMap[karbTile.location.y][karbTile.location.x] > 0 &&
                           unit.unit == myRobot.SPECS.PILGRIM && unit.team == myRobot.me.team)) {
            myRobot.karboDepots.remove(karbTile);
            i--;
         }
      }
      
      for (int i = 0; i < myRobot.fuelDepots.size(); i++) {
         TileNode fuelTile = myRobot.fuelDepots.get(i);
         Robot unit = GeneralUnit.getUnitAtLoc(myRobot, fuelTile.location);
         if (MapFunctions.radiusDistance(myLoc, fuelTile.location) > 100 ||
                  // there is already a pilgrim on the tile
                  (myRobot.visibleMap[fuelTile.location.y][fuelTile.location.x] > 0 &&
                           unit.unit == myRobot.SPECS.PILGRIM) && unit.team == myRobot.me.team) {
            myRobot.fuelDepots.remove(fuelTile);
            i--;
         }
      }
   }
   
   public static BuildAction producePilgrim(MyRobot myRobot) {
      Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);
      Tuple closestNode = null;
      if (myRobot.karbOverFuel) {
         myRobot.log("Building a karbonite pilgrim");
         myRobot.log("finding closest karbo depot");

         // find the closest Karbonite deposit to send the pilgrim to
         Path closestNodePath = MapFunctions.bfsGetClosestMiningLoc(myRobot, myRobot.karboDepots);
         if (closestNodePath == null) {
            return null;
         }
         
         closestNode = closestNodePath.destination();
      } else {
         myRobot.log("finding closest fuel depot");
         myRobot.log("Building a fuel pilgrim");

         // find the closest Karbonite deposit to send the pilgrim to
         Path closestNodePath = MapFunctions.bfsGetClosestMiningLoc(myRobot, myRobot.fuelDepots);
         if (closestNodePath == null) {
            return null;
         }
         
         closestNode = closestNodePath.destination();
      }

      if (closestNode != null ) {
         BuildAction nobleBot = myRobot.buildRobo(myRobot.SPECS.PILGRIM, 
                  10 + Directions.dirTowards(myLoc, closestNode));
         if (nobleBot != null) {
            myRobot.closestNode = myRobot.tileNodeMap[closestNode.y][closestNode.x];
            // so that another pilgrim build later on doesn't visit the same depot
            if (myRobot.karbOverFuel) {
               myRobot.karbPilgrimsProduced++;
               myRobot.karboDepots.remove(myRobot.closestNode);
            } else {
               myRobot.fuelPilgrimsProduced++;
               myRobot.fuelDepots.remove(myRobot.closestNode);
            }
            // tell the pilgrim the location of the Karbonite deposit

            myRobot.pilgrimsProduced++;
            myRobot.signal(myRobot.closestNode.location.x * 100 + myRobot.closestNode.location.y, 2);
            return nobleBot;
         }
      }

      return null;
   }

   
   /**
    * Changes the karbLimit so that other castles can also 
    * produce units.  Otherwise the castle that comes first 
    * in the queue order would always end up producing more units
    */
   public static void adjustKarbLimit(MyRobot myRobot) {
      myRobot.karbLimit = (myRobot.karbLimit % 73) + 90;
   }
}