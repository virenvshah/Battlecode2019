package bc19;

import java.util.ArrayList;

public class MapFunctions{
   /**
    * Moves towards end using fuel amount of fuel
    * @param end
    *   The final destination
    * @param steps
    *   The amount of steps that can be taken
    * @param mapType
    *   0 to use the general passable map (in almost all cases we use 0)
    *   and 1 to use the visible map 
    * @return
    *   The action if we can move, or null if reached destination
    */
   public static Tuple moveTo(MyRobot robot, Tuple end, int steps, int mapType) {
      // not enough fuel to move to next location, we save 150 fuel
      // for attacking 
      if (robot.fuel <= 100 || robot.fuel < robot.fuelLimit) {
         robot.log("Not enough fuel to move");
         return null;
      }

      makeVisibleMap(robot);
      Tuple myLoc = new Tuple(robot.me.x, robot.me.y);
     
      // if already at destination
      if (end.equals(new Tuple(robot.me.x, robot.me.y))) {
         // path is over
         robot.currentPath = null;
         return null;
      // if no path or wrong path is set
      } else if (robot.currentPath == null || !robot.currentPath.destination().equals(end)) {
         robot.currentPath = shortestPath(robot, end, mapType);
      }

      // make sure currentPath is not null because shortest Path may have returned null
      // also make sure we are going towards the right destination
      if (robot.currentPath != null && robot.currentPath.destination().equals(end)) {
         // take a look at the next location   
         Tuple next = null;
         boolean foundMove = false;
         int s = steps;
         
         // find the farthest location in speed radius that can be moved to
         // keep shrinking step size until some location is found
         do {
             next = robot.currentPath.peekLocation(s);
             if (robot.visibleMap[next.y][next.x] == 0) {
                foundMove = true;
                steps = s;
                break;
             }
             s--;
         } while (s >= 1);
             
         
         // if the next location is occupied
         if (!foundMove) {
          //  robot.log("Beginning detour process");
            // check if destination is visble, if so just path to it using 
            // the visible map
            Path detour = null;
            // from which index should this detour overwrite in the orignal path
            int indexOrigPath = 0;
            
            // continue while within vision range
            int count = 0;
            int maxCount = (int) Math.sqrt(robot.SPECS.UNITS[robot.me.unit].VISION_RADIUS);
            // iterate through the path looking for a location that is not occupied
            for (int i = robot.currentPath.index + steps; i < robot.currentPath.tupleList.length; i++) {
               
               if (count >= maxCount) {
                  break;
               }
               
               Tuple pathTuple = robot.currentPath.tupleList[i];
               
               // if a non-occupied location is found
               if (robot.visibleMap[pathTuple.y][pathTuple.x] == 0) {
                  // Find a detour to this path
                  detour = shortestPath(robot, pathTuple, 1);
                  indexOrigPath = i + 1;
                  if (detour != null) {
                   //  robot.log("Found detour to " + pathTuple.toString());
                     break;
                  }
               }
               
               count++;
            }
            
            // detour is still null, check if destination is in range
            if (detour == null && MapFunctions.radiusDistance(robot.currentPath.destination(), myLoc) <=
                     robot.SPECS.UNITS[robot.me.unit].VISION_RADIUS) {
               detour = shortestPath(robot, robot.currentPath.destination(), 1);
               indexOrigPath = robot.currentPath.tupleList.length;
              // robot.log("Found detour to destination");
            }

            if (detour != null) {
               robot.currentPath.prependPath(detour, indexOrigPath);
               // reassign steps
               s = steps;
               do {
                  next = robot.currentPath.peekLocation(s);
                  if (robot.visibleMap[next.y][next.x] == 0) {
                     foundMove = true;
                     steps = s;
                     break;
                  }
                  s--;
               } while (s >= 1);
               
               if (!foundMove) return null;
               
            } else {
               // detour failed
               // robot.log("Detour failed");
               robot.currentPath = null;
               return null;
            }
         }
         
         // If path is over, set currentPath to null
         if (robot.currentPath != null && robot.currentPath.peekLocation(steps) == null) {
            robot.currentPath = null;
         }
         
         // return a Tuple indicating the direction towards the next location
         // robot.log("Next location is " + robot.currentPath.peekLocation(steps) + " Steps " + steps);
         return robot.diff(robot.currentPath.nextLocation(steps), myLoc);
      }
      
      // if nothing else
      robot.currentPath = null;
      return null;
   }
   
