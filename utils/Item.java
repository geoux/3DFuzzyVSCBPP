/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.Serializable;

/**
 *
 * @author g-ux
 */
public class Item implements Serializable{
    
    private float weight;

    public Item(float weight) {
        this.weight = weight;
    }
    
    public float getWeight() {
        return weight;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Float.floatToIntBits(this.weight);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Item other = (Item) obj;
        if (Float.floatToIntBits(this.weight) != Float.floatToIntBits(other.weight)) {
            return false;
        }
        return true;
    }
    
    
}
