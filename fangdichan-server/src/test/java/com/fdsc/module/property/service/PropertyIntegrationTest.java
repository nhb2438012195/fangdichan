package com.fdsc.module.property.service;

import com.fdsc.common.exception.BusinessException;
import com.fdsc.common.result.PageResult;
import com.fdsc.module.property.entity.Property;
import com.fdsc.module.property.mapper.PropertyMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PropertyIntegrationTest {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private PropertyMapper propertyMapper;

    private Property testProperty;

    @BeforeEach
    void setUp() {
        testProperty = new Property();
        testProperty.setTitle("测试房源-朝阳区三室");
        testProperty.setLocation("朝阳区测试路100号");
        testProperty.setDistrict("朝阳区");
        testProperty.setFloor("10");
        testProperty.setFloorTotal(20);
        testProperty.setRoomType("三室");
        testProperty.setArea(new BigDecimal("100"));
        testProperty.setPrice(new BigDecimal("5000000"));
        testProperty.setDescription("测试用房源");
    }

    @Test
    void createProperty_shouldInsertAndCalculateUnitPrice() {
        propertyService.createProperty(1L, testProperty);

        assertNotNull(testProperty.getId());
        assertEquals("PENDING", testProperty.getStatus());
        assertTrue(testProperty.getIsVacant());
        // unitPrice = 5000000 / 100 = 50000.00
        assertEquals(0, new BigDecimal("50000.00").compareTo(testProperty.getUnitPrice()));

        // Verify persisted in DB
        Property saved = propertyMapper.selectById(testProperty.getId());
        assertNotNull(saved);
        assertEquals("PENDING", saved.getStatus());
    }

    @Test
    void createProperty_shouldThrowWhenPriceNegative() {
        testProperty.setPrice(new BigDecimal("-100"));
        assertThrows(BusinessException.class, () -> propertyService.createProperty(1L, testProperty));
    }

    @Test
    void updateProperty_shouldModifyFields() {
        propertyService.createProperty(1L, testProperty);
        Long id = testProperty.getId();

        Property update = new Property();
        update.setTitle("已更新的房源");
        update.setPrice(new BigDecimal("6000000"));
        update.setArea(new BigDecimal("120"));
        propertyService.updateProperty(1L, id, update);

        Property updated = propertyMapper.selectById(id);
        assertEquals("已更新的房源", updated.getTitle());
        assertEquals(0, new BigDecimal("50000.00").compareTo(updated.getUnitPrice()));
    }

    @Test
    void updateProperty_shouldThrowWhenNotOwner() {
        propertyService.createProperty(1L, testProperty);
        assertThrows(BusinessException.class,
            () -> propertyService.updateProperty(999L, testProperty.getId(), new Property()));
    }

    @Test
    void approveProperty_shouldChangeStatus() {
        propertyService.createProperty(1L, testProperty);
        propertyService.approveProperty(10L, testProperty.getId());

        Property saved = propertyMapper.selectById(testProperty.getId());
        assertEquals("APPROVED", saved.getStatus());
    }

    @Test
    void approveProperty_shouldThrowWhenNotPending() {
        propertyService.createProperty(1L, testProperty);
        propertyService.approveProperty(10L, testProperty.getId());
        assertThrows(BusinessException.class,
            () -> propertyService.approveProperty(10L, testProperty.getId()));
    }

    @Test
    void rejectProperty_shouldChangeStatus() {
        propertyService.createProperty(1L, testProperty);
        propertyService.rejectProperty(10L, testProperty.getId());

        Property saved = propertyMapper.selectById(testProperty.getId());
        assertEquals("REJECTED", saved.getStatus());
    }

    @Test
    void setOffMarket_shouldChangeStatus() {
        propertyService.createProperty(1L, testProperty);
        propertyService.approveProperty(10L, testProperty.getId());
        propertyService.setOffMarket(1L, testProperty.getId());

        Property saved = propertyMapper.selectById(testProperty.getId());
        assertEquals("OFF_MARKET", saved.getStatus());
    }

    @Test
    void setOffMarket_shouldThrowWhenWrongCompany() {
        propertyService.createProperty(1L, testProperty);
        assertThrows(BusinessException.class,
            () -> propertyService.setOffMarket(999L, testProperty.getId()));
    }

    @Test
    void listApproved_shouldReturnOnlyApproved() {
        // Create and approve a property
        propertyService.createProperty(1L, testProperty);
        propertyService.approveProperty(10L, testProperty.getId());

        PageResult<Property> result = propertyService.listApproved(1, 10);
        assertTrue(result.getList().stream().allMatch(p -> "APPROVED".equals(p.getStatus())));
    }

    @Test
    void recommended_shouldReturnApproved() {
        propertyService.createProperty(1L, testProperty);
        propertyService.approveProperty(10L, testProperty.getId());

        PageResult<Property> result = propertyService.recommended(1, 6);
        assertTrue(result.getTotal() > 0);
        assertTrue(result.getList().stream().allMatch(p -> "APPROVED".equals(p.getStatus())));
    }

    @Test
    void search_shouldFindByKeyword() {
        propertyService.createProperty(1L, testProperty);
        propertyService.approveProperty(10L, testProperty.getId());

        PageResult<Property> result = propertyService.search("朝阳区", null, null, null, null, null, null, null, 1, 10);
        assertTrue(result.getTotal() > 0);
    }

    @Test
    void search_shouldFilterByDistrict() {
        propertyService.createProperty(1L, testProperty);
        propertyService.approveProperty(10L, testProperty.getId());

        PageResult<Property> result = propertyService.search(null, "朝阳区", null, null, null, null, null, null, 1, 10);
        assertTrue(result.getTotal() > 0);
        assertTrue(result.getList().stream().allMatch(p -> "朝阳区".equals(p.getDistrict())));
    }

    @Test
    void search_shouldFilterByPrice() {
        propertyService.createProperty(1L, testProperty);
        propertyService.approveProperty(10L, testProperty.getId());

        PageResult<Property> result = propertyService.search(null, null, null,
            new BigDecimal("4000000"), new BigDecimal("6000000"), null, null, null, 1, 10);
        assertTrue(result.getTotal() > 0);
        assertTrue(result.getList().stream().allMatch(p ->
            p.getPrice().compareTo(new BigDecimal("4000000")) >= 0 &&
            p.getPrice().compareTo(new BigDecimal("6000000")) <= 0));
    }

    @Test
    void search_shouldFilterByArea() {
        propertyService.createProperty(1L, testProperty);
        propertyService.approveProperty(10L, testProperty.getId());

        PageResult<Property> result = propertyService.search(null, null, null, null, null,
            new BigDecimal("50"), new BigDecimal("150"), null, 1, 10);
        assertTrue(result.getTotal() > 0);
        assertTrue(result.getList().stream().allMatch(p ->
            p.getArea().compareTo(new BigDecimal("50")) >= 0 &&
            p.getArea().compareTo(new BigDecimal("150")) <= 0));
    }

    @Test
    void search_shouldFilterByRoomType() {
        propertyService.createProperty(1L, testProperty);
        propertyService.approveProperty(10L, testProperty.getId());

        PageResult<Property> result = propertyService.search(null, null, "三室", null, null, null, null, null, 1, 10);
        assertTrue(result.getTotal() > 0);
        assertTrue(result.getList().stream().allMatch(p -> "三室".equals(p.getRoomType())));
    }

    @Test
    void search_shouldCombineMultipleFilters() {
        propertyService.createProperty(1L, testProperty);
        propertyService.approveProperty(10L, testProperty.getId());

        PageResult<Property> result = propertyService.search(null, "朝阳区", "三室",
            new BigDecimal("4000000"), new BigDecimal("6000000"),
            new BigDecimal("50"), new BigDecimal("150"), null, 1, 10);
        assertTrue(result.getTotal() > 0);
        assertTrue(result.getList().stream().allMatch(p -> "APPROVED".equals(p.getStatus())));
    }

    @Test
    void search_shouldReturnEmptyForNoMatch() {
        propertyService.createProperty(1L, testProperty);
        propertyService.approveProperty(10L, testProperty.getId());

        PageResult<Property> result = propertyService.search(null, "海淀区", "五室",
            new BigDecimal("100"), new BigDecimal("200"),
            new BigDecimal("1000"), new BigDecimal("2000"), null, 1, 10);
        assertEquals(0, result.getTotal());
    }

    @Test
    void search_shouldOnlyReturnApproved() {
        // Create but don't approve
        propertyService.createProperty(1L, testProperty);

        PageResult<Property> result = propertyService.search(testProperty.getTitle(), null, null, null, null, null, null, null, 1, 10);
        assertEquals(0, result.getTotal());
    }

    @Test
    void getDetail_shouldReturnApprovedProperty() {
        propertyService.createProperty(1L, testProperty);
        propertyService.approveProperty(10L, testProperty.getId());

        Property detail = propertyService.getDetail(testProperty.getId());
        assertNotNull(detail);
        assertEquals(testProperty.getTitle(), detail.getTitle());
    }

    @Test
    void getDetail_shouldThrowWhenNotApproved() {
        propertyService.createProperty(1L, testProperty);
        assertThrows(BusinessException.class, () -> propertyService.getDetail(testProperty.getId()));
    }

    @Test
    void getDetail_shouldThrowWhenRejected() {
        propertyService.createProperty(1L, testProperty);
        propertyService.rejectProperty(10L, testProperty.getId());
        assertThrows(BusinessException.class, () -> propertyService.getDetail(testProperty.getId()));
    }

    @Test
    void getDetail_shouldThrowWhenOffMarket() {
        propertyService.createProperty(1L, testProperty);
        propertyService.approveProperty(10L, testProperty.getId());
        propertyService.setOffMarket(1L, testProperty.getId());
        assertThrows(BusinessException.class, () -> propertyService.getDetail(testProperty.getId()));
    }

    @Test
    void getDetail_shouldThrowWhenNotFound() {
        assertThrows(BusinessException.class, () -> propertyService.getDetail(99999L));
    }

    @Test
    void listByCompany_shouldReturnScopedResults() {
        propertyService.createProperty(1L, testProperty);

        PageResult<Property> result = propertyService.listByCompany(1L, 1, 10);
        assertTrue(result.getTotal() > 0);
        assertTrue(result.getList().stream().allMatch(p -> p.getCompanyId().equals(1L)));
    }

    @Test
    void pagination_shouldWork() {
        // Create multiple properties
        for (int i = 0; i < 3; i++) {
            Property p = new Property();
            p.setTitle("分页测试-" + i);
            p.setLocation("测试地点");
            p.setRoomType("三室");
            p.setArea(new BigDecimal("100"));
            p.setPrice(new BigDecimal("5000000"));
            propertyService.createProperty(1L, p);
            propertyService.approveProperty(10L, p.getId());
        }

        PageResult<Property> page1 = propertyService.listApproved(1, 2);
        assertTrue(page1.getList().size() <= 2);
        assertEquals(1, page1.getPage());

        PageResult<Property> page2 = propertyService.listApproved(2, 2);
        assertEquals(2, page2.getPage());
    }
}
