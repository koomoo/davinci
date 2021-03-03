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
    		+ "|| execution(* edp.davinci.service.impl.DashboardPortalServiceImpl.createDashboardPortal(..)) "
    		+ "|| execution(* edp.davinci.service.impl.DashboardPortalServiceImpl.deleteDashboardPortal(..)) "
    		+ "|| execution(* edp.davinci.service.impl.DashboardServiceImpl.createDashboard(..)) "
    		+ "|| execution(* edp.davinci.service.impl.DashboardServiceImpl.deleteDashboard(..)) "
    		)
    public void afterPointcut() {
    }
    
    @Before("beforePointcut()")
    public void beforeMethod(JoinPoint joinPoint) throws Exception {
    		//这里要先查询表里
    		String serviceName = joinPoint.getTarget().getClass().getSimpleName();
		String methodName = joinPoint.getSignature().getName();
		if(PROJECT_SERVICE_NAME.equals(serviceName)) {
			//project
			if(PROJECT_UPDATE_METHOD_NAME.equals(methodName)) {
				updateProject(joinPoint);
			}else if(PROJECT_DELETE_METHOD_NAME.equals(methodName)) {
				deleteProject(joinPoint);
			}
		}else if(DASHBOARD_PORTAL_SERVICE_NAME.equals(serviceName)) {
			if(DASHBOARD_PORTAL_UPDATE_METHOD_NAME.equals(methodName)) {
				updateDashboardPortal(joinPoint);
			}
		}else if(DASHBOARD_SERVICE_NAME.equals(serviceName)) {
			if(DASHBOARD_UPDATE_METHOD_NAME.equals(methodName)) {
				updateDashboard(joinPoint);
			}
		}
    }
    
    @AfterReturning(pointcut="afterPointcut()", returning="methodRe")
    public void afterMethod(JoinPoint joinPoint, Object methodRe) throws Exception {
    		String className = joinPoint.getTarget().getClass().getSimpleName();
    		String methodName = joinPoint.getSignature().getName();
    		if(PROJECT_SERVICE_NAME.equals(className)) {
    			//project
    			if(PROJECT_CREAT_METHOD_NAME.equals(methodName)) {
    				//增
    				createProject(joinPoint, methodRe);
    			}
    		}else if(DASHBOARD_PORTAL_SERVICE_NAME.equals(className)) {
    			//dashboard_portal
    			if(DASHBOARD_PORTAL_CREAT_METHOD_NAME.equals(methodName)) {
    				//增
    				createDashboardPortal(joinPoint, methodRe);
    			}else if(DASHBOARD_PORTAL_DELETE_METHOD_NAME.equals(methodName)) {
    				//删
    				deleteDashboardPortal(joinPoint, methodRe);
    			}
    		}else if(DASHBOARD_SERVICE_NAME.equals(className)) {
    			//dashboard
    			if(DASHBOARD_CREAT_METHOD_NAME.equals(methodName)) {
    				//增
    				createDashboard(joinPoint, methodRe);
    			}else if(DASHBOARD_DELETE_METHOD_NAME.equals(methodName)) {
    				//删
    				deleteDashboard(joinPoint, methodRe);
    			}
    		}
    }

	private void deleteDashboard(JoinPoint joinPoint, Object methodRe) {
		Long id = (Long)joinPoint.getArgs()[0];
		User user = (User)joinPoint.getArgs()[1];
		
		deleteCrmResource(assembleResourceUrl(TYPE_DASHBOARD, id), user.getUsername());
	}
	
	private void updateDashboard(JoinPoint joinPoint) {
		DashboardDto[] dashboards = (DashboardDto[]) joinPoint.getArgs()[1];
		User user = (User) joinPoint.getArgs()[2];

		for (DashboardDto dashboard : dashboards) {
			updateCrmResource(assembleResourceUrl(TYPE_DASHBOARD, dashboard.getId()), dashboard.getName(),
					user.getUsername());
		}
	}

	private void createDashboard(JoinPoint joinPoint, Object methodRe) throws Exception {
		DashboardCreate dashboardCreate = (DashboardCreate) joinPoint.getArgs()[0];
		User user = (User) joinPoint.getArgs()[1];
		Dashboard dashboard = (Dashboard) methodRe;
		createCrmResource(null, assembleResourceUrl(TYPE_DASHBOARD_PORTAL, dashboardCreate.getDashboardPortalId()),
				dashboardCreate.getName(), assembleResourceUrl(TYPE_DASHBOARD, dashboard.getId()), 1,
				user.getUsername(), assembleShareUrl(dashboard.getId(), user));

		List<CrmRoleResourceCreate> rel = Lists.newArrayList();
		CrmRoleResourceCreate crmRoleResourceCreate = new CrmRoleResourceCreate();
		crmRoleResourceCreate
				.setRoleEnglish(assembleRoleEnglish(TYPE_DASHBOARD_PORTAL, dashboardCreate.getDashboardPortalId()));
		crmRoleResourceCreate.setResourceUrl(assembleResourceUrl(TYPE_DASHBOARD, dashboard.getId()));
		rel.add(crmRoleResourceCreate);
		relCrmRoleResource(user.getUsername(), rel);
	}

	private String assembleShareUrl(Long id, User user) throws Exception {
		ShareEntity shareEntity = new ShareEntity();
		shareEntity.setMode(ShareMode.NORMAL);
		shareEntity.setExpired(DateUtils.getUtilDate("2050-03-18 17:07:42", "yyyy-MM-dd HH:mm:ss"));
		ShareResult shareResult = dashboardService.shareDashboard(id, user, shareEntity);
		return "/dav/share.html?shareToken=" + shareResult.getToken() + "#share/dashboard";
	}

	private void deleteDashboardPortal(JoinPoint joinPoint, Object methodRe) {
		Long id = (Long) joinPoint.getArgs()[0];
		User user = (User) joinPoint.getArgs()[1];
		deleteCrmResource(assembleResourceUrl(TYPE_DASHBOARD_PORTAL, id), user.getUsername());
		deleteCrmRole(assembleRoleEnglish(TYPE_DASHBOARD_PORTAL, id), user.getUsername());
	}

	private void updateDashboardPortal(JoinPoint joinPoint) {
		DashboardPortalUpdate dashboardPortalUpdate = (DashboardPortalUpdate) joinPoint.getArgs()[0];
		User user = (User) joinPoint.getArgs()[1];
		updateCrmResource(assembleResourceUrl(TYPE_DASHBOARD_PORTAL, dashboardPortalUpdate.getId()),
				dashboardPortalUpdate.getName(), user.getUsername());
		updateCrmRole(assembleRoleEnglish(TYPE_DASHBOARD_PORTAL, dashboardPortalUpdate.getId()),
				dashboardPortalUpdate.getName() + "管理员", user.getUsername());
	}

	private void createDashboardPortal(JoinPoint joinPoint, Object methodRe) {
		DashboardPortalCreate dashboardPortalCreat = (DashboardPortalCreate) joinPoint.getArgs()[0];
		User user = (User) joinPoint.getArgs()[1];
		DashboardPortal dashboardPortal = (DashboardPortal) methodRe;
		createCrmResource(null, assembleResourceUrl(TYPE_PROJECT, dashboardPortalCreat.getProjectId()),
				dashboardPortalCreat.getName(), assembleResourceUrl(TYPE_DASHBOARD_PORTAL, dashboardPortal.getId()), 0,
				user.getUsername());
		createCrmRole(assembleRoleEnglish(TYPE_DASHBOARD_PORTAL, dashboardPortal.getId()),
				dashboardPortalCreat.getName() + "管理员", null,
				assembleRoleEnglish(TYPE_PROJECT, dashboardPortalCreat.getProjectId()), 0, user.getUsername());
		
		List<CrmRoleResourceCreate> rel = Lists.newArrayList();
		CrmRoleResourceCreate crmSystemManageMenuRel = new CrmRoleResourceCreate();
		crmSystemManageMenuRel.setRoleEnglish(assembleRoleEnglish(TYPE_DASHBOARD_PORTAL, dashboardPortal.getId()));
		crmSystemManageMenuRel.setResourceId(CRM_RESOURCE_MENU_SYSTEMMANAGER);
		rel.add(crmSystemManageMenuRel);
		
		CrmRoleResourceCreate crmDavinciMenuRel = new CrmRoleResourceCreate();
		crmDavinciMenuRel.setRoleEnglish(assembleRoleEnglish(TYPE_DASHBOARD_PORTAL, dashboardPortal.getId()));
		crmDavinciMenuRel.setResourceId(CRM_RESOURCE_MENU_DAVINCI);
		rel.add(crmDavinciMenuRel);
		
		CrmRoleResourceCreate projectResourceRel = new CrmRoleResourceCreate();
		projectResourceRel.setRoleEnglish(assembleRoleEnglish(TYPE_DASHBOARD_PORTAL, dashboardPortal.getId()));
		projectResourceRel.setResourceUrl(assembleResourceUrl(TYPE_PROJECT, dashboardPortalCreat.getProjectId()));
		rel.add(projectResourceRel);
		
		CrmRoleResourceCreate dashboardPortalResourceRel = new CrmRoleResourceCreate();
		dashboardPortalResourceRel.setRoleEnglish(assembleRoleEnglish(TYPE_DASHBOARD_PORTAL, dashboardPortal.getId()));
		dashboardPortalResourceRel.setResourceUrl(assembleResourceUrl(TYPE_DASHBOARD_PORTAL, dashboardPortal.getProjectId()));
		rel.add(dashboardPortalResourceRel);
		
		CrmRoleResourceCreate authResourceRel = new CrmRoleResourceCreate();
		authResourceRel.setRoleEnglish(assembleRoleEnglish(TYPE_DASHBOARD_PORTAL, dashboardPortal.getId()));
		authResourceRel.setResourceId(CRM_DAVINCI_AUTH_RESOURCE_ID);
		rel.add(authResourceRel);
		
		relCrmRoleResource(user.getUsername(), rel);
	}

	private void deleteProject(JoinPoint joinPoint) {
		Long projectId = (Long) joinPoint.getArgs()[0];
		User user = (User) joinPoint.getArgs()[1];
		
		ProjectDetail projectDetail = projectService.getProjectDetail(projectId, user, true);
		if(!DAVINCI_ORGANIZE_CRM_ID.equals(projectDetail.getOrgId())) return;
		
		deleteCrmResource(assembleResourceUrl(TYPE_PROJECT, projectId), user.getUsername());
		deleteCrmRole(assembleRoleEnglish(TYPE_PROJECT, projectId), user.getUsername());
	}

	private void updateProject(JoinPoint joinPoint) throws Exception {
		Long projectId = (Long)joinPoint.getArgs()[0];
		ProjectUpdate projectUpdate = (ProjectUpdate)joinPoint.getArgs()[1];
		User user = (User)joinPoint.getArgs()[2];
		String name = projectUpdate.getName();
		ProjectDetail projectDetail = projectService.getProjectDetail(projectId, user, true);
		if(projectDetail.getName().equals(name)) return;
		if(!DAVINCI_ORGANIZE_CRM_ID.equals(projectDetail.getOrgId())) return;

		updateCrmResource(assembleResourceUrl(TYPE_PROJECT, projectId), projectUpdate.getName(), user.getUsername());
		updateCrmRole(assembleRoleEnglish(TYPE_PROJECT, projectId), projectUpdate.getName(), user.getUsername());
	}

	private void createProject(JoinPoint joinPoint, Object methodRe) {
		ProjectCreat projectCreat = (ProjectCreat)joinPoint.getArgs()[0];
		User user = (User)joinPoint.getArgs()[1];
		ProjectInfo projectInfo = (ProjectInfo)methodRe;
		if(!DAVINCI_ORGANIZE_CRM_ID.equals(projectCreat.getOrgId())) return;
		
		createCrmResource(CRM_RESOURCE_MENU_DAVINCI, null, projectCreat.getName(), assembleResourceUrl(TYPE_PROJECT, projectInfo.getId()), 0, user.getUsername());
		createCrmRole(assembleRoleEnglish(TYPE_PROJECT, projectInfo.getId()), projectInfo.getName(), CRM_ROLE_MENU, null, 0, user.getUsername());
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
