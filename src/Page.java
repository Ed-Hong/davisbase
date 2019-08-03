import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/* This class handles all the page related logic*/
public class Page {

  public PageType pageType;
  short noOfCells = 0;
  public int pageNo;
  short contentStartOffset;
  public int rightPage;
  public int parentPageNo;
  private List<TableRecord> records;
  boolean refreshTableRecords = false;
  long pageStart;
  int lastRowId;
  int availableSpace;
  RandomAccessFile binaryFile;
  List<TableInteriorRecord> leftChildren;

  public DataType indexValueDataType;
  public TreeSet<Long> lIndexValues;
  public TreeSet<String> sIndexValues;
  public HashMap<String,IndexRecord> indexValuePointer;
  private Map<Integer,TableRecord> recordsMap;
  
  //Load a page from a file
  //Reads the page header from the page and fills the attributes
  public Page(RandomAccessFile file, int pageNo) {
    try 
    {
      this.pageNo = pageNo;
      indexValueDataType = null;
      lIndexValues = new TreeSet<>();
      sIndexValues = new TreeSet<>();
      indexValuePointer = new HashMap<String,IndexRecord>();
      recordsMap = new HashMap<>();

      this.binaryFile = file;
      lastRowId = 0;
      pageStart = DavisBaseBinaryFile.pageSize * pageNo;
      binaryFile.seek(pageStart);
      pageType = PageType.get(binaryFile.readByte()); // pagetype
      binaryFile.readByte(); // unused
      noOfCells = binaryFile.readShort();
      contentStartOffset = binaryFile.readShort();
      availableSpace = contentStartOffset - 0x10 - (noOfCells *2); 

      rightPage = binaryFile.readInt();

      parentPageNo = binaryFile.readInt();

      binaryFile.readShort();// 2 unused bytes

      //Load the table records
      if (pageType == PageType.LEAF)
        fillTableRecords();
      if(pageType == PageType.INTERIOR)
        fillLeftChildren();
      if(pageType == PageType.INTERIORINDEX || pageType == PageType.LEAFINDEX)
        fillIndexRecords();
    
    } catch (IOException ex) {
      System.out.println("! Error while reading the page " + ex.getMessage());
    }
  }

  public List<String> getIndexValues()
  {
      List<String> strIndexValues = new ArrayList<>();

      if(sIndexValues.size() > 0)
        strIndexValues.addAll(Arrays.asList(sIndexValues.toArray(new String[sIndexValues.size()])));
       if(lIndexValues.size() > 0)
      {
        Long[] lArray = lIndexValues.toArray(new Long[lIndexValues.size()]);
                for(int i=0;i<lArray.length;i++)
        {
          strIndexValues.add(lArray[i].toString());
        }
            }
            
              return strIndexValues;

        
  }

  public boolean isRoot()
  {
    return parentPageNo == -1;
  }
  
  

  public static PageType getPageType(RandomAccessFile file,int pageNo) throws IOException
  {
    try 
    {
      int pageStart = DavisBaseBinaryFile.pageSize * pageNo;
      file.seek(pageStart);
      return  PageType.get(file.readByte()); 
    } catch (IOException ex) {
      System.out.println("! Error while getting the page type " + ex.getMessage());
      throw ex;
    }
  }

  public static int addNewPage(RandomAccessFile file,PageType pageType, int rightPage, int parentPageNo)
  {
    try 
    {
      int pageNo = Long.valueOf((file.length()/DavisBaseBinaryFile.pageSize)).intValue();
      file.setLength(file.length() + DavisBaseBinaryFile.pageSize);
      file.seek(DavisBaseBinaryFile.pageSize * pageNo);
      file.write(pageType.getValue());
      file.write(0x00); //unused
      file.writeShort(0); // no of cells
      file.writeShort((short)(DavisBaseBinaryFile.pageSize)); // cell start offset
     
      file.writeInt(rightPage);
      
      file.writeInt(parentPageNo);
      
      return pageNo;
    } 
    catch (IOException ex) 
    {
        System.out.println("! Error while adding new page" + ex.getMessage());
        return -1;
    }
  }

