import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static java.lang.System.out;


/**
 *  @author Chris Irwin Davis
 *  @version 1.0
 *  <b>
 *  <p>This is an example of how to create an interactive prompt</p>
 *  <p>There is also some guidance to get started wiht read/write of
 *     binary data files using RandomAccessFile class</p>
 *  </b>
 *
 */
public class DavisBasePrompt {

	/* This can be changed to whatever you like */
	static String prompt = "davisql> ";
	static String version = "v1.0b(example)";
	static String copyright = "Chris Irwin Davis";

	static boolean isExit = false;
	/*
	 * Page size for alll files is 512 bytes by default.
	 * You may choose to make it user modifiable
	 */
    static long pageSize = 512; 
    
	/* 
	 *  The Scanner class is used to collect user commands from the prompt
	 *  There are many ways to do this. This is just one.
	 *
	 *  Each time the semicolon (;) delimiter is entered, the userCommand 
	 *  String is re-populated.
	 */
	static Scanner scanner = new Scanner(System.in).useDelimiter(";");
	
	/** ***********************************************************************
	 *  Main method
	 */
    public static void main(String[] args) {

		/* Display the welcome screen */
        splashScreen();
    
        File dataDir = new File("data");
    
        if(!new File(dataDir,DavisBaseBinaryFile.tablesTable+".tbl").exists() || 
            !new File(dataDir,DavisBaseBinaryFile.columnsTable+".tbl").exists())
                DavisBaseBinaryFile.initializeDataStore();
        else
         DavisBaseBinaryFile.dataStoreInitialized = true;

        /* Variable to collect user input from the prompt */
		String userCommand = ""; 

		while(!isExit) {
			System.out.print(prompt);
			/* toLowerCase() renders command case insensitive */
			userCommand = scanner.next().replace("\n", " ").replace("\r", "").trim().toLowerCase();
			// userCommand = userCommand.replace("\n", "").replace("\r", "");
			parseUserCommand(userCommand);
		}  
		System.out.println("Exiting...");
    }
    
	/** ***********************************************************************
	 *  Static method definitions
	 */

	/**
	 *  Display the splash screen
	 */
	public static void splashScreen() {
		System.out.println(line("-",80));
        System.out.println("Welcome to DavisBaseLite"); // Display the string.
		System.out.println("DavisBaseLite Version " + getVersion());
		System.out.println(getCopyright());
		System.out.println("\nType \"help;\" to display supported commands.");
		System.out.println(line("-",80));
	}
	
	/**
	 * @param s The String to be repeated
	 * @param num The number of time to repeat String s.
	 * @return String A String object, which is the String s appended to itself num times.
	 */
	public static String line(String s,int num) {
		String a = "";
		for(int i=0;i<num;i++) {
			a += s;
		}
		return a;
	}
	
	public static void printCmd(String s) {
		System.out.println("\n\t" + s + "\n");
	}
	public static void printDef(String s) {
		System.out.println("\t\t" + s);
	}
	
		/**
		 *  Help: Display supported commands
		 */
		public static void help() {
			out.println(line("*",80));
			out.println("SUPPORTED COMMANDS\n");
			out.println("All commands below are case insensitive\n");

			out.println("SHOW TABLES;");
			out.println("\tDisplay the names of all tables.\n");

			out.println("CREATE TABLE <table_name> (<column_name> <data_type> <null> <unique>);");
			out.println("\tCreates a table with the given columns.\n");

			out.println("DROP TABLE <table_name>;");
			out.println("\tRemove table data (i.e. all records) and its schema.\n");

			out.println("UPDATE TABLE <table_name> SET <column_name> = <value> [WHERE <condition>];");
			out.println("\tModify records data whose optional <condition>");
			out.println("\tis <column_name> = <value>.\n");

			out.println("INSERT INTO <table_name> (<column_list>) VALUES (<values_list>);");
			out.println("\tInserts a new record into the table with the given values for the given columns.\n");

			out.println("SELECT <column_list> FROM <table_name> [WHERE <condition>];");
			out.println("\tDisplay table records whose optional <condition>");
			out.println("\tis <column_name> = <value>.\n");

			out.println("VERSION;");
			out.println("\tDisplay the program version.\n");

			out.println("HELP;");
			out.println("\tDisplay this help information.\n");

			out.println("EXIT;");
			out.println("\tExit the program.\n");

			out.println(line("*",80));
		}

	/** return the DavisBase version */
	public static String getVersion() {
		return version;
	}
	
	public static String getCopyright() {
		return copyright;
	}
	
