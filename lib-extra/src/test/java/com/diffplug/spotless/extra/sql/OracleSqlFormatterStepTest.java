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
package com.diffplug.spotless.extra.sql;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.diffplug.spotless.FormatterStep;
import com.diffplug.spotless.LineEnding;
import com.diffplug.spotless.ResourceHarness;
import com.diffplug.spotless.SerializableEqualityTester;
import com.diffplug.spotless.TestProvisioner;

class OracleSqlFormatterStepTest extends ResourceHarness {

	private static OracleSqlFormatterStep.Config newConfig() {
		return OracleSqlFormatterStep.newConfig(TestProvisioner.mavenLocal());
	}

	@Test
	void defaultSettings() throws Exception {
		FormatterStep step = newConfig().createStep();
		String output = format(step, "select sysdate from dual;");
		assertThat(output).isEqualTo("SELECT\n    sysdate\nFROM\n    dual;");
	}

	@Test
	void trivadisSettings() throws Exception {
		OracleSqlFormatterStep.Config config = newConfig();
		config.settingsFile(createTestFile("sql/oracle/trivadis_advanced_format.xml"));
		config.arboriFile(createTestFile("sql/oracle/trivadis_custom_format.arbori"));
		FormatterStep step = config.createStep();
		String output = format(step, "SELECT\n    sysdate\nFROM\n    dual;");
		assertThat(output).isEqualTo("select sysdate\n  from dual;");
	}

	@Test
	void equality() throws Exception {
		new SerializableEqualityTester() {
			@Override
			protected void setupTest(SerializableEqualityTester.API api) {
				api.areDifferentThan();
			}

			@Override
			protected FormatterStep create() {
				return newConfig().createStep();
			}
		}.testEquals();
	}

	private String format(FormatterStep step, String input) throws Exception {
		File inputFile = setFile("testSource").toContent(input);
		return LineEnding.toUnix(step.format(input, inputFile));
	}
}
