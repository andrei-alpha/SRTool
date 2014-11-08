package srt.tool;

import srt.ast.AssertStmt;
import srt.ast.AssignStmt;
import srt.ast.IntLiteral;

public class SMTLIBQueryBuilder {

	private ExprToSmtlibVisitor exprConverter;
	private CollectConstraintsVisitor constraints;
	private String queryString = "";

	public SMTLIBQueryBuilder(CollectConstraintsVisitor ccv) {
		this.constraints = ccv;
		this.exprConverter = new ExprToSmtlibVisitor();
	}

	public void buildQuery() {
		System.out.println("constraints: " + this.constraints);
		System.out.println("exprConverter: " + this.exprConverter);
		// If we don't have any assertion add one
		if (constraints.propertyNodes.isEmpty()) {
			constraints.propertyNodes.add(new AssertStmt(new IntLiteral(1)));
		}
		
		StringBuilder query = new StringBuilder();
		query.append("(set-logic QF_BV)\n");
		
		query.append("(define-fun tobv32 ((p Bool)) (_ BitVec 32) (ite p (_ bv1 32) (_ bv0 32)))\n");
		query.append("(define-fun tobool ((p (_ BitVec 32))) (Bool) (not (= p (_ bv0 32))))\n");
		// TODO: Define more functions above (for convenience), as needed.
		
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
			String assertionQuery = "(not (tobool " + exprConverter.visit(stmt.getCondition()) + "))";
			query.append("(assert (= " + propName(i) + " " + assertionQuery + "))\n");
		}
		query.append("(assert (or" + getAllProps() + "))\n");
		
		query.append("\n(check-sat)\n");
		query.append("(get-value (" + getAllProps() + " $Q1 $Q2 $Q3 $Q4 $Q5 i$1 i$2 start))\n");
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

}
