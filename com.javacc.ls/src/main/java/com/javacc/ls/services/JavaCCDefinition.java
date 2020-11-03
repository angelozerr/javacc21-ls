/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.javacc.ls.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.javacc.ls.utils.JavaCCPositionUtility;
import com.javacc.ls.utils.JavaCCSearchUtils;
import com.javacc.parser.Node;
import com.javacc.parser.tree.GrammarFile;
import com.javacc.parser.tree.Identifier;

/**
 * Qute definition support.
 *
 */
class JavaCCDefinition {

	public List<? extends LocationLink> findDefinition(GrammarFile grammarFile, Position position,
			CancelChecker cancelChecker) {
		Node node = JavaCCPositionUtility.findNodeAt(grammarFile, position);
		if (node == null) {
			return Collections.emptyList();
		}
		if (!(node instanceof Identifier)) {
			return Collections.emptyList();
		}
		Identifier identifier = (Identifier) node;
		if (JavaCCSearchUtils.isProductionIdentifier(identifier)) {
			return Collections.emptyList();
		}
		Identifier originIdentifier = JavaCCSearchUtils.searchOriginIdentifier(identifier);
		if (originIdentifier != null) {
			List<LocationLink> locations = new ArrayList<>();
			LocationLink location = JavaCCPositionUtility.createLocationLink(identifier, originIdentifier);
			locations.add(location);
			return locations;
		}
		return Collections.emptyList();

	}

}
