package net.zargor.afterlife.web;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Encryption: SHA-512
 */
public class PasswordEncrypt {
    public String encryptPassword(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String salt = "DSkPoHdgw1xjCG6NybH22hE7dWFTZ8Edv#LX8l1HPJAyq19mNAQ2MIbPzNqW_3UuFpT8zsXqNdFQyZWVjqPxsr-LjpkpZxPrD#-SDFsxhcNMik5zptjoSkMYxeg9Nn0Mw55SFeX-5cCCMx04I4IgCM7q-lMolm5OkYMEFFwVItRLF#YHYPFEaDIqrzc1SYiYGE9fv_95qHcQ8Wy73Bba85KvmC-PD6N#r_#DFxUblQjd8_UDZwngUHXxol8fIm0AET8ifOYAz4kq7IYdGYzJhVHe19jQIrFkp2lp17KvW63F-jiSYfIQy4jFCKY1Y44FWi8N2AyQwE1A9GFu9soQ_96OE1eYLh9mATMDZKSMzh_AFRKsJV-zZzB51YF4rn4Am001pl9y2CSghqHFbWFqC39y6vT-Ih12Ut8Xubbbp2sxARCeCCkugnSlIojqioYX1#gW4VOlIRBPShVwFHTCvNp-qeJ#-pCK_vCjNphJ-fmytbSSK0IiIR_Jz1ko2v_U";
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt.getBytes("UTF-8"));
        byte[] bytes = md.digest(password.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
