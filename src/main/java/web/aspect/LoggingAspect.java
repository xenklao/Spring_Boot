package web.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class LoggingAspect {

    @Around("execution(* web.repository.*.*(..))")
    public Object aroundAllRepositoryMethodsAdvice(ProceedingJoinPoint joinPoint) throws Throwable {

        System.out.println("Start method " + joinPoint.getSignature());
        long start = System.currentTimeMillis();
        Object targetMethodResult = joinPoint.proceed();
        System.out.println(joinPoint.getSignature().getName() + " executed in " + (System.currentTimeMillis() - start) + "ms");

        return targetMethodResult;
    }

}
