//
//  SplitAttribute.java
//
//  Created by Veronica Mayorga
//  Copyright. All rights reserved.
//

import java.util.LinkedList;


public class SplitAttribute {
    private LinkedList[] Xs     = null;
    private Attribute attribute   = null;

    public SplitAttribute (LinkedList[] xs, Attribute a) {
        attribute = a;
        Xs = xs;
    }
    
    public LinkedList[] getXs() {
        return Xs;
    }
    public Attribute getAttribute() {
        return attribute;
    }
}
