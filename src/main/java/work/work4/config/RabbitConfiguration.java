    package work.work4.config;

    import org.springframework.amqp.core.*;
    import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
    import org.springframework.amqp.support.converter.MessageConverter;
    import org.springframework.beans.factory.annotation.Qualifier;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;

    @Configuration
    public class RabbitConfiguration {
        // 队列名称
        public static final String CHAT_QUEUE = "chat.message.queue";
        // 交换机名称
        public static final String CHAT_EXCHANGE = "chat.message.exchange";
        // 路由键
        public static final String CHAT_ROUTING_KEY = "chat.message";

        @Bean("chatQueue")
        public Queue chatQueue() {
            return QueueBuilder.durable(CHAT_QUEUE)
                    .withArgument("x-max-length", 10000)
                    .ttl(5000)
                    .build();
        }

        @Bean("chatExchange")
        public DirectExchange chatExchange() {
            return new DirectExchange(CHAT_EXCHANGE);
        }

        @Bean("binding")
        public Binding chatBinding(@Qualifier("chatExchange") Exchange exchange,
                                   @Qualifier("chatQueue") Queue queue) {
            return BindingBuilder.bind(queue)
                    .to(exchange)
                    .with(CHAT_ROUTING_KEY)
                    .noargs();
        }

        @Bean("jacksonConverter")
        public MessageConverter converter(){
            // 使用面向未来的新转换器，完美消除弃用警告
            return new JacksonJsonMessageConverter();
        }
    }