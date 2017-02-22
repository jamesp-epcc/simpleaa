package uk.ac.ed.epcc.safeaa;

import java.util.ArrayList;

/*
 * This class represents the value of a single attribute, retrieved from the
 * database
 */
public class AttributeResult {
    // TODO: handle other types if necessary
    public static final int STRING = 0;
    public static final int ARRAY = 1;

    public AttributeResult(String name, String value) {
	this.name = name;
	type = AttributeResult.STRING;
	stringval = value;
    }
    
    public AttributeResult(String name, String[] value) {
	this.name = name;
	type = AttributeResult.ARRAY;
	arrayval = value;
    }

    public AttributeResult(String name, ArrayList<String> value) {
	this.name = name;
	type = AttributeResult.ARRAY;
	arrayval = new String[value.size()];
	int i;
	for (i = 0; i < value.size(); i++) {
	    arrayval[i] = value.get(i);
	}
    }

    String name;
    int type;
    String stringval;
    String[] arrayval;
}

