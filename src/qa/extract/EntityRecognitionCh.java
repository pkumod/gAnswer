package qa.extract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import lcn.EntityFragmentFields;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode;
import com.huaban.analysis.jieba.SegToken;

import edu.stanford.nlp.util.Pair;
import fgmt.TypeFragment;
import qa.Query;
import rdf.EntityMapping;
import rdf.TypeMapping;
import nlp.ds.*;
import utils.FileUtil;

final class MODNUM
{
	public static int prime=9999991;
}
//TODO: replace by nlp.ds.word
class Word
{
	//type:0=normal word 1=entity 2=literal(string)
	String word;
	int type;
	int pos=0;
	List<String> entList=null;
	Word(String w)
	{
		word=w;
		type=0;
	}	
	Word(String w,int i)
	{
		word=w;
		type=i;
	}
	Word(String w,int i, int j)
	{
		word=w;
		type=i;
		pos=j;
	}	
	Word(String w,int i, int j,List<String> l)
	{
		word=w;
		type=i;
		pos=j;
		entList=l;
	}	
}

class Ent
{
	public final int mod=MODNUM.prime;
	public String entity_name,mention;
	public int no;
	public long hashe,hashm;
	public Ent(String load)
	{
		int indexOf9=load.indexOf(9);
		if (indexOf9>=0)
		{
			mention=load.substring(0, indexOf9);
			String tmp=load.substring(indexOf9+1);
			int t9=tmp.indexOf(9);
			if (t9>=0)
			{
				entity_name=tmp.substring(0, t9);
				String numberStr=tmp.substring(t9+1);
				try
				{
					no=Integer.valueOf(numberStr);
				}catch(Exception e){no=-1;};
			}
			else entity_name=tmp;
			hashe=calHash(entity_name);			
		}
		else
		{
			mention=load;
			hashe=-1;
		}
		hashm=calHash(mention);
	}
	public long calHash(String p)
	{
		long x=0;
		if (p==null || p.length()==0) return 0;
		for (int i=0;i<p.length();i++)
		{
			x=x*65536+(long)(int)p.charAt(i);
			x=x%mod;
		}
		return x;
	}
	@Override
	public int hashCode()
	{
		return (int)hashm;
	}
	public Ent(){};
}

public class EntityRecognitionCh {
	public static HashMap<String, List<String>> entMap,nentMap;
	public static JiebaSegmenter segmenter = new JiebaSegmenter();
	
	public final static int MaxEnt=20;
	
	static
	{
		long t0 = System.currentTimeMillis();
		List<String> nent = FileUtil.readFile("data/pkubase/paraphrase/ccksminutf.txt");
		List<String> mention2ent = FileUtil.readFile("data/pkubase/paraphrase/pkubase-mention2ent.txt");		

		entMap=new HashMap<>();
		nentMap=new HashMap<>();

		System.out.println("Mention2Ent size: " + mention2ent.size());
		for (String input:mention2ent)
		{
			Ent q=new Ent(input);
			if (entMap.containsKey(q.mention)) 
				entMap.get(q.mention).add(q.entity_name);
			else
			{
				List<String> l=new ArrayList<>();
				l.add(q.entity_name);
				entMap.put(q.mention, l);
			}
		}
		// mention: NOT ent word; entity_name: frequency	
		for (String input:nent)
		{
			Ent q=new Ent(input);
			if (nentMap.containsKey(q.mention)) 
				nentMap.get(q.mention).add(q.entity_name);
			else
			{
				List<String> l=new ArrayList<>();
				l.add(q.entity_name);
				nentMap.put(q.mention, l);
			}
		}		
		
		long t1 = System.currentTimeMillis();
		System.out.println("Read Mention2Ent used "+(t1-t0)+"ms");	
	}
	
