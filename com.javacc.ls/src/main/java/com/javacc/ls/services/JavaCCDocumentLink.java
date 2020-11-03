package com.javacc.ls.services;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.Range;

import com.javacc.ls.utils.JavaCCPositionUtility;
import com.javacc.parser.Node;
import com.javacc.parser.tree.GrammarFile;
import com.javacc.parser.tree.GrammarInclusion;
import com.javacc.parser.tree.StringLiteral;

class JavaCCDocumentLink {

	public List<DocumentLink> findDocumentLinks(GrammarFile grammarFile) {
		Node root = grammarFile.getRoot();
		if (root == null) {
			return Collections.emptyList();
		}
		List<GrammarInclusion> inclusions = root.childrenOfType(GrammarInclusion.class);
		if (inclusions != null) {
			List<DocumentLink> documentLinks = new ArrayList<>();
			for (GrammarInclusion inclusion : inclusions) {
				// Test if it's INCLUDE("JSON.javacc")
				StringLiteral include = getIncludeStringLiteral(inclusion);
				if (include != null) {
					// StringLiteral = "JSON.javacc"
					File includedFile = getIncludeFile(include);
					String target = toUri(includedFile).toString();
					Range range = JavaCCPositionUtility.toRange(include);
					DocumentLink link = new DocumentLink(range, target);
					documentLinks.add(link);
				}
			}
			return documentLinks;
		}
		return Collections.emptyList();
	}

	public static StringLiteral getIncludeStringLiteral(GrammarInclusion inclusion) {
		if (inclusion.getChildCount() == 4) {
			Node child = inclusion.getChild(2);
			if (child instanceof StringLiteral) {
				return (StringLiteral) child;
			}
			return null;
		}
		return null;
	}

	public static File getIncludeFile(StringLiteral include) {
		String location = include.getImage();
		location = location.substring(1, location.length() - 1);
		File file = new File(location);
		if (!file.exists()) {
			if (!file.isAbsolute()) {
				file = new File(new File(include.getGrammar().getFilename()).getParent(), location);
			}
		}
		return file;
	}

	public static URI toUri(File file) {
		// URI scheme specified by language server protocol and LSP
		try {
			return new URI("file", "", file.getAbsoluteFile().toURI().getPath(), null); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (URISyntaxException e) {
			return file.getAbsoluteFile().toURI();
		}
	}
}
