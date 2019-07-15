public class Condition
{
    public String columnName;
    OperatorType operator;
    public String value;
    boolean negation;
    
    public Condition(String columnName, OperatorType operator, String value){
        this.columnName = columnName;
        this.operator = operator;
        this.value = value;
        this.negation = false;
    }
    public static OperatorType getOperatorType(String strOperator){
        switch(strOperator){
            case ">": return OperatorType.GREATERTHAN;
            case "<": return OperatorType.LESSTHAN;
            case "=": return OperatorType.EQUALTO;
            case ">=": return OperatorType.GREATERTHANOREQUAL;
            case "<=": return OperatorType.LESSTHANOREQUAL;
            case "<>": return OperatorType.NOTEQUAL;
            default:
                System.out.println("Invalid operator \"" + strOperator + "\"");
            return Operator.INVALID;
        }
    }

    public void setOperator(String operator){
        this.operator = operator;
    }

    public void setNegation(boolean negate){
        this.negation = negate;
    }

    public OperatorType getOperator(){
        if(!negation) return this.operator;
        else return negateOperator();
    }

    private OperatorType negateOperator(){
        switch(this.operator){
            case LESSTHANOREQUAL: return OperatorType.GREATERTHAN;
            case GREATERTHANOREQUAL: return OperatorType.LESSTHAN;
            case NOTEQUAL: return OperatorType.EQUALTO;
            case LESSTHAN: return OperatorType.GREATERTHANOREQUAL;
            case GREATERTHAN: return OperatorType.LESSTHANOREQUAL;
            case EQUALTO: return OperatorType.NOTEQUAL;
            default:
                System.out.println("Invalid operator \"" + strOperator + "\"");
            return Operator.INVALID;
        }
    }
}

