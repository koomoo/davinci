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

import com.google.common.collect.Lists;

import edp.davinci.core.enums.LogNameEnum;
import edp.davinci.dto.dashboardDto.DashboardPortalCreate;
import edp.davinci.dto.dashboardDto.DashboardPortalUpdate;
import edp.davinci.model.Dashboard;
import edp.davinci.model.DashboardPortal;
import edp.davinci.model.User;
import edp.davinci.service.DashboardService;
import lombok.extern.slf4j.Slf4j;

@Component
@Aspect
@Slf4j
public class DashboardPortalAspect {
	private static final Logger optLogger = LoggerFactory.getLogger(LogNameEnum.BUSINESS_OPERATION.getName());
	private static final String DASHBOARD_PORTAL_CREAT_METHOD_NAME = "createDashboardPortal";
	private static final String DASHBOARD_PORTAL_UPDATE_METHOD_NAME = "updateDashboardPortal";
	private static final String DASHBOARD_PORTAL_DELETE_METHOD_NAME = "deleteDashboardPortal";

	private static final Integer CRM_DAVINCI_AUTH_RESOURCE_ID = 1841;

	@Autowired
	private DashboardService dashboardService;
	@Autowired
	private DashboardAspect dashboardAspect;
	// 切点
	@Pointcut("execution(* edp.davinci.service.impl.DashboardPortalServiceImpl.deleteDashboardPortal(..)) ")
	public void beforePointcut() {
	}
	
	@Pointcut("execution(* edp.davinci.service.impl.DashboardPortalServiceImpl.createDashboardPortal(..)) "
			+ "|| execution(* edp.davinci.service.impl.DashboardPortalServiceImpl.updateDashboardPortal(..)) "
			)
	public void afterPointcut() {
	}

	@Before("beforePointcut()")
	public void beforeMethod(JoinPoint joinPoint) throws Exception {
		String methodName = joinPoint.getSignature().getName();
		if (DASHBOARD_PORTAL_DELETE_METHOD_NAME.equals(methodName)) {
			deleteDashboardPortal((Long)joinPoint.getArgs()[0], (User)joinPoint.getArgs()[1]);
		}
	}
	@AfterReturning(pointcut = "afterPointcut()", returning = "methodRe")
	public void afterMethod(JoinPoint joinPoint, Object methodRe) throws Exception {
		String methodName = joinPoint.getSignature().getName();
		if (DASHBOARD_PORTAL_CREAT_METHOD_NAME.equals(methodName)) {
			createDashboardPortal(joinPoint, methodRe);
		} else if (DASHBOARD_PORTAL_UPDATE_METHOD_NAME.equals(methodName)) {
			updateDashboardPortal(joinPoint, methodRe);
		} else if (DASHBOARD_PORTAL_DELETE_METHOD_NAME.equals(methodName)) {
			deleteDashboardPortal((Long)joinPoint.getArgs()[0], (User)joinPoint.getArgs()[1]);
		}
	}

	public void deleteDashboardPortal(Long dashboardPortalId, User user) {
		CrmResourceAndRoleUtil.deleteCrmResource(
				CrmResourceAndRoleUtil.assembleResourceUrl(CrmConstant.TYPE_DASHBOARD_PORTAL, dashboardPortalId),
				user.getUsername());
		CrmResourceAndRoleUtil.deleteCrmRole(
				CrmResourceAndRoleUtil.assembleRoleEnglish(CrmConstant.TYPE_DASHBOARD_PORTAL, dashboardPortalId),
				user.getUsername());
		
		List<Dashboard> dashboards = dashboardService.getDashboards(dashboardPortalId, user);
		if(CollectionUtils.isEmpty(dashboards)) return;
		dashboards.forEach(dashboard -> {
			try {
				dashboardAspect.deleteDashboard(dashboard.getId(), user);
			} catch (Exception e) {
				log.error("删除dashboard资源失败");
			}
		});
		
	}

