package edu.yu.cs.com1320.project.stage2.impl;

import edu.yu.cs.com1320.project.stage2.DocumentStore;
import edu.yu.cs.com1320.project.stage2.DocumentStore.DocumentFormat;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.*;


public class DocumentStoreImplTest  {
		DocumentStore documentStore;
		URI uri1;
		URI uri2;
		URI uri3;
		URI uri4;
		URI uri5;
		URI uri6;
		URI uri7;
		URI uri8;
		URI uri9;

		InputStream content1;
		InputStream content2;
		InputStream content3;
		InputStream content4;
		InputStream content5;
		InputStream content6;
		InputStream content7;
		InputStream content8;
		InputStream content9;
		
		List<URI> uriList = new ArrayList<>();
		List<InputStream> contentList = new ArrayList<>();

		@BeforeEach
		public void init() throws IOException, URISyntaxException {
		documentStore = new DocumentStoreImpl();
		 uri1 = new URI("docs.oracle.com1");
		 uri2 = new URI("docs.oracle.com2");
		 uri3 = new URI("docs.oracle.com3");
		 uri4 = new URI("docs.oracle.com4");
		 uri5 = new URI("docs.oracle.com5");
		 uri6 = new URI("docs.oracle.com6");
		 uri7 = new URI("docs.oracle.com7");
		 uri8 = new URI("docs.oracle.com8");
		 uri9 = new URI("docs.oracle.com9");

		 content1 = new ByteArrayInputStream("Test1".getBytes());
		 content2 = new ByteArrayInputStream("Test2".getBytes());
		 content3 = new ByteArrayInputStream("Test3".getBytes());
		 content4 = new ByteArrayInputStream("Test4".getBytes());
		 content5 = new ByteArrayInputStream("Test5".getBytes());
		 content6 = new ByteArrayInputStream("Test6".getBytes());
		 content7 = new ByteArrayInputStream("Test7".getBytes());
		 content8 = new ByteArrayInputStream("Test8".getBytes());
		 content9 = new ByteArrayInputStream("Test9".getBytes());

		uriList.add(uri1);
		uriList.add(uri2);
		uriList.add(uri3);
		uriList.add(uri4);
		uriList.add(uri5);
		uriList.add(uri6);
		uriList.add(uri7);
		uriList.add(uri8);
		uriList.add(uri9);

		contentList.add(content1);
		contentList.add(content2);
		contentList.add(content3);
		contentList.add(content4);
		contentList.add(content5);
		contentList.add(content6);
		contentList.add(content7);
		contentList.add(content8);
		contentList.add(content9);

		Iterator<InputStream> it1 = contentList.iterator();
		Iterator<URI> it2 = uriList.iterator();

		while (it1.hasNext() && it2.hasNext()) {
			InputStream nextInputStream = it1.next();
			URI nextURI = it2.next();
			documentStore.putDocument(nextInputStream, nextURI, DocumentFormat.TXT);
		}
	}

	@Test
	void undoLastPutDocument() {
		documentStore.undo();
		assertEquals(null,documentStore.getDocument(uri9)); //I assume that when an undo has been made, the resulting document call will give null, as a delete is putting null in the hashtable. uri9 = last put into table
	}

	@Test
	void undoPutDocumentURI() {		
		documentStore.undo(uri7);
		assertEquals(null,documentStore.getDocument(uri7)); //I assume that when an undo has been made, the resulting document call will give null, as a delete is putting null in the hashtable
	}

	@Test
	void undoLastModifiedDocument () throws IOException, URISyntaxException{
		assertEquals("Test9", documentStore.getDocument(uri9).getDocumentTxt());
		documentStore.putDocument(new ByteArrayInputStream("foo".getBytes()), uri9, DocumentFormat.TXT);
		assertEquals("foo", documentStore.getDocument(uri9).getDocumentTxt());
		documentStore.undo();
		assertEquals("Test9", documentStore.getDocument(uri9).getDocumentTxt());
	}

	@Test
	void undoModifiedDocumentURI () throws IOException, URISyntaxException{
		assertEquals("Test7", documentStore.getDocument(uri7).getDocumentTxt());
		documentStore.putDocument(new ByteArrayInputStream("foo".getBytes()), uri7, DocumentFormat.TXT);

		assertEquals("Test9", documentStore.getDocument(uri9).getDocumentTxt());
		documentStore.putDocument(new ByteArrayInputStream("bar".getBytes()), uri9, DocumentFormat.TXT);

		documentStore.undo(uri7);
		assertEquals("Test7", documentStore.getDocument(uri7).getDocumentTxt());
		assertEquals("bar", documentStore.getDocument(uri9).getDocumentTxt());
	}


	@Test
	void undoLastDeletedDocument () throws IOException, URISyntaxException{
		assertEquals("Test9", documentStore.getDocument(uri9).getDocumentTxt());
		documentStore.putDocument(null, uri9, DocumentFormat.TXT);
		assertEquals(null, documentStore.getDocument(uri9));
		documentStore.undo();
		assertEquals("Test9", documentStore.getDocument(uri9).getDocumentTxt());
	}

	@Test
	void undoDeletedDocumentURI () throws IOException, URISyntaxException{
		assertEquals("Test9", documentStore.getDocument(uri9).getDocumentTxt());
		assertEquals("Test3", documentStore.getDocument(uri3).getDocumentTxt());
		
		documentStore.putDocument(null, uri3, DocumentFormat.TXT);
		documentStore.putDocument(null, uri9, DocumentFormat.TXT);
		assertEquals(null, documentStore.getDocument(uri3));		
		assertEquals(null, documentStore.getDocument(uri9));
		documentStore.undo(uri3);
		assertEquals("Test3", documentStore.getDocument(uri3).getDocumentTxt());
		assertEquals(null, documentStore.getDocument(uri9));		
	}	

	@Test
	void undoDeletedDocumentURIWithDeleteDocument () throws IOException, URISyntaxException{
		assertEquals("Test9", documentStore.getDocument(uri9).getDocumentTxt());
		assertEquals("Test3", documentStore.getDocument(uri3).getDocumentTxt());
		
		documentStore.deleteDocument(uri3);
		documentStore.deleteDocument(uri9);
		assertEquals(null, documentStore.getDocument(uri3));		
		assertEquals(null, documentStore.getDocument(uri9));
		documentStore.undo(uri3);
		assertEquals("Test3", documentStore.getDocument(uri3).getDocumentTxt());
		assertEquals(null, documentStore.getDocument(uri9));		
	}	

	@Test
	void undoEntireStackThrowISE () throws IOException, URISyntaxException {
		for (URI uri : uriList) {
			documentStore.undo();
		}
		assertThrows(IllegalStateException.class, () -> documentStore.undo());
	}
	
	@Test
	void attemptUndoNonExistantURIThrowISE() throws IOException, URISyntaxException {
		assertThrows(IllegalStateException.class, () -> documentStore.undo(new URI ("docs.oracle.com10")));
	}
 
}


