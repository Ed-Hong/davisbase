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
 * @author Team Yellow
 * @version 1.0 <b>
 *          <p>
 *          This is an example of how to create an interactive prompt
 *          </p>
 *          <p>
 *          There is also some guidance to get started with read/write of binary
 *          data files using RandomAccessFile class
 *          </p>
 *          </b>
 *
 */
public class DavisBasePrompt {

	static String prompt = "sqverylite> ";
	static String version = "v1.0";
	static String copyright = "Team Yellow";

	static boolean isExit = false;
	/*
	 * Page size for alll files is 512 bytes by default. You may choose to make it
	 * user modifiable
	 */
	static long pageSize = 512;

	/*
	 * The Scanner class is used to collect user commands from the prompt There are
	 * many ways to do this. This is just one.
	 *
	 * Each time the semicolon (;) delimiter is entered, the userCommand String is
	 * re-populated.
	 */
	static Scanner scanner = new Scanner(System.in).useDelimiter(";");

	/**
	 * ******** Main method ******************
	 */
	public static void main(String[] args) {

		/* Display the welcome screen */
		splashScreen();

		File dataDir = new File("data");

		if (!new File(dataDir, DavisBaseBinaryFile.tablesTable + ".tbl").exists()
				|| !new File(dataDir, DavisBaseBinaryFile.columnsTable + ".tbl").exists())
			DavisBaseBinaryFile.initializeDataStore();
		else
			DavisBaseBinaryFile.dataStoreInitialized = true;

		/* Variable to collect user input from the prompt */
		String userCommand = "";

		while (!isExit) {
			System.out.print(prompt);
			/* toLowerCase() renders command case insensitive */
			userCommand = scanner.next().replace("\n", " ").replace("\r", "").trim().toLowerCase();
			// userCommand = userCommand.replace("\n", "").replace("\r", "");
			parseUserCommand(userCommand);
		}
		System.out.println("Exiting...");
	}

	/**
	 * ***********************************************************************
	 * Static method definitions
	 */

	/**
	 * Display the splash screen
	 */
	public static void splashScreen() {
		System.out.println(line("-", 80));
		System.out.println("Welcome to SQVeryLite"); // Display the string.
		System.out.println("SQVeryLite Version " + getVersion());
		System.out.println(getCopyright());
		System.out.println("\nType \"help;\" to display supported commands.");
		System.out.println(line("-", 80));
	}

