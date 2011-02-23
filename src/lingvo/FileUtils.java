/*
 *   FileUtils
 *
 */
package lingvo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

/**
 *  Набор сервистных функций для работы с файлами.
 *  Все методы статические.
 *
 *  @author <a href="mailto:movsikov@uib.cherkassy.net">Vladyslav Movsikov</a>
 *  @author <a href="mailto:Paul@uib.cherkassy.net">П.И. Бабачанах</a>
 *  @author <a href="mailto:churchyn@uib.cherkassy.net">Андрей Чурчин</a>
 *  @version     1.01
 *  @since       ZOO 4.0
*/
public final class FileUtils {
   /**
     *  Возвращает перечень файлов в указанном каталоге.
     *
     *  @param dir    каталог, для которого нужно формировать список.
     *  @return       форматированный список файлов.
     */
    public static String dirList(String dir, boolean recursive) {
      String ls = System.getProperty("line.separator","\r\n");
      java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
      File f1 = new File(dir);
      if ( f1.isFile() ) {
        return
            //f1.getName() + "   " +
            f1.getAbsolutePath()  + "   " +
            df.format(new java.util.Date(f1.lastModified())) + "   " +
            f1.length() + ls;
      }
      File[] list = f1.listFiles();
      String lst = dir + "   " + list.length + ls;
      //files
      for ( int i = 0; i<list.length; i++ ) {
        if ( list[i].isFile() ) {
          lst = lst + "   " +
            list[i].getName() + "   " +
            df.format(new java.util.Date(list[i].lastModified())) + "   " +
            list[i].length() + ls;
        }
      }
      //directories
      if (recursive) {
	      for ( int i = 0; i<list.length; i++ ) {
	        if ( list[i].isDirectory() ) {
	          lst = lst + dirList(list[i].getAbsolutePath(), recursive);
	        }
	      }
      }
      return lst;
    }

  /**
   * Возвращает сортированный массив строк в каталоге dir согласно фильтру filter
   * @param dir каталог в котором ищутся файлы
   * @param filter регулярное выражение в стиле Unix для описания имени файла
   * @return массив строк в каталоге dir согласно фильтру filter.
   *         Возвращает null если нет такого каталога.
   */
  public static String[] dirList(String dir, String filter) {
    File path = new File(dir);
    //System.out.println(path.getPath());
    String[] list;
    if(filter == null || filter.length() == 0)
      list = path.list();
    else
      list = path.list(new DirFilter(filter));

    if ( list != null ) {
      Arrays.sort(list, Utils.getAlphabeticComparator());
      //for(int i = 0; i < list.length; i++) System.out.println(list[i]);
    }
    return list;
  }

  /**
   * Удаление файлов из каталога согласно фильтру. При ошибке оставшиеся файлы
	 * не удаляются.
   * author      <a href="mailto:Paul@uib.cherkassy.net">П.И. Бабачанах</a>
   * @param dir имя каталога
   * @param filter регулярное выражение в стиле Unix для описания имен файлов
   * @see   DirFilter#dos2unixMask
   * @return количество успешно удаленных файлов
   * @throws IOException при невозможности удалить файл
   */
  public static int delete(String dir, String filter) throws IOException {
    int cnt = 0;
    String[] list = dirList(dir, filter);
    if ( list == null ) return 0;
    //System.out.println("Files in "+dir + " " +filter + " :"+list.length);
    for(int i = 0; i < list.length; i++) {
      //System.out.println("File="+ dir+File.separatorChar+list[i]+"=");
      if (new File(dir+File.separatorChar+list[i]).delete()) cnt ++;
			else throw new IOException("Не можу видалити файл: " + dir+File.separatorChar+list[i]);
    }
    return cnt;
  }

