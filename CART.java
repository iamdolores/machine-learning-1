//
//  CART.java
//
//  Created by Veronica Mayorga
//  Copyright. All rights reserved.
//


import java.util.LinkedList;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.ListIterator;
import java.util.Random;
import java.lang.Double;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Math;
import javax.swing.tree.DefaultMutableTreeNode;

public class CART {
    public DefaultMutableTreeNode root  = null;
    public Features features            = null;
    public boolean pruning              = false;
    public String impurityMeasure       = "gini";
    public int crossfoldV               = 10;
    // height of < 1 means return Tmax, otherwise return tree of height h
    public int height                   = -1;

    public static String GINI           = "gini";
    public static String ENTROPY        = "entropy";
    private static long seed            = (new Long("1164735835086")).longValue();        
    private static Random rNumGenerator = new Random(seed);
    private int generateTreeCount       = -1;

    public CART(LinkedList X, LinkedList P, Features f, String measure, boolean prune, int V, int h) {
        crossfoldV = V;
        features = f;
        impurityMeasure = measure;
        pruning = prune;
        height = h;
        run(X, P);
        // if (h != -1) {System.out.println("TREE HEIGHT was "+ h);}
    }
    public CART(LinkedList X, Features f, String measure, boolean prune, int V) {
        crossfoldV = V;
        features = f;
        impurityMeasure = measure;
        pruning = prune;
        LinkedList P = new LinkedList();
        run(X,P);
    }

    public CART(LinkedList X, Features f, String measure) {
        features = f;
        impurityMeasure = measure;
        LinkedList P = new LinkedList();
        run(X,P);
    }
    public void run(LinkedList X, LinkedList P) {
        // generates Tmax on root
        if (pruning && P.size() == 0) {
            root = getXVPrunedTree(X,crossfoldV);
        } else if (pruning && P.size() > 0) {
            root = getPruneSetPrunedTree(X,P);
        } else {
            root = getTmax(X);
        }
        //printNode(root);
    }

    public DefaultMutableTreeNode getTmax (LinkedList X) {
        DefaultMutableTreeNode Tmax = new DefaultMutableTreeNode();
        generateTree(X, Tmax, totalWeight(X));
        return Tmax;
    }