  public void updateRecord(TableRecord record,int ordinalPosition,Byte[] newValue) throws IOException
  {
    binaryFile.seek(pageStart + record.recordOffset + 7);
    int valueOffset = 0;
    for(int i=0;i<ordinalPosition;i++)
    {
    
      valueOffset+= DataType.getLength((byte)binaryFile.readByte());
    }

    binaryFile.seek(pageStart + record.recordOffset + 7 + record.colDatatypes.length + valueOffset);
    binaryFile.write(ByteConvertor.Bytestobytes(newValue));
      
  }


  public void addNewColumn(ColumnInfo columnInfo) throws IOException
  {
    try {
      addTableRow(DavisBaseBinaryFile.columnsTable, Arrays.asList(new Attribute[] { 
        new Attribute(DataType.TEXT, columnInfo.tableName),
        new Attribute(DataType.TEXT, columnInfo.columnName),
        new Attribute(DataType.TEXT, columnInfo.dataType.toString()),
        new Attribute(DataType.SMALLINT, columnInfo.ordinalPosition.toString()), 
        new Attribute(DataType.TEXT, columnInfo .isNullable ? "YES":"NO"),
        columnInfo .isPrimaryKey ? 
        new Attribute(DataType.TEXT, "PRI") : new Attribute(DataType.NULL, "NULL") ,
        new Attribute(DataType.TEXT, columnInfo .isUnique ? "YES": "NO")
       })); 
    } catch (Exception e) {
      System.out.println("! Could not add column " + columnInfo.columnName);
    }
  }
 
//adds a table row - this method converts the attributes into byte array and calls addNewPageRecord
public int addTableRow(String tableName,List<Attribute> attributes) throws IOException
  {
      List<Byte> colDataTypes = new ArrayList<Byte>();
      List<Byte> recordBody = new ArrayList<Byte>();

      TableMetaData metaData  = null;
      if(DavisBaseBinaryFile.dataStoreInitialized)
      {
        metaData = new TableMetaData(tableName);
        if(!metaData.validateInsert(attributes))
            return -1;
      }

      for(Attribute attribute : attributes)
      {
        //add value for the record body
        recordBody.addAll(Arrays.asList(attribute.fieldValueByte));
       
        //Fill column Datatype for every attribute in the row
        if(attribute.dataType == DataType.TEXT)
          {
             colDataTypes.add(Integer.valueOf(DataType.TEXT.getValue() + (new String(attribute.fieldValue).length())).byteValue());
          }
        else
          {
              colDataTypes.add(attribute.dataType.getValue());
          }
        }

        lastRowId++;

        //calculate pay load size
        short payLoadSize = Integer.valueOf(recordBody.size() + 
                                  colDataTypes.size() + 1).shortValue();
       
        //create record header
        List<Byte> recordHeader = new ArrayList<>();

        recordHeader.addAll(Arrays.asList(ByteConvertor.shortToBytes(payLoadSize)));  //payloadSize
        recordHeader.addAll(Arrays.asList(ByteConvertor.intToBytes(lastRowId))); //rowid
        recordHeader.add(Integer.valueOf(colDataTypes.size()).byteValue()); //number of columns
        recordHeader.addAll(colDataTypes); //column data types

         addNewPageRecord(recordHeader.toArray(new Byte[recordHeader.size()]), 
                               recordBody.toArray(new Byte[recordBody.size()])
                                );

         refreshTableRecords = true;
         if(DavisBaseBinaryFile.dataStoreInitialized)
         {
            metaData.recordCount++;
            metaData.updateMetaData();
         }
           return lastRowId;
  }
  
public List<TableRecord> getPageRecords(){

if(refreshTableRecords)
   fillTableRecords();
   
   refreshTableRecords = false;
   
   return records;
}

  private void DeletePageRecord(short recordIndex)
  {
    try{

      for (int i = recordIndex + 1; i < noOfCells; i++) { 
      binaryFile.seek(pageStart + 0x10 + (i *2) );
      short cellStart = binaryFile.readShort();
      
      if(cellStart == 0)
          continue;
          
      binaryFile.seek(pageStart + 0x10 + ((i-1) *2));
      binaryFile.writeShort(cellStart);
      }

      noOfCells--;
      
      binaryFile.seek(pageStart + 2); 
      binaryFile.writeShort(noOfCells);

    }
    catch(IOException e){
      System.out.println("! Error while deleting record at "+ recordIndex + "in page " + pageNo);
    }
  }

