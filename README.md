# تطبيق «الدعاء والزيارة» — Android (Kotlin + Jetpack Compose)

تطبيق أندرويد أصلي (Native) لكتاب «الدعاء والزيارة» للسيد محمد الحسيني الشيرازي.
يعمل **بالكامل دون إنترنت**. يحتوي على: المكتبة والبحث، القارئ، خِيرة القرآن،
مواقيت الصلاة **مع تنبيهات أصلية**، التقويم الهجري بمناسبات الشيعة الإمامية،
تسبيح الزهراء، المفضلة، والإعدادات (مظهر فاتح/داكن، حجم الخط، طريقة الحساب، الموقع).

---

## 1) المتطلبات

- **Android Studio** (إصدار Koala 2024.1 أو أحدث).
- أو سطر الأوامر مع Gradle و Android SDK.
- JDK 17 (يأتي مدمجاً مع Android Studio).

## 2) فتح المشروع وبناؤه (الطريقة الموصى بها)

1. افتح Android Studio → **Open** → اختر مجلّد `DuaaWaZiyara`.
2. سينزّل Android Studio تلقائياً: Gradle wrapper، إضافات Android، ومكتبات Compose.
   (هذه الخطوة تحتاج إنترنت **مرة واحدة فقط** وقت البناء — التطبيق نفسه يعمل دون إنترنت.)
3. انتظر اكتمال المزامنة (Gradle Sync).
4. للتجربة: وصّل جهازاً أو شغّل محاكياً، ثم اضغط **Run ▶**.

> ملاحظة: لم أُضمّن ملف `gradle/wrapper/gradle-wrapper.jar` (يحتاج تنزيلاً).
> Android Studio يولّده تلقائياً عند الفتح. أو نفّذ `gradle wrapper` إن كان Gradle مثبتاً لديك.

## 3) بناء حزمة الإصدار للنشر (AAB لمتجر Play)

Google Play يتطلّب **Android App Bundle (.aab)** موقّعاً.

### أ) إنشاء مفتاح التوقيع (مرّة واحدة)
```bash
keytool -genkey -v -keystore keystore.jks -keyalg RSA -keysize 2048 \
  -validity 10000 -alias duaa
```
احفظ كلمة المرور والـ alias في مكان آمن (إن فقدتها لا يمكنك تحديث التطبيق لاحقاً).

### ب) ضبط متغيرات البيئة للتوقيع
```bash
export DUAA_KEYSTORE=$(pwd)/keystore.jks
export DUAA_STORE_PASSWORD=كلمة_مرور_المخزن
export DUAA_KEY_ALIAS=duaa
export DUAA_KEY_PASSWORD=كلمة_مرور_المفتاح
```

### ج) بناء الحزمة
```bash
./gradlew bundleRelease     # ينتج .aab للنشر على Play
# أو
./gradlew assembleRelease   # ينتج .apk للتثبيت المباشر (Sideload)
```
الناتج:
- `app/build/outputs/bundle/release/app-release.aab`
- `app/build/outputs/apk/release/app-release.apk`

> إن لم تضبط مفتاح التوقيع، سيبني الوضع `debug` بمفتاح تجريبي تلقائي للتجربة فقط.

## 4) النشر على Google Play

1. ادخل إلى **Google Play Console** ($25 رسوم تسجيل لمرّة واحدة).
2. أنشئ تطبيقاً جديداً، واملأ بيانات المتجر (انظر مجلّد `store-listing/`).
3. ارفع ملف `app-release.aab`.
4. فعّل **Play App Signing** (موصى به — يحفظ مفتاحك لدى Google).
5. املأ استبيان المحتوى، سياسة الخصوصية، والفئة (Books & Reference أو Lifestyle).
6. أرسل للمراجعة.

## 5) الأذونات المستخدمة ولماذا

| الإذن | السبب |
|------|-------|
| `POST_NOTIFICATIONS` | إظهار تنبيه دخول وقت الصلاة (أندرويد 13+). |
| `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` | جدولة التنبيه في وقت الصلاة بدقّة. |
| `RECEIVE_BOOT_COMPLETED` | إعادة جدولة التنبيهات بعد إعادة تشغيل الجهاز. |
| `ACCESS_*_LOCATION` (اختياري) | تحديد الموقع تلقائياً لحساب المواقيت (يمكن اختيار مدينة يدوياً بدلاً منه). |

لا يطلب التطبيق إنترنت ولا يجمع أي بيانات؛ كل شيء محلي على الجهاز.

## 6) بنية المشروع

```
app/src/main/java/com/shirazi/duaa/
  MainActivity.kt           نقطة الدخول، RTL، إذن الإشعارات
  data/Data.kt              كل النصوص (49 دعاءً/زيارة) + الخيرة + المناسبات + المدن
  data/Settings.kt          حفظ الإعدادات والمفضلة محلياً
  logic/HijriCalendar.kt    تحويل التقويم الهجري
  logic/PrayerTimes.kt      حساب المواقيت فلكياً + منتصف الليل الشرعي
  logic/ArabicSearch.kt     تطبيع البحث العربي
  notify/PrayerScheduler.kt جدولة التنبيهات (AlarmManager)
  notify/PrayerReceiver.kt  استقبال المنبّه وإظهار الإشعار
  notify/BootReceiver.kt    إعادة الجدولة بعد الإقلاع
  ui/                       واجهة Compose: الثيم، الشاشات، الأوراق السفلية
```

## 7) ملاحظات دينية

- النصوص بنُسخها المتداولة المعروفة وقد تختلف يسيراً عن الطبعة الورقية؛
  يُرجى مراجعة المطبوع عند الحاجة للدقّة، خصوصاً في الزيارات الطويلة.
- المواقيت وبدايات الأشهر الهجرية تقريبية، فيُحتاط في العبادات.

---

### English quick start
Open the `DuaaWaZiyara` folder in Android Studio → wait for Gradle sync → Run.
For Play: create a keystore, set the `DUAA_*` env vars, run `./gradlew bundleRelease`,
upload the `.aab` to Play Console. The app is fully offline and collects no data.
