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
package com.javacc.ls.settings;

/**
 * JavaCC shared settings.
 * 
 * @author Angelo ZERR
 *
 */
public class SharedSettings {

	private final JavaCCCompletionSettings completionSettings;
	private final JavaCCCodeLensSettings codeLensSettings;
	private final JavaCCFormattingOptions formattingSettings;
	private final JavaCCValidationSettings validationSettings;
	private final JavaCCFoldingSettings foldingSettings;

	public SharedSettings() {
		this.completionSettings = new JavaCCCompletionSettings();
		this.codeLensSettings = new JavaCCCodeLensSettings();
		this.formattingSettings = new JavaCCFormattingOptions();
		this.validationSettings = new JavaCCValidationSettings();
		this.foldingSettings = new JavaCCFoldingSettings();
	}

	public SharedSettings(SharedSettings newSettings) {
		this();
		//this.completionSettings.merge(newSettings.getCompletionSettings());
		this.formattingSettings.merge(newSettings.getFormattingSettings());
		// this.validationSettings.merge(newSettings.getValidationSettings());
		// this.codeLensSettings.merge(newSettings.getCodeLensSettings());
	}

	/**
	 * Returns the completion settings.
	 * 
	 * @return the completion settings.
	 */
	public JavaCCCompletionSettings getCompletionSettings() {
		return completionSettings;
	}

	/**
	 * Returns the codelens settings.
	 * 
	 * @return the codelens settings.
	 */
	public JavaCCCodeLensSettings getCodeLensSettings() {
		return codeLensSettings;
	}

	/**
	 * Returns the formatting settings.
	 * 
	 * @return the formatting settings.
	 */
	public JavaCCFormattingOptions getFormattingSettings() {
		return formattingSettings;
	}

	/**
	 * Returns the validation settings.
	 * 
	 * @return the validation settings.
	 */
	public JavaCCValidationSettings getValidationSettings() {
		return validationSettings;
	}

	/**
	 * Returns the folding settings.
	 * 
	 * @return the folding settings.
	 */
	public JavaCCFoldingSettings getFoldingSettings() {
		return foldingSettings;
	}

}
