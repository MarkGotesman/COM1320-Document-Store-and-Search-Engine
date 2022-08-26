package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.Trie;

import java.util.*;
import java.lang.Math;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TrieImplTest {
	Trie trie;
	Comparator<Integer> integerComparator;

	final static boolean DEBUG = false;
	private static void print (Object str) {if (DEBUG) System.out.println(new Throwable().getStackTrace()[1] + ": " + str);}

	@BeforeEach
	void init() {
		trie = new TrieImpl<Set<Integer>>();		
		integerComparator = (Integer int1, Integer int2) -> int2 - int1;
	}

	@Test
	void putOneKeyIntegerSetValue () {
		Set<Integer> tempIntegerSet = this.randomIntegerSet(5);
		for (Integer integer : tempIntegerSet) {
			trie.put("Shell", integer);
		}
		List<Integer> tempIntegerList = new ArrayList<>(tempIntegerSet);
		tempIntegerList.sort(integerComparator);
		assertEquals(tempIntegerList, trie.getAllSorted("Shell", integerComparator));
	}

	@Test
	void putManyKeysIntegerSetValue () {
		Map<String, Set<Integer>> stringtoInteger = this.randomStringtoIntegerSetMap (50);
		for (String key : stringtoInteger.keySet()) {
			for (Integer integer : stringtoInteger.get(key)) {
				trie.put(key, integer);
			}			
		}

		for (String key : stringtoInteger.keySet()) {
			List<Integer> tempIntegerList = new ArrayList<>(stringtoInteger.get(key));
			tempIntegerList.sort(integerComparator);			
			assertEquals(tempIntegerList, trie.getAllSorted(key, integerComparator));
		}		
	}

	@Test
	void getAllWithPrefixSorted () {
		Set<Integer> integerSet1 = this.randomIntegerSet(5);
		Set<Integer> integerSet2 = this.randomIntegerSet(5);
		Set<Integer> integerSet3 = this.randomIntegerSet(5);
		Set<Integer> integerSet4 = this.randomIntegerSet(5);
		Set<Integer> integerSet5 = this.randomIntegerSet(5);

		this.putSetIntoTrie("S", integerSet1, trie);
		this.putSetIntoTrie("Sh", integerSet2, trie);
		this.putSetIntoTrie("She", integerSet3, trie);
		this.putSetIntoTrie("Sells", integerSet4, trie);
		this.putSetIntoTrie("Shore", integerSet5, trie);

		Set<Integer> tempIntegerSet = new HashSet<>();
		tempIntegerSet.addAll(integerSet2);
		tempIntegerSet.addAll(integerSet3);
		tempIntegerSet.addAll(integerSet5);

		List<Integer> tempIntegerList = new ArrayList<>(tempIntegerSet);

		tempIntegerList.sort(integerComparator);
		assertEquals(tempIntegerList, trie.getAllWithPrefixSorted("Sh", integerComparator));
	}

	@Test
	void getAllWithPrefixSortedCASEINSENSITIVE () {
		Set<Integer> integerSet1 = this.randomIntegerSet(5);
		Set<Integer> integerSet2 = this.randomIntegerSet(5);
		Set<Integer> integerSet3 = this.randomIntegerSet(5);
		Set<Integer> integerSet4 = this.randomIntegerSet(5);
		Set<Integer> integerSet5 = this.randomIntegerSet(5);

		this.putSetIntoTrie("s", integerSet1, trie);
		this.putSetIntoTrie("sH", integerSet2, trie);
		this.putSetIntoTrie("ShE", integerSet3, trie);
		this.putSetIntoTrie("seLlS", integerSet4, trie);
		this.putSetIntoTrie("ShORe", integerSet5, trie);

		Set<Integer> tempIntegerSet = new HashSet<>();
		tempIntegerSet.addAll(integerSet2);
		tempIntegerSet.addAll(integerSet3);
		tempIntegerSet.addAll(integerSet5);

		List<Integer> tempIntegerList = new ArrayList<>(tempIntegerSet);

		tempIntegerList.sort(integerComparator);
		assertEquals(tempIntegerList, trie.getAllWithPrefixSorted("sH", integerComparator));
	}


	@Test
	void getAllWithPrefixSortedSameValue() {
		Set<Integer> integerSet1 = new HashSet<>(Arrays.asList(3,4,7,8));
		Set<Integer> integerSet2 = new HashSet<>(Arrays.asList(3,4,7,9));		

		this.putSetIntoTrie("Hi", integerSet1, trie);
		this.putSetIntoTrie("His", integerSet2, trie);

		Set<Integer> tempIntegerSet = new HashSet<>();
		tempIntegerSet.addAll(integerSet1);
		tempIntegerSet.addAll(integerSet2);

		List<Integer> tempIntegerList = new ArrayList<>(tempIntegerSet);

		tempIntegerList.sort(integerComparator);
		assertEquals(tempIntegerList, trie.getAllWithPrefixSorted("Hi", integerComparator));
	}

	@Test
	void deleteAllWithPrefix() {
		Set<Integer> integerSet1 = this.randomIntegerSet(5);
		Set<Integer> integerSet2 = this.randomIntegerSet(5);
		Set<Integer> integerSet3 = this.randomIntegerSet(5);
		Set<Integer> integerSet4 = this.randomIntegerSet(5);
		Set<Integer> integerSet5 = this.randomIntegerSet(5);

		this.putSetIntoTrie("H", integerSet1, trie);
		this.putSetIntoTrie("He", integerSet2, trie);		
		this.putSetIntoTrie("Her", integerSet3, trie);
		this.putSetIntoTrie("Help", integerSet4, trie);
		this.putSetIntoTrie("Hat", integerSet5, trie);

		Set<Integer> tempIntegerList = new HashSet<>();
		tempIntegerList.addAll(integerSet2);
		tempIntegerList.addAll(integerSet3);
		tempIntegerList.addAll(integerSet4);	


		assertEquals(tempIntegerList, trie.deleteAllWithPrefix("He"));
		assertEquals(new ArrayList<>(), trie.getAllSorted("He",integerComparator));
		assertEquals(new ArrayList<>(), trie.getAllSorted("Her",integerComparator));
		assertEquals(new ArrayList<>(), trie.getAllSorted("Help",integerComparator));

	}

	@Test
	void deleteAll() {
		Set<Integer> integerSet1 = this.randomIntegerSet(5);
		Set<Integer> integerSet2 = this.randomIntegerSet(5);
		Set<Integer> integerSet3 = this.randomIntegerSet(5);
		Set<Integer> integerSet4 = this.randomIntegerSet(5);
		Set<Integer> integerSet5 = this.randomIntegerSet(5);

		this.putSetIntoTrie("H", integerSet1, trie);
		this.putSetIntoTrie("He", integerSet2, trie);		
		this.putSetIntoTrie("Her", integerSet3, trie);
		this.putSetIntoTrie("Help", integerSet4, trie);
		this.putSetIntoTrie("Hat", integerSet5, trie);

		assertEquals(integerSet2, new HashSet<Integer> (trie.deleteAll("He")));

		List<Integer> tempIntegerList = new ArrayList<>(integerSet1);
		tempIntegerList.sort(integerComparator);
		assertEquals(tempIntegerList, trie.getAllSorted("H", integerComparator));
	}

	@Test
	void delete() {
		Set<Integer> integerSet1 = new HashSet<>(Arrays.asList(3,4,7,8));
		Set<Integer> integerSet2 = new HashSet<>(Arrays.asList(3,4,7,9));		

		this.putSetIntoTrie("Hi", integerSet1, trie);
		this.putSetIntoTrie("His", integerSet2, trie);

		assertEquals(3, trie.delete("Hi", 3));
		List<Integer> tempIntegerList = new ArrayList<Integer>(Arrays.asList(8,7,4));
		assertEquals(tempIntegerList, trie.getAllSorted("Hi", integerComparator));		
	}

	public void putSetIntoTrie (String key, Set<Integer> set, Trie trie) {
		for (Integer val : set) {
			trie.put(key, val);
		}
	}

	@Test 
	void deleteEntireTrie() {
		Set<Integer> integerSet1 = this.randomIntegerSet(5);
		Set<Integer> integerSet2 = this.randomIntegerSet(5);
		Set<Integer> integerSet3 = this.randomIntegerSet(5);
		Set<Integer> integerSet4 = this.randomIntegerSet(5);
		Set<Integer> integerSet5 = this.randomIntegerSet(5);

		this.putSetIntoTrie("H", integerSet1, trie);
		this.putSetIntoTrie("He", integerSet2, trie);		
		this.putSetIntoTrie("Her", integerSet3, trie);
		this.putSetIntoTrie("Help", integerSet4, trie);
		this.putSetIntoTrie("Hat", integerSet5, trie);
		
		Set<Integer> tempIntegerSet = new HashSet<>();
		tempIntegerSet.addAll(integerSet1);
		tempIntegerSet.addAll(integerSet2);
		tempIntegerSet.addAll(integerSet3);
		tempIntegerSet.addAll(integerSet4);
		tempIntegerSet.addAll(integerSet5);

		assertEquals(tempIntegerSet, trie.deleteAllWithPrefix("h"));

		tempIntegerSet = this.randomIntegerSet(5);
		for (Integer integer : tempIntegerSet) {
			trie.put("Shell", integer);
		}
		List<Integer> tempIntegerList = new ArrayList<>(tempIntegerSet);
		tempIntegerList.sort(integerComparator);
		assertEquals(tempIntegerList, trie.getAllSorted("Shell", integerComparator));
	}

//-----------------Helper Methods--------------------//
	public Map<String, Set<Integer>> randomStringtoIntegerSetMap (int size) {
		Set<String> randomStrings = this.randomStringSet(size);
		Set<Set<Integer>> randomIntegersSets = new HashSet<>();
		while (randomIntegersSets.size() <= size) {
			randomIntegersSets.add(this.randomIntegerSet(5));
		}
 		
		Map<String, Set<Integer>> randomStringtoIntegerSetMap = new HashMap<>();

		Iterator<String> strIterator = randomStrings.iterator();
		Iterator<Set<Integer>> intIterator = randomIntegersSets.iterator();

		while (strIterator.hasNext() && intIterator.hasNext()) {
			randomStringtoIntegerSetMap.put(strIterator.next(), intIterator.next());
		}		
		return randomStringtoIntegerSetMap;
	}

	public Set<Integer> randomIntegerSet (int size) {
		Set<Integer> integerSet = new HashSet<>();
		while (integerSet.size() < size) {
			integerSet.add((int) (Math.random() * 10 * size));
		}
		return integerSet;
	}

	public Set<String> randomStringSet (int size) {
		Set<String> stringSet = new HashSet<>();
		while (stringSet.size() < size) {
			stringSet.addAll(this.randomStringSet(size, stringSet, new String ("")));
		}
		return stringSet;
	}

	private Set<String> randomStringSet (int size, Set<String> stringSet, String key) {
		int incrementor = (int)(Math.random() * 58);
		for (int i = incrementor; Character.isAlphabetic(65 + i) && stringSet.size() < size && key.length() < 5; i = i + incrementor) {
			key = key + (char)(65 + i);
			if (Math.random() < .5 && !stringSet.stream().anyMatch(key::equalsIgnoreCase)) {
				stringSet.add(key);
			}
			this.randomStringSet(size, stringSet, key);
		} 
		return stringSet;
	}
}