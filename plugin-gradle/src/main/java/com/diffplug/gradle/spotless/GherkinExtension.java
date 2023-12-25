/*
 * Copyright 2016-2023 DiffPlug
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diffplug.gradle.spotless;

import javax.inject.Inject;

import com.diffplug.spotless.FormatterStep;
import com.diffplug.spotless.gherkin.GherkinUtilsStep;

public class GherkinExtension extends FormatExtension {
	static final String NAME = "gherkin";

	@Inject
	public GherkinExtension(SpotlessExtension spotless) {
		super(spotless);
	}

	@Override
	protected void setupTask(SpotlessTask task) {
		if (target == null) {
			throw noDefaultTargetException();
		}
		super.setupTask(task);
	}

	public GherkinUtilsConfig gherkinUtils() {
		return new GherkinUtilsConfig();
	}

	public class GherkinUtilsConfig {
		private String version;
		private int indent;

		public GherkinUtilsConfig() {
			this.version = GherkinUtilsStep.defaultVersion();
			this.indent = com.diffplug.spotless.gherkin.GherkinUtilsConfig.defaultIndentSpaces();
			addStep(createStep());
		}

		public void version(String version) {
			this.version = version;
			replaceStep(createStep());
		}

		private FormatterStep createStep() {
			return GherkinUtilsStep.create(new com.diffplug.spotless.gherkin.GherkinUtilsConfig(indent), version, provisioner());
		}
	}

}
