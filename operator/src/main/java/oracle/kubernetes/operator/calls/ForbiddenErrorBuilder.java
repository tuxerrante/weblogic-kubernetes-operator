// Copyright (c) 2019, 2020, Oracle Corporation and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package oracle.kubernetes.operator.calls;

import java.net.HttpURLConnection;

import io.kubernetes.client.openapi.ApiException;

/**
 * A builder for 'forbidden' async results.
 */
public class ForbiddenErrorBuilder implements FailureStatusSource {
  private static final String FORBIDDEN_REASON = "Forbidden";

  private final String message;

  private ForbiddenErrorBuilder(CallResponse callResponse) {

    // FIXME: build message including details of request

    this.message = e.getMessage();
  }

  public static boolean isForbiddenOperation(ApiException e) {
    return e.getCode() == HttpURLConnection.HTTP_FORBIDDEN;
  }

  /**
   * Create a ForbiddenErrorBuilder from the provided failed call.
   * @param callResponse the failed call
   * @return the ForbiddenErrorBuilder
   */
  public static ForbiddenErrorBuilder fromFailedCall(CallResponse callResponse) {
    ApiException apiException = callResponse.getE();
    if (!isForbiddenOperation(apiException)) {
      throw new IllegalArgumentException("Is not forbidden exception");
    }

    return new ForbiddenErrorBuilder(callResponse);
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public String getReason() {
    return FORBIDDEN_REASON;
  }
}
