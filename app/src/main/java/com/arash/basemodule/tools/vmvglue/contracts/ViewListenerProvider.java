package com.arash.basemodule.tools.vmvglue.contracts;

import android.view.View;

import com.arash.basemodule.contracts.Consumer;


public interface ViewListenerProvider {
    void registerListener(View view, Consumer<Object> consumer);

    void unregisterListener(View view, Consumer<Object> consumer);
}
