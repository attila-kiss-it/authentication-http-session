/**
 * This file is part of Everit - HTTP Session based authentication.
 *
 * Everit - HTTP Session based authentication is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - HTTP Session based authentication is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - HTTP Session based authentication.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.authentication.http.session;

/**
 * Constants of the Session Authentication component.
 */
public final class SessionAuthenticationConstants {

    /**
     * The service factory PID of the Session Authentication component.
     */
    public static final String SERVICE_FACTORYPID_SESSION_AUTHENTICATION =
            "org.everit.osgi.authentication.http.session.SessionAuthentication";

    public static final String PROP_AUTHENTICATION_PROPAGATOR = "authenticationPropagator.target";

    public static final String PROP_LOG_SERVICE = "logService.target";

    public static final String PROP_SESSION_LOGOUT_SERVLET_SUCCESS_LOGOUT_URL = "session.logout.servlet.success.logout.url";

    public static final String DEFAULT_SESSION_LOGOUT_SERVLET_SUCCESS_LOGOUT_URL = "/logout.html";

    public static final String PROP_SESSION_ATTR_NAME_AUTHENTICATED_RESOURCE_ID =
            "session.attr.name.authenticated.resource.id";

    public static final String DEFAULT_SESSION_ATTR_NAME_AUTHENTICATED_RESOURCE_ID = "authenticated.resource.id";

    private SessionAuthenticationConstants() {
    }

}
