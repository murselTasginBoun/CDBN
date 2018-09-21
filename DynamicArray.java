import java.io.File;
import java.util.*;

//import MainClass.nmiStatistics;
/**
 * @author Mursel
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

public class DynamicArray {

private int[] data;  
private edgeData edges[];
 
public HashMap<String,Double> parameterMap;
public HashMap<Integer,String> eventLog;
public HashMap<Integer,Double> communityList;

 
public String name;
public String history;
public int updateFlag;
public  int neighbour_count;
public int incoming_links;
public double weight;
public double modularity;
public double clusteringCoefficient;
public double shortestPathLengths;
public int shortestPathCount; //number of shortest paths starting from this node
public double betweenness;
public int state;

public int boundaryState; //0: not a borderNode and not in borderStack, 1: borderNode in borderStack
public int boundaryNodeStateCounter; //how many times its borderNode state changes

public String neighbourList;
public int communityID,communityArc;
public int cascadeCount,cascadeCountW;
public double SF;
public int gossipSpreadCount;
public int CIDchangeCount;
 
public class edgeData
{
	private int data;
	public double weight,negativeWeight;
	public double edgeBetweenness;
	public int lastInteractionTime;
	public double mutualScore;
	public edgeData()
	{
		data=0; //nodeID
		weight=0.0;
		edgeBetweenness=0.0;
		lastInteractionTime=0;
		negativeWeight=0.0;
		
	}
}


public DynamicArray() {
   edges=new edgeData[1];
   edges[0]=new edgeData();
   data = new int[1];
   updateFlag=1;
   
   parameterMap=new HashMap();
    
   eventLog=new HashMap<Integer,String>();
   communityList=new HashMap<Integer,Double>();
   name=" ";
   neighbour_count=0;
   
   incoming_links=0;
   gossipSpreadCount=0;
   
   weight=1.0;
   modularity=-1.0;
   state=0;

   clusteringCoefficient=0.0;
   shortestPathLengths=0.0;
   shortestPathCount=0;
   neighbourList="";
   cascadeCount=0;
   cascadeCountW=0;
   SF=0.0;
   
   boundaryState=0; 
   boundaryNodeStateCounter=0;  
   CIDchangeCount=0;
  
}



public void writeLog(String str)
{
	int currentCounter=this.eventLog.size()+1;
	this.eventLog.put(currentCounter, str);
}

public void putCID(int CID)
{
	this.communityList.put(CID, 1.0);
}

public void putCID(int CID,double weight)
{
	this.communityList.put(CID, weight);
}

public boolean isMember(int CID)
{
	if(communityList.get(CID)!=null)
		return true;
	else
		return false;
}

public void removeCID(int CID)
{
	this.communityList.remove(CID);
}

public void readLog()
{
	for(int i=1;i<eventLog.size();i++)
	{
		System.out.println("name["+name+"].record("+i+"): "+eventLog.get(i));
	}
}

public String readLogStr()
{
	String retVal="";
	for(int i=1;i<eventLog.size();i++)
	{
		retVal=retVal+"D["+name+"].log["+i+"]:"+eventLog.get(i)+"\\n"; 
		//System.out.println("name["+name+"].record("+i+"): "+eventLog.get(i));
	}
	return retVal;
}

 

 

public double getAllNodesWeights()
{
	double allWeights=0.0;
	for(int i=0;i<this.neighbour_count;i++)
		allWeights+=this.edges[i].weight;
	return allWeights;
}

public double getNodeWeight(int neighbourID)
{
	double weight=-1.0;
	for(int i=0;i<this.neighbour_count;i++)
		if(this.get(i)==neighbourID)
			weight=edges[i].weight;

	return weight;
}

public double getEdgeWeight(int neighbourID)
{
	double weight=-1.0;
	for(int i=0;i<this.neighbour_count;i++)
		if(this.get(i)==neighbourID)
			weight=edges[i].weight;

	return weight;
}

public double getNodeMutualScore(int neighbourID)
{
	double mutualScore=-1.0;
	for(int i=0;i<this.neighbour_count;i++)
		if(this.get(i)==neighbourID)
			mutualScore=edges[i].mutualScore;

	return mutualScore;
}

public void setNodeMutualScore(int neighbourID,double mutualScore)
{
	
	for(int i=0;i<this.neighbour_count;i++)
		if(this.get(i)==neighbourID)
			edges[i].mutualScore=mutualScore;

}

public void setNodeWeight(int neighbourID,double weight)
{
	
	for(int i=0;i<this.neighbour_count;i++)
		if(this.get(i)==neighbourID)
			edges[i].weight=weight;

}

public void setEdgeWeight(int neighbourID,double weight)
{
	
	for(int i=0;i<this.neighbour_count;i++)
		if(this.get(i)==neighbourID)
			edges[i].weight=weight;

}
public int get(int position) {
  /* if (position >= data.length)
      return 0;
   else
      return data[position];*/
	if(position>=edges.length)
		return 0;
	else
		return edges[position].data;
}


