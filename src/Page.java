import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* This class handles all the page related logic*/
public class Page {

  public PageType pageType;
  short noOfCells;
  public int pageNo;
  short contentStartOffset;
  public int rightPage;
  public int parentPageNo;
  public boolean isRoot;
  public List<TableRecord> records;
  long pageStart;
  int lastRowId;
  short cellHeaderSize = 6;
  int availableSpace = 0;
  RandomAccessFile binaryFile;
  List<Integer> leftChildren;
  
  //Load a page from a file
  //Reads the page header from the page and fills the attributes
  public Page(RandomAccessFile file, int pageNo) {
    try 
    {
      this.pageNo = pageNo;
      records = new ArrayList<TableRecord>();
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
      isRoot = parentPageNo == -1;

      binaryFile.readShort();// 2 unused bytes

      //Load the table records
      if (pageType == PageType.LEAF)
        fillTableRecords();
      if(pageType == PageType.INTERIOR)
        fillLeftChildren(); //TODO

    } catch (IOException ex) {
      System.out.println("Error while reading the page " + ex.getMessage());
    }
  }

//increase the size of the file and add a page to it (Todo: handle overflow, page spliting and other functionalities)
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
        System.out.println("Error while adding new page" + ex.getMessage());
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
 
//adds a table row - this method converts the attributes into byte array and calls addNewTableRecord
public int addTableRow(String tableName,List<Attribute> attributes) throws IOException
  {
      List<Byte> colDataTypes = new ArrayList<Byte>();
      List<Byte> recordBody = new ArrayList<Byte>();

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

        short newCellOffset = addNewTableRecord(recordHeader.toArray(new Byte[recordHeader.size()]), 
                               recordBody.toArray(new Byte[recordBody.size()])
                                );

          //Add  the record to in memory records list 
          records.add(new TableRecord(
            lastRowId,newCellOffset,
            ByteConvertor.lsttobyteList(colDataTypes), 
            ByteConvertor.lsttobyteList(recordBody)));
       
           if(DavisBaseBinaryFile.dataStoreInitialized)
           {
             // TableMetaData metaData = new TableMetaData(tableName);
              TableMetaData.updateMetaData(tableName);
                     
           }
           return pageNo;
  }




//adds a new table record and updates the corresponding bytes in the page
  private short addNewTableRecord(Byte[] recordHeader, Byte[] recordBody) throws IOException
  {

        //if there is no space in the current page
      if(recordHeader.length + recordBody.length + 4 > availableSpace)
      {
        try{
        handleTableOverFlow();
      
      
        }
        catch(IOException e){
          System.out.println("Error while handleTableOverFlow");
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
    return newCellStart;
    
    }

 

  // This method creates new page and handles the overflow condition
  // TODO : This was not tested (not sure of the logic),
  // I wrote this based on my understanding from SDL
  // have to check with professor regarding B+1 tree
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
          newParentPageNo = newParentPage.addLeftChild(pageNo);
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
          parentPageNo = parentPage.addLeftChild(pageNo);

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
  }


//Add left child for the current page
  private int addLeftChild(int leftChildPageNo) throws IOException
  {
    if(pageType == PageType.INTERIOR)
    {
      List<Byte> recordHeader= new ArrayList<>();
      List<Byte> recordBody= new ArrayList<>();

      //increment rowid
      lastRowId++;
      recordHeader.addAll(Arrays.asList(ByteConvertor.intToBytes(leftChildPageNo)));
      recordBody.addAll(Arrays.asList(ByteConvertor.intToBytes(lastRowId)));

      addNewTableRecord(recordHeader.toArray(new Byte[recordHeader.size()]),
                                        recordBody.toArray(new Byte[recordBody.size()]));
    }
   return pageNo;

  }

//TODO -  in case of Interior page fill the left children of the current page into a list of Integers
  private void fillLeftChildren(){
    leftChildren = new ArrayList<>();


  }

  //Returns the right most child page for inserting new records
  public static int getPageNoForInsert(RandomAccessFile file,int rootPageNo)
  {
       Page rootPage = new Page(file,rootPageNo);
       if(rootPage.pageType!= PageType.LEAF 
            && rootPage.pageType!=PageType.LEAFINDEX)
        return rootPage.rightPage;
      else
        return rootPageNo;
       
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
    isRoot = newPage.isRoot;
    records = newPage.records;
    pageStart = newPage.pageStart;
    lastRowId = newPage.lastRowId;
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

//fills the list of rows in the page into a list object
  private void fillTableRecords() {
    short payLoadSize = 0;
    byte noOfcolumns = 0;
  
    try {
      for (int i = 0; i < noOfCells; i++) {
        binaryFile.seek(pageStart + 0x10 + (i *2) );
        short cellStart = binaryFile.readShort();
        binaryFile.seek(cellStart);

        payLoadSize = binaryFile.readShort();
        int rowId = binaryFile.readInt();
        noOfcolumns = binaryFile.readByte();
        
        if(lastRowId < rowId) lastRowId = rowId;
        
        byte[] colDatatypes = new byte[noOfcolumns];
        byte[] recordBody = new byte[payLoadSize - noOfcolumns - 1];

        binaryFile.read(colDatatypes);
        binaryFile.read(recordBody);

        TableRecord record = new TableRecord(rowId,cellStart,colDatatypes, recordBody);
        records.add(record);
      }
    } catch (IOException ex) {
      System.out.println("Error while filling records from the page" + ex.getMessage());
    }
  }
}
