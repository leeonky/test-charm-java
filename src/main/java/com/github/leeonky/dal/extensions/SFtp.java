package com.github.leeonky.dal.extensions;

import com.github.leeonky.util.Suppressor;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

//TODO close
public class SFtp extends SFtpFile {
    //    TODO refactor
    public final String host, port, user, password;
    private final String path;
    //    TODO refactor
    public ChannelSftp channel;

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
    protected List<ChannelSftp.LsEntry> list(String path) {
        return Suppressor.get(() -> (Vector<ChannelSftp.LsEntry>) channel.ls(path)).stream()
                .filter(entry -> !entry.getFilename().equals("."))
                .filter(entry -> !entry.getFilename().equals(".."))
                .collect(Collectors.toList());
    }

    @Override
    public String name() {
        return path;
    }

    public static class SubSFtpFile extends SFtpFile {
        private final SFtpFile parent;
        private final ChannelSftp.LsEntry entry;

        public SubSFtpFile(SFtpFile parent, ChannelSftp.LsEntry entry) {
            this.parent = parent;
            this.entry = entry;
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
        protected List<ChannelSftp.LsEntry> list(String path) {
            return parent.list(parent.name() + "/" + path);
        }
    }
}
