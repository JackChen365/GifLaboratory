package com.cz.android.gif.lzw;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test LZW algorithm.
 *
 */
public class LZWAlgorithm {

    public static List<Integer> encode(String value){
        Map<List<Character>,Integer> directory=new HashMap<>();
        int idleCode=256;
        for(int i=0;i<idleCode;i++){
            directory.put(Arrays.asList((char)i),i);
        }
        // 1. Initialize string table;
        List<Integer> result=new ArrayList<>();
        List<Character> prefix=new ArrayList<>();
        // 2. [.prefix.] <- empty;
        List<Character> cur;
        for(int i=0;i<value.length();i++){
        // 3. K <- next character in charstream;
            char K = value.charAt(i);
            cur=new ArrayList<>(prefix.size()+1);
            cur.addAll(prefix);
            cur.add(K);
        // 4. Is [.prefix.]K in string table?
            if(directory.containsKey(cur)){
                // [.prefix.] <- [.prefix.]K;
                // go to [3];
                prefix = cur;
            } else {
                // add [.prefix.]K to the string table;
                // output the code for [.prefix.] to the codestream;
                // [.prefix.] <- K;
                // go to [3];
                directory.put(cur,idleCode++);
                result.add(directory.get(prefix));
                prefix = Arrays.asList(K);
            }
        }
        if(0 < prefix.size()){
            result.add(directory.get(prefix));
        }
        return result;
    }


    public static String decode(List<Integer> array) {
        // 1. Initialize string table;
        StringBuilder output=new StringBuilder();
        Map<Integer,List<Character>> directory=new HashMap<>();
        int idleCode=256;
        for(int i=0;i<idleCode;i++){
            directory.put(i,Arrays.asList((char)i));
        }
        int pre;
        // 2. get first code: <code>;
        int cur = array.get(0);
        // 3. output the string for <code> to the charstream;
        List<Character> outputCharacter = directory.get(cur);
        for(Character ch:outputCharacter){
            output.append(ch);
        }
        List<Character> privious;
        for(int i=1;i<array.size();i++){
            //4. <old> = <code>;
            pre = cur;
            //5. <code> <- next code in codestream;
            cur=array.get(i);
            //6. does <code> exist in the string table?
            if(directory.containsKey(cur)){
                // yes:
                    //output the string for <code> to the charstream;
                    //[...] <- translation for <old>;
                    //K <- first character of translation for <code>;
                    //add [...]K to the string table;
                    //<old> <- <code>;
                List<Character> value = directory.get(cur);
                for(Character ch:value){
                    output.append(ch);
                }
                char c = value.get(0);

                privious = directory.get(pre);
                List<Character> list=new ArrayList<>(privious.size()+1);
                list.addAll(privious);
                list.add(c);
                directory.put(idleCode++,list);
            } else {
                //no:
                    //[...] <- translation for <old>;
                    //K <- first character of [...];
                    //output [...]K to charstream and add it to string table;
                    //<old> <- <code>
                privious = directory.get(pre);
                char c = privious.get(0);
                List<Character> list=new ArrayList<>(privious.size()+1);
                list.addAll(privious);
                list.add(c);
                directory.put(idleCode++,list);

                for(Character ch:list){
                    output.append(ch);
                }
            }
            // 7. go to [5];
        }
        return output.toString();
    }
}
