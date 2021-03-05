package edp.crm.aspect;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edp.davinci.core.enums.LogNameEnum;
import edp.davinci.dto.projectDto.ProjectCreat;
import edp.davinci.dto.projectDto.ProjectInfo;
import edp.davinci.dto.projectDto.ProjectUpdate;
import edp.davinci.model.DashboardPortal;
import edp.davinci.model.User;
import edp.davinci.service.DashboardPortalService;
import lombok.extern.slf4j.Slf4j;

@Component
@Aspect
@Slf4j
public class ProjectAspect {
	private static final Logger optLogger = LoggerFactory.getLogger(LogNameEnum.BUSINESS_OPERATION.getName());
	private static final String PROJECT_CREAT_METHOD_NAME = "createProject";
	private static final String PROJECT_UPDATE_METHOD_NAME = "updateProject";
	private static final String PROJECT_DELETE_METHOD_NAME = "deleteProject";

	@Autowired
	private DashboardPortalService dashboardPortalService;
	@Autowired
	private DashboardPortalAspect dashboardPortalAspect;
	// 切点
	@Pointcut("execution(* edp.davinci.service.impl.ProjectServiceImpl.deleteProject(..)) ")
	public void beforePointcut() {
	}
	@Pointcut("execution(* edp.davinci.service.impl.ProjectServiceImpl.createProject(..)) "
			+ "|| execution(* edp.davinci.service.impl.ProjectServiceImpl.updateProject(..)) "
			)
	public void afterPointcut() {
	}
	
	@Before("beforePointcut()")
	public void beforeMethod(JoinPoint joinPoint) throws Exception {
		String methodName = joinPoint.getSignature().getName();
		if (PROJECT_DELETE_METHOD_NAME.equals(methodName)) {
			deleteProject(joinPoint);
		}
	}

	@AfterReturning(pointcut = "afterPointcut()", returning = "methodRe")
	public void afterMethod(JoinPoint joinPoint, Object methodRe) throws Exception {
		String methodName = joinPoint.getSignature().getName();
		if (PROJECT_CREAT_METHOD_NAME.equals(methodName)) {
			createProject(joinPoint, methodRe);
		} else if (PROJECT_UPDATE_METHOD_NAME.equals(methodName)) {
			updateProject(joinPoint);
		} else if (PROJECT_DELETE_METHOD_NAME.equals(methodName)) {
			deleteProject(joinPoint);
		}
	}

	private void deleteProject(JoinPoint joinPoint) {
		Long projectId = (Long) joinPoint.getArgs()[0];
		User user = (User) joinPoint.getArgs()[1];

		CrmResourceAndRoleUtil.deleteCrmResource(
				CrmResourceAndRoleUtil.assembleResourceUrl(CrmConstant.TYPE_PROJECT, projectId), user.getUsername());
		CrmResourceAndRoleUtil.deleteCrmRole(
				CrmResourceAndRoleUtil.assembleRoleEnglish(CrmConstant.TYPE_PROJECT, projectId), user.getUsername());
		
		//刘飞同步删除dashboard_portal
		List<DashboardPortal> dashboardPortals = dashboardPortalService.getDashboardPortals(projectId, user);
		if(CollectionUtils.isEmpty(dashboardPortals)) return;
		dashboardPortals.forEach(dashboardPortal -> {
			dashboardPortalAspect.deleteDashboardPortal(dashboardPortal.getId(), user);
		});
		
	}

	private void updateProject(JoinPoint joinPoint) throws Exception {
		Long projectId = (Long) joinPoint.getArgs()[0];
		ProjectUpdate projectUpdate = (ProjectUpdate) joinPoint.getArgs()[1];
		User user = (User) joinPoint.getArgs()[2];

		CrmResourceAndRoleUtil.updateCrmResource(
				CrmResourceAndRoleUtil.assembleResourceUrl(CrmConstant.TYPE_PROJECT, projectId),
				projectUpdate.getName(), user.getUsername());
		CrmResourceAndRoleUtil.updateCrmRole(
				CrmResourceAndRoleUtil.assembleRoleEnglish(CrmConstant.TYPE_PROJECT, projectId),
				projectUpdate.getName(), user.getUsername());
	}

	private void createProject(JoinPoint joinPoint, Object methodRe) {
		ProjectCreat projectCreat = (ProjectCreat) joinPoint.getArgs()[0];
		User user = (User) joinPoint.getArgs()[1];
		ProjectInfo projectInfo = (ProjectInfo) methodRe;

		CrmResourceAndRoleUtil.createCrmResource(CrmConstant.CRM_RESOURCE_MENU_DAVINCI, null, projectCreat.getName(),
				CrmResourceAndRoleUtil.assembleResourceUrl(CrmConstant.TYPE_PROJECT, projectInfo.getId()), 0,
				user.getUsername());
		CrmResourceAndRoleUtil.createCrmRole(
				CrmResourceAndRoleUtil.assembleRoleEnglish(CrmConstant.TYPE_PROJECT, projectInfo.getId()),
				projectInfo.getName(), CrmConstant.CRM_ROLE_MENU, null, 0, user.getUsername());
	}
}
