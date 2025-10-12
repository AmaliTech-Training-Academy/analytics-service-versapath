package com.capstone.service.mapping;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Generic analysis of mapping updates
 * @param <M> The mapping entity type
 */
@Data
@AllArgsConstructor
public class MappingUpdateAnalysis<M> {
    private List<M> toAdd;
    private List<M> toUpdate;
    private int preserved;
}