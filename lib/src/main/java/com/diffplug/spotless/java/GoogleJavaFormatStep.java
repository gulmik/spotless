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
package com.diffplug.spotless.java;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Objects;

import com.diffplug.spotless.FormatterFunc;
import com.diffplug.spotless.FormatterStep;
import com.diffplug.spotless.JarState;
import com.diffplug.spotless.Jvm;
import com.diffplug.spotless.Provisioner;

/** Wraps up <a href="https://github.com/google/google-java-format">google-java-format</a> as a FormatterStep. */
public class GoogleJavaFormatStep {
	// prevent direct instantiation
	private GoogleJavaFormatStep() {}

	private static final String DEFAULT_STYLE = "GOOGLE";
	private static final boolean DEFAULT_REFLOW_LONG_STRINGS = false;
	private static final boolean DEFAULT_REORDER_IMPORTS = false;
	private static final boolean DEFAULT_FORMAT_JAVADOC = true;
	static final String NAME = "google-java-format";
	public static final String MAVEN_COORDINATE = "com.google.googlejavaformat:google-java-format";

	/** Creates a step which formats everything - code, import order, and unused imports. */
	public static FormatterStep create(Provisioner provisioner) {
		return create(defaultVersion(), provisioner);
	}

	/** Creates a step which formats everything - code, import order, and unused imports. */
	public static FormatterStep create(String version, Provisioner provisioner) {
		return create(version, DEFAULT_STYLE, provisioner);
	}

	/** Creates a step which formats everything - code, import order, and unused imports. */
	public static FormatterStep create(String version, String style, Provisioner provisioner) {
		return create(version, style, provisioner, DEFAULT_REFLOW_LONG_STRINGS);
	}

	/** Creates a step which formats everything - code, import order, and unused imports - and optionally reflows long strings. */
	public static FormatterStep create(String version, String style, Provisioner provisioner, boolean reflowLongStrings) {
		return create(MAVEN_COORDINATE, version, style, provisioner, reflowLongStrings);
	}

	public static FormatterStep create(String groupArtifact, String version, String style, Provisioner provisioner, boolean reflowLongStrings) {
		return create(groupArtifact, version, style, provisioner, reflowLongStrings, DEFAULT_REORDER_IMPORTS);
	}

	public static FormatterStep create(String groupArtifact, String version, String style, Provisioner provisioner, boolean reflowLongStrings, boolean reorderImports) {
		return create(groupArtifact, version, style, provisioner, reflowLongStrings, reorderImports, DEFAULT_FORMAT_JAVADOC);
	}

	/** Creates a step which formats everything - groupArtifact, code, import order, and unused imports - and optionally reflows long strings. */
	public static FormatterStep create(String groupArtifact, String version, String style, Provisioner provisioner, boolean reflowLongStrings, boolean reorderImports, boolean formatJavadoc) {
		Objects.requireNonNull(groupArtifact, "groupArtifact");
		if (groupArtifact.chars().filter(ch -> ch == ':').count() != 1) {
			throw new IllegalArgumentException("groupArtifact must be in the form 'groupId:artifactId'");
		}
		Objects.requireNonNull(version, "version");
		Objects.requireNonNull(style, "style");
		Objects.requireNonNull(provisioner, "provisioner");
		return FormatterStep.createLazy(NAME,
				() -> new State(NAME, groupArtifact, version, style, provisioner, reflowLongStrings, reorderImports, formatJavadoc),
				State::createFormat);
	}

	static final Jvm.Support<String> JVM_SUPPORT = Jvm.<String> support(NAME)
			.addMin(11, "1.8") // we only support google-java-format >= 1.8 due to api changes
			.addMin(16, "1.10.0") // java 16 requires at least 1.10.0 due to jdk api changes in JavaTokenizer
			.addMin(21, "1.17.0") // java 21 requires at least 1.17.0 due to https://github.com/google/google-java-format/issues/898
			.add(11, "1.18.1"); // default version

