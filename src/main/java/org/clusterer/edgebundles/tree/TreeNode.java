package org.clusterer.edgebundles.tree;

import java.awt.Point;

/**
 *
 * <p>Title: Tree Node</p>
 *
 * <p>Description: A class to store properties of a given node in the visual
 * tree</p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: TU Wien</p>
 *
 * @author not attributable
 * @version 1.0
 */

public class TreeNode {
        private TreeNode parentNode;

        private Point center = new Point();

        private int id;

        private String caption;

        public String toString() {
          return caption;
        }

        /**
         * gets the position of the node on the screen (valid only after
         * applying a tree layouting and visualisation algorithm)
         */
        public Point getCenter() {
            return center;
        }

        /**
         * assigns the location of of the node on the screen
         */
        public void setCenter(int x, int y) {
            center.setLocation(x, y);
        }

	TreeNode[] children;

        TreeNode[] adjacentNodes;

        /**
         * constructs a node which parent is parentNode and caption is String
         */

	public TreeNode(TreeNode parentNode, String caption, int id) {
          this.id = id;
          this.caption = caption;
          this.parentNode = parentNode;
	}


        /**
         * returns the parent node of this node.
         */

	public TreeNode getParent() {
		return parentNode;
	}


        /**
         * returns the number of children of this node
         */

	public int getChildrenCount() {
		return children.length;
	}

        /**
         * returns the ith child of this node
         */
	public TreeNode getChild(int i) {
		return children[i];
	}

        /**
         * returns the number of adjacent nodes of this node
         */
        public int getAdjacentNodesCount() {
            return adjacentNodes.length;
        }

        /**
         * returns the ith adjacent node of this node
         */
        public TreeNode getAdjacentNode(int i) {
            return adjacentNodes[i];
        }

        /**
         * returns the id of this node (unique number assigned at creation)
         */
        public int getId() {
          return id;
        }

}
