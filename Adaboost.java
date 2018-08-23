//
//  Adaboost.java
//
//  Created by Veronica Mayorga
//  Copyright.  All rights reserved.
//
import java.util.LinkedList;
import java.util.ListIterator;
import java.lang.Math;
import common.Multinomial;
import common.Util;

public class Adaboost {

    // contains CART objects
    private LinkedList hypothesis       = new LinkedList();
    // contain doubles
    private LinkedList trainErrorRate   = new LinkedList();
    private LinkedList testError        = new LinkedList();
    boolean zeroTrainErrorRate          = false;
    double e;

    private String boostWith            = "sampling";
    private Features features           = null;
    private String CART_measure         = null;
    private boolean CART_prune          = false;
    private int CART_V                  = 10;
    private int CART_height                  = -1;

    public static String SAMPLING       = "sampling";
    public static String WEIGHTING      = "weighting";
    
    // consider using array of Examples for X instead

    public Adaboost (LinkedList X, LinkedList P, Features f, String measure, boolean prune, int V, int h, String btype) {
        Util.initRandom(false);
        setCartArgs(f, measure, prune, V, h);
        generateCARTMasterHypothesis(X, P);
        boostWith = btype;
        e = 1/(2 * X.size());
    }    
    public void printTrainErrorRate() {
        System.out.println("");
        ListIterator trainErrorRateIt = trainErrorRate.listIterator(0);       
        for( ; trainErrorRateIt.hasNext(); ) {
            System.out.print(" " + ((Double)trainErrorRateIt.next()).doubleValue());    
        }
        System.out.println("");        
    }
    public void printTestErrorRates() {        
        System.out.println("");
        ListIterator testErrorIt = testError.listIterator(0);     
        for(; testErrorIt.hasNext(); ) {
            System.out.print(" " + ((Double)testErrorIt.next()).doubleValue());    
        }
        System.out.println("");        
    }
    public void printWeights(double[] w) {        
        System.out.println("");        
        for(int i = 0; i < w.length; i++) {
            System.out.print(" " + w[i]);    
        }
        System.out.println("");        
    }
    public void setCartArgs(Features f, String measure, boolean prune, int V, int h) {
        features     = f;
        CART_measure = measure;
        CART_prune   = prune;
        CART_V       = V;
        CART_height  = h; 
    }
    public void generateCARTMasterHypothesis (LinkedList X, LinkedList P) {
        hypothesis.clear();
        trainErrorRate.clear();
        zeroTrainErrorRate  = false;
        double zero         = 0;
        
        Example trainExample = null;
        LinkedList Xr        = null;
        LinkedList Pr        = null;
        CART CARTt          = null;
        Multinomial dt      = null;
        double epsilon      = .000001;
        double prevEpsilon  = 0;
        double alpha;
        int sample, t;
        int[] htofi         = new int[X.size()];
        int[] yi            = new int[X.size()];
        
        double[] weightX     = new double[X.size()];
        double[] weightP     = new double[P.size()];
        double totalWeightX  = 0;
        double totalWeightP  = 0;
        
        // init all weights to 1 and totalWeight to n, number of samples
        int i;
        for (i=0; i < weightX.length; i++) {
            weightX[i] = 1;
        }
        for (i=0; i < weightP.length; i++) {
            weightP[i] = 1;
        }
        weightX = renormalize(weightX);
        weightP = renormalize(weightP);
        
        Object[] XArray = new Example[X.size()];
        Object[] PArray = new Example[P.size()];
        XArray = X.toArray();
        PArray = P.toArray();
        Example ex = null;
        boolean start = true;
        int randomNum;
        int iteration = 0;
        while (start ||(epsilon != 0 && epsilon < .5 && epsilon != prevEpsilon)) {
        //while (start || iteration == 20||(epsilon < .5 && epsilon != prevEpsilon)) {
            iteration++;
            start = false;
            prevEpsilon = epsilon;
            Xr = new LinkedList();
            Pr = new LinkedList();
            if (boostWith.equals(SAMPLING)) {
                // form filtered training set by sampling from training set.
                dt = new Multinomial(weightX);
                for (i=0; i < XArray.length; i++) {
                    randomNum = dt.sample();
                    // System.out.print(" "+ randomNum);
                    Xr.add(XArray[randomNum]);
                }
                // System.out.println("\n");
                if (P.size() != 0) {
                    dt = new Multinomial(weightP);
                    for (i=0; i < PArray.length; i++) {
                        randomNum = dt.sample();
                        Pr.add(XArray[randomNum]);
                    }
                }
            } else if (boostWith.equals(WEIGHTING)) {
                for (i=0; i< XArray.length; i++) {
                    ex = (Example)XArray[i];
                    ex.weight = weightX[i];
                    Xr.add(ex);
                }
                for (i=0; i< PArray.length; i++) {
                    ex = (Example)PArray[i];
                    ex.weight = weightP[i];
                    Pr.add(ex);
                }
            } else {
                System.out.println("invalid boosting parameter for boostWith: " + boostWith);
                System.exit(1);
            }
            // train weak learner on Xr, with Pr pruning
            CARTt = new CART(Xr, Pr, features, CART_measure, CART_prune, CART_V, CART_height);
            hypothesis.add(CARTt);
            // get training error epsilon
            epsilon = 0; ex = null;
            ListIterator XrIt = Xr.listIterator(0);
            for (i=0; XrIt.hasNext(); i++ ) {
                ex = (Example)XrIt.next();
                htofi[i]      = CARTt.predict(ex);
                yi[i]         = ex.label;
                if (htofi[i] != yi[i]) {epsilon += weightX[i];}
            }
            ex = null;Xr = null; XrIt = null;
            // if (epsilon == zero) {zeroTrainErrorRate = true;}
            trainErrorRate.add(new Double(epsilon));
            // increase weight on correctly labeled examples
            // alpha = Math.log(1-epsilon) - Math.log(epsilon);
            for (i=0; i < weightX.length; i++ ) {
                if (htofi[i] == yi[i]) {weightX[i] *= epsilon/(1- epsilon);}
            }
            // renormalize weights so sum to 1
            weightX = renormalize(weightX);

            ex = null;
            ListIterator PrIt = Pr.listIterator(0);
            for (i=0; PrIt.hasNext(); i++ ) {
                ex = (Example)PrIt.next();
                htofi[i]      = CARTt.predict(ex);
                yi[i]         = ex.label;
                // increase weight on correctly labeled examples
                if (htofi[i] == yi[i]) {weightP[i] *= epsilon/(1- epsilon);}
            }
            ex = null;Pr = null; PrIt = null;
            // renormalize weights so sum to 1
            weightP = renormalize(weightP);


        }
        XArray = null;
        PArray = null;
        if (epsilon >= 0.5) {
            // dont include last classifier if error >= 0.5
            hypothesis.removeLast();
            trainErrorRate.removeLast();
        }
        
        if (epsilon == 0 && hypothesis.size() > 1) {
            // dont include last classifier if error
            hypothesis.removeLast();
            trainErrorRate.removeLast();
            //System.out.println("remove 0 hyp"+ trainErrorRate.get(hypothesis.size() -1));
        }
        
        printTrainErrorRate();
        // printWeights(weight);
        
    }
    public double error (Example ex) {
        double error = 0;
        return error;
    }
    public double[] renormalize (double[] w) {
        double total = sum(w);
        for (int i = 0; i < w.length; i++) {
            w[i] = w[i]/total;
        }
        return w;
    }
    public double sum(double [] w) {
        double total = 0;
        for(int i = 0; i < w.length; i++) {
            total += w[i];
        }
        return total;
    }
    public void resetTestError () {
        testError.clear();
    }
    public int predict(Example ex) {
        CART ht             = null;
        double vote         = 0;
        double epsilon, alpha;
        double zero         = 0;
        double distribution = 0;
        
        int t;
        int htofi;
        
        // to avoid dividing by 0 when epsilon = 0
        // add e to num and dem of log calc
        // where e = 1/(2*#samples)
        ListIterator hypothesisIt = hypothesis.listIterator(0);
        ListIterator trainErrorRateIt = trainErrorRate.listIterator(0);
        for(t = 0; hypothesisIt.hasNext() && trainErrorRateIt.hasNext(); t++) {
            ht      = (CART)hypothesisIt.next();
            htofi   = ht.predict(ex);
            epsilon = ((Double)trainErrorRateIt.next()).doubleValue();
            // System.out.println("epsilon in master hyp " + epsilon);
            // increment vote
            //vote         += Math.log((1-epsilon + e)/(epsilon + e)) * htofi;
            //distribution += Math.log((1-epsilon + e)/(epsilon + e)) * 0.5;
            vote         += Math.log((1-epsilon)/(epsilon)) * htofi;
            distribution += Math.log((1-epsilon)/(epsilon)) * 0.5;
        }
        if (vote > distribution) {
            return 1;
        } else {
            return 0;
        }
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
        ListIterator testsetIt = testset.listIterator(0);
        for (; testsetIt.hasNext(); ) {
            test      = (Example)testsetIt.next();
            predValue = predict(test);
            trueValue = test.label;
            confusion[trueValue][predValue]++;
        }
        return confusion;
    }    
}
