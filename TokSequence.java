import java.util.StringTokenizer;
public class TokSequence { 
	private StringTokenizer tk;  
	public int token_count;
	public TokSequence(StringTokenizer tk) 
	{    
		this.tk = tk;  
		token_count=tk.countTokens();
	
	}  
	public int getInt() 
	{    
		return Integer.valueOf(tk.nextToken()).intValue();  
	
	}  
	public double getDouble() 
	{    
		return Double.valueOf(tk.nextToken()).doubleValue();  
	}  
	public String getString() 
	{    
		return String.valueOf(tk.nextToken());   
       
	}
	
}