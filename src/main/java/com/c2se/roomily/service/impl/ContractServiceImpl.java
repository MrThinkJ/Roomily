package com.c2se.roomily.service.impl;

import com.c2se.roomily.config.StorageConfig;
import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.entity.Room;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.payload.request.LandlordFillContractRequest;
import com.c2se.roomily.payload.request.ModifyContractRequest;
import com.c2se.roomily.payload.request.TenantFillContractRequest;
import com.c2se.roomily.payload.response.ContractResponsibilitiesResponse;
import com.c2se.roomily.payload.response.ContractUserInfoResponse;
import com.c2se.roomily.service.*;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
    private final RentedRoomService rentedRoomService;
    private final RoomService roomService;
    private final ContractStorageService contractStorageService;
    private final ContractGenerationService contractGenerationService;

    @Override
    public void generateDefaultContract(String roomId) {
        Room room = roomService.getRoomEntityById(roomId);
        Document document = contractGenerationService.generateDefaultContract(room);
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
            document.getElementById("contractDay").val(String.valueOf(request.getContractDate().getDayOfMonth()));
            document.getElementById("contractMonth").val(String.valueOf(request.getContractDate().getMonthValue()));
            document.getElementById("contractYear").val(String.valueOf(request.getContractDate().getYear()));
        }
        if (request.getContractAddress() != null) {
            document.getElementById("contractAddress").val(request.getContractAddress());
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
            document.getElementById("deposit").val(request.getDeposit().toString());
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
        RentedRoom rentedRoom = rentedRoomService.getRentedRoomEntityById(request.getRentedRoomId());
        if (rentedRoom == null) {
            throw new APIException(HttpStatus.NOT_FOUND, ErrorCode.FLEXIBLE_ERROR, "Rented room not found");
        }
        Document document = Jsoup.parse(
                new String(contractStorageService.getRentedRoomContract(request.getRentedRoomId())));
        document.getElementById("landlordName").val(request.getLandlordFullName());
        document.getElementById("landlordBirthDate").val(request.getLandlordDateOfBirth().toString());
        document.getElementById("landlordAddress").val(request.getLandlordPermanentResidence());
        document.getElementById("landlordID").val(request.getLandlordIdentityNumber());
        if (request.getLandlordIdentityProvidedDate() != null) {
            document.getElementById("landlordIDDay").val(
                    String.valueOf(request.getLandlordIdentityProvidedDate().getDayOfMonth()));
            document.getElementById("landlordIDMonth").val(
                    String.valueOf(request.getLandlordIdentityProvidedDate().getMonthValue()));
            document.getElementById("landlordIDYear").val(
                    String.valueOf(request.getLandlordIdentityProvidedDate().getYear()));
        }
        document.getElementById("landlordIDPlace").val(request.getLandlordIdentityProvidedPlace());
        document.getElementById("landlordPhone").val(request.getLandlordPhoneNumber());
        contractStorageService.saveRentedRoomContract(rentedRoom.getId(), document.html());
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

            document.getElementById("tenantName").val(request.getTenantFullName());
            document.getElementById("tenantBirthDate").val(request.getTenantDateOfBirth().toString());
            document.getElementById("tenantAddress").val(request.getTenantPermanentResidence());
            document.getElementById("tenantID").val(request.getTenantIdentityNumber());

            if (request.getTenantIdentityProvidedDate() != null) {
                document.getElementById("tenantIDDay").val(
                        String.valueOf(request.getTenantIdentityProvidedDate().getDayOfMonth()));
                document.getElementById("tenantIDMonth").val(
                        String.valueOf(request.getTenantIdentityProvidedDate().getMonthValue()));
                document.getElementById("tenantIDYear").val(
                        String.valueOf(request.getTenantIdentityProvidedDate().getYear()));
            }

            document.getElementById("tenantIDPlace").val(request.getTenantIdentityProvidedPlace());
            document.getElementById("tenantPhone").val(request.getTenantPhoneNumber());

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
            String fullName = document.getElementById("landlordName").val();
            String birthDateStr = document.getElementById("landlordBirthDate").val();
            String address = document.getElementById("landlordAddress").val();
            String identityNumber = document.getElementById("landlordID").val();
            String idDay = document.getElementById("landlordIDDay").val();
            String idMonth = document.getElementById("landlordIDMonth").val();
            String idYear = document.getElementById("landlordIDYear").val();
            String idPlace = document.getElementById("landlordIDPlace").val();
            String phone = document.getElementById("landlordPhone").val();

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
            String fullName = document.getElementById("tenantName").val();
            String birthDateStr = document.getElementById("tenantBirthDate").val();
            String address = document.getElementById("tenantAddress").val();
            String identityNumber = document.getElementById("tenantID").val();
            String idDay = document.getElementById("tenantIDDay").val();
            String idMonth = document.getElementById("tenantIDMonth").val();
            String idYear = document.getElementById("tenantIDYear").val();
            String idPlace = document.getElementById("tenantIDPlace").val();
            String phone = document.getElementById("tenantPhone").val();

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
