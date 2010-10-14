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


package org.apache.isis.webapp.view.display;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.webapp.context.RequestContext;
import org.apache.isis.webapp.context.RequestContext.Scope;
import org.apache.isis.webapp.processor.PageWriter;
import org.apache.isis.webapp.processor.Request;
import org.apache.isis.webapp.processor.Request.RepeatMarker;


public class TableBuilder extends AbstractTableView {

    protected TableContentWriter createRowBuilder(
            final Request request,
            RequestContext context,
            final String parent,
            ObjectAssociation[] allFields) {

        final String variable = request.getOptionalProperty(ELEMENT_NAME, ELEMENT);

        final TableBlock block = new TableBlock();
        request.setBlockContent(block);
        request.pushNewBuffer();
        request.processUtilCloseTag();
        final String headers = request.popBuffer();

        return new TableContentWriter() {

            public void writeFooters(PageWriter writer) {}

            public void writeHeaders(PageWriter writer) {
                writer.appendHtml(headers);
            }

            public void writeElement(Request request, RequestContext context, ObjectAdapter element) {
                context.addVariable(variable, context.mapObject(element, Scope.REQUEST), Scope.REQUEST);
                RepeatMarker end = request.createMarker();
                RepeatMarker marker = block.getMarker();
                marker.repeat();
                request.processUtilCloseTag();
                end.repeat();
            }
        };
    }

    public String getName() {
        return "table-builder";
    }

}
