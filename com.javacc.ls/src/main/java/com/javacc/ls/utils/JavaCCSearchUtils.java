package com.javacc.ls.utils;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.javacc.parser.Node;
import com.javacc.parser.tree.BNFProduction;
import com.javacc.parser.tree.CompilationUnit;
import com.javacc.parser.tree.Identifier;
import com.javacc.parser.tree.TokenProduction;

public class JavaCCSearchUtils {

	public static boolean isProductionIdentifier(Identifier identifier) {
		Node parent = identifier.getParent();
		if (parent == null) {
			return false;
		}
		return isProductionNode(parent);
	}

	public static boolean isProductionNode(Node node) {
		return node instanceof BNFProduction || node instanceof TokenProduction;
	}

	public static void searchReferencedIdentifiers(Identifier identifier, Consumer<Identifier> collector) {
		Node root = identifier.getRoot();
		searchReferencedIdentifiers(identifier, root, collector);
	}

	public static void searchReferencedIdentifiers(Identifier identifier, Node root, Consumer<Identifier> collector) {
		List<Node> children = root.children();
		for (Node node : children) {
			if (node instanceof Identifier && isMatchIdentifier((Identifier) node, identifier)) {
				collector.accept((Identifier) node);
			} else if (!(node instanceof CompilationUnit)) {
				searchReferencedIdentifiers(identifier, node, collector);
			}
		}
	}

	public static Identifier searchOriginIdentifier(Identifier identifier) {
		Node root = identifier.getRoot();
		List<Node> children = root.children();
		for (Node node : children) {
			if (node instanceof BNFProduction || node instanceof TokenProduction) {
				Identifier originIdentifier = node.firstChildOfType(Identifier.class);
				if (isMatchIdentifier(identifier, originIdentifier)) {
					return originIdentifier;
				}
			}
		}
		return null;
	}

	private static boolean isMatchIdentifier(Identifier identifier, Identifier originIdentifier) {
		if (identifier == originIdentifier) {
			return false;
		}
		if (identifier == null || originIdentifier == null) {
			return false;
		}
		return Objects.equals(identifier.getImage(), originIdentifier.getImage());
	}
}
