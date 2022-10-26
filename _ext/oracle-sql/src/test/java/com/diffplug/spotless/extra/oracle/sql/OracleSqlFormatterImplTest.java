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
package com.diffplug.spotless.extra.oracle.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Properties;

import org.junit.jupiter.api.Test;

import com.diffplug.spotless.ResourceHarness;

/**
 * Oracle db tools wrapper integration tests
 */
class OracleSqlFormatterImplTest extends ResourceHarness {

	private Properties properties;

	@Test
	void sqlDefaultProperties() throws Exception {
		properties = new Properties();
		String formatted = getTestResource("sql/oracle/sql.formatted");
		String unformatted = getTestResource("sql/oracle/sql.test");
		assertEquals(formatted, format(unformatted));
	}

	@Test
	void procedureDefaultProperties() throws Exception {
		properties = new Properties();
		String formatted = getTestResource("sql/oracle/procedure.formatted");
		String unformatted = getTestResource("sql/oracle/procedure.test");
		assertEquals(formatted, format(unformatted));
	}

	@Test
	void procedureEmbeddedProperties() throws Exception {
		properties = new Properties();
		properties.setProperty("xml", "embedded");
		String formatted = getTestResource("sql/oracle/procedure-embedded.formatted");
		String unformatted = getTestResource("sql/oracle/procedure.test");
		assertEquals(formatted, format(unformatted));
	}

	@Test
	void procedureTrivadis() throws Exception {
		properties = new Properties();
		properties.setProperty("xml", createTestFile("sql/oracle/trivadis_advanced_format.xml").toString());
		properties.setProperty("arbori", createTestFile("sql/oracle/trivadis_custom_format.arbori").toString());
		String formatted = getTestResource("sql/oracle/procedure-trivadis.formatted");
		String unformatted = getTestResource("sql/oracle/procedure.test");
		assertEquals(formatted, format(unformatted));
	}

	@Test
	void invalidSyntax() throws Exception {
		properties = new Properties();
		String unformatted = getTestResource("sql/oracle/invalid.test");
		assertThrows(IllegalArgumentException.class, () -> format(unformatted));
	}

	private String format(final String input) throws Exception {
		OracleSqlFormatterImpl formatter = new OracleSqlFormatterImpl(properties);
		return formatter.format(input);
	}
}
