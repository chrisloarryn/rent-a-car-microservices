package com.kodlamaio.commonpackage.utils.constants;

public class Roles
{
    private Roles() {
    }

    public static final String User = "hasRole('user')";
    public static final String UserOrAbove = "hasAnyRole('user', 'admin', 'moderator')";
    public static final String AdminOrModerator = "hasAnyRole('admin', 'moderator')";
}
