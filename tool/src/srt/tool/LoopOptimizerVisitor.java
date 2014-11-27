package srt.tool;

import java.util.ArrayList;
import java.util.HashSet;

import srt.ast.AssertStmt;
import srt.ast.AssignStmt;
import srt.ast.AssumeStmt;
import srt.ast.BinaryExpr;
import srt.ast.BlockStmt;
import srt.ast.Decl;
import srt.ast.DeclRef;
import srt.ast.Expr;
import srt.ast.IfStmt;
import srt.ast.IntLiteral;
import srt.ast.Node;
import srt.ast.Stmt;
import srt.ast.StmtList;
import srt.ast.UnaryExpr;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;
import srt.util.FunctionUtil;

public class LoopOptimizerVisitor extends DefaultVisitor {
	private ArrayList<String> vars;
	private ArrayList<Expr> exprs;
	private ArrayList<Boolean> containsX;
	private Expr iterationsCountExpr;
	private DeclRef iterationsCountRef;
	private boolean expectX;
	private int loopCountIndex = 0;
	
	public LoopOptimizerVisitor() {
		super(true);
	}
	
	@Override
	public Object visit(WhileStmt whileStmt) {
		// Try to simply any children first
		whileStmt = (WhileStmt) super.visit(whileStmt);
		
		// Here we collect statements from the loop
		ArrayList<Stmt> stmts = new ArrayList<Stmt>();
		
		// Some ugly way of initializing vars
		vars = new ArrayList<String>();
		exprs = new ArrayList<Expr>();
		containsX = new ArrayList<Boolean>();
		iterationsCountRef = newLoopCountDeclRef();
		boolean canSimply = true;
		canSimply &= analyzeLoop(whileStmt, whileStmt.getBody(), stmts);
		canSimply &= analyzeCondition(whileStmt, whileStmt.getCondition());
		//System.out.println("Loop " + whileStmt + " simply " + canSimply);
		
		// TO DO: if we cannot simply the loop we still can remove some stmts out of the loop
		if (!canSimply) {
			loopCountIndex -= 1;
			return whileStmt;
		}
		
		// Nice, we can simply this loop then, add the loop iterationsExpr
		AssignStmt iterations = new AssignStmt(iterationsCountRef, iterationsCountExpr);
		stmts.add(0, iterations);
		
		//BlockStmt blockStmt = new BlockStmt(stmts);
		return super.visit(new StmtList(stmts));
	}
	
	private DeclRef newLoopCountDeclRef() {
		loopCountIndex += 1;
		return new DeclRef("$cnt" + loopCountIndex);
	}
	
	// Check the loop condition is applied on only one variable
	private boolean analyzeCondition(WhileStmt whileStmt, Expr expr) {
		if (!(expr instanceof BinaryExpr)) {
			//System.out.println("Not binary expr!");
			return false;
		}
		BinaryExpr binaryExpr = (BinaryExpr) expr;
		Expr lhs = binaryExpr.getLhs();
		Expr rhs = binaryExpr.getRhs();
		int operator = binaryExpr.getOperator();
		
		// Let's assume lhs has the var in mod set and rhs is constant
		if (FunctionUtil.setIntersection(rhs.getUses(), whileStmt.getModifies())) {
			lhs = binaryExpr.getRhs();
			rhs = binaryExpr.getLhs();
			if (BinaryExpr.isReversableOperator(operator))
				operator = BinaryExpr.reverseOperator(binaryExpr.getOperator());
		}
		if (!(lhs instanceof DeclRef) || FunctionUtil.setIntersection(rhs.getUses(), whileStmt.getModifies())) {
			//System.out.println("not normal case!" + lhs + " " + rhs);
			return false;
		}
		DeclRef iterator = (DeclRef) lhs;
		
		Expr delta = getIteratorExpr(iterator.getName());
		IntLiteral justOne = new IntLiteral(1);
		//System.out.println("delta: " + delta);
		if (delta == null)
			return false;
		
		// Can handle only these operators
		Expr iterationsExpr = null;
		if (operator == BinaryExpr.GEQ) {
			iterationsExpr = new BinaryExpr(BinaryExpr.SUBTRACT, iterator, 
					new BinaryExpr(BinaryExpr.SUBTRACT, rhs, justOne));
		} else if (operator == BinaryExpr.GT) {
			iterationsExpr = new BinaryExpr(BinaryExpr.SUBTRACT, iterator, rhs);		
		} else if (operator == BinaryExpr.LEQ) {
			iterationsExpr = new BinaryExpr(BinaryExpr.SUBTRACT, rhs,
					new BinaryExpr(BinaryExpr.SUBTRACT, iterator, justOne));
		} else if (operator == BinaryExpr.LT) {
			iterationsExpr = new BinaryExpr(BinaryExpr.SUBTRACT, rhs, iterator);		
		}
		
		if (iterationsExpr != null) {
			iterationsExpr = new BinaryExpr(BinaryExpr.ADD, iterationsExpr,
					new BinaryExpr(BinaryExpr.SUBTRACT, delta, justOne));
			iterationsCountExpr = new BinaryExpr(BinaryExpr.DIVIDE, iterationsExpr, delta);
			return true;
		}
		
		return false;
	}
	
