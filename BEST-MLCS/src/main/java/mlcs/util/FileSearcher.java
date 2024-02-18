package mlcs.util;

import java.io.File;
import java.io.IOException;

public class FileSearcher {

  public static String getFileShortName(File file) {
    String fileName = file.getName();
    int extensionIdx = fileName.lastIndexOf(".");
    if (extensionIdx == -1) {
      return fileName;
    } else {
      return fileName.substring(0, extensionIdx);
    }
  }
  public static String getOutFile(File sourceFile, String outFileName) throws IOException {
    String sep = File.separator;
    String path = new File(sourceFile.getParent() + sep + ".." + sep + "out" + sep).getCanonicalPath();
    return path + sep + outFileName;
  }
}
