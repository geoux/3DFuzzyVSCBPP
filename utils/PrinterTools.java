package utils;

import execution.AlgParams;
import execution.AlgorithmResult;
import metaheurictics.strategy.Strategy;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import problem.definition.State;

import java.io.*;
import java.util.*;

public class PrinterTools {

    private static int countUsedBins(State bestVector){
        ArrayList<Integer> usedBins = new ArrayList<>();
        for (Object o : bestVector.getCode()) {
            if(!usedBins.contains(o)){
                usedBins.add((int)o);
            }
        }
        return usedBins.size();
    }

    public static void iterationResult(String title, AlgParams params, List<State> r, Strategy strategy, int iter, ProblemInstance problemInstance){
        double eval = strategy.listBest.get(0).getEvaluation().get(0);
        double evalAlpha = 1-strategy.listBest.get(0).getEvaluation().get(1);
        double improvement = Tools.initialEval - eval;
        float current = Tools.reEvaluateCost(strategy.getBestState(),problemInstance);
        params.setAverage(params.getAverage() + current);

        if(current < params.getBest()){
            params.setBest(current);
            params.setBestTime(Strategy.timeExecute);
            params.setBestVector(strategy.getBestState());
        }

        params.setAverageTime(Strategy.timeExecute);
        System.out.println();
//        System.out.println("Minimal cost: " + current);
//        System.out.println("Maximal membership: " + evalAlpha);
        System.out.println("Non dominated: " + strategy.listRefPoblacFinal.size());
        System.out.println("Execution time: " + strategy.timeExecute + "ms");
        PrinterTools.saveResultToExcel(title,params,r, iter, problemInstance);
    }

    public static void printSymmary(String title, AlgParams params, float optimalKnown, ArrayList<State> results, ProblemInstance problemInstance) {

        System.out.println("**********************************************************");
        System.out.println("********************* SUMMARY ****************************");
        System.out.println("**********************************************************");
        System.out.println(title);
        System.out.println("Average cost: " + params.getAverage());
        System.out.println("Average time (ms): " + params.getAverageTime());
        System.out.println("Pareto Front Size: " + results.size());
        System.out.println("**********************************************************");
        saveResultToExcel(title,params,results,21, problemInstance);
    }

    public static void saveStates(List<State> states, String fileName, ProblemInstance problemInstance, AlgParams params){
        try{

            states.sort(Comparator.comparing((State s) -> s.getEvaluation().get(0))
                    .thenComparing((State s) -> s.getEvaluation().get(1))
                    .thenComparing((State s) -> s.getEvaluation().get(2))
                    .thenComparing((State s) -> s.getEvaluation().get(3)));

            ArrayList<State> bestSelected = new ArrayList<>();
            int i = 0;
            int end = 1000;
            if(states.size() < 1000){
                end = states.size() - 1;
            }
            while(i <= end){
                bestSelected.add(states.get(i));
                i++;
            }

            FileOutputStream fileout = new FileOutputStream(new File("soluciones/"+fileName+".xls"));
            Workbook ficheroWb = new HSSFWorkbook();
            Sheet sheet = ficheroWb.createSheet("Soluciones");

            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("Costo");
            row.createCell(1).setCellValue("Capacidad");
            row.createCell(2).setCellValue("Empaquetado");
            row.createCell(3).setCellValue("Prioridad");
            int rowIndex = 1;
            for(State s: bestSelected){
                row = sheet.createRow(rowIndex);
                //row.createCell(0).setCellValue(Tools.reEvaluateCost(s,problemInstance));
                row.createCell(0).setCellValue(s.getEvaluation().get(0));
                row.createCell(1).setCellValue(s.getEvaluation().get(1));
                row.createCell(2).setCellValue(s.getEvaluation().get(2));
                row.createCell(3).setCellValue(s.getEvaluation().get(3));
                //row.createCell(3).setCellValue(Tools.reConvertPriorities(s,problemInstance));
                rowIndex++;
            }

            Row init = sheet.getRow(0);
            init.createCell(4).setCellValue("FP Size");
            init.createCell(5).setCellValue("Ave. Cost");
            init.createCell(6).setCellValue("Ave. Time (ms)");
            init = sheet.getRow(1);
            init.createCell(4).setCellValue(states.size());
            init.createCell(5).setCellValue(params.getAverage());
            init.createCell(6).setCellValue(params.getAverageTime());

            ficheroWb.write(fileout);
            fileout.flush();
        }catch (IOException ex)
        {
            System.out.println(ex.getMessage());
        }
    }

