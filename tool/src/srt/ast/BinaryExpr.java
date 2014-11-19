package srt.ast;

import srt.parser.SimpleCLexer;

public class BinaryExpr extends Expr {
	
	private int operator;
	
	public BinaryExpr(int operator, Expr lhs, Expr rhs) {
		this(operator, lhs, rhs, null);
	}
	
	public BinaryExpr(int operator, Expr lhs, Expr rhs, NodeInfo nodeInfo)
	{
		super(nodeInfo);
		this.operator = operator;
		children.add(lhs);
		children.add(rhs);
	}

	public int getOperator() {
		return operator;
	}
	
	public Expr getLhs()
	{
		return (Expr)children.get(0);
	}
	
	public Expr getRhs()
	{
		return (Expr)children.get(1);
	}
	
	public static final int ADD = SimpleCLexer.ADD;
	public static final int BAND = SimpleCLexer.BAND;
	public static final int BOR = SimpleCLexer.BOR;
	public static final int BXOR = SimpleCLexer.BXOR;
	public static final int DIVIDE = SimpleCLexer.DIVIDE;
	public static final int GEQ = SimpleCLexer.GEQ;
	public static final int GT = SimpleCLexer.GT;
	public static final int LAND = SimpleCLexer.LAND;
	public static final int LEQ = SimpleCLexer.LEQ;
	public static final int LOR = SimpleCLexer.LOR;
	public static final int LSHIFT = SimpleCLexer.LSHIFT;
	public static final int LT = SimpleCLexer.LT;
	public static final int MOD = SimpleCLexer.MOD;
	public static final int MULTIPLY = SimpleCLexer.MULTIPLY;
	public static final int NEQUAL = SimpleCLexer.NEQUAL;
	public static final int EQUAL = SimpleCLexer.EQUAL;
	public static final int RSHIFT = SimpleCLexer.RSHIFT;
	public static final int SUBTRACT = SimpleCLexer.SUBTRACT;
	
	public static String getOperatorString(final int operator)
	{
		switch(operator)
		{
			case ADD:
				return "+";
			case BAND:
				return "&";
			case BOR:
				return "|";
			case BXOR:
				return "^";
			case DIVIDE:
				return "/";
			case GEQ:
				return ">=";
			case GT:
				return ">";
			case LAND:
				return "&&";
			case LEQ:
				return "<=";
			case LOR:
				return "||";
			case LSHIFT:
				return "<<";
			case LT:
				return "<";
			case MOD:
				return "%";
			case MULTIPLY:
				return "*";
			case NEQUAL:
				return "!=";
			case EQUAL:
				return "==";
			case RSHIFT:
				return ">>";
			case SUBTRACT:
				return "-";
			default:
		}
		throw new IllegalArgumentException("Invalid binary operator");
	}
	
	public static boolean isReversableOperator(final int operator) {
		switch(operator)
		{
			case GEQ:
			case GT:
			case LEQ:
			case LT:
			case NEQUAL:
			case EQUAL:
				return true;
			default:
				return false;
		}
	}
	
	public static int reverseOperator(final int operator)
	{
		switch(operator)
		{
			case GEQ:
				return LT;
			case GT:
				return LEQ;
			case LEQ:
				return GT;
			case LT:
				return GEQ;
			case NEQUAL:
				return NEQUAL;
			case EQUAL:
				return EQUAL;
			default:
		}
		throw new IllegalArgumentException("Invalid binary operator");
	}
	
	public String toString()
	{
		return "(" + this.getLhs() + " " + getOperatorString(operator) + " " + this.getRhs() + ")";
	}
	
	public int calculate() 
	{
		if (!(getLhs() instanceof IntLiteral) || !(getRhs() instanceof IntLiteral))
			throw new IllegalArgumentException("This needs to be called for ints only.");
		int lhs = ((IntLiteral) getLhs()).getValue();
		int rhs = ((IntLiteral) getRhs()).getValue();
		boolean blhs = (lhs != 0 ? true : false);
		boolean brhs = (rhs != 0 ? true : false);
		
		switch(operator)
		{
			case ADD:
				return lhs + rhs;
			case BAND:
				return lhs & rhs;
			case BOR:
				return lhs | rhs;
			case BXOR:
				return lhs ^ rhs;
			case DIVIDE:
				return lhs / rhs;
			case GEQ:
				return (lhs >= rhs ? 1 : 0);
			case GT:
				return (lhs > rhs ? 1 : 0);
			case LAND:
				return (blhs && brhs ? 1 : 0);
			case LEQ:
				return (lhs <= rhs ? 1 : 0);
			case LOR:
				return (blhs || brhs ? 1 : 0);
			case LSHIFT:
				return lhs << rhs;
			case LT:
				return (lhs < rhs ? 1 : 0);
			case MOD:
				return lhs % rhs;
			case MULTIPLY:
				return lhs * rhs;
			case NEQUAL:
				return (blhs != brhs ? 1 : 0);
			case EQUAL:
				return (blhs == brhs ? 1 : 0);
			case RSHIFT:
				return lhs >> rhs;
			case SUBTRACT:
				return lhs - rhs;
			default:
		}
		throw new IllegalArgumentException("Invalid binary operator");
	}
}
