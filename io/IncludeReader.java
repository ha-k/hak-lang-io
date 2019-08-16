//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.io;

/**
 * @version     Last modified on Wed Apr 24 08:40:33 2013 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

import hlt.language.util.Stack;
import hlt.language.tools.Misc;
import hlt.language.tools.Debug;

import java.util.HashSet;
import java.io.Reader;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * This class implements a Reader that may include another Reader
 * in midstream reading, and eventually resume reading at that point
 * upon reaching EOF in the included Reader.
 *
 * <p>
 *
 * By default, included readers are read seamlessly. That is, when starting or
 * ending an inclusion, the next character is that which starts the included
 * reader or the next one in the enclosing reader. Inclusion seams may or not be
 * made visible with <tt>setSeamless(boolean)</tt>. Called with <tt>false</tt> on
 * an <tt>IncludeReader</tt> object, it will make its <tt>read()</tt> return
 * <tt>IO.SOI</tt> ('start of input') or <tt>IO.EOI</tt> ('end of input'),
 * both <tt>int</tt> constants, when starting or ending, respectively, reading an
 * inclusion. Calling it with <tt>true</tt> re/sets seamless reading.
 *
 * <p>
 *
 * <b>NB:</b> It is possible to prevent circular inclusions, although
 * not in all situations. Indeed there is no way, in general, for an
 * arbitary Reader to be identified as reading from a source being
 * actively included. However, this can be done for <tt>IncludeReader</tt>s
 * constructed from file readers by using the <tt>IncludeReader(String)</tt>
 * constructor and the <tt>include(String)</tt> method, which provide
 * identifiable file names. For file readers included using this
 * constructor and method, any circular inclusion will be detected
 * and cause a <tt>CircularInclusionException</tt> to be thrown.
 *
 * @see CircularInclusionException
 */

public class IncludeReader extends Reader
{
  /**
   * The current underlying reader.
   */
  private Reader _reader;

  /**
   * The name of the file associated to the current reader - or null
   * if the current reader is not reading from a file.
   */
  private String _file;

  /**
   * The current line number of the current reader.
   */
  private int _line = 1;

  /**
   * The current column number on the current line of the current reader.
   */
  private int _col = 0;

  /**
   * The stack recording the suspended readers.
   */
  private Stack _readerStack = new Stack();

  /**
   * The set of files currently being actively included.
   */
  private HashSet _dejaVu = new HashSet();

  /**
   * A flag indicating whether inclusions are seamless.
   */
  private boolean _isSeamless = true;

  /**
   * A flag indicating that nothing has been read yet from the
   * current reader.
   */
  private boolean _isInclusionStart = false;

  private boolean _reachedEndOfInclusion = false;

  public boolean reachedEndOfInclusion ()
    {
      return _reachedEndOfInclusion;
    }

  /**
   * The latest character read in the current reader.
   */
  private int _chr = IO.SOI;

  // HAK 2 HAK: the constructors should throw an exception if used with a
  // reader of type hlt.language.io.LAReader!!!

  /**
   * Constructs an <tt>IncludeReader</tt> with the specified <tt>Reader</tt>.
   */
  public IncludeReader (Reader reader)
    {
      _reader = reader;
    }

  /**
   * Constructs an <tt>IncludeReader</tt> with the specified file name.
   *
   * @throws    FileNotFoundException (f the specified file does not exist)
   */
  public IncludeReader (String file) throws FileNotFoundException
    {
      _reader = new BufferedReader(new FileReader(file));
      _file = file;
      _dejaVu.add(file);
    }

  /**
   * Sets or unsets seamless inclusion mode.
   */
  public final void setSeamless (boolean flag)
    {
      _isSeamless = flag;
    }

  /**
   * Returns <tt>true</tt> iff this IncludeReader is seamless.
   */
  public final boolean isSeamless ()
    {
      return _isSeamless;
    }

  /**
   * Returns the name of the file associated to the current reader -
   * or null if the current reader is not reading from a file.
   */
  public final String getFile ()
    {
      if (_file == null && _reader instanceof IncludeReader)
        return ((IncludeReader)_reader).getFile();

      return _file;
    }

