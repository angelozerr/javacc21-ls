package com.javacc.ls.parser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.javacc.Grammar;
import com.javacc.parser.JavaCCParser;
import com.javacc.parser.ParseException;
import com.javacc.parser.tree.GrammarFile;

public class JavaCCGrammarParserUtils {

	private static CancelChecker DEFAULT_CANCEL_CHECKER = () -> {
	};

	public static GrammarFile parse(String content) {
		return parse(content, null, DEFAULT_CANCEL_CHECKER);
	}

	public static GrammarFile parse(String content, String fileName, CancelChecker cancelChecker) {
		if (cancelChecker == null) {
			cancelChecker = DEFAULT_CANCEL_CHECKER;
		}
		Grammar grammar = new Grammar();
		try {
			grammar.setFilename(URLDecoder.decode(fileName.replace("file:///", "").replace("file://", ""), "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		grammar.setReporter(new LSPJavaCCErrorReporter());

		JavaCCParser parser = new JavaCCParser(grammar, fileName, content);
		parser.setEnterIncludes(false);

		// parser.setParserTolerant(true);
		parser.setBuildTree(true);

		try {
			return parser.Root();
		} catch (ParseException e) {
			grammar.addParseError(e.getToken(), e.getMessage());

		} catch (IOException e) {
			// Should never occurs
		}
		GrammarFile grammarFile = new GrammarFile();
		grammarFile.setGrammar(grammar);
		return grammarFile;
	}
}
