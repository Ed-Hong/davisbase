
/* Class to denote column name and datatype  of table metadata */
public class ColumnInfo
{
    public DataType dataType;
    
    public String columnName;

    public boolean isUnique;
    public boolean isNullable;
    public Short ordinalPosition;


    public boolean isPrimaryKey;

    ColumnInfo(){
        
    }
    ColumnInfo(DataType dataType,String columnName,boolean isUnique,boolean isNullable,short ordinalPosition){
        this.dataType = dataType;
        this.columnName = columnName;
        this.isUnique = isUnique;
        this.isNullable = isNullable;
        this.ordinalPosition = ordinalPosition;
    }

    

    public void setAsPrimaryKey()
    {
        isPrimaryKey = true;
    }
}