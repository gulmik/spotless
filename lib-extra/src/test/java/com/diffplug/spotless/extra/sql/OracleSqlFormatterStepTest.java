/*
 * Copyright 2022-2024 DiffPlug
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
		String formattedSql = getTestResource("sql/oracle/sql.formatted");
		String unformattedSql = getTestResource("sql/oracle/sql.test");
		assertEquals(formattedSql, format(step, unformattedSql));

		String formattedProc = getTestResource("sql/oracle/procedure.formatted");
		String unformattedProc = getTestResource("sql/oracle/procedure.test");
		assertEquals(formattedProc, format(step, unformattedProc));
	}

	@Test
	void trivadisSettings() throws Exception {
		OracleSqlFormatterStep.Config config = newConfig();
		config.settingsFile(createTestFile("sql/oracle/trivadis_advanced_format.xml"));
		config.arboriFile(createTestFile("sql/oracle/trivadis_custom_format.arbori"));
		FormatterStep step = config.createStep();
		String formatted = getTestResource("sql/oracle/procedure-trivadis.formatted");
		String unformatted = getTestResource("sql/oracle/procedure.test");
		assertEquals(formatted, format(step, unformatted));
	}

	@Test
	void embeddedSettings() throws Exception {
		OracleSqlFormatterStep.Config config = newConfig();
		config.useEmbeddedConfig(true);
		FormatterStep step = config.createStep();
		String formatted = getTestResource("sql/oracle/procedure-embedded.formatted");
		String unformatted = getTestResource("sql/oracle/procedure.test");
		assertEquals(formatted, format(step, unformatted));
	}

	@Test
	void invalidSyntax() throws Exception {
		FormatterStep step = newConfig().createStep();
		String unformatted = getTestResource("sql/oracle/invalid.test");
		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> format(step, unformatted));
		assertTrue(thrown.getMessage().startsWith("Invalid sql syntax for formatting"));
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
