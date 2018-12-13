package nlp.tool;

import java.util.List;

import qa.Globals;

import nlp.ds.Sentence;
import nlp.ds.Word;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PositionAnnotation;
import edu.stanford.nlp.ling.CoreLabel;

public class NERecognizer {
	
	static String serializedClassifier;
	static AbstractSequenceClassifier<CoreLabel> classifier;
	//public static String localPath="E:\\Hanshuo\\gAnswer\\";
		
	public NERecognizer() {
		serializedClassifier = Globals.localPath+"lib/stanford-ner-2012-11-11/classifiers/english.all.3class.distsim.crf.ser.gz";
		classifier  = CRFClassifier.getClassifierNoExceptions(serializedClassifier);
	}
	
	/*public NERecognizer(String basePath, boolean flag) {
		serializedClassifier = "WEB-INF\\lib\\stanford-ner-2012-11-11\\stanford-ner-2012-11-11\\classifiers\\english.all.3class.distsim.crf.ser.gz";
	}*/
	
	public void recognize(Sentence sentence) {
		List<CoreLabel> lcl = classifier.classify(sentence.plainText).get(0);
		for (CoreLabel cl : lcl) {
			int position = Integer.parseInt(cl.get(PositionAnnotation.class))+1;
			Word w = sentence.getWordByIndex(position);
			String ner = cl.get(AnswerAnnotation.class);
			if (ner.equals("O")) w.ner = null;
			else w.ner = ner;
		}
	}
			
	public static void main(String[] args) {
		System.out.println("Test NER");
		Globals.init();
		
		Sentence s = new Sentence("I go to school at Stanford University, which is located in California.");//"Which states of Germany are governed by the Social Democratic Party?"
		Globals.nerRecognizer.recognize(s);
		for (Word word : s.words) {
			System.out.print(word + "   ");
			System.out.println("ner=" + word.ner);
		}
	}
}
