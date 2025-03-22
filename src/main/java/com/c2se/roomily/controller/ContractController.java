package com.c2se.roomily.controller;

import com.c2se.roomily.payload.request.LandlordFillContractRequest;
import com.c2se.roomily.payload.request.ModifyContractRequest;
import com.c2se.roomily.payload.request.TenantFillContractRequest;
import com.c2se.roomily.payload.response.ContractResponsibilitiesResponse;
import com.c2se.roomily.payload.response.ContractUserInfoResponse;
import com.c2se.roomily.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contracts")
public class ContractController extends BaseController{
    private final ContractService contractService;

    @PostMapping("/generate/{roomId}")
    public ResponseEntity<Void> generateDefaultContract(@PathVariable String roomId) {
        contractService.generateDefaultContract(roomId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/responsibilities/{roomId}")
    public ResponseEntity<ContractResponsibilitiesResponse> getContractResponsibilities(
            @PathVariable String roomId) {
        String userId = this.getUserInfo().getId();
        ContractResponsibilitiesResponse response = contractService.getContractResponsibilities(userId, roomId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/default-contract/{roomId}")
    public ResponseEntity<byte[]> getDefaultContractPdfByRoomId(@PathVariable String roomId) {
        byte[] pdf = contractService.getDefaultContractPdfByRoomId(roomId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"document.pdf\"")
                .body(pdf);
    }

    @GetMapping("/download/rented-contract/{rentedRoomId}")
    public ResponseEntity<byte[]> getContractPdfByRentedRoomId(@PathVariable String rentedRoomId) {
        byte[] pdf = contractService.getDefaultContractPdfByRoomId(rentedRoomId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"document.pdf\"")
                .body(pdf);
    }

    @PutMapping("/modify")
    public ResponseEntity<Void> modifyContract(@RequestBody ModifyContractRequest request) {
        contractService.modifyContract(request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/landlord-fill")
    public ResponseEntity<Void> fillContractByLandlord(
            @RequestBody LandlordFillContractRequest request) {
        contractService.fillContractByLandlord(request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/tenant-fill")
    public ResponseEntity<Void> fillContractByTenant(
            @RequestBody TenantFillContractRequest request) {
        contractService.fillContractByTenant(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/default/{roomId}", produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> getDefaultContract(@PathVariable String roomId) {
        String contract = contractService.getDefaultContract(roomId);
        return ResponseEntity.ok(contract);
    }

    @GetMapping("/user-info/{rentedRoomId}")
    public ResponseEntity<ContractUserInfoResponse> getContractUserInfo(
            @PathVariable String rentedRoomId) {
        String userId = this.getUserInfo().getId();
        ContractUserInfoResponse response = contractService.getContractUserInfo(userId, rentedRoomId);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{rentedRoomId}", produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> getContract(@PathVariable String rentedRoomId) {
        String contract = contractService.getContract(rentedRoomId);
        return ResponseEntity.ok(contract);
    }
}