	public static boolean isAllNumber(String q)
	{
		boolean ret=true;
		for (int i=0;i<q.length();i++)
		{
			if (q.charAt(i)<48 || q.charAt(i)>57) return false;
		}
		return ret;
	}
	public static String longestFirst2(String Question)
	{
		String ret="";
		String input=Question.replace('{',' ').replace('}',' ');
		
		int len=input.length();
		int[][] ex=new int[len+3][];
		Ent[][] entx=new Ent[len+3][];
		for (int i=0;i<len+2;i++) ex[i]=new int[len+3];
		for (int i=0;i<len+2;i++) entx[i]=new Ent[len+3];
		for (int l=1;l<=len;l++) 
		{
			int pos=0;
			for (int j=l-1;j<len;j++)
			{
				String searchstr=input.substring(j-l+1,j+1);
				List<String> rstlist=entMap.get(searchstr);

				if (rstlist!=null && rstlist.size()>0)
				{
					++pos;
					ex[l][pos]=j;
					entx[l][pos]=new Ent(searchstr);
				}
			}
			ex[l][0]=pos;
		}	
		int covered[]=new int[len+3];
		for (int l=len;l>=1;l--)
		{
			for (int p=1;p<=ex[l][0];p++)
			{
				int flag=1;
				for (int k=ex[l][p];k>=ex[l][p]-l+1;k--) if (covered[k]>0) flag=0;
				if (flag==1)
				{
					//1:占用  2:词头 4:词尾  8:其他
					int FLAG=0;
					List<String> nlist=nentMap.get(entx[l][p].mention);
					if (nlist!=null && nlist.size()>0) FLAG=8;
					if (isAllNumber(entx[l][p].mention)) FLAG=8;
					
					covered[ex[l][p]]|=4;
					covered[ex[l][p]-l+1]|=2;
					for (int k=ex[l][p];k>=ex[l][p]-l+1;k--)
					{
						covered[k]|=1|FLAG;
					}
				}
			}
		}
		
		for (int i=0;i<len;i++)
		{
			if ((covered[i]&2)!=0 && (covered[i]&8)==0) ret=ret+"{";
			ret=ret+Question.charAt(i);
			if ((covered[i]&4)!=0 && (covered[i]&8)==0) ret=ret+"}";
		}
		//System.out.println("Longest First: "+ret);
		//System.out.println("Time: "+(t1-t0)+"ms");
		return ret;
	}
	//1->①
	public static String intToCircle(int i)
	{
		if (0>i || i>20) return null;
		String ret="";
		ret=ret+(char)(9311+i);
		return ret;
	}
	//①->1
	public static int circleToInt(String i)
	{
		int ret=i.charAt(0)-9311;
		if (0<ret&& ret<20) return ret;
		else return -1;
	}
	public static Pair<String,List<Word>> processedString(String s)
	{
		List<Word> ret=new ArrayList<>();
		String sentence = "";
		int flag=0;
		String word="";
		for (int i=0;i<s.length();i++)
		{
			if (s.charAt(i)=='{')
			{
				flag=1;
				continue;
			}
			if (s.charAt(i)=='}')
			{
				if (word.length()<=2)
				{
					sentence+=word;
					word="";
					flag=0;
					continue;
				}
				int FLAG=-1;
				for (Word j:ret)
					if (word.equals(j.word)) 
						FLAG=j.pos;
				if (FLAG==-1)
				{
					flag=0;
					ret.add(new Word(word,1,ret.size()+1));
					word="";
					sentence+=intToCircle(ret.size());
					continue;
				}
				else
				{
					flag=0;
					word="";
					sentence+=intToCircle(FLAG);
					continue;
				}
			}
			if (flag==0) sentence+=s.charAt(i);
			if (flag==1) word=word+s.charAt(i);
		}
		return new Pair<String,List<Word>>(sentence,ret);
	}
	public static String reprocess(List<Word> d, List<SegToken> list)
	{
		String ret="";
		
		int used[]=new int[list.size()+1];
		int isValid[]=new int[list.size()+1];
		for (int i=0;i<list.size();i++) isValid[i]=0;
		
		
		for(int len=4;len>=1;len--)
		{
			for (int i=0;i<list.size()-len+1;i++)
			{
				String tmp="";
				int flag=1;
				for (int j=i;j<i+len;j++)
				{
					tmp=tmp+list.get(j).word;
					if (tmp.length()>4) flag=0;
					if (circleToInt(list.get(j).word)>=0) flag=0;
					if (used[j]==1) flag=0;
				}
				if (flag==0) continue;
				List<String> rstlist=entMap.get(tmp);
				List<String> nlist=nentMap.get(tmp);
				if (nlist!=null && nlist.size()>0)
				{
					for (int j=i;j<i+len;j++) 
					{
						used[j]=1;	
					}
				}
				if (rstlist!=null && rstlist.size()>0 && (nlist==null||nlist.size()==0))
				{
					for (int j=i;j<i+len;j++) used[j]=1;
					int pos=-1;
					for (Word k:d) if (tmp.equals(k.word))
					{
						pos=k.pos;break;
					}
					if (pos>0) 
					{
						isValid[i]=pos;
						for (int j=i+1;j<i+len;j++)isValid[j]=-1;
					}
					else
					{
						d.add(new Word(tmp,1,d.size()+1));
						isValid[i]=d.size();
						for (int j=i+1;j<i+len;j++)isValid[j]=-1;
					}
				}

			}
		}
		for (int i=0;i<list.size();i++)
		{
			if (isValid[i]==0)
			{
				ret=ret+list.get(i).word;
			}
			if (isValid[i]>0)
			{
				ret=ret+intToCircle(isValid[i]);
			}
		}
		return ret;
	}
	public static String removeQueryId2(String question)
	{
		String ret = question;
		int st = question.indexOf(":");
		if(st!=-1 && st<6  && question.length()>4 && ((question.charAt(0)>='0' && question.charAt(0)<='9') ||question.charAt(0)=='q'))
		{
			ret = question.substring(st+1);
		}
		return ret;
	}
	public static String thirdprocess(String sentence,List<Word> d)
	{
		String temp="",rets2="";
		int insyh=0;
		int count=0;
		List<Integer> lst=new ArrayList<>();
		String syh="";
		for (int i=0;i<sentence.length();i++)
		{
			if (circleToInt(""+sentence.charAt(i))!=-1)
			{
				count++;
			}
			else
			{
				if (count>=3)
				{
					String newent="";
					for (int j=i-count;j<i;j++)
					{
						newent+=d.get(circleToInt(""+sentence.charAt(j))-1).word;
					}
					temp+=intToCircle(d.size());
					d.add(new Word(newent,2,d.size()+1));
				}
				else
					for (int j=i-count;j<i;j++)
					{
						temp+=sentence.charAt(j);
					}
				temp+=sentence.charAt(i);
				count=0;
			}
		}	
		for (int i=0;i<temp.length();i++)
		{
			if (temp.charAt(i)=='"'&&insyh==0 || temp.charAt(i)=='“')
			{
				insyh=1;
				syh="";
				rets2+=temp.charAt(i);
			}
			else if (temp.charAt(i)=='"'&&insyh==1 || temp.charAt(i)=='”')
			{
				insyh=0;
				if (lst.size()>=1)
				{
					String rp="";
					for (int j=0;j<syh.length();j++)
					{
						int q=circleToInt(""+syh.charAt(j));
						if (q==-1) 
							rp+=syh.charAt(j);
						else
						{
							rp+=d.get(q-1).word;
							//ret[q]="";
						}
					}
					d.add(new Word(rp,2,d.size()+1));
					rets2+=intToCircle(d.size())+temp.charAt(i);
				}
				else
				{
					rets2+=syh+temp.charAt(i);
				}
			}
			else if (insyh==1)
			{
				if (circleToInt(""+temp.charAt(i))!=-1)
					lst.add(circleToInt(""+temp.charAt(i)));
				syh+=temp.charAt(i);
			}
			else
				rets2+=temp.charAt(i);
		}
		return rets2;
	}
	