    public DefaultMutableTreeNode getXVPrunedTree(LinkedList L, int V) {
        // performs cost-complexity pruning with cross-validation

        DefaultMutableTreeNode prunedTree = null;

        // Generate Tmax on full train sample L
        DefaultMutableTreeNode TMax = new DefaultMutableTreeNode();
        generateTree(L, TMax, totalWeight(L));

        // Get minCostComplexitySubTrees TkSeq on full Train Sample L
        LinkedList TkSeq = minCostComplexitySubTrees(TMax);

        // For V fold cross-validation minCostComplexitySubTrees
        //   - where L(v) = L - Lv, v = 1, ..., V

        LinkedList[] Lv = getCVSets (L, V);
        // check that L is partitioned evenly into V lists contained in Lv

        int v, i, j, k;
        LinkedList[] Lofv = new LinkedList[V];
        ListIterator LvIt = null;
        for (v = 0; v < V; v++) {
            Lofv[v] = new LinkedList();
            for (i = 0; i < V; i++) {
                if (i != v){
                    LvIt = Lv[i].listIterator(0);
                    for(; LvIt.hasNext();) {
                        Lofv[v].add(LvIt.next());
                    }
                }
            }
        }

        // check that Lofv contains L - Lv
        
        //   - Train on L(v) and get T(v)max 
        //   - Get minCostComplexitySubTrees T(v)ks for each T(v)max
        DefaultMutableTreeNode[] TofvMax = new DefaultMutableTreeNode[V];
        LinkedList[] TofvSeq = new LinkedList[V];

        for (v = 0; v < V; v++) {
            TofvMax[v] = new DefaultMutableTreeNode();
            generateTree(Lofv[v], TofvMax[v], Lofv[v].size());
            TofvSeq[v] = minCostComplexitySubTrees(TofvMax[v]);
            
        }

        // Get geometric midpoints akprime = sqrt(ak*akplus1) of TkSeq
        // akprime is LinkedList of CCSubTrees.
        // it contains each CCSubTree contains akprime and corresponding Tk tree
        LinkedList akprime = getAlphaGeometricMeans(TkSeq);
        
        
        // For each akprime:
        //   For each v:
        //     find corresponding T(v)k
        //     use T(v)k to predict on Lv
        //     calculate R(cv) = 1/N *  Nij 
        //        where pred class i !=  real class j for each akprime
        // for akprime with min R(cv) pick corresponding Tk as optimal pruned tree
        DefaultMutableTreeNode Tofv = null;
        // use akmin = 0 so return T1 by default
        int kmin = 0;
        int errorCount = 0;
        int errorMin   = L.size();
        CCSubTree akprimeCCTree = null;
        if (akprime.size() == 1) {
            // if only one, then prime will be this one
            akprimeCCTree = (CCSubTree)akprime.get(kmin);
            prunedTree = akprimeCCTree.getTree();
            return prunedTree;            
        }
        double akprimeAlpha;
        ListIterator akprimeIt = akprime.listIterator(0);
        for (k = 0; akprimeIt.hasNext(); k++) {
            errorCount = 0;
            akprimeCCTree = (CCSubTree)akprimeIt.next();
            akprimeAlpha = akprimeCCTree.getAlpha();
            for(v = 0; v < V; v++) {
                Tofv = findTkofAlpha(TofvSeq[v],akprimeAlpha);
                errorCount += runSample(Lv[v], Tofv);
            }
            if (errorCount < errorMin) {
                kmin = k;
                errorMin = errorCount;
            }
        }
        
        akprimeCCTree = (CCSubTree)akprime.get(kmin);
        prunedTree = akprimeCCTree.getTree();
        return prunedTree;

    }
    public DefaultMutableTreeNode getPruneSetPrunedTree(LinkedList L, LinkedList P) {
        // performs cost-complexity pruning with cross-validation

        DefaultMutableTreeNode prunedTree = null;

        // Generate Tmax on full train sample L
        DefaultMutableTreeNode TMax = new DefaultMutableTreeNode();
        generateTree(L, TMax, totalWeight(L));

        // Get minCostComplexitySubTrees TkSeq on full Train Sample L
        LinkedList TkSeq = minCostComplexitySubTrees(TMax);

        // For V fold cross-validation minCostComplexitySubTrees
        //   - where L(v) = L - Lv, v = 1, ..., V
        int v, i, j, k;

        // For each minCCTree in TkSeq:
        //     use minCCTree to predict on P
        //     calculate R(cv) = 1/N *  Nij 
        //        where pred class i !=  real class j
        // for minCCTree with min R(cv) pick corresponding Tk as optimal pruned tree
        DefaultMutableTreeNode Tk = null;
        // use akmin = 0 so return T1 by default
        int kmin = 0;
        int errorCount = 0;
        int errorMin   = P.size();
        CCSubTree minCCTree = null;

        double akprimeAlpha;
        ListIterator TkSeqIt = TkSeq.listIterator(0);
        for (k = 0; TkSeqIt.hasNext(); k++) {
            minCCTree = (CCSubTree)TkSeqIt.next();
            Tk = minCCTree.getTree();
            errorCount = runSample(P, Tk);
            if (errorCount < errorMin) {
                kmin = k;
                errorMin = errorCount;
            }
        }
        
        minCCTree = (CCSubTree)TkSeq.get(kmin);
        prunedTree = minCCTree.getTree();
        return prunedTree;

    }
    
    public static DefaultMutableTreeNode findTkofAlpha( LinkedList CCSubTrees, double alpha) {
        //Find  Tk corresponding to alpha.  If none found return T1.
        DefaultMutableTreeNode Tk = null;
        CCSubTree CCTk = null;
        double ak;
        
        ListIterator CCSubTreesIt = CCSubTrees.listIterator(0);
        for (; CCSubTreesIt.hasNext(); ) {
            CCTk = (CCSubTree)CCSubTreesIt.next();
            Tk   = CCTk.getTree();
            ak   = CCTk.getAlpha();
            if (alpha < ak) { return Tk;}
        }

        // return T1 otherwise
        CCTk = (CCSubTree)CCSubTrees.get(0);
        Tk   = CCTk.getTree();
        return Tk;
    }
    
