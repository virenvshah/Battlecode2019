package bc19;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MyRobot extends BCAbstractRobot {
   int turn = 0;
   boolean horizRefl = false;
   TileNode[][] tileNodeMap = null;
   int fuelPilgrimsProduced = 0;
   int karbPilgrimsProduced = 0;
   int unitsProduced = 0;
   int pilgrimsProduced = 0;
   int preachersProduced = 0;
   int prophetsProduced = 0;
   TileNode[][] tileNodeVisibleMap = null;
   int[][] visibleMap = null;
   Robot[] visibleRobots = null;
   ArrayList<TileNode> karboDepots = null;
   ArrayList<TileNode> homeKarboDepots = null;
   ArrayList<TileNode> enemyKarboDepots = null;
   ArrayList<TileNode> fuelDepots = null;
   ArrayList<TileNode> homeFuelDepots = null;
   ArrayList<TileNode> enemyFuelDepots = null;
   boolean fuelPilgrim = false;
   boolean karboPilgrim = false;
   boolean karbOverFuel = true;
   int mineRole = 0;
   Path currentPath = null;
   Tuple structLoc = null;
   Tuple castleLoc = null;
   HashSet<Tuple> otherCastleLocs = null;
   HashMap<Integer,Integer> otherCastleLocsX = null; // this and the one below are ID to coordinate
   HashMap<Integer,Integer> otherCastleLocsY = null;
   Tuple otherCastleLoc = null;
   Tuple enemyCastleLoc = null;
   Tuple nextEnemyLoc = null;
   TileNode closestNode = null;
   Tuple home = null;
   int karbLimit = 0;
   int fuelLimit = 200;
   boolean attackMode = false;
   boolean moveMode = false;
   boolean rushMode = false;
   int speed = 1;
   int enemyDirection = -1; // for castles
   Tuple closestEnemyLoc;
   double allyTroopsToProduce = 0;
   // whether there is an enemy in the vicinity
   boolean alert = false;
   boolean moveModeProphet = false;
   int moveModeTimes = 0;
   boolean attackCounter = false;
   boolean closeToBase = false;
   boolean closeToEnemyMine = false;
   int karbCapacity = 20;
   int fuelCapacity = 100;
   int numKarbo = 15;
   int castleNumber = -1;
   ArrayList<Integer> allyCastleIds = new ArrayList<Integer>();
   ArrayList<Tuple> allyCastleLocs = new ArrayList<Tuple>();
   ArrayList<Tuple> otherEnemyCastleLocs = new ArrayList<Tuple>();
   int numFuel = 15;
   int baseKarb;
   int castleNum = -1;
   boolean pilgrimSentEnemyX = false;
   boolean pilgrimSentEnemyY = false;
   int castleRequestX = -1;
   int castleRequestY = -1;
   int requestProphetsMade = 0;
   Tuple pilgrimRescueLoc = new Tuple(-1, -1);
   int rescueTroops = 0;
   ArrayList<Integer> alreadyRescued = new ArrayList<Integer>();

   public Action turn() {
      try {
         turn++;

         // create the initial map for each unit
         if (tileNodeMap == null) {
            MapFunctions.makeMap(this);
         }

         // update every turn
         visibleRobots = getVisibleRobots();
         visibleMap = getVisibleRobotMap();

         return VirenTwo.run(this);

      } catch (Exception e) {
         log("Turn " + me.turn + " Robot type " + me.unit +
                  " Location " + new Tuple(me.x, me.y).toString() +
                  " Exception thrown: " + e.toString());
         if (currentPath != null) {
            String msg = "";
            for (Tuple t : currentPath.tupleList) {
               msg += t.toString();
            }
            log(msg);
         }
      }
      return null;
   }


   /**
    * Returns a tuple defining the enemies location
    * @param: castleLoc
    *       a friendly castle location
    * @param: karboniteLocs
    *       all karbonite depot locations
    */
   public Tuple getEnemyLoc(Tuple castleLoc,ArrayList<TileNode> karboniteLocs) {
      Tuple karbToCheck;
      
      // finds the map's reflection axis by checking karbonite locations
      horizRefl = true;
      for (int i = 0; i < 5; i++) {
         karbToCheck = karboniteLocs.get((int) Math.floor(Math.random() * karboniteLocs.size())).location;
         if(!karboniteMap[karbToCheck.y][karboniteMap[0].length - karbToCheck.x - 1]) {
            log(karbToCheck.toString() + " "  + new Tuple(karboniteMap[0].length - karbToCheck.x - 1, karbToCheck.y).toString());
            horizRefl = false;
         }
      }
      if (horizRefl) {
         return new Tuple(map[0].length - castleLoc.x - 1, castleLoc.y);
      } else {
         return new Tuple(castleLoc.x, map.length - castleLoc.y - 1);
      }
   }

   public Tuple randomMovePreacher(Tuple start, int direction) {
      currentPath = null;
      Tuple[] possibleMoves;
      if (direction >= 0) {
         possibleMoves = Directions.orderDirs(direction);
      } else {
         possibleMoves = Directions.dirs;
      }

      for (int i = 0 ; i < 5; i++) {
         Tuple t = possibleMoves[i];
         int x = t.x + start.x;
         int y = t.y + start.y;
         Tuple dest = new Tuple(x, y);
         if (y >= 0 && y < this.map.length && x >= 0 && x < this.map.length) {
            if (this.map[y][x] && this.visibleMap[y][x] == 0) {
               boolean nearbyTroops = false;
               // make sure there are no adjacent robots
               for (Robot r : visibleRobots) {
                  // if the robot is not visible
                  if (!(r.x >= 0) || r.id == me.id) {
                     continue;
                  }
                  Tuple rLoc = new Tuple(r.x, r.y);
                  // if a robot is detected near t, then don't move to t
                  if (MapFunctions.radiusDistance(rLoc, dest) <= 2) {
                     log("nearby " + dest.toString() + "is " + rLoc.toString());
                     nearbyTroops = true;
                     break;
                  }
               }
               if (!nearbyTroops) {
                  log("Preacher random move success");
                  return t;
               }
            }
         }
      }
      return randomMove(start, direction);
   }

   /**
    * Returns a tuple representing a move one unit in a random direction.
    * @param start
    * @return
    */
   public Tuple randomMove(Tuple start, int direction) {
      if (fuel <= 150) {
         log("Not random moving to conserve fuel");
         return null;
      }
      // not moving on a path anymore
      currentPath = null;
      Tuple[] possibleMoves;
      if (direction >= 0 && direction < 8) {
         possibleMoves = Directions.orderDirs(direction);
      } else if (direction >= 10 && direction < 18) {
         possibleMoves = Directions.orderSpeedyDirs(direction % 10);
      }
      else {
         possibleMoves = Directions.dirs;
      }

      ArrayList<Tuple> unoccupiedTiles = new ArrayList<Tuple>();
      for (Tuple t : possibleMoves) {
         int x = t.x + start.x;
         int y = t.y + start.y;
         if (y >= 0 && y < this.map.length && x >= 0 && x < this.map.length) {
            if (this.map[y][x] && this.visibleMap[y][x] == 0) {
               if (direction >= 0) return t;
               else unoccupiedTiles.add(t);
            }
         }
      }

      int numMoves = unoccupiedTiles.size();
      if (numMoves == 0)
         return null;

      return unoccupiedTiles.get((int) (Math.random() * numMoves)); // change to random when it compiles
   }

   public Tuple diff(Tuple a, Tuple b) {
      return new Tuple(a.x - b.x, a.y - b.y);
   }
   
   
   public Tuple add(Tuple a, Tuple b) {
      return new Tuple(a.x + b.x, a.y + b.y);
   }

    /**
     * Given the integer type of the unit you want to build, build the unit.
     * @precondition:
     *      assumes that your unit is a unit that can build things, and that the target unit you want to build is a unit
     *      you can build
     * @param: unitType
     *      int type of unit to build
     * @returns:
     *      a BuildAction specifying where to build (after querying all adjacent spaces), or "null" otherwise.
     */
   public BuildAction buildRobo(int roboType, int direction) {
      // we need to make sure we have at least enough fuel to attack and mine
      // no point in building more troops when we have less than 200 fuel
      // because we won't have enough fuel to perform any actions
      
      // by pass the fuel limit check
      if (direction >= 9) {
         direction = direction - 10;
      }
      else if (fuel <= fuelLimit) {
         log("not enough fuel to build");
         return null;
      }
      
      if (karbonite < SPECS.UNITS[roboType].CONSTRUCTION_KARBONITE) {
         // log("not enough karbonite");
         return null;
      }

      ArrayList<Tuple> unoccupiedTiles = new ArrayList<Tuple>();

      Tuple[] directionList;

      // if a direction is specified, get the ordered list of directions
      // in terms of that direction, otherwise get default ordering
      if (direction >= 0) {
         directionList = Directions.orderDirs(direction);
      } else {
         directionList = Directions.dirs;
      }

      for (Tuple dir : directionList) {
         // if out of bounds
         if (me.x + dir.x < 0 || me.x + dir.x >= this.map.length ||
                  me.y + dir.y < 0 || me.y + dir.y >= this.map.length) {
            continue;
         }

         if (tileNodeMap[me.y + dir.y][me.x + dir.x].tileType != -1 && visibleMap[me.y + dir.y][me.x + dir.x] == 0) {
            if (direction >= 0) {
               return buildUnit(roboType, dir.x, dir.y);
            }
            else unoccupiedTiles.add(dir);
         }
      }

      int numMoves = unoccupiedTiles.size();
      if (numMoves == 0) {
         signal(59999, 36);
      } else {
         Tuple randUnoccupied = unoccupiedTiles.get((int) (Math.random() * numMoves));
         return buildUnit(roboType, randUnoccupied.x, randUnoccupied.y);
      }

      return null;
   }

   /**
    * Given an ArrayList of TileNodes, return the TileNode that is heuristically
    * closest to the robot's own position via diagonalDistance
    * @param nodeList
    *       the list of tile nodes
    * @return
    *       the tilenode that is heuristically closest via diagonalDistance
    */
   public TileNode closestNode(ArrayList<TileNode> nodeList) {
      TileNode retNode = null;
      int minDist = Integer.MAX_VALUE;
      Tuple myLoc = new Tuple(me.x,me.y);
      for(TileNode t : nodeList) {
         int rDistance = MapFunctions.radiusDistance(myLoc, t.location);
         if(rDistance <= minDist) {
            retNode = t;
            minDist = rDistance;
         }
      }
      return retNode;
   }

   /**
    * From a tile node map (TileNode[][]), returns a list of all TileNodes
    * which contain the given type (an integer):
    * -1 is impassable, 0 is normal, 1 is karbo, 2 is fuel
    * @param map
    *       the map
    * @param type
    *       the TileNode type
    * @return
    *       a list of all tiles of the given type. This is fairly bruteforce
    */
   public ArrayList<TileNode> getTypedTiles(TileNode[][] map, int type) {
      ArrayList<TileNode> ret = new ArrayList<TileNode>();
      for(int i = 0; i < map.length; i++) {
         for(int j = 0; j < map[i].length; j++) {
            if(map[i][j].tileType == type) {
               ret.add(map[i][j]);
            }
         }
      }
      return ret;
   }
}
