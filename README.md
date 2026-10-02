# 🛡️ localhost-gildie

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Minecraft](https://img.shields.io/badge/Minecraft-1.20.4-brightgreen.svg)
![Engine](https://img.shields.io/badge/Server-Paper%20%7C%20Spigot%20%7C%20Purpur-blue.svg)
![Build](https://img.shields.io/badge/Build-Maven-red.svg)
![Author](https://img.shields.io/badge/Author-Localhost-purple.svg)

Nowoczesny, lekki i w 100% zoptymalizowany **System Gildii (Factions / Wojny Gildii)** napisany od zera z myślą o serwerach Minecraft 1.20+ (Survival, Gildie, Anarchia, Hardcore, PvP).

Zaprojektowany tak, jak powinien wyglądać wzorcowy plugin na gildie: **bez zbędnego bloatware'u, bez skomplikowanych zależności**, z czytelnym kodem, płynną ochroną cuboidów, Sersem Gildii, systemem żyć, rankingiem ELO, menu GUI oraz dynamicznym paskiem ActionBar.

---

## ✨ Kluczowe Funkcje

- **Zakładanie i Zarządzanie Gildią**:
  - Tworzenie gildii (`/g zaloz <tag> <nazwa>`) z pobieraniem wymaganych surowców (diamenty, szmaragdy, złoto, żelazo).
  - Weryfikacja unikalności tagu, długości oraz odległości od spawnu i innych gildii (zapobieganie nachodzeniu na siebie terenów).
- **Serce Gildii i System Żyć**:
  - W centrum terenu stawiane jest **Serce Gildii** (Płaczący Obsydian).
  - Każda gildia posiada **3 życia**. Aby podbić wrogą gildię, intruzi muszą odnaleźć i zaatakować jej serce!
  - Przy każdym ataku członkowie gildii otrzymują dźwiękowy alarm o najezdzie.
- **Ochrona Terenu (Cuboid Claim)**:
  - Automatyczny claim o promieniu np. 35 bloków (teren **70x70**).
  - Blokada niszczenia, stawiania bloków oraz otwierania pojemników (skrzynki, beczki, leje, piece, kowadła, shulkery) dla obcych graczy.
  - Powiadomienia na pasku ActionBar przy przekraczaniu granicy terytorium.
- **Baza i Bezpieczna Teleportacja**:
  - `/g baza` (lub `/g dom`) – teleportacja do bazy z 5-sekundowym odliczaniem, która zostaje przerwana w przypadku poruszenia się lub otrzymania obrażeń.
  - `/g ustawbaze` – ustawienie nowej pozycji bazy na własnym terenie.
- **Friendly Fire (PvP w Gildii)**:
  - `/g pvp` – Lider lub Zastępca może jednym poleceniem przełączać możliwość bicia się między członkami gildii.
- **Panel Graficzny GUI (`/g panel` lub `/g`)**:
  - Intuicyjne menu z podglądem statystyk gildii, koordynatów, liczby członków, punktów ELO oraz przyciskiem do szybkiej teleportacji i zmiany PvP.
- **Ranking ELO i Punkty (`/g top`)**:
  - Startowe 1000 punktów ELO.
  - Zabicie członka wrogiej gildii dodaje punkty zwycięzcy i odejmuje przegranemu, tworząc dynamiczną rywalizację.
- **Komendy Administracyjne (`/ga`)**:
  - Zarządzanie życiami, punktami ELO, natychmiastowa teleportacja do serca dowolnej gildii oraz usuwanie nieaktywnych frakcji.

---

## 💻 Komendy i Uprawnienia

### Komendy Graczy:
Główna komenda: `/gildia` (aliasy: `/g`, `/gildie`, `/guild`)

| Komenda | Opis |
| :--- | :--- |
| `/g` lub `/g panel` | Otwiera graficzny panel zarządzania gildią (GUI) |
| `/g zaloz <tag> <nazwa>` | Zakłada nową gildię w aktualnym miejscu |
| `/g baza` | Rozpoczyna teleportację do bazy gildii |
| `/g ustawbaze` | Ustawia nową pozycję bazy (wymaga lidera/zastępcy) |
| `/g zapros <gracz>` | Wysyła zaproszenie do wybranego gracza |
| `/g dolacz <tag>` | Dołącza do zaproszonej gildii |
| `/g wyrzuc <gracz>` | Wyrzuca członka z gildii |
| `/g opusc` | Opuszcza obecną gildię |
| `/g rozwiaz` | Bezpowrotnie usuwa gildię (wymaga lidera) |
| `/g lider <gracz>` | Przekazuje rangę lidera innemu członkowi |
| `/g pvp` | Przełącza Friendly Fire wewnątrz gildii |
| `/g info [tag]` | Wyświetla szczegółowe informacje o wybranej gildii |
| `/g top` | Wyświetla ranking TOP 10 najlepszych gildii na serwerze |

### Komendy Administracyjne:
Główna komenda: `/gadmin` (alias: `/ga`)

| Komenda | Opis |
| :--- | :--- |
| `/ga usun <tag>` | Wymusza natychmiastowe usunięcie gildii i jej terenu |
| `/ga tp <tag>` | Teleportuje administratora bezpośrednio do serca gildii |
| `/ga punkty <tag> <set/add/take> <ilość>` | Zmienia punkty ELO wybranej gildii |
| `/ga zycia <tag> <set/add/take> <ilość>` | Zmienia liczbę żyć gildii |
| `/ga reload` | Przeładowuje plik konfiguracyjny `config.yml` |

---

## 📂 Struktura Projektu

```text
localhost-gildie/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── pl/localhost/gildie/
        │       ├── Main.java                        # Główna klasa pluginu (Main)
        │       ├── commands/
        │       │   ├── GuildAdminCommand.java       # Komendy administracyjne /gadmin
        │       │   └── GuildCommand.java            # Komendy graczy /gildia i TabCompleter
        │       ├── gui/
        │       │   └── GuildPanelGUI.java           # Menu GUI panelu gildii
        │       ├── listeners/
        │       │   └── GuildListener.java           # Ochrona cuboidów, ataki na serce, Friendly Fire i ELO
        │       ├── managers/
        │       │   ├── ConfigManager.java           # Menadżer konfiguracji i komunikatów
        │       │   ├── GuildManager.java            # Główny silnik gildii i zapis do gildie.yml
        │       │   └── TeleportManager.java         # Zegar odliczania do teleportacji (/g baza)
        │       └── objects/
        │           └── Guild.java                   # Model gildii: teren, serce, lider, członkowie, ELO
        └── resources/
            ├── config.yml                           # Ustawienia promienia, itemów na gildię i wiadomości
            └── plugin.yml                           # Metadane pluginu
```

---

## 🛠️ Jak Zbudować (Kompilacja)

### Wymagania:
- **Java 17** lub nowsza
- **Maven 3.8+**

### Kompilacja:
```bash
cd localhost-gildie
mvn clean package
```

Zbudowany plik `.jar` pojawi się w folderze `target/`:
```text
target/localhost-gildie-1.0-SNAPSHOT.jar
```

---

## ⚙️ Wymagania i Zależności

- **Zależności:** Brak dodatkowych pluginów! Działa w 100% natywnie (*standalone*) na **Paper**, **Purpur** oraz **Spigot 1.20+**.
- **Kompatybilność:** Java 17+, Paper 1.20 - 1.20.4+.

---

## 👤 Autor

- **Autor:** Localhost
- **Discord:** `localhost_127001`
- **Projekt:** Seria pluginów serwerowych `localhost-*`
