package com.securestore.controller;

public class CommonConstants {

	public static final String ERROR_CODE = "Error Code";

	public static final String ERROR_MESSAGE = "Error Message";
	public static final String SECRET_KEY = "$T@R@PP!#(^";
	public static final int EXPIRATION_UPDATE_TIME = 30 * 60000;
	public static final String TOKEN_HEADER = "Authorization";
	public static final String TOKENDAO_USER_NAME = "userName";
	public static final String TOKENDAO_TOKEN = "token";
	public static final String TOKENDAO_ROLE = "role";
	public static final String TOKENDAO_GIVEN_NAME = "givenName";
	public static final String TOKENDAO_FIRST_NAME = "firstName";
	public static final String TOKENDAO_LAST_NAME = "lastName";
	public static final String TOKENDAO_LOGGED_IN = "logged in";
	public static final String TOKENDAO_LOGGED_OUT = "logged out";
	public static final int BUSINESS_UNIT_ALL = 0;
	public static final String AUTHENTICATION_FAILED = "Authentication Failed";
	public static final String LOGIN_URL_BYPASS = "/api/login";
	public static final String NAME = "name";
	public static final String ROLE = "role";
	public static final String TOKEN = "token";
	public static final String SIR = "SIR";
	public static final String SCHEDULED = "SCHEDULED";

	public static final String STAR001 = "STAR001";
	public static final String STAR002 = "STAR002";
	public static final String STAR003 = "STAR003";
	public static final String STAR005 = "STAR005";
	public static final String STAR006 = "STAR006";
	public static final String STAR007 = "STAR007";
	public static final String STAR008 = "STAR008";
	public static final String STAR108 = "STAR108";
	public static final String STAR109 = "STAR109";
	public static final String STAR099 = "STAR099";
	public static final String STAR100 = "STAR100";
	public static final String STAR101 = "STAR101";
	public static final String STAR102 = "STAR102";
	public static final String STAR103 = "STAR103";
	public static final String STAR200 = "STAR200";
	public static final String STAR098 = "STAR098";
	public static final String STAR097 = "STAR097";
	public static final String STAR096 = "STAR096";
	public static final String STAR095 = "STAR095";

	public static final String STAR094 = "STAR094";
	public static final String STAR093 = "STAR093";
	public static final String STAR092 = "STAR092";

	public static final String STAR010 = "STAR010";
	public static final String STAR011 = "STAR011";
	public static final String STAR012 = "STAR012";
	public static final String STAR013 = "STAR013";
	public static final String STAR014 = "STAR014";
	public static final String STAR015 = "STAR015";

	public enum StatusCodes {

		SUCCESSFUL(2), FAILURE(4), SERVER_ERROR(5);
		private int codeValue;

		private StatusCodes(int codeValue) {
			this.codeValue = codeValue;
		}

		public int getCodeValue() {
			return codeValue;
		}
	}

}