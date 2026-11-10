# Widgets Sergi

Tres widgets d'Android en català:

- **Calendari (4×2)**: flip del dia, setmana de dilluns a diumenge, festius en colors.
  Colors: vermell = Estat, blau = Catalunya, lila = Barcelona (capital), verd = local (Tarragona i Reus).
  Toca ‹ › per canviar de mes, el títol per tornar a avui, la resta obre el calendari.
- **Rellotge GMT (2×2)**: números de 24 h amb cercle fi blau/vermell, agulla GMT amb zona configurable,
  finestreta de data, marca FWCo i busca de segons un cop per segon (Android 12+).
- **Rellotge GMT minimal (2×2)**: només els números de 24 h i agulles de segment.

La zona "Barcelona" és `Europe/Madrid` (mateixa hora) però amb nom de Barcelona.

## Dues versions

| Versió | Fitxer | Festes locals |
|---|---|---|
| sergi | `app-sergi-debug.apk` | Tarragona i Reus (+ Barcelona com a capital, en lila) |
| lluis | `app-lluis-debug.apk` | Barcelona (en verd) |

Tot el que no sigui festius i textos és comú. Les diferències són a `app/src/lluis/`.
Les dues tenen identificador diferent (`...widgets` i `...widgets.lluis`), així que es poden instal·lar al mateix mòbil.

## Compilar amb GitHub

1. Puja tot el contingut al repositori (inclosa la carpeta oculta `.github`; si no la puja,
   crea a mà `.github/workflows/build.yml` amb el contingut de `build.yml`).
2. **Actions** → "Compila l'APK" (s'executa sola en pujar; també amb *Run workflow*).
3. Els dos APK queden a **Releases** i a l'artefacte **widgets-apk**.
   L'APK es signa amb la mateixa clau (`app/debug.keystore`): les versions noves
   s'instal·len a sobre sense desinstal·lar.

## Festius

Són a `app/src/main/assets/holidays.json` (versió Sergi) i `app/src/lluis/assets/holidays.json` (Lluís).

```json
{ "date": "2027-05-17", "name": "Dilluns de Pasqua Granada", "level": "capital", "place": "Barcelona", "provisional": true }
```

`level`: `state`, `catalonia`, `capital` o `local`. Els `provisional` surten amb `*`.
Es marquen tots els festius, també si cauen en diumenge.
Pendents: locals 2027 de Tarragona i Reus, confirmar les de Barcelona 2027, i tot el 2028.

## Ajustos ràpids

- Colors del calendari: `CalendarRenderer.kt` (`C_STATE`, `C_CATALONIA`, `C_CAPITAL`, `C_LOCAL`).
- Colors del bisell i marca: `ClockRenderer.kt`.
- Cantonades: `CORNER` als rellotges i `0.05f` al calendari.
