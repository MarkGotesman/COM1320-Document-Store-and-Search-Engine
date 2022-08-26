package edu.yu.cs.com1320.project.stage3.impl;
import edu.yu.cs.com1320.project.stage3.Document;
import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;


import java.net.URI;
import java.util.*;

public class DocumentImpl implements Document {
//------------------Debug-------------------//	
	final static boolean DEBUG = false;
	private static void print (Object str) {if (DEBUG) System.out.println(new Throwable().getStackTrace()[1] + ": " + str);}

//------------------Instance Variable(s)-------------------//
	private URI uri;
	private String text;
	private byte[] binaryData;
	private Set<String> uniqueWords;	
	private HashMap<String, Integer> wordsToAppearances;

//------------------Constructor(s)-------------------//
    public DocumentImpl(URI uri, String txt) {
    	if (uri == null || uri.toASCIIString().isBlank() || txt == null || txt.isBlank()) {
    		throw new IllegalArgumentException();
    	}
    	this.uri = uri;
    	this.text = txt;
    	this.binaryData = null;
    	this.uniqueWords = new HashSet<>();
    	this.wordsToAppearances = new HashMap<>();
    	
       	//Reassign the variable txt to a standardized version of the document content.
    	//Call putWordsIntoTable() to add the words to the hash table. 
    	txt = this.standardizeString(txt);
    	this.putWordsIntoTable(txt);
    	print ("New Documetn created. URI = " + this.uri + ". Raw text = " + this.text + ". Unique words = " + this.uniqueWords + "Words to appearances = " + this.wordsToAppearances);
    }

    public DocumentImpl(URI uri, byte[] binaryData) {
   		if (uri == null || binaryData == null || binaryData.length == 0) {
    		throw new IllegalArgumentException();
    	}    	
    	this.uri = uri;
    	this.binaryData = binaryData;
    	this.text = null;
    }


//------------------Getter(s)-------------------//
	@Override	
	public String getDocumentTxt() {
		return text;
	}
	
	@Override
    public byte[] getDocumentBinaryData() {
    	return binaryData;
    }
	
	@Override
    public URI getKey() {
    	return uri;
    }

	@Override
	public int wordCount(String word) {
		//If this document contains no text, e.g. it is a byte doc, or if it does not contain the word at all, return 0
		if (this.text == null || this.text == "") {
			return 0;
		}

		//Standardize the word string. Check if empty - if so, return 0.
		//Also, check if the doc contains the now standardized word. If not, return 0.
		word = this.standardizeString(word);
		if (word.equals("") || !this.uniqueWords.contains(word)) {
			return 0;
		}


		//Call .get() on the hashTable and return the value stored at the key for the word.
		return this.wordsToAppearances.get(word);
	}

	@Override
	public Set<String> getWords() {
		//If this document contains no text, e.g. it is a byte doc, return an empty set
		if (this.text == null || this.text == "") {
			return new HashSet<String>();
		}
		return this.uniqueWords;
	}


//------------------Override(s)-------------------//
	@Override
	public int hashCode() {
		int result = this.uri.hashCode();
		result = 31 * result + (this.text != null ? this.text.hashCode() : 0);
		result = 31 * result + Arrays.hashCode(this.binaryData);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		Document otherDocument = (Document) obj;
		return (this.hashCode() == otherDocument.hashCode());
	}

//------------------Private-------------------//
	private String standardizeString (String txt) {
		//Make the string standardized for document processing. Make lowercase and remove non-alphanumeric charecters via regex expr. 
		txt = txt.replaceAll("[^a-zA-Z0-9 ]", "");		
		txt = txt.toLowerCase();
		return txt;
	}

	private void putWordsIntoTable (String txt) {
		//Iterate through the string that was passed in, delimiting at whitespace.
		//Attempt to add the word to the uniqueWords set. If return is true, the word it new, so create a new entry for it in the hashtable.
		//If return if false, increment the already existing value for that word by 1. 
		for (String word : txt.split("\\s+")) {
			if (this.uniqueWords.add(word) == true) {
				print ("Inserting into hash table: " + word);
				this.wordsToAppearances.put(word, 1);
			} else {
				print ("Incrementing intances in hashtable for word: " + word + ". Prev. isntances = " + this.wordsToAppearances.get(word));
				this.wordsToAppearances.put(word, this.wordsToAppearances.get(word) + 1);

			}
		}
	}

}