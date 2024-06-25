package com.github.leeonky.cucumber.restful;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringPattern {
    public static String replaceAll(String content, String regex, Function<String, String> replacement) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        StringBuilder stringBuilder = new StringBuilder();
        int pos = 0;
        while (matcher.find()) {
            stringBuilder.append(content, pos, matcher.start());
            stringBuilder.append(replacement.apply(matcher.groupCount() > 0 ? matcher.group(1) : null));
            pos = matcher.end();
        }
        stringBuilder.append(content.substring(pos));
        return stringBuilder.toString();
    }
}