  /**
   * Перенос файлов из вх каталога в вых согласно фильтру
   * author      <a href="mailto:Paul@uib.cherkassy.net">П.И. Бабачанах</a>
   * @param fromDir имя каталога источника
   * @param filter регулярное выражение в стиле Unix для описания имен файлов
   * @see   DirFilter#dos2unixMask
   * @param toDir имя каталога приемника
   * @param overwrite если true, то перезаписываем существующий файл приемника
   * @return количество успешно перенесенных файлов
   */
  public static int move(String fromDir, String filter, String toDir, boolean overwrite) {
    /*
    int cnt = 0;
    String[] list = dirList(fromDir, filter);
    if ( list == null ) return 0;
    //System.out.println("Files in "+dir + " " +filter + " :"+list.length);
    for(int i = 0; i < list.length; i++) {
      //System.out.println("File="+ fromDir+File.separatorChar+list[i]+"=");
      File from_file = new File(fromDir+File.separatorChar+list[i]);
      File to_file = new File(toDir+File.separatorChar+list[i]);
      if ( overwrite ) {
        if (to_file.exists()) to_file.delete();
      }
      from_file.renameTo(to_file);
      cnt ++;
    }
    return cnt;
     */
    return copyMove(fromDir, filter, toDir, overwrite, true);
  }

  /**
   * Копирование файлов из вх каталога в вых согласно фильтру
   * author      <a href="mailto:Paul@uib.cherkassy.net">П.И. Бабачанах</a>
   * @param fromDir имя каталога источника
   * @param filter регулярное выражение в стиле Unix для описания имен файлов
   * @see   DirFilter#dos2unixMask
   * @param toDir имя каталога приемника
   * @param overwrite если true, то перезаписываем существующий файл приемника
   * @return количество успешно скопированных файлов
   */
  public static int copy(String fromDir, String filter, String toDir, boolean overwrite) {
    return copyMove(fromDir, filter, toDir, overwrite, false);
  }

  /**
   * Копирование файлов из вх каталога в вых согласно фильтру
   * author      <a href="mailto:Paul@uib.cherkassy.net">П.И. Бабачанах</a>
   * @param fromDir имя каталога источника
   * @param filter регулярное выражение в стиле Unix для описания имен файлов
   * @see   DirFilter#dos2unixMask
   * @param toDir имя каталога приемника
   * @param overwrite если true, то перезаписываем существующий файл приемника
   * @param move если true, то удаляет файл источника
   * @return количество успешно скопированных файлов
   */
  static int copyMove(String fromDir, String filter, String toDir,
    boolean overwrite, boolean move) {
    int cnt = 0;
    String[] list = dirList(fromDir, filter);
    if ( list == null ) return 0;
    //System.out.println("Files in "+dir + " " +filter + " :"+list.length);
    for(int i = 0; i < list.length; i++) {
      //System.out.println("File="+ fromDir+File.separatorChar+list[i]+"=");
      try {
        copyMove(fromDir+File.separatorChar+list[i], toDir, overwrite, move);
        cnt ++;
      } catch( IOException e) {}
    }
    return cnt;
  }

  /**
   * Копирование одного файла
   * author      <a href="mailto:Paul@uib.cherkassy.net">П.И. Бабачанах</a>
   * @param from_name имя файла источника
   * @param to_name имя файла приемника
   * @param overwrite если true, то перезаписываем существующий файл приемника
   * @throws IOException при ошибках
   */
  public static void copy(String from_name, String to_name, boolean overwrite) throws IOException{
    copyMove(from_name, to_name, overwrite, false);
  }

  /**
   * Копирование одного файла
   *
   * @param from_file файл источник
   * @param to_file файл приемник
   * @param overwrite если true, то перезаписываем существующий файл приемника
   * @throws IOException при ошибках
   */
  public static void copy(File from_file, File to_file, boolean overwrite) throws IOException{
    copyMove(from_file, to_file, overwrite, false);
  }

  /**
   * Перенос одного файла
   * @param from_name имя файла источника
   * @param to_name имя файла приемника
   * @param overwrite если true, то перезаписываем существующий файл приемника
   * @throws IOException при ошибках
   */
  public static void move(String from_name, String to_name, boolean overwrite) throws IOException{
    copyMove(from_name, to_name, overwrite, true);
  }

    /**
     *  Перенос одного файла
     *
     *  @param from_file файл источник
     *  @param to_file файл приемник
     *  @param overwrite если true, то перезаписываем существующий файл приемника
     *  throws IOException при ошибках
     */
     public static void move(File from_file, File to_file, boolean overwrite) throws IOException{
         copyMove(from_file, to_file, overwrite, true);
     }

