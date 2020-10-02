//
// Created by Jack Chen on 9/26/2020.
//

#ifndef GIFSAMPLE_GIFDECODER_H
#define GIFSAMPLE_GIFDECODER_H

#include <iostream>
#include "GifHeaderDecoder.h"

class GifDecoder {
private:
    const int COLOR_TRANSPARENT_BLACK = 0x00000000;
    const int MAX_STACK_SIZE=1 << 12;
    /**
     * The header decoder object. This is for we to know the most common information in the file.
     * By decoding this kind of information. We could easy know the dimension of the files and others.
     */
    GifHeaderDecoder* headerDecoder;
    /**
     * File reader.
     */
    BufferReader* reader;

    uint32_t pixelsSize;
    /**
     * The code table. It stores all the code that inside the file.
     */
    uint8_t* pixels;
    //-----------------------------------------------------------
    // prefix, suffix and pixelStack is for decoding the code table.
    //-----------------------------------------------------------
    uint16_t* prefix;
    uint8_t* suffix;
    uint8_t* pixelStack;
    /**
     * Current pixels for decoding each image.
     */
    uint32_t* currentPixels;

    /**
     * Current pixels for decoding each image.
     */
    uint32_t* previousPixels;
    /**
     * If the GIF file has a frame that the disposal method is {@link #DISPOSAL_PREVIOUS}
     * We have to go back find the frame that the disposal method is {@link #DISPOSAL_NONE} or {@link #DISPOSAL_UNSPECIFIED}
     * Here we don't want to check every time. That's why we got this class field.
     */
    bool hasDisposalMethod;

    /**
     * Read the basic image data.
     * @param reader
     * @param currentFrame
     * @param previousFrame
     * @param colorTable
     * @param disposalMethod
     * @return
     */
    uint32_t* readBasedImageData(uint16_t& frameIndex,BufferReader &reader, GifFrame* currentFrame,
                GifFrame* previousFrame, uint32_t *colorTable, uint8_t &disposalMethod);
public:
    GifDecoder();
    ~GifDecoder();

    /**
     * Load a Gif file.
     * @param reader
     */
    bool loadImage(char* filePath);
    /**
     * Decode the pixels from the GifFrame.
     * @param index
     * @return
     */
    uint32_t* decodeFrame(uint16_t& index);
    /**
     * Decode the code table.
     */
    uint8_t* decodeCodeTable(uint16_t& frameIndex,uint16_t &imageWidth, uint16_t &imageHeight);

    /**
     * The version of the file. It usually GIF89a or GIF87a
     * @return
     */
    char* getVersion();

    /**
     * The frame total size of the file.
     * @return
     */
    uint16_t getFrameSize();

    /**
     * The logical screen width.
     * @return
     */
    uint16_t& getWidth();

    /**
     * The logical screen width.
     * @return
     */
    uint16_t& getHeight();

    /**
     * The Global color table.
     * @return
     */
    uint32_t* getGlobalColorTable();

    /**
     * The Global color table size.
     * @return
     */
    uint32_t getGlobalColorTableSize();

    /**
     * The loop count and if the value equal to zero. It means loop infinite.
     * @return
     */
    uint16_t& getLoopCount();

    /**
     * The delay time for each frame.
     * @param index
     * @return
     */
    uint16_t& getDelayTime(int index);

};

#endif //GIFSAMPLE_GIFDECODER_H
