import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/*
    Attribute class denotes each cell in the table (Datatype and value)
*/
public class Attribute
{
    //represents the byte array, the format stored in binary file
    public byte[] fieldValuebyte;
    public Byte[] fieldValueByte;

    public DataType dataType;
    
    //converted string value of the attribute
    public String fieldValue;

    //constructor
    Attribute(DataType dataType,byte[] fieldValue){
        this.dataType = dataType;
        this.fieldValuebyte = fieldValue;
    try{
    //Convert the byte array into string
      switch(dataType)
      {
         case NULL:
            this.fieldValue= "NULL"; break;
        case TINYINT: this.fieldValue = Byte.valueOf(ByteConvertor.byteFromByteArray(fieldValuebyte)).toString(); break;
        case SMALLINT: this.fieldValue = Short.valueOf(ByteConvertor.shortFromByteArray(fieldValuebyte)).toString(); break;
        case INT: this.fieldValue = Integer.valueOf(ByteConvertor.intFromByteArray(fieldValuebyte)).toString(); break;
        case BIGINT: this.fieldValue =  Long.valueOf(ByteConvertor.longFromByteArray(fieldValuebyte)).toString(); break;
        case FLOAT: this.fieldValue = Float.valueOf(ByteConvertor.floatFromByteArray(fieldValuebyte)).toString(); break;
        case DOUBLE: this.fieldValue = Double.valueOf(ByteConvertor.doubleFromByteArray(fieldValuebyte)).toString(); break;
        case TEXT: this.fieldValue = new String(fieldValuebyte, "UTF-8"); break;
         default:
         this.fieldValue= new String(fieldValuebyte, "UTF-8"); break;
      }
         this.fieldValueByte = ByteConvertor.byteToBytes(fieldValuebyte);
    } catch(Exception ex) {
        System.out.println("Cannot convert " + fieldValue + " to " + dataType.toString());
    }

    }

    Attribute(DataType dataType,String fieldValue){
        this.dataType = dataType;
        this.fieldValue = fieldValue;

        //Convert the string value into byte array based on DataType
        try {
            switch(dataType)
            {
              case NULL:
                  this.fieldValuebyte = null; break;
              case TINYINT: this.fieldValuebyte = new byte[]{ Byte.parseByte(fieldValue)}; break;
              case SMALLINT: this.fieldValuebyte = ByteConvertor.shortTobytes(Short.parseShort(fieldValue)); break;
              case INT: this.fieldValuebyte = ByteConvertor.intTobytes(Integer.parseInt(fieldValue)); break;
              case BIGINT: this.fieldValuebyte =  ByteConvertor.longTobytes(Long.parseLong(fieldValue)); break;
              case FLOAT: this.fieldValuebyte = ByteConvertor.floatTobytes(Float.parseFloat(fieldValue)); break;
              case DOUBLE: this.fieldValuebyte = ByteConvertor.doubleTobytes(Double.parseDouble(fieldValue)); break;
              case TEXT: this.fieldValuebyte = fieldValue.getBytes(StandardCharsets.US_ASCII); break;
               default:
               this.fieldValuebyte = fieldValue.getBytes(StandardCharsets.US_ASCII); break;
            }
            this.fieldValueByte = ByteConvertor.byteToBytes(fieldValuebyte);   
        } catch (Exception e) {
            System.out.println("Cannot convert " + fieldValue + " to " + dataType.toString());
            throw e;
        }
    }
   
}