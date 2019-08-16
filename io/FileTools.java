//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.util.List;
import java.util.ArrayList;

/**
 * This class defines some simple static functions that manipulate
 * file names. It has no constructor since all its methods are
 * static and accessible through the class name.
 *
 * @version     Last modified on Fri Oct 19 17:32:08 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public final class FileTools
{
  /**
   * Breaks a file path down into individual elements and adds to a
   * list, which is returned.  Example: if a path is <tt>/a/b/c/d.txt</tt>, the breakdown
   * will be <tt>[d.txt,c,b,a]</tt>.
   *
   * @param file input file
   * @return a List collection with the individual elements of the path in reverse order
   */
  private static List pathToList (File file)
    {
      List list = new ArrayList();
      File part;
      try
	{
	  part = file.getCanonicalFile();
	  while (part != null)
	    {
	      list.add(part.getName());
	      part = part.getParentFile();
	    }
	}
      catch (IOException e)
	{
	  e.printStackTrace();
	  list = null;
	}

      return list;
    }

  /**
   * Returns the string representing the relative path of 'path' with
   * respect to 'home'.
   *
   * @param home home path
   * @param path path of file
   */
  private static String matchPathLists (List home, List path)
    {
      int i; // index for 'home'
      int j; // index for 'path'
      String relpath; // relative path

      // First, eliminate common root:
      
      // Start at the beginning of the lists and iterate while both lists are equal:
      relpath = "";
      i = home.size()-1;
      j = path.size()-1;
      while ((i >= 0)&&(j >= 0)&&(home.get(i).equals(path.get(j))))
	{
	  i--;
	  j--;
	}

      // For each remaining level in the home path, add a "..":
      for (;i>=0;i--)
	relpath += ".." + File.separator;

      // Add  each level in the file path to the relative path:
      for (;j>=1;j--)
	relpath += path.get(j) + File.separator;

      relpath += path.get(j);

      // Return the relative path name:
      return relpath;
    }

  /**
   * Gets the relative path of File <tt>file</tt> with respect to
   * <tt>home</tt> directory.
   *
   * Example: <pre>home = /a/b/c
   *file = /a/d/e/x.txt
   *path = getRelativePath(home,file) = ../../d/e/x.txt</pre>
   *
   * @param home base path (should be a directory, not a file, or it doesn't make sense)
   * @param file file to generate relative path for
   * @return path from home to file as a string
   */
  public static String getRelativePath (File home, File file)
    {
      return matchPathLists(pathToList(home),
			    pathToList(file));
    }

//   /**
//    * Tests the above three functions.
//    */
//   public static void main (String args[])
//     {
//       if (args.length != 2) {
// 	System.out.println("RelativePath <home> <file>");
// 	return;
//       }
//       System.out.println("home = " + args[0]);
//       System.out.println("file = " + args[1]);
//       System.out.println("path = " + getRelativePath(new File(args[0]),new File(args[1])));
//     }

  /**
   * Copies src file to dst file.  If the dst file does not exist, it is
   * created.
   */
  public final static void copy (File src, File dst) throws IOException
    {
      InputStream in = new FileInputStream(src);
      OutputStream out = new FileOutputStream(dst);

      // Transfer bytes from in to out
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0)
        out.write(buf, 0, len);
      in.close();
      out.close();
    }

  static private char _separator = File.separatorChar;

  public final static void setSeparator (String s)
    {
      if (s.length() == 0)
        throw new RuntimeException("*** empty file separator");

      if (s.length() > 1)
        System.err.println("*** WARNING: bad file separator: \""+
                           s+"\" - taking only first char: '"+
                           s.charAt(0)+"\'");

      _separator = s.charAt(0);
    }

  public final static void setSeparator (char c)
    {
      _separator = c;
    }

  /**
   * Returns the position of the dot marking the file's extension,
   * or -1 if there is none. Note that if <tt>name = foo.bar/baz</tt>,
   * this returns -1.
   */
  public final static int dotPosition (String name)
    {
      int dotPos = name.lastIndexOf('.');

      if (dotPos < 0)
	return dotPos;

      int sepPos = name.lastIndexOf(_separator);

      if (sepPos < dotPos)
	return dotPos;

      return -1;
    }

  /**
   * Returns true iff the specified name has an extension dot
   * (though not necessarily an extension).
   */
  public final static boolean hasDot (String name)
    {
      return dotPosition(name) > 0;
    }

  /**
   * Returns the <tt>name.ext</tt> form of the specified file name.
   */
  public final static String simpleName (String name)
    {
      if (!hasDot(name)) return prefix(name);
      
      return prefix(name)+"."+suffix(name);
    }

  /**
   * Returns the <tt>name.ext</tt> form of the specified file.
   */
  public final static String simpleName (File file)
    {
      return simpleName (file.toString());
    }

  /**
   * Returns the relative prefix of the specified file name.
   */
  public final static String prefix (String name)
    {
      int start = name.lastIndexOf(_separator);
      int end = dotPosition(name);
      
      if (end < 0) return name.substring(start+1);

      return name.substring(start+1,end);       
    }

  /**
   * Returns the absolute prefix of the specified file name.
   */
  public final static String fullPrefix (String name)
    {
      int end = dotPosition(name);
      
      if (end < 0) return name.substring(0);

      return name.substring(0,end);       
    }

  /**
   * Returns the name prefix of the specified file.
   */
  public final static String prefix (File file)
    {
      return prefix(file.toString());
    }

  /**
   * Returns the extension of the specified file name.
   */
  public final static String extension (String name)
    {
      return suffix(name);
    }

  /**
   * Returns the suffix of the specified file name.
   */
  public final static String suffix (String name)
    {
      int dot = dotPosition(name);

      if (dot < 0) return "";

      return name.substring(dot+1);     
    }

  /**
   * Returns the suffix of the specified file name, or the name as is if
   * it contains no dot.
   */
  public final static String suffixIfDot (String name)
    {
      int dot = dotPosition(name);

      if (dot < 0) return name;

      return name.substring(dot+1);     
    }

  /**
   * Returns the suffix of the specified file.
   */
  public final static String suffix (File file)
    {
      return suffix(file.toString());
    }

  /**
   * Returns the directory part of the specified file name.
   */
  public final static String dir (String name)
    {
      int end = name.lastIndexOf(_separator);

      if (end < 0) return "";

      return name.substring(0,end);     
    }

  /**
   * Returns the directory part of the specified file.
   */
  public final static String dir (File file)
    {
      return dir(file.toString());
    }

}
