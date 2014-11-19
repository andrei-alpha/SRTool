package srt.ast.visitor.impl;

import java.util.List;

import srt.ast.AssignStmt;
import srt.ast.DeclRef;
import srt.ast.Node;

public class ReverseVisitor extends DefaultVisitor {
	
	protected boolean stopVisitingChildren = false;
	private boolean doesModify;
	
	public ReverseVisitor(boolean doesModify) {
		super(doesModify);
		this.doesModify = doesModify;
	}
	
	public Object visitChildren(Node node)
	{
		List<Node> children = node.getChildrenCopy();
		boolean modifiedChildren = false;
		for(int i=children.size()-1; !stopVisitingChildren && i >= 0; --i)
		{
			Node child = children.get(i);
			if(child != null)
			{
				Object res = visit(child);
				if(doesModify && res != child) {
					children.set(i, (Node) res);
					modifiedChildren = true;
				}
				if (res == null) {
					children.remove(i);
				}
			}
		}
		
		// Compute the modifies set for any node, declref and assign are special cases
		node.resetVars();
		if (node instanceof DeclRef) {
			node.addUsesVar(((DeclRef) node).getName());
		}
		if (node instanceof AssignStmt) {
			node.addModifiesVar(((AssignStmt) node).getLhs().getName());
		}
		
		for(int i=children.size()-1; !stopVisitingChildren && i >= 0; --i) {
			Node child = children.get(i);
			if (child != null) {
				node.setLoopCount(Math.max(node.getLoopCount(), child.getLoopCount()) );
				node.addAllModifies(child.getModifies());
				// Special case for assign, don't add to use set LHS
				if (!(node instanceof AssignStmt && i == 0))
					node.addAllUses(child.getUses());
				if (child.hasAsserts())
					node.setAsserts();
			}
		}
		
		if(modifiedChildren) {
			return node.withNewChildren(children);
		}
		
		return node;
	}
}
