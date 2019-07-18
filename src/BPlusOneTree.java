import java.util.ArrayList;
import java.util.List;
import java.io.RandomAccessFile;

import java.io.IOException;

//B + 1 tree implementation for traversing Table files
public class BPlusOneTree{

    
    RandomAccessFile binaryFile;
    int rootPageNo;

    public BPlusOneTree(RandomAccessFile file, int rootPageNo)
    {
        this.binaryFile = file;
        this.rootPageNo = rootPageNo;
    }
    
   

    //This method does a traversal on the B+1 tree and returns the leaf pages in order
    public List<Integer> getAllLeaves() throws IOException{


        List<Integer> leafPages = new ArrayList<>();
         binaryFile.seek(rootPageNo * DavisBaseBinaryFile.pageSize);
        // if root is leaf page read directly return one one, no traversal required
        PageType rootPageType = PageType.get(binaryFile.readByte());
        if(rootPageType == PageType.LEAF)
        {
            leafPages.add(rootPageNo);
        }
        else // TODO traverse from root , add only leaf pages
        {
            addLeaves(rootPageNo,leafPages);
        }

        return leafPages;


    }

//recursively adds leaves
    private void addLeaves(int interiorPageNo,List<Integer> leafPages) throws IOException
    {
        Page interiorPage = new Page(binaryFile,interiorPageNo);
        for(TableInteriorRecord leftPage: interiorPage.leftChildren)
        {
            if(Page.getPageType(binaryFile,leftPage.leftChildPageNo) == PageType.LEAF)
            {
                leafPages.add(leftPage.leftChildPageNo);
            }
            else{
                addLeaves(leftPage.leftChildPageNo, leafPages);
            }
        }

        if(Page.getPageType(binaryFile,interiorPage.rightPage) == PageType.LEAF)
        {
            leafPages.add(interiorPage.rightPage);
        }
        else{
            addLeaves(interiorPage.rightPage, leafPages);
        }

    }
}