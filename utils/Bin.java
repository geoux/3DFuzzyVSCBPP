/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author g-ux
 */
public class Bin implements Serializable{
    
    private float capacity;
    private float cost;

    public Bin(float capacity, float cost) {
        this.capacity = capacity;
        this.cost = cost;
    }

    float getCapacity() {
        return capacity;
    }

    float getRatio(){
        return this.cost / this.capacity;
    }

    float getCost() {
        return cost;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Float.floatToIntBits(this.capacity);
        hash = 83 * hash + Float.floatToIntBits(this.cost);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) return false;
        final Bin other = (Bin) obj;
        if (Float.floatToIntBits(this.capacity) != Float.floatToIntBits(other.capacity)) return false;
        return Float.floatToIntBits(this.cost) == Float.floatToIntBits(other.cost);
    }
    
}
