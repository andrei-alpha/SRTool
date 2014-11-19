package srt.ast;

public class AssertStmt extends Stmt {
	private boolean isUnwinding;
	private boolean isHoudini;
	private Expr baseHoudininInvariant;
	
	public AssertStmt(Expr condition) {
		this(condition, null);
	}
	
	public AssertStmt(Expr condition, NodeInfo nodeInfo) {
		super(nodeInfo);
		children.add(condition);
		baseHoudininInvariant = null;
	}
	
	public Expr getCondition() {
		return (Expr) children.get(0);
	}
	
	public void makeUnwinding() {
		isUnwinding = true;
	}
	
	public void makeHoudini() {
		isHoudini = true;
	}
	
	public void setHoudiniInvariant(Expr inv) {
		baseHoudininInvariant = inv;
	}
	
	public Expr getHoudiniInvariant() {
		return baseHoudininInvariant;
	}
	
	public boolean isUnwinding() {
		return isUnwinding;
	}
	
	public boolean isHoudini() {
		return isHoudini;
	}
	
	@Override
	public String toString() {
		return "AssertStmt: " + this.getCondition();
	}
}
