package bc19;

import java.util.ArrayList;

public class Viren {
   public static Action run(MyRobot myRobot) {
      if (myRobot.me.unit == myRobot.SPECS.CASTLE) {
         return castle(myRobot);
      } else if (myRobot.me.unit == myRobot.SPECS.PILGRIM) {
         return pilgrim(myRobot);
      } else if (myRobot.me.unit == myRobot.SPECS.PROPHET) {
         return prophet(myRobot); 
      } else if (myRobot.me.unit == myRobot.SPECS.CRUSADER) {
         return crusader(myRobot);
      } else if (myRobot.me.unit == myRobot.SPECS.CHURCH) {
         return church(myRobot);
      } else {
         return null;
      }
   }
   
   public static Action castle(MyRobot myRobot) {
      myRobot.log("Turn " + myRobot.turn);
      if (myRobot.turn == 1) {
         Castle.initialize(myRobot);
      }
      
      if (myRobot.turn <= 3) {
         Castle.findCastleLocations(myRobot);
      }  
      
      if (myRobot.turn % 10 == 0) {
         Castle.adjustKarbLimit(myRobot, 75);
      }
      
      if (myRobot.fuel > 1000) {
         myRobot.fuelLimit = 750;
      }
      
      if (myRobot.karbonite >= 15) {
         BuildAction troop = Castle.produceEmergencyTroops(myRobot, myRobot.SPECS.CRUSADER);
         if (troop != null) {
            return troop;
         }
      }
      
      AttackAction attack = GeneralUnit.attackClosestEnemy(myRobot);
      if (attack != null) {
         return attack;
      }

      // only produce if you have more Karbonite than karbLimit
      if (myRobot.karbonite > myRobot.karbLimit) {
         BuildAction unit = null;

         // produce a worker
         if (!(myRobot.unitsProduced % 4 == 3)) {
            // number of ally castles
            int numCastles = myRobot.allyCastleLocs.size() + 1;

            // only produce pilgrims for your side of the map and each castle should
            // share the load of number of pilgrims produced
            int karbPilgrimLimit = (myRobot.numKarbo / (numCastles * 2));
            int fuelPilgrimLimit = (myRobot.numKarbo / (numCastles * 2));
            karbPilgrimLimit = (karbPilgrimLimit <= 5) ? karbPilgrimLimit : 5;
            fuelPilgrimLimit = (fuelPilgrimLimit <= 5) ? fuelPilgrimLimit : 5;

            if (myRobot.karbPilgrimsProduced * 2 * numCastles < myRobot.numKarbo
                     // changed from myRobot.pilgrimsProduced because once we finish 
                     // producing fuelPilgrims we don't produce any more karbonite pilgrims
                     && myRobot.turn % 2 == 0) {
               myRobot.karbOverFuel = true;
               unit = Castle.producePilgrim(myRobot);
            }   
            else if (myRobot.fuelPilgrimsProduced * 2 * numCastles < myRobot.numFuel){
               myRobot.karbOverFuel = false;            
               unit = Castle.producePilgrim(myRobot);
            }
         }

         if (unit == null) {
            unit = myRobot.buildRobo(myRobot.SPECS.PROPHET, myRobot.enemyDirection);
            if (myRobot.otherEnemyCastleLocs.size() > 0) {
               Tuple enemyLoc = myRobot.otherEnemyCastleLocs.get(0);
               myRobot.signal(5 * 10000 + enemyLoc.x * 100 + enemyLoc.y, 8);
            } else {
               myRobot.signal(59999, 8);
            }
         } 
   
         if (unit != null) {
            myRobot.unitsProduced++;
            if (myRobot.unitsProduced > 5) {
               Castle.adjustKarbLimit(myRobot, 75);
            }
            return unit;
         }
      }
      
      return null;
   }
   
   
   public static Action church(MyRobot myRobot) {
      if (myRobot.turn == 1) {
         Church.initialize(myRobot);
      }
      
      if (myRobot.turn % 10 == 0) {
         Castle.adjustKarbLimit(myRobot, 85);
      }
      
      if (myRobot.fuel >= 1000) {
         myRobot.fuelLimit = 750;
      }
      
      if (myRobot.karbonite >= 15) {
         BuildAction troop = Castle.produceEmergencyTroops(myRobot, myRobot.SPECS.CRUSADER);
         if (troop != null) {
            return troop;
         }
      }
      
      // only produce if you have more Karbonite than karbLimit
      if (myRobot.karbonite > myRobot.karbLimit) {
         BuildAction unit = myRobot.buildRobo(myRobot.SPECS.PROPHET, 
                  myRobot.enemyDirection);
         myRobot.signal(5 * 10000 + myRobot.castleLoc.x * 100 + 
                     myRobot.castleLoc.y, 8);
         
         if (unit != null) {
            myRobot.log("Churched produced a unit");
            myRobot.unitsProduced++;
            Castle.adjustKarbLimit(myRobot, 85);
            return unit;
         }
      }
      
      return null;
   }
   
