package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;

import edu.yu.cs.com1320.project.stage3.DocumentStore;

import edu.yu.cs.com1320.project.stage3.Document;
import edu.yu.cs.com1320.project.stage3.impl.DocumentImpl;

import edu.yu.cs.com1320.project.Undoable;
import edu.yu.cs.com1320.project.GenericCommand;
import edu.yu.cs.com1320.project.CommandSet;
import java.util.function.Function;

import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.impl.TrieImpl;

import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.impl.StackImpl;

import java.util.*;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;

import java.net.URI;
import java.net.URISyntaxException;

public class DocumentStoreImpl implements DocumentStore {
//------------------Debug-------------------//	
	final static boolean DEBUG = false;
	private static void print (Object str) {if (DEBUG) System.out.println(new Throwable().getStackTrace()[1] + ": " + str);}

//------------------Instance Variable(s)-------------------//
	private HashTableImpl <URI, Document> uriToDocument;
	private StackImpl <Undoable> commandStack;
	private Trie<Document> trie;

//------------------Constructor(s)-------------------//
	public DocumentStoreImpl () {
		this.uriToDocument = new HashTableImpl<>();
		this.commandStack = new StackImpl<>();
		this.trie = new TrieImpl<>();
	}

//------------------Getter(s)-------------------//
    @Override
    public Document getDocument(URI uri) {
    	//print ("Document retrieved URI: " + uri + ", content:  " + (uriToDocument.get(uri).getDocumentTxt() == null ? uriToDocument.get(uri).getDocumentBinaryData() : uriToDocument.get(uri).getDocumentTxt())); 
    	return uriToDocument.get(uri);
    }

    @Override
    public List<Document> search(String keyword) {
    	keyword = this.standardizeString(keyword);
    	return trie.getAllSorted(keyword, this.keywordDocumentComparatorGenerator(keyword));
    }

    @Override
    public List<Document> searchByPrefix(String keywordPrefix) {
    	keywordPrefix = this.standardizeString(keywordPrefix);
    	return trie.getAllWithPrefixSorted(keywordPrefix, this.prefixDocumentComparatorGenerator(keywordPrefix));
    }

//------------------Setter(s)-------------------//
    @Override 
    public int putDocument(InputStream input, URI uri, DocumentFormat format) throws IOException {
    	//Delete
    	if (input == null) { 
    		if (uriToDocument.get(uri) == null) {return 0;} // Return 0 if there is no document to delete
			this.commandStack.push(this.modificationCommand(uri)); // otherwise, store the command for re-creation of the deleted doc
			this.deleteFromTrie(uri); //Delete any trace of the document from the trie.
			return uriToDocument.put(uri, null).hashCode(); //call .put() with (uri, null) and return the hashcode of the value from .put()
    	}

    	//Check for bad input
    	if (uri == null || format == null || uri.toASCIIString().isBlank()) {throw new IllegalArgumentException();}

    	//Create
    	if (uriToDocument.get(uri) == null) {
    		this.commandStack.push(this.creationCommand(uri));
    		this.insertDocumnet (input, uri, format); //This method will also add the document to the trie
    		print ("Document inserted. Content = " + this.getDocument(uri).getDocumentTxt() + ". URI = " + uri + ". Format = " + format);
    		return 0;
    	}  

		//Overwrite 	
    	this.commandStack.push(this.modificationCommand(uri));
    	int previousDocReturnValue = uriToDocument.get(uri).hashCode();
		this.deleteFromTrie(uri);  //Delete any trace of the old version of the document from the trie.
    	this.insertDocumnet (input, uri, format); //This method will also add the new version of the document to the trie
    	return previousDocReturnValue;
    }
    
    @Override
    public boolean deleteDocument(URI uri) {
		if (this.uriToDocument.get(uri) != null) { 
			this.commandStack.push(this.modificationCommand(uri)); //store the command for re-creation of the deleted doc
			this.deleteFromTrie(uri);    	
		}
    	return (uriToDocument.put(uri,null) == null ? false : true);
    }

    @Override
	public void undo() throws IllegalStateException {
		if (this.commandStack.peek() == null) {throw new IllegalStateException();} //Throw an ISE if there are no commands on the stack
		this.commandStack.pop().undo(); 
	}
  
