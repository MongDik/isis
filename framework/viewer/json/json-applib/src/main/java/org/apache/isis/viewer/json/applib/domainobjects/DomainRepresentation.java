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
package org.apache.isis.viewer.json.applib.domainobjects;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.JsonRepresentation.HasExtensions;
import org.apache.isis.viewer.json.applib.JsonRepresentation.HasLinks;
import org.apache.isis.viewer.json.applib.JsonRepresentation.LinksToSelf;
import org.apache.isis.viewer.json.applib.links.LinkRepresentation;
import org.apache.isis.viewer.json.applib.links.Rel;
import org.codehaus.jackson.JsonNode;

public abstract class DomainRepresentation extends JsonRepresentation implements LinksToSelf, HasLinks, HasExtensions {

    public DomainRepresentation(JsonNode jsonNode) {
        super(jsonNode);
    }

    public LinkRepresentation getSelf() {
        return getLinkWithRel(Rel.SELF);
    }

    public JsonRepresentation getLinks() {
        return getArray("links").ensureArray();
    }

    public LinkRepresentation getLinkWithRel(Rel rel) {
        return getLinkWithRel(rel.getName());
    }

    public LinkRepresentation getLinkWithRel(String rel) {
        return getLink(String.format("links[rel=%s]", rel));
    }

    public JsonRepresentation getExtensions() {
        return getMap("extensions");
    }

}