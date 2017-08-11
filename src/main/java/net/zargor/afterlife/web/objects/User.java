package net.zargor.afterlife.web.objects;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

/**
 * Simple user
 */
@Data
@AllArgsConstructor
public class User {

    private String name;
    private String email;
    private byte[] avatar;
    private Map<String, String> informations;
    private Group group;
}