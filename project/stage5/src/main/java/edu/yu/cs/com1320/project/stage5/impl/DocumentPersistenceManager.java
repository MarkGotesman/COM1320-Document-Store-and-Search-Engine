package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.*;
import java.io.*;
import java.nio.file.*;

import jakarta.xml.bind.DatatypeConverter;
import com.google.gson.*;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {
//------------------Debug-------------------//  
    final static boolean DEBUG = false;
    private static void print (Object str) {if (DEBUG) System.out.println(" * " + new Throwable().getStackTrace()[1] + ": " + str);}

//------------------Nested Class(es)-------------------//
class DocumentSerializer implements JsonSerializer<DocumentImpl> {
    public JsonElement serialize(DocumentImpl src, Type typeOfSrc, JsonSerializationContext context) {
        //Serialize the relevant components of the Document to the JSON. The correspondingly null value of binaryData for a text document and text for a binary document should be left out due to them being null.
        print ("Serializing  Document. URI: " + src.getKey() + ". Text: " + src.getDocumentTxt() + ". BinaryData: " + src.getDocumentBinaryData() + " . wordCountMap: " + (src.getDocumentBinaryData() == null ? src.getWordMap() : null));
        JsonObject jsonDocument = new JsonObject();

        //Only try to get the word map if the document is a word document, i.e. binaryData == null
        if (src.getDocumentBinaryData() == null) {
            jsonDocument.addProperty("text", src.getDocumentTxt());
            jsonDocument.addProperty("uri", src.getKey().toString());  
           
            //Parsing the wordCountMap is left to the default implementation of GSON. A type token is used to identify the HashMap<String, Integer> class and avoid type erasure.
            Gson gson = new Gson();
            JsonElement jsonMap = gson.toJsonTree(src.getWordMap(), new TypeToken<HashMap<String,Integer>>(){}.getType());
            jsonDocument.add("wordCountMap", jsonMap);  
        } else {
            print ("Byte document serialized binary data: " + DatatypeConverter.printBase64Binary(src.getDocumentBinaryData()));
            jsonDocument.addProperty("binaryData", DatatypeConverter.printBase64Binary(src.getDocumentBinaryData()));
            jsonDocument.addProperty("uri", src.getKey().toString());          
        }

        print ("Properties added to jsonDocument. URI: " + src.getKey() + ". Text: " + src.getDocumentTxt() + ". BinaryData: " + src.getDocumentBinaryData() + " . wordCountMap: " + (src.getDocumentBinaryData() == null ? src.getWordMap() : null));
        print ("Final JSON String: " + jsonDocument.toString());
        return jsonDocument;
    }
}

class DocumentDeserializer implements JsonDeserializer<DocumentImpl> {
    public DocumentImpl deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        try {
            DocumentImpl doc;
            if (jsonObject.has("text")) {
                String text = jsonObject.get("text").getAsString();
                URI uri = new URI(jsonObject.get("uri").getAsString());

                //Parsing the wordCountMap is left to the default implementation of GSON. A type token is used to identify the HashMap<String, Integer> class and avoid type erasure.
                Gson gson = new Gson();
                Map<String, Integer> wordCountMap = gson.fromJson(jsonObject.get("wordCountMap"), new TypeToken<HashMap<String,Integer>>(){}.getType());   

                print ("Deserializing Text Document. URI: " + uri + ". Text: " + text + " . wordCountMap: " + wordCountMap);
                doc = new DocumentImpl (uri, text, wordCountMap); 
            } else {
                print ("Binary from JSON object: "+ jsonObject.get("binaryData").getAsString());
                byte[] binaryData = DatatypeConverter.parseBase64Binary(jsonObject.get("binaryData").getAsString());
                URI uri = new URI(jsonObject.get("uri").getAsString());

                print ("Deserializing Binary Document. URI: " + uri + ". BinaryData: " + binaryData);                
                doc = new DocumentImpl (uri, binaryData);                            
            }
            return doc;
        } catch (URISyntaxException e) {
            throw new JsonParseException("Error parsing the following URI from Json file: " + jsonObject.get("uri").getAsString(), e);
        }
    }
}


