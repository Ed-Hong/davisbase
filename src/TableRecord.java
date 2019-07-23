import java.util.List;

import java.util.Arrays;
import java.util.ArrayList;

//This class contains helper methods for converting the table record data (header and body) into byte array
public class TableRecord
{
    public int rowId;
    public Byte[] colDatatypes;
    public Byte[] recordBody;
    private List<Attribute> attributes;
    public short recordOffset;
    public short pageHeaderIndex;

    TableRecord(short pageHeaderIndex,int rowId, short recordOffset, byte[] colDatatypes, byte[] recordBody)
    {
        this.rowId = rowId;
        this.recordBody= ByteConvertor.byteToBytes(recordBody);
        this.colDatatypes = ByteConvertor.byteToBytes(colDatatypes);
        this.recordOffset =  recordOffset;
        this.pageHeaderIndex = pageHeaderIndex;
        setAttributes();
    }

    public List<Attribute> getAttributes()
    {
        return attributes;
    }

    private void setAttributes()
    {
        attributes = new ArrayList<>();
        int pointer = 0;
        for(Byte colDataType : colDatatypes)
        {
             byte[] fieldValue = ByteConvertor.Bytestobytes(Arrays.copyOfRange(recordBody,pointer, pointer + DataType.getLength(colDataType)));
             attributes.add(new Attribute(DataType.get(colDataType), fieldValue));
                    pointer =  pointer + DataType.getLength(colDataType);
        }
    }
    
}