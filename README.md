# JimonGramm Android App

Нативное Android приложение для [jimongramm.com](https://jimongramm.com) на базе WebView.

## Как получить APK (бесплатно через GitHub Actions)

### Шаг 1: Создай репозиторий на GitHub
1. Зайди на [github.com](https://github.com) → войди в аккаунт
2. Нажми **New repository**
3. Название: `jimongramm-android`
4. Выбери **Private** (чтобы код был закрытым)
5. Нажми **Create repository**

### Шаг 2: Залей код
```bash
cd jimongramm-android
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/ТВОй_ЮЗЕРНЕЙМ/jimongramm-android.git
git push -u origin main
```

### Шаг 3: Включи GitHub Actions Releases
1. В репозитории зайди в **Settings** → **Actions** → **General**
2. В разделе "Workflow permissions" выбери **Read and write permissions**
3. Нажми **Save**

### Шаг 4: Запусти сборку
- После push код соберётся **автоматически** (~5-7 минут)
- Зайди во вкладку **Actions** → увидишь процесс сборки
- После завершения зайди в **Releases** — там будет готовый APK

### Шаг 5: Поделись ссылкой
Ссылка на скачивание будет выглядеть так:
```
https://github.com/ТВОй_ЮЗЕРНЕЙМ/jimongramm-android/releases/latest
```

## Установка на телефон
1. Скачать APK на Android телефон
2. **Настройки → Безопасность → Разрешить установку из неизвестных источников**
3. Открыть скачанный файл → Установить
4. Готово! Иконка JimonGramm появится на рабочем столе

## Технические детали
- Минимальная версия Android: 5.0 (API 21)
- Целевая версия: Android 14 (API 34)
- Размер APK: ~3-5 МБ
- Открывает: https://jimongramm.com
- Поддержка: JavaScript, LocalStorage, Камера, Геолокация
