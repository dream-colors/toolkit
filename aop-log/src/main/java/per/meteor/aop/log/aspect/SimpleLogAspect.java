package per.meteor.aop.log.aspect;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.servlet.ServletUtil;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;
import per.meteor.aop.log.common.annotations.OperationLog;
import per.meteor.aop.log.common.utils.JsonUtil;
import per.meteor.aop.log.domain.LogDomain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author meteor
 * @date 2021-10-03 18:23
 */
@Aspect
@Component
@Slf4j
public class SimpleLogAspect {

    private static final String SYSTEM_VERSION = "1.0";

    /**
     * 设置操作日志切入点 记录操作日志 在注解的位置切入代码
      */
    @Pointcut("@annotation(per.meteor.aop.log.common.annotations.OperationLog)")
    public void optLogPointCut() {
        // pass
    }

    /**
    * 设置操作异常切入点记录异常日志 扫描所有controller包下操作
    */
    @Pointcut("execution(* per.meteor.aop.log.controller..*.*(..))")
    public void optExceptionLogPointCut() {
        // pass
    }

    /**
     * 处理正常请求日志
     * @author meteor
     * @date 2021-10-03 22:13
     * @param joinPoint 切点
     * @param result 处理结果
     */
    @AfterReturning(value = "optLogPointCut()", returning = "result")
    public void saveOperationLog(JoinPoint joinPoint, Object result) {
        LogDomain logDomain = new LogDomain(joinPoint)
                .request(this::parseRequestInfo)
                .operation(this::parseOperationInfo)
                .browser(this::parseBrowserInfo)
                .success(result, this::parseResponseInfo);

        // 根据自身操作对日志进行处理, 进行异步任务数据库存储，或存储到文件
        log.info("{}", logDomain);
    }

    /**
     * 处理正常请求日志
     * @author meteor
     * @date 2021-10-03 22:13
     * @param joinPoint 切点
     * @param e 异常信息
     */
    @AfterThrowing(pointcut = "optExceptionLogPointCut()", throwing = "e")
    public void saveExceptionLog(JoinPoint joinPoint, Throwable e) {
        LogDomain logDomain = new LogDomain(joinPoint)
                .request(this::parseRequestInfo)
                .operation(this::parseOperationInfo)
                .browser(this::parseBrowserInfo)
                .error(e, (error, log) -> {
                    log.setExceptionName(e.getClass().getName());
                    log.setExceptionMessage(stackTraceToString(e.getClass().getName(), e.getMessage(), e.getStackTrace()));
                    return log;
                });
        // 根据自身操作对日志进行处理, 进行异步任务数据库存储，或存储到文件
        log.info("{}", logDomain);
    }

    /**
     * 解析请求信息
     *
     * @author meteor
     * @date 2021-10-03 22:12
     * @param joinPoint 切点
     * @param logDomain 日志信息
     * @return per.meteor.easy.code.common.domain.LogDomain
     */
    private LogDomain parseRequestInfo(JoinPoint joinPoint, LogDomain logDomain) {
        // 请求信息
        LocalDateTime now = LocalDateTime.now();
        // 获取HttpServletRequest
        HttpServletRequest request = getRequest();
        assert request != null;

        // 封装基本请求信息WebConfig
        logDomain.setRequestParameter(getRequestParameters(joinPoint));
        logDomain.setRequestUrl(request.getRequestURI());
        logDomain.setRequestMethod(request.getMethod());
        logDomain.setRequestTime(now);

        log.info("请求路径: {}  {}", request.getMethod(), request.getRequestURI());
        log.info("请求参数:{}", getRequestParameters(joinPoint));

        logDomain.setRequestHeader(JsonUtil.toJSONString(parseHeader()));
        logDomain.setRequestIp(ServletUtil.getClientIP(request));
        // 从切面织入点处通过反射机制获取织入点处的方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getName();
        String methodPath = className + "." + signature.getMethod().getName();
        logDomain.setRequestMethodPath(methodPath);

        return logDomain;
    }

    /**
     *
     * @author meteor
     * @date 2021-10-03 21:54
     * @param result 响应结果
     * @param logDomain 日志信息
     * @return per.meteor.easy.code.common.domain.LogDomain
     */
    private LogDomain parseResponseInfo(Object result, LogDomain logDomain) {

        logDomain.setResponseBody(JsonUtil.toJSONString(result));
        LocalDateTime requestTime = logDomain.getRequestTime();
        long timeConsuming = requestTime == null ? 0 : Duration.between(requestTime, LocalDateTime.now()).toMillis();
        logDomain.setRequestTimeConsuming(timeConsuming + "ms");
        return logDomain;
    }

    /**
     * 解析请求头信息
     *
     * @author meteor
     * @date 2021-10-03 21:37
     * @return java.util.HashMap<java.lang.String,java.lang.String>
     */
    private HashMap<String, String> parseHeader() {
        HttpServletRequest request = getRequest();
        assert request != null;
        HashMap<String, String> hashMap = new HashMap<>(16);
        Enumeration<String> er = request.getHeaderNames();
        while(er.hasMoreElements()){
            String name	= er.nextElement();
            String value = request.getHeader(name);
            hashMap.put(name, value);
        }

        return hashMap;
    }

