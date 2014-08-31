Szyfrowanie plików na potrzeby GIIF
===================================

Ten program służy do podpisywania i szyfrowania  plików na potrzeby GIIF,
W odróżnieniu od oficjalnego oprogramowania [SecureFile] pozwala on na 
podpisywanie i szyfrowanie plików większych niż 193MB, a także wprowadza
nowy, kompaktowy format pliku.

Stary format pliku
------------------

Do października 2014 roku GIIF obsługiwał wyłącznie jeden format pliku.
W tym formacie plik przesyłany do GIIF jest opakowywany w serię kopert.

Pierwsza koperta to wiadomość CMS typu signed-data. Przesyłany plik jest w niej
przechowywany jako element "data content type". Ta koperta zawiera podpis
elektroniczny złożony na dokumencie, oraz certyfikat użyty do podpisu.
W kopercie może znajdować się podpisany atrybut signing-time, ale jego wartość
jest ignorowana (dla plików przysłanych pocztą elektroniczną brany jest pod
uwagę czas wejścia pliku na pierwszy serwer pocztowy ministerstwa, a w przypadku
plików przesyłanych przez WWW czas rozpoczęcia przesyłania pliku). Plik, który
jest przekazwany może być zakodowany jako DER lub BER.  
Ta koperta powoduje przyrost pliku wynikowego o kilkaset bajtów.

Druga koperta to S/MIME, które opakowuje binarne dane z podpisanym plikiem.
Ta koperta powoduje przyrost pliku o 34%.

Trzecia koperta to wiadomość CMS typu enveloped-data. Plik wynikowy z drugiej
koperty jest w niej przechowywany jako element "data content type". Ta koperta
jest wykorzystywana do szyfrowania podpisanych danych z użyciem certyfikatu
GIIF. Do szyfrowania symetrycznego używany jest algorytm 3DES-EDE w trybie CDC.
Ta koperta powoduje przyrost pliku wynikowego o kilkaset bajtów w porównaniu
do pliku wynikowego z koperty drugiej. Szyfrowanie dodatkowo sprawia, że dobrze
kompresowalne dane o transakcjach stają się nie kompresowalne.

Czwarta i ostatnia koperta to S/MIME które opakowuje binarne zaszyfrowane dane.
Ta koperta powoduje przyrost pliku o kolejne 34%.

W rezultacie dwóch niepotrzebnych konwersji pliku binarnego do formatu S/MIME,
plik rośnie o 77%, co jest duży problemem w przypadku wykazów transakcji
z banków.

Nowy format pliku
-----------------

Od października 2014 GIIF obsługuje także nowy format pliku. W tym formacie
plik przesyłany jest również opakowywany w serię kopert.

Pierwsza koperta to CMS typu signed-data, identyczny jak w starym formacie
pliku.

Druga, opcjonalna koperta, to kompresja zgodnie z [RFC-3274][RFC3274]. Wynik
pierwszej koperty jest w niej przechowywany jako signed-data. Dane transakcji
dobrze kompresują się z użyciem algorytmu zLib (nawet do 10% oryginalnego
rozmiaru pliku).

Kolejna, tym razem obowiązkowa koperta, to CMS typu enveloped-data. Przechowuje
ona plik wynikowy z pierwszej koperty jako signed-data, lub z drugiej koperty
jako compressed-data, jest ona wykorzystywana do szyfrowania popisanych
(i opcjonalnie skompresowanych) danych z użyciem certyfikatu GIIF.
Do szyfrowania symetrycznego wykorzystywany jest algorytm 3DES-EDE lub AES128,
oba w trybie CDC.



[SecureFile]: https://www.giif.mofnet.gov.pl/giif/oprogramowanie/SecureFile.exe
[RFC3274]: http://tools.ietf.org/html/rfc3274 "RFC-3274"