  public void DeleteTableRecord(String tableName, short recordIndex)
  {
    DeletePageRecord(recordIndex);
    TableMetaData metaData = new TableMetaData(tableName);    
    metaData.recordCount --;
    metaData.updateMetaData();
    refreshTableRecords = true;

  }

//adds a new record and updates the page header accordingly
  private void addNewPageRecord(Byte[] recordHeader, Byte[] recordBody) throws IOException
  {

        //if there is no space in the current page
      if(recordHeader.length + recordBody.length + 4 > availableSpace)
      {
        try{
          if(pageType == PageType.LEAF || pageType == PageType.INTERIOR)
           {
              handleTableOverFlow();
           }
          else
          {  
            handleIndexOverflow();
            return;
          }
        }
        catch(IOException e){
          System.out.println("! Error while handleTableOverFlow");
        }
      }
    
    short cellStart =  contentStartOffset;
    
                        
    short newCellStart  = Integer.valueOf((cellStart - recordBody.length  - recordHeader.length - 2)).shortValue();
    binaryFile.seek(pageNo * DavisBaseBinaryFile.pageSize + newCellStart);
  
    //record head
    binaryFile.write(ByteConvertor.Bytestobytes(recordHeader)); // datatypes

    //record body
    binaryFile.write(ByteConvertor.Bytestobytes(recordBody));

    binaryFile.seek(pageStart + 0x10 + (noOfCells * 2));
    binaryFile.writeShort(newCellStart);
    
    contentStartOffset = newCellStart;
    
    binaryFile.seek(pageStart + 4); binaryFile.writeShort(contentStartOffset);

    noOfCells++;
    binaryFile.seek(pageStart + 2); binaryFile.writeShort(noOfCells);
    
    availableSpace = contentStartOffset - 0x10 - (noOfCells *2);
    
    }
    
    private boolean idxPageCleaned;

