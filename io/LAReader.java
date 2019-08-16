//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.io;

import java.io.Reader;
import java.io.IOException;
import hlt.language.tools.Misc;

/**
 * This class provides the convenience for doing lookaheads of arbitrary
 * length on a given Reader.
 *
 * @see         LAStreamTokenizer
 * @version     Last modified on Wed Jun 20 13:57:09 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class LAReader extends Reader
{
  /**
   * The underlying Reader.
   */
  private Reader reader;  

  /**
   * The buffer.
   */
  private int[] buffer;

  /**
   * The buffer's size.
   */
  private int BUFFER_SIZE = 20;

  /**
   * The buffer's start index.
   */
  private int start = 0;

  /**
   * The buffer's end.
   */
  private int end = 0;

  /**
   * A flag to indicate that the end of file has been read.
   */
  private boolean eofHasBeenRead = false;

  /**
   * Fills the buffer starting at position 0.
   */
  private final void fill () throws IOException
    {
      fill_from(0);
    }

  /**
   * Fills the buffer starting at the specified position until it is full, or
   * the end of file is read, or the reader becomes non ready.
   */
  private final void fill_from (int beginning) throws IOException
    {
      if (eofHasBeenRead) return;

      end = beginning;

      for (int i=beginning; i<BUFFER_SIZE; i++)
        {
          buffer[i] = reader.read();
          end++;
          if (buffer[i] == IO.EOF)
          {
            eofHasBeenRead = true;
            break;
          }
        }

      start = 0;
    }

  /**
   * Refills the buffer after shifting its contents down to
   * start at 0.
   */
  private final void refill () throws IOException
    {
      if (eofHasBeenRead) return;

      for (int i=0; i<end-start; i++)
        buffer[i] = buffer[start+i];

      fill_from(end-start);
    }

  /*************************************************************************/

  ////////////////
  // Public part :
  ////////////////

  /**
   * Constructs a LAReader with the specified Reader.
   */
  public LAReader (Reader reader)
    {
      this.reader = reader;
      buffer = new int[BUFFER_SIZE];
    }

  /**
   * Constructs a LAReader with the specified Reader
   * and buffer size.
   */
  public LAReader (Reader reader, int size)
    {
      this.reader = reader;
      buffer = new int[BUFFER_SIZE=size];
    }

  /**
   * Consumes and returns the next character in the LAReader.
   */
  public final int read () throws IOException
    {
      if (start == end) fill();
      if (buffer[start] != IO.EOF) return buffer[start++];
      return IO.EOF;    // end of file
    }

  /**
   * Returns the string of the <tt>n</tt> characters to be read next. If less
   * than <tt>n</tt> characters are available in the currentl buffer, it is
   * first refilled. If <tt>n</tt> is greater than <tt>BUFFER_SIZE</tt>, the
   * buffer is first resized to <tt>n</tt> and refilled. If the end of file is
   * read, or the reader becomes non ready before <tt>n</tt> characters are
   * consumed, only those characters that have been already read are used (and
   * the lookahead is then shorter than <tt>n</tt>). If the reader is not
   * ready or only the end of file remains, the empty string is returned.
   */
  public final String lookahead (int n) throws IOException
    {
      if (n <= 0) return "";

      if (n > BUFFER_SIZE)
        resizeBuffer(n);
      else
        if (n > end-start)
          refill();

      StringBuilder string = new StringBuilder(n);

      for (int i=start; i<start+n; i++)
        {
          if (i == end || buffer[i] == IO.EOF) break;
          string.append((char)buffer[i]);
        }

      return string.toString();
    }

  /**
   * Returns a lookahead string of length equal to the buffer's.
   */
  public final String lookahead () throws IOException
    {
      return lookahead(BUFFER_SIZE);
    }    

  /**
   * Resizes the lookahead buffer to the specified size.
   */
  public void resizeBuffer (int size) throws IOException
    {
      if (size < end-start || eofHasBeenRead) return;

      int[] newBuffer = new int[size];

      for (int i=0; i<end-start; i++)
        newBuffer[i] = buffer[start+i];

      buffer = newBuffer;
      BUFFER_SIZE = size;
      fill_from(end-start-1);
    }

  /**
   * Closes this LAReader.
   */
  public void close () throws IOException
    {
      reader.close();
    }

  /**
   * Returns a string describing the current buffer state.
   */
  public String toString ()
    {
      StringBuilder s = new StringBuilder();

      s.append("BUFFER_SIZE = ").append(BUFFER_SIZE);
      s.append("\nEOF read? = ").append(eofHasBeenRead);
      s.append("\nstart = ").append(start);
      s.append("\nend = ").append(end);
      for (int i=start; i<end; i++)
        {
          s.append("\nbuffer[").append(i).append("] = ");
          s.append("'").append(Misc.pform(buffer[i])).append("'");
          if (buffer[i] == IO.EOF) break;
        }
      return s.toString();
    }

  // The following methods adapt the remaining methods of
  // <tt>java.io.Reader</tt> to work consistently for an
  // <tt>LAReader</tt>.

  /**
   * Read characters into a portion of an array.
   * @param     cbuf    Destination buffer
   * @param     off     Offset at which to start storing characters
   * @param     len     Maximum number of characters to read
   * @return   Number of characters read, or IO.EOF.
   * @throws    IOException If an I/O error occurs
   */
  public int read (char[] cbuf, int off, int len) throws IOException
    {
      int c;
      int count = 0;

      while (off < cbuf.length && count < len)
        switch (c = read())
          {
          case IO.EOF:
            return c;
          default:
            cbuf[off] = (char)c;
            off++;
            count++;
          }

      return count;
    }

  /**
   * Read characters into an array.
   * @param     cbuf    Destination buffer
   * @return   Number of characters read, or one of IO.EOF, IO.SOI, or IO.EOI.
   * @throws    IOException If an I/O error occurs
   */
  public int read (char[] cbuf) throws IOException
    {
      return read(cbuf,0,cbuf.length);
    }

  /**
   * Skip <tt>n</tt> characters, up to EOF (or, more generally,
   * any negative value returned by <tt>read()</tt>).
   * @param     n       The number of characters to skip
   * @return    The number of characters actually skipped
   * @throws    IllegalArgumentException If n is negative
   * @throws    IOException If an I/O error occurs
   */
  public long skip (long n) throws IOException
    {
      if (n < 0)
        throw new IllegalArgumentException
          ("Cannot skip negative number of chars: "+n);

      int count = 0;

      while (count < n)
        if (read() < 0)
          return count;
        else
          count++;

      return count;
    }

  /**
   * Returns <tt>true</tt> iff there is a character available to
   * be read.
   */
   public boolean ready () throws IOException
    {
      return start < end || reader.ready();
    }

  /**
   * Returns <tt>false</tt> because it there is no simple way to
   * accommodate marking with the lookahead buffer.
   */
  public boolean markSupported ()
    {
      return false;
    }

  /**
   * This method does nothing because marking is not supported.
   */
  public void mark (int readAheadLimit) throws IOException
    {
    }

  /**
   * This method does nothing because marking is not supported.
   */
  public void reset () throws IOException
    {
    }
}



