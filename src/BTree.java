import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.SortedSet;

public class BTree {
    Page root;
    RandomAccessFile binaryFile;

    public BTree(RandomAccessFile file) {
        this.binaryFile = file;
        this.root = new Page(binaryFile, DavisBaseBinaryFile.getRootPageNo(binaryFile));
    }

    //Recursively does a binary search using the given value and find the right pageNo to insert the index value
    private int getPageNoToInsert(Page page, String value) {
        if (page.pageType == PageType.LEAFINDEX) {
            return page.pageNo;
        } else {
            if (value.compareTo(page.indexValues.first()) < 0)
                return getPageNoToInsert
                    (new Page(binaryFile,page.indexValuePointer.get(page.indexValues.first()).leftPageNo),
                        value);
            else if(value.compareTo(page.indexValues.last()) > 0)
                return getPageNoToInsert(
                    new Page(binaryFile,page.indexValuePointer.get(page.indexValues.last()).leftPageNo),
                        value);
            else{
                //perform binary search 
                String closestValue = binarySearch(page.indexValues.toArray(new String[page.indexValues.size()]),value,value,0,page.indexValues.size());

                if(closestValue.compareTo(value) < 0)
                {
                    return page.indexValuePointer.get(closestValue).rightPageNo;
                }
                else if(closestValue.compareTo(value) > 0)
                {
                    return page.indexValuePointer.get(closestValue).leftPageNo;
                }
                else{
                    return page.pageNo;
                }
            }
        }
    }

    //Inserts index value into the index page
    public void insert(Attribute attribute,int rowId)
    {
    try{
        int pageNo = getPageNoToInsert(root, attribute.fieldValue) ;
        Page page = new Page(binaryFile, pageNo);
        page.addIndex(new IndexNode(attribute,Arrays.asList(rowId)));
        }
        catch(IOException e)
        {
        System.out.println("! Error while insering " + attribute.fieldValue +" into index file");
        }
    }

    private String binarySearch(String[] values,String searchValue,String closestValue,int start, int end)
    {
        if(end>=start)
        {
            int mid = (end - start)/2 + start;
            closestValue = values[mid];
            if(values[mid].equals(searchValue))
                return closestValue;

            if(values[mid].compareTo(searchValue) < 0)
                return binarySearch(values,searchValue,closestValue,mid + 1,end);
            else 
                return binarySearch(values,searchValue,closestValue,start,mid - 1);
        }

        return closestValue;
    }


}