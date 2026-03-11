package org.testcharm.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextCursor {
    private String content;
    private final String allContent;

    public TextCursor(String content) {
        this.content = content.trim();
        allContent = content;
    }

    public Optional<String[]> popGroup(Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.lookingAt()) {
            content = content.substring(matcher.end()).trim();
            String[] items = new String[matcher.groupCount() + 1];
            for (int i = 0; i <= matcher.groupCount(); i++)
                items[i] = matcher.group(i);
            return Optional.of(items);
        }
        return Optional.empty();
    }

    public Optional<String> pop(Pattern pattern) {
        return popGroup(pattern).map(items -> items[0]);
    }

    public Optional<String> pop1(Pattern pattern) {
        return popGroup(pattern).map(items -> items[1]);
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

    public String leftContent() {
        return content;
    }

    public String content() {
        return allContent;
    }
}
