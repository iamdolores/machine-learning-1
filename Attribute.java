//
//  Attribute.java
//
//  Created by Veronica Mayorga
//  Copyright. All rights reserved.
//
import java.util.LinkedList;

public class Attribute {
    private int attributeIndex;
    private LinkedList attributeValues = null;
    
    public Attribute(int attr, LinkedList attrValues) {
        attributeIndex      = attr;
        attributeValues = attrValues;
    }
    
    public int getAttributeIndex() {
        return attributeIndex;
    }
    
    public LinkedList getAttributeValues() {
        return attributeValues;
    }
    
    public Object clone(){
       // do i need to make a copy of attributeValues? no
       /*
       LinkedList attrVals = new LinkedList();
       for (int i = 0; i < attributeValues.size(); i++) {
           attrVals.add(attributeValues.get(i));
       }
       */
       return new Attribute(attributeIndex, attributeValues);
       
    }
    
    public String toString() {
        return "attribute : " + String.valueOf(attributeIndex) + String.valueOf(attributeValues);
    }
    
}
