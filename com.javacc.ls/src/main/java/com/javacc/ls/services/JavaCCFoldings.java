package com.javacc.ls.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.lsp4j.FoldingRange;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.javacc.ls.parser.Template;
import com.javacc.ls.settings.JavaCCFoldingSettings;
import com.javacc.parser.Node;
import com.javacc.parser.tree.BNFProduction;

class JavaCCFoldings {

	public List<FoldingRange> getFoldingRanges(Template template, JavaCCFoldingSettings context,
			CancelChecker cancelChecker) {
		Node root = template.getRoot();
		if (root == null) {
			return Collections.emptyList();
		}
		List<FoldingRange> result = new ArrayList<>();
		List<Node> children = root.children();
		for (Node child : children) {
			if (child instanceof BNFProduction) {
				//child.
			}
		}
		return result;
	}

}
