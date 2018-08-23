//
//  DataWriter.java
//
//  Created by Veronica Mayorga
//  Copyright. All rights reserved.
//
import java.io.*;
import java.io.File;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.FileReader;

import java.util.StringTokenizer;
import java.util.LinkedList;
import java.lang.Integer;
import java.lang.Double;
import java.lang.Math;
import java.util.Random;

public class DataWriter {
    private static long seed            = (new Long("1164735835086")).longValue();        
    private static Random rNumGenerator = new Random(seed);

    public static void writeFile(String filename, LinkedList data) throws Exception {

        PrintWriter outFile = new PrintWriter(new FileWriter(filename));
        Example ex = null;
        String line = "";
        
        int i,j;
        for (i = 0; i < data.size(); i++) {
            line = "";
            ex = (Example)data.get(i);
            for (j = 0; j < ex.attrs.length - 1; j++) {
                line += String.valueOf(ex.attrs[j]) + " ";
            }
            line += String.valueOf(ex.attrs[j]);
            outFile.println(line);
        }
        
        outFile.close();
    
    }
    public static void writeArffFile(String filename, LinkedList data) throws Exception {

        PrintWriter outFile = new PrintWriter(new FileWriter(filename));
        Example ex = null;
        String line = "";
        int i,j;
        
        line = "@RELATION "+filename;
        outFile.println(line);
        outFile.println();
        for (i = 0; i < 54; i++) {
            line = "";
            line = "@ATTRIBUTE a" +i +" NUMERIC";
            outFile.println(line);
        }
        line = "@ATTRIBUTE class {0,1}";
        outFile.println(line);
        outFile.println();
        line = "@DATA";
        outFile.println(line);
        outFile.println();

        for (i = 0; i < data.size(); i++) {
            line = "";
            ex = (Example)data.get(i);
            for (j = 1; j < ex.attrs.length - 1; j++) {
                line += String.valueOf(ex.attrs[j]) + ",";
            }
            line += String.valueOf(ex.attrs[j]) +","+String.valueOf(ex.attrs[0]);
            outFile.println(line);
        }
        
        outFile.close();
    
    }

    public static void randomSplitData(LinkedList data, double percent, String file1, String file2) throws Exception {
    
        int num = (new Double(Math.floor(data.size() * percent))).intValue();
        LinkedList split1 = new LinkedList();
        int r;

        for (int i = 0; i < num; i++){
            r = rNumGenerator.nextInt(data.size());
            split1.add(data.remove(r));
        }
        writeFile(file1,split1);
        writeFile(file2,data);
    }

    public static void main (String args[])  throws Exception{
        LinkedList trainset = null;
        double percent = 0.10;
        int i;

        for (i = 0; i < cs242_proj.pyrPropTrain.length; i++) {
            trainset = DataReader.PyrimidinesProp(cs242_proj.pyrPropTrain[i]);
            randomSplitData(trainset, percent, cs242_proj.pyrPropPruneSet[i],cs242_proj.pyrPropPSTrain[i]); 
            trainset = null;            
        }

/*    
        for (i = 0; i < cs242_proj.triazinePropTrain.length; i++) {
            trainset = DataReader.TriazinesProp(cs242_proj.triazinePropTrain[i]);
            trainset = null;            
        }
*/
    }
}
