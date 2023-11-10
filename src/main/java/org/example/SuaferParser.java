package org.example;

public interface SuaferParser {

    void parseObject(Object object, String path);

    <T> Object parseFile(String path, Class<T> cls) throws IllegalAccessException, InstantiationException;
}
