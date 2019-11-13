package com.neo.rabbit.consumer;

import com.neo.model.User;
import com.neo.rabbit.message.convert.JPEGMessageConverter;
import com.neo.rabbit.message.convert.TextMessageConverter;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.ContentTypeDelegatingMessageConverter;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhanglifeng
 * @date 2019/11/12/0012
 */
@Configuration
public class MQConfig {

//    @Bean
//    public ConnectionFactory connectionFactory(){
//        CachingConnectionFactory factory = new CachingConnectionFactory();
//        factory.setUri("amqp://zhihao.miao:123456@192.168.1.131:5672");
//        return factory;
//    }
//
//    @Bean
//    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory){
//        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
//        return rabbitAdmin;
//    }
//
//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
//        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//        return rabbitTemplate;
//    }

    @Bean
    public SimpleMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames("so.order");

        MessageListenerAdapter adapter = new MessageListenerAdapter(new MessageHandler());

        // 指定Json转换器
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper javaTypeMapper = new DefaultJackson2JavaTypeMapper();
        Map<String, Class<?>> idClassMap = new HashMap<>();
        idClassMap.put("Order", Order.class);
        idClassMap.put("User", User.class);
        javaTypeMapper.setIdClassMapping(idClassMap);


        jackson2JsonMessageConverter.setJavaTypeMapper(javaTypeMapper);
//        adapter.setMessageConverter(jackson2JsonMessageConverter);


        ContentTypeDelegatingMessageConverter contentTypeDelegatingMessageConverter =
                new ContentTypeDelegatingMessageConverter();
//        contentTypeDelegatingMessageConverter.addDelegate(MessageProperties.CONTENT_TYPE_TEXT_PLAIN, );
        TextMessageConverter textMessageConverter = new TextMessageConverter();
        JPEGMessageConverter jpegMessageConverter = new JPEGMessageConverter();
        contentTypeDelegatingMessageConverter.addDelegate(MessageProperties.CONTENT_TYPE_JSON, jackson2JsonMessageConverter);
        contentTypeDelegatingMessageConverter.addDelegate(MessageProperties.CONTENT_TYPE_TEXT_PLAIN, textMessageConverter);
        contentTypeDelegatingMessageConverter.addDelegate("image/jpg", jpegMessageConverter);
        contentTypeDelegatingMessageConverter.addDelegate("image/jpeg", jpegMessageConverter);
        contentTypeDelegatingMessageConverter.addDelegate("image/png", jpegMessageConverter);

        adapter.setMessageConverter(contentTypeDelegatingMessageConverter);

        // 设置处理器的消费消息的默认方法
        adapter.setDefaultListenerMethod("onMessage");
        container.setMessageListener(adapter);

        return container;
    }

}
