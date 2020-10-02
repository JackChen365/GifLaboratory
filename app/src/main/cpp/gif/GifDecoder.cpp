//
// Created by Jack Chen on 9/26/2020.
//
#include "GifDecoder.h"

GifDecoder::GifDecoder(){
    pixelsSize = 0;
    headerDecoder = new GifHeaderDecoder();
    reader = new BufferReader();
    pixels = nullptr;
    prefix = nullptr;
    suffix = nullptr;
    pixelStack = nullptr;
    currentPixels = nullptr;
    previousPixels = nullptr;
    hasDisposalMethod=false;
}

GifDecoder::~GifDecoder(){
    delete reader;
    delete headerDecoder;
    delete[] pixels;
    delete[] prefix;
    delete[] suffix;
    delete[] pixelStack;
    delete[] currentPixels;
    delete[] previousPixels;
}

bool GifDecoder::loadImage(char* filePath) {
    if(reader->open(filePath)){
        return headerDecoder->loadImage(*reader);
    }
    return false;
}

uint32_t * GifDecoder::decodeFrame(uint16_t& index) {
    GifFrame* currentFrame = headerDecoder->frameArray[index];
    uint32_t* colorTable;
    if(currentFrame->localColorTable){
        colorTable = currentFrame->localColorTable;
    } else {
        colorTable = headerDecoder->globalColorTable;
    }
    int oldTransparentColor = colorTable[currentFrame->transparentIndex];
    if(currentFrame->transparency){
        colorTable[currentFrame->transparentIndex]=0;
    }
    if(currentFrame->imageDescriptor){
        reader->seekTo(currentFrame->imageDescriptor->start);
    }
    GifFrame* previousFrame= nullptr;
    if(0 < index){
        previousFrame = headerDecoder->getFrame(index-1);
    }
    uint32_t* pixels = readBasedImageData(index,*reader, currentFrame,previousFrame, colorTable, currentFrame->disposalMethod);
    colorTable[currentFrame->transparentIndex]=oldTransparentColor;
    return pixels;
}

