package com.enavamaratha.enavamaratha.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utility {

	 private static Pattern pattern;
	    private static Matcher matcher;
	    //Email Pattern
        private static final String EMAIL_PATTERN = "^([A-Za-z\\s]+[A-Za-z]+)$";
    private static final String EMAILid =

            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	  
	    private static final String Mobile = "^[0-9]{10}$";
	  
	    /**
	     * Validate Email with regular expression
	     * 
	     * @return true for Valid Email and false for Invalid Email
	     */
        public static boolean validate(String name) {


            pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(name);
            return matcher.matches();
            // return true;
        }

	public static boolean validate2(String mobile)
	{
		 pattern = Pattern.compile(Mobile);
	        matcher = pattern.matcher(mobile);
	        return matcher.matches();
	        //return true;
	}

	public static boolean validate1(String emailID)
	{
		
		 
		 pattern = Pattern.compile(EMAILid);
	        matcher = pattern.matcher(emailID);
	        return matcher.matches();
	        //return true;
	}

}