  /**
   * Sets the name of the file associated to the current reader.
   */
  public final void setFile (String file)
    {
      _file = file;
    }

  /**
   * Returns latest character read in the current reader.
   */
  public final int latestChar ()
    {
      return _chr;
    }

  /**
   * Returns the current line number of the current reader.
   */
  public final int getLineNumber ()
    {
      return _line;
    }

  /**
   * Returns the current column number of the current reader.
   */
  public final int getColumnNumber ()
    {
      return _col;
    }

  /**
   * Closes all readers associated with this <tt>IncludeReader</tt>.
   *
   * @throws    IOException (if an I/O error occurs)
   */
  public final void close () throws IOException
    {
      _reader.close();

      while (!_readerStack.isEmpty())
        ((ReaderStackElement)_readerStack.pop()).reader().close();
    }

  /**
   * Returns the depth of inclusion of the current reader.
   */
  public final int depth ()
    {
      return _readerStack.size();
    }

  /**
   * Returns <tt>true</tt> iff this is reading from the outermost reader.
   */
  public final boolean isOutermost ()
    {
      return _readerStack.isEmpty();
    }

  /**
   * Reads and returns the next character in this <tt>IncludeReader</tt>.  When
   * not in seamless mode, returns <tt>IO.SOI</tt> (resp., <tt>IO.EOI</tt>) right
   * before starting (resp., after ending) reading an inclusion. Returns
   * <tt>IO.EOF</tt> only at the end of the outermost enclosing reader.
   *
   * @return    The character read (0 to 65535), IO.EOF, IO.EOI, or IO.SOI.
   * @throws    IOException (if an I/O error occurs)
   */
  public final int read () throws IOException
    {
      if (_isInclusionStart)
        {
          _isInclusionStart = false;
          _chr = IO.SOI;
        }
      else
        {
          _chr = _reader.read();

          while (_chr == IO.EOF)
            {

              if (_readerStack.isEmpty())
                {
                  _chr = IO.EOF;
		  _reachedEndOfInclusion = true;
		  if (Debug.flagIsOn())
		  	System.out.println("Reached the end of inclusion at depth "+depth());
                  break;
                }

              _reader.close();

              if (_file != null)
		_dejaVu.remove(_file);

              ReaderStackElement elt = (ReaderStackElement)_readerStack.pop();

              _reader = elt.reader();
              _file   = elt.file();
              _line   = elt.line();
              _col    = elt.col();

              _chr = IO.EOI;
            }
        }

      switch (_chr)
        {
        case IO.SOI:
          _line = 1;
          _col = 0;
          break;
        case IO.EOI:
        case IO.EOF:
          break;
        case IO.EOL:
          _line++;
          _col = 0;
          break;
        case IO.BSP:
          if (_col > 0) _col--;
          break;
        default:
          _col++;
        }

      if (_isSeamless && (_chr == IO.SOI || _chr == IO.EOI))
        return read();

      return _chr;
    }

  /**
   * Suspends reading from the current reader and makes further reads
   * proceed with the specified reader. Reading from the suspended reader
   * at the exact point it was left resumes automatically upon reaching
   * the end of the included reader.
   *
   */
  public final void include (Reader reader)
    {
      _readerStack.push(new ReaderStackElement(_reader,_line,_col));
      _reader = reader;
      _isInclusionStart = true;
    }

  /**
   * Suspends reading from the current reader and makes further reads
   * proceed with a reader from the specified file. Reading from the
   * suspended file reader at the exact point it was left resumes
   * automatically upon reaching the end of the included file.
   *
   * @throws    FileNotFoundException (if the specified file does not exist)
   * @throws    CircularInclusionException (if trying to open an enclosing Reader)
   */
  public final void include (String file) throws FileNotFoundException, CircularInclusionException //, BogusException
    {
      if (_dejaVu.contains(file))
        throw new CircularInclusionException(file);

      _readerStack.push(new ReaderStackElement(_reader,_file,_line,_col));
      _reader = new BufferedReader(new FileReader(file));
      _file = file;
      _dejaVu.add(file);
      _isInclusionStart = true;
    }

