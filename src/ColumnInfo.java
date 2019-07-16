/* Class to denote column name and datatype  of table metadata */
public class ColumnInfo
{
    public DataType dataType;
    
    public String columnName;

    ColumnInfo(DataType dataType,String columnName){
        this.dataType = dataType;
        this.columnName = columnName;
    }
}