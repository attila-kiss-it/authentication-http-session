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

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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
import org.everit.osgi.authentication.context.AuthenticationPropagator;
import org.everit.osgi.authentication.http.session.AuthenticationSessionAttributeNames;
import org.everit.osgi.authentication.http.session.SessionAuthenticationConstants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.log.LogService;

/**
 * Authentication Filter:
 * <p>
 * Checks the {@link HttpSession} for an Authenticated Resource ID by the attribute name provided by
 * the {@link AuthenticationSessionAttributeNames}.
 *
 * If there is a <code>non-null</code> value assigned to this attribute name, executes the
 * authenticated process in the name of the Authenticated Resource. This means it invokes further
 * the filter chain via an {@link AuthenticationPropagator}.
 *
 * If there is no Authenticated Resource ID available in the session, the filter chain will be
 * processed further without any special extension.
 * </p>
 * Logout Servlet:
 * <p>
 * Invalidates the {@link HttpSession} of the request and redirects to a specified location.
 * </p>
 */
@Component(name = SessionAuthenticationConstants.SERVICE_FACTORYPID_SESSION_AUTHENTICATION,
    metatype = true,
    configurationFactory = true, policy = ConfigurationPolicy.REQUIRE, immediate = true)
@Properties({
    @Property(name = Constants.SERVICE_DESCRIPTION, propertyPrivate = false,
        value = SessionAuthenticationConstants.DEFAULT_SERVICE_DESCRIPTION),
    @Property(
        name = SessionAuthenticationConstants.PROP_SESSION_ATTR_NAME_AUTHENTICATED_RESOURCE_ID,
        value = SessionAuthenticationConstants.DEFAULT_SESSION_ATTR_NAME_AUTHENTICATED_RESOURCE_ID),
    @Property(name = SessionAuthenticationConstants.PROP_LOGGED_OUT_URL,
        value = SessionAuthenticationConstants.DEFAULT_LOGGED_OUT_URL),
    @Property(name = SessionAuthenticationConstants.PROP_REQ_PARAM_NAME_LOGGED_OUT_URL,
        value = SessionAuthenticationConstants.DEFAULT_REQ_PARAM_NAME_LOGGED_OUT_URL),
    @Property(name = SessionAuthenticationConstants.PROP_AUTHENTICATION_PROPAGATOR),
    @Property(name = SessionAuthenticationConstants.PROP_LOG_SERVICE),
})
@Service
public class SessionAuthenticationComponent
    extends HttpServlet
    implements Filter, AuthenticationSessionAttributeNames {

  private static final long serialVersionUID = 5302724920732803866L;

  @Reference(bind = "setAuthenticationPropagator")
  private AuthenticationPropagator authenticationPropagator;

  @Reference(bind = "setLogService")
  private LogService logService;

  private String loggedOutUrl;

  private String reqParamNameLoggedOutUrl;

  private String sessionAttrNameAuthenticatedResourceId;

  @Activate
  public void activate(final BundleContext context, final Map<String, Object> componentProperties)
      throws Exception {

    sessionAttrNameAuthenticatedResourceId = getStringProperty(componentProperties,
        SessionAuthenticationConstants.PROP_SESSION_ATTR_NAME_AUTHENTICATED_RESOURCE_ID);
    sessionAttrNameAuthenticatedResourceId = sessionAttrNameAuthenticatedResourceId.trim();
    if (sessionAttrNameAuthenticatedResourceId.isEmpty()) {
      throw new IllegalArgumentException("sessionAttrNameAuthenticatedResourceId cannot be blank");
    }

    loggedOutUrl = getStringProperty(componentProperties,
        SessionAuthenticationConstants.PROP_LOGGED_OUT_URL);
    loggedOutUrl = loggedOutUrl.trim();
    if (loggedOutUrl.isEmpty()) {
      throw new IllegalArgumentException("loggedOutUrl cannot be blank");
    }

    reqParamNameLoggedOutUrl = getStringProperty(componentProperties,
        SessionAuthenticationConstants.PROP_REQ_PARAM_NAME_LOGGED_OUT_URL);
    reqParamNameLoggedOutUrl = reqParamNameLoggedOutUrl.trim();
    if (reqParamNameLoggedOutUrl.isEmpty()) {
      throw new IllegalArgumentException("reqParamNameLoggedOutUrl cannot be blank");
    }
  }

  @Override
  public String authenticatedResourceId() {
    return sessionAttrNameAuthenticatedResourceId;
  }

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response,
      final FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
    HttpSession httpSession = httpServletRequest.getSession(false);

    Long authenticatedResourceId = httpSession == null
        ? null
        : (Long) httpSession.getAttribute(sessionAttrNameAuthenticatedResourceId);

    if (authenticatedResourceId != null) {
      doFilterAsAuthenticatedResource(request, response, chain, authenticatedResourceId);
    } else {
      doFilterAsDefaultResource(request, response, chain);
    }

  }

  private void doFilterAsAuthenticatedResource(final ServletRequest request,
      final ServletResponse response,
      final FilterChain chain, final Long authenticatedResourceId)
      throws IOException, ServletException {
    Exception exception = authenticationPropagator.runAs(authenticatedResourceId, () -> {
      try {
        chain.doFilter(request, response);
        return null;
      } catch (IOException | ServletException e) {
        logService.log(LogService.LOG_ERROR, "Authenticated process execution failed", e);
        return e;
      }
    });
    if (exception != null) {
      if (exception instanceof IOException) {
        throw (IOException) exception;
      } else if (exception instanceof ServletException) {
        throw (ServletException) exception;
      }
    }
  }

  private void doFilterAsDefaultResource(final ServletRequest request,
      final ServletResponse response,
      final FilterChain chain) throws IOException, ServletException {
    chain.doFilter(request, response);
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException,
      IOException {
    logout(req, resp);
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {
    logout(req, resp);
  }

  private String getStringProperty(final Map<String, Object> componentProperties,
      final String propertyName)
      throws ConfigurationException {
    Object value = componentProperties.get(propertyName);
    if (value == null) {
      throw new ConfigurationException(propertyName, "property not defined");
    }
    return String.valueOf(value);
  }

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
  }

  private void logout(final HttpServletRequest req, final HttpServletResponse resp)
      throws IOException {

    Optional.ofNullable(req.getSession(false))
        .ifPresent((httpSession) -> {
          try {
            httpSession.invalidate();
          } catch (IllegalStateException e) {
            logService.log(LogService.LOG_DEBUG, e.getMessage(), e);
          }
        });

    String reqLoggedOutUrl = req.getParameter(reqParamNameLoggedOutUrl);
    if (reqLoggedOutUrl != null) {
      resp.sendRedirect(reqLoggedOutUrl);
    } else {
      resp.sendRedirect(loggedOutUrl);
    }
  }

  public void setAuthenticationPropagator(final AuthenticationPropagator authenticationPropagator) {
    this.authenticationPropagator = authenticationPropagator;
  }

  public void setLogService(final LogService logService) {
    this.logService = logService;
  }

}
