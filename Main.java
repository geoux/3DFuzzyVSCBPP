import execution.Experimenter;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Stream;

import utils.ProblemInstance;
import utils.Tools;

public class Main{

    private static String getFileNameFromAddress(String address){
        int pos = address.lastIndexOf('/');
        int end = address.lastIndexOf('.');
        return address.substring(pos + 1,end);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ArrayList<String> instanceAddress = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get("instances/Grupo1"))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(path -> instanceAddress.add(path.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
        System.out.println();
        System.out.println("INSTANCES AVAILABLE");
        int index = 1;
        for (String name: instanceAddress) {
            System.out.println(index+"- "+name);
            index++;
        }

        System.out.println("Select the Instance (1-"+(index - 1)+"):");
        Scanner tmp = new Scanner(System.in);
        int instance = tmp.nextInt();
        */

        int index = 0;
        for(String addr : instanceAddress){
            try {
                ProblemInstance problemInstance = Tools.ReadInstance(addr);
                problemInstance.setTolerancePercent(0.2f);
                problemInstance.sortAllLists();
                Experimenter exe = new Experimenter(problemInstance);
                //exe.LocalSearch();
                //exe.RSMOU_Alg();
                exe.NSGAII_Alg();
                exe.MOGA_Alg();
            } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}