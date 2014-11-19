package srt.ast;

import srt.parser.SimpleCLexer;

public class UnaryExpr extends Expr {
	private int operator;

	public UnaryExpr(int operator, Expr operand) {
		this(operator, operand, null);
	}
	
	public UnaryExpr(int operator, Expr operand, NodeInfo nodeInfo)
	{
		super(nodeInfo);
		this.operator = operator;
		children.add(operand);
	}

	public int getOperator() {
		return operator;
	}
	
	public Expr getOperand()
	{
		return (Expr)children.get(0);
	}
	
	public static final int UMINUS=SimpleCLexer.UMINUS;
	public static final int UPLUS=SimpleCLexer.UPLUS;
	public static final int LNOT=SimpleCLexer.LNOT;
	public static final int BNOT=SimpleCLexer.BNOT;
	
	public static String getOperatorString(int operator)
	{
		switch(operator)
		{
			case UMINUS:
				return "-";
			case UPLUS:
				return "+";
			case LNOT:
				return "!";
			case BNOT:
				return "~";
			default:
		}
		throw new IllegalArgumentException("Invalid binary operator");
	}
	
	public int calculate() {
		if (!(getOperand() instanceof IntLiteral))
			throw new IllegalArgumentException("Must be used only on int values");
		int val = ((IntLiteral) getOperand()).getValue();
		
		switch(operator)
		{
			case UMINUS:
				return -val;
			case UPLUS:
				return +val;
			case LNOT:
				return (val == 0 ? 1 : 0);
			case BNOT:
				return ~val;
			default:
		}
		throw new IllegalArgumentException("Invalid binary operator");
	}
	
	public String toString() {
		return getOperatorString(operator) + this.getOperand();
	}
}
