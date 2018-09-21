import java.io.IOException;


public class MainClass {
	public static void main (String[] args) {
		
		       String headerMsg=" ------Community detection using boundary nodes ------------------------------\n";
		    headerMsg=headerMsg+"|>> Citation info >>:                                                        |\n";
		     String citationMsg="| Tasgin, M. and H. O. Bingol, “Community detection using boundary nodes in  |\n";
		citationMsg=citationMsg+"| complex networks”, Physica A: Statistical Mechanics and its Applications   |\n";
		citationMsg=citationMsg+"| Vol.513,pp.315–324,2019                                                    |\n";
		citationMsg=citationMsg+"| http://www.sciencedirect.com/science/article/pii/S0378437118311658         |\n";
		citationMsg=citationMsg+=" ----------------------------------------------------------------------------\n";
	           
	           System.out.print(headerMsg);
	           System.out.print(citationMsg);
	         
	           
		if(args.length==0)
		{
			System.out.println("Usage: CDwBN.jar inputfilename weightOption");
			System.out.println("Option:1           - CDwBN - Community detection using boundary nodes - Individual approach");
			System.out.println("Option:2 (default) - CDwBN - Community detection using boundary nodes - Group approach");
			return;
		}
		
		if(args.length==1)
		{
			System.out.println(DateUtils.now()+"         --> Working on inputfile:"+args[0]+" using CDwBN with default option: Group approach");
			CommunityDetection C1=new CommunityDetection();
			C1.CDwBN_method=2;
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
			String givenOption="Group approach";
			if(Integer.valueOf(args[1])==1)
				givenOption="Individual approach";
			if(!(Integer.valueOf(args[1])==1 || Integer.valueOf(args[1])==2))
			{
				System.out.println("Invalid option given!");
				
				System.out.println("Usage: CDwBN.jar inputfilename weightOption");
				System.out.println("Option:1           - CDwBN - Community detection using boundary nodes - Individual approach");
				System.out.println("Option:2 (default) - CDwBN - Community detection using boundary nodes - Group approach");
				return;
				
			}
			System.out.println(DateUtils.now()+"         --> Working on inputfile:"+args[0]+" using CDwBN with the given option:"+givenOption);
			CommunityDetection C1=new CommunityDetection();
			C1.CDwBN_method=Integer.valueOf(args[1]);
			try {
				C1.communityDetection(args[0]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