    private void handleIndexOverflow() throws IOException
    {
     if(pageType == PageType.LEAFINDEX)
     {
       //if currrent page is root
       if(parentPageNo == -1)
       {
        //create a new interior Parent root Page
        parentPageNo = addNewPage(binaryFile, PageType.INTERIORINDEX, pageNo , -1);
       }
       //create a new left Page 
       int newLeftLeafPageNo = addNewPage(binaryFile, PageType.LEAFINDEX,pageNo, parentPageNo);

       //set the new parentPage as parent for the current page 
       setParent(parentPageNo);
      
       IndexNode incomingInsertTemp = this.incomingInsert;
       //Split the index records
     
    // Insert half the items into leftchild page
       Page leftLeafPage = new Page(binaryFile, newLeftLeafPageNo);
       //call the split method
       IndexNode toInsertParentIndexNode = splitIndexRecordsBetweenPages(leftLeafPage);
      
       //Insert Middle record to the parent page with left page No

       Page parentPage = new Page(binaryFile,parentPageNo);
     
       //shift page based on the incoming index value
       int comparisonResult= Condition.compare(incomingInsertTemp.indexValue.fieldValue,toInsertParentIndexNode.indexValue.fieldValue,incomingInsert.indexValue.dataType);
       
       if(comparisonResult == 0)
       {
          toInsertParentIndexNode.rowids.addAll(incomingInsertTemp.rowids);
          parentPage.addIndex(toInsertParentIndexNode,newLeftLeafPageNo);
          shiftPage(parentPage);
          return;
       }
      else if(comparisonResult < 0)
      {
          leftLeafPage.addIndex(incomingInsertTemp);
          shiftPage(leftLeafPage);
       }
       else
       {
          addIndex(incomingInsertTemp);
       }

       parentPage.addIndex(toInsertParentIndexNode,newLeftLeafPageNo);

      }
     
     else{
      //multilevel split - split on interior page
       //create a new interior Parent root Page
       
       if(noOfCells < 3 && !idxPageCleaned)
       {
          idxPageCleaned = true;
          String[] indexValuesTemp = getIndexValues().toArray(new String[getIndexValues().size()]);
          HashMap<String, IndexRecord> indexValuePointerTemp = (HashMap<String, IndexRecord>) indexValuePointer.clone();
          IndexNode incomingInsertTemp = this.incomingInsert;
           cleanPage();
          for (int i = 0; i < indexValuesTemp.length; i++) {
            addIndex(indexValuePointerTemp.get(indexValuesTemp[i]).getIndexNode(),indexValuePointerTemp.get(indexValuesTemp[i]).leftPageNo);
          }
          
          addIndex(incomingInsertTemp);
          return;
       }

       if(idxPageCleaned)
       {
         System.out.println("! Page overflow, increase the page size. Reached Max number of rows for an Index value");
         return;
       }

      
       
       if(parentPageNo == -1)
       {
        parentPageNo = addNewPage(binaryFile, PageType.INTERIORINDEX, pageNo , -1);
       }
       //create a new Interior Page 
       int newLeftInteriorPageNo = addNewPage(binaryFile, PageType.INTERIORINDEX, pageNo, parentPageNo );

       
       //set the new parentPage as parent for the current page 
       setParent(parentPageNo);

       IndexNode incomingInsertTemp = this.incomingInsert;
       //Split the index records
     
        //Insert half the items into leftchild page
        Page leftInteriorPage = new Page(binaryFile, newLeftInteriorPageNo);
        
        IndexNode toInsertParentIndexNode = splitIndexRecordsBetweenPages(leftInteriorPage);

        Page parentPage = new Page(binaryFile,parentPageNo);
       //shift page based on the incoming index value
       int comparisonResult= Condition.compare(incomingInsertTemp.indexValue.fieldValue,toInsertParentIndexNode.indexValue.fieldValue,incomingInsert.indexValue.dataType);
       

       //add the middle Orphan to the left page
       Page middleOrphan = new Page(binaryFile,toInsertParentIndexNode.leftPageNo);
       middleOrphan.setParent(parentPageNo);
       leftInteriorPage.setRightPageNo(middleOrphan.pageNo);
   
       if(comparisonResult == 0)
       {
          toInsertParentIndexNode.rowids.addAll(incomingInsertTemp.rowids);
          parentPage.addIndex(toInsertParentIndexNode,newLeftInteriorPageNo);
          shiftPage(parentPage);
          return;
       }
       else if(comparisonResult < 0)
       {
        leftInteriorPage.addIndex(incomingInsertTemp);
        shiftPage(leftInteriorPage);
       }
       else
       {  
          addIndex(incomingInsertTemp);
       }     
  
       parentPage.addIndex(toInsertParentIndexNode,newLeftInteriorPageNo);

     }
     
     
     
    }
   // clean the current(rightleaf) page by resetting the page offsets and no of
    // records,
    private void cleanPage() throws IOException {

      noOfCells = 0;
      contentStartOffset = Long.valueOf(DavisBaseBinaryFile.pageSize).shortValue();
      availableSpace = contentStartOffset - 0x10 - (noOfCells * 2); // this page will now be treated as a new page
      byte[] emptybytes = new byte[512-16];
      Arrays.fill(emptybytes, (byte) 0 );
      binaryFile.seek(pageStart + 16);
      binaryFile.write(emptybytes);
      binaryFile.seek(pageStart + 2);
      binaryFile.writeShort(noOfCells);
      binaryFile.seek(pageStart + 4);
      binaryFile.writeShort(contentStartOffset);
      lIndexValues = new TreeSet<>();
      sIndexValues = new TreeSet<>();
      indexValuePointer = new HashMap<>();

    }


    //copies half to left page and rewrite the current right page with remaining half records
  // returns the middle index Node which should be added to the parent
  private IndexNode splitIndexRecordsBetweenPages(Page newleftPage) throws IOException {

    try{
    int mid = getIndexValues().size() / 2;
    String[] indexValuesTemp = getIndexValues().toArray(new String[getIndexValues().size()]);

    IndexNode toInsertParentIndexNode = indexValuePointer.get(indexValuesTemp[mid]).getIndexNode();
    toInsertParentIndexNode.leftPageNo = indexValuePointer.get(indexValuesTemp[mid]).leftPageNo;
  
    HashMap<String, IndexRecord> indexValuePointerTemp = (HashMap<String, IndexRecord>) indexValuePointer.clone();

    for (int i = 0; i < mid; i++) {
      newleftPage.addIndex(indexValuePointerTemp.get(indexValuesTemp[i]).getIndexNode(),indexValuePointerTemp.get(indexValuesTemp[i]).leftPageNo);
    }

    cleanPage();
    sIndexValues = new TreeSet<>();
    lIndexValues = new TreeSet<>();
    indexValuePointer = new HashMap<String, IndexRecord>();

    //Insert the other half into right child page
    for(int i=mid+1;i<indexValuesTemp.length;i++)
    {  
       addIndex(indexValuePointerTemp.get(indexValuesTemp[i]).getIndexNode(),indexValuePointerTemp.get(indexValuesTemp[i]).leftPageNo);
    }
  
    return toInsertParentIndexNode;
  }
  catch(IOException e)
  {
    System.out.println("! Insert into Index File failed. Error while splitting index pages");
    throw e;
  }

  }

