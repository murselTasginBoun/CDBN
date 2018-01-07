
import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;


import javax.swing.JFrame;
import javax.swing.JTextArea;




import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.StandardCopyOption.*;

 
 

public class CommunityDetection {
    
    
	static int line_counter=0;
	public static int selfLoops=0;
    String lineStr="";
    
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
	public int CDwPN_method=0;
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
    
   
    

    
    
    public void constructPreferenceNetwork(DynamicArray D1[],boolean logging)
    {
    	HashMap<Integer,Double> firstScores=new HashMap<Integer,Double>();
    	HashMap<Integer,Double> secondScores=new HashMap<Integer,Double>();
    	HashMap<Integer,Double> finalScores=new HashMap<Integer,Double>();
    	 
    	//Initialize communities of size-1 (self communities)
    	for(int i=0;i<D1.length;i++)
    		D1[i].communityID=i;
    	
    	for(int i=0;i<D1.length;i++)
    	{
    		double maxContribution=0;
    		double maxScore=0;
    		firstScores.clear();
    		secondScores.clear();
    		finalScores.clear();
    		
    		int Idx=i;
    		int AltIdx=i;
    		for(int j=0;j<D1[i].neighbour_count;j++)
    		{
    			int neighbourID=D1[i].get(j);
    			double contribution=(double)D1[neighbourID].getEdgeWeight(i);
    			double myContribution=(double)D1[i].getEdgeWeight(neighbourID);
    			double alternateContribution=0;

    			double totalContribution=0;
    			totalContribution=contribution;
    			alternateContribution=myContribution;

    			if(contribution>=maxContribution)
    			{
    				maxContribution=contribution;
    				Idx=neighbourID;
    				firstScores.put(neighbourID,totalContribution);
    				secondScores.put(neighbourID, alternateContribution);	
    			}	
    		}
    		//Just insert the neighbour having highest contribution
    		finalScores.clear();
    		{
    			Iterator<Integer> J1=firstScores.keySet().iterator();
    			while(J1.hasNext())
    			{
    				Object ID=J1.next();
    				double scoreVal=firstScores.get(ID);
    				if(scoreVal==maxContribution)
    					finalScores.put(Integer.valueOf(ID.toString()), scoreVal);
    			}	
    		}
    		// maxContribution is found
    		//if there are more than 1 maxContribution, then look at the alternateContribution
    		{// No alternate scores
    			if(finalScores.size()>0)
    			{
    				maxScore=0;		
    				int randInt=rand1.nextInt(finalScores.size());	
    				int randCntr=0;
    				Iterator<Integer> I1=finalScores.keySet().iterator();
    				while(I1.hasNext())
    				{
    					Object IDx1=I1.next();
    					if(randCntr++==randInt)
    					{
    						int neighbourIdx=Integer.valueOf(IDx1.toString());
    						D1[i].communityID=D1[neighbourIdx].communityID; 
    						break;
    					}
    				}
    			}
    			else
    				D1[i].communityID=i;
    		}		
    	}
    	
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
    	 
    	Random randNumber=new Random(System.currentTimeMillis());		
    	 
    	HashMap<Integer,Integer> neighbourMap=new HashMap<Integer,Integer>();
    	Stack<Integer> tempGossipStack=new Stack<Integer>();
    	double cascadeSize=0;
    	int currentSpreader=0;
    	int currentVictimN=0;
    	double spreadF=0.0,deltaSF=0.0;
    	 
    	inputFileName=fileN;

		//--------------READ NETWORK INPUT FILE ---------------------------------------------------------------------

		if(inputFileName.contains(".gml"))							
			readGMLformat(inputFileName);			
		if(inputFileName.contains(".txt") || inputFileName.contains(".net"))				
			readPajekformat(inputFileName,true);
		if(inputFileName.contains(".dat"))				
			this.readDATformat(inputFileName);
	 
		networkSize=this.vertice_count;
		

    	//Alternative-1: Common neighbors-----------------------------------------------------------------------------------------
    	if(CDwPN_method==0) //Common neighbours as edge weights
    	{
    		for(int i=0;i<MainArray.length;i++)
    			for(int j=0;j<MainArray[i].neighbour_count;j++)
    			{
    				int neighbourID=MainArray[i].get(j);
    				int commonNeighbours=this.calculateCommonNeighbours(MainArray, i, neighbourID);
    				MainArray[i].setEdgeWeight(neighbourID, commonNeighbours);
    			}		 	
    	}
    	//------------------------------------------------------------------------------------------------------------------------

    	
    	// Alternative-2: Spread capability (Gossip algorithm)--------------------------------------------------------------------
    	if (CDwPN_method==1)
    	{ 	
    		Stack<Integer> cascadeMembers=new Stack<Integer>();
    		//1.1 Spread_GOSSIP.start -------------------------------------------------------------------------------------------
    		for(int i=0;i<MainArray.length;i++)
    		{
    			neighbourMap.clear();
    			cascadeSize=1;
    			spreadF=0.0;
    			deltaSF=0.0;
    		 
    			
    			//1.2 Put neighbours into array as originators -----------------------------------------------------------------
    			for(int j=0;j<MainArray[i].neighbour_count;j++)
    			{
    				int x=MainArray[i].get(j);
    				MainArray[x].state=-(i+1);
    				MainArray[i].state=0;
    				neighbourMap.put(x, x);
    				//if(debugMode) System.out.print(x+"-");
    			} 
    			currentVictimN=MainArray[i].neighbour_count;
    			
    			//1.3 select neighbours as originators one-by-one  --------------------------------------------------------------
    			while(!neighbourMap.isEmpty())
    			{
    				Iterator<Integer> STkey=neighbourMap.keySet().iterator();
    				while(STkey.hasNext())
    				{
    					Object IDx=STkey.next();
    					currentSpreader=neighbourMap.get(IDx);
    					neighbourMap.remove(IDx);
    					tempGossipStack.push(currentSpreader);
    					cascadeSize=1;
    					deltaSF=0.0;
    					break;
    				}
    				cascadeMembers.clear();
    				int initiatorID=currentSpreader;
    				cascadeMembers.push(initiatorID);
    				
    			//1.3.1 start spreading gossip with the selected originator	-----------------------------------------------------
    				while(!tempGossipStack.isEmpty())
    				{		 
    					currentSpreader=tempGossipStack.pop();
    					//1.3.1.1 find members on spreading cascade ------------------------------------------------------------- 
    					for(int k=0;k<MainArray[currentSpreader].neighbour_count;k++)
    					{	
    						int id1=MainArray[currentSpreader].get(k);
    						MainArray[currentSpreader].state=0;
    						int y=MainArray[id1].state+(i+1);
    						if(y==0)
    						{
    							MainArray[id1].state=0;
    							cascadeSize++;
    							neighbourMap.remove(id1);
    							cascadeMembers.push(id1);
    							tempGossipStack.push(id1);
    						}
    					}
    					//1.3.1.1.end -------------------------------------------------------------------------------------------- 
    				}
    				//1.3.1.end - spread with the selected originator finished ---------------------------------------------------
    				deltaSF=((double)(cascadeSize*cascadeSize))/(currentVictimN*currentVictimN);
    				//1.3.2 set SF for each cascade members ----------------------------------------------------------------------
    				while(!cascadeMembers.empty())
    				{
    					int cascadeMemberIDX=cascadeMembers.pop();
    					MainArray[cascadeMemberIDX].setEdgeWeight(i,deltaSF);
    				}
    				//1.3.2.end -------------------------------------------------------------------------------------------------   				
    				spreadF=spreadF+deltaSF;			 
    			}
    			//1.3.end --------------------------------------------------------------------------------------------------------	
    			//1.4 set SF values ----------------------------------------------------------------------------------------------
    			MainArray[i].SF=spreadF*MainArray[i].neighbour_count;  
    			 
    			if(MainArray[i].neighbour_count<2)
    				MainArray[i].SF=0;
    			//1.4.end --------------------------------------------------------------------------------------------------------
    		} // end of for loop
    		//1.1 end ------------------------------------------------------------------------------------------------------------
		}
    	//1.end ------------------------------------------------------------------------------------------------------------------

    	//Construct preference network using highest edge weight for each node ---------------------------------------------------
    	constructPreferenceNetwork(MainArray, false);

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
    	System.out.println(DateUtils.now()+"-FUNCTION-printCMTYfile started");
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
    	
    	System.out.println(DateUtils.now()+"All communities are put into list. Number of distinct communities:"+communityMap.size());
    	//1.end ---------------------------------------------------------------------------------
    	
    	
    	//2.Read from communityMAP and insert into records  List --------------------------------
    	

    	try {
    		BufferedWriter writer;
    		if (firstTime==0)
    			writer = new BufferedWriter(new FileWriter(outputFileName, false),bufSize);
    		else //append the file
    			writer = new BufferedWriter(new FileWriter(outputFileName, true),bufSize);
    		System.out.println(DateUtils.now()+"         -->Community (CMTY) file is being written");	

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
    				System.out.println(DateUtils.now()+"         -->"+cacheLimit+" number of records written to disk");
    			}
    		}
    		//2.end ---------------------------------------------------------------------------------

