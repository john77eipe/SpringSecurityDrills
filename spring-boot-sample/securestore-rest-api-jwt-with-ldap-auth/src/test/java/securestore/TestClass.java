package securestore;

import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.LdapShaPasswordEncoder;
import org.springframework.security.crypto.password.Md4PasswordEncoder;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

public class TestClass {
	@Test
	public void testEncryption() {
		String pass = "PasswOrd";
//		encoders.put("ldap", new org.springframework.security.crypto.password.LdapShaPasswordEncoder());
//	    encoders.put("MD4", new org.springframework.security.crypto.password.Md4PasswordEncoder());
//	    encoders.put("MD5", new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("MD5"));
//	    encoders.put("noop", org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance());
//	    encoders.put("pbkdf2", new Pbkdf2PasswordEncoder());
//	    encoders.put("scrypt", new SCryptPasswordEncoder());
//	    encoders.put("SHA-1", new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("SHA-1"));
//	    encoders.put("SHA-256", new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("SHA-256"));
//	    encoders.put("sha256", new org.springframework.security.crypto.password.StandardPasswordEncoder());

		System.out.println(new BCryptPasswordEncoder().encode(pass));
		System.out.println(new LdapShaPasswordEncoder().encode(pass));
		System.out.println(new Md4PasswordEncoder().encode(pass));
		System.out.println(new MessageDigestPasswordEncoder("MD5").encode(pass));


		System.out.println(new StandardPasswordEncoder().encode(pass));

		
	}
}
