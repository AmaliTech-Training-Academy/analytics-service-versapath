package com.capstone.exception;

import java.util.UUID;

public class DuplicateTalentRouteException extends RuntimeException {
    public DuplicateTalentRouteException(UUID talentRouteId) {
        super("Talent route with ID " + talentRouteId + " already exists");
    }
}