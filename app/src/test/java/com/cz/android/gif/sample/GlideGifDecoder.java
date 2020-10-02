package com.cz.android.gif.sample;

/*
 * Copyright (c) 2013 Xcellent Creations, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */



import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cz.android.gif.glide.GifDecoder;
import com.cz.android.gif.glide.GifFrame;
import com.cz.android.gif.glide.GifHeader;
import com.cz.android.gif.glide.GifHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import static com.cz.android.gif.glide.GifDecoder.STATUS_FORMAT_ERROR;
import static com.cz.android.gif.glide.GifDecoder.STATUS_OK;
import static com.cz.android.gif.glide.GifDecoder.STATUS_OPEN_ERROR;
import static com.cz.android.gif.glide.GifDecoder.STATUS_PARTIAL_DECODE;
import static com.cz.android.gif.glide.GifDecoder.TOTAL_ITERATION_COUNT_FOREVER;
import static com.cz.android.gif.glide.GifFrame.DISPOSAL_BACKGROUND;
import static com.cz.android.gif.glide.GifFrame.DISPOSAL_NONE;
import static com.cz.android.gif.glide.GifFrame.DISPOSAL_PREVIOUS;
import static com.cz.android.gif.glide.GifFrame.DISPOSAL_UNSPECIFIED;

/**
 * Reads frame data from a GIF image source and decodes it into individual frames for animation
 * purposes.  Image data can be read from either and InputStream source or a byte[].
 *
 * <p>This class is optimized for running animations with the frames, there are no methods to get
 * individual frame images, only to decode the next frame in the animation sequence.  Instead, it
 * lowers its memory footprint by only housing the minimum data necessary to decode the next frame
 * in the animation sequence.
 *
 * <p>The animation must be manually moved forward using {@link #advance()} before requesting the
 * next frame.  This method must also be called before you request the first frame or an error
 * will occur.
 *
 * <p>Implementation adapted from sample code published in Lyons. (2004). <em>Java for
 * Programmers</em>, republished under the MIT Open Source License
 *
 * @see <a href="https://www.w3.org/Graphics/GIF/spec-gif89a.txt">GIF 89a Specification</a>
 */
public class GlideGifDecoder {
  private static final String TAG = GlideGifDecoder.class.getSimpleName();

  /** Maximum pixel stack size for decoding LZW compressed data. */
  private static final int MAX_STACK_SIZE = 4 * 1024;

  private static final int NULL_CODE = -1;

  private static final int INITIAL_FRAME_POINTER = -1;

  private static final int BYTES_PER_INTEGER = Integer.SIZE / 8;

  private static final int MASK_INT_LOWEST_BYTE = 0x000000FF;

  @ColorInt
  private static final int COLOR_TRANSPARENT_BLACK = 0x00000000;

  // Global File Header values and parsing flags.
  /**
   * Active color table.
   * Maximum size is 256, see GifHeaderParser.readColorTable
   */
  @ColorInt
  private int[] act;
  /** Private color table that can be modified if needed. */
  @ColorInt
  private final int[] pct = new int[256];

  /** Raw GIF data from input source. */
  private ByteBuffer rawData;

  /** Raw data read working array. */
  private byte[] block;

  private GifHeaderParser parser;

  // LZW decoder working arrays.
  private short[] prefix;
  private byte[] suffix;
  private byte[] pixelStack;
  private byte[] mainPixels;

  private int[] priviousPixelArray;
  @ColorInt
  private int[] mainScratch;

  private int framePointer;
  private GifHeader header;
  private boolean savePrevious;
  @GifDecoder.GifDecodeStatus
  private int status;
  private int sampleSize;
  private int downsampledHeight;
  private int downsampledWidth;
  @Nullable
  private Boolean isFirstFrameTransparent;

  private TimeTrace timeTrace=new TimeTrace();

  public GlideGifDecoder() {
    header = new GifHeader();
  }

  public void trace(String tag){
    timeTrace.trace(tag);
  }

  public TimeTrace getTimeTrace() {
    return timeTrace;
  }

  public int getWidth() {
    return header.width;
  }

  public int getHeight() {
    return header.height;
  }

  @NonNull
  public ByteBuffer getData() {
    return rawData;
  }

  public int getStatus() {
    return status;
  }

