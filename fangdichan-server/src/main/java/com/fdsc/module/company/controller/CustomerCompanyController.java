package com.fdsc.module.company.controller;

import com.fdsc.common.result.Result;
import com.fdsc.module.company.entity.Company;
import com.fdsc.module.company.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/company")
@RequiredArgsConstructor
public class CustomerCompanyController {
    private final CompanyService companyService;

    @GetMapping("/list")
    public Result<List<Company>> listAll() {
        return Result.success(companyService.listAll());
    }

    @GetMapping("/{id}")
    public Result<Company> getDetail(@PathVariable Long id) {
        return Result.success(companyService.getCompanyById(id));
    }
}
