import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.SortedMap;

import java.util.ArrayList;
import java.lang.Math.*;
import java.util.Arrays;
import static java.lang.System.out;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Team Yellow
 * @version 1.0
 */
public class DavisBaseBinaryFile {

   public static String columnsTable = "davisbase_columns";
   public static String tablesTable = "davisbase_tables";
   public static boolean showRowId = false;
   public static boolean dataStoreInitialized = false;

   /* This static variable controls page size. */
   static int pageSizePower = 9;
   /* This strategy insures that the page size is always a power of 2. */
   static int pageSize = (int) Math.pow(2, pageSizePower);

   RandomAccessFile file;

   public DavisBaseBinaryFile(RandomAccessFile file) {
      this.file = file;
   }

   public boolean recordExists(TableMetaData tablemetaData, List<String> columNames, Condition condition) throws IOException{

   BPlusOneTree bPlusOneTree = new BPlusOneTree(file, tablemetaData.rootPageNo, tablemetaData.tableName);

   
   for(Integer pageNo :  bPlusOneTree.getAllLeaves(condition))
   {
         Page page = new Page(file,pageNo);
         for(TableRecord record : page.getPageRecords())
         {
            if(condition!=null)
            {
               if(!condition.checkCondition(record.getAttributes().get(condition.columnOrdinal).fieldValue))
                  continue;
            }
           return true;
         }
   }
   return false;

   }


   
   public int updateRecords(TableMetaData tablemetaData,Condition condition, 
                  List<String> columNames, List<String> newValues) throws IOException
   {
      int count = 0;


      List<Integer> ordinalPostions = tablemetaData.getOrdinalPostions(columNames);

      //map new values to column ordinal position
      int k=0;
      Map<Integer,Attribute> newValueMap = new HashMap<>();

      for(String strnewValue:newValues){
           int index = ordinalPostions.get(k);

         try{
                newValueMap.put(index,
                      new Attribute(tablemetaData.columnNameAttrs.get(index).dataType,strnewValue));
                      }
                      catch (Exception e) {
							System.out.println("! Invalid data format for " + tablemetaData.columnNames.get(index) + " values: "
									+ strnewValue);
							return count;
						}

         k++;
      }

      BPlusOneTree bPlusOneTree = new BPlusOneTree(file, tablemetaData.rootPageNo,tablemetaData.tableName);
      
            List<Integer> updateRowids = new ArrayList<Integer>();
      int conditionalColumnIndex = -1;
      for(Integer pageNo :  bPlusOneTree.getAllLeaves(condition))
      {
            short deleteCountPerPage = 0;
            Page page = new Page(file,pageNo);
            for(TableRecord record : page.getPageRecords())
            {
               if(condition!=null)
               {
                  if(!condition.checkCondition(record.getAttributes().get(condition.columnOrdinal).fieldValue))
                     continue;
               }
               if(!updateRowids.contains(record.rowId))
                 count++;
                 
                List<Attribute> attrs = record.getAttributes();
               for(int i :newValueMap.keySet())
               {
                  Attribute oldValue = record.getAttributes().get(i);
                  int rowId = record.rowId;
                  if((record.getAttributes().get(i).dataType == DataType.TEXT
                   && record.getAttributes().get(i).fieldValue.length() == newValueMap.get(i).fieldValue.length())
                     || (record.getAttributes().get(i).dataType != DataType.NULL && record.getAttributes().get(i).dataType != DataType.TEXT)
                  ){
                     page.updateRecord(record,i,newValueMap.get(i).fieldValueByte);
                     Attribute attr = attrs.get(i);
                     attrs.remove(i);
                     attr = newValueMap.get(i);
                     attrs.add(i, attr);
                  }
                  else{
                   //Delete the record and insert a new one, update indexes
                     if(condition!=null)
                          conditionalColumnIndex = condition.columnOrdinal;
                          
                     page.DeleteTableRecord(tablemetaData.tableName ,
                     Integer.valueOf(record.pageHeaderIndex - deleteCountPerPage).shortValue());
                     deleteCountPerPage++;
                     
                     Attribute attr = attrs.get(i);
                     attrs.remove(i);
                     attr = newValueMap.get(i);
                     attrs.add(i, attr);
                     int pageNoToinsert = BPlusOneTree.getPageNoForInsert(file,tablemetaData.rootPageNo);
                     Page pageToInsert = new Page(file,pageNoToinsert);
                     rowId =  pageToInsert.addTableRow(tablemetaData.tableName , attrs);
                     updateRowids.add(rowId);
                }
                
                if(tablemetaData.columnNameAttrs.get(i).hasIndex && condition!=null
                   ){
                  RandomAccessFile indexFile = new RandomAccessFile(DavisBasePrompt.getNDXFilePath(tablemetaData.columnNameAttrs.get(i).tableName, tablemetaData.columnNameAttrs.get(i).columnName), "rw");
                  BTree bTree = new BTree(indexFile);
                  bTree.delete(oldValue,record.rowId);
                  bTree.insert(newValueMap.get(i), rowId);
                  indexFile.close();
                }
                if(conditionalColumnIndex !=-1 && tablemetaData.columnNameAttrs.get(conditionalColumnIndex).hasIndex )
                {
                  RandomAccessFile indexFile = new RandomAccessFile(DavisBasePrompt.getNDXFilePath(tablemetaData.columnNameAttrs.get(conditionalColumnIndex).tableName, tablemetaData.columnNameAttrs.get(conditionalColumnIndex).columnName), "rw");
                  BTree bTree = new BTree(indexFile);
                  bTree.delete(record.getAttributes().get(conditionalColumnIndex),record.rowId);
                  bTree.insert(record.getAttributes().get(conditionalColumnIndex), rowId);
                  indexFile.close();
                }
                
               }
             }
      }
    
      if(!tablemetaData.tableName.equals(tablesTable) && !tablemetaData.tableName.equals(columnsTable))
          System.out.println("* " + count+" record(s) updated.");
          
         return count;

   }

