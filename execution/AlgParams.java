package execution;

import problem.definition.State;

public class AlgParams {
    private String name;
    private double best;
    private double average;
    private int bestCounter;
    private State bestVector;
    private float bestTime;
    private float averageTime;

    AlgParams(double best) {
        this.best = best;
        this.average = 0;
        this.bestCounter = 0;
        this.bestVector = new State();
        this.bestTime = 0;
        this.averageTime = 0;
    }

    public double getBest() {
        return best;
    }

    public void setBest(double best) {
        this.best = best;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public int getBestCounter() {
        return bestCounter;
    }

    public void setBestCounter(int bestCounter) {
        this.bestCounter = bestCounter;
    }

    public State getBestVector() {
        return bestVector;
    }

    public void setBestVector(State bestVector) {
        this.bestVector = bestVector;
    }

    public float getBestTime() {
        return bestTime;
    }

    public void setBestTime(float bestTime) {
        this.bestTime = bestTime;
    }

    public float getAverageTime() {
        return averageTime;
    }

    public void setAverageTime(float averageTime) {
        this.averageTime = averageTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
