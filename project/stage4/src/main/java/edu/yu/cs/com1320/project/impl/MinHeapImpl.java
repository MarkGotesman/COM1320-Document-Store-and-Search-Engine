package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.MinHeap;

import java.util.NoSuchElementException;
    
import java.util.*;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E>  {
//------------------Debug-------------------//  
    final static boolean DEBUG = false;
    private static void print (Object str) {if (DEBUG) System.out.println(new Throwable().getStackTrace()[1] + ": " + str);}

//------------------Instance Variable(s)-------------------//


//------------------Constructor(s)-------------------//  	
    @SuppressWarnings("unchecked")
    public MinHeapImpl() {
        this.elements = ((E[]) new Comparable[15]); //It seems that elements needs to be instantiated, but java is giving me a generic array error... 
    }

//------------------Getter(s)-------------------//
    @Override
    protected int getArrayIndex(E element) {
    	for (int i = 0; i < elements.length; i++) {
            if (elements[i] != null && elements[i].equals(element)) {
                return i;
            }
        }
        throw new NoSuchElementException();
    }

//------------------Setter(s)-------------------//
    @Override
    public void reHeapify(E element) {
        //Find the index of the element. 
        //Then decide if it neds to be moved up or down in the heap by comparing it to its children or ancestor. 
        //Call the appropriate method to move the element.
        int i = this.getArrayIndex(element);

        if (i > 1 && this.isGreater(i / 2, i)) {
            this.upHeap(i);
        } else {
            this.downHeap(i);
        }
    }

    @Override
    @SuppressWarnings("unchecked")        
    protected void doubleArraySize() {
        E[] orriginalArray = elements;
        elements = (E[]) new Comparable[2*orriginalArray.length];
        for (int i = 0; i < orriginalArray.length; i++) {
            elements[i] = orriginalArray[i];
        }
    }
//------------------Private-------------------//

}