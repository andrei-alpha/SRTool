package srt.ast.visitor.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import srt.ast.*;

/**
 * This class uses a Visitor to check that
 * variables are declared before use
 * and that there are no duplicate local variables
 * in the same scope.
 */
public class UniqueVisitor {
	private HashSet<String> variablesSeen;
	private Scopes scopes;
	
	public UniqueVisitor() {
		variablesSeen = new HashSet<String>();
	}
	
	private DefaultVisitor visitor = new DefaultVisitor(true) {
		@Override
		public Object visit(BlockStmt blockStmt) {
			scopes.pushScope();
			Object res = super.visit(blockStmt);
			scopes.popScope();
			return res;
		}
		
		@Override
		public Object visit(Decl decl) {
			String initName = decl.getName();
			while (true) {
				String name = decl.getName();
				if (variablesSeen.contains(name)) {
					scopes.renameVariable(initName, name);
					String newName = scopes.getName(initName);
					decl.setName(newName);
				} else {
					break;
				}
			}
			variablesSeen.add(decl.getName());
			return super.visit(decl);
		}
		
		@Override
		public Object visit(DeclRef declRef) {
			String newName = scopes.getName(declRef.getName());
			declRef.setName(newName);
			
			return super.visit(declRef);
		}
	};
	
	public Program visit(Program p) {
		scopes = new Scopes();
		visitor.visit(p);
		return p;
	}
	
	private class Scopes {
		private HashMap<String, String> variablesRename = new HashMap<String, String>();
		private List<HashMap<String, String>> scopes = new ArrayList<HashMap<String, String>>();
		
		public Scopes() {
			pushScope();
		}
		
		public void pushScope() {
			scopes.add(new HashMap<String, String>());
		}
		
		public void popScope() {
			int last = scopes.size()-1;
			HashMap<String, String> variablesLost = scopes.get(last);
			scopes.remove(last);
			for (String var : variablesLost.keySet())
				variablesRename.remove(var);
		}
		
		public void renameVariable(String initName, String name) {
			String number = name.replaceFirst("^(.*[a-zA-Z-])", "");
			int index = (number.equals("") ? 2 : Integer.valueOf(number) + 1);
			String newName = name.replaceFirst("[0-9]+$", "") + String.valueOf(index);
			
			int last = scopes.size()-1;
			variablesRename.put(initName, newName);
			scopes.get(last).put(initName, newName);
		}
		
		public String getName(String var) {
			if (variablesRename.containsKey(var))
				return variablesRename.get(var);
			return var;
		}
	}
}