uint32_t * GifDecoder::readBasedImageData(uint16_t& frameIndex,BufferReader& reader, GifFrame* currentFrame, GifFrame* previousFrame,
                                          uint32_t* colorTable, uint8_t& disposalMethod) {
    uint16_t& width = headerDecoder->width;
    uint16_t& height = headerDecoder->height;
    uint32_t npix = width * height;
    if(!currentPixels){
        currentPixels =new uint32_t[npix];
    }
    uint32_t* previousImage = previousPixels;
    // clear all pixels when meet first frame and drop prev image from last loop
    if (!previousFrame) {
        previousImage = NULL;
        std::fill(currentPixels,currentPixels+npix,COLOR_TRANSPARENT_BLACK);
    }
    // clear all pixels when dispose is 3 but previousImage is null.
    // When DISPOSAL_PREVIOUS and previousImage didn't be set, new frame should draw on
    // a empty image
    if (previousFrame && previousFrame->disposalMethod == DISPOSAL_PREVIOUS && !previousImage) {
        std::fill(currentPixels,currentPixels+MAX_STACK_SIZE,COLOR_TRANSPARENT_BLACK);
    }
    // fill in starting image contents based on last image's dispose code
    if (previousFrame && disposalMethod > DISPOSAL_UNSPECIFIED) {
        // We don't need to do anything for DISPOSAL_NONE, if it has the correct pixels so will our
        // mainScratch and therefore so will our dest array.
        if (previousFrame->disposalMethod == DISPOSAL_BACKGROUND) {
            // Start with a canvas filled with the background color
            uint32_t backgroundColor = COLOR_TRANSPARENT_BLACK;
            if (!currentFrame->transparency) {
                uint8_t& backgroundColorIndex = headerDecoder->backgroundColorIndex;
                backgroundColor = headerDecoder->backgroundColor;
                if (currentFrame->localColorTable && backgroundColorIndex == currentFrame->transparentIndex) {
                    backgroundColor = COLOR_TRANSPARENT_BLACK;
                }
            }
            // The area used by the graphic must be restored to the background color.
            uint16_t& topPosition = previousFrame->top;
            uint16_t&  leftPosition = previousFrame->left;
            uint16_t&  imageWidth = previousFrame->imageWidth;
            uint16_t&  imageHeight = previousFrame->imageHeight;
            uint32_t  topLeft = topPosition * width + leftPosition;
            uint32_t  bottomLeft = topLeft + imageHeight * width;
            for (uint32_t left = topLeft; left < bottomLeft; left += width) {
                uint32_t right = left + imageWidth;
                for (int pointer = left; pointer < right; pointer++) {
                    currentPixels[pointer] = backgroundColor;
                }
            }
        } else if (previousFrame->disposalMethod == DISPOSAL_PREVIOUS && previousImage) {
            // Start with the previous frame
            currentPixels = previousImage;
        }
    }
    uint16_t& top=currentFrame->top;
    uint16_t& left=currentFrame->left;
    uint16_t& imageWidth = currentFrame->imageWidth;
    uint16_t& imageHeight = currentFrame->imageHeight;
    bool interlace = currentFrame->interlace;
    uint8_t* pixels = decodeCodeTable(frameIndex, imageWidth, imageHeight);
    // copy each source line to the appropriate place in the destination
    uint32_t pass = 1;
    uint32_t inc = 8;
    uint32_t iline = 0;
    uint8_t transparentColorIndex = -1;
    for (uint32_t i = 0; i < imageHeight; i++) {
        uint32_t line = i;
        if (interlace) {
            if (iline >= imageHeight) {
                pass++;
                switch (pass) {
                    case 2 :
                        iline = 4;
                        break;
                    case 3 :
                        iline = 2;
                        inc = 4;
                        break;
                    case 4 :
                        iline = 1;
                        inc = 2;
                }
            }
            line = iline;
            iline += inc;
        }
        line += top;
        if (line < height) {
            uint32_t k = line * width;
            uint32_t pixelIndex = k + left; // start of line in dest
            uint32_t dlim = pixelIndex + imageWidth; // end of dest line
            if ((k + width) < dlim) {
                dlim = k + width; // past dest edge
            }
            uint32_t sx = i * imageWidth; // start of line in source
            while (pixelIndex < dlim) {
                // map color and insert in destination
                uint8_t byteCurrentColorIndex = pixels[sx];
                uint32_t currentColorIndex = byteCurrentColorIndex & 0xff;
                if(currentColorIndex != transparentColorIndex){
                    uint32_t color = colorTable[currentColorIndex];
                    if(color != COLOR_TRANSPARENT_BLACK){
                        currentPixels[pixelIndex] = color;
                    } else {
                        transparentColorIndex = byteCurrentColorIndex;
                    }
                }
                pixelIndex++;
                sx++;
            }
        }
    }
    // fill in starting image contents based on last image's dispose code
//        Values :    0 -   No disposal specified. The decoder is
//        not required to take any action.
//        1 -   Do not dispose. The graphic is to be left
//        in place.
//        2 -   Restore to background color. The area used by the
//        graphic must be restored to the background color.
//        3 -   Restore to previous. The decoder is required to
//        restore the area overwritten by the graphic with
//        what was there prior to rendering the graphic.
//        4-7 -    To be defined.
    uint32_t* drawingBitmap= currentPixels;
    if(!drawingBitmap){
        drawingBitmap=new uint32_t[npix];
    }
    // Keep the drawing bitmap.
    if(hasDisposalMethod && (currentFrame->disposalMethod ==DISPOSAL_UNSPECIFIED|| currentFrame->disposalMethod == DISPOSAL_NONE)){
        //Save the current bitmap.
        previousPixels = drawingBitmap;
    }
    return drawingBitmap;
}

