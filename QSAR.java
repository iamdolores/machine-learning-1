//
//  QSAR.java
//
//  Created by Veronica Mayorga
//  Copyright (c). All rights reserved.
//
import java.util.*;
import java.util.LinkedList;

public class QSAR {
        public static int pyrDataFolds          = 5;
        public static int triazineDataFolds     = 6;
        public static Features pyrFeatures      = new Features( Features.PYR_PROP);
        public static Features triazineFeatures = new Features(Features.TRIAZINE_PROP);
        public static String[] pyrPropPSTrain = {
           "uci-data/pyrimidines/prop/ps-train1.ind",
           "uci-data/pyrimidines/prop/ps-train2.ind",
           "uci-data/pyrimidines/prop/ps-train3.ind",
           "uci-data/pyrimidines/prop/ps-train4.ind",
           "uci-data/pyrimidines/prop/ps-train5.ind"
        };
        public static String[] pyrPropPruneSet = {
           "uci-data/pyrimidines/prop/pruneset1.ind",
           "uci-data/pyrimidines/prop/pruneset2.ind",
           "uci-data/pyrimidines/prop/pruneset3.ind",
           "uci-data/pyrimidines/prop/pruneset4.ind",
           "uci-data/pyrimidines/prop/pruneset5.ind"
        };
        public static String[] pyrPropTrainArff = {
           "uci-data/pyrimidines/prop/arff/train1.arff",
           "uci-data/pyrimidines/prop/arff/train2.arff",
           "uci-data/pyrimidines/prop/arff/train3.arff",
           "uci-data/pyrimidines/prop/arff/train4.arff",
           "uci-data/pyrimidines/prop/arff/train5.arff"
        };
        public static String[] pyrPropTestArff  = {
           "uci-data/pyrimidines/prop/arff/test1.arff",
           "uci-data/pyrimidines/prop/arff/test2.arff",
           "uci-data/pyrimidines/prop/arff/test3.arff",
           "uci-data/pyrimidines/prop/arff/test4.arff",
           "uci-data/pyrimidines/prop/arff/test5.arff"
        };
        public static String[] pyrPropTrain = {
           "uci-data/pyrimidines/prop/train1.ind",
           "uci-data/pyrimidines/prop/train2.ind",
           "uci-data/pyrimidines/prop/train3.ind",
           "uci-data/pyrimidines/prop/train4.ind",
           "uci-data/pyrimidines/prop/train5.ind"
        };
        public static String[] pyrPropTest  = {
           "uci-data/pyrimidines/prop/test1.ind",
           "uci-data/pyrimidines/prop/test2.ind",
           "uci-data/pyrimidines/prop/test3.ind",
           "qsar/uci-data/triazines/prop/ps-train3.ind",
           "uci-data/triazines/prop/ps-train4.ind",
           "uci-data/triazines/prop/ps-train5.ind",
           "uci-data/triazines/prop/ps-train6.ind"
        };
        public static String[] triazinePropPruneSet = {
           "uci-data/triazines/prop/pruneset1.ind",
           "uci-data/triazines/prop/pruneset2.ind",
           "uci-data/triazines/prop/pruneset3.ind",
           "uci-data/triazines/prop/pruneset4.ind",
           "uci-data/triazines/prop/pruneset5.ind",
           "uci-data/triazines/prop/pruneset6.ind"
         };
        public static String[] triazinePropTrain = {
           "uci-data/triazines/prop/train1.ind",
           "uci-data/triazines/prop/train2.ind",
           "uci-data/triazines/prop/train3.ind",
           "uci-data/triazines/prop/train4.ind",
           "uci-data/triazines/prop/train5.ind",
           "uci-data/triazines/prop/train6.ind"
        };
        public static String[] triazinePropTest  = {
           "uci-data/triazines/prop/test1.ind",
           "uci-data/triazines/prop/test2.ind",
           "uci-data/triazines/prop/test3.ind",
           "uci-data/triazines/prop/test4.ind",
           "uci-data/triazines/prop/test5.ind",
           "uci-data/triazines/prop/test6.ind"
        };

