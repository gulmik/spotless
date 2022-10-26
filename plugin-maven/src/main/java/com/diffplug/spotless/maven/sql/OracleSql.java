/*
 * Copyright 2022 DiffPlug
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
package com.diffplug.spotless.maven.sql;

import java.io.File;

import org.apache.maven.plugins.annotations.Parameter;

import com.diffplug.spotless.FormatterStep;
import com.diffplug.spotless.extra.sql.OracleSqlFormatterStep;
import com.diffplug.spotless.maven.FormatterStepConfig;
import com.diffplug.spotless.maven.FormatterStepFactory;

public class OracleSql implements FormatterStepFactory {

	@Parameter
	private String settingsFile;

	@Parameter
	private String arboriFile;

	@Parameter
	private String groupArtifactVersion;

	@Override
	public FormatterStep newFormatterStep(FormatterStepConfig stepConfig) {
		OracleSqlFormatterStep.Config config = OracleSqlFormatterStep.newConfig(stepConfig.getProvisioner());
		if (null != settingsFile) {
			File file = stepConfig.getFileLocator().locateFile(settingsFile);
			config.settingsFile(file);
		}
		if (null != arboriFile) {
			File file = stepConfig.getFileLocator().locateFile(arboriFile);
			config.arboriFile(file);
		}
		if (null != groupArtifactVersion) {
			config.coordinate(groupArtifactVersion);
		}
		return config.createStep();
	}
}
