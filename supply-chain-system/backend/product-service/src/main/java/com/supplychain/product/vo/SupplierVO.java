package com.supplychain.product.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplierVO {

    private Long id;

    private String supplierName;

    private String contactPerson;

    private String contactPhone;

    private String address;

    private Integer level;

    private Integer status;

    private LocalDateTime createTime;

    @JsonProperty("contact")
    public String getContact() {
        return contactPerson;
    }

    @JsonProperty("contact")
    public void setContact(String contact) {
        this.contactPerson = contact;
    }

    @JsonProperty("phone")
    public String getPhone() {
        return contactPhone;
    }

    @JsonProperty("phone")
    public void setPhone(String phone) {
        this.contactPhone = phone;
    }

    @JsonProperty("rating")
    public Integer getRating() {
        return level;
    }

    @JsonProperty("rating")
    public void setRating(Integer rating) {
        this.level = rating;
    }
}
