package com.javacc.ls.services;

import java.util.Arrays;
import java.util.List;

import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import com.javacc.ls.settings.SharedSettings;
import com.javacc.ls.utils.JavaCCPositionUtility;
import com.javacc.output.java.JavaFormatter;
import com.javacc.parser.tree.GrammarFile;

public class JavaCCFormatter {

	public List<? extends TextEdit> format(GrammarFile grammarFile, Range range, SharedSettings settings) {
		JavaFormatter formatter = new JavaFormatter();
		String formattedContent = formatter.format(grammarFile);
		TextEdit textEdit = new TextEdit(JavaCCPositionUtility.toRange(grammarFile), formattedContent);
		return Arrays.asList(textEdit);
	}

}
