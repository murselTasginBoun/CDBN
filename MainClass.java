import java.io.IOException;


public class MainClass {
	public static void main (String[] args) {
		
		if(args.length==0)
		{
			System.out.println("Usage: CDwPN.jar inputfilename weightOption");
			System.out.println("weightOption:0 (default) - PCN - Community detection using common neighbors");
			System.out.println("weightOption:1           - PSC - Community detection using gossip spread capability");
			return;
		}
		
		if(args.length==1)
		{
			System.out.println("Working on inputfile:"+args[0]+" using CDwPCN- common neighbors");
			CommunityDetection C1=new CommunityDetection();
			C1.CDwPN_method=0;
			try {
				C1.communityDetection(args[0]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		if(args.length==2)
		{
			System.out.println("Working on inputfile:"+args[0]+" using CDwPCN- common neighbors");
			CommunityDetection C1=new CommunityDetection();
			C1.CDwPN_method=Integer.valueOf(args[1]);
			try {
				C1.communityDetection(args[0]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
