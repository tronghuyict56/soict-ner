import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import com.sun.org.apache.xerces.internal.impl.xs.identity.Selector;
import com.sun.org.apache.xpath.internal.SourceTree;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tronghuy2807 on 8/26/2016.
 */
public class FormatFile {
    public static void main(String[] args) {
        String fileRead = "data/combine.txt";
        String fileOut = "data/trainNer";
        String fileTest = "data/testNer";
        String line = null;
        try {
            File folder = new File("data/NER2016-training_data-1");
            File[] listFolder = folder.listFiles();
            FileWriter writer = new FileWriter(fileOut, true);
            BufferedWriter bw = new BufferedWriter(writer);

            FileWriter writer1 = new FileWriter(fileTest, true);
            BufferedWriter bw1 = new BufferedWriter(writer1);

            List<String> content = new ArrayList<>();
            StringBuffer tempStr = new StringBuffer();
//            for (File file : listFolder) {
            FileReader fr = new FileReader(fileRead);
            BufferedReader br = new BufferedReader(fr);


            while ((line = br.readLine()) != null) {
//                System.out.println(line);
                if (line.contains("title") || line.contains("editor") || line.contains("DOCSTART")) continue;
                else {
//                        if (line.contains("<s>")) {
//                            bw.write("");
//                            bw.newLine();
//                        } else if (line.contains("</s>")) {
//                            bw.write("");
//                        } else {
//                            bw.write(line);
//                            bw.newLine();
//                        }
                    try {
                        if (!line.isEmpty()) {
                            String[] textArr = line.split("\t");
                            if (!textArr[3].isEmpty()) {
                                String text = textArr[0] + "\t" + textArr[3];
                                tempStr.append(text);
                                tempStr.append("\n");
                            }
                        } else {
                            tempStr.append(line);
                            tempStr.append("\n");
                        }
                    }catch(Exception e){
                        System.out.println(e+"\t"+line);
                    }
                }
            }
//            }
            String text = tempStr.toString();
            String[] arr = text.split("\\n\\n");
            for (String a : arr) {
                content.add(a);
            }
            int count = content.size() / 3;
            Collections.shuffle(content);

            List<String> test = new ArrayList<>();

            for (int i = 0; i <= count; i++) {
                test.add(content.get(i).toString());
            }
            for(String t:test){
                bw1.write(t);
                bw1.newLine();
                bw1.newLine();

            }
            content.removeAll(test);
            for(String train:content){
                bw.write(train);
                bw.newLine();
                bw.newLine();
            }


            System.out.println(content.get(0));


////            System.out.println(tempStr);
//            Pattern pattern = Pattern.compile("<s>(.|\\n)*?</s>");
//            Matcher matcher = pattern.matcher(tempStr.toString());
//            matcher.find();
//            System.out.println(matcher.group(0));

            bw.close();
            bw1.close();
        } catch (Exception e) {

        }

    }

}
