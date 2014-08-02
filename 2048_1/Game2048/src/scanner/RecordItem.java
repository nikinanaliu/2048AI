/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package scanner;

/**
 *
 * @author Dongle
 */
public class RecordItem {
    private String status;
    private int nextOpt;
    private int score;
    private int step;

    public RecordItem(String status, int nextOpt, int score, int step) {
        this.status = status;
        this.nextOpt = nextOpt;
        this.score = score;
        this.step = step;
    }
    
    public RecordItem(){}

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getNextOpt() {
        return nextOpt;
    }

    public void setNextOpt(int nextOpt) {
        this.nextOpt = nextOpt;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    @Override
    public String toString() {
        return String.format("%s,%d,%d,%d", status, nextOpt, score, step);
    }   
}
