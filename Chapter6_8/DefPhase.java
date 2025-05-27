/***
 * Excerpted from "The Definitive ANTLR 4 Reference",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/tpantlr2 for more book information.
 ***/
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

/**
 * A listener that extends `CymbolBaseListener` to perform the symbol definition phase for Cymbol
 * source code. It traverses the parse tree to identify scopes (global, function, block) and define
 * symbols such as functions and variables within their appropriate scopes. It uses a
 * `ParseTreeProperty` to associate scopes with parse tree nodes.
 */
public class DefPhase extends CymbolBaseListener {
  ParseTreeProperty<Scope> scopes = new ParseTreeProperty<Scope>();
  GlobalScope globals;
  Scope currentScope; // define symbols in this scope

  /**
   * Called when the listener enters the top-level file rule. Initializes the global scope and sets
   * it as the current scope.
   * @param ctx The parse tree context for the file rule.
   */
  public void enterFile(CymbolParser.FileContext ctx) {
    globals = new GlobalScope(null);
    currentScope = globals;
  }

  /**
   * Called when the listener exits the top-level file rule. Prints the global scope and all its
   * defined symbols (for debugging or informational purposes).
   * @param ctx The parse tree context for the file rule.
   */
  public void exitFile(CymbolParser.FileContext ctx) {
    System.out.println(globals);
  }

  /**
   * Called when the listener enters a function declaration. Defines a `FunctionSymbol` in the
   * current scope, then pushes a new scope for the function, making it the current scope. The
   * function's type is determined from the parse tree.
   * @param ctx The parse tree context for the function declaration.
   */
  public void enterFunctionDecl(CymbolParser.FunctionDeclContext ctx) {
    String name = ctx.ID().getText();
    int typeTokenType = ctx.type().start.getType();
    Symbol.Type type = CheckSymbols.getType(typeTokenType);

    // push new scope by making new one that points to enclosing scope
    FunctionSymbol function = new FunctionSymbol(name, type, currentScope);
    currentScope.define(function); // Define function in current scope
    saveScope(ctx, function); // Push: set function's parent to current
    currentScope = function; // Current scope is now function scope
  }

  void saveScope(ParserRuleContext ctx, Scope s) {
    scopes.put(ctx, s);
  }

  /**
   * Called when the listener exits a function declaration. Prints the current (function) scope (for
   * debugging or informational purposes) and then pops it, restoring the enclosing scope as the
   * current scope.
   * @param ctx The parse tree context for the function declaration.
   */
  public void exitFunctionDecl(CymbolParser.FunctionDeclContext ctx) {
    System.out.println(currentScope);
    currentScope = currentScope.getEnclosingScope(); // pop scope
  }

  /**
   * Called when the listener enters a block statement. Pushes a new `LocalScope` associated with
   * this block, making it the current scope.
   * @param ctx The parse tree context for the block statement.
   */
  public void enterBlock(CymbolParser.BlockContext ctx) {
    // push new local scope
    currentScope = new LocalScope(currentScope);
    saveScope(ctx, currentScope);
  }

  /**
   * Called when the listener exits a block statement. Prints the current (local) scope (for
   * debugging or informational purposes) and then pops it, restoring the enclosing scope as the
   * current scope.
   * @param ctx The parse tree context for the block statement.
   */
  public void exitBlock(CymbolParser.BlockContext ctx) {
    System.out.println(currentScope);
    currentScope = currentScope.getEnclosingScope(); // pop scope
  }

  /**
   * Called when the listener exits a formal parameter declaration within a function. Defines a
   * `VariableSymbol` for the parameter in the current (function) scope.
   * @param ctx The parse tree context for the formal parameter.
   */
  public void exitFormalParameter(CymbolParser.FormalParameterContext ctx) {
    defineVar(ctx.type(), ctx.ID().getSymbol());
  }

  /**
   * Called when the listener exits a variable declaration. Defines a `VariableSymbol` for the
   * variable in the current scope.
   * @param ctx The parse tree context for the variable declaration.
   */
  public void exitVarDecl(CymbolParser.VarDeclContext ctx) {
    defineVar(ctx.type(), ctx.ID().getSymbol());
  }

  void defineVar(CymbolParser.TypeContext typeCtx, Token nameToken) {
    int typeTokenType = typeCtx.start.getType();
    Symbol.Type type = CheckSymbols.getType(typeTokenType);
    VariableSymbol var = new VariableSymbol(nameToken.getText(), type);
    currentScope.define(var); // Define symbol in current scope
  }
}
