//
//  Example.java
//
//  Created by Veronica Mayorga
//  Copyright. All rights reserved.
//
import java.lang.Integer;
import java.util.LinkedList;

public class Example {
    public int numAttrs;
    public int[] attrs;
    public int label;
    public double weight = 1;
    
    public Example (Integer l, LinkedList attributes ) {
        label = l.intValue();
        numAttrs = attributes.size() + 1;
        attrs = new int[numAttrs];
        Integer tmp;
        attrs[0] = label;
        for (int i = 1; i < numAttrs; i++) {
            tmp= (Integer)attributes.get(i - 1);
            attrs[i] = tmp.intValue();
        }
    }
    
    public String toString () {
        String out = "";
        for (int i = 0; i < attrs.length; i++) {
            out += String.valueOf(attrs[i]) + " ";
        }
        return out;
    }

}
