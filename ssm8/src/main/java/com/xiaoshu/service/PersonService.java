package com.xiaoshu.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xiaoshu.dao.CompanyMapper;
import com.xiaoshu.dao.PersonMapper;
import com.xiaoshu.entity.Company;
import com.xiaoshu.entity.Person;
import com.xiaoshu.entity.PersonVo;

@Service
public class PersonService {
	
	@Autowired
	private PersonMapper personMapper;
	
	
	@Autowired
	private CompanyMapper companyMapper;
	
	
	public List<PersonVo> countPerson(){
		return personMapper.countPerson();
	}
	
	public List<Company> findCompany(){
		return companyMapper.selectAll();
	}
	
	
	public void importPerson(MultipartFile personFile) throws InvalidFormatException, IOException{
		//personFile文件转换成workBook
		//03  与 07 版本兼容问题
		//工厂类
		Workbook workbook = WorkbookFactory.create(personFile.getInputStream());
		
		//获取sheet对象
		Sheet sheet = workbook.getSheetAt(0);
		
		
		int lastRowNum = sheet.getLastRowNum();//最后一行的下标
		
		for (int i = 0; i < lastRowNum; i++) {
			//获取行对对象   
			Row row = sheet.getRow(i+1);//第一行是表头信息 ， 无效数据 不需要解析
			
			//通过POI解析数据
			//1、字符串  toString
			//2、时间    getDateCellValue
			//3、数字   getNumericCellValue且强转为long
			String name = row.getCell(0).toString();
			String sex = row.getCell(1).toString();
			String trait = row.getCell(2).toString();
			Date entryTime = row.getCell(3).getDateCellValue();
			String cname = row.getCell(4).toString();//公司名称
			
			//判断性别是男的  并且  公司是京东快递
			
//			if(sex.equals("男") && cname.equals("京东快递")){
				
			Person personName = findByName(name);
			if(personName==null){//名称不存在
				//解析的数据，封装到实体类
				Person p = new Person();
				p.setExpressName(name);
				p.setSex(sex);
				p.setExpressTrait(trait);
				p.setEntryTime(entryTime);
				
				//根据公司名称查询公司id
				Company param = new Company();
				param.setExpressName(cname);
				Company company = companyMapper.selectOne(param );
				//如果公司不存在，仍然可以导入成功
				
				if(company==null){
					//如果该公司不存在，添加该公司（获取公司的主键id）
					companyMapper.addCom(param);
//					company = param; //重新赋值
					p.setExpressTypeId(param.getId());
				}else{
					p.setExpressTypeId(company.getId());
				}
				
				//保持入库
				p.setCreateTime(new Date());
				personMapper.insert(p);
			}

//			}
			
//			long phone = (long)row.getCell(5).getNumericCellValue();
//			long age = (long)row.getCell(6).getNumericCellValue();
//			
			
//			String phoneStr = phone+"";
//			if(phoneStr.startsWith("136") 
//					|| phoneStr.startsWith("137")
//					|| phoneStr.startsWith("138")){
//				phoneStr = "联通";
//			}
//			if(phoneStr.startsWith("189") 
//					|| phoneStr.startsWith("199")){
//				phoneStr = "电信";
//			}
			
//			person.setPhone(phoneStr);

			
		}
	}
	
	public List<PersonVo> findList(PersonVo personVo){
		return personMapper.findList(personVo);
	}
	
	public PageInfo<PersonVo> findPage(PersonVo personVo, Integer pageNum, Integer pageSize){
		PageHelper.startPage(pageNum, pageSize);
		
		List<PersonVo> list = personMapper.findList(personVo);
		
		return new PageInfo<>(list);
	}
	
	public Person findByName(String name){
		Person param = new Person();
		param.setExpressName(name);
		return personMapper.selectOne(param );
	}
	
	public void addPerson(Person person){
		person.setCreateTime(new Date());//初试化创建时间
//		personMapper.insert(person);
		
		personMapper.addPerson(person);
		
		
	}
	
	public void updatePerson(Person person){
		personMapper.updateByPrimaryKeySelective(person);
	}
	
	public void delPerson(Long id){
		personMapper.deleteByPrimaryKey(id);
	}
}
