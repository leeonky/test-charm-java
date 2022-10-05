package com.github.leeonky.dal.extensions;

import java.util.List;

public abstract class SFtpFile {
    protected abstract List<SFtpFile> list(String path, SFtpFile parent);

    public abstract String name();

    public abstract List<SFtpFile> ls();

    public SFtpFile access(Object property) {
        return ls().stream().filter(file -> file.name().equals(property)).findFirst()
                .orElseThrow(IllegalStateException::new);
    }
}
