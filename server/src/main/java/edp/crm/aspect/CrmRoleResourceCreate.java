package edp.crm.aspect;

import lombok.Data;

@Data
public class CrmRoleResourceCreate {
	private Integer roleId;
	private String roleEnglish;
	private Integer resourceId;
	private String resourceUrl;
}
