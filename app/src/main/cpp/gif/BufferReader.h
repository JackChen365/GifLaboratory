//
// Created by cz on 2020/9/2.
//
#ifndef GIFSAMPLE_BUFFERREADER_H
#define GIFSAMPLE_BUFFERREADER_H

#include <fstream>

class BufferReader {
private :
    char *buffer;
    std::ifstream *fin;
    uint64_t bufferSize;
    uint64_t remaining;
    uint64_t position;
    uint64_t off;
public:
    BufferReader();

    ~BufferReader();

    /**
     * Open the file.
     * @param filePath
     * @return
     */
    bool open(const char* filePath);

    /**
     * Fill the buffer and reset the position.
     */
    void readBuffer();

    void offset(uint32_t num);

    void seekTo(uint64_t pos);

    bool hasRemaining();

    void read(char* dst,uint32_t size);

    void read(uint8_t& dst, uint32_t size);

    void read(uint8_t& dst);

    void read(uint16_t& dst);

    void read(uint32_t& dst);

    void read(uint64_t& dst);

    uint64_t getPosition();
};

#endif //GIFSAMPLE_BUFFERREADER_H
