package com.ten.devs.cards.cards.user.infrastructure.db;

import com.ten.devs.cards.cards.user.domain.UserSpecification;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

class UserSpecificationAdapter {
    
    static Specification<UserEntity> toJpaSpecification(UserSpecification userSpec) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            userSpec.getUsernameContains().ifPresent(username -> 
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("username")), 
                    "%" + username.toLowerCase() + "%"
                ))
            );
            
            userSpec.getEmailContains().ifPresent(email -> 
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")), 
                    "%" + email.toLowerCase() + "%"
                ))
            );
            
            userSpec.getFirstNameContains().ifPresent(firstName -> 
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("firstName")), 
                    "%" + firstName.toLowerCase() + "%"
                ))
            );
            
            userSpec.getLastNameContains().ifPresent(lastName -> 
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("lastName")), 
                    "%" + lastName.toLowerCase() + "%"
                ))
            );
            
            userSpec.getRoles().ifPresent(roles -> {
                if (!roles.isEmpty()) {
                    predicates.add(root.join("roles").in(roles));
                }
            });
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}