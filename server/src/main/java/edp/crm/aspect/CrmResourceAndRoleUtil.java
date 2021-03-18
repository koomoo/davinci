package edp.crm.aspect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import edp.davinci.core.enums.LogNameEnum;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class CrmResourceAndRoleUtil {
	private static final Logger optLogger = LoggerFactory.getLogger(LogNameEnum.BUSINESS_OPERATION.getName());
	//private static final String CRM_SERVER = "http://api.ymt.io/crm-gateway/api";
	private static final String CRM_SERVER = "http://dev-crm-host001.ymt.io:81/api";
	private static final String RESOURCE_CREATE_URL = "/resource";
	private static final String RESOURCE_UPDATE_URL = "/resource/update";
	private static final String RESOURCE_DELETE_URL = "/resource/delete";
	private static final String ROLE_CREATE_URL = "/role";
	private static final String ROLE_UPDATE_URL = "/role/update";
	private static final String ROLE_DELETE_URL = "/role/delete";
	private static final String ROLE_RESOURCE_REL_URL = "/role/resource";
	
	public static void relCrmRoleResource(String createdByUsername, List<CrmRoleResourceCreate> rel) {
		if(CollectionUtils.isEmpty(rel)) return;
		String relRoleResourceReStr = CRMHttpClientUtil.doPostJson(CRM_SERVER + ROLE_RESOURCE_REL_URL + "?createdByUsername=" + createdByUsername, JSON.toJSONString(rel));
		Map<String,Object> relRoleResourceRe = JSON.parseObject(relRoleResourceReStr, new TypeReference<Map<String,Object>>(){}.getType());
		if(relRoleResourceRe == null || !Integer.valueOf(0).equals(relRoleResourceRe.get("status"))) {
			optLogger.info("CRM角色和资源关联失败，re={}", relRoleResourceReStr);
			log.info("CRM角色和资源关联失败，re={}", relRoleResourceReStr);
			throw new RuntimeException("CRM角色和资源关联失败");
		}
	}
	
	public static void deleteCrmRole(String roleEnglish, String deleteByUsername) {
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
	
	public static void deleteCrmResource(String resourceUrl, String deleteByUsername) {
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
	
	public static void updateCrmRole(String roleEnglish, String roleName, String updatedByUsername) {
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
	public static void updateCrmResource(String resourceUrl, String resourceName, String updatedByUsername) {
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
	
	public static void createCrmRole(String roleEnglish, String roleName, Integer parentRoleId, String parentRoleEnglish,
			Integer isLeaf, String createdByUsername) {
		Map<String, Object> roleParam = new HashMap<>();
		roleParam.put("roleGroupId", CrmConstant.CRM_ROLE_GROUP_ID);
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
	
	public static void createCrmResource(Integer parentResourceId, String parentResourceUrl, String resourceName,
			String resourceUrl, Integer isLeaf, String createdByUsername, String resourceDesc, Integer resourceTypeId) {
		Map<String, Object> resourceParam = new HashMap<>();
		resourceParam.put("resourceTypeId", resourceTypeId);
		resourceParam.put("parentResourceId", parentResourceId);
		resourceParam.put("parentResourceUrl", parentResourceUrl);
		resourceParam.put("resourceName", resourceName);
		resourceParam.put("resourceUrl", resourceUrl);
		resourceParam.put("resourceDesc", resourceDesc);
		resourceParam.put("systemCode", CrmConstant.CRM_RESOURCE_SYSTEM_CODE_CRM);
		resourceParam.put("isLeaf", isLeaf);
		resourceParam.put("displayName", resourceName);
		resourceParam.put("createdByUsername", createdByUsername);
		String resourcePostReStr = CRMHttpClientUtil.doPostJson(CRM_SERVER + RESOURCE_CREATE_URL,
				JSON.toJSONString(resourceParam));
		Map<String, Object> resourcePostRe = JSON.parseObject(resourcePostReStr,
				new TypeReference<Map<String, Object>>() {
				}.getType());
		if (resourcePostRe == null || !Integer.valueOf(0).equals(resourcePostRe.get("status"))) {
			log.error("创建CRM资源失败,re={}", resourcePostReStr);
			throw new RuntimeException("创建CRM资源失败");
		}
	}

	public static void createCrmResource(Integer parentResourceId, String parentResourceUrl, String resourceName,
			String resourceUrl, Integer isLeaf, String createdByUsername, Integer resourceTypeId) {
		createCrmResource(parentResourceId, parentResourceUrl, resourceName, resourceUrl, isLeaf, createdByUsername,
				null, resourceTypeId);
	}
	
	public static String assembleResourceUrl(String type, Long id) {
		return "DAVINCI" + "_" + type + "_" + id;
	}
	public static String assembleRoleEnglish(String type, Long id) {
		return "ROLE_DAVINCI" + "_" + type + "_" + id;
	}
}
