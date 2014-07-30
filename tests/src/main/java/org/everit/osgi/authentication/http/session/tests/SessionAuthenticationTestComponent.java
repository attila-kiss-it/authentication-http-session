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
package org.everit.osgi.authentication.http.session.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.everit.osgi.authentication.context.AuthenticationContext;
import org.everit.osgi.dev.testrunner.TestDuringDevelopment;
import org.everit.osgi.dev.testrunner.TestRunnerConstants;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;

@Component(name = "SessionAuthenticationTest", immediate = true, configurationFactory = false,
        policy = ConfigurationPolicy.OPTIONAL)
@Properties({
        @Property(name = TestRunnerConstants.SERVICE_PROPERTY_TESTRUNNER_ENGINE_TYPE, value = "junit4"),
        @Property(name = TestRunnerConstants.SERVICE_PROPERTY_TEST_ID, value = "SessionAuthenticationTest"),
        @Property(name = "httpService.target", value = "(org.osgi.service.http.port=*)"),
        @Property(name = "authenticationContext.target")
})
@Service(value = SessionAuthenticationTestComponent.class)
public class SessionAuthenticationTestComponent {

    @Reference(bind = "setHttpService")
    private HttpService httpService;

    @Reference(bind = "setAuthenticationContext")
    private AuthenticationContext authenticationContext;

    private int port;

    private String helloUrl;

    private long defaultResourceId;

    @Activate
    public void activate(final BundleContext context, final Map<String, Object> componentProperties)
            throws Exception {
        helloUrl = "http://localhost:" + port + "/hello";

        defaultResourceId = authenticationContext.getDefaultResourceId();
    }

    private long hello(final HttpContext httpContext, final long expectedResourceId)
            throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(helloUrl);
        HttpResponse httpResponse = httpClient.execute(httpGet, httpContext);
        Assert.assertEquals(HttpServletResponse.SC_OK, httpResponse.getStatusLine().getStatusCode());
        HttpEntity responseEntity = httpResponse.getEntity();
        InputStream inputStream = responseEntity.getContent();
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer);
        String[] responseBodyAsString = writer.toString().split(":");
        long actualResourceId = Long.valueOf(responseBodyAsString[0]).longValue();
        long newResourceId = Long.valueOf(responseBodyAsString[1]).longValue();
        Assert.assertEquals(expectedResourceId, actualResourceId);
        return newResourceId;
    }

    public void setAuthenticationContext(final AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    public void setHttpService(final HttpService httpService, final Map<String, Object> properties) {
        this.httpService = httpService;
        port = Integer.valueOf((String) properties.get("org.osgi.service.http.port"));
        port--; // TODO port must be decremented because the port of the Server is less than the value of the service
        // portperty queried above
    }

    @Test
    @TestDuringDevelopment
    public void testAccessHelloPage() throws Exception {
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        long sessionResourceId = hello(httpContext, defaultResourceId);
        hello(httpContext, sessionResourceId);

        cookieStore = new BasicCookieStore();
        httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        hello(httpContext, defaultResourceId);
    }

}
