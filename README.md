# davisbase
CS 6360.5U1 - Database Design: Project 

## TODO
### Features required for Part 1:
* Create Table
...7/18: Implemented by Hua/Arun; Testing thus far seems good.

* Insert Record 
...7/18: Implemented by Hua; Testing pre-merge worked with some minor caveats.
...* All attribute values inserted are inserted as TEXT - need to insert the correct data type.
...* TEXT is inserted as Unicode rather than ASCII. 
...Testing post-merge produced some bugs (see below).

* Query record(s) without a where clause - ie: SELECT [* or column_list] FROM [table name];
...7/18: `SELECT *` seems to work, need further testing when selecting individual columns.

* List all tables - ie: SHOW TABLES;
...7/18: Tested and in working order.

## BUGS
### In order of decreasing priority:
* 7/18 post-merge of branch Hua_davisbase and master:
...Setup:
...1. Clear data/
...2. `./run`
...3. Create a table (table schema is irrelevant)
...* Unable to `insert into` the newly created table - always returning "attribute can not be null".
...* `SELECT * FROM davisbase_columns` does not properly print the added columns for the newly created table.
...4. Create another table
...* With 4 tables, `SELECT * FROM davisbase_columns` print formatting is all wack. 

* 7/18 post-merge of branch Hua_davisbase and master: overflow logic
...Setup:
...1. Clear data/
...2. `./run`
...3. Create a table (table schema is irrelevant although preferably make just a few columns)
...Verify that `SELECT * FROM <tableName>` works properly.
...4. Create another table
...Notice that `SELECT * FROM <tableName1>` where `<tableName1>` is the table name of the first table no longer works properly.
...I believe this is likely due to the overflow logic causing us to lose a pointer somewhere (more below).
...Also notice when creating the two new tables, notice that davisbase_columns creates a new page for the two new tables, rather than creating a single new page and inserting the new records into the new page, when there's clearly enough space within the single new page (ie: no overflow should have occured).

* Parser logic should be improved so as not to require spaces between everything.
...ie: `create table tbl ( c1 int , c2 text );` should just be `create table tbl (c1 int, c2 text);`
