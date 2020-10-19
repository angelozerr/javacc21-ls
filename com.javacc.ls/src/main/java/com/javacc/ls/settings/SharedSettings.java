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
	private final JavaCCFormattingSettings formattingSettings;
	private final JavaCCValidationSettings validationSettings;

	public SharedSettings() {
		this.completionSettings = new JavaCCCompletionSettings();
		this.codeLensSettings = new JavaCCCodeLensSettings();
		this.formattingSettings = new JavaCCFormattingSettings();
		this.validationSettings = new JavaCCValidationSettings();
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
	public JavaCCFormattingSettings getFormattingSettings() {
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

}