    public static LinkedList getAlphaGeometricMeans (LinkedList CCSubTrees) {
        // calculates geometric means ak' = sqrt(ak*ak+1) for CCSubTrees
        // returns results in LinkedList of CCSubTrees of ak' and Tk pairs
        // if CCSubTrees is empty, return null
        LinkedList akprimeList = new LinkedList();

        double ak, akplus1, akprime;
        DefaultMutableTreeNode Tk = null;
        CCSubTree CCTk            = null;
        CCSubTree CCTkplus1       = null;
        
        int i;
        int sizeminus1 = CCSubTrees.size() - 1;
        ListIterator CCSubTreesIt = CCSubTrees.listIterator(0);
        for (i = 0; i < sizeminus1 && CCSubTreesIt.hasNext(); i++) {
            CCTk      = (CCSubTree)CCSubTreesIt.next();
            CCTkplus1 = (CCSubTree)CCSubTreesIt.next();
            CCSubTreesIt.previous();
            Tk        = CCTk.getTree();
            ak        = CCTk.getAlpha();
            akplus1   = CCTkplus1.getAlpha();
            akprime   = Math.pow(ak*akplus1, 0.5);
            akprimeList.add(new CCSubTree(Tk, akprime));
        }

        if (CCSubTrees.size() == 1) {
            akprimeList.add(CCSubTrees.get(0));
        } else if (CCSubTrees.size() == 0) {
            return null;
        }
        return akprimeList;
        
    }

    public static LinkedList[] getCVSets (LinkedList X, int V) {
        // divide X into V equal parts randomly
        // if X.size() < V return null
        if (X.size() < V) {return null;}
        LinkedList[] CVSets = new LinkedList[V];
        int v, r, i;
        for (v = 0; v < V; v++) {
            CVSets[v] = new LinkedList();
        }
        
        // copy X to Y
        LinkedList Y = new LinkedList();
        ListIterator Xit = null;
        for (Xit = X.listIterator(0); Xit.hasNext(); ) {
            Y.add(Xit.next());
        }
        Xit = null;
        
        // System.currentTimeMillis()
        for (v = 0; Y.size() > 0; v++){
            i = v % V;
            r = rNumGenerator.nextInt(Y.size());
            // randomly pick r and remove r from Y add to CVSets[v]
            CVSets[i].add(Y.remove(r));
        }
        return CVSets;
    }

    public LinkedList minCostComplexitySubTrees (DefaultMutableTreeNode node) {
        // - Find the minimal Cost Complexity SubTrees, T(a) and a, for tree rooted at node
        // -
        LinkedList subTrees = new LinkedList();
        double a;
        DefaultMutableTreeNode Ta = null;
        CCSubTree minCCTree = null;

        // print T0
        // System.out.println("T0 leaf cnt " + node.getLeafCount());
        
        // a1 = 0;
        // Find T1 by merging terminal nodes such tL, tR are terminal nodes with parent t
        // and R(t) = R(tL) + R(tR)
        a = 0;        
        Ta = findT1(node);



        minCCTree = new CCSubTree(Ta, a);

        int i = 1;
        // Find all subsequent subtrees Ta
        while (minCCTree != null) {
            // print Ta
            //System.out.println("T" +i + " leaf cnt " + (minCCTree.getTree()).getLeafCount()+ " alpha "+minCCTree.getAlpha());
            subTrees.add(minCCTree);
            minCCTree = minCostComplexityTree(minCCTree);
            i++;
        }
        return subTrees;
        
    }

    public CCSubTree minCostComplexityTree(CCSubTree minCCTreeTk){
        // find Tkplus1 and a
        // return null if Tkplus1 does not exist
        DefaultMutableTreeNode Tk = minCCTreeTk.getTree();
        CCSubTree Tkplus1MinCCTree = null;
        DefaultMutableTreeNode Tkplus1 = null;
        double akplus1;

        if (Tk.isLeaf()) {return null;}
        
        Tkplus1 = cloneTree(Tk);
        LinkedList minSubTrees = new LinkedList();
        DefaultMutableTreeNode t;
        double tAlpha;
        double aMin = Double.MAX_VALUE;
        assert (Double.MAX_VALUE < Double.POSITIVE_INFINITY);
        int childrenCount = Tkplus1.getChildCount();
        int i;
        
        // - find all subtrees of Tk which are not terminal nodes
        //   and have min alpha.
        Enumeration allChildren = Tkplus1.breadthFirstEnumeration();
        for (;allChildren.hasMoreElements();) {
            t = (DefaultMutableTreeNode)allChildren.nextElement();
            if (t.isLeaf()) {continue;}
            tAlpha = criticalAlphaValue(t);
            if (tAlpha < aMin) {
                minSubTrees.clear();
                minSubTrees.add(t);
                aMin = tAlpha; 
            } else if (tAlpha == aMin && aMin != Double.MAX_VALUE ) {
                minSubTrees.add(t);
            }
        }
        
        if (minSubTrees.size() == 0) {return Tkplus1MinCCTree;}

        // - remove all subtrees with min alphas from Tk
        ListIterator minSubTreesIt = null;
        for (minSubTreesIt = minSubTrees.listIterator(0); minSubTreesIt.hasNext();) {
            t = (DefaultMutableTreeNode)minSubTreesIt.next();
            pruneChildren(t);
        }
        minSubTreesIt = null;
        Tkplus1MinCCTree = new CCSubTree(Tkplus1, aMin);
        return Tkplus1MinCCTree;
        
    }

