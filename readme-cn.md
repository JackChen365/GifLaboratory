## README

一个GIF实验程序，项目内有完整的GIF各部分编码解码程序示例。

当前项目结构

```
:app
:library
```

### 主要测试用测

文件例表:

```
|-- bit
    |-- BitBufferTest （字缓冲测试）
|-- file
    |-- BufferedChannelReaderTest （文件Channel缓冲包装对象）
|-- gif
    |-- DecodeHeaderTest (GIF全局信息解码测试）
    |-- GifDecodeTest （GIF常规解码测试）
    |-- GifTest1 
    |-- GifTest2
|-- lzw
    |-- LZWAlgorithmTest LZW压缩算法简单实现。
```

所有的测试都是在实验过程中，记录下来辅助理解的测试。

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
    通过可视化的组件展示一个GIF文件所有数据块信息。
* GifViewActivity
    展示不同DisposalMethod
* NativeGifActivity
    展示Native层解码
* NativeTextureGifViewActivity
    展示使用TextureView在线程池使用Native解码播放。

* BitmapFillTestActivity
    测试不同方式填充位图：1 以每个像素单独填充，2 以setPixels数组填充 3 Native层内存拷贝
* GifPerformanceTestActivity
    演示4种实现方式对比：1 Glide的Gif解码 2 自己实现的Java层解码 3 Native层解码 4 TextureNative多线程解码
* GifListActivity
    列表展示大量的GIf文件，使用TextureNative 线程池解码。优化大规模展示GIF场景性能

* ByteTextViewActivity
    展示文件字节块
    
* GridLayoutActivity
    展示网块控件。

### 参考资料

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
