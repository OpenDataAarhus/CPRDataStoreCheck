package dk.aarhuskommune.odaa;

public class ReadArguments {
	
	private String args[]; 
	
	public ReadArguments(String[] args) {
		super();
		this.args = args;
	}

	public String getParameter(String parameter) {
		for (int i=0;i<args.length;i++) {			
			if ((" " + args[i]).indexOf(" " + parameter)>-1) {
				int ai=args[i].indexOf("=");
				if (ai>0) {
					return args[i].substring(ai+1);
				} else {
					return args[i+1];
				}
			}
		}		
		return null;
	}
	
}