    /**
     * 解析浏览器信息
     * @author meteor
     * @date 2021-10-03 21:28
     * @param logDomain  请求对象
     * @return per.meteor.easy.code.common.domain.LogDomain
     */
    private LogDomain parseBrowserInfo(LogDomain logDomain) {
        // 浏览器信息
        HttpServletRequest request = getRequest();
        assert request != null;

        String header = request.getHeader("User-Agent");
        UserAgent userAgent = UserAgent.parseUserAgentString(header);
        String browserVersion = userAgent.getBrowserVersion() == null ? null : userAgent.getBrowserVersion().getVersion();
        String operatingSystem = userAgent.getOperatingSystem().getName();
        logDomain.setUserAgent(userAgent.toString());
        logDomain.setBrowserVersion(browserVersion);
        logDomain.setOperatingSystem(operatingSystem);

        Browser browser = userAgent.getBrowser();
        if (null != browser) {
            logDomain.setBrowserName(browser.getName());
            logDomain.setBrowserType(browser.getBrowserType().getName());
        }

        return logDomain;
    }

    /**
     * 解析操作信息
     * @author meteor
     * @date 2021-10-03 21:28
     * @param joinPoint 切入点
     * @param logDomain 日志信息
     * @return per.meteor.easy.code.common.domain.LogDomain
     */
    private LogDomain parseOperationInfo(JoinPoint joinPoint, LogDomain logDomain) {
        // 从切面织入点处通过反射机制获取织入点处的方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取切入点所在的方法
        Method method = signature.getMethod();

        OperationLog annotation = method.getAnnotation(OperationLog.class);
        if (null != annotation) {
            logDomain.setOperationModule(annotation.module());
            logDomain.setOperationType(annotation.operationType().name());
            logDomain.setOperationDesc(annotation.desc());
            logDomain.setOperationUser("admin");
            logDomain.setSystemVersion(SYSTEM_VERSION);
        }

        return logDomain;
    }


    /**
     * 获取请求对象
     * @return javax.servlet.http.HttpServletRequest
     * @author meteor
     * @since 2020/12/5 18:30
     */
    private HttpServletRequest getRequest() {
        // 获取RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        // 从获取RequestAttributes中获取HttpServletRequest的信息
        assert requestAttributes != null;
        return (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
    }

    /**
     * 获取请求的参数，放到log中
     *
     * @param joinPoint 切点
     */
    private String getRequestParameters(JoinPoint joinPoint) {

        HttpServletRequest request = getRequest();
        assert request != null;
        String requestMethod = request.getMethod();
        String params;
        if (HttpMethod.PUT.name().equals(requestMethod) || HttpMethod.POST.name().equals(requestMethod)) {
            // 由于args为参数数组，不带参数名，所以封装参数名及参数类型的Map,进行后续参数封装处理
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            HashMap<String, Class<?>> keyTypeMap = new HashMap<>(16);
            String[] names = signature.getParameterNames();
            for (int i = 0; i < names.length; i++) {
                keyTypeMap.put(names[i], signature.getParameterTypes()[i]);
            }
            params = JsonUtil.toJSONString(argsArrayToString(keyTypeMap, joinPoint.getArgs()));
        } else {
            // 获取一般请求参数
            HashMap<String, Object> paramsMap = new HashMap<>(16);
            Enumeration<String> parameterNames = request.getParameterNames();
            while(parameterNames.hasMoreElements()) {
                String paramName = parameterNames.nextElement();
                paramsMap.put(paramName, request.getParameter(paramName));
            }
            // 获取路径参数
            HashMap<?, ?> pathParamsMap = (HashMap<?, ?>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            pathParamsMap.keySet().forEach(key -> paramsMap.put((String) key, pathParamsMap.get(key)));
            params = CharSequenceUtil.sub(JsonUtil.toJSONString(paramsMap), 0, 2000);
        }


        return params;
    }

    /**
     * 参数拼装
     * @author meteor
     * @date 2021-10-03 23:47
     * @param paramsArray  参数
     * @return java.lang.String
     */
    private HashMap<String, Object> argsArrayToString(HashMap<String, Class<?>> paramNameType, Object[] paramsArray) {
        assert paramsArray != null;
        HashMap<String, Object> argsMap = new HashMap<>(16);
        for (Object o : paramsArray) {
            if (!isFilterObject(o)) {
                for (Map.Entry<String, Class<?>> entry : paramNameType.entrySet()) {
                    if (o.getClass().equals(entry.getValue())) {
                        argsMap.put(entry.getKey(), o);
                        break;
                    }
                }
            }
        }
        return argsMap;
    }

    /**
     * 判断是否需要过滤的对象。
     *
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    @SuppressWarnings("rawtypes")
    private boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            Iterator iterator = collection.iterator();
            if (iterator.hasNext()) {
                return iterator.next() instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            Iterator iterator = map.entrySet().iterator();
            if (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }

    /**
     * 转换异常信息为字符串
     *
     * @param exceptionName    异常名称
     * @param exceptionMessage 异常信息
     * @param elements         堆栈信息
     */
    private String stackTraceToString(String exceptionName, String exceptionMessage, StackTraceElement[] elements) {
        StringBuilder stringBuffer = new StringBuilder();
        for (StackTraceElement stet : elements) {
            stringBuffer.append(stet).append("\n");
        }
        return exceptionName + ":" + exceptionMessage + "\n" + stringBuffer;
    }

}
