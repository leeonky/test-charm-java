package com.github.leeonky.util;

import lombok.SneakyThrows;

import javax.tools.*;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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
        File location = getLocation();
        location.mkdirs();
        clean(location.toPath());
    }

    private void clean(Path dir) {
        try (Stream<Path> s = Sneaky.get(() -> Files.walk(dir))) {
            s.sorted(Comparator.comparingInt(Path::getNameCount).reversed())
                    .filter(p -> !p.equals(dir))
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (Exception ignored) {
                        }
                    });
        }
    }

    @SneakyThrows
    @Deprecated
    private URLClassLoader getUrlClassLoader() {
        return URLClassLoader.newInstance(new URL[]{new File("").toURI().toURL()});
    }

    @SneakyThrows
    @Deprecated
    public List<Class<?>> compileToClasses(List<String> classCodes) {
        if (classCodes.isEmpty())
            return emptyList();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        List<JavaSourceFromStringLegacy> files = classCodes.stream().map(code ->
                        new JavaSourceFromStringLegacy(ClassDefinition.guessClassName(code).replaceAll("<.*>", ""), declarePackage() + code))
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
    public List<ClassDefinition> compile(Collection<String> classCodes) {
        return Sneaky.get(() -> {
            if (classCodes.isEmpty())
                return emptyList();
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            List<ClassDefinition> definitions = classCodes.stream().map(ClassDefinition::new)
                    .collect(Collectors.toList());
            javax.tools.JavaCompiler systemJavaCompiler = getSystemJavaCompiler();
            StandardJavaFileManager standardFileManager = systemJavaCompiler.getStandardFileManager(diagnostics, null, null);
            standardFileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singletonList(getLocation()));

            List<File> classPath = new ArrayList<>();
            String currentCp = System.getProperty("java.class.path");
            for (String path : currentCp.split(File.pathSeparator))
                classPath.add(new File(path));
            classPath.add(getLocation());
            standardFileManager.setLocation(StandardLocation.CLASS_PATH, classPath);
            if (!systemJavaCompiler.getTask(null, standardFileManager, diagnostics, null, null, definitions).call()) {
                System.out.println(diagnostics.getDiagnostics().stream().filter(d -> d.getSource() != null).collect(groupingBy(Diagnostic::getSource))
                        .entrySet().stream().map(this::compileResults).collect(Collectors.joining("\n")));
                throw new IllegalStateException("Failed to compile java code: \n");
            }
            return definitions;
        });
    }

    public File getLocation() {
        return new File("./" + packageName.replace('.', '/'));
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
        return Class.forName(packagePrefix() + name, true, loader);
    }

    @Deprecated
    public String packagePrefix() {
        return packageName.isEmpty() ? "" : packageName + ".";
    }

    @Deprecated
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
