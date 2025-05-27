/***
 * Excerpted from "The Definitive ANTLR 4 Reference",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/tpantlr2 for more book information.
 ***/
import java.util.HashMap;
import java.util.Map;

/**
 * A visitor class that evaluates expressions defined in the `LabeledExpr.g4` grammar. It extends
 * `LabeledExprBaseVisitor` and overrides methods to handle assignments, print statements,
 * arithmetic operations, and variable lookups. Results of evaluations are integers.
 */
public class EvalVisitor extends LabeledExprBaseVisitor<Integer> {
  /** "memory" for our calculator; variable/value pairs go here */
  Map<String, Integer> memory = new HashMap<String, Integer>();

  /** ID '=' expr NEWLINE */
  /**
   * Handles assignment statements (e.g., `x = 5`). Computes the value of the expression on the
   * right-hand side and stores it in memory using the variable ID from the left-hand side.
   * @param ctx The parse tree context for the assignment rule.
   * @return The integer value assigned to the variable.
   */
  @Override
  public Integer visitAssign(LabeledExprParser.AssignContext ctx) {
    String id = ctx.ID().getText(); // id is left-hand side of '='
    int value = visit(ctx.expr()); // compute value of expression on right
    memory.put(id, value); // store it in our memory
    return value;
  }

  /** expr NEWLINE */
  /**
   * Handles print statements. Evaluates the expression and prints its result to standard output.
   * @param ctx The parse tree context for the print expression rule.
   * @return A dummy integer value (0), as the primary purpose is the side effect of printing.
   */
  @Override
  public Integer visitPrintExpr(LabeledExprParser.PrintExprContext ctx) {
    Integer value = visit(ctx.expr()); // evaluate the expr child
    System.out.println(value); // print the result
    return 0; // return dummy value
  }

  /** INT */
  /**
   * Handles integer literals. Converts the integer text from the parse tree into an Integer object.
   * @param ctx The parse tree context for an integer literal.
   * @return The integer value of the literal.
   */
  @Override
  public Integer visitInt(LabeledExprParser.IntContext ctx) {
    return Integer.valueOf(ctx.INT().getText());
  }

  /** ID */
  /**
   * Handles variable identifiers. Looks up the variable's ID in memory. If found, returns its
   * stored integer value; otherwise, returns 0.
   * @param ctx The parse tree context for an identifier.
   * @return The integer value of the identifier from memory, or 0 if not found.
   */
  @Override
  public Integer visitId(LabeledExprParser.IdContext ctx) {
    String id = ctx.ID().getText();
    if (memory.containsKey(id)) return memory.get(id);
    return 0;
  }

  /** expr op=('*'|'/') expr */
  /**
   * Handles multiplication and division operations. Evaluates the left and right sub-expressions
   * and performs the multiplication or division based on the operator token.
   * @param ctx The parse tree context for a multiplication or division expression.
   * @return The integer result of the multiplication or division.
   */
  @Override
  public Integer visitMulDiv(LabeledExprParser.MulDivContext ctx) {
    int left = visit(ctx.expr(0)); // get value of left subexpression
    int right = visit(ctx.expr(1)); // get value of right subexpression
    if (ctx.op.getType() == LabeledExprParser.MUL) return left * right;
    return left / right; // must be DIV
  }

  /** expr op=('+'|'-') expr */
  /**
   * Handles addition and subtraction operations. Evaluates the left and right sub-expressions and
   * performs the addition or subtraction based on the operator token.
   * @param ctx The parse tree context for an addition or subtraction expression.
   * @return The integer result of the addition or subtraction.
   */
  @Override
  public Integer visitAddSub(LabeledExprParser.AddSubContext ctx) {
    int left = visit(ctx.expr(0)); // get value of left subexpression
    int right = visit(ctx.expr(1)); // get value of right subexpression
    if (ctx.op.getType() == LabeledExprParser.ADD) return left + right;
    return left - right; // must be SUB
  }

  /** '(' expr ')' */
  /**
   * Handles expressions enclosed in parentheses. Evaluates the inner expression and returns its
   * value.
   * @param ctx The parse tree context for a parenthesized expression.
   * @return The integer value of the inner expression.
   */
  @Override
  public Integer visitParens(LabeledExprParser.ParensContext ctx) {
    return visit(ctx.expr()); // return child expr's value
  }
}
