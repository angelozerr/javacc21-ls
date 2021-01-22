package com.javacc.ls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;

import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import com.javacc.Grammar;
import com.javacc.ls.ls.commons.BadLocationException;
import com.javacc.ls.ls.commons.TextDocument;
import com.javacc.ls.services.JavaCCLanguageService;
import com.javacc.parser.JavaCCParser;
import com.javacc.parser.ParseException;
import com.javacc.parser.tree.GrammarFile;

public class JavaCCAssert {

	// ------------------- Definition assert

	public static void testDefinitionFor(String content, LocationLink... expected)
			throws BadLocationException, IOException, ParseException {
		testDefinitionFor(content, null, expected);
	}

	public static void testDefinitionFor(String value, String fileURI, LocationLink... expected)
			throws BadLocationException, IOException, ParseException {
		int offset = value.indexOf('@');
		value = value.substring(0, offset) + value.substring(offset + 1);

		TextDocument document = new TextDocument(value, fileURI != null ? fileURI : "test://test/test.javacc");
		Position position = document.positionAt(offset);

		JavaCCLanguageService javaccLanguageService = new JavaCCLanguageService();

		GrammarFile javaccDocument = parseGrammar(document.getUri(), document.getText());
		List<? extends LocationLink> actual = javaccLanguageService.findDefinition(javaccDocument, position, () -> {
		});
		assertLocationLink(actual, expected);

	}

	public static LocationLink ll(final String uri, final Range originRange, Range targetRange) {
		return new LocationLink(uri, targetRange, targetRange, originRange);
	}

	public static void assertLocationLink(List<? extends LocationLink> actual, LocationLink... expected) {
		assertEquals(expected.length, actual.size());
		for (int i = 0; i < expected.length; i++) {
			actual.get(i).setTargetUri(actual.get(i).getTargetUri().replaceAll("file:///", "file:/"));
			expected[i].setTargetUri(expected[i].getTargetUri().replaceAll("file:///", "file:/"));
		}
		assertArrayEquals(expected, actual.toArray());
	}

	public static GrammarFile parseGrammar(String fileName, String content) throws IOException, ParseException {
		Grammar grammar = new Grammar();
		grammar.setFilename(fileName);
		JavaCCParser parser = new JavaCCParser(grammar, fileName, content);
		return parser.Root();
	}

	public static Range r(int startLine, int startCharacter, int endLine, int endCharacter) {
		return new Range(new Position(startLine, startCharacter), new Position(endLine, endCharacter));
	}
}
