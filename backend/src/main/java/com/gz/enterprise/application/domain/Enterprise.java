package com.gz.enterprise.application.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "enterprise")
public class Enterprise extends BaseEntity {

    @NotBlank
    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @NotBlank
    @Column(nullable = false, unique = true, length = 128)
    private String name;

    @Column(length = 64)
    private String creditCode;

    @Column(length = 64)
    private String area;

    @Column(length = 64)
    private String industry;

    @Column(length = 64)
    private String scale;

    @Column(length = 64)
    private String legalRepresentative;

    @Column(length = 64)
    private String contactName;

    @Column(length = 32)
    private String contactPhone;

    @Column(length = 255)
    private String address;

    private Long registeredCapital;
    private String establishedDate;
    private Long annualRevenue;
    private Double revenueGrowthRate;
    private Long rdInvestment;
    private Double rdRatio;
    private Double debtRatio;
    private Long netProfit;
    private Integer employees;
    private Integer rdStaffCount;
    private Double rdStaffRatio;
    private Integer patentCount;
    private Double marketShare;

    @Column(length = 255)
    private String tags;

    @Column(length = 32)
    private String inventoryStatus = "在库";

    @Column(length = 32)
    private String status = "NORMAL";
}
