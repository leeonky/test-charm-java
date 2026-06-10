package org.testcharm.jfactory;

class ConsistencyProviderException extends RuntimeException {
    ConsistencyProviderException(ConsistencyItem<?> consistencyItem, Exception e) {
        super(consistencyItem.buildErrorMessageForProvider(true), e);
    }
}
