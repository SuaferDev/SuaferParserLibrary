package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface ToJSON {
}

public class JSONParser {

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
    private void makeError(String error_text){System.out.println(ERROR_COlOR+"Error:\n" + error_text+DEFAULT_COlOR);}



    /** Методы для превращения экземпляра класса в json*/
    public void objectToJson(Object object, String path){
        String s = "{"+"\n";
        Class<?> cls = object.getClass();
        if (cls.isAnnotationPresent(ToJSON.class)) {
            for (Field field : cls.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(object);
                    s+=write(field, value);
                } catch (IllegalAccessException e) {
                    makeError("Error processing class\n");
                }
            }
        }
        s=s.substring(0, s.length() - 2);
        s=s+"\n"+"}";
        writeToFile(path,s);
    }
    private String write(Field field, Object value){
        String name = value.getClass().getSimpleName();
        if(name.length()>1 && name.charAt(name.length()-2)=='[' && name.charAt(name.length()-1)==']'){
            return writeArray(field, value);
        }
        return writeUsualParameter(field, value);
    }
    private String writeUsualParameter(Field field, Object value){
        String s = "";
        if(field.getType().getSimpleName().equals("String")){
            s=s+"   "+"\""+field.getName()+"\""+" : "+"\""+String.valueOf(value)+"\""+","+"\n";
        }else{
            s=s+"   "+"\""+field.getName()+"\""+" : "+String.valueOf(value)+","+"\n";
        }
        return s;
    }

    private String writeArray(Field field, Object value){
        if(value.getClass().getSimpleName().equals("int[]")){return writeIntArray(field, value);}

        if(value.getClass().getSimpleName().equals("double[]")){return writeDoubleArray(field, value);}

        if(value.getClass().getSimpleName().equals("boolean[]")){return writeBooleanArray(field, value);}

        if(value.getClass().getSimpleName().equals("Object[]")){return writeObjectArray(field, value);}

        return writeStringArray(field, value);

    }

    private String writeIntArray(Field field, Object value){
        String s ="   "+"\""+field.getName()+"\""+" : ["+"\n";
        int[] array = (int[]) value;
        for(int i : array){
            s = s + "        " + i + ",\n";
        }
        s=s.substring(0, s.length() - 2);
        s=s+"\n"+"    ],"+"\n";
        return s;
    }

    private String writeDoubleArray(Field field, Object value){
        String s ="   "+"\""+field.getName()+"\""+" : ["+"\n";
        double[] array = (double[]) value;
        for(double i : array){
            s = s + "        " + i + ",\n";
        }
        s=s.substring(0, s.length() - 2);
        s=s+"\n"+"    ],"+"\n";
        return s;
    }
    private String writeBooleanArray(Field field, Object value){
        String s ="   "+"\""+field.getName()+"\""+" : ["+"\n";
        boolean[] array = (boolean[]) value;
        for(boolean i : array){
            s = s + "        " + i + ",\n";
        }
        s=s.substring(0, s.length() - 2);
        s=s+"\n"+"    ],"+"\n";
        return s;
    }

    private String writeObjectArray(Field field, Object value){
        String s ="   "+"\""+field.getName()+"\""+" : ["+"\n";
        Object[] array = (Object[]) value;
        for(Object i : array){
            s = s + "        " + i + ",\n";
        }
        s=s.substring(0, s.length() - 2);
        s=s+"\n"+"    ],"+"\n";
        return s;
    }

    private String writeStringArray(Field field, Object value){
        String s ="   "+"\""+field.getName()+"\""+" : ["+"\n";
        String[] array = (String[]) value;
        for(String i : array){
            s = s + "        \"" + i + "\",\n";
        }
        s=s.substring(0, s.length() - 2);
        s=s+"\n"+"    ],"+"\n";
        return s;
    }



    /** Методы для превращения json в экземпляр класса*/
    public <T> T jsonToObject(String path, Class<T> cls) throws IllegalAccessException, InstantiationException {
        String s = readFromFile(path);
        List<Parameter> data = new ArrayList<>();
        getParameterFromList(data, s);

        return createNewClassInstans(data, cls);
    }

    private void getParameterFromList(List<Parameter> data, String s){
        String[] array = s.split("\n");
        for(int i=0; i<array.length; i++)System.out.println(i+" "+ array[i]);
        int i=0;
        while(i<array.length){
            if(array[i].trim().length()>2) {
                String stroke = array[i].trim();
                if (getStringType(stroke) == 1) {
                    data.add(getLastUsualParameter(stroke.trim()));
                    System.out.println(getLastUsualParameter(stroke).getName() + " "+ getLastUsualParameter(stroke).getValue().getClass().getSimpleName());
                    i++;
                } else {
                    if (getStringType(stroke) == 2) {
                        data.add(getUsualParameter(stroke));
                        System.out.println(getUsualParameter(stroke).getName() + " "+ getUsualParameter(stroke).getValue());
                        i++;
                    } else {
                        if (getStringType(stroke) == 3) {
                            String name = getOnlyName(stroke);
                            List<Object> list = new ArrayList<>();
                            int j = i + 1;
                            while (j< array.length) {
                                list.add(defineType(getValue(array[j])));
                                j++;
                                if(array[j].trim().charAt(0)==']'){
                                    break;
                                }
                            }
                            data.add(new Parameter(name, defineArrayType(listToArray(list))));
                            i=j;
                        }
                    }
                }
            }else {
                i++;
            }
        }
    }

    public <T> T createNewClassInstans(List<Parameter> data, Class<T> clazz) throws IllegalAccessException, InstantiationException {
        T instance = clazz.newInstance();

        Field[] fields = clazz.getDeclaredFields();

        for (int i = 0; i < fields.length && i < data.size(); i++) {
            Field field = fields[i];

            field.setAccessible(true);
            field.set(instance, data.get(i).getValue());
        }

        return instance;
    }

    private Object[] listToArray(List<Object> list){
        Object[] array = new Object[list.size()];
        for(int i=0; i<list.size();i++){
            array[i] = list.get(i);
        }
        return array;
    }

    private int getStringType(String s){
        if(s.charAt(s.length()-1)=='['){
            return 3;
        }
        if(s.charAt(s.length()-1)==','){
            return 2;
        }
        return 1;
    }

    private String getValue(String string){
        string = string.trim();
        if(string.charAt(string.length()-1)==','){
            return  string.substring(0, string.length() - 1);
        }
        return string;
    }

    private Parameter getUsualParameter(String s){
        /*Описание
            Получить из строки типа:
            "название" : значение,
            Название и значение, после чего записать их в Parameter
         */
        int nameStartIndex = s.indexOf("\"") + 1;
        int nameEndIndex = s.indexOf("\"", nameStartIndex);

        int valueStartIndex = s.indexOf(":") + 1;
        int valueEndIndex = s.indexOf(",", valueStartIndex);

        String name = s.substring(nameStartIndex, nameEndIndex);
        String value = s.substring(valueStartIndex, valueEndIndex);


        name = name.trim();
        value = value.trim();
        return new Parameter(name, defineType(value.trim()));
    }

    private Parameter getLastUsualParameter(String s){
        /*Описание
            Получить из строки типа:
            "название" : значение
            Название и значение, после чего записать их в Parameter
         */
        int nameStartIndex = s.indexOf("\"") + 1;
        int nameEndIndex = s.indexOf("\"", nameStartIndex);

        int valueStartIndex = s.indexOf(":") + 1;

        String name = s.substring(nameStartIndex, nameEndIndex);
        String value = s.substring(valueStartIndex);

        return new Parameter(name, defineType(value.trim()));
    }

    private String getOnlyName(String s){
        /*Описание
            Получить из строки типа:
            "название" :
            Название, после чего вернуть его
         */
        int c=0;
        String name="";

        for(int i=0; i<s.length(); i++){
            if(s.charAt(i)=='"'){
                c++;
            }else{
                name=name+s.charAt(i);
            }
            if(c==2){
                return name;
            }
        }
        return name;
    }

    private class Parameter{

        private final String name;
        private final Object value;

        public Parameter(String name, Object value){
            this.name = name;
            this.value = value;
        }

        public String getName() {return name;}

        public Object getValue() {return value;}
    }

    private Object defineArrayType(Object[] o){
        if(o.length!=0){
            if(o[0].getClass().getSimpleName().equals("int")){
                int[] array = new int[o.length];
                for(int i=0; i<o.length; i++){
                    array[i] = (int) o[i];;
                }
                return array;
            }
            if(o[0].getClass().getSimpleName().equals("double")){
                double[] array = new double[o.length];
                for(int i=0; i<o.length; i++){
                    array[i] = (double) o[i];
                }
                return array;
            }
            if(o[0].getClass().getSimpleName().equals("String")){
                String[] array = new String[o.length];
                for(int i=0; i<o.length; i++){
                    array[i] = (String) o[i];
                }
                return array;
            }
            if(o[0].getClass().getSimpleName().equals("boolean")){
                boolean[] array = new boolean[o.length];
                for(int i=0; i<o.length; i++){
                    array[i] = (boolean) o[i];
                }
                return array;
            }
        }
        return null;
    }

    private Object defineType(String s){
        if(itsInt(s)){return Integer.parseInt(s);}

        if(itsBoolean(s)){return Boolean.parseBoolean(s);}

        if(itsDouble(s)){return Double.parseDouble(s);}

        return String.valueOf(s);
    }
    private boolean itsInt(String s){
        int j=0;
        if(s.charAt(0)=='-'){j++;}
        for(int i=j; i<s.length(); i++){
            if(!(s.charAt(i)>='0' && s.charAt(i)<='9')){return false;}
        }
        return true;
    }
    private boolean itsBoolean(String s){
        return (Objects.equals(s, "true") || Objects.equals(s, "false"));
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

    private static final String ERROR_COlOR = "\u001B[31m";
    private static final String DEFAULT_COlOR = "\u001B[0m";

}

