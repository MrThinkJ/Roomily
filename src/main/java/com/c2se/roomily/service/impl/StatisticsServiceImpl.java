package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.*;
import com.c2se.roomily.enums.BillStatus;
import com.c2se.roomily.enums.RentedRoomStatus;
import com.c2se.roomily.payload.response.LandlordStatisticsResponse;
import com.c2se.roomily.payload.response.TenantStatisticsResponse;
import com.c2se.roomily.repository.*;
import com.c2se.roomily.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {
    private final RentedRoomRepository rentedRoomRepository;
    private final BillLogRepository billLogRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final LandlordStatisticsRepository landlordStatisticsRepository;
    private final TenantStatisticsRepository tenantStatisticsRepository;

    @Override
    public Double getTenantSuccessRentedRate(String userId) {
        long totalRentedRooms = rentedRoomRepository.countByUserId(userId);
        if (totalRentedRooms == 0) {
            return 0.0;
        }
        
        long failedRentals = rentedRoomRepository.countByUserIdAndStatus(userId, RentedRoomStatus.CANCELLED);
        return (double) (totalRentedRooms - failedRentals) / totalRentedRooms;
    }

    @Override
    public Double getTenantDebtRate(String userId) {
        List<String> rentedRoomIds = rentedRoomRepository.findByUserId(userId)
                .stream()
                .map(RentedRoom::getId)
                .collect(Collectors.toList());
        if (rentedRoomIds.isEmpty()) {
            return 0.0;
        }
        long totalBills = billLogRepository.countByRentedRoomIdIn(rentedRoomIds);
        if (totalBills == 0) {
            return 0.0;
        }
        long lateBills = billLogRepository.countByRentedRoomIdInAndBillStatusIn(
                rentedRoomIds, 
                List.of(BillStatus.LATE, BillStatus.LATE_PAID, BillStatus.UNPAID)
        );
        return (double) lateBills / totalBills;
    }

    @Override
    public Double getLandlordResponseRate(String landlordId) {
        long totalChatRooms = chatRoomRepository.countTotalChatRoomsByLandlord(landlordId);
        if (totalChatRooms == 0) {
            return 0.0;
        }
        long chatRoomsResponded = chatRoomRepository.countChatRoomsRespondedByLandlord(landlordId);
        return (double) chatRoomsResponded / totalChatRooms;
    }

    @Override
    public TenantStatisticsResponse getTenantStatistics(String userId) {
        Optional<TenantStatistics> cachedStats = tenantStatisticsRepository.findByTenantId(userId);
        if (cachedStats.isPresent()) {
            TenantStatistics stats = cachedStats.get();
            return TenantStatisticsResponse.builder()
                    .successRentedRate(stats.getSuccessRentedRate())
                    .debtRate(stats.getDebtRate())
                    .totalRentedRooms(stats.getTotalRentedRooms())
                    .totalSuccessRented(stats.getTotalSuccessRented())
                    .totalLatePayments(stats.getTotalLatePayments())
                    .build();
        }

        Double successRate = getTenantSuccessRentedRate(userId);
        Double debtRate = getTenantDebtRate(userId);
        int totalRentedRooms = rentedRoomRepository.countByUserId(userId);
        int totalSuccessRented = rentedRoomRepository.countByUserIdAndStatusNotIn(userId, Collections.singletonList(
                RentedRoomStatus.CANCELLED));
        
        List<String> rentedRoomIds = rentedRoomRepository.findByUserId(userId)
                .stream()
                .map(RentedRoom::getId)
                .collect(Collectors.toList());
        
        int totalLatePayments = 0;
        if (!rentedRoomIds.isEmpty()) {
            totalLatePayments = billLogRepository.countByRentedRoomIdInAndBillStatusIn(
                    rentedRoomIds,
                    List.of(BillStatus.LATE, BillStatus.LATE_PAID, BillStatus.UNPAID)
            );
        }
        
        return TenantStatisticsResponse.builder()
                .successRentedRate(successRate)
                .debtRate(debtRate)
                .totalRentedRooms(totalRentedRooms)
                .totalSuccessRented(totalSuccessRented)
                .totalLatePayments(totalLatePayments)
                .build();
    }

    @Override
    public LandlordStatisticsResponse getLandlordStatistics(String landlordId) {
        Optional<LandlordStatistics> cachedStats = landlordStatisticsRepository.findByLandlordId(landlordId);
        if (cachedStats.isPresent()) {
            LandlordStatistics stats = cachedStats.get();
            return LandlordStatisticsResponse.builder()
                    .responseRate(stats.getResponseRate())
                    .totalChatRooms(stats.getTotalChatRooms())
                    .respondedChatRooms(stats.getRespondedChatRooms())
                    .averageResponseTimeMinutes(stats.getAverageResponseTimeMinutes())
                    .totalRentedRooms(stats.getTotalRentedRooms())
                    .build();
        }

        Double responseRate = getLandlordResponseRate(landlordId);

        long totalChatRooms = chatRoomRepository.countTotalChatRoomsByLandlord(landlordId);
        long respondedChatRooms = chatRoomRepository.countChatRoomsRespondedByLandlord(landlordId);

        double averageResponseTime = calculateAverageLandlordResponseTime(landlordId);
        int totalRentedRooms = rentedRoomRepository.countByLandlordId(landlordId);

        return LandlordStatisticsResponse.builder()
                .responseRate(responseRate)
                .totalChatRooms(totalChatRooms)
                .respondedChatRooms(respondedChatRooms)
                .averageResponseTimeMinutes((long)averageResponseTime)
                .totalRentedRooms(totalRentedRooms)
                .build();
    }

    private Double calculateAverageResponseTime(List<ChatMessage> chatMessages) {
        if (chatMessages.isEmpty()) {
            return 0.0;
        }
        if (chatMessages.size() == 1) {
            return 0.0;
        }
        LocalDateTime lastMessageTime = null;
        boolean isTenantMessage = false;
        int responseCount = 0;
        Duration totalResponseTime = Duration.ZERO;
        for (ChatMessage chatMessage : chatMessages){
            if (chatMessage.getRoleName() == null) {
                continue;
            }
            if (chatMessage.getRoleName().equals("ROLE_TENANT")) {
                isTenantMessage = true;
                lastMessageTime = chatMessage.getCreatedAt();
            } else if (chatMessage.getRoleName().equals("ROLE_LANDLORD") && isTenantMessage) {
                if (lastMessageTime != null) {
                    Duration responseTime = Duration.between(lastMessageTime, chatMessage.getCreatedAt());
                    totalResponseTime = totalResponseTime.plus(responseTime);
                    responseCount++;
                }
                isTenantMessage = false;
            }
        }
        if (responseCount == 0) {
            return 0.0;
        }
        return (double) totalResponseTime.toMinutes() / responseCount;
    }

    private double calculateAverageLandlordResponseTime(String landlordId){
        List<ChatRoom> chatRooms = chatRoomRepository.findByManagerId(landlordId);
        double totalResponseTime = 0d;
        int chatRoomCount = 0;
        for (ChatRoom chatRoom : chatRooms){
            List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomId(
                    chatRoom.getId(),
                    LocalDateTime.now().minusMonths(1)
            );
            if (chatMessages.isEmpty()) {
                continue;
            }
            if (chatMessages.size() == 1) {
                continue;
            }
            totalResponseTime += calculateAverageResponseTime(chatMessages);
            chatRoomCount++;
        }
        if (chatRoomCount == 0) {
            return 0.0;
        }
        return totalResponseTime / chatRoomCount;
    }

    @Scheduled(cron = "0 0 0 * * ?") // Run at midnight every day
    @Transactional(rollbackFor = Exception.class)
    public void updateAllStatistics() {
        updateLandlordStatistics();
        updateTenantStatistics();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateLandlordStatistics() {
        log.info("Starting scheduled update of landlord statistics");

        List<User> landlords = userRepository.findAllLandlords();

        for (User landlord : landlords) {
            try {
                Double responseRate = getLandlordResponseRate(landlord.getId());

                long totalChatRooms = chatRoomRepository.countTotalChatRoomsByLandlord(landlord.getId());
                long respondedChatRooms = chatRoomRepository.countChatRoomsRespondedByLandlord(landlord.getId());

                int totalRentedRooms = rentedRoomRepository.countByLandlordId(landlord.getId());

                LandlordStatistics stats = landlordStatisticsRepository.findByLandlordId(landlord.getId())
                        .orElse(new LandlordStatistics());

                stats.setLandlordId(landlord.getId());
                stats.setResponseRate(responseRate);
                stats.setTotalChatRooms(totalChatRooms);
                stats.setRespondedChatRooms(respondedChatRooms);
                stats.setAverageResponseTimeMinutes((long) calculateAverageLandlordResponseTime(landlord.getId()));
                stats.setTotalRentedRooms(totalRentedRooms);
                landlordStatisticsRepository.save(stats);
                log.info("Updated statistics for landlord: {}", landlord.getId());
            } catch (Exception e) {
                log.error("Error updating statistics for landlord: {}", landlord.getId(), e);
            }
        }
        log.info("Completed scheduled update of landlord statistics");
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateTenantStatistics() {
        log.info("Starting scheduled update of tenant statistics");

        List<User> tenants = userRepository.findAllTenants();

        for (User tenant : tenants) {
            try {
                Double successRate = getTenantSuccessRentedRate(tenant.getId());
                Double debtRate = getTenantDebtRate(tenant.getId());
                int totalRentedRooms = rentedRoomRepository.countByUserId(tenant.getId());
                int totalSuccessRented = rentedRoomRepository.countByUserIdAndStatus(tenant.getId(), RentedRoomStatus.IN_USE);
                
                List<String> rentedRoomIds = rentedRoomRepository.findByUserId(tenant.getId())
                        .stream()
                        .map(RentedRoom::getId)
                        .collect(Collectors.toList());
                
                int totalLatePayments = 0;
                if (!rentedRoomIds.isEmpty()) {
                    totalLatePayments = billLogRepository.countByRentedRoomIdInAndBillStatusIn(
                            rentedRoomIds,
                            List.of(BillStatus.LATE, BillStatus.LATE_PAID, BillStatus.UNPAID)
                    );
                }

                TenantStatistics stats = tenantStatisticsRepository.findByTenantId(tenant.getId())
                        .orElse(new TenantStatistics());

                stats.setTenantId(tenant.getId());
                stats.setSuccessRentedRate(successRate);
                stats.setDebtRate(debtRate);
                stats.setTotalRentedRooms(totalRentedRooms);
                stats.setTotalSuccessRented(totalSuccessRented);
                stats.setTotalLatePayments(totalLatePayments);
                tenantStatisticsRepository.save(stats);
                log.info("Updated statistics for tenant: {}", tenant.getId());
            } catch (Exception e) {
                log.error("Error updating statistics for tenant: {}", tenant.getId(), e);
            }
        }
        log.info("Completed scheduled update of tenant statistics");
    }
}