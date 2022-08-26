package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.Stack;
import java.util.*;

public class StackImpl<T> implements Stack<T> {
//------------------Debug-------------------//  
    final static boolean DEBUG = false;
    private static void print (Object str) {if (DEBUG) System.out.println(new Throwable().getStackTrace()[1] + ": " + str);}

//------------------Nested Class(es)-------------------//
    class Item <T> {
        T data;
        Item<T> next;
        Item (T data) {
            this.data = data;
        }
    }

//------------------Instance Variable(s)-------------------//
    private Item<T> head;

//------------------Constructor(s)-------------------//  	
    public StackImpl () {
        head = null;
  	}

//------------------Getter(s)-------------------//
    @Override
    public T peek() {
         if (head == null) {
            return null;
        }        
        print ("Element peeked: " + head.data);
    	return head.data;
    }

    @Override
    public int size() {
        int size = 0;
        Item<T> pointer = head;
        while (pointer != null) {
            pointer = pointer.next;
            size++;    
        }
        print ("Size: " + size);
        return size;
    }

//------------------Setter(s)-------------------//
 	@Override	
    public void push(T element) {
        print ("Element pushed: " + element);
        Item <T> insert = new Item<>(element);
        insert.next = head;
        head = insert;
    }
    
 	@Override 
    public T pop() {
        if (head == null) {
            return null;
        }
    	T value = head.data;
        head = head.next;
        print ("Element popped: " + value);
        return value;
    }
//------------------Private-------------------//

    // public List<T> showStack (){
    //     List<T> stackList = new ArrayList<>();
    //     Item<T> item = head;
    //     while (item!= null && item.next != null) {
    //         stackList.add(item.data);
    //         item = item.next;
    //     }
    //     return stackList;
    // }
}