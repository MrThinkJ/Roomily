package com.c2se.roomily.service;

import org.springframework.stereotype.Service;

@Service
public interface ContractStorageService {
    void saveRoomContract(String roomId, String contractHtml);
    void saveRentedRoomContract(String rentedRoomId, String contractHtml);
    byte[] getLandlordContract(String landlordId);
    byte[] getRoomContract(String roomId);
    byte[] getRentedRoomContract(String rentedRoomId);
    byte[] getRoomContractPdf(String roomId);
    byte[] getRentedRoomContractPdf(String rentedRoomId);
    boolean roomContractExists(String roomId);
    boolean rentedRoomContractExists(String rentedRoomId);
} 