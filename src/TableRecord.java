import java.nio.ByteBuffer;
import java.nio.ByteOrder;

//This class contains helper methods for converting the table record data (header and body) into byte array
public class TableRecord
{
    public int rowId;
    public Byte[] colDatatypes;
    public Byte[] recordBody;
    

    TableRecord(int rowId, byte[] colDatatypes, byte[] recordBody)
    {
        this.rowId = rowId;
        this.recordBody= byteToBytes(recordBody);
        this.colDatatypes = byteToBytes(colDatatypes);
    }

    public static Byte[] intToBytes(int data) {
		return byteToBytes(ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.BIG_ENDIAN).putInt(data).array());
	}

    public static Byte[] byteToBytes(final byte[] data){
        Byte[] result= new Byte[data.length];
        for(int i=0;i<data.length;i++)
            result[i] = data[i];
        return result;
    }

    public static Byte[] shortToBytes(final short data)
    {
        return byteToBytes(ByteBuffer.allocate(Short.BYTES).order(ByteOrder.BIG_ENDIAN).putShort(data).array());
    }

    public static Byte[] longToBytes(final long data) {
		return byteToBytes(ByteBuffer.allocate(Long.BYTES).putLong(data).array());
	}


    public static Byte[] floatToBytes(final float data) {
		return byteToBytes(ByteBuffer.allocate(Float.BYTES).putFloat(data).array());
    }
    

    public static Byte[] doubleToBytes(final double data) {
		return byteToBytes(ByteBuffer.allocate(Double.BYTES).putDouble(data).array());
    }

    public static byte[] Bytestobytes(final Byte[] data){
        byte[] result= new byte[data.length];
        for(int i=0;i<data.length;i++)
            result[i] = data[i];
        return result;
    }
    
}