    /**
     * Копирование/перенос файла из вх каталога в вых.
     *
     * @param from_file файл источник
     * @param to_file файл приемник
     * @param overwrite если true, то перезаписываем существующий файл приемника
     * @param move если true, то удаляет файл источника
     * @throws IOException при ошибках
     */
    private static void copyMove(File from_file, File to_file,
            boolean overwrite, boolean move) throws IOException {

        // First make sure the source file exists, is a file, and is readable.
        if (!from_file.exists()) abort("FileCopy: Не знайдено вхідного файла: " + from_file.getAbsolutePath());
        if (!from_file.isFile()) abort("FileCopy: Можна копіювати тільки файли: " + from_file.getAbsolutePath());
        if (!from_file.canRead()) abort("FileCopy: Помилка читання файла: " + from_file.getAbsolutePath());

        // If the destination is a directory, use the source file name
        // as the destination file name
        if (to_file.exists() && to_file.isDirectory()) to_file = new File(to_file, from_file.getName());

        // If the destination exists, make sure it is a writeable file
        // and ask before overwriting it.  If the destination doesn't
        // exist, make sure the directory exists and is writeable.
        if (to_file.exists()) {
            if (!to_file.canWrite()) abort("FileCopy: Помилка доступу до файла: " + to_file.getAbsolutePath());
            if (!overwrite) abort("FileCopy: Існуючий файл не перезаписан.");
        } else {
            // if file doesn't exist, check if directory exists and is writeable.
            // If getParent() returns null, then the directory is the current dir.
            // so look up the user.dir system property to find out what that is.
            String parent = to_file.getParent();  // Get the destination directory
            if (parent == null) parent = System.getProperty("user.dir"); // or CWD
            File dir = new File(parent);          // Convert it to a file.
            if (!dir.exists()) abort("FileCopy: Каталог не існує: " + parent);
            if (dir.isFile())  abort("FileCopy: Помилкове ім'я каталога: " + parent);
            if (!dir.canWrite()) abort("FileCopy: Помилка доступу до каталога: " + parent);
        }

        // If we've gotten this far, then everything is okay.
        // So we copy the file, a buffer of bytes at a time.
        FileInputStream from = null;  // Stream to read from source
        FileOutputStream to = null;   // Stream to write to destination
        try {
            from = new FileInputStream(from_file);  // Create input stream
            to = new FileOutputStream(to_file);     // Create output stream
            byte[] buffer = new byte[4096];         // A buffer to hold file contents
            int bytes_read;                         // How many bytes in buffer
            // Read a chunk of bytes into the buffer, then write them out,
            // looping until we reach the end of the file (when read() returns -1).
            // Note the combination of assignment and comparison in this while
            // loop.  This is a common I/O programming idiom.
            while((bytes_read = from.read(buffer)) != -1)   // Read bytes until EOF
                to.write(buffer, 0, bytes_read);            //   write bytes

            //21.04.2003 16:39
            to.close();
            to_file.setLastModified(from_file.lastModified());
            from.close();
            if (move) if (!from_file.delete())
							throw new IOException("Не можу видалити файл: " + from_file.getAbsolutePath());
        } finally {
          // Always close the streams, even if exceptions were thrown
          if (from != null) try { from.close(); } catch (IOException e) { ; }
          if (to != null) try { to.close(); } catch (IOException e) { ; }
        }
    }

    /**
     * This example is from _Java Examples in a Nutshell_. (http://www.oreilly.com)
     * Copyright (c) 1997 by David Flanagan
     * This example is provided WITHOUT ANY WARRANTY either expressed or implied.
     * You may study, use, modify, and distribute it for non-commercial purposes.
     * For any commercial use, see http://www.davidflanagan.com/javaexamples
     *
     * The static method that actually performs the file copy.
     * Before copying the file, however, it performs a lot of tests to make
     * sure everything is as it should be.
     *
     * Копирование/перенос файлов из вх каталога в вых согласно фильтру
     * @param from_name имя файла источника
     * @param to_name имя файла приемника
     * @param overwrite если true, то перезаписываем существующий файл приемника
     * @param move если true, то удаляет файл источника
     * @throws IOException при ошибках
     * @author      <a href="mailto:Paul@uib.cherkassy.net">П.И. Бабачанах</a>
     */
   private static void copyMove(String from_name, String to_name,
           boolean overwrite, boolean move) throws IOException {

        copyMove(new File(from_name), new File(to_name), overwrite, move);
    }

