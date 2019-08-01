import java.util.Date;
import java.text.SimpleDateFormat;
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
        case YEAR: this.fieldValue = Integer.valueOf((int)Byte.valueOf(ByteConvertor.byteFromByteArray(fieldValuebyte))+2000).toString(); break;
        case TIME:
            // HH:MM:SS
            int millisSinceMidnight = ByteConvertor.intFromByteArray(fieldValuebyte) % 86400000;
            int seconds = millisSinceMidnight / 1000;
            int hours = seconds / 3600;
            int remHourSeconds = seconds % 3600;
            int minutes = remHourSeconds / 60;
            int remSeconds = remHourSeconds % 60;
            this.fieldValue = String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", remSeconds);
            break;
        case DATETIME:
            // YYYY-MM-DD_HH:MM:SS
            Date rawdatetime = new Date(Long.valueOf(ByteConvertor.longFromByteArray(fieldValuebyte)));
            this.fieldValue = String.format("%02d", rawdatetime.getYear()+1900) + "-" + String.format("%02d", rawdatetime.getMonth()+1)
                + "-" + String.format("%02d", rawdatetime.getDate()) + "_" + String.format("%02d", rawdatetime.getHours()) + ":"
                + String.format("%02d", rawdatetime.getMinutes()) + ":" + String.format("%02d", rawdatetime.getSeconds());
            break;
        case DATE:
            // YYYY-MM-DD
            Date rawdate = new Date(Long.valueOf(ByteConvertor.longFromByteArray(fieldValuebyte)));
            this.fieldValue = String.format("%02d", rawdate.getYear()+1900) + "-" + String.format("%02d", rawdate.getMonth()+1)
                + "-" + String.format("%02d", rawdate.getDate());
            break;
        case TEXT: this.fieldValue = new String(fieldValuebyte, "UTF-8"); break;
         default:
         this.fieldValue= new String(fieldValuebyte, "UTF-8"); break;
      }
         this.fieldValueByte = ByteConvertor.byteToBytes(fieldValuebyte);
    } catch(Exception ex) {
        System.out.println("! Formatting exception");
        //debug: ex.printStackTrace();
    }

    }

    Attribute(DataType dataType,String fieldValue) throws Exception {
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
              case YEAR: this.fieldValuebyte = new byte[] { (byte) (Integer.parseInt(fieldValue) - 2000) }; break;
              case TIME: this.fieldValuebyte = ByteConvertor.intTobytes(Integer.parseInt(fieldValue)); break;
              case DATETIME:
                  SimpleDateFormat sdftime = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                  Date datetime = sdftime.parse(fieldValue);  
                  this.fieldValuebyte = ByteConvertor.longTobytes(datetime.getTime());              
                break;
              case DATE:
                  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                  Date date = sdf.parse(fieldValue);  
                  this.fieldValuebyte = ByteConvertor.longTobytes(date.getTime());              
                break;
              case TEXT: this.fieldValuebyte = fieldValue.getBytes(); break;
               default:
               this.fieldValuebyte = fieldValue.getBytes(StandardCharsets.US_ASCII); break;
            }
            this.fieldValueByte = ByteConvertor.byteToBytes(fieldValuebyte);  
        } catch (Exception e) {
            System.out.println("! Cannot convert " + fieldValue + " to " + dataType.toString());
            throw e;
        }
    }
   
}