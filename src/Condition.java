
/* Condition class 

This class handles logic for where clause*/
public class Condition {
    String columnName;
    private OperatorType operator;
    String comparisonValue;
    boolean negation;
    public int columnOrdinal;
    public DataType dataType;

    public Condition(DataType dataType) {
        this.dataType = dataType;
    }

    public static String[] supportedOperators = { "<=", ">=", "<>", ">", "<", "=" };

    // Converts the operator string from the user input to OperatorType
    public static OperatorType getOperatorType(String strOperator) {
        switch (strOperator) {
        case ">":
            return OperatorType.GREATERTHAN;
        case "<":
            return OperatorType.LESSTHAN;
        case "=":
            return OperatorType.EQUALTO;
        case ">=":
            return OperatorType.GREATERTHANOREQUAL;
        case "<=":
            return OperatorType.LESSTHANOREQUAL;
        case "<>":
            return OperatorType.NOTEQUAL;
        default:
            System.out.println("! Invalid operator \"" + strOperator + "\"");
            return OperatorType.INVALID;
        }
    }

    public static int compare(String value1, String value2, DataType dType) {
        if (dType == DataType.TEXT)
            return value1.toLowerCase().compareTo(value2);
        else if (dType == DataType.NULL) {
            if (value1.equals(value2))
                return 0;
            else if (value1.toLowerCase().equals("null"))
                return -1;
            else
                return 1;
        } else {
            return Long.valueOf(Long.parseLong(value1) - Long.parseLong(value2)).intValue();
        }
    }

    private boolean doOperationOnDifference(OperatorType operation,int difference)
    {
        switch (operation) {
            case LESSTHANOREQUAL:
            return difference <= 0;
        case GREATERTHANOREQUAL:
            return difference >= 0;
        case NOTEQUAL:
            return difference != 0;
        case LESSTHAN:
            return difference < 0;
        case GREATERTHAN:
            return difference > 0;
        case EQUALTO:
            return difference == 0;
        default:
            return false;
        }
    }

    private boolean doStringCompare(String currentValue, OperatorType operation) {
        return doOperationOnDifference(operation,currentValue.toLowerCase().compareTo(comparisonValue));
    }

    // Does comparison on currentvalue with the comparison value
    public boolean checkCondition(String currentValue) {
        OperatorType operation = getOperation();

        if(currentValue.toLowerCase().equals("null")
        || comparisonValue.toLowerCase().equals("null"))
            return doOperationOnDifference(operation,compare(currentValue,comparisonValue,DataType.NULL));

        if (dataType == DataType.TEXT || dataType == DataType.NULL)
            return doStringCompare(currentValue, operation);
        else {

            switch (operation) {
            case LESSTHANOREQUAL:
                return Long.parseLong(currentValue) <= Long.parseLong(comparisonValue);
            case GREATERTHANOREQUAL:
                return Long.parseLong(currentValue) >= Long.parseLong(comparisonValue);

            case NOTEQUAL:
                return Long.parseLong(currentValue) != Long.parseLong(comparisonValue);
            case LESSTHAN:
                return Long.parseLong(currentValue) < Long.parseLong(comparisonValue);

            case GREATERTHAN:
                return Long.parseLong(currentValue) > Long.parseLong(comparisonValue);
            case EQUALTO:
                return Long.parseLong(currentValue) == Long.parseLong(comparisonValue);

            default:
                return false;

            }

        }

    }

    public void setConditionValue(String conditionValue) {
        this.comparisonValue = conditionValue;
        this.comparisonValue = comparisonValue.replace("'", "");
        this.comparisonValue = comparisonValue.replace("\"", "");

    }

    public void setColumName(String columnName) {
        this.columnName = columnName;
    }

    public void setOperator(String operator) {
        this.operator = getOperatorType(operator);
    }

    public void setNegation(boolean negate) {
        this.negation = negate;
    }

    public OperatorType getOperation() {
        if (!negation)
            return this.operator;
        else
            return negateOperator();
    }

    // In case of NOT operator, invert the operator
    private OperatorType negateOperator() {
        switch (this.operator) {
        case LESSTHANOREQUAL:
            return OperatorType.GREATERTHAN;
        case GREATERTHANOREQUAL:
            return OperatorType.LESSTHAN;
        case NOTEQUAL:
            return OperatorType.EQUALTO;
        case LESSTHAN:
            return OperatorType.GREATERTHANOREQUAL;
        case GREATERTHAN:
            return OperatorType.LESSTHANOREQUAL;
        case EQUALTO:
            return OperatorType.NOTEQUAL;
        default:
            System.out.println("! Invalid operator \"" + this.operator + "\"");
            return OperatorType.INVALID;
        }
    }
}
