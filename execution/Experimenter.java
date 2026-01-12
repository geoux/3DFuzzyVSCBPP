package execution;

import evolutionary_algorithms.complement.CrossoverType;
import evolutionary_algorithms.complement.MutationType;
import evolutionary_algorithms.complement.ReplaceType;
import evolutionary_algorithms.complement.SelectionType;
import local_search.complement.StopExecute;
import local_search.complement.TabuSolutions;
import local_search.complement.UpdateParameter;
import metaheurictics.strategy.Strategy;
import metaheuristics.generators.*;
import problem.definition.ObjetiveFunction;
import problem.definition.Problem;
import problem.definition.State;
import problem.extension.TypeSolutionMethod;
import utils.PrinterTools;
import utils.ProblemInstance;
import utils.Tools;

import javax.tools.Tool;
import java.io.BufferedWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Experimenter {
    private int countMaxIterations;
    private int execNumber;
    Problem problem;
    private ProblemInstance problemInstance;
    private List<State> aproximateRealParetoFront;
    private List<State> bestFullMembership;

    public Experimenter(ProblemInstance problemInstance) {
        this.problemInstance = problemInstance;
        aproximateRealParetoFront = new ArrayList<>();
        bestFullMembership = new ArrayList<>();
        configProblem();
    }

    private void configElementsOfStrategy(Problem problem) {
        Strategy.getStrategy().setStopexecute(new StopExecute());
        Strategy.getStrategy().setUpdateparameter(new UpdateParameter());
        Strategy.getStrategy().setProblem(problem);
        Strategy.getStrategy().saveListBestStates = true;
        Strategy.getStrategy().calculateTime = true;
        Strategy.getStrategy().saveListStates = true;
    }

    private Problem configProblem()
    {
        execNumber = 10;
        countMaxIterations = 60000;
        Validator validator = new Validator();
        validator.setProblemInstance(problemInstance);
        CustomOperator operator = new CustomOperator();
        operator.setProblemInstance(problemInstance);
        FO_Cost objectiveCost = new FO_Cost();
        objectiveCost.setProblemInstance(problemInstance);
        objectiveCost.setTypeProblem(Problem.ProblemType.Minimizar);
        FO_Membership objectiveMembership = new FO_Membership();
        objectiveMembership.setProblemInstance(problemInstance);
        objectiveMembership.setTypeProblem(Problem.ProblemType.Maximizar);
        FO_Packing objectivePacking = new FO_Packing();
        objectivePacking.setProblemInstance(problemInstance);
        objectivePacking.setTypeProblem(Problem.ProblemType.Maximizar);
        FO_Priority objectivePriority = new FO_Priority();
        objectivePriority.setProblemInstance(problemInstance);
        objectivePriority.setTypeProblem(Problem.ProblemType.Maximizar);
        ArrayList<ObjetiveFunction> listObjFunt = new ArrayList<>();
        listObjFunt.add(objectiveCost);
        listObjFunt.add(objectiveMembership);
        listObjFunt.add(objectivePacking);
        listObjFunt.add(objectivePriority);

        this.problem = new Problem();
        this.problem.setCodification(validator);
        this.problem.setFunction(listObjFunt);
        this.problem.setOperator(operator);
        //problem.setPossibleValue(2);
        this.problem.setTypeProblem(Problem.ProblemType.Maximizar);
        this.problem.setTypeSolutionMethod(TypeSolutionMethod.MultiObjetivoPuro);
        this.problem.setState(new State());
        Strategy.getStrategy().validate = true;
        Strategy.getStrategy().saveListStates = true;
        return problem;
    }

    public void LocalSearch() throws IllegalArgumentException, SecurityException, ClassNotFoundException,
            InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        AlgParams params = new AlgParams(999999999);
        params.setName(problemInstance.getName());
        for (int i = 0; i < execNumber; i++)
        {
            configElementsOfStrategy(this.problem);
            System.out.println("********************************");
            System.out.println("LOCAL SEARCH");
            System.out.println("Run number " + i);
            Strategy.getStrategy().executeStrategy(countMaxIterations, 1, GeneratorType.MultiobjectiveStochasticHillClimbing);

            //Extract non dominated on current iteration
            ArrayList<State> pf = new ArrayList<>(extractNonDominated(Strategy.getStrategy().listStates,problemInstance));
            aproximateRealParetoFront.addAll(pf);
            State currentBestFullMembership = getFullMbsSolution(Strategy.getStrategy().listStates);
            bestFullMembership.add(currentBestFullMembership);
            PrinterTools.iterationResult("LS", params, pf, Strategy.getStrategy(), i, problemInstance);
        }
        params.setAverage(params.getAverage() / execNumber);
        params.setAverageTime(params.getAverageTime() / execNumber);
        //Extract non dominated from all previous iterations
        ArrayList<State> realPF = new ArrayList<>(extractNonDominated(aproximateRealParetoFront,problemInstance));
        State globalBestFullMembership = getFullMbsSolution(bestFullMembership);
        if(!checkIfExists(globalBestFullMembership,realPF))
            realPF.add(globalBestFullMembership);
        PrinterTools.saveStates(realPF,"LS_PF_Final_"+problemInstance.getName(), problemInstance, params);
        PrinterTools.printSymmary("LocalSearch",params,problemInstance.getOptimal(),realPF, problemInstance);
        Strategy.destroyExecute();
        aproximateRealParetoFront.clear();
        bestFullMembership.clear();
    }

    public void LocalSearchHigherDistance() throws IllegalArgumentException, SecurityException, ClassNotFoundException,
            InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        configElementsOfStrategy(configProblem());
        AlgParams params = new AlgParams(999999999);
        params.setName(problemInstance.getName());
        for (int i = 0; i < execNumber; i++)
        {
            configElementsOfStrategy(this.problem);
            System.out.println("********************************");
            System.out.println("TABU SEARCH");
            System.out.println("Run number " + i);
            TabuSolutions.maxelements = 100;
            Strategy.getStrategy().executeStrategy(countMaxIterations, 1, GeneratorType.MultiobjectiveTabuSearch);

            ArrayList<State> pf = new ArrayList<>(extractNonDominated(Strategy.getStrategy().listStates,problemInstance));
            aproximateRealParetoFront.addAll(pf);
            PrinterTools.iterationResult("MOTS", params, pf, Strategy.getStrategy(), i, problemInstance);
            //PrinterTools.saveStates(Strategy.getStrategy().listStates,"LS_"+i+"_", problemInstance);
            //PrinterTools.saveStates(pf,"LSD_PF_"+i+"_"+problemInstance.getName(), problemInstance);
        }
        params.setAverage(params.getAverage() / execNumber);
        params.setAverageTime(params.getAverageTime() / execNumber);
        ArrayList<State> realPF = new ArrayList<>(extractNonDominated(aproximateRealParetoFront,problemInstance));
        PrinterTools.saveStates(realPF,"MOTS_PF_Final_"+problemInstance.getName(), problemInstance, params);
        PrinterTools.printSymmary("TabuSearch",params,problemInstance.getOptimal(),realPF, problemInstance);
        Strategy.destroyExecute();
        aproximateRealParetoFront.clear();
    }

    public void RSMOU_Alg() throws IllegalArgumentException, SecurityException, ClassNotFoundException,
            InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        AlgParams params = new AlgParams(999999999);
        params.setName(problemInstance.getName());
        for (int i = 0; i < execNumber; i++)
        {
            configElementsOfStrategy(this.problem);
            System.out.println("********************************");
            System.out.println("UMOSA");
            System.out.println("Run number " + i);
            UMOSA.countIterationsT = 500;
            UMOSA.alpha = 0.9;
            UMOSA.tfinal = 0.0;
            UMOSA.tinitial = 500.0;
            Strategy.getStrategy().executeStrategy(countMaxIterations, 1, GeneratorType.UMOSA);
            ArrayList<State> pf = new ArrayList<>(extractNonDominated(Strategy.getStrategy().listStates,problemInstance));
            aproximateRealParetoFront.addAll(pf);
            PrinterTools.iterationResult("UMOSA", params, pf, Strategy.getStrategy(), i, problemInstance);
            //PrinterTools.saveStates(Strategy.getStrategy().listStates,"LS_"+i+"_", problemInstance);
            //PrinterTools.saveStates(pf,"UMOSA_PF_"+i+"_"+problemInstance.getName(), problemInstance);
        }
        params.setAverage(params.getAverage() / execNumber);
        params.setAverageTime(params.getAverageTime() / execNumber);
        ArrayList<State> realPF = new ArrayList<>(extractNonDominated(aproximateRealParetoFront,problemInstance));
        PrinterTools.saveStates(realPF,"UMOSA_PF_Final_"+problemInstance.getName(), problemInstance, params);
        PrinterTools.printSymmary("UMOSA",params,problemInstance.getOptimal(),realPF, problemInstance);
        Strategy.destroyExecute();
        aproximateRealParetoFront.clear();
    }

    public void NSGAII_Alg() throws IllegalArgumentException, SecurityException, ClassNotFoundException,
            InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        AlgParams params = new AlgParams(999999999);
        params.setName(problemInstance.getName());
        double timeInit = (double)System.currentTimeMillis();
        for (int i = 0; i < execNumber; i++)
        {
            configElementsOfStrategy(this.problem);
            System.out.println("********************************");
            System.out.println("NSGAII");
            System.out.println("Run number " + i);
            NSGAII.countRef = 100; //cantidad de individuos
            NSGAII.selectionType = SelectionType.TournamentSelection;
            NSGAII.crossoverType = CrossoverType.GenericCrossover;
            NSGAII.mutationType = MutationType.GenericMutation;
            NSGAII.PM = 0.8;
            NSGAII.PC = 0.5;
            Strategy.getStrategy().executeStrategy(countMaxIterations, 1, GeneratorType.RandomSearch);
            ArrayList<State> pf = new ArrayList<>(extractNonDominated(Strategy.getStrategy().listStates,problemInstance));
            aproximateRealParetoFront.addAll(pf);
            PrinterTools.iterationResult("NSGAII", params, pf, Strategy.getStrategy(), i, problemInstance);
            //PrinterTools.saveStates(Strategy.getStrategy().listStates,"LS_"+i+"_", problemInstance);
            //PrinterTools.saveStates(pf,"NSGAII_PF_"+i+"_"+problemInstance.getName(), problemInstance);
        }
        double timeFinal = (double)System.currentTimeMillis() - timeInit;
        params.setAverage(params.getAverage() / execNumber);
        params.setAverageTime((float) (timeFinal / execNumber));
        ArrayList<State> realPF = new ArrayList<>(extractNonDominated(aproximateRealParetoFront,problemInstance));
        PrinterTools.saveStates(realPF,"NSGAII_PF_Final_"+problemInstance.getName(), problemInstance, params);
        PrinterTools.printSymmary("NSGAII",params,problemInstance.getOptimal(),realPF, problemInstance);
        Strategy.destroyExecute();
        aproximateRealParetoFront.clear();
    }

    public void MOGA_Alg() throws IllegalArgumentException, SecurityException, ClassNotFoundException,
            InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        AlgParams params = new AlgParams(999999999);
        params.setName(problemInstance.getName());
        double timeInit = (double)System.currentTimeMillis();
        for (int i = 0; i < execNumber; i++)
        {
            configElementsOfStrategy(this.problem);
            System.out.println("********************************");
            System.out.println("Genetic Algorithm");
            System.out.println("Run number " + i);
            MOGA.countRef = 100;
            MOGA.selectionType = SelectionType.TournamentSelection;
            MOGA.crossoverType = CrossoverType.GenericCrossover;
            MOGA.mutationType = MutationType.GenericMutation;
            MOGA.replaceType = ReplaceType.GenerationalReplace;
            MOGA.PM = 0.8;
            MOGA.PC = 0.5;
            Strategy.getStrategy().executeStrategy(countMaxIterations, 1, GeneratorType.RandomSearch);
            ArrayList<State> pf = new ArrayList<>(extractNonDominated(Strategy.getStrategy().listStates,problemInstance));
            aproximateRealParetoFront.addAll(pf);
            PrinterTools.iterationResult("MOGA", params, pf, Strategy.getStrategy(), i, problemInstance);
            //PrinterTools.saveStates(Strategy.getStrategy().listStates,"LS_"+i+"_", problemInstance);
            //PrinterTools.saveStates(pf,"MOGA_PF_"+i+"_"+problemInstance.getName(), problemInstance);
        }
        double timeFinal = (double)System.currentTimeMillis() - timeInit;
        params.setAverage(params.getAverage() / execNumber);
        params.setAverageTime((float) (timeFinal / execNumber));
        ArrayList<State> realPF = new ArrayList<>(extractNonDominated(aproximateRealParetoFront,problemInstance));
        PrinterTools.saveStates(realPF,"MOGA_PF_Final_"+problemInstance.getName(), problemInstance, params);
        PrinterTools.printSymmary("MOGA",params,problemInstance.getOptimal(),realPF, problemInstance);
        Strategy.destroyExecute();
        aproximateRealParetoFront.clear();
    }

    /**
     * Devuelve la mejor solucion (menor costo, maxima prioridad) con pertenencia 1 en restricciones
     * Retorna: vector solucion
     */
    private State getFullMbsSolution(List<State> states){
        State result = new State();
        for (State candidate : states) {
            if(result.getCode().isEmpty()){
                if(candidate.getEvaluation().get(1).floatValue() == 1 && candidate.getEvaluation().get(2).floatValue() == 1){
                    result = candidate;
                }
            }else{
                if(candidate.getEvaluation().get(1).floatValue() == 1 && candidate.getEvaluation().get(2).floatValue() == 1){
                    if(result.getEvaluation().get(0).floatValue() > candidate.getEvaluation().get(0).floatValue() &&
                            result.getEvaluation().get(3).floatValue() < candidate.getEvaluation().get(3).floatValue()){
                        result = candidate;
                    }
                }
            }
        }
        return result;
    }

    private List<State> extractNonDominated(List<State> states, ProblemInstance pi) {
        if (states == null || states.isEmpty()) return new ArrayList<>();

        // Configuración: Obj0 min (false), Obj1-3 max (true)
        boolean[] isMaximize = {false, true, true, true};
        List<State> paretoFront = new ArrayList<>();

        for (State candidate : states) {
            if (isInvalid(candidate)) continue;

            boolean skipCandidate = false;
            List<State> toRemove = new ArrayList<>();

            for (State existing : paretoFront) {
                int res = compareDominance(existing, candidate, isMaximize);

                if (res == 1) {
                    // El existente domina al candidato -> Descartar candidato
                    skipCandidate = true;
                    break;
                } else if (res == 0) {
                    // ¡AQUÍ SE EVITAN DUPLICADOS!
                    // Son iguales hasta el 4º decimal -> Descartar candidato
                    skipCandidate = true;
                    break;
                } else if (res == -1) {
                    // El candidato domina al existente -> Marcar para borrar existente
                    toRemove.add(existing);
                }
            }

            if (!skipCandidate) {
                paretoFront.removeAll(toRemove);
                paretoFront.add(candidate);
            }
        }
        return paretoFront;
    }

    /**
     * Compara dos estados usando redondeo para evitar errores de Double.
     * Retorna:
     * 1 si s1 domina a s2
     * -1 si s2 domina a s1
     * 0 si son iguales
     * 2 si son incomparables (ninguno domina al otro)
     */
    private int compareDominance(State s1, State s2, boolean[] isMaximize) {
        List<Double> e1 = s1.getEvaluation();
        List<Double> e2 = s2.getEvaluation();

        boolean s1Better = false;
        boolean s2Better = false;

        // Factor de escala para 4 decimales
        double scale = 10.0;

        for (int i = 0; i < e1.size(); i++) {
            // Escalamos y redondeamos para mantener 4 decimales de precisión
            long v1 = Math.round(e1.get(i) * scale);
            long v2 = Math.round(e2.get(i) * scale);

            if (isMaximize[i]) {
                if (v1 > v2) s1Better = true;
                else if (v2 > v1) s2Better = true;
            } else { // Minimizar
                if (v1 < v2) s1Better = true;
                else if (v2 < v1) s2Better = true;
            }
        }

        if (s1Better && !s2Better) return 1;  // s1 domina
        if (s2Better && !s1Better) return -1; // s2 domina
        if (!s1Better && !s2Better) return 0; // Iguales (hasta el 4º decimal)
        return 2; // Incomparables
    }

    private boolean isInvalid(State s) {
        return s.getEvaluation().stream().anyMatch(v -> Math.round(v) == -1);
    }


    /*
    private List<State> extractNonDominated(List<State> states, ProblemInstance problemInstance){
        states.sort(Comparator.comparing((State s) -> s.getEvaluation().get(0))
                .thenComparing((State s) -> s.getEvaluation().get(1))
                .thenComparing((State s) -> s.getEvaluation().get(2)));

        List<State> result = new ArrayList<>();
        List<State> resultToRemove = new ArrayList<>();
        result.add(states.get(0));
        //Recorro todas las soluciones
        for(int i = 1; i < states.size(); i++) {
            int countEquals = 0;
            int countBetter = 0;
            int c = 0;
            boolean check = false;
            while (c < states.get(i).getEvaluation().size() && !check){
                if(states.get(i).getEvaluation().get(c).floatValue() == -1)
                    check = true;
                c++;
            }
            if(!check){
                boolean dominated = false;
                //Recorro la lista donde se almacenara la solucion actual si no es dominada
                for (State state : result) {
                    for (int k = 0; k < states.get(i).getEvaluation().size() && !dominated; k++) {
                        //Solucion de referencia
                        float ref_eval = states.get(i).getEvaluation().get(k).floatValue();
                        //Solucion con la que voy a comparar
                        float cmp_eval = state.getEvaluation().get(k).floatValue();
                        //Si el estado de referencia es mejor
                        if (ref_eval > cmp_eval) {
                            countBetter++;
                        } else if (ref_eval == cmp_eval) {
                            countEquals++;
                        }
                    }
                    //Con que haya una mejor y ninguna peor es no dominada
                    //Borro la dominada y guardo esta
                    //Si hay una peor ya es dominada
                    if (countBetter > 0 && countEquals < state.getEvaluation().size()) {
                        if (countEquals == state.getEvaluation().size() - countBetter ||
                                countBetter == state.getEvaluation().size() - countEquals ||
                                countBetter == state.getEvaluation().size()) {
                            resultToRemove.add(state);
                        }
                        //result.add(states.get(i));
                    } else {
                        dominated = true;
                    }
                }
                if(!dominated){
                    result.add(states.get(i));
                    if(!resultToRemove.isEmpty()){
                        result.removeAll(resultToRemove);
                    }
                }
                resultToRemove.clear();
            }
        }

        return result;
    }

    private List<State> extractNonDominated(List<State> states, ProblemInstance problemInstance)
    {
        ArrayList<State> toRemove = new ArrayList<>();
        ArrayList<State> result = new ArrayList<>();
        for (State state : states) {
            if(state.getEvaluation().get(1) == -1 || state.getEvaluation().get(2) == -1 )
                toRemove.add(state);
            state.getEvaluation().set(0, (double) Tools.reEvaluateCost(state, problemInstance));
            state.getEvaluation().set(1, 1-state.getEvaluation().get(1));
            state.getEvaluation().set(2, 1-state.getEvaluation().get(2));
        }
        states.removeAll(toRemove);
        states.sort(Comparator.comparing((State s) -> s.getEvaluation().get(0))
                .thenComparing((State s) -> s.getEvaluation().get(1))
                .thenComparing((State s) -> s.getEvaluation().get(2)));
        for (State state : states) {
            state.getEvaluation().set(1, 1-state.getEvaluation().get(1));
            state.getEvaluation().set(2, 1-state.getEvaluation().get(2));
        }

        for(int i = 1; i < states.size(); i++){
            boolean dominated = false;
            int j = 0;
            while(!dominated && j < result.size()){
                float o1_candidate = states.get(i).getEvaluation().get(0).floatValue();
                float o1_cmp = result.get(j).getEvaluation().get(0).floatValue();

                float o2_candidate = states.get(i).getEvaluation().get(1).floatValue();
                float o2_cmp = result.get(j).getEvaluation().get(1).floatValue();

                float o3_candidate = states.get(i).getEvaluation().get(2).floatValue();
                float o3_cmp = result.get(j).getEvaluation().get(2).floatValue();

                if(o1_candidate >= o1_cmp && o2_candidate <= o2_cmp && o3_candidate <= o3_cmp){
                    dominated = true;
                }else if((o1_candidate == o1_cmp && o2_candidate > o2_cmp && o3_candidate > o3_cmp) ||
                        (o1_candidate < o1_cmp && o2_candidate == o2_cmp && o3_candidate > o3_cmp) ||
                        (o1_candidate < o1_cmp && o2_candidate < o2_cmp && o3_candidate == o3_cmp)){
                    result.remove(j);
                }
                j++;
            }
            if(!dominated && !checkIfExists(states.get(i),result))
                result.add(states.get(i));
        }
        return result;
    }
     */

    private boolean checkIfExists(State s, List<State> cmp){
        boolean found = false;
        int index = 0;
        while(!found && index < cmp.size()){
            if(cmp.get(index).getEvaluation().get(0).floatValue() == s.getEvaluation().get(0).floatValue() &&
                    cmp.get(index).getEvaluation().get(1).floatValue() == s.getEvaluation().get(1).floatValue() &&
                    cmp.get(index).getEvaluation().get(2).floatValue() == s.getEvaluation().get(2).floatValue() &&
                    cmp.get(index).getEvaluation().get(3).floatValue() == s.getEvaluation().get(3).floatValue())
            {
                found = true;
            }
            index++;
        }
        return found;
    }
}
