package com.capstone.controller;

import com.capstone.dto.response.*;
import com.capstone.model.ReadinessLevel;
import com.capstone.service.TalentReadinessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics/talent-readiness")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Talent Readiness Analytics", description = "APIs for talent readiness assessment and reporting")
public class TalentReadinessController {

    private final TalentReadinessService talentReadinessService;

    @GetMapping
    @Operation(
            summary = "Get all talent readiness (no filters)",
            description = "Retrieve paginated list of all learners with their readiness metrics"
    )
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<TalentReadinessDto>>> getAllTalentReadiness(
            @PageableDefault(sort = "learner.username") Pageable pageable) {

        log.info("GET /api/v1/analytics/talent-readiness - All learners, page: {}", pageable.getPageNumber());

        PaginatedResponseDto<TalentReadinessDto> response = talentReadinessService.getAllTalentReadiness(pageable);

        return ResponseEntity.ok(ApiResponseDto.success(response, "Talent readiness list retrieved successfully"));
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search talent readiness by keyword",
            description = "Search learners by username, email, or talent route name"
    )
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<TalentReadinessDto>>> searchTalentReadiness(
            @Parameter(description = "Search keyword for username, email, or route name")
            @RequestParam String keyword,
            @PageableDefault(sort = "learner.username") Pageable pageable) {

        log.info("GET /api/v1/analytics/talent-readiness/search - keyword: {}, page: {}", keyword, pageable.getPageNumber());

        PaginatedResponseDto<TalentReadinessDto> response = talentReadinessService.searchTalentReadiness(keyword, pageable);

        return ResponseEntity.ok(ApiResponseDto.success(response, "Search results retrieved successfully"));
    }

    @GetMapping("/by-talent-route/{talentRouteId}")
    @Operation(
            summary = "Filter by talent route",
            description = "Get learners enrolled in a specific talent route"
    )
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<TalentReadinessDto>>> getByTalentRoute(
            @Parameter(description = "Talent route ID")
            @PathVariable UUID talentRouteId,
            @PageableDefault(sort = "learner.username") Pageable pageable) {

        log.info("GET /api/v1/analytics/talent-readiness/by-talent-route/{} - page: {}", talentRouteId, pageable.getPageNumber());

        PaginatedResponseDto<TalentReadinessDto> response = talentReadinessService.getByTalentRoute(talentRouteId, pageable);

        return ResponseEntity.ok(ApiResponseDto.success(response, "Talent readiness by route retrieved successfully"));
    }

    @GetMapping("/by-readiness-level/{readinessLevel}")
    @Operation(
            summary = "Filter by readiness level",
            description = "Get learners with specific readiness level (NOT_READY, MEDIUM, HIGH)"
    )
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<TalentReadinessDto>>> getByReadinessLevel(
            @Parameter(description = "Readiness level: NOT_READY, MEDIUM, or HIGH")
            @PathVariable ReadinessLevel readinessLevel,
            @PageableDefault(sort = "learner.username") Pageable pageable) {

        log.info("GET /api/v1/analytics/talent-readiness/by-readiness-level/{} - page: {}", readinessLevel, pageable.getPageNumber());

        PaginatedResponseDto<TalentReadinessDto> response = talentReadinessService.getByReadinessLevel(readinessLevel, pageable);

        return ResponseEntity.ok(ApiResponseDto.success(response, "Talent readiness by level retrieved successfully"));
    }

    @PostMapping("/by-clusters")
    @Operation(
            summary = "Filter by clusters/skills",
            description = "Get learners who have completed capsules tagged with specified clusters (skills)"
    )
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<TalentReadinessDto>>> getByClusters(
            @Parameter(description = "Cluster filter request with cluster IDs")
            @RequestBody ClusterFilterRequest request,
            @PageableDefault(sort = "learner.username") Pageable pageable) {

        log.info("POST /api/v1/analytics/talent-readiness/by-clusters - clusterIds: {}, page: {}",
                request.getClusterIds(), pageable.getPageNumber());

        PaginatedResponseDto<TalentReadinessDto> response = talentReadinessService.getByClusters(
                request.getClusterIds(), pageable);

        return ResponseEntity.ok(ApiResponseDto.success(response, "Talent readiness by clusters retrieved successfully"));
    }

    @GetMapping("/clusters")
    @Operation(
            summary = "Get available clusters",
            description = "Retrieve all unique clusters/skills available in the system for filtering purposes"
    )
    public ResponseEntity<ApiResponseDto<List<ClusterDto>>> getAvailableClusters() {
        log.info("GET /api/v1/analytics/talent-readiness/clusters");

        List<ClusterDto> clusters = talentReadinessService.getAvailableClusters();

        return ResponseEntity.ok(ApiResponseDto.success(clusters, "Available clusters retrieved successfully"));
    }

    @GetMapping("/talent-routes")
    @Operation(
            summary = "Get available talent routes",
            description = "Retrieve all talent routes that have enrolled learners for dropdown population"
    )
    public ResponseEntity<ApiResponseDto<List<TalentRouteDto>>> getAvailableTalentRoutes() {
        log.info("GET /api/v1/analytics/talent-readiness/talent-routes");

        List<TalentRouteDto> routes = talentReadinessService.getAvailableTalentRoutes();

        return ResponseEntity.ok(ApiResponseDto.success(routes, "Available talent routes retrieved successfully"));
    }

    @GetMapping("/heatmap")
    @Operation(
            summary = "Get role readiness heatmap",
            description = "Retrieve aggregated readiness statistics grouped by talent routes for heatmap visualization"
    )
    public ResponseEntity<ApiResponseDto<RoleReadinessHeatmapDto>> getRoleReadinessHeatmap() {
        log.info("GET /api/v1/analytics/talent-readiness/heatmap");

        RoleReadinessHeatmapDto heatmap = talentReadinessService.getRoleReadinessHeatmap();

        return ResponseEntity.ok(ApiResponseDto.success(heatmap, "Role readiness heatmap retrieved successfully"));
    }
}
