# DavisBase Project: Part 2 TODO
# Team Yellow

----------

## Features Required:
* Primary Key column definition as part of Create Table.
    * Only need to support Primary Key on a single column
    * Test attempting to insert duplicate key
* Create Index (only need to support creating an index on a single column)
    * Note that when a table is created with a column designated as the primary key, an index will be implicitly and automatically created on that column.
* Update record
    * Any updates to columns with fixed-sized data types should be done "in place". 
    * Any update to a text/string column that results in a longer string should delete the original record from the B+1 tree and recreate the record with the longer string value as a new rowid at the far right leaf of the tree.
    * All indexes in the table, including primary keys should be updated to point to the new rowid.
* Delete record
    * Simply remove the cell offset from the table header and shift the remaining offsets to close the gap.
    * It is not necessary to physically remove the record data or move any other records from the body of the page.
* Drop Table
    * (1) Remove a table file, (2) all of its associated indexes, and (3) references to the table in the meta-data tables.
* Querying using WHERE clause ie: SELECT * FROM table WHERE column = value;


## Extra Features (if we have time):
* Batch capability (Matt is currently working on this).
* Support for AND, OR within WHERE clause, allowing for multi-column queries.
* Allowing omission of a condition for update/delete?
* FK constraints.
* Join?

----------

## Bugs:
* ~~CREATE INDEX should not be permitted when an index for the column already exists.~~ FIXED

----------

## Features Supported	
* INSERT performs data type validation and will ABORT any invalid insertions.	

 * Nullable Columns: NULL may only be inserted into a nullable column, columns by default are nullable, and default value for a nullable column is NULL.	

 * Unique Columns: Columns by default are NOT unique. Attempting to INSERT a duplicate value into a unique column will fail.	

 * MetaData Updates: Record Count in `davisbase_tables.tbl` is updated on INSERT.	

 * Display RowId: `SHOW ROWID;` will enable RowId to be displayed on `SELECT`;

----------

## Design Assumptions:
* `Date` expects the following format when inserting: `YYYY-MM-DD`.

* `Time` expects milliseconds after midnight when inserting, but will display as `hh:mm:ss` in 24-hour time or military time. 

* `DateTime` expects the following format when inserting: `YYYY-MM-DD_hh:mm:ss`.

* `Year` expects the following format when inserting: `YYYY`. Note that we do not validate whether the given value falls within the range `[1872, 2127]`, so values outside this range will experience over/underflow.

* When inserting, `INSERT INTO <table> (<column_list>) VALUES (<value_list>)`, the given `<column_list>` will match on the columns of `<table>` using the NAMES of the columns, ie: it does not matter the order of the columns passed in `<column_list>` - just the names must match the actual column names. 

* When inserting, `INSERT INTO <table> (<column_list>) VALUES (<value_list>)`, the given `<value_list>` is comma delimited, and strings need not be in quotes - quotes will be dropped. This means you cannot insert the string `NULL` into a nullable text column. 