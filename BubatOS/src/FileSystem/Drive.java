package inodes;
import java.io.File;
import java.util.*;
import inodes.FileEntry;
import inodes.FileEntry.Types;
public class Drive {
//	public enum Op{
//		O_RDONLNY	(0),
//		O_WRONLY	(1),
//		O_RDWR		(2)
//		;
//		private final int opCode;
//		Op(int opCode){
//			this.opCode = opCode;
//		}
//		public int getOpCode() {
//	        return this.opCode;
//	    }
//	}
	private static final int DRIVE_SIZE = 1024; //B 32B*32
	private static final int DRIVE_BLOCK_SIZE = 32; //B
	private static final int DRIVE_BLOCK_AMOUNT = 32;
	private static int FREE_BLOCK_AMOUNT = 32;
	
	public char 	[] drive;
	public int 		[] bitVector;
	//public File 	[] openFilesTable;
	//public ArrayList<Inode>  inodesTable;
	public Inode	[]  inodesTable;
	public Hashtable<String,FileEntry> catalog;
	
	/*--Constructor--*/
	public Drive(){
		drive = new char[DRIVE_SIZE];
		bitVector = new int[DRIVE_BLOCK_AMOUNT];
		//openFilesTable =  new File[32];
		//inodesTable = new ArrayList<Inode>(); //max.32
		inodesTable = new Inode[32];
		catalog = new Hashtable<String,FileEntry>(); //max.32
		
		/*--ZEROWANIE BIT VECTORA--*/
		for(int i=0;i<DRIVE_BLOCK_AMOUNT;i++){
			bitVector[i] = 1; //1 oznacza pole wolne
		}
		/*--ZEROWANIE DYSKU--*/
		for(int i=0;i<DRIVE_SIZE;i++){
			drive[i] = (char)0;
		}
		//tworzenie katalogu glownego i sciezki do niego
		//np. /home - montowanie systemu plik�w 
	}
	//np. u�ytkownik wpisze CR P1 40;
	/*
	 O_RDONLNY - tylko do odczytu (0)
	 O_WRONLY - tylko do zapisu (1)
	 O_RDWR - do zapisu i odczytu (2)
	 */
	private int freeSpaceCheck(){
		for(int i=0;i<DRIVE_BLOCK_AMOUNT;i++){
			if(bitVector[i] == 1)
			{
				bitVector[i] = 0;
				//inicjalizacja zaj�tego bloku 
				Arrays.fill(drive, i*32, i*32+32, (char)(-1));
				--FREE_BLOCK_AMOUNT;
				return i;
			}
		}
		return -1;
	}
	private int freeInodeIndex(){
		for(int i=0;i<32;i++){
			if(inodesTable[i] == null)
			{
				return i;
			}
		}
		return -1;//full
	}
	public void createFile(String name){
		int freeBlock = freeSpaceCheck();
		if(!catalog.containsKey(name) && freeBlock != -1){
			System.out.println("Creating a file...");
			FileEntry file = new FileEntry();
			Inode inode = new Inode();
			Calendar cal = Calendar.getInstance();
			file.name = name;
			file.type_of_file = Types.FILE;
			//zamkni�ty
			//file.stan = false;
			file.currentPositionPtr=0;//zapis i odczyt od pocz�tku pliku
			//file.s = new Semaphore("semafor");
			//nast�pny indeks tablicy
			//file.inodeNum = inodesTable.size();
			file.inodeNum = freeInodeIndex();
			/*I-NODES*/
			inode.month = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH );
			inode.day = cal.get(Calendar.DAY_OF_MONTH);
			inode.hour = cal.get(Calendar.HOUR_OF_DAY);
			inode.minute = cal.get(Calendar.MINUTE);
			//inode.type_of_file = Types.FILE;
			inode.stan = false;
			//inode.s =new Semaphore("semafor");
			inode.LinkCounter = 1;//first link
			inode.sizeF = 0;//B
			//okre�lenie pierwszego numeru bloku dyskowego
			
			inode.inode_table[0] = freeBlock;
			inode.inode_table[1] = -1;//-1 oznacza, �e nie jest wykorzystywane adresowanie po�rednie
			catalog.put(name,file);
			inodesTable[file.inodeNum] = inode;
			//inodesTable.add(inode);
			
			System.out.println("Created!");
		}
		else if(catalog.containsKey(name)){
			System.out.println("Istnieje juz plik o takiej nazwie");
		}
		else if(freeBlock == -1){
			System.out.println("Wszystkie bloki s� zaj�te");
		}
		//if()
		/*if(size < File.MAX_FILE_SIZE){
			
		}else{
			System.out.println("Za du�y rozmiar pliku");
		}*/
	}
	//Thread running;
	public void openFile(String name){
		//przegl�da katalog i kopiuje odpowiedni wpis katalogowy do tablicy otwartych plik�w
		//nale�y sprawdzi� czy plik nie jest otwarty przez inny proces
		//je�li ochrona na to zezwala
		//zwraca wskaznik do wpisu w tej tablicy, kt�ry jest u�ywany przez pozosta�e operacje
		//po otwarciu pliku kopia i-w�z�a jest przechowywana w pami�ci g��wnej
		if(catalog.containsKey(name))
		{
			FileEntry F = catalog.get(name);
			int k = F.inodeNum;
			//F.s.V(); //arg metooda zwracajacy aktualnie wykonywany proces;
			if(inodesTable[k].stan == true)
				System.out.println("Plik jest ju� otwarty");
				//return -1; //ju� otwarty
			else
			{
				inodesTable[k].stan = true; //uzywany, operacje na semaforach???
				F.currentPositionPtr=0;
				System.out.println("Pomy�lnie otwarto plik");
				//return k; //zwraca numer i-w�z�a
			}
		}
		else
			System.out.println("Plik o takiej nazwie nie istnieje");
		//return -2; //brak pliku o takiej nazwie
	}
	public void closeFile(String name){
		//usuwa wpis z tablicy otwartych plik�w
		/*
		 WYKONA� JESZCZE OPERACJE:
		 -zmiany stanu pliku(odblokowanie), dzia�anie na semaforach
		 -jak procesor umiera to mo�e np. wykoan� metod� close na pliku
		 i wtedy zmieni� jego stan
		 */
		if(catalog.containsKey(name))
		{
			FileEntry F = catalog.get(name);
			int k = F.inodeNum;
			if(inodesTable[k].stan == false)
				System.out.println("Plik jest ju� zamkniety");
				//return -1; //ju� zamkni�ty
			else
			{
				inodesTable[k].stan = false; //uzywany, operacje na semaforach???
				System.out.println("Pomy�lnie zamkni�to plik");
				//return k;
			}
		}
		else
			System.out.println("Plik o takiej nazwie nie istnieje");
		//return -2;
	}
	//DZIA�A LEGITNIE, ALE CZY CHODZI O TAKI SPOS�B???
	//ustali� czy podawa� miejsce, od okt�rego mamy wpisaywa� i ile
	public void writeFile(String name, String data){
		/*
		 WYKONA� JESZCZE OPERACJ�:
		 -sprawdzi� stan pliku przed podj�ciem akcji
		  
		 */
		//wywo�anie operacji open()
		//pisanie sekwencyjne
		//wskaznik za nowo napisanymi danymi
		//dane zapisuje si� w pobranym od zarz�dcy obszar�w wolnych bloku indeksowym
		//umieszcza si� go w i-tej pozycji
		//system przechowuje wskaznik pisania okreslajacy miejsce w pliku
		//int currentPositionPtr -> ustawiany na koncu pliku;
		if(catalog.containsKey(name))
		{
			FileEntry F = catalog.get(name);
			int k = F.inodeNum;
			if(inodesTable[k].stan == true) //mo�e sprawdzanie stanu semafora
			{
				int dataSize = data.length();//inodesTable.get(k).sizeF;
				if(dataSize <= 32)
				{
					int directBlockNum = inodesTable[k].inode_table[0];
					for(int i=0;i<data.length();i++)
					{
						drive[directBlockNum*32+i] = data.charAt(i);
					}
					inodesTable[k].sizeF = data.length();
					//closeFile(name);
				}
				//tworzymy blok indeksowy w i-node i
				//bierzemy pod uwage tylko ineksowy[1] i kolejne dyskowe
				//plus jeden, bo jeszcze blok indeksowy wliczamy
				else if(((dataSize+32-1)/32) <= FREE_BLOCK_AMOUNT) //+1
				{
					int restSize = dataSize - 32;
					//liczba s�u�y do okraniczenia wpis�w nr blok�w indekowych
					int n = (restSize+32-1)/32;
					int directBlockNum = inodesTable[k].inode_table[0];
					int inDirectBlockNum = freeSpaceCheck();
					//if(directBlockNum  != -1)
					//{
						//tylko zapis
						inodesTable[k].inode_table[1] = inDirectBlockNum;
						int in=0;
						for(;in<32;in++)
						{
							drive[directBlockNum*32+in] = data.charAt(in);
						}
						//wpisanie nr bloku dyskowego do bloku indeksowego
						//tyle razy 
						//int n = (a + b - 1) / b; -->ceiling
						//
						for(int j=0;j<n;j++)
						{
							drive[inDirectBlockNum*32+j] = (char)freeSpaceCheck();
						}
						for(int j=0;j<n;j++)
						{
							int from = (int)drive[inDirectBlockNum*32+j];
							//System.out.println("from: "+from);
							for(int i=0;in<data.length();i++,in++)
							{
								drive[from*32+i] = data.charAt(in);
							}
						}
						/*-----AKTUALIZACJA DANYCH O PLIKU----*/
						Calendar cal = Calendar.getInstance();
						inodesTable[k].month = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH );
						inodesTable[k].day = cal.get(Calendar.DAY_OF_MONTH);
						inodesTable[k].hour = cal.get(Calendar.HOUR_OF_DAY);
						inodesTable[k].minute = cal.get(Calendar.MINUTE);
						
						inodesTable[k].sizeF = data.length();
						
						//closeFile(name);
					//}
				}
				else
				{
					System.out.println("B�ad, brak miejsca na dysku");
				}
			}
			else
			{
				System.out.println("Plik nie jest otwarty");
			}
		}
		else
		{
			System.out.println("Nie istnieje plik o takiej nazwie");	
		}
	}
	public void appendFile(String name, String newData){
		//wypada ustawi� wskaznik na koncu pliku
		if(catalog.containsKey(name))
		{
			FileEntry F = catalog.get(name);
			int k = F.inodeNum;
			if(inodesTable[k].stan == true)
			{
				boolean flaga=false; //czy jest miejsce
				
				//int newDataSize = newData.length();
				int acDataSize = inodesTable[k].sizeF;
				int newDataSize = newData.length();
				
				int totalSize = acDataSize + newDataSize;
				int directBlockNum;
				if(totalSize <= 32)
				{
					flaga=true;
					//System.out.println("OK");///////////////////////////
					int in=0;
					directBlockNum = inodesTable[k].inode_table[0];
					for(int i=acDataSize;in<newData.length();in++,i++)
					{
						drive[directBlockNum*32+i] = newData.charAt(in);
					}
					inodesTable[k].sizeF += newData.length();
					//closeFile(name);
				}
				//plus jeden, bo  
				else if(acDataSize < 32 && ((newDataSize-(32-acDataSize)+32-1)/32)+1 <= FREE_BLOCK_AMOUNT)//+1
				{
					flaga=true;
					//System.out.println("OK2");//////////////////////////
					directBlockNum = inodesTable[k].inode_table[0];
					int in=0;
					for(int i=acDataSize;i<32;i++,in++)
					{
						drive[directBlockNum*32+i] = newData.charAt(in);
						--newDataSize;
					}
					//if(((newDataSize+32-1)/32) <= FREE_BLOCK_AMOUNT+1) 
					//{
					//liczba s�u�y do ograniczenia wpis�w nr blok�w indekowych
					int n = (newDataSize+32-1)/32;
					//int directBlockNum = inodesTable[k].inode_table[0];
					int inDirectBlockNum = freeSpaceCheck();
					//tylko zapis
					inodesTable[k].inode_table[1] = inDirectBlockNum;
					//wpisanie nr bloku dyskowego do bloku indeksowego
					//tyle razy 
					//int n = (a + b - 1) / b; -->ceiling
					//
					for(int j=0;j<n;j++)
					{
						drive[inDirectBlockNum*32+j] = (char)freeSpaceCheck();
					}
					for(int j=0;j<n;j++)
					{
						int from = (int)drive[inDirectBlockNum*32+j];
						//System.out.println("from: "+from);
						for(int i=0;in<newData.length();i++,in++)
						{
							drive[from*32+i] = newData.charAt(in);
						}
					}
					/*-----AKTUALIZACJA DANYCH O PLIKU----*/
					Calendar cal = Calendar.getInstance();
					inodesTable[k].month = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH );
					inodesTable[k].day = cal.get(Calendar.DAY_OF_MONTH);
					inodesTable[k].hour = cal.get(Calendar.HOUR_OF_DAY);
					inodesTable[k].minute = cal.get(Calendar.MINUTE);
					
					inodesTable[k].sizeF += newData.length();
					//closeFile(name);
					//}
				}
				else
				{
					//System.out.println("OK3");//////////////////////////
					/***************/
					int restSize = acDataSize - 32;
					//System.out.println("acDataSize "+acDataSize);
					//System.out.println("restSize "+restSize);
					int indexBlockAmount = (restSize+32-1)/32;//liczba wpis�w w bloku indeksowym
					//System.out.println("indexBlockAmount "+indexBlockAmount);
					int inDirectBlockNum3 = inodesTable[k].inode_table[1];
					//System.out.println("inDirectBlockNum3 "+inDirectBlockNum3);
					int x = (32*indexBlockAmount) - restSize; //ilosc wolnych wpisow w bloku dyskowym
					//System.out.println("x "+x);
					if((((newData.length()-x)+32-1)/32) <= FREE_BLOCK_AMOUNT)
					{
						flaga=true;
						if(newData.length() > x)
						{
							//System.out.println("OK5");
							//obliczamy ile przeznaczyc blokow dyskowych na append
							int adDriveBlocksAmount = ((newData.length()-x)+32-1)/32;
							//System.out.println("adDriveBlocksAmount "+adDriveBlocksAmount);
							
							//zapis do wolnej przestrzeni dostepnego bloku dyskowego
							int saveFrom = (int)drive[inDirectBlockNum3*32+indexBlockAmount-1];//-1, bo zapisane od zerowego indeksu
							//System.out.println("saveFrom "+saveFrom);
							int in=0;
							//System.out.println("32-x "+(32-x));
							for(int i=32-x;i<32;i++,in++)
							{
								drive[saveFrom*32+i]=newData.charAt(in);
							}
							//System.out.println("data l "+newData.length());
							//System.out.println("in "+in);
							/********************/
							for(int j=0, pom=indexBlockAmount;j<adDriveBlocksAmount;j++,pom++)
							{
								//nastepnemu indexBlockAmount przypisujemy adres wolnego bloku dyskowego
								drive[inDirectBlockNum3*32+pom] = (char)freeSpaceCheck();
								//System.out.println("nr nowego bloku dyskowego "+(int)drive[inDirectBlockNum3*32+pom]);
							}
							for(int j=0, pom=indexBlockAmount;j<adDriveBlocksAmount;j++,pom++)
							{
								int from = (int)drive[inDirectBlockNum3*32+pom];
								//System.out.println("from_tutaj "+from);
								//System.out.println("from: "+from);
								for(int i=0;in<newData.length();i++,in++)
								{
									drive[from*32+i] = newData.charAt(in);
								}
							}
						}
						else
						{	
							System.out.println("OK4");
							int saveFrom = (int)drive[inDirectBlockNum3*32+indexBlockAmount-1];//-1, bo zapisane od zerowego indeksu
							System.out.println("saveFrom "+saveFrom);
							int in3=0;
							System.out.println("32-x "+(32-x));
							for(int i=32-x;in3<newData.length();i++,in3++)
							{
								drive[saveFrom*32+i]=newData.charAt(in3);
							}
						}
						/*-----AKTUALIZACJA DANYCH O PLIKU----*/
						Calendar cal = Calendar.getInstance();
						inodesTable[k].month = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH );
						inodesTable[k].day = cal.get(Calendar.DAY_OF_MONTH);
						inodesTable[k].hour = cal.get(Calendar.HOUR_OF_DAY);
						inodesTable[k].minute = cal.get(Calendar.MINUTE);
						
						inodesTable[k].sizeF += newData.length();
						//closeFile(name);//usunac
					}
				}
				if(!flaga)
				{
					System.out.println("B�ad, brak miejsca na dysku");
				}
			}
			else
			{
				System.out.println("Plik nie jest otwarty");
			}
		}
		else
		{
			System.out.println("Nie istnieje plik o takiej nazwie");	
		}
	}
	public void readFile(String name, int amount){
		//wywo�anie operacji open()
		//ustawienie wskaznika na poczatku pliku
		
		//czytanie sekwencyjne
		//podczas odczytania wskaznik wedruje na koniec i okre�la nowa operacje wejscia-wyjscia
		//system przechowuje wskaznik czytania okreslajacy miejsce nast�pnego czytania w pliku
		
		//podamy ile plik ma zawartosci
		//u�ytkownik nie zawsze czyta ca�� zawarto��
		//wskaznik bie��cej pozycji b�dzie potrzebny, gdy b�dzie chcia� wznowi� czytanie.
		//int currentPositionPtr;
		if(catalog.containsKey(name))
		{
			FileEntry F = catalog.get(name);
			int k = F.inodeNum;
			if(inodesTable[k].stan == true)
			{
				String content = "";
				int s = inodesTable[k].sizeF;
				
				if(s <= 32)
				{
					int directBlockNum = inodesTable[k].inode_table[0];
					//System.out.println("F.currentPositionPtr "+F.currentPositionPtr);
					//System.out.println((amount>(s-F.currentPositionPtr)?s:amount+F.currentPositionPtr));
					for(int i=F.currentPositionPtr;i<(amount>(s-F.currentPositionPtr)?s:amount+F.currentPositionPtr);i++)
					{
						content += drive[directBlockNum*32+i];
					}
					if(amount >=32 ) //na ko�cu pliku
						F.currentPositionPtr = 32;
					else	
						F.currentPositionPtr += amount;
					//System.out.println("F.currentPositionPtr new "+F.currentPositionPtr);
				}
				else
				{
					int directBlockNum = inodesTable[k].inode_table[0];
					int inDirectBlockNum = inodesTable[k].inode_table[1];
					int pom = amount;
					//int restSize = s - 32;
					//liczba s�u�y do ograniczenia wpis�w nr blok�w indekowych
					//int n = (restSize+32-1)/32;
					int in=F.currentPositionPtr;
					if(in<32)
					{
						for(;in<(amount>(32-F.currentPositionPtr)?32:amount+F.currentPositionPtr);in++)
						{
							content += drive[directBlockNum*32+in];
							--pom;
						}
					}
					//wpisanie nr bloku dyskowego do bloku indeksowego
					//tyle razy 
					//int n = (a + b - 1) / b; -->ceiling
					//
					else if(amount>(32-F.currentPositionPtr) && F.currentPositionPtr < s)
					{
						int restSize = pom;
						//System.out.println("restSize "+restSize);
						int pom2;//,n;
						if(restSize > s-32)
						{
							pom=s-32;
							restSize = pom;
							//pom2=pom;
							//n=(restSize+32-1)/32;
						}
						pom2 = pom;
						//n = (restSize+32-1)/32;
						//restSize < 5?n-1:n-2)
						int j =(((F.currentPositionPtr)/32)-1);
						int z=j;//�eby tylko raz, bo j b�dzie inkrementowane
						//System.out.println("(restSize+32-1)/32="+((restSize+32-1)/32));
						//System.out.println("((F.currentPositionPtr)/32)-1="+(((F.currentPositionPtr)/32)-1));
						//System.out.println("(F.currentPositionPtr+s-restSize)/32)-1="+(((F.currentPositionPtr+s-restSize)/32)-1));
						for(;j<(restSize>s-F.currentPositionPtr?(F.currentPositionPtr+s-F.currentPositionPtr)/32:(F.currentPositionPtr+restSize)/32);j++)
						{
							//restSize>s-F.currentPositionPtr?(F.currentPositionPtr+s-F.currentPositionPtr)/32:(F.currentPositionPtr+restSize)/32
							//(((F.currentPositionPtr-restSize)/32)+1)
							//(F.currentPositionPtr+s-32-restSize)/32) dla 34
							//((F.currentPositionPtr+restSize)/32)
							//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1
							int which = (int)drive[inDirectBlockNum*32+j];
							//System.out.println("which: "+which);
							for(int i=(F.currentPositionPtr-32*(z+1));i<(pom>(s-F.currentPositionPtr)?(F.currentPositionPtr-32*(z+1))+s-F.currentPositionPtr:(F.currentPositionPtr-32*(z+1))+pom);i++)
							{
								content += drive[which*32+i];
								--pom2;
							}
							pom=pom2;
						}
						
					}
					//System.out.println("amount"+amount);
					if(amount+in >= s)
						F.currentPositionPtr = s;
					else
						F.currentPositionPtr += amount;
					//System.out.println("F.currentPositionPtr new "+F.currentPositionPtr);
				}
				//closeFile(name);
				System.out.println("content: "+content);
			}
			else
			{
				System.out.println("Plik nie jest otwarty");
			}
		}
		else
		{
			System.out.println("Nie istnieje plik o takiej nazwie");	
		}
	}
	public void deleteFile(String name){
		//sprawdzi� czy nie wyst�puje w spisie tablicy otwartych plik�w
			//je�eli wyst�puje to wywyo�a� closeFIle()
		//przeszukanie katalogu w celu odnalezienia wpisu
		//likwiduje si� wpis katalogowy
		if(catalog.containsKey(name))
		{
			FileEntry F = catalog.get(name);
			int k = F.inodeNum;
			if(inodesTable[k].stan == false)
			{
				if(--inodesTable[k].LinkCounter > 0)
				{
					catalog.remove(name);
				}
				else
				{
					int indexAmount = inodesTable[k].sizeF > 32 ? 2:1;
					
					int directBlockNum,inDirectBlockNum;
					
					//System.out.println(indexAmount);
					if(indexAmount == 1)
					{
						directBlockNum = inodesTable[k].inode_table[0];
						Arrays.fill(drive, directBlockNum*32, directBlockNum*32+32, (char)0);		
						bitVector[directBlockNum] = 1;
						++FREE_BLOCK_AMOUNT;
						//F.inodeNum--;
						inodesTable[k]=null;
		
						catalog.remove(name);
					}
					else if(indexAmount == 2)
					{
						directBlockNum = inodesTable[k].inode_table[0];
						Arrays.fill(drive, directBlockNum*32, directBlockNum*32+32, (char)0);		
						bitVector[directBlockNum] = 1;
						++FREE_BLOCK_AMOUNT;
						
						inDirectBlockNum = inodesTable[k].inode_table[1];
						int pom=0;
						while((int)drive[inDirectBlockNum*32+pom] != 65535)//65535--> -1 z char to int :/
						{
							int from = (int)drive[inDirectBlockNum*32+pom];
							//System.out.println("from_del:" +from);
							Arrays.fill(drive, from*32, from*32+32, (char)0);
							++FREE_BLOCK_AMOUNT;
							pom++;
						}
						Arrays.fill(drive, inDirectBlockNum*32, inDirectBlockNum*32+32, (char)0);
						++FREE_BLOCK_AMOUNT;
						
						inodesTable[k]=null;
						catalog.remove(name);
					}
				}
				System.out.println("Plik zosta� usuni�ty");
			}
			else
			{
				System.out.println("Plik jest wykorzystywany! Nie mo�na go teraz usun��");
			}
		}
		else
		{
			System.out.println("Plik o tej nazwie nie istnieje");
		}
	}
	public void renameFile(String name, String newName){
		if(catalog.containsKey(name)){
			catalog.get(name).name = newName;
			catalog.put(newName, catalog.remove(name));
		}
		else
			System.out.println("Plik o tej nazwie nie istnieje");
	}
	public void createLink(String newName, String name){
		if(catalog.containsKey(name)){
			if(!catalog.containsKey(newName)){
				System.out.println("OK");
				FileEntry newF = new FileEntry();
				FileEntry F = catalog.get(name);
				newF.name = newName;
				newF.inodeNum = F.inodeNum;
				newF.currentPositionPtr = F.currentPositionPtr;
				inodesTable[F.inodeNum].LinkCounter++;
				//semafor pozostaje bez zmian i tymczasowe pole stan r�wnie�
				newF.type_of_file = Types.LINK;
				catalog.put(newName,newF);
				//pami�te� o odycji delete
				//i o zmianie nazwy
			}
			else
			{
				System.out.println("Plik o tej nazwie ju� istnieje. Nie mo�na utworzy� dowi�zania!");
			}
		}
		else
		{
			System.out.println("Plik o tej nazwie nie istnieje");
		}
	}
