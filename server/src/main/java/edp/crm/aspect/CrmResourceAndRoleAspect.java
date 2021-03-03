/*
 * <<
 *  Davinci
 *  ==
 *  Copyright (C) 2016 - 2020 EDP
 *  ==
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  >>
 */

package edp.crm.aspect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;

import edp.core.utils.DateUtils;
import edp.crm.util.CRMHttpClientUtil;
import edp.davinci.core.enums.LogNameEnum;
import edp.davinci.dto.dashboardDto.DashboardCreate;
import edp.davinci.dto.dashboardDto.DashboardDto;
import edp.davinci.dto.dashboardDto.DashboardPortalCreate;
import edp.davinci.dto.dashboardDto.DashboardPortalUpdate;
import edp.davinci.dto.projectDto.ProjectCreat;
import edp.davinci.dto.projectDto.ProjectDetail;
import edp.davinci.dto.projectDto.ProjectInfo;
import edp.davinci.dto.projectDto.ProjectUpdate;
import edp.davinci.dto.shareDto.ShareEntity;
import edp.davinci.model.Dashboard;
import edp.davinci.model.DashboardPortal;
import edp.davinci.model.User;
import edp.davinci.service.DashboardPortalService;
import edp.davinci.service.DashboardService;
import edp.davinci.service.ProjectService;
import edp.davinci.service.share.ShareMode;
import edp.davinci.service.share.ShareResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Aspect
@Slf4j
public class CrmResourceAndRoleAspect {
	private static final Logger optLogger = LoggerFactory.getLogger(LogNameEnum.BUSINESS_OPERATION.getName());
	private static final String PROJECT_SERVICE_NAME = "ProjectServiceImpl";
	private static final String PROJECT_CREAT_METHOD_NAME = "createProject";
	private static final String PROJECT_UPDATE_METHOD_NAME = "updateProject";
	private static final String PROJECT_DELETE_METHOD_NAME = "deleteProject";
	private static final String PROJECT_TRANSFER_METHOD_NAME = "transferPeoject";
	
	private static final String DASHBOARD_PORTAL_SERVICE_NAME = "DashboardPortalServiceImpl";
	private static final String DASHBOARD_PORTAL_CREAT_METHOD_NAME = "createDashboardPortal";
	private static final String DASHBOARD_PORTAL_UPDATE_METHOD_NAME = "updateDashboardPortal";
	private static final String DASHBOARD_PORTAL_DELETE_METHOD_NAME = "deleteDashboardPortal";
	
	private static final String DASHBOARD_SERVICE_NAME = "DashboardServiceImpl";
	private static final String DASHBOARD_CREAT_METHOD_NAME = "createDashboard";
	private static final String DASHBOARD_UPDATE_METHOD_NAME = "updateDashboards";
	private static final String DASHBOARD_DELETE_METHOD_NAME = "deleteDashboard";
	
	private static final String TYPE_PROJECT = "PROJECT";
	private static final String TYPE_DASHBOARD_PORTAL = "DASHBOARD_PORTAL";
	private static final String TYPE_DASHBOARD = "DASHBOARD";
	
	private static final Long DAVINCI_ORGANIZE_CRM_ID = 3l;
	
	private static final Integer CRM_RESOURCE_TYPE_ID = 2;
	private static final Integer CRM_RESOURCE_MENU_SYSTEMMANAGER = 1;
	private static final Integer CRM_RESOURCE_MENU_DAVINCI = 1802;
	private static final Integer CRM_DAVINCI_AUTH_RESOURCE_ID = 1841;
	private static final String CRM_RESOURCE_SYSTEM_CODE_CRM = "CRM";
	private static final Integer CRM_ROLE_MENU = 407;
	private static final Integer CRM_ROLE_GROUP_ID = 1;
	
	//private static final String CRM_SERVER = "http://api.ymt.io/crm-gateway/api";
	private static final String CRM_SERVER = "http://dev-crm-host001.ymt.io:81/api";
	private static final String RESOURCE_CREATE_URL = "/resource";
	private static final String RESOURCE_UPDATE_URL = "/resource/update";
	private static final String RESOURCE_DELETE_URL = "/resource/delete";
	private static final String ROLE_CREATE_URL = "/role";
	private static final String ROLE_UPDATE_URL = "/role/update";
	private static final String ROLE_DELETE_URL = "/role/delete";
	private static final String ROLE_RESOURCE_REL_URL = "/roleResource";
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private DashboardPortalService dashboardPortalService;
	@Autowired
    private DashboardService dashboardService;
	
