package com.javacc.ls.utils;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.javacc.parsegen.RegularExpression;
import com.javacc.parser.Node;
import com.javacc.parser.tree.BNFProduction;
import com.javacc.parser.tree.CompilationUnit;
import com.javacc.parser.tree.Identifier;
import com.javacc.parser.tree.ParserCodeDecls;
import com.javacc.parser.tree.RegexpRef;
import com.javacc.parser.tree.RegexpSpec;
import com.javacc.parser.tree.TokenProduction;
import com.javacc.parser.tree.TypeDeclaration;


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

	public static boolean isParserCodeDeclsNode(Node node) {
		return node instanceof ParserCodeDecls;
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
		Node parent = identifier.getParent();
		if (isParserCodeDeclsNode(parent)) {
			Node root = identifier.getParent();
			CompilationUnit compilationUnit = root.firstChildOfType(CompilationUnit.class);
			if (compilationUnit == null) {
				return null;
			}
			TypeDeclaration type = compilationUnit.firstChildOfType(TypeDeclaration.class);
			if (type == null) {
				return null;
			}
			Identifier id = type.firstChildOfType(Identifier.class);
			if (id != null && isMatchIdentifier(identifier, id)) {
				return id;
			}
		} else if (parent instanceof RegexpRef) {
			// Search in token production
			Node root = identifier.getRoot();
			List<Node> children = root.children();
			for (Node node : children) {
				if (node instanceof TokenProduction) {
					TokenProduction tokenProduction = (TokenProduction) node;
					children = tokenProduction.children();
					for (Node reg : children) {
						if (reg instanceof RegexpSpec) {
							RegularExpression expression = ((RegexpSpec) reg).getRegexp();
							if (expression != null) {
								String label = expression.getLabel();
								if (identifier.getImage().equals(label)) {
									// FIXME: < IDENTIFIER: <LETTER> (<PART_LETTER>)* > #Identifier
									// IDENTIFIER cannot be extracted by JavaCC, we create a fake Identifier class,
									// but location is bad
									Identifier id = new Identifier(null, label, expression.getInputSource());
									id.setGrammar(identifier.getGrammar());
									id.setBeginLine(reg.getBeginLine());
									id.setBeginColumn(reg.getBeginColumn() + 1);
									id.setEndLine(reg.getBeginLine());
									id.setEndColumn(id.getBeginColumn() + label.length() - 1); // bad location
									return id;
								}
							}

						}
					}
				}
			}
		} else {
			// BNFProduction, TokenProduction
			Node root = identifier.getRoot();
			List<Node> children = root.children();
			for (Node node : children) {
				Identifier originIdentifier = getIndentifierNode(node);
				if (originIdentifier != null && isMatchIdentifier(identifier, originIdentifier)) {
					return originIdentifier;
				}
			}
		}
		return null;
	}

	public static Identifier getIndentifierNode(Node node) {
		if (isProductionNode(node) || isParserCodeDeclsNode(node)) {
			return node.firstChildOfType(Identifier.class);
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
