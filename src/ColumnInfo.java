import java.io.File;

/* Class to denote column name and datatype  of table metadata */
public class ColumnInfo
{
    public DataType dataType;
    
    public String columnName;

    public boolean isUnique;
    public boolean isNullable;
    public Short ordinalPosition;
    public boolean hasIndex;
    public String tableName;
    public boolean isPrimaryKey;

    ColumnInfo(){
        
    }
    ColumnInfo(String tableName,DataType dataType,String columnName,boolean isUnique,boolean isNullable,short ordinalPosition){
        this.dataType = dataType;
        this.columnName = columnName;
        this.isUnique = isUnique;
        this.isNullable = isNullable;
        this.ordinalPosition = ordinalPosition;
        this.tableName = tableName;

        this.hasIndex = (new File(DavisBasePrompt.getNDXFilePath(tableName, columnName)).exists());

    }

    

    public void setAsPrimaryKey()
    {
        isPrimaryKey = true;
    }
}