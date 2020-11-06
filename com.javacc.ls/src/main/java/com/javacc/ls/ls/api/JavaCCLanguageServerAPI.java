/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.javacc.ls.ls.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * JavaCC language server API.
 * 
 * @author Angelo ZERR
 *
 */
@JsonSegment("javacc")
public interface JavaCCLanguageServerAPI extends LanguageServer {

	@JsonRequest
	CompletableFuture<Integer> generateParser(TextDocumentIdentifier params);

}
