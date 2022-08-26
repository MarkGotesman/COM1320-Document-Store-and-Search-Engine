package edu.yu.cs.com1320.project.stage1.impl;

import edu.yu.cs.com1320.project.stage1.DocumentStore;
import edu.yu.cs.com1320.project.stage1.DocumentStore.DocumentFormat;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

public class DocumentStoreImplTest {
	@Test
	void putDocument() throws IOException, URISyntaxException {
		DocumentStore documentStore = new DocumentStoreImpl();
		URI uri = new URI("docs.oracle.com");
		InputStream content = new ByteArrayInputStream("Test".getBytes());

		documentStore.putDocument(content, uri , DocumentFormat.TXT);
		System.out.println(documentStore.getDocument(uri));
	}
}
