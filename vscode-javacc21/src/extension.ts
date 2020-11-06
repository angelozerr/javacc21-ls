/**
 * Copyright 2019 Red Hat, Inc. and others.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import * as path from 'path';
import * as requirements from './languageServer/requirements';

import { DidChangeConfigurationNotification, LanguageClientOptions, LanguageClient, ReferencesRequest, TextDocumentIdentifier, RequestType } from 'vscode-languageclient';
import { ExtensionContext, commands, window, workspace, Uri, Position } from 'vscode';
import { prepareExecutable } from './languageServer/javaServerStarter';
import { registerConfigurationUpdateCommand, registerOpenURICommand, CommandKind } from './lsp-commands';
import { Commands } from './commands';
import { markdownPreviewProvider } from './markdownPreviewProvider';

namespace GenerateParserRequest {
  export const type: RequestType<TextDocumentIdentifier, any, any, any> = new RequestType('javacc/generateParser');
}

let languageClient: LanguageClient;

export function activate(context: ExtensionContext) {

  // Register commands for XML documentation
  context.subscriptions.push(markdownPreviewProvider);
  context.subscriptions.push(commands.registerCommand(Commands.OPEN_DOCS_HOME, async () => {
    const uri = 'README.md';
    const title = 'JavaCC Documentation';
    const sectionId = '';
    markdownPreviewProvider.show(context.asAbsolutePath(path.join('docs', uri)), title, sectionId, context);
  }));
  context.subscriptions.push(commands.registerCommand(Commands.OPEN_DOCS, async (params: { page: string, section: string }) => {
    const page = params.page.endsWith('.md') ? params.page.substr(0, params.page.length - 3) : params.page;
    const uri = page + '.md';
    const sectionId = params.section || '';
    const title = 'JavaCC ' + page;
    markdownPreviewProvider.show(context.asAbsolutePath(path.join('docs', uri)), title, sectionId, context);
  }));

  connectToJavaCC21LS(context).then(() => {

  }).catch((error) => {
    window.showErrorMessage(error.message, error.label).then((selection) => {
      if (error.label && error.label === selection && error.openUrl) {
        commands.executeCommand('vscode.open', error.openUrl);
      }
    });
  });
}

export function deactivate() {
  languageClient.stop();
}

function connectToJavaCC21LS(context: ExtensionContext) {
  return requirements.resolveRequirements().then(requirements => {
    const clientOptions: LanguageClientOptions = {
      documentSelector: [
        { scheme: 'file', language: 'javacc' }],
      // wrap with key 'settings' so it can be handled same a DidChangeConfiguration
      initializationOptions: {
        settings: getJavaCC21Settings()
      },
      synchronize: {
        // preferences starting with these will trigger didChangeConfiguration
        configurationSection: ['javacc', '[javacc]']
      },
      middleware: {
        workspace: {
          didChangeConfiguration: () => {
            languageClient.sendNotification(DidChangeConfigurationNotification.type, { settings: getJavaCC21Settings() });
          }
        }
      }
    };

    const serverOptions = prepareExecutable(requirements, []);
    languageClient = new LanguageClient('javacc', 'JavaCC 21 Support', serverOptions, clientOptions);
    context.subscriptions.push(languageClient.start());
    return languageClient.onReady().then(() => {

      // Code Lens actions
      context.subscriptions.push(commands.registerCommand(Commands.SHOW_REFERENCES, (uriString: string, position: Position) => {
        const uri = Uri.parse(uriString);
        workspace.openTextDocument(uri).then(document => {
          // Consume references service from the XML Language Server
          const param = languageClient.code2ProtocolConverter.asTextDocumentPositionParams(document, position);
          languageClient.sendRequest(ReferencesRequest.type, param).then(locations => {
            commands.executeCommand(Commands.EDITOR_SHOW_REFERENCES, uri, languageClient.protocol2CodeConverter.asPosition(position), locations.map(languageClient.protocol2CodeConverter.asLocation));
          });
        });
      }));

      // Register custom JavaCC commands
      context.subscriptions.push(commands.registerCommand(Commands.GENERATE_PARSER, async (params) => {
        const uri = window.activeTextEditor.document.uri;
        const param = TextDocumentIdentifier.create(uri.toString());
        let text = languageClient.sendRequest(GenerateParserRequest.type, param);
      }));


    });
  });

  /**
   * Returns a json object with key 'javacc' and a json object value that
   * holds all javacc. settings.
   *
   * Returns: {
   *            'javacc': {...}
   *          }
   */
  function getJavaCC21Settings(): JSON {
    const configJavaCC = workspace.getConfiguration().get('javacc');
    let javacc;
    if (!configJavaCC) { // Set default preferences if not provided
      const defaultValue =
      {
        javacc: {

        }
      };
      javacc = defaultValue;
    } else {
      const x = JSON.stringify(configJavaCC); // configJavaCC is not a JSON type
      javacc = { javacc: JSON.parse(x) };
    }
    return javacc;
  }
}