public void remove(int nodeID)
{
	int removalPos=0;
	for(int i=0;i<this.neighbour_count;i++)
	{
		//System.out.println(name+"'s Neighbour data="+edges[i].data+" and nodeID="+nodeID);
		if(edges[i].data==nodeID)
		{
			removalPos=i;
			//System.out.println(this.name+" to "+nodeID+" is removed");
			break;
		}
	}
	//System.out.println("RemovalPos="+removalPos);
	for(int i=removalPos;i<this.neighbour_count-1;i++)
		edges[i].data=edges[i+1].data;
	edgeData newData[]=new edgeData[this.neighbour_count-1];
	for(int i=0;i<this.neighbour_count-1;i++)
		newData[i]=new edgeData();
	System.arraycopy(edges, 0, newData, 0, edges.length-1);
    edges = newData;
    this.neighbour_count--;
}
public boolean put(int neighbourID, double weight) {
	if (this.CheckNeigbourhood(neighbourID)) //if it is already neighbour, do not insert it
		   return false;
	else
	{
		int position=this.neighbour_count;
		if (position >= edges.length) 
		{
			int newSize = edges.length+1;
			if (position >= newSize)
				newSize = position+1;
			edgeData newData[] = new edgeData[newSize];
			for(int i=0;i<newSize;i++)
				newData[i]=new edgeData();

			System.arraycopy(edges, 0, newData, 0, edges.length);
			edges = newData;

			// System.out.println("Size of dynamic array increased to " + newSize);
		}
		edges[position].data = neighbourID;
		edges[position].weight=weight;
		neighbour_count++;
		// System.out.println(value+" is connected to me("+this.name+")");
	
		return true;
	}
}


public void put(int position, int value) {
   if (this.CheckNeigbourhood(value)) //if it is already neighbour, do not insert it
	   return;
   
	if (position >= edges.length) 
   {
      int newSize = edges.length+1;
      if (position >= newSize)
         newSize = position+1;
     edgeData newData[] = new edgeData[newSize];
      for(int i=0;i<newSize;i++)
    	  newData[i]=new edgeData();
      
      System.arraycopy(edges, 0, newData, 0, edges.length);
      edges = newData;
     // System.out.println("Size of dynamic array increased to " + newSize);
   }
   edges[position].data = value;
   neighbour_count++;
   //System.out.println(value+" is connected to me("+this.name+")");
}


public void rewire(int position, int value) {
	   if (position >= edges.length) 
	      System.out.println("Size of dynamic array is larger than given position");
	   else
	   {edges[position].data = value;
	   //System.out.println(value+" is rewired to me("+this.name+")");
	   }
	}

 

public boolean CheckNeigbourhood(int value)
{
	boolean return_val=false;
	for(int i=0;i<neighbour_count;i++)
		{
		if(edges[i].data==(value))
		{
			return_val=true;
			i=neighbour_count+1;
		}
		      else
		      	return_val=false;
		}
	
	return return_val;
	
	
}
 

} 
