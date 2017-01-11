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
 * This class implements a web service that can respond to SAML attribute queries.
 * The attribute values are stored in a MySQL database. There is a separate service
 * for modifying the database (see AADatabaseUpdater).
 */

import java.sql.SQLException;
import java.util.Iterator;
import java.util.ArrayList;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMAttribute;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;

public class SimpleAAService {
    // default values for issuer format and value
    private String issuerFormat = "urn:oasis:names:tc:SAML:2.0:nameid-format:entity";
    private String issuerValue = "DefaultService";

    // reads the IssuerFormat and IssuerValue properties from services.xml
    private void readProperties() {
	MessageContext mc = MessageContext.getCurrentMessageContext();
	Parameter ifparam = mc.getAxisService().getParameter("IssuerFormat");
	if (ifparam != null) {
	    issuerFormat = ifparam.getParameterElement().getText();
	}
	Parameter ivparam = mc.getAxisService().getParameter("IssuerValue");
	if (ivparam != null) {
	    issuerValue = ivparam.getParameterElement().getText();
	}
    }

    // queries the database for the given attribute names for the given user.
    // if attributeNames is of length zero, all the user's attributes are returned
    private ArrayList<AttributeResult> getAttributeValues(String subjectName, ArrayList<String> attributeNames)
	throws UnknownSubjectException, InvalidAttributeNameException, ClassNotFoundException, SQLException {
	int i;
	ArrayList<AttributeResult> result = new ArrayList<AttributeResult>();
	if (attributeNames.size() == 0) {
	    // special case. No attribute names given, so retrieve all of them
	    result = Util.getAllAttributesForUser(subjectName);
	}
	else {
	    // retrieve the attributes requested
	    for (i = 0; i < attributeNames.size(); i++) {
		result.add(new AttributeResult(attributeNames.get(i), Util.getAttributeForUser(subjectName, attributeNames.get(i))));
	    }
	}
	return result;
    }

    // builds the SAML XML for the given attribute
    // this currently only supports simple scalar string attributes, but more
    // complex types could be added
    private OMElement buildAttributeXML(AttributeResult attr, OMNamespace ns) throws SQLException, ClassNotFoundException, InvalidAttributeNameException {
	// build an XML Attribute for one attribute result
	OMFactory fac = OMAbstractFactory.getOMFactory();

	// create XML Schema namespaces
	OMNamespace xs = fac.createOMNamespace("http://www.w3.org/2001/XMLSchema", "xs");
	OMNamespace xsi = fac.createOMNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
	
	// create base Attribute element
	OMElement attributeElem = fac.createOMElement("Attribute", ns);
	OMAttribute nameAttr = fac.createOMAttribute("Name", null, attr.name);
	// look up friendly name here
	OMAttribute friendlyNameAttr = fac.createOMAttribute("FriendlyName", null, Util.getFriendlyName(attr.name));
	OMAttribute nameFormatAttr = fac.createOMAttribute("NameFormat", null, "urn:oasis:names:tc:SAML:2.0:attrname-format:uri");
	attributeElem.addAttribute(nameAttr);
	attributeElem.addAttribute(friendlyNameAttr);
	attributeElem.addAttribute(nameFormatAttr);

	// create AttributeValue element
	OMElement attributeValue = fac.createOMElement("AttributeValue", ns);
	attributeValue.declareNamespace(xs);
	attributeValue.declareNamespace(xsi);

	// rest depends on result type
	OMAttribute typeAttr = null;
	switch (attr.type) {
	case AttributeResult.STRING:
	    typeAttr = fac.createOMAttribute("type", xsi, "xs:string");
	    fac.createOMText(attributeValue, attr.stringval);
	    break;
	}
	attributeValue.addAttribute(typeAttr);
	
	attributeElem.addChild(attributeValue);

	return attributeElem;
    }

