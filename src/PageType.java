import java.util.HashMap;
import java.util.Map;

public enum PageType {
    INTERIOR((byte)5),
    INTERIORINDEX((byte)2),
    LEAF((byte)13),
    LEAFINDEX((byte)10);
     
 private static final Map<Byte,PageType> pageTypeLookup = new HashMap<Byte,PageType>();

 static {
      for(PageType s : PageType.values())
      pageTypeLookup.put(s.getValue(), s);
 }
 private byte value;

 private PageType(byte value) {
      this.value = value;
 }

 public byte getValue() { return value; }

 public static PageType get(byte value) { 
      return pageTypeLookup.get(value); 
 }
 
}