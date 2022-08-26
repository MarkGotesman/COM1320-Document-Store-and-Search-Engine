package edu.yu.cs.com1320.project.stage1.impl;

import edu.yu.cs.com1320.project.stage1.DocumentStore;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage1.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage1.Document;
import edu.yu.cs.com1320.project.HashTable;
import java.util.*;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;




public class DocumentStoreImpl implements DocumentStore {
//------------------Debug-------------------//	
	final static boolean DEBUG = false;
	private static void print (Object str) {if (DEBUG) System.out.println(new Throwable().getStackTrace()[1] + ": " + str);}
	public static void main (String[] args) throws IOException, URISyntaxException {
		DocumentStore documentStore = new DocumentStoreImpl();
		URI uri1 = new URI("edu.yu.cs");
		URI uri2 = new URI("edu.yu.cs.hi");
		URI uri3 = new URI("edu.yu.cs.testuri");
		URI uri4 = new URI("edu.yu.cs.fun");
		URI uri5 = new URI("edu.yu.cs.toplay");
		URI uri6 = new URI("edu.yu.cs");
		URI uri11 = new URI("edu.yu.cs");

		InputStream content1 = new ByteArrayInputStream("Test1".getBytes());
		InputStream content2 = new ByteArrayInputStream("Test2".getBytes());
		InputStream content3 = new ByteArrayInputStream("Test3".getBytes());
		InputStream content4 = new ByteArrayInputStream("Test4".getBytes());
		InputStream content5 = new ByteArrayInputStream("Test5".getBytes());
		InputStream content6 = new ByteArrayInputStream("Test6".getBytes());
		InputStream content11 = new ByteArrayInputStream("Test11".getBytes());
	
}

//------------------Instance Variable(s)-------------------//
	private HashTable <URI, Document> uriToDocument;

//------------------Constructor(s)-------------------//
	public DocumentStoreImpl () {
		this.uriToDocument = new HashTableImpl<>();
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
    	if (input == null) {
    		return uriToDocument.get(uri) == null ? 0 : uriToDocument.put(uri, null).hashCode(); // Return 0 if there is no document to delete, otherwise call .put() with (uri, null) and return the hashcode of the value from .put()
    	}

    	if (uri == null || format == null || uri.toASCIIString().isBlank()) {
    		throw new IllegalArgumentException();
    	}
    	int previousDocReturnValue = uriToDocument.get(uri) == null ? 0 : uriToDocument.get(uri).hashCode();
    	if (format == DocumentFormat.BINARY) {
			uriToDocument.put(uri, new DocumentImpl(uri, input.readAllBytes()));    		 	
    	} else { 
			uriToDocument.put(uri, new DocumentImpl(uri, new String(input.readAllBytes())));    		 	    		
    	}
    	return previousDocReturnValue;
    }
    
    @Override
    public boolean deleteDocument(URI uri) {
    	return (uriToDocument.put(uri,null) == null ? false : true);
    }
}