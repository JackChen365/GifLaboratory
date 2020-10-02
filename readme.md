## README

A library to learn the GIF file encoding and decoding from scratch.

### [中文简介](./readme-cn.md)

### [示例下载](https://github.com/momodae/LibraryResources/blob/master/GifLaboratory/file/app-debug.apk?raw=true)

The project structure.

```
:app
:library
```

### Picture

* ![image1](https://github.com/momodae/LibraryResources/blob/master/GifLaboratory/image/image1.gif?raw=true)

* ![image2](https://github.com/momodae/LibraryResources/blob/master/GifLaboratory/image/image2.gif?raw=true)

* ![image3](https://github.com/momodae/LibraryResources/blob/master/GifLaboratory/image/image3.gif?raw=true)

* ![image4](https://github.com/momodae/LibraryResources/blob/master/GifLaboratory/image/image4.gif?raw=true)

* ![image5](https://github.com/momodae/LibraryResources/blob/master/GifLaboratory/image/image5.gif?raw=true)

### TestCase

File list:

```
|-- bit
    |-- BitBufferTest
|-- file
    |-- BufferedChannelReaderTest
|-- gif
    |-- DecodeHeaderTest
    |-- GifDecodeTest
    |-- GifTest1
    |-- GifTest2
|-- lzw
    |-- LZWAlgorithmTest
```

All of the test cases helps you understand how the GIF file works better.


### How to start.

First, you should learn how the LZW algorithm work. There are a lot of the articles about how LZW algorithm words.<br>
After you know how it works. Please read the document:spec-gif89a. It introduce all the basic encoding about the GIF file.<br>

From this document. you are able to read the basic information inside the GIF file. Until you are trying to read the image block.

I did a few preparation for you. which are all in the test cases.

If you want to understand how the image decoding works. Find a really easy file. That's a good start.
Use this file to get though how the file encoding and decoding.   

The most difficult part of decoding the file is why the byte data is unlike the code we decoded.<br> 
To be honest I have not seen others mention how they process the byte data after encoding. So here I am trying to make it clear.

Here are the basic data.

```
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
```

The color table

```
int[] colorTable=new int[4];
colorTable[0]=-1;
colorTable[1]=-65536;
colorTable[2]=-16776961;
colorTable[3]=-16777216;
```

For my understanding of encode the file is:

1. First use the color table instead of the real pixel. For example, if the color is #FFFF0000 which is red.
We store the red color in the table in the position zero. We use zero to represent the color red.

That's how the color tale works.

So the pixel table above looks list this.

![image](app/src/main/assets/image/image1.gif)

It's easy!

Then we keep going.

2. We use LZW algorithm to encode the pixels. Please refer to the test case:GifTest1

The final code table would looks like this

```
#4 #1 #6 #6 #2 #9 #9 #7 #8 #10 #2 #12 #1 #14 #15 #6 #0 #21 #0 #10 #7 #22 #23 #18 #26 #7 #10 #29 #13 #24 #12 #18 #16 #36 #12 #5
```

Here. after we got all the codes. It is time for use to reduce the byte size.

We reduce the pixel code from one hundred numbers to only thirty-six. If I remember correctly.

We assume that the code side is three. It actually is three. in that case We only store three bits into the data flow.

For example:

```
//        00000101 5
//        00000111 7
//        00001111 15
//        00011111 31
//        00100001 33
//        00100011 35
//        01111111 127
//        01110101

number:5 and 7
we keep the two of the numbers into one byte.
101 111 
if the we keep going and put number 15 into the byte.
101 111 11
11000000

It will look like this
```

This is how we reduce the bits.


### Sample list.

```
|-- gif
    |-- GifDetailActivity
    |-- GifViewActivity
    |-- NativeGifActivity
    |-- NativeTextureGifViewActivity
|-- test
    |-- BitmapFillTestActivity
    |-- GifPerformanceTestActivity
    |-- GifListActivity
|-- view
    |-- ByteTextViewActivity
    |-- GridLayoutActivity
```


* GifDetailActivity
    Read a GIF file and display all the data blocks.
* GifViewActivity
    Illustrate the disposal method: none/previous/background
* NativeGifActivity
    Demonstrate the native GIF decoding.
* NativeTextureGifViewActivity
    Demonstrate the native GIF decoding and render the image in work thread.

* BitmapFillTestActivity
    Testing fill a bitmap.
* GifPerformanceTestActivity
    Gif performance test.
* GifListActivity
    This demo uses NativeTextureGifView that shows how to fast recycle view and display GIF images. It is the most complicated case.

* ByteTextViewActivity
    How to display the byte in the file.
    
* GridLayoutActivity
    The GridLayout.

### References

* [spec-gif87](references/spec-gif87.txt)
* [spec-gif89a](references/spec-gif89a.txt)
* [LZW and GIF explained](references/LZW%20and%20GIF%20explained.html)
* what is in a gif
    This blog is Amazing. If you want to learn more about how GIF file encoding and decoding. Trust me, It worth your time.
    
    * [What's In A GIF - Bit by Byte](references/What's%20In%20A%20GIF%20-%20Bit%20by%20Byte.html)
    * [What's In A GIF - LZW Image Data](references/What's%20In%20A%20GIF%20-%20LZW%20Image%20Data.html)
    * [What's In A GIF - Animation and Transparency](references/What's%20In%20A%20GIF%20-%20Animation%20and%20Transparency.html)
    * [What's In A GIF - GIF Explorer](references/What's%20In%20A%20GIF%20-%20GIF%20Explorer.html)

* [Animation Basics -- IM v6 Examples](references/Animation%20Basics%20--%20IM%20v6%20Examples.html)
