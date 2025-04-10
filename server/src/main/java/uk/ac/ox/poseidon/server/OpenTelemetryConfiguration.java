/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.poseidon.server;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;

import java.util.Optional;

import static java.lang.System.Logger.Level.INFO;

public class OpenTelemetryConfiguration {

    private static final System.Logger logger =
        System.getLogger(OpenTelemetryConfiguration.class.getName());

    public static OpenTelemetry initOpenTelemetry() {
        final SdkTracerProvider tracerProvider =
            Optional.ofNullable(System.getenv().get("OTEL_EXPORTER_OTLP_ENDPOINT"))
                .map(OpenTelemetryConfiguration::createTracerProvider)
                .orElseGet(() -> SdkTracerProvider.builder().build());
        return OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .buildAndRegisterGlobal();
    }

    private static SdkTracerProvider createTracerProvider(final String endpoint) {
        logger.log(INFO, "Using OTLP endpoint: {0}", endpoint);
        final OtlpGrpcSpanExporter spanExporter =
            OtlpGrpcSpanExporter.builder()
                .setEndpoint(endpoint)
                .build();
        final BatchSpanProcessor spanProcessor =
            BatchSpanProcessor.builder(spanExporter).build();
        return SdkTracerProvider
            .builder()
            .addSpanProcessor(spanProcessor)
            .setResource(Resource.create(Attributes.of(
                AttributeKey.stringKey("service.name"), "poseidon-server"
            )))
            .build();
    }
}
