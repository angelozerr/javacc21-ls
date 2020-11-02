/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.javacc.ls.ls;

import static com.javacc.ls.utils.VersionHelper.getVersion;
import static org.eclipse.lsp4j.jsonrpc.CompletableFutures.computeAsync;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import com.javacc.ls.ls.api.JavaCCLanguageClientAPI;
import com.javacc.ls.ls.api.JavaCCLanguageServerAPI;
import com.javacc.ls.ls.commons.ParentProcessWatcher.ProcessLanguageServer;
import com.javacc.ls.ls.commons.client.ExtendedClientCapabilities;
import com.javacc.ls.ls.commons.client.InitializationOptionsExtendedClientCapabilities;
import com.javacc.ls.services.JavaCCLanguageService;
import com.javacc.ls.settings.SharedSettings;
import com.javacc.ls.settings.capabilities.JavaCCCapabilityManager;
import com.javacc.ls.settings.capabilities.ServerCapabilitiesInitializer;

/**
 * JavaCC language server.
 *
 */
public class JavaCCLanguageServer implements LanguageServer, ProcessLanguageServer, JavaCCLanguageServerAPI {

	private static final Logger LOGGER = Logger.getLogger(JavaCCLanguageServer.class.getName());

	private final JavaCCLanguageService javaccLanguageService;
	private final JavaCCTextDocumentService textDocumentService;
	private final WorkspaceService workspaceService;

	private Integer parentProcessId;
	private JavaCCLanguageClientAPI languageClient;
	private JavaCCCapabilityManager capabilityManager;

	public JavaCCLanguageServer() {
		javaccLanguageService = new JavaCCLanguageService();
		textDocumentService = new JavaCCTextDocumentService(this, new SharedSettings());
		workspaceService = new JavaCCWorkspaceService(this);
	}

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		LOGGER.info("Initializing JavaCC server " + getVersion() + " with " + System.getProperty("java.home"));

		this.parentProcessId = params.getProcessId();

		ExtendedClientCapabilities extendedClientCapabilities = InitializationOptionsExtendedClientCapabilities
				.getExtendedClientCapabilities(params);
		capabilityManager.setClientCapabilities(params.getCapabilities(), extendedClientCapabilities);

		textDocumentService.updateClientCapabilities(params.getCapabilities());
		ServerCapabilities serverCapabilities = ServerCapabilitiesInitializer
				.getNonDynamicServerCapabilities(capabilityManager.getClientCapabilities());

		InitializeResult initializeResult = new InitializeResult(serverCapabilities);
		return CompletableFuture.completedFuture(initializeResult);
	}

	/*
	 * Registers all capabilities that do not support client side preferences to
	 * turn on/off
	 *
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.lsp4j.services.LanguageServer#initialized(org.eclipse.lsp4j.
	 * InitializedParams)
	 */
	@Override
	public void initialized(InitializedParams params) {
		capabilityManager.initializeCapabilities();
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		return computeAsync(cc -> new Object());
	}

	@Override
	public void exit() {
		exit(0);
	}

	@Override
	public void exit(int exitCode) {
		System.exit(exitCode);
	}

	public TextDocumentService getTextDocumentService() {
		return this.textDocumentService;
	}

	public WorkspaceService getWorkspaceService() {
		return this.workspaceService;
	}

	public JavaCCLanguageClientAPI getLanguageClient() {
		return languageClient;
	}

	public JavaCCCapabilityManager getCapabilityManager() {
		return capabilityManager;
	}

	public void setClient(LanguageClient languageClient) {
		this.languageClient = (JavaCCLanguageClientAPI) languageClient;
		this.capabilityManager = new JavaCCCapabilityManager(languageClient);
	}

	@Override
	public long getParentProcessId() {
		return parentProcessId != null ? parentProcessId : 0;
	}

	public JavaCCLanguageService getJavaCCLanguageService() {
		return javaccLanguageService;
	}

}
