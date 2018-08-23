//
//  DTNode.java
//
//  Created by Veronica Mayorga
//  Copyright. All rights reserved.
//

public class DTNode {

    // majority at label and cost of node always set
    private Label majority      = null;
    private double cost;

    // attribute set if DTNode is branch node
    private Attribute attribute = null;

    // label is set if DTNode is leaf node
    private Label label         = null;

    public DTNode() {
    }

    public DTNode(Label m, double c) {
        majority = m;
        cost     = c;
    }

    public DTNode(Attribute attr, Label m, double c) {
        attribute = attr;
        majority  = m;
        cost      = c;
    }

    public DTNode(Attribute attr) {
        attribute = attr;
    }

    public DTNode(Label l, Label m, double c) {
        label    = l;
        majority = m;
        cost     = c;
    }

    public void setMajority (Label m) {
        majority = m;
    }

    public void setNodeCost(double c) {
        cost = c;
    }

    public void setAttribute (Attribute attr) {
        attribute = attr;
    }
    
    public void setLabel(Label l){
        label = l;
    }

    public Label getMajority() {
        return majority;
    }

    public double getNodeCost() {
        return cost;
    }

    public Attribute getAttribute(){
        return attribute;
    }

    public Label getLabel(){
        return label;
    }    

    public Object clone() {
        DTNode dup = new DTNode();
        dup.setNodeCost(cost);
        if (majority != null) {
            dup.setMajority((Label)majority.clone());
        }
        if (attribute != null) {
            dup.setAttribute((Attribute)attribute.clone());
        }
        if (label != null) {
            dup.setLabel((Label)label.clone());
        }
        return dup;
    }
    
    public String toString() {
        String s;
        s = "cost " + String.valueOf(cost);
        if (majority != null) {
            s = " " + majority.toString();
        }
        if (attribute != null) {
            s = " " + attribute.toString();
        }
        if (label != null) {
            s = " " + label.toString();
        }
        return s;
    }


}
