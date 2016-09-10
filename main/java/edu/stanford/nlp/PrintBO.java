package edu.stanford.nlp;

/**
 * Created by tronghuy2807 on 9/8/16.
 */
public class PrintBO {
    public String wi;
    public String goldAnswer;
    public String answer;

    public PrintBO(String wi, String goldAnswer, String answer) {
        this.wi = wi;
        this.goldAnswer = goldAnswer;
        this.answer = answer;
    }

    public String getWi() {
        return wi;
    }

    public void setWi(String wi) {
        this.wi = wi;
    }

    public String getGoldAnswer() {
        return goldAnswer;
    }

    public void setGoldAnswer(String goldAnswer) {
        this.goldAnswer = goldAnswer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
