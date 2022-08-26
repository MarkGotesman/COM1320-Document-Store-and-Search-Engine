package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.HashTable;

public class HashTableImpl <Key, Value> implements HashTable <Key, Value>{

//------------------Debug-------------------//	
	final static boolean DEBUG = false;
	private static void print (Object str) {if (DEBUG) System.out.println(new Throwable().getStackTrace()[1] + ": " + str);}

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
	private int elementCounter;

//------------------Constructor(s)-------------------//
	public HashTableImpl () {
		elementCounter = 0;
		tableArray = new KVP[5];
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
		print ("Inserting into HashTable. Key: " + k + ". Value: " + v);
		if (k == null) { // null keys return null
			return null;
		}		
		KVP[] kvpDouble = this.tableSearch(k);
		Value oldValue = kvpDouble == null ? null : (Value)kvpDouble[1].value;
		
		if (kvpDouble == null) {
			if (.75 * this.tableArray.length < this.elementCounter) {
				this.doubleTable();
			}
			int index = this.hashFunction (k);
			print ("tableArray state before insertion: " + this.tableArray[index]);
			this.tableArray[index] = new KVP (k, v, (this.tableArray[index] == null ? null : this.tableArray[index]));
			print ("New KVP inserted with key of " + this.tableArray[index].key + ", value of " + this.tableArray[index].value + " and pointer to " + this.tableArray[index].pointer);
			this.elementCounter++; //increment the counter of the element in the array
		} else if (v == null) {
			deleteKVP (k);
			print ("Key "+ k + " was deleted!");
			this.elementCounter--; //decrement the counter of the element in the array
		} else {
			kvpDouble[1].value = v;
			print ("KVP changed from " + oldValue + " to " + kvpDouble[1].value);
		}
		print ("current elementCounter: " + elementCounter);
		return (Value)oldValue;
	}

//------------------Private-------------------//
	private void deleteKVP (Key k) {
		print ("Deleting KVP with key: "+ k);
		int index = this.hashFunction (k);		
		KVP[] kvpDouble = this.tableSearch(k);
		if (kvpDouble[0] == null) {		
			this.tableArray[index] = (KVP)kvpDouble[1].pointer;
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
		return k == null ? 0 : (k.hashCode() & 0x7fffffff) % this.tableArray.length; 
	}	

	/*
	doubleTable()
	Original KVP table is first assigned to be pointed at by a variable. Then the 'tableArray' variable is assigned to a new KVP array 
	of size 2 * the length of the original table array. Now there are two arrays; the original one, and the doubled new one.
	Element counter is set back to 0 as the new table now contains 0 elements.
	The next step is to itterate through the orginal table and slot all those values into the new table, 
	via a call to the put method with arguments as the key and pointer of every entry in the orignial table. 
	This is done by assigning a temp KVP to each index of the original table and "following the chain" of KVPs until we get to a null one, 
	in which case the while loop exits and the for loop iterates to the next index of the oringinal table.
	*/
	private void doubleTable() { 
	 	KVP[] originalTableArray = tableArray;
		tableArray = new KVP[originalTableArray.length*2];
		elementCounter = 0; 
		print ("Doubling table. Original Table Length: " + originalTableArray.length + " New Table Length" + tableArray.length);		
		for (int i = 0; i < originalTableArray.length; i++) {
			KVP kvpTemp = originalTableArray[i];
			while (kvpTemp != null ) {
				this.put((Key)kvpTemp.key, (Value)kvpTemp.value);
				kvpTemp = (KVP)kvpTemp.pointer;
			}
		}
		for (KVP current : tableArray) {
			print(current);
		}
	}

}