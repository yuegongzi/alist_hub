package org.alist.hub.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.function.BiFunction;

public class Condition {
    private final BiFunction<Root<?>, CriteriaBuilder, Predicate> predicateFunction;

    private Condition(BiFunction<Root<?>, CriteriaBuilder, Predicate> predicateFunction) {
        this.predicateFunction = predicateFunction;
    }

    public static Condition of(BiFunction<Root<?>, CriteriaBuilder, Predicate> predicateFunction, boolean condition) {
        if (condition) {
            return new Condition(predicateFunction);
        }
        return null;
    }

    public Predicate toPredicate(Root<?> root, CriteriaBuilder cb) {
        return predicateFunction.apply(root, cb);
    }


}
