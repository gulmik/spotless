/*
 * Copyright 2023 DiffPlug
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
package com.diffplug.spotless.npm;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.Serializable;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

class NpmFormatterStepLocations implements Serializable {

	private static final long serialVersionUID = -1055408537924029969L;
	@SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
	private final transient File projectDir;

	@SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
	private final transient File buildDir;

	@SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
	private final transient File cacheDir;

	@SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
	private final transient Supplier<File> npmExecutable;

	@SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
	private final transient Supplier<File> nodeExecutable;

	public NpmFormatterStepLocations(@Nonnull File projectDir, @Nonnull File buildDir, File cacheDir, @Nonnull Supplier<File> npmExecutable, @Nonnull Supplier<File> nodeExecutable) {
		this.projectDir = requireNonNull(projectDir);
		this.buildDir = requireNonNull(buildDir);
		this.cacheDir = cacheDir;
		this.npmExecutable = requireNonNull(npmExecutable);
		this.nodeExecutable = requireNonNull(nodeExecutable);
	}

	public File projectDir() {
		return projectDir;
	}

	public File buildDir() {
		return buildDir;
	}

	public File cacheDir() {
		return cacheDir;
	}

	public File npmExecutable() {
		return npmExecutable.get();
	}

	public File nodeExecutable() {
		return nodeExecutable.get();
	}
}
