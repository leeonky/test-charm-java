package org.testcharm.cucumber.message.gherkin;

import io.cucumber.core.gherkin.DocStringArgument;

public class DocStringArgumentDelegate implements DocStringArgument {
    private String content;
    private String contentType;
    private String mediaType;
    private int line;

    @Override
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }
}
