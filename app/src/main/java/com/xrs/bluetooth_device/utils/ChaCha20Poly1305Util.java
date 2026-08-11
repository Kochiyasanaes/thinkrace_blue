package com.xrs.bluetooth_device.utils;

import android.util.Base64;
import android.util.Log;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.generators.SCrypt;
import org.bouncycastle.crypto.modes.ChaCha20Poly1305;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

/**
 * ChaCha20-Poly1305 加密工具类
 * 完全兼容 Node.js crypto 模块的实现（Scrypt + ChaCha20-Poly1305）
 * 适用于 Android 4.4+ 全版本（API 19+）
 */
public class ChaCha20Poly1305Util {

    // 注册 BouncyCastle 提供者
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
    // 在常量定义区域添加
    private static final int SCRYPT_N_DEFAULT = 16384; // 原始高安全参数
    private static final int SCRYPT_N_LIGHT = 2048;      // 移动端优化参数
    // 在常量定义区域添加（约第25行）
    private static final int SCRYPT_N = 2048;      // 从16384降低到2048（约2MB内存）
    private static final int SCRYPT_R = 8;         // 保持8
    private static final int SCRYPT_P = 1;         // 保持1

    // 常量定义
    private static final int KEY_SIZE = 32;
    private static final int NONCE_SIZE = 12;
    private static final int TAG_SIZE = 16;
    private static final int SALT_SIZE = 8;
    private static final int MAC_SIZE_BITS = TAG_SIZE * 8;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // ===================================================================
    // Base64 兼容性工具类（核心修复）
    // ===================================================================
    private static class Base64Compat {
        private static final int API_LEVEL = android.os.Build.VERSION.SDK_INT;
        private static final boolean IS_JAVA8 = API_LEVEL >= 26; // API 26+ 支持 java.util.Base64

        public static String encode(byte[] data) {
            if (IS_JAVA8) {
                try {
                    // 反射调用 java.util.Base64（API 26+）
                    Class<?> base64Class = Class.forName("java.util.Base64");
                    Object encoder = base64Class.getMethod("getEncoder").invoke(null);
                    return (String) encoder.getClass().getMethod("encodeToString", byte[].class)
                            .invoke(encoder, data);
                } catch (Exception e) {
                    // 降级到 android.util.Base64
                    return Base64.encodeToString(data, Base64.DEFAULT);
                }
            } else {
                // API < 26 直接使用 android.util.Base64
                return Base64.encodeToString(data, Base64.DEFAULT);
            }
        }

        public static byte[] decode(String base64String) {
            if (IS_JAVA8) {
                try {
                    // 反射调用 java.util.Base64
                    Class<?> base64Class = Class.forName("java.util.Base64");
                    Object decoder = base64Class.getMethod("getDecoder").invoke(null);
                    return (byte[]) decoder.getClass().getMethod("decode", String.class)
                            .invoke(decoder, base64String);
                } catch (Exception e) {
                    return Base64.decode(base64String, Base64.DEFAULT);
                }
            } else {
                return Base64.decode(base64String, Base64.DEFAULT);
            }
        }
    }

