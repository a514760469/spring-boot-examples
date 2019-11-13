package com.neo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neo.model.User;
import com.neo.rabbit.consumer.Order;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RabbitMQApplicationTests {

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Test
	public void contextLoads() {
		System.out.println("hello world");
	}

	@Test
	public void consumerTest() throws JsonProcessingException {

		Order order = new Order();
		order.setId(1);
		order.setUserId(1000);
		order.setAmout(88d);
		order.setTime(LocalDateTime.now().toString());


        Order order2 = new Order();
        order2.setId(2);
        order2.setUserId(2000);
        order2.setAmout(99d);
        order2.setTime(LocalDateTime.now().toString());

        List<Order> orderList = Arrays.asList(order, order2);

		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(orderList);
		System.out.println(json);

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);

        Message message = new Message(json.getBytes(), messageProperties);
		rabbitTemplate.convertAndSend("", "so.order", message);

	}

    /**
     * autoConvert Order
     */
	@Test
	public void consumerTest2() throws JsonProcessingException {

		Order order = new Order();
		order.setId(1);
		order.setUserId(1000);
		order.setAmout(88d);
		order.setTime(LocalDateTime.now().toString());


		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(order);
		System.out.println(json);

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        // 生产者在发送json数据的时候，需要指定这个json是哪个对象，否则消费者收到消息之后，不知道要转换成哪个java对象
//        messageProperties.getHeaders().put("__TypeId__", "com.neo.rabbit.consumer.Order");// 增加了耦合度
        messageProperties.getHeaders().put("__TypeId__", "Order");// 不需要指定具体类名

        Message message = new Message(json.getBytes(), messageProperties);
		rabbitTemplate.convertAndSend("so.order", message);
	}

    @Test
	public void consumerTestSendUser() throws JsonProcessingException {

        User user = new User();
        user.setName("zhangsan");
        user.setPass("zsPass");
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(user);
		System.out.println(json);

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        // TypeId的值可以是java对象全称，也可以是映射的key. 如果消费者没有配置映射key，则只能指定java对象全称
        messageProperties.getHeaders().put("__TypeId__", "User");// 不需要指定具体类名

        Message message = new Message(json.getBytes(), messageProperties);
		rabbitTemplate.convertAndSend("so.order", message);
	}

    /**
     * 如果生产者发送的是list的json数据，则还需要增加一个__ContentTypeId__的header，
     * 用于指明List里面的具体对象
     */
    @Test
	public void consumerTestList() throws JsonProcessingException {

        Order order = new Order();
        order.setId(1);
        order.setUserId(1000);
        order.setAmout(88d);
        order.setTime(LocalDateTime.now().toString());


        Order order2 = new Order();
        order2.setId(2);
        order2.setUserId(2000);
        order2.setAmout(99d);
        order2.setTime(LocalDateTime.now().toString());

        List<Order> orderList = Arrays.asList(order, order2);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(orderList);
        System.out.println(json);

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
//        messageProperties.getHeaders().put("__TypeId__", "User");// 不需要指定具体类名
        // 发送List时，指定具体的对象
        messageProperties.getHeaders().put("__contentTypeId__", "order");

        Message message = new Message(json.getBytes(), messageProperties);
		rabbitTemplate.convertAndSend("so.order", message);
	}

    /**
     * 如果生产者发送的是map的json数据，
     * 则需要指定__KeyTypeId__，__ContentTypeId__的header，
     * 用于指明map里面的key，value的具体对象。
     */
    @Test
	public void consumerTestMap() throws JsonProcessingException {

        Order order = new Order();
        order.setId(1);
        order.setUserId(1000);
        order.setAmout(88d);
        order.setTime(LocalDateTime.now().toString());


        Order order2 = new Order();
        order2.setId(2);
        order2.setUserId(2000);
        order2.setAmout(99d);
        order2.setTime(LocalDateTime.now().toString());

        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("10", order);
        objectMap.put("20", order2);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(objectMap);
        System.out.println(json);

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        // 发送List时，指定具体的类型
        messageProperties.getHeaders().put("__contentTypeId__", "order");
        // 发送Map时，指定key的具体类型
        messageProperties.getHeaders().put("__KeyTypeId__", "string");

        Message message = new Message(json.getBytes(), messageProperties);
		rabbitTemplate.convertAndSend("so.order", message);
	}


    @Test
    public void sendJpg() throws Exception{
        byte[] body = Files.readAllBytes(Paths.get("C:/Users/Public/Pictures/Sample Pictures","企鹅.jpg"));

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType("image/jpg");

        Message message = new Message(body, messageProperties);

        rabbitTemplate.send("so.order", message);
    }
}
