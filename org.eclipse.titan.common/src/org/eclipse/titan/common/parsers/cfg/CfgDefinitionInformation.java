/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg;

import java.util.List;

/**
 * @author eferkov
 * */
public final class CfgDefinitionInformation {
	private String value = null;
	private List<CfgLocation> locations = null;
	
	public CfgDefinitionInformation(final String value, final List<CfgLocation> locations) {
		this.value = value;
		this.locations = locations;
	}
	
	public void addLocation(final CfgLocation location) {
		locations.add(location);
	}
	
	public String getValue() {
		return value;
	}
	
	public List<CfgLocation> getLocations() {
		return locations;
	}
}