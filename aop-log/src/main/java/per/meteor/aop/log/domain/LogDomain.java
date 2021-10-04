package per.meteor.aop.log.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import per.meteor.aop.log.common.constant.DatePattern;
import per.meteor.aop.log.common.enums.ModuleType;
import per.meteor.aop.log.common.enums.OperationType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

/**
 * @author meteor
 * @date 2021-10-03 3:37
 */
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
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss", timezone = "GMT+8")
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
    private ModuleType operationModule;
    /** 操作类型 **/
    private OperationType operationType;
    /** 操作描述 **/
    private String operationDesc;

    private final JoinPoint joinPoint;
    public LogDomain(JoinPoint joinPoint) {
        this.joinPoint = joinPoint;
    }

    public LogDomain request(BiFunction<JoinPoint, LogDomain, LogDomain> function) {
        return function.apply(joinPoint, this);
    }

    public LogDomain success(Object result, BiFunction<Object, LogDomain, LogDomain> function) {
        this.setResponseCode(200);
        this.setResponseStatus("success");
        this.setLogSerialNumber(createSerialNumber());
        this.setResponseTime(LocalDateTime.now());
        return function.apply(result, this);
    }

    public LogDomain error(Throwable e, BiFunction<Object, LogDomain, LogDomain> function) {
        this.setResponseCode(500);
        this.setResponseStatus("failure");
        this.setLogSerialNumber(createSerialNumber());
        this.setResponseTime(LocalDateTime.now());
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

    /**
     * 流水
     * @author meteor
     * @date 2021-10-04 15:26
     * @return java.lang.String
     */
    private String createSerialNumber() {
        return "" + operationModule.ordinal() +
                operationType.ordinal() +
                requestTime.format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN)) +
                createRandomNumber();
    }

    /**
     * 2位数随机数
     * @author meteor
     * @date 2021-10-04 16:25
     * @return java.lang.String
     */
    private String createRandomNumber() {
        int number = new Random().nextInt(99);
        return number < 10 ? "0" + number : "" + number;
    }


    /*----------------------------------getter及setter ---------------------------------------------*/

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getLogSerialNumber() {
        return logSerialNumber;
    }

    public void setLogSerialNumber(String logSerialNumber) {
        this.logSerialNumber = logSerialNumber;
    }

    public String getRequestParameter() {
        return requestParameter;
    }

    public void setRequestParameter(String requestParameter) {
        this.requestParameter = requestParameter;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public LocalDateTime getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(LocalDateTime responseTime) {
        this.responseTime = responseTime;
    }

    public String getExceptionName() {
        return exceptionName;
    }

    public void setExceptionName(String exceptionName) {
        this.exceptionName = exceptionName;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public String getRequestIp() {
        return requestIp;
    }

    public void setRequestIp(String requestIp) {
        this.requestIp = requestIp;
    }

    public String getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(String requestHeader) {
        this.requestHeader = requestHeader;
    }

    public String getRequestMethodPath() {
        return requestMethodPath;
    }

    public void setRequestMethodPath(String requestMethodPath) {
        this.requestMethodPath = requestMethodPath;
    }

    public String getRequestTimeConsuming() {
        return requestTimeConsuming;
    }

    public void setRequestTimeConsuming(String requestTimeConsuming) {
        this.requestTimeConsuming = requestTimeConsuming;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getBrowserType() {
        return browserType;
    }

    public void setBrowserType(String browserType) {
        this.browserType = browserType;
    }

    public String getBrowserName() {
        return browserName;
    }

    public void setBrowserName(String browserName) {
        this.browserName = browserName;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    public void setBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getOperationUser() {
        return operationUser;
    }

    public void setOperationUser(String operationUser) {
        this.operationUser = operationUser;
    }

    public String getSystemVersion() {
        return systemVersion;
    }

    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
    }

    public ModuleType getOperationModule() {
        return operationModule;
    }

    public void setOperationModule(ModuleType operationModule) {
        this.operationModule = operationModule;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public String getOperationDesc() {
        return operationDesc;
    }

    public void setOperationDesc(String operationDesc) {
        this.operationDesc = operationDesc;
    }

    @Override
    public String toString() {
        return "LogDomain{" +
                "logId='" + logId + '\'' +
                ", logSerialNumber='" + logSerialNumber + '\'' +
                ", requestParameter='" + requestParameter + '\'' +
                ", requestUrl='" + requestUrl + '\'' +
                ", requestMethod='" + requestMethod + '\'' +
                ", requestTime=" + requestTime +
                ", responseStatus='" + responseStatus + '\'' +
                ", responseCode=" + responseCode +
                ", responseBody='" + responseBody + '\'' +
                ", responseTime=" + responseTime +
                ", exceptionName='" + exceptionName + '\'' +
                ", exceptionMessage='" + exceptionMessage + '\'' +
                ", requestIp='" + requestIp + '\'' +
                ", requestHeader='" + requestHeader + '\'' +
                ", requestMethodPath='" + requestMethodPath + '\'' +
                ", requestTimeConsuming='" + requestTimeConsuming + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", browserType='" + browserType + '\'' +
                ", browserName='" + browserName + '\'' +
                ", browserVersion='" + browserVersion + '\'' +
                ", operatingSystem='" + operatingSystem + '\'' +
                ", operationUser='" + operationUser + '\'' +
                ", systemVersion='" + systemVersion + '\'' +
                ", operationModule=" + operationModule +
                ", operationType=" + operationType +
                ", operationDesc='" + operationDesc + '\'' +
                ", joinPoint=" + joinPoint +
                '}';
    }
}
