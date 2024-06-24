package com.github.leeonky.dal.extensions.basic.sftp.util;

import com.github.leeonky.util.Suppressor;
import com.jcraft.jsch.ChannelSftp;

import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;

public abstract class SFtpFile {
    public abstract String name();

    protected abstract ChannelSftp channel();

    protected abstract String fullName();

    public InputStream download() {
        return Suppressor.get(() -> channel().get(fullName()));
    }

    public abstract boolean isDir();

    public List<SFtpFile> ls() {
        return Suppressor.get(() -> (Vector<ChannelSftp.LsEntry>) channel().ls(fullName())).stream()
                .filter(entry -> !entry.getFilename().equals("."))
                .filter(entry -> !entry.getFilename().equals(".."))
                .sorted(Comparator.<ChannelSftp.LsEntry, Boolean>comparing(e -> e.getAttrs().isDir())
                        .thenComparing(ChannelSftp.LsEntry::getFilename))
                .map(entry -> new SFtp.SubSFtpFile(this, entry, channel(), remote())).collect(Collectors.toList());
    }

    public Optional<SFtpFile> access(Object property) {
        return ls().stream().filter(file -> file.name().equals(property)).findFirst();
    }

    public abstract String attribute();

    public abstract String remote();

    String remoteInfo() {
        String s = "sftp " + remote() + ":" + fullName();
        return s;
    }
}
