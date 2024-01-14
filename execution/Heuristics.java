package execution;


import org.apache.commons.lang3.tuple.ImmutablePair;
import problem.definition.State;
import utils.ProblemInstance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


public class Heuristics {

    static ArrayList<Integer> firstFit(ArrayList<Integer> bins, ArrayList<Float> items){
        ArrayList<Integer> result = new ArrayList<>();
        int s = 0;
        while (s < items.size()){
            result.add(0);
            s++;
        }
        float[] collectedBins = new float[bins.size()];
        for(int b = 0; b < bins.size(); b++)
            collectedBins[b] = bins.get(b);

        int i = 0;
        while (i < items.size()){
            int j = 0;
            boolean flag = false;
            while (j < bins.size() && !flag){
                if (collectedBins[j] >= items.get(i)){
                    collectedBins[j] = collectedBins[j] - items.get(i);
                    result.set(i, j);
                    flag = true;
                }
                j++;
            }
            i++;
        }
        return result;
    }

    public static void packingState(State state, ProblemInstance problemInstance){
        state.initializeArray(problemInstance.getCapacities().size());
        for(int i = 0; i < problemInstance.getCapacities().size(); i++){
            state.getPacking()[i] = problemInstance.getCapacities().get(i);
        }
        int j = 0;
        while(j < state.getCode().size()){
            int location = (int)state.getCode().get(j);
            if(location != -1){
                state.getPacking()[location] -= problemInstance.getItems().get(j);
            }
            j++;
        }
    }

    static ArrayList<Integer> generatePopulation(ProblemInstance problemInstance, boolean ordered) {

        ArrayList<Integer> capacities = new ArrayList<>(problemInstance.getCapacities());
        ArrayList<Integer> indexes = new ArrayList<>();
        for(int a = 0; a < capacities.size(); a++){
            indexes.add(a);
        }
        if(ordered)
            shuffleArray(capacities,indexes);
        //Do packing here
        float[] tmpCapacities = new float[problemInstance.getCapacities().size()];
        int[] bin = {0};
        capacities.forEach(c -> {
            tmpCapacities[bin[0]] = c;
            bin[0]++;
        });
        ArrayList<Integer> assigment = new ArrayList<>();
        double alpha = Math.random();
        for(int j = 0; j < problemInstance.getItems().size(); j++){
            boolean packed = false;
            int index = 0;
            while(index < tmpCapacities.length && !packed){
                float tolerance = problemInstance.getTolerancePercent()*problemInstance.getCapacities().get(index);
                float modCap = (float) (tmpCapacities[index] + alpha*tolerance);
//                float modCap = 0;
//                switch (capacities.get(index)){
//                    case 50 : modCap = (float) (tmpCapacities[index] + alpha*6);
//                        break;
//                    case 100 : modCap = (float) (tmpCapacities[index] + alpha*5);
//                        break;
//                    case 150 : modCap = (float) (tmpCapacities[index] + alpha*7);
//                        break;
//                }
                if(problemInstance.getItems().get(j) <= modCap){
                    tmpCapacities[index] -= problemInstance.getItems().get(j);
                    assigment.add(index);
                    packed = true;
                }
                index++;
            }
        }
        if(ordered)
            reArrangeAssigment(assigment, indexes);
        return assigment;
    }

    private static void shuffleArray(ArrayList<Integer> origin, ArrayList<Integer> indexes) {
        Random rnd = ThreadLocalRandom.current();
        for (int i = origin.size() - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = origin.get(index);
            origin.set(index, origin.get(i));
            origin.set(i, a);

            int x = indexes.get(index);
            indexes.set(index, indexes.get(i));
            indexes.set(i, x);
        }
    }

    private static void reArrangeAssigment(ArrayList<Integer> assigment, ArrayList<Integer> indexes) {
        for (int i = 0; i < assigment.size(); i++)
            assigment.set(i,indexes.get(assigment.get(i)));
    }

    static void mutationOperator(State state, ProblemInstance problemInstance, int key)
    {
        ArrayList<ImmutablePair<Integer, Float>> itemsToRepack = new ArrayList<>();
        ArrayList<ImmutablePair<Integer, Float>> unpacked = new ArrayList<>();
        double alpha = Math.random();
        if(state.getPacking() == null)
            packingState(state, problemInstance);
        for (int i = 0; i < state.getCode().size(); i++) {
            if ((int) state.getCode().get(i) == key) {
                ImmutablePair<Integer, Float> element = new ImmutablePair<>(i, problemInstance.getItems().get(i));
                itemsToRepack.add(element);
                state.getCode().set(i, -1);
            }
        }
        state.getPacking()[key] = problemInstance.getCapacities().get(key);
        itemsToRepack.sort(Comparator.comparing(ImmutablePair<Integer, Float>::getRight).reversed());
        itemsToRepack.forEach(item ->{
            boolean packed = false;
            int index = 0;
            while(index < state.getPacking().length && !packed){
                float tolerance = problemInstance.getTolerancePercent()*problemInstance.getCapacities().get(index);
                float modCap = (float) (state.getPacking()[index] + alpha*tolerance);
//                float modCap = 0;
//                switch (problemInstance.getCapacities().get(index)){
//                    case 50 : modCap = (float) (state.getPacking()[index] + alpha*6);
//                        break;
//                    case 100 : modCap = (float) (state.getPacking()[index] + alpha*5);
//                        break;
//                    case 150 : modCap = (float) (state.getPacking()[index] + alpha*7);
//                        break;
//                }
                if(item.getRight() <= modCap && key != index){
                    state.getPacking()[index] -= item.getRight();
                    state.getCode().set(item.getLeft(),index);
                    packed = true;
                }
                index++;
            }
            if(!packed){
                unpacked.add(item);
            }
         });

        if(!unpacked.isEmpty()){
            unpacked.forEach(u -> {
                    state.getCode().set(u.getLeft(),key);
                    state.getPacking()[key] -= u.getRight();
            });
        }
    }

