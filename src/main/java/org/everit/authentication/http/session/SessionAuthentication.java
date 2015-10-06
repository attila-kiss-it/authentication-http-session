/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
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
package org.everit.authentication.http.session;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.everit.authentication.context.AuthenticationPropagator;
import org.everit.web.servlet.HttpServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class SessionAuthentication extends HttpServlet
    implements Filter, AuthenticationSessionAttributeNames {

  private static final Logger LOGGER = LoggerFactory.getLogger(SessionAuthentication.class);

  private AuthenticationPropagator authenticationPropagator;

  private String loggedOutUrl;

  private String reqParamNameLoggedOutUrl;

  private String sessionAttrNameAuthenticatedResourceId;

  /**
   * Constructor.
   *
   * @param sessionAttrNameAuthenticatedResourceId
   *          The name of the session attribute that stores the Resource ID of the authenticated
   *          user.
   * @param loggedOutUrl
   *          The URL where the browser will be redirected in case of logout.
   * @param reqParamNameLoggedOutUrl
   *          The name of the request parameter that overrides the "Logged out URL" configuration if
   *          present in the HTTP request.
   * @param authenticationPropagator
   *          the {@link AuthenticationPropagator} instance.
   *
   * @throws NullPointerException
   *           if one of the parameter is <code>null</code>.
   * @throws IllegalArgumentException
   *           if sessionAttrNameAuthenticatedResourceId or loggedOutUrl or reqParamNameLoggedOutUrl
   *           is blank.
   */
  public SessionAuthentication(final String sessionAttrNameAuthenticatedResourceId,
      final String loggedOutUrl, final String reqParamNameLoggedOutUrl,
      final AuthenticationPropagator authenticationPropagator) {
    Objects.requireNonNull(sessionAttrNameAuthenticatedResourceId,
        "sessionAttrNameAuthenticatedResourceId cannot be null");
    if (sessionAttrNameAuthenticatedResourceId.isEmpty()) {
      throw new IllegalArgumentException("sessionAttrNameAuthenticatedResourceId cannot be blank");
    }
    this.sessionAttrNameAuthenticatedResourceId = sessionAttrNameAuthenticatedResourceId.trim();

    Objects.requireNonNull(loggedOutUrl, "loggedOutUrl cannot be null");
    if (loggedOutUrl.isEmpty()) {
      throw new IllegalArgumentException("loggedOutUrl cannot be blank");
    }
    this.loggedOutUrl = loggedOutUrl.trim();

    Objects.requireNonNull(reqParamNameLoggedOutUrl, "reqParamNameLoggedOutUrl cannot be null.");
    if (reqParamNameLoggedOutUrl.isEmpty()) {
      throw new IllegalArgumentException("reqParamNameLoggedOutUrl cannot be blank");
    }
    this.reqParamNameLoggedOutUrl = reqParamNameLoggedOutUrl.trim();

    this.authenticationPropagator = Objects.requireNonNull(authenticationPropagator,
        "authenticationPropagator cannot be null.");
  }

  @Override
  public String authenticatedResourceId() {
    return sessionAttrNameAuthenticatedResourceId;
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
        LOGGER.error("Authenticated process execution failed", e);
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
  public void init(final FilterConfig filterConfig) throws ServletException {
  }

  private void logout(final HttpServletRequest req, final HttpServletResponse resp)
      throws IOException {

    Optional.ofNullable(req.getSession(false))
        .ifPresent((httpSession) -> {
          try {
            httpSession.invalidate();
          } catch (IllegalStateException e) {
            LOGGER.debug(e.getMessage(), e);
          }
        });

    String reqLoggedOutUrl = req.getParameter(reqParamNameLoggedOutUrl);
    if (reqLoggedOutUrl != null) {
      resp.sendRedirect(URLEncoder.encode(reqLoggedOutUrl, StandardCharsets.UTF_8.displayName()));
    } else {
      resp.sendRedirect(loggedOutUrl);
    }
  }

  @Override
  protected void service(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {
    logout(req, resp);
  }

}
