package org.clusterer.edgebundles.tree;

import java.util.ArrayList;

import org.clusterer.edgebundles.io.DataReader;

/**
 * <p>Title: Visual Tree</p>
 *
 * <p>Description: A class which stores the tree and its adjacency relations
 * in a rich data structure which enables fast queries</p>
 * It reads the hiearchical and adjacency relations from a data reader, and
 * create the corresponding data structure upon contruction
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class VisualTree {
    private TreeNode[] nodes;

    /**
     * the roots of the tree
     * @return TreeNode
     */
    public TreeNode getRoot() {
        return nodes[0];
    }

    /**
     * the ith node in the tree (the nodes are partially ordered via
     * an order which respects the hierarchy
     * @return int
     */

    public TreeNode getNode(int i) {
        return nodes[i];
    }

    /**
     * the number of tree nodes
     * @return int
     */

    public int getNodesCount() {
        return nodes.length;
    }

    /**
     * constructing the tree from a data reader
     * @param dr DataReader
     */
    public VisualTree(DataReader dr) {
        int count = dr.getNodesCount();
    	nodes = new TreeNode[count];
        ArrayList[] children = new ArrayList[count];
        nodes[0] = new TreeNode(null, "ROOT", 0);
        children[0] = new ArrayList();
        for (int i = 1; i < count; i++) {
            int prnt = dr.getParent(i);
            nodes[i] = new TreeNode(nodes[prnt], (i + 1) + "", i);
            children[prnt].add(nodes[i]);
            children[i] = new ArrayList();
        }
        for (int i = 0; i < count; i++) {
            nodes[i].children =(TreeNode[]) children[i].toArray(new TreeNode[0]);
            int[] adjacentNodes = dr.getAdjacentNodes(i);
            nodes[i].adjacentNodes = new TreeNode[adjacentNodes.length];
            for (int j = 0; j < adjacentNodes.length; j++)
                nodes[i].adjacentNodes[j] = nodes[adjacentNodes[j]];
        }
    }
}
