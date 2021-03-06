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

package org.apache.isis.viewer.dnd.tree;

import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.CompositeViewSpecification;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.base.Layout;
import org.apache.isis.viewer.dnd.view.composite.CompositeViewUsingBuilder;
import org.apache.isis.viewer.dnd.view.composite.ViewBuilder;

public abstract class CompositeNodeSpecification extends NodeSpecification implements CompositeViewSpecification {
    protected ViewBuilder builder;
    private NodeSpecification collectionLeafNodeSpecification;
    private NodeSpecification objectLeafNodeSpecification;

    public void setCollectionSubNodeSpecification(final NodeSpecification collectionLeafNodeSpecification) {
        this.collectionLeafNodeSpecification = collectionLeafNodeSpecification;
    }

    public void setObjectSubNodeSpecification(final NodeSpecification objectLeafNodeSpecification) {
        this.objectLeafNodeSpecification = objectLeafNodeSpecification;
    }

    public void createAxes(final Content content, final Axes axes) {
    }

    @Override
    protected View createNodeView(final Content content, final Axes axes) {
        final CompositeViewUsingBuilder view = new CompositeViewUsingBuilder(content, this, axes, createLayout(content, axes), builder);
        return view;
    }

    protected abstract Layout createLayout(Content content, Axes axes);

    /*
     * public View createView(final Content content, Axes axes, int fieldNumber)
     * { ViewRequirement requirement = new ViewRequirement(content,
     * ViewRequirement.CLOSED); if
     * (collectionLeafNodeSpecification.canDisplay(content, requirement )) {
     * return collectionLeafNodeSpecification.createView(content, axes, -1); }
     * 
     * if (objectLeafNodeSpecification.canDisplay(content, requirement)) {
     * return objectLeafNodeSpecification.createView(content, axes, -1); }
     * 
     * return null; }
     */
}
