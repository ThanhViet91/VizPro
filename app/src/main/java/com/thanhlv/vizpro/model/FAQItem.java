package com.thanhlv.vizpro.model;

public class FAQItem {
    private String question;
    private String answer;
    private Boolean isShown;

    public FAQItem(String question, String answer, Boolean isShown) {
        this.question = question;
        this.answer = answer;
        this.isShown = isShown;
    }

    public Boolean getShown() {
        return isShown;
    }

    public void setShown(Boolean shown) {
        isShown = shown;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
