import java.util.List;

public class IndexRecord{
    public Byte noOfRowIds;
    public DataType dataType;
    public Byte[] indexValue;
    public List<Integer> rowIds;
    public short pageHeaderIndex;
    public short pageOffset;
    int leftPageNo;
    int rightPageNo;
    int pageNo;
    private IndexNode indexNode;


    IndexRecord(short pageHeaderIndex,DataType dataType,Byte NoOfRowIds, byte[] indexValue, List<Integer> rowIds
    ,int leftPageNo,int rightPageNo,int pageNo,short pageOffset){
      
        this.pageOffset = pageOffset;
        this.pageHeaderIndex = pageHeaderIndex;
        this.noOfRowIds = NoOfRowIds;
        this.dataType = dataType;
        this.indexValue = ByteConvertor.byteToBytes(indexValue);
        this.rowIds = rowIds;

        indexNode = new IndexNode(new Attribute(this.dataType, indexValue),rowIds);
        this.leftPageNo = leftPageNo;
        this.rightPageNo = rightPageNo;
        this.pageNo = pageNo;
    }

    public IndexNode getIndexNode()
    {
        return indexNode;
    }


}