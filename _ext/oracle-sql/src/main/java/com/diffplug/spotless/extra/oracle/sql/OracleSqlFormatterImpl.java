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

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import oracle.dbtools.app.Format;
import oracle.dbtools.app.Persist2XML;
import oracle.dbtools.parser.Lexer;
import oracle.dbtools.parser.LexerToken;
import oracle.dbtools.parser.Parsed;
import oracle.dbtools.parser.plsql.SqlEarley;

/** Formatter step which calls out to the Oracle db tools formatter. */
public class OracleSqlFormatterImpl {

	private final Format formatter;

	public OracleSqlFormatterImpl(Properties properties) {
		this.formatter = FormatFactory.createFormat(properties);
	}

	/** Formatting sql string */
	public String format(final String input) throws Exception {
		assertSyntax(input);
		return formatter.format(input);
	}

	private void assertSyntax(final String input) {
		final String content = new StringBuilder(input.length() + 1).append("\n").append(input).toString();
		final List<LexerToken> tokens = Lexer.parse(content);
		final Parsed parsed = new Parsed(content, tokens, SqlEarley.getInstance(), new String[]{"sql_statements"});
		final String message = Optional.ofNullable(parsed.getSyntaxError())
				.filter(error -> error.getMessage() != null)
				.map(error -> new StringBuilder("Invalid sql syntax for formatting:\n").append(error).toString())
				.orElse(null);
		if (message != null) {
			throw new IllegalArgumentException(message);
		}
	}

	private static class FormatFactory {

		static Format createFormat(Properties properties) {
			final Format formatter = new Format();
			configure(formatter, properties);
			return formatter;
		}

		private static void configure(Format formatter, Properties properties) {
			final String xmlConfig = properties.getProperty("xml", "default");
			if ("embedded".equals(xmlConfig)) {
				configureEmbeddedFormat(formatter);
			} else if (!"default".equals(xmlConfig)) {
				configureFormatFromFile(formatter, xmlConfig);
			}

			final String arboriConfig = properties.getProperty("arbori", "default");
			if (!"default".equals(arboriConfig)) {
				configureArboriFromFile(formatter, arboriConfig);
			}
		}

		private static void configureEmbeddedFormat(Format formatter) {
			// Code Editor: Format
			formatter.options.put(formatter.adjustCaseOnly, false);                                         // default: false (set true to skip formatting)
			// Advanced Format: General
			formatter.options.put(formatter.kwCase, Format.Case.lower);                                     // default: javaFormat.Case.UPPER
			formatter.options.put(formatter.idCase, Format.Case.NoCaseChange);                              // default: javaFormat.Case.lower
			formatter.options.put(formatter.singleLineComments, Format.InlineComments.CommentsUnchanged);   // default: javaFormat.InlineComments.CommentsUnchanged
			// Advanced Format: Alignment
			formatter.options.put(formatter.alignTabColAliases, false);                                     // default: true
			formatter.options.put(formatter.alignTypeDecl, true);                                           // default: true
			formatter.options.put(formatter.alignNamedArgs, true);                                          // default: true
			formatter.options.put(formatter.alignAssignments, true);                                        // default: false
			formatter.options.put(formatter.alignEquality, false);                                          // default: false
			formatter.options.put(formatter.alignRight, true);                                              // default: false
			// Advanced Format: Indentation
			formatter.options.put(formatter.identSpaces, 3);                                                // default: 3
			formatter.options.put(formatter.useTab, false);                                                 // default: false
			// Advanced Format: Line Breaks
			formatter.options.put(formatter.breaksComma, Format.Breaks.After);                              // default: javaFormat.Breaks.After
			formatter.options.put("commasPerLine", 1);                                                      // default: 5
			formatter.options.put(formatter.breaksConcat, Format.Breaks.Before);                            // default: javaFormat.Breaks.Before
			formatter.options.put(formatter.breaksAroundLogicalConjunctions, Format.Breaks.Before);         // default: javaFormat.Breaks.Before
			formatter.options.put(formatter.breakAnsiiJoin, true);                                          // default: false
			formatter.options.put(formatter.breakParenCondition, true);                                     // default: false
			formatter.options.put(formatter.breakOnSubqueries, true);                                       // default: true
			formatter.options.put(formatter.maxCharLineSize, 120);                                          // default: 128
			formatter.options.put(formatter.forceLinebreaksBeforeComment, false);                           // default: false
			formatter.options.put(formatter.extraLinesAfterSignificantStatements, Format.BreaksX2.Keep);    // default: javaFormat.BreaksX2.X2
			formatter.options.put(formatter.breaksAfterSelect, false);                                      // default: true
			formatter.options.put(formatter.flowControl, Format.FlowControl.IndentedActions);               // default: javaFormat.FlowControl.IndentedActions
			// Advanced Format: White Space
			formatter.options.put(formatter.spaceAroundOperators, true);                                    // default: true
			formatter.options.put(formatter.spaceAfterCommas, true);                                        // default: true
			formatter.options.put(formatter.spaceAroundBrackets, Format.Space.Default);                     // default: javaFormat.Space.Default
			// Advanced Format: Hidden, not configurable in the GUI preferences dialog of SQLDev 20.4.1
			formatter.options.put(formatter.breaksProcArgs, false);                                         // default: false (overridden in Arbori program based on other settings)
			formatter.options.put(formatter.formatThreshold, 1);                                            // default: 1 (disables deprecated post-processing logic)
		}

		private static void configureFormatFromFile(Format formatter, String xmlConfig) {
			try {
				URL url = Paths.get(xmlConfig).toUri().toURL();
				Persist2XML.read(url).forEach(formatter.options::put);
			} catch (Exception ex) {
				throw new IllegalArgumentException("Failed to read configuration file: " + xmlConfig, ex);
			}
		}

		private static void configureArboriFromFile(Format formatter, String arboriConfig) {
			Path arboriConfigPath = Paths.get(arboriConfig);
			if (!Files.isReadable(arboriConfigPath)) {
				throw new IllegalArgumentException("Failed to read arbori file: " + arboriConfig);
			}
			String arboriHome = Optional.ofNullable(arboriConfigPath.getParent()).map(Path::toAbsolutePath).map(Path::toString).orElseThrow(() -> new IllegalArgumentException("Failed to access arbori path."));
			System.setProperty("dbtools.arbori.home", arboriHome);
			formatter.options.put(formatter.formatProgramURL, arboriConfigPath.toFile().getAbsolutePath());
		}
	}
}
