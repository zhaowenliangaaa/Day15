package com.xiaoshu.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xiaoshu.dao.CourseMapper;
import com.xiaoshu.dao.StudentMapper;
import com.xiaoshu.entity.Course;
import com.xiaoshu.entity.Student;
import com.xiaoshu.entity.StudentVo;

import redis.clients.jedis.Jedis;

@Service
public class StudentService {
	@Autowired
	private StudentMapper studentMapper;
	
	@Autowired
	private CourseMapper courseMapper;
	
	
	public List<StudentVo> countStu(){
		return studentMapper.countStu();
	}
	
	
	public List<Course> findCourseAll(){
		return courseMapper.selectAll();
	}
	
	public PageInfo<StudentVo> findPage(StudentVo studentVo,Integer pageNum,Integer pageSize){
		PageHelper.startPage(pageNum, pageSize);
		List<StudentVo> list = studentMapper.findList(studentVo);
		return new PageInfo<StudentVo>(list);
	}
	
	public void addStu(Student student){
		student.setCreatetime(new Date());
		studentMapper.insert(student);
	}
	
	public void updateStu(Student student){
		studentMapper.updateByPrimaryKeySelective(student);
	}
	
	public Course findCourseByCode(String code){
		Course param = new Course();
		param.setCode(code);
		return courseMapper.selectOne(param );
	}
	
	public void addC(Course course){
		course.setCreatetime(new Date());
		courseMapper.addC(course);
		
//		添加到Redis
		Jedis jedis = new Jedis("localhost", 6379);
		
		jedis.hset("部门信息", course.getId()+"", JSONObject.toJSONString(course));
		
	}

}
