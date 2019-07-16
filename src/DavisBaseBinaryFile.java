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

/**
 *
 * @author Team Yellow
 * @version 1.0
 */
public class DavisBaseBinaryFile {

   public static String columnsTable = "davisbase_columns";
   public static String tablesTable = "davisbase_tables";

   public static String getDataFilePath(String tableName)
   {
      return "data/" + tableName + ".tbl";
   }

   /* This static variable controls page size. */
   static int pageSizePower = 9;
   /* This strategy insures that the page size is always a power of 2. */
   static int pageSize = (int) Math.pow(2, pageSizePower);

   RandomAccessFile file;

   public DavisBaseBinaryFile(RandomAccessFile file) {
      this.file = file;
   }

   public void selectRecords(TableMetaData tablemetaData, List<String> columNames, Condition condition, boolean showRowId) throws IOException{
   
   //The select order might be different from the table ordinal position
   List<Integer> ordinalPostions = tablemetaData.getOrdinalPostions(columNames);

   System.out.println();
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

  

   if(condition == null)
   {
      //brute force logic (as there are no index) traverse theough the tree and get all leaf pages
      leaves = bPlusOneTree.getAllLeaves();
   }
   else{
      //TODO find the leaf page numbers based on the condition and index files
      //right now we are taking all the leaves
      leaves = bPlusOneTree.getAllLeaves();
   }


   String currentValue ="";
   for(Integer pageNo : leaves)
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
         for (int i = 0; i < binaryfile.length() / binaryfile.length(); i++) {
            binaryfile.seek(i * DavisBaseBinaryFile.pageSize + 0x0A);
            if (binaryfile.readInt() == -1) {
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
               getDataFilePath(DavisBaseBinaryFile.tablesTable), "rw");
         Page.addNewPage(davisbaseTablesCatalog, PageType.LEAF, 0, -1, -1);
         Page page = new Page(davisbaseColumnsCatalog,currentPageNo);

         currentPageNo = page.addTableRow(Arrays.asList(new Attribute[] { 
               new Attribute(DataType.TEXT, DavisBaseBinaryFile.tablesTable),
               new Attribute(DataType.INT, "2"), 
               new Attribute(DataType.SMALLINT, "0"),
               new Attribute(DataType.SMALLINT, "0") 
               }));

        currentPageNo = page.addTableRow(Arrays.asList(new Attribute[] {
               new Attribute(DataType.TEXT, DavisBaseBinaryFile.columnsTable), new Attribute(DataType.INT, "9"),
               new Attribute(DataType.SMALLINT, "0"), new Attribute(DataType.SMALLINT, "0") }));

         davisbaseTablesCatalog.close();
      } catch (Exception e) {
         out.println("Unable to create the database_tables file");
         out.println(e);

      }

      /** Create davisbase_columns systems catalog */
      try {
         RandomAccessFile davisbaseColumnsCatalog = new RandomAccessFile(
            getDataFilePath(DavisBaseBinaryFile.columnsTable), "rw");
         Page.addNewPage(davisbaseColumnsCatalog, PageType.LEAF, 0, -1, -1);
         Page page = new Page(davisbaseColumnsCatalog, 0);

         int currentPageNo = 0;
         
         currentPageNo =  page.addTableRow(Arrays.asList(new Attribute[] { 
               new Attribute(DataType.TEXT, DavisBaseBinaryFile.tablesTable),
               new Attribute(DataType.TEXT, "table_name"),
               new Attribute(DataType.TEXT, "TEXT"),
               new Attribute(DataType.SMALLINT, "1"), 
               new Attribute(DataType.TEXT, "NO") }));
               
         currentPageNo = page.addTableRow(Arrays.asList(new Attribute[] { 
               new Attribute(DataType.TEXT, DavisBaseBinaryFile.tablesTable),
               new Attribute(DataType.TEXT, "record_count"),
               new Attribute(DataType.TEXT, "SMALLINT"),
               new Attribute(DataType.SMALLINT, "2"), 
               new Attribute(DataType.TEXT, "NO") }));
               
          currentPageNo = page.addTableRow(Arrays.asList(new Attribute[] { 
              
               new Attribute(DataType.TEXT, DavisBaseBinaryFile.tablesTable),
               new Attribute(DataType.TEXT, "avg_length"),
               new Attribute(DataType.TEXT, "SMALLINT"),
               new Attribute(DataType.SMALLINT, "3"), 
               new Attribute(DataType.TEXT, "NO") }));
               
          currentPageNo = page.addTableRow(Arrays.asList(new Attribute[] { 
               new Attribute(DataType.TEXT, DavisBaseBinaryFile.tablesTable),
               new Attribute(DataType.TEXT, "root_page"),
               new Attribute(DataType.TEXT, "SMALLINT"),
               new Attribute(DataType.SMALLINT, "4"), 
               new Attribute(DataType.TEXT, "NO") }));


         currentPageNo = page.addTableRow(
               Arrays.asList(new Attribute[] { new Attribute(DataType.TEXT, DavisBaseBinaryFile.columnsTable),
                     new Attribute(DataType.TEXT, "table_name"), new Attribute(DataType.TEXT, "TEXT"),
                     new Attribute(DataType.SMALLINT, "1"), new Attribute(DataType.TEXT, "NO") }));

         currentPageNo = page.addTableRow(
               Arrays.asList(new Attribute[] { new Attribute(DataType.TEXT, DavisBaseBinaryFile.columnsTable),
                     new Attribute(DataType.TEXT, "column_name"), new Attribute(DataType.TEXT, "TEXT"),
                     new Attribute(DataType.SMALLINT, "2"), new Attribute(DataType.TEXT, "NO") }));

         currentPageNo = page.addTableRow(
               Arrays.asList(new Attribute[] { new Attribute(DataType.TEXT, DavisBaseBinaryFile.columnsTable),
                     new Attribute(DataType.TEXT, "data_type"), new Attribute(DataType.TEXT, "TEXT"),
                     new Attribute(DataType.SMALLINT, "3"), new Attribute(DataType.TEXT, "NO") }));

         currentPageNo = page.addTableRow(
               Arrays.asList(new Attribute[] { new Attribute(DataType.TEXT, DavisBaseBinaryFile.columnsTable),
                     new Attribute(DataType.TEXT, "ordinal_position"), new Attribute(DataType.TEXT, "TEXT"),
                     new Attribute(DataType.SMALLINT, "4"), new Attribute(DataType.TEXT, "NO") }));

         currentPageNo = page.addTableRow(
               Arrays.asList(new Attribute[] { new Attribute(DataType.TEXT, DavisBaseBinaryFile.columnsTable),
                     new Attribute(DataType.TEXT, "is_nullable"), new Attribute(DataType.TEXT, "TEXT"),
                     new Attribute(DataType.SMALLINT, "5"), new Attribute(DataType.TEXT, "NO") }));

         davisbaseColumnsCatalog.close();
      } catch (Exception e) {
         out.println("Unable to create the database_columns file");
         out.println(e);
      }
   }


   //test creat table by hua 14/07
   public static void test() {
      System.out.println("1");

      try {
                  
         RandomAccessFile davisbaseTablesCatalog = new RandomAccessFile("data/test.tbl", "rw");
         Page.addNewPage(davisbaseTablesCatalog, PageType.LEAF, 0, 0, -1, -1);
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

}


