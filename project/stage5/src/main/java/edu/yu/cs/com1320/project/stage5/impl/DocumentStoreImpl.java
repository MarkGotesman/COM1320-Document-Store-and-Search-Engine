package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.DocumentStore;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;

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

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.impl.BTreeImpl;

import edu.yu.cs.com1320.project.stage5.PersistenceManager;
import edu.yu.cs.com1320.project.stage5.impl.DocumentPersistenceManager;

import java.util.*;
import java.io.*;
import java.nio.file.*;

import java.net.URI;
import java.net.URISyntaxException;

public class DocumentStoreImpl implements DocumentStore {
//------------------Debug-------------------//	
	final static boolean DEBUG = false;
	private static void print (Object str) {if (DEBUG) System.out.println(new Throwable().getStackTrace()[1] + ": " + str);}

//------------------Instance Variable(s)-------------------//
	private StackImpl <Undoable> commandStack;
	private Trie<URI> trie;
	private MinHeap<URI> minHeap;
	private BTreeImpl <URI, Document> bTree;
	private Set<URI> uriInDisk;

	private int documentCount;
	private int documentBytes;
	private int maxDocumentBytes;
	private int maxDocumentCount;

//------------------Constructor(s)-------------------//
	public DocumentStoreImpl () {
		this(null);
	}

	public DocumentStoreImpl (File baseDir) {
		this.commandStack = new StackImpl<>();
		this.trie = new TrieImpl<>();
		this.bTree = new BTreeImpl<>();
		this.bTree.setPersistenceManager(new DocumentPersistenceManager(baseDir));
		this.minHeap = new MinHeapImpl<>(bTree);
		this.uriInDisk = new HashSet<>();

		this.documentCount = 0;
		this.documentBytes = 0;
		this.maxDocumentBytes = Integer.MAX_VALUE;
		this.maxDocumentCount = Integer.MAX_VALUE;				
	}
//------------------Getter(s)-------------------//

    @Override
    public Document getDocument(URI uri) {
    	//Get the document from the BTree, update the lastUseTime if there is a non-null document that is being retrieved
    	// print ("Document retrieved URI: " + uri + ", content:  " + (bTree.get(uri).getDocumentTxt() == null ? bTree.get(uri).getDocumentBinaryData() : bTree.get(uri).getDocumentTxt())); 
    	print ("Getting doc");
    	Document doc = bTree.get(uri);
    	if (doc != null) {
    		print ("Got doc: " + doc.getKey());
			doc.setLastUseTime(System.nanoTime());    		
    		//If this document is being pulled back from disk: update the memory allocation accordngly and add to the min heap.
    		if (uriInDisk.remove(doc.getKey())) {
		    	this.minHeap.insert(uri);		
				this.updateMemoryUsage(uri);
    		} else {
				minHeap.reHeapify(doc.getKey());    
			}		
    	}
    	return doc;
    }

    @Override
    public List<Document> search(String keyword) {
    	//Call the trie to get documents sorted by keyword, update the lastUseTime of each document as well as the heap.
    	keyword = this.standardizeString(keyword);
 		List<URI> searchedURIs = trie.getAllSorted(keyword, this.keywordDocumentComparatorGenerator(keyword));
 		List<Document> searchedDocuments = new ArrayList<>();
 		for (URI uri : searchedURIs) {
 			bTree.get(uri).setLastUseTime(System.nanoTime()); 			
    		if (uriInDisk.remove(uri)) {
		    	this.minHeap.insert(uri);		
				this.updateMemoryUsage(uri);	
    		} else {
	 			minHeap.reHeapify(uri);
 			}
	 		searchedDocuments.add(bTree.get(uri));
 		}
    	return searchedDocuments;
    }

