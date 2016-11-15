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
package com.github.qwazer.gradle.plugin;

import com.github.qwazer.markdown.confluence.core.service.ConfluenceService;
import com.github.qwazer.markdown.confluence.core.SpringConfig;
import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.markdown.confluence.core.ssl.SslUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import static com.github.qwazer.markdown.confluence.core.ssl.SslUtil.sslTrustAll;

public class ConfluenceGradleTask extends DefaultTask {

    @TaskAction
    public void confluence() throws NoSuchAlgorithmException, KeyManagementException, IOException {
        final AnnotationConfigApplicationContext annotationConfigApplicationContext =
                new AnnotationConfigApplicationContext(SpringConfig.class);
        final ConfluenceService confluenceService = annotationConfigApplicationContext
                .getBean(ConfluenceService.class);

        final ConfluenceConfig confluenceConfig = getProject().getExtensions().findByType(ConfluenceConfig.class);
        if (confluenceConfig.isSslTrustAll()){
            sslTrustAll();
        }

        confluenceService.processAll(confluenceConfig);
        annotationConfigApplicationContext.close();
    }

}
