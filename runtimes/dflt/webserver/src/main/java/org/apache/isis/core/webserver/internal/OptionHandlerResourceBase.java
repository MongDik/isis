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


package org.apache.isis.core.webserver.internal;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import org.apache.isis.core.commons.config.IsisConfigurationBuilder;
import org.apache.isis.runtimes.dflt.runtime.runner.BootPrinter;
import org.apache.isis.runtimes.dflt.runtime.runner.options.OptionHandler;
import org.apache.isis.core.webserver.WebServerConstants;

public final class OptionHandlerResourceBase implements OptionHandler {
	private String resourceBase;
	static final String RESOURCE_BASE_LONG_OPT = "webapp";
	static final String RESOURCE_BASE_OPT = "w";

	@SuppressWarnings("static-access")
	public void addOption(Options options) {
	    Option option = OptionBuilder.withArgName("webapp directory").hasArg().withLongOpt(OptionHandlerResourceBase.RESOURCE_BASE_LONG_OPT).withDescription(
	    "directory holding webapp").create(OptionHandlerResourceBase.RESOURCE_BASE_OPT);
	    options.addOption(option);
	}

	public boolean handle(CommandLine commandLine,
			BootPrinter bootPrinter, Options options) {
	    resourceBase = commandLine.getOptionValue(OptionHandlerResourceBase.RESOURCE_BASE_OPT, resourceBase);
		return true;
	}

	public void primeConfigurationBuilder(
			IsisConfigurationBuilder isisConfigurationBuilder) {
		if (resourceBase == null) {
			return;
		}
		isisConfigurationBuilder.add(WebServerConstants.EMBEDDED_WEB_SERVER_RESOURCE_BASE_KEY, resourceBase);
	}
}