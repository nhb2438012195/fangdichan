package com.fdsc.module.company.service;

import com.fdsc.common.exception.BusinessException;
import com.fdsc.module.company.entity.Company;
import com.fdsc.module.company.mapper.CompanyMapper;
import com.fdsc.module.company.service.impl.CompanyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {

    @Mock private CompanyMapper companyMapper;
    private CompanyServiceImpl companyService;

    @BeforeEach
    void setUp() {
        companyService = new CompanyServiceImpl(companyMapper);
    }

    @Test
    void getCompanyByUserId_shouldReturnCompany() {
        Company company = new Company();
        company.setId(1L);
        company.setCompanyName("测试公司");
        when(companyMapper.selectOne(any())).thenReturn(company);

        Company result = companyService.getCompanyByUserId(1L);
        assertNotNull(result);
        assertEquals("测试公司", result.getCompanyName());
    }

    @Test
    void getCompanyByUserId_shouldThrowWhenNotFound() {
        when(companyMapper.selectOne(any())).thenReturn(null);
        assertThrows(BusinessException.class, () -> companyService.getCompanyByUserId(1L));
    }

    @Test
    void getCompanyById_shouldReturnCompany() {
        Company company = new Company();
        company.setId(1L);
        when(companyMapper.selectById(1L)).thenReturn(company);

        Company result = companyService.getCompanyById(1L);
        assertNotNull(result);
    }

    @Test
    void updateCompany_shouldSucceed() {
        when(companyMapper.selectOne(any())).thenReturn(new Company());

        Company update = new Company();
        update.setCompanyName("新名称");
        companyService.updateCompany(1L, update);
        verify(companyMapper).update(any(), any());
    }

    @Test
    void listAll_shouldReturnList() {
        when(companyMapper.selectList(any())).thenReturn(List.of(new Company(), new Company()));
        assertEquals(2, companyService.listAll().size());
    }
}
