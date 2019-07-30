import java.util.List;

public class IndexNode{
    public Attribute indexValue;
    public List<Integer> rowids;
    public boolean isInteriorNode;
    public int leftPageNo;

    public IndexNode(Attribute indexValue,List<Integer> rowids)
    {
        this.indexValue = indexValue;
        this.rowids = rowids;
    }

}