    public double criticalAlphaValue (DefaultMutableTreeNode node) {
        // returns infinity is node is terminal node, i.e. leaf node
        // otherwise calc and return critical alpha

        double alpha = Double.POSITIVE_INFINITY;
        if (node.isLeaf()) {return alpha;}

        double Rt, RTt,Tt;
        Tt = node.getLeafCount();
        DTNode userObject = (DTNode)node.getUserObject();
        Rt = userObject.getNodeCost();
        RTt = terminalNodeCost(node);
        alpha = (Rt - RTt)/(Tt - 1);
        // if (alpha < 0){System.out.println("RTt: " + RTt + " Rt: " + Rt + " Tt: " + Tt + " alpha: " + alpha);}
        assert !(alpha == Double.NaN);
        return alpha;
    }

    private DefaultMutableTreeNode findT1 (DefaultMutableTreeNode node) {
        // Find T1 by merging terminal nodes such tL, tR are terminal nodes with parent t
        // and R(t) = R(tL) + R(tR)
        // if node is T1, return node
        
        DefaultMutableTreeNode T1 = cloneTree(node);
        
        // - while found >= 1 case of following:
        //   - iterate over all terminal nodes 
        //     - look for case where R(t) = R(tL) + R(tR) and merging them
        DefaultMutableTreeNode curr, parent;
        boolean found = true;
        double Rt, sumTerminals;

        while (found) {
            found = false;
            // if T1 is leaf, only root node in T1 -- done
            if (T1.isLeaf()) {return T1;}
            for (curr = T1.getFirstLeaf(); curr != null && !curr.isRoot(); curr = curr.getNextLeaf()) {
                assert (curr.isLeaf());
                parent = (DefaultMutableTreeNode)curr.getParent();
                assert  (parent !=null && !parent.isLeaf());
                assert (parent.getLeafCount() ==  parent.getChildCount());
                Rt = nodeCost(parent);
                sumTerminals = terminalNodeCost(parent);
                assert (Rt > sumTerminals);
                if (Rt == sumTerminals) {
                  // prune children from parent, which are also it's terminal nodes
                  pruneChildren(parent);
                  found = true;
                }
                curr = parent;
            }
        }
        return T1; 
    }
    
    public static void pruneChildren(DefaultMutableTreeNode node) {
        // prunes children from node making it a leaf node
        //   - must reset userobject (set attribute = null, set label = majority)
        // if node.isLeaf() no changes are necessary

        if (node.isLeaf()) {return;}
        node.removeAllChildren();
        DTNode userObject = (DTNode)node.getUserObject();
        userObject.setAttribute(null);
        userObject.setLabel(userObject.getMajority());
        return;
    }

    public static double nodeCost (DefaultMutableTreeNode node) {
        // returns node cost for leaf node or inner node
        DTNode userObject = (DTNode)node.getUserObject();
        return userObject.getNodeCost();
    }

    private static double terminalNodeCost(DefaultMutableTreeNode node) {
        // - sum cost of terminal nodes
        // - if node is leaf, return node cost 

        double cost = 0;
        DefaultMutableTreeNode curr;
        DTNode userObject;
        for (curr = node.getFirstLeaf(); curr != null; curr = curr.getNextLeaf()) {
            userObject = (DTNode)curr.getUserObject();
            cost += userObject.getNodeCost();
        }
        return cost;
    }

    private static DefaultMutableTreeNode cloneNode (DefaultMutableTreeNode node) {
        // returns a shallow duplicate of node without parent or children, but with user object if any

        DefaultMutableTreeNode dup = new DefaultMutableTreeNode();
        DTNode userObject = (DTNode)node.getUserObject();
        dup.setUserObject(userObject.clone());
        return dup;
    }

