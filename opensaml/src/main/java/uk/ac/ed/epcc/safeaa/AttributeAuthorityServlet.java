package uk.ac.ed.epcc.safeaa;

import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.namespace.QName;

import org.joda.time.DateTime;

import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSStringBuilder;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPSOAP11Decoder;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPSOAP11Encoder;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.StatusMessage;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.security.RandomIdentifierGenerationStrategy;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;

public class AttributeAuthorityServlet extends HttpServlet {

    private static RandomIdentifierGenerationStrategy secureRandomIdGenerator;

    static {
	secureRandomIdGenerator = new RandomIdentifierGenerationStrategy();
    }

    // values loaded from properties
    private boolean loadedProperties;
    private String issuerValue;
    private String destinationValue;

    @Override
    public void init(ServletConfig config) throws ServletException {
	super.init(config);

	try {
	    // initialise OpenSAML
	    InitializationService.initialize();
	    loadedProperties = false;
	}
	catch (Exception e) {
	    System.out.println("Initialisation failed: " + e);
	    throw new RuntimeException("Initialisation failed: " + e);
	}
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	resp.setContentType("text/html");
	PrintWriter out = resp.getWriter();
	out.println("<h1>Attribute Authority Servlet</h1>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

	// load the properties if this is the first time
	if (!loadedProperties) {
	    issuerValue = "defaultissuer";
	    destinationValue = "defaultdestination";

	    Properties prop = new Properties();
	    prop.load(getServletContext().getResourceAsStream("/WEB-INF/safeaa.properties"));
	    issuerValue = prop.getProperty("issuer");
	    destinationValue = prop.getProperty("destination");
	    loadedProperties = true;
	}

	HTTPSOAP11Decoder decoder = new HTTPSOAP11Decoder();

	decoder.setHttpServletRequest(req);
	try {
	    BasicParserPool parserPool = new BasicParserPool();
	    parserPool.initialize();
	    decoder.setParserPool(parserPool);
	    decoder.initialize();
	    decoder.decode();
	}
	catch (MessageDecodingException e) {
	    throw new RuntimeException(e);
	}
	catch (ComponentInitializationException e) {
	    throw new RuntimeException(e);
	}

	SAMLObject message = decoder.getMessageContext().getMessage();
	Response response;

	System.out.println("Entering processQuery");

	try {
	    response = processQuery((AttributeQuery) message);
	}
	catch (UnknownSubjectException e) {
	    response = buildErrorResponse(StatusCode.AUTHN_FAILED, e.toString());
	}
	catch (InvalidAttributeNameException e) {
	    response = buildErrorResponse(StatusCode.INVALID_ATTR_NAME_OR_VALUE, e.toString());
	}
	catch (Exception e) {
	    // generic internal server error
	    response = buildErrorResponse(StatusCode.RESPONDER, e.toString());
	}
	
	MessageContext<SAMLObject> context = new MessageContext<SAMLObject>();
	context.setMessage(response);

	HTTPSOAP11Encoder encoder = new HTTPSOAP11Encoder();
	encoder.setMessageContext(context);
	encoder.setHttpServletResponse(resp);
	try {
	    encoder.prepareContext();
	    encoder.initialize();
	    encoder.encode();
	}
	catch (MessageEncodingException e) {
	    throw new RuntimeException(e);
	}
	catch (ComponentInitializationException e) {
	    throw new RuntimeException(e);
	}
    }

    private Response buildErrorResponse(String code, String message) {
	Response response = buildSAMLObject(Response.class);
	response.setDestination(destinationValue);
	response.setIssueInstant(new DateTime());
	response.setID(secureRandomIdGenerator.generateIdentifier());
	
	Status status = buildSAMLObject(Status.class);
	StatusCode statusCode = buildSAMLObject(StatusCode.class);
	statusCode.setValue(code);
	status.setStatusCode(statusCode);

	StatusMessage statusMessage = buildSAMLObject(StatusMessage.class);
	statusMessage.setMessage(message);
	status.setStatusMessage(statusMessage);

	response.setStatus(status);

	return response;
    }

    private Response processQuery(AttributeQuery message) throws Exception {
	Response response = buildSAMLObject(Response.class);
	response.setDestination(destinationValue);
	response.setIssueInstant(new DateTime());
	response.setID(secureRandomIdGenerator.generateIdentifier());
	Issuer issuer2 = buildSAMLObject(Issuer.class);
	issuer2.setValue(issuerValue);
	response.setIssuer(issuer2);

	Status status2 = buildSAMLObject(Status.class);
	StatusCode statusCode2 = buildSAMLObject(StatusCode.class);
	statusCode2.setValue(StatusCode.SUCCESS);
	status2.setStatusCode(statusCode2);
	response.setStatus(status2);

	Assertion assertion = buildAssertion(message);
	response.getAssertions().add(assertion);
	return response;
    }

    private Assertion buildAssertion(AttributeQuery query) throws Exception {
	Assertion assertion = buildSAMLObject(Assertion.class);

	Issuer issuer = buildSAMLObject(Issuer.class);
	issuer.setValue(issuerValue);
	assertion.setIssuer(issuer);
	assertion.setIssueInstant(new DateTime());

	assertion.setID(secureRandomIdGenerator.generateIdentifier());

	Subject subject = buildSAMLObject(Subject.class);
	NameID nameID = buildSAMLObject(NameID.class);
	nameID.setFormat(NameIDType.PERSISTENT);
	NameID queryNameID = query.getSubject().getNameID();
	nameID.setValue(queryNameID.getValue());
	nameID.setNameQualifier(queryNameID.getNameQualifier());
	subject.setNameID(nameID);
	subject.getSubjectConfirmations().add(buildSubjectConfirmation(query));
	assertion.setSubject(subject);

	assertion.getAttributeStatements().add(buildAttributeStatement(query));

	return assertion;
    }

    private SubjectConfirmation buildSubjectConfirmation(AttributeQuery query) {
	SubjectConfirmation subjectConfirmation = buildSAMLObject(SubjectConfirmation.class);
	subjectConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);
	
	SubjectConfirmationData subjectConfirmationData = buildSAMLObject(SubjectConfirmationData.class);
	subjectConfirmationData.setInResponseTo(query.getID());
	DateTime dateTime = new DateTime();
	subjectConfirmationData.setNotBefore(dateTime.minusDays(2));
	subjectConfirmationData.setNotOnOrAfter(dateTime.plusDays(2));
	subjectConfirmationData.setRecipient(query.getIssuer().getValue());
	subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);
	return subjectConfirmation;
    }

    private AttributeStatement buildAttributeStatement(AttributeQuery query) throws Exception {
	AttributeStatement attributeStatement = buildSAMLObject(AttributeStatement.class);
	int i, j;

	// get subject name from query
	String subjectName = query.getSubject().getNameID().getValue();

	// parse out names of requested attributes from query
	ArrayList<AttributeResult> results;

	List<Attribute> attributes = query.getAttributes();
	if ((attributes == null) || (attributes.size() == 0)) {
	    // if none given, assume querying all attributes
	    results = AttributeRetriever.getAllAttributeValues(subjectName);
	}
	else {
	    // call another class to get the attribute values required
	    results = new ArrayList<AttributeResult>();
	    for (i = 0; i < attributes.size(); i++) {
		Attribute attribute = attributes.get(i);
		results.add(AttributeRetriever.getAttributeValue(subjectName, attribute.getName()));
	    }
	}

	XSStringBuilder stringBuilder = (XSStringBuilder)XMLObjectProviderRegistrySupport.getBuilderFactory().getBuilder(XSString.TYPE_NAME);

	// build the response. handle single or multiple values for each attribute
	for (i = 0; i < results.size(); i++) {
	    AttributeResult result = results.get(i);
	    Attribute attribute = buildSAMLObject(Attribute.class);
	    attribute.setName(result.name);

	    switch (result.type) {
	    case AttributeResult.STRING:
		{
		    XSString attributeValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
		    attributeValue.setValue(result.stringval);
		    attribute.getAttributeValues().add(attributeValue);
		}
		break;
	    case AttributeResult.ARRAY:
		for (j = 0; j < result.arrayval.length; j++) {
		    XSString attributeValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
		    attributeValue.setValue(result.arrayval[j]);
		    attribute.getAttributeValues().add(attributeValue);
		}
		break;
	    }

	    attributeStatement.getAttributes().add(attribute);
	}

	return attributeStatement;
    }

    private <T> T buildSAMLObject(final Class<T> clazz) {
	T object = null;
	try {
	    XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
	    QName defaultElementName = (QName)clazz.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null);
	    object = (T)builderFactory.getBuilder(defaultElementName).buildObject(defaultElementName);
	}
	catch (IllegalAccessException e) {
	    throw new IllegalArgumentException("Could not create SAML object");
	}
	catch (NoSuchFieldException e) {
	    throw new IllegalArgumentException("Could not create SAML object");
	}
	return object;
    }
}