  public void advance() {
    framePointer = (framePointer + 1) % header.frameCount;
  }

  public int getDelay(int n) {
    int delay = -1;
    if ((n >= 0) && (n < header.frameCount)) {
      delay = header.frames.get(n).delay;
    }
    return delay;
  }

  public int getNextDelay() {
    if (header.frameCount <= 0 || framePointer < 0) {
      return 0;
    }

    return getDelay(framePointer);
  }

  public int getFrameCount() {
    return header.frameCount;
  }

  public int getCurrentFrameIndex() {
    return framePointer;
  }

  public void resetFrameIndex() {
    framePointer = INITIAL_FRAME_POINTER;
  }

  @Deprecated
  public int getLoopCount() {
    if (header.loopCount == GifHeader.NETSCAPE_LOOP_COUNT_DOES_NOT_EXIST) {
      return 1;
    }
    return header.loopCount;
  }

  public int getNetscapeLoopCount() {
    return header.loopCount;
  }

  public int getTotalIterationCount() {
    if (header.loopCount == GifHeader.NETSCAPE_LOOP_COUNT_DOES_NOT_EXIST) {
      return 1;
    }
    if (header.loopCount == GifHeader.NETSCAPE_LOOP_COUNT_FOREVER) {
      return TOTAL_ITERATION_COUNT_FOREVER;
    }
    return header.loopCount + 1;
  }

  public int getByteSize() {
    return rawData.limit() + mainPixels.length + (mainScratch.length * BYTES_PER_INTEGER);
  }

  @Nullable
  public synchronized int[] getNextFrame() {
    if (header.frameCount <= 0 || framePointer < 0) {
      status = STATUS_FORMAT_ERROR;
    }
    if (status == STATUS_FORMAT_ERROR || status == STATUS_OPEN_ERROR) {
      return null;
    }
    status = STATUS_OK;

    if (block == null) {
      block = new byte[255];
    }

    GifFrame currentFrame = header.frames.get(framePointer);
    GifFrame previousFrame = null;
    int previousIndex = framePointer - 1;
    if (previousIndex >= 0) {
      previousFrame = header.frames.get(previousIndex);
    }

    // Set the appropriate color table.
    act = currentFrame.lct != null ? currentFrame.lct : header.gct;
    if (act == null) {
      // No color table defined.
      status = STATUS_FORMAT_ERROR;
      return null;
    }

    // Reset the transparent pixel in the color table
    if (currentFrame.transparency) {
      // Prepare local copy of color table ("pct = act"), see #1068
      System.arraycopy(act, 0, pct, 0, act.length);
      // Forget about act reference from shared header object, use copied version
      act = pct;
      // Set transparent color if specified.
      act[currentFrame.transIndex] = COLOR_TRANSPARENT_BLACK;

      if (currentFrame.dispose == DISPOSAL_BACKGROUND && framePointer == 0) {
        // TODO: We should check and see if all individual pixels are replaced. If they are, the
        // first frame isn't actually transparent. For now, it's simpler and safer to assume
        // drawing a transparent background means the GIF contains transparency.
        isFirstFrameTransparent = true;
      }
    }
    // Transfer pixel data to image.
    return setPixels(currentFrame, previousFrame);
  }

  public int read(@Nullable InputStream is, int contentLength) {
    if (is != null) {
      try {
        int capacity = (contentLength > 0) ? (contentLength + 4 * 1024) : 16 * 1024;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(capacity);
        int nRead;
        byte[] data = new byte[16 * 1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
          buffer.write(data, 0, nRead);
        }
        buffer.flush();

        read(buffer.toByteArray());
      } catch (IOException e) {
        System.err.println("Error reading data from stream");
      }
    } else {
      status = STATUS_OPEN_ERROR;
    }

    try {
      if (is != null) {
        is.close();
      }
    } catch (IOException e) {
      System.err.println("Error closing stream");
    }

    return status;
  }

  public void clear() {
    header = null;
    if (mainPixels != null) {
      mainPixels=null;
    }
    if (mainScratch != null) {
      mainScratch=null;
    }
    rawData = null;
    isFirstFrameTransparent = null;
    if (block != null) {
      block=null;
    }
  }

  public synchronized void setData(@NonNull GifHeader header, @NonNull byte[] data) {
    setData(header, ByteBuffer.wrap(data));
  }