    /** A convenience method to throw an exception */
    private static void abort(String msg) throws IOException {
        throw new IOException(msg);
    }


    /**
     * Фильтр для выбора файлов
     * @author      <a href="mailto:Paul@uib.cherkassy.net">П.И. Бабачанах</a>
     */
    public static class DirFilter implements FilenameFilter {
      private Pattern p;
      DirFilter(String afn) {p = Pattern.compile(afn.toLowerCase());}

      /**
       * Преобразует маску MS-DOS в регулярное выражение в стиле Unix
       * @param mask маска файла в стиле MS-DOS. Разрешены *?
       * @return регулярное выражение в стиле Unix
       * author      <a href="mailto:Kolomiets@uib.cherkassy.net">А.В. Коломиец/a>
       */
      public static String dos2unixMask(String mask) {
        String result = "";
        if (mask.equals("*.")) result = "[^.]*";
        else
        for (int i=0;i<mask.length();i++) {
            if (mask.charAt(i)=='*') result += ".*";
                else if (mask.charAt(i)=='?') result += ".";
                    else result += "["+mask.charAt(i)+"]";
        }
        return result+"[ ]*";
      }

      public boolean accept(File dir, String name) {
        // Получение информации о пути:
        String f = new File(name).getName();
        //return f.indexOf(afn) != -1;
        //return f.startsWidth(afn);
        //return Pattern.matches(afn, f);
        return p.matcher(f.toLowerCase()).matches();
      }
    }  // DirFilter

    /**
     * Прочитать файл в байтовий буфер
     * @param path имя файла
     * @return   массив байт. Для преобразования в строку можно использовать
     * метод String(byte[] bytes).
     * @throws IOException при ошибках
     * author      <a href="mailto:Paul@uib.cherkassy.net">П.И. Бабачанах</a>
     */
  static public byte[] getBuffer(String path) throws IOException {
    File fin = new File(path);
    //byte[] b2 = new byte[fin.length()];//буфер файлу
    long llen = fin.length();
    if (llen > Integer.MAX_VALUE) {
      throw new IOException("Файл дуже великий. Максимальний розмiр " + Integer.MAX_VALUE);
    }
    int len = (int) llen;
    byte[] b2 = new byte[len];//буфер файлу

    FileInputStream in = new FileInputStream(path);
    in.read(b2);
    in.close();
    //Log.write(path+": fin.length()="+llen+" b2.length="+b2.length);
    return b2;
  }

    /**
   	 * Прочитать файл в байтовий буфер из URL. Например из jar.
     * @param url имя файла
     * @return   массив байт. Для преобразования в строку можно использовать
	 * метод String(byte[] bytes).
     * @throws IOException при ошибках
     * author <a href="mailto:churchyn@uib.cherkassy.net">Андрей Чурчин</a>
     */
  static public byte[] getBufferFromURL(URL url) throws IOException {
    InputStream in = url.openStream();
    int len=in.available();
    byte[] buf = new byte[len];
    in.read(buf);
    in.close();
    return buf;
  }


  /**
   * Записать байтовий буфер в файл
   * @param path - имя файла
   * @param buf - массив байтов
   * @throws IOException при ошибках
   * author <a href="mailto:churchyn@uib.cherkassy.net">Андрей Чурчин</a>
   */
  static public void setBuffer(String path, byte[] buf) throws IOException {
    FileOutputStream out = new FileOutputStream(path);
    out.write(buf);
    out.close();
  }

  /**
   * Возвращает расширение имени файла или "" если его определить не удалось
   * @param filename String
   * @return String
   * author <a href="mailto:churchyn@uib.cherkassy.net">Андрей Чурчин</a>
   */
  public static String getFileExtention(String filename) {
      String ext="";
      if (filename != null) {
        String words[] = filename.split("\\.");
        if (words.length>1)
            return words[words.length-1];
      }
      return ext;
  }

