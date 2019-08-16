//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.io;

/**
 * This is a central repository for constants used in various IO
 * classes. Using them saves repeated redefinitions and/or clashes.
 *
 * @version     Last modified on Wed Jun 20 13:56:22 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public interface IO
{
  /** End of file */
  public static final int EOF = -1;
  /** End of input */
  public static final int EOI = -2;
  /** Start of input */
  public static final int SOI = -3;
  /** Word */
  public static final int WRD = -4;
  /** Number */
  public static final int NUM = -5;
  /** Nothing */
  public static final int NTG = -6;
  /** Special */
  public static final int SPL = -7;

  /** Blank space */
  public static final char SPC = ' ';
  /** End of line */
  public static final char EOL = '\n';
  /** Tab */
  public static final char TAB = '\t';
  /** Carriage return */
  public static final char CRT = '\r';
  /** Backspace */
  public static final char BSP = '\b';
  /** Form feed */
  public static final char FFD = '\f';
  /** Backslash */
  public static final char BSL = '\\';
  /** Single quote */
  public static final char SQT = '\'';
  /** Double quote */
  public static final char DQT = '"';
  /** Back quote */
  public static final char BQT = '`';
  /** Beep */
  public static final char BIP = '\u0007';
}
