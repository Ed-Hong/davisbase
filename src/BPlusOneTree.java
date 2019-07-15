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
        if(rootPageType == PageType.LEAF || rootPageType == PageType.LEAFINDEX)
        {
            leafPages.add(rootPageNo);
            return leafPages;
        }
        else // TODO traverse from root , add only leaf pages
        {
            
             return leafPages;
        }

    }

    public int getRightMostPage() throws IOException{

       return 0;

    }
}