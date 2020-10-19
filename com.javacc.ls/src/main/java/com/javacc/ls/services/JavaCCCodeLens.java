package com.javacc.ls.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.javacc.ls.parser.Template;
import com.javacc.ls.settings.JavaCCCodeLensSettings;
import com.javacc.ls.utils.JavaCCPositionUtility;
import com.javacc.ls.utils.JavaCCSearchUtils;
import com.javacc.parser.Node;
import com.javacc.parser.tree.Identifier;

public class JavaCCCodeLens {

	public List<? extends CodeLens> getCodeLens(Template template, JavaCCCodeLensSettings codeLensSettings,
			CancelChecker cancelChecker) {
		Node root = template.getRoot();
		if (root == null) {
			return Collections.emptyList();
		}
		boolean supportedByClient = true;
		List<CodeLens> lenses = new ArrayList<>();
		Map<String, CodeLens> cache = new HashMap<>();

		List<Node> children = root.children();
		for (Node child : children) {
			if (JavaCCSearchUtils.isProductionNode(child)) {
				Identifier identifier = child.firstChildOfType(Identifier.class);
				if (identifier != null) {
					Range range = JavaCCPositionUtility.toRange(child);
					CodeLens codeLens = new CodeLens(range);
					codeLens.setCommand(
							new ReferenceCommand(identifier.getInputSource(), range.getStart(), supportedByClient));
					cache.put(identifier.getImage(), codeLens);
					lenses.add(codeLens);
				}
			}
		}
		for (Node child : children) {
			for (Node child1 : child.children()) {
				upateCodelens(child1, cache);
			}
		}
		return lenses;
	}

	private void upateCodelens(Node node, Map<String, CodeLens> cache) {
		if (node instanceof Identifier) {
			if (!JavaCCSearchUtils.isProductionIdentifier((Identifier) node)) {
				Identifier identifier = (Identifier) node;
				CodeLens codeLens = cache.get(identifier.getImage());
				if (codeLens != null) {
					((ReferenceCommand) codeLens.getCommand()).increment();
				}
			}
		} else {
			for (Node child : node.children()) {
				upateCodelens(child, cache);
			}
		}
	}

}
