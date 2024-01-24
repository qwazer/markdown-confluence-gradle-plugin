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

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class ConfluenceGradlePlugin implements Plugin<Project> {

    @Override
    public void apply(final Project project) {
        ConfluenceExtension extension = project.getExtensions().create("confluence", ConfluenceExtension.class);
        extension.getAuthenticationType().convention(AuthenticationType.BASIC);
        extension.getAuthenticationTypeString().convention("");
        extension.getParserType().convention("commonmark");

        final Map<String, Object> options = new HashMap<>();
        options.put("type", ConfluenceGradleTask.class);

        project.task(options, "confluence");
    }

}