	/**
	 * @param s   The String to be repeated
	 * @param num The number of time to repeat String s.
	 * @return String A String object, which is the String s appended to itself num
	 *         times.
	 */
	public static String line(String s, int num) {
		String a = "";
		for (int i = 0; i < num; i++) {
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
	 * Help: Display supported commands
	 */
	public static void help() {
		out.println(line("*", 80));
		out.println("SUPPORTED COMMANDS\n");
		out.println("All commands below are case insensitive\n");

		out.println("SHOW TABLES;");
		out.println("\tDisplay the names of all tables.\n");

		out.println("CREATE TABLE <table_name> (<column_name> <data_type> <not_null> <unique>);");
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

		out.println(line("*", 80));
	}

	/** return the DavisBase version */
	public static String getVersion() {
		return version;
	}

	public static String getCopyright() {
		return copyright;
	}

	public static void displayVersion() {
		System.out.println("SQVeryLite Version " + getVersion());
		System.out.println(getCopyright());
	}

	public static void parseUserCommand(String userCommand) {

		/*
		 * commandTokens is an array of Strings that contains one token per array
		 * element The first token can be used to determine the type of command The
		 * other tokens can be used to pass relevant parameters to each command-specific
		 * method inside each case statement
		 */
		ArrayList<String> commandTokens = new ArrayList<String>(Arrays.asList(userCommand.split(" ")));

		/*
		 * This switch handles a very small list of hardcoded commands of known syntax.
		 * You will want to rewrite this method to interpret more complex commands.
		 */
		switch (commandTokens.get(0)) {
		case "show":
			if (commandTokens.get(1).equals("tables"))
				parseUserCommand("select * from davisbase_tables");
			else if (commandTokens.get(1).equals("rowid")) {
				DavisBaseBinaryFile.showRowId = true;
				System.out.println("* Table Select will now include RowId.");
			} else
				System.out.println("! I didn't understand the command: \"" + userCommand + "\"");
			break;
		case "select":
			parseQuery(userCommand);
			break;
		case "drop":
			dropTable(userCommand);
			break;
		case "create":
			if (commandTokens.get(1).equals("table"))
				parseCreateTable(userCommand);
			else if (commandTokens.get(1).equals("index"))
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
		case "test":
			test();
			break;
		default:
			System.out.println("! I didn't understand the command: \"" + userCommand + "\"");
			break;
		}
	}

	public static void test() {
		Scanner scan = new Scanner(System.in);
      parseUserCommand("create table test (id int, name text)");
      scan.nextLine();
      parseUserCommand("create index on test (name)");
      scan.nextLine();
		for (int i = 1; i < 35; i++)
		{	
   //   System.out.println(i);
      parseUserCommand("insert into test (id , name) values (" + (i) + ", "+ i + "'arun' )");

		//scan.nextLine();
      }
		parseUserCommand("show tables");

		scan.nextLine();

	}

	public static void parseCreateIndex(String createIndexString) {
		ArrayList<String> createIndexTokens = new ArrayList<String>(Arrays.asList(createIndexString.split(" ")));
		try {
			if (!createIndexTokens.get(2).equals("on") || !createIndexString.contains("(")
					|| !createIndexString.contains(")") && createIndexTokens.size() < 4) {
				System.out.println("! Syntax Error");
				return;
			}

			String tableName = createIndexString
					.substring(createIndexString.indexOf("on") + 3, createIndexString.indexOf("(")).trim();
			String columnName = createIndexString
					.substring(createIndexString.indexOf("(") + 1, createIndexString.indexOf(")")).trim();

			// check if the index already exists
			if (new File(DavisBasePrompt.getNDXFilePath(tableName, columnName)).exists()) {
				System.out.println("! Index already exists");
				return;
			}
			
		
			RandomAccessFile tableFile = new RandomAccessFile(getTBLFilePath(tableName), "rw");

			TableMetaData metaData = new TableMetaData(tableName);

			if (!metaData.tableExists) {
				System.out.println("! Invalid Table name");
				tableFile.close();
				return;
			}

			int columnOrdinal = metaData.columnNames.indexOf(columnName);

			if (columnOrdinal < 0) {
				System.out.println("! Invalid column name");
				tableFile.close();
				return;
			}
         
             
         // create index file
			RandomAccessFile indexFile = new RandomAccessFile(getNDXFilePath(tableName, columnName), "rw");
			Page.addNewPage(indexFile, PageType.LEAFINDEX, -1, -1);


			if (metaData.recordCount > 0) {
				BPlusOneTree bPlusOneTree = new BPlusOneTree(tableFile, metaData.rootPageNo, metaData.tableName);
				for (int pageNo : bPlusOneTree.getAllLeaves()) {
					Page page = new Page(tableFile, pageNo);
					BTree bTree = new BTree(indexFile);
					for (TableRecord record : page.getPageRecords()) {
						bTree.insert(record.getAttributes().get(columnOrdinal), record.rowId);
					}
				}
			}

			System.out.println("* Index created on the column : " + columnName);
			indexFile.close();
			tableFile.close();

		} catch (IOException e) {

			System.out.println("! Error on creating Index");
			System.out.println(e);
		}

	}

	/**
	 * Stub method for dropping tables
	 * 
	 * @param dropTableString is a String of the user input
	 */
	public static void dropTable(String dropTableString) {
		System.out.println("STUB: This is the dropTable method.");
		System.out.println("\tParsing the string:\"" + dropTableString + "\"");
	}

	/**
	 * Stub method for executing queries
	 * 
	 * @param queryString is a String of the user input
	 */
	public static void parseQuery(String queryString) {
		String table_name = "";
		List<String> column_names = new ArrayList<String>();

		// Get table and column names for the select
		ArrayList<String> queryTableTokens = new ArrayList<String>(Arrays.asList(queryString.split(" ")));
		int i = 0;

		for (i = 1; i < queryTableTokens.size(); i++) {
			if (queryTableTokens.get(i).equals("from")) {
				++i;
				table_name = queryTableTokens.get(i);
				break;
			}
			if (!queryTableTokens.get(i).equals("*") && !queryTableTokens.get(i).equals(",")) {
				if (queryTableTokens.get(i).contains(",")) {
					ArrayList<String> colList = new ArrayList<String>(
							Arrays.asList(queryTableTokens.get(i).split(",")));
					for (String col : colList) {
						column_names.add(col.trim());
					}
				} else
					column_names.add(queryTableTokens.get(i));
			}
		}

		TableMetaData tableMetaData = new TableMetaData(table_name);
      if(!tableMetaData.tableExists){
         System.out.println("! Table does not exist");
         return;
      }
      
		Condition condition = null;
		try {

			condition = extractConditionFromQuery(tableMetaData, queryString);

		} catch (Exception e) {
			System.out.println(e.getMessage());
			return;
		}

		if (column_names.size() == 0) {
			column_names = tableMetaData.columnNames;
		}
		try {

			RandomAccessFile tableFile = new RandomAccessFile(getTBLFilePath(table_name), "r");
			DavisBaseBinaryFile tableBinaryFile = new DavisBaseBinaryFile(tableFile);
			tableBinaryFile.selectRecords(tableMetaData, column_names, condition);
			tableFile.close();
		} catch (IOException exception) {
			System.out.println("! Error selecting columns from table");
		}

	}

	/**
	 * Stub method for updating records
	 * 
	 * @param updateString is a String of the user input
	 */
	public static void parseUpdate(String updateString) {
		ArrayList<String> updateTokens = new ArrayList<String>(Arrays.asList(updateString.split(" ")));

		String table_name = updateTokens.get(1);
		List<String> columnsToUpdate = new ArrayList<>();
		List<String> valueToUpdate = new ArrayList<>();

		if (!updateTokens.get(2).equals("set") || !updateTokens.contains("=")) {
			System.out.println("! Syntax error");
			System.out.println(
					"Expected Syntax: UPDATE [table_name] SET [Column_name] = val1 where [column_name] = val2; ");
			return;
		}

		String updateColInfoString = updateString.split("set")[1].split("where")[0];

		List<String> column_newValueSet = Arrays.asList(updateColInfoString.split(","));

		for (String item : column_newValueSet) {
			columnsToUpdate.add(item.split("=")[0].trim());
			valueToUpdate.add(item.split("=")[1].trim().replace("\"", "").replace("'", ""));
		}

		TableMetaData metadata = new TableMetaData(table_name);

		if (!metadata.tableExists) {
			System.out.println("! Invalid Table name");
			return;
		}

		if (!metadata.columnExists(columnsToUpdate)) {
			System.out.println("! Invalid column name(s)");
			return;
		}

		Condition condition = null;
		try {

			condition = extractConditionFromQuery(metadata, updateString);

		} catch (Exception e) {
			System.out.println(e.getMessage());
			return;

		}

		try {
			RandomAccessFile file = new RandomAccessFile(getTBLFilePath(table_name), "rw");
			DavisBaseBinaryFile binaryFile = new DavisBaseBinaryFile(file);
			binaryFile.updateRecords(metadata, condition, columnsToUpdate, valueToUpdate);
			file.close();
		} catch (Exception e) {
			out.println("Unable to update the " + table_name + " file");
			out.println(e);

		}

	}

	public static void parseInsert(String queryString) {
		// INSERT INTO table_name ( columns ) VALUES ( values );
		ArrayList<String> insertTokens = new ArrayList<String>(Arrays.asList(queryString.split(" ")));

		if (!insertTokens.get(1).equals("into") || !queryString.contains(") values")) {
			System.out.println("! Syntax error");
			System.out.println("Expected Syntax: INSERT INTO table_name ( columns ) VALUES ( values );");

			return;
		}

		try {
			String tableName = insertTokens.get(2);
			if (tableName.trim().length() == 0) {
				System.out.println("! Tablename cannot be empty");
				return;
			}

			// parsing logic
			if (tableName.indexOf("(") > -1) {
				tableName = tableName.substring(0, tableName.indexOf("("));
			}
			TableMetaData dstMetaData = new TableMetaData(tableName);

			if (!dstMetaData.tableExists) {
				System.out.println("! Table does not exist.");
				return;
			}

			ArrayList<String> columnTokens = new ArrayList<String>(Arrays.asList(
					queryString.substring(queryString.indexOf("(") + 1, queryString.indexOf(") values")).split(",")));

			// Column List validation
			for (String colToken : columnTokens) {
				if (!dstMetaData.columnNames.contains(colToken.trim())) {
					System.out.println("! Invalid column : " + colToken.trim());
					return;
				}
			}

			String valuesString = queryString.substring(queryString.indexOf("values") + 6, queryString.length() - 1);

			ArrayList<String> valueTokens = new ArrayList<String>(Arrays
					.asList(valuesString.substring(valuesString.indexOf("(") + 1, valuesString.length()).split(",")));

			// fill attributes to insert
			List<Attribute> attributeToInsert = new ArrayList<>();

			for (ColumnInfo colInfo : dstMetaData.columnNameAttrs) {
				int i = 0;
				boolean columnProvided = false;
				for (i = 0; i < columnTokens.size(); i++) {
					if (columnTokens.get(i).trim().equals(colInfo.columnName)) {
						columnProvided = true;
						try {
							String value = valueTokens.get(i).replace("'", "").replace("\"", "").trim();
							if (valueTokens.get(i).trim().equals("null")) {
								if (!colInfo.isNullable) {
									System.out.println("! Cannot Insert NULL into " + colInfo.columnName);
									return;
								}
								colInfo.dataType = DataType.NULL;
								value = value.toUpperCase();
							}
							Attribute attr = new Attribute(colInfo.dataType, value);
							attributeToInsert.add(attr);
							break;
						} catch (Exception e) {
							System.out.println("! Invalid data format for " + columnTokens.get(i) + " values: "
									+ valueTokens.get(i));
							return;
						}
					}
				}
				if (columnTokens.size() > i) {
					columnTokens.remove(i);
					valueTokens.remove(i);
				}

				if (!columnProvided) {
					if (colInfo.isNullable)
						attributeToInsert.add(new Attribute(DataType.NULL, "NULL"));
					else {
						System.out.println("! Cannot Insert NULL into " + colInfo.columnName);
						return;
					}
				}
			}

			// insert attributes to the page
			RandomAccessFile dstTable = new RandomAccessFile(getTBLFilePath(tableName), "rw");
			int dstPageNo = BPlusOneTree.getPageNoForInsert(dstTable, dstMetaData.rootPageNo);
			Page dstPage = new Page(dstTable, dstPageNo);

			int rowNo = dstPage.addTableRow(tableName, attributeToInsert);

			// update Index
			if (rowNo != -1) {

				for (int i = 0; i < dstMetaData.columnNameAttrs.size(); i++) {
					ColumnInfo col = dstMetaData.columnNameAttrs.get(i);

					if (col.hasIndex) {
						RandomAccessFile indexFile = new RandomAccessFile(getNDXFilePath(tableName, col.columnName),
								"rw");
						BTree bTree = new BTree(indexFile);
						bTree.insert(attributeToInsert.get(i), rowNo);
					}

				}
			}

			dstTable.close();
			if (rowNo != -1)
				System.out.println("* Record Inserted");
			System.out.println();

		} catch (Exception ex) {
			System.out.println("! Error while inserting record");
			System.out.println(ex);

		}
	}

	/**
	 * Create new table
	 * 
	 * @param queryString is a String of the user input
	 */
	public static void parseCreateTable(String createTableString) {

		ArrayList<String> createTableTokens = new ArrayList<String>(Arrays.asList(createTableString.split(" ")));
		// table and () check
		if (!createTableTokens.get(1).equals("table")) {
			System.out.println("! Syntax Error");
			return;
		}
		String tableName = createTableTokens.get(2);
		if (tableName.trim().length() == 0) {
			System.out.println("! Tablename cannot be empty");
			return;
		}
		try {

			if (tableName.indexOf("(") > -1) {
				tableName = tableName.substring(0, tableName.indexOf("("));
			}

			List<ColumnInfo> lstcolumnInformation = new ArrayList<>();
			ArrayList<String> columnTokens = new ArrayList<String>(Arrays.asList(createTableString
					.substring(createTableString.indexOf("(") + 1, createTableString.length() - 1).split(",")));

			short ordinalPosition = 1;

			String primaryKeyColumn = "";

			for (String columnToken : columnTokens) {

				ArrayList<String> colInfoToken = new ArrayList<String>(Arrays.asList(columnToken.trim().split(" ")));
				ColumnInfo colInfo = new ColumnInfo();
				colInfo.tableName = tableName;
				colInfo.columnName = colInfoToken.get(0);
				colInfo.isNullable = true;
				colInfo.dataType = DataType.get(colInfoToken.get(1).toUpperCase());
				for (int i = 0; i < colInfoToken.size(); i++) {

					if ((colInfoToken.get(i).equals("null"))) {
						colInfo.isNullable = true;
					}
					if (colInfoToken.get(i).contains("not") && (colInfoToken.get(i + 1).contains("null"))) {
						colInfo.isNullable = false;
						i++;
					}

					if ((colInfoToken.get(i).equals("unique"))) {
						colInfo.isUnique = true;
					} else if (colInfoToken.get(i).contains("primary") && (colInfoToken.get(i + 1).contains("key"))) {
						colInfo.isPrimaryKey = true;
						colInfo.isUnique = true;
						colInfo.isNullable = false;
						primaryKeyColumn = colInfo.columnName;
						i++;
					}

				}
				colInfo.ordinalPosition = ordinalPosition++;
				lstcolumnInformation.add(colInfo);

			}

			// update sys file
			RandomAccessFile davisbaseTablesCatalog = new RandomAccessFile(
					getTBLFilePath(DavisBaseBinaryFile.tablesTable), "rw");
			TableMetaData davisbaseTableMetaData = new TableMetaData(DavisBaseBinaryFile.tablesTable);

			int pageNo = BPlusOneTree.getPageNoForInsert(davisbaseTablesCatalog, davisbaseTableMetaData.rootPageNo);

			Page page = new Page(davisbaseTablesCatalog, pageNo);

			int rowNo = page.addTableRow(DavisBaseBinaryFile.tablesTable,
					Arrays.asList(new Attribute[] { new Attribute(DataType.TEXT, tableName), // DavisBaseBinaryFile.tablesTable->test
							new Attribute(DataType.INT, "0"), new Attribute(DataType.SMALLINT, "0"),
							new Attribute(DataType.SMALLINT, "0") }));
			davisbaseTablesCatalog.close();

			if (rowNo == -1) {
				System.out.println("! Duplicate table Name");
				return;
			}
			RandomAccessFile tableFile = new RandomAccessFile(getTBLFilePath(tableName), "rw");
			Page.addNewPage(tableFile, PageType.LEAF, -1, -1);
			tableFile.close();

			RandomAccessFile davisbaseColumnsCatalog = new RandomAccessFile(
					getTBLFilePath(DavisBaseBinaryFile.columnsTable), "rw");
			TableMetaData davisbaseColumnsMetaData = new TableMetaData(DavisBaseBinaryFile.columnsTable);
			pageNo = BPlusOneTree.getPageNoForInsert(davisbaseColumnsCatalog, davisbaseColumnsMetaData.rootPageNo);

			Page page1 = new Page(davisbaseColumnsCatalog, pageNo);

			for (ColumnInfo column : lstcolumnInformation) {
				page1.addNewColumn(column);
			}

			davisbaseColumnsCatalog.close();

			System.out.println("* Table created");

			if (primaryKeyColumn.length() > 0) {
				parseCreateIndex("create index on " + tableName + "(" + primaryKeyColumn + ")");
			}
		} catch (Exception e) {

			System.out.println("! Error on creating Table");
			System.out.println(e.getMessage());
			parseDelete("delete from table " + DavisBaseBinaryFile.tablesTable + " where table_name = '" + tableName
					+ "' ");
			parseDelete("delete from table " + DavisBaseBinaryFile.columnsTable + " where table_name = '" + tableName
					+ "' ");
		}

	}

	/**
	 * Delete records from table
	 * 
	 * @param queryString is a String of the user input
	 */
	private static void parseDelete(String deleteTableString) {
		ArrayList<String> deleteTableTokens = new ArrayList<String>(Arrays.asList(deleteTableString.split(" ")));

		String tableName = "";

		try {

			if (!deleteTableTokens.get(1).equals("from") || !deleteTableTokens.get(2).equals("table")) {
				System.out.println("! Syntax Error");
				return;
			}

			tableName = deleteTableTokens.get(3);

			TableMetaData metaData = new TableMetaData(tableName);
			Condition condition = null;
			try {
				condition = extractConditionFromQuery(metaData, deleteTableString);

			} catch (Exception e) {
				System.out.println(e);
				return;
			}
			RandomAccessFile tableFile = new RandomAccessFile(getTBLFilePath(tableName), "rw");

			BPlusOneTree tree = new BPlusOneTree(tableFile, metaData.rootPageNo, metaData.tableName);
			List<TableRecord> deletedRecords = new ArrayList<TableRecord>();
			int count = 0;
			for (int pageNo : tree.getAllLeaves(condition)) {
				short deleteCountPerPage = 0;
				Page page = new Page(tableFile, pageNo);
				for (TableRecord record : page.getPageRecords()) {
					if (condition != null) {
						if (!condition.checkCondition(record.getAttributes().get(condition.columnOrdinal).fieldValue))
							continue;
					}

					deletedRecords.add(record);
					page.DeleteTableRecord(tableName,
							Integer.valueOf(record.pageHeaderIndex - deleteCountPerPage).shortValue());
					deleteCountPerPage++;
					count++;
				}
			}

			// update Index

			// if there is no condition, all the rows will be deleted.
			// so just delete the existing index files on the table and create new ones
			if (condition == null) {
				// TODO delete exisitng index files and create new ones;
				



			} else {
				for (int i = 0; i < metaData.columnNameAttrs.size(); i++) {
					if (metaData.columnNameAttrs.get(i).hasIndex) {
						RandomAccessFile indexFile = new RandomAccessFile(getNDXFilePath(tableName, metaData.columnNameAttrs.get(i).columnName), "rw");
						BTree bTree = new BTree(indexFile);
						for (TableRecord record : deletedRecords) {
							bTree.delete(record.getAttributes().get(i),record.rowId);
						}
					}
				}
			}

			System.out.println();
			tableFile.close();
			System.out.println(count + " record(s) deleted!");

		} catch (Exception e) {
			System.out.println("! Error on deleting rows in table : " + tableName);
			System.out.println(e.getMessage());
		}

	}

	public static String getTBLFilePath(String tableName) {
		return "data/" + tableName + ".tbl";
	}

	public static String getNDXFilePath(String tableName, String columnName) {
		return "data/" + tableName + "_" + columnName + ".ndx";
	}

	private static Condition extractConditionFromQuery(TableMetaData tableMetaData, String query) throws Exception {
		if (query.contains("where")) {
			Condition condition = new Condition(DataType.TEXT);
			String whereClause = query.substring(query.indexOf("where") + 6, query.length());
			ArrayList<String> whereClauseTokens = new ArrayList<String>(Arrays.asList(whereClause.split(" ")));

			// WHERE NOT column operator value
			if (whereClauseTokens.get(0).equalsIgnoreCase("not")) {
				condition.setNegation(true);
          }
        
          
          for (int i = 0; i < Condition.supportedOperators.length; i++) {
				if (whereClause.contains(Condition.supportedOperators[i])) {
					whereClauseTokens = new ArrayList<String>(
							Arrays.asList(whereClause.split(Condition.supportedOperators[i])));
				{	condition.setOperator(Condition.supportedOperators[i]);
			   	    condition.setConditionValue(whereClauseTokens.get(1).trim());
					condition.setColumName(whereClauseTokens.get(0).trim());
					break;
				}
				
				}
			}
          
									
			if (tableMetaData.tableExists
					&& tableMetaData.columnExists(new ArrayList<String>(Arrays.asList(condition.columnName)))) {
				condition.columnOrdinal = tableMetaData.columnNames.indexOf(condition.columnName);
				condition.dataType = tableMetaData.columnNameAttrs.get(condition.columnOrdinal).dataType;
			} else {
				throw new Exception(
						"! Invalid Table/Column : " + tableMetaData.tableName + " . " + condition.columnName);
			}
			return condition;
		} else
			return null;
	}

}