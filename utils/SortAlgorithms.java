package utils;

import java.util.ArrayList;

public class SortAlgorithms {

    private static int partition(ArrayList<Integer> cap, ArrayList<Float> cost, int begin, int end) {
        int pivot = cap.get(end);
        int i = (begin-1);

        for (int j = begin; j < end; j++) {
            if (cap.get(j) >= pivot) {
                i++;

                int swapTemp = cap.get(i);
                cap.set(i,cap.get(j));
                cap.set(j,swapTemp);

                if(cost != null){
                    float swapTempCost = cost.get(i);
                    cost.set(i,cost.get(j));
                    cost.set(j,swapTempCost);
                }
            }
        }

        int swapTemp = cap.get(i+1);
        cap.set(i+1,cap.get(end));
        cap.set(end, swapTemp);

        if(cost != null){
            float swapTempCost = cost.get(i+1);
            cost.set(i+1,cost.get(end));
            cost.set(end, swapTempCost);
        }

        return i+1;
    }

    static void quickSort(ArrayList<Integer> cap, ArrayList<Float> cost, int begin, int end) {
        if (begin < end) {
            int partitionIndex = partition(cap, cost, begin, end);

            quickSort(cap, cost, begin, partitionIndex-1);
            quickSort(cap, cost,partitionIndex+1, end);
        }
    }
}
