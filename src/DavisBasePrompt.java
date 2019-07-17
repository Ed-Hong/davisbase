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
	static String copyright = "�2019 Chris Irwin Davis";

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

	
		//System.out.println(page1.records);
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
				tableBinaryFile.selectRecords(tableMetaData,column_names,condition,false);
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
       System.out.println("STUB: This is the parseInsert method");
        System.out.println("\tParsing the string:\"" + queryString + "\"");
    }


	/**
	 *  Stub method for creating new tables
	 *  @param queryString is a String of the user input
	 */
	public static void parseCreateTable(String createTableString) {
		
		ArrayList<String> createTableTokens = new ArrayList<String>(Arrays.asList(createTableString.split(" ")));

		/* Define table file name */
		
		
		/* YOUR CODE GOES HERE */
		
		/*  Code to create a .tbl file to contain table data */
		try {
			/*  Create RandomAccessFile tableFile in read-write mode.
			 *  Note that this doesn't create the table file in the correct directory structure
			 */
			//System.out.println(createTableTokens.size());
			//for(int i = 0;i<createTableTokens.size();i++)
				//System.out.println(createTableTokens.get(i));
			
			if(!createTableTokens.get(1).equals("table")||!createTableTokens.get(3).equals("(")||!createTableTokens.get(createTableTokens.size()-1).equals(")")){
				System.out.println("error");
				//System.out.println(createTableTokens.get(1));
				//System.out.println(createTableTokens.get(3));
				//System.out.println(createTableTokens.get(createTableTokens.size()-1));
				return;
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
		
      	RandomAccessFile davisbaseColumnsCatalog = new RandomAccessFile(getTBLFilePath(DavisBaseBinaryFile.columnsTable), "rw");
			TableMetaData davisbaseColumnsMetaData = new TableMetaData(DavisBaseBinaryFile.columnsTable);
			pageNo = Page.getPageNoForInsert(davisbaseColumnsCatalog, davisbaseColumnsMetaData.rootPageNo);

            Page page1 = new Page(davisbaseColumnsCatalog,pageNo);
			
			
			for(int i = 4,j = 1;i<createTableTokens.size();i+=3,j++){


			pageNo = page1.addTableRow(DavisBaseBinaryFile.columnsTable,Arrays.asList(new Attribute[]{
					new Attribute(DataType.TEXT,tableName),
					new Attribute(DataType.TEXT,createTableTokens.get(i)),
					new Attribute(DataType.TEXT,createTableTokens.get(i+1).toUpperCase()),
					new Attribute(DataType.SMALLINT,String.valueOf(j)),
					new Attribute(DataType.TEXT,"NO")
		   		})); 
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