        public static String EMPTY = "";
        // true value, predicted value
        // 00(true neg) 01(false pos)
        // 10(false neg) 11(true pos)
        public static int[][] confusion; 

    public static void main (String args[])  throws Exception{
        String trainFile    = null;
        String testFile     = null;
        String pstrainFile  = null;
        String pruneFile    = null;
        String desc         = null;

        NaiveBayes nb       = null;  
        CART cart           = null;
        boolean t = true;
        boolean f = false;

        int i,h;
        int idataStep          = 5;
        int istart = 0;
        int iend   = istart + idataStep;
        
        boolean[] test = {false, true , true, true, true, 
                          false, false, true};

       for (i = istart; i < iend ; i++) {
           trainFile    = pyrPropTrain[i];
           testFile     = pyrPropTest[i];
           pstrainFile  = pyrPropPSTrain[i];
           pruneFile    = pyrPropPruneSet[i];
           
           if (f) {
           // run naive Bayes nb1
           desc = "\nNaiveBayes Pyr-Split " + i;
           doPyrNB(trainFile, testFile, desc);
           }

           if (f) {
           // run CART - CART.GINI, no pruning
           desc = "\nCART GINI TMax Pyr-Split " + i;
           doPyrCART(trainFile, EMPTY, testFile, desc, CART.GINI, false, 10, -1);
           }

           if (f) {
           // run CART - CART.GINI, pruning w/ PS
           desc = "\nCART GINI Pruning w/ PS Pyr-Split " + i;
           doPyrCART(pstrainFile, pruneFile, testFile, desc, CART.GINI, true, 10, -1);
           }

           if (f) {
           // run CART - CART.ENTROPY, no pruning
           desc = "\nCART ENTROPY TMax Pyr-Split " + i;
           doPyrCART(trainFile, EMPTY, testFile, desc, CART.ENTROPY, false, 10, -1);
           }

           if (f) {
           // run CART - CART.ENTROPY, pruning w/ PS
           desc = "\nCART ENTROPY Pruning w/ PS Pyr-Split " + i;
           doPyrCART(trainFile, pruneFile, testFile, desc, CART.ENTROPY, true, 10, -1);
           }

           if (f) {
           // run CART - CART.GINI, Pruned SD0 10 fold XValidation
           desc = "\nCART GINI Pruned SD0 10 fold XValidation Pyr-Split " + i;
           doPyrCART(trainFile, EMPTY,testFile, desc, CART.GINI, true, 10, -1);
           }

           if (f) {
           // run CART - CART.ENTROPY, Pruned SD0 10 fold XValidation
           desc = "\nCART ENTROPY Pruned SD0 10 fold XValidation Pyr-Split " + i;
           doPyrCART(trainFile, EMPTY, testFile, desc, CART.ENTROPY, true, 10, -1);
           }

           for (h = 20; h < 1800; h += 30) {
               if (t) {
               // run Adaboost by SAMPLING - CART.ENTROPY, no pruning
               desc = "\nAdaboost by SAMPLING CART ENTROPY TMax Pyr-Split " + i + " HEIGHT "+ h;
               doPyrAda(trainFile, EMPTY, testFile, desc, CART.ENTROPY, false, 10, h, Adaboost.SAMPLING);
               }

               if (t) {
               // run Adaboost by SAMPLING - CART.ENTROPY, pruning with PS
               desc = "\nAdaboost by SAMPLING CART ENTROPY pruning with PS Pyr-Split " + i + " HEIGHT "+ h;
               doPyrAda(pstrainFile, pruneFile, testFile, desc, CART.ENTROPY, true, 10, h, Adaboost.SAMPLING);
               }
            }

               if (f) {
               // run Adaboost by SAMPLING - CART.ENTROPY, no pruning
               desc = "\nAdaboost by SAMPLING CART ENTROPY TMax Pyr-Split " + i ;
               doPyrAda(trainFile, EMPTY, testFile, desc, CART.ENTROPY, false, 10, -1, Adaboost.SAMPLING);
               }

               if (f) {
               // run Adaboost by SAMPLING - CART.ENTROPY, pruning with PS
               desc = "\nAdaboost by SAMPLING CART ENTROPY pruning with PS Pyr-Split " + i ;
               doPyrAda(pstrainFile, pruneFile, testFile, desc, CART.ENTROPY, true, 10, -1, Adaboost.SAMPLING);
               }

           if (f) {
           // run Adaboost by WEIGHTING - CART.ENTROPY, no pruning
           desc = "\nAdaboost by WEIGHTING CART ENTROPY TMax Pyr-Split " + i;
           doPyrAda(trainFile, EMPTY, testFile, desc, CART.ENTROPY, false, 10, -1, Adaboost.WEIGHTING);
           }

           if (f) {
           // run Adaboost by WEIGHTING - CART.ENTROPY, pruning with PS
           desc = "\nAdaboost by WEIGHTING CART ENTROPY pruning with PS Pyr-Split " + i;
           doPyrAda(pstrainFile, pruneFile, testFile, desc, CART.ENTROPY, true, 10, -1, Adaboost.WEIGHTING);
           }

           if (f) {
           // run CART - CART.GINI, Pruned SD0 30 fold XValidation
           desc = "\nCART GINI Pruned SD0 30 fold XValidation Pyr-Split " + i;
           doPyrCART(trainFile, EMPTY, testFile, desc, CART.GINI, true, 30, -1);
           }

        }

        test[0] = false;  test[1] = true;  test[2] = true;  test[3] = true;  test[4] = true;
        test[5] = true;  test[6] = true; test[7] = false;
        int j;
        int jdataStep          = 0;
        int jstart = 4;
        int jend   = jstart + jdataStep;
        for (j = jstart; j < jend ; j++) {
           trainFile = triazinePropTrain[i];
           testFile  = triazinePropTest[i];
           pstrainFile  = triazinePropPSTrain[i];
           pruneFile    = triazinePropPruneSet[i];

           if (t) {
           // run naive Bayes nb1
           desc = "\nNaiveBayes Triazine-Split " + j;
           doTriazineNB(trainFile, testFile, desc);
           }

           if (t) {
           // run CART - CART.GINI, no pruning
           desc = "\nCART GINI TMax Triazine-Split " + j;
           doTriazineCART(trainFile, EMPTY, testFile, desc, CART.GINI, false, 10, -1);
           }

           if (t) {
           // run CART - CART.GINI, pruning w/PS
           desc = "\nCART GINI Pruning w/PS Triazine-Split " + j;
           doTriazineCART(pstrainFile, pruneFile, testFile, desc, CART.GINI, true, 10, -1);
           }

           if (t) {
           // run CART - CART.ENTROPY, no pruning
           desc = "\nCART ENTROPY TMax Triazine-Split " + j;
           doTriazineCART(trainFile, EMPTY, testFile, desc, CART.ENTROPY, false, 10, -1);
           }

           if (t) {
           // run CART - CART.ENTROPY, pruning w/PS
           desc = "\nCART ENTROPY Pruning w/PS Triazine-Split " + j;
           doTriazineCART(pstrainFile, pruneFile, testFile, desc, CART.ENTROPY, true, 10, -1);
           }

            if (f) {
           // run Adaboost CART - CART.GINI, no pruning
           desc = "\nAdaBoost CART GINI TMax Triazine-Split " + j;
           doTriazineAda(trainFile, EMPTY, testFile, desc, CART.GINI, false, 10, -1, Adaboost.SAMPLING);
           }

           if (f) {
           // run CART - CART.GINI, Pruned SD0 10 fold XValidation
           desc = "\nCART GINI Pruned SD0 10 fold XValidation Triazine-Split " + j;
           doTriazineCART(trainFile, EMPTY, testFile, desc, CART.GINI, true, 10, -1);
           }

           if (f) {
           // run CART - CART.GINI, Pruned SD0 30 fold XValidation
           desc = "\nCART GINI Pruned SD0 30 fold XValidation Triazine-Split " + j;
           doTriazineCART(trainFile, EMPTY, testFile, desc, CART.GINI, true, 30, -1);
           }

        }

        LinkedList trainset = null;
        LinkedList testset = null;
        double percent = 0.10;

        // create arffs for pyrimidine data
        if (f) {

           for (i = 0; i < pyrPropTrainArff.length; i++) {
                trainset = DataReader.PyrimidinesProp(pyrPropTrain[i]);
                DataWriter.writeArffFile(pyrPropTrainArff[i], trainset); 
                trainset = null;            
                testset = DataReader.PyrimidinesProp(pyrPropTest[i]);
                DataWriter.writeArffFile(pyrPropTestArff[i], testset); 
                testset = null;            
            }           
        }
         // create prune set for pyrimidine data
        if (f) {
            trainset = null;
            percent = 0.10;
            // Example ex = null;

           for (i = 0; i < pyrPropTrain.length; i++) {
                trainset = DataReader.PyrimidinesProp(pyrPropTrain[i]);
                /*
                System.out.println("read this file ");
                for (j = 0; j < trainset.size(); j++) {
                    ex = (Example)trainset.get(j);
                    System.out.println("line "+ j+ " "+ ex.toString());
                }
                System.out.println("end of this file");
                */
                DataWriter.randomSplitData(trainset, percent, pyrPropPruneSet[i],pyrPropPSTrain[i]); 
                trainset = null;            
            }           
        }
        // create prune set for triazine data
        if (f) {
            trainset = null;
            percent = 0.10;

            for (i = 0; i < triazinePropTrain.length; i++) {
                trainset = DataReader.TriazinesProp(triazinePropTrain[i]);
                DataWriter.randomSplitData(trainset, percent, triazinePropPruneSet[i],triazinePropPSTrain[i]); 
                trainset = null;            
            }           
        }

    }

