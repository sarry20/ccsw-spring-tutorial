package com.ccsw.tutorial.loan;

import com.ccsw.tutorial.common.criteria.SearchCriteria;
import com.ccsw.tutorial.loan.model.Loan;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;

public class LoanSpecification implements Specification<Loan> {

    private static final long serialVersionUID = 1L;

    private final SearchCriteria criteria;

    public LoanSpecification(SearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<Loan> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        if (criteria.getValue() == null)
            return null;

        Path<?> path = getPath(root);
        String op = criteria.getOperation();

        if (":".equals(op) && path.getJavaType() == String.class) {
            return builder.like(path.as(String.class), "%" + criteria.getValue() + "%");
        }

        if (":".equals(op)) {
            return builder.equal(path, criteria.getValue());
        }

        if ("!:".equals(op) && path.getJavaType() == String.class) {
            return builder.notLike(path.as(String.class), "%" + criteria.getValue() + "%");
        }

        if ("!:".equals(op)) {
            return builder.notEqual(path, criteria.getValue());
        }

        if (">=".equals(op) && Date.class.isAssignableFrom(path.getJavaType())) {
            return builder.greaterThanOrEqualTo(path.as(Date.class), (Date) criteria.getValue());
        }

        if ("<=".equals(op) && Date.class.isAssignableFrom(path.getJavaType())) {
            return builder.lessThanOrEqualTo(path.as(Date.class), (Date) criteria.getValue());
        }

        return null;
    }

    private Path<?> getPath(Root<Loan> root) {
        String key = criteria.getKey();
        String[] split = key.split("[.]", 0);

        Path<?> expression = root.get(split[0]);
        for (int i = 1; i < split.length; i++) {
            expression = expression.get(split[i]);
        }

        return expression;
    }
}