    @Override
	public void undo(URI uri) throws IllegalStateException {
		boolean undoFlag = false; //Flag to note if an "undo" was performed for the given URI
		Stack<Undoable> tempStack = new StackImpl<>();
		while(commandStack.peek() != null) {
			//Pop the top command into a var. Check for the instantiation of the Undoable and, after casting, call the appropriate methods 
			//to test if the command matches the URI. If there is a match, undo, set the flag to true, and break. If not, push the popped command onto the temp stack. 
			Undoable poppedCommand = commandStack.pop();
			if (poppedCommand instanceof CommandSet && ((CommandSet)poppedCommand).containsTarget(uri)) {
				print ("Undoning command set: " + poppedCommand);				
				((CommandSet)poppedCommand).undo(uri);
				undoFlag = true;
				if (((CommandSet)poppedCommand).size() > 0) {
					print ("Command set pushed back onto stack as size = " +((CommandSet)poppedCommand).size());
					tempStack.push(poppedCommand);
				}
				break;
			} else if (((GenericCommand)poppedCommand).getTarget().equals(uri)) {	
				print ("Undoning generic command: " + poppedCommand);
				((GenericCommand)poppedCommand).undo();
				undoFlag = true;
				break;
			}
			tempStack.push(poppedCommand);
		}
		while (tempStack.peek() != null) {
			commandStack.push(tempStack.pop());		
		}
		if (undoFlag == false) {throw new IllegalStateException();} //Throw an ISE if there is no such URI for the undo, i.e. the undo was not performed
	}

	@Override 
	public Set<URI> deleteAll(String keyword) {
		Set<Document> deletedDocuments = trie.deleteAll(keyword);
		print ("Deleted Docs: " + deletedDocuments);
		return this.deleteMultipleDocuments (deletedDocuments);
	}

	@Override 
	public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
		Set<Document> deletedDocuments = trie.deleteAllWithPrefix(keywordPrefix);
		print ("Deleted Docs by prefix: " + deletedDocuments);		
		return this.deleteMultipleDocuments (deletedDocuments);
	}