    /**
     * 核心方法：解密 Node.js 发来的数据
     * 格式: [8字节salt][12字节nonce][16字节tag][密文]
     */
    public static String decryptFromNodejs(String base64String, String password) throws Exception {
        // 1. Base64 解码（自动兼容所有API）
        Log.d("Crypto", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Log.d("Crypto", "待解密Base64: [" + base64String + "]");
        Log.d("Crypto", "长度: " + base64String.length());

        if (base64String == null || base64String.trim().isEmpty()) {
            throw new IllegalArgumentException("输入为空或null");
        }

        // 检查是否包含非法字符
        if (!base64String.matches("^[A-Za-z0-9+/=]+$")) {
            Log.e("Crypto", "❌ 包含非Base64字符！");
            Log.e("Crypto", "非法字符: " + base64String.replaceAll("[A-Za-z0-9+/=]", ""));
            throw new IllegalArgumentException("bad base-64: 包含非法字符");
        }
        try {
            byte[] data = Base64Compat.decode(base64String);

            // 2. 验证数据最小长度
            if (data.length < SALT_SIZE + NONCE_SIZE + TAG_SIZE) {
                throw new IllegalArgumentException(
                        "密文格式错误: 数据长度不足。最小需要 " + (SALT_SIZE + NONCE_SIZE + TAG_SIZE) + " 字节"
                );
            }

            // 3. 按 Node.js 格式解析数据
            byte[] salt = Arrays.copyOfRange(data, 0, SALT_SIZE);
            byte[] nonce = Arrays.copyOfRange(data, SALT_SIZE, SALT_SIZE + NONCE_SIZE);
            byte[] authTag = Arrays.copyOfRange(data, SALT_SIZE + NONCE_SIZE, SALT_SIZE + NONCE_SIZE + TAG_SIZE);
            byte[] ciphertext = Arrays.copyOfRange(data, SALT_SIZE + NONCE_SIZE + TAG_SIZE, data.length);

            String keyCode = PropertiesUtil.getSystemProperties("persist.sys.chacha.keycode", "01");
            int n = "02".equals(keyCode) ? SCRYPT_N_LIGHT : SCRYPT_N_DEFAULT;
            Log.d("Crypto", "使用SCrypt参数 N=" + n + " (keyCode=" + keyCode + ")");

            // 4. 使用 Scrypt 派生密钥（必须与 Node.js 参数完全一致）
            // 修改后：
            byte[] key = SCrypt.generate(
                    password.getBytes(StandardCharsets.UTF_8),
                    salt,
                    n,  // 动态N值（必须与加密时一致）
                    SCRYPT_R,
                    SCRYPT_P,
                    KEY_SIZE
            );
            // 5. 解密并验证标签
            byte[] plaintext = decryptWithSeparateTag(ciphertext, key, nonce, authTag, null);

            // 6. 安全擦除派生密钥
            wipeKey(key);

            return new String(plaintext, StandardCharsets.UTF_8);
        }catch (OutOfMemoryError e) {
            ICLogger.i("ChaCha20Poly1305Util"+"SCrypt OOM: 解密时内存不足", e);
            System.gc();
            throw new Exception("设备内存不足，请稍后重试", e);
        }
    }

    /**
     * 标准 AEAD 解密（认证标签与密文一体）
     */
    public static byte[] decrypt(byte[] ciphertext, byte[] key, byte[] nonce, byte[] associatedData)
            throws InvalidCipherTextException {
        validateKeyAndNonce(key, nonce);

        ChaCha20Poly1305 cipher = new ChaCha20Poly1305();
        AEADParameters params = new AEADParameters(
                new KeyParameter(key),
                MAC_SIZE_BITS,
                nonce,
                associatedData != null ? associatedData : new byte[0]
        );

        cipher.init(false, params);

        byte[] output = new byte[cipher.getOutputSize(ciphertext.length)];
        int len = cipher.processBytes(ciphertext, 0, ciphertext.length, output, 0);
        int finalLen = cipher.doFinal(output, len);

        return Arrays.copyOfRange(output, 0, len + finalLen);
    }

    /**
     * 标准 AEAD 加密（认证标签附加在密文后）
     */
    public static byte[] encrypt(byte[] plaintext, byte[] key, byte[] nonce, byte[] associatedData)
            throws InvalidCipherTextException {
        validateKeyAndNonce(key, nonce);

        ChaCha20Poly1305 cipher = new ChaCha20Poly1305();
        AEADParameters params = new AEADParameters(
                new KeyParameter(key),
                MAC_SIZE_BITS,
                nonce,
                associatedData != null ? associatedData : new byte[0]
        );

        cipher.init(true, params);

        byte[] output = new byte[cipher.getOutputSize(plaintext.length)];
        int len = cipher.processBytes(plaintext, 0, plaintext.length, output, 0);
        cipher.doFinal(output, len);

        return output;
    }

    /**
     * 核心方法：生成 Node.js 服务端可解密的格式
     * 格式: [8字节salt][12字节nonce][16字节authTag][密文] → Base64
     */
    public static String encryptForNodejs(String plaintext, String password) throws Exception {
        // 1. 生成随机 salt 和 nonce
        try {
            String keyCode = PropertiesUtil.getSystemProperties("persist.sys.chacha.keycode", "01");
            int n = "02".equals(keyCode) ? SCRYPT_N_LIGHT : SCRYPT_N_DEFAULT;
            Log.d("Crypto", "使用SCrypt参数 N=" + n + " (keyCode=" + keyCode + ")");

            byte[] salt = generateRandomBytes(SALT_SIZE);
            byte[] nonce = generateRandomBytes(NONCE_SIZE);

            byte[] key = SCrypt.generate(
                    password.getBytes(StandardCharsets.UTF_8),
                    salt,
                    n,  // 动态N值
                    SCRYPT_R,
                    SCRYPT_P,
                    KEY_SIZE
            );

            // 3. 加密（返回：密文 + 认证标签）
            byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertextWithTag = encrypt(plaintextBytes, key, nonce, null);

            // 4. 分离密文和认证标签
            int ciphertextLen = ciphertextWithTag.length - TAG_SIZE;
            byte[] ciphertext = Arrays.copyOfRange(ciphertextWithTag, 0, ciphertextLen);
            byte[] authTag = Arrays.copyOfRange(ciphertextWithTag, ciphertextLen, ciphertextWithTag.length);

            // 5. 组合成 Node.js 格式
            byte[] result = new byte[SALT_SIZE + NONCE_SIZE + TAG_SIZE + ciphertext.length];
            System.arraycopy(salt, 0, result, 0, SALT_SIZE);
            System.arraycopy(nonce, 0, result, SALT_SIZE, NONCE_SIZE);
            System.arraycopy(authTag, 0, result, SALT_SIZE + NONCE_SIZE, TAG_SIZE);
            System.arraycopy(ciphertext, 0, result, SALT_SIZE + NONCE_SIZE + TAG_SIZE, ciphertext.length);

            // 6. 安全擦除
            wipeKey(key);

            // 7. Base64 编码（自动兼容所有API）
            return Base64Compat.encode(result);
        }catch (OutOfMemoryError e) {
            ICLogger.i("ChaCha20Poly1305Util"+"SCrypt OOM: 解密时内存不足", e);
            System.gc();
            throw new Exception("设备内存不足，请稍后重试", e);
        }
    }

    /**
     * 工具方法：分离式标签解密（用于 Node.js 格式）
     */
    public static byte[] decryptWithSeparateTag(byte[] ciphertext, byte[] key, byte[] nonce,
                                                byte[] authTag, byte[] associatedData)
            throws InvalidCipherTextException {
        // 拼接成 BC 要求的格式: [密文][认证标签]
        byte[] ciphertextWithTag = new byte[ciphertext.length + TAG_SIZE];
        System.arraycopy(ciphertext, 0, ciphertextWithTag, 0, ciphertext.length);
        System.arraycopy(authTag, 0, ciphertextWithTag, ciphertext.length, TAG_SIZE);

        return decrypt(ciphertextWithTag, key, nonce, associatedData);
    }

    /**
     * 便捷方法：Nonce 前置格式
     */
    public static byte[] encryptWithNoncePrefix(byte[] plaintext, byte[] key, byte[] associatedData)
            throws InvalidCipherTextException {
        byte[] nonce = generateRandomBytes(NONCE_SIZE);
        byte[] ciphertextWithTag = encrypt(plaintext, key, nonce, associatedData);

        byte[] result = new byte[NONCE_SIZE + ciphertextWithTag.length];
        System.arraycopy(nonce, 0, result, 0, NONCE_SIZE);
        System.arraycopy(ciphertextWithTag, 0, result, NONCE_SIZE, ciphertextWithTag.length);

        return result;
    }

    public static byte[] decryptWithNoncePrefix(byte[] data, byte[] key, byte[] associatedData)
            throws InvalidCipherTextException {
        if (data == null || data.length < NONCE_SIZE + TAG_SIZE) {
            throw new IllegalArgumentException("密文数据格式错误：长度不足");
        }

        byte[] nonce = Arrays.copyOfRange(data, 0, NONCE_SIZE);
        byte[] ciphertextWithTag = Arrays.copyOfRange(data, NONCE_SIZE, data.length);

        return decrypt(ciphertextWithTag, key, nonce, associatedData);
    }

    /**
     * 密钥管理工具
     */
    public static byte[] generateKey() {
        return generateRandomBytes(KEY_SIZE);
    }

    public static void wipeKey(byte[] key) {
        if (key != null) {
            Arrays.fill(key, (byte) 0);
        }
    }

    private static byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return bytes;
    }

    private static void validateKeyAndNonce(byte[] key, byte[] nonce) {
        if (key == null || key.length != KEY_SIZE) {
            throw new IllegalArgumentException("密钥必须为32字节（256位）");
        }
        if (nonce == null || nonce.length != NONCE_SIZE) {
            throw new IllegalArgumentException("nonce必须为12字节");
        }
    }
}