//------------------Instance Variable(s)-------------------//
    private File baseDir;
    private GsonBuilder gsonBuilder;
    private Gson gson;

//------------------Constructor(s)-------------------//
    public DocumentPersistenceManager(File baseDir){
        //Set the base directory of the manager to the system working directory (if argument is null), or the given baseDir if not.  
        this.baseDir = (baseDir == null? new File (System.getProperty("user.dir")) : baseDir);
       
        //Create a GsonBuilder, register the (De)serialization for the custom serializers, and make create a gson object. 
        gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DocumentImpl.class, new DocumentSerializer());
        gsonBuilder.registerTypeAdapter(DocumentImpl.class, new DocumentDeserializer());
        gson = gsonBuilder.create();
    }

//------------------Getter(s)-------------------//
    @Override
    public Document deserialize(URI uri) throws IOException {
        String jsonDirectoryAndFileString = baseDir + this.parseDirectoryFromURI(uri) + ".json"; 
        File jsonFile = new File (jsonDirectoryAndFileString);

        print ("Reading jsonString: " + Files.readString(jsonFile.toPath()) + ". From File: " + jsonFile);

        Document doc = gson.fromJson(Files.readString(jsonFile.toPath()), DocumentImpl.class);
        return doc;
    }

//------------------Setter(s)-------------------//
    @Override
    public void serialize(URI uri, Document val) throws IOException {
        String jsonDirectoryAndFileString = baseDir + this.parseDirectoryFromURI(uri) + ".json";
        File jsonFile = new File (jsonDirectoryAndFileString);

        String jsonString = gson.toJson(val);

        print ("Writing jsonString: " + jsonString + ". To File: " + jsonFile);

        //Parse the jsonDirectoryAndFileString to chop off the actual file name so the directories can be created. 
        //Then create the directories, create the file, and write to it. Explicit creation of the file provides redundancy, ensuring that the file is not present beforehand. 
        String jsonDirectoryString = jsonDirectoryAndFileString.substring(0, jsonDirectoryAndFileString.lastIndexOf("/"));
        Files.createDirectories(Paths.get(jsonDirectoryString));
        Files.createFile(jsonFile.toPath());
        Files.writeString(jsonFile.toPath(), jsonString);
    }

    @Override
    public boolean delete(URI uri) throws IOException {
        //File deletion
        String jsonDirectoryAndFileString = baseDir + this.parseDirectoryFromURI(uri) + ".json"; 
        File jsonFile = new File (jsonDirectoryAndFileString);        
        boolean deleted = jsonFile.delete();

        //Directory Deletion
        //Parse the jsonDirectoryAndFileString to chop off the actual file name so the directories can be created. 
        String jsonDirectoryString = jsonDirectoryAndFileString.substring(0, jsonDirectoryAndFileString.lastIndexOf("/"));

        //While we have not reached the baseDir
        while (!jsonDirectoryString.equals(baseDir)) {
            print ("Checking deletion for file path: " + jsonDirectoryString);

            //An ungly way to take the directory string -> path -> file and check that there are no files in that directory
            if (Paths.get(jsonDirectoryString).toFile().listFiles().length == 0) {
                print ("Deleting directory: " + jsonDirectoryString);
                Files.deleteIfExists(Paths.get(jsonDirectoryString));
                //Chop off a "/" to move back another directry folder upwards
                int indexOfNewDirectory = Math.max(jsonDirectoryString.lastIndexOf("/"), jsonDirectoryString.lastIndexOf("\\"));
                jsonDirectoryString = jsonDirectoryString.substring(0, indexOfNewDirectory);
            } else {
                //If got here, there was a file in that directory, break.
                break;
            }
        }

        return deleted;
    }

//------------------Private-------------------//
    private String parseDirectoryFromURI(URI uri) {
        String uriSchemeSpecific = uri.getSchemeSpecificPart();
        while (uriSchemeSpecific.charAt(0) == '/' && uriSchemeSpecific.charAt(1) == '/') {
            uriSchemeSpecific = uriSchemeSpecific.substring(1);
        }
        return uriSchemeSpecific;
    } 
}