   public static Action prophet(MyRobot myRobot) {
      Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);
      myRobot.speed = 1;
      
      if (myRobot.turn == 1) {
         Prophet.initialize(myRobot);
      }
      
      if (myRobot.fuel >= 1000) {
         myRobot.fuelLimit = 750;
      }

      Prophet.reactSignalRush(myRobot);
      
      // move once every 10 turns starting from 1st turn, if you have ally troops with you
      // also move if you are very close to your castle
      // also move if you are on a resource tile
      int[] types = {myRobot.SPECS.PROPHET, myRobot.SPECS.CRUSADER, myRobot.SPECS.PREACHER};
      ArrayList<Robot> allyTroops = GeneralUnit.getRobotsWithinRadius(myRobot, types, 8);
      if ((myRobot.turn % 10 == 1 && allyTroops.size() >= 6) || 
             /*  MapFunctions.radiusDistance(myLoc, myRobot.castleLoc) <= 9 || */
               MapFunctions.radiusDistance(myLoc, myRobot.home) <= 9 ||               
               myRobot.karboniteMap[myLoc.y][myLoc.x] || myRobot.fuelMap[myLoc.y][myLoc.x]) {
         // need to have enough fuel
         if (myRobot.fuel > myRobot.fuelLimit) {
            myRobot.moveMode = true;
         } else {
            myRobot.moveMode = false;
            myRobot.log("Not moving to conserve fuel");
         }
      } else {
         myRobot.moveMode = false;
      }
      
      // first thing to do is check if you can attack
      // myRobot.log("About to give");
      Action action = Prophet.kite(myRobot);
      if (action != null) {
         return action;
      }
      
      // we try and move once every 10 turns
      if (myRobot.moveMode) {
         Tuple tup = MapFunctions.moveTo(myRobot, myRobot.enemyCastleLoc, myRobot.speed, 0);
         if (tup != null) {
            myRobot.moveMode = false;
            return myRobot.move(tup.x, tup.y);
         } else {
            // if the castle is visible, check if it is destroyed
            if (MapFunctions.radiusDistance(myLoc, myRobot.enemyCastleLoc) <= 64) {
               // if it is destroyed, change enemyCastleLoc to the next enemy castle
               // location
               Robot r = GeneralUnit.getUnitAtLoc(myRobot, myRobot.enemyCastleLoc);
               // castle destroyed
               if (r == null || r.unit != myRobot.SPECS.CASTLE) {
                  if (myRobot.otherEnemyCastleLocs.size() > 0) {
                     myRobot.enemyCastleLoc = myRobot.otherEnemyCastleLocs.get(0);
                     // for some reason remove(index) doesn't seem to transpile to js
                     myRobot.otherEnemyCastleLocs.remove(myRobot.enemyCastleLoc);
                  }
               }
            }
         }
      }
      
      // if nothing else see if you have Karbonite / fuel to give to castle
      GiveAction giveResource = GeneralUnit.giveResourceTowardsCastle(myRobot);
      if (giveResource != null) {
         return giveResource;
      }
      