    private static DefaultMutableTreeNode cloneTree (DefaultMutableTreeNode node) {
        // makes tree clone of node
        // treats node as root, worries only about children if any
        
        DefaultMutableTreeNode dup = cloneNode(node);
        // if node is leaf, done.  return dup
        if (node.isLeaf()) {return dup;}

        int numChildren = node.getChildCount();
        DefaultMutableTreeNode child = null;
        for (int i = 0; i < numChildren; i++) {
            child = (DefaultMutableTreeNode)node.getChildAt(i);
            dup.add(cloneTree(child));
        }
        
        return dup;
    }

    private static void printNode(DefaultMutableTreeNode node){
        Enumeration subtree = node.breadthFirstEnumeration();
        DefaultMutableTreeNode currentNode = null;
        
        int i;
        for (i = 0; subtree.hasMoreElements(); i++) {
            currentNode = (DefaultMutableTreeNode)subtree.nextElement();
            if (currentNode.isLeaf()) {
                System.out.println("node " + i + "at level " + currentNode.getLevel() + "is leaf " );
            } else {
                System.out.println("node " + i + " at level " + currentNode.getLevel() + " ");
            }
        }
    }
    
    private static int getTreeSize(DefaultMutableTreeNode node){
        Enumeration subtree = node.breadthFirstEnumeration();
        int i;
        for (i = 0; subtree.hasMoreElements(); i++) {
            subtree.nextElement();
        }
        return i;
    }

    private void generateTree(LinkedList X, DefaultMutableTreeNode parent, double N) {
        generateTreeCount++;
        double zero     = 0;
        DTNode dtNode = new DTNode();
        cost(X,N,dtNode);

        double nodeEntr = nodeImpurity(X);
        if (nodeEntr == zero || height == generateTreeCount) {
            // create a leaf labelled by majority class in X
            dtNode.setLabel(dtNode.getMajority());
            parent.setUserObject(dtNode);
            dtNode = null;
            return;
        }
        SplitAttribute s = splitAttribute(X);
        // - if SplitAttribute is null, then cannot divide X further
        // - find majorityClass and create class node
        if (s == null) {
            dtNode.setLabel(dtNode.getMajority());
            parent.setUserObject(dtNode);
            dtNode = null;
            return;
        }
        LinkedList[] Xi = s.getXs();
        // check that impurity drops at each step.  It does.
        // double impurityChange =  nodeEntr - splitImpurity(Xi, X.size());
        // if (impurityChange < zero) { System.out.println("change in impurity" + String.valueOf(impurityChange) );}
        dtNode.setAttribute(s.getAttribute());
        parent.setUserObject(dtNode);
        dtNode = null;
        DefaultMutableTreeNode child = null;
        for (int i = 0; i < Xi.length; i ++) {
            // create node
            child = new DefaultMutableTreeNode();
            parent.add(child);
            generateTree(Xi[i], child, N);
        }
        
    }

    private double cost(LinkedList X, double N, DTNode node){
        return misclassificationRate(X, N, node);
    }

    private double misclassificationRate(LinkedList X, double N, DTNode node){
        // - calculates misclassificationError for a node with X examples
        // - find classLabel for node using majority rule
        // - 1/Nm * Sum [i in Rm] (Indicator(yi != k(m))) = 1 - pmk(m)
        // - Nm = number of observation in node m, i.e. X.size()
        // - i  = observation i in node m represented by region Rm
        // - yi = class label of observation i
        // - k(m) = the majority class in node m
        // - pmk(m) = 1/Nm * Sum [i in Rm](Indicator (yi = k(m)))
        // - fail is X.size() is 0

        LinkedList labels = features.all[0];
        double [] labelCounts = new double [labels.size()];
        Example ex = null;
        double Nm = 0;

        int i,k;
        ListIterator Xit = null;
        for (Xit = X.listIterator(0); Xit.hasNext();) {
            ex = (Example) Xit.next();
            labelCounts[ex.label] += ex.weight;
            Nm += ex.weight;
        }
        Xit = null;
        int majority = 0;
        for (k = 0; k < labelCounts.length; k++) {
            if (labelCounts[k] > labelCounts[majority] ) {
                majority = k;
            }
        }
        double poft = Nm/N;
        double pmk =labelCounts[majority] / Nm;
        double roft = (1 - pmk);
        double rate = roft * poft;
        // System.out.println("rate: " + rate + " Nm: " + Nm + " N: " + N + " majority cnt: " + labelCounts[majority]);
        node.setNodeCost(rate);
        node.setMajority(new Label(majority));
        return rate;

    }

