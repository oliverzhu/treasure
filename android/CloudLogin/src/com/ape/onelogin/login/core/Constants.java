/*
 * Copyright (C) 2010-2013 The SINA WEIBO Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ape.onelogin.login.core;

/**
 * @author jianwen.zhu
 * @since 2014-3-4
 */
public interface Constants {
    
    /** SINA */
    public static final String APP_KEY_SINA      = "2188110959";
    public static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
    public static final String SCOPE_SINA = 
             "email,direct_messages_read,direct_messages_write,"
             + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
             + "follow_app_official_microblog," + "invitation_write";
    
    /** TENCENT */
    //public static final String APP_KEY_TENCENT      = "1101740780";
    public static final String APP_KEY_TENCENT      = "1103402063";
    public static final String SCOPE_TENCENT = "all";
    
    /** CLOUDLOGIN */
    public static final String APP_KEY_CLOUDLOGIN      = "488e406315b632ad94db48672b31c5d2";
    public static final String APP_NAME_CLOUDLOGIN      = "login";
    
    /** GUEST */
    public static final String GUEST_NAME = "guest";
    
    public static final String SINA_MASK = "sina";
    public static final String TENCENT_MASK = "tencent";
    public static final String PASSWORD_MASK = "(sw_login_service)";
    
    /** Preferences */
    public static final String PREFERENCE_NAME_GUEST = "onelogin_guest_file";
    public static final String KEY_CHECK_CODE = "guest_check_code";
    public static final String KEY_USER_KEY = "guest_user_key";
    
    public static final int TYPE_SDK_NONE = 0;
    public static final int TYPE_SDK_SINA = 1;
    public static final int TYPE_SDK_TENCENT = 2;
    public static final int TYPE_SDK_TINNO = 3;
    
    public static final String ACTION_ONELOGIN_ACCESS = "com.ape.onelogin.login.core.Action.ONELOGIN_ACCESS";
    
    public static final String REQ_SEND_MAIL_PARAM_1 = "param1";
    public static final String REQ_SEND_MAIL_PARAM_2 = "param2";
    
    public static final String AVATAR_LOCAL_DIR      = "/avatar";
    
    public static final String ACCOUNT_TYPE = "com.ape.onelogin";
    public static final String AUTHTOKEN_TYPE = "com.ape.onelogin";
}
