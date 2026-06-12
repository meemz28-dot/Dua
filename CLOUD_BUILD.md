# بناء APK في السحابة عبر GitHub Actions (دون تثبيت أي شيء)

هذه الطريقة تبني ملف APK على خوادم GitHub مجاناً وتعطيك إيّاه للتنزيل.

## الخطوات

### 1) أنشئ مستودعاً على GitHub
- ادخل https://github.com → New repository.
- سمِّه مثلاً `duaa-android`، واجعله Private أو Public (كلاهما يعمل، وActions مجاني للعام تماماً وبحصة شهرية للخاص).

### 2) ارفع المشروع
الطريقة الأسهل (بدون أوامر):
- في صفحة المستودع الجديد اضغط **uploading an existing file**.
- فُكّ ضغط `DuaaWaZiyara-android.zip` على جهازك، ثم اسحب **محتويات** مجلّد `android` (وليس المجلّد نفسه) إلى الصفحة — أي تأكّد أن `app/` و`settings.gradle.kts` ومجلّد `.github/` في جذر المستودع.
- اضغط **Commit changes**.

أو عبر Git إن كان مثبتاً لديك:
```bash
cd android
git init && git add . && git commit -m "first commit"
git branch -M main
git remote add origin https://github.com/USERNAME/duaa-android.git
git push -u origin main
```

> مهمّ: تأكّد أن مجلّد `.github/workflows/` مرفوع. بعض أدوات فكّ الضغط تُخفي المجلّدات التي تبدأ بنقطة — فعّل إظهار الملفات المخفية.

### 3) شغّل البناء
- بمجرّد رفع الملفات إلى فرع `main` سيبدأ البناء تلقائياً.
- أو اذهب إلى تبويب **Actions** → اختر **Build APK** → **Run workflow**.

### 4) نزّل الـ APK
- افتح تبويب **Actions** → ادخل آخر تشغيل (علامة ✓ خضراء بعد ~3-5 دقائق).
- في أسفل الصفحة قسم **Artifacts** → نزّل **duaa-debug-apk**.
- فُكّ الضغط لتحصل على `app-debug.apk`.

### 5) ثبّته على هاتفك
- انقل الملف إلى الهاتف (أو نزّله مباشرة عليه).
- افتحه، واسمح بـ «تثبيت من مصادر غير معروفة» إن طُلب.

---

## (اختياري) بناء حزمة AAB موقّعة للنشر على Play

الوظيفة `build-release` في الـ workflow تبني `.aab` موقّعاً، لكن فقط إذا أضفت أسرار التوقيع:

1. أنشئ مفتاحاً على جهازك:
   ```bash
   keytool -genkey -v -keystore keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias duaa
   ```
2. حوّله إلى Base64:
   ```bash
   base64 -w0 keystore.jks    # على ماك: base64 -i keystore.jks
   ```
3. في المستودع: **Settings → Secrets and variables → Actions → New repository secret**، وأضف:
   - `KEYSTORE_BASE64` = ناتج الأمر أعلاه
   - `STORE_PASSWORD` = كلمة مرور المخزن
   - `KEY_ALIAS` = `duaa`
   - `KEY_PASSWORD` = كلمة مرور المفتاح
4. شغّل الـ workflow يدوياً (Run workflow) — ستجد `duaa-release-aab` في Artifacts.

---

## إن فشل البناء
افتح خطوة **Build debug APK** في صفحة التشغيل، وانسخ رسالة الخطأ وأرسلها لي — أُصلحها فوراً. هذه أول عملية ترجمة فعلية للكود، فإن ظهر خطأ بسيط فهذا متوقّع وسهل الحل.
