//
// Created by cz on 2020/9/2.
//

#include <cstring>
#include "GifHeaderDecoder.h"

GifFrame::GifFrame() {
    index = 0;
    left = top = imageWidth = imageHeight = 0;
    interlace =false;
    transparency = false;
    disposalMethod = DISPOSAL_NONE;
    transparentIndex = 0;
    delay = 0;
    localColorTableSize = 0;
    localColorTable = nullptr;
    imageDescriptor = nullptr;
}

GifFrame::GifFrame(const GifFrame& o){
    index = o.index;
    left = o.left;
    top = o.top;
    imageWidth = o.imageWidth;
    imageHeight = o.imageHeight;
    interlace = o.interlace;
    transparency = o.transparency;
    disposalMethod = o.disposalMethod;
    transparentIndex = o.transparentIndex;
    delay = o.delay;

    localColorTableSize = o.localColorTableSize;
    localColorTable = o.localColorTable;
    imageDescriptor = o.imageDescriptor;
}

GifFrame::~GifFrame() {
    delete imageDescriptor;
    delete [] localColorTable;
}

bool GifHeaderDecoder::loadImage(BufferReader &reader) {
    //Step1: determine whether this is a gif file.
    if(readHeader(reader)){
        //Step2: The logical screen descriptor.
        readLogicalScreenDescriptor(reader);
        //Step3: Start read content.
        readContent(reader);
        return true;
    }
    return false;
}

GifHeaderDecoder::GifHeaderDecoder() {
    version= nullptr;
    width=0;
    height=0;

    globalColorTableSize=0;
    globalColorTable= nullptr;

    backgroundColor = 0;
    backgroundColorIndex=0;

    frameSize =0;
    frameArray = nullptr;
}

GifHeaderDecoder::~GifHeaderDecoder() {
    delete[] version;
    delete [] frameArray;
    delete [] globalColorTable;
}

/**
 * The first step determine the version of the file.
 * @param is
 * @return the pointer of the string version.
 */
bool GifHeaderDecoder::readHeader(BufferReader &reader) {
//    printf("ReadHead=============================\n");
    char* head=new char[3];
    reader.read(head,3);
//    printf("read:%d\n",(strncmp("GIF", head,3)));
    if(0==strncmp("GIF", head,3)){
        reader.read(head,3);
        if(0==strncmp("87a", head,3) || 0==strncmp("89a", head,3)){
            this->version = head;
            return true;
        }
    }
    delete [] head;
    return false;
}

/**
 * Read the logical screen descriptor of the GIF file, After read the head of the File.
 * @param reader
 * @param decoder
 */
void GifHeaderDecoder::readLogicalScreenDescriptor(BufferReader &reader) {
//    printf("ReadLogicalScreenDescriptor=============================\n");
//    i) Logical Screen Width - Width, in pixels, of the Logical Screen
//    where the images will be rendered in the displaying device.
    reader.read(this->width);
//    ii) Logical Screen Height - Height, in pixels, of the Logical
//    Screen where the images will be rendered in the displaying device.
    reader.read(this->height);
//    printf("\twidth:%d height:%d\n",this->width,this->height);

//<Packed Fields>  =      Global Color Table Flag       1 Bit
//                        Color Resolution              3 Bits
//                        Sort Flag                     1 Bit
//                        Size of Global Color Table    3 Bits
    uint8_t packed;
    reader.read(packed);
    //11111111
    uint8_t globalColorTableFlag = (packed>>7) & 0x01;
    uint8_t colorResolution  = (packed>>4)&0x07;
    //turn 11110111 to 11110 by move to left 3 bit.
    uint8_t sortFlag  = (packed>>3) & 0x01;
    //The hex 0xf8 equal to binary value:11111000
    uint8_t colorTableSize=packed & 0x07;

    // To determine that actual size of the color table, raise 2 to [the value of the field + 1].
    //Here are different calculation about this expression.
    /*
     * 1. gctSize = 1 << ((packed & 0x07) + 1);
     * 2. gctSize = 2 << (packed & 7); // 6-8 :
     * 3. gctSize = Math.pow(2,(packed & 7)+1);
     */
    this->globalColorTableSize = 1 << (colorTableSize + 1);
//    printf("\tpacked:%d globalColorTableFlag:%d colorResolution:%d sortFlag:%d globalColorTableSize:%d GifLocalColorTableSize:%d\n",
//           packed, globalColorTableFlag, colorResolution, sortFlag, globalColorTableSize,colorTableSize);

//    If the globalColorTableFlag is set to 0, It should be ignored.
    reader.read(this->backgroundColorIndex);

//    If the value of the field is not 0, this approximation of the aspect ratio
//    is computed based on the formula:
//    Aspect Ratio = (Pixel Aspect Ratio + 15) / 64

    uint8_t aspectRatioValue;
    reader.read(aspectRatioValue);
    float aspectRatio=(aspectRatioValue+15)/64;
//    printf("\taspectRatioValue:%d,Aspect ratio:%f\n",aspectRatioValue,aspectRatio);

    // Read color table
    if(1 == globalColorTableFlag){
        //Collect all the global color table.
        this->globalColorTable=readColorTable(reader,this->globalColorTableSize);
        this->backgroundColor=globalColorTable[backgroundColorIndex];
    }
}

