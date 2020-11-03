package com.javacc.ls.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.javacc.ls.utils.JavaCCPositionUtility;
import com.javacc.ls.utils.JavaCCSearchUtils;
import com.javacc.parser.Node;
import com.javacc.parser.tree.GrammarFile;
import com.javacc.parser.tree.Identifier;

public class JavaCCReference {

	public List<? extends Location> findReferences(GrammarFile grammarFile, Position position, ReferenceContext context,
			CancelChecker cancelChecker) {
		Node node = JavaCCPositionUtility.findNodeAt(grammarFile, position);
		if (node == null) {
			return Collections.emptyList();
		}
		if (!(node instanceof Identifier)) {
			return Collections.emptyList();
		}
		Identifier identifier = (Identifier) node;
		List<Location> locations = new ArrayList<>();
		JavaCCSearchUtils.searchReferencedIdentifiers(identifier, targetNode -> {
			Range targetRange = JavaCCPositionUtility.toRange(targetNode);
			locations.add(new Location(targetNode.getInputSource(), targetRange));
		});
		return locations;
	}

}
