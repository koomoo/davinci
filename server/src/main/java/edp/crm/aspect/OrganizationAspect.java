package edp.crm.aspect;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.pagehelper.PageInfo;

import edp.davinci.core.enums.LogNameEnum;
import edp.davinci.dto.organizationDto.OrganizationPut;
import edp.davinci.dto.projectDto.ProjectWithCreateBy;
import edp.davinci.model.User;
import edp.davinci.service.ProjectService;
import lombok.extern.slf4j.Slf4j;

@Component
@Aspect
@Slf4j
public class OrganizationAspect {
	private static final Logger optLogger = LoggerFactory.getLogger(LogNameEnum.BUSINESS_OPERATION.getName());
	private static final String ORGANIZATION_UPDATE_METHOD_NAME = "updateOrganization";

	@Autowired
	private ProjectService projectService;
	@Autowired
	private ProjectAspect projectAspect;
	// 切点
	@Pointcut("execution(* edp.davinci.service.impl.OrganizationServiceImpl.updateOrganization(..))")
	public void afterPointcut() {
	}
	
	@AfterReturning(pointcut = "afterPointcut()", returning = "methodRe")
	public void afterMethod(JoinPoint joinPoint, Object methodRe) throws Exception {
		String methodName = joinPoint.getSignature().getName();
		if (ORGANIZATION_UPDATE_METHOD_NAME.equals(methodName)) {
			updateOrganization(joinPoint, methodRe);
		}
	}

	private void updateOrganization(JoinPoint joinPoint, Object methodRe) {
		OrganizationPut put = (OrganizationPut)joinPoint.getArgs()[0];
		User user = (User)joinPoint.getArgs()[1];
		
		PageInfo<ProjectWithCreateBy> projectsByOrg = projectService.getProjectsByOrg(put.getId(), user, "", 0, 100);
		List<ProjectWithCreateBy> projects = projectsByOrg.getList();
		if(CollectionUtils.isEmpty(projects)) return;
		projects.forEach(project -> {
			projectAspect.updateProjectName(project.getId(), user);
		});
	}
}
