//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.io;

/**
 * @version     Last modified on Wed Jun 20 13:55:55 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

import java.io.IOException;

/**
 * Signals an attempt to include a file already being included.
 *
 * @see         IncludeReader
 */

public class CircularInclusionException extends IOException
{
  /**
   * Constructs a new CircularInclusionException with a file name.
   */
  public CircularInclusionException (String file)
    {
      super("File: "+file);
    }
}
