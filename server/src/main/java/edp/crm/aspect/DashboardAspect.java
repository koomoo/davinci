package edp.crm.aspect;

import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import edp.core.utils.DateUtils;
import edp.davinci.dto.dashboardDto.DashboardCreate;
import edp.davinci.dto.dashboardDto.DashboardDto;
import edp.davinci.dto.shareDto.ShareEntity;
import edp.davinci.model.Dashboard;
import edp.davinci.model.User;
import edp.davinci.service.DashboardService;
import edp.davinci.service.share.ShareMode;
import edp.davinci.service.share.ShareResult;
import lombok.extern.slf4j.Slf4j;

@Component
@Aspect
@Slf4j
public class DashboardAspect {
	private static final String DASHBOARD_CREAT_METHOD_NAME = "createDashboard";
	private static final String DASHBOARD_UPDATE_METHOD_NAME = "updateDashboards";
	private static final String DASHBOARD_DELETE_METHOD_NAME = "deleteDashboard";
	
	@Autowired
    private DashboardService dashboardService;
	//切点
    @Pointcut(
    		"execution(* edp.davinci.service.impl.DashboardServiceImpl.createDashboard(..)) "
    		+ "|| execution(* edp.davinci.service.impl.DashboardServiceImpl.deleteDashboard(..)) "
    		+ "|| execution(* edp.davinci.service.impl.DashboardServiceImpl.updateDashboards(..)) "
    		)
    public void afterPointcut() {
    }
    
    @AfterReturning(pointcut="afterPointcut()", returning="methodRe")
    public void afterMethod(JoinPoint joinPoint, Object methodRe) throws Exception {
    	String methodName = joinPoint.getSignature().getName();
		if (DASHBOARD_CREAT_METHOD_NAME.equals(methodName)) {
			createDashboard(joinPoint, methodRe);
		} else if (DASHBOARD_UPDATE_METHOD_NAME.equals(methodName)) {
			updateDashboard(joinPoint, methodRe);
		} else if (DASHBOARD_DELETE_METHOD_NAME.equals(methodName)) {
			deleteDashboard((Long)joinPoint.getArgs()[0], ((User)joinPoint.getArgs()[1]).getUsername());
		}
    }
    
    public void deleteDashboard(Long dashboardId, String username) {
		CrmResourceAndRoleUtil.deleteCrmResource(CrmResourceAndRoleUtil.assembleResourceUrl(CrmConstant.TYPE_DASHBOARD, dashboardId), username);
	}
    
    private void updateDashboard(JoinPoint joinPoint, Object methodRe) {
		DashboardDto[] dashboards = (DashboardDto[]) joinPoint.getArgs()[1];
		User user = (User) joinPoint.getArgs()[2];

		for (DashboardDto dashboard : dashboards) {
			CrmResourceAndRoleUtil.updateCrmResource(CrmResourceAndRoleUtil.assembleResourceUrl(CrmConstant.TYPE_DASHBOARD, dashboard.getId()), dashboard.getName(),
					user.getUsername());
		}
	}
    
    private void createDashboard(JoinPoint joinPoint, Object methodRe) throws Exception {
		DashboardCreate dashboardCreate = (DashboardCreate) joinPoint.getArgs()[0];
		User user = (User) joinPoint.getArgs()[1];
		Dashboard dashboard = (Dashboard) methodRe;
		CrmResourceAndRoleUtil.createCrmResource(null, CrmResourceAndRoleUtil.assembleResourceUrl(CrmConstant.TYPE_DASHBOARD_PORTAL, dashboardCreate.getDashboardPortalId()),
				dashboardCreate.getName(), CrmResourceAndRoleUtil.assembleResourceUrl(CrmConstant.TYPE_DASHBOARD, dashboard.getId()), 1,
				user.getUsername(), assembleShareUrl(dashboard.getId(), user));

		List<CrmRoleResourceCreate> rel = Lists.newArrayList();
		CrmRoleResourceCreate crmRoleResourceCreate = new CrmRoleResourceCreate();
		crmRoleResourceCreate
				.setRoleEnglish(CrmResourceAndRoleUtil.assembleRoleEnglish(CrmConstant.TYPE_DASHBOARD_PORTAL, dashboardCreate.getDashboardPortalId()));
		crmRoleResourceCreate.setResourceUrl(CrmResourceAndRoleUtil.assembleResourceUrl(CrmConstant.TYPE_DASHBOARD, dashboard.getId()));
		rel.add(crmRoleResourceCreate);
		CrmResourceAndRoleUtil.relCrmRoleResource(user.getUsername(), rel);
	}
    
    private String assembleShareUrl(Long id, User user) throws Exception {
		ShareEntity shareEntity = new ShareEntity();
		shareEntity.setMode(ShareMode.NORMAL);
		shareEntity.setExpired(DateUtils.getUtilDate("2050-03-18 17:07:42", "yyyy-MM-dd HH:mm:ss"));
		ShareResult shareResult = dashboardService.shareDashboard(id, user, shareEntity);
		return "/dav/share.html?shareToken=" + shareResult.getToken() + "#share/dashboard";
	}
}
