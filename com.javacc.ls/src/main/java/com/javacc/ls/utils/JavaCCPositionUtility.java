package com.javacc.ls.utils;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import com.javacc.parser.Node;
import com.javacc.parser.tree.GrammarFile;


//import qute.ast.EACH;
//import qute.ast.ENDEACH;
//import qute.ast.ENDFOR;
//import qute.ast.ENDIF;
//import qute.ast.END_SECTION;
//import qute.ast.FOR;
//import qute.ast.IF;
//import qute.ast.START_SECTION;

public class JavaCCPositionUtility {

	public static Location toLocation(LocationLink locationLink) {
		return new Location(locationLink.getTargetUri(), locationLink.getTargetRange());
	}

	public static Range toRange(Node node) {
		Position start = new Position(node.getBeginLine() - 1, node.getBeginColumn() - 1);
		Position end = new Position(node.getEndLine() - 1, node.getEndColumn());
		return new Range(start, end);
	}

	public static Node findNodeAt(GrammarFile template, Position position) {
		Node root = template.getRoot();
		if (root == null) {
			return null;
		}
		return root.findNodeAt(position.getLine() + 1, position.getCharacter() + 1);
	}

	public static boolean isEndSection(Node node) {
		return false; // return node instanceof END_SECTION || node instanceof ENDIF|| node instanceof
						// ENDFOR|| node instanceof ENDEACH;
	}

	public static boolean isStartSection(Node node) {
		return false;// return node instanceof START_SECTION || node instanceof IF || node instanceof
						// FOR || node instanceof EACH;
	}

	public static LocationLink createLocationLink(Node origin, Node target) {
		Range originSelectionRange = toRange(origin);
		Range targetRange = toRange(target);
		Range targetSelectionRange = targetRange;
		String targetUri = target.getInputSource();
		return new LocationLink(targetUri, targetRange, targetSelectionRange, originSelectionRange);
	}

}
