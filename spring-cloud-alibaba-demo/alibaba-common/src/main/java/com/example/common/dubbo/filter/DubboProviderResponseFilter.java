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

/**
 * Dubbo 提供端响应拦截器
 * 在提供端（服务提供者）拦截 Dubbo 响应，记录响应日志和耗时
 */
@Slf4j
@Activate(group = CommonConstants.PROVIDER)
public class DubboProviderResponseFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 获取 TraceId（从消费端传递过来）
        String traceId = RpcContext.getServerAttachment().getAttachment("traceId");
        if (traceId == null) {
            traceId = "UNKNOWN";
        }

        // 获取调用信息
        String serviceName = invoker.getInterface().getSimpleName();
        String methodName = invocation.getMethodName();

        // 记录请求开始时间
        long startTime = System.currentTimeMillis();

        // 继续调用下一个过滤器或具体实现
        Result result = invoker.invoke(invocation);

        // 计算耗时
        long costTime = System.currentTimeMillis() - startTime;

        // 判断调用是否成功
        boolean isSuccess = !result.hasException();
        String resultStr = isSuccess ? "success" : "exception";

        // 记录响应日志
        log.info("[Dubbo Filter] RESPONSE <- {}.{}() | TraceId: {} | Cost: {}ms | Result: {}", 
                 serviceName, methodName, traceId, costTime, resultStr);

        return result;
    }
}
