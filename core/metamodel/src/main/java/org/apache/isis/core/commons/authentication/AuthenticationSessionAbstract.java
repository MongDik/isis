/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.core.commons.authentication;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.commons.lang.ToString;

public abstract class AuthenticationSessionAbstract implements AuthenticationSession, Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final List<String> roles = new ArrayList<String>();
    private final String validationCode;

    private final Map<String, Object> attributeByName = new HashMap<String, Object>();

    // ///////////////////////////////////////////////////////
    // Constructor, encode
    // ///////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    public AuthenticationSessionAbstract(final String name, final String code) {
        this(name, Collections.EMPTY_LIST, code);
    }

    public AuthenticationSessionAbstract(final String name, final List<String> roles, final String validationCode) {
        this.name = name;
        this.roles.addAll(roles);
        this.validationCode = validationCode;
        initialized();
    }

    public AuthenticationSessionAbstract(final DataInputExtended input) throws IOException {
        this.name = input.readUTF();
        this.roles.addAll(Arrays.asList(input.readUTFs()));
        this.validationCode = input.readUTF();
        initialized();
    }

    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        output.writeUTF(getUserName());
        output.writeUTFs(roles.toArray(new String[] {}));
        output.writeUTF(validationCode);
    }

    private void initialized() {
        // nothing to do
    }

    // ///////////////////////////////////////////////////////
    // User Name
    // ///////////////////////////////////////////////////////

    @Override
    public String getUserName() {
        return name;
    }

    @Override
    public boolean hasUserNameOf(final String userName) {
        return userName == null ? false : userName.equals(getUserName());
    }

    // ///////////////////////////////////////////////////////
    // Roles
    // ///////////////////////////////////////////////////////

    /**
     * Can be overridden.
     */
    @Override
    public List<String> getRoles() {
        return Collections.unmodifiableList(roles);
    }

    // ///////////////////////////////////////////////////////
    // Validation Code
    // ///////////////////////////////////////////////////////

    @Override
    public String getValidationCode() {
        return validationCode;
    }

    // ///////////////////////////////////////////////////////
    // Attributes
    // ///////////////////////////////////////////////////////

    @Override
    public Object getAttribute(final String attributeName) {
        return attributeByName.get(attributeName);
    }

    @Override
    public void setAttribute(final String attributeName, final Object attribute) {
        attributeByName.put(attributeName, attribute);
    }

    // ///////////////////////////////////////////////////////
    // toString
    // ///////////////////////////////////////////////////////

    @Override
    public String toString() {
        return new ToString(this).append("name", getUserName()).append("code", getValidationCode()).toString();
    }

}
