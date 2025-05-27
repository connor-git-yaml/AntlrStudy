/***
 * Excerpted from "The Definitive ANTLR 4 Reference",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/tpantlr2 for more book information.
 ***/
import org.antlr.v4.runtime.TokenStream;

/**
 * A listener that extends `JavaBaseListener` to extract a Java interface definition from a parsed
 * Java class. It prints the import statements, and generates an interface with the same class name
 * prefixed by 'I', containing all public methods of the original class.
 */
public class ExtractInterfaceListener extends JavaBaseListener {
  JavaParser parser;

  /**
   * Constructs a new `ExtractInterfaceListener`.
   * @param parser The `JavaParser` instance that produced the parse tree. This is used to access
   *     the token stream for extracting original text.
   */
  public ExtractInterfaceListener(JavaParser parser) {
    this.parser = parser;
  }

  /** Listen to matches of classDeclaration */
  /**
   * Called when the listener enters an import declaration. Prints the full import statement to
   * standard output.
   * @param ctx The parse tree context for the import declaration.
   */
  @Override
  public void enterImportDeclaration(JavaParser.ImportDeclarationContext ctx) {
    System.out.println(parser.getTokenStream().getText(ctx));
  }

  /**
   * Called when the listener enters a class declaration. Prints the beginning of the interface
   * definition, using the original class name prefixed with 'I'.
   * @param ctx The parse tree context for the class declaration.
   */
  @Override
  public void enterClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
    System.out.println("interface I" + ctx.Identifier() + " {");
  }

  /**
   * Called when the listener exits a class declaration. Prints the closing brace '}' for the
   * interface definition.
   * @param ctx The parse tree context for the class declaration.
   */
  @Override
  public void exitClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
    System.out.println("}");
  }

  /** Listen to matches of methodDeclaration */
  /**
   * Called when the listener enters a method declaration. Extracts the method's type, name, and
   * formal parameters, and prints the method signature as part of the interface definition. If the
   * method has no explicit type, 'void' is assumed.
   * @param ctx The parse tree context for the method declaration.
   */
  @Override
  public void enterMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
    // need parser to get tokens
    TokenStream tokens = parser.getTokenStream();
    String type = "void";
    if (ctx.type() != null) {
      type = tokens.getText(ctx.type());
    }
    String args = tokens.getText(ctx.formalParameters());
    System.out.println("\t" + type + " " + ctx.Identifier() + args + ";");
  }
}
