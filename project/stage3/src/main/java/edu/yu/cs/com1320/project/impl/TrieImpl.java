package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.Trie;

import java.util.*;

public class TrieImpl<Value> implements Trie<Value> {
//------------------Debug-------------------//  
    final static boolean DEBUG = false;
    private static void print (Object str) {if (DEBUG) System.out.println(new Throwable().getStackTrace()[1] + ": " + str);}

//------------------Nested Class(es)-------------------//
    private class Node<Value> {
        protected HashSet<Value> values = new HashSet<>();
        protected Node[] links = new Node[128];
    }

//------------------Instance Variable(s)-------------------//
    private Node root = new Node<>();

//------------------Constructor(s)-------------------//  	

//------------------Getter(s)-------------------//
	@Override
	public List<Value> getAllSorted(String key, Comparator<Value> comparator) {
		//Standardize the key by making lowercase.
		key = key.toLowerCase();

		//Call .get() on the key. If the node does not exist, return empty Array List.
		print ("Call to .getAllSorted(). Key = " + key + ", comparator = " + comparator);
		Node node = this.get(this.root, key, 0);

		if (node == null) {
			return new ArrayList<Value>();
		}

		//Assign the values set of the node to a list, sort it, and return it.
		List<Value> valuesList = new ArrayList<Value>(node.values);
		valuesList.sort(comparator);
		print ("Returning .getAllSorted(). key = " + key + "comparator = " + comparator + ", valuesList = " + valuesList);				
		return valuesList;
	}

	@Override
	public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator) {
		//Standardize the key by making lowercase.
		prefix = prefix.toLowerCase();

		//Call .get() on the key. If the node does not exist then there are no descendants to match the prefix, return empty Array List.		
		Node node = this.get(this.root, prefix, 0);

		if (node == null) {
			return new ArrayList<Value>();
		}

		//Make a call to .getAllDescendantValues() with the node of the prefix as the parameter. 
		//Take the returned Set and convert it to an ArrayList. Sort it and return it.
		ArrayList<Value> valuesList = new ArrayList<>(this.getAllDescendantValues(node, new HashSet<Value>()));		
		print ("getAllWithPrefixSorted values pre sort = " + valuesList);
		valuesList.sort(comparator);
		print ("getAllWithPrefixSorted values post sort = " + valuesList);
		return valuesList;
	}

//------------------Setter(s)-------------------//
	@Override	
	public void put(String key, Value val) {
		//Standardize the key by making lowercase.
		key = key.toLowerCase();

		print ("Call to .put(). Args: key = " + key + " val = " + val);
		//If val is null - this is a delete.
		//Else, call the private .put() method.
		if (val == null) {
			this.deleteAll(key);
		} else { 
			this.put(this.root, key, val, 0);
		}
	}

	@Override
	public Set<Value> deleteAllWithPrefix(String prefix) {
		//Empty string should return an empty set
		if (prefix.equals("")) {
			return new HashSet<Value>();
		}

		//Standardize the key by making lowercase.
		prefix = prefix.toLowerCase();

		//The node at which the prefix subtrie is rooted is the node whose key is the prefix minus the prefix's last letter.
		//Call .get() on the prefixAncestor key. If the node does not exist then there are no descendants to match the prefix, return null.		
		String prefixAncestorKey = prefix.substring(0, prefix.length() - 1);
		Node prefixAncestor = this.get(this.root, prefixAncestorKey, 0);

		if (prefixAncestor == null) {
			return new HashSet<Value>();
		}

		//Extract the last letter of the prefix, assign the prefix node to a variable.
		char c = prefix.charAt(prefix.length() - 1);
		Node prf = prefixAncestor.links[c];

		//Call .getAllDescendantValues() to get all the values of the subtrie that starts with the prefix.
		HashSet<Value> valueSet = new HashSet<>(this.getAllDescendantValues(prf, new HashSet<Value>()));

		//Null out the link to the prefix node in the prefix ancestor.
		prefixAncestor.links[c] = null;

		//Call .deleteAllEmptyAncestors() to clean up the trie.
		this.deleteAllEmptyAncestors(prefixAncestorKey);

		print ("deleteAllWithPrefix Values Deleted = " + valueSet);
		return valueSet;
	}

	@Override
	public Set<Value> deleteAll(String key) {
		//Empty string should return an empty set.
		if (key.equals("")) {
			return new HashSet<Value>();
		}

		//Standardize the key by making lowercase.
		key = key.toLowerCase();

		//Call .get() on the key. If the node does not exist, return null.
		Node node = this.get(this.root, key, 0);

		if (node == null) {
			return new HashSet<Value>();
		}

		//Store the values to be deleted, delete all values from the node. 
		HashSet<Value> deletedValues = new HashSet<>(node.values);
		node.values.clear();

		//Call .deleteAllEmptyAncestors() to clean up the trie, return the values that were deleted.
		this.deleteAllEmptyAncestors(key);

		return deletedValues;
	}

	@Override
	public Value delete(String key, Value val) {
		//Standardize the key by making lowercase.
		key = key.toLowerCase();

		//Call .get() on the key. If the node does not exist, return null.		
		Node node = this.get(this.root, key, 0);

		if (node == null) {
			return null;
		}

		//Attempt to remove the specificed value. If it was removed, .remove() returns true.
		//If node.values is empty, call .deleteAllEmptyAncestors() to clean up the trie
		//Return the inputted valued .
		if (node.values.remove(val) == true) {
			if (node.values.isEmpty()) {
				this.deleteAllEmptyAncestors (key);
			} 
			return val;
		}
		
		//If a value was not removed, return null, per the spec.		
		return null;
		}

