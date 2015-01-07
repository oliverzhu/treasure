package com.ape.onelogin.authenticator;

import com.ape.onelogin.OneLoginApplication;
import com.ape.onelogin.login.core.Constants;
import com.ape.onelogin.myos.ui.LoginActivity;
import com.ape.onelogin.util.LogUtil;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;

public class Authenticator extends AbstractAccountAuthenticator {

    private Context mContext;
    private LogUtil mLogUtil;
    
    public Authenticator(Context context) {
        super(context);
        mContext = context;
        mLogUtil = new LogUtil("Authenticator");
    }
    
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
            String authTokenType, String[] requiredFeatures, Bundle options)
            throws NetworkErrorException {
        mLogUtil.i("enter addAccount()");
        Bundle bundle = new Bundle();
        AccountManager accountManager = AccountManager.get(mContext);
        Account[] accounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        if (accounts.length == 0) {
            Intent intent = new Intent(mContext, LoginActivity.class);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        } else {
            
        }
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response,
            Account account, Bundle options) throws NetworkErrorException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response,
            String accountType) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response,
            Account account, String authTokenType, Bundle options)
            throws NetworkErrorException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response,
            Account account, String[] features) throws NetworkErrorException {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response,
            Account account, String authTokenType, Bundle options)
            throws NetworkErrorException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bundle getAccountRemovalAllowed(
            AccountAuthenticatorResponse response, Account account)
            throws NetworkErrorException {
        mLogUtil.i("enter getAccountRemovalAllowed()");
        LoginActivity.closeLoginHandler();
        if (OneLoginApplication.sCloudSdkService != null) {
            try {
                mLogUtil.w("logout", "<Account Logout>userName:%s", account.name);
                OneLoginApplication.sCloudSdkService.logout();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return super.getAccountRemovalAllowed(response, account);
    }
}
