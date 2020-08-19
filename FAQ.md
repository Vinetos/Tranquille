# Frequently Asked Questions

If you didn't find an answer to your question, ask [in issues](https://gitlab.com/xynngh/YetAnotherCallBlocker/-/issues).


## The app doesn't prevent a short sound before a call is blocked.

See ["Advanced call blocking mode"](FAQ.md#whats-that-advanced-call-blocking-mode).


## Do I have to set Yet Another Call Blocker as the default "Phone app"?

Not necessarily. See ["Advanced call blocking mode"](FAQ.md#whats-that-advanced-call-blocking-mode).


## Is there a whitelist? How can I allow a particular number with negative rating to call me?

There's no whitelist and it's currently not planned because it adds complexity (you may create an [issue](https://gitlab.com/xynngh/YetAnotherCallBlocker/-/issues) if you want to convince me otherwise).  
Contacts are never blocked (you need to enable "use contacts"), so you can simply add that specific number to your contacts and it will be able to call you.


## Can I block all numbers not present in Contacts?

There's no dedicated option for that, but you can still achieve the effect: enable the "use contacts" option and create a blacklist pattern matching any number (`*`). The app never blocks contacts and all the other numbers will be blocked by the blacklist pattern. You also need the "block hidden numbers" option enabled to have hidden numbers blocked.

Also, modern Android versions have "Do not Disturb" mode that may be customized to block non-contacts.

If you think Yet Another Call Blocker should have this option, [create an issue](https://gitlab.com/xynngh/YetAnotherCallBlocker/-/issues).


## Calls aren't blocked, I don't get any informational notifications

Check that you granted all the requested permissions (the app asks for missing permissions when you open its main screen).

The app may have troubles with these features on stock firmwares of some manufacturers (like Xiaomi's MIUI). You may try enabling the ["advanced call blocking mode"](FAQ.md#whats-that-advanced-call-blocking-mode) (in Yet Another Call Blocker settings). This feature should fix call blocking, but won't help with the informational notifications ("call blocked" notifications will work fine though).


## What's that "Advanced call blocking mode"?

That's a mode that uses a modern call blocking method ([CallScreeningService](https://developer.android.com/reference/android/telecom/CallScreeningService)-based) that allows to block calls before the phone starts ringing (the "classic" mode cannot block calls fast enough, so your phone may ring for a very short time before the call gets blocked).  
For this feature to work the app needs to be set as the "Phone app" (Android 7–9) or as the "Caller ID app" (Android 10+). The app doesn't replace any in-call UI - your pre-installed Dialer app is used for calls (or the "Phone app" selected by you on Android 10+).  
Obviously, on Android 7–9 you can't enable this feature **and** select some third-party Dialer app. This is an Android's restriction, I'm not aware of any way to work around it.


## The app doesn't have a persistent notification. Does it work?

Yet Another Call Blocker doesn't have permanent notifications because it doesn't have any always-running services. The only background work it may do is optional auto-updates and call handling (which is limited to the duration of the corresponding events). So yeah, it does work ([unless it doesn't](FAQ.md#calls-arent-blocked-i-dont-get-any-informational-notifications)).


## I don't want to see some of the informational notifications, can I disable them? Can I change notification priorities?

If you don't want to receive some notifications (like notifications for calls from your contacts), you should use Android's [notification channels](https://www.androidcentral.com/notification-channels) feature to disable particular notification types or change their priorities. Yet Another Call Blocker provides plenty of notification channels for you to customize.  
On pre-Android 8 devices there's a couple of notification-related options in the settings.

There's also an option to disable all the informational notifications at once.


## What countries are covered by the offline number database?

I'm not sure to be honest. But I believe most of the world is covered.  
You can install the app and look up some recent unwanted calls (if you have any) to see whether the app would have blocked them for you.


## How do wildcards in the blacklist work?

`*` matches zero or more digits, `#` matches exactly one digit.  
So a pattern `+123*` will match any number starting with `+123`.  
A pattern `*123` will match any number ending with `123`.  
A pattern `*123*` will match any number that contains `123`.  
A pattern `+123##` will match any 5-digit number starting with `+123` (like `+12345`).

The number format *must* match the format that Android uses, that's why the leading `+` with country code is usually important.


## How do blocking options work exactly?

1. If "use contacts" is enabled and the number is in contacts, the call is **not blocked** (regardless of other options).  
  Extra information (if any) about the number is displayed anyway.
1. If "block hidden numbers" is enabled and the number is hidden, the call is **blocked**.  
  Theoretically, failure to detect number may result in a call from a contact to be blocked, but I haven't heard about it ever happening.
1. If "block based on rating" is enabled and the number has a *negative rating*, the call is **blocked**.  
  Currently "negative rating" means that the number has more negative reviews than the sum of neutral and positive reviews.
1. If "block blacklisted numbers" is enabled and the number matches any valid blacklist pattern, the call is **blocked**.


## Is there a way to display an overlay/pop-up screen with caller information?

Not yet. If you want this feature, vote for [this issue](https://gitlab.com/xynngh/YetAnotherCallBlocker/-/issues/3).


## There's plenty of other \[better looking, with more features\] Android call blocking apps around. Why should I use yours?

You don't have to. If you're happy with some other app - good for you.  
This project was started because I needed to help my non-techie relatives fight phone spam. Giving calls and contacts permissions to some proprietary app is just not an option for me.  
There's only a few FOSS (free and open source) apps that do call blocking and none of them has any kind of crowdsourced blacklist. So I created Yet Another Call Blocker to solve this.  
After a while the app got new features, some of which are unique on the FOSS scene (for example, I believe that the "advanced call blocking mode" is not present in any other FOSS app).


## I have "block hidden numbers" enabled, but some hidden/"private" numbers are still not blocked.

Hidden number detection is not properly standardized in Android. It took quite some effort to implement the feature as it is, but it was mostly borrowed code and guesswork.  
It probably works better in ["advanced call blocking mode"](FAQ.md#whats-that-advanced-call-blocking-mode).

If you receive a hidden call that wasn't blocked, [create a "crash report"](FAQ.md#how-to-report-a-crash-get-app-logs) and send it to me so I can improve the feature. You can send a report even if you didn't have "block hidden numbers" enabled at the moment - the report should be just as useful.


## Can I use the app with VPN/Tor?

Sure! There's no proxy settings in the app, but system-wide tunnels should work fine. The initial database download (from gitlab) via Tor previously failed due to gitlab making extra checks, but I implemented a hack that should work for now. You can always perform the initial download (no identifiable information used) using normal internet connection. You can even avoid it by [embedding main DB](FAQ.md#the-app-takes-too-much-storage-space-what-can-i-do).


## The app takes too much storage space. What can I do?

Normally the app takes a little under 120 MB in total: ~7 MB for the APK and ~110 MB for data (the offline number rating database).

If you really want to save space, you can build the app yourself embedding the "base" database (see the optional step in [build instructions](BUILDING.md#clone-the-assets-repo-optional-step-allows-to-avoid-the-initial-db-downloading-after-installation)). The APK size would increase to ~30 MB, but the app data size will decrease by 90 MB. You won't need to perform that "initial DB downloading" on first start. The downside is that you'll eventually (once in a couple of months) have to rebuild the app with new base DB (and you won't be able to update via F-Droid).


## What's the source of that "third-party crowdsourced phone number database"?

I'm not sure that publicly name the source is a great idea, I didn't ask for a permission to use it after all. Finding out the source is quite easy anyway.


## Is there any plans for X feature?

Check [issues](https://gitlab.com/xynngh/YetAnotherCallBlocker/-/issues). If there's nothing about it, create a new one and ask there.


## How to report a crash / get app logs?

Sometimes reporting steps needed to reproduce a problem is enough, but most of the time you need to provide extra information in the form of app logs.  
You can get app logs ([logcat](https://developer.android.com/studio/debug/am-logcat) output) by going to "Settings -> Advanced settings" and pressing "Export logcat".  
As it says in the description, the logs may contain some personal information - don't post it publicly without checking.  
If you redact personal data (which you should), *replace* numbers (with random numbers, preferably without changing format) instead of *removing* them completely. Otherwise it is hard to tell whether the number was missing in the app or you removed it. That is especially important when dealing with hidden numbers.
