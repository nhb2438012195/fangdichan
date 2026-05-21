package com.fdsc.config;

import com.fdsc.module.property.entity.Property;
import com.fdsc.module.property.mapper.PropertyMapper;
import com.fdsc.module.region.entity.Region;
import com.fdsc.module.region.mapper.RegionMapper;
import com.fdsc.module.roomtype.entity.RoomType;
import com.fdsc.module.roomtype.mapper.RoomTypeMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RegionMapper regionMapper;
    private final RoomTypeMapper roomTypeMapper;
    private final PropertyMapper propertyMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        if (regionMapper.selectCount(null) > 0) {
            log.info("Data already initialized, skipping");
            return;
        }

        log.info("Starting data initialization...");

        insertRoomTypes();
        insertRegions();
        insertProperties();

        log.info("Data initialization complete");
    }

    private void insertRoomTypes() {
        insertRoomType(1, 1, "一室一厅");
        insertRoomType(2, 1, "两室一厅");
        insertRoomType(3, 1, "三室一厅");
        insertRoomType(3, 2, "三室两厅");
        insertRoomType(4, 2, "四室两厅");
        insertRoomType(5, 2, "五室两厅");
        log.info("Inserted 6 room types");
    }

    private void insertRoomType(int bedroom, int livingRoom, String displayName) {
        RoomType rt = new RoomType();
        rt.setBedroomCount(bedroom);
        rt.setLivingRoomCount(livingRoom);
        rt.setDisplayName(displayName);
        roomTypeMapper.insert(rt);
    }

    private void insertRegions() throws Exception {
        ClassPathResource resource = new ClassPathResource("data/regions.json");
        List<Map<String, Object>> regions = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<Map<String, Object>>>() {});

        Map<String, Long> nameToId = new HashMap<>();

        for (Map<String, Object> item : regions) {
            int level = (int) item.get("level");
            if (level == 1) {
                Region r = new Region();
                r.setName((String) item.get("name"));
                r.setLevel(1);
                r.setType((String) item.get("type"));
                regionMapper.insert(r);
                nameToId.put(r.getName(), r.getId());
            }
        }

        for (Map<String, Object> item : regions) {
            int level = (int) item.get("level");
            if (level == 2) {
                Region r = new Region();
                r.setName((String) item.get("name"));
                r.setLevel(2);
                r.setType((String) item.get("type"));
                String parentName = (String) item.get("parentName");
                if (parentName != null && nameToId.containsKey(parentName)) {
                    r.setParentId(nameToId.get(parentName));
                }
                regionMapper.insert(r);
            }
        }

        log.info("Inserted {} regions", regions.size());
    }

    private void insertProperties() {
        // Using existing Property fields only (regionId/provinceId/roomTypeId added in Task 6)
        insertProperty("朝阳区精装三居室", "朝阳区建国路88号", 120, 5000000, true, "APPROVED");
        insertProperty("海淀区学区两居室", "海淀区中关村大街1号", 85, 3500000, true, "APPROVED");
        insertProperty("东城区精装一居室", "东城区王府井大街10号", 50, 2800000, false, "APPROVED");
        insertProperty("西城区大平层", "西城区金融街5号", 200, 12000000, true, "APPROVED");
        insertProperty("丰台区花园洋房", "丰台区科技园南路", 135, 6200000, true, "APPROVED");
        insertProperty("浦东新区江景房", "浦东新区陆家嘴路1号", 150, 8000000, true, "APPROVED");
        insertProperty("广州市天河区住宅", "天河区天河路100号", 110, 4000000, true, "APPROVED");
        insertProperty("深圳市南山区科技园住宅", "南山区科技园南路", 95, 6000000, true, "APPROVED");
        insertProperty("杭州市西湖区景观房", "西湖区龙井路1号", 120, 4500000, true, "APPROVED");
        log.info("Inserted 9 test properties");
    }

    private void insertProperty(String title, String location, int area, int price,
                                 boolean isVacant, String status) {
        Property p = new Property();
        p.setTitle(title);
        p.setLocation(location);
        p.setArea(new BigDecimal(area));
        p.setPrice(new BigDecimal(price));
        p.setIsVacant(isVacant);
        p.setStatus(status);
        propertyMapper.insert(p);
    }
}
