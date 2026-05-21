package com.fdsc.module.property.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fdsc.common.exception.BusinessException;
import com.fdsc.common.result.PageResult;
import com.fdsc.module.property.entity.Property;
import com.fdsc.module.property.entity.PropertyImage;
import com.fdsc.module.property.mapper.PropertyImageMapper;
import com.fdsc.module.property.mapper.PropertyMapper;
import com.fdsc.module.property.service.PropertyService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {
    private final PropertyMapper propertyMapper;
    private final PropertyImageMapper propertyImageMapper;
    private final MinioClient minioClient;

    @Override
    public void createProperty(Long companyId, Property property) {
        if (property.getPrice() != null && property.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(400, "价格不能为负数");
        }
        property.setCompanyId(companyId);
        property.setStatus("PENDING");
        property.setIsVacant(true);
        propertyMapper.insert(property);
    }

    @Override
    public void updateProperty(Long companyId, Long propertyId, Property property) {
        Property existing = propertyMapper.selectById(propertyId);
        if (existing == null || !existing.getCompanyId().equals(companyId)) {
            throw new BusinessException(404, "房源不存在");
        }
        property.setId(propertyId);
        property.setCompanyId(companyId);
        property.setUnitPrice(null);
        if (property.getPrice() != null && property.getArea() != null && property.getArea().compareTo(BigDecimal.ZERO) > 0) {
            property.setUnitPrice(property.getPrice().divide(property.getArea(), 2, java.math.RoundingMode.HALF_UP));
        }
        propertyMapper.updateById(property);
    }

    @Override
    public void setOffMarket(Long companyId, Long propertyId) {
        Property property = propertyMapper.selectById(propertyId);
        if (property == null || !property.getCompanyId().equals(companyId)) {
            throw new BusinessException(404, "房源不存在");
        }
        property.setStatus("OFF_MARKET");
        propertyMapper.updateById(property);
    }

    @Override
    public void approveProperty(Long adminId, Long propertyId) {
        Property property = propertyMapper.selectById(propertyId);
        if (property == null || !"PENDING".equals(property.getStatus())) {
            throw new BusinessException(400, "该房源不在待审核状态");
        }
        property.setStatus("APPROVED");
        propertyMapper.updateById(property);
    }

    @Override
    public void rejectProperty(Long adminId, Long propertyId) {
        Property property = propertyMapper.selectById(propertyId);
        if (property == null || !"PENDING".equals(property.getStatus())) {
            throw new BusinessException(400, "该房源不在待审核状态");
        }
        property.setStatus("REJECTED");
        propertyMapper.updateById(property);
    }

    @Override
    public PageResult<Property> listByCompany(Long companyId, int page, int size) {
        Page<Property> p = propertyMapper.selectPage(new Page<>(page, size),
            new LambdaQueryWrapper<Property>().eq(Property::getCompanyId, companyId));
        return PageResult.of(p.getRecords(), page, size, p.getTotal());
    }

    @Override
    public PageResult<Property> search(String keyword, String district, String roomType,
                                        BigDecimal priceMin, BigDecimal priceMax,
                                        BigDecimal areaMin, BigDecimal areaMax, int page, int size) {
        LambdaQueryWrapper<Property> qw = new LambdaQueryWrapper<Property>()
            .eq(Property::getStatus, "APPROVED");
        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like(Property::getTitle, keyword).or().like(Property::getLocation, keyword));
        }
        if (district != null && !district.isEmpty()) qw.eq(Property::getDistrict, district);
        if (roomType != null && !roomType.isEmpty()) qw.eq(Property::getRoomType, roomType);
        if (priceMin != null) qw.ge(Property::getPrice, priceMin);
        if (priceMax != null) qw.le(Property::getPrice, priceMax);
        if (areaMin != null) qw.ge(Property::getArea, areaMin);
        if (areaMax != null) qw.le(Property::getArea, areaMax);
        Page<Property> p = propertyMapper.selectPage(new Page<>(page, size), qw);
        return PageResult.of(p.getRecords(), page, size, p.getTotal());
    }

    @Override
    public Property getDetail(Long propertyId) {
        Property property = propertyMapper.selectById(propertyId);
        if (property == null) throw new BusinessException(404, "房源不存在");
        return property;
    }

    @Override
    public String uploadImage(MultipartFile file) {
        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            minioClient.putObject(PutObjectArgs.builder()
                .bucket("fangdichan")
                .object(filename)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());
            return "/api/image/" + filename;
        } catch (Exception e) {
            throw new BusinessException(500, "图片上传失败");
        }
    }

    @Override
    public void deleteImage(Long imageId) {
        propertyImageMapper.deleteById(imageId);
    }
}
