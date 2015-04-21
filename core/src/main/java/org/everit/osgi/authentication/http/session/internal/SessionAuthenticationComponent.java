/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.biz)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.osgi.authentication.http.session.internal;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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
    implements Filter, AuthenticationSessionAttributeNames, Servlet {

  @Reference(bind = "setAuthenticationPropagator")
  private AuthenticationPropagator authenticationPropagator;

  @Reference(bind = "setLogService")
  private LogService logService;

  private String loggedOutUrl;

  private String reqParamNameLoggedOutUrl;

  private String sessionAttrNameAuthenticatedResourceId;

  private ServletConfig config;

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
      } else if (exception instanceof RuntimeException) {
        throw (RuntimeException) exception;
      }
    }
  }

  private void doFilterAsDefaultResource(final ServletRequest request,
      final ServletResponse response,
      final FilterChain chain) throws IOException, ServletException {
    chain.doFilter(request, response);
  }

  @Override
  public ServletConfig getServletConfig() {
    return config;
  }

  @Override
  public String getServletInfo() {
    return "";
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

  @Override
  public void init(final ServletConfig pConfig) throws ServletException {
    config = pConfig;
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
      resp.sendRedirect(URLEncoder.encode(reqLoggedOutUrl, "UTF-8"));
    } else {
      resp.sendRedirect(loggedOutUrl);
    }
  }

  @Override
  public void service(final ServletRequest req, final ServletResponse res) throws ServletException,
      IOException {
    HttpServletRequest request;
    HttpServletResponse response;

    if (!((req instanceof HttpServletRequest) && (res instanceof HttpServletResponse))) {
      throw new ServletException("non-HTTP request or response");
    }

    request = (HttpServletRequest) req;
    response = (HttpServletResponse) res;

    logout(request, response);
  }

  public void setAuthenticationPropagator(final AuthenticationPropagator authenticationPropagator) {
    this.authenticationPropagator = authenticationPropagator;
  }

  public void setLogService(final LogService logService) {
    this.logService = logService;
  }

}
