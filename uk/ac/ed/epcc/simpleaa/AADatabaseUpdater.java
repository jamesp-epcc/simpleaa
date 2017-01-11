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
 * A simple web service for updating the attribute database
 */

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

public class AADatabaseUpdater {
    // Error codes
    public static final int SUCCESS = 0;
    public static final int ATTRIBUTE_ALREADY_EXISTS = 1;
    public static final int INTERNAL_SERVER_ERROR = 2;
    public static final int USER_ALREADY_EXISTS = 3;
    public static final int USER_DOES_NOT_EXIST = 4;
    public static final int ATTRIBUTE_DOES_NOT_EXIST = 5;

    // add a new attribute type to the database
    public int addAttributeType(String name, String friendlyName) {
	try {
	    // check it doesn't already exist
	    String fn = null;
	    try {
		fn = Util.getFriendlyName(name);
	    }
	    catch (InvalidAttributeNameException iane) {
	    }
	    if (fn != null) {
		// it exists already
		return ATTRIBUTE_ALREADY_EXISTS;
	    }

	    // now create it
	    Connection con = Util.getDatabaseConnection();
	    PreparedStatement ps = con.prepareStatement("insert into attribute_types(name, friendly_name) values (?, ?)");
	    ps.setString(1, name);
	    ps.setString(2, friendlyName);
	    ps.executeUpdate();
	    ps.close();
	    con.close();
	}
	catch (ClassNotFoundException cnfe) {
	    return INTERNAL_SERVER_ERROR;
	}
	catch (SQLException se) {
	    return INTERNAL_SERVER_ERROR;
	}
	       
	return SUCCESS;
    }

    // add a new user to the database
    public int addUser(String name) {
	try {
	    // check if user already exists
	    Connection con = Util.getDatabaseConnection();
	    PreparedStatement ps = con.prepareStatement("select id from users where name=?");
	    ps.setString(1, name);
	    ResultSet rs = ps.executeQuery();
	    if (rs.first()) {
		ps.close();
		con.close();
		return USER_ALREADY_EXISTS;
	    }
	    ps.close();

	    // create new user now
	    ps = con.prepareStatement("insert into users values (null, ?)");
	    ps.setString(1, name);
	    ps.executeUpdate();
	    ps.close();
	    con.close();
	}
	catch (ClassNotFoundException cnfe) {
	    return INTERNAL_SERVER_ERROR;
	}
	catch (SQLException se) {
	    return INTERNAL_SERVER_ERROR;
	}

	return SUCCESS;
    }

    // adds a new attribute value for a user. should only be used with attributes
    // that can have multiple values. otherwise, use setAttributeForUser
    public int addAttributeForUser(String userName, String attrName, String attrValue) {
	try {
	    // get user ID first
	    Connection con = Util.getDatabaseConnection();
	    PreparedStatement ps = con.prepareStatement("select id from users where name=?");
	    ps.setString(1, userName);
	    ResultSet rs = ps.executeQuery();
	    if (!rs.first()) {
		ps.close();
		con.close();
		return USER_DOES_NOT_EXIST;
	    }
	    int userId = rs.getInt("id");
	    ps.close();
	    
	    // get attribute ID
	    ps = con.prepareStatement("select id from attribute_types where name=?");
	    ps.setString(1, attrName);
	    rs = ps.executeQuery();
	    if (!rs.first()) {
		ps.close();
		con.close();
		return ATTRIBUTE_DOES_NOT_EXIST;
	    }
	    int attrId = rs.getInt("id");
	    ps.close();

	    // insert new attribute value
	    ps = con.prepareStatement("insert into user_attributes values (?, ?, ?)");
	    ps.setInt(1, userId);
	    ps.setInt(2, attrId);
	    ps.setString(3, attrValue);
	    ps.executeUpdate();
	    ps.close();
	    con.close();
	}
	catch (ClassNotFoundException cnfe) {
	    return INTERNAL_SERVER_ERROR;
	}
	catch (SQLException se) {
	    return INTERNAL_SERVER_ERROR;
	}

	return SUCCESS;
    }

