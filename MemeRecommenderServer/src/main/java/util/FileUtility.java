package util;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * The FileUtil class is used to avoid having try / catch blocks for file input and output in the code.
 *
 */
public class FileUtility {
	
	private HashMap<String, FileWriter> writers;
	private HashMap<String, BufferedReader> readers;
	
	public FileUtility() {
		writers = new HashMap<String, FileWriter>();
		readers = new HashMap<String, BufferedReader>();
	}
	
	/**
	 * Opens a new file writer for the given file name.
	 * @param fileName
	 * The name of the file. If the file is open, nothing happens. If a file was already closed, nothing happens.
	 */
	private boolean openWriter(String fileName) {
		if(!writers.containsKey(fileName)) {
			try {
				writers.put(fileName, new FileWriter(new File(fileName)));
				return true;
			} catch (IOException e) {
				Const.log(Const.LEVEL_ERROR, e.getMessage());
				return false;
			}			
		}

		return true; // to allow chaining as in "new FileUtil().openReader(...).openWriter(...) 
	}
	
	private boolean openReader(String fileName) {
		if(!readers.containsKey(fileName)) {
			try {
				readers.put(fileName, new BufferedReader(new FileReader(new File(fileName))));
				return true;
			} catch (IOException e) {
				Const.log(Const.LEVEL_ERROR, e.getMessage());
				return false;
			}
		}
		return true;
	}
	
	
	/** 
	 * @param key
	 * The path of the file that the String should be written to.
	 * @param s
	 * What should be written in the file
	 */
	public void write(String key, String s) {
		if(!writers.containsKey(key)) {
			if(!openWriter(key)) {
				return;
			}
		}
		
		try {	
			writers.get(key).write(s);
		} catch (IOException e) {
			Const.log(Const.LEVEL_ERROR, e.getMessage());
			e.printStackTrace();
		}
	}
	
	/** 
	 * Writes the String in the file & creates a new line
	 * @param key
	 * The path of the file that the String should be written to.
	 * @param s
	 * What should be written in the file
	 */
	public void writeLine(String key, String s) {
		write(key, s + "\n");
	}
	
	/**
	 * @param key 
	 * The file from which a line is read
	 * @return
	 * 	
	 */
	public String readLine(String key) {
		if(!readers.containsKey(key)) { 
			if(!openReader(key)) {
				return null;
			}
		}
		
		try {
			return readers.get(key).readLine();
		} catch (IOException e) {
			Const.log(Const.LEVEL_ERROR, e.getMessage());
			
		}
		
		
		return null;
	}
	
	public void closeAll() {
		closeReaders();
		closeWriters();
	}
	
	public void closeReader(String fileName) {
		if(readers != null && readers.containsKey(fileName)) {
			try {
				readers.get(fileName).close();
			} catch (Exception e) {
				Const.log(Const.LEVEL_ERROR, e.getMessage());
			}
			
			readers.remove(fileName);
		}
	}
	
	public void closeWriter(String fileName) {
		if(writers != null && writers.containsKey(fileName)) {
			try {
				writers.get(fileName).close();
			} catch (Exception e) {
				Const.log(Const.LEVEL_ERROR, e.getMessage());
			}
			
			writers.remove(fileName);
		}
	}
	
	private void closeReaders() {
		if(readers != null) {
			Iterator<BufferedReader> it = readers.values().iterator();
			it.forEachRemaining(new Consumer<BufferedReader>() {
				@Override
				public void accept(BufferedReader t) {
					try {
						t.close();
					} catch (IOException e) {
						Const.log(Const.LEVEL_ERROR, e.getMessage());
					}
				}		
			});
		}
	}
	
	private void closeWriters() {
		if(writers != null) {
			Iterator<FileWriter> it = writers.values().iterator();
			it.forEachRemaining(new Consumer<FileWriter>() {
				@Override
				public void accept(FileWriter t) {
					try {
						t.close();
					} catch (IOException e) {
						Const.log(Const.LEVEL_ERROR, e.getMessage());
					}
				}		
			});
		}
	}
	
	public void flushWriter(String fileName) {
		if(writers != null && writers.containsKey(fileName)) {
			FileWriter w = writers.get(fileName);
			try {
				w.flush();
			} catch (IOException e) {
				Const.log(Const.LEVEL_ERROR, e.getMessage());
			}
		}
	}
	
	public String readWholeFile(String key) {
		openReader(key);
		
		StringBuilder b = new StringBuilder();
		String s;
		while((s = this.readLine(key)) != null) {
			b.append(s);
		}
		
		closeReader(key);
		
		return b.toString();
	}

    public void forEveryLineIn(String key, Consumer<String> consumer) {
        if(openReader(key)) {
            String s;
            while((s = readLine(key)) != null) {
                consumer.accept(s);
            }
        }
    }

}
