package edu.yu.cs.com1320.project.stage4.impl;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;

import edu.yu.cs.com1320.project.stage4.DocumentStore;

import edu.yu.cs.com1320.project.stage4.Document;
import edu.yu.cs.com1320.project.stage4.impl.DocumentImpl;

import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.impl.TrieImpl;

import edu.yu.cs.com1320.project.Undoable;
import edu.yu.cs.com1320.project.GenericCommand;
import edu.yu.cs.com1320.project.CommandSet;
import java.util.function.Function;

import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.impl.StackImpl;

import edu.yu.cs.com1320.project.MinHeap;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;

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
	private MinHeap<Document> minHeap;
	private int documentCount;
	private int documentBytes;
	private int maxDocumentBytes;
	private int maxDocumentCount;

//------------------Constructor(s)-------------------//
	public DocumentStoreImpl () {
		this.uriToDocument = new HashTableImpl<>();
		this.commandStack = new StackImpl<>();
		this.trie = new TrieImpl<>();
		this.minHeap = new MinHeapImpl<>();
		this.documentCount = 0;
		this.documentBytes = 0;
		this.maxDocumentBytes = Integer.MAX_VALUE;
		this.maxDocumentCount = Integer.MAX_VALUE;		

	}

//------------------Getter(s)-------------------//

    @Override
    public Document getDocument(URI uri) {
    	//Get the document from the hashtable, update the lastUseTime if there is a non-null document that is being retrieved
    	//print ("Document retrieved URI: " + uri + ", content:  " + (uriToDocument.get(uri).getDocumentTxt() == null ? uriToDocument.get(uri).getDocumentBinaryData() : uriToDocument.get(uri).getDocumentTxt())); 
    	Document doc = uriToDocument.get(uri);
    	if (doc != null) {
			doc.setLastUseTime(System.nanoTime());
			minHeap.reHeapify(doc);    		
    	}

    	return doc;
    }

    @Override
    public List<Document> search(String keyword) {
    	//Call the trie to get documents sorted by keyword, update the lastUseTime of each document as well as the heap.
    	keyword = this.standardizeString(keyword);
 		List<Document> searchedDocuments = trie.getAllSorted(keyword, this.keywordDocumentComparatorGenerator(keyword));
 		for (Document doc : searchedDocuments) {
 			doc.setLastUseTime(System.nanoTime());
 			minHeap.reHeapify(doc);
 		}
    	return searchedDocuments;
    }

    @Override
    public List<Document> searchByPrefix(String keywordPrefix) {
    	//Call the trie to get documents sorted by keyword prefix, update the lastUseTime of each document as well as the heap.	
    	keywordPrefix = this.standardizeString(keywordPrefix);
 		List<Document> searchedDocuments = trie.getAllWithPrefixSorted(keywordPrefix, this.prefixDocumentComparatorGenerator(keywordPrefix));    	
 		for (Document doc : searchedDocuments) {
 			doc.setLastUseTime(System.nanoTime());
 			minHeap.reHeapify(doc);
 		}
    	return searchedDocuments;    	
    }

