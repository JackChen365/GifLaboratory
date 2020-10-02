//
// Created by cz on 2020/9/2.
//

#ifndef GIFSAMPLE_GIFHEADERDECODER_H
#define GIFSAMPLE_GIFHEADERDECODER_H


#include <cstdint>
#include <vector>
#include <memory>
#include "BufferReader.h"

/**
 * GIF Disposal Method meaning take no action.
 * <p><b>GIF89a</b>: <i>No disposal specified.
 * The decoder is not required to take any action.</i></p>
 */
static const int DISPOSAL_UNSPECIFIED = 0;
/**
 * GIF Disposal Method meaning leave canvas from previous frame.
 * <p><b>GIF89a</b>: <i>Do not dispose.
 * The graphic is to be left in place.</i></p>
 */
static const int DISPOSAL_NONE = 1;
/**
 * GIF Disposal Method meaning clear canvas to background color.
 * <p><b>GIF89a</b>: <i>Restore to background color.
 * The area used by the graphic must be restored to the background color.</i></p>
 */
static const int DISPOSAL_BACKGROUND = 2;
/**
 * GIF Disposal Method meaning clear canvas to frame before last.
 * <p><b>GIF89a</b>: <i>Restore to previous.
 * The decoder is required to restore the area overwritten by the graphic
 * with what was there prior to rendering the graphic.</i></p>
 */
static const int DISPOSAL_PREVIOUS = 3;

/**
    * The minimum frame delay in hundredths of a second.
    */
static const int MIN_FRAME_DELAY = 2;
/**
 * The default frame delay in hundredths of a second.
 * This is used for GIFs with frame delays less than the minimum.
 */
static const int DEFAULT_FRAME_DELAY = 10;

/**
 * The data block. We use this class record the start offset of the block and end of the block.
 */
class GifDataBlock {
public:
    uint64_t start;
    uint64_t end;
};

/**
 * The GIF image frame. Include all the information of the image.
 * The image dimension and location and animation delay time.
 */
class GifFrame {
public:
    uint16_t index;
    /**
     * The frame data block.
     */
    GifDataBlock* imageDescriptor;
    /**
     * The image location.
     */
    uint16_t left, top;
    /**
     * The image dimension.
     */
    uint16_t imageWidth,imageHeight;
    /**
     * Control Flag.
     */
    bool interlace;
    /**
     * Control Flag.
     */
    bool transparency;
    /**
     * Disposal Method.
     */
    uint8_t disposalMethod;
    /**
     * Transparency Index.
     */
    uint8_t transparentIndex;
    /**
     * Delay, in milliseconds, to next frame.
     */
    uint16_t delay;
    /**
     * Local color table size.
     */
    uint32_t localColorTableSize;
    /**
     * Local Color Table.
     */
    uint32_t* localColorTable;

    GifFrame();
    GifFrame(const GifFrame& o);
    ~GifFrame();
};

/**
 * The GIF header decoder. We only decode some common information except the image block.
 * After this you could be able to use a GIF decoder to decode each image.
 */
class GifHeaderDecoder {
public:
    char* version;
    uint16_t width;
    uint16_t height;

    uint16_t loopCount;

    uint8_t backgroundColorIndex;
    uint8_t backgroundColor;

    uint32_t* globalColorTable;
    uint32_t globalColorTableSize;

    uint16_t frameSize;
    GifFrame** frameArray;

    GifHeaderDecoder();
    ~GifHeaderDecoder();

    /**
     * Load a Gif file.
     * @param reader
     */
    bool loadImage(BufferReader &reader);

    /**
     * The first step determine the version of the file.
     * @param is
     * @return the pointer of the string version.
     */
    bool readHeader(BufferReader &reader);
    /**
     * The second step read the logical screen descriptor.
     * @param reader
     * @param decoder
     */
    void readLogicalScreenDescriptor(BufferReader &reader);

    /**
     * The color table by the given color table size.
     * @param reader
     * @param colorTableSize
     */
    uint32_t* readColorTable(BufferReader &reader,uint32_t& colorTableSize);

    /**
     * 20. Image Descriptor.
     * localed in spec-gif89a
     * @param reader
     * @param decoder
     */
    void readImageDescriptor(BufferReader &reader,GifFrame* frame);

    /**
     * 22. Table Based Image Data.
     * You should refer to this article: http://www.matthewflickinger.com/lab/whatsinagif/lzw_image_data.asp
     * @param reader
     * @param decoder
     */
    void skipTableBasedImageData(BufferReader &reader);

    /**
     * Read plain text extension.
     * @param reader
     * @param decoder
     */
    void readPlainTextExtension(BufferReader &reader);

    /**
     * Read application Extension.
     * @param reader
     * @param decoder
     */
    void readApplicationExtension(BufferReader &reader);

    /**
     * Skip sub-block.
     * When We read the base image block we could skip it.
     */
    void skipDataBlock(BufferReader &reader);

    /**
     * 23. Graphic Control Extension.
     * @param reader
     * @param decoder
     */
    void readGraphicControlExtension(BufferReader &reader,GifFrame* frame);

    /**
     * Start to read extension. Always start by the byte 0x21
     * @param reader
     * @param decoder
     */
    void readExtension(BufferReader &reader,GifFrame* frame);

    /**
     * Start to read the content after read the logical screen descriptor.
     * @param reader
     * @param decoder
     */
    void readContent(BufferReader &reader);
    /**
     * Return the frame by the given index.
     * @param frameIndex
     * @return
     */
    GifFrame* getFrame(uint16_t frameIndex);

};

#endif //GIFSAMPLE_GIFHEADERDECODER_H
