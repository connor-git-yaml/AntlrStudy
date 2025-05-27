/***
 * Excerpted from "The Definitive ANTLR 4 Reference",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/tpantlr2 for more book information.
 ***/
import java.io.FileInputStream;
import java.io.InputStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Main class for a simple calculator program. It reads expressions from a file or standard input,
 * parses them, and then evaluates them using the `EvalVisitor`.
 */
public class Calc {
  /**
   * Entry point for the calculator program. Sets up the input stream, lexer, parser, and initiates
   * the parsing and evaluation process.
   * @param args Command line arguments. If an argument is provided, it's treated as the input file
   *     path. Otherwise, standard input is used.
   * @throws Exception If there's an error reading the input file or during parsing/evaluation.
   */
  public static void main(String[] args) throws Exception {
    String inputFile = null;
    if (args.length > 0) inputFile = args[0];
    InputStream is = System.in;
    if (inputFile != null) is = new FileInputStream(inputFile);
    ANTLRInputStream input = new ANTLRInputStream(is);
    LabeledExprLexer lexer = new LabeledExprLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    LabeledExprParser parser = new LabeledExprParser(tokens);
    ParseTree tree = parser.prog(); // parse

    EvalVisitor eval = new EvalVisitor();
    eval.visit(tree);
  }
}
