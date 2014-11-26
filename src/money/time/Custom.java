package money.time;

public class Custom {
	private String customBig;
	private String customSmall;
	private String dateString;
	
	public Custom(String string, String string2, String string3) {
		this.customBig = string;
		this.customSmall = string2;
		this.dateString = string3;
		
	}
	public String getcustomBig() { return customBig; }
	
	public void setcustomBig(String customBig) { this.customBig = customBig; }
	
	public String getcustomSmall() { return customSmall; }
	
	public void setcustomSmall(String customSmall) { this.customSmall = customSmall; }
	
	public String getdateString() { return dateString; }
	
	public void setdateString(String dateString) { this.dateString = dateString; }
}
