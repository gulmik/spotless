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

public class KotlinGradleExtension extends BaseKotlinExtension {
	private static final String GRADLE_KOTLIN_DSL_FILE_EXTENSION = "*.gradle.kts";

	static final String NAME = "kotlinGradle";

	@Inject
	public KotlinGradleExtension(SpotlessExtension spotless) {
		super(spotless);
	}

	@Override
	protected boolean isScript() {
		return true;
	}

	@Override
	protected void setupTask(SpotlessTask task) {
		if (target == null) {
			target = parseTarget(GRADLE_KOTLIN_DSL_FILE_EXTENSION);
		}
		super.setupTask(task);
	}
}
