package com.fdsc.module.property.service;

import com.fdsc.common.result.PageResult;
import com.fdsc.module.property.entity.Property;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface PropertyService {
    void createProperty(Long companyId, Property property);
    void updateProperty(Long companyId, Long propertyId, Property property);
    void setOffMarket(Long companyId, Long propertyId);
    void approveProperty(Long adminId, Long propertyId);
    void rejectProperty(Long adminId, Long propertyId);
    PageResult<Property> listByCompany(Long companyId, int page, int size);
    PageResult<Property> search(String keyword, String district, String roomType,
                                BigDecimal priceMin, BigDecimal priceMax,
                                BigDecimal areaMin, BigDecimal areaMax, int page, int size);
    Property getDetail(Long propertyId);
    String uploadImage(MultipartFile file);
    void deleteImage(Long imageId);
}
