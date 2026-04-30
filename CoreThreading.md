# 🛡️ APS Architektur-Regelwerk & Threading-Guidelines

Diese Richtlinien definieren, wie der APS-Kern (`APSCore`) und die Fassade (`APS`) interagieren, um rechnerische Korrektheit und technische Thread-Sicherheit zu garantieren.

## 1. Separation of Concerns (Aufgabentrennung)

### **APSCore (Der Rechenkern)**
*   **Zweck:** Enthält den gesamten veränderlichen Zustand (`rollingHistory`, `currentBg`) und medizinische Algorithmen.
*   **Threading-Regel:** Der Kern ist **thread-agnostisch**. Er darf keine eigenen Scopes (`launch`, `async`) verwalten oder Dispatcher wechseln.
*   **Annahme:** Jede Methode im Kern geht davon aus, dass sie bereits in einer serialisierten (single-threaded) Umgebung aufgerufen wird.
*   **Sichtbarkeit:** Logik-Methoden sollten standardmäßig `internal` oder `private` sein.

### **APS (Die Fassade)**
*   **Zweck:** Dient als einziger Eintrittspunkt (API) für die App und verwaltet den Lebenszyklus der Plugins.
*   **Threading-Hoheit:** Verwaltet den `apsDispatcher` (Single-Threaded) und den `apsScope`.
*   **API-Regel:** Jede öffentliche Methode der Fassade muss die Ausführung sofort in den `apsScope` delegieren.
*   **Serialisierung:** Sie stellt sicher, dass Aufrufe an den `APSCore` nacheinander (sequentiell) erfolgen.

---

## 2. Das "Goldene Thread" Prinzip

Alle APS-Berechnungen laufen auf einem einzigen dedizierten Hintergrund-Thread (`SingleThreadExecutor`). 
*   **Vorteil:** Keine Race-Conditions auf komplexen Datenstrukturen wie dem Ringpuffer.
*   **Vorteil:** Kein Overhead durch Sperrmechanismen wie `synchronized` innerhalb der Rechenlogik.

---

## 3. Verwendung von Mutexen

Obwohl wir einen Single-Thread-Dispatcher nutzen, ist eine `Mutex` in der Fassade notwendig, wenn Operationen über **Suspend-Points** hinweg atomar bleiben müssen.

### **Die "Suspend-Falle"**
Wenn eine Coroutine im `apsScope` suspendiert (z. B. für einen Datenbank-Schreibvorgang), wird der Thread frei. In dieser Zeit könnte eine andere Coroutine (z. B. ein neuer BZ-Wert) auf demselben Thread starten und den Zustand im Kern ändern, bevor die erste Coroutine fortgesetzt wird.

**Regel:** 
Nutze `stateMutex.withLock { ... }` in der `APS`-Fassade für alle Operationen, die:
1.  Den Zustand im `APSCore` ändern **UND**
2.  Mindestens einen `suspend`-Aufruf (DB, Preferences, etc.) tätigen.

---

## 4. Datenfluss & Beobachtbarkeit

*   **Eingehend (Command):** Daten fließen über Methodenaufrufe an der `APS`-Klasse ein (Fire-and-Forget).
*   **Ausgehend (Query/Observe):** Ergebnisse werden ausschließlich über `StateFlow` nach außen gereicht.
*   **Observer-Pattern:** Der `APSCore` aktualisiert private `MutableStateFlows`. Die Fassade exponiert diese als schreibgeschützte `StateFlows`. Dies ermöglicht der UI ein thread-sicheres Mitlesen, ohne den Rechenkern zu blockieren.

---

## 5. Checkliste für Änderungen

1.  **Medizinische Logik / Zustandsänderung?** -> Implementierung im `APSCore`.
2.  **Eintrittspunkt von außen?** -> Methode in `APS` hinzufügen, die in den `apsScope` wechselt.
3.  **Wird in der Pipeline suspendiert?** -> Gesamten Block in `APS` mit der `stateMutex` absichern.
4.  **Daten für die UI?** -> `StateFlow` im Kern anlegen und durch die Fassade durchreichen.
5.  **Ressourcen-Cleanup?** -> Sicherstellen, dass neue Hintergrund-Jobs in `APS.stop()` gecancelt werden.