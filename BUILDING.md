# Building

## Clone the project repo

```
git clone https://github.com/Vinetos/Tranquille.git
```

### Clone the assets repo (optional step: allows to avoid the initial DB downloading after installation)

```
git clone https://gitlab.com/xynngh/YetAnotherCallBlocker_data.git
```

Sym-link the assets:

Linux
```
cd YetAnotherCallBlocker/app/src/main/assets/
ln -s ../../../../../YetAnotherCallBlocker_data/assets/sia .
```
Windows
```
cd YetAnotherCallBlocker\app\src\main\assets
mklink /d sia ..\..\..\..\..\YetAnotherCallBlocker_data\assets\sia
```

**or** copy the whole directory `YetAnotherCallBlocker_data/assets/sia` into `YetAnotherCallBlocker/app/src/main/assets/`.


## Build the app

Open and build the project in Android Studio or use Gradle:
```
./gradlew build
```
