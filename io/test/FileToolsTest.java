import hlt.language.io.FileTools;

public class FileToolsTest
{
  public static void main (String[] Args)
    {
      String file = "./../foo/bar/baz/fuz.tst";
      FileTools.setSeparator("/");

      System.out.println("file              = \""+file+"\"");
      System.out.println("dir(file)         = \""+FileTools.dir(file)+"\"");
      System.out.println("extension(file)   = \""+FileTools.extension(file)+"\"");
      System.out.println("fullPrefix(file)  = \""+FileTools.fullPrefix(file)+"\"");
      System.out.println("prefix(file)      = \""+FileTools.prefix(file)+"\"");
      System.out.println("simpleName(file)  = \""+FileTools.simpleName(file)+"\"");
      System.out.println("suffix(file)      = \""+FileTools.suffix(file)+"\"");
      System.out.println("hasDot(file)      = "+FileTools.hasDot(file));
      System.out.println("dotPosition(file) = "+FileTools.dotPosition(file));
    }
}
