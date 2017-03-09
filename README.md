# simpleaa

An XML web service that provides a minimalist SAML Attribute Authority. The service runs in Apache Axis 2 within Tomcat, and responds to SAML attribute queries via SOAP. The attribute values are retrieved from a MySQL database.

A more up-to-date version of the attribute authority, based on the OpenSAML library, is now included in the `safeaa` directory.

A script for querying the attribute authority via the web service client is included in `runaaclient.sh`.


## Deploying the service

To deploy the service, first install Apache Tomcat (version 8 has been tested but other versions should also work) and Apache Axis 2 as a webapp within Tomcat. You should also place the Drizzle JDBC jar in the `lib/` directory within simpleaa as this is required for accessing the MySQL database. Then compile the two service classes (you will need the Axis libraries in your classpath for this to work) and create a `.aar` file (Axis Archive) consisting of all the `.class` files generated, along with `META-INF/services.xml` and the contents of `lib/`. Example:

```
javac uk/ac/ed/epcc/simpleaa/SimpleAAService.java
javac uk/ac/ed/epcc/simpleaa/AADatabaseUpdater.java
jar cvf SimpleAAService.aar META-INF/* lib/* uk/ac/ed/epcc/simpleaa/*.class
```

Copy `SimpleAAService.aar` to the Axis 2 services directory. You may need to restart Tomcat for the new services to be picked up.

There are two separate services. The main service, which responds to SAML attribute queries, is `SimpleAAService`. There is also a secondary service called `AADatabaseUpdater` for writing to the database. If you don't wish to use the updater service (because you are writing to the database directly from another application instead, for example), you can omit this class from the archive and remove its information from `services.xml`.

Various service parameters can be set in `services.xml`. The database related ones are described below. The `IssuerFormat` and `IssuerName` to include in the SAML responses can also be set here.


## Deploying the database

The database structure is very simple. It consists of:

* a table of users (ID and name)
* a table of attribute types (ID, formal name and friendly name)
* a table of attribute values (user ID, attribute ID, attribute value)

No particular format is enforced for user names or attribute names or values - they are simply treated as strings by the service code.

The `simpleaa.sql` file can be used to initialise the database. It will create the three tables required and also populate the attribute types table with some common LDAP attributes.

Settings for connecting to the database are stored in `services.xml`. The URL, username and password are required here. If you are using both web services, remember to include the database settings in both service elements within `services.xml`.


## The client

A basic client is included in `SimpleAAClient.java`, providing static Java methods for accessing the services, as well as a command line interface to all of these. This is intended mainly for testing and to demonstrate how to access the services programmatically. Operations available are:

* `query` - perform a SAML attribute query against the service
* `addattrtype` - add a new attribute type to the database
* `adduser` - add a new user to the database
* `deleteuser` - deletes a user from the database
* `updateuserattr` - sets or updates the value of a given attribute for a given user (for attributes that can only have a single value, such as the user's name)
* `adduserattr` - adds a new value for a given attribute for a given user (for attributes that can have multiple values, such as group memberships)
* `removeuserattr` - removes a given attribute's value for a given user

The client depends on `AADatabaseUpdaterStub.java` in order to access the updater service. This file was autogenerated from the service WSDL using the Axis Data Binding tool and has not been modified, but is included in the source for convenience.


## License

This software is licensed under the Apache 2.0 License (see LICENSE for details).
