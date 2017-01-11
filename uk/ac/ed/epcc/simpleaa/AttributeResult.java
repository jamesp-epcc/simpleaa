package uk.ac.ed.epcc.simpleaa;

/*
 * Copyright (c) 2017 The University of Edinburgh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;

/*
 * This class represents the value of a single attribute, retrieved from the
 * database
 */
public class AttributeResult {
    // TODO: handle other types if necessaryx
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

