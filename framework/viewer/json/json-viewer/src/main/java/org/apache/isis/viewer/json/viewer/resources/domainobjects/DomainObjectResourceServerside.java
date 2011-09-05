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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.json.applib.HttpStatusCode;
import org.apache.isis.viewer.json.applib.domainobjects.DomainObjectResource;
import org.apache.isis.viewer.json.viewer.resources.ResourceAbstract;
import org.apache.isis.viewer.json.viewer.util.UrlDecoderUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

@Path("/objects")
public class DomainObjectResourceServerside extends ResourceAbstract implements
        DomainObjectResource {

    @GET
    @Path("/{oid}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response object(@PathParam("oid") final String oidStr) {

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final DomainObjectRepBuilder builder = DomainObjectRepBuilder
                .newBuilder(getResourceContext(), objectAdapter);
        return responseOfOk(jsonRepresentionFrom(builder));
    }

    @GET
    @Path("/{oid}/properties/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response propertyDetails(
        @PathParam("oid") final String oidStr,
        @PathParam("propertyId") final String propertyId) {

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final OneToOneAssociation property = getPropertyThatIsVisibleAndUsable(
                objectAdapter, propertyId, Intent.ACCESS);

        final PropertyRepBuilder builder = PropertyRepBuilder.newBuilder(
                getResourceContext(), objectAdapter, property);
        return responseOfOk(jsonRepresentionFrom(builder));
    }

    @GET
    @Path("/{oid}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response accessCollection(
        @PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId) {

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final OneToManyAssociation collection = getCollectionThatIsVisibleAndUsable(
                objectAdapter, collectionId, Intent.ACCESS);

        final CollectionRepBuilder builder = CollectionRepBuilder.newBuilder(
                getResourceContext(), objectAdapter, collection);
        return responseOfOk(jsonRepresentionFrom(builder));
    }

    @GET
    @Path("/{oid}/actions/{actionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response actionPrompt(
        @PathParam("oid") final String oidStr,
        @PathParam("actionId") final String actionId) {

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final ObjectAction action = getObjectActionThatIsVisibleAndUsable(
                objectAdapter, actionId, Intent.ACCESS);

        ActionRepBuilder builder = ActionRepBuilder.newBuilder(
                getResourceContext(), objectAdapter, action);
        return responseOfOk(jsonRepresentionFrom(builder));
    }

    @GET
    @Path("/{oid}/actions/{actionId}/invoke")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response invokeActionIdempotent(
        @PathParam("oid") final String oidStr,
        @PathParam("actionId") final String actionId,
        @QueryParam("arg") final List<String> arguments) {

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final ObjectAction action = getObjectActionThatIsVisibleAndUsable(
                objectAdapter, actionId, Intent.ACCESS);

        if (!isIdempotent(action)) {
            return responseOf(HttpStatusCode.METHOD_NOT_ALLOWED, 
                    "Method not allowed; action '%s' is not idempotent", action.getId());
        }
        int numParameters = action.getParameterCount();
        int numArguments = arguments.size();
        if (numArguments != numParameters) {
            return responseOf(HttpStatusCode.BAD_REQUEST, 
                    "Action '%s' has %d parameters but received %d  arguments", action.getId(), numParameters, numArguments);
        }

        List<ObjectAdapter> parameters;
        try {
            parameters = argumentAdaptersFor(action, arguments);
        } catch (IOException e) {
            return responseOf(HttpStatusCode.BAD_REQUEST, 
                    "Action '%s' has body that cannot be parsed as JSON", e, action.getId());
        }
        return invokeActionUsingAdapters(action, objectAdapter, parameters);
    }

    private boolean isIdempotent(final ObjectAction action) {
        // TODO: determine whether action is idempotent
        return true;
    }

    private List<ObjectAdapter> argumentAdaptersFor(ObjectAction action,
        List<String> arguments) throws JsonParseException, JsonMappingException, IOException {
        List<ObjectActionParameter> parameters = action.getParameters();
        List<ObjectAdapter> argumentAdapters = Lists.newArrayList();
        for (int i = 0; i < parameters.size(); i++) {
            ObjectActionParameter parameter = parameters.get(i);
            ObjectSpecification paramSpc = parameter.getSpecification();
            String argument = arguments.get(i);
            argumentAdapters.add(objectAdapterFor(paramSpc, argument));
        }

        return argumentAdapters;
    }

    /**
     * Similar to {@link #objectAdapterFor(ObjectSpecification, Object)},
     * however the object being interpreted is a String holding URL encoded JSON
     * (rather than having already been parsed into a List/Map representation).
     */
    private ObjectAdapter objectAdapterFor(ObjectSpecification spec,
        String urlEncodedJson) throws JsonParseException, JsonMappingException, IOException  {
        final String json = UrlDecoderUtils.urlDecode(urlEncodedJson);
        if (spec.containsFacet(EncodableFacet.class)) {
            EncodableFacet encodableFacet = spec.getFacet(EncodableFacet.class);
            return encodableFacet.fromEncodedString(json);
        } else {
            Map<String, Object> representation = jsonMapper.readAsMap(json);
            return objectAdapterFor(spec, representation);
        }
    }

    // /////////////////////////////////////////////////////////////////
    // put
    // /////////////////////////////////////////////////////////////////

    @PUT
    @Path("/{oid}/properties/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response modifyProperty(@PathParam("oid") final String oidStr,
        @PathParam("propertyId") final String propertyId,
        final InputStream body) {

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final OneToOneAssociation property = getPropertyThatIsVisibleAndUsable(
                objectAdapter, propertyId, Intent.MUTATE);

        ObjectSpecification propertySpec = property.getSpecification();

        ObjectAdapter argAdapter = parseBody(propertySpec, body);

        Consent consent = property.isAssociationValid(objectAdapter, argAdapter);
        if (consent.isVetoed()) {
            return responseOfUnauthorized(consent);
        }

        property.set(objectAdapter, argAdapter);

        return responseOfNoContent();
    }

    @PUT
    @Path("/{oid}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response addToSet(
        @PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId,
        final InputStream body) {

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final OneToManyAssociation collection = getCollectionThatIsVisibleAndUsable(
                objectAdapter, collectionId, Intent.MUTATE);

        if (!collection.getCollectionSemantics().isSet()) {
            return responseOf(HttpStatusCode.BAD_REQUEST, 
                    "Collection '' does not have set semantics", collectionId);
        }

        ObjectSpecification collectionSpec = collection.getSpecification();
        ObjectAdapter argAdapter = parseBody(collectionSpec, body);

        Consent consent = collection.isValidToAdd(objectAdapter, argAdapter);
        if (consent.isVetoed()) {
            return responseOfUnauthorized(consent);
        }

        collection.addElement(objectAdapter, argAdapter);
        
        return responseOfNoContent();
    }

    // /////////////////////////////////////////////////////////////////
    // delete
    // /////////////////////////////////////////////////////////////////

    @DELETE
    @Path("/{oid}/properties/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response clearProperty(
        @PathParam("oid") final String oidStr,
        @PathParam("propertyId") final String propertyId) {

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final OneToOneAssociation property = getPropertyThatIsVisibleAndUsable(
                objectAdapter, propertyId, Intent.MUTATE);

        Consent consent = property.isAssociationValid(objectAdapter, null);
        if (consent.isVetoed()) {
            return responseOfUnauthorized(consent);
        }

        property.set(objectAdapter, null);

        return responseOfNoContent();
    }

    @DELETE
    @Path("/{oid}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response removeFromCollection(
        @PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId,
        final InputStream body) {

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final OneToManyAssociation collection = getCollectionThatIsVisibleAndUsable(
                objectAdapter, collectionId, Intent.MUTATE);

        ObjectSpecification collectionSpec = collection.getSpecification();
        ObjectAdapter argAdapter = parseBody(collectionSpec, body);

        Consent consent = collection.isValidToRemove(objectAdapter, argAdapter);
        if (consent.isVetoed()) {
            return responseOfUnauthorized(consent);
        }

        collection.removeElement(objectAdapter, argAdapter);
        
        return responseOfNoContent();
    }

    // /////////////////////////////////////////////////////////////////
    // post
    // /////////////////////////////////////////////////////////////////

    @POST
    @Path("/{oid}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response addToList(
        @PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId,
        final InputStream body) {

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final OneToManyAssociation collection = getCollectionThatIsVisibleAndUsable(
                objectAdapter, collectionId, Intent.MUTATE);

        if (!collection.getCollectionSemantics().isListOrArray()) {
            return responseOf(HttpStatusCode.METHOD_NOT_ALLOWED, 
                    "Collection '%s' does not have list or array semantics", collectionId);
        }

        ObjectSpecification collectionSpec = collection.getSpecification();
        ObjectAdapter argAdapter = parseBody(collectionSpec, body);

        Consent consent = collection.isValidToAdd(objectAdapter, argAdapter);
        if (consent.isVetoed()) {
            return responseOfUnauthorized(consent);
        }

        collection.addElement(objectAdapter, argAdapter);
        
        return responseOfNoContent();
    }

    @POST
    @Path("/{oid}/actions/{actionId}/invoke")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response invokeAction(
        @PathParam("oid") final String oidStr,
        @PathParam("actionId") final String actionId,
        final InputStream body) {

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final ObjectAction action = getObjectActionThatIsVisibleAndUsable(
                objectAdapter, actionId, Intent.MUTATE);

        List<ObjectAdapter> argumentAdapters = parseBody(action, body);
        return invokeActionUsingAdapters(action, objectAdapter,
                argumentAdapters);
    }

    private List<ObjectAdapter> parseBody(final ObjectAction action,
        final InputStream body) {
        List<ObjectAdapter> argAdapters = Lists.newArrayList();
        List<?> arguments = parseBody(body);

        int numParameters = action.getParameterCount();
        int numArguments = arguments.size();
        if (numArguments != numParameters) {
            throw new WebApplicationException(
                responseOf(HttpStatusCode.BAD_REQUEST, 
                        "Action '%s' has %d parameters but received %d arguments in body", 
                        action.getId(), numParameters, numArguments));
        }

        for (int i = 0; i < numParameters; i++) {
            ObjectAdapter argAdapter = objectAdapterFor(action, arguments, i);
            argAdapters.add(argAdapter);
        }
        return argAdapters;

    }

    private ObjectAdapter objectAdapterFor(final ObjectAction action,
        List<?> arguments, int i) {
        List<ObjectActionParameter> parameters = action.getParameters();

        ObjectSpecification paramSpec = parameters.get(i).getSpecification();
        Object arg = arguments.get(i);

        ObjectAdapter objectAdapter = objectAdapterFor(action, i, paramSpec, arg);
        return objectAdapter;
    }

    private ObjectAdapter objectAdapterFor(final ObjectAction action, int i,
        ObjectSpecification paramSpec, Object arg) {
        try {
            return objectAdapterFor(paramSpec, arg);
        } catch (ExpectedStringRepresentingValueException e) {
            throw new WebApplicationException(
                    responseOf(HttpStatusCode.BAD_REQUEST, 
                        "Action '%s', argument %d should be a URL encoded string representing a value of type %s",
                        action.getId(), i, resourceFor(paramSpec)));
        } catch (ExpectedMapRepresentingReferenceException e) {
            throw new WebApplicationException(
                    responseOf(HttpStatusCode.BAD_REQUEST, 
                        "Action '%s', argument %d should be a map representing a link to reference of type %s",
                        action.getId(), i, resourceFor(paramSpec)));
        }
    }

    // ///////////////////////////////////////////////////////////////////
    // helpers
    // ///////////////////////////////////////////////////////////////////

    private Response invokeActionUsingAdapters(final ObjectAction action,
        final ObjectAdapter objectAdapter,
        final List<ObjectAdapter> argAdapters) {

        List<ObjectActionParameter> parameters = action.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            ObjectActionParameter parameter = parameters.get(i);
            ObjectAdapter paramAdapter = argAdapters.get(i);
            if (paramAdapter.getSpecification().containsFacet(ValueFacet.class)) {
                Object arg = paramAdapter.getObject();
                String reasonNotValid = parameter.isValid(objectAdapter, arg);
                if (reasonNotValid != null) {
                    return responseOf(HttpStatusCode.NOT_ACCEPTABLE, reasonNotValid);
                }
            }
        }
        ObjectAdapter[] argArray = argAdapters.toArray(new ObjectAdapter[0]);
        Consent consent = action.isProposedArgumentSetValid(objectAdapter,
                argArray);
        if (consent.isVetoed()) {
            return responseOf(HttpStatusCode.NOT_ACCEPTABLE, consent.getReason());
        }

        final ObjectAdapter returnedAdapter = action.execute(objectAdapter,
                argArray);
        if (returnedAdapter == null) {
            return responseOfNoContent();
        }
        final CollectionFacet facet = returnedAdapter.getSpecification()
                .getFacet(CollectionFacet.class);
        if (facet != null) {
            final Collection<ObjectAdapter> collectionAdapters = facet
                    .collection(returnedAdapter);
            return responseOfOk(jsonRepresentationOf(collectionAdapters));
        } else {
            return responseOfOk(jsonRepresentationOf(returnedAdapter));
        }
    }

    private ObjectAdapter parseBody(ObjectSpecification objectSpec,
        final InputStream body) {
        List<?> arguments = parseBody(body);
        if (arguments.size() != 1) {
            throw new WebApplicationException(
                    responseOf(HttpStatusCode.BAD_REQUEST, 
                        "Body should contain 1 argument representing a value of type '%s'", 
                        resourceFor(objectSpec)));
        }

        ObjectAdapter proposedValueAdapter = objectAdapterFor(objectSpec, arguments.get(0));
        return proposedValueAdapter;
    }

    private List<?> parseBody(final InputStream body) {
        try {
            byte[] byteArray = ByteStreams.toByteArray(body);
            String bodyAsString = new String(byteArray, Charsets.UTF_8);

            List<?> arguments = jsonMapper.readAsList(bodyAsString);
            return arguments;
        } catch (JsonParseException e) {
            throw new WebApplicationException(e,
                    responseOf(HttpStatusCode.BAD_REQUEST, "could not parse body", e));
        } catch (JsonMappingException e) {
            throw new WebApplicationException(e,
                    responseOf(HttpStatusCode.BAD_REQUEST, "could not map body to a Map structure", e));
        } catch (IOException e) {
            throw new WebApplicationException(e,
                    responseOf(HttpStatusCode.BAD_REQUEST, "could not read body", e));
        }
    }

    private static String resourceFor(ObjectSpecification objectSpec) {
        // TODO: should return a string in the form
        // http://localhost:8080/types/xxx
        return objectSpec.getFullIdentifier();
    }

    private enum Intent {
        ACCESS, MUTATE;

        public boolean isMutate() {
            return this == MUTATE;
        }
    }

    private OneToOneAssociation getPropertyThatIsVisibleAndUsable(
        final ObjectAdapter objectAdapter, final String propertyId,
        final Intent intent) {
        ObjectAssociation association = objectAdapter.getSpecification()
                .getAssociation(propertyId);
        if (association == null || !association.isOneToOneAssociation()) {
            throwNotFoundException(propertyId, MemberType.PROPERTY);
        }
        OneToOneAssociation property = (OneToOneAssociation) association;
        return ensureVisibleAndUsableForIntent(objectAdapter, property,
                MemberType.PROPERTY, intent);
    }

    private OneToManyAssociation getCollectionThatIsVisibleAndUsable(
        final ObjectAdapter objectAdapter, 
        final String collectionId,
        final Intent intent) {
        
        ObjectAssociation association = objectAdapter.getSpecification()
                .getAssociation(collectionId);
        if (association == null || !association.isOneToManyAssociation()) {
            throwNotFoundException(collectionId, MemberType.COLLECTION);
        }
        OneToManyAssociation collection = (OneToManyAssociation) association;
        return ensureVisibleAndUsableForIntent(objectAdapter, collection,
                MemberType.COLLECTION, intent);
    }

    private ObjectAction getObjectActionThatIsVisibleAndUsable(
        final ObjectAdapter objectAdapter,
        final String actionId,
        Intent intent) {
        
        ObjectAction action = objectAdapter.getSpecification().getObjectAction(actionId);
        return ensureVisibleAndUsableForIntent(objectAdapter, action, MemberType.ACTION, intent);
    }

    public <T extends ObjectMember> T ensureVisibleAndUsableForIntent(
        final ObjectAdapter objectAdapter, T objectMember,
        MemberType memberType, Intent intent) {
        String memberId = objectMember.getId();
        if (objectMember.isVisible(getAuthenticationSession(), objectAdapter).isVetoed()) {
            throwNotFoundException(memberId, memberType);
        }
        if (intent.isMutate()) {
            Consent usable = objectMember.isUsable(getAuthenticationSession(), objectAdapter);
            if (usable.isVetoed()) {
                String memberTypeStr = memberType.name().toLowerCase();
                throw new WebApplicationException(
                        responseOf(HttpStatusCode.NOT_ACCEPTABLE, 
                        "%s is not usable: '%s' (%s)", 
                        memberTypeStr, memberId, usable.getReason()));
            }
        }
        return objectMember;
    }

    private static void throwNotFoundException(
            final String memberId, MemberType memberType) {
        String memberTypeStr = memberType.name().toLowerCase();
        throw new WebApplicationException(
                responseOf(HttpStatusCode.NOT_FOUND, 
                        "%s '%s' either does not exist or is not visible", 
                        memberTypeStr, memberId));
    }

}