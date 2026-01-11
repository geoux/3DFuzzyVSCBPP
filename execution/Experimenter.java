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
import java.util.stream.Stream;

public class Experimenter {
    private int countMaxIterations;
    private int execNumber;
    Problem problem;
    private ProblemInstance problemInstance;
    private List<State> aproximateRealParetoFront;

    public Experimenter(ProblemInstance problemInstance) {
        this.problemInstance = problemInstance;
        aproximateRealParetoFront = new ArrayList<>();
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

            ArrayList<State> pf = new ArrayList<>(extractNonDominated(Strategy.getStrategy().listStates,problemInstance));
            aproximateRealParetoFront.addAll(pf);
            PrinterTools.iterationResult("LS", params, pf, Strategy.getStrategy(), i, problemInstance);
            //PrinterTools.saveStates(Strategy.getStrategy().listStates,"LS_"+i+"_", problemInstance);
            //PrinterTools.saveStates(pf,"LS_PF_"+i+"_"+problemInstance.getName(), problemInstance);
        }
        params.setAverage(params.getAverage() / execNumber);
        params.setAverageTime(params.getAverageTime() / execNumber);
        ArrayList<State> realPF = new ArrayList<>(extractNonDominated(aproximateRealParetoFront,problemInstance));
        PrinterTools.saveStates(realPF,"LS_PF_Final_"+problemInstance.getName(), problemInstance, params);
        PrinterTools.printSymmary("LocalSearch",params,problemInstance.getOptimal(),realPF, problemInstance);
        Strategy.destroyExecute();
        aproximateRealParetoFront.clear();
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

    private List<State> extractNonDominated(List<State> states, ProblemInstance problemInstance) {
        if (states.isEmpty()) return new ArrayList<>();
        boolean[] config = {false, true, true, true};

        // 1. Ordenamos por el objetivo que minimizamos (Obj 0).
        // Si empatan, desempatamos por los que maximizamos (descendente) para descartar antes.
        states.sort(Comparator.comparing((State s) -> s.getEvaluation().get(0).floatValue())
                .thenComparing(Comparator.comparing((State s) -> s.getEvaluation().get(1).floatValue()).reversed())
                .thenComparing(Comparator.comparing((State s) -> s.getEvaluation().get(2).floatValue()).reversed()));

        List<State> paretoFront = new ArrayList<>();

        for (State candidate : states) {
            // Ignorar estados inválidos (los que tienen -1 según tu lógica)
            if (candidate.getEvaluation().stream().anyMatch(v -> v.floatValue() == -1)) continue;

            boolean isDominated = false;

            // Comparamos el candidato contra los que ya están en el frente
            // Usamos un Iterator por si necesitáramos borrar, aunque con el Sort previo
            // normalmente solo añadimos o descartamos el nuevo.
            for (State existing : paretoFront) {
                if (checkDominance(existing, candidate, config)) { // ¿El existente domina al nuevo?
                    isDominated = true;
                    break;
                }
            }

            if (!isDominated) {
                // Eliminar de paretoFront cualquier solución que el nuevo candidato domine
                // (Aunque con el Sort por Obj 0, es menos probable que ocurra, pero asegura integridad)
                paretoFront.removeIf(existing -> checkDominance(candidate, existing, config));
                paretoFront.add(candidate);
            }
        }
        return paretoFront;
    }

    /**
     * s1 domina a s2 si:
     * 1. s1 es mejor o igual que s2 en TODOS los objetivos.
     * 2. s1 es estrictamente mejor que s2 en al menos UNO.
     * * @param isMaximize Array booleano donde true = Maximizar, false = Minimizar
     */
    private boolean checkDominance(State s1, State s2, boolean[] isMaximize) {
        List<Double> e1 = s1.getEvaluation();
        List<Double> e2 = s2.getEvaluation();

        boolean betterInAny = false;

        for (int i = 0; i < e1.size(); i++) {
            double v1 = e1.get(i);
            double v2 = e2.get(i);

            if (isMaximize[i]) {
                // Caso Maximizar: v1 debe ser >= v2
                if (v1 < v2) return false; // s2 es mejor en este punto, s1 no domina
                if (v1 > v2) betterInAny = true;
            } else {
                // Caso Minimizar: v1 debe ser <= v2
                if (v1 > v2) return false; // s2 es mejor en este punto, s1 no domina
                if (v1 < v2) betterInAny = true;
            }
        }

        return betterInAny;
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
            if(cmp.get(index).getEvaluation().get(0).floatValue() == s.getEvaluation().get(0).floatValue() && cmp.get(index).getEvaluation().get(1).floatValue() == s.getEvaluation().get(1).floatValue()){
                found = true;
            }
            index++;
        }
        return found;
    }
}
