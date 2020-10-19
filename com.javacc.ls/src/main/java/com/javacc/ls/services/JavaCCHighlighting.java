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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.javacc.ls.ls.commons.BadLocationException;
import com.javacc.ls.parser.Template;
import com.javacc.ls.utils.JavaCCPositionUtility;
import com.javacc.ls.utils.JavaCCSearchUtils;
import com.javacc.parser.Node;
import com.javacc.parser.tree.Identifier;

class JavaCCHighlighting {

	private static final Logger LOGGER = Logger.getLogger(JavaCCHighlighting.class.getName());

	public List<DocumentHighlight> findDocumentHighlights(Template template, Position position,
			CancelChecker cancelChecker) {
		try {
			Node node = JavaCCPositionUtility.findNodeAt(template, position);
			if (node == null) {
				return Collections.emptyList();
			}
			if (!(node instanceof Identifier)) {
				return Collections.emptyList();
			}
			List<DocumentHighlight> highlights = new ArrayList<>();
			fillWithDefaultHighlights((Identifier) node, template.getRoot(), position, highlights, cancelChecker);
			return highlights;
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In JavaCCHighlighting the client provided Position is at a BadLocation", e);
			return Collections.emptyList();
		}
	}

	private static void fillWithDefaultHighlights(Identifier identifier, Node root, Position position,
			List<DocumentHighlight> highlights, CancelChecker cancelChecker) throws BadLocationException {
		if (JavaCCSearchUtils.isProductionIdentifier(identifier)) {
			Range originRange = JavaCCPositionUtility.toRange(identifier);
			highlights.add(new DocumentHighlight(originRange, DocumentHighlightKind.Read));
			JavaCCSearchUtils.searchReferencedIdentifiers(identifier, targetNode -> {
				Range targetRange = JavaCCPositionUtility.toRange(targetNode);
				highlights.add(new DocumentHighlight(targetRange, DocumentHighlightKind.Write));
			});
		} else {
			Range targetRange = JavaCCPositionUtility.toRange(identifier);
			highlights.add(new DocumentHighlight(targetRange, DocumentHighlightKind.Write));
			Identifier originIdentifier = JavaCCSearchUtils.searchOriginIdentifier(identifier);
			if (originIdentifier != null) {
				Range orignRange = JavaCCPositionUtility.toRange(originIdentifier);
				highlights.add(new DocumentHighlight(orignRange, DocumentHighlightKind.Read));
			}
		}

	}

}