    public static void doPyrNB(String trainFile, String testFile, String desc) throws Exception{
        LinkedList trainset = DataReader.PyrimidinesProp(trainFile);
        NaiveBayes nb       = new NaiveBayes(trainset, pyrFeatures);
        trainset            = null;
        LinkedList testset  = DataReader.PyrimidinesProp(testFile);
        confusion           = nb.runClassificationTest(testset);
        testset             = null;
        nb                  = null;
        System.out.println(desc);
        printConfusion(confusion);
        confusion          = null;
    }
    public static void doTriazineNB(String trainFile, String testFile, String desc) throws Exception{
        LinkedList trainset = DataReader.TriazinesProp(trainFile);
        NaiveBayes nb       = new NaiveBayes(trainset, triazineFeatures);
        trainset            = null;
        LinkedList testset  = DataReader.TriazinesProp(testFile);
        confusion           = nb.runClassificationTest(testset);
        testset             = null;
        nb                  = null;
        System.out.println(desc);
        printConfusion(confusion);
        confusion           = null;
    }
    public static void doPyrAda(String trainFile, String pruneFile, String testFile, String desc,
                                String measure, boolean pruning, int V, int h, String btype) throws Exception{
        LinkedList pruneset = null;
        if (pruneFile.equals(EMPTY)) {
            pruneset = new LinkedList();
        } else {
            pruneset = DataReader.PyrimidinesProp(pruneFile);
        }
        LinkedList trainset = DataReader.PyrimidinesProp(trainFile);
        Adaboost ada        = new Adaboost(trainset, pruneset, pyrFeatures, measure, pruning, V, h, btype);
        trainset            = null;
        LinkedList testset  = DataReader.PyrimidinesProp(testFile);
        confusion           = ada.runClassificationTest(testset);
        testset             = null;
        ada                 = null;
        System.out.println(desc);
        printConfusion(confusion);
        confusion           = null;
    }
    public static void doTriazineAda(String trainFile, String pruneFile, String testFile, String desc,
                                     String measure, boolean pruning, int V, int h, String btype) throws Exception{
        LinkedList pruneset = null;
        if (pruneFile.equals(EMPTY)) {
            pruneset = new LinkedList();
        } else {
            pruneset = DataReader.TriazinesProp(pruneFile);
        }
        LinkedList trainset = DataReader.TriazinesProp(trainFile);
        Adaboost ada       = new Adaboost(trainset, pruneset, triazineFeatures,measure, pruning, V, h, btype);
        trainset           = null;
        LinkedList testset = DataReader.TriazinesProp(testFile);
        confusion          = ada.runClassificationTest(testset);
        testset            = null;
        ada                = null;
        System.out.println(desc);
        printConfusion(confusion);
        confusion          = null;
    }