  // The following methods adapt the remaining methods of <tt>java.io.Reader</tt>
  // to work consistently for an <tt>IncludeReader</tt>.

  /**
   * Read characters into a portion of an array. Note that if not in seamless mode,
   * IO.EOI and IO.SOI will be read into the array like any other character.
   *
   * @param     cbuf    Destination buffer
   * @param     off     Offset at which to start storing characters
   * @param     len     Maximum number of characters to read
   * @return    number of characters read or IO.EOF
   * @throws    IOException (if an I/O error occurs)
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
   *
   * @param     cbuf    Destination buffer
   * @return    Number of characters read, or one of IO.EOF, IO.SOI, or IO.EOI.
   * @throws    IOException (if an I/O error occurs)
   */
  public int read (char[] cbuf) throws IOException
    {
      return read(cbuf,0,cbuf.length);
    }

  /**
   * Skip <tt>n</tt> characters or until IO.EOF is found. Note that
   * IO.SOI and IO.EOI are skipped but not counted, regardless of
   * whether the inclusion mode is seamless or not.
   *
   * @param     n       The number of characters to skip
   * @return    The number of characters actually skipped
   * @throws    IllegalArgumentException (if n is negative)
   * @throws    IOException (if an I/O error occurs)
   */
  public final long skip (long n) throws IOException
    {
      if (n < 0)
        throw new IllegalArgumentException
          ("Cannot skip negative number of chars: "+n);

      int count = 0;
      while (count < n)
        switch (read())
          {
          case IO.EOF:
            return count;
          case IO.EOI: case IO.SOI:
            continue;
          default:
            count++;
          }

      return count;
    }

  /**
   * This method is a simple delegation to the current underlying Reader.
   * Note that this way of handling it does not take into account other
   * Readers being suspended in the Reader stack. What is returned
   * depends only on the <i>current</i> underlying Reader.
   */
   public boolean ready () throws IOException
    {
      return _reader.ready();
    }

  /**
   * This method is a simple delegation to the current underlying Reader.
   * Note that this way of handling it does not take into account other
   * Readers being suspended in the Reader stack. What is returned
   * depends only on the <i>current</i> underlying Reader.
   */
  public boolean markSupported ()
    {
      return _reader.markSupported();
    }

  /**
   * This method is a simple delegation to the current underlying Reader.
   * Note that this way of handling it does not take into account other
   * Readers being suspended in the Reader stack. In particular, preserving
   * the mark cannot be done past the end of the <i>current</i> underlying Reader.
   */
  public void mark (int readAheadLimit) throws IOException
    {
      _reader.mark(readAheadLimit);
    }

  /**
   * This method is a simple delegation to the current underlying Reader.
   * Note that this way of handling it does not take into account other
   * Readers being suspended in the Reader stack. In particular, it will
   * not reset past the beginning of the current Reader.
   */
  public void reset () throws IOException
    {
      _reader.reset();
    }

  /**
   * Returns a string form describing the current state of the reader.
   */
  public final String toString ()
    {
      return "<File: "   + getFile() +
            ", Line: "   + _line +
            ", Column: " + _col  +
            ", Char: '"  + Misc.pform(_chr) +
            "'>";
    }

  // Ancillary class definition...

  /**
   * The data structure for elements pushed on the reader stack.
   * It consists simply of a quadruple <tt>&lt;reader,file,line,column&gt;</tt>.
   */
  private static class ReaderStackElement
    {
      private Reader _reader;
      private String _file;
      private int _line;
      private int _col;

      ReaderStackElement (Reader reader, int line, int col)
        {
          _reader = reader;
          _line = line;
          _col = col;
        }
      
      ReaderStackElement (Reader reader, String file, int line, int col)
        {
          _reader = reader;
          _file = file;
          _line = line;
          _col = col;
        }

      final Reader reader ()
        {
          return _reader;
        }

      final String file ()
        {
          return _file;
        }

      final int line ()
        {
          return _line;
        }

      final int col ()
        {
          return _col;
        }
    }
}