    @Override
    public List<Document> searchByPrefix(String keywordPrefix) {
    	//Call the trie to get documents sorted by keyword prefix, update the lastUseTime of each document as well as the heap.	
    	keywordPrefix = this.standardizeString(keywordPrefix);
 		List<URI> searchedURIs = trie.getAllWithPrefixSorted(keywordPrefix, this.prefixDocumentComparatorGenerator(keywordPrefix));    	
 		List<Document> searchedDocuments = new ArrayList<>();
 		for (URI uri : searchedURIs) {
 			bTree.get(uri).setLastUseTime(System.nanoTime()); 			
    		if (uriInDisk.remove(uri)) {
		    	this.minHeap.insert(uri);		
				this.updateMemoryUsage(uri);	
    		} else {
	 			minHeap.reHeapify(uri);
 			}
	 		searchedDocuments.add(bTree.get(uri));
 		}
    	return searchedDocuments;    	
    }

//------------------Setter(s)-------------------//
    @Override 
    public int putDocument(InputStream input, URI uri, DocumentFormat format) throws IOException {
    	//Delete
    	if (input == null) { 
    		if (bTree.get(uri) == null) {return 0;} // Return 0 if there is no document to delete
			this.commandStack.push(this.modificationCommand(uri)); // otherwise, store the command for re-creation of the deleted doc
			this.deleteDocumentAndUpdate(uri); // Update document tracking structures and data to reflect deletion.
			return bTree.put(uri, null).hashCode(); //call .put() with (uri, null) and return the hashcode of the value from .put()
    	}

    	//Check for bad input
    	if (uri == null || format == null || uri.toASCIIString().isBlank()) {throw new IllegalArgumentException("Bad input");}

    	//Create
    	if (bTree.get(uri) == null) {
    		this.commandStack.push(this.creationCommand(uri));
   		 	print ("Creating Document. Input: " + input + ". URI: " + uri + ". Format: " + format);    		
    		this.insertDocumnet (input, uri, format); //This method will also add the document to the trie
    		print ("Document inserted. Content = " + this.bTree.get(uri).getDocumentTxt() + ". URI = " + uri + ". Format = " + format);    		
    		return 0;
    	}  

		//Overwrite 	
		print ("Overriting doc with URI" + uri);
    	this.commandStack.push(this.modificationCommand(uri));
    	int previousDocReturnValue = bTree.get(uri).hashCode();
		this.deleteDocumentAndUpdate(uri); // Update document tracking structures and data to reflect deletion.
    	this.insertDocumnet (input, uri, format); //This method will also add the new version of the document to the trie and the heap.
    	return previousDocReturnValue;//
    }
    
    @Override
    public boolean deleteDocument(URI uri) {
    	
		if (this.bTree.get(uri) != null) { 
			this.commandStack.push(this.modificationCommand(uri)); //store the command for re-creation of the deleted doc
			print ("stored the command for re-creation of the deleted doc with URI: " + uri);
			this.deleteDocumentAndUpdate(uri); // Update document tracking structures and data to reflect deletion.
		}
		print ("***************documentCount = " + documentCount + ". maxDocumentCount = " + maxDocumentCount + ". documentBytes = " + documentBytes + ". maxDocumentBytes = " + maxDocumentBytes);
    	return (bTree.put(uri,null) == null ? false : true);
    }

	@Override 
	public Set<URI> deleteAll(String keyword) {
		Set<URI> deletedURIs = trie.deleteAll(keyword);
		print ("Deleted URIs: " + deletedURIs);
		return this.deleteMultipleDocuments (deletedURIs);
	}

