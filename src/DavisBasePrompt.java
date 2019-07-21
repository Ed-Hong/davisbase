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
    
		//test creat table by hua 14/07
		/*if(!new File(dataDir,"test.tbl").exists()){
			System.out.println("creating test.tbl...");
			DavisBaseBinaryFile.test();
		}
		else{
			System.out.println("update test.tbl...");
			DavisBaseBinaryFile.test1();
		}*/

		//===end test


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
			out.println("SELECT <column_list> FROM <table_name> [WHERE <condition>];");
			out.println("\tDisplay table records whose optional <condition>");
			out.println("\tis <column_name> = <value>.\n");
			out.println("DROP TABLE <table_name>;");
			out.println("\tRemove table data (i.e. all records) and its schema.\n");
			out.println("UPDATE TABLE <table_name> SET <column_name> = <value> [WHERE <condition>];");
			out.println("\tModify records data whose optional <condition> is\n");
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
		// String[] commandTokens = userCommand.split(" ");
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
					System.out.println("Table Select will noe include RowId");
					}
				else
					System.out.println("I didn't understand the command: \"" + userCommand + "\"");
				break;
			case "select":
				parseQuery(userCommand);
				break;
			case "drop":
				System.out.println("CASE: DROP");
				dropTable(userCommand);
				break;
			case "create":
   			parseCreateTable(userCommand);
				break;
			case "update":
				System.out.println("CASE: UPDATE");
				parseUpdate(userCommand);
                break;
            case "insert":
				parseInsert(userCommand);
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
			default:
				System.out.println("I didn't understand the command: \"" + userCommand + "\"");
				break;
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
		//INSERT INTO davisbase_tables ( table_name ) VALUES ( testName );
    	System.out.println("STUB: This is the parseInsert method");
		System.out.println("\tParsing the string:\"" + queryString + "\"");
		ArrayList<String> insertTokens = new ArrayList<String>(Arrays.asList(queryString.split(" ")));
		
		try{
			RandomAccessFile davisbaseColumnsCatalog = new RandomAccessFile("data/davisbase_columns.tbl", "rw");
			Page page = new Page(davisbaseColumnsCatalog,0);
			String table = insertTokens.get(2);
			//System.out.println(table);
			//0: table name 1:col name 2:data_type 3:position 4:nullable
			
			ArrayList<String> row = new	ArrayList<String>();
			ArrayList<ArrayList<String>> domains = new	ArrayList<ArrayList<String>>();
			for(int i = 0; i<page.records.size();i++){
				//i start from 1 (0 is rowId)
				//get target table
				//System.out.println(page.records.get(i).getAttributes().get(0).fieldValue);
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
			/*
			for(int j = 0; j<domains.size();j++){
				System.out.println(domains.get(j).get(0));
				System.out.println(domains.get(j).get(1));
				System.out.println(domains.get(j).get(2));
				System.out.println(domains.get(j).get(3));
			}*/
			
			RandomAccessFile dstTable = new RandomAccessFile("data/"+ table +".tbl", "rw");
			Page dstPage = new Page(dstTable,0);
			
			ArrayList<String> dstAttribute = new ArrayList<String>();
			ArrayList<String> dstData = new	ArrayList<String>();
			for(int i = 4,step = 0; i<insertTokens.size();i++){//step:0 get attribue 1: data
				
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
				System.out.println("error");
				return;
			}

			for(int i = 0; i<dstData.size();i++){
				//System.out.println(dstAttribute.get(i));
				//System.out.println(dstData.get(i));
				valid = false;
				for(int j = 0; j<domains.size();j++){
					//column_name data_type ordinal_position is_nullable
					DataType type = DataType.TEXT; 	//todo insert actual data type
					switch (domains.get(j).get(1)) {
						case "INT":type = DataType.INT; break;
						case "TEXT":type = DataType.TEXT; break;
						case "SMALLINT":type = DataType.SMALLINT; break;

						default:
							break;
					}
					if(domains.get(j).get(0).equals(dstAttribute.get(i))){
						record.add(new Attribute(type ,dstData.get(i)));
						valid = true;
					}
					else if(domains.get(j).get(3).equals("yes")){
						record.add(new Attribute(type ,"NULL"));
						valid = true;
					}/*
					else{
						System.out.println("attribute can not be null");
						valid = false;
						break;
					}*/
					
				}
				
			}
			
			
			//System.out.println(record.get(0).fieldValue);
			//System.out.println(record.get(1).fieldValue);
			if(valid)
				dstPage.addTableRow(table, record);
			else
				System.out.println("attribute can not be null");
			dstTable.close();
			davisbaseColumnsCatalog.close();
		}
		catch(IOException ex){
			System.out.println("Cannot seek to start content of the file :");
		}
    }


	/**
	 *  Stub method for creating new tables
	 *  @param queryString is a String of the user input
	 */
	public static void parseCreateTable(String createTableString) {
		//create table aaa ( id int , c2 int , c3 text );
		System.out.println("STUB: Calling your method to create a table");
		System.out.println("Parsing the string:\"" + createTableString + "\"");
		ArrayList<String> createTableTokens = new ArrayList<String>(Arrays.asList(createTableString.split(" ")));

		
		try {
      
		
			 //table and () check
			if(!createTableTokens.get(1).equals("table")){
				System.out.println("Syntax Error");
				return;
			}
         
        List<ColumnInfo> lstcolumnInformation = new ArrayList<>();
        ArrayList<String> columnTokens = new ArrayList<String>(Arrays.
                     asList(createTableString
                        .substring(createTableString.indexOf("(") +1,createTableString.length() - 2).split(",")));              
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
			   String tableName = createTableTokens.get(2);
			RandomAccessFile tableFile = new RandomAccessFile(getTBLFilePath(tableName), "rw");
            Page.addNewPage(tableFile, PageType.LEAF,-1, -1);
            tableFile.close();
			
			//update sys file
			RandomAccessFile davisbaseTablesCatalog = new RandomAccessFile(getTBLFilePath(DavisBaseBinaryFile.tablesTable), "rw");
			TableMetaData davisbaseTableMetaData = new TableMetaData(DavisBaseBinaryFile.tablesTable);
			
         int pageNo = Page.getPageNoForInsert(davisbaseTablesCatalog, davisbaseTableMetaData.rootPageNo);

			Page page = new Page(davisbaseTablesCatalog,pageNo);

			pageNo = page.addTableRow(DavisBaseBinaryFile.tablesTable,Arrays.asList(new Attribute[]{
					new Attribute(DataType.TEXT,createTableTokens.get(2)),//DavisBaseBinaryFile.tablesTable->test
					new Attribute(DataType.INT,"0"),
					new Attribute(DataType.SMALLINT,"0"),
					new Attribute(DataType.SMALLINT,"0")
			})); 
			davisbaseTablesCatalog.close();
		
      if(pageNo ==-1) return; //error
      
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
			System.out.println(e);
		}
		
		/*  Code to insert a row in the davisbase_tables table 
		 *  i.e. database catalog meta-data 
		 */
		
		/*  Code to insert rows in the davisbase_columns table  
		 *  for each column in the new table 
		 *  i.e. database catalog meta-data 
		 */
    }

    
	public static String getTBLFilePath(String tableName)
	{
	   return "data/" + tableName + ".tbl";
	}
    
	




}