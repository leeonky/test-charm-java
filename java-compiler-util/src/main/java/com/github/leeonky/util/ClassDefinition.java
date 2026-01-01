package com.github.leeonky.util;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ClassDefinition extends SimpleJavaFileObject {
    private final String name;
    private final String sourceCode;

    ClassDefinition(String sourceCode) {
        super(URI.create("string:///" + guessClassName(sourceCode).replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        name = guessClassName(sourceCode);
        this.sourceCode = sourceCode;
    }

    public static String guessClassName(String sourceCode) {
        return guessClassNameWithGenericType(sourceCode).replaceAll("<.*>", "");
    }

    private static String guessClassNameWithGenericType(String code) {
        String s = Stream.of(code.split("\n")).filter(l -> l.contains("class") || l.contains("interface"))
                .findFirst().orElse(null);
        Matcher matcher = Pattern.compile(".* class\\s(.*)\\sextends.*", Pattern.DOTALL).matcher(s);
        if (matcher.matches())
            return matcher.group(1).trim();
        matcher = Pattern.compile(".* class\\s(.*)\\simplements.*", Pattern.DOTALL).matcher(s);
        if (matcher.matches())
            return matcher.group(1).trim();
        matcher = Pattern.compile(".* class\\s([^{]*)\\s\\{.*", Pattern.DOTALL).matcher(s);
        if (matcher.matches())
            return matcher.group(1).trim();
        matcher = Pattern.compile(".* interface\\s(.*)\\sextends.*", Pattern.DOTALL).matcher(s);
        if (matcher.matches())
            return matcher.group(1).trim();
        matcher = Pattern.compile(".* interface\\s([^{]*)\\s\\{.*", Pattern.DOTALL).matcher(s);
        if (matcher.matches())
            return matcher.group(1).trim();
        throw new IllegalStateException("Can not guess class name of code:\n" + code);
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return sourceCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ClassDefinition.class, "", name);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ClassDefinition && Objects.equals(name, ((ClassDefinition) o).name);
    }

    public String className() {
        return name;
    }
}
