/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.invocation;

import javax.annotation.Nullable;

/**
 * Responsible for executing a {@link BuildAction} and generating the result. A {@link BuildActionRunner} runs within the root build of the build tree.
 */
public interface BuildActionRunner {
    /**
     * Runs the given action, returning a result that describes the build outcome and the result that should be returned to the client.
     *
     * <p>Build failures should be packaged in the returned result, rather than thrown.
     */
    Result run(BuildAction action, BuildController buildController);

    /**
     * Packages up the result of a {@link BuildAction}, either success plus an optional result object, or failure.
     *
     * <p>Failures are represented using 2 exceptions: the build failure, which is the failure that the build completed with and that should be reported to the user (via logging), plus the client failure, which is the exception that should be forwarded to the client. Often these are the same, but may be different for specific combinations of action + failure.
     */
    class Result {
        private final boolean hasResult;
        private final Object result;
        private final Throwable buildFailure;
        private final Throwable clientFailure;
        private static final Result NOTHING = new Result(false, null, null, null);
        private static final Result NULL = new Result(true, null, null, null);

        private Result(boolean hasResult, Object result, Throwable buildFailure, Throwable clientFailure) {
            this.hasResult = hasResult;
            this.result = result;
            this.buildFailure = buildFailure;
            this.clientFailure = clientFailure;
        }

        public static Result nothing() {
            return NOTHING;
        }

        public static Result of(@Nullable Object result) {
            if (result == null) {
                return NULL;
            }
            return new Result(true, result, null, null);
        }

        public static Result failed(Throwable buildFailure) {
            return new Result(true, null, buildFailure, buildFailure);
        }

        public static Result failed(Throwable buildFailure, RuntimeException clientFailure) {
            return new Result(true, null, buildFailure, clientFailure);
        }

        /**
         * Is there a result, possibly a null object or a build failure?
         */
        public boolean hasResult() {
            return hasResult;
        }

        /**
         * Returns the result to send back to the client, possibly null.
         */
        @Nullable
        public Object getClientResult() {
            return result;
        }

        /**
         * Returns the failure to report in the build outcome.
         */
        @Nullable
        public Throwable getBuildFailure() {
            return buildFailure;
        }

        /**
         * Returns the failure to send back to the client.
         */
        @Nullable
        public Throwable getClientFailure() {
            return clientFailure;
        }

    }
}
