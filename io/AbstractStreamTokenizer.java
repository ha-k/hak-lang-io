//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.io;

/**
 * @version     Last modified on Sat Jul 28 05:41:45 2018 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

import java.io.Reader;
import java.io.IOException;
import java.io.EOFException;

import hlt.language.util.Span;
import hlt.language.util.Location;

import hlt.language.tools.Misc;
// import hlt.language.tools.Debug;

/**
 *
 * There are two concrete subclasses of this abstract class:
 * <tt>hlt.language.io.StreamTokenizer</tt> and
 * <tt>hlt.language.io.LAStreamTokenizer</tt>.  They provide the exact
 * same interface as that of the standard Java Core API's
 * <tt>java.io.StreamTokenizer</tt> but with a simpler (and IMHO
 * sounder) implementation. One may use both subclasses instead of
 * <tt>java.io.StreamTokenizer</tt> without change.  However, they
 * support a more complete and more general set of methods.  In
 * addition, <tt>hlt.language.io.LAStreamTokenizer</tt> provides a
 * method for arbitrary lookaheads:
 *
 * <pre>
 * public final String lookahead (int n) throws IOException
 * </pre>
 * which returns the string of <tt>n</tt> characters in the input stream
 * including and starting with the current character.
 * <p>
 * The syntax configuration used as the default setting of the tokenizer
 * is such that:
 * <ul>
 * <li> the type precedence ordering is set to: <tt>WORD</tt>,
 *      <tt>WHITESPACE</tt>, <tt>COMMENT</tt>, <tt>QUOTE</tt>,
 *      and <tt>ORDINARY</tt>;
 * <li> control characters and space are <tt>WHITESPACE</tt> characters;
 * <li> letters and numerals are <tt>WORD</tt> characters;
 * <li> <tt>_</tt> and <tt>.</tt> are <tt>WORD</tt> characters;
 * <li> single quote and double quote are quote characters;
 * <li> end-of-line is not significant;
 * <li> white spaces are not significant;
 * <li> all other characters are ordinary;
 * <li> parsing numbers is effective;
 * <li> C-style and C++-style comments are recognized and skipped.
 * </ul>
 *
 * This default configuration is reinstated with <tt>setDefaultSyntax()</tt>.
 *
 * @see         StreamTokenizer
 * @see         LAStreamTokenizer
 */

public abstract class AbstractStreamTokenizer
{
  /**
   * Makes the specified character a comment start character exclusively.
   */
  abstract public void commentChar (int c);

  /**
   * C++-style comments will be skipped iff the argument is <tt>true</tt>.
   */
  abstract public void slashSlashComments (boolean flag);

  /**
   * C-style comments will be skipped iff the argument is <tt>true</tt>.
   */
  abstract public void slashStarComments (boolean flag);

  /**
   * Reads a token and returns its type, which is also the value of
   * <tt>ttype</tt>.
   */
  public int nextTokenType () throws IOException
  {
    return nextToken();
  }

  /**
   * Reads a token and returns its type, which is also the value of
   * <tt>ttype</tt>.
   */
  abstract public int nextToken () throws IOException;

  abstract public void enableComments ();

  abstract public void disableComments ();

  abstract void readFollowingChar () throws IOException;

  abstract void readDecimal () throws IOException;
 
  /* ======================================================================= */
  /**
   * The following are public constants denoting recognized <i>token</i>
   * types.
   */
  public final static byte ORDINARY   = 0;
  public final static byte WHITESPACE = 1;
  public final static byte QUOTE      = 2;
  public final static byte WORD       = 3;
  public final static byte NUMERIC    = 4;
  public final static byte COMMENT    = 5;
  public final static byte SPECIAL    = 6;

  /**
   * The following are protected constants denoting recognized
   * <i>character</i> types.  A character type indicates how to process
   * it as part (or whole) of a token of this type. Any character can
   * have no or several types. Its <i>prevailing</i> type is that
   * prescribed by the current type precedence ordering.  The values for
   * these <i>character</i>  types are not accidental but such that
   * <tt><i>T</i>_TYPE = 2<sup><i>T</i></sup></tt>, where <i><tt>T</tt></i>
   * is the token type.
   */
  protected final static byte EMPTY_TYPE      =  0;
  protected final static byte ORDINARY_TYPE   =  1;
  protected final static byte WHITESPACE_TYPE =  2;
  protected final static byte QUOTE_TYPE      =  4;
  protected final static byte WORD_TYPE       =  8;
  protected final static byte NUMERIC_TYPE    = 16;
  protected final static byte COMMENT_TYPE    = 32;
  protected final static byte SPECIAL_TYPE    = 64;

  /**
   * The escape character used by default in quoted words.
   */
  protected static int ESCAPE_CHAR = IO.BSL;

  /**
   * This contains the precedence ordering among the character types.
   * The lower the index, the higher the type takes precedence.
   * This ordering may be changed with <tt>setTypePrecedence(...)</tt>.
   */
  protected final static int[] typePrecedence
    = { WORD, WHITESPACE, COMMENT, QUOTE, ORDINARY, SPECIAL };


  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
  // The following provide the java.io.StreamTokenizer API.
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * These constants are return values of <tt>nextToken()</tt> as well as
   * <tt>ttype</tt>.
   */

