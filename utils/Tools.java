package utils;

import execution.Heuristics;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import problem.definition.State;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Tools {

    public static double initialEval;

    public static int countFails;

    public static ProblemInstance ReadInstance(String selection)
    {
        ProblemInstance result = new ProblemInstance();
        File instanceFile = new File(selection);
        result.setName(instanceFile.getName().substring(0,instanceFile.getName().length()-4));
        try
        {
            FileInputStream ficheroXlsx = new FileInputStream(instanceFile);
            Workbook ficheroWb = new HSSFWorkbook(ficheroXlsx);
            Sheet sheet = ficheroWb.getSheetAt(0);
            int i = 1;
            //for the items
            Row row = sheet.getRow(i);
            if(row.getCell(3) != null){
                result.setOptimal((float) row.getCell(3).getNumericCellValue());
            }else{
                result.setOptimal(0);
            }

            while(row != null && row.getCell(2) != null){
                result.getItems().add((int) row.getCell(2).getNumericCellValue());
                result.getItemsPriorities().add((int) row.getCell(3).getNumericCellValue());
                i++;
                row = sheet.getRow(i);
            }
            //for the bins
            i = 1;
            row = sheet.getRow(i);
            while(row != null && row.getCell(0) != null){
                result.getCapacities().add((int) row.getCell(0).getNumericCellValue());
                result.getCosts().add((int) row.getCell(1).getNumericCellValue());
                i++;
                row = sheet.getRow(i);
            }
            ficheroXlsx.close();
        }
        catch (IOException ex)
        {
            System.out.println(ex.getMessage());
        }
        return result;
    }

    public static float reEvaluateCost(State state, ProblemInstance problemInstance){
        float result = 0;
        if(state.getPacking() == null)
            Heuristics.packingState(state, problemInstance);
        for(int i = 0; i < problemInstance.getCapacities().size(); i++){
            if(problemInstance.getCapacities().get(i) != state.getPacking()[i]){
                result += problemInstance.getCosts().get(i);
            }
        }
        return result;
    }

    public static float reConvertPriorities(State state, ProblemInstance problemInstance){
        float result = 0;
        for(int i = 0; i < state.getCode().size(); i++){
            if((int)state.getCode().get(i) != -1){
                result += problemInstance.getItemsPriorities().get(i);
            }
        }
        return result;
    }

    /**************************************************************************************************************
    * En Teoría de la Información se denomina distancia de Hamming a la efectividad de los códigos de bloque y
    * depende de la diferencia entre una palabra de código válida y otra. Cuanto mayor sea esta diferencia,
    * menor es la posibilidad de que un código válido se transforme en otro código válido por una serie de errores.
    * A esta diferencia se le llama distancia de Hamming, y se define como el número de bits que tienen que cambiarse
    * para transformar una palabra de código válida en otra palabra de código válida.
    *
    ****************************************************************************************************************/
    public static int HammingDistance(State first, State second)
    {
        int i = 0, count = 0;
        while (i < first.getCode().size())
        {
            if (first.getCode().get(i) != second.getCode().get(i))
                count++;
            i++;
        }
        return count;
    }

    /************************************************************************************************************
    * La distancia de Levenshtein, distancia de edición o distancia entre palabras es el número mínimo
    * de operaciones requeridas para transformar una cadena de caracteres en otra, se usa ampliamente en
    * teoría de la información y ciencias de la computación. Se entiende por operación,
    * bien una inserción, eliminación o la sustitución de un carácter.
    * Se le considera una generalización de la distancia de Hamming, que se usa para cadenas de la misma longitud
    * y que solo considera como operación la sustitución.
    **************************************************************************************************************/
    public static int LevenshteinDistance(State first, State second)
    {
        int [][]distance = new int[first.getCode().size()+1][second.getCode().size()+1];

        for(int i=0;i<=first.getCode().size();i++){
            distance[i][0]=i;
        }
        for(int j=0;j<=second.getCode().size();j++){
            distance[0][j]=j;
        }
        for(int i=1;i<=first.getCode().size();i++){
            for(int j=1;j<=second.getCode().size();j++){
                distance[i][j]= minimum(distance[i-1][j]+1,
                        distance[i][j-1]+1,
                        distance[i-1][j-1]+
                                ((first.getCode().get(i-1)==second.getCode().get(j-1))?0:1));
            }
        }
        return distance[first.getCode().size()][second.getCode().size()];
    }

    private static int minimum(int a, int b, int c) {
        return Math.min(a, Math.min(b, c));
    }
}
