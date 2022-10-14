package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.JavaClassPropertyAccessor;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.dal.runtime.inspector.Inspector;
import com.github.leeonky.dal.runtime.inspector.InspectorCache;
import com.github.leeonky.dal.util.TextUtil;
import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.InvocationException;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;

public class SFTPExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        RuntimeContextBuilder builder = dal.getRuntimeContextBuilder();
        builder.registerImplicitData(SFtpFile.class, SFtpFile::download)
                .registerListAccessor(SFtpFile.class, SFtpFile::ls)
                .registerPropertyAccessor(SFtpFile.class, new SFtpFileJavaClassPropertyAccessor())
                .registerInspector(SFtpFile.class, this::sftpInspector);
    }

    private Inspector sftpInspector(Data data) {
        SFtpFile sFtpFile = (SFtpFile) data.getInstance();
        return sFtpFile.isDir() ? new DirInspector(sFtpFile, data) : (path, cache) -> sFtpFile.attribute() + " " + sFtpFile.name();
    }

    private Object getSubFile(SFtpFile sFtpFile, Object property) {
        Optional<SFtpFile> first = sFtpFile.access(property);
        if (first.isPresent())
            return first.get();
        if (sFtpFile.ls().stream().anyMatch(f -> f.name().startsWith(property + ".")))
            return new SftpFileGroup(sFtpFile, property.toString());
        throw new InvocationException(new FileNotFoundException(String.format("File or File Group <%s> not found", property)));
    }

    private abstract static class SFtpInspector implements Inspector {
        protected final SFtpFile sFtpFile;
        protected final Data data;

        public SFtpInspector(SFtpFile sFtpFile, Data data) {
            this.sFtpFile = sFtpFile;
            this.data = data;
        }
    }

    private static class DirInspector extends SFtpInspector {
        public DirInspector(SFtpFile sFtpFile, Data data) {
            super(sFtpFile, data);
        }

        @Override
        public String inspect(String path, InspectorCache inspectorCache) {
            return String.join("\n", new ArrayList<String>() {{
                add("sftp dir " + sFtpFile.fullName());
                data.getDataList().stream().map(Data::dump).forEach(this::add);
            }});
        }

        @Override
        public String dump(String path, InspectorCache caches) {
            String name = sFtpFile.name() + "/";
            List<Data> dataList = data.getDataList();
            if (dataList.isEmpty())
                return name;
            return name + "\n" + dataList.stream().map(Data::dump).map(TextUtil::indent).collect(Collectors.joining("\n"));
        }
    }

    private class SFtpFileJavaClassPropertyAccessor extends JavaClassPropertyAccessor<SFtpFile> {
        public SFtpFileJavaClassPropertyAccessor() {
            super(BeanClass.create(SFtpFile.class));
        }

        @Override
        public Object getValue(SFtpFile sFtpFile, Object property) {
            return sFtpFile.isDir() ? getSubFile(sFtpFile, property) : super.getValue(sFtpFile, property);
        }

        @Override
        public Set<Object> getPropertyNames(SFtpFile sFtpFile) {
            return sFtpFile.isDir() ? sFtpFile.ls().stream().map(SFtpFile::name)
                    .collect(toCollection(LinkedHashSet::new)) : super.getPropertyNames(sFtpFile);
        }
    }
}
