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

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import edp.crm.util.HttpClientUtil;
import edp.davinci.core.enums.LogNameEnum;
import edp.davinci.dto.projectDto.ProjectCreat;
import edp.davinci.dto.projectDto.ProjectInfo;
import edp.davinci.dto.projectDto.ProjectUpdate;
import edp.davinci.model.Project;
import edp.davinci.model.User;
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
	
	private static final Integer CRM_RESOURCE_TYPE_ID_URL = 2;
	private static final Integer CRM_DAVINCI_MENU_RESOURCE_ID = 1782;
	private static final String CRM_RESOURCE_SYSTEM_CODE_CRM = "CRM";
	
	//切点
    @Pointcut("execution(* edp.davinci.service.impl.ProjectServiceImpl.createProject(..)) "
    		+ "|| execution(* edp.davinci.service.impl.ProjectServiceImpl.updateProject(..)) "
    		+ "|| execution(* edp.davinci.service.impl.ProjectServiceImpl.deleteProject(..)) "
    		+ "|| execution(* edp.davinci.service.impl.DashboardPortalServiceImpl.createDashboardPortal(..)) "
    		+ "|| execution(* edp.davinci.service.impl.DashboardPortalServiceImpl.updateDashboardPortal(..)) "
    		+ "|| execution(* edp.davinci.service.impl.DashboardPortalServiceImpl.deleteDashboardPortal(..)) "
    		+ "|| execution(* edp.davinci.service.impl.DashboardServiceImpl.createDashboard(..)) "
    		+ "|| execution(* edp.davinci.service.impl.DashboardServiceImpl.updateDashboards(..)) "
    		+ "|| execution(* edp.davinci.service.impl.DashboardServiceImpl.deleteDashboard(..)) "
    		)
    public void pointcut() {
    }

    @AfterReturning(pointcut="pointcut()", returning="methodRe")
    public void afterMethod(JoinPoint joinPoint, Object methodRe) throws Exception {
    		String className = joinPoint.getTarget().getClass().getSimpleName();
    		String methodName = joinPoint.getSignature().getName();
    		if(PROJECT_SERVICE_NAME.equals(className)) {
    			//project
    			if(PROJECT_CREAT_METHOD_NAME.equals(methodName)) {
    				//增
    				createProject(joinPoint, methodRe);
    			}else if(PROJECT_UPDATE_METHOD_NAME.equals(methodName)) {
    				//改
    				updateProject(joinPoint, methodRe);
    			}else if(PROJECT_DELETE_METHOD_NAME.equals(methodName)) {
    				//删
    				deleteProject(joinPoint, methodRe);
    			}
    		}else if(DASHBOARD_PORTAL_SERVICE_NAME.equals(className)) {
    			//dashboard_portal
    			if(DASHBOARD_PORTAL_CREAT_METHOD_NAME.equals(methodName)) {
    				//增
    				createDashboardPortal(joinPoint, methodRe);
    			}else if(DASHBOARD_PORTAL_UPDATE_METHOD_NAME.equals(methodName)) {
    				//改
    				updateDashboardPortal(joinPoint, methodRe);
    			}else if(DASHBOARD_PORTAL_DELETE_METHOD_NAME.equals(methodName)) {
    				//删
    				deleteDashboardPortal(joinPoint, methodRe);
    			}
    		}else if(DASHBOARD_SERVICE_NAME.equals(className)) {
    			//dashboard
    			if(DASHBOARD_CREAT_METHOD_NAME.equals(methodName)) {
    				//增
    				createDashboard(joinPoint, methodRe);
    			}else if(DASHBOARD_UPDATE_METHOD_NAME.equals(methodName)) {
    				//改
    				updateDashboard(joinPoint, methodRe);
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

	private void updateDashboard(JoinPoint joinPoint, Object methodRe) {
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

	private void updateDashboardPortal(JoinPoint joinPoint, Object methodRe) {
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

	private void updateProject(JoinPoint joinPoint, Object methodRe) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("updateProject");
		log.info("updateProject后置通知");
		String doGet = HttpClientUtil.doPost("http://www.baidu.com",null);
		System.out.println(doGet);
		Object[] args = joinPoint.getArgs();
		int i = 1;
		for (Object object : args) {
			System.out.println("第" + i);
			System.out.println(object);
			i++;
		}
		Long id = (Long)args[0];
		System.out.println(id);
		ProjectUpdate projectUpdate = (ProjectUpdate) args[1];
		System.out.println(JSON.toJSONString(projectUpdate));
		User user = (User) args[2];
		System.out.println(JSON.toJSONString(user));
	}

	private void createProject(JoinPoint joinPoint, Object methodRe) {
		ProjectCreat projectCreat = (ProjectCreat)joinPoint.getArgs()[0];
		User user = (User)joinPoint.getArgs()[1];
		Map<String,Object> param = new HashMap<>();
		param.put("resourceTypeId", CRM_RESOURCE_TYPE_ID_URL);
		param.put("parentResourceId", CRM_DAVINCI_MENU_RESOURCE_ID);
		param.put("resourceName", projectCreat.getName());
		ProjectInfo projectInfo = (ProjectInfo)methodRe;
		param.put("resourceUrl", assembleResourceUrl(projectInfo.getId()));
		param.put("systemCode", CRM_RESOURCE_SYSTEM_CODE_CRM);
		param.put("displayName", projectCreat.getName());
		String postReStr = HttpClientUtil.doPostJson("刘飞 创建资源的接口", JSON.toJSONString(param));
		Map<String,Object> postRe = JSON.parseObject(postReStr, new TypeReference<Map<String,Object>>(){}.getType());
		if(postRe == null || Integer.valueOf(0).equals(postRe.get("status"))) {
			optLogger.error("创建CRM资源失败,erroMsg={}", postRe.get("msg"));
		}
		
		//创建角色
		//2.创建一个角色 在407下，依然是个不可选的父角色，但是要和projectId有关系，因为在创建portal的时候要创建角色，这个角色要挂在该项目对应的角色下
		
	}
	
	private String assembleResourceUrl(Long projectId) {
		return "DAVINCI_PROJECT_" + projectId;
	}
}
