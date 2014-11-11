package srt.tool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import srt.ast.AssertStmt;
import srt.ast.AssignStmt;
import srt.ast.DeclRef;
import srt.ast.visitor.impl.DefaultVisitor;

public class CollectConstraintsVisitor extends DefaultVisitor {
	
	public Set<String> variableNames = new HashSet<String>();
	public List<AssignStmt> transitionNodes = new ArrayList<AssignStmt>();
	public List<AssertStmt> propertyNodes = new ArrayList<AssertStmt>();
	
	
	public CollectConstraintsVisitor() {
		super(false);
	}
	
	@Override
	public Object visit(DeclRef declRef) {
		variableNames.add(declRef.getName());
		return super.visit(declRef);
	}

	@Override
	public Object visit(AssertStmt assertStmt) {
		propertyNodes.add(assertStmt);
		return super.visit(assertStmt);
	}

	@Override
	public Object visit(AssignStmt assignment) {
		transitionNodes.add(assignment);
		return super.visit(assignment);
	}
	
	@Override
	public String toString() {
		System.out.print("\nvariableNames: ");
		for (String var : variableNames) {
			System.out.print(var + ", ");
		}
		System.out.print("\ntransitionNodes: ");
		for (AssignStmt var : transitionNodes) {
			System.out.print(var + ", ");
		}
		System.out.print("\npropertyNodes: ");
		for (AssertStmt var : propertyNodes) {
			System.out.print(var + ", ");
		}
		return "";
	}
	
}




