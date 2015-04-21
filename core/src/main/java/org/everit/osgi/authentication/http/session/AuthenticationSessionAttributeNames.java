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
