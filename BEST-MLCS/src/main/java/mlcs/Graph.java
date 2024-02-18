/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mlcs;

import java.math.BigDecimal;
import java.util.*;

/**
 * The MLCS problem solution graph.
 * It's a layered storage structure. The nodes[i] represents the set of key points at layer i.
 */
public class Graph {
  public final HashMap<Node, Node>[] layers;
  public final HashMap<Node, Node> nodes;
  public final Mlcs mlcs;
  public int maxLevel = 0;
  public Node end;

  public Graph(Mlcs mlcs) {
    this.mlcs = mlcs;
    this.maxLevel = mlcs.maxLength;
    layers = new HashMap[maxLevel + 2];//0..maxLevel+1
    for (int i = 0; i < maxLevel + 2; i++) {
      layers[i] = new HashMap<>();
    }
    layers[0].put(mlcs.start, mlcs.start);
    nodes = new HashMap<>();
  }

  /**
   * How many point in the layer which contains the most points.
   *
   * @return
   */
  public int height() {
    int max = 0;
    for (HashMap<Node, Node> ns : layers) {
      if (ns != null && ns.size() > max) {
        max = ns.size();
      }
    }
    return max;
  }

  public int size() {
    int count = 0;
    for (HashMap<Node, Node> ns : layers) {
      if (ns != null) {
        count += ns.size();
      }
    }
    return count;
  }

  /**
   * Fetch the node corresponding the given id and level.
   *
   * @param id
   * @param level
   * @return
   */
  private Node get(Node id, short level) {
    return layers[level].get(id);
  }

  /**
   * Register a layer
   *
   * @param level
   * @param ns
   */
  public void addLevel(short level, HashMap<Node, Node> ns) {
    layers[level] = ns;
  }

  /**
   * Register a single node
   *
   * @param level
   * @param n
   */
  public void add(short level, Node n) {
    HashMap<Node, Node> ns = layers[level];
    if (null == ns) {
      ns = new HashMap<>();
      layers[level] = ns;
    }
    ns.put(n, n);
  }


  public void add(Node loc) {
    nodes.put(loc, loc);
    layers[loc.level].put(loc, loc);
  }

  public void shift(Node loc, short fromLevel, short toLevel) {
    //System.out.println("shift " + loc.toString() + " from " + fromLevel + " to " + toLevel);
    layers[fromLevel].remove(loc);
    //backwardRemovePath(loc, fromLevel);
    layers[toLevel].put(loc, loc);
  }

  public  void cleanup(){
    for(int i=0;i<maxLevel;i++){
      var layer= layers[i];
      var removed= new HashSet<Node>();
      for(Node n:layer.keySet()){

      }
    }
  }

  private boolean cleanupNode(Node loc) {
    boolean exists = false;
    for (Node succor : mlcs.nextLocations(loc)) {
      if (null != layers[loc.level + 1].get(succor)) {
        exists = true;
        break;
      }
    }
    if (!exists) {
      for (Node succor : mlcs.nextLocations(loc)) {
        var su = nodes.get(succor);
        if (null != su && null != su.precs) {
          if (su.precs.contains(loc)) {
           su.precs.remove(loc);
            break;
          }
        }
      }
    }
    return exists;
  }

  private void backwardRemovePath(Node from, short fromLevel) {
    layers[from.level].remove(from);
    nodes.remove(from);
    //System.out.println("remove "+ from.toString());
    if (null != from.precs) {
      var removedPreces = new HashSet<Node>();
      for (Node prec : from.precs) {
        if (!existSuccessors(prec)) {
          removedPreces.add(prec);
          backwardRemovePath(prec, prec.level);
        }
      }
      from.precs.removeAll(removedPreces);
    }
  }

  private boolean existSuccessors(Node loc) {
    boolean exists = false;
    for (Node succor : mlcs.nextLocations(loc)) {
      if (null != layers[loc.level + 1].get(succor)) {
        exists = true;
        break;
      }
    }
    if (!exists) {
      for (Node succor : mlcs.nextLocations(loc)) {
        var su = nodes.get(succor);
        if (null != su && null != su.precs) {
          if (su.precs.contains(loc)) {
            System.out.println("dd");
            break;
          }
        }
      }
    }
    return exists;
  }

