//
// Created by cz on 2020/9/2.
//
#include "BufferReader.h"

BufferReader::BufferReader() {
    this->fin = nullptr;
    this->buffer = nullptr;
    this->remaining = 0;
    this->position = 0;
    this->off = 0;
}

BufferReader::~BufferReader() {
    if(fin){
        fin->close();
        delete(fin);
    }
    delete[] buffer;
}

bool BufferReader::open(const char* filePath) {
    fin=new std::ifstream(filePath, std::ifstream::ate | std::ios::in | std::ios::binary);
    if(fin->is_open()){
        std::streampos fileLength=fin->tellg();
        fin -> seekg(0,std::ios::beg);
        if(fileLength<1024){
            bufferSize = 1024;
        } else if(fileLength< 4*1024){
            bufferSize = 2*1024;
        } else {
            bufferSize = 8*1024;
        }
        if(buffer){
            delete [] buffer;
        }
        buffer=new char[bufferSize];
        readBuffer();
        return true;
    }
    return false;
}

bool BufferReader::hasRemaining() {
    return fin && (!fin->eof() || (fin->eof() && remaining));
}

void BufferReader::seekTo(uint64_t pos) {
    if(fin){
        fin->clear();
        fin->seekg(pos);
        position = pos;
        readBuffer();
    }
}

void BufferReader::readBuffer() {
    if(!fin->eof()){
        fin->read(this->buffer, bufferSize);
        remaining=fin->gcount();
        off = 0;
    }
}

void BufferReader::read(uint16_t& dst) {
    read(reinterpret_cast<uint8_t &>(dst), 2);
}

void BufferReader::read(uint32_t &dst) {
    read(reinterpret_cast<uint8_t &>(dst), 4);
}

void BufferReader::read(uint64_t &dst) {
    read(reinterpret_cast<uint8_t &>(dst), 8);
}

void BufferReader::read(uint8_t &dst) {
    read(dst,1);
}

void BufferReader::read(uint8_t& dst, uint32_t size) {
    if(remaining <= size){
        if(0 < remaining){
            size-=remaining;
            memcpy(&dst, buffer + off, remaining);
            offset(remaining);
        }
        remaining = 0;
        readBuffer();
    }
    if(hasRemaining()){
        memcpy(&dst, buffer + off, size);
        offset(size);
    }
}

void BufferReader::read(char *dst, uint32_t size) {
    if(remaining-size <= 0){
        size-=remaining;
        memcpy(&dst, buffer + off, remaining);
        offset(remaining);
        readBuffer();
    }
    memcpy(dst, buffer + off, size);
    offset(size);
}

void BufferReader::offset(uint32_t size) {
    if(remaining >= size){
        off+=size;
        position+=size;
        remaining-=size;
    } else if(!fin->eof()){
        size-=remaining;
        position+=remaining;
        readBuffer();
        offset(size);
    } else if(0 < remaining){
        //Consume the rest.
        position+=remaining;
        remaining = 0;
    }
}

uint64_t BufferReader::getPosition() {
    return position;
}
