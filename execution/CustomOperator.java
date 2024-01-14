package execution;

import metaheurictics.strategy.Strategy;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import problem.definition.Operator;
import problem.definition.State;
import utils.PackingState;
import utils.ProblemInstance;
import utils.Tools;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CustomOperator extends Operator {

    private ProblemInstance problemInstance;

    void setProblemInstance(ProblemInstance problemInstance) {
        this.problemInstance = problemInstance;
    }

    /***********************************************
    * SE IGNORA EL SEGUNDO PARAMETRO SEGUN CYNTHIA
    * Este metodo no se ejecuta en los Geneticos
    * Solo LS y SA
    *
    *************************************************/
    @Override
    public List<State> generatedNewState(State state, Integer integer) {
        boolean ok = false;
        int key;
        int value = 0;
        while(!ok){
            key = Strategy.getStrategy().getProblem().getCodification().getAleatoryKey();
            value = (int) state.getCode().get(key);
            if(value != -1)
                ok = true;
        }
        double min = 0;
        if(state.getEvaluation().isEmpty()){
            ArrayList<Double> membership = new ArrayList<>(Heuristics.getCapacityMembership(state,problemInstance));
            min = membership.stream().min(Comparator.comparing(Double::doubleValue)).get();
        }else{
            min = state.getEvaluation().get(1);
        }
        double probabilityOperator = 1 - ((Strategy.getStrategy().getCountMax()-Strategy.getStrategy().getCountCurrent())/Strategy.getStrategy().getCountMax());
        double roulette = Math.random();
        if(min < 1){
            if(roulette < probabilityOperator)
                Heuristics.mutationOperator(state,problemInstance,value);
            else {
                if(Math.random() > 0.5)
                    Heuristics.capacityMembershipOperator(state, problemInstance);
                else
                    Heuristics.packingMembershipOperator(state, problemInstance);
            }
        }else{
            if(Math.random() > 0.5)
                Heuristics.mutationOperator(state,problemInstance,value);
            else
                Heuristics.packingMembershipOperator(state, problemInstance);
        }

        List<State> listNeigborhood = new ArrayList<>();
        listNeigborhood.add(state);
        return listNeigborhood;
    }

    /*****************************************
    * SE IGNORA EL PARAMETRO SEGUN CYNTHIA
    * Este se ejecuta en todos los algoritmos
    *
    ******************************************/
    @Override
    public List<State> generateRandomState(Integer integer) {

        ArrayList<Integer> childCode =  new ArrayList<>();
        int flag = (int) (Math.random() * 2);
        if(flag == 1)
            childCode.addAll(Heuristics.generatePopulation(problemInstance,true));
        else
            childCode.addAll(Heuristics.generatePopulation(problemInstance, false));
        List<State> listNeigborhood = new ArrayList<>();
        State result = new State();
        result.setCode(new ArrayList<>(childCode));
        Heuristics.packingState(result, problemInstance);
        FO_Cost initialEval = new FO_Cost();
        initialEval.setProblemInstance(problemInstance);
        Tools.initialEval = initialEval.Evaluation(result);
        listNeigborhood.add(result);
        return listNeigborhood;
    }

    /*********************************************************
     * La estrategia de reparacion del crossover consiste en:
     * 1- Seleccionar los items que sobran y dejarlos fuera
     * 2- Reparar la solucion con sobrecarga
     ********************************************************/
    @Override
    public List<State> generateNewStateByCrossover(State state, State state1)
    {
        ArrayList<PackingState> packingStates = new ArrayList<>();
        State sonState = new State();
        sonState.initializeArray(problemInstance.getCapacities().size());
        for(int pk = 0; pk < sonState.getPacking().length; pk++){
            sonState.getPacking()[pk] = problemInstance.getCapacities().get(pk);
            PackingState ps = new PackingState();
            ps.setId(pk);
            ps.setCapacity(problemInstance.getCapacities().get(pk));
            packingStates.add(ps);
        }

        for(int i = 0; i < state.getCode().size(); i++){
            double parent = Math.random();
            int itemPosition;
            if(parent > 0.5){
                itemPosition = (int)state1.getCode().get(i);
            }else{
                itemPosition = (int)state.getCode().get(i);
            }
            sonState.getCode().add(-1);
            int index = 0;
            boolean found = false;
            Pair<Integer,Float> item = new ImmutablePair<>(i,problemInstance.getItems().get(i));
            while(index < packingStates.size() && !found){
                if(packingStates.get(index).getId() == itemPosition){
                    packingStates.get(index).getItems().add(item);
                    found = true;
                }
                index++;
            }
        }

        //Seleccionar la estrategia de reparacion
        ArrayList<Pair<Integer,Float>> candidates = new ArrayList<>();
        if(Math.random() < 0.5){
            selectCandidates(candidates,packingStates);
        }else{
            fixCrossover(candidates,packingStates);
        }

        for(int j = 0; j < sonState.getPacking().length; j++){
            sonState.getPacking()[j] = packingStates.get(j).getUsedCapacity();
            for(Pair<Integer,Float> it : packingStates.get(j).getItems()){
                sonState.getCode().set(it.getLeft(),j);
            }
        }

        List<State> newInd =new ArrayList<>();
        newInd.add(sonState);
        return newInd;
    }

    private void selectCandidates(ArrayList<Pair<Integer,Float>> candidates,ArrayList<PackingState> packingStates){
        packingStates.forEach(b -> {
            b.sortItems();
            if(b.getOverload() > 0){
                boolean enough = false;
                int position = 0;
                while(!enough){
                    Pair<Integer,Float> extracted = new ImmutablePair<>(b.getItems().get(position).getLeft(),b.getItems().get(position).getRight());
                    candidates.add(extracted);
                    b.getItems().remove(position);
                    if(b.getOverload() == 0){
                        enough = true;
                    }
                }
            }
        });
    }

    private void fixCrossover(ArrayList<Pair<Integer,Float>> candidates,ArrayList<PackingState> packingStates)
    {
        selectCandidates(candidates,packingStates);
        candidates.sort(Comparator.comparing(p -> -p.getRight()));
        ArrayList<Pair<Integer,Float>> unablesToRepack = new ArrayList<>();
        if(candidates.size() > 0){
            for(Pair<Integer,Float> c : candidates){
                boolean packed = false;
                int index = 0;
                while(index < packingStates.size() && !packed){
                    if(packingStates.get(index).getUsedCapacity() >= c.getRight()){
                        packingStates.get(index).getItems().add(c);
                        packed = true;
                    }
                    index++;
                }
                if(!packed){
                    unablesToRepack.add(c);
                }
            }
        }

        if(!unablesToRepack.isEmpty()){
            candidates.clear();
            unablesToRepack.forEach(ur -> {
                candidates.add(ur);
                float[] minRate = {999999};
                int[] indexFound = {-1};
                int[] index = {0};
                packingStates.forEach(cp -> {
                    float currentRate = ur.getRight() / cp.getCapacity();
                    if(minRate[0] > currentRate){
                        if(!cp.biggerItem(ur.getRight())){
                            minRate[0] = currentRate;
                            indexFound[0] = index[0];
                        }
                    }
                    index[0]++;
                });
                candidates.addAll(packingStates.get(indexFound[0]).getItems());
                packingStates.get(indexFound[0]).getItems().clear();
            });
            fixCrossover(candidates,packingStates);
        }
    }
}
