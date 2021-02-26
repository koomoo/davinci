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
import java.util.Map;

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

import edp.crm.util.HttpClientUtil;
import edp.davinci.core.enums.LogNameEnum;
import edp.davinci.dto.projectDto.ProjectCreat;
import edp.davinci.dto.projectDto.ProjectDetail;
import edp.davinci.dto.projectDto.ProjectInfo;
import edp.davinci.dto.projectDto.ProjectUpdate;
import edp.davinci.model.User;
import edp.davinci.service.ProjectService;
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
	private static final String TYPE_PORTAL = "PORTAL";
	private static final String TYPE_DASHBOARD = "DASHBOARD";
	
	private static final Integer CRM_RESOURCE_TYPE_ID = 2;
	private static final Integer CRM_RESOURCE_MENU = 1782;
	private static final String CRM_RESOURCE_SYSTEM_CODE_CRM = "CRM";
	private static final Integer CRM_ROLE_MENU = 407;
	private static final Integer CRM_ROLE_GROUP_ID = 1;
	
	private static final String CRM_SERVER = "http://api.ymt.io/crm-gateway/api";
	private static final String RESOURCE_CREATE_URL = "/resource";
	private static final String RESOURCE_UPDATE_URL = "/resource/update";
	private static final String ROLE_CREATE_URL = "/role";
	private static final String ROLE_UPDATE_URL = "/role";
	
	@Autowired
	private ProjectService projectService;
	
	@Pointcut(
    		"execution(* edp.davinci.service.impl.ProjectServiceImpl.updateProject(..)) "
    		+ "|| execution(* edp.davinci.service.impl.DashboardPortalServiceImpl.updateDashboardPortal(..)) "
    		+ "|| execution(* edp.davinci.service.impl.DashboardServiceImpl.updateDashboards(..)) "
    		)
    public void beforePointcut() {
    }
	
	//切点
    @Pointcut("execution(* edp.davinci.service.impl.ProjectServiceImpl.createProject(..)) "
    		+ "|| execution(* edp.davinci.service.impl.ProjectServiceImpl.deleteProject(..)) "
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
    			}else if(PROJECT_DELETE_METHOD_NAME.equals(methodName)) {
    				//删
    				deleteProject(joinPoint, methodRe);
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
		// TODO Auto-generated method stub
		System.out.println("deleteDashboard");
	}

	private void updateDashboard(JoinPoint joinPoint) {
		// TODO Auto-generated method stub
		System.out.println("updateDashboard");
	}

	private void createDashboard(JoinPoint joinPoint, Object methodRe) {
		// TODO Auto-generated method stub
		System.out.println("createDashboard");
	}

	private void deleteDashboardPortal(JoinPoint joinPoint, Object methodRe) {
		// TODO Auto-generated method stub
		System.out.println("deleteDashboardPortal");
	}

	private void updateDashboardPortal(JoinPoint joinPoint) {
		// TODO Auto-generated method stub
		System.out.println("updateDashboardPortal");
	}

	private void createDashboardPortal(JoinPoint joinPoint, Object methodRe) {
		// TODO Auto-generated method stub
		System.out.println("createDashboardPortal");
	}

	private void deleteProject(JoinPoint joinPoint, Object methodRe) {
		// TODO Auto-generated method stub
		System.out.println("deleteProject");
	}

	private void updateProject(JoinPoint joinPoint) throws Exception {
		//只有修改项目名的时候需要同步相应的资源和角色
		Object[] args = joinPoint.getArgs();
		Long projectId = (Long)args[0];
		ProjectUpdate projectUpdate = (ProjectUpdate)args[1];
		User user = (User)args[2];
		String name = projectUpdate.getName();
		ProjectDetail projectDetail = projectService.getProjectDetail(projectId, user, true);
		if(projectDetail.getName().equals(name)) return;
		
		Map<String,Object> updateResourceParam = new HashMap<>();
		updateResourceParam.put("resourceUrl", assembleResourceUrl(TYPE_PROJECT, projectId));
		updateResourceParam.put("resourceName", projectUpdate.getName());
		updateResourceParam.put("displayName", projectUpdate.getName());
		updateResourceParam.put("updatedByUsername", user.getUsername());
		String updateResourceReStr = HttpClientUtil.doPostJson(CRM_SERVER + RESOURCE_UPDATE_URL, JSON.toJSONString(updateResourceParam));
		Map<String,Object> updateResourceRe = JSON.parseObject(updateResourceReStr, new TypeReference<Map<String,Object>>(){}.getType());
		if(updateResourceRe == null || !Integer.valueOf(0).equals(updateResourceRe.get("status"))) {
			optLogger.info("更新CRM资源失败，re={}", updateResourceReStr);
			throw new RuntimeException("更新CRM资源失败");
		}
		
		//刘飞 更改角色名
	}

	private void createProject(JoinPoint joinPoint, Object methodRe) {
		ProjectCreat projectCreat = (ProjectCreat)joinPoint.getArgs()[0];
		User user = (User)joinPoint.getArgs()[1];
		
		Map<String,Object> resourceParam = new HashMap<>();
		resourceParam.put("resourceTypeId", CRM_RESOURCE_TYPE_ID);
		resourceParam.put("parentResourceId", CRM_RESOURCE_MENU);
		resourceParam.put("resourceName", projectCreat.getName());
		ProjectInfo projectInfo = (ProjectInfo)methodRe;
		resourceParam.put("resourceUrl", assembleResourceUrl(TYPE_PROJECT, projectInfo.getId()));
		resourceParam.put("systemCode", CRM_RESOURCE_SYSTEM_CODE_CRM);
		resourceParam.put("displayName", projectCreat.getName());
		resourceParam.put("createdByUsername", user.getUsername());
		String resourcePostReStr = HttpClientUtil.doPostJson(CRM_SERVER + RESOURCE_CREATE_URL, JSON.toJSONString(resourceParam));
		Map<String,Object> resourcePostRe = JSON.parseObject(resourcePostReStr, new TypeReference<Map<String,Object>>(){}.getType());
		if(resourcePostRe == null || Integer.valueOf(0).equals(resourcePostRe.get("status"))) {
			optLogger.error("创建CRM资源失败,erroMsg={}", resourcePostRe.get("msg"));
			throw new RuntimeException("创建CRM资源失败");
		}
		
		//创建角色
		Map<String,Object> roleParam = new HashMap<>();
		roleParam.put("roleGroupId", CRM_ROLE_GROUP_ID);
		roleParam.put("roleEnglish", assembleRoleEnglish(TYPE_PROJECT, projectInfo.getId()));
		roleParam.put("roleName", assembleRoleName(TYPE_PROJECT, projectInfo.getName()));
		roleParam.put("parentRoleId", CRM_ROLE_MENU);
		roleParam.put("isLeaf", 0);
		roleParam.put("createdByUsername", user.getUsername());
		String rolePostReStr = HttpClientUtil.doPostJson(CRM_SERVER + ROLE_CREATE_URL, JSON.toJSONString(roleParam));
		Map<String,Object> rolePostRe = JSON.parseObject(rolePostReStr, new TypeReference<Map<String,Object>>(){}.getType());
		if(rolePostRe == null || Integer.valueOf(0).equals(rolePostRe.get("status"))) {
			optLogger.error("创建CRM角色失败,erroMsg={}", rolePostRe.get("msg"));
			throw new RuntimeException("创建CRM角色失败");
		}
	}

	private String assembleResourceUrl(String type, Long id) {
		return "DAVINCI" + "_" + type + "_" + id;
	}
	private String assembleRoleEnglish(String type, Long id) {
		return "ROLE_DAVINCI" + "_" + type + "_" + id;
	}
	private String assembleRoleName(String type, String name) {
		return name + "_" + type + "_" + "管理员";
	}
}
