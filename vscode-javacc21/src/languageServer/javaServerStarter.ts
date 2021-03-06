import * as os from 'os';
import * as path from 'path';
import { workspace } from 'vscode';
import { Executable, ExecutableOptions } from 'vscode-languageclient';
import { RequirementsData } from './requirements';
const glob = require('glob');

const DEBUG = startedInDebugMode();
const DEBUG_PORT = 1074;
const JAVACC_SERVER_NAME = 'com.javacc.ls-uber.jar';
const JAVACC_SERVER_MAIN_CLASS = 'com.javacc.ls.ls.JavaCCServerLauncher';

export function prepareExecutable(requirements: RequirementsData, microprofileJavaExtensions: string[]): Executable {
  const executable: Executable = Object.create(null);
  const options: ExecutableOptions = Object.create(null);
  options.env = process.env;
  options.stdio = 'pipe';
  executable.options = options;
  executable.command = path.resolve(requirements.java_home + '/bin/java');
  executable.args = prepareParams(microprofileJavaExtensions);
  return executable;
}

function prepareParams(microprofileJavaExtensions: string[]): string[] {
  const params: string[] = [];
  if (DEBUG) {
    if (process.env.SUSPEND_SERVER === 'true') {
      params.push(`-agentlib:jdwp=transport=dt_socket,server=y,address=${DEBUG_PORT}`);
    } else {
      params.push(`-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${DEBUG_PORT},quiet=y`);
    }
  }

  const vmargs = workspace.getConfiguration("javacc").get("server.vmargs", '');
  if (os.platform() === 'win32') {
    const watchParentProcess = '-DwatchParentProcess=';
    if (vmargs.indexOf(watchParentProcess) < 0) {
      params.push(watchParentProcess + 'false');
    }
  }
  parseVMargs(params, vmargs);
  const serverHome: string = path.resolve(__dirname, '../server');
  const microprofileServerFound: Array<string> = glob.sync(`**/${JAVACC_SERVER_NAME}`, { cwd: serverHome });
  if (microprofileServerFound.length) {
    let mpJavaExtensionsClasspath = '';
    if (microprofileJavaExtensions.length > 0) {
      const classpathSeperator = os.platform() === 'win32' ? ';' : ':';
      mpJavaExtensionsClasspath = classpathSeperator + microprofileJavaExtensions.join(classpathSeperator);
    }

    params.push('-cp');
    params.push(`${serverHome}/*` + mpJavaExtensionsClasspath);
    params.push(JAVACC_SERVER_MAIN_CLASS);
  } else {
    throw new Error('Unable to find required Language Server JARs');
  }
  return params;
}

function hasDebugFlag(args: string[]): boolean {
  if (args) {
    // See https://nodejs.org/en/docs/guides/debugging-getting-started/
    return args.some(arg => /^--inspect/.test(arg) || /^--debug/.test(arg));
  }
  return false;
}

function startedInDebugMode(): boolean {
  const args: string[] = process.execArgv;
  return hasDebugFlag(args);
}

// exported for tests
export function parseVMargs(params: any[], vmargsLine: string) {
  if (!vmargsLine) {
    return;
  }
  const vmargs = vmargsLine.match(/(?:[^\s"]+|"[^"]*")+/g);
  if (vmargs === null) {
    return;
  }
  vmargs.forEach(arg => {
    // remove all standalone double quotes
    arg = arg.replace(/(\\)?"/g, ($0, $1) => { return ($1 ? $0 : ''); });
    // unescape all escaped double quotes
    arg = arg.replace(/(\\)"/g, '"');
    if (params.indexOf(arg) < 0) {
      params.push(arg);
    }
  });
}