  public synchronized void setData(@NonNull GifHeader header, @NonNull ByteBuffer buffer) {
    setData(header, buffer, 1);
  }

  public synchronized void setData(@NonNull GifHeader header, @NonNull ByteBuffer buffer,
                                   int sampleSize) {
    if (sampleSize <= 0) {
      throw new IllegalArgumentException("Sample size must be >=0, not: " + sampleSize);
    }
    // Make sure sample size is a power of 2.
    sampleSize = Integer.highestOneBit(sampleSize);
    this.status = STATUS_OK;
    this.header = header;
    framePointer = INITIAL_FRAME_POINTER;
    // Initialize the raw data buffer.
    rawData = buffer.asReadOnlyBuffer();
    rawData.position(0);
    rawData.order(ByteOrder.LITTLE_ENDIAN);

    // No point in specially saving an old frame if we're never going to use it.
    savePrevious = false;
    for (GifFrame frame : header.frames) {
      if (frame.dispose == DISPOSAL_PREVIOUS) {
        savePrevious = true;
        break;
      }
    }

    this.sampleSize = sampleSize;
    downsampledWidth = header.width / sampleSize;
    downsampledHeight = header.height / sampleSize;
    // Now that we know the size, init scratch arrays.
    // TODO Find a way to avoid this entirely or at least downsample it (either should be possible).
    mainPixels = new byte[header.width * header.height];
    mainScratch = new int[downsampledWidth * downsampledHeight];
  }

  @NonNull
  private GifHeaderParser getHeaderParser() {
    if (parser == null) {
      parser = new GifHeaderParser();
    }
    return parser;
  }

  @GifDecoder.GifDecodeStatus
  public synchronized int read(@Nullable byte[] data) {
    this.header = getHeaderParser().setData(data).parseHeader();
    if (data != null) {
      setData(header, data);
    }

    return status;
  }


  /**
   * Creates new frame image from current data (and previous frames as specified by their
   * disposition codes).
   */
  private int[] setPixels(GifFrame currentFrame,GifFrame previousFrame) {
    // Final location of blended pixels.
    final int[] dest = mainScratch;

    // clear all pixels when meet first frame and drop prev image from last loop
    if (previousFrame == null) {
      Arrays.fill(dest, COLOR_TRANSPARENT_BLACK);
    }
    // clear all pixels when dispose is 3 but previousImage is null.
    // When DISPOSAL_PREVIOUS and previousImage didn't be set, new frame should draw on
    // a empty image
    if (previousFrame != null && previousFrame.dispose == DISPOSAL_PREVIOUS) {
      Arrays.fill(dest, COLOR_TRANSPARENT_BLACK);
    }

    // fill in starting image contents based on last image's dispose code
    if (previousFrame != null && previousFrame.dispose > DISPOSAL_UNSPECIFIED) {
      // We don't need to do anything for DISPOSAL_NONE, if it has the correct pixels so will our
      // mainScratch and therefore so will our dest array.
      if (previousFrame.dispose == DISPOSAL_BACKGROUND) {
        // Start with a canvas filled with the background color
        @ColorInt int c = COLOR_TRANSPARENT_BLACK;
        if (!currentFrame.transparency) {
          c = header.bgColor;
          if (currentFrame.lct != null && header.bgIndex == currentFrame.transIndex) {
            c = COLOR_TRANSPARENT_BLACK;
          }
        }
        // The area used by the graphic must be restored to the background color.
        int downsampledIH = previousFrame.ih / sampleSize;
        int downsampledIY = previousFrame.iy / sampleSize;
        int downsampledIW = previousFrame.iw / sampleSize;
        int downsampledIX = previousFrame.ix / sampleSize;
        int topLeft = downsampledIY * downsampledWidth + downsampledIX;
        int bottomLeft = topLeft + downsampledIH * downsampledWidth;
        for (int left = topLeft; left < bottomLeft; left += downsampledWidth) {
          int right = left + downsampledIW;
          for (int pointer = left; pointer < right; pointer++) {
            dest[pointer] = c;
          }
        }
      } else if (previousFrame.dispose == DISPOSAL_PREVIOUS && priviousPixelArray != null) {
        // Start with the previous frame
        System.arraycopy(priviousPixelArray,0,dest,0,dest.length);
      }
    }
    int currentFrameIndex = getCurrentFrameIndex();
    timeTrace.trace("frame:"+currentFrameIndex+" initialize previous.");
    // Decode pixels for this frame into the global pixels[] scratch.
    decodeBitmapData(currentFrame);
    timeTrace.trace("frame:"+currentFrameIndex+" decode code table.");
    if (currentFrame.interlace || sampleSize != 1) {
      copyCopyIntoScratchRobust(currentFrame);
    } else {
      copyIntoScratchFast(currentFrame);
    }

    // Copy pixels into previous image
    if (savePrevious && (currentFrame.dispose == DISPOSAL_UNSPECIFIED
        || currentFrame.dispose == DISPOSAL_NONE)) {
//      if (priviousPixelArray == null) {
//        priviousPixelArray = getNextBitmap();
//      }
//      previousImage.setPixels(dest, 0, downsampledWidth, 0, 0, downsampledWidth,
//          downsampledHeight);
    }
    timeTrace.trace("frame:"+currentFrameIndex+" process pixels.");
    // Set pixels for current image.
    return dest;
  }

