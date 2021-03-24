# javacc21-ls

This project provides a Language Server for JavaCC21 and a client for Eclipse IDE and vscode.

The Language Server for JavaCC21 supports:

 * symbols for JavaCC grammar (outline)
 * validation for JavaCC grammar
 * definition for identifier
 * highlight for identifiers

See the following demo:
 
![JavaCC21 demo](images/JavaCC21Demo.gif)

## Run

To run the language server, download the compiled JAR [from here](https://github.com/angelozerr/javacc21-ls/raw/main/com.javacc.lsp4e/server/com.javacc.ls-uber.jar).

Then, use the following command:

```sh
java -jar com.javacc.ls-uber.jar
```

Language server protocol messages are passed using standard I/O.
