/**
 *  Copyright (c) 2020 Red Hat, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Red Hat Inc. - initial API and implementation
 */
'use strict';

/**
 * Commonly used commands
 */
export namespace Commands {

    /**
     * Show JavaCC references
     */
    export const SHOW_REFERENCES = 'javacc.show.references';

    /**
     * Show editor references
     */
    export const EDITOR_SHOW_REFERENCES = 'editor.action.showReferences';

    /**
     * Render markdown string to html string
     */
    export const MARKDOWN_API_RENDER = 'markdown.api.render';

    export const OPEN_DOCS = "javacc.open.docs";

    export const OPEN_DOCS_HOME = "javacc.open.docs.home";

    /**
     * JavaCC commands
     */

    export const GENERATE_PARSER = "javacc.generate.parser";
}
