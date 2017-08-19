package net.zargor.afterlife.passwords.algorithms;

import org.apache.commons.codec.digest.DigestUtils;

public class SHA256 extends PasswordHashingAlgorithm {

    public SHA256(String salt) {
        super("sha-256", salt);
    }

    @Override
    public String hashPassword(String password) {
        return DigestUtils.sha256Hex(getSalt() + password);
    }

    @Override
    public boolean checkPassword(String unhashed, String hashed) {
        String unhashedHashed = hashPassword(unhashed);
        return unhashedHashed.equals(hashed);
    }
}