//------------------Private-------------------//
	private Set<URI> deleteMultipleDocuments (Set<Document> documents) {
		//Create a new command set to hold the commands for deletion of multiple documents.
		CommandSet<URI> deletionCommands = new CommandSet<>();
		Set<URI> deletedURIs = new HashSet<>();

		for (Document doc : documents) {
			//If the set of documents to delete only contains one document, treat this as a deletion of one document. Create a generic command and add it to the command stack.
			if (documents.size() == 1) {
				print ("Document: " + doc + " is the only document being deleted in a call to deleteAll or deleteAllWithPrefix");
				this.commandStack.push(this.modificationCommand(doc.getKey())); //store the command for re-creation of the deleted doc
				this.uriToDocument.put(doc.getKey(), null);			
				this.deleteFromTrie(doc);  
				return new HashSet<URI>(Arrays.asList(doc.getKey()));			
			} 

			//If there is more than one document in the set, proceed to delete the individual document. 
			//Add the deletion command to the command set, as well as the URI to the deleted uri set.
			//Make a call to delete from the hashTable as well as in the trie.
			print ("Document: " + doc + " is being deleted deleted in a call to deleteAll or deleteAllWithPrefix");
			deletionCommands.addCommand(this.modificationCommand(doc.getKey()));
			deletedURIs.add(doc.getKey());
			this.uriToDocument.put(doc.getKey(), null);
			this.deleteFromTrie(doc);
		}
		this.commandStack.push(deletionCommands);
		return deletedURIs;
	}

	private String standardizeString (String txt) {
		//Make the string standardized for document processing. Make lowercase and remove non-alphanumeric charecters via regex expr. 
		txt = txt.replaceAll("[^a-zA-Z0-9 ]", "");		
		txt = txt.toLowerCase();
		return txt;
	}

	//Insert a document into the hashtable, dependant on format. Add to trie if format is text.
	private void insertDocumnet (InputStream input, URI uri, DocumentFormat format) throws IOException {
    	if (format == DocumentFormat.BINARY) {
			uriToDocument.put(uri, new DocumentImpl(uri, input.readAllBytes()));    		 	
    	} else { 
			uriToDocument.put(uri, new DocumentImpl(uri, new String(input.readAllBytes())));
			this.addToTrie(uri);    		 	    		
    	}		
	}

	//Delete from trie, URI param.
	private void deleteFromTrie (URI uri) {
		print ("Deleting from trie with URI = " + uri);
		//For every word in the doc, delete the instance of the document from that node in the trie.		
		Document doc = this.getDocument(uri);
		for (String word : doc.getWords()) {
			if (this.trie.delete(word, doc) == null) {
				print ("Warning - deleteFromTrie attempted to delete a value from the trie that was not present! Word = " + word);
			}
		}		
	}

	//Delete from trie, Document param.
	private void deleteFromTrie (Document doc) {
		//For every word in the doc, delete the instance of the document from that node in the trie.		
		for (String word : doc.getWords()) {
			if (this.trie.delete(word, doc) == null) {
				print ("Warning - deleteFromTrie attempted to delete a value from the trie that was not present! Word = " + word);
			}
		}	
	}	

	//Add to trie, URI param.
	private void addToTrie (URI uri) {
		//For every word in the doc, add the instance of the document to that node in the trie.				
		Document doc = this.getDocument(uri);
		for (String word : doc.getWords()) {
			print ("Added to trie. Doc = " + doc + ". Word = " + word);
			this.trie.put(word, doc);
		}
	}

	//Add to trie, Document param.
	private void addToTrie (Document doc) {
		//For every word in the doc, add the instance of the document to that node in the trie.				
		for (String word : doc.getWords()) {
			this.trie.put(word, doc);
		}
	}

	//Command for document creation
	private GenericCommand<URI> creationCommand (URI uri) {
		return new GenericCommand<URI>(uri, (URI commandURI) -> {
			print("Command for creation of Document with URI " + commandURI);
			print ("Inital state: " + this.getDocument(commandURI));

			//When the command is executed:
			//Delete the created doc from the trie.
			this.deleteFromTrie(this.getDocument(commandURI));

			//Delete the created doc from the hash table.
			this.uriToDocument.put(commandURI,null);

			print ("Undone state: " + this.getDocument(commandURI));

			return true;
		});
	}

	//Command for document deletion or overwriting
	private GenericCommand<URI> modificationCommand (URI uri) {
		Document originalDoc = this.getDocument(uri);	
		return new GenericCommand<URI>(uri, (URI commandURI) -> { 
			print("Command for modification of Document with URI " + commandURI);
			print ("Inital state: " + this.getDocument(commandURI));

			//When the command is executed:
			//If this is not undoing a deletion (and so the documetn appears in the hash table) delete the modified version of the doc from the trie. 
			if (this.getDocument(commandURI) != null) {
				this.deleteFromTrie(this.getDocument(commandURI));
				print ("Deleted from trie");
			}

			//Add the original version of the doc to the trie.
			this.addToTrie(originalDoc);

			//Override the modified version of the doc in the hash table with the original version of the doc.
			this.uriToDocument.put(commandURI, originalDoc);

			print ("Undone state: " + this.getDocument(commandURI));			
			return true;
		});
	}

	//Create a comparator for sorting by a keyword
	private Comparator<Document> keywordDocumentComparatorGenerator (String keyword) {
		Comparator<Document> keywordDocumentComparator = (Document doc1, Document doc2) -> doc2.wordCount(keyword) - doc1.wordCount(keyword);
		return keywordDocumentComparator;
	}

	//Create a comparator for sorting by a prefix
	private Comparator<Document> prefixDocumentComparatorGenerator (String prefix) {
		Comparator<Document> prefixDocumentComparator = (Document doc1, Document doc2) -> {
			Set<String> allWords = new HashSet<>();
			allWords.addAll(doc1.getWords());
			allWords.addAll(doc2.getWords());
			int difference = 0;
			print ("Prefix: " + prefix); 
			print (allWords);
			for (String word : allWords) {
				if (word.length() >= prefix.length() && word.substring(0, prefix.length()).equals(prefix)) {
					print ("Doc 1: " + doc1 + " - Word: " + word);
					print ("Doc 2: " + doc2 + " - Word: " + word);					
					difference += (doc2.wordCount(word) - doc1.wordCount(word));
				} 
			}
			return difference;
		};
		return prefixDocumentComparator;
	}		

}