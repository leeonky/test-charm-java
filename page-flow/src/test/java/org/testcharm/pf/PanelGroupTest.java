package org.testcharm.pf;

import org.junit.jupiter.api.Test;
import org.testcharm.util.Sneaky;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PanelGroupTest {

    private final PanelGroup<Panel<?>> panelGroup = new PanelGroup<>();

    @Test
    void should_invoke_navigateTo() {
        Target<?> target = mock(Target.class);

        panelGroup.open(Sneaky.cast(target));

        verify(target).navigateTo();
    }

    @Test
    void should_cache_and_return_opened_panel() {
        Panel<?> opened = mock(Panel.class);
        Panel<?> panel = panelGroup.open(new Target<Panel<?>>() {
            @Override
            public void navigateTo() {

            }

            @Override
            public Panel<?> create() {
                return opened;
            }
        });

        assertThat(panel).isSameAs(opened);
        assertThat(panelGroup.opened()).containsExactly(opened);
    }

    @Test
    void should_return_matched_opened_panel_without_navigating() {
        Panel<?> existedOpened = mock(Panel.class);
        panelGroup.open(new Target<Panel<?>>() {
            @Override
            public void navigateTo() {
            }

            @Override
            public Panel<?> create() {
                return existedOpened;
            }
        });

        assertThat((Panel<?>) panelGroup.open(new Target<Panel<?>>() {
            @Override
            public void navigateTo() {
                fail();
            }

            @Override
            public Panel<?> create() {
                fail();
                return null;
            }

            @Override
            public boolean matches(Panel<?> current) {
                return current == existedOpened;
            }
        })).isSameAs(existedOpened);
    }
}