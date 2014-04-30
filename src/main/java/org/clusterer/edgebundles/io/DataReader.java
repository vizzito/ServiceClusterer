package org.clusterer.edgebundles.io;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * <p>Title: Data Reader Interface</p>
 *
 * <p>Description: Reads a data set data provider.</p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author Bilal Alsallakh
 * @version 1.0
 */
public interface DataReader {
        /**
         * returns the number of nodes in the tree stored in the data provider
         * @return int
         */
    int getNodesCount();

        /**
         * returns the index of the parent of the ith node, in the nodes list
         * @return int
         */
    int getParent(int node);

    /**
     * returns the indices of the adjacent nodes of a given node
     * @return int
     */
    int[] getAdjacentNodes(int node);
    
    
    int[][] getAdjacencyList();
    int[] getParents();
    AbstractMap<Integer,String> getNamesMap();
    AbstractMap<String,String> getMapParentFile();
    

}
