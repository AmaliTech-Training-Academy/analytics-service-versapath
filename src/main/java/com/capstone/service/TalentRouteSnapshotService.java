package com.capstone.service;

import com.capstone.model.TalentRouteSnapshot;
import org.common.event.TalentRouteEvent;

public interface TalentRouteSnapshotService {
    TalentRouteSnapshot processTalentRouteEvent(TalentRouteEvent event);
    TalentRouteSnapshot createTalentRoute(TalentRouteEvent event);
    TalentRouteSnapshot updateTalentRoute(TalentRouteSnapshot existingRoute, TalentRouteEvent event);
}