    private Label majorityClass(LinkedList X) {
        // - pick default label at 0.
        // - the default is used if there is no majority label

        Label l = null;
        LinkedList labels = features.all[0];
        int max = 0; //rNumGenerator.nextInt(labels.size());
        double [] labelCounts = new double [labels.size()];
        Example ex = null;
        ListIterator Xit = null;
        for (Xit = X.listIterator(0); Xit.hasNext();) {
            ex = (Example)Xit.next();
            labelCounts[ex.label] += ex.weight;
        }
        Xit = null;
        for (int j = 0; j < labelCounts.length; j++) {
            if (labelCounts[j] > labelCounts[max] ) {
                max = j;
            }
        }
        l = new Label(max);
        return l ;
    
    }

    private SplitAttribute splitAttribute(LinkedList X) {
        // - this method finds which attribute and attribute value
        //   to split on
        // - it test an attribute with all possible values that
        //   and calculates the impurity
        // - chose split with the least impurity
        // - returns none if there is no possible attribute value pair
        //   that can split this X further
        
        double minEnt = Double.MAX_VALUE;
        double splitEnt;
        double Nm = X.size();
        int d;
        double value;
        SplitAttribute bestSplitAttr = null;
        SplitAttribute s = null;

        // - split on each attribute and attribute value
        //   and calc impurity.
        // - pick bestSplitAttr as the one with the min Entropy
        //   because gives the biggest drop in entropy
        for (d = 1; d < features.all.length ; d++) {
            for(int i = 0; i < features.all[d].size(); i++) {
                value = ((Integer)features.all[d].get(i)).doubleValue();
                s = split(X, d, value);
                if (s == null) {
                    // - X could not be divided further by this attribute
                    //   and value combination, so try another combination
                    continue;
                }
                splitEnt = splitImpurity(s.getXs(), Nm);
                assert (!Double.isNaN(splitEnt));
                if (splitEnt < minEnt) {
                    minEnt = splitEnt;
                    bestSplitAttr = s;
                }
                s = null;
            }
        }
        return bestSplitAttr;
        
    }

    private SplitAttribute split(LinkedList X, int d, double value) {
        // split X into X1, X2 
        // if attribute d for Example ex is < = value, then put in X1 else put in X2
        // if X cannot be separated by attribute and value pair, return null
        SplitAttribute s = null;
        // binary splits so array has size 2
        LinkedList[] Xs = new LinkedList[2];

        int i, j;
        // init Xs array
        for (i = 0; i < Xs.length; i++) {
            Xs[i] = new LinkedList();
        }

        Example ex = null;
        ListIterator Xit = null;
        for (Xit = X.listIterator(0);  Xit.hasNext();) {
            ex = (Example) Xit.next();
            // if (d == 17) {System.out.println(ex.toString());}
            if (ex.attrs[d] <= value) {
                Xs[0].add(ex);
            } else {
                Xs[1].add(ex);
            }
        }
        Xit = null; ex = null;
        // this attribute and value pair do not divide the data further so return null
        for (i = 0; i < 2; i++) {
            if (Xs[i].size() == 0) {
                Xs[0] = null; Xs[1] = null;
                return null;
            }
        }

        // init Attribute for SplitAttribute
        LinkedList a = new LinkedList();
        a.add(new Double(value));
        Attribute attr = new Attribute(d, a);

        s = new SplitAttribute(Xs, attr);
        return s;
    }

    private double nodeImpurity(LinkedList X) {
        double impurity = 0;
        if (impurityMeasure.equals(GINI)) {
            impurity = nodeGiniMeasure(X);
        } else if (impurityMeasure.equals(ENTROPY)) {
            impurity = nodeEntropy(X);
        } else {
            System.out.println("invalid impurity measure");
            System.exit(1);
        }
        return impurity;
    }

    private double nodeEntropy(LinkedList X) {
        // Nm      = number of training instances reaching node
        // Nim     = number of training instances reaching node m that 
        //           belong to class i
        // pim     = Nim / Nm
        // Entropy = Sum from i=1 to K (-pim log pim) -- for k classes
        double Nm = 0;
        int K = features.all[0].size();
        double[] Nim = new double[K];
        Example ex;
        int i,k;
        // init all values of Nim
        for (i = 0; i < K; i++ ) {
            Nim[i] = 0;
        }
        ListIterator Xit = null;
        for (Xit = X.listIterator(0); Xit.hasNext() ;) {
            ex = (Example)Xit.next();
            Nim[ex.label] += ex.weight;
            Nm += ex.weight;
        }
        Xit = null;
        double entropy = 0;
        double zero = 0;
        double pim;
        double ln2 = Math.log(2);
        for (i = 0; i < K; i++){
            pim = Nim[i]/Nm;
            if (pim != zero) {
                entropy += pim * (Math.log(pim)/ln2);
            }
        }
        entropy *= -1;
        return entropy;
    }