    public static void doPyrCART(String trainFile, String pruneFile, String testFile, String desc,
                                   String measure, boolean pruning, int V, int h) throws Exception{
        LinkedList pruneset = null;
        if (pruneFile.equals(EMPTY)) {
            pruneset = new LinkedList();
        } else {
            pruneset = DataReader.PyrimidinesProp(pruneFile);
        }
        LinkedList trainset  = DataReader.PyrimidinesProp(trainFile);
        /*
        Example ex = null;
        System.out.println("read this file ");
        for (int j = 0; j < trainset.size(); j++) {
              ex = (Example)trainset.get(j);
              System.out.println("line "+ j+ " "+ ex.toString());
        }
        System.out.println("end of this file");
        */        
        CART cart            = new CART(trainset, pruneset, pyrFeatures, measure, pruning, V, h);
        trainset             = null;
        LinkedList testset   = DataReader.PyrimidinesProp(testFile);
        confusion            = cart.runClassificationTest(testset);
        testset              = null;
        cart                 = null;
        System.out.println(desc);
        printConfusion(confusion);
        confusion            = null;
    }

    public static void doTriazineCART(String trainFile, String pruneFile, String testFile, String desc, 
                          String measure, boolean pruning, int V, int h) throws Exception{
        LinkedList pruneset = null;
        if (pruneFile.equals(EMPTY)) {
            pruneset = new LinkedList();
        } else {
            pruneset = DataReader.TriazinesProp(pruneFile);
        }
        LinkedList trainset  = DataReader.TriazinesProp(trainFile);
        CART cart            = new CART(trainset, pruneset, triazineFeatures,measure, pruning, V, h);
        trainset             = null;
        LinkedList testset   = DataReader.TriazinesProp(testFile);
        confusion            = cart.runClassificationTest(testset);
        testset              = null;
        cart                 = null;
        System.out.println(desc);
        printConfusion(confusion);
        confusion            = null;
    }

    public static void printConfusion (int[][] confusion) {
        for (int i = 0; i < 2; i++) {
            for(int j = 0; j < 2; j++) {
                System.out.println(i + " " + j + " (true/pred)- " + confusion[i][j]);
            }
        }
        // accuracy
        double accuracy;
        double sensitivity;
        double specificity;
        double ppv;
        double npv;
        double tp   = confusion[1][1];
        double tn   = confusion[0][0];
        double fp   = confusion[0][1];
        double fn   = confusion[1][0];
        accuracy    = (tp + tn)/(tp + tn + fp + fn);
        sensitivity = tp/(tp + fn);
        specificity = tn/(tn + fp);
        ppv = tp/(tp + fp);
        npv = tn/(tn + fn);
        System.out.println("accuracy: " + accuracy);
        System.out.println("sensitivity: " + sensitivity);
        System.out.println("specificity: " + specificity);
        System.out.println("ppv: " + ppv);
        System.out.println("npv: " + npv);
        
    }
}
