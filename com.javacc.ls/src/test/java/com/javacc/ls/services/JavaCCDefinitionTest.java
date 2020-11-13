package com.javacc.ls.services;

import static com.javacc.ls.JavaCCAssert.ll;
import static com.javacc.ls.JavaCCAssert.r;
import static com.javacc.ls.JavaCCAssert.testDefinitionFor;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.javacc.ls.ls.commons.BadLocationException;
import com.javacc.parser.ParseException;

public class JavaCCDefinitionTest {

	@Test
	public void token() throws BadLocationException, IOException, ParseException {
		String content = "TOKEN: \r\n" + //
				"	<A: \"\">\r\n" + //
				"	|\r\n" + //
				"	<B: <@A>>;";
		testDefinitionFor(content, "test.javacc", //
				ll("test.javacc", r(3, 6, 3, 7), r(1, 1, 1, 3)));
	}
}
