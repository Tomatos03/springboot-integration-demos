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
 * Dubbo 异常处理过滤器
 * 在提供端拦截异常，记录详细的异常日志
 */
@Slf4j
@Activate(group = CommonConstants.PROVIDER)
public class DubboExceptionFilter implements Filter {

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

        try {
            // 继续调用下一个过滤器或具体实现
            Result result = invoker.invoke(invocation);

            // 如果有异常，记录异常日志
            if (result.hasException()) {
                Throwable exception = result.getException();
                log.error("[Dubbo Filter] EXCEPTION <- {}.{}() | TraceId: {} | Error: {}", 
                         serviceName, methodName, traceId, exception.getMessage(), exception);
            }

            return result;
        } catch (RpcException e) {
            // 捕获 RpcException
            log.error("[Dubbo Filter] EXCEPTION <- {}.{}() | TraceId: {} | Error: {}", 
                     serviceName, methodName, traceId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            // 捕获其他异常
            log.error("[Dubbo Filter] EXCEPTION <- {}.{}() | TraceId: {} | Error: {}", 
                     serviceName, methodName, traceId, e.getMessage(), e);
            throw new RpcException("Dubbo service exception", e);
        }
    }
}
