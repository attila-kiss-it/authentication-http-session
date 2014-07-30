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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.everit.osgi.authentication.context.AuthenticationContext;
import org.everit.osgi.authentication.context.AuthenticationPropagator;
import org.osgi.service.log.LogService;

public class SessionAuthenticationFilter implements Filter {

    private final AuthenticationPropagator authenticationPropagator;

    private final AuthenticationContext authenticationContext;

    private final String sessionAttrNameAuthenticatedResourceId;

    private final LogService logService;

    public SessionAuthenticationFilter(final AuthenticationPropagator authenticationPropagator,
            final AuthenticationContext authenticationContext, final String sessionAttrNameAuthenticatedResourceId,
            final LogService logService) {
        super();
        this.authenticationPropagator = authenticationPropagator;
        this.authenticationContext = authenticationContext;
        this.sessionAttrNameAuthenticatedResourceId = sessionAttrNameAuthenticatedResourceId;
        this.logService = logService;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {

        // Check the session for Resource ID and use the Default Resource ID if not assigned yet to the session
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpSession httpSession = httpServletRequest.getSession();

        Long authenticatedResourceId = (Long) httpSession.getAttribute(sessionAttrNameAuthenticatedResourceId);
        if (authenticatedResourceId == null) {
            authenticatedResourceId = authenticationContext.getDefaultResourceId();
            httpSession.setAttribute(sessionAttrNameAuthenticatedResourceId, authenticatedResourceId);
        }

        // Execute authenticated process
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

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

}