  private void copyIntoScratchFast(GifFrame currentFrame) {
    int[] dest = mainScratch;
    int left = currentFrame.ix;
    int top = currentFrame.iy;
    int imageWidth = currentFrame.iw;
    int imageHeight = currentFrame.ih;
    // Copy each source line to the appropriate place in the destination.
    boolean isFirstFrame = framePointer == 0;
    int width = this.downsampledWidth;
    byte[] mainPixels = this.mainPixels;
    int[] act = this.act;
    byte transparentColorIndex = -1;
    for (int i = imageHeight-1; i < imageHeight; i++) {
      int line = i + top;
      int k = line * width;
      // Start of line in dest.
      int dx = k + left;
      // End of dest line.
      int dlim = dx + imageWidth;
      if (k + width < dlim) {
        // Past dest edge.
        dlim = k + width;
      }
      // Start of line in source.
      int sx = i * imageWidth;

      while (dx < dlim) {
        byte byteCurrentColorIndex = mainPixels[sx];
        int currentColorIndex = ((int) byteCurrentColorIndex) & MASK_INT_LOWEST_BYTE;
        if (currentColorIndex != transparentColorIndex) {
          int color = act[currentColorIndex];
          if (color != COLOR_TRANSPARENT_BLACK) {
            dest[dx] = color;
          } else {
            transparentColorIndex = byteCurrentColorIndex;
          }
        }
        ++sx;
        ++dx;
      }
    }

    isFirstFrameTransparent =
        (isFirstFrameTransparent != null && isFirstFrameTransparent)
            || (isFirstFrameTransparent == null && isFirstFrame && transparentColorIndex != -1);
  }

