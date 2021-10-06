package com.arash.basemodule.controllers;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.arash.basemodule.R;
import com.arash.basemodule.constants.RequestCode;
import com.arash.basemodule.models.nonentities.PermissionPackage;
import com.arash.basemodule.tools.Utils;
import com.arash.basemodule.tools.sessionmanager.SessionRepository;
import com.arash.basemodule.tools.vmvglue.BindProcessor;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class BaseActivity extends AppCompatActivity {
    /**
     * Usually, you need to init each activity by calling a set of methods to bind views to xml, hire a view-model, request some permissions,...
     * to make them all easier, call this method<br/>
     * It is better to call this method in onStart() method
     *
     * @param hierarchyScanLevel  force BindProcessor to scan parents. 0: scan no parent, -1: do not bind any object
     * @param viewModelClass      any view-model you need
     * @param onPermissionGranted @ListenFor is used to listen to signals. Trigger this method with this annotation when permissions are granted
     * @param permissionPackages  any permission you need
     */
    protected SessionRepository.Session initActivity(int hierarchyScanLevel, Class<?> viewModelClass, String onPermissionGranted, PermissionPackage... permissionPackages) {
        SessionRepository.Session session = null;
        try {
            if (hierarchyScanLevel >= 0) {
                Object viewModel = null;
                if (viewModelClass != null) {
                    viewModel = BindProcessor.getViewModel(this);
                    if (viewModel == null)
                        viewModel = viewModelClass.newInstance();
                }
                beforeBindingElements();
                BindProcessor.init(this, hierarchyScanLevel, viewModel, true);
                afterBindingElements();
                getIntent().putExtra("bindProcessorEnabled", true);
            }

            if (onPermissionGranted != null) {
                session = SessionRepository.getSession(getSessionName());
                session.register(this);
                if (permissionPackages.length > 0)
                    checkPermissions(session.getName(), onPermissionGranted, permissionPackages);
                getIntent().putExtra("sessionTriggered", true);
            }
        } catch (IllegalAccessException e) {
            Utils.log(e);
        } catch (InstantiationException e) {
            Utils.log(e);
        }
        return session;
    }

    /**
     * If you use initActivity() and pass a view-model class, this method will get call before binding elements
     */
    protected void beforeBindingElements() {
    }

    /**
     * If you use initActivity() and pass a view-model class, this method will get call before loading data into elements
     */
    protected void beforeLoadingDataIntoViewFromViewModel() {
    }

    /**
     * If you use initActivity() and pass a view-model class, this method will get call before registering for element events and view-model changes
     */
    protected void beforeRegisteringListeners() {
    }

    /**
     * If you use initActivity() and pass a view-model class, this method will get call after binding elements
     */
    protected void afterBindingElements() {
    }

    /**
     * If you use initActivity() and pass a view-model class, this method will get call after loading data into elements
     */
    protected void afterLoadingDataIntoViewFromViewModel() {
    }

    /**
     * If you use initActivity() and pass a view-model class, this method will get call after registering for element events and view-model changes
     */
    protected void afterRegisteringListeners() {
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isBindProcessorEnabled()) {
            try {
                beforeLoadingDataIntoViewFromViewModel();
                BindProcessor.loadDataFromViewModelIntoView(this);
                afterLoadingDataIntoViewFromViewModel();

                beforeRegisteringListeners();
                BindProcessor.registerForViewModelChanges(this);
                BindProcessor.registerForViewChanges(this);
                afterRegisteringListeners();
            } catch (IllegalAccessException e) {
                Utils.log(e);
            } catch (InvocationTargetException e) {
                Utils.log(e);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBindProcessorEnabled()) {
            BindProcessor.unregisterObservers(this);
        }
    }

    private boolean isBindProcessorEnabled() {
        return getIntent().getBooleanExtra("bindProcessorEnabled", false);
    }

    private boolean isSessionTriggered() {
        return getIntent().getBooleanExtra("sessionTriggered", false);
    }

    /**
     * This method clears session and BindProcessor memory.<br/>
     * It is better to use this method in onStop() method
     */
    protected void clearSessionThings() {
        boolean bindProcessorEnabled = isBindProcessorEnabled();
        if (bindProcessorEnabled)
            BindProcessor.finish(this, true);

        boolean sessionTriggered = isSessionTriggered();
        if (bindProcessorEnabled || sessionTriggered)
            SessionRepository.removeSession(getSessionName());
    }


    /**
     * This method hires a session from SessionRepository class<br/>
     * If you can grant the required permission, a signal will be sent to target-session:onSuccessSignalName(), otherwise no signal will be sent
     *
     * @param sessionName         target session. pass null to use default session
     * @param onSuccessSignalName you have to listen for this signal
     * @param perms               the list of permissions you need
     */
    public void checkPermissions(String sessionName, String onSuccessSignalName, PermissionPackage... perms) {
        if (sessionName == null)
            sessionName = SessionRepository.getDefaultSession().getName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            grantPermissions(sessionName, onSuccessSignalName, Utils.getUngrantedPermissions(this, perms));
        } else {
            signalPermissionGranted(sessionName, onSuccessSignalName);
        }
    }

    /**
     * This method attempts to grant required permissions without any check for android version and already granted permissions, so it is recommended to use "checkPermissions()"
     *
     * @param sessionName
     * @param onSuccessSignalName
     * @param lst                 list of ungranted permissions not all required permission. to make it simpler call checkPermission() method
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void grantPermissions(String sessionName, String onSuccessSignalName, List<PermissionPackage> lst) {
        if (lst != null && lst.size() > 0) {
            Object[] data = new Object[]{sessionName, onSuccessSignalName, lst};
            SessionRepository.getDefaultSession().put("REQUEST_PERMISSION_DATA", data);
            requestPermission(lst);
        } else {
            signalPermissionGranted(sessionName, onSuccessSignalName);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermission(List<PermissionPackage> lst) {
        requestPermissions(new String[]{lst.get(0).getPermission()}, RequestCode.REQUEST_PERMISSION.codeValue);
    }

    protected void signalPermissionGranted(String sessionName, String signalName) {
        if (signalName != null) {
            SessionRepository.getSession(sessionName).put(signalName, Void.TYPE);
        }
    }

    private void clearRequestPermissionMem() {
        SessionRepository.getDefaultSession().remove("REQUEST_PERMISSION_DATA");
    }

    protected AlertDialog getPermissionExplanationDialog(String exp) {
        return new AlertDialog.Builder(this)
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setMessage(exp)
                .setNeutralButton(R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss())
                .create();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RequestCode.REQUEST_PERMISSION.codeValue) {
            Object[] data = (Object[]) SessionRepository.getDefaultSession().get("REQUEST_PERMISSION_DATA", null);
            List<PermissionPackage> lst = (List<PermissionPackage>) data[2];
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {// permission granted
                lst.remove(0);
                if (lst.size() > 0) {
                    requestPermission(lst);
                } else {
                    clearRequestPermissionMem();
                    signalPermissionGranted((String) data[0], (String) data[1]);
                }
            } else {// permission denied
                PermissionPackage pp = lst.get(0);
                if (pp.getExplanation() != null && shouldShowRequestPermissionRationale(pp.getPermission())) {
                    clearRequestPermissionMem();
                    getPermissionExplanationDialog(pp.getExplanation()).show();
                }
            }
        }
    }

    protected String getSessionName() {
        return getClass().getName();
    }
}