	private Expr getIteratorExpr(String varName) {
		//System.out.println("try to get iterator " + varName);
		Expr iteratorExpr = null;
		
		for (int i = 0; i < vars.size(); ++i) {
			if (vars.get(i).equals(varName)) {
				Expr expr = exprs.get(i);
				boolean containsIterator = containsX.get(i);
				// if i is always set to a constant eg: i = c + 2
				if (!containsIterator)
					return null;
				
				if (iteratorExpr != null)
					iteratorExpr = new BinaryExpr(BinaryExpr.ADD, iteratorExpr, expr);
				else
					iteratorExpr = expr;
			}
		}
		return iteratorExpr;
	}
	
	// Check the loop doesn't contain if, while, asserts or assumes on loop modset
	private boolean analyzeLoop(WhileStmt whileStmt, Node node, ArrayList<Stmt> stmts) {
		boolean result = true;
		HashSet<String> modifies = whileStmt.getModifies();
		//System.out.println("loop modifies: " + modifies);
		
		if (node instanceof WhileStmt)
			return false;
		
		// Will handle if cases later if they don't contain condition on modset
		if (node instanceof IfStmt)
			return false;
		if (node instanceof AssumeStmt) {
			if (FunctionUtil.setIntersection(modifies, node.getUses()))
				return false;
			stmts.add((AssumeStmt) node);
		}
		if (node instanceof AssertStmt) {
			if (FunctionUtil.setIntersection(modifies, node.getUses()))
				return false;
			stmts.add((AssertStmt) node);
		}
		if (node instanceof BlockStmt) {
			for (Node child : ((BlockStmt) node).getStmtList().getStatements())
				result &= analyzeLoop(whileStmt, child, stmts);
		}
		if (node instanceof Decl)
			return false;
		
		// The tricky case, enforce no assignment from vars in modifies
		if (node instanceof AssignStmt) {
			String varName = ((AssignStmt) node).getLhs().getName();
			HashSet<String> uses = node.getUses();
			HashSet<String> usesAndModifies = FunctionUtil.getIntersection(modifies, uses);
			
			//System.out.println(node + " uses " + uses);
			
			// If uses contains only x it's fine
			if (usesAndModifies.size() == 1 && usesAndModifies.contains(varName)) {
				//System.out.println(node + " uses and modifies " + usesAndModifies);
				
				// Check the expression has only x and has a PLUS or MINUS operator on it
				expectX = true;
				if (analyzeExpr(varName, ((AssignStmt) node).getRhs()) == false) {
					//System.out.println("fail on analyze expr: " + ((AssignStmt) node).getRhs());
					return false;
				}
				Expr expr = transformExpr(varName, ((AssignStmt) node).getRhs());
				
				// Create assignment and add to stmts list
				DeclRef declRef = new DeclRef(varName);
				BinaryExpr binaryExpr = new BinaryExpr(BinaryExpr.ADD, declRef, 
						new BinaryExpr(BinaryExpr.MULTIPLY, expr, iterationsCountRef));
				stmts.add(new AssignStmt(declRef, binaryExpr));
				
				containsX.add(true);
				vars.add(varName);
				exprs.add(expr);
			} else if (usesAndModifies.size() == 0) {
				//System.out.println("constant expr" + node);
				
				// Create assignment and add to stmts list
				DeclRef declRef = new DeclRef(varName);
				stmts.add(new AssignStmt(declRef, ((AssignStmt) node).getRhs()));
				
				containsX.add(false);
				vars.add(varName);
				exprs.add(((AssignStmt) node).getRhs());
			} else {
				//System.out.println("fail" + node);
				return false;
			}
		}
	
		
		return result;
	}
	
