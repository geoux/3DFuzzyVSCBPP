package execution;

import metaheurictics.strategy.Strategy;
import problem.definition.ObjetiveFunction;
import problem.definition.State;
import utils.ProblemInstance;

public class FO_Priority extends ObjetiveFunction {
    private ProblemInstance problemInstance;

    void setProblemInstance(ProblemInstance problemInstance) {
        this.problemInstance = problemInstance;
    }

    @Override
    public Double Evaluation(State state) {

        if (!Strategy.getStrategy().getProblem().getCodification().validState(state))
            return -1.0;
        else {
            return Heuristics.getPackingPriority(state,problemInstance);
        }
    }
}
