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
        if(!leafPages.contains(rootPageNo))
            leafPages.add(rootPageNo);
        }
        else
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
             if(!leafPages.contains(leftPage.leftChildPageNo))
                leafPages.add(leftPage.leftChildPageNo);
            }
            else{
                addLeaves(leftPage.leftChildPageNo, leafPages);
            }
        }

        if(Page.getPageType(binaryFile,interiorPage.rightPage) == PageType.LEAF)
        {
           if(!leafPages.contains(interiorPage.rightPage))
               leafPages.add(interiorPage.rightPage);
        }
        else{
            addLeaves(interiorPage.rightPage, leafPages);
        }

    }


    //TODO - complete with index mapping
	public List<Integer> getAllLeaves(Condition condition) throws IOException{
	
   if(condition == null)
   {
      //brute force logic (as there are no index) traverse through the tree and get all leaf pages
      return getAllLeaves();
   }
   else{
      //TODO find the leaf page numbers based on the condition and index files
      //right now we are taking all the leaves
     return getAllLeaves();
   }
   
   	}
      
  //Returns the right most child page for inserting new records
  public static int getPageNoForInsert(RandomAccessFile file,int rootPageNo)
  {
       Page rootPage = new Page(file,rootPageNo);
       if(rootPage.pageType!= PageType.LEAF 
            && rootPage.pageType!=PageType.LEAFINDEX)
        return getPageNoForInsert(file,rootPage.rightPage);
      else
        return rootPageNo;
       
  }

}