/**
 * The color table by the given color table size.
 * @param reader
 * @param colorTableSize
 */
uint32_t* GifHeaderDecoder::readColorTable(BufferReader &reader, uint32_t &colorTableSize) {
//    printf("ReadColorTable=============================\n");
    //Du to some weird problems. Like the transparent index was 255...
    //Here we are not going to just use the color table size.
    uint32_t* colorTable=new uint32_t[256];
    uint8_t red;
    uint8_t green;
    uint8_t blue;
    for(auto i=0;i<colorTableSize;i++){
        //The order the color is not ARGB, It is RGBA reversedly.
        reader.read(red);
        reader.read(green);
        reader.read(blue);
        uint32_t& color=colorTable[i];
        color=0xFF000000;
        color|=blue<<16;
        color|=green<<8;
        color|=red;
    }
    return colorTable;
}

/**
 * 20. Image Descriptor.
 * localed in spec-gif89a
 * @param reader
 * @param decoder
 */
void GifHeaderDecoder::readImageDescriptor(BufferReader &reader,GifFrame* frame) {
//    printf("ReadImageDescriptor=============================\n");
    reader.read(frame->left);
    reader.read(frame->top);
    reader.read(frame->imageWidth);
    reader.read(frame->imageHeight);

    uint8_t packed;
    reader.read(packed);
//<Packed Fields>  =
//                Local Color Table Flag        1 Bit
//                Interlace Flag                1 Bit
//                Sort Flag                     1 Bit
//                Reserved                      2 Bits
//                Size of Local Color Table     3 Bits

    //Local Color Table Flag
    uint8_t localColorTableFlag=packed>>7;
    //Interlace Flag the value 01000000 equal to the hex value: 0x40
    frame->interlace=(packed&0x40) >> 6;
    //Sort Flag the value 00100000 equal to the hex value: 0x40
    //uint8_t sortFlag=(packed&0x10)>>5;
    //Skip the reserved bits.
    //Size of Local Color Table 0x07 equal to the binary value:0000 0111
    if(localColorTableFlag){
        frame->localColorTableSize=1 << ((packed&0x07) + 1);
        frame->localColorTable=readColorTable(reader,frame->localColorTableSize);
    }
}

/**
 * 22. Table Based Image Data.
 * You should refer to this article: http://www.matthewflickinger.com/lab/whatsinagif/lzw_image_data.asp
 * @param reader
 */
void GifHeaderDecoder::skipTableBasedImageData(BufferReader &reader) {
//    printf("ReadTableBasedImageData=============================\n");
    //LZW Minimum Code Size
    reader.offset(1);
    skipDataBlock(reader);
}

/**
 * Read plain text extension.
 * @param reader
 * @param decoder
 */
