package inodes;

public class FileEntry {
	public enum Types{
		FILE	(0),
		LINK	(1)
		;
		private final int typeCode;
		Types(int typeCode){
			this.typeCode = typeCode;
		}
		public int getTypeCode() {
	        return this.typeCode;
	    }
	}
	//protected static final int MAX_FILE_SIZE = 40; //B
	String name;
	//String ext;
	//String location; //po�a�enie wskaznik /Users/greg/text.txt
	int inodeNum; //ideks do tablicy i-w�z��w
	Types type_of_file; //dowi�zanie czy zwyk�y plik ????????????????? potrzebne przy dowi�zaniu
	//int adrIndexBlock;
	//boolean stan; //czy otwarty true-uzywany przez proces
	//int size; //B
	//w momencie czytanie umieszczany na poczatku pliku
	//w momencie zapisu na koncu pliku
	int currentPositionPtr;//pozycja czytania w pliku
	//obiekt semafora
	//Semaphore s;
	//protected int [] atrybuty = new int[10];//trwxrwxrwx pierwszy okresla typ(zwyk�y czy katalog)
									//x np.pozwala zmienic biezacy katalog
	//blok indeskowy
}