	public static void displayVersion() {
		System.out.println("DavisBaseLite Version " + getVersion());
		System.out.println(getCopyright());
	}
	
		
	public static void parseUserCommand (String userCommand) {
		
		/* commandTokens is an array of Strings that contains one token per array element 
		 * The first token can be used to determine the type of command 
		 * The other tokens can be used to pass relevant parameters to each command-specific
		 * method inside each case statement */
		ArrayList<String> commandTokens = new ArrayList<String>(Arrays.asList(userCommand.split(" ")));
		
		/*
		*  This switch handles a very small list of hardcoded commands of known syntax.
		*  You will want to rewrite this method to interpret more complex commands. 
		*/
		switch (commandTokens.get(0)) {
			case "show":
				if(commandTokens.get(1).equals("tables"))
					parseUserCommand("select * from davisbase_tables");			
				else if(commandTokens.get(1).equals("rowid"))
					{DavisBaseBinaryFile.showRowId = true;
					System.out.println("Table Select will now include RowId");
					}
				else
					System.out.println("I didn't understand the command: \"" + userCommand + "\"");
				break;
			case "select":
				parseQuery(userCommand);
				break;
			case "drop":
				dropTable(userCommand);
				break;
			case "create":
			if(commandTokens.get(1).equals("table"))
				parseCreateTable(userCommand);
			else if(commandTokens.get(1).equals("index"))
				parseCreateIndex(userCommand);
				break;
			case "update":
				parseUpdate(userCommand);
                break;
            case "insert":
				parseInsert(userCommand);
				break;
			case "delete":
				parseDelete(userCommand);
				break;
			case "help":
				help();
				break;
			case "version":
				displayVersion();
				break;
			case "exit":
				isExit = true;
				break;
			case "quit":
				isExit = true;
				break;
			default:
				System.out.println("I didn't understand the command: \"" + userCommand + "\"");
				break;
		}
	}
	

	//TODO create Index
	public static void parseCreateIndex(String createIndexString)
	{
		ArrayList<String> createIndexTokens = new ArrayList<String>
											(Arrays.asList(createIndexString.split(" ")));
		try {
			if(!createIndexTokens.get(4).equals("on")){
				System.out.println("Syntax Error");
				return;
			}
			String tableName = createIndexTokens.get(3);
			String columnName = createIndexTokens.get(5);
			
			//create index file
			RandomAccessFile indexFile = new RandomAccessFile(getNDXFilePath(tableName, columnName),"rw");
			Page.addNewPage(indexFile, PageType.LEAFINDEX,-1, -1);
			
            indexFile.close();
         

		}
		catch(IOException e) {
			
			System.out.println("Error on creating Index");
			System.out.println(e);
		}
      
	}

	/**
	 *  Stub method for dropping tables
	 *  @param dropTableString is a String of the user input
	 */
	public static void dropTable(String dropTableString) {
		System.out.println("STUB: This is the dropTable method.");
		System.out.println("\tParsing the string:\"" + dropTableString + "\"");
	}
	
	/**
	 *  Stub method for executing queries
	 *  @param queryString is a String of the user input
	 */
	public static void parseQuery(String queryString) {
		String table_name ="";
		List<String> column_names = new ArrayList<String>();
		
		//Get table and column names for the select
		ArrayList<String> queryTableTokens = new ArrayList<String>(Arrays.asList(queryString.split(" ")));
		int i=0;
		
		for(i=1;i<queryTableTokens.size();i++)
		{
         if(queryTableTokens.get(i).equals("from"))
			{
				++i;
				table_name = queryTableTokens.get(i);
				break;
			}
			if(!queryTableTokens.get(i).equals("*") && !queryTableTokens.get(i).equals(","))
			{
				if(queryTableTokens.get(i).contains(","))
				{
					ArrayList<String> colList = new ArrayList<String>(Arrays.asList(queryTableTokens.get(i).split(",")));
					for(String col :colList)
					{
						column_names.add(col);
					}
				}
				else
					column_names.add(queryTableTokens.get(i));
			}
		}

		++i;

		Condition condition = null;

		if(queryTableTokens.size() > i)
		{
			condition = new Condition();
			
		
         if(queryTableTokens.get(i).equals("where"))
			{
				i++;
			}
         if(queryTableTokens.get(i).equals("not"))
			{
				i++;condition.setNegation(true);
			}

			condition.setColumName(queryTableTokens.get(i));
			condition.setOperator(queryTableTokens.get(i+1));
        	condition.setConditionValue(queryTableTokens.get(i+2));
		}

		TableMetaData tableMetaData = new TableMetaData(table_name);

		if(tableMetaData.tableExists
			&& tableMetaData.columnExists(table_name,column_names))
		{

			if(condition != null)
         {
			 if(tableMetaData.columnExists(table_name,
				 new ArrayList<String>(Arrays.asList(condition.columnName))))
				{
					condition.columnOrdinal = tableMetaData.columnNames.indexOf(condition.columnName);
					condition.dataType = tableMetaData.columnNameAttrs.get(condition.columnOrdinal).dataType;
				}
				else{
					System.out.println("Cannot find table/columns in catalog files");
					return;
				}
         }


			if(column_names.size() == 0)
			{
				column_names = tableMetaData.columnNames;
			}
			try {		
         
				RandomAccessFile tableFile = new RandomAccessFile(getTBLFilePath(table_name), "r");
				DavisBaseBinaryFile tableBinaryFile = new DavisBaseBinaryFile(tableFile);
				tableBinaryFile.selectRecords(tableMetaData,column_names,condition);
				tableFile.close();
			}
			catch(IOException exception){
				System.out.println("Error selecting columns from table");
			}
		}
		 else{
			System.out.println("Cannot find table/columns in catalog files");
		}

	}

