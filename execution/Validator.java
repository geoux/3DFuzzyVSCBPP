package execution;

import metaheurictics.strategy.Strategy;
import problem.definition.Codification;
import problem.definition.State;
import utils.ProblemInstance;
import utils.Tools;

import java.util.ArrayList;

public class Validator extends Codification {

    private ProblemInstance problemInstance;

    void setProblemInstance(ProblemInstance problemInstance) {
        this.problemInstance = problemInstance;
    }

    @Override
    public boolean validState(State state) {
        int j = 0;
        if(state.getPacking() == null)
            Heuristics.packingState(state, problemInstance);
        while(j < state.getPacking().length){
            float modCap = state.getPacking()[j] + (problemInstance.getTolerancePercent()*problemInstance.getCapacities().get(j));
//            float modCap = 0;
//            switch (problemInstance.getCapacities().get(j)){
//                case 50 : modCap = state.getPacking()[j] + 6;
//                    break;
//                case 100 : modCap = state.getPacking()[j] + 5;
//                    break;
//                case 150 : modCap = state.getPacking()[j] + 7;
//                    break;
//            }
            if(modCap < 0) {
                Tools.countFails++;
                return false;
            }
            j++;
        }
        return true;
    }

    @Override
    public Object getVariableAleatoryValue(int i) {
        return (int)(Math.random() * problemInstance.getCapacities().size() - 1);
    }

    @Override
    public int getAleatoryKey() {
        return (int)(Math.random() * problemInstance.getItems().size() - 1);
    }

    @Override
    public int getVariableCount() {
        return problemInstance.getItems().size() * problemInstance.getCapacities().size();
    }
}