//	public void unlinkFile(String location){//int inode, String name, String ext){
//
//		//usuwa dowi�zania do pliku
//	}
//	/*FUNCKCJE KATALOGU*/
////	public boolean searchFile(String name, String ext){
////		
////		//sprawdzamy czy plik o podanej nazwie wystpeuje w spisie wpisow katalogowych
////		//
////		return false;
////	}
	private String timView(int t){
		if (t >= 10)
			return Integer.toString(t);
		else
			return "0"+Integer.toString(t);
	}
	//wypisz zawarto�� katalogu
	public void ListDirectory(){
		//number of hard links, owner, size, last-modified date and filename
		System.out.println("Directory of root: ");
		for(Map.Entry<String, FileEntry> entry : catalog.entrySet()){
			FileEntry F = entry.getValue();
			int k = F.inodeNum;
			System.out.print(inodesTable[k].month+" "+timView(inodesTable[k].day));
			System.out.print(" "+timView(inodesTable[k].hour)+":"+timView(inodesTable[k].minute));
			System.out.print(" "+inodesTable[k].sizeF+"B");
			System.out.print(" "+entry.getKey());
			System.out.print(" "+F.type_of_file);
			//System.out.print(" nr "+k);
			System.out.println();
		}
	}
	/*----POMOCNICZE FUNKCJE----*/
	
	public void printBitVector(){
		for(int i=0;i < bitVector.length;i++){
			System.out.println("["+i+"]=" + bitVector[i]);
		}
	}
	public void printDrive(){
		for(int i=0;i < drive.length;i++){
			System.out.println("["+i+"]="+drive[i]);
		}
	}
	public void printDiskBlock(int nr){
		System.out.println("Blok dyskowy nr: "+nr);
		if(nr<=32 && nr >= 0)
		{
			for(int i=nr*32;i<(nr*32+32);i++)
				System.out.println("["+i+"]="+drive[i]);
		}
		else
		{
			System.out.println("Numer poza zakresem");
		}
	}
	public void printInodeInfo(String name){
		if(catalog.containsKey(name)){
			FileEntry F = catalog.get(name);
			int k = F.inodeNum;
			System.out.println(name + " I-NODE INFO:");
			System.out.print(">"+inodesTable[k].month+" "+timView(inodesTable[k].day));
			System.out.print(" "+timView(inodesTable[k].hour)+":"+timView(inodesTable[k].minute));
			System.out.print(" "+inodesTable[k].sizeF+"B");
			System.out.println(" "+name);
			System.out.println(">I-node nr: "+k);
			//System.out.println(">Type: "+inodesTable[k].type_of_file);
			System.out.println(">LinkCounter: "+inodesTable[k].LinkCounter);
			System.out.println(">I-node table:\n >[0]->blok dyskowy: "+inodesTable[k].inode_table[0]);
			int l = inodesTable[k].inode_table[1] == -1?-1:1;
			System.out.println(" >[1]->blok indeksowy: "+
					(
						inodesTable[k].inode_table[1] == -1?"brak"
						:
						inodesTable[k].inode_table[1]
					));
			System.out.println();
		}
		else
		{
			System.out.println("Nie ma pliku o podanej nazwie");
		}
	}
}






