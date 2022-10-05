package com.github.leeonky.dal.extensions;

import com.jcraft.jsch.ChannelSftp;

import java.util.List;
import java.util.stream.Collectors;

public abstract class SFtpFile {
    protected abstract List<ChannelSftp.LsEntry> list(String path);

    public abstract String name();

    public List<SFtpFile> ls() {
        return list(name()).stream().map(entry -> new SFtp.SubSFtpFile(this, entry)).collect(Collectors.toList());
    }

    public SFtpFile access(Object property) {
        return ls().stream().filter(file -> file.name().equals(property)).findFirst()
                .orElseThrow(IllegalStateException::new);
    }
}
