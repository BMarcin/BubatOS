package Memory_PC;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

public class PageTab {
	byte[] tab; // tablica numerów stron, w których s¹ dane procesu
	int size;
	String fileName;

	// Poni¿sze 3 zmienne s³u¿¹ do wyci¹gania komend
	int[][] jumpedIn = new int[4][2];
	int lastCommand = -1;
	byte comP = 0;
	byte comD = 0;

	public char[] getProcessMemory() {
		char[] ret = new char[size];
		for (byte i = 0; i != size; ++i) {
			ret[i] = Memory.read(tab[i/16], (byte) (i%16));
		}
		return ret;
	}

	public String getFileName() {
		return fileName;
	}

	public int getSize() {
		return size;
	}

	public PageTab(String fileName, int size) throws IOException {
		if (fileName == "") {
			this.fileName="";
			this.size = size;
			char[] data = new char[] {0};
			tab = MassMemory.load(data);
			return;
		}
		this.fileName = fileName;
		this.size = size;
		FileReader Rr = new FileReader(fileName);
		BufferedReader BRr = new BufferedReader(Rr);
		String str = "";
		String line;
		while ((line = BRr.readLine()) != null) {
			if (line != "HX" && line != "hx") {
				line += "\n";
			}
			str += line;
		}
		BRr.close();
		Rr.close();
		char data[] = str.toCharArray();
		char[] data2 = new char[size];
		for (short i = 0; i != data.length; ++i) {
			data2[i] = str.charAt(i);
		}
		for (short i = (short) data.length; i != size; ++i) {
			data2[i] = 0;
		}
		tab = MassMemory.load(data2);
	}

	public void finalize() {
		MassMemory.clear(tab);
	}

	public void save() throws IOException {
		int i = 0;
		FileWriter Wr = new FileWriter(fileName);
		while (i != size) {
			Wr.write(Memory.read(tab[i / 16], (byte) (i % 16)));
			++i;
		}
		Wr.close();
	}

	// funkcja zwracaj¹ca komendê o numerze n
	public Vector<String> getCommand(int n) {
		Vector<String> ret = new Vector<String>();
		if (++lastCommand != n) {
			lastCommand = 0;
			for (comP = 0;; ++comP) {
				if (lastCommand == n) {
					if (comD == 16) {
						comD = 0;
						++comP;
					}
					break;
				}
				for (comD = 0; comD != 16; ++comD) {
					if (lastCommand == n) {
						if (comD == 16) {
							comD = 0;
							++comP;
						}
						break;
					}
					// System.out.println("comP="+comP+" comD="+ comD);
					char c = Memory.read(tab[comP], comD);
					if (c == 10) {
						++lastCommand;
					}
				}
				if (lastCommand == n) {
					if (comD == 16) {
						comD = 0;
						++comP;
					}
					break;
				}
			}
		}
		String str = "";
		while (true) {
			for (; comD != 16; comD++) {
				if (str == "HX" || str == "hx") {
					ret.add(str);
					comD++;
					if (comD > 15) {
						++comP;
						comD -= 16;
					}
					return ret;
				}
				char c = Memory.read(tab[comP], comD);
				if (c == 10) {
					ret.add(str);
					comD++;
					if (comD > 15) {
						++comP;
						comD -= 16;
					}
					return ret;
				} else if (c == 32) {
					ret.add(str);
					str = "";
				} else {
					str += c;
				}
			}
			comD = 0;
			++comP;
		}
	}

	// Pobiera komendê spod podanego adresu logicznego
	public Vector<String> getCommandFromAdress(int adr) {
		Vector<String> ret = new Vector<String>();
		lastCommand = 0;
		for (comP = 0;; ++comP) {
			if ((comP * 16 + comD) == adr) {
				if (comD == 16) {
					comD = 0;
					++comP;
				}
				break;
			}
			for (comD = 0; comD != 16; ++comD) {
				if ((comP * 16 + comD) == adr) {
					if (comD == 16) {
						comD = 0;
						++comP;
					}
					break;
				}
				// System.out.println("comP="+comP+" comD="+ comD);
				char c = Memory.read(tab[comP], comD);
				if (c == 10) {
					++lastCommand;
				}
			}
			if ((comP * 16 + comD) == adr) {
				if (comD == 16) {
					comD = 0;
					++comP;
				}
				break;
			}
		}
		String str = "";
		while (true) {
			for (; comD != 16; comD++) {
				if (str == "HX" || str == "hx") {
					ret.add(str);
					comD++;
					if (comD > 15) {
						++comP;
						comD -= 16;
					}
					return ret;
				}
				char c = Memory.read(tab[comP], comD);
				if (c == 10) {
					ret.add(str);
					comD++;
					if (comD > 15) {
						++comP;
						comD -= 16;
					}
					return ret;
				} else if (c == 32) {
					ret.add(str);
					str = "";
				} else {
					str += c;
				}
			}
			comD = 0;
			++comP;
		}
	}

