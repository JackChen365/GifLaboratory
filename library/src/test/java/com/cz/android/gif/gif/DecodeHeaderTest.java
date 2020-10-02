package com.cz.android.gif.gif;

import com.cz.android.gif.GifHeaderDecoder;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by cz
 * @date 2020/9/16 5:12 PM
 * @email bingo110@126.com
 *
 * Decode test. this sample illustrate the class {@link GifHeaderDecoder}
 * We only decode the basic information of the file. Not include the image data, but save the data block range instead.
 *
 * @see GifHeaderDecoder
 *
 */
public class DecodeHeaderTest {

    @Test
    public void decodeTest() throws IOException {
        //https://wiki.whatwg.org/wiki/GIF
        List<File> fileList=new ArrayList<>();
        fileList.add(new File("../app/src/main/assets/image/image3.gif"));
//        fileList.add(new File("../app/src/main/assets/image/image2.gif"));
//        fileList.add(new File("../app/src/main/assets/image/image3.gif"));
//        fileList.add(new File("../app/src/main/assets/image/image4.gif"));
//        fileList.add(new File("../app/src/main/assets/image/interlace.gif"));
        GifHeaderDecoder decoder=new GifHeaderDecoder();
        for(File f:fileList){
            decoder.loadFile(f);
            int frameCount = decoder.getFrameCount();
            System.out.println("File:"+f.getName()+" frameCount:"+frameCount);
            for(int i=0;i<frameCount;i++){
                GifHeaderDecoder.GifFrame frame = decoder.getFrame(i);
                System.out.println("index:"+frame.index+" start:"+frame.imageDescriptor.start+" end:"+frame.imageDescriptor.end+
                        " left:"+frame.left+" top:"+frame.top+" width:"+frame.imageWidth+" height:"+frame.imageHeight);
            }
        }
    }
}