    private double nodeGiniMeasure(LinkedList X) {
        // Nm      = number of training instances reaching node
        // Nim     = number of training instances reaching node m that 
        //           belong to class i
        // pim     = Nim / Nm
        // Sum from i=1 to K where i != j (pim * pjm) -- for k class
        double impurity = 0;
        double Nm = 0;
        int K = features.all[0].size();
        double[] Nim = new double[K];
        Example ex;
        int i,k;
        // init all values of Nim
        for (i = 0; i < K; i++ ) {
            Nim[i] = 0;
        }
        ListIterator Xit = X.listIterator(0);
        for (; Xit.hasNext() ;) {
            ex = (Example)Xit.next();
            Nim[ex.label] += ex.weight;
            Nm += ex.weight;
        }
        Xit = null;
        double pim;
        double pjm;
        for (i = 0; i < K; i++){
            pim = Nim[i]/Nm;
            pjm = (Nm - Nim[i])/Nm;
            impurity += pim * pjm;
        }
        return impurity;
    }

    private double splitImpurity(LinkedList[] Xs, double Nm) {
        double impurity = 0;
        if (impurityMeasure.equals(GINI)) {
            impurity = splitGiniMeasure(Xs);
        } else if (impurityMeasure.equals(ENTROPY)) {
            impurity = splitEntropy(Xs);
        } else {
            System.out.println("invalid impurity measure");
            System.exit(1);
        }
        return impurity;
    }

    private double splitEntropy(LinkedList[] Xs) {
        //  Nm = number of instances at node m
        //  Nmj = number of instances at node m that take branch j
        //  Nimj = number of instances from Nmj belonging to class i
        //  pimj = Nimj/Nmj
        //  Entropy = SUM[j=1 to N]( (Nmj/Nm) * SUM[i=1 to K]( pimj log pimj) )
        /*
        int N = Xs.length;
        int K = features.all[0].size();
        int i,j,l;
        double zero = 0;
        
        double[] Nmj = new double[N];
        // init Nmj array values
        for (j=0; j < N; j++) {
            Nmj[j] = Xs[j].size();
        }
        
        double[][] Nimj = new double[K][N];
        // init Nimj array values to zero
        for (i=0; i < K; i++) {
            for(j=0; j < N; j++){
                Nimj[i][j] = 0;
            }
        }
        // count classes for each branch, i.e. Nimj
        Example ex;
        for(j=0; j < N; j++) {
            for(l=0; l < Xs[j].size(); l++){
                ex = (Example)Xs[j].get(l);
                Nimj[ex.label][j]++;
            }
        }
        double pimj    = 0;
        double pimjSum = 0;
        double entropy = 0;
        double ln2 = Math.log(2);

        for (j=0; j< N; j++) {
            pimjSum = 0;
            for(i=0; i < K; i++) {
                pimj = Nimj[i][j]/Nmj[j];
                if (pimj != zero) {
                    pimjSum += pimj * (Math.log(pimj)/ln2);
                }
            }
            entropy += (Nmj[j] / Nm) * pimjSum;
        }
        entropy  *= -1;
        return entropy;
        */
        double impurity = 0;
        int N = Xs.length;
        double Nm = 0;
        int j;
        
        double[] Nmj = new double[N];
        for (j=0;j<N;j++) {
            Nmj[j] = 0;
        }
        
        // init Nmj array values
        ListIterator XsIt = null;
        Example ex = null;
        for (j=0; j < N; j++) {
            XsIt = Xs[j].listIterator(0);
            for (;XsIt.hasNext();) {
                ex = (Example)XsIt.next();
                Nmj[j] += ex.weight;
                Nm += ex.weight;
            }
        }

        double pimjSum = 0;

        for (j=0; j< N; j++) {
            pimjSum = nodeEntropy(Xs[j]);
            impurity += (Nmj[j] / Nm) * pimjSum;
        }
        return impurity;      
    }