//------------------Private-------------------//
    private Node put(Node x, String key, Value val, int d) {
    	print ("Call to private .put(). Args: x = " + x +" key = " + key + " val = " + val + " d = " + d);
    	
    	//Create new node if the node neded for descending to the put does not yet exist
    	if (x == null) { 
    		x = new Node<Value>();
    		print ("Node created = " + x);
    	}

    	//d is the lentgh of the Trie chain; once it reaches the length of key, we've arrived
    	if (d == key.length()) {
    		x.values.add(val);
    		print ("Added val = " + val + " to x = " + x);    		
    		print ("Current value set: " + x.values);
    		return x;
  		}

  		//If we are not yet at the correct node, extract the next char and call a put on that char by incrementing d. 
  		//The returned node from that put will be set to x.links of this method frame to rebuild the trie.
  		char c = key.charAt(d);
  		x.links[c] = this.put (x.links[c], key, val, d + 1);
  		return x;
    }

    private Node get(Node x, String key, int d) {
    	//Link from parent was null - return null, indicating a miss (a node with given key is not in the trie).
    	if (x == null) {
    		return null;
    	}

    	//d is the lentgh of the Trie chain; once it reaches the length of key, we've arrived.
    	//Return the node that matched the key.
    	if (d == key.length()) {
    		return x;
    	}

  		//If we are not yet at the correct node, extract the next char and call a get on that char by incrementing d. 
  		//There is nothing to do on the way up from the recursive descent, so we can return at the call to .get().
  		char c = key.charAt(d);
  		return this.get(x.links[c], key, d+1);
    }

    private Set<Value> getAllDescendantValues(Node x, Set<Value> valueSet) {
    	//Add the values of x.values to the valueSet.
    	//If x.values is empty, i.e the node is only a connecting node in a longer word, then null will be added, not affecting the set.
    	valueSet.addAll(x.values);
    	print ("Value added to the valueSet = " + x.values);
    	
    	//Recurse down into the descendants of the given node for all non-null links, and add their values to the valueSet.
    	for (int i = 0; i < 128; i++) {
 			if (x.links[i] != null) {
    			this.getAllDescendantValues (x.links[i], valueSet);
    		}
    	}

    	//Return the value set.
    	return valueSet;
    }

    private void deleteAllEmptyAncestors (String key) {
    	//If the string is empty, this is the root and there are no ancestors, return.
    	if (key.equals("")) {
    		return;
    	}

    	//Get the node of the immediate ancestor to the node of 'key' by calling .get() on 'key' minus its last char.
    	String ancestorKey = key.substring(0, key.length() - 1);
    	Node ancestor = this.get (this.root, ancestorKey, 0);

    	//Char c, representing the child of this ancestor, is at the slot in the array of the acestor which is the last char of the key.
    	char c = key.charAt(key.length() - 1);

    	//If the descendant values are null, null out the link from the ancestor and call deleteAllEmptyAncestors once again.
    	if (getAllDescendantValues (ancestor.links[c], new HashSet<Value>()) == null) {
    		ancestor.links[c] = null;
    		this.deleteAllEmptyAncestors(ancestorKey);
    	}
    }
}

