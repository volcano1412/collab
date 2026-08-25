package org.project.collab.project.domain;
public enum ProjectRole { OWNER,ADMIN,MEMBER; public boolean canAdminister(){return this==OWNER||this==ADMIN;} public boolean canManageAnyTask(){return canAdminister();} }
