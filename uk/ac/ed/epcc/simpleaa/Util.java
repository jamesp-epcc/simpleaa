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
 * Utility methods for the attribute authority
 */

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.Formatter;
import java.text.SimpleDateFormat;
import java.security.SecureRandom;
import java.net.InetAddress;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;

public class Util {
    // returns a connection to the MySQL database
    // connection information is stored in parameters in the services.xml file
    public static Connection getDatabaseConnection() throws ClassNotFoundException, SQLException {
	Class.forName("org.drizzle.jdbc.DrizzleDriver");

	String dburl = "jdbc:mysql://localhost:3306/simpleaa";
	String dbuser = "simpleaa";
	String dbpassword = "simpleaa";

	MessageContext mc = MessageContext.getCurrentMessageContext();
	Parameter dburlparam = mc.getAxisService().getParameter("dburl");
	if (dburlparam != null) {
	    dburl = dburlparam.getParameterElement().getText();
	}
	Parameter dbuparam = mc.getAxisService().getParameter("dbuser");
	if (dbuparam != null) {
	    dbuser = dbuparam.getParameterElement().getText();
	}
	Parameter dbpparam = mc.getAxisService().getParameter("dbpassword");
	if (dbpparam != null) {
	    dbpassword = dbpparam.getParameterElement().getText();
	}

	if (!dburl.contains(":thin:")) {
	    dburl = dburl.replaceAll("mysql:", "mysql:thin:");
	}
	Connection con = DriverManager.getConnection(dburl, dbuser, dbpassword);
	return con;
    }

    // gets all attributes for the given user from the database
    public static ArrayList<AttributeResult> getAllAttributesForUser(String user) throws SQLException, ClassNotFoundException, UnknownSubjectException {

	// first get user ID from database users table
	Connection con = getDatabaseConnection();
	PreparedStatement ps = con.prepareStatement("select id from users where name=?");
	ps.setString(1, user);
	ResultSet rs = ps.executeQuery();
	if (!rs.first()) throw new UnknownSubjectException("User '" + user + "' not known");
	int userId = rs.getInt("id");
	ps.close();

	// now get all attributes for that user
	ArrayList<AttributeResult> result = new ArrayList<AttributeResult>();
	
	ps = con.prepareStatement("select attribute_id,value from user_attributes where user_id=?");
	ps.setInt(1, userId);
	rs = ps.executeQuery();
	while (rs.next()) {
	    int attrId = rs.getInt("attribute_id");
	    String value = rs.getString("value");

	    // look up attribute name
	    PreparedStatement ps2 = con.prepareStatement("select name from attribute_types where id=?");
	    ps2.setInt(1, attrId);
	    ResultSet rs2 = ps2.executeQuery();
	    rs2.first();
	    String attrname = rs2.getString("name");
	    ps2.close();

	    result.add(new AttributeResult(attrname, value));
	}
	ps.close();

	con.close();
	return result;
    }

    // get named attribute for the given user from the database
    public static String getAttributeForUser(String user, String attr) throws SQLException, ClassNotFoundException, UnknownSubjectException, InvalidAttributeNameException {
	// first get user ID from database users table
	Connection con = getDatabaseConnection();
	PreparedStatement ps = con.prepareStatement("select id from users where name=?");
	ps.setString(1, user);
	ResultSet rs = ps.executeQuery();
	if (!rs.first()) throw new UnknownSubjectException("User '" + user + "' not known");
	int userId = rs.getInt("id");
	ps.close();

	// then get attribute ID from database attributes table
	ps = con.prepareStatement("select id from attribute_types where name=?");
	ps.setString(1, attr);
	rs = ps.executeQuery();
	if (!rs.first()) throw new InvalidAttributeNameException("Attribute name '" + attr + "' not recognised");
	int attrId = rs.getInt("id");
	ps.close();

	// now query for the attribute value
	ps = con.prepareStatement("select value from user_attributes where user_id=? and attribute_id=?");
	ps.setInt(1, userId);
	ps.setInt(2, attrId);
	String result = "";
	rs = ps.executeQuery();
	if (rs.first()) {
	    result = rs.getString("value");
	}
	ps.close();
	
	con.close();
	return result;
    }

    // get friendly name for an attribute, given its formal name
    public static String getFriendlyName(String attr) throws ClassNotFoundException, SQLException, InvalidAttributeNameException {

	Connection con = getDatabaseConnection();
	PreparedStatement ps = con.prepareStatement("select friendly_name from attribute_types where name=?");
	ps.setString(1, attr);
	ResultSet rs = ps.executeQuery();
	if (!rs.first()) throw new InvalidAttributeNameException("Attribute name '" + attr + "' not recognised");
	String result = rs.getString("friendly_name");
	ps.close();
	con.close();
	return result;
    }

    // get the current time in UTC as a string
    public static String getTimeUTC() {
	SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
	return fmt.format(new Date());
    }

    // get the instant five minutes in the future in UTC as a string
    public static String getTimePlus5Minutes() {
	SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
	Date date = new Date(System.currentTimeMillis() + 5 * 60 * 1000);
	return fmt.format(date);
    }

    // generate a unique ID for a SAML request or response
    public static String generateID() {
	SecureRandom rng = new SecureRandom();
	rng.setSeed((long)(Math.random() * 1000000000000.0));
	rng.setSeed(System.nanoTime());

	try {
	    String hostname = InetAddress.getLocalHost().getHostName();
	    rng.setSeed(hostname.getBytes());
	}
	catch (Exception ex) {
	}

	byte[] idbytes = new byte[16];
	rng.nextBytes(idbytes);

	int i;
	StringBuilder sb = new StringBuilder(32);
	for (i = 0; i < 16; i++) {
	    sb.append(String.format("%02x", idbytes[i]));
	}

	return sb.toString();
    }
}
