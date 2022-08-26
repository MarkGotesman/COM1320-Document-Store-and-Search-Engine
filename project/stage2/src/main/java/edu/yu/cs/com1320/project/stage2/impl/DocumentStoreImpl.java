package edu.yu.cs.com1320.project.stage2.impl;

import edu.yu.cs.com1320.project.stage2.DocumentStore;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage2.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage2.Document;
import edu.yu.cs.com1320.project.HashTable;
import java.util.*;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;

import edu.yu.cs.com1320.project.Command;
import java.util.function.Function;

import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.impl.StackImpl;


public class DocumentStoreImpl implements DocumentStore {
//------------------Debug-------------------//	
	final static boolean DEBUG = false;
	private static void print (Object str) {if (DEBUG) System.out.println(new Throwable().getStackTrace()[1] + ": " + str);}

//------------------Instance Variable(s)-------------------//
	private HashTableImpl <URI, Document> uriToDocument;
	private StackImpl <Command> commandStack;

//------------------Constructor(s)-------------------//
	public DocumentStoreImpl () {
		this.uriToDocument = new HashTableImpl<>();
		this.commandStack = new StackImpl<>();
	}

//------------------Getter(s)-------------------//
    @Override
    public Document getDocument(URI uri) {
    	//print ("Document retrieved URI: " + uri + ", content:  " + (uriToDocument.get(uri).getDocumentTxt() == null ? uriToDocument.get(uri).getDocumentBinaryData() : uriToDocument.get(uri).getDocumentTxt())); 
    	return uriToDocument.get(uri);
    }

//------------------Setter(s)-------------------//
    @Override 
    public int putDocument(InputStream input, URI uri, DocumentFormat format) throws IOException {
    	//Delete
    	if (input == null) { 
    		if (uriToDocument.get(uri) == null) {return 0;} // Return 0 if there is no document to delete
			this.commandStack.push(this.modificationCommand(uri)); // otherwise, store the command for re-creation of the deleted doc
			return uriToDocument.put(uri, null).hashCode(); //call .put() with (uri, null) and return the hashcode of the value from .put()
    	}

    	//Check for bad input
    	if (uri == null || format == null || uri.toASCIIString().isBlank()) {throw new IllegalArgumentException();}

    	//Create
    	if (uriToDocument.get(uri) == null) {
    		this.commandStack.push(this.creationCommand(uri));
    		this.insertDocumnet (input, uri, format);
    		return 0;
    	}  

		//Overwrite 	
    	int previousDocReturnValue = uriToDocument.get(uri).hashCode();
    	this.commandStack.push(this.modificationCommand(uri));
    	this.insertDocumnet (input, uri, format);
    	return previousDocReturnValue;
    }
    
    @Override
    public boolean deleteDocument(URI uri) {
		this.commandStack.push(this.modificationCommand(uri)); //store the command for re-creation of the deleted doc    	
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
		Stack<Command> tempStack = new StackImpl<>();
		while(commandStack.peek() != null) {
			Command command = commandStack.pop();
			if (command.getUri() == uri) {
				command.undo();
				undoFlag = true;
				break;
			}
			tempStack.push(command);
		}
		while (tempStack.peek() != null) {
			commandStack.push(tempStack.pop());		
		}
		if (undoFlag == false) {throw new IllegalStateException();} //Throw an ISE if there is no such URI for the undo, i.e. the undo was not performed
	}

//------------------Private-------------------//
	//Insert a document into the hashtable, dependant on format
	private void insertDocumnet (InputStream input, URI uri, DocumentFormat format) throws IOException {
    	if (format == DocumentFormat.BINARY) {
			uriToDocument.put(uri, new DocumentImpl(uri, input.readAllBytes()));    		 	
    	} else { 
			uriToDocument.put(uri, new DocumentImpl(uri, new String(input.readAllBytes())));    		 	    		
    	}		
	}

	//Command for document creation
	private Command creationCommand (URI uri) {
		return new Command(uri, (URI commandURI) -> {
			print("Command for creation of Document with URI " + commandURI);
			print ("Inital state: " + this.getDocument(commandURI));
			this.uriToDocument.put(commandURI,null);
			print ("Undone state: " + this.getDocument(commandURI));
			return true;
		});
	}

	//Command for document deletion or overwriting
	private Command modificationCommand (URI uri) {
		Document doc = this.getDocument(uri);	
		return new Command(uri, (URI commandURI) -> {
			print("Command for modification of Document with URI " + commandURI);
			print ("Inital state: " + this.getDocument(commandURI));			
			uriToDocument.put(commandURI, doc);
			print ("Undone state: " + this.getDocument(commandURI));			
			return true;
		});
	}

}