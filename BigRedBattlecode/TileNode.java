package bc19;

/**
 * Represents a tile in the game
 */
public class TileNode {
   // the Cartesian location of the tile
   Tuple location;

   // -1 represents an impassable tile
   // 0 represents an empty tile
   // 1 represents a Karbonite tile
   // 2 represents a fuel tile
   int tileType;

   // the id of the unit on the tile
   int occupantId;

   // contains the coordinates of tiles that can be reached from this TileNode
   Tuple[] neighbors;

   // the
   int numOfNeighbors;

   // the distance from the starting point
   int distance;

   // the sum of the distance from the starting point and
   // the heuristic function distance to the ending point
   int weight;

   // whether shortest path algorithm has visited this tile yet or not
   boolean wasVisited;

   // the parent of this TileNode
   TileNode predecessor;

   /**
    * Creates a new TileNode
    */
   public TileNode() {
      neighbors = new Tuple[8];
      numOfNeighbors = 0;
   }
   
   public TileNode(Tuple location) {
      this.location = new Tuple(location.x, location.y);
      neighbors = new Tuple[8];
      numOfNeighbors = 0;
   }
   
   @Override
   public boolean equals(Object o) {
      // need a try catch for the type casting
      try {
         TileNode other = (TileNode) o;

         if (this.location.x == other.location.x && this.location.y == other.location.y)
            return true;

         return false;
      } catch (Exception e) {
         return false;
      }
   }

   public String toString() {
      return location.toString();
   }
}