  /**
   * Возвращает путь с разделителями в Юникс-стиле /
   * @param filename String
   * @return String
   * author <a href="mailto:churchyn@uib.cherkassy.net">Андрей Чурчин</a>
   */
  public static String getUnixStyleFilename(String filename) {
	  return filename.replace('\\', '/');
  }

  
  /**
   * Возвращает каталог оканчивающийся на символ-разделитель каталогов,
   * если он не задан
   * @param path путь к каталогу
   * @return путь с символом-разделителем в конце 
   *         или null если аргумент path is null.
   * author <a href="mailto:churchyn@uib.cherkassy.net">Андрей Чурчин</a>
   */
  public static String validatePath(String path) {
      if (path == null) return null;

      String separator = System.getProperty("file.separator");
      if (path.endsWith(separator))
          return path;
      return path+separator;
  }

  /**
   * Удаляет файл если это возможно.
   * @param filename путь и имя файла
   * @return true если файл удалось удалить
   * author <a href="mailto:churchyn@uib.cherkassy.net">Андрей Чурчин</a>
   */
  public static boolean deleteFileIfPossible(String filename) {
      try { // to erase class file
          (new File(filename)).delete(); // erase temporary class file
          return true;
      }
      catch (Exception ex) {
          return false;
      }
  }

  /**
   * Запуск программы.
   *
   * @param commandName String
   * @param commandArguments String
   * @param workFolder String - текущий каталог (если null, то по-умолчанию)
   * @param wait boolean - ожидать ли завершения запущенной программы
   * @return int - код возврата запущенной программы, если wait=true, иначе 0
   * @throws IOException
   * @throws InterruptedException
   */
  public static int shellExec(String commandName,
                              String commandArguments,
                              String workFolder,
                              boolean wait)
      throws IOException, InterruptedException
  {
      Runtime runtime = Runtime.getRuntime();
      if (Utils.isBlank(commandArguments))
          commandArguments="";
      else
          commandArguments=" "+commandArguments.trim();
      File folder = null;
      if (!Utils.isBlank(workFolder))
          folder = new File(workFolder);
      if (wait) return (runtime.exec(commandName+commandArguments, null, folder)).waitFor();
      runtime.exec(commandName+commandArguments, null, folder);
      return 0;
  }

  /**
   * Открывает файл средствами оболочки системы. Т.е. система сама определяет
   * каую программу использовать в зависимости от типа открываемого файла.
   * @param file String - файл, который нужно открыть
   * @param workFolder String - текущий каталог (если null, то по-умолчанию)
   * @param wait boolean - ожидать ли завершения запущенной программы
   * @return int - код возврата запущенной программы, если wait=true, иначе 0
   * @throws IOException
   * @throws InterruptedException
   */
  public static int shellOpenFile(String file,
                                  String workFolder,
                                  boolean wait)
      throws IOException, InterruptedException
  {
      String osCmd = "";
      if (System.getProperty("os.name").equalsIgnoreCase("Windows 98"))
          osCmd = "start /w "; //для Win 98
      else
          osCmd = "cmd.exe /c "; //для Win XP
      return shellExec(osCmd, file, workFolder, wait);
  }
  	
  /**
   * Возвращает контрольную сумму (CRC32) файла
   * @param file
   * @return long
   * @throws IOException 
   */
	public static long getCRC32(File file) throws Exception {
		return getCRC32(getBuffer(file.getAbsolutePath()));
	}

	/**
   * Возвращает контрольную сумму (CRC32) файла
   * @param fileName
   * @return long
   * @throws IOException 
   */
	public static long getCRC32(String fileName) throws Exception {
		return getCRC32(getBuffer(fileName));
	}

	/**
	 * Возвращает контрольную сумму (CRC32) массива байт
	 */
	public static long getCRC32(byte bytes[]) {
		CRC32 crc=new CRC32();
		crc.update(bytes);
		return crc.getValue();
	}

	/* just for debugging*/
	/*
  public static void main(String[] args) {
    try {
  		//int cnt = FileUtils.delete("c:\\1\\1", FileUtils.DirFilter.dos2unixMask("*.001"));
			//System.out.println("cnt="+cnt);
    	copyMove("c:\\1\\1\\1.1","c:\\1\\1\\1.2", true, true);
    } catch (Exception e) {
			System.out.println("ex="+e);
    }
  }
	 */

} // end of class FileUtils
