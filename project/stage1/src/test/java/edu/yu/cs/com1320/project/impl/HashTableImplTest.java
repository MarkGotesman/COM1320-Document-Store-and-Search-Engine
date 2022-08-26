package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.HashTable;


import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HashTableImplTest {
	
	@Test
	void putKeyNullValueNull() {
		HashTable<Integer, Integer> hashTable = new HashTableImpl<>();
		hashTable.put(null, null);		
		assertEquals(hashTable.get(null), null);
	}

	@Test
	void returnNullNonExistentValue() {
		System.out.println("---------- \nRunning Test: returnNullNoValue");
		HashTable<Integer, Integer> hashTable = new HashTableImpl<>();
		assertEquals(null, hashTable.get(1));	
	}
	@Test
	void putKeyIntValueNull() {
		System.out.println("\n ---------- Running Test: putKeyIntValueNull");
		HashTable<Integer, Integer> hashTable = new HashTableImpl<>();
		hashTable.put(5, null);		
		assertEquals(null, hashTable.get(5));
	}

	@Test
	void putKeyNullValueInt() {
		HashTable<Integer, Integer> hashTable = new HashTableImpl<>();
		assertThrows (NullPointerException.class, () -> hashTable.put(null, 5), "hi");
	}

	@Test 
	void putKeyIntInt() {
		HashTable<Integer, Integer> hashTable = new HashTableImpl<>();
		assertEquals(null, hashTable.put(1, 2));
		assertEquals(2, hashTable.get(1));
	}

	@Test 
	void putKeyDeleteKey() {
		HashTable<Integer, Integer> hashTable = new HashTableImpl<>();
		hashTable.put(1, 2);
		hashTable.put(2, 4);
		assertEquals(2, hashTable.get(1));
		hashTable.put(1, null);
		assertEquals(null, hashTable.get(1));
	}	

	@Test 
	void putNewKeyReturnOldValue() {
		HashTable<Integer, Integer> hashTable = new HashTableImpl<>();
		hashTable.put(1, 2);
		assertEquals(2, hashTable.get(1));
		assertEquals(2, hashTable.put(1, 7));
	}	

	@Test 
	void putKeyCharChar() {
		HashTable<Character, Character> hashTable = new HashTableImpl<>();
		hashTable.put('A', 'B');
		assertEquals('B', hashTable.get('A'));
	}

	@Test 
	void putKeyStringString() {
		HashTable<String, String> hashTable = new HashTableImpl<>();
		hashTable.put("Hi", "how are you today?");
		assertEquals("how are you today?", hashTable.get("Hi"));		
	}

	@Test 
	void putKeyIntValueString() {
		HashTable<Integer, String> hashTable = new HashTableImpl<>();
		hashTable.put(7, "Cheese");
		assertEquals("Cheese", hashTable.get(7));
	}

	@Test
	void putKeyHashSetValueIntArray () {
		HashTable <HashSet, Integer[]> hashTable = new HashTableImpl<>();
		
		HashSet<Integer> hashSet1 = new HashSet<>();
		hashSet1.add(5);
		hashSet1.add(18);
		hashSet1.add(24);

		HashSet<Integer> hashSet2 = new HashSet<>();
		hashSet2.add(6);
		hashSet2.add(17);
		hashSet2.add(22);

		Integer[] intArray1 = {55,3,5,6,7};
		Integer[] intArray2 = {23,4,45,5};

		hashTable.put(hashSet1, intArray1);
		hashTable.put(hashSet2, intArray2);

		assertEquals(55, hashTable.get(hashSet1)[0]);
		assertEquals(23, hashTable.get(hashSet2)[0]);
	}
}