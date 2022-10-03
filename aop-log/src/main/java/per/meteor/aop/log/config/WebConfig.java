package per.meteor.aop.log.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import per.meteor.aop.log.common.constant.DatePattern;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;

/**
 * @author meteor
 * @date 2021-10-02 2:27
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 处理jackJson的时区处理问题，默认ISO会缺少时间
     *
     * @return com.fasterxml.jackson.databind.ObjectMapper
     * @author meteor
     * @date 2021-10-02 2:39
     */
    @Bean
    public ObjectMapper jacksonObjectMapperCustomization() {
        SimpleDateFormat format = new SimpleDateFormat(DatePattern.NORM_DATETIME_PATTERN);
        TimeZone timeZone = TimeZone.getTimeZone(ZoneId.of("GMT+8"));
        format.setTimeZone(timeZone);

        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder()
                .timeZone(timeZone)
                .dateFormat(format);

        return builder.build();
    }

    /**
     * 添加自定义消息转换器
     *
     * @param converters 转换列表
     * @author meteor
     * @date 2021-10-02 2:41
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.removeIf(MappingJackson2HttpMessageConverter.class::isInstance);
        converters.add(new MappingJackson2HttpMessageConverter(jacksonObjectMapperCustomization()));
    }

    /**
     * 配置localDate转换器
     *
     * @return org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
     * @author meteor
     * @date 2021-10-02 2:39
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer enumCustomizer() {
        return builder -> builder
                .deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)))
                .deserializerByType(LocalDate.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)))
                .serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)))
                .serializerByType(LocalDate.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)));
    }
}
