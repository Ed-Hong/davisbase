import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Page {

  public PageType pageType;
  short noOfCells;
  public int pageNo;
  short contentStartOffset;
  public int rightSiblingPageNo;
  public int rightmostChildPageNo;
  public int parentPageNo;
  public boolean isRoot;
  public List<TableRecord> records;
  long pageStart;
  int lastRowId;
  short cellHeaderSize = 6;

  RandomAccessFile binaryFile;

//Load a page from a file
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

      if (pageType == PageType.INTERIOR || pageType == PageType.INTERIORINDEX)
        rightmostChildPageNo = binaryFile.readInt();
      else
        rightSiblingPageNo = binaryFile.readInt();

      parentPageNo = binaryFile.readInt();
      isRoot = parentPageNo == -1;

      binaryFile.readShort();// 2 unused bytes

      if (pageType == PageType.LEAF)
        fillTableRecords();
    } catch (IOException ex) {
      System.out.println("Error while reading the page" + ex.getMessage());
    }
  }

//increase the size of the file and add a page to it (Todo: handle overflow, page spliting and other functionalities)
  public static void addNewPage(RandomAccessFile file,PageType pageType, int pageNo,int rightmostChild,int rightSibling, int parent)
  {
    try 
    {
      file.setLength(file.length() + DavisBaseBinaryFile.pageSize);
      file.seek(DavisBaseBinaryFile.pageSize * pageNo);
      file.write(pageType.getValue());
      file.write(0x00); //unused
      file.writeShort(0); // no of cells
      file.writeShort((short)(DavisBaseBinaryFile.pageSize * pageNo + DavisBaseBinaryFile.pageSize)); // cell start offset
     
      if (pageType == PageType.INTERIOR || pageType == PageType.INTERIORINDEX)
        file.writeInt(rightmostChild);
      else
        file.writeInt(rightSibling);
      
      file.writeInt(parent);
    } 
    catch (IOException ex) 
    {
        System.out.println("Error while adding new page" + ex.getMessage());
    }
  }

//adds a table row - this method converts the attributes into byte array and sends it to addNewTableRecord
public void addTableRow(List<Attribute> attributes)
  {
      List<Byte> colDataTypes = new ArrayList<Byte>();
      List<Byte> recordBody = new ArrayList<Byte>();

      for(Attribute attribute : attributes)
      {
        if(attribute.dataType == DataType.TEXT)
          {
             colDataTypes.add(Integer.valueOf(DataType.TEXT.getValue() + (new String(attribute.field).length())).byteValue());
             recordBody.addAll(Arrays.asList(TableRecord.byteToBytes(attribute.field.getBytes())));
          }
        else
          {
              colDataTypes.add(attribute.dataType.getValue());
              
              switch(attribute.dataType.getValue()){
                case 0: 
                  break;
                case 1:
                  //TODO check how to convert tinyint
                  break;
                case 2:
                  recordBody.addAll(Arrays.asList(TableRecord.shortToBytes(new Short(attribute.field)))); break;
                case 3:
                   recordBody.addAll(Arrays.asList(TableRecord.intToBytes(new Integer (attribute.field)))); break;
                case 4:
                  recordBody.addAll(Arrays.asList(TableRecord.longToBytes(new Long (attribute.field)))); break;
                case 5:
                  recordBody.addAll(Arrays.asList(TableRecord.floatToBytes(new Float (attribute.field)))); break;
                case 6:
                  recordBody.addAll(Arrays.asList(TableRecord.doubleToBytes(new Double (attribute.field)))); break;
              }

          }
                   
        }
        addNewTableRecord(TableRecord.Bytestobytes(colDataTypes.toArray(new Byte[colDataTypes.size()])), 
                            TableRecord.Bytestobytes(recordBody.toArray(new Byte[recordBody.size()])),
                            Integer.valueOf(recordBody.size() + colDataTypes.size() + 1).shortValue());
  }

//adds a new table record and updates the corresponding bytes in the page
  private void addNewTableRecord(byte[] colDatatypes, byte[] recordBody, short payLoadSize)
  {
    try {
    lastRowId = lastRowId +1;
    
    short cellStart = contentStartOffset;
    
    short newCellStart = (short)(cellStart - (short)(payLoadSize)  - cellHeaderSize);
    binaryFile.seek(newCellStart);
    binaryFile.writeShort(payLoadSize);
    binaryFile.writeInt(lastRowId);
   
    //record head
    binaryFile.writeByte(colDatatypes.length); // number of columns
    binaryFile.write(colDatatypes); // datatypes

    //record body
    binaryFile.write(recordBody);

    binaryFile.seek(pageStart + 0x10 + (noOfCells * 2));
    binaryFile.writeShort(newCellStart);
    
    contentStartOffset = newCellStart;
    
    binaryFile.seek(pageStart + 4); binaryFile.writeShort(contentStartOffset);

    records.add(new TableRecord(lastRowId,colDatatypes, recordBody));
    noOfCells++;
    binaryFile.seek(pageStart + 2); binaryFile.writeShort(noOfCells);
    
  } catch (IOException ex) {
    System.out.println("Error while adding record to the page : " + ex.getMessage());
  }
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
        
        if(lastRowId == 0) lastRowId = rowId;
        
        byte[] colDatatypes = new byte[noOfcolumns];
        byte[] recordBody = new byte[payLoadSize - noOfcolumns - 1];

        binaryFile.read(colDatatypes);
        binaryFile.read(recordBody);

        TableRecord record = new TableRecord(rowId,colDatatypes, recordBody);
        records.add(record);
      }
    } catch (IOException ex) {
      System.out.println("Error while filling records from the page" + ex.getMessage());
    }

  }



}