uint8_t* GifDecoder::decodeCodeTable(uint16_t& frameIndex, uint16_t &imageWidth, uint16_t &imageHeight) {
    uint16_t& width = getWidth();
    uint16_t& height = getHeight();
    uint32_t npix = !pixels ? width*height : imageWidth * imageHeight;
    if(!pixels||pixelsSize<npix){
        pixelsSize = npix;
        pixels = new uint8_t[npix]; // allocate new pixel array
    }
    if(!prefix){
        prefix = new uint16_t [MAX_STACK_SIZE];
    }
    if(!suffix){
        suffix = new uint8_t[MAX_STACK_SIZE];
    }
    if(!pixelStack){
        pixelStack = new uint8_t [MAX_STACK_SIZE + 1];
    }
    //LZW Minimum Code Size
    uint32_t index=0;
    uint8_t minLZWSize;
    reader->read(minLZWSize);
    uint16_t codeSize = minLZWSize+1;
    uint16_t clearCode = 1 << minLZWSize;
    uint16_t eof = clearCode+1;
    uint16_t codeMask = (1 << codeSize) - 1;
    for(uint16_t i=0;i<clearCode;i++){
        suffix[i] = (uint8_t)i;
    }
    //The code index is the index of the code table.
    uint16_t codeIndex = eof + 1;
    //Stack index is actually the stack size. We pop each element reversely
    uint8_t data;
    uint8_t blockSize;
    reader->read(blockSize);
    uint32_t stackIndex=0;
    uint32_t pixelIndex=0;
    uint32_t bits = 0;
    uint32_t oldCode = 0;
    uint32_t first = 0;
    uint32_t code = 0;
    uint32_t datum = 0;
    while(index < npix){
        //consume all the pixels in the stack.
        while(0 != stackIndex){
            uint8_t c = pixelStack[--stackIndex];
            pixels[pixelIndex] = c;
            pixelIndex++;
        }
        if(bits < codeSize){
            if(0 == blockSize){
                reader->read(blockSize);
                if(0 >= blockSize){
                    break;
                }
            }
            reader->read(data);
            datum |= data << bits;
            blockSize --;
            bits += 8;
            continue;
        }
        code  = datum & codeMask;
        datum >>= codeSize;
        bits -= codeSize;

        if(code > codeIndex || code == eof){
            break;
        } else if(code == clearCode){
            codeSize=minLZWSize+1;
            codeMask=(1 << codeSize) - 1;
            codeIndex = clearCode+2;
            //Here the old code would be the clear code.
            oldCode = code;
            continue;
        } else if (oldCode == clearCode) {
            //When we clear the code table. What should we do.
            *(pixelStack+stackIndex++) = suffix[code];
            oldCode = code;
            first = code;
            continue;
        }
        int curCode = code;
        if (code == codeIndex) {
            pixelStack[stackIndex++] = (uint8_t)first;
            code = oldCode;
        }
        while(code >= clearCode){
            pixelStack[stackIndex++] = suffix[code];
            code = prefix[code];
        }
        first = (suffix[code]) & 0xFF;
        pixelStack[stackIndex++] = (uint8_t)first;
        if (codeIndex < MAX_STACK_SIZE){
            prefix[codeIndex] = oldCode;
            suffix[codeIndex] = (uint8_t)first;
            codeIndex++;
            if((0 == (codeIndex & codeMask)) && (codeSize < 12)){
                codeSize++;
                codeMask = (1 << codeSize) - 1;
            }
        }
        oldCode = curCode;
    }
    while(0 != stackIndex){
        pixels[pixelIndex++] = pixelStack[--stackIndex];
    }
    std::fill(pixels+pixelIndex,pixels+npix,COLOR_TRANSPARENT_BLACK);
    return pixels;
}


/**
 * The version of the file. It usually GIF89a or GIF87a
 * @return
 */
char* GifDecoder::getVersion(){
    return headerDecoder->version;
}

/**
 * The frame total size of the file.
 * @return
 */
uint16_t GifDecoder::getFrameSize(){
    return headerDecoder->frameSize;
}

/**
 * The logical screen width.
 * @return
 */
uint16_t& GifDecoder::getWidth(){
    return headerDecoder->width;
}

/**
 * The logical screen width.
 * @return
 */
uint16_t& GifDecoder::getHeight(){
    return headerDecoder->height;
}

/**
 * The Global color table.
 * @return
 */
uint32_t* GifDecoder::getGlobalColorTable(){
    return headerDecoder->globalColorTable;
}

uint32_t GifDecoder::getGlobalColorTableSize() {
    return headerDecoder->globalColorTableSize;
}

/**
 * The loop count and if the value equal to zero. It means loop infinite.
 * @return
 */
uint16_t& GifDecoder::getLoopCount(){
    return headerDecoder->loopCount;
}

/**
 * The delay time for each frame.
 * @param index
 * @return
 */
uint16_t& GifDecoder::getDelayTime(int index){
    GifFrame* frame=headerDecoder->getFrame(index);
    return frame->delay;
}