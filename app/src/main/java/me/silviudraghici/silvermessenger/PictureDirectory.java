package me.silviudraghici.silvermessenger;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by Silviu on 2016-01-12.
 */
public class PictureDirectory {

    class KeyNamePair{
        String key;
        String name;
        KeyNamePair(String key, String name){
            this.key = key;
            this.name = name;
        }

        public String toCSV(){
            return new String(key + "," + name + "\n");
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof KeyNamePair){
                if(((KeyNamePair)obj).key.equals(key)){
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "KeyNamePair{" +
                    "key='" + key + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
    private LinkedList<KeyNamePair> pictureList;
    private Context context;
    private String folderName;
    private File dir;
    private File manifestFile;

    private static PictureDirectory instance = null;
    public static PictureDirectory getInstance(Context context, String folderName){
        if (instance == null || !folderName.equals(instance.folderName)){
            instance = new PictureDirectory(context, folderName);
        }
        return instance;
    }


    private PictureDirectory(Context context, String folderName){
        pictureList = new LinkedList<>();
        this.context = context;
        this.folderName = folderName;

        read_manifest();
    }

    private void read_manifest(){
        //create folder for current conversations pictures
        dir = new File(context.getFilesDir(), folderName);
        dir.mkdir();
        manifestFile = new File(dir, "manifest");
        BufferedReader manifestIn = null;
        try {
            manifestIn = new BufferedReader(new FileReader(manifestFile));
            String line;
            while ((line = manifestIn.readLine()) != null) {
                String[] keyValuePair = line.split(",");
                pictureList.add(new KeyNamePair(keyValuePair[0], keyValuePair[1]));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (manifestIn != null) {
                try {
                    manifestIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String addPicture(String pictureName){
        int nextKeyNum = pictureList.size();
        while(pictureList.contains(new KeyNamePair(String.valueOf(nextKeyNum), null))){
            nextKeyNum++;
        }
        KeyNamePair newPair = new KeyNamePair(String.valueOf(nextKeyNum), pictureName);
        pictureList.add(new KeyNamePair(String.valueOf(nextKeyNum), pictureName));
        boolean saved = saveList();
        if(!saved){
            pictureList.remove(newPair);
        }
        return String.valueOf(nextKeyNum);
    }

    public void removePicture(String key){
        int index = pictureList.indexOf(new KeyNamePair(key, null));
        KeyNamePair delPair = pictureList.remove(index);
        boolean saved = saveList();
        if(!saved){
            pictureList.add(index, delPair);
        }
    }

    private boolean saveList(){
        FileWriter manifestOut = null;
        try {
            manifestOut = new FileWriter(manifestFile);
            for (KeyNamePair namePair : pictureList) {
                manifestOut.write(namePair.toCSV());
            }
            manifestOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (manifestOut != null) {
                try {
                    manifestOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    public int getIndex(String key){
        return pictureList.indexOf(new KeyNamePair(key, null));
    }

    public String getPath(String key){
        int index = getIndex(key);
        return  getPathByIndex(index);
    }

    public String getPathByIndex(int index) {
        return  folderName + "/" + pictureList.get(index).name;
    }

    public int size() {
        return pictureList.size();
    }

    @Override
    public String toString() {
        return "PictureDirectory {" +
                "folderName = '" + folderName + '\'' +
                ", pictureList = " + pictureList +
                '}';
    }
}
