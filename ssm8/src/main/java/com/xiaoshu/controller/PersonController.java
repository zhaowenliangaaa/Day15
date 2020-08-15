package com.xiaoshu.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.xiaoshu.config.util.ConfigUtil;
import com.xiaoshu.entity.Log;
import com.xiaoshu.entity.Operation;
import com.xiaoshu.entity.Person;
import com.xiaoshu.entity.PersonVo;
import com.xiaoshu.entity.User;
import com.xiaoshu.service.OperationService;
import com.xiaoshu.service.PersonService;
import com.xiaoshu.service.RoleService;
import com.xiaoshu.service.UserService;
import com.xiaoshu.util.StringUtil;
import com.xiaoshu.util.TimeUtil;
import com.xiaoshu.util.WriterUtil;

@Controller
@RequestMapping("person")
public class PersonController extends LogController{
	static Logger logger = Logger.getLogger(PersonController.class);

	@Autowired
	private UserService userService;
	
	@Autowired
	private RoleService roleService ;
	
	@Autowired
	private OperationService operationService;
	
	@Autowired
	private PersonService personService;
	
	
	@RequestMapping("personIndex")
	public String index(HttpServletRequest request,Integer menuid) throws Exception{
		List<Operation> operationList = operationService.findOperationIdsByMenuid(menuid);
		request.setAttribute("operationList", operationList);
		request.setAttribute("cList", personService.findCompany());
		return "person";
	}
	
	
	@RequestMapping(value="personList",method=RequestMethod.POST)
	public void personList(PersonVo personVo,HttpServletRequest request,HttpServletResponse response,String offset,String limit) throws Exception{
		try {
			String order = request.getParameter("order");
			String ordername = request.getParameter("ordername");
			
			Integer pageSize = StringUtil.isEmpty(limit)?ConfigUtil.getPageSize():Integer.parseInt(limit);
			Integer pageNum =  (Integer.parseInt(offset)/pageSize)+1;
			
			
//			PageInfo<User> userList= userService.findUserPage(user,pageNum,pageSize,ordername,order);
			
			PageInfo<PersonVo> page = personService.findPage(personVo, pageNum, pageSize);
			
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("total",page.getTotal() );
			jsonObj.put("rows", page.getList());
	        WriterUtil.write(response,jsonObj.toString());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("人员展示错误",e);
			throw e;
		}
	}
	
	
	// 新增或修改
	@RequestMapping("reserveUser")
	public void reserveUser(HttpServletRequest request,Person person,HttpServletResponse response){
		Long id = person.getId();
		JSONObject result=new JSONObject();
		try {
			
			Person person2 = personService.findByName(person.getExpressName());
			
			if (id != null) {   // userId不为空 说明是修改
				if(person2==null ||(person2 != null && person2.getId().equals(id))){
					personService.updatePerson(person);
					result.put("success", true);
				}else{
					result.put("success", true);
					result.put("errorMsg", "该人员名被使用");
				}
				
			}else {   // 添加
				if(person2==null){  // 没有重复可以添加
					personService.addPerson(person);
					result.put("success", true);
				} else {
					result.put("success", true);
					result.put("errorMsg", "该人员名被使用");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("保存用户信息错误",e);
			result.put("success", true);
			result.put("errorMsg", "对不起，操作失败");
		}
		WriterUtil.write(response, result.toString());
	}
	
	
	@RequestMapping("deleteUser")
	public void delUser(HttpServletRequest request,HttpServletResponse response){
		JSONObject result=new JSONObject();
		try {
			String[] ids=request.getParameter("ids").split(",");
			for (String id : ids) {
//				userService.deleteUser(Integer.parseInt(id));
				personService.delPerson(Long.parseLong(id));
			}
			result.put("success", true);
			result.put("delNums", ids.length);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("删除用户信息错误",e);
			result.put("errorMsg", "对不起，删除失败");
		}
		WriterUtil.write(response, result.toString());
	}

	@RequestMapping("importPerson") //multipart/form-data
	public void importPerson(MultipartFile personFile, HttpServletRequest request,HttpServletResponse response){
		JSONObject result=new JSONObject();
		try {
			
			//导入  
			
			personService.importPerson(personFile);
			
			result.put("success", true);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("删除用户信息错误",e);
			result.put("errorMsg", "对不起，删除失败");
		}
		WriterUtil.write(response, result.toString());
	}

	@RequestMapping("exportPerson")
	public void exportPerson(PersonVo personVo,HttpServletRequest request,HttpServletResponse response){
		JSONObject result=new JSONObject();
		try {
			//导出：数据库里面的数据查询出来，导出Excel文件
			//1、查询需要导出的数据
			
			
			//只导出性别是男的   并且  公司是圆通快递
			
			personVo.setSex("男");
//			long typeId = 1;
//			personVo.setExpressTypeId(typeId);
			
			List<PersonVo> list = personService.findList(personVo);//条件查询
			
			//2、把查询到的数据写入到excel
			//创建workBook对象
			Workbook wb = new HSSFWorkbook();//03版本
			//创建sheet对象
			Sheet sheet = wb.createSheet();
			//创建行
			//创建表头
			Row firtsRow = sheet.createRow(0);
			String[] headers = {"用户编号","人员名字","人员性别","人员特点","入职时间","所属公司","创建时间"};
			for (int i = 0; i < headers.length; i++) {
				firtsRow.createCell(i).setCellValue(headers[i]);
			}
			
			
			//写入查询到的数据
			for (int i = 0; i < list.size(); i++) {
				//值是从list元素里面获取
				PersonVo vo = list.get(i);
				
				
				//先创建行对象
				Row row = sheet.createRow(i+1);
				row.createCell(0).setCellValue(vo.getId());
				row.createCell(1).setCellValue(vo.getExpressName());
				row.createCell(2).setCellValue(vo.getSex());
				row.createCell(3).setCellValue(vo.getExpressTrait());
				row.createCell(4).setCellValue(TimeUtil.formatTime(vo.getEntryTime(), "yyyy-MM-dd"));
				row.createCell(5).setCellValue(vo.getCname());
				row.createCell(6).setCellValue(TimeUtil.formatTime(vo.getCreateTime(), "yyyy-MM-dd"));
			}
			
			//把workbook对象，写入到磁盘
			//写出文件（path为文件路径含文件名）
			OutputStream os;
			File file = new File("C:\\Users\\Administrator\\Desktop\\H1910B\\day11"+File.separator+"person.xls");
			
			if (!file.exists()){//若此目录不存在，则创建之  
				file.createNewFile();  
				logger.debug("创建文件夹路径为："+ file.getPath());  
            } 
			os = new FileOutputStream(file);
			wb.write(os);
			os.close();
			result.put("success", true);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("导出人员信息错误",e);
			result.put("errorMsg", "对不起，导出失败");
		}
		WriterUtil.write(response, result.toString());
	}

	@RequestMapping("countPerson")
	public void countPerson(HttpServletRequest request,HttpServletResponse response){
		JSONObject result=new JSONObject();
		try {
			
			List<PersonVo> list = personService.countPerson();
			
			result.put("success", true);
			result.put("data", list);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("统计信息错误",e);
			result.put("errorMsg", "对不起，统计失败");
		}
		WriterUtil.write(response, result.toString());
	}

	
	@RequestMapping("editPassword")
	public void editPassword(HttpServletRequest request,HttpServletResponse response){
		JSONObject result=new JSONObject();
		String oldpassword = request.getParameter("oldpassword");
		String newpassword = request.getParameter("newpassword");
		HttpSession session = request.getSession();
		User currentUser = (User) session.getAttribute("currentUser");
		if(currentUser.getPassword().equals(oldpassword)){
			User user = new User();
			user.setUserid(currentUser.getUserid());
			user.setPassword(newpassword);
			try {
				userService.updateUser(user);
				currentUser.setPassword(newpassword);
				session.removeAttribute("currentUser"); 
				session.setAttribute("currentUser", currentUser);
				result.put("success", true);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("修改密码错误",e);
				result.put("errorMsg", "对不起，修改密码失败");
			}
		}else{
			logger.error(currentUser.getUsername()+"修改密码时原密码输入错误！");
			result.put("errorMsg", "对不起，原密码输入错误！");
		}
		WriterUtil.write(response, result.toString());
	}
}
