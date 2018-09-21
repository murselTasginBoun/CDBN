
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;



import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

 

 
 

public class CommunityDetection {
    
    
	static int line_counter=0;
	public static int selfLoops=0;
    String lineStr="";
    int benefitGroupBy=2; 
    String parms="";
    public double SFContribution=0.2;
    static int vertice_count,v2;
    static String inputfile;
    public static int lineCounter=0;
    static Random rand1=new Random(System.currentTimeMillis());
    
    static int edge_count=0;
    static int numberOfIslands=0;
    static int vertice_temp;
    public int numberOfCommunities;
    public static DynamicArray MainArray[];
    static FileReader inputFileReader;
    static BufferedReader inputStream;
    public static String networkFileName; //network dataset name
    public static String networkFileNameLong; //network dataset name with full path
    public static String copyCSVtoFile=null;
    static String in1;
    //Logging variables
    static int log_limit=17500;  //write_to_log buffer
    static int log_counter=0;
    static String logfile="mylog.txt";
    static String[] log_array=new String[log_limit];
    String bufferStr=null;
    public double totalWeight=0.0; 
    String dirName;
    String uniqueRunLabel=DateUtils.now("yyyyMMddHHmmss")+"";
    public static int LFRCounter=0;
    public boolean printDendogramCommunities=true;
    public String inputFileName;
    
	 
    public static class DateUtils {
        public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

