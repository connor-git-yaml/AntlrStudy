/***
 * Excerpted from "The Definitive ANTLR 4 Reference",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/tpantlr2 for more book information.
 ***/
import org.antlr.v4.runtime.tree.ParseTreeProperty;

/**
 * A listener that extends `CymbolBaseListener` to perform the symbol reference checking phase for
 * Cymbol source code. It traverses the parse tree and, using the scopes previously populated (e.g.,
 * by `DefPhase`), resolves references to variables and functions. It reports errors for undefined
 * symbols or type mismatches (e.g., using a variable as a function).
 */
public class RefPhase extends CymbolBaseListener {
  ParseTreeProperty<Scope> scopes;
  GlobalScope globals;
  Scope currentScope; // resolve symbols starting in this scope

  /**
   * Constructs a new `RefPhase` listener.
   * @param globals The global scope containing all top-level symbol definitions.
   * @param scopes A `ParseTreeProperty` mapping parse tree nodes to their corresponding `Scope`
   *     objects, typically populated by a definition phase listener.
   */
  public RefPhase(GlobalScope globals, ParseTreeProperty<Scope> scopes) {
    this.scopes = scopes;
    this.globals = globals;
  }

  /**
   * Called when the listener enters the top-level file rule. Sets the current scope to the global
   * scope.
   * @param ctx The parse tree context for the file rule.
   */
  public void enterFile(CymbolParser.FileContext ctx) {
    currentScope = globals;
  }

  /**
   * Called when the listener enters a function declaration. Retrieves the scope associated with this
   * function from the `scopes` map and sets it as the current scope.
   * @param ctx The parse tree context for the function declaration.
   */
  public void enterFunctionDecl(CymbolParser.FunctionDeclContext ctx) {
    currentScope = scopes.get(ctx);
  }

  /**
   * Called when the listener exits a function declaration. Pops the current (function) scope,
   * restoring the enclosing scope as the current scope.
   * @param ctx The parse tree context for the function declaration.
   */
  public void exitFunctionDecl(CymbolParser.FunctionDeclContext ctx) {
    currentScope = currentScope.getEnclosingScope();
  }

  /**
   * Called when the listener enters a block statement. Retrieves the scope associated with this
   * block from the `scopes` map and sets it as the current scope.
   * @param ctx The parse tree context for the block statement.
   */
  public void enterBlock(CymbolParser.BlockContext ctx) {
    currentScope = scopes.get(ctx);
  }

  /**
   * Called when the listener exits a block statement. Pops the current (local) scope, restoring the
   * enclosing scope as the current scope.
   * @param ctx The parse tree context for the block statement.
   */
  public void exitBlock(CymbolParser.BlockContext ctx) {
    currentScope = currentScope.getEnclosingScope();
  }

  /**
   * Called when the listener exits a variable reference (e.g., in an expression). Resolves the
   * variable name in the current scope. Reports an error if the variable is not found or if the
   * resolved symbol is not a variable (e.g., it's a function name).
   * @param ctx The parse tree context for the variable reference.
   */
  public void exitVar(CymbolParser.VarContext ctx) {
    String name = ctx.ID().getSymbol().getText();
    Symbol var = currentScope.resolve(name);
    if (var == null) {
      CheckSymbols.error(ctx.ID().getSymbol(), "no such variable: " + name);
    }
    if (var instanceof FunctionSymbol) {
      CheckSymbols.error(ctx.ID().getSymbol(), name + " is not a variable");
    }
  }

  /**
   * Called when the listener exits a function call. Resolves the function name in the current
   * scope. Reports an error if the function is not found or if the resolved symbol is not a
   * function (e.g., it's a variable name).
   * @param ctx The parse tree context for the function call.
   */
  public void exitCall(CymbolParser.CallContext ctx) {
    // can only handle f(...) not expr(...)
    String funcName = ctx.ID().getText();
    Symbol meth = currentScope.resolve(funcName);
    if (meth == null) {
      CheckSymbols.error(ctx.ID().getSymbol(), "no such function: " + funcName);
    }
    if (meth instanceof VariableSymbol) {
      CheckSymbols.error(ctx.ID().getSymbol(), funcName + " is not a function");
    }
  }
}
