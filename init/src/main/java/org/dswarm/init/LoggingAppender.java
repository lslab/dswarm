/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.init;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.boolex.OnMarkerEvaluator;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.filter.EvaluatorFilter;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.RollingPolicy;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedFileNamingAndTriggeringPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.FilterReply;
import com.google.common.collect.ImmutableList;

final class LoggingAppender {

	private static final String FILE_SEPARATOR = System.getProperty("file.separator", "/");

	private final Context context;
	private final String basePath;
	private final List<String> extraPaths;
	private final String logFileBaseName;
	private final String name;
	private final Level level;
	private final Optional<String> marker;
	private final FilterReply markerReply;
	private final String pattern;
	private final int maxFileSizeInMB;
	private final int maxHistory;

	private LoggingAppender(
			final Context context,
			final String basePath,
			final List<String> extraPaths,
			final String logFileBaseName,
			final String name,
			final Level level,
			final Optional<String> marker,
			final FilterReply markerReply,
			final String pattern,
			final int maxFileSizeInMB,
			final int maxHistory) {
		this.context = context;
		this.basePath = basePath;
		this.extraPaths = extraPaths;
		this.logFileBaseName = logFileBaseName;
		this.name = name;
		this.level = level;
		this.marker = marker;
		this.markerReply = markerReply;
		this.pattern = pattern;
		this.maxFileSizeInMB = maxFileSizeInMB;
		this.maxHistory = maxHistory;
	}

	static Builder of(final Context context) {
		return new Builder(context);
	}

	static Builder copy(final LoggingAppender prototype) {
		final Builder builder = new Builder(prototype.context)
				.withBasePath(prototype.basePath)
				.withName(prototype.name)
				.withLogFileBaseName(prototype.logFileBaseName)
				.withLevel(prototype.level)
				.withPattern(prototype.pattern)
				.withMaxFileSizeInMB(prototype.maxFileSizeInMB)
				.withMaxHistory(prototype.maxHistory);
		prototype.extraPaths.forEach(builder::addPath);

		if (prototype.markerReply == FilterReply.ACCEPT) {
			prototype.marker.ifPresent(builder::withMarker);
		} else if (prototype.markerReply == FilterReply.DENY) {
			prototype.marker.ifPresent(builder::withoutMarker);
		}

		return builder;
	}

	private String filePath(final CharSequence baseName, final String suffix) {
		final StringJoiner joiner = new StringJoiner(FILE_SEPARATOR).add(basePath);
		extraPaths.forEach(joiner::add);
		return joiner
				.add(level.levelStr.toLowerCase())
				.add(baseName) + "." + suffix;
	}

	private TimeBasedFileNamingAndTriggeringPolicy<ILoggingEvent> sizedBasedNaming(final Context context) {
		final SizeAndTimeBasedFNATP<ILoggingEvent> fnatp = new SizeAndTimeBasedFNATP<>();
		fnatp.setContext(context);
		fnatp.setMaxFileSize(maxFileSizeInMB + "MB");
		return fnatp;
	}

	private RollingPolicy rollingPolicy(final CharSequence baseName, final Context context) {
		final TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
		rollingPolicy.setContext(context);
		rollingPolicy.setFileNamePattern(filePath(baseName, "%d{yyyy-MM-dd}.%i.log"));
		rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(sizedBasedNaming(context));
		rollingPolicy.setMaxHistory(maxHistory);

		return rollingPolicy;
	}

	private Encoder<ILoggingEvent> encoder(final Context context) {
		final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setContext(context);
		encoder.setPattern(pattern);
		encoder.start();

		return encoder;
	}

	private Filter<ILoggingEvent> levelFilter(final Context context) {
		final ThresholdFilter filter = new ThresholdFilter();
		filter.setContext(context);
		filter.setLevel(level.levelStr);
		filter.start();

		return filter;
	}

	private Function<String, Filter<ILoggingEvent>> markerFilter(final Context context) {
		return marker -> markerFilter(marker, context);
	}