   public void selectRecords(TableMetaData tablemetaData, List<String> columNames, Condition condition) throws IOException{
   
   //The select order might be different from the table ordinal position
   List<Integer> ordinalPostions = tablemetaData.getOrdinalPostions(columNames);

   System.out.println();
   
   List<Integer> printPosition = new ArrayList<>();
   
   int columnPrintLength = 0;
   printPosition.add(columnPrintLength);
   int totalTablePrintLength =0;
   if(showRowId)
   {
      System.out.print("rowid");
      System.out.print(DavisBasePrompt.line(" ",5));
      printPosition.add(10);
      totalTablePrintLength +=10;
   }

   
   for(int i:ordinalPostions)
   {
      String columnName = tablemetaData.columnNameAttrs.get(i).columnName;
      columnPrintLength = Math.max(columnName.length()
                                 ,tablemetaData.columnNameAttrs.get(i).dataType.getPrintOffset()) + 5;
      printPosition.add(columnPrintLength);
      System.out.print(columnName);
      System.out.print(DavisBasePrompt.line(" ",columnPrintLength - columnName.length() ));
      totalTablePrintLength +=columnPrintLength;
   }
       System.out.println();
       System.out.println(DavisBasePrompt.line("-",totalTablePrintLength));

   BPlusOneTree bPlusOneTree = new BPlusOneTree(file, tablemetaData.rootPageNo,tablemetaData.tableName);
  
   String currentValue ="";
   int count = 0;
   for(Integer pageNo : bPlusOneTree.getAllLeaves(condition))
   {
         Page page = new Page(file,pageNo);
         for(TableRecord record : page.getPageRecords())
         {
            if(condition!=null)
            {
               if(!condition.checkCondition(record.getAttributes().get(condition.columnOrdinal).fieldValue))
                  continue;
            }
            int columnCount = 0;
            if(showRowId)
            {
                  currentValue = Integer.valueOf(record.rowId).toString();
                  System.out.print(currentValue);
                  System.out.print(DavisBasePrompt.line(" ",printPosition.get(++columnCount) - currentValue.length()));
            }
            for(int i :ordinalPostions)
            {
               currentValue = record.getAttributes().get(i).fieldValue;
               System.out.print(currentValue);
               System.out.print(DavisBasePrompt.line(" ",printPosition.get(++columnCount) - currentValue.length()));
            }
            System.out.println();
            count++;
         }
   }
   System.out.println();
   System.out.println("* " + count + " records retrieved.");
   }
       

   // Find the root page manually
   public static int getRootPageNo(RandomAccessFile binaryfile) {
     int rootpage = 0;
      try {   
         for (int i = 0; i < binaryfile.length() / DavisBaseBinaryFile.pageSize; i++) {
            binaryfile.seek(i * DavisBaseBinaryFile.pageSize + 0x0A);
            int a =binaryfile.readInt();
          
            if (a == -1) {
               return i;
            }
         }
         return rootpage;
      } catch (Exception e) {
         out.println("error while getting root page no ");
         out.println(e);
      }
      return -1;

   }