void GifHeaderDecoder::readPlainTextExtension(BufferReader &reader){
    //Plain Text Label
//    printf("ReadPlainTextExtension=============================\n");
    //Block Size
    uint8_t plainTextBlockSize;
    reader.read(plainTextBlockSize);

    //The Text Grid Left Position
    uint16_t textGridLeftPosition;
    reader.read(textGridLeftPosition);
    //The Text Grid Top Position
    uint16_t textGridTopPosition;
    reader.read(textGridTopPosition);
//    printf("\tBlock Size:%d Grid Left Position:%d Grid Top Position:%d\n",plainTextBlockSize,textGridLeftPosition,textGridTopPosition);

    //Text Grid Width
    uint16_t gridWidth;
    reader.read(gridWidth);
    //Text Grid Height
    uint16_t gridHeight;
    reader.read(gridHeight);

    //Character Cell Width
    uint8_t characterCellWidth;
    reader.read(characterCellWidth);
    //Character Cell Height
    uint8_t characterCellHeight;
    reader.read(characterCellHeight);
//    printf("\tGrid Width:%d Grid Height:%d Character Cell Width:%d Character Cell Height:%d\n",
//           gridWidth,gridHeight,characterCellWidth,characterCellHeight);

    //Text Foreground Color Index
    uint8_t textForegroundColorIndex;
    reader.read(textForegroundColorIndex);
    //Text Background Color Index
    uint8_t textBackgroundColorIndex;
    reader.read(textBackgroundColorIndex);
//    printf("\tText Foreground Color Index:%d Text Background Color Index:%d\n",textForegroundColorIndex,textBackgroundColorIndex);
}

/**
 * Skip sub-block.
 * When We read the base image block we could skip it.
 */
void GifHeaderDecoder::skipDataBlock(BufferReader &reader){
    uint8_t blockSize;
    reader.read(blockSize);
    while(0 != blockSize){
        reader.offset(blockSize);
        reader.read(blockSize);
    }
}

/**
 * Read application Extension.
 * @param reader
 * @param decoder
 */
void GifHeaderDecoder::readApplicationExtension(BufferReader &reader){
//    printf("ReadApplicationExtension=============================\n");
    //Block Size                    1 Byte
    uint8_t extensionBlockSize;
    reader.read(extensionBlockSize);
//    printf("\tBlock Size:%d\n",extensionBlockSize);
    //Application Identifier        8 Bytes
    char* applicationIdentifier=new char[8];
    reader.read(applicationIdentifier,8);
//    printf("\tApplication Identifier:%s %d\n",applicationIdentifier,(strncmp("NETSCAPE",applicationIdentifier,8)));

    //Appl. Authentication Code     3 Bytes
    char* authenticationCode=new char[3];
    reader.read(authenticationCode,3);
//    printf("\tAuthentication Code:%s %d\n",authenticationCode,(strncmp("2.0",authenticationCode,3)));

    if(0==strncmp("NETSCAPE",applicationIdentifier,8)&&0==strncmp("2.0",authenticationCode,3)){
        //process Netscape2.0 extension
        reader.read(this->loopCount);
        reader.offset(1);
        //Application Data              Data Sub-blocks
//        printf("\tApplication Data Sub-blocks:%d\n",loopCount);
    } else {
        //Do nothing.
        this->skipDataBlock(reader);
        uint64_t position=reader.getPosition();
//        printf("\tskip:%d\n",position);
    }
    delete[] applicationIdentifier;
    delete[] authenticationCode;
}

/**
 * 23. Graphic Control Extension.
 * @param reader
 */