   /**
    * Initializes th1e TileNode map, by creating a grid of tile nodes
    * based on the Passable, Karbonite, and Fuel maps
    */
   public static void makeMap(MyRobot robot) {
      robot.tileNodeMap = new TileNode[robot.map.length][robot.map.length];
      // iterate through each tile and initialize it
      for (int i = 0; i < robot.map.length; i++) {
         for (int j = 0; j < robot.map.length; j++) {
            robot.tileNodeMap[i][j] = new TileNode();

            // something at [i][j] has coordinates j, i
            robot.tileNodeMap[i][j].location = new Tuple(j, i);

            robot.tileNodeMap[i][j].occupantId = -1;

            // determines the tileType based on the API
            // if the terrain is passable then it is either a Karbonite tile, Fuel tile,
            // or empty
            if (robot.map[i][j]) {
               // if the tile has karbonite
               if (robot.karboniteMap[i][j]) {
                  robot.tileNodeMap[i][j].tileType = 1;
               // if the tile has fuel
               } else if (robot.fuelMap[i][j]) {
                  robot.tileNodeMap[i][j].tileType = 2;
               // tile has nothing
               } else {
                  robot.tileNodeMap[i][j].tileType = 0;
               }
               
               // the next step is to add all neighboring passable tiles to each
               // tile node
   
               int index = 0;
   
               // notice that row, col and x,y are opposite
               // south
               if (i-1 >= 0 && robot.map[i-1][j])
                  robot.tileNodeMap[i][j].neighbors[index++] = new Tuple(j, i-1);
   
               // north
               if (i+1 < robot.map.length && robot.map[i+1][j])
                  robot.tileNodeMap[i][j].neighbors[index++] = new Tuple(j, i+1);
   
               // west
               if (j-1 >= 0 && robot.map[i][j-1])
                  robot.tileNodeMap[i][j].neighbors[index++] = new Tuple(j-1, i);
   
               // east
               if (j+1 < robot.map.length && robot.map[i][j+1])
                  robot.tileNodeMap[i][j].neighbors[index++] = new Tuple(j+1, i);
   
               // south-west
               if (i-1 >= 0 && j-1 >= 0 && robot.map[i-1][j-1])
                  robot.tileNodeMap[i][j].neighbors[index++] = new Tuple(j-1, i-1);
   
               // south-east
               if (i-1 >= 0 && j+1 < robot.map.length && robot.map[i-1][j+1])
                  robot.tileNodeMap[i][j].neighbors[index++] = new Tuple(j+1, i-1);
   
               // north-west
               if (i+1 < robot.map.length && j-1 >= 0 && robot.map[i+1][j-1])
                  robot.tileNodeMap[i][j].neighbors[index++] = new Tuple(j-1, i+1);
   
               // north-east
               if (i+1 < robot.map.length && j+1 < robot.map.length && robot.map[i+1][j+1])
                  robot.tileNodeMap[i][j].neighbors[index++] = new Tuple(j+1, i+1);
   
               robot.tileNodeMap[i][j].numOfNeighbors = index;
            // otherwise the tile is impassable
            } else {
               robot.tileNodeMap[i][j].tileType = -1;
            }   
         }
      }
   }
   
