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
package com.diffplug.spotless.glue.json;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonPatch;

import com.diffplug.spotless.FormatterFunc;

public class JsonPatchFormatterFunc implements FormatterFunc {
	private final ObjectMapper objectMapper;
	private final List<Map<String, Object>> patch;
	private final String patchString;

	public JsonPatchFormatterFunc(String patchString) {
		this.objectMapper = new ObjectMapper();
		this.patch = null;
		this.patchString = patchString;
	}

	public JsonPatchFormatterFunc(List<Map<String, Object>> patch) {
		this.objectMapper = new ObjectMapper();
		this.patch = patch;
		this.patchString = null;
	}

	@Override
	public String apply(String input) throws Exception {
		var patchNode = this.patch == null
				? objectMapper.readTree(patchString)
				: objectMapper.valueToTree(patch);

		var inputNode = objectMapper.readTree(input);

		var patchedNode = JsonPatch.apply(patchNode, inputNode);

		return objectMapper.writeValueAsString(patchedNode);
	}
}
