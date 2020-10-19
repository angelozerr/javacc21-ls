/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.javacc.ls.services;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.javacc.ls.ls.commons.TextDocument;
import com.javacc.ls.parser.Template;
import com.javacc.ls.settings.JavaCCValidationSettings;
import com.javacc.parser.ParseException;
import com.javacc.parser.Token;

/**
 * Qute diagnostics support.
 *
 */
class JavaCCDiagnostics {

	/**
	 * Validate the given Qute <code>template</code>.
	 * 
	 * @param template           the Qute template.
	 * @param document
	 * @param validationSettings the validation settings.
	 * @param cancelChecker      the cancel checker.
	 * @return the result of the validation.
	 */
	public List<Diagnostic> doDiagnostics(Template template, TextDocument document,
			JavaCCValidationSettings validationSettings, CancelChecker cancelChecker) {
		if (validationSettings == null) {
			validationSettings = JavaCCValidationSettings.DEFAULT;
		}
		List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
		ParseException parseException = template.getParseException();
		if (parseException != null) {
			try {
				Field f = ParseException.class.getDeclaredField("token");
				f.setAccessible(true);
				Token token = (Token) f.get(parseException);
				Range range = new Range(new Position(token.getBeginLine() -1, token.getBeginColumn()-1),
						new Position(token.getEndLine() -1, token.getEndColumn()-1));
				Diagnostic diagnostic = new Diagnostic(range, parseException.getMessage(), DiagnosticSeverity.Error,
						"javacc", null);
				diagnostics.add(diagnostic);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		// if (validationSettings.isEnabled()) {
		/*
		 * List<ParsingProblem> problems = template.getProblems(); if (problems != null)
		 * { problems.forEach(p -> { Range range =
		 * JavaCCPositionUtility.toRange(p.getNode()); Diagnostic diagnostic = new
		 * Diagnostic(range, p.getDescription(), DiagnosticSeverity.Error, "qute",
		 * null); diagnostics.add(diagnostic); }); }
		 */
		// }
		return diagnostics;
	}

}
