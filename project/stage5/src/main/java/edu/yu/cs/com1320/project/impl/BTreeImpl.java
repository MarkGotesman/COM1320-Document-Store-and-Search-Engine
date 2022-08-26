package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.BTree;

import edu.yu.cs.com1320.project.stage5.PersistenceManager;
import edu.yu.cs.com1320.project.stage5.impl.DocumentPersistenceManager;

import java.io.IOException;

//Credit to Prof. Judah Diament for the majority of this code - COM1320, Spring 2022. See "[Slides] Data_Structures_Slides (Merged)" for details.
public class BTreeImpl <Key extends Comparable<Key>, Value> implements BTree<Key, Value> {
//---------------------------Debug------------------//
    final static boolean DEBUG = false;
    private static void print (Object str) {if (DEBUG) System.out.println(" * " + new Throwable().getStackTrace()[1] + ": " + str);}

//------------------Nested Class(es)-------------------//
    private static class Node {
        private int entryCount; //number of entries
        private Entry[] entries = new Entry[MAX];//child links 

        Node () {
            entryCount = 0;
        }    
        Node (int i) {
            entryCount = i;
        }    
    }

    public static class Entry {
        private Comparable key;
        private Object val;
        private Node child;

        Entry () {
            this.key = null;
            this.val = null;
            this.child = null;            
        }

        Entry (Comparable key, Object val, Node child) {
            this.key = key;
            this.val = val;
            this.child = child;
        }
    }

//---------------------Variables--------------//
    private static final int MAX = 4;
    private Node root; 
    private int height; 
    private int n;
    private PersistenceManager persistencManager; 
    private static final Character DISK_INDICATOR = '@';  

//------------------Constructor(s)-------------------//  	

    public BTreeImpl () {
        this.root = new Node();
        root.entries[0] = new Entry('*', null, null); 
    }

//------------------Getter(s)-------------------//	
    @Override
    public Value get(Key k) {
        print ("Public get");
        if (k == null) {
            return null;
        }

        Entry returnedEntry = this.get(this.root, k, this.height);

        //If there is no match, or if the value had already been deleted and so the value is null - return null
        if (returnedEntry == null || returnedEntry.val == null)  {
            return null;
        }

        //If the entry that was gotten is on disk, indicated by containing the value of the special char '@',  
        //call persistencManager.deserialize() on the key of that entry, i.e. the URI, to move it into memory. Then call delete to delete it.
        try {
            if (returnedEntry.val == DISK_INDICATOR) {
                returnedEntry.val = this.persistencManager.deserialize((Key)returnedEntry.key);
                print ("Object of key: " + k + " was on disk. got and deleted file");
                this.persistencManager.delete((Key)returnedEntry.key);

            }
        } catch (IOException e) {
            throw new IllegalStateException ("IOException was thrown on an internal call to persistencManager.deserialize() with the key: " + k + ". Exception: " + e);
        }
        return (Value)returnedEntry.val;
    }

    private Entry get(Node currentNode, Key key, int height)  {
        Entry[] entries = currentNode.entries;
        //current node is external (i.e. height == 0)
        if (height == 0) {
            for (int j = 0; j < currentNode.entryCount; j++){
                if (isEqual(key, entries[j].key)) {
                    //found desired key. Return it the entry
                    return entries[j];
                }
            }
            //didn't find the key
            return null;
        }

        //current node is internal (height > 0)
        else {
            for (int j = 0; j < currentNode.entryCount; j++) {
            //if (we are at the last key in this node OR the key we 
            //are looking for is less than the next key, i.e. the
            //desired key must be in the subtree below the current entry, a.k.a.
            //entries[j]), then recurse into the current entry’s child/subtree
            if (j + 1 == currentNode.entryCount || less(key, entries[j + 1].key)) {
                return this.get(entries[j].child, key, height - 1);
                }
            }
        }
        return null;
    }


//------------------Setter(s)-------------------//    
    @Override
    public Value put(Key k, Value v) {
        print ("Putting new node");
        //if the key already exists in the b-tree, then check if it is on disk or not.
        Entry alreadyThere = this.get(this.root, k, this.height);
            if (alreadyThere != null) {
                Value previousVal;
                
                //If it is in disk, first deserialize to get prev value, then call persistencManager.delete to delete it from disk.
                if (alreadyThere.val == DISK_INDICATOR) {
                    print ("Key of entry found on disk. Deleting old value to make room for new value in memory");
                    try {
                        previousVal = (Value)this.persistencManager.deserialize((Key)alreadyThere.key);
                        this.persistencManager.delete((Key)alreadyThere.key);
                        print ("Deleted value off disk");
                    } catch (IOException e) {
                        throw new IllegalStateException ("IOException was thrown on an internal call to persistencManager.delete() with the key: " + k + ". Exception: " + e);
                    }
                }

                //If it is in memory, simply get the old value
                else {
                    previousVal = (Value)alreadyThere.val;
                }

                //For both cases: set the current val to the value passed in, return the previous val.
                alreadyThere.val = v;
                print ("Set new value to: " + v);
                return previousVal;    
            }

        print ("alreadyThere not found");

        Node newNode = this.put (this.root, k, v, this.height);
        this.n++;
        
        if (newNode == null) { //no split of root, we’re done
            return null;
        }

        //private put method only returns non-null if root.entryCount == MAX
        //(see if-else on previous slide.) Private code will have copied the upper M/2 
        //entries over. We now set the old root to be new root's first entry, and
        //set the node returned from private method to be new root's second entry
        Node newRoot = new Node(2);
        newRoot.entries[0] = new Entry(this.root.entries[0].key, null, this.root);
        newRoot.entries[1] = new Entry(newNode.entries[0].key, null, newNode);
        this.root = newRoot;

        //a split at the root always increases the tree height by 1
        this.height++;
        return null;
    }


