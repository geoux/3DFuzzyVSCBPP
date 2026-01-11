package utils;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.Comparator;

public class ProblemInstance {

    private String name;
    private ArrayList<Integer> capacities;
    private ArrayList<Integer> costs;
    private ArrayList<Integer> items;
    private ArrayList<Integer> itemsPriorities;
    private float Optimal;
    private float tolerancePercent;
    private ArrayList<Triple<Integer,Integer,Integer>> binTypes;

    ProblemInstance() {
        capacities = new ArrayList<>();
        costs = new ArrayList<>();
        items = new ArrayList<>();
        itemsPriorities = new ArrayList<>();
        binTypes = new ArrayList<>();
    }

    public ArrayList<Integer> getCapacities() {
        return capacities;
    }

    public ArrayList<Integer> getCosts() {
        return costs;
    }

    public ArrayList<Integer> getItems() {
        return items;
    }

    public ArrayList<Integer> getItemsPriorities() {
        return itemsPriorities;
    }
    public float getOptimal() {
        return Optimal;
    }

    void setOptimal(float optimal) {
        Optimal = optimal;
    }

    public float getTolerancePercent() {
        return tolerancePercent;
    }

    public void setTolerancePercent(float tolerancePercent) {
        this.tolerancePercent = tolerancePercent;
    }

    public void sortAllLists(){
        SortAlgorithms.quickSort(capacities,costs,0, capacities.size()-1);
        items.sort(Comparator.comparing(Integer::floatValue).reversed());
        int cap = capacities.get(0);
        int start = 0;
        for(int i = 1; i < capacities.size(); i++){
            if(capacities.get(i) != cap){
                Triple<Integer,Integer,Integer> p = new ImmutableTriple<>(cap,start,i-1);
                binTypes.add(p);
                cap = capacities.get(i);
                start = i;
            }
        }
        Triple<Integer,Integer,Integer> p = new ImmutableTriple<>(cap,start,capacities.size()-1);
        binTypes.add(p);
    }

    public ArrayList<Triple<Integer, Integer, Integer>> getBinTypes() {
        return binTypes;
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public int getMaxCost(){
        int[] total = {0};
        costs.forEach(x -> total[0] += x);
        return total[0];
    }
}
