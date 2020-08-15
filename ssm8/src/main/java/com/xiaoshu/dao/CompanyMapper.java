package com.xiaoshu.dao;

import com.xiaoshu.base.dao.BaseMapper;
import com.xiaoshu.entity.Company;

public interface CompanyMapper extends BaseMapper<Company> {
	
	public void addCom(Company company);
}