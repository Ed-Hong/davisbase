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

   public int CountOf(TableMetaData tablemetaData, List<String> columNames, Condition condition) throws IOException{

   BPlusOneTree bPlusOneTree = new BPlusOneTree(file, tablemetaData.rootPageNo);

   int rowCount = 0;
   
   for(Integer pageNo :  bPlusOneTree.getAllLeaves(condition))
   {
         Page page = new Page(file,pageNo);
         for(TableRecord record : page.records)
         {
            if(condition!=null)
            {
               if(!condition.checkCondition(record.getAttributes().get(condition.columnOrdinal).fieldValue))
                  continue;
            }
            rowCount++;
         }
   }
   return rowCount;

   }

   public void updateRecords(TableMetaData tablemetaData,Condition condition, List<String> columNames, List<Byte[]> newValues) throws IOException
   {
      int count = 0;
      List<Integer> ordinalPostions = tablemetaData.getOrdinalPostions(columNames);

      //map new values to column ordinal position
      int k=0;
      Map<Integer,Byte[]> newValueMap = new HashMap<>();

      for(Byte[] newValue:newValues){
         newValueMap.put(ordinalPostions.get(k++), newValue);
      }

      BPlusOneTree bPlusOneTree = new BPlusOneTree(file, tablemetaData.rootPageNo);
   
      for(Integer pageNo :  bPlusOneTree.getAllLeaves(condition))
      {
            Page page = new Page(file,pageNo);
            for(TableRecord record : page.records)
            {
               if(condition!=null)
               {
                  if(!condition.checkCondition(record.getAttributes().get(condition.columnOrdinal).fieldValue))
                     continue;
               }
               count++;
               for(int i :newValueMap.keySet())
               {
                  if(record.getAttributes().get(i).dataType !=DataType.TEXT || (record.getAttributes().get(i).fieldValue.length() == newValueMap.get(i).toString().length())){
                     page.updateRecord(record,i,newValueMap.get(i));
                  }
                  else{
                     //TODO delete the record and insert a new one, update indexes
                  }
                  
               }
             }
      }
      if(!tablemetaData.tableName.equals(tablesTable) && !tablemetaData.tableName.equals(columnsTable))
          System.out.println(count+" record(s) updated!");

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

   BPlusOneTree bPlusOneTree = new BPlusOneTree(file, tablemetaData.rootPageNo);
   List<Integer> leaves = new ArrayList<>();

  



   String currentValue ="";
   for(Integer pageNo : bPlusOneTree.getAllLeaves(condition))
   {
         Page page = new Page(file,pageNo);
         for(TableRecord record : page.records)
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
         }
   }
   
   System.out.println();

   }
     

   public void InsertRecord(int pageNo,TableRecord record) {
  


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

         currentPageNo = page.addTableRow(tablesTable,Arrays.asList(new Attribute[] { 
               new Attribute(DataType.TEXT, DavisBaseBinaryFile.tablesTable),
               new Attribute(DataType.INT, "2"), 
               new Attribute(DataType.SMALLINT, "0"),
               new Attribute(DataType.SMALLINT, "0") 
               }));

        currentPageNo = page.addTableRow(tablesTable,Arrays.asList(new Attribute[] {
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
         page.addNewColumn(tablesTable, new ColumnInfo(DataType.TEXT, "table_name", true, false, ordinal_position++));
         page.addNewColumn(tablesTable, new ColumnInfo(DataType.SMALLINT, "record_count", false, false, ordinal_position++));
         page.addNewColumn(tablesTable, new ColumnInfo(DataType.SMALLINT, "avg_length", false, false, ordinal_position++));
         page.addNewColumn(tablesTable, new ColumnInfo(DataType.SMALLINT, "root_page", false, false, ordinal_position++));
      
       

         //Add new columns to davisbase_columns

         ordinal_position = 1;

         page.addNewColumn(columnsTable, new ColumnInfo(DataType.TEXT, "table_name", false, false, ordinal_position++));
         page.addNewColumn(columnsTable, new ColumnInfo(DataType.TEXT, "column_name", false, false, ordinal_position++));
         page.addNewColumn(columnsTable, new ColumnInfo(DataType.SMALLINT, "data_type", false, false, ordinal_position++));
         page.addNewColumn(columnsTable, new ColumnInfo(DataType.SMALLINT, "ordinal_position", false, false, ordinal_position++));
         page.addNewColumn(columnsTable, new ColumnInfo(DataType.TEXT, "is_nullable", false, false, ordinal_position++));
         page.addNewColumn(columnsTable, new ColumnInfo(DataType.SMALLINT, "column_key", false, false, ordinal_position++));
         page.addNewColumn(columnsTable, new ColumnInfo(DataType.SMALLINT, "is_unique", false, false, ordinal_position++));

         davisbaseColumnsCatalog.close();
         dataStoreInitialized = true;
      } catch (Exception e) {
         out.println("Unable to create the database_columns file");
         out.println(e);
      }
   }

/*
   //test creat table by hua 14/07
   public static void test() {
      System.out.println("1");

      try {
                  
         RandomAccessFile davisbaseTablesCatalog = new RandomAccessFile("data/test.tbl", "rw");
         Page.addNewPage(davisbaseTablesCatalog, PageType.LEAF, -1, -1);
         Page page = new Page(davisbaseTablesCatalog,0);

         page.addTableRow(Arrays.asList(new Attribute[]{
                  new Attribute(DataType.TEXT,"test"),//DavisBaseBinaryFile.tablesTable->test
                  new Attribute(DataType.INT,"2"),
                  new Attribute(DataType.SMALLINT,"0"),
                  new Attribute(DataType.SMALLINT,"0")
         })); 
         davisbaseTablesCatalog.close();
      }
      catch(IOException ex){
         System.out.println("Cannot seek to start content of the file :");
      }
      
     
   }
   public static void test1() {
      try {
                  
         RandomAccessFile davisbaseTablesCatalog = new RandomAccessFile("data/test.tbl", "rw");
         //Page.addNewPage(davisbaseTablesCatalog, PageType.LEAF, 0, 0, -1, -1);
         Page page = new Page(davisbaseTablesCatalog,0);

         page.addTableRow(Arrays.asList(new Attribute[]{
                  new Attribute(DataType.TEXT,"test"),//DavisBaseBinaryFile.tablesTable->test
                  new Attribute(DataType.INT,"2"),
                  new Attribute(DataType.SMALLINT,"0"),
                  new Attribute(DataType.SMALLINT,"0")
         })); 
         davisbaseTablesCatalog.close();
      }
      catch(IOException ex){
         System.out.println("Cannot seek to start content of the file :");
      }
      
     
   }
   //====end test
   */

}