    private Node put(Node currentNode, Key key, Value val, int height) {
        print ("In private put method");        
        //Both leaf and internal nodes
        int j;
        Entry newEntry = new Entry(key, val, null);

        //Leaf nodes
        if (height == 0) {
            print ("Checking leaf node");
            for (j = 0; j < currentNode.entryCount; j++) {
                if (less(key, currentNode.entries[j].key)) {
                    break;
                }
            }
        } 

        //Internal nodes
        else {
            for (j = 0; j < currentNode.entryCount; j++) {
                if ((j + 1 == currentNode.entryCount) || less(key, currentNode.entries[j + 1].key)) {
                    Node newNode = this.put(currentNode.entries[j++].child, key, val, height - 1);
                    if (newNode == null) {
                        return null;
                    }

                    newEntry.key = newNode.entries[0].key;
                    newEntry.val = null;
                    newEntry.child = newNode;
                    break;
                }
            }
        }

        //Both leaf and internal nodes
        for (int i = currentNode.entryCount; i > j; i--) {
            currentNode.entries[i] = currentNode.entries[i - 1];
        }
        print ("Entering new entry");
        currentNode.entries[j] = newEntry;
        currentNode.entryCount++;
        if (currentNode.entryCount < MAX) {
            return null;
        }
        else {
            return this.split(currentNode);
        }
    }    
   
   @Override 
    public void moveToDisk(Key k) throws Exception {
        if (persistencManager == null) {
            throw new IllegalStateException("Attempted to call moveToDisk() before setPersistenceManager() was passed a valid PersistenceManager!");
        }
        
        Entry movingEntry = this.get(this.root, k, this.height);
        if (movingEntry == null) {
            throw new IllegalArgumentException("Attempted to move an entry to the disk of key " + k + ", but no matching entry was found!");
        }

        if (movingEntry.val == DISK_INDICATOR) {
            throw new IllegalArgumentException("Attempted to move an entry to the disk of key " + k + ", but the entry is already in disk!");            
        }

        this.persistencManager.serialize((Key)movingEntry.key, (Value)movingEntry.val);
        movingEntry.val = DISK_INDICATOR;
        return;

    }

    @Override
    public void setPersistenceManager(PersistenceManager<Key,Value> pm) {
        this.persistencManager = pm;
    }

//------------------Private-------------------//
    private Node split(Node currentNode){
        Node newNode = new Node(MAX / 2);
        
        //copy top half of currentNode, which is being moved into newNode
            for (int j = 0; j < MAX / 2; j++){
            newNode.entries[j] = currentNode.entries[MAX / 2 + j];
            
            //set references in top half of currentNode to null to avoid memory leaks
            currentNode.entries[MAX / 2 + j] = null;
            }

        //divide currentNode.entryCount by 2
        currentNode.entryCount = MAX / 2;
        return newNode;
    }

    private static boolean isEqual(Comparable k1, Comparable k2) {
        return k1.compareTo(k2) == 0;
    }

    private boolean less(Comparable k1, Comparable k2){
        return k1.compareTo(k2) < 0;
    }


    private Entry getMatchingEntry(Node node, Key key) {
        for (Entry entry : node.entries) {
            if (isEqual (key, entry.key)) {
                return entry;
            }
        }
        return null;
    }    
}