package srt.ast;

import java.util.List;

public class InvariantList extends Node {
	
	public InvariantList(Invariant[] invars) {
		this(invars, null);
	}
	
	public InvariantList(List<Invariant> invars) {
		this(invars.toArray(new Invariant[invars.size()]), null);
	}
	
	public InvariantList(Invariant[] invars, NodeInfo nodeInfo) {
		super(nodeInfo);
		for(Invariant i : invars) {
			children.add(i);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Invariant> getInvariants() {
		return (List<Invariant>) children.clone();
	}

	public void removeAt(int index) {
		children.remove(index);		
	}
	
	public void setCandidateAt(int index, boolean value) {
		Invariant inv = (Invariant) children.get(index);
		inv.setCandidate(value);
	}
}