  /**
   * This value indicates that the end of file has been read.
   */
  public final static int TT_EOF     = IO.EOF;

  /**
   * This value indicates that an end of line has been read. This happens iff
   * <tt>eolIsSignificant</tt> has been set to <tt>true</tt>.
   */
  public final static int TT_EOL     = IO.EOL;

  /**
   * This value indicates that a word has been read. The field
   * <tt>sval</tt> contains the string. All alphabetical letters are word
   * characters by default. So are the numerals '<tt>0</tt>' to
   * '<tt>9</tt>' and the characters '<tt>_</tt>', and '<tt>.</tt>'.
   */
  public final static int TT_WORD    = IO.WRD;

  /**
   * This value indicates that a number has been read. The field <tt>sval</tt>
   * contains the number as a <tt>double</tt>.
   * <p>
   * This happens whenever <tt>parseNumbers()</tt> (or <tt>parseNumbers(true)</tt>)
   * has been called (default) and <tt>parseNumbers(false)</tt> has not yet been
   * called. Note that the characters '<tt>0</tt>' to '<tt>9</tt>'
   * and the characters '<tt>-</tt>', '<tt>+</tt>', and
   * '<tt>.</tt>' are always considered numeric when parsing numbers is on.
   * <p>
   * The format of numbers recognized is that of Java, including octal
   * (<tt>0...</tt>) and hexadecimal (<tt>0x...</tt> or
   * <tt>0X...</tt>) integers, and complete floating point format. Type
   * letter suffixes (i.e., '<tt>l</tt>' or '<tt>L</tt>' for
   * integers, and '<tt>f</tt>', '<tt>F</tt>', '<tt>d</tt>',
   * '<tt>D</tt>' for floating point numbers, are also recognized, but
   * they are ignored since the value returned is always a <tt>double</tt>.
   */
  public final static int TT_NUMBER  = IO.NUM;

  /**
   * This value indicates that no token has been read yet.
   */
  public final static int TT_NOTHING = IO.NTG;
  
  /**
   * This value indicates that a special token has been returned.
   */
  public final static int TT_SPECIAL = IO.SPL;
  
  /**
   * This always contains the type of token just read. It is equal to:
   * <ul>
   * <li><tt>TT_NOTHING</tt>, if nothing has been read yet.
   * <li><tt>TT_EOF</tt>, if the end of file has been read.
   * <li><tt>TT_EOL</tt>, if the end of line is significant and has been read.
   * <li><tt>TT_NUMBER</tt>, if numbers are parsed and a number has been read.
   * <li><tt>TT_WORD</tt>, is a word has been read. Any character can be declared
         to be a word character with <tt>wordChar(...)</tt>.
   * <li>a quote character, if a quoted word has been read. The word value
   *     is then in <tt>sval</tt>. The single quote character and
   *     the double quote character are quote characters by default.
   *     Any character can be declared to be a quote character with <tt>quoteChar(...)</tt>.
   *     Distinct left/right quotes can also be defined with <tt>quotePair(...)</tt>.
   * <li>An ordinary character, otherwise. Any character can be declared to be
         an ordinary character with <tt>ordinaryChar(...)</tt>.
   * <li><tt>TT_SPECIAL</tt>, if nothing else above applies.
   * </ul>
   */
  public int ttype = TT_NOTHING;

  /**
   * This contains the token string when a word, or a quoted word, has been read.
   */
  public String sval;

  /**
   * This contains the token value whenever a number has been read.
   */
  public double nval;

  /**
   * This is set to true whenever an integer number has been read.
   */
  public boolean isInteger;

  /* ======================================================================= */

  /**
   * The underlying reader.
   */
  protected Reader reader;

  /**
   * The location of the character starting the current token.
   */
  protected Location tokenStart;
  /**
   * The location of the character starting the previous token.
   */
  protected Location previousTokenStart;
  /**
   * Returns the location of the start of the token at hand.
   */
  public Location tokenStart ()
    {
      if (pushedBack)
	return previousTokenStart;
      return tokenStart;
    }

  /**
   * The location of the character ending the current token.
   */
  protected Location tokenEnd;
  /**
   * The location of the character ending the previous token.
   */
  protected Location previousTokenEnd;
  /**
   * Returns the location of the end of the token at hand.
   */
  public Location tokenEnd ()
    {
      if (pushedBack)
	return previousTokenEnd;
      return tokenEnd;
    }

  protected Location currentCharLocation ()
    {
      return new Location(currentCharFile,currentCharLine,currentCharCol);
    }

  protected void setStartLocation ()
    {
      previousTokenStart = tokenStart;
      tokenStart = currentCharLocation();
    }

  protected void setEndLocation ()
    {
      previousTokenEnd = tokenEnd;
      tokenEnd = currentCharLocation();
    }

  protected void resetStartLocation ()
    {
      tokenStart.setFile(currentCharFile);
      tokenStart.setLine(currentCharLine);
      tokenStart.setColumn(currentCharCol);
    }

