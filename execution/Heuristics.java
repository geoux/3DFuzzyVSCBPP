package execution;


import org.apache.commons.lang3.tuple.ImmutablePair;
import problem.definition.State;
import utils.ProblemInstance;

import java.util.*;
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
        ArrayList<ImmutablePair<Integer, Integer>> itemsToRepack = new ArrayList<>();
        ArrayList<ImmutablePair<Integer, Integer>> unpacked = new ArrayList<>();
        double alpha = Math.random();
        if(state.getPacking() == null)
            packingState(state, problemInstance);
        for (int i = 0; i < state.getCode().size(); i++) {
            if ((int) state.getCode().get(i) == key) {
                ImmutablePair<Integer, Integer> element = new ImmutablePair<>(i, problemInstance.getItems().get(i));
                itemsToRepack.add(element);
                state.getCode().set(i, -1);
            }
        }
        state.getPacking()[key] = problemInstance.getCapacities().get(key);
        itemsToRepack.sort(Comparator.comparing(ImmutablePair<Integer, Integer>::getRight).reversed());
        itemsToRepack.forEach(item ->{
            boolean packed = false;
            int index = 0;
            while(index < state.getPacking().length && !packed){
                float tolerance = problemInstance.getTolerancePercent()*problemInstance.getCapacities().get(index);
                float modCap = (float) (state.getPacking()[index] + alpha*tolerance);
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

    /*Protección de Prioridad: He modificado la búsqueda del ítem en el minIndex.
    Ahora el algoritmo intenta elegir un ítem que no sea de prioridad máxima para moverlo.
    Esto evita que los ítems críticos estén "saltando" de bin en bin innecesariamente.
    Cálculo de Espacio Libre: Tu código original usaba state.getPacking()[i] como si fuera la carga acumulada, pero en los métodos anteriores lo tratábamos como espacio restante.
    He unificado la lógica: packing[i] es el espacio libre. Si es negativo, hay sobrecarga.Eficiencia de tipos:
    Cambiado 999999 por Double.MAX_VALUE.Uso de float y double de forma coherente con tus estructuras (float[] para el packing).
    Casting de (int) para los Object del code.Lógica de Pertenencia ($\alpha$):
    He refinado el cálculo de alpha para que represente qué tan "cómodo" queda el ítem en el nuevo bin comparado con el desastre que era el bin anterior (minValue).
     */
    static void capacityMembershipOperator(State state, ProblemInstance problemInstance) {
        // 1. Obtener pertenencias y encontrar el bin con el valor mínimo (peor estado)
        List<Double> ms = getCapacityMembership(state, problemInstance);
        double minValue = Double.MAX_VALUE;
        int minIndex = -1;

        for (int i = 0; i < ms.size(); i++) {
            if (ms.get(i) < minValue) {
                minValue = ms.get(i);
                minIndex = i;
            }
        }

        if (minIndex == -1) return;

        // 2. Identificar el ítem más ligero en el bin crítico
        // Añadimos lógica de prioridad: No queremos sacar ítems de prioridad máxima si podemos evitarlo
        List<Object> code = state.getCode();
        List<Integer> items = problemInstance.getItems();
        List<Integer> priorities = problemInstance.getItemsPriorities();
        int maxPriority = priorities.stream().mapToInt(Integer::intValue).max().orElse(0);

        double minWeight = Double.MAX_VALUE;
        int position = -1;

        for (int i = 0; i < code.size(); i++) {
            if ((int) code.get(i) == minIndex) {
                // Priorizamos sacar ítems que NO sean de prioridad máxima
                // O si todos son de prioridad máxima, el más ligero
                int currentPriority = priorities.get(i);
                double currentWeight = items.get(i).doubleValue();

                if (position == -1 || (currentPriority < maxPriority && currentWeight < minWeight)) {
                    minWeight = currentWeight;
                    position = i;
                }
            }
        }

        if (position == -1) return;

        // 3. Buscar un nuevo bin (found) para mover el ítem
        int found = -1;
        float[] packing = state.getPacking();
        List<Integer> capacities = problemInstance.getCapacities();
        float tolerancePercent = (float) problemInstance.getTolerancePercent();
        float itemWeight = items.get(position).floatValue();

        for (int i = 0; i < packing.length; i++) {
            if (i == minIndex) continue;

            float capacity = capacities.get(i).floatValue();
            float tolerance = tolerancePercent * capacity;
            float currentFreeSpace = packing[i]; // Espacio libre actual

            // ¿Cabe con la tolerancia?
            if (currentFreeSpace + tolerance >= itemWeight) {
                float spaceAfterMove = currentFreeSpace - itemWeight;

                // Si cabe sin exceder la capacidad nominal, es una buena opción
                if (spaceAfterMove >= 0) {
                    found = i;
                    break; // Encontrado bin ideal
                } else {
                    // Si requiere usar tolerancia, evaluamos si la nueva pertenencia es mejor que la original
                    double alpha = (tolerance - Math.abs(spaceAfterMove)) / tolerance;
                    if (alpha > minValue) {
                        found = i;
                    }
                }
            }
        }

        // 4. Ejecutar el movimiento si se encontró un sitio mejor
        if (found > -1) {
            // Devolver el peso al bin antiguo
            packing[minIndex] += itemWeight;
            // Restar el peso al nuevo bin
            packing[found] -= itemWeight;
            // Actualizar el código
            code.set(position, found);
        }
    }
    /*
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
     */

    public static ArrayList<Double> getCapacityMembership(State state, ProblemInstance problemInstance)
    {
        if(state.getPacking() == null)
            packingState(state,problemInstance);
        ArrayList<Double> membership = new ArrayList<>();
        int i = 0;
        while (i < state.getPacking().length) {
            float maxOverload = problemInstance.getTolerancePercent()*problemInstance.getCapacities().get(i);
            if (state.getPacking()[i] < 0) {
                float realOverload = Math.abs(0 - state.getPacking()[i]);
                if (realOverload > maxOverload) {
                    membership.add(0d);
                } else {
                    float difference = maxOverload - realOverload;
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
        if (state.getPacking() == null)
            packingState(state, problemInstance);

        // Ajuste de tipos según tu estructura
        List<Object> code = state.getCode();
        float[] packing = state.getPacking();
        List<Integer> itemWeights = problemInstance.getItems();
        List<Integer> capacities = problemInstance.getCapacities();
        List<Integer> priorities = problemInstance.getItemsPriorities();

        // --- 1. FASE DE EMPAQUETAMIENTO INICIAL ---
        for (int i = 0; i < code.size(); i++) {
            if ((int)code.get(i) == -1) {
                tryPackItem(i, code, packing, itemWeights);
            }
        }

        // --- 2. FASE DE VACIADO PROBABILÍSTICO ---
        Set<Integer> binsToEmpty = new HashSet<>();
        problemInstance.getBinTypes().forEach(bt -> {
            double probability = Math.random();
            // Calculamos el límite del primer tercio del rango
            int limit = bt.getMiddle() + (bt.getRight() - bt.getMiddle()) / 3;

            for (int j = bt.getMiddle(); j <= limit; j++) {
                if (Math.random() > probability) {
                    // Comprobamos si el bin no está lleno comparando con su capacidad original
                    if (packing[j] < capacities.get(j).floatValue()) {
                        binsToEmpty.add(j);
                    }
                }
            }
        });

        // Vaciado en una sola pasada O(N)
        for (int k = 0; k < code.size(); k++) {
            int currentBin = (int)code.get(k);
            if (binsToEmpty.contains(currentBin)) {
                packing[currentBin] += itemWeights.get(k).floatValue();
                code.set(k, -1);
            }
        }

        // --- 3. CICLO DE CORRECCIÓN POR PRIORIDAD ---
        // Buscamos la prioridad máxima
        int maxPriority = priorities.stream().mapToInt(Integer::intValue).max().orElse(0);

        for (int i = 0; i < code.size(); i++) {
            // Si el ítem es de alta prioridad y está fuera, intentamos reinsertarlo
            if ((int)code.get(i) == -1 && priorities.get(i) == maxPriority) {
                tryPackItem(i, code, packing, itemWeights);
            }
        }
    }

    // Método auxiliar ajustado a float[] e Integer
    private static boolean tryPackItem(int itemIdx, List<Object> code, float[] packing, List<Integer> weights) {
        float weight = weights.get(itemIdx).floatValue();
        for (int j = 0; j < packing.length; j++) {
            if (packing[j] >= weight) {
                code.set(itemIdx, j);
                packing[j] -= weight;
                return true;
            }
        }
        return false;
    }
    /*
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
     */

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
            Integer totalWeight = problemInstance.getItems().stream().reduce(0, Integer::sum);
            return 1 - sum / totalWeight;
        }
    }

    public static Double getPackingPriority(State state, ProblemInstance problemInstance)
    {
        if(state.getPacking() == null)
            packingState(state,problemInstance);

        int sum = 0;
        int i = 0;
        while (i < state.getCode().size()) {
            if((int)state.getCode().get(i) == -1){
                sum += problemInstance.getItemsPriorities().get(i);
            }
            i++;
        }
        if(sum == 0){
            return 0.0;
        }else{
            return (double) sum;
        }
    }
}
