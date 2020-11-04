package com.javacc.ls.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import com.javacc.JavaCCError;
import com.javacc.JavaCCError.Type;
import com.javacc.JavaCCErrorReporter;
import com.javacc.ls.utils.JavaCCPositionUtility;
import com.javacc.parser.Node;

public class LSPJavaCCErrorReporter implements JavaCCErrorReporter {

	private static final String SOURCE_JAVACC = "javacc";

	private final List<JavaCCError> errors;

	public LSPJavaCCErrorReporter() {
		this.errors = new ArrayList<>();
	}

	@Override
	public void reportError(JavaCCError error) {
		errors.add(error);
	}

	public List<Diagnostic> getDiagnostics() {
		List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
		for (JavaCCError error : errors) {
			Range range = createRange(error);
			DiagnosticSeverity diagnosticSeverity = getDiagnosticSeverity(error.getType());
			String message = error.getMessage();
			String errorCode = null;
			Diagnostic diagnostic = new Diagnostic(range, message, diagnosticSeverity, SOURCE_JAVACC, errorCode);
			diagnostics.add(diagnostic);
		}
		return diagnostics;
	}

	private static Range createRange(JavaCCError error) {
		Node node = error.getNode();
		if (node != null) {
			return JavaCCPositionUtility.toRange(node);
		}
		return new Range(new Position(0, 0), new Position(0, 1));
	}

	private static DiagnosticSeverity getDiagnosticSeverity(Type type) {
		switch (type) {
		case WARNING:
			return DiagnosticSeverity.Warning;
		default:
			return DiagnosticSeverity.Error;
		}
	}
}
