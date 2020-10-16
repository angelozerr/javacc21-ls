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

import static com.javacc.ls.utils.JavaCCPositionUtility.isEndSection;
import static com.javacc.ls.utils.JavaCCPositionUtility.isStartSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.javacc.ls.ls.commons.BadLocationException;
import com.javacc.ls.parser.Template;
import com.javacc.ls.utils.JavaCCPositionUtility;
import com.javacc.parser.Node;

/**
 * Qute definition support.
 *
 */
class JavaCCDefinition {

	private static final Logger LOGGER = Logger.getLogger(JavaCCDefinition.class.getName());

	public List<? extends LocationLink> findDefinition(Template template, Position position,
			CancelChecker cancelChecker) {
		try {
			Node node = JavaCCPositionUtility.findNodeAt(template, position);
			if (node == null) {
				return Collections.emptyList();
			}
			List<LocationLink> locations = new ArrayList<>();
			// Start end tag definition
			findStartEndTagDefinition(node, template, locations);
			return locations;
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuteDefinition the client provided Position is at a BadLocation", e);
			return Collections.emptyList();
		}

	}

	/**
	 * Find start end tag definition.
	 * 
	 * @param document
	 * @param template
	 * 
	 * @param request   the definition request
	 * @param locations the locations
	 * @throws BadLocationException
	 */
	private static void findStartEndTagDefinition(Node node, Template template, List<LocationLink> locations)
			throws BadLocationException {
		Node originNode = null;
		Node targetNode = null;
		if (isStartSection(node)) {
			originNode = node;
			Node section = originNode.getParent();
			targetNode = section.getChild(section.getChildCount() - 1);
		} else if (isEndSection(node)) {
			originNode = node;
			Node section = originNode.getParent();
			targetNode = section.getChild(0);
		}
		if (originNode != null && targetNode != null) {
			Range originRange = JavaCCPositionUtility.toRange(originNode);
			Range targetRange = JavaCCPositionUtility.toRange(targetNode);
			locations.add(new LocationLink(template.getId(), targetRange, targetRange, originRange));
		}
	}

}