	public static String defaultGroupArtifact() {
		return MAVEN_COORDINATE;
	}

	/** Get default formatter version */
	public static String defaultVersion() {
		return JVM_SUPPORT.getRecommendedFormatterVersion();
	}

	public static String defaultStyle() {
		return DEFAULT_STYLE;
	}

	public static boolean defaultReflowLongStrings() {
		return DEFAULT_REFLOW_LONG_STRINGS;
	}

	public static boolean defaultReorderImports() {
		return DEFAULT_REORDER_IMPORTS;
	}

	public static boolean defaultFormatJavadoc() {
		return DEFAULT_FORMAT_JAVADOC;
	}

	static final class State implements Serializable {
		private static final long serialVersionUID = 1L;

		/** The jar that contains the formatter. */
		final JarState jarState;
		final String stepName;
		final String version;
		final String style;
		final boolean reflowLongStrings;
		final boolean reorderImports;
		final boolean formatJavadoc;

		State(String stepName, String version, Provisioner provisioner) throws Exception {
			this(stepName, version, DEFAULT_STYLE, provisioner);
		}

		State(String stepName, String version, String style, Provisioner provisioner) throws Exception {
			this(stepName, version, style, provisioner, DEFAULT_REFLOW_LONG_STRINGS);
		}

		State(String stepName, String version, String style, Provisioner provisioner, boolean reflowLongStrings) throws Exception {
			this(stepName, version, style, provisioner, reflowLongStrings, DEFAULT_REORDER_IMPORTS);
		}

		State(String stepName, String version, String style, Provisioner provisioner, boolean reflowLongStrings, boolean reorderImports) throws Exception {
			this(stepName, MAVEN_COORDINATE, version, style, provisioner, reflowLongStrings, reorderImports, DEFAULT_FORMAT_JAVADOC);
		}

		State(String stepName, String groupArtifact, String version, String style, Provisioner provisioner, boolean reflowLongStrings, boolean reorderImports, boolean formatJavadoc) throws Exception {
			JVM_SUPPORT.assertFormatterSupported(version);
			ModuleHelper.doOpenInternalPackagesIfRequired();
			this.jarState = JarState.from(groupArtifact + ":" + version, provisioner);
			this.stepName = stepName;
			this.version = version;
			this.style = style;
			this.reflowLongStrings = reflowLongStrings;
			this.reorderImports = reorderImports;
			this.formatJavadoc = formatJavadoc;
		}

		FormatterFunc createFormat() throws Exception {
			final ClassLoader classLoader = jarState.getClassLoader();
			Class<?> formatterFunc = classLoader.loadClass("com.diffplug.spotless.glue.java.GoogleJavaFormatFormatterFunc");
			Constructor<?> constructor = formatterFunc.getConstructor(String.class, String.class, boolean.class, boolean.class, boolean.class);
			FormatterFunc googleJavaFormatFormatterFunc = (FormatterFunc) constructor.newInstance(version, style, reflowLongStrings, reorderImports, formatJavadoc);

			return JVM_SUPPORT.suggestLaterVersionOnError(version, googleJavaFormatFormatterFunc);
		}

		FormatterFunc createRemoveUnusedImportsOnly() throws Exception {
			ClassLoader classLoader = jarState.getClassLoader();
			Class<?> formatterFunc = classLoader.loadClass("com.diffplug.spotless.glue.java.GoogleJavaFormatRemoveUnusedImporterFormatterFunc");
			Constructor<?> constructor = formatterFunc.getConstructor(String.class); //version
			FormatterFunc googleJavaFormatRemoveUnusedImporterFormatterFunc = (FormatterFunc) constructor.newInstance(version);

			return JVM_SUPPORT.suggestLaterVersionOnError(version, googleJavaFormatRemoveUnusedImporterFormatterFunc);
		}

	}

}
