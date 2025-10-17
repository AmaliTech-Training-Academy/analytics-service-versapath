package com.capstone.service;

import com.capstone.dto.response.*;
import com.capstone.model.ReadinessLevel;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TalentReadinessService {

    PaginatedResponseDto<TalentReadinessDto> getAllTalentReadiness(Pageable pageable);

    PaginatedResponseDto<TalentReadinessDto> searchTalentReadiness(String keyword, Pageable pageable);

    PaginatedResponseDto<TalentReadinessDto> getByTalentRoute(UUID talentRouteId, Pageable pageable);

    PaginatedResponseDto<TalentReadinessDto> getByReadinessLevel(ReadinessLevel readinessLevel, Pageable pageable);

    PaginatedResponseDto<TalentReadinessDto> getByClusters(List<UUID> clusterIds, Pageable pageable);

    List<ClusterDto> getAvailableClusters();

    List<TalentRouteDto> getAvailableTalentRoutes();

    RoleReadinessHeatmapDto getRoleReadinessHeatmap();
}
