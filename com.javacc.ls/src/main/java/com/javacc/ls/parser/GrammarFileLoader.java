package com.javacc.ls.parser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;

import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.javacc.Grammar;
import com.javacc.JavaCCOptions;
import com.javacc.parser.JavaCCParser;
import com.javacc.parser.ParseException;
import com.javacc.parser.Token;
import com.javacc.parser.tree.GrammarFile;

public class GrammarFileLoader extends GrammarFile {

	private static CancelChecker DEFAULT_CANCEL_CHECKER = () -> {
	};

	public static GrammarFile parse(String content) {
		return parse(content, null, DEFAULT_CANCEL_CHECKER);
	}

	public static GrammarFile parse(String content, String fileName, CancelChecker cancelChecker) {
		if (cancelChecker == null) {
			cancelChecker = DEFAULT_CANCEL_CHECKER;
		}
		Grammar grammar = new Grammar(new JavaCCOptions(new String[] { fileName }));
		try {
			grammar.setFilename(URLDecoder.decode(fileName.replace("file:///", "").replace("file://", ""), "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		JavaCCParser parser = new JavaCCParser(grammar, fileName, content);
		parser.setEnterIncludes(false);

		// parser.setParserTolerant(true);
		parser.setBuildTree(true);

		try {
			return parser.Root();
		} catch (ParseException e) {
			try {
				Field f = ParseException.class.getDeclaredField("token");
				f.setAccessible(true);
				Token token = (Token) f.get(e);
				grammar.addParseError(token, e.getMessage());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		GrammarFile grammarFile = new GrammarFile();
		grammarFile.setGrammar(grammar);
		return grammarFile;
	}
}
