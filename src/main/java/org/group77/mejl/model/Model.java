package org.group77.mejl.model;

import javax.mail.*;
import java.io.*;
import java.util.Properties;
import java.util.Scanner;

// THIS CLASS WILL BE SPLIT INTO MULTIPLE SINGLE RESPONSIBILITY CLASSES ONCE
// SOME THINGS ARE FIGURED OUT
public class Model {
    /**
     * Stores esp information serialized in data directory
     *
     * @param accountInformation an object with required data for connecting to remote ESP
     */
    public void writeESP(AccountInformation accountInformation) {
        try {
            String path = getESPsDir() + accountInformation.getIdentifier();
            createFile(path);
            writeTo(accountInformation, path);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String getESPsDir(){
       return getDataDir() + "esp.d\\";
    }
    private String getDataDir(){
        return "C:\\Users\\Martin\\AppData\\Local\\grupp77\\mejl\\";
    }

    /**
     * @param path to the file which contains ESP data
     * @return a new deserialized ESP object
     */
    public AccountInformation readESP(String path) {
        try {
            AccountInformation accountInformation = readFrom(path);
            return accountInformation;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void createFile(String path) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
    }

    private <T> void writeTo(T o, String path) throws IOException {
        FileOutputStream file = new FileOutputStream(path);
        ObjectOutputStream out = new ObjectOutputStream(file);
        out.writeObject(o);
        out.close();
        file.close();
    }


    private <T> T readFrom(String path) throws IOException, ClassNotFoundException {
        FileInputStream file = new FileInputStream(path);
        ObjectInputStream in = new ObjectInputStream(file);
        T o = (T) in.readObject();
        in.close();
        file.close();
        return o;
    }

    /**
     * sets the active ESP by writing to the file active_esp
     * @param identifier target
     * @implNote not using symlink because Windows require elevated permission
     */
    public void setAcitiveESP(String identifier){
        String path = getDataDir() + "active_esp";
        File file = new File(path);
        try{
            if(!file.exists()){
                createFile(path);
            }
            FileWriter writer = new FileWriter(path);
            if(!file.canWrite()){
                file.setWritable(true);
            }
            writer.write(identifier);
            writer.flush();
            writer.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * reads the active ESP which is indicated in the file active_esp
     * @return the in use ESP
     */
    public AccountInformation getActiveESP(){
        String s = getDataDir() + "active_esp";
        File file = new File(s);
        try{
            if(!file.exists()){
                createFile(s);
            }
            Scanner scanner = new Scanner(file);
            String identifier = scanner.nextLine();
            return readFrom(getESPsDir() + identifier);
        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }

       return null;
    }

    private Folder[] getFolders(Store store) throws MessagingException {
        return store.getDefaultFolder().list("*");
    }

    private Folder createFolder(Store store, String identifier) throws MessagingException {
       Folder folder = store.getFolder(identifier);
       if (!folder.exists()){
           folder.create(Folder.HOLDS_FOLDERS);
       }
       return folder;
    }

    // IN DEVELOPMENT
    public Message[] getMessages(Folder folder) throws MessagingException {
        folder.open(Folder.READ_ONLY);
        Message[] messages = folder.getMessages();
        return messages;
    }


}



