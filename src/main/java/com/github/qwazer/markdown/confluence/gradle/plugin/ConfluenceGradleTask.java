/*
 * Copyright 2016 Aaron Knight
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.qwazer.markdown.confluence.gradle.plugin;

import com.github.qwazer.markdown.confluence.core.SpringConfig;
import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.markdown.confluence.core.service.MainService;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.Assert;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import static com.github.qwazer.markdown.confluence.core.ssl.SslUtil.sslTrustAll;

public class ConfluenceGradleTask extends DefaultTask {

    @TaskAction
    public void confluence() throws NoSuchAlgorithmException, KeyManagementException, IOException {
        final AnnotationConfigApplicationContext annotationConfigApplicationContext =
                new AnnotationConfigApplicationContext(SpringConfig.class);
        final MainService mainService = annotationConfigApplicationContext
                .getBean(MainService.class);

        final ConfluenceConfig confluenceConfig = getProject().getExtensions().findByType(ConfluenceConfig.class);

        validate(confluenceConfig);

        if (confluenceConfig.isSslTrustAll()){
            sslTrustAll();
        }

        mainService.processAll(confluenceConfig);
        annotationConfigApplicationContext.close();
    }


    protected static void validate(ConfluenceConfig config){
        Assert.notNull(config);
        Assert.hasLength(config.getRestApiUrl());
        Assert.hasLength(config.getSpaceKey());
        Assert.notNull(config.getPages());

        for (ConfluenceConfig.Page page :config.getPages()){
            Assert.hasLength(page.getParentTitle());
            Assert.hasLength(page.getTitle());
            Assert.notNull(page.getSrcFile());
            Assert.isTrue(!page.getParentTitle().equals(page.getTitle()), String.format("Page with title %s cannot be parent of itself ", page.getTitle()));
        }
        validateNoDuplicates(config.getPages());
    }

    protected static void validateNoDuplicates(Collection<ConfluenceConfig.Page> pages) {

        Set<ConfluenceConfig.Page> set = new TreeSet<>(new Comparator<ConfluenceConfig.Page>() {
            @Override
            public int compare(ConfluenceConfig.Page o1, ConfluenceConfig.Page o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });

        set.addAll(pages);

        if (set.size() < pages.size()) {
            throw new IllegalArgumentException("Found duplicate pageTitle in confluence pages");
        }
    }

}
