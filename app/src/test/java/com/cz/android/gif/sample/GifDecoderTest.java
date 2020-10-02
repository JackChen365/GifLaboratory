package com.cz.android.gif.sample;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

/** Tests for {@link GifDecoder}. */
public class GifDecoderTest {
  @Test
  public void testCorrectPixelsDecoded1() throws IOException {
    File file=new File("src/main/assets/image/image3.gif");
    GifDecoder decoder1=new GifDecoder();
    decoder1.loadFile(file);
    int[] globalColorTable = decoder1.getGlobalColorTable();

    //-71 -120 -107 117
//    GlideGifDecoder decoder2=new GlideGifDecoder();
//    byte[] bytes = Files.readAllBytes(file.toPath());
//    GifHeaderParser headerParser = new GifHeaderParser();
//    headerParser.setData(bytes);
//    GifHeader header = headerParser.parseHeader();
//    decoder2.setData(header, bytes);
//    decoder2.trace("loadFile");
//
//    int frameCount = decoder1.getFrameCount();
//    for(int i=0;i<1;i++){
//      int count=0;
//      int[] bytes1 = decoder1.decodeFrame(i);
//      decoder2.advance();
//      int[] bytes2 = decoder2.getNextFrame();
//      for(int k=0;k<bytes1.length;k++){
//        if(bytes1[k] != bytes2[k]){
//          count++;
//        }
//      }
//      System.out.println("index:"+i+" size:"+count);
//    }
//    TimeTrace timeTrace1 = decoder1.getTimeTrace();
//    TimeTrace timeTrace2 = decoder2.getTimeTrace();
//
//    long totalTime1=0;
//    long totalTime2=0;
//    Map<String, Long> timeRecord1 = timeTrace1.getTimeRecord();
//    Map<String, Long> timeRecord2 = timeTrace2.getTimeRecord();
//    Set<String> traceTags = timeRecord1.keySet();
//    for(String tag:traceTags){
//      long escapedTime1 = timeRecord1.get(tag);
//      long escapedTime2 = timeRecord2.get(tag);
//      totalTime1+=escapedTime1;
//      totalTime2+=escapedTime2;
//      if(tag.endsWith("decode code table.")){
//        System.out.println(tag+" time:"+escapedTime1+" "+escapedTime2);
//      }
//    }
//    //totalTime1:470285 totalTime2:453316 time:16969
//    //totalTime1:509343 totalTime2:491219 time:18124
//    //totalTime1:495849 totalTime2:478848 time:17001
//    System.out.println("totalTime1:"+totalTime1+" totalTime2:"+totalTime2+" time:"+(totalTime1-totalTime2));

  }

  @Test
  public void testDecoding() throws IOException {
    File file=new File("src/main/assets/image/image1.gif");
    GifDecoder decoder1=new GifDecoder();
    decoder1.loadFile(file);
    int frameCount = decoder1.getFrameCount();
    for(int i=0;i<frameCount;i++) {
      int[] bytes1 = decoder1.decodeFrame(i);
      int width = decoder1.getWidth();
      int height = decoder1.getHeight();
      for(int i1=0;i1<height;i1++){
        for(int i2=0;i2<width;i2++){
          int index=(i1*width)+i2;
          int b=bytes1[index];
          System.out.print(b+",");
        }
        System.out.println();
      }
    }
  }

  @Test
  public void colorTest(){
    //    dest:-7704065 a:255 blue:138 green:113 red:255 color:-36470 a:255 g:255 b:113 r:138
    //The alpha color red:255 green:113 blue:138
    int alpha=0xFF000000;
    int red=255;
    int green=113;
    int blue=138;
    int color=alpha;
    color|=blue<<16;
    color|=green<<8;
    color|=red;
  }

  public CharSequence padStart(CharSequence text,int length,char padChar){
    if (length < 0)
      throw new IllegalArgumentException("Desired length $length is less than zero.");
    if (length <= text.length())
      return text.subSequence(0, text.length());

    StringBuilder sb = new StringBuilder(length);
    for (int i=1;i<=(length - text.length());i++)
      sb.append(padChar);
    sb.append(text);
    return sb;
  }
}
