# Widgets Sergi

Tres widgets d'Android en català:

- **Calendari (4×2)**: flip del dia, setmana de dilluns a diumenge, festius en colors.
  Colors: vermell = Estat, blau = Catalunya, lila = Barcelona (capital), verd = local (Tarragona i Reus).
  Toca ‹ › per canviar de mes, el títol per tornar a avui, la resta obre el calendari.
- **Rellotge GMT (2×2)**: com un GMT de polsera. L'agulla taronja marca l'hora local en 24 h sobre una
  esfera fixa invisible (24 a dalt, 12 a baix); els números de 24 h i el cercle fi blau/vermell giren segons la
  zona escollida. Marca FWCo a les 12, nom de la zona a les 6, finestreta de data i busca de segons
  un cop per segon (Android 12+).
- **Rellotge GMT minimal (2×2)**: només els números de 24 h i agulles de segment.

La zona "Barcelona" és `Europe/Madrid` (mateixa hora) però amb nom de Barcelona.

## Tres versions

| Versió | Fitxer | Festes locals |
|---|---|---|
| sergi | `app-sergi-debug.apk` | Tarragona i Reus (+ Barcelona com a capital, en lila) |
| lluis | `app-lluis-debug.apk` | Barcelona (en verd) |
| joan | `app-joan-debug.apk` | Castelldefels i Cardona (+ Barcelona com a capital, en lila) |

Tot el que no sigui festius i textos és comú. Les diferències són a `app/src/lluis/` i `app/src/joan/`.
Tenen identificador diferent (`...widgets`, `...widgets.lluis` i `...widgets.joan`), així que es poden instal·lar al mateix mòbil.

## Compilar amb GitHub

1. Puja tot el contingut al repositori (inclosa la carpeta oculta `.github`; si no la puja,
   crea a mà `.github/workflows/build.yml` amb el contingut de `build.yml`).
2. **Actions** → "Compila l'APK" (s'executa sola en pujar; també amb *Run workflow*).
3. Els tres APK queden a **Releases** i a l'artefacte **widgets-apk**.
   L'APK es signa amb la mateixa clau (`app/debug.keystore`): les versions noves
   s'instal·len a sobre sense desinstal·lar.

## Festius

Són a `app/src/main/assets/holidays.json` (versió Sergi), `app/src/lluis/assets/holidays.json` (Lluís) i `app/src/joan/assets/holidays.json` (Joan).

```json
{ "date": "2027-05-17", "name": "Dilluns de Pasqua Granada", "level": "capital", "place": "Barcelona", "provisional": true }
```

`level`: `state`, `catalonia`, `capital` o `local`. Els `provisional` surten amb `*`.
Es marquen tots els festius, també si cauen en diumenge.
Pendents: locals 2027 de Tarragona, Reus, Castelldefels i Cardona (l'ordre de la Generalitat es publica cap al desembre),
confirmar les de Barcelona 2027, i tot el 2028.

Locals 2026 de la versió Joan: Castelldefels 14 d'agost i 7 de desembre, Cardona 14 i 15 de setembre
(Ordre EMT/208/2025 modificada per l'Ordre EMT/3/2026).

## Ajustos ràpids

- Colors del calendari: `CalendarRenderer.kt` (`C_STATE`, `C_CATALONIA`, `C_CAPITAL`, `C_LOCAL`).
- Colors del bisell i marca: `ClockRenderer.kt`.
- Cantonades: `CORNER` als rellotges i `0.05f` al calendari.
