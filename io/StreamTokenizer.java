//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.io;

import java.io.Reader;
import java.io.PushbackReader;
import java.io.IOException;
import java.io.EOFException;

import hlt.language.tools.Misc;
import hlt.language.tools.Debug;

/**
 * This provides the exact same interface as that of the standard Java
 * Core API's <tt>java.io.StreamTokenizer</tt> but with a simpler (and
 * IMHO sounder) implementation, as well as a more complete and more general
 * set of methods (see <tt>AbstractStreamTokenizer</tt>).
 *
 * @see         AbstractStreamTokenizer
 * @see         LAStreamTokenizer
 * @version     Last modified on Fri Mar 11 12:34:38 2016 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class StreamTokenizer extends AbstractStreamTokenizer
{
  /**
   * Creates a <tt>StreamTokenizer</tt> for the specified <tt>Reader</tt>
   * and sets the default syntax.
   */
  public StreamTokenizer (Reader rd)
    {
      initializeCharInfo();
      reader = rd;
      input = new PushbackReader(rd);
      setDefaultSyntax();      
    }

  public final void enableComments ()
    {
      slashStarComments(true);
      slashSlashComments(true);
    }

  public final void disableComments ()
    {
      slashStarComments(false);
      slashSlashComments(false);
    }

  /**
   * Makes the specified character a comment start character exclusively.
   */
  public final void commentChar (int c)
    {
      if (0 <= c && c < charInfo.length)
        charInfo[c].type = COMMENT_TYPE;
    }

  /**
   * C++-style comments will be skipped iff the argument is <tt>true</tt>.
   */
  public final void slashSlashComments (boolean flag)
    {
      slashSlashComments = flag;
    }

  /**
   * C-style comments will be skipped iff the argument is <tt>true</tt>.
   */
  public final void slashStarComments (boolean flag)
    {
      slashStarComments = flag;
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
   * <p>
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
	  setEndLocation();
          return nextToken();
        case '/':
          if (followingChar == '/' && slashSlashComments)
            {
              skipLine();
              return nextToken();
            }
          if (followingChar == '*' && slashStarComments)
            {
              skipComment();
              return nextToken();
            }
        }
      
      switch (currentType())
        {
        case SPECIAL:	// This means that the current character is negative (see currentType ())
	  setEndLocation();
          return (ttype = TT_SPECIAL);
        case COMMENT:
          skipLine();
          return nextToken();
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
   * Checks whether this character's type is comment, among other types.
   */
  public final boolean isCommentChar (int c)
    {
      return (0 <= c && c < charInfo.length) && hasType(c,COMMENT_TYPE);
    }

  /**
    * Sets the default escape character.
    */
  public final void setEscapeChar (int c)
    {
      ESCAPE_CHAR = c;
    }

  /**
   * <a name="setTypePrecedence"></a>
   * Changes the precedence ordering among the character types
   * to the order of the specified parameters. For example, the
   * default ordering is set by:
   * <p>
   * <tt>setTypePrecedence(WORD,WHITESPACE,COMMENT,QUOTE,ORDINARY);</tt>
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
    (int first, int second, int third, int forth, int fifth) throws Exception
    {
      // This is a cheap and foolproof test to ensure that all values are legal.
      if ((first|second|third|forth|fifth) != (WORD|WHITESPACE|COMMENT|QUOTE|ORDINARY)
         || first+second+third+forth+fifth != WORD+WHITESPACE+COMMENT+QUOTE+ORDINARY)
        throw new Exception("Illegal type precedence");

      typePrecedence[0] = first;
      typePrecedence[1] = second;
      typePrecedence[2] = third;
      typePrecedence[3] = forth;
      typePrecedence[4] = fifth;
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
  // The following are private facilities...
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  private PushbackReader input;

  private boolean slashSlashComments;
  private boolean slashStarComments;

  private final void skipComment () throws IOException
    {
      do
	{
	  nextChar();
	  if (currentChar == IO.EOF)
	    throw new IOException
	      ("Premature end of file while reading a comment: (line,col) = "
	       + currentCharLocation());
	}
      while (currentChar != '*' || followingChar != '/');
      nextChar();
    }

  final void readFollowingChar () throws IOException
    {
      if (followingChar != TT_EOF)
        {
          followingChar = input.read();
          followingCharCol++;
        }

      // throw away possible ^M garbage inherited from MacroHard:
      if (followingChar == IO.CRT)
      	readFollowingChar();

      // if (Debug.flagIsOn())
      // 	System.out.println("Read followingChar = "+Misc.pform(followingChar));
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

      if (followingChar == '.')
        {
          int peek = input.read();
          input.unread(peek);
          if (peek != '.')
	    nextChar();
        }

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
          if (isInteger)
	    nextChar();
          return;
        case 'd': case 'D': case 'f': case 'F':
          if (isInteger)
	    isInteger = false;
	  nextChar();
        }
    }
}
