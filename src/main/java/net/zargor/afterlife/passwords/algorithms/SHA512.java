package net.zargor.afterlife.passwords.algorithms;

import org.apache.commons.codec.digest.DigestUtils;

public class SHA512 extends PasswordHashingAlgorithm {

    public SHA512(String salt) {
        super("sha-512", salt);
    }

    @Override
    public String hashPassword(String password) {
        return DigestUtils.sha512Hex(getSalt() + password);
    }

    @Override
    public boolean checkPassword(String unhashed, String hashed) {
        String unhashedHashed = hashPassword(unhashed);
        return unhashedHashed.equals(hashed);
    }
}
