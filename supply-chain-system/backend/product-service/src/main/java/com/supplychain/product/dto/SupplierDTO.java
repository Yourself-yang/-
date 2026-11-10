package com.supplychain.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplierDTO {

    @NotBlank(message = "供应商名称不能为空")
    private String supplierName;

    @NotBlank(message = "联系人不能为空")
    private String contactPerson;

    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    private String address;

    private Integer level = 3;

    private Integer status = 1;

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
