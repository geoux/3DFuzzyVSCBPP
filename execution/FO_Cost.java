package execution;

import metaheurictics.strategy.Strategy;
import problem.definition.ObjetiveFunction;
import problem.definition.State;
import utils.ProblemInstance;

import java.util.ArrayList;

public class FO_Cost  extends ObjetiveFunction {

    private ProblemInstance problemInstance;

    void setProblemInstance(ProblemInstance problemInstance) {
        this.problemInstance = problemInstance;
    }

    @Override
    public Double Evaluation(State state) {

        if(!Strategy.getStrategy().getProblem().getCodification().validState(state))
            return Math.exp(999999999);
        else{
            double result = 0;
            for(int i = 0; i < problemInstance.getCapacities().size(); i++){
                if(problemInstance.getCapacities().get(i) != state.getPacking()[i]){
                    result += problemInstance.getCosts().get(i);
                }
            }
            //Normalizar entre cero y uno el resultado de la FO
            result = 1 - (result / problemInstance.getMaxCost());
            return result;
        }
    }
}