	/**
	 *  Stub method for updating records
	 *  @param updateString is a String of the user input
	 */
	public static void parseUpdate(String updateString) {
		System.out.println("STUB: This is the dropTable method");
		System.out.println("Parsing the string:\"" + updateString + "\"");
	}

    public static void parseInsert(String queryString) {
		//INSERT INTO table_name ( columns ) VALUES ( values );
		ArrayList<String> insertTokens = new ArrayList<String>(Arrays.asList(queryString.split(" ")));
		
		try{
			RandomAccessFile davisbaseColumnsCatalog = new RandomAccessFile("data/davisbase_columns.tbl", "rw");
			TableMetaData davisbaseColumnsMetaData = new TableMetaData(DavisBaseBinaryFile.columnsTable);
			int pageNo = Page.getPageNoForInsert(davisbaseColumnsCatalog, davisbaseColumnsMetaData.rootPageNo);
			
			String table = insertTokens.get(2);
			//0: table name 1:col name 2:data_type 3:position 4:nullable
			
			ArrayList<String> row = new	ArrayList<String>();
			ArrayList<ArrayList<String>> domains = new	ArrayList<ArrayList<String>>();

			Page page = new Page(davisbaseColumnsCatalog,pageNo);
				
			for(int i = 0; i<page.records.size();i++){
				//get target table
				if(page.records.get(i).getAttributes().get(0).fieldValue.equals(table)){
					//get ith col from davisbase_columns.tbl
					row.add(page.records.get(i).getAttributes().get(1).fieldValue);
					row.add(page.records.get(i).getAttributes().get(2).fieldValue);
					row.add(page.records.get(i).getAttributes().get(3).fieldValue);
					row.add(page.records.get(i).getAttributes().get(4).fieldValue);
					//store domain and constrain in domains
					domains.add(new ArrayList<String>(row));
					row.clear();					
						
				}

			}
			
			if(domains.size()==0){
				System.out.println("Table does not exist.");
				return;
			}

			RandomAccessFile dstTable = new RandomAccessFile("data/"+ table +".tbl", "rw");
			TableMetaData dstMetaData = new TableMetaData("data/"+ table +".tbl");
			int dstPageNo = Page.getPageNoForInsert(dstTable, dstMetaData.rootPageNo);
			Page dstPage = new Page(dstTable,dstPageNo);
			
			ArrayList<String> dstAttribute = new ArrayList<String>();
			ArrayList<String> dstData = new	ArrayList<String>();

			for(int i = 4,step = 0; i<insertTokens.size();i++){ //step:0 get attribue 1: data
				String tmp = insertTokens.get(i);
				if(tmp.equals("values")){
					step++;
					continue;
				}
				if(!tmp.equals(",")&& !tmp.equals("(") && !tmp.equals(")") && step ==0){
					dstAttribute.add(tmp);
				}
				if(!tmp.equals(",")&& !tmp.equals("(") && !tmp.equals(")") && step ==1){
					dstData.add(tmp);
				}
			}
			boolean valid = true;
			ArrayList<Attribute> record = new ArrayList<Attribute>();
			if(dstAttribute.size() !=dstData.size()){
				System.out.println("Error: received " + dstAttribute.size() + " columns but got " + dstData.size() + " values.");
				return;
			}

			for(int i = 0; i<dstData.size();i++){
				valid = false;
				for(int j = 0; j<domains.size();j++){
					//column_name data_type ordinal_position is_nullable
					DataType type = DataType.TEXT;
					switch (domains.get(j).get(1)) {
						case "INT":type = DataType.INT; break;
						case "TEXT":type = DataType.TEXT; break;
						case "SMALLINT":type = DataType.SMALLINT; break;
						case "TINYINT":type = DataType.TINYINT; break;
						case "BIGINT":type = DataType.BIGINT; break;
						case "FLOAT":type = DataType.FLOAT; break;
						case "DOUBLE":type = DataType.DOUBLE; break;
						case "YEAR":type = DataType.YEAR; break;
						case "TIME":type = DataType.TIME; break;
						case "DATETIME":type = DataType.DATETIME; break;
						case "DATE":type = DataType.DATE; break;
						default: type = DataType.TEXT; break;
					}
					
					if(domains.get(j).get(0).equals(dstAttribute.get(i))){
						try {
							// Inserting NULL into a column that allows NULLs
							if(dstData.get(i).equalsIgnoreCase("NULL")) {
								if(domains.get(j).get(3).equalsIgnoreCase("YES")) {
									record.add(new Attribute(DataType.NULL, "NULL"));
									valid = true;
								} else {
									System.out.println("Column " + domains.get(j).get(0) + " does not allow NULLs.");
									valid = false;
								}
							} else {
								record.add(new Attribute(type, dstData.get(i)));
								valid = true;
							}
						} catch (Exception e) {
							valid = false;
						}
					}
				}
				
			}
			
			if(valid) {
				dstPage.addTableRow(table, record);
			}
			else {
				System.out.println("INSERT aborted.");
			}
			dstTable.close();
			davisbaseColumnsCatalog.close();
		}
		catch(IOException ex){
			System.out.println("Cannot seek to start content of the file :");
		}
    }


