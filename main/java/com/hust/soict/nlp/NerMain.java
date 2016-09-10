package com.hust.soict.nlp;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.DocumentReaderAndWriter;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.util.StringUtils;
import edu.stanford.nlp.util.Timing;

import java.util.Properties;

/**
 * Created by thuyenhx on 17/06/2016.
 */
public class NerMain {
    AbstractSequenceClassifier<CoreLabel> classifier;
    CRFClassifier<CoreLabel> crf;
    SeqClassifierFlags flags;
    Properties prop;

    public NerMain() {
        prop = StringUtils.propFileToProperties("data/austen-test.prop");
        flags = new SeqClassifierFlags(prop);
        crf = new CRFClassifier<CoreLabel>(flags);

    }

    public static void main(String[] args) throws Exception {
        NerMain trainMd = new NerMain();
        trainMd.train();
//        trainMd.testModel();
    }

    public void train() {

        String serializeTo = flags.serializeTo;
        String serializeToText = flags.serializeToText;

        if (crf.flags.trainFile != null) {
            Timing timing = new Timing();
            crf.train();
            timing.done("CRFClassifier training");
        }

        if (serializeTo != null) {
            crf.serializeClassifier(serializeTo);
        }

        if (crf.flags.serializeWeightsTo != null) {
            crf.serializeWeights(crf.flags.serializeWeightsTo);
        }

        if (crf.flags.serializeFeatureIndexTo != null) {
            crf.serializeFeatureIndex(crf.flags.serializeFeatureIndexTo);
        }

        if (serializeToText != null) {
            crf.serializeTextClassifier(serializeToText);
        }

    }
    public void testModel() throws Exception {

        String loadPath = flags.serializeTo;
        String testFile = flags.testFile;

        if (loadPath != null) {
            crf.loadClassifierNoExceptions(loadPath, prop);
        }

        if (testFile != null) {
            // todo: Change testFile to call testFiles with a singleton list
            DocumentReaderAndWriter<CoreLabel> readerAndWriter = crf.defaultReaderAndWriter();
            crf.classifyAndWriteAnswers(testFile, readerAndWriter, true);
//            crf.printProbs(testFile, readerAndWriter);
//            crf.getCliqueTrees(testFile,readerAndWriter);
        }
    }
}
