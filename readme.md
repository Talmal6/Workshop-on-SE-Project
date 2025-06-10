# README — אתחול נתונים (גרסה 3)

מסמך זה מתאר בצורה תמציתית כיצד לאתחל את המערכת בהתאם ל‑**דרישות גרסה 3 – שמירה לאורך זמן וחסינות**.

* קובץ `bootstrap-config.json` — הגדרות חיבור למסד הנתונים ולמערכות חיצוניות.
* קובץ `initial-state.json` — סדרת פעולות חוקיות המבוצעות בשלב האתחול (אופציונלי).

> כאשר הקבצים אינם קיימים, המערכת תעלה עם **מערך נתוני הדגמה** המוגדר במחלקה `UnifiedDataSeeder`. הקבצים מאפשרים להחליף או להרחיב את הנתונים ללא שינוי קוד.

---

## 1 מבנה תיקיות

```text
src/main/resources/bootstrap/
├─ bootstrap-config.json   ← קובץ קונפיגורציה (חובה בסביבת ייצור)
└─ initial-state.json      ← קובץ מצב‑התחלתי   (אופציונלי)
```

> עבור בדיקות יחידה/אינטגרציה יש ליצור את אותה היררכיה תחת `src/test/resources/bootstrap/` ולספק קבצים ייעודיים.

---

## 2 ‎`bootstrap-config.json`

פורמט JSON. כל הערכים חובה בסביבת ייצור.

```jsonc
{
  "datasource": {
    "url": "jdbc:postgresql://<HOST>:5432/marketplace",
    "username": "marketplace_app",
    "password": "********"
  },
  "externalServices": {
    "payment":  { "endpoint": "https://payments.example/api",  "timeoutMillis": 5000 },
    "shipping": { "endpoint": "https://shipping.example/api", "timeoutMillis": 8000 }
  }
}
```

*שדות עיקריים*

| מקש                              | תיאור                                         |
| -------------------------------- | --------------------------------------------- |
| `datasource.url`                 | ‎נתיב JDBC למסד הנתונים (מרוחק).              |
| `datasource.username / password` | פרטי חיבור.                                   |
| `externalServices.*`             | נקודות קצה ו‑timeout‑ים למערכות תשלום ואספקה. |

---

## 3 ‎`initial-state.json` (אופציונלי)

מערך JSON של קריאות Use‑Case. כל אובייקט כולל:

| שדה       | טיפוס  | דוגמה                    |
| --------- | ------ | ------------------------ |
| `useCase` | String | "guest-registration"     |
| `args`    | Array  | \["alice", "secret", {}] |

דוגמה מינימלית:

```jsonc
[
  { "useCase": "guest-registration", "args": ["alice", "pass", {}] },
  { "useCase": "login",              "args": ["alice", "pass"] },
  { "useCase": "open-shop",          "args": ["alice", "BookShop", {}] }
]
```

אם פעולה אחת נכשלת — האתחול נכשל והמערכת לא תעלה, בהתאם לדרישה 4‑b.

---

## 4 סדר האתחול

1. המערכת עולה ↠ Spring שולח `ApplicationReadyEvent`.
2. ‎`UnifiedDataSeeder` בודק אם קיימים משתמשים בטבלה `users`.

    * **קיימים משתמשים** → האתחול מסתיים (טעינת ServiceLocator בלבד).
    * **אין משתמשים**    → המערכת טוענת נתוני‑דמו מובנים ולאחר‑מכן מריצה את ‎`initial-state.json` (אם קיים).
3. כל הפעולות מבוצעות בשכבת השירותים ומסומנות `@Transactional` — נתונים עקביים גם במקרה קריסה.

---

## 5 הוראות מהירות למפתח

| פעולה                                     | מתי לבצע                      |
| ----------------------------------------- | ----------------------------- |
| ליצור את התיקייה `bootstrap/` ב‑resources | תמיד                          |
| למלא ‎`bootstrap-config.json`             | סביבת ייצור / Staging / CI‑H2 |
| למלא ‎`initial-state.json`                | כשנדרש מצב‑התחלתי מותאם       |

בפיתוח מקומי ניתן להריץ את היישום ללא שני הקבצים — נתוני ההדגמה ייטענו אוטומטית.

---

*סוף קובץ*