	private Filter<ILoggingEvent> markerFilter(final String marker, final Context context) {
		final OnMarkerEvaluator markerEvaluator = new OnMarkerEvaluator();
		markerEvaluator.setContext(context);
		markerEvaluator.addMarker(marker);
		markerEvaluator.start();

		final EvaluatorFilter<ILoggingEvent> filter = new EvaluatorFilter<>();
		filter.setContext(context);
		filter.setEvaluator(markerEvaluator);
		filter.setOnMatch(markerReply);
		filter.setOnMismatch(markerReply == FilterReply.ACCEPT ? FilterReply.DENY : FilterReply.NEUTRAL);
		filter.start();

		return filter;
	}

	private RollingFileAppender<ILoggingEvent> rollingAppender(final CharSequence baseName, final Context context) {
		final RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
		fileAppender.setContext(context);
		fileAppender.setName(name);
		fileAppender.setFile(filePath(baseName, "log"));

		return fileAppender;
	}

	Appender<ILoggingEvent> createFileAppender() {
		return createFileAppender(logFileBaseName, context);
	}

	Appender<ILoggingEvent> createFileAppender(final CharSequence baseName, final Context context) {
		final RollingFileAppender<ILoggingEvent> fileAppender = rollingAppender(baseName, context);
		final RollingPolicy rollingPolicy = rollingPolicy(baseName, context);

		rollingPolicy.setParent(fileAppender);
		fileAppender.setRollingPolicy(rollingPolicy);
		marker.map(markerFilter(context)).ifPresent(fileAppender::addFilter);
		fileAppender.addFilter(levelFilter(context));
		fileAppender.setEncoder(encoder(context));

		rollingPolicy.start();
		fileAppender.start();

		return fileAppender;
	}

	LoggingAppender appendTo(final AppenderAttachable<ILoggingEvent> logger) {
		logger.addAppender(createFileAppender());
		return this;
	}

	Builder copy() {
		return copy(this);
	}

	static class Builder {
		private final Context context;
		private final ImmutableList.Builder<String> addPaths = ImmutableList.builder();
		private String basePath;
		private String name;
		private String logFileBaseName = "messages";
		private Level level = Level.INFO;
		private String pattern = "%date %level [%thread] %logger [%file:%line] %msg%n";
		private Optional<String> marker = Optional.empty();
		private FilterReply markerReply = FilterReply.NEUTRAL;
		private int maxFileSizeInMB = 50;
		private int maxHistory = 30;

		private Builder(final Context context) {
			this.context = context;
		}

		Builder withBasePath(final String basePath) {
			this.basePath = basePath;
			return this;
		}

		Builder addPath(final String path) {
			addPaths.add(path);
			return this;
		}

		Builder withLogFileBaseName(final String logFileBaseName) {
			this.logFileBaseName = logFileBaseName;
			return this;
		}

		Builder withName(final String name) {
			this.name = name;
			return this;
		}

		Builder withLevel(final Level level) {
			this.level = level;
			return this;
		}

		Builder withPattern(final String pattern) {
			this.pattern = pattern;
			return this;
		}

		Builder withMarker(final String marker) {
			this.marker = Optional.of(marker);
			this.markerReply = FilterReply.ACCEPT;
			return this;
		}

		Builder withoutMarker(final String marker) {
			this.marker = Optional.of(marker);
			this.markerReply = FilterReply.DENY;
			return this;
		}

		Builder withMaxFileSizeInMB(final int maxFileSizeInMB) {
			this.maxFileSizeInMB = maxFileSizeInMB;
			return this;
		}

		Builder withMaxHistory(final int maxHistory) {
			this.maxHistory = maxHistory;
			return this;
		}

		LoggingAppender build() {
			return new LoggingAppender(
					context,
					basePath,
					addPaths.build(),
					logFileBaseName,
					name,
					level,
					marker,
					markerReply,
					pattern,
					maxFileSizeInMB,
					maxHistory);
		}

		Builder appendTo(final AppenderAttachable<ILoggingEvent> logger) {
			return build().appendTo(logger).copy();
		}
	}
}
