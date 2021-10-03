package per.meteor.aop.log.common.annotations;


import per.meteor.aop.log.common.enums.OperationType;

import java.lang.annotation.*;

/**
 * @author meteor
 * @date 2021-10-03 3:39
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    /**
     * 模块
     * @return ""
     */
    String module() default "";

    /**
     * 操作类型
     * @return ""
     */
    OperationType operationType() default OperationType.OTHER;
    /**
     * 描述
     * @return ""
     */
    String desc() default "";
}
