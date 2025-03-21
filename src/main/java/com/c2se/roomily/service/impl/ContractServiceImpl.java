package com.c2se.roomily.service.impl;

import com.c2se.roomily.config.StorageConfig;
import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.entity.Room;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.payload.request.LandlordFillContractRequest;
import com.c2se.roomily.payload.request.ModifyContractRequest;
import com.c2se.roomily.payload.request.TenantFillContractRequest;
import com.c2se.roomily.service.ContractService;
import com.c2se.roomily.service.RentedRoomService;
import com.c2se.roomily.service.RoomService;
import com.c2se.roomily.service.StorageService;
import com.c2se.roomily.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
    private final RentedRoomService rentedRoomService;
    private final RoomService roomService;
    private final StorageService storageService;
    private final StorageConfig storageConfig;

    @Override
    public void generateDefaultContract(String roomId) {
        Room room = roomService.getRoomEntityById(roomId);
        if (room == null) {
            throw new APIException(HttpStatus.NOT_FOUND, ErrorCode.FLEXIBLE_ERROR, "Room not found");
        }

        try {
            String contractTemplatePath = AppConstants.CONTRACT_TEMPLATE_PATH;
            String htmlContent = new String(Files.readAllBytes(Paths.get(contractTemplatePath)));
            Document document = Jsoup.parse(htmlContent);

            document.getElementById("rentalAddress").val(room.getAddress());
            document.getElementById("deposit").val(room.getRentalDeposit().toString());
            document.getElementById("rentalPrice").val(room.getPrice().toString());
            document.getElementById("electricityRate").val(room.getElectricityPrice().toString());
            document.getElementById("waterRate").val(room.getWaterPrice().toString());

            ensureResponsibilitiesSection(document, "responsibilitiesA", "* Trách nhiệm của bên A:");
            ensureResponsibilitiesSection(document, "responsibilitiesB", "* Trách nhiệm của bên B:");
            ensureResponsibilitiesSection(document, "responsibilitiesCommon", "TRÁCH NHIỆM CHUNG");

            String contractFilename = "contract_" + roomId + ".html";
            storageService.putObject(
                    MultipartFile.class.cast(document.html().getBytes()),
                    storageConfig.getBucketContract(),
                    contractFilename
            );
        } catch (Exception e) {
            throw new APIException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.FLEXIBLE_ERROR,
                                   "Error generating default contract: " + e.getMessage());
        }
    }

    @Override
    public void modifyContract(ModifyContractRequest request) {
        Room room = roomService.getRoomEntityById(request.getRoomId());
        if (room == null) {
            throw new APIException(HttpStatus.NOT_FOUND, ErrorCode.FLEXIBLE_ERROR, "Room not found");
        }

        try {
            String contractFilename = "contract_" + request.getRoomId() + ".html";
            byte[] contractBytes = storageService.getObject(storageConfig.getBucketContract(), contractFilename)
                    .readAllBytes();

            Document document = Jsoup.parse(new String(contractBytes));

            if (request.getContractDay() != null) {
                document.getElementById("contractDay").val(request.getContractDay());
            }
            if (request.getContractMonth() != null) {
                document.getElementById("contractMonth").val(request.getContractMonth());
            }
            if (request.getContractYear() != null) {
                document.getElementById("contractYear").val(request.getContractYear());
            }
            if (request.getContractAddress() != null) {
                document.getElementById("contractAddress").val(request.getContractAddress());
            }
            if (request.getRentalAddress() != null) {
                document.getElementById("rentalAddress").val(request.getRentalAddress());
            }
            if (request.getDeposit() != null) {
                document.getElementById("deposit").val(request.getDeposit().toString());
            }
            if (request.getResponsibilitiesA() != null) {
                updateResponsibilities(document, request.getResponsibilitiesA(), "responsibilitiesA");
            }
            if (request.getResponsibilitiesB() != null) {
                updateResponsibilities(document, request.getResponsibilitiesB(), "responsibilitiesB");
            }
            if (request.getResponsibilitiesCommon() != null) {
                updateResponsibilities(document, request.getResponsibilitiesCommon(), "responsibilitiesCommon");
            }

            storageService.putObject(
                    MultipartFile.class.cast(document.html().getBytes()),
                    storageConfig.getBucketContract(),
                    contractFilename
            );
        } catch (Exception e) {
            throw new APIException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.FLEXIBLE_ERROR,
                                   "Error modifying contract: " + e.getMessage());
        }
    }

    private void updateResponsibilities(Document document, List<String> responsibilities, String sectionId) {
        var section = document.getElementById(sectionId);
        if (section == null) {
            throw new APIException(HttpStatus.BAD_REQUEST, ErrorCode.FLEXIBLE_ERROR,
                                   "Section not found: " + sectionId);
        }
        section.children().remove();
        for (String responsibility : responsibilities) {
            section.append("<p>- " + responsibility + "</p>");
        }
    }

    private void ensureResponsibilitiesSection(Document document, String sectionId, String sectionTitle) {
        if (document.getElementById(sectionId) == null) {
            var content = document.select(".content").first();
            content.append("<div id='" + sectionId + "'><h3>" + sectionTitle + "</h3></div>");
        }
    }

    @Override
    public void fillContractByLandlord(LandlordFillContractRequest request) {
        RentedRoom rentedRoom = rentedRoomService.getRentedRoomEntityById(request.getRentedRoomId());
        if (rentedRoom == null) {
            throw new APIException(HttpStatus.NOT_FOUND, ErrorCode.FLEXIBLE_ERROR, "Rented room not found");
        }

        try {
            String roomId = rentedRoom.getRoom().getId();
            String contractFilename = "contract_" + roomId + ".html";
            byte[] contractBytes = storageService.getObject(storageConfig.getBucketContract(),
                                                            contractFilename).readAllBytes();

            Document document = Jsoup.parse(new String(contractBytes));

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

            String filledContractFilename = "contract_filled_" + request.getRentedRoomId() + ".html";
            storageService.putObject(
                    MultipartFile.class.cast(document.html().getBytes()),
                    storageConfig.getBucketContract(),
                    filledContractFilename
            );
        } catch (Exception e) {
            throw new APIException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.FLEXIBLE_ERROR,
                                   "Error filling landlord information: " + e.getMessage());
        }
    }

    @Override
    public void fillContractByTenant(TenantFillContractRequest request) {
        RentedRoom rentedRoom = rentedRoomService.getRentedRoomEntityById(request.getRentedRoomId());
        if (rentedRoom == null) {
            throw new APIException(HttpStatus.NOT_FOUND, ErrorCode.FLEXIBLE_ERROR, "Rented room not found");
        }

        try {
            String filledContractFilename = "contract_filled_" + request.getRentedRoomId() + ".html";
            byte[] contractBytes = storageService.getObject(storageConfig.getBucketContract(), filledContractFilename)
                    .readAllBytes();

            Document document = Jsoup.parse(new String(contractBytes));

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

            storageService.putObject(
                    MultipartFile.class.cast(document.html().getBytes()),
                    storageConfig.getBucketContract(),
                    filledContractFilename
            );
        } catch (Exception e) {
            throw new APIException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.FLEXIBLE_ERROR,
                                   "Error filling tenant information: " + e.getMessage());
        }
    }

    @Override
    public String getDefaultContract(String roomId) {
        try {
            String contractFilename = "contract_" + roomId + ".html";
            byte[] contractBytes = storageService.getObject(storageConfig.getBucketContract(), contractFilename)
                    .readAllBytes();
            return new String(contractBytes);
        } catch (Exception e) {
            generateDefaultContract(roomId);
            return getDefaultContract(roomId);
        }
    }

    private List<String> extractResponsibilities(Document document, String sectionId) {
        List<String> responsibilities = new ArrayList<>();
        var section = document.getElementById(sectionId);
        if (section != null) {
            section.select("p").forEach(element -> {
                String text = element.text();
                if (text.startsWith("- ")) {
                    text = text.substring(2);
                }
                responsibilities.add(text);
            });
        }
        return responsibilities;
    }

    @Override
    public String getContract(String rentedRoomId) {
        try {
            String filledContractFilename = "contract_filled_" + rentedRoomId + ".html";
            byte[] contractBytes = storageService.getObject(storageConfig.getBucketContract(), filledContractFilename)
                    .readAllBytes();
            return new String(contractBytes);
        } catch (Exception e) {
            throw new APIException(HttpStatus.NOT_FOUND, ErrorCode.FLEXIBLE_ERROR,
                                   "Contract not found for rented room: " + rentedRoomId);
        }
    }
}
