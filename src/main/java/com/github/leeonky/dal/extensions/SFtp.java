package com.github.leeonky.dal.extensions;

import com.github.leeonky.util.Suppressor;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.InputStream;

//TODO close
public class SFtp extends SFtpFile {
    //    TODO refactor
    public final String host, port, user, password;
    private final String path;
    private final ChannelSftp channel;

    public SFtp(String host, String port, String user, String password, String path) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.path = path;
        channel = Suppressor.get(this::getChannelSftp);
    }

    private ChannelSftp getChannelSftp() throws JSchException {
        JSch jsch = new JSch();
        Session jschSession = jsch.getSession(user, host, Integer.parseInt(port));
        jschSession.setConfig("StrictHostKeyChecking", "no");
        jschSession.setPassword(password);
        jschSession.connect();
        ChannelSftp channel = (ChannelSftp) jschSession.openChannel("sftp");
        channel.connect();
        return channel;
    }

    @Override
    public String name() {
        return path;
    }

    @Override
    protected ChannelSftp channel() {
        return channel;
    }

    @Override
    protected String fullName() {
        return name();
    }

    public static class SubSFtpFile extends SFtpFile {
        private final SFtpFile parent;
        private final ChannelSftp.LsEntry entry;
        private final ChannelSftp channel;

        public SubSFtpFile(SFtpFile parent, ChannelSftp.LsEntry entry, ChannelSftp channel) {
            this.parent = parent;
            this.entry = entry;
            this.channel = channel;
        }

        @Override
        public String toString() {
            return name();
        }

        @Override
        public String name() {
            return entry.getFilename();
        }

        @Override
        protected ChannelSftp channel() {
            return channel;
        }

        @Override
        protected String fullName() {
            return parent.fullName() + "/" + name();
        }

        public InputStream download() {
            return Suppressor.get(() -> channel.get(fullName()));
        }

        @Override
        public boolean isDir() {
            return entry.getAttrs().isDir();
        }
    }
}
