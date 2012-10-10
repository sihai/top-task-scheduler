package com.ihome.top.scheduler;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public class BaseTopTaskScheduleTestCase extends
		AbstractDependencyInjectionSpringContextTests {

	@Override
	protected String[] getConfigLocations() {
		return new String[]{"classpath*:/spring-test/spring-top-task-schedule-test.xml"};
	}

}