	private void updateDashboardPortal(JoinPoint joinPoint, Object methodRe) {
		DashboardPortalUpdate dashboardPortalUpdate = (DashboardPortalUpdate) joinPoint.getArgs()[0];
		User user = (User) joinPoint.getArgs()[1];
		CrmResourceAndRoleUtil.updateCrmResource(CrmResourceAndRoleUtil
				.assembleResourceUrl(CrmConstant.TYPE_DASHBOARD_PORTAL, dashboardPortalUpdate.getId()),
				dashboardPortalUpdate.getName(), user.getUsername());
		CrmResourceAndRoleUtil
				.updateCrmRole(
						CrmResourceAndRoleUtil.assembleRoleEnglish(CrmConstant.TYPE_DASHBOARD_PORTAL,
								dashboardPortalUpdate.getId()),
						dashboardPortalUpdate.getName() + "管理员", user.getUsername());
	}

	private void createDashboardPortal(JoinPoint joinPoint, Object methodRe) {
		DashboardPortalCreate dashboardPortalCreat = (DashboardPortalCreate) joinPoint.getArgs()[0];
		User user = (User) joinPoint.getArgs()[1];
		DashboardPortal dashboardPortal = (DashboardPortal) methodRe;
		CrmResourceAndRoleUtil.createCrmResource(null,
				CrmResourceAndRoleUtil.assembleResourceUrl(CrmConstant.TYPE_PROJECT,
						dashboardPortalCreat.getProjectId()),
				dashboardPortalCreat.getName(),
				CrmResourceAndRoleUtil.assembleResourceUrl(CrmConstant.TYPE_DASHBOARD_PORTAL, dashboardPortal.getId()),
				0, user.getUsername(), CrmConstant.CRM_RESOURCE_TYPE_ID_MENU);
		CrmResourceAndRoleUtil.createCrmRole(
				CrmResourceAndRoleUtil.assembleRoleEnglish(CrmConstant.TYPE_DASHBOARD_PORTAL, dashboardPortal.getId()),
				dashboardPortalCreat.getName() + "管理员", null, CrmResourceAndRoleUtil.assembleRoleEnglish(
						CrmConstant.TYPE_PROJECT, dashboardPortalCreat.getProjectId()),
				1, user.getUsername());

		List<CrmRoleResourceCreate> rel = Lists.newArrayList();
		CrmRoleResourceCreate projectResourceRel = new CrmRoleResourceCreate();
		projectResourceRel.setRoleEnglish(
				CrmResourceAndRoleUtil.assembleRoleEnglish(CrmConstant.TYPE_DASHBOARD_PORTAL, dashboardPortal.getId()));
		projectResourceRel.setResourceUrl(CrmResourceAndRoleUtil.assembleResourceUrl(CrmConstant.TYPE_PROJECT,
				dashboardPortalCreat.getProjectId()));
		rel.add(projectResourceRel);

		CrmRoleResourceCreate dashboardPortalResourceRel = new CrmRoleResourceCreate();
		dashboardPortalResourceRel.setRoleEnglish(
				CrmResourceAndRoleUtil.assembleRoleEnglish(CrmConstant.TYPE_DASHBOARD_PORTAL, dashboardPortal.getId()));
		dashboardPortalResourceRel.setResourceUrl(
				CrmResourceAndRoleUtil.assembleResourceUrl(CrmConstant.TYPE_DASHBOARD_PORTAL, dashboardPortal.getId()));
		rel.add(dashboardPortalResourceRel);

		CrmRoleResourceCreate authResourceRel = new CrmRoleResourceCreate();
		authResourceRel.setRoleEnglish(
				CrmResourceAndRoleUtil.assembleRoleEnglish(CrmConstant.TYPE_DASHBOARD_PORTAL, dashboardPortal.getId()));
		authResourceRel.setResourceId(CRM_DAVINCI_AUTH_RESOURCE_ID);
		rel.add(authResourceRel);

		CrmResourceAndRoleUtil.relCrmRoleResource(user.getUsername(), rel);
	}
}
