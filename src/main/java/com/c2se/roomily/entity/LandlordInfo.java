package com.c2se.roomily.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDate;

@Entity
@Table(name = "landlord_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class LandlordInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "landlord_info_id")
    private String id;
    
    private String fullName;
    
    private LocalDate dateOfBirth;
    
    private String permanentResidence;
    
    private String identityNumber;
    
    private LocalDate identityProvidedDate;
    
    private String identityProvidedPlace;
    
    private String phoneNumber;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;
} 