package com.awp.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 单页应用(SPA)路由兜底：
 * 前端用 history 模式，刷新 /home、/category 等路径时浏览器会直接请求服务器，
 * 这里把这些「无扩展名的单段路径」转发回 index.html，交给前端路由处理。
 *
 * 不影响：
 * - /api/**          多段路径，由各 RestController 处理
 * - /assets/x.js 等  含「.」的静态资源，由静态资源处理器直接返回
 */
@Controller
public class SpaForwardController {

    @RequestMapping(value = {"/", "/{path:[^\\.]*}"})
    public String forward() {
        return "forward:/index.html";
    }
}