	public static Pair<String,List<Word>> parse(String input, JiebaSegmenter segmenter)
	{
//		input=removeQueryId2(input);	// Remove query id before.
		String newinput=longestFirst2 (input);

		Pair<String,List<Word>> d=null,r=new Pair<String,List<Word>>();
		r.second=new ArrayList<>();
		try {
			d=processedString(newinput);
		} catch (Exception e) {
			System.out.println(e);
		}
		if (d!=null)
		{
			//System.out.println(d.first);
			
			List<SegToken> q=segmenter.process(d.first, SegMode.SEARCH);
			String secondstr="";
			for (SegToken t:q)
			{
				secondstr=secondstr+t.word+",";
			}
			//System.out.println("First process: "+secondstr);

			String finalstring="";
			String stickstr=reprocess(d.second,q);
			String thirdstr=thirdprocess(stickstr,d.second);
			
			List<SegToken> q2=segmenter.process(thirdstr, SegMode.SEARCH);
			for (SegToken t:q2)
			{
				finalstring=finalstring+t.word+",";
				int p=circleToInt(""+t.word.charAt(0));
				if (p!=-1)
				{
					Word ds=d.second.get(p-1);
					r.second.add(new Word(ds.word,ds.type,ds.pos,entMap.get(ds.word)));
				}
				else
				{
					r.second.add(new Word(t.word,0,-1));
				}
			}
			
			System.out.println("Result: "+finalstring);
			
			r.first=thirdstr;
			
			return r;
		}
		else return null;
	}
	
