package com.capstone.service.mapping;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Generic parsed mapping data
 * @param <T> The target entity type
 */
@Data
@AllArgsConstructor
public class ParsedMapping<T> {
    private T entity;
    private Integer sequence; // Optional - null for cluster mappings

    public ParsedMapping(T entity) {
        this(entity, null);
    }
}
