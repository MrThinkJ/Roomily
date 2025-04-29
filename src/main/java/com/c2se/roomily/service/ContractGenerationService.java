package com.c2se.roomily.service;

import com.c2se.roomily.entity.RentedRoom;
import com.c2se.roomily.entity.Room;
import com.c2se.roomily.payload.request.LandlordFillContractRequest;
import org.jsoup.nodes.Document;

import java.util.List;

public interface ContractGenerationService {
    Document generateRoomContract(Room room);
    Document generateRentContract(RentedRoom rentedRoom);
    void updateResponsibilities(Document document, List<String> responsibilities, String sectionId);
    void ensureResponsibilitiesSection(Document document, String sectionId, String sectionTitle);
    List<String> extractResponsibilities(Document document, String sectionId);
}
