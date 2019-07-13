import java.util.HashMap;
import java.util.Map;

public enum DataType {
    NULL((byte)0),
    TINYINT((byte)1),
    SMALLINT((byte)2),
    INT((byte)3),
    BIGINT((byte)4),
    FLOAT((byte)5),
    DOUBLE((byte)6),
    YEAR((byte)8), 
    TIME((byte)9),
    DATETIME((byte)10),
    DATE((byte)11), 
    TEXT((byte)12);
     
 private static final Map<Byte,DataType> dataTypeLookup = new HashMap<Byte,DataType>();

 static {
      for(DataType s : DataType.values())
      dataTypeLookup.put(s.getValue(), s);
 }
 private byte value;

 private DataType(byte value) {
      this.value = value;
 }

 public byte getValue() { return value; }

 public static DataType get(byte value) { 
     if(value > 12)
        return DataType.TEXT;
      return dataTypeLookup.get(value); 
 }
 
}