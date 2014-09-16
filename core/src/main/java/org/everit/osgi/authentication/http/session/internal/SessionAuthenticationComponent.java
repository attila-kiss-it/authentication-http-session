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
package org.everit.osgi.authentication.http.session.internal;

import java.util.Hashtable;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.Servlet;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.authentication.context.AuthenticationPropagator;
import org.everit.osgi.authentication.http.session.AuthenticationSessionAttributeNames;
import org.everit.osgi.authentication.http.session.SessionAuthenticationConstants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.log.LogService;

@Component(name = SessionAuthenticationConstants.SERVICE_FACTORYPID_SESSION_AUTHENTICATION, metatype = true,
        configurationFactory = true, policy = ConfigurationPolicy.REQUIRE)
@Properties({
        @Property(name = SessionAuthenticationConstants.PROP_SESSION_LOGOUT_SERVLET_LOGGED_OUT_URL,
                value = SessionAuthenticationConstants.DEFAULT_SESSION_LOGOUT_SERVLET_LOGGED_OUT_URL),
        @Property(name = SessionAuthenticationConstants.PROP_REQ_PARAM_NAME_LOGGED_OUT_URL,
                value = SessionAuthenticationConstants.DEFAULT_REQ_PARAM_NAME_LOGGED_OUT_URL),
        @Property(name = SessionAuthenticationConstants.PROP_SESSION_ATTR_NAME_AUTHENTICATED_RESOURCE_ID,
                value = SessionAuthenticationConstants.DEFAULT_SESSION_ATTR_NAME_AUTHENTICATED_RESOURCE_ID),
        @Property(name = SessionAuthenticationConstants.PROP_AUTHENTICATION_PROPAGATOR),
        @Property(name = SessionAuthenticationConstants.PROP_LOG_SERVICE),
})
@Service
public class SessionAuthenticationComponent implements AuthenticationSessionAttributeNames {

    @Reference(bind = "setAuthenticationPropagator")
    private AuthenticationPropagator authenticationPropagator;

    @Reference(bind = "setLogService")
    private LogService logService;

    private ServiceRegistration<Filter> sessionAuthenticationFilterSR;

    private ServiceRegistration<Servlet> sessionLogoutServletSR;

    private String sessionAttrNameAuthenticatedResourceId;

    @Activate
    public void activate(final BundleContext context, final Map<String, Object> componentProperties) throws Exception {

        sessionAttrNameAuthenticatedResourceId = getStringProperty(componentProperties,
                SessionAuthenticationConstants.PROP_SESSION_ATTR_NAME_AUTHENTICATED_RESOURCE_ID);
        String loggedOutUrl = getStringProperty(componentProperties,
                SessionAuthenticationConstants.PROP_SESSION_LOGOUT_SERVLET_LOGGED_OUT_URL);
        String reqParamNameLoggedOutUrl = getStringProperty(componentProperties,
                SessionAuthenticationConstants.PROP_REQ_PARAM_NAME_LOGGED_OUT_URL);

        Filter sessionAuthenticationFilter = new SessionAuthenticationFilter(authenticationPropagator,
                sessionAttrNameAuthenticatedResourceId, logService);

        Hashtable<String, Object> properties = new Hashtable<>();
        properties.putAll(componentProperties);

        sessionAuthenticationFilterSR = context.registerService(
                Filter.class, sessionAuthenticationFilter, properties);

        Servlet sessionLogoutServlet = new SessionLogoutServlet(reqParamNameLoggedOutUrl, loggedOutUrl, logService);

        sessionLogoutServletSR = context.registerService(
                Servlet.class, sessionLogoutServlet, properties);
    }

    @Override
    public String authenticatedResourceId() {
        return sessionAttrNameAuthenticatedResourceId;
    }

    @Deactivate
    public void deactivate() {
        if (sessionAuthenticationFilterSR != null) {
            sessionAuthenticationFilterSR.unregister();
            sessionAuthenticationFilterSR = null;
        }
        if (sessionLogoutServletSR != null) {
            sessionLogoutServletSR.unregister();
            sessionLogoutServletSR = null;
        }
    }

    private String getStringProperty(final Map<String, Object> componentProperties, final String propertyName)
            throws ConfigurationException {
        Object value = componentProperties.get(propertyName);
        if (value == null) {
            throw new ConfigurationException(propertyName, "property not defined");
        }
        return String.valueOf(value);
    }

    public void setAuthenticationPropagator(final AuthenticationPropagator authenticationPropagator) {
        this.authenticationPropagator = authenticationPropagator;
    }

    public void setLogService(final LogService logService) {
        this.logService = logService;
    }

}