    private double splitGiniMeasure(LinkedList[] Xs) {
        //  Nm   = number of instances at node m
        //  Nmj  = number of instances at node m that take branch j
        //  Nimj = number of instances from Nmj belonging to class i
        //  pimj = Nimj/Nmj
        //  Entropy = SUM[j=1 to N]( (Nmj/Nm) * SUM[i=1 to K where i != l]( pimj * plmj) )

        double impurity = 0;
        int N = Xs.length;
        double Nm = 0;
        int j;
        
        double[] Nmj = new double[N];
        for (j=0; j < N ;j++){
            Nmj[j] = 0;
        }
        
        ListIterator XsjIt = null;
        Example ex = null;
        // init Nmj array values
        for (j=0; j < N; j++) {
            XsjIt = Xs[j].listIterator(0);
            for (;XsjIt.hasNext();) {
                ex = (Example)XsjIt.next();
                Nmj[j] += ex.weight;
                Nm += ex.weight;
            }
        }

        double pimjSum = 0;

        for (j=0; j< N; j++) {
            pimjSum = nodeGiniMeasure(Xs[j]);
            impurity += (Nmj[j] / Nm) * pimjSum;
        }
        return impurity;
    }

    public int predict(Example ex) {
        int label = -1;
        
        Label classLabel = traverse(root, ex);
        if (classLabel != null) {
            label = (int)classLabel.getLabel();
        } else {
            System.out.println("no match for ex, so predict does not label");
            System.exit(1);
        }
        return label;
    }

    public int predict(Example ex, DefaultMutableTreeNode node) {
        int label = -1;
        
        Label classLabel = traverse(node, ex);
        if (classLabel != null) {
            label = (int)classLabel.getLabel();
        } else {
            System.out.println("no match for ex, so predict does not label");
            System.exit(1);
        }
        return label;
    }

    public int runSample (LinkedList testset, DefaultMutableTreeNode tree) {
        int predValue, trueValue, i;
        int misClassificationCount = 0;

        Example ex = null;
        ListIterator testsetIt = null;
        for (testsetIt = testset.listIterator(0); testsetIt.hasNext();  ) {
            ex      = (Example)testsetIt.next();
            predValue = predict(ex,tree);
            trueValue = ex.label;
            if (predValue != trueValue) {
                misClassificationCount += ex.weight;
            }
        }
        return misClassificationCount;
    }
    private boolean match (DefaultMutableTreeNode node, Example ex) {
        // node is garanteed not to be a leaf node
        assert (!node.isLeaf());
        boolean matched = false;

        // get node attribute information
        DTNode dtnode = (DTNode)node.getUserObject();
        Attribute attribute = dtnode.getAttribute();
        int attrIndex = attribute.getAttributeIndex();
        LinkedList attrValues = attribute.getAttributeValues();


        Double exValue = new Double(ex.attrs[attrIndex]);
        Double attrValue = (Double)attrValues.get(0);
        if (exValue.doubleValue() <= attrValue.doubleValue()) {
            matched = true;
        }
        return matched;
    }

    private Label traverse(DefaultMutableTreeNode node, Example ex) {
        // printNode(node);
        Label label = null;
        if (node.isLeaf()) {
            label = ((DTNode)node.getUserObject()).getLabel();
            return label;
        }
        boolean matched = match(node, ex);
        int i = 0;
        if (!matched) {
            i = 1;
        }
        DefaultMutableTreeNode child = (DefaultMutableTreeNode)node.getChildAt(i);
        return traverse(child, ex);

    }

    public double totalWeight(LinkedList X) {
        ListIterator i = X.listIterator(0);
        double total = 0;
        Example ex = null;
        for (;i.hasNext();) {
            ex = (Example)i.next();
            total += ex.weight;
        }
        return total;
    }
    public int[][] runClassificationTest(LinkedList testset) {
        int i,j;
        int predValue;
        int trueValue;
        // true value, predicted value
        // 00(true neg) 01(false pos)
        // 10(false neg) 11(true pos)
        int[][] confusion = new int [2][2];
        for (i=0; i < 2; i++) {
            for (j=0;j<2;j++) {
                confusion[i][j]= 0;
            }
        }
        Example test = null;
        ListIterator testsetIt = null;
        for (testsetIt =  testset.listIterator(0); testsetIt.hasNext(); ) {
            test      = (Example)testsetIt.next();
            predValue = predict(test);
            trueValue = test.label;
            confusion[trueValue][predValue] ++;
        }     
        return confusion;
    }
    public static void test1() {
    
    }
}
