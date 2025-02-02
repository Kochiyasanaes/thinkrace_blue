package com.xrs.bluetooth_device.model;

/**
 * @ClassName ResponseData
 * @Author kotlin
 * @Email 949390151@qq.com
 * @Date 2024/12/14 17:18
 * ^_^^_^^_^^_^^_^^_^^_^
 */
public class ResponseData {
    private Data data;
    private int code;
    private String message;

    // Getter and Setter methods

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class Data {
        private String imei;
        private String host;
        private int port;
        private String apn;
        private int mcc;
        private int mnc;
        private String smtpServer;
        private String smtpUser;
        private String smtpPass;
        private int smtpPort;
        private int strapTolerance;

        public int getStrapTolerance() {
            return strapTolerance;
        }

        public void setStrapTolerance(int strapTolerance) {
            this.strapTolerance = strapTolerance;
        }


        private boolean smtpSSL;
        private String email;

        private String otaUrl;

        // Getter and Setter methods

        public String getImei() {
            return imei;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getApn() {
            return apn;
        }

        public void setApn(String apn) {
            this.apn = apn;
        }

        public int getMcc() {
            return mcc;
        }

        public void setMcc(int mcc) {
            this.mcc = mcc;
        }

        public void setOtaUrl(String otaUrl) {
            this.otaUrl = otaUrl;
        }

        public String getOtaUrl() {
            return otaUrl;
        }

        public int getMnc() {
            return mnc;
        }

        public void setMnc(int mnc) {
            this.mnc = mnc;
        }

        public String getSmtpServer() {
            return smtpServer;
        }

        public void setSmtpServer(String smtpServer) {
            this.smtpServer = smtpServer;
        }

        public String getSmtpUser() {
            return smtpUser;
        }

        public void setSmtpUser(String smtpUser) {
            this.smtpUser = smtpUser;
        }

        public String getSmtpPass() {
            return smtpPass;
        }

        public void setSmtpPass(String smtpPass) {
            this.smtpPass = smtpPass;
        }

        public int getSmtpPort() {
            return smtpPort;
        }

        public void setSmtpPort(int smtpPort) {
            this.smtpPort = smtpPort;
        }

        public boolean getSmtpSSL() {
            return smtpSSL;
        }

        public void setSmtpSSL(boolean smtpSSL) {
            this.smtpSSL = smtpSSL;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}