    		//3.Flush remaining records to disk
    		System.out.println(DateUtils.now()+"         -->"+records.size() + " remaining records will be written");

    		for (String record: records) {
    			writer.write(record);
    		}
    		writer.flush();
    		records.clear();

    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	System.out.println(DateUtils.now()+"         --> CMTY file is written:"+outputFileName);
    	System.out.println(DateUtils.now()+"-FUNCTION-printCMTYfile ended");
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
    	System.out.println(DateUtils.now()+"-("+lineCounter+")-"+"-FUNCTION readGMLformat started");
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
		
	System.out.println(DateUtils.now()+"-("+lineCounter+")-"+"         -->V1.size="+V1.size()+" E1.size="+E1.size());

	vertice_count=V1.size();
	this.networkSize=V1.size(); 
		MainArray=new DynamicArray[V1.size()];
		for(i=0;i<V1.size();i++)
			MainArray[i]=new DynamicArray();	
   		
    
   		System.out.println(DateUtils.now()+"-("+lineCounter+")-"+"      --> doReadGML.starttime");
   		 
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
   			  	System.out.println(DateUtils.now()+"-("+lineCounter+")-"+"         -->readGMLformat-Lines read:"+line_counter);
   			  
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
   			  	System.out.println(DateUtils.now()+"-("+lineCounter+")-"+"         -->readGMLformat-Lines read:"+line_counter);
   			   