  private void handleTableOverFlow() throws IOException
  {
    if(pageType == PageType.LEAF)
      {
         //create a new leaf page
        int newRightLeafPageNo = addNewPage(binaryFile,pageType,-1,-1);

        //if the current leaf page is root
        if(parentPageNo == -1){
        
          //create new parent page
           
           int newParentPageNo = addNewPage(binaryFile, PageType.INTERIOR,
            newRightLeafPageNo, -1);

          //set the new leaf page as right sibling to the current page
          setRightPageNo(newRightLeafPageNo);
          //set the newly created parent page as parent to the current page
          setParent(newParentPageNo);

          //Add the current page as left child for the parent
          Page newParentPage = new Page(binaryFile,newParentPageNo);
          newParentPageNo = newParentPage.addLeftTableChild(pageNo,lastRowId);
          //add the newly created leaf page as rightmost child of the parent
          newParentPage.setRightPageNo(newRightLeafPageNo);


          //add the newly created parent page as parent to newly created right page
          Page newLeafPage = new Page(binaryFile,newRightLeafPageNo);
          newLeafPage.setParent(newParentPageNo);

          //make the current page as newly created page for further operations
          shiftPage(newLeafPage);
        }
        else
        {
          //Add the current page as left child for the parent
          Page parentPage = new Page(binaryFile,parentPageNo);
          parentPageNo = parentPage.addLeftTableChild(pageNo,lastRowId);

          //add the newly created leaf page as rightmost child of the parent
          parentPage.setRightPageNo(newRightLeafPageNo);

          //set the new leaf page as right sibling to the current page
          setRightPageNo(newRightLeafPageNo);

          //add the parent page as parent to newly created right page
          Page newLeafPage = new Page(binaryFile,newRightLeafPageNo);
          newLeafPage.setParent(parentPageNo);

          //make the current page as newly created page for further operations
          shiftPage(newLeafPage);
        }
      }
      else {
        //create a new leaf page
        int newRightLeafPageNo = addNewPage(binaryFile,pageType,-1,-1);

        //create new parent page
           
           int newParentPageNo = addNewPage(binaryFile, PageType.INTERIOR,
            newRightLeafPageNo, -1);
            
             //set the new leaf page as right sibling to the current page
          setRightPageNo(newRightLeafPageNo);
          
           //set the newly created parent page as parent to the current page
          setParent(newParentPageNo);
          
          //Add the current page as left child for the parent
          Page newParentPage = new Page(binaryFile,newParentPageNo);
          newParentPageNo = newParentPage.addLeftTableChild(pageNo,lastRowId);
          //add the newly created leaf page as rightmost child of the parent
          newParentPage.setRightPageNo(newRightLeafPageNo);


          //add the newly created parent page as parent to newly created right page
          Page newLeafPage = new Page(binaryFile,newRightLeafPageNo);
          newLeafPage.setParent(newParentPageNo);

          //make the current page as newly created page for further operations
          shiftPage(newLeafPage);
      }
  }


//Add left child for the current page
  private int addLeftTableChild(int leftChildPageNo,int rowId) throws IOException
  {
    for( TableInteriorRecord intRecord: leftChildren)
    {
      if(intRecord.rowId == rowId)
        return pageNo;
    }
    if(pageType == PageType.INTERIOR)
    {
      List<Byte> recordHeader= new ArrayList<>();
      List<Byte> recordBody= new ArrayList<>();

      recordHeader.addAll(Arrays.asList(ByteConvertor.intToBytes(leftChildPageNo)));
      recordBody.addAll(Arrays.asList(ByteConvertor.intToBytes(rowId)));

      addNewPageRecord(recordHeader.toArray(new Byte[recordHeader.size()]),
                                        recordBody.toArray(new Byte[recordBody.size()]));
    }
   return pageNo;

  }


