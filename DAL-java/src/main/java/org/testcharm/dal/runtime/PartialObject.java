package org.testcharm.dal.runtime;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface PartialObject {
    default Optional<String> findExpectedField(Set<?> fields, Object prefix, Object postfix) {
        if (postfix instanceof String) {
            List<String> matched = fields.stream().filter(String.class::isInstance).map(Object::toString)
                    .filter(field -> predicate(field, buildField(prefix, postfix)))
                    .collect(Collectors.toList());
            if (matched.size() > 1)
                throw new AmbiguousFieldException(matched, getClass());
            return matched.stream().findFirst();
        }
        return Optional.empty();
    }

    default boolean predicate(String candidate, String field) {
        return candidate.equalsIgnoreCase(field);
    }

    default String buildField(Object prefix, Object postfix) {
        return String.format("%s%s", prefix, postfix);
    }
}