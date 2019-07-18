public class TableInteriorRecord
{
    public int rowId;
    public int leftChildPageNo;

    public TableInteriorRecord(int rowId, int leftChildPageNo){
        this.rowId = rowId;this.leftChildPageNo = leftChildPageNo;  
    }

}