	@Pointcut(
    		"execution(* edp.davinci.service.impl.ProjectServiceImpl.updateProject(..)) "
    		+ "|| execution(* edp.davinci.service.impl.ProjectServiceImpl.deleteProject(..)) "
    		+ "|| execution(* edp.davinci.service.impl.DashboardPortalServiceImpl.updateDashboardPortal(..)) "
    		+ "|| execution(* edp.davinci.service.impl.DashboardServiceImpl.updateDashboards(..)) "
    		)
    public void beforePointcut() {
    }
	
	//切点
    @Pointcut("execution(* edp.davinci.service.impl.ProjectServiceImpl.createProject(..)) "
    		+ "|| execution(* edp.davinci.service.impl.ProjectServiceImpl.transferPeoject(..)) "
    		+ "|| execution(* edp.davinci.service.impl.DashboardPortalServiceImpl.createDashboardPortal(..)) "
    		+ "|| execution(* edp.davinci.service.impl.DashboardPortalServiceImpl.deleteDashboardPortal(..)) "
    		+ "|| execution(* edp.davinci.service.impl.DashboardServiceImpl.createDashboard(..)) "
    		+ "|| execution(* edp.davinci.service.impl.DashboardServiceImpl.deleteDashboard(..)) "
    		)
    public void afterPointcut() {
    }
    
	@Before("beforePointcut()")
	public void beforeMethod(JoinPoint joinPoint) throws Exception {
		String serviceName = joinPoint.getTarget().getClass().getSimpleName();
		String methodName = joinPoint.getSignature().getName();
		if (PROJECT_SERVICE_NAME.equals(serviceName)) {
			if (PROJECT_UPDATE_METHOD_NAME.equals(methodName)) {
				updateProject((Long) joinPoint.getArgs()[0], ((ProjectUpdate) joinPoint.getArgs()[1]).getName(),
						(User) joinPoint.getArgs()[2]);
			} else if (PROJECT_DELETE_METHOD_NAME.equals(methodName)) {
				deleteProject((Long) joinPoint.getArgs()[0], (User) joinPoint.getArgs()[1]);
			}
		} else if (DASHBOARD_PORTAL_SERVICE_NAME.equals(serviceName)) {
			if (DASHBOARD_PORTAL_UPDATE_METHOD_NAME.equals(methodName)) {
				updateDashboardPortal(((DashboardPortalUpdate) joinPoint.getArgs()[0]).getId(),
						((DashboardPortalUpdate) joinPoint.getArgs()[0]).getName(),
						((User) joinPoint.getArgs()[1]).getUsername());
			}
		} else if (DASHBOARD_SERVICE_NAME.equals(serviceName)) {
			if (DASHBOARD_UPDATE_METHOD_NAME.equals(methodName)) {
				updateCrmDashboard((DashboardDto[]) joinPoint.getArgs()[1],
						((User) joinPoint.getArgs()[2]).getUsername());
			}
		}
	}
    
	@AfterReturning(pointcut = "afterPointcut()", returning = "methodRe")
	public void afterMethod(JoinPoint joinPoint, Object methodRe) throws Exception {
		String className = joinPoint.getTarget().getClass().getSimpleName();
		String methodName = joinPoint.getSignature().getName();
		Object[] args = joinPoint.getArgs();
		if (PROJECT_SERVICE_NAME.equals(className)) {
			if (PROJECT_CREAT_METHOD_NAME.equals(methodName)) {
				createProject(((ProjectInfo) methodRe).getId(), ((ProjectCreat) args[0]).getName(),
						((User) args[1]).getUsername());
			} else if (PROJECT_TRANSFER_METHOD_NAME.equals(methodName)) {
				// 刘飞 这里处理
				transferProject(joinPoint);
			}
		} else if (DASHBOARD_PORTAL_SERVICE_NAME.equals(className)) {
			if (DASHBOARD_PORTAL_CREAT_METHOD_NAME.equals(methodName)) {
				createDashboardPortal(((DashboardPortalCreate) args[0]).getProjectId(),
						((DashboardPortalCreate) args[0]).getName(), ((DashboardPortal) methodRe).getId(),
						((User) args[1]).getUsername());
			} else if (DASHBOARD_PORTAL_DELETE_METHOD_NAME.equals(methodName)) {
				deleteDashboardPortal((Long) joinPoint.getArgs()[0], (User) joinPoint.getArgs()[1]);
			}
		} else if (DASHBOARD_SERVICE_NAME.equals(className)) {
			if (DASHBOARD_CREAT_METHOD_NAME.equals(methodName)) {
				createDashboard(((DashboardCreate) args[0]).getDashboardPortalId(),
						((DashboardCreate) args[0]).getName(), ((Dashboard) methodRe).getId(), (User) args[1]);
			} else if (DASHBOARD_DELETE_METHOD_NAME.equals(methodName)) {
				// 删
				deleteDashboard((Long) args[0], ((User) args[1]).getUsername());
			}
		}
	}

