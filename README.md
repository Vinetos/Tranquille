**Disclaimer:** This is a free and open source project, but it relies on third-party web-services for some functions to work. This project is not affiliated with any third-party entities in any other sense.


# Yet Another Call Blocker

The goal of this project is to provide a free and open source application that can block unwanted calls or warn about probable intentions of the callers using a third-party crowdsourced phone number database (from some other proprietary app).


## Features

At this point of time the application is at the very early stages of development.

Currently implemented (more or less):

* Showing a notification with some number info (positive/negative, category, number of negative reviews and such) when the phone's ringing. *Works offline.*
* Loading and displaying a list of reviews for a calling number (accessed by pressing the notification). *Requires internet.*
* Automatic blocking of unwanted calls (may not work on some devices; doesn't work on Android 9+). *Works offline.*
* Automatic update of the database (for offline use). *Requires internet.*


## Available data

The following data is available in the main offline database:

* a phone number,
* a category (telemarketers, dept collectors, scam, etc.),
* a number of negative reviews,
* a number of positive reviews,
* a number of neutral reviews.

The main database may receive delta updates from third-party servers.

The "featured" database provides "names" (company names or short descriptions) for some (presumably) subset of numbers in the main database.

The third-party servers can be queried for a list of detailed user reviews for a specific phone number.
A detailed review contains:

* A rating: positive, negative or neutral.
* A category: each review may have a different one.
* A title and a comment: the actual description the user left for the number.


## Privacy

Protecting the user's privacy is the first concern during development. No personal data is sent to or otherwise shared with anyone.
The only known possible data leaks are the following:

* Database update procedure leaks user's IP address to the update servers.  
  The request also includes current database version (base or updated).
* User review requests leak user's IP address coupled with the phone number in question.  
  Shouldn't be a big deal unless you call it for a bunch of numbers in your phone book.

No other identifiable information is sent with the requests.


## Rationale

Some may find the original application (whose DB and servers are used) hard to trust because of its proprietary nature (and also the use of firebase analytics and the like).  
But since the database behind that application is crowdsourced, some may find it acceptable (in a moral sense) to use that database in a separate open source application.  
Also, this project is meant to be non-commercial. So, there's that.


## License

[AGPLv3](https://www.gnu.org/licenses/agpl-3.0.en.html).  


## Contributing

Any contributions are very welcome.


## Building

Clone the project repo:

```
git clone https://gitlab.com/xynngh/YetAnotherCallBlocker.git
```

Clone the assets repo:

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

Then you can build the app using Android Studio or using Gradle:

```
./gradlew assemble
```
