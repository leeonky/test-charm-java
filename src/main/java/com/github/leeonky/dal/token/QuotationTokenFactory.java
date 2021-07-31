package com.github.leeonky.dal.token;

import com.github.leeonky.dal.SyntaxException;

public abstract class QuotationTokenFactory implements TokenFactory {

    private final String errorMessage;
    private final char startChar;

    public QuotationTokenFactory(char startChar, String errorMessage) {
        this.startChar = startChar;
        this.errorMessage = errorMessage;
    }

    @Override
    public Token fetchToken(SourceCode sourceCode, Token previous) {
        if (sourceCode.notEnd() && sourceCode.getChar() == startChar)
            return parseConstValueToken(sourceCode);
        return null;
    }

    private Token parseConstValueToken(SourceCode sourceCode) {
        int startPosition = sourceCode.getPosition();
        TokenParser parser = createParser();
        int codeLength = 0;
        while (sourceCode.notEnd() && parser.feed(sourceCode.takeChar()))
            codeLength++;
        if (!parser.isFinished())
            throw new SyntaxException(startPosition + codeLength, errorMessage);
        return createToken(parser.value());
    }

    protected abstract Token createToken(String value);

    protected abstract TokenParser createParser();
}
