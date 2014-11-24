package srt.ast;

public class DeclRef extends Expr {
	private String name;
	private int index;

	public DeclRef(String name) {
		this(name, null);
		this.index = 0;
	}
	
	public DeclRef(String name, int index) {
		this(name, null);
		this.index = index;
	}
	
	public DeclRef(String name, NodeInfo nodeInfo) {
		super(nodeInfo);
		this.name = name;
		this.index = 0;
	}

	public String getName() {
		if (index == 0)
			return name;
		return name + "$" + String.valueOf(index);
	}
	
	public void setName(String newName) {
		this.name = newName;
	}
	
	public String toString() {
		return getName();
	}
}
