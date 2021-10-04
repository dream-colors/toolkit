package per.meteor.aop.log.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import per.meteor.aop.log.domain.LogDomain;

/**
 * @author meteor
 * @date 2021-10-04 15:08
 */
@Service
public class LogServiceAsync {

    Logger logger = LoggerFactory.getLogger(LogServiceAsync.class);

    @Async
    public void saveOperationLogAsync(LogDomain logDomain) {
        logger.info("异步任务执行日志保存操作:{}", logDomain);
    }

    @Async
    public void saveExceptionLogAsync(LogDomain logDomain) {
        logger.info("异步任务执行日志保存操作:{}", logDomain);
    }
}
