package org.testcharm.cucumber.restful.spec;

import org.testcharm.io.VirtualFile;
import org.testcharm.jfactory.Spec;

import java.nio.charset.StandardCharsets;

public class Files {

    public static class ATextFile extends Spec<TextFile> {
    }

    public static class TextFile implements VirtualFile {
        private String name;
        private String content;

        @Override
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        @Override
        public byte[] binary() {
            return content.getBytes(StandardCharsets.UTF_8);
        }
    }
}
