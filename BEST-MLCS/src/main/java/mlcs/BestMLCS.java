package mlcs;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static mlcs.util.FileSearcher.getFileShortName;
import static mlcs.util.FileSearcher.getOutFile;

public class BestMLCS {

  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      System.out.println("Usage:BestMLCS /path/to/your/data/file [minReserved=5]");
      return;
    }
    Map<String, String> arguments = new HashMap<>();
    if (args.length >= 2) {
      for (int i = 1; i < args.length; i++) {
        String arg = args[i];
        int eqIndx = arg.indexOf('=');
        String key = arg.substring(0, eqIndx).trim();
        String value = arg.substring(eqIndx + 1).trim();
        arguments.put(key, value);
      }
    }
    long startAt = System.currentTimeMillis();
    var sourceFile = new File(args[0]);
    Mlcs mlcs = Mlcs.build(Mlcs.loadData(sourceFile));
    int minReserved = Integer.parseInt(arguments.getOrDefault("minReserved", String.valueOf(mlcs.maxLength)));
    var lb = LowerBoundEstimator.estimate(mlcs, minReserved);
    var bestMlcs = new BestMLCS(mlcs, lb);
    String resultFile = getOutFile(sourceFile, "ep_" + getFileShortName(sourceFile) + ".txt");
    var graph = bestMlcs.search();
    graph.cleanup();
    statResult(graph, resultFile, startAt);
  }

  private static void statResult(Graph graph, String resultFile, long startAt) {
    Result result = graph.stat(graph.nodes.size(), graph.nodes.size(), startAt);
    System.out.println(result.buildResultString());
    result.dumpTo(resultFile);
    System.out.println("find " + result.mlcsCount.toString() + " mlcs(length " + result.maxLevel + ")");
    if (graph.maxLevel < 300) result.visualize();
  }


  public Mlcs mlcs;
  private final int lowerBound;

  public BestMLCS(Mlcs mlcs, int lowerBound) {
    this.mlcs = mlcs;
    this.lowerBound = lowerBound;
  }

  public Graph search() {
    var k = 0;
    Graph graph = new Graph(this.mlcs);
    while (!graph.layers[k].isEmpty()) {//Lk
      //new set for avoid ConcurrentModification,due to shifting from k to k+1
      var layerK = new HashSet<>(graph.layers[k].keySet());
      System.out.println("layer " + k + " size " + layerK.size());
      for (Node p : layerK) {
        if (p.level != k) continue;
        for (Node q : mlcs.nextLocations(p)) {
          var existQ = graph.nodes.get(q);
          if (existQ == null) {
            q.updateLevel((short) (k + 1));
            int possible = mlcs.tailUpbound(q.id);
            if (possible + q.level >= lowerBound) {
              q.addEdge(p);
              graph.add(q);
            }
          } else {
            if (existQ.level < (k + 1)) {
              graph.shift(existQ, existQ.level, (short) (k + 1));
              existQ.updateLevel((short) (k + 1));
              existQ.setEdge(p);//only reserved this one
            } else {
              existQ.addEdge(p);
            }
          }
        }
      }
      k = k + 1;
    }
    graph.maxLevel = k - 1;
    mlcs.end.level = (short) k;
    for (Node n : graph.layers[graph.maxLevel].keySet()) {
      mlcs.end.addEdge(n);
    }
    graph.add((short) (k), mlcs.end);
    return graph;
  }
}