	/**
	 *  Create new table
	 *  @param queryString is a String of the user input
	 */
	public static void parseCreateTable(String createTableString) {

		ArrayList<String> createTableTokens = new ArrayList<String>(Arrays.asList(createTableString.split(" ")));
		
		try {
      
			 //table and () check
			if(!createTableTokens.get(1).equals("table")){
				System.out.println("Syntax Error");
				return;
			}
			String tableName = createTableTokens.get(2);
         
         if(tableName.indexOf("(") > -1)
         {
         tableName = tableName.substring(0,tableName.indexOf("("));
                           } 
                  
        List<ColumnInfo> lstcolumnInformation = new ArrayList<>();
        ArrayList<String> columnTokens = new ArrayList<String>(Arrays.
                     asList(createTableString
                        .substring(createTableString.indexOf("(") +1,createTableString.length() - 1).split(",")));              
						short ordinalPosition = 1;
         for(String columnToken :columnTokens)
         {
            
		    ArrayList<String> colInfoToken = new ArrayList<String>(Arrays.asList(columnToken.trim().split(" ")));
            ColumnInfo colInfo = new ColumnInfo();
			colInfo.columnName = colInfoToken.get(0);
			colInfo.dataType = DataType.get(colInfoToken.get(1).toUpperCase());
			for(int i=0;i<colInfoToken.size();i++)
			{
	
				if((colInfoToken.get(i).equals("null")))
					{colInfo.isNullable = true; }
				if(colInfoToken.get(i).contains("not") && (colInfoToken.get(i+1).contains("null")))
				{
						i++;
				}

				if((colInfoToken.get(i).equals("unique")))
					{colInfo.isUnique = true; }
			    else if(colInfoToken.get(i).contains("primary") && (colInfoToken.get(i+1).contains("key"))){
					colInfo.isPrimaryKey = true;	
					i++;
				}
			
			}
			colInfo.ordinalPosition = ordinalPosition++;
			lstcolumnInformation.add(colInfo);
         
         }
			  
			
			//update sys file
			RandomAccessFile davisbaseTablesCatalog = new RandomAccessFile(getTBLFilePath(DavisBaseBinaryFile.tablesTable), "rw");
			TableMetaData davisbaseTableMetaData = new TableMetaData(DavisBaseBinaryFile.tablesTable);
			
         int pageNo = Page.getPageNoForInsert(davisbaseTablesCatalog, davisbaseTableMetaData.rootPageNo);

			Page page = new Page(davisbaseTablesCatalog,pageNo);

			pageNo = page.addTableRow(DavisBaseBinaryFile.tablesTable,Arrays.asList(new Attribute[]{
					new Attribute(DataType.TEXT,tableName),//DavisBaseBinaryFile.tablesTable->test
					new Attribute(DataType.INT,"0"),
					new Attribute(DataType.SMALLINT,"0"),
					new Attribute(DataType.SMALLINT,"0")
			})); 
			davisbaseTablesCatalog.close();
		
      if(pageNo ==-1) {
		  System.out.println("Duplicate table Name");
		return;
	  }
      	RandomAccessFile tableFile = new RandomAccessFile(getTBLFilePath(tableName), "rw");
            Page.addNewPage(tableFile, PageType.LEAF,-1, -1);
            tableFile.close();
         
      		RandomAccessFile davisbaseColumnsCatalog = new RandomAccessFile(getTBLFilePath(DavisBaseBinaryFile.columnsTable), "rw");
			TableMetaData davisbaseColumnsMetaData = new TableMetaData(DavisBaseBinaryFile.columnsTable);
			pageNo = Page.getPageNoForInsert(davisbaseColumnsCatalog, davisbaseColumnsMetaData.rootPageNo);

            Page page1 = new Page(davisbaseColumnsCatalog,pageNo);
			
			for(ColumnInfo column :lstcolumnInformation)
			{
				page1.addNewColumn(tableName, column);
			}

			davisbaseColumnsCatalog.close();

		
         System.out.println("\nTable created");
		}
		catch(Exception e) {
			
			System.out.println("Error on creating Table");
			System.out.println(e);
		}
	}
	

