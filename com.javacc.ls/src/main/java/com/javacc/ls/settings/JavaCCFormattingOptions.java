/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package com.javacc.ls.settings;

import org.eclipse.lsp4j.FormattingOptions;

/**
 * This class is the root of all formatting settings. It is necessary to update
 * this class for any new additions.
 * 
 * All defaults should be set here to eventually be overridden if needed.
 */
public class JavaCCFormattingOptions extends FormattingOptions {

	public static final int DEFAULT_TAB_SIZE = 2;

	public JavaCCFormattingOptions() {
		this(false);
	}

	/**
	 * Create an XMLFormattingOptions instance with the option to initialize default
	 * values for all supported settings.
	 */
	public JavaCCFormattingOptions(boolean initializeDefaults) {
		if (initializeDefaults) {
			initializeDefaultSettings();
		}
	}

	/**
	 * Necessary: Initialize default values in case client does not provide one
	 */
	private void initializeDefaultSettings() {
		super.setTabSize(DEFAULT_TAB_SIZE);
		super.setInsertSpaces(true);
//		this.setEnabled(true);
	}

	public JavaCCFormattingOptions(int tabSize, boolean insertSpaces, boolean initializeDefaultSettings) {
		if (initializeDefaultSettings) {
			initializeDefaultSettings();
		}
		super.setTabSize(tabSize);
		super.setInsertSpaces(insertSpaces);
	}

	public JavaCCFormattingOptions(int tabSize, boolean insertSpaces) {
		this(tabSize, insertSpaces, true);
	}

	public JavaCCFormattingOptions(FormattingOptions options, boolean initializeDefaultSettings) {
		if (initializeDefaultSettings) {
			initializeDefaultSettings();
		}
		merge(options);
	}

	public JavaCCFormattingOptions(FormattingOptions options) {
		this(options, true);
	}

	public JavaCCFormattingOptions merge(FormattingOptions formattingOptions) {
		formattingOptions.entrySet().stream().forEach(entry -> {
			this.put(entry.getKey(), entry.getValue());
		});
		return this;
	}

	public static JavaCCFormattingOptions create(FormattingOptions options, FormattingOptions sharedFormattingOptions) {
		return new JavaCCFormattingOptions(options).merge(sharedFormattingOptions);
	}

}
