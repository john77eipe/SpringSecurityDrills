package com.securestore.config;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * This is added here for reference - it's not used
 */
public class PasswordEncryption implements PasswordEncoder {

    public String encode(CharSequence rawPassword){
        String hashed = "";
        try {
            hashed = BCrypt.hashpw(rawPassword.toString(), BCrypt.gensalt(12));
        } catch (IllegalArgumentException illegalArgumentException) {

        } catch (NullPointerException nullPointerException) {

        } catch(Exception exception) {

        }
        return hashed;
    }


    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        boolean result = false;
        try {
            result = BCrypt.checkpw(rawPassword.toString(), encodedPassword);
        } catch (IllegalArgumentException illegalArgumentException){

        } catch (NullPointerException nullPointerException){

        } catch(Exception exception){

        }
        return result;
    }


}
