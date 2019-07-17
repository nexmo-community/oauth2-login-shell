package com.vonage.oauth2.commands;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OAuth2Token {

	@JsonProperty("access_token")
	private String accessToken;

	@JsonProperty("token_token")
	private String tokenType;

	@JsonProperty("expires_in")
	private long expiresIn;

	@JsonProperty("refresh_token")
	private String refreshToken;

	@JsonProperty("scope")
	private String scope;

	public String getAccessToken() {
		return accessToken;
	}

	public String getTokenType() {
		return tokenType;
	}

	public long getExpiresIn() {
		return expiresIn;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public String getScope() {
		return scope;
	}

	@Override
	public String toString() {
		return "OAuth2Token [accessToken=" + accessToken + ", tokenType=" + tokenType + ", expiresIn=" + expiresIn
						+ ", refreshToken=" + refreshToken + ", scope=" + scope + "]";
	}

}
