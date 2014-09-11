authentication-http-session
===========================

This OSGi component is a part of the [Everit Authentication][1]. It uses the 
[Whiteboard Pattern][2] to enable the Servlet API in the OSGi environment. The 
[SessionAuthenticationComponent][3] registers three OSGi services:
 - [SessionAuthenticationFilter][4] (javax.servlet.Filter): executes 
 authenticated processes in the name of the Authenticated Resource assigned to 
 a Http Session
 - [SessionLogoutServlet][5] (javax.servlet.Servlet): invalidates the Http 
 Session assigned to the Authenticated Resource in case of logout
 - [AuthenticationSessionAttributeNames][6]: provides the session attribute 
 names between the services involved in the authentication mechanisms

For more information about the behavior of the services check the javadoc.

The component configuration can be done via Configuration Admin.

[1]: http://everitorg.wordpress.com/2014/07/31/everit-authentication
[2]: http://felix.apache.org/documentation/subprojects/apache-felix-http-service.html#using-the-whiteboard
[3]: https://github.com/everit-org/authentication-http-session/blob/master/core/src/main/java/org/everit/osgi/authentication/http/session/internal/SessionAuthenticationComponent.java
[4]: https://github.com/everit-org/authentication-http-session/blob/master/core/src/main/java/org/everit/osgi/authentication/http/session/internal/SessionAuthenticationFilter.java
[5]: https://github.com/everit-org/authentication-http-session/blob/master/core/src/main/java/org/everit/osgi/authentication/http/session/internal/SessionLogoutServlet.java
[6]: https://github.com/everit-org/authentication-http-session/blob/master/core/src/main/java/org/everit/osgi/authentication/http/session/AuthenticationSessionAttributeNames.java