   /**
    * Initializes th1e TileNodeVisible map, by creating a grid of tile nodes
    * based on the VisiblieTile map
    */
   public static void makeVisibleMap(MyRobot robot) {
      int[][] visibleMap = robot.getVisibleRobotMap();
      robot.tileNodeVisibleMap = new TileNode[robot.map.length][robot.map.length];

      for (int i = 0; i < robot.map.length; i++) {
         for (int j = 0; j < robot.map.length; j++) {
            robot.tileNodeVisibleMap[i][j] = new TileNode();

            // something at [i][j] has coordinates j, i
            robot.tileNodeVisibleMap[i][j].location = new Tuple(j, i);

            robot.tileNodeVisibleMap[i][j].occupantId = -1;

            // determines the tileType based on the API
            // if the terrain is passable then it is either a Karbonite tile, Fuel tile,
            // or empty
            if ((visibleMap[i][j] == 0 && robot.map[i][j]) || visibleMap[i][j] == robot.id) {
               // if the tile has karbonite
               if (robot.karboniteMap[i][j]) {
                  robot.tileNodeVisibleMap[i][j].tileType = 1;
               // if the tile has fuel
               } else if (robot.fuelMap[i][j]) {
                  robot.tileNodeVisibleMap[i][j].tileType = 2;
               // tile has nothing
               } else {
                  robot.tileNodeVisibleMap[i][j].tileType = 0;
               }
               
               // the next step is to add all neighboring passable tiles to each
               // tile node
   
               int index = 0;
   
               // notice that row, col and x,y are opposite
               // south
               if (i-1 >= 0 && robot.map[i-1][j] && visibleMap[i-1][j] == 0)
                  robot.tileNodeVisibleMap[i][j].neighbors[index++] = new Tuple(j, i-1);
   
               // north
               if (i+1 < robot.map.length && robot.map[i+1][j] && visibleMap[i+1][j] == 0)
                  robot.tileNodeVisibleMap[i][j].neighbors[index++] = new Tuple(j, i+1);
   
               // west
               if (j-1 >= 0 && robot.map[i][j-1] && visibleMap[i][j-1] == 0)
                  robot.tileNodeVisibleMap[i][j].neighbors[index++] = new Tuple(j-1, i);
   
               // east
               if (j+1 < robot.map.length && robot.map[i][j+1] && visibleMap[i][j+1] == 0)
                  robot.tileNodeVisibleMap[i][j].neighbors[index++] = new Tuple(j+1, i);
   
               // south-west
               if (i-1 >= 0 && j-1 >= 0 && robot.map[i-1][j-1] && visibleMap[i-1][j-1] == 0)
                  robot.tileNodeVisibleMap[i][j].neighbors[index++] = new Tuple(j-1, i-1);
   
               // south-east
               if (i-1 >= 0 && j+1 < robot.map.length && robot.map[i-1][j+1] && visibleMap[i-1][j+1] == 0)
                  robot.tileNodeVisibleMap[i][j].neighbors[index++] = new Tuple(j+1, i-1);
   
               // north-west
               if (i+1 < robot.map.length && j-1 >= 0 && robot.map[i+1][j-1] && visibleMap[i+1][j-1] == 0)
                  robot.tileNodeVisibleMap[i][j].neighbors[index++] = new Tuple(j-1, i+1);
   
               // north-east
               if (i+1 < robot.map.length && j+1 < robot.map.length && robot.map[i+1][j+1] && visibleMap[i+1][j+1] == 0)
                  robot.tileNodeVisibleMap[i][j].neighbors[index++] = new Tuple(j+1, i+1);
   
               // robot.log(robot.tileNodeVisibleMap[i][j].location.toString() + " has " + index + " neighbours");
               robot.tileNodeVisibleMap[i][j].numOfNeighbors = index;
            // otherwise the tile is impassable or has an occupant or is not visible
            } else {
               robot.tileNodeVisibleMap[i][j].tileType = -1;
            }   
         }
      }
   }
   
   /**
    * Adds a TileNode to the neighbor list of all its neighbors
    */
   public static void addNeighborsVisibleMap(MyRobot robot, Tuple point) { 
      TileNode tile = robot.tileNodeVisibleMap[point.y][point.x];
      for (Tuple d : Directions.dirs) {
         // make sure the points are in bounds
         if (point.x + d.x < 0 || point.x + d.x >= robot.tileNodeVisibleMap.length || 
                  point.y + d.y < 0 || point.y + d.y >= robot.tileNodeVisibleMap.length) {
            continue;
         }
         
         TileNode neighbor = robot.tileNodeVisibleMap[point.y + d.y][point.x + d.x];
         neighbor.neighbors[neighbor.numOfNeighbors++] = tile.location;
      }
   }
   