   			line_counter++;
   			i++;  
   		}
   		bufferStr=DateUtils.now()+"-("+lineCounter+")-"+"         -->readGMLformat-Lines read:"+line_counter+" "+DateUtils.now();
   	  	System.out.println(bufferStr);
   	  	 
   	   
   	  	
   	  	bufferStr=DateUtils.now()+"-("+lineCounter+")-"+"         -->readGMLformat- Network file finished succesfully!";
   	  	bufferStr=bufferStr+"\n"+DateUtils.now()+"-("+lineCounter+")-"+"         -->readGMLformat-Total weight of edges="+totalWeight;
   	  	bufferStr=bufferStr+"\n"+DateUtils.now()+"-("+lineCounter+")-"+"         -->readGMLformat-Total number of nodes="+this.networkSize;
   	  	bufferStr=bufferStr+"\n"+DateUtils.now()+"-("+lineCounter+")-"+"         -->readGMLformat-Total number of edges="+this.edge_count;
   	  	System.out.println(bufferStr);
   	  	 
   	    
   		inputStream.close();
   		
   	}catch (IOException e){
   		System.out.println("IOException:");
           e.printStackTrace();
   	}
  
   	bufferStr=DateUtils.now()+"-("+lineCounter+")-"+"         -->readGMLformat-Number of vertices="+vertice_count;
   	System.out.println(bufferStr);
    
    
   	
    //  ****COUNT ALL THE EDGES
 	for(i=0;i<vertice_count;i++)
 	edge_count=edge_count+MainArray[i].neighbour_count;
 	edge_count=edge_count/2;
	
	bufferStr=DateUtils.now()+"-("+lineCounter+")-"+"         -->readGMLformat-Number of edges="+edge_count;
	System.out.println(bufferStr);
	 
	 
	System.out.println(DateUtils.now()+"-("+lineCounter+")-"+"-FUNCTION readGMLformat ended");
   
    }
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
   

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public  void readDATformat(String inputfile)
    {
    	System.out.println(DateUtils.now()+"-("+lineCounter+")-"+"-FUNCTION-readDATformat started");
    	System.out.println(DateUtils.now()+"-("+lineCounter+")-"+"         -->readDATformat- Reading file:"+inputfile);
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
    				System.out.println(DateUtils.now()+"-("+lineCounter+")-"+"          -->readDATformat- Reading lines:"+counter);
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

    	System.out.println(DateUtils.now()+"-("+lineCounter+")-"+"         -->readDATformat- Network size="+vertice_count);

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
    			System.out.println(DateUtils.now()+"-("+lineCounter+")-"+"          -->readDATformat- MainArray entries inserted:"+counter);
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
    			System.out.println(DateUtils.now()+"-("+lineCounter+")-"+"          -->readDATformat- Edges are inserted:"+counter);
    		}
    	}

    	bufferStr=DateUtils.now()+"-("+lineCounter+")-"+"         -->readDATformat- Number of vertices="+vertice_count;
    	System.out.println(bufferStr);
    	 

    	//  ****COUNT ALL THE EDGES
    	for(int i=0;i<vertice_count;i++)
    		edge_count=edge_count+MainArray[i].neighbour_count;
    	edge_count=edge_count/2;

    	bufferStr=DateUtils.now()+"-("+lineCounter+")-"+"         -->readDATformat- Number of edges="+edge_count;
    	System.out.println(bufferStr);
    	 
    	//write_to_log(bufferStr,logfile,0);
    	System.out.println(DateUtils.now()+"-("+lineCounter+")-"+"FUNCTION-readDATformat ended:");
    }
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++  
    public  void readPajekformat(String inputfile,boolean AddExtraChars2Name ) throws IOException
   	{
    	 
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
    		   System.out.println(DateUtils.now()+"-("+lineCounter+")-"+"         -->readPajekformat-Openning the file="+inputfile);
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

    		   System.out.println(DateUtils.now()+"-("+lineCounter+")-"+"         -->readPajekformat-Network Size is:"+MainArray.length);
    		  

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
    				   name=name.replace("\"", "")+"-"+userId;//names may not be unique 27.10.2015
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
    				   System.out.println(DateUtils.now()+"-("+lineCounter+")-"+"         -->readPajekformat-Lines read:"+line_counter);

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
    				   System.out.println(DateUtils.now()+"-("+lineCounter+")-"+"readPajekformat-self loop for ID:"+idx1);
    				   selfLoops++;
    				   in1=inputStream.readLine();
    				   continue;
    			   }

    			   double weight=1.0;
    			   if(isNetworkWeighted==1)
    				   try{

    					   weight=ts.getDouble();
    				   }catch(NoSuchElementException e){
    					   System.out.println(DateUtils.now()+"-("+lineCounter+")-"+"readPajekformat-NoSuchElement for weight:");
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
    			   {System.out.println(DateUtils.now()+"-("+lineCounter+")-"+"         -->readPajekformat-Lines read:"+line_counter);

    			   }
    			   line_counter++;
    		   }
    		   bufferStr=DateUtils.now()+"-("+lineCounter+")-"+"         -->readPajekformat-Lines read:"+line_counter;
    		   System.out.println(bufferStr);

    		   bufferStr=DateUtils.now()+"-("+lineCounter+")-"+"         -->readPajekformat-Network file finished succesfully!";
    		   bufferStr=bufferStr+"\n"+DateUtils.now()+"-("+lineCounter+")-"+"         -->readPajekformat-Total weight of edges="+totalWeight;
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

    	   bufferStr=DateUtils.now()+"-("+lineCounter+")-"+"         -->readPajekformat-Number of vertices="+vertice_count;
    	   System.out.println(bufferStr);
    	   System.out.println(DateUtils.now()+"-("+lineCounter+")-"+"         -->readPajekformat-Number of islands="+numberOfIslands);
    	   System.out.println(DateUtils.now()+"-("+lineCounter+")-"+"         -->readPajekformat-Number of self loops avoided="+selfLoops);


    	   //  ****COUNT ALL THE EDGES
    	   for(int i=0;i<vertice_count;i++)
    		   edge_count=edge_count+MainArray[i].neighbour_count;
    	   edge_count=edge_count/2;

    	   bufferStr=DateUtils.now()+"-("+lineCounter+")-"+"         -->readPajekformat-Number of edges="+edge_count;
    	   System.out.println(bufferStr);
 
    	   System.out.println(DateUtils.now()+"-("+lineCounter+")-"+"-FUNCTIION-readPajekformat ended");
   	}
 
}

       
	
	 	
		   
	 
	 