    // remove an attribute value for a user
    public int removeAttributeForUser(String userName, String attrName, String attrValue) {
	try {
	    // get user ID first
	    Connection con = Util.getDatabaseConnection();
	    PreparedStatement ps = con.prepareStatement("select id from users where name=?");
	    ps.setString(1, userName);
	    ResultSet rs = ps.executeQuery();
	    if (!rs.first()) {
		ps.close();
		con.close();
		return USER_DOES_NOT_EXIST;
	    }
	    int userId = rs.getInt("id");
	    ps.close();
	    
	    // get attribute ID
	    ps = con.prepareStatement("select id from attribute_types where name=?");
	    ps.setString(1, attrName);
	    rs = ps.executeQuery();
	    if (!rs.first()) {
		ps.close();
		con.close();
		return ATTRIBUTE_DOES_NOT_EXIST;
	    }
	    int attrId = rs.getInt("id");
	    ps.close();

	    // delete the attribute value
	    ps = con.prepareStatement("delete from user_attributes where user_id=? and attribute_id=? and value=?");
	    ps.setInt(1, userId);
	    ps.setInt(2, attrId);
	    ps.setString(3, attrValue);
	    ps.executeUpdate();
	    ps.close();
	    con.close();
	}
	catch (ClassNotFoundException cnfe) {
	    return INTERNAL_SERVER_ERROR;
	}
	catch (SQLException se) {
	    return INTERNAL_SERVER_ERROR;
	}

	return SUCCESS;
    }

    // set an attribute value for a user
    // will modify the existing entry if it exists, otherwise create a new entry
    // should only be used for attributes that have a single value! use
    // addAttributeForUser and removeAttributeForUser for attributes that can have
    // multiple values
    public int setAttributeForUser(String userName, String attrName, String attrValue) {
	try {
	    // get user ID first
	    Connection con = Util.getDatabaseConnection();
	    PreparedStatement ps = con.prepareStatement("select id from users where name=?");
	    ps.setString(1, userName);
	    ResultSet rs = ps.executeQuery();
	    if (!rs.first()) {
		ps.close();
		con.close();
		return USER_DOES_NOT_EXIST;
	    }
	    int userId = rs.getInt("id");
	    ps.close();
	    
	    // get attribute ID
	    ps = con.prepareStatement("select id from attribute_types where name=?");
	    ps.setString(1, attrName);
	    rs = ps.executeQuery();
	    if (!rs.first()) {
		ps.close();
		con.close();
		return ATTRIBUTE_DOES_NOT_EXIST;
	    }
	    int attrId = rs.getInt("id");
	    ps.close();

	    // now check if attribute already exists for user
	    ps = con.prepareStatement("select value from user_attributes where user_id=? and attribute_id=?");
	    ps.setInt(1, userId);
	    ps.setInt(2, attrId);
	    String result = "";
	    rs = ps.executeQuery();
	    if (rs.first()) {
		// if so, update it
		ps.close();

		ps = con.prepareStatement("update user_attributes set value=? where user_id=? and attribute_id=?");
		ps.setString(1, attrValue);
		ps.setInt(2, userId);
		ps.setInt(3, attrId);
		ps.executeUpdate();
		ps.close();
	    }
	    else {
		// if not, create it
		ps.close();

		ps = con.prepareStatement("insert into user_attributes values (?, ?, ?)");
		ps.setInt(1, userId);
		ps.setInt(2, attrId);
		ps.setString(3, attrValue);
		ps.executeUpdate();
		ps.close();
	    }
	    con.close();
	}
	catch (ClassNotFoundException cnfe) {
	    return INTERNAL_SERVER_ERROR;
	}
	catch (SQLException se) {
	    return INTERNAL_SERVER_ERROR;
	}

	return SUCCESS;
    }

    // delete a user from the database
    public int deleteUser(String name) {
	try {
	    // check if user exists
	    Connection con = Util.getDatabaseConnection();
	    PreparedStatement ps = con.prepareStatement("select id from users where name=?");
	    ps.setString(1, name);
	    ResultSet rs = ps.executeQuery();
	    if (!rs.first()) {
		ps.close();
		con.close();
		return USER_DOES_NOT_EXIST;
	    }
	    int userId = rs.getInt("id");
	    ps.close();

	    // delete user's attribute entries as well
	    ps = con.prepareStatement("delete from user_attributes where user_id=?");
	    ps.setInt(1, userId);
	    ps.executeUpdate();
	    ps.close();

	    ps = con.prepareStatement("delete from users where name=?");
	    ps.setString(1, name);
	    ps.executeUpdate();
	    ps.close();
	    con.close();
	}
	catch (ClassNotFoundException cnfe) {
	    return INTERNAL_SERVER_ERROR;
	}
	catch (SQLException se) {
	    return INTERNAL_SERVER_ERROR;
	}

	return SUCCESS;
    }
}