	// x can only be in a BinaryExpr and only appear once, otherwise we should return false
	private boolean analyzeExpr(String varName, Expr expr) {
		if (expr instanceof BinaryExpr) {
			Expr exprLhs = ((BinaryExpr) expr).getLhs();
			Expr exprRhs = ((BinaryExpr) expr).getRhs();
			Expr checkExpr = null;
			int operator = ((BinaryExpr) expr).getOperator();
			
			// If LHS expression contains x
			if (exprLhs instanceof DeclRef && ((DeclRef) exprLhs).getName().equals(varName)) {
				checkExpr = exprRhs;
			}
			// If RHS expression contains x
			else if (exprRhs instanceof DeclRef && ((DeclRef) exprRhs).getName().equals(varName)) {
				checkExpr = exprLhs;
			}
			
			if (exprLhs.getUses().contains(varName) || exprRhs.getUses().contains(varName)) {
				// If x is contained in the expression subtree we can have only ADD or MINUS
				if (operator != BinaryExpr.ADD && operator != BinaryExpr.SUBTRACT) {
					//System.out.println("We were not expecting operator: " + BinaryExpr.getOperatorString(operator));
					return false;
				}
			}
			
			if (checkExpr != null) {
				//System.out.println("found x in here! " + expectX);
				if (expectX == false) {
					//System.out.println("We were not expecting x!");
					return false;
				}
				expectX = false;
				return analyzeExpr(varName, checkExpr);
			} else {
				return analyzeExpr(varName, exprLhs) && analyzeExpr(varName, exprRhs);
			}
		}
		if (expr instanceof IntLiteral) {
			return true;
		}
		if (expr instanceof UnaryExpr) {
			// We can't handle x to be part of unary expr
			boolean previous = expectX;
			expectX = false;
			boolean result = analyzeExpr(varName, ((UnaryExpr) expr).getOperand());
			expectX = previous;
			return result;
		}
		if (expr instanceof DeclRef && !((DeclRef) expr).getName().equals(varName)) {
			return true;
		}
		//System.out.println("x mentioned twice !! " + expr);
		// This is for DeclRef that should not be reached
		return false;
	}
	
	private Expr transformExpr(String varName, Expr expr) {
		// x can only be in a BinaryExpr and can only appear once
		if (expr instanceof BinaryExpr) {
			BinaryExpr binaryExpr = (BinaryExpr) expr;
			Expr exprLhs = binaryExpr.getLhs();
			Expr exprRhs = binaryExpr.getRhs();
			int operator = binaryExpr.getOperator();
			
			// If LHS expression contains x
			if (exprLhs instanceof DeclRef && ((DeclRef) exprLhs).getName().equals(varName)) {
				if (operator == BinaryExpr.ADD)
					return exprRhs;
				return new UnaryExpr(UnaryExpr.UMINUS, exprRhs);
			}
			// If RHS expression contains x
			if (exprRhs instanceof DeclRef && ((DeclRef) exprRhs).getName().equals(varName)) {
				return exprLhs;
			}
			return new BinaryExpr(operator, transformExpr(varName, exprLhs), transformExpr(varName, exprRhs));
		}
				
		return expr;
	}
}
