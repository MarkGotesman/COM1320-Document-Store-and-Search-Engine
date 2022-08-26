package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.HashTable;

public class HashTableImpl <Key, Value> implements HashTable <Key, Value>{

//------------------Debug-------------------//	
	final static boolean DEBUG = false;
	private static void print (Object str) {if (DEBUG) System.out.println(new Throwable().getStackTrace()[1] + ": " + str);}
	
	public static void main (String[] args) {
		HashTable<Integer, Integer> hashTable = new HashTableImpl<>();
		hashTable.put(1,11);
		hashTable.put(2,22);
		hashTable.put(3,33);
		hashTable.put(4,44);
		hashTable.put(5,55);
		hashTable.put(6,66);
		print(hashTable.put(null,55));
		print (hashTable.put(6, null));
	}

//------------------Nested Class(es)-------------------//
	class KVP <Key, Value> { //Key + Value + Pointer entry object
		Key key;
		Value value;
		Object pointer;
		KVP (Key k, Value v) {
			this(k, v, null);
		}
		KVP (Key k, Value v, Object p) {
			key = k;
			value = v;
			pointer = p;
		}
	}

//------------------Instance Variable(s)-------------------//
	private KVP[] tableArray;
	private int length;

//------------------Constructor(s)-------------------//
	public HashTableImpl () {
		length = 5;
		tableArray = new KVP[length];
	}

//------------------Getter(s)-------------------//
	@Override
	public Value get (Key k) {
		if (k == null) { // null keys return null
			return null;
		}
		if (this.tableSearch(k) != null) {
			print ("Key " + k + " returns value " + this.tableSearch(k)[1].value);	
		}	
		return (this.tableSearch(k) == null ? null : (Value)this.tableSearch(k)[1].value);
	}

//------------------Setter(s)-------------------//
	@Override
	public Value put (Key k, Value v) {
		if (k == null) { // null keys return null
			return null;
		}		
		int index = this.hashFunction (k);
		KVP[] kvpDouble = this.tableSearch(k);
		Value oldValue = kvpDouble == null ? null : (Value)kvpDouble[1].value;
		
		if (kvpDouble == null) {
			print ("tableArray state before insertion: " + this.tableArray[index]);
			this.tableArray[index] = new KVP (k, v, (this.tableArray[index] == null ? null : this.tableArray[index]));
			print ("New KVP inserted with key of " + this.tableArray[index].key + ", value of " + this.tableArray[index].value + " and pointer to " + this.tableArray[index].pointer);
		} else if (v == null) {
			deleteKVP (k);
			print ("Key "+ k + " was deleted!");
		} else {
			kvpDouble[1].value = v;
			print ("KVP changed from " + oldValue + " to " + kvpDouble[1].value);
		}
		return (Value)oldValue;
	}

//------------------Private-------------------//
	private void deleteKVP (Key k) {
		int index = this.hashFunction (k);		
		KVP[] kvpDouble = this.tableSearch(k);
		if (kvpDouble[0] == null) {
			this.tableArray[index] = null;
		} else {
			kvpDouble[0].pointer = kvpDouble[1].pointer;
		}
	}

	private KVP[] tableSearch (Key k){

		int index = this.hashFunction (k); 
		KVP current = this.tableArray[index];
		KVP prior = null;
		while (current != null) {
			print ("Prior KVP: " + prior + ". Current KVP: " +current + ". Current.key = " + current.key);		
			if (current.key.equals(k)){
				print ("tableSearch found a match for key " + k);
				return new KVP[] {prior, current};
			}
			prior = current;
			print ("Prior after assignment to current:" + prior);
			current = (KVP)current.pointer == null ? null : (KVP)current.pointer;		
			print ("Current after assignment to next KVP: " + current);
		}
		return null;	
	} 

	private int hashFunction (Key k) {
		return k == null ? 0 : (k.hashCode() & 0x7fffffff) % this.length; 
	}		
}