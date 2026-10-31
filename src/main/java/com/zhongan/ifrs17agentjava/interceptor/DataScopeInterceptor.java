package com.zhongan.ifrs17agentjava.interceptor;

import com.zhongan.ifrs17agentjava.context.UserContext;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.Properties;

@Component
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class DataScopeInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = boundSql.getSql();

        // 获取当前用户的机构代码
        String orgCode = UserContext.getOrgCode();

        // 简单粗暴的 SQL 拼接（企业级建议用 JSqlParser 解析 SQL 树）
        if (orgCode != null && sql != null && !sql.contains("org_code =")) {
            // 假设表里有个 org_code 字段。在 WHERE 后面或末尾追加条件
            if (sql.toLowerCase().contains("where")) {
                sql = sql + " AND org_code = '" + orgCode + "'";
            } else {
                sql = sql + " WHERE org_code = '" + orgCode + "'";
            }
            // 利用反射修改最终执行的 SQL
            java.lang.reflect.Field field = boundSql.getClass().getDeclaredField("sql");
            field.setAccessible(true);
            field.set(boundSql, sql);
            System.out.println(">>> [拦截器] 已自动注入数据权限隔离条件: " + sql);
        }

        return invocation.proceed();
    }
}