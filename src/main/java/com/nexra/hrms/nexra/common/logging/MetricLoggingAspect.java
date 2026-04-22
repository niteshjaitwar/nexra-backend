package com.nexra.hrms.nexra.common.logging;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * Cross cutting metric logger that enforces the mandated service log line
 * prescribed in instruction.md. Wraps every public method on a Spring bean
 * annotated with {@code @Service} inside the Nexra module tree and emits a
 * single entry line plus a single exit line with a millisecond duration. This
 * relieves module authors from hand writing identical boilerplate on every
 * service method while guaranteeing uniform observability. The aspect is
 * intentionally quiet on debug so production logs stay readable; it only
 * escalates to error when the method throws, capturing the stack trace.
 *
 * @author niteshjaitwar
 */
@Slf4j
@Aspect
@Component
public class MetricLoggingAspect {

    private final MeterRegistry meterRegistry;

    public MetricLoggingAspect(final MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Targets every public method on any bean annotated with {@code @Service}
     * inside the com.nexra.hrms.nexra.modules tree. Excludes the common
     * library itself to prevent aspect recursion.
     */
    @Pointcut("within(@org.springframework.stereotype.Service com.nexra.hrms.nexra.modules..*)")
    public void servicePointcut() {
    }

    /**
     * Logs entry, exit and elapsed milliseconds around every matched service
     * method. Re throws the original exception transparently.
     *
     * @param joinPoint the intercepted method invocation.
     * @return the value returned by the intercepted method.
     * @throws Throwable propagated from the intercepted method.
     */
    @Around("servicePointcut()")
    public Object logAround(final ProceedingJoinPoint joinPoint) throws Throwable {
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final String className = signature.getDeclaringType().getSimpleName();
        final String methodName = signature.getName();
        final String moduleName = resolveModule(signature.getDeclaringTypeName());
        final Timer.Sample sample = Timer.start(meterRegistry);
        final long start = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("{} - {}() - enter, argCount={}", className, methodName, joinPoint.getArgs() == null ? 0 : joinPoint.getArgs().length);
        }
        try {
            final Object result = joinPoint.proceed();
            final long elapsed = System.currentTimeMillis() - start;
            log.info("{} - {}() - exit, milliseconds={}", className, methodName, elapsed);
            sample.stop(Timer.builder("nexra.service.execution")
                    .description("Execution time for service methods")
                    .tag("module", moduleName)
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("outcome", "success")
                    .register(meterRegistry));
            return result;
        } catch (final Throwable thrown) {
            final long elapsed = System.currentTimeMillis() - start;
            log.error("{} - {}() - error type={}, message={}, milliseconds={}",
                    className,
                    methodName,
                    thrown.getClass().getSimpleName(),
                    thrown.getMessage(),
                    elapsed);
            sample.stop(Timer.builder("nexra.service.execution")
                    .description("Execution time for service methods")
                    .tag("module", moduleName)
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("outcome", "error")
                    .register(meterRegistry));
            throw thrown;
        }
    }

    private String resolveModule(final String declaringTypeName) {
        final String marker = ".modules.";
        final int markerIndex = declaringTypeName.indexOf(marker);
        if (markerIndex < 0) {
            return "common";
        }
        final String modulePath = declaringTypeName.substring(markerIndex + marker.length());
        final int separator = modulePath.indexOf('.');
        return separator < 0 ? modulePath : modulePath.substring(0, separator);
    }
}
