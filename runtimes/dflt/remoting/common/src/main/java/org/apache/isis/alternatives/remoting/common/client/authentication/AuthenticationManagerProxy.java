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


package org.apache.isis.alternatives.remoting.common.client.authentication;

import org.apache.isis.alternatives.remoting.common.exchange.CloseSessionRequest;
import org.apache.isis.alternatives.remoting.common.facade.ServerFacade;
import org.apache.isis.alternatives.remoting.common.protocol.ObjectEncoderDecoder;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.runtimes.dflt.runtime.authentication.standard.AuthenticationManagerStandard;


public class AuthenticationManagerProxy extends AuthenticationManagerStandard {

    private ServerFacade serverFacade;

    public AuthenticationManagerProxy(
    		final IsisConfiguration configuration,
    		final ServerFacade serverFacade,
    		final ObjectEncoderDecoder encoderDecoder) {
    	super(configuration);
    	this.serverFacade = serverFacade;

        ProxyAuthenticator authenticator =
        	new ProxyAuthenticator(getConfiguration(), serverFacade, encoderDecoder);
		addAuthenticator(authenticator);
    }


    @Override
    public void closeSession(final AuthenticationSession authSession) {
		serverFacade.closeSession(new CloseSessionRequest(authSession));
        super.closeSession(authSession);
    }

}
