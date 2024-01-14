package execution;

import problem.definition.State;

import java.util.ArrayList;

public class AlgorithmResult {
    private String name;
    private int runNumber;
    private ArrayList<State> paretoFront;

    public AlgorithmResult(String name, int runNumber, ArrayList<State> paretoFront) {
        this.name = name;
        this.runNumber = runNumber;
        this.paretoFront = new ArrayList<>(paretoFront);
    }

    public String getName() {
        return name;
    }

    public int getRunNumber() {
        return runNumber;
    }

    public ArrayList<State> getParetoFront() {
        return paretoFront;
    }
}