      return null;
   }
   
   public static Action crusader(MyRobot myRobot) {     
      if (myRobot.turn == 1) {
         Crusader.initialize(myRobot);
      }
      
      Crusader.reactToCastleChurchSignal(myRobot);
      
      Action action = Crusader.attackClosestEnemy(myRobot);
      if (action != null) {
         return action;
      }
      
      if (myRobot.attackMode) {
         Tuple tup = MapFunctions.moveTo(myRobot, myRobot.closestEnemyLoc, 3, 0); 
         if (tup != null) {
            return myRobot.move(tup.x, tup.y);
         } else {
            myRobot.attackMode = false;
         }
      }

      return null;
   }
   
   
   public static Action pilgrim(MyRobot myRobot) {
      Tuple myLoc = new Tuple(myRobot.me.x, myRobot.me.y);

      if (myRobot.turn == 1) {
         Pilgrim.initialize(myRobot);
      }
      
      
      // detect enemies
      Robot enemyRob = Combat.getClosestCombatEnemy(myRobot);
      if (enemyRob != null) {
         Tuple enemyLoc = new Tuple(enemyRob.x, enemyRob.y);
         // if enemy is within 2 * its attack radius, then start running away
         if (MapFunctions.radiusDistance(enemyLoc, myLoc) <= 
                  1.3 * myRobot.SPECS.UNITS[enemyRob.unit].VISION_RADIUS + 16.8) {
            myRobot.log("PILGRIM " + (1.3 * myRobot.SPECS.UNITS[enemyRob.unit].VISION_RADIUS + 16.8));
            Tuple randMove = myRobot.randomMove(myLoc, Directions.dirTowards(enemyLoc, myLoc));
            if (randMove != null) {
               return myRobot.move(randMove.x, randMove.y);
            }
         }
      }
      
      // look for a structure close to the mining location if there exists one
      if (myRobot.closestNode.location.equals(myLoc) && !myRobot.closeToBase) {
         int[] types = {myRobot.SPECS.CASTLE, myRobot.SPECS.CHURCH};
         ArrayList<Robot> temp = GeneralUnit.
                  getRobotsWithinRadius(myRobot, types, 25);
         ArrayList<Robot> structures = new ArrayList<Robot>();
         for (int i = 0; i < temp.size(); i++) {
            Robot r = temp.get(i);
            if (r.team == myRobot.me.team) {
               structures.add(r);
            }
         }
         if (structures.size() > 0) {
            Robot homeStruct = structures.get(0);
            myRobot.home = new Tuple(homeStruct.x, homeStruct.y);
            myRobot.closeToBase = true;
         }
      }
      
      if (myRobot.me.karbonite >= myRobot.karbCapacity || myRobot.me.fuel >= 100) {
         // if adjacent structure exists, give it Karbonite or Fuel when you fill up
         Robot closestStruct = Pilgrim.getAdjacentStructure(myRobot);
         if (closestStruct != null) {
            return myRobot.give(closestStruct.x - myLoc.x, closestStruct.y - myLoc.y,
                     myRobot.me.karbonite, myRobot.me.fuel);
         }
      }
      
      // if you are on your mining location,
      // have enough karbonite and you're far from your base, build a church
      if (myRobot.closestNode.location.equals(myLoc) && myRobot.karbonite >= 50
               && !myRobot.closeToBase) {
         // build a nearby church if you don't see one already
         // try building the church on a non-resource tile
         int direction = -1;
         for (Tuple t : Directions.dirs) {
            if (!myRobot.karboniteMap[myLoc.y + t.y][myLoc.x + t.x] && 
                     !myRobot.fuelMap[myLoc.y + t.y][myLoc.x + t.x]) {
               direction = Directions.tupleToDir(t);
            }
         }
         BuildAction church = myRobot.buildRobo(myRobot.SPECS.CHURCH, direction);
         if(church != null) {
            myRobot.closeToBase = true;
            myRobot.log("Building a church");
            myRobot.signal(4 * 10000 + 100 * myRobot.castleLoc.x + myRobot.castleLoc.y, 2);
            return church;
         }
      }
      
      // if you have not filled up on karbonite or Fuel go to the closest mining location
      if (!myRobot.closestNode.location.equals(myLoc) && 
               (myRobot.me.karbonite < myRobot.karbCapacity && myRobot.me.fuel < 100)) {
         Tuple tup = MapFunctions.moveTo(myRobot, myRobot.closestNode.location, 2, 0);
         if (tup != null) {
            return myRobot.move(tup.x, tup.y);
         } else {
            // If there is a pilgrim or a church on your mining location, reassign your mining location
            Robot obstruction = GeneralUnit.getUnitAtLoc(myRobot, myRobot.closestNode.location);
            if (obstruction != null && 
                     (obstruction.unit == myRobot.SPECS.CHURCH || obstruction.unit == myRobot.SPECS.PILGRIM)) {
               int type = myRobot.karboPilgrim ? 1 : 2;
               Pilgrim.reassignMiningLoc(myRobot, type);
            }      
         }
      }
      
      // if capacity is filled up and no structure is visible, go back home to deposit karbonite
      if (!myRobot.home.equals(myLoc) && (myRobot.me.karbonite >= myRobot.karbCapacity
               || myRobot.me.fuel >= 100)) {
         Tuple tup = MapFunctions.moveTo(myRobot, myRobot.home, 2,0);
         if (tup != null) {
            return myRobot.move(tup.x, tup.y);
         } 
         else {
            // if cannot go home (probably because of a unit obstruction,  
            // see if you have Karbonite / fuel to give to castle, and you can give 
            // the Karb / fuel to the unit who will pass it on to the castle
            GiveAction giveResource = GeneralUnit.giveResourceTowardsCastle(myRobot);
            if (giveResource != null) {
               return giveResource;
            }
         }
      }
      
      // if on mining location
      if (myRobot.closestNode.location.equals(myLoc) &&
               (myRobot.me.karbonite < myRobot.karbCapacity && myRobot.me.fuel < 100)) {
         return myRobot.mine(); // if we can't give, mine.
      }
      
      return null;
   }
}