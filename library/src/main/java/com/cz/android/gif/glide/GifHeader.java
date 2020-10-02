package com.cz.android.gif.glide;

import androidx.annotation.ColorInt;

import java.util.ArrayList;
import java.util.List;

/**
 * A header object containing the number of frames in an animated GIF image as well as basic
 * metadata like width and height that can be used to decode each individual frame of the GIF. Can
 * be shared by one or more {@link com.bumptech.glide.gifdecoder.GifDecoder}s to play the same
 * animated GIF in multiple views.
 *
 * @see <a href="https://www.w3.org/Graphics/GIF/spec-gif89a.txt">GIF 89a Specification</a>
 */
public class GifHeader {

  /** The "Netscape" loop count which means loop forever. */
  public static final int NETSCAPE_LOOP_COUNT_FOREVER = 0;
  /** Indicates that this header has no "Netscape" loop count. */
  public static final int NETSCAPE_LOOP_COUNT_DOES_NOT_EXIST = -1;

  @ColorInt
  public int[] gct = null;
  @GifDecoder.GifDecodeStatus
  public int status = GifDecoder.STATUS_OK;
  public int frameCount = 0;

  public GifFrame currentFrame;
  public final List<GifFrame> frames = new ArrayList<>();
  /** Logical screen size: Full image width. */
  public int width;
  /** Logical screen size: Full image height. */
  public int height;

  // 1 : global color table flag.
  public boolean gctFlag;
  /**
   * Size of Global Color Table.
   * The value is already computed to be a regular number, this field doesn't store the exponent.
   */
  public int gctSize;
  /** Background color index into the Global/Local color table. */
  public int bgIndex;
  /**
   * Pixel aspect ratio.
   * Factor used to compute an approximation of the aspect ratio of the pixel in the original image.
   */
  public int pixelAspect;
  @ColorInt
  public int bgColor;
  public int loopCount = NETSCAPE_LOOP_COUNT_DOES_NOT_EXIST;

  public int getHeight() {
    return height;
  }

  public int getWidth() {
    return width;
  }

  public int getNumFrames() {
    return frameCount;
  }

  /**
   * Global status code of GIF data parsing.
   */
  @GifDecoder.GifDecodeStatus
  public int getStatus() {
    return status;
  }
}
