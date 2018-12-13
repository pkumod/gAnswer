package nlp.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import nlp.ds.DependencyTree;
import nlp.ds.Sentence;
import qa.Globals;

public class Main {
	public static void main (String[] args) {
		Globals.init();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			while (true) {
				System.out.println("Test maltparser.");
				System.out.print("Please input the NL question: ");
				String question = br.readLine();
				if (question.length() <= 3)
					break;
				try {
					long t1 = System.currentTimeMillis();
					Sentence s = new Sentence(question);
					DependencyTree dt = new DependencyTree(s, Globals.stanfordParser);
					System.out.println("====StanfordDependencies====");
					System.out.println(dt);
					DependencyTree dt2 = new DependencyTree(s, Globals.maltParser);
					System.out.println("====MaltDependencies====");
					System.out.println(dt2);
					long t2 = System.currentTimeMillis();
					System.out.println("time=" + (t2-t1) + "ms");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();	
		}
	}

}
