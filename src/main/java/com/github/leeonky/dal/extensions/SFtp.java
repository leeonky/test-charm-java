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
    //    TODO refactor
    public ChannelSftp channel;

    public SFtp(String host, String port, String user, String password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
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
    public List<SFtpFile> ls() {
        return list("/", this);
    }

    @Override
    protected List<SFtpFile> list(String path, SFtpFile parent) {
        return Suppressor.get(() -> (Vector<ChannelSftp.LsEntry>) channel.ls(path)).stream()
                .filter(entry -> !entry.getFilename().equals("."))
                .filter(entry -> !entry.getFilename().equals(".."))
                .map(entry1 -> new SubSFtpFile(parent, entry1)).collect(Collectors.toList());
    }

    @Override
    public String name() {
        return "";
    }

    public class SubSFtpFile extends SFtpFile {
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
        protected List<SFtpFile> list(String path, SFtpFile parent) {
            return this.parent.list(this.parent.name() + "/" + path, parent);
        }

        @Override
        public String name() {
            return entry.getFilename();
        }

        @Override
        public List<SFtpFile> ls() {
            return parent.list(parent.name() + "/" + name(), this);
        }
    }
}
