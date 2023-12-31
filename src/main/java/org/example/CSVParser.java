package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class CSVParser implements SuaferParser{

    /** Вспомогательные методы */
    private String readFromFile(String path){
        String s = "";
        try {s = new String(Files.readAllBytes(Paths.get(path)));
        }catch (IOException e) {
            makeError("Unable to read from file\n");
        }
        return s;
    }
    private void writeToFile(String path, String s){
        try(FileWriter writer = new FileWriter(path, false)) {
            writer.write(s);
            writer.flush();
        }
        catch(IOException ex){
            makeError("Unable to write to file\n");
        }
    }
    private void makeError(String error_text){System.out.println(ERROR_WARNING+"Error:\n" +BLACK_COLOR + ERROR_COlOR+ error_text+DEFAULT_COlOR);}
    private void makeWarning(String error_text){System.out.println(ERROR_WARNING+"Warning:\n" + error_text+DEFAULT_COlOR);}



    /** Методы для превращения экземпляра класса в csv*/
    public void parseObject(Object object, String path){
        if (object instanceof Object[]) {
            pleaseMakeAFileFromTheClassArray((Object[]) object, path);
        } else if (object instanceof Object) {
            pleaseMakeAFileFromTheClass(object, path);
        }
    }

    private void pleaseMakeAFileFromTheClass(Object object, String path){
        if(object==null){
            makeWarning("null is accepted as input -> nothing is written");
            return;
        }
        String s = "";
        Class<?> cls = object.getClass();
        if (cls.isAnnotationPresent(DoParse.class)) {
            for(Field field : cls.getDeclaredFields()){
                field.setAccessible(true);
                s=s+field.getName()+";";
            }
            s=s.substring(0, s.length() - 1);
            s=s+"\n";

            for (Field field : cls.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(object);
                    s+=value+";";
                } catch (IllegalAccessException e) {
                    makeError("Error processing class\n");
                }
            }
            s=s.substring(0, s.length() - 1);
            s=s+"\n";
        }
        writeToFile(path,s);
    }

    private void pleaseMakeAFileFromTheClassArray(Object[] objects, String path){
        if(objects.length==0){
            makeWarning("Empty data for writing is accepted as input -> nothing is written");
            return;
        }
        String s = "";

        List<Class<?>> clsList = new ArrayList<>();
        for(Object o : objects){
            clsList.add(o.getClass());
        }
        if (clsList.get(0).isAnnotationPresent(DoParse.class)) {
            for(Field field : clsList.get(0).getDeclaredFields()){
                field.setAccessible(true);
                s=s+field.getName()+";";
            }
            s=s.substring(0, s.length() - 1);
            s=s+"\n";

            for(int i=0; i<clsList.size();i++){
                for (Field field : clsList.get(i).getDeclaredFields()) {
                    field.setAccessible(true);
                    try {
                        Object value = field.get(objects[i]);
                        s+=value+";";
                    } catch (IllegalAccessException e) {
                        makeError("Error processing class\n");
                    }
                }
                s=s.substring(0, s.length() - 1);
                s=s+"\n";
            }
        }
        writeToFile(path,s);
    }


    /** Методы для превращения csv в экземпляр класса*/
    public <T> List<T> parseFile(String path, Class<T> cls) throws IllegalAccessException, InstantiationException {
        String s = readFromFile(path);
        List<List<Object>> dataList = new ArrayList<>();
        getValueFromString(dataList, s);
        if(dataList.isEmpty()){
            makeError("There are no parameters to create an instance of the class. Returned null");
            return null;
        }

        List<T> instanceList = new ArrayList<>();
        for (List<Object> data : dataList) {
            InstansFactory instansFactory = new InstansFactory();
            T instance = instansFactory.createNewClassInstans(data, cls);
            instanceList.add(instance);
        }

        return instanceList;
    }


    private void getValueFromString(List<List<Object>> data, String s){
        String[] array = s.split("\n");
        if(array.length<2){
            makeError("There are no parameters in the file to create an instance of the class");
            return;
        }
        for(int i=1; i<array.length; i++){
            String[] t = array[i].split(";");
            if(t.length<2){
                makeError("Error in the class definition line, empty field instead of a value");
                return;
            }
            List<Object> test = new ArrayList<>();
            for (String string : t) {
                test.add(getValue(string));
            }
            data.add(test);
        }
    }


    private Object getValue(String string){
        if(itsInt(string)){
            return Integer.parseInt(string);
        }
        if(itsDouble(string)){
            return Double.parseDouble(string);
        }
        return string;
    }

    private boolean itsInt(String s){
        int j=0;
        if(s.charAt(0)=='-'){j++;}
        for(int i=j; i<s.length(); i++){
            if(!(s.charAt(i)>='0' && s.charAt(i)<='9')){return false;}
        }
        return true;
    }
    private boolean itsDouble(String s){
        int point=0;
        int j=0;
        if(s.charAt(0)=='-'){j++;}
        if(j==1 && s.length()>1 && s.charAt(1)=='.'){return false;}
        for(int i=j; i<s.length(); i++){
            if(!(s.charAt(i)>='0' && s.charAt(i)<='9')){
                if(s.charAt(i)=='.'){
                    point++;
                }
                if(s.charAt(i)!='.' || point>1){return false;}
            }
        }
        return true;
    }

    private static final String ERROR_COlOR = "\u001B[41m";
    private static final String ERROR_WARNING = "\u001B[31m";
    private static final String ERROR_t = "\u001B[33m";
    private static final String DEFAULT_COlOR = "\u001B[0m";
    private static final String BLACK_COLOR = "\u001B[30m";
}
