package com.aol.cyclops2;

import org.junit.Test;

import cyclops.collections.immutable.PStackX;
import cyclops.collections.immutable.PVectorX;

public class NQueens {
    
    private final int num=8;
    @Test
    public void run(){
        show(placeQueens(num));
    }
    
    public PStackX<PStackX<Integer>> placeQueens(int k){
        if (k == 0) 
            return PStackX.of(PStackX.empty());
        else{
            return placeQueens(k - 1).forEach2(
                         queens -> PStackX.range(1, num),
                         (queens, column) -> isSafe(column, queens,1),
                         (queens, column) -> queens.plus(column));
                  
        }
      
    }
   
    public Boolean isSafe(int column, PStackX<Integer> queens, int delta){
       return  queens.visit((c, rest)-> c != column &&
                                           Math.abs(c - column) != delta &&
                                           isSafe(column, rest.toPStackX(), delta + 1) ,
                            ()->true);
    }
           

    
    public void show(PStackX<PStackX<Integer>> solutions){
        solutions.forEach(solution->{
            System.out.println("----Solution----");
            solution.forEach(col->{
                System.out.println(PVectorX.range(0,solution.size())
                                           .map(i->"*")
                                           .with(col-1, "X")
                                           .join(" "));
            });
        });
    }
  
 
}
