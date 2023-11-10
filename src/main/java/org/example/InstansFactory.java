package org.example;

import java.lang.reflect.Field;
import java.util.List;

public class InstansFactory {

    public <T> T createNewClassInstans(List<Object> data, Class<T> clazz) throws IllegalAccessException, InstantiationException {
        T instance = clazz.newInstance();

        Field[] fields = clazz.getDeclaredFields();
        if(fields.length!=data.size()){
            return null;
        }

        for (int i = 0; i < fields.length && i < data.size(); i++) {
            Field field = fields[i];

            field.setAccessible(true);
            field.set(instance, getValue(data.get(i)));
        }

        return instance;
    }

    private Object getValue(Object o){
        if(o.getClass().getSimpleName().equals("Integer")){
            int i = (int) o;
            return i;
        }if(o.getClass().getSimpleName().equals("Double")){
            return (double) o;
        }

        return String.valueOf(o);
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
}