    // builds a SAML response containing the values of the requested attributes
    private OMElement buildSAMLResponse(String subjectName, String senderID, String queryIssuer, ArrayList<AttributeResult> results) throws SQLException, ClassNotFoundException, InvalidAttributeNameException {
	int i;
	OMFactory fac = OMAbstractFactory.getOMFactory();

	/*
	 * Create top level element, including namespaces and attributes
	 */
	OMNamespace samlNs = fac.createOMNamespace("urn:oasis:names:tc:SAML:2.0:assertion", "saml");
	OMNamespace samlpNs = fac.createOMNamespace("urn:oasis:names:tc:SAML:2.0:protocol", "samlp");

	OMElement response = fac.createOMElement("Response", samlpNs);
	response.declareNamespace(samlNs);
	response.declareNamespace(samlpNs);

	// add ID attribute
	OMAttribute idAttr = fac.createOMAttribute("ID", null, Util.generateID());
	response.addAttribute(idAttr);

	// add InResponseTo attribute
	OMAttribute ir2Attr = fac.createOMAttribute("InResponseTo", null, senderID);
	response.addAttribute(ir2Attr);

	// add IssueInstant attribute
	OMAttribute issueInstantAttr = fac.createOMAttribute("IssueInstant", null, Util.getTimeUTC());
	response.addAttribute(issueInstantAttr);

	// add Version attribute
	OMAttribute versionAttr = fac.createOMAttribute("Version", null, "2.0");
	response.addAttribute(versionAttr);

	/*
	 * Add Issuer element
	 */
	OMAttribute issuerFormatAttr = fac.createOMAttribute("Format", null, issuerFormat);
	OMElement issuer = fac.createOMElement("Issuer", samlNs);
	issuer.addAttribute(issuerFormatAttr);
	fac.createOMText(issuer, issuerValue);
	response.addChild(issuer);

	/*
	 * Add StatusCode element
	 */
	OMElement status = fac.createOMElement("Status", samlpNs);
	OMElement statusCodeElem = fac.createOMElement("StatusCode", samlpNs);
	OMAttribute statusCodeAttr = fac.createOMAttribute("Value", null, "urn:oasis:names:tc:SAML:2.0:status:Success");
	statusCodeElem.addAttribute(statusCodeAttr);
	status.addChild(statusCodeElem);
	response.addChild(status);

	/*
	 * Add Assertion element
	 */
	OMElement assertion = fac.createOMElement("Assertion", samlNs);
	OMAttribute assertionID = fac.createOMAttribute("ID", null, Util.generateID());
	assertion.addAttribute(assertionID);
	OMAttribute assertionDate = fac.createOMAttribute("IssueInstant", null, Util.getTimeUTC());
	assertion.addAttribute(assertionDate);

	/*
	 * Add Issuer element to Assertion
	 */
	OMAttribute issuerFormatAttrA = fac.createOMAttribute("Format", null, issuerFormat);
	OMElement issuerA = fac.createOMElement("Issuer", samlNs);
	issuerA.addAttribute(issuerFormatAttrA);
	fac.createOMText(issuerA, issuerValue);
	assertion.addChild(issuerA);

	/*
	 * Add Subject element to Assertion
	 */
	OMElement subject = fac.createOMElement("Subject", samlNs);
	// TODO: add confirmation data here if required
	OMElement nameID = fac.createOMElement("NameID", samlNs);
	fac.createOMText(nameID, subjectName);
	subject.addChild(nameID);
	assertion.addChild(subject);

	/*
	 * Add Conditions element to Assertion
	 */
	OMElement conditions = fac.createOMElement("Conditions", samlNs);
	OMAttribute notBefore = fac.createOMAttribute("NotBefore", null, Util.getTimeUTC());
	OMAttribute notOnOrAfter = fac.createOMAttribute("NotOnOrAfter", null, Util.getTimePlus5Minutes());
	conditions.addAttribute(notBefore);
	conditions.addAttribute(notOnOrAfter);
	// TODO: add audience restriction here if required
	assertion.addChild(conditions);

	/*
	 * Add AttributeStatement element to Assertion
	 */
	OMElement attributeStatement = fac.createOMElement("AttributeStatement", samlNs);
	for (i = 0; i < results.size(); i++) {
	    OMElement attributeElem = buildAttributeXML(results.get(i), samlNs);
	    attributeStatement.addChild(attributeElem);
	}

	assertion.addChild(attributeStatement);

	response.addChild(assertion);
	return response;
    }

