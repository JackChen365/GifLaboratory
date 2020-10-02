package com.cz.android.gif.glide;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Inner model class housing metadata for each frame.
 *
 * @see <a href="https://www.w3.org/Graphics/GIF/spec-gif89a.txt">GIF 89a Specification</a>
 */
public class GifFrame {
  /**
   * GIF Disposal Method meaning take no action.
   * <p><b>GIF89a</b>: <i>No disposal specified.
   * The decoder is not required to take any action.</i></p>
   */
  public static final int DISPOSAL_UNSPECIFIED = 0;
  /**
   * GIF Disposal Method meaning leave canvas from previous frame.
   * <p><b>GIF89a</b>: <i>Do not dispose.
   * The graphic is to be left in place.</i></p>
   */
  public static final int DISPOSAL_NONE = 1;
  /**
   * GIF Disposal Method meaning clear canvas to background color.
   * <p><b>GIF89a</b>: <i>Restore to background color.
   * The area used by the graphic must be restored to the background color.</i></p>
   */
  public static final int DISPOSAL_BACKGROUND = 2;
  /**
   * GIF Disposal Method meaning clear canvas to frame before last.
   * <p><b>GIF89a</b>: <i>Restore to previous.
   * The decoder is required to restore the area overwritten by the graphic
   * with what was there prior to rendering the graphic.</i></p>
   */
  public static final int DISPOSAL_PREVIOUS = 3;

  /**
   * <p><b>GIF89a</b>:
   * <i>Indicates the way in which the graphic is to be treated after being displayed.</i></p>
   * Disposal methods 0-3 are defined, 4-7 are reserved for future use.
   *
   * @see #DISPOSAL_UNSPECIFIED
   * @see #DISPOSAL_NONE
   * @see #DISPOSAL_BACKGROUND
   * @see #DISPOSAL_PREVIOUS
   */
  @Retention(RetentionPolicy.SOURCE)
  @IntDef(value = {DISPOSAL_UNSPECIFIED, DISPOSAL_NONE, DISPOSAL_BACKGROUND, DISPOSAL_PREVIOUS})
  public @interface GifDisposalMethod {
  }

  public int ix, iy, iw, ih;
  /**
   * Control Flag.
   */
  public boolean interlace;
  /**
   * Control Flag.
   */
  public boolean transparency;
  /**
   * Disposal Method.
   */
  @GifDisposalMethod
  public int dispose;
  /**
   * Transparency Index.
   */
  public int transIndex;
  /**
   * Delay, in milliseconds, to next frame.
   */
  public int delay;
  /**
   * Index in the raw buffer where we need to start reading to decode.
   */
  public int bufferFrameStart;
  /**
   * Local Color Table.
   */
  @ColorInt
  public int[] lct;
}
