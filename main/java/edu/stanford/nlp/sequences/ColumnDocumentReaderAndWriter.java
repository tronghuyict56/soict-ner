package edu.stanford.nlp.sequences;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import edu.stanford.nlp.PrintBO;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.objectbank.DelimitRegExIterator;
import edu.stanford.nlp.objectbank.IteratorFromReaderFactory;
import java.util.function.Function;
import edu.stanford.nlp.util.StringUtils;


/**
 * DocumentReader for column format.
 *
 * @author Jenny Finkel
 */
public class ColumnDocumentReaderAndWriter implements DocumentReaderAndWriter<CoreLabel> {

  private static final long serialVersionUID = 3806263423697973704L;

//  private SeqClassifierFlags flags; // = null;
  //map can be something like "word=0,tag=1,answer=2"
  private String[] map; // = null;
  private IteratorFromReaderFactory<List<CoreLabel>> factory;

//  public void init(SeqClassifierFlags flags) {
//    this.flags = flags;
//    this.map = StringUtils.mapStringToArray(flags.map);
//    factory = DelimitRegExIterator.getFactory("\n(\\s*\n)+", new ColumnDocParser());
//  }

  @Override
  public void init(SeqClassifierFlags flags) {
    this.map = StringUtils.mapStringToArray(flags.map);
    factory = DelimitRegExIterator.getFactory("\n(?:\\s*\n)+", new ColumnDocParser());
  }


  public void init(String map) {
//    this.flags = null;
    this.map = StringUtils.mapStringToArray(map);
    factory = DelimitRegExIterator.getFactory("\n(?:\\s*\n)+", new ColumnDocParser());
  }

  @Override
  public Iterator<List<CoreLabel>> getIterator(Reader r) {
    return factory.getIterator(r);
  }

  private int num; // = 0;


  private class ColumnDocParser implements Serializable, Function<String,List<CoreLabel>> {

    private static final long serialVersionUID = -6266332661459630572L;
    private final Pattern whitePattern = Pattern.compile("\\s+"); // should this really only do a tab?

    int lineCount = 0;

    @Override
    public List<CoreLabel> apply(String doc) {
      if (num > 0 && num % 1000 == 0) { System.err.print("["+num+"]"); }
      num++;

      List<CoreLabel> words = new ArrayList<CoreLabel>();

      String[] lines = doc.split("\n");

      for (String line : lines) {
        ++lineCount;
        if (line.trim().length() == 0) {
          continue;
        }
        String[] info = whitePattern.split(line);
        // todo: We could speed things up here by having one time only having converted map into an array of CoreLabel keys (Class<? extends CoreAnnotation<?>>) and then instantiating them. Need new constructor.
        CoreLabel wi;
        try {
          wi = new CoreLabel(map, info);
          // Since the map normally only specified answer, we copy it to GoldAnswer unless they've put something else there!
          if ( ! wi.containsKey(CoreAnnotations.GoldAnswerAnnotation.class) && wi.containsKey(CoreAnnotations.AnswerAnnotation.class)) {
            wi.set(CoreAnnotations.GoldAnswerAnnotation.class, wi.get(CoreAnnotations.AnswerAnnotation.class));
          }
        } catch (RuntimeException e) {
          System.err.println("Error on line " + lineCount + ": " + line);
          throw e;
        }
        words.add(wi);
      }
      return words;
    }

  } // end class ColumnDocParser


  @Override
  public void printAnswers(List<CoreLabel> doc, PrintWriter out) {

//    try {
//      PrintWriter fileOut = null;
//      fileOut = new PrintWriter(new FileWriter("data/testResultWrong.txt", true), true);
    List<PrintBO> printList = new ArrayList<>() ;
    boolean flag = false;

    for (CoreLabel wi : doc) {
      String answer = wi.get(CoreAnnotations.AnswerAnnotation.class);
      String goldAnswer = wi.get(CoreAnnotations.GoldAnswerAnnotation.class);
      if(!answer.equals(goldAnswer)){
        flag=true;
      }
//      out.println(wi.word() + "\t" + goldAnswer + "\t" + answer);
      PrintBO pb = new PrintBO(wi.word(),goldAnswer,answer);
      printList.add(pb);
//      if(!goldAnswer.equals(answer)) {
//        fileOut.write(wi.word() + "\t" + goldAnswer + "\t" + answer);
//        fileOut.write("\n");
//      }

    }
    for (PrintBO p:printList){
      if(flag==true){
        System.out.println(p.getWi()+"\t"+p.getGoldAnswer()+"\t"+p.getAnswer());
      }
    }
//    fileOut.write("\n");
//    fileOut.close();
    if(flag==true) {
      out.println();
    }
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
  }
  public void writeFileTest(String wi,String goldAnswer, String answer) throws IOException {
    PrintWriter out = new PrintWriter(new FileWriter("data/testResult.txt", true), true);
    out.write(wi + "\t" + goldAnswer + "\t" + answer);
    out.write("\n");
    out.close();
  }

}
