package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.entity.Room;
import com.c2se.roomily.enums.ErrorCode;
import com.c2se.roomily.exception.APIException;
import com.c2se.roomily.payload.request.LandlordFillContractRequest;
import com.c2se.roomily.service.ContractGenerationService;
import com.c2se.roomily.service.ContractStorageService;
import com.c2se.roomily.util.AppConstants;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractGenerationServiceImpl implements ContractGenerationService {
    @Value("${app.resource.static-location}")
    private String staticLocation;
    private final ContractStorageService contractStorageService;

    @Override
    public Document generateRoomContract(Room room) {
        if (room == null) {
            throw new APIException(HttpStatus.NOT_FOUND, ErrorCode.FLEXIBLE_ERROR, "Room not found");
        }
        try {
            String contractTemplatePath = staticLocation+ AppConstants.CONTRACT_TEMPLATE_PATH;
            String htmlContent = new String(Files.readAllBytes(Paths.get(contractTemplatePath)));
            Document document = Jsoup.parse(htmlContent);

            reformatRentalAddress(document, room.getAddress());
            document.getElementById("deposit").append(room.getRentalDeposit().toString());
            document.getElementById("rentalPrice").append(room.getPrice().toString());
            document.getElementById("electricityRate").append(room.getElectricityPrice().toString());
            document.getElementById("waterRate").append(room.getWaterPrice().toString());

            ensureResponsibilitiesSection(document, "responsibilitiesA", "* Trách nhiệm của bên A:");
            ensureResponsibilitiesSection(document, "responsibilitiesB", "* Trách nhiệm của bên B:");
            ensureResponsibilitiesSection(document, "responsibilitiesCommon", "TRÁCH NHIỆM CHUNG");
            contractStorageService.saveRoomContract(room.getId(), document.html());
            return document;
        } catch (Exception e) {
            throw new APIException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.FLEXIBLE_ERROR,
                                   "Error generating default contract: " + e.getMessage());
        }
    }

    @Override
    public Document generateRentContract(RentedRoom rentedRoom) {
        if (rentedRoom == null) {
            throw new APIException(HttpStatus.NOT_FOUND, ErrorCode.FLEXIBLE_ERROR, "Rented room not found");
        }
        try {
            byte[] roomContract = contractStorageService.getRoomContract(rentedRoom.getRoom().getId());
            if (roomContract == null) {
                generateRoomContract(rentedRoom.getRoom());
            }
            Document document = Jsoup.parse(new String(roomContract));
            contractStorageService.saveRentedRoomContract(rentedRoom.getId(), document.html());
            return document;
        } catch (Exception e) {
            throw new APIException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.FLEXIBLE_ERROR,
                                   "Error generating rent contract: " + e.getMessage());
        }
    }

    private void reformatRentalAddress(Document document, String address) {
        var labelParagraph = document.getElementsContainingOwnText("Bên A đồng ý cho bên B thuê 01 phòng ở tại địa chỉ:").first();
        if (labelParagraph != null) {
            String formattedAddress = address.trim();
            String newParagraphHtml = "Bên A đồng ý cho bên B thuê 01 phòng ở tại địa chỉ: " +
                    formattedAddress;
            labelParagraph.html(newParagraphHtml);
        }
    }

    @Override
    public void updateResponsibilities(Document document, List<String> responsibilities, String sectionId) {
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

    @Override
    public void ensureResponsibilitiesSection(Document document, String sectionId, String sectionTitle) {
        if (document.getElementById(sectionId) == null) {
            var content = document.select(".content").first();
            content.append("<div id='" + sectionId + "'><h3>" + sectionTitle + "</h3></div>");
        }
    }

    @Override
    public List<String> extractResponsibilities(Document document, String sectionId) {
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
}
