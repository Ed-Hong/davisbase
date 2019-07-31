import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
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
    public int lastRowId;

    public TableMetaData(String tableName)
    {
        this.tableName = tableName;
        tableExists = false;
        try {

            RandomAccessFile davisbaseTablesCatalog = new RandomAccessFile(
                DavisBasePrompt.getTBLFilePath(DavisBaseBinaryFile.tablesTable), "r");
            
            //get the root page of the table
            int rootPageNo = DavisBaseBinaryFile.getRootPageNo(davisbaseTablesCatalog);
           
            BPlusOneTree bplusOneTree = new BPlusOneTree(davisbaseTablesCatalog, rootPageNo,tableName);
            //search through all leaf papges in davisbase_tables
            for (Integer pageNo : bplusOneTree.getAllLeaves()) {
               Page page = new Page(davisbaseTablesCatalog, pageNo);
               //search theough all the records in each page
               for (TableRecord record : page.getPageRecords()) {
                   //if the record with table is found, get the root page No and record count; break the loop
                  if (new String(record.getAttributes().get(0).fieldValue).equals(tableName)) {
                    this.rootPageNo = Integer.parseInt(record.getAttributes().get(3).fieldValue);
                    recordCount = Integer.parseInt(record.getAttributes().get(1).fieldValue);
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
            } else {
               throw new Exception("Table does not exist.");
            }
            
         } catch (Exception e) {
           // System.out.println("! Error while checking Table " + tableName + " exists.");
            //debug: System.out.println(e);
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
            DavisBasePrompt.getTBLFilePath(DavisBaseBinaryFile.columnsTable), "r");
           int rootPageNo = DavisBaseBinaryFile.getRootPageNo(davisbaseColumnsCatalog);
  
           columnData = new ArrayList<>();
           columnNameAttrs = new ArrayList<>();
           columnNames = new ArrayList<>();
           BPlusOneTree bPlusOneTree = new BPlusOneTree(davisbaseColumnsCatalog, rootPageNo,tableName);
         
           /* Get all columns from the davisbase_columns, loop through all the leaf pages 
           and find the records with the table name */
           for (Integer pageNo : bPlusOneTree.getAllLeaves()) {
           
             Page page = new Page(davisbaseColumnsCatalog, pageNo);
              
              for (TableRecord record : page.getPageRecords()) {
                  
                 if (record.getAttributes().get(0).fieldValue.equals(tableName)) {
                    {
                     //set column information in the data members of the class
                       columnData.add(record);
                       columnNames.add(record.getAttributes().get(1).fieldValue);
                       ColumnInfo colInfo = new ColumnInfo(
                                          tableName  
                                        , DataType.get(record.getAttributes().get(2).fieldValue)
                                        , record.getAttributes().get(1).fieldValue
                                        , record.getAttributes().get(6).fieldValue.equals("YES")
                                        , record.getAttributes().get(4).fieldValue.equals("YES")
                                        , Short.parseShort(record.getAttributes().get(3).fieldValue)
                                        );
                                          
                    if(record.getAttributes().get(5).fieldValue.equals("PRI"))
                          colInfo.setAsPrimaryKey();
                        
                     columnNameAttrs.add(colInfo);

                      
                    }
                 }
              }
           }
  
           davisbaseColumnsCatalog.close();
        } catch (Exception e) {
           System.out.println("! Error while getting column data for " + tableName);
           //debug: System.out.println(e);
        }
  
     }

     // Method to check if the columns exists for the table
   public boolean columnExists(List<String> columns) {

    if(columns.size() == 0)
       return true;
       
       List<String> lColumns =new ArrayList<>(columns);

    for (ColumnInfo column_name_attr : columnNameAttrs) {
       if (lColumns.contains(column_name_attr.columnName))
          lColumns.remove(column_name_attr.columnName);
    }

    return lColumns.isEmpty();
 }



 public void updateMetaData()
 {

   //update root page in the tables catalog
   try{
      RandomAccessFile tableFile = new RandomAccessFile(
         DavisBasePrompt.getTBLFilePath(tableName), "r");
   
         Integer rootPageNo = DavisBaseBinaryFile.getRootPageNo(tableFile);
         tableFile.close();
          
         
         RandomAccessFile davisbaseTablesCatalog = new RandomAccessFile(
                      DavisBasePrompt.getTBLFilePath(DavisBaseBinaryFile.tablesTable), "rw");
       
         DavisBaseBinaryFile tablesBinaryFile = new DavisBaseBinaryFile(davisbaseTablesCatalog);

         TableMetaData tablesMetaData = new TableMetaData(DavisBaseBinaryFile.tablesTable);
         
         Condition condition = new Condition(DataType.TEXT);
         condition.setColumName("table_name");
         condition.columnOrdinal = 0;
         condition.setConditionValue(tableName);
         condition.setOperator("=");

         List<String> columns = Arrays.asList("record_count","root_page");
         List<String> newValues = new ArrayList<>();

         newValues.add(new Integer(recordCount).toString());
         newValues.add(new Integer(rootPageNo).toString());

         tablesBinaryFile.updateRecords(tablesMetaData,condition,columns,newValues);
                                              
       davisbaseTablesCatalog.close();
   }
   catch(IOException e){
      System.out.println("! Error updating meta data for " + tableName);
   }

   
 }

 public boolean validateInsert(List<Attribute> row) throws IOException
 {
  RandomAccessFile tableFile = new RandomAccessFile(DavisBasePrompt.getTBLFilePath(tableName), "r");
  DavisBaseBinaryFile file = new DavisBaseBinaryFile(tableFile);
         
     
     for(int i=0;i<columnNameAttrs.size();i++)
     {
     
        Condition condition = new Condition(columnNameAttrs.get(i).dataType);
         condition.columnName = columnNameAttrs.get(i).columnName;
         condition.columnOrdinal = i;
         condition.setOperator("=");

        if(columnNameAttrs.get(i).isUnique)
        {
         condition.setConditionValue(row.get(i).fieldValue);
            if(file.recordExists(this, Arrays.asList(columnNameAttrs.get(i).columnName), condition)){
          System.out.println("! Insert failed: Column "+ columnNameAttrs.get(i).columnName + " should be unique." );
               tableFile.close();
            return false;
        }

      
         }
      

  
     }
 tableFile.close();
     return true;
 }

}