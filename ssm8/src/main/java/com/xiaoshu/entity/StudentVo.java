package com.xiaoshu.entity;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

public class StudentVo extends Student {

	private String cname;
	
	private Integer num;
	
	private String key;
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date entrytime1;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date entrytime2;

	public String getCname() {
		return cname;
	}

	public void setCname(String cname) {
		this.cname = cname;
	}

	public Date getEntrytime1() {
		return entrytime1;
	}

	public void setEntrytime1(Date entrytime1) {
		this.entrytime1 = entrytime1;
	}

	public Date getEntrytime2() {
		return entrytime2;
	}

	public void setEntrytime2(Date entrytime2) {
		this.entrytime2 = entrytime2;
	}

}