    static void capacityMembershipOperator(State state, ProblemInstance problemInstance) {
        double minValue = 9999999;
        int minIndex = -1;
        ArrayList<Double> ms = new ArrayList<>(getCapacityMembership(state, problemInstance));
        for (int i = 0; i < ms.size(); i++) {
            if (ms.get(i) < minValue) {
                minValue = ms.get(i);
                minIndex = i;
            }
        }

        double minWeight = 999999;
        int position = 0;
        int cursor = 0;
        for(Object o : state.getCode()){
            if((int)o == minIndex && minWeight > problemInstance.getItems().get(cursor)){
                minWeight = problemInstance.getItems().get(cursor);
                position = cursor;
            }
            cursor++;
        }

        int found = -1;
        for(int i = 0; i < state.getPacking().length; i++){
            if(state.getPacking()[i] < problemInstance.getCapacities().get(i) && i != minIndex){
//                int tolerance = 0;
//                switch (problemInstance.getCapacities().get(i)) {
//                    case 50: tolerance = 6;
//                        break;
//                    case 100: tolerance = 5;
//                        break;
//                    case 150: tolerance = 7;
//                        break;
//                }
                float tolerance = problemInstance.getTolerancePercent()*problemInstance.getCapacities().get(i);
                float modCap = state.getPacking()[i] + tolerance;
                if(modCap >= problemInstance.getItems().get(position)){
                    float currentValue = modCap - problemInstance.getItems().get(position);
                    if(currentValue > tolerance){
                        found = i;
                    }else{
                        double alpha = (tolerance - currentValue) / tolerance;
                        if(alpha > minValue){
                            found = i;
                        }
                    }
                }
            }
        }

        if(found > -1){
            state.getPacking()[found] -= problemInstance.getItems().get(position);
            state.getCode().set(position,found);
        }

    }

    public static ArrayList<Double> getCapacityMembership(State state, ProblemInstance problemInstance)
    {
        if(state.getPacking() == null)
            packingState(state,problemInstance);
        ArrayList<Double> membership = new ArrayList<>();
        int i = 0;
        while (i < state.getPacking().length) {
//            float maxOverload = 0;
//            int currentCap = problemInstance.getCapacities().get(i);
//            switch (currentCap) {
//                case 50:
//                    maxOverload = 6;
//                    break;
//                case 100:
//                    maxOverload = 5;
//                    break;
//                case 150:
//                    maxOverload = 7;
//                    break;
//            }
            float maxOverload = problemInstance.getTolerancePercent()*problemInstance.getCapacities().get(i);
            if (state.getPacking()[i] < 0) {
                float realOverload = Math.abs(0 - state.getPacking()[i]);
                if (realOverload > maxOverload) {
                    membership.add(0d);
                } else {
                    float difference = maxOverload - realOverload;
                    //If difference is less than 1
                    //it can be considered full load
//                        if(difference < 1)
//                            difference = 1;
                    double alpha = difference / maxOverload;
                    membership.add(alpha);
                }
            } else {
                membership.add(1d);
            }
            i++;
        }
        return membership;
    }

    /*****************************************************************************
    1- Reempaco los items que estan fuera
    2- Por cada tipo de bin recorro el intervalo en las capacidades
    3- En cada bin de tipo Z genero numero aleatorio para determinar si lo vacio o no
    4- Si lo vacio, primero verifico si esta usado
    5- Si esta usado modifico en -1 cada item asignado a ese bin
    6- Restauro el peso original de ese bin en el empaquetado resultante
    ***************************************************************************/
    static void packingMembershipOperator(State state, ProblemInstance problemInstance) {
        if(state.getPacking() == null)
            packingState(state,problemInstance);

        for(int i = 0; i < state.getCode().size(); i++){
            if((int)state.getCode().get(i) == -1){
                double weight = problemInstance.getItems().get(i);
                boolean packed = false;
                int current = 0;
                while(!packed && current < state.getPacking().length){
                    if(state.getPacking()[current] >= weight){
                        state.getCode().set(i,current);
                        state.getPacking()[current] -= weight;
                        packed = true;
                    }
                    current++;
                }
            }
        }

        problemInstance.getBinTypes().forEach(bt -> {
            double probability = Math.random();
            int amount = (bt.getRight() - bt.getMiddle()) / 3;
            for(int j = bt.getMiddle(); j <= amount; j++){
                double current = Math.random();
                if(current > probability){
                    if(state.getPacking()[j] < problemInstance.getCapacities().get(j)){
                        for(int k = 0; k < state.getCode().size(); k++){
                            if((int)state.getCode().get(k) == j){
                                state.getCode().set(k,-1);
                                state.getPacking()[j] += problemInstance.getItems().get(k);
                            }
                        }
                    }
                }
            }
        });
    }

    public static Double getPackingMembership(State state, ProblemInstance problemInstance)
    {
        if(state.getPacking() == null)
            packingState(state,problemInstance);

        double sum = 0;
        int i = 0;
        while (i < state.getCode().size()) {
            if((int)state.getCode().get(i) == -1){
                sum += problemInstance.getItems().get(i);
            }
            i++;
        }
        if(sum == 0){
            return 1d;
        }else{
            Float totalWeight = problemInstance.getItems().stream().reduce(0f, Float::sum);
            return 1 - sum / totalWeight;
        }
    }
}
