package com.capstone.service;

import com.capstone.model.CompleteCapsule;
import org.common.event.CapsuleCompletionEvent;

import java.util.UUID;

public interface CompleteCapsuleService {
    CompleteCapsule processCapsuleCompletionEvent(CapsuleCompletionEvent event);
    boolean isAlreadyComplete(UUID learnerId, UUID capsuleId);
}
