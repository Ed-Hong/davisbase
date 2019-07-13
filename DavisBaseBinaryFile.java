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
   
	/* This static variable controls page size. */
	static int pageSizePower = 9;
	/* This strategy insures that the page size is always a power of 2. */
	static int pageSize = (int)Math.pow(2, pageSizePower);
   
   RandomAccessFile binaryFile;

   private String _tablename;
   public DavisBaseBinaryFile(String tableName)
   {
      _tablename = tableName;
      try
      {
          binaryFile = new RandomAccessFile("data/" + tableName, "rw");
      }
      catch(FileNotFoundException ex){
         System.out.println("Cannot find file :" + _tablename);
      }
   }

   public void ListAllRecords()
   {
   
   }
   
   public void InsertRecord(HashMap row){
      if(_tablename == tablesTable){

            //get the character length
            //row.get("table_name");


      }
   }


   /**
	 * This static method creates the DavisBase data storage container
	 * and then initializes two .tbl files to implement the two 
	 * system tables, davisbase_tables and davisbase_columns
	 *
	 *  WARNING! Calling this method will destroy the system database
	 *           catalog files if they already exist.
	 */
	public static void initializeDataStore() {

		/** Create data directory at the current OS location to hold */
		try {
			File dataDir = new File("data");
            dataDir.mkdir();
			String[] oldTableFiles;
			oldTableFiles = dataDir.list();
			for (int i=0; i<oldTableFiles.length; i++) {
				File anOldFile = new File(dataDir, oldTableFiles[i]); 
            anOldFile.delete();
			}
		}
		catch (SecurityException se) {
			out.println("Unable to create data container directory");
			out.println(se);
		}

		/** Create davisbase_tables system catalog */
		try {
            
			   RandomAccessFile davisbaseTablesCatalog = new RandomAccessFile("data/"+DavisBaseBinaryFile.tablesTable+".tbl", "rw");
            Page.addNewPage(davisbaseTablesCatalog, PageType.LEAF, 0, 0, -1, -1);
            Page page = new Page(davisbaseTablesCatalog,0);

            page.addTableRow(Arrays.asList(new Attribute[]{
                     new Attribute(DataType.TEXT,DavisBaseBinaryFile.tablesTable),
                     new Attribute(DataType.INT,"2"),
                     new Attribute(DataType.SMALLINT,"0"),
                     new Attribute(DataType.SMALLINT,"0")
            })); 
            
             page.addTableRow(Arrays.asList(new Attribute[]{
                     new Attribute(DataType.TEXT,DavisBaseBinaryFile.columnsTable),
                     new Attribute(DataType.INT,"8"),
                     new Attribute(DataType.SMALLINT,"0"),
                     new Attribute(DataType.SMALLINT,"0")
            })); 
          
            davisbaseTablesCatalog.close();
      }
		catch (Exception e) {
			out.println("Unable to create the database_tables file");
			out.println(e);
                    
		}

		/** Create davisbase_columns systems catalog */
		try {
			RandomAccessFile davisbaseColumnsCatalog = new RandomAccessFile("data/"+DavisBaseBinaryFile.columnsTable+".tbl", "rw");
            Page.addNewPage(davisbaseColumnsCatalog, PageType.LEAF, 0, 0, -1, -1);
            Page page = new Page(davisbaseColumnsCatalog,0);
            
            page.addTableRow(Arrays.asList(new Attribute[]{
                     new Attribute(DataType.TEXT,DavisBaseBinaryFile.tablesTable),
                     new Attribute(DataType.TEXT,"rowid"),
                     new Attribute(DataType.TEXT,"INT"),
                     new Attribute(DataType.SMALLINT,"1"),
                     new Attribute(DataType.TEXT,"NO")
            })); 
            
             page.addTableRow(Arrays.asList(new Attribute[]{
                     new Attribute(DataType.TEXT,DavisBaseBinaryFile.tablesTable),
                     new Attribute(DataType.TEXT,"table_name"),
                     new Attribute(DataType.TEXT,"TEXT"),
                     new Attribute(DataType.SMALLINT,"2"),
                     new Attribute(DataType.TEXT,"NO")
            })); 

             page.addTableRow(Arrays.asList(new Attribute[]{
                     new Attribute(DataType.TEXT,DavisBaseBinaryFile.columnsTable),
                     new Attribute(DataType.TEXT,"rowid"),
                     new Attribute(DataType.TEXT,"INT"),
                     new Attribute(DataType.SMALLINT,"1"),
                     new Attribute(DataType.TEXT,"NO")
            })); 
            
            page.addTableRow(Arrays.asList(new Attribute[]{
                     new Attribute(DataType.TEXT,DavisBaseBinaryFile.columnsTable),
                     new Attribute(DataType.TEXT,"table_name"),
                     new Attribute(DataType.TEXT,"TEXT"),
                     new Attribute(DataType.SMALLINT,"2"),
                     new Attribute(DataType.TEXT,"NO")
            })); 
            
             page.addTableRow(Arrays.asList(new Attribute[]{
                     new Attribute(DataType.TEXT,DavisBaseBinaryFile.columnsTable),
                     new Attribute(DataType.TEXT,"column_name"),
                     new Attribute(DataType.TEXT,"TEXT"),
                     new Attribute(DataType.SMALLINT,"3"),
                     new Attribute(DataType.TEXT,"NO")
            })); 
         
            page.addTableRow(Arrays.asList(new Attribute[]{
                     new Attribute(DataType.TEXT,DavisBaseBinaryFile.columnsTable),
                     new Attribute(DataType.TEXT,"data_type"),
                     new Attribute(DataType.TEXT,"TEXT"),
                     new Attribute(DataType.SMALLINT,"4"),
                     new Attribute(DataType.TEXT,"NO")
            })); 
            
            page.addTableRow(Arrays.asList(new Attribute[]{
                     new Attribute(DataType.TEXT,DavisBaseBinaryFile.columnsTable),
                     new Attribute(DataType.TEXT,"ordinal_position"),
                     new Attribute(DataType.TEXT,"TEXT"),
                     new Attribute(DataType.SMALLINT,"5"),
                     new Attribute(DataType.TEXT,"NO")
            })); 
            
             page.addTableRow(Arrays.asList(new Attribute[]{
                     new Attribute(DataType.TEXT,DavisBaseBinaryFile.columnsTable),
                     new Attribute(DataType.TEXT,"is_nullable"),
                     new Attribute(DataType.TEXT,"TEXT"),
                     new Attribute(DataType.SMALLINT,"6"),
                     new Attribute(DataType.TEXT,"NO")
            })); 
                                                                                                     
			davisbaseColumnsCatalog.close();
		}
		catch (Exception e) {
			out.println("Unable to create the database_columns file");
			out.println(e);
		}
	}
   
   
   private void seekToWritePosition(int rowSize)
   {
      try {
         //seek to the start of rightmost page
         //pagetype =  binaryFile.readShort();
         //numberofRecordsInPage =  binaryFile.readShort();
         //contentStart =  binaryFile.readInt();
         //binaryFile.seek(contentStart - rowSize - extra);

         binaryFile.seek(2);   


      }
      catch(IOException ex){
         System.out.println("Cannot seek to start content of the file :" +_tablename);
      }
   }

}