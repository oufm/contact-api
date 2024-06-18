`contact-api` is used to add/delete a contact with comandline for Android.

## Setup
Install the app and grant `WRITE_CONTACTS` `READ_CONTACTS` and `Autostart` permission.

## Usage
Add a contact:

```
am broadcast -n com.oufme.contact_api/.ContactReceiver -e operation add -e name "Kitty" -e number "1234567890"
```

Delete a contact:

```
am broadcast -n com.oufme.contact_api/.ContactReceiver -e operation remove -e name "Kitty"
```
