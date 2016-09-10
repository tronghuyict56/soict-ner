import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;

import java.util.*;

/**
 * Created by HuyDT on 11/12/2015.
 */
public class DemoNER {
    AbstractSequenceClassifier<CoreLabel> classifier;
    public static void main(String[] args) throws Exception {

        DemoNER test = new DemoNER();
        test.run();
    }
    public void run() throws Exception {

        String serializedClassifier ="data/model.testCampaign.gz" ;
        classifier = CRFClassifier.getClassifier(serializedClassifier);
        String[] example = {"Nguyễn Kỳ Sơn chỉ là một trong hàng chục vạn người lính đã nằm xuống cho độc lập tự do của Tổ Quốc"};
        for(String str : example) {
            System.out.println(classifier.classifyToString(str));
        }

//        for (String str : example) {
//            HashMap<String, HashMap<String, Integer>> ent = extractEntities(str);
//            Set set = ent.entrySet();
//            Iterator iterator = set.iterator();
//            while(iterator.hasNext()) {
//                Map.Entry me = (Map.Entry) iterator.next();
//                System.out.println("Key: " + me.getKey() + ", value: " + me.getValue());
//            }
//        }
//
//        for(String str : example) {
//            List<Triple<String, Integer, Integer>> result = classifier.classifyToCharacterOffsets(str);
//            for (Triple<String, Integer, Integer> triple : result)
//            {
//                System.out.println(triple.first + " : " + str.substring(triple.second, triple.third));
//            }
//        }

//        for(String str : example) {
//            for(List<CoreLabel> lcl : classifier.classify(str)) {
//
//                Iterator<CoreLabel> iterator = lcl.iterator();
//                while(iterator.hasNext()) {
//                    CoreLabel cl = iterator.next();
//                    System.out.println("test: " + cl.getString(CoreAnnotations.PartOfSpeechAnnotation.class));
//                }
//            }
//        }
    }
    public HashMap<String, HashMap<String, Integer>> extractEntities(String text){

        HashMap<String, HashMap<String, Integer>> entities =
                new HashMap<String, HashMap<String, Integer>>();

        for (List<CoreLabel> lcl : classifier.classify(text)) {

            Iterator<CoreLabel> iterator = lcl.iterator();

            if (!iterator.hasNext())
                continue;

            CoreLabel cl = iterator.next();

            while (iterator.hasNext()) {
                String answer =
                        cl.getString(CoreAnnotations.AnswerAnnotation.class);

                if (answer.equals("O")) {
                    cl = iterator.next();
                    continue;
                }

                if (!entities.containsKey(answer))
                    entities.put(answer, new HashMap<String, Integer>());

                String value = cl.getString(CoreAnnotations.ValueAnnotation.class);

                while (iterator.hasNext()) {
                    cl = iterator.next();
                    if (answer.equals(
                            cl.getString(CoreAnnotations.AnswerAnnotation.class)))
                        value = value + " " +
                                cl.getString(CoreAnnotations.ValueAnnotation.class);
                    else {
                        if (!entities.get(answer).containsKey(value))
                            entities.get(answer).put(value, 0);

                        entities.get(answer).put(value,
                                entities.get(answer).get(value) + 1);

                        break;
                    }
                }

                if (!iterator.hasNext())
                    break;
            }
        }

        return entities;
    }
    public TreeMap<String, String> tokenize(String title, String modelPath) throws Exception {
//        String modelPath = ModelFactory.getModel(catId);
//        if (modelPath == null) {
//            return null;
//        }

        TreeMap<String, String> data = new TreeMap<>();
        List<String> startLabels = Arrays.asList("B-PN", "B-BR", "B-STYLE", "B-OB", "B-FUNC", "B-PROP", "B-FUNC-OB", "B-PN-OB");
        AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(modelPath);
        List<List<CoreLabel>> classify = classifier.classify(title);
        List<CoreLabel> coreLabels = classify.get(0);

        String key = "";
        int index = 0;
        int indexChild = 0;

        for (CoreLabel coreLabel : coreLabels) {
            String word = coreLabel.word();
            String answer = coreLabel.get(CoreAnnotations.AnswerAnnotation.class);
            int pos = 0;
            if (answer.contains("-")) {
                pos = 2;
            }
            String label = answer.substring(pos);
            String labelParent = "";
            if (label.contains("-")) {
                labelParent = answer.substring(pos, pos + label.lastIndexOf("-"));
            }
            if (startLabels.contains(answer)) {
                if (labelParent == "") {
                    key = index + "_" + label;
                    index++;
                } else {
                    key = (index - 1) + "_" + indexChild + "_" + label;
                    indexChild++;
                }

                data.put(key, word);
            } else if (!answer.equals("O")) {
                if (data.containsKey(key)) {
                    data.put(key, data.get(key) + "_" + word);
                }
            }
        }

        return data;
    }
}
