package com.jasonphanley.dashbit.model;

public class Activities {
    
    public final int steps;
    
    public final int floors;
    
    public final float distance;
    
    public final int calories;
    
    public final Units units;
    
    public Activities(int steps, int floors, float distance, int calories,
            Units units) {
        super();
        
        this.steps = steps;
        this.floors = floors;
        this.distance = distance;
        this.calories = calories;
        this.units = units;
    }
    
    @Override
    public String toString() {
        return steps + " steps, " + floors + " floors, " + distance
                + " " + (units == Units.US ? "miles" : "kilometers")
                + ", " + calories + " calories";
    }
    
}