package com.x.processplatform.assemble.surface.jaxrs.task;

import com.x.base.core.project.exception.PromptException;

class ExceptionEmptyRouteName extends PromptException {

	private static final long serialVersionUID = 1040883405179987063L;

	ExceptionEmptyRouteName() {
		super("路由选择不能为空.");
	}
}