        public static String now() {
          Calendar cal = Calendar.getInstance();
          SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
          return sdf.format(cal.getTime());

        }
        public static String now(String dateFormat) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            return sdf.format(cal.getTime());

          }
      }
     
    
   
     
	class nodeDescription
	{
		int nodeIndex;
		String nodeLabel;
	}
	
	class nodeDescriptionExt
	{
		int nodeIndex;
		String nodeLabel;
		HashMap<Integer,Integer> P1;
		HashMap<Integer,Integer> P2;
	}
	
	HashMap<Integer,nodeDescription> vertices=new HashMap<Integer,nodeDescription>();
	HashMap<String,Integer> verticesReverse=new HashMap<String,Integer>();
	 
    
    public HashMap<Integer,Integer> CommunityCount;
	public int CDwBN_method=2;
	private int networkSize;
	private enum gmlFileFormat 
	{ node, label, id, source, target, value, edge,novalue ; 

	public static gmlFileFormat fromString(String Str) 

	{ 
		try 
		{
			return valueOf(Str);
		} catch (Exception ex){return novalue;} } 
	}; 
	
	public class eventList{
		Vector<String> v1;
	}
    public CommunityDetection()
    {
    	 
    	CommunityCount=new HashMap<Integer,Integer>();
    	 
    }
    class vertice
    {
    	public vertice(int id2, String label2) {
			// TODO Auto-generated constructor stub
    		id=id2;
    		label=label2;
		}
    	public vertice(int id2, String label2,int CID) {
			// TODO Auto-generated constructor stub
    		id=id2;
    		label=label2;
    		communityID=CID;
		}
		int id;
    	String label;
    	int communityID;
    }
    class edge
    {
    	public edge(int source2, int target2, double value2) {
			// TODO Auto-generated constructor stub
    		source=source2;
    		target=target2;
    		value=value2;
		}
		int source;
    	int target;
    	double value; //weight
    }
   
    
    

    
    public void calculateEdgeWeights(DynamicArray D1[])
    {
    	System.out.println(DateUtils.now()+" FUNCTION-calculateEdgeWeights started---------------");

    	int loopCounter=0;
    	int printFreq=50000;
    	
    	// based on commonNeighbours
    	for(int i=0;i<D1.length;i++)  
    	{
    		for(int j=0;j<D1[i].neighbour_count;j++)
    		{
    			int neighbourID=D1[i].get(j);
    			int CN=this.calculateCommonNeighbours(D1, i, neighbourID);
    			D1[i].setEdgeWeight(neighbourID, CN);
    			//D1[neighbourID].setEdgeWeight(i, edgeWeight);
    			loopCounter++;
    			if(loopCounter%printFreq==0)
    				System.out.println(DateUtils.now()+"         --> calculateEdgeWeight counter:"+loopCounter);
    		}
    	}

    	System.out.println(DateUtils.now()+" FUNCTION-calculateEdgeWeights ended---------------");
    }

    
    
    
    public void communityDetectionBoundaryNodes(DynamicArray D1[]) throws IOException
    {
    	System.out.println(DateUtils.now()+" FUNCTION communityDetectionBoundaryNodes started---------------");
 
    	int loopTotalCounter=0;
	
    	Stack<Integer> boundaryNodeStack=new Stack<Integer>();
    	HashMap<Integer,Double> benefitScores=new HashMap<Integer,Double>();
    	HashMap<Integer,Integer> communityMembers=new HashMap<Integer,Integer>();
    	 
    	//0- Initialize communities so that each node is inside its community of size 1
    	for(int i=0;i<D1.length;i++)
    		D1[i].communityID=i;

    	//0.1 - Set edge weights ------------------------------------------------------------------------------	 	
    	calculateEdgeWeights(D1);
    	
    	//0.1.end ---------------------------------------------------------------------------------------------
    	
    	//1-Initial heuristic 
    	for(int i=0;i<D1.length;i++)
    	{
    		double maxW=0;
    		int idx=i;
    		for(int j=0;j<D1[i].neighbour_count;j++)
    		{
    			int neighbourID=D1[i].get(j);
    			double w=D1[i].getEdgeWeight(neighbourID);
    			if(w>maxW)
    			{
    				maxW=w;
    				idx=neighbourID;				
    			}
    		}
    		//set communityID with the highest argmax{edgeWeight} in neighbourhood
    		D1[i].communityID=D1[idx].communityID;

    	}
    	
    	System.out.println(DateUtils.now()+"         --> boundaryNodes initialization done!");
    	//------- End of initialization -----------------------------------------------------------------------
    	
    	
    	//2-Decide the communities of the boundary nodes --------------------------------------------------------
    	//2.1 Initialize boundary node stack
    	for(int i=0;i<D1.length;i++)
    	{
    		for(int j=0;j<D1[i].neighbour_count;j++)
    		{
    			int neighbourID=D1[i].get(j);
    			if(D1[i].communityID!=D1[neighbourID].communityID && D1[i].boundaryState==0)
    			{
    				boundaryNodeStack.push(i);
    				D1[i].boundaryNodeStateCounter++;
    				loopTotalCounter++;
    				D1[i].boundaryState=1;		
    				break;
    			}
    		}
    	}
    	
    	System.out.println(DateUtils.now()+"         --> Initial boundary stack size="+boundaryNodeStack.size());
    	
    	//-------- End of decide communities of boundary nodes  -----------------------------------------------
    	

    	//3-Merge LOOP - Loop while boundaryNodeStack is not empty, merge boundary nodes with communities.
    	int loopCount=0;
    	int messageFreq=5000;
    	while(!boundaryNodeStack.isEmpty())
    	{
    		loopCount++;
    		loopTotalCounter++;
    		int currentID=0;
    	 
    		//Random order. Get element from stack with random index and remove it
    		//-----------------------------------------------------------------------------
    		{
    			int randIndex=rand1.nextInt(boundaryNodeStack.size());
    			currentID=boundaryNodeStack.remove(randIndex);
    		}
    		 
    		//--------------------------------------------------------------
    		D1[currentID].boundaryState=0;
    		if((loopCount%messageFreq)==0)
    		{
    			System.out.println(DateUtils.now()+"         --> communityDetection counter:"+loopCount+" boundaryNodeStack.size:"+boundaryNodeStack.size()+" current node D["+currentID+"].k:"+D1[currentID].neighbour_count);
    		}
    		 
    		//evaluate the benefits
    		benefitScores.clear();
    		communityMembers.clear();
    		
    		
    		
    		 
    		for(int i=0;i<D1[currentID].neighbour_count;i++)
    		{
    			int neighbourID=D1[currentID].get(i);
    			int commID=D1[neighbourID].communityID;	
    			double benefit=D1[currentID].getEdgeWeight(neighbourID);

    			// benefitGroupBy parameter ------------------------------------------------------
    			// 0 : Scores are for each neighbor
    			// 1 : Scores are group summed for each community --------------------------------
    			if(benefitGroupBy==1)
    			{
    				benefitScores.put(neighbourID, benefit);
    			}

    			if(benefitGroupBy==2)
    			{
    				if(benefitScores.get(commID)!=null)
    				{
    					double score=benefitScores.get(commID);
    					benefitScores.put(commID, score+benefit);
    				}
    				else
    				{
    					benefitScores.put(commID, benefit);
    				}

    				//---------------------------------------------------------------------------
    				// Size of the communities
    				if(communityMembers.get(commID)==null)
    				{
    					communityMembers.put(commID,1);
    				}
    				else
    				{
    					int X=communityMembers.get(commID);
    					communityMembers.put(commID, X+1);
    				}
    				//---------------------------------------------------------------------------
    			}	   				
    		}

    		//-----------------------------------------------------------------------------------------
    		//Find max benefit score and its ID
    		//-----------------------------------------------------------------------------------------
    		Iterator<Integer> STkey=benefitScores.keySet().iterator();
    		double highestScore=-999999;
    		int highestCommID=-1;
        	while(STkey.hasNext())
        	{
        		Object IDx=STkey.next();
        		double currentScore=benefitScores.get(IDx);
        		if(currentScore>highestScore)
        		{
        			highestScore=currentScore;
        			highestCommID=Integer.valueOf(IDx.toString());
        		}
        	}
        	//-----------------------------------------------------------------------------------------
        	
        	
        	
        	//------ Find the neighbours having highest score; i.e. may be more than 1 ---------------------------
        	HashMap<Integer,Double> finalScores=new HashMap<Integer,Double>();
        	
        	//----- Put into finalScores hashMap -----------------------------------------------------------------
        	finalScores.clear();
    		{
    			Iterator<Integer> J1=benefitScores.keySet().iterator();
    			while(J1.hasNext())
    			{
    				Object ID=J1.next();
    				double scoreVal=benefitScores.get(ID);
    				if(scoreVal==highestScore)
    					finalScores.put(Integer.valueOf(ID.toString()), scoreVal);
    			}	
    		}
    		//----------------------------------------------------------------------------------------------------
    		
    		// TIE-SITUATIONS ------------------------------------------------------------------------------------
    		int maxScore;
 
    		//------------------------- TIE SITUATIONS ---------------------------------------------------------------
    		//--- IF currentCommID is already in finalScores then no update
    		int currentCommIDofNode=D1[currentID].communityID;

    		if(finalScores.size()>0)
    			if(finalScores.get(currentCommIDofNode)==null)
    			{
    				maxScore=0;		
    				//----------- select an item randomly ----------------------
    				int randomIDx=rand1.nextInt(finalScores.keySet().size());
    				Object ID=finalScores.keySet().toArray()[randomIDx];
    				highestCommID=Integer.valueOf(ID.toString());
    				//----------------------------------------------------------
    			}
    			else
    			{
    				highestCommID=currentCommIDofNode;
    			}

    		// End of TIE-SITUATIONS -------------------------------------------------------------------------------- 

 
        
        	int currentNodeCommID=D1[currentID].communityID;
          
        	
        	
        	if(currentNodeCommID!=highestCommID)
        	{
        		//Community ID changes

        		int oldCommID=D1[currentID].communityID;
        		D1[currentID].communityID=highestCommID;
        		D1[currentID].CIDchangeCount++;
        		String tmpHistory=DateUtils.now()+";"+lineCounter+";Node;"+currentID+";oldCID;"+oldCommID+";newCommID;"+highestCommID+";changeCount;"+D1[currentID].CIDchangeCount;
        		D1[currentID].writeLog(tmpHistory);
       

        		//Insert neighbours belonging to old communityID to the stack (they are new border nodes)
        		for(int i=0;i<D1[currentID].neighbour_count;i++)
        		{
        			int neighbourID=D1[currentID].get(i);
        			//------------------------------------------------
        			int nodeCommID=D1[currentID].communityID;
        			int neighbourCommID=D1[neighbourID].communityID;

        			nodeCommID=D1[currentID].communityID;
        			neighbourCommID=D1[neighbourID].communityID;

        			//------------------------------------------------

        			if(nodeCommID!=neighbourCommID && D1[neighbourID].boundaryState==0)
        			{	
        				boundaryNodeStack.push(neighbourID);
        				D1[neighbourID].boundaryNodeStateCounter++;
        				D1[neighbourID].boundaryState=1;

        			}
        		}
        	}

    	}
    	//--------- END  OF MERGE LOOP --------------------------------------------------------------------
    	
    	//---- find communities   -------------------------------------------------------------------------

    	{	
    		System.out.println(DateUtils.now()+"         --> findCommunityOfNode for whole network started");
    		for(int i=0;i<D1.length;i++)
    		{
    			D1[i].communityArc=D1[i].communityID; // Store the direct arc that leads to community core
    			int communityID=this.findCommunityOfNode(D1,i);
    		 
    			D1[i].communityID=communityID;
    			 
    		}
    	}
    	//---- end of find communities using preference network -------------------------------------------

    	System.out.println(DateUtils.now()+"         --> totalLoopCount:"+loopTotalCounter);
    	System.out.println(DateUtils.now()+" FUNCTION-communityDetectionBoundaryNodes ended---------------");
    	 
    	
    }
    
   
    
    public int findCommunityOfNode(DynamicArray D1[],int ID)
    { 	 
    	int commID=ID;
    	int transitID=0;
    	HashMap<Integer,Integer> linklist=new HashMap<Integer,Integer>();	
    	int returnVal=0;		   	 
    		 
    			if(D1[ID].communityID==ID) //Self commID
    				returnVal=ID;
    			else
    			{
    				linklist.put(ID, ID);
    				transitID=D1[ID].communityID;
    				while(linklist.get(transitID)==null)
    				{
    					linklist.put(transitID, transitID);
    					transitID=D1[transitID].communityID;
    				}
    				 
    				Iterator<Integer> TI=linklist.keySet().iterator();
    				while(TI.hasNext())
    				{
    					Object IDx=TI.next();
    					D1[Integer.valueOf(IDx.toString())].communityID=transitID;
    				}
    				returnVal=transitID;
    			}
    		 
    	return returnVal;	
    }
    
    
      
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 
    public int calculateCommonNeighbours(DynamicArray D1[],int firstIdx,int secondIdx)
    {
    	//CDwPN.calculateCommonNeighbours
    	int retVal=0;
    	int commonN=0;
    	HashMap<Integer,Integer> commonNeighbours=new HashMap<Integer,Integer>();
    	int k1=D1[firstIdx].neighbour_count;
    	int k2=D1[secondIdx].neighbour_count;
    	
    	if(k1<k2)
    	{
    		for(int i=0;i<k1;i++)
    		commonNeighbours.put(D1[firstIdx].get(i), D1[firstIdx].get(i));
    		for(int i=0;i<k2;i++)
    		{
    			if(commonNeighbours.get(D1[secondIdx].get(i))!=null)
    				commonN++;
    		}
    	}
    	else
    	{
    		for(int i=0;i<k2;i++)
        		commonNeighbours.put(D1[secondIdx].get(i), D1[secondIdx].get(i));
    		for(int i=0;i<k1;i++)
    		{
    			if(commonNeighbours.get(D1[firstIdx].get(i))!=null)
    				commonN++;
    		}
    	}
    	retVal=commonN;
    	return retVal;
    }
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 
    
   
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public String communityDetection(String fileN) throws IOException
    {
    	 

    	 
    	inputFileName=fileN;

		//--------------READ NETWORK INPUT FILE ---------------------------------------------------------------------

		if(inputFileName.contains(".gml"))							
			readGMLformat(inputFileName);			
		if(inputFileName.contains(".txt") || inputFileName.contains(".net"))				
			readPajekformat(inputFileName,true);
		if(inputFileName.contains(".dat"))				
			this.readDATformat(inputFileName);
	 
		

    	//Option-1: Community detection using boundary node - INDIVIDUAL APPROACH---------------------------------
    	if(CDwBN_method==1) //Common neighbours with individual approach
    	{
    		System.out.println(DateUtils.now()+"         --> CDwBN with Individual approach is running");
    		for(int i=0;i<MainArray.length;i++)
    			for(int j=0;j<MainArray[i].neighbour_count;j++)
    			{
    				int neighbourID=MainArray[i].get(j);
    				int commonNeighbours=this.calculateCommonNeighbours(MainArray, i, neighbourID);
    				MainArray[i].setEdgeWeight(neighbourID, commonNeighbours);
    			}		
    		this.benefitGroupBy=1;
    	}
    	//------------------------------------------------------------------------------------------------------------------------

    	
    	//Option-2: Community detection using boundary node - INDIVIDUAL APPROACH------------------------------------------------------------
    	if (CDwBN_method==2)
    	{ 	
    		System.out.println(DateUtils.now()+"         --> CDwBN with Group approach is running");
    		for(int i=0;i<MainArray.length;i++)
    			for(int j=0;j<MainArray[i].neighbour_count;j++)
    			{
    				int neighbourID=MainArray[i].get(j);
    				int commonNeighbours=this.calculateCommonNeighbours(MainArray, i, neighbourID);
    				MainArray[i].setEdgeWeight(neighbourID, commonNeighbours);
    			}		
    		this.benefitGroupBy=2;
		}
    	//1.end ------------------------------------------------------------------------------------------------------------------


    	// Community detection algorithm
    	this.communityDetectionBoundaryNodes(MainArray);

    	CommunityCount.clear();
    	//Find communities using the constructed preference network --------------------------------------------------------------
    	for(int i=0;i<MainArray.length;i++)
    	{
    		MainArray[i].communityArc=MainArray[i].communityID; // Store the direct arc that leads to community core
    		int communityID=this.findCommunityOfNode(MainArray,i);
    		MainArray[i].communityID=communityID;
    		CommunityCount.put(communityID, communityID); 
    	} 

    	//------------------------------------------------------------------------------------------------------------------------
		 
		String filename=inputFileName.substring(0,inputFileName.lastIndexOf("."));    
	 	if(System.getProperty("os.name").contains("Mac OS"))
			filename=filename.replace("\"","");
	    filename=filename+".communities.txt";
	    
	    //Print communities
	    this.printCMTYfile(MainArray, filename);
		return filename;
		//-------------------------------------------------------------------------------------------------------------------------
    }
    
  
    
   
    
  
    
    
    public void printCMTYfile(DynamicArray D1[],String outputFileName) 
    {
    	System.out.println(DateUtils.now()+" FUNCTION-printCMTYfile started------------------------------");
    	System.out.println(DateUtils.now()+"         --> OutputFile:"+outputFileName);
    	
    	HashMap<Integer,String> communityMap=new HashMap<Integer,String>();

    	String edgeStr;
    	int bufSize=8192;
    	int cacheLimit=50000;
    	int firstTime=0;

        int intCnt=0;
        int totalCnt=0;
    	int intLimit=5000;
    	
    	int RECORD_COUNT=cacheLimit+2;
        List<String> records = new ArrayList<String>(RECORD_COUNT);
        
    	//1.Insert into the community hash map --------------------------------------------------
    	for(int i=0;i<D1.length;i++)
    	{
    		int communityID=D1[i].communityID;
    		String nodeLIST=communityMap.get(communityID);
    		
    		if(nodeLIST==null)
    			nodeLIST=(i+1)+"";
    		else
    			nodeLIST=nodeLIST+" "+(i+1);
    		 
    		communityMap.put(communityID, nodeLIST);
    	}
    	
    	System.out.println(DateUtils.now()+"         -----------------------------------------");
    	System.out.println(DateUtils.now()+"         --> Number of nodes      :"+D1.length);
    	System.out.println(DateUtils.now()+"         --> Number of edges      :"+this.edge_count);
    	System.out.println(DateUtils.now()+"         --> Number of communities:"+communityMap.size());
    	System.out.println(DateUtils.now()+"         -----------------------------------------");
    	//1.end ---------------------------------------------------------------------------------
    	
    	
    	//2.Read from communityMAP and insert into records  List --------------------------------
    	

    	try {
    		BufferedWriter writer;
    		if (firstTime==0)
    			writer = new BufferedWriter(new FileWriter(outputFileName, false),bufSize);
    		else //append the file
    			writer = new BufferedWriter(new FileWriter(outputFileName, true),bufSize);
    		System.out.println(DateUtils.now()+"         --> Community (CMTY) file is being written");	

    		Iterator<Integer> TI=communityMap.keySet().iterator();
    		while(TI.hasNext())
    		{

    			Integer IDx=TI.next();
    			String memberLIST=communityMap.get(IDx);
    			records.add(memberLIST+String.format("%n"));
    			intCnt++;
    			totalCnt++;
    			if(intCnt%5000==0)
    				System.out.println(DateUtils.now()+"         --> "+intCnt+" number of communities are inserted into list");
    			if(records.size()%cacheLimit==0)
    			{
    				totalCnt=0;
    				for (String record: records) {
    					writer.write(record);
    				}
    				writer.flush();
    				records.clear();
    				System.out.println(DateUtils.now()+"         --> "+cacheLimit+" number of records written to disk");
    			}
    		}
    		//2.end ---------------------------------------------------------------------------------

    		//3.Flush remaining records to disk
    		System.out.println(DateUtils.now()+"         --> "+records.size() + " remaining records will be written");

    		for (String record: records) {
    			writer.write(record);
    		}
    		writer.flush();
    		records.clear();

    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	System.out.println(DateUtils.now()+"         --> Communities are written to .cmty file:"+outputFileName);
    	System.out.println(DateUtils.now()+" FUNCTION-printCMTYfile ended------------------------------");
    }
    
    
     
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ 
    public String getBetween(String input,String startStr,String endStr)
    {
    	if(input.indexOf(startStr)==-1 || input.indexOf(endStr)==-1)
    		return null;
    	else
    		return input.substring(input.indexOf(startStr)+startStr.length(),input.substring(input.indexOf(startStr)+startStr.length()).indexOf(endStr)+input.indexOf(startStr)+startStr.length()).trim();
    }  
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public void readGMLformat(String inputfile)
    {
    	System.out.println(DateUtils.now()+" FUNCTION readGMLformat started------------------------------");
    	Vector<vertice> V1=new Vector<vertice>();
    	Vector<edge> E1=new Vector<edge>();
    	vertice v=new vertice(0,"");
    	edge a1=new edge(0,0,0.0);
    	String in1;
    	vertices.clear(); //clear vertices hash table
    	verticesReverse.clear(); 
    	
    	totalWeight=0.0;
    	vertice_count=0;
    	edge_count=0;
    	MainArray=null;
    	int lowestID=19999999;
    	String weightOrvalue=null;
    	int i=0;
    	this.networkFileName=inputfile.substring(inputfile.lastIndexOf(File.separator)+1);
    	this.networkFileNameLong=inputfile;
    	
    	Stack<String> readStack=new Stack<String>();
    	try
		{
		FileReader inputFileReader= new FileReader(inputfile);
		BufferedReader inputStream=new BufferedReader(inputFileReader);
		in1=inputStream.readLine();
		while(in1!=null)
		{
			if(in1.contains("]"))
			{
				String objectStr="";
				String popStr=readStack.pop();
				while(!popStr.contains("["))
				{
					objectStr+="$"+popStr.trim()+"$";
					popStr=readStack.pop().trim();
				}
				String objectType=readStack.pop().trim();
				
				if(objectType.contains("node"))
				{
					
					v.id=Integer.valueOf(getBetween(objectStr,"$id","$").trim());
					if(v.id<lowestID)
						lowestID=v.id;		 
					String nodeLabel=getBetween(objectStr,"$label","$");			 
					
					if(nodeLabel==null)
						v.label=""+v.id;
					else
						v.label=nodeLabel.replace("\"","").trim();
					
					if(objectStr.indexOf("$communityID")>0)
						v.communityID=Integer.valueOf(getBetween(objectStr,"$communityID","$").trim());
					else
						v.communityID=v.id;
					
					V1.add(new vertice(v.id,v.label,v.communityID));
					
				 
				}
				
				if(objectType.contains("edge"))
				{
					a1.source=Integer.valueOf(getBetween(objectStr,"$source","$"));
					a1.target=Integer.valueOf(getBetween(objectStr,"$target","$"));
					String edgeWeightValue=getBetween(objectStr,"$value","$");
					String edgeWeightValue2=getBetween(objectStr,"$weight","$");
					
					if (edgeWeightValue==null && edgeWeightValue2==null)
						a1.value=1.0; // unweighted gml networks
					else
						if(edgeWeightValue==null)
					        a1.value=Double.valueOf(edgeWeightValue2.replace("inf","1.116516"));
						else
							a1.value=Double.valueOf(edgeWeightValue.replace("inf","1.116516"));
					E1.addElement(new edge(a1.source,a1.target,a1.value));
				}
			}
			else
				readStack.push(in1.trim());
			in1=inputStream.readLine();
		}
		
	System.out.println(DateUtils.now()+"         --> V1.size="+V1.size()+" E1.size="+E1.size());

	vertice_count=V1.size();
	this.networkSize=V1.size(); 
		MainArray=new DynamicArray[V1.size()];
		for(i=0;i<V1.size();i++)
			MainArray[i]=new DynamicArray();	
   		
    
   		System.out.println(DateUtils.now()+"         --> doReadGML.starttime");
   		 
   		i=0;
   		while(V1.size()>0)
   		{  		
   			vertice vTmp=V1.remove(V1.size()-1);
   			
   			if(vertices.get(vTmp)==null)
			{
				nodeDescription n1=new nodeDescription();
				n1.nodeIndex=vertices.size();
				n1.nodeLabel=vTmp.label;
				vertices.put(vTmp.id, n1);
				 
			}
   			
   			verticesReverse.put(vTmp.label,vTmp.id);
   			  MainArray[vertices.get(vTmp.id).nodeIndex].name=vTmp.label;
   			  MainArray[vertices.get(vTmp.id).nodeIndex].communityID=vTmp.communityID;
   			 
   			  if((line_counter%50000)==0){
   			  	System.out.println(DateUtils.now()+"         --> Lines read:"+line_counter);
   			  
   			  }
   			  line_counter++;
   		 
   			
   		}

   		i=0;
   		while(E1.size()>0) 
   		{
   			edge tmpEdge=new edge(0,0,0.0);
   			tmpEdge=E1.remove(E1.size()-1);
   			
   			int sourceNodeIdx=vertices.get(tmpEdge.source).nodeIndex;
   			int targetNodeIdx=vertices.get(tmpEdge.target).nodeIndex;
   			
   	 
   			if(tmpEdge.source!=tmpEdge.target)
   			if(MainArray[sourceNodeIdx].CheckNeigbourhood(targetNodeIdx)==false)
   			{
   				MainArray[sourceNodeIdx].put(targetNodeIdx,tmpEdge.value);
   				if(tmpEdge.value<Double.POSITIVE_INFINITY)
   					totalWeight=totalWeight+tmpEdge.value;
   		 
   			// for undirected graph, second insertion 
   			if(MainArray[targetNodeIdx].CheckNeigbourhood(sourceNodeIdx)==false)
   			 	MainArray[targetNodeIdx].put(sourceNodeIdx,tmpEdge.value);
   			
   			}
   		
   			
   			if((line_counter%50000)==0)
   			  	System.out.println(DateUtils.now()+"         --> Lines read:"+line_counter);
   			   
   			line_counter++;
   			i++;  
   		}
   		bufferStr=DateUtils.now()+"         --> Lines read:"+line_counter+" "+DateUtils.now();
   	  	System.out.println(bufferStr);
   	  	 
   	   
   	  	
   	  	bufferStr=DateUtils.now()+"         --> Network file finished succesfully!";
   	  	bufferStr=bufferStr+"\n"+DateUtils.now()+"         --> Total weight of edges="+totalWeight;
   	  	bufferStr=bufferStr+"\n"+DateUtils.now()+"         --> Total number of nodes="+this.networkSize;
   	  	bufferStr=bufferStr+"\n"+DateUtils.now()+"         --> Total number of edges="+this.edge_count;
   	  	System.out.println(bufferStr);
   	  	 
   	    
   		inputStream.close();
   		
   	}catch (IOException e){
   		System.out.println("IOException:");
           e.printStackTrace();
   	}
  
   	bufferStr=DateUtils.now()+"         --> Number of vertices="+vertice_count;
   	System.out.println(bufferStr);
    
    
   	
    //  ****COUNT ALL THE EDGES
 	for(i=0;i<vertice_count;i++)
 	edge_count=edge_count+MainArray[i].neighbour_count;
 	edge_count=edge_count/2;
	
	bufferStr=DateUtils.now()+"         --> Number of edges="+edge_count;
	System.out.println(bufferStr);
	 
	 
	System.out.println(DateUtils.now()+" FUNCTION readGMLformat ended------------------------------");
   
    }
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
   

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public  void readDATformat(String inputfile)
    {
    	System.out.println(DateUtils.now()+" FUNCTION-readDATformat started------------------------------");
    	System.out.println(DateUtils.now()+"         --> Reading file:"+inputfile);
    	int source = 0,target=0;
    	totalWeight=0.0;
    	vertice_count=0;
    	String name="";
    	edge_count=0;
    	MainArray=null;
    	vertices.clear();
    	this.verticesReverse.clear();
    	int counter=0;
    	int printInterval=100000;
    	int firstLine=0;
    	int minIdx=0;
    	int arr[]=new int[2];
    	 
    	String in1;
    	HashMap<String,Integer> edges=new HashMap<String,Integer>();
    	this.networkFileName=inputfile.substring(inputfile.lastIndexOf(File.separator)+1);
    	this.networkFileNameLong=inputfile;


    	try
    	{
    		FileReader inputFileReader= new FileReader(inputfile);
    		BufferedReader inputStream=new BufferedReader(inputFileReader);

    		in1=inputStream.readLine();

    		while(in1!=null)
    		{

    			TokSequence ts1 = new TokSequence(new StringTokenizer(in1));

    			//Understand the .dat file format

    			String[] s=in1.split("\\s+");

    			if(s.length==3)
    			{
    				source=Integer.parseInt(s[0]);
    				target=Integer.parseInt(s[1]);
    			}
    			else
    			{
    				source=ts1.getInt();
    				target=ts1.getInt();
    			}
    			arr[0]=source;
    			arr[1]=target;


    			if(vertices.get(source)==null)
    			{
    				name=Integer.toString(source);
    				nodeDescription n1=new nodeDescription();
    				n1.nodeIndex=vertices.size();
    				n1.nodeLabel=name;
    				vertices.put(source, n1);
    				verticesReverse.put(n1.nodeLabel,n1.nodeIndex);
    			}



    			if(vertices.get(target)==null)
    			{	
    				name=target+"-"+target;//names may not be unique 27.10.2015
    				name=Integer.toString(target);
    				nodeDescription n1=new nodeDescription();
    				n1.nodeIndex=vertices.size();
    				n1.nodeLabel=name;
    				vertices.put(target, n1);
    				verticesReverse.put(n1.nodeLabel,n1.nodeIndex);
    			}

    			Arrays.sort(arr);
    			String edgeStr=Integer.toString(arr[0])+"-"+Integer.toString(arr[1]);
    			if(edges.get(edgeStr)==null)
    				edges.put(edgeStr,1);
    			else
    			{
    				int w=edges.get(edgeStr)+1;
    				edges.put(edgeStr, w);
    			}

    			counter++;
    			if(counter%printInterval==0)
    			{
    				System.out.println(DateUtils.now()+"         --> Reading lines:"+counter);
    			}

    			in1=inputStream.readLine();
    		}
    		inputStream.close();
    	}catch (IOException e){
    		System.out.println("IOException:");
    		e.printStackTrace();
    	}



    	vertice_count=vertices.size();
    	this.networkSize=vertice_count;
    	vertice_temp=0;

    	System.out.println(DateUtils.now()+"         --> Network size="+vertice_count);

    	MainArray=new DynamicArray[vertice_count];
    	for(int i=0;i<vertice_count;i++)
    		MainArray[i]=new DynamicArray();	

    	counter=0;
    	Iterator<Integer> TI=vertices.keySet().iterator();
    	while(TI.hasNext())
    	{

    		Object IDx=TI.next();
    		nodeDescription n2=vertices.get(IDx);
    		int MainArrayidx=n2.nodeIndex;

    		MainArray[MainArrayidx].name=n2.nodeLabel;

    		counter++;
    		if(counter%printInterval==0)
    		{
    			System.out.println(DateUtils.now()+"         --> MainArray entries inserted:"+counter);
    		}

    	}

    	counter=0;
    	Iterator<String> edgeIdx=edges.keySet().iterator();
    	while(edgeIdx.hasNext())
    	{
    		Object IDx=edgeIdx.next();
    		String sourceTargetPair=IDx.toString();
    		int sourceNode=Integer.valueOf(IDx.toString().split("-")[0]);
    		int targetNode=Integer.valueOf(IDx.toString().split("-")[1]);


    		int sourceIdx=verticesReverse.get(String.valueOf(sourceNode));
    		int targetIdx=verticesReverse.get(String.valueOf(targetNode));
    		double weight=(double)edges.get(IDx);

    		if(sourceIdx!=targetIdx)
    			if(MainArray[sourceIdx].CheckNeigbourhood(targetIdx)==false)
    			{
    				MainArray[sourceIdx].put(targetIdx,weight);
    				totalWeight+=(double)weight;

    				// for undirected graph, second insertion 
    				if(MainArray[targetIdx].CheckNeigbourhood(sourceIdx)==false)
    					MainArray[targetIdx].put(sourceIdx,weight);

    			}
    		counter++;
    		if(counter%printInterval==0)
    		{
    			System.out.println(DateUtils.now()+"         --> Edges are inserted:"+counter);
    		}
    	}

    	bufferStr=DateUtils.now()+"         --> Number of vertices="+vertice_count;
    	System.out.println(bufferStr);
    	 

    	//  ****COUNT ALL THE EDGES
    	for(int i=0;i<vertice_count;i++)
    		edge_count=edge_count+MainArray[i].neighbour_count;
    	edge_count=edge_count/2;

    	bufferStr=DateUtils.now()+"         --> Number of edges="+edge_count;
    	System.out.println(bufferStr);
    	 
    	//write_to_log(bufferStr,logfile,0);
    	System.out.println(DateUtils.now()+" FUNCTION-readDATformat ended------------------------------");
    }
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++  
    public  void readPajekformat(String inputfile,boolean AddExtraChars2Name ) throws IOException
   	{
    	 
    	System.out.println(DateUtils.now()+" FUNCTION-readPajekformat started------------------------------");
    	   totalWeight=0.0;
    	   vertice_count=0;
    	   edge_count=0;
    	   MainArray=null;
    	   vertices.clear();
    	   this.verticesReverse.clear();
    	   int firstLine=0;
    	   int minIdx=0;
    	   this.inputFileName=inputfile;
    	   this.networkFileName=inputfile.substring(inputfile.lastIndexOf(File.separator)+1);
    	   this.networkFileNameLong=inputfile;
    	   String in1;
    	   try
    	   {
    		   System.out.println(DateUtils.now()+"         --> Openning the file="+inputfile);
    		   FileReader inputFileReader= new FileReader(inputfile.replace("\"", "").trim());
    		   BufferedReader inputStream=new BufferedReader(inputFileReader);

    		   in1=inputStream.readLine();
    		   TokSequence ts1 = new TokSequence(new StringTokenizer(in1));
    		   in1=ts1.getString();
    		   vertice_count=ts1.getInt();
    		   this.networkSize=vertice_count;
    		   vertice_temp=0;

    		   int isNetworkWeighted=0;

    		   MainArray=new DynamicArray[vertice_count];
    		   for(int i=0;i<vertice_count;i++)
    			   MainArray[i]=new DynamicArray();	

    		   System.out.println(DateUtils.now()+"         --> Network Size is:"+MainArray.length);
    		  

    		   while (vertice_temp<vertice_count) 
    		   {  		 
    			   in1=inputStream.readLine();
    			   TokSequence ts = new TokSequence(new StringTokenizer(in1));   
    			   int userId = ts.getInt();   
    			   int tok_count=ts.token_count-2;
    			   String name = ts.getString(); 
    			   while(tok_count>0)
    			   {name=name+" "+ts.getString();
    			   tok_count--;
    			   }
    			   if(AddExtraChars2Name)
    				   name=name.replace("\"", "")+"-"+userId;//names may not be unique  
    			   else 
    				   name=name.replace("\"", "");

    			   MainArray[vertice_temp].name=name; 
    			   nodeDescription n1=new nodeDescription();
    			   n1.nodeIndex=vertice_temp;
    			   n1.nodeLabel=name;
    			   vertices.put(userId,n1); //put into Hash
    			   verticesReverse.put(name,n1.nodeIndex);
    			  
    			   vertice_temp++;

    			   if((line_counter%50000)==0){
    				   System.out.println(DateUtils.now()+"         --> Lines read:"+line_counter);

    			   }
    			   line_counter++;
    		   }
    		   in1=inputStream.readLine();
    		   in1=inputStream.readLine();
    		   // READ EDGES
    		   while(in1!=null)
    		   {
    			   int userId1=0;
    			   int userId2=0;

    			   TokSequence ts = new TokSequence(new StringTokenizer(in1));  
    			   userId1 = ts.getInt(); 
    			   userId2 = ts.getInt();
 
    			   int idx1=vertices.get(userId1).nodeIndex;
    			   int idx2=vertices.get(userId2).nodeIndex;

    			   //check the loop and avoid loops 
    			   if (idx1==idx2)
    			   {
    				   System.out.println(DateUtils.now()+"         --> self loop for ID:"+idx1);
    				   selfLoops++;
    				   in1=inputStream.readLine();
    				   continue;
    			   }

    			   double weight=1.0;
    			   if(isNetworkWeighted==1)
    				   try{

    					   weight=ts.getDouble();
    				   }catch(NoSuchElementException e){
    					   System.out.println(DateUtils.now()+"         --> NoSuchElement for weight:");
    					   e.printStackTrace();
    					   isNetworkWeighted=0;
    				   }


    			   if(userId1!=userId2)
    				   if(MainArray[idx1].CheckNeigbourhood(idx2)==false)
    				   {
    					   MainArray[idx1].put(idx2,weight);
    					   totalWeight+=weight;

    					   // for undirected graph, second insertion 
    					   if(MainArray[idx2].CheckNeigbourhood(idx1)==false)
    						   MainArray[idx2].put(idx1,weight);

    				   }
    			   in1=inputStream.readLine();

    			   if((line_counter%50000)==0)
    			   {System.out.println(DateUtils.now()+"         --> Lines read:"+line_counter);

    			   }
    			   line_counter++;
    		   }
    		   bufferStr=DateUtils.now()+"         --> Lines read:"+line_counter;
    		   System.out.println(bufferStr);

    		   bufferStr=DateUtils.now()+"         --> Network file finished succesfully!";
    		   bufferStr=bufferStr+"\n"+DateUtils.now()+"         --> Total weight of edges="+totalWeight;
    		   System.out.println(bufferStr);

    		   inputStream.close();
  
    	   }catch (IOException e){
    		   System.out.println("IOException:");
    		   e.printStackTrace();
    	   }

    	   for(int i2=0;i2<vertice_count;i2++)
    	   {
    		   if(MainArray[i2].neighbour_count==0)
    			   numberOfIslands++;
    	   }

    	   bufferStr=DateUtils.now()+"         --> Number of vertices="+vertice_count;
    	   System.out.println(bufferStr);
    	   System.out.println(DateUtils.now()+"         --> Number of islands="+numberOfIslands);
    	   System.out.println(DateUtils.now()+"         --> Number of self loops avoided="+selfLoops);


    	   //  ****COUNT ALL THE EDGES
    	   for(int i=0;i<vertice_count;i++)
    		   edge_count=edge_count+MainArray[i].neighbour_count;
    	   edge_count=edge_count/2;

    	   bufferStr=DateUtils.now()+"         --> Number of edges="+edge_count;
    	   System.out.println(bufferStr);
 
    	   System.out.println(DateUtils.now()+" FUNCTIION-readPajekformat ended------------------------------");
   	}
 
}

       
	
	 	
		   
	 
	 

