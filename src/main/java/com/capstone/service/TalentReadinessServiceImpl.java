package com.capstone.service;

import com.capstone.dto.response.*;
import com.capstone.exception.TalentReadinessException;
import com.capstone.model.*;
import com.capstone.mapper.TalentReadinessMapper;
import com.capstone.repository.*;
import com.capstone.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TalentReadinessServiceImpl implements TalentReadinessService {

    private final LearnerOnboardingRepository learnerOnboardingRepository;
    private final AssessmentSnapshotRepository assessmentSnapshotRepository;
    private final CompleteCapsuleRepository completeCapsuleRepository;
    private final GrowthTrackCapsuleMappingRepository trackCapsuleMappingRepository;
    private final CapsuleClusterMappingRepository capsuleClusterMappingRepository;
    private final TalentReadinessMapper mapper;

    @Override
    public PaginatedResponseDto<TalentReadinessDto> getAllTalentReadiness(Pageable pageable) {
        try {
            log.info("Getting all talent readiness - page: {}", pageable.getPageNumber());

            Page<LearnerOnboarding> onboardings = learnerOnboardingRepository.findAllWithDetails(pageable);

            return buildTalentReadinessResponse(onboardings);

        } catch (Exception e) {
            log.error("Error getting all talent readiness", e);
            throw new TalentReadinessException("Error retrieving talent readiness data", e);
        }
    }

    @Override
    public PaginatedResponseDto<TalentReadinessDto> searchTalentReadiness(String keyword, Pageable pageable) {
        try {
            log.info("Searching talent readiness - keyword: {}, page: {}", keyword, pageable.getPageNumber());

            Page<LearnerOnboarding> onboardings = learnerOnboardingRepository.searchByKeyword(keyword, pageable);

            return buildTalentReadinessResponse(onboardings);

        } catch (Exception e) {
            log.error("Error searching talent readiness", e);
            throw new TalentReadinessException("Error searching talent readiness data", e);
        }
    }

    @Override
    public PaginatedResponseDto<TalentReadinessDto> getByTalentRoute(UUID talentRouteId, Pageable pageable) {
        try {
            log.info("Getting talent readiness by route - talentRouteId: {}, page: {}", talentRouteId, pageable.getPageNumber());

            Page<LearnerOnboarding> onboardings = learnerOnboardingRepository.findByTalentRoute(talentRouteId, pageable);

            return buildTalentReadinessResponse(onboardings);

        } catch (Exception e) {
            log.error("Error getting talent readiness by route", e);
            throw new TalentReadinessException("Error retrieving talent readiness by route", e);
        }
    }

    @Override
    public PaginatedResponseDto<TalentReadinessDto> getByReadinessLevel(ReadinessLevel readinessLevel, Pageable pageable) {
        try {
            log.info("Getting talent readiness by level - readinessLevel: {}, page: {}", readinessLevel, pageable.getPageNumber());

            // Get all learners first, then filter by readiness level
            // Note: This is less efficient but simple. For better performance, use native query with calculated field
            Page<LearnerOnboarding> allOnboardings = learnerOnboardingRepository.findAllWithDetails(pageable);

            Page<TalentReadinessDto> dtoPage = buildTalentReadinessDtoPage(allOnboardings);

            // Filter by readiness level
            List<TalentReadinessDto> filteredList = dtoPage.getContent().stream()
                    .filter(dto -> dto.getReadinessLevel() == readinessLevel)
                    .toList();

            return buildFilteredPaginatedResponse(filteredList, dtoPage);

        } catch (Exception e) {
            log.error("Error getting talent readiness by level", e);
            throw new TalentReadinessException("Error retrieving talent readiness by level", e);
        }
    }

    @Override
    public PaginatedResponseDto<TalentReadinessDto> getByClusters(List<UUID> clusterIds, Pageable pageable) {
        try {
            log.info("Getting talent readiness by clusters - clusterIds: {}, page: {}", clusterIds, pageable.getPageNumber());

            // Step 1: Find learners who completed capsules with these clusters
            // This is WHY we need learnerIds!
            List<UUID> learnerIds = capsuleClusterMappingRepository.findLearnerIdsByClusterIds(clusterIds);

            if (learnerIds.isEmpty()) {
                return buildEmptyPaginatedResponse();
            }

            // Step 2: Get those specific learners
            Page<LearnerOnboarding> onboardings = learnerOnboardingRepository.findByLearnerIds(learnerIds, pageable);

            return buildTalentReadinessResponse(onboardings);

        } catch (Exception e) {
            log.error("Error getting talent readiness by clusters", e);
            throw new TalentReadinessException("Error retrieving talent readiness by clusters", e);
        }
    }

    @Override
    public List<ClusterDto> getAvailableClusters() {
        try {
            log.info("Getting all available clusters");
            List<ClusterSnapshot> clusters = capsuleClusterMappingRepository.findAllUniqueClusters();
            return mapper.toClusterDtoList(clusters);
        } catch (Exception e) {
            log.error("Error getting available clusters", e);
            throw new TalentReadinessException("Failed to retrieve clusters", e);
        }
    }

    @Override
    public List<TalentRouteDto> getAvailableTalentRoutes() {
        try {
            log.info("Getting all available talent routes");
            List<Object[]> routes = learnerOnboardingRepository.getAvailableTalentRoutes();
            return routes.stream()
                    .map(row -> TalentRouteDto.builder()
                            .talentRouteId((UUID) row[0])
                            .routeName((String) row[1])
                            .build())
                    .toList();
        } catch (Exception e) {
            log.error("Error getting available talent routes", e);
            throw new TalentReadinessException("Failed to retrieve talent routes", e);
        }
    }

    @Override
    public RoleReadinessHeatmapDto getRoleReadinessHeatmap() {
        try {
            log.info("Getting role readiness heatmap");

            List<Object[]> aggregates = learnerOnboardingRepository.getHeatmapAggregates();

            if (aggregates.isEmpty()) {
                return RoleReadinessHeatmapDto.builder()
                        .talentRoutes(Collections.emptyList())
                        .build();
            }

            List<RoleReadinessDto> roles = aggregates.stream()
                    .map(this::buildRoleReadinessFromAggregate)
                    .toList();

            return RoleReadinessHeatmapDto.builder()
                    .talentRoutes(roles)
                    .build();

        } catch (Exception e) {
            log.error("Error getting role readiness heatmap", e);
            throw new TalentReadinessException("Failed to retrieve heatmap data", e);
        }
    }

    private PaginatedResponseDto<TalentReadinessDto> buildTalentReadinessResponse(Page<LearnerOnboarding> onboardings) {
        if (onboardings.isEmpty()) {
            return buildEmptyPaginatedResponse();
        }

        Page<TalentReadinessDto> dtoPage = buildTalentReadinessDtoPage(onboardings);
        return PaginationUtil.toPaginatedResponse(dtoPage);
    }

    private Page<TalentReadinessDto> buildTalentReadinessDtoPage(Page<LearnerOnboarding> onboardings) {
        // Batch fetch all data (4 queries total for N learners)
        List<UUID> userIds = onboardings.getContent().stream()
                .map(o -> o.getLearner().getUserId())
                .toList();

        List<UUID> trackIds = onboardings.getContent().stream()
                .map(o -> o.getGrowthTrack().getGrowthTrackId())
                .distinct()
                .toList();

        Map<String, Double> assessmentMap = batchGetAssessmentAverages(userIds, trackIds);
        Map<String, Long> completedMap = batchGetCompletedCounts(userIds, trackIds);
        Map<UUID, Long> totalMap = batchGetTotalCounts(trackIds);
        Map<UUID, List<ClusterDto>> clusterMap = batchGetTrackClusters(trackIds);

        // Build DTOs
        return onboardings.map(o ->
                buildTalentReadinessDto(o, assessmentMap, completedMap, totalMap, clusterMap));
    }

    private Map<String, Double> batchGetAssessmentAverages(List<UUID> userIds, List<UUID> trackIds) {
        try {
            List<Object[]> results = assessmentSnapshotRepository.batchGetAverageScoresByUsersAndTracks(userIds, trackIds);
            return results.stream().collect(Collectors.toMap(
                    r -> (String) r[0],
                    r -> r[1] != null ? ((Number) r[1]).doubleValue() : 0.0
            ));
        } catch (Exception e) {
            log.error("Error batch fetching assessment averages", e);
            return new HashMap<>();
        }
    }

    private Map<String, Long> batchGetCompletedCounts(List<UUID> userIds, List<UUID> trackIds) {
        try {
            List<Object[]> results = completeCapsuleRepository.batchGetCompletedCountsByUsersAndTracks(userIds, trackIds);
            return results.stream().collect(Collectors.toMap(
                    r -> (String) r[0],
                    r -> r[1] != null ? ((Number) r[1]).longValue() : 0L
            ));
        } catch (Exception e) {
            log.error("Error batch fetching completed counts", e);
            return new HashMap<>();
        }
    }

    private Map<UUID, Long> batchGetTotalCounts(List<UUID> trackIds) {
        try {
            List<Object[]> results = trackCapsuleMappingRepository.batchGetCapsuleCountsByTrackIds(trackIds);
            return results.stream().collect(Collectors.toMap(
                    r -> (UUID) r[0],
                    r -> ((Number) r[1]).longValue()
            ));
        } catch (Exception e) {
            log.error("Error batch fetching total counts", e);
            return new HashMap<>();
        }
    }

    private Map<UUID, List<ClusterDto>> batchGetTrackClusters(List<UUID> trackIds) {
        try {
            List<Object[]> results = capsuleClusterMappingRepository.batchGetClustersByTrackIds(trackIds);
            return results.stream().collect(Collectors.groupingBy(
                    r -> (UUID) r[0],
                    Collectors.mapping(r -> mapper.toClusterDto((ClusterSnapshot) r[1]), Collectors.toList())
            ));
        } catch (Exception e) {
            log.error("Error batch fetching track clusters", e);
            return new HashMap<>();
        }
    }

    private TalentReadinessDto buildTalentReadinessDto(
            LearnerOnboarding onboarding,
            Map<String, Double> assessmentMap,
            Map<String, Long> completedMap,
            Map<UUID, Long> totalMap,
            Map<UUID, List<ClusterDto>> clusterMap) {

        UUID userId = onboarding.getLearner().getUserId();
        UUID trackId = onboarding.getGrowthTrack().getGrowthTrackId();
        String compositeKey = userId.toString() + "_" + trackId.toString();

        TalentReadinessDto dto = mapper.toBaseTalentReadinessDto(onboarding);

        Double avgScore = assessmentMap.getOrDefault(compositeKey, 0.0);
        Long completed = completedMap.getOrDefault(compositeKey, 0L);
        Long total = totalMap.getOrDefault(trackId, 0L);

        int skillsCompleted = completed.intValue();
        int skillsTotal = total.intValue();
        double progressPercentage = skillsTotal > 0 ? (skillsCompleted * 100.0 / skillsTotal) : 0.0;

        dto.setAssessmentAverage(avgScore);
        dto.setSkillsCompleted(skillsCompleted);
        dto.setSkillsTotal(skillsTotal);
        dto.setSkillsProgress(skillsCompleted + "/" + skillsTotal);
        dto.setSkillsProgressPercentage(progressPercentage);
        dto.setClusters(clusterMap.getOrDefault(trackId, Collections.emptyList()));
        dto.setReadinessLevel(calculateReadinessLevel(avgScore, progressPercentage));
        dto.setReadinessScore(calculateReadinessScore(avgScore, progressPercentage));

        return dto;
    }

    private RoleReadinessDto buildRoleReadinessFromAggregate(Object[] aggregate) {
        UUID talentRouteId = (UUID) aggregate[0];
        String talentRouteName = (String) aggregate[1];
        Integer learnerCount = ((Number) aggregate[2]).intValue();

        List<Object[]> routeStats = learnerOnboardingRepository.getRouteReadinessStats(talentRouteId);

        double avgScore = 0.0;
        int notReady = 0;
        int medium = 0;
        int high = 0;

        if (!routeStats.isEmpty() && routeStats.getFirst() != null) {
            Object[] stats = routeStats.getFirst();
            avgScore = stats[0] != null ? ((Number) stats[0]).doubleValue() : 0.0;
            notReady = stats[1] != null ? ((Number) stats[1]).intValue() : 0;
            medium = stats[2] != null ? ((Number) stats[2]).intValue() : 0;
            high = stats[3] != null ? ((Number) stats[3]).intValue() : 0;
        }

        return RoleReadinessDto.builder()
                .talentRouteId(talentRouteId)
                .talentRouteName(talentRouteName)
                .learnersCount(learnerCount)
                .averageAssessmentScore(avgScore)
                .readinessBreakdown(ReadinessBreakdownDto.builder()
                        .notReady(notReady)
                        .medium(medium)
                        .high(high)
                        .build())
                .build();
    }

    private ReadinessLevel calculateReadinessLevel(Double avgScore, Double progressPercentage) {
        if (avgScore < 70 || progressPercentage < 50) {
            return ReadinessLevel.NOT_READY;
        }
        if (avgScore >= 85 && progressPercentage >= 80) {
            return ReadinessLevel.HIGH;
        }
        return ReadinessLevel.MEDIUM;
    }

    private Double calculateReadinessScore(Double avgScore, Double progressPercentage) {
        return (avgScore * 0.6) + (progressPercentage * 0.4);
    }

    private PaginatedResponseDto<TalentReadinessDto> buildEmptyPaginatedResponse() {
        return PaginatedResponseDto.<TalentReadinessDto>builder()
                .items(Collections.emptyList())
                .pagination(PaginationMetadata.builder()
                        .page(0)
                        .size(0)
                        .totalElements(0L)
                        .totalPages(0)
                        .hasNext(false)
                        .hasPrevious(false)
                        .build())
                .build();
    }

    private PaginatedResponseDto<TalentReadinessDto> buildFilteredPaginatedResponse(
            List<TalentReadinessDto> filteredList,
            Page<TalentReadinessDto> originalPage) {

        return PaginatedResponseDto.<TalentReadinessDto>builder()
                .items(filteredList)
                .pagination(PaginationMetadata.builder()
                        .page(originalPage.getNumber())
                        .size(filteredList.size())
                        .totalElements(originalPage.getTotalElements())
                        .totalPages(originalPage.getTotalPages())
                        .hasNext(originalPage.hasNext())
                        .hasPrevious(originalPage.hasPrevious())
                        .build())
                .build();
    }
}
