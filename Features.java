//
//  Features.java
//
//  Created by Veronica Mayorga
//  Copyright. All rights reserved.
//
import java.util.LinkedList;

public class Features {

    // - the first LinkedList in all is for the labels
    // - subsequent LinkedList in all are for features
    // - each LinkedList in all holds the possible labels
    //   for the feature/label... nominal values translated to 
    //   0 and 1.
    public LinkedList[] all;
    // size of all
    public int size = 0;
    // type is descriptor of features
    // e.g. "pyrimidine-prop"
    public String type = "";
    public static String PYR_PROP     = "pyrimidine-prop";
    public static String TRIAZINE_PROP = "triazine-prop";

    private int[][] pyrFeatures = 
    {
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4, 5, 6, 7, 8},
        {0, 1, 2, 3, 4, 5, 6, 7, 8},
        {0, 1, 2},
        {0, 1, 2, 3},
        {0, 1, 2},
        {0, 1, 2},
        {0, 1, 2, 3},
        {0, 1, 2, 3, 4, 5},
        {0, 1, 2, 3, 4}
    };
    private int[][] triazineFeatures = {
        {-1, 0, 1, 2, 3, 4, 5},
        {-1, 0, 1, 2, 3, 4, 5, 6, 7, 8},
        {-1, 0, 1, 2, 3, 4, 5, 6, 7, 8},
        {-1, 0, 1, 2},
        {-1, 0, 1, 2, 3},
        {-1, 0, 1, 2},
        {-1, 0, 1, 2},
        {-1, 0, 1, 2, 3},
        {-1, 0, 1, 2, 3, 4, 5},
        {-1, 0, 1, 2, 3, 4}
    };
    private int[] binaryClass = {0 , 1};
    
    public Features (String t) {
        if (t == PYR_PROP) {
            size = 54 + 1;
            type = t;
            int[] indeces = new int[size - 1];
            int k = 0;
            int i, j;
            // - pyr contain 3 regions of substitution
            //   with 9 attributes desc each region
            // - classification data contains 2X 
            // - so 6 regions with 9 attr each
            for (i = 0; i < 6; i ++) {
                for (j = 0; j < 9; j++) {
                    indeces[k] = j;
                    k++;
                }
            
            }
            all = new LinkedList[size];
            LinkedList values = null;
            // each of 54 attributes and their possible values
            // 
            for (i = 0; i < indeces.length; i++) {
                values = new LinkedList();
                for (j = 0; j < pyrFeatures[indeces[i]].length; j++) {
                    values.add(new Integer(pyrFeatures[indeces[i]][j]));
                }
                all[i+1] = values;
            }
            LinkedList label = new LinkedList();
            for (i = 0; i < binaryClass.length; i++) {
                label.add(new Integer(i));
            }
            all[0] = label;
        } else if (t == TRIAZINE_PROP) {
            // size is # attributes + class label
            size = 120 + 1;
            type = t;
            int[] indeces = new int[size - 1];
            int k = 0;
            int i, j;
            // - 6 regions of 10 attributes each for each compound
            // - total of 12 regions with 10 attributes each
            for (i = 0; i < 12; i ++) {
                for (j = 0; j < 10; j++) {
                    indeces[k] = j;
                    k++;
                }
            
            }
            all = new LinkedList[size];
            LinkedList values = null;
            for (i = 0; i < indeces.length; i++) {
                values = new LinkedList();
                for (j = 0; j < triazineFeatures[indeces[i]].length; j++) {
                    values.add(new Integer(triazineFeatures[indeces[i]][j]));
                }
                all[i+1] = values;
            }

            // two class classification 0 or 1
            LinkedList label = new LinkedList();
            for (i = 0; i < binaryClass.length; i++) {
                label.add(new Integer(i));
            }
            all[0] = label;
        } else {
            System.out.println("error - unrecognized file type: " + t);
            System.exit(1);
        }
    }

}
