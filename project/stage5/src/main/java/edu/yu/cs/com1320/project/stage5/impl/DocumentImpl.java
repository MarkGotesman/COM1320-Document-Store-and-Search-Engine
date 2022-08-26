package edu.yu.cs.com1320.project.stage5.impl;
import edu.yu.cs.com1320.project.stage5.Document;

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
	private Map<String, Integer> wordCountMap;
	private long lastUseTime;

//------------------Constructor(s)-------------------//
    public DocumentImpl(URI uri, String txt) {
    	this (uri, txt, null);
    }

    public DocumentImpl(URI uri, String text, Map<String, Integer> wordCountMap) {
    	if (uri == null || uri.toASCIIString().isBlank() || text == null || text.isBlank()) {
    		throw new IllegalArgumentException("Issue in Document creation");
    	}
    	this.uri = uri;
    	this.text = text;
    	this.binaryData = null;
    	this.lastUseTime = 0;

    	if (wordCountMap == null) {
	       	//Create a new map. Reassign the variable text to a standardized version of the document content. Call putWordsIntoTable() to add the words to the word count map.     		
    		this.wordCountMap = new HashMap<>();       	
	    	text = this.standardizeString(text);
	    	this.putWordsIntoTable(text);	    		
    	} else {
			this.wordCountMap = wordCountMap;
    	}
    	
    	print ("New Document created. URI = " + this.uri + ". Raw text = " + this.text + ". Unique words = " + this.wordCountMap.keySet() + "Words to appearances = " + this.wordCountMap);

    }

    public DocumentImpl(URI uri, byte[] binaryData) {
   		if (uri == null || binaryData == null || binaryData.length == 0) {
    		throw new IllegalArgumentException();
    	}    	
    	this.uri = uri;
    	this.binaryData = binaryData;
    	this.text = null;
    	this.lastUseTime = 0;    	
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
		if (word.equals("") || !this.wordCountMap.keySet().contains(word)) {
			return 0;
		}


		//Call .get() on the wordmap and return the value stored at the key for the word.
		return this.wordCountMap.get(word);
	}

	@Override
	public Set<String> getWords() {
		//If this document contains no text, e.g. it is a byte doc, return an empty set
		if (this.text == null || this.text == "") {
			return new HashSet<String>();
		}
		return this.wordCountMap.keySet();
	}

	@Override
	public long getLastUseTime() {
		return this.lastUseTime;
	}

	@Override
	public Map<String,Integer> getWordMap() {
		Map<String,Integer> newMap = new HashMap<>();
		newMap.putAll (this.wordCountMap);
		return newMap;
	}


//------------------Setter(s)-------------------//
	@Override
	public void setLastUseTime(long timeInNanoseconds) {
		this.lastUseTime = timeInNanoseconds;
	}

	@Override
	public void setWordMap(Map<String,Integer> wordMap) {
		if (this.text == null) {
			throw new IllegalArgumentException("Attempted to set wordMap for a non-text Document"); 
		}
		this.wordCountMap = wordMap;
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

	@Override
	public int compareTo (Document doc) {
		return (int) (this.lastUseTime - doc.getLastUseTime());
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
		//Check if the word is contaied in the wordcount key set. If return is false, the word it new, so create a new entry for it in the wordcount map.
		//If return if false, increment the already existing value for that word by 1. 
		for (String word : txt.split("\\s+")) {
			if (this.wordCountMap.keySet().contains(word) == false) {
				print ("Inserting into hash table: " + word);
				this.wordCountMap.put(word, 1);
			} else {
				print ("Incrementing intances in wordCountMap for word: " + word + ". Prev. isntances = " + this.wordCountMap.get(word));
				this.wordCountMap.put(word, this.wordCountMap.get(word) + 1);

			}
		}
	}

}