  //Copies all the members from the new page to the current page
  private void shiftPage(Page newPage)
  {
    pageType = newPage.pageType;
    noOfCells = newPage.noOfCells;
    pageNo = newPage.pageNo;
    contentStartOffset = newPage.contentStartOffset;
    rightPage = newPage.rightPage;
    parentPageNo = newPage.parentPageNo;
    leftChildren = newPage.leftChildren;
    sIndexValues = newPage.sIndexValues;
    lIndexValues = newPage.lIndexValues;
    indexValuePointer = newPage.indexValuePointer;
    records = newPage.records;
    pageStart = newPage.pageStart;
    availableSpace = newPage.availableSpace;
  }



  //sets the parentPageNo as parent for the current page
 public void setParent(int parentPageNo) throws IOException
 {
    binaryFile.seek(DavisBaseBinaryFile.pageSize * pageNo + 0x0A);
    binaryFile.writeInt(parentPageNo);
    this.parentPageNo = parentPageNo;
 }

//sets the rightPageNo as rightPageNo (right sibling or right most child) for the current page
 public void setRightPageNo(int rightPageNo) throws IOException
 {
    binaryFile.seek(DavisBaseBinaryFile.pageSize * pageNo + 0x06);
    binaryFile.writeInt(rightPageNo);
    this.rightPage = rightPageNo;
 }

 public void DeleteIndex(IndexNode node) throws IOException
 {
    DeletePageRecord(indexValuePointer.get(node.indexValue.fieldValue).pageHeaderIndex);
    fillIndexRecords();
    refreshHeaderOffset();
 }

 public void addIndex(IndexNode node) throws IOException
 {
    addIndex(node,-1);
 }

