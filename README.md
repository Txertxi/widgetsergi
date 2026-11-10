# Widgets Sergi

Dos widgets d'Android en català:

- **Calendari (4×2)**: flip del dia, setmana de dilluns a diumenge, festius en colors
  (vermell = Estat, blau = Catalunya, verd = local de Barcelona, Tarragona, Reus i Salou).
  Toca ‹ › per canviar de mes, el títol per tornar a avui, la resta obre el calendari.
- **Rellotge GMT (2×2)**: bisell de 24 h, agulla GMT amb zona configurable (per defecte UTC),
  finestreta de data i busca de segons que avança un cop per segon (Android 12+).

## Compilar amb GitHub

1. Crea un repositori nou (p. ex. `widgets-sergi`) i puja-hi tot el contingut d'aquesta carpeta
   (inclosa la carpeta oculta `.github`).
2. Ves a **Actions** → "Compila l'APK" (s'executa sola en pujar; també amb *Run workflow*).
3. Quan acabi, baixa l'artefacte **widgets-sergi-apk**, descomprimeix-lo i instal·la l'APK al mòbil.
   L'APK es signa sempre amb la mateixa clau (`app/debug.keystore`), així que les versions noves
   s'instal·len a sobre sense desinstal·lar.

## Afegir-los

Premut llarg a la pantalla d'inici → Widgets → "Widgets Sergi".
Per canviar la zona horària del rellotge: premut llarg al widget → Reconfigura
(el nom exacte depèn del llançador).

## Festius

Estan a `app/src/main/assets/holidays.json` (2026 i 2027). Format:

```json
{ "date": "2027-05-17", "name": "Dilluns de Pasqua Granada", "level": "local", "place": "Barcelona", "provisional": true }
```

`level` pot ser `state`, `catalonia` o `local`. Els marcats `provisional` surten amb un `*`.
Pendents: festes locals 2027 de Tarragona i Reus, i confirmar les de Barcelona i Salou
quan es publiqui l'ordre del Departament d'Empresa i Treball; i tot el 2028.
Els noms "Festa local" de Reus i Salou són genèrics: posa-hi el nom real si el vols.

## Ajustos ràpids

- Colors del calendari: `CalendarRenderer.kt` (constants `C_STATE`, `C_CATALONIA`, `C_LOCAL`).
- Colors del bisell: `ClockRenderer.kt` (`NIGHT`, `DAY`, `AMBER`).
- Agulles: `app/src/main/res/drawable/hand_*.xml`.
