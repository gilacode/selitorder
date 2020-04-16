/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.avicenna.security.apis;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ApisClient {

  private final String accessTokenEndPoint ;
  private final String clientId;
  private final String clientSecret;
  private final String resourceId;
  private final String resourceSecret;
  private final String authorizationURL;
  private final String redirectUri;
  private final String verificationUrl;
  private final String scopes;

  @JsonCreator
  public ApisClient(String accessTokenEndPoint,
                    String clientId, // client id
                    String clientSecret, // client secret
                    String resourceId, // resource id
                    String resourceSecret, // resource secret
                    String authorizationURL, // authorization server url
                    String redirectUri, // client redirect url
                    String verificationUrl, // access token verification url
                    String scopes) {
        this.accessTokenEndPoint = accessTokenEndPoint;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.resourceId = resourceId;
        this.resourceSecret = resourceSecret;
        this.authorizationURL = authorizationURL;
        this.redirectUri = redirectUri;
        this.verificationUrl = verificationUrl;
        this.scopes = scopes;
  }

  public String getAccessTokenEndPoint() {
    return accessTokenEndPoint;
  }

  public String getClientId() {
    return clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public String getAuthorizationURL() {
    return authorizationURL;
  }

  public String getRedirectUri() {
    return redirectUri;
  }

  public String getVerificationUrl() {
    return verificationUrl;
  }

  public String getResourceId() {
    return resourceId;
  }

  public String getResourceSecret() {
    return resourceSecret;
  }

  public String getScopes() {
    return scopes;
  }
}
