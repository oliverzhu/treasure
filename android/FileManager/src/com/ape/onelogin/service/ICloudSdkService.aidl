package com.ape.onelogin.service;

import com.ape.onelogin.service.IQueryCompletionCallback;
import com.ape.onelogin.service.IModifyCompletionCallback;
import com.ape.onelogin.service.IAvatarCompletionCallback;

interface ICloudSdkService {
    boolean isSessionValid();
    String getUserName();
    void getAvatar();
    void getUserKey();
    void modifyUserInfo(in Map userInfoMap); 
    void registerQueryCallback(IQueryCompletionCallback callback);
    void unRegisterQueryCallback(IQueryCompletionCallback callback);
    void registerModifyCallback(IModifyCompletionCallback callback);
    void unRegisterModifyCallback(IModifyCompletionCallback callback);
    void registerAvatarCallback(IAvatarCompletionCallback callback);
    void unRegisterAvatarCallback(IAvatarCompletionCallback callback);
    void logout();
}