    private static void saveResultToExcel(String title, AlgParams params, List<State> results, int iteration, ProblemInstance problemInstance){
        try {
            String iterName;
            if(iteration==21)
                iterName = "full";
            else
                iterName = String.valueOf(iteration);
            String filename = "results/"+title+"_"+params.getName()+"_"+iterName+".csv";
            File mipFile = new File(filename);
            BufferedWriter writer = new BufferedWriter(new FileWriter(mipFile));

            List<State> toStore = extractNonDominatedFast(results);

            for(State s: toStore){
                int count = 0;
                for(Object i: s.getCode()){
                    writer.write(i.toString());
                    if(count < s.getCode().size()-1)
                        writer.write(";");
                    count++;
                }
                writer.newLine();
                float reEvaluated = Tools.reEvaluateCost(s,problemInstance);
                writer.write("sCost: "+s.getEvaluation().get(0).toString());
                writer.write(";");
                writer.write("mCap: "+s.getEvaluation().get(1).toString());
                writer.write(";");
                writer.write("mPack: "+s.getEvaluation().get(2).toString());
                writer.write(";");
                writer.write("sPrio: "+s.getEvaluation().get(3).toString());
                writer.newLine();
                writer.write(String.valueOf(params.getAverageTime()));
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<State> extractNonDominatedFast(List<State> states) {
        if (states == null || states.isEmpty()) return new ArrayList<>();

        int n = states.size();

        // 1. Pre-calcular objetivos como double sin redondeo
        double[][] objs = new double[n][4];
        for (int i = 0; i < n; i++) {
            State s = states.get(i);
            objs[i][0] = s.getEvaluation().get(0).doubleValue();
            objs[i][1] = s.getEvaluation().get(1).doubleValue();
            objs[i][2] = s.getEvaluation().get(2).doubleValue();
            objs[i][3] = s.getEvaluation().get(3).doubleValue();
        }

        // 2. Filtrar soluciones inválidas (cualquier objetivo == -1)
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (objs[i][0] != -1.0 && objs[i][1] != -1.0 &&
                    objs[i][2] != -1.0 && objs[i][3] != -1.0) {
                validIndices.add(i);
            }
        }

        if (validIndices.isEmpty()) return new ArrayList<>();

        // 3. Eliminar duplicados exactos antes del análisis de dominancia
        Map<String, Integer> seen = new LinkedHashMap<>();
        for (int i : validIndices) {
            String key = objs[i][0] + "," + objs[i][1] + "," + objs[i][2] + "," + objs[i][3];
            seen.putIfAbsent(key, i);
        }

        List<Integer> uniqueIndices = new ArrayList<>(seen.values());
        int m = uniqueIndices.size();

        // 4. Ordenar por obj[0] ascendente (minimizar costo)
        uniqueIndices.sort((a, b) -> Double.compare(objs[a][0], objs[b][0]));

        boolean[] isDominated = new boolean[n];

        // 5. Análisis de dominancia sin break para garantizar todas las comparaciones
        for (int ii = 0; ii < m; ii++) {
            int i = uniqueIndices.get(ii);
            if (isDominated[i]) continue;

            for (int jj = ii + 1; jj < m; jj++) {
                int j = uniqueIndices.get(jj);
                if (isDominated[j]) continue;

                if (objs[j][0] > objs[i][0]) {
                    // j tiene mayor costo → solo i puede dominar a j
                    if (dominates(objs[i], objs[j])) {
                        isDominated[j] = true;
                    }
                } else {
                    // objs[i][0] == objs[j][0] → comparación bidireccional
                    if (dominates(objs[i], objs[j])) {
                        isDominated[j] = true;
                    } else if (dominates(objs[j], objs[i])) {
                        isDominated[i] = true;
                        // Sin break: i ya no puede dominar a nadie más
                        // pero otros j posteriores podrían ser dominados por i's futuros
                    }
                }
            }
        }

        // 6. Recoger no dominados
        List<State> nonDominated = new ArrayList<>();
        for (int i : uniqueIndices) {
            if (!isDominated[i]) {
                nonDominated.add(states.get(i));
            }
        }

        return nonDominated;
    }

    /**
     * A domina a B si:
     *   - obj[0] minimizar → a[0] <= b[0]
     *   - obj[1..3] maximizar → a[k] >= b[k]
     *   - Al menos una condición estrictamente mejor
     */
    private static boolean dominates(double[] a, double[] b) {
        return a[0] <= b[0]
                && a[1] >= b[1]
                && a[2] >= b[2]
                && a[3] >= b[3]
                && (a[0] < b[0] || a[1] > b[1] || a[2] > b[2] || a[3] > b[3]);
    }
}