  private void copyCopyIntoScratchRobust(GifFrame currentFrame) {
    int[] dest = mainScratch;
    int downsampledIH = currentFrame.ih / sampleSize;
    int downsampledIY = currentFrame.iy / sampleSize;
    int downsampledIW = currentFrame.iw / sampleSize;
    int downsampledIX = currentFrame.ix / sampleSize;
    // Copy each source line to the appropriate place in the destination.
    int pass = 1;
    int inc = 8;
    int iline = 0;
    boolean isFirstFrame = framePointer == 0;
    int sampleSize = this.sampleSize;
    int downsampledWidth = this.downsampledWidth;
    int downsampledHeight = this.downsampledHeight;
    byte[] mainPixels = this.mainPixels;
    int[] act = this.act;
    @Nullable
    Boolean isFirstFrameTransparent = this.isFirstFrameTransparent;
    for (int i = 0; i < downsampledIH; i++) {
      int line = i;
      if (currentFrame.interlace) {
        if (iline >= downsampledIH) {
          pass++;
          switch (pass) {
            case 2:
              iline = 4;
              break;
            case 3:
              iline = 2;
              inc = 4;
              break;
            case 4:
              iline = 1;
              inc = 2;
              break;
            default:
              break;
          }
        }
        line = iline;
        iline += inc;
      }
      line += downsampledIY;
      boolean isNotDownsampling = sampleSize == 1;
      if (line < downsampledHeight) {
        int k = line * downsampledWidth;
        // Start of line in dest.
        int dx = k + downsampledIX;
        // End of dest line.
        int dlim = dx + downsampledIW;
        if (k + downsampledWidth < dlim) {
          // Past dest edge.
          dlim = k + downsampledWidth;
        }
        // Start of line in source.
        int sx = i * sampleSize * currentFrame.iw;
        if (isNotDownsampling) {
          int averageColor;
          while (dx < dlim) {
            int currentColorIndex = ((int) mainPixels[sx]) & MASK_INT_LOWEST_BYTE;
            averageColor = act[currentColorIndex];
            if (averageColor != COLOR_TRANSPARENT_BLACK) {
              dest[dx] = averageColor;
            } else if (isFirstFrame && isFirstFrameTransparent == null) {
              isFirstFrameTransparent = true;
            }
            sx += sampleSize;
            dx++;
          }
        } else {
          int averageColor;
          int maxPositionInSource = sx + ((dlim - dx) * sampleSize);
          while (dx < dlim) {
            // Map color and insert in destination.
            // TODO: This is substantially slower (up to 50ms per frame) than just grabbing the
            // current color index above, even with a sample size of 1.
            averageColor = averageColorsNear(sx, maxPositionInSource, currentFrame.iw);
            if (averageColor != COLOR_TRANSPARENT_BLACK) {
              dest[dx] = averageColor;
            } else if (isFirstFrame && isFirstFrameTransparent == null) {
              isFirstFrameTransparent = true;
            }
            sx += sampleSize;
            dx++;
          }
        }
      }
    }

    if (this.isFirstFrameTransparent == null) {
      this.isFirstFrameTransparent = isFirstFrameTransparent == null
          ? false : isFirstFrameTransparent;
    }
  }

  @ColorInt
  private int averageColorsNear(int positionInMainPixels, int maxPositionInMainPixels,
      int currentFrameIw) {
    int alphaSum = 0;
    int redSum = 0;
    int greenSum = 0;
    int blueSum = 0;

    int totalAdded = 0;
    // Find the pixels in the current row.
    for (int i = positionInMainPixels;
         i < positionInMainPixels + sampleSize && i < mainPixels.length
             && i < maxPositionInMainPixels; i++) {
      int currentColorIndex = ((int) mainPixels[i]) & MASK_INT_LOWEST_BYTE;
      int currentColor = act[currentColorIndex];
      if (currentColor != 0) {
        alphaSum += currentColor >> 24 & MASK_INT_LOWEST_BYTE;
        redSum += currentColor >> 16 & MASK_INT_LOWEST_BYTE;
        greenSum += currentColor >> 8 & MASK_INT_LOWEST_BYTE;
        blueSum += currentColor & MASK_INT_LOWEST_BYTE;
        totalAdded++;
      }
    }
    // Find the pixels in the next row.
    for (int i = positionInMainPixels + currentFrameIw;
         i < positionInMainPixels + currentFrameIw + sampleSize && i < mainPixels.length
             && i < maxPositionInMainPixels; i++) {
      int currentColorIndex = ((int) mainPixels[i]) & MASK_INT_LOWEST_BYTE;
      int currentColor = act[currentColorIndex];
      if (currentColor != 0) {
        alphaSum += currentColor >> 24 & MASK_INT_LOWEST_BYTE;
        redSum += currentColor >> 16 & MASK_INT_LOWEST_BYTE;
        greenSum += currentColor >> 8 & MASK_INT_LOWEST_BYTE;
        blueSum += currentColor & MASK_INT_LOWEST_BYTE;
        totalAdded++;
      }
    }
    if (totalAdded == 0) {
      return COLOR_TRANSPARENT_BLACK;
    } else {
      return ((alphaSum / totalAdded) << 24)
          | ((redSum / totalAdded) << 16)
          | ((greenSum / totalAdded) << 8)
          | (blueSum / totalAdded);
    }
  }

