package com.zhongan.ifrs17agentjava.context;

public class UserContext {
    // 使用 ThreadLocal 保证同一次请求线程内，数据共享
    private static final ThreadLocal<String> ORG_CODE = new ThreadLocal<>();

    public static void setOrgCode(String orgCode) { ORG_CODE.set(orgCode); }
    public static String getOrgCode() { return ORG_CODE.get(); }
    public static void clear() { ORG_CODE.remove(); }
}