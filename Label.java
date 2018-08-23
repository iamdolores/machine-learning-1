//
//  Label.java
//
//  Created by Veronica Mayorga
//  Copyright. All rights reserved.
//

public class Label {
    private int label;

    public Label(int l) {
        label = l;
    }
    public int getLabel() {
        return label;
    }
    
    public Object clone() {
        return new Label(label);
    }
    
    public String toString () {
        return "label: "+ String.valueOf(label);
    }
}
