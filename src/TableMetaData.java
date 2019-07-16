import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.RandomAccessFile;
import java.math.BigInteger;

/* This class is used to read and manipulate the meta data of the table (davisbase_tables and davisbas_columns)
    we need to make sure to update the meta data
        - record_count
        - root page No
     of the table whenever a records gets inserted or deleted
       
*/
public class TableMetaData{

    public int recordCount;
    public List<TableRecord> columnData;
    public List<ColumnInfo> columnNameAttrs;
    public List<String> columnNames;
    public String tableName;
    public boolean tableExists;
    public int rootPageNo;

    public TableMetaData(String tableName)
    {
        this.tableName = tableName;
        tableExists = false;
        try {

            RandomAccessFile davisbaseTablesCatalog = new RandomAccessFile(
                DavisBaseBinaryFile.getDataFilePath(DavisBaseBinaryFile.tablesTable), "r");
            
            //get the root page of the table
            int rootPageNo = DavisBaseBinaryFile.getRootPageNo(davisbaseTablesCatalog);
           
            BPlusOneTree bplusOneTree = new BPlusOneTree(davisbaseTablesCatalog, rootPageNo);
            //search through all leaf papges in davisbase_tables
            for (Integer pageNo : bplusOneTree.getAllLeaves()) {
               Page page = new Page(davisbaseTablesCatalog, pageNo);
               //search theough all the records in each page
               for (TableRecord record : page.records) {
                   //if the record with table is found, get the root page No and record count; break the loop
                  if (new String(record.getAttributes().get(0).fieldValue).equals(tableName)) {
                    rootPageNo = Integer.parseInt(record.getAttributes().get(3).fieldValue);
                    recordCount = Integer.parseInt(record.getAttributes().get(2).fieldValue);
                    tableExists = true;
                     break;
                  }
               }
               if(tableExists)
                break;
            }
   
            davisbaseTablesCatalog.close();
            if(tableExists)
            {
               loadColumnData();
            }
            
         } catch (Exception e) {
            System.out.println("error while checking Table Exists " + tableName);
              System.out.println(e);
         }
    }

    public List<Integer> getOrdinalPostions(List<String> columns){
				List<Integer> ordinalPostions = new ArrayList<>();
				for(String column :columns)
				{
					ordinalPostions.add(columnNames.indexOf(column));
                }
                return ordinalPostions;
    }

    //loads the column information for thr table
    private void loadColumnData() {
        try {
  
           RandomAccessFile davisbaseColumnsCatalog = new RandomAccessFile(
            DavisBaseBinaryFile.getDataFilePath(DavisBaseBinaryFile.columnsTable), "r");
           int rootPageNo = DavisBaseBinaryFile.getRootPageNo(davisbaseColumnsCatalog);
  
           columnData = new ArrayList<>();
           columnNameAttrs = new ArrayList<>();
           columnNames = new ArrayList<>();
           BPlusOneTree bPlusOneTree = new BPlusOneTree(davisbaseColumnsCatalog, rootPageNo);
         
           /* Get all columns from the davisbase_columns, loop through all the leaf pages 
           and find the records with the table name */
           for (Integer pageNo : bPlusOneTree.getAllLeaves()) {
           
             Page page = new Page(davisbaseColumnsCatalog, pageNo);
              
              for (TableRecord record : page.records) {
                  
                 if (record.getAttributes().get(0).fieldValue.equals(tableName)) {
                    {
                     //set column information in the data members of the class
                       columnData.add(record);
                       columnNames.add(record.getAttributes().get(1).fieldValue);
                       columnNameAttrs.add(new ColumnInfo(DataType.get(record.getAttributes().get(2).fieldValue)
                                        , record.getAttributes().get(1).fieldValue));
                    }
                 }
              }
           }
  
           davisbaseColumnsCatalog.close();
        } catch (Exception e) {
            System.out.println("error while getting column data for " + tableName);
           System.out.println(e);
        }
  
     }

     // Method to check if the columns exists for the table in davisbase_columns
   public boolean columnExists(String tableName, List<String> columns) {

    if(columns.size() == 0)
       return true;
       
       List<String> lColumns =new ArrayList<>(columns);

    for (ColumnInfo column_name_attr : columnNameAttrs) {
       if (lColumns.contains(column_name_attr.columnName))
          lColumns.remove(column_name_attr.columnName);
    }

    return lColumns.isEmpty();
 }
}