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

import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.javacc.ls.ls.commons.BadLocationException;
import com.javacc.ls.parser.Template;
import com.javacc.ls.utils.JavaCCPositionUtility;
import com.javacc.parser.Node;

class JavaCCHighlighting {

	private static final Logger LOGGER = Logger.getLogger(JavaCCHighlighting.class.getName());

	public List<DocumentHighlight> findDocumentHighlights(Template template, Position position,
			CancelChecker cancelChecker) {
		try {
			Node node = JavaCCPositionUtility.findNodeAt(template, position);
			if (node == null) {
				return Collections.emptyList();
			}
			List<DocumentHighlight> highlights = new ArrayList<>();
			fillWithDefaultHighlights(node, position, highlights, cancelChecker);
			return highlights;
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In JavaCCHighlighting the client provided Position is at a BadLocation", e);
			return Collections.emptyList();
		}
	}

	private static void fillWithDefaultHighlights(Node node, Position position, List<DocumentHighlight> highlights,
			CancelChecker cancelChecker) throws BadLocationException {
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
			Range startTagRange = JavaCCPositionUtility.toRange(originNode);
			Range endTagRange = JavaCCPositionUtility.toRange(targetNode);
			if (doesTagCoverPosition(startTagRange, endTagRange, position)) {
				fillHighlightsList(startTagRange, endTagRange, highlights);
			}
		}
	}

	private static void fillHighlightsList(Range startTagRange, Range endTagRange, List<DocumentHighlight> result) {
		if (startTagRange != null) {
			result.add(new DocumentHighlight(startTagRange, DocumentHighlightKind.Read));
		}
		if (endTagRange != null) {
			result.add(new DocumentHighlight(endTagRange, DocumentHighlightKind.Read));
		}
	}

	public static boolean doesTagCoverPosition(Range startTagRange, Range endTagRange, Position position) {
		return startTagRange != null && covers(startTagRange, position)
				|| endTagRange != null && covers(endTagRange, position);
	}

	public static boolean covers(Range range, Position position) {
		return isBeforeOrEqual(range.getStart(), position) && isBeforeOrEqual(position, range.getEnd());
	}

	public static boolean isBeforeOrEqual(Position pos1, Position pos2) {
		return pos1.getLine() < pos2.getLine()
				|| (pos1.getLine() == pos2.getLine() && pos1.getCharacter() <= pos2.getCharacter());
	}

}
