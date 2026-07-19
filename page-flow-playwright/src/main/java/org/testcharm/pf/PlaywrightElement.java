package org.testcharm.pf;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import org.testcharm.dal.runtime.SoloList;
import org.testcharm.io.VirtualFile;
import org.testcharm.util.CollectionHelper;

import java.util.Collection;
import java.util.List;

import static java.lang.String.format;
import static org.testcharm.pf.By.*;

public abstract class PlaywrightElement<T extends PlaywrightElement<T, P>, P extends PlaywrightPageFlow>
        extends AbstractElement<T, Locator, P> implements WebElement<T, Locator, P> {

    protected PlaywrightElement(P pf, Locator locator) {
        super(pf, locator);
    }

    @Override
    public String attributeValue(String name) {
        return raw().getAttribute(name);
    }

    @Override
    public List<Locator> findElements(By by) {
        return raw().locator(locateInfo(by)).all();
    }

    private String locateInfo(By by) {
        switch (by.type()) {
            case CSS:
                return by.value();
            case XPATH:
                return "xpath=" + by.value();
            case CAPTION:
                return String.format("text='%s'", by.value());
            case PLACEHOLDER:
                return format("xpath=.//*[@placeholder='%s']", by.value());
            default:
                throw new UnsupportedOperationException("Unsupported find type: " + by.type());
        }
    }

    @Override
    public String tag() {
        return raw().evaluate("el => el.tagName").toString().toLowerCase();
    }

    @Override
    public String text() {
        return raw().evaluate("el => el.innerText").toString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T click() {
        raw().click();
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T typeIn(String value) {
        raw().pressSequentially(value);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T clear() {
        raw().clear();
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T fillIn(Object value) {
        if (checkAble()) {
            if (raw().isChecked() != (boolean) value)
                click();
        } else if (selectAble()) {
            raw().selectOption(CollectionHelper.asStream(value).map(String::valueOf).toArray(String[]::new));
        } else if (value instanceof VirtualFile) {
            raw().setInputFiles(pageFlow().workingDir().write((VirtualFile) value));
        } else
            raw().fill(String.valueOf(value));
        return (T) this;
    }

    @Override
    public Object value() {
        if (checkAble())
            return raw().isChecked();
        if (selectAble())
            return SoloList.soloList((Collection<?>)
                    raw().evaluate("select => Array.from(select.selectedOptions).map(option => option.text)"));
        return raw().inputValue();
    }

    @Override
    public byte[] screenshot() {
        return raw().screenshot();
    }

    @Override
    public String getDom() {
        return (String) raw().evaluate("e => e.outerHTML");
    }

    @Override
    public T download() {
        Download download = pageFlow().page().waitForDownload(this::click);
        download.saveAs(pageFlow().workingDir().resolve(download.suggestedFilename()));
        return (T) this;
    }

    @Override
    public boolean isVisible() {
        return raw().isVisible();
    }

    @Override
    public boolean isEnabled() {
        return raw().isEnabled();
    }
}
