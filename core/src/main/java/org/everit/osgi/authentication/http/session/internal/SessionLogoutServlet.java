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
import java.util.Objects;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.osgi.service.log.LogService;

/**
 * Invalidates the {@link HttpSession} of the request and redirects to a specified location.
 */
public class SessionLogoutServlet extends HttpServlet {

    private static final long serialVersionUID = -3935910461013610805L;

    private final String loggedOutUrl;

    private final String reqParamNameLoggedOutUrl;

    private final LogService logService;

    public SessionLogoutServlet(final String reqParamNameLoggedOutUrl, final String loggedOutUrl,
            final LogService logService) {
        super();
        Objects.requireNonNull(reqParamNameLoggedOutUrl, "reqParamNameLoggedOutUrl cannot be null");
        Objects.requireNonNull(loggedOutUrl, "loggedOutUrl cannot be null");

        this.reqParamNameLoggedOutUrl = reqParamNameLoggedOutUrl.trim();
        if (this.reqParamNameLoggedOutUrl.isEmpty()) {
            throw new IllegalArgumentException("reqParamNameLoggedOutUrl cannot be blank");
        }

        this.loggedOutUrl = loggedOutUrl.trim();
        if (this.loggedOutUrl.isEmpty()) {
            throw new IllegalArgumentException("loggedOutUrl cannot be blank");
        }

        this.logService = logService;
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
            IOException {
        logout(req, resp);
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        logout(req, resp);
    }

    private void invalidateSession(final HttpServletRequest req) {
        Optional.ofNullable(req.getSession(false))
                .ifPresent((httpSession) -> {
                    try {
                        httpSession.invalidate();
                    } catch (IllegalStateException e) {
                        logService.log(LogService.LOG_DEBUG, e.getMessage(), e);
                    }
                });
    }

    private void logout(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

        invalidateSession(req);

        String reqLoggedOutUrl = req.getParameter(reqParamNameLoggedOutUrl);
        if (reqLoggedOutUrl != null) {
            resp.sendRedirect(reqLoggedOutUrl);
        } else {
            resp.sendRedirect(loggedOutUrl);
        }
    }

}