	private String assembleShareUrl(Long id, User user) throws Exception {
		ShareEntity shareEntity = new ShareEntity();
		shareEntity.setMode(ShareMode.NORMAL);
		shareEntity.setExpired(DateUtils.getUtilDate("2050-03-18 17:07:42", "yyyy-MM-dd HH:mm:ss"));
		ShareResult shareResult = dashboardService.shareDashboard(id, user, shareEntity);
		return "/dav/share.html?shareToken=" + shareResult.getToken() + "#share/dashboard";
	}

	private void transferProject(JoinPoint joinPoint) throws Exception {
		Long projectId = (Long)joinPoint.getArgs()[0];
		Long orgId = (Long)joinPoint.getArgs()[1];
		User user = (User)joinPoint.getArgs()[2];
		
		if(DAVINCI_ORGANIZE_CRM_ID.equals(orgId)) {
			ProjectDetail projectDetail = projectService.getProjectDetail(projectId, user, true);
			//处理项目
			createProject(projectId, projectDetail.getName(), user.getUsername());
			//处理dashboard_portal
			List<DashboardPortal> dashboardPortals = dashboardPortalService.getDashboardPortals(projectId, user);
			if(CollectionUtils.isEmpty(dashboardPortals)) return;
			for (DashboardPortal dashboardPortal : dashboardPortals) {
				createDashboardPortal(projectId, dashboardPortal.getName(), dashboardPortal.getId(), user.getUsername());
				//处理dashboard
				List<Dashboard> dashboards = dashboardService.getDashboards(dashboardPortal.getId(), user);
				if(CollectionUtils.isEmpty(dashboards)) continue;
				for (Dashboard dashboard : dashboards) {
					createDashboard(dashboardPortal.getId(), dashboard.getName(), dashboard.getId(), user);
				}
			}
		}else {
			deleteProject(projectId, user);
		}
	}
	
	private void createProject(Long projectId, String projectName, String username) {
		createCrmResource(CRM_RESOURCE_MENU_DAVINCI, null, projectName, assembleResourceUrl(TYPE_PROJECT, projectId), 0, username);
		createCrmRole(assembleRoleEnglish(TYPE_PROJECT, projectId), projectName, CRM_ROLE_MENU, null, 0, username);
	}
	private void updateProject(Long projectId, String projectName, User user) throws Exception {
		ProjectDetail projectDetail = projectService.getProjectDetail(projectId, user, true);
		if(projectDetail.getName().equals(projectName)) return;
		if(!DAVINCI_ORGANIZE_CRM_ID.equals(projectDetail.getOrgId())) return;

		updateCrmResource(assembleResourceUrl(TYPE_PROJECT, projectId), projectName, user.getUsername());
		updateCrmRole(assembleRoleEnglish(TYPE_PROJECT, projectId), projectName, user.getUsername());
	}
	
	private void deleteProject(Long projectId, User user) {
		ProjectDetail projectDetail = projectService.getProjectDetail(projectId, user, true);
		if(!DAVINCI_ORGANIZE_CRM_ID.equals(projectDetail.getOrgId())) return;
		
		deleteCrmResource(assembleResourceUrl(TYPE_PROJECT, projectId), user.getUsername());
		deleteCrmRole(assembleRoleEnglish(TYPE_PROJECT, projectId), user.getUsername());
		
		//级联删除项目下的所有portal
		List<DashboardPortal> dashboardPortals = dashboardPortalService.getDashboardPortals(projectId, user);
		if(CollectionUtils.isEmpty(dashboardPortals)) return;
		for (DashboardPortal dashboardPortal : dashboardPortals) {
			deleteDashboardPortal(dashboardPortal.getId(), user);
		}
	}
	
