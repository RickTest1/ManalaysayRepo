package model;

/**
 * Interface for objects that can perform calculations
 * Demonstrates abstraction in OOP
 */
public interface Calculable {
    /**
     * Perform calculation logic
     */
    void calculate();
    
    /**
     * Get the calculated amount
     */
    double getAmount();
}