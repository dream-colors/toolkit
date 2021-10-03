package per.meteor.aop.log.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;

import java.time.LocalDateTime;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

/**
 * @author meteor
 * @date 2021-10-03 3:37
 */
@Data
@Aspect
public class LogDomain {

    /** 主键id **/
    private String logId;
    /** 日志流水 **/
    private String logSerialNumber;

    /** 请求参数 **/
    private String requestParameter;
    /** 请求地址 **/
    private String requestUrl;
    /** 请求方式 **/
    private String requestMethod;
    /** 请求时间 **/
    private LocalDateTime requestTime;

    /** 响应状态:  success、failure **/
    private String responseStatus;
    /** 响应状态码 **/
    private Integer responseCode;
    /** 响应数据 **/
    private String responseBody;
    /** 响应时间 **/
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss", timezone = "GMT+8")
    private LocalDateTime responseTime;

    /* 异常记录 */
    /** 异常名称 **/
    private String exceptionName;
    /** 异常信息 **/
    private String exceptionMessage;

    /* 其他信息 **/

    /** 请求ip **/
    private String requestIp;
    /** 请求头 **/
    private String requestHeader;
    /** 请求方法名 **/
    private String requestMethodPath;
    /** 请求耗时 **/
    private String requestTimeConsuming;


    /** 请求头 **/
    private String userAgent;
    /** 浏览器类型 **/
    private String browserType;
    /** 浏览器名称 **/
    private String browserName;
    /** 浏览器版本 **/
    private String browserVersion;
    /** 操作系统 **/
    private String operatingSystem;


    /** 操作人员 **/
    private String operationUser;
    /** 系统版本 **/
    private String systemVersion;


    /** 操作名称 **/
    private String operationModule;
    /** 操作类型 **/
    private String operationType;
    /** 操作描述 **/
    private String operationDesc;

    private JoinPoint joinPoint;
    public LogDomain(JoinPoint joinPoint) {
        this.joinPoint = joinPoint;
    }

    public LogDomain request(BiFunction<JoinPoint, LogDomain, LogDomain> function) {
        return function.apply(joinPoint, this);
    }

    public LogDomain success(Object result, BiFunction<Object, LogDomain, LogDomain> function) {
        this.setResponseCode(200);
        this.setResponseStatus("success");
        return function.apply(result, this);
    }

    public LogDomain error(Throwable e, BiFunction<Object, LogDomain, LogDomain> function) {
        this.setResponseCode(500);
        this.setResponseStatus("failure");
        return function.apply(e, this);
    }

    public LogDomain browser(UnaryOperator<LogDomain> function) {
        return function.apply(this);
    }

    public LogDomain operation(BiFunction<JoinPoint, LogDomain, LogDomain> function) {
        return function.apply(joinPoint, this);
    }

    public LogDomain system(UnaryOperator<LogDomain> function) {
        return function.apply(this);
    }
}
