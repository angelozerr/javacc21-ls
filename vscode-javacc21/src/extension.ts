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
import * as requirements from './languageServer/requirements';

import { VSCodeCommands } from './definitions/constants';

import { DidChangeConfigurationNotification, LanguageClientOptions, LanguageClient } from 'vscode-languageclient';
import { ExtensionContext, commands, window, workspace } from 'vscode';
import { QuarkusContext } from './QuarkusContext';
import { addExtensionsWizard } from './addExtensions/addExtensionsWizard';
import { createTerminateDebugListener } from './debugging/terminateProcess';
import { generateProjectWizard } from './generateProject/generationWizard';
import { prepareJavaCC21Executable } from './languageServer/javaServerStarter';
import { tryStartDebugging } from './debugging/startDebugging';
import { WelcomeWebview } from './webviews/WelcomeWebview';
import { QuarkusConfig } from './QuarkusConfig';
import { registerConfigurationUpdateCommand, registerOpenURICommand, CommandKind } from './lsp-commands';

let languageClient: LanguageClient;

export function activate(context: ExtensionContext) {

  connectToJavaCC21LS(context).then(() => {

  }).catch((error) => {
    window.showErrorMessage(error.message, error.label).then((selection) => {
      if (error.label && error.label === selection && error.openUrl) {
        commands.executeCommand('vscode.open', error.openUrl);
      }
    });
  });

  function bindRequest(request: string) {
    languageClient.onRequest(request, async (params: any) =>
      <any> await commands.executeCommand("java.execute.workspaceCommand", request, params)
    );
  }

  function bindNotification(notification: string) {
    context.subscriptions.push(commands.registerCommand(notification, (event: any) => {
      languageClient.sendNotification(notification, event);
    }));
  }
}

export function deactivate() {
}

function connectToJavaCC21LS(context: ExtensionContext) {
  return requirements.resolveRequirements().then(requirements => {
    const clientOptions: LanguageClientOptions = {
      documentSelector: [
        { scheme: 'file', language: 'javacc21' }      ],
      // wrap with key 'settings' so it can be handled same a DidChangeConfiguration
      initializationOptions: {
        settings: getJavaCC21Settings()
      },
      synchronize: {
        // preferences starting with these will trigger didChangeConfiguration
        configurationSection: ['javacc21', '[javacc21]']
      },
      middleware: {
        workspace: {
          didChangeConfiguration: () => {
            languageClient.sendNotification(DidChangeConfigurationNotification.type, { settings: getJavaCC21Settings() });
          }
        }
      }
    };

    const serverOptions = prepareJavaCC21Executable(requirements);
    languageClient = new LanguageClient('javacc21', 'JavaCC 21 Support', serverOptions, clientOptions);
    context.subscriptions.push(languageClient.start());
    return languageClient.onReady();
  });

  /**
   * Returns a json object with key 'quarkus' and a json object value that
   * holds all quarkus. settings.
   *
   * Returns: {
   *            'quarkus': {...}
   *          }
   */
  function getJavaCC21Settings(): JSON {
    const configQuarkus = workspace.getConfiguration().get('javacc21');
    let quarkus;
    if (!configQuarkus) { // Set default preferences if not provided
      const defaultValue =
      {
        qute: {

        }
      };
      quarkus = defaultValue;
    } else {
      const x = JSON.stringify(configQuarkus); // configQuarkus is not a JSON type
      quarkus = { quarkus : JSON.parse(x)};
    }
    return quarkus;
  }
}