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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import com.diffplug.common.base.Errors;
import com.diffplug.spotless.FileSignature;
import com.diffplug.spotless.FormatterFunc;
import com.diffplug.spotless.FormatterStep;
import com.diffplug.spotless.JarState;
import com.diffplug.spotless.Provisioner;
import com.diffplug.spotless.ThrowingEx;

public final class OracleSqlFormatterStep {

	private static final String FORMATTER_CLASS = "com.diffplug.spotless.extra.oracle.sql.OracleSqlFormatterImpl";
	private static final String FORMATTER_METHOD = "format";
	private static final String FORMATTER_COORDINATE = "com.diffplug.spotless:spotless-oracle-sql:0.1.0-SNAPSHOT";
	private static final String DBTOOLS_COORDINATE = "oracle.dbtools:dbtools-common:22.3.0.270.1254";

	private OracleSqlFormatterStep() {}

	public static String name() {
		return "oracle sql formatter";
	}

	public static Config newConfig(Provisioner provisioner) {
		return new Config(provisioner, OracleSqlFormatterStep::apply);
	}

	private static FormatterFunc apply(State state) throws Exception {
		Class<?> formatterCls = state.loadClass(FORMATTER_CLASS);
		Object formatter = formatterCls.getConstructor(Properties.class).newInstance(state.getPreferences());
		Method method = formatterCls.getMethod(FORMATTER_METHOD, String.class);
		return input -> (String) method.invoke(formatter, input);
	}

	public static class Config {
		private final Provisioner provisioner;
		private final ThrowingEx.Function<State, FormatterFunc> stateToFormatter;
		private Collection<String> dependencies;
		private File formatterXml;
		private File formatterArbori;

		Config(Provisioner provisioner, ThrowingEx.Function<State, FormatterFunc> stateToFormatter) {
			this.provisioner = Objects.requireNonNull(provisioner, "provisioner");
			this.stateToFormatter = Objects.requireNonNull(stateToFormatter, "stateToFormatter");
			this.dependencies = Collections.unmodifiableCollection(Arrays.asList(FORMATTER_COORDINATE, DBTOOLS_COORDINATE));
		}

		public void coordinate(String value) {
			this.dependencies = Collections.unmodifiableCollection(Arrays.asList(FORMATTER_COORDINATE, value));
		}

		public void settingsFile(File value) {
			this.formatterXml = value;
		}

		public void arboriFile(File value) {
			this.formatterArbori = value;
		}

		public FormatterStep createStep() {
			return FormatterStep.createLazy(name(), this::get, stateToFormatter);
		}

		State get() throws IOException {
			return new State(formatterXml, formatterArbori, provisioner, dependencies);
		}
	}

	private static class State implements Serializable {
		private static final long serialVersionUID = 1L;
		private final FileSignature formatterXmlFile;
		private final FileSignature formatterArboriFile;
		private final JarState jarState;

		State(File formatterXml, File formatterArbori, Provisioner provisioner, Collection<String> dependencies) throws IOException {
			this.formatterXmlFile = toFileSignature(formatterXml);
			this.formatterArboriFile = toFileSignature(formatterArbori);
			this.jarState = JarState.withoutTransitives(dependencies, provisioner);
		}

		private FileSignature toFileSignature(File file) throws IOException {
			return FileSignature.signAsList(Optional.ofNullable(file).map(Collections::singletonList).orElseGet(Collections::emptyList));
		}

		Properties getPreferences() {
			Properties properties = new Properties();
			if (!formatterXmlFile.files().isEmpty()) {
				properties.setProperty("xml", formatterXmlFile.getOnlyFile().getAbsolutePath());
			}
			if (!formatterArboriFile.files().isEmpty()) {
				properties.setProperty("arbori", formatterArboriFile.getOnlyFile().getAbsolutePath());
			}
			return properties;
		}

		Class<?> loadClass(String name) {
			try {
				return jarState.getClassLoader(this).loadClass(name);
			} catch (ClassNotFoundException e) {
				throw Errors.asRuntime(e);
			}
		}
	}
}
