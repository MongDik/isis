/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.json.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.AbstractRepresentationBuilder;
import org.apache.isis.viewer.json.viewer.resources.ResourceAbstract.Caching;
import org.apache.isis.viewer.json.viewer.resources.domainobjects.DomainResourceAbstract.Intent;

@Path("/services")
public class DomainServiceResourceServerside extends DomainResourceAbstract implements
        DomainServiceResource {

    @Override
    @GET
    @Path("/")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response services() {
        init();

        ResourceContext resourceContext = getResourceContext();
        final List<ObjectAdapter> serviceAdapters = getPersistenceSession().getServices();

        DomainObjectListRepBuilder builder = 
                DomainObjectListRepBuilder.newBuilder(resourceContext)
                    .usingLinkToBuilder(new DomainServiceLinkToBuilder())
                    .withSelf("services")
                    .withAdapters(serviceAdapters);
        
        return responseOfOk(RepresentationType.LIST, builder, Caching.ONE_DAY).build();
    }

    ////////////////////////////////////////////////////////////
    // domain service
    ////////////////////////////////////////////////////////////

    @GET
    @Path("/{serviceId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Override
    public Response service(
            @PathParam("serviceId") String serviceId) {
        init();
        
        final ObjectAdapter serviceAdapter = getServiceAdapter(serviceId);
        
        ResourceContext resourceContext = getResourceContext();
        AbstractRepresentationBuilder<?> builder = 
                DomainObjectRepBuilder.newBuilder(resourceContext)
                    .usingLinkToBuilder(new DomainServiceLinkToBuilder())
                    .withAdapter(serviceAdapter);
        
        return responseOfOk(RepresentationType.DOMAIN_OBJECT, builder, Caching.ONE_DAY).build();
    }


    ////////////////////////////////////////////////////////////
    // domain service property
    ////////////////////////////////////////////////////////////

    @GET
    @Path("/{serviceId}/properties/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response propertyDetails(
            @PathParam("serviceId") final String serviceId,
            @PathParam("propertyId") final String propertyId) {
        init();

        final ObjectAdapter serviceAdapter = getServiceAdapter(serviceId);
        final OneToOneAssociation property = getPropertyThatIsVisibleAndUsable(
                serviceAdapter, propertyId, Intent.ACCESS);

        ResourceContext resourceContext = getResourceContext();
        final ObjectPropertyRepBuilder builder = ObjectPropertyRepBuilder.newBuilder(
                resourceContext, serviceAdapter, property);

        return responseOfOk(RepresentationType.OBJECT_PROPERTY, builder, Caching.ONE_DAY).build();
    }



    ////////////////////////////////////////////////////////////
    // domain service action
    ////////////////////////////////////////////////////////////
    
    @GET
    @Path("/{serviceId}/actions/{actionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response actionPrompt(
            @PathParam("serviceId") final String serviceId, 
            @PathParam("actionId") final String actionId) {
        init();

        final ObjectAdapter serviceAdapter = getServiceAdapter(serviceId);

        return actionPrompt(actionId, serviceAdapter);
    }

    
    ////////////////////////////////////////////////////////////
    // domain service action invoke
    ////////////////////////////////////////////////////////////

    @GET
    @Path("/{oid}/actions/{actionId}/invoke")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response serviceInvokeActionQueryOnly(
            @PathParam("oid") final String oidStr, 
            @PathParam("actionId") final String actionId,
            @QueryParam("args") final String arguments) {
        init();

        // TODO
        throw new UnsupportedOperationException();
    }

    @PUT
    @Path("/{oid}/actions/{actionId}/invoke")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response serviceInvokeActionIdempotent(
            @PathParam("oid") final String oidStr, 
            @PathParam("actionId") final String actionId,
            final InputStream arguments) {
        init();

        // TODO
        throw new UnsupportedOperationException();
    }

    @POST
    @Path("/{oid}/actions/{actionId}/invoke")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response serviceInvokeAction(
            @PathParam("oid") final String oidStr, 
            @PathParam("actionId") final String actionId,
            final InputStream arguments) {
        init();

        // TODO
        throw new UnsupportedOperationException();
    }

}