package com.github.leeonky.util;

import lombok.SneakyThrows;

import javax.tools.*;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static javax.tools.ToolProvider.getSystemJavaCompiler;

public class JavaCompiler {
    @Deprecated
    private final URLClassLoader loader = getUrlClassLoader();
    private final String packageName;
    private final int id;

    public JavaCompiler(String packageName, int id) {
        this.packageName = packageName + id;
        this.id = id;
    }

    @SneakyThrows
    private URLClassLoader getUrlClassLoader() {
        return URLClassLoader.newInstance(new URL[]{new File("").toURI().toURL()});
    }

    public static String guessClassName(String code) {
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

    @SneakyThrows
    @Deprecated
    public List<Class<?>> compileToClasses(List<String> classCodes) {
        if (classCodes.isEmpty())
            return emptyList();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        List<JavaSourceFromStringLegacy> files = classCodes.stream().map(code ->
                        new JavaSourceFromStringLegacy(guessClassName(code).replaceAll("<.*>", ""), declarePackage() + code))
                .collect(Collectors.toList());
        javax.tools.JavaCompiler systemJavaCompiler = getSystemJavaCompiler();
        StandardJavaFileManager standardFileManager = systemJavaCompiler.getStandardFileManager(diagnostics, null, null);
        standardFileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singletonList(new File("./")));
        if (!systemJavaCompiler.getTask(null, standardFileManager, diagnostics, null, null, files).call()) {
            System.out.println(diagnostics.getDiagnostics().stream().filter(d -> d.getSource() != null).collect(groupingBy(Diagnostic::getSource))
                    .entrySet().stream().map(this::compileResults).collect(Collectors.joining("\n")));
            throw new IllegalStateException("Failed to compile java code: \n");
        }
        return files.stream().map(f -> f.name).map(this::loadClass).collect(Collectors.toList());
    }

    @SneakyThrows
    public List<Definition> compile(Collection<String> classCodes) {
        return Sneaky.get(() -> {
            if (classCodes.isEmpty())
                return emptyList();
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            List<Definition> definitions = classCodes.stream().map(code -> new Definition(packageName, guessClassName(code).replaceAll("<.*>", ""), code))
                    .collect(Collectors.toList());
            javax.tools.JavaCompiler systemJavaCompiler = getSystemJavaCompiler();
            StandardJavaFileManager standardFileManager = systemJavaCompiler.getStandardFileManager(diagnostics, null, null);
            standardFileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singletonList(new File("./")));
            if (!systemJavaCompiler.getTask(null, standardFileManager, diagnostics, null, null, definitions).call()) {
                System.out.println(diagnostics.getDiagnostics().stream().filter(d -> d.getSource() != null).collect(groupingBy(Diagnostic::getSource))
                        .entrySet().stream().map(this::compileResults).collect(Collectors.joining("\n")));
                throw new IllegalStateException("Failed to compile java code: \n");
            }
            return definitions;
        });
    }

    @Deprecated
    private String declarePackage() {
        return packageName.isEmpty() ? "" : "package " + packageName + ";";
    }

    @SneakyThrows
    private String compileResults(Map.Entry<? extends JavaFileObject, List<Diagnostic<? extends JavaFileObject>>> e) {
        String sourceCode = String.valueOf(e.getKey().getCharContent(true));
        Object[] codeBase = sourceCode.chars().mapToObj(c -> c == '\n' ? (char) c : ' ').map(String::valueOf).toArray();
        List<String> result = new ArrayList<>();
        result.add(e.getKey().toString());
        for (Diagnostic<?> diagnostic : e.getValue()) {
            result.add(diagnostic.getMessage(null));
            if (diagnostic.getPosition() >= 0 && diagnostic.getPosition() < codeBase.length)
                codeBase[(int) diagnostic.getPosition()] = '^';
        }
        String[] codes = sourceCode.split("\n");
        String[] codeMarks = Stream.of(codeBase).map(String::valueOf).collect(Collectors.joining()).split("\n");
        for (int i = 0; i < codes.length; i++) {
            result.add(codes[i]);
            if (i < codeMarks.length && !codeMarks[i].trim().isEmpty())
                result.add(codeMarks[i]);
        }
        return String.join("\n", result);
    }

    @SneakyThrows
    @Deprecated
    public Class<?> loadClass(String name) {
        return Class.forName(fullName(name), true, loader);
    }

    public String fullName(String name) {
        return packagePrefix() + name;
    }

    public String packagePrefix() {
        return packageName.isEmpty() ? "" : packageName + ".";
    }

    public int getId() {
        return id;
    }
}

@Deprecated
class JavaSourceFromStringLegacy extends SimpleJavaFileObject {
    final String name;
    final String code;

    JavaSourceFromStringLegacy(String name, String code) {
        super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.name = name;
        this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return code;
    }
}
