package srt.tool;

import java.util.ArrayList;

import srt.ast.AssertStmt;
import srt.ast.AssignStmt;
import srt.ast.Expr;
import srt.ast.IntLiteral;

public class SMTLIBQueryBuilder {

	private ExprToSmtlibVisitor exprConverter;
	private CollectConstraintsVisitor constraints;
	private String queryString = "";
	private ArrayList<String> unwindingConditions;
	private ArrayList<Integer> houdiniConditions;
	
	public SMTLIBQueryBuilder(CollectConstraintsVisitor ccv) {
		constraints = ccv;
		exprConverter = new ExprToSmtlibVisitor();
		unwindingConditions = new ArrayList<String>();
		houdiniConditions = new ArrayList<Integer>();
	}

	public void buildQuery() {
		// If we don't have any assertion add one
		if (constraints.propertyNodes.isEmpty()) {
			constraints.propertyNodes.add(new AssertStmt(new IntLiteral(1)));
		}
		
		StringBuilder query = new StringBuilder();
		query.append("(set-logic QF_BV)\n");
		
		query.append("(define-fun tobv32 ((p Bool)) (_ BitVec 32) (ite p (_ bv1 32) (_ bv0 32)))\n");
		query.append("(define-fun tobool ((p (_ BitVec 32))) Bool (not (= p (_ bv0 32))))\n");
		// Defined more functions above (for convenience), as needed.
		
		for (String varName : constraints.variableNames) {
			query.append("(declare-fun " + varName + " () (_ BitVec 32))\n");
		}
		for (int i = 0; i < constraints.propertyNodes.size(); ++i) {
			query.append("(declare-fun " + propName(i) + " () Bool)\n");
		}
		for (AssignStmt stmt : constraints.transitionNodes) {
			query.append("(assert (= " + stmt.getLhs().getName() + " " +
					exprConverter.visit(stmt.getRhs()) + "))\n");
		}
		for (int i = 0; i < constraints.propertyNodes.size(); ++i) {
			AssertStmt stmt = constraints.propertyNodes.get(i);
			if (stmt.isHoudini())
				houdiniConditions.add(i);
			if (stmt.isUnwinding())
				unwindingConditions.add(propName(i));
			
 			String assertionQuery = "(not (tobool " + exprConverter.visit(stmt.getCondition()) + "))";
			query.append("(assert (= " + propName(i) + " " + assertionQuery + "))\n");
		}
		query.append("(assert (or" + getAllProps() + "))\n");
		
		query.append("\n(check-sat)\n");
		// append all unwinding conditions
		query.append("(get-value (" + getAllProps() + " ))\n");
		
		queryString = query.toString();
	}

	private String propName(int index) {
		return "prop" + String.valueOf(index);
	}
	
	private String getAllProps() {
		String props = "";
		for (int i = 0; i < constraints.propertyNodes.size(); ++i) {
			props += " " + propName(i);
		}
		return props;
	}
	
	public String getQuery() {
		return queryString;
	}

	public boolean isUnwindingFailure(String queryResult) {
		for (String condition : unwindingConditions) {
			if (queryResult.contains(condition + " true"))
				return true;
		}
		return false;
	}
	
	public ArrayList<Expr> getFailedHoudini(String queryResult) {
		ArrayList<Expr> stmts = new ArrayList<Expr>();
		
		for (Integer conditionIndex : houdiniConditions) {
			String condition = propName(conditionIndex);
			if (queryResult.contains(condition + " true")) {
				stmts.add(constraints.propertyNodes.get(conditionIndex).getHoudiniInvariant());
			}
		}
		
		return stmts;
	}
}
