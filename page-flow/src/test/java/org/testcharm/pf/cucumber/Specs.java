package org.testcharm.pf.cucumber;

import lombok.Getter;
import lombok.Setter;
import org.testcharm.io.VirtualFile;
import org.testcharm.jfactory.Spec;

public class Specs {
    @Getter
    @Setter
    public static class TextFile implements VirtualFile {
        private String name;
        private String content;

        @Override
        public byte[] binary() {
            return content.getBytes();
        }
    }

    public static class File extends Spec<TextFile> {
    }
}
