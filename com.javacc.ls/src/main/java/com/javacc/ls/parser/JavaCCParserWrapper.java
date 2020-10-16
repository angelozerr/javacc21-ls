package com.javacc.ls.parser;

import java.io.IOException;

import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.javacc.Grammar;
import com.javacc.JavaCCOptions;
import com.javacc.parser.JavaCCParser;
import com.javacc.parser.Node;
import com.javacc.parser.ParseException;

public class JavaCCParserWrapper {

	private static CancelChecker DEFAULT_CANCEL_CHECKER = () -> {
	};

	public static Template parse(String content) {
		return parse(content, null, DEFAULT_CANCEL_CHECKER);
	}

	public static Template parse(String content, String templateId, CancelChecker cancelChecker) {
		if (cancelChecker == null) {
			cancelChecker = DEFAULT_CANCEL_CHECKER;
		}
		Template template = new Template(templateId);
		
		Grammar grammar = new Grammar(new JavaCCOptions(new String[] { templateId }));
		JavaCCParser parser = new JavaCCParser(grammar, templateId, content);

		//parser.setParserTolerant(true);
		 parser.setBuildTree(true);
		try {
			parser.Root();

			// At the end of the children Root, there is a Token node
			// which encloses the all Root which causes some trouble with findNodeAt
			// we remove it
			Node root = parser.rootNode(); // .getChild(0);
//			int count = root.getChildCount();
//			if (count > 1) {
//				Node n = root.getChild(count - 1);
//				if (n instanceof Token) {
//					root.removeChild(n);
//				}
//			}
			template.setRoot(root);
			// template.setParsingProblems(parser.getParsingProblems());
		} catch (ParseException e) {
			template.setParseException(e);
			e.printStackTrace();
		} catch (IOException e) {

		}
		return template;
	}
}
