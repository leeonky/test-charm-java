package org.testcharm.cucumber.swarm.util;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Gradle's JaCoCo integration instruments the current JVM only.
 * Some cucumber-swarm tests spawn additional JVMs via {@link ProcessBuilder}.
 *
 * This helper detects the current JVM's JaCoCo -javaagent argument and injects a compatible
 * agent argument into child JVM commands.
 */
public final class JacocoAgentSupport {
    private JacocoAgentSupport() {
    }

    public static void injectJacocoAgentIntoJavaCommand(List<String> javaCommand) {
        if (javaCommand == null || javaCommand.isEmpty()) {
            return;
        }
        // Expect: [java, <jvmArgs...>, -cp, ..., mainClass, ...]
        if (!looksLikeJavaCommand(javaCommand.get(0))) {
            return;
        }

        if (alreadyHasJacocoAgent(javaCommand)) {
            return;
        }

        Optional<String> agentArg = childProcessJacocoAgentArg();
        if (!agentArg.isPresent()) {
            return;
        }

        // Insert right after the java executable.
        javaCommand.add(1, agentArg.get());
    }

    public static String[] injectJacocoAgentIntoJavaCommand(String[] javaCommand) {
        if (javaCommand == null || javaCommand.length == 0) {
            return javaCommand;
        }
        List<String> list = new ArrayList<>();
        for (String s : javaCommand) {
            list.add(s);
        }
        injectJacocoAgentIntoJavaCommand(list);
        return list.toArray(new String[0]);
    }

    private static boolean looksLikeJavaCommand(String executable) {
        if (executable == null) {
            return false;
        }
        String lower = executable.toLowerCase();
        return lower.endsWith("/java") || lower.endsWith("\\java.exe") || lower.equals("java") || lower.endsWith("java.exe");
    }

    private static boolean alreadyHasJacocoAgent(List<String> javaCommand) {
        for (String a : javaCommand) {
            if (a == null) {
                continue;
            }
            if (a.startsWith("-javaagent:") && a.contains("jacoco")) {
                return true;
            }
        }
        return false;
    }

    public static Optional<String> childProcessJacocoAgentArg() {
        return childProcessAgentArg(uniqueSuffix());
    }

    private static Optional<String> childProcessAgentArg(String uniqueSuffix) {
        Optional<JacocoAgentArg> current = currentJacocoAgentArg();
        if (!current.isPresent()) {
            return Optional.empty();
        }
        JacocoAgentArg arg = current.get();

        // If there's no destfile, we can't safely redirect to a predictable place.
        if (arg.destFile == null || arg.destFile.trim().isEmpty()) {
            return Optional.empty();
        }

        String childDestFile = arg.destFile + "." + uniqueSuffix;
        StringBuilder opts = new StringBuilder();
        opts.append("destfile=").append(childDestFile);
        opts.append(",append=true");

        // Preserve other agent options (output, includes, excludes, etc.) except destfile/append.
        for (String extra : arg.extraOptions) {
            if (extra == null) {
                continue;
            }
            String k = extra.trim();
            if (k.isEmpty()) {
                continue;
            }
            if (k.startsWith("destfile=") || k.startsWith("append=")) {
                continue;
            }
            opts.append(',').append(k);
        }

        return Optional.of("-javaagent:" + arg.agentJar + "=" + opts);
    }

    private static Optional<JacocoAgentArg> currentJacocoAgentArg() {
        for (String a : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (a == null) {
                continue;
            }
            if (!a.startsWith("-javaagent:")) {
                continue;
            }
            // Gradle uses .../jacocoagent.jar ; be lenient.
            if (!a.contains("jacoco")) {
                continue;
            }

            String rest = a.substring("-javaagent:".length());
            String jar;
            String optString = null;
            int eq = rest.indexOf('=');
            if (eq >= 0) {
                jar = rest.substring(0, eq);
                optString = rest.substring(eq + 1);
            } else {
                jar = rest;
            }

            JacocoAgentArg parsed = new JacocoAgentArg(jar);
            if (optString != null && !optString.trim().isEmpty()) {
                for (String opt : optString.split(",")) {
                    if (opt == null || opt.trim().isEmpty()) {
                        continue;
                    }
                    String o = opt.trim();
                    if (o.startsWith("destfile=")) {
                        parsed.destFile = o.substring("destfile=".length());
                    } else if (o.startsWith("append=")) {
                        // ignored (we force append=true for child processes)
                    } else {
                        parsed.extraOptions.add(o);
                    }
                }
            }

            if (parsed.agentJar != null && !parsed.agentJar.trim().isEmpty()) {
                return Optional.of(parsed);
            }
        }
        return Optional.empty();
    }

    private static String uniqueSuffix() {
        return Long.toString(System.nanoTime()) + "-" + UNIQUE_SEQ.next();
    }

    private static final UniqueSeq UNIQUE_SEQ = new UniqueSeq();

    private static final class UniqueSeq {
        private int n;

        private synchronized int next() {
            return ++n;
        }
    }

    private static final class JacocoAgentArg {
        final String agentJar;
        String destFile;
        final List<String> extraOptions = new ArrayList<>();

        private JacocoAgentArg(String agentJar) {
            this.agentJar = agentJar;
        }
    }
}
