import java.util.ArrayList;
import java.util.List;
import java.io.RandomAccessFile;
import java.io.File;
import java.io.IOException;

//B + 1 tree implementation for traversing Table files
public class BPlusOneTree{

    
    RandomAccessFile binaryFile;
    int rootPageNo;
    String tableName;

    public BPlusOneTree(RandomAccessFile file, int rootPageNo,String tableName)
    {
        this.binaryFile = file;
        this.rootPageNo = rootPageNo;
        this.tableName = tableName;
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


    
	public List<Integer> getAllLeaves(Condition condition) throws IOException{
	
   if(condition == null || condition.getOperation() == OperatorType.NOTEQUAL || !(new File(DavisBasePrompt.getNDXFilePath(tableName, condition.columnName)).exists()))
   {
      //brute force logic (as there is no index) traverse through the tree and get all leaf pages
      return getAllLeaves();
   }
   else{
      //TODO find the leaf page numbers based on the condition and index files
      //right now we are taking all the leaves
      RandomAccessFile indexFile = new RandomAccessFile(DavisBasePrompt.getNDXFilePath(tableName, condition.columnName),"r");
      BTree bTree = new BTree(indexFile);
     
      //Binary search on the btree
      List<Integer> rowIds = bTree.getRowIds(condition);

      

      //TODO remove later, print the rowids from index search
      for(int rowId : rowIds)
      {
          System.out.print(" " + rowId + " ");
      }

      System.out.println();
      indexFile.close();

      //TODO stil using brute force - find logic to convert the obtained rowids into pageNumbers
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