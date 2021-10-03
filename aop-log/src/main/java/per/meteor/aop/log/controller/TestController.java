package per.meteor.aop.log.controller;

import cn.hutool.db.Page;
import org.springframework.web.bind.annotation.*;
import per.meteor.aop.log.common.annotations.OperationLog;
import per.meteor.aop.log.common.enums.OperationType;

import javax.servlet.http.HttpServletRequest;

/**
 * @author meteor
 * @date 2021-10-04 0:14
 */
@RestController
public class TestController {

    @OperationLog(module = "测试", operationType = OperationType.OTHER, desc = "测试")
    @GetMapping("/test01/{sex}")
    public void test01(@RequestParam String name, Integer age, @PathVariable String sex, HttpServletRequest request) {
        // pass
    }

    @OperationLog(module = "测试", operationType = OperationType.OTHER, desc = "测试")
    @PostMapping("/test02")
    public void test01(@RequestBody Page page, @RequestParam String name, HttpServletRequest request) {
        // pass
    }
}