   /**
    * Finds the shortest path between two points
    *
    *
    * @param endingPoint
    *    The point at which the path ends
    *
    * @return
    *    An array of Tuples which represents the
    *    points that the path visits in order
    */
   public static Path shortestPath(MyRobot robot, Tuple endingPoint, int mapType) {
      //long start = robot.me.time;
      TileNode[][] theMap;
      if (mapType == 0) {
         theMap = robot.tileNodeMap;
      } else {
         theMap = robot.tileNodeVisibleMap;
      }
       
      Tuple startingPoint = new Tuple(robot.me.x, robot.me.y);

      // the goal is an impassable tile, for visible map, the goal can be an 
      // occupied tile, but not an impassable tile
      if ((theMap[endingPoint.y][endingPoint.x].tileType < 0 && mapType == 0) ||
               (mapType == 1 && robot.visibleMap[endingPoint.y][endingPoint.x] < 0)) { 
         // robot.log(endingPoint.toString() + " Ending point is impassable");
         return null;
      }
      
      if (mapType == 1 && robot.visibleMap[endingPoint.y][endingPoint.x] > 0) {
         addNeighborsVisibleMap(robot, endingPoint);
      }
         

      // the start and end points are the same
      if (startingPoint.equals(endingPoint))
         return null;

      // create a minPriorityQueue
      PriorityQueue frontier =
            new PriorityQueue(true);

      reset(robot, mapType);
      
      // set the distance to be 0 and predecessor to null
      // because in a previous iteration it may have been a different value
      theMap[startingPoint.y][startingPoint.x].distance = 0;
      theMap[startingPoint.y][startingPoint.x].predecessor = null;

      // update visited status
      theMap[startingPoint.y][startingPoint.x].wasVisited = true;

      // set the weight for the starting node
      theMap[startingPoint.y][startingPoint.x].weight =
            manhattanDistance(startingPoint, endingPoint);

      // add the starting node to the priority queue
      frontier.add(theMap[startingPoint.y][startingPoint.x],
               theMap[startingPoint.y][startingPoint.x].weight);

      
      int count = 0;
      while (frontier.size() > 0) {
         count++;
         TileNode tile = frontier.poll();  // tile with smallest weight

         //robot.log(tile.location.toString());

         // get each neighboring tile
         for (int i = 0; i < tile.numOfNeighbors; i++) {
            TileNode successor =
                     theMap[tile.neighbors[i].y][tile.neighbors[i].x];

            // if the goal has been reached
            if (successor.location.equals(endingPoint)) {
               successor.predecessor = tile;
               successor.distance = tile.distance + 
                        manhattanDistance(successor.location, tile.location);
              // robot.log("Success");
               return new Path(getPath(successor), successor.distance);
            }

            // if the tile was already visited skip the tile
            if (successor.tileType < 0 || successor.wasVisited) {
               continue;
            }

            // update successor's distance, weight, visited status
            successor.distance = tile.distance + 
                     manhattanDistance(successor.location, tile.location);
            successor.weight = successor.distance +
                  manhattanDistance(successor.location, endingPoint);
            successor.wasVisited = true;

            // update successor's parent
            successor.predecessor = tile;

            // add successor to priority queue
            frontier.add(successor, successor.weight);
         }
      }

     // robot.log("No path found");

      return null;
   }

   /**
    * Obtains the path from the startingNode to the endingNode
    * after A* search is done.
    *
    * @param   lastNode
    *    The endingNode
    * @return
    *    The path
    */
   private static Tuple[] getPath(TileNode lastNode) {
      // + 1 for starting point
      Tuple[] tempPath = new Tuple[lastNode.distance + 1];
      int index = lastNode.distance;

      TileNode current = lastNode; 
      TileNode previous = lastNode.predecessor;

      // the previous node is the node that came before this node in the path
      // if the previous node is null it means we reached the startNode
      while (previous != null) {
         tempPath[index--] = new Tuple(current.location.x, current.location.y);
         current = previous;
         previous = current.predecessor;
      }
      
      // add the starting point
      tempPath[index] = new Tuple(current.location.x, current.location.y);
      
      // first index of tempPath
      
      // tempPath may have some empty spaces in the beginning
      // because lastNode.distance overestimates the size of the array
      // hence we must copy to path
      Tuple[] path = new Tuple[tempPath.length - index];
      for (int i = 0; i < path.length; i++) {
         path[i] = tempPath[index++];
      }

      return path;
   }

