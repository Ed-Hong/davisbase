import java.util.HashMap;
import java.util.Map;

public enum DataType {
     NULL((byte)0){ 
          @Override
          public String toString(){ return "NULL"; }},
     TINYINT((byte)1){ 
      @Override
      public String toString(){ return "TINYINT"; }},
     SMALLINT((byte)2){ 
      @Override
      public String toString(){ return "SMALLINT"; }},
     INT((byte)3){ 
      @Override
      public String toString(){ return "INT"; }},
     BIGINT((byte)4){ 
      @Override
      public String toString(){ return "BIGINT"; }},
     FLOAT((byte)5){ 
      @Override
      public String toString(){ return "FLOAT"; }},
     DOUBLE((byte)6){ 
      @Override
      public String toString(){ return "DOUBLE"; }},
     YEAR((byte)8){ 
      @Override
      public String toString(){ return "YEAR"; }},
     TIME((byte)9){ 
      @Override
      public String toString(){ return "TIME"; }},
     DATETIME((byte)10){ 
      @Override
      public String toString(){ return "DATETIME"; }},
     DATE((byte)11){ 
      @Override
      public String toString(){ return "DATE"; }},
     TEXT((byte)12){ 
      @Override
      public String toString(){ return "TEXT"; }};
     
    
     
 private static final Map<Byte,DataType> dataTypeLookup = new HashMap<Byte,DataType>();
 private static final Map<Byte,Integer> dataTypeSizeLookup = new HashMap<Byte,Integer>();
 private static final Map<String,DataType> dataTypeStringLookup = new HashMap<String,DataType>();
 private static final Map<DataType,Integer> dataTypePrintOffset = new HashMap<DataType,Integer>();



 static {
      for(DataType s : DataType.values())
          {
               dataTypeLookup.put(s.getValue(), s);
               dataTypeStringLookup.put(s.toString(), s);
              
               if(s == DataType.TINYINT || s== DataType.YEAR)
                   {
                          dataTypeSizeLookup.put(s.getValue(), 1);
                          dataTypePrintOffset.put(s, 6);
                   }
               else if(s == DataType.SMALLINT){
                    dataTypeSizeLookup.put(s.getValue(), 2);
                    dataTypePrintOffset.put(s, 8);
               }
               else if(s == DataType.INT || s == DataType.FLOAT || s == DataType.TIME){
                    dataTypeSizeLookup.put(s.getValue(), 4);
                    dataTypePrintOffset.put(s, 10);
               }
               else if(s == DataType.BIGINT || s == DataType.DOUBLE
                          || s == DataType.DATETIME || s == DataType.DATE ){
                    dataTypeSizeLookup.put(s.getValue(), 8);
                    dataTypePrintOffset.put(s, 25);
                          }
               else if(s == DataType.TEXT){
                    dataTypePrintOffset.put(s, 25);
               }
               else if(s == DataType.NULL){
                    dataTypeSizeLookup.put(s.getValue(), 0);
                    dataTypePrintOffset.put(s, 6);
               }
          }


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

 //GET the Datatype from String map (eg: "INT" -- > DataType.INT)
 public static DataType get(String text) { 
      return dataTypeStringLookup.get(text); 
 }

 public static int getLength(DataType type){
     return getLength(type.getValue());
}
 public static int getLength(byte value){
     if(get(value)!=DataType.TEXT)
          return dataTypeSizeLookup.get(value);
     else
          return value - 12;
 }

 public int getPrintOffset(){
      return dataTypePrintOffset.get(get(this.value));
 }


 
 
}