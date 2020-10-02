package com.cz.android.gif.lzw;

import org.junit.Test;

import java.util.List;

/**
 * Test how LZW algorithm works.
 * @see LZWAlgorithm
 * Also take a look at /references/LZW and GIF explained.html
 */
public class LZWAlgorithmTest {
    @Test
    public void lawEncodeTest() {
        String value="abcbcabcabcd";
        System.out.println("Before encode:"+value);
        List<Integer> codeTable = LZWAlgorithm.encode(value);
//        ab code:256
//        bc code:257
//        cb code:258
//        bca code:259
//        abc code:260
//        ca code:261
//        abcd code:262
        System.out.println("Code table:"+codeTable);
        System.out.println("Decode result:"+LZWAlgorithm.decode(codeTable));

        //---------------------------------------------
        // Before encode:abcbcabcabcd
        // Code table:[97, 98, 99, 257, 256, 99, 260, 100]
        // Decode result:abcbcabcabcd
        //---------------------------------------------
    }
}
