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

    public static final String PROP_FILTER_NAME = "filterName";

    public static final String PROP_RANKING = "ranking";

    public static final String PROP_SESSION_ATTR_NAME_AUTHENTICATED_RESOURCE_ID =
            "session.attr.name.authenticated.resource.id";

    public static final String PROP_AUTHENTICATION_CONTEXT = "authenticationContext.target";

    public static final String PROP_AUTHENTICATION_PROPAGATOR = "authenticationPropagator.target";

    public static final String PROP_LOG_SERVICE = "logService.target";

    /**
     * The default value of the {@link #PROP_FILTER_NAME}.
     */
    public static final String DEFAULT_FILTER_NAME = "SessionAuthenticationFilter";

    /**
     * The default value of the {@link org.apache.felix.http.whiteboard.HttpWhiteboardConstants#PATTERN}.
     */
    public static final String DEFAULT_PATTERN = "/.*";

    /**
     * The default value of the {@link org.apache.felix.http.whiteboard.HttpWhiteboardConstants#CONTEXT_ID}.
     */
    public static final String DEFAULT_CONTEXT_ID = "defaultContext";

    /**
     * The default value of the {@link #PROP_RANKING}.
     */
    public static final String DEFAULT_RANKING = "0";

    public static final String DEFAULT_SESSION_PARAM_NAME_AUTHENTICATED_RESOURCE_ID = "authenticated.resource.id";

    private SessionAuthenticationConstants() {
    }

}
