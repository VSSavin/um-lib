package com.github.vssavin.umlib.config.aspect;

import com.github.vssavin.umlib.config.DataSourceSwitcher;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Ensures that the datasource is switched before methods are annotated with the
 * {@link com.github.vssavin.umlib.config.aspect.UmRouteDatasource} annotation.
 *
 * @author vssavin on 02.09.2023
 */
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
class UmRoutingDatasourceAspect {

	private final DataSourceSwitcher dataSourceSwitcher;

	@Autowired
	UmRoutingDatasourceAspect(DataSourceSwitcher dataSourceSwitcher) {
		this.dataSourceSwitcher = dataSourceSwitcher;
	}

	@Around("@annotation(UmRouteDatasource)")
	public Object routeDatasource(ProceedingJoinPoint joinPoint) throws Throwable {
		Object result;
		dataSourceSwitcher.switchToUmDataSource();
		try {
			result = joinPoint.proceed();
		}
		finally {
			dataSourceSwitcher.switchToPreviousDataSource();
		}

		return result;
	}

}
