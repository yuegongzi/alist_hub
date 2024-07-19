package org.alist.hub.repository;

import org.springframework.data.domain.ExampleMatcher;

public class ExampleMatcherBuilder<T> {
    private ExampleMatcher matcher = ExampleMatcher.matching();

    public static <T> ExampleMatcherBuilder<T> create() {
        return new ExampleMatcherBuilder<>();
    }

    public interface MatcherConfigurer {
        void configure(ExampleMatcher.GenericPropertyMatcher match);
    }

    public ExampleMatcherBuilder<T> withContains(String property, Object value) {
        return with(property, value, ExampleMatcher.GenericPropertyMatcher::contains);
    }

    public ExampleMatcherBuilder<T> with(String property, Object value, MatcherConfigurer configurer) {
        if (isValidValue(value)) {
            matcher = matcher.withMatcher(property, configurer::configure);
        }
        return this;
    }

    private boolean isValidValue(Object value) {
        if (value instanceof String) {
            return !((String) value).isEmpty();
        }
        return value != null;
    }

    public ExampleMatcher build() {
        return matcher;
    }
}



