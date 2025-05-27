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
import java.util.Set;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.MultiMap;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.stringtemplate.v4.ST;

/**
 * Main class for generating a function call graph from Cymbol source code. It takes a Cymbol file
 * as input (or reads from standard input), parses it, and uses a `FunctionListener` to build a
 * graph representation of function calls. The resulting graph can be printed in a simple string
 * format or as a DOT language string for visualization.
 */
public class CallGraph {
  /**
   * Represents the function call graph. It stores function names as nodes and calls between
   * functions as directed edges. Provides methods to add edges and to serialize the graph to a
   * string format or a DOT language string for visualization tools like Graphviz.
   */
  static class Graph {
    // I'm using org.antlr.v4.runtime.misc: OrderedHashSet, MultiMap
    Set<String> nodes = new OrderedHashSet<String>(); // list of functions
    MultiMap<String, String> edges = // caller->callee
        new MultiMap<String, String>();

    /**
     * Adds a directed edge to the graph, representing a function call.
     * @param source The name of the calling function (caller).
     * @param target The name of the called function (callee).
     */
    public void edge(String source, String target) {
      edges.map(source, target);
    }

    /**
     * Returns a simple string representation of the graph, listing all edges and functions.
     * @return A string detailing the graph's edges and nodes.
     */
    public String toString() {
      return "edges: " + edges.toString() + ", functions: " + nodes;
    }

    /**
     * Generates a DOT language representation of the call graph. This output can be used with tools
     * like Graphviz to visualize the graph. The DOT output includes basic styling for nodes and
     * edges.
     * @return A string containing the graph in DOT format.
     */
    public String toDOT() {
      StringBuilder buf = new StringBuilder();
      buf.append("digraph G {\n");
      buf.append("  ranksep=.25;\n");
      buf.append("  edge [arrowsize=.5]\n");
      buf.append("  node [shape=circle, fontname=\"ArialNarrow\",\n");
      buf.append("        fontsize=12, fixedsize=true, height=.45];\n");
      buf.append("  ");
      for (String node : nodes) { // print all nodes first
        buf.append(node);
        buf.append("; ");
      }
      buf.append("\n");
      for (String src : edges.keySet()) {
        for (String trg : edges.get(src)) {
          buf.append("  ");
          buf.append(src);
          buf.append(" -> ");
          buf.append(trg);
          buf.append(";\n");
        }
      }
      buf.append("}\n");
      return buf.toString();
    }

    /**
     * Fill StringTemplate: digraph G { rankdir=LR; <edgePairs:{edge| <edge.a> -> <edge.b>;};
     * separator="\n"> <childless:{f | <f>;}; separator="\n"> }
     *
     * <p>Just as an example. Much cleaner than buf.append method
     */
    /**
     * Generates a DOT language representation of the call graph using StringTemplate. This method
     * demonstrates an alternative and often cleaner way to generate structured text output compared
     * to manual string concatenation.
     * @return A `ST` (StringTemplate) object configured to render the graph in DOT format.
     */
    public ST toST() {
      ST st =
          new ST(
              "digraph G {\n"
                  + "  ranksep=.25; \n"
                  + "  edge [arrowsize=.5]\n"
                  + "  node [shape=circle, fontname=\"ArialNarrow\",\n"
                  + "        fontsize=12, fixedsize=true, height=.45];\n"
                  + "  <funcs:{f | <f>; }>\n"
                  + "  <edgePairs:{edge| <edge.a> -> <edge.b>;}; separator=\"\\n\">\n"
                  + "}\n");
      st.add("edgePairs", edges.getPairs());
      st.add("funcs", nodes);
      return st;
    }
  }

  /**
   * A listener that extends `CymbolBaseListener` to build a call graph. It populates a `Graph`
   * object by tracking function declarations and calls within the Cymbol source code.
   */
  static class FunctionListener extends CymbolBaseListener {
    Graph graph = new Graph();
    String currentFunctionName = null;

    /**
     * Called when the listener enters a function declaration. Adds the function's name to the set
     * of graph nodes and sets it as the current function being processed.
     * @param ctx The parse tree context for the function declaration.
     */
    public void enterFunctionDecl(CymbolParser.FunctionDeclContext ctx) {
      currentFunctionName = ctx.ID().getText();
      graph.nodes.add(currentFunctionName);
    }

    /**
     * Called when the listener exits a function call expression. Adds an edge from the current
     * function (caller) to the function being called (callee) in the graph.
     * @param ctx The parse tree context for the function call.
     */
    public void exitCall(CymbolParser.CallContext ctx) {
      String funcName = ctx.ID().getText();
      // map current function to the callee
      graph.edge(currentFunctionName, funcName);
    }
  }

  /**
   * Main entry point for the call graph generator. Parses the input Cymbol source (from a file or
   * stdin), walks the parse tree using `FunctionListener` to collect call graph data, and then
   * prints the graph information and its DOT representation to standard output.
   * @param args Command line arguments. If an argument is provided, it is treated as the path to
   *     the input Cymbol file. Otherwise, standard input is used.
   * @throws Exception If there is an error reading the input file or during parsing.
   */
  public static void main(String[] args) throws Exception {
    String inputFile = null;
    if (args.length > 0) inputFile = args[0];
    InputStream is = System.in;
    if (inputFile != null) {
      is = new FileInputStream(inputFile);
    }
    ANTLRInputStream input = new ANTLRInputStream(is);
    CymbolLexer lexer = new CymbolLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    CymbolParser parser = new CymbolParser(tokens);
    parser.setBuildParseTree(true);
    ParseTree tree = parser.file();
    // show tree in text form

    ParseTreeWalker walker = new ParseTreeWalker();
    FunctionListener collector = new FunctionListener();
    walker.walk(collector, tree);
    System.out.println(collector.graph.toString());
    System.out.println(collector.graph.toDOT());

    // Here's another example that uses StringTemplate to generate output
  }
}
