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
package org.everit.osgi.authentication.http.session;

/**
 * Provides the session attribute names stored in and read from the
 * {@link javax.servlet.http.HttpSession} by the authentication components.
 */
public interface AuthenticationSessionAttributeNames {

  /**
   * Returns the session attribute name of the Authenticated Resource ID.
   *
   * @return the session attribute name of the Authenticated Resource ID
   */
  String authenticatedResourceId();

}
