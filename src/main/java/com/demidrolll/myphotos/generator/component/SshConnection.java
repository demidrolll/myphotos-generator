package com.demidrolll.myphotos.generator.component;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.util.Properties;

public class SshConnection implements AutoCloseable {

    private final Session session;

    public SshConnection() throws JSchException {
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");

        session = new JSch().getSession("myphotos", "127.0.0.1", 5000);
        session.setPassword("1");
        session.setConfig(config);

        connect();
    }

    private void connect() throws JSchException {
        session.connect();
        session.setPortForwardingL(6000, "127.0.0.1", 5432);
    }

    @Override
    public void close() throws Exception {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }
}