    // builds an error response including the given SAML status code and message
    // if senderID is empty, omit InResponseTo attribute
    private OMElement buildErrorResponse(String statusCode, String message, String senderID) {
	OMFactory fac = OMAbstractFactory.getOMFactory();

	/*
	 * Create top level element, including namespaces and attributes
	 */
	OMNamespace samlpNs = fac.createOMNamespace("urn:oasis:names:tc:SAML:2.0:protocol", "samlp");

	OMElement response = fac.createOMElement("Response", samlpNs);
	response.declareNamespace(samlpNs);

	// add ID attribute
	OMAttribute idAttr = fac.createOMAttribute("ID", null, Util.generateID());
	response.addAttribute(idAttr);

	// add InResponseTo attribute
	if ((senderID != null) && (!senderID.equals(""))) {
	    OMAttribute ir2Attr = fac.createOMAttribute("InResponseTo", null, senderID);
	    response.addAttribute(ir2Attr);
	}

	// add IssueInstant attribute
	OMAttribute issueInstantAttr = fac.createOMAttribute("IssueInstant", null, Util.getTimeUTC());
	response.addAttribute(issueInstantAttr);

	// add Version attribute
	OMAttribute versionAttr = fac.createOMAttribute("Version", null, "2.0");
	response.addAttribute(versionAttr);

	/*
	 * Add Status element
	 */
	OMElement status = fac.createOMElement("Status", samlpNs);
	OMElement statusCodeElem = fac.createOMElement("StatusCode", samlpNs);
	OMAttribute statusCodeAttr = fac.createOMAttribute("Value", null, statusCode);
	statusCodeElem.addAttribute(statusCodeAttr);
	status.addChild(statusCodeElem);

	if ((message != null) && (!message.equals(""))) {
	    OMElement statusMessage = fac.createOMElement("StatusMessage", samlpNs);
	    fac.createOMText(statusMessage, message);
	    status.addChild(statusMessage);
	}

	response.addChild(status);

	return response;
    }

    // service entry point for the attribute query
    public OMElement AttributeQuery(OMElement element) throws XMLStreamException {
	element.build();
	element.detach();

	// read settings from services.xml
	readProperties();

	OMElement response = null;
	String senderID = "";

	try {
	    // Stuff we need to parse out of the query:
	    //  - ID (to use as InResponseTo)
	    //  - issuer (if we need to put in an "audience restriction")
	    //  - subject name
	    //  - names of attributes to query
	    String subjectName = "";
	    String queryIssuer = "";
	    String version = "2.0";
	    ArrayList<String> attributes = new ArrayList<String>();
	    Iterator attrs = element.getAllAttributes();
	    while (attrs.hasNext()) {
		OMAttribute attr = (OMAttribute)attrs.next();
		if (attr.getLocalName().equals("ID")) {
		    senderID = attr.getAttributeValue();
		}
		else if (attr.getLocalName().equals("Version")) {
		    version = attr.getAttributeValue();
		}
	    }
	    if (!version.equals("2.0")) throw new VersionMismatchException("This service only supports SAML version 2.0");
	    
	    Iterator children = element.getChildElements();
	    while (children.hasNext()) {
		OMElement child = (OMElement)children.next();
		if (child.getLocalName().equals("Subject")) {
		    OMElement nameID = child.getFirstElement();
		    subjectName = nameID.getText();
		}
		else if (child.getLocalName().equals("Issuer")) {
		    queryIssuer = child.getText();
		}
		else if (child.getLocalName().equals("Attribute")) {
		    Iterator attrattrs = child.getAllAttributes();
		    while (attrattrs.hasNext()) {
			OMAttribute attr = (OMAttribute)attrattrs.next();
			if (attr.getLocalName().equals("Name")) {
			    attributes.add(attr.getAttributeValue());
			    break;
			}
		    }	
		}
	    }
	    System.out.println("Subject name = " + subjectName);
	    System.out.println("Sender ID = " + senderID);
	    System.out.println("Query issuer = " + queryIssuer);
	    int i;
	    for (i = 0; i < attributes.size(); i++) {
		System.out.println("Attribute = " + attributes.get(i));
	    }
	    
	    // Then use this information to retrieve the value for each attribute
	    ArrayList<AttributeResult> results = getAttributeValues(subjectName, attributes);
	    
	    // Build the XML response
	    response = buildSAMLResponse(subjectName, senderID, queryIssuer,
					 results);
	}
	// generate error response if exception occurred
	catch (UnknownSubjectException use) {
	    response = buildErrorResponse("urn:oasis:names:tc:SAML:2.0:status:AuthnFailed", use.toString(), senderID);
	}
	catch (InvalidAttributeNameException iane) {
	    response = buildErrorResponse("urn:oasis:names:tc:SAML:2.0:status:InvalidAttrNameOrValue", iane.toString(), senderID);
	}
	catch (VersionMismatchException vme) {
	    response = buildErrorResponse("urn:oasis:names:tc:SAML:2.0:status:VersionMismatch", vme.toString(), senderID);
	}
	catch (Exception ex) {
	    response = buildErrorResponse("urn:oasis:names:tc:SAML:2.0:status:Responder", ex.toString(), senderID);
	}

	return response;
    }

}
