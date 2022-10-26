package com.github.leeonky.dal.extensions.basic.sftp.util;

import com.github.leeonky.dal.extensions.basic.file.util.Util;
import com.github.leeonky.util.Suppressor;
import com.jcraft.jsch.*;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import static com.github.leeonky.util.function.Extension.not;

public class SFtp extends SFtpFile {
    private final String host, port, user, password;
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
        return Paths.get(path).getFileName().toString();
    }

    @Override
    protected ChannelSftp channel() {
        return channel;
    }

    @Override
    protected String fullName() {
        return path;
    }

    @Override
    public boolean isDir() {
        return Suppressor.get(() -> channel.lstat(path)).isDir();
    }

    @Override
    public String attribute() {
        String name = Paths.get(path).getFileName().toString();
        return attribute(Suppressor.get(() ->
                        ((Vector<ChannelSftp.LsEntry>) channel.ls(Paths.get(path).getParent().toString()))).stream()
                .filter(e -> e.getFilename().equals(name)).findFirst().orElseThrow(IllegalStateException::new));
    }

    private static String attribute(ChannelSftp.LsEntry entry) {
        SftpATTRS attrs = entry.getAttrs();
        List<String> items = Arrays.stream(entry.getLongname().split(" ")).filter(not(String::isEmpty))
                .collect(Collectors.toList());
        return String.format("%s %s %s %6s %s", attrs.getPermissionsString(), items.get(2), items.get(3),
                Util.formatFileSize(attrs.getSize()), Instant.ofEpochMilli(attrs.getMTime() * 1000L));
    }

    public void close() {
        channel.exit();
        Suppressor.run(() -> channel.getSession().disconnect());
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

        @Override
        public boolean isDir() {
            return entry.getAttrs().isDir();
        }

        @Override
        public String attribute() {
            return SFtp.attribute(entry);
        }
    }
}
