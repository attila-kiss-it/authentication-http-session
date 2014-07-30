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

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.Filter;

import org.apache.felix.http.whiteboard.HttpWhiteboardConstants;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.everit.osgi.authentication.context.AuthenticationContext;
import org.everit.osgi.authentication.context.AuthenticationPropagator;
import org.everit.osgi.authentication.http.session.SessionAuthenticationConstants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.log.LogService;

@Component(name = SessionAuthenticationConstants.SERVICE_FACTORYPID_SESSION_AUTHENTICATION, metatype = true,
        configurationFactory = true, policy = ConfigurationPolicy.REQUIRE)
@Properties({
        @Property(name = SessionAuthenticationConstants.PROP_FILTER_NAME,
                value = SessionAuthenticationConstants.DEFAULT_FILTER_NAME),
        @Property(name = HttpWhiteboardConstants.PATTERN,
                value = SessionAuthenticationConstants.DEFAULT_PATTERN),
        @Property(name = HttpWhiteboardConstants.CONTEXT_ID,
                value = SessionAuthenticationConstants.DEFAULT_CONTEXT_ID),
        @Property(name = SessionAuthenticationConstants.PROP_RANKING,
                value = SessionAuthenticationConstants.DEFAULT_RANKING),
        @Property(name = SessionAuthenticationConstants.PROP_SESSION_ATTR_NAME_AUTHENTICATED_RESOURCE_ID,
                value = SessionAuthenticationConstants.DEFAULT_SESSION_PARAM_NAME_AUTHENTICATED_RESOURCE_ID),
        @Property(name = SessionAuthenticationConstants.PROP_AUTHENTICATION_CONTEXT),
        @Property(name = SessionAuthenticationConstants.PROP_AUTHENTICATION_PROPAGATOR),
        @Property(name = SessionAuthenticationConstants.PROP_LOG_SERVICE),
})
public class SessionAuthenticationComponent {

    @Reference(bind = "setAuthenticationContext")
    private AuthenticationContext authenticationContext;

    @Reference(bind = "setAuthenticationPropagator")
    private AuthenticationPropagator authenticationPropagator;

    @Reference(bind = "setLogService")
    private LogService logService;

    private ServiceRegistration<Filter> sessionAuthenticationFilterSR;

    @Activate
    public void activate(final BundleContext context, final Map<String, Object> componentProperties) throws Exception {
        String sessionAttrNameAuthenticatedResourceId = getStringProperty(componentProperties,
                SessionAuthenticationConstants.PROP_SESSION_ATTR_NAME_AUTHENTICATED_RESOURCE_ID);

        Filter sessionAuthenticationFilter = new SessionAuthenticationFilter(authenticationPropagator,
                authenticationContext, sessionAttrNameAuthenticatedResourceId, logService);

        String filterName =
                getStringProperty(componentProperties, SessionAuthenticationConstants.PROP_FILTER_NAME);
        String pattern =
                getStringProperty(componentProperties, HttpWhiteboardConstants.PATTERN);
        String contextId =
                getStringProperty(componentProperties, HttpWhiteboardConstants.CONTEXT_ID);
        Long ranking =
                Long.valueOf(getStringProperty(componentProperties, SessionAuthenticationConstants.PROP_RANKING));

        Dictionary<String, Object> filterProperties = new Hashtable<>();
        filterProperties.put(SessionAuthenticationConstants.PROP_FILTER_NAME, filterName);
        filterProperties.put(HttpWhiteboardConstants.PATTERN, pattern);
        filterProperties.put(HttpWhiteboardConstants.CONTEXT_ID, contextId);
        filterProperties.put(Constants.SERVICE_RANKING, ranking);
        sessionAuthenticationFilterSR =
                context.registerService(Filter.class, sessionAuthenticationFilter, filterProperties);
    }

    @Deactivate
    public void deactivate() {
        if (sessionAuthenticationFilterSR != null) {
            sessionAuthenticationFilterSR.unregister();
            sessionAuthenticationFilterSR = null;
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

    public void setAuthenticationContext(final AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    public void setAuthenticationPropagator(final AuthenticationPropagator authenticationPropagator) {
        this.authenticationPropagator = authenticationPropagator;
    }

    public void setLogService(final LogService logService) {
        this.logService = logService;
    }

}
