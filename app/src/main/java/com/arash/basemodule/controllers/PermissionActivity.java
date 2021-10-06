package com.arash.basemodule.controllers;

import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.arash.basemodule.models.nonentities.PermissionPackage;

import java.util.List;

public class PermissionActivity extends BaseActivity {
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(getView());
        Object[] data = (Object[]) getIntent().getSerializableExtra("data");
        grantPermissions((String) data[0], (String) data[1], (List<PermissionPackage>) data[2]);
    }

    private View getView() {
        RelativeLayout rl = new RelativeLayout(this);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rl.setLayoutParams(lp);
        return rl;
    }

    @Override
    protected AlertDialog getPermissionExplanationDialog(String exp) {
        AlertDialog dialog = super.getPermissionExplanationDialog(exp);
        dialog.setOnDismissListener(dialogInterface -> finish());
        return dialog;
    }

    @Override
    protected void signalPermissionGranted(String sessionName, String signalName) {
        finish();
        super.signalPermissionGranted(sessionName, signalName);
    }
}
