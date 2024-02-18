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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * The location of a single character in all strings
 */
public class Node implements Serializable {
  private static final long serialVersionUID = -1561971028727693347L;

  public short[] id;// position information
  private int hashCode; // hash code
  public short level;
  public Set<Node> precs;

  public int sum() {
    int sum = 0;
    for (short i : id) {
      sum += i;
    }
    return sum;
  }

  public void updateLevel(short n){
    this.level=n;
  }
  /**
   * link precursor to this nodes
   *
   * @param prec
   */
  public void addEdge(Node prec) {
    if (null == precs) {
      precs = new HashSet<>();
    }
    precs.add(prec);
  }

  /**
   * link precursor to this nodes
   *
   * @param prec
   */
  public void setEdge(Node prec) {
    if (null == precs) {
      precs = new HashSet<>();
    }

    var removed = new HashSet<Node>();
    for (Node p : this.precs) {
      if (p.level < this.level - 1) {
        removed.add(p);
      }
    }
    precs.removeAll(removed);
    precs.add(prec);
  }

  @Override
  public String toString() {
    String str = Arrays.toString(id);
    str = str.replaceAll(" ", "");
    return "(" + str.substring(1, str.length() - 1) + ")";
  }

  public Node(short[] index) {
    this.id = index;
    this.hashCode = buildHashCode();
  }

  private int buildHashCode() {
    int rs = 1;
    int i = 0;
    while (i < id.length) {
      rs = 31 * rs + id[i];
      i += 1;
    }
    return rs;
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

  @Override
  public boolean equals(Object o) {
    Node bl = (Node) o;
    if (bl == this) return true;
    short[] b = bl.id;
    short[] a = id;
    int i = 0;
    boolean equals = true;
    while (i < a.length && equals) {
      if (a[i] != b[i]) {
        equals = false;
      }
      i += 1;
    }
    return equals;
  }

  public static class SumSorter implements Comparator<Node> {
    Mlcs mlcs;

    public SumSorter(Mlcs mlcs) {
      this.mlcs = mlcs;
    }

    public int compare(Node o1, Node o2) {
      return o1.sum() - o2.sum();
    }
  }
}
