package com.github.leeonky.dal.extensions;

import com.github.leeonky.util.Suppressor;
import com.jcraft.jsch.ChannelSftp;

import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;

public abstract class SFtpFile {
    public abstract String name();

    protected abstract ChannelSftp channel();

    protected abstract String fullName();

    public boolean isDir() {
        return true;
    }

    public List<SFtpFile> ls() {
        return Suppressor.get(() -> (Vector<ChannelSftp.LsEntry>) channel().ls(fullName())).stream()
                .filter(entry -> !entry.getFilename().equals("."))
                .filter(entry -> !entry.getFilename().equals(".."))
                .map(entry -> new SFtp.SubSFtpFile(this, entry, channel())).collect(Collectors.toList());
    }

    public Optional<SFtpFile> access(Object property) {
        return ls().stream().filter(file -> file.name().equals(property)).findFirst();
    }
}