  /**
   * Decodes LZW image data into pixel array. Adapted from John Cristy's BitmapMagick.
   */
  private byte[] decodeBitmapData(GifFrame frame) {
    if (frame != null) {
      // Jump to the frame start position.
      rawData.position(frame.bufferFrameStart);
    }

    int npix = (frame == null) ? header.width * header.height : frame.iw * frame.ih;
    int codeIndex, clear, codeMask, codeSize, endOfInformation, inCode, oldCode, bits, code, count,
        i, datum, dataSize, first, top, bi, pi;

    if (mainPixels == null || mainPixels.length < npix) {
      // Allocate new pixel array.
      mainPixels = new byte[npix];
    }
    byte[] mainPixels = this.mainPixels;
    if (prefix == null) {
      prefix = new short[MAX_STACK_SIZE];
    }
    short[] prefix = this.prefix;
    if (suffix == null) {
      suffix = new byte[MAX_STACK_SIZE];
    }
    byte[] suffix = this.suffix;
    if (pixelStack == null) {
      pixelStack = new byte[MAX_STACK_SIZE + 1];
    }
    byte[] pixelStack = this.pixelStack;

    // Initialize GIF data stream decoder.
    dataSize = readByte();
    clear = 1 << dataSize;
    endOfInformation = clear + 1;
    codeIndex = clear + 2;
    oldCode = NULL_CODE;
    codeSize = dataSize + 1;
    codeMask = (1 << codeSize) - 1;
    for (code = 0; code < clear; code++) {
      // XXX ArrayIndexOutOfBoundsException.
      prefix[code] = 0;
      suffix[code] = (byte) code;
    }
    byte[] block = this.block;
    // Decode GIF pixel stream.
    i = datum = bits = count = first = top = pi = bi = 0;
    while (i < npix) {
      // Read a new data block.
      if (count == 0) {
        count = readBlock();
        if (count <= 0) {
          status = STATUS_PARTIAL_DECODE;
          break;
        }
        bi = 0;
      }
      datum += (((int) block[bi]) & MASK_INT_LOWEST_BYTE) << bits;
      bits += 8;
      ++bi;
      --count;

      while (bits >= codeSize) {
        // Get the next code.
        code = datum & codeMask;
        datum >>= codeSize;
        bits -= codeSize;
        // Interpret the code.
        if (code == clear) {
          // Reset decoder.
//          System.out.println("codeSize2:"+codeSize+" pixelIndex:"+pi+" blockIndex:"+(rawData.position()-(block.length-bi)));
          codeSize = dataSize + 1;
          codeMask = (1 << codeSize) - 1;
          codeIndex = clear + 2;
          oldCode = NULL_CODE;
          continue;
        } else if (code == endOfInformation) {
          break;
        } else if (oldCode == NULL_CODE) {
          mainPixels[pi] = suffix[code];
          ++pi;
          ++i;
          oldCode = code;
          first = code;
          continue;
        }

        inCode = code;
        if (code >= codeIndex) {
          pixelStack[top] = (byte) first;
          ++top;
          code = oldCode;
        }

        while (code >= clear) {
          pixelStack[top] = suffix[code];
          ++top;
          code = prefix[code];
        }
        first = ((int) suffix[code]) & MASK_INT_LOWEST_BYTE;
        mainPixels[pi] = (byte) first;
        ++pi;
        ++i;

        while (top > 0) {
          // Pop a pixel off the pixel stack.
          mainPixels[pi] = pixelStack[--top];
          ++pi;
          ++i;
        }

        // Add a new string to the string table.
        if (codeIndex < MAX_STACK_SIZE) {
          prefix[codeIndex] = (short) oldCode;
          suffix[codeIndex] = (byte) first;
          ++codeIndex;
          if (((codeIndex & codeMask) == 0) && (codeIndex < MAX_STACK_SIZE)) {
            ++codeSize;
            codeMask += codeIndex;
          }
        }
        oldCode = inCode;
      }
    }
    // Clear missing pixels.
    Arrays.fill(mainPixels, pi, npix, (byte) COLOR_TRANSPARENT_BLACK);
    return mainPixels;
  }

  /**
   * Reads a single byte from the input stream.
   */
  private int readByte() {
    return rawData.get() & MASK_INT_LOWEST_BYTE;
  }

  /**
   * Reads next variable length block from input.
   *
   * @return number of bytes stored in "buffer".
   */
  private int readBlock() {
    int blockSize = readByte();
    if (blockSize <= 0) {
      return blockSize;
    }
    rawData.get(block, 0, Math.min(blockSize, rawData.remaining()));
    return blockSize;
  }
}
