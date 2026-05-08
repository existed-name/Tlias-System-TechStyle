package com.github.existed_name.tliaswebmanagement.interceptor;

import com.github.existed_name.tlias_util.CurrentHolder;
import com.github.existed_name.tlias_util.JwtUtils;
import com.github.existed_name.tliaswebmanagement.exception.BusinessException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class TokenInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle( HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1. 获取请求url。
        String url = request.getRequestURL().toString();

        //2. 判断请求url中是否包含login，如果包含，说明是登录操作，放行。
//        if(url.contains("login")){ //登录请求
//            log.info("登录请求 , 直接放行");
//            return true;
//        }
        // 👆 会误判 /logs/login
        String uri = request.getRequestURI();
        if ("/login".equals(uri)) {
            log.info("登录请求 , 直接放行");
            return true;
        }

        //3. 获取请求头中的令牌（token）。
        String jwt = request.getHeader("token");

        //4. 判断令牌是否存在，如果不存在，返回错误结果（未登录）。
        if(!StringUtils.hasLength(jwt)){ //jwt为空
            log.warn("获取到jwt令牌为空, 返回错误结果");
            response.setStatus( HttpStatus.SC_UNAUTHORIZED);
            throw new BusinessException( "检测到您未登录, 请登录后再进行操作~~~" );
        }

        //5. 解析token，如果解析失败，返回错误结果（未登录）。
        try {
            Claims claims = JwtUtils.parseJWT( jwt );
            Integer empId = Integer.valueOf(claims.get("id").toString());
            CurrentHolder.setCurrentId(empId);
        } catch (Exception e) {
            log.warn( "解析令牌失败, 返回错误结果", e );
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            // 实际场景：token 过期、被篡改、格式错误 → 都是"未登录"的业务场景
            throw new BusinessException("登录状态失效，请重新登录");
        }

        //6. 放行
        log.info("令牌合法, 放行");
        // ❌ 不要在这里 remove()！！因为preHandle是在Controller执行之前就执行了
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // ✅ 在整个请求结束后（包括 AOP 和 Controller 执行完），再清理线程数据
        //7. 清空当前线程绑定的id
        log.info("请求结束，清理 ThreadLocal 数据");
        CurrentHolder.remove();
    }
}