	@Override 
	public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
		print ("Deleting from prefix:" + keywordPrefix);
		Set<URI> deletedURIs = trie.deleteAllWithPrefix(keywordPrefix);
		print ("Deleted URIs by prefix: " + deletedURIs);		
		return this.deleteMultipleDocuments (deletedURIs);
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
    		this.documentOverflowMovetoDisk();
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
    		this.documentOverflowMovetoDisk();
    	}	
	


	}

	@Override
	//Throw IAE if negative?
	public void setMaxDocumentCount(int limit) {
		this.maxDocumentCount = limit;
		if (this.documentCount > this.maxDocumentCount) {
			print ("maxDocumentCount Overflow! documentCount = " + this.documentCount + ". maxDocumentCount = " + this.maxDocumentCount);
			this.documentOverflowMovetoDisk();
		}
	}

	@Override
	//Throw IAE if negative?	
	public void setMaxDocumentBytes(int limit) {
		this.maxDocumentBytes = limit;
		if (this.documentBytes > this.maxDocumentBytes) {
			print ("maxDocumentBytes Overflow! documentBytes = " + this.documentBytes + ". maxDocumentBytes = " + this.maxDocumentBytes);			
			this.documentOverflowMovetoDisk();
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
			bTree.put(uri, new DocumentImpl(uri, bytes));    		 	
    	} else { 
	

		print ("URI: " + uri + "Bytes: "+ new String(bytes));
			bTree.put(uri, new DocumentImpl(uri, new String(bytes)));
	

			this.addToTrie(uri);    		 	    		
    	}

    	//Use the fact that the created document was just added to the BTree to retreive it, set last use time, add to the minHeap.
    	this.minHeap.insert(uri);		
    	this.bTree.get(uri).setLastUseTime(System.nanoTime());
    	this.minHeap.reHeapify(uri);
	


    	//Update documentCount and documntBytes to reflect the added document.
		print ("***************documentCount = " + documentCount + ". maxDocumentCount = " + maxDocumentCount + ". documentBytes = " + documentBytes + ". maxDocumentBytes = " + maxDocumentBytes);		
		this.updateMemoryUsage(uri);
		print ("***************documentCount = " + documentCount + ". maxDocumentCount = " + maxDocumentCount + ". documentBytes = " + documentBytes + ". maxDocumentBytes = " + maxDocumentBytes);

    }
	
			//Add to trie, Document param.
			private void addToTrie (Document doc) {
				//For every word in the doc, add the instance of the document to that node in the trie.				
				for (String word : doc.getWords()) {
					this.trie.put(word, doc.getKey());
				}
			}

			//Add to trie, URI param.
			private void addToTrie (URI uri) {
				this.addToTrie(this.bTree.get(uri));		
			}


//MOVE_TO_DISK:
	//Move documents to disk using minHeap.remove() until maxDocumentCount and maxDocumentBytes are both satisfied.
	//Deletion is of: documentCount/documentBytes.
	//Left in: Trie, CommandStack
	private void documentOverflowMovetoDisk () {
		//While the count or bytes exceeds the max - proceed to delete the least used document.
		while (this.documentCount > this.maxDocumentCount || this.documentBytes > this.maxDocumentBytes) {
			print ("maxDocumentCount Overflow! documentCount = " + documentCount + ". maxDocumentCount = " + maxDocumentCount + ". documentBytes = " + documentBytes + ". maxDocumentBytes = " + maxDocumentBytes);			
			URI deletingURI = minHeap.remove();
			print ("URI: " + deletingURI);
			this.documentBytes -= this.documentBtyeValue(deletingURI); // Decrease documentBytes by the byte representation of the document.			
			this.documentCount --; // Decrement the documentCount.
			print ("doc count: " + documentCount);
			String content = bTree.get(deletingURI).getDocumentTxt();
			try {
				this.uriInDisk.add(deletingURI);
				this.bTree.moveToDisk(deletingURI);
				print ("Moved URI to disk, added URI to uriInDisk set: " + deletingURI);

			} catch (Exception e) {
				throw new IllegalStateException ("Error moving URI to disk: " + deletingURI + "Exception:" + e);
			}
		}
	}

	/* *DEPRECATED* 
	Commands are no longer deleted from the CommandStack.
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
	*/