 private IndexNode incomingInsert;
 public void addIndex(IndexNode node,int leftPageNo) throws IOException
 {
  incomingInsert = node;
  incomingInsert.leftPageNo = leftPageNo;
  List<Integer> rowIds = new ArrayList<>();
  
  //If index already exists, delete the old one, add the new rowid to the array and insert
  List<String> ixValues = getIndexValues();
  if(getIndexValues().contains(node.indexValue.fieldValue))
  {
      leftPageNo = indexValuePointer.get(node.indexValue.fieldValue).leftPageNo;
      incomingInsert.leftPageNo = leftPageNo;
      rowIds = indexValuePointer.get(node.indexValue.fieldValue).rowIds;
      rowIds.addAll(incomingInsert.rowids);
      incomingInsert.rowids = rowIds;
      DeletePageRecord(indexValuePointer.get(node.indexValue.fieldValue).pageHeaderIndex);
      if(indexValueDataType == DataType.TEXT || indexValueDataType == null || node.indexValue.fieldValue.toUpperCase().equals("NULL"))
        sIndexValues.remove(node.indexValue.fieldValue);
      else
        lIndexValues.remove(Long.parseLong(node.indexValue.fieldValue));
  }

     rowIds.addAll(node.rowids);

     rowIds = new ArrayList<>(new HashSet<>(rowIds));

    List<Byte> recordHead = new ArrayList<>(); 
    List<Byte> recordBody = new ArrayList<>();

    //no of row ids
    recordBody.addAll(Arrays.asList(Integer.valueOf(rowIds.size()).byteValue()));

    //index data type

    if(node.indexValue.dataType == DataType.TEXT)
      recordBody.add(Integer.valueOf(node.indexValue.dataType.getValue() 
                                + node.indexValue.fieldValue.length()).byteValue());
    else
      recordBody.add(node.indexValue.dataType.getValue());
 
    //index value
    recordBody.addAll(Arrays.asList(node.indexValue.fieldValueByte));


    //list of rowids
    for(int i=0;i<rowIds.size();i++)
    {
      recordBody.addAll(Arrays.asList(ByteConvertor.intToBytes(rowIds.get(i))));
    } 

    short payload = Integer.valueOf(recordBody.size()).shortValue();
    if(pageType == PageType.INTERIORINDEX)
        recordHead.addAll(Arrays.asList(ByteConvertor.intToBytes(leftPageNo)));

    recordHead.addAll(Arrays.asList(ByteConvertor.shortToBytes(payload)));

    addNewPageRecord(recordHead.toArray(new Byte[recordHead.size()]), 
                                                  recordBody.toArray(new Byte[recordBody.size()])
     );

    fillIndexRecords();
    refreshHeaderOffset();
    
 }

private void refreshHeaderOffset()
{
  try {
  binaryFile.seek(pageStart + 0x10);
  for(String indexVal : getIndexValues())
  {
    binaryFile.writeShort(indexValuePointer.get(indexVal).pageOffset);
  }

} catch (IOException ex) {
  System.out.println("! Error while refrshing header offset " + ex.getMessage());
}
}


//fills the list of rows in the page into a list object
  private void fillTableRecords() {
    short payLoadSize = 0;
    byte noOfcolumns = 0;
    records = new ArrayList<TableRecord>();
    recordsMap =  new HashMap<>();
    try {
      for (short i = 0; i < noOfCells; i++) {
        binaryFile.seek(pageStart + 0x10 + (i *2) );
        short cellStart = binaryFile.readShort();
        if(cellStart == 0)
          continue;
        binaryFile.seek(pageStart + cellStart);

        payLoadSize = binaryFile.readShort();
        int rowId = binaryFile.readInt();
        noOfcolumns = binaryFile.readByte();
        
        if(lastRowId < rowId) lastRowId = rowId;
        
        byte[] colDatatypes = new byte[noOfcolumns];
        byte[] recordBody = new byte[payLoadSize - noOfcolumns - 1];

        binaryFile.read(colDatatypes);
        binaryFile.read(recordBody);

        TableRecord record = new TableRecord(i, rowId, cellStart
                                              , colDatatypes, recordBody);
        records.add(record);
        recordsMap.put(rowId, record);
      }
    } catch (IOException ex) {
      System.out.println("! Error while filling records from the page " + ex.getMessage());
    }
  }

// In case of Interior page fill the left children of the current page into a list of Integers
private void fillLeftChildren(){
  try {
    leftChildren = new ArrayList<>();
  
    int leftChildPageNo = 0;
    int rowId =0;
    for (int i = 0; i < noOfCells; i++) {
      binaryFile.seek(pageStart + 0x10 + (i *2) );
      short cellStart = binaryFile.readShort();
      if(cellStart == 0)//ignore deleted cells
        continue;
      binaryFile.seek(pageStart + cellStart);

      leftChildPageNo = binaryFile.readInt();
      rowId = binaryFile.readInt();
      leftChildren.add(new TableInteriorRecord(rowId, leftChildPageNo));
    }
  } catch (IOException ex) {
    System.out.println("! Error while filling records from the page " + ex.getMessage());
  }

}

private void fillIndexRecords(){
  try {
    lIndexValues = new TreeSet<>();
    sIndexValues = new TreeSet<>();
    indexValuePointer = new HashMap<>();

    int leftPageNo = -1;
    byte noOfRowIds = 0;
    byte dataType = 0;
    for (short i = 0; i < noOfCells; i++) {
      binaryFile.seek(pageStart + 0x10 + (i *2) );
      short cellStart = binaryFile.readShort();
      if(cellStart == 0)//ignore deleted cells
        continue;
      binaryFile.seek(pageStart + cellStart);

      if(pageType == PageType.INTERIORINDEX)
         leftPageNo = binaryFile.readInt();
     
      short payload = binaryFile.readShort(); // payload

      noOfRowIds = binaryFile.readByte();
      dataType = binaryFile.readByte();
     
      if(indexValueDataType == null && DataType.get(dataType) != DataType.NULL)
        indexValueDataType = DataType.get(dataType);

      byte[] indexValue = new byte[DataType.getLength(dataType)];
      binaryFile.read(indexValue);

      List<Integer> lstRowIds = new ArrayList<>();
      for(int j=0;j<noOfRowIds;j++)
      {
          lstRowIds.add(binaryFile.readInt());
      }

      IndexRecord record = new IndexRecord(i, DataType.get(dataType),noOfRowIds, indexValue
                                        , lstRowIds,leftPageNo,rightPage,pageNo,cellStart);
      
      if(indexValueDataType == DataType.TEXT || indexValueDataType == null || record.getIndexNode().indexValue.fieldValue.toUpperCase().equals("NULL"))
        sIndexValues.add(record.getIndexNode().indexValue.fieldValue);
      else
        lIndexValues.add(Long.parseLong(record.getIndexNode().indexValue.fieldValue));

      indexValuePointer.put(record.getIndexNode().indexValue.fieldValue, record);

    }
  } catch (IOException ex) {
    System.out.println("! Error while filling records from the page " + ex.getMessage());
  }
} 

}
