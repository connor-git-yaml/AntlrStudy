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
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.*;

public class InsertSerialID {
  public static void main(String[] args) throws Exception {
    String inputFile = null;
    if (args.length > 0) inputFile = args[0];
    InputStream is = System.in;
    if (inputFile != null) {
      is = new FileInputStream(inputFile);
    }
    ANTLRInputStream input = new ANTLRInputStream(is);

    JavaLexer lexer = new JavaLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    JavaParser parser = new JavaParser(tokens);
    ParseTree tree = parser.compilationUnit(); // parse

    ParseTreeWalker walker = new ParseTreeWalker(); // create standard walker
    InsertSerialIDListener extractor = new InsertSerialIDListener(tokens);
    walker.walk(extractor, tree); // initiate walk of tree with listener

    // print back ALTERED stream
    System.out.println(extractor.rewriter.getText());
  }
}