	private void createDashboardPortal(Long projectId, String dashboardPortalName, Long dashboardPortalId, String username) {
		createCrmResource(null, assembleResourceUrl(TYPE_PROJECT, projectId),
				dashboardPortalName, assembleResourceUrl(TYPE_DASHBOARD_PORTAL, dashboardPortalId), 0,
				username);
		createCrmRole(assembleRoleEnglish(TYPE_DASHBOARD_PORTAL, dashboardPortalId),
				dashboardPortalName + "管理员", null,
				assembleRoleEnglish(TYPE_PROJECT, projectId), 0, username);
		
		List<CrmRoleResourceCreate> rel = Lists.newArrayList();
		CrmRoleResourceCreate crmSystemManageMenuRel = new CrmRoleResourceCreate();
		crmSystemManageMenuRel.setRoleEnglish(assembleRoleEnglish(TYPE_DASHBOARD_PORTAL, dashboardPortalId));
		crmSystemManageMenuRel.setResourceId(CRM_RESOURCE_MENU_SYSTEMMANAGER);
		rel.add(crmSystemManageMenuRel);
		
		CrmRoleResourceCreate crmDavinciMenuRel = new CrmRoleResourceCreate();
		crmDavinciMenuRel.setRoleEnglish(assembleRoleEnglish(TYPE_DASHBOARD_PORTAL, dashboardPortalId));
		crmDavinciMenuRel.setResourceId(CRM_RESOURCE_MENU_DAVINCI);
		rel.add(crmDavinciMenuRel);
		
		CrmRoleResourceCreate projectResourceRel = new CrmRoleResourceCreate();
		projectResourceRel.setRoleEnglish(assembleRoleEnglish(TYPE_DASHBOARD_PORTAL, dashboardPortalId));
		projectResourceRel.setResourceUrl(assembleResourceUrl(TYPE_PROJECT, projectId));
		rel.add(projectResourceRel);
		
		CrmRoleResourceCreate dashboardPortalResourceRel = new CrmRoleResourceCreate();
		dashboardPortalResourceRel.setRoleEnglish(assembleRoleEnglish(TYPE_DASHBOARD_PORTAL, dashboardPortalId));
		dashboardPortalResourceRel.setResourceUrl(assembleResourceUrl(TYPE_DASHBOARD_PORTAL, projectId));
		rel.add(dashboardPortalResourceRel);
		
		CrmRoleResourceCreate authResourceRel = new CrmRoleResourceCreate();
		authResourceRel.setRoleEnglish(assembleRoleEnglish(TYPE_DASHBOARD_PORTAL, dashboardPortalId));
		authResourceRel.setResourceId(CRM_DAVINCI_AUTH_RESOURCE_ID);
		rel.add(authResourceRel);
		
		relCrmRoleResource(username, rel);
	}
	
	private void updateDashboardPortal(Long dashboardPortalId, String dashboardPortalName, String username) {
		updateCrmResource(assembleResourceUrl(TYPE_DASHBOARD_PORTAL, dashboardPortalId),
				dashboardPortalName, username);
		updateCrmRole(assembleRoleEnglish(TYPE_DASHBOARD_PORTAL, dashboardPortalId),
				dashboardPortalName + "管理员", username);
	}
	
	private void deleteDashboardPortal(Long dashboardPortalId, User user) {
		deleteCrmResource(assembleResourceUrl(TYPE_DASHBOARD_PORTAL, dashboardPortalId), user.getUsername());
		deleteCrmRole(assembleRoleEnglish(TYPE_DASHBOARD_PORTAL, dashboardPortalId), user.getUsername());
		
		List<Dashboard> dashboards = dashboardService.getDashboards(dashboardPortalId, user);
		if(CollectionUtils.isEmpty(dashboards)) return;
		for (Dashboard dashboard : dashboards) {
			deleteDashboard(dashboard.getId(), user.getUsername());
		}
	}
	
	private void createDashboard(Long dashboardPortalId, String dashboardName, Long dashboardId, User user) throws Exception {
		createCrmResource(null, assembleResourceUrl(TYPE_DASHBOARD_PORTAL, dashboardPortalId),
				dashboardName, assembleResourceUrl(TYPE_DASHBOARD, dashboardId), 1,
				user.getUsername(), assembleShareUrl(dashboardId, user));

		List<CrmRoleResourceCreate> rel = Lists.newArrayList();
		CrmRoleResourceCreate crmRoleResourceCreate = new CrmRoleResourceCreate();
		crmRoleResourceCreate
				.setRoleEnglish(assembleRoleEnglish(TYPE_DASHBOARD_PORTAL, dashboardPortalId));
		crmRoleResourceCreate.setResourceUrl(assembleResourceUrl(TYPE_DASHBOARD, dashboardId));
		rel.add(crmRoleResourceCreate);
		relCrmRoleResource(user.getUsername(), rel);
	}
	
