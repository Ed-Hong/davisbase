import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.RandomAccessFile;
import java.io.File;
import java.io.IOException;

//B + 1 tree implementation for traversing Table files
public class BPlusOneTree {

    RandomAccessFile binaryFile;
    int rootPageNo;
    String tableName;

    public BPlusOneTree(RandomAccessFile file, int rootPageNo, String tableName) {
        this.binaryFile = file;
        this.rootPageNo = rootPageNo;
        this.tableName = tableName;
    }

    // This method does a traversal on the B+1 tree and returns the leaf pages in
    // order
    public List<Integer> getAllLeaves() throws IOException {

        List<Integer> leafPages = new ArrayList<>();
        binaryFile.seek(rootPageNo * DavisBaseBinaryFile.pageSize);
        // if root is leaf page read directly return one one, no traversal required
        PageType rootPageType = PageType.get(binaryFile.readByte());
        if (rootPageType == PageType.LEAF) {
            if (!leafPages.contains(rootPageNo))
                leafPages.add(rootPageNo);
        } else {
            addLeaves(rootPageNo, leafPages);
        }

        return leafPages;

    }

    // recursively adds leaves
    private void addLeaves(int interiorPageNo, List<Integer> leafPages) throws IOException {
        Page interiorPage = new Page(binaryFile, interiorPageNo);
        for (TableInteriorRecord leftPage : interiorPage.leftChildren) {
            if (Page.getPageType(binaryFile, leftPage.leftChildPageNo) == PageType.LEAF) {
                if (!leafPages.contains(leftPage.leftChildPageNo))
                    leafPages.add(leftPage.leftChildPageNo);
            } else {
                addLeaves(leftPage.leftChildPageNo, leafPages);
            }
        }

        if (Page.getPageType(binaryFile, interiorPage.rightPage) == PageType.LEAF) {
            if (!leafPages.contains(interiorPage.rightPage))
                leafPages.add(interiorPage.rightPage);
        } else {
            addLeaves(interiorPage.rightPage, leafPages);
        }

    }

    public List<Integer> getAllLeaves(Condition condition) throws IOException {

        if (condition == null || condition.getOperation() == OperatorType.NOTEQUAL
                || !(new File(DavisBasePrompt.getNDXFilePath(tableName, condition.columnName)).exists())) {
            // brute force logic (as there is no index) traverse through the tree and get
            // all leaf pages
            return getAllLeaves();
        } else {

            RandomAccessFile indexFile = new RandomAccessFile(
                    DavisBasePrompt.getNDXFilePath(tableName, condition.columnName), "r");
            BTree bTree = new BTree(indexFile);

            // Binary search on the btree
            List<Integer> rowIds = bTree.getRowIds(condition);
            Set<Integer> hash_Set = new HashSet<>();
           
            for (int rowId : rowIds) {
                hash_Set.add(getPageNo(rowId, new Page(binaryFile, rootPageNo)));
            }

            // DEBUG: print the rowids from index search
            // System.out.print(" count : " + rowIds.size() + " ---> ");
            // for (int rowId : rowIds) {
            //     System.out.print(" " + rowId + " ");
            // }
            // System.out.println();
            // System.out.println(" leaves: " + hash_Set);
            // System.out.println();

            indexFile.close();

            return Arrays.asList(hash_Set.toArray(new Integer[hash_Set.size()]));
        }

    }

    // Returns the right most child page for inserting new records
    public static int getPageNoForInsert(RandomAccessFile file, int rootPageNo) {
        Page rootPage = new Page(file, rootPageNo);
        if (rootPage.pageType != PageType.LEAF && rootPage.pageType != PageType.LEAFINDEX)
            return getPageNoForInsert(file, rootPage.rightPage);
        else
            return rootPageNo;

    }

    // perform binary search on Bplus one tree and find the rowids
    public int getPageNo(int rowId, Page page) {
        if (page.pageType == PageType.LEAF)
            return page.pageNo;

        int index = binarySearch(page.leftChildren, rowId, 0, page.noOfCells - 1);

        if (rowId < page.leftChildren.get(index).rowId) {
            return getPageNo(rowId, new Page(binaryFile, page.leftChildren.get(index).leftChildPageNo));
        } else {
        if( index+1 < page.leftChildren.size())
            return getPageNo(rowId, new Page(binaryFile, page.leftChildren.get(index+1).leftChildPageNo));
        else
           return getPageNo(rowId, new Page(binaryFile, page.rightPage));


        }
    }

    private int binarySearch(List<TableInteriorRecord> values, int searchValue, int start, int end) {

        if(end - start <= 2)
        {
            int i =start;
            for(i=start;i <end;i++){
                if(values.get(i).rowId < searchValue)
                    continue;
                else
                    break;
            }
            return i;
        }
        else{
            
                int mid = (end - start) / 2 + start;
                if (values.get(mid).rowId == searchValue)
                    return mid;

                if (values.get(mid).rowId < searchValue)
                    return binarySearch(values, searchValue, mid + 1, end);
                else
                    return binarySearch(values, searchValue, start, mid - 1);
            
        }

    }

}