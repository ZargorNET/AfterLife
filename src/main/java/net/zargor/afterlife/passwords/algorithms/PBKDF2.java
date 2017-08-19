package net.zargor.afterlife.passwords.algorithms;


import org.apache.commons.codec.binary.Hex;

import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PBKDF2 extends PasswordHashingAlgorithm {

    public PBKDF2(String salt) {
        super("PBKDF2", salt);
    }

    @Override
    public String hashPassword(String password) {
        try {
            int iterations = 4096;
            char[] chars = password.toCharArray();
            byte[] salt = getSalt().getBytes(Charset.forName("UTF-8"));

            PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 4096);


            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Hex.encodeHexString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean checkPassword(String unhashed, String hashed) {
        return hashPassword(unhashed).equals(hashed);
    }

}
