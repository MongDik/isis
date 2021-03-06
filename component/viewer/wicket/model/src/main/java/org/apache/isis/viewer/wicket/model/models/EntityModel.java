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

package org.apache.isis.viewer.wicket.model.models;

import java.io.Serializable;
import java.util.Map;

import com.google.common.collect.Maps;

import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;
import org.apache.isis.viewer.wicket.model.mementos.PropertyMemento;

/**
 * Backing model to represent a {@link ObjectAdapter}.
 * 
 * <p>
 * So that the model is {@link Serializable}, the {@link ObjectAdapter} is
 * stored as a {@link ObjectAdapterMemento}.
 */
public class EntityModel extends BookmarkableModel<ObjectAdapter> {

    private static final long serialVersionUID = 1L;
    

    // //////////////////////////////////////////////////////////
    // factory methods for PageParameters
    // //////////////////////////////////////////////////////////

    /**
     * Factory method for creating {@link PageParameters} to represent an
     * entity.
     */
    public static PageParameters createPageParameters(final ObjectAdapter adapter) {

        final PageParameters pageParameters = new PageParameters();

        final Boolean persistent = adapter.representsPersistent();

        if (persistent) {
            final String oidStr = adapter.getOid().enStringNoVersion(getOidMarshaller());

            PageParameterNames.OBJECT_OID.addStringTo(pageParameters, oidStr);
        } else {
            // don't do anything; instead the page should be redirected back to
            // an EntityPage so that the underlying EntityModel that contains
            // the
            // memento for the transient ObjectAdapter can be accessed.
        }
        
        PageParameterNames.PAGE_TYPE.addEnumTo(pageParameters, PageType.ENTITY);
        PageParameterNames.PAGE_TITLE.addStringTo(pageParameters, adapter.titleString());

        return pageParameters;
    }



    public enum RenderingHint {
        REGULAR,
        COMPACT,
        /**
         * icon only
         */
        ULTRA_COMPACT;

        public boolean isCompactOrUltraCompact() {
            return isCompact() || isUltraCompact();
        }

        public boolean isRegular() {
            return this == REGULAR;
        }

        public boolean isCompact() {
            return this == COMPACT;
        }

        public boolean isUltraCompact() {
            return this == ULTRA_COMPACT;
        }
    }

	public enum Mode {
        VIEW, EDIT;
    }

    private ObjectAdapterMemento adapterMemento;
    private Mode mode = Mode.VIEW;
    private RenderingHint renderingHint = RenderingHint.REGULAR;
    private final Map<PropertyMemento, ScalarModel> propertyScalarModels = Maps.newHashMap();


    /**
     * Toggled by 'entityDetailsButton'.
     */
    private boolean entityDetailsVisible;
    
    /**
     * {@link ConcurrencyException}, if any, that might have occurred previously
     */
    private ConcurrencyException concurrencyException;

    // //////////////////////////////////////////////////////////
    // constructors
    // //////////////////////////////////////////////////////////

    public EntityModel() {
        pendingModel = new PendingModel(this);
    }

    public EntityModel(final PageParameters pageParameters) {
        this(ObjectAdapterMemento.createPersistent(rootOidFrom(pageParameters)));
    }

    public EntityModel(final ObjectAdapter adapter) {
        this(ObjectAdapterMemento.createOrNull(adapter));
        setObject(adapter);
    }

    public EntityModel(final ObjectAdapterMemento adapterMemento) {
        this.adapterMemento = adapterMemento;
        pendingModel = new PendingModel(this);
    }

    private static String oidStr(final PageParameters pageParameters) {
        return PageParameterNames.OBJECT_OID.getStringFrom(pageParameters);
    }

    private static RootOid rootOidFrom(final PageParameters pageParameters) {
        return getOidMarshaller().unmarshal(oidStr(pageParameters), RootOid.class);
    }
    


    // //////////////////////////////////////////////////////////
    // asPageParameters
    // //////////////////////////////////////////////////////////

    @Override
    public PageParameters asPageParameters() {
        return createPageParameters(getObject());
    }

