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

import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static io.grpc.Status.INVALID_ARGUMENT;
import static java.lang.System.Logger.Level.ERROR;

public abstract class RequestHandler<ReqT, RespT> {

    private static final System.Logger logger = System.getLogger(RequestHandler.class.getName());

    static LocalDateTime toLocalDateTime(final Timestamp startDateTime) {
        return LocalDateTime.ofInstant(
            Instant.ofEpochSecond(
                startDateTime.getSeconds()), ZoneOffset.UTC
        );
    }

    static UUID parseId(final String id) {
        try {
            return UUID.fromString(id);
        } catch (final IllegalArgumentException e) {
            throw INVALID_ARGUMENT
                .withDescription("Invalid UUID: " + id)
                .asRuntimeException();
        }
    }

    protected abstract RespT getResponse(final ReqT request);

    public void handle(
        final ReqT request,
        final StreamObserver<RespT> responseObserver
    ) {
        try {
            responseObserver.onNext(getResponse(request));
            responseObserver.onCompleted();
        } catch (final StatusRuntimeException e) {
            logger.log(ERROR, e);
            responseObserver.onError(e);
        } catch (final Exception e) {
            logger.log(ERROR, e);
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("Unexpected server error: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException()
            );
        }
    }

}
