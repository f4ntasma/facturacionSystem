package pe.altoque.common;

import java.security.SecureRandom;

public final class Tokens {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] ALFABETO = "abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private Tokens() {}

    public static String nuevo(int largo) {
        char[] out = new char[largo];
        for (int i = 0; i < largo; i++) out[i] = ALFABETO[RANDOM.nextInt(ALFABETO.length)];
        return new String(out);
    }
}
