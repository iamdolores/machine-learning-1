//
//  NaiveBayes.java
//
//  Created by Veronica Mayorga
//  Copyright. All rights reserved.
//
import java.util.LinkedList;
import java.util.Hashtable;
import java.util.ListIterator;

public class NaiveBayes {

    public double[] Priors;
    public double[] classTotals;
    public Features features;
    public LinkedList trainset;

    public NaiveBayes(LinkedList X, Features f) {
        features          = f;
        trainset          = X;
        LinkedList labels = (LinkedList)features.all[0];
        Priors            = new double[labels.size()];
        classTotals       = new double[labels.size()];
        
        int i, j, k, l;

        
        // calculate Laplacian priors
        double num[]  = new double[labels.size()];
        double den[]  = new double[labels.size()];
        Example train = null;
        for (i = 0, j = -1, l = 0; i < labels.size(); i++) {
            num[i] = 1;
            den[i] = labels.size();
            j      = ((Integer)labels.get(i)).intValue();
            for (k = 0; k < trainset.size(); k++) {
                train = (Example) trainset.get(k);
                if (j == train.label) {
                    l++;
                }
            }
            num[i] += l;
            den[i] += trainset.size();
            Priors[i] = num[i]/den[i];
            classTotals[i] = l;
            l = 0;
            j = -1;           
        }
    }

    public int predict(Example ex) {
        LinkedList labels = (LinkedList)features.all[0];
        LinkedList featureValues = null;
        int featureValue;
        int val = 0;
        // - calculate Laplacian attribute densities
        // - for each possible value of the label
        //   get p(xn | label) for each attribute n
        int i, j, k;
        double [][] num = new double[labels.size()][features.size - 1];
        Example train = null;
        int currLabel;
        
        // - get numerator counts for
        //   p(xn | yi) for each n and i
        // - init num array to 1 for all
        for (i = 0; i < labels.size(); i++) {
            for(j = 0; j < features.size - 1; j++){
                num[i][j] = 1;
            }
        }
                
        // for each example in trainset
        for (i=0; i < trainset.size(); i++) {
            train = (Example)trainset.get(i);
            // for each possible class label
            for (j = 0; j < labels.size(); j++) {
                currLabel = ((Integer)labels.get(j)).intValue();
                // if label of the training example matchess currLabel that counting
                if (train.label == currLabel) {
                    // - for each attribute in the example
                    for(k = 0; k < features.size - 1; k++) {
                        // - if jth attribute value match for example ex and train example train
                        //     - increment num count for the value of this attribute
                        if (ex.attrs[k+1] == train.attrs[k+1]) {
                            num[j][k]++;
                        }
                    }
                }
            }
        }

        double[][] densities = new double[labels.size()][features.size - 1];
        double[] possibleFeatures = new double[features.size-1];
        for (j=0;j < features.size - 1; j++) {
            possibleFeatures[j] = (features.all[j + 1]).size();
        }
        // - calculate p(xn|label) using num[][] and classTotals[]
        for (i=0; i < labels.size(); i++){
            for (j=0; j < features.size - 1; j++) {
                densities[i][j] = num[i][j]/ (classTotals[i] + possibleFeatures[j]);
            }
        }

        double[] posterior = new double [labels.size()];
        // - for each label multiply the likelihoods p(xn | label) 
        //   that get for each attribute n and it's prior to get
        //   p(label | xn) for each label.
        for (i = 0; i < labels.size(); i++) {
            posterior[i] = Priors[i];
            for (j = 0; j < features.size - 1; j++) {
                posterior[i] *= densities[i][j];
            }
        }
        // - predict on the biggest value.
        double max = posterior[0];
        int iMax = 0;
        for (i = 0; i < labels.size(); i++) {
            if (posterior[i] > max) {
                max = posterior[i];
                iMax = i;
            }
        }
        val = ((Integer)labels.get(iMax)).intValue();
        return val;
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
        for (i = 0; i < testset.size(); i++ ) {
            test      = (Example)testset.get(i);
            predValue = predict(test);
            trueValue = test.label;
            confusion[trueValue][predValue]++;
        }
        return confusion;
    }
}