    // //////////////////////////////////////////////////////////
    // ObjectAdapterMemento, typeOfSpecification
    // //////////////////////////////////////////////////////////

    public ObjectAdapterMemento getObjectAdapterMemento() {
        return adapterMemento;
    }

    /**
     * Overridable for submodels (eg {@link ScalarModel}) that know the type of
     * the adapter without there being one.
     */
    public ObjectSpecification getTypeOfSpecification() {
        if (adapterMemento == null) {
            return null;
        }
        return getSpecificationFor(adapterMemento.getObjectSpecId());
    }

    private ObjectSpecification getSpecificationFor(ObjectSpecId objectSpecId) {
        return getSpecificationLoader().lookupBySpecId(objectSpecId);
    }

    // //////////////////////////////////////////////////////////
    // load, setObject
    // //////////////////////////////////////////////////////////

    /**
     * Not Wicket API, but used by <tt>EntityPage</tt> to do eager loading
     * when rendering after post-and-redirect.
     * @return 
     */
    public ObjectAdapter load(ConcurrencyChecking concurrencyChecking) {
        if (adapterMemento == null) {
            return null;
        }
        
        final ObjectAdapter objectAdapter = adapterMemento.getObjectAdapter(concurrencyChecking);
        if(concurrencyChecking == ConcurrencyChecking.NO_CHECK) {
            this.resetPropertyModels();
        }
        return objectAdapter;
    }

    @Override
    public ObjectAdapter load() {
        return load(ConcurrencyChecking.CHECK);
    }

    @Override
    public void setObject(final ObjectAdapter adapter) {
        super.setObject(adapter);
        adapterMemento = ObjectAdapterMemento.createOrNull(adapter);
    }

    @Override
    public void detach() {
        if (isAttached()) {
            if (adapterMemento != null) {
                adapterMemento.captureTitleHintIfPossible();
            }
        }
        super.detach();
    }


    // //////////////////////////////////////////////////////////
    // PropertyModels
    // //////////////////////////////////////////////////////////

    /**
     * Lazily populates with the current value of each property.
     */
    public ScalarModel getPropertyModel(final PropertyMemento pm) {
        ScalarModel scalarModel = propertyScalarModels.get(pm);
        if (scalarModel == null) {
            scalarModel = new ScalarModel(getObjectAdapterMemento(), pm);
            if (isViewMode()) {
                scalarModel.toViewMode();
            } else {
                scalarModel.toEditMode();
            }
            propertyScalarModels.put(pm, scalarModel);
        }
        return scalarModel;

    }

    /**
     * Resets the {@link #propertyScalarModels hash} of {@link ScalarModel}s for
     * each {@link PropertyMemento property} to the value held in the underlying
     * {@link #getObject() entity}.
     */
    public void resetPropertyModels() {
        adapterMemento.resetVersion();
        for (final PropertyMemento pm : propertyScalarModels.keySet()) {
            final ScalarModel scalarModel = propertyScalarModels.get(pm);
            final ObjectAdapter associatedAdapter = pm.getProperty().get(getObject());
            scalarModel.setObject(associatedAdapter);
        }
    }

    // //////////////////////////////////////////////////////////
    // RenderingHint, Mode, entityDetailsVisible
    // //////////////////////////////////////////////////////////


    public RenderingHint getRenderingHint() {
        return renderingHint;
    }
    public void setRenderingHint(RenderingHint renderingHint) {
        this.renderingHint = renderingHint;
    }

    public Mode getMode() {
        return mode;
    }

    protected void setMode(final Mode mode) {
        this.mode = mode;
    }

    public boolean isViewMode() {
        return mode == Mode.VIEW;
    }

    public boolean isEditMode() {
        return mode == Mode.EDIT;
    }

    public EntityModel toEditMode() {
        setMode(Mode.EDIT);
        for (final ScalarModel scalarModel : propertyScalarModels.values()) {
            scalarModel.toEditMode();
        }
        return this;
    }

