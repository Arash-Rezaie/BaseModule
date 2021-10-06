package com.arash.basemodule.models.nonentities;

import com.arash.basemodule.tools.Utils;

import java.io.Serializable;

public class PermissionPackage implements Serializable {
    private String permission;
    private String explanation;

    public PermissionPackage() {
    }

    public PermissionPackage(String permission) {
        this.permission = permission;
    }

    public PermissionPackage(String permission, String explanation) {
        this.permission = permission;
        this.explanation = explanation;
    }

    public PermissionPackage(String permission, int explanationId) {
        this.permission = permission;
        this.explanation = Utils.getString(explanationId);
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