  protected void resetEndLocation ()
    {
      tokenEnd.setFile(currentCharFile);
      tokenEnd.setLine(currentCharLine);
      tokenEnd.setColumn(currentCharCol);
    }

  /**
   * Returns the current line number.
   */
  public final int getLineNumber ()
    {
      return currentCharLine;
    }

  /**
   * Returns the current line number. For compatibility
   * with <tt>java.io.StreamTokenizer</tt>.
   */
  public final int lineno ()
    {
      return getLineNumber();
    }

  /**
   * Returns the current file if any, or <tt>null</tt>.
   */
  public final String getFile ()
    {
      return currentCharFile;
    }

  public String location ()
    {
      String line = "line "+lineno();
      String file = getFile();
      if (file == null)
        return line;

      return "file "+file+", "+line;
    }

  /**
   * Makes all characters between the first and second arguments inclusive
   * ordinary exclusively.
   */
  public final void ordinaryChars (int from, int to)
    {
      resetCharRange(ORDINARY_TYPE, from, to);
    }

  /**
   * Makes all characters between the first and second arguments inclusive
   * word characters exclusively.
   */
  public final void wordChars (int from, int to)
    {
      resetCharRange(WORD_TYPE, from, to);
    }

  /**
   * Makes all characters between the first and second arguments inclusive
   * white space characters exclusively.
   */
  public final void whitespaceChars (int from, int to)
    {
      resetCharRange(WHITESPACE_TYPE, from, to);
    }

  /**
   * Makes the specified character a quote character exclusively.
   */
  public final void quoteChar (int c)
    {
      quotePair(c,c,ESCAPE_CHAR);
    }

  /**
   * Makes the specified character a quote character exclusively,
   * and specifies its escape character.
   */
  public final void quoteChar (int c, int e)
    {
      quotePair(c,c,e);
    }

  /**
   * Makes the specified character ordinary exclusively.
   */
  public final void ordinaryChar (int c)
    {
      if (0 <= c && c < charInfo.length)
        charInfo[c].type = ORDINARY_TYPE;
    }
  
  /* ======================================================================= */

  /**
   * The end-of-line is recognized as a token iff the argument is <tt>true</tt>.
   */
  public final void eolIsSignificant (boolean flag)
    {
      eolIsSignificant = flag;
    }

  /**
   * White spaces are recognized as tokens iff the argument is <tt>true</tt>.
   */
  public final void spaceIsSignificant (boolean flag)
    {
      spaceIsSignificant = flag;
    }

  /**
   * Specifies that numbers should be parsed.
   */
  public final void parseNumbers ()
    {
      parseNumbers(true);
    }
  
  /**
   * Specifies that octal and hexadecimal numbers should be parsed.
   */
  public final void parseNonDecimals ()
    {
      parseNonDecimals(true);
    }
  
  /**
   * Specifies that octal and hexadecimal numbers should not be parsed.
   */
  public final void ignoreNonDecimals ()
    {
      parseNonDecimals(false);
    }

  /**
   * Sets this tokenizer to parse numbers, but only whole numbers made
   * out of maximal sequences of numerical digits, considering as
   * non-numeric characters the <tt>'.'</tt>, <tt>'+'</tt>,
   * <tt>'-'</tt>, as well as letters (<i>i.e.</i>, <tt>'e'</tt>,
   * <tt>'E'</tt>, <tt>'f'</tt>, <tt>'F'</tt>, <tt>'l'</tt>,
   * <tt>'L'</tt>, <tt>'d'</tt>, or <tt>'D'</tt>))
   */
  public final void parseDigitsOnly ()
    {
      parseNumbers();
      parsingDigitsOnly = true;
      setType("+-.",ORDINARY);
    }

  public final boolean parsingDigitsOnly ()
    {
      return parsingDigitsOnly;
    }

  public final void parseNumbers (boolean flag)
    {
      parsingDigitsOnly = false;

      if (parsingNumbers = flag)
        {
          setType('0','9',NUMERIC);
          setType("+-.",NUMERIC);
          parseNonDecimals();
        }
      else
        {
          unsetType('0','9',NUMERIC);
          unsetType("+-.",NUMERIC);
        }
    }

   public final void parseNonDecimals (boolean flag)
    {
      parsingNonDecimals = flag;
    }

  /* ======================================================================= */

  /**
   * If called, the next call to <tt>NextToken()</tt> will return the same
   * token again.
   */
  public final void pushBack ()
    {
      if (ttype != TT_NOTHING) pushedBack = true;
    }
  
  /* ======================================================================= */

