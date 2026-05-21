package com.fdsc.module.company.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fdsc.common.exception.BusinessException;
import com.fdsc.module.company.entity.Company;
import com.fdsc.module.company.mapper.CompanyMapper;
import com.fdsc.module.company.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {
    private final CompanyMapper companyMapper;

    @Override
    public Company getCompanyByUserId(Long userId) {
        Company company = companyMapper.selectOne(
            new LambdaQueryWrapper<Company>().eq(Company::getUserId, userId));
        if (company == null) throw new BusinessException(404, "公司信息不存在");
        return company;
    }

    @Override
    public Company getCompanyById(Long id) {
        Company company = companyMapper.selectById(id);
        if (company == null) throw new BusinessException(404, "公司不存在");
        return company;
    }

    @Override
    public void updateCompany(Long userId, Company company) {
        Company existing = companyMapper.selectOne(
            new LambdaQueryWrapper<Company>().eq(Company::getUserId, userId));
        if (existing == null) throw new BusinessException(404, "公司信息不存在");
        company.setId(existing.getId());
        company.setUserId(userId);
        companyMapper.update(company, new LambdaQueryWrapper<Company>().eq(Company::getId, existing.getId()));
    }

    @Override
    public List<Company> listAll() {
        return companyMapper.selectList(null);
    }
}