   /**
    * Resets the values of wasVisited to false for each TileNode before
    * the A* heuristic search
    */
   private static void reset(MyRobot robot, int mapType) {
      TileNode[][] theMap;
      if (mapType == 0) {
         theMap = robot.tileNodeMap;
      } else {
         theMap = robot.tileNodeVisibleMap;
      }
      
      for (int i = 0; i < robot.map.length; i++) {
         for (int j = 0; j < robot.map.length; j++) {
            theMap[i][j].wasVisited = false;
         }
      }
   }

   /**
    * Estimates smallest distance between two points in terms of robot movement
    * This distance is the diagonal distance heuristic function.
    *
    * @return
    *    The estimated distance
    */
   public static int radiusDistance(Tuple a, Tuple b) {
      // the difference in x values of the two points
      int xDiff = Math.abs(a.x - b.x);

      // the difference in y values of the two points
      int yDiff = Math.abs(a.y - b.y);

      // return the radius distance;
      return xDiff * xDiff + yDiff * yDiff;
   }
   
   /**
    * Estimates smallest radius distance between two points,
    * This estimate is equal to the manhattan distance between the two points
    */
   public static int manhattanDistance(Tuple a, Tuple b) {
      // the difference in x values of the two points
      int xDiff = Math.abs(a.x - b.x);

      // the difference in y values of the two points
      int yDiff = Math.abs(a.y - b.y);

      // return the radius distance;
      return xDiff + yDiff;
   }
   
   /**
    * Finds the closest karbonite or fuel depot IN ROBOT.KARBODEPOTS OR 
    * ROBOT.FUELDEPOTS by running a BFS from the robot's current location.
    * @param tileType 
    *    1 for karbonite, 2 for fuel
    * @return
    */
   public static Path bfsGetClosestMiningLoc(MyRobot robot, ArrayList<TileNode> possibleDestinations) {
      TileNode[] frontier = new TileNode[4096];
      int start = 0;
      int end = 0;
      Tuple myLoc = new Tuple(robot.me.x, robot.me.y);
      TileNode[][] theMap = robot.tileNodeMap;
      // reset the tile node map
      reset(robot, 0);
      // robot.log("Time is " + robot.me.time);
      
      frontier[end++] = theMap[robot.me.y][robot.me.x];
      
      if (possibleDestinations.size() == 0) { 
         return null;
      }
      
      // the start and end points are the same
      if (possibleDestinations.contains(theMap[robot.me.y][robot.me.x])) {
         Tuple[] path = {myLoc}; 
         return new Path(path, 0);
      }
      
      theMap[robot.me.y][robot.me.x].distance = 0;
      theMap[robot.me.y][robot.me.x].predecessor = null;

      // update visited status
      theMap[robot.me.y][robot.me.x].wasVisited = true;
      
      int count = 0;
      while (start != end) {
         count++;
         TileNode tile = frontier[start++];  // tile with smallest weight
         //robot.log(tile.location.toString());

         // get each neighboring tile
         for (int i = 0; i < tile.numOfNeighbors; i++) {
            TileNode successor =
                     theMap[tile.neighbors[i].y][tile.neighbors[i].x];

            // if the goal has been reached
            if (possibleDestinations.contains(successor)) {
               successor.predecessor = tile;
               successor.distance = tile.distance + 
                        manhattanDistance(successor.location, tile.location);
               // robot.log("Success");
               return new Path(getPath(successor), successor.distance);
            }

            // if the tile was already visited skip the tile
            if (successor.tileType < 0 || successor.wasVisited) {
               continue;
            }

            // update successor's distance, weight, visited status
            successor.distance = tile.distance + 
                     manhattanDistance(successor.location, tile.location);

            successor.wasVisited = true;
            // update successor's parent
            successor.predecessor = tile;

            // add successor to priority queue
            frontier[end++] = successor;
         }
      }
      
      robot.log("No BFS path found");

      return null;
   }
}