  /**
   * Returns a printable value of the current state of this
   * <tt>StreamTokenizer</tt>.
   */
  public String toString()
    {
      String string;

      switch (ttype)
        {
        case TT_EOF:
          string = "EOF";
          break;
        case TT_EOL:
          string = "EOL";
          break;
        case TT_WORD:
          string = "WORD(" + sval + ")";
          break;
        case TT_NUMBER:
          if (isInteger)
            string = "NUMBER(" + (int)nval + ")";
          else
            string = "NUMBER(" + nval + ")";
          break;
        case TT_NOTHING:  
          string = "NOTHING";
          break;
        default:
          {
            if (ttype == leftQuote)
              {
                string = "QUOTE(" + (char)leftQuote + sval + (char)rightQuote + ")";
                break;
              }

            char s[] = new char[3];
            s[0] = s[2] = IO.SQT;
            s[1] = (char)ttype;
            string = new String(s);
            break;
          }
        }

      return new Span(tokenStart,tokenEnd) + "\t" + string;
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
  // The following are additional public utilities that I find useful. NB: Using
  // these will make your application INCOMPATIBLE with java.io.StreamTokenizer.
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

  /**
   * Defines a pair of characters as a pair of left and right quotes.
   * This allows tokenizing words quoted by two different characters.
   * Note that only the left quote is categorized as <tt>QUOTE_TYPE</tt>.
   * The third argument specifies the escape character: if 0, no escape
   * is allowed within these quotes.
   */
  public final void quotePair (int left, int right, int escape)
    {
      if (0 <= left && left < charInfo.length)
        {
          charInfo[left].type = QUOTE_TYPE;
          charInfo[left].right = right;
          charInfo[left].escape = escape;
        }
    }

  /**
   * Same as above with implicit default escape character.
   */
  public final void quotePair (int left, int right)
    {
      quotePair(left,right,ESCAPE_CHAR);
    }

  /**
   * Same as <tt>quotePair</tt> but <tt>left</tt>'s <tt>QUOTE_TYPE</tt> is
   * non-exclusive.
   */
  public final void setQuotePair (int left, int right, int escape)
    {
      if (0 <= left && left < charInfo.length)
        {
          setType(left,QUOTE_TYPE);
          charInfo[left].right = right;
          charInfo[left].escape = escape;
        }
    }

  /**
   * Same as above with implicit default escape character.
   */
  public final void setQuoteChar (int c)
    {
      setQuotePair(c,c,ESCAPE_CHAR);
    }

  /**
   * Sets this character to be a quote character, non-exclusively,
   * and specifies its escape character.
   */
  public final void setQuoteChar (int c, int e)
    {
      setQuotePair(c,c,e);
    }

  /**
   * Sets this character to be an ordinary character, non-exclusively.
   */
  public final void setOrdinaryChar (int c)
    {
      if (0 <= c && c < charInfo.length) setType(c,ORDINARY_TYPE);
    }

  /**
   * Sets this character to be a word character, exclusively.
   */
  public final void wordChar (int c)
    {
      if (0 <= c && c < charInfo.length)
        charInfo[c].type = WORD_TYPE;
    }

  /**
   * Sets this character to be a word character, non-exclusively.
   */
  public final void setWordChar (int c)
    {
      if (0 <= c && c < charInfo.length) setType(c,WORD_TYPE);
    }

  /**
   * Sets all characters in this string to be word characters, exclusively.
   */
  public final void wordChars (String chars)
    {
      resetCharString(WORD_TYPE, chars);
    }

  /**
   * Sets all characters in this string to be word characters, non-exclusively.
   */
  public final void setWordChars (String chars)
    {
      setCharString(WORD_TYPE, chars);
    }

  /**
   * Sets this character to be a whitespace character, exclusively.
   */
  public final void whitespaceChar (int c)
    {
      if (0 <= c && c < charInfo.length) charInfo[c].type = WHITESPACE_TYPE;
    }

  /**
   * Sets this character to be a whitespace character, non-exclusively.
   */
  public final void setWhitespaceChar (int c)
    {
      if (0 <= c && c < charInfo.length) setType(c,WHITESPACE_TYPE);
    }

  /**
   * Sets all characters in this string to be whitespace characters, exclusively.
   */
  public final void whitespaceChars (String chars)
    {
      resetCharString(WHITESPACE_TYPE, chars);
    }

  /**
   * Sets all characters in this string to be whitespace characters, non-exclusively.
   */
  public final void setWhitespaceChars (String chars)
    {
      setCharString(WHITESPACE_TYPE, chars);
    }

  /**
   * Sets all characters in this string to be ordinary characters, exclusively.
   */
  public final void ordinaryChars (String chars)
    {
      resetCharString(ORDINARY_TYPE, chars);
    }

  /**
   * Sets all characters in this string to be ordinary characters, non-exclusively.
   */
  public final void setOrdinaryChars (String chars)
    {
      setCharString(ORDINARY_TYPE, chars);
    }

  /**
   * Checks whether this character's type is ordinary, among other types.
   */
  public final boolean isOrdinaryChar (int c)
    {
      return (0 <= c && c < charInfo.length) && hasType(c,ORDINARY_TYPE);
    }

  /**
   * Checks whether this character's type is numeric, among other types.
   */
  public final boolean isNumericChar (int c)
    {
      return (0 <= c && c < charInfo.length) && hasType(c,NUMERIC_TYPE);
    }

  /**
   * Checks whether this character's type is word, among other types.
   */
  public final boolean isWordChar (int c)
    {
      return c >= charInfo.length || c > 0 && hasType(c,WORD_TYPE);
    }

  /**
   * Checks whether this character's type is whitespace, among other types.
   */
  public final boolean isWhitespaceChar (int c)
    {
      return (0 <= c && c < charInfo.length) && hasType(c,WHITESPACE_TYPE);
    }

  /* ======================================================================= */

  /**
   * Checks whether this character's type is quote, among other types.
   */
  public final boolean isQuoteChar (int c)
    {
      return (0 <= c && c < charInfo.length) && hasType(c,QUOTE_TYPE);
    }

  /**
   * Returns the next character to be read.
   */
  public final int peek ()
    {
      return followingChar;
    }

  /**
   * Returns the previous character that was read.
   */
  public final int peekBack ()
    {
      return previousChar;
    }

  /**
   * A public method for skipping the current character
   * in the input.
   */
  public final void skipChar () throws IOException
    {
      nextChar();
    }    

  /**
   * Same as <tt>skipChar()</tt>, but if the specified flag is <tt>false</tt>
   * (resp., <tt>true</tt>) resets the start (resp., end) location of the current
   * token.
   */
  public final void skipChar (boolean flag) throws IOException
    {
      nextChar();
      if (flag)
        resetEndLocation();
      else
        resetStartLocation();
    }    

  /* ======================================================================= */

  /**
   * Set the type of this character to be the one specified, non-exclusively.
   */
  public final void setType (int c, int type)
    {
      setType(c,(byte)(1<<type));
    }      

  /**
   * Set the type of all characters in this range to be the one specified,
   * non-exclusively.
   */
  public final void setType (int low, int high, int type)
    {
      for (int c = low; c <= high; c++) setType(c,type);
    }      

  /**
   * Set the type of all characters in this string to be the one specified,
   * non-exclusively.
   */
  public final void setType (String chars, int type)
    {
      for (int i = 0; i < chars.length(); i++) setType(chars.charAt(i),type);
    }      

  /**
   * Unset the specified type, but no other one, for this character.
   */
  public final void unsetType (int c, int type)
    {
      unsetType(c,(byte)(1<<type));
    }      

  /**
   * Unset the specified type, but no other one, for all characters in this range.
   */
  public final void unsetType (int low, int high, int type)
    {
      for (int c = low; c <= high; c++) unsetType(c,type);
    }      

  /**
   * Unset the specified type, but no other one, for all characters in this string.
   */
  public final void unsetType (String chars, int type)
    {
      for (int i = 0; i < chars.length(); i++) unsetType(chars.charAt(i),type);
    }      

  /**
   * returns the current character.
   */
  public final int currentChar ()
    {
      return currentChar;
    }      

  /* ======================================================================= */

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
  // The following are protected facilities...
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  protected int currentChar   = TT_NOTHING;
  protected int previousChar  = TT_NOTHING;
  protected int followingChar = TT_NOTHING;

  protected int currentCharLine = 1;
  protected int followingCharLine = 1;

  protected int currentCharCol = 0;
  protected int followingCharCol = 0;

  protected String currentCharFile;
  protected String followingCharFile;

  protected StringBuilder wordBuffer;
  protected int leftQuote;
  protected int rightQuote;

  protected boolean parsingNumbers;
  protected boolean parsingNonDecimals;
  protected boolean parsingDigitsOnly;

  protected boolean eolIsSignificant;
  protected boolean spaceIsSignificant;
  protected boolean pushedBack = false;

  /* ======================================================================= */

  protected final CharInfo[] charInfo = new CharInfo[256];

  protected final void initializeCharInfo ()
    {
      for (int c=0; c<charInfo.length; c++) charInfo[c] = new CharInfo();
    }

  protected final void setType (int c, byte type)
    {
      charInfo[c].type |= type;
    }      

  protected final void unsetType (int c, byte type)
    {
      charInfo[c].type &= ~type;
    }      

  protected final boolean hasType (int c, byte type)
    {
      return (charInfo[c].type & type) != EMPTY_TYPE;
    }      

  protected final boolean thisHasType (byte type)
    {
      return hasType(currentChar,type);
    }
    
  /* ======================================================================= */

  /**
   * Make all characters ordinary, do not parse numbers, and do not recognize
   * comments.
   */
  public final void resetSyntax ()
    {
      typePrecedence[0] = WORD;
      typePrecedence[1] = WHITESPACE;
      typePrecedence[2] = COMMENT;
      typePrecedence[3] = QUOTE;
      typePrecedence[4] = ORDINARY;

      for (int c = 0; c < charInfo.length; c++) charInfo[c].type = ORDINARY_TYPE;
      parseNumbers       (false);
      eolIsSignificant   (true);
      spaceIsSignificant (true);
      disableComments();
    }

  /**
   * Resets the syntax configuration of the tokenizer to the default setting.
   * Namely:
   * <ul>
   * <li> set the type precedence ordering to: <tt>WORD</tt>,
   *      <tt>WHITESPACE</tt>, <tt>COMMENT</tt>, <tt>QUOTE</tt>,
   *      and <tt>ORDINARY</tt>;
   * <li> make all control characters and space <tt>WHITESPACE</tt> characters;
   * <li> make all letters and numerals <tt>WORD</tt> characters;
   * <li> make <tt>_</tt> and <tt>.</tt> <tt>WORD</tt> characters;
   * <li> make <tt>'</tt> and <tt>"</tt> quote characters (<tt>'</tt> for characters
   *      and character octal codes and unicode, and <tt>"</tt> for strings);
   * <li> make end-of-line not significant;
   * <li> make white spaces not significant;
   * <li> make all other characters ordinary;
   * <li> make parsing numbers effective;
   * <li> make skipping C-style and C++-style comments effective.
   * </ul>
   */
  public final void setDefaultSyntax ()
    {
      typePrecedence[0] = WORD;
      typePrecedence[1] = WHITESPACE;
      typePrecedence[2] = COMMENT;
      typePrecedence[3] = QUOTE;
      typePrecedence[4] = ORDINARY;

      for (int c = 0; c < charInfo.length; c++) charInfo[c].type = ORDINARY_TYPE;
      
      whitespaceChars    (0,' ');

      wordChars          ('A','Z');
      wordChars          ('a','z');
      wordChars          ('0','9');
      wordChars          ("_.");

      quoteChar          ('"');
      quoteChar          ('\''); 
      
      parseNumbers       (true);
      eolIsSignificant   (false);
      spaceIsSignificant (false);
      slashStarComments  (true);
      slashSlashComments (true);
    }

  /**
   * <a name="currentType()"></a> Returns the type of the current character. If
   * numbers are parsed, the three characters '<tt>.</tt>', '<tt>+</tt>', and
   * '<tt>-</tt>' take a special meaning. For non-numeric characters, since
   * they may have several types, their type is interpreted according to
   * precedence reflected by the rank in the sequence of tests specified by the
   * <tt>typePrecedence</tt> array. The default ordering is: <tt>WORD</tt>,
   * <tt>WHITESPACE</tt>, <tt>COMMENT</tt>, <tt>QUOTE</tt>, and <tt>ORDINARY</tt>.
   *
   * @see StreamTokenizer#setTypePrecedence(int,int,int,int,int)
   * @see LAStreamTokenizer#setTypePrecedence(int,int,int,int)
   */
  final int currentType ()
    {
      return currentType(true);
    }

  /**
   * This is the same as <tt>currentType()</tt>, but if <tt>strict</tt>
   * is <tt>false</tt> it bypasses treating some characters strictly and just
   * returns the character's type according to the current type precedence.
   */
  protected final int currentType (boolean strict)
    {
      if (strict)
        {
          // Any negative value returned by the reader is deemed SPECIAL:
          if (currentChar < 0) return SPECIAL;
          
          // The first case takes care of foreign characters:
          if (currentChar >= charInfo.length) return WORD;
          
          // The next case takes care of dot and sign characters in numbers:
          if (parsingNumbers && thisHasType(NUMERIC_TYPE))
	    {
	      if (parsingDigitsOnly)
		if (isDecimalDigit(currentChar))
		  return NUMERIC;
		else
		  return currentType(false);

	      switch (currentChar)
		{ // This will consider '.', '+', and '-' as the start of a number
		  // if they are followed by a decimal digit; '+', and '-' are also
		  // considered to start a number if the next character is a '.'.
		  // Note that the latter will fail if the '.' is not followed by
		  // a number (an exception will occur while parsing the expected
		  // number).
		case '+': case '-':
		  if (followingChar == '.')
		    return NUMERIC;
		  if (!isDecimalDigit(followingChar))
		    return currentType(false);
		case '.':
		  if (!isDecimalDigit(followingChar) || previousChar == '.')
		    return currentType(false);
		}

	      return NUMERIC;
	    }
        }

      for (int i = 0; i<typePrecedence.length; i++)
        if (thisHasType((byte)(1<<typePrecedence[i])))
          return typePrecedence[i];

      return SPECIAL; // This is not needed, but Java complains otherwise.
    }
  
  protected final void setCharRange (byte type, int low, int high)
    {
      if (0 <= low && high < charInfo.length)
        for (int c = low; c <= high; c++) setType(c,type);
    }

  protected final void resetCharRange (byte type, int low, int high)
    {
      if (0 <= low && high < charInfo.length)
        for (int c = low; c <= high; c++) charInfo[c].type = type;
    }

  protected final void setCharString (byte type, String chars)
    {
      for (int i = 0; i<chars.length(); i++)
        {
          int c = chars.charAt(i);
          if (0 <= c && c < charInfo.length) setType(c,type);
        }
    }

  protected final void resetCharString (byte type, String chars)
    {
      for (int i = 0; i<chars.length(); i++)
        {
          int c = chars.charAt(i);
          if (0 <= c && c < charInfo.length)
            charInfo[c].type = type;
        }
    }

  /**
   * Reads the next character and keeps track of line numbers and
   * file names.
   */
  protected final void nextChar () throws IOException
    {
      previousChar = currentChar;

      currentChar     = followingChar;
      currentCharLine = followingCharLine;
      currentCharCol  = followingCharCol;
      currentCharFile = followingCharFile;

      readFollowingChar();

      if (reader instanceof IncludeReader)
        {
          followingCharLine = ((IncludeReader)reader).getLineNumber();
          followingCharCol = ((IncludeReader)reader).getColumnNumber();
        }
      else
        if (followingChar == IO.EOL)
          {
            followingCharLine++;
            followingCharCol = 0;
          }

      if (reader instanceof IncludeReader)
        followingCharFile = ((IncludeReader)reader).getFile();

      // // The following two statements are for debugging purposes:
      // if (Debug.flagIsOn())
      // 	{
      // 	  System.out.println("The current char is "+Misc.pform(currentChar)+" read from "+
      // 			     (currentCharFile != null
      // 			      ? "file "+currentCharFile+" (line: "+currentCharLine+", col: "+currentCharCol+")"
      // 			      : "the console"));
      // 	  System.out.println("The following char is "+Misc.pform(followingChar)+" read from "+
      // 			     (followingCharFile != null
      // 			      ? "file "+followingCharFile+" (line: "+followingCharLine+", col: "+followingCharCol+")"
      // 			      : "the console"));
      // 	}
    }

  protected final void skipLine () throws IOException
    {
      // if (Debug.flagIsOn())
      // 	System.out.println("Skipping line "+
      // 			   (currentCharFile != null
      // 			    ? currentCharLine+" in file "+currentCharFile
      // 			    : "at the console"));

      while (followingChar != IO.EOL)
        {
          nextChar();

	  // if (Debug.flagIsOn())
	  //   System.out.println("The following char while skipping the line is "+Misc.pform(followingChar));

          if (followingChar == IO.EOF)
	    break;
        }

      // if (Debug.flagIsOn())
      // 	System.out.println("The following char after skipping the line is "+Misc.pform(followingChar));
    }

  /* ======================================================================= */


  protected final void readWord () throws IOException
    {
      wordBuffer = new StringBuilder();
      wordBuffer.append((char)currentChar);
      while (isWordChar(followingChar))
        {
          wordBuffer.append((char)followingChar);
          nextChar();
        }
      sval = wordBuffer.toString().intern();      
    }

  /**
   * <a name="readNumber()"></a>
   * Reads a number and set <tt>nval</tt> to its value. If the number is an
   * integer, the flag <tt>isInteger</tt> is also set to <tt>true</tt>.
   * <p>
   * If the number starts with a '<tt>-</tt>' or '<tt>+</tt>'
   * <i>immediately</i> followed by a decimal digit or a '<tt>.</tt>',
   * then the sign is used as the number's. If the sign is immediately followed
   * by a '<tt>.</tt>', which in turn is <i>not</i> immediately followed
   * by a decimal digit, this is an error and a <tt>"Bad number format"</tt>
   * exception is thrown. If the input being tokenized contains numbers with
   * gaps between signs and numbers, then the sign is tokenized as indicated
   * by its prevailing type.
   * <p>
   * NB: The sign of an exponent in the floating point notation is always parsed
   * as the exponent's sign regardless of the sign character's type. For example,
   * <tt>12.3e-4</tt> is correctly parsed as <tt>0.00123</tt> even
   * if '<tt>-</tt>' is ordinary.
   * <p>
   * If the first digit is '<tt>0</tt>', the number is parsed as an octal if
   * the next digit is <i>not</i> '<tt>x</tt>' or '<tt>X</tt>';
   * otherwise, as a hexadecimal. In either case, the number must be an integer
   * (i.e., it has no mantissa).
   * <p>
   * Type letter suffixes (i.e., '<tt>l</tt>' or '<tt>L</tt>' for
   * integers, and '<tt>f</tt>', '<tt>F</tt>', '<tt>d</tt>',
   * '<tt>D</tt>' for floating point numbers) are also recognized, but
   * ignored since the value returned is always a <tt>double</tt>.
   * @see        #currentType()
   */
  protected final void readNumber () throws IOException
    {
      isInteger = true;
      nval = 0;

      boolean isNegative = (currentChar == '-');

      if (currentChar == '-' || currentChar == '+') nextChar();

      switch (currentChar)
        {
        case '0':
          if (parsingNonDecimals)
            switch(followingChar)
              {
              case 'x': case 'X':
                readHexadecimal();
                break;
              case '0': case '1': case '2': case '3':
              case '4': case '5': case '6': case '7':
                readOctal();
                break;
              default:
                readDecimal();
              }
          else
            readDecimal();
          break;
        case '.': case '1': case '2': case '3': case '4':
        case '5': case '6': case '7': case '8': case '9':
          readDecimal();
          break;
        default:
          // This should not happen, because this method is entered only with
          // currentChar having been determined to be of type NUMERIC, which
          // entails that exactly one of the foregoing cases fires.
          throw new IOException("Bad number format");
        }

      if (isNegative)
	nval = -nval;
    }

  protected final void readOctal ()  throws IOException
    {
      while (isOctalDigit(followingChar))
        {
          nval = 8*nval + (followingChar - '0');
          nextChar();
        }

      if (followingChar == 'l' || followingChar == 'L') nextChar();
    }
          
  protected final void readHexadecimal ()  throws IOException
    {
      nextChar(); // skip the 'x' (NB: "0x" and "0X" are parsed as 0).
      while (isHexadecimalDigit(followingChar))
        {
          nval = 16*nval + hexValue(followingChar);
          nextChar();
        }

      if (followingChar == 'l' || followingChar == 'L') nextChar();
    }

  protected final int hexValue (int digit)
    {
      switch (digit)
        {
        case '0': case '1': case '2': case '3': case '4':
        case '5': case '6': case '7': case '8': case '9':
          return (digit - '0');
        case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
          return 10 + (digit - 'A');
        case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
          return 10 + (digit - 'a');
        }
      return 0;
    }

  /* ======================================================================= */


  protected final boolean canFollowDecimalDot(int c)
    {
      return (isDecimalDigit(c)
              || c == 'e' || c == 'E'
              || c == 'd' || c == 'D'
              || c == 'f' || c == 'F');
    }

  protected final void readInteger () throws IOException
    {
      nval = currentChar - '0';

      while (isDecimalDigit(followingChar))
        {
          nval = 10*nval + (followingChar - '0');
          nextChar();
        }
    }
    
  protected final void readMantissa (boolean hasDigits) throws IOException
    {
      double mantissa = 0;
      int precision = 1;

      while (isDecimalDigit(followingChar))
        {
          hasDigits = true;
          mantissa = 10*mantissa + (followingChar - '0');
          precision *= 10;
          nextChar();
        }

      if (!hasDigits) throw new IOException("Bad number format");

      nval += mantissa/precision;
    }

  protected final void readExponent () throws IOException
    {
      boolean hasDigits = false;
      boolean isFractional = false;
      int exponent = 0;

      nextChar();
      if (followingChar == '-' || followingChar == '+')
        {
          isFractional = (followingChar == '-');
          nextChar();
        }

      while (isDecimalDigit(followingChar))
        {
          hasDigits = true;
          exponent = 10*exponent + (followingChar - '0');
          nextChar();
        }
      
      if (!hasDigits) throw new IOException("Bad number format");

      for (int i = 1; i <= exponent; i++)
        if (isFractional)
          nval /= 10;
        else
          nval *= 10;
    }

  protected final void readQuotedWord () throws IOException
    {
      wordBuffer = new StringBuilder();
      for (;;)
        {
          nextChar();
          if (currentChar == rightQuote) break;
          if (currentChar == charInfo[leftQuote].escape) readEscapedChar();
          if (currentChar == IO.EOF)
            throw new EOFException
              ("End of file encountered while reading a quoted string");

          wordBuffer.append((char)currentChar);
        }
      sval = wordBuffer.toString().intern();
    }

  protected final void readEscapedChar () throws IOException
    {
      if (isOctalDigit(followingChar))
        {
          readOctalCode();
          return;
        }

      switch (followingChar)
        {
        case 'n':
          currentChar = IO.EOL;
          break;
        case 't':
          currentChar = IO.TAB;
          break;
        case 'b':
          currentChar = IO.BSP;
          break;
        case 'r':
          currentChar = IO.CRT;
          break;
        case 'f':
          currentChar = IO.FFD;
          break;
        case 'u':
          readUnicode();
          break;
        default: // interpret the next char literally
          currentChar = followingChar;
        }

      readFollowingChar();
    }

  protected final void readOctalCode () throws IOException
    {
      currentChar = 0;
      
      for (int i = 0; i < 3; i++)
        {
          if (!isOctalDigit(followingChar)) return;

          currentChar = 8*currentChar + (followingChar - '0');
          readFollowingChar();
        }
    }

  protected final void readUnicode () throws IOException
    {
      while (followingChar == 'u') readFollowingChar();

      currentChar = 0;
      
      for (int i = 0; i < 4; i++)
        {
          if (!isHexadecimalDigit(followingChar))
            throw new IOException
              ("Non-hexadecimal digit in unicode ("+location()+")");

          currentChar = 16*currentChar + hexValue(followingChar);
          if (i <3) readFollowingChar();
        }
    }

  protected final boolean isOctalDigit (int c)
    {
      return ('0' <= c && c <= '7');
    }

  protected final boolean isDecimalDigit (int c)
    {
      return ('0' <= c && c <= '9');
    }

  protected final boolean isHexadecimalDigit (int c)
    {
      return ('0' <= c && c <= '9' || 'A' <= c && c <= 'F' || 'a' <= c && c <= 'f');
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
    * This defines the class of objects stored in the character information table
    * <tt>charInfo</tt>.
    */
  static protected class CharInfo
    {
      /**
        * The type of character.
        */
      byte type = EMPTY_TYPE;
      /**
        * If the type is <tt>QUOTE_TYPE</tt>, this is the closing quote char.
        */
      int right;
      /**
        * If the type is <tt>QUOTE_TYPE</tt>, this is the escape character used;
        * when equal to 0, no escape may be used for this quote.
        */
      int escape = ESCAPE_CHAR;

      CharInfo () {};
    }
}
