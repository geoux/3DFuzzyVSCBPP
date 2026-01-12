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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
        System.out.println("Average time: " + params.getAverageTime());
        System.out.println("Pareto Front Size: " + results.size());
        System.out.println("**********************************************************");
        saveResultToExcel(title,params,results,21, problemInstance);
    }

    public static void saveStates(List<State> states, String fileName, ProblemInstance problemInstance, AlgParams params){
        try{

            states.sort(Comparator.comparing((State s) -> s.getEvaluation().get(0))
                    .thenComparing((State s) -> s.getEvaluation().get(1))
                    .thenComparing((State s) -> s.getEvaluation().get(2)));

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
                row.createCell(0).setCellValue(Tools.reEvaluateCost(s,problemInstance));
                row.createCell(1).setCellValue(s.getEvaluation().get(1));
                row.createCell(2).setCellValue(s.getEvaluation().get(2));
                row.createCell(3).setCellValue(Tools.reConvertPriorities(s,problemInstance));
                rowIndex++;
            }

            Row init = sheet.getRow(0);
            init.createCell(4).setCellValue("FP Size");
            init.createCell(5).setCellValue("Ave. Cost");
            init.createCell(6).setCellValue("Ave. Time");
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
            for(State s: results){
                int count = 0;
                for(Object i: s.getCode()){
                    writer.write(i.toString());
                    if(count < s.getCode().size()-1)
                        writer.write(";");
                    count++;
                }
                writer.newLine();
                float reEvaluated = Tools.reEvaluateCost(s,problemInstance);
                writer.write("sCost: "+String.valueOf(reEvaluated));
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
}
