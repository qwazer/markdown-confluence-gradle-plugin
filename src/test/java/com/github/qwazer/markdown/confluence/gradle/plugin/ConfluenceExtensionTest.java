package com.github.qwazer.markdown.confluence.gradle.plugin;

import org.gradle.api.NamedDomainObjectContainer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(MockitoJUnitRunner.class)
public class ConfluenceExtensionTest {

    @Mock
    private NamedDomainObjectContainer<ConfluenceExtension.Page> configuredPages;

    @Test
    public void testGetPagesOrder() {

        final ConfluenceExtension.Page page1 = Mockito.mock();
        Mockito.when(page1.getName()).thenReturn("Page1");
        Mockito.when(page1.getTitle()).thenCallRealMethod();
        Mockito.when(page1.getParentTitle()).thenReturn("Home");

        final ConfluenceExtension.Page page2 = Mockito.mock();
        Mockito.when(page2.getName()).thenReturn("Page2");
        Mockito.when(page2.getTitle()).thenCallRealMethod();
        Mockito.when(page2.getParentTitle()).thenReturn("Page1");

        final ConfluenceExtension.Page page3 = Mockito.mock();
        Mockito.when(page3.getName()).thenReturn("Page3");
        Mockito.when(page3.getTitle()).thenCallRealMethod();
        Mockito.when(page3.getParentTitle()).thenReturn("Home");

        Mockito.when(configuredPages.toArray()).thenReturn(new Object[]{page1, page2, page3});

        final ConfluenceExtension extension = Mockito.mock();
        Mockito.when(extension.getConfiguredPages()).thenReturn(configuredPages);
        Mockito.when(extension.getPages()).thenCallRealMethod();
        Mockito.when(extension.hasParent(ArgumentMatchers.any(), ArgumentMatchers.anyList())).thenCallRealMethod();

        final List<ConfluenceExtension.Page> pages = extension.getPages();
        assertEquals(3, pages.size());
        assertEquals("Page1", pages.get(0).getTitle());
        assertEquals("Page3", pages.get(1).getTitle());
        assertEquals("Page2", pages.get(2).getTitle());
    }

    @Test
    public void testHasParentPageTitleAndParentTitleAreTheSame() {

        final ConfluenceExtension.Page page = Mockito.mock();
        final ConfluenceExtension extension = Mockito.mock();
        Mockito.when(extension.hasParent(ArgumentMatchers.any(), ArgumentMatchers.anyList()))
            .thenCallRealMethod();

        final List<ConfluenceExtension.Page> pages = new ArrayList<>();
        pages.add(page);
        assertFalse(extension.hasParent(page, pages));
    }

}