	private void updateCrmDashboard(DashboardDto[] dashboards, String username) {
		for (DashboardDto dashboard : dashboards) {
			updateCrmResource(assembleResourceUrl(TYPE_DASHBOARD, dashboard.getId()), dashboard.getName(),
					username);
		}
	}
	
	private void deleteDashboard(Long dashboardId, String username) {
		deleteCrmResource(assembleResourceUrl(TYPE_DASHBOARD, dashboardId), username);
	}
	
	private void relCrmRoleResource(String createdByUsername, List<CrmRoleResourceCreate> rel) {
		if(CollectionUtils.isEmpty(rel)) return;
		String relRoleResourceReStr = CRMHttpClientUtil.doPostJson(CRM_SERVER + ROLE_RESOURCE_REL_URL + "?createdByUsername=" + createdByUsername, JSON.toJSONString(rel));
		Map<String,Object> relRoleResourceRe = JSON.parseObject(relRoleResourceReStr, new TypeReference<Map<String,Object>>(){}.getType());
		if(relRoleResourceRe == null || !Integer.valueOf(0).equals(relRoleResourceRe.get("status"))) {
			optLogger.info("CRM角色和资源关联失败，re={}", relRoleResourceReStr);
			log.info("CRM角色和资源关联失败，re={}", relRoleResourceReStr);
			throw new RuntimeException("CRM角色和资源关联失败");
		}
	}
	
	private void deleteCrmRole(String roleEnglish, String deleteByUsername) {
		Map<String,Object> deleteRoleParam = new HashMap<>();
		deleteRoleParam.put("roleEnglish", roleEnglish);
		deleteRoleParam.put("deleteByUsername", deleteByUsername);
		String deleteRoleReStr = CRMHttpClientUtil.doPostJson(CRM_SERVER + ROLE_DELETE_URL, JSON.toJSONString(deleteRoleParam));
		Map<String,Object> deleteRoleRe = JSON.parseObject(deleteRoleReStr, new TypeReference<Map<String,Object>>(){}.getType());
		if(deleteRoleRe == null || !Integer.valueOf(0).equals(deleteRoleRe.get("status"))) {
			optLogger.info("删除CRM角色失败，re={}", deleteRoleReStr);
			log.info("删除CRM角色失败，re={}", deleteRoleReStr);
			throw new RuntimeException("删除CRM角色失败");
		}
	}
	
	private void deleteCrmResource(String resourceUrl, String deleteByUsername) {
		Map<String,Object> deleteResourceParam = new HashMap<>();
		deleteResourceParam.put("resourceUrl", resourceUrl);
		deleteResourceParam.put("deleteByUsername", deleteByUsername);
		String deleteResourceReStr = CRMHttpClientUtil.doPostJson(CRM_SERVER + RESOURCE_DELETE_URL, JSON.toJSONString(deleteResourceParam));
		Map<String,Object> deleteResourceRe = JSON.parseObject(deleteResourceReStr, new TypeReference<Map<String,Object>>(){}.getType());
		if(deleteResourceRe == null || !Integer.valueOf(0).equals(deleteResourceRe.get("status"))) {
			optLogger.info("删除CRM资源失败，re={}", deleteResourceReStr);
			log.info("删除CRM资源失败，re={}", deleteResourceReStr);
			throw new RuntimeException("删除CRM资源失败");
		}
	}
	
	private void updateCrmRole(String roleEnglish, String roleName, String updatedByUsername) {
		Map<String,Object> updateRoleParam = new HashMap<>();
		updateRoleParam.put("roleEnglish", roleEnglish);
		updateRoleParam.put("roleName", roleName);
		updateRoleParam.put("updatedByUsername", updatedByUsername);
		String updateRoleReStr = CRMHttpClientUtil.doPostJson(CRM_SERVER + ROLE_UPDATE_URL, JSON.toJSONString(updateRoleParam));
		Map<String,Object> updateRoleRe = JSON.parseObject(updateRoleReStr, new TypeReference<Map<String,Object>>(){}.getType());
		if(updateRoleRe == null || !Integer.valueOf(0).equals(updateRoleRe.get("status"))) {
			optLogger.info("更新CRM角色失败，re={}", updateRoleRe);
			log.info("更新CRM角色失败，re={}", updateRoleRe);
			throw new RuntimeException("更新CRM角色失败");
		}
	}
	private void updateCrmResource(String resourceUrl, String resourceName, String updatedByUsername) {
		Map<String,Object> updateResourceParam = new HashMap<>();
		updateResourceParam.put("resourceUrl", resourceUrl);
		updateResourceParam.put("resourceName", resourceName);
		updateResourceParam.put("displayName", resourceName);
		updateResourceParam.put("updatedByUsername", updatedByUsername);
		String updateResourceReStr = CRMHttpClientUtil.doPostJson(CRM_SERVER + RESOURCE_UPDATE_URL, JSON.toJSONString(updateResourceParam));
		Map<String,Object> updateResourceRe = JSON.parseObject(updateResourceReStr, new TypeReference<Map<String,Object>>(){}.getType());
		if(updateResourceRe == null || !Integer.valueOf(0).equals(updateResourceRe.get("status"))) {
			optLogger.info("更新CRM资源失败，re={}", updateResourceReStr);
			log.info("更新CRM资源失败，re={}", updateResourceReStr);
			throw new RuntimeException("更新CRM资源失败");
		}
	}
	
