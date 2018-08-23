//
//  DataReader.java
//
//  Created by Veronica Mayorga
//  Copyright. All rights reserved.
//
import java.io.*;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;
import java.util.LinkedList;
import java.lang.Integer;

/*
*/
public class DataReader {
    public static final int DIR     = 0;
    public static final int FILE    = 1;
    public static final int INVALID = -1;

public static LinkedList PyrimidinesProp (String filename) throws Exception {
    // need: 
    // * class label and attribute list
    // check file exists
    // file format:
    // * 1class label, 54 attributes per line
    return readPyrFile(testFile(filename, "file"));
}

public static LinkedList PyrimidinesPropWNegs (String filename) throws Exception {
    // need: 
    // * class label and attribute list
    // check file exists
    // file format:
    // * 1class label, 54 attributes per line
    return readPyrFileWNegs(testFile(filename, "file"));
}

public static LinkedList TriazinesProp (String filename) throws Exception {
    return readTriazineFile(testFile(filename, "file"));
}

public static LinkedList readTriazineFile (File filename) throws Exception  {
    LinkedList examples = new LinkedList();
    StringTokenizer st;
    int first = 1;
    Integer label = null;
    LinkedList attrs = new LinkedList();
    String line;
    Integer feature = null;
    
    BufferedReader infile = new BufferedReader(new FileReader(filename));
    for (line = infile.readLine(); line != null; line = infile.readLine() ) {
        // System.out.println(line);
        st = new StringTokenizer(line);
        first = 1;
        while (st.hasMoreTokens()) {
            if (first == 1) {
                label = new Integer(st.nextToken());
                first = 0;
            } else {
                // don't convert -1 to 0s
                feature = new Integer(st.nextToken());
                attrs.add(feature);
            }
        }
        examples.add(new Example(label, attrs));
        label = null;
        attrs = new LinkedList();
        
    }
    infile.close();
    if (examples.size() == 0) {
        System.out.println("file is empty: " + filename.getName());
        System.exit(1);
    }
    return examples;
}

public static LinkedList readPyrFile (File filename) throws Exception  {
    LinkedList examples = new LinkedList();
    StringTokenizer st;
    int first = 1;
    int firstNeg = 1;
    Integer label = null;
    LinkedList attrs = new LinkedList();
    String line;
    Integer feature = null;
    
    BufferedReader infile = new BufferedReader(new FileReader(filename));
    for (line = infile.readLine(); line != null; line = infile.readLine() ) {
        // System.out.println(line);
        st = new StringTokenizer(line);
        first = 1;
        firstNeg = 1;
        while (st.hasMoreTokens()) {
            if (first == 1) {
                label = new Integer(st.nextToken());
                first = 0;
            } else {
                feature = new Integer(st.nextToken());
                // convert -1 to 0s
                if (feature.intValue() == -1 && firstNeg == 1) {
                    feature = new Integer(1);
                    firstNeg = 0;
                } else if (feature.intValue() == -1 && firstNeg == 0) {
                    feature = new Integer(0);
                }
                attrs.add(feature);
            }
        }
        examples.add(new Example(label, attrs));
        label = null;
        attrs = new LinkedList();
        
    }
    infile.close();
    if (examples.size() == 0) {
        System.out.println("file is empty: " + filename.getName());
        System.exit(1);
    }
    return examples;
}

public static LinkedList readPyrFileWNegs (File filename) throws Exception  {
    LinkedList examples = new LinkedList();
    StringTokenizer st;
    int first = 1;
    int firstNeg = 1;
    Integer label = null;
    LinkedList attrs = new LinkedList();
    String line;
    Integer feature = null;
    
    BufferedReader infile = new BufferedReader(new FileReader(filename));
    for (line = infile.readLine(); line != null; line = infile.readLine() ) {
        // System.out.println(line);
        st = new StringTokenizer(line);
        first = 1;
        firstNeg = 1;
        while (st.hasMoreTokens()) {
            if (first == 1) {
                label = new Integer(st.nextToken());
                first = 0;
            } else {
                feature = new Integer(st.nextToken());
                // no conversion of negativs
                attrs.add(feature);
            }
        }
        examples.add(new Example(label, attrs));
        label = null;
        attrs = new LinkedList();
        
    }
    infile.close();
    if (examples.size() == 0) {
        System.out.println("file is empty: " + filename.getName());
        System.exit(1);
    }
    return examples;
}

public static File testFile (String filename, String ftype) {
    File testFile;
    testFile = new File(filename);
    if (!testFile.exists()) {
        System.out.println("invalid " + ftype + " name :" + filename);
        System.exit(1);
    }
    int filetype = INVALID;
    if (ftype.equals("dir")) {
        filetype = DIR;
    } else if (ftype.equals("file")) {
        filetype = FILE;
    } else {
        System.out.println("invalid " + "file type name :" + ftype);
        System.exit(1);       
    }
    
    if (testFile.isDirectory() && filetype == DIR) {
        return testFile;
    } else if (testFile.isFile() && filetype == FILE) {
        return testFile;
    } else {
        System.out.println("bad file: " + filename +" " + ftype);
        System.exit(1);  
    }
    return testFile;
}

}

