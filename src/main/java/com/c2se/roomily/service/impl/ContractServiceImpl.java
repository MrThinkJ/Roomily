package com.c2se.roomily.service.impl;

import com.c2se.roomily.config.StorageConfig;
import com.c2se.roomily.entity.LandlordInfo;
import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.entity.Room;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.payload.request.LandlordFillContractRequest;
import com.c2se.roomily.payload.request.ModifyContractRequest;
import com.c2se.roomily.payload.request.TenantFillContractRequest;
import com.c2se.roomily.payload.response.ContractResponsibilitiesResponse;
import com.c2se.roomily.payload.response.ContractUserInfoResponse;
import com.c2se.roomily.payload.response.RentedRoomResponse;
import com.c2se.roomily.payload.response.RoomResponse;
import com.c2se.roomily.security.CustomUserDetails;
import com.c2se.roomily.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.internal.util.StringHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractServiceImpl implements ContractService {
    private final RentedRoomService rentedRoomService;
    private final RoomService roomService;
    private final ContractStorageService contractStorageService;
    private final ContractGenerationService contractGenerationService;
    private final LandlordInfoService landlordInfoService;

    @Override
    public void generateDefaultContract(String roomId) {
        Room room = roomService.getRoomEntityById(roomId);
        Document document = contractGenerationService.generateRoomContract(room);
        if (landlordInfoService.existsByUserId(room.getLandlord().getId())) {
            LandlordInfo landlordInfo = landlordInfoService.getLandlordInfoByLandlordId(room.getLandlord().getId());
            fillLandlordInfoFromEntity(document, landlordInfo);
        }
        contractStorageService.saveRoomContract(roomId, document.html());
    }

    @Override
    public ContractResponsibilitiesResponse getContractResponsibilities(String userId, String roomId) {
        Room room = roomService.getRoomEntityById(roomId);
        if (!room.getLandlord().getId().equals(userId)) {
            throw new APIException(HttpStatus.FORBIDDEN, ErrorCode.FLEXIBLE_ERROR,
                                   "You are not the landlord of this room");
        }
        byte[] contractBytes = contractStorageService.getRoomContract(roomId);
        Document document = Jsoup.parse(new String(contractBytes));
        List<String> responsibilitiesA = contractGenerationService.extractResponsibilities(
                document, "responsibilitiesA");
        List<String> responsibilitiesB = contractGenerationService.extractResponsibilities(
                document, "responsibilitiesB");
        List<String> commonResponsibilities = contractGenerationService.extractResponsibilities(
                document, "responsibilitiesCommon");
        return ContractResponsibilitiesResponse.builder()
                .responsibilitiesA(responsibilitiesA)
                .responsibilitiesB(responsibilitiesB)
                .commonResponsibilities(commonResponsibilities)
                .build();
    }

    @Override
    public void modifyContract(ModifyContractRequest request) {
        Room room = roomService.getRoomEntityById(request.getRoomId());
        if (room == null) {
            throw new APIException(HttpStatus.NOT_FOUND, ErrorCode.FLEXIBLE_ERROR, "Room not found");
        }
        Document document = Jsoup.parse(new String(contractStorageService.getRoomContract(request.getRoomId())));
        if (request.getContractDate() != null) {
            document.getElementById("contractDay").html(String.valueOf(request.getContractDate().getDayOfMonth()));
            document.getElementById("contractMonth").html(String.valueOf(request.getContractDate().getMonthValue()));
            document.getElementById("contractYear").html(String.valueOf(request.getContractDate().getYear()));
        }
        if (request.getContractAddress() != null) {
            document.getElementById("contractAddress").html(request.getContractAddress());
        }
        if (request.getRentalAddress() != null) {
            var addressSpan = document.getElementById("rentalAddress");
            if (addressSpan != null) {
                addressSpan.text(request.getRentalAddress());
            } else {
                var labelParagraph = document.getElementsContainingOwnText("Bên A đồng ý cho bên B thuê 01 phòng ở tại địa chỉ:").first();
                if (labelParagraph != null) {
                    String newParagraphHtml = "Bên A đồng ý cho bên B thuê 01 phòng ở tại địa chỉ: " +
                            request.getRentalAddress();
                    labelParagraph.html(newParagraphHtml);
                }
            }
        }
        if (request.getDeposit() != null) {
            document.getElementById("deposit").html(request.getDeposit().toString());
        }
        if (request.getResponsibilitiesA() != null) {
            contractGenerationService.updateResponsibilities(document, request.getResponsibilitiesA(),
                                                             "responsibilitiesA");
        }
        if (request.getResponsibilitiesB() != null) {
            contractGenerationService.updateResponsibilities(document, request.getResponsibilitiesB(),
                                                             "responsibilitiesB");
        }
        if (request.getCommonResponsibilities() != null) {
            contractGenerationService.updateResponsibilities(document, request.getCommonResponsibilities(),
                                                             "responsibilitiesCommon");
        }
        contractStorageService.saveRoomContract(request.getRoomId(), document.html());
    }

    @Override
    public void fillContractByLandlord(LandlordFillContractRequest request) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if (customUserDetails.getAuthorities().stream().noneMatch(auth -> auth.getAuthority().equals("ROLE_LANDLORD"))) {
            throw new APIException(HttpStatus.FORBIDDEN, ErrorCode.FLEXIBLE_ERROR,
                                   "You are not the landlord");
        }
        String landlordId = customUserDetails.getId();
        LandlordInfo landlordInfo = landlordInfoService.getLandlordInfoByLandlordId(landlordId);
        if (landlordInfo == null) {
            landlordInfo = landlordInfoService.saveLandlordInfo(landlordId, request);
        } else {
            landlordInfo = landlordInfoService.updateLandlordInfo(landlordId, request);
        }
        updateAllContractsForLandlord(landlordId, landlordInfo);
    }

    private void updateAllContractsForLandlord(String landlordId, LandlordInfo landlordInfo) {
        try {
            List<RoomResponse> landlordRooms = roomService.getRoomsByLandlordId(landlordId);
            List<CompletableFuture<Void>> roomFutures = landlordRooms.stream()
                    .map(room -> CompletableFuture.runAsync(() -> {
                        try {
                            byte[] contractBytes = contractStorageService.getRoomContract(room.getId());
                            if (contractBytes != null) {
                                Document document = Jsoup.parse(new String(contractBytes));
                                fillLandlordInfoFromEntity(document, landlordInfo);
                                contractStorageService.saveRoomContract(room.getId(), document.html());
                            }
                        } catch (Exception e) {
                            log.error("Error updating contract for room {}: {}", room.getId(), e.getMessage());
                        }
                    }))
                    .toList();
            CompletableFuture.allOf(roomFutures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            throw new APIException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.FLEXIBLE_ERROR,
                                   "Error updating contracts: " + e.getMessage());
        }
    }

    private void fillLandlordInfoFromEntity(Document document, LandlordInfo landlordInfo) {
        document.getElementById("landlordName").html(landlordInfo.getFullName());
        if (landlordInfo.getDateOfBirth() != null) {
            document.getElementById("landlordBirthDate").html(landlordInfo.getDateOfBirth().toString());
        }
        document.getElementById("landlordAddress").html(landlordInfo.getPermanentResidence());
        document.getElementById("landlordID").html(landlordInfo.getIdentityNumber());
        if (landlordInfo.getIdentityProvidedDate() != null) {
            document.getElementById("landlordIDDay").html(
                    String.valueOf(landlordInfo.getIdentityProvidedDate().getDayOfMonth()));
            document.getElementById("landlordIDMonth").html(
                    String.valueOf(landlordInfo.getIdentityProvidedDate().getMonthValue()));
            document.getElementById("landlordIDYear").html(
                    String.valueOf(landlordInfo.getIdentityProvidedDate().getYear()));
        }
        document.getElementById("landlordIDPlace").html(landlordInfo.getIdentityProvidedPlace());
        document.getElementById("landlordPhone").html(landlordInfo.getPhoneNumber());
    }

    @Override
    public void fillContractByTenant(TenantFillContractRequest request) {
        RentedRoom rentedRoom = rentedRoomService.getRentedRoomEntityById(request.getRentedRoomId());
        if (rentedRoom == null) {
            throw new APIException(HttpStatus.NOT_FOUND, ErrorCode.FLEXIBLE_ERROR, "Rented room not found");
        }
        try {
            Document document = Jsoup.parse(
                    new String(contractStorageService.getRentedRoomContract(request.getRentedRoomId())));

            document.getElementById("tenantName").html(request.getTenantFullName());
            document.getElementById("tenantBirthDate").html(request.getTenantDateOfBirth().toString());
            document.getElementById("tenantAddress").html(request.getTenantPermanentResidence());
            document.getElementById("tenantID").html(request.getTenantIdentityNumber());

            if (request.getTenantIdentityProvidedDate() != null) {
                document.getElementById("tenantIDDay").html(
                        String.valueOf(request.getTenantIdentityProvidedDate().getDayOfMonth()));
                document.getElementById("tenantIDMonth").html(
                        String.valueOf(request.getTenantIdentityProvidedDate().getMonthValue()));
                document.getElementById("tenantIDYear").html(
                        String.valueOf(request.getTenantIdentityProvidedDate().getYear()));
            }

            document.getElementById("tenantIDPlace").html(request.getTenantIdentityProvidedPlace());
            document.getElementById("tenantPhone").html(request.getTenantPhoneNumber());

            contractStorageService.saveRentedRoomContract(rentedRoom.getId(), document.html());
        } catch (Exception e) {
            throw new APIException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.FLEXIBLE_ERROR,
                                   "Error filling tenant information: " + e.getMessage());
        }
    }

    @Override
    public byte[] getDefaultContractPdfByRoomId(String roomId) {
        return contractStorageService.getRoomContractPdf(roomId);
    }

    @Override
    public byte[] getContractPdfByRentedRoomId(String rentedRoomId) {
        return contractStorageService.getRentedRoomContractPdf(rentedRoomId);
    }

    @Override
    public String getDefaultContract(String roomId) {
        try {
            byte[] contract = contractStorageService.getRoomContract(roomId);
            return new String(contract);
        } catch (Exception e) {
            generateDefaultContract(roomId);
            return getDefaultContract(roomId);
        }
    }

    @Override
    public ContractUserInfoResponse getContractUserInfo(String userId, String rentedRoomId) {
        RentedRoom rentedRoom = rentedRoomService.getRentedRoomEntityById(rentedRoomId);
        byte[] contractBytes = contractStorageService.getRentedRoomContract(rentedRoomId);
        if (contractBytes == null) {
            throw new APIException(HttpStatus.NOT_FOUND, ErrorCode.FLEXIBLE_ERROR,
                                   "Contract not found for rented room: " + rentedRoomId);
        }
        Document document = Jsoup.parse(new String(contractBytes));
        ContractUserInfoResponse userInfo = null;
        if (userId.equals(rentedRoom.getLandlord().getId())) {
            userInfo = extractLandlordInfo(document);
        } else if (userId.equals(rentedRoom.getUser().getId())) {
            userInfo = extractTenantInfo(document);
        }
        if (userInfo == null) {
            throw new APIException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.FLEXIBLE_ERROR,
                                   "Error when extract data from html");
        }
        return userInfo;
    }

    @Override
    public String getContract(String rentedRoomId) {
        try {
            byte[] contractBytes = contractStorageService.getRentedRoomContract(rentedRoomId);
            return new String(contractBytes);
        } catch (Exception e) {
            throw new APIException(HttpStatus.NOT_FOUND, ErrorCode.FLEXIBLE_ERROR,
                                   "Contract not found for rented room: " + rentedRoomId);
        }
    }

    private ContractUserInfoResponse extractLandlordInfo(Document document) {
        try {
            String fullName = document.getElementById("landlordName").html();
            String birthDateStr = document.getElementById("landlordBirthDate").html();
            String address = document.getElementById("landlordAddress").html();
            String identityNumber = document.getElementById("landlordID").html();
            String idDay = document.getElementById("landlordIDDay").html();
            String idMonth = document.getElementById("landlordIDMonth").html();
            String idYear = document.getElementById("landlordIDYear").html();
            String idPlace = document.getElementById("landlordIDPlace").html();
            String phone = document.getElementById("landlordPhone").html();

            LocalDate birthDate = null;
            if (birthDateStr != null && !birthDateStr.isEmpty()) {
                try {
                    birthDate = LocalDate.parse(birthDateStr);
                } catch (Exception e) {
                }
            }

            LocalDate identityProvidedDate = null;
            if (idDay != null && !idDay.isEmpty() &&
                    idMonth != null && !idMonth.isEmpty() &&
                    idYear != null && !idYear.isEmpty()) {
                try {
                    identityProvidedDate = LocalDate.of(
                            Integer.parseInt(idYear),
                            Integer.parseInt(idMonth),
                            Integer.parseInt(idDay)
                    );
                } catch (Exception e) {
                    throw new APIException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.FLEXIBLE_ERROR,
                                           "Error when extract data from html");
                }
            }

            return ContractUserInfoResponse.builder()
                    .fullName(fullName)
                    .dateOfBirth(birthDate)
                    .permanentResidence(address)
                    .identityNumber(identityNumber)
                    .identityProvidedDate(identityProvidedDate)
                    .identityProvidedPlace(idPlace)
                    .phoneNumber(phone)
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    private ContractUserInfoResponse extractTenantInfo(Document document) {
        try {
            String fullName = document.getElementById("tenantName").html();
            String birthDateStr = document.getElementById("tenantBirthDate").html();
            String address = document.getElementById("tenantAddress").html();
            String identityNumber = document.getElementById("tenantID").html();
            String idDay = document.getElementById("tenantIDDay").html();
            String idMonth = document.getElementById("tenantIDMonth").html();
            String idYear = document.getElementById("tenantIDYear").html();
            String idPlace = document.getElementById("tenantIDPlace").html();
            String phone = document.getElementById("tenantPhone").html();

            LocalDate birthDate = null;
            if (birthDateStr != null && !birthDateStr.isEmpty()) {
                try {
                    birthDate = LocalDate.parse(birthDateStr);
                } catch (Exception e) {
                    throw new APIException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.FLEXIBLE_ERROR,
                                           "Error when extract data from html");
                }
            }

            LocalDate identityProvidedDate = null;
            if (idDay != null && !idDay.isEmpty() &&
                    idMonth != null && !idMonth.isEmpty() &&
                    idYear != null && !idYear.isEmpty()) {
                try {
                    identityProvidedDate = LocalDate.of(
                            Integer.parseInt(idYear),
                            Integer.parseInt(idMonth),
                            Integer.parseInt(idDay)
                    );
                } catch (Exception e) {
                    // Handle date parse errors
                }
            }

            return ContractUserInfoResponse.builder()
                    .fullName(fullName)
                    .dateOfBirth(birthDate)
                    .permanentResidence(address)
                    .identityNumber(identityNumber)
                    .identityProvidedDate(identityProvidedDate)
                    .identityProvidedPlace(idPlace)
                    .phoneNumber(phone)
                    .build();
        } catch (Exception e) {
            return null;
        }
    }
}