	// metoda read w wersji zwracaj¹cej String
	public String readString(int ad, int amount) throws Exception {
		return new StringBuilder().append(read(ad, amount)).toString();
	}

	// metoda write w akceptuj¹ca dane w formie String
	public void write(int ad, String data) throws Exception {
		write(ad, data.toCharArray());
	}

	// metoda odczytuj¹ca amount znaków zaczynaj¹c od adresu ad
	public char[] read(int ad, int amount) throws Exception {
		if (ad + amount > size) {
			throw new Exception("Poza zakresem");
		}
		if (ad + amount >= tab.length * 16) { // Gdy odwo³ano siê do znaku o zbyt du¿ym adresie
			return null;
		}
		char[] ret = new char[amount];
		if ((ad % 16) + amount > 32) { // odczytywanie z trzech stron
			byte p = tab[ad / 16];
			byte d = (byte) (ad % 16);
			byte n = (byte) (16 - d);
			char[] part = Memory.read(p, d, n);
			byte re = 0;
			for (byte i = 0; i < n; ++i) {
				ret[i] = part[i];
			}
			re += n;
			p = tab[ad / 16 + 1];
			d = 0;
			n = 16;
			part = Memory.read(p, d, n);
			for (byte i = 0; i < n; ++i) {
				ret[re + i] = part[i];
			}
			re += n;
			p = tab[ad / 16 + 2];
			d = 0;
			n = (byte) (amount - re);
			part = Memory.read(p, d, n);
			for (byte i = 0; i < n; ++i) {
				ret[re + i] = part[i];
			}
		} else if ((ad % 16) + amount > 16) { // odczytywanie z dwóch stron
			byte p = tab[ad / 16];
			byte d = (byte) (ad % 16);
			byte n = (byte) (16 - d);
			char[] part = Memory.read(p, d, n);
			byte re = 0;
			for (byte i = 0; i < n; ++i) {
				ret[i] = part[i];
			}
			re += n;
			p = tab[ad / 16 + 1];
			d = 0;
			n = (byte) (amount - re);
			part = Memory.read(p, d, n);
			for (byte i = 0; i < n; ++i) {
				ret[re + i] = part[i];
			}
		} else { // odczytywanie z jednej strony
			byte p = tab[ad / 16];
			byte d = (byte) (ad % 16);
			byte n = (byte) (amount);
			char[] part = Memory.read(p, d, n);
			for (byte i = 0; i < n; ++i) {
				ret[i] = part[i];
			}
		}
		return ret;
	}

	// metoda zapisuj¹ca znaki data zaczynaj¹c od adresu ad
	public void write(int ad, char[] data) throws Exception {
		if (ad + data.length > size) {
			throw new Exception("Poza zakresem");
		}
		if (ad + data.length >= tab.length * 16) { // Gdy odwo³ano siê do znaku o zbyt du¿ym adresie
			throw new Exception("Poza zakresem");
		}
		if ((ad % 16) + data.length > 32) { // zapisywanie na trzech stronach
			byte p = tab[ad / 16];
			byte d = (byte) (ad % 16);
			byte n = (byte) (16 - d);
			char[] part = new char[n];
			for (byte i = 0; i < n; ++i) {
				part[i] = data[i];
			}
			Memory.write(p, d, part);
			byte wr = 0;
			wr += n;
			p = tab[ad / 16 + 1];
			d = 0;
			n = 16;
			part = new char[n];
			for (byte i = 0; i < n; ++i) {
				part[i] = data[wr + i];
			}
			Memory.write(p, d, part);
			wr += n;
			p = tab[ad / 16 + 2];
			d = 0;
			n = (byte) (data.length - wr);
			part = new char[n];
			for (byte i = 0; i < n; ++i) {
				part[i] = data[wr + i];
			}
			Memory.write(p, d, part);
		} else if ((ad % 16) + data.length > 16) { // zapisywanie na dwóch stronach
			byte p = tab[ad / 16];
			byte d = (byte) (ad % 16);
			byte n = (byte) (16 - d);
			byte wr = 0;
			char[] part = new char[n];
			for (byte i = 0; i < n; ++i) {
				part[i] = data[i];
			}
			Memory.write(p, d, part);
			wr += n;
			p = tab[ad / 16 + 1];
			d = 0;
			n = (byte) (data.length - wr);
			part = new char[n];
			for (byte i = 0; i < n; ++i) {
				part[i] = data[wr + i];
			}
			Memory.write(p, d, part);
		} else { // zapisywanie na jednej stronie
			byte p = tab[ad / 16];
			byte d = (byte) (ad % 16);
			byte n = (byte) (data.length);
			char[] part = new char[n];
			for (byte i = 0; i != n; ++i) {
				part[i] = data[i];
			}
			Memory.write(p, d, part);
		}
	}
}