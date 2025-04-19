package com.leave.management.system.constants;

public class ServerRoutes {
    public final static String AUTH = "/api/auth";
    public final static String TEST = "";
    public final static String POST = "/api/post";
    public final static String LIKE = "/api/like";
    public static final String COMMENT = "/api/comment";

    public final static String POST_GET = POST;
    public final static String POST_UPDATE = POST+"/update";
    public final static String POST_GET_BY_ID = POST+"/get";
    public final static String POST_SEARCH = POST+"/search";
    public final static String POST_DELETE = POST+"/delete";
}
