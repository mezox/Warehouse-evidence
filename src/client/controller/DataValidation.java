package client.controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataValidation {

	private static Pattern pattern;
	private static Matcher matcher;

	private static final String USERNAME_REGEX = "^(x[a-z]{5}[0-9]{2})$";
	private static final String PASSWORD_REGEX = "^[a-zA-Z0-9]{8,}$";

     /**
     * Validates username to match WIS login
     * @param username
     * @return match
     */
	public static boolean validateUsername(String username){

		pattern = Pattern.compile(USERNAME_REGEX);
		return validate(username);
	}

     /**
     * Validates password to match DB password
     * @param password
     * @return match
     */
	public static boolean validatePassword(String password){

		pattern = Pattern.compile(PASSWORD_REGEX);
		return validate(password);
	}
        
    /**
     * Validator itself :)
     * @param textToValidate
     * @return match
     */
    private static boolean validate(final String textToValidate){
        matcher = pattern.matcher(textToValidate);
        return matcher.matches();
    }
}
