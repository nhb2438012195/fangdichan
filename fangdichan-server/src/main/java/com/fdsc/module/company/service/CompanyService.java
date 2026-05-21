package com.fdsc.module.company.service;

import com.fdsc.module.company.entity.Company;

import java.util.List;

public interface CompanyService {
    Company getCompanyByUserId(Long userId);
    Company getCompanyById(Long id);
    void updateCompany(Long userId, Company company);
    List<Company> listAll();
}
