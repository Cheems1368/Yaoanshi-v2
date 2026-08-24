package com.yaoanshi.patient;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.nio.charset.StandardCharsets;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

@CapacitorPlugin(name = "SecureMedicalCache")
public class SecureMedicalCachePlugin extends Plugin {
    private static final String KEY_ALIAS = "yaoanshi_medical_cache_v1";
    private static final String KEYSTORE = "AndroidKeyStore";
    private static final String FILE_NAME = "medical-cache-v1.enc";

    @PluginMethod
    public void read(PluginCall call) {
        try {
            File path = cacheFile();
            JSObject result = new JSObject();
            if (!path.exists()) {
                result.put("value", "");
                call.resolve(result);
                return;
            }

            String[] parts = new String(readAll(path), StandardCharsets.UTF_8).split("\\.", 2);
            if (parts.length != 2) {
                throw new IllegalStateException("Encrypted cache format is invalid");
            }
            byte[] iv = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] encrypted = Base64.decode(parts[1], Base64.NO_WRAP);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(128, iv));
            result.put("value", new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8));
            call.resolve(result);
        } catch (Exception error) {
            call.reject("SECURE_CACHE_READ_FAILED", error);
        }
    }

    @PluginMethod
    public void write(PluginCall call) {
        String value = call.getString("value");
        if (value == null) {
            call.reject("SECURE_CACHE_VALUE_REQUIRED");
            return;
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            String encoded = Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP)
                    + "." + Base64.encodeToString(encrypted, Base64.NO_WRAP);
            try (FileOutputStream output = new FileOutputStream(cacheFile(), false)) {
                output.write(encoded.getBytes(StandardCharsets.UTF_8));
            }
            call.resolve();
        } catch (Exception error) {
            call.reject("SECURE_CACHE_WRITE_FAILED", error);
        }
    }

    @PluginMethod
    public void clear(PluginCall call) {
        try {
            File file = cacheFile();
            if (file.exists() && !file.delete()) {
                throw new IllegalStateException("Encrypted cache could not be deleted");
            }
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE);
            keyStore.load(null);
            if (keyStore.containsAlias(KEY_ALIAS)) {
                keyStore.deleteEntry(KEY_ALIAS);
            }
            call.resolve();
        } catch (Exception error) {
            call.reject("SECURE_CACHE_CLEAR_FAILED", error);
        }
    }

    private File cacheFile() {
        return new File(getContext().getFilesDir(), FILE_NAME);
    }

    private byte[] readAll(File file) throws Exception {
        byte[] bytes = new byte[(int) file.length()];
        try (FileInputStream input = new FileInputStream(file)) {
            int offset = 0;
            while (offset < bytes.length) {
                int count = input.read(bytes, offset, bytes.length - offset);
                if (count < 0) break;
                offset += count;
            }
            if (offset != bytes.length) throw new IllegalStateException("Encrypted cache read was incomplete");
        }
        return bytes;
    }

    private SecretKey getOrCreateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE);
        keyStore.load(null);
        KeyStore.Entry existing = keyStore.getEntry(KEY_ALIAS, null);
        if (existing instanceof KeyStore.SecretKeyEntry) {
            return ((KeyStore.SecretKeyEntry) existing).getSecretKey();
        }

        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE);
        generator.init(new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build());
        return generator.generateKey();
    }
}