	public static List<nlp.ds.Word> parseSentAndRecogEnt(String sent)
	{
		Pair<String, List<Word>> result = parse(sent, segmenter);
		if(result == null)
			return null;
		
		List<nlp.ds.Word> words = new ArrayList<nlp.ds.Word>();
		int position = 1;
		for(Word ow: result.second)
		{
			// Note: jieba postag is deprecated, so we utilize stanford parser to get postag in later. 
			nlp.ds.Word word = new nlp.ds.Word(ow.word, ow.word, null, position++);
			words.add(word);
			if(ow.type == 1 && ow.entList != null)
			{
				// Now just consider TYPE there in a smiple way.
				if(TypeFragment.typeShortName2IdList.containsKey(ow.word))
				{
					word.mayType = true;
					word.tmList.add(new TypeMapping(TypeFragment.typeShortName2IdList.get(ow.word).get(0), ow.word, 100.0));
				}
				word.mayEnt = true;
				word.emList = new ArrayList<EntityMapping>();
				double score = 100;
				for(String ent: ow.entList)
				{
					if(EntityFragmentFields.entityName2Id.containsKey(ent))
					{
						//TODO: consider more suitable entity score
						int eid = EntityFragmentFields.entityName2Id.get(ent);
//						String fstr = EntityFragmentFields.entityFragmentString.get(eid);
//						System.out.println(eid+"\t"+fstr);
						word.emList.add(new EntityMapping(eid, ent, score));
						score -= 10;
					}
				}
			}
			else if(ow.type == 2)
				word.mayLiteral = true;
			// TODO: consider TYPE
		}
		
		return words;
	}
	
	public static void main(String[] args) throws IOException {
		
		EntityFragmentFields.load();
		
		List<String> inputList = FileUtil.readFile("data/test/mini-ccks.txt");
		
		for(String input: inputList) 
		{
			if (input.length()<2 || input.charAt(0)!='q') continue;
			System.out.println("----------------------------------------");
			System.out.println(input);
			EntityRecognitionCh.parseSentAndRecogEnt(input);
		}

	}

}

