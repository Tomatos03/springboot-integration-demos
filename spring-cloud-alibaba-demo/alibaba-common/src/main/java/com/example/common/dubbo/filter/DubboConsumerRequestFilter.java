package com.example.common.dubbo.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;

import java.util.UUID;

/**
 * Dubbo 消费端请求拦截器
 * 在消费端（调用者）拦截 Dubbo 请求，记录请求日志并添加链路追踪 ID
 */
@Slf4j
@Activate(group = CommonConstants.CONSUMER)
public class DubboConsumerRequestFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 生成或获取 TraceId
        String traceId = RpcContext.getClientAttachment().getAttachment("traceId");
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
        }

        // 将 TraceId 添加到 RpcContext，传递给服务提供端
        RpcContext.getClientAttachment().setAttachment("traceId", traceId);

        // 获取调用信息
        String serviceName = invoker.getInterface().getSimpleName();
        String methodName = invocation.getMethodName();
        Object[] args = invocation.getArguments();

        // 构建参数字符串
        String paramsStr = formatParams(args);

        // 记录请求日志
        log.info("[Dubbo Filter] REQUEST -> {}.{}() | TraceId: {} | Params: {}",
                 serviceName, methodName, traceId, paramsStr);

        // 记录请求开始时间
        long startTime = System.currentTimeMillis();
        RpcContext.getClientAttachment().setAttachment("startTime", String.valueOf(startTime));

        // 继续调用
        return invoker.invoke(invocation);
    }

    /**
     * 格式化请求参数为字符串
     */
    private String formatParams(Object[] args) {
        if (args == null || args.length == 0) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            Object arg = args[i];
            if (arg == null) {
                sb.append("null");
            } else if (arg instanceof String) {
                sb.append("'").append(arg).append("'");
            } else if (arg instanceof Number || arg instanceof Boolean) {
                sb.append(arg);
            } else {
                // 对于复杂对象，只显示类名
                sb.append(arg.getClass().getSimpleName());
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
