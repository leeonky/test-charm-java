package org.testcharm.jfactory;

class ConsistencyException extends RuntimeException {
    ConsistencyException(ConsistencyItem<?>.Resolver resolver, boolean composerError, Exception e) {
        super(resolver.buildErrorMessageForProvider(composerError, e.toString()), e);
    }

    ConsistencyException(ConsistencyItem<?>.Resolver resolver, boolean composerError, String string) {
        super(resolver.buildErrorMessageForProvider(composerError, string));
    }
}
