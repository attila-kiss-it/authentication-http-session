/**
 * This file is part of Everit - HTTP Session based authentication tests.
 *
 * Everit - HTTP Session based authentication tests is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - HTTP Session based authentication tests is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - HTTP Session based authentication tests.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.authentication.http.session.tests.sample;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Random;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.felix.http.whiteboard.HttpWhiteboardConstants;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.authentication.context.AuthenticationContext;
import org.everit.osgi.authentication.http.session.SessionAuthenticationConstants;
import org.everit.osgi.http.context.simple.HttpContextSimpleConstants;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.annotations.Activate;

@Component(name = "HelloWorldServletComponent", metatype = true,
        configurationFactory = true, policy = ConfigurationPolicy.REQUIRE)
@Properties({
        @Property(name = HttpWhiteboardConstants.ALIAS,
                value = "/hello"),
        @Property(name = HttpWhiteboardConstants.CONTEXT_ID,
                value = HttpContextSimpleConstants.DEFAULT_CONTEXT_ID),
        @Property(name = SessionAuthenticationConstants.PROP_SESSION_ATTR_NAME_AUTHENTICATED_RESOURCE_ID,
                value = SessionAuthenticationConstants.DEFAULT_SESSION_PARAM_NAME_AUTHENTICATED_RESOURCE_ID),
        @Property(name = "authenticationContext.target")
})
@Service(value = Servlet.class)
public class HelloWorldServletComponent extends HttpServlet {

    private static final long serialVersionUID = -5545883781165913751L;

    @Reference
    private AuthenticationContext authenticationContext;

    private String sessionAttrNameAuthenticatedResourceId;

    @Activate
    public void activate(final BundleContext context, final Map<String, Object> componentProperties) throws Exception {
        sessionAttrNameAuthenticatedResourceId = getStringProperty(componentProperties,
                SessionAuthenticationConstants.PROP_SESSION_ATTR_NAME_AUTHENTICATED_RESOURCE_ID);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
            IOException {
        long currentResourceId = authenticationContext.getCurrentResourceId();

        HttpSession httpSession = req.getSession();
        long newResourceId = new Random().nextLong();
        httpSession.setAttribute(sessionAttrNameAuthenticatedResourceId, newResourceId);

        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();
        out.print(currentResourceId + ":" + newResourceId);
    }

    private String getStringProperty(final Map<String, Object> componentProperties, final String propertyName)
            throws ConfigurationException {
        Object value = componentProperties.get(propertyName);
        if (value == null) {
            throw new ConfigurationException(propertyName, "property not defined");
        }
        return String.valueOf(value);
    }

}
