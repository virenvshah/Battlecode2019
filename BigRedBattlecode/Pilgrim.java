package bc19;

import java.util.ArrayList;

public class Pilgrim {
   public static void initialize(MyRobot myRobot) {
      GeneralUnit.initializeUnit(myRobot);
      
      int[] types = {myRobot.SPECS.CASTLE, myRobot.SPECS.CHURCH};
      int signal = GeneralUnit.getSignalFrom(myRobot, types);

      if (signal >= 0 && signal < 6464) {
         Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);
         // the closest mining location
         myRobot.home = myRobot.castleLoc;
         
         if (myRobot.karboniteMap[myRobot.home.y][myRobot.home.x]) {
            myRobot.karboPilgrim = true;
            myRobot.fuelPilgrim = false;
         } else {
            myRobot.karboPilgrim = false;
            myRobot.fuelPilgrim = true;
         }

         // The castle's signal is a 4 digit number where the first 2 digits
         // are the x-coordinate, and the last 2 are the y-coordinate
         Tuple closestNode = new Tuple(signal / 100, signal % 100);
         myRobot.closestNode = myRobot.tileNodeMap[closestNode.y][closestNode.x];
         
         if (MapFunctions.radiusDistance(closestNode, myLoc) <= 5) {
            myRobot.closeToBase = true;
            myRobot.karbCapacity = 10;
         } else {
            myRobot.karbCapacity = 20;
         }
         myRobot.log("Closest mining location is " + closestNode.toString());
      }
   }
   
   /**
    * Returns a castle or a church if an adjacent structure exists
    */
   public static Robot getAdjacentStructure(MyRobot myRobot) {
      Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);
      for (Robot r : myRobot.visibleRobots) {
         Tuple rLoc = new Tuple(r.x, r.y);
         if ((r.unit == myRobot.SPECS.CASTLE || r.unit == myRobot.SPECS.CHURCH) &&
                r.x >= 0 && MapFunctions.radiusDistance(myLoc, rLoc) <= 2) {
            return r;
         }
      }
      
      return null;
   }
   
   /**
    * Reassigns the pilgrim's mining location, type = 1 for karbonite, 2 for fuel
    */
   public static void reassignMiningLoc(MyRobot myRobot, int type) {
      ArrayList<TileNode> locationList;
      myRobot.closeToBase = false;
      if (type == 1) {
         if (myRobot.homeKarboDepots.size() > 0) {
            locationList = myRobot.homeKarboDepots;
         } else {
            locationList = myRobot.karboDepots;
         }
      } else {
         if (myRobot.homeFuelDepots.size() > 0) {
            locationList = myRobot.homeFuelDepots;
         } else {
            locationList = myRobot.fuelDepots;
         }
      }
      
      if (myRobot.closestNode != null) {
         if (type == 1) {      
            myRobot.karboDepots.remove(myRobot.closestNode);
            myRobot.homeKarboDepots.remove(myRobot.closestNode);
         } else {
            myRobot.fuelDepots.remove(myRobot.closestNode);
            myRobot.homeFuelDepots.remove(myRobot.closestNode);
         }
      }
      
      Path closestPath = MapFunctions.bfsGetClosestMiningLoc(myRobot, locationList);
      Tuple closestNode = closestPath.destination();
      myRobot.closestNode = myRobot.tileNodeMap[closestNode.y][closestNode.x];
      myRobot.currentPath = closestPath;
   }
}