    public EntityModel toViewMode() {
        setMode(Mode.VIEW);
        for (final ScalarModel scalarModel : propertyScalarModels.values()) {
            scalarModel.toViewMode();
        }
        return this;
    }

    public boolean isEntityDetailsVisible() {
        return entityDetailsVisible;
    }

    public void toggleDetails() {
        entityDetailsVisible = !entityDetailsVisible;
    }

    
    // //////////////////////////////////////////////////////////
    // concurrency exceptions
    // //////////////////////////////////////////////////////////

    public void setException(ConcurrencyException ex) {
        this.concurrencyException = ex;
    }

    public String getAndClearConcurrencyExceptionIfAny() {
        if(concurrencyException == null) {
            return null;
        }
        final String message = concurrencyException.getMessage();
        concurrencyException = null;
        return message;
    }

    // //////////////////////////////////////////////////////////
    // validation
    // //////////////////////////////////////////////////////////

    public String getReasonInvalidIfAny() {
        final ObjectAdapter adapter = getObjectAdapterMemento().getObjectAdapter(ConcurrencyChecking.CHECK);
        final Consent validity = adapter.getSpecification().isValid(adapter);
        return validity.isAllowed() ? null : validity.getReason();
    }

    public void apply() {
        final ObjectAdapter adapter = getObjectAdapterMemento().getObjectAdapter(ConcurrencyChecking.CHECK);
        for (final ScalarModel scalarModel : propertyScalarModels.values()) {
            final OneToOneAssociation property = scalarModel.getPropertyMemento().getProperty();
            final ObjectAdapter associate = scalarModel.getObject();
            property.set(adapter, associate);
        }
        getObjectAdapterMemento().setAdapter(adapter);
        toViewMode();
    }


    // //////////////////////////////////////////////////////////
    // Pending
    // //////////////////////////////////////////////////////////
    
    private static final class PendingModel extends Model<ObjectAdapterMemento> {
        private static final long serialVersionUID = 1L;

        private final EntityModel entityModel;

        /**
         * Whether pending has been set (could have been set to null)
         */
        private boolean hasPending;
        /**
         * The new value (could be set to null; hasPending is used to distinguish).
         */
        private ObjectAdapterMemento pending;
        

        public PendingModel(EntityModel entityModel) {
            this.entityModel = entityModel;
        }

        @Override
        public ObjectAdapterMemento getObject() {
            if (hasPending) {
                return pending;
            }
            final ObjectAdapter adapter = entityModel.getObject();
            return ObjectAdapterMemento.createOrNull(adapter);
        }

        @Override
        public void setObject(final ObjectAdapterMemento adapterMemento) {
            pending = adapterMemento;
            hasPending = true;
        }

        public void clearPending() {
            this.hasPending = false;
            this.pending = null;
        }

        private ObjectAdapter getPendingAdapter() {
            final ObjectAdapterMemento memento = getObject();
            return memento != null ? memento.getObjectAdapter(ConcurrencyChecking.NO_CHECK) : null;
        }

        public ObjectAdapter getPendingElseCurrentAdapter() {
            return hasPending ? getPendingAdapter() : entityModel.getObject();
        }

        public void setPending(ObjectAdapterMemento selectedAdapterMemento) {
            this.pending = selectedAdapterMemento;
            hasPending=true;
        }
    }
    
    private final PendingModel pendingModel;

    public ObjectAdapter getPendingElseCurrentAdapter() {
        return pendingModel.getPendingElseCurrentAdapter();
    }

    public ObjectAdapter getPendingAdapter() {
        return pendingModel.getPendingAdapter();
    }

    public void setPending(ObjectAdapterMemento selectedAdapterMemento) {
        pendingModel.setPending(selectedAdapterMemento);
    }

    public void clearPending() {
        pendingModel.clearPending();
    }


    // //////////////////////////////////////////////////////////
    // Dependencies (from context)
    // //////////////////////////////////////////////////////////

    protected static OidMarshaller getOidMarshaller() {
		return IsisContext.getOidMarshaller();
	}

    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }




}