void GifHeaderDecoder::readGraphicControlExtension(BufferReader &reader,GifFrame* frame){
//    printf("ReadGraphicControlExtension=============================\n");
    //Block Size                    1 Byte
    uint8_t extensionBlockSize;
    reader.read(extensionBlockSize);
//    printf("\tBlock Size:%d\n",extensionBlockSize);

    //Packed field
    uint8_t packed;
    reader.read(packed);
    //Reserved                      3 Bits
    //byte reserved= (byte) (packed >> 5);
    //Disposal Method               3 Bits
    frame->disposalMethod = packed >> 2 & 0x3;
    //User Input Flag               1 Bit
    int userInputFlag = packed >> 1 & 0x1;
    //Transparent Color Flag        1 Bit
    frame->transparency = 1 == (packed & 0x1);

    //Delay Time
    uint16_t delayInHundredthsOfASecond;
    reader.read(delayInHundredthsOfASecond);
    if (delayInHundredthsOfASecond < MIN_FRAME_DELAY) {
        delayInHundredthsOfASecond = DEFAULT_FRAME_DELAY;
    }
    frame->delay = delayInHundredthsOfASecond*10;
//    printf("\tDelay Time:%d\n",frame->delay);

    //Transparent Color Index
    reader.read(frame->transparentIndex);
//    printf("\tTransparent Color Index:%d\n",frame->transparentIndex);

    //Skip the block terminator.
    reader.offset(1);
}

/**
 * Start to read extension. Always start by the byte 0x21
 * @param reader
 * @param decoder
 */
void GifHeaderDecoder::readExtension(BufferReader &reader,GifFrame* frame){
    uint8_t separator;
    reader.read(separator);
    switch (separator){
        case 0xF9:{
            // 23. Graphic Control Extension.
            //i) Graphic Control Label - Identifies the current block as a Graphic Control Extension. This field contains the fixed value 0xF9.
//            printf("Start read the Graphic Control Extension.\n");
            readGraphicControlExtension(reader,frame);
            break;
        }
        case 0xFE:{
            // 24. Comment Extension.
            //i) Comment Label - Identifies the block as a Comment Extension.This field contains the fixed value 0xFE.
//            printf("Start read the Comment Extension.\n");
            skipDataBlock(reader);
            break;
        }
        case 0x01:{
            // 25. Plain Text Extension.
            //i) Plain Text Label - Identifies the current block as a Plain Text Extension. This field contains the fixed value 0x01.
//            printf("Start read the Plain Text Extension.\n");
            readPlainTextExtension(reader);
            break;
        }
        case 0xFF:{
            // 26. Application Extension Label.
            //ii) Application Extension Label - Identifies the block as an Application Extension. This field contains the fixed value 0xFF.
//            printf("Start read the Application Extension Label.\n");
            readApplicationExtension(reader);
            break;
        }
    }
}

/**
 * Start to read the content after read the logical screen descriptor.
 * @param reader
 * @param decoder
 */
void GifHeaderDecoder::readContent(BufferReader &reader){
    uint8_t separator;
    uint16_t frameIndex;
    std::vector<GifFrame*> frameList;
    GifFrame* frame=new GifFrame();
    while(true) {
        reader.read(separator);
        switch (separator) {
            case 0x2C: {
                //i) Image Separator - Identifies the beginning of an Image Descriptor. This field contains the fixed value 0x2C.
//                printf("Start read the Image Separator.\n");
                frame->index=frameIndex++;
                frame->imageDescriptor=new GifDataBlock();
                readImageDescriptor(reader, frame);
                frame->imageDescriptor->start=reader.getPosition();
                skipTableBasedImageData(reader);
                frame->imageDescriptor->end=reader.getPosition();
                frameList.push_back(frame);
                frame = new GifFrame();
                break;
            }
            case 0x21: {
//                printf("Start read the Extension.\n");
                readExtension(reader, frame);
                break;
            }
            case 0x3B: {
                // 27. Trailer.
                //i)a. Description. This block is a single-field block indicating the end of the GIF Data Stream.  It contains the fixed value 0x3B.
                frameSize = frameList.size();
                frameArray = new GifFrame*[frameSize];
                for(int i=0;i<frameList.size();i++){
                    frameArray[i]=frameList[i];
                }
//                printf("The Trailer. this is the end of the file.\n");
                delete frame;
                return;
            }
            case 0: // bad byte, just keep going and see what gonna happen;
                break;
            default:
//                printf("Unknown code:%d\n",separator);
                return;
        }
    }
}

/**
 * Return the frame by the given index.
 * @param frameIndex
 * @return
 */
GifFrame* GifHeaderDecoder::getFrame(uint16_t frameIndex){
    return *(frameArray+frameIndex);
}