package org.testcharm.util;

import java.util.Objects;

public class Suppressor {
    /**
     * Asserts that the actual value matches the expected value for the last/fallback case.
     * <p>
     * This method is used at the end of an if-else if chain to validate that when all
     * previous conditions fail, the remaining value is the one we expect.
     * <p>
     * If the assertion fails, it indicates either:
     * <ul>
     *   <li>A new case type was added but not handled in the if-else chain, OR</li>
     *   <li>The fallback assumption is incorrect</li>
     * </ul>
     * <p>
     * This allows comprehensive unit test coverage for branches that cannot be tested
     * through normal code paths (like the final else when all primitives are covered).
     *
     * @param actual the actual value encountered
     * @param expect the expected value for this fallback case
     * @throws IllegalStateException if actual does not equal expect, indicating
     *                               the if-else chain may be incomplete or a new case needs handling
     */
    public static void assertLastEqualsCase(Object actual, Object expect) {
        if (!Objects.equals(actual, expect))
            throw new IllegalStateException(
                    "Unexpected case in fallback branch: expected <" + expect + "> but got <" + actual + "> - "
                            + "this may indicate a new case type was added but not handled in the condition chain");
    }

    public static <T> T getIgnoring(ThrowingSupplier<T> supplier, T defaultValue) {
        try {
            return supplier.get();
        } catch (Throwable e) {
            return defaultValue;
        }
    }

    public static void runIgnoring(ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            // ignore
        }
    }
}
