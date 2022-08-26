package edu.yu.cs.com1320.project.stage1.impl;
import edu.yu.cs.com1320.project.stage1.Document;
import java.net.URI;
import java.util.Arrays;

public class DocumentImpl implements Document {
//------------------Debug-------------------//	
	final static boolean DEBUG = false;
	private static void print (Object str) {if (DEBUG) System.out.println(new Throwable().getStackTrace()[1] + ": " + str);}

//------------------Instance Variable(s)-------------------//
	private URI uri;
	private String text;
	private byte[] binaryData;

//------------------Constructor(s)-------------------//
    public DocumentImpl(URI uri, String txt) {
    	if (uri == null || uri.toASCIIString().isBlank() || txt == null || txt.isBlank()) {
    		throw new IllegalArgumentException();
    	}
    	this.uri = uri;
    	this.text = txt;
    	this.binaryData = null;
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
}