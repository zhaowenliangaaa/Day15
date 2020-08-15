package com.xiaoshu.service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import com.alibaba.fastjson.JSONObject;
import com.xiaoshu.entity.Person;

import redis.clients.jedis.Jedis;

public class MyMessageListener implements MessageListener{

	@Override
	public void onMessage(Message message) {
		// TODO Auto-generated method stub
		
		
		try {
			TextMessage msg = (TextMessage)message;
			String json = msg.getText();//获取消息内容
			
			System.out.println("人员信息: "+json);
			
			Person person = JSONObject.parseObject(json, Person.class);
			
			Jedis jedis = new Jedis("localhost", 6379);
			jedis.set(person.getExpressName(), person.getId()+"");
			
			
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