   /**
    * This static method creates the DavisBase data storage container and then
    * initializes two .tbl files to implement the two system tables,
    * davisbase_tables and davisbase_columns
    *
    * WARNING! Calling this method will destroy the system database catalog files
    * if they already exist.
    */
   public static void initializeDataStore() {

      /** Create data directory at the current OS location to hold */
      try {
         File dataDir = new File("data");
         dataDir.mkdir();
         String[] oldTableFiles;
         oldTableFiles = dataDir.list();
         for (int i = 0; i < oldTableFiles.length; i++) {
            File anOldFile = new File(dataDir, oldTableFiles[i]);
            anOldFile.delete();
         }
      } catch (SecurityException se) {
         out.println("Unable to create data container directory");
         out.println(se);
      }

      /** Create davisbase_tables system catalog */
      try {
         
         int currentPageNo = 0;

         RandomAccessFile davisbaseTablesCatalog = new RandomAccessFile(
               DavisBasePrompt.getTBLFilePath(tablesTable), "rw");
         Page.addNewPage(davisbaseTablesCatalog, PageType.LEAF, -1, -1);
         Page page = new Page(davisbaseTablesCatalog,currentPageNo);

         page.addTableRow(tablesTable,Arrays.asList(new Attribute[] { 
               new Attribute(DataType.TEXT, DavisBaseBinaryFile.tablesTable),
               new Attribute(DataType.INT, "2"), 
               new Attribute(DataType.SMALLINT, "0"),
               new Attribute(DataType.SMALLINT, "0") 
               }));

         page.addTableRow(tablesTable,Arrays.asList(new Attribute[] {
               new Attribute(DataType.TEXT, DavisBaseBinaryFile.columnsTable),
                new Attribute(DataType.INT, "11"),
               new Attribute(DataType.SMALLINT, "0"),
                new Attribute(DataType.SMALLINT, "2") }));

         davisbaseTablesCatalog.close();
      } catch (Exception e) {
         out.println("Unable to create the database_tables file");
         out.println(e);
         

      }

      /** Create davisbase_columns systems catalog */
      try {
         RandomAccessFile davisbaseColumnsCatalog = new RandomAccessFile(
            DavisBasePrompt.getTBLFilePath(columnsTable), "rw");
         Page.addNewPage(davisbaseColumnsCatalog, PageType.LEAF, -1, -1);
         Page page = new Page(davisbaseColumnsCatalog, 0);

         short ordinal_position = 1;

         //Add new columns to davisbase_tables
         page.addNewColumn(new ColumnInfo(tablesTable,DataType.TEXT, "table_name", true, false, ordinal_position++));
         page.addNewColumn(new ColumnInfo(tablesTable,DataType.INT, "record_count", false, false, ordinal_position++));
         page.addNewColumn(new ColumnInfo(tablesTable,DataType.SMALLINT, "avg_length", false, false, ordinal_position++));
         page.addNewColumn(new ColumnInfo(tablesTable,DataType.SMALLINT, "root_page", false, false, ordinal_position++));
      
       

         //Add new columns to davisbase_columns

         ordinal_position = 1;

         page.addNewColumn(new ColumnInfo(columnsTable,DataType.TEXT, "table_name", false, false, ordinal_position++));
         page.addNewColumn(new ColumnInfo(columnsTable,DataType.TEXT, "column_name", false, false, ordinal_position++));
         page.addNewColumn(new ColumnInfo(columnsTable,DataType.SMALLINT, "data_type", false, false, ordinal_position++));
         page.addNewColumn(new ColumnInfo(columnsTable,DataType.SMALLINT, "ordinal_position", false, false, ordinal_position++));
         page.addNewColumn(new ColumnInfo(columnsTable,DataType.TEXT, "is_nullable", false, false, ordinal_position++));
         page.addNewColumn(new ColumnInfo(columnsTable,DataType.SMALLINT, "column_key", false, true, ordinal_position++));
         page.addNewColumn(new ColumnInfo(columnsTable,DataType.SMALLINT, "is_unique", false, false, ordinal_position++));

         davisbaseColumnsCatalog.close();
         dataStoreInitialized = true;
      } catch (Exception e) {
         out.println("Unable to create the database_columns file");
         out.println(e);
      }
   }
}