	private void createCrmRole(String roleEnglish, String roleName, Integer parentRoleId, String parentRoleEnglish,
			Integer isLeaf, String createdByUsername) {
		Map<String, Object> roleParam = new HashMap<>();
		roleParam.put("roleGroupId", CRM_ROLE_GROUP_ID);
		roleParam.put("roleEnglish", roleEnglish);
		roleParam.put("roleName", roleName);
		roleParam.put("parentRoleId", parentRoleId);
		roleParam.put("parentRoleEnglish", parentRoleEnglish);
		roleParam.put("isLeaf", isLeaf);
		roleParam.put("createdByUsername", createdByUsername);
		String rolePostReStr = CRMHttpClientUtil.doPostJson(CRM_SERVER + ROLE_CREATE_URL, JSON.toJSONString(roleParam));
		Map<String, Object> rolePostRe = JSON.parseObject(rolePostReStr, new TypeReference<Map<String, Object>>() {
		}.getType());
		if (rolePostRe == null || !Integer.valueOf(0).equals(rolePostRe.get("status"))) {
			optLogger.error("创建CRM角色失败,re={}", rolePostReStr);
			log.error("创建CRM角色失败,re={}", rolePostReStr);
			throw new RuntimeException("创建CRM角色失败");
		}
	}
	
	private void createCrmResource(Integer parentResourceId, String parentResourceUrl, String resourceName, String resourceUrl, Integer isLeaf, String createdByUsername, String resourceDesc) {
		Map<String,Object> resourceParam = new HashMap<>();
		resourceParam.put("resourceTypeId", CRM_RESOURCE_TYPE_ID);
		resourceParam.put("parentResourceId", parentResourceId);
		resourceParam.put("parentResourceUrl", parentResourceUrl);
		resourceParam.put("resourceName", resourceName);
		resourceParam.put("resourceUrl", resourceUrl);
		resourceParam.put("resourceDesc", resourceDesc);
		resourceParam.put("systemCode", CRM_RESOURCE_SYSTEM_CODE_CRM);
		resourceParam.put("isLeaf", isLeaf);
		resourceParam.put("displayName", resourceName);
		resourceParam.put("createdByUsername", createdByUsername);
		String resourcePostReStr = CRMHttpClientUtil.doPostJson(CRM_SERVER + RESOURCE_CREATE_URL, JSON.toJSONString(resourceParam));
		Map<String,Object> resourcePostRe = JSON.parseObject(resourcePostReStr, new TypeReference<Map<String,Object>>(){}.getType());
		if(resourcePostRe == null || !Integer.valueOf(0).equals(resourcePostRe.get("status"))) {
			log.error("创建CRM资源失败,re={}", resourcePostReStr);
			throw new RuntimeException("创建CRM资源失败");
		}
	}

	private void createCrmResource(Integer parentResourceId, String parentResourceUrl, String resourceName,
			String resourceUrl, Integer isLeaf, String createdByUsername) {
		createCrmResource(parentResourceId, parentResourceUrl, resourceName, resourceUrl, isLeaf, createdByUsername,
				null);
	}
	private String assembleResourceUrl(String type, Long id) {
		return "DAVINCI" + "_" + type + "_" + id;
	}
	private String assembleRoleEnglish(String type, Long id) {
		return "ROLE_DAVINCI" + "_" + type + "_" + id;
	}
	@Data
	class CrmRoleResourceCreate{
		private Integer roleId;
		private String roleEnglish;
		private Integer resourceId;
		private String resourceUrl;
	}
}
