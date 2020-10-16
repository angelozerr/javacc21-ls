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

import java.util.List;

import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.javacc.ls.ls.commons.TextDocument;
import com.javacc.ls.parser.Template;
import com.javacc.ls.settings.QuteCompletionSettings;
import com.javacc.ls.settings.QuteFormattingSettings;
import com.javacc.ls.settings.QuteValidationSettings;

/**
 * The Qute language service.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaCCLanguageService {

	private final JavaCCCompletions completions;
	private final JavaCCHighlighting highlighting;
	private final JavaCCDefinition definition;
	private final JavaCCSymbolsProvider symbolsProvider;
	private final JavaCCDiagnostics diagnostics;

	public JavaCCLanguageService() {
		this.completions = new JavaCCCompletions();
		this.highlighting = new JavaCCHighlighting();
		this.definition = new JavaCCDefinition();
		this.symbolsProvider = new JavaCCSymbolsProvider();
		this.diagnostics = new JavaCCDiagnostics();
	}

	/**
	 * Returns completion list for the given position
	 * 
	 * @param template           the Qute template
	 * @param position           the position where completion was triggered
	 * @param completionSettings the completion settings.
	 * @param formattingSettings the formatting settings.
	 * @param cancelChecker      the cancel checker
	 * @return completion list for the given position
	 */
	public CompletionList doComplete(Template template, Position position, QuteCompletionSettings completionSettings,
			QuteFormattingSettings formattingSettings, CancelChecker cancelChecker) {
		return completions.doComplete(template, position, completionSettings, formattingSettings, cancelChecker);
	}

	public List<DocumentHighlight> findDocumentHighlights(Template template, Position position,
			CancelChecker cancelChecker) {
		return highlighting.findDocumentHighlights(template, position, cancelChecker);
	}

	public List<? extends LocationLink> findDefinition(Template template, Position position,
			CancelChecker cancelChecker) {
		return definition.findDefinition(template, position, cancelChecker);
	}

	public List<DocumentSymbol> findDocumentSymbols(Template template, CancelChecker cancelChecker) {
		return symbolsProvider.findDocumentSymbols(template, cancelChecker);
	}

	public List<SymbolInformation> findSymbolInformations(Template template, CancelChecker cancelChecker) {
		return symbolsProvider.findSymbolInformations(template, cancelChecker);
	}

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
			QuteValidationSettings validationSettings, CancelChecker cancelChecker) {
		return diagnostics.doDiagnostics(template, document, validationSettings, cancelChecker);
	}

}