  /**
   * Query layer nodes at the given level
   *
   * @param l
   * @return
   */
  public HashMap<Node, Node> getLevel(int l) {
    return layers[l];
  }

  /**
   * Calculate all MLCS paths
   *
   * @return
   */
  public List<List<Node>> paths() {
    return paths(-1);
  }

  /**
   * Calculate limit MLCS paths
   *
   * @return the path list
   * @limit -1 represent unlimit
   */
  public List<List<Node>> paths(int limit) {
    List<List<Node>> results = new ArrayList<>();
    Stack<Node> stack = new Stack<>();
    Stack<Node> path = new Stack<>();
    Collection<Node> lasts = end.precs;
    for (Node n : lasts) stack.push(n);

    while ((limit < 0 || results.size() < limit) && !stack.isEmpty()) {
      Node current = stack.peek();
      if (!path.isEmpty() && path.peek().equals(current)) {
        path.pop();
        stack.pop();
      } else {
        path.push(current);
        if (current.level == 1) {
          List<Node> onePath = new ArrayList<>(path);
          Collections.reverse(onePath);//堆栈的dump是从底部开始的。所以要取反
          results.add(onePath);
          stack.pop();
          path.pop();
        } else {
          for (Node pres : current.precs) stack.push(pres);
        }
      }
    }
    return results;
  }

  /**
   * Stat the MLCS count
   *
   * @param totalCreateCount
   * @param highestCapacity
   * @param startAt
   * @return
   */
  public Result stat(long totalCreateCount, long highestCapacity, long startAt) {
    Node startLocation = mlcs.start;
    Node endLocation = mlcs.end;
    Node end = layers[maxLevel + 1].get(endLocation);
    this.end = end;
    //link();

    Set<Node> keyLocs = new HashSet<Node>();
    BigDecimal matchedCount = new BigDecimal(0); // Number of matched results
    LinkedList<Node> queue = new LinkedList<>();

    // The number of alternative paths from the virtual endpoint to the node, with an initial endpoint of 1
    Map<Node, BigDecimal> routeCounts = new HashMap<Node, BigDecimal>();

    routeCounts.put(endLocation, new BigDecimal(1));

    Node queueEnd = endLocation;
    queue.addLast(endLocation);

    short currentLevel = end.level;
    while (!queue.isEmpty()) {
      Node loc = queue.removeFirst();
      Node node = get(loc, currentLevel);
      for (Node ploc : node.precs) {
//        if (ploc.level != node.level - 1) {
//          System.out.println(ploc);
//        }
        if (keyLocs.contains(ploc)) {
          routeCounts.put(ploc, routeCounts.get(ploc).add(routeCounts.get(loc)));
        } else {
          keyLocs.add(ploc);
          routeCounts.put(ploc, routeCounts.get(loc));
          queue.addLast(ploc);
        }
      }
      if (loc == queueEnd) {
        if (!queue.isEmpty()) queueEnd = queue.getLast();
        currentLevel -= 1;
      }
    }
    keyLocs.remove(startLocation);
    keyLocs.remove(endLocation);
    matchedCount = routeCounts.get(startLocation);

    return new Result(this, matchedCount, keyLocs.size(), maxLevel, totalCreateCount, highestCapacity,
        startAt, System.currentTimeMillis());
  }

  /**
   * Link the graph from back to forward.
   */
  public void link() {
    int l = maxLevel;
    while (l >= 0) {
      HashMap<Node, Node> nexts = getLevel(l + 1);
      Iterator<Node> iter = getLevel(l).values().iterator();
      boolean isLastLayer = l == maxLevel;
      while (iter.hasNext()) {
        Node n = iter.next();
        List<Node> nextLocs = mlcs.nextLocations(n);
        for (Node loc : nextLocs) {
          Node next = nexts.get(loc);
          if (next != null) next.addEdge(n);
        }
        if ((nextLocs.isEmpty() && isLastLayer)) {
          end.addEdge(n);
        }
      }
      l -= 1;
    }
  }

  public boolean largeThan(Graph graph) {
    if (this.maxLevel > graph.maxLevel) return true;
    else if (this.maxLevel < graph.maxLevel) return false;
    else return this.size() > graph.size();
  }
}