	/**
	 *  Delete records from table
	 *  @param queryString is a String of the user input
	 */
	private static void parseDelete(String deleteTableString) {
		ArrayList<String> deleteTableTokens = new ArrayList<String>(Arrays.asList(deleteTableString.split(" ")));
		int i=0;
		String tableName =  "";
		
	try {
      
		if(!deleteTableTokens.get(1).equals("from") 
		 && !deleteTableTokens.get(2).equals("table")){
			System.out.println("Syntax Error");
			return;
		}

		tableName = deleteTableTokens.get(3);
		Condition condition = null;
		int ordinalPosition = 0;

		RandomAccessFile tableFile = new RandomAccessFile(getTBLFilePath(tableName), "rw");
		TableMetaData metaData = new TableMetaData(tableName);

		if(deleteTableTokens.size() > 4 && deleteTableTokens.get(4).equals("where"))
		{
			i=5;
			condition = new Condition();

			if(deleteTableTokens.get(5).equals("not"))
			{
				condition.setNegation(true);
				i++;
			}

			condition.columnName = deleteTableTokens.get(i++);
			condition.setOperator(deleteTableTokens.get(i++));
			condition.setConditionValue(deleteTableTokens.get(i++));
			ordinalPosition = metaData.columnNames.indexOf(condition.columnName);
			condition.dataType = metaData.columnNameAttrs.get(ordinalPosition).dataType;

		}

	

		if(metaData.tableExists)
			{
				if(!metaData.columnExists(tableName,Arrays.asList(condition.columnName)))
				{
					System.out.println("Column " + condition.columnName+ " does not exist !");
					tableFile.close();
					return;
				}
			}
		else{
			System.out.println("Table " + tableName+ " does not exist !");
			tableFile.close();
			 return;
		}

    
		BPlusOneTree tree = new BPlusOneTree(tableFile, metaData.rootPageNo);
		int count =0;
		for(int pageNo : tree.getAllLeaves(condition))
		{
			short deleteCountPerPage = 0;
      	    Page page = new Page(tableFile,pageNo);
			for(TableRecord record : page.records)
			{
			   if(condition!=null)
			   {
				if(!condition.checkCondition(record.getAttributes()
								.get(ordinalPosition).fieldValue))
					continue;
			   }
            
			   page.DeleteTableRecord(tableName, Integer.valueOf(record.pageHeaderIndex 
			   														- deleteCountPerPage).shortValue());
			   deleteCountPerPage++;
			   count++;
			}
		}

		System.out.println();
		tableFile.close();
		System.out.println(count+" record(s) deleted!");

	}
	catch(Exception e) {
		System.out.println("Error on deleting rows in table : " +tableName);
		System.out.println(e);
	}

	}

    
	public static String getTBLFilePath(String tableName)
	{
	   return "data/" + tableName + ".tbl";
	}
	

	public static String getNDXFilePath(String tableName,String columnName)
	{
	   return "data/" + tableName + "_"+ columnName + ".tbl";
	}
	
	
	
	




}