//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.io;

import java.io.Reader;
import java.io.IOException;
import java.io.EOFException;

/**
 * This provides the exact same interface as that of the standard Java
 * Core API's <tt>java.io.StreamTokenizer</tt> but with a simpler (and
 * IMHO sounder) implementation, as well as a more complete and more general
 * set of methods (see <tt>AbstractStreamTokenizer</tt>).  In addition,
 * <tt>io.LAStreamTokenizer</tt> provides a method for arbitrary
 * lookaheads:
 * <pre>
 * public final String lookahead (int n) throws IOException
 * </pre>
 * which returns the string of <tt>n</tt> characters in the input stream
 * including and starting with the current character.
 *
 * @see         AbstractStreamTokenizer
 * @see         StreamTokenizer
 * @version     Last modified on Fri Mar 11 12:29:21 2016 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class LAStreamTokenizer extends AbstractStreamTokenizer
{
  /**
   * Creates a <tt>StreamTokenizer</tt> for the specified <tt>Reader</tt>
   * and sets the default syntax.
   */
  public LAStreamTokenizer (Reader rd)
    {
      initializeCharInfo();
      reader = rd;
      input = new LAReader(rd);
      setDefaultSyntax();      
    }

   public final void enableComments ()
    {
      skipLineComments = true;
      skipBracketComments = true;
    }

   public final void disableComments ()
    {
      skipLineComments = false;
      skipBracketComments = false;
    }

  /**
   * Makes the specified character a comment start character exclusively
   * and enable skipping comment lines starting with this character.
   */
  public final void commentChar (int c)
    {
      if (0 <= c && c < charInfo.length)
        {
          commentLine(String.valueOf((char)c));
          skipLineComments = true;
        }
    }

  /**
   * C++-style comments will be skipped iff the argument is <tt>true</tt>.
   */
  public final void slashSlashComments (boolean flag)
    {
      if (flag) commentLine("//");
      skipLineComments = flag;
    }

  /**
   * C-style comments will be skipped iff the argument is <tt>true</tt>.
   */
  public final void slashStarComments (boolean flag)
    {
      if (flag) commentBrackets("/*","*/");
      skipBracketComments = flag;
    }

  /**
   * Reads a token and returns its type, which is also the value of <tt>ttype</tt>.
   * The possible token types are:
   * <dl>
   * <dt><tt>TT_NOTHING</tt>
   * <dd>if no token has been read yet;
   * <dt><tt>TT_EOF</tt>
   * <dd>if the end of file has been read;
   * <dt><tt>TT_EOL</tt>
   * <dd>if the end of line is significant and has been read;
   * <dt><tt>TT_WORD</tt>
   * <dd>if a word has been read;
   * <dt><tt>TT_NUMBER</tt>
   * <dd>if number parsing is effective and a number has been read;
   * <dt>a quote character
   * <dd>if a quoted word has been read;
   * <dt>a white space
   * <dd>if white spaces are significant and one has been read;
   * <dt>an ordinary character
   * <dd>otherwise.
   * </dl>
   * If the type is <tt>TT_WORD</tt> or a quote character, the value of the word
   * read is in <tt>sval</tt>. If the type is <tt>TT_NUMBER</tt>, the value
   * of the number is in <tt>nval</tt>. If the number read is an integer, the
   * flag <tt>isInteger</tt> is set to <tt>true</tt>.
   */
  public final int nextToken () throws IOException
    {
      if (pushedBack)
        {
          pushedBack = false;
          if (!(ttype == TT_EOL && !eolIsSignificant
                || isWhitespaceChar(ttype) && !spaceIsSignificant))
            return ttype;
        }

      sval = null;
      nval = Double.NaN;
      isInteger = false;

      nextChar();

      setStartLocation();

      switch (currentChar)
        {  
        case TT_EOF:
	  setEndLocation();
          return (ttype = TT_EOF);
        case TT_NOTHING:
          return nextToken();
        default:
          if (skipLineComments &&
              lookahead(COMMENT_LINE.length()).equals(COMMENT_LINE))
            {
              skipLine();
              return nextToken();
            }
          if (skipBracketComments &&
              lookahead(COMMENT_START.length()).equals(COMMENT_START))
            {
              skipComment();
              return nextToken();
            }
        }
      
      switch (currentType())
        {
        case SPECIAL:
	  setEndLocation();
          return (ttype = TT_SPECIAL);
        case WHITESPACE:
          if (currentChar == IO.EOL && eolIsSignificant)
	    {
	      setEndLocation();
	      return (ttype = TT_EOL);
	    }
          if (spaceIsSignificant)
	    {
	      setEndLocation();
	      return (ttype = currentChar);
	    }
          return nextToken();
        case ORDINARY:
	  setEndLocation();
          return (ttype = currentChar);
        case NUMERIC:
          readNumber();
          setEndLocation();
          return (ttype = TT_NUMBER);
        case QUOTE:
          leftQuote = currentChar;
          rightQuote = charInfo[leftQuote].right;
          readQuotedWord();
          setEndLocation();
          return (ttype = leftQuote);
        case WORD:
          readWord();
          setEndLocation();
          return (ttype = TT_WORD);
        }
      return ttype;
    }

  /**
   * Sets the default escape character.
   */
  public final void setEscapeChar (int c)
    {
      ESCAPE_CHAR = c;
    }

  /**
   * Enables bracketed comments with the specified start and end strings.
   */
  public final void commentBrackets (String left, String right)
    {
      COMMENT_START = left;
      COMMENT_END = right;
      skipBracketComments = true;
    }

  /**
   * Enables line comments with the specified start string.
   */
  public final void commentLine (String s)
    {
      COMMENT_LINE = s;
      skipLineComments = true;
    }

  /**
   * <a name="setTypePrecedence"></a>
   * Changes the precedence ordering among the character types
   * to the order of the specified parameters. For example, the
   * default ordering is set by:
   * <p>
   * <tt>setTypePrecedence(WORD,WHITESPACE,QUOTE,ORDINARY);</tt>
   * <p>
   * and this means that the <tt>WORD</tt> type is considered in
   * priority, then <tt>WHITESPACE</tt>, etc...
   * <p>
   * The <tt>NUMERIC</tt> type is not among those that can be ordered
   * because it is relevant only when numbers are parsed. If they are, then
   * the full Java number format is recognized independently of the character
   * syntax setting. This means that <tt>NUMERIC</tt> has always highest
   * precedence if number parsing is effective, and ignored otherwise.
   */
  public void setTypePrecedence
    (int first, int second, int third, int forth) throws Exception
    {
      // This is a cheap and foolproof test to ensure that all values are legal.
      if ((first|second|third|forth) != (WORD|WHITESPACE|QUOTE|ORDINARY)
         || first+second+third+forth != WORD+WHITESPACE+QUOTE+ORDINARY)
        throw new Exception("Illegal type precedence");

      typePrecedence[0] = first;
      typePrecedence[1] = second;
      typePrecedence[2] = third;
      typePrecedence[3] = forth;
    }

  /**
   * Returns the string of n characters to be processed next, including and
   * starting with the current character. 
   */
  public final String lookahead (int n) throws IOException
    {
      return String.valueOf((char)currentChar)+
             String.valueOf((char)followingChar)+
             input.lookahead(n-2);
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
  // The following are private facilities...
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  private LAReader input;

  private boolean skipLineComments;
  private boolean skipBracketComments;

  /**
    * The string marking the start of a bracketed comment.
    */
  private String COMMENT_START;

  /**
    * The string marking the end of a bracketed comment.
    */
  private String COMMENT_END;

  /**
    * The string marking the start of a line comment.
    */
  private String COMMENT_LINE;

  private final void skipComment () throws IOException
    {
      do
        {
          nextChar();
          if (currentChar == TT_EOF)
            throw new EOFException("Missing end of comment ("+COMMENT_END+"): "+
                                   location());
        }
      while (!lookahead(COMMENT_END.length()).equals(COMMENT_END));
      for (int i=1; i<COMMENT_END.length(); i++) nextChar();
    }

  final void readFollowingChar () throws IOException
    {
      if (followingChar != TT_EOF)
        {
          followingChar = input.read();
          followingCharCol++;
        }
      // throw away possible ^M garbage inherited from MacroHard:
      if (followingChar == IO.CRT) readFollowingChar();
    }

  final void readDecimal () throws IOException
    {
      boolean hasDigits = false;

      if (currentChar != '.')
        {
          hasDigits = true;
          readInteger();
        }

      if (parsingDigitsOnly)
	return;

      // At this point, followingChar is what follows the sequence of
      // digits just read in by readInteger(). It may be a trailing dot,
      // in which case the number may be a floating point number. But,
      // we must prevent an anomaly such as, e.g., "1.." to be parsed as
      // 1.0 followed by a dot but rather as 1 followed by two dots. So
      // we proceed with caution, reading a further character only if
      // the character to be read next is not a dot.

      if (followingChar == '.' && input.lookahead(1).charAt(0) != '.')
        nextChar();

      if (currentChar == '.')
        {
          isInteger = false;
          readMantissa(hasDigits);
        }

      if (followingChar == 'e' || followingChar == 'E')
        {
          isInteger = false;
          readExponent();
        }

      switch (followingChar)
        {
        case 'l': case 'L':
          if (isInteger) nextChar();
          return;
        case 'd': case 'D': case 'f': case 'F':
          if (!isInteger) nextChar();
        }
    }
}
