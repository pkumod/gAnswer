package qa;

import java.util.ArrayList;


public class Answer implements Comparable<Answer>{
	public String questionFocusKey=null;
	public String questionFocusValue=null;
	public ArrayList<String> otherInformationKey = null;
	public ArrayList<String> otherInformationValue = null;
	
	public Answer(String qf, String[] ans) {
		otherInformationKey = new ArrayList<String>();
		otherInformationValue = new ArrayList<String>();
		int p1, p2;
		for (String line : ans) {
			System.out.println("line=" + line);
			if (line.startsWith(qf)) {
				questionFocusKey  = qf;
				p1 = line.indexOf('<');
				p2 = line.lastIndexOf('>');
				String value = null;
				if (p1 != -1 && p2 != -1) {
					value = line.substring(p1+1, p2);
				}
				else {
					p1 = line.indexOf('\"');
					p2 = line.lastIndexOf('\"');
					if(p1 != -1 && p2 != -1)
						value = line.substring(p1+1, p2);
					else
					{
						p1 = line.indexOf(':');
						value = line.substring(p1+1);
					}
				}
				questionFocusValue = value;
			}
			else {
				
				p1 = line.indexOf(':');
				String key = line.substring(0, p1);

				p1 = line.indexOf('<');
				p2 = line.lastIndexOf('>');
				String value = null;
				if (p1 != -1 && p2 != -1) {
					value = line.substring(p1+1, p2);
				}
				else {
					p1 = line.indexOf('\"');
					p2 = line.lastIndexOf('\"');
					if(p1 != -1 && p2 != -1)
						value = line.substring(p1+1, p2);
					else
					{
						p1 = line.indexOf(':');
						value = line.substring(p1+1);
					}
				}
				
				otherInformationKey.add(key);
				otherInformationValue.add(value);
			}
		}
		
		// Sove BUG: GStore return messy code in questionFocusKey
		if (questionFocusKey==null || questionFocusValue==null)
		{
			questionFocusKey  = qf;
			String line = ans[0];
			p1 = line.indexOf('<');
			p2 = line.lastIndexOf('>');
			String value = null;
			if (p1 != -1 && p2 != -1) {
				value = line.substring(p1+1, p2);
			}
			else {
				p1 = line.indexOf('\"');
				p2 = line.lastIndexOf('\"');
				if(p1 != -1 && p2 != -1)
					value = line.substring(p1+1, p2);
				else
				{
					p1 = line.indexOf(':');
					value = line.substring(p1+1);
				}
			}
			questionFocusValue = value;			
			otherInformationKey.clear();
			otherInformationValue.clear();
		}
		
		/*System.out.println("otherInformationKey.size=" + otherInformationKey.size());
		for (String k : otherInformationKey) {
			System.out.println("otherInfoKey = " + k);
		}*/
	}
	
	public int compareTo (Answer p)
	{
		return questionFocusValue.compareTo(p.questionFocusValue);
	}
	
}
