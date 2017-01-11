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
/*
 * A simple Java API and commandline client for accessing the attribute authority
 * service
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Set;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMAttribute;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.Constants;
import org.apache.axis2.client.ServiceClient;

public class SimpleAAClient {
    // endpoint for attribute queries
    private static EndpointReference targetEPR = 
	new EndpointReference("http://localhost:8080/axis2/services/SimpleAAService");

    // endpoint for updating the database
    private static EndpointReference updateEPR =
	new EndpointReference("http://localhost:8080/axis2/services/AADatabaseUpdater");

    /*
     * Generate an XML SAML attribute query. Returns the root OMElement.
     */
    private static OMElement createSAMLQuery(String subjectNameID, String[] attributes) {
	OMFactory fac = OMAbstractFactory.getOMFactory();

	/*
	 * Create top level element, including namespaces and attributes
	 */
	OMNamespace samlNs = fac.createOMNamespace("urn:oasis:names:tc:SAML:2.0:assertion", "saml");
	OMNamespace samlpNs = fac.createOMNamespace("urn:oasis:names:tc:SAML:2.0:protocol", "samlp");

	OMElement attrQuery = fac.createOMElement("AttributeQuery", samlpNs);
	attrQuery.declareNamespace(samlNs);
	attrQuery.declareNamespace(samlpNs);

	OMAttribute idAttr = fac.createOMAttribute("ID", null, Util.generateID());
	attrQuery.addAttribute(idAttr);
	OMAttribute versionAttr = fac.createOMAttribute("Version", null, "2.0");
	attrQuery.addAttribute(versionAttr);
	OMAttribute issueInstantAttr = fac.createOMAttribute("IssueInstant", null, Util.getTimeUTC());
	attrQuery.addAttribute(issueInstantAttr);

	/*
	 * Create issuer element
	 */
	OMElement issuer = fac.createOMElement("Issuer", samlNs);
	OMAttribute issuerFormatAttr = fac.createOMAttribute("Format", null, "urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
	issuer.addAttribute(issuerFormatAttr);
	String hostname = "localhost";
	try {
	    hostname = InetAddress.getLocalHost().getHostName();
	}
	catch (UnknownHostException uhe) {
	}
	fac.createOMText(issuer, hostname);
	attrQuery.addChild(issuer);

	/*
	 * Create subject element
	 */
	OMElement subject = fac.createOMElement("Subject", samlNs);
	OMElement nameID = fac.createOMElement("NameID", samlNs);
	fac.createOMText(nameID, subjectNameID);
	subject.addChild(nameID);
	attrQuery.addChild(subject);

	/*
	 * Create attribute elements for each attribute
	 */
	int i;
	// may be null if querying for all available attributes
	if (attributes != null) {
	    for (i = 0; i < attributes.length; i++) {
		OMElement attribute = fac.createOMElement("Attribute", samlNs);
		OMAttribute nameAttr = fac.createOMAttribute("Name", null, attributes[i]);
		attribute.addAttribute(nameAttr);
		attrQuery.addChild(attribute);
	    }
	}

	return attrQuery;
    }

    // convenience method for parsing response XML
    private static OMElement getChildNamed(OMElement elem, String name) {
	Iterator children = elem.getChildElements();
	while (children.hasNext()) {
	    OMElement child = (OMElement)children.next();
	    if (child.getLocalName().equals(name)) {
		return child;
	    }
	}
	return null;
    }

    // convenience method for parsing response XML
    private static String getAttributeNamed(OMElement elem, String name) {
	Iterator attrs = elem.getAllAttributes();
	while (attrs.hasNext()) {
	    OMAttribute attr = (OMAttribute)attrs.next();
	    if (attr.getLocalName().equals(name)) {
		return attr.getAttributeValue();
	    }
	}
	return "";
    }

    // performs a SAML attribute query for the given attributes of the specified
    // user. If attrs is null or is an empty array, all the user's attributes are
    // returned.
    public static HashMap<String,String> attributeQuery(String name, String[] attrs) throws Exception {
	int i;
	OMElement samlQuery = createSAMLQuery(name, attrs);
	System.out.println("SAML query: " + samlQuery.toString());

	Options options = new Options();
	options.setTo(targetEPR);
	
	options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
	
	ServiceClient sender = new ServiceClient();
	sender.setOptions(options);
	OMElement result = sender.sendReceive(samlQuery);
	
	System.out.println("Response XML: " + result.toString());
	
	// first check response status code
	OMElement statusElem = getChildNamed(result, "Status");
	if (statusElem == null) throw new Exception("No status element in SAML response");
	OMElement statusCodeElem = getChildNamed(statusElem, "StatusCode");
	if (statusCodeElem == null) throw new Exception("No status code element in SAML response");
	
	if (!getAttributeNamed(statusCodeElem, "Value").equals("urn:oasis:names:tc:SAML:2.0:status:Success")) {
	    throw new Exception("Server returned status code " + getAttributeNamed(statusCodeElem, "Value"));
	}
	
	// parse out attribute values from response
	OMElement assertion = getChildNamed(result, "Assertion");
	if (assertion == null) throw new Exception("No assertion element in SAML response");
	OMElement attributeStatement = getChildNamed(assertion, "AttributeStatement");
	if (attributeStatement == null) throw new Exception("No attribute statement element in SAML response");

	HashMap<String,String> attrvals = new HashMap<String,String>();

	Iterator attributes = attributeStatement.getChildElements();
	while (attributes.hasNext()) {
	    OMElement child = (OMElement)attributes.next();
	    String attrname = getAttributeNamed(child, "Name");
	    String friendlyname = getAttributeNamed(child, "FriendlyName");

	    Iterator attrvalsit = child.getChildElements();
	    if (!attrvalsit.hasNext()) {
		throw new Exception("Missing AttributeValue element in SAML response");
	    }

	    // generate comma separated list if attribute has multiple values
	    String attrvalue = "";
	    while (attrvalsit.hasNext()) {
		OMElement attrval = (OMElement)attrvalsit.next();
		if (attrval.getLocalName().equals("AttributeValue")) {
		    if (!attrvalue.equals("")) attrvalue = attrvalue + ",";
		    attrvalue = attrvalue + attrval.getText();
		}
	    }
	    attrvals.put(attrname, attrvalue);
	}

	return attrvals;
    }

    public static void addAttributeType(String name, String friendlyName) throws Exception {
	AADatabaseUpdaterStub stub = new AADatabaseUpdaterStub("http://localhost:8080/axis2/services/AADatabaseUpdater");
	AADatabaseUpdaterStub.AddAttributeType req = new AADatabaseUpdaterStub.AddAttributeType();
	req.setArgs0(name);
	req.setArgs1(friendlyName);	
	AADatabaseUpdaterStub.AddAttributeTypeResponse res = stub.addAttributeType(req);
	switch (res.get_return()) {
	case 1:
	    throw new Exception("Attribute type already exists");
	case 2:
	    throw new Exception("Internal server error");
	}
    }

    public static void addUser(String name) throws Exception {
	AADatabaseUpdaterStub stub = new AADatabaseUpdaterStub("http://localhost:8080/axis2/services/AADatabaseUpdater");
	AADatabaseUpdaterStub.AddUser req = new AADatabaseUpdaterStub.AddUser();
	req.setArgs0(name);
	AADatabaseUpdaterStub.AddUserResponse res = stub.addUser(req);
	switch (res.get_return()) {
	case 3:
	    throw new Exception("User already exists");
	case 2:
	    throw new Exception("Internal server error");
	}
    }

    public static void updateAttributeForUser(String user, String attr, String value) throws Exception {
	AADatabaseUpdaterStub stub = new AADatabaseUpdaterStub("http://localhost:8080/axis2/services/AADatabaseUpdater");
	AADatabaseUpdaterStub.SetAttributeForUser req = new AADatabaseUpdaterStub.SetAttributeForUser();
	req.setArgs0(user);
	req.setArgs1(attr);
	req.setArgs2(value);
	AADatabaseUpdaterStub.SetAttributeForUserResponse res = stub.setAttributeForUser(req);
	switch (res.get_return()) {
	case 2:
	    throw new Exception("Internal server error");
	case 4:
	    throw new Exception("User name not recognised");
	case 5:
	    throw new Exception("Attribute name not recognised");
	}
    }

    public static void addAttributeForUser(String user, String attr, String value) throws Exception {
	AADatabaseUpdaterStub stub = new AADatabaseUpdaterStub("http://localhost:8080/axis2/services/AADatabaseUpdater");
	AADatabaseUpdaterStub.AddAttributeForUser req = new AADatabaseUpdaterStub.AddAttributeForUser();
	req.setArgs0(user);
	req.setArgs1(attr);
	req.setArgs2(value);
	AADatabaseUpdaterStub.AddAttributeForUserResponse res = stub.addAttributeForUser(req);
	switch (res.get_return()) {
	case 2:
	    throw new Exception("Internal server error");
	case 4:
	    throw new Exception("User name not recognised");
	case 5:
	    throw new Exception("Attribute name not recognised");
	}
    }

    public static void removeAttributeForUser(String user, String attr, String value) throws Exception {
	AADatabaseUpdaterStub stub = new AADatabaseUpdaterStub("http://localhost:8080/axis2/services/AADatabaseUpdater");
	AADatabaseUpdaterStub.RemoveAttributeForUser req = new AADatabaseUpdaterStub.RemoveAttributeForUser();
	req.setArgs0(user);
	req.setArgs1(attr);
	req.setArgs2(value);
	AADatabaseUpdaterStub.RemoveAttributeForUserResponse res = stub.removeAttributeForUser(req);
	switch (res.get_return()) {
	case 2:
	    throw new Exception("Internal server error");
	case 4:
	    throw new Exception("User name not recognised");
	case 5:
	    throw new Exception("Attribute name not recognised");
	}
    }

    public static void deleteUser(String name) throws Exception {
	AADatabaseUpdaterStub stub = new AADatabaseUpdaterStub("http://localhost:8080/axis2/services/AADatabaseUpdater");
	AADatabaseUpdaterStub.DeleteUser req = new AADatabaseUpdaterStub.DeleteUser();
	req.setArgs0(name);
	AADatabaseUpdaterStub.DeleteUserResponse res = stub.deleteUser(req);
	switch (res.get_return()) {
	case 2:
	    throw new Exception("Internal server error");
	case 4:
	    throw new Exception("User name not recognised");
	}
    }

    public static void main(String[] args) {
	int i;
	if (args.length < 1) {
	    System.out.println("Usage: SimpleAAClient <operation> <parameters>");
	    System.out.println("  (valid operations are query, addattrtype, adduser, updateuserattr, adduserattr, removeuserattr, deleteuser)");
	    return;
	}
	String operation = args[0];
	try {
	    if (operation.equals("query")) {
		if (args.length < 2) {
		    System.out.println("Usage: SimpleAAClient query <user name> [<attribute names>]");
		    return;
		}

		String userName = args[1];
		String[] samlattrs = new String[args.length - 2];
		for (i = 0; i < samlattrs.length; i++) {
		    samlattrs[i] = args[i+2];
		}
		
		HashMap<String,String> result = attributeQuery(userName, samlattrs);
		Set<String> keys = result.keySet();
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
		    String key = it.next();
		    System.out.println(key + " = " + result.get(key));
		}
	    }
	    else if (operation.equals("addattrtype")) {
		if (args.length != 3) {
		    System.out.println("Usage: SimpleAAClient addattrtype <formal name> <friendly name>");
		    return;
		}
		addAttributeType(args[1], args[2]);
	    }
	    else if (operation.equals("adduser")) {
		if (args.length != 2) {
		    System.out.println("Usage: SimpleAAClient adduser <name>");
		    return;
		}
		addUser(args[1]);
	    }
	    else if (operation.equals("updateuserattr")) {
		if (args.length != 4) {
		    System.out.println("Usage: SimpleAAClient updateuserattr <user name> <attribute name> <value>");
		    return;
		}
		updateAttributeForUser(args[1], args[2], args[3]);
	    }
	    else if (operation.equals("adduserattr")) {
		if (args.length != 4) {
		    System.out.println("Usage: SimpleAAClient adduserattr <user name> <attribute name> <value>");
		    return;
		}
		addAttributeForUser(args[1], args[2], args[3]);
	    }
	    else if (operation.equals("removeuserattr")) {
		if (args.length != 4) {
		    System.out.println("Usage: SimpleAAClient removeuserattr <user name> <attribute name> <value>");
		    return;
		}
		removeAttributeForUser(args[1], args[2], args[3]);
	    }
	    else if (operation.equals("deleteuser")) {
		if (args.length != 2) {
		    System.out.println("Usage: SimpleAAClient deleteuser <name>");
		    return;
		}
		deleteUser(args[1]);
	    }
	    else {
		System.err.println("Unrecognised operation type '" + operation + "'");
	    }
	}
	catch (Exception e) {
	    System.err.println("Exception occurred: " + e.toString());
	}
    }
}
