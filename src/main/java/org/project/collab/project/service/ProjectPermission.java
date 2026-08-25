package org.project.collab.project.service;
import org.project.collab.common.error.*; import org.project.collab.project.domain.*;
public final class ProjectPermission {private ProjectPermission(){} public static void administer(ProjectMember m){if(!m.getRole().canAdminister())throw new BusinessException(ErrorCode.PROJECT_FORBIDDEN);} public static void owner(ProjectMember m){if(m.getRole()!=ProjectRole.OWNER)throw new BusinessException(ErrorCode.PROJECT_FORBIDDEN);} }
