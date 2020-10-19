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
package com.javacc.ls.services;

import java.util.Arrays;

import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;

import com.javacc.ls.ls.client.ClientCommands;

/**
 * References command for CodeLens.
 * 
 * @author Angelo ZERR
 *
 */
public class ReferenceCommand extends Command {

	private transient int nbReferences = 0;

	public ReferenceCommand(String uri, Position position, boolean supportedByClient) {
		super(computeTitle(0), supportedByClient ? ClientCommands.SHOW_REFERENCES : "");
		super.setArguments(Arrays.asList(uri, position));
	}

	public void increment() {
		nbReferences++;
		super.setTitle(computeTitle(nbReferences));
	}

	private static String computeTitle(int nbReferences) {
		if (nbReferences == 1) {
			return nbReferences + " reference";
		}
		return nbReferences + " references";
	}

}