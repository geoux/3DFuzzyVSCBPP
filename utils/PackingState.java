package utils;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

public class PackingState {
    private int id;
    private float capacity;
    private float tolerance;
    private ArrayList<Pair<Integer,Float>> items;

    public PackingState() {
        items = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getCapacity() {
        return capacity;
    }

    public void setCapacity(float capacity) {
        this.capacity = capacity;
    }

    public ArrayList<Pair<Integer, Float>> getItems() {
        return items;
    }

    public void setItems(ArrayList<Pair<Integer, Float>> items) {
        this.items = items;
    }

    public void sortItems(){
        this.items.sort(Comparator.comparing(Pair::getRight));
    }

    public float getOverload(){
        float[] cumulative = {0};
        this.items.forEach(i -> cumulative[0] += i.getRight());
        float rest = this.capacity - cumulative[0];
        return (rest < 0)?Math.abs(rest):0f;
    }

    public float getUsedCapacity(){
        float[] cumulative = {0};
        this.items.forEach(i -> cumulative[0] += i.getRight());
        return this.capacity - cumulative[0];
    }

    public boolean biggerItem(float cmp){
        boolean result = false;
        int i = 0;
        while(i < items.size() && !result){
            if(items.get(i).getRight() > cmp)
                result = true;
            i++;
        }
        return result;
    }

    public float getTolerance() {
        return tolerance;
    }

    public void setTolerance(float tolerance) {
        this.tolerance = tolerance;
    }
}