//DELETION:
	private Set<URI> deleteMultipleDocuments (Set<URI> uriSet) {
		//Create a new command set to hold the commands for deletion of multiple uriSet.
		CommandSet<URI> deletionCommands = new CommandSet<>();
		Set<URI> deletedURIs = new HashSet<>();
		for (URI uri : uriSet) {
			//If the set of uriSet to delete only contains one document, treat this as a deletion of one document. Create a generic command and add it to the command stack.
			if (uriSet.size() == 1) {
				print ("URI: " + uri + " is the only document being deleted in a call to deleteAll or deleteAllWithPrefix");
				this.commandStack.push(this.modificationCommand(uri)); //store the command for re-creation of the deleted doc
				this.deleteDocumentAndUpdate(uri);
				this.bTree.put(uri, null);			
				return new HashSet<URI>(Arrays.asList(uri));			
			} 

			//If there is more than one document in the set, proceed to delete the individual document. 
			//Add the deletion command to the command set, as well as the URI to the deleted uri set.
			//Make a call to delete from the BTree as well as in the trie and heap.
			print ("Document: " + uri + " is being deleted deleted in a call to deleteAll or deleteAllWithPrefix");
			deletionCommands.addCommand(this.modificationCommand(uri));
			deletedURIs.add(uri);
			this.deleteDocumentAndUpdate(uri);
			this.bTree.put(uri, null);
		}
		this.commandStack.push(deletionCommands);
		return deletedURIs;
	}

	//Delete a document from the data structures used to track documents (trie and heap) as well as updating documentCount and documentBytes.
	//DOES NOT MODIFY THE BTree.
	//URI param.
	private void deleteDocumentAndUpdate (URI uri) {
		//Only attempt to delete from the heap and lower the memory values if the document is NOT on the disk
		if (!this.uriInDisk.contains(uri)) {
			this.deleteFromHeap(uri); //Delete any trace of the document from the heap.
			this.documentCount--; // Decrement the documentCount.
			this.documentBytes -= this.documentBtyeValue(uri); // Decrease documentBytes by the byte representation of the document.
		}
		this.deleteFromTrie(uri);  //Delete any trace of the document from the trie.
		print ("deleted from trie");
	}

	//Document param.
	private void deleteDocumentAndUpdate (Document doc) {
		this.deleteDocumentAndUpdate(doc.getKey());
	}		
			//Delete from trie, URI param.
			private void deleteFromTrie (URI uri) {
				print ("Deleting from trie with URI = " + uri);
				//For every word in the doc, delete the instance of the document from that node in the trie.		
				Document doc = this.bTree.get(uri);
				for (String word : doc.getWords()) {
					if (this.trie.delete(word, doc.getKey()) == null) {
						print ("Warning - deleteFromTrie attempted to delete a value from the trie that was not present! Word = " + word);
					}
				}		
			}

			//Delete from trie, Document param.
			private void deleteFromTrie (Document doc) {
				//For every word in the doc, delete the instance of the document from that node in the trie.		
				for (String word : doc.getWords()) {
					if (this.trie.delete(word, doc.getKey()) == null) {
						print ("Warning - deleteFromTrie attempted to delete a value from the trie that was not present! Word = " + word);
					}
				}	
			}	


			//Delete from minHeap, Document param.
			// private void deleteFromHeap (Document doc) {
			// 	print ("Deleting from min heap");
			// 	//Create a temporary list to store the heap elements that are to be moved aside.
			// 	//Loop trough the heap elements. Remove and check each element. 
			// 	//If it matches the document to be deleted, break (and don't add it to the temp list). If not, add it to the temp list
			// 	//If the loop goes through the end of the heap and the document is not there to be deleted, allow the exception to bubble up from the heap.
			// 	List<Document> heapList = new ArrayList<>();
			// 	while (true) {
			// 		Document tempDoc = bTree.get(this.minHeap.remove());
			// 		if (doc.equals(tempDoc)) {
			// 			break;		
			// 		}
			// 		heapList.add(tempDoc);
			// 	}

			// 	//Reinsert the documents that were moved aside from the minHeap.
			// 	for (Document tempDoc : heapList) {
			// 		this.minHeap.insert(tempDoc.getKey());
			// 	}
			// }

			//Delete from minHeap. URI param. 
			private void deleteFromHeap (URI uri) {

				print ("Deleting from min heap");
				//Create a temporary list to store the heap elements that are to be moved aside.
				//Loop trough the heap elements. Remove and check each element. 
				//If it matches the document to be deleted, break (and don't add it to the temp list). If not, add it to the temp list
				//If the loop goes through the end of the heap and the document is not there to be deleted, allow the exception to bubble up from the heap.
				List<URI> heapList = new ArrayList<>();
				while (true) {
					URI tempURI = this.minHeap.remove();

					print (tempURI);
					print ("Checking if in heap: " + uri + " against URI in heap: " + tempURI);
					if (uri.equals(tempURI)) {
						break;		
					}
					heapList.add(tempURI);
				}

				//Reinsert the documents that were moved aside from the minHeap.
				for (URI tempURI : heapList) {
					print ("Re-adding to min heap: " + tempURI);
					this.minHeap.insert(tempURI);
				}

			}

	
