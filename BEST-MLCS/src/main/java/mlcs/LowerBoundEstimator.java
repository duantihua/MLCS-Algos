package mlcs;

import mlcs.util.Stopwatch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class LowerBoundEstimator {


  /**
   * Try to find a approximate length of the given MLCS
   */
  public static short estimate(Mlcs mlcs, int estimateCount) {
    var startTime = System.currentTimeMillis();
    List<Node> routes = List.of(mlcs.start);
    Node.SumSorter sorter = new Node.SumSorter(mlcs);
    ForkJoinPool pool = mlcs.newPool();
    short level = 0;
    while (!routes.isEmpty()) {
      level += 1;
      HashSet<Node> nexts = new HashSet<>();
      for (Node a : routes) nexts.addAll(mlcs.nextLocations(a));
      if (nexts.isEmpty()) {
        level -= 1;
        break;
      }
      ArrayList<Node> fronts = new ArrayList<>(nexts);
      // sorting and filtering
      if (fronts.size() <= estimateCount) {
        routes = fronts;
      } else {
        fronts.sort(sorter);
        routes = fronts.subList(0, estimateCount);
      }
    }
    System.out.println("\restimate mlcs length " + level + " 100% (reserve " + estimateCount + " points) using "
      + Stopwatch.format(System.currentTimeMillis() - startTime));
    pool.shutdown();
    return level;
  }

}
