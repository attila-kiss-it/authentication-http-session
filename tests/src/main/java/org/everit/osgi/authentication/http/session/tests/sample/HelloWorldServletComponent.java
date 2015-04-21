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
import java.util.Random;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.authentication.context.AuthenticationContext;
import org.everit.osgi.authentication.http.session.AuthenticationSessionAttributeNames;

@Component(name = "HelloWorldServletComponent", metatype = true, configurationFactory = true,
        policy = ConfigurationPolicy.REQUIRE, immediate = true)
@Properties({
        @Property(name = "authenticationSessionAttributeNames.target"),
        @Property(name = "authenticationContext.target")
})
@Service(value = Servlet.class)
public class HelloWorldServletComponent extends HttpServlet {

    private static final long serialVersionUID = -5545883781165913751L;

    @Reference(bind = "setAuthenticationContext")
    private AuthenticationContext authenticationContext;

    @Reference(bind = "setAuthenticationSessionAttributeNames")
    private AuthenticationSessionAttributeNames authenticationSessionAttributeNames;

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
            IOException {
        long currentResourceId = authenticationContext.getCurrentResourceId();
        StringBuilder sb = null;
        if (currentResourceId == 1) {
            sb = new StringBuilder();
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                sb.append("\tat ").append(stackTraceElement).append("\n");
            }
        }

        HttpSession httpSession = req.getSession();
        long newResourceId = new Random().nextLong();
        httpSession.setAttribute(authenticationSessionAttributeNames.authenticatedResourceId(), newResourceId);

        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();
        out.print(currentResourceId + ":" + newResourceId);
        if (sb != null) {
            out.print(":\n === Server stackrace for analizing Filter chain and Servlet invocations ===\n"
                    + sb.toString().replaceAll(":", "-->")
                    + " === Server stacktrace END ===\n");
        }
    }

    public void setAuthenticationContext(final AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    public void setAuthenticationSessionAttributeNames(
            final AuthenticationSessionAttributeNames authenticationSessionAttributeNames) {
        this.authenticationSessionAttributeNames = authenticationSessionAttributeNames;
    }

}