//------------------Setter(s)-------------------//
    @Override 
    public int putDocument(InputStream input, URI uri, DocumentFormat format) throws IOException {
    	//Delete
    	if (input == null) { 
    		if (uriToDocument.get(uri) == null) {return 0;} // Return 0 if there is no document to delete
			this.commandStack.push(this.modificationCommand(uri)); // otherwise, store the command for re-creation of the deleted doc
			this.deleteDocumentAndUpdate(uri); // Update document tracking structures and data to reflect deletion.
			return uriToDocument.put(uri, null).hashCode(); //call .put() with (uri, null) and return the hashcode of the value from .put()
    	}

    	//Check for bad input
    	if (uri == null || format == null || uri.toASCIIString().isBlank()) {throw new IllegalArgumentException("Bad input");}

    	//Create
    	if (uriToDocument.get(uri) == null) {
    		this.commandStack.push(this.creationCommand(uri));
   		 	print ("Creating Document. Input: " + input + ". URI: " + uri + ". Format: " + format);    		
    		this.insertDocumnet (input, uri, format); //This method will also add the document to the trie
    		print ("Document inserted. Content = " + this.uriToDocument.get(uri).getDocumentTxt() + ". URI = " + uri + ". Format = " + format);    		
    		return 0;
    	}  

		//Overwrite 	
    	this.commandStack.push(this.modificationCommand(uri));
    	int previousDocReturnValue = uriToDocument.get(uri).hashCode();
		this.deleteDocumentAndUpdate(uri); // Update document tracking structures and data to reflect deletion.
    	this.insertDocumnet (input, uri, format); //This method will also add the new version of the document to the trie and the heap.
    	return previousDocReturnValue;
    }
    
    @Override
    public boolean deleteDocument(URI uri) {
		if (this.uriToDocument.get(uri) != null) { 
			this.commandStack.push(this.modificationCommand(uri)); //store the command for re-creation of the deleted doc
			this.deleteDocumentAndUpdate(uri); // Update document tracking structures and data to reflect deletion.
		}
    	return (uriToDocument.put(uri,null) == null ? false : true);
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

    @Override
	public void undo() throws IllegalStateException {
		print ("In undo");
		print ("Command stack next element: " + commandStack.peek());
		if (this.commandStack.peek() == null) { //Throw an ISE if there are no commands on the stack
			print ("Exception thrown for no elements on commandStack!");
			throw new IllegalStateException();
			} 
		this.commandStack.pop().undo(); 
    	if ((this.documentCount > this.maxDocumentCount || this.documentBytes > this.maxDocumentBytes)) {
    		this.documentOverflowDeletion();
    	}		
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
			} else if (poppedCommand instanceof GenericCommand && ((GenericCommand)poppedCommand).getTarget().equals(uri)) {	
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
    	if ((this.documentCount > this.maxDocumentCount || this.documentBytes > this.maxDocumentBytes)) {
    		this.documentOverflowDeletion();
    	}	
	}

	@Override
	//Throw IAE if negative?
	public void setMaxDocumentCount(int limit) {
		this.maxDocumentCount = limit;
		if (this.documentCount > this.maxDocumentCount) {
			print ("maxDocumentCount Overflow! documentCount = " + this.documentCount + ". maxDocumentCount = " + this.maxDocumentCount);
			this.documentOverflowDeletion();
		}
	}

	@Override
	//Throw IAE if negative?	
	public void setMaxDocumentBytes(int limit) {
		this.maxDocumentBytes = limit;
		if (this.documentBytes > this.maxDocumentBytes) {
			print ("maxDocumentBytes Overflow! documentBytes = " + this.documentBytes + ". maxDocumentBytes = " + this.maxDocumentBytes);			
			this.documentOverflowDeletion();
		}		
	}

//------------------Private-------------------//

//INSERTION:
	//Insert a document into the document store, dependant on format. Add to trie if format is text.
	private void insertDocumnet (InputStream input, URI uri, DocumentFormat format) throws IOException {
		byte[] bytes = input.readAllBytes();
		if (bytes.length > this.maxDocumentBytes) {
			throw new IllegalArgumentException("Document attempted to be inserted is larget than the total byte limit!");
		}
    	if (format == DocumentFormat.BINARY) {
			uriToDocument.put(uri, new DocumentImpl(uri, bytes));    		 	
    	} else { 
			uriToDocument.put(uri, new DocumentImpl(uri, new String(bytes)));
			this.addToTrie(uri);    		 	    		
    	}
    	//Use the fact that the created document was just added to the HashTable to retreive it, set last use time, add to the minHeap.
    	this.uriToDocument.get(uri).setLastUseTime(System.nanoTime());
    	this.minHeap.insert(uriToDocument.get(uri));		

    	//Update documentCount and documntBytes to reflect the added document.
    	this.documentCount++;
    	this.documentBytes += this.documentBtyeValue(uri);
    	if ((this.documentCount > this.maxDocumentCount || this.documentBytes > this.maxDocumentBytes)) {
    		this.documentOverflowDeletion();
    	}
	}
			//Add to trie, Document param.
			private void addToTrie (Document doc) {
				//For every word in the doc, add the instance of the document to that node in the trie.				
				for (String word : doc.getWords()) {
					print ("Added to trie. Doc = " + doc + ". Word = " + word);					
					this.trie.put(word, doc);
				}
			}

			//Add to trie, URI param.
			private void addToTrie (URI uri) {
				this.addToTrie(this.uriToDocument.get(uri));		
			}

//DELETION:
	//Delete documents using minHeap.remove() until maxDocumentCount and maxDocumentBytes are both satisfied.
	//Deletion is from: Trie, HashTable, documentCount/documentBytes, and CommandStack.
	private void documentOverflowDeletion () {
		//While the count or bytes exceeds the max - proceed to delete the least used document.
		while (this.documentCount > this.maxDocumentCount || this.documentBytes > this.maxDocumentBytes) {
			print ("maxDocumentCount Overflow! documentCount = " + documentCount + ". maxDocumentCount = " + maxDocumentCount + ". documentBytes = " + documentBytes + ". maxDocumentBytes = " + maxDocumentBytes);			
			Document deletingDoc = minHeap.remove();
			print ("Removing due to overflow: " + deletingDoc + ". URI: " + deletingDoc.getKey());
			this.deleteFromTrie(deletingDoc);
			this.documentBytes -= this.documentBtyeValue(deletingDoc); // Decrease documentBytes by the byte representation of the document.			
			this.documentCount --; // Decrement the documentCount.
			print ("doc count: " + documentCount);
			this.uriToDocument.put(deletingDoc.getKey(), null);
			this.deleteFromCommandStack (deletingDoc.getKey());
		}
	}
			private void deleteFromCommandStack(URI uri) {
				Stack<Undoable> tempStack = new StackImpl<>();
				while(commandStack.peek() != null) {
					//Pop the top command into a var. Check for the instantiation of the Undoable and, after casting, call the appropriate methods 
					Undoable poppedCommand = commandStack.pop();
					if (poppedCommand instanceof CommandSet && ((CommandSet)poppedCommand).containsTarget(uri)) {
						Iterator commandIterator = ((CommandSet)poppedCommand).iterator();
						while (commandIterator.hasNext()) {
							GenericCommand command = (GenericCommand) commandIterator.next();
							if (command.getTarget().equals(uri)) {
								print ("Removed command from command set: " + command);
								commandIterator.remove();
							}	
						}
						if (((CommandSet)poppedCommand).size() > 0) {
							tempStack.push(poppedCommand);
						}
					} else if (poppedCommand instanceof GenericCommand && ((GenericCommand)poppedCommand).getTarget().equals(uri)) {
						print ("Removed generic command from stack: " + poppedCommand);
					} else {
						tempStack.push(poppedCommand);
					}
				}
				while (tempStack.peek() != null) {
					commandStack.push(tempStack.pop());		
				}							
			}


	private Set<URI> deleteMultipleDocuments (Set<Document> documents) {
		//Create a new command set to hold the commands for deletion of multiple documents.
		CommandSet<URI> deletionCommands = new CommandSet<>();
		Set<URI> deletedURIs = new HashSet<>();

		for (Document doc : documents) {
			//If the set of documents to delete only contains one document, treat this as a deletion of one document. Create a generic command and add it to the command stack.
			if (documents.size() == 1) {
				print ("Document: " + doc + " is the only document being deleted in a call to deleteAll or deleteAllWithPrefix");
				this.commandStack.push(this.modificationCommand(doc.getKey())); //store the command for re-creation of the deleted doc
				this.deleteDocumentAndUpdate(doc);
				this.uriToDocument.put(doc.getKey(), null);			
				return new HashSet<URI>(Arrays.asList(doc.getKey()));			
			} 

			//If there is more than one document in the set, proceed to delete the individual document. 
			//Add the deletion command to the command set, as well as the URI to the deleted uri set.
			//Make a call to delete from the hashTable as well as in the trie and heap.
			print ("Document: " + doc + " is being deleted deleted in a call to deleteAll or deleteAllWithPrefix");
			deletionCommands.addCommand(this.modificationCommand(doc.getKey()));
			deletedURIs.add(doc.getKey());
			this.deleteDocumentAndUpdate(doc);
			this.uriToDocument.put(doc.getKey(), null);
		}
		this.commandStack.push(deletionCommands);
		return deletedURIs;
	}

	//Delete a document from the data structures used to track documents (trie and heap) as well as updating documentCount and documentBytes.
	//DOES NOT MODIFY THE HASHTABLE.
	//URI param.
	private void deleteDocumentAndUpdate (URI uri) {
		this.deleteFromHeap(uri); //Delete any trace of the document from the heap.
		this.deleteFromTrie(uri);  //Delete any trace of the document from the trie.
		this.documentCount--; // Decrement the documentCount.
		this.documentBytes -= this.documentBtyeValue(uri); // Decrease documentBytes by the byte representation of the document.
	}

	//Document param.
	private void deleteDocumentAndUpdate (Document doc) {
		this.deleteDocumentAndUpdate(doc.getKey());
	}		
			//Delete from trie, URI param.
			private void deleteFromTrie (URI uri) {
				print ("Deleting from trie with URI = " + uri);
				//For every word in the doc, delete the instance of the document from that node in the trie.		
				Document doc = this.uriToDocument.get(uri);
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


			//Delete from minHeap, Document param.
			private void deleteFromHeap (Document doc) {
				//Create a temporary list to store the heap elements that are to be moved aside.
				//Loop trough the heap elements. Remove and check each element. 
				//If it matches the document to be deleted, break (and don't add it to the temp list). If not, add it to the temp list
				//If the loop goes through the end of the heap and the document is not there to be deleted, allow the exception to bubble up from the heap.
				List<Document> heapList = new ArrayList<>();
				while (true) {
					Document tempDoc = this.minHeap.remove();
					if (doc.equals(tempDoc)) {
						break;		
					}
					heapList.add(tempDoc);
				}

				//Reinsert the documents that were moved aside from the minHeap.
				for (Document tempDoc : heapList) {
					this.minHeap.insert(tempDoc);
				}
			}

			//Delete from minHeap. URI param. Call the method that deletes by document.
			private void deleteFromHeap (URI uri) {
				this.deleteFromHeap(this.uriToDocument.get(uri));
			}
	

//COMMANDS:
	//Command for document creation
	private GenericCommand<URI> creationCommand (URI uri) {
		return new GenericCommand<URI>(uri, (URI commandURI) -> {
			print("Command for creation of Document with URI " + commandURI);
			print ("Inital state: " + this.uriToDocument.get(commandURI));

			//When the undo of the command is executed:
			//Delete the created doc from the trie.
			this.deleteFromTrie(this.uriToDocument.get(commandURI));

			//Delete the created doc from the heap.
			this.deleteFromHeap(this.uriToDocument.get(commandURI));			

			this.documentCount--; // Decrement the documentCount.
			this.documentBytes -= this.documentBtyeValue(commandURI); // Decrease documentBytes by the byte representation of the document.

			//Delete the created doc from the hash table.
			this.uriToDocument.put(commandURI,null);

			print ("Undone state: " + this.uriToDocument.get(commandURI));

			return true;
		});
	}

	//Command for document deletion or overwriting
	private GenericCommand<URI> modificationCommand (URI uri) {
		Document originalDoc = this.uriToDocument.get(uri);	
		return new GenericCommand<URI>(uri, (URI commandURI) -> { 
			print("Command for modification of Document with URI " + commandURI);
			print ("Inital state: " + this.uriToDocument.get(commandURI));

			//When the command is executed:
			//If this is not undoing a deletion (and so the document appears in the hash table) delete the modified version of the doc from the trie and the heap. 
			if (this.uriToDocument.get(commandURI) != null) {
				this.deleteDocumentAndUpdate(commandURI);
				print ("Deleted from trie and heap");
			}

			//Add the original version of the doc to the trie.
			this.addToTrie(originalDoc);

			//Update the lastUseTime of the original version of the doc to reflect that a modification has been undone and insert it into the heap.
			originalDoc.setLastUseTime(System.nanoTime());
			minHeap.insert(originalDoc);

			//Override the modified version of the doc in the hash table with the original version of the doc.
			this.uriToDocument.put(commandURI, originalDoc);

	    	//Update documentCount and documntBytes to reflect the re-added document.
	    	this.documentCount++;
	    	this.documentBytes += this.documentBtyeValue(originalDoc);				

			print ("Undone state: " + this.uriToDocument.get(commandURI));			
			return true;
		});
	}


//COMPARATORS:
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


//MISC:
	//Get the number of bytes in a document's in-memory representation. 
	//Document param.
	private int documentBtyeValue (Document doc) {
		if (doc.getDocumentBinaryData() != null) {
			return doc.getDocumentBinaryData().length;
		} else {
			return doc.getDocumentTxt().getBytes().length;
		}
	}

	//Get the number of bytes in a document's in-memory representation. 
	//URI param.
	private int documentBtyeValue (URI uri) {
		return this.documentBtyeValue(this.uriToDocument.get(uri));
	}


	private String standardizeString (String txt) {
		//Make the string standardized for document processing. Make lowercase and remove non-alphanumeric charecters via regex expr. 
		txt = txt.replaceAll("[^a-zA-Z0-9 ]", "");		
		txt = txt.toLowerCase();
		return txt;
	}		
}