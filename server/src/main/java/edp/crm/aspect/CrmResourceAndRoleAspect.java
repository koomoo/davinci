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

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import edp.crm.util.HttpClientUtil;
import edp.davinci.core.enums.LogNameEnum;
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

    @After("pointcut()")
    public void afterMethod(JoinPoint joinPoint) throws Exception {
    		String className = joinPoint.getTarget().getClass().getSimpleName();
    		String methodName = joinPoint.getSignature().getName();
    		if(PROJECT_SERVICE_NAME.equals(className)) {
    			//project
    			if(PROJECT_CREAT_METHOD_NAME.equals(methodName)) {
    				//增
    				createProject(joinPoint);
    			}else if(PROJECT_UPDATE_METHOD_NAME.equals(methodName)) {
    				//改
    				updateProject(joinPoint);
    			}else if(PROJECT_DELETE_METHOD_NAME.equals(methodName)) {
    				//删
    				deleteProject(joinPoint);
    			}
    		}else if(DASHBOARD_PORTAL_SERVICE_NAME.equals(className)) {
    			//dashboard_portal
    			if(DASHBOARD_PORTAL_CREAT_METHOD_NAME.equals(methodName)) {
    				//增
    				createDashboardPortal(joinPoint);
    			}else if(DASHBOARD_PORTAL_UPDATE_METHOD_NAME.equals(methodName)) {
    				//改
    				updateDashboardPortal(joinPoint);
    			}else if(DASHBOARD_PORTAL_DELETE_METHOD_NAME.equals(methodName)) {
    				//删
    				deleteDashboardPortal(joinPoint);
    			}
    		}else if(DASHBOARD_SERVICE_NAME.equals(className)) {
    			//dashboard
    			if(DASHBOARD_CREAT_METHOD_NAME.equals(methodName)) {
    				//增
    				createDashboard(joinPoint);
    			}else if(DASHBOARD_UPDATE_METHOD_NAME.equals(methodName)) {
    				//改
    				updateDashboard(joinPoint);
    			}else if(DASHBOARD_DELETE_METHOD_NAME.equals(methodName)) {
    				//删
    				deleteDashboard(joinPoint);
    			}
    		}
    }

	private void deleteDashboard(JoinPoint joinPoint) {
		// TODO Auto-generated method stub
		System.out.println("deleteDashboard");
	}

	private void updateDashboard(JoinPoint joinPoint) {
		// TODO Auto-generated method stub
		System.out.println("updateDashboard");
	}

	private void createDashboard(JoinPoint joinPoint) {
		// TODO Auto-generated method stub
		System.out.println("createDashboard");
	}

	private void deleteDashboardPortal(JoinPoint joinPoint) {
		// TODO Auto-generated method stub
		System.out.println("deleteDashboardPortal");
	}

	private void updateDashboardPortal(JoinPoint joinPoint) {
		// TODO Auto-generated method stub
		System.out.println("updateDashboardPortal");
	}

	private void createDashboardPortal(JoinPoint joinPoint) {
		// TODO Auto-generated method stub
		System.out.println("createDashboardPortal");
	}

	private void deleteProject(JoinPoint joinPoint) {
		// TODO Auto-generated method stub
		System.out.println("deleteProject");
	}

	private void updateProject(JoinPoint joinPoint) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("updateProject");
		log.info("updateProduct后置通知");
		String doGet = HttpClientUtil.doPost("http://www.baidu.com",null);
		System.out.println(doGet);
	}

	private void createProject(JoinPoint joinPoint) {
		// TODO Auto-generated method stub
		System.out.println("createProject");
		
		//1.在一个固定菜单【1782】下创建一个资源，资源的resource_url与davinci的projectId有关，用于创建dashboard_portal的时候查询父resourceId
		
		//2.创建一个角色 在407下，依然是个不可选的父角色，但是要和projectId有关系，因为在创建portal的时候要创建角色，这个角色要挂在该项目对应的角色下
		
	}
}
