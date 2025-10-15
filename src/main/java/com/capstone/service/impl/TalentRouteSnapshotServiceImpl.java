package com.capstone.service.impl;

import com.capstone.exception.DuplicateTalentRouteException;
import com.capstone.exception.TalentRouteProcessingException;
import com.capstone.mapper.TalentRouteEventMapper;
import com.capstone.model.TalentRouteSnapshot;
import com.capstone.repository.TalentRouteSnapshotRepository;
import com.capstone.service.TalentRouteSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.TalentRouteEvent;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TalentRouteSnapshotServiceImpl implements TalentRouteSnapshotService {

    private final TalentRouteSnapshotRepository talentRouteSnapshotRepository;
    private final TalentRouteEventMapper talentRouteEventMapper;

    @Override
    public TalentRouteSnapshot processTalentRouteEvent(TalentRouteEvent event) {
        log.info("Processing talent route event for routeId: {}", event.getId());

        try {
            Optional<TalentRouteSnapshot> existingRoute = talentRouteSnapshotRepository.findByTalentRouteId(event.getId());

            if (existingRoute.isPresent()) {
                log.info("Route exists, updating route with ID: {}", event.getId());
                return updateTalentRoute(existingRoute.get(), event);
            } else {
                log.info("Route does not exist, creating new route with ID: {}", event.getId());
                return createTalentRoute(event);
            }

        } catch (Exception e) {
            log.error("Error processing talent route event for routeId: {}", event.getId(), e);
            throw new TalentRouteProcessingException("Failed to process talent route event", e);
        }
    }

    @Override
    public TalentRouteSnapshot createTalentRoute(TalentRouteEvent event) {
        log.debug("Creating new talent route from event: {}", event);

        try {
            // Create basic route entity
            TalentRouteSnapshot newRoute = talentRouteEventMapper.toTalentRouteSnapshot(event);
            TalentRouteSnapshot savedRoute = talentRouteSnapshotRepository.save(newRoute);
            log.info("Successfully created talent route with ID: {}", savedRoute.getTalentRouteId());

            return savedRoute;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when creating route with ID: {}", event.getId(), e);
            throw new DuplicateTalentRouteException(event.getId());
        } catch (Exception e) {
            log.error("Unexpected error creating route with ID: {}", event.getId(), e);
            throw new TalentRouteProcessingException("Failed to create talent route", e);
        }
    }

    @Override
    public TalentRouteSnapshot updateTalentRoute(TalentRouteSnapshot existingRoute, TalentRouteEvent event) {
        log.debug("Updating existing route {} with event data", existingRoute.getTalentRouteId());

        try {
            // Update basic route fields
            talentRouteEventMapper.updateTalentRouteSnapshot(event, existingRoute);
            TalentRouteSnapshot updatedRoute = talentRouteSnapshotRepository.save(existingRoute);

            log.info("Successfully updated talent route with ID: {}", updatedRoute.getTalentRouteId());
            return updatedRoute;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when updating route with ID: {}", existingRoute.getTalentRouteId(), e);
            throw new TalentRouteProcessingException("Route update failed due to data constraint violation", e);
        } catch (Exception e) {
            log.error("Unexpected error updating route with ID: {}", existingRoute.getTalentRouteId(), e);
            throw new TalentRouteProcessingException("Failed to update talent route", e);
        }
    }
}