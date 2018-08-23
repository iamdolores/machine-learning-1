//
//  CCSubTree.java
//
//  Created by Veronica Mayorga
//  Copyright. All rights reserved.
//
import javax.swing.tree.DefaultMutableTreeNode;

public class CCSubTree {
    private DefaultMutableTreeNode tree = null;
    private double alpha;

    public CCSubTree() {
    }
    public CCSubTree(DefaultMutableTreeNode t, double a) {
        tree = t;
        alpha = a;
    }
    public void setRoot(DefaultMutableTreeNode t) {
        tree = t;
    }
    public void setAlpha(double a) {
        alpha = a;
    }
    public DefaultMutableTreeNode getTree() {
        return tree;
    }
    public double getAlpha() {
        return alpha;
    }
}