//COMMANDS:
	//Command for document creation (when undone --> deletes)
	private GenericCommand<URI> creationCommand (URI uri) {
		return new GenericCommand<URI>(uri, (URI commandURI) -> {
			print("Command for creation of Document with URI " + commandURI);
			print ("Inital state: " + this.bTree.get(commandURI));

			//When the undo of the command is executed:
			//Delete the created doc from the trie.
			this.deleteFromTrie(commandURI);

			//Delete the created doc from the heap.
			this.deleteFromHeap(commandURI);			

			this.documentCount--; // Decrement the documentCount.
			this.documentBytes -= this.documentBtyeValue(commandURI); // Decrease documentBytes by the byte representation of the document.

			//Delete the created doc from the BTree.
			this.bTree.put(commandURI,null);

			print ("Undone state: " + this.bTree.get(commandURI));

			return true;
		});
	}

	//Command for document deletion or overwriting (when undone --> creates, or returns contents to original state)
	private GenericCommand<URI> modificationCommand (URI uri) {
		print ("Creating modificationCommand");
		Document originalDoc = this.bTree.get(uri);	
		return new GenericCommand<URI>(uri, (URI commandURI) -> { 
			print("Command for modification of Document with URI " + commandURI);
			print ("Inital state: " + this.bTree.get(commandURI) + ". Modifying back to originalDoc: " + originalDoc + "documentCount: " + documentCount + "documentBytes" + documentBytes);

			//When the command is executed:
			//If this is not undoing a deletion (and so the document appears in the BTree) delete the modified version of the doc from the trie and the heap. 
			if (this.bTree.get(commandURI) != null) {
				this.deleteDocumentAndUpdate(commandURI);
				print ("Deleted from trie and heap");
			}
			//Override the modified version of the doc in the BTree with the original version of the doc.
			this.bTree.put(commandURI, originalDoc);

			//Add the original version of the doc to the trie.
			this.addToTrie(originalDoc);
			print ("Added original version of Doc to Trie: " + uri );

			//Update the lastUseTime of the original version of the doc to reflect that a modification has been undone and insert it into the heap.
			originalDoc.setLastUseTime(System.nanoTime());
			print("Inserting into heap and set last use time for Document with URI " + commandURI);			
			minHeap.insert(originalDoc.getKey());

	    	//Update documentCount and documntBytes to reflect the re-added document.
	    	this.documentCount++;
	    	this.documentBytes += this.documentBtyeValue(originalDoc);				

			print ("Undone state: " + this.bTree.get(commandURI) + " documentCount: " + documentCount + " documentBytes" + documentBytes);			

			return true;
		});
	}


//COMPARATORS:
	//Create a comparator for sorting by a keyword
	private Comparator<URI> keywordDocumentComparatorGenerator (String keyword) {
		Comparator<URI> keywordDocumentComparator = (URI uri1, URI uri2) -> bTree.get(uri2).wordCount(keyword) - bTree.get(uri1).wordCount(keyword);
		return keywordDocumentComparator;
	}

	//Create a comparator for sorting by a prefix
	private Comparator<URI> prefixDocumentComparatorGenerator (String prefix) {
		Comparator<URI> prefixDocumentComparator = (URI uri1, URI uri2) -> {
			Set<String> allWords = new HashSet<>();
			allWords.addAll(bTree.get(uri1).getWords());
			allWords.addAll(bTree.get(uri2).getWords());
			int difference = 0;
			print ("Prefix: " + prefix); 
			print (allWords);
			for (String word : allWords) {
				if (word.length() >= prefix.length() && word.substring(0, prefix.length()).equals(prefix)) {
					print ("Doc 1: " + bTree.get(uri1) + " - Word: " + word);
					print ("Doc 2: " + bTree.get(uri2) + " - Word: " + word);					
					difference += (bTree.get(uri2).wordCount(word) - bTree.get(uri1).wordCount(word));
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
		return this.documentBtyeValue(this.bTree.get(uri));
	}

	private void updateMemoryUsage(URI uri) {
    	//Update documentCount and documntBytes to a new document or one moved to memory.
    	this.documentCount++;
    	this.documentBytes += this.documentBtyeValue(uri);
    	if ((this.documentCount > this.maxDocumentCount || this.documentBytes > this.maxDocumentBytes)) {
    		this.documentOverflowMovetoDisk();		
		}
	}

	private String standardizeString (String txt) {
		//Make the string standardized for document processing. Make lowercase and remove non-alphanumeric charecters via regex expr. 
		txt = txt.replaceAll("[^a-zA-Z0-9 ]", "");		
		txt = txt.toLowerCase();
		return txt;
	}		

}