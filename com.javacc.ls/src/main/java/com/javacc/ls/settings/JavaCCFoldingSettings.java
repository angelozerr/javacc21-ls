/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
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

import org.eclipse.lsp4j.FoldingRangeCapabilities;

/**
 * A wrapper around LSP {@link FoldingRangeCapabilities}.
 *
 */
public class JavaCCFoldingSettings {

	private FoldingRangeCapabilities capabilities;

	public void setCapabilities(FoldingRangeCapabilities capabilities) {
		this.capabilities = capabilities;
	}

	public FoldingRangeCapabilities getCapabilities() {
		return capabilities;
	}

	public Integer getRangeLimit() {
		return capabilities != null ? capabilities.getRangeLimit() : null;
	}
}
