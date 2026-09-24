package com.yaoanshi.patient;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        registerPlugin(SecureMedicalCachePlugin.class);
        super.onCreate(savedInstanceState);
    }
}
