package com.cz.android.gif.gif;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a very good example to understand how GIF file encoding and decoding.
 * We use a int array to store a 10*10 GIF image. As you can see above. It only have four different colors.
 * And the picture you are able to fine it in app/src/main/assets/image/image1.gif.
 *
 * This is the code that refer to the reference:references/What's In A GIF - LZW Image Data.html
 * If you want know more details about how the GIF file works. You have to take a look.
 *
 */
public class GifTest1 {

    @Test
    public void fileTest(){
        //10*10
        int[] colorTable=new int[4];
        colorTable[0]=-1;
        colorTable[1]=-65536;
        colorTable[2]=-16776961;
        colorTable[3]=-16777216;
        //Encode the pixels.
        encodingTest(colorTable);
        //Decode the file.
        decodingTest(colorTable);
    }

    private void encodingTest(int[] colorTable) {
        //pixel
        int[] pixelArray=new int[]{
                1,1,1,1,1,2,2,2,2,2,
                1,1,1,1,1,2,2,2,2,2,
                1,1,1,1,1,2,2,2,2,2,
                1,1,1,0,0,0,0,2,2,2,
                1,1,1,0,0,0,0,2,2,2,
                2,2,2,0,0,0,0,1,1,1,
                2,2,2,0,0,0,0,1,1,1,
                2,2,2,2,2,1,1,1,1,1,
                2,2,2,2,2,1,1,1,1,1,
                2,2,2,2,2,1,1,1,1,1
        };
        System.out.println("Code table:");
        List<Integer> codeStream = compress(colorTable, pixelArray);
        for(Integer code:codeStream){
            System.out.print(code+",");
        }
        System.out.println("\n");
    }

    private void decodingTest(int[] colorTable) {
        int[] pixelArray=new int[]{
                1,1,1,1,1,2,2,2,2,2,
                1,1,1,1,1,2,2,2,2,2,
                1,1,1,1,1,2,2,2,2,2,
                1,1,1,0,0,0,0,2,2,2,
                1,1,1,0,0,0,0,2,2,2,
                2,2,2,0,0,0,0,1,1,1,
                2,2,2,0,0,0,0,1,1,1,
                2,2,2,2,2,1,1,1,1,1,
                2,2,2,2,2,1,1,1,1,1,
                2,2,2,2,2,1,1,1,1,1
        };
        List<Integer> codeStream = compress(colorTable, pixelArray);
        List<Integer> arr = decompress(colorTable, codeStream);
        System.out.println("Decode result:");
        outputArray(arr,10,10);
    }


    private void outputArray(List<Integer> arr,int rowCount,int columnCount) {
        for(int row=0;row<rowCount;row++){
            for(int column=0;column<columnCount;column++){
                int i = row * columnCount + column;
                System.out.print(arr.get(i)+",");
            }
            System.out.println();
        }
    }

    private List<Integer> compress(int[] colorTable,int[] pixelArray){
        //1.Initialize code table
        Map<List<Integer>,Integer> codeTable=new HashMap<>();
        for(int i=0;i<colorTable.length;i++){
            codeTable.put(Arrays.asList(i), i);
        }
        int clearCode = colorTable.length;
        int eof = colorTable.length+1;
        int codeIndex=eof+1;
        //2.Always start by sending a clear code to the code stream.
        List<Integer> result=new ArrayList<>();
//        result.add(clearCode);
        //3.Read first index from index stream. This value is now the value for the index buffer
        List<Integer> previous=new ArrayList<>();
        List<Integer> current;
        int index=0;
        int K = pixelArray[index++];
        previous.add(K);
        //<LOOP POINT>
        while(index < pixelArray.length){
            K=pixelArray[index++];
            current=new ArrayList<>(previous.size()+1);
            current.addAll(previous);
            current.add(K);
            if(codeTable.containsKey(current)){
                previous=current;
            } else {
                codeTable.put(current,codeIndex++);
                result.add(codeTable.get(previous));
                previous.clear();
                previous.add(K);
            }
        }
        if(!previous.isEmpty()){
            result.add(codeTable.get(previous));
        }
//        result.add(eof);
        //The final code stream would look like this:
        //    #4 #1 #6 #6 #2 #9 #9 #7 #8 #10 #2 #12 #1 #14 #15 #6 #0 #21 #0 #10 #7 #22 #23 #18 #26 #7 #10 #29 #13 #24 #12 #18 #16 #36 #12 #5
        //This is only 36 codes versus the 100 that would be required without compression.
        return result;
    }

    private List<Integer> decompress(int[] colorTable, List<Integer> codeStream) {
        List<Integer> result=new ArrayList<>();
        Map<Integer,List<Integer>> codeTable=new HashMap<>();
        for(int i=0;i<colorTable.length;i++){
            codeTable.put(i,Arrays.asList(i));
        }
        int index=0;
        int clearCode=colorTable.length;
        int eof=colorTable.length+1;
        int codeIndex=colorTable.length+2;
        //let CODE be the first code in the code stream
        //output {CODE} to index stream
        int previousCode;
        int currentCode=codeStream.get(index++);
        List<Integer> list = codeTable.get(currentCode);
        output(result,list);
        List<Integer> previous;
        while(index < codeStream.size()){
            previousCode = currentCode;
            currentCode = codeStream.get(index++);
            if(codeTable.containsKey(currentCode)){
                List<Integer> value=codeTable.get(currentCode);
                output(result,value);
                Integer c = value.get(0);

                previous = codeTable.get(previousCode);
                list=new ArrayList<>();
                list.addAll(previous);
                list.add(c);
                codeTable.put(codeIndex++,list);
            } else {
                previous = codeTable.get(previousCode);
                Integer c = previous.get(0);
                list=new ArrayList<>();
                list.addAll(previous);
                list.add(c);
                codeTable.put(codeIndex++,list);

                output(result,list);
            }
        }
        System.out.println("-------------------------------");
        System.out.println("Code table");
        for(Map.Entry<Integer,List<Integer>> entry:codeTable.entrySet()){
            Integer code = entry.getKey();
            List<Integer> value = entry.getValue();
            System.out.println("Code:"+code+" "+value);
        }
        System.out.println("-------------------------------");
//        1,1,1,1,1,2,2,2,2,2,
//        1,1,1,1,1,2,2,2,2,2,
//        1,1,1,1,1,2,2,2,2,2,
//        1,1,1,0,0,0,0,2,2,2,
//        1,1,1,0,0,0,0,2,2,2,
//        2,2,2,0,0,0,0,1,1,1,
//        2,2,2,0,0,0,0,1,1,1,
//        2,2,2,2,2,1,1,1,1,1,
//        2,2,2,2,2,1,1,1,1,1,
//        2,2,2,2,2,1,1,1,1,1,
        return result;
    }

    private void output(List<Integer> result,List<Integer> list){
        for(Integer i:list){
            result.add(i);
        }
    }
}
